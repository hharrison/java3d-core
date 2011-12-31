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
 * Class specifying a wakeup when a specific number of milliseconds
 * have elapsed.
 *
 */
public final class WakeupOnElapsedTime extends WakeupCriterion {

    long wait;

    /**
     * This represents the triggered time
     */
    long triggeredTime;

    /**
     * Constructs a new WakeupOnElapsedTime criterion.
     * @param milliseconds the number of milliseconds to the wakeup.  A value
     * of zero or less will cause an IllegalArgumentException to be thrown.
     */
    public WakeupOnElapsedTime(long milliseconds) {
	if(milliseconds <= 0L)
	    throw new IllegalArgumentException(J3dI18N.getString("WakeupOnElapsedTime0"));
	this.wait = milliseconds;
    }

    /**
     * Retrieve the WakeupCriterion's elapsed time value that was used when
     * constructing this object.
     * @return the elapsed time specified when constructing this object
     */
    public long getElapsedFrameTime(){
	return wait;
    }

    /**
     * This is a callback from BehaviorStructure. It is
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	this.triggeredTime = wait + J3dClock.currentTimeMillis();
	behav.wakeupArray[BehaviorRetained.WAKEUP_TIME_INDEX]++;
	behav.wakeupMask |= BehaviorRetained.WAKEUP_TIME;
	VirtualUniverse.mc.timerThread.add(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	behav.wakeupArray[BehaviorRetained.WAKEUP_TIME_INDEX]--;
	if (behav.wakeupArray[BehaviorRetained.WAKEUP_TIME_INDEX] == 0) {
	    behav.wakeupMask &= ~BehaviorRetained.WAKEUP_TIME;
	}
	VirtualUniverse.mc.timerThread.remove(this);
    }

    /**
     * This is invoked when Behavior processStimulus can't schedule
     * to run because behav.active = false. In this case we must
     * reinsert the wakeupOnElapseTime condition back to the
     * TimerThread wakeup heap
     */
    void reInsertElapseTimeCond() {
	super.reInsertElapseTimeCond();
	this.triggeredTime = wait + J3dClock.currentTimeMillis();
	VirtualUniverse.mc.timerThread.add(this);
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {
	this.triggeredTime = wait + J3dClock.currentTimeMillis();
	VirtualUniverse.mc.timerThread.add(this);
    }
}
