/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.*;


/**
 * Internal class that implements picking functionality.
 */

class Picking {

    static SceneGraphPath[] pickAll(Locale locale, PickShape shape) {
	if(locale == null) {
	    return null;
	}

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);
	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 
	    // although getSceneGraphPath() also return null, we
	    // save time for synchronization
	    return null;  
	}
	synchronized (locale.universe.sceneGraphLock) {
	    return getSceneGraphPath(null, null, geomAtoms, locale); 
	}
    }
  


    static SceneGraphPath[] pickAll(BranchGroup node, 
				    PickShape shape) {
	if (node == null) {
	    return null;
	}

	BranchGroupRetained nodeR = (BranchGroupRetained) node.retained;

	if (nodeR.inSharedGroup) {
	    throw new RestrictedAccessException(J3dI18N.getString("Picking0"));
	}

	Locale locale = nodeR.locale;
							 
	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);

	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 
	    return null;
	}

	synchronized (nodeR.universe.sceneGraphLock) {
	    return getSceneGraphPath(initSceneGraphPath(nodeR),
				     nodeR, geomAtoms, locale);
	}
    }

  
    static SceneGraphPath[] pickAllSorted(Locale locale, 
					  PickShape shape) {
	if(locale == null) {
	    return null;
	}

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);

	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 
	    return null;
	}

	sortGeomAtoms(geomAtoms, shape);

	synchronized (locale.universe.sceneGraphLock) {
	    return getSceneGraphPath(null, null, geomAtoms, locale); 
	}
    }


    static SceneGraphPath[] pickAllSorted(BranchGroup node, 
					  PickShape shape) {
	if (node == null) {
	    return null;
	}

	BranchGroupRetained nodeR = (BranchGroupRetained) node.retained;

	if (nodeR.inSharedGroup) {
	    throw new RestrictedAccessException(J3dI18N.getString("Picking0"));
	}

	Locale locale = nodeR.locale;

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);

	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 
	    return null;
	}

	// we have to sort first before eliminate duplicate Text3D
	// since we want the closest geometry atoms of Text3D
	sortGeomAtoms(geomAtoms, shape);

	synchronized (nodeR.universe.sceneGraphLock) {
	    return getSceneGraphPath(initSceneGraphPath(nodeR),
				     nodeR, geomAtoms, locale);
	}
    }

  
    static SceneGraphPath pickClosest(Locale locale, 
				      PickShape shape) {	

	if(locale == null) {
	    return null;
	}

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);


	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 
	    return null;
	}


	GeometryAtom geomAtom = selectClosest(geomAtoms, shape);


	synchronized (locale.universe.sceneGraphLock) {
	    return getSceneGraphPath(null, null, geomAtom, locale); 
	}
    }

  
    static SceneGraphPath pickClosest(BranchGroup node, 
				      PickShape shape) {
	if (node == null) {
	    return null;
	}

	BranchGroupRetained nodeR = (BranchGroupRetained) node.retained;

	if (nodeR.inSharedGroup) {
	    throw new RestrictedAccessException(J3dI18N.getString("Picking0"));
	}

	Locale locale = nodeR.locale;

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);



	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 	
	    return null;
	}


	// We must sort all since the closest one in geomAtoms may not
	// under the BranchGroup node
	sortGeomAtoms(geomAtoms, shape);

	synchronized (nodeR.universe.sceneGraphLock) {
	    return getFirstSceneGraphPath(initSceneGraphPath(nodeR),
					  nodeR, geomAtoms, locale);

	}
    }
  

    static SceneGraphPath pickAny(Locale locale, PickShape shape) {

	if(locale == null) {
	    return null;
	}

	GeometryAtom geomAtom =
	    locale.universe.geometryStructure.pickAny(locale, shape);

	if (geomAtom == null) {
	    return null;
	}

	
	synchronized (locale.universe.sceneGraphLock) {
	    return getSceneGraphPath(null, null, geomAtom, locale); 
	}
    }
  
    static SceneGraphPath pickAny(BranchGroup node, PickShape shape) {	

	if (node == null) {
	    return null;
	}

	BranchGroupRetained nodeR = (BranchGroupRetained) node.retained;
	
	if (nodeR.inSharedGroup) {
	    throw new RestrictedAccessException(J3dI18N.getString("Picking0"));
	}
	
	Locale locale = nodeR.locale;

	// since PickAny return from geometry may not lie under
	// BranchGroup node, we have to use pickAll 

	GeometryAtom geomAtoms[] =
	    locale.universe.geometryStructure.pickAll(locale, shape);

	if ((geomAtoms == null) || (geomAtoms.length == 0)) { 	
	    return null;
	}

	synchronized (nodeR.universe.sceneGraphLock) {
	    return getFirstSceneGraphPath(initSceneGraphPath(nodeR),
				     nodeR, geomAtoms, locale);
	}
    }


    /**
     * Search the path from nodeR up to Locale.
     * Return the search path as ArrayList if found.
     * Note that the locale will not insert into path.
     */
    static private ArrayList initSceneGraphPath(NodeRetained nodeR) { 
	ArrayList path = new ArrayList(5);

	do {
	    if (nodeR.source.getCapability(Node.ENABLE_PICK_REPORTING)){
		path.add(nodeR);  
	    }
	    nodeR = nodeR.parent;
	} while (nodeR != null);  // reach Locale

	return path;
    }

    /**
     * return all SceneGraphPath[] of the geomAtoms.
     * If initpath is null, the path is search from 
     * geomAtom Shape3D/Morph Node up to Locale
     * (assume the same locale).
     * Otherwise, the path is search up to node or 
     * null is return if it is not hit.
     */
    static private SceneGraphPath[] getSceneGraphPath(ArrayList initpath, 
						      BranchGroupRetained node, 
						      GeometryAtom geomAtoms[],
						      Locale locale) {

	ArrayList paths = new ArrayList(5);
	GeometryAtom geomAtom;
	NodeRetained target;
	ArrayList texts = null;

	if (geomAtoms == null) {
	    return null;
	}


	for (int i=0; i < geomAtoms.length; i++) {
	    geomAtom = (GeometryAtom) geomAtoms[i];
	    Shape3DRetained shape = geomAtom.source;
		
	    // isPickable and currentSwitchOn has been check in BHTree

	    if (!inside(shape.branchGroupPath, node)) {
		continue;
	    }
		
	    target = shape.sourceNode;	

	    if (target == null) {
		// The node is just detach from branch so sourceNode = null		
		continue;
	    }
	    
	    // Special case, for Text3DRetained, it is possible
	    // for different geomAtoms pointing to the same
	    // source Text3DRetained. So we need to combine
	    // those cases and report only once.
	    if (target instanceof Shape3DRetained) {
		Shape3DRetained s3dR = (Shape3DRetained) target;  
		GeometryRetained geomR = null;
		for(int cnt=0; cnt<s3dR.geometryList.size(); cnt++) {
		    geomR = (GeometryRetained) s3dR.geometryList.get(cnt);
		    if(geomR != null)
			break;
		}

		if (geomR == null)
		    continue;
	    
		if (geomR instanceof Text3DRetained) {
		    // assume this case is not frequent, we allocate
		    // ArrayList only when necessary and we use ArrayList
		    // instead of HashMap since the case of when large
		    // number of distingish Text3DRetained node hit is
		    // rare.
		    if (texts == null) {
			texts = new ArrayList(3);
		    } else {
			int size = texts.size();
			boolean found = false;
			for (int j=0; j < size; j++) {
			    if (texts.get(j) == target) {
				found = true;
				break;
			    }
			}
			if (found) {
			    continue;  // try next geomAtom
			}
		    }
		    texts.add(target);
		}
	    }
	    
	    ArrayList path = retrievePath(target, node,
					  geomAtom.source.key);

	    if (path == null) {
		continue;
	    }

	    // If target is instance of compile retained, then loop thru
	    // the entire source list and add it to the scene graph path
	    if (target instanceof Shape3DCompileRetained) {
		Shape3DCompileRetained s3dCR = (Shape3DCompileRetained)target;
		Node[] mpath = mergePath(path, initpath);
		for (int n = 0; n < s3dCR.srcList.length; n++) {
		    SceneGraphPath sgpath = new SceneGraphPath(locale,
							       mpath,
							       (Node) s3dCR.srcList[n]);
		    sgpath.setTransform(shape.getCurrentLocalToVworld(0));
		    paths.add(sgpath);
		}
		    
	    }
	    else {
		SceneGraphPath sgpath = new SceneGraphPath(locale,
							   mergePath(path, initpath),
							   (Node) target.source);
		sgpath.setTransform(shape.getCurrentLocalToVworld(0));
		paths.add(sgpath);
	    }


	}
	SceneGraphPath pathArray[] = new SceneGraphPath[paths.size()];
	return (SceneGraphPath []) paths.toArray(pathArray);
    }

    /**
     * return the SceneGraphPath of the geomAtom. 
     * If initpath is null, the path is search from 
     * geomAtom Shape3D/Morph Node up to Locale
     * (assume the same locale).
     * Otherwise, the path is search up to node or 
     * null is return if it is not hit.
     */
    static private SceneGraphPath getSceneGraphPath(ArrayList initpath, 
						    BranchGroupRetained node, 
						    GeometryAtom geomAtom,
						    Locale locale) {
	if (geomAtom == null) {
	    return null;
	}
	
	Shape3DRetained shape = geomAtom.source;
	NodeRetained target = shape.sourceNode;
	
	if (target == null) {
	    // The node is just detach from branch so sourceNode = null		
	    return null;
	}

	if (!inside(shape.branchGroupPath, node)) {
	    return null;
	}

	ArrayList path = retrievePath(target, node, shape.key);

	if (path == null) {
	    return null;
	}

	SceneGraphPath sgpath = new SceneGraphPath(locale, 
						   mergePath(path, initpath),
						   (Node)
						   target.source);
	sgpath.setTransform(shape.getCurrentLocalToVworld(0));
	return sgpath;
    }

    /**
     * Return true if bg is inside cachedBG or bg is null
     */
    static private boolean inside(BranchGroupRetained bgArr[],
				  BranchGroupRetained bg) {

	if ((bg == null) || (bgArr == null)) {
	    return true;
	}

	for (int i=0; i < bgArr.length; i++) {
	    if (bgArr[i] == bg) {
		return true;
	    }
	}
	return false;
    }


    /**
     * return the first SceneGraphPath of the geomAtom. 
     * If initpath is null, the path is search from 
     * geomAtom Shape3D/Morph Node up to Locale
     * (assume the same locale).
     * Otherwise, the path is search up to node or 
     * null is return if it is not hit.
     */
    static private SceneGraphPath getFirstSceneGraphPath(ArrayList initpath, 
							 BranchGroupRetained node, 
							 GeometryAtom geomAtoms[],
							 Locale locale) {
	if (geomAtoms == null) {
	    return null;
	}

	for (int i=0; i < geomAtoms.length; i++) {
	    Shape3DRetained shape = geomAtoms[i].source;
	    NodeRetained target = shape.sourceNode;

	    if (target == null) {
		// The node is just detach from branch so sourceNode = null		
		continue;
	    }
	    if (!inside(shape.branchGroupPath, node)) {
		continue;
	    }
	    ArrayList path = retrievePath(target, node, geomAtoms[i].source.key);

	    if (path == null) {
		continue;
	    }
	    SceneGraphPath sgpath = new SceneGraphPath(locale, 
						       mergePath(path, initpath),
						       (Node) target.source);
	    sgpath.setTransform(shape.getCurrentLocalToVworld(0));
	    return sgpath;
	}
	return null;
    }


    /**
     * search the full path from the botton of the scene graph -
     * startNode, up to the Locale if endNode is null.
     * If endNode is not null, the path is found up to, but not
     * including, endNode or return null if endNode not hit 
     * during the search.
     */
    static private ArrayList retrievePath(NodeRetained startNode, 
					  NodeRetained endNode,
					  HashKey key) {

	ArrayList path = new ArrayList(5);
	NodeRetained nodeR = startNode;
	
	if (nodeR.inSharedGroup) {
	    // getlastNodeId() will destroy this key
	    key = new HashKey(key);
	}

	do {
	    if (nodeR == endNode) { // we found it !
		return path;
	    }

	    if (nodeR.source.getCapability(Node.ENABLE_PICK_REPORTING)) {
		path.add(nodeR);  
	    }

	    if (nodeR instanceof SharedGroupRetained) {
		// retrieve the last node ID
		String nodeId = key.getLastNodeId();
		Vector parents = ((SharedGroupRetained) nodeR).parents;
		int sz = parents.size();
		NodeRetained prevNodeR = nodeR;
		for(int i=0; i< sz; i++) {
		    NodeRetained linkR = (NodeRetained) parents.elementAt(i);
		    if (linkR.nodeId.equals(nodeId)) {
			nodeR = linkR;
			// Need to add Link to the path report
			path.add(nodeR);
			// since !(endNode instanceof Link), we 
			// can skip the check (nodeR == endNode) and
			// proceed to parent of link below
			break;
		    }
		}
		if (nodeR == prevNodeR) { 
		    // branch is already detach
		    return null;
		}
	    }
	    nodeR = nodeR.parent;
	} while (nodeR != null); // reach Locale
	
	if (endNode == null) {
	    // user call pickxxx(Locale locale, PickShape shape)
	    return path;
	}

	// user call pickxxx(BranchGroup endNode, PickShape shape)
	// if locale is reached and endNode not hit, this is not
	// the path user want to select 
	return null;
    }
    
    /**
     * copy p1, (follow by) p2 into a new array, p2 can be null
     * The path is then reverse before return.
     */
    static private Node[] mergePath(ArrayList p1, ArrayList p2) {
	int s = p1.size();
	int len;
	int i;
	int l;
	if (p2 == null) {
	    len = s;
	} else {
	    len = s + p2.size();
	}

	Node nodes[] = new Node[len];
	l = len-1;
	for (i=0; i < s; i++) {
	    nodes[l-i] = (Node) ((NodeRetained) p1.get(i)).source;
	}
	for (int j=0; i< len; i++, j++) {
	    nodes[l-i] = (Node) ((NodeRetained) p2.get(j)).source;
	}
	return nodes;
    }

    /**
     * Select the closest geomAtoms from shape
     * geomAtoms.length must be >= 1
     */
    static private GeometryAtom selectClosest(GeometryAtom geomAtoms[], 
					      PickShape shape) {
	Point4d pickPos = new Point4d();
	GeometryAtom closestAtom = geomAtoms[0];
	shape.intersect(closestAtom.source.vwcBounds, pickPos);
	double distance = pickPos.w;

	for (int i=1; i < geomAtoms.length; i++) {
	    shape.intersect(geomAtoms[i].source.vwcBounds, pickPos);	    
	    if (pickPos.w < distance) {
		distance = pickPos.w;
		closestAtom = geomAtoms[i];
	    }
	}
	return closestAtom;
    }

    /**
     * Sort the GeometryAtoms distance from shape in ascending order
     * geomAtoms.length must be >= 1
     */
    static private void sortGeomAtoms(GeometryAtom geomAtoms[], 
				      PickShape shape) {

	final double distance[] = new double[geomAtoms.length];
	Point4d pickPos = new Point4d();

	for (int i=0; i < geomAtoms.length; i++) {
	    shape.intersect(geomAtoms[i].source.vwcBounds, pickPos);	    
	    distance[i] = pickPos.w;
	}

	class Sort {
	    
	    GeometryAtom atoms[];

	    Sort(GeometryAtom[] atoms) {
		this.atoms = atoms;
	    }

	    void sorting() {
		if (atoms.length < 7) {
		    insertSort();
	    	} else {
		    quicksort(0, atoms.length-1);
    		}
	    }

	    // Insertion sort on smallest arrays
	    final void insertSort() {
		for (int i=0; i<atoms.length; i++) {
		    for (int j=i; j>0 && 
			     (distance[j-1] > distance[j]); j--) {
			double t = distance[j];
			distance[j] = distance[j-1];
			distance[j-1] = t;
			GeometryAtom p = atoms[j];
			atoms[j] = atoms[j-1];
			atoms[j-1] = p;
		    }
		}
	    }

            final void quicksort( int l, int r ) {
		int i = l;
		int j = r;
		double k = distance[(l+r) / 2];

		do {
		    while (distance[i]<k) i++;
		    while (k<distance[j]) j--;
		    if (i<=j) {
			double tmp = distance[i];
			distance[i] =distance[j];
			distance[j] = tmp;
			
			GeometryAtom p=atoms[i];
			atoms[i]=atoms[j];
			atoms[j]=p;
			i++;
			j--;
		    }
		} while (i<=j);
		
		if (l<j) quicksort(l,j);
		if (l<r) quicksort(i,r);
	    }
	}

	(new Sort(geomAtoms)).sorting();
    }
  
}
