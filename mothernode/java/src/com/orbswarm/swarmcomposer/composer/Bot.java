package com.orbswarm.swarmcomposer.composer;

import com.orbswarm.choreography.OrbControl;

import com.orbswarm.swarmcomposer.util.TokenReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author Simran Gleason
 */
public class Bot implements NeighborListener {
    protected String basePath;
    protected String name;
    protected int botnum;
    //
    // constants for song file parsing
    //
    public static final String SONG       = "<song>";
    public static final String END_SONG   = "</song>";
    public static final String MULTI_CHANNEL_COMPOSITION = "MultiChannelComposition";
    public static final String LAYER      = "<layer>";
    public static final String END_LAYER  = "</layer>";
    public static final String SET        = "<set>";
    public static final String END_SET    = "</set>";
    public static final String MATRIX     = "<matrix>"; 
    public static final String END_MATRIX = "</matrix>"; 
    public static final String SOUND     = "<sound>"; 
    public static final String END_SOUND = "</sound>"; 
    public static final String TRACK     = "<track>"; 
    public static final String END_TRACK = "</track>"; 

    public static final String END        = "end";
    public static final String HEADERS    = "headers";
    public static final String COMPILED   = "compiled";
    public static final String STRATEGY   = "strategy";
    public static final String PATH       = "path";
    public static final String BEAT_LOCK  = "beatlock";
    public static final String BPM        = "bpm";
    public static final String HASH       = "hash";
    public static final String MP3_HASH   = "mp3hash";
    public static final String DURATION   = "duration";

    protected Neighbor  selfAsNeighbor;
    protected HashMap   neighbors;
    protected double[]  neighborDistances;
    protected ArrayList neighborListeners;
    protected ArrayList songs;

    protected PlayerThread playerThread = null;
    protected OrbControl orbControl;

    //
    // current selections
    //
    protected Song  currentSong;
    protected Layer currentLayer;
    protected Set   currentSet;
    protected Sound currentSound;

    protected int songTime = 0; // seconds

    protected SoundFilePlayer currentPlayer = null;
    protected SoundFilePlayer defaultPlayer = null;
    protected int broadcastOnFlag = 1;

    // TODO: player loop
    // TODO: song negotiation
    // TODO: layer selection
    // TODO: neighbor listener
    // TODO: set selection
    // TODO: set reads directory to get sounds
    // TODO: aif/wav player
    // TODI: mp3 player
    // TODO: multichannel player
    // TOOO: test strategy reading. 

    public Bot(int num, String name, String basePath, OrbControl orbControl) {
        this.botnum = num;
        this.name = name;
        this.basePath = basePath;
        this.orbControl = orbControl;
        this.songs = new ArrayList();
        this.neighbors = new HashMap();
        this.neighborListeners = new ArrayList();
        this.neighborDistances = new double[6]; // TODO: need way to do this so it works with varying
        //                                   // numbers of bots...
    }

    public String getName() {
        return this.name;
    }

    public void setName(String val) {
        this.name = val;
    }

    public void setSongTime(int val) {
        this.songTime = val;
    }
    
    public String getBasePath() {
        return this.basePath;
    }

    public void setBasePath(String val) {
        this.basePath = val;
    }

    public HashMap getNeighbors() {
        return neighbors;
    }

    public void setDefaultPlayer(SoundFilePlayer player) {
        this.defaultPlayer = player;
    }
    
    /////////////////////////////////
    /// Current song, layer, etc  ///
    /////////////////////////////////

    public Song getCurrentSong() {
        return currentSong;
    }

    public void setCurrentSong(Song val) {
        currentSong = val;
    }

    public String getCurrentSongName() {
        if (currentSong == null) {
            return "none";
        }
        return currentSong.getName();
    }

    public int getCurrentSongPlayTime() {
        return 27; // TODO: fix this
    }

    public Layer getCurrentLayer() {
        return currentLayer;
    }

