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


abstract class BHNode {

    static final byte BH_TYPE_INTERNAL             = 1;
    static final byte BH_TYPE_LEAF                 = 2;

    static final int NUMBER_OF_PLANES              = 6;

    static final boolean debug = false;
    static final boolean debug2 = false;

    BHNode parent;
    byte   nodeType;
    BoundingBox bHull = null;
    boolean mark;

    BHNode () {
	this.parent = null;
	mark = false;
    }

    BHNode (BHNode parent) {
	this.parent = parent;
	mark = false;
    }

    BHNode (BHNode parent, BoundingBox bHull) {
	this.parent = parent;
	mark = false;

	this.bHull = bHull;
    }

    BHNode getParent () {
	return (this.parent) ;
    }

    abstract void computeBoundingHull();
    abstract void updateMarkedBoundingHull();
    abstract void destroyTree(BHNode[] bhArr, int[] index);

    void setParent (BHNode node) {
	this.parent = node;
    }

    BoundingBox getBoundingHull() {
	return (this.bHull);
    }

    void setBoundingHull(BoundingBox bHull) {
	this.bHull = bHull;
    }

    // given two nodes determine the bHull surrounding them, ie. the parent hull
    void combineBHull(BHNode node1, BHNode node2 ) {
	BoundingBox bHull1 = null;
	BoundingBox bHull2 = null;

	bHull1 = node1.getBoundingHull();
	bHull2 = node2.getBoundingHull();

	if(this.bHull==null)
	    this.bHull = new BoundingBox(bHull1);
	else
	    this.bHull.set(bHull1);

	this.bHull.combine(bHull2);

    }

    // returns true iff the bHull is completely inside this
    // bounding hull i.e.  bHull values are strictly less
    // than or equal to all this.bHull values
    boolean isInside(BoundingBox bHull) {
	if(bHull == null)
	    return false;

	if( this.bHull.isEmpty() || bHull.isEmpty() ) {
	    return false;
        }

	if( this.bHull.upper.x < bHull.upper.x ||
	    this.bHull.upper.y < bHull.upper.y ||
	    this.bHull.upper.z < bHull.upper.z ||
	    this.bHull.lower.x > bHull.lower.x ||
	    this.bHull.lower.y > bHull.lower.y ||
	    this.bHull.lower.z > bHull.lower.z )
	    return false;
	else
	    return true;
    }

    // finds the node matching the search element in the tree and returns
    // the node if found, else it returns null if the node couldn't be found
    BHNode findNode(BHNode node) {
	BHNode fNode = null;

	if ( this.nodeType == BHNode.BH_TYPE_LEAF) {
	    if ( this == node ) {
		return this;
	    }
	}
	else {
	    if (((BHInternalNode) this).rChild.isInside(node.bHull)) {
		fNode = ((BHInternalNode)this).rChild.findNode(node);
		if(fNode != null) {
		    return fNode;
		}
	    }
	    if (((BHInternalNode)this).lChild.isInside(node.bHull)) {
		return ((BHInternalNode)this).lChild.findNode(node);
	    }
	}
	return null;
    }

    void deleteFromParent() {
	BHInternalNode parent;

	// System.err.println("deleteFromParent - this " + this );
	parent = (BHInternalNode) (this.parent);
	if(parent != null) {
	    if(parent.rChild == this)
		parent.rChild = null;
	    else if(parent.lChild == this)
		parent.lChild = null;
	    else {
		if(debug2) {
		    System.err.println("BHNode.java: Trouble! No match found. This can't happen.");
		    System.err.println("this " + this );
		    if ( this.nodeType == BHNode.BH_TYPE_INTERNAL) {
			System.err.println("rChild " + ((BHInternalNode)this).rChild +
					   " lChild " + ((BHInternalNode)this).lChild);
		    }
		    System.err.println("parent " + parent +
				       " parent.rChild " + parent.rChild +
				       " parent.lChild " + parent.lChild);
		}
	    }
	}
    }

