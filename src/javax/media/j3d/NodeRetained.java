/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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


import java.util.ArrayList;
import java.util.Vector;

/**
 * The Node class provides an abstract class for all Group and Leaf
 * Nodes.  It provides a common framework for constructing a Java 3D
 * scene graph, including bounding volumes and parent pointers.
 */
abstract class NodeRetained extends SceneGraphObjectRetained implements NnuId {

    // All the node types in the scene graph
    static final int BACKGROUND		= 1;
    static final int CLIP 		= 2;
    static final int LINEARFOG 		= 3;
    static final int EXPONENTIALFOG 	= 4;
    static final int AMBIENTLIGHT 	= 5;
    static final int DIRECTIONALLIGHT 	= 6;
    static final int POINTLIGHT		= 7;
    static final int SPOTLIGHT 		= 8;
    static final int LINK 		= 9;
    static final int MORPH 		= 10;
    static final int SHAPE 		= 11;
    static final int BACKGROUNDSOUND 	= 12;
    static final int POINTSOUND 	= 13;
    static final int CONESOUND 		= 14;
    static final int SOUNDSCAPE 	= 15;
    static final int VIEWPLATFORM 	= 16;
    static final int BEHAVIOR 		= 17;

    static final int SWITCH 		= 18;
    static final int BRANCHGROUP 	= 19;
    static final int ORDEREDGROUP 	= 20;
    static final int DECALGROUP		= 21;
    static final int SHAREDGROUP 	= 22;
    static final int GROUP 		= 23;
    static final int TRANSFORMGROUP 	= 24;
    static final int BOUNDINGLEAF 	= 25;
    static final int MODELCLIP 		= 26;
    static final int ALTERNATEAPPEARANCE= 27;
    static final int ORIENTEDSHAPE3D    = 28;
    static final int VIEWSPECIFICGROUP  = 29;
    static final int NUMNODES           = 29;

    // traverse flags
    static final int CONTAINS_VIEWPLATFORM 	= 0x1;


    /**
     * The universe that we are in
     */
    VirtualUniverse universe = null;

    /**
     * The locale that this node is attatched to.  This is only non-null
     * if this instance is directly linked into a locale.
     */
    Locale locale = null;

    /**
     * The node's parent.
     */
    NodeRetained parent = null;

    /**
     * The node's internal identifier.
     */
    String nodeId = null;

    /**
     * An int that represents the nodes type.  Used for quick if tests
     * in the traverser.
     */
    int nodeType;

    // This keeps track of how many times this Node is refernced, refCount > 1
    // if node is in a shared group
    int refCount = 0;

    /**
     * This is the index for the child, as seen by its parent.
     */
    int childIndex = -1;

    /**
     * This boolean is true when the node is in a sharedGroup
     */
    boolean inSharedGroup = false;

    /**
     * This indicates if the node is pickable. If this node is not
     * pickable then neither are any children
     */
    boolean pickable = true;

    /**
     * The collidable setting; see getCollidable and setCollidable.
     */
    boolean collidable = true;

    // A list of localToVworld transforms. If inSharedGroup is false,
    // then only localToVworld[0][] is valid.
    // Note: this contains reference to the actual transforms in the
    //		TransformGroupRetained
    Transform3D localToVworld[][] = null;
    int		localToVworldIndex[][] = null;

    static final int  LAST_LOCAL_TO_VWORLD    = 0;
    static final int  CURRENT_LOCAL_TO_VWORLD = 1;

    // A parallel array to localToVworld.  This is the keys for
    // localToVworld transforms in shared groups.
    HashKey localToVworldKeys[] = null;

    /**
     * This boolean is true when the geometric bounds for the node is
     * automatically updated
     */
    boolean boundsAutoCompute = true;

    // "effective" bounds in local coordinate if boundsAutoCompute == F,
    // used for internal operations, not used if boundsAutoCompute == T
    Bounds localBounds;

    // Bounds set by the API
    Bounds apiBounds;

    protected Bounds cachedBounds=null;     // Cached auto compute bounds, could we use localBounds ?
    protected boolean validCachedBounds = false; // Fix to Issue 514
    /**
     * Each element, p, of branchGroupPaths is a list of BranchGroup from
     * root of the tree to this.
     * For BranchGroup under a non-shared group this size of
     * branchGroupPaths is always 1. Otherwise, the size is equal to
     * the number of possible paths to reach this node.
     * This variable is used to cached BranchGroup for fast picking.
     * For non BranchGroupRetained class this is a reference to
     * the previous BranchGroupRetained branchGroupPaths.
     */
ArrayList<BranchGroupRetained[]> branchGroupPaths = new ArrayList<BranchGroupRetained[]>(1);

