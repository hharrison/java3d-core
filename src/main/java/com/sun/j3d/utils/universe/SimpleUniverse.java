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

import com.sun.j3d.utils.geometry.Primitive;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.net.URL;

import javax.media.j3d.*;


/**
 * This class sets up a minimal user environment to quickly and easily
 * get a Java 3D program up and running.  This utility class creates
 * all the necessary objects on the "view" side of the scene graph.
 * Specifically, this class creates a locale, a single ViewingPlatform, 
 * and a Viewer object (both with their default values).
 * Many basic Java 3D applications
 * will find that SimpleUniverse provides all necessary functionality
 * needed by their applications. More sophisticated applications
 * may find that they need more control in order to get extra functionality
 * and will not be able to use this class.
 * 
 * @see Viewer
 * @see ViewingPlatform
 */
public class SimpleUniverse extends VirtualUniverse {

    /**
     * Locale reference needed to create the "view" portion
     * of the scene graph.
     */
    protected Locale          locale;

    /**
     * Viewer reference needed to create the "view" portion
     * of the scene graph.
     */
    protected Viewer[]        viewer = null;

    /**
     * Creates a locale, a single ViewingPlatform, and
     * and a Viewer object (both with their default values).
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public SimpleUniverse() {
        // call main constructor with default values.
        this(null, 1, null, null);
    }

    /**
     * Creates a locale, a single ViewingPlatform, and a Viewer object
     * (with default values).  The ViewingPlatform is created with the
     * specified number of TransformGroups.
     *
     * @param numTransforms The number of transforms to be in the
     * MultiTransformGroup object.
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     *
     * @since Java 3D 1.2.1
     */
    public SimpleUniverse(int numTransforms) {
	// call main constructor with default values except numTransforms
	this(null, numTransforms, null, null);
    }

    /**
     * Creates a locale, a single ViewingPlatform (with default values), and
     * and a Viewer object.  The Viewer object uses default values for
     * everything but the canvas.
     *
     * @param canvas The canvas to associate with the Viewer object.  Passing
     *  in null will cause this parameter to be ignored and a canvas to be
     *  created by the utility.
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public SimpleUniverse(Canvas3D canvas) {
        // call main constructor with default values for everything but
        // the canvas parameter.
        this(null, 1, canvas, null);
    }

    /**
     * Creates a locale, a single ViewingPlatform, and a Viewer object
     * The Viewer object uses default values for everything but the canvas.
     * The ViewingPlatform is created with the specified number of
     * TransformGroups.
     *
     * @param canvas The canvas to associate with the Viewer object.  Passing
     * in null will cause this parameter to be ignored and a canvas to be
     * created by the utility.
     * @param numTransforms The number of transforms to be in the
     * MultiTransformGroup object.
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     *
     * @since Java 3D 1.2.1
     */
    public SimpleUniverse(Canvas3D canvas, int numTransforms) {
	// call main constructor with default values except canvas
	// and numTransforms
	this(null, numTransforms, canvas, null);
    }

    /**
     * Creates a locale, a single ViewingPlatform, and a Viewer object
     * The Viewer object uses default values for everything but the canvas.
     * The ViewingPlatform is created with the specified number of
     * TransformGroups.
     *
     * @param canvas The canvas to associate with the Viewer object.  Passing
     * in null will cause this parameter to be ignored and a canvas to be
     * created by the utility.
     * @param numTransforms The number of transforms to be in the
     * MultiTransformGroup object.
     * @param localeFactory Factory for creating the locale
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     *
     * @since Java 3D 1.5.1
     */
    public SimpleUniverse(Canvas3D canvas, int numTransforms, LocaleFactory localeFactory) {
	// call main constructor with default values except canvas,
	// numTransforms and localeFactory
	this(null, numTransforms, canvas, null, localeFactory);
    }

