package com.orbswarm.choreography.timeline;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.swarmcomposer.util.StdDraw;
import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.Swarm;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
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
    OrbControl orbControl = null;
    
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
    private double eventTrackHeight = timelineHeight / 10.;
    private double eventTrackPadding = eventTrackHeight / 10.;

    private ArrayList pendingEvents;
    private ArrayList runningEvents;
    private ArrayList addToRunningEvents;
    private HashMap   runningEventMap;
    private ArrayList completedEvents;
    

    private float timelineDuration = 1.0f;

    public Color bgColor, eventColor_past, eventColor_current, eventColor_future;
    public Color eventColor_border, eventColor_text;
    public Color sequenceColor;
    public Color timeCursorColor;
    public HSV[] leitMotifColors;
    protected Component repaintComponent;

    private Timeline timeline = null;
    private ArrayList eventTracks = null;
    
    public TimelineDisplay(int canvasWidth, int canvasHeight) {
        this.canvasWidth  = canvasWidth;
        this.canvasHeight = canvasHeight;
        initColors();
        initLeitMotifColors();
        setupDrawer();
        calculateDimensions(true);
        createMainPanel(); // TODO: pass in place to find the choreoography files.
    }

    public void setSwarmCon(SwarmCon val) {
        this.swarmCon = val;
        this.orbControl = swarmCon.getOrbControl();
    }
    
    public void setTimeline(String timelinePath) throws IOException {
        Timeline timeline = Timeline.readTimeline(timelinePath);
        setTimeline(timeline);
    }
    
    public void setTimeline(Timeline val) {
        this.timeline = val;
        timelineDuration = this.timeline.getDuration();
        calculateDimensions(true);
        setupTimelineRunner(this.timeline);
        extractEventTracks(this.timeline);
        display(0.f);
        drawer.show(true);
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
        sequenceColor      = Color.getHSBColor(.05f, .35f,   1.0f);
    }
    
    public void initLeitMotifColors() {
        leitMotifColors = new HSV[6];
        for(int i=0; i < leitMotifColors.length; i++) {
            float hue = (float)i / (float)leitMotifColors.length;
            leitMotifColors[i] = new HSV(hue, 1.f, 1.f);
        }
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
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JComboBox drop = new JComboBox();
        drop.setBackground(bgColor);
        // TODO: find the choreography files and populate dropdown with them
        drop.addItem("== Select a timeline ==");
        addTimelines(drop);

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
        gbc.gridwidth = 2;
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
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
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
        addDebugLeitMotifButtons(panel);
        return panel;
    }
    public void addDebugLeitMotifButtons(JPanel panel) {
        int r =0;
        int c = 0;
        for(int i=0; i < 6; i++) {
            JButton lmButton = new JButton("[" + i + "]");
            lmButton.setPreferredSize(new Dimension(55, 12));
            lmButton.setMinimumSize(new Dimension(55, 12));
            GridBagConstraints gbc = new GridBagConstraints();
            
            gbc.gridx = c;
            gbc.gridy = 2 + r;
            c++;
            if (c > 2) {
                c = 0;
                r++;
            }
            gbc.fill = GridBagConstraints.NONE;
            panel.add(lmButton, gbc);
            lmButton.addActionListener(new lmButtonActionListener(i));
        }
    }

    class lmButtonActionListener implements ActionListener {
        private int orbNum;
        public lmButtonActionListener(int orbNum) {
            this.orbNum = orbNum;
        }
        public void actionPerformed(ActionEvent e) {
            doLeitMotif(orbNum);
        }
    }
    
    public void addTimelines(JComboBox cb) {
        List timelines = findTimelines();
        for(Iterator it=timelines.iterator(); it.hasNext(); ) {
            cb.addItem((String)it.next());
        }
    }

    public List findTimelines() {
        String timelinesFile = "resources/timelines/timelines.list";
        ArrayList timelines = new ArrayList();
        try {
            TokenReader reader = new TokenReader(timelinesFile);
            String token = reader.readToken();
            while (token != null) {
                timelines.add(token);
                token = reader.readToken();
            }
        } catch (IOException ex) {
            System.out.println("TimelineDisplay.findTimelines caught exception reading timelines.");
            ex.printStackTrace();
        }
        return timelines;
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
     
    ////////////////////////////////////////////////////////////////////////////
    /// Setup the timeline runner:        
    ///  Keep 3 lists of events (later optimization: sort them by time)
    ///    - pending events haven't been started yet.
    ///    - running events have been kicked off, and are waiting to be
    ///      stopped. 
    ///    - completed event have done their stuff.
    ///    * an event will only be on one list at a time.
    ///    * it's unknown whether an event's Specialist gets created at setup
    ///      time or at start time. (ends up depending on how line it takes
    ///      to create specialists. )
    ///
    ////////////////////////////////////////////////////////////////////////////

    public void setupTimelineRunner(Timeline timeline) {
        pendingEvents = new ArrayList();
        runningEvents = new ArrayList();
        addToRunningEvents = new ArrayList();
        runningEventMap = new HashMap();
        completedEvents = new ArrayList();
        
        for(Iterator it = timeline.getEvents().iterator(); it.hasNext() ; ) {
            Event event = (Event)it.next();
            event.setState(Event.STATE_PENDING);
            pendingEvents.add(event);
        }
    }

    public void stopStoppableEvents(float time) {
        //System.out.println("TD::Stop Stoppables...");
        for(Iterator it = runningEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            float et = event.getEndTime();
            if (et < time) {
                stopEvent(event);
                it.remove();
                completedEvents.add(event);
            }
        }
        if (addToRunningEvents.size() > 0) {
            for(Iterator it = addToRunningEvents.iterator(); it.hasNext();) {
                Event event = (Event)it.next();
                runningEvents.add(event);
            }
            addToRunningEvents = new ArrayList();
        }
    }

    public void stopAllRunningEvents() {
        System.out.println("TD: stop all running events. ");
        for(Iterator it = runningEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            stopEvent(event);
            it.remove();
            completedEvents.add(event);
        }
    }

    public void startPendingEvents(float time) {
        for(Iterator it = pendingEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            float st = event.getStartTime();
            if (st < time) {
                startEvent(event);
                float et = event.getEndTime();
                it.remove();
                if (et != Event.NO_TIME && et != st) {
                    //System.out.println("  adding event " + event.getName() + " to running events. ");
                    runningEvents.add(event);
                } else {
                    completedEvents.add(event);
                }
            }
        }
    }

    public void stopEvent(Event event) {
        System.out.println("Stopping event: " + event);
        event.setState(Event.STATE_COMPLETED);
        String name = event.getName();
        if (name != null) {
            runningEventMap.put(name, null);
        }
        
        Specialist sp = event.getSpecialist();
        if (sp != null) {
            sp.stop();
            event.setSpecialist(null);
        }
        // if this is a composite event, stop its subevents.
        ArrayList subEvents = event.getEvents();
        if (subEvents != null) {
            for(Iterator it = subEvents.iterator(); it.hasNext(); ) {
                Event sub = (Event)it.next();
                stopEvent(sub);
            }
        }

        // if this is a member of a sequence, start the next event in the sequence.
        Event parent = event.getParent();
        if (parent != null && parent instanceof Sequence) {
            Sequence seq = (Sequence)parent;
            Sequence next = seq.getNext();
            System.out.println("   stopped event was in a sequence. trying to start next: " + next);
            if (next != null) {
                startEvent(next);
                addToRunningEvents.add(next);
            }
        }
    }

    public void startEvent(Event event) {
        if (event == null) {
            return;
        }
        if (event instanceof Sequence) {
            System.out.println("Starting Sequence: " + event);
            Event first = ((Sequence)event).getFirst();
            if (first != null) {
                System.out.println("Starting first event of Sequence: " + first);
                startEvent(first);
                // need to put it on the running event list specially. 
                addToRunningEvents.add(first);
            }
            return;
        }
        System.out.println("Starting Event: " + event);
        int type = event.getType();
        if (type == Event.TYPE_PARAMETER || type == Event.TYPE_ACTION) {
            Event targetEvent = findRunningEvent(event.getTarget());
            if (targetEvent != null) {
                targetEvent.performAction(event);
            }
        } else {
            event.startSpecialist(orbControl);
            runningEventMap.put(event.getName(), event);
        }
        // if this is a composite event, start its subevents.
        ArrayList subEvents = event.getEvents();
        if (subEvents != null) {
            for(Iterator it = subEvents.iterator(); it.hasNext(); ) {
                Event sub = (Event)it.next();
                startEvent(sub);
            }
        }
    }

    public Event findRunningEvent(String name) {
        return (Event)runningEventMap.get(name);
    }

    /////////////////////////////////////////
    /// Arrange events into tracks       ////
    /////////////////////////////////////////

    public void extractEventTracks(Timeline timeline) {
        eventTracks = new ArrayList();
        eventTracks.add(new ArrayList()); // first track!
        for(Iterator it = timeline.getEvents().iterator(); it.hasNext() ; ) {
            Event event = (Event)it.next();
            event.setupSpecialist(orbControl);
            if (event instanceof Sequence) {
                ((Sequence)event).calculateDuration();
                ((Sequence)event).adjustEventTimes();
            }
            
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

    // TODO: give orbState messages to running specialists
    public boolean cycle(float time) {
        //System.out.println("TIMELINE cycle[" + time + "]");
        if (time < timeline.getDuration()) {
            stopStoppableEvents(time);
            startPendingEvents(time);
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
        drawer.show(true);
        repaint();
    }

    // TODO: display sequences...
    public void displayEvent(Event event, int track) {
        if (event instanceof Sequence) {
            displaySequence((Sequence)event, track);
            return;
        }
        
        float st = event.getStartTime();
        float et = event.getEndTime();
        if (et == Temporal.NO_TIME) {
            et = st;
        }
        //System.out.println("DisplayEvent. name: " + event.getName() + " start: " + st + " end: " + et);
        
        Color eventColor;
        Color jcolor = event.jcolor; // for a color event, a swatch indicating the actual color
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
        displayEventStartPoint(evStartX, evY, jcolor);
        String text = event.getName();
        if (text == null) {
            text = "<>";
        }
        if (event.getTarget() != null) {
            text += "->" + event.getTarget();
        }
        displayEventText(evStartX + eventTrackHeight * .5, evY - eventTrackHeight * .4, text, jcolor);
    }

    public void displaySequence(Sequence seq, int track) {
        float st = seq.getStartTime();
        float fd = seq.calculateDuration();
        float et = st + fd;
        double seqStartX = timeWindowPixel(timeToTimePixel(st));
        double seqEndX = timeWindowPixel(timeToTimePixel(et));
        double seqY = timeWindowHeight - (track + .5) * (eventTrackHeight + eventTrackPadding);

        drawer.setPenColor(sequenceColor);
        double width = Math.abs(seqEndX - seqStartX);
        double barHeight = eventTrackHeight * 1.1;
        double y = seqY - barHeight;
        drawer.filledRectangle(seqStartX, y, width, barHeight);

        //
        // Now draw the events in the sequence...
        //
        Sequence n = seq;
        while (n != null) {
            Event event = n.getFirst();
            if (event == null) {

            } else {
                displayEvent(event, track);
            }
            n = n.getNext();
        }
    }


    public void displayEventStartPoint(double evStartX, double evY, Color jcolor) {
        double eventTickWidth = eventTrackHeight * .5;
        double x = evStartX;
        double y =  evY - eventTrackHeight;
        drawer.filledRectangle(x, y,
                               eventTickWidth, eventTrackHeight);
        Color keep = drawer.getPenColor();
        drawer.setPenColor(eventColor_border);
        drawer.line(x, y, x, evY);
        if (jcolor != null) {
            drawer.setPenColor(jcolor);
            drawer.filledRectangle(x + eventTickWidth, y,
                               2 * eventTickWidth, eventTrackHeight);
        }
        drawer.setPenColor(keep);
    }

    public void displayEventEndPoint(double evEndX, double evY) {
        double eventTickWidth = eventTrackHeight * .5;
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

    public void displayEventText(double evStartX, double evY, String text, Color jcolor) {
        drawer.setPenColor(eventColor_text);
        double eventTickWidth = eventTrackHeight * .5;
        double offset = (jcolor == null ? 0. : 2 * eventTickWidth);
        drawer.text(evStartX + offset, evY, (text == null ? "<>" : text));
        // TODO: use the width of the text in determining whether events overlap
    }

    public void displayTimeCursor(float time) {
        double timePixel = timeToTimePixel(time);
        double timeWindowPixel = timeWindowPixel(timePixel) - timeCursorWidth * .5;
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

    public void doLeitMotif(int orbNum) {
        doLeitMotifInThread(orbNum);
    }
    
    public void doLeitMotifInThread(int orbNum) {
        final int _orbNum = orbNum;
        final Orb orb = (Orb)swarmCon.getSwarm().getOrb(_orbNum);
        final OrbControl _orbControl = orbControl;
        final HSV leitMotifColor = leitMotifColors[_orbNum];
        final Color prevColor = orb.getOrbColor();
        final HSV prevHSV = HSV.fromColor(prevColor);
        new Thread() {
            public void run()  {
                System.out.println("TimelineDisplay:doLeitMotif(" + _orbNum + ")");
                int toColor = 500;
                int toWhite = 200;
                int fromWhite = 200;
                int fromColor = 500;
                int lh = (int)(leitMotifColor.getHue() * 255);
                int ls = (int)(leitMotifColor.getSat() * 255);
                int lv = (int)(leitMotifColor.getVal() * 255);
                _orbControl.orbColor(_orbNum, lh, ls, lv, toColor);
                try { Thread.sleep(toColor); } catch (Exception ex) {}
                _orbControl.orbColor(_orbNum, 0, 0, 255, toWhite);
                try { Thread.sleep(toWhite); } catch (Exception ex) {}
                _orbControl.orbColor(_orbNum, lh, ls, lv, fromWhite);
                try { Thread.sleep(fromWhite); } catch (Exception ex) {}
                _orbControl.orbColor(_orbNum,
                                     (int)(255*prevHSV.getHue()),
                                     (int)(255*prevHSV.getSat()),
                                     (int)(255*prevHSV.getVal()),
                                     fromColor);
            }
        }.start();
    }

    //////////////////////////////////////////////////
    /// Joystick control                           ///
    //////////////////////////////////////////////////
    public static final int LEITMOTIF_BUTTON = 0;

    public void joystickButton(int orbNum, int buttonNumber) {
        if (buttonNumber == LEITMOTIF_BUTTON) {
            doLeitMotif(orbNum);
        }
    }
    
}
