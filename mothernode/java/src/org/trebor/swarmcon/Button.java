package org.trebor.swarmcon;

import java.awt.*;
import java.awt.geom.*;

import static java.lang.Math.*;

   /** You know a button. */

public class Button extends Mobject
{
         /** button text, may contain carrage returns */

      private String text;

         /** shape of the button */

      private Shape shape;

         /** font to print button text in */

      private Font font;

         /** Construct a new button.
          *
          * @param text text to display in button
          */

      public Button(String text) //, Font font)
      {
         this(text, null, 0, 0);
      }
      public Button(String text, Font font, double x, double y)
      {
         super(0);
         this.text = text;
         this.font = font;
         setPosition(x, y);
      }
         /** Is the given point (think mouse click point) eligable to
          * select this object?
          *
          * @param clickPoint the point where the mouse was clicked
          */

      Color color = new Color(0, 128, 0, 128);
      public boolean isSelectedBy(Point2D.Double clickPoint)
      {
         if (shape != null && shape.contains(
                clickPoint.getX() - getX(), clickPoint.getY() - getY()))
         {
            color = new Color(128, 0, 0, 128);
         }
         return false;
      }
         /** Paint this button. */
      
      public void paint(Graphics2D g)
      {
         AffineTransform at = g.getTransform();
         g.translate(getX(), getY());

            // if we've got font use that

         if (font != null)
            g.setFont(font);

            // if the shape is not defined, create it

         if (shape == null)
         {
            FontMetrics fm = g.getFontMetrics();
            Rectangle2D sBounds = fm.getStringBounds(text, g);
            Shape line = new Line2D.Double(
               0,                  
               sBounds.getY() + sBounds.getHeight() / 2,
               sBounds.getWidth(),  
               sBounds.getY() + sBounds.getHeight() / 2);
            BasicStroke stroke = new BasicStroke(
               (float)sBounds.getHeight(),
               BasicStroke.CAP_ROUND,
               BasicStroke.JOIN_ROUND);
            shape = stroke.createStrokedShape(line);
         }
            // draw background shape

         g.setColor(color);
         g.fill(shape);
         g.setColor(Color.BLACK);
         g.drawString(text, 0, 0);
         g.setTransform(at);
         color = new Color(0, 128, 0, 128);
      }
}
