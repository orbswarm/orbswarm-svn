package com.orbswarm.swarmcon.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.orbswarm.swarmcon.view.IRenderable;

public class FileStore extends AItemStore
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(FileStore.class);

  private static final Pattern FILE_PATTERN_RE =
    Pattern.compile("^(" + GUID_PATTERN + ")\\.xml$");
  private static final String FILE_FORMAT = "%s%s%s.xml";
  private final File mStore;

  public FileStore(String storePath)
  {
    mStore = new File(storePath);
    if (!mStore.exists())
      throw new IllegalArgumentException("file store does not exits: " +
        mStore);
    if (!mStore.isDirectory())
      throw new IllegalArgumentException("file store is not a directory: " +
        mStore);
    if (!mStore.canWrite())
      throw new IllegalArgumentException("file store is not writable: " +
        mStore);
    if (!mStore.canRead())
      throw new IllegalArgumentException("file store is not readable: " +
        mStore);
    
    initialize();
    
    for (IItem<? extends IRenderable> i: getItems())
    {
      log.debug("item: " + i);
      log.debug("  id: " + i.getId());
      log.debug("   i: " + i.getItem());
    }
  }

  Collection<UUID> catalog()
  {
    final Collection<UUID> catalog = new Vector<UUID>();
    for (String name : mStore.list())
    {
      Matcher m = FILE_PATTERN_RE.matcher(name);
      if (m.find())
        catalog.add(UUID.fromString(m.group(1)));
    }
    return catalog;
  }

  String restore(UUID id)
  {
    StringBuffer buffer = new StringBuffer();
    try
    {
      BufferedReader input =
        new BufferedReader(new FileReader(establishFile(id)));
      
      try
      {
        String separator = System.getProperty("line.separator");
        String line = null;
        while ((line = input.readLine()) != null)
        {
          buffer.append(line);
          buffer.append(separator);
        }
      }
      finally
      {
        input.close();
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    return buffer.toString();
  }

  private File establishFile(UUID id)
  {
    return new File(String.format(FILE_FORMAT, mStore, File.separator, id));
  }

  void save(UUID id, String xml)
  {
    try
    {
      FileWriter writer = new FileWriter(establishFile(id));
      writer.write(xml);
      writer.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
}
