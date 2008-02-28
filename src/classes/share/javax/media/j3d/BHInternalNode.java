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

import javax.vecmath.*;

class BHInternalNode extends BHNode {
    
    static boolean debug2 = true;
    
    BHNode rChild;
    BHNode lChild;
    
    BHInternalNode() {
	super();
	nodeType = BH_TYPE_INTERNAL;
	this.rChild = null;
	this.lChild = null;
    }
    
    BHInternalNode(BHNode parent) {
	super(parent);
	nodeType = BH_TYPE_INTERNAL;
	this.rChild = null;
	this.lChild = null;    
    }
    
    BHInternalNode(BHNode parent, BHNode rChild, BHNode lChild) {
	super(parent);
	nodeType = BH_TYPE_INTERNAL;
	this.rChild = rChild;
	this.lChild = lChild;    
    }
    
    BHInternalNode(BHNode parent, BoundingBox bHull) {
	super(parent, bHull);
	nodeType = BH_TYPE_INTERNAL;
	this.rChild = null;
	this.lChild = null;        
    }
    
    BHInternalNode(BHNode parent, BHNode rChild, BHNode lChild, BoundingBox bHull) {
	super(parent, bHull);
	nodeType = BH_TYPE_INTERNAL;
	this.rChild = rChild;
	this.lChild = lChild;    
    }
    
    BHNode getLeftChild() {
	return (BHNode) lChild;
    }
    
    BHNode getRightChild() {
	return (BHNode) rChild;
    }
    
    void setLeftChild(BHNode child) {
	lChild = child;
    }
    
    void setRightChild(BHNode child) {
	rChild = child;
    }
    
    void computeBoundingHull(BoundingBox bHull) {
	computeBoundingHull();
	bHull.set(this.bHull);
    }
 
    void computeBoundingHull() {
	BoundingBox rChildBound = null;
	BoundingBox lChildBound = null;
	int i;
	
	if((lChild==null) && (rChild==null)) {
	    bHull = null;
	    return;
	}
	
	if(lChild != null)
	    lChildBound = lChild.getBoundingHull();
	
	if(rChild != null)
	    rChildBound = rChild.getBoundingHull();
	
	if(bHull == null)
	    bHull = new BoundingBox();    
	
	// Since left child is null. bHull is equal to right child's Hull.
	if(lChild == null) {
	    bHull.set(rChildBound);
	    return;
	}
	
	// Since right child is null. bHull is equal to left child's Hull.
	if(rChild == null) {
	    bHull.set(lChildBound);
	    return;
	}

	// Compute the combined bounds of the children.
	bHull.set(rChildBound);
	bHull.combine(lChildBound);
	
    }
    
    void updateMarkedBoundingHull() {
	
	if(mark == false)
	    return;
	
	rChild.updateMarkedBoundingHull();
	lChild.updateMarkedBoundingHull();
	computeBoundingHull();
	mark = false;
	
    }
  
    // this method inserts a single element into the tree given the stipulation
    // that the current tree node already contains the child ... 3 cases
    // one --node is inside the left child, and not inside the right
    // so recurse placing it inside the left child
    // two -- node is not inside the left but is inside the right
    // recurse placing it inside the right child
    // three -- node is not inside either one, added it to the current
    // element
    
    void insert( BHNode node, BHInsertStructure insertStructure ) {
	// NOTE: the node must already be inside this node if its not then fail.
	if(debug2)
	    if ( !this.isInside(node.bHull) ) {
		System.err.println("Incorrect use of insertion, current node");
		System.err.println("must contain the input element ...");
	    }
	
	boolean insideRightChild = false;
	boolean insideLeftChild = false;
	
	// leaf children are considered inpenetrable for insert so returns false
	if(this.rChild.nodeType == BHNode.BH_TYPE_LEAF) {
	    insideRightChild = false;
	} else {
	    insideRightChild = this.rChild.isInside(node.bHull);
	}
	if(this.lChild.nodeType == BHNode.BH_TYPE_LEAF) {
	    insideLeftChild  = false;
	} else {
	    insideLeftChild  = this.lChild.isInside(node.bHull);
	}
	
	if ( insideLeftChild && !insideRightChild ) {
	    ((BHInternalNode)this.lChild).insert(node, insertStructure);
	} else if ( !insideLeftChild && insideRightChild ) {
	    ((BHInternalNode)this.rChild).insert(node, insertStructure);
	} else if ( insideLeftChild && insideRightChild ) {
	    // choose randomly to put it in the left or right
	    if ( insertStructure.randomNumber.nextBoolean() ) {
		((BHInternalNode)this.lChild).insert(node, insertStructure);
	    } else {
		((BHInternalNode)this.rChild).insert(node, insertStructure);
	    }
	} else {
	    // doesn't fit in either one ....
	    // lookup the current node this in the auxilaryInsertStructure
	    // if it appears then add element to the array of sub elements
	    // if not then allocate a new element to the array
	    insertStructure.lookupAndInsert(this, node);
	}
    }   

    void destroyTree(BHNode[] bhArr, int[] index) {
	
	if(rChild != null)
	    rChild.destroyTree(bhArr, index);
	
	if(lChild != null)
	    lChild.destroyTree(bhArr, index);
	
	rChild = null;
	lChild = null;
    }
}

