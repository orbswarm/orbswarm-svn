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

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.view.ArenaPanel;
import com.orbswarm.swarmcon.view.IRenderer;
import com.orbswarm.swarmcon.view.RendererSet;
import com.orbswarm.swarmcon.vobject.IVobject;

public class Library extends JPanel
{
  private static final long serialVersionUID = 2017028152849656592L;

  public static final int PANEL_WIDTH = 70;

  public static final int PANEL_HEIGHT = 70;

  private final Collection<ItemPanel<?>> mItems;
  
  public Library()
  {
    mItems = new Vector<ItemPanel<?>>();
    update();
  }

  public void layoutItems()
  {
    removeAll();
    setLayout(new GridLayout());
//    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    for (ItemPanel<?> itemPanel : mItems)
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
  
  public void update()
  {
    CurveBlock cbl = new CurveBlock(90, 4, CurveBlock.Type.LEFT);
    CurveBlock cbr = new CurveBlock(90, 4, CurveBlock.Type.LEFT);
    StraightBlock sb = new StraightBlock(4);
    
    BlockPath bp1 = new BlockPath(cbl, sb, cbl, cbr, cbr);
    BlockPath bp2 = new BlockPath(cbr, sb, cbl, cbr, cbr);
    BlockPath bp3 = new BlockPath(sb, cbl, cbr, cbr);
    
    mItems.add(new ItemPanel<BlockPath>(bp1, RendererSet.getRenderer(bp1)));
    mItems.add(new ItemPanel<BlockPath>(bp2, RendererSet.getRenderer(bp2)));
    mItems.add(new ItemPanel<BlockPath>(bp3, RendererSet.getRenderer(bp3)));
    layoutItems();
  }
  
  public static void main(String[] args)
  {
    JFrame f = new JFrame();
    f.getContentPane().add(new Library());
    f.pack();
//    f.setSize(new Dimension(3 * PANEL_WIDTH, 3 * PANEL_HEIGHT));
    f.setVisible(true);
  }
  
  
  public Collection<ItemPanel<?>> getItems()
  {
    return mItems;
  }

  class ItemPanel<T extends IVobject & INamed> extends ArenaPanel
  {
    private final T mItem;
    private final IRenderer<T> mRenderer;
    private double mBorder = 2;
    
    private static final long serialVersionUID = 7883183615949305291L;
    
    public ItemPanel(T item, IRenderer<T> renderer)
    {
      super();
      mItem = item;
      mRenderer = renderer;
      setSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
      setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
    }

    public T getItem()
    {
      return mItem;
    }

    public void paint(Graphics graphics)
    {
      Rectangle2D bounds = RendererSet.getShape(mItem).getBounds();
      bounds.setRect(bounds.getX() - mBorder , bounds.getY() - mBorder , bounds
        .getWidth() +
        2 * mBorder, bounds.getHeight() + 2 * mBorder);
      setViewPort(bounds);
      
      Graphics2D g = (Graphics2D)graphics;
      
      
      super.paint(g);
      mRenderer.render(g, mItem);
    }
  }
}
