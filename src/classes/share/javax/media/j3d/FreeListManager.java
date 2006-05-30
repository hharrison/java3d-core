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


class FreeListManager {

    private static final boolean DEBUG = false;

    // constants that represent the freelists managed by the Manager
    static final int DISPLAYLIST = 0;
    static final int TEXTURE2D = 1;
    static final int TEXTURE3D = 2;    
    static final int MESSAGE = 3;
    static final int BHLEAF = 4;
    static final int TRANSFORM3D = 5;
    static final int BHINTERNAL = 6;
    static final int VECTOR3D = 7;
    static final int POINT3D = 8;

    private static int maxFreeListNum = 8;
    
    // what list we are going to shrink next
    private static int currlist = 0;

    static MemoryFreeList[] freelist = null;
    
    static void createFreeLists(boolean useFreeLists) {
        if (useFreeLists) {
            freelist = new MemoryFreeList[maxFreeListNum+1];
            freelist[DISPLAYLIST] = new IntegerFreeList();
            freelist[TEXTURE2D] = new IntegerFreeList();
            freelist[TEXTURE3D] = new IntegerFreeList();
            freelist[MESSAGE] = new MemoryFreeList("javax.media.j3d.J3dMessage");
            freelist[BHLEAF] = new MemoryFreeList("javax.media.j3d.BHLeafNode");
            freelist[TRANSFORM3D] = new MemoryFreeList("javax.media.j3d.Transform3D");
            freelist[BHINTERNAL] = new MemoryFreeList("javax.media.j3d.BHInternalNode");
            freelist[POINT3D] = new MemoryFreeList("javax.vecmath.Point3d");
            freelist[VECTOR3D] = new MemoryFreeList("javax.vecmath.Vector3d");
        }
        else {
            // Fix to Issue 123       
            maxFreeListNum = 2;
            freelist = new MemoryFreeList[maxFreeListNum+1];
            freelist[DISPLAYLIST] = new IntegerFreeList();
            freelist[TEXTURE2D] = new IntegerFreeList();
            freelist[TEXTURE3D] = new IntegerFreeList();            
        }
    }

    // see if the current list can be shrunk
    static void manageLists() {
// 	System.out.println("manageLists");
	if (freelist[currlist] != null) {
	    freelist[currlist].shrink();
	}
	
	currlist++;
	if (currlist > maxFreeListNum) currlist = 0;
    }

    // return the freelist specified by the list param
    static MemoryFreeList getFreeList(int list) {
	if (list < 0 || list > maxFreeListNum) {
	    if (DEBUG) System.out.println("illegal list");
	    return null;
	}
	else {
	    return freelist[list];
	}
    }

    static Object getObject(int listId) {
	return freelist[listId].getObject();
    }

    static void freeObject(int listId, Object obj) {
	freelist[listId].add(obj);
    }

    static void clearList(int listId) {
	freelist[listId].clear();
    }

}
