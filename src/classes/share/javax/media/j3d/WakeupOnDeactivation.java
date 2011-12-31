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
