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
import java.util.Enumeration;
import java.util.Vector;

/**
 * A Locale object defines a high-resolution position within a
 * VirtualUniverse, and serves as a container for a collection of
 * BranchGroup-rooted subgraphs (branch graphs), at that position.
 * Objects within a Locale are defined using standard double-precision
 * coordinates, relative to the origin of the Locale.  This origin
 * defines the Virtual World coordinate system for that Locale.
 * <p>
 * A Locale object defines methods to set and get its high-resolution
 * coordinates, and methods to add, remove, and enumerate the branch
 * graphs.
 *
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a> and
 * <a href="doc-files/VirtualUniverse.html">Scene Graph Superstructure</a>
 * documents.
 *
 * @see VirtualUniverse
 * @see HiResCoord
 * @see BranchGroup
 */

public class Locale extends Object {

    /**
     * The virtual universe that this Locale object is contained within.
     */
    VirtualUniverse universe;

    /**
     * The high resolution coordinate associated with this Locale object.
     */
    HiResCoord 	hiRes;

    /**
     * List of BranchGroup objects included in this Locale
     */
    Vector	branchGroups = new Vector();

    // locale's identifier
    String nodeId = null;

    /**
     * Constructs and initializes a new high resolution Locale object
     * located at (0, 0, 0).
     * @param universe the virtual universe that will contain this
     * Locale object
     */
    public Locale(VirtualUniverse universe) {
	this.universe = universe;
	this.universe.addLocale(this);
	this.hiRes = new HiResCoord();
        nodeId = universe.getNodeId();
    }

    /**
     * Constructs and initializes a new high resolution Locale object
     * from the parameters provided.
     * @param universe the virtual universe that will contain this
     * Locale object
     * @param x an eight element array specifying the x position
     * @param y an eight element array specifying the y position
     * @param z an eight element array specifying the z position
     */
    public Locale(VirtualUniverse universe, int[] x, int[] y, int[] z) {
	this.universe = universe;
	this.universe.addLocale(this);
	this.hiRes = new HiResCoord(x, y, z);
        nodeId = universe.getNodeId();
    }

    /**
     * Constructs and initializes a new high resolution Locale object
     * at the location specified by the HiResCoord argument.
     * @param universe the virtual universe that will contain this
     * Locale object
     * @param hiRes the HiRes coordinate to use in creating this Locale
     */
    public Locale(VirtualUniverse universe, HiResCoord hiRes) {
	this.universe = universe;
	this.universe.addLocale(this);
	this.hiRes = new HiResCoord(hiRes);
        nodeId = universe.getNodeId();
    }

    /**
     * Retrieves the virtual universe within which this Locale object
     * is contained.  A null reference indicates that this
     * Locale has been removed from its VirtualUniverse.
     * @return the virtual universe within which this Locale object
     * is contained.
     */
    public VirtualUniverse getVirtualUniverse() {
	return universe;
    }

    /**
     * Sets the HiRes coordinate of this Locale to the location
     * specified by the parameters provided.
     * @param x an eight element array specifying the x position
     * @param y an eight element array specifying the y position
     * @param z an eight element array specifying the z position
     */
    public void setHiRes(int[] x, int[] y, int[] z) {
	this.hiRes.setHiResCoord(x, y, z);
    }

    /**
     * Sets the HiRes coordinate of this Locale
     * to the location specified by the HiRes argument.
     * @param hiRes the HiRes coordinate specifying this node's new location
     */
    public void setHiRes(HiResCoord hiRes) {
	this.hiRes.setHiResCoord(hiRes);
    }

    /**
     * Returns this node's HiResCoord.
     * @param hiRes a HiResCoord object that will receive the
     * HiRes coordinate of this Locale node
     */
    public void getHiRes(HiResCoord hiRes) {
	this.hiRes.getHiResCoord(hiRes);
    }

    /**
     * Add a new branch graph rooted at BranchGroup to
     * the list of branch graphs.
     * @param branchGroup root of the branch graph to be added
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     * @exception MultipleParentException if the specified BranchGroup node
     * is already live.
     */
    public void addBranchGraph(BranchGroup branchGroup){
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

	// if the BranchGroup already has a parent, or has already been
	// added to a locale, throw MultipleParentException
        if ((((BranchGroupRetained)branchGroup.retained).parent != null) ||
	    (branchGroup.isLive())) {
	    throw new MultipleParentException(J3dI18N.getString("Locale0"));
        }

        universe.notifyStructureChangeListeners(true, this, branchGroup);
	universe.resetWaitMCFlag();
	synchronized (universe.sceneGraphLock) {
	    doAddBranchGraph(branchGroup);
	    universe.setLiveState.reset(this);
	}
	universe.waitForMC();
    }

