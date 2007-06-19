package org.trebor.swarmcon;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.border.*;

import static java.lang.System.*;
import static javax.swing.Action.*;
import static javax.swing.KeyStroke.*;
import static java.awt.event.KeyEvent.*;
import static java.awt.event.ActionEvent.*;

class PidTuner extends JPanel
{
         // vectors of samples and targets, plus min and max of these

      Vector<Double> samples = new Vector<Double>();
      Vector<Double> references = new Vector<Double>();
      double min = Double.MAX_VALUE;
      double max = Double.MIN_VALUE;

         // some colors
      
      public static final Color GRAPH_COLOR = new Color(128, 128, 128);
      public static final Color EDGE_COLOR = new Color(  32,  32,  32);

         // internal controller

      PIDController controller;
      Controller[] controllers;

         // normalize a double

      public double normalize(double x)
      {
         return min + (x / (max - min));
      }
         // sample graph

      JPanel graph = new JPanel()
         {
               public void paint(Graphics graphics)
               {
                     // configure graphics

                  Graphics2D g = (Graphics2D)graphics;
                  g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                  Rectangle bounds = g.getClipBounds();
                  
                     // set white background

                  g.setColor(Color.WHITE);
                  g.fill(bounds);
                  
                     // if we have not data, stop now

                  if (samples.isEmpty())
                     return;

                     // compute graph adjstments
                  
                  double scale = bounds.getHeight() / (max - min); 
                  g.scale(1.0d, scale);
                  g.translate(0, -min);
                  g.setStroke(
                     new BasicStroke((float)(scale / 1), 
                                     BasicStroke.CAP_BUTT, 
                                     BasicStroke.JOIN_MITER));

/*                  float ref0 = references.get(0).floatValue();
                  float refn = references.lastElement().floatValue();
                  float n = samples.size() - 1;

                  GeneralPath sShape = new GeneralPath();
                  sShape.moveTo(0f, ref0);
                  for (float i = 0; i < samples.size(); ++i)
                     sShape.lineTo(i, samples.get((int)i).floatValue());
                  sShape.lineTo(n, refn);

                  GeneralPath rShape = new GeneralPath();
                  rShape.moveTo(0f, ref0);
                  rShape.lineTo(n, refn);

                  g.setColor(new Color(255, 32, 32, 128));
                  g.fill(sShape);
                  g.setColor(new Color(255, 0, 0));
                  g.draw(sShape);
                  g.setColor(new Color(32, 32, 32));
                  g.draw(rShape);
                     //g.drawLine(0, (int)ref0, (int)n, (int)refn);
                     */

                     // run through the samples and paint them

                  double oldSample = 0;
                  double oldReference = 0;
                  for (int i = 0; i < samples.size(); ++i)
                  {
                     double sample = samples.get(i);
                     double reference = references.get(i);

                     g.setColor(GRAPH_COLOR);
                     g.draw(new Line2D.Double(i, sample, i, reference));

                        // if we've got some history draw lines
                     
//                      if (i > 0)
//                      {
//                         g.setColor(EDGE_COLOR);
//                         g.draw(new Line2D.Double(i, oldSample, 
//                                                  i, sample));
//                         g.draw(new Line2D.Double(i, oldReference, 
//                                                  i, reference));
//                      }
//                         // record history

//                      oldSample = sample;
//                      oldReference = reference;
                  }
               }
         };

         // pid spinners

      JSpinner pValue = new JSpinner(
         new SpinnerNumberModel(0.5, 
                                -Double.MAX_VALUE, 
                                Double.MAX_VALUE, 
                                1.0d));

      JSpinner iValue = new JSpinner(
         new SpinnerNumberModel(1.0e100d,
                                -Double.MAX_VALUE, 
                                Double.MAX_VALUE, 
                                1.0d));

      JSpinner dValue = new JSpinner(
         new SpinnerNumberModel(1.0e-100d,
                                -Double.MAX_VALUE, 
                                Double.MAX_VALUE,
                                1.0d));

         // min max output spinners

      JSpinner minOutput = new JSpinner(
         new SpinnerNumberModel(-1,
                                -Double.MAX_VALUE, 
                                Double.MAX_VALUE,
                                1.0d));

      JSpinner maxOutput = new JSpinner(
         new SpinnerNumberModel(1,
                                -Double.MAX_VALUE,
                                Double.MAX_VALUE,
                                1.0d));

      
         // sample and reference fields

      JTextField sampleFld = new JTextField();
      JTextField referenceFld = new JTextField();

         // actions

      PidAction pt10 = new PidAction(
         "* 10",
         getKeyStroke(VK_P, 0),
         "multiply P by 10",
         pValue, 10d);
      PidAction pd10 = new PidAction(
         "/ 10",
         getKeyStroke(VK_P, 0),
         "divide P by 10",
         pValue, 0.1d);
      PidAction it10 = new PidAction(
         "* 10",
         getKeyStroke(VK_P, 0),
         "multiply I by 10",
         iValue, 10d);
      PidAction id10 = new PidAction(
         "/ 10",
         getKeyStroke(VK_P, 0),
         "divide I by 10",
         iValue, 0.1d);
      PidAction dt10 = new PidAction(
         "* 10",
         getKeyStroke(VK_P, 0),
         "multiply D by 10",
         dValue, 10d);
      PidAction dd10 = new PidAction(
         "/ 10",
         getKeyStroke(VK_P, 0),
         "divide D by 10",
         dValue, 0.1d);

