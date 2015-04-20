/*
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
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


class FreeListManager {

    private static final boolean DEBUG = false;

    // constants that represent the freelists managed by the Manager
    static final int DISPLAYLIST = 0;

    private static int maxFreeListNum = 0;

    // what list we are going to shrink next
    private static int currlist = 0;

    static MemoryFreeList[] freelist = null;

    static void createFreeLists() {
        maxFreeListNum = 0;
        freelist = new MemoryFreeList[maxFreeListNum+1];
        freelist[DISPLAYLIST] = new IntegerFreeList();
    }

    // see if the current list can be shrunk
    static void manageLists() {
// 	System.err.println("manageLists");
	if (freelist[currlist] != null) {
	    freelist[currlist].shrink();
	}

	currlist++;
	if (currlist > maxFreeListNum) currlist = 0;
    }

    // return the freelist specified by the list param
    static MemoryFreeList getFreeList(int list) {
	if (list < 0 || list > maxFreeListNum) {
	    if (DEBUG) System.err.println("illegal list");
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
