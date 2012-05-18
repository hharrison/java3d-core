/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.media.j3d;

import java.util.ArrayList;

/**
 * The OrderedGroup is a group node that ensures its children rendered
 * in index increasing order.
 */

class OrderedGroupRetained extends GroupRetained {
    // mapping of ordered child id to child index
    int orderedChildIdTable[];

    // This is a counter for ordered child id
    private int orderedChildIdCount = 0;

    // This is a vector of free orderedChildId
    private ArrayList orderedChildIdFreeList = new ArrayList();

// used to lock the orderedBin array
private final Object lockObj = new Object();
// One OrderedBin per view
private OrderedBin[] orderedBin = new OrderedBin[0];

    // ChildCount used by renderBin to initialize the
    // orderedCollection in each orderedBin (per view)
    int childCount = 0;

    // per children ordered path data
    ArrayList childrenOrderedPaths = new ArrayList(1);


    // child index order - set by the user.
    int[] userChildIndexOrder = null;

    // child index order - use by j3d internal.
    int[] childIndexOrder = null;


    OrderedGroupRetained() {
          this.nodeType = NodeRetained.ORDEREDGROUP;
    }


    void setChildIndexOrder(int[] cIOArr) {
	if(cIOArr != null) {
	    if((userChildIndexOrder == null) ||
	       (userChildIndexOrder.length != cIOArr.length)) {
		userChildIndexOrder = new int[cIOArr.length];
	    }

	    System.arraycopy(cIOArr, 0, userChildIndexOrder,
			     0, userChildIndexOrder.length);
	}
	else {
	    userChildIndexOrder = null;
	}

	if (source.isLive()) {
	    int[] newArr = new int[cIOArr.length];
	    System.arraycopy(cIOArr, 0, newArr,
			     0, newArr.length);
	    J3dMessage m;
	    m = new J3dMessage();
	    m.threads = J3dThread.UPDATE_RENDER;
	    m.type = J3dMessage.ORDERED_GROUP_TABLE_CHANGED;
	    m.universe = universe;
	    m.args[3] = this;
	    m.args[4] = newArr;
	    VirtualUniverse.mc.processMessage(m);
	}
    }

    int[] getChildIndexOrder() {
	if (userChildIndexOrder == null) {
	    return null;
	}

	int[] newArr = new int[userChildIndexOrder.length];
	System.arraycopy(userChildIndexOrder, 0,
			 newArr, 0, userChildIndexOrder.length);
	return newArr;

    }

    Integer getOrderedChildId() {
        Integer orderedChildId;
	synchronized(orderedChildIdFreeList) {
	    if (orderedChildIdFreeList.size() == 0) {
		orderedChildId = new Integer(orderedChildIdCount);
		orderedChildIdCount++;
	    } else {
		orderedChildId = (Integer)orderedChildIdFreeList.remove(0);
	    }
	}
        return(orderedChildId);
    }

    void freeOrderedChildId(int id) {
	synchronized(orderedChildIdFreeList) {
	    orderedChildIdFreeList.add(new Integer(id));
	}
    }

    int getOrderedChildCount() {
	int count;

	synchronized (orderedChildIdFreeList) {
	    count = orderedChildIdCount;
	}
	return count;
    }

    void addChild(Node child) {
	if(userChildIndexOrder != null) {
	    doAddChildIndexEntry();
	}

	// GroupRetained.addChild have to check for case of non-null child index order
	// array and handle it.
	super.addChild(child);

    }

    void addChild(Node child, int[] cIOArr) {
	if(cIOArr != null) {
	    userChildIndexOrder  = new int[cIOArr.length];

	    System.arraycopy(cIOArr, 0, userChildIndexOrder,
			     0, userChildIndexOrder.length);
	}
	else {
	    userChildIndexOrder = null;
	}

	// GroupRetained.addChild have to check for case of non-null child
	// index order array and handle it.
	super.addChild(child);

    }

    void moveTo(BranchGroup bg) {
	if(userChildIndexOrder != null) {
	    doAddChildIndexEntry();
	}

	// GroupRetained.moveto have to check for case of non-null child
	// index order array and handle it.
	super.moveTo(bg);
    }


    void doRemoveChildIndexEntry(int index) {

	int[] newArr  = new int[userChildIndexOrder.length - 1];

	for(int i=0, j=0; i<userChildIndexOrder.length; i++) {
	    if(userChildIndexOrder[i] > index) {
		newArr[j] = userChildIndexOrder[i] - 1;
		j++;
	    }
	    else if(userChildIndexOrder[i] < index) {
		newArr[j] = userChildIndexOrder[i];
		j++;
	    }
	}

	userChildIndexOrder = newArr;

    }

    void doAddChildIndexEntry() {
	int[] newArr  = new int[userChildIndexOrder.length + 1];

	System.arraycopy(userChildIndexOrder, 0, newArr,
			 0, userChildIndexOrder.length);

	newArr[userChildIndexOrder.length] = userChildIndexOrder.length;

	userChildIndexOrder = newArr;
    }

