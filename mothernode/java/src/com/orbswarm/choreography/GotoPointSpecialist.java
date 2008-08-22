package com.orbswarm.choreography;

import com.orbswarm.swarmcon.IOrbControl;
import com.orbswarm.swarmcon.SmoothPath;
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
        x = getDoubleProperty("x:", 0.);
        y = getDoubleProperty("y:", 0.);
        relative = getBooleanProperty("relative:", false);
        /* debug
        System.out.print("[[[[ GotoPoint.  <setup> orbs: {");
        for(int i=0; i < orbs.length; i++) {
            System.out.print(orbs[i] + ", ");
        }
        System.out.println("} ]]]]");
        */
    }

    public void start() {
        //System.out.print("[[[[ GotoPoint.  <start> orbs: {");
        timeline = sc.getTimeline();
        for(int i=0; i < orbs.length; i++) {
            System.out.print(orbs[i] + ", ");
        }
        //System.out.println("[[[[ GotoPoint.  {" + x + ", " + y + "}  ]]]]");
        Swarm swarm = sc.getSwarm();
        if (enabled) {
            long travelTime = 0l;
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
                    threadedFollowPath(orbControl, orbs[i], gpath);
                }
            }
        }
    }

    private void threadedFollowPath(final IOrbControl orbControl,
                                    final int orbNum,
                                    final TimelinePath gpath) {
        Thread t = new Thread() {
                public void run() {
                    System.out.println("GotoPoint specialist. followpath. gpath: " + gpath + " timeline: " + timeline);
                    timeline.addEphemeralPath(gpath);
                    orbControl.followPath(orbNum, gpath);
                    timeline.removeEphemeralPath(gpath);
                    broadcastCommandCompleted("start", orbs, null);
                }
            };
        t.start();
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
