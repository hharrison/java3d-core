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

package com.sun.j3d.utils.behaviors.vp ;

import java.util.* ;
import javax.vecmath.* ;
import javax.media.j3d.* ;
import com.sun.j3d.utils.universe.* ;
import com.sun.j3d.utils.behaviors.sensor.* ;

/**
 * Manipulates view platform transforms using a motion-tracked wand or mouse
 * equipped with a six degree of freedom (6DOF) sensor.  An optional two axis
 * (2D) valuator sensor is also directly supported.  Default operation is set
 * up to enable both direct manipulation of the view transform and translation
 * back and forth along the direction the 6DOF sensor is pointing; rotation
 * is handled by the 2D valuator if available.  An arbitrary number of sensors
 * and action bindings can be customized by accessing this behavior's
 * <code>SensorEventAgent</code> directly.
 * <p>
 * This behavior can be instantiated from the configuration file read by
 * <code>ConfiguredUniverse</code> and fully configured using the
 * <code>ViewPlatformBehaviorProperties</code> command to set the properties
 * described below, but neither <code>ConfiguredUniverse</code> nor
 * <code>SimpleUniverse</code> are required by this behavior.  Conventional
 * <code>set</code> and <code>get</code> accessors are provided for
 * configuring this behavior directly; these methods have the same names as
 * the properties but are prefixed with <code>get</code> and <code>set</code>.
 * Property values are spelled with mixed case strings while the corresponding
 * constant field names for the conventional accessors are spelled with upper
 * case strings and underscores.
 * <p>
 * {@link #Sensor6D Sensor6D} is the 6DOF sensor to use.  This can also be set
 * directly with the appropriate constructor.  This sensor must generate 6
 * degree of freedom position and orientation reads relative to the tracker
 * base in physical units.  By default this behavior provides an echo for the
 * 6DOF sensor which indicates its position and orientation in the virtual
 * world; the echo attributes can be set by the {@link #EchoType EchoType},
 * {@link #EchoSize EchoSize}, {@link #EchoColor EchoColor}, and {@link
 * #EchoTransparency EchoTransparency} properties.  See also the {@link
 * #NominalSensorRotation NominalSensorRotation} property, and the
 * <code>setHotSpot</code> method of the <code>Sensor</code> class.
 * <p>
 * {@link #Sensor2D Sensor2D} is an optional 2D valuator to use in conjunction
 * with the 6DOF sensor.  This can be set directly with the appropriate
 * constructor.  The valuator should generate X and Y reads ranging from [-1.0
 * .. +1.0], with a nominal (deadzone) value of 0.0.  The default
 * configuration expects to find these values along the translation components
 * of the read matrix, at indices 3 and 7, but these indices can be also be
 * specified by the {@link #MatrixIndices2D MatrixIndices2D} property.
 * <p>
 * {@link #ButtonAction6D ButtonAction6D} sets an action for a specific button
 * on a 6DOF sensor.  The actions available are:
 * <ul>
 * <li>
 * <code>GrabView</code> - Directly manipulates the view platform by moving
 * it in inverse response to the sensor's position and orientation,
 * producing the effect of attaching the virtual world to the sensor's
 * movements.  If a button is available then this action is bound to button 0
 * by default.
 * </li>
 * <li>
 * <code>TranslateForward</code> - Translates the view platform forward along
 * the direction the sensor is pointing; the virtual world appears to move
 * towards the sensor.  The default is button 1 if two buttons are available.
 * Related properties are {@link #TranslationSpeed TranslationSpeed}, {@link
 * #AccelerationTime AccelerationTime}, {@link #ConstantSpeedTime
 * ConstantSpeedTime}, and {@link #FastSpeedFactor FastSpeedFactor}.
 * </li>
 * <li>
 * <code>TranslateBackward</code> - Translates the view platform backwards
 * along the direction the sensor is pointing; the virtual world appears to
 * move away from the sensor.  The default is button 2 if three buttons are
 * available. 
 * </li>
 * <li>
 * <code>RotateCCW</code> - Rotates the view platform counter-clockwise about
 * a Y axis; the virtual world appears to rotate clockwise.  This action is
 * not assigned by default.  Related properties are {@link #RotationSpeed
 * RotationSpeed}, {@link #RotationCoords RotationCoords}, {@link
 * #TransformCenterSource TransformCenterSource}, {@link #TransformCenter
 * TransformCenter}, and <code>AccelerationTime</code>.
 * </li>
 * <li>
 * <code>RotateCW</code> - Rotates the view platform clockwise about a Y axis;
 * the virtual world appears to rotate counter-clockwise.  This action is not
 * assigned by default.
 * </li>
 * <li>
 * <code>ScaleUp</code> - Scales the view platform larger so that the virtual
 * world appears to grow smaller.  This action is not assigned by default.
 * Related properties are {@link #ScaleSpeed ScaleSpeed},
 * <code>TransformCenterSource</code>, <code>TransformCenter</code>, and
 * <code>AccelerationTime</code>.
 * </li>
 * <li>
 * <code>ScaleDown</code> - Scales the view platform smaller so that the
 * virtual world appears to grow larger.  This action is not assigned by
 * default.
 * </li>
 * </ul>
 * <p>
 * {@link #ReadAction2D ReadAction2D} sets the action bound to 2D valuator
 * reads; that is, non-zero values generated by the device when no buttons
 * have been pressed.  If the value is (0.0, 0.0) or below the threshold value
 * set by {@link #Threshold2D Threshold2D}, then this behavior does nothing;
 * otherwise, the following actions can be performed:
 * <ul>
 * <li>
 * <code>Rotation</code> - Rotates the view platform.  This is the default 2D
 * valuator action set by this behavior. Related properties are
 * <code>RotationSpeed</code>, <code>RotationCoords</code>,
 * <code>TransformCenterSource</code>, and <code>TransformCenter</code>.
 * </li>
 * <li>
 * <code>Translation</code> - Translates the view platform.  The translation
 * occurs relative to the X and Z basis vectors of either the 6DOF sensor or
 * the view platform if one is not available.  The maximum speed is equal to
 * the product of the <code>TranslationSpeed</code> and
 * <code>FastSpeedFactor</code> property values.
 * </li>
 * <li>
 * <code>Scale</code> - Scales the view platform smaller with positive Y
 * values and larger with negative Y values.  The effect is to increase the
 * apparent size of the virtual world when pushing the valuator forwards and
 * to decrease it when pushing backwards.  Related properties are
 * <code>ScaleSpeed</code>, <code>TransformCenterSource</code>, and
 * <code>TransformCenter</code>.
 * </li>
 * </ul>
 * <p>
 * {@link #ButtonAction2D ButtonAction2D} sets an action for a specific button
 * on the 2D valuator. The available actions are the same as for
 * <code>ReadAction2D</code>.  No actions are bound by default to the 2D
 * valuator buttons.
 * <p>
 * The view transform may be reset to its home transform by pressing a number
 * of buttons simultaneously on the 6DOF sensor.  The minimum number of
 * buttons that must be pressed is set by {@link #ResetViewButtonCount6D
 * ResetViewButtonCount6D}.  This value must be greater than one; the default
 * is three.  This action may be disabled by setting the property value to
 * None.  The corresponding property for the 2D valuator is {@link
 * #ResetViewButtonCount2D ResetViewButtonCount2D}, with a default value of
 * None.  Note, however, that the reset view action will be ineffectual if an
 * action which always modifies the view transform is bound to reads on the
 * sensor used to reset the view, since the reset transform will get
 * overwritten by the read action.
 * <p>
 * The special value <code>None</code> can in general be assigned to any
 * button or read action to prevent any defaults from being bound to it.
 *
 * @see ConfiguredUniverse
 * @see SensorEventAgent
 * @since Java 3D 1.3
 */
public class WandViewBehavior extends ViewPlatformBehavior {
    /**
     * Indicates a null configuration choice.
     */
    public static final int NONE = 0 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to grabbing the view.  The default is button 0.
     */
    public static final int GRAB_VIEW = 1 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to translating the view forward.  The default is button 1.
     */
    public static final int TRANSLATE_FORWARD = 2 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to translating the view backward.  The default is button 2.
     */
    public static final int TRANSLATE_BACKWARD = 3 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to rotate the view plaform counter-clockwise about a Y axis.
     */
    public static final int ROTATE_CCW = 4 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to rotate the view platform clockwise about a Y axis.
     */
    public static final int ROTATE_CW = 5 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to scaling the view platform larger.
     */
    public static final int SCALE_UP = 6 ;

    /**
     * Indicates that a 6DOF sensor button action should be bound
     * to scaling the view platform smaller.
     */
    public static final int SCALE_DOWN = 7 ;

    /**
     * Indicates that a 2D sensor button or read action should be bound
     * to translation.  
     */
    public static final int TRANSLATION = 8 ;

    /**
     * Indicates that a 2D sensor button or read action should be bound
     * to scaling.  
     */
    public static final int SCALE = 9 ;

    /**
     * Indicates that a 2D sensor button or read action should be bound
     * to rotation.  The default is to bind rotation to the 2D sensor reads.
     */
    public static final int ROTATION = 10 ;

    /**
     * Indicates that translation, rotation, or scaling speeds are
     * per frame.
     */
    public static final int PER_FRAME = 11 ;

    /**
     * Use to indicate that translation, rotation, or scaling speeds are per
     * second.  This is the default.
     */
    public static final int PER_SECOND = 12 ;

    /**
     * Indicates that translation speed is in virtual world units.
     */
    public static final int VIRTUAL_UNITS = 13 ;

    /**
     * Indicates that translation speed is in physical world units
     * (meters per second or per frame).  This is the default.
     */
    public static final int PHYSICAL_METERS = 14 ;

    /**
     * Indicates that rotation speed should be in radians.
     */
    public static final int RADIANS = 15 ;
    
    /**
     * Indicates that rotation speed should be in degrees.  This is the
     * default.
     */
    public static final int DEGREES = 16 ;
    
    /**
     * Indicates that rotation should occur in view platform
     * coordinates.
     */
    public static final int VIEW_PLATFORM = 17 ;

    /**
     * Indicates that rotation should occur in head coordinates.
     */
    public static final int HEAD = 18 ;

    /**
     * Indicates that rotation should occur in sensor coordinates.
     * This is the default.
     */
    public static final int SENSOR = 19 ;

    /**
     * Indicates that rotation or scale should be about a fixed point
     * in virtual world coordinates.
     */
    public static final int VWORLD_FIXED = 20 ;

    /**
     * Indicates that rotation or scale should be about a 6DOF sensor
     * hotspot.  This is the default.
     */
    public static final int HOTSPOT = 21 ;

    /**
     * Indicates that the 6DOF sensor read action should be bound to
     * displaying the sensor's echo in the virtual world.  This is the
     * default.
     */
    public static final int ECHO = 22 ;
    
    /**
     * Indicates that the echo type is a gnomon displaying the
     * directions of the sensor's local coordinate system axes at the location
     * of the sensor's hotspot.
     */
    public static final int GNOMON = 23 ;

    /**
     * Indicates that the echo type is a beam extending from the
     * origin of the sensor's local coordinate system to its hotspot.
     */
    public static final int BEAM = 24 ;

    /**
     * Indicates that a button listener or read listener has not been
     * set for a particular target.  This allows this behavior to use that
     * target for a default listener.
     */
    private static final int UNSET = -1 ;

    private View view = null ;
    private SensorEventAgent eventAgent = null ;
    private String sensor6DName = null ;
    private String sensor2DName = null ;
    private Shape3D echoGeometry = null ;
    private BranchGroup echoBranchGroup = null ;
    private TransformGroup echoTransformGroup = null ;
    private SensorReadListener echoReadListener6D = null ;
    private boolean echoBranchGroupAttached = false ;
    private WakeupCondition wakeupConditions = new WakeupOnElapsedFrames(0) ;
    private boolean configured = false ;

    // The rest of these private fields are all configurable through
    // ConfiguredUniverse. 
    private Sensor sensor6D = null ;
    private Sensor sensor2D = null ;
    private int x2D = 3 ;
    private int y2D = 7 ;
    private double threshold2D = 0.0 ;

    private int readAction6D = UNSET ;
    private int readAction2D = UNSET ;

    private ArrayList buttonActions6D = new ArrayList() ;
    private ArrayList buttonActions2D = new ArrayList() ;

    private double translationSpeed = 0.1 ;
    private int translationUnits = PHYSICAL_METERS ;
    private int translationTimeBase = PER_SECOND ;
    private double accelerationTime = 1.0 ;
    private double constantSpeedTime = 8.0 ;
    private double fastSpeedFactor = 10.0 ;

    private double rotationSpeed = 180.0 ;
    private int rotationUnits = DEGREES ;
    private int rotationTimeBase = PER_SECOND ;
    private int rotationCoords = SENSOR ;

    private double scaleSpeed = 2.0 ;
    private int scaleTimeBase = PER_SECOND ;

    private int transformCenterSource = HOTSPOT ;
    private Point3d transformCenter = new Point3d(0.0, 0.0, 0.0) ;

    private int resetViewButtonCount6D = 3 ;
    private int resetViewButtonCount2D = NONE ;

    private int echoType = GNOMON ;
    private double echoSize = 0.01 ;
    private Color3f echoColor = null ;
    private float echoTransparency = 0.0f ;
    private Transform3D nominalSensorRotation = null ;

    /**
     * Parameterless constructor for this behavior.  This is called when this
     * behavior is instantiated from a configuration file.
     * <p>
     * <b>Syntax:</b><br>(NewViewPlatformBehavior <i>&lt;name&gt;</i>
     * com.sun.j3d.utils.behaviors.vp.WandViewBehavior)
     */
    public WandViewBehavior() {
        // Create an event agent.
        eventAgent = new SensorEventAgent(this) ;

        // Set a default SchedulingBounds.
        setSchedulingBounds(new BoundingSphere(new Point3d(0.0, 0.0, 0.0),
                                               Double.POSITIVE_INFINITY)) ;
    }

    /**
     * Creates a new instance with the specified sensors and echo parameters.
     * At least one sensor must be non-<code>null</code>.
     * <p>
     * This constructor should only be used if either
     * <code>SimpleUniverse</code> or <code>ConfiguredUniverse</code> is used
     * to set up the view side of the scene graph, or if it is otherwise to be
     * attached to a <code>ViewingPlatform</code>.  If this behavior is not
     * instantiated from a configuration file then it must then be explicitly
     * attached to a <code>ViewingPlatform</code> instance with the
     * <code>ViewingPlatform.setViewPlatformBehavior</code> method.
     * 
     * @param sensor6D a six degree of freedom sensor which generates reads
     *  relative to the tracker base in physical units; may be
     *  <code>null</code>
     * @param sensor2D 2D valuator which generates X and Y reads ranging from
     *  [-1.0 .. +1.0]; may be <code>null</code>
     * @param echoType either <code>GNOMON</code>, <code>BEAM</code>, or
     *  <code>NONE</code> for the 6DOF sensor echo
     * @param echoSize the width of the 6DOF sensor echo in physical meters;
     *  ignored if echoType is <code>NONE</code>
     */
    public WandViewBehavior(Sensor sensor6D, Sensor sensor2D,
                            int echoType, double echoSize) {
        this() ;
        this.sensor6D = sensor6D ;
        this.sensor2D = sensor2D ;
        this.echoType = echoType ;
        this.echoSize = echoSize ;
    }

