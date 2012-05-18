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

/**
 * Class specifying a wakeup when an active ViewPlatform no longer
 * intersects the specified boundary.
 */
public final class WakeupOnViewPlatformExit extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;
    static final int BOUNDSEXIT_IN_BS_LIST = 1;

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
     * Constructs a new WakeupOnExit criterion.
     * @param region the region that will trigger a wakeup if a ViewPlatform
     * no longer intersects.
     */
    public WakeupOnViewPlatformExit(Bounds region) {
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
     * view platform exit.
     *
     * @since Java 3D 1.3
     */
    public ViewPlatform getTriggeringViewPlatform() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnViewPlatformExit0"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new IllegalStateException(J3dI18N.getString("WakeupOnViewPlatformExit0"));
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
	behav.wakeupArray[BehaviorRetained.WAKEUP_VP_EXIT_INDEX]++;
	behav.wakeupMask |= BehaviorRetained.WAKEUP_VP_EXIT;
	bs.addVPExitCondition(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.removeVPExitCondition(this);
	behav.wakeupArray[BehaviorRetained.WAKEUP_VP_EXIT_INDEX]--;
	if (behav.wakeupArray[BehaviorRetained.WAKEUP_VP_EXIT_INDEX] == 0) {
	    behav.wakeupMask &= ~BehaviorRetained.WAKEUP_VP_EXIT;
	}
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
