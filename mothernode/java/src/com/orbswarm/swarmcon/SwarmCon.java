
package com.orbswarm.swarmcon;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.text.*;
import org.trebor.pid.*;

import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.OrbControl;

import com.orbswarm.swarmcomposer.color.*;
import com.orbswarm.swarmcomposer.composer.BotVisualizer;

import static org.trebor.util.ShapeTools.*;
import static java.lang.System.*;
import static java.awt.Color.*;
import static java.lang.Math.*;
import static javax.swing.KeyStroke.*;
import static java.awt.event.KeyEvent.*;

public class SwarmCon extends JFrame implements OrbControl
{
         // global source of randomness

      public static final Random RND = new Random();

         /** minimum frame delay in milliseconds */

      public static final long MIN_FRAME_DELAY = 10;

         // some general parameters

      public static final double ORB_RADIUS        =   0.5; // meters
      public static final double MAX_ROLL          =  35.0; // deg
      public static final double MAX_ROLL_RATE     =  30.0; // deg/sec
      public static final double DROLL_RATE_DT     =  10.0; // deg/sec
      public static final double MAX_PITCH_RATE    = 114.6; // deg/sec
      public static final double DPITCH_RATE_DT    =  40.0; // deg/sec
      public static final double MAX_YAW_RATE      =  30.0; // deg/sec
      public static final double DYAW_RATE_DT      =  30.0; // deg/sec
      public static final double ORB_DIAMETER      =   1.0; // meters
      public static final double SAFE_DISTANCE     =   3.0; // meters
      public static final double CRITICAL_DISTANCE =   2.0; // meters
      public static final int    INITIAL_ORBS      =   6  ; // orbs
      public static final int    ORB_SPAR_COUNT    =   4  ; // arcs

         /** time in seconds for a phantom to move to it's target postion */

      public static final double PHANTOM_PERIOD    =  2  ;

         /** scale for graphics */

      public static final double PIXLES_PER_METER  = 30.0;

         /** arena in which we play */

      final JPanel arena = new JPanel()
         {
               public void paint(Graphics graphics)
               {
                  paintArena(graphics);
               }
         };

         /** card layout for main view area */

      CardLayout cardLayout;

         /** center panel which is the main view area */

      JPanel centerPanel;

         /** pid tuner object */

      public PidTuner tuner;

         /** communcation with the outside world */
      
      OrbIo orbIo;

         /**  Specialists listening to OrbState messages */
      ArrayList specialists = new ArrayList();


         // color

      public static Color BACKGROUND    = WHITE;
      public static Color TEXT_CLR      = new Color(  0,   0,   0, 128);
      public static Color MENU_CLR      = new Color(  0,   0,   0, 128);
      public static Color ORB_CLR       = new Color(196, 196, 196);
      public static Color ORB_FRAME_CLR = new Color( 64,  64,  64);
      public static Color SEL_ORB_CLR   = new Color(255, 196, 255);
      public static Color VECTOR_CRL    = new Color(255,   0,   0, 128);
      public static Font  MISC_FONT     = new Font("Helvetica", 
                                                   Font.PLAIN, 15);
      public static Font  ORB_FONT      =  new Font("Helvetica", 
                                                    Font.PLAIN, 10);
      public static Font  MENU_FONT      =  new Font("Helvetica", 
                                                    Font.PLAIN, 15);

      static
      {
         ORB_FONT = scaleFont(ORB_FONT);
      }

         /** Scale a font which are to be used in arena units 
          *
          * @param original font to be scale
          * @return a scaled copy of the font
          */

      public static Font scaleFont(Font original)
      {
         return original.deriveFont(
            (float)(original.getSize() 
                    / PIXLES_PER_METER));
      }

         /** Standard button font */

      public static Font BUTTON_FONT = (new Font("Lucida Grande",
                                                 Font.PLAIN, 1)).
                                        deriveFont(4f);

         // fix font sizes

         /** swarm of mobjects (not just orbs) (maybe this should not be
          * called the swarm?) */

      Swarm swarm;

         /** selected objects */

      Mobjects selected = new Mobjects();
      
         /** phantom objects */

      Vector<Phantom> phantoms = new Vector<Phantom>();
      
