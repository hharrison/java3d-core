/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

/**
 * The SharedGroup node provides the ability to share a scene graph from
 * multiple other scene graphs through the use of a Link node.
 */

class SharedGroupRetained extends GroupRetained implements TargetsInterface {

    /*
    static final int ILLEGAL_LEAF_MASK =
                          1 << NodeRetained.BACKGROUND |
                          1 << NodeRetained.BEHAVIOR |
                          1 << NodeRetained.CLIP |
                          1 << NodeRetained.LINEARFOG |
                          1 << NodeRetained.EXPONENTIALFOG |
                          1 << NodeRetained.SOUNDSCAPE |
                          1 << NodeRetained.VIEWPLATFORM |
                          1 << NodeRetained.BOUNDINGLEAF;
    */

    // The current list of child transform group nodes or link nodes
    // under a transform group
    ArrayList childTransformLinks = new ArrayList(1);

    // key which identifies a unique path from a
    // locale to this transform group
    HashKey currentKey = new HashKey();

    // key which identifies a unique path from a locale to this switch link
    HashKey switchKey = new HashKey();

    /**
     * The Shared Group Node's parent vector.
     */
    Vector parents = new Vector(1);

    // J3d copy.
    CachedTargets[] j3dCTs = null;

    // User copy.
    CachedTargets[] cachedTargets = null;

    // A bitmask of the types in targets for transform targets
    int localTargetThreads = 0;
    // combined localTargetThreads and decendants' localTargetThreads
    int targetThreads = 0;

    ArrayList switchStates = null;

    SharedGroupRetained() {
        this.nodeType = NodeRetained.SHAREDGROUP;
    }

    // SharedGroup specific data at SetLive.
    void setAuxData(SetLiveState s, int index, int hkIndex) {
	int i, size;

	// Group's setAuxData()
	super.setAuxData(s, index, hkIndex);

	branchGroupPaths.add(hkIndex, s.branchGroupPaths.get(index));

        if (orderedPaths == null) {
            orderedPaths = new ArrayList(1);
        }
        orderedPaths.add(hkIndex, s.orderedPaths.get(index));

        if (switchStates == null) {
            switchStates = new ArrayList(1);
        }
        switchStates.add(hkIndex, s.switchStates.get(index));

	if (viewLists == null) {
	    viewLists = new ArrayList(1);
	}
	// If there are some ViewSpecificGroups in the path above this SharedGroup
	//	System.err.println("====> hkIndex = "+hkIndex+" s.viewLists = "+s.viewLists);
	if (s.viewLists != null) {
	    viewLists.add(hkIndex, s.viewLists.get(index));
	}
	else {
	    viewLists.add(hkIndex, null);
	}

	if (lights == null) {
	    lights = new ArrayList(1);
	}
	if (s.lights != null) {
	    lights.add(hkIndex, s.lights.get(index));
	}
	else {
	    lights.add(hkIndex, null);
	}

	if (fogs == null) {
	    fogs = new ArrayList(1);
	}
	if (s.fogs != null) {
	    fogs.add(hkIndex, s.fogs.get(index));
	}
	else {
	    fogs.add(hkIndex, null);
	}


	if (modelClips == null) {
	    modelClips = new ArrayList(1);
	}
	if (s.modelClips != null) {
	    modelClips.add(hkIndex, s.modelClips.get(index));
	}
	else {
	    modelClips.add(hkIndex, null);
	}


	if (altAppearances == null) {
	    altAppearances = new ArrayList(1);
	}
	if (s.altAppearances != null) {
	    altAppearances.add(hkIndex, s.altAppearances.get(index));
	}
	else {
	    altAppearances.add(hkIndex, null);
	}
    }


