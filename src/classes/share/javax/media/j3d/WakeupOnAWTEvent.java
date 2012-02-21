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

import java.awt.AWTEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

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
final ArrayList<AWTEvent> events = new ArrayList<AWTEvent>();

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
public AWTEvent[] getAWTEvent() {
	synchronized (events) {
		AWTEvent[] eventArray = events.toArray(new AWTEvent[events.size()]);
		events.clear();
		return eventArray;
	}
}

   /**
    * Sets the AWT event that will cause a behavior wakeup.
    * @param event The event causing this wakeup
    */
void addAWTEvent(AWTEvent event) {
	synchronized (events) {
		events.add(event);
	}
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
		}
		else if (AwtId == MouseEvent.MOUSE_WHEEL) {
		    behav.universe.enableMouseWheelEvents();
		}
		else if (AwtId == MouseEvent.MOUSE_CLICKED ||
			 AwtId == MouseEvent.MOUSE_ENTERED ||
			 AwtId == MouseEvent.MOUSE_EXITED ||
			 AwtId == MouseEvent.MOUSE_PRESSED ||
			 AwtId == MouseEvent.MOUSE_RELEASED) {
		    behav.universe.enableMouseEvents();
		}
	    } else {
		if ((EventMask & AWTEvent.MOUSE_EVENT_MASK) != 0) {
		    behav.universe.enableMouseEvents();
		}
		if ((EventMask & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0) {
		    behav.universe.enableMouseMotionEvents();
		}
		if ((EventMask & AWTEvent.MOUSE_WHEEL_EVENT_MASK) != 0) {
		    behav.universe.enableMouseWheelEvents();
		}
	    }
	    enableAWTEventTS = bs.awtEventTimestamp;
	}
    }
}