    public void setCurrentLayer(Layer val) {
        currentLayer = val;
    }

    public String getCurrentLayerName() {
        if (currentLayer == null) {
            return "none";
        }
        return currentLayer.getName();
    }

    public Set getCurrentSet() {
        return currentSet;
    }

    public void setCurrentSet(Set val) {
        currentSet = val;
    }

    public String  getCurrentSetName() {
        if (currentSet == null) {
            return "none";
        }
        return currentSet.getName();
    }

    public String getCurrentSoundName() {
        if (currentSound == null) {
            return "none";
        }
        return currentSound.getName();
    }

    /**
     * read song file with one or more songs in it.
     */
    public static List readSongFile(String songFilePath) throws IOException {
        return readSongFile(songFilePath, null);
    }
    
    public static List readSongFile(String songFilePath, String basePath) throws IOException {
        TokenReader reader = new TokenReader(songFilePath);
        reader.setIgnoreCommentLines(true);
        List songs = new ArrayList();
        String songToken = reader.readUntilToken(SONG);
        while (songToken != null) {
            String token = reader.readToken();
            System.out.println("RSF: " + token);
            System.out.println("MCC: " + Bot.MULTI_CHANNEL_COMPOSITION);
            Song song;
            if (token.equalsIgnoreCase(Bot.MULTI_CHANNEL_COMPOSITION)) {
                String name = reader.readToken();
                dbpost("Reading MultichannelComposition[" + name + "]");
                song = new MultiChannelComposition(name, basePath);
                readMultiChannelComposition(reader, (MultiChannelComposition)song);
            } else {
                String name = token;
                dbpost("Reading Song[" + name + "]");
                song = new Song(name, basePath);
                readSong(reader, song);
                dbpost("Song:" + name + ".matrix: " + song.getMatrix());
            }
            songs.add(song);
            songToken = reader.readUntilToken(SONG);
        }
        return songs;
    }

    public static void readMultiChannelComposition(TokenReader reader,
                                                   MultiChannelComposition mcSong)  throws IOException {
        String token = reader.readToken();
        System.out.println("RMC: " + token);
        while (token != null) {
            if (token.equalsIgnoreCase(END_SONG)) {
                return;
            } else if (token.equalsIgnoreCase(PATH)) {
                String path = reader.readToken();
                dbpost("Song path: " + path);
                mcSong.setPath(path);
            } else if (token.equalsIgnoreCase(COMPILED)) {
                boolean compiled = reader.readBoolean();
                mcSong.setCompiled(compiled);
            } else if (token.equalsIgnoreCase(TRACK)) {
                System.out.println(" Adding  <track>: " + token);
                Track track = readTrack(reader, mcSong);
                mcSong.addTrack(track);
            } else {
                System.out.println(" Adding simply-named track: " + token);
                Track track = new Track(mcSong, token);
                mcSong.addTrack(track);
            }
            // else read metadata here...
            token = reader.readToken();
        }
    }
    
    public static void readSong(TokenReader reader, Song song) throws IOException {
        String token = reader.readToken();
        while (token != null) {
            if (token.equalsIgnoreCase(END_SONG)) {
                return;
            } else if (token.equalsIgnoreCase(STRATEGY)) {
                String strategyName = reader.readToken();
                song.setStrategy(strategyName);
            } else if (token.equalsIgnoreCase(COMPILED)) {
                boolean compiled = reader.readBoolean();
                song.setCompiled(compiled);
            } else if (token.equalsIgnoreCase(LAYER)) {
                dbpost("Reading layer..");
                song.addLayer(readLayer(reader, song));
            } else if (token.equalsIgnoreCase(MATRIX)) {
                dbpost("Reading matrix..");
                song.getMatrix().readMatrix(reader);
            } else if (token.equalsIgnoreCase(PATH)) {
                String path = reader.readToken();
                dbpost("Song path: " + path);
                song.setPath(path);
            } else if (token.equalsIgnoreCase(BEAT_LOCK)) {
                int beatlock = reader.readInt();
                dbpost("Song beatlock: " + beatlock);
                song.setBeatLock(beatlock);
            } else if (token.equalsIgnoreCase(BPM)) {
                int bpm = reader.readInt();
                dbpost("Song bpm: " + bpm);
                song.setBPM(bpm);
            }
            // else read metadata here...
            token = reader.readToken();
        }
    }
    

