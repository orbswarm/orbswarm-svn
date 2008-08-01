package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcon.IOrbControl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;


/**
 * @author Simran Gleason
 */
public class BotController implements NeighborListener, SwarmListener
{
    protected ArrayList bots;
    protected String basePath;
    protected int numbots;
    protected IOrbControl orbControl;
    protected ControllerThread controllerThread = null;
    protected double[][] swarmDistances;

    public BotController(int numbots, String basePath, IOrbControl orbControl)
    {
      this.numbots = numbots;
      this.basePath = basePath;
      bots = new ArrayList();
      for (int i=0; i < numbots; i++)
      {
        Bot bot = new Bot(i, "" + i, basePath, orbControl);
        bots.add(bot);
      }

    }

    public void startControlling()
    {
      setupBots();
      displayCurrentlyPlayingBots();
      startControllerThread();
    }

    public void stopControlling()
    {
      stopControllerThread();
    }

    public void displayCurrentlyPlayingBots()
    {
      for (Iterator bit = bots.iterator(); bit.hasNext(); )
      {
        Bot bot = (Bot)bit.next();
        System.out.println("Bot("  + bot.getName() + ") currently playing:" + bot.displayCurrentlyPlaying());
      }
    }

    public void setupBots()
    {
      for (Iterator it = bots.iterator(); it.hasNext();)
      {
        Bot bot = (Bot)it.next();
        bot.addNeighborListener(this);
      }
      broadcastDistances();
    }

    public void broadcastDistances()
    {
      for (Iterator it = bots.iterator(); it.hasNext();)
      {
        Bot botA = (Bot)it.next();
        Neighbor neighborA = botA.getSelfAsNeighbor(); // this returns a new one. no need to clone.
        for (Iterator itB = bots.iterator(); itB.hasNext();)
        {
          Bot botB = (Bot)itB.next();
          if (botA != botB)
          {
            // TODO: set distance on the neighbor, or something.
            GossipEvent gev = new GossipEvent(neighborA, "distance", new Float(0.));
            botB.setNeighbor(gev);
            botB.neighborChanged(gev);
          }
        }
      }
    }


    public void startBots()
    {
      System.out.println("BC: startBots(((((((()))))))).");
      for (Iterator it = bots.iterator(); it.hasNext(); )
      {
        Bot bot = (Bot)it.next();
        System.out.println("       " + bot.summarize());
      }
      for (Iterator it = bots.iterator(); it.hasNext(); )
      {
        Bot bot = (Bot)it.next();
        bot.startPlaying();
      }
    }

