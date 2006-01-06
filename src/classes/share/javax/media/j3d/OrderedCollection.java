/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.ArrayList;


/**
 * An OrderCollections contains a LightBin and an ArrayList of
 * of all top level OrderedGroups under this OrderCollection
 */
class OrderedCollection extends Object implements ObjectUpdate{

    LightBin lightBin = null;

    // a list of top level orderedBins under this orderedCollection
    ArrayList childOrderedBins = new ArrayList();

    // LightBin used for next frame
    LightBin nextFrameLightBin = null;
    
    // LightBins to be added for this frame
    LightBin addLightBins = null;

    boolean onUpdateList = false;

    public void updateObject() {
	int i;
	LightBin lb;
	lightBin = nextFrameLightBin;
	if (addLightBins != null) {
	    if (lightBin != null) {
		addLightBins.prev = lightBin;
		lightBin.next = addLightBins;
	    }
	    else {
		lightBin = addLightBins;
		nextFrameLightBin = lightBin;
	    }
	}
	addLightBins = null;
	onUpdateList = false;
    }
    


}

