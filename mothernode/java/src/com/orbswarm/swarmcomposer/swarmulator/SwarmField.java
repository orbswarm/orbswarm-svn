/*************************************************************************
 * SwarmField
 *  Implements the Main program wrapper of Swarmulator's SwarmField
 *
 *
 *************************************************************************/

package com.orbswarm.swarmcomposer.swarmulator;

import com.orbswarm.swarmcomposer.util.StdDraw;
import com.orbswarm.swarmcomposer.util.TokenReader;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

/**
 * @author Simran Gleason
 */
public class SwarmField implements ActionListener, KeyListener {

    public    Swarmulator swarmulator;
    protected IndraThread indraThread;
    protected boolean fullscreen = false;
    protected boolean decorated = false;
    protected JFrame frame;
    protected Container drawingPane;
    StdDraw   drawer;
    TokenReader reader;

    protected double dt = 75.;
    protected int playTimeLo = 0;
    protected int playTimeHi = 0;
    protected boolean loopPlayList;
    protected boolean shufflePlayList;
    private int initialPause = 0;
    protected int trails = 0;
    protected int saved_trails = 0;
    protected boolean restartWorld =  false;
    
    public SwarmField(int canvasSize) {
        drawer = new StdDraw(canvasSize);
        reader = new TokenReader();
    }

    public void initSwarmulator() {
        swarmulator = new Swarmulator(reader, drawer);
        swarmulator.setRepaintComponent(drawer.getDrawingPane());
    }

    public static JFrame recreateFrame(JFrame frame, SwarmField swarmField, StdDraw drawer, boolean decorated) {
        if (frame != null) {
            frame.setContentPane(new JLabel("hello!"));
            frame.dispose();
        }
        frame = new JFrame();
        swarmField.drawingPane = drawer.getDrawingPane();
        // the frame for drawing to the screen
        frame.setVisible(false);
        frame.setContentPane(swarmField.drawingPane);
        frame.setResizable(decorated);
        frame.setUndecorated(!decorated);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        // frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);      // closes only current window
        frame.setTitle("Swarmulator");
        //JMenuBar mb = createMenuBar();
        //frame.setJMenuBar(mb);
        //mb.setVisible(false);
        return frame;
    }

    public void repaint() {
        frame.repaint();
    }
    
    public void setPlayTimes(int playTimeLo, int playTimeHi) {
        this.playTimeLo = playTimeLo;
        this.playTimeHi = playTimeHi;
        swarmulator.setPlayTimes(playTimeLo, playTimeHi);
    }

    public void setTrails(int trails) {
        this.trails = trails;
        swarmulator.setTrails(trails);
    }

    public void setLoopPlayList(boolean val) {
        this.loopPlayList = val;
    }
    
    public void setShufflePlayList(boolean val) {
        this.shufflePlayList = val;
    }
    
    public void setDt(double dt) {
        this.dt = dt;
    }
    
    public void startSwarming(ArrayList playlist) {
        for(Iterator it = playlist.iterator(); it.hasNext(); ) {
            System.out.println("Playlist: " + (String)it.next());
        }
        if (indraThread == null) {
            indraThread = new IndraThread(this, playlist, dt);
            indraThread.start();
        }
    }

    public void stopSwarming() {
        stopIndraThread();
    }
    
    public void stopIndraThread() {
        if (indraThread != null) {
            indraThread.halt();
        }
    }
    class IndraThread extends Thread {
        public boolean yuga = true;
        protected SwarmField swarmField;
        protected ArrayList playlist;
        protected double dt;

