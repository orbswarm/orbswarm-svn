
/*************************************************************************
 * Swarmulator
 *  Implements the core of the Swarmulator swarm simulator
 *
 *
 *************************************************************************/

package com.orbswarm.swarmcomposer.swarmulator;

import com.orbswarm.choreography.Orb;
import com.orbswarm.choreography.OrbControl;
import com.orbswarm.choreography.Point;
import com.orbswarm.choreography.Specialist;
import com.orbswarm.choreography.Swarm;

import com.orbswarm.swarmcomposer.color.BotColorListener;
import com.orbswarm.swarmcomposer.color.ColorScheme;
import com.orbswarm.swarmcomposer.color.ColorSchemeListener;
import com.orbswarm.swarmcomposer.color.ColorSchemer;
import com.orbswarm.swarmcomposer.color.HSV;

import com.orbswarm.swarmcomposer.composer.Sound;
import com.orbswarm.swarmcomposer.composer.SwarmListener;

import com.orbswarm.swarmcomposer.util.StdDraw;
import com.orbswarm.swarmcomposer.util.TokenReader;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

/**
 * @author Simran Gleason
 */
public class Swarmulator implements MouseListener, MouseMotionListener, ColorSchemeListener, BotColorListener, OrbControl {
    public  double radius;            // radius of universe
    public  int maxBeasties = 25;      // max number of beasties
    private String title = null;
    private int nbeasties;
    private Beastie[] beasties;         // array of beasties

    private HashMap infos;
    private double friction = .98;
    private double minDistance;
    private String background = "radar";

    private int gridCircles = 0;
    private int gridRadii = 0;

    private int cycleTime =  100; // ms;
    public int creationDelay =  200; // ms;
    private int playTimeLo = 0;
    private int playTimeHi = 0;
    private int playCycles = 0;
    public int numCycles = 0;
    public int broadcastOnNthCycles = 10;

    public int debugLevel = 0;
    boolean paused = false;
    boolean displayInfo = false;
    boolean showHelpInfo = false;
    List helpInfo = null;
    
    public Color bgColor, circleColor, circleBorderColor, gridColor, gridColor2;
    public Color dbColor, titleColor, forceColor;
    protected TokenReader reader;
    protected StdDraw drawer;

    protected double dt = 30.;
    protected double local_dt = -1.;
    protected int trails = 0; // approx # of cycles to run trails

    protected Component repaintComponent;

    public Swarmulator(TokenReader reader, StdDraw drawer) {
        init();
        this.reader = reader;
        this.drawer = drawer;
        Beastie.drawer = drawer;
    
        drawer.addMouseListener(this);
        drawer.addMouseMotionListener(this);
    }

    public Swarmulator() {}

    private void init() {
        initBGColors();
        clearInfos();
    }

    public void setupDrawing() {
        drawer.show(true);
        repaint();
        System.out.println("Swarmulator.setUpDrawing -- complete. ");
    }
    
    public void setReboundMethod(int r) {
        Beastie.setReboundMethod(r);
    }

    public void setDt(double val) {
        this.dt = val;
    }
    public double getDt() {
        return this.dt;
    }

    public void setCycleTime(int val) {
        this.cycleTime = val;
    }
    public int getCycleTime() {
        return this.cycleTime;
    }

    public void setPlayTimes(int valLo, int valHi) {
        this.playTimeLo = valLo; // seconds.
        this.playTimeHi = valHi; // seconds.
    }

    public void randomizePlayCycles()  {
        int playTime = randomRange(this.playTimeLo, this.playTimeHi);
        this.playCycles = secondsToCycles(playTime);
    }

    public int secondsToCycles(int secs) {
        // this number 150 was empirically determined. Doesn't actually do real timing.
        if (this.cycleTime > 0)  {
            return (int) ((1000. / this.cycleTime) * secs);
        } else  {
            return 100 * secs;
        }
    }

    public int getDebugLevel() {
        return  this.debugLevel;
    }
    public void setDebugLevel(int val) {
        this.debugLevel = val;
    }

    public int getTrails() {
        return this.trails;
    }
    public void setTrails(int val) {
        this.trails = val;
        System.out.println("Swarmulator: set trails (" + val + ")");
        drawer.setTrails(val);
    }
    
    
    public void resetDefaults() {
        friction = .99;
        Beastie.resetDefaults();
        title = "";
        background = "";
        local_dt = -1.;
    }
    public void clearWorld() {
        resetDefaults();
        Beastie.setFriction(friction);
    }
    public void readWorld() {
        readWorld(reader);

    }
    
    public void readWorld(TokenReader reader) {
        clearInfos();
        try {
            readThings(reader);
            System.out.println("MaxBeasties:" + maxBeasties);
        } catch (Exception ex) {
            System.out.println("Caught exception: " + ex);
            ex.printStackTrace();
        }
    }
    
