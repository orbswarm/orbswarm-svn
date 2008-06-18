package com.orbswarm.choreography.timeline;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import org.trebor.util.ShapeTools;
import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.Point;


/**
 * Representation of a region in the arena which can trigger timeline events
 * when orbs enter or leave.
 */

public class Region  {
    int numOrbs = 6;
    boolean[] occupyingOrbs;
    int numOrbsInside = 0;
    ArrayList enterEvents;
    ArrayList exitEvents;
    ArrayList insideEvents;
    ArrayList []runningInsideEvents;
    
    public double x1 = 0., x2 = 0.;
    public double y1 = 0., y2 = 0.;
    public double width = 0.;
    public double height = 0.;
    Color baseColor, occupiedColor, highlightColor;
    float baseHue = 0.f;
    int highlight = 0;
    private Shape regionShape;
    private String name = null;
    private Timeline timeline;
    
    public Region(int numOrbs, Timeline timeline) {
        this.numOrbs = numOrbs;
        this.timeline = timeline;
        this.runningInsideEvents = new ArrayList[numOrbs];
        this.occupyingOrbs = new boolean[numOrbs];
        for(int i=0; i < numOrbs; i++) {
            this.occupyingOrbs[i] = false;
            this.runningInsideEvents[i] = new ArrayList();
        }
        numOrbsInside = 0;
        enterEvents = new ArrayList();
        exitEvents = new ArrayList();
        insideEvents = new ArrayList();
        resetColors();
    }

    public Shape createRegionShape() {
        //return ShapeTools.normalize(new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1));
        return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String val) {
        this.name = val;
    }
    
    public void setBounds(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.width = Math.abs(x2 - x1);
        this.height = Math.abs(y2 - y1);
        regionShape = createRegionShape();
    }

    public void setBaseHue(int regionNum, int outOf) {
        baseHue = (float)regionNum / (float)outOf;
        resetColors();
    }

    private void resetColors() {
        /* orig numbers
        baseColor = Color.getHSBColor(baseHue, .4f, .9f);
        occupiedColor = Color.getHSBColor(baseHue, .5f, .8f);
        highlightColor = Color.getHSBColor(baseHue, .6f, .7f);
        */
        baseColor = Color.getHSBColor(baseHue, .2f, .95f);
        occupiedColor = Color.getHSBColor(baseHue, .4f, .65f);
        highlightColor = Color.getHSBColor(baseHue, .6f, .45f);

    }
    
    public void addEvent(Event event) {
        // figure out what type of event: ENTER, EXIT, WHILE_INSIDE
        if (!event.isTrigger()) {
            // ignore
        } else {
            String triggerType = event.getTriggerLocation();
            if (triggerType.equalsIgnoreCase("enter")) {
                enterEvents.add(event);
            } else if (triggerType.equalsIgnoreCase("exit")) {
                exitEvents.add(event);
            } else if (triggerType.equalsIgnoreCase("inside")) {
                insideEvents.add(event);
            } else {
                // ignore
            }
        }
    }

    public boolean orbInRegion(int orbId) {
        return occupyingOrbs[orbId];
    }

    public void placeOrb(int orbId, boolean occupying) {
        boolean previouslyOccupied = occupyingOrbs[orbId];
        occupyingOrbs[orbId] = occupying;
        if (occupying != previouslyOccupied) {
            if (occupying) {
                numOrbsInside++;
            } else {
                numOrbsInside--;
            }
        }
    }

    public void highlight(int highlight) {
        this.highlight = highlight;
    }
    
    public void testOrbIntersection(int orbId, double orbx, double orby) {
        if (intersects(orbx, orby)) {
            if (orbInRegion(orbId)) {
                // no change
            } else {
                //System.out.println("ENTER<" + orbId + "> " + getName());
                placeOrb(orbId, true);
                boolean triggered = triggerEnterEvents(orbId);
                if (triggered) {
                    highlight(10); // number of repaint cycles to leave the highlight on. 
                }
            }
        } else {
            if (orbInRegion(orbId)) {
                //System.out.println("EXIT<" + orbId + "> " + getName());
                placeOrb(orbId, false);
                triggerExitEvents(orbId);
            } else {
                // no change
            }
            
        }
    }

