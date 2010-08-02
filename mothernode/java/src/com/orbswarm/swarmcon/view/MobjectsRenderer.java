package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;

import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class MobjectsRenderer extends ARenderer<IVobjects<?>>
{
  @SuppressWarnings("unchecked")
  public void render(Graphics2D g, IVobjects<?> vobjects)
  {
    for (IVobject vobject: vobjects)
      Renderer.render(g, vobject);
  }
}
