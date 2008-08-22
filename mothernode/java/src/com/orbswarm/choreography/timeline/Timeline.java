package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;
import com.orbswarm.swarmcon.Swarm;
import com.orbswarm.swarmcon.Target;
import com.orbswarm.choreography.Orb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.trebor.util.JarTools;

// for debugging:
import com.orbswarm.swarmcomposer.composer.BotVisualizer;



/**
 *  Class which handles the timeline.
 *  
 */
public class Timeline extends Temporal {

    protected float arena = NO_SIZE;
    protected static HashMap specialistRegistry;
    public    TimelineDisplay timelineDisplay = null;
    protected ArrayList proximityTriggers = new ArrayList();
    protected HashMap   paths = new HashMap();
    protected ArrayList ephemeralPaths = new ArrayList();
    protected ArrayList regions = new ArrayList();
    protected boolean   looping = true;
    

    public void setTimelineDisplay(TimelineDisplay val) {
        this.timelineDisplay = val;
    }

    public void setArena(float val) {
        this.arena = val;
    }
    
    public float getArena() {
        return this.arena;
    }

    public boolean getLooping() {
        return this.looping;
    }

    public static Timeline readTimeline(String timelinePath) throws IOException {
        System.out.println("ReadTimeline. " + timelinePath);
        TokenReader reader = new TokenReader(
           JarTools.getResourceAsStream(timelinePath));
        return readTimeline(reader);
    }
    
    public static Timeline readTimeline(TokenReader reader)  throws IOException {
        Timeline timeline = new Timeline();
        String token = reader.readUntilToken(TIMELINE);
        while (token != null && !token.equalsIgnoreCase(END_TIMELINE)) {
            System.out.println("ReadTimeline: " + token);
            if (token.equalsIgnoreCase(NAME)) {
                String name = reader.readLine();
                timeline.setName(name);
            } else if (token.equalsIgnoreCase(DURATION)) {
                float duration = reader.readFloat();
                timeline.setDuration(duration); 
            } else if (token.equalsIgnoreCase(ARENA)) {
                float arena = reader.readFloat();
                timeline.setArena(arena); 
            } else if (token.equalsIgnoreCase(NOTES)) {
                String notes = reader.gatherUntilToken(END_NOTES, false);
                timeline.setNotes(notes);
            } else if (token.equalsIgnoreCase(STARTTIME)) {
                float startTime = reader.readFloat();
                timeline.setStartTime(startTime);
            } else if (token.equalsIgnoreCase(ENDTIME)) {
                float time = reader.readFloat();
                timeline.setEndTime(time);
            } else if (token.equalsIgnoreCase(EVENT)) {
                Event event = readEvent(reader, timeline, null, END_EVENT);
                timeline.addEvent(event);
            } else if (token.equalsIgnoreCase(SEQUENCE)) {
                Sequence seq = readSequence(reader, timeline, null, END_SEQUENCE);
                timeline.addEvent(seq);
            } else if (token.equalsIgnoreCase(REGION)) {
                Region region = readRegion(reader, timeline, END_REGION);
                timeline.addRegion(region);
            } else if (token.equalsIgnoreCase(PATH)) {
                TimelinePath path = readPath(reader, END_PATH);
                timeline.addPath(path);
            } else if (token.equalsIgnoreCase(PROXIMITY)) {
                readProximityEvent(reader, timeline, END_PROXIMITY);
            }
            token = reader.readToken(); 
        }
        timeline.postProcess();
        return timeline;
    }

    private void postProcess() {
        annealRegions();
        annealPaths();
    }

    

