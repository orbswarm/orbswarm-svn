@XmlJavaTypeAdapters
({
  @XmlJavaTypeAdapter(value=AffineTransformAdapter.class,type=AffineTransform.class),
  @XmlJavaTypeAdapter(value=Point2DAdapter.class,type=Point2D.class)
})

package com.orbswarm.swarmcon.vobject;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import com.orbswarm.swarmcon.store.AffineTransformAdapter;
import com.orbswarm.swarmcon.store.Point2DAdapter;