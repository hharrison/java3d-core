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

/**
 * This defines the interface for handling a sensor's button events in
 * conjunction with a <code>SensorEventAgent</code> instance. 
 * <p>
 * The events passed to this listener's methods are <i>ephemeral</i>; they
 * are only valid until the listener has returned.  If a listener needs to
 * retain the event it must be copied using the
 * <code>SensorEvent(SensorEvent)</code> constructor.
 * 
 * @see SensorEvent
 * @see SensorEventAgent
 * @see SensorReadListener
 * @since Java 3D 1.3
 */
public interface SensorButtonListener {
    /**
     * This method is called when a sensor's button is pressed.
     * 
     * @param e the sensor event
     */
    public void pressed(SensorEvent e) ;

    /**
     * This method is called when a sensor's button is released.
     * 
     * @param e the sensor event
     */
    public void released(SensorEvent e) ;

    /**
     * This method is called with each invocation of the
     * <code>dispatchEvents</code> method of <code>SensorEventAgent</code>
     * if any button bound to the listener is down and has not changed
     * state since the last invocation.  The sensor value has not
     * necessarily changed from the last drag event.
     * 
     * @param e the sensor event
     */
    public void dragged(SensorEvent e) ;

    /**
     * This method is currently not used by <code>SensorEventAgent</code>,
     * but is included here for future possible development.  Its
     * implementations should remain empty for the present.
     */
    public void clicked(SensorEvent e) ;
}

