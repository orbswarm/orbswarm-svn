package com.orbswarm.swarmcon.store;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;
import java.util.Vector;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

public class CloudStore extends AItemStore
{
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(CloudStore.class);

  private final String mHost;
  private final int mPort;

  public static void main(String[] args)
  {
    CloudStore s = new CloudStore("localhost", 8888);
    log.debug("result: " + s.restore(UUID.randomUUID()));
  }
  
  public CloudStore(String host, int port)
  {
    super(true);
    mHost = host;
    mPort = port;

    initialize();
  }

  private String httpGet(String path)
  {
    String response = null;
    
    try
    {
      HttpHost target = new HttpHost(mHost, mPort, "http");
      HttpClient client = new DefaultHttpClient();
      HttpGet get = new HttpGet(path);

      response = EntityUtils.toString(client.execute(target, get).getEntity());
    }
    catch (ClientProtocolException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return response;
  }
  
  private String httpPost(String path, String data)
  {
    String response = null;
    
    try
    {
      HttpHost target = new HttpHost(mHost, mPort, "http");
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(path);
      response = EntityUtils.toString(client.execute(target, post).getEntity());
    }
    catch (ClientProtocolException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return response;
  }

  Collection<UUID> catalog()
  {
    final Collection<UUID> catalog = new Vector<UUID>();
    String[] idStrings = httpGet("/swarmstore?action=catalog").split(" ");
    for (String idString: idStrings)
      catalog.add(UUID.fromString(idString));
    return catalog;
  }

  String restore(UUID id)
  {
    return httpGet("/swarmstore?action=restore&id=" + id);
  }

  void save(UUID id, String xml)
  {
    httpPost("/swarmstore?action=save&id=" + id, xml);
  }
}
