package com.orbswarm.swarmcomposer.composer;

import java.util.List;

/**
 * 
 * Listen for neighbor change (gossip) events. 
 *
 * @author Simran Gleason
 */

public interface NeighborListener {
    public void setNeighbor(GossipEvent gev);
    public void neighborChanged(GossipEvent gev);
    public void neighborsChanged(List gossipEvents);
}
