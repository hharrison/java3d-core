/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

/**
 * The View object contains all parameters needed in rendering a
 * three dimensional scene from one viewpoint.  A view contains a list
 * of Canvas3D objects that the view is rendered into.  It exists outside
 * of the scene graph, but attaches to a ViewPlatform leaf node object
 * in the scene graph.  It also contains a reference to a PhysicalBody
 * and a PhysicalEnvironment object.
 * <P>
 * The View object is the main Java 3D object for controlling the
 * Java 3D viewing model. All of the components that specify the
 * view transform used to render to the 3D canvases are either contained
 * in the View object or in objects that are referenced by the View
 * object.
 * <P>
 * Java 3D allows applications to specify multiple simultaneously active
 * View objects, each controlling its own set of canvases.
 * <P>
 * The Java 3D View object has several instance variables and methods,
 * but most are calibration variables or user-helping functions. The
 * viewing policies defined by the View object are described below.
 * <P>
 * <b>Policies</b><P>
 *
 * The View object defines the following policies:<P>
 * <UL>
 * <LI>View policy - informs Java 3D whether it should generate
 * the view using the head-tracked system of transformations or the
 * head-mounted system of transformations. These policies are attached
 * to the Java 3D View object. There are two view policies:</LI><P>
 * <UL>
 * <LI>SCREEN_VIEW - specifies that Java 3D should compute a new
 * viewpoint using the sequence of transforms appropriate to screen-based
 * head-tracked display environments (fish-tank VR/portals/VR-desks).
 * This is the default setting.</LI><P>
 * <LI>HMD_VIEW - specifies that Java 3D should compute a new viewpoint
 * using the sequence of transforms appropriate to head mounted display
 * environments. This policy is not available in compatibility mode
 * (see the setCompatibilityModeEnable method description).</LI><P>
 * </UL>
 * <LI>Projection policy - specifies whether Java 3D should generate
 * a parallel projection or a perspective projection. This policy
 * is attached to the Java 3D View object. There are two projection
 * policies:</LI><P>
 * <UL>
 * <LI>PARALLEL_PROJECTION - specifies that a parallel projection
 * transform is computed.</LI><P>
 * <LI>PERSPECTIVE_PROJECTION - specifies that a perspective projection
 * transform is computed. This is the default policy.</LI><P>
 * </UL>
 * <LI>Screen scale policy - specifies where the screen scale comes from.
 * There are two screen scale policies:</LI><P>
 * <UL>
 * <LI>SCALE_SCREEN_SIZE - specifies that the scale is derived from the
 * physical screen according to the following formula (this is the
 * default mode):</LI>
 * <UL>
 * <code>screenScale = physicalScreenWidth / 2.0</code><P>
 * </UL>
 * <LI>SCALE_EXPLICIT - pecifies that the scale is taken directly from
 * the user-provided <code>screenScale</code> attribute (see the
 * setScreenScale method description).</LI><P>
 * </UL>
 * <LI>Window resize policy - specifies how Java 3D modifies the view
 * when users resize windows. When users resize or move windows,
 * Java 3D can choose to think of the window as attached either to
 * the physical world or to the virtual world. The window resize
 * policy allows an application to specify how the
 * view model will handle resizing requests.
 * There are two window resize policies:</LI><P>
 * <UL>
 * <LI>VIRTUAL_WORLD - implies that the original image remains the
 * same size on the screen but the user sees more or less of the
 * virtual world depending on whether the window grew or shrank
 * in size.</LI><P>
 * <LI>PHYSICAL_WORLD - implies that the original image continues
 * to fill the window in the same way using more or less pixels
 * depending on whether the window grew or shrank in size.</LI><P>
 * </UL>
 * <LI>Window movement policy - specifies what part of the virtual
 * world Java 3D draws as a function of window placement on the screen.
 * There are two window movement policies:</LI><P>
 * <UL>
 * <LI>VIRTUAL_WORLD - implies that the image seen in the window
 * changes as the position of the window shifts on the screen.
 * (This mode acts as if the window were a window into the virtual
 * world.)</LI><P>
 * <LI>PHYSICAL_WORLD - implies that the image seen in the window
 * remains the same no matter where the user positions the window
 * on the screen.</LI><P>
 * </UL>
 * <LI>Window eyepoint policy - comes into effect in a non-head-tracked
 * environment. The policy tells Java 3D how to construct a new view
 * frustum based on changes in the field of view and in the Canvas3D's
 * location on the screen. The policy only comes into effect when the
 * application changes a parameter that can change the placement of the
 * eyepoint relative to the view frustum.
 * There are three window eyepoint policies:</LI><P>
 * <UL>
 * <LI>RELATIVE_TO_SCREEN - tells Java 3D to interpret the eye's position
 * relative to the entire screen. No matter where an end user moves a
 * window (a Canvas3D), Java 3D continues to interpret the eye's position
 * relative to the screen. This implies that the view frustum changes shape
 * whenever an end user moves the location of a window on the screen.
 * In this mode, the field of view is read-only.</LI><P>
 * <LI>RELATIVE_TO_WINDOW - specifies that Java 3D should interpret the
 * eye's position information relative to the window (Canvas3D). No matter
 * where an end user moves a window (a Canvas3D), Java 3D continues to
 * interpret the eye's position relative to that window. This implies
 * that the frustum remains the same no matter where the end user
 * moves the window on the screen. In this mode, the field of view
 * is read-only.</LI><P>
 * <LI>RELATIVE_TO_FIELD_OF_VIEW - tells Java 3D that it should
 * modify the eyepoint position so it is located at the appropriate
 * place relative to the window to match the specified field of view.
 * This implies that the view frustum will change whenever the
 * application changes the field of view. In this mode, the eye
 * position is read-only. This is the default setting.</LI><P>
 * <LI>RELATIVE_TO_COEXISTENCE - tells Java 3D to interpret the eye's
 * position in coexistence coordinates. In this mode, the eye position
 * is taken from the view (rather than the Canvas3D) and transformed from
 * coexistence coordinates to image plate coordinates for each
 * Canvas3D.  The resulting eye position is relative to the screen. As
 * in RELATIVE_TO_SCREEN mode, this implies that the view frustum
 * changes shape whenever an end user moves the location of a window
 * on the screen.  In this mode, the field of view is
 * read-only.</LI><P>
 * </UL>
 * <LI>Front and back clip policies - specifies how Java 3D
 * interprets clipping distances to both the near and far clip
 * planes. The policies can contain one of four values specifying
 * whether a distance measurement should be interpreted in
 * the physical or the virtual world and whether that distance
 * measurement should be interpreted relative to the physical
 * eyepoint or the physical screen.
 * The front and back clip policies
 * are specified separately. The front clip policy determines
 * where Java 3D places the front clipping plane. The back clip
 * policy determines where Java 3D places the back clipping plane.
 * The values for both front and back clipping planes are:</LI><P>
 * <UL>
 * <LI>VIRTUAL_EYE - specifies that the associated distance is from
 * the eye and in units of virtual distance.</LI><P>
 * <LI>PHYSICAL_EYE - specifies that the associated distance is from
 * the eye and in units of physical distance (in meters).
 * This is the default policy for both front and back clipping.</LI><P>
 * <LI>VIRTUAL_SCREEN  - specifies that the associated distance is
 * from the screen and in units of virtual distance. </LI><P>
 * <LI>PHYSICAL_SCREEN - specifies that the associated distance is
 * from the screen and in units of physical distance (in meters).
 * </LI><P>
 * </UL>
 * <LI>Visibility policy - specifies how visible and invisible objects
 * are drawn. There are three visibility policies:</LI><P>
 * <UL>
 * <LI>VISIBILITY_DRAW_VISIBLE - only visible objects are drawn
 * (this is the default).</LI><P>
 * <LI>VISIBILITY_DRAW_INVISIBLE - only invisible objects are drawn.</LI><P>
 * <LI>VISIBILITY_DRAW_ALL - both visible and invisible
 * objects are drawn. </LI><P>
 * </UL>
 * <LI>Transparency sorting policy - specifies whether and how
 * transparent objects are sorted.  Sorting multiple transparent
 * objects is necessary to avoid artifacts caused by overlapping
 * transparent objects.  There are two transparency sorting
 * policies:</LI><P>
 * <UL>
 * <LI>TRANSPARENCY_SORT_NONE - no depth sorting of transparent
 * objects is performed (this is the default).  Transparent objects
 * are drawn after opaque objects, but are not sorted from back to
 * front.</LI><P>
 * <LI>TRANSPARENCY_SORT_GEOMETRY - transparent objects are
 * depth-sorted on a per-geometry basis.  Each geometry object of each
 * transparent Shape3D node is drawn from back to front.  Note that
 * this policy will not split geometry into smaller pieces, so
 * intersecting or intertwined objects may not be sorted
 * correctly.  The method used for determining which geometry is closer
 * is implementation dependent.</LI><P>
 * </UL>
 * </UL>
 * <b>Projection and Clip Parameters</b><P>
 * The projection and clip parameters determine the view model's
 * field of view and the front and back clipping distances.<P>
 * <UL>
 * <LI>Field of view - specifies the view model's horizontal
 * field of view in radians, when in the default non-head-tracked
 * mode. This value is ignored when the view model is operating
 * in head-tracked mode, or when the Canvas3D's window eyepoint
 * policy is set to a value other than the default setting of
 * RELATIVE_TO_FIELD_OF_VIEW.</LI><P>
 * <LI>Front clip distance - specifies the distance away from the
 * clip origin, specified by the front clip policy variable, in
 * the direction of gaze where objects stop disappearing. Objects
 * closer than the clip origin (eye or screen)
 * plus the front clip distance are not drawn. Measurements are
 * done in the space (physical or virtual) that is specified by
 * the associated front clip policy parameter.</LI><P>
 * <LI>Back clip distance - specifies the distance away from the
 * clip origin (specified by the back clip policy variable) in the
 * direction of gaze where objects begin disappearing. Objects
 * farther away from the clip origin (eye or
 * screen) plus the back clip distance are not drawn.
 * Measurements are done in the space (physical or virtual) that
 * is specified by the associated back clip policy
 * parameter. The View object's back clip distance is ignored
 * if the scene graph contains an active Clip leaf node.</LI><P>
 * There are several considerations to take into account when
 * choosing values for the front and back clip distances.<P>
 * <UL>
 * <LI>The front clip distance must be greater than 0.0 in physical
 * eye coordinates.</LI><P>
 * <LI>The front clipping plane must be in front of the back clipping
 * plane, that is, the front clip distance must be less than the
 * back clip distance in physical eye coordinates.</LI><P>
 * <LI>The front and back clip distances, in physical eye coordinates,
 * must be less than the largest positive single-precision floating
 * point value, Float.MAX_VALUE. In practice, since these physical
 * eye coordinate distances are in meters, the values
 * should be much less than that.</LI><P>
 * <LI>The ratio of the back distance divided by the front distance,
 * in physical eye coordinates, affects Z-buffer precision. This ratio
 * should be less than about 3000 to accommodate 16-bit Z-buffers.
 * Values of 100 to less than 1000 will produce better results.</LI><P>
 * </UL>
 * Violating any of the above rules will result in undefined behavior.
 * In many cases, no picture will be drawn.<P>
 * </UL>
 * <b>Frame Start Time, Duration, and Number</b><P>
 *
 * There are five methods used to get information about system
 * execution and performance:<P>
 * <UL>
 * <code>getCurrentFrameStartTime</code> returns the time at which
 * the most recent rendering frame started.<P>
 * <code>getLastFrameDuration</code> returns the duration, in milliseconds, of
 * the most recently completed rendering frame.<P>
 * <code>getFrameNumber</code> returns the frame number for this view.<P>
 * <code>getMaxFrameStartTimes</code> retrieves the implementation-dependent
 * maximum number of frames whose start times will be recorded by
 * the system.<P>
 * <code>getFrameStartTimes</code> copies the last k frame start time values
 * into the user-specified array.<P>
 * </UL>
 * <b>View Traversal and Behavior Scheduling</b><P>
 * The following methods control the traversal, the rendering, and
 * the execution of the behavior scheduler for this view:<P>
 * <UL>
 * <code>startBehaviorScheduler</code> starts the behavior scheduler
 * running after it has been stopped.<P>
 * <code>stopBehaviorScheduler</code> stops the behavior scheduler after all
 * currently-scheduled behaviors are executed.<P>
 * <code>isBehaviorSchedulerRunning</code> retrieves a flag that indicates
 * whether the behavior scheduler is currently running.<P>
 * <code>startView</code> starts traversing this view and starts the renderers
 * associated with all canvases attached to this view.<P>
 * <code>stopView</code> stops traversing this view after the current state of
 * the scene graph is reflected on all canvases attached to this
 * view.<P>
 * <code>isViewRunning</code> returns a flag indicating whether the traverser
 * is currently running on this view.<P>
 * </UL>
 * Note: The above six methods are heavy-weight methods intended
 * for verification and image capture (recording). They are not
 * intended to be used for flow control.<P>
 *
 * <b>Scene Antialiasing</b><P>
 *
 * The following methods set and retrieve the scene antialiasing
 * flag. Scene antialiasing is either enabled or disabled for this
 * view. If enabled, the entire scene will be antialiased on each
 * canvas in which scene antialiasing is available. Scene
 * antialiasing is disabled by default.<P>
 * <UL>
 * <code>setSceneAntialiasingEnable</code> sets the scene antialiasing flag.<P>
 * <code>getSceneAntialiasingEnable</code> returns the scene antialiasing
 * flag.<P>
 * </UL>
 * Note that line and point antialiasing are independent of scene
 * antialiasing. If antialiasing is enabled for lines and points,
 * the lines and points will be antialiased prior to scene antialiasing.
 * If scene antialiasing is disabled, antialiased lines and points will
 * still be antialiased.
 * <p>
 * <b>Note:</b> Scene antialiasing is ignored in pure immediate mode,
 * but is supported in mixed-immediate mode.
 * <p>
 * <b>Depth Buffer</b><P>
 *
 * The following two methods enable and disable automatic freezing
 * of the depth buffer for objects rendered during the transparent
 * rendering pass (that is, objects rendered using alpha blending)
 * for this view. If enabled, depth buffer writes are disabled
 * during the transparent rendering pass regardless of the value
 * of the depth-buffer-write-enable flag in the RenderingAttributes
 * object for a particular node. This flag is enabled by default.<P>
 * <UL>
 * <code>setDepthBufferFreezeTransparent</code> enables depth buffer freezing.<P>
 * <code>getDepthBufferFreezeTransparent</code> retrieves the depth buffer
 * flag.<P>
 * </UL>
 * Transparent objects include BLENDED transparent primitives
 * and antialiased lines
 * and points. Transparent objects do not include opaque objects
 * or primitives rendered with SCREEN_DOOR transparency.<p>
 *
 * <b>Sensors</b><P>
 *
 * The following methods retrieve the sensor's location in the
 * virtual world:<P>
 * <UL>
 * <code>getSensorToVworld</code> takes the sensor's last reading and
 * generates a sensor-to-vworld coordinate system transform. This
 * Transform3D object takes points in that sensor's local coordinate
 * system and transforms them into virtual world coordinates.<P>
 *
 * <code>getSensorHotSpotInVworld</code> retrieves the specified sensor's
 * last hotspot location in virtual world coordinates.<P>
 * </UL>
 *
 * <b>Compatibility Mode</b><P>
 *
 * A camera-based view model allows application programmers to think
 * about the images displayed on the computer screen as if a virtual
 * camera took those images. Such a view model allows application
 * programmers to position and orient a virtual camera within a
 * virtual scene, to manipulate some parameters of the virtual
 * camera's lens (specify its field of view), and to specify the
 * locations of the near and far clipping planes.<P>
 * Java 3D allows applications to enable compatibility mode for
 * room-mounted, non-head-tracked display environments, or to disable
 * compatibility mode using the following methods. Camera-based
 * viewing functions are only available in compatibility mode.<P>
 * <UL>
 * <code>setCompatibilityModeEnable</code> turns compatibility mode on or off.
 * Compatibility mode is disabled by default.<P>
 * <code>getCompatabilityModeEnable</code> returns the compatibility mode
 * enable flag.<P>
 * </UL>
 * Use of these view-compatibility functions will disable some of
 * Java 3D's view model features and limit the portability of Java
 * 3D programs. These methods are primarily intended to help
 * jump-start porting of existing applications.<P>
 *
 * Setting the Viewing Transform<P>
 *
 * The View object provides the following compatibility-mode
 * methods that operate on the viewing transform.<P>
 * <UL>
 * <code>setVpcToEc</code> a compatibility mode method that
 * specifies the ViewPlatform
 * coordinates (VPC) to eye coordinates viewing transform.<P>
 * <code>getVpcToEc</code> returns the VPC.<P>
 * </UL>
 * Setting the Projection Transform
 * <p>
 * The View object provides the following compatibility-mode
 * methods that operate on the projection transform:<P>
 * <UL>
 * The <code>setLeftProjection</code> and <code>setRightProjection</code>
 * methods specify
 * a viewing frustum for the left and right eye that transforms
 * points in eye coordinates to clipping coordinates.<P>
 *
 * The <code>getLeftProjection</code> and <code>getRightProjection</code>
 * methods return
 * the viewing frustum for the left and right eye.<P>
 * </UL>
 *
 * <p>
 * <b>Additional Information</b>
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a> and
 * <a href="doc-files/ViewModel.html">View Model</a>
 * documents.
 *
 * @see Canvas3D
 * @see PhysicalBody
 * @see PhysicalEnvironment
 * @see ViewPlatform
 * @see TransparencyAttributes
 */

