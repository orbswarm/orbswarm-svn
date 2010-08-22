@XmlJavaTypeAdapters
({
    @XmlJavaTypeAdapter(value=AffineTransformAdapter.class,type=AffineTransform.class)
})

package com.orbswarm.swarmcon.vobject;
import java.awt.geom.AffineTransform;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import com.orbswarm.swarmcon.store.AffineTransformAdapter;