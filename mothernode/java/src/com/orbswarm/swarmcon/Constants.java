package com.orbswarm.swarmcon;

import static java.awt.Color.WHITE;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.Random;

public class Constants
{
  // color
  
  public static final Color BACKGROUND       = WHITE;
  public static final Color BUTTON_CLR       = new Color(  0,   0,   0, 164);
  /** Standard button font */
  
  /** path to resources directory */
  public static final String RESOURCES_PATH    = "resources";
  public static final Font BUTTON_FONT = new Font("Lucida Grande", Font.PLAIN, 40);
  /** the "way too close do something about it" distance */
  public static final double CRITICAL_DISTANCE =   2.0; // meters
  /** default scale */
  public static final double DEFAULT_PIXELS_PER_METER = 30;
  /** location of default properties file. */
  public static final String DEFAULT_PROPERTIES_FILE =
    RESOURCES_PATH + "/swarmcon.properties";
  /** maximum change in pitch range */
  public static final double DPITCH_RATE_DT    =  40.0; // degrees/second
  /** maximum change in roll rate */
  public static final double DROLL_RATE_DT     =  20.0; // degrees/second
  /** size of label font */
  public static final float LABEL_FONT_SIZE    = 18;
  /*
   * The following are hard coded constants.
   */
  
  /** physical radius of the orb */
  public static final double ORB_RADIUS        =   0.760 / 2; // meters
  /** Total maximum anticipated orb count. */
  public static final int MAX_ORB_COUNT       = 6;
  /** maximum rate of pitch */
  public static final double MAX_PITCH_RATE    = 114.6; // degrees/second
  /** maximum roll (left or right) */
  public static final double MAX_ROLL       =  35.0; // degrees
  /** maximum rate of roll */
  public static final double MAX_ROLL_RATE   =  50.0; // degrees/second
  public static final Color MENU_CLR         = new Color(  0,   0,   0, 128);
  public static final Font  MENU_FONT        =  new Font("Helvetica", Font.PLAIN, 15);
  public static final Color ORB_CLR          = new Color(196, 196, 196);
  
  /** physical diameter of orb */
  
  public static final double ORB_DIAMETER    = 2 * ORB_RADIUS; // meters
  public static Font  ORB_FONT               = new Font("Courier New", Font.PLAIN, 15);
  public static final Color ORB_FRAME_CLR    = new Color( 64,  64,  64);
  /*
   * The following are hard coded constants.
   */
  
  /** The offset between internal orb IDs (0-5) and actual orb IDs (60
   * - 65) */
  public static final int ORB_OFFSET_ID = 60;
  /** number of spars graphically printed on the orb */
  public static final int    ORB_SPAR_COUNT    =   4  ; // arcs
  public static Font  PHANTOM_ORB_FONT =  new Font("Courier New", Font.PLAIN, 3);
  /** time in seconds for a phantom to move to it's target position */
  public static final double PHANTOM_PERIOD    =  1   ;
  /** user modifiable properties file */
  public static final String PROPERTIES_FILE_LOCATION =
    System.getProperty("user.home") +
    System.getProperty("file.separator") + ".swarmcon.properties";
  /*
   * The following are global objects.
   */
  
  /** The global source of randomness. */
  public static final Random RND = new Random();
  /** safe distance from other object */
  public static final double SAFE_DISTANCE     =   3.0; // meters
  public static final Color SEL_ORB_CLR      = new Color(255, 196, 255);
  /** simulation key word */
  public static final String SIMULATION = "simulation";
  /*
   * The following are hard coded constants.
   */
  
  /** minimum frame delay in milliseconds */
  public static final long MIN_FRAME_DELAY   = 50;          
  public static final Font  MISC_FONT        = new Font("Helvetica", Font.PLAIN, 15);
  // color
  
  public static final Color TEXT_CLR         = new Color(  0,   0,   0, 128);
  public static final Color VECTOR_CRL       = new Color(255,   0,   0, 128);
  /** format for printing heading values */
  
  public static final NumberFormat HEADING_FORMAT = NumberFormat.getNumberInstance();
  public static final NumberFormat UTM_FORMAT = NumberFormat.getNumberInstance();
  public static final NumberFormat STANDARD_FORMAT = NumberFormat.getNumberInstance();
}
