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

import java.util.ArrayList;
import java.util.logging.Level;

class BehaviorScheduler extends J3dThread {

    /**
     * The virtual universe that owns this BehaviorScheduler
     */
    VirtualUniverse univ = null;

    // reference to behaviourStructure processList
    UnorderList processList[];

    // reference to scheduleList;
    IndexedUnorderSet scheduleList;

    // reference to universe.behaviorStructure
    BehaviorStructure behaviorStructure;

    // A count for BehaviorScheduler start/stop
    int stopCount = -1;

    /**
     * These are used for start/stop BehaviorScheduler
     */
    long lastStartTime;
    long lastStopTime;

    // lock to ensure consistency of interval values read
    Object intervalTimeLock = new Object();

    /**
     * Some variables used to name threads correctly
     */
    private static int numInstances = 0;
    private int instanceNum = -1;

    private synchronized int newInstanceNum() {
	return (++numInstances);
    }

    int getInstanceNum() {
	if (instanceNum == -1)
	    instanceNum = newInstanceNum();
	return instanceNum;
    }


    BehaviorScheduler(ThreadGroup t, VirtualUniverse universe) {
	super(t);
	setName("J3D-BehaviorScheduler-" + getInstanceNum());
        this.univ = universe;
	behaviorStructure = universe.behaviorStructure;
	scheduleList = behaviorStructure.scheduleList;
	processList = behaviorStructure.processList;
	type = J3dThread.BEHAVIOR_SCHEDULER;
    }

    void stopBehaviorScheduler(long[] intervalTime) { 

	stopCount = 2;
	VirtualUniverse.mc.sendRunMessage(univ, J3dThread.BEHAVIOR_SCHEDULER);
	while (!userStop ) {
	    MasterControl.threadYield();
	}
	synchronized (intervalTimeLock) {
	    intervalTime[0] = lastStartTime;
	    intervalTime[1] = lastStopTime;
	}
    }

    void startBehaviorScheduler() {
	// don't allow scheduler start until intervalTime is read
	synchronized (intervalTimeLock) {
	    stopCount = -1;
	    userStop = false;
	    VirtualUniverse.mc.setWork();
	}
    }

    void deactivate() {
	active = false;
	if (stopCount >= 0) {
	    userStop = true;
	}
    }

    /**
     * The main loop for the Behavior Scheduler.
     * Main method for firing off vector of satisfied conditions that
     * are contained in the condMet vector.  Method is synchronized 
     * because it is modifying the current wakeup vectors in the
     * clean (emptying out satisfied conditions) and processStimulus 
     * (adding conditions again if wakeupOn called) calls.
     */
    void doWork(long referenceTime) {
	BehaviorRetained arr[];
	UnorderList list;
	int i, size, interval;

	lastStartTime = J3dClock.currentTimeMillis();

	if (stopCount >= 0) {
	    VirtualUniverse.mc.sendRunMessage(univ, J3dThread.BEHAVIOR_SCHEDULER);
	    if (--stopCount == 0) {
		userStop = true;
	    }
	}


	for (interval = 0;
	     interval < BehaviorRetained.NUM_SCHEDULING_INTERVALS;
	     interval++) {

	    list = processList[interval];

	    if (list.isEmpty()) {
		continue;
	    }
	    arr = (BehaviorRetained []) list.toArray(false);

	    size = list.arraySize();

	    for (i = 0; i < size ; i++) {
		BehaviorRetained behavret = arr[i];


		synchronized (behavret) {
		    Behavior behav = (Behavior) behavret.source;

		    if (!behav.isLive() || 
			!behavret.conditionSet ||
			(behavret.wakeupCondition == null)) {
			continue;
		    }

		    if (behavret.wakeupCondition.trigEnum == null) {
			behavret.wakeupCondition.trigEnum =
			    new WakeupCriteriaEnumerator(behavret.wakeupCondition,
							 WakeupCondition.TRIGGERED_ELEMENTS);
		    } else {
			behavret.wakeupCondition.trigEnum.reset(
								behavret.wakeupCondition,
								WakeupCondition.TRIGGERED_ELEMENTS);
		    }

		    // BehaviorRetained now cache the old
		    // wakeupCondition in order to 
		    // reuse it without the heavyweight cleanTree()
		    // behavret.wakeupCondition.cleanTree();

		    behavret.conditionSet = false;
		    WakeupCondition wakeupCond = behavret.wakeupCondition;

		    synchronized (behavret) {
			behavret.inCallback = true;
			univ.inBehavior = true;
			try {
			    behav.processStimulus(wakeupCond.trigEnum);
			}
			catch (RuntimeException e) {
			    // Force behavior condition to be unset
			    // Issue 21: don't call cleanTree here
			    behavret.conditionSet = false;
			    System.err.println("Exception occurred during Behavior execution:");
			    e.printStackTrace();
			}
			catch (Error e) {
			    // Force behavior condition to be unset
                            // Fix for issue 264
			    behavret.conditionSet = false;
			    System.err.println("Error occurred during Behavior execution:");
			    e.printStackTrace();
			}
			univ.inBehavior = false;
			behavret.inCallback = false;
		    }
		    // note that if the behavior wasn't reset, we need to make the
		    // wakeupcondition equal to null
		    if (behavret.conditionSet == false) {
			if (wakeupCond != null) {
			    wakeupCond.cleanTree(behaviorStructure);
			}
			behavret.wakeupCondition = null;
			behavret.active = false;
			scheduleList.remove(behavret);
		    } else {
			behavret.handleLastWakeupOn(wakeupCond,
						    behaviorStructure);
		    }
		}    
	    }
	    list.clear();
	}

	behaviorStructure.handleAWTEvent();
	behaviorStructure.handleBehaviorPost();
	lastStopTime = J3dClock.currentTimeMillis();
	
        if (MasterControl.isStatsLoggable(Level.FINE)) {
            VirtualUniverse.mc.recordTime(MasterControl.TimeType.BEHAVIOR, (lastStopTime-lastStartTime)*1000000);
        }
    }

    void free() {
	behaviorStructure = null;
	getThreadData(null, null).thread = null;
	univ = null;
	for (int i=BehaviorRetained.NUM_SCHEDULING_INTERVALS-1; 
	     i >= 0; i--) {
	    processList[i].clear();
	}
	scheduleList.clear();
    }
}
