package com.orbswarm.swarmcon;

public interface IOrbControl
{
    public SmoothPath gotoTarget(int orbNum, Target target);
    public SmoothPath followPath(int orbNum, Path targets);

    public void stopOrb(int orbNum);

    public SwarmCon getSwarmCon();
}

