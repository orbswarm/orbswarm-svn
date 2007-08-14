package com.orbswarm.choreography.timeline;

import com.orbswarm.swarmcomposer.color.HSV;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Temporal {

    public static final String TIMELINE     = "<timeline>";
    public static final String END_TIMELINE = "</timeline>";
    public static final String EVENT        = "<event>";
    public static final String END_EVENT    = "</event>";
    public static final String SUBEVENT     = "<subevent>";
    public static final String END_SUBEVENT = "</subevent>";
    public static final String SEQUENCE     = "<sequence>";
    public static final String END_SEQUENCE = "</sequence>";
    public static final String PROPERTIES   = "<properties>";
    public static final String END_PROPERTIES = "</properties>";

    public static final String END          = "end";
    public static final String DURATION     = "duration:";
    public static final String ARENA        = "arena:";
    public static final String NAME         = "name:";
    public static final String TARGET       = "target:";
    public static final String SPECIALIST   = "specialist:";
    public static final String NOTES        = "<notes>";
    public static final String END_NOTES    = "</notes>";
    public static final String ORBS         = "<orbs>";
    public static final String END_ORBS     = "</orbs>";
    public static final String STARTTIME    = "startTime:";
    public static final String ENDTIME      = "endTime:";
    public static final String LENGTH       = "length";
    public static final String COLOR        = "<color>";
    public static final String END_COLOR    = "</color>";

    public static final float NO_TIME = -1.f;
    public static final float NO_SIZE = -1.f;

    protected ArrayList events    = null;
    protected String    name      = null;
    protected String    notes     = null;
    protected float     duration  = NO_TIME;
    protected float     startTime = NO_TIME;
    protected float     endTime   = NO_TIME;
    protected Properties properties = null;

    public void setName(String val) {
        this.name = val;
    }
    public String getName() {
        return this.name;
    }

    public void setProperties(Properties val) {
        this.properties = val;
    }
    public Properties getProperties() {
        return this.properties;
    }
    
    public void setNotes(String val) {
        this.notes = val;
    }
    public String getNotes() {
        return this.notes;
    }

    public void setDuration(float val) {
        this.duration = val;
    }
    public float getDuration() {
        return this.duration;
    }
    

    public void setStartTime(float val) {
        this.startTime = val;
    }
    public float getStartTime() {
        return this.startTime;
    }
    
    public void setEndTime(float val)     {
        this.endTime = val;
    }
    public float getEndTime()     {
        return this.endTime;
    }

    public static  boolean intervalIntersects(double st1, double end1,
                                              double st2, double end2) {
        return  ((st1 <= st2 && st2 <= end1) ||
                 (st1 <= end2 && end2 <= end1) ||
                 (st2 < st1 && end2 > end1));
    }
 
    //  events happen at the same time as the main event.
    // This allows events on more than one orb, with different
    // parameters.
    // 
    public void addEvent(Event event ) {
        if (events == null) {
            events = new ArrayList();
        }
        events.add(event);
    }

    public ArrayList getEvents() {
        return events;
    }


    public static void writeTag(StringBuffer buf, String indent, String tagName, boolean nl) {
        buf.append(indent);
        buf.append(tagName);
        if (nl) {
            buf.append("\n");
        }
    }
        
        
    public static void writeAttribute(StringBuffer buf, String indent, String attName, String value) {
        if (value == null) {
            return;
        }
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writeAttribute(StringBuffer buf, String indent, String attName, int value, int noValue) {
        if (value == noValue) {
            return;
        }
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writeAttribute(StringBuffer buf, String indent, String attName, float value, float noValue) {
        if (value == noValue) {
            return;
        }
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writePairedAttribute(StringBuffer buf, String indent,
                                            String startTag, String endTag,
                                            String value) {
        if (value == null) {
            return;
        }
        buf.append(indent);
        buf.append(startTag);
        buf.append(' ');
        buf.append(value);
        buf.append(' ');
        buf.append(endTag);
        buf.append('\n');
    }

    public static HSV colorFromSpec(List colorSpec) {
        HSV color = null;
        try {
            Iterator it = colorSpec.iterator();
            String type = (String)it.next();
            if (type.equalsIgnoreCase("HSV")) {
                float h = Float.parseFloat((String)it.next());
                float s = Float.parseFloat((String)it.next());
                float v = Float.parseFloat((String)it.next());
                color = new HSV(h, s, v);
            } else if (type.equalsIgnoreCase("RGB")) {
                int r = Integer.parseInt((String)it.next());
                int g = Integer.parseInt((String)it.next());
                int b = Integer.parseInt((String)it.next());
                color = HSV.fromRGB(r, g, b);
            }
        } catch (Exception ex) {
            System.err.println("Temporal.colorFromSpec caught exception: " + ex);
            ex.printStackTrace();
        }
       return color;

    }
}