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

import java.util.Vector;

/**
 * Class specifying any number of AND wakeup conditions ORed together.
 * This WakeupCondition object specifies that Java 3D should awaken 
 * this Behavior when any of the WakeupCondition's constituent WakeupAnd
 * conditions becomes valid.
 * <p>
 * Note that a unique WakeupCriterion object must be used for each
 * individual element in the set of arrays specified by the array of
 * WakeupAnd objects.
 */

public final class WakeupOrOfAnds extends WakeupCondition {

    WakeupAnd conditions[];

    /**
     * Constructs a new WakeupOrOfAnds criterion.
     * @param conditions a vector of individual Wakeup conditions
     */
    public WakeupOrOfAnds(WakeupAnd conditions[]) {
	this.conditions = new WakeupAnd[conditions.length];
	for(int i = 0; i < conditions.length; i++){
	    this.conditions[i] = conditions[i];
	}
    }

    
    /**
     * This sets the bit for the given child, then checks if the full condition is met
     */
    void setConditionMet(int id, Boolean checkSchedulingRegion) {
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
