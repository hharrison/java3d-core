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

import java.util.Enumeration ;
import javax.media.j3d.* ;
import javax.vecmath.Point3d ;
import javax.vecmath.Vector3f ;

/**
 * This class provides basic behavior for a 6DOF mouse sensor.  It
 * generates a visible 3D cursor echo in the virtual world which tracks the
 * position and orientation of the 6DOF mouse in the physical world.  It 
 * can be extended to provide other functions by accessing its
 * SensorEventAgent. 
 * 
 * @see SensorEventAgent
 * @since Java 3D 1.3
 */
public class Mouse6DPointerBehavior extends Behavior {
    private Sensor sensor = null ;
    private SensorEventAgent eventAgent = null ;
    private TransformGroup echoTransformGroup = null ;
    private WakeupCondition conditions = new WakeupOnElapsedFrames(0) ;

    /**
     * Constructs the behavior with a default echo.  To make the echo visible,
     * call getEcho() to retrieve the TransformGroup that parents the echo
     * geometry, and then add that TransformGroup to the scene graph.
     * <p>
     * The default echo is a solid 6-pointed star where each point is aligned
     * with the axes of the local coordinate system of the sensor, and with
     * the center of the star at the location of the sensor hotspot.
     * 
     * @param sensor a 6 degree of freedom Sensor which generates position
     *  and orientation relative to the tracker base.
     * @param size the physical width of the echo in centimeters.
     * @param enableLighting a boolean indicating whether the echo geometry
     *  should have lighting enabled.
     */
    public Mouse6DPointerBehavior(Sensor sensor, double size,
				  boolean enableLighting) {

	this.sensor = sensor ;
	echoTransformGroup = new TransformGroup() ;
	echoTransformGroup.setCapability
	    (TransformGroup.ALLOW_TRANSFORM_WRITE) ;

	Point3d hotspot = new Point3d() ;
	sensor.getHotspot(hotspot) ;

	Transform3D t3d = new Transform3D() ;
	Vector3f v3f = new Vector3f(hotspot) ;
	t3d.set(v3f) ;

	Shape3D echo =
	    new SensorGnomonEcho(t3d, 0.001*size, 0.005*size, enableLighting) ;
	echoTransformGroup.addChild(echo) ;

	eventAgent = new SensorEventAgent(this) ;
	eventAgent.addSensorReadListener(sensor, new EchoReadListener()) ;
    }

    /**
     * Constructs the behavior with an echo parented by the specified
     * TransformGroup.
     *
     * @param sensor a 6 degree of freedom Sensor which generates position
     *  and orientation relative to the tracker base.
     * @param tg a TransformGroup with a child defining the visible echo
     *  which will track the Sensor position and orientation; the Transform3D
     *  associated with the TransformGroup will be updated in order to effect
     *  the behavior, so it must have the ALLOW_TRANSFORM_WRITE capability
     *  set before the scene graph is set live
     */
    public Mouse6DPointerBehavior(Sensor sensor, TransformGroup tg) {
	this.sensor = sensor ;
	echoTransformGroup = tg ;
	eventAgent = new SensorEventAgent(this) ;
	eventAgent.addSensorReadListener(sensor, new EchoReadListener()) ;
    }

    /**
     *  Gets the sensor used by this behavior.
     *
     *  @return the sensor used by this behavior
     */
    public Sensor getSensor() {
	return sensor ;
    }

    /**
     *  Gets the echo used by this behavior.
     *
     *  @return the TransformGroup parenting this behavior's echo geometry 
     */
    public TransformGroup getEcho() {
	return echoTransformGroup ;
    }

    /**
     * Gets the SensorEventAgent used by this behavior.  This can be used to
     * add customized event bindings to this behavior.
     *
     * @return the SensorEventAgent
     */
    public SensorEventAgent getSensorEventAgent() {
	return eventAgent ;
    }

    /**
     * Initializes the behavior.
     * NOTE: Applications should not call this method. It is called by the
     * Java 3D behavior scheduler.
     */
    public void initialize() {
	wakeupOn(conditions) ;
    }

    /**
     * Processes a stimulus meant for this behavior. 
     * NOTE: Applications should not call this method. It is called by the
     * Java 3D behavior scheduler.
     */
    public void processStimulus(Enumeration criteria) {
	eventAgent.dispatchEvents() ;
	wakeupOn(conditions) ;
    }

    /**
     * This member class updates the echo transform in response to sensor
     * reads.
     */
    public class EchoReadListener implements SensorReadListener {
	private Transform3D t3d = new Transform3D() ;

	public void read(SensorEvent e) {
	    // Get the Transform3D that transforms points from local sensor
	    // coordinates to virtual world coordinates, based on the primary
	    // view associated with this Behavior.  This view is defined to be
	    // the first View attached to a live ViewPlatform.
	    //
	    // Note that this will display frame lag if another behavior such
	    // as OrbitBehavior is used to manipulate the view transform while
	    // the echo is visible.  In order to eliminate frame lag the
	    // behavior driving the view transform must also compute the echo
	    // transform as well.  See the WandViewBehavior utility for the
	    // appropriate techniques.
	    getView().getSensorToVworld(e.getSensor(), t3d) ;
	    echoTransformGroup.setTransform(t3d) ;
	}
    }
}
