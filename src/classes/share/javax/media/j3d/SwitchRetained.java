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
import java.util.BitSet;

/**
 *  The switch node controls which one of its children will be rendered.
 */

class SwitchRetained extends GroupRetained implements TargetsInterface
{
    static final int GEO_NODES		= 0x0001;
    static final int ENV_NODES		= 0x0002;
    static final int BEHAVIOR_NODES	= 0x0004;
    static final int SOUND_NODES	= 0x0008;
    static final int BOUNDINGLEAF_NODES	= 0x0010;

    /**
     * The value specifing which child to render.
     */
    int whichChild = Switch.CHILD_NONE;

    /**
     * The BitSet specifying which children are to be selected for
     * rendering. This is used ONLY if whichChild is set to CHILD_MASK.
     */
    BitSet	childMask = new BitSet();

    /**
     * The childmask bitset used for rendering
     */
    BitSet 	renderChildMask = new BitSet();

    // A boolean indication that something changed
    boolean isDirty = true;

// switchLevel per key, used in traversing switch children
ArrayList<Integer> switchLevels = new ArrayList<Integer>(1);

    // key which identifies a unique path from a locale to this switch link
    HashKey switchKey = new HashKey();

    // switch index counter to identify specific children
    int switchIndexCount = 0;

    // for message processing
    UpdateTargets updateTargets = null;

ArrayList<ArrayList<SwitchState>> childrenSwitchStates = null;

    SwitchRetained() {
        this.nodeType = NodeRetained.SWITCH;
    }

    /**
     * Sets which child should be drawn.
     * @param whichChild the child to choose during a render operation
     */
    // synchronized with clearLive
    synchronized void setWhichChild(int whichChild, boolean updateAlways) {

        int i, nchildren;

        this.whichChild = whichChild;
	isDirty = true;

        if (source != null && source.isLive()) {
            updateTargets = new UpdateTargets();
            ArrayList<SwitchState> updateList = new ArrayList<SwitchState>(1);
            nchildren = children.size();
            switch (whichChild) {
            case Switch.CHILD_ALL:
                for (i=0; i<nchildren; i++) {
                    if (renderChildMask.get(i) == false || updateAlways) {
                        renderChildMask.set(i);
                        updateSwitchChild(i, true, updateList);
                    }
                }
                break;
            case Switch.CHILD_NONE:
                for (i=0; i<nchildren; i++) {
                    if (renderChildMask.get(i) == true || updateAlways) {
                        renderChildMask.clear(i);
                        updateSwitchChild(i, false, updateList);
                    }
                }
                break;
            case Switch.CHILD_MASK:
                for (i=0; i<nchildren; i++) {
                    if (childMask.get(i) == true) {
                        if (renderChildMask.get(i) == false || updateAlways) {
                            renderChildMask.set(i);
                            updateSwitchChild(i, true, updateList);
                        }
                    } else {
                        if (renderChildMask.get(i) == true || updateAlways) {
                            renderChildMask.clear(i);
                            updateSwitchChild(i, false, updateList);
                        }
                    }
                }
                break;
            default:
                for (i=0; i<nchildren; i++) {
                    if (i == whichChild) {
                        if (renderChildMask.get(i) == false || updateAlways) {
                            renderChildMask.set(i);
                            updateSwitchChild(i, true, updateList);
                        }
                    } else {
                        if (renderChildMask.get(i) == true || updateAlways) {
                            renderChildMask.clear(i);
                            updateSwitchChild(i, false, updateList);
                        }
                    }
                }
                break;
            }
	    sendMessage(updateList);
        }
        dirtyBoundsCache();
    }

    /**
     * Returns the index of the current child.
     * @return the default child's index
     */
    int getWhichChild() {
        return this.whichChild;
    }

