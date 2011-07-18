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

package com.sun.j3d.utils.universe ;

import java.awt.GraphicsConfiguration ;
import java.awt.GraphicsEnvironment;
import java.awt.Point ;
import java.awt.Rectangle ;
import java.text.DecimalFormat ;
import java.text.FieldPosition ;
import java.util.* ;
import javax.media.j3d.* ;
import javax.vecmath.* ;

/**
 * Provides methods to extract synchronized transform information from a View.
 * These transforms are derived from application scene graph information, as
 * opposed to similar core Java 3D methods that derive transforms from
 * internally maintained data.	This allows updates to the scene graph to be
 * synchronized with the current view platform position.<p>
 *
 * The architecture of the Java 3D 1.3 sample implementation introduces a
 * frame latency between updates to the application scene graph structure and
 * their effects on internal Java 3D state. <code>getImagePlateToVworld</code>
 * and other methods in the core Java 3D classes use a transform from view
 * platform coordinates to virtual world coordinates that can be out of date
 * with respect to the state of the view platform as set by the application.
 * When an application uses the transforms returned by those methods to update
 * view dependent parts of the scene graph, those updates might not be
 * synchronized with what the viewer actually sees.<p>
 *
 * The methods in this class work around this problem at the expense of
 * querying the application state of the scene graph to get the current
 * transform from view platform to virtual world coordinates.  This can
 * involve a potential performance degradation, however, since the application
 * scene graph state is not designed for high performance queries.  The view
 * platform must also have <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability
 * set, which potentially inhibits internal scene graph optimization.<p>
 *
 * On the other hand, application behaviors that create the view platform
 * transformation directly will have access to it without the need to query it
 * from the scene graph; in that case, the transforms from physical
 * coordinates to view platform coordinates provided by this class are all
 * that are needed.  The <code>ALLOW_LOCAL_TO_VWORLD_READ</code> view platform
 * capability doesn't need to be set for these applications.<p>
 *
 * <b>Other Synchronization Issues</b><p>
 * 
 * Scene graph updates are guaranteed to take effect in the same frame only
 * if run from the processStimulus() method of a Behavior. Updates from
 * multiple behaviors are only guaranteed to take effect in the same frame if
 * they're responding to a WakeupOnElapsedFrames(0) condition.  Use a single
 * behavior to perform view dependent updates if possible; otherwise, use
 * WakeupOnElapsedFrames(0) and set behavior scheduling intervals to ensure
 * that behaviors that need the current view platform transform are run after
 * it's set.  Updating scene graph elements from anything other than the
 * Behavior thread, such as an external input thread or a renderer callback
 * in Canvas3D, will not necessarily be synchronized with rendering.<p>
 * 
 * Direct updates to geometry data have a different frame latency than
 * updates to scene graph transforms and structure. In the Java 3D 1.3
 * architecture, updates to by-reference geometry arrays and texture data have
 * a 1-frame latency, while updates to transforms and scene graph structure
 * have a 2-frame latency.  Because of bug 4799494, which is outstanding
 * in Java 3D 1.3.1, updates to by-copy geometry arrays also have a 1-frame
 * latency.  It is therefore recommended that view dependent scene graph
 * updates be limited to transforms and scene graph structure only.<p>
 *
 * If it is not possible to avoid updating geometry directly, then these
 * updates must be delayed by one frame in order to remain synchronized with
 * the view platform.  This can be accomplished by creating an additional
 * behavior to actually update the geometry, separate from the behavior that
 * computes the changes that need to be made based on current view state.  If
 * the update behavior is awakened by a behavior post from the computing
 * behavior then the update will be delayed by a single frame.<p>
 * 
 * <b>Implementation Notes</b><p>
 * 
 * This utility is essentially a rewrite of a few private Java 3D core
 * classes, but designed for public use and source code availability.  The
 * source code may be helpful in understanding some of the more complex
 * aspects of the view model, especially with regards to various interactions
 * between attributes which are not adequately documented.  None of the actual
 * core Java 3D source code is used, but the code is designed to comply with
 * the view model as defined by the Java 3D Specification, so it can be
 * considered an alternative implementation.  This class will produce the
 * same results as the Java 3D core implementation except for:<p><ul>
 * 
 * <li>The frame latency issue for virtual world transforms.</li><p>
 * 
 * <li>Active clip node status.	 If a clip node is active in the scene graph,
 *     it should override the view's back clip plane.  This class has no such
 *     information, so this can't be implemented.</li><p>
 * 
 * <li>"Infinite" view transforms for background geometry.  These are simply
 *     the rotation components of the normal view transforms with adjusted
 *     clip planes. Again, this function depends upon scene graph content
 *     inaccessible to this class.</li><p>
 * 
 * <li>Small floating point precision differences resulting from the
 *     alternative computations.</li><p>
 * 
 * <li>Bugs in this class and the Java 3D core.</li><p>
 * 
 * <li>Tracked head position.</li></ul><p>
 * 
 * The last item deserves some mention.	 Java 3D provides no way to directly
 * query the tracked head position being used by the renderer.	The View's
 * <code>getUserHeadToVworld</code> method always incorporates a virtual world
 * transform that is out of date with respect to the application scene graph
 * state.  ViewInfo reads data from the head tracking sensor directly, but
 * since head trackers are continuous input devices, getting the same data
 * that the renderer is using is unlikely.  See the source code for the
 * private method <code>getHeadInfo</code> in this class for more information
 * and possible workarounds.<p>
 *
 * <b>Thread Safety</b><p>
 * 
 * All transforms are lazily evaluated.  The <code>updateScreen</code>,
 * <code>updateCanvas</code>, <code>updateViewPlatform</code>,
 * <code>updateView</code>, and <code>updateHead</code> methods just set flags
 * indicating that derived transforms need to be recomputed; they are safe to
 * call from any thread.  <code>updateCanvas</code>, for example, can safely
 * be called from an AWT event listener.<p>
 *
 * Screens and view platforms can be shared between separate views in the Java
 * 3D view model.  To remain accurate, ViewInfo also allows this sharing.
 * Since it is likely that a multi-view application has separate threads
 * managing each view, potential concurrent modification of data associated
 * with a screen or a view platform is internally synchronized in this class.
 * It is safe for each thread to use its own instance of a ViewInfo
 * corresponding to the view it is managing.<p>
 *
 * Otherwise, none of the other methods in this class are internally
 * synchronized.  <i>Except for the update methods mentioned above, a single
 * instance of ViewInfo should not be used by more than one concurrent thread
 * without external synchronization.</i><p>
 *
 * @since Java 3D 1.3.1
 */
public class ViewInfo {
    private final static boolean verbose = false ;

    /**
     * Indicates that updates to a Screen3D associated with the View should
     * be automatically checked with each call to a public method in this
     * class.
     */
    public final static int SCREEN_AUTO_UPDATE = 1 ;

    /**
     * Indicates that updates to a Canvas3D associated with the View should
     * be automatically checked with each call to a public method in this
     * class.
     */
    public final static int CANVAS_AUTO_UPDATE = 2 ;

    /**
     * Indicates that updates to the View should be automatically checked
     * with each call to a public method in this class.
     */
    public final static int VIEW_AUTO_UPDATE = 4 ;

    /**
     * Indicates that updates to the tracked head position should be
     * automatically checked with each call to a public method in this class.
     */
    public final static int HEAD_AUTO_UPDATE = 8 ;

    /**
     * Indicates that updates to the ViewPlatform <code>localToVworld</code>
     * transform should be automatically checked with each call to a public
     * method in this class.  The View must be attached to a ViewPlatform
     * which is part of a live scene graph, and the ViewPlatform node must
     * have its <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.
     */
    public final static int PLATFORM_AUTO_UPDATE = 16 ;

    //
    // Screen3D and ViewPlatform instances are shared across multiple Views in
    // the Java 3D view model.	Since ViewInfo is per-View and we want to
    // cache screen and platform derived data, we maintain static references
    // to the screens and platforms here.
    // 
    // From a design standpoint our ViewInfo objects should probably be in the
    // scope of an object that encloses these maps so they can be gc'ed
    // properly.  This is cumbersome with the current design constraints, so
    // for now we provide an alternative constructor to override these static
    // maps and a method to explicitly clear them.  The alternative
    // constructor can be used to wrap this class into a multi-view context
    // that provides the maps.
    //
    private static Map staticVpMap = new HashMap() ;
    private static Map staticSiMap = new HashMap() ;

    private Map screenMap = null ;
    private Map viewPlatformMap = null ;

    // The target View and some derived data.
    private View view = null ;
    private Sensor headTracker = null ;
    private boolean useTracking = false ;
    private boolean clipVirtual = false ;

    // The current ViewPlatform and Canvas3D information used by this object.
    private ViewPlatformInfo vpi = null ;
    private int canvasCount = 0 ;
    private Map canvasMap = new HashMap() ;
    private CanvasInfo[] canvasInfo = new CanvasInfo[1] ;

    // This View's update flags.  The other update flags are maintained by
    // ScreenInfo, CanvasInfo, and ViewPlatformInfo.
    private boolean updateView = true ;
    private boolean updateHead = true ;
    private boolean autoUpdate = false ;
    private int autoUpdateFlags = 0 ;

    // Cached View policies.  
    private int viewPolicy = View.SCREEN_VIEW ;
    private int resizePolicy = View.PHYSICAL_WORLD ;
    private int movementPolicy = View.PHYSICAL_WORLD ;
    private int eyePolicy = View.RELATIVE_TO_FIELD_OF_VIEW ;
    private int projectionPolicy = View.PERSPECTIVE_PROJECTION ;
    private int frontClipPolicy = View.PHYSICAL_EYE ;
    private int backClipPolicy = View.PHYSICAL_EYE ;
    private int scalePolicy = View.SCALE_SCREEN_SIZE ;
    private boolean coeCentering = true ;

    // This View's cached transforms.  See ScreenInfo, CanvasInfo, and
    // ViewPlatformInfo for the rest of the cached transforms.
    private Transform3D coeToTrackerBase = null ;
    private Transform3D headToHeadTracker = null ;

    // These are from the head tracker read.
    private Transform3D headTrackerToTrackerBase = null ;
    private Transform3D trackerBaseToHeadTracker = null ;

    // These are derived from the head tracker read.
    private Transform3D headToTrackerBase = null ;
    private Transform3D coeToHeadTracker = null ;

    // Cached physical body and environment.
    private PhysicalEnvironment env = null ;
    private PhysicalBody body = null ;
    private Point3d leftEyeInHead = new Point3d() ;
    private Point3d rightEyeInHead = new Point3d() ;

    // Temporary variables.  These could just be new'ed as needed, but we'll
    // assume that ViewInfo instances are used much more than they're created.
    private Vector3d v3d = new Vector3d() ;
    private double[] m16d = new double[16] ;
    private Point3d leftEye = new Point3d() ;
    private Point3d rightEye = new Point3d() ;
    private Map newMap = new HashMap() ;
    private Set newSet = new HashSet() ;

    /**
     * Creates a new ViewInfo for the specified View.<p>
     *
     * Applications are responsible for informing this class of changes to the
     * View, its Canvas3D and Screen3D components, the tracked head position,
     * and the ViewPlatform's <code>localToVworld</code> transform.  These
     * notifications are performed with the <code>updateView</code>,
     * <code>updateCanvas</code>, <code>updateScreen</code>,
     * <code>updateHead</code>, and <code>updateViewPlatform</code>
     * methods.<p>
     *
     * The View must be attached to a ViewPlatform.  If the ViewPlatform is
     * attached to a live scene graph, then <code>ALLOW_POLICY_READ</code>
     * capability must be set on the ViewPlatform node.
     * 
     * @param view the View to use
     * @see #updateView
     * @see #updateCanvas updateCanvas(Canvas3D)
     * @see #updateScreen updateScreen(Screen3D)
     * @see #updateHead
     * @see #updateViewPlatform
     */
    public ViewInfo(View view) {
	this(view, 0) ;
    }

    /**
     * Creates a new ViewInfo for the specified View.  The View must be
     * attached to a ViewPlatform.  If the ViewPlatform is attached to a live
     * scene graph, then <code>ALLOW_POLICY_READ</code> capability must be set
     * on the ViewPlatform node.
     * 
     * @param view the View to use<p>
     * @param autoUpdateFlags a logical <code>OR</code> of any of the
     *	<code>VIEW_AUTO_UPDATE</code>, <code>CANVAS_AUTO_UPDATE</code>,
     *	<code>SCREEN_AUTO_UPDATE</code>, <code>HEAD_AUTO_UPDATE</code>, or
     *	<code>PLATFORM_AUTO_UPDATE</code> flags to control whether changes to
     *	the View, its Canvas3D or Screen3D components, the tracked head
     *	position, or the ViewPlatform's <code>localToVworld</code> transform
     *	are checked automatically with each call to a public method of this
     *	class; if a flag is not set, then the application must inform this
     *	class of updates to the corresponding data
     */
    public ViewInfo(View view, int autoUpdateFlags) {
	this(view, autoUpdateFlags, staticSiMap, staticVpMap) ;
    }

    /**
     * Creates a new ViewInfo for the specified View.  The View must be
     * attached to a ViewPlatform.  If the ViewPlatform is attached to a live
     * scene graph, then <code>ALLOW_POLICY_READ</code> capability must be set
     * on the ViewPlatform node.<p>
     *
     * ViewInfo caches Screen3D and ViewPlatform data, but Screen3D and
     * ViewPlatform instances are shared across multiple Views in the Java 3D
     * view model.  Since ViewInfo is per-View, all ViewInfo constructors
     * except for this one use static references to manage the shared Screen3D
     * and ViewPlatform objects.  In this constructor, however, the caller
     * supplies two Map instances to hold these references for all ViewInfo
     * instances, so static references can be avoided; it can be used to wrap
     * this class into a multi-view context that provides the required
     * maps.<p>
     *
     * Alternatively, the other constructors can be used by calling
     * <code>ViewInfo.clear</code> when done with ViewInfo, or by simply
     * retaining the static references until the JVM exits.<p>
     * 
     * @param view the View to use<p>
     * @param autoUpdateFlags a logical <code>OR</code> of any of the
     *	<code>VIEW_AUTO_UPDATE</code>, <code>CANVAS_AUTO_UPDATE</code>,
     *	<code>SCREEN_AUTO_UPDATE</code>, <code>HEAD_AUTO_UPDATE</code>, or
     *	<code>PLATFORM_AUTO_UPDATE</code> flags to control whether changes to
     *	the View, its Canvas3D or Screen3D components, the tracked head
     *	position, or the ViewPlatform's <code>localToVworld</code> transform
     *	are checked automatically with each call to a public method of this
     *	class; if a flag is not set, then the application must inform this
     *	class of updates to the corresponding data<p>
     * @param screenMap a writeable Map to hold Screen3D information
     * @param viewPlatformMap a writeable Map to hold ViewPlatform information
     */
    public ViewInfo(View view, int autoUpdateFlags,
		    Map screenMap, Map viewPlatformMap) {

	if (verbose)
	    System.err.println("ViewInfo: init " + hashCode()) ;
	if (view == null)
	    throw new IllegalArgumentException("View is null") ;
	if (screenMap == null)
	    throw new IllegalArgumentException("screenMap is null") ;
	if (viewPlatformMap == null)
	    throw new IllegalArgumentException("viewPlatformMap is null") ;

	this.view = view ;
	this.screenMap = screenMap ;
	this.viewPlatformMap = viewPlatformMap ;

	if (autoUpdateFlags == 0) {
	    this.autoUpdate = false ;
	}
	else {
	    this.autoUpdate = true ;
	    this.autoUpdateFlags = autoUpdateFlags ;
	}

	getViewInfo() ;
    }

    /**
     * Gets the current transforms from image plate coordinates to view
     * platform coordinates and copies them into the given Transform3Ds.<p>
     * 
     * With a monoscopic canvas the image plate transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left image plate transform, and
     * if the second argument is non-null it receives the right image plate
     * transform.  These transforms are always the same unless a head mounted
     * display driven by a single stereo canvas is in use.
     *
     * @param c3d the Canvas3D associated with the image plate
     * @param ip2vpl the Transform3D to receive the left transform
     * @param ip2vpr the Transform3D to receive the right transform, or null
     */
    public void getImagePlateToViewPlatform(Canvas3D c3d,
					    Transform3D ip2vpl,
					    Transform3D ip2vpr) {

	CanvasInfo ci = updateCache
	    (c3d, "getImagePlateToViewPlatform", false) ;

	getImagePlateToViewPlatform(ci) ;
	ip2vpl.set(ci.plateToViewPlatform) ;
	if (ci.useStereo && ip2vpr != null)
	    ip2vpr.set(ci.rightPlateToViewPlatform) ;
    }

