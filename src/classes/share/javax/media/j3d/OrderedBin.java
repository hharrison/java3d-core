/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
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
 * An OrderedBin contains an array of OrderedCollection, each represents
 * a child of the OrderedGroup
 */
class OrderedBin extends Object {
    // ArrayList of orderedCollection, one for each child of the orderedGroup
    ArrayList orderedCollections = new ArrayList();
    
    // orderedGroup source
    OrderedGroupRetained source;
    OrderedChildInfo childInfoList= null;
    OrderedChildInfo lastChildInfo = null;

    boolean onUpdateList = false;

    // Value of already existing orderedCollection
    ArrayList setOCForCI = new ArrayList();
    ArrayList valueOfSetOCForCI = new ArrayList();

    // Value of orderedCollection based on oi, these arrays
    // have size > 0 only during update_view;
    ArrayList setOCForOI = new ArrayList();
    ArrayList valueOfSetOCForOI = new ArrayList();

    OrderedBin(int nchildren, OrderedGroupRetained src){
        int i;
        for (i=0; i< nchildren; i++) {
            orderedCollections.add(null);
        }
        source = src;
    }

    void addRemoveOrderedCollection() {
	int i, index;

	// Add the setValues first, since they reflect already existing
	// orderedCollection
	for (i = 0; i < setOCForCI.size(); i++) {
	    index = ((Integer)setOCForCI.get(i)).intValue();
	    OrderedCollection oc = (OrderedCollection)valueOfSetOCForCI.get(i);
	    orderedCollections.set(index, oc);
	}

	setOCForCI.clear();
	valueOfSetOCForCI.clear();

	while (childInfoList != null) {
	    if (childInfoList.type == OrderedChildInfo.ADD) {
		orderedCollections.add(childInfoList.childId, childInfoList.value);
	    }
	    else if (childInfoList.type == OrderedChildInfo.REMOVE) {
		orderedCollections.remove(childInfoList.childId);
	    }
	    childInfoList = childInfoList.next;
	}
	
	// Now update the sets based on oi, since the og.orderedChildIdTable reflects
	// the childIds for the next frame, use the table to set the oc at the
	// correct place
	for (i = 0; i < setOCForOI.size(); i++) {
	    index = ((Integer)setOCForOI.get(i)).intValue();
	    OrderedCollection oc = (OrderedCollection)valueOfSetOCForOI.get(i);
	    int ci = source.orderedChildIdTable[index];
	    orderedCollections.set(ci, oc);
	}
	setOCForOI.clear();
	valueOfSetOCForOI.clear();
	
	onUpdateList = false;
	lastChildInfo = null;


    }
    void addChildInfo(OrderedChildInfo cinfo) {
	// Add this cinfo at the end
	if (childInfoList == null) {
	    childInfoList = cinfo;
	    lastChildInfo = cinfo;
	}
	else {
	    // Add at the end
	    cinfo.prev = lastChildInfo;
	    lastChildInfo.next = cinfo;	    
	    cinfo.next = null;
	    // Update this to be the last child
	    lastChildInfo = cinfo;
	}

    }
    
}


