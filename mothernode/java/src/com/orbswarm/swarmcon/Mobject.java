package com.orbswarm.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;
import static org.trebor.util.ShapeTools.*;

// mobile object

public class Mobject
{
      /** position of mobject in space */

      private Point position = new Point();

      /** has this mobject been selected */

      private boolean selected = false;

      /** children mobjects which are placed relative to this mobject */

      private Mobjects children = new Mobjects();

      /** master alpha value to use for painting this mobject */

      private double masterAlpha = 1;

      /** shape of this mobject used for selection and perhaps display */

      private Shape shape;

      /** Create a mobject.
       * 
       * @param size the typical size of the object use for selection
       * and to compute arangemt of object, here the shape of the
       * object is assumed to be a circle
       */

      public Mobject(double size)
      {
         setShape(new Ellipse2D.Double(-(size / 2), -(size / 2), size, size));
      }
      /** Create a mobject.
       * 
       * @param shape the shape of the object use for selection and
       * to compute arangemt of object
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
      /** Is this mobject selected?
       *
       * @return true if object selected
       */

      public boolean isSelected()
      {
         return selected;
      }
      /** Is the given point (think mouse click point) eligable to
       * select this object?
       *
       * @param clickPoint the point where the mouse was clicked
       */

      public boolean isSelectedBy(Point2D.Double clickPoint)
      {
         return shape.contains(clickPoint.x - getPosition().x, 
                               clickPoint.y - getPosition().y);
      }
      /** Set the selection state of this mobject.
       *
       * @param selected selection state of mobject
       */

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }
      /** Get the size of this mobject.
       *
       * @return the nominal of this mobject
       */

      public double getSize()
      {
         Rectangle2D b = shape.getBounds2D();
         return b.getWidth() > b.getHeight()
            ? b.getWidth()
            : b.getHeight();
      }
      /** Set the shape of this mobject.
       *
       * @param shape the nominal of this mobject
       */

      public void setShape(Shape shape)
      {
         this.shape = shape;
      }
      /** Get the shape of this mobject.
       *
       * @return the nominal of this mobject
       */

      public Shape getShape()
      {
         return shape;
      }
      // positon getter 

      public Point getPosition()
      {
         return new Point(getX(), getY());
      }
      // get x position

      public double getX()
      {
         return position.getX();
      }
      // get y position

      public double getY()
      {
         return position.getY();
      }
      // position setter

      void setPosition(Point2D.Double position)
      {
         setPosition(position.getX(), position.getY());
      }
      // position setter

      void setPosition(double x, double y)
      {
         this.position.setLocation(x, y);
      }
      // set delta position

      void deltaPosition(double dX, double dY)
      {
         setPosition(getX() + dX, getY() + dY);
      }
      // update state of this object

      public void update(double time)
      {
         for (Mobject child: children)
            child.update(time);
      }
      /** Add a child mobject to this mobject.
       *
       * @param child child mobject
       */

      public void addChild(Mobject child)
      {
         children.add(child);
      }
      /** Add a child mobject to this mobject.
       *
       * @param child child mobject
       */
      public void removeChild(Mobject child)
      {
         children.add(child);
      }
      // paint this object onto a graphics area

      public void paint(Graphics2D g)
      {
         for (Mobject child: children)
         {
            AffineTransform oldTransform = g.getTransform();
            g.translate(getX(), getY());
            child.paint(g);
            g.setTransform(oldTransform);
         }
      }
      // paint phantom version of this mobject onto the graphics area

      public void paint(Phantom phantom, Graphics2D g)
      {
         AffineTransform oldTransform = g.getTransform();
         g.translate(
           phantom.getX() - getX(), 
           phantom.getY() - getY());
         paint(g);
         g.setTransform(oldTransform);
      }
      // compute heading to some point

      public double headingTo(Mobject other)
      {
         return headingTo(other.getPosition());
      }
      // compute heading to some point

      public double headingTo(Point2D.Double point)
      {
         double dx = point.getX() - getPosition().getX();
         double dy = point.getY() - getPosition().getY();
         double angle = atan2(dx, dy);
         return toDegrees(angle) + (dx < 0 ? 360 : 0);
      }
      // compute distance to some point

      public double distanceTo(Mobject other)
      {
         return distanceTo(other.getPosition());
      }
      // compute distance to some point

      public double distanceTo(Point2D.Double point)
      {
         return getPosition().distance(point);
      }
      /** Get current master alpha value.
       *
       * @return normalized value (0..1) of master alpha
       */
      
      public double getMasterAlpha()
      {
         return masterAlpha;
      }
      /** Set current master alpha value.
       *
       * @param masterAlpha normalized master alpha value from 0 to 1
       */
      
      public void setMasterAlpha(double masterAlpha)
      {
         assert(masterAlpha >= 0 && masterAlpha <= 1);
         this.masterAlpha = masterAlpha;
      }
      /** Sets the color of a grapics object taking into account the
       * master alpha.
       * 
       * @param g graphics to set color for
       * @param color color to apply alpha to 
       */
      
      public void setColor(Graphics2D g, Color color)
      {
         g.setColor(new Color(
                       color.getRed(),
                       color.getGreen(),
                       color.getBlue(),
                       (int)((color.getAlpha() / 255d) * 
                             masterAlpha * 255)));
      }
}
