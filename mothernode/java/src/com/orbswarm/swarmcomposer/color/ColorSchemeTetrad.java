package com.orbswarm.swarmcomposer.color;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class ColorSchemeTetrad extends ColorScheme {

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
        baseColor.setHue(hue);
        recalculate();
        broadcastColorSchemeChanged();
    }

    public void setBaseSat(float sat) {
        baseColor.setSat(sat);
        recalculate();
        broadcastColorSchemeChanged();
    }

    public void setBaseVal(float val) {
        baseColor.setVal(val);
        recalculate();
        broadcastColorSchemeChanged();
    }

    public void setBaseColor(HSV color) {
        baseColor = color;
        recalculate();
        broadcastColorSchemeChanged();
    }

    public void setSpread(float spread) {
        this.spread = spread;
        recalculate();
        broadcastColorSchemeChanged();
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
        if (theta < 0.) {
            theta += TWO_PI;
        }
        if (dragSwatch == 0) {
            float hue = this.angleToHue((float)theta);
            float sat = (float)(r / colorWheelRadius);
            baseColor.setHue(hue);
            baseColor.setSat(sat);
            this.setBaseColor(baseColor);
        } else {
        }
    }

    double spreadRange = Math.PI / 2.0;

    private void recalculate() {
        float baseHue = baseColor.getHue();
        float baseSat = baseColor.getSat();
        float baseAngle = hueToAngle(baseHue);
            
        for(int i=0; i < numSwatches; i++) {
            swatches[i].setSat(baseColor.getSat());
            swatches[i].setVal(baseColor.getVal());
        }
        float PI_OVER_2 = (float)(Math.PI / 2.);

        // in this one, the spread controls the saturation of the 2nd set of things. 
        swatches[0].setHue(angleToHue(baseAngle + PI_OVER_2 ));
        swatches[1].setHue(angleToHue(baseAngle - (float)Math.PI ));
        swatches[2].setHue(angleToHue(baseAngle - PI_OVER_2 ));
        swatches[3].setHue(baseHue);
        swatches[3].setSat(spread * baseSat);
        swatches[4].setHue(angleToHue(baseAngle + PI_OVER_2 ));
        swatches[4].setSat(spread * baseSat);
    }
}