public class View extends Object {
    /**
     * Specifies a policy whereby the origin of physical or virtual
     * coordinates is relative to the position of the nominal head.
     * When used as a view attach policy, this sets the origin of view
     * platform coordinates to be at the eyepoint.
     * @see ViewPlatform#setViewAttachPolicy
     * @see PhysicalEnvironment#setCoexistenceCenterInPworldPolicy
     */
    public static final int NOMINAL_HEAD = 0;

    /**
     * Specifies a policy whereby the origin of physical or virtual
     * coordinates is relative to the position of the nominal feet.
     * When used as a view attach policy, this sets the origin of view
     * platform coordinates to be at the ground plane.
     * @see ViewPlatform#setViewAttachPolicy
     * @see PhysicalEnvironment#setCoexistenceCenterInPworldPolicy
     */
    public static final int NOMINAL_FEET = 1;

    /**
     * Specifies a policy whereby the origin of physical or virtual
     * coordinates is relative to the screen.
     * When used as a view attach policy, this sets the origin of view
     * platform coordinates to be at the center of the window or screen,
     * in effect, allowing the user to view objects from an optimal viewpoint.
     * @see ViewPlatform#setViewAttachPolicy
     * @see PhysicalEnvironment#setCoexistenceCenterInPworldPolicy
     */
    public static final int NOMINAL_SCREEN = 2;

    /**
     * Specifies that the screen scale for this view is derived from
     * the physical screen size.  This scale factor is computed as follows:
     * <ul>
     * physical_screen_width / 2.0
     * </ul>
     * This allows an application to define a world in a normalized
     * [-1,1] space and view it on a screen of any size.
     * @see #setScreenScalePolicy
     */
    public static final int SCALE_SCREEN_SIZE = 0;

    /**
     * Specifies that the screen scale for this view is taken directly
     * from the user-provided screenScale parameter.
     * @see #setScreenScalePolicy
     * @see #setScreenScale
     */
    public static final int SCALE_EXPLICIT = 1;

    /**
     * Specifies that the associated distance is measured
     * from the screen in virtual world coordinates.
     * Policy for interpreting clip plane distances.
     * Used in specifying the policy in frontClipPolicy and backClipPolicy.
     * @see #setFrontClipPolicy
     * @see #setBackClipPolicy
     */
    public static final int VIRTUAL_SCREEN = 0;

    /**
     * Specifies that the associated distance is measured
     * from the screen in meters.
     * Policy for interpreting clip plane distances.
     * Used in specifying the policy in frontClipPolicy and backClipPolicy.
     * @see #setFrontClipPolicy
     * @see #setBackClipPolicy
     */
    public static final int PHYSICAL_SCREEN = 1;

    /**
     * Specifies that the associated distance is measured
     * from the eye in virtual world coordinates.
     * Policy for interpreting clip plane distances.
     * Used in specifying the policy in frontClipPolicy and backClipPolicy.
     * @see #setFrontClipPolicy
     * @see #setBackClipPolicy
     */
    public static final int VIRTUAL_EYE = 2;

    /**
     * Specifies that the associated distance is measured
     * from the eye in meters.
     * Policy for interpreting clip plane distances.
     * Used in specifying the policy in frontClipPolicy and backClipPolicy.
     * This is the default policy for both front and back clipping.
     * @see #setFrontClipPolicy
     * @see #setBackClipPolicy
     */
    public static final int PHYSICAL_EYE = 3;

    /**
     * Policy for resizing and moving windows.
     * Used in specifying windowResizePolicy and windowMovementPolicy.
     * VIRTUAL_WORLD specifies that the associated action takes place
     * in the virtual world as well as in the physical world.
     * @see #setWindowResizePolicy
     * @see #setWindowMovementPolicy
     */
    public static final int VIRTUAL_WORLD = 0;

    /**
     * Policy for resizing and moving windows.
     * Used in specifying windowResizePolicy and windowMovementPolicy.
     * PHYSICAL_WORLD specifies that the specified action takes place
     * only in the physical world.
     * @see #setWindowResizePolicy
     * @see #setWindowMovementPolicy
     */
    public static final int PHYSICAL_WORLD = 1;

    /**
     * Policy for placing the eyepoint in non-head-tracked modes.
     * Specifies that Java 3D should interpret the
     * given fixed eyepoint position as relative to the entire screen.
     * This implies
     * that the view frustum shape will change whenever a
     * user moves the location of a window on the screen.
     * @see #setWindowEyepointPolicy
     */
    public static final int RELATIVE_TO_SCREEN = 0;

    /**
     * Policy for placing the eyepoint in non-head-tracked modes.
     * Specifies that Java 3D should interpret the
     * given fixed-eyepoint position as relative to the window.
     * @see #setWindowEyepointPolicy
     */
    public static final int RELATIVE_TO_WINDOW = 1;

    /**
     * Policy for placing the eyepoint in non-head-tracked modes.
     * Specifies that Java 3D should
     * modify the position of the eyepoint to match any changes in field
     * of view; the view frustum will change whenever the application
     * program changes the field of view.
     * <p>
     * NOTE: when this policy is specified, the Z coordinate of
     * the derived eyepoint is used in place of
     * nominalEyeOffsetFromNominalScreen.
     * @see #setWindowEyepointPolicy
     */
    public static final int RELATIVE_TO_FIELD_OF_VIEW = 2;

    /**
     * Policy for placing the eyepoint in non-head-tracked modes.
     * Specifies that Java 3D should interpret the fixed eyepoint
     * position in the view as relative to the origin
     * of coexistence coordinates.  This eyepoint is transformed from
     * coexistence coordinates to image plate coordinates for each
     * Canvas3D.
     * As in RELATIVE_TO_SCREEN mode, this implies
     * that the view frustum shape will change whenever a
     * user moves the location of a window on the screen.
     * @see #setWindowEyepointPolicy
     *
     * @since Java 3D 1.2
     */
    public static final int RELATIVE_TO_COEXISTENCE = 3;

    /**
     * Specifies that monoscopic view generated should be the view as seen
     * from the left eye.
     * @see Canvas3D#setMonoscopicViewPolicy
     */
    public static final int LEFT_EYE_VIEW = 0;

    /**
     * Specifies that monoscopic view generated should be the view as seen
     * from the right eye.
     * @see Canvas3D#setMonoscopicViewPolicy
     */
    public static final int RIGHT_EYE_VIEW = 1;

    /**
     * Specifies that monoscopic view generated should be the view as seen
     * from the 'center eye', the fictional eye half-way between the left and
     * right eye.
     * @see Canvas3D#setMonoscopicViewPolicy
     */
    public static final int CYCLOPEAN_EYE_VIEW = 2;

    /**
     * Specifies that the viewing environment for this view is a
     * standard screen-based display environment.
     * In this mode, Java 3D will compute new viewpoints
     * using that sequence of transforms appropriate to screen-based,
     * display environments, that may or may not include head tracking
     * (e.g., a monoscopic screen, fish-tank VR, portals, VR-desks).
     * This is the default mode.
     * @see #setViewPolicy
     */
    public static final int SCREEN_VIEW = 0;

    /**
     * Specifies that the viewing environment for this view is a
     * head-mounted display environment.
     * In this mode, Java 3D will compute new viewpoints
     * using that sequence of transforms appropriate to head-mounted display
     * environments.  These environments are generally head-tracked.
     * @see #setViewPolicy
     */
    public static final int HMD_VIEW = 1;

    /**
     * Specifies that Java 3D should generate a parallel projection matrix
     * for this View.
     * @see #setProjectionPolicy
     */
    public static final int PARALLEL_PROJECTION = 0;

    /**
     * Specifies that Java 3D should generate a perspective projection matrix
     * for this View.
     * This is the default mode.
     * @see #setProjectionPolicy
     */
    public static final int PERSPECTIVE_PROJECTION = 1;

    /**
     * Policy that specifies that only visible objects should be drawn.
     * This is the default mode.
     * @see #setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    public static final int VISIBILITY_DRAW_VISIBLE = 0;

    /**
     * Policy that specifies that only invisible objects should be drawn.
     * @see #setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    public static final int VISIBILITY_DRAW_INVISIBLE = 1;

    /**
     * Policy that specifies that both visible and invisible objects
     * should be drawn.
     * @see #setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    public static final int VISIBILITY_DRAW_ALL = 2;

    /**
     * Policy that specifies that no sorting of transparent objects
     * is done.
     * This is the default mode.
     * @see #setTransparencySortingPolicy
     *
     * @since Java 3D 1.3
     */
    public static final int TRANSPARENCY_SORT_NONE = 0;

    /**
     * Policy that specifies that transparent objects
     * are sorted from back to front on a per-geometry basis.
     * @see #setTransparencySortingPolicy
     *
     * @since Java 3D 1.3
     */
    public static final int TRANSPARENCY_SORT_GEOMETRY = 1;


    //
    // The AWT window for display.
    //
    // This object can be queried to obtain:
    // screen width in pixels
    // screen height in pixels
    // window width in pixels
    // window height in pixels
    // window upper left corner location in pixels relative to screen
    //
    // Use getCanvases() to access this
private Vector<Canvas3D> canvases = new Vector<Canvas3D>(3);

    //
    // The current universe associated with this view
    //
    VirtualUniverse universe = null;

    //
    // The RenderBin associated with this view.
    //
    RenderBin renderBin = null;

    // This is the SoundScheduler associated with this view.
    SoundScheduler soundScheduler = null;

    // AudioDevice enumerator current position
    //    AudioDeviceEnumerator allAudioEnumerator = null;

    // These are used for tracking the frame times
    static final int NUMBER_FRAME_START_TIMES = 10;

    long[] frameStartTimes = new long[NUMBER_FRAME_START_TIMES];
    long[] frameNumbers = new long[NUMBER_FRAME_START_TIMES];
    int currentFrameIndex = 0;

    // These are the values that are set at the end of each frame
    long currentFrameStartTime = 0;
    long currentFrameDuration = 0;
    long currentFrameNumber = 0;

    // These are the ones that get updated directly by MC
    long frameNumber = 0;
    long startTime = 0;
    long stopTime = 0;