    // The method that does the work once the lock is acquired.
    void doAddBranchGraph(BranchGroup branchGroup) {
	BranchGroupRetained bgr = (BranchGroupRetained)branchGroup.retained;
	J3dMessage createMessage;
	SetLiveState s = universe.setLiveState;

	// bgr.setLocale(this);

	// addElement needs to precede setLive or else any liveness checks
        // in the initialize() call of a user behavior (ie, calling collision
        // or picking constructor with a SceneGraphPath) will fail
        // when SceneGraphPath.validate() attempts to verify that
        // the proper Locale is associated with that SceneGraphPath
	bgr.attachedToLocale = true;
	branchGroups.addElement(branchGroup);
	s.reset(this);
	s.currentTransforms[0] = new Transform3D[2];
	s.currentTransforms[0][0] = new Transform3D();
	s.currentTransforms[0][1] = new Transform3D();
	s.currentTransformsIndex[0] = new int[2];
	s.currentTransformsIndex[0][0] = 0;
	s.currentTransformsIndex[0][1] = 0;

	s.localToVworld = s.currentTransforms;
	s.localToVworldIndex = s.currentTransformsIndex;

	s.branchGroupPaths = new ArrayList<BranchGroupRetained[]>();
	s.branchGroupPaths.add(new BranchGroupRetained[0]);

        s.orderedPaths = new ArrayList(1);
        s.orderedPaths.add(new OrderedPath());

        s.switchStates = new ArrayList(1);
        s.switchStates.add(new SwitchState(false));

	bgr.setLive(s);

	createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDER| J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.type = J3dMessage.ORDERED_GROUP_INSERTED;
	createMessage.universe = universe;
	createMessage.args[0] = s.ogList.toArray();
	createMessage.args[1] = s.ogChildIdList.toArray();
	createMessage.args[2] = s.ogOrderedIdList.toArray();
	createMessage.args[3] = s.ogCIOList.toArray();
	createMessage.args[4] = s.ogCIOTableList.toArray();

	VirtualUniverse.mc.processMessage(createMessage);

	createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.type = J3dMessage.VIEWSPECIFICGROUP_INIT;
	createMessage.universe = universe;
	createMessage.args[0] = s.changedViewGroup;
	createMessage.args[1] = s.changedViewList;
	createMessage.args[2] = s.keyList;
	VirtualUniverse.mc.processMessage(createMessage);


	createMessage = new J3dMessage();
	createMessage.threads = s.notifyThreads;
        createMessage.type = J3dMessage.INSERT_NODES;
        createMessage.universe = universe;
        createMessage.args[0] = s.nodeList.toArray();
	createMessage.args[1] = null;
	createMessage.args[2] = null;
	if (s.viewScopedNodeList != null) {
	    createMessage.args[3] = s.viewScopedNodeList;
	    createMessage.args[4] = s.scopedNodesViewList;
	}
	VirtualUniverse.mc.processMessage(createMessage);

	int sz = s.behaviorNodes.size();
	for (int i=0; i< sz; i++) {
	    BehaviorRetained b;
	    b = (BehaviorRetained)s.behaviorNodes.get(i);
	    b.executeInitialize();
	}

	createMessage = new J3dMessage();
        createMessage.threads = J3dThread.UPDATE_BEHAVIOR;
        createMessage.type = J3dMessage.BEHAVIOR_ACTIVATE;
        createMessage.universe = universe;
        VirtualUniverse.mc.processMessage(createMessage);

	// Free up memory.
	s.reset(null);
    }

    /**
     * Removes a branch graph rooted at BranchGroup from
     * the list of branch graphs.
     * @param branchGroup root of the branch graph to be removed
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     * @exception CapabilityNotSetException if the ALLOW_DETACH capability is
     * not set in the specified BranchGroup node.
     */
    public void removeBranchGraph(BranchGroup branchGroup){
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

	if (! branchGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
	    throw new CapabilityNotSetException(J3dI18N.getString("Locale1"));
	}
	universe.resetWaitMCFlag();
	synchronized (universe.sceneGraphLock) {
	    doRemoveBranchGraph(branchGroup, null, 0);
	    universe.setLiveState.reset(this);
	}
	universe.waitForMC();
    }


