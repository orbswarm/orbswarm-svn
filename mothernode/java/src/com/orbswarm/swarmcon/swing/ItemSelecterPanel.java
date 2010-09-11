package com.orbswarm.swarmcon.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.orbswarm.swarmcon.store.IItem;
import com.orbswarm.swarmcon.store.IItemFilter;
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

  public static final int PANEL_WIDTH = 100;
  public static final int PANEL_HEIGHT = 100;

  private final ISelectableList<ItemPanel> mItemPanels;

  private final IItemStore mStore;
  private final IItemFilter mFilter;
  private final Window mParent;
  private IItem<?> mSelectedItem = null;
  
  public static void main(String[] args)
  {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.getContentPane().add(
      new ItemSelecterPanel(new TestStore(), IItemStore.ACCEPT_ALL, f));
    f.pack();
    f.setVisible(true);
  }

  public ItemSelecterPanel(IItemStore store, IItemFilter filter, Window parent)
  {
    mItemPanels = new SelectableList<ItemPanel>();
    mStore = store;
    mFilter = filter;
    mParent = parent;
    addAction(mNext);
    addAction(mPreviouseItem);
    addAction(mSelectItem);
    addAction(mCancel);
    update();
  }
  
  public void addAction(SwarmAction action)
  {
    getActionMap().put(action, action);
    getInputMap().put(action.getAccelerator(), action);    
  }
  

  public void update()
  {
    mItemPanels.removeAll();
    for (IItem<?> item: mStore.getSome(mFilter))
      mItemPanels.addAfter(new ItemPanel(item));
    mItemPanels.first();
    layoutItems();
  }
  
  public void layoutItems()
  {
    int rows = mItemPanels.size() / 5;
    int cols = 5;
    
    removeAll();
    setLayout(new GridLayout(rows, cols));

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
  
  public Collection<ItemPanel> getItems()
  {
    return mItemPanels.getAll();
  }

  public IItem<?> getSelectedItem()
  {
    return mSelectedItem;
  }
  
  protected void previouseItem()
  {
    mItemPanels.previouse();
  }

  protected void nextItem()
  {
    mItemPanels.next();
  }
  
  protected void selectCurrent()
  {
    mSelectedItem = mItemPanels.getCurrent().getItem();
    exit();
  }
  
  protected void cancel()
  {
    mSelectedItem = null;
    exit();
  }

  protected void exit()
  {
    mParent.dispose();
    mParent.setVisible(false);
  }
  
  class ItemPanel extends ArenaPanel implements ISelectable
  {
    private boolean mSelected;
    private final IItem<?> mItem;
    private boolean mSuppressed;
    
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
        Constants.ARENA_VIEWPORT_BORDER);

      // set background according to selectedness

      setBackground(isSelected()
        ? Constants.DEFAULT_ARENA_BACKGROUND
        : Constants.NOT_SELECTED_BACKGROUND);

      // paint the view

      Graphics2D g = (Graphics2D)graphics;
      super.paint(g);

      // render the item

      mItem.getItem().setSuppressed(true);
      RendererSet.render(g, mItem.getItem());
      mItem.getItem().setSuppressed(false);
    }

    public boolean isSelected()
    {
      return mSuppressed ? false : mSelected;
    }

    public void setSelected(boolean selected)
    {
      mSelected = selected;
    }
    
    public void setSuppressed(boolean suppressed)
    {
      mSuppressed = suppressed;
    }
  }
  
  @SuppressWarnings("serial")
  private final SwarmAction mPreviouseItem = new SwarmAction(
    "Previouse", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      0), "move to previouse item")
  {
    public void actionPerformed(ActionEvent e)
    {
      previouseItem();
    }
  };

  @SuppressWarnings("serial")
  private final SwarmAction mNext= new SwarmAction("Next",
    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0),
    "move to next item")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextItem();
    }
  };
  
  @SuppressWarnings("serial")
  private final SwarmAction mSelectItem = new SwarmAction("Select",
    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
    "select this item")
  {
    public void actionPerformed(ActionEvent e)
    {
      selectCurrent();
    }
  };
  
  @SuppressWarnings("serial")
  private final SwarmAction mCancel = new SwarmAction("Cancel",
    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
    "exit without selecting an item")
  {
    public void actionPerformed(ActionEvent e)
    {
      cancel();
    }
  };
}