        public IndraThread(SwarmField jk, ArrayList playlist, double dt) {
            this.playlist = playlist;
            this.swarmField = jk;
            this.dt = dt;
        }
        public synchronized void run() {
            // true the first time. 
            yuga = true;
            boolean loop = true;
            while (loop) {
                // next time through, only if loopPlayList is true.
                loop = loopPlayList;
                System.out.println("Playlist Loop start");
                System.out.println("Playlist size: " + playlist.size());
                if (shufflePlayList) {
                    playlist = scramble(playlist);
                }
                Iterator plays = playlist.iterator();
                swarmulator = swarmField.swarmulator;
                String filename = null;
                if (plays.hasNext()) {
                    filename = (String)plays.next();
                }
                while(filename != null && yuga) {
                    System.out.println("\n\n");
                    System.out.println("/////////////////////////////////////////////////////////");
                    System.out.println("////   SwarmField                                    ////");
                    System.out.print("////   " + filename);
                    if (filename.length() <= 46) {
                        for(int i = 0; i < 46 - filename.length(); i++) {
                            System.out.print(' ');
                        }
                        System.out.println("////");
                    } else {
                        System.out.println();
                    }
                    System.out.println("/////////////////////////////////////////////////////////");
            
                    swarmulator.setDt(dt);
                    swarmulator.clearWorld();
                    reader.open(filename);
                    swarmulator.clearBG();
                    drawer.initbg(swarmulator.bgColor);
                    swarmulator.readWorld();
                    if (initialPause > 0) {
                        try { Thread.currentThread().sleep(1000 * initialPause); }
                        catch (InterruptedException e) { System.out.println("Error sleeping"); }
                    }
                    swarmulator.setupDrawing();
                    swarmulator.draw_slowly(swarmulator.creationDelay);

                    swarmulator.startDrawingThread(filename);
                    // wait for it... (might need to be in own thread, like polarball's listenerthread)
                    try { Thread.currentThread().sleep(10000); }
                    catch (InterruptedException e) { System.out.println("Error sleeping"); }
                    System.out.println("SwarmField: waiting for drawing thread.");
                    swarmulator.waitForDrawingThread();
                    System.out.println("SwarmField: done waiting for drawing thread.");
                    reader.close();
                    if (initialPause > 0) {
                        try { Thread.currentThread().sleep(1000 * initialPause / 2); }
                        catch (InterruptedException e) { System.out.println("Error sleeping"); }
                    }
                    if (!restartWorld) {
                        if (plays.hasNext()) {
                            filename = (String)plays.next();
                        } else {
                            filename = null;
                        }
                    }
                    restartWorld =  false;
                }
            }
            System.exit(0);
        }
    
        public void halt() {
            yuga = false;
        }
    }

    public ArrayList scramble(ArrayList list) {
        ArrayList scrambled = new ArrayList();
        int n = list.size();
        if (swarmulator.debugLevel > 0) System.out.println("Scrambling list");
        if (swarmulator.debugLevel > 0) System.out.println("Before: " + list);
        for(int i=0; i < n; i++) {
            int place = (int)(n * Math.random());
            String item = (String)list.get(place);
            while (item == null) {
                place ++;
                if (place >= n) {
                    place = 0;
                }
                item = (String)list.get(place);
            }
            scrambled.add(item);
            list.set(place, null);
        }
        if (swarmulator.debugLevel > 0) System.out.println("After: " + scrambled);
        return scrambled;
    }

    public void playNext() {
        System.out.println("SwarmField: playNext()");
        swarmulator.winkOut();
    }

    public void restartWorld() {
        System.out.println("SwarmField: restartWorld()");
        restartWorld = true;
        playNext();
    }

    public void winkOut() {
        swarmulator.winkOut();
    }

