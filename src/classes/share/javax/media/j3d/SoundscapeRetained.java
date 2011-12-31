/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;
import java.util.ArrayList;

/**
 * The SoundscapeRetained object defines all soundscape rendering state
 * as a subclass of a Leaf node.
 */
class SoundscapeRetained extends LeafRetained
{
    static final int ATTRIBUTES_CHANGED		= 0x00001;
    static final int BOUNDING_LEAF_CHANGED	= 0x00002;
    static final int APPLICATION_BOUNDS_CHANGED	= 0x00004;

    /**
     * Soundscape nodes application region
     */
    Bounds             applicationRegion = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * The transformed Application Region
     */
    Bounds transformedRegion = null;

    /**
     * Aural attributes associated with this Soundscape
     */
    AuralAttributesRetained    attributes = null;

    // A bitmask that indicates that the something has changed.
    int isDirty = 0xffff;

    // Target threads to be notified when sound changes
    int targetThreads = J3dThread.UPDATE_SOUND |
                        J3dThread.SOUND_SCHEDULER;


    // Is true, if the mirror light is viewScoped
    boolean isViewScoped = false;

    void dispatchMessage(int dirtyBit, Object argument) {
        // Send message including a integer argument
        J3dMessage createMessage = new J3dMessage();
        createMessage.threads = targetThreads;
        createMessage.type = J3dMessage.SOUNDSCAPE_CHANGED;
        createMessage.universe = universe;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(dirtyBit);
        createMessage.args[2] = new Integer(0);
        createMessage.args[3] = null;
        createMessage.args[4] = argument;
        VirtualUniverse.mc.processMessage(createMessage);
    }


    SoundscapeRetained() {
        super();
        this.nodeType = NodeRetained.SOUNDSCAPE;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }


    /**
     * Set the Soundscape's application region.
     * @param region a region that contains the Soundscape's new application region
     */
    void setApplicationBounds(Bounds region)
    {
        if (region != null) {
            applicationRegion = (Bounds) region.clone();
	    if (staticTransform != null) {
                applicationRegion.transform(staticTransform.transform);
            }
        }
        else {
            applicationRegion = null;
        }
        updateTransformChange();
        this.isDirty |= APPLICATION_BOUNDS_CHANGED;
        dispatchMessage(APPLICATION_BOUNDS_CHANGED, region);

	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Get the Soundscape's application region.
     * @return this Soundscape's application region information
     */
    Bounds getApplicationBounds()
    {
      Bounds b = null;

      if (this.applicationRegion == null)
          return (Bounds)null;
      else {
          b = (Bounds) applicationRegion.clone();
          if (staticTransform != null) {
              Transform3D invTransform = staticTransform.getInvTransform();
              b.transform(invTransform);
          }
          return b;
      }
    }

    /**
     * Set the Soundscape's application region to the specified Leaf node.
     */
    void setApplicationBoundingLeaf(BoundingLeaf region) {
        if (boundingLeaf != null) {
            // Remove the soundscape as users of the original bounding leaf
            boundingLeaf.mirrorBoundingLeaf.removeUser(this);
        }
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
            boundingLeaf.mirrorBoundingLeaf.addUser(this);
	} else {
	    boundingLeaf = null;
	}
        updateTransformChange();
        this.isDirty |= BOUNDING_LEAF_CHANGED;
        dispatchMessage(BOUNDING_LEAF_CHANGED, region);
// QUESTION needed??
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Get the Soundscape's application region
     */
    BoundingLeaf getApplicationBoundingLeaf() {
	if (boundingLeaf != null) {
	    return (BoundingLeaf)boundingLeaf.source;
	} else {
	    return (BoundingLeaf)null;
	}
    }

    /**
     * Set a set of aural attributes for this Soundscape
     * @param attributes aural attributes to be set
     */
    void setAuralAttributes(AuralAttributes attributes)
    {
	if (this.source.isLive()) {
	    if (this.attributes != null) {
		this.attributes.clearLive(refCount);
	    }

	    if (attributes != null) {
		((AuralAttributesRetained)attributes.retained).setLive(inBackgroundGroup, refCount);
	    }
	}

	if (this.attributes != null) {
	    this.attributes.removeUser(this);
	}

	if (attributes != null) {
	    this.attributes = (AuralAttributesRetained)attributes.retained;
	    this.attributes.addUser(this);
	} else {
	    this.attributes = null;
        }

	// copy all fields out of attributes and put into our copy of attributes
        this.isDirty |= ATTRIBUTES_CHANGED;
        dispatchMessage(ATTRIBUTES_CHANGED, attributes);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieve a reference to Aural Attributes
     * @return attributes aural attributes to be returned
     */
    AuralAttributes getAuralAttributes()
    {
	if (attributes != null) {
            return ((AuralAttributes) attributes.source);
	}
        else
            return ((AuralAttributes) null);
    }

/*
// NOTE: OLD CODE
    // The update Object function.
    public synchronized void updateObject() {
	if ((attributes != null) && (attributes.aaDirty)) {
	    if (attributes.mirrorAa == null) {
		attributes.mirrorAa = new AuralAttributesRetained();
	    }
	    attributes.mirrorAa.update(attributes);
	}
    }
*/

    // The update Object function.
    synchronized void updateMirrorObject(Object[] objs) {
        // NOTE: There doesn't seem to be a use for mirror objects since
        //     Soundscapes can't be shared.
        // This method updates the transformed region from either bounding
        // leaf or application bounds.   Bounding leaf takes precidence.
        Transform3D trans = null;
        int component = ((Integer)objs[1]).intValue();
        if ((component & BOUNDING_LEAF_CHANGED) != 0) {
            if (this.boundingLeaf != null) {
                transformedRegion = boundingLeaf.transformedRegion;
            }
            else { // evaluate Application Region if not null
                if (applicationRegion != null) {
                    transformedRegion = (Bounds)applicationRegion.clone();
                    transformedRegion.transform(applicationRegion,
                          getLastLocalToVworld());
                }
                else {
                    transformedRegion = null;
                }
            }
        }
        else if ((component & APPLICATION_BOUNDS_CHANGED) != 0) {
            // application bounds only used when bounding leaf null
            if (boundingLeaf == null) {
                transformedRegion = (Bounds)applicationRegion.clone();
                transformedRegion.transform(applicationRegion,
                                getLastLocalToVworld());
            }
            else {
                transformedRegion = null;
            }
        }
    }

    // The update tranform fields
    synchronized void updateTransformChange() {
            if (boundingLeaf != null) {
                transformedRegion = boundingLeaf.transformedRegion;
            }
            else { // evaluate Application Region if not null
                if (applicationRegion != null) {
                    transformedRegion = applicationRegion.copy(transformedRegion);
                    transformedRegion.transform(applicationRegion,
                              getLastLocalToVworld());
                }
                else {
                    transformedRegion = null;
                }
            }
    }

    void updateBoundingLeaf(long refTime) {
        // This is necessary, if for example, the region
        // changes from sphere to box.
        if (boundingLeaf != null && boundingLeaf.switchState.currentSwitchOn) {
            transformedRegion = boundingLeaf.transformedRegion;
        } else { // evaluate Application Region if not null
            if (applicationRegion != null) {
                transformedRegion = applicationRegion.copy(transformedRegion);
                transformedRegion.transform(applicationRegion,
                                                getLastLocalToVworld());
            } else {
                    transformedRegion = null;
            }
        }
    }

// QUESTION: not needed?
/*
    synchronized void initMirrorObject(SoundscapeRetained ms) {
        GroupRetained group;
        Transform3D trans;
        Bounds region = null;

        if (ms == null)
            return;
}
        ms.isDirty = isDirty;
        ms.setApplicationBounds(getApplicationBounds());
        ms.setApplicationBoundingLeaf(getApplicationBoundingLeaf());
        ms.setAuralAttributes(getAuralAttributes());

// QUESTION: no lineage of mirror node kept??
        ms.sgSound = sgSound;
        ms.key = null;
        ms.mirrorSounds = new SoundscapeRetained[1];
        ms.numMirrorSounds = 0;
        ms.parent = parent;
        ms.transformedRegion = null;
        if (boundingLeaf != null) {
            if (ms.boundingLeaf != null)
                ms.boundingLeaf.removeUser(ms);
            ms.boundingLeaf = boundingLeaf.mirrorBoundingLeaf;
            // Add this mirror object as user
            ms.boundingLeaf.addUser(ms);
            ms.transformedRegion = ms.boundingLeaf.transformedRegion;
        }
        else {
            ms.boundingLeaf = null;
        }

        if (applicationRegion != null) {
            ms.applicationRegion = (Bounds) applicationRegion.clone();
            // Assign region only if bounding leaf is null
            if (ms.transformedRegion == null) {
                ms.transformedRegion = (Bounds) ms.applicationRegion.clone();
                ms.transformedRegion.transform(ms.applicationRegion,
                                    ms.getLastLocalToVworld());
            }

        }
        else {
            ms.applicationRegion = null;
        }
    }
*/

    /**
     * This setLive routine first calls the superclass's method, then
     * it adds itself to the list of soundscapes
     */
    void setLive(SetLiveState s) {
        super.doSetLive(s);

	if (attributes != null) {
	    attributes.setLive(inBackgroundGroup, s.refCount);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.SND_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }
	// If its view Scoped, then add this list
	// to be sent to Sound Structure
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}

	if (inBackgroundGroup) {
	    throw new
                IllegalSceneGraphException(J3dI18N.getString("SoundscapeRetained1"));
        }

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("SoundscapeRetained0"));
	}

        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.SND_TARGETS);
        }
        switchState = (SwitchState)s.switchStates.get(0);
	s.notifyThreads |= (J3dThread.UPDATE_SOUND |
			    J3dThread.SOUND_SCHEDULER);

	super.markAsLive();
    }

    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of lights
     */
    void clearLive(SetLiveState s) {
        super.clearLive(s);
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(this, Targets.SND_TARGETS);
        }

	if (attributes != null) {
	    attributes.clearLive(s.refCount);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(this, Targets.SND_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}
	// If its view Scoped, then add this list
	// to be sent to Sound Structure
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(this);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(this);
	}
	s.notifyThreads |= (J3dThread.UPDATE_SOUND |
			    J3dThread.SOUND_SCHEDULER);
    }

    // Simply pass along to the NodeComponents
    /*
    void compile(CompileState compState) {
	setCompiled();

	if (attributes != null)
	   attributes.compile(compState);
    }
    */

    // This makes this sound look just like the one passed in
    void update(SoundscapeRetained ss) {
        applicationRegion = (Bounds)ss.applicationRegion.clone();
        attributes = ss.attributes;
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