    public void readThings(TokenReader reader) throws IOException {
        boolean done = false;
        while (!done) {
            String token = reader.readToken();
            if (token == null) {
                done = true;
            } else if (token.startsWith("@")) {
                String key = token.substring(1);
                String line = reader.readLine();
                addInfo(key, line);
            } else if (token.equalsIgnoreCase("stop")) {
                done = true;
            } else if (token.equalsIgnoreCase("background")) {
                background = reader.readToken();
                drawBG(background);
                drawer.initbg(bgColor);
                drawer.show(true);
                repaint();
            } else if (token.equalsIgnoreCase("drawbg")) {
                drawer.initbg(bgColor);
                drawer.show(true);
                repaint();
            } else if (token.equalsIgnoreCase("title")) {
                title = reader.readLine().trim();
        
            } else if (token.equalsIgnoreCase("dt")) {
                local_dt = reader.readDouble();
        
            } else if (token.equalsIgnoreCase("maxbeasties")) {
                maxBeasties = reader.readInt();
                nbeasties = 0;
                beasties = new Beastie[maxBeasties];

            } else if (token.equalsIgnoreCase("radius")) {
                radius = reader.readDouble();
                Beastie.setSwarmFieldRadius(radius);
                drawer.setXscale(-radius, +radius); 
                drawer.setYscale(-radius, +radius);
                minDistance = radius * .01;
                Beastie.setMinDistance(minDistance);

            } else if (token.equalsIgnoreCase("baseradius")) {
                double br = reader.readDouble();
                Beastie.setBaseRadius(br);

            } else if (token.equalsIgnoreCase("friction")) {
                friction = reader.readDouble();
                Beastie.setFriction(friction);
        
            } else if (token.equalsIgnoreCase("gridcircles")) {
                gridCircles = reader.readInt();
                if (debugLevel > 0) {
                    System.out.println("  gridCircles: " + gridCircles);
                }
        
        
            } else if (token.equalsIgnoreCase("gridradii")) {
                gridRadii = reader.readInt();
                System.out.println("  gridRadii: " + gridRadii);

            } else if (token.equalsIgnoreCase("reboundmethod")) {
                String next = reader.readToken();
                if (next.equalsIgnoreCase("square")) {
                    setReboundMethod(Beastie.REBOUND_SQUARE);
                } else if (next.equalsIgnoreCase("circle")) {
                    setReboundMethod(Beastie.REBOUND_CIRCLE);
                } else if (next.equalsIgnoreCase("circlefake")) {
                    setReboundMethod(Beastie.REBOUND_CIRCLE_FAKE);
                } else if (next.equalsIgnoreCase("circlenotrap")) {
                    setReboundMethod(Beastie.REBOUND_CIRCLE_NOTRAP);
                }
                System.out.println("  Rebound Method: " + next);
        
            } else if (token.equalsIgnoreCase("beastie")) {
                System.out.println("READBEASTIE.");
                readBeastie(reader);
        
            } else {
                reader.readLine();
            }
            //System.out.println(" ReadLine. AtEol: " + reader.atEol());
        }
    }


    private Beastie readBeastie(TokenReader reader)  {
        try {
            Vect pos = readCoords(reader);
            Vect v = readCoords(reader);
            double radius = reader.readDouble();
            if (debugLevel > 0) {
                System.out.println("BEASTIE ");
            }
            Beastie beastie =  new Beastie(pos, v, radius, maxBeasties);
            addBeastie(beastie);
            readBeastieMods(reader, beastie);
            return beastie;
        } catch (Exception ex) {
            System.out.println(" Caught exception reading Beastie. " + ex);
            ex.printStackTrace();
            try {
                reader.readLine();
            } catch (Exception exx) {
                System.out.println(" Caught exception reading rest of line. " + ex);
                exx.printStackTrace();
            }
            return null;
        }
    }


    public void addBeastie(Beastie beastie) {
        beastie.swarmulator = this;
        if (nbeasties < maxBeasties) {
            beasties[nbeasties] = beastie;
            nbeasties++;
        }
    }

    public int getNumBeasties() {
        return nbeasties;
    }
    
    public Vect readCoords(TokenReader reader) throws IOException {
        String next = reader.readToken();
        Vect pos;
        if (next.equalsIgnoreCase("polar")) {
            double r = reader.readDouble();
            double theta = reader.readDouble();
            pos = Vect.createPolarDegrees(r, theta);
        } else {
            double rx = Double.parseDouble(next); 
            double ry = reader.readDouble(); 
            pos = new Vect(rx, ry);
        }
        return pos;
    }


