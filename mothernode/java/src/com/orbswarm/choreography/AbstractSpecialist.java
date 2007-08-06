package com.orbswarm.choreography;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public abstract class AbstractSpecialist implements Specialist {
    protected OrbControl orbControl;
    protected Properties properties;
    protected ArrayList commandListeners = null;

    public void setup(OrbControl orbControl, Properties initialProperties) {
        this.orbControl = orbControl;
        this.properties = new Properties();
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

    public void broadcastCommandCompleted(String action, int orb, String param) {
        if (commandListeners != null) {
            for(Iterator it = commandListeners.iterator(); it.hasNext(); ) {
                SpecialistListener ear = (SpecialistListener)it.next();
                ear.commandCompleted(this, action, orb, param);
            }
        }
    }

    public void delayedBroadcastCommandCompleted(int durationMS, String action, int orb, String param) {
        DelayedBCCThread dbccThread = new DelayedBCCThread(durationMS, action, orb, param);
        dbccThread.start();
    }

    class DelayedBCCThread extends Thread {
        int durationMS;
        String action;
        int orb;
        String param;

        public DelayedBCCThread(int durationMS, String action, int orb, String param) {
            this.durationMS = durationMS;
            this.action = action;
            this.orb = orb;
            this.param = param;
        }

        public void run() {
            try {
                Thread.sleep(durationMS);
            } catch (InterruptedException ex) {
            }
            broadcastCommandCompleted(action, orb, param);
        }
    }

}