    private void getImagePlateToViewPlatform(CanvasInfo ci) {
	if (ci.updatePlateToViewPlatform) {
	    if (verbose) System.err.println("updating PlateToViewPlatform") ;
	    if (ci.plateToViewPlatform == null)
		ci.plateToViewPlatform = new Transform3D() ;

	    getCoexistenceToImagePlate(ci) ;
	    getViewPlatformToCoexistence(ci) ;

	    ci.plateToViewPlatform.mul(ci.coeToPlate, ci.viewPlatformToCoe) ;
	    ci.plateToViewPlatform.invert() ;

	    if (ci.useStereo) {
		if (ci.rightPlateToViewPlatform == null)
		    ci.rightPlateToViewPlatform = new Transform3D() ;

		ci.rightPlateToViewPlatform.mul(ci.coeToRightPlate,
						ci.viewPlatformToCoe) ;
		ci.rightPlateToViewPlatform.invert() ;
	    }
	    ci.updatePlateToViewPlatform = false ;
	    if (verbose) t3dPrint(ci.plateToViewPlatform, "plateToVp") ;
	}
    }

    /**
     * Gets the current transforms from image plate coordinates to virtual
     * world coordinates and copies them into the given Transform3Ds.<p>
     * 
     * With a monoscopic canvas the image plate transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left image plate transform, and
     * if the second argument is non-null it receives the right image plate
     * transform.  These transforms are always the same unless a head mounted
     * display driven by a single stereo canvas is in use.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.
     *
     * @param c3d the Canvas3D associated with the image plate
     * @param ip2vwl the Transform3D to receive the left transform
     * @param ip2vwr the Transform3D to receive the right transform, or null
     */
    public void getImagePlateToVworld(Canvas3D c3d,
				      Transform3D ip2vwl, Transform3D ip2vwr) {

	CanvasInfo ci = updateCache(c3d, "getImagePlateToVworld", true) ;
	getImagePlateToVworld(ci) ;
	ip2vwl.set(ci.plateToVworld) ;
	if (ci.useStereo && ip2vwr != null)
	    ip2vwr.set(ci.rightPlateToVworld) ;
    }

    private void getImagePlateToVworld(CanvasInfo ci) {
	if (ci.updatePlateToVworld) {
	    if (verbose) System.err.println("updating PlateToVworld") ;
	    if (ci.plateToVworld == null)
		ci.plateToVworld = new Transform3D() ;

	    getImagePlateToViewPlatform(ci) ;
	    ci.plateToVworld.mul
		(vpi.viewPlatformToVworld, ci.plateToViewPlatform) ;

	    if (ci.useStereo) {
		if (ci.rightPlateToVworld == null)
		    ci.rightPlateToVworld = new Transform3D() ;

		ci.rightPlateToVworld.mul
		    (vpi.viewPlatformToVworld, ci.rightPlateToViewPlatform) ;
	    }
	    ci.updatePlateToVworld = false ;
	}
    }

    /**
     * Gets the current transforms from coexistence coordinates to image plate
     * coordinates and copies them into the given Transform3Ds.  The default
     * coexistence centering enable and window movement policies are
     * <code>true</code> and <code>PHYSICAL_WORLD</code> respectively, which
     * will center coexistence coordinates to the middle of the canvas,
     * aligned with the screen (image plate). A movement policy of
     * <code>VIRTUAL_WORLD</code> centers coexistence coordinates to the
     * middle of the screen.<p>
     *
     * If coexistence centering is turned off, then canvases and screens can
     * have arbitrary positions with respect to coexistence, set through the
     * the Screen3D <code>trackerBaseToImagePlate</code> transform and the
     * PhysicalEnvironment <code>coexistenceToTrackerBase</code> transform.
     * These are calibration constants used for multiple fixed screen displays.
     * For head mounted displays the transform is determined by the user head
     * position along with calibration parameters found in Screen3D and
     * PhysicalBody. (See the source code for the private method
     * <code>getEyesHMD</code> for more information).<p>
     *
     * With a monoscopic canvas the image plate transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left image plate transform, and
     * if the second argument is non-null it receives the right image plate
     * transform.  These transforms are always the same unless a head mounted
     * display driven by a single stereo canvas is in use.<p>
     *
     * @param c3d the Canvas3D associated with the image plate
     * @param coe2ipl the Transform3D to receive the left transform
     * @param coe2ipr the Transform3D to receive the right transform, or null
     */
    public void getCoexistenceToImagePlate(Canvas3D c3d,
					   Transform3D coe2ipl,
					   Transform3D coe2ipr) {

	CanvasInfo ci = updateCache(c3d, "getCoexistenceToImagePlate", false) ;
	getCoexistenceToImagePlate(ci) ;
	coe2ipl.set(ci.coeToPlate) ;
	if (ci.useStereo && coe2ipr != null)
	    coe2ipr.set(ci.coeToRightPlate) ;
    }

    private void getCoexistenceToImagePlate(CanvasInfo ci) {
	//
	// This method will always set coeToRightPlate even if stereo is not
	// in use.  This is necessary so that getEyeToImagePlate() can handle
	// a monoscopic view policy of CYCLOPEAN_EYE_VIEW (which averages the
	// left and right eye positions) when the eyepoints are expressed in
	// coexistence coordinates or are derived from the tracked head.
	// 
	if (ci.updateCoeToPlate) {
	    if (verbose) System.err.println("updating CoeToPlate") ;
	    if (ci.coeToPlate == null) {
		ci.coeToPlate = new Transform3D() ;
		ci.coeToRightPlate = new Transform3D() ;
	    }
	    if (viewPolicy == View.HMD_VIEW) {
		// Head mounted displays have their image plates fixed with
		// respect to the head, so get the head position in
		// coexistence.
		ci.coeToPlate.mul(ci.si.headTrackerToLeftPlate,
				  coeToHeadTracker) ;
		if (ci.useStereo)
		    // This is the only case in the view model in which the
		    // right plate transform could be different from the left.
		    ci.coeToRightPlate.mul(ci.si.headTrackerToRightPlate,
					   coeToHeadTracker) ;
		else
		    ci.coeToRightPlate.set(ci.coeToPlate) ;
	    }
	    else if (coeCentering) {
		// The default, for fixed single screen displays with no
		// motion tracking.  The transform is just a translation.
		if (movementPolicy == View.PHYSICAL_WORLD)
		    // The default.  Coexistence is centered in the window.
		    v3d.set(ci.canvasX + (ci.canvasWidth  / 2.0),
			    ci.canvasY + (ci.canvasHeight / 2.0), 0.0) ;
		else
		    // Coexistence is centered in the screen.
		    v3d.set(ci.si.screenWidth  / 2.0,
			    ci.si.screenHeight / 2.0, 0.0) ;

		ci.coeToPlate.set(v3d) ;
		ci.coeToRightPlate.set(v3d) ;
	    }
	    else {
		// Coexistence centering should be false for multiple fixed
		// screens and/or motion tracking.  trackerBaseToImagePlate
		// and coexistenceToTrackerBase are used explicitly.
		ci.coeToPlate.mul(ci.si.trackerBaseToPlate, coeToTrackerBase) ;
		ci.coeToRightPlate.set(ci.coeToPlate) ;
	    }
	    ci.updateCoeToPlate = false ;
	    if (verbose) t3dPrint(ci.coeToPlate, "coeToPlate") ;
	}
    }

    /**
     * Gets the current transform from view platform coordinates to
     * coexistence coordinates and copies it into the given transform.	View
     * platform coordinates are always aligned with coexistence coordinates
     * but may differ in scale and in Y and Z offset.  The scale is derived
     * from the window resize and screen scale policies, while the offset is
     * derived from the view attach policy.<p>
     *
     * Java 3D constructs a view from the physical position of the eyes
     * relative to the physical positions of the image plates; it then uses a
     * view platform to position that physical configuration into the virtual
     * world and from there computes the correct projections of the virtual
     * world onto the physical image plates.  Coexistence coordinates are used
     * to place the physical positions of the view platform, eyes, head, image
     * plate, sensors, and tracker base in relation to each other. The view
     * platform is positioned with respect to the virtual world through the
     * scene graph, so the view platform to coexistence transform defines the
     * space in which the virtual world and physical world coexist.<p>
     * 
     * This method requires a Canvas3D.	 A different transform may be returned
     * for each canvas in the view if any of the following apply:<p><ul>
     *
     * <li>The window resize policy is <code>PHYSICAL_WORLD</code>, which
     *	   alters the scale depending upon the width of the canvas.</li><p>
     *
     * <li>The screen scale policy is <code>SCALE_SCREEN_SIZE</code>,
     *	   which alters the scale depending upon the width of the screen
     *	   associated with the canvas.</li><p>
     *
     * <li>A window eyepoint policy of <code>RELATIVE_TO_FIELD_OF_VIEW</code>
     *	   with a view attach policy of <code>NOMINAL_HEAD</code> in effect,
     *	   which sets the view platform Z offset in coexistence coordinates
     *	   based on the width of the canvas.  These are the default policies.
     *	   The offset also follows the width of the canvas when the
     *	   <code>NOMINAL_FEET</code> view attach policy is used.</li></ul>
     *
     * @param c3d the Canvas3D to use
     * @param vp2coe the Transform3D to receive the transform
     */
    public void getViewPlatformToCoexistence(Canvas3D c3d,
					     Transform3D vp2coe) {

	CanvasInfo ci = updateCache
	    (c3d, "getViewPlatformToCoexistence", false) ;

	getViewPlatformToCoexistence(ci) ;
	vp2coe.set(ci.viewPlatformToCoe) ;
    }

    private void getViewPlatformToCoexistence(CanvasInfo ci) {
	if (!ci.updateViewPlatformToCoe) return ;
	if (verbose) System.err.println("updating ViewPlatformToCoe") ;
	if (ci.viewPlatformToCoe == null)
	    ci.viewPlatformToCoe = new Transform3D() ;
	//
	// The scale from view platform coordinates to coexistence coordinates
	// has two components -- the screen scale and the window scale.	 The
	// window scale only applies if the resize policy is PHYSICAL_WORLD.
	//
	// This scale is not the same as the vworld to view platform scale.
	// The latter is contained in the view platform's localToVworld
	// transform as defined by the scene graph.  The complete scale factor
	// from virtual units to physical units is the product of the vworld
	// to view platform scale and the view platform to coexistence scale.
	//
	getScreenScale(ci) ;
	if (resizePolicy == View.PHYSICAL_WORLD)
	    ci.viewPlatformToCoe.setScale(ci.screenScale * ci.windowScale) ;
	else
	    ci.viewPlatformToCoe.setScale(ci.screenScale) ;

	if (viewPolicy == View.HMD_VIEW) {
	    // In HMD mode view platform coordinates are the same as
	    // coexistence coordinates, except for scale.
	    ci.updateViewPlatformToCoe = false ;
	    return ;
	}

	//
	// Otherwise, get the offset of the origin of view platform
	// coordinates relative to the origin of coexistence.  This is is
	// specified by two policies: the view platform's view attach policy
	// and the physical environment's coexistence center in pworld policy.
	//
	double eyeOffset ;
	double eyeHeight = body.getNominalEyeHeightFromGround() ;
	int viewAttachPolicy = view.getViewPlatform().getViewAttachPolicy() ;
	int pworldAttachPolicy = env.getCoexistenceCenterInPworldPolicy() ;

	if (eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW)
	    // The view platform origin is the same as the eye position.
	    eyeOffset = ci.getFieldOfViewOffset() ;
	else
	    // The view platform origin is independent of the eye position.
	    eyeOffset = body.getNominalEyeOffsetFromNominalScreen() ;

	if (pworldAttachPolicy == View.NOMINAL_SCREEN) {
	    // The default.  The physical coexistence origin locates the
	    // nominal screen.	This is rarely, if ever, set to anything
	    // else, and the intended effects of the other settings are
	    // not well documented.
	    if (viewAttachPolicy == View.NOMINAL_HEAD) {
		// The default.	 The view platform origin is at the origin
		// of the nominal head in coexistence coordinates, offset
		// from the screen along +Z.  If the window eyepoint
		// policy is RELATIVE_TO_FIELD_OF_VIEW, then the eyepoint
		// is the same as the view platform origin.
		v3d.set(0.0, 0.0, eyeOffset) ;
	    }
	    else if (viewAttachPolicy == View.NOMINAL_SCREEN) {
		// View platform and coexistence are the same except for
		// scale.
		v3d.set(0.0, 0.0, 0.0) ;
	    }
	    else {
		// The view platform origin is at the ground beneath the
		// head.
		v3d.set(0.0, -eyeHeight, eyeOffset) ;
	    }
	}
	else if (pworldAttachPolicy == View.NOMINAL_HEAD) {
	    // The physical coexistence origin locates the nominal head.
	    if (viewAttachPolicy == View.NOMINAL_HEAD) {
		// The view platform origin is set to the head;
		// coexistence and view platform coordinates differ only
		// in scale.
		v3d.set(0.0, 0.0, 0.0) ;
	    }
	    else if (viewAttachPolicy == View.NOMINAL_SCREEN) {
		// The view platform is set in front of the head, at the
		// nominal screen location.
		v3d.set(0.0, 0.0, -eyeOffset) ;
	    }
	    else {
		// The view platform origin is at the ground beneath the
		// head.
		v3d.set(0.0, -eyeHeight, 0.0) ;
	    }
	}
	else {
	    // The physical coexistence origin locates the nominal feet.
	    if (viewAttachPolicy == View.NOMINAL_HEAD) {
		v3d.set(0.0, eyeHeight, 0.0) ;
	    }
	    else if (viewAttachPolicy == View.NOMINAL_SCREEN) {
		v3d.set(0.0, eyeHeight, -eyeOffset) ;
	    }
	    else {
		v3d.set(0.0, 0.0, 0.0) ;
	    }
	}

	ci.viewPlatformToCoe.setTranslation(v3d) ;
	ci.updateViewPlatformToCoe = false ;
	if (verbose) t3dPrint(ci.viewPlatformToCoe, "vpToCoe") ;
    }

    /**
     * Gets the current transform from coexistence coordinates to
     * view platform coordinates and copies it into the given transform.<p>
     *
     * This method requires a Canvas3D.	 The returned transform may differ
     * across canvases for the same reasons as discussed in the description of
     * <code>getViewPlatformToCoexistence</code>.<p>
     *
     * @param c3d the Canvas3D to use
     * @param coe2vp the Transform3D to receive the transform
     * @see #getViewPlatformToCoexistence
     *       getViewPlatformToCoexistence(Canvas3D, Transform3D)
     */
    public void getCoexistenceToViewPlatform(Canvas3D c3d,
					     Transform3D coe2vp) {

	CanvasInfo ci = updateCache
	    (c3d, "getCoexistenceToViewPlatform", false) ;

	getCoexistenceToViewPlatform(ci) ;
	coe2vp.set(ci.coeToViewPlatform) ;
    }

    private void getCoexistenceToViewPlatform(CanvasInfo ci) {
	if (ci.updateCoeToViewPlatform) {
	    if (verbose) System.err.println("updating CoeToViewPlatform") ;
	    if (ci.coeToViewPlatform == null)
		ci.coeToViewPlatform = new Transform3D() ;

	    getViewPlatformToCoexistence(ci) ;
	    ci.coeToViewPlatform.invert(ci.viewPlatformToCoe) ;

	    ci.updateCoeToViewPlatform = false ;
	    if (verbose) t3dPrint(ci.coeToViewPlatform, "coeToVp") ;
	}
    }

    /**
     * Gets the current transform from coexistence coordinates to virtual
     * world coordinates and copies it into the given transform.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.<p>
     *
     * This method requires a Canvas3D.	 The returned transform may differ
     * across canvases for the same reasons as discussed in the description of
     * <code>getViewPlatformToCoexistence</code>.<p>
     *
     * @param c3d the Canvas3D to use
     * @param coe2vw the Transform3D to receive the transform
     * @see #getViewPlatformToCoexistence
     *       getViewPlatformToCoexistence(Canvas3D, Transform3D)
     */
    public void getCoexistenceToVworld(Canvas3D c3d,
				       Transform3D coe2vw) {

	CanvasInfo ci = updateCache(c3d, "getCoexistenceToVworld", true) ;
	getCoexistenceToVworld(ci) ;
	coe2vw.set(ci.coeToVworld) ;
    }

    private void getCoexistenceToVworld(CanvasInfo ci) {
	if (ci.updateCoeToVworld) {
	    if (verbose) System.err.println("updating CoexistenceToVworld") ;
	    if (ci.coeToVworld == null) ci.coeToVworld = new Transform3D() ;

	    getCoexistenceToViewPlatform(ci) ;
	    ci.coeToVworld.mul(vpi.viewPlatformToVworld,
			       ci.coeToViewPlatform) ;

	    ci.updateCoeToVworld = false ;
	}
    }

