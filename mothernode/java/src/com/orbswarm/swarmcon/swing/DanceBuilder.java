package com.orbswarm.swarmcon.swing;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.KeyStroke;

import com.orbswarm.swarmcon.model.SimModel;
import com.orbswarm.swarmcon.orb.IOrb;
import com.orbswarm.swarmcon.orb.Orb;
import com.orbswarm.swarmcon.path.BlockPath;
import com.orbswarm.swarmcon.path.CurveBlock;
import com.orbswarm.swarmcon.path.Dance;
import com.orbswarm.swarmcon.path.IBlockPath;
import com.orbswarm.swarmcon.path.IDance;
import com.orbswarm.swarmcon.path.StraightBlock;
import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.Item;
import com.orbswarm.swarmcon.util.NameGenerator;

@SuppressWarnings("serial")
public class DanceBuilder extends PathBuilder
{
  @SuppressWarnings("unused")

  public static void main(String[] args)
  {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    new DanceBuilder(new FileStore("/tmp/store"));
  }

  public DanceBuilder(FileStore fileStore)
  {
    super(fileStore);
    mEditMenu.addSeparator();
    mEditMenu.add(mPreviousePath);
    mEditMenu.add(mNextPath);
    mEditMenu.addSeparator();
    JMenu layoutMenu = new JMenu("Set Layout");
    mEditMenu.add(layoutMenu);
    
    for (Dance.Layout layout: Dance.Layout.values())
    {
      final Dance.Layout finalLayout = layout;
      layoutMenu.add(new SwarmAction(layout.name(), null,
        "set dance layout to " + layout.name().toLowerCase())
      {
        public void actionPerformed(ActionEvent e)
        {
          getCurrentDance().setLayout(finalLayout);
          repaint();
        }
      });
    }
  }
  
  @Override
  protected void createNewArtifact()
  {
    setArtifact(new Item<Dance>(new Dance(Dance.Layout.CIRLCE, 2), NameGenerator.getName(2)));
    for (int i = 0; i < 6; ++i)
    {
      getCurrentDance().addAfter(new BlockPath());
      getCurrentPath().addAfter(new StraightBlock(1));
    }
    repaint();
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

  @Override
  protected void createSwarm()
  {
    int orbId = 0;
    
    for (IBlockPath path : getCurrentDance().getPaths())
    {
      IOrb orb = new Orb(new SimModel(), orbId++);
      mSwarm.add(orb);
      orb.setHeading(path.getHeading());
      orb.setPosition(path.getPosition());
      orb.getModel().setTargetPath(path);
    }
  }

  private final SwarmAction mPreviousePath = new SwarmAction(
    "Previouse Path", KeyStroke.getKeyStroke(KeyEvent.VK_A, 0),
    "select previouse path")
  {
    public void actionPerformed(ActionEvent e)
    {
      previousePath();
    }
  };

  private final SwarmAction mNextPath = new SwarmAction("Next Path",
    KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "select next path")
  {
    public void actionPerformed(ActionEvent e)
    {
      nextPath();
    }
  };
}
