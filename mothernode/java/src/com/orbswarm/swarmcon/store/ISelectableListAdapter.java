package com.orbswarm.swarmcon.store;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.orbswarm.swarmcon.util.ISelectable;
import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;

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