    /**
     * Sets current childMask.
     * @param childMask a BitSet to select the children for rendering
     */
    // synchronized with clearLive
    synchronized final void setChildMask(BitSet childMask) {
	int i, nbits, nchildren;

	if (childMask.size() > this.childMask.size()) {
	    nbits = childMask.size();
	} else {
	    nbits = this.childMask.size();
	}

	for (i=0; i<nbits; i++) {
	    if (childMask.get(i)) {
		this.childMask.set(i);
	    } else {
		this.childMask.clear(i);
	    }
	}
	this.isDirty = true;
        if (source != null && source.isLive() &&
                whichChild == Switch.CHILD_MASK) {
            updateTargets = new UpdateTargets();
            ArrayList<SwitchState> updateList = new ArrayList<SwitchState>(1);
            nchildren = children.size();
            for (i=0; i<nchildren; i++) {
                if (childMask.get(i) == true) {
                    if (renderChildMask.get(i) == false) {
                        renderChildMask.set(i);
                        updateSwitchChild(i, true, updateList);
                    }
                } else {
                    if (renderChildMask.get(i) == true) {
                        renderChildMask.clear(i);
                        updateSwitchChild(i, false, updateList);
                    }
                }
            }
	    sendMessage(updateList);
        }
        dirtyBoundsCache();
    }

void sendMessage(ArrayList<SwitchState> updateList) {

        J3dMessage m ;
        int i,j,size,threads;
        Object[] nodesArr, nodes;

        threads = updateTargets.computeSwitchThreads();

        if (threads > 0) {

            m = new J3dMessage();
            m.type = J3dMessage.SWITCH_CHANGED;
            m.universe = universe;
            m.threads = threads;
            m.args[0] = updateTargets;
            m.args[2] = updateList;
            UnorderList blnList =
                        updateTargets.targetList[Targets.BLN_TARGETS];

            if (blnList != null) {
                BoundingLeafRetained mbleaf;
                size = blnList.size();

                Object[] boundingLeafUsersArr = new Object[size];
                nodesArr = blnList.toArray(false);
                for (j=0; j<size; j++) {
                    nodes = (Object[])nodesArr[j];
                    Object[] boundingLeafUsers = new Object[nodes.length];
                    boundingLeafUsersArr[j] = boundingLeafUsers;
                    for (i=0; i<nodes.length; i++) {
                        mbleaf = (BoundingLeafRetained) nodes[i];
                        boundingLeafUsers[i] = mbleaf.users.toArray();
                    }
                }
                m.args[1] = boundingLeafUsersArr;
            }
            VirtualUniverse.mc.processMessage(m);
        }

        UnorderList vpList = updateTargets.targetList[Targets.VPF_TARGETS];
        if (vpList != null) {
            ViewPlatformRetained vp;
            size = vpList.size();

            nodesArr = vpList.toArray(false);
            for (j=0; j<size; j++) {
                nodes = (Object[])nodesArr[j];
                for (i=0; i<nodes.length; i++) {
                    vp = (ViewPlatformRetained)nodes[i];
                    vp.processSwitchChanged();
                }
            }
        }
    }

    /**
     * Returns the current childMask.
     * @return the current childMask
     */
    final BitSet getChildMask() {
        return (BitSet)this.childMask.clone();
    }

    /**
     * Returns the current child.
     * @return the current child
     */
    Node currentChild() {
	if ((whichChild < 0) || (whichChild >= children.size()))
	    return null;
	else
	    return getChild(whichChild);
    }

void updateSwitchChild(int child, boolean switchOn, ArrayList<SwitchState> updateList) {
        int i;
        int switchLevel;

        if (inSharedGroup) {
            for (i=0; i<localToVworldKeys.length; i++) {
                switchLevel = switchLevels.get(i).intValue();
                traverseSwitchChild(child, localToVworldKeys[i], i, this,
                                                false, false, switchOn, switchLevel, updateList);
            }
        } else {
            switchLevel = switchLevels.get(0).intValue();
            traverseSwitchChild(child, null, 0, this, false, false,
                                                switchOn, switchLevel, updateList);
        }
    }

    // Switch specific data at SetLive.
    void setAuxData(SetLiveState s, int index, int hkIndex) {
	int size;
	ArrayList<SwitchState> switchStates;

	// Group's setAuxData()
	super.setAuxData(s, index, hkIndex);
	switchLevels.add(new Integer(s.switchLevels[index]));
	int nchildren = children.size();
        for (int i=0; i<nchildren; i++) {
            switchStates = childrenSwitchStates.get(i);
            switchStates.add(hkIndex, new SwitchState(true));
        }
    }

