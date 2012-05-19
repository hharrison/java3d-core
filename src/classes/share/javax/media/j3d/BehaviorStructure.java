/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.AWTEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A behavior structure is a object that organizes behaviors,
 * wakeup conditions, and other behavior scheduler entities.
 */

class BehaviorStructure extends J3dStructure {

    /**
     * The list of behaviors
     */
    IndexedUnorderSet behaviors;

    /**
     * The list of view platforms
     */
    IndexedUnorderSet viewPlatforms;

    /**
     * An array of schedulable behaviors, use in
     * removeViewPlatform() to go through only active behaviors
     */
    IndexedUnorderSet scheduleList;

    /**
     * An array of process behaviors
     */
    UnorderList processList[] = new UnorderList[BehaviorRetained.NUM_SCHEDULING_INTERVALS];

   /**
     * A bounds used for getting a view platform scheduling BoundingSphere
     */
    //    BoundingSphere tempSphere = new BoundingSphere();
    //    BoundingSphere vpsphere = new BoundingSphere();
    Point3d vpCenter = new Point3d();
    Point3d vpTransCenter = new Point3d();

    /**
     * A list of bounds WakeupOnViewPlatformEntry objects that
     * have seen ViewPlatformEntry
     */
    WakeupIndexedList boundsEntryList;

    /**
     * A list of bounds WakeupOnViewPlatformExit objects that have
     * seen ViewPlatformEntry
     */
    WakeupIndexedList boundsExitList;

    /**
     * A list of WakeupOnSensorEntry objects that have seen a sensor
     */
    WakeupIndexedList currentSensorEntryList;

    /**
     * A list of WakeupOnSensorExit objects that have seen a sensor
     */
    WakeupIndexedList currentSensorExitList;

    /**
     * The lists of the WakeupCriterion objects that the
     * behavior scheduler keeps.
     */
    WakeupIndexedList wakeupOnAWTEvent;
    WakeupIndexedList wakeupOnActivation;
    WakeupIndexedList wakeupOnDeactivation;
    WakeupIndexedList wakeupOnBehaviorPost;
    WakeupIndexedList wakeupOnElapsedFrames;
    WakeupIndexedList wakeupOnViewPlatformEntry;
    WakeupIndexedList wakeupOnViewPlatformExit;
    WakeupIndexedList wakeupOnSensorEntry;
    WakeupIndexedList wakeupOnSensorExit;

    // Temporary array for processTransformChanged()
    UnorderList transformViewPlatformList = new UnorderList(ViewPlatformRetained.class);


    // The number of active wakeup condition in wakeupOnElapsedFrames
    int activeWakeupOnFrameCount = 0;

    // The number of active wakeup condition in wakeupOnSensorEntry/Exit
    int activeWakeupOnSensorCount = 0;

    /**
     * Buffers to hold events when user thread is in processStimulus()
     * while this event is receiving. This avoid any lost of event.
     * We did not remove individual element from the following list
     * (except clear()) so the order is still preserve.
     */
    UnorderList awtEventsBuffer = new UnorderList(AWTEvent.class);

    // Use generic integer array to avoid new Integer() for individual element
    int postIDBuffer[] = new int[10]; // size of default UnorderList
    int clonePostIDBuffer[] = new int[postIDBuffer.length];

    UnorderList behaviorPostBuffer = new UnorderList(Behavior.class);

    // temp values for transformed hotspot used in
    // wakeupOnSensorEntry/ExitupdateSensorsHotspot
    Transform3D sensorTransform = new Transform3D();
    Vector3d sensorLoc = new Vector3d();
    Point3d ptSensorLoc = new Point3d();

    // list of active physical environments
    UnorderList physicalEnvironments = new UnorderList(1, PhysicalEnvironment.class);


    // list of Behavior waiting to be add to behavior list and buildTree()
    UnorderList pendingBehaviors = new UnorderList(BehaviorRetained.class);

    // true if branch detach
    boolean branchDetach = false;

    // This is used to notify WakeupOnAWTEvent re-enable Canvas3D events
    long awtEventTimestamp = 1;

    // used to process transform messages
    boolean transformMsg = false;
    UpdateTargets targets = null;

    BehaviorStructure(VirtualUniverse u) {
	super(u, J3dThread.UPDATE_BEHAVIOR);

	for (int i=BehaviorRetained.NUM_SCHEDULING_INTERVALS-1;
	     i >= 0; i--) {
	    processList[i] = new UnorderList(BehaviorRetained.class);
	}
	behaviors = new IndexedUnorderSet(BehaviorRetained.class,
					  BehaviorRetained.BEHAIVORS_IN_BS_LIST, u);
	viewPlatforms = new IndexedUnorderSet(ViewPlatformRetained.class,
					      ViewPlatformRetained.VP_IN_BS_LIST, u);
	scheduleList = new IndexedUnorderSet(BehaviorRetained.class,
					     BehaviorRetained.SCHEDULE_IN_BS_LIST, u);
	boundsEntryList = new WakeupIndexedList(WakeupOnViewPlatformEntry.class,
						WakeupOnViewPlatformEntry.BOUNDSENTRY_IN_BS_LIST, u);
	boundsExitList = new WakeupIndexedList(WakeupOnViewPlatformExit.class,
					       WakeupOnViewPlatformExit.BOUNDSEXIT_IN_BS_LIST, u);
	currentSensorEntryList = new WakeupIndexedList(WakeupOnSensorEntry.class,
						       WakeupOnSensorEntry.SENSORENTRY_IN_BS_LIST, u);
	currentSensorExitList = new WakeupIndexedList(WakeupOnSensorExit.class,
						       WakeupOnSensorExit.SENSOREXIT_IN_BS_LIST, u);
	wakeupOnAWTEvent = new WakeupIndexedList(WakeupOnAWTEvent.class,
						 WakeupOnAWTEvent.COND_IN_BS_LIST, u);
	wakeupOnActivation = new WakeupIndexedList(WakeupOnActivation.class,
						   WakeupOnActivation.COND_IN_BS_LIST, u);
	wakeupOnDeactivation = new WakeupIndexedList(WakeupOnDeactivation.class,
						     WakeupOnDeactivation.COND_IN_BS_LIST, u);
	wakeupOnBehaviorPost = new WakeupIndexedList(WakeupOnBehaviorPost.class,
						     WakeupOnBehaviorPost.COND_IN_BS_LIST, u);
	wakeupOnElapsedFrames = new WakeupIndexedList(WakeupOnElapsedFrames.class,
						      WakeupOnElapsedFrames.COND_IN_BS_LIST, u);
	wakeupOnViewPlatformEntry = new WakeupIndexedList(WakeupOnViewPlatformEntry.class,
							  WakeupOnViewPlatformEntry.COND_IN_BS_LIST, u);
	wakeupOnViewPlatformExit = new WakeupIndexedList(WakeupOnViewPlatformExit.class,
							 WakeupOnViewPlatformExit.COND_IN_BS_LIST, u);
	wakeupOnSensorEntry = new WakeupIndexedList(WakeupOnSensorEntry.class,
						    WakeupOnSensorEntry.COND_IN_BS_LIST, u);
	wakeupOnSensorExit = new WakeupIndexedList(WakeupOnSensorExit.class,
						   WakeupOnSensorExit.COND_IN_BS_LIST, u);

    }

