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

package com.sun.j3d.utils.behaviors.sensor ;

import javax.media.j3d.Sensor ;
import javax.media.j3d.Transform3D ;

/**
 * This class defines the event object that is created by a
 * <code>SensorEventAgent</code> and passed to registered
 * <code>SensorReadListener</code> and <code>SensorButtonListener</code>
 * implementations.
 * <p>
 * The events passed to the listeners are <i>ephemeral</i>; they are only
 * valid until the listener has returned.  This is done to avoid
 * allocating large numbers of mostly temporary objects, especially for
 * behaviors that wake up every frame.  If a listener needs to retain the
 * event it must be copied using the <code>SensorEvent(SensorEvent)</code>
 * constructor.
 * 
 * @see SensorEventAgent
 * @see SensorButtonListener
 * @see SensorReadListener
 * @since Java 3D 1.3
 */
public class SensorEvent {
    /**
     * A button pressed event.
     */
    public static final int PRESSED = 1 ;

    /**
     * A button released event.
     */
    public static final int RELEASED = 2 ;

    /**
     * A button dragged event.
     */
    public static final int DRAGGED = 3 ;

    /**
     * A sensor read event.
     */
    public static final int READ = 4 ;

    /**
     * The value that is returned by <code>getButton</code> when no
     * buttons have changed state.
     */
    public static final int NOBUTTON = -1 ;

    private int id = 0 ;
    private Object source = null ;
    private Sensor sensor = null ;
    private int button = NOBUTTON ;
    private int[] buttonState = null ;
    private Transform3D sensorRead = null ;
    private long time = 0 ;
    private long lastTime = 0 ;
    private boolean ephemeral = false ;

    /**
     * Creates a new <code>SensorEvent</code>.
     * 
     * @param source a reference to the originating object which
     *  instantiated the <code>SensorEventAgent</code>, usually a
     *  <code>Behavior</code>; may be null
     * @param id event type
     * @param sensor a reference to the provoking sensor
     * @param sensorRead the sensor's read value at the time of the event
     * @param buttonState the state of the sensor's buttons at the time of
     *  the event, where a 1 in the array indicates that the button at that
     *  index is down, and a 0 indicates that button is up; may be null
     * @param button index of the button that changed state, from 0 to
     *  <code>(buttonCount - 1)</code>, or the value <code>NOBUTTON</code>
     * @param time the time in nanoseconds at which the
     *  <code>dispatchEvents</code> method of
     *  <code>SensorEventAgent</code> was called to generate this event,
     *  usually from the <code>processStimulus</code> method of a Behavior
     * @param lastTime the time in nanoseconds at which the
     *  <code>dispatchEvents</code> method of
     *  <code>SensorEventAgent</code> was <i>last</i> called to generate
     *  events, usually from the <code>processStimulus</code> method of a
     *  <code>Behavior</code>; may be used to measure frame time in
     *  behaviors that wake up every frame
     */
    public SensorEvent(Object source, int id, Sensor sensor,
		       Transform3D sensorRead, int[] buttonState,
		       int button, long time, long lastTime) {

	this.source = source ;
	this.id = id ;
	this.sensor = sensor ;
	this.button = button ;
	this.time = time ;
	this.lastTime = lastTime ;
	if (sensorRead == null)
	    throw new NullPointerException("sensorRead can't be null") ;
	this.sensorRead = new Transform3D(sensorRead) ;
	if (buttonState != null) {
	    this.buttonState = new int[buttonState.length] ;
	    for (int i = 0 ; i < buttonState.length ; i++)
		this.buttonState[i] = buttonState[i] ;
	}
	this.ephemeral = false ;
    }

    /**
     * Creates a new <i>ephemeral</i> <code>SensorEvent</code>.  In order
     * to avoid creating large numbers of sensor event objects, the events
     * passed to the button and read listeners by the
     * <code>dispatchEvents</code> method of <code>SensorEventAgent</code>
     * are valid only until the listener returns.  If the event needs to
     * be retained then they must be copied with the
     * <code>SensorEvent(SensorEvent)</code> constructor.
     */
    public SensorEvent() {
	this.ephemeral = true ;
    }

    /**
     * Creates a copy of the given <code>SensorEvent</code>.  Listeners
     * must use this constructor to copy events that need to be retained.
     * NOTE: The <code>Sensor</code> and <code>Object</code> references
     * returned by <code>getSensor</code> and <code>getSource</code>
     * remain references to the original objects.
     *
     * @param e the event to be copied
     */
    public SensorEvent(SensorEvent e) {
	this.source = e.source ;
	this.id = e.id ;
	this.sensor = e.sensor ;
	this.button = e.button ;
	this.time = e.time ;
	this.lastTime = e.lastTime ;
	if (e.sensorRead == null)
	    throw new NullPointerException("sensorRead can't be null") ;
	this.sensorRead = new Transform3D(e.sensorRead) ;
	if (e.buttonState != null) {
	    this.buttonState = new int[e.buttonState.length] ;
	    for (int i = 0 ; i < e.buttonState.length ; i++)
		this.buttonState[i] = e.buttonState[i] ;
	}
	this.ephemeral = false ;
    }