    // background node whose geometry branch contains this node
    BackgroundRetained geometryBackground = null;

    // closest parent which is a TransformGroupRetained or sharedGroupRetained
    GroupRetained parentTransformLink = null;

    // closest parent which is a SwitchRetained or sharedGroupRetained
    GroupRetained parentSwitchLink = null;

    // static transform if a parent transform group is merged during compile.
    TransformGroupRetained staticTransform = null;

    // orderedId assigned by OrderedGroup parent
    Integer orderedId = null;

    // Id use for quick search.
    int nnuId;

    NodeRetained() {
	// Get a not necessary unique Id.
	nnuId = NnuIdManager.getId();

	localBounds = new BoundingBox((Bounds)null);
    }


    @Override
    public int getId() {
	return nnuId;
    }

    @Override
    public int equal(NnuId obj) {
	int keyId = obj.getId();
	if(nnuId < keyId) {
	    return -1;
	}
	else if(nnuId > keyId) {
	    return 1;
	}
	else { // Found it!
	    return 0;
	}
    }

    Bounds getLocalBounds(Bounds bounds) {
	return (Bounds)bounds.clone();
    }

    /**
     * Sets the geometric bounds of a node.
     * @param bounds the bounding object for the node
     */
    void setBounds(Bounds bounds) {
	apiBounds = bounds;
	if (source.isLive()) {
	    if (!boundsAutoCompute) {
		if (bounds != null) {
		    localBounds = getLocalBounds(bounds);
		    if (staticTransform != null) {
			localBounds.transform(staticTransform.transform);
		    }
		} else {
		    if(localBounds != null) {
			localBounds.set((Bounds)null);
		    }
		    else {
			localBounds = new BoundingBox((Bounds)null);
		    }
		}
	    }
	} else {
	    if (bounds != null) {
		localBounds = getLocalBounds(bounds);
		if (staticTransform != null) {
		    localBounds.transform(staticTransform.transform);
		}
	    } else {
		if(localBounds != null) {
		    localBounds.set((Bounds)null);
		}
		else {
		    localBounds = new BoundingBox((Bounds)null);
		}
	    }
	}
    }

    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    Bounds getEffectiveBounds() {
	Bounds b = null;
	if (localBounds != null && !localBounds.isEmpty()) {
	    b = (Bounds) localBounds.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
	}
	return b;
    }

    Bounds getBounds() {
	return apiBounds;
    }

    /**
     * ONLY needed for SHAPE, MORPH, and LINK node type.
     * Compute the combine bounds of bounds and its localBounds.
     */
    void computeCombineBounds(Bounds bounds) {
	// Do nothing except for Group, Shape3D, Morph, and Link node.
    }


    /**
     * Sets the automatic calcuation of geometric bounds of a node.
     * @param autoCompute is a boolean value indicating if automatic calcuation
     * of bounds
     */
    void setBoundsAutoCompute(boolean autoCompute) {
        if (this.boundsAutoCompute==autoCompute) {
            return;
        }

	this.boundsAutoCompute = autoCompute;
        dirtyBoundsCache();
    }

    /**
     * Gets the auto Compute flag for the geometric bounds.
     * @return the node's auto Compute flag for the geometric bounding object
     */
    boolean getBoundsAutoCompute() {
	return boundsAutoCompute;
    }

    /**
     * Replaces the specified parent by a new parent.
     * @param parent the new parent
     */
    void setParent(NodeRetained parent) {
	this.parent = parent;
    }

/**
 * Returns the parent of the node.
 * @return the parent.
 */
NodeRetained getParent() {
	return parent;
}

    // Transform the input bound by the current LocalToVWorld
    void transformBounds(SceneGraphPath path, Bounds bound) {
	if (!((NodeRetained) path.item.retained).inSharedGroup) {
	    bound.transform(getCurrentLocalToVworld());
	} else {
	    HashKey key = new HashKey("");
	    path.getHashKey(key);
	    bound.transform(getCurrentLocalToVworld(key));
	}
    }


