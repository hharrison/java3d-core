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

import java.util.*;

/**
 * A transform update is a object that manages TransformGroups
 */

class TransformStructure extends J3dStructure implements ObjectUpdate {
    /**
     * An ArrayList of TransformGroups to traverse
     */
    UnorderList traverseList = new UnorderList(TransformGroupRetained.class);
    
    /**
     * A Parallel ArrayList of Transforms for the traverse list
     */
    UnorderList transformList = new UnorderList(Transform3D.class);

    ArrayList objectList = new ArrayList();

    /**
     * arraylist of the bounding leaf users affected by the transform
     */
    ArrayList blUsers = new ArrayList();

    // to gather transform targets
    UpdateTargets targets = new UpdateTargets();

    /**
     * An arrayList of nodes that need collisionBounds updates 
     */
    ArrayList collisionObjectList = new ArrayList();

    // The object that contains the dynamic HashKey - a string type object
    HashKey key = new HashKey(250);

    // List of dirty TransformGroups
    ArrayList dirtyTransformGroups = new ArrayList();

    // Associated Keys with the dirtyNodeGroup
    ArrayList keySet = new ArrayList();

    // current locale under traversal
    Locale locale = null;    

    // The transform used in intermediate calc
    Transform3D currentTrans = new Transform3D();

    TransformGroupRetained tgs[];
    Transform3D t3ds[];

    // the active list contains changed TransformGroup minus those that
    // have been switched-off, plus those that have been changed but
    // just switched-on
    UnorderList activeTraverseList =
		new UnorderList(TransformGroupRetained.class);

    // contains TG that have been previously changed but just switched-on
    ArrayList switchDirtyTgList = new ArrayList(1);

    boolean lazyUpdate = false;

    // ArrayList of switches that have changed, use for lastSwitchOn updates
    ArrayList switchChangedList = new ArrayList();

    // true if already in MasterControl's update object list
    boolean inUpdateObjectList = false;

    /**
     * This constructor does nothing
     */
    TransformStructure(VirtualUniverse u) {
	super(u, J3dThread.UPDATE_TRANSFORM);
    }
  
    void processMessages(long referenceTime) {
	J3dMessage[] messages = getMessages(referenceTime);
	int nMsg = getNumMessage();
	J3dMessage m;
	int i, index;

	if (nMsg <= 0) {
	    return;
	}

	targets.clearNodes();
	objectList.clear();
	blUsers.clear();
	inUpdateObjectList = false;

	synchronized (universe.sceneGraphLock) {
	    // first compact the TRANSFORM_CHANGED messages by going
	    // backwards through the messages
	    for (i = (nMsg-1); i >= 0; i--) {
		m = messages[i];
		if (m.type == J3dMessage.TRANSFORM_CHANGED) {
		    index = traverseList.indexOf(m.args[1]);
		    // if this transform group isn't in our list, add
		    // the information
		    if (index == -1) {
			traverseList.add(m.args[1]);
			transformList.add(m.args[2]);
		    }
		}
	    }
	    
	    for (i=0; i<nMsg; i++) {
		m = messages[i];
		
		switch (m.type) {
		case J3dMessage.INSERT_NODES:
		    objectList.add(m.args[0]);
		    if (m.args[1] != null) {
			TargetsInterface ti = (TargetsInterface)m.args[1];
			ti.updateCachedTargets(
					       TargetsInterface.TRANSFORM_TARGETS,
					       (CachedTargets[])m.args[2]);
		    }
		    break;
		case J3dMessage.REMOVE_NODES:
		    removeNodes(m);
		    break;
		case J3dMessage.SWITCH_CHANGED:
		    processSwitchChanged(m);
		    break;
		case J3dMessage.SHAPE3D_CHANGED:
		    objectList.add(m.args[3]);
		    if (m.args[4] != null) {
			TargetsInterface ti = (TargetsInterface)m.args[4];
			ti.updateCachedTargets(
					       TargetsInterface.TRANSFORM_TARGETS,
					       (CachedTargets[])m.args[5]);
		    }
		    break;
		case J3dMessage.GEOMETRY_CHANGED:
		    objectList.add(m.args[0]);
		    break;
		case J3dMessage.MORPH_CHANGED:
		    objectList.add(m.args[3]);
		    break;
		case J3dMessage.TEXT3D_DATA_CHANGED:
		    objectList.add(m.args[1]);
		    Object tiArr[] = (Object[])m.args[2];
		    if (tiArr != null) {
			Object newCtArr[] = (Object[])m.args[3];
			for (int j=0; j<tiArr.length;j++) {
			    TargetsInterface ti = 
				(TargetsInterface)tiArr[j];
			    ti.updateCachedTargets(
						   TargetsInterface.TRANSFORM_TARGETS,
						   (CachedTargets[])newCtArr[j]);
			}
		    }
		    break;
		case J3dMessage.TEXT3D_TRANSFORM_CHANGED:
		    objectList.add(m.args[0]);
		    break;
		case J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED:
		    processBoundsAutoComputeChanged(m);
		    break;
		case J3dMessage.REGION_BOUND_CHANGED:
		    processRegionBoundChanged(m);
		    break;
		case J3dMessage.COLLISION_BOUND_CHANGED:
		    processCollisionBoundChanged(m);
		    break;
		}
		m.decRefcount();
	    }
	    processCurrentLocalToVworld();
	    
	    // XXXX: temporary -- processVwcBounds will be
	    // done in GeometryStructure
	    if (objectList.size() > 0) { 
		processGeometryAtomVwcBounds();
	    }
	    processVwcBounds();
	}

	Arrays.fill(messages, 0, nMsg, null);
    }