    public void readBeastieMods(TokenReader reader, Beastie beastie) throws IOException {
        boolean doneBeastie = false;
        Segments segments = null;
        while (!doneBeastie) {
            String next = reader.readToken();
            if (true || debugLevel > 0) {
                System.out.println("      read mod: " + next + " atEol: " + reader.atEol());
            }
            if (next == null) {
                reader.readLine();
                // ignore..
            } else if (next.equalsIgnoreCase("end")) {
                System.out.println("END BEASTIE.");
                doneBeastie = true;
            } else if (next.equalsIgnoreCase("offset")) {
                Vect off = readCoords(reader);
                beastie.pos.plusEquals(off);

            } else if (next.equalsIgnoreCase("trajectory")) {
                segments = new Segments();
                beastie.setTrajectory(segments);
                System.out.println("Trajectory...");

            } else if (next.equalsIgnoreCase("line")) {
                if (segments == null) {
                    segments = new Segments();
                    beastie.setTrajectory(segments);
                }
                double theta = reader.readDouble();
                double distance = reader.readDouble();
                int timeInSteps = reader.readInt();
                LineSegment seg = new LineSegment(theta, distance/dt, timeInSteps/dt);
                System.out.println("Read line segment: " + seg);
                segments.addSegment(seg);

            } else if (next.equalsIgnoreCase("arc")) {
                if (segments == null) {
                    segments = new Segments();
                    beastie.setTrajectory(segments);
                }
                double theta0 = reader.readDouble();
                double thetaN = reader.readDouble();
                double radius = reader.readDouble();
                int timeInSteps = reader.readInt();
                ArcSegment seg = new ArcSegment(theta0, thetaN, radius, timeInSteps / dt);
                System.out.println("Read arc segment: " + seg);
                segments.addSegment(seg);

            } else {
                if (debugLevel > 0) {
                    System.out.println("    other mod: " + next);
                }
            }
        }
    }
    
    public void clearInfos() {
        infos = new HashMap();
    }

    public void addInfo(String key, String line) {
        List info = (List)infos.get(key);
        if (info == null) {
            info = new ArrayList();
        }
        info.add(line);
        infos.put(key, info);
    }

    public List getInfo(String key) {
        return (List)infos.get(key);
    }
    public Iterator getInfoKeysIterator() {
        // later: sort the keys?
        return infos.keySet().iterator();
    }

    public void swarmStep(double dt) {
        swarmStep_boids(dt);
    }
    
    public void swarmStep_adhoc(double dt) {
        Vect centerOfMass = new Vect(0., 0.);
        Vect flockVelocity = new Vect(0., 0.);
        for(int i=0; i < nbeasties; i++) {
            Beastie b = beasties[i];
            centerOfMass.plusEquals(b.pos);
            flockVelocity.plusEquals(b.v);
        }
        //System.out.println("CenterOfMass(B4):   " + centerOfMass);
        centerOfMass.timesEquals(1.0 / (double)nbeasties);
        System.out.println("CenterOfMass(NORM): " + centerOfMass);
        drawer.setPenColor(titleColor);
        drawVect(drawer, 0., 0., centerOfMass, 1);

        double flockVelocityTheta = flockVelocity.theta();
        Vect drawableFlockVelocity = Vect.createPolar(30000, flockVelocityTheta);
        drawer.setPenColor(dbColor);
        drawVect(drawer, 0., 0., drawableFlockVelocity, 1);

        for (int i = 0; i < nbeasties; i++) {
            Beastie beastieI = beasties[i];
            if (beastieI != null && beastieI.alive)  {
                Trajectory trajectory = beastieI.getTrajectory();
                //System.out.println("beastie(" + i + "). v: " + beastieI.getVelocity() + " trajectory: " + trajectory + " hasNext: " + (trajectory != null ? trajectory.hasNextStep():""));
                Vect newV;
                if (trajectory != null && trajectory.hasNextStep()) {
                    Vect trajectoryV = trajectory.nextStep();
                    newV = trajectoryV;
                } else {
                    newV = beastieI.getVelocity();
                }
                // if beastie gets outside the world, strongly point it towards the center
                double diff;
                double percent;
                if (beastieI.pos.r() > radius) {
                    percent = .8;
                    newV.thetaPlusEquals(percent * (Math.PI - beastieI.pos.theta()));
                } else {
                    // tend towards center of mass:
                    Vect toCenterOfMass = centerOfMass.minus(beastieI.pos);
                    //drawVect(drawer, beastieI.pos.x, beastieI.pos.y, toCenterOfMass, 1.);
                    double thetaToCenter = toCenterOfMass.theta();
                    double vTheta = beastieI.v.theta();
                    diff = thetaToCenter - vTheta;
                    percent = .2;
                    newV.thetaPlusEquals(diff * percent);
            
                    // tend towards average velocity direction
            
                    double newVTheta = newV.theta();
                    diff = flockVelocityTheta - newVTheta;
                    percent = .05;
                    newV.thetaPlusEquals(diff * percent);
                }

                // if anybody's too close, move away from it.
                percent = .4;
                for(int j=0; j < i; j++) {
                    Beastie beastieJ = beasties[j];
                    if (i != j && beastieJ != null && beastieJ.alive)  {
                        Vect dist = beastieI.pos.minus(beastieJ.pos);
                        if (dist.magnitude() < 5. * beastieI.radius) {
                            beastieI.v.thetaPlusEquals(percent*(Math.PI - dist.theta()));
                            beastieJ.v.setTheta(percent* (dist.theta() - Math.PI));
                        }
                    }
                }
                
                beastieI.setVelocity(newV);
                // then do the swarming algorithms and weight them against the trajectory.
                //for (int j = 0; j < nbeasties; j++) {
                //Beastie beastieJ = beasties[j];
                //if (beastieJ != null && beastieJ.alive) {
                //  if (i != j) {
                //    
                //  }
                //}
            }
        }
    
        for(int i = 0; i < nbeasties; i++) {
            Beastie beastie = beasties[i];
            if (beastie != null && beastie.alive) {
                beastie.moveStep(dt, radius, radius);
            }
        }

    }