    /**
     * Gets the transforms from eye coordinates to image plate coordinates and
     * copies them into the Transform3Ds specified.<p>
     * 
     * When head tracking is used the eye positions are taken from the head
     * position and set in relation to the image plates with each Screen3D's
     * <code>trackerBaseToImagePlate</code> transform.	Otherwise the window
     * eyepoint policy is used to derive the eyepoint relative to the image
     * plate.  When using a head mounted display the eye position is
     * determined solely by calibration constants in Screen3D and
     * PhysicalBody; see the source code for the private method
     * <code>getEyesHMD</code> for more information.<p>
     * 
     * Eye coordinates are always aligned with image plate coordinates, so
     * these transforms are always just translations.  With a monoscopic
     * canvas the eye transform is copied to the first argument and the second
     * argument is not used.  For a stereo canvas the first argument receives
     * the left eye transform, and if the second argument is non-null it
     * receives the right eye transform.
     *
     * @param c3d the Canvas3D associated with the image plate
     * @param e2ipl the Transform3D to receive left transform
     * @param e2ipr the Transform3D to receive right transform, or null
     */
    public void getEyeToImagePlate(Canvas3D c3d,
				   Transform3D e2ipl, Transform3D e2ipr) {

	CanvasInfo ci = updateCache(c3d, "getEyeToImagePlate", false) ;
	getEyeToImagePlate(ci) ;
	e2ipl.set(ci.eyeToPlate) ;
	if (ci.useStereo && e2ipr != null)
	    e2ipr.set(ci.rightEyeToPlate) ;
    }

    private void getEyeToImagePlate(CanvasInfo ci) {
	if (ci.updateEyeInPlate) {
	    if (verbose) System.err.println("updating EyeInPlate") ;
	    if (ci.eyeToPlate == null)
		ci.eyeToPlate = new Transform3D() ;

	    if (viewPolicy == View.HMD_VIEW) {
		getEyesHMD(ci) ;
	    }
	    else if (useTracking) {
		getEyesTracked(ci) ;
	    }
	    else {
		getEyesFixedScreen(ci) ;
	    }
	    ci.updateEyeInPlate = false ;
	    if (verbose) System.err.println("eyeInPlate: " + ci.eyeInPlate) ;
	}
    }

    //
    // Get physical eye positions for head mounted displays.  These are
    // determined solely by the headTrackerToImagePlate and headToHeadTracker
    // calibration constants defined by Screen3D and the PhysicalBody.
    //
    // Note that headTrackerLeftToImagePlate and headTrackerToRightImagePlate
    // should be set according to the *apparent* position and orientation of
    // the image plates, relative to the head and head tracker, as viewed
    // through the HMD optics.	This is also true of the "physical" screen
    // width and height specified by the Screen3D -- they should be the
    // *apparent* width and height as viewed through the HMD optics.  They
    // must be set directly through the Screen3D methods; the default pixel
    // metrics of 90 pixels/inch used by Java 3D aren't appropriate for HMD
    // optics. 
    // 
    // Most HMDs have 100% overlap between the left and right displays; in
    // that case, headTrackerToLeftImagePlate and headTrackerToRightImagePlate
    // should be identical.  The HMD manufacturer's specifications of the
    // optics in terms of field of view, image overlap, and distance to the
    // focal plane should be used to derive these parameters.
    //
    private void getEyesHMD(CanvasInfo ci) {
	if (ci.useStereo) {
	    // This case is for head mounted displays driven by a single
	    // stereo canvas on a single screen.  These use a field sequential
	    // stereo signal to split the left and right images.
	    leftEye.set(leftEyeInHead) ;
	    headToHeadTracker.transform(leftEye) ;
	    ci.si.headTrackerToLeftPlate.transform(leftEye,
						   ci.eyeInPlate) ;
	    rightEye.set(rightEyeInHead) ;
	    headToHeadTracker.transform(rightEye) ;
	    ci.si.headTrackerToRightPlate.transform(rightEye,
						    ci.rightEyeInPlate) ;
	    if (ci.rightEyeToPlate == null)
		ci.rightEyeToPlate = new Transform3D() ;

	    v3d.set(ci.rightEyeInPlate) ;
	    ci.rightEyeToPlate.set(v3d) ;
	}
	else {
	    // This case is for 2-channel head mounted displays driven by two
	    // monoscopic screens, one for each eye.
	    switch (ci.monoscopicPolicy) {
	    case View.LEFT_EYE_VIEW:
		leftEye.set(leftEyeInHead) ;
		headToHeadTracker.transform(leftEye) ;
		ci.si.headTrackerToLeftPlate.transform(leftEye,
						       ci.eyeInPlate) ;
		break ;
	    case View.RIGHT_EYE_VIEW:
		rightEye.set(rightEyeInHead) ;
		headToHeadTracker.transform(rightEye) ;
		ci.si.headTrackerToRightPlate.transform(rightEye,
							ci.eyeInPlate) ;
		break ;
	    case View.CYCLOPEAN_EYE_VIEW:
	    default:
		throw new IllegalStateException
		    ("Illegal monoscopic view policy for 2-channel HMD") ;
	    }
	}
	v3d.set(ci.eyeInPlate) ;
	ci.eyeToPlate.set(v3d) ;
    }

    private void getEyesTracked(CanvasInfo ci) {
	leftEye.set(leftEyeInHead) ;
	rightEye.set(rightEyeInHead) ;
	headToTrackerBase.transform(leftEye) ;
	headToTrackerBase.transform(rightEye) ;
	if (coeCentering) {
	    // Coexistence and tracker base coordinates are the same.
	    // Centering is normally turned off for tracking.
	    getCoexistenceToImagePlate(ci) ;
	    ci.coeToPlate.transform(leftEye) ;
	    ci.coeToRightPlate.transform(rightEye) ;
	}
	else {
	    // The normal policy for head tracking.
	    ci.si.trackerBaseToPlate.transform(leftEye) ;
	    ci.si.trackerBaseToPlate.transform(rightEye) ;
	}
	setEyeScreenRelative(ci, leftEye, rightEye) ;
    }

    private void getEyesFixedScreen(CanvasInfo ci) {
	switch (eyePolicy) {
	case View.RELATIVE_TO_FIELD_OF_VIEW:
	    double z = ci.getFieldOfViewOffset() ;
	    setEyeWindowRelative(ci, z, z) ;
	    break ;
	case View.RELATIVE_TO_WINDOW:
	    setEyeWindowRelative(ci,
				 ci.leftManualEyeInPlate.z,
				 ci.rightManualEyeInPlate.z) ;
	    break ;
	case View.RELATIVE_TO_SCREEN:
	    setEyeScreenRelative(ci, 
				 ci.leftManualEyeInPlate,
				 ci.rightManualEyeInPlate) ;
	    break ;
	case View.RELATIVE_TO_COEXISTENCE:
	    view.getLeftManualEyeInCoexistence(leftEye) ;
	    view.getRightManualEyeInCoexistence(rightEye) ;

	    getCoexistenceToImagePlate(ci) ;
	    ci.coeToPlate.transform(leftEye) ;
	    ci.coeToRightPlate.transform(rightEye) ;
	    setEyeScreenRelative(ci, leftEye, rightEye) ;
	    break ;
	}
    }

    private void setEyeWindowRelative(CanvasInfo ci,
				      double leftZ, double rightZ) {

	// Eye position X is offset from the window center.
	double centerX = (ci.canvasX + (ci.canvasWidth / 2.0)) ;
	leftEye.x  = centerX + leftEyeInHead.x ;
	rightEye.x = centerX + rightEyeInHead.x ;

	// Eye position Y is always the canvas center.
	leftEye.y = rightEye.y = ci.canvasY + (ci.canvasHeight / 2.0) ;
	
	// Eye positions Z are as given.
	leftEye.z = leftZ ;
	rightEye.z = rightZ ;

	setEyeScreenRelative(ci, leftEye, rightEye) ;
    }

    private void setEyeScreenRelative(CanvasInfo ci,
				      Point3d leftEye, Point3d rightEye) {
	if (ci.useStereo) {
	    ci.eyeInPlate.set(leftEye) ;
	    ci.rightEyeInPlate.set(rightEye) ;

	    if (ci.rightEyeToPlate == null)
		ci.rightEyeToPlate = new Transform3D() ;

	    v3d.set(ci.rightEyeInPlate) ;
	    ci.rightEyeToPlate.set(v3d) ;
	}
	else {
	    switch (ci.monoscopicPolicy) {
	    case View.CYCLOPEAN_EYE_VIEW:
		ci.eyeInPlate.set((leftEye.x + rightEye.x) / 2.0,
				  (leftEye.y + rightEye.y) / 2.0,
				  (leftEye.z + rightEye.z) / 2.0) ;
		break ;
	    case View.LEFT_EYE_VIEW:
		ci.eyeInPlate.set(leftEye) ;
		break ;
	    case View.RIGHT_EYE_VIEW:
		ci.eyeInPlate.set(rightEye) ;
		break ;
	    }
	}
	v3d.set(ci.eyeInPlate) ;
	ci.eyeToPlate.set(v3d) ;
    }

    /**
     * Gets the current transforms from eye coordinates to view platform
     * coordinates and copies them into the given Transform3Ds.<p>
     * 
     * With a monoscopic canvas the eye transform is copied to the first
     * argument and the second argument is not used.  For a stereo canvas the
     * first argument receives the left eye transform, and if the second
     * argument is non-null it receives the right eye transform.<p>
     *
     * This method requires a Canvas3D.	 When using a head mounted display,
     * head tracking with fixed screens, or a window eyepoint policy of
     * <code>RELATIVE_TO_COEXISTENCE</code>, then the transforms returned may
     * be different for each canvas if stereo is not in use and they have
     * different monoscopic view policies.  They may additionally differ in
     * scale across canvases with the <code>PHYSICAL_WORLD</code> window
     * resize policy or the <code>SCALE_SCREEN_SIZE</code> screen scale
     * policy, which alter the scale depending upon the width of the canvas or
     * the width of the screen respectively.<p>
     * 
     * With window eyepoint policies of <code>RELATIVE_TO_FIELD_OF_VIEW</code>,
     * <code>RELATIVE_TO_SCREEN</code>, or <code>RELATIVE_TO_WINDOW</code>,
     * then the transforms returned may differ across canvases due to
     * the following additional conditions:<p><ul>
     * 
     * <li>The window eyepoint policy is <code>RELATIVE_TO_WINDOW</code> or
     *	   <code>RELATIVE_TO_SCREEN</code>, in which case the manual eye
     *	   position in image plate can be set differently for each
     *	   canvas.</li><p>
     *
     * <li>The window eyepoint policy is <code>RELATIVE_TO_FIELD_OF_VIEW</code>
     *	   and the view attach policy is <code>NOMINAL_SCREEN</code>, which
     *	   decouples the view platform's canvas Z offset from the eyepoint's
     *	   canvas Z offset.</li><p> 
     *
     * <li>The eyepoint X and Y coordinates are centered in the canvas with a
     *	   window eyepoint policy of <code>RELATIVE_TO_FIELD_OF_VIEW</code>
     *	   or <code>RELATIVE_TO_WINDOW</code>, and a window movement policy
     *	   of <code>VIRTUAL_WORLD</code> centers the view platform's X and Y
     *	   coordinates to the middle of the screen.</li><p>
     *
     * <li>Coexistence centering is set false, which allows each canvas and
     *	   screen to have a different position with respect to coexistence
     *	   coordinates.</li></ul>
     *
     * @param c3d the Canvas3D to use
     * @param e2vpl the Transform3D to receive the left transform
     * @param e2vpr the Transform3D to receive the right transform, or null
     */
    public void getEyeToViewPlatform(Canvas3D c3d, 
				     Transform3D e2vpl, Transform3D e2vpr) {

	CanvasInfo ci = updateCache(c3d, "getEyeToViewPlatform", false) ;
	getEyeToViewPlatform(ci) ;
	e2vpl.set(ci.eyeToViewPlatform) ;
	if (ci.useStereo && e2vpr != null)
	    e2vpr.set(ci.rightEyeToViewPlatform) ;
    }

    private void getEyeToViewPlatform(CanvasInfo ci) {
	if (ci.updateEyeToViewPlatform) {
	    if (verbose) System.err.println("updating EyeToViewPlatform") ;
	    if (ci.eyeToViewPlatform == null)
		ci.eyeToViewPlatform = new Transform3D() ;

	    getEyeToImagePlate(ci) ;
	    getImagePlateToViewPlatform(ci) ;
	    ci.eyeToViewPlatform.mul(ci.plateToViewPlatform, ci.eyeToPlate) ;

	    if (ci.useStereo) {
		if (ci.rightEyeToViewPlatform == null) 
		    ci.rightEyeToViewPlatform = new Transform3D() ;

		ci.rightEyeToViewPlatform.mul
		    (ci.rightPlateToViewPlatform, ci.rightEyeToPlate) ;
	    }
	    ci.updateEyeToViewPlatform = false ;
	    if (verbose) t3dPrint(ci.eyeToViewPlatform, "eyeToVp") ;
	}
    }

    /**
     * Gets the current transforms from view platform coordinates to eye
     * coordinates and copies them into the given Transform3Ds.<p>
     * 
     * With a monoscopic canvas the eye transform is copied to the first
     * argument and the second argument is not used.  For a stereo canvas the
     * first argument receives the left eye transform, and if the second
     * argument is non-null it receives the right eye transform.<p>
     *
     * This method requires a Canvas3D.	 The transforms returned may differ
     * across canvases for all the same reasons discussed in the description
     * of <code>getEyeToViewPlatform</code>.
     * 
     * @param c3d the Canvas3D to use
     * @param vp2el the Transform3D to receive the left transform
     * @param vp2er the Transform3D to receive the right transform, or null
     * @see #getEyeToViewPlatform
     *       getEyeToViewPlatform(Canvas3D, Transform3D, Transform3D)
     */
    public void getViewPlatformToEye(Canvas3D c3d, 
				     Transform3D vp2el, Transform3D vp2er) {

	CanvasInfo ci = updateCache(c3d, "getViewPlatformToEye", false) ;
	getViewPlatformToEye(ci) ;
	vp2el.set(ci.viewPlatformToEye) ;
	if (ci.useStereo && vp2er != null)
	    vp2er.set(ci.viewPlatformToRightEye) ;
    }

    private void getViewPlatformToEye(CanvasInfo ci) {
	if (ci.updateViewPlatformToEye) {
	    if (verbose) System.err.println("updating ViewPlatformToEye") ;
	    if (ci.viewPlatformToEye == null)
		ci.viewPlatformToEye = new Transform3D() ;

	    getEyeToViewPlatform(ci) ;
	    ci.viewPlatformToEye.invert(ci.eyeToViewPlatform) ;

	    if (ci.useStereo) {
		if (ci.viewPlatformToRightEye == null)
		    ci.viewPlatformToRightEye = new Transform3D() ;

		ci.viewPlatformToRightEye.invert(ci.rightEyeToViewPlatform) ;
	    }
	    ci.updateViewPlatformToEye = false ;
	}
    }

    /**
     * Gets the current transforms from eye coordinates to virtual world
     * coordinates and copies them into the given Transform3Ds.<p>
     * 
     * With a monoscopic canvas the eye transform is copied to the first
     * argument and the second argument is not used.  For a stereo canvas the
     * first argument receives the left eye transform, and if the second
     * argument is non-null it receives the right eye transform.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.<p>
     *
     * This method requires a Canvas3D.	 The transforms returned may differ
     * across canvases for all the same reasons discussed in the description
     * of <code>getEyeToViewPlatform</code>.  
     *
     * @param c3d the Canvas3D to use
     * @param e2vwl the Transform3D to receive the left transform
     * @param e2vwr the Transform3D to receive the right transform, or null
     * @see #getEyeToViewPlatform
     *       getEyeToViewPlatform(Canvas3D, Transform3D, Transform3D)
     */
    public void getEyeToVworld(Canvas3D c3d,
			       Transform3D e2vwl, Transform3D e2vwr) {

	CanvasInfo ci = updateCache(c3d, "getEyeToVworld", true) ;
	getEyeToVworld(ci) ;
	e2vwl.set(ci.eyeToVworld) ;
	if (ci.useStereo && e2vwr != null)
	    e2vwr.set(ci.rightEyeToVworld) ;
    }

    private void getEyeToVworld(CanvasInfo ci) {
	if (ci.updateEyeToVworld) {
	    if (verbose) System.err.println("updating EyeToVworld") ;
	    if (ci.eyeToVworld == null)
		ci.eyeToVworld = new Transform3D() ;

	    getEyeToViewPlatform(ci) ;
	    ci.eyeToVworld.mul
		(vpi.viewPlatformToVworld, ci.eyeToViewPlatform) ;

	    if (ci.useStereo) {
		if (ci.rightEyeToVworld == null)
		    ci.rightEyeToVworld = new Transform3D() ;

		ci.rightEyeToVworld.mul
		    (vpi.viewPlatformToVworld, ci.rightEyeToViewPlatform) ;
	    }
	    ci.updateEyeToVworld = false ;
	}
    }

