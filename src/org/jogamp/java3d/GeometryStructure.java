/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.jogamp.java3d;

import java.util.ArrayList;
import java.util.Arrays;

import org.jogamp.vecmath.Vector3d;


/**
 * A geometry structure is a object that organizes geometries
 * and bounds.
 */

class GeometryStructure extends J3dStructure {
    /**
     * used during Transform Processing
     */
    UpdateTargets targets = null;

    /**
     * A multiple read single write Lock to sychronize access into this
     * GeometryStructure.
     * To prevent deadlock a call to read/write lock must end with a read/write
     * unlock respectively.
     */
    private MRSWLock lock = null;

    /**
     * A lock object to prevent concurrent getVisibleBHTree query.
     */
    private Object  visLock = new Object();

    /**
     * A lock object to prevent concurrent collideEntryList,
     * collideExitList using toArray() in BehaviorStructure buildTree()
     * while clearMirror() is invoked in GeometryStructure removeNode()
     */
    private Object collideListLock = new Object();

    /**
     * Binary Hull Tree structure for handling geometry atoms.
     * Do not change the following private variables to public, their access
     * need to synchronize via lock.
     */

    private BHTree[] bhTreeArr = null;
    private int bhTreeCount;
    private int bhTreeMax;
    private int bhTreeBlockSize = 5;

    /**
     * The array of BHNode, a data pool, for passing data between GS and BHTrees.
     * Do not change the following private variables to public, their access
     * need to synchronize via lock.
     */
    private BHNode[] bhNodeArr = null;
    private int bhNodeCount, bhNodeMax;
    private int bhNodeBlockSize = 50;

    // Support for multi-locale.
    private Vector3d localeTrans = new Vector3d();


    //The lists of wakeupCriterion object currently in collision.
    WakeupIndexedList collideEntryList;
    WakeupIndexedList collideExitList;
    WakeupIndexedList collideMovementList;

    // The lists of wakeupCriterion objects that GeometryStructure keeps
    WakeupIndexedList wakeupOnCollisionEntry;
    WakeupIndexedList wakeupOnCollisionExit;
    WakeupIndexedList wakeupOnCollisionMovement;

    // When Shape insert/remove for WakeupOnCollisionxxx() using
    // Group node and USE_GEOMETRY, we need to reevaluate the
    // cache geometryAtoms list.
    boolean reEvaluateWakeupCollisionGAs;

    private boolean transformMsg = false;

    /**
     *  Constructor.
     */
    GeometryStructure(VirtualUniverse u) {
	super(u, J3dThread.UPDATE_GEOMETRY);
	bhNodeCount = 0;
	bhNodeMax = bhNodeBlockSize;
	bhNodeArr = new BHNode[bhNodeMax];
	bhTreeMax = 1;
	bhTreeArr = new BHTree[bhTreeMax];
	bhTreeCount=0;
	lock = new MRSWLock();
	collideEntryList = new WakeupIndexedList(WakeupOnCollisionEntry.class,
						 WakeupOnCollisionEntry.COLLIDEENTRY_IN_BS_LIST, u);
	collideExitList = new WakeupIndexedList(WakeupOnCollisionExit.class,
						WakeupOnCollisionExit.COLLIDEEXIT_IN_BS_LIST, u);
	collideMovementList =  new WakeupIndexedList(WakeupOnCollisionMovement.class,
						     WakeupOnCollisionMovement.COLLIDEMOVE_IN_BS_LIST, u);
	wakeupOnCollisionEntry = new WakeupIndexedList(WakeupOnCollisionEntry.class,
						       WakeupOnCollisionEntry.COND_IN_GS_LIST, u);
	wakeupOnCollisionExit = new WakeupIndexedList(WakeupOnCollisionExit.class,
						      WakeupOnCollisionExit.COND_IN_GS_LIST, u);
	wakeupOnCollisionMovement = new WakeupIndexedList(WakeupOnCollisionMovement.class,
							  WakeupOnCollisionMovement.COND_IN_GS_LIST, u);
    }

