package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.vobject.IVobject;
import com.orbswarm.swarmcon.vobject.IVobjects;

public class MobjectsRenderer extends ARenderer<SwarmCon.MouseMobject>
{
  @SuppressWarnings("unchecked")
  public void render(Graphics2D g, IVobject o)
  {
    for (IVobject vobject: (IVobjects<IVobject>)o)
      Renderer.render(g, vobject);
  }
}
