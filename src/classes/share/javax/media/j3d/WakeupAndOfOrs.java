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

import java.util.Vector;

/**
 * Class specifying any number of OR wakeup conditions ANDed together.
 * This WakeupCondition object specifies that Java 3D should awaken this 
 * Behavior when all of the WakeupCondition's constituent WakeupOr 
 * conditions become valid.
 * <p>
 * Note that a unique WakeupCriterion object must be used for each
 * individual element in the set of arrays specified by the array of
 * WakeupOr objects.
 */

public final class WakeupAndOfOrs extends WakeupCondition {

    WakeupOr conditions[];
    boolean conditionsMet[];

    /**
     * Constructs a new WakeupAndOfOrs criterion.
     * @param conditions a vector of individual Wakeup conditions
     */
    public WakeupAndOfOrs(WakeupOr conditions[]) {
	this.conditions = new WakeupOr[conditions.length];
	this.conditionsMet = new boolean[conditions.length];

	for(int i = 0; i < conditions.length; i++){
	    this.conditions[i] = conditions[i];
	    // conditionsMet is false by default when it is initilized
	    //      this.conditionsMet[i] = false;
	}
    }
    

    /**
     * This sets the bit for the given child, then checks if the full condition is met
     */
    void setConditionMet(int id, Boolean checkSchedulingRegion) {
	conditionsMet[id] = true;
	
	for (int i=0; i<this.conditionsMet.length; i++) {
	    if (!conditionsMet[i]) {
		return;
	    }
	}
	
	if (parent == null) {
	    super.setConditionMet(this.id, checkSchedulingRegion);
	} else {
	    parent.setConditionMet(this.id, checkSchedulingRegion);
	}
    }
    
    /**
     * This gets called when this condition is added to the AndOr tree.
     */
    void buildTree(WakeupCondition parent, int id, BehaviorRetained b) {
	
	super.buildTree(parent, id, b);
	
	for(int i = 0; i < conditions.length; i++) {
	    if (conditions[i] != null) {
		conditions[i].buildTree(this, i, b);
	    }
	}
    }
    
    /**
     * This goes through the AndOr tree to remove the various criterion from the
     * BehaviorStructure lists
     */
    void cleanTree(BehaviorStructure bs) {
	for (int i=0; i<conditions.length; i++) {
	    conditions[i].cleanTree(bs);
	    conditionsMet[i] = false;
	}
    }

 
    void reInsertElapseTimeCond() {
	super.reInsertElapseTimeCond();
	for(int i = 0; i < conditions.length; i++) {
	    if (conditions[i] != null) {
		conditions[i].reInsertElapseTimeCond();
	    }
	}
    }

   /**
     * This goes through the AndOr tree to remove the various criterion from the 
     * BehaviorStructure.
     */
    void resetTree() {
	super.resetTree();
	for(int i = 0; i < conditions.length; i++) {
	    if (conditions[i] != null) {
		conditions[i].resetTree();
	    }
	}
    }
    
}