    @Override
    void processMessages(long referenceTime) {
	J3dMessage m;
	J3dMessage[] messages = getMessages(referenceTime);
	int nMsg = getNumMessage();

	if (nMsg > 0) {
	    reEvaluateWakeupCollisionGAs = false;
	    for (int i=0; i < nMsg; i++) {
		lock.writeLock();
		m = messages[i];
		switch (m.type) {
		case J3dMessage.TRANSFORM_CHANGED:
		    transformMsg = true;
		    break;
		case J3dMessage.SWITCH_CHANGED:
		    processSwitchChanged(m);
		    // may need to process dirty switched-on transform
		    if (universe.transformStructure.getLazyUpdate()) {
			transformMsg = true;
		    }
		    break;
		case J3dMessage.INSERT_NODES:
		    insertNodes((Object[])m.args[0]);
		    reEvaluateWakeupCollisionGAs = true;
		    break;
		case J3dMessage.REMOVE_NODES:
		    removeNodes(m);
		    reEvaluateWakeupCollisionGAs = true;
		    break;
		case J3dMessage.SHAPE3D_CHANGED: {
		    int comp = ((Integer)m.args[1]).intValue();
		    if (comp == Shape3DRetained.GEOMETRY_CHANGED) {
			m.args[0] = m.args[2];
			removeNodes(m);
			insertNodes((Object[])m.args[3]);
			reEvaluateWakeupCollisionGAs = true;
		    }
		    else if (comp == Shape3DRetained.APPEARANCE_CHANGED) {
			processVisibleChanged(m.args[2],
					      ((GeometryAtom[]) m.args[3]));
		    }
		    break;
		}
		case J3dMessage.TEXT3D_DATA_CHANGED:
		    removeNodes(m);
		    insertNodes((Object[])m.args[1]);
		    break;
		case J3dMessage.TEXT3D_TRANSFORM_CHANGED:
		    processBoundsChanged((Object []) m.args[0], false);
		    break;
		case J3dMessage.MORPH_CHANGED: {
		    int comp = ((Integer)m.args[1]).intValue();
		    if (comp == MorphRetained.GEOMETRY_CHANGED) {
			processBoundsChanged((Object []) m.args[3], false);
		    }
		    else if (comp == MorphRetained.APPEARANCE_CHANGED) {
			processVisibleChanged(m.args[2],
					      ((GeometryAtom[]) m.args[3]));
		    }
		    break;
		}
		case J3dMessage.REGION_BOUND_CHANGED:
		case J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED:
		    //  Only set this flag, when bounds might be empty.
		    processBoundsChanged((Object [])m.args[0], false);
		    break;
		case J3dMessage.GEOMETRY_CHANGED:
		    // System.err.println("J3dMessage.GEOMETRY_CHANGED");
		    processBoundsChanged((Object []) m.args[0], false);
		    break;
		case J3dMessage.RENDERINGATTRIBUTES_CHANGED:
		    processVisibleChanged(m.args[2],
					  ((GeometryAtom[]) m.args[3]));
		    break;
		}

		lock.writeUnlock();
		m.decRefcount();
	    }

	    if (transformMsg) {
		targets = universe.transformStructure.getTargetList();
		lock.writeLock();

		processTransformChanged(targets);

		lock.writeUnlock();

		transformMsg = false;
		targets = null;
	    }

	    Arrays.fill(messages, 0, nMsg, null);
	}

	processCollisionDetection();
    }


    private int getBHTreeIndex(Locale  locale) {
	int i;

	for (i=0; i< bhTreeCount; i++) {
	    if (bhTreeArr[i].locale == locale)
		return i;
	}
	// Can't find will return -1 so that other
	// program know this
	return -1;
    }

    private int getOrAddBHTreeIndex(Locale  locale) {
	int i;

	for (i=0; i< bhTreeCount; i++) {
	    if (bhTreeArr[i].locale == locale)
		return i;
	}

	if (bhTreeCount >= bhTreeMax) {
	    // allocate a bigger array here....
	    if (J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.geometryStructure, J3dDebug.LEVEL_2,
				 "Expanding bhTreeArr array ...\n");
	    bhTreeMax += bhTreeBlockSize;
	    BHTree[] oldBhTreeArr = bhTreeArr;

	    bhTreeArr = new BHTree[bhTreeMax];
	    System.arraycopy(oldBhTreeArr, 0, bhTreeArr, 0, oldBhTreeArr.length);
	}

	bhTreeArr[bhTreeCount] = new BHTree(locale);
	bhTreeCount++;
	return i;
    }

    private void clearBhNodeArr() {
        // Issue 353: set all elements to null so we don't leak
        // NOTE: we really should change this to be an ArrayList, but that
        // would be a less localized change. Consider for 1.6.0.
        for (int i = 0; i < bhNodeCount; i++) {
            bhNodeArr[i] = null;
        }

        bhNodeCount = 0;
    }

    private void addToBhNodeArr(BHNode bhNode) {

	// Add to bhNodeArr.
	if (bhNodeCount >= bhNodeMax) {
	    bhNodeMax += bhNodeBlockSize;
	    BHNode[] oldbhNodeArr = bhNodeArr;

	    bhNodeArr = new BHNode[bhNodeMax];
	    System.arraycopy(oldbhNodeArr, 0, bhNodeArr, 0, oldbhNodeArr.length);
	}

	bhNodeArr[bhNodeCount] = bhNode;
	bhNodeCount++;
    }

