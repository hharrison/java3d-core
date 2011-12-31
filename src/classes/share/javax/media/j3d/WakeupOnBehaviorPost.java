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
 * Class that specifies a Behavior wakeup when a specific behavior object
 * posts a specific event
 */
public final class WakeupOnBehaviorPost extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

    Behavior armingBehavior, triggeringBehavior;
    int post, triggeringPost;


    /**
     * Constructs a new WakeupOnBehaviorPost criterion.  A behavior of null
     * specifies a wakeup from any behavior on the specified postId. A postId
     * of 0 specifies a wakeup on any postId from the specified behavior.
     * A behavior of null AND a postId of 0 specify a wakeup on any postId
     * from any behavior.
     * @param behavior the behavior that must be the source of the post,
     * if behavior == null, then any behavior posting the postId will cause
     * the wakeup.
     * @param postId the postId that will trigger a wakeup if posted by the
     * specified behavior, if postId == 0, then any post by the specified
     * behavior will cause the wakeup.
     */
    public WakeupOnBehaviorPost(Behavior behavior, int postId) {
	this.armingBehavior = behavior;
	this.post = postId;
	triggeringPost = -1;
	triggeringBehavior = null;
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Retrieve the WakeupCriterion's specified postId
     * @return the post id specified in this object's construction.
     */
    public int getPostId(){
	return post;
    }


    /**
     *  Returns the behavior specified in this object's constructor.
     *  @return the arming behavior
     */
    public Behavior getBehavior () {
	return armingBehavior;
    }


    /**
     *  Returns the postId that caused the behavior to wakeup.  If the postId
     *  used to construct this wakeup criterion was not zero, then the
     *  triggering postId will always be equal to the postId used in the
     *  constructor.
     */
    public int getTriggeringPostId() {
	return triggeringPost;
    }


    /**
     *  Returns the behavior that triggered this wakeup.  If the arming
     *  behavior used to construct this object was not null, then the
     *  triggering behavior will be the same as the arming behavior.
     */
    public Behavior getTriggeringBehavior() {
	return triggeringBehavior;
    }


    /**
     * This is a callback from BehaviorStructure. It is
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	bs.wakeupOnBehaviorPost.add(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.wakeupOnBehaviorPost.remove(this);
    }

    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