    void setNodeData(SetLiveState s) {
        super.setNodeData(s);
        // add this node to parent's childSwitchLink
        if (s.childSwitchLinks != null) {
            if(!inSharedGroup ||
	       // only add if not already added in sharedGroup
	       !s.childSwitchLinks.contains(this)) {
                s.childSwitchLinks.add(this);
            }
        }
        // Note that s.childSwitchLinks is updated in super.setLive
        s.parentSwitchLink = this;

        if (!inSharedGroup) {
            setAuxData(s, 0, 0);
        } else {
            // For inSharedGroup case.
            int j, hkIndex;

            s.hashkeyIndex = new int[s.keys.length];
            for(j=0; j<s.keys.length; j++) {
                hkIndex = s.keys[j].equals(localToVworldKeys, 0,
						localToVworldKeys.length);
                if(hkIndex >= 0) {
                    setAuxData(s, j, hkIndex);
                } else {
                    MasterControl.getCoreLogger().severe("Can't Find matching hashKey in setNodeData.");
                }
                s.hashkeyIndex[j] = hkIndex;
            }
        }
    }

    void setLive(SetLiveState s) {
    	int i,j,k;
        boolean switchOn;
	SwitchRetained switchRoot;
	int size;

        // save setLiveState
        Targets[] savedSwitchTargets = s.switchTargets;
	ArrayList<SwitchState> savedSwitchStates = s.switchStates;
        SwitchRetained[] savedClosestSwitchParents = s.closestSwitchParents;
        int[] savedClosestSwitchIndices = s.closestSwitchIndices;
        ArrayList savedChildSwitchLinks = s.childSwitchLinks;
        GroupRetained savedParentSwitchLink = s.parentSwitchLink;
        int[] savedHashkeyIndex = s.hashkeyIndex;

        // update setLiveState for this node
        s.closestSwitchParents = (SwitchRetained[])
					savedClosestSwitchParents.clone();
        s.closestSwitchIndices = (int[])savedClosestSwitchIndices.clone();

        // Note that s.containsNodesList is updated in super.setLive
        // Note that s.closestSwitchIndices is updated in super.setLive
        for (i=0; i< s.switchLevels.length; i++) {
            s.switchLevels[i]++;
            s.closestSwitchParents[i] = this;
        }

        super.doSetLive(s);

        initRenderChildMask();

	// update switch leaves' compositeSwitchMask
	// and update switch leaves' switchOn flag if this is top level switch
        if (inSharedGroup) {
            for (i=0; i<s.keys.length; i++) {
                j = s.hashkeyIndex[i];
                // j is index in ContainNodes
                if (j < localToVworldKeys.length) {
                    switchRoot = (s.switchLevels[i] == 0)? this : null;
		    size = children.size();
		    for (k=0; k<size; k++) {
                        switchOn = renderChildMask.get(k);
                        traverseSwitchChild(k, s.keys[i], j, switchRoot, true, false,
					    switchOn, s.switchLevels[i], null);
                    }
                }
            }
        } else {
            switchRoot = (s.switchLevels[0] == 0)? this : null;
	    size = children.size();
	    for (i=0; i<size; i++) {
                switchOn = renderChildMask.get(i);
                traverseSwitchChild(i, null, 0, switchRoot, true, false,
                                                switchOn, s.switchLevels[0], null);
            }
        }

        // restore setLiveState
        s.switchTargets = savedSwitchTargets;
        s.switchStates = savedSwitchStates;
        s.closestSwitchParents = savedClosestSwitchParents;
        s.closestSwitchIndices = savedClosestSwitchIndices;
        for (i=0; i< s.switchLevels.length; i++) {
            s.switchLevels[i]--;
        }
        s.childSwitchLinks = savedChildSwitchLinks;
        s.parentSwitchLink = savedParentSwitchLink;
        s.hashkeyIndex = savedHashkeyIndex;
	super.markAsLive();
    }