    void setNodeData(SetLiveState s) {

	// For inSharedGroup case.
	int i, j, len;

	if (localToVworld == null) {
	    localToVworld = new Transform3D[s.keys.length][];
	    localToVworldIndex = new int[s.keys.length][];
	    localToVworldKeys = new HashKey[s.keys.length];
            cachedTargets = new CachedTargets[s.keys.length];
	    len=0;
	}
	else {

	    int newLen = localToVworld.length + s.keys.length;

	    Transform3D newTList[][] = new Transform3D[newLen][];
	    HashKey newHList[] = new HashKey[newLen];
	    int newIndexList[][] = new int[newLen][];
            CachedTargets newTargets[] = new CachedTargets[newLen];

	    len = localToVworld.length;

	    // Copy the existing data into the newly created data objects.
	    System.arraycopy(localToVworld, 0, newTList, 0, localToVworld.length);
	    System.arraycopy(localToVworldIndex, 0, newIndexList, 0,
			     localToVworldIndex.length);
	    System.arraycopy(localToVworldKeys, 0, newHList, 0,
			     localToVworldKeys.length);
            System.arraycopy(cachedTargets, 0, newTargets, 0,
                             cachedTargets.length);

	    localToVworld = newTList;
	    localToVworldIndex = newIndexList;
	    localToVworldKeys = newHList;
            cachedTargets = newTargets;
	}

	int[] hkIndex = new int[1];
	int hkIndexPlus1, blkSize;

        s.hashkeyIndex = new int[s.keys.length];

	// This should appear before super.setNodeData() if it exists
	s.parentBranchGroupPaths = branchGroupPaths;

	for(i=len, j=0; i<localToVworld.length; i++, j++) {

	    if(s.keys[j].equals(localToVworldKeys, hkIndex, 0, i)) {
		MasterControl.getCoreLogger().severe("Found matching hashKey in setNodeData.");
	    }
            s.hashkeyIndex[j] = hkIndex[0];


	    if(hkIndex[0] == i) { // Append to last.
		localToVworldKeys[i] = s.keys[j];
		localToVworld[i] = s.currentTransforms[j];
		localToVworldIndex[i] = s.currentTransformsIndex[j];
	    }
	    else { // Insert in between array elements.
		hkIndexPlus1 = hkIndex[0] + 1;
		blkSize = i - hkIndex[0];

		// Shift the later portion of array elements by one position.
		// This is the make room for the new data entry.
		System.arraycopy(localToVworldKeys, hkIndex[0], localToVworldKeys,
				 hkIndexPlus1, blkSize);
		System.arraycopy(localToVworld, hkIndex[0], localToVworld,
				 hkIndexPlus1, blkSize);
		System.arraycopy(localToVworldIndex, hkIndex[0], localToVworldIndex,
				 hkIndexPlus1, blkSize);
                System.arraycopy(cachedTargets, hkIndex[0], cachedTargets,
                                 hkIndexPlus1, blkSize);

		localToVworldKeys[hkIndex[0]] = s.keys[j];
		localToVworld[hkIndex[0]] = s.currentTransforms[j];
		localToVworldIndex[hkIndex[0]] = s.currentTransformsIndex[j];
	    }

	    //	    System.err.println("SG: j = "+j+" hkIndex[0] = "+hkIndex[0]+" s.keys[j] = "+s.keys[j]);
	    // For now (1.2.1beta2) only. We cleanup setLive, and clearLive in
	    // next release.
	    setAuxData(s, j, hkIndex[0]);
	}

	// The SetLiveState need the reflect the new state of this SharedGroup.
	// The SetLiveState will get reset back in SetLive, after all children of this
	// node have been set live.
	s.localToVworld = localToVworld;
	s.localToVworldIndex = localToVworldIndex;
	s.localToVworldKeys = localToVworldKeys;
        s.orderedPaths = orderedPaths;
        s.switchStates = switchStates;

        // Note that s.childSwitchLinks is updated in super.setLive
        s.childTransformLinks = childTransformLinks;
        s.parentTransformLink = this;
        s.parentSwitchLink = this;
	s.viewLists = viewLists;
	s.lights = lights;
	s.fogs = fogs;
	s.altAppearances = altAppearances;
	s.modelClips = modelClips;
    }

