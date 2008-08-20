package com.orbswarm.choreography;

import com.orbswarm.swarmcon.IOrbControl;
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
        System.out.println("[[[[ FOLLOW PATH.  <setup> " + path.getName());
    }

    public void start() {
        System.out.println("[[[[ FOLLOW PATH.  <start> " + path.getName());
        if (enabled && path != null) {
            //path.setOnCall(true);
            path.setActive(true);
            for(int i=0; i < orbs.length; i++) {
                if (orbs[i] >= 0) {
                    System.out.println("FollowPath <orb: " + i + " path: " + path.getName() + ">");
                    orbControl.followPath(i, path);
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
