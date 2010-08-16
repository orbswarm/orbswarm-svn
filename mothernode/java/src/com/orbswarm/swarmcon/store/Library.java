package com.orbswarm.swarmcon.store;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;

import com.orbswarm.swarmcon.view.ArenaPanel;
import com.orbswarm.swarmcon.view.RendererSet;

public class Library extends JPanel
{
  private static final long serialVersionUID = 2017028152849656592L;

  public static final int PANEL_WIDTH = 70;

  public static final int PANEL_HEIGHT = 70;

  private final Collection<ItemPanel> mItemPanels;

  private final IItemStore mStore;
  
  public Library(IItemStore store)
  {
    mItemPanels = new Vector<ItemPanel>();
    mStore = store;
    update();
  }

  public void update()
  {
    mItemPanels.clear();
    for (IItem<?> item: mStore.getItems())
      mItemPanels.add(new ItemPanel(item));
    layoutItems();
  }
  
  public void layoutItems()
  {
    removeAll();
    setLayout(new GridLayout());

    for (ItemPanel itemPanel : mItemPanels)
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
    f.getContentPane().add(new Library(new TestStore()));
    f.pack();
    f.setVisible(true);
  }
  
  
  public Collection<ItemPanel> getItems()
  {
    return mItemPanels;
  }

  class ItemPanel extends ArenaPanel
  {
    private final IItem<?> mItem;
    private double mBorder = 2;
    
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
      
      Rectangle2D bounds = RendererSet.getShape(mItem.getItem()).getBounds();
      bounds.setRect(bounds.getX() - mBorder , bounds.getY() - mBorder , bounds
        .getWidth() +
        2 * mBorder, bounds.getHeight() + 2 * mBorder);
      setViewPort(bounds);
      
      // paint the view
      
      Graphics2D g = (Graphics2D)graphics;
      super.paint(g);
      
      // render the item
      
      RendererSet.render(g, mItem.getItem());
    }
  }
}