         /** last time mobects were updated */
      
      Calendar lastUpdate = Calendar.getInstance();
      
         /** format for printing heading values */
      
      public static NumberFormat HeadingFormat = NumberFormat.getNumberInstance();
      public static NumberFormat StdFormat = NumberFormat.getNumberInstance();
      
         /** static initializations */

      static
      {
         HeadingFormat.setMaximumIntegerDigits(3);
         HeadingFormat.setMinimumIntegerDigits(3);
         HeadingFormat.setMaximumFractionDigits(0);
         StdFormat.setMinimumIntegerDigits(1);
         StdFormat.setMaximumFractionDigits(2);
         StdFormat.setMinimumFractionDigits(2);
      }
         // entry point

      public static void main(String[] args)
      {
         SwarmCon c = new SwarmCon();
         setupColorSchemeSpecialist(c);
      }

    
         // construct a swarm

      public SwarmCon()
      {
            // construct the frame
         
         constructFrame(getContentPane());
         
            // get the graphics device from the local graphic environment
         
         GraphicsDevice gv = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getScreenDevices()[0];
         
            // if full screen is supported setup frame accoringly 
         
         if (false && gv.isFullScreenSupported())
         {
            setUndecorated(true);
            setVisible(true);
            pack();
            gv.setFullScreenWindow(this);
         }
            // otherwise just make a big frame

         else
         {
            pack();
            setExtendedState(MAXIMIZED_BOTH);
            setVisible(true);
         }
         cardLayout.first(centerPanel);

            // start the animation thread

         new Thread()
         {
               public void run()
               {
                  try {sleep(1000); repaint();} 
                  catch (Exception e) {e.printStackTrace();}
                  lastUpdate = Calendar.getInstance();
                  while (true) {update();}
               }
         }.start();
      }

      public Controller[] addOrbs(Rectangle2D.Double bounds)
      {
         swarm = new Swarm(bounds);
         
         Mobject preveouse = new MouseMobject(arena);
         swarm.add(preveouse);
         Controller[] controllers = null;

            // construct the swarm

         for (int i = 0; i < INITIAL_ORBS; ++i)
         {
               // create an orb

            Orb orb = new Orb(swarm, new SimModel());
            controllers = ((SimModel)orb.getModel()).getControllers();
               // add behvaiors

            swarm.add(orb);
            Behavior wb = new WanderBehavior();
            Behavior fb = new FollowBehavior(preveouse);
            Behavior rb = new RandomBehavior();
            Behavior cb = new ClusterBehavior();
            Behavior fab = new AvoidBehavior(fb);
            Behavior cab = new AvoidBehavior(cb);
               //orb.add(wb);
            orb.add(fb);
            orb.add(rb);
            orb.add(cb);
            orb.add(fab);
            orb.add(cab);

            preveouse = orb;
         }
         swarm.nextBehavior();

         return controllers;
      }
         // update the world

      public void update()
      {
         synchronized (swarm)
         {
               // sleep until it's been a minimum frame delay
            
            try
            {
               long start = lastUpdate.getTimeInMillis();
               while (currentTimeMillis() - start < MIN_FRAME_DELAY)
                  Thread.sleep(10);
            }
            catch (Exception ex)
            {
               System.out.println(ex);
            }
               // get now

            Calendar now = Calendar.getInstance();
            double time = (now.getTimeInMillis() 
                           - lastUpdate.getTimeInMillis()) / 1000d;

               // establish the time since last update

            lastUpdate = now;

               // update all the objects

            swarm.update(time);

            swarm.updateOrbDistances();
            broadcastOrbState();
            
               // repaint the screen

            arena.repaint();
         }
      }

          // add a Specialist to list of OrbState receivers
      public void addSpecialist(Specialist sp) 
      {
          specialists.add(sp);
      }
    
         // broadcast OrbState messages to all the Specialists\
      public void broadcastOrbState() 
      {
          for(Iterator it=specialists.iterator(); it.hasNext(); ) {
              Specialist specialist = (Specialist)it.next();
              specialist.orbState(swarm);
          }
      }
 
         // place gui object into frame
      