    /**
     * Compiles the children of the OrderedGroup, preventing shape merging at
     * this level or above
     */
    void compile(CompileState compState) {

        super.compile(compState);

        // don't remove this group node
        mergeFlag = SceneGraphObjectRetained.DONT_MERGE;

        if (J3dDebug.devPhase && J3dDebug.debug) {
            compState.numOrderedGroups++;
        }
    }

void setOrderedBin(OrderedBin ob, int index) {
	synchronized (lockObj) {
		if (index < orderedBin.length) {
			orderedBin[index] = ob;
			return;
		}

		// If we're clearing the entry to null, just return, don't bother
		// expanding the array
		if (ob == null)
			return;

		OrderedBin[] newList = new OrderedBin[index + 1];
		System.arraycopy(orderedBin, 0, newList, 0, orderedBin.length);
		orderedBin = newList;
		orderedBin[index] = ob;
	}
}

// Get the orderedBin for this view index
OrderedBin getOrderedBin(int index) {
	synchronized (lockObj) {
		if (index >= orderedBin.length)
			return null;
		else
			return orderedBin[index];
	}
}

    void updateChildIdTableInserted(int childId, int orderedId) {
	int size = 0;
	int i;

	//System.err.println("updateChildIdTableInserted childId " + childId + " orderedId " + orderedId + " " + this);
        if (orderedChildIdTable != null) {
	    size = orderedChildIdTable.length;
	    for (i=0; i<size; i++) {
		if (orderedChildIdTable[i] != -1) {
		    if (orderedChildIdTable[i] >= childId) {
			orderedChildIdTable[i]++; // shift upward
		    }
	        }
	    }
	}
        if (orderedId >= size) {
            int newTable[];
            newTable = new int[orderedId+1];
	    if (size > 0) {
		System.arraycopy(orderedChildIdTable,0,newTable,0,
				 orderedChildIdTable.length);
	    }
	    else {
		for (i = 0; i < newTable.length; i++) {
		    newTable[i] = -1;
		}
	    }
            orderedChildIdTable = newTable;
        }
        orderedChildIdTable[orderedId] = childId;
	//printTable(orderedChildIdTable);

    }

    void updateChildIdTableRemoved(int childId ) {
	// If the orderedGroup itself has been clearLived, then the ids
	// have been returned, if only some of the children of the
	// OGs is removed, then removed the specific entries
	// from the table
	if (orderedChildIdTable == null)
	    return;

	for (int i=0; i<orderedChildIdTable.length; i++) {
	    if (orderedChildIdTable[i] != -1) {
		if (orderedChildIdTable[i] > childId) {
		    orderedChildIdTable[i]--; // shift downward
		}
		else if (orderedChildIdTable[i] == childId) {
		    orderedChildIdTable[i] = -1;
		    //System.err.println("og.updateChildIdTableRemoved freeId " + i);
		    freeOrderedChildId(i);
		}
	    }
	}

    }

    void setAuxData(SetLiveState s, int index, int hkIndex) {
        OrderedPath setLiveStateOrderedPath, newOrderedPath;
        ArrayList childOrderedPaths;

        setLiveStateOrderedPath = (OrderedPath) s.orderedPaths.get(hkIndex);
        for (int i=0; i<children.size(); i++) {
		NodeRetained child = children.get(i);
            if (refCount == s.refCount) {
                // only need to do it once if in shared group when the first
                // instances is to be added
                child.orderedId = getOrderedChildId();
            }

            newOrderedPath = setLiveStateOrderedPath.clonePath();
            newOrderedPath.addElementToPath(this, child.orderedId);
            childOrderedPaths = (ArrayList)childrenOrderedPaths.get(i);
            childOrderedPaths.add(hkIndex, newOrderedPath);
        }
    }


    void setLive(SetLiveState s) {
	super.setLive(s);
        s.orderedPaths = orderedPaths;
	if((userChildIndexOrder != null) && (refCount == 1)) {

	    // Don't send a message for initial set live.
	    int[]newArr = new int[userChildIndexOrder.length];
	    System.arraycopy(userChildIndexOrder, 0, newArr,
			     0, userChildIndexOrder.length);
	    childIndexOrder = newArr;
	}
    }

    void clearLive(SetLiveState s) {
	super.clearLive(s);
	// This is used to clear the childIdTable and set the orderedBin
	// for all views to be null

        if (refCount == 0) {
	    s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT;
            // only need to do it once if in shared group
            s.nodeList.add(this);
	    s.ogCIOList.add(this);
	    s.ogCIOTableList.add(null);
	    userChildIndexOrder = null;
        }
        s.orderedPaths = orderedPaths;
    }

