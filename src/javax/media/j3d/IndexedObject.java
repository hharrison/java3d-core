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