    private void processVisibleChanged(Object valueObj, GeometryAtom[] gaArr) {
	boolean visible = true;  // Default is true.
	int i, treeIndex;

	if ((gaArr == null) || (gaArr.length < 1))
	    return;

	treeIndex = getBHTreeIndex(gaArr[0].locale);

	visible = ((Boolean)valueObj).booleanValue();

	for ( i=gaArr.length-1; i>=0; i--) {
	    gaArr[i].visible = visible;
	}

    }

    private void insertNodes(Object[] nodes) {
	Object node;
	GeometryAtom geomAtom;

	clearBhNodeArr();

	// System.err.println("GS : nodes.length is " + nodes.length);

	for (int i=0; i<nodes.length; i++) {
	    node = nodes[i];
	    if (node instanceof GeometryAtom) {
		synchronized (node) {
		    geomAtom = (GeometryAtom) node;
                    if (geomAtom.source.inBackgroundGroup) {
                        geomAtom.source.geometryBackground.
			    addBgGeometryAtomList(geomAtom);
                        continue;
                    }
		    BHLeafNode bhLeafNode = new BHLeafNode();
		    bhLeafNode.leafIF = geomAtom;
		    geomAtom.bhLeafNode = bhLeafNode;
		    bhLeafNode.computeBoundingHull();
		    // System.err.println("bhLeafNode.bHull is " + bhLeafNode.bHull);
		    addToBhNodeArr(bhLeafNode);
		}
	    } else if (node instanceof GroupRetained) {
		synchronized (node) {
		    GroupRetained group = (GroupRetained) node;
		    BHLeafNode bhLeafNode = new BHLeafNode();
		    bhLeafNode.leafIF = group;
		    group.bhLeafNode = bhLeafNode;
		    bhLeafNode.computeBoundingHull();
		    addToBhNodeArr(bhLeafNode);
		}
	    }
	}

	if (bhNodeCount < 1) {
	    return;
	}

	// Look for the right BHTree to insert to.
	// We must separate the following two calls
	// since the first Call will allocate storage bhTreeArr
	// for the second index operation. (see bug 4361998)
	int idx = getOrAddBHTreeIndex(((BHLeafNode)bhNodeArr[0]).getLocale());
	BHTree currTree = bhTreeArr[idx];
	currTree.insert(bhNodeArr, bhNodeCount);

	// Issue 353: must clear array after we are done with it
	clearBhNodeArr();

	// currTree.gatherTreeStatistics();
    }

    @Override
    void removeNodes(J3dMessage m) {
	Object[] nodes = (Object[]) m.args[0];
	Object node;

	clearBhNodeArr();

	for (int i=0; i<nodes.length; i++) {
	    node = nodes[i];
	    if (node instanceof GeometryAtom) {
		synchronized (node) {
		    GeometryAtom geomAtom = (GeometryAtom) node;
                    if ((geomAtom.source != null) &&
			(geomAtom.source.inBackgroundGroup)) {
                        geomAtom.source.geometryBackground.
			    removeBgGeometryAtomList(geomAtom);
                        continue;
                    }
		    if (geomAtom.bhLeafNode != null) {
			addToBhNodeArr(geomAtom.bhLeafNode);
			// Dereference BHLeafNode in GeometryAtom.
			geomAtom.bhLeafNode = null;
		    }

		}
	    } else if (node instanceof GroupRetained) {
		if (((NodeRetained)node).nodeType != NodeRetained.ORDEREDGROUP) {
		synchronized (node) {
		    GroupRetained group = (GroupRetained) node;
		    if (group.bhLeafNode != null) {
			addToBhNodeArr(group.bhLeafNode);
			// Dereference BHLeafNode in GroupRetained
			group.bhLeafNode = null;
		    }
		}
		}
	    } else if (node instanceof BehaviorRetained) {
		synchronized (node) {
		    BehaviorRetained behav = (BehaviorRetained) node;
		    // cleanup collideEntryList & collideExitList
		    // since we didn't remove
		    // it on remove in removeWakeupOnCollision()

		    // Note that GeometryStructure may run in
		    // parallel with BehaviorStructure when
		    // BS invoke activateBehaviors() to buildTree()
		    // which in turn call addWakeupOnCollision()
		    // to modify collideEntryList at the same time.

		    WakeupOnCollisionEntry wentry;
		    WakeupOnCollisionEntry wentryArr[] =
			(WakeupOnCollisionEntry []) collideEntryList.toArray();
		    for (int j=collideEntryList.arraySize()-1; j>=0; j--) {
			wentry = wentryArr[j];
			if (wentry.behav == behav) {
			    collideEntryList.remove(wentry);
			}
		    }
		    WakeupOnCollisionExit wexit;
		    WakeupOnCollisionExit wexitArr[] =
			(WakeupOnCollisionExit []) collideExitList.toArray();
		    for (int j=collideExitList.arraySize()-1; j>=0; j--) {
			wexit = wexitArr[j];
			if (wexit.behav == behav) {
			    collideExitList.remove(wexit);
			}
		    }
		}
	    }
	}

	if (bhNodeCount < 1) {
	    return;
	}

	int index = getBHTreeIndex(((BHLeafNode) bhNodeArr[0]).getLocale());
	if (index < 0) {
		// Issue 353: must clear array after we are done with it
		clearBhNodeArr();
		return;
	}
	BHTree currTree = bhTreeArr[index];
	currTree.delete(bhNodeArr, bhNodeCount);

	// Issue 353: must clear array after we are done with it
	clearBhNodeArr();

        // It is safe to do it here since only GeometryStructure
	// thread invoke wakeupOnCollisionEntry/Exit .toArray()

	wakeupOnCollisionEntry.clearMirror();
	wakeupOnCollisionMovement.clearMirror();
	wakeupOnCollisionExit.clearMirror();

	synchronized (collideListLock) {
	    collideEntryList.clearMirror();
	    collideExitList.clearMirror();
	}
    }


