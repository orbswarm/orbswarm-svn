package com.orbswarm.swarmcon.path;

public interface ISelectable
{
  /**
   * Returns true is this object is selected.
   * 
   * @return true if selected
   */
  
  boolean isSelected();

  /**
   * Set the selected state of this object.
   * 
   * @param selected true if selected
   */

  void setSelected(boolean selected);
}