    void removeNodeData(SetLiveState s) {

        int numChildren = children.size();
        int i, j;
	ArrayList<SwitchState> switchStates;

	if (refCount <= 0) {
        // remove this node from parentSwitchLink's childSwitchLinks
        // clear childSwitchLinks
            ArrayList switchLinks;
            if (parentSwitchLink != null) {
                for(i=0; i<parentSwitchLink.childrenSwitchLinks.size();i++) {
                    switchLinks = (ArrayList)
                                parentSwitchLink.childrenSwitchLinks.get(i);
                    if (switchLinks.contains(this)) {
                        switchLinks.remove(this);
                        break;
                    }
		}
            }
            for (j=0; j<numChildren; j++) {
                switchStates = childrenSwitchStates.get(j);
                switchStates.clear();
            }
            switchLevels.remove(0);
	} else {
            // remove children dependent data
            int hkIndex;

            // Must be in reverse, to preserve right indexing.
            for (i = s.keys.length-1; i >= 0; i--) {
                hkIndex = s.keys[i].equals(localToVworldKeys, 0,
                                        localToVworldKeys.length);
                if(hkIndex >= 0) {
                    for (j=0; j<numChildren; j++) {
                	switchStates = (ArrayList)childrenSwitchStates.get(j);
                	switchStates.remove(hkIndex);
                    }
                    switchLevels.remove(hkIndex);
                }
            }
        }

        super.removeNodeData(s);
    }



    // synchronized with setWhichChild and setChildMask
    synchronized void clearLive(SetLiveState s) {
        Targets[] savedSwitchTargets = s.switchTargets;
	s.switchTargets = null;
        super.clearLive(s);
        s.switchTargets = savedSwitchTargets;
    }

    void initRenderChildMask() {
	int i, nchildren;
	nchildren = children.size();
        switch(whichChild) {
        case Switch.CHILD_ALL:
            for (i=0; i<nchildren; i++) {
                renderChildMask.set(i);
            }
            break;
        case Switch.CHILD_NONE:
            for (i=0; i<nchildren; i++) {
                renderChildMask.clear(i);
            }
            break;
        case Switch.CHILD_MASK:
            for (i=0; i<nchildren; i++) {
                if (childMask.get(i) == true) {
                    renderChildMask.set(i);
                } else {
                    renderChildMask.clear(i);
                }
            }
            break;
        default:
            for (i=0; i<nchildren; i++) {

                if (i == whichChild) {
                    renderChildMask.set(i);
                } else {
                    renderChildMask.clear(i);
                }
            }
        }
    }

    void traverseSwitchChild(int child, HashKey key, int index,
			     SwitchRetained switchRoot, boolean init,
			     boolean swChanged, boolean switchOn,
			     int switchLevel, ArrayList<SwitchState> updateList) {
	int i,j,k;
	SwitchRetained sw;
	LinkRetained ln;
	Object obj;
        ArrayList childSwitchLinks;

	boolean newSwChanged = false;
        ArrayList<SwitchState> childSwitchStates = childrenSwitchStates.get(child);
        SwitchState switchState = childSwitchStates.get(index);
        switchState.updateCompositeSwitchMask(switchLevel, switchOn);

        if (switchRoot != null) {
            if (init) {
                if (!switchState.initialized) {
                    switchState.initSwitchOn();
                }
            } else {
                boolean compositeSwitchOn = switchState.evalCompositeSwitchOn();
                if (switchState.cachedSwitchOn != compositeSwitchOn) {
                    switchState.updateCachedSwitchOn();

                    switchRoot.updateTargets.addCachedTargets(
						switchState.cachedTargets);
                    newSwChanged = true;
                    updateList.add(switchState);
                }
            }
        }


        childSwitchLinks = (ArrayList)childrenSwitchLinks.get(child);
	int cslSize =childSwitchLinks.size();
        for (i=0; i<cslSize; i++) {

	    obj = childSwitchLinks.get(i);
            if (obj instanceof SwitchRetained) {
                sw = (SwitchRetained)obj;
		int swSize = sw.children.size();
		for(j=0; j<swSize; j++) {
                    sw.traverseSwitchChild(j, key, index, switchRoot, init,
					   newSwChanged, switchOn, switchLevel,
					   updateList);
		}
            } else { // LinkRetained
                ln = (LinkRetained)obj;
		if (key == null) {
                    switchKey.reset();
                    switchKey.append(locale.nodeId);
		} else {
		    switchKey.set(key);
		}
		switchKey.append(LinkRetained.plus).append(ln.nodeId);

		if ((ln.sharedGroup != null) &&
		    (ln.sharedGroup.localToVworldKeys != null)) {

		    j = switchKey.equals(ln.sharedGroup.localToVworldKeys,0,
					 ln.sharedGroup.localToVworldKeys.length);
		    if(j < 0) {
			System.err.println("SwitchRetained : Can't find hashKey");
		    }

		    if (j<ln.sharedGroup.localToVworldKeys.length) {
			int lscSize = ln.sharedGroup.children.size();
			for(k=0; k<lscSize; k++) {
			    ln.sharedGroup.traverseSwitchChild(k, ln.sharedGroup.
							       localToVworldKeys[j],
							       j, switchRoot,
							       init, newSwChanged,
							       switchOn, switchLevel, updateList);
			}
		    }
		}
	    }
	}
    }

