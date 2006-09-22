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

import java.util.ArrayList;
import java.util.Vector;
import javax.vecmath.Point4d;

class BHTree  {
    
    Locale locale;
    BHNode root;
    BHInsertStructure insertStructure = null;

    
    // Temporary point, so we dont generate garbage
    Point4d tPoint4d = new Point4d();    
    
    // A flag to signal that number of renderAtoms sent to RenderBin is stable.
    private boolean stable = false;

    // An estimate of the maxmium depth of this tree (upper bound).
    int estMaxDepth;

    static final double LOG_OF_2 = Math.log(2);

    // Assume that the size avg. leaf node is 256 bytes. For a 64bit system, we'll
    // down with max depth of 56 for an ideal balance tree.
    static final int DEPTH_UPPER_BOUND = 56;
    static final int INCR_DEPTH_BOUND = 5;
    int  depthUpperBound = DEPTH_UPPER_BOUND;
    
    BHTree() {
	locale = null;
	root = null;
    }
    
    BHTree(Locale loc) {
	locale = loc;
	root = null;
    }

    BHTree(BHNode bhArr[]) {
	locale = null;
	root = null;
	create(bhArr);
    }

    void setLocale(Locale loc) {
	locale = loc;
    }

    Locale getLocale() {
	return locale;
    }
    
    void cluster(BHInternalNode root, BHNode[] bhArr) {

	if(J3dDebug.devPhase) {
	    if(J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
				"BHTree.java :In cluster length is " + bhArr.length
				+ "\n")) {	    
		
		for(int i=0; i<bhArr.length; i++) {
		    System.out.println(bhArr[i]);
		}
	    }
	}
	
	if((bhArr == null) || (bhArr.length < 2) || (root == null)){
	    if(J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_1,
				 "BHTree.java : cluster : trouble! \n");
	    return;
	}
	
	int centerValuesIndex[] = new int[bhArr.length];
	float centerValues[][] = computeCenterValues(bhArr, centerValuesIndex);
	