    // User adjustable minimum frame cycle time
    long minFrameCycleTime;

    // True when stopBehaviorScheduler invoke
    boolean stopBehavior;

    //
    // View cache for this view.
    //
    ViewCache viewCache = null;

    // Compatibility mode related field has changed.
    // { compatibilityModeEnable, compatVpcToEc, compatLeftProjection,
    //   compatRightProjection }
    static final int COMPATIBILITY_MODE_DIRTY         = 0x01;
    // ScreenScalePolicy field has changed.
    static final int SCREEN_SCALE_POLICY_DIRTY        = 0x02;
    // Screen scale field has changed.
    static final int SCREEN_SCALE_DIRTY               = 0x04;
    // Window Resize Policy field has changed.
    static final int WINDOW_RESIZE_POLICY_DIRTY       = 0x08;
    // View Policy eye in image plate field has changed.
    static final int VIEW_POLICY_DIRTY                = 0x10;
    // Clip related field has changed.
    // { frontClipDistance, backClipDistance, frontClipPolicy, backClipPolicy }
    static final int CLIP_DIRTY                       = 0x20;
    // Projection Policy field has changed.
    static final int PROJECTION_POLICY_DIRTY          = 0x40;
    // Window Movement Policy field has changed.
    static final int WINDOW_MOVEMENT_POLICY_DIRTY     = 0x80;
    // Window Eye Point Policy field has changed.
    static final int WINDOW_EYE_POINT_POLICY_DIRTY    = 0x100;
    // Monoscopic View Policy field has changed.
    static final int MONOSCOPIC_VIEW_POLICY_DIRTY     = 0x200;
    // Field Of View field has changed.
    static final int FIELD_OF_VIEW_DIRTY              = 0x400;
    // Tracking Enable field has changed.
    static final int TRACKING_ENABLE_DIRTY            = 0x800;
    // User Head To Vworld Enable field has changed.
    static final int USER_HEAD_TO_VWORLD_ENABLE_DIRTY = 0x1000;
    // coexistenceCenteringEnable flag has changed.
    static final int COEXISTENCE_CENTERING_ENABLE_DIRTY = 0x2000;
    // leftManualEyeInCoexistence has changed.
    static final int LEFT_MANUAL_EYE_IN_COEXISTENCE_DIRTY = 0x4000;
    // rightManualEyeInCoexistence has changed.
    static final int RIGHT_MANUAL_EYE_IN_COEXISTENCE_DIRTY = 0x8000;
    // visibilityPolicy has changed.
    static final int VISIBILITY_POLICY_DIRTY               = 0x10000;

    // This is not from View object. It is here for the purpose
    // keeping all ViewCache's dirty mask bit declaration in one place.
    // ViewPlatformRetained viewAttach Policy field has changed.
    static final int VPR_VIEW_ATTACH_POLICY_DIRTY = 0x10000;
    static final int VPR_VIEWPLATFORM_DIRTY       = 0x20000;

    // PhysicalEnvironment fields has changed.
    static final int PE_COE_TO_TRACKER_BASE_DIRTY           = 0x100000;
    static final int PE_TRACKING_AVAILABLE_DIRTY            = 0x200000;
    static final int PE_COE_CENTER_IN_PWORLD_POLICY_DIRTY   = 0x400000;

    // PhysicalBody fields has changed.
    static final int PB_EYE_POSITION_DIRTY                             = 0x1000000;
    static final int PB_EAR_POSITION_DIRTY                             = 0x2000000;
    static final int PB_NOMINAL_EYE_HEIGHT_FROM_GROUND_DIRTY           = 0x4000000;
    static final int PB_NOMINAL_EYE_OFFSET_FROM_NOMINAL_SCREEN_DIRTY   = 0x8000000;


    // Mask that indicates this View's view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    int vDirtyMask = (COMPATIBILITY_MODE_DIRTY | SCREEN_SCALE_POLICY_DIRTY
		      | SCREEN_SCALE_DIRTY | WINDOW_RESIZE_POLICY_DIRTY
		      | VIEW_POLICY_DIRTY | CLIP_DIRTY
		      | PROJECTION_POLICY_DIRTY | WINDOW_MOVEMENT_POLICY_DIRTY
		      | WINDOW_EYE_POINT_POLICY_DIRTY | MONOSCOPIC_VIEW_POLICY_DIRTY
		      | FIELD_OF_VIEW_DIRTY | TRACKING_ENABLE_DIRTY
		      | USER_HEAD_TO_VWORLD_ENABLE_DIRTY
		      | COEXISTENCE_CENTERING_ENABLE_DIRTY
		      | LEFT_MANUAL_EYE_IN_COEXISTENCE_DIRTY
		      | RIGHT_MANUAL_EYE_IN_COEXISTENCE_DIRTY
		      | VISIBILITY_POLICY_DIRTY);


    //
    // This object contains a specification of the user's physical body.
    //
    // Attributes of this object are defined in head coordinates and
    // include information such as the location of the user's eyes and
    // ears.
    // The origin is defined to be halfway between the left and right eye
    // in the plane of the face.
    // The x-axis extends to the right (of the head looking out from the head).
    // The y-axis extends up. The z-axis extends to the rear of the head.
    //
    PhysicalBody  physicalBody;

    // This object contains a specification of the physical environment.
    PhysicalEnvironment  physicalEnvironment;

    // View model compatibility mode flag
    boolean compatibilityModeEnable = false;

    // View model coexistenceCenteringEnable flag
    boolean coexistenceCenteringEnable = true;

    Point3d leftManualEyeInCoexistence = new Point3d();
    Point3d rightManualEyeInCoexistence = new Point3d();

    //
    // Indicates which major mode of view computation to use:
    // HMD mode or screen/fish-tank-VR mode.
    //
    int		viewPolicy = SCREEN_VIEW;

    // The current projection policy (parallel versus perspective)
    int projectionPolicy = PERSPECTIVE_PROJECTION;

    //
    // The view model's field of view.
    //
    double	fieldOfView = 45.0 * Math.PI / 180.0;

    //
    // The distance away from the clip origin
    // in the direction of gaze for the front and back clip planes.
    // The default values are in meters.
    //
    double	frontClipDistance = 0.1;
    double	backClipDistance = 10.0;

    // This variable specifies where the screen scale comes from
    int screenScalePolicy = SCALE_SCREEN_SIZE;

    // The screen scale value used when the screen scale policy is
    // SCALE_EXPLICIT
    double screenScale = 1.0;

    //
    // This variable specifies how Java 3D modifies the view when
    // the window is resized (VIRTUAL_WORLD or PHYSICAL_WORLD).
    //
    int windowResizePolicy = PHYSICAL_WORLD;

    //
    // This variable specifies how Java 3D modifies the view when
    // the window is moved (VIRTUAL_WORLD or PHYSICAL_WORLD).
    //
    int windowMovementPolicy = PHYSICAL_WORLD;

    //
    // Specifies how Java 3D handles the predefined eyepoint in
    // non-head-tracked environment (RELATIVE_TO_SCREEN,
    // RELATIVE_TO_WINDOW, RELATIVE_TO_FIELD_OF_VIEW, or
    // RELATIVE_TO_COEXISTENCE)
    //
    int windowEyepointPolicy = RELATIVE_TO_FIELD_OF_VIEW;

    //
    // Specifies how Java 3D generates monoscopic view
    // (LEFT_EYE_VIEW, RIGHT_EYE_VIEW, or CYCLOPEAN_EYE_VIEW).
    //
    int monoscopicViewPolicy = CYCLOPEAN_EYE_VIEW;

    /**
     * Defines the policy for placing the front clipping plane.
     * Legal values include PHYSICAL_EYE, PHYSICAL_SCREEN,
     * VIRTUAL_EYE, and VIRTUAL_SCREEN.
     */
    int		frontClipPolicy = PHYSICAL_EYE;

    /**
     * Defines the policy for placing the back clipping plane.
     */
    int		backClipPolicy = PHYSICAL_EYE;

    /**
     * Defines the visibility policy.
     */
    int		visibilityPolicy = VISIBILITY_DRAW_VISIBLE;

    /**
     * Defines the transparency sorting policy.
     */
    int		transparencySortingPolicy = TRANSPARENCY_SORT_NONE;

    /**
     * Flag to enable tracking, if so allowed by the trackingAvailable flag.
     */
    boolean		trackingEnable = false;

    /**
     * This setting enables the continuous updating by Java 3D of the
     * userHeadToVworld transform.
     */
    boolean userHeadToVworldEnable = false;

    /**
     * The view platform currently associated with this view.
     */
    private ViewPlatform viewPlatform = null;

    // The current compatibility mode view transform
    Transform3D compatVpcToEc = new Transform3D();

    // The current compatibility mode projection transforms
    Transform3D compatLeftProjection = new Transform3D();
    Transform3D compatRightProjection = new Transform3D();

    // The long id of this view - used for dirty bit evaluation in the scene graph
    Integer viewId = null;
    int viewIndex = -1;

    // A boolean that indicates whether or not this is the primary view
    boolean primaryView = false;

    // A boolean that indicates whether or not this view is active as
    // seen by MasterControl
    boolean active = false;

    // A boolean that indicates whether or not this view is active as
    // seen by this view. There is a delay before MasterControl set
    // active flag, so a local activeStatus is used. Otherwise
    // activate event may lost if it proceed by deactivate event
    // but MC not yet set active to false. This happens in
    // viewplatform detach and attach.
    boolean activeStatus = false;

    // This boolean indicates whether or not the view is running.  It
    // is used for startView/stopView
    volatile boolean isRunning = true;

    // A flag to indicate that we are in a canvas callback routine
    boolean inCanvasCallback = false;

    //
    // Flag to enable depth buffer freeze during trasparent rendering pass
    //
    boolean depthBufferFreezeTransparent = true;

    //
    // Flag to enable scene antialiasing
    //
    boolean sceneAntialiasingEnable = false;

    //
    // Flag to enable local eye lighting
    //
    boolean localEyeLightingEnable = false;

// Array Lists to track the screens and canvases associated with this View.
// use getScreens() to access this
private ArrayList<Screen3D> screenList = new ArrayList<Screen3D>();

