package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcomposer.color.BotColorListener;
import com.orbswarm.swarmcomposer.color.ColorScheme;
import com.orbswarm.swarmcomposer.color.ColorSchemeListener;
import com.orbswarm.swarmcomposer.color.HSV;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
    

/**
 * @author Simran Gleason
 */
public class BotControllerColor extends BotController implements SwarmListener, ColorSchemeListener {
    private ArrayList botColorListeners = new ArrayList();

    public BotControllerColor(int numBots, String basePath) {
        super(numBots, basePath);
        setupBotColors(numBots, numBots);
    }

    public void updateSwarmDistances(double radius, int nbeasties, int [][] distances) {
        super.updateSwarmDistances(radius, nbeasties, distances);
        updateBotColors(nbeasties, distances, currentColorScheme);
    }


    ///////////////////////////////////////
    ///  Color algorithm.              
    ///
    /// * each bot has a color (HSV)
    /// * colors for each bot can change within a range around the
    ///   color set by the color scheme. This can also be represented by a range.
    ///   The range can also be represented as an HSV. 
    /// * the distance of the bots from the bot with the base color determines
    ///   the size of the range.
    /// * Need some way to determine how bots are assigned to swatches in the
    ///   ColorScheme. 
    ///   possibility: sort them by order of the sum distance to the other bots.
    ///   Bot with most close-by neighbors is the base color bot.
    /// * When the base color bot changes, the ColorScheme gets its base color
    ///   changed (which will change the colors of the other bots). 
    ///

    HSV[] botColors;
    HSV[] botColorRanges;
    int [] botToSwatchMapping;
    int [] swatchToBotMapping;
    int baseColorBot = 0;
    ColorScheme currentColorScheme;

    public void setColorScheme(ColorScheme cs) {
        this.currentColorScheme = cs;
    }
    

    private void setupBotColors(int nbeasties, int numSwatches) {
        // for now, assuming nbeasties == numSwatches. (== 6 for orbswarm)
        botColors = new HSV[nbeasties];
        botColorRanges = new HSV[nbeasties];
        botToSwatchMapping = new int[nbeasties];
        swatchToBotMapping = new int[nbeasties];

        for(int i=0; i < nbeasties; i++) {
            botColors[i] = new HSV(0.f, 0.f, 1.f);
            botColorRanges[i] = new HSV(.5f, .7f, 0.f);
            botToSwatchMapping[i] = i;
            swatchToBotMapping[i] = i;
        }
    }

    boolean switchSchemeColorWhenBaseChanges = false;
    int baseSwapChance = 100;

