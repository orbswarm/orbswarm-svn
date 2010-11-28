package com.orbswarm.swarmcon.path;

import com.orbswarm.swarmcon.view.APositionable;

// smooth path for printing on display

public class SmoothMobject extends APositionable
{
  private final SmoothPath mSmoothPath;

  public SmoothMobject(SmoothPath sp)
  {
    mSmoothPath = sp;
  }

  public void update(double time)
  {
  }

  public SmoothPath getSmoothPath()
  {
    return mSmoothPath;
  }
}