      public void constructFrame(Container frame)
      {
            // frame closes on exit

         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // set frame to box layout
         
         frame.setLayout(new BorderLayout());
         
            // paint area mouse listener
         
         MouseInputAdapter mia = new SwarmMia();
         
            // create center Panel
         
         centerPanel = new JPanel();
         cardLayout = new CardLayout();
         centerPanel.setLayout(cardLayout);

            // splash panel

         JPanel splash = new JPanel();
         splash.setLayout(new BoxLayout(splash, BoxLayout.Y_AXIS));
         splash.add(Box.createVerticalGlue());
         JButton button = new JButton(simulation);
         splash.add(button);
         button.setAlignmentX(Component.CENTER_ALIGNMENT);
         button.setAlignmentY(Component.CENTER_ALIGNMENT);
         splash.add(Box.createVerticalGlue());
         for (String portId :OrbIo.listSerialPorts())
         {
            button = new JButton(new SwarmComPortAction(portId));
            splash.add(button);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setAlignmentY(Component.CENTER_ALIGNMENT);
         }
         splash.add(Box.createVerticalGlue());
         centerPanel.add(splash, "splash");

            // setup paint area
         
         arena.addMouseMotionListener(mia);
         arena.addMouseListener(mia);
         centerPanel.add(arena, "arena");


         frame.add(centerPanel, BorderLayout.CENTER);

            // init Swarm

         Controller[] controllers = addOrbs(new Rectangle2D
                                            .Double(0, 0, 20, 20));
//             .Double(arena.getBounds().getX() / PIXLES_PER_METER,
//                     arena.getBounds().getY() / PIXLES_PER_METER,
//                     arena.getBounds().getWidth()  / PIXLES_PER_METER,
//                     arena.getBounds().getHeight() / PIXLES_PER_METER));
         
            // add pid tuner

            //add(tuner = new PidTuner(controllers), BorderLayout.EAST);

            // add actions

         InputMap inputMap = getRootPane().getInputMap();
         ActionMap actionMap = getRootPane().getActionMap();
         for (SwarmAction a: actions)
         {
            inputMap.put(a.getAccelerator(), a.getName());
            actionMap.put(a.getName(), a);
         }
            // add menu

         JMenuBar menuBar = new JMenuBar();
         menuBar.setBorder(null);
         add(menuBar, BorderLayout.NORTH);
         JMenu fileMenu = new JMenu("file");
         fileMenu.setForeground(MENU_CLR);
         fileMenu.setFont(MENU_FONT);
         menuBar.add(fileMenu);
         for (SwarmAction a: actions)
         {
            JMenuItem menu = new JMenuItem(a);
            menu.setFont(MENU_FONT);
            menu.setForeground(MENU_CLR);
            fileMenu.add(menu);
         }
      }
         /** Establish the pattern of phantoms on the screen. */

      public void configurePhantoms()
      {
         int count = phantoms.size();

            // compute 90 % of minimum dimention which is the maximum
            // size to take up

         double maxSize = min(
            swarm.getArena().getWidth(),
            swarm.getArena().getHeight()) * 0.9d;

            // find the center of the arena

         Point2D.Double center = swarm.getCenter();

            // if we've got 1 orb, size it real big

         if (count == 1)
         {
            Phantom p = phantoms.get(0);
            p.setTarget(center, maxSize / p.getSize());
         }
         else if (count > 1)
         {
            double size = phantoms.get(0).getSize();
            double scale = maxSize / ((3 * size) -
                                      (size / 4 * (6 - count)));
            double radius = scale * size;
            double dAngle = 2 * PI / phantoms.size();
            double angle = 0;
            for (Phantom p: phantoms)
            {
               p.setTarget(new Point2D.Double(
                              center.getX() + cos(angle) * radius,
                              center.getY() + sin(angle) * radius),
                           scale);
               angle += dAngle;
            }
         }
      }
         /** Paint all objcts in arena.
          *
          * @param graphics graphics object to paint onto
          */

      Calendar lastPaint = Calendar.getInstance();

      public void paintArena(Graphics graphics)
      {
            // config graphics
                     
         Graphics2D g = (Graphics2D)graphics;
         g.setColor(BACKGROUND);
         g.fillRect(0, 0, getWidth(), getHeight());
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                            RenderingHints.VALUE_ANTIALIAS_ON);

