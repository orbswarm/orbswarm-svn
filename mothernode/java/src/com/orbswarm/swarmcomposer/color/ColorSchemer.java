package com.orbswarm.swarmcomposer.color;

import com.orbswarm.swarmcomposer.util.StdDraw;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;


/**
 * @author Simran Gleason
 */
public class ColorSchemer implements ColorSchemeListener, MouseListener, MouseMotionListener, BotColorListener {
    private StdDraw drawer;
    public Color bgColor, circleColor, circleBorderColor, vectColor, vectColor_act, baseVectColor, dragVectColor;
    private int canvasSize = 500;
    private ColorScheme colorScheme;
    private ColorScheme defaultColorScheme;
    private String colorSchemeType;
    private int numSwatches = 6;
    public double radius = 100.;            // radius of universe
    public double colorWheel_x;
    public double colorWheel_y;
    public double colorWheelRadius;

    private JSlider spreadSlider;
    private JSlider baseHueSlider;
    private JSlider baseSatSlider;
    private JSlider baseValSlider;

    public HSV[] actualColors;

    private JFrame frame = null;
    private JPanel mainPanel = null;
    protected Container drawingPane;
    protected Component repaintComponent;

    protected ArrayList colorSchemeListeners;

    public void repaint() {
        repaintComponent.repaint();
    }

    public ColorSchemer(String colorSchemeType) {
        this.colorSchemeType = colorSchemeType;
        this.colorSchemeListeners = new ArrayList();
        this.actualColors = new HSV[numSwatches];
        for(int i=0; i < numSwatches; i++) {
            this.actualColors[i] = null;
        }
        
        drawer = new StdDraw(canvasSize);
        colorWheel_x = 0;
        colorWheel_y = radius / 2.5;
        colorWheelRadius = radius / 1.5;

        drawer.setXscale(-radius, +radius); 
        drawer.setYscale(-radius, +radius);
        drawer.addMouseListener(this);
        drawer.addMouseMotionListener(this);
        drawer.getDrawingPane().addMouseListener(this);
        drawer.getDrawingPane().addMouseMotionListener(this);
        repaintComponent = drawer.getDrawingPane();
        HSV initialBaseColor = new HSV(.7f, .8f, 1.f);
        float initialSpread = .16f;

        defaultColorScheme = new ColorSchemeAnalogous();
        colorScheme = ColorScheme.getColorScheme(colorSchemeType);
        if (colorScheme == null) {
            colorScheme = defaultColorScheme;
        }
        colorScheme.init(numSwatches, initialBaseColor, initialSpread);
        colorScheme.addColorSchemeListener(this);
        System.out.println("##################################");
        System.out.println("###  Color Schemer   init()    ###");
        System.out.println("##################################");
        init();
    }
    
