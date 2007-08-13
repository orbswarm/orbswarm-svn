package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *  Class which handles the timeline.
 *  
 */
public class Timeline extends Temporal {

    protected float arena = NO_SIZE;

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
                String name = reader.readToken();
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
                String name = reader.readToken();
                event.setName(name);
            } else if (token.equalsIgnoreCase(SPECIALIST)) {
                String specialistName = reader.readToken();
                event.setSpecialist(specialistName); 
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
            } else if (token.equalsIgnoreCase(ORBS)) {
                List orbs = reader.gatherTokensUntilToken(END_ORBS, false);
                event.setOrbsFromStrings(orbs);
                
            } else if (token.equalsIgnoreCase(COLOR)) {
                List colorSpec = reader.gatherTokensUntilToken(END_COLOR, false);
                HSV color = colorFromSpec(colorSpec);
                event.setColor(color);
            } 
            token = reader.readToken();
        }
        //System.out.println("Finished ReadEvent: " + token);

        return event;
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

}

