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

package com.sun.j3d.utils.behaviors.mouse;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.internal.J3dUtilsI18N;


/**
 * Base class for all mouse manipulators (see MouseRotate, MouseZoom
 * and MouseTranslate for
 * examples of how to extend this base class). 
 */

public abstract class MouseBehavior extends Behavior
     implements MouseListener, MouseMotionListener, MouseWheelListener {

    private boolean listener = false;
    
    protected WakeupCriterion[] mouseEvents;
    protected WakeupOr mouseCriterion;
    protected int x, y;
    protected int x_last, y_last;
    protected TransformGroup transformGroup;
    protected Transform3D transformX;
    protected Transform3D transformY;
    protected Transform3D currXform;
    protected boolean buttonPress = false;
    protected boolean reset = false;
    protected boolean invert = false;
    protected boolean wakeUp = false;
    protected int flags = 0;

    // to queue the mouse events
    protected LinkedList mouseq;

    // true if this behavior is enable
    protected boolean enable = true;

   /**
    * Set this flag if you want to manually wakeup the behavior.
    */
    public static final int MANUAL_WAKEUP = 0x1;

    /** 
     * Set this flag if you want to invert the inputs.  This is useful when
     * the transform for the view platform is being changed instead of the 
     * transform for the object.
     */
    public static final int INVERT_INPUT = 0x2;

    /**
     * Creates a mouse behavior object with a given transform group.
     * @param transformGroup The transform group to be manipulated.
     */
    public MouseBehavior(TransformGroup transformGroup) {
	super();
	// need to remove old behavior from group 
	this.transformGroup = transformGroup;
	currXform = new Transform3D();
	transformX = new Transform3D();
	transformY = new Transform3D();
	reset = true;
    }

    /**
     * Initializes standard fields. Note that this behavior still
     * needs a transform group to work on (use setTransformGroup(tg)) and
     * the transform group must add this behavior.
     * @param format flags
     */
    public MouseBehavior(int format) {
	super();
	flags = format;
	currXform = new Transform3D();
	transformX = new Transform3D();
	transformY = new Transform3D();
	reset = true;
    }

    /**
     * Creates a mouse behavior that uses AWT listeners and behavior
     * posts rather than WakeupOnAWTEvent.  The behaviors is added to
     * the specified Component and works on the given TransformGroup.
     * A null component can be passed to specify the behaviors should use
     * listeners.  Components can then be added to the behavior with the
     * addListener(Component c) method.
     * @param c The Component to add the MouseListener and
     * MouseMotionListener to.
     * @param transformGroup The TransformGroup to operate on.
     * @since Java 3D 1.2.1
     */
    public MouseBehavior(Component c, TransformGroup transformGroup) {
	this(transformGroup);
	if (c != null) {
	    c.addMouseListener(this);
	    c.addMouseMotionListener(this);
	    c.addMouseWheelListener(this);
	}
	listener = true;
    }

    /**
     * Creates a mouse behavior that uses AWT listeners and behavior
     * posts rather than WakeupOnAWTEvent.  The behavior is added to the
     * specified Component.  A null component can be passed to specify
     * the behavior should use listeners.  Components can then be added
     * to the behavior with the addListener(Component c) method.
     * Note that this behavior still needs a transform
     * group to work on (use setTransformGroup(tg)) and the transform
     * group must add this behavior.
     * @param format interesting flags (wakeup conditions).
     * @since Java 3D 1.2.1
     */
    public MouseBehavior(Component c, int format) {
	this(format);
	if (c != null) {
	    c.addMouseListener(this);
	    c.addMouseMotionListener(this);
	    c.addMouseWheelListener(this);
	}
	listener = true;
    }
 
  /** 
   * Swap a new transformGroup replacing the old one. This allows 
   * manipulators to operate on different nodes.
   * 
   * @param transformGroup The *new* transform group to be manipulated.
   */
  public void setTransformGroup(TransformGroup transformGroup){
    // need to remove old behavior from group 
    this.transformGroup = transformGroup;
    currXform = new Transform3D();
    transformX = new Transform3D();
    transformY = new Transform3D();
    reset = true;
  }

  /**
   * Return the transformGroup on which this node is operating
   */
  public TransformGroup getTransformGroup() {
    return this.transformGroup;
  }

  /** Initializes the behavior.
   */

  public void initialize() {
    mouseEvents = new WakeupCriterion[4];

    if (!listener) {
	mouseEvents[0] = new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
	mouseEvents[1] = new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
	mouseEvents[2] = new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED);
	mouseEvents[3] = new WakeupOnAWTEvent(MouseEvent.MOUSE_WHEEL);
    }
    else {
	mouseEvents[0] = new WakeupOnBehaviorPost(this,
						  MouseEvent.MOUSE_DRAGGED);
	mouseEvents[1] = new WakeupOnBehaviorPost(this,
						  MouseEvent.MOUSE_PRESSED);
	mouseEvents[2] = new WakeupOnBehaviorPost(this,
						  MouseEvent.MOUSE_RELEASED);
	mouseEvents[3] = new WakeupOnBehaviorPost(this,
						  MouseEvent.MOUSE_WHEEL);
	mouseq = new LinkedList();
    }
    mouseCriterion = new WakeupOr(mouseEvents);
    wakeupOn (mouseCriterion);
    x = 0;
    y = 0;
    x_last = 0;
    y_last = 0;
  }
  
  /** 
   * Manually wake up the behavior. If MANUAL_WAKEUP flag was set upon 
   * creation, you must wake up this behavior each time it is handled.
   */

  public void wakeup()
  {
    wakeUp = true;
  }

  /**
   * Handles mouse events
   */
  public void processMouseEvent(MouseEvent evt) {
    if (evt.getID()==MouseEvent.MOUSE_PRESSED) {
      buttonPress = true;
      return;
    }
    else if (evt.getID()==MouseEvent.MOUSE_RELEASED){
      buttonPress = false;
      wakeUp = false;
    }    
    /* 
       else if (evt.getID() == MouseEvent.MOUSE_MOVED) {
       // Process mouse move event
       }
       else if (evt.getID() == MouseEvent.MOUSE_WHEEL) {
       // Process mouse wheel event
       }
    */
  }
  
  /**
   * All mouse manipulators must implement this.
   */
  public abstract void processStimulus (Enumeration criteria);

    /**
     * Adds this behavior as a MouseListener, mouseWheelListener and MouseMotionListener to
     * the specified component.  This method can only be called if
     * the behavior was created with one of the constructors that takes
     * a Component as a parameter.
     * @param c The component to add the MouseListener, MouseWheelListener and
     * MouseMotionListener to.
     * @exception IllegalStateException if the behavior was not created
     * as a listener
     * @since Java 3D 1.2.1
     */
    public void addListener(Component c) {
	if (!listener) {
	   throw new IllegalStateException(J3dUtilsI18N.getString("Behavior0"));
	}
	c.addMouseListener(this);
	c.addMouseMotionListener(this);
	c.addMouseWheelListener(this);
    }

    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    public void mousePressed(MouseEvent e) {
// 	System.out.println("mousePressed");

	// add new event to the queue
	// must be MT safe
	if (enable) {
	    synchronized (mouseq) {
		mouseq.add(e);
		// only need to post if this is the only event in the queue
		if (mouseq.size() == 1) 
		    postId(MouseEvent.MOUSE_PRESSED);
	    }
	}
    }

    public void mouseReleased(MouseEvent e) {
// 	System.out.println("mouseReleased");

	// add new event to the queue
	// must be MT safe
	if (enable) {
	    synchronized (mouseq) {
		mouseq.add(e);
		// only need to post if this is the only event in the queue
		if (mouseq.size() == 1)
		    postId(MouseEvent.MOUSE_RELEASED);
	    }
	}
    }

    public void mouseDragged(MouseEvent e) {
// 	System.out.println("mouseDragged");

	// add new event to the to the queue
	// must be MT safe.
	if (enable) {
	    synchronized (mouseq) {
		mouseq.add(e);
		// only need to post if this is the only event in the queue
		if (mouseq.size() == 1) 
		    postId(MouseEvent.MOUSE_DRAGGED);
	    }
	}
    }

    public void mouseMoved(MouseEvent e) {}

    public void setEnable(boolean state) {
	super.setEnable(state);
        this.enable = state;
	if (!enable && (mouseq != null)) {
	    mouseq.clear();
	}
    }

    public void mouseWheelMoved(MouseWheelEvent e){
	System.out.println("MouseBehavior : mouseWheel enable = " + enable );
	
	// add new event to the to the queue
	// must be MT safe.
	if (enable) {
	    synchronized (mouseq) {
		mouseq.add(e);
		// only need to post if this is the only event in the queue
		if (mouseq.size() == 1) 
		    postId(MouseEvent.MOUSE_WHEEL);
	    }
	}
    }
}


