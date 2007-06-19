package org.trebor.swarmcon;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import java.text.*;

import static java.lang.System.*;
import static java.awt.Color.*;
import static java.lang.Math.*;

public class SwarmCon extends JFrame
{
         // global source of randomness

      public static final Random RND = new Random();

         /** minimum frame delay in milliseconds */

      public static final long MIN_FRAME_DELAY = 50;

         // some general parameters

      public static final double ORB_RADIUS        =   0.5; // meters
      public static final double MAX_VELOCITY      =   0.5; // meters/sec
      public static final double DVELOCITY_DT      =   2.0; // meters/sec
      public static final double MAX_ROLL          =  35.0; // deg
      public static final double MAX_ROLL_RATE     =  30.0; // deg/sec
      public static final double DROLL_RATE_DT     =  10.0; // deg/sec
      public static final double MAX_PITCH_RATE    = 114.6; // deg/sec
      public static final double DPITCH_RATE_DT    =  10.0; // deg/sec
      public static final double MAX_YAW_RATE      =  30.0; // deg/sec
      public static final double DYAW_RATE_DT      =  30.0; // deg/sec
      public static final double ORB_DIAMETER      =   1.0; // meters
      public static final double SAFE_DISTANCE     =   3.0; // meters
      public static final double CRITICAL_DISTANCE =   2.0; // meters
      public static final int    INITIAL_ORBS      =   1  ; // orbs
      public static final int    ORB_SPAR_COUNT    =   4  ; // arcs
      
         /** time in seconds for a phantom to move to it's target postion */

      public static final double PHANTOM_PERIOD    =  2  ;

         /** scale for graphics */

      public static final double PIXLES_PER_METER  = 65.0;

         /** arena in which we play */

      final JPanel arena = new JPanel()
         {
               public void paint(Graphics graphics)
               {
                  paintArena(graphics);
               }
         };

         /** arena in which we play */

      public PidTuner tuner;

         /** communcation with the outside world */
      
         //OrbIo orbIo;

         // color

