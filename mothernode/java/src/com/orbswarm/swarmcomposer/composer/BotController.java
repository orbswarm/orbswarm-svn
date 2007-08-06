package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.swarmcomposer.sound.SimpleJavaPlayer;

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
public class BotController implements NeighborListener, SwarmListener, ColorSchemeListener {
    private ArrayList bots;
    private ArrayList playList;
    private String basePath;
    private int numbots;
    private ArrayList orbsongs;
    private BotVisualizer bv;
    private ArrayList botColorListeners = new ArrayList();
    
    private int songTime = 0;
    
    public BotController(int numBots, String basePath) {
        this.numbots = numbots;
        this.basePath = basePath;
        bots = new ArrayList();
        orbsongs = new ArrayList();
        playList = new ArrayList();
        for(int i=0; i < numBots; i++) {
            Bot bot = new Bot(i, "Bot_" + i, basePath);
            bots.add(bot);
            bot.setDefaultPlayer(new SimpleJavaPlayer(i));
        }
        setupBotColors(numBots, numBots);

        bv = new BotVisualizer(6);
        this.addNeighborListener(bv);
        this.addSwarmListener(bv);
        
    }

    public void setSongTime(int val) {
        this.songTime = val;
    }

    public int getSongTime() {
        return this.songTime;
    }

    public void startControlling() {
        setupBots();
        displayCurrentlyPlayingBots();
        startControllerThread();
    }

    public void displayCurrentlyPlayingBots() {
        for(Iterator bit = bots.iterator(); bit.hasNext(); ) {
            Bot bot = (Bot)bit.next();
            System.out.println("Bot("  + bot.getName() + ") currently playing:" + bot.displayCurrentlyPlaying());
        }
    }

    public void readSongFile(String songFile) throws IOException {
        List songs = Bot.readSongFile(songFile, basePath);
        for(Iterator it = songs.iterator(); it.hasNext(); ) {
            Song song = (Song)it.next();
            playList.add(song);
            for(Iterator botit = bots.iterator(); botit.hasNext(); ) {
                Bot bot = (Bot)botit.next();
                // Note: going to need to clone the song here. 
                bot.addSong(song);
            }
        }
    }

    public Song chooseRandomSong() {
        int n = playList.size();
        return (Song)playList.get((int)(n * Math.random()));
    }

    public void randomizePlaylist() {
    }
    
    public void setupBots() {
        for(Iterator it = bots.iterator(); it.hasNext();) {
            Bot bot = (Bot)it.next();
            bot.addNeighborListener(this);
        }
        broadcastDistances();
    }

    public void broadcastDistances() {
        for(Iterator it = bots.iterator(); it.hasNext();) {
            Bot botA = (Bot)it.next();
            Neighbor neighborA = botA.getSelfAsNeighbor(); // this returns a new one. no need to clone. 
            for(Iterator itB = bots.iterator(); itB.hasNext();) {
                Bot botB = (Bot)itB.next();
                if (botA != botB) {
                    // TODO: set distance on the neighbor, or something.
                    GossipEvent gev = new GossipEvent(neighborA, "distance", new Float(0.));
                    botB.setNeighbor(gev);
                    botB.neighborChanged(gev);
                }
            }
        }
    }
    
    public void resetSong(Song song) {
        // not sure about when they should turn off gossip & when they should
        // turn it on, to get the startup sequence right...
        for(Iterator it = bots.iterator(); it.hasNext();) {
            Bot bot = (Bot)it.next();
            //bot.refrainFromGossip(true); //TODO
        }
        for(Iterator it = bots.iterator(); it.hasNext();) {
            Bot bot = (Bot)it.next();
            //bot.refrainFromGossip(false);
            bot.loadSong(song);  // TODO (also, this doesn't really start it)
        }
    }

