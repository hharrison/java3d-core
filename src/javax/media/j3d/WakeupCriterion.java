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
    @Override
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
    @Override
    void cleanTree(BehaviorStructure bs){
	conditionMet = false;
	removeBehaviorCondition(bs);
    };


    /**
     * This goes through the AndOr tree to reset various criterion.
     */
    @Override
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