         g.setColor(TEXT_CLR);
         g.setFont(MISC_FONT);
         g.drawString("frame delay: " + 
                      (currentTimeMillis() - lastPaint.getTimeInMillis())
                      + "ms", 5, 15);
         lastPaint = Calendar.getInstance();

            // draw current behavior
                     
         if (swarm != null)
         {
            synchronized (swarm)
            {
               int id = 1;
               for (Mobject mobject: swarm)
                  if (mobject instanceof Orb)
                  {
                     Orb orb = (Orb)mobject;
                     Behavior behavior = orb.getBehavior();
                                 
                     g.setColor(TEXT_CLR);
                     g.setFont(MISC_FONT);
                     g.drawString(
                        orb.getId() + ": " + 
                        (behavior != null 
                         ? behavior.toString() 
                         : "[none]") +
                        " R: " + HeadingFormat.
                        format(round(orb.getRoll ())) +
                        " P: " + HeadingFormat.
                        format(round(orb.getPitch())) +
                        " Y: " + HeadingFormat.
                        format(round(orb.getYaw  ())) +
                        " YR: " + HeadingFormat.
                        format(round(orb.getYawRate())) +
                        " V: " + round(orb.getSpeed() * 100) / 100d,
                        5, 15 + id++ * 15);
                  }
            }
         }
            // set 0,0 to lower left corner, and scale for meters

         g.translate(0, getHeight());
         g.scale(PIXLES_PER_METER, -PIXLES_PER_METER);
                     
            // draw mobjects
                     