    private void init() {
        initBGColors();
        drawColorWheel(drawer);
        drawer.initbg(bgColor);
        redraw();
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public JPanel getPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();

            mainPanel.setBackground(Color.BLACK);
            mainPanel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            drawingPane = drawer.getDrawingPane();
            mainPanel.add(drawingPane, gbc);

            gbc.weightx = 0.0;
            gbc.weighty = 0.0;
            gbc.gridx = 0;
            gbc.gridy = 1;
            JPanel controlPanel = createControlPanel();
            mainPanel.add(controlPanel, gbc);
        }
        return mainPanel;
    }
    
    public JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.EAST;
        // put sliders in here for the spread and value
        panel.add(new JLabel("Color Scheme"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(makeColorSchemeDropDown(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        panel.add(new JLabel("Spread"), gbc);
        gbc.gridy = 2;
        spreadSlider = make01Slider(null, colorScheme.getSpread());
        spreadSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting())
                    float newSpread = source.getValue() / 100.f;
                    getColorScheme().setSpread(newSpread);
                }
            });
        panel.add(spreadSlider, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(new JLabel("Base Color"), gbc);

        gbc.gridy = 2;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Hue"), gbc);
        gbc.gridx = 2;
        baseHueSlider = make01Slider("Hue", colorScheme.getBaseColor().getHue());
        baseHueSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    float newHue = source.getValue() / 100.f;
                    getColorScheme().setBaseHue(newHue);
                    if (!source.getValueIsAdjusting()) {
                        System.out.println("Hue slider changed. newHue: " + newHue + " color sscheme: " + getColorScheme());
                    }
                }
            });
        panel.add(baseHueSlider, gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        panel.add(new JLabel("Sat"), gbc);
        gbc.gridx = 2;
        baseSatSlider = make01Slider("Sat",colorScheme.getBaseColor().getSat());
        baseSatSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting())
                    float newSat = source.getValue() / 100.f;
                    colorScheme.setBaseSat(newSat);
                }
            });
        panel.add(baseSatSlider, gbc);

        gbc.gridy = 4;
        gbc.gridx = 1;
        panel.add(new JLabel("Val"), gbc);
        gbc.gridx = 2;
        baseValSlider = make01Slider("Val", colorScheme.getBaseColor().getVal());
        baseValSlider.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    JSlider source = (JSlider)e.getSource();
                    //if (!source.getValueIsAdjusting())
                    float newVal = source.getValue() / 100.f;
                    colorScheme.setBaseVal(newVal);
                }
            });

        panel.add(baseValSlider, gbc);
        return panel;
    }

    // just make a slider for now. 
    public JSlider make01Slider(String label, float val) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(100. * val));
        return slider;
    }

    public void setSpreadSlider(float newSpread) {
        int newval = (int)(100 * newSpread);
        if (spreadSlider.getValue() != newval) {
            spreadSlider.setValue(newval);
        }
    }

    public void setBaseHueSlider(float newHue) {
        int newval = (int)(100 * newHue);
        System.out.println("SetBaseHueSLider (old: " + baseHueSlider.getValue() + " new: " + newval+ ")");
        if (baseHueSlider.getValue() != newval) {
            baseHueSlider.setValue(newval);
        }
    }

    public void setBaseSatSlider(float newSat) {
        int newval = (int)(100 * newSat);
        if (baseSatSlider.getValue() != newval) {
            baseSatSlider.setValue(newval);
        }
    }

    public void setBaseValSlider(float newVal) {
        int newvalue = (int)(100 * newVal);
        if (baseValSlider.getValue() != newvalue) {
            baseValSlider.setValue(newvalue);
        }
    }

    public JComboBox makeColorSchemeDropDown() {
        JComboBox drop = new JComboBox();
        drop.setBackground(Color.LIGHT_GRAY);
        for(Iterator it = ColorScheme.getRegisteredColorSchemes(); it.hasNext(); ) {
            String scheme = (String)it.next();
            drop.addItem(scheme);
        }
        drop.setSelectedItem(this.colorSchemeType);

        drop.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    JComboBox cb = (JComboBox)e.getSource();
                    String newSchemeType = (String)cb.getSelectedItem();
                    setColorScheme(newSchemeType);
                }
            });
        return drop;
    }

    public void setColorScheme(String colorSchemeType) {
        this.colorSchemeType = colorSchemeType;
        ColorScheme oldColorScheme = this.colorScheme;
        ColorScheme newScheme = ColorScheme.getColorScheme(colorSchemeType);
        System.out.println("Switching color schemes. old: " + oldColorScheme + " new: " + newScheme);
        this.colorScheme = newScheme;
        newScheme.copyListeners(oldColorScheme);
        // TODO: generalize num swatches??
        newScheme.init(6, oldColorScheme.getBaseColor(), oldColorScheme.getSpread());
        redraw();
        broadcastNewColorScheme(this.colorScheme);
    }

    public JFrame getFrame() {
        if (frame == null) {
            createFrame(true);
        }
        return frame;
    }
    
    private JFrame createFrame(boolean decorated) {
        frame = new JFrame();
        // the frame for drawing to the screen
        frame.setVisible(false);
        frame.setContentPane(getPanel());
        frame.setResizable(decorated);
        frame.setUndecorated(!decorated);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        frame.setTitle("Swarm Color Tests");
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    private void redraw() {
        drawer.clearbg(bgColor);
        drawColorVectors(drawer, numSwatches, colorScheme);
        drawSwatches(drawer, numSwatches, colorScheme);
        drawer.show(true);
        repaint();
    }

    public void initBGColors() {
        bgColor = Color.getHSBColor(.8f, .2f, .1f);
        circleBorderColor = Color.getHSBColor(.7f, .35f, .2f);
        circleColor = Color.getHSBColor(.01234f, .25f, .10f);
        vectColor = Color.getHSBColor(.74f, 0.8f, .2f);
        vectColor_act = Color.getHSBColor(.74f, 0.8f, .5f);
        dragVectColor = Color.getHSBColor(.01f, 0.9f, .5f);
    }

    public void drawColorWheel(StdDraw drawer) {
        double x = colorWheel_x;
        double y = colorWheel_y;
        drawer.setPenColor_bg(circleBorderColor);
        //drawer.setPenRadius(colorWheelRadius / 20.);
        drawer.circle_bg(x, y, colorWheelRadius * 1.05);
        int colorPoints = 25;
        double pointSize = colorWheelRadius / (double)colorPoints;
        double pointRadius = pointSize / 2.;
        float val = 1.0f; // later: use slider;
        for(int deg = 0; deg < 360; deg++) {
            float hue = (float)deg / 360.f;
            double theta = Math.toRadians((double)deg);
            for(int i = 0; i < colorPoints; i++) {
                double r = i * pointSize;
                double px = (double)r * Math.cos(theta);
                double py = (double)r * Math.sin(theta);
                float sat = (float)r / (float)colorWheelRadius;
                Color pcolor = Color.getHSBColor(hue, sat, val);
                drawer.setPenColor_bg(pcolor);
                drawer.filledSquare_bg(x + px, y + py, pointRadius);
            }
            if (deg % 5 == 0) {
                repaint();
            }
        }
    }

    public void drawColorVectors(StdDraw drawer, int numSwatches, ColorScheme colorScheme) {
        for(int i=0; i < numSwatches; i++) {
            HSV color = colorScheme.getColor(i);
            float theta = colorScheme.hueToAngle(color.getHue());
            float r = (float)(color.getSat() * colorWheelRadius);
            double px = r * Math.cos(theta) + colorWheel_x;
            double py = r * Math.sin(theta) + colorWheel_y;
            if (i == dragSwatch) {
                drawer.setPenColor(dragVectColor);
            } else {
                drawer.setPenColor(vectColor);
            }
            drawer.setPenRadius();
            double oldPenRadius  = drawer.getPenRadius();
            //drawer.setPenRadius(1);
            drawer.line(colorWheel_x, colorWheel_y, px, py);
            if (i == 0) {
                drawer.setPenRadius(oldPenRadius * 3);
                drawer.circle(px, py, 3.5);
            } else {
                drawer.setPenRadius(oldPenRadius * 2);
                drawer.circle(px, py, 1.3);
            }
            drawer.setPenRadius(oldPenRadius);
            drawer.text(px+4, py+2, "" + i);

            if (i != 0 && actualColors[i] != null) {
                HSV a_color = actualColors[i];
                float a_theta = colorScheme.hueToAngle(a_color.getHue());
                float a_r = (float)(a_color.getSat() * colorWheelRadius);
                double a_px = a_r * Math.cos(a_theta) + colorWheel_x;
                double a_py = a_r * Math.sin(a_theta) + colorWheel_y;
                drawer.setPenColor(vectColor_act);
                drawer.filledCircle(a_px, a_py, 1.0);
                drawer.text(a_px+2, a_py+1, "[" + i + "]");
            }

        }
    }

    public void drawSwatches(StdDraw drawer, int numSwatches, ColorScheme colorScheme) {
        double size = .9 * (radius / numSwatches);
        double y = -.5 * radius;
        for(int i=0; i < numSwatches; i++) {
            double x = -1. * radius + i * 2.2 * size + size * .75;
            HSV swColor = colorScheme.getColor(i);
            Color c = swColor.toColor();
            drawer.setPenColor(c);
            drawer.filledSquare(x, y, size);
        }

        // draw the swatches for the actual colors used, if they have been set.
        y -= size * 2.3;
        for(int i=0; i < numSwatches; i++) {
            if (actualColors[i] != null) {
                double x = -1. * radius + i * 2.2 * size + size * .75;
                drawer.setPenColor(actualColors[i].toColor());
                drawer.filledSquare(x, y, size);
            }
        }
    }

    public void botColorChanged(int bot, int swatch, HSV hsv) {
        //System.out.println("     SCHEMER: bot colorChange(" + swatch + "): " + hsv);
        if (swatch < actualColors.length) {
            this.actualColors[swatch] = hsv;
            redraw();
        }
    }

    //
    // methods from ColorSchemeListener interface
    //
    public void colorSchemeChanged(ColorScheme cs) {
        HSV baseColor = cs.getBaseColor();
        System.out.println("SCHEMER: got colorSchemeChanged. base:" + baseColor );
        setBaseHueSlider(baseColor.getHue());
        setBaseSatSlider(baseColor.getSat());
        setBaseValSlider(baseColor.getVal());
        setSpreadSlider(cs.getSpread());
        redraw();
        broadcastColorSchemeChanged(cs);
    }
    
    public void newColorScheme(ColorScheme ncs) {
        if (this.colorScheme != ncs) {
            this.colorScheme = ncs;
            redraw();
        }
    }    
    
    public void addColorSchemeListener(ColorSchemeListener ear) {
        colorSchemeListeners.add(ear);
    }

    public void broadcastColorSchemeChanged() {
        broadcastColorSchemeChanged(colorScheme);
    }
    
    public void broadcastColorSchemeChanged(ColorScheme colorScheme) {
        //System.out.println("SCHEMER: boradcasting colorSchemeChanged. cs=" + colorScheme + " [" + colorSchemeListeners.size() + "] listeners");
        for(Iterator it = colorSchemeListeners.iterator(); it.hasNext(); ) {
            ColorSchemeListener ear = (ColorSchemeListener)it.next();
            ear.colorSchemeChanged(colorScheme);
        }
    }

    public void broadcastNewColorScheme(ColorScheme colorScheme) {
        //System.out.println("SCHEMER: broadcasting NEWcolorScheme. cs=" + colorScheme + " [" + colorSchemeListeners.size() + "] listeners");
        for(Iterator it = colorSchemeListeners.iterator(); it.hasNext(); ) {
            ColorSchemeListener ear = (ColorSchemeListener)it.next();
            ear.newColorScheme(colorScheme);
        }
    }

    
    int dragSwatch = -1;
    public void mousePressed (MouseEvent e) {
        //System.out.println("Bing!");
        double mx = drawer.mouseX();
        double my = drawer.mouseY();
        dragSwatch = findSwatch(mx, my, colorScheme);
    }

    public int findSwatch(double mx, double my, ColorScheme colorScheme) {
        double cwx = mx - colorWheel_x;
        double cwy = my - colorWheel_y; 
        double threshold = 4.;
        for(int i=0; i < numSwatches; i++) {
            HSV color = colorScheme.getColor(i);
            float theta = colorScheme.hueToAngle(color.getHue());
            float r = (float)(color.getSat() * colorWheelRadius);
            double px = r * Math.cos(theta);
            double py = r * Math.sin(theta);
            if ((Math.abs(px - cwx) < threshold) &&
                (Math.abs(py - cwy) < threshold)) {
                return i;
            }
        }
        return -1;
    }
        

    public void mouseClicked (MouseEvent e) { }
    public void mouseEntered (MouseEvent e){  }
    public void mouseExited  (MouseEvent e) { }
    public void mouseReleased(MouseEvent e) {
        dragSwatch = -1;
        redraw();
    }
    public void mouseMoved(MouseEvent e) { }

    public void mouseDragged(MouseEvent e) {
        if (drawer.mousePressed() && dragSwatch != -1 ) {
            double mx = drawer.mouseX();
            double my = drawer.mouseY();
            double cwx = mx - colorWheel_x;
            double cwy = my - colorWheel_y;
            double r = Math.sqrt(cwx * cwx + cwy * cwy);
            if (r > colorWheelRadius) {
                r = colorWheelRadius;
            }
            double theta = Math.atan2(cwy, cwx);

            colorScheme.swatchDragged(dragSwatch, r, theta, colorWheelRadius);
        }
    }

    public static void main(String args[]) {
        ColorScheme.registerColorScheme("Crown", ColorSchemeCrown.class);
        ColorScheme.registerColorScheme("Analogous", ColorSchemeAnalogous.class);
        ColorScheme.registerColorScheme("Split Complement", ColorSchemeSplitComplement.class);
        ColorScheme.registerColorScheme("Split Complement 3", ColorSchemeSplitComplement3.class);
        ColorScheme.registerColorScheme("Triad", ColorSchemeTriad.class);
        ColorScheme.registerColorScheme("Tetrad", ColorSchemeTetrad.class);
        String colorSchemeType = "Analogous";
        if (args.length > 0) {
            colorSchemeType = args[0];
        }
            
        ColorSchemer ct = new ColorSchemer(colorSchemeType);
        ct.getFrame();
    }
}