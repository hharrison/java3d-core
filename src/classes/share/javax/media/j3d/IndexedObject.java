/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Class used for IndexedUnorderedList
 */

abstract class IndexedObject extends Object {

    /**
     * A 2D array listIdx[3][len] is used.
     * The entry listIdx[0][], listIdx[0][1] is used for each VirtualUniverse.
     * The entry listIdx[2][0] is used for index to which one to use.
     *
     * This is used to handle the case the Node Object move from
     * one VirtualUniverse A to another VirtualUniverse B.
     * It is possible that another Structures in B may get the add
     * message first before the Structures in A get the remove
     * message to clear the entry. This cause MT problem. So a
     * 2D array is used to resolve it.
     */
    int[][] listIdx;

    abstract VirtualUniverse getVirtualUniverse();
    
    synchronized int getIdxUsed(VirtualUniverse u) {
	int idx = listIdx[2][0];
	if (u == getVirtualUniverse()) {
	    return idx;
	}
	return (idx == 0 ? 1 : 0);
    }

    void incIdxUsed() {
	if (listIdx[2][0] == 0) {
	    listIdx[2][0] = 1; 
	} else {
	    listIdx[2][0] = 0; 		    
	}	
    }
}


