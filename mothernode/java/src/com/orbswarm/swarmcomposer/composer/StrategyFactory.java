package com.orbswarm.swarmcomposer.composer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Strategy for selection of sounds from a set.
 *
 * @author Simran Gleason
 */

public class StrategyFactory {
    public static Strategy createStrategy(String strategyName) {
	if (strategyName.equalsIgnoreCase(Strategy.RANDOM)) {
	    return new RandomStrategy();
	} else if (strategyName.equalsIgnoreCase("SingleTrack")) {
	    return new SingleTrackStrategy();
    }
	// else if ... other known strategy types
	// else ... find the class by name. treat strategy name as fully qualified class name on classpath
	return null;
    }
}