      PidAction pt3 = new PidAction(
         "* 3",
         getKeyStroke(VK_P, 0),
         "multiply P by 3",
         pValue, 3d);
      PidAction pd3 = new PidAction(
         "/ 3",
         getKeyStroke(VK_P, 0),
         "divide P by 3",
         pValue, (1d / 3));
      PidAction it3 = new PidAction(
         "* 3",
         getKeyStroke(VK_P, 0),
         "multiply I by 3",
         iValue, 3d);
      PidAction id3 = new PidAction(
         "/ 3",
         getKeyStroke(VK_P, 0),
         "divide I by 3",
         iValue, (1d / 3));
      PidAction dt3 = new PidAction(
         "* 3",
         getKeyStroke(VK_P, 0),
         "multiply D by 3",
         dValue, 3d);
      PidAction dd3 = new PidAction(
         "/ 3",
         getKeyStroke(VK_P, 0),
         "divide D by 3",
         dValue, (1d / 3));

      PidAction pt2 = new PidAction(
         "* 2",
         getKeyStroke(VK_P, 0),
         "multiply P by 2",
         pValue, 2d);
      PidAction pd2 = new PidAction(
         "/ 2",
         getKeyStroke(VK_P, 0),
         "divide P by 2",
         pValue, 0.5d);
      PidAction it2 = new PidAction(
         "* 2",
         getKeyStroke(VK_P, 0),
         "multiply I by 2",
         iValue, 2d);
      PidAction id2 = new PidAction(
         "/ 2",
         getKeyStroke(VK_P, 0),
         "divide I by 2",
         iValue, 0.5d);
      PidAction dt2 = new PidAction(
         "* 2",
         getKeyStroke(VK_P, 0),
         "multiply D by 2",
         dValue, 2d);
      PidAction dd2 = new PidAction(
         "/ 2",
         getKeyStroke(VK_P, 0),
         "divide D by 2",
         dValue, 0.5d);

         // constructor

      public PidTuner(Controller[] controllers)
      {
         this.controllers = controllers;
         constructFrame();
      }
         // create the elements which make up the visual frame

      public void constructFrame()
      {
            // add border to panel 

         setBorder(new LineBorder(Color.GRAY, 10));

            // create the spinner number editors

         pValue.setEditor(new JSpinner.NumberEditor(pValue, "0.###E0"));
         iValue.setEditor(new JSpinner.NumberEditor(iValue, "0.###E0"));
         dValue.setEditor(new JSpinner.NumberEditor(dValue, "0.###E0"));

            // set layout

         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            // construct and add P panel

         add(constructParamPanel("P", pValue, 
                                 new NormalButton(pt10), 
                                 new NormalButton(pt3),
                                 new NormalButton(pt2),
                                 new NormalButton(pd10),
                                 new NormalButton(pd3),
                                 new MarkedButton(pd2)));

            // construct and add I panel

         add(constructParamPanel("I", iValue, 
                                 new NormalButton(it10), 
                                 new MarkedButton(it3),
                                 new NormalButton(it2),
                                 new NormalButton(id10),
                                 new NormalButton(id3),
                                 new NormalButton(id2)));

            // construct and add D panel

         add(constructParamPanel("D", dValue, 
                                 new NormalButton(dt10), 
                                 new NormalButton(dt3),
                                 new NormalButton(dt2),
                                 new NormalButton(dd10),
                                 new MarkedButton(dd3),
                                 new NormalButton(dd2)));

            // add min max spiners

         JPanel mmPan = new JPanel();
         Dimension d = minOutput.getPreferredSize();
         d.width = 60;
         minOutput.setPreferredSize(d);
         maxOutput.setPreferredSize(d);
         mmPan.add(createLabel("MIN"));
         mmPan.add(minOutput);
         mmPan.add(createLabel("OUTPUT"));
         mmPan.add(maxOutput);
         mmPan.add(createLabel("MAX"));
         add(mmPan);

            // add current reading

         JPanel rsPan = new JPanel();
         d = referenceFld.getPreferredSize();
         d.width = 60;
         sampleFld.setPreferredSize(d);
         referenceFld.setPreferredSize(d);
         rsPan.add(createLabel("Sample"));
         rsPan.add(sampleFld);
         rsPan.add(createLabel("Reference"));
         rsPan.add(referenceFld);
         add(rsPan);

            // add graph
         
            //graph.setBorder(new LineBorder(Color.BLACK, 20));
            //new BevelBorder(BevelBorder.LOWERED));
         graph.setPreferredSize(new Dimension(20, 400));
         add(graph);
         
            // add current reading

         JPanel cPan = new JPanel();
         final PidTuner tuner = this;
         for (final Controller c: controllers)
         {
            JButton button = new JButton(c.toString());
            cPan.add(button);
            button.addActionListener(new ActionListener()
               {
                     public void actionPerformed(ActionEvent e)
                     {
                        c.setTuner(tuner);
                        min = Double.MAX_VALUE;
                        max = Double.MIN_VALUE;
                        samples.clear();
                        references.clear();
                     }
               });
         }
         add(cPan);

            // add writter

         JButton button = new JButton("write");
         button.addActionListener(new ActionListener()
            {
                  public void actionPerformed(ActionEvent e)
                  {
                     for (final Controller c: controllers)
                     {
                        if (c instanceof PDController)
                        {
                           PDController pd = (PDController)c;
                           System.out.println("Contoller: " + c.toString());
                           System.out.println(" P: " + pd.getKp());
                           System.out.println(" D: " + pd.getKd());
                        }
                        if (c instanceof PIDController)
                        {
                           PIDController pid = (PIDController)c;
                           System.out.println("Contoller: " + c.toString());
                           System.out.println(" P: " + pid.getKp());
                           System.out.println(" I: " + pid.getKi());
                           System.out.println(" D: " + pid.getKd());
                        }
                     }
                  }
            });
         add(button);
      }
         // construct parameter panel

