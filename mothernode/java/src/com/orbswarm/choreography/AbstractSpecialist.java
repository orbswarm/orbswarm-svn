package com.orbswarm.choreography;

public abstract class AbstractSpecialist implements Specialist {
    protected OrbControl orbControl;
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

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public void setProperty(String name, String val) {
        return properties.getProperty(name, val);
    }
}