    // assumption: have already read the start event token.
    // Note: works for events and sub events.
    //
    public static Event readEvent(TokenReader reader,
                                  Timeline timeline, Event parent,
                                  String endToken)  throws IOException {
        Event event = new Event(timeline, parent);
        String token = reader.readToken();
        while (token != null && !token.equalsIgnoreCase(endToken)) {
            //System.out.println("ReadEvent: " + token);
            if (token.equalsIgnoreCase(NAME)) {
                String name = reader.readLine();
                event.setName(name);
            } else if (token.equalsIgnoreCase(TARGET)) {
                String target = reader.readToken();
                event.setTarget(target); 

            } else if (token.equalsIgnoreCase(TRIGGER)) {
                String location = reader.readToken();
                event.setTrigger(true);
                event.setTriggerLocation(location.toLowerCase());
                
            } else if (token.equalsIgnoreCase(TRIGGER_ACTION)) {
                String action = reader.readToken();
                System.out.println("Trigger.action: " + action);
                if (action.equalsIgnoreCase(TRIGGER_ADDITIVE)) {
                    event.setTriggerAction(TRIGGER_ACTION_ADD);
                } else if (action.equalsIgnoreCase(TRIGGER_CLEAR)) {
                    event.setTriggerAction(TRIGGER_ACTION_CLEAR);
                } else if (action.equalsIgnoreCase(TRIGGER_REPLACE)) {
                    event.setTriggerAction(TRIGGER_ACTION_REPLACE);
                }

            } else if (token.equalsIgnoreCase(SPECIALIST)) {
                String specialistName = reader.readToken();
                event.setSpecialistName(specialistName); 
            } else if (token.equalsIgnoreCase(DURATION)) {
                float duration = reader.readFloat();
                event.setDuration(duration); 
            } else if (token.equalsIgnoreCase(NOTES)) {
                String notes = reader.gatherUntilToken(END_NOTES, false);
                event.setNotes(notes);
            } else if (token.equalsIgnoreCase(STARTTIME)) {
                float startTime = reader.readFloat();
                event.setStartTime(startTime);
            } else if (token.equalsIgnoreCase(ENDTIME)) {
                String endtoken = reader.readToken();
                if (endtoken.equalsIgnoreCase(END)) {
                    event.setEndTime(timeline.getDuration());
                } else {
                    float time = Float.parseFloat(endtoken);
                    event.setEndTime(time);
                }
            } else if (token.equalsIgnoreCase(SUBEVENT)) {
                Event subEvent = readEvent(reader, timeline, event, END_SUBEVENT);
                event.addEvent(subEvent);

            } else if (token.equalsIgnoreCase(SEQUENCE)) {
                Sequence seq = readSequence(reader, timeline, event, END_SEQUENCE);
                event.addEvent(seq);

            } else if (token.equalsIgnoreCase(ORBS)) {
                List orbs = reader.gatherTokensUntilToken(END_ORBS, false);
                event.setOrbsFromStrings(orbs);
                
            } else if (token.equalsIgnoreCase(COLOR_TAG)) {
                List colorSpec = reader.gatherTokensUntilToken(END_COLOR, false);
                HSV color = colorFromSpec(colorSpec);
                event.setColor(color);

            } else if (token.equalsIgnoreCase(COLOR)) {
                String colorSpec = reader.readLine();
                HSV color = colorFromSpec(colorSpec);
                event.setColor(color);

            } else if (token.equalsIgnoreCase(FADE_TIME)) {
                float  fadeTime = reader.readFloat();
                event.setFadeTime(fadeTime);

            } else if (token.equalsIgnoreCase(PROPERTIES)) {
                Properties properties = readProperties(reader);
                event.setProperties(properties);

            } 
            token = reader.readToken();
        }
        System.out.println("Finished ReadEvent: " + token + " testing if ALL_ORBS needs to be set");
        if (event.getOrbs() == null) {
            System.out.println("Event " + event.getName() + ": setting ALL_ORBS");
            event.setAllOrbs();
        }

        return event;
    }

