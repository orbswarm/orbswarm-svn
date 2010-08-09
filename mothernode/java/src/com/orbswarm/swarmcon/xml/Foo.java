/**
 * 
 */
package com.orbswarm.swarmcon.xml;

import java.util.Vector;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class Foo
{
  private Vector<IBar> mBars = new Vector<IBar>();

  public void add(IBar bar)
  {
    getBars().add(bar);
  }

  public void setBars(Vector<IBar> bars)
  {
    mBars = bars;
  }

  public Vector<IBar> getBars()
  {
    return mBars;
  }

  public String toString()
  {
    return "Foo [mBars=" + mBars + "]";
  }
}