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
import java.util.ArrayList;

/**
 * Class specifying a wakeup when the transform within a specified
 * TransformGroup changes
 */
public final class WakeupOnTransformChange extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

   TransformGroupRetained transform;

  /**
   * Constructs a new WakeupOnTransformChange criterion.
   *
   * @param node the TransformGroup node that will trigger a wakeup if
   * its transform is modified
   */
    public WakeupOnTransformChange(TransformGroup node) {
	this.transform = (TransformGroupRetained)node.retained;
	synchronized (transform) {
	    if (transform.transformChange == null) {
		transform.transformChange = new WakeupIndexedList(1,
					  WakeupOnTransformChange.class,
					  WakeupOnTransformChange.COND_IN_BS_LIST, 
								  transform.universe);
	    }
	}
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Returns the TransformGroup node used in creating this WakeupCriterion
     * @return the TransformGroup used in this criterion's construction
     */
    public TransformGroup getTransformGroup(){
	return (TransformGroup)this.transform.source;
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	transform.addCondition(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	transform.removeCondition(this);
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