      public JPanel constructParamPanel(String tag, JSpinner value, 
                                        JButton t10,
                                        JButton t3,
                                        JButton t2,
                                        JButton d10,
                                        JButton d3,
                                        JButton d2)
      {
         JPanel pPanel = new JPanel();
         JPanel buttonPanel = new JPanel();
         buttonPanel.setLayout(new GridLayout(2,3));
         buttonPanel.add(t2);
         buttonPanel.add(t3);
         buttonPanel.add(t10);
         buttonPanel.add(d2);
         buttonPanel.add(d3);
         buttonPanel.add(d10);
         pPanel.add(createLabel(tag));
         pPanel.add(value);
         pPanel.add(buttonPanel);
         return pPanel;
      }
         // main for testing

      public static void main(String[] args)
      {
         new PidTuner(null);
      }
         // add sample to graph

      public void addSample(double sample, double reference)
      {
         sampleFld.setText("" + sample);
         referenceFld.setText("" + reference);
         samples.add(0, sample);
         references.add(0, reference);
         min = Math.min(min, sample);
         max = Math.max(max, sample);
         min = Math.min(min, reference);
         max = Math.max(max, reference);

            //minOutput.setValue(min);
            //maxOutput.setValue(max);
         if (samples.size() > graph.getWidth())
            samples.setSize(graph.getWidth());

         graph.repaint();
      }
         // getter for P

      public double getP()
      {
         return ((Double)pValue.getValue()).doubleValue();
      }
         // getter for I

      public double getI()
      {
         return ((Double)iValue.getValue()).doubleValue();
      }
         // getter for D

      public double getD()
      {
         return ((Double)dValue.getValue()).doubleValue();
      }
         // getter for min output

      public double getMin()
      {
         return ((Double)minOutput.getValue()).doubleValue();
      }
         // getter for max output

      public double getMax()
      {
         return ((Double)maxOutput.getValue()).doubleValue();
      }
         // setter for P

      public void setP(double value)
      {
         pValue.setValue(new Double(value));
      }
         // setter for P

      public void setI(double value)
      {
         iValue.setValue(new Double(value));
      }
         // setter for P

      public void setD(double value)
      {
         dValue.setValue(new Double(value));
      }
         // setter for min output

      public void setMin(double value)
      {
         minOutput.setValue(new Double(value));
      }
         // setter for max output

      public void setMax(double value)
      {
         maxOutput.setValue(new Double(value));
      }
         // create label

      public JLabel createLabel(String title)
      {
         JLabel l = new JLabel(title);
         l.setFont(new Font("courier", 0, 40));
         return l;
      }
         // marked button class

      class NormalButton extends JButton
      {
            public NormalButton(AbstractAction a)
            {
               super(a);
               setFont(getFont().deriveFont(18f));
            }
      }
         // marked button class

      class MarkedButton extends NormalButton
      {
            public MarkedButton(AbstractAction a)
            {
               super(a);
               setForeground(new Color(204, 0, 0));
            }
      }
         // action class

      class PidAction extends AbstractAction
      {
            JSpinner variable;
            double constant;

               // construct the action

            public PidAction(String name, KeyStroke key,
                             String description, 
                             JSpinner variable, double constant)
            {
               this.variable = variable;
               this.constant = constant;

               putValue(NAME, name);
               putValue(SHORT_DESCRIPTION, description);
               putValue(ACCELERATOR_KEY, key);
            }
               // execute action

            public void actionPerformed(ActionEvent e)
            {
               variable.setValue(new Double(((Double)variable.getValue())
                                            .doubleValue() * constant));
            }
      }
}
