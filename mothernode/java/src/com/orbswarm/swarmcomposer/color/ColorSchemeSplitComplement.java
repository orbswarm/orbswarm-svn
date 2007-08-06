package com.orbswarm.swarmcomposer.color;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Simran Gleason
 */
public class ColorSchemeSplitComplement extends ColorScheme {

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
        this.baseColor = baseColor.copy();
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
        baseColor = color.copy();
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
            // later: send dragSwatch() to colorScheme & let it figure it out.
            double baseAngle = this.hueToAngle(baseColor.getHue());
            double spreadAngle = spreadRange * spread;
            double baseDelta = spreadAngle/2.f;
            double gnomon = baseAngle - baseDelta;
            
            double delta = Math.abs(theta - gnomon);
            if (delta > Math.PI) {
                delta -= Math.PI;
            }
            double newSpreadAngle = 0.0;
            if (dragSwatch == 1 || dragSwatch == 5) {
                newSpreadAngle = delta * 2;
                System.out.println("\nDrag 1: (r:" + r + ", th: " + theta + ")" );
                System.out.println("  baseAngle: " + baseAngle + " baseHue: " + baseColor.getHue());
                System.out.println("  spreadAngle: " + spreadAngle + " spread: " + spread);
                System.out.println("  baseDelta: " + baseDelta + " gnomon: " + gnomon);
                System.out.println("  delta: " + delta + " newSpreadAngle: " + newSpreadAngle);
                
                    
            } else if (dragSwatch == 2 || dragSwatch == 3 || dragSwatch == 4) {
                double complementGnomon = (Math.PI + baseAngle) - baseDelta;
                double complementDelta = Math.abs(theta - complementGnomon);
                newSpreadAngle = complementDelta * 2;
            }
            double newSpread = newSpreadAngle / spreadRange;
            System.out.println("  New spread: " + newSpread);
            this.setSpread((float)newSpread);
        }
    }

    double spreadRange = Math.PI;

    private void recalculate() {
        float baseHue = baseColor.getHue();
        float baseAngle = hueToAngle(baseHue);
        // spread ranges from 0 to 1, while the spread angle ranges from 
        // 0 to 180 degrees.
        // TODO: parametrize the spreadAngle range.
        //
        float spreadAngle = (float)spreadRange * spread;
        float baseDelta = spreadAngle/2.f;
        float gnomon = baseAngle - baseDelta;
        float complementGnomon = gnomon + (float)Math.PI;
        if (complementGnomon > TWO_PI) {
            complementGnomon -= TWO_PI;
        }
            
        //System.out.println("\nRecalc SplitComp");
        //System.out.println("  baseAngle: " + baseAngle + " baseHue: " + baseColor.getHue());
        //System.out.println("  spreadAngle: " + spreadAngle + " spread: " + spread);
        //System.out.println("  baseDelta: " + baseDelta + " gnomon: " + gnomon + " compGnomon: " + complementGnomon);
        //System.out.println("  g - bd: " + (gnomon - baseDelta));
        
        for(int i=0; i < numSwatches; i++) {
            swatches[i].setSat(baseColor.getSat());
            swatches[i].setVal(baseColor.getVal());
        }
        swatches[0].setHue(angleToHue(gnomon - baseDelta));
        swatches[1].setHue(angleToHue(complementGnomon + baseDelta));
        swatches[2].setHue(angleToHue(complementGnomon - baseDelta));
        swatches[3].setHue(swatches[2].getHue());
        swatches[3].setSat(swatches[2].getSat() * .6f);
        swatches[4].setHue(baseColor.getHue());
        swatches[4].setSat(baseColor.getSat() * .4f);
    }
}
