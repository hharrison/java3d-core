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

package com.sun.j3d.utils.universe;

import java.awt.event.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import javax.media.j3d.*;
import javax.swing.*;
import com.sun.j3d.audioengines.AudioEngine3DL2;
import java.lang.reflect.Constructor;

/**
 * The Viewer class holds all the information that describes the physical
 * and virtual "presence" in the Java 3D universe.  The Viewer object
 * consists of:
 * <UL>
 * <LI>Physical Objects</LI>
 *  <UL>
 *   <LI>Canvas3D's - used to render with.</LI>
 *   <LI>PhysicalEnvironment - holds characteristics of the hardware platform
 *    being used to render on.</LI>
 *   <LI>PhysicalBody -  holds the physical characteristics and personal
 *    preferences of the person who will be viewing the Java 3D universe.</LI>
 *  </UL>
 * <LI>Virtual Objects</LI>
 *  <UL>
 *   <LI>View - the Java 3D View object.</LI>
 *   <LI>ViewerAvatar - the geometry that is used by Java 3D to represent the
 *    person viewing the Java 3D universe.</LI>
 *  </UL>
 * </UL>
 * If the Viewer object is created without any Canvas3D's, or indirectly
 * through a configuration file, it will create the Canvas3D's as needed.
 * The default Viewer creates one Canvas3D.  If the Viewer object creates
 * the Canvas3D's, it will also create a JPanel and JFrame for each Canvas3D.
 *
 * Dynamic video resize is a new feature in Java 3D 1.3.1.   
 * This feature provides a means for doing swap synchronous resizing
 * of the area that is to be magnified (or passed through) to the
 * output video resolution. This functionality allows an application
 * to draw into a smaller viewport in the framebuffer in order to reduce
 * the time spent doing pixel fill. The reduced size viewport is then 
 * magnified up to the video output resolution using the SUN_video_resize
 * extension. This extension is only implemented in XVR-4000 and later
 * hardware with back end video out resizing capability.
 *
 * If video size compensation is enable, the line widths, point sizes and pixel 
 * operations will be scaled internally with the resize factor to approximately 
 * compensate for video resizing. The location of the pixel ( x, y ) in the 
 * resized framebuffer = ( floor( x * factor + 0.5 ), floor( y * factor + 0.5 ) )
 *
 * <p>
 * @see Canvas3D
 * @see PhysicalEnvironment
 * @see PhysicalBody
 * @see View
 * @see ViewerAvatar
 */
public class Viewer {
    private static final boolean             debug               = false;
    private static PhysicalBody              physicalBody        = null;
    private static PhysicalEnvironment       physicalEnvironment = null;
    private        View                      view                = null;
    private        ViewerAvatar	             avatar              = null;
    private        Canvas3D[]                canvases            = null;
    private        JFrame[]                  j3dJFrames          = null;
    private        JPanel[]                  j3dJPanels          = null;
    private        Window[]                  j3dWindows          = null;
    private        ViewingPlatform           viewingPlatform     = null;

    
    static HashMap viewerMap = new HashMap(5);
    private float dvrFactor = 1.0f;
    private boolean doDvr = false;
    private boolean doDvrResizeCompensation = true;


