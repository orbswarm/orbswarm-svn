package com.orbswarm.swarmcon.store;

import java.awt.geom.AffineTransform;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import static com.orbswarm.swarmcon.store.AffineTransformAdapter.MarshalablleAffineTransform;

public class AffineTransformAdapter extends
  XmlAdapter<MarshalablleAffineTransform, AffineTransform>
{
  @XmlAccessorType(XmlAccessType.FIELD)
  public static class MarshalablleAffineTransform
  {
    @XmlElement(name="value")
    private final double[] mMatrix;

    public MarshalablleAffineTransform()
    {
      this(null);
    }
    
    public MarshalablleAffineTransform(AffineTransform transform)
    {
      mMatrix = new double[6];
      if (null != transform)
        transform.getMatrix(mMatrix);
    }
    
    public AffineTransform createTransform()
    {
      return new AffineTransform(mMatrix);
    }

    public void setMatrix(double[] matrix)
    {
      for (int i = 0; i < mMatrix.length && i < matrix.length; ++i)
        mMatrix[i] = matrix[i];
    }
  }

  public MarshalablleAffineTransform marshal(AffineTransform v)
    throws Exception
  {
    return new MarshalablleAffineTransform(v);
  }

  public AffineTransform unmarshal(MarshalablleAffineTransform v)
    throws Exception
  {
    return v.createTransform();
  }
}