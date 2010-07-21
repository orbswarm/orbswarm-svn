package com.orbswarm.swarmcon.orb;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;



import static javax.swing.AbstractAction.NAME;

/** You know a button. */

public class Button extends AMobject
{
      /** button text, may contain carriage returns */

      private String text;

      /** font to print button text in */

      private Font font;

      /** action this button should perform */

      private AbstractAction action;

      /** Construct a new button.
       *
       * @param text text to display in button
       */

      public Button(AbstractAction action)
      {
         this(action, null, 0, 0);
      }
      public Button(AbstractAction action, Font font, double x, double y)
      {
        super(1.0d);
         this.action = action;
         this.text = (String)action.getValue(NAME);
         this.font = font;
         setPosition(x, y);
      }
      /** Paint this button.
       *
       * @param g graphics context used to identify button's size
       */
      
      public void paint(Graphics2D g)
      {
         AffineTransform at = g.getTransform();
         g.translate(getX(), getY());

         // if we've got font use that

         if (font != null)
            g.setFont(font);

         // if the shape is not defined, create it

         if (getShape() == null)
            setShape(createButtonShape(g));

         // draw background shape

         g.setColor(isSelected()
                    ? new Color(128, 0, 0, 128)
                    : new Color(0, 128, 0, 128));
         g.fill(getShape());
         g.setColor(Color.BLACK);
         g.drawString(text, 0, 0);
         g.setTransform(at);
      }
      /** Create shape for this button.
       *
       * @param g graphics context used to identify button's size
       * @return created button shape
       */

      Shape createButtonShape(Graphics2D g)
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
         return stroke.createStrokedShape(line);
      }
      /** Indicate that the action is to be performed */

      public void performAction(ActionEvent event)
      {
         action.actionPerformed(event);
      }
      
      public void update(double time)
      {
      }
}
