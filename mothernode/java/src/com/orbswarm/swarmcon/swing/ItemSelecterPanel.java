package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.orbswarm.swarmcon.store.IItem;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.store.TestStore;
import com.orbswarm.swarmcon.util.Constants;
import com.orbswarm.swarmcon.util.ISelectable;
import com.orbswarm.swarmcon.util.ISelectableList;
import com.orbswarm.swarmcon.util.SelectableList;
import com.orbswarm.swarmcon.view.RendererSet;

public class ItemSelecterPanel extends JPanel
{
  private static final long serialVersionUID = 2017028152849656592L;

  public static final int PANEL_WIDTH = 70;

  public static final int PANEL_HEIGHT = 70;

  private final ISelectableList<ItemPanel> mItemPanels;

  private final IItemStore mStore;
  
  public ItemSelecterPanel(IItemStore store)
  {
    mItemPanels = new SelectableList<ItemPanel>();
    mStore = store;
    update();
  }

  public void update()
  {
    mItemPanels.removeAll();
    for (IItem<?> item: mStore.getItems())
      mItemPanels.addAfter(new ItemPanel(item));
    layoutItems();
  }
  
  public void layoutItems()
  {
    removeAll();
    setLayout(new GridLayout());

    for (ItemPanel itemPanel : mItemPanels.getAll())
    {
      JPanel wrapper = new JPanel();
      wrapper.setLayout(new BorderLayout());
      wrapper.add(itemPanel);
      wrapper.setBorder(new TitledBorder(new LineBorder(Color.gray, 5), itemPanel
        .getItem().getName()));
      add(wrapper);
    }

    validate();
    repaint();
  }
  
  public static void main(String[] args)
  {
    JFrame f = new JFrame();
    f.getContentPane().add(new ItemSelecterPanel(new TestStore()));
    f.pack();
    f.setVisible(true);
  }
  
  public Collection<ItemPanel> getItems()
  {
    return mItemPanels.getAll();
  }

  class ItemPanel extends ArenaPanel implements ISelectable
  {
    private boolean mSelected;
    private final IItem<?> mItem;
    
    private static final long serialVersionUID = 7883183615949305291L;
    
    public ItemPanel(IItem<?> item)
    {
      super(false, true, false);
      mItem = item;
      setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    }

    public IItem<?> getItem()
    {
      return mItem;
    }

    public void paint(Graphics graphics)
    {
      // view only the item

      setViewPort(mItem.getItem().getBounds2D(),
        Constants.ARENA_VIWPORT_BORDER);

      // set background according to selectedness

      setBackground(isSelected()
        ? Constants.DEFAULT_ARENA_BACKGROUND
        : Constants.NOT_SELECTED_BACKGROUND);

      // paint the view

      Graphics2D g = (Graphics2D)graphics;
      super.paint(g);

      // render the item

      RendererSet.render(g, mItem.getItem());
    }

    public boolean isSelected()
    {
      return mSelected;
    }

    public void setSelected(boolean selected)
    {
      mSelected = selected;
    }
  }
}