    public void stopBots()
    {
      System.out.println("BC: stopBots(((((((()))))))).");

      for (Iterator it = bots.iterator(); it.hasNext(); )
      {
        Bot bot = (Bot)it.next();
        bot.stopPlaying();
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    /// The controller thread.                                               ///
    /// The idea is to have the bots update distances and negotiate          ///
    /// songs, sets, etc on a separate thread from the one that sends the    ///
    /// distances to the BotController. That happens here.                   ///
    ////////////////////////////////////////////////////////////////////////////

    public void startControllerThread()
    {
      System.out.println("start controller thread. controllerThread: " + controllerThread);
      if (controllerThread == null)
      {
        controllerThread = new ControllerThread(this);
        System.out.println("created controller thread. ");
      }
      System.out.println("starting controller thread: " + controllerThread);
      controllerThread.start();
      System.out.println("started controller thread: " + controllerThread);

    }

    public void stopControllerThread()
    {
      if (controllerThread != null)
      {
        controllerThread.halt();
      }
    }

    class ControllerThread extends Thread
    {
        public boolean controlling = false;
        protected BotController botController;
        public ControllerThread(BotController botController)
        {
          this.botController = botController;
        }
        public synchronized void run()
        {
          try
          {
            controlling = true;
            while (controlling)
            {
              botController.sendSwarmDistancesToBots();
              try
              {
                Thread.currentThread().sleep(400);
              }
              catch (InterruptedException e)
              {
                System.out.println("Error sleeping");
              }
            }
          }
          catch (Exception ex)
          {
            System.err.println("Controller Thread caught exception: " + ex);
            ex.printStackTrace(System.err);
          }
        }

        public  void halt()
        {
          System.out.println("\n\n\n======ControllerTHread   HALT!!!\n\n\n");
          controlling = false;
        }
    }

    /////////////////////////////
    /// Neighborly relations  ///
    /////////////////////////////

    //
    // from the controller's point of view, we need to receive neighbor events
    // from bots, and then broadcast them to all the other bots.
    //

    public void setNeighbor(GossipEvent gev)
    {
      Neighbor theGossip = gev.getNeighbor();
      String name = theGossip.getName();
      for (Iterator it = bots.iterator(); it.hasNext(); )
      {
        Bot bot = (Bot)it.next();
        if (bot.getName() != name)
        {
          bot.setNeighbor(gev);
        }
      }
      for (Iterator it = neighborListeners.iterator(); it.hasNext(); )
      {
        NeighborListener gossip = (NeighborListener)it.next();
        gossip.setNeighbor(gev);
      }
    }

    public void neighborChanged(GossipEvent gev)
    {
      setNeighbor(gev);
      Neighbor theGossip = gev.getNeighbor();
      String name = theGossip.getName();
      for (Iterator it = bots.iterator(); it.hasNext(); )
      {
        Bot bot = (Bot)it.next();
        if (bot.getName() != name)
        {
          bot.neighborChanged(gev);
        }
      }
      for (Iterator it = neighborListeners.iterator(); it.hasNext(); )
      {
        NeighborListener gossip = (NeighborListener)it.next();
        gossip.neighborChanged(gev);
      }

      String event = gev.getEvent();
      if (event.equals("end_song"))
      {
      }
    }

    public void neighborsChanged(List gossipEvents)
    {
      for (Iterator it = gossipEvents.iterator(); it.hasNext(); )
      {
        neighborChanged((GossipEvent)it.next());
      }
    }

    private List neighborListeners = new ArrayList();
    public void addNeighborListener(NeighborListener gossip)
    {
      neighborListeners.add(gossip);
    }

    public void broadcastGossip(GossipEvent gev)
    {
      for (Iterator it = neighborListeners.iterator(); it.hasNext(); )
      {
        NeighborListener gossip = (NeighborListener)it.next();
        gossip.neighborChanged(gev);
      }
    }

    private List swarmListeners = new ArrayList();
    public void addSwarmListener(SwarmListener ear)
    {
      swarmListeners.add(ear);
    }

    // this is how the bot controller gets the distance measurements
    public void updateSwarmDistances(double radius, int nbeasties, double [][] distances)
    {
      //System.out.println("BotController:: updateSwarmDistances...");
      swarmDistances = distances;
      needToSendSwarmDistances = true;
      for (Iterator it = swarmListeners.iterator(); it.hasNext(); )
      {
        SwarmListener sl = (SwarmListener)it.next();
        sl.updateSwarmDistances(radius, nbeasties, distances);
      }

    }

    boolean needToSendSwarmDistances = false;
    public void sendSwarmDistancesToBots()
    {
      //System.out.println("BotController:: send Swarm Distances to bots... needed=" + needToSendSwarmDistances);
      if (!needToSendSwarmDistances)
      {
        return;
      }
      // Somehow this needs to go on another thread...
      // maybe we need some nasty semaphore stuff to tell it when new
      // distances have arrived.
      int i=0;
      for (Iterator it = bots.iterator(); it.hasNext();)
      {
        Bot bot = (Bot)it.next();
        bot.updateNeighborDistances(swarmDistances[i]);
        i++;
      }
      needToSendSwarmDistances = false;
    }

    //////////////////////////
    /// Random utils...    ///
    //////////////////////////

    protected boolean randomChance(int percent)
    {
      return (100 * Math.random() < percent);
    }

    protected float randomRange(float a, float b)
    {
      float delta = b - a;
      return (float)(a + delta * Math.random());
    }

    protected float randomSign()
    {
      if (Math.random() < .5)
      {
        return -1.f;
      }
      return 1.f;
    }

}