    public void swarmStep_boids(double dt) {
        //System.out.println("CenterOfMass(NORM): " + centerOfMass);
        //drawer.setPenColor(titleColor);
        //drawVect(drawer, 0., 0., centerOfMass, 1);

        double localityThreshold = radius / 4.;
        for (int i = 0; i < nbeasties; i++) {
            Beastie beastieI = beasties[i];
            if (beastieI != null && beastieI.alive)  {
                Trajectory trajectory = beastieI.getTrajectory();
                //System.out.println("beastie(" + i + "). v: " + beastieI.getVelocity() + " trajectory: " + trajectory + " hasNext: " + (trajectory != null ? trajectory.hasNextStep():""));
                Vect newV;
                if (trajectory != null && trajectory.hasNextStep()) {
                    Vect trajectoryV = trajectory.nextStep();
                    newV = trajectoryV;
                } else {
                    newV = beastieI.getVelocity();
                }
                // if beastie gets outside the world, strongly point it towards the center
                double diff;
                double percent;
                if (beastieI.pos.r() > radius) {
                    percent = .8;
                    newV.thetaPlusEquals(percent * (Math.PI - beastieI.pos.theta()));
                    // if beastie is waaaay out of the world, put back in the center. 
                    if (beastieI.pos.r() > 2 * radius) {
                        beastieI.pos = new Vect(0., 0.);
                    }                    
                } else {
                    // steer towards the center of mass of local objects
                    Vect localCenterOfMass = new Vect(0., 0.);
                    Vect localVelocity = new Vect(0., 0.);
                    int numLocal = 0;
                    for(int j=0; j < nbeasties; j++) {
                        Beastie jb = beasties[j];
                        if (j != i && jb != null && jb.alive) {
                            Vect distV = beastieI.pos.minus(jb.pos);
                            double dist = distV.magnitude();
                            if (dist < localityThreshold) {
                                numLocal ++;
                                localCenterOfMass.plusEquals(jb.pos);
                                localVelocity.plusEquals(jb.v);
                                if (dist < 5. * beastieI.radius) {
                                    percent = .1;
                                    beastieI.v.thetaPlusEquals(percent*(Math.PI - distV.theta()));
                                    //beastieJ.v.setThetaPlusEquals(percent* (dist.theta() - Math.PI));
                                }
                            }
                        }
                    }
                    //System.out.println("CenterOfMass(B4):   " + localCenterOfMass);
                    localCenterOfMass.timesEquals(1.0 / (double)numLocal);
                    localVelocity.timesEquals(1.0 / (double)numLocal);

                    // tend towards center of mass:
                    if (numLocal > 0) {
                        Vect toCenterOfMass = localCenterOfMass.minus(beastieI.pos);
                        //drawVect(drawer, beastieI.pos.x, beastieI.pos.y, toCenterOfMass, 1.);
                        double thetaToCenter = toCenterOfMass.theta();
                        double vTheta = beastieI.v.theta();
                        diff = thetaToCenter - vTheta;
                        percent = .07;
                        newV.thetaPlusEquals(diff * percent);
            
                        // tend towards average velocity direction
                        double localVelocityTheta = localVelocity.theta();
                        //Vect drawableFlockVelocity = Vect.createPolar(30000, flockVelocityTheta);
                        //drawer.setPenColor(dbColor);
                        //drawVect(drawer, 0., 0., drawableFlockVelocity, 1);
            
                        double newVTheta = newV.theta();
                        diff = localVelocityTheta - newVTheta;
                        percent = .05;
                        newV.thetaPlusEquals(diff * percent);
                    }
                }

                /*
                // if anybody's too close, move away from it.
                percent = .6;
                for(int j=0; j < i; j++) {
                Beastie beastieJ = beasties[j];
                if (i != j && beastieJ != null && beastieJ.alive)  {
                Vect dist = beastieI.pos.minus(beastieJ.pos);
                if (dist.magnitude() < 5. * beastieI.radius) {
                beastieI.v.thetaPlusEquals(percent*(Math.PI - dist.theta()));
                beastieJ.v.setTheta(percent* (dist.theta() - Math.PI));
                }
                }
                }
                */


                beastieI.setVelocity(newV);
                // then do the swarming algorithms and weight them against the trajectory.
                //for (int j = 0; j < nbeasties; j++) {
                //Beastie beastieJ = beasties[j];
                //if (beastieJ != null && beastieJ.alive) {
                //  if (i != j) {
                //    
                //  }
                //}
            }
        }
    
        for(int i = 0; i < nbeasties; i++) {
            Beastie beastie = beasties[i];
            if (beastie != null && beastie.alive) {
                beastie.moveStep(dt, radius, radius);
            }
        }

    }