    // assumption: already read the token "layer"
    public static Layer readLayer(TokenReader reader, Song song) throws IOException {
        String name = reader.readToken();
        Layer layer = new Layer(name, song);
        String token = reader.readToken();
        while (token != null) {
            if (token.equals(END_LAYER)) {
                return layer;
            } else if (token.equalsIgnoreCase(STRATEGY)) {
                String strategyName = reader.readToken();
                layer.setStrategy(strategyName);
            } else if (token.equalsIgnoreCase(SET)) {
                layer.addSet(readSet(reader, layer));
            } else if (token.equalsIgnoreCase(PATH)) {
                String path = reader.readToken();
                layer.setPath(path);
            } else if (token.equalsIgnoreCase(BEAT_LOCK)) {
                int beatlock = reader.readInt();
                dbpost("Layer beatlock: " + beatlock);
                layer.setBeatLock(beatlock);
            } else if (token.equalsIgnoreCase(BPM)) {
                int bpm = reader.readInt();
                dbpost("Layer bpm: " + bpm);
                layer.setBPM(bpm);
            } // else read metadata here...

            token = reader.readToken();
        }
        return layer;
    }

    // assumption: already read the token "set"
    public static Set readSet(TokenReader reader, Layer layer) throws IOException {
        String name = reader.readToken();
        Set set = new Set(name, layer);
        String token = reader.readToken();
        dbpost("Reading set(" + name + ")...");
        while (token != null) {
            if (token.equals(END_SET)) {
                break;
            } else if (token.equalsIgnoreCase(STRATEGY)) {
                String strategyName = reader.readToken();
                set.setStrategy(strategyName);
            } else if (token.equalsIgnoreCase(SOUND)) {
                // actually, these are read from the set's directory?
                set.addSound(readSound(reader, set));
            } else if (token.equalsIgnoreCase(PATH)) {
                String path = reader.readToken();
                set.setPath(path);
            } // else read metadata here...

            token = reader.readToken();
        }
        // check to see if it's an already compiled set?
        set.readSounds();
        return set;
    }

    public static Sound readSound(TokenReader reader, Set set) throws IOException {
        // tbf: read a compiled sound declaration:
        // path, name, length, hash
        String name = reader.readToken();
        Sound sound = new Sound(set, name);
        String token = reader.readToken();
        while (token != null) {
            if (token.equals(END_SOUND)) {
                break;
            } else if (token.equalsIgnoreCase(DURATION)) {
                float duration = reader.readFloat();
                sound.setDuration(duration);
            } else if (token.equalsIgnoreCase(HASH)) {
                String hash = reader.readToken();
                sound.setPCMHash(hash);
            } else if (token.equalsIgnoreCase(MP3_HASH)) {
                String hash = reader.readToken();
                sound.setMP3Hash(hash);
            }
            token = reader.readToken();
        }
        return sound;
    }

    public static Track readTrack(TokenReader reader, MultiChannelComposition mcSong) throws IOException {
        // tbf: read a compiled sound declaration:
        // path, name, length, hash
        String name = reader.readToken();
        Track track = new Track(mcSong, name);
        String token = reader.readToken();
        while (token != null) {
            if (token.equals(END_TRACK)) {
                break;
            } else if (token.equalsIgnoreCase(DURATION)) {
                float duration = reader.readFloat();
                track.setDuration(duration);
            } else if (token.equalsIgnoreCase(HASH)) {
                String hash = reader.readToken();
                track.setPCMHash(hash);
            } else if (token.equalsIgnoreCase(MP3_HASH)) {
                String hash = reader.readToken();
                track.setMP3Hash(hash);
            }
            token = reader.readToken();
        }
        return track;
    }

