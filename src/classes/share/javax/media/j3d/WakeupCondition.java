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

import java.util.Enumeration;

/**
 * An abstract class specifying a single wakeup Condition. This class
 * is extended by the WakeupCriterion, WakeupOr, WakeupAnd,
 * WakeupOrOfAnds, and WakeupAndOfOr classes. A Behavior node hands a
 * WakeupCondition object to the behavior scheduler and the behavior 
 * scheduler hands back an enumeration of that WakeupCondition.
 */

public abstract class WakeupCondition extends Object {

    static final int ALL_ELEMENTS = 0;
    static final int TRIGGERED_ELEMENTS = 1;

    /**
     * This boolean indicates whether this condition has been fully met.
     */
    boolean conditionMet = false;
    
    /**
     * This points to the parent of this criterion in the AndOr tree
     */
    WakeupCondition parent = null;
    
    /**
     * The location of this criterion in the parents array.
     */
    int id;

    /**
     * The BehaviorRetained node that is using this condition
     */
    BehaviorRetained behav = null;
    
    /**
     * This is the allElements enumerator
     */
    WakeupCriteriaEnumerator allEnum = null;

    /**
     * This is the triggeredElements enumerator
     */
    WakeupCriteriaEnumerator trigEnum = null;

    // Use in WakeupIndexedList
    int listIdx[][];

    /**
     * Returns an enumeration of all WakeupCriterias in this Condition.
     */
    public Enumeration allElements() {
	if (allEnum == null) {
	    allEnum = new WakeupCriteriaEnumerator(this, ALL_ELEMENTS);
	} else {
	    allEnum.reset(this, ALL_ELEMENTS);
	}
	return allEnum;
    }
    
    /**
     * Returns an enumeration of all triggered WakeupCriterias in this Condition.
     */
    public Enumeration triggeredElements() {
	if (trigEnum == null) {
	    trigEnum = new WakeupCriteriaEnumerator(this, TRIGGERED_ELEMENTS);
	} else {
	    trigEnum.reset(this, TRIGGERED_ELEMENTS);
	}
	return trigEnum;
    }

    /** 
     * this sets the conditionMet flag.
     */
    void setConditionMet(int id, Boolean checkSchedulingRegion) {

	if (!conditionMet) {
	    conditionMet = true;
	    J3dMessage message = VirtualUniverse.mc.getMessage();
	    message.type = J3dMessage.COND_MET;
	    message.threads = J3dThread.UPDATE_BEHAVIOR;
	    message.universe = behav.universe;
	    message.args[0] = behav;
	    message.args[1] = checkSchedulingRegion;
	    message.args[2] = this;
	    VirtualUniverse.mc.processMessage(message);
	}
    }
    
    /**
     * Initialize And/Or tree and add criterion to the BehaviourStructure
     */
    void buildTree(WakeupCondition parent, int id, BehaviorRetained b){
	this.parent = parent;
	this.behav = b;
	this.id = id;
	conditionMet = false;
    }

    /**
     * This goes through the AndOr tree to remove the various criterion from the 
     * BehaviorStructure.
     * We can't use  behav.universe.behaviorStructure since behav
     * may reassign to another universe at this time.
     */
    void cleanTree(BehaviorStructure bs) {
	conditionMet = false;
    }

    void reInsertElapseTimeCond() {
	conditionMet = false;
    }

    void resetTree() {
	conditionMet = false;
    }
}