    public void startBots() {
        System.out.println("BC: startBots(((((((()))))))).");
        for(Iterator it = bots.iterator(); it.hasNext(); ) {
            Bot bot = (Bot)it.next();
            System.out.println("       " + bot.summarize());
        }
        for(Iterator it = bots.iterator(); it.hasNext(); ) {
            Bot bot = (Bot)it.next();
            bot.startPlaying();
        }        
    }
    //
    // is this an inline loop, or its own thread?
    //
    public void startControllerThread() {
        for(Iterator sit=playList.iterator(); sit.hasNext(); ) {
            Song song = (Song)sit.next();
            if (song instanceof MultiChannelComposition) {
                playMultiChannelComposition((MultiChannelComposition)song);
            } else {
                System.out.println("Starting Song: " + song.getName());
                for(Iterator it = bots.iterator(); it.hasNext(); ) {
                    Bot bot = (Bot)it.next();
                    bot.setSongTime(songTime);
                    bot.loadSong(song);
                }
                
                startBots();
                for(Iterator it = bots.iterator(); it.hasNext(); ) {
                    Bot bot = (Bot)it.next();
                    Thread pt = bot.getPlayerThread();
                    if (pt != null) {
                        try {
                            System.out.println("BC::Waiting for bot thread(" + bot.getName() + ")");
                            // later: take into account waiting for not longer than song time. 
                            pt.join();
                            System.out.println("BC::FINISHED bot thread(" + bot.getName() + ")");
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            }
            System.out.println("=================");
            System.out.println("= song ended    =");
            System.out.println("=================");
            try {
                Thread.currentThread().sleep(5000);
            } catch (Exception ex) {
            }
        }
    }

    public void playMultiChannelComposition(MultiChannelComposition song) {
        ArrayList threads = new ArrayList();
        for(int i=0; i < song.numTracks(); i++) {
            String trackPath = song.getTrackPath(i);
            SoundFilePlayer player = new SimpleJavaPlayer(i);
            Thread playThread = new TrackPlayThread(player, trackPath);
            threads.add(playThread);
            playThread.start();
        }
        System.out.println("Now wait for them all...");
        for(Iterator it = threads.iterator(); it.hasNext(); ) {
            Thread t = (Thread)it.next();
            try {
                t.join();
            } catch (Exception ex) {
            }
        }
    }

    class TrackPlayThread extends Thread {
        private SoundFilePlayer player;
        private String trackPath;
        public TrackPlayThread(SoundFilePlayer player, String trackPath) {
            this.player = player;
            this.trackPath = trackPath;
        }   
        public void run() {
            player.playFile(trackPath);
        }
    }
    
    /////////////////////////////
    /// Neighborly relations  ///
    /////////////////////////////

    //
    // from the controller's point of view, we need to receive neighbor events
    // from bots, and then broadcast them to all the other bots.
    //

    public void setNeighbor(GossipEvent gev) {
        Neighbor theGossip = gev.getNeighbor();
        String name = theGossip.getName();
        for(Iterator it = bots.iterator(); it.hasNext(); ) {
            Bot bot = (Bot)it.next();
            if (bot.getName() != name) {
                bot.setNeighbor(gev);
            }
        }
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            NeighborListener gossip = (NeighborListener)it.next();
            gossip.setNeighbor(gev);
        }
    }
    
    public void neighborChanged(GossipEvent gev) {
        setNeighbor(gev);
        Neighbor theGossip = gev.getNeighbor();
        String name = theGossip.getName();
        for(Iterator it = bots.iterator(); it.hasNext(); ) {
            Bot bot = (Bot)it.next();
            if (bot.getName() != name) {
                bot.neighborChanged(gev);
            }
        }
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            NeighborListener gossip = (NeighborListener)it.next();
            gossip.neighborChanged(gev);
        }

        String event = gev.getEvent();
        if (event.equals("end_song")) {
        }
    }

    public void neighborsChanged(List gossipEvents) {
        for (Iterator it = gossipEvents.iterator(); it.hasNext(); ) {
            neighborChanged((GossipEvent)it.next());
        }
    }

    private List neighborListeners = new ArrayList();
    public void addNeighborListener(NeighborListener gossip) {
        neighborListeners.add(gossip);
    }

    public void broadcastGossip(GossipEvent gev) {
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            NeighborListener gossip = (NeighborListener)it.next();
            gossip.neighborChanged(gev);
        }
    }

    private List swarmListeners = new ArrayList();
    public void addSwarmListener(SwarmListener ear) {
        swarmListeners.add(ear);
    }

    // this is how the bot controller gets the distance measurements
    public void updateSwarmDistances(double radius, int nbeasties, int [][] distances) {
        //System.out.println("BotController:: updateSwarmDistances...");
        int i=0;
        for(Iterator it = bots.iterator(); it.hasNext();) {
            Bot bot = (Bot)it.next();
            bot.updateNeighborDistances(distances[i]);
            i++;
        }
        for(Iterator it = swarmListeners.iterator(); it.hasNext(); ) {
            SwarmListener sl = (SwarmListener)it.next();
            sl.updateSwarmDistances(radius, nbeasties, distances);
        }

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
        //System.out.println("BC:: update bot colors..");
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

    private boolean randomChance(int percent) {
        return (100 * Math.random() < percent);
    }
    
    private float randomRange(float a, float b) {
        float delta = b - a;
        return (float)(a + delta * Math.random());
    }

    private float randomSign() {
        if (Math.random() < .5) {
            return -1.f;
        }
        return 1.f;
    }

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

    public JFrame getFrame() {
        return bv.getFrame();
    }

    public JPanel getPanel() {
        return bv.getPanel();
    }
    
    public static void main(String[] args) {
        int numbots = 6;
        if (args.length > 0) {
            BotController botctl = new BotController(numbots, "/orbsongs");
            botctl.bv.getFrame();
            botctl.handleArgs(args);
            botctl.playSongs();
        }
    }

    public void handleArgs(String[] args) {
        int songTime = 0;
        int i=0;
        while (i < args.length) {
            if (args[i].equalsIgnoreCase("--songtime")) {
                i++;
                songTime = Integer.parseInt(args[i]);
                i++;
            } else {
                orbsongs.add(args[i]);
                System.out.println("BC:: song: " + args[i]);
                i++;
            }
        }
        this.setSongTime(songTime);
    }

    public void playSongs() {
        playSongs(this.orbsongs);
    }
    
    public void playSongs(List orbsongs) {
        for(Iterator it=orbsongs.iterator(); it.hasNext(); ) {
            String songFile = (String)it.next();
            System.out.println("BotController: reading song file: " + songFile);
            try {
                this.readSongFile(songFile);
            } catch (Exception ex) {
                System.out.println("BotController.main() caught exception. " + ex);
                ex.printStackTrace();
            }
        }
        Bot bot0 = (Bot) this.bots.get(0);
        System.out.println(bot0.toString());
        System.out.println("----------------");

        this.startControlling();
        
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
}
