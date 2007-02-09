/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Class specifying a wakeup when an active ViewPlatform intersects the
 * specified boundary.
 */
public final class WakeupOnViewPlatformEntry extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;
    static final int BOUNDSENTRY_IN_BS_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    Bounds region;

    /**
     * Transformed region
     */
    Bounds transformedRegion;

    /**
     * ViewPlatform that triggered this wakeup condition.
     */
    ViewPlatformRetained triggeredVP;
    
    /**
     * Constructs a new WakeupOnEntry criterion.
     * @param region the region that will trigger a wakeup if a ViewPlatform
     *        intersects.
     */
    public WakeupOnViewPlatformEntry(Bounds region) {
	this.region = (Bounds)region.clone();
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }
    
    /**
     * Returns this object's bounds specification
     * @return the bounds used in constructing this WakeupCriterion.
     */
    public Bounds getBounds() {
	return (Bounds)region.clone();
    }

    /**
     * Retrieves the ViewPlatform node that caused the wakeup.
     *
     * @return the triggering ViewPlatform node
     *
     * @exception IllegalStateException if not called from within 
     * a behavior's processStimulus method that was awoken by a
     * view platform entry.
     *
     * @since Java 3D 1.3
     */
    public ViewPlatform getTriggeringViewPlatform() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnViewPlatformEntry0"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException(J3dI18N.getString("WakeupOnViewPlatformEntry0"));
	    }
	}

	return (triggeredVP != null) ? (ViewPlatform)triggeredVP.source : null;
    }

    /**
     * Update the cached Transfrom Region, call from BehaviorStructure
     * when TRANSFORM_CHANGED message get. Also call from buildTree.
     */
    void updateTransformRegion(BehaviorRetained b) {
	if (transformedRegion != null) {
	    transformedRegion.set(region);
	} else {
	    // region is read only once initialize (since there is no
	    // set method for region). So no need to use cloneWithLock()
	    transformedRegion = (Bounds) region.clone();
	}
	transformedRegion.transform(b.getCurrentLocalToVworld(null));
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	updateTransformRegion(behav);
	behav.wakeupArray[BehaviorRetained.WAKEUP_VP_ENTRY_INDEX]++;
	behav.wakeupMask |= BehaviorRetained.WAKEUP_VP_ENTRY;
	bs.addVPEntryCondition(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_VP_ENTRY_INDEX]--;
	if (behav.wakeupArray[BehaviorRetained.WAKEUP_VP_ENTRY_INDEX] == 0) {
	    behav.wakeupMask &= ~BehaviorRetained.WAKEUP_VP_ENTRY;
	}
	bs.removeVPEntryCondition(this);
    }



    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {
	// updateTransformRegion() is invoked in BehaviorStructure
	// whenever Behavior transform change so there is
	// no need to transform here every time.
    }
}
