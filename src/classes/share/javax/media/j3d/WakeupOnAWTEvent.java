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

import java.awt.AWTEvent;
import java.awt.event.*;
import java.util.Vector;

/**
 * Class that specifies a Behavior wakeup when a specific AWT event occurs.
 */
public final class WakeupOnAWTEvent extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 1;

    // one of these two variables must be equal to zero
    int AwtId;
    long EventMask;
    long enableAWTEventTS = 0L;
    Vector events = new Vector();

    /**
     * Constructs a new WakeupOnAWTEvent object that informs the Java 3D
     * scheduler to wake up the specified Behavior object whenever the 
     * specified AWT event occurs.
     * @param AWTId the AWT ids that this behavior wishes to intercept
     */
    public WakeupOnAWTEvent(int AWTId) {
	this.AwtId = AWTId;
	this.EventMask = 0L;
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Constructs a new WakeupOnAWTEvent using Ored EVENT_MASK values.
     * @param eventMask the AWT EVENT_MASK values Ored together
     */
    public WakeupOnAWTEvent(long eventMask) {
	this.EventMask = eventMask;
	this.AwtId = 0;
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Retrieves the array of consecutive AWT event that triggered this wakeup.
     * A value of null implies that this event was not the trigger for the
     * behavior wakeup.
     * @return either null (if not resposible for wakeup) or the array of
     * AWTEvents responsible for the wakeup.
     */
    public AWTEvent[] getAWTEvent(){
	AWTEvent eventArray[];

	synchronized (events) {
	    eventArray = new AWTEvent[events.size()];
	    events.copyInto(eventArray);
	    events.removeAllElements(); 
	}

	return eventArray;
    }


   /**
    * Sets the AWT event that will cause a behavior wakeup.
    * @param event The event causing this wakeup
    */
    void addAWTEvent(AWTEvent event){
	events.addElement(event);
	this.setTriggered();
    }

 
    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	resetBehaviorCondition(bs);
	bs.wakeupOnAWTEvent.add(this);
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.wakeupOnAWTEvent.remove(this);
    }

    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {
	
	if (enableAWTEventTS != bs.awtEventTimestamp) {
	    if ((AwtId >= ComponentEvent.COMPONENT_FIRST &&
		 AwtId <= ComponentEvent.COMPONENT_LAST) ||
		(EventMask & AWTEvent.COMPONENT_EVENT_MASK) != 0) {
		behav.universe.enableComponentEvents();
	    }
	    if ((AwtId >= FocusEvent.FOCUS_FIRST && AwtId <= FocusEvent.FOCUS_LAST) ||
		(EventMask & AWTEvent.FOCUS_EVENT_MASK) != 0) {
		behav.universe.enableFocusEvents();
	    }
	    if ((AwtId >= KeyEvent.KEY_FIRST && AwtId <= KeyEvent.KEY_LAST) ||
		(EventMask & AWTEvent.KEY_EVENT_MASK) != 0) {
		behav.universe.enableKeyEvents();
	    }
	    if ((AwtId >= MouseEvent.MOUSE_FIRST) && 
		(AwtId <= MouseEvent.MOUSE_LAST)) {
		if ((AwtId == MouseEvent.MOUSE_DRAGGED) || 
		    (AwtId == MouseEvent.MOUSE_MOVED)) {
		    behav.universe.enableMouseMotionEvents();
		} else {
		    behav.universe.enableMouseEvents();
		}
	    } else {
		if ((EventMask & AWTEvent.MOUSE_EVENT_MASK) != 0) {
		    behav.universe.enableMouseEvents();
		}
		if ((EventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0) {
		    behav.universe.enableMouseMotionEvents();
		}
	    }
	    enableAWTEventTS = bs.awtEventTimestamp;
	}
    }
}
