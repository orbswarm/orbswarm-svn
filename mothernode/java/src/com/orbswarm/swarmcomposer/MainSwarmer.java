package com.orbswarm.swarmcomposer;

import com.orbswarm.swarmcomposer.composer.BotController;
import com.orbswarm.swarmcomposer.color.ColorSchemer;
import com.orbswarm.swarmcomposer.color.ColorScheme;
import com.orbswarm.swarmcomposer.color.*;
import com.orbswarm.swarmcomposer.swarmulator.SwarmField;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;

/**
 * Wrapper class for the swarm composer and color schemer,
 * using the swarmulator simulation to provide the distance data.
 *
 * @author Simran Gleason
 */
public class MainSwarmer {
    public static final int NONE          = 0;
    public static final int SWARMULATOR   = 1;
    public static final int COMPOSER      = 2;
    public static final int COLOR_SCHEMER = 3;
    private JFrame frame;
    
    public static void main(String[] args) {
        ArrayList swarmulatorArgList = new ArrayList();
        ArrayList swarmComposerArgList = new ArrayList();
        ArrayList colorSchemerArgList = new ArrayList();
        int state = NONE;
        for(int i=0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("--swarmulator")) {
                state = SWARMULATOR;
            } else if (args[i].equalsIgnoreCase("--composer")) {
                state = COMPOSER;
            } else if (args[i].equalsIgnoreCase("--colorschemer")) {
                state = COLOR_SCHEMER;
            } else {
                switch (state) {
                case NONE:
                    break;
                case SWARMULATOR:
                    swarmulatorArgList.add(args[i]);
                    break;
                case COMPOSER:
                    swarmComposerArgList.add(args[i]);
                    break;
                case COLOR_SCHEMER:
                    colorSchemerArgList.add(args[i]);
                    break;
                }
            }
        }
        String[] swarmulatorArgs  = toStringArray(swarmulatorArgList);
        String[] swarmComposerArgs     = toStringArray(swarmComposerArgList);
        String[] colorSchemerArgs = toStringArray(colorSchemerArgList);

        int canvasSize = 800;
        SwarmField swarmField = new SwarmField(canvasSize);
        swarmField.handleArgs(swarmulatorArgs);

        int numbots = 6;
        BotController botctl = new BotController(numbots, "/orbsongs");
        botctl.handleArgs(swarmComposerArgs);

        ColorScheme.registerColorScheme("Analogous", ColorSchemeAnalogous.class);
        ColorScheme.registerColorScheme("Split Complement", ColorSchemeSplitComplement.class);
        ColorScheme.registerColorScheme("Split Complement 3", ColorSchemeSplitComplement3.class);
        ColorScheme.registerColorScheme("Triad", ColorSchemeTriad.class);
        ColorScheme.registerColorScheme("Tetrad", ColorSchemeTetrad.class);
        ColorScheme.registerColorScheme("Crown", ColorSchemeCrown.class);

        //ColorSchemer schemer = new ColorSchemer("Analogous");
        ColorSchemer schemer = new ColorSchemer("Triad");
        schemer.addColorSchemeListener(botctl);
        //schemer.addColorSchemeListener(jk.swarmulator); // for now. later, botcontroller will tell jk the colors.
        schemer.broadcastColorSchemeChanged();
        
        botctl.setColorScheme(schemer.getColorScheme()); // TODO: make this loosely coupled with a listener
        botctl.addBotColorListener(schemer);
        botctl.addBotColorListener(swarmField.swarmulator);
        
        swarmField.swarmulator.addSwarmListener(botctl);
        JPanel mainPanel = createPanel(schemer, botctl);
        JFrame frame = createFrame(mainPanel, true);
        
        botctl.playSongs();
    }

    private static JPanel createPanel(ColorSchemer colorSchemer, BotController botctl) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(colorSchemer.getPanel(), gbc);
        
        gbc.gridy = 1;
        panel.add(botctl.getPanel(), gbc);
            
        // put sliders in here for the spread and value
        return panel;
    }

    private static JFrame createFrame(JPanel panel, boolean decorated) {
        JFrame frame = new JFrame();
        // the frame for drawing to the screen
        frame.setVisible(false);
        frame.setContentPane(panel);
        frame.setResizable(decorated);
        frame.setUndecorated(!decorated);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);            // closes all windows
        frame.setTitle("Swarm Color Tests");
        frame.pack();
        frame.setVisible(true);
        return frame;
    }

    private static String[] toStringArray(ArrayList l) {
        String[] sar = new String[l.size()];
        for(int i=0; i < l.size(); i++) {
            sar[i] = (String)l.get(i);
        }
        return sar;
    }
}