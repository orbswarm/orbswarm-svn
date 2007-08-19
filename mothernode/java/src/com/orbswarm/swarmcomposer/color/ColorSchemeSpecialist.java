package com.orbswarm.swarmcomposer.color;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.AbstractSpecialist;
import com.orbswarm.choreography.Swarm;
import com.orbswarm.choreography.timeline.Timeline;

import com.orbswarm.swarmcon.OrbControlImpl;
import com.orbswarm.swarmcon.SwarmCon;

import com.orbswarm.swarmcomposer.composer.BotControllerColor; // this needs to move!

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public  class ColorSchemeSpecialist extends AbstractSpecialist implements ColorSchemeListener, BotColorListener {
    private boolean enabled = true;
    private BotControllerColor botctl_color;
    private ColorSchemer schemer;
    private SwarmCon swarmCon;

    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
        // HACQUE ALERT! this is too tightly coupled, but we're late in the game, and OUCH!
        OrbControlImpl oci = (OrbControlImpl)orbControl; // bad simran! bad simran!!
        swarmCon = oci.getSwarmCon();
        schemer = swarmCon.colorSchemer;
        this.addBotColorListener(schemer);
        schemer.addColorSchemeListener(this);

        int numbots = orbs.length;
        numbots = 6;
        botctl_color = new BotControllerColor(numbots, "/orbsongs", orbControl);
        botctl_color.addBotColorListener(this);

        schemer.broadcastNewColorScheme();
        schemer.broadcastColorSchemeChanged();
        // need to do this again to set the properties on the schemer, etc once it's been created. 
        setProperties(initialProperties);
    }

    
    public void start() {
        if (enabled) {
            String scheme = getProperty("colorscheme", null);
            if (schemer != null && scheme != null) {
                schemer.setColorScheme(scheme);
            }
            String baseColorStr = getProperty("basecolor", null);
            HSV color = Timeline.colorFromSpec(baseColorStr);
            if (color != null) {
                schemer.getColorScheme().setBaseColor(color);
            }
            int spread = getIntProperty("spread", -1);
            if (spread != -1) {
                schemer.getColorScheme().setSpread((float)spread / 100.f);
            }
            int meander = getIntProperty("meander", -1);
            if (meander != -1) {
                botctl_color.setMeander((float)meander / 100.f);
            }
            
            swarmCon.addSpecialist(this);
        }
    }

    public void stop() {
        if (schemer != null) {
            schemer.removeColorSchemeListener(this);
        }
        swarmCon.removeSpecialist(this);

    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        System.out.println("COlorSchemeSpecialist.orbState()");
        //int n = orbSwarm.getNumOrbs();
        int n = 6;
        double[][] distances = new double[n][n];
        // TODO: get the arena diameter stuff correct...
        //       (comes from 
        double diameter = 10.;
        for(int i=0; i < n; i++) {
            Orb orb = orbSwarm.getOrb(i);
            // note:: orb distances are in meters.
            //        color scheme expects distances as percentage of the diameter of the arena
            double orbDistances[] = orb.getDistances();
            for(int j=0; j < n; j++) {
                distances[i][j] = orbDistances[j] * 100. / diameter;  
            }
        }
        //printDistances(distances); // debug. 
        double radius = 100.;
        botctl_color.updateSwarmDistances(radius, n, distances);
    }

    public static void printDistances(double [][] distances) {
        StringBuffer buf = new StringBuffer();
        printDistances(buf, distances);
        System.out.println(buf.toString());
    }


    public static void printDistances(StringBuffer buf, double [][] distances) {
        int n = distances[0].length;
        buf.append(" + |   ");
        for(int i=0; i < n; i++) {
            buf.append(i + "   ");
        }
        buf.append("\n________________\n");
            for(int i=0; i < n; i++) {
            buf.append(" " + i + " | ");
            for(int j=0; j <=i; j++) { 
                String num = (int)distances[i][j] + "";
                while (num.length() < 3) {
                    num = " " + num;
                }
                buf.append(num + " ");
            }
            buf.append("\n");
        }
        buf.append("------------------\n");
    }

    public void setProperty(String name, String value) {
        super.setProperty(name, value);
        if (name.equalsIgnoreCase("colorscheme") && schemer != null) { 
            schemer.setColorScheme(value);

        } else if (name.equalsIgnoreCase("basecolor") && schemer != null) {
            HSV color = Timeline.colorFromSpec(value);
            if (color != null) {
                schemer.getColorScheme().setBaseColor(color);
            }

        } else if  (name.equalsIgnoreCase("spread") && schemer != null) {
            int spread = getIntProperty("spread", -1);
            if (spread != -1) {
                schemer.getColorScheme().setSpread((float)spread / 100.f);
            }
        } else if  (name.equalsIgnoreCase("meander") && botctl_color != null) {
            int meander = getIntProperty("meander", -1);
            if (meander != -1) {
                botctl_color.setMeander((float)meander / 100.f);
            }
        }
        //TODO: handle settinghte color scheme, base color, etc.
    }
    
    public void command(String command, int orb, String property) {
        //TODO: handle 
    }

    
      //////////////////////////////////////
     /// ColorSchemeListener            ///
    //////////////////////////////////////

    public void colorSchemeChanged(ColorScheme colorScheme) {
        System.out.println("CSS.colorChemeChanged(" + colorScheme + ")");
        botctl_color.colorSchemeChanged(colorScheme);
    }

    public void newColorScheme(ColorScheme ncs) {
        System.out.println("CSS.newColorScheme(" + ncs + ")");
        botctl_color.newColorScheme(ncs);
    }

      //////////////////////////////////////
     /// Bot Color Listener             ///
    //////////////////////////////////////

    public void botColorChanged(int bot, int swatch, HSV color) {
        System.out.println("CSS: botColorChanged( " + bot + " ) " + color);
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
