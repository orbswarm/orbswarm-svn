package com.orbswarm.swarmcon.path;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.orbswarm.swarmcon.model.Rate;

public class Tests
{

  /** Test path code. */

  public static void main(String[] args)
  {
    smoothPathTest();
  }

  @SuppressWarnings("serial")
  public static void smoothPathTest()
  {
    final int count = 5;
    final double smoothness = 120;

    final Rate rate = new Rate("test", 0, 30, 1);
    final java.util.Random rnd = new java.util.Random();

    JFrame frame = new JFrame();
    JPanel panel = new JPanel()
    {
      public void paint(java.awt.Graphics graphics)
      {
        java.awt.Graphics2D g = (java.awt.Graphics2D)graphics;
        g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
          java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Rectangle clip = g.getClipBounds();
        g.setColor(java.awt.Color.WHITE);
        g.fill(clip);

        Path path = new Path();
        for (int i = 0; i < count; ++i)
          path.add(new Target((double)rnd.nextInt(clip.width / 2) +
            clip.width / 4, (double)rnd.nextInt(clip.height / 2) +
            clip.height / 4));

        g.setColor(java.awt.Color.BLACK);
        for (int i = 0; i < count - 1; ++i)
          g.drawLine((int)path.get(i).x, (int)path.get(i).y, (int)path
            .get(i + 1).x, (int)path.get(i + 1).y);

        SmoothPath smoothPath = new SmoothPath(path, rate, 1.0d, smoothness,
          1.0);
        g.setColor(new java.awt.Color(0, 0, 0, 64));
        g.setStroke(new java.awt.BasicStroke(20,
          java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.draw(smoothPath.getContinousePath());

        g.setStroke(new java.awt.BasicStroke(40,
          java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.setColor(new java.awt.Color(0, 0, 255, 64));
        g.drawLine((int)smoothPath.get(0).x, (int)smoothPath.get(0).y,
          (int)smoothPath.get(0).x, (int)smoothPath.get(0).y);

        g.setStroke(new java.awt.BasicStroke(10,
          java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g.setColor(new java.awt.Color(255, 0, 0, 64));
        for (Waypoint p : smoothPath)
          g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
      }
    };

    frame.add(panel);
    frame.setSize(400, 400);
    frame.setVisible(true);
  }

}
