/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Class specifying a wakeup the first time an active Viewplatform's
 * activation
 * volume intersects with this object's scheduling region.
 * This gives the behavior an explicit means of executing code when 
 * it is activated.
 */
public final class WakeupOnActivation extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

    /**
     * Constructs a new WakeupOnActivation criterion.
     */
    public WakeupOnActivation() {
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_ACTIVATE_INDEX]++;
	behav.wakeupMask |= BehaviorRetained.WAKEUP_ACTIVATE;
	bs.wakeupOnActivation.add(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_ACTIVATE_INDEX]--;
	if (behav.wakeupArray[BehaviorRetained.WAKEUP_ACTIVATE_INDEX] == 0) {
	    behav.wakeupMask &= ~BehaviorRetained.WAKEUP_ACTIVATE;
	}
	bs.wakeupOnActivation.remove(this);
    }

    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
