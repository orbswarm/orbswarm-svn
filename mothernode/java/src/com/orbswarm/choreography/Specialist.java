package com.orbswarm.choreography;

import java.util.Properties;

public interface Specialist {
    /** Called to initialize the specialist.
        Implemented in AbstractSpecialist superclass.
        Most implementations will call super.setup(...)
    */
    public void setup(OrbControl orbControl, Properties initialProperties);

    /** Called when specialist is started. */
    public void start();

    /** Called when specialist is stopped. */
    public void stop();

    /** Called to enable/disable specialist. Specialists shouldn't send send data
        when disabled.
        Implemented in AbstractSpecialist.
        by default Specialists are enabled. 
    */
    public void enable(boolean value); 

    /** Called periodically to update the orb state */
    public void orbState(Swarm orbSwarm);

    /** Give this specialist a command. */
    public void command(String action, int orb, String param); // orb == -1 if N/A

    ///////////////////////////////////////////////////
    /// Typically implemented in AbstractSpecialist ///
    ///////////////////////////////////////////////////

    public void    setProperty(String name, String val);
    public String  getProperty(String name);

    public void addCommandListener(SpecialistListener ear);

}