    void processCurrentLocalToVworld() {
	int i, j, tSize, sSize;
	TransformGroupRetained tg;
	BranchGroupRetained bgr;
	Transform3D t;
        TransformGroupData data;

	lazyUpdate = false;

	tSize = transformList.size();
        sSize = switchDirtyTgList.size();
        if (tSize <= 0 && sSize <= 0) {
            return;
        }

        // process TG with setTransform changes
	// update Transform3D, switchDirty and lToVwDrity flags
	if (tSize > 0) {
            tgs = (TransformGroupRetained[])traverseList.toArray(false);
            t3ds = (Transform3D[])transformList.toArray(false);
            for (i=0; i<tSize; i++) {
	        tg = tgs[i];
                tg.currentTransform.set(t3ds[i]);
                VirtualUniverse.mc.addToTransformFreeList(t3ds[i]);

                synchronized(tg) { // synchronized with tg.set/clearLive
                if(tg.perPathData != null) {
                  if (! tg.inSharedGroup) {
                    data = tg.perPathData[0];
                    if (! data.switchState.inSwitch) {
                        // always add to activetraverseList if not in switch
                        activeTraverseList.add(tg);
                        data.markedDirty = true;
                        data.switchDirty = false;
                    } else {
                        // if in switch, add to activetraverseList only if it is
                        // currently switched on, otherwise, mark it as
                        // switchDirty
                        if (data.switchState.currentSwitchOn) {
                            activeTraverseList.add(tg);
                            data.switchDirty = false;
                            data.markedDirty = true;
                        } else {
                            data.switchDirty = true;
                            data.markedDirty = false;
                        }
                    }
                  } else {
                    int npaths = tg.perPathData.length;
                    boolean added = false;

                    for (int k=0; k<npaths; k++) {
                        data = tg.perPathData[k];
                        if (!data.switchState.inSwitch) {
                            if (!added) {
                                // add to activetraverseList if not in switch
                                added = true;
                                activeTraverseList.add(tg);
                            }
                            data.markedDirty = true;
                            data.switchDirty = false;
                        } else {
                            // if in switch, add to activetraverseList only if
                            // it is currently switched on, otherwise, 
                            // mark it as switchDirty
                            if (data.switchState.currentSwitchOn) {
                                if (!added) {
                                    added = true;
                                    activeTraverseList.add(tg);
                                }
                                data.switchDirty = false;
                                data.markedDirty = true;
                            } else {
                                data.switchDirty = true;
                                data.markedDirty = false;
                            }
                        }
                    }
                  }
                }
                }
            }
        }

        // merge switchDirty into activeTraverseList
        if (sSize > 0) {
            int size = switchDirtyTgList.size();
            for (i=0; i<size; i++) {
                // Note: UnorderList does not implement addAll
                activeTraverseList.add(switchDirtyTgList.get(i));
            }
            switchDirtyTgList.clear();
	    lazyUpdate = true;
        }

        // activeTraverseList contains switched-on tg as well
        tgs = (TransformGroupRetained[])activeTraverseList.toArray(false);
        tSize = activeTraverseList.size();

        // process active TGs
        if (tSize > 0) {

            sortTransformGroups(tSize);

            // update lToVw and gather targets
            for (i=0; i<tSize; i++) {
                tgs[i].processChildLocalToVworld(dirtyTransformGroups, keySet,
                                                targets, blUsers);
            }
	    if (!inUpdateObjectList) {
                VirtualUniverse.mc.addMirrorObject(this);
	        inUpdateObjectList = true;
	    }
        }

	transformList.clear();
	traverseList.clear();
        activeTraverseList.clear();
    }