    /**
     * Creates the "view" side of the scene graph.  The passed in parameters
     * override the default values where appropriate.
     *
     * @param origin The origin used to set the origin of the Locale object.
     *  If this object is null, then 0.0 is used.
     * @param numTransforms The number of transforms to be in the
     *  MultiTransformGroup object.
     * @param canvas The canvas to draw into.  If this is null, it is
     *  ignored and a canvas will be created by the utility.
     * @param userConfig The URL to the user's configuration file, used
     *  by the Viewer object.  This is never examined and default values are
     *  always taken.
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     * @deprecated use ConfiguredUniverse constructors to read a
     *  configuration file
     */
    public SimpleUniverse(HiResCoord origin, int numTransforms,
      Canvas3D canvas, URL userConfig) {
          this( origin, numTransforms, canvas, userConfig, null );
    }
    
    /**
     * Creates the "view" side of the scene graph.  The passed in parameters
     * override the default values where appropriate.
     *
     * @param origin The origin used to set the origin of the Locale object.
     *  If this object is null, then 0.0 is used.
     * @param numTransforms The number of transforms to be in the
     *  MultiTransformGroup object.
     * @param canvas The canvas to draw into.  If this is null, it is
     *  ignored and a canvas will be created by the utility.
     * @param userConfig The URL to the user's configuration file, used
     *  by the Viewer object.  This is never examined and default values are
     *  always taken.
     * @param localeFactory The Locale Factory which will instantiate the
     *  locale(s) for this universe.
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     * @deprecated use ConfiguredUniverse constructors to read a
     *  configuration file
     */
    public SimpleUniverse(HiResCoord origin, int numTransforms,
      Canvas3D canvas, URL userConfig, LocaleFactory localeFactory ) {
	ViewingPlatform vwp;

        createLocale( origin, localeFactory );
        
        // Create the ViewingPlatform and Viewer objects, passing
        // down the appropriate parameters.
	vwp = new ViewingPlatform(numTransforms);
        vwp.setUniverse( this );
        viewer = new Viewer[1];
        // viewer[0] = new Viewer(canvas, userConfig);
        viewer[0] = new Viewer(canvas);
        viewer[0].setViewingPlatform(vwp);

        // Add the ViewingPlatform to the locale - the scene
        // graph is now "live".
        locale.addBranchGraph(vwp);
    }


    /**
     * Creates the "view" side of the scene graph.  The passed in parameters
     * override the default values where appropriate.
     *
     * @param viewingPlatform The viewingPlatform to use to create
     *  the "view" side of the scene graph.
     * @param viewer The viewer object to use to create
     *  the "view" side of the scene graph.
     */
    public SimpleUniverse(ViewingPlatform viewingPlatform, Viewer viewer) {
        this( viewingPlatform, viewer, null );
    }
    
    /**
     * Creates the "view" side of the scene graph.  The passed in parameters
     * override the default values where appropriate.
     *
     * @param viewingPlatform The viewingPlatform to use to create
     *  the "view" side of the scene graph.
     * @param viewer The viewer object to use to create
     *  the "view" side of the scene graph.
     * @param localeFactory The factory used to create the Locale Object
     */
    public SimpleUniverse(ViewingPlatform viewingPlatform, Viewer viewer,
			  LocaleFactory localeFactory ) {
        createLocale( null, localeFactory );
        viewingPlatform.setUniverse( this );
        
        // Assign object references.
        this.viewer = new Viewer[1];
        this.viewer[0] = viewer;

        // Add the ViewingPlatform to the Viewer object.
        this.viewer[0].setViewingPlatform(viewingPlatform);

        // Add the ViewingPlatform to the locale - the scene
        // graph is now "live".
        locale.addBranchGraph(viewingPlatform);
    }
    
    /** 
     * Constructor for use by Configured Universe
     */
    SimpleUniverse( HiResCoord origin, LocaleFactory localeFactory ) {
        createLocale( origin, localeFactory );
    }
    
    /** 
     *  Create the Locale using the LocaleFactory and HiRes origin,
     *  if specified. 
     */
    private void createLocale( HiResCoord origin,
			       LocaleFactory localeFactory ) {

	if (localeFactory != null) {
	    if (origin != null)
		locale = localeFactory.createLocale(this, origin);
	    else
		locale = localeFactory.createLocale(this);
	}
	else {
	    if (origin != null)
		locale = new Locale(this, origin);
	    else
		locale = new Locale(this);
	}
    }

