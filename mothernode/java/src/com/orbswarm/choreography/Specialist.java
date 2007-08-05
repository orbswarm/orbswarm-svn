package com.orbswarm.choreography;

import com.orbswarm.swarmcom.Swarm;

public interface Specialist {
    public void start();
    public void stop();
    public void enable(boolean value); // by default Specialists are enabled. 

    public void setup(OrbControl orbControl, Properties initialProperties);

    public void orbState(Swarm orbSwarm);

    public void    setProperty(String name, String val);
    public String  getProperty(String name);

    public void command(String action, int orb, String param); // orb == -1 if N/A
    public void addCommandListener(SpecialistListener ear);
}
