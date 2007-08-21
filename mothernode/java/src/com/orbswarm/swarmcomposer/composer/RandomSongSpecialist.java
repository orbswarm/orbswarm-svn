package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.AbstractSpecialist;
import com.orbswarm.choreography.Swarm;

import com.orbswarm.swarmcon.OrbControlImpl;
import com.orbswarm.swarmcon.SwarmCon;

import java.io.File;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

public  class RandomSongSpecialist extends AbstractSpecialist {
    private boolean enabled = true;
    private BotControllerSongs botctl_songs;
    private BotVisualizer bv;
    private SwarmCon swarmCon;

    public void setup(OrbControl orbControl, Properties initialProperties, int[] orbs) {
        super.setup(orbControl, initialProperties, orbs);

        // HACQUE ALERT! this is too tightly coupled, but we're late in the game, and OUCH!
        OrbControlImpl oci = (OrbControlImpl)orbControl; // bad simran! bad simran!!
        swarmCon = oci.getSwarmCon();
        bv = swarmCon.botVisualizer;


        int numbots = orbs.length;
        numbots = 6; // TODO: allow it to control only the specified bots.
        
        botctl_songs = new BotControllerSongs(numbots, "/orbsounds/songs", orbControl);
        this.addNeighborListener(bv);
        this.addSwarmListener(bv);
        setProperties(initialProperties);
    }
    
    public void start() {
        if (enabled) {
            String songDir  = getProperty("songdir",   "resources/songs");
            String songName = getProperty("song",   "terminal.orbs");
            int time        = getIntProperty("time", 124); // seconds
            setDuration(time);
            swarmCon.addSpecialist(this);

            // TODO: set up the player to play through the OrbControl. 
            playSong(songDir + File.separatorChar + songName, time);
        }
    }

    public void playSong(final String songFilePath, final int songtime) {
        new Thread()
        {
            public void run()
            {
                botctl_songs.playSong(songFilePath, songtime);
            }
        }.start();
    }
    
    public void stop() {
        swarmCon.removeSpecialist(this);
        botctl_songs.stopControlling();
    }

    public void enable(boolean value) {
        // TODO: tell botController not to send stuff to OrbControl
        //       unless enabled. 
        enabled = value;
    }

    public void orbState(Swarm orbSwarm) {
        //System.out.println("RandomSongSpecialist . orbstate.");
        //int n = orbSwarm.getNumOrbs();
        int n = 6;
        // perhaps we should centralize the distances as pct of diameter calculation?
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
        double radius = 100.;
        botctl_songs.updateSwarmDistances(radius, n, distances);

    }

    public void command(String command, int orb, String property) {
        // handle volume and playsong commands.
        if (command.equalsIgnoreCase("play")) {
            String songDir  = getProperty("songdir",   "../songs");
            String songName = getProperty("song",   "terminal.orbs");
            int time        = getIntProperty("time", 0); // seconds
            
            playSong(songDir + File.separatorChar + songName, time);
        }
    }

      //////////////////////////////////////
     /// Swarm  Listener               ///
    //////////////////////////////////////
    /*
    ArrayList swarmListeners = new ArrayList();
    public void broadcastSwarmDistances(double radius, int nbeasties, double[][] distances) {
        for(Iterator it = swarmListeners.iterator(); it.hasNext(); ) {
            ((SwarmListener)it.next()).updateSwarmDistances(radius, nbeasties, distances);
        }
    }

    public void addSwarmListener(SwarmListener snoop) {
        swarmListeners.add(snoop);
    }
    */

    public void addSwarmListener(SwarmListener snoop) {
        botctl_songs.addSwarmListener(snoop);
    }

      //////////////////////////////////////
     /// Neighbor  Listener               ///
    //////////////////////////////////////

    /* I think we can do this differently: pass the listeners on to the botctl. 
    ArrayList neighborListeners = new ArrayList();

    public void setNeighbor(GossipEvent gev) {
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            ((NeighborListener)it.next()).setNeighbor(gev);
        }
    }
    public void neighborChanged(GossipEvent gev) {
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            ((NeighborListener)it.next()).neighborChanged(gev);
        }
    }
    
    public void neighborsChanged(List gossipEvents) {
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            ((NeighborListener)it.next()).neighborsChanged(gossipEvents);
        }
    }

    public void addNeighborListener(NeighborListener snoop) {
        neighborListeners.add(snoop);
    }
*/

    public void addNeighborListener(NeighborListener snoop) {
        botctl_songs.addNeighborListener(snoop);
    }

}