    /**
     * Gets the transforms from eye coordinates to clipping coordinates
     * and copies them into the given Transform3Ds.  These transforms take
     * a viewing volume bounded by the physical canvas edges and the
     * physical front and back clip planes and project it into a range
     * bound to [-1.0 .. +1.0] on each of the X, Y, and Z axes.	 If a
     * perspective projection has been specified then the physical image
     * plate eye location defines the apex of a viewing frustum;
     * otherwise, the orientation of the image plate determines the
     * direction of a parallel projection.<p>
     * 
     * With a monoscopic canvas the projection transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left projection transform,
     * and if the second argument is non-null it receives the right
     * projection transform.<p>
     *
     * If either of the clip policies <code>VIRTUAL_EYE</code> or
     * <code>VIRTUAL_SCREEN</code> are used, then the View should be attached
     * to a ViewPlatform that is part of a live scene graph and that has its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set; otherwise, a
     * scale factor of 1.0 will be used for the scale factor from virtual
     * world units to view platform units.
     * 
     * @param c3d the Canvas3D to use
     * @param e2ccl the Transform3D to receive left transform
     * @param e2ccr the Transform3D to receive right transform, or null
     */
    public void getProjection(Canvas3D c3d, 
			      Transform3D e2ccl, Transform3D e2ccr) {

	CanvasInfo ci = updateCache(c3d, "getProjection", true) ;
	getProjection(ci) ;
	e2ccl.set(ci.projection) ;
	if (ci.useStereo && e2ccr != null)
	    e2ccr.set(ci.rightProjection) ;
    }

    private void getProjection(CanvasInfo ci) {
	if (ci.updateProjection) {
	    if (verbose) System.err.println("updating Projection") ;
	    if (ci.projection == null)
		ci.projection = new Transform3D() ;

	    getEyeToImagePlate(ci) ;
	    getClipDistances(ci) ;

	    // Note: core Java 3D code insists that the back clip plane
	    // relative to the image plate must be the same left back clip
	    // distance for both the left and right eye.  Not sure why this
	    // should be, but the same is done here for compatibility.
	    double backClip = getBackClip(ci, ci.eyeInPlate) ;
	    computeProjection(ci, ci.eyeInPlate,
			      getFrontClip(ci, ci.eyeInPlate),
			      backClip, ci.projection) ;

	    if (ci.useStereo) {
		if (ci.rightProjection == null)
		    ci.rightProjection = new Transform3D() ;

		computeProjection(ci, ci.rightEyeInPlate,
				  getFrontClip(ci, ci.rightEyeInPlate),
				  backClip, ci.rightProjection) ;
	    }
	    ci.updateProjection = false ;
	    if (verbose) t3dPrint(ci.projection, "projection") ;
	}
    }

    /**
     * Gets the transforms from clipping coordinates to eye coordinates
     * and copies them into the given Transform3Ds.  These transforms take
     * the clip space volume bounded by the range [-1.0 .. + 1.0] on each
     * of the X, Y, and Z and project it into eye coordinates.<p>
     * 
     * With a monoscopic canvas the projection transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left projection transform, and
     * if the second argument is non-null it receives the right projection
     * transform.<p>
     *
     * If either of the clip policies <code>VIRTUAL_EYE</code> or
     * <code>VIRTUAL_SCREEN</code> are used, then the View should be attached
     * to a ViewPlatform that is part of a live scene graph and that has its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set; otherwise, a
     * scale factor of 1.0 will be used for the scale factor from virtual
     * world units to view platform units.
     * 
     * @param c3d the Canvas3D to use
     * @param cc2el the Transform3D to receive left transform
     * @param cc2er the Transform3D to receive right transform, or null
     */
    public void getInverseProjection(Canvas3D c3d, 
				     Transform3D cc2el, Transform3D cc2er) {

	CanvasInfo ci = updateCache(c3d, "getInverseProjection", true) ;
	getInverseProjection(ci) ;
	cc2el.set(ci.inverseProjection) ;
	if (ci.useStereo && cc2er != null)
	    cc2er.set(ci.inverseRightProjection) ;
    }

    private void getInverseProjection(CanvasInfo ci) {
	if (ci.updateInverseProjection) {
	    if (verbose) System.err.println("updating InverseProjection") ;
	    if (ci.inverseProjection == null)
		ci.inverseProjection = new Transform3D() ;

	    getProjection(ci) ;
	    ci.inverseProjection.invert(ci.projection) ;

	    if (ci.useStereo) {
		if (ci.inverseRightProjection == null)
		    ci.inverseRightProjection = new Transform3D() ;

		ci.inverseRightProjection.invert(ci.rightProjection) ;
	    }
	    ci.updateInverseProjection = false ;
	}
    }

    /**
     * Gets the transforms from clipping coordinates to view platform
     * coordinates and copies them into the given Transform3Ds.	 These
     * transforms take the clip space volume bounded by the range
     * [-1.0 .. +1.0] on each of the X, Y, and Z axes and project into
     * the view platform coordinate system.<p>
     * 
     * With a monoscopic canvas the projection transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left projection transform, and
     * if the second argument is non-null it receives the right projection
     * transform.<p>
     *
     * If either of the clip policies <code>VIRTUAL_EYE</code> or
     * <code>VIRTUAL_SCREEN</code> are used, then the View should be attached
     * to a ViewPlatform that is part of a live scene graph and that has its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set; otherwise, a
     * scale factor of 1.0 will be used for the scale factor from virtual
     * world units to view platform units.
     * 
     * @param c3d the Canvas3D to use
     * @param cc2vpl the Transform3D to receive left transform
     * @param cc2vpr the Transform3D to receive right transform, or null
     */
    public void getInverseViewPlatformProjection(Canvas3D c3d, 
						 Transform3D cc2vpl,
						 Transform3D cc2vpr) {

	CanvasInfo ci = updateCache
	    (c3d, "getInverseViewPlatformProjection", true) ;

	getInverseViewPlatformProjection(ci) ;
	cc2vpl.set(ci.inverseViewPlatformProjection) ;
	if (ci.useStereo & cc2vpr != null)
	    cc2vpr.set(ci.inverseViewPlatformRightProjection) ;
    }

    private void getInverseViewPlatformProjection(CanvasInfo ci) {
	if (ci.updateInverseViewPlatformProjection) {
	    if (verbose) System.err.println("updating InverseVpProjection") ;
	    if (ci.inverseViewPlatformProjection == null)
		ci.inverseViewPlatformProjection = new Transform3D() ;

	    getInverseProjection(ci) ;
	    getEyeToViewPlatform(ci) ;
	    ci.inverseViewPlatformProjection.mul
		(ci.eyeToViewPlatform, ci.inverseProjection) ;

	    if (ci.useStereo) {
		if (ci.inverseViewPlatformRightProjection == null)
		    ci.inverseViewPlatformRightProjection = new Transform3D() ;

		ci.inverseViewPlatformRightProjection.mul
		    (ci.rightEyeToViewPlatform, ci.inverseRightProjection) ;
	    }
	    ci.updateInverseVworldProjection = false ;
	}
    }

    /**
     * Gets the transforms from clipping coordinates to virtual world
     * coordinates and copies them into the given Transform3Ds.	 These
     * transforms take the clip space volume bounded by the range
     * [-1.0 .. +1.0] on each of the X, Y, and Z axes and project into
     * the virtual world.<p>
     * 
     * With a monoscopic canvas the projection transform is copied to the
     * first argument and the second argument is not used.  For a stereo
     * canvas the first argument receives the left projection transform, and
     * if the second argument is non-null it receives the right projection
     * transform.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.
     *
     * @param c3d the Canvas3D to use
     * @param cc2vwl the Transform3D to receive left transform
     * @param cc2vwr the Transform3D to receive right transform, or null
     */
    public void getInverseVworldProjection(Canvas3D c3d, 
					   Transform3D cc2vwl,
					   Transform3D cc2vwr) {

	CanvasInfo ci = updateCache(c3d, "getInverseVworldProjection", true) ;
	getInverseVworldProjection(ci) ;
	cc2vwl.set(ci.inverseVworldProjection) ;
	if (ci.useStereo & cc2vwr != null)
	    cc2vwr.set(ci.inverseVworldRightProjection) ;
    }

    private void getInverseVworldProjection(CanvasInfo ci) {
	if (ci.updateInverseVworldProjection) {
	    if (verbose) System.err.println("updating InverseVwProjection") ;
	    if (ci.inverseVworldProjection == null)
		ci.inverseVworldProjection = new Transform3D() ;

	    getInverseViewPlatformProjection(ci) ;
	    ci.inverseVworldProjection.mul
		(vpi.viewPlatformToVworld, ci.inverseViewPlatformProjection) ;

	    if (ci.useStereo) {
		if (ci.inverseVworldRightProjection == null)
		    ci.inverseVworldRightProjection = new Transform3D() ;

		ci.inverseVworldRightProjection.mul
		    (vpi.viewPlatformToVworld,
		     ci.inverseViewPlatformRightProjection) ;
	    }
	    ci.updateInverseVworldProjection = false ;
	}
    }

    //
    // Compute a projection matrix from the given eye position in image plate,
    // the front and back clip Z positions in image plate, and the current
    // canvas position in image plate.
    // 
    private void computeProjection(CanvasInfo ci, Point3d eye,
				   double front, double back, Transform3D p) {

	// Convert everything to eye coordinates.
	double lx = ci.canvasX - eye.x ;		   // left   (low x)
	double ly = ci.canvasY - eye.y ;		   // bottom (low y)
	double hx = (ci.canvasX+ci.canvasWidth)	 - eye.x ; // right  (high x)
	double hy = (ci.canvasY+ci.canvasHeight) - eye.y ; // top    (high y)
	double nz = front - eye.z ;			   // front  (near z)
	double fz = back  - eye.z ;			   // back   (far z)
	double iz = -eye.z ;				   // plate  (image z)

	if (projectionPolicy == View.PERSPECTIVE_PROJECTION)
	    computePerspectiveProjection(lx, ly, hx, hy, iz, nz, fz, m16d) ;
	else
	    computeParallelProjection(lx, ly, hx, hy, nz, fz, m16d) ;

	p.set(m16d) ;
    }

    //
    // Compute a perspective projection from the given eye-space bounds.
    //
    private void computePerspectiveProjection(double lx, double ly,
					      double hx, double hy,
					      double iz, double nz,
					      double fz, double[] m) {
        //
        // We first derive the X and Y projection components without regard
        // for Z scaling.  The Z scaling or perspective depth is handled by
        // matrix elements expressed solely in terms of the near and far clip
        // planes.
        //
        // Since the eye is at the origin, the projector for any point V in
        // eye space is just V.  Any point along this ray can be expressed in
        // parametric form as P = tV.  To find the projection onto the plane
        // containing the canvas, find t such that P.z = iz; ie, t = iz/V.z.
        // The projection P is thus [V.x*iz/V.z, V.y*iz/V.z, iz].
        // 
        // This projection can expressed as the following matrix equation:
        //
        //   -iz     0     0     0       V.x
        //    0     -iz    0     0   X   V.y
        //    0      0    -iz    0       V.z
        //    0      0    -1     0        1              {matrix 1}
        //
        // where the matrix elements have been negated so that w is positive.
        // This is mostly by convention, although some hardware won't handle
        // clipping in the -w half-space.
        //
        // After the point has been projected to the image plate, the
        // canvas bounds need to be mapped to the [-1..1] of Java 3D's
        // clipping space.  The scale factor for X is thus 2/(hx - lx); adding
        // the translation results in (V.x - lx)(2/(hx - lx)) - 1, which after
        // some algebra can be confirmed to the same as the following
        // canonical scale/offset form:
        // 
        //   V.x*2/(hx - lx) - (hx + lx)/(hx - lx)
        //
        // Similarly for Y:
        //
        //   V.y*2/(hy - ly) - (hy + ly)/(hy - ly)
        //
        // If we set idx = 1/(hx - lx) and idy = 1/(hy - ly), then we get:
        //
        //   2*V.x*idx - (hx + lx)idx
        //   2*V.y*idy - (hy + ly)idy
        // 
        // These scales and offsets are represented by the following matrix:
        // 
        //   2*idx       0         0  -(hx + lx)*idx
        //     0       2*idy       0  -(hy + ly)*idy
        //     0         0         1         0      
        //     0         0         0         1           {matrix 2} 
        //
        // The result after concatenating the projection transform
        // ({matrix 2} X {matrix 1}):
        // 
        //   -2*iz*idx     0      (hx + lx)*idx    0
        //       0     -2*iz*idy  (hy + ly)*idy    0
        //       0         0           -iz {a}     0 {b}
        //       0         0           -1          0     {matrix 3}
        // 
        // The Z scaling is handled by m[10] ("a") and m[11] ("b"), which must
        // map the range [front..back] to [1..-1] in clipping space.  If ze is
        // the Z coordinate in eye space, and zc is the Z coordinate in
        // clipping space after division by w, then from {matrix 3}:
        // 
        //   zc =  (a*ze + b)/-ze = -(a + b/ze)
        // 
        // We want this to map to +1 when ze is at the near clip plane, and
        // to -1 when ze is at the far clip plane:
        //
        //   -(a + b/nz) = +1
        //   -(a + b/fz) = -1
        //
        // Solving results in:
        //
        //   a = -(nz + fz)/(nz - fz)
        //   b =  (2*nz*fz)/(nz - fz).
        //
        // NOTE: this produces a perspective transform that has matrix
        // components with a different scale than the matrix computed by the
        // Java 3D core.  They do in fact effect the equivalent clipping in 4D
        // homogeneous coordinates and project to the same 3D Euclidean
        // coordinates. m[14] is always -1 in our derivation above.  If the
        // matrix components produced by Java 3D core are divided by its value
        // of -m[14], then both matrices are the same.
        //
	double idx = 1.0 / (hx - lx) ;
	double idy = 1.0 / (hy - ly) ;
	double idz = 1.0 / (nz - fz) ;

	m[0]  = -2.0 * iz * idx ;
	m[5]  = -2.0 * iz * idy ;
	m[2]  =	 (hx + lx) * idx ;
	m[6]  =	 (hy + ly) * idy ;
	m[10] = -(nz + fz) * idz ;
	m[11] =	 2.0 * fz * nz * idz ;
	m[14] = -1.0 ;
	m[1] = m[3] = m[4] = m[7] = m[8] = m[9] = m[12] = m[13] = m[15] = 0.0 ;
    }

    //
    // Compute a parallel projection from the given eye-space bounds.
    //
    private void computeParallelProjection(double lx, double ly,
					   double hx, double hy,
					   double nz, double fz, double[] m) {
	//
	// A parallel projection in eye space just involves scales and offsets
	// with no w division.	We can use {matrix 2} for the X and Y scales
	// and offsets and then use a linear mapping of the front and back
	// clip distances to the [1..-1] Z clip range.
	//
	double idx = 1.0 / (hx - lx) ;
	double idy = 1.0 / (hy - ly) ;
	double idz = 1.0 / (nz - fz) ;

	m[0]  = 2.0 * idx ;
	m[5]  = 2.0 * idy ;
	m[10] = 2.0 * idz ;
	m[3]  = -(hx + lx) * idx ;
	m[7]  = -(hy + ly) * idy ;
	m[11] = -(nz + fz) * idz ;
	m[15] = 1.0 ;
	m[1] = m[2] = m[4] = m[6] = m[8] = m[9] = m[12] = m[13] = m[14] = 0.0 ;
    }

    //
    // Get front clip plane Z coordinate in image plate space.
    // 
    private double getFrontClip(CanvasInfo ci, Point3d eye) {
	if (frontClipPolicy == View.PHYSICAL_EYE ||
	    frontClipPolicy == View.VIRTUAL_EYE) {
	    return eye.z - ci.frontClipDistance ;
	}
	else {
	    return - ci.frontClipDistance ;
	}
    }

    //
    // Get back clip plane Z coordinate in image plate space.
    // 
    private double getBackClip(CanvasInfo ci, Point3d eye) {
	//
	// Note: Clip node status is unavailable here.	If a clip node is
	// active in the scene graph, it should override the view's back
	// clip plane.
	//
	if (backClipPolicy == View.PHYSICAL_EYE ||
	    backClipPolicy == View.VIRTUAL_EYE) {
	    return eye.z - ci.backClipDistance ;
	}
	else {
	    return -ci.backClipDistance ;
	}
    }

    //
    // Compute clip distance scale.
    //
    private double getClipScale(CanvasInfo ci, int clipPolicy) {
	if (clipPolicy == View.VIRTUAL_EYE ||
	    clipPolicy == View.VIRTUAL_SCREEN) {
	    getScreenScale(ci) ;
	    if (resizePolicy == View.PHYSICAL_WORLD)
		return vpi.vworldToViewPlatformScale * ci.screenScale *
		    ci.windowScale ;
	    else
		return vpi.vworldToViewPlatformScale * ci.screenScale ;
	}
	else {
	    if (resizePolicy == View.PHYSICAL_WORLD)
		return ci.windowScale ;  // see below
	    else
		return 1.0 ;
	}
    }