    // delete all leaf nodes marked with DELETE_UPDATE and update the
    // bounds of the parents node
    BHNode deleteAndUpdateMarkedNodes() {

	if (this.mark == true) {
	    if (this.nodeType == BH_TYPE_LEAF) {
		this.deleteFromParent();
		return null;

	    } else {
		if(debug)
		    if(((BHInternalNode)(this)).rChild == ((BHInternalNode)(this)).lChild)
			System.err.println("rChild " + ((BHInternalNode)(this)).rChild +
					   " lChild " + ((BHInternalNode)(this)).lChild);


		if(((BHInternalNode)(this)).rChild != null)
		    ((BHInternalNode)(this)).rChild =
			((BHInternalNode)(this)).rChild.deleteAndUpdateMarkedNodes();
		if(((BHInternalNode)(this)).lChild != null)
		    ((BHInternalNode)(this)).lChild =
			((BHInternalNode)(this)).lChild.deleteAndUpdateMarkedNodes();

		if ((((BHInternalNode)(this)).rChild == null) &&
		    (((BHInternalNode)(this)).lChild == null)) {
		    this.deleteFromParent();
		    return null;
		} else {
		    if ( ((BHInternalNode)this).rChild == null ) {
			BHNode leftChild = ((BHInternalNode)this).lChild;
			leftChild.parent = this.parent;
			// delete self, return lChild
			this.deleteFromParent();
			return leftChild;
		    } else if ( ((BHInternalNode)this).lChild == null ) {
			BHNode rightChild = ((BHInternalNode)this).rChild;
			rightChild.parent = this.parent;
			// delete self, return rChild
			this.deleteFromParent();
			return rightChild;
		    } else {
			// recompute your bounds and return yourself
			this.combineBHull(((BHInternalNode)this).rChild,
					       ((BHInternalNode)this).lChild);
			// update the parent's pointers
			((BHInternalNode)this).rChild.parent = this;
			((BHInternalNode)this).lChild.parent = this;
			this.mark = false;
			return this;
		    }
		}
	    }
	} else {
	    // mark is NOT set, simply return self
	    return this;
	}
    }


    // generic tree gathering statistics operations

    int countNumberOfInternals() {
	if ( this.nodeType == BHNode.BH_TYPE_LEAF ) {
	    return 0;
	} else {
	    return (((BHInternalNode)this).rChild.countNumberOfInternals() +
		    ((BHInternalNode)this).lChild.countNumberOfInternals() + 1 );
	}
    }

    // recursively traverse the tree and compute the total number of leaves
    int countNumberOfLeaves() {
	if ( this.nodeType == BHNode.BH_TYPE_LEAF ) {
	    return 1;
	} else {
	    return ( ((BHInternalNode)this).rChild.countNumberOfLeaves() +
		     ((BHInternalNode)this).lChild.countNumberOfLeaves() );
	}
    }


    // traverse tree and compute the maximum depth to a leaf
    int computeMaxDepth (int currentDepth) {
	if ( this.nodeType == BHNode.BH_TYPE_LEAF ) {
	    return (currentDepth);
	} else {
	    int rightDepth = ((BHInternalNode)this).rChild.computeMaxDepth(currentDepth + 1);
	    int leftDepth = ((BHInternalNode)this).lChild.computeMaxDepth(currentDepth + 1);
	    if( rightDepth > leftDepth )
		return rightDepth;
	    return leftDepth;
	}
    }

    // compute the average depth of the leaves ...
    float computeAverageLeafDepth ( int numberOfLeaves, int currentDepth ) {
	int sumOfDepths = this.computeSumOfDepths(0);
	return ( (float)sumOfDepths / (float)numberOfLeaves );
    }

    int computeSumOfDepths ( int currentDepth ) {
	if ( this.nodeType == BHNode.BH_TYPE_LEAF ) {
	    return ( currentDepth );
	} else {
	    return (((BHInternalNode)this).rChild.computeSumOfDepths(currentDepth + 1) +
		    ((BHInternalNode)this).lChild.computeSumOfDepths(currentDepth + 1) ) ;
	}
    }


}
