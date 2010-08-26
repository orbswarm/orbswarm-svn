package com.orbswarm.swarmcon.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.store.FileStore;

@SuppressWarnings("serial")
public class DanceBuilder2 extends BlockPathBuilder
{
  @SuppressWarnings("unused")

  public static void main(String[] args)
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new DanceBuilder2(new FileStore("/tmp/store"));
  }

  public DanceBuilder2(FileStore fileStore)
  {
    super(fileStore);
    mEditMenu.addSeparator();
    mEditMenu.add(mPreviousePath);
    mEditMenu.add(mNextPath);
  }
  
  @Override
  protected void initializeArtifact()
  {
    setArtifact(new Dance(Dance.Layout.CIRLCE, 2));
    for (int i = 0; i < 6; ++i)
    {
      getCurrentDance().addAfter(new BlockPath());
      getCurrentPath().addAfter(new CurveBlock(60, 2, CurveBlock.Type.RIGHT));
    }
  }

  @Override
  public IBlockPath getCurrentPath()
  {
    return getCurrentDance().getCurrentPath();
  }

  public IDance getCurrentDance()
  {
    return (IDance)getArtifact();
  }

  private void nextPath()
  {
    getCurrentDance().nextPath();
    repaint();
  }

  private void previousePath()
  {
    getCurrentDance().previousePath();
    repaint();
  }

  private final SwarmAction mPreviousePath = new SwarmAction(
    "Previouse Path", KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
      KeyEvent.META_DOWN_MASK), "select previouse Path")
  {
    public void actionPerformed(ActionEvent e)
    {
      previousePath();
    }
  };

  private final SwarmAction mNextPath = new SwarmAction("Next Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.META_DOWN_MASK),
    "select next bath")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextPath();
    }
  };
}