    public static Sequence readSequence(TokenReader reader,
                                        Timeline timeline, Event parent,
                                        String endToken)  throws IOException {
        Sequence seq = new Sequence(timeline, parent);
        String token = reader.readToken();
        while (token != null && !token.equalsIgnoreCase(endToken)) {
            //System.out.println("ReadSequence: " + token);
            if (token.equalsIgnoreCase(NAME)) {
                String name = reader.readLine();
                seq.setName(name);
            } else if (token.equalsIgnoreCase(NOTES)) {
                String notes = reader.gatherUntilToken(END_NOTES, false);
                seq.setNotes(notes);
            } else if (token.equalsIgnoreCase(STARTTIME)) {
                float startTime = reader.readFloat();
                seq.setStartTime(startTime);

            } else if (token.equalsIgnoreCase(TRIGGER)) {
                String location = reader.readToken();
                seq.setTrigger(true);
                seq.setTriggerLocation(location.toLowerCase());
                String maybeAction = reader.readToken();
                if (maybeAction.equalsIgnoreCase(TRIGGER_ADDITIVE)) {
                    seq.setTriggerAction(TRIGGER_ACTION_ADD);
                } else if (maybeAction.equalsIgnoreCase(TRIGGER_CLEAR)) {
                    seq.setTriggerAction(TRIGGER_ACTION_CLEAR);
                } else {
                    reader.pushToken();
                }

            } else if (token.equalsIgnoreCase(EVENT)) {
                Event event = readEvent(reader, timeline, seq, END_EVENT);
                seq.appendEvent(event);

            } else if (token.equalsIgnoreCase(SEQUENCE)) {
                Sequence subseq = readSequence(reader, timeline, seq, END_SEQUENCE);
                seq.addEvent(subseq);

            } else if (token.equalsIgnoreCase(PROPERTIES)) {
                Properties properties = readProperties(reader);
                seq.setProperties(properties);

            } 
            token = reader.readToken();
        }
        //System.out.println("Finished ReadSequence: " + token);

        return seq;
    }

    private void annealRegions() {
        int numRegions = regions.size();
        int i=0;
        for(Iterator it = regions.iterator(); it.hasNext(); ) {
            Region region = (Region)it.next();
            region.setBaseHue(i, numRegions);
            i++;
        }
    }

    private void annealPaths() {
        int numPaths = paths.size();
        int i=0;
        for(Iterator it = getPathsIterator(); it.hasNext(); ) {
            TimelinePath timelinePath = (TimelinePath)it.next();
            timelinePath.setBaseHue(i, numPaths);
            i++;
        }
    }
    
    public static Region readRegion(TokenReader reader,
                                    Timeline timeline,
                                    String endToken)  throws IOException {
        int numOrbs = 6;  // (where do we really get this??)
        Region region = new Region(numOrbs, timeline);
        String token = reader.readToken();
        double x1 = 0., y1 = 0., x2 = 0., y2 = 0.;
        System.out.println("Reading REgion. ");
        while (token != null && !token.equalsIgnoreCase(endToken)) {
            if (token.equalsIgnoreCase(NAME)) {
                String name = reader.readLine();
                System.out.println(" region name: " + name);
                region.setName(name);

            } else if (token.equalsIgnoreCase(X)) {
                x1 = reader.readDouble();
                x2 = reader.readDouble();
                System.out.println(" region X coords: " + x1 + " " + x2);

            } else if (token.equalsIgnoreCase(Y)) {
                y1 = reader.readDouble();
                y2 = reader.readDouble();
                System.out.println(" region Y coords: " + y1 + " " + y2);

            // Events need to be children of a region,
            // with a trigger type: ENTER, EXIT, INSIDE
            } else if (token.equalsIgnoreCase(EVENT)) {
                System.out.println("  region:  reading event...");

                Event event = readEvent(reader, timeline, null, END_EVENT);
                region.addEvent(event);

            } else if (token.equalsIgnoreCase(SEQUENCE)) {
                Sequence subseq = readSequence(reader, timeline, null, END_SEQUENCE);
                region.addEvent(subseq);

                /* do we need these for regions?
            } else if (token.equalsIgnoreCase(PROPERTIES)) {
                Properties properties = readProperties(reader);
                region.setProperties(properties);
                */
            } 
            token = reader.readToken();
        }
        region.setBounds(x1, y1, x2, y2);

        System.out.println("Finished ReadRegion: " + token);

        return region;
    }

