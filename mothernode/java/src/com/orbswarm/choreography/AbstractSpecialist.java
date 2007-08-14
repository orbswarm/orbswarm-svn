package com.orbswarm.choreography;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public abstract class AbstractSpecialist implements Specialist {
    protected OrbControl orbControl;
    protected Properties properties;
    protected ArrayList commandListeners = null;
    protected int []orbs;

    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        this.orbControl = orbControl;
        this.properties = new Properties();
        this.orbs = orbs;
        if (this.orbs == null) {
            this.orbs = Specialist.ALL_ORBS;
        }
        
        if (initialProperties != null) {
            for(Enumeration en = initialProperties.propertyNames(); en.hasMoreElements(); ) {
                String name = (String)en.nextElement();
                properties.setProperty(name, initialProperties.getProperty(name));
            }
        }
    }

    /////////////////////////////////////////////
    /// Property management                   ///
    /////////////////////////////////////////////
    
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public String getProperty(String name, String defaultVal) {
        String result = properties.getProperty(name);
        if (result == null) {
            result = defaultVal;
        }
        return result;
    }


    public void setProperties(Properties addProps) {
        if (addProps == null) {
            return;
        }
        for(Enumeration en = addProps.propertyNames(); en.hasMoreElements(); ) {
            String pname = (String)en.nextElement();
            properties.setProperty(pname, addProps.getProperty(pname));
        }
    }

    public void setProperty(String name, String val) {
        properties.setProperty(name, val);
    }

    public int getIntProperty(String name, int defaultVal) {
        String prop = getProperty(name);
        int result = defaultVal;
        try {
            result = Integer.parseInt(prop);
        } catch (Exception ex) {
        }
        return result;
    }

    public float getFloatProperty(String name, float defaultVal) {
        String prop = getProperty(name);
        float result = defaultVal;
        try {
            result = Float.parseFloat(prop);
        } catch (Exception ex) {
        }
        return result;
    }

    public double getDoubleProperty(String name, double defaultVal) {
        String prop = getProperty(name);
        double result = defaultVal;
        try {
            result = Double.parseDouble(prop);
        } catch (Exception ex) {
        }
        return result;
    }
 
    /////////////////////////////////
    /// Command Listener          ///
    /////////////////////////////////
    public void addCommandListener(SpecialistListener ear) {
        if (commandListeners == null) {
            commandListeners = new ArrayList();
        }
        commandListeners.add(ear);
    }

    // Note: only need to do this when an event is in a sequence. 
    public void broadcastCommandCompleted(String action, int []orbs, String param) {
        if (commandListeners != null) {
            for(Iterator it = commandListeners.iterator(); it.hasNext(); ) {
                SpecialistListener ear = (SpecialistListener)it.next();
                ear.commandCompleted(this, action, orbs, param);
            }
        }
    }

    public void delayedBroadcastCommandCompleted(int durationMS, String action, int[] orbs, String param) {
        DelayedBCCThread dbccThread = new DelayedBCCThread(durationMS, action, orbs, param);
        dbccThread.start();
    }

    class DelayedBCCThread extends Thread {
        int durationMS;
        String action;
        int[] orbs;
        String param;

        public DelayedBCCThread(int durationMS, String action, int[] orbs, String param) {
            this.durationMS = durationMS;
            this.action = action;
            this.orbs = orbs;
            this.param = param;
        }

        public void run() {
            try {
                Thread.sleep(durationMS);
            } catch (InterruptedException ex) {
            }
            broadcastCommandCompleted(action, orbs, param);
        }
    }

}