    private void processBoundsChanged(Object[] nodes, boolean transformChanged) {

	int index;
	Object node;

	clearBhNodeArr();

	for (int i = 0; i < nodes.length; i++) {
	    node = nodes[i];
	    if (node instanceof GeometryAtom) {
		synchronized (node) {

		    GeometryAtom geomAtom = (GeometryAtom) node;
                    if (geomAtom.bhLeafNode != null) {
			addToBhNodeArr(geomAtom.bhLeafNode);
		    }
		}
	    } else if (node instanceof GroupRetained) {

		GroupRetained group = (GroupRetained) node;
                if (group.nodeType != NodeRetained.SWITCH) {
		    synchronized (node) {
		        if (group.bhLeafNode != null) {
			    addToBhNodeArr(group.bhLeafNode);
		        }
		    }
		}
	    }
	}

	if (bhNodeCount < 1) {
	    return;
	}

	index = getBHTreeIndex(((BHLeafNode)bhNodeArr[0]).getLocale());

	if (index >= 0) {
	    bhTreeArr[index].boundsChanged(bhNodeArr, bhNodeCount);
	}

        // Issue 353: must clear array after we are done with it
        clearBhNodeArr();

    }

    private void processTransformChanged(UpdateTargets targets) {

	int i, j, index;
        Object[] nodes, nodesArr;
	UnorderList arrList;
	int size;

	clearBhNodeArr();

	arrList = targets.targetList[Targets.GEO_TARGETS];

	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);

