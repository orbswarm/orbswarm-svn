package com.orbswarm.swarmcomposer.color;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class ColorSchemeAnalogous extends ColorScheme {

    private HSV baseColor;
    private float spread;
    private int numSwatches;

    private HSV[] swatches;
    
    public void init(int numSwatches, HSV baseColor, float spread) {
        this.numSwatches = numSwatches - 1;
        swatches = new HSV[this.numSwatches];
        for(int i=0; i < numSwatches - 1; i++) {
            swatches[i] = new HSV(0.f, 0.f, 0.f);
        }
        this.baseColor = baseColor;
        this.spread = spread;
        recalculate();
        broadcastColorSchemeChanged();
    }
    
    public void setBaseHue(float hue) {
        if (hue != baseColor.getHue()) {
            baseColor.setHue(hue);
            recalculate();
            broadcastColorSchemeChanged();
        }
    }

    public void setBaseSat(float sat) {
        if (sat != baseColor.getSat()) {
            baseColor.setSat(sat);
            recalculate();
            broadcastColorSchemeChanged();
        }
    }

    public void setBaseVal(float val) {
        if (val != baseColor.getVal()) {
            baseColor.setVal(val);
            recalculate();
            broadcastColorSchemeChanged();
        }
    }

    public void setBaseColor(HSV color) {
        baseColor = color;
        recalculate();
        broadcastColorSchemeChanged();
    }

    public void setSpread(float spread) {
        if (spread != this.spread) {
            this.spread = spread;
            recalculate();
            broadcastColorSchemeChanged();
        }
    }

    /* might put these in later...
       public  void setHue(int nth, float hue);
       public  void setSat(int nth, float sat);
       public  void setVal(int nth, float sat);
    */

    public HSV getBaseColor() {
        return baseColor;
    }
    
    public HSV getColor(int nth) {
        if (nth == 0) {
            return getBaseColor();
        }
        return swatches[nth - 1];
    }

    public float getSpread() {
        return this.spread;
    }

    public void swatchDragged(int dragSwatch, double r, double theta, double colorWheelRadius) {
        HSV baseColor = this.getBaseColor();
        if (dragSwatch == 0) {
            float hue = this.angleToHue((float)theta);
            float sat = (float)(r / colorWheelRadius);
            baseColor.setHue(hue);
            baseColor.setSat(sat);
            this.setBaseColor(baseColor);
        } else {
            // later: send dragSwatch() to colorScheme & let it figure it out.
            double baseAngle = this.hueToAngle(baseColor.getHue());
            double delta = Math.abs(theta - baseAngle);
            if (delta > Math.PI) {
                delta -= Math.PI;
            }
            double spread;
            if (dragSwatch == 2 || dragSwatch == 3) {
                spread = .5 * delta / Math.PI;
            } else if (dragSwatch == 5) {
                spread = delta / Math.PI;
            } else {
                spread = delta / (Math.PI);
            }
            
            this.setSpread((float)spread);
        }
    }

    
    private void recalculate() {
        float baseHue = baseColor.getHue();
        float baseAngle = hueToAngle(baseHue);
        // spread ranges from 0 to 1, while the spread angle ranges from 
        // 0 to 180 degrees. 
        float spreadAngle = (float)Math.PI * spread;
        for(int i=0; i < numSwatches; i++) {
            swatches[i].setSat(baseColor.getSat());
            swatches[i].setVal(baseColor.getVal());
        }
        swatches[0].setHue(angleToHue(baseAngle +  spreadAngle));
        swatches[1].setHue(angleToHue(baseAngle + .5f * spreadAngle));
        swatches[2].setHue(angleToHue(baseAngle - .5f * spreadAngle));
        swatches[3].setHue(angleToHue(baseAngle -  spreadAngle));
        swatches[4].setHue(angleToHue(baseAngle  + .25f * spreadAngle));
        swatches[4].setSat(baseColor.getSat() * .75f);
    }
}