    private void sortTransformGroups(int size) {
        if (size < 7) {
            insertSort(size);
        } else {
            quicksort(0, size-1);
        }
    }

    // Insertion sort on smallest arrays
    private void insertSort(int size) {
        for (int i=0; i<size; i++) {
            for (int j=i; j>0 && 
		 (tgs[j-1].maxTransformLevel > tgs[j].maxTransformLevel); j--) {
                TransformGroupRetained tmptg = tgs[j];
                tgs[j] = tgs[j-1];
                tgs[j-1] = tmptg;
            }
        }
    }

    private void quicksort( int l, int r ) {
        int i = l;
        int j = r;
        double k = tgs[(l+r) / 2].maxTransformLevel;
        do {
            while (tgs[i].maxTransformLevel<k) i++;
            while (k<tgs[j].maxTransformLevel) j--;
            if (i<=j) {
                TransformGroupRetained tmptg = tgs[i];
                tgs[i] = tgs[j];
                tgs[j] = tmptg;

                i++;
                j--;
             }
         } while (i<=j);

         if (l<j) quicksort(l,j);
         if (l<r) quicksort(i,r);
    }

    
    public void updateObject() {
	processLastLocalToVworld();
	processLastSwitchOn();
    }


    void processLastSwitchOn() {
        int size = switchChangedList.size();
        if (size > 0) {
            SwitchState switchState;

            for (int i = 0; i < size; i++) {
                switchState  = (SwitchState)switchChangedList.get(i);
                switchState.updateLastSwitchOn();
            }
            switchChangedList.clear();
        }
    }


    void processLastLocalToVworld() {
	int i, j, k;
	TransformGroupRetained tg;
	HashKey key;

	
	int dTGSize = dirtyTransformGroups.size();
	if (J3dDebug.devPhase && J3dDebug.debug) {
	    J3dDebug.doDebug(J3dDebug.transformStructure, J3dDebug.LEVEL_5,
                        "processLastLocalToVworld(): dTGSize= " + dTGSize + "\n");
	}
	
	for (i=0, k=0; i < dTGSize; i++) {
	    tg  = (TransformGroupRetained)dirtyTransformGroups.get(i);
	    // Check if the transformGroup is still alive
	    
	    // XXXX: This is a hack, should be fixed after EA
	    // Null pointer checking should be removed!
	    // should call trans = tg.getCurrentChildLocalToVworld(key);
	    synchronized(tg) {
		if (tg.childLocalToVworld != null) {
		    if (tg.inSharedGroup) {
			key = (HashKey) keySet.get(k++);
			for (j=0; j<tg.localToVworldKeys.length; j++) {
			    if (tg.localToVworldKeys[j].equals(key)) {
				break;
			    }
			}		  
			if (j < tg.localToVworldKeys.length) {
			    // last index = current index
			    tg.childLocalToVworldIndex[j][NodeRetained.LAST_LOCAL_TO_VWORLD] = 
				tg.childLocalToVworldIndex[j][NodeRetained.CURRENT_LOCAL_TO_VWORLD];
			}
		    }
		    else {
			// last index = current index
			tg.childLocalToVworldIndex[0][NodeRetained.LAST_LOCAL_TO_VWORLD] = 
			    tg.childLocalToVworldIndex[0][NodeRetained.CURRENT_LOCAL_TO_VWORLD];
		    }
		}
		
	    }

	}
	dirtyTransformGroups.clear();
	keySet.clear();
	
    }