    void setNodeData(SetLiveState s) {
        super.setNodeData(s);
        if (!inSharedGroup) {
            setAuxData(s, 0, 0);
        } else {
            // For inSharedGroup case.
            int j, hkIndex;

            for(j=0; j<s.keys.length; j++) {
                hkIndex = s.keys[j].equals(localToVworldKeys, 0,
                                                localToVworldKeys.length);

                if(hkIndex >= 0) {
                    setAuxData(s, j, hkIndex);

                } else {
                    MasterControl.getCoreLogger().severe("Can't Find matching hashKey in setNodeData.");
                }
            }
        }
        // Note s.orderedPaths is to be updated in GroupRetained.setLive
        // for each of its children
    }

    void removeNodeData(SetLiveState s) {

        if((inSharedGroup) && (s.keys.length != localToVworld.length)) {
            int i, index;
            ArrayList childOrderedPaths;

            // Must be in reverse, to preserve right indexing.
            for (i = s.keys.length-1; i >= 0; i--) {
                index = s.keys[i].equals(localToVworldKeys, 0,
                                        localToVworldKeys.length);
                if(index >= 0) {
                    for (int j=0; j<children.size(); j++) {
                        childOrderedPaths = (ArrayList)childrenOrderedPaths.get(j);
                        childOrderedPaths.remove(index);
                    }
                }
            }
            // Note s.orderedPaths is to be updated in GroupRetained.clearLive
            // for each of its children
        }
        super.removeNodeData(s);
    }


    // This node has been cleared, so
    void clearDerivedDataStructures() {
	int i;

	// Clear the orderedBin and childId table for all views
	// since this orderedGroup has been clearLived!
	synchronized (lockObj) {
		for (i = 0; i < orderedBin.length; i++) {
			if (orderedBin[i] == null)
				continue;
			orderedBin[i].source = null;
			orderedBin[i] = null;
		}
	}
	if (orderedChildIdTable != null) {
	    for (i=0; i<orderedChildIdTable.length; i++) {
		if (orderedChildIdTable[i] != -1) {
		    orderedChildIdTable[i] = -1;
		    //System.err.println("og.clearDerivedDataStructures freeId " + i);
		    freeOrderedChildId(i);
		}
	    }
	    orderedChildIdTable = null;
	}
    }

    void incrChildCount() {
	childCount++;
    }


    void decrChildCount() {
	childCount--;
    }

    void printTable(int[] table) {
	for (int i=0; i<table.length; i++) {
	    System.err.print(" " + table[i]);
	}
	System.err.println("");
   }

    void insertChildrenData(int index) {
        childrenOrderedPaths.add(index, new ArrayList(1));
    }

    void appendChildrenData() {
        childrenOrderedPaths.add(new ArrayList(1));
    }

    void doRemoveChild(int index, J3dMessage messages[], int messageIndex) {

    	if(userChildIndexOrder != null) {
	    doRemoveChildIndexEntry(index);
	}

	super.doRemoveChild(index, messages, messageIndex);

    }

    void removeChildrenData(int index) {
        childrenOrderedPaths.remove(index);
    }

    void childDoSetLive(NodeRetained child, int childIndex, SetLiveState s) {
        if (refCount == s.refCount) {
            s.ogList.add(this);
            s.ogChildIdList.add(new Integer(childIndex));
            s.ogOrderedIdList.add(child.orderedId);
        }
        s.orderedPaths = (ArrayList)childrenOrderedPaths.get(childIndex);
        if(child!=null)
            child.setLive(s);
    }

    void childCheckSetLive(NodeRetained child, int childIndex,
                                SetLiveState s, NodeRetained linkNode) {
        OrderedPath childOrderedPath;
        ArrayList childOrderedPaths;

        if (linkNode != null) {
            int ci = children.indexOf(linkNode);
            childOrderedPaths = (ArrayList)childrenOrderedPaths.get(ci);
        } else {
            child.orderedId = getOrderedChildId();
            // set this regardless of refCount
            s.ogList.add(this);
            s.ogChildIdList.add(new Integer(childIndex));
            s.ogOrderedIdList.add(child.orderedId);

	    if(userChildIndexOrder != null) {
		s.ogCIOList.add(this);
		int[] newArr = new int[userChildIndexOrder.length];
		System.arraycopy(userChildIndexOrder, 0, newArr,
				 0, userChildIndexOrder.length);

		s.ogCIOTableList.add(newArr);
	    }

	    childOrderedPaths = (ArrayList)childrenOrderedPaths.get(childIndex);

            for(int i=0; i< orderedPaths.size();i++){
                childOrderedPath =
                            ((OrderedPath)orderedPaths.get(i)).clonePath();
                childOrderedPath.addElementToPath(this, child.orderedId);
                childOrderedPaths.add(childOrderedPath);
            }
        }
        s.orderedPaths = childOrderedPaths;
        child.setLive(s);
    }
}
