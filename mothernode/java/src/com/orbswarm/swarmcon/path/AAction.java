package com.orbswarm.swarmcon.path;

import java.util.UUID;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({SyncAction.class})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AAction implements IAction
{
  @SuppressWarnings("unused")
  @XmlID
  private final String mId;
  
  public AAction()
  {
    mId = UUID.randomUUID().toString();
  }
}