    public static TimelinePath readPath(TokenReader reader,
                                String endToken)  throws IOException {
        TimelinePath path = new TimelinePath();
        String token = reader.readToken();
        double x = 0., y = 0.;
        System.out.println("Reading Path. ");
        boolean absolute = true;
        while (token != null && !token.equalsIgnoreCase(endToken)) {
            if (token.equalsIgnoreCase(NAME)) {
                String name = reader.readLine();
                System.out.println(" path name: " + name);
                path.setName(name);

            } else if (token.equalsIgnoreCase(POINT)) {
                x = reader.readDouble();
                y = reader.readDouble();
                System.out.println(" path target coords: " + x + " " + y);
                path.add(new Target(x, y));

            } else if (token.equalsIgnoreCase(RELATIVE)) {
                absolute = false;
            } 
            token = reader.readToken();
        }
        path.setAbsolute(absolute);
        if (absolute) {
            path.reshape();
        } else {
            // TODO: didn't get relative paths correct at all!
            //Target zero = new Target(0., 0.);
            //path.reshapeRelative(zero);
        }

        System.out.println("Finished ReadPath: " + token);

        return path;
    }

    public static void readProximityEvent(TokenReader reader,
                                          Timeline timeline,
                                          String endToken)  throws IOException {
        String token = reader.readToken();
        System.out.println("Reading Proximity. ");
        List locusOrbs = new ArrayList();
        List encroachingOrbs = new ArrayList();
        double triggerDistance = 1.;
        double resetDistance = 2.;
        Event triggeringEvent = null;
        
        while (token != null && !token.equalsIgnoreCase(endToken)) {
            if (token.equalsIgnoreCase(LOCUS_ORBS)) {
                locusOrbs = reader.gatherTokensUntilToken(END_LOCUS_ORBS, false);
            } else if (token.equalsIgnoreCase(ENCROACHING_ORBS)) {
                encroachingOrbs = reader.gatherTokensUntilToken(END_ENCROACHING_ORBS, false);
            } else if (token.equalsIgnoreCase(TRIGGER_DISTANCE)) {
                triggerDistance = reader.readDouble();
            } else if (token.equalsIgnoreCase(RESET_DISTANCE)) {
                resetDistance = reader.readDouble();
            } else if (token.equalsIgnoreCase(EVENT)) {
                Event event = readEvent(reader, timeline, null, END_EVENT);
                triggeringEvent = event;
            } else if (token.equalsIgnoreCase(SEQUENCE)) {
                Sequence subseq = readSequence(reader, timeline, null, END_SEQUENCE);
                triggeringEvent = subseq;
            }
            token = reader.readToken();
        }

        Event parentEvent = null; // can a proximity event even have a parent?
        // Note: this is a cross product of the locusOrbs and the encroaching orbs:
        //       fill out the entire mXn, one ProximityEvent per combination
        if (encroachingOrbs.size() == 0) {
            // TODO: get correct number of orbs
            for(int i=0; i < 6; i++) {
                encroachingOrbs.add("" + i);
            }
        }
        
        if (triggeringEvent != null) {
            for(Iterator it = locusOrbs.iterator(); it.hasNext(); ) {
                String orbStr = (String)it.next();
                int locusOrb = Integer.parseInt(orbStr);
                for(Iterator enc = encroachingOrbs.iterator(); enc.hasNext(); ) {
                    String encStr = (String)enc.next();
                    int encroachingOrb = Integer.parseInt(encStr);
                    if (locusOrb != encroachingOrb) {
                        ProximityEvent prox = new ProximityEvent(timeline,
                                                                 parentEvent,
                                                                 locusOrb,
                                                                 encroachingOrb,
                                                                 triggeringEvent);
                        timeline.addProximityTrigger(prox);
                    }
                }
            }
        }
        System.out.println("Finished ReadProximity: " + locusOrbs.size() + " X " + encroachingOrbs.size() + " = " + (locusOrbs.size() * encroachingOrbs.size()) );
    }