    void processMessages(long referenceTime) {

	J3dMessage[] messages = getMessages(referenceTime);
	int nMsg = getNumMessage();
	J3dMessage m;

	if (nMsg > 0) {
	    for (int i=0; i<nMsg; i++) {
		m = messages[i];

		switch (m.type) {
		case J3dMessage.TRANSFORM_CHANGED: // Compress Message
		    transformMsg = true;
		    break;
		case J3dMessage.COND_MET:
		    // No need to compress Message since wakeupCondition
		    // will make sure that only one message is sent.
		    processConditionMet((BehaviorRetained) m.args[0],
					(Boolean) m.args[1]);
		    break;
		case J3dMessage.INSERT_NODES:
		    insertNodes((Object[])m.args[0]);
		    break;
		case J3dMessage.REMOVE_NODES:
		    removeNodes(m);
		    break;
		case J3dMessage.BEHAVIOR_ACTIVATE:
		    activateBehaviors();
		    break;
		case J3dMessage.BEHAVIOR_ENABLE:
		    addToScheduleList((BehaviorRetained) m.args[1]);
		    reEvaluateWakeupCount();
		    break;
		case J3dMessage.BEHAVIOR_DISABLE:
		    removeFromScheduleList((BehaviorRetained) m.args[1]);
		    reEvaluateWakeupCount();
		    break;
		case J3dMessage.SCHEDULING_INTERVAL_CHANGED:
		    ((BehaviorRetained) m.args[1]).schedulingInterval
			= ((Integer) m.args[2]).intValue();
		    break;
		case J3dMessage.SWITCH_CHANGED:
		    processSwitchChanged(m);
		    // may need to process dirty switched-on transform
		    if (universe.transformStructure.getLazyUpdate()) {
			transformMsg = true;
		    }
		    break;
		case J3dMessage.BOUNDINGLEAF_CHANGED:
		    processBoundingLeafChanged((Object []) m.args[3],
					       (Bounds) m.args[2]);
		    break;
		case J3dMessage.UPDATE_VIEW:
		    reEvaluatePhysicalEnvironments();
		    ViewPlatform v = ((View)
				      m.args[0]).getViewPlatform();
		    if (v != null) {
			// ViewPlatform may set to null when deactivate()
			processViewPlatformTransform((ViewPlatformRetained) v.retained);
		    }
		    break;
		case J3dMessage.UPDATE_VIEWPLATFORM:
		    ViewPlatformRetained vp = (ViewPlatformRetained) m.args[0];
		    // update cached scheduling region first
		    vp.updateActivationRadius(((Float) m.args[1]).floatValue());
		    // then process the VP transform
		    processViewPlatformTransform(vp);
		    break;
		case J3dMessage.REGION_BOUND_CHANGED:
		    {
			BehaviorRetained behav = (BehaviorRetained) m.args[1];
			behav.updateTransformRegion();
			processBehaviorTransform(behav);
		    }
		    break;
		case J3dMessage.BEHAVIOR_REEVALUATE:
		    {
			BehaviorRetained behav = (BehaviorRetained) m.args[0];
			behav.active = false;
			addToScheduleList(behav);
		    }
		    break;
		}
		m.decRefcount();
	    }

	    if (transformMsg) {
		// get the targets from the transform structure
		targets = universe.transformStructure.getTargetList();

		// process the transform changed for each target
		UnorderList arrList;

		arrList = targets.targetList[Targets.BEH_TARGETS];
		if (arrList != null) {
		    processBehXformChanged(arrList);
		}

		arrList = targets.targetList[Targets.VPF_TARGETS];
		if (arrList != null) {
		    processVpfXformChanged(arrList);
		}

		transformMsg = false;
		targets = null;
	    }
	    Arrays.fill(messages, 0, nMsg, null);
	}

	// wakeup even when message is null since wakeupOnElapsedFrame
	// will wakeup this

	if (activeWakeupOnSensorCount <= 0) {
	    if (activeWakeupOnFrameCount > 0) {
		// Wakeup render thread when there is pending wakeupOnElapsedFrames
		VirtualUniverse.mc.sendRunMessage(universe,
						  J3dThread.BEHAVIOR_SCHEDULER|
						  J3dThread.RENDER_THREAD);

	    } else {
		VirtualUniverse.mc.sendRunMessage(universe,
						      J3dThread.BEHAVIOR_SCHEDULER);
	    }
	} else {
	    checkSensorEntryExit();
	    // we have to invoke checkSensorEntryExit() next time
	    if (activeWakeupOnFrameCount > 0) {
		VirtualUniverse.mc.sendRunMessage(universe,
						  J3dThread.UPDATE_BEHAVIOR|
						  J3dThread.BEHAVIOR_SCHEDULER|
						  J3dThread.RENDER_THREAD);

		} else {
		    VirtualUniverse.mc.sendRunMessage(universe,
						      J3dThread.UPDATE_BEHAVIOR|
						      J3dThread.BEHAVIOR_SCHEDULER);
		}
	}
    }

    void insertNodes(Object[] nodes) {
	for (int i=0; i<nodes.length; i++) {
		Object node = nodes[i];

	    if (node instanceof BehaviorRetained) {
		pendingBehaviors.add(node);
	    }
	    else if (node instanceof ViewPlatformRetained) {
		addViewPlatform((ViewPlatformRetained) node);
	    }
	}
    }

    void activateBehaviors() {
	BehaviorRetained behav;
	BehaviorRetained behavArr[] = (BehaviorRetained [])
	                            pendingBehaviors.toArray(false);

	for (int i=pendingBehaviors.arraySize()-1; i>=0; i--) {
	    behav = behavArr[i];
	    behav.wakeupCondition = behav.newWakeupCondition;
	    if (behav.wakeupCondition != null) {
		behav.wakeupCondition.buildTree(null, 0, behav);
		behav.conditionSet = true;
		behaviors.add(behav);
		behav.updateTransformRegion();
		addToScheduleList(behav);
	    }
	}
	pendingBehaviors.clear();
    }

    void addViewPlatform(ViewPlatformRetained vp) {
	int i;
	BehaviorRetained behavArr[] = (BehaviorRetained []) behaviors.toArray(false);

	viewPlatforms.add(vp);
	vp.updateTransformRegion();

	if (!vp.isActiveViewPlatform()) {
	    return;
	}

	// re-evaulate all behaviors to see if we need to put
	// more behaviors in scheduleList

	for (i=behaviors.arraySize()-1; i>=0; i--) {
	    addToScheduleList(behavArr[i]);
	}

	// handle ViewPlatform Entry
	WakeupOnViewPlatformEntry wakeupOnViewPlatformEntryArr[] =
	    (WakeupOnViewPlatformEntry []) wakeupOnViewPlatformEntry.toArray(false);
	WakeupOnViewPlatformEntry wentry;

	for (i=wakeupOnViewPlatformEntry.arraySize()-1; i >=0; i--) {
	    wentry = wakeupOnViewPlatformEntryArr[i];
	    if (!boundsEntryList.contains(wentry) &&
		wentry.transformedRegion.intersect(vp.center)) {
		boundsEntryList.add(wentry);
		wentry.triggeredVP = vp;
		wentry.setTriggered();
	    }
	}

	// handle ViewPlatform Exit
	WakeupOnViewPlatformExit wakeupOnViewPlatformExitArr[] =
	    (WakeupOnViewPlatformExit []) wakeupOnViewPlatformExit.toArray(false);
	WakeupOnViewPlatformExit wexit;

	for (i=wakeupOnViewPlatformExit.arraySize()-1; i >=0; i--) {
	    wexit = wakeupOnViewPlatformExitArr[i];
	    if (!boundsExitList.contains(wexit) &&
		wexit.transformedRegion.intersect(vp.center)) {
		wexit.triggeredVP = vp;
		boundsExitList.add(wexit);
	    }
	}

    }