    void processGeometryAtomVwcBounds() {


        Shape3DRetained ms;
	GeometryAtom ga;

	//int num_locales = universe.listOfLocales.size();
	int oSize = objectList.size();
	for (int i = 0; i < oSize; i++) {
	    Object[] nodes = (Object[]) objectList.get(i);
	    if (J3dDebug.devPhase && J3dDebug.debug) {
		J3dDebug.doDebug(J3dDebug.transformStructure, J3dDebug.LEVEL_5,
				 "vwcBounds computed this frame = " + nodes.length + "\n");
	    }	    
	    for (int j = 0; j < nodes.length; j++) {
		// If the list has geometry atoms, update the vwc bounds
		synchronized(nodes[j]) {
		    if (nodes[j] instanceof GeometryAtom) {
			ga = (GeometryAtom) nodes[j]; 
                        ms = ga.source;

                        // update mirrorShape's vwcBounds if in use
                        // shape with multiple geometries only needed to be
                        // updated once

                        synchronized(ms.bounds) {
                            ms.vwcBounds.transform(ms.bounds,
                                          ms.getCurrentLocalToVworld(0));
                        }
                        if (ms.collisionBound != null) {
                            ms.collisionVwcBound.transform(
                                          ms.collisionBound,
                                          ms.getCurrentLocalToVworld(0));
			}
			ga.centroidIsDirty = true;
		    }  else if (nodes[j] instanceof GroupRetained) {
			// Update collisionVwcBounds of mirror GroupRetained
			GroupRetained g =  (GroupRetained) nodes[j];
			Bounds bound = (g.sourceNode.collisionBound != null ?
                                       g.sourceNode.collisionBound :
                                       g.sourceNode.getEffectiveBounds());
			g.collisionVwcBounds.transform(bound, 
					g.getCurrentLocalToVworld());
		    }
		}
	    }
	}
	// process collision bounds only update 
	for (int i = 0; i < collisionObjectList.size(); i++) {
	    Object[] nodes = (Object[]) collisionObjectList.get(i);
	    for (int j = 0; j < nodes.length; j++) {
		synchronized(nodes[j]) {
		    if (nodes[j] instanceof GeometryAtom) {
		        ga = (GeometryAtom) nodes[j]; 
                        ms = ga.source;

                        if (ms.collisionVwcBound != null) {
                            ms.collisionVwcBound.transform(
                                       ms.collisionBound,
                                       ms.getCurrentLocalToVworld(0));
                        }
		    } 
		}
            }
        }
	collisionObjectList.clear();
    }

    void processVwcBounds() {


        int size;
        int i,j;
	GeometryAtom ga;
        Shape3DRetained ms;
        Object nodes[], nodesArr[];

        UnorderList arrList = targets.targetList[Targets.GEO_TARGETS];
        if (arrList != null) {
            size = arrList.size();
	    nodesArr = arrList.toArray(false);

            for (i = 0; i<size; i++) {
                nodes = (Object[])nodesArr[i];
                for (j = 0; j < nodes.length; j++) {
                    synchronized(nodes[j]) {
                        ga = (GeometryAtom) nodes[j];
                        ms = ga.source;
                        synchronized(ms.bounds) {
                            ms.vwcBounds.transform(ms.bounds,
                                          ms.getCurrentLocalToVworld(0));
                        }
                        if (ms.collisionBound != null) {
                            ms.collisionVwcBound.transform(
                                          ms.collisionBound,
                                          ms.getCurrentLocalToVworld(0));
                        }
                        ga.centroidIsDirty = true;
		    }
	        }
	    }
	}

        arrList = targets.targetList[Targets.GRP_TARGETS];
        if (arrList != null) {
            size = arrList.size();
	    nodesArr = arrList.toArray(false);

            for (i = 0; i<size; i++) {
                nodes = (Object[])nodesArr[i];
                for (j = 0; j < nodes.length; j++) {
                    // Update collisionVwcBounds of mirror GroupRetained
                    GroupRetained g = (GroupRetained)nodes[j];
                    Bounds bound = (g.sourceNode.collisionBound != null ?
                                       g.sourceNode.collisionBound :
                                       g.sourceNode.getEffectiveBounds());
                    g.collisionVwcBounds.transform(bound,
                                        g.getCurrentLocalToVworld());
 	        }
 	    }
 	}

	// process collision bounds only update 
	for (i = 0; i < collisionObjectList.size(); i++) {
	    nodes = (Object[]) collisionObjectList.get(i);
	    for (j = 0; j < nodes.length; j++) {
		synchronized(nodes[j]) {
		    if (nodes[j] instanceof GeometryAtom) {
		        ga = (GeometryAtom) nodes[j]; 
                        ms = ga.source;

                        if (ms.collisionVwcBound != null) {
                            ms.collisionVwcBound.transform(
                                       ms.collisionBound,
                                       ms.getCurrentLocalToVworld(0));
                        }
		    } 
		}
            }
        }
	collisionObjectList.clear();
    }