    void setLive(SetLiveState s) {

	int i,j;
	Targets[] newTargets = null;

        // save setLiveState
	Transform3D savedLocalToVworld[][] = s.localToVworld;
	int savedLocalToVworldIndex[][] = s.localToVworldIndex;
	HashKey savedLocalToVworldKeys[] = s.localToVworldKeys;
        ArrayList savedOrderedPaths = s.orderedPaths;
	ArrayList savedViewList = s.viewLists;
	ArrayList savedLights = s.lights;
	ArrayList savedFogs = s.fogs;
	ArrayList savedMclips = s.modelClips;
	ArrayList savedAltApps = s.altAppearances;

	SharedGroupRetained savedLastSharedGroup = s.lastSharedGroup;
        Targets[] savedSwitchTargets = s.switchTargets;
        ArrayList savedSwitchStates = s.switchStates;
        ArrayList savedChildSwitchLinks = s.childSwitchLinks;
        GroupRetained savedParentSwitchLink = s.parentSwitchLink;
        ArrayList savedChildTransformLinks = s.childTransformLinks;
        GroupRetained savedParentTransformLink = s.parentTransformLink;
        int[] savedHashkeyIndex = s.hashkeyIndex;

        // update setLiveState for this node
        // Note that s.containsNodesList is updated in super.setLive
	s.lastSharedGroup = this;

        Targets[] savedTransformTargets = s.transformTargets;

        int numPaths = s.keys.length;
        newTargets = new Targets[numPaths];
        for(i=0; i<numPaths; i++) {
	    if (s.transformLevels[i] >= 0) {
                newTargets[i] = new Targets();
	    } else {
                newTargets[i] = null;
	    }
        }
	s.transformTargets = newTargets;

        super.setLive(s);

        int hkIndex;
        for(i=0; i<numPaths; i++) {
            if (s.transformTargets[i] != null) {
		hkIndex = s.hashkeyIndex[i];
                cachedTargets[hkIndex] = s.transformTargets[i].snapShotInit();
            }
        }
        // Assign data in cachedTargets to j3dCTs.
        j3dCTs = new CachedTargets[cachedTargets.length];
        copyCachedTargets(TargetsInterface.TRANSFORM_TARGETS, j3dCTs);

        computeTargetThreads(TargetsInterface.TRANSFORM_TARGETS, cachedTargets);


        // restore setLiveState
	s.localToVworld = savedLocalToVworld;
	s.localToVworldIndex = savedLocalToVworldIndex;
	s.localToVworldKeys = savedLocalToVworldKeys;
        s.orderedPaths = savedOrderedPaths;
	s.viewLists = savedViewList;

	s.lights = savedLights;
	s.fogs = savedFogs;
	s.modelClips = savedMclips;
	s.altAppearances = savedAltApps;

	s.lastSharedGroup = savedLastSharedGroup;
        s.switchTargets = savedSwitchTargets;
        s.switchStates = savedSwitchStates;

        s.childSwitchLinks = savedChildSwitchLinks;
        s.parentSwitchLink = savedParentSwitchLink;
        s.childTransformLinks = savedChildTransformLinks;
        s.parentTransformLink = savedParentTransformLink;

        s.transformTargets = savedTransformTargets;
        s.hashkeyIndex = savedHashkeyIndex;
/*
// XXXX : port this
        for (int i=0; i < children.size(); i++) {
            if ((childContains[i][0] & ILLEGAL_LEAF_MASK) != 0) {
                throw new IllegalSharingException(J3dI18N.getString("SharedGroupRetained0"));            }
        }
*/
    }


