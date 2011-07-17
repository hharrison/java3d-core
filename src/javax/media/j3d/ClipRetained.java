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

import javax.vecmath.*;
import java.util.ArrayList;

/**
 * The Clip leaf node defines the back, or far, clipping distance in
 * the virtual universe.  The front clipping plane is defined in the
 * View object.  If no clip node is in scope of the view platform
 * associated with the current view, then the back clipping plane is
 * also defined by the View.
 * @see View
 */
class ClipRetained extends LeafRetained {

    static final int BOUNDS_CHANGED		= 0x00001;
    static final int BOUNDINGLEAF_CHANGED	= 0x00002;
    static final int BACKDISTANCE_CHANGED	= 0x00004;

    /**
     * Clip's back distance
     */
    double backDistance = 100.0;	

    /**
     * back distance scaled to vworld
     */
    double backDistanceInVworld;

    /**
     * The Boundary object defining the application region.
     */  
    Bounds applicationRegion = null;

    /** 
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * The transformed value of the applicationRegion.
     */
    Bounds transformedRegion = null;

    // This is true when this object is referenced in an immediate mode context
    boolean inImmCtx = false;


    // Target threads to be notified when light changes
    // Note, the rendering env structure only get notified
    // when there is a bounds related change
    static final int targetThreads = J3dThread.UPDATE_RENDER |
                                     J3dThread.UPDATE_RENDERING_ENVIRONMENT;


    // Is true, if the clip is viewScoped
    boolean isViewScoped = false;

    /**
     * Constructs a Clip node with a default color (black).
     */
    ClipRetained () {
        this.nodeType = NodeRetained.CLIP;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    /**
     * initializes the clip's back distance to the specified value.  
     * @param backDistance the new back clipping distance
     */
    final void initBackDistance(double backDistance) {
        this.backDistance = backDistance;
    }


    /**
     * Sets the clip's back distance to the specified value.  
     * @param backDistance the new back clipping distance
     */
    final void setBackDistance(double backDistance) {
        this.backDistance = backDistance;
	sendMessage(BACKDISTANCE_CHANGED, new Double(backDistance), null);
    }

    /**
     * Retrieves the clip's back distance.
     * @return the current back clipping distance
     */
    final double getBackDistance() {
	return backDistance;
    }


   /**
     * Initializes the Clip's application region.
     * @param region a region that contains the Backgound's new application bounds
     */  
    final void initApplicationBounds(Bounds region) {
	if (region != null) {
	    applicationRegion = (Bounds) region.clone();
	} else {
	    applicationRegion = null;
	}
    }

    /**
     * Set the Clip's application region.
     * @param region a region that contains the Clip's new application bounds
     */  
    final void setApplicationBounds(Bounds region) {
	initApplicationBounds(region);
	// Don't send the message if there is a valid boundingleaf
	if (boundingLeaf == null) {
	    sendMessage(BOUNDS_CHANGED, 
			(region != null ? region.clone(): null), null);
	}
    }

    /**  
     * Get the Backgound's application region.
     * @return this Clip's application bounds information
     */  
    final Bounds getApplicationBounds() {
	return (applicationRegion != null ?
		(Bounds) applicationRegion.clone() : null);
    }

   /**
     * Initializes the Clip's application region 
     * to the specified Leaf node.
     */  
    void initApplicationBoundingLeaf(BoundingLeaf region) {
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}
    }