      public static Color BACKGROUND    = WHITE;
      public static Color TEXT_CLR      = new Color(  0,   0,   0, 128);
      public static Color ORB_CLR       = new Color(196, 196, 196);
      public static Color ORB_FRAME_CLR = new Color( 64,  64,  64);
      public static Color SEL_ORB_CLR   = new Color(255, 196, 255);
      public static Color VECTOR_CRL    = new Color(255,   0,   0, 128);
      public static Font  MISC_FONT     = new Font("Helvetica", 
                                                   Font.PLAIN, 15);
      public static Font  ORB_FONT      =  new Font("Helvetica", 
                                                    Font.PLAIN, 10);

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
                                        deriveFont(2f);

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
            //Angle.unitTest();
      }
         // construct a swarm

      public SwarmCon()
      {
            // create OrbIo instance
         
            //orbIo = new OrbIo("/dev/cu.usbserial0");

            // construct the frame
         
         constructFrame(getContentPane());
         
            // get the graphics device from the local graphic environment
         
         GraphicsDevice gv = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getScreenDevices()[0];
         
            // if full screen is supported setup frame accoringly 
         
         if (gv.isFullScreenSupported())
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
            
               // repaint the screen

            arena.repaint();
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
         
            // setup paint area
         
         arena.addMouseMotionListener(mia);
         arena.addMouseListener(mia);
         frame.add(arena, BorderLayout.CENTER);

            // add pid tuner

/*         controllers = 
         {
            new PDController("test1", 2, 10),
            new PDController("test2", 1, 5),
         };

*/
            // init Swarm

         Controller[] controllers = addOrbs(new Rectangle2D
                                            .Double(0, 0, 10, 10));
//             .Double(arena.getBounds().getX() / PIXLES_PER_METER,
//                     arena.getBounds().getY() / PIXLES_PER_METER,
//                     arena.getBounds().getWidth()  / PIXLES_PER_METER,
//                     arena.getBounds().getHeight() / PIXLES_PER_METER));

         frame.add(tuner = new PidTuner(controllers), BorderLayout.EAST);

            // add a key listener
         
         addKeyListener(new KeyAdapter()
            {
                  public void keyPressed(KeyEvent e)
                  {
                     int key = e.getKeyCode();
                     int mods = e.getModifiers();

                           // handle unmodified events

                     switch (key)
                     {
                              // move right action
                           
                        case KeyEvent.VK_R:
                           swarm.randomize();
                           break;

                           // move left action
                        
                        case KeyEvent.VK_UP:
                           swarm.nextBehavior();
                           break;
                           
                              // move right action
                           
                        case KeyEvent.VK_DOWN:
                           swarm.previousBehavior();
                           break;
                           
                              // move left action
                           
                        case KeyEvent.VK_LEFT:
                           break;
                           
                              // move right action
                           
                        case KeyEvent.VK_RIGHT:
                           break;
                           
                              // exist the system
                           
                        case KeyEvent.VK_ESCAPE:
                           System.exit(0);
                           break;
                     }
                  }
            });
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
         // rotate a shape

      public static Shape rotate(Shape shape, double degrees)
      {
         return AffineTransform.getRotateInstance(degrees / 180 * Math.PI)
            .createTransformedShape(shape);
      }
         // rotate a shape

      public static Shape rotateAboutCenter(Shape shape, double degrees)
      {
         Rectangle2D bounds = shape.getBounds2D();
         return AffineTransform.getRotateInstance(degrees / 180 * Math.PI,
                                                  bounds.getX() + bounds.getWidth() / 2,
                                                  bounds.getY() + bounds.getHeight() / 2)
            .createTransformedShape(shape);
      }
         // translate a shape

      public static Shape translate(Shape shape, double x, double y)
      {
         return AffineTransform.getTranslateInstance(x, y)
            .createTransformedShape(shape);
      }
         // scale a shape

      public static Shape scale(Shape shape, double x, double y)
      {
         return AffineTransform.getScaleInstance(x, y)
            .createTransformedShape(shape);
      }
         // rotate an area

      public static Area rotate(Area area, double degrees)
      {
         area.transform(AffineTransform.getRotateInstance(degrees / 180 * Math.PI));
         return new Area(area);
      }
         // rotate an area

      public static Area rotateAboutCenter(Area area, double degrees)
      {
         Rectangle2D bounds = area.getBounds2D();
         area.transform(AffineTransform.getRotateInstance(degrees / 180 * Math.PI,
                                                          bounds.getX() + bounds.getWidth() / 2,
                                                          bounds.getY() + bounds.getHeight() / 2));
         return new Area(area);
      }
         // translate an area

      public static Area translate(Area area, double x, double y)
      {
         area.transform(AffineTransform.getTranslateInstance(x, y));
         return new Area(area);
      }
         // scale an area

      public static Area scale(Area area, double x, double y)
      {
         area.transform(AffineTransform.getScaleInstance(x, y));
         return new Area(area);
      }
         // normalize shape (centered at origin, length & with <= 1.0)

      public static Shape normalize(Shape shape)
      {
            // center the shape on the origin

         Rectangle2D bounds = shape.getBounds2D();
         shape = translate(shape,
            -(bounds.getX() + bounds.getWidth() / 2),
            -(bounds.getY() + bounds.getHeight() / 2));

            // normalize size
         
         bounds = shape.getBounds2D();
         double scale = bounds.getWidth() > bounds.getHeight()
            ? 1.0 / bounds.getWidth()
            : 1.0 / bounds.getHeight();
         return scale(shape, scale, scale);
      }
         // create cirlce shape

      public static Shape createCirlcle()
      {
         return normalize(new Ellipse2D.Double(-.5, -.5, 1, 1));
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
         // create arrow shape

      public static Shape createOrbShape()
      {
         Area area = new Area();
         area.add(new Area(createCirlcle()));
         area.subtract(new Area(new Rectangle2D.Double(-.05, -1, .10, 2)));
         return area;
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
            ((double)e.getX() / arena.getWidth()) * 2 - 1,
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
}
