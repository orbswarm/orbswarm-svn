package com.orbswarm.swarmcomposer.color;

import java.awt.Color;


/**
 * @author Simran Gleason
 */
public class HSV {
    protected float hue;
    protected float sat;
    protected float val;
    protected Color jcolor = null;

    public HSV(float hue, float sat, float val) {
        this.hue = hue;
        this.sat = sat;
        this.val = val;
    }

    public static HSV fromRGB(int r, int g, int b) {
        return fromColor(new Color(r, g, b));
    }

    public static HSV fromColor(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        HSV result =  new HSV(hsb[0], hsb[1], hsb[2]);
        result.jcolor = color;
        return result;
    }
    
    public HSV copy() {
        HSV copy = new HSV(hue, sat, val);
        copy.jcolor = jcolor;
        return copy;
    }
    
    public float getHue() {
        return hue;
    }

    public void setHue(float val) {
        this.hue = val;
        jcolor = null;
    }


    public float getSat() {
        return sat;
    }

    public void setSat(float val) {
        this.sat = val;
        jcolor = null;
    }


    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
        jcolor = null;
    }

    public Color toColor() {
        if (jcolor == null) {
            jcolor =  Color.getHSBColor(hue, sat, val);
        }
        return jcolor;
    }

    public String toString() {
        return "HSV[" + hue + ", " + sat + ", " + val + "]";
    }
}