    /**
     * Sets the fields of an ephemeral event.  No objects are copied.  An
     * <code>IllegalStateException</code> will be thrown if this event
     * is not ephemeral.
     * 
     * @param source a reference to the originating object which
     *  instantiated the <code>SensorEventAgent</code>, usually a
     *  <code>Behavior</code>; may be null
     * @param id event type
     * @param sensor a reference to the provoking sensor
     * @param sensorRead the sensor's read value at the time of the event
     * @param buttonState the state of the sensor's buttons at the time of
     *  the event; a 1 in the array indicates that the button at that
     *  index is down, while a 0 indicates that button is up
     * @param button index of the button that changed state, from 0 to
     *  <code>(buttonCount - 1)</code>, or the value <code>NOBUTTON</code>
     * @param time the time in nanoseconds at which the
     *  <code>dispatchEvents</code> method of
     *  <code>SensorEventAgent</code> was called to generate this event,
     *  usually from the <code>processStimulus</code> method of a Behavior
     * @param lastTime the time in nanoseconds at which the
     *  <code>dispatchEvents</code> method of
     *  <code>SensorEventAgent</code> was <i>last</i> called to generate
     *  events, usually from the <code>processStimulus</code> method of a
     *  <code>Behavior</code>; may be used to measure frame time in
     *  behaviors that wake up every frame
     */
    public void set(Object source, int id, Sensor sensor,
		    Transform3D sensorRead, int[] buttonState,
		    int button, long time, long lastTime) {

	if (!ephemeral)
	    throw new IllegalStateException
		("Can't set the fields of non-ephemeral events") ;

	this.source = source ;
	this.id = id ;
	this.sensor = sensor ;
	if (sensorRead == null)
	    throw new NullPointerException("sensorRead can't be null") ;
	this.sensorRead = sensorRead ;
	this.buttonState = buttonState ;
	this.button = button ;
	this.time = time ;
	this.lastTime = lastTime ;
    }

    /**
     * Gets a reference to the originating object which instantiated the
     * <code>SensorEventAgent</code>, usually a <code>Behavior</code>; may
     * be null.
     * @return the originating object
     */
    public Object getSource() {
	return source ;
    }

    /**
     * Gets the event type.
     * @return the event id
     */
    public int getID() {
	return id ;
    }
         

    /**
     * Gets a reference to the provoking sensor.
     * @return the provoking sensor
     */
    public Sensor getSensor() {
	return sensor ;
    }

    /**
     * Gets the time in nanoseconds at which the
     * <code>dispatchEvents</code> method of <code>SensorEventAgent</code>
     * was called to generate this event, usually from the
     * <code>processStimulus</code> method of a <code>Behavior</code>.
     * @return time in nanoseconds
     */
    public long getTime() {
	return time ;
    }

    /**
     * Gets the time in nanoseconds at which the
     * <code>dispatchEvents</code> method of <code>SensorEventAgent</code>
     * was <i>last</i> called to generate events, usually from the
     * <code>processStimulus</code> method of a <code>Behavior</code>; may
     * be used to measure frame time in behaviors that wake up every
     * frame.
     * @return last time in nanoseconds
     */
    public long getLastTime() {
	return lastTime ;
    }

    /**
     * Copies the sensor's read value at the time of the event into the
     * given <code>Transform3D</code>.
     * 
     * @param t the transform to receive the sensor read
     */
    public void getSensorRead(Transform3D t) {
	t.set(sensorRead) ;
    }

    /**
     * Gets the index of the button that changed state when passed to a
     * <code>pressed</code> or <code>released</code> callback.  The index
     * may range from 0 to <code>(sensor.getSensorButtonCount() -
     * 1)</code>.  The value returned is <code>NOBUTTON</code> for events
     * passed to a <code>read</code> or <code>dragged</code> callback.
     * @return the button index
     */
    public int getButton() {
	return button ;
    }

    /**
     * Copies the state of the sensor's buttons at the time of the event
     * into the given array.  A 1 in the array indicates that the button
     * at that index is down, while a 0 indicates that button is up.  
     * @param buttonState the state of the sensor buttons
     */
    public void getButtonState(int[] buttonState) {
	if (buttonState.length != this.buttonState.length)
	    throw new ArrayIndexOutOfBoundsException
		("buttonState array is the wrong length") ;

	for (int i = 0 ; i < buttonState.length ; i++)
	    buttonState[i] = this.buttonState[i] ;
    }

    /**
     * Returns true if this event is <i>ephemeral</i> and is valid only
     * until the listener returns.  A copy of the event can be created by
     * passing it to the <code>SensorEvent(SensorEvent)</code>
     * constructor.
     */
    public boolean isEphemeral() {
	return ephemeral ;
    }
}