    /**
     * Gets the front clip distance scaled to physical meters.  This is useful
     * for ensuring that objects positioned relative to a physical coordinate
     * system (such as eye, image plate, or coexistence) will be within the
     * viewable Z depth.  This distance will be relative to either the eye or
     * the image plate depending upon the front clip policy.<p>
     *
     * Note that this is not necessarily the clip distance as set by
     * <code>setFrontClipDistance</code>, even when the front clip policy
     * is <code>PHYSICAL_SCREEN</code> or <code>PHYSICAL_EYE</code>.  <i>If
     * the window resize policy is <code>PHYSICAL_WORLD</code>, then physical
     * clip distances as specified are in fact scaled by the ratio of the
     * window width to the screen width.</i> The Java 3D view model does this
     * to prevent the physical clip planes from moving with respect to the
     * virtual world when the window is resized.<p>
     *
     * If either of the clip policies <code>VIRTUAL_EYE</code> or
     * <code>VIRTUAL_SCREEN</code> are used, then the View should be attached
     * to a ViewPlatform that is part of a live scene graph and that has its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set; otherwise, a
     * scale factor of 1.0 will be used for the scale factor from virtual
     * world units to view platform units.
     * 
     * @param c3d the Canvas3D to use
     * @return the physical front clip distance
     */
    public double getPhysicalFrontClipDistance(Canvas3D c3d) {
	CanvasInfo ci = updateCache
	    (c3d, "getPhysicalFrontClipDistance", true) ;

	getClipDistances(ci) ;
	return ci.frontClipDistance ;
    }

    /**
     * Gets the back clip distance scaled to physical meters.  This is useful
     * for ensuring that objects positioned relative to a physical coordinate
     * system (such as eye, image plate, or coexistence) will be within the
     * viewable Z depth.  This distance will be relative to either the eye or
     * the image plate depending upon the back clip policy.<p>
     *
     * Note that this is not necessarily the clip distance as set by
     * <code>setBackClipDistance</code>, even when the back clip policy
     * is <code>PHYSICAL_SCREEN</code> or <code>PHYSICAL_EYE</code>.  <i>If
     * the window resize policy is <code>PHYSICAL_WORLD</code>, then physical
     * clip distances as specified are in fact scaled by the ratio of the
     * window width to the screen width.</i> The Java 3D view model does this
     * to prevent the physical clip planes from moving with respect to the
     * virtual world when the window is resized.<p>
     *
     * If either of the clip policies <code>VIRTUAL_EYE</code> or
     * <code>VIRTUAL_SCREEN</code> are used, then the View should be attached
     * to a ViewPlatform that is part of a live scene graph and that has its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set; otherwise, a
     * scale factor of 1.0 will be used for the scale factor from virtual
     * world units to view platform units.
     * 
     * @param c3d the Canvas3D to use
     * @return the physical back clip distance
     */
    public double getPhysicalBackClipDistance(Canvas3D c3d) {
	CanvasInfo ci = updateCache(c3d, "getPhysicalBackClipDistance", true) ;
	getClipDistances(ci) ;
	return ci.backClipDistance ;
    }

    private void getClipDistances(CanvasInfo ci) {
	if (ci.updateClipDistances) {
	    if (verbose) System.err.println("updating clip distances") ;

	    ci.frontClipDistance = view.getFrontClipDistance() *
		getClipScale(ci, frontClipPolicy) ;
		
	    ci.backClipDistance = view.getBackClipDistance() *
		getClipScale(ci, backClipPolicy) ;

	    ci.updateClipDistances = false ;
	    if (verbose) {
		System.err.println
		    ("  front clip distance " + ci.frontClipDistance) ;
		System.err.println
		    ("  back clip distance  " + ci.backClipDistance) ;
	    }
	}
    }

    private void getScreenScale(CanvasInfo ci) {
	if (ci.updateScreenScale) {
	    if (verbose) System.err.println("updating screen scale") ;

	    if (scalePolicy == View.SCALE_SCREEN_SIZE)
		ci.screenScale = ci.si.screenWidth / 2.0 ;
	    else
		ci.screenScale = view.getScreenScale() ;

	    ci.updateScreenScale = false ;
	    if (verbose) System.err.println("screen scale " + ci.screenScale) ;
	}
    }

    /**
     * Gets the scale factor from physical meters to view platform units.<p>
     * 
     * This method requires a Canvas3D.	 A different scale may be returned
     * for each canvas in the view if any of the following apply:<p><ul>
     *
     * <li>The window resize policy is <code>PHYSICAL_WORLD</code>, which
     *	   alters the scale depending upon the width of the canvas.</li><p>
     *
     * <li>The screen scale policy is <code>SCALE_SCREEN_SIZE</code>,
     *	   which alters the scale depending upon the width of the screen
     *	   associated with the canvas.</li></ul>
     *
     * @param c3d the Canvas3D to use
     * @return the physical to view platform scale
     */
    public double getPhysicalToViewPlatformScale(Canvas3D c3d) {
	CanvasInfo ci = updateCache
	    (c3d, "getPhysicalToViewPlatformScale", false) ;

	getPhysicalToViewPlatformScale(ci) ;
	return ci.physicalToVpScale ;
    }

    private void getPhysicalToViewPlatformScale(CanvasInfo ci) {
	if (ci.updatePhysicalToVpScale) {
	    if (verbose) System.err.println("updating PhysicalToVp scale") ;

	    getScreenScale(ci) ;
	    if (resizePolicy == View.PHYSICAL_WORLD)
		ci.physicalToVpScale = 1.0/(ci.screenScale * ci.windowScale) ;
	    else
		ci.physicalToVpScale = 1.0/ci.screenScale ;

	    ci.updatePhysicalToVpScale = false ;
	    if (verbose) System.err.println("PhysicalToVp scale " +
					    ci.physicalToVpScale) ;
	}
    }

    /**
     * Gets the scale factor from physical meters to virtual units.<p>
     * 
     * This method requires a Canvas3D.	 A different scale may be returned
     * across canvases for the same reasons as discussed in the description of
     * <code>getPhysicalToViewPlatformScale</code>.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.
     *
     * @param c3d the Canvas3D to use
     * @return the physical to virtual scale
     * @see #getPhysicalToViewPlatformScale
     *       getPhysicalToViewPlatformScale(Canvas3D)
     */
    public double getPhysicalToVirtualScale(Canvas3D c3d) {
	CanvasInfo ci = updateCache(c3d, "getPhysicalToVirtualScale", true) ;
	getPhysicalToVirtualScale(ci) ;
	return ci.physicalToVirtualScale ;
    }

    private void getPhysicalToVirtualScale(CanvasInfo ci) {
	if (ci.updatePhysicalToVirtualScale) {
	    if (verbose)
		System.err.println("updating PhysicalToVirtual scale") ;

	    getPhysicalToViewPlatformScale(ci) ;
	    ci.physicalToVirtualScale =
		ci.physicalToVpScale / vpi.vworldToViewPlatformScale ;

	    ci.updatePhysicalToVirtualScale = false ;
	    if (verbose) System.err.println("PhysicalToVirtual scale " +
					    ci.physicalToVirtualScale) ;
	}
    }

    /**
     * Gets the width of the specified canvas scaled to physical meters.  This
     * is derived from the physical screen width as reported by the Screen3D
     * associated with the canvas.  If the screen width is not explicitly set
     * using the <code>setPhysicalScreenWidth</code> method of Screen3D, then
     * Java 3D will derive the screen width based on a screen resolution of 90
     * pixels/inch.
     * 
     * @param c3d the Canvas3D to use
     * @return the width of the canvas scaled to physical meters
     */
    public double getPhysicalWidth(Canvas3D c3d) {
	CanvasInfo ci = updateCache(c3d, "getPhysicalWidth", false) ;
	return ci.canvasWidth ;
    }

    /**
     * Gets the height of the specified canvas scaled to physical meters.  This
     * is derived from the physical screen height as reported by the Screen3D
     * associated with the canvas.  If the screen height is not explicitly set
     * using the <code>setPhysicalScreenHeight</code> method of Screen3D, then
     * Java 3D will derive the screen height based on a screen resolution of 90
     * pixels/inch.
     * 
     * @param c3d the Canvas3D to use
     * @return the height of the canvas scaled to physical meters
     */
    public double getPhysicalHeight(Canvas3D c3d) {
	CanvasInfo ci = updateCache(c3d, "getPhysicalHeight", false) ;
	return ci.canvasHeight ;
    }

    /**
     * Gets the location of the specified canvas relative to the image plate
     * origin.  This is derived from the physical screen parameters as
     * reported by the Screen3D associated with the canvas.  If the screen
     * width and height are not explicitly set in Screen3D, then Java 3D will
     * derive those screen parameters based on a screen resolution of 90
     * pixels/inch.
     * 
     * @param c3d the Canvas3D to use
     * @param location the output position, in meters, of the lower-left
     *  corner of the canvas relative to the image plate lower-left corner; Z
     *  is always 0.0
     */
    public void getPhysicalLocation(Canvas3D c3d, Point3d location) {
	CanvasInfo ci = updateCache(c3d, "getPhysicalLocation", false) ;
	location.set(ci.canvasX, ci.canvasY, 0.0) ;
    }

    /**
     * Gets the location of the AWT pixel value and copies it into the
     * specified Point3d.
     *
     * @param c3d the Canvas3D to use
     * @param x the X coordinate of the pixel relative to the upper-left
     *  corner of the canvas
     * @param y the Y coordinate of the pixel relative to the upper-left
     *  corner of the canvas
     * @param location the output position, in meters, relative to the
     *  lower-left corner of the image plate; Z is always 0.0
     */
    public void getPixelLocationInImagePlate(Canvas3D c3d, int x, int y,
					     Point3d location) {

	CanvasInfo ci = updateCache
	    (c3d, "getPixelLocationInImagePlate", false) ;

	location.set(ci.canvasX + ((double)x * ci.si.metersPerPixelX),
		     ci.canvasY - ((double)y * ci.si.metersPerPixelY) +
		     ci.canvasHeight, 0.0) ;
    }

    /**
     * Gets a read from the specified sensor and transforms it to virtual
     * world coordinates.  The View must be attached to a ViewPlatform which
     * is part of a live scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.<p>
     *
     * This method requires a Canvas3D.	 The returned transform may differ
     * across canvases for the same reasons as discussed in the description of
     * <code>getViewPlatformToCoexistence</code>.
     *
     * @param sensor the Sensor instance to read
     * @param s2vw the output transform
     * @see #getViewPlatformToCoexistence
     *       getViewPlatformToCoexistence(Canvas3D, Transform3D)
     */
    public void getSensorToVworld(Canvas3D c3d, 
				  Sensor sensor, Transform3D s2vw) {

	CanvasInfo ci = updateCache(c3d, "getSensorToVworld", true) ;
	getTrackerBaseToVworld(ci) ;
	sensor.getRead(s2vw) ;
	s2vw.mul(ci.trackerBaseToVworld, s2vw) ;
    }

    /**
     * Gets the transform from tracker base coordinates to view platform
     * coordinates and copies it into the specified Transform3D.<p>
     *
     * This method requires a Canvas3D.	 The returned transform may differ
     * across canvases for the same reasons as discussed in the description of
     * <code>getViewPlatformToCoexistence</code>.
     *
     * @param c3d the Canvas3D to use
     * @param tb2vp the output transform
     * @see #getViewPlatformToCoexistence
     *       getViewPlatformToCoexistence(Canvas3D, Transform3D)
     */
    public void getTrackerBaseToViewPlatform(Canvas3D c3d, Transform3D tb2vp) {
	CanvasInfo ci = updateCache
	    (c3d, "getTrackerBaseToViewPlatform", false) ;

	getTrackerBaseToViewPlatform(ci) ;
	tb2vp.set(ci.trackerBaseToViewPlatform) ;
    }

    private void getTrackerBaseToViewPlatform(CanvasInfo ci) {
	if (ci.updateTrackerBaseToViewPlatform) {
	    if (verbose) System.err.println("updating TrackerBaseToVp") ;
	    if (ci.trackerBaseToViewPlatform == null)
		ci.trackerBaseToViewPlatform = new Transform3D() ;

	    getViewPlatformToCoexistence(ci) ;
	    ci.trackerBaseToViewPlatform.mul(coeToTrackerBase,
					     ci.viewPlatformToCoe) ;

	    ci.trackerBaseToViewPlatform.invert() ;
	    ci.updateTrackerBaseToViewPlatform = false ;
	    if (verbose) t3dPrint(ci.trackerBaseToViewPlatform,
				  "TrackerBaseToViewPlatform") ;
	}
    }

    /**
     * Gets the transform from tracker base coordinates to virtual world
     * coordinates and copies it into the specified Transform3D.  The View
     * must be attached to a ViewPlatform which is part of a live scene graph,
     * and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.<p>
     *
     * This method requires a Canvas3D.	 The returned transform may differ
     * across canvases for the same reasons as discussed in the description of
     * <code>getViewPlatformToCoexistence</code>.
     *
     * @param c3d the Canvas3D to use
     * @param tb2vw the output transform
     * @see #getViewPlatformToCoexistence
     *       getViewPlatformToCoexistence(Canvas3D, Transform3D)
     */
    public void getTrackerBaseToVworld(Canvas3D c3d, Transform3D tb2vw) {
	CanvasInfo ci = updateCache(c3d, "getTrackerBaseToVworld", true) ;
	getTrackerBaseToVworld(ci) ;
	tb2vw.set(ci.trackerBaseToVworld) ;
    }

    private void getTrackerBaseToVworld(CanvasInfo ci) {
	if (ci.updateTrackerBaseToVworld) {
	    if (verbose) System.err.println("updating TrackerBaseToVworld") ;
	    if (ci.trackerBaseToVworld == null)
		ci.trackerBaseToVworld = new Transform3D() ;
	    //
	    // We compute trackerBaseToViewPlatform and compose with
	    // viewPlatformToVworld instead of computing imagePlateToVworld
	    // and composing with trackerBaseToImagePlate.  That way it works
	    // with HMD and avoids the issue of choosing the left image plate
	    // or right image plate transform.
	    //
	    getTrackerBaseToViewPlatform(ci) ;
	    ci.trackerBaseToVworld.mul(vpi.viewPlatformToVworld,
				       ci.trackerBaseToViewPlatform) ;

	    ci.updateTrackerBaseToVworld = false ;
	}
    }

    /**
     * Release all static memory references held by ViewInfo, if any.  These
     * are the Screen3D and ViewPlatform maps shared by all existing ViewInfo
     * instances if they're not provided by a constructor.  Releasing the
     * screen references effectively releases all canvas references in all
     * ViewInfo instances as well.<p>
     *
     * It is safe to continue using existing ViewInfo instances after calling
     * this method; the data in the released maps will be re-derived as
     * needed.
     */
    public static synchronized void clear() {
	Iterator i = staticVpMap.values().iterator() ;
	while (i.hasNext()) ((ViewPlatformInfo)i.next()).clear() ;
	staticVpMap.clear() ;

	i = staticSiMap.values().iterator() ;
	while (i.hasNext()) ((ScreenInfo)i.next()).clear() ;
	staticSiMap.clear() ;
    }

    /**
     * Arrange for an update of cached screen parameters.  If automatic update
     * has not been enabled, then this method should be called if any of the
     * attributes of the Screen3D have changed.	 This method should also be
     * called if the screen changes pixel resolution.
     *
     * @param s3d the Screen3D to update
     */
    public void updateScreen(Screen3D s3d) {
	if (verbose) System.err.println("updateScreen") ;
	ScreenInfo si = (ScreenInfo)screenMap.get(s3d) ;
	if (si != null) si.updateScreen = true ;
    }

    /**
     * Arrange for an update of cached canvas parameters.  If automatic update
     * has not been enabled, then this method should be called if any of the
     * attributes of the Canvas3D have changed.	 These attributes include the
     * canvas position and size, but do <i>not</i> include the attributes of
     * the associated Screen3D, which are cached separately.
     *
     * @param c3d the Canvas3D to update
     */
    public void updateCanvas(Canvas3D c3d) {
	if (verbose) System.err.println("updateCanvas") ;
	CanvasInfo ci = (CanvasInfo)canvasMap.get(c3d) ;
	if (ci != null) ci.updateCanvas = true ;
    }

