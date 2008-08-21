package com.orbswarm.choreography;

import com.orbswarm.swarmcon.IOrbControl;
import com.orbswarm.swarmcon.SmoothPath;
import com.orbswarm.swarmcon.SwarmCon;

import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.choreography.timeline.TimelinePath;
import com.orbswarm.choreography.timeline.Timeline;

import java.util.Properties;

public class FollowPathSpecialist extends AbstractSpecialist  {
    private boolean enabled = true;
    private TimelinePath path;
    private Timeline timeline;
    
    public void setup(IOrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);
        SwarmCon sc = orbControl.getSwarmCon();
        // TODO: this is problematic. can't get tehe sc from the interface?
        timeline = sc.getTimeline();
        
        String pathName = getProperty("path:", null);
        if (pathName != null) {
            path = timeline.getPath(pathName);
        }
        //System.out.println("[[[[ FOLLOW PATH.  <setup> " + path.getName());
    }

    public void start() {
        ///System.out.println("[[[[ FOLLOW PATH.  <start> " + path.getName());
        if (enabled && path != null) {
            for(int i=0; i < orbs.length; i++) {
                if (orbs[i] >= 0) {
                    //System.out.println("FollowPath <orb: " + i + " path: " + path.getName() + ">");
                    threadedFollowPath(orbControl, orbs[i], path);
                }
            }
        }
    }

    private void threadedFollowPath(final IOrbControl orbControl,
                                    final int orbNum,
                                    final TimelinePath gpath) {
        Thread t = new Thread() {
                public void run() {
                    gpath.setActive(true);
                    timeline.addEphemeralPath(gpath);
                    orbControl.followPath(orbNum, gpath);
                    timeline.removeEphemeralPath(gpath);
                    gpath.setActive(false);
                    broadcastCommandCompleted("start", orbs, null);
                }
            };
        t.start();
    }

    public void stop() {
        //orbControl.stopPath(path);
        if (path != null) {
            path.setActive(false);
        }
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