    /**
     * trigger all the ENTER and WHILE_INSIDE events.
     * @return true if any events got triggered
     */
    private boolean triggerEnterEvents(int orbId) {
        // trigger all the Enter and WHILE_INSIDE events that match the orbId
        //System.out.println("triggerENTERevents orb: " + orbId);
        boolean triggered = false;
        for(Iterator it = enterEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            //System.out.println("    testing event: " + event.getName() + " trig: " + event.isTrigger() + " triggerLoc: " + event.getTriggerLocation());
            if (event.orbMatches(orbId)) {
                timeline.timelineDisplay.startTriggeredEvent(orbId, event);
                triggered = true;
                // TODO: how do we stop an ongoing event when the timeline stops?
            } else {
                //System.out.println("      DOESN'T match orb.");
            }
        }
        for(Iterator it = insideEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            if (event.orbMatches(orbId)) {
                Event triggeredEvent =
                    timeline.timelineDisplay.startTriggeredEvent(orbId, event);
                triggered = true;
                runningInsideEvents[orbId].add(triggeredEvent);
                // TODO: how do we stop an ongoing event when the timeline stops?
            }
        }
        return triggered;
    }

    /**
     * trigger all the EXIT events, and stop the pending WHILE_INSIDE events.
     * @return true if any events got triggered
     */
    private boolean triggerExitEvents(int orbId) {
        // trigger all the EXIT events, and stop the running INSIDE events.
        //System.out.println("triggerEXITevents orb: " + orbId);
        boolean triggered = false;
        for(Iterator it = exitEvents.iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            if (event.orbMatches(orbId)) {
                timeline.timelineDisplay.startTriggeredEvent(orbId, event);
                triggered = true;
                // TODO: how do we stop an ongoing event when the timeline stops?
            } 
        }
        for(Iterator it = runningInsideEvents[orbId].iterator(); it.hasNext(); ) {
            Event event = (Event)it.next();
            timeline.timelineDisplay.stopEvent(event);
            //System.out.println("Stopping INSIDE event " + event.getName() + " on orb: " + orbId);
        }
        return triggered;
    }

    public boolean intersects(double orbx, double orby) {
        return (orbx >= x1 && orbx <= x2 &&
                orby >= y1 && orby <= y2);
    }

    public void paint(Graphics2D g) {

        // record old transform and make the orb the center of the
        // world
        
        AffineTransform old = g.getTransform();
        //g.translate(x1 + width/2., y1 + height/2.); // from Orb: g.translate(getX(), getY());
        //double scale = Math.max(width, height);
        g.translate(0, 0);
        //double scale = SwarmCon.PIXELS_PER_METER;
        //g.scale(scale, scale);  // or should it be width. height // or max(width, height?)
        
        // draw region shape

        if (highlight > 0) {
            highlight--;
            setColor(g, highlightColor);
        } else if (numOrbsInside > 0) {
            setColor(g, occupiedColor);
        } else {
            setColor(g, baseColor);
        }

        //System.out.println("Region paint. " + getName());
        g.fill(regionShape);
         
        // draw region name

        if (name != null) {
            double fontScale = 2;
            g.scale(fontScale, fontScale);
            g.setFont(SwarmCon.ORB_FONT);
            g.setColor(SwarmCon.TEXT_CLR);
            //drawText(g, -.5, -.5, name);
            drawText(g, x1 / fontScale, y1 / fontScale, name);
        }

         // restore old transform
         
         g.setTransform(old);
      }

    //////////// stolen from Orb...
    
    double masterAlpha = 1.;

    public void setColor(Graphics2D g, Color color)
      {
         g.setColor(new Color(
                       color.getRed(),
                       color.getGreen(),
                       color.getBlue(),
                       (int)((color.getAlpha() / 255d) * 
                             masterAlpha * 255)));
      }
    
      // draw text at a given location

      public void drawText(Graphics2D g, double x, double y, String text)
      {
         drawText(g, new Point(x, y), text);
      }

      // draw text at a given location

      public void drawText(Graphics2D g, Point2D point, String text)
      {
         AffineTransform old = g.getTransform();
         g.setTransform(new AffineTransform());
         g.setFont(g.getFont().deriveFont(
                      old.getScaleInstance(old.getScaleX(),
                                           -old.getScaleY())));

         //(float)(g.getFont().getSize() * old.getScaleX())));
         Point2D n = old.transform(point, new Point2D.Double());
         g.drawString(text, (int)n.getX(), (int)n.getY());
         g.setTransform(old);
      }

}