package com.orbswarm.swarmcon.orb;

import java.awt.*;
import java.awt.geom.*;

import com.orbswarm.swarmcon.path.Point;

import static java.lang.Math.*;
import static org.trebor.util.ShapeTools.*;

// mobile object

public class Mobject implements IMobject
{
      /** position of mobject in space */

      private Point position = new Point();

      /** has this mobject been selected */

      private boolean selected = false;

      /** children mobjects which are placed relative to this mobject */

      private Mobjects children = new Mobjects();

      /** shape of this mobject used for selection and perhaps display */

      private Shape shape;

      /** Create a mobject.
       * 
       * @param size the typical size of the object use for selection
       * and to compute arrangement of object, here the shape of the
       * object is assumed to be a circle
       */

      public Mobject(double size)
      {
         setShape(new Ellipse2D.Double(-(size / 2), -(size / 2), size, size));
      }
      /** Create a mobject.
       * 
       * @param shape the shape of the object use for selection and
       * to compute arrangement of object
       */

      public Mobject(Shape shape)
      {
         if (shape != null)
         {
            double dx = -(shape.getBounds2D().getX() + shape.getBounds2D().getWidth()  / 2);
            double dy = -(shape.getBounds2D().getY() + shape.getBounds2D().getHeight() / 2);
            setShape(translate(shape, dx, dy));
         }
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#isSelected()
       */

      public boolean isSelected()
      {
         return selected;
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#isSelectedBy(java.awt.geom.Point2D.Double)
       */

      public boolean isSelectedBy(Point2D.Double clickPoint)
      {
         return shape.contains(clickPoint.x - getPosition().x, 
                               clickPoint.y - getPosition().y);
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#setSelected(boolean)
       */

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#getSize()
       */

      public double getSize()
      {
         Rectangle2D b = shape.getBounds2D();
         return b.getWidth() > b.getHeight()
            ? b.getWidth()
            : b.getHeight();
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#setShape(java.awt.Shape)
       */

      public void setShape(Shape shape)
      {
         this.shape = shape;
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#getShape()
       */

      public Shape getShape()
      {
         return shape;
      }
      // position getter 

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#getPosition()
       */
      public Point getPosition()
      {
         return new Point(getX(), getY());
      }
      // get x position

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#getX()
       */
      public double getX()
      {
         return position.getX();
      }
      // get y position

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#getY()
       */
      public double getY()
      {
         return position.getY();
      }
      // position setter

      public void setPosition(Point2D.Double position)
      {
         setPosition(position.getX(), position.getY());
      }
      // position setter

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#setPosition(double, double)
       */
      public void setPosition(double x, double y)
      {
         this.position.setLocation(x, y);
      }
      // set delta position

      void deltaPosition(double dX, double dY)
      {
         setPosition(getX() + dX, getY() + dY);
      }
      // update state of this object

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#update(double)
       */
      public void update(double time)
      {
         for (IMobject child: children)
            child.update(time);
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#addChild(com.orbswarm.swarmcon.orb.Mobject)
       */

      public void addChild(Mobject child)
      {
         children.add(child);
      }
      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#removeChild(com.orbswarm.swarmcon.orb.Mobject)
       */
      public void removeChild(Mobject child)
      {
         children.add(child);
      }
      // paint this object onto a graphics area

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#paint(java.awt.Graphics2D)
       */
      public void paint(Graphics2D g)
      {
      }
      // paint phantom version of this mobject onto the graphics area

      // compute heading to some point

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#headingTo(com.orbswarm.swarmcon.orb.IMobject)
       */
      public double headingTo(IMobject other)
      {
         return headingTo(other.getPosition());
      }
      // compute heading to some point

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#headingTo(java.awt.geom.Point2D.Double)
       */
      public double headingTo(Point2D.Double point)
      {
         double dx = point.getX() - getPosition().getX();
         double dy = point.getY() - getPosition().getY();
         double angle = atan2(dx, dy);
         return toDegrees(angle) + (dx < 0 ? 360 : 0);
      }
      // compute distance to some point

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#distanceTo(com.orbswarm.swarmcon.orb.IMobject)
       */
      public double distanceTo(IMobject other)
      {
         return distanceTo(other.getPosition());
      }
      // compute distance to some point

      /* (non-Javadoc)
       * @see com.orbswarm.swarmcon.orb.IMobject#distanceTo(java.awt.geom.Point2D.Double)
       */
      public double distanceTo(Point2D.Double point)
      {
         return getPosition().distance(point);
      }
}
