package com.orbswarm.swarmcon.view;

import java.awt.Graphics2D;

import com.orbswarm.swarmcon.SwarmCon;
import com.orbswarm.swarmcon.mobject.IMobject;
import com.orbswarm.swarmcon.mobject.IMobjects;

public class MobjectsRenderer extends ARenderer<SwarmCon.MouseMobject>
{
  @SuppressWarnings("unchecked")
  public void render(Graphics2D g, IMobject o)
  {
    for (IMobject vobject: (IMobjects<IMobject>)o)
      Renderer.render(g, vobject);
  }
}