    // create the menu bar
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem("Save...   ");
        menuItem1.addActionListener(this);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);

        JMenuItem menuItemP = new JMenuItem("Pause...   ");
        menuItemP.addActionListener(this);
        menuItemP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemP);

        JMenuItem menuItemR = new JMenuItem("Resume...   ");
        menuItemR.addActionListener(this);
        menuItemR.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemR);

        JMenuItem menuItemN = new JMenuItem("Next...   ");
        menuItemN.addActionListener(this);
        menuItemN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemN);

        JMenuItem menuItemF = new JMenuItem("FullScreen...   ");
        menuItemF.addActionListener(this);
        menuItemF.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemF);

        JMenuItem menuItemQ = new JMenuItem("Quit...   ");
        menuItemQ.addActionListener(this);
        menuItemQ.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                                                        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItemQ);
        return menuBar;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        System.out.println("SWARMFIELD  action command: " + cmd);
        if (cmd.equals("Quit...   ")) {
            System.exit(0);
        } else if (cmd.equals("FullScreen...   ")) {
            toggleFullScreen();
        } else if (cmd.equals("Pause...   ")) {
            System.out.println("We should pause...?");
            swarmulator.pause();
        } else if (cmd.equals("Resume...   ")) {
            System.out.println("We should resume...?");
            swarmulator.resume();
        } else if (cmd.equals("Next...   ")) {
            playNext();
        } else if (cmd.equals("Quit...   ")) {
            System.out.println("We should resume...?");
        } else if (cmd.equals("Save...   ")) {
            saveDrawing();
        }
    }

    public void toggleFullScreen() {
        fullscreen = !fullscreen;
        /* this bit hasn't been working...
           boolean decorated = !fullscreen;
           recreateFrame(drawer, decorated);
        */
        System.out.println("Toggling fullscreen: [" + fullscreen + "]");
        drawer.fullscreen(frame, fullscreen);
    }

    public void toggleTrails() {
        if (trails == 0) {
            if (saved_trails == 0) {
                trails = 25;
            } else {
                trails = saved_trails;
            }
        } else {
            saved_trails = trails;
            trails = 0;
        }
        setTrails(trails);
    }

    public void saveDrawing() {
        FileDialog chooser = new FileDialog(this.frame, "Use a .png or .jpg extension", FileDialog.SAVE);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            drawer.save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }

    public void  keyTyped(KeyEvent e) {
        //lastKeyTyped = e.getKeyChar();
        //  (migrate this stuff from StdDraw)
    }
    public void keyPressed   (KeyEvent e)   { }
    public void keyReleased  (KeyEvent e)   {
        System.out.println("SWARMFIELD got KeyEvent: [" + e.getKeyChar() + "]");
        System.out.println("SWARMFIELD got KeyEvent: " + e);
        char ch = e.getKeyChar();
        if (ch == 'Q') {
            swarmulator.stopDrawingThread();
            stopSwarming();
            System.exit(0);
        } else if (ch == 'X') {
            swarmulator.stopDrawingThread();
            stopSwarming();
        } else if (ch == 'F') {
            toggleFullScreen();
        } else if (ch == 'T') {
            toggleTrails();
        } else if (ch == 'P') {
            swarmulator.togglePause();
        } else if (ch == 'R') {
            restartWorld();
        } else if (ch == 'N') {
            playNext();
        } else if (ch == 'S') {
            swarmulator.stopDrawingThread();
        } else if (ch == 'I') {
            swarmulator.toggleInfoDisplay();
        } else if (ch == 'H' || ch == 'h' || ch == '?') {
            swarmulator.showHelpInfo(getRuntimeKeysHelp());
        } else if (ch == ' ') {
            swarmulator.hideHelpInfo();
        } else if (ch == 'D') {
            int dbl = swarmulator.getDebugLevel();
            dbl ++;
            if (dbl > 1) {
                dbl = 0;
            }
            swarmulator.setDebugLevel(dbl);
        } 
    }

    List runtimeKeysHelp = null;
    public List getRuntimeKeysHelp() {
        if (runtimeKeysHelp == null) {
            List help = new ArrayList();
            help.add("Command Keys:");
            help.add("");
            help.add("  F -- Toggle full screen");
            help.add("  T -- Toggle trails");
            help.add("  P -- Toggle Pause");
            help.add("  D -- Toggle debug info");
            help.add("  R -- Restart world");
            help.add("  N -- Next world");
            help.add("  H, ? -- Show this list");
            help.add("  Q -- Quit");
            help.add("");
            help.add("  Space bar to hide list");
            runtimeKeysHelp = help;
        }
        return runtimeKeysHelp;
    }
    
    public static void readPlaylistFile(String playlistFile, List playlist)  throws IOException {
        TokenReader reader = new TokenReader(playlistFile);
        boolean done = false;
        String prefix = null;
        while (!done) {
            String token = reader.readToken();
            if (token == null) {
                done = true;
            } else if (token.equalsIgnoreCase("prefix")) {
                prefix = reader.readToken();
            } else {
                if (prefix != null) {
                    playlist.add(prefix + token);
                } else {
                    playlist.add(token);
                }
            }
        }
    }

    public static void usage() {
        StringBuffer buf = new StringBuffer();
        usage(buf);
        System.out.println(buf.toString());
    }
    
    public static void usage(StringBuffer buf) {
        buf.append("java swarmField.SwarmField [options]*  [WorldFiles]*\n");
        buf.append("  options:\n");
        buf.append("    --time <seconds>      Playing time for each world\n");
        buf.append("    --timerange <low> <high>  Playing time for each world randomly varies \n");
        buf.append("                          between <low> and <high>\n");
        buf.append("    --pause <seconds>     Initial pause for each world: after drawing\n");
        buf.append("                          and before starting the simulation\n");
        buf.append("    --cycletime <ms>      Minimum time for each increment cycle. \n");
        buf.append("                          If cycles take less time than specified, the \n");
        buf.append("                          simulation thread will sleep for the difference.\n");
        buf.append("    --dt <mtu>            World-coordinate time for each cycle.\n");
        buf.append("                          (<mtu> is 'mythical time units')\n");
        buf.append("    --debug               turn on debugging information (mostly prints \n");
        buf.append("                          notes as they're played)\n");
        buf.append("    --trails <cycles>     Set fading trails to fade over a specified \n");
        buf.append("                          number of cycles.\n");
        buf.append("    --canvas <pixels>     Canvas size, in pixels\n");
        buf.append("    --fullscreen          Start in fullscreen mode, with no frame decorations.\n");
        buf.append("    --playlist <file>     Specify a playlist. \n");
        buf.append("                          The playlist comprises the world files specified\n");
        buf.append("                          in --playlist arguments concatenated with world\n");
        buf.append("                          files specified on the command line, in order.\n");
        buf.append("    --loop                Loop the playlist. \n");
        buf.append("    --shuffle             Randomize the order of the playlist\n");
        buf.append("\n");
        buf.append(" Keyboard commands while simulation is running:\n");
        buf.append("    F  -- Toggle fullscreen mode. If system starts up in fullscreen mode, \n");
        buf.append("          the frame will be undecorated. \n");
        buf.append("    P  -- Toggle Pause mode.\n");
        buf.append("    N  -- Next world. The stars will wink out, the sounds will turn off, and\n");
        buf.append("          the next world in the playlist will start.\n");
        buf.append("    S  -- Stop the world. \n");
        buf.append("          The simulation will stop. But unfortunately, at the moment, \n");
        buf.append("          the sounds will continue annoyingly. \n");
        buf.append("    Q  -- Quit.\n");
        buf.append("    D  -- Toggle debug mode.\n");
        buf.append("    T  -- Toggle trails. Will turn trails on or off. \n");
    }   

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
            System.exit(0);
        }
        int canvasSize = 800;
        SwarmField jk = new SwarmField(canvasSize);
        jk.handleArgs(args);
    }


    public void handleArgs(String[] args) {
        String dtArg = null;
        ArrayList playlist = new ArrayList();
        int i = 0;
        double dt = 75.;
        int cycleTime = 0;
        int playTimeLo = 0;
        int playTimeHi = 0;
        int debugLevel = 0;
        boolean loopPlayList = false; 
        boolean shufflePlayList = false; 
        int initialPause = 0;
        boolean fullscreen = false;
        int canvasSize = 0;
        int trails = 0; 

        ArrayList orbsongs = new ArrayList();
        if (args.length > 0) {
            while (i < args.length) {
                try {
                    if (args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-h")) {
                        usage();
                        System.exit(0);
                    } else if (args[i].equalsIgnoreCase("--time") || args[i].equalsIgnoreCase("--playtime")) {
                        i++;
                        playTimeLo = Integer.parseInt(args[i]);
                        playTimeHi = playTimeLo;
                        i++;
                    } else if (args[i].equalsIgnoreCase("--timerange")) {
                        i++;
                        playTimeLo = Integer.parseInt(args[i]);
                        i++;
                        playTimeHi = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--cycletime")) {
                        i++;
                        cycleTime = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--dt")) {
                        i++;
                        dt = Double.parseDouble(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--trails")) {
                        i++;
                        trails = Integer.parseInt(args[i]);
                        i++;
                    } else if (args[i].equalsIgnoreCase("--debug")) {
                        i++;
                        debugLevel = 1;


                    } else if (args[i].equalsIgnoreCase("--pause")) {
                        i++;
                        initialPause = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--canvas")) {
                        i++;
                        canvasSize = Integer.parseInt(args[i]);
                        i++;

                    } else if (args[i].equalsIgnoreCase("--full") || args[i].equalsIgnoreCase("--fullscreen")) {
                        i++;
                        fullscreen = true;
            

                    } else if (args[i].equalsIgnoreCase("--loop")) {
                        i++;
                        loopPlayList = true;

                    } else if (args[i].equalsIgnoreCase("--shuffle")) {
                        i++;
                        shufflePlayList = true;

                    } else if (args[i].equalsIgnoreCase("--noloop")) {
                        i++;
                        loopPlayList = false;

                    } else if (args[i].equalsIgnoreCase("--playlist")) {
                        i++;
                        String playlistFile = args[i];
                        i++;
                        try {
                            readPlaylistFile(playlistFile, playlist);
                        } catch (Exception ex) {
                            System.out.println("SwarmField caught exception reading playlist file: " + ex);
                        }

                    } else {
                        playlist.add(args[i]);
                        i++;
                    }
                } catch (NumberFormatException ex) {
                    // skip this arg
                    i++;
                }
            }
        }

        this.initialPause = initialPause;
        this.fullscreen = fullscreen;
        this.decorated = !fullscreen;
        this.setLoopPlayList(loopPlayList);
        this.setShufflePlayList(shufflePlayList);

        this.initSwarmulator();
        this.frame = recreateFrame(this.frame, this, this.drawer, this.decorated);
        this.frame.addKeyListener(this);    // JLabel cannot get keyboard focus

        this.frame.setBackground(this.swarmulator.bgColor);


        this.frame.pack();
        if (fullscreen) {
            this.drawer.fullscreen(this.frame, true);
        }

        this.frame.setVisible(true);

        this.swarmulator.setDebugLevel(debugLevel);
        if (cycleTime > 0 ) {
            this.swarmulator.setCycleTime(cycleTime);
        }
        this.setPlayTimes(playTimeLo, playTimeHi);
        this.setTrails(trails);
        this.setDt(dt);

        this.startSwarming(playlist);
    }
}
