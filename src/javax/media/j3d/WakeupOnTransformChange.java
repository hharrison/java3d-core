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
    @Override
    void addBehaviorCondition(BehaviorStructure bs) {
	transform.addCondition(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is
     * used to remove wakeupCondition from behavior structure.
     */
    @Override
    void removeBehaviorCondition(BehaviorStructure bs) {
	transform.removeCondition(this);
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    @Override
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