    void traverseSwitchParent() {
        boolean switchOn;
        int switchLevel;
	SwitchRetained switchRoot;
	int i,j;
	int size;

        // first traverse this node's child
        if (inSharedGroup) {
            for (j=0; j<localToVworldKeys.length; j++) {
                switchLevel = switchLevels.get(j).intValue();
                switchRoot = (switchLevel == 0)? this : null;
		size = children.size();
		for (i=0; i<size; i++) {
                    switchOn = renderChildMask.get(i);
                    traverseSwitchChild(i, localToVworldKeys[j], j, switchRoot,
					true, false, switchOn, switchLevel, null);
                }
            }
        } else {
            switchLevel = switchLevels.get(0).intValue();
            switchRoot = (switchLevel == 0)? this : null;
             size = children.size();
	     for (i=0; i<size; i++) {
                switchOn = renderChildMask.get(i);
                traverseSwitchChild(i, null, 0, switchRoot,
				    true, false,  switchOn, switchLevel, null);
            }
        }

        // now traverse this node's parent
        if (parentSwitchLink != null) {
            if (parentSwitchLink instanceof SwitchRetained) {
                ((SwitchRetained)parentSwitchLink).traverseSwitchParent();
            } else if (parentSwitchLink instanceof SharedGroupRetained) {
                ((SharedGroupRetained)parentSwitchLink).traverseSwitchParent();
            }
        }
    }


    void computeCombineBounds(Bounds bounds) {
        if (boundsAutoCompute) {
            if (!VirtualUniverse.mc.cacheAutoComputedBounds) {
                if (whichChild == Switch.CHILD_ALL) {
				for (int i = 0; i < children.size(); i++) {
					NodeRetained child = children.get(i);
                        if (child != null) {
                            child.computeCombineBounds(bounds);
                        }
                    }
                } else if (whichChild == Switch.CHILD_MASK) {
				for (int i = 0; i < children.size(); i++) {
                        if (childMask.get(i)) {
						NodeRetained child = children.get(i);
                            if (child != null) {
                                child.computeCombineBounds(bounds);
                            }
                        }
                    }
                } else if (whichChild != Switch.CHILD_NONE) {
                    if (whichChild < children.size()) {
					NodeRetained child = children.get(whichChild);
                        if (child != null) {
                            child.computeCombineBounds(bounds);
                        }
                    }
                }
            } else {
                // Issue 514 : NPE in Wonderland : triggered in cached bounds computation
                if (!validCachedBounds) {
                    validCachedBounds = true;

                    // Issue 544
                    if (VirtualUniverse.mc.useBoxForGroupBounds) {
                        cachedBounds = new BoundingBox((Bounds) null);
                    } else {
					cachedBounds = new BoundingSphere((Bounds)null);
                    }
                    if (whichChild == Switch.CHILD_ALL) {
					for (int i = 0; i < children.size(); i++) {
						NodeRetained child = children.get(i);
                            if (child != null) {
                                child.computeCombineBounds(cachedBounds);
                            }
                        }
                    } else if (whichChild == Switch.CHILD_MASK) {
					for (int i = 0; i < children.size(); i++) {
                            if (childMask.get(i)) {
							NodeRetained child = children.get(i);
                                if (child != null) {
                                    child.computeCombineBounds(cachedBounds);
                                }
                            }
                        }
                    } else if (whichChild != Switch.CHILD_NONE) {
                        if (whichChild < children.size()) {
						NodeRetained child = children.get(whichChild);
                            if (child != null) {
                                child.computeCombineBounds(cachedBounds);
                            }
                        }
                    }
                }
                bounds.combine(cachedBounds);
            }
        } else {
            // Should this be lock too ? ( MT safe  ? )
            synchronized (localBounds) {
                bounds.combine(localBounds);
            }
        }
    }