    // Note : key will get modified in this method.
    private void computeLocalToVworld( NodeRetained caller, NodeRetained nodeR,
				       HashKey key, Transform3D l2Vw) {
	int i;

	//  To handle localToVworld under a SG.
	if(nodeR instanceof SharedGroupRetained) {
	    // Get the immediate parent's id and remove last id from key.
	    String nodeId = key.getLastNodeId();

	    SharedGroupRetained sgRetained = (SharedGroupRetained) nodeR;

	    // Search for the right parent.
	    for(i=0; i<sgRetained.parents.size(); i++) {

			if (nodeId.equals(sgRetained.parents.get(i).nodeId)) {
		    // Found the right link. Now traverse upward.

			computeLocalToVworld(caller, sgRetained.parents.get(i), key, l2Vw);
		    return;
		}
	    }
	    // Problem !
	    throw new RuntimeException(J3dI18N.getString("NodeRetained4"));
	}
	else {

		NodeRetained nodeParentR = nodeR.getParent();

	    if(nodeParentR == null) {
		// Base case. It has to be a BG attached to a locale.
		if(((BranchGroupRetained)(nodeR)).locale != null) {
		    l2Vw.setIdentity();
		}
		else {
		    throw new RuntimeException(J3dI18N.getString("NodeRetained5"));
		}
	    }
	    else {
			computeLocalToVworld(caller, nodeParentR, key, l2Vw);

	    }

	}

	if((nodeR instanceof TransformGroupRetained) && (nodeR != caller)) {
	    Transform3D t1 = new Transform3D();
	    ((TransformGroupRetained)(nodeR)).transform.getWithLock(t1);
	    l2Vw.mul(t1);
	} else if ((nodeR == caller) && (staticTransform != null)) {
	    l2Vw.mul(staticTransform.transform);
	}

	return;
    }

    /**
     * Compute the LocalToVworld of this node even though it is not live. We
     * assume the graph is attached at the origin of a locale
     */
    void computeNonLiveLocalToVworld(Transform3D t, Node caller) {
        NodeRetained n = getParent();

        if (n==null)
            t.setIdentity();
        else
            n.computeNonLiveLocalToVworld(t, caller);

        if (this instanceof TransformGroupRetained && this.source!=caller) {
            Transform3D trans = new Transform3D();
            ((TransformGroupRetained)this).getTransform(trans);
            t.mul(trans);
        }

    }

    /**
     * Get the localToVworld transform for a node.
     */
    void getLocalToVworld(Transform3D t) {
	if (inSharedGroup) {
	    throw new IllegalSharingException(J3dI18N.getString("NodeRetained0"));
	}

	// Lock the object while writing into t.
	if (localToVworld == null) {
	    t.setIdentity();
	} else {
	    computeLocalToVworld(this, this, null, t);
	}
    }


    /**
     * Get the localToVworld transform for a node.
     */
    void getLocalToVworld(SceneGraphPath path, Transform3D t) {
        HashKey key = new HashKey("");

        if (inSharedGroup == false) {
	    throw new IllegalSharingException(J3dI18N.getString("NodeRetained1"));
        }
        path.validate(key);
	computeLocalToVworld(this, this, key, t);

    }

    /**
     * Get the localToVworld transform for a node
     */
    void getLocalToVworld(Transform3D t, HashKey key) {
	HashKey newKey = new HashKey(key);
	computeLocalToVworld(this, this, newKey, t);
    }


    /**
     * Get the Locale to which the node is attached
     */
    Locale getLocale() {
	if (inSharedGroup) {
	    throw new IllegalSharingException(J3dI18N.getString("NodeRetained0"));
	}

	return locale;
    }


    /**
     * Get the current localToVworld transform for a node
     */
    Transform3D getCurrentLocalToVworld() {

	if (localToVworld != null) {
	    return localToVworld[0][localToVworldIndex[0][CURRENT_LOCAL_TO_VWORLD]];
	} else {
	    return new Transform3D();
	}
    }

    // this method expects that localToVworld is not null

    Transform3D getCurrentLocalToVworld(int index) {
	return localToVworld[index][localToVworldIndex[index][CURRENT_LOCAL_TO_VWORLD]];
    }

    Transform3D getCurrentLocalToVworld(HashKey key) {

	if (localToVworld != null) {
	    if (!inSharedGroup) {
	        return localToVworld[0][localToVworldIndex[0][CURRENT_LOCAL_TO_VWORLD]];
	    } else {
		int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
		if(i>= 0) {
		    return localToVworld[i][localToVworldIndex[i][CURRENT_LOCAL_TO_VWORLD]];
		}
	    }
	}
	return new Transform3D();
    }

