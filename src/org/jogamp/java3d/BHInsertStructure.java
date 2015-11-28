/*
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
 */

package org.jogamp.java3d;

import java.util.ArrayList;
import java.util.Random;

class BHInsertStructure  {

    static boolean debug = false;
    static boolean debug2 = false;

    Random randomNumber;
    ArrayList[] bhListArr = null;
    ArrayList[] oldBhListArr = null;
    BHNode[] bhListArrRef = null;
    BHNode[] oldBhListArrRef = null;
    int bhListArrCnt = 0;
    int bhListArrMaxCnt = 0;
    int blockSize = 0;

    BHInsertStructure(int length) {
	randomNumber = new Random(0);

	if(length > 50) {
	    length = 50;
	}

	blockSize = 50;
	bhListArr = new ArrayList[length];
	bhListArrRef = new BHNode[length];
	bhListArrCnt = 0;
	bhListArrMaxCnt = length;

    }

    void clear() {

	for(int i=0; i<	bhListArrCnt; i++) {
	    bhListArr[i].clear();
	    bhListArrRef[i] = null;
	}
	bhListArrCnt = 0;
    }

    void lookupAndInsert(BHNode parent, BHNode child) {
	boolean found = false;

	for ( int i=0; i<bhListArrCnt; i++ ) {
	    // check for current parent
	    if ( bhListArrRef[i] == parent ) {
		// place child element in currents array of children
		bhListArr[i].add(child);
		found = true;
		break;
	    }
	}

	if ( !found ) {

	    if(bhListArrCnt >= bhListArrMaxCnt) {
		// allocate a bigger array here....
		if(debug)
		    System.err.println("(1) Expanding bhListArr array ...");
		bhListArrMaxCnt += blockSize;
		oldBhListArr = bhListArr;
		oldBhListArrRef = bhListArrRef;

		bhListArr = new ArrayList[bhListArrMaxCnt];
		bhListArrRef = new BHNode[bhListArrMaxCnt];
		System.arraycopy(oldBhListArr, 0, bhListArr, 0, oldBhListArr.length);
		System.arraycopy(oldBhListArrRef, 0, bhListArrRef, 0,
				 oldBhListArrRef.length);
	    }

	    bhListArrRef[bhListArrCnt] = parent;
	    bhListArr[bhListArrCnt] = new ArrayList();
	    bhListArr[bhListArrCnt].add(child);
	    bhListArrCnt++;
	}

    }

    void updateBoundingTree(BHTree bhTree) {

	// based on the data in this stucture, update the tree such that
	// all things work out now .. i.e for each element of the array list
	// of bhListArr ... create a new reclustered tree.
	int size, cnt;
	BHNode child1, child2;

	for ( int i=0; i < bhListArrCnt; i++ ) {
	    // extract and form an array of all children : l, r, and n1 ... nk
	    cnt = 0;
	    child1 = ((BHInternalNode)(bhListArrRef[i])).getLeftChild();
	    child2 = ((BHInternalNode)(bhListArrRef[i])).getRightChild();
	    if(child1 != null) cnt++;
	    if(child2 != null) cnt++;

	    size = bhListArr[i].size();

	    BHNode bhArr[] = new BHNode[cnt + size];

	    bhListArr[i].toArray(bhArr);

	    //reset cnt, so that we can reuse it.
	    cnt = 0;
	    if(child1 != null) {
		bhArr[size] = child1;
		cnt++;
		bhArr[size + cnt] =  child2;
	    }

	    if(debug2)
		if((child1 == null) || (child2 == null)) {
		    System.err.println("child1 or child2 is null ...");
		    System.err.println("This is bad, it shouldn't happen");

		}

	    ((BHInternalNode)(bhListArrRef[i])).setRightChild(null);
	    ((BHInternalNode)(bhListArrRef[i])).setLeftChild(null);

	    bhTree.cluster((BHInternalNode)bhListArrRef[i], bhArr);
	}
    }

}