    boolean debugswap = false;
    // distances are measured in percentage of diameter. 
    public void updateBotColors(int nbeasties, int [][]distances, ColorScheme colorScheme) {
        System.out.println("BC:: update bot colors..");
        System.out.println("BC::    colroScheme: " + colorScheme);
        // for now, assuming nbeasties == numSwatches. (== 6 for orbswarm)

        //
        // First, figure out which bot should be the base color, by summing the distances
        // to the other bots and choosing the one with the lowest overall sum.
        //  (Note: this shouldn't be done too often)
        //  (Also: it may work just setting the base color, but not worrying about ranking the others)
        int lowestSum = 50000;
        int newBaseBot = 0;
        for(int b=0; b < nbeasties; b++) {
            int sum = 0;
            int[] bd = distances[b];
            for(int o=0; o < nbeasties; o++) {
                sum += bd[o];
            }
            if (sum < lowestSum) {
                lowestSum = sum;
                newBaseBot = b;
            }
        }
        int currentBaseBot = swatchToBotMapping[0];
        if (newBaseBot != currentBaseBot && randomChance(baseSwapChance)) {
            System.out.println("BC:: SWAP BASE. old: " + currentBaseBot + " new: " + newBaseBot);
            int newBaseBotsOldSwatch = botToSwatchMapping[newBaseBot];
            swatchToBotMapping[0] = newBaseBot;
            botToSwatchMapping[newBaseBot] = 0;
            swatchToBotMapping[newBaseBotsOldSwatch] = currentBaseBot;
            botToSwatchMapping[currentBaseBot] = newBaseBotsOldSwatch;
            if (switchSchemeColorWhenBaseChanges) {
                HSV newBaseColor = colorScheme.getColor(newBaseBotsOldSwatch);
                if (newBaseColor.getSat() < .6f) {
                    newBaseColor.setSat(.7f);
                }
                colorScheme.setBaseColor(newBaseColor.copy());
            } 
              
        }

        //
        // next, determine the range for each bot color based on the distance to the base bot.
        //   (multiply the given range by the distance in percents, so closer bots will have a
        //    smaller range).
        // finally update the colors randomly within the ranges.
        //
        if (debugswap) {
            System.out.println();
            System.out.print("B->S: ");
            for(int i=0; i < 6; i++) {
                System.out.print("  b" + i + "=<" + botToSwatchMapping[i] + ">");
            }
            System.out.println();
            System.out.print("S->B: ");
            for(int i=0; i < 6; i++) {
                System.out.print("  <" + i + ">=" + swatchToBotMapping[i] + "b");
            }
            System.out.println();
        }
        for(int i=0; i < nbeasties; i++) {
            int baseColorBot = swatchToBotMapping[0];
            if (baseColorBot == i) {
                HSV schemeColor = colorScheme.getColor(0);
                HSV botColor = botColors[i];
                botColor.setHue(schemeColor.getHue());
                botColor.setSat(schemeColor.getSat());
                botColor.setVal(schemeColor.getVal());
                
                //Color actualColor = botColor.toColor();
                broadcastBotColorChanged(i, 0, botColor);
                
            } else {
                int[] botDistances = distances[i];
                int distanceToBase = botDistances[baseColorBot];

                float rangeMultiplier = distanceToBase / 100.f;

                int swatch = botToSwatchMapping[i];
                HSV currentSchemeColor = colorScheme.getColor(swatch);
                HSV botColor = botColors[i];
                //
                // The colors move in increments (+/- 1/10 of range?)
                // giving kind of a random walk.
                // However, they are constrained to be within the range of the scheme hue.
                //
                float hueRange = rangeMultiplier * botColorRanges[i].getHue();
                float increment = hueRange * .1f * randomSign();
                float schemeHue = currentSchemeColor.getHue();
                float newHue = botColor.getHue() + increment;
                if (debugswap) 
                System.out.println("   Bot(" + i + " sw<" + swatch + ">) dist: " + distanceToBase + " hueRange: " + hueRange + " old Hue: " + botColor.getHue() + " newHue: " + newHue + " <swatchHue: " + schemeHue + ">");
                newHue = constrainDelta(newHue, schemeHue, hueRange / 2.f);
                if (debugswap) 
                System.out.println("         newHue(after constrain)Delta: " + newHue);
                newHue = constrainHue(newHue);

                float satRange = rangeMultiplier * botColorRanges[i].getSat();
                float schemeSat = currentSchemeColor.getSat();
                float newSat = botColor.getSat() + increment;
                newSat = constrainDelta(newSat, schemeSat, satRange / 2.f);
                newSat = constrainN1(newSat, .4f);

                // later: move halfway from current hue/sat to the randomized one?
                botColor.setHue(newHue);
                botColor.setSat(newSat);
                //System.out.println("    BC::BotColor[" + i + "] h: " + newHue + " s: " + newSat);
                //Color actualColor = botColor.toColor();
                broadcastBotColorChanged(i, swatch, botColor);
            }
        }
    }

      //////////////////////////////////////
     /// ColorSchemeListener            ///
    //////////////////////////////////////

    public void colorSchemeChanged(ColorScheme colorScheme) {
        System.out.println("BC: colorschemechanged....");
        for(int i=0; i < numbots; i++) {
            int swatch = botToSwatchMapping[i];
            botColors[i] = colorScheme.getColor(swatch).copy();
        }
    }

    public void newColorScheme(ColorScheme ncs) {
        System.out.println("BC: got NEW color scheme. ");
        setColorScheme(ncs);
    }
    
    public void addBotColorListener(BotColorListener snoop) {
        botColorListeners.add(snoop);
    }

    public void broadcastBotColorChanged(int bot, int swatch, HSV color) {
        for(Iterator it = botColorListeners.iterator(); it.hasNext(); ) {
            ((BotColorListener)it.next()).botColorChanged(bot, swatch, color);
        }
    }

    /////////////////////////////////////
    /// color wander constraints      ///
    /////////////////////////////////////

    private float constrainDelta(float val, float target, float delta) {
        if (val < target - delta) {
            return target - delta;
        } else if (val > target + delta) {
            return target + delta;
        } else {
            return val;
        }
    }
    
    private float constrain01(float v) {
        if (v < 0.f) {
            return 0.f;
        }
        if (v > 1.f) {
            return 1.f;
        }
        return v;
    }

    private float constrainN1(float v, float lowerBound) {
        if (v < lowerBound) {
            return lowerBound;
        }
        if (v > 1.f) {
            return 1.f;
        }
        return v;
    }

    private float constrainHue(float v) {
        if (v < 0.f) {
            return v + 1.0f;
        }
        if (v > 1.f) {
            return v - 1.f;
        }
        return v;
    }

}