  /**
   * Gets the bounding object of a node.
   * @return the node's bounding object
   */
    Bounds getBounds() {
        if (boundsAutoCompute) {
            // Issue 514 : NPE in Wonderland : triggered in cached bounds computation
            if (validCachedBounds) {
                return (Bounds) cachedBounds.clone();
            }

            // issue 544
            Bounds boundingObject = null;
            if (VirtualUniverse.mc.useBoxForGroupBounds) {
                boundingObject = new BoundingBox((Bounds) null);
            } else {
			boundingObject = new BoundingSphere((Bounds)null);
            }

            if (whichChild == Switch.CHILD_ALL) {
			for (int i = 0; i < children.size(); i++) {
				NodeRetained child = children.get(i);
                    if (child != null) {
                        child.computeCombineBounds((Bounds) boundingObject);
                    }
                }
            } else if (whichChild == Switch.CHILD_MASK) {
			for (int i = 0; i < children.size(); i++) {
                    if (childMask.get(i)) {
					NodeRetained child = children.get(i);
                        if (child != null) {
                            child.computeCombineBounds((Bounds) boundingObject);
                        }
                    }
                }
            } else if (whichChild != Switch.CHILD_NONE &&
                    whichChild >= 0 &&
                    whichChild < children.size()) {

			NodeRetained child = children.get(whichChild);
                if (child != null) {
                    child.computeCombineBounds((Bounds) boundingObject);
                }
            }

            return (Bounds) boundingObject;
        } else {
            return super.getBounds();
        }
    }


    /*
    void compile(CompileState compState) {
	setCompiled();
	compState.startGroup(null); // don't merge at this level
	compileChildren(compState);
	compState.endGroup();
    }
    */

    /**
     * Compiles the children of the switch, preventing shape merging at
     * this level or above
     */
    void compile(CompileState compState) {


        super.compile(compState);

	// don't remove this group node
        mergeFlag = SceneGraphObjectRetained.DONT_MERGE;

        if (J3dDebug.devPhase && J3dDebug.debug) {
            compState.numSwitches++;
        }
    }

    void insertChildrenData(int index) {
        if (childrenSwitchStates == null) {
		childrenSwitchStates = new ArrayList<ArrayList<SwitchState>>(1);
            childrenSwitchLinks = new ArrayList(1);
        }

        childrenSwitchLinks.add(index, new ArrayList(1));

	ArrayList<SwitchState> switchStates = new ArrayList<SwitchState>(1);
        childrenSwitchStates.add(index, switchStates);
        if (source != null && source.isLive()) {
            for (int i=0; i<localToVworld.length; i++) {
                switchStates.add(new SwitchState(true));
            }
        }
    }

    void appendChildrenData() {
        if (childrenSwitchStates == null) {
            childrenSwitchStates = new ArrayList<ArrayList<SwitchState>>(1);
            childrenSwitchLinks = new ArrayList(1);
        }
        childrenSwitchLinks.add(new ArrayList(1));

        ArrayList<SwitchState> switchStates = new ArrayList<SwitchState>(1);
        childrenSwitchStates.add(switchStates);
        if (source != null && source.isLive()) {
            for (int i=0; i<localToVworld.length; i++) {
                switchStates.add(new SwitchState(true));
            }
        }
    }

    void removeChildrenData(int index) {
	ArrayList<SwitchState> oldSwitchStates = childrenSwitchStates.get(index);
        oldSwitchStates.clear();
        childrenSwitchStates.remove(index);

        ArrayList oldSwitchLinks = (ArrayList)childrenSwitchLinks.get(index);
        oldSwitchLinks.clear();
        childrenSwitchLinks.remove(index);
    }