    /**
     * Arrange for an update of cached view parameters.	 If automatic update
     * has not been enabled for the View, then this method should be called if
     * any of the attributes of the View associated with this object have
     * changed.<p>
     * 
     * These do <i>not</i> include the attributes of the existing Canvas3D or
     * Screen3D components of the View, but do include the attributes of all
     * other components such as the PhysicalEnvironment and PhysicalBody, and
     * all attributes of the attached ViewPlatform except for its
     * <code>localToVworld</code> transform.  The screen and canvas components
     * as well as the ViewPlatform's <code>localToVworld</code> are cached
     * separately.<p>
     *
     * This method should also be called if the ViewPlatform is replaced with
     * another using the View's <code>attachViewPlatform</code> method, or if
     * any of the <code>setCanvas3D</code>, <code>addCanvas3D</code>,
     * <code>insertCanvas3D</code>, <code>removeCanvas3D</code>, or
     * <code>removeAllCanvas3Ds</code> methods of View are called to change
     * the View's canvas list.<p>
     *
     * Calling this method causes most transforms to be re-derived.  It should
     * be used only when necessary.
     */
    public void updateView() {
	if (verbose) System.err.println("updateView") ;
	this.updateView = true ;
    }

    /**
     * Arrange for an update of the cached head position if head tracking is
     * enabled.	 If automatic update has not enabled for the head position,
     * then this method should be called anytime a new head position is to be
     * read.
     */
    public void updateHead() {
	if (verbose) System.err.println("updateHead") ;
	this.updateHead = true ;
    }

    /**
     * Arrange for an update of the cached <code>localToVworld</code>
     * transform of the view platform.	If automatic update has not been
     * enabled for this transform, then this method should be called anytime
     * the view platform has been repositioned in the virtual world and a
     * transform involving virtual world coordinates is desired.<p>
     *
     * The View must be attached to a ViewPlatform which is part of a live
     * scene graph, and the ViewPlatform node must have its
     * <code>ALLOW_LOCAL_TO_VWORLD_READ</code> capability set.
     */
    public void updateViewPlatform() {
	if (verbose) System.err.println("updateViewPlatform") ;
	vpi.updateViewPlatformToVworld = true ;
    }

    //
    // Set cache update bits based on auto update flags.
    // VIEW_AUTO_UPDATE is handled in updateCache().
    //
    private void getAutoUpdate(CanvasInfo ci) {
	if ((autoUpdateFlags & SCREEN_AUTO_UPDATE) != 0)
	    ci.si.updateScreen = true ;

	if ((autoUpdateFlags & CANVAS_AUTO_UPDATE) != 0)
	    ci.updateCanvas = true ;

	if ((autoUpdateFlags & PLATFORM_AUTO_UPDATE) != 0)
	    vpi.updateViewPlatformToVworld = true ;

	if ((autoUpdateFlags & HEAD_AUTO_UPDATE) != 0)
	    this.updateHead = true ;
    }

    //
    // Update any changed cached data.	This takes a Canvas3D instance.	 The
    // cache mechanism could have used a Canvas3D index into the View instead,
    // but the direct reference is probably more convenient for applications.
    //
    private CanvasInfo updateCache(Canvas3D c3d, String name, boolean vworld) {
	if (verbose) {
	    System.err.println("updateCache: " + name + " in " + hashCode()) ;
	    System.err.println("  canvas " + c3d.hashCode()) ;
	}

	// The View may have had Canvas3D instances added or removed, or may
	// have been attached to a different ViewPlatform, so update the view
	// before anything else.
	if (updateView || (autoUpdateFlags & VIEW_AUTO_UPDATE) != 0)
	    getViewInfo() ;

	// Now get the CanvasInfo to update.
	CanvasInfo ci = (CanvasInfo)canvasMap.get(c3d) ;
	if (ci == null)
	    throw new IllegalArgumentException(
                    "Specified Canvas3D is not a component of the View") ;

	// Check rest of autoUpdateFlags.
	if (autoUpdate) getAutoUpdate(ci) ;

	// Update the screen, canvas, view platform, and head caches.
	if (ci.si.updateScreen)
	    ci.si.getScreenInfo() ;

	if (ci.updateCanvas)
	    ci.getCanvasInfo() ;

	if (vworld && vpi.updateViewPlatformToVworld)
	    vpi.getViewPlatformToVworld() ;

	if (useTracking && updateHead)
	    getHeadInfo() ;

	// Return the CanvasInfo instance.
	return ci ;
    }

    //
    // Get physical view parameters and derived data.  This is a fairly
    // heavyweight method -- everything gets marked for update since we don't
    // currently track changes in individual view attributes.  Fortunately
    // there shouldn't be a need to call it very often.
    //
    private void getViewInfo() {
	if (verbose) System.err.println("  getViewInfo") ;

	// Check if an update of the Canvas3D collection is needed. 
	if (this.canvasCount != view.numCanvas3Ds()) {
	    this.canvasCount = view.numCanvas3Ds() ;
	    getCanvases() ;
	}
	else {
	    for (int i = 0 ; i < canvasCount ; i++) {
		if (canvasMap.get(view.getCanvas3D(i)) != canvasInfo[i]) {
		    getCanvases() ;
		    break ;
		}
	    }
	}

	// Update the ViewPlatform.
	getViewPlatform() ;

	// Update the PhysicalBody and PhysicalEnvironment.
	this.body = view.getPhysicalBody() ;
	this.env = view.getPhysicalEnvironment() ;

	// Use the result of the possibly overridden method useHeadTracking()
	// to determine if head tracking is to be used within ViewInfo.
	this.useTracking = useHeadTracking() ;

	// Get the head tracker only if really available.
	if (view.getTrackingEnable() && env.getTrackingAvailable()) {
	    int headIndex = env.getHeadIndex() ;
	    this.headTracker = env.getSensor(headIndex) ;
	}

	// Get the new policies and update data derived from them.
	this.viewPolicy = view.getViewPolicy() ;
	this.projectionPolicy = view.getProjectionPolicy() ;
	this.resizePolicy = view.getWindowResizePolicy() ;
	this.movementPolicy = view.getWindowMovementPolicy() ;
	this.eyePolicy = view.getWindowEyepointPolicy() ;
	this.scalePolicy = view.getScreenScalePolicy() ;
	this.backClipPolicy = view.getBackClipPolicy() ;
	this.frontClipPolicy = view.getFrontClipPolicy() ;

	if (useTracking || viewPolicy == View.HMD_VIEW) {
	    if (this.headToHeadTracker == null)
		this.headToHeadTracker = new Transform3D() ;
	    if (this.headTrackerToTrackerBase == null)
		this.headTrackerToTrackerBase = new Transform3D() ;

	    if (viewPolicy == View.HMD_VIEW) {
		if (this.trackerBaseToHeadTracker == null)
		    this.trackerBaseToHeadTracker = new Transform3D() ;
		if (this.coeToHeadTracker == null)
		    this.coeToHeadTracker = new Transform3D() ;
	    }
	    else {
		if (this.headToTrackerBase == null)
		    this.headToTrackerBase = new Transform3D() ;
	    }
	    
	    body.getLeftEyePosition(this.leftEyeInHead) ;
	    body.getRightEyePosition(this.rightEyeInHead) ;
	    body.getHeadToHeadTracker(this.headToHeadTracker) ;

	    if (verbose) {
		System.err.println("    leftEyeInHead  " + leftEyeInHead) ;
		System.err.println("    rightEyeInHead " + rightEyeInHead) ;
		t3dPrint(headToHeadTracker, "    headToHeadTracker") ;
	    }
	}

	if (eyePolicy == View.RELATIVE_TO_WINDOW ||
	    eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW) {
	    body.getLeftEyePosition(this.leftEyeInHead) ;
	    body.getRightEyePosition(this.rightEyeInHead) ;
	    if (verbose) {
		System.err.println("    leftEyeInHead  " + leftEyeInHead) ;
		System.err.println("    rightEyeInHead " + rightEyeInHead) ;
	    }
	}

	if ((env.getCoexistenceCenterInPworldPolicy() !=
	     View.NOMINAL_SCREEN) || (viewPolicy == View.HMD_VIEW))
	    this.coeCentering = false ;
	else
	    this.coeCentering = view.getCoexistenceCenteringEnable() ;

	if (!coeCentering || useTracking) {
	    if (this.coeToTrackerBase == null)
		this.coeToTrackerBase = new Transform3D() ;

	    env.getCoexistenceToTrackerBase(this.coeToTrackerBase) ;
	    if (verbose) t3dPrint(coeToTrackerBase, "    coeToTrackerBase") ;
	}

	if (backClipPolicy  == View.VIRTUAL_EYE ||
	    backClipPolicy  == View.VIRTUAL_SCREEN ||
	    frontClipPolicy == View.VIRTUAL_EYE ||
	    frontClipPolicy == View.VIRTUAL_SCREEN) {
	    this.clipVirtual = true ;
	}
	else {
	    this.clipVirtual = false ;
	}

	// Propagate view updates to each canvas.
	for (int i = 0 ; i < canvasCount ; i++) 
	    this.canvasInfo[i].updateViewDependencies() ;

	this.updateView = false ;
	if (verbose) {
	    System.err.println("    tracking " + useTracking) ;
	    System.err.println("    coeCentering " + coeCentering) ;
	    System.err.println("    clipVirtual " + clipVirtual) ;
	}
    }

    // 
    // Each view can have multiple canvases, each with an associated screen.
    // Each canvas is associated with only one view.  Each screen can have
    // multiple canvases that are used across multiple views.  We rebuild the
    // canvas info instead of trying to figure out what canvases have been
    // added or removed from the view.
    // 
    private void getCanvases() {
	if (this.canvasInfo.length < canvasCount) {
	    this.canvasInfo = new CanvasInfo[canvasCount] ;
	}

	for (int i = 0 ; i < canvasCount ; i++) {
	    Canvas3D c3d = view.getCanvas3D(i) ;
	    Screen3D s3d = c3d.getScreen3D() ;

	    // Check if we have a new screen.
	    ScreenInfo si = (ScreenInfo)screenMap.get(s3d) ;
	    if (si == null) {
		si = new ScreenInfo(s3d, c3d.getGraphicsConfiguration()) ;
		screenMap.put(s3d, si) ;
	    }

	    // Check to see if we've encountered the screen so far in this
	    // loop over the view's canvases.  If not, clear the screen's list
	    // of canvases for this ViewInfo.
	    if (newSet.add(si)) si.clear(this) ;

	    // Check if this is a new canvas.
	    CanvasInfo ci = (CanvasInfo)canvasMap.get(c3d) ;
	    if (ci == null) ci = new CanvasInfo(c3d, si) ;

	    // Add this canvas to the screen's list for this ViewInfo.
	    si.addCanvasInfo(this, ci) ;

	    // Add this canvas to the new canvas map and canvas array.
	    this.newMap.put(c3d, ci) ;
	    this.canvasInfo[i] = ci ;
	}

	// Null out old references if canvas count shrinks.
	for (int i = canvasCount ; i < canvasInfo.length ; i++)
	    this.canvasInfo[i] = null ;

	// Update the CanvasInfo map.
	Map tmp = canvasMap ;
	this.canvasMap = newMap ;
	this.newMap = tmp ;

	// Clear the temporary collections.
	this.newMap.clear() ;
	this.newSet.clear() ;
    }

    //
    // Force the creation of new CanvasInfo instances.  This is called when a
    // screen is removed from the screen map.
    //
    private void clearCanvases() {
	this.canvasCount = 0 ;
	this.canvasMap.clear() ;
	this.updateView = true ;
    }