    // draw the N beasties

    public void draw() {
        if (displayInfo) {
            drawDisplayInfo();
        }

        if (showHelpInfo && helpInfo != null) {
            drawHelpInfo();
        }

        for (int i = 0; i < nbeasties; i++) {
            Beastie beastie = beasties[i];
            if (beastie != null && beastie.alive) {
                beastie.draw(drawer);
                drawVect(drawer, beastie.pos.x, beastie.pos.y, beastie.v, 100.0);
            }
        }
    }


    public void drawVect(StdDraw drawer, double x, double y, Vect vect, double factor) {
        double xend = x + (vect.x) * factor;
        double yend = y + (vect.y) * factor;
        drawer.line(x, y, xend, yend);
        drawer.circle(xend, yend, 150.);
    }

    public void drawDisplayInfo() {
        drawer.set_text_point(-.98 * radius, .97*radius);
        drawer.next_line(); // go down one from the title. 
        drawer.setPenColor(titleColor);
        for(Iterator it = getInfoKeysIterator(); it.hasNext(); ) {
            String key = (String)it.next();
            List lines = getInfo(key);
            drawer.text_line(key);
            drawer.next_line();
            for(Iterator lit = lines.iterator(); lit.hasNext(); ) {
                String line = (String)lit.next();
                drawer.text_line("   " + line);
                drawer.next_line();
            }
        }
    }

    public void drawHelpInfo() {
        drawer.set_text_point(-.98 * radius, 0.);
        drawer.setPenColor(titleColor);
        for(Iterator it = helpInfo.iterator(); it.hasNext(); ) {
            String line = (String)it.next();
            drawer.text_line("   " + line);
            drawer.next_line();
        }
    }


    
    public void draw_slowly(int delay) {
        for (int i = 0; i < nbeasties; i++) {
            Beastie beastie = beasties[i];
            if (beastie != null) {
                beastie.draw(drawer);
                drawer.show(delay);
                repaint();
            }
        }
    } 


    public void initBGColors() {
        bgColor = Color.getHSBColor(.8f, .2f, .1f);
        circleBorderColor = Color.getHSBColor(.7f, .35f, .2f);
        circleColor = Color.getHSBColor(.01234f, .25f, .10f);
        gridColor = Color.getHSBColor(.74f, 0.4f, .5f);
        gridColor2 = Color.getHSBColor(.95f, 0.5f, .3f);
        titleColor = Color.getHSBColor(.74f, 0.4f, .5f);
        forceColor = Color.getHSBColor(.74f, 0.5f, .3f);
        dbColor = Color.getHSBColor(0.f, 0.8f, 1.0f);
    }

    public void clearBG() {
        drawBG_blank();
    }

    public void drawBG(String background) {
        if (background.equals("radar")) {
            drawBG_radar();
        } else if (background.equals("square")) {
            drawBG_square();
        } else if (background.equals("circle")) {
            drawBG_circle();

        } else if (background.equals("stars")) {
            drawBG_stars();

        } else if (background.equals("starclusters") || background.equals("starclusters_circle")) {
            drawStarClusters(50, 55, 35000.);

        } else if (background.equals("starclusters_square")) {
            drawStarClusters_square(45, 55, 35000.);

        } else if (background.equals("none") || background.equals("blank") || background.equals("clear")) {
            drawBG_blank();
        } else {
            drawBG_radar();
        }
        if (title != null) {
            drawer.setPenColor_bg(titleColor);
            drawer.text_bg(-.98 * radius, .97*radius, title);
        }
    }
    public void drawBG_radar() {
        drawBG_circle();
        drawgrid();
    }

    public void drawBG_stars() {
        drawStars();
    }

    public void drawBG_blank() {
        drawer.setPenColor_bg(bgColor);
        drawer.filledSquare_bg(0.0, 0.0, radius * 1.15);
    }

