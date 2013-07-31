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

package javax.media.j3d;


class BHLeafNode extends BHNode {

    BHLeafInterface leafIF;

    BHLeafNode() {
	super();
	nodeType = BH_TYPE_LEAF;
	leafIF = null;
    }

    BHLeafNode(BHNode parent) {
	super(parent);
	nodeType = BH_TYPE_LEAF;
    }

    BHLeafNode(BHLeafInterface lIF) {
	super();
	nodeType = BH_TYPE_LEAF;
	leafIF = lIF;
    }

    BHLeafNode(BHNode parent, BHLeafInterface lIF) {
	super(parent);
	leafIF = lIF;
	nodeType = BH_TYPE_LEAF;
    }

    BHLeafNode(BHNode parent, BoundingBox bHull) {
	super(parent, bHull);
	nodeType = BH_TYPE_LEAF;
    }

    BHLeafNode(BHNode parent, BHLeafInterface lIF, BoundingBox bHull) {
	super(parent, bHull);
	leafIF = lIF;
	nodeType = BH_TYPE_LEAF;
    }

    @Override
    void computeBoundingHull() {
	bHull = leafIF.computeBoundingHull();
    }

    @Override
    void updateMarkedBoundingHull() {

	if(mark == false)
	    return;

	computeBoundingHull();
	mark = false;
    }

    boolean isEnable() {
	return leafIF.isEnable();
    }

    boolean isEnable(int vis) {
	return leafIF.isEnable(vis);
    }

    Locale getLocale() {
	return leafIF.getLocale2();
    }

    @Override
    void destroyTree(BHNode[] bhArr, int[] index) {
	if(bhArr.length <= index[0]) {
	    // System.err.println("BHLeafNode : Problem bhArr overflow!!!");
	    return;
	}

	parent = null;
	bhArr[index[0]] = this;
	index[0]++;
    }

}
