package com.orbswarm.swarmcomposer.swarmulator;


/*************************************************************************
 *
 *
 *************************************************************************/

import com.orbswarm.swarmcomposer.util.StdDraw;

import java.awt.Color;

/**
 * @author Simran Gleason
 */
public class Beastie {
    public static final int REBOUND_NONE = 0;
    public static final int REBOUND_SQUARE = 1;
    public static final int REBOUND_CIRCLE = 2;
    public static final int REBOUND_CIRCLE_FAKE = 3;
    public static final int REBOUND_CIRCLE_NOTRAP = 4;

    protected Swarmulator swarmulator;

    public Vect pos = null;      // position
    public Vect v = null;      // velocity
    public double radius;    // size
    public static double swarmFieldRadius; 
    public static double baseRadius = 2400;        // minimum size, world coordinates
    public static double massToSizeFactor = 1.234; // mass affects size times this.

    public static StdDraw drawer;
    
    protected int highlight = 0;
    protected int select = 0;
    protected int playing = 0;
    protected int channel = 0;
    public boolean alive  = true;
    
    protected Color color;            // color
    protected Color highlightColor;  // color
    protected Color labelColor;  // color
    protected Color playingColor;  // color
    protected Color selectColor;  // color
    private static double friction = .99;
    protected double penRadius = .025;
    protected static double minDistance;
    private static double minMass = -1.0;
    private static double maxMass = 0.0;
    protected static double basePenRadius = 0.025;
    protected static int reboundMethod = REBOUND_SQUARE;
    protected boolean constrainMovesToRadius = false;

    private static int next_nth = 0;
    private int nth=0;
    private String auxText = null;

    protected Trajectory trajectory = null;
    
    public Beastie(Vect pos, Vect v, double radius, int numballs) {
        this.pos = pos;
        this.v = v;
        this.radius = radius;
        this.nth = next_nth;
        next_nth++;
        float hue = (float) ((1.0 / (float)numballs) * (float)nth);
        if (hue > 1.f) {
            hue = 1.f;
        }
        color = Color.getHSBColor(hue, .70f, .65f);
        highlightColor = Color.getHSBColor(hue, .8f, .80f);
        playingColor = Color.getHSBColor(hue, .85f, 1.0f);
        selectColor = Color.getHSBColor(hue, 1.0f, .5f);
        labelColor = Color.getHSBColor(hue, .8f, .2f);
    }

    protected Beastie() {
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public void setAuxText(String v) {
        this.auxText = v;
    }
    
    public static void resetDefaults() {
        next_nth = 0;
        baseRadius = 1024.;
    }

    public static void setSwarmFieldRadius(double val) {
        swarmFieldRadius = val;
    }

    public void setTrajectory(Trajectory val) {
        this.trajectory = val;
    }
    public Trajectory getTrajectory() {
        return this.trajectory;
    }
    
    public static void setFriction(double val) {
        friction = val;
    }

    public static void setMinDistance(double val) {
        minDistance = val;
    }

    public static void setReboundMethod(int r) {
        reboundMethod = r;
    }

    public static void setBaseRadius(double r) {
        baseRadius = r;
    }

    public Vect getPos() {
        return this.pos;
    }
    public void setPos(Vect newPos) {
        this.pos = newPos;
    }

    public Vect getVelocity() {
        return this.v;
    }
    public void setVelocity(Vect newV) {
        if (newV != null) {
            this.v = newV;
        }
    }
    
    public void moveStep()  {
        pos.plusEquals(v.times(swarmulator.dt));
    }

    // add reflection. 
    public void moveStep(double dt, double xbound, double ybound) {
        pos.plusEquals(v.times(dt));
        //possiblyRebound(xbound, ybound);
    }

    public void possiblyRebound(double xbound, double ybound) {
        if (reboundMethod == REBOUND_SQUARE) {
            if (Math.abs(pos.x()) > xbound) {
                v.setX(0.0 - v.x());
            }
            if (Math.abs(pos.y()) > ybound) {
                v.setY( 0.0 - v.y());
            }
        } else if (reboundMethod == REBOUND_CIRCLE_FAKE) {
            if (pos.magnitude() >= xbound) {
                // cheat: simply reverse thevector...
                v.timesEquals(-1.0);
            }
        } else if (reboundMethod == REBOUND_CIRCLE) {
            if (pos.magnitude() >= xbound) {
                Vect normal = pos.unit();
                Vect reflection = this.v.reflect(normal);
        
                this.v = reflection;
            }
        } else if (reboundMethod == REBOUND_CIRCLE_NOTRAP) {
            if (pos.magnitude() > xbound) {
                Vect normal = pos.unit();
                Vect reflection = this.v.reflect(normal);
                System.out.println("\n");
        
                this.v = reflection;
                // stop getting trapped outside the rim
                // by repositioning back to the rim. 
                pos = normal.timesEquals(xbound);  
            }
        }
    }

    public void setDrawColor(StdDraw drawer) {
        //System.out.println("BODY:Draw " + nth + " (" + pos.x() + ", " + pos.y() + ") r: " + radius);
        //drawer.setPenRadius(penRadius);
        if (select > 0) {
            drawer.setPenColor(selectColor);
            select -= 1;
        } else if (playing > 0) {
            drawer.setPenColor(playingColor);
            highlight -= 1;
        } else if (highlight > 0) {
            drawer.setPenColor(highlightColor);
            highlight -= 1;
        } else if (highlight == -1) {
            drawer.setPenColor(highlightColor);
        } else {
            drawer.setPenColor(color);
        }
        //drawer.point(pos.x(), pos.y());
        if (playing > 0) {
            playing -= 1;
        }
    }

    public void setLabelColor(StdDraw drawer) {
        drawer.setPenColor(labelColor);
    }


    public void setHighlightColor(StdDraw drawer) {
        drawer.setPenColor(highlightColor);
    }

    public void draw(StdDraw drawer) {
        setDrawColor(drawer);
        drawer.filledCircle(pos.x(), pos.y(), radius);
        setLabelColor(drawer);
        drawer.text_center(pos.x(), pos.y(), "" + nth);
        if (auxText != null) {
            drawer.text_center(pos.x(), pos.y() + radius/2, auxText);
        }
        // then set the color to draw the vector...
        setHighlightColor(drawer);
    }

    public void highlight(int val) {
        highlight = val;
    }

    public int getHighlight() {
        return highlight;
    }

    public void select(int val) {
        select = val;
    }

    public int getSelect() {
        return select;
    }

    // cheating for now. Seeing if x,y is within the square of
    // side radius around the position. 
    public boolean intersects(double x, double y) {
        return (x > pos.x() - radius && x < pos.x() + radius &&
                y > pos.y() - radius && y < pos.y() + radius);
    }
        

    public void moveto(double x, double y) {
        pos.setX(x);
        pos.setY(y);
    }


    public void die() {
        alive = false;
    }
    

    public static boolean randomChance(int chance) {
        double ran = Math.random();
        return (ran * 100. < chance);
    }

    public static double randomRange(double lo, double hi) {
        double delta = hi - lo;
        return lo + delta * Math.random();
    }
    
} 