    public void drawBG_circle() {
        drawer.setPenColor_bg(circleColor);
        drawer.filledCircle_bg(0.0, 0.0, radius * 1.02);
        drawer.setPenColor_bg(circleBorderColor);
        drawer.setPenRadius_bg(.005);
        drawer.circle_bg(0.0, 0.0, radius);
    }

    public void drawBG_square() {
        drawer.setPenColor_bg(circleColor);
        drawer.filledSquare_bg(0.0, 0.0, radius * 1.05);
        drawer.setPenRadius_bg(.015);
        drawer.setPenColor_bg(circleBorderColor);
        drawer.square_bg(0.0, 0.0, radius * 1.05);
    }

    public void drawStars() {
        int numStars = (int)randomRange(175., 550.);
        for(int i=0; i < numStars; i++) {
            double x = randomRange(-1. * radius, radius);
            double y = randomRange(-1. * radius, radius);
            drawRandomStar(x, y, 40., 250.);
        }
    }
    
    public void drawStarClusters(int clusters, int starsPer, double clusterSizeRange) {
        int numClusters = randomRange(5, clusters);
        for(int i=0; i < numClusters; i++) {
            int perCluster = randomRange(starsPer / 4, starsPer);
            double clusterSize = randomRange(0., clusterSizeRange);
            double r = randomRange(clusterSize, radius);
            double theta = randomRange(0., 360.);
            Vect offset = Vect.createPolarDegrees(r, theta);
        
            drawStarCluster(offset, clusterSize, perCluster);
        }
    }

    public void drawStarClusters_square(int clusters, int starsPer, double clusterSizeRange) {
        int numClusters = randomRange(5, clusters);
        for(int i=0; i < numClusters; i++) {
            int perCluster = randomRange(starsPer / 4, starsPer);
            double clusterSize = randomRange(0., clusterSizeRange);
            double x = randomRange(-1. * radius, radius);
            double y = randomRange(-1. * radius, radius);
        
            Vect offset = new Vect(x, y);
            drawStarCluster(offset, clusterSize, perCluster);
        }
    }

    public void drawStarCluster(Vect offset, double clusterSize, int stars) {
        for(int i=0; i < stars; i++) {
            double rStar = randomRange(0., clusterSize);
            double thetaStar = randomRange(0., 360.);
            Vect starVect = Vect.createPolar(rStar, thetaStar);
            starVect.plusEquals(offset);
            double x = starVect.x();
            double y = starVect.y();
            drawRandomStar(x, y, 20., 200.);
        }
    }
    public void drawRandomStar(double x, double y, double weightLo, double weightHi) {
        double weight = randomRange(weightLo, weightHi);
        float brite = randomRange(0.f, .7f);
        float sat = randomRange(0.f, .25f);
        float hue = randomRange(0.f, 1.f);
        Color starColor = Color.getHSBColor(hue, sat, brite);
        drawer.setPenColor_bg(starColor);
        drawer.filledCircle_bg(x, y, weight);
    }
    
    public void drawgrid() {
        drawer.setPenColor_bg(gridColor);
        drawer.setPenRadius_bg(.001);
        for(int i=0; i < gridCircles; i++) {
            drawer.circle_bg(0.0, 0.0, (double)i * radius / gridCircles);
        }
        for(int j = 0; j < gridRadii; j++) {
            double thetaR = j * 2. * Math.PI / gridRadii;
            Vect rad = Vect.createPolar(radius, thetaR);
            drawer.line_bg(0., 0., rad.x(), rad.y());
        }
        /*
          int gridRadii2 = 16;
          drawer.setPenColor_bg(gridColor2);
          for(int j = 0; j < gridRadii2; j++) {
          double thetaD = (double)j * 360. / gridRadii2;
          Vect rad = Vect.createPolarDegrees(radius, thetaD);
          drawer.line_bg(0., 0., rad.x(), rad.y());
          }
        */
    }

