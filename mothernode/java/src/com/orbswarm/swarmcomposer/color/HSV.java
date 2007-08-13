package com.orbswarm.swarmcomposer.color;

import java.awt.Color;


/**
 * @author Simran Gleason
 */
public class HSV {
    protected float hue;
    protected float sat;
    protected float val;

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
        return new HSV(hsb[0], hsb[1], hsb[2]);
    }
    
    public HSV copy() {
        return new HSV(hue, sat, val);
    }
    
    public float getHue() {
        return hue;
    }

    public void setHue(float val) {
        this.hue = val;
    }


    public float getSat() {
        return sat;
    }

    public void setSat(float val) {
        this.sat = val;
    }


    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public Color toColor() {
        return Color.getHSBColor(hue, sat, val);
    }

    public String toString() {
        return "HSV[" + hue + ", " + sat + ", " + val + "]";
    }
}