package com.orbswarm.swarmcomposer.composer;

/**
 * 
 *
 * Represents a communication of a change in a neighbor's state. 
 *
 * @author Simran Gleason
 */
public class GossipEvent {
    protected Neighbor neighbor;
    protected String event;
    protected Object object;
    public GossipEvent(Neighbor neighbor, String event, Object object) {
        super();
        this.neighbor = neighbor;
        this.event = event;
        this.object = object;
    }
    public String getEvent() {
        return event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public Neighbor getNeighbor() {
        return neighbor;
    }
    public void setNeighbor(Neighbor neighbor) {
        this.neighbor = neighbor;
    }
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
  
}