    public void mousePressed (MouseEvent e) {
        System.out.println("Bing!");
        double mx = drawer.mouseX();
        double my = drawer.mouseY();
    }

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e) { }
    public void mouseExited  (MouseEvent e) { }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseMoved(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) {
        if (drawer.mousePressed() ) {
            double mx = drawer.mouseX();
            double my = drawer.mouseY();
        }
    }    

    public Beastie findBeastie(double x, double y) {
        for(int i=0; i < nbeasties; i++) {
            Beastie beastie = beasties[i];
            if (beastie.intersects(x, y)) {
                return beastie;
            }
        }
        return null;
    }


    public void cycle(double dt) {
        //System.out.print(". ");
        drawer.clearbg(bgColor);
        swarmStep(dt); 
        draw(); 
        drawer.show(true);
        repaint();
    }

    ////////////////////////
    //// Drawing Thread ////
    ////////////////////////

    protected DrawingThread drawingThread = null;
    public void startDrawingThread(String filename) {
        System.out.println("start drawing thread(" + filename + "). drawingThread: " + drawingThread);
        if (drawingThread == null) {
            drawingThread = new DrawingThread(this);
            System.out.println("created drawing thread. ");
        }
        drawingThread.worldfile = filename;
        System.out.println("starting drawing thread: " + drawingThread);
        drawingThread.start();
        System.out.println("started drawing thread: " + drawingThread);
    }

    public void stopDrawingThread() {
        System.out.println("Swarmulator: stop drawing thread");
        if (drawingThread != null) {
            drawingThread.halt();
            drawingThread = null;
        }
    }

    class DrawingThread extends Thread {
        public boolean drawing = false;
        protected Swarmulator swarmulator;
        public String worldfile;
        public DrawingThread(Swarmulator swarmulator) {
            this.swarmulator = swarmulator;
        }
        public synchronized void run() {
            try {
                System.out.println("DrawingThread.run. worldfioel: " + worldfile);
                drawing = true;
                paused = false;
                numCycles = 0;
                randomizePlayCycles();
                System.out.println("SWARMULATOR. playTimeRange: {" + playTimeLo + ", " + playTimeHi + "} playCycles: " + playCycles);
                double dt = swarmulator.dt;
                if (swarmulator.local_dt > 0.) {
                    dt = swarmulator.local_dt;
                }
                while (drawing) {
                    if (playCycles > 0 && numCycles >= playCycles && !winkingOut) {
                        numCycles = 0;
                        winkOut();
                    } else if (!paused) {
                        long beforeCycleMs = System.currentTimeMillis();
                        swarmulator.cycle(dt);
                        numCycles ++;
                        if (numCycles % broadcastOnNthCycles == 0) {
                            swarmulator.broadcastDistances();
                        }
                        long afterCycleMs = System.currentTimeMillis();
                        long actualCycleTime = afterCycleMs - beforeCycleMs;
                        if (swarmulator.cycleTime > 0 && actualCycleTime < swarmulator.cycleTime) {
                            //System.out.print("(" + actualCycleTime + "#" + swarmulator.cycleTime + ") ");
                            try { Thread.currentThread().sleep(swarmulator.cycleTime - actualCycleTime); }
                            catch (InterruptedException e) { System.out.println("Error sleeping"); }
                        } else {
                            //System.out.print("(" + actualCycleTime + "|" + swarmulator.cycleTime + ") ");
                        }
                        //if (numCycles % 10 == 0) {
                        //    System.out.println();
                        //}
                    } else {
                        try { Thread.currentThread().sleep(400); }
                        catch (InterruptedException e) { System.out.println("Error sleeping"); }
                    }           
                }
            } catch (Exception ex) {
                System.err.println("Drawing Thread caught exception: " + ex);
                ex.printStackTrace(System.err);
            }
        }

        public  void halt() {
            System.out.println("\n\n\n======SwarmulatorDrawingTHread   HALT!!!\n\n\n");
            drawing = false;
        }
    }

    public void togglePause() {
        paused = !paused;
        if (paused) {
            //mute();
        }
    }

    public void toggleInfoDisplay() {
        displayInfo = !displayInfo;
    }

    public void showHelpInfo(List helpInfo) {
        this.helpInfo = helpInfo;
        this.showHelpInfo = true;
    }

    public void hideHelpInfo() {
        this.showHelpInfo = false;
    }

    public void pause() {
        paused = true;
    }
    public void resume() {
        paused=false;
    }

    public void waitForDrawingThread() {
        if (drawingThread != null && drawingThread.drawing) {
            try {
                drawingThread.join();
                drawingThread = null;
                System.out.println("\n\n GODOT arrived at the drawing thread. \n\n");
            } catch (Exception ex) {
                System.out.println("Swarmulator.gotException waiting for drawing thread. ");
                ex.printStackTrace();
            }
        }
    }

    private boolean winkingOut = false;
    public void winkOut() {
        Thread winkingOutThread = new WinkingOutThread(this);
        winkingOutThread.start();
    }

    class WinkingOutThread extends Thread {
        private Swarmulator swarmulator;
        public WinkingOutThread(Swarmulator swarmulator) {
            this.swarmulator = swarmulator;
        }
        public synchronized void run() {
            swarmulator.winkOut(400, true);
        }
    }

    public void winkOut(int stepTime, boolean stopDrawingThreadAfterwards) {
        winkingOut = true;
        for(int i=0; i < nbeasties; i++) {
            if (beasties[i].alive) {
                beasties[i] = null;
                delay(randomRange(stepTime, 2 * stepTime));
            }
        }

        if (stopDrawingThreadAfterwards) {
            stopDrawingThread();
        }
        winkingOut = false;
    }

    
    public static int randomRange(int low, int hi) {
        return low + (int)(Math.random() * (double)(hi - low));
    }

    public static double randomRange(double low, double hi) {
        return low + Math.random() * (hi - low);
    }

    public static float randomRange(float low, float hi) {
        return low + (float)Math.random() * (hi - low);
    }


    public void delay(int t) {
        try { Thread.currentThread().sleep(t); }
        catch (InterruptedException e) { System.out.println("Error sleeping"); }
    }

    public void repaint() {
        repaintComponent.repaint();
    }

    public void setRepaintComponent(Component ear) {
        this.repaintComponent = ear;
    }

    //
    // broadcast distances.
    //

    // distances are calculated as percent of double the radius. 
    public double[][] calculateDistances() {
        double diameter = radius * 2.;
        double[][] distances = new double[nbeasties][nbeasties];
        for(int i=0; i < nbeasties; i++) {
            Beastie beastieI = beasties[i];
            for(int j=0; j <= i; j++) {
                if (i == j) {
                    distances[i][j] = 0.;
                } else {
                    Beastie beastieJ = beasties[j];
                    Vect dist = beastieI.pos.minus(beastieJ.pos);
                    double distance = dist.magnitude();
                    double distPercent = 100. * distance / diameter;
                    distances[i][j] = distPercent;
                    distances[j][i] = distPercent;
                }
            }
        }
        return distances;
    }

    public void broadcastDistances() {
        double [][] distances = calculateDistances();  // optimize: reuse the array.
        //printDistances(distances);
        for(Iterator it = swarmListeners.iterator(); it.hasNext(); ) {
            SwarmListener sl = (SwarmListener)it.next();
            sl.updateSwarmDistances(radius, nbeasties, distances);
        }
        System.out.println("\nSwarmulator: broadcast distances. specialists: " + specialists.size());
        printDistances(distances);
        if (specialists != null && specialists.size() > 0) {
            Swarm swarm = createSwarm(radius, nbeasties, distances);
            for(Iterator it=specialists.iterator(); it.hasNext(); ) {
                Specialist sp = (Specialist)it.next();
                sp.orbState(swarm);
            }
        }
    }

    public Swarm createSwarm(double radius, int nbeasties, double[][] distances) {
        Swarm swarmImpl = new SwarmulatorSwarmImpl(nbeasties);
        for(int i=0; i < nbeasties; i++) {
            SwarmulatorOrbImpl orb = (SwarmulatorOrbImpl)swarmImpl.getOrb(i);
            orb.setDistances(distances[i]);
        }
        return swarmImpl;
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

    private List swarmListeners = new ArrayList();
    public void addSwarmListener(SwarmListener ear) {
        swarmListeners.add(ear);
    }

    private List specialists = new ArrayList();
    public void addSpecialist(Specialist sp) {
        specialists.add(sp);
    }


      //////////////////////////////////////
     /// ColorSchemeListener            ///
    //////////////////////////////////////

    public void colorSchemeChanged(ColorScheme colorScheme) {
        int numColors = 6;
        for(int i=0; i < nbeasties; i++) {
            HSV hsv = colorScheme.getColor(i % numColors);
            beasties[i].setColor(hsv.toColor());
        }
    }

    public void newColorScheme(ColorScheme cs) {
        // ignore;
    }

    public void botColorChanged(int bot, int swatch, HSV hsv) {
        beasties[bot].setColor(hsv.toColor());
        beasties[bot].setAuxText("<" + swatch + ">");
    }
 
    ////////////////////////////////
    /// Swarmulator Orb Control  /// 
    ////////////////////////////////
    public OrbControl getOrbControl() {
        return this;
    }

    // sound control methods not implemented.
    public float playSoundFile(int orb, String soundFilePath) {return -1.f;}
    public float playSound(int orb, Sound sound) {return -1.f;}
    public void stopSound(int orb) {}
    public void volume(int orb, int volume) {}
    public Sound lookupSound(String soundFilePath) {return null;}

    // only one Light control method implemented
    public void orbColor(int orb, HSV hsv, int time) {
        System.out.println("Swarmulator: orbColor(orb: " + orb + ") HSV: " + hsv);
        // time ignored here. 
        beasties[orb].setColor(hsv.toColor());
    }
    
    public void orbColorFade(int orbNum,
                             HSV color1, HSV color2,
                             int time) {}

    public HSV getOrbColor(int orbNum) {
        return null;
    }
    
    //
    // Motion methods
    //
    public void followPath(Point[] wayPoints) {}
    public void stopOrb(int orb) {}
    
    //
    // SoundFile -> sound hash mapping.
    //
    public void   addSoundFileMapping(String soundFilePath, String soundFileHash) {}
    public String getSoundFileHash(String soundFilePath) {return null;}
    public List   getSoundFileMappingKeys() {return null;}

    
}

