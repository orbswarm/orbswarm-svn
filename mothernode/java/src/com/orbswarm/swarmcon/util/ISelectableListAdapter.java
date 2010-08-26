package com.orbswarm.swarmcon.util;

import javax.xml.bind.annotation.adapters.XmlAdapter;


public class ISelectableListAdapter
  extends
  XmlAdapter<SelectableList<? extends ISelectable>, ISelectableList<? extends ISelectable>>
{
  public ISelectableList<? extends ISelectable> unmarshal(
    SelectableList<? extends ISelectable> v) throws Exception
  {
    v.initializeIterator();
    return v;
  }

  public SelectableList<? extends ISelectable> marshal(
    ISelectableList<? extends ISelectable> v) throws Exception
  {
    return (SelectableList<? extends ISelectable>)v;
  }
}