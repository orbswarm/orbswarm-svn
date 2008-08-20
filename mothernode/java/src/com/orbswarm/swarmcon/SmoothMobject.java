package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;
import static org.trebor.util.ShapeTools.*;
import static java.awt.BasicStroke.*;

// smooth path for printing on display

public class SmoothMobject extends Mobject
{
    private SmoothPath sp;
    private GeneralPath gp;
    private static final Stroke stroke = 
      new BasicStroke(0.5f, CAP_ROUND, JOIN_ROUND); 
    private static final Ellipse2D.Double dot = 
      new Ellipse2D.Double(-.2, -.2, .4, .4);

    public SmoothMobject(SmoothPath sp)
    {
      super(1);
      this.sp = sp;
      this.gp = sp.getContinousePath();
      System.out.println("smooth count: " + sp.size());

//       boolean isFirst = true;
//       for (Waypoint wp: sp)
//       {
//         System.out.println("wp: " + wp);
//         if (isFirst)
//         {
//           gp.moveTo((float)wp.getX(), (float)wp.getY());
//           isFirst = false;
//         }
//         else
//           gp.lineTo((float)wp.getX(), (float)wp.getY());
        
//       }
    }

    // paint this object onto a graphics area
    
    public void paint(Graphics2D g)
    {
      g.setStroke(stroke);
      g.setColor(new Color(255, 128, 64));
      g.draw(gp);
      
      g.setColor(new Color(128, 255, 64));
      for (Waypoint wp: sp)
      {
        AffineTransform t = g.getTransform();
        g.translate(wp.getX(), wp.getY());
        g.fill(dot);
        g.setTransform(t);
      }

    }
}
