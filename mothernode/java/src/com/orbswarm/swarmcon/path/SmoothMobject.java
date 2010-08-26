package com.orbswarm.swarmcon.path;

import com.orbswarm.swarmcon.view.ARenderable;

// smooth path for printing on display

public class SmoothMobject extends ARenderable
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
