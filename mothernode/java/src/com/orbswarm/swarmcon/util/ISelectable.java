package com.orbswarm.swarmcon.util;

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
  
  /**
   * Suppress the selected state. The selected state is maintained but
   * suppressed.
   * 
   * @param suppress if true selected state is suppressed, otherwise it
   *        returns to it's original state.
   */
  
  void setSuppressed(boolean suppress);
}
