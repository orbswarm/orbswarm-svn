package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *  Representation of a single timeline event,
 *  which can have duration.
 */
public class Event extends Temporal {
    
    protected Timeline timeline;
    protected Event parent;
    
    protected String specialist = null;
    protected int[]  orbs       = null;
    protected HSV    color      = null;
    protected String sound      = null;
    protected String song       = null;
    protected float  playTime   = NO_TIME; // maybe use duration?
    protected String command    = null;

        
    public Event(Timeline timeline, Event parent) {
        this.timeline = timeline;
        this.parent = parent;
    }

    public void setOrbs(int[] orbs) {
        this.orbs = orbs;
    }
    public int[] getOrbs() {
        return this.orbs;
    }

    public void setOrbsFromStrings(List orbstrings) {
        int[] orbs = new int[orbstrings.size()];
        int i=0;
        for(Iterator it = orbstrings.iterator(); it.hasNext();) {
            String intstr = (String)it.next();
            int n = -1;
            try {
                n = Integer.parseInt(intstr);
                orbs[i] = n;
                i++;
            } catch (NumberFormatException ex) {
                System.err.println("Event caught exception reading orbs from strings: " + intstr);
            }
        }
        this.orbs = orbs;
    }

    public String getOrbsAsString() {
        return getOrbsAsString(this.orbs, " ");
    }
    public static String getOrbsAsString(int[] orbs, String sep) {
        if (orbs == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        boolean first = true;
        for(int i=0; i < orbs.length; i++) {
            if (!first) {
                buf.append(sep);
            }
            first = false;
            buf.append("" + orbs[i]);
        }
        return buf.toString();
    }
            

    public void setSpecialist(String val) {
        this.specialist = val;
    }

    public String getSpecialist() {
        return this.specialist;
    }
    

    public void setCommand(String val) {
        this.command = val;
    }

    public String getCommand() {
        return this.command;
    }
    
    public void setColor( HSV val) {
        this.color = val;
    }
    public HSV getColor() {
        return this.color;
    }

    // TODO: remember whether it was specified as HSV or RGB
    public String getColorAsString() {
        return getColorAsString(this.color);
    }
    
    public static String getColorAsString(HSV color) {
        if (color == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        buf.append("HSV ");
        buf.append(color.getHue());
        buf.append(" ");
        buf.append(color.getSat());
        buf.append(" ");
        buf.append(color.getVal());

        return buf.toString();
    }
        
    
    public void setSound( String val) {
        this.sound = val;
    }
    public String getSound() {
        return this.sound;
    }
    
    public void setSong( String val) {
        this.song = val;
    }
    public String getSong() {
        return this.song;
    }
    
    public void setPlayTime( float val) {
        this.playTime = val;
    }
    public float getPlayTime() {
        return this.playTime;
    }

    
    public boolean intersects(Event other) {
        float fudgeFactor = .5f;
        if (endTime == NO_TIME) {
            if (other.endTime == NO_TIME) {
                return (Math.abs(startTime - other.startTime) < fudgeFactor);
            }
            return (startTime >= other.startTime && startTime <= other.endTime) ;
        } else {
            return ((other.startTime >= startTime && other.startTime <= endTime) ||
                    (other.endTime   >= startTime && other.endTime   <= endTime) ||
                    (other.startTime <  startTime && other.endTime >  endTime));
        }
    }
    
    public String write(String indent) {
        StringBuffer buf = new StringBuffer();
        write(buf, indent);
        return buf.toString();
    }

    public void write(StringBuffer buf, String indent) {
        String origIndent = indent;
        buf.append(origIndent);
        String startTag;
        String endTag;
        if (parent == null) {
            startTag = EVENT;
            endTag = END_EVENT;
        } else {
            startTag = SUBEVENT;
            endTag = END_SUBEVENT;
        }
        buf.append(startTag);
        buf.append('\n');
        indent += "    ";
        writeAttribute(buf, indent, NAME,       name);
        writeAttribute(buf, indent, SPECIALIST, specialist);
        writePairedAttribute(buf, indent, NOTES, END_NOTES, notes);
        writeAttribute(buf, indent, DURATION,   duration, NO_TIME);
        writeAttribute(buf, indent, STARTTIME,  startTime, NO_TIME);
        writeAttribute(buf, indent, ENDTIME,    endTime, NO_TIME);
        writePairedAttribute(buf, indent, ORBS, END_ORBS, getOrbsAsString());
        writePairedAttribute(buf, indent, COLOR, END_COLOR, getColorAsString());

        if (events != null) {
            for(Iterator it = events.iterator(); it.hasNext(); ) {
                Event sub = (Event)it.next();
                sub.write(buf, indent);
            }
        }
        buf.append(origIndent);
        buf.append(endTag);
        buf.append('\n');
    }
        
}