    public static void writeSong(StringBuffer buf, Song song) {
        song.write(buf);
    }
    
    public static void writeAttribute(StringBuffer buf, String indent, String attName, String value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writeAttribute(StringBuffer buf, String indent, String attName, int value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writeAttribute(StringBuffer buf, String indent, String attName, float value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
        buf.append('\n');
    }

    public static void writeAttribute_nlf(StringBuffer buf, String indent, String attName, String value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
    }

    public static void writeAttribute_nlf(StringBuffer buf, String indent, String attName, int value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
    }

    public static void writeAttribute_nlf(StringBuffer buf, String indent, String attName, float value) {
        buf.append(indent);
        buf.append(attName);
        buf.append(' ');
        buf.append(value);
    }

    public void addSong(Song song) {
        song.setBot(this);
        songs.add(song);
    }

    public Song findSong(String name) {
        for(Iterator it = songs.iterator(); it.hasNext(); ) {
            Song song = (Song)it.next();
            if (song.getName().equalsIgnoreCase(name)) {
                return song;
            }
        }
        return null;
    }


    //////////////////////////////
    ///  Neighborly relations  ///
    //////////////////////////////

    public void broadcastOff() {
        broadcastOnFlag --;
    }

    public void broadcastOn() {
        broadcastOnFlag++;
        if (broadcastOnFlag > 1) {
            broadcastOnFlag = 1;
        }
    }

    public boolean broadcastIsOn() {
        return broadcastOnFlag > 0;
    }
        
    public void broadcastGossip(String event, Object value) {
        Neighbor selfNeighbor = getSelfAsNeighbor();
        GossipEvent gev = new GossipEvent(selfNeighbor, event, value);
    
        broadcastGossip(gev);
    }

    public void broadcastGossip(GossipEvent gev) {
        for(Iterator it = neighborListeners.iterator(); it.hasNext(); ) {
            NeighborListener gossip = (NeighborListener)it.next();
            gossip.neighborChanged(gev);
        }
    }
        
    public void addNeighborListener(NeighborListener gossip) {
        neighborListeners.add(gossip);
    }

    public void setNeighbor(GossipEvent gev) {
        Neighbor theGossip = gev.getNeighbor();
        String name = theGossip.getName();
        neighbors.put(name, theGossip);
    }
    
    public void neighborChanged(GossipEvent gev) {
        // handle single gossip event
        // TODO:neighborGossipReceived(neighbor);
        //neighborGossipReceived(neighbor);
        Neighbor theGossip = gev.getNeighbor();
        String event = gev.getEvent();
        Object value = gev.getObject();
        if (event.equals("end_song")) {
            dbpost("BOT("+getName()+") received end_song message.");
            stopPlayerThread();
        } else if (event.equals("song_change")) {
            negotiateSongChange((Song)value); // or is that the song name?
        } else if (event.equals("layer_change")) {
            negotiateLayerChange((Layer)value);
        } else if (event.equals("set_change")) {
            // I don't think we have to do any set negotiation here.
            // the distance changes are what make the sets change...
            //negotiateSetChange((Set)value);
        }
        // still don't know how to handle distances...
    } 

    public void neighborsChanged(List gossipEvents) {
        // handle multiple gossip events
    } 

    public void negotiateSongChange(Song s) {
        dbpost("Bot(" + getName() + "): negotiate song change: " + s.getName());
        //loadSong(s);
    }

    public void negotiateLayerChange(Layer layer) {
        if (layer != null) {
            String layerName = layer.getName();
            dbpost("Bot(" + getName() + "): negotiate layer change: " + layerName);
            if (currentLayer == null ||
                !layerName.equals(currentLayer.getName())) {
                selectNewLayer();
            }
        }
    }
    
    public void negotiateSetChange(Set s) {
        //dbpost("Bot(" + getName() + "): negotiate set change: " + s.getName());
    }

    public void updateSelfAsNeighbor() {
        selfAsNeighbor = new Neighbor(botnum,
                                      getName(), 
                                      getCurrentSongName(),
                                      getCurrentSongPlayTime(),
                                      getCurrentLayerName(),
                                      getCurrentSetName(),
                                      getCurrentSoundName());
    }
    
    public Neighbor getSelfAsNeighbor() {
        updateSelfAsNeighbor();
        return selfAsNeighbor;
    }

    //////////////////////////////
    ///  Layer negotiations    ///
    //////////////////////////////
    public void loadSong(Song song, int songTime) {
        this.songTime = songTime;
        loadSong(song);
    }

    public void loadSong(Song song) {
        if (this.currentSong != song) {
            this.currentSong = song;
            dbpost("Bot(" + getName() + "): loadSong: " + song.getName());
            broadcastOff();
            selectNewLayer();
            broadcastOn();
            broadcastSongChange(song);
        }
    }

    public void selectNewLayer() {
        // find a layer that another neighbor isn't using.
        dbpost("Bot(" + getName() + ") selectNewLayer. currentLayer: " + (currentLayer == null ? " null" : currentLayer.getName()));
        dbpost("  neighbors: " + neighbors.size());
        for(Iterator lit = currentSong.getLayers().iterator(); lit.hasNext(); ) {
            Layer layer = (Layer)lit.next();
            dbpost(" .Bot(" + getName() + ") trying layer: " + layer.getName());
            if (layer != currentLayer) {
                boolean noNeighborHasLayer = true;
                for(Iterator nit = neighbors.keySet().iterator(); nit.hasNext(); ) {
                    Neighbor neighbor = (Neighbor)neighbors.get(nit.next());
                    dbpost("  Bot(" + getName() + ") Testing layer(" + layer.getName() + ") against neighbor(" + neighbor.getName() + ") layer name: [" + neighbor.getLayer() + "]");
                    if (layer.getName().equals(neighbor.getLayer())) {
                        dbpost("    nope...");
                        noNeighborHasLayer = false;
                    }
                }
                dbpost("  Bot(" + getName() + ") noNeighborHasLayer: " + noNeighborHasLayer);
                if (noNeighborHasLayer) {
                    this.currentLayer = layer;
                    dbpost("  Bot(" + getName() + ") choosing layer: " + layer.getName());
                    broadcastOff();
                    selectNewSet();
                    broadcastOn();
                    displayNeighborStates();
                    broadcastLayerChange(layer);
                    return;
                }
            }
        }
        // if no layer has been chosen (perhaps because there are not yet any neighbors?)
        // then just take the first layer.
        if (currentLayer == null) {
            currentLayer = (Layer)currentSong.getLayers().get(0);
            dbpost("   Bot(" + getName() + ") falling through & choosing first layer. " + currentLayer.getName());
            broadcastOff();
            selectNewSet();
            broadcastOn();
            displayNeighborStates();
            broadcastLayerChange(currentLayer);
        }
    }

    public void displayNeighborStates() {
        dbpost("Bot(" + getName() + ") NeighborStates:");
        for(Iterator nit = neighbors.keySet().iterator(); nit.hasNext(); ) {
            String nn = (String)nit.next();
            Neighbor neighbor = (Neighbor)neighbors.get(nn);
            dbpost("  Neighbor("  + neighbor.getName() + ") Song: " + neighbor.getSong() + " layer: " + neighbor.getLayer() + " set: " + neighbor.getSet());
        }
    }

    /**
     * This is where the big distance-compatibility matrix selection happens.
     */
    public void selectNewSet() {
        //dbpost("   Bot[" + botnum + "] Selecting new set.");
        if (currentSong == null) {
            return;
        }
        
        ArrayList sets = currentLayer.getSets();
        if (sets.size() == 0) {
            selectSet(null);
            return;
        } else if (sets.size() == 1) {
            selectSet((Set)sets.get(0));
            //System.out.print("   Bot[" + botnum + "] Selecting from [" + sets.get(0) + "]");
            return;
        }
        ArrayList setCandidates = new ArrayList();
        for(Iterator it = sets.iterator(); it.hasNext();) {
            Set candidateSet = (Set)it.next();
            int sumInterferences = 0;
            //System.out.print("   Bot[" + botnum + "] S:" + candidateSet.getName() + " Interferences: ");
            for(Iterator nit = neighbors.keySet().iterator(); nit.hasNext(); ) {
                Neighbor neighbor = (Neighbor)neighbors.get(nit.next());
                String nset = neighbor.getSet();
                int nn = neighbor.getNum();
                if (nset != null && !nset.equals("none")) {
                    int interference =
                        (100 - currentSong.compatibility(candidateSet, nset)) *
                        (100 - (int)neighborDistances[nn]);
                    sumInterferences += interference;
                    //System.out.print(" {" + nn + ":" + nset + ":");
                    //System.out.print(" C: " + currentSong.compatibility(candidateSet, nset));
                    //System.out.print(" D: " + neighborDistances[nn]);
                    //System.out.print(" I::" + interference + "} ");
                }
            }
            //dbpost(" SUM: " + sumInterferences);
            sortedInsert(new SetCandidate(candidateSet, sumInterferences), setCandidates);
        }
        //dbpost("Sorted Candidates: " + setCandidates);
        //
        // find the top candidates (within 5% of the top one (lowest interference))
        //  note:  5% of possible interference coefficient, which ranges from 0-10000,
        //          == 500
        //
        int fudgeFactor = 3000;
        SetCandidate topContender = (SetCandidate)setCandidates.get(0);
        int topRange = 1; // index of first candidate not within 5% (or length)
        for(int i=topRange; i< setCandidates.size(); i++) {
            SetCandidate sc = (SetCandidate)setCandidates.get(i);
            if (sc.interference < topContender.interference + fudgeFactor) {
                topRange++;
            }
        }
        SetCandidate setc = (SetCandidate)randomChoice(setCandidates, topRange);
        selectSet(setc.set);
        dbpost("Bot(" + getName() + ") choosing set: " + currentSet.getName() + " from " + setCandidates + "[0, "+ topRange + "]");
    }

    public void sortedInsert(SetCandidate sc, ArrayList sclist) {
        for(int i=0; i < sclist.size(); i++) {
            SetCandidate lsc = (SetCandidate)sclist.get(i);
            if (sc.interference <= lsc.interference) {
                sclist.add(i, sc);
                return;
            }
        }
        sclist.add(sc);
    }

    class SetCandidate {
        public Set set;
        public int interference;
        public SetCandidate(Set set, int interference) {
            this.set = set;
            this.interference = interference;
        }
        public String toString() {
            return "SC("+ set.getName() + "|" + interference + ")";
        }
    }
    
    // return random element between 0 and max (not inclusive)
    public Object randomChoice(ArrayList list, int max) {
        if (max == 0 || max > list.size()) {
            max = list.size();
        }
        return list.get((int)(max * Math.random()));
    }
    
    public void selectSet(Set set) {
        this.currentSet = set;
        if (set != null) {
            broadcastSetChange(set);
        }
    }

    public void broadcastSongChange(Song song) {
        if (broadcastIsOn()) {
            broadcastGossip("song_change", song);
        }
    }

    public void broadcastLayerChange(Layer layer) {
        if (broadcastIsOn()) {
            broadcastGossip("layer_change", layer);
        }
    }

    public void broadcastSetChange(Set set) {
        if (broadcastIsOn()) {
            broadcastGossip("set_change", set);
        }
    }


    //////////////////////////////
    ///  Set choice alogrithm  ///
    //////////////////////////////
    /*
     * Given set of distances between the neighbors:
     *   If the distance to any of the neighbors changes bin (say 20% bins):
     *      for each candidate set:
     *         calculate sum of interference factors with each of neighbors
     *      sort candidates by interference factor
     *      create set of acceptables by taking the top candidates that
     *       all have an interference factor within 5% of the top one. 
     *      Choose among those by possible criteria:
     *        - if current set is in that set, keep it.
     *             (perhaps based on a random factor, or how long we've had it)
     *        - choose set in list that is least used by neighbors
     *        - just choose randomly.
     *
     *  InterferenceFactor(SetX, SetA) = (1 - compatibility(SetX, SetA)) *
     *                                   (100 - distancePercent(BotX, BotA)
     *
     */
    public void updateNeighborDistances(double[] distances) {
        boolean chooseNewSet = false;
        int binsize = 20;
        //System.out.print("Bot[" + botnum + "]: neighbor dist: ");
        for(int i=0; i < distances.length; i++) {
            if (i != botnum) {
                int oldbin = (int)(neighborDistances[i] / binsize);
                int newbin = (int)(distances[i] / binsize);
                //System.out.print("(" + oldbin + "=>" + newbin + ") ");
                if (oldbin != newbin) {
                    chooseNewSet = true;
                    break;
                }
            }
        }
        //dbpost(" choose new: " + chooseNewSet);
        neighborDistances = distances;
        if (chooseNewSet) {
            selectNewSet();
        }
    }

    //////////////////////////////
    ///  Player thread         ///
    //////////////////////////////

    public PlayerThread getPlayerThread() {
        return playerThread;
    }
    
    public void startPlaying() {
        if (playerThread == null) {
            playerThread = new PlayerThread(this);
            playerThread.start();
        }
    }

    public void stopPlaying() {
        System.out.println("Bot(" + getName() + ") stopPlaying.");
        stopPlayerThread();
    }
    
    public void stopPlayerThread() {
        System.out.println("Bot(" + getName() + ") stopPlayerThread.");
        if (playerThread != null) {
            playerThread.halt();
            playerThread = null;
        }
        int orbNum = botnum;
        orbControl.stopSound(orbNum);
        if (currentPlayer != null) {
            currentPlayer.stop();
            
        }
    }
    class PlayerThread extends Thread {
        public boolean playing = true;
        private Bot bot;

        public PlayerThread(Bot bot) {
            this.bot = bot;
        }

        public synchronized void run() {
            // true the first time. 
            playing = true;
            dbpost(":::::::::::::::: " + bot.getName() + " :::::::::::::::::::::::");
            dbpost(":::::::::::::::: SongTime:" + songTime + " :::::::::::::::::::::::");
            long startTime = System.currentTimeMillis();

            while (playing) {
                Song song = bot.getCurrentSong();
                Layer layer = bot.getCurrentLayer();
                Set set = bot.getCurrentSet();

                Strategy strategy = set.getStrategy();
                Sound sound = strategy.select(set);
                long currentTime = System.currentTimeMillis();
                // later: take into account if it's a SingleTrackSong
                long runningTime = currentTime - startTime;
                dbpost("\n=============== Bpt("+bot.getName() + ") SOUND: " + (sound == null? "null" : sound.getName()) + " RunningTime: " + runningTime + " ===================");
                
                if (songTime > 0 && runningTime > 1000 * songTime) {
                    broadcastGossip("end_song", null);
                    halt();

                } else if (sound == Sound.END_SONG) {
                    broadcastGossip("end_song", null);
                    halt();
                } else if (sound == null) {
                    // ignore. 
                } else {
                    currentSound = sound;
                    //currentPlayer = bot.getPlayer(sound);
                    broadcastGossip("sound_start", sound.getName());
                    // possibly wait for a beat...
                    long waitForBeatTime = currentLayer.getWaitForBeatTime(runningTime);
                    if (waitForBeatTime > 0) {
                        System.out.println("Bot("+bot.getName()+":" + currentLayer.getName() + ") waiting[" + waitForBeatTime + "] (measure length: " + currentLayer.getMeasureLength() + ") for beat lock...");
                        try {
                            Thread.sleep(waitForBeatTime);
                        } catch (Exception ex) {
                        }
                        System.out.println("Bot("+bot.getName()+") DONE waiting for beat lock...");
                    } 

                    int orbNum = botnum;
                    float durationSec = orbControl.playSound(orbNum, sound);
                    try {
                        Thread.sleep((int)(1000 * durationSec));
                    } catch (Exception ex) {
                    }
                    currentSound = null;
                    // might not want to release tre player at every play...
                    //bot.releasePlayer(currentPlayer);
                    currentPlayer = null;
                }
            }
        }
        
        public void halt() {
            System.out.println("Bot.playerthread.halt()");
            playing = false;
            if (currentPlayer != null) {
                currentPlayer.stop();
            }
        }
    }

    public SoundFilePlayer getPlayer(Sound sound) {
        // TODO: figure out player from sound file type.
        //       perhaps a single playere that can handle all of them;
        //       perhaps a pool of a couple players.

        // and set the current player.
        currentPlayer = defaultPlayer;
        return currentPlayer;
    }

    public void releasePlayer(SoundFilePlayer player) {
        currentPlayer = null;
    }
    //////////////////////////////
    ///                        ///
    //////////////////////////////

    public String displayCurrentlyPlaying() {
        StringBuffer buf = new StringBuffer();
        displayCurrentlyPlaying(buf);
        return buf.toString();
    }

    public void displayCurrentlyPlaying(StringBuffer buf) {
        buf.append("Song: ");
        if (currentSong == null) {
            buf.append("null.");
        } else {
            buf.append(currentSong.getName());
            buf.append(" layer: ");
            if (currentLayer == null) {
                buf.append("null.");
            } else {
                buf.append(currentLayer.getName());
                buf.append(" set: ");
                if (currentSet == null) {
                    buf.append("null.");
                } else {
                    buf.append(currentSet.getName());
                }
            }
        }
    }
        
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        toString(buf);
        return(buf.toString());
    }

    public void toString(StringBuffer buf) {
        buf.append("Bot " + getName() + "\n");
        buf.append("\nSongs: [");
        for(Iterator it = songs.iterator(); it.hasNext(); ) {
            Song song = (Song)it.next();
            buf.append(song.getName() + " " );
            //song.toString(buf);
        }
        buf.append("] Current Song: " + (currentSong == null ? "null" : currentSong.getName()));
        buf.append(" Current layer: " + (currentLayer == null ? "null" : currentLayer.getName()));
        buf.append(" Current set: " + (currentSet == null ? "null" : currentSet.getName()));
    }

    public String summarize() {
        StringBuffer buf = new StringBuffer();
        summarize(buf);
        return buf.toString();
    }

    public void summarize(StringBuffer buf) {
        buf.append("Bot " + getName());
        buf.append("Songs: [");
        for(Iterator it = songs.iterator(); it.hasNext(); ) {
            Song song = (Song)it.next();
            buf.append(song.getName() + " " );
        }
        buf.append("] Current Song: " + (currentSong == null ? "null" : currentSong.getName()));
        buf.append(" Current layer: " + (currentLayer == null ? "null" : currentLayer.getName()));
        buf.append(" Current set: " + (currentSet == null ? "null" : currentSet.getName()));
    }

    public static boolean debug = false;
    public static void setDebug(boolean val) {
        debug = val;
    }

    public static void dbpost(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }
    public void dbpostnl(String msg) {
        if (debug) {
            System.out.print(msg);
        }
    }
}
