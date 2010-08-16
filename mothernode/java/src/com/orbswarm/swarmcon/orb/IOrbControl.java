package com.orbswarm.swarmcon.orb;

import com.orbswarm.swarmcon.path.Path;
import com.orbswarm.swarmcon.path.SmoothPath;
import com.orbswarm.swarmcon.path.Target;
import com.orbswarm.swarmcon.swing.SwarmCon;

public interface IOrbControl
{
  public SmoothPath gotoTarget(int orbNum, Target target);

  public SmoothPath followPath(int orbNum, Path targets);

  public void stopOrb(int orbNum);

  public SwarmCon getSwarmCon();
}