    /**
     * Get the last localToVworld transform for a node
     */
    Transform3D getLastLocalToVworld() {

	if (localToVworld != null) {
	    return localToVworld[0][localToVworldIndex[0][LAST_LOCAL_TO_VWORLD]];
	} else {
	    return new Transform3D();
        }
    }

    Transform3D getLastLocalToVworld(int index) {
	    return localToVworld[index][localToVworldIndex[index][LAST_LOCAL_TO_VWORLD]];
    }

    Transform3D getLastLocalToVworld(HashKey key) {

	if (localToVworld != null) {
	    if (!inSharedGroup) {
	        return localToVworld[0][localToVworldIndex[0][LAST_LOCAL_TO_VWORLD]];
	    } else {
		int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
		if(i>= 0) {
		    return localToVworld[i][localToVworldIndex[i][LAST_LOCAL_TO_VWORLD]];
		}
	    }
	}
	return new Transform3D();
    }

    // Do nothing for NodeRetained.
    void setAuxData(SetLiveState s, int index, int hkIndex) {

    }

    void setNodeData(SetLiveState s) {
	localToVworld = s.localToVworld;
	localToVworldIndex = s.localToVworldIndex;
	localToVworldKeys = s.localToVworldKeys;

	// reference to the last branchGroupPaths
	branchGroupPaths = s.parentBranchGroupPaths;

        parentTransformLink = s.parentTransformLink;
        parentSwitchLink = s.parentSwitchLink;
    }


    // set pickable, recursively update cache result
    void setPickable(boolean pickable) {
	if (this.pickable == pickable)
	    return;

	this.pickable = pickable;

	if (source.isLive()) {
	    synchronized(universe.sceneGraphLock) {
		boolean pick[];
		if (!inSharedGroup) {
		    pick = new boolean[1];
		} else {
		    pick = new boolean[localToVworldKeys.length];
		}

		findPickableFlags(pick);
		updatePickable(localToVworldKeys, pick);
	    }
	}
    }

    void updatePickable(HashKey pickKeys[], boolean pick[]) {
	for (int i=0; i < pick.length; i++) {
	    if (!pickable) {
		pick[i] = false;
	    }
	}
    }

    // get pickable
    boolean getPickable() {
	return pickable;
    }


    // set collidable, recursively update cache result
    void setCollidable(boolean collidable) {
	if (this.collidable == collidable)
	    return;

	this.collidable = collidable;

	if (source.isLive()) {
	    synchronized(universe.sceneGraphLock) {
		boolean collide[];
		if (!inSharedGroup) {
		    collide = new boolean[1];
		} else {
		    collide = new boolean[localToVworldKeys.length];
		}

		findCollidableFlags(collide);
		updateCollidable(localToVworldKeys, collide);
	    }
	}
    }


    // get collidable
    boolean getCollidable() {
	return collidable;
    }


    void updateCollidable(HashKey keys[], boolean collide[]) {
	for (int i=0; i < collide.length; i++) {
	    if (!collidable) {
		collide[i] = false;
	    }
	}
    }

    /**
     * For the default, just pass up to parent
     */
    void notifySceneGraphChanged(boolean globalTraverse){}

    void recombineAbove() {}

    @Override
    void setLive(SetLiveState s) {
	int oldrefCount = refCount;

	doSetLive(s);
	if (oldrefCount <= 0)
	    super.markAsLive();
    }

    // The default set of setLive actions.
    @Override
    void doSetLive(SetLiveState s) {
	int i;
	int oldrefCount = refCount;

	refCount += s.refCount;
	if(!(locale == null || universe == s.universe))
            throw new IllegalSharingException(J3dI18N.getString("NodeRetained3"));
	if(s.locale == null)
	    System.err.println("NodeRetained.setLive() locale is null");


	locale = s.locale;
	inSharedGroup = s.inSharedGroup;

	if (oldrefCount <= 0) {
	    if (listIdx == null) {
		universe = s.universe;
	    } else {
		// sync with getIdxUsed()
		if (s.universe != universe) {
		    synchronized (this) {
			universe = s.universe;
			incIdxUsed();
		    }
		}
	    }
	}
	s.universe.numNodes++;

	//  pickable & collidable array have the same length
	for (i=0; i < s.pickable.length; i++) {
	    if (!pickable) {
		s.pickable[i] = false;
	    }
	    if (!collidable) {
		s.collidable[i] = false;
	    }
	}


	if (oldrefCount <= 0)
	    super.doSetLive(s);

        if (inBackgroundGroup) {
            geometryBackground = s.geometryBackground;
        }

	setNodeData(s);
    }