	constructTree(root, bhArr, centerValues, centerValuesIndex);  
	
    }
    
    // bhArr can only contains BHLeafNode.

    void boundsChanged(BHNode bhArr[], int size) {
	// Mark phase.
	markParentChain(bhArr, size);
	
	// Compute phase.
	root.updateMarkedBoundingHull();
    }

    
    // Return true if bhTree's root in encompass by frustumBBox and nothing changed.
    boolean getVisibleBHTrees(RenderBin rBin, ArrayList bhTrees,
			      BoundingBox frustumBBox, long referenceTime,
			      boolean stateChanged, int visibilityPolicy,
			      boolean singleLocale) {
	
	int i, j, size;
	
	if ((frustumBBox != null) && (root != null)) {
	    
	    boolean inSide = aEncompassB(frustumBBox, root.bHull);
	    /*
	      System.out.println("stateChanged is " + stateChanged); 
	      System.out.println("frustumBBox is " + frustumBBox);
	      System.out.println("root.bHull is " + root.bHull);
	      System.out.println("inSide is " + inSide);
	    */
	     
	    if(singleLocale && !stateChanged && inSide && stable) {
		// just return the whole tree, no change in render mol..
		// System.out.println("Optimize case 1 ..." + this);
		bhTrees.add(root);
		return true;
	    }
	    else if(!stateChanged && inSide) {
		// the whole tree is in, but we've to be sure that RenderBin is
		// stable ...
		// System.out.println("Optimize case 2 ..." + this);
		select(rBin, bhTrees, frustumBBox, root, referenceTime,
		       visibilityPolicy, true);
		
		bhTrees.add(root);
		stable = true;
	    } else {
		// System.out.println("Not in Optimize case ..." + this);
		select(rBin, bhTrees, frustumBBox, root, referenceTime,
		       visibilityPolicy, false);

		stable = false;
	    }

	}

	return false;
    }
    
    private void select(RenderBin rBin, ArrayList bhTrees, BoundingBox frustumBBox,
			BHNode bh, long referenceTime, int visibilityPolicy,
			boolean inSide) {

	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return;
	}
	
	switch(bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    if((((BHLeafNode) bh).leafIF instanceof GeometryAtom) &&
	       (((BHLeafNode) bh).isEnable(visibilityPolicy)) &&
	       ((inSide) ||  (frustumBBox.intersect(bh.bHull)))) {
		
	       // do render atom setup.
		rBin.processGeometryAtom((GeometryAtom)
					 (((BHLeafNode)bh).leafIF),
					 referenceTime);
		if(!inSide) {
		    bhTrees.add(bh);	
		}
	    }
	    break;
	case BHNode.BH_TYPE_INTERNAL:
	    if(inSide) {
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getRightChild(),
		       referenceTime, visibilityPolicy, true);
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getLeftChild(),
		       referenceTime, visibilityPolicy, true);	
	    }
	    else if(aEncompassB(frustumBBox, bh.bHull)) {
		bhTrees.add(bh);	
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getRightChild(),
		       referenceTime, visibilityPolicy, true);
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getLeftChild(),
		       referenceTime, visibilityPolicy, true);
	    }  
	    else if(frustumBBox.intersect(bh.bHull)) {
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getRightChild(),
		       referenceTime, visibilityPolicy, false);
		select(rBin, bhTrees, frustumBBox,
		       ((BHInternalNode)bh).getLeftChild(),
		       referenceTime, visibilityPolicy, false);
	    }
	    break;		  	 
	}
    }    
    
    // returns true iff the bBox is completely inside aBox
    // i.e.  bBoxl values are strictly less than or equal to all aBox values.
    static boolean aEncompassB(BoundingBox aBox, BoundingBox bBox) {
	return ((aBox.upper.x >= bBox.upper.x) &&
		(aBox.upper.y >= bBox.upper.y) &&
		(aBox.upper.z >= bBox.upper.z) &&
		(aBox.lower.x <= bBox.lower.x) &&
		(aBox.lower.y <= bBox.lower.y) &&
		(aBox.lower.z <= bBox.lower.z));
    }

    
    BHLeafInterface selectAny(GeometryAtom atom, int accurancyMode) {
	if (atom.source.geometryList == null)
	    return null;
	BHNode bhNode = doSelectAny(atom, root, accurancyMode);
	if (bhNode == null) {
	    return null;
	}

	return ((BHLeafNode) bhNode).leafIF;
    }

    
    BHLeafInterface selectAny(GeometryAtom atoms[], int size, int accurancyMode) {
	BHNode bhNode = doSelectAny(atoms, size, root, accurancyMode);
	if (bhNode == null) {
	    return null;
	}

	return ((BHLeafNode) bhNode).leafIF;
    }


    private BHNode doSelectAny(GeometryAtom atoms[],int atomSize,
			       BHNode bh, int accurancyMode) {
	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return null;
	}
	switch (bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    BHLeafInterface leaf = ((BHLeafNode) bh).leafIF;
	    GeometryAtom atom;
	    int i;

	    if (leaf instanceof GeometryAtom) {
		GeometryAtom leafAtom = (GeometryAtom) leaf;

		if (((BHLeafNode) bh).isEnable() &&
		    leafAtom.source.isCollidable) {

		    // atom self intersection between atoms[]
		    for (i=atomSize-1; i >=0; i--) {
			if (atoms[i] == leafAtom) {
			    return null;
			}
		    }
		    for (i=atomSize-1; i >=0; i--) {
			atom = atoms[i];
			if ((atom.source.sourceNode != leafAtom.source.sourceNode) &&
			    (atom.source.collisionVwcBound.intersect(leafAtom.source.collisionVwcBound)) &&
			    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
			     ((leafAtom.source.geometryList != null) && 
			      (atom.source.intersectGeometryList(leafAtom.source))))) {
			    return bh;
			}
		    }
		}
	    } else if (leaf instanceof GroupRetained) {
		if (((BHLeafNode) bh).isEnable() &&
		    ((GroupRetained) leaf).sourceNode.collidable) {
		    for (i=atomSize-1; i >=0; i--) {
			atom = atoms[i];
			if (atom.source.collisionVwcBound.intersect(bh.bHull) &&
			    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
			     (atom.source.intersectGeometryList(
			     atom.source.getCurrentLocalToVworld(0), bh.bHull)))) {
			    return bh;
			}
		    }
		}
	    }
	    return null;
	case BHNode.BH_TYPE_INTERNAL:
	    for (i=atomSize-1; i >=0; i--) {
		atom = atoms[i];
		if (atom.source.collisionVwcBound.intersect(bh.bHull))
		    {
			BHNode hitNode = doSelectAny(atoms, 
						     atomSize,
						     ((BHInternalNode) bh).getRightChild(),
						     accurancyMode);
			if (hitNode != null)
			    return hitNode;
		
			return doSelectAny(atoms, atomSize,
					   ((BHInternalNode) bh).getLeftChild(),
					   accurancyMode);
		    }
	    }
	    return null;
	}
	return null;
    }


    private BHNode doSelectAny(GeometryAtom atom, BHNode bh, int accurancyMode) {
	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return null;
	}
	switch (bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    BHLeafInterface leaf = ((BHLeafNode) bh).leafIF;
	    if (leaf instanceof GeometryAtom) {
		GeometryAtom leafAtom = (GeometryAtom) leaf;
		if ((atom.source.sourceNode != leafAtom.source.sourceNode) &&
		    (((BHLeafNode) bh).isEnable()) &&
		    (leafAtom.source.isCollidable) && 
		    (atom.source.collisionVwcBound.intersect(leafAtom.source.collisionVwcBound)) &&
		    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
		     ((leafAtom.source.geometryList != null) && 
		      (atom.source.intersectGeometryList(leafAtom.source))))) {
		    return bh;
		}
	    } else if (leaf instanceof GroupRetained) {
		if (((BHLeafNode) bh).isEnable() &&
		    ((GroupRetained) leaf).sourceNode.collidable &&
		    atom.source.collisionVwcBound.intersect(bh.bHull) &&
		    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
		     (atom.source.intersectGeometryList(
			atom.source.getCurrentLocalToVworld(0), bh.bHull)))) {
		    return bh;
		}
	    }
	    return null;
	case BHNode.BH_TYPE_INTERNAL:
	    if (atom.source.collisionVwcBound.intersect(bh.bHull)) {		
		BHNode hitNode = doSelectAny(atom, 
					     ((BHInternalNode) bh).getRightChild(),
					     accurancyMode);
		if (hitNode != null)
		    return hitNode;
		
		return doSelectAny(atom,
				   ((BHInternalNode) bh).getLeftChild(),
				   accurancyMode);
	    }
	    return null;
	}
	return null;
    }

    BHLeafInterface selectAny(Bounds bound, int accurancyMode,
			      NodeRetained armingNode) {
	if (bound == null) {
	    return null;
	}
	BHNode bhNode = doSelectAny(bound, root, accurancyMode, armingNode);
	if (bhNode == null) {
	    return null;
	}
	return ((BHLeafNode) bhNode).leafIF;
    }

    private BHNode doSelectAny(Bounds bound, BHNode bh, int accurancyMode,
			       NodeRetained armingNode) {
	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return null;
	}

	switch (bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    BHLeafInterface leaf = ((BHLeafNode) bh).leafIF;
	    if (leaf instanceof GeometryAtom) {
		GeometryAtom leafAtom = (GeometryAtom) leaf;
		if ((((BHLeafNode) bh).isEnable()) &&
		    (leafAtom.source.isCollidable) && 
		    (bound.intersect(leafAtom.source.collisionVwcBound)) &&
		    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
		     ((leafAtom.source.geometryList != null) && 
		      (leafAtom.source.intersectGeometryList(
			leafAtom.source.getCurrentLocalToVworld(0), bound))))) {
		    return bh;
		}
	    } else if (leaf instanceof GroupRetained) {
		if ((leaf != armingNode) &&
		    ((BHLeafNode) bh).isEnable() &&
		    ((GroupRetained) leaf).sourceNode.collidable &&
		    bound.intersect(bh.bHull)) {
		    return bh;
		}
	    }
	    return null;
	case BHNode.BH_TYPE_INTERNAL:
	    if (bound.intersect(bh.bHull)) {		
		BHNode hitNode = doSelectAny(bound,
				      ((BHInternalNode) bh).getRightChild(),
				      accurancyMode,
				      armingNode);
		if (hitNode != null)
		    return hitNode;
		
		return doSelectAny(bound,
				   ((BHInternalNode) bh).getLeftChild(),
				   accurancyMode,
				   armingNode);
	    }
	    return null;
	}
	return null;
    }


    BHLeafInterface selectAny(Bounds bound, int accurancyMode,
			      GroupRetained armingGroup) {
	if (bound == null) {
	    return null;
	}
	BHNode bhNode = doSelectAny(bound, root, accurancyMode, armingGroup);
	if (bhNode == null) {
	    return null;
	}
	return ((BHLeafNode) bhNode).leafIF;
    }

    private BHNode doSelectAny(Bounds bound, BHNode bh, int accurancyMode,
			       GroupRetained armingGroup) {
	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return null;
	}
	switch (bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    BHLeafInterface leaf = ((BHLeafNode) bh).leafIF;

	    if (leaf instanceof GeometryAtom) {
		GeometryAtom leafAtom = (GeometryAtom) leaf;
		if ((((BHLeafNode) bh).isEnable()) &&
		    (leafAtom.source.isCollidable) && 
		    (bound.intersect(leafAtom.source.collisionVwcBound)) &&
	 	    (!isDescendent(leafAtom.source.sourceNode,
				   armingGroup, leafAtom.source.key)) &&
		    ((accurancyMode == WakeupOnCollisionEntry.USE_BOUNDS) ||
		     ((leafAtom.source.geometryList != null) && 
		      (leafAtom.source.intersectGeometryList(
			leafAtom.source.getCurrentLocalToVworld(0), bound))))) {
		    return bh;
		}
	    } else if (leaf instanceof GroupRetained) {
		GroupRetained group = (GroupRetained) leaf;
		if (((BHLeafNode) bh).isEnable() &&
		    group.sourceNode.collidable &&
		    bound.intersect(bh.bHull) &&
		    !isDescendent(group.sourceNode, armingGroup, group.key)) {
			return bh;
		}
	    }
	    return null;
	case BHNode.BH_TYPE_INTERNAL:
	    if (bound.intersect(bh.bHull)) {		
		BHNode hitNode = doSelectAny(bound,
				      ((BHInternalNode) bh).getRightChild(),
				      accurancyMode,
				      armingGroup);
		if (hitNode != null)
		    return hitNode;
		
		return doSelectAny(bound,
				   ((BHInternalNode) bh).getLeftChild(),
				   accurancyMode,
				   armingGroup);
	    }
	    return null;
	}
	return null;
    }

    // Return true if node is a descendent of group
    private boolean isDescendent(NodeRetained node, 
				 GroupRetained group,
				 HashKey key) {

	synchronized (group.universe.sceneGraphLock) {
	    if (node.inSharedGroup) {
		// getlastNodeId() will destroy this key
		if (key != null) {
		    key = new HashKey(key);
		}
	    }
	    
	    do {
		if (node == group) {
		    return true;
		}
		if (node instanceof SharedGroupRetained) {
		    // retrieve the last node ID
		    String nodeId = key.getLastNodeId();
		    NodeRetained prevNode = node;
		    Vector parents = ((SharedGroupRetained) node).parents;
		    for(int i=parents.size()-1; i >=0; i--) {
			NodeRetained link = (NodeRetained) parents.elementAt(i);
			if (link.nodeId.equals(nodeId)) {
			    node = link;
			    break;
			}
		    }
		    if (prevNode == node) {
			// branch is already detach
			return true;
		    }
		}
		node = node.parent;
	    } while (node != null); // reach Locale
	}
	return false;
    }


    void select(PickShape pickShape, UnorderList hitArrList) {
	
	if((pickShape == null)||(root == null))
	    return;

	doSelect(pickShape, hitArrList, root, tPoint4d);
	
    }
    
    
    private void doSelect(PickShape pickShape, UnorderList hitArrList,
			  BHNode bh, Point4d pickPos) {
	
	if ((bh == null) || (bh.bHull.isEmpty())) {
	    return;
	}
	    
	switch(bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    if (((BHLeafNode)(bh)).isEnable() &&
		(((BHLeafNode) bh).leafIF instanceof GeometryAtom) &&
		 ((GeometryAtom) (((BHLeafNode)
				   bh).leafIF)).source.isPickable &&
		pickShape.intersect(bh.bHull, pickPos)) {
		hitArrList.add(bh);	 
	    }
	    break;
	case BHNode.BH_TYPE_INTERNAL:
	    if (pickShape.intersect(bh.bHull, pickPos)) {
		doSelect(pickShape, 
			 hitArrList,
			 ((BHInternalNode)bh).getRightChild(),
			 pickPos);
		doSelect(pickShape, 
			 hitArrList,
			 ((BHInternalNode)bh).getLeftChild(),
			 pickPos);
	    }
	    break;		  	 
	} 
    }

    BHNode selectAny(PickShape pickShape) {
	
	if((pickShape == null)||(root == null))
	    return null;
	
	return doSelectAny(pickShape, root, tPoint4d);
	
    }
    
    
    private BHNode doSelectAny(PickShape pickShape, BHNode bh, Point4d pickPos) {

	BHNode hitNode = null;
	
	if((bh == null) || (bh.bHull.isEmpty()))
	    return null;

	switch(bh.nodeType) {
	case BHNode.BH_TYPE_LEAF:
	    if (((BHLeafNode)(bh)).isEnable() &&
		(((BHLeafNode) bh).leafIF instanceof GeometryAtom) &&
		((GeometryAtom) (((BHLeafNode)
				   bh).leafIF)).source.isPickable &&
		pickShape.intersect(bh.bHull, pickPos)) {
		return bh;
	    } 
	    break;
	case BHNode.BH_TYPE_INTERNAL:
	    if (pickShape.intersect(bh.bHull, pickPos)) {
		hitNode = doSelectAny(pickShape, 
				      ((BHInternalNode)bh).getRightChild(),
				      pickPos);
		
		if (hitNode != null) {
		    return hitNode;
		}
		
		return doSelectAny(pickShape, 
				   ((BHInternalNode)bh).getLeftChild(),
				   pickPos);
	    }
	    break;
	}
	return null;
    }
    
    
    private void create(BHNode bhArr[]) {
	int i;

	if(bhArr == null) {
	    root = null;
	    return;
	}
	
	if(bhArr.length == 1) {   
	    bhArr[0].computeBoundingHull();	    
	    root = (BHNode)bhArr[0];
	    return;
	}
	
	int centerValuesIndex[] = new int[bhArr.length];
	float centerValues[][] = computeCenterValues(bhArr, centerValuesIndex);

	/*
	  System.out.println("Length of array is " +  bhArr.length);
	  for(int kk=0; kk<bhArr.length;kk++) {	
	  System.out.println("( " + centerValues[kk][0] + ", " + 
	  centerValues[kk][1] + ", " + centerValues[kk][2] + " )");
	  }
	  */
	
	root = new BHInternalNode();
	constructTree((BHInternalNode) root, bhArr, centerValues,
		      centerValuesIndex);  


	if(J3dDebug.devPhase && J3dDebug.debug)
	    gatherTreeStatistics();
    
    }
    
    void insert(BHNode bhArr[], int size) {
	// first pass: add all elements to the tree creating k array internal
	// nodes using the auxiliaryInsertStucture
	// second pass: go through all elements of the auxiliaryInsertStructure
	// and then update these nodes reclustering the trees with the new
	// k element siblings ...

	if(J3dDebug.devPhase && J3dDebug.debug)
	    J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
			     "BHTree.java : Insert - bhArr.length is " +
			     bhArr.length + "\n");
	
	if((bhArr == null) || (size < 1) || (bhArr.length < 1))
	    return;	
	
	if(root == null) {
	    if(J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
				 "BHTree.java : Tree has not be created yet.\n");
	    
	    // This extra "new" is needed, because create() require its input
	    // array's length be equal to the number of inserted nodes.
	    BHNode[] newbhArr = new BHNode[size];
	    System.arraycopy(bhArr, 0, newbhArr, 0, size);
	    create(newbhArr);
	    return;
	}
	
	if(root.nodeType == BHNode.BH_TYPE_LEAF) {
	    BHNode[] oldBhArr = bhArr;
	    bhArr = new BHNode[size + 1];
	    System.arraycopy(oldBhArr, 0, bhArr, 0, size);
	    bhArr[size] = root;
	    create(bhArr);
	    return;
	}
	
	if(insertStructure == null) {
	    insertStructure = new BHInsertStructure(size);
	}
	else {
	    insertStructure.clear();
	}
	
	for (int i=0; i<size; i++) {
	    // test if its inside the 'root' element
	    if ( root.isInside(bhArr[i].bHull) ) {
		((BHInternalNode)root).insert(bhArr[i], insertStructure);
	    } 
	    else {
		// extend the bounds of root  by joining with current element
		root.bHull.combine(bhArr[i].bHull);
		insertStructure.lookupAndInsert(root, bhArr[i]);
	    }
	}
	
	insertStructure.updateBoundingTree(this);
	// System.out.println("BHTree - Inserting ...");
	
	// Guard against size<1 is done at the start of this method.
	estMaxDepth += (int) (Math.log(size)/LOG_OF_2) + 1;
	
	if(estMaxDepth > depthUpperBound) {  
	    int maxDepth = root.computeMaxDepth(0);
	    int leafCount = root.countNumberOfLeaves();
	    double compDepth = Math.log(leafCount)/LOG_OF_2;
	    /*
	      System.out.println("BHTree - evaluate for reConstructTree ...");
	      System.out.println("compDepth " + compDepth);
	      System.out.println("maxDepth " + maxDepth);
	      System.out.println("leafCount " + leafCount);
	      */
	    
	    // Upper bound guard. 
	    if(maxDepth > depthUpperBound) {
		reConstructTree(leafCount);
		maxDepth = root.computeMaxDepth(0);
		/*
		  System.out.println("BHTree - Did reConstructTree ...");
		  System.out.println("compDepth " + compDepth);
		  System.out.println("maxDepth " + maxDepth);
		  */
	    }

	    // Adjust depthUpperBound according to app. need.
	    // If we encounter lots of overlapping bounds, the re-balanced
	    // tree may not be an ideal balance tree. So there might be a
	    // likehood of maxDepth exceeding the preset depthUpperBound.
	    if(maxDepth > depthUpperBound) {
	    	depthUpperBound = depthUpperBound + INCR_DEPTH_BOUND;
	    }else if((depthUpperBound != DEPTH_UPPER_BOUND) &&
		     (maxDepth * 1.5 < depthUpperBound)) {
		depthUpperBound = depthUpperBound - INCR_DEPTH_BOUND;
		
		if(depthUpperBound < DEPTH_UPPER_BOUND) {
		    // Be sure that DEPTH_UPPER_BOUND is the min. 
		    depthUpperBound = DEPTH_UPPER_BOUND;
		}
	    }
	    
	    // This is the only place for resetting estMaxDepth to the tree real
	    // maxDepth. Hence in cases where tree may get deteriorate fast, such
	    // as multiple inserts and deletes frequently. estMaxDepth is accuminated,
	    // and will lead to tree re-evaluation and possibly re-balancing.
	    estMaxDepth = maxDepth;
	}
    
    }


    // mark all elements of the node and its parent as needing updating
    private void markParentChain(BHNode[] nArr, int size) {
	BHNode node;
	
	for(int i=0; i<size; i++) {
	    node = nArr[i];
	    node.mark = true;
	    while((node.parent != null) && (node.parent.mark == false)) {
		node = node.parent;
		node.mark = true;
	    }
	}
    }

    // mark all elements of the node and its parent as needing updating
    private void markParentChain(BHNode node) {
	node.mark = true;
	while((node.parent != null) && (node.parent.mark == false)) {
	    node = node.parent;
	    node.mark = true;
	}
    }
    
    // Delete a series of n node elements from the input binary tree.
    // These elements are removed from the tree in a 2 phase process.
    // First, all elements to be removed are marked and all parent
    // chains are marked ... then a second phase of the algorithm goes
    // through and deletes them and updates all of the bounds ...
    
    // delete the n elements in the array from the tree
    void delete(BHNode bhArr[], int size) {
	BHNode node;
	
	/*
	  if((bhArr == null) || (bhArr.length < 1))
	  return;
	  System.out.println("BHTree.java : delete - bhArr.length is " +
	  bhArr.length);
	  for(int i=0; i<bhArr.length; i++)
	  System.out.println("bhArr[" + i +"] " + bhArr[i]);
	  
	    */
	
	for(int i=0; i<size; i++) {
	    if((bhArr[i] != null) && (bhArr[i].nodeType == BHNode.BH_TYPE_LEAF)) {
		markParentChain(bhArr[i]);
	    } else {
		if(J3dDebug.devPhase)
		    J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_1,
				     "Warning, element " + i + " is null/not leaf node.\n"
				     + "Error in deletion routine, element " +
				     bhArr[i] + "\n" +
				     "In tree = " + this +
				     " can not delete it ...\n");
	    }
	    
	}
	
	root = root.deleteAndUpdateMarkedNodes();
	
	if(J3dDebug.devPhase)
	    if (root == null) {
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
				 "Tree has been completely deleted ...\n");
	    }	
    }
    
    // compute the center values along each of the three dimensions given
    // the array of leaf objects to be split and joined
    float[][] computeCenterValues(BHNode[] bhArr, int[] cIndex) {
	float centers[][] = new float[bhArr.length][3];
	
	// compute the center values of the input array of nodes
	for ( int i=0; i < bhArr.length; i++ ) {
	    cIndex[i] = i;
	    
	    bhArr[i].computeBoundingHull();
		
	    centers[i][0] =
		(float)((bhArr[i].bHull.upper.x + bhArr[i].bHull.lower.x))/ 2.0f;
	    centers[i][1] =
		(float)((bhArr[i].bHull.upper.y + bhArr[i].bHull.lower.y)) / 2.0f;
	    centers[i][2] =
		(float)((bhArr[i].bHull.upper.z + bhArr[i].bHull.lower.z)) / 2.0f;
	    
	}
	return centers;
    }
    
    
    void computeMeansAndSumSquares(float[][] centerValues, int[] centerValuesIndex,
				   float[] means, float[] ss) {
	
	int i, arrLen;
	float sumCenters[] = new float[3];
	float temp = 0.0f;
	
	arrLen = centerValuesIndex.length;
	// Initialization.
	for(i=2; i>=0; i--) {
	    sumCenters[i] = 0.0f;
	    ss[i] = 0.0f;
	}
	
	for(i=arrLen-1; i>=0 ; i--) {
	    sumCenters[0] += centerValues[centerValuesIndex[i]][0];
	    sumCenters[1] += centerValues[centerValuesIndex[i]][1];
	    sumCenters[2] += centerValues[centerValuesIndex[i]][2];
	}
	
	means[0] = sumCenters[0]/(float)arrLen;
	means[1] = sumCenters[1]/(float)arrLen;
	means[2] = sumCenters[2]/(float)arrLen;
	
	for(i=arrLen-1; i>=0 ; i--) {
	    temp = (centerValues[centerValuesIndex[i]][0] - means[0]);
	    ss[0] += (temp*temp);
	    temp = (centerValues[centerValuesIndex[i]][1] - means[1]);
	    ss[1] += (temp*temp);
	    temp = (centerValues[centerValuesIndex[i]][2] - means[2]);
	    ss[2] += (temp*temp);
	    
	}	
	
    }
    
    // find the split axis (the highest ss and return its index) for
    // a given set of ss values
    int findSplitAxis ( float ss[] ) {
	int splitAxis = -1;
	float maxSS = 0.0f;
	
	// the largest ss  index value
	for (int i=0; i < 3; i++) {
	    if ( ss[i] > maxSS ) {
		maxSS = ss[i];
		splitAxis = i;
	    }
	}
	return splitAxis;
    }
    
    // Recursive method for constructing a binary tree.    
    void constructTree( BHInternalNode parent, BHNode bhArr[],
			float[][] centerValues, 
			int[] centerValuesIndex ){
	
	int i, splitAxis;
	int rightSetCount = 0;
	int leftSetCount = 0;
	float means[] = new float[3];
	float ss[] = new float[3];
	
	if(J3dDebug.devPhase)
	    if ( bhArr.length <= 1 ) {
		// this is only here for debugging can be removed after testing
		// to ensure that it never gets called
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_1,
				 "constructTree - bhArr.length <= 1. Bad !!!\n");
	    }
	
	computeMeansAndSumSquares(centerValues, centerValuesIndex, means, ss);
	
	splitAxis = findSplitAxis(ss);

	// an array of decision variables for storing the values of inside
	// the right or left set for a particular element of bhArr.
	// true if its in the left set, false if its in the right set
	boolean leftOrRightSet[] = new boolean[bhArr.length];
	
	if ( splitAxis == -1 ) {
	    // This is bad. Since we can't find a split axis, the best thing
	    // to do is to split the set in two sets; each with about the
	    // same number of elements. By doing this we can avoid constructing
	    // a skew tree.
	    
	    // split elements into half.
	    for ( i=0; i < bhArr.length; i++) {
		if(leftSetCount > rightSetCount) {
		    rightSetCount++;
		    leftOrRightSet[i] = false;
		} else {
		    leftSetCount++;
		    leftOrRightSet[i] = true;
		}	
	    }
	}
	else {
	    for ( i=0; i < bhArr.length; i++) {
		// the split criterion, special multiple equals cases added
		if ( centerValues[centerValuesIndex[i]][splitAxis] <
		     means[splitAxis]) {
		    
		    if(J3dDebug.devPhase)
			J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
					 "Found a left element\n");
		    leftSetCount++;
		    leftOrRightSet[i] = true;
		} else if ( centerValues[centerValuesIndex[i]][splitAxis] >
			    means[splitAxis]) {
		    if(J3dDebug.devPhase)
			J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
					 "Found a right element\n");
		    rightSetCount++;
		    leftOrRightSet[i] = false;
		} else {
		    if(J3dDebug.devPhase)
			J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4,
					 "Found an equal element\n");
		    if(leftSetCount > rightSetCount) {
			rightSetCount++;
			leftOrRightSet[i] = false;
		    } else {
			leftSetCount++;
			leftOrRightSet[i] = true;
		    }
		}
	    }
	}

	if(J3dDebug.devPhase)
	    J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_2,
			     "LeftSetCount " + leftSetCount + " RightSetCount "+
			     rightSetCount + "\n");


	// Don't think that this guard is needed, but still like to have it. 
	// Just in case, bad means and the sum of squares might lead us into the guard. 
	if (leftSetCount == bhArr.length) {
	    if(J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_1,
				 "Split Axis of = " + splitAxis + " didn't yield "+
				 "any split among the objects ?\n");
	    // split elements into half
	    rightSetCount = 0;
	    leftSetCount = 0;
	    for ( i=0; i < bhArr.length; i++) {
		if(leftSetCount > rightSetCount) {
		    rightSetCount++;
		    leftOrRightSet[i] = false;
		} else {
		    leftSetCount++;
		    leftOrRightSet[i] = true;
		}	
	    }	    
	} else if (rightSetCount == bhArr.length) {
	    if(J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_1,
				 "Split Axis of = " + splitAxis + " didn't yield "+
				 "any split among the objects ?\n");
	    // split elements into half
	    rightSetCount = 0;
	    leftSetCount = 0;
	    for ( i=0; i < bhArr.length; i++) {
		if(leftSetCount > rightSetCount) {
		    rightSetCount++;
		    leftOrRightSet[i] = false;
		} else {
		    leftSetCount++;
		    leftOrRightSet[i] = true;
		}	
	    }
	}
	
	if(J3dDebug.devPhase)
	    if(J3dDebug.doDebug(J3dDebug.bHTree, J3dDebug.LEVEL_4))
		// check to make sure that rightSet and leftSet sum to the
		// number of elements in the original array.
		if ( bhArr.length != (rightSetCount + leftSetCount) ) {
		    System.out.println("An error has occurred in spliting");	
		}
	       
	BHNode rightSet[] = new BHNode[rightSetCount];
	BHNode leftSet[] = new BHNode[leftSetCount];
	int centerValuesIndexR[] = new int[rightSetCount];
	int centerValuesIndexL[] = new int[leftSetCount];
	
	rightSetCount = 0;
	leftSetCount = 0;
	
	for (i=0; i < bhArr.length; i++) {
	    if ( leftOrRightSet[i] ) { // element in left set
		leftSet[leftSetCount] = bhArr[i];	  
		centerValuesIndexL[leftSetCount] = centerValuesIndex[i];
		leftSetCount++;
	    } else {
		rightSet[rightSetCount] = bhArr[i];
		centerValuesIndexR[rightSetCount] = centerValuesIndex[i];
		rightSetCount++;
	    }
	}
	
	if (rightSet.length != 1) {
	    parent.rChild = new BHInternalNode();
	    parent.rChild.setParent(parent);
	    constructTree((BHInternalNode)(parent.rChild),  rightSet, centerValues,
			  centerValuesIndexR); 
	} else {
	    parent.rChild = rightSet[0];
	    parent.rChild.setParent(parent);
	}
	
	if (leftSet.length != 1) {
	    parent.lChild = new BHInternalNode();
	    parent.lChild.setParent(parent);
	    constructTree((BHInternalNode)(parent.lChild), leftSet, centerValues, 
			  centerValuesIndexL); 
	} else {
	    parent.lChild = leftSet[0];
	    parent.lChild.setParent(parent);
	}
	    
	parent.combineBHull(parent.rChild, parent.lChild);
    }


    void reConstructTree(int numOfLeaf) {
	if(root == null)
	    return;

	BHNode bhArr[] = new BHNode[numOfLeaf];
	int index[] = new int[1];
	index[0] = 0;
	root.destroyTree(bhArr, index);

	/*
	  if(bhArr.length != index[0])
	  System.out.println("BHTree - This isn't right!!! - bhArr.length " +
	  bhArr.length + " index " + index[0]); 
	  */
	
	create(bhArr);

    }
    
    void gatherTreeStatistics() {
	
	int leafCount = root.countNumberOfLeaves();
	int internalCount = root.countNumberOfInternals();
	int maxDepth = root.computeMaxDepth(0);
	float averageDepth = root.computeAverageLeafDepth ( leafCount, 0);	
	
	
	System.out.println("Statistics for tree = " + this);
	System.out.println("Total Number of nodes in tree = " + 
			   (leafCount + internalCount) );
	System.out.println("Number of Leaf Nodes = " + leafCount );
	System.out.println("Number of Internal Nodes = " + internalCount );
	System.out.println("Maximum Leaf depth = " + maxDepth );
	System.out.println("Average Leaf depth = " + averageDepth );
	System.out.println("root.bHull = " + root.bHull);
	// printTree(root);
	
    }    


    void printTree(BHNode bh) {
	if(bh!= null) {
	    if(bh.nodeType == BHNode.BH_TYPE_INTERNAL) {
		System.out.println("BH_TYPE_INTERNAL - bHull : " + bh);
		System.out.println(bh.bHull);
		System.out.println("rChild : " + ((BHInternalNode)bh).rChild +
				   " lChild : " + ((BHInternalNode)bh).lChild);
		printTree(((BHInternalNode)bh).rChild);
		printTree(((BHInternalNode)bh).lChild);
	    }
	    else if(bh.nodeType == BHNode.BH_TYPE_LEAF) {
		System.out.println("BH_TYPE_LEAF - bHull : " + bh);
		System.out.println(bh.bHull);    
	    }
		
	}


    }
}






