package com.orbswarm.swarmcomposer.color;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.AbstractSpecialist;
import com.orbswarm.choreography.Swarm;

import com.orbswarm.swarmcomposer.composer.BotControllerColor; // this needs to move!

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public  class ColorSchemeSpecialist extends AbstractSpecialist implements ColorSchemeListener, BotColorListener {
    private boolean enabled = true;
    private BotControllerColor botctl_color;

    public void setup(OrbControl orbControl, Properties initialProperties) {
        super.setup(orbControl, initialProperties);

        int numbots = 6; // where does this really come from?
        botctl_color = new BotControllerColor(numbots, "/orbsongs");
        botctl_color.addBotColorListener(this);
    }

    
    public void start() {
        if (enabled) {
            int orbNum = getIntProperty("orb",   -1);
            int hue    = getIntProperty("hue",    0);
            int sat    = getIntProperty("sat",   80);
            int val    = getIntProperty("val",   80);
            int time   = getIntProperty("time", 400);
        }
    }

    public void stop() {
        // N/A
    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        System.out.println("COlorSchemeSpecialist.orbState()");
        int n = orbSwarm.getNumOrbs();
        int[][] intdistances = new int[n][n];
        for(int i=0; i < n; i++) {
            Orb orb = orbSwarm.getOrb(i);
            double distances[] = orb.getDistances();
            for(int j=0; j < n; j++) {
                intdistances[i][j] = (int)distances[j];  // note:: orb distances are in meters.
                // TODO: get the radius stuff correct...
            }
        }
        double radius = 100.;
        botctl_color.updateSwarmDistances(radius, n, intdistances);
    }

    public void setProperty(String name, String value) {
        super.setProperty(name, value);
        //TODO: handle settinghte color scheme, base color, etc.
    }
    
    public void command(String command, int orb, String property) {
        //TODO: handle 
    }

    
      //////////////////////////////////////
     /// ColorSchemeListener            ///
    //////////////////////////////////////

    public void colorSchemeChanged(ColorScheme colorScheme) {
        botctl_color.colorSchemeChanged(colorScheme);
    }

    public void newColorScheme(ColorScheme ncs) {
        botctl_color.newColorScheme(ncs);
    }

      //////////////////////////////////////
     /// Bot Color Listener             ///
    //////////////////////////////////////
    public void botColorChanged(int bot, int swatch, HSV color) {
        int hue = (int)(255 * color.getHue());
        int sat = (int)(255 * color.getSat());
        int val = (int)(255 * color.getVal());
        int time = 400; // what should this be?
        orbControl.orbColor(bot, hue, sat, val, time);
        
        broadcastBotColorChanged(bot, swatch, color);
    }

    ArrayList botColorListeners = new ArrayList();
    public void broadcastBotColorChanged(int bot, int swatch, HSV color) {
        for(Iterator it = botColorListeners.iterator(); it.hasNext(); ) {
            ((BotColorListener)it.next()).botColorChanged(bot, swatch, color);
        }
    }

    public void addBotColorListener(BotColorListener snoop) {
        botColorListeners.add(snoop);
    }

}