    // Method to remove all branch graphs from this Locale and remove
    // this Locale from the VirtualUniverse
    void removeFromUniverse() {
	if (branchGroups.size() > 0) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
		// Make a copy of the branchGroups list so that we can safely
		// iterate over it.
		Object[] bg = branchGroups.toArray();
		for (int i = 0; i < bg.length; i++) {
		    doRemoveBranchGraph((BranchGroup)bg[i], null, 0);
		}
	    }
	    // Put after sceneGraphLock to prevent deadlock
	    universe.waitForMC();
	}

	// free nodeId
	if (nodeId != null) {
	    universe.nodeIdFreeList.addElement(nodeId);
	    nodeId = null;
	}

	// Set universe pointer to null, indicating that this Locale
	// has been removed from its universe
	universe = null;
    }


    // The method that does the work once the lock is acquired.
    void doRemoveBranchGraph(BranchGroup branchGroup,
                                J3dMessage messages[], int startIndex) {

	BranchGroupRetained bgr = (BranchGroupRetained)branchGroup.retained;
	J3dMessage destroyMessage;

	if (!branchGroup.isLive())
	    return;
	bgr.attachedToLocale = false;
	branchGroups.removeElement(branchGroup);
	universe.setLiveState.reset(this);
	bgr.clearLive(universe.setLiveState);
	bgr.setParent(null);
	bgr.setLocale(null);

	if (messages == null) {
	    destroyMessage = new J3dMessage();
	} else {
	    destroyMessage = messages[startIndex++];
	}
	destroyMessage.threads = J3dThread.UPDATE_RENDER| J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	destroyMessage.type = J3dMessage.ORDERED_GROUP_REMOVED;
	destroyMessage.universe = universe;
	destroyMessage.args[0] = universe.setLiveState.ogList.toArray();
	destroyMessage.args[1] = universe.setLiveState.ogChildIdList.toArray();
	destroyMessage.args[3] = universe.setLiveState.ogCIOList.toArray();
	destroyMessage.args[4] = universe.setLiveState.ogCIOTableList.toArray();

        // Issue 312: We need to send the REMOVE_NODES message to the
        // RenderingEnvironmentStructure before we send VIEWSPECIFICGROUP_CLEAR,
        // since the latter clears the list of views that is referred to by
        // scopedNodesViewList and used by removeNodes.
	if (messages == null) {
            VirtualUniverse.mc.processMessage(destroyMessage);
            destroyMessage = new J3dMessage();
        } else {
            destroyMessage = messages[startIndex++];
        }
        destroyMessage.threads = universe.setLiveState.notifyThreads;
        destroyMessage.type = J3dMessage.REMOVE_NODES;
        destroyMessage.universe = universe;
        destroyMessage.args[0] = universe.setLiveState.nodeList.toArray();
	if (universe.setLiveState.viewScopedNodeList != null) {
	    destroyMessage.args[3] = universe.setLiveState.viewScopedNodeList;
	    destroyMessage.args[4] = universe.setLiveState.scopedNodesViewList;
	}

	if (messages == null) {
            VirtualUniverse.mc.processMessage(destroyMessage);
            destroyMessage = new J3dMessage();
        } else {
            destroyMessage = messages[startIndex++];
        }
	destroyMessage.threads =  J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	destroyMessage.type = J3dMessage.VIEWSPECIFICGROUP_CLEAR;
	destroyMessage.universe = universe;
	destroyMessage.args[0] = universe.setLiveState.changedViewGroup;
	destroyMessage.args[1] = universe.setLiveState.keyList;

	if (messages == null) {
            VirtualUniverse.mc.processMessage(destroyMessage);
        } else {
            destroyMessage = messages[startIndex++];
	}

	if (universe.isEmpty()) {
	    VirtualUniverse.mc.postRequest(MasterControl.EMPTY_UNIVERSE,
					   universe);
	}
	universe.setLiveState.reset(null); // cleanup memory
        universe.notifyStructureChangeListeners(false, this, branchGroup);
    }

    /**
     * Replaces the branch graph rooted at oldGroup in the list of
     * branch graphs with the branch graph rooted at
     * newGroup.
     * @param oldGroup root of the branch graph to be replaced.
     * @param newGroup root of the branch graph that will replace the old
     * branch graph.
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     * @exception CapabilityNotSetException if the ALLOW_DETACH capability is
     * not set in the old BranchGroup node.
     * @exception MultipleParentException if the new BranchGroup node
     * is already live.
     */
    public void replaceBranchGraph(BranchGroup oldGroup,
			    BranchGroup newGroup){

	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

	if (! oldGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
	    throw new CapabilityNotSetException(J3dI18N.getString("Locale1"));
	}

        if (((BranchGroupRetained)newGroup.retained).parent != null) {
	    throw new MultipleParentException(J3dI18N.getString("Locale3"));
        }
	universe.resetWaitMCFlag();
        universe.notifyStructureChangeListeners(true, this, newGroup);
	synchronized (universe.sceneGraphLock) {
	    doReplaceBranchGraph(oldGroup, newGroup);
	    universe.setLiveState.reset(this);
	}
        universe.notifyStructureChangeListeners(false, this, oldGroup);
	universe.waitForMC();
    }

    // The method that does the work once the lock is acquired.
    void doReplaceBranchGraph(BranchGroup oldGroup,
			    BranchGroup newGroup){
	BranchGroupRetained obgr = (BranchGroupRetained)oldGroup.retained;
	BranchGroupRetained nbgr = (BranchGroupRetained)newGroup.retained;
	J3dMessage createMessage;
	J3dMessage destroyMessage;


	branchGroups.removeElement(oldGroup);
	obgr.attachedToLocale = false;
	universe.setLiveState.reset(this);
	obgr.clearLive(universe.setLiveState);

	destroyMessage = new J3dMessage();

	destroyMessage.threads = J3dThread.UPDATE_RENDER| J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	destroyMessage.type = J3dMessage.ORDERED_GROUP_REMOVED;
	destroyMessage.universe = universe;
	destroyMessage.args[0] = universe.setLiveState.ogList.toArray();
	destroyMessage.args[1] = universe.setLiveState.ogChildIdList.toArray();
	destroyMessage.args[3] = universe.setLiveState.ogCIOList.toArray();
	destroyMessage.args[4] = universe.setLiveState.ogCIOTableList.toArray();
	VirtualUniverse.mc.processMessage(destroyMessage);

	destroyMessage = new J3dMessage();
	destroyMessage.threads =  J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	destroyMessage.type = J3dMessage.VIEWSPECIFICGROUP_CLEAR;
	destroyMessage.universe = universe;
	destroyMessage.args[0] = universe.setLiveState.changedViewGroup;
	destroyMessage.args[1] = universe.setLiveState.keyList;
	VirtualUniverse.mc.processMessage(destroyMessage);


	destroyMessage = new J3dMessage();
        destroyMessage.threads = universe.setLiveState.notifyThreads;
        destroyMessage.type = J3dMessage.REMOVE_NODES;
        destroyMessage.universe = universe;
        destroyMessage.args[0] = universe.setLiveState.nodeList.toArray();
        VirtualUniverse.mc.processMessage(destroyMessage);

	branchGroups.addElement(newGroup);
	nbgr.attachedToLocale = true;
	universe.setLiveState.reset(this);
	universe.setLiveState.currentTransforms[0] = new Transform3D[2];
	universe.setLiveState.currentTransforms[0][0] = new Transform3D();
	universe.setLiveState.currentTransforms[0][1] = new Transform3D();
	universe.setLiveState.currentTransformsIndex[0] = new int[2];
	universe.setLiveState.currentTransformsIndex[0][0] = 0;
	universe.setLiveState.currentTransformsIndex[0][1] = 0;

	universe.setLiveState.localToVworld =
	    universe.setLiveState.currentTransforms;
	universe.setLiveState.localToVworldIndex =
	    universe.setLiveState.currentTransformsIndex;

	universe.setLiveState.branchGroupPaths = new ArrayList<BranchGroupRetained[]>();
	universe.setLiveState.branchGroupPaths.add(new BranchGroupRetained[0]);

        universe.setLiveState.orderedPaths = new ArrayList(1);
        universe.setLiveState.orderedPaths.add(new OrderedPath());

        universe.setLiveState.switchStates = new ArrayList(1);
        universe.setLiveState.switchStates.add(new SwitchState(false));

	nbgr.setLive(universe.setLiveState);


	createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDER| J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.type = J3dMessage.ORDERED_GROUP_INSERTED;
	createMessage.universe = universe;
	createMessage.args[0] = universe.setLiveState.ogList.toArray();
	createMessage.args[1] = universe.setLiveState.ogChildIdList.toArray();
	createMessage.args[2] = universe.setLiveState.ogOrderedIdList.toArray();
	createMessage.args[3] = universe.setLiveState.ogCIOList.toArray();
	createMessage.args[4] = universe.setLiveState.ogCIOTableList.toArray();
	VirtualUniverse.mc.processMessage(createMessage);

	// XXXX: make these two into one message
	createMessage = new J3dMessage();
        createMessage.threads = universe.setLiveState.notifyThreads;
        createMessage.type = J3dMessage.INSERT_NODES;
        createMessage.universe = universe;
        createMessage.args[0] = universe.setLiveState.nodeList.toArray();
	createMessage.args[1] = null;
	createMessage.args[2] = null;
	if (universe.setLiveState.viewScopedNodeList != null) {
	    createMessage.args[3] = universe.setLiveState.viewScopedNodeList;
	    createMessage.args[4] = universe.setLiveState.scopedNodesViewList;
	}
        VirtualUniverse.mc.processMessage(createMessage);

	Object behaviorNodes[] = universe.setLiveState.behaviorNodes.toArray();

	if (universe.isEmpty()) {
	    VirtualUniverse.mc.postRequest(MasterControl.EMPTY_UNIVERSE,
					   universe);
	}

        for (int i=0; i< behaviorNodes.length; i++) {
            ((BehaviorRetained) behaviorNodes[i]).executeInitialize();
        }

	createMessage = new J3dMessage();
        createMessage.threads = J3dThread.UPDATE_BEHAVIOR;
        createMessage.type = J3dMessage.BEHAVIOR_ACTIVATE;
        createMessage.universe = universe;
        VirtualUniverse.mc.processMessage(createMessage);

	// Free up memory.
	universe.setLiveState.reset(null);
    }

    /**
     * Get number of branch graphs in this Locale.
     * @return number of branch graphs in this Locale.
     */
    public int numBranchGraphs(){
	return  branchGroups.size();
    }

    /**
     * Gets an Enumeration object of all branch graphs in this Locale.
     * @return an Enumeration object of all branch graphs.
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     */
    public Enumeration getAllBranchGraphs(){
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

	return branchGroups.elements();
    }


    void validateModeFlagAndPickShape(int mode, int flags, PickShape pickShape) {

        if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

        if((mode != PickInfo.PICK_BOUNDS) && (mode != PickInfo.PICK_GEOMETRY)) {

          throw new IllegalArgumentException(J3dI18N.getString("Locale5"));
        }

        if((pickShape instanceof PickPoint) && (mode == PickInfo.PICK_GEOMETRY)) {
          throw new IllegalArgumentException(J3dI18N.getString("Locale6"));
        }

        if(((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) &&
                ((flags & PickInfo.ALL_GEOM_INFO) != 0)) {
            throw new IllegalArgumentException(J3dI18N.getString("Locale7"));
        }

        if((mode == PickInfo.PICK_BOUNDS) &&
                (((flags & (PickInfo.CLOSEST_GEOM_INFO |
                            PickInfo.ALL_GEOM_INFO |
                            PickInfo.CLOSEST_DISTANCE |
                            PickInfo.CLOSEST_INTERSECTION_POINT)) != 0))) {

          throw new IllegalArgumentException(J3dI18N.getString("Locale8"));
        }

        if((pickShape instanceof PickBounds) &&
                (((flags & (PickInfo.CLOSEST_GEOM_INFO |
                            PickInfo.ALL_GEOM_INFO |
                            PickInfo.CLOSEST_DISTANCE |
                            PickInfo.CLOSEST_INTERSECTION_POINT)) != 0))) {

          throw new IllegalArgumentException(J3dI18N.getString("Locale9"));
        }
    }

    /**
     * Returns an array referencing all the items that are pickable below this
     * <code>Locale</code> that intersect with PickShape.
     * The resultant array is unordered.
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @see BranchGroup#pickAll
     */
    public SceneGraphPath[] pickAll( PickShape pickShape ) {
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

        PickInfo[] pickInfoArr = pickAll( PickInfo.PICK_BOUNDS,
                PickInfo.SCENEGRAPHPATH, pickShape);

       if(pickInfoArr == null) {
            return null;
       }
        SceneGraphPath[] sgpArr = new SceneGraphPath[pickInfoArr.length];
        for( int i=0; i<sgpArr.length; i++) {
            sgpArr[i] = pickInfoArr[i].getSceneGraphPath();
        }

        return sgpArr;

    }


    /**
     * Returns an array unsorted references to all the PickInfo objects that are pickable
     * below this <code>Locale</code> that intersect with PickShape.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see BranchGroup#pickAll(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo[] pickAll( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);

	GeometryAtom geomAtoms[] = universe.geometryStructure.pickAll(this, pickShape);

        return PickInfo.pick(this, geomAtoms, mode, flags, pickShape, PickInfo.PICK_ALL);

    }

    /**
     * Returns a sorted array of references to all the pickable items
     * that intersect with the pickShape. Element [0] references the
     * item closest to <i>origin</i> of PickShape successive array
     * elements are further from the <i>origin</i>
     * <br>
     * NOTE: If pickShape is of type PickBounds, the resulting array
     * is unordered.
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @see BranchGroup#pickAllSorted
     */
    public SceneGraphPath[] pickAllSorted( PickShape pickShape ) {
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

        PickInfo[] pickInfoArr = pickAllSorted( PickInfo.PICK_BOUNDS,
                PickInfo.SCENEGRAPHPATH, pickShape);

       if(pickInfoArr == null) {
            return null;
       }
        SceneGraphPath[] sgpArr = new SceneGraphPath[pickInfoArr.length];
        for( int i=0; i<sgpArr.length; i++) {
            sgpArr[i] = pickInfoArr[i].getSceneGraphPath();
        }

        return sgpArr;

    }

    /**
     * Returns a sorted array of PickInfo references to all the pickable
     * items that intersect with the pickShape. Element [0] references
     * the item closest to <i>origin</i> of PickShape successive array
     * elements are further from the <i>origin</i>
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see BranchGroup#pickAllSorted(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo[] pickAllSorted( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
        GeometryAtom geomAtoms[] = universe.geometryStructure.pickAll(this, pickShape);

        if ((geomAtoms == null) || (geomAtoms.length == 0)) {
            return null;
        }

        PickInfo[] pickInfoArr  = null;

	if (mode == PickInfo.PICK_GEOMETRY) {
            // Need to have closestDistance set
            flags |= PickInfo.CLOSEST_DISTANCE;
            pickInfoArr= PickInfo.pick(this, geomAtoms, mode, flags, pickShape, PickInfo.PICK_ALL);
	    if (pickInfoArr != null) {
		PickInfo.sortPickInfoArray(pickInfoArr);
	    }
        }
        else {
            PickInfo.sortGeomAtoms(geomAtoms, pickShape);
            pickInfoArr= PickInfo.pick(this, geomAtoms, mode, flags, pickShape, PickInfo.PICK_ALL);
        }

        return pickInfoArr;
    }

    /**
     * Returns a SceneGraphPath which references the pickable item
     * which is closest to the origin of <code>pickShape</code>.
     * <br>
     * NOTE: If pickShape is of type PickBounds, the return is any
     * pickable node below this Locale.
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @see BranchGroup#pickClosest
     */
    public SceneGraphPath pickClosest( PickShape pickShape ) {
        if (universe == null) {
            throw new IllegalStateException(J3dI18N.getString("Locale4"));
        }

        PickInfo pickInfo = pickClosest( PickInfo.PICK_BOUNDS,
                PickInfo.SCENEGRAPHPATH, pickShape);

        if(pickInfo == null) {
            return null;
        }
        return pickInfo.getSceneGraphPath();
    }

    /**
     * Returns a PickInfo which references the pickable item
     * which is closest to the origin of <code>pickShape</code>.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see BranchGroup#pickClosest(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo pickClosest( int mode, int flags, PickShape pickShape ) {

        PickInfo[] pickInfoArr = null;

        pickInfoArr = pickAllSorted( mode, flags, pickShape );

        if(pickInfoArr == null) {
            return null;
        }

        return pickInfoArr[0];

    }

    /**
     * Returns a reference to any item that is Pickable below this
     * Locale which intersects with <code>pickShape</code>.
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @see BranchGroup#pickAny
     */
    public SceneGraphPath pickAny( PickShape pickShape ) {
	if (universe == null) {
	    throw new IllegalStateException(J3dI18N.getString("Locale4"));
	}

        PickInfo pickInfo = pickAny( PickInfo.PICK_BOUNDS,
                PickInfo.SCENEGRAPHPATH, pickShape);

        if(pickInfo == null) {
            return null;
        }
        return pickInfo.getSceneGraphPath();

    }

    /**
     * Returns a PickInfo which references the pickable item  below this
     * Locale which intersects with <code>pickShape</code>.
     * The accuracy of the pick is set by the pick mode. The mode include :
     * PickInfo.PICK_BOUNDS and PickInfo.PICK_GEOMETRY. The amount of information returned
     * is specified via a masked variable, flags, indicating which components are
     * present in each returned PickInfo object.
     *
     * @param mode  picking mode, one of <code>PickInfo.PICK_BOUNDS</code> or <code>PickInfo.PICK_GEOMETRY</code>.
     *
     * @param flags a mask indicating which components are present in each PickInfo object.
     * This is specified as one or more individual bits that are bitwise "OR"ed together to
     * describe the PickInfo data. The flags include :
     * <ul>
     * <code>PickInfo.SCENEGRAPHPATH</code> - request for computed SceneGraphPath.<br>
     * <code>PickInfo.NODE</code> - request for computed intersected Node.<br>
     * <code>PickInfo.LOCAL_TO_VWORLD</code> - request for computed local to virtual world transform.<br>
     * <code>PickInfo.CLOSEST_INTERSECTION_POINT</code> - request for closest intersection point.<br>
     * <code>PickInfo.CLOSEST_DISTANCE</code> - request for the distance of closest intersection.<br>
     * <code>PickInfo.CLOSEST_GEOM_INFO</code> - request for only the closest intersection geometry information.<br>
     * <code>PickInfo.ALL_GEOM_INFO</code> - request for all intersection geometry information.<br>
     * </ul>
     *
     * @param pickShape the description of this picking volume or area.
     *
     * @exception IllegalArgumentException if flags contains both CLOSEST_GEOM_INFO and
     * ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is a PickPoint and pick mode
     * is set to PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is neither PICK_BOUNDS
     * nor PICK_GEOMETRY.
     *
     * @exception IllegalArgumentException if pick mode is PICK_BOUNDS
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalArgumentException if pickShape is PickBounds
     * and flags includes any of CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE,
     * CLOSEST_GEOM_INFO or ALL_GEOM_INFO.
     *
     * @exception IllegalStateException if this Locale has been
     * removed from its VirtualUniverse.
     *
     * @exception CapabilityNotSetException if the mode is
     * PICK_GEOMETRY and the Geometry.ALLOW_INTERSECT capability bit
     * is not set in any Geometry objects referred to by any shape
     * node whose bounds intersects the PickShape.
     *
     * @exception CapabilityNotSetException if flags contains any of
     * CLOSEST_INTERSECTION_POINT, CLOSEST_DISTANCE, CLOSEST_GEOM_INFO
     * or ALL_GEOM_INFO, and the capability bits that control reading of
     * coordinate data are not set in any GeometryArray object referred
     * to by any shape node that intersects the PickShape.
     * The capability bits that must be set to avoid this exception are as follows :
     * <ul>
     * <li>By-copy geometry : GeometryArray.ALLOW_COORDINATE_READ</li>
     * <li>By-reference geometry : GeometryArray.ALLOW_REF_DATA_READ</li>
     * <li>Indexed geometry : IndexedGeometryArray.ALLOW_COORDINATE_INDEX_READ
     * (in addition to one of the above)</li>
     * </ul>
     *
     * @see BranchGroup#pickAny(int,int,javax.media.j3d.PickShape)
     * @see PickInfo
     *
     * @since Java 3D 1.4
     *
     */
    public PickInfo pickAny( int mode, int flags, PickShape pickShape ) {

        validateModeFlagAndPickShape(mode, flags, pickShape);
	GeometryAtom geomAtoms[] = universe.geometryStructure.pickAll(this, pickShape);

        PickInfo[] pickInfoArr = PickInfo.pick(this, geomAtoms, mode, flags, pickShape, PickInfo.PICK_ANY);

        if(pickInfoArr == null) {
            return null;
        }

        return pickInfoArr[0];

    }

}