    /**
     * remove the localToVworld transform for a node.
     */
    void removeNodeData(SetLiveState s) {

	int numChildren = children.size();
        ArrayList switchTargets;
	int i,j;

        if (refCount <= 0) {
	    localToVworld = null;
	    localToVworldIndex = null;
	    localToVworldKeys = null;
	    // restore to default and avoid calling clear()
	    // that may clear parent reference branchGroupPaths
	    // Note that this function did not invoke super.removeNodeData()
	    branchGroupPaths = new ArrayList(1);
            orderedPaths = null;
            switchStates = null;
            cachedTargets = null;
            targetThreads = 0;
	    lights.clear();
	    fogs.clear();
	    modelClips.clear();
	    altAppearances.clear();
	}
	else {
	    int index, len;

	    // Remove the localToVworld key
	    int newLen = localToVworld.length - s.keys.length;

	    Transform3D[][] newTList = new Transform3D[newLen][];
	    HashKey[] newHList = new HashKey[newLen];
	    Transform3D newChildTList[][] = null;
	    int[][] newIndexList = new int[newLen][];
            CachedTargets[] newTargets = new CachedTargets[newLen];

	    int[] tempIndex = new int[s.keys.length];
	    int curStart =0, newStart =0;
	    boolean found = false;

	    for(i=0;i<s.keys.length;i++) {
		index = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);

		tempIndex[i] = index;

		if(index >= 0) {
		    found = true;
		    if(index == curStart) {
			curStart++;
		    }
		    else {
			len = index - curStart;
			System.arraycopy(localToVworld, curStart, newTList, newStart, len);
			System.arraycopy(localToVworldIndex, curStart, newIndexList,
					 newStart, len);
			System.arraycopy(localToVworldKeys, curStart, newHList, newStart, len);
                        System.arraycopy(cachedTargets, curStart, newTargets,
                                         newStart, len);

			curStart = index+1;
			newStart = newStart + len;
		    }
		}
		else {
		    found = false;
		    MasterControl.getCoreLogger().severe("Can't Find matching hashKey in SG.removeNodeData.");
		}
	    }

	    if((found == true) && (curStart < localToVworld.length)) {
		len = localToVworld.length - curStart;
		System.arraycopy(localToVworld, curStart, newTList, newStart, len);
		System.arraycopy(localToVworldIndex, curStart, newIndexList,
				 newStart, len);
		System.arraycopy(localToVworldKeys, curStart, newHList, newStart, len);
                System.arraycopy(cachedTargets, curStart, newTargets,
                                 newStart, len);
	    }

	    // Must be in reverse, to preserve right indexing.
	    for (i = tempIndex.length-1; i >= 0 ; i--) {
		if(tempIndex[i] >= 0) {
		    branchGroupPaths.remove(tempIndex[i]);
                    orderedPaths.remove(tempIndex[i]);
                    switchStates.remove(tempIndex[i]);
		    lights.remove(tempIndex[i]);
		    fogs.remove(tempIndex[i]);
		    modelClips.remove(tempIndex[i]);
		    altAppearances.remove(tempIndex[i]);
		}
	    }

	    localToVworld = newTList;
	    localToVworldIndex = newIndexList;
	    localToVworldKeys = newHList;
            cachedTargets = newTargets;
	}
	s.localToVworld = localToVworld;
	s.localToVworldIndex = localToVworldIndex;
	s.localToVworldKeys = localToVworldKeys;
	s.orderedPaths = orderedPaths;
	s.switchStates = switchStates;
	s.viewLists = viewLists;
	s.lights = lights;
	s.fogs = fogs;
	s.modelClips = modelClips;
	s.altAppearances = altAppearances;
    }

    void clearLive(SetLiveState s) {

        int i,j,k, index;

        Transform3D savedLocalToVworld[][] = s.localToVworld;
        int savedLocalToVworldIndex[][] = s.localToVworldIndex;
        HashKey savedLocalToVworldKeys[] = s.localToVworldKeys;
        ArrayList savedOrderedPaths = s.orderedPaths;
        ArrayList savedViewLists = s.viewLists;

	ArrayList savedLights = s.lights;
	ArrayList savedFogs = s.fogs;
	ArrayList savedMclips = s.modelClips;
	ArrayList savedAltApps = s.altAppearances;

        Targets[] savedSwitchTargets = s.switchTargets;
        Targets[] savedTransformTargets = s.transformTargets;
        // no need to gather targets from sg in clear live
        s.transformTargets = null;
        s.switchTargets = null;


	// XXXX: This is a hack since removeNodeData is called before
	// children are clearLives
	int[] tempIndex = null;
	// Don't keep the indices if everything will be cleared
	if (s.keys.length != localToVworld.length) {
	    tempIndex = new int[s.keys.length];
	    for (i = s.keys.length-1; i >= 0; i--) {
		tempIndex[i] = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);
	    }
	}

        super.clearLive(s);
	// Do this after children clearlive since part of the viewLists may get cleared
	// during removeNodeData
        if(refCount <= 0) {
	    viewLists.clear();
	}
	else {
	    // Must be in reverse, to preserve right indexing.
	    for (i = tempIndex.length-1; i >= 0 ; i--) {
		if(tempIndex[i] >= 0) {
		    viewLists.remove(tempIndex[i]);
		}
	    }
	}

        // restore setLiveState from it's local variables.
        // removeNodeData has altered these variables.
        s.localToVworld = savedLocalToVworld;
        s.localToVworldIndex = savedLocalToVworldIndex;
        s.localToVworldKeys = savedLocalToVworldKeys;
        s.orderedPaths = savedOrderedPaths;
        s.viewLists = savedViewLists;
	s.lights = savedLights;
	s.fogs = savedFogs;
	s.modelClips = savedMclips;
	s.altAppearances = savedAltApps;
        s.transformTargets = savedTransformTargets;
        s.switchTargets = savedSwitchTargets;
    }

    void updateChildLocalToVworld(HashKey key, int index,
                                        ArrayList dirtyTransformGroups,
                                        ArrayList keySet,
		                        UpdateTargets targets,
                                        ArrayList blUsers) {

        LinkRetained ln;
        TransformGroupRetained tg;
        int i,j;
	Object obj;

        CachedTargets ct = j3dCTs[index];
        if (ct != null) {
            targets.addCachedTargets(ct);
            if (ct.targetArr[Targets.BLN_TARGETS] != null) {
                gatherBlUsers(blUsers, ct.targetArr[Targets.BLN_TARGETS]);
            }
        }

        synchronized(childTransformLinks) {
        for (i=0; i<childTransformLinks.size(); i++) {
            obj = childTransformLinks.get(i);

            if (obj instanceof TransformGroupRetained) {
                tg = (TransformGroupRetained)obj;
                tg.updateChildLocalToVworld(
				tg.localToVworldKeys[index], index,
				dirtyTransformGroups, keySet,
				targets, blUsers);


            } else { // LinkRetained
                ln = (LinkRetained)obj;
                currentKey.set(key);
                currentKey.append(LinkRetained.plus).append(ln.nodeId);
                if (ln.sharedGroup.localToVworldKeys != null) {
		    j = currentKey.equals(ln.sharedGroup.localToVworldKeys,0,
					  ln.sharedGroup.localToVworldKeys.length);
		    if(j < 0) {
			System.err.println("SharedGroupRetained : Can't find hashKey");
		    }

                    if (j < ln.sharedGroup.localToVworldKeys.length) {
                        ln.sharedGroup.updateChildLocalToVworld(
				ln.sharedGroup.localToVworldKeys[j], j,
				dirtyTransformGroups, keySet,
				targets, blUsers);
                    }
                }
            }
        }
        }
    }

    void traverseSwitchChild(int child, HashKey key,
			     int index, SwitchRetained switchRoot,
			     boolean init, boolean swChanged,
			     boolean switchOn, int switchLevel,
			     ArrayList updateList) {

        SwitchRetained sw;
        LinkRetained ln;
        Object obj;
        ArrayList childSwitchLinks;
	int i,j,k;

        childSwitchLinks = (ArrayList)childrenSwitchLinks.get(child);
        for (i=0; i<childSwitchLinks.size(); i++) {
            obj = childSwitchLinks.get(i);

            if (obj instanceof SwitchRetained) {
                sw = (SwitchRetained)obj;
                for(j=0; j<sw.children.size(); j++) {
                    sw.traverseSwitchChild(j, key, index, switchRoot,
					   init, swChanged, switchOn, switchLevel, updateList);
                }
            } else { // LinkRetained
                ln = (LinkRetained)obj;
                switchKey.set(key);
                switchKey.append(LinkRetained.plus).append(ln.nodeId);

		if (ln.sharedGroup.localToVworldKeys != null) {

		    j = switchKey.equals(ln.sharedGroup.localToVworldKeys,0,
					 ln.sharedGroup.localToVworldKeys.length);
		    if(j < 0) {
			System.err.println("SharedGroupRetained : Can't find hashKey");
		    }

		    if (j < ln.sharedGroup.localToVworldKeys.length) {
			for(k=0; k<ln.sharedGroup.children.size(); k++) {
			    ln.sharedGroup.traverseSwitchChild(k,
							       ln.sharedGroup.
							       localToVworldKeys[j],
							       j, switchRoot, init,
							       swChanged, switchOn,
							       switchLevel, updateList);
			}
		    }
		}
            }
        }
    }

    void traverseSwitchParent() {
        int i;
        NodeRetained ln;

        for(i=0; i<parents.size(); i++) {
            ln = (NodeRetained) parents.elementAt(i);
            if (ln.parentSwitchLink != null) {
                if (parentSwitchLink instanceof SwitchRetained) {
                    ((SwitchRetained)parentSwitchLink).traverseSwitchParent();
                } else if (parentSwitchLink instanceof SharedGroupRetained) {
                    ((SharedGroupRetained)parentSwitchLink).traverseSwitchParent();
                }
            }
        }
    }

    // Top level compile call, same as BranchGroup.compile()
    void compile()  {

        if (source.isCompiled() || VirtualUniverse.mc.disableCompile)
            return;

        if (J3dDebug.devPhase && J3dDebug.debug) {
            J3dDebug.doDebug(J3dDebug.compileState, J3dDebug.LEVEL_3,
                        "SharedGroupRetained.compile()....\n");
        }

        CompileState compState = new CompileState();

        isRoot = true;

        compile(compState);
        merge(compState);

        if (J3dDebug.devPhase && J3dDebug.debug) {
            if (J3dDebug.doDebug(J3dDebug.compileState, J3dDebug.LEVEL_3)) {
                compState.printStats();
            }
            if (J3dDebug.doDebug(J3dDebug.compileState, J3dDebug.LEVEL_5)) {
                this.traverse(false, 1);
                System.err.println();
            }
        }

    }

    /**
     * Returns the Link nodes that refer to this SharedGroup node
     * @return An array of Link nodes
     */
    Link[] getLinks() {
	Link[] links;
	// make sure this method is MT-safe
	synchronized(parents) {
	    int n = parents.size();
	    // allocate new array
	    links = new Link[n];
	    for(int i = 0; i < n; i++) {
		// copy Link nodes from this node's list of parents
		links[i] = (Link)((LinkRetained)parents.elementAt(i)).source;
	    }
	}
	return links;
    }

    void insertChildrenData(int index) {
        if (childrenSwitchLinks == null) {
            childrenSwitchLinks = new ArrayList(1);
        }
        childrenSwitchLinks.add(index, new ArrayList(1));
    }

    void appendChildrenData() {
        if (childrenSwitchLinks == null) {
            childrenSwitchLinks = new ArrayList(1);
        }
        childrenSwitchLinks.add(new ArrayList(1));
    }

    void removeChildrenData(int index) {
        ArrayList oldSwitchLinks = (ArrayList)childrenSwitchLinks.get(index);
        oldSwitchLinks.clear();
        childrenSwitchLinks.remove(index);
    }


    // ***************************
    // TargetsInterface methods
    // ***************************

    public int getTargetThreads(int type) {
	if (type == TargetsInterface.TRANSFORM_TARGETS) {
            return targetThreads;
        } else {
            System.err.println("getTargetThreads: wrong arguments");
	    return -1;
        }
    }

    TargetsInterface getClosestTargetsInterface(int type) {
        return this;
    }

    // re-evalute localTargetThreads using newCachedTargets and
    // re-evaluate targetThreads
    public void computeTargetThreads(int type,
					CachedTargets[] newCachedTargets) {

	localTargetThreads = 0;
	if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    for(int i=0; i<newCachedTargets.length; i++) {
	        if (newCachedTargets[i] != null) {
                    localTargetThreads |= newCachedTargets[i].computeTargetThreads();
		}
	    }
	    targetThreads = localTargetThreads;

	    int numLinks = childTransformLinks.size();
	    TargetsInterface childLink;
	    NodeRetained node;

	    for(int i=0; i<numLinks; i++) {
		node = (NodeRetained)childTransformLinks.get(i);
		if (node.nodeType == NodeRetained.LINK) {
		    childLink = (TargetsInterface)
			((LinkRetained)node).sharedGroup;
		} else {
		    childLink = (TargetsInterface) node;
		}
		if (childLink != null) {
		    targetThreads |=
			childLink.getTargetThreads(TargetsInterface.TRANSFORM_TARGETS);
		}
	    }

        } else {
            System.err.println("computeTargetsThreads: wrong arguments");
        }
    }

    // re-compute localTargetThread, targetThreads and
    // propagate changes to ancestors
    public void updateTargetThreads(int type, CachedTargets[] newCachedTargets) {
        // type is ignored here, only need for SharedGroup
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
	    computeTargetThreads(type, newCachedTargets);
            if (parentTransformLink != null) {
                TargetsInterface pti = (TargetsInterface)parentTransformLink;
                pti.propagateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
                                        targetThreads);
            }
        } else {
            System.err.println("updateTargetThreads: wrong arguments");
        }
    }

    // re-evaluate targetThreads using childTargetThreads and
    // propagate changes to ancestors
    public void propagateTargetThreads(int type, int childTargetThreads) {
        if (type == TargetsInterface.TRANSFORM_TARGETS) {
            LinkRetained ln;
	    // XXXX : For now we'll OR more than exact.
            //targetThreads = localTargetThreads | childTargetThreads;
	    targetThreads = targetThreads | childTargetThreads;
            for(int i=0; i<parents.size(); i++) {
                ln = (LinkRetained) parents.elementAt(i);
                if (ln.parentTransformLink != null) {
                    TargetsInterface pti =
                                (TargetsInterface)ln.parentTransformLink;
                    pti.propagateTargetThreads(type, targetThreads);
                }
            }
        } else {
            System.err.println("propagateTargetThreads: wrong arguments");
        }
    }

    public void updateCachedTargets(int type, CachedTargets[] newCt) {
	if (type == TargetsInterface.TRANSFORM_TARGETS) {
            j3dCTs = newCt;
        } else {
            System.err.println("updateCachedTargets: wrong arguments");
        }
    }

    public void copyCachedTargets(int type, CachedTargets[] newCt) {
	if (type == TargetsInterface.TRANSFORM_TARGETS) {
            int size = cachedTargets.length;
            for (int i=0; i<size; i++) {
                newCt[i] = cachedTargets[i];
            }
	} else {
            System.err.println("copyCachedTargets: wrong arguments");
        }
    }

    public CachedTargets getCachedTargets(int type, int index, int child) {
	if (type == TargetsInterface.SWITCH_TARGETS) {
            // child info is not used, SG does not have per child states
            if (index < switchStates.size()) {
                SwitchState switchState = (SwitchState)switchStates.get(index);
                return switchState.cachedTargets;
            } else {
                return null;
            }
        } else {
	    // type == TargetsInterface.TRANSFORM_TARGETS
            return cachedTargets[index];
        }
    }

    public void resetCachedTargets(int type,
			CachedTargets[] newCtArr,int child) {
	if (type == TargetsInterface.SWITCH_TARGETS) {
            // child info is not used, SG does not have per child states
            SwitchState switchState;
            if (newCtArr.length != switchStates.size()) {
                System.err.println("resetCachedTargets: unmatched length!" +
                                   newCtArr.length + " " + switchStates.size());
                System.err.println("  resetCachedTargets: " + this);
            }
            for (int i=0; i<newCtArr.length; i++) {
                switchState = (SwitchState)switchStates.get(i);
                switchState.cachedTargets = newCtArr[i];
            }

        } else {
	    // type == TargetsInterface.TRANSFORM_TARGETS
            cachedTargets = newCtArr;
        }
    }

    public ArrayList getTargetsData(int type, int index) {
        // index is ignores for SharedGroup
	if (type == TargetsInterface.SWITCH_TARGETS) {
            return switchStates;
        } else {
            System.err.println("getTargetsData: wrong arguments");
	    return null;
        }
    }


    void childDoSetLive(NodeRetained child, int childIndex, SetLiveState s) {

	int i;
        s.childSwitchLinks = (ArrayList)childrenSwitchLinks.get(childIndex);
        s.switchStates = switchStates;

        if(child!=null)
	    child.setLive(s);
    }

    void childCheckSetLive(NodeRetained child, int childIndex, SetLiveState s) {
        s.childTransformLinks = childTransformLinks;
        s.parentTransformLink = this;
        child.setLive(s);
    }

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
            synchronized(parents) {
                Enumeration e = parents.elements();
                while(e.hasMoreElements()) {
                    LinkRetained parent = (LinkRetained) e.nextElement();
                    if (parent!=null) {
                        parent.dirtyBoundsCache();
                    }
                }
            }
        }
    }
}
