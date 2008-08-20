package com.orbswarm.choreography;

import com.orbswarm.swarmcon.IOrbControl;
import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.Mobject;
import com.orbswarm.swarmcon.Target;
import com.orbswarm.swarmcon.Point;

import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.choreography.timeline.TimelinePath;
import com.orbswarm.choreography.timeline.Timeline;

import java.util.Properties;

public class GotoPointSpecialist extends AbstractSpecialist  {
    private boolean enabled = true;
    private Timeline timeline;
    private double x = 0.;
    private double y = 0.;
    private boolean relative = false;
    private SwarmCon sc = null;

    public void setup(IOrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
        sc = orbControl.getSwarmCon();
        // TODO: this is problematic. can't get tehe sc from the interface?
        timeline = sc.getTimeline();
        x = getDoubleProperty("x:", 0.);
        y = getDoubleProperty("y:", 0.);
        relative = getBooleanProperty("relative:", false);
        
        System.out.print("[[[[ GotoPoint.  <setup> orbs: {");
        for(int i=0; i < orbs.length; i++) {
            System.out.print(orbs[i] + ", ");
        }
        System.out.println("} ]]]]");
    }

    public void start() {
        System.out.print("[[[[ GotoPoint.  <start> orbs: {");
        for(int i=0; i < orbs.length; i++) {
            System.out.print(orbs[i] + ", ");
        }
        System.out.println("[[[[ GotoPoint.  {" + x + ", " + y + "}  ]]]]");
        Swarm swarm = sc.getSwarm();
        if (enabled) {
            for(int i=0; i < orbs.length; i++) {
                if (orbs[i] >= 0) {
                    Orb orbi = swarm.getOrb(orbs[i]);
                    Point posi = orbi.getPosition();
                    double x0 = posi.getX();
                    double y0 = posi.getY();
                    System.out.println("GotoPoint orb " + orbs[i] + " from {" + x0 + ", " + y0 + "} to {" + x + ", " + y + "}");
                    String name = "goto("+ orbs[i]+ ")<" + x + ", " + y + ">";
                    TimelinePath gpath = new TimelinePath(name);
                    gpath.add(new Target(x0, y0));
                    gpath.add(new Target(x, y));
                    gpath.reshape();
                    gpath.resetColors();
                    gpath.setActive(true);
                    timeline.addEphemeralPath(gpath);
                    orbControl.followPath(orbs[i], gpath);
                }
            }
            broadcastCommandCompleted("start", orbs, null);
        }
    }

    public void stop() {
        //orbControl.stopPath(path);
    }

    public void enable(boolean value) {
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        // N/A
    }

    public void command(String command, int orb, String property) {
        // later we can put speed controls and such in here. 
    }
    
}
