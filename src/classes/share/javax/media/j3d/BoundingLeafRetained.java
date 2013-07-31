/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * The BoundingLeaf node defines a bounding region object that can be
 * referenced by other nodes to define a region of influence, an
 * application region, or a scheduling region.
 */
class BoundingLeafRetained extends LeafRetained {
    // Statics used when something in the boundingleaf changes
    static final int REGION_CHANGED          = 0x0001;
    static final Integer REGION_CHANGED_MESSAGE = new Integer(REGION_CHANGED);

    // The bounding region object defined by this node
    Bounds      region = null;


    // For the mirror object, this region is the transformed region
    // (the region of the original bounding leaf object transformed
    // by the cache transform)
    Bounds  transformedRegion = null;

    BoundingLeafRetained mirrorBoundingLeaf;

    // A list of Objects that refer, directly or indirectly, to this
    // bounding leaf object
    ArrayList users = new ArrayList();

    // Target threads to be notified when light changes
    int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
	                J3dThread.UPDATE_RENDER;


    // Target threads for tranform change
    int transformTargetThreads =
    J3dThread.UPDATE_RENDERING_ENVIRONMENT | J3dThread.UPDATE_GEOMETRY;

    BoundingLeafRetained() {
        this.nodeType = NodeRetained.BOUNDINGLEAF;
    }

    void createBoundingLeaf() {
	this.nodeType = NodeRetained.BOUNDINGLEAF;
	mirrorBoundingLeaf = new BoundingLeafRetained();
    }

    /**
     * Initialize the bounding region
     */
    void initRegion(Bounds region) {
	if (region != null) {
	    this.region = (Bounds) region.clone();
	}
	else {
	    this.region = null;
	}
	if (staticTransform != null) {
	    this.region.transform(staticTransform.transform);
	}
    }

    /**
     * Set the bounding region
     */
    void setRegion(Bounds region) {
	initRegion(region);
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = mirrorBoundingLeaf.targetThreads;
	createMessage.type = J3dMessage.BOUNDINGLEAF_CHANGED;
	createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= REGION_CHANGED_MESSAGE;
	if (region != null) {
	    createMessage.args[2] = (Bounds)(region.clone());
	} else {
	    createMessage.args[2] = null;
	}
	createMessage.args[3] = mirrorBoundingLeaf.users.toArray();
	VirtualUniverse.mc.processMessage(createMessage);
    }


    /**
     * Get the bounding region
     */
    Bounds getRegion() {
	Bounds b = null;
	if (this.region != null) {
	    b = (Bounds) this.region.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
	}
	return b;
    }


    @Override
    void setLive(SetLiveState s) {
	super.doSetLive(s);

        if (inBackgroundGroup) {
            throw new
               IllegalSceneGraphException(J3dI18N.getString("BoundingLeafRetained0"));
        }

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("BoundingLeafRetained1"));
	}


        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorBoundingLeaf,
                                                Targets.BLN_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }
	mirrorBoundingLeaf.localToVworld = new Transform3D[1][];
	mirrorBoundingLeaf.localToVworldIndex = new int[1][];
	mirrorBoundingLeaf.localToVworld[0] = this.localToVworld[0];
	mirrorBoundingLeaf.localToVworldIndex[0] = this.localToVworldIndex[0];
	mirrorBoundingLeaf.parent = parent;
	if (region != null) {
	    mirrorBoundingLeaf.region = (Bounds)region.clone();
	    mirrorBoundingLeaf.transformedRegion = (Bounds)region.clone();
	    mirrorBoundingLeaf.transformedRegion.transform(
			mirrorBoundingLeaf.getCurrentLocalToVworld());
	} else {
	    mirrorBoundingLeaf.region = null;
	    mirrorBoundingLeaf.transformedRegion = null;
	}
        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorBoundingLeaf,
                                                Targets.BLN_TARGETS);
        }
	mirrorBoundingLeaf.switchState = s.switchStates.get(0);
	super.markAsLive();
    }


  /** Update the "component" field of the mirror object with the
   *  given "value"
   */
    synchronized void updateImmediateMirrorObject(Object[] objs) {

	int component = ((Integer)objs[1]).intValue();
	Bounds b = ((Bounds)objs[2]);
	Transform3D t;

	if ((component & REGION_CHANGED) != 0) {
	    mirrorBoundingLeaf.region = b;
	    if (b != null) {
		mirrorBoundingLeaf.transformedRegion = (Bounds)b.clone();
		t = mirrorBoundingLeaf.getCurrentLocalToVworld();
		mirrorBoundingLeaf.transformedRegion.transform(b, t);
	    }
	    else {
		mirrorBoundingLeaf.transformedRegion = null;
	    }

	}
    }

    /**
     * Add a user to the list of users.
     * There is no 	if (node.source.isLive()) check since
     * mirror objects are the users of the mirror bounding leaf
     * and they do not have a source.
     */
    synchronized void addUser(LeafRetained node) {
	users.add(node);
	if (node.nodeType == NodeRetained.BACKGROUND ||
	    node.nodeType == NodeRetained.CLIP ||
	    node.nodeType == NodeRetained.ALTERNATEAPPEARANCE ||
	    node instanceof FogRetained ||
	    node instanceof LightRetained) {
	    transformTargetThreads |= J3dThread.UPDATE_RENDER;
	}
	else if (node instanceof BehaviorRetained) {
	    transformTargetThreads |= J3dThread.UPDATE_BEHAVIOR;
	    targetThreads |= J3dThread.UPDATE_BEHAVIOR;
	}
	else if (node instanceof SoundRetained ||
		 node.nodeType == NodeRetained.SOUNDSCAPE) {
	    transformTargetThreads |= J3dThread.UPDATE_SOUND;
	}

    }

    /**
     * Remove user from the list of users.
     * There is no 	if (node.source.isLive()) check since
     * mirror objects are the users of the mirror bounding leaf
     * and they do not have a source.
     */
    synchronized void removeUser(LeafRetained u) {
	int i;
	users.remove(users.indexOf(u));
	// For now reconstruct the transform target threads from scratch
	transformTargetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
	                J3dThread.UPDATE_RENDER;

	for (i =0; i < users.size(); i++) {
	    LeafRetained node = (LeafRetained)users.get(i);
	    if (node.nodeType == NodeRetained.BACKGROUND ||
		node.nodeType == NodeRetained.CLIP ||
		node.nodeType == NodeRetained.ALTERNATEAPPEARANCE ||
		node instanceof FogRetained ||
		node instanceof LightRetained) {
		transformTargetThreads |= J3dThread.UPDATE_RENDER;
	    }
	    else if (node.nodeType == NodeRetained.BEHAVIOR) {
		transformTargetThreads |= J3dThread.UPDATE_BEHAVIOR;
		targetThreads |= J3dThread.UPDATE_BEHAVIOR;
	    }
	    else if (node instanceof SoundRetained ||
		     node.nodeType == NodeRetained.SOUNDSCAPE) {
		transformTargetThreads |= J3dThread.UPDATE_SOUND;
	    }
	}
    }


    // This function is called on the mirror bounding leaf
    void updateImmediateTransformChange() {
	Transform3D t;
	t = getCurrentLocalToVworld();
	if (region != null) {
	    transformedRegion.transform(region, t);
	}
    }

    @Override
    void clearLive(SetLiveState s) {
	super.clearLive();
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorBoundingLeaf,
                                                Targets.BLN_TARGETS);
        }
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorBoundingLeaf,
                                                Targets.BLN_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }
    }

    @Override
    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
	region.transform(xform.transform);
    }

}
