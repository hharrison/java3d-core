/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
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
 * The BranchGroup node provides the ability to insert a branch of
 * a scene graph into the virtual world.
 */

class BranchGroupRetained extends GroupRetained {

    // This boolean is used in a moveTo operation to get transforms to update
    boolean isDirty = false;

    // This boolean is used when the BG is inserted into the scene
    boolean isNew = false;

    /**
     * True, if this branchGroup is directly attached to the locale
     */
    boolean attachedToLocale = false;


    BranchGroupRetained() {
        this.nodeType = NodeRetained.BRANCHGROUP;
    }

    /**
     * This sets the current locale.
     */
    void setLocale(Locale loc) {
         locale = loc;
    }

    /**
     * This gets the current locale
     */
    Locale getLocale() {
         return locale;
    }

    /**
     * Detaches this BranchGroup from its parent.
     */
    void detach() {
	if (universe != null) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
		if (source.isLive()) {
	            notifySceneGraphChanged(true);
		}
	        do_detach();
		universe.setLiveState.clear();
	    }
	    universe.waitForMC();
	} else { // Not live yet, just do it.
	    this.do_detach();
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();
		}
	    }
	}
    }

    // The method that does the work once the lock is acquired.
    void do_detach() {
	if (attachedToLocale) {
	    locale.doRemoveBranchGraph((BranchGroup)this.source, null, 0);
	} else if (parent != null) {
	    GroupRetained g = (GroupRetained)parent;
	    g.doRemoveChild(g.children.indexOf(this), null, 0);
	}
    }

    void setNodeData(SetLiveState s) {
	// super.setNodeData will set branchGroupPaths
	// based on s.parentBranchGroupPaths, we need to
	// set it earlier in order to undo the effect.
	s.parentBranchGroupPaths = branchGroupPaths;

        super.setNodeData(s);

        if (!inSharedGroup) {
            setAuxData(s, 0, 0);
        } else {
            // For inSharedGroup case.
            int j, hkIndex;
            for(j=0; j<s.keys.length; j++) {
                hkIndex = s.keys[j].equals(localToVworldKeys, 0, 
						localToVworldKeys.length);

                if(hkIndex >= 0) {
                    setAuxData(s, j, hkIndex);

                } else {
		    // TODO: change this to an assertion exception
                    System.out.println("Can't Find matching hashKey in setNodeData.");
                    System.out.println("We're in TROUBLE!!!");
                }
            }
        }
    }


    void setAuxData(SetLiveState s, int index, int hkIndex) {
	super.setAuxData(s, index, hkIndex);

	BranchGroupRetained path[] = (BranchGroupRetained[]) 
	    s.branchGroupPaths.get(index);
	BranchGroupRetained clonePath[] =
	    new BranchGroupRetained[path.length+1];
	System.arraycopy(path, 0, clonePath, 0, path.length);
	clonePath[path.length] = this;
	s.branchGroupPaths.set(index, clonePath);
	branchGroupPaths.add(hkIndex, clonePath);
    }


    /**
     * remove the localToVworld transform for this node.
     */
    void removeNodeData(SetLiveState s) {

	if((!inSharedGroup) || (s.keys.length == localToVworld.length)) {
	    // restore to default and avoid calling clear() 
	    // that may clear parent reference branchGroupPaths
	    branchGroupPaths = new ArrayList(1);
	}
	else {
	    int i, index;
	    // Must be in reverse, to preserve right indexing.
	    for (i = s.keys.length-1; i >= 0; i--) {
		index = s.keys[i].equals(localToVworldKeys, 0, localToVworldKeys.length);
		if(index >= 0) {
		    branchGroupPaths.remove(index);
		}
	    }
	    // Set it back to its parent localToVworld data. This is b/c the parent has
	    // changed it localToVworld data arrays.
	}
        super.removeNodeData(s);
    }
    
    void setLive(SetLiveState s) {
	// recursively call child
	super.doSetLive(s);
	super.markAsLive();
    }

   
    // Top level compile call
    void compile()  {

	if (source.isCompiled() || VirtualUniverse.mc.disableCompile)
	    return;

	if (J3dDebug.devPhase && J3dDebug.debug) {
	    J3dDebug.doDebug(J3dDebug.compileState, J3dDebug.LEVEL_3, 
			"BranchGroupRetained.compile()....\n");
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
		System.out.println();
	    }
	}
    }

    void compile(CompileState compState) {
	// if this branch group is previously compiled, don't
	// go any further. Mark the keepTG flag for now. Static
	// transform doesn't go beyond previously compiled group.

	if (mergeFlag == SceneGraphObjectRetained.MERGE_DONE) {
	    compState.keepTG = true;
	    return;
	}

	super.compile(compState);

	// don't remove this node because user can do pickAll on this node
	// without any capabilities set
	mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
    }
}