         if (swarm != null)
         {
            synchronized (swarm)
            {
               for (Mobject mobject: swarm)
                  mobject.paint(g);
            }
         }
      }
                  
         // object which is always set to the position of the mouse

      public class MouseMobject extends Mobject
      {
            JPanel arenax = arena;

               // shape to be drawn

            Shape shape = new Ellipse2D.Double(
               -ORB_DIAMETER / 4, -ORB_DIAMETER / 4,
               ORB_DIAMETER / 2, ORB_DIAMETER / 2);

               // construct a MouseMobject

            public MouseMobject(Component arena)
            {
               super(ORB_DIAMETER / 2);
               MouseInputAdapter mia = new MouseInputAdapter()
                  {
                        public void mouseMoved(MouseEvent e)
                        {
                           setPosition(e.getX() / PIXLES_PER_METER,
                                       (arenax.getHeight() - e.getY()) /
                                       PIXLES_PER_METER);
                        }
                  };
               
                  arena.addMouseListener(mia);
                  arena.addMouseMotionListener(mia);
            }
               /** Is the given point (think mouse click point) eligable to
                * select this object?
                *
                * @param clickPoint the point where the mouse was clicked
                */
            
            public boolean isSelectedBy(Point2D.Double clickPoint)
            {
               return false;
            }
               // update positon of this object

            public void update(double time) {}

               // paint this object onto a graphics area

            public void paint(Graphics2D g)
            {
               g.setColor(RED);
               g.fill(translate(shape, getX(), getY()));
            }
      }
         // create arrow shape

      public static Shape createArrow()
      {
         GeneralPath gp = new GeneralPath();
         Shape square = new Rectangle2D.Double(-.5, -.5, 1, 1);
         gp.append(square, false);
         gp.append(translate(createRightTriangle(), 0, - .5), false);
         return normalize(gp);
      }
         // create right triangle

      public static Shape createRightTriangle()
      {
         Area rTriangle = new Area();
         rTriangle.add(new Area(rotate(new Rectangle2D.Double(-0.5, -0.5, 1, 1), 45)));
         rTriangle.subtract(new Area(new Rectangle2D.Double(-2, 0, 4, 2)));
         return rTriangle;
      }
         /** joystick position */

      static public Point joystick = new Point(0, 0);

         /** set the "joystick" position based on mouse location.  The
          * joystick values are normalized to be between -1 and 1.
          *
          * @param event the mouse event from which to work
          */

      public void setMouseJoy(MouseEvent e)
      {
         joystick.setLocation(
            ((double)e.getX() / arena.getWidth() ) * 2 - 1,
            ((double)e.getY() / arena.getHeight()) * 2 - 1);
      }
         /** swarm mouse input adapter */

      class SwarmMia extends MouseInputAdapter
      {
               // mouse dragged event
            
            public void mouseDragged(MouseEvent e)
            {
            }
               // mouse moved event
            
            public void mouseMoved(MouseEvent e)
            {
               setMouseJoy(e);
            }
               // mouse clicked event
            
            public void mouseClicked(MouseEvent e)
            {
                  // convert point to meters

               Point2D.Double point = new Point2D.Double(
                  e.getX() / PIXLES_PER_METER, 
                  (arena.getHeight() - e.getY()) / PIXLES_PER_METER);

                  // find nearest selectable mobject

               final Mobject nearest = swarm.findSelected(point);

                  // if shift is not down, clear selected

               if (!e.isShiftDown())
               {
                  for (Mobject m: selected)
                     m.setSelected(false);
                  for (Phantom p: phantoms)
                     swarm.remove(p);
                  
                  selected.clear();
                  phantoms.clear();
               }
                  // if nearest found, ad to selected set
               
               if (nearest != null)
               {
                     // set selected
                  
                  nearest.setSelected(true);

                     // add to selected mobjects

                  selected.add(nearest);

                     // add phatom for this mobject

                  Phantom p = new Phantom(
                     nearest, PHANTOM_PERIOD);
                  phantoms.add(p);
                  swarm.add(p);
                  
                     // tell the phatoms to reconfigure themselfs

                  configurePhantoms();
               }
            }
      }
         /** SwarmCon action class */

      abstract class SwarmAction extends AbstractAction
      {
               // construct the action

            public SwarmAction(String name, KeyStroke key, String description)
            {
               super(name);
               putValue(NAME, name);
               putValue(SHORT_DESCRIPTION, description);
               putValue(ACCELERATOR_KEY, key);
            }
               /** Return accelerator key for this action.
                *
                * @return accelerator key for this action
                */

            public KeyStroke getAccelerator()
            {
               return (KeyStroke)getValue(ACCELERATOR_KEY);
            }
               /** Return name of this action.
                *
                * @return name of this action
                */

            public String getName()
            {
               return (String)getValue(NAME);
            }
      }
         /** Action class wich selects a given serial port with witch to
          * commucate to the orbs. */

      class SwarmComPortAction extends SwarmAction
      {
               /** communications port id */

            private String portId;

               /** Construct SwarmComPortAction with a given com port
                * id.
                *
                * @param portId a string representation of com port
                */

            public SwarmComPortAction(String portId)
            {
               super("connect via " + portId, null,
                     "connect to orbs via serial port " + portId);
               this.portId = portId;
            }
            public void actionPerformed(ActionEvent e)
            {
               orbIo = new OrbIo(portId);
               cardLayout.last(centerPanel);
            }
      }
         /** Action to select simulated rather live operation. */
      
      SwarmAction simulation = new SwarmAction(
         "simulate orbs", 
         getKeyStroke(VK_ENTER, 0),
         "simulate orb motion rather then connect to live orbs")
         {
               public void actionPerformed(ActionEvent e)
               {
                  orbIo = null;
                  cardLayout.last(centerPanel);
               }
         };

         /** Action to reset simulation state */
      
      SwarmAction reset = new SwarmAction(
         "reset sim", 
         getKeyStroke(VK_R, 0),
         "reset simulation state")
         {
               public void actionPerformed(ActionEvent e)
               {
                  swarm.randomize();
               }
         };

         /** action to select next orb behavior */
      
      SwarmAction nextBehavior = new SwarmAction(
         "next behavior", 
         getKeyStroke(VK_UP, 0),
         "select next orb behavior")
         {
               public void actionPerformed(ActionEvent e)
               {
                  swarm.nextBehavior();
               }
         };
      
         /** action to select previous orb behavior */
      
      SwarmAction previousBehavior = new SwarmAction(
         "previous behavior", 
         getKeyStroke(VK_DOWN, 0),
         "select previous orb behavior")
         {
            public void actionPerformed(ActionEvent e)
            {
               swarm.previousBehavior();
            }
      };
         /** action to exist the system */
      
      SwarmAction exit = new SwarmAction(
         "exit", 
         getKeyStroke(VK_ESCAPE, 0),
         "exit this program")
         {
               public void actionPerformed(ActionEvent e)
               {
                  System.exit(0);
               }
         };
      
         /** all the actions in one handy place */

      SwarmAction[] actions = 
      {
         reset,
         nextBehavior,
         previousBehavior,
         exit,
      };

    public static void registerColorSchemes() {
        ColorScheme.registerColorScheme("Analogous", ColorSchemeAnalogous.class);
        ColorScheme.registerColorScheme("Split Complement", ColorSchemeSplitComplement.class);
        ColorScheme.registerColorScheme("Split Complement 3", ColorSchemeSplitComplement3.class);
        ColorScheme.registerColorScheme("Triad", ColorSchemeTriad.class);
        ColorScheme.registerColorScheme("Tetrad", ColorSchemeTetrad.class);
        ColorScheme.registerColorScheme("Crown", ColorSchemeCrown.class);
    }
    
    /** Setup the ColorSchemeSpecialist and it's controller interface */
    public static void setupColorSchemeSpecialist(SwarmCon swarmCon) {
        registerColorSchemes();
        ColorSchemeSpecialist colorSchemeSpecialist = new ColorSchemeSpecialist();
        colorSchemeSpecialist.setup((OrbControl)swarmCon, null);
                
        swarmCon.addSpecialist(colorSchemeSpecialist);

        ColorSchemer schemer = new ColorSchemer("Crown");
        schemer.addColorSchemeListener(colorSchemeSpecialist);
        schemer.broadcastNewColorScheme();
        schemer.broadcastColorSchemeChanged();

        colorSchemeSpecialist.addBotColorListener(schemer);

        JPanel colorUIPanel = createColorUIPanel(schemer, null);
        JFrame colorUIFrame = createFrame(colorUIPanel, true);
    }
    
    private static JPanel createColorUIPanel(ColorSchemer colorSchemer, BotVisualizer bv) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(colorSchemer.getPanel(), gbc);
        
        gbc.gridy = 1;
        if (bv != null) {
            panel.add(bv.getPanel(), gbc);
        }
            
        // put sliders in here for the spread and value
        return panel;
    }

    private static JFrame createFrame(JPanel panel, boolean decorated) {
        JFrame frame = new JFrame();
        // the frame for drawing to the screen
        frame.setVisible(false);
        frame.setContentPane(panel);
        frame.setResizable(decorated);
        frame.setUndecorated(!decorated);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        frame.setTitle("Swarm Color Tests");
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    //
    // Implementation of methods from orb.swarmcon.choreography.OrbControl
    //
    public OrbControl getOrbControl() {
        return (OrbControl)this;
    }

    // sound control methods not implemented.
    public int  playSoundFile(int orb, String soundFilePath) {return -1;}
    public void stopSound(int orb) {}
    public void volume(int orb, int volume) {}

    // only one Light control method implemented
    public void orbColor(int orbNum, int hue, int sat, int val, int time) {
        System.out.println("SwarmCon:OrbControl orbColor(orb: " + orbNum + "HSV: [" + hue + ", " + sat + ", " + val + "])");
        float fhue = hue / 255.f;
        float fsat = sat / 255.f;
        float fval = val / 255.f;
        HSV hsv = new HSV(fhue, fsat, fval);
        // time ignored here.
        Color color = hsv.toColor();
        Orb orb = (Orb)swarm.getOrb(orbNum);
        orb.setOrbColor(color);
        // TODO: send color command out on OrbIO, or give it to model, or something. 
    }
    
    public void orbColorFade(int orb,
                             int hue1, int sat1, int val1,
                             int hue2, int sat2, int val2,
                             int time) {}

    //
    // Motion methods
    //
    public void followPath(com.orbswarm.choreography.Point[] wayPoints) {}
    public void stopOrb(int orb) {}
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash) {}
    public String getSoundFileHash(String soundFilePath) {return null;}
    public java.util.List   getSoundFileMappingKeys() {return null;}

}