    /**
     * remove the localToVworld transform for this node.
     */
    void removeNodeData(SetLiveState s) {

        if (refCount <= 0) {
            localToVworld = null;
            localToVworldIndex = null;
            localToVworldKeys = null;
	    // restore to default and avoid calling clear()
	    // that may clear parent reference branchGroupPaths
		branchGroupPaths = new ArrayList<BranchGroupRetained[]>(1);
            parentTransformLink = null;
            parentSwitchLink = null;
	}
	else {
	    // Set it back to its parent localToVworld data. This is b/c the parent has
	    // changed it localToVworld data arrays.
	    localToVworld = s.localToVworld;
	    localToVworldIndex = s.localToVworldIndex;
	    localToVworldKeys = s.localToVworldKeys;

            // Reference of parent branchGroupPaths will not change

	    // no need to reset parentSwitchLink or parentTransformLink
	    // because there are not per path data
	}

    }

    // The default set of clearLive actions
    void clearLive(SetLiveState s) {

	refCount-=s.refCount;

	if (refCount <= 0) {
           super.clearLive();

	   // don't remove the nodeId unless there are no more references
	   if (nodeId != null) {
	     universe.nodeIdFreeList.addElement(nodeId);
	     nodeId = null;
	   }
	}

	universe.numNodes--;


	removeNodeData(s);

	if(refCount <= 0) {
	    locale = null;
	    geometryBackground = null;
	}
    }

    // search up the parent to determine if this node is pickable
    void  findPickableFlags(boolean pick[]) {
	NodeRetained nodeR = this;


	if (!inSharedGroup) {
	    pick[0] = true;
	    nodeR = nodeR.parent;
	    while (nodeR != null) {
		if (!nodeR.pickable) {
		    pick[0] = false;
		    break;
		}
		nodeR = nodeR.parent;
	    }
	} else {
	    HashKey key;
	    for (int i=0; i < pick.length; i++) {
		nodeR = this;
		pick[i] = true;
		key = new HashKey(localToVworldKeys[i]);

		do {
		    if (nodeR instanceof SharedGroupRetained) {
			String nodeId = key.getLastNodeId();
			Vector<NodeRetained> parents = ((SharedGroupRetained)nodeR).parents;
			int sz = parents.size();
			NodeRetained prevNodeR = nodeR;
			for(int j=0; j< sz; j++) {
				NodeRetained linkR = parents.get(j);
			    if (linkR.nodeId.equals(nodeId)) {
				nodeR = linkR;
				break;
			    }
			}
			if (prevNodeR == nodeR) {
			    // branch is already detach
			    return;
			}
		    } else {
			nodeR = nodeR.parent;
		    }
		    if (nodeR == null)
			break;
		    if (!nodeR.pickable) {
			pick[i] = false;
			break;
		    }
		} while (true);
	    }
	}
    }


    // search up the parent to determine if this node is collidable
    void findCollidableFlags(boolean collide[]) {
	NodeRetained nodeR = this;

	if (!inSharedGroup) {
	    collide[0] = true;
	    nodeR = nodeR.parent;
	    while (nodeR != null) {
		if (!nodeR.collidable) {
		    collide[0] = false;
		    break;
		}
		nodeR = nodeR.parent;
	    }
	} else {
	    HashKey key;
	    for (int i=0; i < collide.length; i++) {
		nodeR = this;
		collide[i] = true;
		key = new HashKey(localToVworldKeys[i]);

		do {
		    if (nodeR instanceof SharedGroupRetained) {
			String nodeId = key.getLastNodeId();
			Vector<NodeRetained> parents = ((SharedGroupRetained)nodeR).parents;
			int sz = parents.size();
			NodeRetained prevNodeR = nodeR;
			for(int j=0; j< sz; j++) {
				NodeRetained linkR = parents.get(j);
			    if (linkR.nodeId.equals(nodeId)) {
				nodeR = linkR;
				break;
			    }
			}
			if (nodeR == prevNodeR) {
			    return;
			}
		    } else {
			nodeR = nodeR.parent;
		    }
		    if (nodeR == null)
			break;
		    if (!nodeR.collidable) {
			collide[i] = false;
			break;
		    }
		} while (true);
	    }
	}
    }