    /**
     * Set the Clip's application region to the specified Leaf node.
     */  
    void setApplicationBoundingLeaf(BoundingLeaf region) {
	if (boundingLeaf != null)
	    boundingLeaf.mirrorBoundingLeaf.removeUser(this);
	    
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	    boundingLeaf.mirrorBoundingLeaf.addUser(this);
	} else {
	    boundingLeaf = null;
	}
	sendMessage(BOUNDINGLEAF_CHANGED, 
		    (boundingLeaf != null ?
		     boundingLeaf.mirrorBoundingLeaf : null),
		    (applicationRegion != null ? applicationRegion.clone() : null));
    }

    /**
     * Get the Clip's application region
     */  
    BoundingLeaf getApplicationBoundingLeaf() {
	return (boundingLeaf != null ?
		(BoundingLeaf)boundingLeaf.source : null);
    }

    /**
     * This sets the immedate mode context flag
     */
    void setInImmCtx(boolean inCtx) {
        inImmCtx = inCtx;
    }
 
    /**
     * This gets the immedate mode context flag
     */
    boolean getInImmCtx() {
        return inImmCtx;
    }

    /**
     * This setLive routine first calls the superclass's method, then
     * it adds itself to the list of lights
     */
    void setLive(SetLiveState s) {
        if (inImmCtx) {
           throw new IllegalSharingException(J3dI18N.getString("ClipRetained0"));
        }

        super.doSetLive(s);

        if (inBackgroundGroup) {
            throw new
               IllegalSceneGraphException(J3dI18N.getString("ClipRetained1"));
        }

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("ClipRetained2"));
	}


	initMirrorObject();
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}
        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.ENV_TARGETS);
        }
        switchState = (SwitchState)s.switchStates.get(0);

        // add this node to the transform target
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }

	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;

	super.markAsLive();
    }
 
    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of lights
     */
    void clearLive(SetLiveState s) {
        super.clearLive(s);
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }


	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.ENV_TARGETS);
        }
    }

    void initMirrorObject() {
	Transform3D lastLocalToVworld = getLastLocalToVworld();

	if (boundingLeaf != null) {
	    transformedRegion = (Bounds)boundingLeaf.mirrorBoundingLeaf.transformedRegion;
	}
	else { // Evaluate applicationRegion if not null
	    if (applicationRegion != null) {
		transformedRegion = (Bounds)applicationRegion.clone();
		transformedRegion.transform(applicationRegion, lastLocalToVworld);
	    }
	    else {
		transformedRegion = null;
	    }
		
	}
	backDistanceInVworld = backDistance * 
	    lastLocalToVworld.getDistanceScale();
    }


    // The update Object function.
    void updateImmediateMirrorObject(Object[] objs) {
	int component = ((Integer)objs[1]).intValue();
	Transform3D trans;
	Transform3D currentLocalToVworld = getCurrentLocalToVworld();

	// Bounds message only sent when boundingleaf is null
	if  ((component & BOUNDS_CHANGED) != 0) {
	    if (objs[2] != null) {
		transformedRegion = ((Bounds) objs[2]).copy(transformedRegion);
		transformedRegion.transform(transformedRegion,
					    currentLocalToVworld);
	    }
	    else {
		transformedRegion = null;
	    }
	}
	else if  ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    if (objs[2] != null) {
		transformedRegion = ((BoundingLeafRetained)objs[2]).transformedRegion;
	    }
	    else { // Evaluate applicationRegion if not null
		Bounds appRegion = (Bounds)objs[3];
		if (appRegion != null) {
		    transformedRegion = ((Bounds)appRegion).copy(transformedRegion);
		    transformedRegion.transform(appRegion,
						currentLocalToVworld);
		}
		else {
		    transformedRegion = null;
		}
			
	    }

	}
	else if ((component & BACKDISTANCE_CHANGED) != 0) {
	    backDistanceInVworld = ((Double)objs[2]).doubleValue() *
		currentLocalToVworld.getDistanceScale();
	}
    }

    /** Note: This routine will only be called on
     * the mirror object - will update the object's
     * cached region and transformed region 
     */

    void updateBoundingLeaf() {
        if (boundingLeaf != null &&
		boundingLeaf.mirrorBoundingLeaf.switchState.currentSwitchOn) {
            transformedRegion =
                        boundingLeaf.mirrorBoundingLeaf.transformedRegion;
        } else { // Evaluate applicationRegion if not null
            if (applicationRegion != null) {
                transformedRegion = applicationRegion.copy(transformedRegion);
                transformedRegion.transform(applicationRegion,
                                            getCurrentLocalToVworld());
            } else {
                transformedRegion = null;
            }
        }
    }

    void updateImmediateTransformChange() {
        // If bounding leaf is null, tranform the bounds object
        if (boundingLeaf == null) {
            if (applicationRegion != null) {
                transformedRegion = (Bounds)applicationRegion.clone();
                transformedRegion.transform(applicationRegion,
                                            getCurrentLocalToVworld());
            }
        }
    }

   final void sendMessage(int attrMask, Object attr, Object attr2) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.type = J3dMessage.CLIP_CHANGED;
        createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = attr2;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        if (applicationRegion != null) {
            applicationRegion.transform(xform.transform);
        }
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	leafList.add(this);
    }
}
