package com.orbswarm.swarmcon.swing;

import java.io.File;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.store.FileStore;
import com.orbswarm.swarmcon.store.IItemStore;
import com.orbswarm.swarmcon.store.TestStore;

public class Startup
{
  private static Logger log = Logger.getLogger(Startup.class);

  public static void main(String[] args)
  {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name",
    "SwarmCon");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    File pathStore = new File(Builder.DEFALUT_STORE_PATH);

    IItemStore store = null;

    // if the path store does not exist

    if (!pathStore.exists())
    {
      if (!pathStore.mkdir())
      {
        log
          .error("unable to create storage directory: " + pathStore +
            "\nUsing memory store which will NOT perminently store your items.");

        store = new TestStore();
      }
      else
        store = new FileStore(pathStore.toString());
    }
    else if (!pathStore.isDirectory())
    {
      log.error("path store is not a directory: " + pathStore +
        "\nUsing memory store which will NOT perminently store your items.");

      store = new TestStore();
    }
    else
      store = new FileStore(pathStore.toString());

    new Builder(store);
  }
}
