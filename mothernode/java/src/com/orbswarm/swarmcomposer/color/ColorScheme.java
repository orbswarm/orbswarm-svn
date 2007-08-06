package com.orbswarm.swarmcomposer.color;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public abstract class ColorScheme {
    public static final float TWO_PI = (float)(2.f * Math.PI);

    private static HashMap colorSchemeRegistry = new HashMap();

    public ColorScheme() {
        colorSchemeListeners = new ArrayList();
    }
    
    public static ColorScheme getColorScheme(String type) {
        Class colorSchemeClass = (Class)colorSchemeRegistry.get(type);
        if (colorSchemeClass == null) {
            return null;
        }
        try {
            return (ColorScheme)colorSchemeClass.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void registerColorScheme(String type, Class csClass) {
        colorSchemeRegistry.put(type, csClass);
    }

    public static Iterator getRegisteredColorSchemes() {
        return colorSchemeRegistry.keySet().iterator();
    }
    
    ArrayList colorSchemeListeners = null;
        
    public abstract void init(int numSwatches, HSV baseColor, float spread);

    public abstract void setBaseHue(float hue);
    public abstract void setBaseSat(float sat);
    public abstract void setBaseVal(float val);
    public abstract void setBaseColor(HSV color);
    public abstract void setSpread(float spread);

    /* might put these in later...
       public abstract void setHue(int nth, float hue);
       public abstract void setSat(int nth, float sat);
       public abstract void setVal(int nth, float sat);
    */
    
    public abstract HSV getBaseColor();
    public abstract HSV getColor(int nth);
    public abstract float getSpread();
    
    public abstract void swatchDragged(int swatchNum,
                                       double r, double theta,
                                       double colorWheelRadius);

    public void addColorSchemeListener(ColorSchemeListener ear) {
        colorSchemeListeners.add(ear);
    }

    public void copyListeners(ColorScheme cs) {
        copyListeners(cs, true);
    }
    
    public void copyListeners(ColorScheme cs, boolean nukeOldListeners) {
        if (nukeOldListeners) {
            colorSchemeListeners = new ArrayList();
        }
        for(Iterator it=cs.colorSchemeListeners.iterator(); it.hasNext() ;) {
            ColorSchemeListener ear = (ColorSchemeListener)it.next();
            System.out.println("Copying CS Listener ... " + ear);
            addColorSchemeListener(ear);
        }
    }
    
    public void broadcastColorSchemeChanged() {
        //System.out.println("ColorScheme broadcastCSChange " + this + " [" + colorSchemeListeners.size() + "] listeners");
        for(Iterator it = colorSchemeListeners.iterator(); it.hasNext(); ) {
            ColorSchemeListener ear = (ColorSchemeListener)it.next();
            ear.colorSchemeChanged(this);
        }
    }
    
    protected float hueToAngle(float hue) {
        return TWO_PI * hue;
    }

    protected float angleToHue(float angle) {
        return angle / TWO_PI;
    }
}