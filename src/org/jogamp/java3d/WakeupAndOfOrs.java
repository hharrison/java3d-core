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

package org.jogamp.java3d;


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
    @Override
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
    @Override
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
    @Override
    void cleanTree(BehaviorStructure bs) {
	for (int i=0; i<conditions.length; i++) {
	    conditions[i].cleanTree(bs);
	    conditionsMet[i] = false;
	}
    }


    @Override
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
    @Override
    void resetTree() {
	super.resetTree();
	for(int i = 0; i < conditions.length; i++) {
	    if (conditions[i] != null) {
		conditions[i].resetTree();
	    }
	}
    }

}