	    for (j = 0; j < size; j++) {
		nodes = (Object[])nodesArr[j];
		for (i = 0; i < nodes.length; i++) {
		    GeometryAtom geomAtom = (GeometryAtom) nodes[i];
		    synchronized (geomAtom) {
			if (geomAtom.bhLeafNode != null) {
			    addToBhNodeArr(geomAtom.bhLeafNode);
			}
		    }
		}
	    }
	}


	arrList = targets.targetList[Targets.GRP_TARGETS];
	if (arrList != null) {
	    size = arrList.size();
	    nodesArr = arrList.toArray(false);
	    for ( j = 0; j < size; j++) {
		nodes = (Object[])nodesArr[j];
		for ( i = 0; i < nodes.length; i++) {
		    GroupRetained group = (GroupRetained) nodes[i];
                    if (group.nodeType != NodeRetained.SWITCH) {
		        synchronized (group) {
		            if (group.bhLeafNode != null) {
			        addToBhNodeArr(group.bhLeafNode);
		            }
		        }
		    }
		}
	    }
	}

	if (bhNodeCount < 1) {
	    return;
	}

	index = getBHTreeIndex(((BHLeafNode)bhNodeArr[0]).getLocale());

	if (index >= 0) {
	    bhTreeArr[index].boundsChanged(bhNodeArr, bhNodeCount);

	}

        // Issue 353: must clear array after we are done with it
        clearBhNodeArr();

    }

    // This method is called by RenderBin to get a array of possibly visible
    // sub-trees.
    // bhTrees mustn't be null.
    // Return true if bhTree's root in encompass by frustumBBox.

    boolean getVisibleBHTrees(RenderBin rBin,
			      BoundingBox frustumBBox,
			      Locale locale, long referenceTime,
			      boolean stateChanged,
			      int visibilityPolicy) {

	int i, j;
	boolean unviInFB = true;

	// System.err.println("GeometryStructure : view's locale is " + locale);
	lock.readLock();

        // Issue 353: create a new array list each time rather than passing it
        // in. This will not generate too much garbage, since we only call
        // this once per frame and it is very short-lived.
	ArrayList bhTrees = new ArrayList();
	if (bhTreeCount == 1) {
	    // For debugging only.
	    if (J3dDebug.devPhase) {
		if (J3dDebug.doDebug(J3dDebug.geometryStructure, J3dDebug.LEVEL_2)) {
		    System.err.println("GeometryStructure : In simple case");
		    System.err.println("GeometryStructure : view's locale is " +
				       locale);
		    System.err.println("GeometryStructure : bhTreeArr[0].locale is " +
				       bhTreeArr[0].locale);
		}
	    }
	    // One locale case - Lets make the simple case fast.
	    synchronized(visLock) {
		unviInFB = bhTreeArr[0].getVisibleBHTrees(rBin, bhTrees, frustumBBox,
							  referenceTime,
							  stateChanged,
							  visibilityPolicy, true);
	    }
	}
	else {
	    // Multiple locale case.

	    // For debugging only.
	    if (J3dDebug.devPhase)
		J3dDebug.doDebug(J3dDebug.geometryStructure, J3dDebug.LEVEL_2,
				 "GeometryStructure : bhTreeCount is " +
				 universe.geometryStructure.bhTreeCount +
				 " view's locale is " + locale + "\n");

	    BoundingBox localeFrustumBBox = new BoundingBox();

	    synchronized(visLock) {

		for (j=0; j<bhTreeCount; j++) {
		    if (J3dDebug.devPhase) {
			J3dDebug.doDebug(J3dDebug.geometryStructure, J3dDebug.LEVEL_2,
					 "GeometryStructure : bhTreeArr[" + j +
					 "] is " +
					 bhTreeArr[j].locale + "\n");
		    }
		    if (!locale.hiRes.equals(bhTreeArr[j].locale.hiRes)) {
			bhTreeArr[j].locale.hiRes.difference(locale.hiRes, localeTrans);

			if (J3dDebug.devPhase) {
			    J3dDebug.doDebug(J3dDebug.geometryStructure,
					     J3dDebug.LEVEL_2,
					     "localeTrans is " + localeTrans +
					     "GeometryStructure : localeFrustumBBox " +
					     localeFrustumBBox + "\n" );
			}

			// Need to translate view frustumBBox here.
			localeFrustumBBox.lower.x = frustumBBox.lower.x + localeTrans.x;
			localeFrustumBBox.lower.y = frustumBBox.lower.y + localeTrans.y;
			localeFrustumBBox.lower.z = frustumBBox.lower.z + localeTrans.z;
			localeFrustumBBox.upper.x = frustumBBox.upper.x + localeTrans.x;
			localeFrustumBBox.upper.y = frustumBBox.upper.y + localeTrans.y;
			localeFrustumBBox.upper.z = frustumBBox.upper.z + localeTrans.z;
		    }
		    else {
			frustumBBox.copy(localeFrustumBBox);
		    }

		    if(!(bhTreeArr[j].getVisibleBHTrees(rBin, bhTrees,
							localeFrustumBBox,
							referenceTime,
							stateChanged,
							visibilityPolicy,
							false))) {
			unviInFB = false;
		    }
		}
	    }
	}

	lock.readUnlock();
	return unviInFB;
    }

    GeometryAtom[] pickAll(Locale locale, PickShape shape) {

	int i;
 	UnorderList hitList = new UnorderList(BHNode.class);
	hitList.clear();

	lock.readLock();

	i = getBHTreeIndex(locale);
	if (i < 0) {
	    lock.readUnlock();
	    return null;
	}

	bhTreeArr[i].select(shape, hitList);
	lock.readUnlock();

	int size = hitList.size();

	if (size < 1)
	    return null;

	BHNode[] hitArr = (BHNode []) hitList.toArray(false);

	GeometryAtom[] geometryAtoms = new GeometryAtom[size];
	for (i=0; i<size; i++) {
	    geometryAtoms[i] = (GeometryAtom)(((BHLeafNode)hitArr[i]).leafIF);
	}

	return geometryAtoms;
    }

    GeometryAtom pickAny(Locale locale, PickShape shape) {

	int i;

	BHNode hitNode = null;

	lock.readLock();

	i = getBHTreeIndex(locale);
	if (i < 0) {
	    lock.readUnlock();
	    return null;
	}

	hitNode = bhTreeArr[i].selectAny(shape);

	lock.readUnlock();

	if (hitNode == null)
	    return null;

	return (GeometryAtom)(((BHLeafNode)hitNode).leafIF);

    }


    void addWakeupOnCollision(WakeupOnCollisionEntry w) {

	boolean needTrigger = true;

	// Cleanup, since collideEntryList did not remove
	// its condition in removeWakeupOnCollision
	synchronized (collideListLock) {
	    WakeupOnCollisionEntry collideEntryArr[] =
		(WakeupOnCollisionEntry []) collideEntryList.toArray();
	    WakeupOnCollisionEntry wentry;
	    for (int i=collideEntryList.arraySize()-1; i>=0; i--) {
		wentry = collideEntryArr[i];
		if ((wentry.behav == w.behav) &&
		    (wentry.geometryAtoms == w.geometryAtoms)) {
		    collideEntryList.remove(i);
		    needTrigger = false;
		    break;
		}
	    }
	}

	// add to wakeup list
	wakeupOnCollisionEntry.add(w);
	w.updateCollisionBounds(false);
	// check for collision and triggered event
	BHLeafInterface target = collide(w.behav.locale,
					 w.accuracyMode,
					 w.geometryAtoms,
					 w.vwcBounds,
					 w.boundingLeaf,
					 w.armingNode,
					 null);

	if (target != null) {
	    collideEntryList.add(w);
	    w.setTarget(target);
	}

	if ((target != null) && (needTrigger)) {
	    w.setTriggered();
	}
    }


    void addWakeupOnCollision(WakeupOnCollisionExit w) {

	// Cleanup, since collideExitList did not remove
	// its condition in removeWakeupOnCollision
	boolean needTrigger = true;

	synchronized (collideListLock) {
	    WakeupOnCollisionExit collideExitArr[] =
		(WakeupOnCollisionExit []) collideExitList.toArray();
	    WakeupOnCollisionExit wexit;
	    for (int i=collideExitList.arraySize()-1; i>=0; i--) {
		wexit = collideExitArr[i];
		if ((wexit.behav == w.behav) &&
		    (wexit.geometryAtoms == w.geometryAtoms)) {
		    collideExitList.remove(i);
		    needTrigger = false;
		    break;
		}
	    }
	}

	// add condition
	wakeupOnCollisionExit.add(w);
	w.updateCollisionBounds(false);
	BHLeafInterface target = collide(w.behav.locale,
					 w.accuracyMode,
					 w.geometryAtoms,
					 w.vwcBounds,
					 w.boundingLeaf,
					 w.armingNode,
					 null);

	if (target != null) {
	    // store the target that cause this condition to collide
	    // this is used when this condition is triggered.
	    w.setTarget(target);
	    collideExitList.add(w);
	}

	if (!needTrigger) {
	    return;
	}
	// see if the matching wakeupOnCollisionEntry
	// condition exists

	synchronized (collideListLock) {
	    WakeupOnCollisionEntry collideEntryArr[] =
		(WakeupOnCollisionEntry []) collideEntryList.toArray();
	    WakeupOnCollisionEntry wentry;

	    for (int i=collideEntryList.arraySize()-1; i>=0; i--) {
		wentry = collideEntryArr[i];
		if ((wentry.behav == w.behav) &&
		    (wentry.geometryAtoms == w.geometryAtoms)) {
		    // Should not call collideEntryList.remove(i);
		    // Otherwise wakeupOn for Entry case may call several
		    // time at when initialize if collide
		    if (target == null) {
			w.setTriggered();
		    }
		    break;
		}
	    }
	}
    }

    void addWakeupOnCollision(WakeupOnCollisionMovement w) {
	wakeupOnCollisionMovement.add(w);
	w.updateCollisionBounds(false);
	BHLeafInterface target = collide(w.behav.locale,
					 w.accuracyMode,
					 w.geometryAtoms,
					 w.vwcBounds,
					 w.boundingLeaf,
					 w.armingNode,
					 w);
	if (target != null) {
	    w.setTarget(target);
	    collideMovementList.add(w);
	}
    }

    void removeWakeupOnCollision(WakeupOnCollisionEntry wentry) {
	wakeupOnCollisionEntry.remove(wentry);
	// No need to remove collideEntry, it is used next time
	// when WakeupOnExitCollision is added to determine
	// whether to trigger it.
    }

    void removeWakeupOnCollision(WakeupOnCollisionExit wexit) {
	wakeupOnCollisionExit.remove(wexit);
	// No need to remove collideExit, it is used next time
	// when WakeupOnExitCollision is added to determine
	// whether to trigger it.
    }


    void removeWakeupOnCollision(WakeupOnCollisionMovement wmovement) {
	wakeupOnCollisionMovement.remove(wmovement);
	collideMovementList.remove(wmovement);  // remove if exists
    }

    /**
     * This method test all wakeupOnCollision list and trigger the
     * condition if collision occurs.
     */
    void processCollisionDetection() {
	int i, idx;
	BHLeafInterface target;

	// handle WakeupOnCollisionEntry
	WakeupOnCollisionEntry wentry;
	WakeupOnCollisionEntry wentryArr[] = (WakeupOnCollisionEntry [])
                                       wakeupOnCollisionEntry.toArray();

	for (i = wakeupOnCollisionEntry.arraySize()-1; i >=0; i--) {
	    wentry = wentryArr[i];
	    wentry.updateCollisionBounds(reEvaluateWakeupCollisionGAs);
	    target = collide(wentry.behav.locale,
			     wentry.accuracyMode,
			     wentry.geometryAtoms,
			     wentry.vwcBounds,
			     wentry.boundingLeaf,
			     wentry.armingNode,
			     null);
	    idx = collideEntryList.indexOf(wentry);

	    if (target != null) {
		if (idx < 0) {
		    collideEntryList.add(wentry);
		    wentry.setTarget(target);
		    wentry.setTriggered();
		}
	    } else {
		if (idx >= 0) {
		    collideEntryList.remove(idx);
		}
	    }
	}

	// handle WakeupOnCollisionMovement

	WakeupOnCollisionMovement wmove;
	WakeupOnCollisionMovement wmoveArr[] = (WakeupOnCollisionMovement [])
                                       wakeupOnCollisionMovement.toArray();

	for (i = wakeupOnCollisionMovement.arraySize()-1; i >=0; i--) {
	    wmove = wmoveArr[i];
	    wmove.updateCollisionBounds(reEvaluateWakeupCollisionGAs);
	    target = collide(wmove.behav.locale,
			     wmove.accuracyMode,
			     wmove.geometryAtoms,
			     wmove.vwcBounds,
			     wmove.boundingLeaf,
			     wmove.armingNode,
			     wmove);
	    idx = collideMovementList.indexOf(wmove);
	    if (target != null) {
		if (idx < 0) {
		    collideMovementList.add(wmove);
		    wmove.setTarget(target);
		} else {
		    if (!wmove.duplicateEvent) {
			wmove.setTriggered();
		    }
		}
	    } else {
		if (idx >= 0) {
		    collideMovementList.remove(idx);
		    wmove.lastSrcBounds = null;
		    wmove.lastDstBounds = null;
		}
	    }
	}


	// Finally, handle WakeupOnCollisionExit

	WakeupOnCollisionExit wexit;
	WakeupOnCollisionExit wexitArr[] = (WakeupOnCollisionExit [])
                                       wakeupOnCollisionExit.toArray();

	for (i = wakeupOnCollisionExit.arraySize()-1; i >=0; i--) {
	    wexit = wexitArr[i];
	    wexit.updateCollisionBounds(reEvaluateWakeupCollisionGAs);
	    target = collide(wexit.behav.locale,
			     wexit.accuracyMode,
			     wexit.geometryAtoms,
			     wexit.vwcBounds,
			     wexit.boundingLeaf,
			     wexit.armingNode,
			     null);
	    idx = collideExitList.indexOf(wexit);
	    if (target != null) {
		if (idx < 0) {
		    collideExitList.add(wexit);
		    wexit.setTarget(target);
		}
	    } else {
		if (idx >= 0) {
		    collideExitList.remove(idx);
		    wexit.setTriggered();
		}
	    }
	}

    }


    /**
     * Check for duplicate WakeupOnCollisionMovement event.
     * We don't want to continue deliver event even though the
     * two colliding object did not move but this Geometry update
     * thread continue to run due to transform change in others
     * shape not in collision.
     */
    void checkDuplicateEvent(WakeupOnCollisionMovement wmove,
			     Bounds bound,
			     BHLeafInterface hitNode) {
	Bounds hitBound;

        if ((wmove.lastSrcBounds != null) &&
	    wmove.lastSrcBounds.equals(bound)) {
	    if (hitNode instanceof GeometryAtom) {
		hitBound = ((GeometryAtom) hitNode).source.vwcBounds;
	    } else {
		hitBound = ((GroupRetained) hitNode).collisionVwcBounds;
	    }
	    if ((wmove.lastDstBounds != null) &&
		wmove.lastDstBounds.equals(hitBound)) {
		wmove.duplicateEvent = true;
	    } else {
		wmove.duplicateEvent = false;
		wmove.lastDstBounds = (Bounds) hitBound.clone();
	    }
	} else {
	    wmove.duplicateEvent = false;
	    wmove.lastSrcBounds = (Bounds) bound.clone();
	}
    }


    /**
     * check if either the geomAtoms[] or
     * bound or boundingLeaf collide with BHTree.
     * Only one of geomAtoms, bound, boundingLeaf is non-null.
     * If accurancyMode is USE_GEOMETRY, object geometry is used,
     * otherwise object bounding box is used for collision
     * detection.
     * In case of GROUP & BOUND, the armingNode is used
     * to tell whether the colliding Group is itself or not.
     * Also in case GROUP, geomAtoms is non-null if USE_GEOMETRY.
     * If cond != null, it must be instanceof WakeupOnCollisionMovement
     */
     BHLeafInterface collide(Locale locale,
			     int accurancyMode,
			     UnorderList geomAtoms,
			     Bounds bound,
			     BoundingLeafRetained boundingLeaf,
			     NodeRetained armingNode,
			     WakeupCriterion cond) {

	 lock.readLock();
	 int idx = getBHTreeIndex(locale);

	 if (idx < 0) {
	     lock.readUnlock();
	     return null;
	 }
	 BHLeafInterface hitNode;

	 if (geomAtoms != null) {
	     synchronized (bhTreeArr[idx]) {
		 if ((bound != null) &&
		     (armingNode instanceof GroupRetained)) {
		     // Check Bound intersect first before process
		     // to individual Shape3D geometryAtoms
		     hitNode = bhTreeArr[idx].selectAny(bound,
							accurancyMode,
							(GroupRetained)
							armingNode);
		     if (hitNode == null) {
			 lock.readUnlock();
			 return null;
		     }
		     GeometryAtom galist[] = (GeometryAtom [])
			 geomAtoms.toArray(false);

		     hitNode = bhTreeArr[idx].selectAny(galist,
							geomAtoms.arraySize(),
							accurancyMode);

		     if (hitNode != null) {
			 lock.readUnlock();
			 if (cond != null) {
			     checkDuplicateEvent((WakeupOnCollisionMovement) cond,
						 bound,  hitNode);
			 }
			 return hitNode;
		     }
		 } else {
		     GeometryAtom ga = (GeometryAtom) geomAtoms.get(0);
		     hitNode = bhTreeArr[idx].selectAny(ga, accurancyMode);

		     if (hitNode != null) {
			 lock.readUnlock();
			 if (cond != null) {
			     checkDuplicateEvent((WakeupOnCollisionMovement) cond,
						 ga.source.vwcBounds,
						 hitNode);
			 }
			 return hitNode;
		     }
		 }
	     }
	 } else {
	     if (bound == null) {
		 if (boundingLeaf == null) {
		     lock.readUnlock();
		     return null;
		 }
		 bound = boundingLeaf.transformedRegion;
	     }
	     if (bound == null) {
		 lock.readUnlock();
		 return null;
	     }
	     if (armingNode instanceof GroupRetained) {
		 synchronized (bhTreeArr[idx]) {
		     hitNode = bhTreeArr[idx].selectAny(bound,
							accurancyMode,
							(GroupRetained)
							armingNode);
		     lock.readUnlock();
		     if ((hitNode != null) && (cond != null)) {
			 checkDuplicateEvent((WakeupOnCollisionMovement) cond,
					     bound, hitNode);
		     }
		     return hitNode;
		 }
	     } else {
		 synchronized (bhTreeArr[idx]) {
		     hitNode = bhTreeArr[idx].selectAny(bound, accurancyMode,
							armingNode);
		     lock.readUnlock();
		     if ((hitNode != null) && (cond != null)) {
			 checkDuplicateEvent((WakeupOnCollisionMovement) cond,
					     bound, hitNode);
		     }
		     return hitNode;
		 }
	     }
	 }
	 lock.readUnlock();
	 return null;
    }


    /**
     * This prevents wakeupCondition sent out message and set
     * conditionMet to true but the
     * BehaviorStructure/BehaviorScheduler is not fast enough to
     * process the message and reset conditionMet to false
     * when view deactivate/unregister.
     */
    void resetConditionMet() {
	BehaviorStructure.resetConditionMet(wakeupOnCollisionEntry);
	BehaviorStructure.resetConditionMet(wakeupOnCollisionExit);
	BehaviorStructure.resetConditionMet(wakeupOnCollisionMovement);
    }

    /**
     * This processes a switch change.
     */
    private void processSwitchChanged(J3dMessage m) {

//        int i;
//        UnorderList arrList;
//        int size, treeIndex;
//        Object[] nodes;
//        LeafRetained leaf;

/* is now a NOOP

        UpdateTargets targets = (UpdateTargets)m.args[0];

        arrList = targets.targetList[Targets.GEO_TARGETS];

        if (arrList != null) {
            size = arrList.size();
            nodes = arrList.toArray(false);

            treeIndex = getBHTreeIndex(((LeafRetained)nodes[0]).locale);

            for (i=0; i<size; i++) {
                leaf = (LeafRetained)nodes[i];
            }
        }
*/
    }

    @Override
    void cleanup() {
	collideEntryList.clear();
	collideExitList.clear();
	collideMovementList.clear();
	wakeupOnCollisionEntry.clear();
	wakeupOnCollisionExit.clear();
	wakeupOnCollisionMovement.clear();
    }
}