    /**
     * Creates a new instance with the specified sensors and a 6DOF sensor
     * echo parented by the specified <code>TransformGroup</code>.  At least
     * one sensor must be non-<code>null</code>.
     * <p>
     * This constructor should only be used if either
     * <code>SimpleUniverse</code> or <code>ConfiguredUniverse</code> is used
     * to set up the view side of the scene graph, or if it is otherwise to be
     * attached to a <code>ViewingPlatform</code>.  If this behavior is not
     * instantiated from a configuration file then it must then be explicitly
     * attached to a <code>ViewingPlatform</code> instance with the
     * <code>ViewingPlatform.setViewPlatformBehavior</code> method.
     * <p>
     * If the echo <code>TransformGroup</code> is non-<code>null</code>, it
     * will be added to a new <code>BranchGroup</code> and attached to the
     * <code>ViewingPlatform</code>, where its transform will be updated in
     * response to the sensor reads.  Capabilities to allow writing its
     * transform and to read, write, and extend its children will be set.  The
     * echo geometry is assumed to incorporate the position and orientation of
     * the 6DOF sensor hotspot.
     * 
     * @param sensor6D a six degree of freedom sensor which generates reads
     *  relative to the tracker base in physical units; may be
     *  <code>null</code>
     * @param sensor2D 2D valuator which generates X and Y reads ranging from
     *  [-1.0 .. +1.0]; may be <code>null</code>
     * @param echo a <code>TransformGroup</code> containing the visible echo
     *  which will track the 6DOF sensor's position and orientation, or
     *  <code>null</code> for no echo
     */
    public WandViewBehavior(Sensor sensor6D, Sensor sensor2D,
			    TransformGroup echo) {
        this() ;
        this.sensor6D = sensor6D ;
        this.sensor2D = sensor2D ;
	if (echo != null) {
	    echo.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE) ;
	    echo.setCapability(Group.ALLOW_CHILDREN_READ) ;
	    echo.setCapability(Group.ALLOW_CHILDREN_WRITE) ;
	    echo.setCapability(Group.ALLOW_CHILDREN_EXTEND) ;
	}
        this.echoTransformGroup = echo ;
    }

    /**
     * Creates a new instance with the specified sensors and a 6DOF sensor
     * echo parented by the specified <code>TransformGroup</code>.  At least
     * one sensor must be non-<code>null</code>.
     * <p>
     * This constructor should only be used if <code>SimpleUniverse</code> or
     * <code>ConfiguredUniverse</code> is <i>not</i> used to set up the view
     * side of the scene graph.  The application must set up the view side
     * itself and supply references to the <code>View</code> and the
     * <code>TransformGroup</code> containing the view platform transform.
     * <code>ViewingPlatform.setViewPlatformBehavior</code> must <i>not</i>
     * be called, and this behavior must be explicitly added to the virtual
     * universe by the application.
     * <p>
     * If the echo <code>TransformGroup</code> is non-<code>null</code>, it
     * will only be used to update its associated transform with the position
     * and orientation of a 6DOF sensor (if supplied).  The application is
     * responsible for adding the echo to the virtual universe.  The echo
     * geometry is assumed to incorporate the position and orientation of the
     * 6DOF sensor hotspot.
     * 
     * @param sensor6D a six degree of freedom sensor which generates reads
     *  relative to the tracker base in physical units; may be
     *  <code>null</code>
     * @param sensor2D 2D valuator which generates X and Y reads ranging from
     *  [-1.0 .. +1.0]; may be <code>null</code>
     * @param view a reference to the <code>View</code> attached to the 
     *  <code>ViewPlatform</code> to be manipulated by this behavior
     * @param viewTransform a <code>TransformGroup</code> containing the view
     *  platform transform; appropriate capabilities to update the transform
     *  must be set
     * @param homeTransform a <code>Transform3D</code> containing the
     *  view transform to be used when the view is reset; may be
     *  <code>null</code> for identity
     * @param echo a <code>TransformGroup</code> containing the visible echo
     *  which will track the 6DOF sensor's position and orientation, or
     *  <code>null</code> for no echo; appropriate capabilities to update the
     *  transform must be set
     */
    public WandViewBehavior(Sensor sensor6D, Sensor sensor2D,
			    View view, TransformGroup viewTransform,
			    Transform3D homeTransform, TransformGroup echo) {
        this() ;
        this.sensor6D = sensor6D ;
        this.sensor2D = sensor2D ;
	this.view = view ;
	this.targetTG = viewTransform ;
        this.echoTransformGroup = echo ;

        if (homeTransform == null)
            setHomeTransform(new Transform3D()) ;
	else
	    setHomeTransform(homeTransform) ;
    }

    /**
     * Initializes and configures this behavior.
     * NOTE: Applications should <i>not</i> call this method. It is called by
     * the Java 3D behavior scheduler.
     */
    public void initialize() {
        // Don't configure the sensors and echo after the first time.
        if (!configured) {
            configureSensorActions() ;

	    // Configure an echo only if a ViewingPlatform is in use.
	    if (vp != null) {
		if (echoTransformGroup == null &&
		    sensor6D != null && readAction6D == ECHO) {
		    configureEcho() ;
		}
		if (echoTransformGroup != null) {
		    echoBranchGroup = new BranchGroup() ;
		    echoBranchGroup.setCapability
			(BranchGroup.ALLOW_DETACH) ;
		    echoBranchGroup.setCapability
			(BranchGroup.ALLOW_CHILDREN_READ) ;
		    echoBranchGroup.setCapability
			(BranchGroup.ALLOW_CHILDREN_WRITE) ;

		    echoBranchGroup.addChild(echoTransformGroup) ;
		    echoBranchGroup.compile() ;
		}
		attachEcho() ;
	    }
            configured = true ;
        }
        wakeupOn(wakeupConditions) ;
    }

    /**
     * Processes a stimulus meant for this behavior.  
     * NOTE: Applications should <i>not</i> call this method. It is called by
     * the Java 3D behavior scheduler.
     */
    public void processStimulus(Enumeration criteria) {
        // Invoke the sensor event dispatcher.
        eventAgent.dispatchEvents() ;

        // Wake up on the next frame.
        wakeupOn(wakeupConditions) ;
    }

    /**
     * Enables or disables this behavior.  The default state is enabled.
     * @param enable true or false to enable or disable this behavior
     */
    public void setEnable(boolean enable) {
        if (enable == getEnable()) {
            return ;
	}
	else if (enable) {
	    attachEcho() ;
	}
	else {
	    detachEcho() ;
	}
        super.setEnable(enable) ;
    }
    
    /**
     * Sets the <code>ViewingPlatform</code> for this behavior.  If a subclass
     * overrides this method, it must call
     * <code>super.setViewingPlatform(vp)</code>.  NOTE: Applications should
     * <i>not</i> call this method.  It is called by the
     * <code>ViewingPlatform</code>.
     */
    public void setViewingPlatform(ViewingPlatform vp) {
        super.setViewingPlatform(vp) ;
        if (vp == null) {
	    detachEcho() ;
            return ;
        }

	Viewer[] viewers = vp.getViewers() ;
	if (viewers != null) {
	    // Get the View from the first Viewer attached to the
	    // ViewingPlatform.  Multiple Viewers are not supported.
	    if (viewers.length != 0 && viewers[0] != null)
		view = viewers[0].getView() ;

	    if (viewers.length > 1)
		throw new RuntimeException("multiple Viewers not supported") ;
	}
	if (view == null) {
	    // Fallback to the first View attached to a live ViewPlatform.
	    view = getView() ;
	}
	if (view == null) {
	    // This behavior requires a view.  Bail.
	    throw new RuntimeException("a view is not available") ;
	}

        // Get the top-most TransformGroup in the ViewingPlatform.
        // ViewPlatformBehavior retrieves the bottom-most which won't work
        // if there are multiple TransformGroups.
        targetTG = vp.getMultiTransformGroup().getTransformGroup(0) ;

        // Should be an API for checking if homeTransform is null.
        if (homeTransform == null)
            setHomeTransform(new Transform3D()) ;

	attachEcho() ;
    }
    
    /**
     * Attaches the echo BranchGroup to the ViewingPlatform if appropriate.
     */
    private void attachEcho() {
	if (vp != null &&
	    echoBranchGroup != null && !echoBranchGroupAttached) {
	    vp.addChild(echoBranchGroup) ;
	    echoBranchGroupAttached = true ;
	}
    }

    /**
     * Detaches the echo BranchGroup from the ViewingPlatform if appropriate.
     */
    private void detachEcho() {
	if (echoBranchGroup != null && echoBranchGroupAttached) {
	    echoBranchGroup.detach() ;
	    echoBranchGroupAttached = false ;
	}
    }

    /**
     * Creates the sensor listeners for a 6DOF sensor and/or a 2D valuator
     * sensor using the predefined button and read listeners and the
     * configured action bindings.
     * <p>
     * This is invoked the first time <code>initialize</code> is called.  This
     * method can be overridden by subclasses to modify the configured
     * bindings or introduce other configuration parameters.
     */
    protected void configureSensorActions() {
        SensorButtonListener[] sbls ;
        int buttonCount, buttonActionCount ;

        SimpleUniverse universe = null ;
	if (vp != null) universe = vp.getUniverse() ;
        if (universe != null && universe instanceof ConfiguredUniverse) {
            // Check if sensors were instantiated from a config file.
            Map sensorMap = ((ConfiguredUniverse)universe).getNamedSensors() ;
            
            if (sensor2D == null && sensor2DName != null) {
                sensor2D = (Sensor)sensorMap.get(sensor2DName) ;
                if (sensor2D == null)
                    throw new IllegalArgumentException
                        ("\nsensor " + sensor2DName + " not found") ;
            }
            
            if (sensor6D == null && sensor6DName != null) {
                sensor6D = (Sensor)sensorMap.get(sensor6DName) ;
                if (sensor6D == null)
                    throw new IllegalArgumentException
                        ("\nsensor " + sensor6DName + " not found") ;
            }
        }

        if (sensor6D != null) {
            // Assign default read action.
            if (readAction6D == UNSET)
                readAction6D = ECHO ;

            // Register the read listener.
            if (readAction6D == ECHO) {
                echoReadListener6D = new EchoReadListener6D() ;
                eventAgent.addSensorReadListener
                    (sensor6D, echoReadListener6D) ;
            }

            // Check for button range.
            buttonCount = sensor6D.getSensorButtonCount() ;
            buttonActionCount = buttonActions6D.size() ;
            if (buttonActionCount > buttonCount)
                throw new IllegalArgumentException
                    ("\nbutton index " + (buttonActionCount-1) +
                     " >= number of buttons (" + buttonCount +")") ;
            
            // Assign default button actions.
            if (buttonCount > 2 &&
                (buttonActionCount < 3 || buttonActions6D.get(2) == null))
                setButtonAction6D(2, TRANSLATE_BACKWARD) ;
            if (buttonCount > 1 &&
                (buttonActionCount < 2 || buttonActions6D.get(1) == null))
                setButtonAction6D(1, TRANSLATE_FORWARD) ;
            if (buttonCount > 0 &&
                (buttonActionCount < 1 || buttonActions6D.get(0) == null))
                setButtonAction6D(0, GRAB_VIEW) ;

            buttonActionCount = buttonActions6D.size() ;
            if (buttonActionCount > 0) {
                // Set up the button listener array.
                sbls = new SensorButtonListener[buttonCount] ;
                for (int i = 0 ; i < buttonActionCount ; i++) {
                    Integer button = (Integer)buttonActions6D.get(i) ;
                    if (button != null) {
                        int action = button.intValue() ;
                        if (action == NONE)
                            sbls[i] = null ;
                        else if (action == GRAB_VIEW)
                            sbls[i] = new GrabViewListener6D() ;
                        else if (action == TRANSLATE_FORWARD)
                            sbls[i] = new TranslationListener6D(false) ;
                        else if (action == TRANSLATE_BACKWARD) 
                            sbls[i] = new TranslationListener6D(true) ;
			else if (action == ROTATE_CCW)
			    sbls[i] = new RotationListener6D(false) ;
			else if (action == ROTATE_CW)
			    sbls[i] = new RotationListener6D(true) ;
			else if (action == SCALE_UP)
			    sbls[i] = new ScaleListener6D(false) ;
			else if (action == SCALE_DOWN)
			    sbls[i] = new ScaleListener6D(true) ;
                    }
                }
                // Register the button listeners.
                eventAgent.addSensorButtonListeners(sensor6D, sbls) ;
            }

            // Check for reset view action.
            if (resetViewButtonCount6D != NONE) {
                SensorInputAdaptor r =
                    new ResetViewListener(sensor6D, resetViewButtonCount6D) ;
                eventAgent.addSensorButtonListener(sensor6D, r) ;
                eventAgent.addSensorReadListener(sensor6D, r) ;
            }
        }

        if (sensor2D != null) {
            // Assign default read action
            if (readAction2D == UNSET)
                readAction2D = ROTATION ;

            // Register the read listener.
            if (readAction2D == ROTATION) {
                SensorReadListener r =
                    new RotationListener2D(sensor2D, sensor6D) ;
                eventAgent.addSensorReadListener(sensor2D, r) ;
            }
            else if (readAction2D == TRANSLATION) {
                SensorReadListener r =
                    new TranslationListener2D(sensor2D, sensor6D) ;
                eventAgent.addSensorReadListener(sensor2D, r) ;
            }
            else if (readAction2D == SCALE) {
                SensorReadListener r =
                    new ScaleListener2D(sensor2D, sensor6D) ;
                eventAgent.addSensorReadListener(sensor2D, r) ;
            }

            // Check for button range.
            buttonCount = sensor2D.getSensorButtonCount() ;
            buttonActionCount = buttonActions2D.size() ;
            if (buttonActionCount > buttonCount)
                throw new IllegalArgumentException
                    ("\nbutton index " + (buttonActionCount-1) +
                     " >= number of buttons (" + buttonCount +")") ;

            // No default button actions are defined for the 2D sensor.
            if (buttonActionCount > 0) {
                // Set up the button listener array.
                sbls = new SensorButtonListener[buttonCount] ;
                for (int i = 0 ; i < buttonActionCount ; i++) {
                    Integer button = (Integer)buttonActions2D.get(i) ;
                    if (button != null) {
                        int action = button.intValue() ;
                        if (action == NONE)
                            sbls[i] = null ;
                        else if (action == ROTATION)
                            sbls[i] = new RotationListener2D
                                (sensor2D, sensor6D) ;
                        else if (action == TRANSLATION)
                            sbls[i] = new TranslationListener2D
                                (sensor2D, sensor6D) ;
                        else if (action == SCALE)
                            sbls[i] = new ScaleListener2D
                                (sensor2D, sensor6D) ;
                    }
                }
                // Register the button listeners.
                eventAgent.addSensorButtonListeners(sensor2D, sbls) ;
            }

            // Check for reset view action.
            if (resetViewButtonCount2D != NONE) {
                SensorInputAdaptor r =
                    new ResetViewListener(sensor2D, resetViewButtonCount2D) ;
                eventAgent.addSensorButtonListener(sensor2D, r) ;
                eventAgent.addSensorReadListener(sensor2D, r) ;
            }
        }
    }

    /**
     * Creates a 6DOF sensor echo according to configuration parameters.  This
     * is done only if a 6DOF sensor has been specified, the 6DOF sensor read
     * action has been set to echo the sensor position, the echo transform
     * group has not already been set, and a ViewingPlatform is in use.  This
     * is invoked the first time <code>initialize</code> is called to set this
     * behavior live, but before the echo transform group is added to a
     * <code>BranchGroup</code> and made live.  This method can be overridden
     * to support other echo geometry.
     */
    protected void configureEcho() {
        Point3d hotspot = new Point3d() ;
        sensor6D.getHotspot(hotspot) ;

        if (echoType == GNOMON) {
            Transform3D gnomonTransform = new Transform3D() ;
            if (nominalSensorRotation != null) {
                gnomonTransform.set(nominalSensorRotation) ;
                gnomonTransform.invert() ;
            }
            gnomonTransform.setTranslation(new Vector3d(hotspot)) ;
            echoGeometry = new SensorGnomonEcho
                (gnomonTransform, 0.1 * echoSize, 0.5 * echoSize, true) ;
        }
        else if (echoType == BEAM) {
            echoGeometry = new SensorBeamEcho(hotspot, echoSize, true) ;
        }

        if (echoGeometry != null) {
            Appearance a = echoGeometry.getAppearance() ;
            if (echoColor != null) {
                Material m = a.getMaterial() ;
                m.setDiffuseColor(echoColor) ;
            }
            if (echoTransparency != 0.0f) {
                TransparencyAttributes ta = a.getTransparencyAttributes() ;
                ta.setTransparencyMode(TransparencyAttributes.BLENDED) ;
                ta.setTransparency(echoTransparency) ;
                // Use order independent additive blend for gnomon.
                if (echoGeometry instanceof SensorGnomonEcho)
                    ta.setDstBlendFunction(TransparencyAttributes.BLEND_ONE) ;
            }
            echoTransformGroup = new TransformGroup() ;
            echoTransformGroup.setCapability
                (TransformGroup.ALLOW_TRANSFORM_WRITE) ;
            echoTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ) ;
            echoTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE) ;
            echoTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND) ;
            echoTransformGroup.addChild(echoGeometry) ;
        }
    }

    /**
     * A base class for implementing some of this behavior's listeners.
     */
    public class ListenerBase extends SensorInputAdaptor {
	/**
	 * The initial transform from view platform coordinates to virtual
	 * world coordinates, set by <code>initAction</code>.
	 */
        protected Transform3D viewPlatformToVworld = new Transform3D() ;

	/**
	 * The initial transform from tracker base coordinates to virtual
	 * world coordinates, set by <code>initAction</code>.
	 */
        protected Transform3D trackerToVworld = new Transform3D() ;

	/**
	 * The initial transform from sensor coordinates to virtual
	 * world coordinates, set by <code>initAction</code>.
	 */
        protected Transform3D sensorToVworld = new Transform3D() ;

	/**
	 * The initial transform from sensor coordinates to tracker base
	 * coordinates, set by <code>initAction</code>.
	 */
        protected Transform3D sensorToTracker = new Transform3D() ;

	// Private fields.
        private Transform3D trackerToSensor = new Transform3D() ;
        private boolean active = false ;

	// Misc. temporary objects.
	private double[] s3Tmp = new double[3] ;
	private double[] m16Tmp = new double[16] ;
	private Vector3d v3dTmp = new Vector3d() ;
	private Transform3D t3dTmp = new Transform3D() ;

        /**
         * Initializes the listener action.  Subclasses must call this before
         * starting the action, either from <code>pressed</code> or when a 2D
         * valuator exits the deadzone threshold.
         * 
         * @param s reference to a 6DOF sensor if used by the listener; may
         *  be <code>null</code>
	 */
        protected void initAction(Sensor s) {
            targetTG.getTransform(viewPlatformToVworld) ;
            active = true ;
            if (s == null) return ;

            // Kludge to get the static trackerToVworld for this
            // frame.  This is computed from the two separate sensor reads
            // below, which are close enough to identical to work.  The
            // Java 3D View class needs a getTrackerBaseToVworld() method
            // (see Java 3D RFE 4676808).
            s.getRead(sensorToTracker) ;
            view.getSensorToVworld(s, sensorToVworld) ;

            trackerToSensor.invert(sensorToTracker) ;
            trackerToVworld.mul(sensorToVworld, trackerToSensor) ;
        }

        /**
         * Ends the action.  Subclasses must be call this from
         * <code>released</code> or when a 2D valuator enters the deadzone
         * threshold.
         * 
         * @param s reference to a 6DOF sensor if used by the listener; may
         *  be <code>null</code>
	 */
        protected void endAction(Sensor s) {
            active = false ;
        }

        /**
         * Returns true if the listener is currently active; that is, if
         * <code>initAction</code> has been called but not yet
	 * <code>endAction</code>.
	 *
	 * @return true if the listener is active, false otherwise
         */
        protected boolean isActive() {
            return active ;
        }

        public void pressed(SensorEvent e) {
            initAction(e.getSensor()) ;
        }

        public void released(SensorEvent e) {
            endAction(e.getSensor()) ;
        }

	/**
	 * Gets the physical to virtual scale.
	 */
	protected double getPhysicalToVirtualScale() {
	    view.getCanvas3D(0).getImagePlateToVworld(t3dTmp) ;
	    t3dTmp.get(m16Tmp) ;
	    return Math.sqrt(m16Tmp[0]*m16Tmp[0] +
			     m16Tmp[1]*m16Tmp[1] +
			     m16Tmp[2]*m16Tmp[2]) ;
	}

	/**
	 * Gets the scale from physical units to view platform units.
	 */
	protected double getPhysicalToViewPlatformScale() {
	    double vpToVirtualScale ;

	    targetTG.getTransform(t3dTmp) ;
	    t3dTmp.get(m16Tmp) ;
	    vpToVirtualScale = Math.sqrt(m16Tmp[0]*m16Tmp[0] +
					 m16Tmp[1]*m16Tmp[1] +
					 m16Tmp[2]*m16Tmp[2]) ;

	    return getPhysicalToVirtualScale() / vpToVirtualScale ;
	}

	/**
	 * Translates a coordinate system.
	 *
	 * @param transform the coordinate system to be translated
	 * @param translation the vector by which to translate
	 */
	protected void translateTransform(Transform3D transform,
					  Vector3d translation) {
	    transform.get(v3dTmp) ;
	    v3dTmp.add(translation) ;
	    transform.setTranslation(v3dTmp) ;
	}

	/**
	 * Transforms the target coordinate system about a center point.
	 * This can be used for rotation and scaling.
	 *
	 * @param target the coordinate system to transform
	 * @param center the center point about which to transform
	 * @param transform the transform to apply
	 */
	protected void transformAboutCenter
	    (Transform3D target, Point3d center, Transform3D transform) {

	    // Translate to the center.
	    target.get(v3dTmp) ;
	    v3dTmp.sub(center) ;
	    target.setTranslation(v3dTmp) ;

	    // Apply the transform.
	    target.mul(transform, target) ;

	    // Translate back.
	    target.get(v3dTmp) ;
	    v3dTmp.add(center) ;
	    target.setTranslation(v3dTmp) ;
	}

	/**
	 * Equalizes the scale factors in the view tranform, which must be
	 * congruent.  If successful, the <code>ViewingPlatform
	 * TransformGroup</code> is updated; otherwise, its transform is reset
	 * to the home transform.  This should be called if multiple
	 * incremental scale factors are applied to the view transform.
	 *
	 * @param viewPlatformToVworld the view transform
	 */
	protected void conditionViewScale(Transform3D viewPlatformToVworld) {
	    viewPlatformToVworld.normalize() ;
	    viewPlatformToVworld.get(m16Tmp) ;

	    s3Tmp[0] = m16Tmp[0]*m16Tmp[0] +
		m16Tmp[4]*m16Tmp[4] + m16Tmp[8]*m16Tmp[8] ;
	    s3Tmp[1] = m16Tmp[1]*m16Tmp[1] +
		m16Tmp[5]*m16Tmp[5] + m16Tmp[9]*m16Tmp[9] ;
	    s3Tmp[2] = m16Tmp[2]*m16Tmp[2] +
		m16Tmp[6]*m16Tmp[6] + m16Tmp[10]*m16Tmp[10] ;

	    if (s3Tmp[0] == s3Tmp[1] && s3Tmp[0] == s3Tmp[2])
		return ;

	    s3Tmp[0] = Math.sqrt(s3Tmp[0]) ;
	    s3Tmp[1] = Math.sqrt(s3Tmp[1]) ;
	    s3Tmp[2] = Math.sqrt(s3Tmp[2]) ;

	    int closestToOne = 0 ;
	    if (Math.abs(s3Tmp[1] - 1.0) < Math.abs(s3Tmp[0] - 1.0))
		closestToOne = 1 ;
	    if (Math.abs(s3Tmp[2] - 1.0) < Math.abs(s3Tmp[closestToOne] - 1.0))
		closestToOne = 2 ;
            
	    double scale ;
	    for (int i = 0 ; i < 3 ; i++) {
		if (i == closestToOne) continue ;
		scale = s3Tmp[closestToOne] / s3Tmp[i] ;
		m16Tmp[i+0] *= scale ;
		m16Tmp[i+4] *= scale ;
		m16Tmp[i+8] *= scale ;
	    }
                
	    // Set the view transform and bail out if unsuccessful.
	    viewPlatformToVworld.set(m16Tmp) ;
	    if ((viewPlatformToVworld.getType() & Transform3D.CONGRUENT) == 0)
		goHome() ;
	    else
		targetTG.setTransform(viewPlatformToVworld) ; 
	}
    }

    /**
     * Implements a 6DOF sensor button listener to directly manipulate the
     * view platform transform.  The view platform moves in inverse response
     * to the sensor's position and orientation to give the effect of
     * attaching the virtual world to the sensor's echo.
     * @see #setButtonAction6D
     */
    public class GrabViewListener6D extends ListenerBase {
        private Transform3D t3d = new Transform3D() ;
        private Transform3D initialVworldToSensor = new Transform3D() ;

        public void pressed(SensorEvent e) {
            initAction(e.getSensor()) ;

            // Save the inverse of the initial sensorToVworld.
            initialVworldToSensor.invert(sensorToVworld) ;
        }

        public void dragged(SensorEvent e) {
            // Get sensor read relative to the static view at the time of the
            // button-down.
            Sensor s = e.getSensor() ;
            s.getRead(sensorToTracker) ;
            sensorToVworld.mul(trackerToVworld, sensorToTracker) ;
                    
            // Solve for T, where T x initialSensorToVworld = sensorToVworld
            t3d.mul(sensorToVworld, initialVworldToSensor) ;

            // Move T to the view side by inverting it, and then applying it
            // to the static view transform.
            t3d.invert() ;
            t3d.mul(viewPlatformToVworld) ;
            targetTG.setTransform(t3d) ;
        }
    }

    /**
     * Implements a 6DOF sensor button listener that translates the view
     * platform along the direction the sensor is pointing.
     * @see #setButtonAction6D
     * @see #setTranslationSpeed
     * @see #setAccelerationTime
     * @see #setConstantSpeedTime
     * @see #setFastSpeedFactor
     */
    public class TranslationListener6D extends ListenerBase {
        private long buttonDownTime ;
        private double speedScaled ;
        private double interval0 ;
        private double interval1 ;
        private double interval2 ;
        private Vector3d v3d = new Vector3d() ;

	/**
	 * Construct a new translation button listener for a 6DOF sensor.
	 *
	 * @param reverse if true, translate the view platform backwards;
	 *  otherwise, translate the view platform forwards
	 */
        public TranslationListener6D(boolean reverse) {
            // Compute translation speed intervals.
            interval0 = accelerationTime ;
            interval1 = interval0 + constantSpeedTime ;
            interval2 = interval1 + accelerationTime ;

            // Apply virtual to physical scale if needed.
            if (translationUnits == VIRTUAL_UNITS)
                speedScaled = translationSpeed / getPhysicalToVirtualScale() ;
            else
                speedScaled = translationSpeed ;

	    if (reverse) {
		speedScaled = -speedScaled ;
	    }
        }

        public void pressed(SensorEvent e) {
            initAction(e.getSensor()) ;
            buttonDownTime = e.getTime() ;
        }

        public void dragged(SensorEvent e) {
            long time = e.getTime() ;
            long lastTime = e.getLastTime() ;
            double currSpeed, transTime ;
            double frameTime = 1.0 ;
            if (translationTimeBase == PER_SECOND)
                frameTime = (time - lastTime) / 1e9 ;

            // Compute speed based on acceleration intervals.
            transTime = (time - buttonDownTime) / 1e9 ;
            if (transTime <= interval0) {
                currSpeed = (transTime / accelerationTime) * speedScaled ;
            }
            else if (transTime > interval1 && transTime < interval2) {
                currSpeed = ((((transTime-interval1) / accelerationTime) *
                              (fastSpeedFactor-1.0)) + 1.0) * speedScaled ;
            }
            else if (transTime >= interval2) {
                currSpeed = fastSpeedFactor * speedScaled ;
            }
            else {
                currSpeed = speedScaled ;
            }

            // Transform the translation direction (0, 0, -1).
            v3d.set(0.0, 0.0, -1.0) ;
            if (nominalSensorRotation != null)
                nominalSensorRotation.transform(v3d) ;

            // To avoid echo frame lag, compute sensorToVworld based on
            // computed trackerToVworld.  getSensorToVworld() isn't
            // current for this frame.
            Sensor s = e.getSensor() ;
            s.getRead(sensorToTracker) ;
            sensorToVworld.mul(trackerToVworld, sensorToTracker) ;
            sensorToVworld.transform(v3d) ;

            // Translate the view platform.
            v3d.scale(frameTime * currSpeed) ;
            translateTransform(viewPlatformToVworld, v3d) ;
            targetTG.setTransform(viewPlatformToVworld) ;

            // Translate trackerToVworld.
            translateTransform(trackerToVworld, v3d) ;

            if (readAction6D == ECHO) {
                // Translate sensor echo to compensate for the new view
                // platform movement.
                translateTransform(sensorToVworld, v3d) ;
                updateEcho(s, sensorToVworld) ;
            }
        }
    }

    /**
     * Implements a 6DOF sensor button listener that rotates the view platform
     * about a Y axis.  This axis can be relative to the sensor, user head, or
     * view platform.  The rotation center can be the sensor hotspot or a
     * fixed point in virtual world coordinates.
     * 
     * @see #setButtonAction6D
     * @see #setRotationCoords
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #setRotationSpeed
     * @see #setAccelerationTime
     */
    public class RotationListener6D extends ListenerBase {
	private boolean reverse ;
        private long buttonDownTime ;
        private Vector3d axis = new Vector3d() ;
        private Point3d center = new Point3d() ;
        private Transform3D t3d = new Transform3D() ;
        private AxisAngle4d aa4d = new AxisAngle4d() ;
        private Transform3D headToVworld = new Transform3D() ;
        private double speedScaled ;

        protected void initAction(Sensor s) {
            super.initAction(s) ;
            if (rotationCoords == HEAD) {
                view.setUserHeadToVworldEnable(true) ;
            }
        }

        protected void endAction(Sensor s) {
            super.endAction(s) ;
            viewPlatformToVworld.normalize() ;
            targetTG.setTransform(viewPlatformToVworld) ;
            if (rotationCoords == HEAD) {
                view.setUserHeadToVworldEnable(false) ;
            }
        }

	/**
	 * Construct a new rotation button listener for a 6DOF sensor.
	 *
	 * @param reverse if true, rotate clockwise; otherwise, rotate
	 *  counter-clockwise
	 */
        public RotationListener6D(boolean reverse) {
	    this.reverse = reverse ;
            if (rotationUnits == DEGREES)
                speedScaled = rotationSpeed * Math.PI / 180.0 ;
            else
                speedScaled = rotationSpeed ;
        }

        public void pressed(SensorEvent e) {
            initAction(e.getSensor()) ;
            buttonDownTime = e.getTime() ;
        }

        public void dragged(SensorEvent e) {
            long time = e.getTime() ;
            long lastTime = e.getLastTime() ;
            double currSpeed, transTime ;
            double frameTime = 1.0 ;
            if (rotationTimeBase == PER_SECOND)
                frameTime = (time - lastTime) / 1e9 ;

            // Compute speed based on acceleration interval.
            transTime = (time - buttonDownTime) / 1e9 ;
            if (transTime <= accelerationTime) {
                currSpeed = (transTime / accelerationTime) * speedScaled ;
            }
            else {
                currSpeed = speedScaled ;
            }

            // Set the rotation axis.
	    if (reverse)
		axis.set(0.0, -1.0, 0.0) ;
	    else
		axis.set(0.0,  1.0, 0.0) ;

            // To avoid echo frame lag, compute sensorToVworld based on
            // computed trackerToVworld.  getSensorToVworld() isn't current
            // for this frame.
            Sensor s = e.getSensor() ;
            s.getRead(sensorToTracker) ;
            sensorToVworld.mul(trackerToVworld, sensorToTracker) ;

	    // Transform rotation axis into target coordinate system.
	    if (rotationCoords == SENSOR) {
		if (nominalSensorRotation != null)
		    nominalSensorRotation.transform(axis) ;

		sensorToVworld.transform(axis) ;
	    }
	    else if (rotationCoords == HEAD) {
		view.getUserHeadToVworld(headToVworld) ;
		headToVworld.transform(axis) ;
	    }
	    else {
		viewPlatformToVworld.transform(axis) ;
	    }

	    // Get the rotation center.
	    if (transformCenterSource == HOTSPOT) {
		s.getHotspot(center) ;
		sensorToVworld.transform(center) ;
	    }
	    else {
		center.set(transformCenter) ;
	    }

	    // Construct origin-based rotation about axis.
	    aa4d.set(axis, currSpeed * frameTime) ;
	    t3d.set(aa4d) ;

	    // Apply the rotation to the view platform.
	    transformAboutCenter(viewPlatformToVworld, center, t3d) ;
	    targetTG.setTransform(viewPlatformToVworld) ;

	    // Apply the rotation to trackerToVworld.
	    transformAboutCenter(trackerToVworld, center, t3d) ;

	    if (readAction6D == ECHO) {
		// Transform sensor echo to compensate for the new view
		// platform movement.
		transformAboutCenter(sensorToVworld, center, t3d) ;
		updateEcho(s, sensorToVworld) ;
	    }
        }
    }

    /**
     * Implements a 6DOF sensor button listener that scales the view platform.
     * The center of scaling can be the sensor hotspot or a fixed location in
     * virtual world coordinates.
     * 
     * @see #setButtonAction6D
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #setScaleSpeed
     * @see #setAccelerationTime
     */
    public class ScaleListener6D extends ListenerBase {
	private double direction ;
        private long buttonDownTime ;
        private Point3d center = new Point3d() ;
        private Transform3D t3d = new Transform3D() ;

        protected void endAction(Sensor s) {
            super.endAction(s) ;
            conditionViewScale(viewPlatformToVworld) ;
        }

	/**
	 * Construct a new scale button listener for a 6DOF sensor.
	 *
	 * @param reverse if true, scale the view platform smaller; otherwise,
	 *  scale the view platform larger
	 */
        public ScaleListener6D(boolean reverse) {
	    if (reverse)
		direction = -1.0 ;
	    else
		direction =  1.0 ;
        }

        public void pressed(SensorEvent e) {
            initAction(e.getSensor()) ;
            buttonDownTime = e.getTime() ;
        }

        public void dragged(SensorEvent e) {
            long time = e.getTime() ;
            long lastTime = e.getLastTime() ;
            double scale, exp, transTime ;
            double frameTime = 1.0 ;
            if (scaleTimeBase == PER_SECOND)
                frameTime = (time - lastTime) / 1e9 ;

            // Compute speed based on acceleration interval.
            transTime = (time - buttonDownTime) / 1e9 ;
            if (transTime <= accelerationTime) {
		exp = (transTime / accelerationTime) * frameTime * direction ;
            }
            else {
		exp = frameTime * direction ;
            }
	    scale = Math.pow(scaleSpeed, exp) ;

	    // To avoid echo frame lag, compute sensorToVworld based on
	    // computed trackerToVworld.  getSensorToVworld() isn't current
	    // for this frame.
            Sensor s = e.getSensor() ;
	    s.getRead(sensorToTracker) ;
	    sensorToVworld.mul(trackerToVworld, sensorToTracker) ;

	    // Get the scale center.
	    if (transformCenterSource == HOTSPOT) {
		s.getHotspot(center) ;
		sensorToVworld.transform(center) ;
	    }
	    else {
		center.set(transformCenter) ;
	    }

	    // Apply the scale to the view platform.
	    t3d.set(scale) ;
	    transformAboutCenter(viewPlatformToVworld, center, t3d) ;

	    // Incremental scaling at the extremes can lead to numerical
	    // instability, so catch BadTransformException to prevent the
	    // behavior thread from being killed.  Using a cumulative scale
	    // matrix avoids this problem to a better extent, but causes the
	    // 6DOF sensor hotspot center to jitter excessively.
	    try {
		targetTG.setTransform(viewPlatformToVworld) ;
	    }
	    catch (BadTransformException bt) {
		conditionViewScale(viewPlatformToVworld) ;
	    }

	    // Apply the scale to trackerToVworld.
	    transformAboutCenter(trackerToVworld, center, t3d) ;

	    if (readAction6D == ECHO) {
		// Scale sensor echo to compensate for the new view
		// platform scale.
		transformAboutCenter(sensorToVworld, center, t3d) ;
		updateEcho(s, sensorToVworld) ;
	    }
        }
    }

    /**
     * Implements a 6DOF sensor read listener that updates the orientation and
     * position of the sensor's echo in the virtual world.
     * 
     * @see #setEchoType
     * @see #setEchoSize
     * @see #setReadAction6D
     * @see SensorGnomonEcho
     * @see SensorBeamEcho
     */
    public class EchoReadListener6D implements SensorReadListener {
        private Transform3D sensorToVworld = new Transform3D() ;

        public void read(SensorEvent e) {
            Sensor s = e.getSensor() ;
            view.getSensorToVworld(s, sensorToVworld) ;
            updateEcho(s, sensorToVworld) ;
        }
    }

    /**
     * Implements a 2D valuator listener that rotates the view platform.  The
     * X and Y values from the valuator should have a continuous range from
     * -1.0 to +1.0, although the rotation speed can be scaled to compensate
     * for a different range.  The X and Y values are found in the sensor's
     * read matrix at the indices specified by
     * <code>setMatrixIndices2D</code>, with defaults of 3 and 7 respectively.
     * <p>
     * The rotation direction is controlled by the direction the 2D valuator
     * is pushed, and the rotation speed is scaled by the magnitude of the 2D
     * valuator read values.
     * <p>
     * This listener will work in conjunction with a 6DOF sensor if supplied
     * in the constructor.  If a 6DOF sensor is provided and
     * <code>setRotationCoords</code> has been called with the value
     * <code>SENSOR</code>, then the rotation is applied in the 6DOF sensor's
     * coordinate system; otherwise the rotation is applied either in head
     * coordinates or in view platform coordinates.  If a 6DOF sensor is
     * provided and <code>setTransformCenterSource</code> has been called with
     * the value <code>HOTSPOT</code>, then rotation is about the 6DOF
     * sensor's hotspot; otherwise, the rotation center is the value set by
     * <code>setTransformCenter</code>.
     *
     * @see #setReadAction2D
     * @see #setButtonAction2D
     * @see #setRotationCoords
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #setRotationSpeed
     * @see #setThreshold2D
     * @see #setMatrixIndices2D
     */
    public class RotationListener2D extends ListenerBase {
        private Sensor sensor2D, sensor6D ;
        private double[] m = new double[16] ;
        private Vector3d axis = new Vector3d() ;
        private Point3d center = new Point3d() ;
        private Transform3D t3d = new Transform3D() ;
        private AxisAngle4d aa4d = new AxisAngle4d() ;
        private Transform3D sensor2DRead = new Transform3D() ;
        private Transform3D headToVworld = new Transform3D() ;
        private double speedScaled ;

        protected void initAction(Sensor s) {
            super.initAction(s) ;
            if (rotationCoords == HEAD) {
                view.setUserHeadToVworldEnable(true) ;
            }
            if (s != null && readAction6D == ECHO) {
                // Disable the 6DOF echo.  It will be updated in this action.
                eventAgent.removeSensorReadListener(s, echoReadListener6D) ;
            }
        }

        protected void endAction(Sensor s) {
            super.endAction(s) ;
            viewPlatformToVworld.normalize() ;
            targetTG.setTransform(viewPlatformToVworld) ;
            if (rotationCoords == HEAD) {
                view.setUserHeadToVworldEnable(false) ;
            }
            if (s != null && readAction6D == ECHO) {
                eventAgent.addSensorReadListener(s, echoReadListener6D) ;
            }
        }

        /**
         * Construct an instance of this class with the specified sensors.
         * 
         * @param sensor2D the 2D valuator whose X and Y values drive the
         *  rotation 
         * @param sensor6D the 6DOF sensor to use if the rotation coordinate
         *  system is set to <code>SENSOR</code> or the rotation center source
	 *  is <code>HOTSPOT</code>;  may be <code>null</code>
         */
        public RotationListener2D(Sensor sensor2D, Sensor sensor6D) {
            this.sensor2D = sensor2D ;
            this.sensor6D = sensor6D ;

            if (rotationUnits == DEGREES)
                speedScaled = rotationSpeed * Math.PI / 180.0 ;
            else
                speedScaled = rotationSpeed ;
        }

        public void read(SensorEvent e) {
            sensor2D.getRead(sensor2DRead) ;
            sensor2DRead.get(m) ;

            if (m[x2D] > threshold2D || m[x2D] < -threshold2D ||
                m[y2D] > threshold2D || m[y2D] < -threshold2D) {
                // Initialize action on threshold crossing.
                if (!isActive()) initAction(sensor6D) ;
                
                // m[x2D] is the X valuator value and m[y2D] is the Y valuator
                // value.  Use these to construct the rotation axis.
                double length = Math.sqrt(m[x2D]*m[x2D] + m[y2D]*m[y2D]) ;
                double iLength = 1.0/length ;
                axis.set(m[y2D]*iLength, -m[x2D]*iLength, 0.0) ;

                if (sensor6D != null) {
                    // To avoid echo frame lag, compute sensorToVworld based
                    // on computed trackerToVworld.  getSensorToVworld() isn't
                    // current for this frame.
                    sensor6D.getRead(sensorToTracker) ;
                    sensorToVworld.mul(trackerToVworld, sensorToTracker) ;
                }

                // Transform rotation axis into target coordinate system.
                if (sensor6D != null && rotationCoords == SENSOR) {
                    if (nominalSensorRotation != null)
                        nominalSensorRotation.transform(axis) ;

                    sensorToVworld.transform(axis) ;
                }
                else if (rotationCoords == HEAD) {
                    view.getUserHeadToVworld(headToVworld) ;
                    headToVworld.transform(axis) ;
                }
                else {
                    viewPlatformToVworld.transform(axis) ;
                }

                // Get the rotation center.
                if (transformCenterSource == HOTSPOT && sensor6D != null) {
                    sensor6D.getHotspot(center) ;
                    sensorToVworld.transform(center) ;
                }
                else {
                    center.set(transformCenter) ;
                }

                double frameTime = 1.0 ;
                if (rotationTimeBase == PER_SECOND)
                    frameTime = (e.getTime() - e.getLastTime()) / 1e9 ;

                // Construct origin-based rotation about axis.
                aa4d.set(axis, speedScaled * frameTime * length) ;
                t3d.set(aa4d) ;

                // Apply the rotation to the view platform.
                transformAboutCenter(viewPlatformToVworld, center, t3d) ;
                targetTG.setTransform(viewPlatformToVworld) ;

                if (sensor6D != null) {
                    // Apply the rotation to trackerToVworld.
                    transformAboutCenter(trackerToVworld, center, t3d) ;
                }

                if (sensor6D != null && readAction6D == ECHO) {
                    // Transform sensor echo to compensate for the new view
                    // platform movement.
                    transformAboutCenter(sensorToVworld, center, t3d) ;
                    updateEcho(sensor6D, sensorToVworld) ;
                }
            }
            else {
                // Initialize action on next threshold crossing.
                if (isActive()) endAction(sensor6D) ;
            }
        }

        public void pressed(SensorEvent e) {
            initAction(sensor6D) ;
        }

        public void released(SensorEvent e) {
            if (isActive()) endAction(sensor6D) ;
        }

        public void dragged(SensorEvent e) {
            read(e) ;
        }
    }

    /**
     * Implements a 2D valuator listener that translates the view platform.
     * The X and Y values from the valuator should have a continuous range
     * from -1.0 to +1.0, although the translation speed can be scaled to
     * compensate for a different range.  The X and Y values are found in the
     * sensor's read matrix at the indices specified by
     * <code>setMatrixIndices2D</code>, with defaults of 3 and 7 respectively.
     * <p>
     * The translation direction is controlled by the direction the 2D
     * valuator is pushed, and the speed is the translation speed scaled by
     * the fast speed factor and the magnitude of the 2D valuator reads.
     * <p>
     * This listener will work in conjunction with a 6DOF sensor if supplied
     * in the constructor.  If a 6DOF sensor is provided then the translation
     * occurs along the basis vectors of the 6DOF sensor's coordinate system;
     * otherwise, the translation occurs along the view platform's basis
     * vectors.
     * 
     * @see #setReadAction2D
     * @see #setButtonAction2D
     * @see #setTranslationSpeed
     * @see #setFastSpeedFactor
     * @see #setThreshold2D
     * @see #setMatrixIndices2D
     */
    public class TranslationListener2D extends ListenerBase {
        private Sensor sensor2D, sensor6D ;
        private double[] m = new double[16] ;
        private Vector3d v3d = new Vector3d() ;
        private Transform3D sensor2DRead = new Transform3D() ;
        private double speedScaled ;

        protected void initAction(Sensor s) {
            super.initAction(s) ;
            if (s != null && readAction6D == ECHO) {
                // Disable the 6DOF echo.  It will be updated in this action.
                eventAgent.removeSensorReadListener(s, echoReadListener6D) ;
            }
        }

        protected void endAction(Sensor s) {
            super.endAction(s) ;
            if (s != null && readAction6D == ECHO) {
                // Enable the 6DOF sensor echo.
                eventAgent.addSensorReadListener(s, echoReadListener6D) ;
            }
        }

        /**
         * Construct an instance of this class using the specified sensors.
         * 
         * @param sensor2D 2D valuator sensor for translation
         * @param sensor6D 6DOF sensor for translation direction; may be
         * <code>null</code>
         */
        public TranslationListener2D(Sensor sensor2D, Sensor sensor6D) {
            this.sensor2D = sensor2D ;
            this.sensor6D = sensor6D ;

            // Apply virtual to physical scale if needed.
            if (translationUnits == VIRTUAL_UNITS)
                speedScaled = translationSpeed *
                    fastSpeedFactor / getPhysicalToVirtualScale() ;
            else
                speedScaled = translationSpeed * fastSpeedFactor ;

            // Apply physical to view platform scale if needed.
            if (sensor6D == null)
                speedScaled *= getPhysicalToViewPlatformScale() ;
        }

        public void read(SensorEvent e) {
            sensor2D.getRead(sensor2DRead) ;
            sensor2DRead.get(m) ;

            if (m[x2D] > threshold2D || m[x2D] < -threshold2D ||
                m[y2D] > threshold2D || m[y2D] < -threshold2D) {
                // Initialize action on threshold crossing.
                if (!isActive()) initAction(sensor6D) ;

                // m[x2D] is the X valuator value and m[y2D] is the Y valuator
                // value.  Use these to construct the translation vector.
                double length = Math.sqrt(m[x2D]*m[x2D] + m[y2D]*m[y2D]) ;
                double iLength = 1.0/length ;
                v3d.set(m[x2D]*iLength, 0.0, -m[y2D]*iLength) ;

                // Transform translation vector into target coordinate system.
                if (sensor6D != null) {
                    if (nominalSensorRotation != null)
                        nominalSensorRotation.transform(v3d) ;

                    // To avoid echo frame lag, compute sensorToVworld based
                    // on computed trackerToVworld.  getSensorToVworld() isn't
                    // current for this frame.
                    sensor6D.getRead(sensorToTracker) ;
                    sensorToVworld.mul(trackerToVworld, sensorToTracker) ;
                    sensorToVworld.transform(v3d) ;
                }
                else {
                    viewPlatformToVworld.transform(v3d) ;
                }

                double frameTime = 1.0 ;
                if (translationTimeBase == PER_SECOND)
                    frameTime = (e.getTime() - e.getLastTime()) / 1e9 ;

                v3d.scale(frameTime * speedScaled * length) ;

                // Translate the view platform.
                translateTransform(viewPlatformToVworld, v3d) ;
                targetTG.setTransform(viewPlatformToVworld) ;

                if (sensor6D != null) {
                    // Apply the translation to trackerToVworld.
                    translateTransform(trackerToVworld, v3d) ;
                }

                if (sensor6D != null && readAction6D == ECHO) {
                    // Translate sensor echo to compensate for the new view
                    // platform movement.
                    translateTransform(sensorToVworld, v3d) ;
                    updateEcho(sensor6D, sensorToVworld) ;
                }
            }
            else {
                // Initialize action on next threshold crossing.
                if (isActive()) endAction(sensor6D) ;
            }
        }

        public void pressed(SensorEvent e) {
            initAction(sensor6D) ;
        }

        public void released(SensorEvent e) {
            if (isActive()) endAction(sensor6D) ;
        }

        public void dragged(SensorEvent e) {
            read(e) ;
        }
    }

    /**
     * Implements a 2D valuator listener that scales the view platform.
     * Pushing the valuator forwards gives the appearance of the virtual world
     * increasing in size, while pushing the valuator backwards makes the
     * virtual world appear to shrink.  The X and Y values from the valuator
     * should have a continuous range from -1.0 to +1.0, although the scale
     * speed can be adjusted to compensate for a different range.
     * <p>
     * This listener will work in conjunction with a 6DOF sensor if supplied
     * in the constructor.  If <code>setTransformCenterSource</code> has been
     * called with the value <code>HOTSPOT</code>, then scaling is about the
     * 6DOF sensor's hotspot; otherwise, the scaling center is the value set
     * by <code>setTransformCenter</code>.
     * 
     * @see #setReadAction2D
     * @see #setButtonAction2D
     * @see #setScaleSpeed
     * @see #setTransformCenter
     * @see #setThreshold2D
     * @see #setMatrixIndices2D
     */
    public class ScaleListener2D extends ListenerBase {
        private Sensor sensor2D, sensor6D ;
	private double[] m = new double[16] ;
        private Point3d center = new Point3d() ;
        private Transform3D t3d = new Transform3D() ;
        private Transform3D sensor2DRead = new Transform3D() ;

        protected void initAction(Sensor s) {
            super.initAction(s) ;
            if (s != null && readAction6D == ECHO) {
                // Disable the 6DOF echo.  It will be updated in this action.
                eventAgent.removeSensorReadListener(s, echoReadListener6D) ;
            }
        }

        protected void endAction(Sensor s) {
            super.endAction(s) ;
            conditionViewScale(viewPlatformToVworld) ;
            if (s != null && readAction6D == ECHO) {
                // Enable the 6DOF sensor echo.
                eventAgent.addSensorReadListener(s, echoReadListener6D) ;
            }
        }

        /**
         * Construct an instance of this class with the specified sensors.
         * 
         * @param sensor2D the 2D valuator whose Y value drive the scaling
         * @param sensor6D the 6DOF sensor to use if the rotation/scale center
         *  source is <code>HOTSPOT</code>; may be <code>null</code>
         */
        public ScaleListener2D(Sensor sensor2D, Sensor sensor6D) {
            this.sensor2D = sensor2D ;
            this.sensor6D = sensor6D ;
        }

        public void read(SensorEvent e) {
            sensor2D.getRead(sensor2DRead) ;
            sensor2DRead.get(m) ;

            if (m[y2D] > threshold2D || m[y2D] < -threshold2D) {
                // Initialize action on threshold crossing.
                if (!isActive()) initAction(sensor6D) ;

                if (sensor6D != null) {
                    // To avoid echo frame lag, compute sensorToVworld based
                    // on computed trackerToVworld.  getSensorToVworld() isn't
                    // current for this frame.
                    sensor6D.getRead(sensorToTracker) ;
                    sensorToVworld.mul(trackerToVworld, sensorToTracker) ;
                }

                // Get the scale center.
                if (sensor6D != null && transformCenterSource == HOTSPOT) {
                    sensor6D.getHotspot(center) ;
                    sensorToVworld.transform(center) ;
                }
                else {
                    center.set(transformCenter) ;
                }

                // Compute incremental scale for this frame.
                double frameTime = 1.0 ;
                if (scaleTimeBase == PER_SECOND)
                    frameTime = (e.getTime() - e.getLastTime()) / 1e9 ;

                // Map range:       [-1.0 .. 0 .. 1.0] to:
                // [scaleSpeed**frameTime .. 1 .. 1.0/(scaleSpeed**frameTime)]
                double scale = Math.pow(scaleSpeed, (-m[y2D]*frameTime)) ;

                // Apply the scale to the view platform.
                t3d.set(scale) ;
                transformAboutCenter(viewPlatformToVworld, center, t3d) ;

                // Incremental scaling at the extremes can lead to numerical
                // instability, so catch BadTransformException to prevent the
                // behavior thread from being killed.  Using a cumulative
                // scale matrix avoids this problem to a better extent, but
                // causes the 6DOF sensor hotspot center to jitter
                // excessively.
                try {
                    targetTG.setTransform(viewPlatformToVworld) ;
                }
                catch (BadTransformException bt) {
                    conditionViewScale(viewPlatformToVworld) ;
                }

                if (sensor6D != null) {
                    // Apply the scale to trackerToVworld.
                    transformAboutCenter(trackerToVworld, center, t3d) ;
                }

                if (sensor6D != null && readAction6D == ECHO) {
                    // Scale sensor echo to compensate for the new view
                    // platform scale.
                    transformAboutCenter(sensorToVworld, center, t3d) ;
                    updateEcho(sensor6D, sensorToVworld) ;
                }
            }
            else {
                // Initialize action on next threshold crossing.
                if (isActive()) endAction(sensor6D) ;
            }
        }

        public void pressed(SensorEvent e) {
            initAction(sensor6D) ;
        }

        public void released(SensorEvent e) {
            if (isActive()) endAction(sensor6D) ;
        }

        public void dragged(SensorEvent e) {
            read(e) ;
        }
    }

    /**
     * Resets the view back to the home transform when a specified number of
     * buttons are down simultaneously.
     * 
     * @see #setResetViewButtonCount6D
     * @see ViewPlatformBehavior#setHomeTransform
     *  ViewPlatformBehavior.setHomeTransform
     */
    public class ResetViewListener extends SensorInputAdaptor {
        private int resetCount ;
        private int[] buttonState = null ;
        private boolean goHomeNextRead = false ;

        /**
         * Creates a sensor listener that resets the view when the specified
         * number of buttons are down simultaneously.
         *
         * @param s the sensor to listen to
         * @param count the number of buttons that must be down simultaneously
         */
        public ResetViewListener(Sensor s, int count) {
            resetCount = count ;
            buttonState = new int[s.getSensorButtonCount()] ;
        }

        public void pressed(SensorEvent e) {
            int count = 0 ;
            e.getButtonState(buttonState) ;
            for (int i = 0 ; i < buttonState.length ; i++)
                if (buttonState[i] == 1) count++ ;

            if (count >= resetCount)
                // Ineffectual to reset immediately while other listeners may
                // be setting the view transform.
                goHomeNextRead = true ;
        }

        public void read(SensorEvent e) {
            if (goHomeNextRead) {
                goHome() ;
                goHomeNextRead = false ;
            }
        }
    }

    /**
     * Updates the echo position and orientation.  The echo is placed at the
     * sensor hotspot position if applicable.  This implementation assumes the
     * hotspot position and orientation have been incorporated into the echo
     * geometry.
     * 
     * @param sensor the sensor to be echoed
     * @param sensorToVworld transform from sensor coordinates to virtual
     *  world coordinates
     * @see #setEchoType
     * @see #setEchoSize
     * @see #setReadAction6D
     * @see SensorGnomonEcho
     * @see SensorBeamEcho
     */
    protected void updateEcho(Sensor sensor, Transform3D sensorToVworld) {
        echoTransformGroup.setTransform(sensorToVworld) ;
    }

    /**
     * Property which sets a 6DOF sensor for manipulating the view platform.
     * This sensor must generate 6 degree of freedom orientation and position
     * data in physical meters relative to the sensor's tracker base.
     * <p>
     * This property is set in the configuration file.  The first command form
     * assumes that a <code>ViewingPlatform</code> is being used and that the
     * sensor name can be looked up from a <code>ConfiguredUniverse</code>
     * reference retrieved from <code>ViewingPlatform.getUniverse</code>.  The
     * second form is preferred and accepts the Sensor reference directly.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * Sensor6D <i>&lt;sensorName&gt;</i>)
     * <p>
     * <b>Alternative Syntax:</b><br>(ViewPlatformBehaviorProperty
     * <i>&lt;name&gt;</i> Sensor6D (Sensor <i>&lt;sensorName&gt;</i>))
     * 
     * @param sensor array of length 1 containing a <code>String</code> or
     *  a <code>Sensor</code>
     */
    public void Sensor6D(Object[] sensor) {
	if (sensor.length != 1)
	    throw new IllegalArgumentException
		("Sensor6D requires a single name or Sensor instance") ;

	if (sensor[0] instanceof String)
	    sensor6DName = (String)sensor[0] ;
	else if (sensor[0] instanceof Sensor)
	    sensor6D = (Sensor)sensor[0] ;
	else
	    throw new IllegalArgumentException
		("Sensor6D must be a name or a Sensor instance") ;
    }
    
    /**
     * Returns a reference to the 6DOF sensor used for manipulating the view
     * platform.
     * 
     * @return the 6DOF sensor
     */
    public Sensor getSensor6D() {
        return sensor6D ;
    }

    /**
     * Property which sets a 2D sensor for manipulating the view platform.
     * This is intended to support devices which incorporate a separate 2D
     * valuator along with the 6DOF sensor.  The X and Y values from the
     * valuator should have a continuous range from -1.0 to +1.0, although
     * rotation, translation, and scaling speeds can be scaled to compensate
     * for a different range.  The X and Y values are found in the sensor's
     * read matrix at the indices specified by the
     * <code>MatrixIndices2D</code> property, with defaults of 3 and 7
     * (the X and Y translation components) respectively.
     * <p>
     * This property is set in the configuration file.  The first command form
     * assumes that a <code>ViewingPlatform</code> is being used and that the
     * sensor name can be looked up from a <code>ConfiguredUniverse</code>
     * reference retrieved from <code>ViewingPlatform.getUniverse</code>.  The
     * second form is preferred and accepts the Sensor reference directly.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * Sensor2D <i>&lt;sensorName&gt;</i>)
     * <p>
     * <b>Alternative Syntax:</b><br>(ViewPlatformBehaviorProperty
     * <i>&lt;name&gt;</i> Sensor2D (Sensor <i>&lt;sensorName&gt;</i>))
     * 
     * @param sensor array of length 1 containing a <code>String</code> or
     *  a <code>Sensor</code>
     */
    public void Sensor2D(Object[] sensor) {
	if (sensor.length != 1)
	    throw new IllegalArgumentException
		("Sensor2D requires a single name or Sensor instance") ;

	if (sensor[0] instanceof String)
	    sensor2DName = (String)sensor[0] ;
	else if (sensor[0] instanceof Sensor)
	    sensor2D = (Sensor)sensor[0] ;
	else
	    throw new IllegalArgumentException
		("Sensor2D must be a name or a Sensor instance") ;
    }

    /**
     * Returns a reference to the 2D valuator used for manipulating the view
     * platform.
     * 
     * @return the 2D valuator
     */
    public Sensor getSensor2D() {
        return sensor2D ;
    }

    /**
     * Property which sets a button action for the 6DOF sensor.  The choices
     * are <code>TranslateForward</code>, <code>TranslateBackward</code>,
     * <code>GrabView</code>, <code>RotateCCW</code>, <code>RotateCW</code>,
     * <code>ScaleUp</code>, <code>ScaleDown</code>, or <code>None</code>.  By
     * default, button 0 is bound to <code>GrabView</code>, button 1 is bound
     * to <code>TranslateForward</code>, and button 2 is bound to
     * <code>TranslateBackward</code>. If there are fewer than three buttons
     * available, then the default button actions with the lower button
     * indices have precedence.  A value of <code>None</code> indicates that
     * no default action is to be associated with the specified button.
     * <p>
     * <code>TranslateForward</code> moves the view platform forward along the
     * direction the sensor is pointing.  <code>TranslateBackward</code> does
     * the same, in the opposite direction.  <code>GrabView</code> directly
     * manipulates the view by moving it in inverse response to the sensor's
     * position and orientation.  <code>RotateCCW</code> and
     * <code>RotateCW</code> rotate about a Y axis, while <code>ScaleUp</code>
     * and <code>ScaleDown</code> scale the view platform larger and smaller.
     * <p>
     * Specifying a button index that is greater than that available with the
     * 6DOF sensor will result in an <code>ArrayOutOfBoundsException</code>
     * when the behavior is initialized or attached to a
     * <code>ViewingPlatform</code>.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ButtonAction6D <i>&lt;button index&gt;</i> 
     * [GrabView | TranslateForward | TranslateBackward | RotateCCW |
     * RotateCW | ScaleUp | ScaleDown | None])
     * 
     * @param action array of length 2 containing a <code>Double</code> and a
     *  <code>String</code>.
     * @see #setButtonAction6D
     * @see #Sensor6D Sensor6D
     * @see #TranslationSpeed TranslationSpeed
     * @see #AccelerationTime AccelerationTime
     * @see #ConstantSpeedTime ConstantSpeedTime
     * @see #FastSpeedFactor FastSpeedFactor 
     * @see #RotationSpeed RotationSpeed
     * @see #RotationCoords RotationCoords
     * @see #ScaleSpeed ScaleSpeed
     * @see #TransformCenterSource TransformCenterSource
     * @see #TransformCenter TransformCenter
     * @see GrabViewListener6D
     * @see TranslationListener6D
     * @see RotationListener6D
     * @see ScaleListener6D
     */
    public void ButtonAction6D(Object[] action) {
        if (! (action.length == 2 &&
               action[0] instanceof Double && action[1] instanceof String))
            throw new IllegalArgumentException
                ("\nButtonAction6D must be a number and a string") ;
        
        int button = ((Double)action[0]).intValue() ;
        String actionString = (String)action[1] ;

        if (actionString.equals("GrabView"))
            setButtonAction6D(button, GRAB_VIEW) ;
        else if (actionString.equals("TranslateForward"))
            setButtonAction6D(button, TRANSLATE_FORWARD) ;
        else if (actionString.equals("TranslateBackward"))
            setButtonAction6D(button, TRANSLATE_BACKWARD) ;
        else if (actionString.equals("RotateCCW"))
            setButtonAction6D(button, ROTATE_CCW) ;
        else if (actionString.equals("RotateCW"))
            setButtonAction6D(button, ROTATE_CW) ;
        else if (actionString.equals("ScaleUp"))
            setButtonAction6D(button, SCALE_UP) ;
        else if (actionString.equals("ScaleDown"))
            setButtonAction6D(button, SCALE_DOWN) ;
        else if (actionString.equals("None"))
            setButtonAction6D(button, NONE) ;
        else
            throw new IllegalArgumentException
                ("\nButtonAction6D must be GrabView, TranslateForward, " +
                 "TranslateBackward, RotateCCW, RotateCW, ScaleUp, " +
		 "ScaleDown, or None") ;
    }
    
    /**
     * Sets a button action for the 6DOF sensor.  The choices are
     * <code>TRANSLATE_FORWARD</code>, <code>TRANSLATE_BACKWARD</code>,
     * <code>GRAB_VIEW</code>, <code>ROTATE_CCW</code>,
     * <code>ROTATE_CW</code>, <code>SCALE_UP</code>, <code>SCALE_DOWN</code>,
     * or <code>NONE</code>.  By default, button 0 is bound to
     * <code>GRAB_VIEW</code>, button 1 is bound to
     * <code>TRANSLATE_FORWARD</code>, and button 2 is bound to
     * <code>TRANSLATE_BACKWARD</code>.  If there are fewer than three buttons
     * available, then the default button actions with the lower button
     * indices have precedence.  A value of <code>NONE</code> indicates that
     * no default action is to be associated with the specified button.
     * <p>
     * <code>TRANSLATE_FORWARD</code> moves the view platform forward along
     * the direction the sensor is pointing.  <code>TRANSLATE_BACKWARD</code>
     * does the same, in the opposite direction.  <code>GRAB_VIEW</code>
     * directly manipulates the view by moving it in inverse response to the
     * sensor's position and orientation.  <code>ROTATE_CCW</code> and
     * <code>ROTATE_CW</code> rotate about a Y axis, while
     * <code>SCALE_UP</code> and <code>SCALE_DOWN</code> scale the view
     * platform larger and smaller.
     * <p>
     * Specifying a button index that is greater that that available with the
     * 6DOF sensor will result in an <code>ArrayOutOfBoundsException</code>
     * when the behavior is initialized or attached to a
     * <code>ViewingPlatform</code>.
     * <p>
     * This method only configures the button listeners pre-defined by
     * this behavior.  For complete control over the button actions, access
     * the <code>SensorEventAgent</code> used by this behavior directly.  
     * 
     * @param button index of the button to bind
     * @param action either <code>TRANSLATE_FORWARD</code>,
     *  <code>TRANSLATE_BACKWARD</code>, <code>GRAB_VIEW</code>,
     *  <code>ROTATE_CCW</code>, <code>ROTATE_CW<code>, </code>SCALE_UP</code>,
     *  <code>SCALE_DOWN</code>, or <code>NONE</code>
     * @see #setTranslationSpeed
     * @see #setAccelerationTime
     * @see #setConstantSpeedTime
     * @see #setFastSpeedFactor
     * @see #setRotationSpeed
     * @see #setRotationCoords
     * @see #setScaleSpeed
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #getSensorEventAgent
     * @see GrabViewListener6D
     * @see TranslationListener6D
     * @see RotationListener6D
     * @see ScaleListener6D
     */
    public synchronized void setButtonAction6D(int button, int action) {
        if (! (action == TRANSLATE_FORWARD || action == TRANSLATE_BACKWARD ||
               action == GRAB_VIEW || action == ROTATE_CCW ||
	       action == ROTATE_CW || action == SCALE_UP ||
	       action == SCALE_DOWN || action == NONE))
            throw new IllegalArgumentException
                ("\naction must be TRANSLATE_FORWARD, TRANSLATE_BACKWARD, " +
                 "GRAB_VIEW, ROTATE_CCW, ROTATE_CW, SCALE_UP, SCALE_DOWN, " +
		 "or NONE") ;

        while (button >= buttonActions6D.size()) {
            buttonActions6D.add(null) ;
        }
        buttonActions6D.set(button, new Integer(action)) ;
    }


    /**
     * Gets the action associated with the specified button on the 6DOF sensor.
     * 
     * @return the action associated with the button
     */
    public int getButtonAction6D(int button) {
        if (button >= buttonActions6D.size())
            return NONE ;

        Integer i = (Integer)buttonActions6D.get(button) ;
        if (i == null)
            return NONE ;
        else
            return i.intValue() ;
    }

    /**
     * Property which sets the action to be bound to 2D valuator reads.  This
     * action will be performed on each frame whenever no button actions have
     * been invoked and the valuator read value is greater than the threshold
     * range specified by the <code>Threshold2D</code> property.
     * <p>
     * The X and Y values from the valuator should have a continuous range
     * from -1.0 to +1.0, although speeds can be scaled to compensate for a
     * different range.  The X and Y values are found in the sensor's read
     * matrix at the indices specified by <code>MatrixIndices2D</code>, with
     * defaults of 3 and 7 respectively.
     * <p>
     * The default property value of <code>Rotation</code> rotates the view
     * platform in the direction the valuator is pushed.  The rotation
     * coordinate system is set by the <code>RotationCoords</code> property,
     * with a default of <code>Sensor</code>.  The rotation occurs about a
     * point in the virtual world set by the
     * <code>TransformCenterSource</code> and <code>TransformCenter</code>
     * properties, with the default set to rotate about the hotspot of a 6DOF
     * sensor, if one is available, or about the origin of the virtual world
     * if not.  The rotation speed is scaled by the valuator read value up to
     * the maximum speed set with the <code>RotationSpeed</code> property; the
     * default is 180 degrees per second.
     * <p>
     * A property value of <code>Translation</code> moves the view platform in
     * the direction the valuator is pushed.  The translation occurs along the
     * X and Z basis vectors of either a 6DOF sensor or the view platform if a
     * 6DOF sensor is not specified.  The translation speed is scaled by the
     * valuator read value up to a maximum set by the product of the
     * <code>TranslationSpeed</code> and <code>FastSpeedFactor</code> property
     * values.
     * <p>
     * If this property value is to <code>Scale</code>, then the view platform
     * is scaled smaller or larger when the valuator is pushed forward or
     * backward.  The scaling occurs about a point in the virtual world set by
     * the <code>TransformCenterSource</code> and <code>TransformCenter</code>
     * properties.  The scaling speed is set with the <code>ScaleSpeed</code>
     * property, with a default scale factor of 2.0 per second at the extreme
     * negative range of -1.0, and a factor of 0.5 per second at the extreme
     * positive range of +1.0.
     * <p>
     * A value of <code>None</code> prevents <code>Rotation</code> from being
     * bound to the 2D valuator reads.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ReadAction2D [Rotation | Translation | Scale | None])
     * 
     * @param action array of length 1 containing a <code>String</code>
     * @see #setReadAction2D
     * @see #RotationCoords RotationCoords
     * @see #RotationSpeed RotationSpeed
     * @see #TransformCenterSource TransformCenterSource
     * @see #TransformCenter TransformCenter
     * @see #TranslationSpeed TranslationSpeed
     * @see #FastSpeedFactor FastSpeedFactor
     * @see #ScaleSpeed ScaleSpeed
     * @see #MatrixIndices2D MatrixIndices2D
     * @see RotationListener2D
     * @see TranslationListener2D
     * @see ScaleListener2D
     */
    public void ReadAction2D(Object[] action) {
        if (! (action.length == 1 && action[0] instanceof String))
            throw new IllegalArgumentException
                ("\nReadAction2D must be a String") ;
        
        String actionString = (String)action[0] ;

        if (actionString.equals("Rotation"))
            setReadAction2D(ROTATION) ;
        else if (actionString.equals("Translation"))
            setReadAction2D(TRANSLATION) ;
        else if (actionString.equals("Scale"))
            setReadAction2D(SCALE) ;
        else if (actionString.equals("None"))
            setReadAction2D(NONE) ;
        else
            throw new IllegalArgumentException
                ("\nReadAction2D must be Rotation, Translation, Scale, " +
                 "or None") ;
    }

    /**
     * Sets the action to be bound to 2D valuator reads.  This action will be
     * performed on each frame whenever no button actions have been invoked
     * and the valuator read value is greater than the threshold range
     * specified by <code>setThreshold2D</code>.
     * <p>
     * The X and Y values from the valuator should have a continuous range
     * from -1.0 to +1.0, although speeds can be scaled to compensate for a
     * different range.  The X and Y values are found in the sensor's read
     * matrix at the indices specified by the <code>setMatrixIndices2D</code>
     * method, with defaults of 3 and 7 respectively.
     * <p>
     * The default action of <code>ROTATION</code> rotates the view platform
     * in the direction the valuator is pushed.  The rotation coordinate
     * system is set by <code>setRotationCoords</code>, with a default of
     * <code>SENSOR</code>.  The rotation occurs about a point in the virtual
     * world set by <code>setTransformCenterSource</code> and
     * <code>setTransformCenter</code>, with the default set to rotate about
     * the hotspot of a 6DOF sensor, if one is available, or about the origin
     * of the virtual world if not.  The rotation speed is scaled by the
     * valuator read value up to the maximum speed set with
     * <code>setRotationSpeed</code>; the default is 180 degrees per second.
     * <p>
     * A value of <code>TRANSLATION</code> moves the view platform in the
     * direction the valuator is pushed.  The translation occurs along the X
     * and Z basis vectors of either a 6DOF sensor or the view platform if a
     * 6DOF sensor is not specified.  The translation speed is scaled by the
     * valuator read value up to a maximum set by the product of the
     * <code>setTranslationSpeed</code> and <code>setFastSpeedFactor</code>
     * values.
     * <p>
     * If the value is to <code>SCALE</code>, then the view platform is scaled
     * smaller or larger when the valuator is pushed forward or backward.  The
     * scaling occurs about a point in the virtual world set by
     * <code>setTransformCenterSource</code> and
     * <code>setTransformCenter</code>.  The scaling speed is set with
     * <code>setScaleSpeed</code>, with a default scale factor of 2.0 per
     * second at the extreme negative range of -1.0, and a factor of 0.5 per
     * second at the extreme positive range of +1.0.
     * <p>
     * A value of <code>NONE</code> prevents <code>ROTATION</code> from being
     * bound by default to the 2D valuator reads.
     * <p>
     * This method only configures the read listeners pre-defined by
     * this behavior.  For complete control over the read actions, access
     * the <code>SensorEventAgent</code> used by this behavior directly.  
     * 
     * @param action either <code>ROTATION</code>, <code>TRANSLATION</code>,
     * <code>SCALE</code>, or <code>NONE</code>
     * @see #setRotationCoords
     * @see #setRotationSpeed
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #setTranslationSpeed
     * @see #setFastSpeedFactor
     * @see #setScaleSpeed
     * @see #setMatrixIndices2D
     * @see #getSensorEventAgent
     * @see RotationListener2D
     * @see TranslationListener2D
     * @see ScaleListener2D
     */
    public void setReadAction2D(int action) {
        if (! (action == ROTATION || action == TRANSLATION ||
               action == SCALE || action == NONE))
            throw new IllegalArgumentException
                ("\nReadAction2D must be ROTATION, TRANSLATION, SCALE, " +
                 "or NONE") ;

        this.readAction2D = action ;
    }

    /**
     * Gets the configured 2D valuator read action.
     * 
     * @return the action associated with the sensor read
     */
    public int getReadAction2D() {
        if (readAction2D == UNSET)
            return NONE ;
        else
            return readAction2D ;
    }

    /**
     * Property which sets a button action for the 2D valuator.  The possible
     * values are <code>Rotation</code>, <code>Translation</code>,
     * <code>Scale</code>, or <code>None</code>, with a default of
     * <code>None</code>.  These actions are the same as those for
     * <code>ReadAction2D</code>.
     * <p>
     * Specifying a button index that is greater that that available with the
     * 2D valuator will result in an <code>ArrayOutOfBoundsException</code>
     * when the behavior is initialized or attached to a
     * <code>ViewingPlatform</code>.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ButtonAction2D <i>&lt;button index&gt;</i> 
     * [Rotation | Translation | Scale | None])
     * 
     * @param action array of length 2 containing a <code>Double</code> and a
     *  <code>String</code>.
     * @see #setButtonAction2D
     * @see #ReadAction2D ReadAction2D
     * @see #RotationCoords RotationCoords
     * @see #RotationSpeed RotationSpeed
     * @see #TransformCenterSource TransformCenterSource
     * @see #TransformCenter TransformCenter
     * @see #TranslationSpeed TranslationSpeed
     * @see #FastSpeedFactor FastSpeedFactor
     * @see #ScaleSpeed ScaleSpeed
     * @see #MatrixIndices2D MatrixIndices2D
     * @see RotationListener2D
     * @see TranslationListener2D
     * @see ScaleListener2D
     */
    public void ButtonAction2D(Object[] action) {
        if (! (action.length == 2 &&
               action[0] instanceof Double && action[1] instanceof String))
            throw new IllegalArgumentException
                ("\nButtonAction2D must be a number and a string") ;
        
        int button = ((Double)action[0]).intValue() ;
        String actionString = (String)action[1] ;

        if (actionString.equals("Rotation"))
            setButtonAction2D(button, ROTATION) ;
        else if (actionString.equals("Translation"))
            setButtonAction2D(button, TRANSLATION) ;
        else if (actionString.equals("Scale"))
            setButtonAction2D(button, SCALE) ;
        else if (actionString.equals("None"))
            setButtonAction2D(button, NONE) ;
        else
            throw new IllegalArgumentException
                ("\nButtonAction2D must be Rotation, Translation, Scale " +
                 "or None") ;
    }
    
    /**
     * Sets a button action for the 2D valuator.  The possible values are
     * <code>ROTATION</code>, <code>TRANSLATION</code>, <code>SCALE</code>, or
     * <code>NONE</code>, with a default of <code>NONE</code>.  These actions
     * are the same as those for <code>setReadAction2D</code>.
     * <p>
     * Specifying a button index that is greater that that available with the
     * 2D valuator will result in an <code>ArrayOutOfBoundsException</code>
     * when the behavior is initialized or attached to a
     * <code>ViewingPlatform</code>.
     * <p>
     * This method only configures the button listeners pre-defined by
     * this behavior.  For complete control over the button actions, access
     * the <code>SensorEventAgent</code> used by this behavior directly.  
     * 
     * @param button index of the button to bind
     * @param action either <code>ROTATION</code>, <code>TRANSLATION</code>,
     *  <code>SCALE</code>, or <code>NONE</code>
     * @see #setReadAction2D
     * @see #setRotationCoords
     * @see #setRotationSpeed
     * @see #setTransformCenterSource
     * @see #setTransformCenter
     * @see #setTranslationSpeed
     * @see #setFastSpeedFactor
     * @see #setScaleSpeed
     * @see #setMatrixIndices2D
     * @see #getSensorEventAgent
     * @see RotationListener2D
     * @see TranslationListener2D
     * @see ScaleListener2D
     */
    public synchronized void setButtonAction2D(int button, int action) {
        if (! (action == ROTATION || action == TRANSLATION ||
               action == SCALE || action == NONE))
            throw new IllegalArgumentException
                ("\naction must be ROTATION, TRANSLATION, SCALE, or NONE") ;

        while (button >= buttonActions2D.size()) {
            buttonActions2D.add(null) ;
        }
        buttonActions2D.set(button, new Integer(action)) ;
    }


    /**
     * Gets the action associated with the specified button on the 2D valuator.
     * 
     * @return the action associated with the button
     */
    public int getButtonAction2D(int button) {
        if (button >= buttonActions2D.size())
            return NONE ;

        Integer i = (Integer)buttonActions2D.get(button) ;
        if (i == null)
            return NONE ;
        else
            return i.intValue() ;
    }

    /**
     * Property which sets the action to be bound to 6DOF sensor reads.  This
     * action will be performed every frame whenever a button action has not
     * been invoked.
     * <p>
     * The default is <code>Echo</code>, which displays a geometric
     * representation of the sensor's current position and orientation in the
     * virtual world.  A value of <code>None</code> indicates that no default
     * action is to be applied to the sensor's read.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ReadAction6D [Echo | None])
     * 
     * @param action array of length 1 containing a <code>String</code>
     * @see #setReadAction6D
     * @see EchoReadListener6D
     */
    public void ReadAction6D(Object[] action) {
        if (! (action.length == 1 && action[0] instanceof String))
            throw new IllegalArgumentException
                ("\nReadAction6D must be a String") ;
        
        String actionString = (String)action[0] ;

        if (actionString.equals("Echo"))
            setReadAction6D(ECHO) ;
        else if (actionString.equals("None"))
            setReadAction6D(NONE) ;
        else
            throw new IllegalArgumentException
                ("\nReadAction6D must be Echo or None") ;
    }

    /**
     * Sets the action to be bound to 6DOF sensor reads.  This action will be
     * performed every frame whenever a button action has not been invoked.
     * <p>
     * The default is <code>ECHO</code>, which displays a geometric
     * representation of the sensor's current position and orientation in the
     * virtual world.  A value of <code>NONE</code> indicates that no default
     * action is to be associated with the sensor's read.
     * <p>
     * This method only configures the read listeners pre-defined by
     * this behavior.  For complete control over the read actions, access
     * the <code>SensorEventAgent</code> used by this behavior directly.  
     * 
     * @param action either <code>ECHO</code> or <code>NONE</code>
     * @see EchoReadListener6D
     * @see #getSensorEventAgent
     */
    public void setReadAction6D(int action) {
        if (! (action == ECHO || action == NONE))
            throw new IllegalArgumentException
                ("\naction must be ECHO or NONE") ;

        this.readAction6D = action ;
    }

    /**
     * Gets the configured 6DOF sensor read action.
     * 
     * @return the configured 6DOF sensor read action
     */
    public int getReadAction6D() {
        if (readAction6D == UNSET)
            return NONE ;
        else
            return readAction6D ;
    }

    /**
     * Property which sets the normal translation speed.  The default is 0.1
     * meters/second in physical units.  This property is set in the
     * configuration file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * TranslationSpeed <i>&lt;speed&gt;</i> [PhysicalMeters | VirtualUnits]
     * [PerFrame | PerSecond])
     * 
     * @param speed array of length 3; first element is a <code>Double</code>
     *  for the speed, the second is a <code>String</code> for the units, and
     *  the third is a <code>String</code> for the time base
     * @see #setTranslationSpeed
     */
    public void TranslationSpeed(Object[] speed) {
        if (! (speed.length == 3 && speed[0] instanceof Double &&
               speed[1] instanceof String && speed[2] instanceof String))
            throw new IllegalArgumentException
                ("\nTranslationSpeed must be number, units, and time base") ;
        
        double v = ((Double)speed[0]).doubleValue() ;
        String unitsString = (String)speed[1] ;
        String timeBaseString = (String)speed[2] ;
        int units, timeBase ;

        if (unitsString.equals("PhysicalMeters"))
            units = PHYSICAL_METERS ;
        else if (unitsString.equals("VirtualUnits"))
            units = VIRTUAL_UNITS ;
        else
            throw new IllegalArgumentException
                ("\nTranslationSpeed units must be " +
                 "PhysicalMeters or VirtualUnits") ;

        if (timeBaseString.equals("PerFrame"))
            timeBase = PER_FRAME ;
        else if (timeBaseString.equals("PerSecond"))
            timeBase = PER_SECOND ;
        else
            throw new IllegalArgumentException
                ("\ntime base must be PerFrame or PerSecond") ;

        setTranslationSpeed(v, units, timeBase) ;
    }

    /**
     * Sets the normal translation speed.  The default is 0.1 physical
     * meters/second.
     * 
     * @param speed how fast to translate
     * @param units either <code>PHYSICAL_METERS</code> or
     *  <code>VIRTUAL_UNITS</code>
     * @param timeBase either <code>PER_SECOND</code> or
     *  <code>PER_FRAME</code>
     */
    public void setTranslationSpeed(double speed, int units, int timeBase) {
        this.translationSpeed = speed ;

        if (units == PHYSICAL_METERS || units == VIRTUAL_UNITS)
            this.translationUnits = units ;
        else
            throw new IllegalArgumentException
                ("\ntranslation speed units must be " +
                 "PHYSICAL_METERS or VIRTUAL_UNITS") ;

        if (timeBase == PER_FRAME || timeBase == PER_SECOND)
            this.translationTimeBase = timeBase ;
        else
            throw new IllegalArgumentException
                ("\ntranslation time base must be PER_FRAME or PER_SECOND") ;
    }

    /**
     * Gets the normal speed at which to translate the view platform.
     * 
     * @return the normal translation speed
     */
    public double getTranslationSpeed() {
        return translationSpeed ;
    }

    /**
     * Gets the translation speed units.
     * 
     * @return the translation units
     */
    public int getTranslationUnits() {
        return translationUnits ;
    }

    /**
     * Gets the time base for translation speed.
     * 
     * @return the translation time base
     */
    public int getTranslationTimeBase() {
        return translationTimeBase ;
    }

    /**
     * Property which sets the time interval for accelerating to the
     * translation, rotation, or scale speeds and for transitioning between
     * the normal and fast translation speeds.  The default is 1 second.  This
     * property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.<p>
     * 
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * AccelerationTime <i>&lt;seconds&gt;</i>)
     * 
     * @param time array of length 1 containing a <code>Double</code>
     * @see #setAccelerationTime
     */
    public void AccelerationTime(Object[] time) {
        if (! (time.length == 1 && time[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nAccelerationTime must be a number") ;
        
        setAccelerationTime(((Double)time[0]).doubleValue()) ;
    }

    /**
     * Sets the time interval for accelerating to the translation, rotation,
     * or scale speeds and for transitioning between the normal and fast
     * translation speeds.  The default is 1 second.
     * 
     * @param time number of seconds to accelerate to normal or fast speed
     */
    public void setAccelerationTime(double time) {
        this.accelerationTime = time ;
    }

    /**
     * Gets the time interval for accelerating to normal speed and for
     * transitioning between the normal and fast translation speeds.
     * 
     * @return the acceleration time
     */
    public double getAccelerationTime() {
        return accelerationTime ;
    }

    /**
     * Property which sets the time interval for which the translation occurs
     * at the normal speed.  The default is 8 seconds.  This property is set
     * in the configuration file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ConstantSpeedTime <i>&lt;seconds&gt;</i>)
     * 
     * @param time array of length 1 containing a <code>Double</code>
     * @see #setConstantSpeedTime
     */
    public void ConstantSpeedTime(Object[] time) {
        if (! (time.length == 1 && time[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nConstantSpeedTime must be a number") ;
        
        setConstantSpeedTime(((Double)time[0]).doubleValue()) ;
    }

    /**
     * Sets the time interval for which the translation occurs at the normal
     * speed.  The default is 8 seconds.
     * 
     * @param time number of seconds to translate at a constant speed
     */
    public void setConstantSpeedTime(double time) {
        this.constantSpeedTime = time ;
    }

    /**
     * Gets the time interval for which the translation occurs at the
     * normal speed.
     * 
     * @return the constant speed time
     */
    public double getConstantSpeedTime() {
        return constantSpeedTime ;
    }

    /**
     * Property which sets the fast translation speed factor.  The default is
     * 10 times the normal speed.  This property is set in the configuration
     * file read by </code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * FastSpeedFactor <i>&lt;factor&gt;</i>)
     * 
     * @param factor array of length 1 containing a <code>Double</code>
     * @see #setFastSpeedFactor
     */
    public void FastSpeedFactor(Object[] factor) {
        if (! (factor.length == 1 && factor[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nFastSpeedFactor must be a number") ;
        
        setFastSpeedFactor(((Double)factor[0]).doubleValue()) ;
    }

    /**
     * Sets the fast translation speed factor.  The default is 10 times the
     * normal speed.
     * 
     * @param factor scale by which the normal translation speed is multiplied
     */
    public void setFastSpeedFactor(double factor) {
        this.fastSpeedFactor = factor ;
    }

    /**
     * Gets the factor by which the normal translation speed is multiplied
     * after the constant speed time interval.
     * 
     * @return the fast speed factor
     */
    public double getFastSpeedFactor() {
        return fastSpeedFactor ;
    }

    /**
     * Property which sets the threshold for 2D valuator reads.  The default
     * is 0.0.  It can be set higher to handle noisy valuators.  This property
     * is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * Threshold2D <i>&lt;threshold&gt;</i>)
     * 
     * @param threshold array of length 1 containing a <code>Double</code>
     * @see #setThreshold2D
     */
    public void Threshold2D(Object[] threshold) {
        if (! (threshold.length == 1 && threshold[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nThreshold2D must be a number") ;

        setThreshold2D(((Double)threshold[0]).doubleValue()) ;
    }

    /**
     * Sets the threshold for 2D valuator reads.  The default is 0.0.  It can
     * be set higher to handle noisy valuators.
     * 
     * @param threshold if the absolute values of both the X and Y valuator
     *  reads are less than this value then the values are ignored
     */
    public void setThreshold2D(double threshold) {
        this.threshold2D = threshold ;
    }

    /**
     * Gets the 2D valuator threshold.
     * 
     * @return the threshold value
     */
    public double getThreshold2D() {
        return threshold2D ;
    }

    /**
     * Property which specifies where to find the X and Y values in the matrix
     * read generated by a 2D valuator.  The defaults are along the
     * translation components of the matrix, at indices 3 and 7.  This
     * property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * MatrixIndices2D <i>&lt;X index&gt; &lt;Y index&gt;</i>)
     * 
     * @param indices array of length 2 containing <code>Doubles</code>
     * @see #setMatrixIndices2D
     */
    public void MatrixIndices2D(Object[] indices) {
        if (! (indices.length == 2 &&
               indices[0] instanceof Double && indices[1] instanceof Double))
            throw new IllegalArgumentException
                ("\nMatrixIndices2D must be a numbers") ;
        
        setMatrixIndices2D(((Double)indices[0]).intValue(),
                           ((Double)indices[1]).intValue()) ;
    }
    
    /**
     * Specifies where to find the X and Y values in the matrix read generated
     * by a 2D valuator.  The defaults are along the translation components of
     * the matrix, at indices 3 and 7.
     * 
     * @param xIndex index of the X valuator value
     * @param yIndex index of the Y valuator value
     */
    public void setMatrixIndices2D(int xIndex, int yIndex) {
        this.x2D = xIndex ;
        this.y2D = yIndex ;
    }

    /**
     * Gets the index where the X value of a 2D valuator read matrix can be
     * found.
     * 
     * @return the X index in the read matrix
     */
    public int getMatrixXIndex2D() {
        return x2D ;
    }

    /**
     * Gets the index where the Y value of a 2D valuator read matrix can be
     * found.
     * 
     * @return the Y index in the read matrix
     */
    public int getMatrixYIndex2D() {
        return y2D ;
    }

    /**
     * Property which sets the rotation speed.  The default is 180
     * degrees/second.  This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * RotationSpeed <i>&lt;speed&gt;</i> [Degrees | Radians]
     * [PerFrame | PerSecond])
     * 
     * @param speed array of length 3; first element is a <code>Double</code>
     *  for the speed, the second is a <code>String</code> for the units, and
     *  the third is a <code>String</code> for the time base
     * @see #setRotationSpeed
     */
    public void RotationSpeed(Object[] speed) {
        if (! (speed.length == 3 && speed[0] instanceof Double &&
               speed[1] instanceof String && speed[2] instanceof String))
            throw new IllegalArgumentException
                ("\nRotationSpeed must be number, units, and time base") ;
        
        double v = ((Double)speed[0]).doubleValue() ;
        String unitsString = (String)speed[1] ;
        String timeBaseString = (String)speed[2] ;
        int units, timeBase ;

        if (unitsString.equals("Degrees"))
            units = DEGREES ;
        else if (unitsString.equals("Radians"))
            units = RADIANS ;
        else
            throw new IllegalArgumentException
                ("\nRotationSpeed units must be Degrees or Radians") ;

        if (timeBaseString.equals("PerFrame"))
            timeBase = PER_FRAME ;
        else if (timeBaseString.equals("PerSecond"))
            timeBase = PER_SECOND ;
        else
            throw new IllegalArgumentException
                ("\nRotationSpeed time base must be PerFrame or PerSecond") ;

        setRotationSpeed(v, units, timeBase) ;
    }

    /**
     * Sets the rotation speed.  The default is 180 degrees/second.
     * 
     * @param speed how fast to rotate
     * @param units either <code>DEGREES</code> or <code>RADIANS</code>
     * @param timeBase either <code>PER_SECOND</code> or <code>PER_FRAME</code>
     */
    public void setRotationSpeed(double speed, int units, int timeBase) {
        this.rotationSpeed = speed ;

        if (units == DEGREES || units == RADIANS)
            this.rotationUnits = units ;
        else
            throw new IllegalArgumentException
                ("\nrotation speed units must be DEGREES or RADIANS") ;

        if (timeBase == PER_FRAME || timeBase == PER_SECOND)
            this.rotationTimeBase = timeBase ;
        else
            throw new IllegalArgumentException
                ("\nrotation time base must be PER_FRAME or PER_SECOND") ;
    }

    /**
     * Gets the rotation speed.
     *
     * @return the rotation speed
     */
    public double getRotationSpeed() {
        return rotationSpeed ;
    }

    /**
     * Gets the rotation speed units
     * 
     * @return the rotation units
     */
    public int getRotationUnits() {
        return rotationUnits ;
    }

    /**
     * Gets the time base for rotation speed.
     * 
     * @return the rotation time base
     */
    public int getRotationTimeBase() {
        return rotationTimeBase ;
    }

    /**
     * Property which sets the rotation coordinate system.  The default is
     * <code>Sensor</code>, which means that the rotation axis is parallel to
     * the XY plane of the current orientation of a specified 6DOF sensor.  A
     * value of <code>ViewPlatform</code> means the rotation axis is parallel
     * to the XY plane of the view platform.  The latter is also the fallback
     * if a 6DOF sensor is not specified.  If the value is <code>Head</code>,
     * then the rotation occurs in head coordinates.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * RotationCoords [Sensor | ViewPlatform | Head])
     * 
     * @param coords array of length 1 containing a <code>String</code>
     * @see #setRotationCoords
     */
    public void RotationCoords(Object[] coords) {
        if (! (coords.length == 1 && coords[0] instanceof String))
            throw new IllegalArgumentException
                ("\nRotationCoords must be a String") ;
        
        String coordsString = (String)coords[0] ;

        if (coordsString.equals("Sensor"))
            setRotationCoords(SENSOR) ;
        else if (coordsString.equals("ViewPlatform"))
            setRotationCoords(VIEW_PLATFORM) ;
        else if (coordsString.equals("Head"))
            setRotationCoords(HEAD) ;
        else
            throw new IllegalArgumentException
                ("\nRotationCoords must be Sensor, ViewPlatform, or Head") ;
    }

    /**
     * Sets the rotation coordinate system.  The default is
     * <code>SENSOR</code>, which means that the rotation axis is parallel to
     * the XY plane of the current orientation of a specified 6DOF sensor.  A
     * value of <code>VIEW_PLATFORM</code> means the rotation axis is parallel
     * to the XY plane of the view platform.  The latter is also the fallback
     * if a 6DOF sensor is not specified.  If the value is <code>HEAD</code>,
     * then rotation occurs in head coordinates.
     * 
     * @param coords either <code>SENSOR</code>, <code>VIEW_PLATFORM</code>, or
     *  <code>HEAD</code>
     */
    public void setRotationCoords(int coords) {
        if (! (coords == SENSOR || coords == VIEW_PLATFORM || coords == HEAD))
            throw new IllegalArgumentException
                ("\nrotation coordinates be SENSOR, VIEW_PLATFORM, or HEAD") ;

        this.rotationCoords = coords ;
    }

    /**
     * Gets the rotation coordinate system.
     * 
     * @return the rotation coordinate system
     */
    public int getRotationCoords() {
        return rotationCoords ;
    }

    /**
     * Property which sets the scaling speed.  The default is 2.0 per second,
     * which means magnification doubles the apparent size of the virtual
     * world every second, and minification halves the apparent size of the
     * virtual world every second.  
     * <p>
     * The scaling applied with each frame is <code>Math.pow(scaleSpeed,
     * frameTime)</code>, where <code>frameTime</code> is the time in seconds
     * that the last frame took to render if the time base is
     * <code>PerSecond</code>, or 1.0 if the time base is
     * <code>PerFrame</code>.  If scaling is performed with the 2D valuator,
     * then the valuator's Y value as specified by
     * <code>MatrixIndices2D</code> is an additional scale applied to the
     * exponent.  If scaling is performed by the 6DOF sensor, then the scale
     * speed can be inverted with a negative exponent by using the appropriate
     * listener constructor flag.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ScaleSpeed <i>&lt;speed&gt;</i> [PerFrame | PerSecond])
     * 
     * @param speed array of length 2; first element is a <code>Double</code>
     *  for the speed, and the second is a <code>String</code> for the time
     *  base
     * @see #setScaleSpeed
     */
    public void ScaleSpeed(Object[] speed) {
        if (! (speed.length == 2 &&
               speed[0] instanceof Double && speed[1] instanceof String))
            throw new IllegalArgumentException
                ("\nScalingSpeed must be a number and a string") ;
        
        double v = ((Double)speed[0]).doubleValue() ;
        String timeBaseString = (String)speed[2] ;
        int timeBase ;

        if (timeBaseString.equals("PerFrame"))
            timeBase = PER_FRAME ;
        else if (timeBaseString.equals("PerSecond"))
            timeBase = PER_SECOND ;
        else
            throw new IllegalArgumentException
                ("\nScalingSpeed time base must be PerFrame or PerSecond") ;

        setScaleSpeed(v, timeBase) ;
    }

    /**
     * Sets the scaling speed.  The default is 2.0 per second, which means
     * magnification doubles the apparent size of the virtual world every
     * second, and minification halves the apparent size of the virtual world
     * every second.
     * <p>
     * The scaling applied with each frame is <code>Math.pow(scaleSpeed,
     * frameTime)</code>, where <code>frameTime</code> is the time in seconds
     * that the last frame took to render if the time base is
     * <code>PER_SECOND</code>, or 1.0 if the time base is
     * <code>PER_FRAME</code>.  If scaling is performed with the 2D valuator,
     * then the valuator's Y value as specified by
     * <code>setMatrixIndices2D</code> is an additional scale applied to the
     * exponent.  If scaling is performed by the 6DOF sensor, then the scale
     * speed can be inverted with a negative exponent by using the appropriate
     * listener constructor flag.
     * 
     * @param speed specifies the scale speed
     * @param timeBase either <code>PER_SECOND</code> or <code>PER_FRAME</code>
     */
    public void setScaleSpeed(double speed, int timeBase) {
        this.scaleSpeed = speed ;

        if (timeBase == PER_FRAME || timeBase == PER_SECOND)
            this.scaleTimeBase = timeBase ;
        else
            throw new IllegalArgumentException
                ("\nscaling time base must be PER_FRAME or PER_SECOND") ;
    }

    /**
     * Gets the scaling speed.
     * 
     * @return the scaling speed
     */
    public double getScaleSpeed() {
        return scaleSpeed ;
    }

    /**
     * Gets the time base for scaling speed.
     * 
     * @return the scaling time base
     */
    public int getScaleTimeBase() {
        return scaleTimeBase ;
    }

    /**
     * Property which sets the source of the center of rotation and scale.
     * The default is <code>Hotspot</code>, which means the center of rotation
     * or scale is a 6DOF sensor's current hotspot location.  The alternative
     * is <code>VworldFixed</code>, which uses the fixed virtual world
     * coordinates specified by the <code>TransformCenter</code> property as
     * the center.  The latter is also the fallback if a 6DOF sensor is not
     * specified.  This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * TransformCenterSource [Hotspot | VworldFixed])
     * 
     * @param source array of length 1 containing a <code>String</code>
     * @see #setTransformCenterSource
     */
    public void TransformCenterSource(Object[] source) {
        if (! (source.length == 1 && source[0] instanceof String))
            throw new IllegalArgumentException
                ("\nTransformCenterSource must be a String") ;
        
        String sourceString = (String)source[0] ;

        if (sourceString.equals("Hotspot"))
            setTransformCenterSource(HOTSPOT) ;
        else if (sourceString.equals("VworldFixed"))
            setTransformCenterSource(VWORLD_FIXED) ;
        else
            throw new IllegalArgumentException
                ("\nTransformCenterSource must be Hotspot or " +
                 "VworldFixed") ;
    }

    /**
     * Sets the source of the center of rotation and scale.  The default is
     * <code>HOTSPOT</code>, which means the center of rotation or scale is a
     * 6DOF sensor's current hotspot location.  The alternative is
     * <code>VWORLD_FIXED</code>, which uses the fixed virtual world
     * coordinates specified by <code>setTransformCenter</code> as the center.
     * The latter is also the fallback if a 6DOF sensor is not specified.
     * <p>
     * The transform center source can be dynamically updated while the
     * behavior is running.
     * 
     * @param source either <code>HOTSPOT</code> or <code>VWORLD_FIXED</code>
     */
    public void setTransformCenterSource(int source) {
        if (! (source == HOTSPOT || source == VWORLD_FIXED))
            throw new IllegalArgumentException
                ("\nrotation/scale center source must be HOTSPOT or " +
                 "VWORLD_FIXED") ;

        this.transformCenterSource = source ;
    }

    /**
     * Gets the rotation/scale center source.
     * 
     * @return the rotation/scale center source
     */
    public int getTransformCenterSource() {
        return transformCenterSource ;
    }

    /**
     * Property which sets the center of rotation and scale if the
     * <code>TransformCenterSource</code> property is <code>VworldFixed</code>
     * or if a 6DOF sensor is not specified.  The default is (0.0, 0.0, 0.0)
     * in virtual world coordinates.  This property is set in the
     * configuration file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * TransformCenter <i>&lt;Point3d&gt;</i>)
     * 
     * @param center array of length 1 containing a <code>Point3d</code>
     * @see #setTransformCenter
     */
    public void TransformCenter(Object[] center) {
        if (! (center.length == 1 && center[0] instanceof Point3d))
            throw new IllegalArgumentException
                ("\nTransformCenter must be a Point3d") ;
        
        setTransformCenter((Point3d)center[0]) ;
    }

    /**
     * Sets the center of rotation and scale if
     * <code>setTransformCenterSource</code> is called with
     * <code>VWORLD_FIXED</code> or if a 6DOF sensor is not specified.  The
     * default is (0.0, 0.0, 0.0) in virtual world coordinates.
     * <p>
     * The transform center can be dynamically updated while the behavior is
     * running. 
     *
     * @param center point in virtual world coordinates about which to rotate
     *  and scale
     */
    public void setTransformCenter(Point3d center) {
        this.transformCenter.set(center) ;
    }

    /**
     * Gets the rotation/scale center in virtual world coordinates.
     * @param center <code>Point3d</code> to receive a copy of the
     *  rotation/scale center
     */
    public void getTransformCenter(Point3d center) {
        center.set(transformCenter) ;
    }

    /**
     * Property which sets the nominal sensor rotation.  The default is the
     * identity transform.
     * <p>
     * This behavior assumes that when a hand-held wand is pointed directly at
     * a screen in an upright position, then its 6DOF sensor's local
     * coordinate system axes (its basis vectors) are nominally aligned with
     * the image plate basis vectors; specifically, that the sensor's -Z axis
     * points toward the screen, the +Y axis points up, and the +X axis points
     * to the right.  The translation and rotation listeners provided by this
     * behavior assume this orientation to determine the transforms to be
     * applied to the view platform; for example, translation applies along
     * the sensor Z axis, while rotation applies about axes defined in the
     * sensor XY plane.
     * <p>
     * This nominal alignment may not hold true depending upon how the sensor
     * is mounted and how the specific <code>InputDevice</code> supporting the
     * sensor handles its orientation.  The <code>NominalSensorRotation</code>
     * property can be used to correct the alignment by specifying the
     * rotation needed to transform vectors from the nominal sensor coordinate
     * system, aligned with the image plate coordinate system as described
     * above, to the sensor's actual local coordinate system.
     * <p>
     * NOTE: the nominal sensor transform applies <i>only</i> to the
     * translation directions and rotation axes created by the listeners
     * defined in this behavior; for compatibility with the core Java 3D API,
     * sensor reads and the sensor hotspot location are still expressed in the
     * sensor's local coordinate system.
     * <p>
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * NominalSensorRotation [<i>&lt;Matrix4d&gt;</i> |
     * <i>&lt;Matrix3d&gt;</i>])
     * 
     * @param matrix array of length 1 containing a <code>Matrix4d</code> or
     *  <code>Matrix3d</code>
     * @see #setNominalSensorRotation
     */
    public void NominalSensorRotation(Object[] matrix) {
        if (! (matrix.length == 1 && (matrix[0] instanceof Matrix3d ||
                                      matrix[0] instanceof Matrix4d)))
            throw new IllegalArgumentException
                ("\nNominalSensorRotation must be a Matrix3d or Matrix4d") ;
        
        Transform3D t3d = new Transform3D() ;

        if (matrix[0] instanceof Matrix3d)
            t3d.set((Matrix3d)matrix[0]) ;
        else
            t3d.set((Matrix4d)matrix[0]) ;
            
        setNominalSensorRotation(t3d) ;
    }

    /**
     * Sets the nominal sensor transform.  The default is the identity
     * transform.
     * <p>
     * This behavior assumes that when a hand-held wand is pointed directly at
     * a screen in an upright position, then its 6DOF sensor's local
     * coordinate system axes (its basis vectors) are nominally aligned with
     * the image plate basis vectors; specifically, that the sensor's -Z axis
     * points toward the screen, the +Y axis points up, and the +X axis points
     * to the right.  The translation and rotation listeners provided by this
     * behavior assume this orientation to determine the transforms to be
     * applied to the view platform, in that translation applies along the
     * sensor Z axis, and rotation applies about axes defined in the sensor XY
     * plane.
     * <p>
     * This nominal alignment may not hold true depending upon how the sensor
     * is mounted and how the specific <code>InputDevice</code> supporting the
     * sensor handles its orientation.  <code>setNominalSensorRotation</code>
     * can be called to correct the alignment by specifying the rotation
     * needed to transform vectors from the nominal sensor coordinate system,
     * aligned with the image plate coordinate system as described above, to
     * the sensor's actual local coordinate system.
     * <p>
     * NOTE: the nominal sensor transform applies <i>only</i> to the
     * translation directions and rotation axes created by the listeners
     * defined in this behavior; for compatibility with the core Java 3D API,
     * sensor reads and the sensor hotspot location are still expressed in the
     * sensor's local coordinate system.
     * 
     * @param transform Rotates vectors from the nominal sensor coordinate
     *  system system to the sensor's local coordinate system; only the
     *  rotational components are used.  May be set <code>null</code> for
     *  identity.
     */
    public void setNominalSensorRotation(Transform3D transform) {
        if (transform == null) {
            nominalSensorRotation = null ;
            return ;
        }
            
        if (nominalSensorRotation == null)
            nominalSensorRotation = new Transform3D() ;

        // Set transform and make sure it is a rotation only.
        nominalSensorRotation.set(transform) ;
        nominalSensorRotation.setTranslation(new Vector3d()) ;
    }

    /**
     * Gets the nominal sensor transform.
     * 
     * @param t3d <code>Transform3D</code> to receive a copy of the
     *  nominal sensor transform
     */
    public void getNominalSensorRotation(Transform3D t3d) {
        if (nominalSensorRotation != null) {
            t3d.set(nominalSensorRotation);
        } else {
            t3d.setIdentity();
        }
    }

    /**
     * Property which sets the number of buttons to be pressed simultaneously
     * on the 6DOF sensor in order to reset the view back to the home
     * transform.  The value must be greater than 1; the default is 3.  A
     * value of <code>None</code> disables this action.  This property is set
     * in the configuration file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ResetViewButtonCount6D [<i>&lt;count&gt;</i> | None])
     * 
     * @param count array of length 1 containing a <code>Double</code> or
     *  <code>String</code>
     * @see #setResetViewButtonCount6D
     * @see ViewPlatformBehavior#setHomeTransform
     *  ViewPlatformBehavior.setHomeTransform
     */
    public void ResetViewButtonCount6D(Object[] count) {
        if (! (count.length == 1 &&
               (count[0] instanceof Double || count[0] instanceof String)))
            throw new IllegalArgumentException
                ("\nResetViewButtonCount6D must be a number or None") ;

        if (count[0] instanceof String) {
            String s = (String)count[0] ;
            if (s.equals("None"))
                setResetViewButtonCount6D(NONE) ;
            else
                throw new IllegalArgumentException
                    ("\nResetViewButtonCount6D string value must be None") ;
        }
        else {
            setResetViewButtonCount6D(((Double)count[0]).intValue()) ;
        }
    }

    /**
     * Sets the number of buttons to be pressed simultaneously
     * on the 6DOF sensor in order to reset the view back to the home
     * transform.  The value must be greater than 1; the default is 3.  A
     * value of <code>NONE</code> disables this action.
     * 
     * @param count either <code>NONE</code> or button count > 1
     * @see ViewPlatformBehavior#setHomeTransform
     *  ViewPlatformBehavior.setHomeTransform 
     */
    public void setResetViewButtonCount6D(int count) {
        if (count == NONE || count > 1) {
            resetViewButtonCount6D = count ;
        }
        else {
            throw new IllegalArgumentException
                ("reset view button count must be > 1") ;
        }
    }

    /**
     * Gets the number of buttons to be pressed simultaneously on the 6DOF
     * sensor in order to reset the view back to the home transform.  A value
     * of <code>NONE</code> indicates this action is disabled.
     * 
     * @return the number of buttons to press simultaneously for a view reset
     */
    public int getResetViewButtonCount6D() {
        return resetViewButtonCount6D ;
    }
    
    /**
     * Property which sets the number of buttons to be pressed simultaneously
     * on the 2D valuator in order to reset the view back to the home
     * transform.  The value must be greater than 1; the default is
     * <code>None</code>.  A value of <code>None</code> disables this action.
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * ResetViewButtonCount2D [<i>&lt;count&gt;</i> | None])
     * 
     * @param count array of length 1 containing a <code>Double</code> or
     *  <code>String</code>
     * @see #setResetViewButtonCount2D
     * @see ViewPlatformBehavior#setHomeTransform
     *  ViewPlatformBehavior.setHomeTransform
     */
    public void ResetViewButtonCount2D(Object[] count) {
        if (! (count.length == 1 &&
               (count[0] instanceof Double || count[0] instanceof String)))
            throw new IllegalArgumentException
                ("\nResetViewButtonCount2D must be a number or None") ;

        if (count[0] instanceof String) {
            String s = (String)count[0] ;
            if (s.equals("None"))
                setResetViewButtonCount2D(NONE) ;
            else
                throw new IllegalArgumentException
                    ("\nResetViewButtonCount2D string value must be None") ;
        }
        else {
            setResetViewButtonCount2D(((Double)count[0]).intValue()) ;
        }
    }

    /**
     * Sets the number of buttons to be pressed simultaneously on the 2D
     * valuator in order to reset the view back to the home transform.  The
     * value must be greater than 1; the default is <code>NONE</code>.  A
     * value of <code>NONE</code> disables this action.
     * 
     * @param count either <code>NONE</code> or button count > 1
     * @see ViewPlatformBehavior#setHomeTransform
     *  ViewPlatformBehavior.setHomeTransform
     */
    public void setResetViewButtonCount2D(int count) {
        if (count == NONE || count > 1) {
            resetViewButtonCount2D = count ;
        }
        else {
            throw new IllegalArgumentException
                ("reset view button count must be > 1") ;
        }
    }

    /**
     * Gets the number of buttons to be pressed simultaneously on the 2D
     * valuator in order to reset the view back to the home transform.  A value
     * of <code>NONE</code> indicates this action is disabled.
     * 
     * @return the number of buttons to press simultaneously for a view reset
     */
    public int getResetViewButtonCount2D() {
        return resetViewButtonCount2D ;
    }
    
    /**
     * Property which sets the 6DOF sensor echo type.  The default is
     * <code>Gnomon</code>, which displays an object with points indicating
     * the direction of each of the sensor's coordinate system axes at the
     * location of the sensor's hotspot.  The alternative is
     * <code>Beam</code>, which displays a beam from the sensor's origin to
     * the location of the sensor's hotspot; the hotspot must not be (0, 0, 0)
     * or an <code>IllegalArgumentException</code> will result.  The width of
     * each of these echo types is specified by the <code>EchoSize</code>
     * property.  The <code>EchoType</code> property is set in the
     * configuration file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * EchoType [Gnomon | Beam | None])
     * 
     * @param type array of length 1 containing a <code>String</code>
     * @see #setEchoType
     */
    public void EchoType(Object[] type) {
        if (! (type.length == 1 && type[0] instanceof String))
            throw new IllegalArgumentException
                ("\nEchoType must be a String") ;
        
        String typeString = (String)type[0] ;

        if (typeString.equals("Gnomon"))
            setEchoType(GNOMON) ;
        else if (typeString.equals("Beam"))
            setEchoType(BEAM) ;
        else if (typeString.equals("None"))
            setEchoType(NONE) ;
        else
            throw new IllegalArgumentException
                ("\nEchoType must be Gnomon, Beam, or None") ;
    }

    /**
     * Sets the 6DOF sensor echo type.  The default is <code>GNOMON</code>,
     * which displays an object with points indicating the direction of each
     * of the sensor's coordinate axes at the location of the sensor's
     * hotspot.  The alternative is <code>BEAM</code>, which displays a beam
     * from the sensor's origin to the location of the sensor's hotspot; the
     * hotspot must not be (0, 0, 0) or an
     * <code>IllegalArgumentException</code> will result.  The width of each
     * of these echo types is specified by <code>setEchoSize</code>.
     * 
     * @param type <code>GNOMON</code>, <code>BEAM</code>, or
     *  <code>NONE</code> are recognized
     */
    public void setEchoType(int type) {
        this.echoType = type ;
    }

    /**
     * Gets the echo type.
     * 
     * @return the echo type
     */
    public int getEchoType() {
        return echoType ;
    }

    /**
     * Property which sets the size of the 6DOF sensor echo in physical
     * meters.  This is used for the width of the gnomon and beam echoes.  The
     * default is 1 centimeter.  This property is set in the configuration
     * file read by <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * EchoSize <i>&lt;size&gt;</i>)
     * 
     * @param echoSize array of length 1 containing a <code>Double</code>
     * @see #setEchoSize
     */
    public void EchoSize(Object[] echoSize) {
        if (! (echoSize.length == 1 && echoSize[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nEchoSize must be a Double") ;
        
        setEchoSize(((Double)echoSize[0]).doubleValue()) ;
    }
    
    /**
     * Sets the size of the 6DOF sensor echo in physical meters.  This is used
     * for the width of the gnomon and beam echoes.  The default is 1
     * centimeter.
     * 
     * @param echoSize the size in meters
     */
    public void setEchoSize(double echoSize) {
        this.echoSize = echoSize ;
    }

    /**
     * Gets the size of the 6DOF sensor echo in meters.
     * 
     * @return the echo size
     */
    public double getEchoSize() {
        return echoSize ;
    }
    
    /**
     * Property which sets the color of the 6DOF sensor echo.  The default is
     * white.  This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * EchoColor <i>&lt;red&gt; &lt;green&gt; &lt;blue&gt;</i>)
     * 
     * @param color array of length 3 containing <code>Doubles</code>
     * @see #setEchoColor
     */
    public void EchoColor(Object[] color) {
        if (! (color.length == 3 && color[0] instanceof Double &&
               color[1] instanceof Double && color[2] instanceof Double))
            throw new IllegalArgumentException
                ("\nEchoColor must be 3 numbers for red, green, and blue") ;
        
        setEchoColor(new Color3f(((Double)color[0]).floatValue(),
                                 ((Double)color[1]).floatValue(),
                                 ((Double)color[2]).floatValue())) ;
    }
    
    /**
     * Sets the color of the 6DOF sensor echo.  The default is white.  This
     * can be called to set the color before or after the echo geometry is
     * created.
     * 
     * @param color the echo color
     */
    public void setEchoColor(Color3f color) {
        if (echoColor == null)
            echoColor = new Color3f(color) ;
        else
            echoColor.set(color) ;

        if (echoGeometry != null) {
            Appearance a = echoGeometry.getAppearance() ;
            Material m = a.getMaterial() ;
            m.setDiffuseColor(echoColor) ;
        }
    }

    /**
     * Gets the 6DOF sensor echo color.
     * 
     * @param color the <code>Color3f</code> into which to copy the echo color
     */
    public void getEchoColor(Color3f color) {
        if (echoColor == null)
            color.set(1.0f, 1.0f, 1.0f) ;
        else
            color.set(echoColor) ;
    }
    
    /**
     * Property which sets the 6DOF sensor echo transparency.  The default is
     * opaque.  A value of 0.0 is fully opaque and 1.0 is fully transparent.
     * This property is set in the configuration file read by
     * <code>ConfiguredUniverse</code>.
     * <p>
     * <b>Syntax:</b><br>(ViewPlatformBehaviorProperty <i>&lt;name&gt;</i>
     * EchoTransparency <i>&lt;transparency&gt;</i>)
     * 
     * @param transparency array of length 1 containing a <code>Double</code>
     * @see #setEchoTransparency
     */
    public void EchoTransparency(Object[] transparency) {
        if (! (transparency.length == 1 && transparency[0] instanceof Double))
            throw new IllegalArgumentException
                ("\nEchoTransparency must be a number") ;
        
        setEchoTransparency(((Double)transparency[0]).floatValue()) ;
    }
    
    /**
     * Sets the 6DOF sensor echo transparency.  The default is opaque.  A
     * value of 0.0 is fully opaque and 1.0 is fully transparent.  This can be
     * called to set the transparency before or after the echo geometry is
     * created.
     * 
     * @param transparency the transparency value
     */
    public void setEchoTransparency(float transparency) {
        echoTransparency = transparency ;

        if (echoGeometry != null) {
            Appearance a = echoGeometry.getAppearance() ;
            TransparencyAttributes ta = a.getTransparencyAttributes() ;
            if (echoTransparency == 0.0f) {
                ta.setTransparencyMode(TransparencyAttributes.NONE) ;
                ta.setTransparency(0.0f) ;
            }
            else {
                ta.setTransparencyMode(TransparencyAttributes.BLENDED) ;
                ta.setTransparency(echoTransparency) ;
                // Use order independent additive blend for gnomon.
                if (echoGeometry instanceof SensorGnomonEcho)
                    ta.setDstBlendFunction(TransparencyAttributes.BLEND_ONE) ;
            }
        }
    }

    /**
     * Gets the 6DOF sensor echo transparency value.
     * 
     * @return the transparency value
     */
    public float getEchoTransparency() {
        return echoTransparency ;
    }
    
    /**
     * Sets the transform group containing a 6DOF sensor's echo geometry.
     * This is used to specify a custom echo.  Its transform will be
     * manipulated to represent the position and orientation of the 6DOF
     * sensor.  Capabilities to allow writing its transform and to read,
     * write, and extend its children will be set.
     * <p>
     * This method must be called before the behavior is made live in order to
     * have an effect.
     * 
     * @param echo the <code>TransformGroup</code> containing the
     *  echo geometry
     */
    public void setEchoTransformGroup(TransformGroup echo) {
	echo.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE) ;
	echo.setCapability(Group.ALLOW_CHILDREN_READ) ;
	echo.setCapability(Group.ALLOW_CHILDREN_WRITE) ;
	echo.setCapability(Group.ALLOW_CHILDREN_EXTEND) ;
        this.echoTransformGroup = echo ;
    }

    /**
     * Gets the transform group containing a 6DOF sensor's echo geometry.
     * Capabilities to write its transform and read, write, and extend its
     * children are granted.
     * 
     * @return the echo's transform group
     */
    public TransformGroup getEchoTransformGroup() {
        return echoTransformGroup ;
    }

    /**
     * Gets the <code>Shape3D</code> defining the 6DOF sensor's echo geometry
     * and appearance.  The returned <code>Shape3D</code> allows appearance
     * read and write.  If a custom echo was supplied by providing the echo
     * transform group directly then the return value will be
     * <code>null</code>.
     * 
     * @return the echo geometry, or <code>null</code> if a custom echo was
     *  supplied
     */
    public Shape3D getEchoGeometry() {
        return echoGeometry ;
    }

    /**
     * Gets the <code>SensorEventAgent</code> used by this behavior.  Sensor
     * event generation is delegated to this agent.  This can be accessed to
     * manipulate the sensor button and read action bindings directly.
     * 
     * @return the sensor event agent
     */
    public SensorEventAgent getSensorEventAgent() {
        return eventAgent ;
    }
}
