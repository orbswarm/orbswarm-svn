package com.orbswarm.choreography.timeline;


import java.awt.Graphics2D;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import org.trebor.util.ShapeTools;
import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.Target;
import com.orbswarm.swarmcon.Point;

public class TimelinePath extends com.orbswarm.swarmcon.Path {
    public boolean active = false;
    public boolean onCall = false;
    public boolean absolute = true;
    public int activeSegment = -1;

    private Shape pathShape;
    private Shape[] segmentShapes;
    private String name = null;
    private Color baseColor, activeColor, onCallColor;
    private double x0;  // x coord of start point
    private double y0;
    private float baseHue;
    
    public TimelinePath() {
    }

    public TimelinePath(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public void setAbsolute(boolean value) {
        this.absolute = value;
    }
    
    public void reshape() {
        pathShape = createPathShape();
        Target first = (Target)this.firstElement();
        x0 = first.getX();
        y0 = first.getY();
        reshapeSegments();
    }

    public void reshapeSegments() {
        boolean first = true;
        int numTargets = this.size();
        segmentShapes = new Shape[numTargets - 1];
        Target prev = new Target(0., 0.);
        int segnum = 0;
        for(Target target: this) {
            if (first) {
                prev = target;
                first = false;
            } else {
                GeneralPath seg = new GeneralPath();
                seg.moveTo((float)prev.getX(), (float)prev.getY());
                seg.lineTo((float)target.getX(), (float)target.getY());
                prev = target;
                segmentShapes[segnum] = seg;
                segnum++;
            }
        }
    }
    
    public void setActive(boolean value) {
        this.active = value;
        if (active) {
            this.onCall = false;
        }
    }

    public void setOnCall(boolean value) {
        this.onCall = value;
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

    public void setBaseHue(float val) {
        baseHue = val;
        resetColors();
    }
    
    public void setBaseHue(int pathNum, int outOf) {
        baseHue = (float)pathNum / (float)outOf;
        resetColors();
    }

    public void resetColors() {
        /* orig numbers
        baseColor = Color.getHSBColor(baseHue, .4f, .9f);
        occupiedColor = Color.getHSBColor(baseHue, .5f, .8f);
        highlightColor = Color.getHSBColor(baseHue, .6f, .7f);
        */
        baseColor = Color.getHSBColor(baseHue, .2f, .95f);
        onCallColor = Color.getHSBColor(baseHue, .4f, .85f);
        activeColor = Color.getHSBColor(baseHue, .6f, .45f);
    }

    public Shape createPathShape() {
        GeneralPath pathShape = new GeneralPath();
        boolean first = true;
        for (Target target: this) {
            if (first) {
                pathShape.moveTo((float)target.getX(), (float)target.getY());
                first = false;
            } else {
                pathShape.lineTo((float)target.getX(), (float)target.getY());
            }
        }
        return pathShape;
    }

    public void paint(Graphics2D g) {
        AffineTransform old = g.getTransform();
        if (active) {
            setColor(g, activeColor);
        } else if (onCall) {
            setColor(g, onCallColor);
        } else {
            setColor(g, baseColor);
        }
        g.setStroke(new BasicStroke(.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        //System.out.println("Region paint. " + getName());

        // TODO: if we have active segments, draw the segments differently
        if (activeSegment == -1) {
            g.draw(pathShape);
        } else {
            for(int i=0; i < segmentShapes.length; i++) {
                if (i == activeSegment) {
                    setColor(g, activeColor);
                } else {
                    setColor(g, onCallColor);
                }
                g.draw(segmentShapes[i]);
            }
        }
        
        // draw path name
        if (name != null) {
            double fontScale = .8;
            g.scale(fontScale, fontScale);
            g.setFont(SwarmCon.ORB_FONT);
            //g.setColor(SwarmCon.TEXT_CLR);
            //drawText(g, -.5, -.5, name);
            drawText(g, x0 / fontScale, y0 / fontScale, name);
        }
         // restore old transform
         g.setTransform(old);
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