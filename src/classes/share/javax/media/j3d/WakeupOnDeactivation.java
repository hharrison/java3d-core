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

/**
 * Class specifying a wakeup on first detection of a Viewplatform's
 * activation volume no longer intersecting with this object's scheduling
 * region. This gives the behavior an explicit means of executing code 
 * when it is deactivated.
 */
public final class WakeupOnDeactivation extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

    /**
     * Constructs a new WakeupOnDeactivation criterion.
     */
    public WakeupOnDeactivation() {
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }


    /**
     * Set the Criterion's trigger flag to true.
     * No need to check for scheduling region in this case
     */
    void setTriggered(){
	this.triggered = true;
	if (this.parent == null) {
	    super.setConditionMet(id, Boolean.FALSE);
	} else {
	    parent.setConditionMet(id, Boolean.FALSE);
	}
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_DEACTIVATE_INDEX]++;
	behav.wakeupMask |= BehaviorRetained.WAKEUP_DEACTIVATE;
	bs.wakeupOnDeactivation.add(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_DEACTIVATE_INDEX]--;
	if (behav.wakeupArray[BehaviorRetained.WAKEUP_DEACTIVATE_INDEX] == 0) {
	    behav.wakeupMask &= ~BehaviorRetained.WAKEUP_DEACTIVATE;
	}
	bs.wakeupOnDeactivation.remove(this);
    }
    
    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
