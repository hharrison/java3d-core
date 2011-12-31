/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * List of orderedGroup children that needs to be added/removed for
 * the next frame. Note that the order in which they are removed and
 * added should be maintained after the renderer is done to get the
 * correct order of rendering.
 */
class OrderedChildInfo extends Object {

    static int ADD      = 0x1;
    static int REMOVE   = 0x2;


    /**
     * Type of operation, could be add/remove or set
     */
    int               type;

    /**
     * Ordered index at which this operation takes place
     */
    int               orderedId;

    /**
     * Child index at which this operation takes place
     */
    int               childId;

    /**
     * Value of the orderedCollection, only relavent for
     * add and set
     */
    OrderedCollection            value;


    // Maintains the order in which the ordered children
    // were added and removed
    OrderedChildInfo next;
    OrderedChildInfo prev;

    OrderedChildInfo(int t, int cid, int oid, OrderedCollection val) {
	type = t;
	orderedId = oid;
	childId = cid;
	value = val;
	prev = null;
	next = null;

    }

}