    /**
     * Returns the Locale object associated with this scene graph.
     *
     * @return The Locale object used in the construction of this scene
     *  graph.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the Viewer object associated with this scene graph.  
     * SimpleUniverse creates a single Viewer object for use in the
     * scene graph.
     * 
     * @return The Viewer object associated with this scene graph.
     */
    public Viewer getViewer() {
        return viewer[0];
    }

    /**
     * Returns the ViewingPlatform object associated with this scene graph.
     *
     * @return The ViewingPlatform object of this scene graph.
     */
    public ViewingPlatform getViewingPlatform() {
        return viewer[0].getViewingPlatform();
    }

    /**
     * Returns the Canvas3D object associated with this Java 3D Universe.
     *
     * @return A reference to the Canvas3D object associated with the
     *  Viewer object.  This method is equivalent to calling getCanvas(0).
     *
     * @see Viewer
     */
    public Canvas3D getCanvas() {
        return getCanvas(0);
    }

    /**
     * Returns the Canvas3D object at the specified index associated with
     * this Java 3D Universe.
     *
     * @param canvasNum The index of the Canvas3D object to retrieve.
     *  If there is no Canvas3D object for the given index, null is returned.
     *
     * @return A reference to the Canvas3D object associated with the
     *  Viewer object.
     */
    public Canvas3D getCanvas(int canvasNum) {
        return viewer[0].getCanvas3D(canvasNum);
    }

    /**
     * Used to add Nodes to the geometry side (as opposed to the view side)
     * of the scene graph.  This is a short cut to getting the Locale object
     * and calling that object's addBranchGraph() method.
     *
     * @param bg The BranchGroup to attach to this Universe's Locale.
     */
    public void addBranchGraph(BranchGroup bg) {
        locale.addBranchGraph(bg);
    }

    /**
     * Finds the preferred <code>GraphicsConfiguration</code> object
     * for the system.  This object can then be used to create the
     * Canvas3D objet for this system.
     *
     * @return The best <code>GraphicsConfiguration</code> object for
     *  the system.
     */
    public static GraphicsConfiguration getPreferredConfiguration() {
        GraphicsConfigTemplate3D template = new GraphicsConfigTemplate3D();
        String stereo;

        // Check if the user has set the Java 3D stereo option.
        // Getting the system properties causes appletviewer to fail with a
        //  security exception without a try/catch.

        stereo = (String) java.security.AccessController.doPrivileged(
           new java.security.PrivilegedAction() {
           public Object run() {
               return System.getProperty("j3d.stereo");
           }
        });

        // update template based on properties.
        if (stereo != null) {
            if (stereo.equals("REQUIRED"))
                template.setStereo(template.REQUIRED);
            else if (stereo.equals("PREFERRED"))
                template.setStereo(template.PREFERRED);
        }

        // Return the GraphicsConfiguration that best fits our needs.
        return GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getBestConfiguration(template);
    }

    /**
     * Cleanup memory use and reference by SimpleUniverse.
     * Typically it should be invoked by the applet's destroy method.
     */
    public void cleanup() {
	// Get view associated with this SimpleUniverse
	View view = viewer[0].getView();
	
	// Issue 134: cleanup all off-screen canvases
	for (int i = view.numCanvas3Ds() - 1; i >= 0; i--) {
	    Canvas3D c = view.getCanvas3D(i);
	    if (c.isOffScreen()) {
		c.setOffScreenBuffer(null);
	    }
	}

	// Remove all canvases from view; remove the viewing platform from
	// this viewer; remove all locales to cleanup the scene graph
	view.removeAllCanvas3Ds();
        viewer[0].setViewingPlatform(null);
	removeAllLocales();

	// viewerMap cleanup here to prevent memory leak problem.
	Viewer.clearViewerMap();
        Primitive.clearGeometryCache();

    }
}