    void  findTransformLevels(int transformLevels[]) {
        NodeRetained nodeR = this;
        TransformGroupRetained tg;

        if (!inSharedGroup) {
            transformLevels[0] = -1;
            while (nodeR != null) {
                if (nodeR.nodeType == NodeRetained.TRANSFORMGROUP) {
                    tg = (TransformGroupRetained)nodeR;
                    transformLevels[0] = tg.transformLevels[0];
                    break;
                }
                nodeR = nodeR.parent;
            }
        } else {
            HashKey key;
            int i,j;
            for (i=0; i < transformLevels.length; i++) {
                nodeR = this;
                transformLevels[i] = -1;
                key = new HashKey(localToVworldKeys[i]);

                do {
		    if (nodeR == null)
                        break;
		    else if (nodeR instanceof SharedGroupRetained) {
			// note that key is truncated after getLastNodeId
                        String nodeId = key.getLastNodeId();
						Vector<NodeRetained> parents = ((SharedGroupRetained)nodeR).parents;
                        int sz = parents.size();
                        NodeRetained prevNodeR = nodeR;
                        for (j=0; j< sz; j++) {
							NodeRetained linkR = parents.get(j);
                            if (linkR.nodeId.equals(nodeId)) {
                                nodeR = linkR;
                                break;
                            }
                        }
                        if (prevNodeR == nodeR) {
                            // branch is already detach
                            return;
                        }
                    }
                    else if (nodeR.nodeType == NodeRetained.TRANSFORMGROUP) {
                        tg = (TransformGroupRetained)nodeR;
                        if (tg.inSharedGroup) {

			    j = key.equals(tg.localToVworldKeys, 0,
					   tg.localToVworldKeys.length);

                            transformLevels[i] = tg.transformLevels[j];
                        } else {
                            transformLevels[i] = tg.transformLevels[0];
                        }
                        break;
                    }

                    nodeR = nodeR.parent;
                } while (true);
            }
        }
    }


    @Override
    boolean isStatic() {
	if (source.getCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ) ||
	    source.getCapability(Node.ALLOW_PARENT_READ) ||
	    source.getCapability(Node.ENABLE_PICK_REPORTING) ||
	    source.getCapability(Node.ENABLE_COLLISION_REPORTING) ||
	    source.getCapability(Node.ALLOW_BOUNDS_READ) ||
	    source.getCapability(Node.ALLOW_BOUNDS_WRITE) ||
	    source.getCapability(Node.ALLOW_PICKABLE_READ) ||
	    source.getCapability(Node.ALLOW_PICKABLE_WRITE) ||
	    source.getCapability(Node.ALLOW_COLLIDABLE_READ) ||
	    source.getCapability(Node.ALLOW_COLLIDABLE_WRITE) ||
	    source.getCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ) ||
	    source.getCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE)) {
	    return false;
	}
	return true;
    }

    @Override
    void merge(CompileState compState) {
	staticTransform = compState.staticTransform;
	if (compState.parentGroup != null) {
	    compState.parentGroup.compiledChildrenList.add(this);
	}
	parent = compState.parentGroup;
	if (staticTransform != null) {
	    mergeTransform(staticTransform);
	}
    }

    @Override
    void mergeTransform(TransformGroupRetained xform) {
	if (localBounds != null) {
	    localBounds.transform(xform.transform);
	}
    }
    int[] processViewSpecificInfo(int mode, HashKey k, View v, ArrayList vsgList, int[] keyList,
				 ArrayList leafList) {
	return keyList;

    }

    @Override
    VirtualUniverse getVirtualUniverse() {
	return universe;
    }

    void searchGeometryAtoms(UnorderList list) {}

    /**
     * Make the boundsCache of this node and all its parents dirty
     */
    void dirtyBoundsCache() {
        // Possible optimisation is to not traverse up the tree
        // if the cachedBounds==null. However this is not the case
        // if the node is the child of a SharedGroup
        if (VirtualUniverse.mc.cacheAutoComputedBounds) {
            // Issue 514 : NPE in Wonderland : triggered in cached bounds computation
            validCachedBounds = false;
            if (parent!=null) {
                parent.dirtyBoundsCache();
            }
        }
    }
}

