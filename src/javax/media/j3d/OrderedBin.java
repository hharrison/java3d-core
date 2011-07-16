/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
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


