package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 *  Class which handles the timeline.
 *  
 */
public class Timeline extends Temporal {

    protected float arena = NO_SIZE;
    protected static HashMap specialistRegistry;

    public void setArena(float val) {
        this.arena = val;
    }
    
    public float getArena() {
        return this.arena;
    }
    

    public static Timeline readTimeline(String timelinePath) throws IOException {
        TokenReader reader = new TokenReader(timelinePath);
        return readTimeline(reader);
    }
    
    public static Timeline readTimeline(TokenReader reader)  throws IOException {
        Timeline timeline = new Timeline();
        String token = reader.readUntilToken(TIMELINE);
        while (token != null && !token.equalsIgnoreCase(END_TIMELINE)) {
            //System.out.println("ReadTimeline: " + token);
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
            }
            token = reader.readToken(); 
        }
        return timeline;
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
        //System.out.println("Finished ReadEvent: " + token);

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
    // We need a facility for triggering events or sequences when various conditions happen.
    // The first use of this will be to create identificatory leitmotifs for the orbs so that
    // the people driving them with joysticks will be able to find out which ones are theirs.
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
        // problem: in this scheme we can't make the color go back to where it was before we triggered the leitmotif.
        seq.appendEvent(toColor);
        return seq;
    }
}

