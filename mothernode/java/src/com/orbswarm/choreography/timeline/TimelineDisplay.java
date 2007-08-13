package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.util.StdDraw;
import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.swarmcon.SwarmCon;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;


/**
 *  Class which Displays the timeline.
 *  
 */
public class TimelineDisplay  {
    StdDraw drawer;
    JPanel mainPanel;

    // TODO: we really want looser coupling than this. but for now...
    SwarmCon swarmCon = null;
    
    private int canvasWidth;
    private int canvasHeight;
    private double timelineWidth    = 1000.;
    private double timelineHeight   = 100.;
    private double timeWindowWidth  = 1000.;  // depends on horizontal zoom level
    private double timeWindowHeight = 100.;   // depends on vertical zoom level
    private double hZoom            = 1.f;
    private double vZoom            = 1.f;
    private double timeWindowScrollTime      = 0.;     // time of start of timeWindow
    private double timeWindowEndTime         = 1.0f;    // time of end of timeWindow
    private double timeWindowScrollTimePixel = 0.;     // timePixel of start of timeWindow
    private double timeCursor  = 0.;  // 
    private double timeCursorWidth  = 1.;  // in TimePixels?
    private double eventTrackHeight = timelineHeight / 10;
    private double eventTrackPadding = eventTrackHeight / 10;

    

    private float timelineDuration = 1.0f;

    public Color bgColor, eventColor_past, eventColor_current, eventColor_future;
    public Color eventColor_border, eventColor_text;
    public Color timeCursorColor;

    protected Component repaintComponent;

    private Timeline timeline = null;
    private ArrayList eventTracks = null;
    
    public TimelineDisplay(int canvasWidth, int canvasHeight) {
        this.canvasWidth  = canvasWidth;
        this.canvasHeight = canvasHeight;
        initColors();
        setupDrawer();
        calculateDimensions(true);
        createMainPanel(); // TODO: pass in place to find the choreoography files.
    }

    public void setSwarmCon(SwarmCon val) {
        this.swarmCon = val;
    }
    
    public void setTimeline(String timelinePath) throws IOException {
        Timeline timeline = Timeline.readTimeline(timelinePath);
        setTimeline(timeline);
    }
    
    public void setTimeline(Timeline val) {
        this.timeline = val;
        timelineDuration = this.timeline.getDuration();
        calculateDimensions(true);
        extractEventTracks();
        display(0.f);
        drawer.show(false);
        repaint();
    }
    
    public Timeline getTimeline() {
        return this.timeline;
    }
    
    public void initColors() {
        bgColor            = Color.getHSBColor(.1f,  .05f,  .65f);
        timeCursorColor    = Color.getHSBColor(.01f, .9f,   .9f);
        eventColor_border  = Color.getHSBColor(.8f,  .65f,  .4f);
        eventColor_current = Color.getHSBColor(.65f, .6f,   .9f);
        eventColor_past    = Color.getHSBColor(.65f, .5f,   .6f);
        eventColor_future  = Color.getHSBColor(.65f, .5f,   .65f);
        eventColor_text    = Color.getHSBColor(.95f, .15f, 1.0f);
    }

    public void setupDrawer() {
        drawer = new StdDraw(canvasWidth, canvasHeight);
        setRepaintComponent(drawer.getDrawingPane());
        drawBG(drawer);
        drawer.initbg(bgColor);
        drawer.show(true);
        repaint();
    }

