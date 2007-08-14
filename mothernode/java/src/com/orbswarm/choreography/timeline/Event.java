package com.orbswarm.choreography.timeline;

import com.orbswarm.choreography.ColorSpecialist;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.swarmcomposer.util.TokenReader;
import com.orbswarm.swarmcomposer.color.HSV;

import java.util.ArrayList;
import java.util.Enumeration;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *  Representation of a single timeline event,
 *  which can have duration.
 */
public class Event extends Temporal {
    public static final int STATE_NONE      = 0;
    public static final int STATE_PENDING   = 1;
    public static final int STATE_RUNNING   = 2;
    public static final int STATE_COMPLETED = 3;
    public static final int STATE_ERROR     = 9;

    public static final int TYPE_UNKNOWN   = 0;
    public static final int TYPE_SINGLE    = 1;
    public static final int TYPE_DURATION  = 2;
    public static final int TYPE_SEQUENCE  = 3;
    public static final int TYPE_PARAMETER = 4;
    public static final int TYPE_ACTION    = 5;

    protected Timeline timeline;
    protected Event parent;

    protected int    type       = TYPE_SINGLE;  // not yet impl
    protected String target     = null;
    protected String specialistName = null;
    protected int[]  orbs       = null;
    protected HSV    color      = null;
    protected float  fadeTime   = NO_TIME;
    protected String sound      = null;
    protected String song       = null;
    protected float  playTime   = NO_TIME; // maybe use duration?
    protected String command    = null;
    protected Specialist specialist = null;

    ///
    /// Ephemeral items used when running the timeline
    ///

    protected int state;
        
    public Event(Timeline timeline, Event parent) {
        this.timeline = timeline;
        this.parent = parent;
    }

    public void setType(int val) {
        this.type = val;
    }
    public int getType() {
        return this.type;
    }

    public void setTarget(String val) {
        this.target = val;
    }
    public String getTarget()  {
        return this.target;
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
            

    public void setSpecialistName(String val) {
        this.specialistName = val;
    }

    public String getSpecialistName() {
        return this.specialistName;
    }

    public void setSpecialist(Specialist val) {
        this.specialist = val;
    }

    public Specialist getSpecialist() {
        return this.specialist;
    }
    
    public void setState(int val) {
        this.state = val;
    }

    public int getState() {
        return this.state;
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

    ///////////////////////////////////////////////
    /// Handle performing the event.           ////
    ///////////////////////////////////////////////

    // perform this event's action
    public void performAction() {
        performAction(this);
    }
    
    // perform another event's action, e.g. set properties on this
    // action's Specialist
    public void performAction(Event modifyingEvent) {
        if (specialist == null) {
            return;
        }
        specialist.setProperties(modifyingEvent.properties);
        if (specialist instanceof ColorSpecialist && modifyingEvent.color != null) {
            ColorSpecialist cs = (ColorSpecialist)specialist;
            cs.setColor(modifyingEvent.color, modifyingEvent.fadeTime);
            
        }
        // TODO: run the command
    }

    //
    // Start up this event's specialist.
    //
    public Specialist startSpecialist(OrbControl orbControl) {
        if (specialistName == null) {
            return null;
        }
        Class specialistClass = Timeline.getSpecialistClass(specialistName);
        if (specialistClass == null) {
            return null;
        }
        Specialist sp = null;
        try {
            sp = (Specialist)specialistClass.newInstance();
            this.specialist = sp;
            sp.setup(orbControl, getProperties(), orbs);
            if (sp instanceof ColorSpecialist && color != null) {
                ColorSpecialist cs = (ColorSpecialist)sp;
                cs.setColor(color, fadeTime);
            }
            /* debug...
            System.out.print("Event(" + getName() + ") startSpecialist. orbs:");
            for(int i=0; i < orbs.length; i++) {
                System.out.print(" " + i + ":" + orbs[i]);
            }
            System.out.println();
            */

            sp.start();
            // later: perform the command, if any.
        } catch (Exception ex) {
            System.out.println("Event [name: " + getName() + "] caught exception starting specialist. ");
            ex.printStackTrace();
        }
        return sp;
    }

    
    public void stopSpecialist(OrbControl orbControl) {
        if (specialist ==  null) {
            return;
        }
        specialist.stop();
    }
    

    


    ///////////////////////////////////////////////
    ///                                        ////
    ///////////////////////////////////////////////

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
        writeAttribute(buf, indent, TARGET,     target);
        writeAttribute(buf, indent, SPECIALIST, specialistName);
        writePairedAttribute(buf, indent, NOTES, END_NOTES, notes);
        writeAttribute(buf, indent, DURATION,   duration, NO_TIME);
        writeAttribute(buf, indent, STARTTIME,  startTime, NO_TIME);
        writeAttribute(buf, indent, ENDTIME,    endTime, NO_TIME);
        writePairedAttribute(buf, indent, ORBS, END_ORBS, getOrbsAsString());
        writePairedAttribute(buf, indent, COLOR, END_COLOR, getColorAsString());
        if (properties != null && properties.size() > 0) {
            writeTag(buf, indent, PROPERTIES, true);
            for(Enumeration en = properties.propertyNames(); en.hasMoreElements(); ) {
                String pname = (String)en.nextElement();
                String val = properties.getProperty(pname);
                if (val != null) {
                    buf.append(indent);
                    buf.append("    ");
                    buf.append(pname);
                    buf.append(' ');
                    buf.append(val);
                    buf.append('\n');
                }
            }
            writeTag(buf, indent, END_PROPERTIES, true);
        }

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