    void childDoSetLive(NodeRetained child, int childIndex, SetLiveState s) {

        int numPaths = (inSharedGroup)? s.keys.length : 1;
        s.childSwitchLinks = (ArrayList)childrenSwitchLinks.get(childIndex);
        for (int j=0; j< numPaths; j++) {
            s.closestSwitchIndices[j] = switchIndexCount;
            s.closestSwitchParents[j] = this;
        }
        // use switchIndexCount instead of child index to avoid
        // reordering due to add/remove child later
        switchIndexCount++;

        Targets[] newTargets = new Targets[numPaths];
        for(int i=0; i<numPaths; i++) {
            newTargets[i] = new Targets();
        }
        s.switchTargets = newTargets;
	s.switchStates = childrenSwitchStates.get(childIndex);

        if(child!=null)
            child.setLive(s);

        CachedTargets cachedTargets;
	SwitchState switchState;
        if (! inSharedGroup) {
            cachedTargets = s.switchTargets[0].snapShotInit();
		switchState = s.switchStates.get(0);
            switchState.cachedTargets = cachedTargets;
        } else {
            for(int i=0; i<numPaths; i++) {
                cachedTargets = s.switchTargets[i].snapShotInit();
			switchState = s.switchStates.get(s.hashkeyIndex[i]);
                switchState.cachedTargets = cachedTargets;
            }
        }
    }

    // ***************************
    // TargetsInterface methods
    // ***************************

    TargetsInterface getClosestTargetsInterface(int type) {
        return (type == TargetsInterface.SWITCH_TARGETS)?
                (TargetsInterface)this:
                (TargetsInterface)parentTransformLink;
    }

    public CachedTargets getCachedTargets(int type, int index, int child) {
        if (type == TargetsInterface.SWITCH_TARGETS) {
		ArrayList<SwitchState> switchStates = childrenSwitchStates.get(child);
            if (index < switchStates.size()) {
                SwitchState switchState = switchStates.get(index);
                return switchState.cachedTargets;
            } else {
                return null;
	    }
        } else {
            System.err.println("getCachedTargets: wrong arguments");
            return null;
        }
    }

    public void resetCachedTargets(int type,
			CachedTargets[] newCtArr, int child) {
        if (type == TargetsInterface.SWITCH_TARGETS) {
            ArrayList<SwitchState> switchStates = childrenSwitchStates.get(child);
            if (newCtArr.length != switchStates.size()) {
                System.err.println("resetCachedTargets: unmatched length!" +
				   newCtArr.length + " " + switchStates.size());
                System.err.println("  resetCachedTargets: " + this);
            }
            SwitchState switchState;
            for (int i=0; i<newCtArr.length; i++) {
                switchState = switchStates.get(i);
                switchState.cachedTargets = newCtArr[i];
            }
	} else {
            System.err.println("resetCachedTargets: wrong arguments");
	}
    }

public ArrayList<SwitchState> getTargetsData(int type, int child) {
	if (type == TargetsInterface.SWITCH_TARGETS) {
		return childrenSwitchStates.get(child);
	}
	else {
		System.err.println("getTargetsData: wrong arguments");
		return null;
	}
}

    public int getTargetThreads(int type) {
        System.err.println("getTargetsThreads: wrong arguments");
	return -1;
    }

    public void updateCachedTargets(int type, CachedTargets[] newCt) {
        System.err.println("updateCachedTarget: wrong arguments");
    }

    public void computeTargetThreads(int type, CachedTargets[] newCt) {
        System.err.println("computeTargetThreads: wrong arguments");
    }

    public void updateTargetThreads(int type, CachedTargets[] newCt) {
        System.err.println("updateTargetThreads: wrong arguments");
    }

    public void propagateTargetThreads(int type, int newTargetThreads) {
        System.err.println("propagateTargetThreads: wrong arguments");
    }

    public void copyCachedTargets(int type, CachedTargets[] newCt) {
        System.err.println("copyCachedTarget: wrong arguments");
    }
}
