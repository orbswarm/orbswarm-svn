package com.orbswarm.swarmcon.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.trebor.util.Angle;
import org.trebor.util.PathTool.PathPoint;

import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.performance.IEvent;
import com.orbswarm.swarmcon.performance.IPerformance;
import com.orbswarm.swarmcon.performance.PositionEvent;
import com.orbswarm.swarmcon.swing.SwarmCon.MouseMobject;

public class PerformanceRenderer extends ARenderer<IPerformance>
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(PerformanceRenderer.class);
  
  public IRenderable getSelected(Point2D selectionPoint, MouseMobject o)
  {
    throw new UnsupportedOperationException();
  }

  public Shape getShape(IPerformance performance)
  {
    GeneralPath totalShape = new GeneralPath();
    for (GeneralPath gp: createDottedPathShapes(performance))
      totalShape.append(gp, false);
    return totalShape;
  }

  public void render(Graphics2D g, IPerformance performance)
  {
    g.setColor(new Color(0, 0, 0, 64));
    g.fill(getShape(performance));
  }
  
  private Collection<GeneralPath> createDottedPathShapes(IPerformance performance)
  {
    Map<IOrb, List<Line2D>> linesByOrb = groupsLinesByOrb(performance);

    // generate paths each set of lines

    List<GeneralPath> paths = new Vector<GeneralPath>();
    for (List<Line2D> lines : linesByOrb.values())
    {
      // great the general path

      GeneralPath gp = new GeneralPath();
      for (ListIterator<Line2D> forward = lines.listIterator(); forward
        .hasNext();)
      {
        Line2D line1 = forward.next();

        if (forward.hasNext())
        {
          Line2D line2 = forward.next();
          gp.moveTo(line1.getX1(), line1.getY1());
          gp.lineTo(line1.getX2(), line1.getY2());
          gp.lineTo(line2.getX2(), line2.getY2());
          gp.lineTo(line2.getX1(), line2.getY1());
          gp.lineTo(line1.getX1(), line1.getY1());
        }
      }
      paths.add(gp);
    }

    return paths;
  }
  
  @SuppressWarnings("unused")
  private Collection<GeneralPath> createSolidPathShapes(IPerformance performance)
  {
    Map<IOrb, List<Line2D>> linesByOrb = groupsLinesByOrb(performance);

    // generate paths each set of lines
    
    List<GeneralPath> paths = new Vector<GeneralPath>();
    for (List<Line2D> lines: linesByOrb.values())
    {
      if (lines.isEmpty())
        break;
      
      // great the general path
      
      GeneralPath gp = new GeneralPath();
      
      // walk forward up one side of the 
      
      ListIterator<Line2D> forward = lines.listIterator();
      Point2D first = forward.next().getP1();
      gp.moveTo(first.getX(), first.getY());
      while (forward.hasNext())
      {
        Point2D p = forward.next().getP1();
        gp.lineTo(p.getX(), p.getY());
      }
      ListIterator<Line2D> backward = lines.listIterator(lines.size() - 1);
      while (backward.hasPrevious())
      {
        Point2D p = backward.previous().getP2();
        gp.lineTo(p.getX(), p.getY());
      }
      gp.closePath();
      paths.add(gp);
    }
    
    
    return paths;
  }

  private Map<IOrb, List<Line2D>> groupsLinesByOrb(IPerformance performance)
  {
    Map<IOrb, List<Line2D>> linesByOrb = new HashMap<IOrb, List<Line2D>>();

    // get a sorted set of events
    
    Vector<IEvent> sortedEvents = new Vector<IEvent>();
    sortedEvents.addAll(performance.getEvents()); 
    Collections.sort(sortedEvents);

    // group events by orb
    
    for (IEvent event : sortedEvents)
      if (event instanceof PositionEvent)
      {
        PositionEvent pe = (PositionEvent)event;
        List<Line2D> lines = linesByOrb.get(pe.getOrb());
        if (null == lines)
          linesByOrb.put(pe.getOrb(), lines = new Vector<Line2D>());
        lines.add(getPositionLine(pe));
      }
    return linesByOrb;
  }
  
  private Line2D getPositionLine(PositionEvent pe)
  {
    double l =
      (RenderingConstants.PATH_WIDTH - RenderingConstants.PATH_WIDTH *
        pe.getVelocity() * 0.8d) / 2;
    PathPoint p = pe.getPosition();
    Point2D p1 =
      p.getAngle().rotate(new Angle(90, Angle.Type.DEGREES)).cartesian(l, p);
    Point2D p2 =
      p.getAngle().rotate(new Angle(270, Angle.Type.DEGREES)).cartesian(l, p);
    return new Line2D.Double(p1, p2);
  }
}