    public void addProximityTrigger(ProximityEvent prox) {
        proximityTriggers.add(prox);
    }

    public static Properties readProperties(TokenReader reader) throws IOException {
        Properties properties = new Properties();
        String token = reader.readToken();
        while(token != null && !token.equalsIgnoreCase(END_PROPERTIES)) {
            String value = reader.readLine();
            properties.setProperty(token, value);
            token = reader.readToken();
        }
        return properties;
    }
    
    public String write(String indent) {
        StringBuffer buf = new StringBuffer();
        write(buf, indent);
        return buf.toString();
    }

    public void write(StringBuffer buf, String indent) {
        String origIndent = indent;
        buf.append(origIndent);
        buf.append(TIMELINE);
        buf.append('\n');
        indent += "    ";
        writeAttribute(buf, indent, NAME,       name);
        writePairedAttribute(buf, indent, NOTES, END_NOTES, notes);
        writeAttribute(buf, indent, DURATION,  duration, NO_TIME);
        writeAttribute(buf, indent, ARENA,     duration, 0.f);
        writeAttribute(buf, indent, STARTTIME, startTime, NO_TIME);
        writeAttribute(buf, indent, ENDTIME,   endTime, NO_TIME);

        if (events != null) {
            for(Iterator it = events.iterator(); it.hasNext(); ) {
                Event event = (Event)it.next();
                event.write(buf, indent);
            }
        }
        buf.append(origIndent);
        buf.append(END_TIMELINE);
        buf.append('\n');
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Timeline <timeline file>");
        }
        String timelinePath = args[0];
        System.out.println("Reading timeline....");
        try {
            Timeline timeline = Timeline.readTimeline(timelinePath);
            System.out.println("Read timeline.");
            System.out.println(timeline.write(""));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static {
        specialistRegistry = new HashMap();
    }

    public static void registerSpecialist(String specialistName, String specialistClassName) {
        try {
            Class specialistClass = Class.forName(specialistClassName);
            specialistRegistry.put(specialistName.toLowerCase(), specialistClass);
        } catch (Exception ex) {
            System.err.println("Timeline caught exception registering specialist " + specialistName  + " as " + specialistClassName);
            ex.printStackTrace();
        }
    }

    public static Class getSpecialistClass(String specialistName) {
        return (Class)specialistRegistry.get(specialistName.toLowerCase());
    }

    //
    // Regions
    //
    public void addRegion(Region region) {
        regions.add(region);
    }

    public ArrayList getRegions() {
        return regions;
    }

    //
    // Paths
    //
    public void addPath(TimelinePath path) {
        paths.put(path.getName(), path);
    }

    public void addEphemeralPath(TimelinePath path) {
        ephemeralPaths.add(path);
    }

    public void removeEphemeralPath(TimelinePath path) {
        int idx = ephemeralPaths.indexOf(path);
        if (idx >= 0) {
            ephemeralPaths.remove(idx);
        }
    }

    public TimelinePath getPath(String name) {
        return (TimelinePath)paths.get(name);
    }
    
    public Iterator getPathsIterator() {
        return paths.values().iterator();
    }

    public Iterator getEphemeralPathsIterator() {
        return ephemeralPaths.iterator();
    }

    public ArrayList getEphemeralPaths() {
        return ephemeralPaths;
    }

    /**
     * @return a list of regions that contain the point (x, y)
     *
     */
    public ArrayList intersectingRegions(double x, double y) {
        ArrayList hits = new ArrayList();
        for(Iterator it = regions.iterator(); it.hasNext(); ) {
            Region region = (Region)it.next();
            if (region.intersects(x, y)) {
                hits.add(region);
            }
        }
        return hits;
    }
    
    //
    // Leitmotifs
    // We need a facility for triggering events or
    // sequences when various conditions happen.  The first use of
    // this will be to create identificatory leitmotifs for the orbs
    // so that the people driving them with joysticks will be able to
    // find out which ones are theirs.
    //
    // Ideally what would be triggered would be a sequence of events that could be specified
    // in the timeline. 
    //
    private Event[] leitMotifs = new Event[6];
    public Event getLeitMotif(int orbNum) {
        if (leitMotifs[orbNum] == null) {
            leitMotifs[orbNum] = createLeitMotif(orbNum);
        }
        return leitMotifs[orbNum];
    }

    public Event createLeitMotif(int orbNum) {
        Sequence seq = new Sequence(this, null);
        Event toColor = new Event(this, seq);
        int[] orbs = new int[1];
        orbs[0] = orbNum;
        toColor.setOrbs(orbs);
        toColor.setSpecialistName("SimpleColor");
        //toColor.setColor(leitMotifColor[orbNum]); // not yet?
        toColor.setFadeTime(.5f);
        // problem: in this scheme we can't make the color go back
        // to where it was before we triggered the leitmotif.
        seq.appendEvent(toColor);
        return seq;
    }

    public void orbState(Swarm swarm) {
        // if the timeline has any proximity-triggered events,
        // then we calculate the distances matrix and see if any got triggered
        if (proximityTriggers.size() > 0) {
            double [][]distanceMatrix =
                assembleDistanceMatrix(swarm);
            /* dbg
            StringBuffer buf = new StringBuffer(128);
            BotVisualizer.printDistances(buf, distanceMatrix);
            System.out.println(buf.toString());
            */
            for(Iterator it = proximityTriggers.iterator(); it.hasNext(); ) {
                ProximityEvent pe = (ProximityEvent)it.next();
                // idea: one proximity trigger event per (orb, orb) combination
                //       with the cross product done at readtime rather than
                //       each cycle. That will also make it easier to determine
                //       which ones are running for start/stop potential.
                //       This also means each proximityEvent will have at most
                //       one triggeredEvent at a time, so we can store the
                //       triggered copy of the event on the pe itself.

                // A proximityEvent is triggered when it gets in range,
                // and untriggered when it gets out of range (beyond the reset distance).
                //  (there can be an optional reset range that will only
                //   allow an orb to re-trigger if it goes outside the reset range)
                // This is orthogonal to whether the event is still playing.
                // Even if the event stops early, it won't get re-triggered
                // unless it leaves the locusOrb's personal space and
                // re-enters.
                int locusOrb = pe.getLocusOrb();
                int encroachingOrb = pe.getEncroachingOrb();
                //System.out.print("loc: " + locusOrb + " enc: " + encroachingOrb);
                double distance = distanceMatrix[locusOrb][encroachingOrb];
                //System.out.println("  => dist: " + distance);
                if (pe.isTriggered()) {
                    if (distance > pe.getResetDistance()) {
                        System.out.println("UNTRIGGER. " + pe.getLocusOrb() + " <=> " + pe.getEncroachingOrb());
                        pe.setTriggered(false);
                        Event triggeredEvent = pe.getTriggeredEvent();
                        if (triggeredEvent != null) {
                            timelineDisplay.stopEvent(triggeredEvent);
                            pe.setTriggeredEvent(null);
                        }
                    }
                } else {
                    if (distance <= pe.getPersonalSpaceRadius()) {
                        System.out.println("Triggering prox event: " + pe.getName() + " locus: " + locusOrb + " enc: " + encroachingOrb);
                        pe.setTriggered(true);
                        Event triggeredEvent =
                            timelineDisplay.startTriggeredEvent(locusOrb, pe);
                        pe.setTriggeredEvent(triggeredEvent);
                    }
                }
            }
        }
    }

    public static double[][] assembleDistanceMatrix(Swarm orbSwarm) {
        int n = 6; // TODO: find a better way to get this number
        double[][] distances = new double[n][n];
        for(int i=0; i < n; i++) {
            Orb orb = orbSwarm.getOrb(i);
            // note:: orb distances are in meters.
            double orbDistances[] = orb.getDistances();
            for(int j=0; j < n; j++) {
                distances[i][j] = orbDistances[j];  
            }
        }
        return distances;
    }
}

