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

package javax.media.j3d;

import java.util.ArrayList;

/**
 * SetLiveState is used to encapsulate all state needed when a branch
 * group is added to the scene graph
 */

class SetLiveState extends Object {
    // The VirtualUniverse for this branch group
    VirtualUniverse universe = null;

    // The Locale for this Branch Graph
    Locale locale = null;

    // The transforms used to update state
    Transform3D[][] currentTransforms = new Transform3D[1][];
    int[][]	    currentTransformsIndex = new int[1][];

    // The keys used when dealing with SharedGroups
    HashKey[] keys = null;

    // flags for detecting what we are under
    boolean inSharedGroup = false;
    boolean inBackgroundGroup = false;
    boolean inViewSpecificGroup = false;

    /**
     * The list of nodes added/removed during setLive/clearLive
     */
    ArrayList nodeList = new ArrayList();

/**
 * List of nodes that are viewScoped.  Note that all nodes
 * except Shape3D nodes can be in viewScopedNodeList, shape3D
 * nodes will always be in the nodeList regardless of scoped
 * or not. Also, only renderbin and renderingEnv structure is
 * interested in viewScopedNodeList
 */
ArrayList<NodeRetained> viewScopedNodeList = null;

/**
 * Parallel list to viewScopedNodeList containing a list of views
 * that the viewScopedNode is scoped to
 */
ArrayList<ArrayList<View>> scopedNodesViewList = null;

    // Threads to notify after setLive/clearLive
    int notifyThreads = 0;

    // The current list of leaf nodes for transform targets
    Targets[] transformTargets = null;

    // List of transform level, one per shared path
    int transformLevels[] = new int[]{-1};

// List of scoped lights
ArrayList<ArrayList<LightRetained>> lights = null;

// List of scoped fogs
ArrayList<ArrayList<FogRetained>> fogs = null;

// List of scoped modelClips
ArrayList<ArrayList<ModelClipRetained>> modelClips = null;

// List of scoped alt app
ArrayList<ArrayList<AlternateAppearanceRetained>> altAppearances = null;

// List of viewes scoped to this Group, for all subclasses
// of group, except ViewSpecificGroup its a pointer to closest
// ViewSpecificGroup parent
// viewList for this node, if inSharedGroup is
// false then only viewList(0) is valid
ArrayList<ArrayList<View>> viewLists = null;
ArrayList<ViewSpecificGroupRetained> changedViewGroup = null;
ArrayList<ArrayList<View>> changedViewList = null;
    int[] keyList = null;


    // The current bitmask of types in transformTragets
    //int transformTargetThreads = 0;

ArrayList<OrderedPath> orderedPaths = null;

ArrayList<OrderedGroupRetained> ogList = new ArrayList<OrderedGroupRetained>(5);
ArrayList<Integer> ogChildIdList = new ArrayList<Integer>(5);
ArrayList<Integer> ogOrderedIdList = new ArrayList<Integer>(5);
// ogCIOList contains a list of OG with affected child index order.
ArrayList<OrderedGroupRetained> ogCIOList = new ArrayList<OrderedGroupRetained>(5);
// ogCIOTableList contains a list of affected child index order.
ArrayList<int[]> ogCIOTableList = new ArrayList<int[]>(5);

/**
 * List of BranchGroup from this node to the root of tree This is used by
 * BranchGroupRetained to construct BranchGroup lists for picking.
 *
 * @see NodeRetained.branchGroupPaths
 */
ArrayList<BranchGroupRetained[]> branchGroupPaths = null;
ArrayList<BranchGroupRetained[]> parentBranchGroupPaths = null;

    /**
     * List of Pickable flags, one for each share path.
     * This flag is true when all the NodeRetained.pickable is true
     * along the path except current node.
     */
    boolean pickable[] = new boolean[]{true};

    /**
     * List of collidable flags, one for each share path.
     * This flag is true when all the NodeRetained.pickable is true
     * along the path except current node.
     */
    boolean collidable[] = new boolean[]{true};

    // reference count use in set/clear Live to remember how
    // many references of the original branch that attach()/detach()
    int refCount = 1;

    // background node whose geometry branch contains this node
    BackgroundRetained geometryBackground = null;

    // behavior nodes
    ArrayList behaviorNodes = new ArrayList(1);

// The current list of child transform group nodes or link nodes
// under a transform group
ArrayList<NodeRetained> childTransformLinks = null;

    // closest parent which is a TransformGroupRetained or sharedGroupRetained
    GroupRetained parentTransformLink = null;

    // switch Level, start from -1, increment by one for each SwitchNode
    // encounter in a branch, one per key
    int switchLevels[] = new int[]{-1};

    // closest switch parent, one per key
    SwitchRetained closestSwitchParents[] = new SwitchRetained[]{null};

    // the child id from the closest switch parent, one per key
    int closestSwitchIndices[] = new int[]{-1};

    // The current list of leaf nodes for switch targets
    Targets[] switchTargets = null;

// The current list of closest child switch nodes or
// link nodes under a switch node
ArrayList<NodeRetained> childSwitchLinks = null;

    // closest parent which is a SwitchRetained or sharedGroupRetained
    GroupRetained parentSwitchLink = null;

    SharedGroupRetained lastSharedGroup = null;

    int traverseFlags = 0;

    // Use for set live.
    Transform3D[][] localToVworld = null;
    int[][] localToVworldIndex = null;
    HashKey[] localToVworldKeys = null;

    // cached hashkey index to eliminate duplicate hash key index search
    // currently used by Switch, can be extended for other node types
    int[] hashkeyIndex = null;

ArrayList<SwitchState> switchStates = null;

    SetLiveState(VirtualUniverse u) {
	universe = u;
    }


    void reset(Locale l) {
	locale = l;
	clear();
    }

    void clear() {
	inSharedGroup = false;
	inBackgroundGroup = false;
	inViewSpecificGroup = false;
	nodeList.clear();
	viewScopedNodeList = null;
	scopedNodesViewList = null;

	notifyThreads = 0;
	transformTargets = null;
	lights = null;
	fogs = null;
	modelClips = null;
	altAppearances = null;
	viewLists = null;
	changedViewGroup = null;
	changedViewList = null;
	keyList = null;

        behaviorNodes.clear();
	traverseFlags = 0;

	ogList.clear();
	ogChildIdList.clear();
	ogOrderedIdList.clear();
	ogCIOList.clear();
	ogCIOTableList.clear();

	pickable = new boolean[]{true};
	collidable = new boolean[]{true};
	refCount = 1;
        geometryBackground = null;
        transformLevels = new int[]{-1};
        childTransformLinks = null;
        parentTransformLink = null;

        switchTargets = null;
        switchLevels = new int[]{-1};
	switchStates = null;
        closestSwitchIndices = new int[]{-1};
        closestSwitchParents = new SwitchRetained[]{null};
        childSwitchLinks = null;
        parentSwitchLink = null;

	lastSharedGroup = null;

	keys = null;
	currentTransforms = new Transform3D[1][];
	currentTransformsIndex = new int[1][];

	localToVworld = null;
	localToVworldIndex = null;
	localToVworldKeys = null;

        // XXXX: optimization for targetThreads computation, require
        // cleanup in GroupRetained.doSetLive()
	//transformTargetThreads = 0;

        hashkeyIndex = null;

        // Fix for issue 75
        parentBranchGroupPaths = null;
        branchGroupPaths = null;
        orderedPaths = null;
    }
}
