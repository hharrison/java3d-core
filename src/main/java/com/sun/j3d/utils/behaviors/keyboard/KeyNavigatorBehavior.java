/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.behaviors.keyboard;

import java.awt.event.*;
import java.awt.AWTEvent;
import java.util.Enumeration;
import java.awt.Component;
import java.util.LinkedList;
import javax.vecmath.*;
import javax.media.j3d.*;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * This class is a simple behavior that invokes the KeyNavigator
 * to modify the view platform transform.
 */
public class KeyNavigatorBehavior extends Behavior implements KeyListener {
    private WakeupCriterion w1 = new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED);
    private WakeupCriterion w2 = new WakeupOnAWTEvent(KeyEvent.KEY_RELEASED);
    private WakeupOnElapsedFrames w3 = new WakeupOnElapsedFrames(0);
    private WakeupCriterion[] warray = { w1, w2, w3 };
    private WakeupCondition w = new WakeupOr(warray);
    private KeyEvent eventKey;
    private KeyNavigator keyNavigator;
    private boolean listener = false;

    private LinkedList eventq;


    /**
     *  Override Behavior's initialize method to setup wakeup criteria.
     */
    public void initialize() {
	// Establish initial wakeup criteria
	if (listener) {
	    w1 = new WakeupOnBehaviorPost(this, KeyEvent.KEY_PRESSED);
	    w2 = new WakeupOnBehaviorPost(this, KeyEvent.KEY_RELEASED);
	    warray[0] = w1;
	    warray[1] = w2;
	    w = new WakeupOr(warray);
	    eventq = new LinkedList();
	}
	wakeupOn(w);
    }

    /**
     *  Override Behavior's stimulus method to handle the event.
     */
    public void processStimulus(Enumeration criteria) {
	WakeupOnAWTEvent ev;
	WakeupCriterion genericEvt;
	AWTEvent[] events;
	boolean sawFrame = false;
   
	while (criteria.hasMoreElements()) {
	    genericEvt = (WakeupCriterion) criteria.nextElement();
	    if (genericEvt instanceof WakeupOnAWTEvent) {
		ev = (WakeupOnAWTEvent) genericEvt;
		events = ev.getAWTEvent();
		processAWTEvent(events);
	    } else if (genericEvt instanceof WakeupOnElapsedFrames &&
		       eventKey != null) {
		sawFrame = true;
	    } else if ((genericEvt instanceof WakeupOnBehaviorPost)) {
		while(true) {
		    // access to the queue must be synchronized
		    synchronized (eventq) {
			if (eventq.isEmpty()) break;
			eventKey  = (KeyEvent)eventq.remove(0);
			if (eventKey.getID() == KeyEvent.KEY_PRESSED ||
			    eventKey.getID() == KeyEvent.KEY_RELEASED) {
			    keyNavigator.processKeyEvent(eventKey);
			}
		    }
		}
	    }
	}
	if (sawFrame)
	    keyNavigator.integrateTransformChanges();

	// Set wakeup criteria for next time
	wakeupOn(w);
    }

    /**
     *  Process a keyboard event
     */
    private void processAWTEvent(AWTEvent[] events) {
	for (int loop = 0; loop < events.length; loop++) {
	    if (events[loop] instanceof KeyEvent) {
		eventKey = (KeyEvent) events[loop];
		//  change the transformation; for example to zoom
		if (eventKey.getID() == KeyEvent.KEY_PRESSED ||
		    eventKey.getID() == KeyEvent.KEY_RELEASED) {
		    //System.out.println("Keyboard is hit! " + eventKey);
		    keyNavigator.processKeyEvent(eventKey);
		}
	    }
	}
    }

    /**
     * Adds this behavior as a KeyListener to the specified component.
     * This method can only be called if
     * the behavior was created with one of the constructors that takes
     * a Component as a parameter.
     * @param c The component to add the KeyListener to.
     * @exception IllegalStateException if the behavior was not created
     * as a listener
     * @since Java 3D 1.2.1
     */
    public void addListener(Component c) {
	if (!listener) {
	    throw new IllegalStateException(J3dUtilsI18N.getString("Behavior0"));
	}
	c.addKeyListener(this);
    }

    /**
     * Constructs a new key navigator behavior node that operates
     * on the specified transform group.
     * @param targetTG the target transform group
     */
    public KeyNavigatorBehavior(TransformGroup targetTG) {
	keyNavigator = new KeyNavigator(targetTG);
    }

    /**
     * Constructs a key navigator behavior that uses AWT listeners
     * and behavior posts rather than WakeupOnAWTEvent.  The behavior
     * is added to the specified Component and works on the given
     * TransformGroup.  A null component can be passed to specify
     * the behavior should use listeners.  Components can then be added
     * to the behavior with the addListener(Component c) method.
     * @param c The component to add the KeyListener to.
     * @param targetTG The target transform group.
     * @since Java 3D 1.2.1
     */
    public KeyNavigatorBehavior(Component c, TransformGroup targetTG) {
	this(targetTG);
	if (c != null) {
	    c.addKeyListener(this);
	}
	listener = true;	
    }

    public void keyPressed(KeyEvent evt) {
// 	System.out.println("keyPressed");

	// add new event to the queue
	// must be MT safe
	synchronized (eventq) {
	    eventq.add(evt);
	    // only need to post if this is the only event in the queue
	    if (eventq.size() == 1) postId(KeyEvent.KEY_PRESSED);
	}
    }

    public void keyReleased(KeyEvent evt) {
// 	System.out.println("keyReleased");

	// add new event to the queue
	// must be MT safe
	synchronized (eventq) {
	    eventq.add(evt);
	    // only need to post if this is the only event in the queue
	    if (eventq.size() == 1) postId(KeyEvent.KEY_RELEASED);
	}
    }

    public void keyTyped(KeyEvent evt) {}

}
