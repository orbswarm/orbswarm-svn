package com.orbswarm.swarmcomposer.composer;

/**
 * Compatibility matrix for sets.
 *
 * @author Simran Gleason
 */

public class Compatibility {
    public static final Compatibility UNITY = new Compatibility(100);
    
    private int index;
    public Compatibility(String indexStr) {
        this.index = Integer.parseInt(indexStr);
    }

    public Compatibility(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
    
    public String toString() {
        return "C(" + index + ")";
    }

    public void toString(StringBuffer buf) {
        buf.append("" + index);
    }
}