    public void drawBG(StdDraw drawer) {
        drawer.setPenColor_bg(bgColor);
        drawer.filledRectangle_bg(0.0, 0.0, timeWindowWidth, timeWindowHeight);
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public Container getDrawingPane() {
        if (drawer != null) {
            return drawer.getDrawingPane();
        }
        return null;
    }

    public JPanel createMainPanel() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(bgColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        ((JLabel)getDrawingPane()).setBorder(BorderFactory.createLineBorder(Color.RED));
        mainPanel.add(getDrawingPane(), gbc);

        JPanel timelineControls = createTimelineControls();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(timelineControls, gbc);

        return mainPanel;
    }

    public JPanel createTimelineControls() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(bgColor.darker());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox drop = new JComboBox();
        drop.setBackground(bgColor);
        // TODO: find the choreography files and populate dropdown with them
        drop.addItem("== Select a timeline ==");
        drop.addItem("sampletimeline.tml");
        drop.addItem("timeline1.tml");
        drop.addItem("timeline2.tml");
        drop.addItem("timeline3.tml");

        panel.add(drop, gbc); //
        drop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    String timeline = (String)cb.getSelectedItem();
                    // TODO: abstract this to give the timeline to listener
                    //       which will set the timeline in the timeline runner as well.
                    if (swarmCon != null) {
                        swarmCon.setTimeline(timeline);
                    } else {
                        String resourceDir = "../resources/timelines/";
                        String timelinePath = resourceDir + timeline;
                        try {
                            setTimeline(timelinePath);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

        JButton goButton = new JButton(" > ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(goButton, gbc);
        goButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (swarmCon != null) {
                        swarmCon.startTimeline();
                    } else {
                        startTimelining();
                    }
                }
            });

        JButton stopButton = new JButton(" [] ");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(stopButton, gbc);
        stopButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (swarmCon != null) {
                        swarmCon.stopTimeline();
                    } else {
                        // TODO: stopTimelining();
                    }
                }
            });
        return panel;
    }

    public void calculateDimensions(boolean rescaleDrawer) {
        boolean changed = false;
        double newTimeWindowWidth = timelineWidth * hZoom;
        if (newTimeWindowWidth != timeWindowWidth) {
            changed = true;
        }
        timeWindowScrollTimePixel = timeToTimePixel((float)timeWindowScrollTime);

        double timeWindowDuration = timelineDuration / hZoom;
        double newTimeWindowEndTime  = timeWindowScrollTime + timeWindowDuration;
        if (timeWindowEndTime != newTimeWindowEndTime) {
            changed = true;
            timeWindowEndTime = newTimeWindowEndTime;
        }

        System.out.println("\nTimelineDisplay:calculateDimensions...");
        System.out.println("              changed: " +  changed);
        System.out.println("              rescale: " + (rescaleDrawer || changed));
        System.out.println("                hZoom: " + hZoom);
        System.out.println("     timelineDuration: " + timelineDuration);
        System.out.println("   timeWindowDuration: " + timeWindowDuration);
        System.out.println("           timeWindow: {" + timeWindowScrollTime + ", " + timeWindowEndTime + "}");
        System.out.println("   timeWindowWidth: " + timeWindowWidth);
        
        if (rescaleDrawer || changed) {
            drawer.setXscale(0., timeWindowWidth);
            drawer.setYscale(0., timeWindowHeight);
        }
    }

    public void setHZoom(double val) {
        this.hZoom = val;
        calculateDimensions(false);
    }

    public void setTimeWindowScrollTime(double time) {
        if (time != timeWindowScrollTime) {
            this.timeWindowScrollTime = time;
            calculateDimensions(false);
        }
    }
     
    /////////////////////////////////////////
    /// Arrange events into tracks       ////
    /////////////////////////////////////////
    public void extractEventTracks() {
        eventTracks = new ArrayList();
        eventTracks.add(new ArrayList()); // first track!
        for(Iterator it = timeline.getEvents().iterator(); it.hasNext() ; ) {
            Event event = (Event)it.next();
            if (!placeEventInTrack(event, eventTracks)) {
                ArrayList newTrack = new ArrayList();
                newTrack.add(event);
                eventTracks.add(newTrack);
            }
        }
    }

    public boolean placeEventInTrack(Event event, ArrayList eventTracks) {
        // find first track that doesn't have an overlapping event, and place
        // the event there. If can't find one, then make a new track!
        int i=0;
        for(Iterator tracksit = eventTracks.iterator(); tracksit.hasNext(); ) {
            ArrayList track = (ArrayList)tracksit.next();
            if (eventOverlapsAnEventInTrack(event, track)) {
                // not yet. 
            } else {
                track.add(event);
                return true;
            }
            i++;
        }
        return false;
    }

    public boolean eventOverlapsAnEventInTrack(Event event, ArrayList track) {
        for(Iterator it = track.iterator(); it.hasNext(); ) {
            Event testEvent = (Event)it.next();
            if (event.intersects(testEvent)) {
                return true;
            }
        }
        return false;
    }
            
       
    
    /////////////////////////////////////////
    /// Display current state of timeline ///
    /////////////////////////////////////////

    public void testDraw(double startTime) {
        double startPixel = timeToTimePixel(startTime);
        //double startPixel = timeWindowPixel(timeToTimePixel(startTime));
        int nsq = 10;
        double r = timeWindowHeight / nsq / 2.;
        drawer.setPenColor(Color.DARK_GRAY);
        for(int i=0; i < nsq; i++) {
            double x = startPixel + i * 2. * r + r;
            double y = i * 2. * r + r;
            System.out.println("TestDraw.  startPixel: " + startPixel + " sq(" + x + ", " + y + ", " + r + ")");
            if (i != 2) {
                drawer.filledSquare(x, y, r);
            }
        }

    }

    public boolean cycle(float time) {
        System.out.println("TIMELINE cycle[" + time + "]");
        if (time < timeline.getDuration()) {
            display(time);
            return true;
        } else {
            System.out.println("TD: Timeline ended. stopping swarmcon");
            if (swarmCon != null) {
                swarmCon.stopTimeline();
            }
            return false;
        }
    }
    
    public void display(float time) {
        drawer.clearbg(bgColor);
        //drawer.show();
        timeCursor = time;

        //
        // Stack up the events into tracks so that overlapping events can be shown.
        //
        int trackNum = 0;
        for(Iterator tracksit = eventTracks.iterator(); tracksit.hasNext(); ) {
            ArrayList track = (ArrayList)tracksit.next();
            for(Iterator it = track.iterator(); it.hasNext(); ) {
                Event event = (Event)it.next();
                // TODO: take into account indefinite-time events that haven't been started
                if (intersectsVisibleWindow(event)) {
                    displayEvent(event, trackNum);
                }
            }
            trackNum ++;
        }

        // TODO: if there are too many tracks, reduce the trackHeight

        displayTimeCursor(time);
        drawer.show(false);
        repaint();
    }

    public void displayEvent(Event event, int track) {
        float st = event.getStartTime();
        float et = event.getEndTime();
        if (et == Temporal.NO_TIME) {
            et = st;
        }
        //System.out.println("DisplayEvent. name: " + event.getName() + " start: " + st + " end: " + et);
        
        Color eventColor;
        double timeFudge = 0.;
        double evStartX = timeWindowPixel(timeToTimePixel(st));
        double evEndX = timeWindowPixel(timeToTimePixel(et));
        //System.out.println("  {" + evStartX + ", " + evEndX + "}");
        double timeCursorX = timeWindowPixel(timeToTimePixel(timeCursor));
        if (timeCursorX < evStartX - timeFudge) {
            eventColor = eventColor_future;
        } else if (timeCursorX > evEndX + timeFudge) {
            eventColor = eventColor_past;
        } else {
            eventColor = eventColor_current;
        }
        drawer.setPenColor(eventColor);
        double evY = timeWindowHeight - (track + .5) * (eventTrackHeight + eventTrackPadding);
        if (et > st) {
            displayEventDuration(evStartX, evEndX, evY);
            displayEventEndPoint(evEndX, evY);
        }
        displayEventStartPoint(evStartX, evY);
        displayEventText(evStartX + eventTrackHeight * .5, evY - eventTrackHeight * .4, event.getName());
    }

    public void displayEventStartPoint(double evStartX, double evY) {
        double eventTickWidth = eventTrackHeight / 2.;
        double x = evStartX;
        double y =  evY - eventTrackHeight;
        drawer.filledRectangle(x, y,
                               eventTickWidth, eventTrackHeight);
        Color keep = drawer.getPenColor();
        drawer.setPenColor(eventColor_border);
        drawer.line(x, y, x, evY);
        drawer.setPenColor(keep);
    }

    public void displayEventEndPoint(double evEndX, double evY) {
        double eventTickWidth = eventTrackHeight / 2.;
        double x = evEndX - eventTickWidth;
        double y = evY - eventTrackHeight;
        drawer.filledRectangle(x, y, 
                               eventTickWidth, eventTrackHeight);
        Color keep = drawer.getPenColor();
        drawer.setPenColor(eventColor_border);
        double borderX = x + eventTickWidth;
        drawer.line(borderX, y, borderX, evY);
        drawer.setPenColor(keep);
    }

    public void displayEventDuration(double evStartX, double evEndX, double evY) {
        double width = Math.abs(evEndX - evStartX);
        double barHeight = eventTrackHeight * .66666;
        double y = evY - barHeight;
        drawer.filledRectangle(evStartX, y, width, barHeight);
    }

    public void displayEventText(double evStartX, double evY, String text) {
        drawer.setPenColor(eventColor_text);
        drawer.text(evStartX, evY, text);
    }

    public void displayTimeCursor(float time) {
        double timePixel = timeToTimePixel(time);
        double timeWindowPixel = timeWindowPixel(timePixel) - timeCursorWidth / 2.;
        drawer.setPenColor(timeCursorColor);
        drawer.filledRectangle(timeWindowPixel, 0., timeCursorWidth, timeWindowHeight);
    }

    public void repaint() {
        repaintComponent.repaint();
    }

    public void setRepaintComponent(Component ear) {
        this.repaintComponent = ear;
    }

    public boolean intersectsVisibleWindow(Event event) {
        return Temporal.intervalIntersects(event.getStartTime(), event.getEndTime(),
                                           timeWindowScrollTime, timeWindowEndTime);
    }
    
    public double timeToTimePixel(double  time) {
        double fractionOfTime = time / timelineDuration;
        double timeWindowPixel = timelineWidth * fractionOfTime;
        return timeWindowPixel;
    }

    public double timeWindowPixel(double timeWindowPixel) {
        double placeInTimeWindow = timeWindowPixel - timeWindowScrollTimePixel;
        return placeInTimeWindow;
    }
    
    public static void main(String [] args) {
        /*
        if (args.length < 1) {
            System.out.println("Usage: Timeline <timeline file>");
        }
        String timelinePath = args[0];
        */
        try {
            //Timeline timeline = Timeline.readTimeline(timelinePath);
            //System.out.println("Read timeline.");
            //System.out.println(timeline.write(""));

            int cw = 600;
            int ch = 150;
            TimelineDisplay timelineDisplay = new TimelineDisplay(cw, ch); // cw, ch??
            createFrame(timelineDisplay.getPanel());
            //timelineDisplay.setTimeline(timeline);
            timelineDisplay.drawer.show(true);
            timelineDisplay.repaint();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void startTimelining() {
         new Thread()
         {
               public void run()
               {
                  try {sleep(1000); repaint();} 
                  catch (Exception e) {e.printStackTrace();}
                  _startTimelining();
               }
         }.start();
    }
    
    public void _startTimelining() {
        float duration = timeline.getDuration();
        int slices = 500;
        float interval = duration / slices;
        for(int t=0; t < slices; t++) {
            boolean keepGoing = this.cycle(t * interval);
            //this.testDraw(t * interval);
            this.repaint();
            if (!keepGoing) {
                break;
            }
            try {
                Thread.sleep((int)(1000 * interval));
            } catch (Exception ex) {
            }
        }                
    }
    
    public static JFrame createFrame(JPanel drawingPane) {
        JFrame frame = new JFrame();
        frame.setContentPane(drawingPane);
        frame.setTitle("Timeline display");
        frame.pack();
        frame.setVisible(true);
        return frame;

    }
        
    
}