    // 
    // Update the view platform. Each view can be attached to only one, but
    // each view platform can have many views attached.
    // 
    private void getViewPlatform() {
	ViewPlatform vp = view.getViewPlatform() ;
	if (vp == null)
	    throw new IllegalStateException
		("The View must be attached to a ViewPlatform") ;

	ViewPlatformInfo tmpVpi =
	    (ViewPlatformInfo)viewPlatformMap.get(vp) ;

	if (tmpVpi == null) {
	    // We haven't encountered this ViewPlatform before.
	    tmpVpi = new ViewPlatformInfo(vp) ;
	    viewPlatformMap.put(vp, tmpVpi) ;
	}

	if (this.vpi != tmpVpi) {
	    // ViewPlatform has changed.  Could set an update flag here if it
	    // would be used, but updating the view updates everything anyway.
	    if (this.vpi != null) {
		// Remove this ViewInfo from the list of Views attached to the
		// old ViewPlatform.
		this.vpi.removeViewInfo(this) ;
	    }
	    this.vpi = tmpVpi ;
	    this.vpi.addViewInfo(this) ;

	    // updateViewPlatformToVworld is initially set false since the
	    // capability to read the vworld transform may not be
	    // available. If it is, set it here.
	    if (vp.getCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ)) {
		this.vpi.updateViewPlatformToVworld = true ;
		if (verbose) System.err.println("    vworld read allowed") ;
	    } else
		if (verbose) System.err.println("    vworld read disallowed") ;
	}
    }

    //
    // Force the creation of a new ViewPlatformInfo when a view platform is
    // removed from the view platform map.
    //
    private void clearViewPlatform() {
	this.updateView = true ;
    }

    //
    // Update vworld dependencies for this ViewInfo -- called by
    // ViewPlatformInfo.getViewPlatformToVworld().
    //
    private void updateVworldDependencies() {
	for (int i = 0 ; i < canvasCount ; i++)
	    this.canvasInfo[i].updateVworldDependencies() ;
    }

    /**
     * Returns a reference to a Transform3D containing the current transform
     * from head tracker coordinates to tracker base coordinates.  It is only
     * called if <code>useHeadTracking</code> returns true and a head position
     * update is specified with <code>updateHead</code> or the
     * <code>HEAD_AUTO_UPDATE</code> constructor flag.<p>
     *
     * The default implementation uses the head tracking sensor specified by
     * the View's PhysicalEnvironment, and reads it by calling the sensor's
     * <code>getRead</code> method directly.  The result is a sensor reading
     * that may have been taken at a slightly different time from the one used
     * by the renderer.  This method can be overridden to synchronize the two
     * readings through an external mechanism.
     *
     * @return current head tracker to tracker base transform
     * @see #useHeadTracking
     * @see #updateHead
     * @see #HEAD_AUTO_UPDATE
     */
    protected Transform3D getHeadTrackerToTrackerBase() {
	headTracker.getRead(this.headTrackerToTrackerBase) ;
	return this.headTrackerToTrackerBase ;
    }

    /**
     * Returns <code>true</code> if head tracking should be used.<p>
     *
     * The default implementation returns <code>true</code> if the View's
     * <code>getTrackingEnable</code> method and the PhysicalEnvironment's
     * <code>getTrackingAvailable</code> method both return <code>true</code>.
     * These are the same conditions under which the Java 3D renderer uses
     * head tracking.  This method can be overridden if there is any need to
     * decouple the head tracking status of ViewInfo from the renderer.
     * 
     * @return <code>true</code> if ViewInfo should use head tracking
     */
    protected boolean useHeadTracking() {
	return view.getTrackingEnable() && env.getTrackingAvailable() ;
    }

    //
    // Cache the current tracked head position and derived data.
    // 
    private void getHeadInfo() {
	if (verbose) System.err.println("  getHeadInfo") ;

	this.headTrackerToTrackerBase = getHeadTrackerToTrackerBase() ;
	if (viewPolicy == View.HMD_VIEW) {
	    this.trackerBaseToHeadTracker.invert(headTrackerToTrackerBase) ;
	    this.coeToHeadTracker.mul(trackerBaseToHeadTracker,
				      coeToTrackerBase) ;
	}
	else {
	    this.headToTrackerBase.mul(headTrackerToTrackerBase,
				       headToHeadTracker) ;
	}
	for (int i = 0 ; i < canvasCount ; i++)
	    this.canvasInfo[i].updateHeadDependencies() ;

	this.updateHead = false ;
	// 
	// The head position used by the Java 3D renderer isn't accessible
	// in the public API.  A head tracker generates continuous data, so
	// getting the same sensor read as the renderer is unlikely.
	// 
	// Possible workaround: for fixed screens, get the Java 3D
	// renderer's version of plateToVworld and headToVworld by calling
	// Canvas3D.getImagePlateToVworld() and View.getUserHeadToVworld().
	// Although the vworld components will have frame latency, they can
	// be cancelled out by inverting the former transform and
	// multiplying by the latter, resulting in userHeadToImagePlate,
	// which can then be transformed to tracker base coordinates.
	//
	// For head mounted displays, the head to image plate transforms are
	// just calibration constants, so they're of no use.  There are more
	// involved workarounds possible, but one that may work for both fixed
	// screens and HMD is to define a SensorInterposer class that extends
	// Sensor.  Take the View's head tracking sensor, use it to construct
	// a SensorInterposer, and then replace the head tracking sensor with
	// the SensorInterposer.  SensorInterposer can then override the
	// getRead() methods and thus control what the Java 3D renderer gets.
	// getHeadTrackerToTrackerBase() is a protected method in ViewInfo
	// which can be overridden to call a variant of getRead() so that
	// calls from ViewInfo and from the renderer can be distinguished.
	//
	// Even if getting the same head position as used by the renderer is
	// achieved, tracked eye space interactions with objects in the
	// virtual world still can't be synchronized with rendering. This
	// means that objects in the virtual world cannot be made to appear in
	// a fixed position relative to the tracked head position without a
	// frame lag between them.
	// 
	// The reason for this is that the tracked head position used by the
	// Java 3D renderer is updated asynchronously from scene graph
	// updates.  This is done to reduce latency between the user's
	// position and the rendered image, which is directly related to the
	// quality of the immersive virtual reality experience.	 So while an
	// update to the scene graph may have a frame latency before it gets
	// rendered, a change to the user's tracked position is always
	// reflected in the current frame.
	// 
	// This problem can't be fixed without eliminating the frame latency
	// in the Java 3D internal state, although there are possible
	// workarounds at the expense of increased user position latency.
	// These involve disabling tracking, reading the head sensor directly,
	// performing whatever eye space interactions are necessary with the
	// virtual world (using the view platform's current localToVworld),
	// and then propagating the head position change to the renderer
	// manually through a behavior post mechanism that delays it by a
	// frame.
	//
	// For example, with head tracking in a fixed screen environment (such
	// as a CAVE), disable Java 3D head tracking and set the View's window
	// eyepoint policy to RELATIVE_TO_COEXISTENCE.	Read the sensor to get
	// the head position relative to the tracker base, transform it to
	// coexistence coordinates using the inverse of the value of the
	// coexistenceToTrackerBase transform, and then set the eye positions
	// manually with the View's set{Left,Right}ManualEyeInCoexistence
	// methods.  If these method calls are delayed through a behavior post
	// mechanism, then they will be synchronized with the rendering of the
	// scene graph updates.
	//
	// With a head mounted display the sensor can be read directly to get
	// the head position relative to the tracker base.  If Java 3D's head
	// tracking is disabled, it uses identity for the current
	// headTrackerToTrackerBase transform.	It concatenates its inverse,
	// trackerBaseToHeadTracker, with coexistenceToTrackerBase to get the
	// image plate positions in coexistence; the former transform is
	// inaccessible, but the latter can be set through the
	// PhysicalEnvironment.	 So the workaround is to maintain a local copy
	// with the real value of coexistenceToTrackerBase, but set the
	// PhysicalEnvironment copy to the product of the real value and the
	// trackerBaseToHeadTracker inverted from the sensor read.  Like the
	// CAVE example, this update to the View would have to be delayed in
	// order to synchronize with scene graph updates.
	//
	// Another possibility is to put the Java 3D view model in
	// compatibility mode, where it accepts vpcToEye and eyeToCc
	// (projection) directly.  The various view attributes can still be
	// set and accessed, but will be ignored by the Java 3D view model.
	// The ViewInfo methods can be used to compute the view and projection
	// matrices, which can then be delayed to synchronize with the scene
	// graph.
	// 
	// Note that these workarounds could be used to make view-dependent
	// scene graph updates consistent, but they still can't do anything
	// about synchronizing the actual physical position of the user with
	// the rendered images.	 That requires zero latency between position
	// update and scene graph state.
	//
	// Still another possibility: extrapolate the position of the user
	// into the next few frames from a sample of recently recorded
	// positions.  Unfortunately, that is also a very hard problem.  The
	// Java 3D Sensor API is designed to support prediction but it was
	// never realized successfully in the sample implementation.
    }

    //
    // A per-screen cache, shared between ViewInfo instances.  In the Java 3D
    // view model a single screen can be associated with multiple canvas
    // and view instances.
    //
    private static class ScreenInfo {
	private Screen3D s3d = null ;
	private GraphicsConfiguration graphicsConfiguration = null ;
	private boolean updateScreen = true ;

	private Map viewInfoMap = new HashMap() ;
	private List viewInfoList = new LinkedList() ; 
	private Transform3D t3d = new Transform3D() ;

	private double screenWidth = 0.0 ;
	private double screenHeight = 0.0 ;
	private boolean updateScreenSize = true ;

	private Rectangle screenBounds = null ;
	private double metersPerPixelX = 0.0 ;
	private double metersPerPixelY = 0.0 ;
	private boolean updatePixelSize = true ;

	// These transforms are pre-allocated here since they are required by
	// some view policies and we don't know what views this screen will be
	// attached to.  Their default identity values are used if not
	// explicitly set. TODO: allocate if needed in getCanvasInfo(), where
	// view information will be available.
	private Transform3D trackerBaseToPlate = new Transform3D() ;
	private Transform3D headTrackerToLeftPlate = new Transform3D() ;
	private Transform3D headTrackerToRightPlate = new Transform3D() ;
	private boolean updateTrackerBaseToPlate = false ;
	private boolean updateHeadTrackerToPlate = false ;

	private ScreenInfo(Screen3D s3d, GraphicsConfiguration gc) {
	    this.s3d = s3d ;
	    this.graphicsConfiguration = gc ;
	    if (verbose)
		System.err.println("    ScreenInfo: init " + s3d.hashCode()) ;
	}

	private List getCanvasList(ViewInfo vi) {
	    List canvasList = (List)viewInfoMap.get(vi) ;
	    if (canvasList == null) {
		canvasList = new LinkedList() ;
		viewInfoMap.put(vi, canvasList) ;
		viewInfoList.add(canvasList) ;
	    }
	    return canvasList ;
	}

	private synchronized void clear(ViewInfo vi) {
	    getCanvasList(vi).clear() ;
	}

	private synchronized void clear() {
	    Iterator i = viewInfoMap.keySet().iterator() ;
	    while (i.hasNext()) ((ViewInfo)i.next()).clearCanvases() ;
	    viewInfoMap.clear() ;

	    i = viewInfoList.iterator() ;
	    while (i.hasNext()) ((List)i.next()).clear() ;
	    viewInfoList.clear() ;
	}

	private synchronized void addCanvasInfo(ViewInfo vi, CanvasInfo ci) {
	    getCanvasList(vi).add(ci) ;
	}

	// 
	// Get all relevant screen information, find out what changed, and
	// flag derived data.  With normal use it's unlikely that any of the
	// Screen3D attributes will change after the first time this method is
	// called. It's possible that the screen resolution changed or some
	// sort of interactive screen calibration is in process.
	//
	private synchronized void getScreenInfo() {
	    if (verbose)
		System.err.println("  getScreenInfo " + s3d.hashCode());

	    // This is used for positioning screens in relation to each other
	    // and must be accurate for good results with multi-screen
	    // displays.  By default the coexistence to tracker base transform
	    // is identity so in that case this transform will also set the
	    // image plate in coexistence coordinates.
	    s3d.getTrackerBaseToImagePlate(t3d) ;
	    if (! t3d.equals(trackerBaseToPlate)) {
		this.trackerBaseToPlate.set(t3d) ;
		this.updateTrackerBaseToPlate = true ;
		if (verbose) t3dPrint(trackerBaseToPlate,
				      "    trackerBaseToPlate") ;
	    }

	    // This transform and the following are used for head mounted
	    // displays.  They should be based on the *apparent* position of
	    // the screens as viewed through the HMD optics.
	    s3d.getHeadTrackerToLeftImagePlate(t3d) ;
	    if (! t3d.equals(headTrackerToLeftPlate)) {
		this.headTrackerToLeftPlate.set(t3d) ;
		this.updateHeadTrackerToPlate = true ;
		if (verbose) t3dPrint(headTrackerToLeftPlate,
				      "    headTrackerToLeftPlate") ;
	    }

	    s3d.getHeadTrackerToRightImagePlate(t3d) ;
	    if (! t3d.equals(headTrackerToRightPlate)) {
		this.headTrackerToRightPlate.set(t3d) ;
		this.updateHeadTrackerToPlate = true ;
		if (verbose) t3dPrint(headTrackerToRightPlate,
				      "    headTrackerToRightPlate") ;
	    }

	    // If the screen width and height in meters are not explicitly set
	    // through the Screen3D, then the Screen3D will assume a pixel
	    // resolution of 90 pixels/inch and compute the dimensions from
	    // the screen resolution.  These dimensions should be measured
	    // accurately for multi-screen displays.  For HMD, these
	    // dimensions should be the *apparent* width and height as viewed
	    // through the HMD optics.
	    double w = s3d.getPhysicalScreenWidth() ;
	    double h = s3d.getPhysicalScreenHeight();
	    if (w != screenWidth || h != screenHeight) {
		this.screenWidth  = w ;
		this.screenHeight = h ;
		this.updateScreenSize = true ;
		if (verbose) {
		    System.err.println("    screen width  " + screenWidth) ;
		    System.err.println("    screen height " + screenHeight) ;
		}
	    }

            GraphicsConfiguration gc1 = graphicsConfiguration;
            // Workaround for Issue 316 - use the default config for screen 0
            // if the graphics config is null
            if (gc1 == null) {
                gc1 = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration();
            }
            this.screenBounds = gc1.getBounds() ;
	    double mpx = screenWidth  / (double)screenBounds.width ;
	    double mpy = screenHeight / (double)screenBounds.height ;
	    if ((mpx != metersPerPixelX) || (mpy != metersPerPixelY)) {
		this.metersPerPixelX = mpx ;
		this.metersPerPixelY = mpy ;
		this.updatePixelSize = true ;
		if (verbose) {
		    System.err.println("    screen bounds " + screenBounds) ;
		    System.err.println("    pixel size X " + metersPerPixelX) ;
		    System.err.println("    pixel size Y " + metersPerPixelY) ;
		}
	    }

	    // Propagate screen updates to each canvas in each ViewInfo.
	    Iterator vi = viewInfoList.iterator() ;
	    while (vi.hasNext()) {
		Iterator ci = ((List)vi.next()).iterator() ;
		while (ci.hasNext())
		    ((CanvasInfo)ci.next()).updateScreenDependencies() ;
	    }

	    this.updateTrackerBaseToPlate = false ;
	    this.updateHeadTrackerToPlate = false ;
	    this.updateScreenSize = false ;
	    this.updatePixelSize = false ;
	    this.updateScreen = false ;
	}
    }

    //
    // A per-ViewPlatform cache, shared between ViewInfo instances.  In the
    // Java 3D view model, a view platform may have several views attached to
    // it.  The only view platform data cached here is its localToVworld, the
    // inverse of its localToVworld, and the scale from vworld to view
    // platform coordinates.  The view platform to coexistence transform is
    // cached by the CanvasInfo instances associated with the ViewInfo.
    //
    private static class ViewPlatformInfo {
	private ViewPlatform vp = null ;
	private List viewInfo = new LinkedList() ;
	private double[] m = new double[16] ;

	// These transforms are pre-allocated since we don't know what views
	// will be attached.  Their default identity values are used if a
	// vworld dependent computation is requested and no initial update of
	// the view platform was performed; this occurs if the local to vworld
	// read capability isn't set.  TODO: rationalize this and allocate
	// only if necessary.
	private Transform3D viewPlatformToVworld = new Transform3D() ;
	private Transform3D vworldToViewPlatform = new Transform3D() ;
	private double vworldToViewPlatformScale = 1.0 ;

	// Set these update flags initially false since we might not have the
	// capability to read the vworld transform.
	private boolean updateViewPlatformToVworld = false ;
	private boolean updateVworldScale = false ;

	private ViewPlatformInfo(ViewPlatform vp) {
	    this.vp = vp ;
	    if (verbose) System.err.println
			     ("    ViewPlatformInfo: init " + vp.hashCode()) ;
	}

	private synchronized void addViewInfo(ViewInfo vi) {
	    this.viewInfo.add(vi) ;
	}

	private synchronized void removeViewInfo(ViewInfo vi) {
	    this.viewInfo.remove(vi) ;
	}

	private synchronized void clear() {
	    Iterator i = viewInfo.iterator() ;
	    while (i.hasNext()) ((ViewInfo)i.next()).clearViewPlatform() ;
	    viewInfo.clear() ;
	}

	//
	// Get the view platform's current <code>localToVworld</code> and
	// force the update of derived data.
	//
	private synchronized void getViewPlatformToVworld() {
	    if (verbose) System.err.println
			     ("  getViewPlatformToVworld " + vp.hashCode()) ;

	    vp.getLocalToVworld(this.viewPlatformToVworld) ;
	    this.vworldToViewPlatform.invert(viewPlatformToVworld) ;
	
	    // Get the scale factor from the virtual world to view platform
	    // transform.  Note that this is always a congruent transform. 
	    vworldToViewPlatform.get(m) ;
	    double newScale = Math.sqrt(m[0]*m[0] + m[1]*m[1] + m[2]*m[2]) ;

	    // May need to update clip plane distances if scale changed.  We'll
	    // check with an epsilon commensurate with single precision float.
	    // It would be more efficient to check the square of the distance
	    // and then compute the square root only if different, but that
	    // makes choosing an epsilon difficult.
	    if ((newScale > vworldToViewPlatformScale + 0.0000001) ||
		(newScale < vworldToViewPlatformScale - 0.0000001)) {
		this.vworldToViewPlatformScale = newScale ;
		this.updateVworldScale = true ;
		if (verbose) System.err.println("    vworld scale " +
						vworldToViewPlatformScale) ;
	    }

	    // All virtual world transforms must be updated.
	    Iterator i = viewInfo.iterator() ;
	    while (i.hasNext())
		((ViewInfo)i.next()).updateVworldDependencies() ;

	    this.updateVworldScale = false ;
	    this.updateViewPlatformToVworld = false ;
	}
    }

    //
    // A per-canvas cache.
    // 
    private class CanvasInfo {
	private Canvas3D c3d = null ;
	private ScreenInfo si = null ;
	private boolean updateCanvas = true ;

	private double canvasX = 0.0 ;
	private double canvasY = 0.0 ;
	private boolean updatePosition = true ;

	private double canvasWidth = 0.0 ;
	private double canvasHeight = 0.0 ;
	private double windowScale = 0.0 ;
	private boolean updateWindowScale = true ;

	private double screenScale = 0.0 ;
	private boolean updateScreenScale = true ;

	private boolean useStereo = false ;
	private boolean updateStereo = true ;

	//
	// coeToPlate is the same for each Canvas3D in a Screen3D unless
	// coexistence centering is enabled and the window movement policy is
	// PHYSICAL_WORLD.
	// 
	private Transform3D coeToPlate = null ;
	private Transform3D coeToRightPlate = null ;
	private boolean updateCoeToPlate = true ;

	//
	// viewPlatformToCoe is the same for each Canvas3D in a View unless
	// the window resize policy is PHYSICAL_WORLD, in which case the scale
	// factor includes the window scale; or if the screen scale policy is
	// SCALE_SCREEN_SIZE, in which case the scale factor depends upon the
	// width of the screen associated with the canvas; or if the window
	// eyepoint policy is RELATIVE_TO_FIELD_OF_VIEW and the view attach
	// policy is not NOMINAL_SCREEN, which will set the view platform
	// origin in coexistence based on the width of the canvas.
	// 
	private Transform3D viewPlatformToCoe = null ;
	private Transform3D coeToViewPlatform = null ;
	private boolean updateViewPlatformToCoe = true ;
	private boolean updateCoeToViewPlatform = true ;

	//
	// plateToViewPlatform is composed from viewPlatformToCoe and
	// coeToPlate.
	//
	private Transform3D plateToViewPlatform = null ;
	private Transform3D rightPlateToViewPlatform = null ;
	private boolean updatePlateToViewPlatform = true ;

	//
	// trackerBaseToViewPlatform is computed from viewPlatformToCoe and
	// coeToTrackerBase.
	//
	private Transform3D trackerBaseToViewPlatform = null ;
	private boolean updateTrackerBaseToViewPlatform = true ;

	//
	// Eye position in image plate is always different for each Canvas3D
	// in a View, unless two or more Canvas3D instances are the same
	// position and size, or two or more Canvas3D instances are using a
	// window eyepoint policy of RELATIVE_TO_SCREEN and have the same
	// settings for the manual eye positions.
	//
	private Point3d eyeInPlate = new Point3d() ;
	private Point3d rightEyeInPlate = new Point3d() ;
	private Transform3D eyeToPlate = null ;
	private Transform3D rightEyeToPlate = null ;
	private boolean updateEyeInPlate = true ;

	private Point3d leftManualEyeInPlate = new Point3d() ;
	private Point3d rightManualEyeInPlate = new Point3d() ;
	private boolean updateManualEye = true ;

	private int monoscopicPolicy = -1 ;
	private boolean updateMonoPolicy = true ;

	//
	// eyeToViewPlatform is computed from eyeToPlate and
	// plateToViewPlatform.
	//
	private Transform3D eyeToViewPlatform = null ;
	private Transform3D rightEyeToViewPlatform = null ;
	private boolean updateEyeToViewPlatform = true ;

	private Transform3D viewPlatformToEye = null ;
	private Transform3D viewPlatformToRightEye = null ;
	private boolean updateViewPlatformToEye = true ;

	//
	// The projection transform depends upon eye position in image plate.
	// 
	private Transform3D projection = null ;
	private Transform3D rightProjection = null ;
	private boolean updateProjection = true ;

	private Transform3D inverseProjection = null ;
	private Transform3D inverseRightProjection = null ;
	private boolean updateInverseProjection = true ;

	private Transform3D inverseViewPlatformProjection = null ;
	private Transform3D inverseViewPlatformRightProjection = null ;
	private boolean updateInverseViewPlatformProjection = true ;

	//
	// The physical clip distances can be affected by the canvas width
	// with the PHYSICAL_WORLD resize policy.
	//
	private double frontClipDistance = 0.0 ;
	private double backClipDistance = 0.0 ;
	private boolean updateClipDistances = true ;

	//
	// The physical to view platform scale can be affected by the canvas
	// width with the PHYSICAL_WORLD resize policy.
	//
	private double physicalToVpScale = 0.0 ;
	private double physicalToVirtualScale = 0.0 ;
	private boolean updatePhysicalToVpScale = true ;
	private boolean updatePhysicalToVirtualScale = true ;

	//
	// The vworld transforms require reading the ViewPlaform's
	// localToVworld tranform.
	//
	private Transform3D plateToVworld = null ;
	private Transform3D rightPlateToVworld = null ;
	private boolean updatePlateToVworld = true ;

	private Transform3D coeToVworld = null ;
	private boolean updateCoeToVworld = true ;

	private Transform3D eyeToVworld = null ;
	private Transform3D rightEyeToVworld = null ;
	private boolean updateEyeToVworld = true ;

	private Transform3D trackerBaseToVworld = null ;
	private boolean updateTrackerBaseToVworld = true ;

	private Transform3D inverseVworldProjection = null ;
	private Transform3D inverseVworldRightProjection = null ;
	private boolean updateInverseVworldProjection = true ;

	private CanvasInfo(Canvas3D c3d, ScreenInfo si) {
	    this.si = si ;
	    this.c3d = c3d ;
	    if (verbose) System.err.println("    CanvasInfo: init " +
					    c3d.hashCode()) ;
	}

	private void getCanvasInfo() {
	    if (verbose) System.err.println("  getCanvasInfo " +
					    c3d.hashCode()) ;
	    boolean newStereo =
		c3d.getStereoEnable() && c3d.getStereoAvailable() ;

	    if (useStereo != newStereo) {
		this.useStereo = newStereo ;
		this.updateStereo = true ;
		if (verbose) System.err.println("    stereo " + useStereo) ;
	    }

	    this.canvasWidth  = c3d.getWidth()  * si.metersPerPixelX ;
	    this.canvasHeight = c3d.getHeight() * si.metersPerPixelY ;
	    double newScale = canvasWidth / si.screenWidth ;

	    if (windowScale != newScale) {
		this.windowScale = newScale ;
		this.updateWindowScale = true ;
		if (verbose) {
		    System.err.println("    width  " + canvasWidth) ;
		    System.err.println("    height " + canvasHeight) ;
		    System.err.println("    scale  " + windowScale) ;
		}
	    }
	    
	    // For multiple physical screens, AWT returns the canvas location
	    // relative to the origin of the aggregated virtual screen.	 We
	    // need the location relative to the physical screen origin.
	    Point awtLocation = c3d.getLocationOnScreen() ;
	    int x = awtLocation.x - si.screenBounds.x ;
	    int y = awtLocation.y - si.screenBounds.y ;

	    double newCanvasX = si.metersPerPixelX * x ;
	    double newCanvasY = si.metersPerPixelY *
		(si.screenBounds.height - (y + c3d.getHeight())) ;

	    if (canvasX != newCanvasX || canvasY != newCanvasY) {
		this.canvasX = newCanvasX ;
		this.canvasY = newCanvasY ;
		this.updatePosition = true ;
		if (verbose) {
		    System.err.println("    lower left X " + canvasX) ;
		    System.err.println("    lower left Y " + canvasY) ;
		}
	    }

	    int newMonoPolicy = c3d.getMonoscopicViewPolicy() ;
	    if (monoscopicPolicy != newMonoPolicy) {
		this.monoscopicPolicy = newMonoPolicy ;
		this.updateMonoPolicy = true ;

		if (verbose && !useStereo) {
		    if (monoscopicPolicy == View.LEFT_EYE_VIEW)
			System.err.println("    left eye view") ;
		    else if (monoscopicPolicy == View.RIGHT_EYE_VIEW)
			System.err.println("    right eye view") ;
		    else
			System.err.println("    cyclopean view") ;
		}
	    }

	    c3d.getLeftManualEyeInImagePlate(leftEye) ;
	    c3d.getRightManualEyeInImagePlate(rightEye) ;

	    if (!leftEye.equals(leftManualEyeInPlate) ||
		!rightEye.equals(rightManualEyeInPlate)) {

		this.leftManualEyeInPlate.set(leftEye) ;
		this.rightManualEyeInPlate.set(rightEye) ;
		this.updateManualEye = true ;

		if (verbose && (eyePolicy == View.RELATIVE_TO_WINDOW ||
				eyePolicy == View.RELATIVE_TO_SCREEN)) {
		    System.err.println("    left manual eye in plate  " +
				       leftManualEyeInPlate) ;
		    System.err.println("    right manual eye in plate " +
				       rightManualEyeInPlate) ;
		}
	    }
		
	    updateCanvasDependencies() ;
	    this.updateStereo = false ;
	    this.updateWindowScale = false ;
	    this.updatePosition = false ;
	    this.updateMonoPolicy = false ;
	    this.updateManualEye = false ;
	    this.updateCanvas = false ;
	}

	private double getFieldOfViewOffset() {
	    return 0.5 * canvasWidth / Math.tan(0.5 * view.getFieldOfView()) ;
	}

	private void updateScreenDependencies() {
	    if (si.updatePixelSize || si.updateScreenSize) {
		if (eyePolicy == View.RELATIVE_TO_WINDOW ||
		    eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW) {
		    // Physical location of the canvas might change without
		    // changing the pixel location.
		    updateEyeInPlate = true ;
		}
		if (resizePolicy == View.PHYSICAL_WORLD ||
		    eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW) {
		    // Could change the window scale or view platform Z offset.
		    updateViewPlatformToCoe = true ;
		}
		if (resizePolicy == View.PHYSICAL_WORLD) {
		    // Window scale affects the clip distance and the physical
		    // to viewplatform scale.
		    updateClipDistances = true ;
		    updatePhysicalToVpScale = true ;
		    updatePhysicalToVirtualScale = true ;
		}
		// Upper right corner of canvas may have moved from eye.
		updateProjection = true ;
	    }
	    if (si.updateScreenSize && scalePolicy == View.SCALE_SCREEN_SIZE) {
		// Screen scale affects the clip distances and physical to
		// view platform scale.  The screen scale is also a component
		// of viewPlatformToCoe.
		updateScreenScale = true ;
		updateClipDistances = true ;
		updatePhysicalToVpScale = true ;
		updatePhysicalToVirtualScale = true ;
		updateViewPlatformToCoe = true ;
	    }

	    if (viewPolicy == View.HMD_VIEW) {
		if (si.updateHeadTrackerToPlate) {
		    // Plate moves with respect to the eye and coexistence.
		    updateEyeInPlate = true ;
		    updateCoeToPlate = true ;
		}
	    }
	    else if (coeCentering) {
		if (movementPolicy == View.PHYSICAL_WORLD) {
		    // Coexistence is centered on the canvas.
		    if (si.updatePixelSize || si.updateScreenSize)
			// Physical location of the canvas might change
			// without changing the pixel location.
			updateCoeToPlate = true ;
		}
		else if (si.updateScreenSize)
		    // Coexistence is centered on the screen.
		    updateCoeToPlate = true ;
	    }
	    else if (si.updateTrackerBaseToPlate) {
		// Image plate has possibly changed location.  Could be
		// offset by an update to coeToTrackerBase in the
		// PhysicalEnvironment though.
		updateCoeToPlate = true ;
	    }

	    if (updateCoeToPlate &&
		eyePolicy == View.RELATIVE_TO_COEXISTENCE) {
		// Coexistence has moved with respect to plate.
		updateEyeInPlate = true ;
	    }
	    if (updateViewPlatformToCoe) {
		// Derived transforms.  trackerBaseToViewPlatform is composed
		// from viewPlatformToCoe and coexistenceToTrackerBase.
		updateCoeToViewPlatform = true ;
		updateCoeToVworld = true ;
		updateTrackerBaseToViewPlatform = true ;
		updateTrackerBaseToVworld = true ;
	    }
	    if (updateCoeToPlate || updateViewPlatformToCoe) {
		// The image plate to view platform transform is composed from
		// the coexistence to image plate and view platform to
		// coexistence transforms, so these need updates as well.
		updatePlateToViewPlatform = true ;
		updatePlateToVworld = true ;
	    }
	    updateEyeDependencies() ;
	}

	private void updateEyeDependencies() {
            if (updateEyeInPlate) {
                updateEyeToVworld = true ;
                updateProjection = true ;
            }
            if (updateProjection) {
                updateInverseProjection = true ;
		updateInverseViewPlatformProjection = true ;
                updateInverseVworldProjection = true ;
            }
            if (updateEyeInPlate || updatePlateToViewPlatform) {
                updateViewPlatformToEye = true ;
                updateEyeToViewPlatform = true ;
            }
	}

	private void updateCanvasDependencies() {
	    if (updateStereo || updateMonoPolicy ||
		(updateManualEye && (eyePolicy == View.RELATIVE_TO_WINDOW ||
				     eyePolicy == View.RELATIVE_TO_SCREEN))) {
		updateEyeInPlate = true ;
	    }
	    if (updateWindowScale || updatePosition) {
		if (coeCentering && movementPolicy == View.PHYSICAL_WORLD) {
		    // Coexistence is centered on the canvas.
		    updateCoeToPlate = true ;
		    if (eyePolicy == View.RELATIVE_TO_COEXISTENCE)
			updateEyeInPlate = true ;
		}
		if (eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW ||
		    eyePolicy == View.RELATIVE_TO_WINDOW)
		    // Eye depends on canvas position and size.
		    updateEyeInPlate = true ;
	    }
	    if (updateWindowScale) {
		if (resizePolicy == View.PHYSICAL_WORLD ||
		    eyePolicy == View.RELATIVE_TO_FIELD_OF_VIEW) {
		    // View platform scale and its origin Z offset changed.
		    // trackerBaseToViewPlatform needs viewPlatformToCoe.
		    updateViewPlatformToCoe = true ;
		    updateCoeToViewPlatform = true ;
		    updateCoeToVworld = true ;
		    updateTrackerBaseToViewPlatform = true ;
		    updateTrackerBaseToVworld = true ;
		}
		if (resizePolicy == View.PHYSICAL_WORLD) {
		    // Clip distance and physical to view platform scale are
		    // affected by the window size.
		    updateClipDistances = true ;
		    updateProjection = true ;
		    updatePhysicalToVpScale = true ;
		    updatePhysicalToVirtualScale = true ;
		}
	    }
	    if (updateViewPlatformToCoe || updateCoeToPlate) {
		// The image plate to view platform transform is composed from
		// the coexistence to image plate and the view platform to
		// coexistence transforms, so these need updates.
		updatePlateToViewPlatform = true ;
		updatePlateToVworld = true ;
	    }
	    if (coeCentering && !updateManualEye && !updateWindowScale &&
		(movementPolicy == View.PHYSICAL_WORLD) &&
		(eyePolicy != View.RELATIVE_TO_SCREEN)) {
		// The canvas may have moved, but the eye, coexistence, and
		// view platform moved with it.  updateEyeDependencies()
		// isn't called since it would unnecessarily update the
		// projection and eyeToViewPlatform transforms.  The tested
		// policies are all true by default.
		return ;
	    }
	    updateEyeDependencies() ;
	}

	//
	// TODO: A brave soul could refine cache updates here.  There are a
	// lot of attributes to monitor, so we just update everything for now.
	//
	private void updateViewDependencies() {
	    // View policy, physical body eye positions, head to head
	    // tracker, window eyepoint policy, field of view, coexistence
	    // centering, or coexistence to image plate may have changed.
	    updateEyeInPlate = true ;
	    
	    // If the eye position in image plate has changed, then the
	    // projection transform may need to be updated.  The projection
	    // policy and clip plane distances and policies may have changed.
	    // The window resize policy and screen scale may have changed,
	    // which affects clip plane distance scaling.
	    updateProjection = true ;
	    updateClipDistances = true ;
	    updatePhysicalToVpScale = true ;
	    updatePhysicalToVirtualScale = true ;

	    // View policy, coexistence to tracker base, coexistence centering
	    // enable, or window movement policy may have changed.
	    updateCoeToPlate = true ;

	    // Screen scale, resize policy, view policy, view platform,
	    // physical body, physical environment, eyepoint policy, or field
	    // of view may have changed.
	    updateViewPlatformToCoe = true ;
	    updateCoeToViewPlatform = true ;
	    updateCoeToVworld = true ;

	    // The image plate to view platform transform is composed from the
	    // coexistence to image plate and view platform to coexistence
	    // transforms, so these need updates.
	    updatePlateToViewPlatform = true ;
	    updatePlateToVworld = true ;

	    // View platform to coexistence or coexistence to tracker base may
	    // have changed.
	    updateTrackerBaseToViewPlatform = true ;
	    updateTrackerBaseToVworld = true ;

	    // Screen scale policy or explicit screen scale may have changed.
	    updateScreenScale = true ;

	    // Update transforms derived from eye info.
	    updateEyeDependencies() ;
	}

	private void updateHeadDependencies() {
	    if (viewPolicy == View.HMD_VIEW) {
		// Image plates are fixed relative to the head, so their
		// positions have changed with respect to coexistence, the
		// view platform, and the virtual world.  The eyes are fixed
		// with respect to the image plates, so the projection doesn't
		// change with respect to them.
		updateCoeToPlate = true ;
		updatePlateToViewPlatform = true ;
		updatePlateToVworld = true ;
                updateViewPlatformToEye = true ;
                updateEyeToViewPlatform = true ;
		updateEyeToVworld = true ;
		updateInverseViewPlatformProjection = true ;
		updateInverseVworldProjection = true ;
	    }
	    else {
		// Eye positions have changed with respect to the fixed
		// screens, so the projections must be updated as well as the
		// positions.
		updateEyeInPlate = true ;
		updateEyeDependencies() ;
	    }
	}

	private void updateVworldDependencies() {
	    updatePlateToVworld = true ;
	    updateCoeToVworld = true ;
	    updateEyeToVworld = true ;
	    updateTrackerBaseToVworld = true ;
	    updateInverseVworldProjection = true ;

	    if (vpi.updateVworldScale)
		updatePhysicalToVirtualScale = true ;

	    if (vpi.updateVworldScale && clipVirtual) {
		// vworldToViewPlatformScale changed and clip plane distances
		// are in virtual units.
		updateProjection = true ;
		updateClipDistances = true ;
		updateInverseProjection = true ;
		updateInverseViewPlatformProjection = true ;
	    }
	}
    }

    /**
     * Prints out the specified transform in a readable format.
     *
     * @param t3d transform to be printed
     * @param name the name of the transform
     */
    private static void t3dPrint(Transform3D t3d, String name) {
	double[] m = new double[16] ;
	t3d.get(m) ;
	String[] sa = formatMatrixRows(4, 4, m) ;
	System.err.println(name) ;
	for (int i = 0 ; i < 4 ; i++) System.err.println(sa[i]) ;
    }

    /**
     * Formats a matrix with fixed fractional digits and integer padding to
     * align the decimal points in columns.  Non-negative numbers print up to
     * 7 integer digits, while negative numbers print up to 6 integer digits
     * to account for the negative sign.  6 fractional digits are printed.
     *
     * @param rowCount number of rows in the matrix
     * @param colCount number of columns in the matrix
     * @param m matrix to be formatted
     * @return matrix rows formatted into strings
     */
    private static String[] formatMatrixRows
	(int rowCount, int colCount, double[] m) {

	DecimalFormat df = new DecimalFormat("0.000000") ;
	FieldPosition fp = new FieldPosition(DecimalFormat.INTEGER_FIELD) ;
	StringBuffer sb0 = new StringBuffer() ;
	StringBuffer sb1 = new StringBuffer() ;
	String[] rows = new String[rowCount] ;

	for (int i = 0 ; i < rowCount ; i++) {
	    sb0.setLength(0) ;
	    for (int j = 0 ; j < colCount ; j++) {
		sb1.setLength(0) ;
		df.format(m[i*colCount+j], sb1, fp) ;
		int pad = 8 - fp.getEndIndex() ;
		for (int k = 0 ; k < pad ; k++) {
		    sb1.insert(0, " ") ;
		}
		sb0.append(sb1) ;
	    }
	    rows[i] = sb0.toString() ;
	}
	return rows ;
    }
}
