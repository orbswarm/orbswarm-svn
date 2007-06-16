package org.trebor.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;

   // mobile object

public class Mobject
{
         /** position of mobject in space */

      private Point2D.Double position = new Point2D.Double();

         /** has this mobject been selected */

      private boolean selected = false;

         /** the nominal size of this mobject */

      private double size = 0;

         /** children mobjects which are placed relative to this mobject */

      private Mobjects children = new Mobjects();

         /** master alpha value to use for painting this mobject */

      private double masterAlpha = 1;

         /** Create a mobject.
          * 
          * @param size the typical size of the object use to compute
          * arangemt of object
          */

      public Mobject(double size)
      {
         setSize(size);
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
         return getPosition().distance(clickPoint) < getSize();
      }
         /** Set the selection state of this mobject.
          *
          * @param selected selection state of mobject
          */

      public void setSelected(boolean selected)
      {
         this.selected = selected;
      }
         /** Set the size of this mobject.
          *
          * @param size the nominal of this mobject
          */

      public void setSize(double size)
      {
         this.size = size;
      }
         /** Get the size of this mobject.
          *
          * @return the nominal of this mobject
          */

      public double getSize()
      {
         return size;
      }
         // positon getter 

      Point2D.Double getPosition()
      {
         return new Point2D.Double(getX(), getY());
      }
         // get x position

      double getX()
      {
         return position.getX();
      }
         // get y position

      double getY()
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
         this.position.setLocation(getX() + dX, getY() + dY);
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
         Point2D.Double tmp = position;
         position = phantom.getPosition();
         paint(g);
         position = tmp;
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
         double angle = atan(dy / (dx == 0 ? Double.MIN_VALUE : dx));
         return toDegrees(angle) + (dx > 0 ? 90 : 270);
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
