@XmlJavaTypeAdapters
({
    @XmlJavaTypeAdapter(value=IBlockAdapter.class,type=IBlock.class)
})

package com.orbswarm.swarmcon.path;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import com.orbswarm.swarmcon.store.IBlockAdapter;
