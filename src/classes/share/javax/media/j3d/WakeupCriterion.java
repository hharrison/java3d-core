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
 * An abstract class specifying a singleton wakeup Criterion. This
 * class consists of several subclasses, each of which specifies one
 * particular wakeup criterion, that criterion's associated arguments 
 * (if any), and either a flag that indicates whether this criterion 
 * caused a Behavior object to awaken or a return field containing the 
 * information that caused the Behavior object to awaken.
 * <p>
 * Note that a unique WakeupCriterion object must be used with each instance
 * of a Behavior. Sharing wakeup criteria among different instances of
 * a Behavior is illegal.  Similarly, a unique WakeupCriterion object
 * must be used for each individual element in the set of arrays used
 * to construct WakeupOr, WakeupAnd, WakeupOrOfAnds, and
 * WakeupAndOfOrs objects.
 */

public abstract class WakeupCriterion extends WakeupCondition {

    /**
     * Flag specifying whether this criterion triggered a wakeup
     */
    boolean triggered;

    /**
     * Returns true if this criterion triggered the wakeup.
     * @return true if this criterion triggered the wakeup.
     */
    public boolean hasTriggered(){
	return this.triggered;
    }
  
    /**
     * Set the Criterion's trigger flag to true.
     */
    void setTriggered(){
	this.triggered = true;
	if (this.parent == null) {
	    super.setConditionMet(id, Boolean.TRUE);
	} else {
	    parent.setConditionMet(id, Boolean.TRUE);
	}
    }

    /**
     * Initialize And/Or tree and add criterion to the BehaviourStructure.
     * 
     */
    void buildTree(WakeupCondition parent, int id, BehaviorRetained b) {
	super.buildTree(parent, id, b);
	triggered = false;
	addBehaviorCondition(b.universe.behaviorStructure);
    }


    /**
     * This goes through the AndOr tree to remove the various criterion from the 
     * BehaviorStructure.
     * We can't use  behav.universe.behaviorStructure since behav
     * may reassign to another universe at this time.
     *
     */
    void cleanTree(BehaviorStructure bs){
	conditionMet = false;
	removeBehaviorCondition(bs);
    }; 


    /**
     * This goes through the AndOr tree to reset various criterion. 
     */
    void resetTree() {
	conditionMet = false;
	triggered = false;
	resetBehaviorCondition(behav.universe.behaviorStructure);
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    abstract void addBehaviorCondition(BehaviorStructure bs);


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    abstract void removeBehaviorCondition(BehaviorStructure bs);

    /**
     * It is used reset wakeupCondition when it is reused.
     */
    abstract void resetBehaviorCondition(BehaviorStructure bs);
}
