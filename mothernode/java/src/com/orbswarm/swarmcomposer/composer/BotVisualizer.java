package com.orbswarm.swarmcomposer.composer;

import java.awt.*;
import javax.swing.*;
import java.util.HashMap;
import java.util.List;


/**
 * @author Simran Gleason
 */
public class BotVisualizer implements NeighborListener, SwarmListener {
    int nbots;
    HashMap neighborViews;
    JTextArea distanceMatrixView;
    protected JFrame frame = null;
    protected JPanel mainPanel = null;
    protected Color backgroundColor = Color.DARK_GRAY;
    protected Color foregroundColor = Color.WHITE;
    
    public BotVisualizer(int nbots) {
        this.nbots = nbots;
        neighborViews = new HashMap();
        createUI(nbots, neighborViews);
    }

    public void setNeighbor(GossipEvent gev) {
        neighborChanged(gev);
    }
    
    public void neighborChanged(GossipEvent gev) {
        Neighbor neighbor = gev.getNeighbor();
        NeighborView nv = getNeighborView(neighbor);
        if (nv != null) {
            nv.songField.setText("Song: " + neighbor.getSong());
            nv.layerField.setText("Layer: " + neighbor.getLayer());
            nv.setField.setText("Set: " + neighbor.getSet());
            nv.soundField.setText(neighbor.getSound());
        }
    }

    public void neighborsChanged(List gevs) {
    }
    
    public void updateSwarmDistances(double radius, int nb, int[][] distances) {
        StringBuffer buf = new StringBuffer();
        printDistances(buf, distances);
        distanceMatrixView.setText(buf.toString());
    }
            
    public static void printDistances(int [][] distances) {
        StringBuffer buf = new StringBuffer();
        printDistances(buf, distances);
        System.out.println(buf.toString());
    }

    public static void printDistances(StringBuffer buf, int [][] distances) {
        int n = distances[0].length;
        buf.append(" + |   ");
        for(int i=0; i < n; i++) {
            buf.append(i + "   ");
        }
        buf.append("\n________________\n");
            for(int i=0; i < n; i++) {
            buf.append(" " + i + " | ");
            for(int j=0; j <=i; j++) { 
                String num = distances[i][j] + "";
                while (num.length() < 3) {
                    num = " " + num;
                }
                buf.append(num + " ");
            }
            buf.append("\n");
        }
        buf.append("------------------\n");
    }

    public NeighborView getNeighborView(Neighbor neighbor) {
        return (NeighborView)neighborViews.get(neighbor.getName());
    }
    
    class NeighborView {
        public String    name;
        public JLabel songField;
        public JLabel layerField;
        public JLabel setField;
        public JLabel soundField;
        public NeighborView(String name, 
                            JLabel songField,
                            JLabel layerField,
                            JLabel setField,
                            JLabel soundField) {
            this.name = name;
            this.songField = songField;
            this.layerField = layerField;
            this.setField = setField;
            this.soundField = soundField;
            songField.setForeground(foregroundColor);
            layerField.setForeground(foregroundColor);
            setField.setForeground(foregroundColor);
            soundField.setForeground(foregroundColor);
        }
    }

    public JFrame getFrame() {
        if (frame == null) {
            frame = new JFrame();
            frame.setContentPane(getPanel());
            frame.setTitle("Bot Visualizer: " + nbots + " bots.");
            frame.pack();
            frame.setVisible(true);
        }
        return frame;
    }

    public JPanel getPanel() {
        if (mainPanel == null) {
            createUI(nbots, neighborViews);
        }
        return mainPanel;
    }
    
    public JPanel createUI(int nbots, HashMap neighborViews) {
        mainPanel = new JPanel();
        mainPanel.setBackground(backgroundColor);
        mainPanel.setForeground(foregroundColor);
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        JPanel botsPanel = createBotsPanel(nbots, neighborViews);
        
        // floater thing to keep the universe centered
        gbc.gridx=0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        mainPanel.add(botsPanel, gbc);

        distanceMatrixView = new JTextArea(nbots + 3, 20);
        distanceMatrixView.setBackground(backgroundColor);
        distanceMatrixView.setForeground(foregroundColor);
        
        gbc.gridx = 1;
        mainPanel.add(distanceMatrixView, gbc);
        return mainPanel;
    }

    public JPanel createBotsPanel(int nbots, HashMap neighborViews) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(backgroundColor);
        panel.setForeground(foregroundColor);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        int cols = nbots / 2;
        int bot = 0;
        for(int r=0; r < 2; r++) {
            for(int c=0; c < cols; c++) {
                gbc.gridx = c;
                gbc.gridy = r;
                JPanel viewPanel = createNeighborViewPanel(bot, neighborViews);
                panel.add(viewPanel, gbc);
                bot++;
            }
        }
        return panel;
    }

    public JPanel createNeighborViewPanel(int bot, HashMap neighborViews) {
        JPanel panel = new JPanel();
        panel.setBackground(backgroundColor);
        panel.setForeground(foregroundColor);
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createLineBorder(foregroundColor));
        GridBagConstraints gbc = new GridBagConstraints();

        String botName = "Bot_" + bot;
        NeighborView nb = new NeighborView(botName,
                                           new JLabel(),
                                           new JLabel(),
                                           new JLabel(),
                                           new JLabel());
        neighborViews.put(botName, nb);

        // floater thing to keep the universe centered
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        JLabel bn = new JLabel(botName);
        bn.setForeground(foregroundColor);
        panel.add(bn, gbc);
        
        gbc.gridy++;
        panel.add(nb.songField, gbc);

        gbc.gridy++;
        panel.add(nb.layerField, gbc);
        gbc.gridy++;
        panel.add(nb.setField, gbc);
        gbc.gridy++;
        panel.add(nb.soundField, gbc);

        Dimension d = panel.getSize();
        d.setSize(d.getHeight(), 200);
        return panel;
    }
}