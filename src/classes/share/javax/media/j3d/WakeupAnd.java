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
 * Class specifying any number of wakeup conditions ANDed together.
 * This WakeupCondition object specifies that Java 3D should awaken 
 * this Behavior when all of the WakeupCondition's constituent wakeup 
 * criteria become valid.
 * <p>
 * Note that a unique WakeupCriterion object must be used
 * for each individual element in the array of wakeup criteria.
 */

public final class WakeupAnd extends WakeupCondition {

    WakeupCriterion conditions[];
    boolean conditionsMet[];

    /**
     * Constructs a new WakeupAnd criterion.
     * @param conditions a vector of individual Wakeup conditions
     */
    public WakeupAnd(WakeupCriterion conditions[]) {
	this.conditions = new WakeupCriterion[conditions.length];
	this.conditionsMet = new boolean[conditions.length];

	for(int i = 0; i < conditions.length; i++){
	    this.conditions[i] = conditions[i];
	    // It is false by default when array is initialized.
	    //     this.conditionsMet[i] = false;
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