    void processRegionBoundChanged(J3dMessage m) {
        // need to update mirrorShape's bounds
        processBoundsChanged((Object[]) m.args[0], (Bounds)m.args[1]);
    }

    void processBoundsChanged(Object[] gaArray, Bounds updateBounds) {
	int i;
        GeometryAtom ga;
	Shape3DRetained ms;

	for (i=0; i<gaArray.length; i++) {
            ga = (GeometryAtom)gaArray[i];
            ms = ga.source;

            // update mirrorShape's bound objects
            // since boundsAutoCompute is false and user specified a bound
            ms.bounds = updateBounds;
            if (ms.collisionBound == null) {
                ms.collisionVwcBound = ms.vwcBounds;
            } 
	}
	objectList.add(gaArray);
    }


    void processCollisionBoundChanged(J3dMessage m) {
	int i;
        Shape3DRetained ms;
        Bounds collisionBound = (Bounds)m.args[1];
	
	if (m.args[0] instanceof GroupRetained) {
            GroupRetained g = (GroupRetained) m.args[0];
            if (g.mirrorGroup != null) {
                objectList.add(g.mirrorGroup);
            }
        } else {
	    Object[] gaArray = (Object[]) m.args[0]; 
            GeometryAtom ga;

            for (i=0; i<gaArray.length; i++) {
                ga = (GeometryAtom)gaArray[i];
                ms = ga.source;

                ms.collisionBound = collisionBound;

                if (ms.collisionBound != null) {
                    // may be previously points to ms.vwcBounds, therefore
                    // needs to create one
                    ms.collisionVwcBound = (Bounds)ms.collisionBound.clone();
                } else {
                    ms.collisionVwcBound = ms.vwcBounds;
                }
            }
	    collisionObjectList.add(gaArray);
	}
    }

    void processBoundsAutoComputeChanged(J3dMessage m) {
        // need to update mirrorShape's bounds
        processBoundsChanged((Object[]) m.args[0], (Bounds) m.args[1]);
    }

    void processSwitchChanged(J3dMessage m) {
        ArrayList switchList = (ArrayList)m.args[2];


        int size = switchList.size();
	if (size > 0) {
	    // update SwitchState's CurrentSwitchOn flag
            SwitchState switchState;
            for (int j=0; j<size; j++) {
                switchState = (SwitchState)switchList.get(j);
                switchState.updateCurrentSwitchOn();
            }

            // process switch dirty TranformGroups
            UpdateTargets targets = (UpdateTargets)m.args[0];
            UnorderList arrList = targets.targetList[Targets.GRP_TARGETS];

            if (arrList != null) {

                Object[] nodes;
            	Object[] nodesArr = arrList.toArray(false);
                int aSize = arrList.size();
		int nPaths;
		boolean added;

                TransformGroupRetained tg;
		TransformGroupData data;

                for (int j=0; j<aSize; j++) {
		    nodes = (Object[])nodesArr[j];

                    for (int i=0; i<nodes.length; i++) {
			added = false;
                        tg = (TransformGroupRetained)nodes[i];

                        synchronized(tg) { // synchronized with tg.set/clearLive
			if (tg.perPathData != null) {
                            nPaths = tg.perPathData.length;

                            for (int k=0; k<nPaths; k++) {
                                data = tg.perPathData[k];
                                if (data.switchState.currentSwitchOn &&
					data.switchDirty) {
				    if (!added) {
				        // only needed to add once
                                        switchDirtyTgList.add(tg);
                                        added = true;
				    }
                                    data.switchDirty = false;
                                    data.markedDirty = true;
                                }
                            }
                        }
                        }
                    }
                }
	    }

            // gather a list of SwitchState for lastSwitchOn update
            switchChangedList.addAll(switchList);

	    if (!inUpdateObjectList) {
                VirtualUniverse.mc.addMirrorObject(this);
	        inUpdateObjectList = true;
	    }
	}
    }

    UpdateTargets getTargetList() {
	return targets;
    }

    ArrayList getBlUsers() {
 	return blUsers;
    }

    boolean getLazyUpdate() {
 	return lazyUpdate;
    }

    void removeNodes(J3dMessage m) {
	if (m.args[1] != null) {
	    TargetsInterface ti = (TargetsInterface)m.args[1];
	    ti.updateCachedTargets(
				   TargetsInterface.TRANSFORM_TARGETS,
				   (CachedTargets[])m.args[2]);
	}
    }

    void cleanup() {}
}