    void removeNodes(J3dMessage m) {
	Object[] nodes = (Object[]) m.args[0];
	boolean behavRemove = false;

	for (int i=0; i<nodes.length; i++) {
	    Object node = nodes[i];
	    if (node instanceof BehaviorRetained) {
		behavRemove = true;
		removeBehavior((BehaviorRetained) node);
	    }
	    else if (node instanceof ViewPlatformRetained) {
		removeViewPlatform((ViewPlatformRetained) node);
	    }
	}

	// Since BehaviorScheduler will run after BehaviorStructure
	// (not in parallel). It is safe to do cleanup here.
	wakeupOnAWTEvent.clearMirror();
	awtEventsBuffer.clearMirror();
	wakeupOnBehaviorPost.clearMirror();
	behaviorPostBuffer.clearMirror();
	wakeupOnSensorEntry.clearMirror();
	wakeupOnSensorExit.clearMirror();
	branchDetach = true;

	if (behavRemove) {
	    // disable AWT Event from Canvas3D
	    WakeupOnAWTEvent awtConds[] = (WakeupOnAWTEvent [])
		wakeupOnAWTEvent.toArray();
	    int eventSize = wakeupOnAWTEvent.arraySize();

	    // Component Event always Enable
	    boolean focusEnable = false;
	    boolean keyEnable = false;
	    boolean mouseMotionEnable = false;
	    boolean mouseEnable = false;
	    boolean mouseWheelEnable = false;
	    WakeupOnAWTEvent awtCond;
	    int awtId;
	    long eventMask;
	    boolean incTimestamp = false;

	    for (int i=0; i < eventSize; i++) {
		awtCond = awtConds[i];
		awtId = awtCond.AwtId;
		eventMask = awtCond.EventMask;

		if ((awtId >= FocusEvent.FOCUS_FIRST && awtId <= FocusEvent.FOCUS_LAST) ||
		    (eventMask & AWTEvent.FOCUS_EVENT_MASK) != 0) {
		    focusEnable = true;
		}
		if ((awtId >= KeyEvent.KEY_FIRST && awtId <= KeyEvent.KEY_LAST) ||
		    (eventMask & AWTEvent.KEY_EVENT_MASK) != 0) {
		    keyEnable = true;
		}
		if ((awtId >= MouseEvent.MOUSE_FIRST) &&
		    (awtId <= MouseEvent.MOUSE_LAST)) {
		    if ((awtId == MouseEvent.MOUSE_DRAGGED) ||
			(awtId == MouseEvent.MOUSE_MOVED)) {
			mouseMotionEnable = true;
		    }
		    else if ((awtId == MouseEvent.MOUSE_ENTERED) ||
			     (awtId == MouseEvent.MOUSE_EXITED)  ||
			     (awtId == MouseEvent.MOUSE_CLICKED) ||
			     (awtId == MouseEvent.MOUSE_PRESSED) ||
			     (awtId == MouseEvent.MOUSE_RELEASED) ) {
			mouseEnable = true;
		    }
		    else if (awtId == MouseEvent.MOUSE_WHEEL) {
			mouseWheelEnable = true;
		    }
		} else {
		    if ((eventMask & AWTEvent.MOUSE_EVENT_MASK) != 0) {
			mouseEnable = true;
		    }
		    if ((eventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0) {
			mouseMotionEnable = true;
		    }
		    if ((eventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0) {
			mouseWheelEnable = true;
		    }
		}
	    }

	    if (!focusEnable && universe.enableFocus) {
		incTimestamp = true;
		universe.disableFocusEvents();
	    }
	    if (!keyEnable && universe.enableKey) {
		// key event use for toggle to fullscreen/window mode
		incTimestamp = true;
		universe.disableKeyEvents();
	    }
	    if (!mouseWheelEnable && universe.enableMouseWheel) {
		incTimestamp = true;
		universe.disableMouseWheelEvents();
	    }
	    if (!mouseMotionEnable && universe.enableMouseMotion) {
		incTimestamp = true;
		universe.disableMouseMotionEvents();
	    }
	    if (!mouseEnable && universe.enableMouse) {
		incTimestamp = true;
		universe.disableMouseEvents();
	    }
	    if (incTimestamp) {
		awtEventTimestamp++;
	    }
	}
    }

    void removeViewPlatform(ViewPlatformRetained vp) {
	BehaviorRetained behav;
	int i;

	viewPlatforms.remove(vp);

	BehaviorRetained scheduleArr[] = (BehaviorRetained [])
	                                   scheduleList.toArray(false);

	// handle Deactive
	for (i=scheduleList.arraySize()-1; i >=0 ; i--) {
	    behav = scheduleArr[i];
	    // This vp may contribute to the reason that
	    // behavior is in schedule list
	    if (!intersectVPRegion(behav.transformedRegion)) {
		removeFromScheduleList(behav);
	    }
	}

	// handle ViewPlatform Entry
	WakeupOnViewPlatformEntry boundsEntryArr[] =
	    (WakeupOnViewPlatformEntry []) boundsEntryList.toArray(false);
	WakeupOnViewPlatformEntry wentry;
	ViewPlatformRetained triggeredVP;

	for (i=boundsEntryList.arraySize()-1; i >=0; i--) {
	    wentry = boundsEntryArr[i];
	    // only this thread can modify wentry.transformedRegion, so
	    // no need to getWithLock()
	    triggeredVP = intersectVPCenter(wentry.transformedRegion);
	    if (triggeredVP == null) {
		boundsEntryList.remove(wentry);
	    }
	}

	// handle ViewPlatform Exit
	WakeupOnViewPlatformExit boundsExitArr[] =
	    (WakeupOnViewPlatformExit []) boundsExitList.toArray(false);
	WakeupOnViewPlatformExit wexit;

	for (i=boundsExitList.arraySize()-1; i >=0; i--) {
	    wexit = boundsExitArr[i];
	    // only this thread can modify wentry.transformedRegion, so
	    // no need to getWithLock()
	    triggeredVP = intersectVPCenter(wexit.transformedRegion);
	    if (triggeredVP == null) {
		boundsExitList.remove(wexit);
		wexit.setTriggered();
	    }
	}
    }

    void removeBehavior(BehaviorRetained behav) {
	behaviors.remove(behav);

	if ((behav.wakeupCondition != null) &&
	    (behav.wakeupCondition.behav != null)) {
	    behav.wakeupCondition.cleanTree(this);
	    if (behav.universe == universe) {
		behav.conditionSet = false;
	    }
	}

	// cleanup  boundsEntryList
        // since we didn't remove it on removeVPEntryCondition
	WakeupOnViewPlatformEntry boundsEntryArr[] =
	    (WakeupOnViewPlatformEntry []) boundsEntryList.toArray(false);
	WakeupOnViewPlatformEntry wentry;

	for (int i=boundsEntryList.arraySize()-1; i>=0; i--) {
	    wentry = boundsEntryArr[i];
	    if (wentry.behav == behav) {
		boundsEntryList.remove(wentry);
	    }
	}

	// cleanup  boundsExitList
        // since we didn't remove it on removeVPExitCondition
	WakeupOnViewPlatformExit boundsExitArr[] =
	    (WakeupOnViewPlatformExit []) boundsExitList.toArray(false);
	WakeupOnViewPlatformExit wexit;

	for (int i=boundsExitList.arraySize()-1; i>=0; i--) {
	    wexit = boundsExitArr[i];
	    if (wexit.behav == behav) {
		boundsExitList.remove(wexit);
	    }
	}


	// cleanup currentSensorEntryList
	// since we didn't remove it on removeSensorEntryCondition
	WakeupOnSensorEntry currentSensorEntryArr[] =
	    (WakeupOnSensorEntry []) currentSensorEntryList.toArray(false);
	WakeupOnSensorEntry sentry;

	for (int i=currentSensorEntryList.arraySize()-1; i>=0; i--) {
	    sentry = currentSensorEntryArr[i];
	    if (sentry.behav == behav) {
		currentSensorEntryList.remove(sentry);
	    }
	}


	// cleanup currentSensorExitList
	// since we didn't remove it on removeSensorExitCondition
	WakeupOnSensorExit currentSensorExitArr[] =
	    (WakeupOnSensorExit []) currentSensorExitList.toArray(false);
	WakeupOnSensorExit sexit;

	for (int i=currentSensorExitList.arraySize()-1; i>=0; i--) {
	    sexit = currentSensorExitArr[i];
	    if (sexit.behav == behav) {
		currentSensorExitList.remove(sexit);
	    }
	}
	removeFromScheduleList(behav);

    }


    void handleAWTEvent(AWTEvent evt) {
	awtEventsBuffer.add(evt);
	VirtualUniverse.mc.sendRunMessage(universe,
					  J3dThread.BEHAVIOR_SCHEDULER);
    }

   /**
     * This routine takes the awt event list and gives then to the awt event
     * conditions
     */
    void handleAWTEvent() {
	WakeupOnAWTEvent awtConds[] = (WakeupOnAWTEvent [])
	                                   wakeupOnAWTEvent.toArray();
	AWTEvent events[];
	int eventSize = wakeupOnAWTEvent.arraySize();
	int awtBufferSize;

	synchronized (awtEventsBuffer) {
	    events = (AWTEvent []) awtEventsBuffer.toArray();
	    awtBufferSize = awtEventsBuffer.size();
	    awtEventsBuffer.clear();
	}
	WakeupOnAWTEvent awtCond;
	AWTEvent evt;
	int id;

	for (int i=0; i < eventSize; i++) {
	    awtCond = awtConds[i];
	    for (int j=0; j < awtBufferSize; j++) {
		evt = events[j];
		id = evt.getID();

		if (awtCond.AwtId != 0) {
		    if (awtCond.AwtId == id) {
			// XXXX: how do we clone this event (do we need to?)
			// Bug: 4181321
			awtCond.addAWTEvent(evt);
		    }
		} else {
		    if (id >= ComponentEvent.COMPONENT_FIRST &&
			id <= ComponentEvent.COMPONENT_LAST &&
			(awtCond.EventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0) {
		        awtCond.addAWTEvent(evt);
		    }
		    else if (id >= FocusEvent.FOCUS_FIRST &&
			     id <= FocusEvent.FOCUS_LAST &&
			     (awtCond.EventMask & AWTEvent.FOCUS_EVENT_MASK) != 0) {
			awtCond.addAWTEvent(evt);
		    }
		    else if (id >= KeyEvent.KEY_FIRST &&
			     id <= KeyEvent.KEY_LAST &&
			     (awtCond.EventMask & AWTEvent.KEY_EVENT_MASK) != 0) {
			awtCond.addAWTEvent(evt);
		    }
		    else if ((id == MouseEvent.MOUSE_CLICKED ||
			      id == MouseEvent.MOUSE_ENTERED ||
			      id == MouseEvent.MOUSE_EXITED ||
			      id == MouseEvent.MOUSE_PRESSED ||
			      id == MouseEvent.MOUSE_RELEASED) &&
			     (awtCond.EventMask & AWTEvent.MOUSE_EVENT_MASK) != 0) {
			awtCond.addAWTEvent(evt);
		    }
		    else if ((id == MouseEvent.MOUSE_DRAGGED ||
			      id == MouseEvent.MOUSE_MOVED) &&
			     (awtCond.EventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0) {
			awtCond.addAWTEvent(evt);
		    }
		    else if ((id == MouseEvent.MOUSE_WHEEL) &&
			     (awtCond.EventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0) {
			awtCond.addAWTEvent(evt);
		    }
		}
	    }
	}



    }


    void handleBehaviorPost(Behavior behav, int postid) {

	synchronized (behaviorPostBuffer) {
	    int size = behaviorPostBuffer.size();
	    if (postIDBuffer.length == size) {
		int oldbuffer[] = postIDBuffer;
		postIDBuffer = new int[size << 1];
		System.arraycopy(oldbuffer, 0, postIDBuffer, 0, size);
	    }
	    postIDBuffer[size] = postid;
	    behaviorPostBuffer.add(behav);
	}
	VirtualUniverse.mc.sendRunMessage(universe, J3dThread.BEHAVIOR_SCHEDULER);
    }

   /**
     * This goes through all of the criteria waiting for Behavior Posts
     * and notifys them.
     */
    void handleBehaviorPost() {
	Behavior behav;
	int postid;
        WakeupOnBehaviorPost wakeup;
	WakeupOnBehaviorPost wakeupConds[] = (WakeupOnBehaviorPost [])
	                                    wakeupOnBehaviorPost.toArray();
	Behavior behavArr[];
	int behavBufferSize;

	synchronized (behaviorPostBuffer) {
	    behavArr = (Behavior []) behaviorPostBuffer.toArray();
	    behavBufferSize = behaviorPostBuffer.size();
	    if (clonePostIDBuffer.length < behavBufferSize) {
		clonePostIDBuffer = new int[behavBufferSize];
	    }
	    System.arraycopy(postIDBuffer, 0, clonePostIDBuffer, 0,
			     behavBufferSize);
	    behaviorPostBuffer.clear();
	}

	int size = wakeupOnBehaviorPost.arraySize();
	for (int i=0; i < size; i++) {
	    wakeup = wakeupConds[i];
	    for (int j=0; j < behavBufferSize; j++) {
		behav = behavArr[j];
		postid = clonePostIDBuffer[j];
		if ((wakeup.post == postid || wakeup.post == 0) &&
		    (behav == wakeup.armingBehavior || wakeup.armingBehavior == null)) {
		    wakeup.triggeringBehavior = behav;
		    wakeup.triggeringPost = postid;
		    wakeup.setTriggered();
		}
	    }
	}

    }

    /**
     * This goes through all of the criteria waiting for Elapsed Frames
     * and notified them.
     */
    void incElapsedFrames() {

	WakeupOnElapsedFrames wakeupConds[] = (WakeupOnElapsedFrames [])
	                          wakeupOnElapsedFrames.toArray(true);
	int size = wakeupOnElapsedFrames.arraySize();
	int i = 0;

	while (i < size) {
             wakeupConds[i++].newFrame();
        }

	if ( size > 0) {
	    VirtualUniverse.mc.sendRunMessage(universe,
		      J3dThread.BEHAVIOR_SCHEDULER|J3dThread.UPDATE_BEHAVIOR);
	}

	if (branchDetach) {
	    // Since this procedure may call by immediate mode user
	    // thread, we can't just clear it in removeNodes()
	    wakeupOnElapsedFrames.clearMirror();
	    branchDetach = false;
	}

    }

    void removeVPEntryCondition(WakeupCondition w) {
	wakeupOnViewPlatformEntry.remove(w);
	// don't remove boundsEntryList, it is use next time
	// when addVPExitCondition invoke to determine whether to
	// trigger an event or not.

    }

    void addVPEntryCondition(WakeupOnViewPlatformEntry w) {
	boolean needTrigger = true;

	// see if the matching wakeupOnViewPlatformEntry
	// condition exists & do cleanup
	WakeupOnViewPlatformEntry boundsEntryArr[] =
	    (WakeupOnViewPlatformEntry []) boundsEntryList.toArray(false);
	WakeupOnViewPlatformEntry wentry;

	for (int i=boundsEntryList.arraySize()-1; i>=0; i--) {
	    wentry = boundsEntryArr[i];
	    if ((wentry.behav == w.behav) &&
		(wentry.region.equals(w.region))) {
		boundsEntryList.remove(i);
		// Case where we wakeOr() both condition together.
		// we should avoid calling setTrigger() every time.
		needTrigger = false;
		break;
	    }
	}

	wakeupOnViewPlatformEntry.add(w);

	ViewPlatformRetained triggeredVP = intersectVPCenter(w.transformedRegion);
	if (triggeredVP != null) {
	    boundsEntryList.add(w);
	}

	// we always trigger bound is inside during initialize
	if (needTrigger && (triggeredVP != null)) {
	    w.triggeredVP = triggeredVP;
	    w.setTriggered();
	}
    }

    void removeVPExitCondition(WakeupOnViewPlatformExit w) {
	wakeupOnViewPlatformExit.remove(w);
	// don't remove boundsExitList, it is use next time
	// when addVPEntryCondition invoke to determine whether to
	// trigger an event or not.
    }

    void addVPExitCondition(WakeupOnViewPlatformExit w) {
	// Cleanup, since collideEntryList did not remove
	// its condition in removeVPEntryCondition
	boolean needTrigger = true;
	WakeupOnViewPlatformExit boundsExitArr[] =
	    (WakeupOnViewPlatformExit []) boundsExitList.toArray(false);
	WakeupOnViewPlatformExit wexit;
	for (int i=boundsExitList.arraySize()-1; i>=0; i--) {
	    wexit = boundsExitArr[i];
	    if ((wexit.behav == w.behav) &&
		(wexit.region.equals(w.region))) {
		boundsExitList.remove(i);
		needTrigger = false;
		break;
	    }
	}

	ViewPlatformRetained triggeredVP = intersectVPCenter(w.transformedRegion);
	wakeupOnViewPlatformExit.add(w);

	if (triggeredVP != null) {
	    w.triggeredVP = triggeredVP;
	    boundsExitList.add(w);
	}

	if (!needTrigger) {
	    return;
	}

	// see if the matching wakeupOnViewPlatformEntry
	// condition exists

	WakeupOnViewPlatformEntry boundsEntryArr[] =
	    (WakeupOnViewPlatformEntry []) boundsEntryList.toArray(false);
	WakeupOnViewPlatformEntry wentry;

	for (int i=boundsEntryList.arraySize()-1; i>=0; i--) {
	    wentry = boundsEntryArr[i];
	    if ((wentry.behav == w.behav) &&
		(wentry.region.equals(w.region))) {
		// Don't remove this since if user wakeupOr()
		// Entry and Exit condition together we may have trouble
		//		boundsEntryList.remove(i);
		if (triggeredVP == null) {
		    w.setTriggered();
		}
		break;
	    }
	}

    }


    void removeSensorEntryCondition(WakeupOnSensorEntry w) {
	wakeupOnSensorEntry.remove(w);
	// don't remove currentSensorEntryList, it is use next time
	// when addSensorExitCondition invoke to determine whether to
	// trigger an event or not.
    }

    void addSensorEntryCondition(WakeupOnSensorEntry w) {
	boolean needTrigger = true;

	// see if the matching wakeupOnSensorEntry
	// condition exists
	WakeupOnSensorEntry sensorEntryArr[] =
	    (WakeupOnSensorEntry []) currentSensorEntryList.toArray(false);
	WakeupOnSensorEntry wentry;

	for (int i=currentSensorEntryList.arraySize()-1; i>=0; i--) {
	    wentry = sensorEntryArr[i];
	    if ((wentry.behav == w.behav) &&
		(wentry.region.equals(w.region))) {
		currentSensorEntryList.remove(i);
		needTrigger = false;
		break;
	    }
	}

	wakeupOnSensorEntry.add(w);

	w.updateTransformRegion();
	Sensor target = sensorIntersect(w.transformedRegion);
	if (target != null) {
	    w.setTarget(target);
	    currentSensorEntryList.add(w);
	}

	if (needTrigger && (target != null)) {
	    w.setTriggered();

	}
	VirtualUniverse.mc.sendRunMessage(universe,
					  J3dThread.UPDATE_BEHAVIOR);
    }

    void removeSensorExitCondition(WakeupOnSensorExit w) {
	wakeupOnSensorExit.remove(w);
	// don't remove currentSensorExitList, it is use next time
	// when addSensorEntryCondition invoke to determine whether to
	// trigger an event or not
    }

    void addSensorExitCondition(WakeupOnSensorExit w) {
	// Cleanup
	boolean needTrigger = true;

	WakeupOnSensorExit currentSensorExitArr[] =
	    (WakeupOnSensorExit []) currentSensorExitList.toArray(false);
	WakeupOnSensorExit wexit;
	for (int i=currentSensorExitList.arraySize()-1; i>=0; i--) {
	    wexit = currentSensorExitArr[i];
	    if ((wexit.behav == w.behav) &&
		(wexit.region.equals(w.region))) {
		currentSensorExitList.remove(i);
		needTrigger = false;
		break;
	    }
	}

	w.updateTransformRegion();
	Sensor target = sensorIntersect(w.transformedRegion);
	wakeupOnSensorExit.add(w);

	if (target != null) {
	    w.setTarget(target);
	    currentSensorExitList.add(w);
	}

	if (!needTrigger) {
	    return;
	}
	// see if the matching wakeupOnSensorEntry
	// condition exists
	WakeupOnSensorEntry sensorEntryArr[] =
	    (WakeupOnSensorEntry []) currentSensorEntryList.toArray(false);
	WakeupOnSensorEntry wentry;

	for (int i=currentSensorEntryList.arraySize()-1; i>=0; i--) {
	    wentry = sensorEntryArr[i];
	    if ((wentry.behav == w.behav) &&
		(wentry.region.equals(w.region))) {
		// No need to invoke currentSensorEntryList.remove(i);
		if (target == null) {
		    w.setTriggered();
		}
		break;
	    }
	}
	VirtualUniverse.mc.sendRunMessage(universe,
					  J3dThread.UPDATE_BEHAVIOR);
    }

    void processConditionMet(BehaviorRetained behav,
			     Boolean checkSchedulingRegion) {

	// Since we reuse wakeup condition, the old wakeupCondition
	// will not reactivate again while processStimulus is running
	// which may set another wakeupCondition.
	// Previously we don't reuse wakeupCondition and cleanTree()
	// everytime before calling processStimulus() so the flag
	// inCallback is not necessary to check.
	if (!behav.inCallback &&
	    ((checkSchedulingRegion == Boolean.FALSE) ||
	     behav.active))  {
	    processList[behav.schedulingInterval].add(behav);
	} else {
	    if (((behav.wakeupMask &
		  BehaviorRetained.WAKEUP_TIME) !=  0) &&
		(behav.source != null) &&
		(behav.source.isLive()) &&
		(behav.wakeupCondition != null)) {
		// need to add back wakeupOnElapsedTime condition
		// to TimerThread
		behav.wakeupCondition.reInsertElapseTimeCond();
	    }
	}
    }

    final void processBehXformChanged(UnorderList arrList) {
	BehaviorRetained beh;
	Object[] nodes, nodesArr;

	int size = arrList.size();
        nodesArr = arrList.toArray(false);

	for (int i = 0; i < size; i++) {
            nodes = (Object[])nodesArr[i];
	    for (int j=0; j<nodes.length; j++) {
	        beh = (BehaviorRetained)nodes[j];
                beh.updateTransformRegion();
                processBehaviorTransform(beh);
	    }
	}
    }

    final void processVpfXformChanged(UnorderList arrList) {
	Object[] nodes, nodesArr;

	int size = arrList.size();
        nodesArr = arrList.toArray(false);

	for (int i = 0; i < size; i++) {
            nodes = (Object[])nodesArr[i];
	    for (int j=0; j<nodes.length; j++) {
                processViewPlatformTransform((ViewPlatformRetained)nodes[j]);
	    }
	}
    }

    final void processTransformChanged(Object leaf[]) {
	Object node;
	int i;

	// We have to process them in group rather then one by one,
	// otherwise we may have both activation/deactivation
	// conditions wakeup at the same time when both ViewPlatform
	// and Behavior transform under a branch.

	// Update transformRegion first
	for (i=0; i < leaf.length; i++) {
	    node = leaf[i];
	    if (node instanceof BehaviorRetained) {
		((BehaviorRetained) node).updateTransformRegion();
		processBehaviorTransform((BehaviorRetained) node);

	    } else if (node instanceof ViewPlatformRetained) {
		((ViewPlatformRetained) node).updateTransformRegion();
		transformViewPlatformList.add(node);
	    }
	}

	// finally handle ViewPlatformRetained Transform change
	if (transformViewPlatformList.size() > 0) {
	    ViewPlatformRetained vpArr[] = (ViewPlatformRetained [])
		                     transformViewPlatformList.toArray(false);

	    int size = transformViewPlatformList.arraySize();
	    for (i=0; i < size; i++) {
			processViewPlatformTransform(vpArr[i]);
	    }
	    transformViewPlatformList.clear();
	}
    }


    // assume behav.updateTransformRegion() invoke before
    final void processBehaviorTransform(BehaviorRetained behav) {
	if ((behav.wakeupMask & BehaviorRetained.WAKEUP_VP_ENTRY) != 0) {
	    updateVPEntryTransformRegion(behav);
	}

	if ((behav.wakeupMask & BehaviorRetained.WAKEUP_VP_EXIT) != 0) {
	    updateVPExitTransformRegion(behav);
	}

	if (behav.active) {
	    if (!intersectVPRegion(behav.transformedRegion)) {
		removeFromScheduleList(behav);
	    }
	} else {
	    addToScheduleList(behav);
	}
    }


    void processViewPlatformTransform(ViewPlatformRetained vp) {
	int i;
	BehaviorRetained behav;

	vp.updateTransformRegion();

	if (!vp.isActiveViewPlatform()) {
	    return;
	}

	BehaviorRetained behavArr[] = (BehaviorRetained []) behaviors.toArray(false);

	// re-evaulate all behaviors affected by this vp
	for (i=behaviors.arraySize()-1; i>=0; i--) {
	    behav = behavArr[i];
	    if (behav.active) {
		if (!intersectVPRegion(behav.transformedRegion)) {
		    removeFromScheduleList(behav);
		}
	    } else {
		addToScheduleList(behav);
	    }
	}

	// handle wakeupOnViewPlatformEntry
	WakeupOnViewPlatformEntry wakeupOnViewPlatformEntryArr[] =
	    (WakeupOnViewPlatformEntry []) wakeupOnViewPlatformEntry.toArray(false);
	WakeupOnViewPlatformEntry wentry;
	int idx;
	ViewPlatformRetained triggeredVP;

	for (i=wakeupOnViewPlatformEntry.arraySize()-1; i >=0; i--) {
	    wentry = wakeupOnViewPlatformEntryArr[i];
	    idx = boundsEntryList.indexOf(wentry);
	    if (idx < 0) {
		if (wentry.transformedRegion.intersect(vp.center)) {
		    boundsEntryList.add(wentry);
		    wentry.triggeredVP = vp;
		    wentry.setTriggered();
		}
	    } else {
		triggeredVP = intersectVPCenter(wentry.transformedRegion);
		if (triggeredVP == null) {
		    boundsEntryList.remove(idx);
		}
	    }
	}

	// handle wakeupOnViewPlatformExit;
	WakeupOnViewPlatformExit wakeupOnViewPlatformExitArr[] =
	    (WakeupOnViewPlatformExit []) wakeupOnViewPlatformExit.toArray(false);
	WakeupOnViewPlatformExit wexit;

	for (i=wakeupOnViewPlatformExit.arraySize()-1; i >=0; i--) {
	    wexit = wakeupOnViewPlatformExitArr[i];
	    idx = boundsExitList.indexOf(wexit);
	    if (idx < 0) {
		if (wexit.transformedRegion.intersect(vp.center)) {
		    wexit.triggeredVP = vp;
		    boundsExitList.add(wexit);

		}
	    } else {
		triggeredVP = intersectVPCenter(wexit.transformedRegion);
		if (triggeredVP == null) {
		    boundsExitList.remove(idx);
		    wexit.setTriggered();
		}
	    }
	}
    }

    void updateVPEntryTransformRegion(BehaviorRetained behav) {
	WakeupOnViewPlatformEntry wakeupOnViewPlatformEntryArr[] =
	    (WakeupOnViewPlatformEntry []) wakeupOnViewPlatformEntry.toArray(false);
	WakeupOnViewPlatformEntry wentry;
	ViewPlatformRetained triggeredVP;

	for (int i=wakeupOnViewPlatformEntry.arraySize()-1; i >=0; i--) {
	    wentry = wakeupOnViewPlatformEntryArr[i];
	    if (wentry.behav == behav) {
		wentry.updateTransformRegion(behav);
		int idx = boundsEntryList.indexOf(wentry);

		triggeredVP = intersectVPCenter(wentry.transformedRegion);
		if (triggeredVP != null) {
		    if (idx < 0) {
			boundsEntryList.add(wentry);
			wentry.triggeredVP = triggeredVP;
			wentry.setTriggered();
		    }
		} else {
		    if (idx >=0) {
			boundsEntryList.remove(idx);
		    }
		}
	    }

	}
    }



    void updateVPExitTransformRegion(BehaviorRetained behav) {
	WakeupOnViewPlatformExit wakeupOnViewPlatformExitArr[] =
	    (WakeupOnViewPlatformExit []) wakeupOnViewPlatformExit.toArray(false);
	WakeupOnViewPlatformExit wexit;
	ViewPlatformRetained triggeredVP;

	for (int i=wakeupOnViewPlatformExit.arraySize()-1; i >=0; i--) {
	    wexit = wakeupOnViewPlatformExitArr[i];
	    if (wexit.behav == behav) {
		wexit.updateTransformRegion(behav);
		wexit = wakeupOnViewPlatformExitArr[i];
		int idx = boundsExitList.indexOf(wexit);
		triggeredVP = intersectVPCenter(wexit.transformedRegion);
		if (triggeredVP != null) {
		    if (idx < 0) {
			wexit.triggeredVP = triggeredVP;
			boundsExitList.add(wexit);
		    }
		} else {
		    if (idx >= 0) {
			boundsExitList.remove(idx);
			wexit.setTriggered();
		    }
		}
	    }
	}
    }


void reEvaluatePhysicalEnvironments() {
	// we can't just add or remove from the list since
	// physicalEnvironment may be share by multiple view
	ViewPlatformRetained[] vpr = universe.getViewPlatformList();

	physicalEnvironments.clear();

	for (int i = vpr.length - 1; i >= 0; i--) {
		View[] views = vpr[i].getViewList();
		for (int j = views.length - 1; j >= 0; j--) {
			View v = views[j];
			if (!v.active)
				continue;

			if (!physicalEnvironments.contains(v.physicalEnvironment)) {
				physicalEnvironments.add(v.physicalEnvironment);
			}
		}
	}
}

    void checkSensorEntryExit() {
	int i, idx;
	Sensor target;

	// handle WakeupOnSensorEntry
	WakeupOnSensorEntry wentry;
	WakeupOnSensorEntry wentryArr[] = (WakeupOnSensorEntry [])
	                                    wakeupOnSensorEntry.toArray();

	for (i=wakeupOnSensorEntry.arraySize()-1; i>=0; i--) {
	    wentry = wentryArr[i];
	    idx = currentSensorEntryList.indexOf(wentry);
	    wentry.updateTransformRegion();
	    target = sensorIntersect(wentry.transformedRegion);
	    if (target != null) {
		if (idx < 0) {
		    currentSensorEntryList.add(wentry);
		    wentry.setTarget(target);
		    wentry.setTriggered();
		}
	    } else {
		if (idx >= 0) {
		    currentSensorEntryList.remove(idx);
		}
	    }
	}

	// handle WakeupOnSensorExit
	WakeupOnSensorExit wexit;
	WakeupOnSensorExit wexitArr[] = (WakeupOnSensorExit [])
	                                    wakeupOnSensorExit.toArray();

	for (i=wakeupOnSensorExit.arraySize()-1; i>=0; i--) {
	    wexit = wexitArr[i];
	    idx = currentSensorExitList.indexOf(wexit);
	    wexit.updateTransformRegion();
	    target = sensorIntersect(wexit.transformedRegion);
	    if (target != null) {
		if (idx < 0) {
		    currentSensorExitList.add(wexit);
		    wexit.setTarget(target);
		}
	    } else {
		if (idx >= 0) {
		    currentSensorExitList.remove(idx);
		    wexit.setTriggered();
		}
	    }
	}

    }


/**
 * return the Sensor that intersect with behregion or null
 */
Sensor sensorIntersect(Bounds behregion) {

	if (behregion == null)
		return null;

	PhysicalEnvironment env[] = (PhysicalEnvironment [])
	                               physicalEnvironments.toArray(false);
	for (int i = physicalEnvironments.arraySize() - 1; i >= 0; i--) {
		if (env[i].activeViewRef <= 0)
			continue;

		Sensor[] sensors = env[i].getSensorList();
		if (sensors == null)
			continue;

		for (int j = env[i].users.size() - 1; j >= 0; j--) {
			View v = env[i].users.get(j);
			synchronized (sensors) {
				for (int k = sensors.length - 1; k >= 0; k--) {
					Sensor s = sensors[k];
					if (s == null)
						continue;

					v.getSensorToVworld(s, sensorTransform);
					sensorTransform.get(sensorLoc);
					ptSensorLoc.set(sensorLoc);
					if (behregion.intersect(ptSensorLoc)) {
						return s;
					}
				}
			}
		}
	}
	return null;
}


    /**
     * return true if one of ViewPlatforms intersect behregion
     */
    final boolean intersectVPRegion(Bounds behregion) {
	if (behregion == null) {
	    return false;
	}

	ViewPlatformRetained vp;
	ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
	                                    viewPlatforms.toArray(false);

	for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
	    vp = vpLists[i];
	    if (vp.isActiveViewPlatform() &&
		vp.schedSphere.intersect(behregion)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * return true if one of ViewPlatforms center intersect behregion
     */
    final ViewPlatformRetained intersectVPCenter(Bounds behregion) {
	if (behregion == null) {
	    return null;
	}

	ViewPlatformRetained vp;
	ViewPlatformRetained vpLists[] = (ViewPlatformRetained [])
	                                    viewPlatforms.toArray(false);


	for (int i=viewPlatforms.arraySize()- 1; i>=0; i--) {
	    vp = vpLists[i];
	    if (vp.isActiveViewPlatform() &&
		behregion.intersect(vp.center)) {
		return vp;
	    }
	}
	return null;
    }

    void notifyDeactivationCondition(BehaviorRetained behav) {
	WakeupOnDeactivation wakeup;
	WakeupOnDeactivation wakeupConds[] = (WakeupOnDeactivation [])
	                                  wakeupOnDeactivation.toArray(false);

	for (int i=wakeupOnDeactivation.arraySize()-1; i>=0; i--) {
	    wakeup = wakeupConds[i];
	    if (wakeup.behav == behav) {
		wakeup.setTriggered();
	    }
	}
    }

    void notifyActivationCondition(BehaviorRetained behav) {
	WakeupOnActivation wakeup;
	WakeupOnActivation wakeupConds[] = (WakeupOnActivation [])
	                                  wakeupOnActivation.toArray(false);

	for (int i=wakeupOnActivation.arraySize()-1; i>=0; i--) {
	    wakeup = wakeupConds[i];
	    if (wakeup.behav == behav) {
		wakeup.setTriggered();
	    }
	}
    }


    void processSwitchChanged(J3dMessage m) {

        int i,j;
        UnorderList arrList;
        int size;
        Object[] nodes, nodesArr;

        UpdateTargets targets = (UpdateTargets)m.args[0];
        arrList = targets.targetList[Targets.VPF_TARGETS];

        if (arrList != null) {
	    ViewPlatformRetained vp;
            size = arrList.size();
            nodesArr = arrList.toArray(false);

            for (j=0; j<size; j++) {
                nodes = (Object[])nodesArr[j];
                for (i=nodes.length-1; i>=0; i--) {
                    vp = (ViewPlatformRetained) nodes[i];
		    vp.processSwitchChanged();
                }
            }
        }

        arrList = targets.targetList[Targets.BEH_TARGETS];

        if (arrList != null) {
	    BehaviorRetained behav;
            size = arrList.size();
            nodesArr = arrList.toArray(false);

            for (j=0; j<size; j++) {
                nodes = (Object[])nodesArr[j];
                for (i=nodes.length-1; i>=0; i--) {
                    behav = (BehaviorRetained) nodes[i];
                    if (behav.switchState.currentSwitchOn) {
                        addToScheduleList(behav);
                    } else {
                        removeFromScheduleList(behav);
                    }
                }
            }
        }

        arrList = targets.targetList[Targets.BLN_TARGETS];
        if (arrList != null) {
            size = arrList.size();
            nodesArr = arrList.toArray(false);
            Object[] objArr = (Object[])m.args[1];
            Object[] obj;

            for (int h=0; h<size; h++) {
                nodes = (Object[])nodesArr[h];
                obj = (Object[])objArr[h];
                for (i=nodes.length-1; i>=0; i--) {

                    Object[] users = (Object[])obj[i];
                    Object[] leafObj = new Object[1];
                    for (j = 0; j < users.length; j++) {
                        if (users[j] instanceof BehaviorRetained) {
                            leafObj[0] = users[j];
                            processTransformChanged(leafObj);
                        }
                    }
                }
            }
        }
    }

    void processBoundingLeafChanged(Object users[],
				    Bounds bound) {
	Object leaf;
	BehaviorRetained behav;

	for (int i=users.length-1; i>=0; i--) {
	    leaf = users[i];
	    if (leaf instanceof BehaviorRetained) {
		behav = (BehaviorRetained) leaf;
		behav.updateTransformRegion(bound);
		processBehaviorTransform(behav);
	    }
	}
    }

    final void removeFromScheduleList(BehaviorRetained behav) {
	if (behav.active) {
	    if ((behav.wakeupMask &
		 BehaviorRetained.WAKEUP_DEACTIVATE) != 0) {
		notifyDeactivationCondition(behav);
	    }
	    scheduleList.remove(behav);
	    behav.active = false;
	    if (behav.universe != universe) {
		J3dMessage m = new J3dMessage();
		m.threads = J3dThread.UPDATE_BEHAVIOR;
		m.type = J3dMessage.BEHAVIOR_REEVALUATE;
		m.universe = behav.universe;
		m.args[0] = behav;
		VirtualUniverse.mc.processMessage(m);
	    }
	}
    }

    final void addToScheduleList(BehaviorRetained behav) {

	if (!behav.inCallback &&
	    !behav.active &&
	    behav.enable &&
	    behav.switchState.currentSwitchOn &&
	    (behav.wakeupCondition != null) &&
	    ((Behavior) behav.source).isLive() &&
	    intersectVPRegion(behav.transformedRegion)) {

	    scheduleList.add(behav);
	    behav.active = true;
	    if ((behav.wakeupMask &
		 BehaviorRetained.WAKEUP_ACTIVATE) != 0) {
		notifyActivationCondition(behav);
	    }

	    if (behav.wakeupCondition != null) {
		// This reset the conditionMet, otherwise
		// if conditionMet is true then WakeupCondition
		// will never post message to BehaviorStructure
		behav.wakeupCondition.conditionMet = false;
	    }
	}
    }

    /**
     * This prevents wakeupCondition sent out message and sets
     * conditionMet to true, but the
     * BehaviorStructure/BehaviorScheduler is not fast enough to
     * process the message and reset conditionMet to false
     * when view deactivate/unregister.
     */
    void resetConditionMet() {
	resetConditionMet(wakeupOnAWTEvent);
	resetConditionMet(wakeupOnActivation);
	resetConditionMet(wakeupOnDeactivation);
	resetConditionMet(wakeupOnBehaviorPost);
	resetConditionMet(wakeupOnElapsedFrames);
	resetConditionMet(wakeupOnViewPlatformEntry);
	resetConditionMet(wakeupOnViewPlatformExit);
	resetConditionMet(wakeupOnSensorEntry);
	resetConditionMet(wakeupOnSensorExit);
    }

    static void resetConditionMet(WakeupIndexedList list) {
	WakeupCondition wakeups[] = (WakeupCondition []) list.toArray(false);
	int i = list.size()-1;
	while (i >= 0) {
	    wakeups[i--].conditionMet = false;
	}
    }

    void reEvaluateWakeupCount() {
	WakeupOnElapsedFrames wakeupConds[] = (WakeupOnElapsedFrames [])
	                          wakeupOnElapsedFrames.toArray(true);
	int size = wakeupOnElapsedFrames.arraySize();
	int i = 0;
	WakeupOnElapsedFrames cond;

	activeWakeupOnFrameCount = 0;

	while (i < size) {
	    cond = wakeupConds[i++];
	    if (!cond.passive &&
		(cond.behav != null) &&
		cond.behav.enable) {
		activeWakeupOnFrameCount++;
	    }
        }


	activeWakeupOnSensorCount = 0;
	WakeupOnSensorEntry wentry;
	WakeupOnSensorEntry wentryArr[] = (WakeupOnSensorEntry [])
	                                    wakeupOnSensorEntry.toArray();

	for (i=wakeupOnSensorEntry.arraySize()-1; i>=0; i--) {
	    wentry = wentryArr[i];
	    if ((wentry.behav != null) &&
		(wentry.behav.enable)) {
		activeWakeupOnSensorCount++;
	    }
	}

	WakeupOnSensorExit wexit;
	WakeupOnSensorExit wexitArr[] = (WakeupOnSensorExit [])
	                                    wakeupOnSensorExit.toArray();

	for (i=wakeupOnSensorExit.arraySize()-1; i>=0; i--) {
	    wexit = wexitArr[i];
	    if ((wexit.behav != null) &&
		(wexit.behav.enable)) {
		activeWakeupOnSensorCount++;
	    }
	}

    }

    void cleanup() {
	behaviors.clear();
	viewPlatforms.clear();
	scheduleList.clear();
	boundsEntryList.clear();
	boundsExitList.clear();
	currentSensorEntryList.clear();
	currentSensorExitList.clear();
	wakeupOnAWTEvent.clear();
	wakeupOnActivation.clear();
	wakeupOnDeactivation.clear();
	wakeupOnBehaviorPost.clear();
	wakeupOnElapsedFrames.clear();
	wakeupOnViewPlatformEntry.clear();
	wakeupOnViewPlatformExit.clear();
	wakeupOnSensorEntry.clear();
	wakeupOnSensorExit.clear();
    }
}