    /**
     * Get the Viewer associated with the view object.
     *
     * @param view The View object for inquiry.
     * @return The Viewer object associated with this View object. 
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public static Viewer getViewer(View view) {
	Viewer viewer = null;
	synchronized (viewerMap) {
	    //System.out.println("Viewer.getViewer viewerMap's size is " + viewerMap.size());
	    viewer =  (Viewer) (viewerMap.get(view));
	}
	return viewer;
    }


    /**
     * Removes the entry associated with the view object.
     *
     * @param view The View object to be removed.
     * @return The Viewer object associated with this View object. 
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public static Viewer removeViewerMapEntry(View view) {
	Viewer viewer = null;
	synchronized (viewerMap) {

	    viewer =  (Viewer) (viewerMap.remove(view));
	}
	// System.out.println("viewerMap.size() " + viewerMap.size());

	return viewer;
    }


    /**
     * Removes all Viewer mappings from the Viewer map.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public static void clearViewerMap() {
	synchronized (viewerMap) {	   
	    viewerMap.clear();
	}
	// System.out.println("clearViewerMap - viewerMap.size() " + viewerMap.size());

    }


    /**
     * Returns a status flag indicating whether or not dynamic video size
     * is enabled.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public boolean isDvrEnabled() {
	return doDvr; 
    }

    /**
     * Turns on or off dynamic video size.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @param dvr enables or disables dynamic video size.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public void setDvrEnable(boolean dvr) {
	doDvr = dvr;
	view.repaint();

    }

    /**
     * Retrieves the dynamic video resize factor of this
     * viewer.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public float getDvrFactor() {
	return dvrFactor;
    }


    /**
     * Set the dynamic video resize factor for this viewer.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @param dvr set the dynamic video resize factor for this viewer.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public void setDvrFactor(float dvr) {
	dvrFactor = dvr;
	view.repaint();

    }

    /**
     * Turns on or off dynamic video resize compensation. 
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *     
     * @param dvrRCE enables or disables dynamic video resize compensation.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public void setDvrResizeCompensationEnable(boolean dvrRCE) {
	doDvrResizeCompensation = dvrRCE;
	view.repaint();
    }

    /**
     * Returns a status flag indicating whether or not dynamic video resize 
     * compensation is enabled.
     *
     * Note: This method is targeted for SUN framebuffer XVR-4000 and later 
     * hardware that support video size extension.
     *
     * @since Java 3D 1.3.1
     */
    // To support a back door for DVR support.
    public boolean getDvrResizeCompensationEnable() {
	return doDvrResizeCompensation;
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  A single RGB, double buffered
     * and depth buffered Canvas3D object is created.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     */
    public Viewer() {
        // Call main constructor with default values.
        this(null, null, null, true);
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvas the Canvas3D object to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @since Java3D 1.1
     */
    public Viewer(Canvas3D userCanvas) {
        // Call main constructor.
        this(userCanvas == null ? null : new Canvas3D[] {userCanvas},
	     null, null, true);
    }


    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalBody and PhysicalEnvironment.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvases the Canvas3D objects to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @since Java3D 1.3
     */
    public Viewer(Canvas3D[] userCanvases) {
	this(userCanvases, null, null, true);
    }

    /**
     * Creates a viewer object. The Canvas3D objects, PhysicalEnvironment, and
     * PhysicalBody are taken from the arguments.
     *
     * @param userCanvases the Canvas3D objects to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @param userBody the PhysicalBody to use for this Viewer; if it is
     *  null, a default PhysicalBody object is created
     * @param userEnvironment the PhysicalEnvironment to use for this Viewer;
     *  if it is null, a default PhysicalEnvironment object is created
     * @param setVisible determines if the Frames should be set to visible once created
     * @since Java3D 1.3
     */
    public Viewer(Canvas3D[] userCanvases, PhysicalBody userBody,
		  PhysicalEnvironment userEnvironment, boolean setVisible ) {

	if (userBody == null) {
            physicalBody = new PhysicalBody();
        } else {
            physicalBody = userBody;
	}

        if (userEnvironment == null) {
            physicalEnvironment = new PhysicalEnvironment();
        } else {
            physicalEnvironment = userEnvironment;
	}

        // Create Canvas3D object if none was passed in.
        if (userCanvases == null) {
	    GraphicsConfiguration config =
		ConfiguredUniverse.getPreferredConfiguration();

	    canvases = new Canvas3D[1];
	    canvases[0] = new Canvas3D(config);
            try {
	        canvases[0].setFocusable( true );
            } catch(NoSuchMethodError e) {}
	    createFramesAndPanels(setVisible);
        }
	else {
	    canvases = new Canvas3D[userCanvases.length];
	    for (int i=0; i<userCanvases.length; i++) {
		canvases[i] = userCanvases[i];
                try {
	            canvases[i].setFocusable( true );
                } catch(NoSuchMethodError e) {}
	    }
	}

        // Create a View and attach the Canvas3D and the physical
        // body and environment to the view.
        view = new View();
        
        // Fix to issue 424
        view.setUserHeadToVworldEnable(true);
        
	// Add it to the Viewer's HashMap.
	synchronized (viewerMap) {
	    Viewer.viewerMap.put(view, this);
	}
	for (int i=0; i<canvases.length; i++) {
	    view.addCanvas3D(canvases[i]);
	}
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);
    }

    /**
     * Creates a default Viewer object. The default values are used to create
     * the PhysicalEnvironment and PhysicalBody.  A single RGB, double buffered
     * and depth buffered Canvas3D object is created.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userConfig the URL of the user configuration file used to
     *  initialize the PhysicalBody object; this is always ignored
     * @since Java3D 1.1
     * @deprecated create a ConfiguredUniverse to use a configuration file
     */
    public Viewer(URL userConfig) {
        // Call main constructor.
        this(null, userConfig);
    }

    /**
     * Creates a default viewer object. The default values are used to create
     * the PhysicalEnvironment and PhysicalBody.  The View is created
     * with a front clip distance of 0.1f and a back clip distance of 10.0f.
     *
     * @param userCanvas the Canvas3D object to be used for rendering;
     *  if this is null then a single RGB, double buffered and depth buffered
     *  Canvas3D object is created
     * @param userConfig the URL of the user configuration file used to
     *  initialize the PhysicalBody object; this is always ignored
     * @since Java3D 1.1
     * @deprecated create a ConfiguredUniverse to use a configuration file
     */
    public Viewer(Canvas3D userCanvas, URL userConfig) {
        // Only one PhysicalBody per Universe.
        if (physicalBody == null) {
            physicalBody = new PhysicalBody();
        }

        // Only one PhysicalEnvironment per Universe.
        if (physicalEnvironment == null) {
            physicalEnvironment = new PhysicalEnvironment();
        }

        // Create Canvas3D object if none was passed in.
        if (userCanvas == null) {
	    GraphicsConfiguration config =
		SimpleUniverse.getPreferredConfiguration();

	    canvases = new Canvas3D[1];
	    canvases[0] = new Canvas3D(config);
	    createFramesAndPanels(true);
	}
        else {
	    canvases = new Canvas3D[1];
            canvases[0] = userCanvas;
	}

	try {
	    canvases[0].setFocusable( true );
        } catch(NoSuchMethodError e) {}

        // Create a View and attach the Canvas3D and the physical
        // body and environment to the view.
        view = new View();
        
        // Fix to issue 424
        view.setUserHeadToVworldEnable(true);
        
	// Add it to the Viewer's HashMap.
	synchronized (viewerMap) {
	    Viewer.viewerMap.put(view, this);
	}
        view.addCanvas3D(canvases[0]);
        view.setPhysicalBody(physicalBody);
        view.setPhysicalEnvironment(physicalEnvironment);
    }

    /**
     * Package-scoped constructor to create a Viewer from the configuration
     * objects provided by ConfiguredUniverse.
     *
     * @param cs array of ConfigScreen objects containing configuration
     *  information for the physical screens in the environment
     * @param cv ConfigView object containing configuration information about
     *  the view to be created using the given screens
     * @param setVisible if true, call setVisible(true) on all created Window
     *  components; otherwise, they remain invisible
     */
    Viewer(ConfigScreen[] cs, ConfigView cv, boolean setVisible) {

	// Retrieve the J3D View object from the ConfigView object.
	// The physical body and environment have already been set there.
	view = cv.j3dView;
	// Add it to the Viewer's HashMap.
	synchronized (viewerMap) {
	    Viewer.viewerMap.put(view, this);
	}

	// Set this Viewer's references to the physical body and environment.
	physicalBody = cv.physicalBody;
	physicalEnvironment = cv.physicalEnvironment;

	// Get available screen devices.
	// 
	// When running with JDK 1.3.1 or older under the X Window System with
	// Xinerama enabled, a single screen device is returned which is
	// actually a virtual screen spanning all the physical screens in the
	// X display.  These can only be configured as a single planar screen
	// in the configuration file.
	// 
	// JDK 1.4 and newer returns a screen device for each physical screen,
	// allowing them to configured as distinct screens with arbitrary
	// orientations relative to each other.
	// 
	GraphicsDevice[] devices;
	GraphicsEnvironment graphicsEnv;

        graphicsEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
        devices = graphicsEnv.getScreenDevices();

	if (devices == null)
	    throw new RuntimeException(
                    "No screen devices available in local environment");

	if (debug) {
	    System.out.println
		("Viewer: GraphicsEnvironment returned " + devices.length +
		 " GraphicsDevice object" + (devices.length == 1 ? "" : "s"));

	    for (int i = 0; i < devices.length; i++) {
		System.out.println
		    (devices[i] + "\n" +
		     devices[i].getDefaultConfiguration().getBounds() + "\n");
	    }
	}

	// Allocate the arrays of components to be used.  AWT Windows are used
	// to hold either a JFrame or a JWindow.
	canvases = new Canvas3D[cs.length];
	j3dJFrames = new JFrame[cs.length];
	j3dJPanels = new JPanel[cs.length];
	j3dWindows = new Window[cs.length];

	// Create a graphics template requesting the desired capabilities.
	GraphicsConfigTemplate3D tpl3D = new GraphicsConfigTemplate3D();
	if (cv.stereoEnable) {
	    tpl3D.setStereo(tpl3D.PREFERRED);
	}
        if (cv.antialiasingEnable) {
            tpl3D.setSceneAntialiasing(tpl3D.PREFERRED);
        }

	// Loop through all screens.  Set up the Swing component structure and
	// the configured attributes for the Canvas3D and Screen3D associated
	// with each screen.
	for (int i = 0; i < cs.length; i++) {
            if (cs[i].frameBufferNumber >= devices.length)
                throw new ArrayIndexOutOfBoundsException(
                    cs[i].errorMessage(cs[i].creatingCommand,
                        "Screen " + cs[i].frameBufferNumber + " is invalid; " +
                        (devices.length-1) + " is the maximum local index."));

	    Rectangle bounds;
	    Container contentPane;
	    GraphicsConfiguration cfg =
		devices[cs[i].frameBufferNumber].getBestConfiguration(tpl3D);

	    if (cfg == null)
                throw new RuntimeException(
                        "No GraphicsConfiguration on screen " +
                        cs[i].frameBufferNumber + " conforms to template");

            // Workaround for Issue 316 - use the default config for the screen
            GraphicsConfiguration defCfg = cfg.getDevice().getDefaultConfiguration();
	    bounds = defCfg.getBounds();
	    cs[i].j3dJFrame = j3dJFrames[i] =
		new JFrame(cs[i].instanceName, defCfg);

	    if (cs[i].noBorderFullScreen) {
		try {
		    // Required by JDK 1.4 AWT for borderless full screen.
		    j3dJFrames[i].setUndecorated(true);

		    cs[i].j3dWindow = j3dWindows[i] = j3dJFrames[i];
		    contentPane = j3dJFrames[i].getContentPane();
		}
		catch (NoSuchMethodError e) {
		    // Handle borderless full screen running under JDK 1.3.1.
		    JWindow jwin = new JWindow(j3dJFrames[i], cfg);

		    cs[i].j3dWindow = j3dWindows[i] = jwin;
		    contentPane = jwin.getContentPane();
		}

		contentPane.setLayout(new BorderLayout());
 		j3dWindows[i].setSize(bounds.width, bounds.height);
		j3dWindows[i].setLocation(bounds.x, bounds.y);
	    }
	    else {
		cs[i].j3dWindow = j3dWindows[i] = j3dJFrames[i];

		contentPane = j3dJFrames[i].getContentPane();
		contentPane.setLayout(new BorderLayout());

		if (cs[i].fullScreen) {
		    j3dWindows[i].setSize(bounds.width, bounds.height);
		    j3dWindows[i].setLocation(bounds.x, bounds.y);
		}
		else {
		    j3dWindows[i].setSize(cs[i].windowWidthInPixels,
					  cs[i].windowHeightInPixels);
		    j3dWindows[i].setLocation(bounds.x + cs[i].windowX,
					      bounds.y + cs[i].windowY) ;
		}
	    }

	    // Create a Canvas3D and set its attributes.
	    cs[i].j3dCanvas = canvases[i] = new Canvas3D(cfg);
	    canvases[i].setStereoEnable(cv.stereoEnable);
	    canvases[i].setMonoscopicViewPolicy(cs[i].monoscopicViewPolicy);

	    // Get the Screen3D and set its attributes.
	    Screen3D screen = canvases[i].getScreen3D();

	    if (cs[i].physicalScreenWidth != 0.0)
		screen.setPhysicalScreenWidth(cs[i].physicalScreenWidth);

	    if (cs[i].physicalScreenHeight != 0.0)
		screen.setPhysicalScreenHeight(cs[i].physicalScreenHeight);

	    if (cs[i].trackerBaseToImagePlate != null)
		screen.setTrackerBaseToImagePlate
		    (new Transform3D(cs[i].trackerBaseToImagePlate));

	    if (cs[i].headTrackerToLeftImagePlate != null)
		screen.setHeadTrackerToLeftImagePlate
		    (new Transform3D(cs[i].headTrackerToLeftImagePlate));

	    if (cs[i].headTrackerToRightImagePlate != null)
		screen.setHeadTrackerToRightImagePlate
		    (new Transform3D(cs[i].headTrackerToRightImagePlate));

	    // Put the Canvas3D into a JPanel.
	    cs[i].j3dJPanel = j3dJPanels[i] = new JPanel();
	    j3dJPanels[i].setLayout(new BorderLayout());
	    j3dJPanels[i].add("Center", canvases[i]);

	    // Put the JPanel into the content pane used by JWindow or JFrame.
	    contentPane.add("Center", j3dJPanels[i]);

	    // Attach the Canvas3D to the View.
	    view.addCanvas3D(canvases[i]);

	    // Add a windowListener to detect the window close event.
	    addWindowCloseListener(j3dWindows[i]);

	    // Set Canvas3D focus as required by the JDK 1.4 focus model for
	    // full screen frames.  JDK 1.3.1 sets the focus automatically for
	    // full screen components.
	    try {
		canvases[i].setFocusable(true) ;
	    }
	    catch (NoSuchMethodError e) {
	    }

	    if (debug) {
		System.out.println("Viewer: created Canvas3D for screen " +
				   cs[i].frameBufferNumber + " with size\n  " +
				   j3dWindows[i].getSize());
		System.out.println("Screen3D[" + i + "]:  size in pixels (" +
				   screen.getSize().width + " x " +
				   screen.getSize().height + ")");
		System.out.println("  physical size in meters:  (" +
				   screen.getPhysicalScreenWidth() + " x " +
				   screen.getPhysicalScreenHeight() + ")");
		System.out.println("  hashCode = " + screen.hashCode() + "\n");
	    }
        }

	if (setVisible)
	    // Call setVisible() on all created Window components.
	    setVisible(true);
    }

    // Create the JFrames and JPanels for application-supplied Canvas3D
    // objects. 
    private void createFramesAndPanels( boolean setVisible ) {
	j3dJFrames = new JFrame[canvases.length];
	j3dJPanels = new JPanel[canvases.length];
	j3dWindows = new Window[canvases.length];

	for (int i = 0; i < canvases.length; i++) {
            j3dWindows[i] = j3dJFrames[i] = new JFrame();
            j3dJFrames[i].getContentPane().setLayout(new BorderLayout());
            j3dJFrames[i].setSize(256, 256);
	    
            // Put the Canvas3D into a JPanel.
            j3dJPanels[i] = new JPanel();
            j3dJPanels[i].setLayout(new BorderLayout());
            j3dJPanels[i].add("Center", canvases[i]);
            j3dJFrames[i].getContentPane().add("Center", j3dJPanels[i]);
	    if (setVisible) {
                j3dJFrames[i].setVisible(true);
	    }
	    addWindowCloseListener(j3dJFrames[i]);
	}
    }

    /**
     * Call setVisible() on all Window components created by this Viewer.
     *
     * @param visible boolean to be passed to the setVisible() calls on the
     *  Window components created by this Viewer
     * @since Java3D 1.3
     */
    public void setVisible(boolean visible) {
	for (int i = 0; i < j3dWindows.length; i++) {
	    j3dWindows[i].setVisible(visible);
	}
    }

    /**
     * Returns the View object associated with the Viewer object.
     *
     * @return The View object of this Viewer.
     */
    public View getView() {
        return view;
    }

    /**
     * Set the ViewingPlatform object used by this Viewer.
     *
     * @param platform The ViewingPlatform object to set for this
     *  Viewer object.  Use null to unset the current value and
     *  not assign assign a new ViewingPlatform object.
     */
    public void setViewingPlatform(ViewingPlatform platform) {
	if (viewingPlatform != null) {
	    viewingPlatform.removeViewer(this);
	}

        viewingPlatform = platform;

        if (platform != null) {
            view.attachViewPlatform(platform.getViewPlatform());
            platform.addViewer(this);

            if (avatar != null)
                viewingPlatform.setAvatar(this, avatar);
        }
        else
            view.attachViewPlatform(null);
    }
    /**
     * Get the ViewingPlatform object used by this Viewer.
     *
     * @return The ViewingPlatform object used by this
     *  Viewer object. 
     */
    public ViewingPlatform getViewingPlatform() {
	return viewingPlatform;
    }

    /**
     * Sets the geometry to be associated with the viewer's avatar.  The
     * avatar is the geometry used to represent the viewer in the virtual
     * world.
     *
     * @param avatar The geometry to associate with this Viewer object.
     *  Passing in null will cause any geometry associated with the Viewer
     *  to be removed from the scen graph.
     */
    public void setAvatar(ViewerAvatar avatar) {
        // Just return if trying to set the same ViewerAvatar object.
        if (this.avatar == avatar)
            return;

        this.avatar = avatar;
        if (viewingPlatform != null)
            viewingPlatform.setAvatar(this, this.avatar);
    }

    /**
     * Gets the geometry associated with the viewer's avatar.  The
     * avatar is the geometry used to represent the viewer in the virtual
     * world.
     *
     * @return The root of the scene graph that is used to represent the
     *  viewer's avatar.
     */
    public ViewerAvatar getAvatar() {
        return avatar;
    }

    /**
     * Returns the PhysicalBody object associated with the Viewer object.
     *
     * @return A reference to the PhysicalBody object.
     */
    public PhysicalBody getPhysicalBody() {
        return physicalBody;
    }

    /**
     * Returns the PhysicalEnvironment object associated with the Viewer
     * object.
     *
     * @return A reference to the PhysicalEnvironment object.
     */
    public PhysicalEnvironment getPhysicalEnvironment() {
        return physicalEnvironment;
    }

    /**
     * Returns the 0th Canvas3D object associated with this Viewer object
     *
     * @return a reference to the 0th Canvas3D object associated with this
     *  Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D getCanvas3D() {
        return canvases[0];
    }

    /**
     * Returns the Canvas3D object at the specified index associated with
     * this Viewer object.
     *
     * @param canvasNum the index of the Canvas3D object to retrieve;
     *  if there is no Canvas3D object for the given index, null is returned
     * @return a reference to a Canvas3D object associated with this
     *  Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D getCanvas3D(int canvasNum) {
	if (canvasNum > canvases.length) {
	    return null;
	}
        return canvases[canvasNum];
    }

    /**
     * Returns all the Canvas3D objects associated with this Viewer object.
     *
     * @return an array of references to the Canvas3D objects associated with
     *  this Viewer object
     * @since Java3D 1.3
     */
    public Canvas3D[] getCanvas3Ds() {
	Canvas3D[] ret = new Canvas3D[canvases.length];
	for (int i = 0; i < canvases.length; i++) {
	    ret[i] = canvases[i];
	}
        return ret;
    }

    /**
     * Returns the canvas associated with this Viewer object.
     * @deprecated superceded by getCanvas3D()
     */
    public Canvas3D getCanvases() {
        return getCanvas3D();
    }

    /**
     * This method is no longer supported since Java 3D 1.3.
     * @exception UnsupportedOperationException if called.
     * @deprecated AWT Frame components are no longer created by the
     *  Viewer class.
     */
    public Frame getFrame() {
        throw new UnsupportedOperationException(
                "AWT Frame components are not created by the Viewer class");
    }

    /**
     * Returns the JFrame object created by this Viewer object at the
     * specified index.  If a Viewer is constructed without any Canvas3D
     * objects then the Viewer object will create a Canva3D object, a JPanel
     * containing the Canvas3D object, and a JFrame to place the JPanel in.
     * <p>
     * NOTE: When running under JDK 1.4 or newer, the JFrame always directly
     * contains the JPanel which contains the Canvas3D.  When running under
     * JDK 1.3.1 and creating a borderless full screen through a configuration
     * file, the JFrame will instead contain a JWindow which will contain the
     * JPanel and Canvas3D.
     * <p>
     * @param frameNum the index of the JFrame object to retrieve;
     *  if there is no JFrame object for the given index, null is returned
     * @return a reference to JFrame object created by this Viewer object
     * @since Java3D 1.3
     */
    public JFrame getJFrame(int frameNum) {
	if (j3dJFrames == null || frameNum > j3dJFrames.length) {
	    return(null);
	}
        return j3dJFrames[frameNum];
    }

    /**
     * Returns all the JFrames created by this Viewer object.  If a Viewer is
     * constructed without any Canvas3D objects then the Viewer object will
     * create a Canva3D object, a JPanel containing the Canvas3D object, and a
     * JFrame to place the JPanel in.<p>
     * 
     * NOTE: When running under JDK 1.4 or newer, the JFrame always directly
     * contains the JPanel which contains the Canvas3D.  When running under
     * JDK 1.3.1 and creating a borderless full screen through a configuration
     * file, the JFrame will instead contain a JWindow which will contain the
     * JPanel and Canvas3D.<p>
     *
     * @return an array of references to the JFrame objects created by
     *  this Viewer object, or null if no JFrame objects were created
     * @since Java3D 1.3
     */
    public JFrame[] getJFrames() {
	if (j3dJFrames == null)
	    return null;

	JFrame[] ret = new JFrame[j3dJFrames.length];
	for (int i = 0; i < j3dJFrames.length; i++) {
	    ret[i] = j3dJFrames[i];
	}
        return ret;
    }

    /**
     * This method is no longer supported since Java 3D 1.3.
     * @exception UnsupportedOperationException if called.
     * @deprecated AWT Panel components are no longer created by the
     * Viewer class.
     */
    public Panel getPanel() {
        throw new UnsupportedOperationException(
                "AWT Panel components are not created by the Viewer class");
    }

    /**
     * Returns the JPanel object created by this Viewer object at the
     * specified index.  If a Viewer is constructed without any Canvas3D
     * objects then the Viewer object will create a Canva3D object and a
     * JPanel into which to place the Canvas3D object.
     *
     * @param panelNum the index of the JPanel object to retrieve;
     *  if there is no JPanel object for the given index, null is returned
     * @return a reference to a JPanel object created by this Viewer object
     * @since Java3D 1.3
     */
    public JPanel getJPanel(int panelNum) {
	if (j3dJPanels == null || panelNum > j3dJPanels.length) {
	    return(null);
	}
        return j3dJPanels[panelNum];
    }

    /**
     * Returns all the JPanel objects created by this Viewer object.  If a
     * Viewer is constructed without any Canvas3D objects then the Viewer
     * object will create a Canva3D object and a JPanel into which to place
     * the Canvas3D object.
     *
     * @return an array of references to the JPanel objects created by
     *  this Viewer object, or null or no JPanel objects were created
     * @since Java3D 1.3
     */
    public JPanel[] getJPanels() {
	if (j3dJPanels == null)
	    return null;

	JPanel[] ret = new JPanel[j3dJPanels.length];
	for (int i = 0; i < j3dJPanels.length; i++) {
	    ret[i] = j3dJPanels[i];
	}
        return ret;
    }

    /**
     * Used to create and initialize a default AudioDevice3D used for sound
     * rendering.
     *
     * @return reference to created AudioDevice, or null if error occurs.
     */
    public AudioDevice createAudioDevice() {
	if (physicalEnvironment == null) {
	    System.err.println("Java 3D: createAudioDevice: physicalEnvironment is null");
	    return null;
	}

	try {
	    String audioDeviceClassName =
		(String) java.security.AccessController.doPrivileged(
		    new java.security.PrivilegedAction() {
			public Object run() {
			    return System.getProperty("j3d.audiodevice");
			}
		    });

	    if (audioDeviceClassName == null) {
		throw new UnsupportedOperationException("No AudioDevice specified");
	    }

            // Issue 341: try the current class loader first before trying the
            // system class loader
	    Class audioDeviceClass = null;
            try {
                audioDeviceClass = Class.forName(audioDeviceClassName);
            } catch (ClassNotFoundException ex) {
                // Ignore excpetion and try system class loader
            }

            if (audioDeviceClass == null) {
                ClassLoader audioDeviceClassLoader =
                    (ClassLoader) java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedAction() {
                            public Object run() {
                                return ClassLoader.getSystemClassLoader();
                            }
                        });

                if (audioDeviceClassLoader == null) {
                    throw new IllegalStateException("System ClassLoader is null");
                }

                audioDeviceClass = Class.forName(audioDeviceClassName, true, audioDeviceClassLoader);
            }

	    Class physEnvClass = PhysicalEnvironment.class;
	    Constructor audioDeviceConstructor =
		    audioDeviceClass.getConstructor(new Class[] {physEnvClass});
	    PhysicalEnvironment[] args = new PhysicalEnvironment[] { physicalEnvironment };
	    AudioEngine3DL2 mixer =
		(AudioEngine3DL2) audioDeviceConstructor.newInstance((Object[])args);
	    mixer.initialize();
	    return mixer;
	}
	catch (Throwable e) {
	    e.printStackTrace();
	    physicalEnvironment.setAudioDevice(null);
	    System.err.println("Java 3D: audio is disabled");
	    return null;
	}
    }

    /**
     * Returns the Universe to which this Viewer is attached
     *
     * @return the Universe to which this Viewer is attached
     * @since Java 3D 1.3
     */
    public SimpleUniverse getUniverse() {
        return getViewingPlatform().getUniverse();
    }


    /*
     * Exit if run as an application
     */
    void addWindowCloseListener(Window win) {
	SecurityManager sm = System.getSecurityManager();
	boolean doExit = true;

	if (sm != null) {
	    try {
		sm.checkExit(0);
	    } catch (SecurityException e) {
		doExit = false;
	    }
	}
	final boolean _doExit = doExit;

	win.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvent) {
		Window w = winEvent.getWindow();
		w.setVisible(false);
		try {
		    w.dispose();
		} catch (IllegalStateException e) {}
		if (_doExit) {
		    System.exit(0);
		}
	    }
	});
    }
}