// use getCanvasList() to access this
private ArrayList<ArrayList<Canvas3D>> canvasList = new ArrayList<ArrayList<Canvas3D>>();

    private Canvas3D[][] cachedCanvasList;
    private Canvas3D[] cachedCanvases;
    private Screen3D[] cachedScreens;
    private int longestScreenList = 0;
    private boolean canvasesDirty = true;

    // Flag to notify user thread when renderOnce is finished
    volatile boolean renderOnceFinish = true;

    // Lock to synchronize start/stop/renderOnce call
    private Object startStopViewLock = new Object();

    // Lock for evaluateActive() only. This is used to prevent
    // using lock this which will cause deadlock when MC call
    // snapshot which waitForMC() in user thread.
    private Object evaluateLock = new Object();

    /**
     * use for stop view, when stopview, set to count -1,
     * when reach 1, call stopView() in MC and reset to -1.
     */
    int stopViewCount = -1;

    /**
     * False if current frame cycle time less than minimum frame cycle time
     */
    boolean isMinCycleTimeAchieve = true;

    // Time to sleep if minimum frame cycle time not achieve
    long sleepTime = 0;

    // use in pure immediate mode to tell whether this view rendering
    // thread is added in MC renderThreadData
    volatile boolean inRenderThreadData = false;

    // use to notify MC that render bin has run once, and is ready for
    // renderer to render
    boolean renderBinReady = false;

    // No of time setUniverse() is invoke
    long universeCount = 0;

    // The universe count when UNREGISTER_VIEW request is post,
    // this is used to distingish whether new setUniverse() is
    // invoked after UNREGISTER_VIEW request post to avoid
    // resetting the newly set universe.
    long resetUnivCount = 0;

    // This notify user thread waitForMC() to continue when
    // MC finish unregisterView
    volatile boolean doneUnregister = false;

    static final int TRANSP_SORT_POLICY_CHANGED           = 0x0001;
    static final int OTHER_ATTRS_CHANGED                  = 0x0002;

    /**
     * Constructs a View object with default parameters.  The default
     * values are as follows:
     * <ul>
     * view policy : SCREEN_VIEW<br>
     * projection policy : PERSPECTIVE_PROJECTION<br>
     * screen scale policy : SCALE_SCREEN_SIZE<br>
     * window resize policy : PHYSICAL_WORLD<br>
     * window movement policy : PHYSICAL_WORLD<br>
     * window eyepoint policy : RELATIVE_TO_FIELD_OF_VIEW<br>
     * monoscopic view policy : CYCLOPEAN_EYE_VIEW<br>
     * front clip policy : PHYSICAL_EYE<br>
     * back clip policy : PHYSICAL_EYE<br>
     * visibility policy : VISIBILITY_DRAW_VISIBLE<br>
     * transparency sorting policy : TRANSPARENCY_SORT_NONE<br>
     * coexistenceCentering flag : true<br>
     * compatibility mode : false<br>
     * left projection : identity<br>
     * right projection : identity<br>
     * vpc to ec transform : identity<br>
     * physical body : null<br>
     * physical environment : null<br>
     * screen scale : 1.0<br>
     * field of view : PI/4<br>
     * left manual eye in coexistence : (-0.033, 0.0, 0.4572)<br>
     * right manual eye in coexistence : (0.033, 0.0, 0.4572)<br>
     * front clip distance : 0.1<br>
     * back clip distance : 10.0<br>
     * tracking enable : false<br>
     * user head to vworld enable : false<br>
     * list of Canvas3D objects : empty<br>
     * depth buffer freeze transparent : true<br>
     * scene antialiasing : false<br>
     * local eye lighting : false<br>
     * view platform : null<br>
     * behavior scheduler running : true<br>
     * view running : true<br>
     * minimum frame cycle time : 0<br>
     * </ul>
     */
    public View() {
        viewCache = new ViewCache(this);
    }

    /**
     * Sets the policy for view computation.
     * This variable specifies how Java 3D uses its transforms in
     * computing new viewpoints.
     * <UL>
     * <LI>SCREEN_VIEW specifies that Java 3D should compute a new viewpoint
     * using the sequence of transforms appropriate to screen-based
     * head-tracked display environments (fish-tank VR/portals/VR-desks).
     * </LI>
     * <LI>HMD_VIEW specifies that Java 3D should compute a new viewpoint
     * using the sequence of transforms appropriate to head mounted
     * display environments.
     * </LI>
     * </UL>
     * The default view policy is SCREEN_VIEW.
     * @param policy the new policy, one of SCREEN_VIEW or HMD_VIEW
     * @exception IllegalArgumentException if policy is a value other than
     * SCREEN_VIEW or HMD_VIEW
     * @exception IllegalStateException if the specified policy
     * is HMD_VIEW and if any canvas associated with this view is
     * a stereo canvas with a monoscopicEyePolicy of CYCLOPEAN_EYE_VIEW
     */
    public void setViewPolicy(int policy) {
	if (policy != HMD_VIEW &&
	    policy != SCREEN_VIEW) {

	    throw new IllegalArgumentException(J3dI18N.getString("View0"));
	}
	if(policy == HMD_VIEW) {
	    // Check the following :
	    // 1) If the view is in HMD mode and there exists a canvas in
	    // CYCLOPEAN_EYE_VIEW mode then throw exception.
	    synchronized (canvasList) {
		for (int i=canvases.size()-1; i>=0; i--) {
				Canvas3D c3d = canvases.elementAt(i);

		    if ((c3d.monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW) &&
			(!c3d.useStereo)){
			throw new
			    IllegalStateException(J3dI18N.getString("View31"));
		    }
		}
	    }
	}
	synchronized(this) {
	    this.viewPolicy = policy;
	    vDirtyMask |= View.VIEW_POLICY_DIRTY;
	}
	repaint();
    }

    /**
     * Retrieves the current view computation policy for this View.
     * @return one of: SCREEN_VIEW or HMD_VIEW.
     */
    public int getViewPolicy() {
	return this.viewPolicy;
    }

    /**
     * Sets the projection policy for this View.
     * This variable specifies the type of projection transform that
     * will be generated.  A value of PARALLEL_PROJECTION specifies that
     * a parallel projection transform is generated.  A value of
     * PERSPECTIVE_PROJECTION specifies that
     * a perspective projection transform is generated.
     * The default projection policy is PERSPECTIVE.
     * @param policy the new policy, one of PARALLEL_PROJECTION or
     * PERSPECTIVE_PROJECTION
     * @exception IllegalArgumentException if policy is a value other than
     * PARALLEL_PROJECTION or PERSPECTIVE_PROJECTION
     */
    public void setProjectionPolicy(int policy) {
	if (policy != PERSPECTIVE_PROJECTION &&
	    policy != PARALLEL_PROJECTION) {

	    throw new IllegalArgumentException(J3dI18N.getString("View1"));
	}
	synchronized(this) {
	    this.projectionPolicy = policy;
	    vDirtyMask |= View.PROJECTION_POLICY_DIRTY;
	}

	repaint();
    }

    /**
     * Retrieves the current projection policy for this View.
     * @return one of: PARALLEL_PROJECTION or PERSPECTIVE_PROJECTION.
     */
    public int getProjectionPolicy() {
	return this.projectionPolicy;
    }

    /**
     * Sets the screen scale policy for this view.
     * This policy specifies how the screen scale is derived.
     * The value is either SCALE_SCREEN_SIZE or SCALE_EXPLICIT.
     * A value of SCALE_SCREEN_SIZE specifies that the scale is derived
     * from the size of the physical screen.  A value of SCALE_EXPLICIT
     * specifies that the scale is taken directly from the screenScale
     * parameter.
     * The default screen scale policy is SCALE_SCREEN_SIZE.
     * @param policy the new policy, one of SCALE_SCREEN_SIZE or
     * SCALE_EXPLICIT.
     */
    public void setScreenScalePolicy(int policy) {

	synchronized(this) {
	    this.screenScalePolicy = policy;
	    vDirtyMask |= View.SCREEN_SCALE_POLICY_DIRTY;
	}

	repaint();
    }

    /**
     * Returns the current screen scale policy, one of:
     * SCALE_SCREEN_SIZE or SCALE_EXPLICIT.
     * @return the current screen scale policy
     */
    public int getScreenScalePolicy() {
	return this.screenScalePolicy;
    }

    /**
     * Sets the window resize policy.
     * This variable specifies how Java 3D modifies the view when
     * users resize windows. The variable can contain one of
     * VIRTUAL_WORLD or PHYSICAL_WORLD.
     * A value of VIRTUAL_WORLD implies that the original image
     * remains the same size on the screen but the user sees more
     * or less of the virtual world depending on whether the window
     * grew or shrank in size.
     * A value of PHYSICAL_WORLD implies that the original image
     * continues to fill the window in the same way using more or
     * less pixels depending on whether the window grew or shrank
     * in size.
     * The default window resize policy is PHYSICAL_WORLD.
     * @param policy the new policy, one of VIRTUAL_WORLD or PHYSICAL_WORLD
     */
    public void setWindowResizePolicy(int policy) {

	synchronized(this) {
	    this.windowResizePolicy = policy;
	    vDirtyMask |= View.WINDOW_RESIZE_POLICY_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the current window resize policy, one of:
     * VIRTUAL_WORLD or PHYSICAL_WORLD.
     * @return the current window resize policy
     */
    public int getWindowResizePolicy() {
	return this.windowResizePolicy;
    }

    /**
     * Sets the window movement policy.
     * This variable specifies what part of the virtual world Java 3D
     * draws as a function of window placement on the screen. The
     * variable can contain one of VIRTUAL_WORLD or PHYSICAL_WORLD.
     * A value of VIRTUAL_WORLD implies that the image seen in the
     * window changes as the position of the window shifts on the
     * screen.  (This mode acts as if the window were a window into
     * the virtual world.)
     * A value of PHYSICAL_WORLD implies that the image seen in the
     * window remains the same no matter where the user positions
     * the window on the screen.
     * The default window movement policy is PHYSICAL_WORLD.
     * @param policy the new policy, one of VIRTUAL_WORLD or PHYSICAL_WORLD
     */
    public void setWindowMovementPolicy(int policy) {

	synchronized(this) {
	    this.windowMovementPolicy = policy;
	    vDirtyMask |= View.WINDOW_MOVEMENT_POLICY_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the current window movement policy,
     * one of: VIRTUAL_WORLD or PHYSICAL_WORLD.
     * @return the current window movement policy
     */
    public int getWindowMovementPolicy() {
	return this.windowMovementPolicy;
    }

    /**
     * Sets the view model's window eyepoint policy.
     * This variable specifies how Java 3D handles the predefined eye
     * point in a non-head-tracked environment.  The variable can contain
     * one of:
     * <UL>
     * <LI>RELATIVE_TO_SCREEN, Java 3D should interpret the
     * given fixed-eyepoint position as relative to the screen (this
     * implies that the view frustum shape will change whenever a
     * user moves the location of a window on the screen).
     * </LI>
     * <LI>RELATIVE_TO_WINDOW, Java 3D should interpret the
     * given fixed-eyepoint position as relative to the window.  In this
     * mode, the X and Y values are taken as the center of the window and
     * the Z value is taken from the canvas eyepoint position.
     * </LI>
     * <LI>RELATIVE_TO_FIELD_OF_VIEW, Java 3D should
     * modify the position of the eyepoint to match any changes in field
     * of view (the view frustum will change whenever the application
     * program changes the field of view).
     * </LI>
     * <LI>RELATIVE_TO_COEXISTENCE, Java 3D should interpret the eye's
     * position in coexistence coordinates. In this mode, the eye position
     * is taken from the view (rather than the Canvas3D) and transformed from
     * coexistence coordinates to image plate coordinates for each
     * Canvas3D.  The resulting eye position is relative to the screen (this
     * implies that the view frustum shape will change whenever a
     * user moves the location of a window on the screen).
     * </LI>
     * </UL>
     * The default window eyepoint policy is RELATIVE_TO_FIELD_OF_VIEW.
     * @param policy the new policy, one of RELATIVE_TO_SCREEN,
     * RELATIVE_TO_WINDOW, RELATIVE_TO_FIELD_OF_VIEW, or
     * RELATIVE_TO_COEXISTENCE
     */
    public void setWindowEyepointPolicy(int policy) {
	synchronized(this) {
	    this.windowEyepointPolicy = policy;
	    vDirtyMask |= View.WINDOW_EYE_POINT_POLICY_DIRTY;
	}

	repaint();
    }

    /**
     * Returns the current window eyepoint policy, one of:
     * RELATIVE_TO_SCREEN, RELATIVE_TO_WINDOW, RELATIVE_TO_FIELD_OF_VIEW or
     * RELATIVE_TO_COEXISTENCE.
     * @return the current window eyepoint policy
     */
    public int getWindowEyepointPolicy() {
	return this.windowEyepointPolicy;
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>Canvas3D.setMonoscopicViewPolicy</code>
     */
    public void setMonoscopicViewPolicy(int policy) {
	synchronized(this) {
	    this.monoscopicViewPolicy = policy;
	    vDirtyMask |= View.MONOSCOPIC_VIEW_POLICY_DIRTY;
	}
	repaint();
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>Canvas3D.getMonoscopicViewPolicy</code>
     */
    public int getMonoscopicViewPolicy() {
	return this.monoscopicViewPolicy;
    }

    /**
     * Sets the coexistenceCentering enable flag to true or false.
     * If the coexistenceCentering flag is true, the center of
     * coexistence in image plate coordinates, as specified by the
     * trackerBaseToImagePlate transform, is translated to the center
     * of either the window or the screen in image plate coordinates,
     * according to the value of windowMovementPolicy.
     *
     * <p>
     * By default, coexistenceCentering is enabled.  It should be
     * disabled if the trackerBaseToImagePlate calibration transform
     * is set to a value other than the identity (for example, when
     * rendering to multiple screens or when head tracking is
     * enabled).  This flag is ignored for HMD mode, or when the
     * coexistenceCenterInPworldPolicy is <i>not</i>
     * NOMINAL_SCREEN.
     *
     * @param flag the new coexistenceCentering enable flag
     *
     * @since Java 3D 1.2
     */
    public void setCoexistenceCenteringEnable(boolean flag) {
	synchronized(this) {
	    this.coexistenceCenteringEnable = flag;
	    vDirtyMask |= View.COEXISTENCE_CENTERING_ENABLE_DIRTY;
	}
	repaint();
    }

    /**
     * Retrieves the coexistenceCentering enable flag.
     *
     * @return the current coexistenceCentering enable flag
     *
     * @since Java 3D 1.2
     */
    public boolean getCoexistenceCenteringEnable() {
	return this.coexistenceCenteringEnable;
    }

    /**
     * Sets the compatibility mode enable flag to true or false.
     * Compatibility mode is disabled by default.
     * @param flag the new compatibility mode enable flag
     */
    public void setCompatibilityModeEnable(boolean flag) {
	synchronized(this) {
	    this.compatibilityModeEnable = flag;
	    vDirtyMask |= View.COMPATIBILITY_MODE_DIRTY;
	}
	repaint();
    }

    /**
     * Retrieves the compatibility mode enable flag.
     * @return the current compatibility mode enable flag
     */
    public boolean getCompatibilityModeEnable() {
	return this.compatibilityModeEnable;
    }

    /**
     * Compatibility mode method that specifies a viewing frustum for
     * the left eye that transforms points in Eye Coordinates (EC) to
     * Clipping Coordinates (CC).
     * If compatibility mode is disabled, then this transform is not used;
     * the actual projection is derived from other values.
     * In monoscopic mode, only the left eye projection matrix is used.
     * @param projection the new left eye projection transform
     * @exception RestrictedAccessException if compatibility mode is disabled.
     */
    public void setLeftProjection(Transform3D projection) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View2"));
	}

	synchronized(this) {
	    compatLeftProjection.setWithLock(projection);
	    vDirtyMask |= View.COMPATIBILITY_MODE_DIRTY;
	}
	repaint();
    }

    /**
     * Compatibility mode method that specifies a viewing frustum for
     * the right eye that transforms points in Eye Coordinates (EC) to
     * Clipping Coordinates (CC).
     * If compatibility mode is disabled, then this transform is not used;
     * the actual projection is derived from other values.
     * In monoscopic mode, the right eye projection matrix is ignored.
     * @param projection the new right eye projection transform
     * @exception RestrictedAccessException if compatibility mode is disabled.
     */
    public void setRightProjection(Transform3D projection) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View2"));
	}

	synchronized(this) {
	    compatRightProjection.setWithLock(projection);
	    vDirtyMask |= View.COMPATIBILITY_MODE_DIRTY;
	}

	repaint();
    }

    /**
     * Compatibility mode method that retrieves the current
     * compatibility mode projection transform for the left eye and
     * places it into the specified object.
     * @param projection the Transform3D object that will receive the
     * projection
     * @exception RestrictedAccessException if compatibility mode is disabled.
     */
    public void getLeftProjection(Transform3D projection) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View4"));
	}

	projection.set(compatLeftProjection);
    }

    /**
     * Compatibility mode method that retrieves the current
     * compatibility mode projection transform for the right eye and
     * places it into the specified object.
     * @param projection the Transform3D object that will receive the
     * projection
     * @exception RestrictedAccessException if compatibility mode is disabled.
     */
    public void getRightProjection(Transform3D projection) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View4"));
	}

	projection.set(compatRightProjection);
    }

    /**
     * Compatibility mode method that specifies the ViewPlatform
     * Coordinates (VPC) to Eye Coordinates (EC) transform.
     * If compatibility mode is disabled, then this transform
     * is derived from other values and is read-only.
     * @param vpcToEc the new VPC to EC transform
     * @exception RestrictedAccessException if compatibility mode is disabled.
     * @exception BadTransformException if the transform is not affine.
     */
    public void setVpcToEc(Transform3D vpcToEc) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View6"));
	}

	if (!vpcToEc.isAffine()) {
	    throw new BadTransformException(J3dI18N.getString("View7"));
	}

	synchronized(this) {
	    compatVpcToEc.setWithLock(vpcToEc);
	    vDirtyMask |= View.COMPATIBILITY_MODE_DIRTY;
	}

	repaint();
    }

    /**
     * Compatibility mode method that retrieves the current
     * ViewPlatform Coordinates (VPC) system to
     * Eye Coordinates (EC) transform and copies it into the specified
     * object.
     * @param vpcToEc the object that will receive the vpcToEc transform.
     * @exception RestrictedAccessException if compatibility mode is disabled.
     */
    public void getVpcToEc(Transform3D vpcToEc) {
	if (!compatibilityModeEnable) {
	    throw new RestrictedAccessException(J3dI18N.getString("View8"));
	}

	vpcToEc.set(compatVpcToEc);
    }

    /**
     * Sets the view model's physical body to the PhysicalBody object provided.
     * Java 3D uses the parameters in the PhysicalBody to ensure accurate
     * image and sound generation when in head-tracked mode.
     * @param physicalBody the new PhysicalBody object
     */
    public void setPhysicalBody(PhysicalBody physicalBody) {
	// need to synchronize variable activateStatus
	synchronized (canvasList) {
	    if (activeStatus) {
		if (this.physicalBody != null) {
		    this.physicalBody.removeUser(this);
		}
		physicalBody.addUser(this);
	    }
	}
	this.physicalBody = physicalBody;
	repaint();
    }

    /**
     * Returns a reference to the view model's PhysicalBody object.
     * @return the view object's PhysicalBody object
     */
    public PhysicalBody getPhysicalBody() {
	return this.physicalBody;
    }

    /**
     * Sets the view model's physical environment to the PhysicalEnvironment
     * object provided.
     * @param physicalEnvironment the new PhysicalEnvironment object
     */
    public void setPhysicalEnvironment(PhysicalEnvironment physicalEnvironment) {
	synchronized (canvasList) {
	    if (activeStatus) {
		if (this.physicalEnvironment != null) {
		    this.physicalEnvironment.removeUser(this);
		}
		physicalEnvironment.addUser(this);
	    }
	}
	this.physicalEnvironment = physicalEnvironment;


	if ((viewPlatform != null) && viewPlatform.isLive()) {
	    VirtualUniverse.mc.postRequest(MasterControl.PHYSICAL_ENV_CHANGE, this);
	}
	repaint();
    }

    /**
     * Returns a reference to the view model's PhysicalEnvironment object.
     * @return the view object's PhysicalEnvironment object
     */
    public PhysicalEnvironment getPhysicalEnvironment() {
	return this.physicalEnvironment;
    }

    /**
     * Sets the screen scale value for this view.
     * This is used when the screen scale policy is SCALE_EXPLICIT.
     * The default value is 1.0 (i.e., unscaled).
     * @param scale the new screen scale
     */
    public void setScreenScale(double scale) {
	synchronized(this) {
	    this.screenScale = scale;
	    vDirtyMask |= View.SCREEN_SCALE_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the current screen scale value
     * @return the current screen scale value
     */
    public double getScreenScale() {
	return this.screenScale;
    }

    /**
     * Sets the field of view used to compute the projection transform.
     * This is used when head tracking is disabled and when the Canvas3D's
     * windowEyepointPolicy is RELATIVE_TO_FIELD_OF_VIEW.
     * @param fieldOfView the new field of view in radians
     */
    public void setFieldOfView(double fieldOfView) {
	synchronized(this) {
	    this.fieldOfView = fieldOfView;
	    vDirtyMask |= View.FIELD_OF_VIEW_DIRTY;
	}
	repaint();

    }

    /**
     * Returns the current field of view.
     * @return the current field of view in radians
     */
    public double getFieldOfView() {
	return this.fieldOfView;
    }


    /**
     * Sets the position of the manual left eye in coexistence
     * coordinates.  This value determines eye placement when a head
     * tracker is not in use and the application is directly controlling
     * the eye position in coexistence coordinates.  This value is
     * ignored when in head-tracked mode or when the
     * windowEyePointPolicy is <i>not</i> RELATIVE_TO_COEXISTENCE.
     *
     * @param position the new manual left eye position
     *
     * @since Java 3D 1.2
     */
    public void setLeftManualEyeInCoexistence(Point3d position) {
	synchronized(this) {
	    leftManualEyeInCoexistence.set(position);
	    vDirtyMask |= View.LEFT_MANUAL_EYE_IN_COEXISTENCE_DIRTY;
	}
	repaint();
    }

    /**
     * Sets the position of the manual right eye in coexistence
     * coordinates.  This value determines eye placement when a head
     * tracker is not in use and the application is directly controlling
     * the eye position in coexistence coordinates.  This value is
     * ignored when in head-tracked mode or when the
     * windowEyePointPolicy is <i>not</i> RELATIVE_TO_COEXISTENCE.
     *
     * @param position the new manual right eye position
     *
     * @since Java 3D 1.2
     */
    public void setRightManualEyeInCoexistence(Point3d position) {
	synchronized(this) {
	    rightManualEyeInCoexistence.set(position);
	    vDirtyMask |= View.RIGHT_MANUAL_EYE_IN_COEXISTENCE_DIRTY;
	}
	repaint();
    }

    /**
     * Retrieves the position of the user-specified, manual left eye
     * in coexistence
     * coordinates and copies that value into the object provided.
     * @param position the object that will receive the position
     *
     * @since Java 3D 1.2
     */
    public void getLeftManualEyeInCoexistence(Point3d position) {
	position.set(leftManualEyeInCoexistence);
    }

    /**
     * Retrieves the position of the user-specified, manual right eye
     * in coexistence
     * coordinates and copies that value into the object provided.
     * @param position the object that will receive the position
     *
     * @since Java 3D 1.2
     */
    public void getRightManualEyeInCoexistence(Point3d position) {
	position.set(rightManualEyeInCoexistence);
    }


    /**
     * Sets the view model's front clip distance.
     * This value specifies the distance away from the eyepoint
     * in the direction of gaze where objects stop disappearing.
     * Objects closer to the eye than the front clip
     * distance are not drawn. The default value is 0.1 meters.
     * <p>
     * There are several considerations that need to be taken into
     * account when choosing values for the front and back clip
     * distances.
     * <ul>
     * <li>The front clip distance must be greater than
     * 0.0 in physical eye coordinates.</li>
     * <li>The front clipping plane must be in front of the
     * back clipping plane, that is, the front clip distance
     * must be less than the back clip distance in physical eye
     * coordinates.</li>
     * <li>The front and back clip distances, in physical
     * eye coordinates, must be less than the largest positive
     * single-precision floating point value, <code>Float.MAX_VALUE</code>.
     * In practice, since these physical eye coordinate distances are in
     * meters, the values should be <i>much</i> less than that.
     * <li>The ratio of the back distance divided by the front distance,
     * in physical eye coordinates, affects Z-buffer precision. This
     * ratio should be less than about 3000 in order to accommodate 16-bit
     * Z-buffers. Values of 100 to less than 1000 will produce better
     * results.</li>
     * </ul>
     * Violating any of the above rules will result in undefined
     * behavior. In many cases, no picture will be drawn.
     *
     * @param distance the new front clip distance
     * @see #setBackClipDistance
     */
    public void setFrontClipDistance(double distance) {
	synchronized(this) {
	    this.frontClipDistance = distance;
	    vDirtyMask |= View.CLIP_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the view model's front clip distance.
     * @return the current front clip distance
     */
    public double getFrontClipDistance() {
	return this.frontClipDistance;
    }

    /**
     * Sets the view model's back clip distance.
     * The parameter specifies the distance from the eyepoint
     * in the direction of gaze to where objects begin disappearing.
     * Objects farther away from the eye than the
     * back clip distance are not drawn.
     * The default value is 10.0 meters.
     * <p>
     * There are several considerations that need to be taken into
     * account when choosing values for the front and back clip
     * distances. These are enumerated in the description of
     * <a href=#setFrontClipDistance(double)>setFrontClipDistance</a>.
     * <p>
     * Note that this attribute is only used if there is no Clip node
     * that is in scope of the view platform associated with this view.
     * @param distance the new back clip distance
     * @see #setFrontClipDistance
     * @see Clip#setBackDistance
     */
    public void setBackClipDistance(double distance) {
	synchronized(this) {
	    this.backClipDistance = distance;
	    vDirtyMask |= View.CLIP_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the view model's back clip distance.
     * @return the current back clip distance
     */
    public double getBackClipDistance() {
	return this.backClipDistance;
    }

    /**
     * Retrieves the user-head to virtual-world transform
     * and copies that value into the transform provided.
     * @param t the Transform3D object that will receive the transform
     */
    public void getUserHeadToVworld(Transform3D t) {

        if( userHeadToVworldEnable ) {

           // get the calculated userHeadToVworld transform
           // from the view cache.
           // grab the first canvas -- not sure for multiple canvases
		Canvas3D canvas = this.canvases.firstElement();
           synchronized(canvas.canvasViewCache) {
              t.set(canvas.canvasViewCache.getHeadToVworld());
           }
        }else {
           throw new RestrictedAccessException(J3dI18N.getString("View9"));
        }
    }

    /**
     * Sets the view model's front clip policy, the policy Java 3D uses
     * in computing where to place the front clip plane. The variable
     * can contain one of:
     * <UL>
     * <LI>VIRTUAL_EYE, to specify that the associated distance is
     * from the eye and in units of virtual distance
     * </LI>
     * <LI>PHYSICAL_EYE, to specify that the associated distance is
     * from the eye and in units of physical distance (meters)
     * </LI>
     * <LI>VIRTUAL_SCREEN, to specify that the associated distance is
     * from the screen and in units of virtual distance
     * </LI>
     * <LI>PHYSICAL_SCREEN, to specify that the associated distance is
     * from the screen and in units of physical distance (meters)
     * </LI>
     * </UL>
     * The default front clip policy is PHYSICAL_EYE.
     * @param policy the new policy, one of PHYSICAL_EYE, PHYSICAL_SCREEN,
     * VIRTUAL_EYE, or VIRTUAL_SCREEN
     */
    public void setFrontClipPolicy(int policy) {
	synchronized(this) {
	    this.frontClipPolicy = policy;
	    vDirtyMask |= View.CLIP_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the view model's current front clip policy.
     * @return one of:
     * VIRTUAL_EYE, PHYSICAL_EYE, VIRTUAL_SCREEN, or PHYSICAL_SCREEN
     */
    public int getFrontClipPolicy() {
	return this.frontClipPolicy;
    }

    /**
     * Sets the view model's back clip policy, the policy Java 3D uses
     * in computing where to place the back clip plane. The variable
     * can contain one of:
     * <UL>
     * <LI>VIRTUAL_EYE, to specify that the associated distance is
     * from the eye and in units of virtual distance
     * </LI>
     * <LI>PHYSICAL_EYE, to specify that the associated distance is
     * from the eye and in units of physical distance (meters)
     * </LI>
     * <LI>VIRTUAL_SCREEN, to specify that the associated distance is
     * from the screen and in units of virtual distance
     * </LI>
     * <LI>PHYSICAL_SCREEN, to specify that the associated distance is
     * from the screen and in units of physical distance (meters)
     * </LI>
     * </UL>
     * The default back clip policy is PHYSICAL_EYE.
     * @param policy the new policy, one of PHYSICAL_EYE, PHYSICAL_SCREEN,
     * VIRTUAL_EYE, or VIRTUAL_SCREEN
     */
    public void setBackClipPolicy(int policy) {
	synchronized(this) {
	    this.backClipPolicy = policy;
	    vDirtyMask |= View.CLIP_DIRTY;
	}
	repaint();
    }

    /**
     * Returns the view model's current back clip policy.
     * @return one of:
     * VIRTUAL_EYE, PHYSICAL_EYE, VIRTUAL_SCREEN, or PHYSICAL_SCREEN
     */
    public int getBackClipPolicy() {
	return this.backClipPolicy;
    }

    /**
     * Sets the visibility policy for this view.  This attribute
     * is one of:
     * <UL>
     * <LI>VISIBILITY_DRAW_VISIBLE, to specify that only visible objects
     * are drawn.
     * </LI>
     * <LI>VISIBILITY_DRAW_INVISIBLE, to specify that only invisible objects
     * are drawn.
     * </LI>
     * <LI>VISIBILITY_DRAW_ALL, to specify that both visible and
     * invisible objects are drawn.
     * </LI>
     * </UL>
     * The default visibility policy is VISIBILITY_DRAW_VISIBLE.
     *
     * @param policy the new policy, one of VISIBILITY_DRAW_VISIBLE,
     * VISIBILITY_DRAW_INVISIBLE, or VISIBILITY_DRAW_ALL.
     *
     * @see RenderingAttributes#setVisible
     *
     * @since Java 3D 1.2
     */
    public void setVisibilityPolicy(int policy) {

	synchronized(this) {
	    this.visibilityPolicy = policy;
	    vDirtyMask |= View.VISIBILITY_POLICY_DIRTY;
	}

	if (activeStatus && isRunning) {

	    J3dMessage vpMessage = new J3dMessage();
	    vpMessage.universe = universe;
	    vpMessage.view = this;
	    vpMessage.type = J3dMessage.UPDATE_VIEW;
	    vpMessage.threads = J3dThread.UPDATE_RENDER;
	    vpMessage.args[0] = this;
	    synchronized(((ViewPlatformRetained)viewPlatform.retained).sphere) {
		vpMessage.args[1] = new Float(((ViewPlatformRetained)viewPlatform.
					       retained).sphere.radius);
	    }
	    vpMessage.args[2] = new Integer(OTHER_ATTRS_CHANGED);
	    vpMessage.args[3] = new Integer(transparencySortingPolicy);
	    VirtualUniverse.mc.processMessage(vpMessage);
	}
    }

    /**
     * Retrieves the current visibility policy.
     * @return one of:
     * VISIBILITY_DRAW_VISIBLE,
     * VISIBILITY_DRAW_INVISIBLE, or VISIBILITY_DRAW_ALL.
     *
     * @since Java 3D 1.2
     */
    public int getVisibilityPolicy() {
	return this.visibilityPolicy;
    }

    /**
     * Sets the transparency sorting policy for this view.  This attribute
     * is one of:
     *
     * <UL>
     * <LI>TRANSPARENCY_SORT_NONE, to specify that no depth sorting of
     * transparent objects is performed.  Transparent objects are
     * drawn after opaque objects, but are not sorted from back to
     * front.</LI>
     *
     * <LI>TRANSPARENCY_SORT_GEOMETRY, to specify that transparent
     * objects are depth-sorted on a per-geometry basis.  Each
     * geometry object of each transparent Shape3D node is drawn from
     * back to front.  Note that this policy will not split geometry
     * into smaller pieces, so intersecting or intertwined objects may
     * not be sorted correctly.</LI>
     * </UL>
     *
     * The default policy is TRANSPARENCY_SORT_NONE.
     *
     * @param policy the new policy, one of TRANSPARENCY_SORT_NONE
     * or TRANSPARENCY_SORT_GEOMETRY.
     *
     * @since Java 3D 1.3
     */
    public void setTransparencySortingPolicy(int policy) {
	if (policy == transparencySortingPolicy) {
	    return;
	}

	transparencySortingPolicy = policy;
	if (activeStatus && isRunning) {

	    J3dMessage vpMessage = new J3dMessage();
	    vpMessage.universe = universe;
	    vpMessage.view = this;
	    vpMessage.type = J3dMessage.UPDATE_VIEW;
	    vpMessage.threads = J3dThread.UPDATE_RENDER;
	    vpMessage.args[0] = this;
	    vpMessage.args[1] = null;
	    vpMessage.args[2] = new Integer(TRANSP_SORT_POLICY_CHANGED);
	    vpMessage.args[3] = new Integer(policy);
	    VirtualUniverse.mc.processMessage(vpMessage);
	}
    }

    /**
     * Retrieves the current transparency sorting policy.
     * @return one of:
     * TRANSPARENCY_SORT_NONE or TRANSPARENCY_SORT_GEOMETRY.
     *
     * @since Java 3D 1.3
     */
    public int getTransparencySortingPolicy() {
	return this.transparencySortingPolicy;
    }

    /**
     * Turns head tracking on or off for this view.
     * @param flag specifies whether head tracking is enabled or
     * disabled for this view
     */
    public void setTrackingEnable(boolean flag) {

	synchronized(this) {
	    this.trackingEnable = flag;
	    vDirtyMask |= View.TRACKING_ENABLE_DIRTY;
	}

	repaint();
    }

    /**
     * Returns a status flag indicating whether or not head tracking
     * is enabled.
     * @return a flag telling whether head tracking is enabled
     */
    public boolean getTrackingEnable() {
	return this.trackingEnable;
    }

    /**
     * Turns on or off the continuous
     * updating of the userHeadToVworld transform.
     * @param flag enables or disables continuous updating
     */
    public void setUserHeadToVworldEnable(boolean flag) {

	synchronized(this) {
	    userHeadToVworldEnable = flag;
	    vDirtyMask |= View.USER_HEAD_TO_VWORLD_ENABLE_DIRTY;
	}
	repaint();
    }

    /**
     * Returns a status flag indicating whether or not
     * Java 3D is continuously updating the userHeadToVworldEnable transform.
     * @return a flag indicating if continuously updating userHeadToVworld
     */
    public boolean getUserHeadToVworldEnable() {
	return userHeadToVworldEnable;
    }

    /**
     * Computes the sensor to virtual-world transform
     * and copies that value into the transform provided.
     * The computed transforms takes points in the sensor's coordinate
     * system and produces the point's corresponding value in
     * virtual-world coordinates.
     * @param sensor the sensor in question
     * @param t the object that will receive the transform
     */
    public void getSensorToVworld(Sensor sensor, Transform3D t) {
        // grab the first canvas -- not sure for multiple canvases
	Canvas3D canvas = this.canvases.firstElement();
        Transform3D localTrans = new Transform3D();
        synchronized(canvas.canvasViewCache) {
              t.set(canvas.canvasViewCache.getVworldToTrackerBase());
        }
        t.invert();
        sensor.getRead(localTrans);
        t.mul(localTrans);
    }

    /**
     * Retrieves the position of the specified Sensor's
     * hotspot in virtual-world coordinates
     * and copies that value into the position provided.
     * This value is derived from other values and is read-only.
     * @param sensor the sensor in question
     * @param position the variable that will receive the position
     */
    public void getSensorHotspotInVworld(Sensor sensor,
					 Point3f position) {

        Transform3D sensorToVworld = new Transform3D();
        Point3d hotspot3d = new Point3d();

	getSensorToVworld(sensor, sensorToVworld);
        sensor.getHotspot(hotspot3d);
        position.set(hotspot3d);
        sensorToVworld.transform(position);
    }

    /**
     * Retrieves the position of the specified Sensor's
     * hotspot in virtual-world coordinates
     * and copies that value into the position provided.
     * This value is derived from other values and is read-only.
     * @param sensor the sensor in question
     * @param position the variable that will receive the position
     */
    public void getSensorHotspotInVworld(Sensor sensor,
					 Point3d position) {

        Transform3D sensorToVworld = new Transform3D();

	getSensorToVworld(sensor, sensorToVworld);
        sensor.getHotspot(position);
        sensorToVworld.transform(position);
    }

    /**
     * Sets given Canvas3D at the given index position.
     * @param canvas3D the given Canvas3D to be set
     * @param index the position to be set
     * @exception IllegalStateException if the specified canvas is
     * a stereo canvas with a monoscopicEyePolicy of CYCLOPEAN_EYE_VIEW,
     * and the viewPolicy for this view is HMD_VIEW
     * @exception IllegalSharingException if the specified canvas is
     * associated with another view
     */
    public void setCanvas3D(Canvas3D canvas3D, int index) {

	if((viewPolicy == HMD_VIEW) &&
	   (canvas3D.monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW) &&
	   (!canvas3D.useStereo)){

	    throw new
		IllegalStateException(J3dI18N.getString("View31"));
	}

	Canvas3D cv;

	synchronized(canvasList) {
            if (canvas3D.getView() != null)
		throw new IllegalSharingException(J3dI18N.getString("View10"));
		cv = canvases.elementAt(index);
	    canvases.setElementAt(canvas3D, index);
	    removeFromCanvasList(cv);
	    addToCanvasList(canvas3D);
	    canvasesDirty = true;
	}

	canvas3D.setView(this);
	cv.setView(null);

	if (canvas3D.added) {
	    evaluateActive();
	}
	if (cv.added) {
	    evaluateActive();
	}

    }

    /**
     * Gets the Canvas3D at the specified index position.
     * @param index the position from which to get Canvas3D object
     * @return the Canvas3D at the sprcified index position
     */
    public Canvas3D getCanvas3D(int index){
	return this.canvases.elementAt(index);
    }

    /**
     * Gets the enumeration object of all the Canvas3Ds.
     * @return the enumeration object of all the Canvas3Ds.
     */
    public Enumeration<Canvas3D> getAllCanvas3Ds(){
        return canvases.elements();
    }

    /**
     * Returns the number of Canvas3Ds in this View.
     * @return the number of Canvas3Ds in this View
     *
     * @since Java 3D 1.2
     */
    public int numCanvas3Ds() {
        return canvases.size();
    }

    /**
     * Adds the given Canvas3D at the end of the list.
     * @param canvas3D the Canvas3D to be added
     * @exception IllegalStateException if the specified canvas is
     * a stereo canvas with a monoscopicEyePolicy of CYCLOPEAN_EYE_VIEW,
     * and the viewPolicy for this view is HMD_VIEW
     * @exception IllegalSharingException if the specified canvas is
     * associated with another view
     */
    public void addCanvas3D(Canvas3D canvas3D){

	if((viewPolicy == HMD_VIEW) &&
	   (canvas3D.monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW) &&
	   (!canvas3D.useStereo)) {
	    throw new
		IllegalStateException(J3dI18N.getString("View31"));
	}

	synchronized(canvasList) {
            if (canvas3D.getView() != null)
		throw new IllegalSharingException(J3dI18N.getString("View10"));
	    canvases.addElement(canvas3D);
	    addToCanvasList(canvas3D);
	    canvasesDirty = true;
	}

	canvas3D.setView(this);

	if (canvas3D.added) {
	    if ((canvas3D.visible || canvas3D.offScreen) &&
		canvas3D.firstPaintCalled) {
		canvas3D.active = true;
	    }
	    evaluateActive();
	}
    }

    /**
     * Inserts the Canvas3D at the given index position.
     * @param canvas3D the Canvas3D to be inserted
     * @param index the position to be inserted at
     * @exception IllegalStateException if the specified canvas is
     * a stereo canvas with a monoscopicEyePolicy of CYCLOPEAN_EYE_VIEW,
     * and the viewPolicy for this view is HMD_VIEW
     * @exception IllegalSharingException if the specified canvas is
     * associated with another view
     */
    public void insertCanvas3D(Canvas3D canvas3D, int index){

	if((viewPolicy == HMD_VIEW) &&
	   (canvas3D.monoscopicViewPolicy == View.CYCLOPEAN_EYE_VIEW) &&
	   (!canvas3D.useStereo)) {
	    throw new
		IllegalStateException(J3dI18N.getString("View31"));
	}

	synchronized(canvasList) {
            if (canvas3D.getView() != null)
	     throw new IllegalSharingException(J3dI18N.getString("View10"));
	    this.canvases.insertElementAt(canvas3D, index);
	    addToCanvasList(canvas3D);
	    canvasesDirty = true;
	}

	canvas3D.setView(this);

	if (canvas3D.added) {
	    if ((canvas3D.visible || canvas3D.offScreen) &&
		canvas3D.firstPaintCalled) {
		canvas3D.active = true;
	    }
	    evaluateActive();
	}
    }

    /**
     * Removes the Canvas3D from the given index position.
     * @param index the position of Canvas3D object to be removed
     */
    public void removeCanvas3D(int index) {
	// index -1 is possible if the view is unregistered first
	// because viewPlatform is clearLived,
	// and then removeCanvas from the view
	if (index == -1)
	    return;

	Canvas3D cv;

	synchronized(canvasList) {
		cv = canvases.elementAt(index);

	    canvases.removeElementAt(index);
	    removeFromCanvasList(cv);
	    canvasesDirty = true;
	}

	// reset canvas will set view to null also
	VirtualUniverse.mc.postRequest(MasterControl.RESET_CANVAS,
                                      cv);
	cv.pendingView = null;

	computeCanvasesCached();

	if (cv.added) {
	    cv.active = false;
	    evaluateActive();
	}
	if (universe != null) {
	    universe.waitForMC();
	}
    }


    /**
     * Retrieves the index of the specified Canvas3D in
     * this View's list of Canvas3Ds
     *
     * @param canvas3D the Canvas3D to be looked up.
     * @return the index of the specified Canvas3D;
     * returns -1 if the object is not in the list.
     *
     * @since Java 3D 1.3
     */
    public int indexOfCanvas3D(Canvas3D canvas3D) {
	return canvases.indexOf(canvas3D);
    }


    /**
     * Removes the specified Canvas3D from this View's
     * list of Canvas3Ds.
     * If the specified object is not in the list, the list is not modified.
     *
     * @param canvas3D the Canvas3D to be removed.
     */
    public void removeCanvas3D(Canvas3D canvas3D) {
	removeCanvas3D(canvases.indexOf(canvas3D));
    }


    /**
     * Removes all Canvas3Ds from this View.
     *
     * @since Java 3D 1.3
     */
    public void removeAllCanvas3Ds() {
	LinkedList<Canvas3D> tmpCanvases = new LinkedList<Canvas3D>();

	synchronized(canvasList) {
	    int numCanvases = canvases.size();

	    // Remove in reverse order to ensure valid indices
	    for (int index = numCanvases - 1; index >= 0; index--) {
			Canvas3D cv = canvases.elementAt(index);

		// Record list of canvases to be deleted;
		tmpCanvases.add(cv);

		canvases.removeElementAt(index);
		removeFromCanvasList(cv);
		canvasesDirty = true;
	    }
	}

	// ISSUE 83: postRequest must *not* be called while holding
	// canvasList lock. Holding the lock can cause a deadlock.

	Iterator<Canvas3D> iterator = tmpCanvases.iterator();
	while (iterator.hasNext()) {
	    Canvas3D cv = iterator.next();

	    // reset canvas will set view to null also
	    VirtualUniverse.mc.postRequest(MasterControl.RESET_CANVAS,
					   cv);
	    cv.pendingView = null;

	    if (cv.added) {
		cv.active = false;
	    }
	}

	computeCanvasesCached();

	evaluateActive();

	if (universe != null) {
	    universe.waitForMC();
	}
    }


    // This adds this canvas and its screen to the screen list.
    // Locks are already acquired before this is called.
    private void addToCanvasList(Canvas3D c) {

	for (int i=screenList.size()-1; i>=0; i--) {
		if (screenList.get(i) == c.screen) {
		// This is the right screen slot
			canvasList.get(i).add(c);
		canvasesDirty = true;
		return;
	    }
	}

	// Add a screen slot
	screenList.add(c.screen);
	ArrayList<Canvas3D> clist = new ArrayList<Canvas3D>();
	canvasList.add(clist);
	clist.add(c);
	canvasesDirty = true;
    }

    // This removes this canvas and its screen from the screen list
    // Locks are already acquired before this is called.
    private void removeFromCanvasList(Canvas3D c) {

	for (int i=screenList.size()-1; i>=0; i--) {
		if (screenList.get(i) == c.screen) {
		// This is the right screen slot
			ArrayList<Canvas3D> clist = canvasList.get(i);
		clist.remove(clist.indexOf(c));

		if (clist.size() == 0) {
		    canvasList.remove(i);
		    screenList.remove(i);
		    canvasesDirty = true;
		}
		return;
	    }
	}
    }

    // Locks are not acquired before this is called.
    void computeCanvasesCached() {

        synchronized (canvasList) {
	    ArrayList<Canvas3D> cv;
	    int len = canvases.size();

	    Canvas3D newCachedCanvases[] = new Canvas3D[len];
	    for (int i=0; i < len; i++) {
			newCachedCanvases[i] = canvases.get(i);
	    }
	    // Do this in one instruction so there is no need to
	    // synchronized getCanvases()

	    cachedCanvases = newCachedCanvases;
	    len = 0;
	    longestScreenList = 0;
	    cachedCanvasList = new Canvas3D[canvasList.size()][0];
	    for (int i=0; i < cachedCanvasList.length; i++) {
			cv = canvasList.get(i);
		len = cv.size();
		cachedCanvasList[i] = new Canvas3D[len];
		for (int j=0; j < len; j++) {
				cachedCanvasList[i][j] = cv.get(j);
		}

		if (len > longestScreenList) {
		    longestScreenList = len;
		}
	    }
	    len = screenList.size();
	    Screen3D newCachedScreens[] = new Screen3D[len];

	    for (int i=0; i < len; i++) {
			newCachedScreens[i] = screenList.get(i);
	    }
	    // Do this in one instruction so there is no need to
	    // synchronized getScreens()
	    cachedScreens = newCachedScreens;
	    canvasesDirty = false;
	}
    }

    // This creates a 2 dimentional list of canvases
    // ONLY MC can call this procedure with canCompute=true,
    // since MC want the result return by
    // evaluateCanvases and updateWorkThreads agree to each other,
    // so only evaluateCanvases can compute a new list.
    // Other threads should use getCanvasList(false).
    Canvas3D[][] getCanvasList(boolean canCompute) {
	if (canvasesDirty && canCompute) {
	    computeCanvasesCached();
	}
	return cachedCanvasList;
    }

    // assume getCanvasList is called before
    int getLongestScreenList() {
	return longestScreenList;
    }

    // assume getCanvasList is called before
    Canvas3D[] getCanvases() {
	return cachedCanvases;
    }

    // assume getCanvasList is called before
    Screen3D[] getScreens() {
	return cachedScreens;
    }

    Canvas3D getFirstCanvas() {
	synchronized (canvasList) {
	    if (canvases.size() > 0) {
		return canvases.elementAt(0);
	    }
	    return null;
	}
    }

    /**
     * This method returns the time at which the most recent rendering
     * frame started.  It is defined as the number of milliseconds
     * since January 1, 1970 00:00:00 GMT.
     * Since multiple canvases might be attached to this View,
     * the start of a frame is defined as the point in time just prior
     * to clearing any canvas attached to this view.
     * @return the time at which the most recent rendering frame started
     */
    public long getCurrentFrameStartTime() {
	synchronized (frameStartTimes) {
	    return currentFrameStartTime;
	}
    }

    /**
     * This method returns the duration, in milliseconds, of the most
     * recently completed rendering frame.  The time taken to render
     * all canvases attached to this view is measured.  This duration
     * is computed as the difference between the start of the most recently
     * completed frame and the end of that frame.
     * Since multiple canvases might be attached to this View,
     * the start of a frame is defined as the point in time just prior
     * to clearing any canvas attached to this view--before preRender
     * is called for any canvas.  Similarly, the end of a frame is
     * defined as the point in time just after swapping the buffer for
     * all canvases--after postSwap is called for all canvases.
     * Note that since the frame duration is measured from start to stop
     * for this view only, the value returned is not the same as
     * frame rate; it measures only the rendering time for this view.
     *
     * @return the duration, in milliseconds, of the most recently
     * completed rendering frame
     */
    public long getLastFrameDuration() {
	synchronized (frameStartTimes) {
	    return currentFrameDuration;
	}
    }

    /**
     * This method returns the frame number for this view.  The frame
     * number starts at 0 and is incremented at the start of each
     * frame--prior to clearing all the canvases attached to this
     * view.
     *
     * @return the current frame number for this view
     */
    public long getFrameNumber() {
	synchronized (frameStartTimes) {
	    return currentFrameNumber;
	}
    }

    /**
     * Retrieves the implementation-dependent maximum number of
     * frames whose start times will be recorded by the system.  This
     * value is guaranteed to be at least 10 for all implementations
     * of the Java 3D API.
     * @return the maximum number of frame start times recorded
     */
    public static int getMaxFrameStartTimes() {
	return (NUMBER_FRAME_START_TIMES);
    }

    /**
     * Copies the last <i>k</i> frame start time values into
     * the user-specified array.  The most recent frame start time is
     * copied to location 0 of the array, the next most recent frame
     * start time is copied into location 1 of the array, and so forth.
     * If times.length is smaller than
     * maxFrameStartTimes, then only the last times.length values are
     * copied.  If times.length is greater than maxFrameStartTimes,
     * then all array elements after index maxFrameStartTimes-1 are
     * set to 0.
     *
     * @return the frame number of the most recent frame in the array
     *
     * @see #setMinimumFrameCycleTime
     */
    public long getFrameStartTimes(long[] times) {
	int index, i, loopCount;
	long lastFrameNumber;

	synchronized (frameStartTimes) {
	    index = currentFrameIndex - 1;
	    if (index < 0) {
	        index = NUMBER_FRAME_START_TIMES - 1;
	    }
	    lastFrameNumber = frameNumbers[index];

	    if (times.length <= NUMBER_FRAME_START_TIMES) {
	        loopCount = times.length;
	    } else {
	        loopCount = NUMBER_FRAME_START_TIMES;
	    }

            for (i=0; i<loopCount; i++) {
                times[i] = frameStartTimes[index];
                index--;
                if (index < 0) {
                    index = NUMBER_FRAME_START_TIMES - 1;
                }
            }

	    if (times.length > NUMBER_FRAME_START_TIMES) {
	        for (; i<times.length; i++) {
		    times[i] = 0;
	        }
	    }
	}

	return (lastFrameNumber);
    }

    /**
     * Sets the minimum frame cycle time, in milliseconds, for this
     * view.  The Java 3D renderer will ensure that the time between
     * the start of each successive frame is at least the specified
     * number of milliseconds.  The default value is 0.
     *
     * @param minimumTime the minimum number of milliseconds between
     * successive frames
     *
     * @exception IllegalArgumentException if <code>minimumTime < 0</code>
     *
     * @see #getFrameStartTimes
     *
     * @since Java 3D 1.2
     */
    public void setMinimumFrameCycleTime(long minimumTime) {
	if (minimumTime < 0L)
	    throw new IllegalArgumentException(J3dI18N.getString("View27"));

	minFrameCycleTime = minimumTime;
	VirtualUniverse.mc.setWork();
    }

    /**
     * Retrieves the minimum frame cycle time, in milliseconds, for this view.
     * @return the minimum frame cycle time for this view.
     *
     * @see #getFrameStartTimes
     *
     * @since Java 3D 1.2
     */
    public long getMinimumFrameCycleTime() {
	return minFrameCycleTime;
    }


    /**
     * This adds a frame time to the this of frame times
     */
    void setFrameTimingValues() {

	synchronized (frameStartTimes) {
	    if (currentFrameIndex == NUMBER_FRAME_START_TIMES) {
		currentFrameIndex = 0;
	    }

	    frameNumbers[currentFrameIndex] = frameNumber;

	    frameStartTimes[currentFrameIndex++] = startTime;
	    currentFrameStartTime = startTime;
	    currentFrameDuration = stopTime - startTime;
	    currentFrameNumber = frameNumber;
	}
    }

    /**
     * Return true if maximum fps impose by user reach
     */
    void computeCycleTime() {
	if (minFrameCycleTime == 0) {
	    isMinCycleTimeAchieve = true;
	    sleepTime = 0;
	} else {
	    sleepTime = minFrameCycleTime -
		(J3dClock.currentTimeMillis() - startTime);
	    isMinCycleTimeAchieve = (sleepTime <= 0);
	}
    }


    /**
     * Enables or disables automatic freezing of the depth buffer for
     * objects rendered
     * during the transparent rendering pass (i.e., objects rendered
     * using alpha blending) for this view.
     * If enabled, depth buffer writes will be disabled during the
     * transparent rendering pass regardless of the value of
     * the depth buffer write enable flag in the RenderingAttributes
     * object for a particular node.
     * This flag is enabled by default.
     * @param flag indicates whether automatic freezing of the depth buffer
     * for transparent/antialiased objects is enabled.
     * @see RenderingAttributes#setDepthBufferWriteEnable
     */
    public void setDepthBufferFreezeTransparent(boolean flag) {
	depthBufferFreezeTransparent = flag;
	repaint();
    }

    /**
     * Retrieves the current value of the depth buffer freeze transparent
     * flag for this view.
     * @return a flag that indicates whether or not the depth
     * buffer is automatically frozen during the transparent rendering pass.
     */
    public boolean getDepthBufferFreezeTransparent() {
	return depthBufferFreezeTransparent;
    }

    /**
     * Enables or disables scene antialiasing for this view.
     * If enabled, the entire scene will be antialiased on
     * each canvas in which scene antialiasing is available.
     * Scene antialiasing is disabled by default.
     * <p>
     * NOTE: Scene antialiasing is ignored in pure immediate mode,
     * but is supported in mixed-immediate mode.
     * @param flag indicates whether scene antialiasing is enabled
     *
     * @see Canvas3D#queryProperties
     */
    public void setSceneAntialiasingEnable(boolean flag) {
	 sceneAntialiasingEnable = flag;
	 repaint();
    }

    /**
     * Returns a flag that indicates whether or not scene antialiasing
     * is enabled for this view.
     * @return a flag that indicates whether scene antialiasing is enabled
     */
    public boolean getSceneAntialiasingEnable() {
	return sceneAntialiasingEnable;
    }

    /**
     * Sets a flag that indicates whether the local eyepoint is used in
     * lighting calculations for perspective projections.
     * If this flag is set to true, the view vector is calculated per-vertex
     * based on the direction from the actual eyepoint to the vertex.
     * If this flag is set to false, a single view vector is computed from
     * the eyepoint to the center of the view frustum.  This is
     * called infinite eye lighting.
     * Local eye lighting is disabled by default, and is ignored for
     * parallel projections.
     * @param flag indicates whether local eye lighting is enabled
     */
    public void setLocalEyeLightingEnable(boolean flag) {
	localEyeLightingEnable = flag;
	repaint();
    }

    /**
     * Retrieves a flag that indicates whether or not local eye lighting
     * is enabled for this view.
     * @return a flag that indicates whether local eye lighting is enabled
     */
    public boolean getLocalEyeLightingEnable() {
	return localEyeLightingEnable;
    }

    /**
     * Attach viewPlatform structure to this view.
     * @param vp the viewPlatform to be attached
     */
    public void attachViewPlatform(ViewPlatform vp) {

	if ((vp != null) && (vp == viewPlatform)) {
	    return;
	}

	if (viewPlatform != null) {
	    ((ViewPlatformRetained)viewPlatform.retained).removeView(this);
	    if (viewPlatform.isLive()) {
		synchronized (evaluateLock) {
		    viewPlatform = null;
		    // cleanup View stuff for the old platform
		    evaluateActive();
		    viewPlatform = vp;
		}
		if (universe != null) {
		    universe.waitForMC();
		}
	    } else {
		viewPlatform = vp;
	    }
	} else {
	    viewPlatform = vp;
	}
	if (vp != null) {
	    if (vp.isLive()) {
		checkView();
	        setUniverse(((ViewPlatformRetained)vp.retained).universe);
	    }
	    ((ViewPlatformRetained)vp.retained).setView(this);
	}

	evaluateActive();
	if ((vp == null) && (universe != null)) {
	    universe.waitForMC();
	}
    }

    /**
     * Retrieves the currently attached ViewPlatform object
     * @return the currently attached ViewPlatform
     */
    public ViewPlatform getViewPlatform() {
	return viewPlatform;
    }

    /**
     * Checks view parameters for consistency
     */
    void checkView() {
	if (physicalBody == null)
	    throw new IllegalStateException(J3dI18N.getString("View13"));

	if (physicalEnvironment == null)
	    throw new IllegalStateException(J3dI18N.getString("View14"));
    }


    /**
     * Stops the behavior scheduler after all
     * currently scheduled behaviors are executed.  Any frame-based
     * behaviors scheduled to wake up on the next frame will be
     * executed at least once before the behavior scheduler is
     * stopped.
     * <p>
     * NOTE: This is a heavy-weight method
     * intended for verification and image capture (recording); it
     * is <i>not</i> intended to be used for flow control.
     * @return a pair of integers that specify the beginning and ending
     * time (in milliseconds since January 1, 1970 00:00:00 GMT)
     * of the behavior scheduler's last pass
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final long[] stopBehaviorScheduler() {
	long[] intervalTime = new long[2];

	if (checkBehaviorSchedulerState("View15", "View16")) {
	    if (activeStatus && isRunning &&
		(universe.behaviorScheduler != null)) {
		// view is active
		universe.behaviorScheduler.stopBehaviorScheduler(intervalTime);
	    } else {
		if ((universe != null) &&
		    (universe.behaviorScheduler != null)) {
		    universe.behaviorScheduler.userStop = true;
		}
	    }
	}
	stopBehavior = true;
	return intervalTime;
    }

    /**
     * Starts the behavior scheduler running after it has been stopped.
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final void startBehaviorScheduler() {
	if (checkBehaviorSchedulerState("View17", "View18")) {
	    if (activeStatus && isRunning &&
		(universe.behaviorScheduler != null)) {
		universe.behaviorScheduler.startBehaviorScheduler();

	    } else {
		if ((universe != null) &&
		    (universe.behaviorScheduler != null)) {
		    universe.behaviorScheduler.userStop = false;
		}
	    }
	}

	stopBehavior = false;
    }

    /**
     * Check if BehaviorScheduler is in valid state to start/stop
     * itself.
     * @param s1 Exception String if method is called from a Canvas3D
     * @param s2 Exception String if method is called from a Behavior method
     * @return true if viewPlatform is live
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     *
     */
    boolean checkBehaviorSchedulerState(String s1, String s2) {
	Thread me = Thread.currentThread();

	if (inCanvasCallback) {
	    synchronized (canvasList) {
		for (int i=canvases.size()-1; i>=0; i--) {
				if (canvases.elementAt(i).screen.renderer == me) {
			throw new IllegalStateException(J3dI18N.getString(s1));
		    }
		}
	    }
	}

	if ((viewPlatform != null) && viewPlatform.isLive()) {
	    if (universe.inBehavior && (universe.behaviorScheduler == me)) {
		throw new IllegalStateException(J3dI18N.getString(s2));
	    }
	    return true;
	}
	return false;
    }

    /**
     * Retrieves a flag that indicates whether the behavior scheduler is
     * currently running.
     * @return true if the behavior scheduler is running, false otherwise
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final boolean isBehaviorSchedulerRunning() {
	return  (((universe != null) && !stopBehavior &&
		  (universe.behaviorScheduler != null)) ?
		 !universe.behaviorScheduler.userStop : false);
    }

    /**
     * Stops traversing the scene graph for this
     * view after the current state of the scene graph is reflected on
     * all canvases attached to this view.  The renderers associated
     * with these canvases are also stopped.
     * <p>
     * NOTE: This is a heavy-weight method
     * intended for verification and image capture (recording); it
     * is <i>not</i> intended to be used for flow control.
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final void stopView() {
	checkViewState("View19", "View20");
	synchronized (startStopViewLock) {
	    if (activeStatus && isRunning) {
		VirtualUniverse.mc.postRequest(MasterControl.STOP_VIEW, this);
		while (isRunning) {
		    MasterControl.threadYield();
		}
	    } else {
		isRunning = false;
	    }
	}
    }

    /**
     * Starts
     * traversing this view, and starts the renderers associated
     * with all canvases attached to this view.
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final void startView() {

	checkViewState("View21", "View22");
	synchronized (startStopViewLock) {
	    if (activeStatus && !isRunning) {
		VirtualUniverse.mc.postRequest(MasterControl.START_VIEW, this);
		while (!isRunning) {
		    MasterControl.threadYield();
		}
		VirtualUniverse.mc.sendRunMessage(this,
						  J3dThread.RENDER_THREAD);
	    } else {
		isRunning = true;
	    }
	}

    }

    /**
     *  This will throw IllegalStateException if not in valid state
     *  for start/stop request.
     */
    void checkViewState(String s1, String s2)  throws IllegalStateException {
	if (inCanvasCallback) {
	    Thread me = Thread.currentThread();
	    synchronized (canvasList) {
		for (int i= canvases.size()-1; i>=0; i--) {
				Canvas3D cv = canvases.elementAt(i);
		    if (cv.screen.renderer == me) {
			throw new
			    IllegalStateException(J3dI18N.getString(s1));
		    }
		}
	    }
	}

	if ((viewPlatform != null) &&  viewPlatform.isLive()) {
	    if (universe.inBehavior &&
		(Thread.currentThread() ==  universe.behaviorScheduler)) {
	            throw new IllegalStateException(J3dI18N.getString(s2));
	    }
	}
    }

    /**
     * Retrieves a flag that indicates whether the traverser is
     * currently running on this view.
     * @return true if the traverser is running, false otherwise
     * @exception IllegalStateException if this method is called
     * from a Behavior method or from any Canvas3D render callback
     * method
     */
    public final boolean isViewRunning() {
	return isRunning;
    }

    /**
     * Renders one frame for a stopped View.  Functionally, this
     * method is equivalent to <code>startView()</code> followed by
     * <code>stopview()</code>, except that it is atomic, which
     * guarantees that only one frame is rendered.
     *
     * @exception IllegalStateException if this method is called from
     * a Behavior method or from any Canvas3D render callback, or if
     * the view is currently running.
     *
     * @since Java 3D 1.2
     */
    public void renderOnce() {
	checkViewState("View28", "View29");
	synchronized (startStopViewLock) {
	    if (isRunning) {
		throw new IllegalStateException(J3dI18N.getString("View30"));
	    }
	    renderOnceFinish = false;
	    VirtualUniverse.mc.postRequest(MasterControl.RENDER_ONCE, this);
	    while (!renderOnceFinish) {
		MasterControl.threadYield();
	    }
	    renderOnceFinish = true;
	}
    }

    /**
     * Requests that this View be scheduled for rendering as soon as
     * possible.  The repaint method may return before the frame has
     * been rendered.  If the view is stopped, or if the view is
     * continuously running (for example, due to a free-running
     * interpolator), this method will have no effect.  Most
     * applications will not need to call this method, since any
     * update to the scene graph or to viewing parameters will
     * automatically cause all affected views to be rendered.
     *
     * @since Java 3D 1.2
     */
    public void repaint() {
	if (activeStatus && isRunning) {
	    VirtualUniverse.mc.sendRunMessage(this,
					      J3dThread.RENDER_THREAD);
	}
    }


    /**
     * Update the view cache associated with this view.  Also, shapshot
     * the per-screen parameters associated with all screens attached
     * to this view.
     */
    final void updateViewCache() {

	synchronized(this) {
	    viewCache.snapshot();
	    viewCache.computeDerivedData();
	}

	// Just take the brute force approach and snapshot the
	// parameters for each screen attached to each canvas.  We won't
	// worry about whether a screen is cached more than once.
	// Eventually, dirty bits will take care of this.

	synchronized (canvasList) {
	    int i = canvases.size()-1;
	    while (i>=0) {
			Screen3D scr = canvases.elementAt(i--).getScreen3D();
		if (scr != null)
		    scr.updateViewCache();
	    }
	}
    }


    /**
     * This routine activates or deactivates a view based on various information
     */
    void evaluateActive() {

	synchronized (evaluateLock) {
	    if (universe == null) {
		return;
	    }

	    if ((viewPlatform == null) ||
		!viewPlatform.isLive() ||
		!((ViewPlatformRetained)viewPlatform.retained).switchState.currentSwitchOn) {
		if (activeStatus) {
		    deactivate();
		    activeStatus = false;
		}
		// Destroy threads from MC
		if (VirtualUniverse.mc.isRegistered(this) &&
		    (universe.isEmpty() ||
		     (canvases.isEmpty() &&
		      ((viewPlatform == null) ||
		       !viewPlatform.isLive())))) {
		    // We can't wait until MC finish unregister view
		    // here because user thread may
		    // holds the universe.sceneGraphLock if branch
		    // or locale remove in clearLive(). In this way
		    // There is deadlock since MC also need need
		    // sceneGraphLock in some threads
		    // (e.g. TransformStructure update thread)
		    universe.unRegViewWaiting = this;
		    resetUnivCount = universeCount;
		    VirtualUniverse.mc.postRequest(
					   MasterControl.UNREGISTER_VIEW, this);
		}
	    } else {

		// We're on a live view platform.  See what the canvases say
		// If view not register, MC will register it automatically

		int i;
		VirtualUniverse u = null;
		synchronized (canvasList) {

		    for (i=canvases.size()-1; i>=0; i--) {
					Canvas3D cv = canvases.elementAt(i);
			if (cv.active) {

			    if (!activeStatus && (universeCount > resetUnivCount)) {
				u = universe;
			    }
			    break;
			}
		    }
		}

		// We should do this outside canvasList lock,
		// otherwise it may cause deadlock with MC
		if (u != null) {
		    activate(u);
		    activeStatus = true;
		    return;
		}


		if ((i < 0) && activeStatus) {
		    deactivate();
		    activeStatus = false;
		    return;
		}

		if (VirtualUniverse.mc.isRegistered(this)) {
		    // notify MC that canvases state for this view changed
		    VirtualUniverse.mc.postRequest(
				   MasterControl.REEVALUATE_CANVAS, this);
		}
	    }
	}
    }

    void setUniverse(VirtualUniverse universe) {

	synchronized (VirtualUniverse.mc.requestObjList) {
	    if ((renderBin == null) ||
		(renderBin.universe != universe)) {
		if (renderBin != null) {
		    renderBin.cleanup();
		}
		renderBin = new RenderBin(universe, this);
		renderBin.universe = universe;
	    }


	    if ((soundScheduler == null) ||
		(soundScheduler.universe !=  universe)) {
		// create a sound scheduler for this view, with this universe
		if (soundScheduler != null) {
		    soundScheduler.cleanup();
		}
		soundScheduler = new SoundScheduler(universe, this);
	    }


	    // This has to be the last call before
	    // RenderBin and SoundScheduler construct. Since it is
	    // possible that canvas receive paint call and invoked
	    // evaluateActive in another thread - which check for
	    // universe == null and may let it pass before soundScheduler
	    // and renderBin initialize.
	    universeCount++;
	    this.universe = universe;
	}
	evaluateActive();
    }

    /**
     * This activates all traversers and renderers associated with this view.
     */
    void activate(VirtualUniverse universe) {

	universe.checkForEnableEvents();

	if (physicalBody != null) {
	    physicalBody.addUser(this);
	}

	if (!VirtualUniverse.mc.isRegistered(this)) {
	    universe.regViewWaiting = this;
	}

	VirtualUniverse.mc.postRequest(MasterControl.ACTIVATE_VIEW,
				       this);

	if (!universe.isSceneGraphLock) {
	    universe.waitForMC();
	}
        if (soundScheduler != null) {
            soundScheduler.reset();
        }

	J3dMessage vpMessage = new J3dMessage();
	vpMessage.universe = universe;
	vpMessage.view = this;
	vpMessage.type = J3dMessage.UPDATE_VIEW;
	vpMessage.threads =
			    J3dThread.SOUND_SCHEDULER |
			    J3dThread.UPDATE_RENDER |
			    J3dThread.UPDATE_BEHAVIOR;
	vpMessage.args[0] = this;
	synchronized(((ViewPlatformRetained)viewPlatform.retained).sphere) {
	    vpMessage.args[1] = new Float(((ViewPlatformRetained)viewPlatform.retained).sphere.radius);
	}
	vpMessage.args[2] = new Integer(OTHER_ATTRS_CHANGED);
	vpMessage.args[3] = new Integer(transparencySortingPolicy);
	VirtualUniverse.mc.processMessage(vpMessage);
    }

    /**
     * This deactivates all traversers and renderers associated with this view.
     */
    void deactivate() {
	VirtualUniverse.mc.postRequest(MasterControl.DEACTIVATE_VIEW, this);
	if (physicalBody != null) {
	    physicalBody.removeUser(this);
	}

	// This is a temporary fix for bug 4267395
	// XXXX:cleanup in RenderBin after View detach
	// universe.addViewIdToFreeList(viewId);

	// using new property -Dj3d.forceReleaseView to disable bug fix 4267395
	// this bug fix can produce memory leaks in *some* applications which creates
	// and destroy Canvas3D from time to time. This just add the view in the
	// FreeList earlier.
	if (VirtualUniverse.mc.forceReleaseView) {
	    universe.addViewIdToFreeList(viewId);
	}


	J3dMessage vpMessage = new J3dMessage();
	vpMessage.universe = universe;
	vpMessage.view = this;
	vpMessage.type = J3dMessage.UPDATE_VIEW;
	vpMessage.threads =
			    J3dThread.SOUND_SCHEDULER |
			    J3dThread.UPDATE_RENDER |
			    J3dThread.UPDATE_BEHAVIOR;
	vpMessage.args[0] = this;
	if (viewPlatform != null) {
	    synchronized(((ViewPlatformRetained)viewPlatform.retained).sphere) {
		vpMessage.args[1] = new Float(((ViewPlatformRetained)viewPlatform.retained).sphere.radius);
	    }
	} else {
	    vpMessage.args[1] = new Float(0);
	}
	vpMessage.args[2] = new Integer(OTHER_ATTRS_CHANGED);
	vpMessage.args[3] = new Integer(transparencySortingPolicy);
	VirtualUniverse.mc.processMessage(vpMessage);

    }

    void cleanupViewId() {
	universe.addViewIdToFreeList(viewId);
	viewId = null;
    }


    void assignViewId () {
	if (viewId == null) {
	    viewId = universe.getViewId();
	    viewIndex = viewId.intValue();
	}
    }

    /**
     * This method passes window event to SoundScheduler
     */
    void sendEventToSoundScheduler(AWTEvent evt) {
        if (soundScheduler != null) {
            soundScheduler.receiveAWTEvent(evt);
        }
    }

    void reset() {

	for (int i=0; i < canvases.size(); i++) {
		canvases.get(i).reset();
	}

        // reset the renderBinReady flag
        renderBinReady = false;

	soundScheduler.cleanup();
	soundScheduler = null;

	viewCache = new ViewCache(this);
	getCanvasList(true);
	cleanupViewId();
	renderBin.cleanup();
	renderBin = null;
	universe = null;
    }
}
