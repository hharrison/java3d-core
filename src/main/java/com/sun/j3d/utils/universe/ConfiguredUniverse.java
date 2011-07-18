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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.media.j3d.*;

/**
 * This utility class creates all the necessary objects on the view side of
 * the scene graph.  Specifically, it creates a Locale, one or more
 * ViewingPlatforms, and at least one Viewer object.<p>
 *
 * ConfiguredUniverse can set up a viewing environment based upon the contents
 * of a configuration file.  This allows an application to run without change
 * across a broad range of viewing configurations, such as windows on
 * conventional desktops, stereo-enabled views, full screen immersive displays
 * on single or multiple screens, or virtual reality installations including
 * cave and head-mounted displays incorporating 6 degree of freedom sensor
 * devices.<p>
 *
 * A configuration file may create InputDevice, Sensor, and
 * ViewPlatformBehavior instances as well as Viewers and ViewingPlatforms.  At
 * least one Viewer must be provided by the configuration.  If a
 * ViewingPlatform is not provided, a default one will be created and the
 * Viewer will be attached to it.<p>
 *
 * A configuration file may be specified directly by passing a URL to a
 * ConfiguredUniverse constructor.  Alternatively, a ConfigContainer may be
 * created from a configuration file first, and then passed to an appropriate
 * ConfiguredUniverse constructor.  The latter technique allows Java system
 * properties that affect Java 3D to be specified in the configuration file,
 * as long as no references to a VirtualUniverse are made before creating the
 * container.<p>
 *
 * If a configuration file or container is not provided, then
 * ConfiguredUniverse creates a default viewing environment in the same way as
 * SimpleUniverse.  If one or more Canvas3D objects are provided, it will use
 * them instead of creating new ones.  All of the constructors provided by
 * SimpleUniverse are also available here.<p>
 * 
 * The syntax and description of the configuration file may be found
 * <A href="doc-files/config-syntax.html">here.</a> Example config files can
 * be found <A href="doc-files/config-examples.html">here.</a>
 *
 * @see Locale
 * @see Viewer
 * @see ViewingPlatform
 * @see ConfigContainer
 * @see <a href="doc-files/config-syntax.html">
 *      The Java 3D Configuration File</a>
 * @see <a href="doc-files/config-examples.html">
 *      Example Configuration Files</a>
 *
 * @since Java 3D 1.3
 */
public class ConfiguredUniverse extends SimpleUniverse {

    /**
     * The configuration instance for this universe.
     */
    private ConfigContainer configContainer = null;

    /**
     * Equivalent to <code>SimpleUniverse()</code>.  Creates a
     * Locale, a single ViewingPlatform, and a Viewer object.
     *
     * @see SimpleUniverse#SimpleUniverse()
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse() {
	super();
    }

    /**
     * Equivalent to <code>SimpleUniverse(int)</code>.
     * Creates a Locale, a single ViewingPlatform with the specified number of
     * transforms, and a Viewer object.
     *
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     *
     * @see SimpleUniverse#SimpleUniverse(int)
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(int transformCount) {
	super(transformCount);
    }

    /**
     * Equivalent to <code>SimpleUniverse(Canvas3D)</code>.
     * Creates a Locale, a single ViewingPlatform, and a Viewer object using
     * the given Canvas3D instance.
     *
     * @param canvas the canvas to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     *
     * @see SimpleUniverse#SimpleUniverse(Canvas3D)
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse(Canvas3D canvas) {
	super(canvas);
    }

    /**
     * Equivalent to <code>SimpleUniverse(Canvas3D, int)</code>.
     * Creates a Locale, a single ViewingPlatform with the specified number of
     * transforms, and a Viewer object with the given Canvas3D.
     *
     * @param canvas the canvas to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     *
     * @see SimpleUniverse#SimpleUniverse(Canvas3D, int)
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(Canvas3D canvas, int transformCount) {
	super(canvas, transformCount);
    }

    /**
     * Equivalent to <code>SimpleUniverse(ViewingPlatform, Viewer)</code>.
     * Creates the view side of the scene graph with the given ViewingPlatform
     * and Viewer.
     *
     * @param viewingPlatform the viewingPlatform to use to create
     *  the view side of the scene graph
     * @param viewer the viewer object to use to create
     *  the view side of the scene graph
     *
     * @see SimpleUniverse#SimpleUniverse(ViewingPlatform, Viewer)
     * @see ViewingPlatform
     * @see Viewer
     */
    public ConfiguredUniverse(ViewingPlatform viewingPlatform, Viewer viewer) {
        super(viewingPlatform, viewer, null);
    }

    /**
     * Equivalent to <code>SimpleUniverse(ViewingPlatform, Viewer,
     * LocalFactory)</code>.  Creates the view side of the scene graph with
     * the given ViewingPlatform, Viewer, and Locale created by the specified
     * LocaleFactory.
     *
     * @param viewingPlatform the viewingPlatform to use to create
     *  the view side of the scene graph
     * @param viewer the viewer object to use to create
     *  the view side of the scene graph
     * @param localeFactory the factory object used to create the Locale
     *
     * @see SimpleUniverse#SimpleUniverse(ViewingPlatform, Viewer,
     *  LocaleFactory)
     * @see ViewingPlatform
     * @see Viewer
     * @see LocaleFactory
     */
    public ConfiguredUniverse(ViewingPlatform viewingPlatform, Viewer viewer,
			      LocaleFactory localeFactory ) {
        super(viewingPlatform, viewer, localeFactory);
    }

    /**
     * Creates a Locale, a single ViewingPlatform, and a Viewer object from
     * the given array of Canvas3D instances.  
     *
     * @param canvases the canvases to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse(Canvas3D[] canvases) {
        this(1, canvases, null, null, null, true);
    }

    /**
     * Creates a Locale, a single ViewingPlatform with the specified number of
     * transforms, and a Viewer object using the given array of Canvas3D
     * instances.  
     *
     * @param canvases the canvases to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(Canvas3D[] canvases, int transformCount) {
	this(transformCount, canvases, null, null, null, true);
    }

    /**
     * Creates a Locale, a single ViewingPlatform with the specified number of
     * transforms, and a Viewer object using the given array of Canvas3D
     * instances.  
     *
     * @param canvases the canvases to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     * @param localeFactory the factory object used to create the Locale
     *
     * @since Java 3D 1.5.1
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(Canvas3D[] canvases, int transformCount, LocaleFactory localeFactory ) {
	this(transformCount, canvases, null, localeFactory, null, true);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale,
     * one or more ViewingPlatforms, and at least one Viewer object.  The
     * configuration file may also create InputDevice, Sensor, and
     * ViewPlatformBehavior instances.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer and ViewingPlatform
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse(URL userConfig) {
        this(1, null, userConfig, null, null, true);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale,
     * one or more ViewingPlatforms with the specified number of transforms,
     * and at least one Viewer object.  The configuration file may also create
     * InputDevice, Sensor, and ViewPlatformBehavior instances.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer and ViewingPlatform with the specified
     *  number of transforms
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup objects to be created
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(URL userConfig, int transformCount) {
	this(transformCount, null, userConfig, null, null, true);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale,
     * one or more ViewingPlatforms with the specified number of transforms,
     * and at least one Viewer object with optional visibility.  AWT
     * components used by the Viewers will remain invisible unless the
     * <code>setVisible</code> flag is true.  The configuration file may also
     * create InputDevice, Sensor, and ViewPlatformBehavior instances.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer with the specified visibility and a
     *  ViewingPlatform with the specified number of transforms
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     * @param setVisible if true, calls <code>setVisible(true)</code> on all
     *  created window components; otherwise, they remain invisible
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(URL userConfig,
			      int transformCount, boolean setVisible) {
	this(transformCount, null, userConfig, null, null, setVisible);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale
     * using the given LocaleFactory, one or more ViewingPlatforms, and at
     * least one Viewer object.  The configuration file may also create
     * InputDevice, Sensor, and ViewPlatformBehavior instances.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer and ViewingPlatform with the specified
     *  number of transforms
     * @param localeFactory the factory object used to create the Locale
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse(URL userConfig, LocaleFactory localeFactory) {
        this(1, null, userConfig, localeFactory, null, true);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale
     * using the given LocaleFactory, one or more ViewingPlatforms, and at
     * least one Viewer object with optional visibility.  The configuration
     * file may also create InputDevice, Sensor, and ViewPlatformBehavior
     * instances.  Window components used by the Viewers will remain invisible
     * unless the <code>setVisible</code> flag is true.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer with the specified visibility and a
     *  default ViewingPlatform
     * @param localeFactory the factory object used to create the Locale
     * @param setVisible if true, calls <code>setVisible(true)</code> on all
     *  created window components; otherwise, they remain invisible
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     */
    public ConfiguredUniverse(URL userConfig,
			      LocaleFactory localeFactory,
			      boolean setVisible) {
        this(1, null, userConfig, localeFactory, null, setVisible);
    }

    /**
     * Reads the configuration specified by the given URL to create a Locale
     * using the specified LocaleFactory with the given origin, one or more
     * ViewingPlatforms with the specified number of transforms, and at least
     * one Viewer object with optional visibility.  Window components used by
     * the Viewers will remain invisible unless the <code>setVisible</code>
     * flag is true.  The configuration file may also create InputDevice,
     * Sensor, and ViewPlatformBehavior instances.
     *
     * @param userConfig the URL to the user's configuration file; passing in
     *  null creates a default Viewer with the specified visibility and a
     *  ViewingPlatform with the specified number of transforms
     * @param localeFactory the factory object used to create the Locale
     * @param origin the origin used to set the origin of the Locale object;
     *  if this object is null, then 0.0 is used
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     * @param setVisible if true, calls <code>setVisible(true)</code> on all
     *  created window components; otherwise, they remain invisible
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    public ConfiguredUniverse(URL userConfig, LocaleFactory localeFactory,
			      HiResCoord origin, int transformCount,
			      boolean setVisible) {

        this(transformCount, null, userConfig,
	     localeFactory, origin, setVisible);
    }

    /**
     * Retrieves view-side scenegraph components from the given container to
     * create a universe with one Locale, one or more ViewingPlatforms, and at
     * least one Viewer object.  Equivalent to
     * <code>ConfiguredUniverse(ConfigContainer, null, null)</code>.
     *
     * @param userConfig container holding viewing configuration components;
     *  must not be null
     *
     * @see #ConfiguredUniverse(ConfigContainer, LocaleFactory, HiResCoord)
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @since Java 3D 1.3.1
     */
    public ConfiguredUniverse(ConfigContainer userConfig) {
	this(userConfig, null, null);
    }

    /**
     * Retrieves view-side scenegraph components from the given container to
     * create a universe with one Locale created from the specified
     * LocaleFactory and origin, one or more ViewingPlatforms, and at least
     * one Viewer object.  The container may also provide InputDevice, Sensor,
     * and ViewPlatformBehavior instances which will be incorporated into the
     * universe if they are referenced by any of the Viewer or ViewingPlatform
     * instances.<p>
     *
     * This constructor and <code>ConfiguredUniverse(ConfigContainer)</code>
     * both accept ConfigContainer references directly and are the preferred
     * interfaces for constructing universes from configuration files.  They
     * differ from the constructors that accept URL objects in the
     * following ways:<p>
     * <ul>
     * <li>A Viewer will be attached to a default ViewingPlatform only if
     *     no ViewingPlatforms are provided in the ConfigContainer.  If one
     *     or more ViewingPlatforms are provided by the ConfigContainer, then
     *     Viewers must be attached to them explicitly in the configuration.<p>
     * </li>
     * <li>ViewPlatformBehaviors will be attached to their specified
     *     ViewingPlatforms before ConfiguredUniverse can set a reference to
     *     itself in the ViewingPlatform.  This means that a behavior can't
     *     get a reference to the universe at the time its
     *     <code>setViewingPlatform</code> method is called; it must wait
     *     until its <code>initialize</code> method is called.<p>
     * </li>
     * <li>All Java properties used by Java 3D may be set in the beginning of
     *     the configuration file as long as there is no reference to a
     *     VirtualUniverse prior to creating the ConfigContainer.  Note
     *     however, that some Java 3D utilities and objects such as
     *     Transform3D can cause static references to VirtualUniverse and
     *     trigger the evaluation of Java properties before they are set by
     *     ConfigContainer.<p>
     * </li>
     * </ul>
     * @param userConfig container holding viewing configuration components;
     *  must not be null
     * @param localeFactory the factory object used to create the Locale, or
     *  null 
     * @param origin the origin used to set the origin of the Locale object;
     *  if this object is null, then 0.0 is used
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @since Java 3D 1.3.1
     */
    public ConfiguredUniverse(ConfigContainer userConfig,
			      LocaleFactory localeFactory,
			      HiResCoord origin) {

        super(origin, localeFactory);
	configContainer = userConfig;

	Collection c = configContainer.getViewers();
	if (c == null || c.size() == 0)
	    throw new IllegalArgumentException(
                    "no views defined in configuration file");

	viewer = (Viewer[])c.toArray(new Viewer[1]);
	
	c = configContainer.getViewingPlatforms();
	if (c == null || c.size() == 0) {
	    createDefaultViewingPlatform
		(configContainer.getViewPlatformTransformCount());
	}
	else {
	    Iterator i = c.iterator();
	    while (i.hasNext()) {
		ViewingPlatform vp = (ViewingPlatform)i.next();
		vp.setUniverse(this);
		locale.addBranchGraph(vp);
	    }
	}
    }

    /**
     * Package-scope constructor that creates the view side of the
     * scene graph.  The passed in parameters override the default
     * values where appropriate.  Note that the userCanvases parameter
     * is ignored when the userConfig is non-null.
     *
     * @param transformCount the number of transforms in the
     *  MultiTransformGroup object to be created
     * @param canvases the canvases to associate with the Viewer object;
     *  passing in null will cause this parameter to be ignored and a canvas 
     *  to be created by the utility
     * @param userConfig the URL to the user's configuration file; passing in
     *  null causes the default values to be used.
     * @param localeFactory the factory object used to create the Locale
     * @param origin the origin used to set the origin of the Locale object;
     *  if this object is null, then 0.0 is used
     * @param setVisible if true, calls <code>setVisible(true)</code> on all
     *  created window components; otherwise, they remain invisible
     *
     * @see Locale
     * @see Viewer
     * @see ViewingPlatform
     * @see MultiTransformGroup
     */
    ConfiguredUniverse(int transformCount,
		       Canvas3D[] canvases,
		       URL userConfig,
		       LocaleFactory localeFactory,
		       HiResCoord origin,
		       boolean setVisible) {

        super(origin, localeFactory);

	if (userConfig == null) {
	    viewer = new Viewer[1];
	    viewer[0] = new Viewer(canvases, null, null, setVisible);
	    createDefaultViewingPlatform(transformCount);
	}
	else {
	    // Create a ConfigContainer without attaching behaviors.  The
	    // package-scope constructor is used for backward compatibility.
	    configContainer = new ConfigContainer
		(userConfig, setVisible, transformCount, false);

	    Collection c = configContainer.getViewers();
	    if (c == null || c.size() == 0)
		throw new IllegalArgumentException(
                        "no views defined in configuration file");

	    viewer = (Viewer[])c.toArray(new Viewer[1]);

	    // Get ViewingPlatforms from the ConfigContainer and add them to
	    // the locale.  The package-scoped findConfigObjects() accesor is
	    // used so that backward compatibility can be maintained for older
	    // configuration files.
	    c = configContainer.findConfigObjects("ViewPlatform");
	    if (c == null || c.size() == 0) {
		createDefaultViewingPlatform(transformCount);
	    }
	    else {
		Iterator i = c.iterator();
		while (i.hasNext()) {
		    ConfigViewPlatform cvp = (ConfigViewPlatform)i.next();
		    ViewingPlatform vp = cvp.viewingPlatform;

		    // For backward compatibility, handle the default
		    // attachment of one Viewer to one ViewingPlatform.  If
		    // there are multiple Viewers and ViewingPlatforms then
		    // attachments must be made explicitly in the config file.
		    if (vp.getViewers() == null &&
			viewer.length == 1 && c.size() == 1) {
			if (cvp.viewAttachPolicy == -1) {
			    setDerivedAttachPolicy(viewer[0], vp) ;
			}
			viewer[0].setViewingPlatform(vp);
		    }
		    vp.setUniverse(this);
		    locale.addBranchGraph(vp);

		    // If there's a behavior associated with the platform,
		    // attach it now after the setting the universe reference.
		    cvp.processBehavior();
		}
	    }
	}
    }

    /**
     * Creates a default ViewingPlatform, attaches the first Viewer, and then
     * attaches the platform to the Locale.
     *
     * @param transformCount number of TransformGroups to create in the
     *  ViewingPlatform 
     */
    private void createDefaultViewingPlatform(int transformCount) {
	ViewingPlatform vp = new ViewingPlatform(transformCount);
	setDerivedAttachPolicy(viewer[0], vp);
	viewer[0].setViewingPlatform(vp);
	vp.setUniverse(this);
	locale.addBranchGraph(vp);
    }

    /**
     * Sets a view attach policy appropriate for a window eyepoint policy.
     * 
     * @param v Viewer to which the ViewingPlatform will be attached
     * @param vp ViewingPlatform to which the Viewer will be attached
     */
    private void setDerivedAttachPolicy(Viewer v, ViewingPlatform vp) {
	if (v.getView().getWindowEyepointPolicy() !=
	    View.RELATIVE_TO_FIELD_OF_VIEW) {
	    vp.getViewPlatform().setViewAttachPolicy(View.NOMINAL_SCREEN);
	}
    }


    /**
     * Returns the Viewer object specified by the given index.
     *
     * @param index The index of which Viewer object to return.
     * 
     * @return The Viewer object specified by the given index.
     */
    public Viewer getViewer(int index) {
        return viewer[index];
    }

    /**
     * Returns all of the Viewer objects associated with this scene graph.  
     * 
     * @return The Viewer objects associated with this scene graph.
     */
    public Viewer[] getViewers() {
	Viewer[] ret = new Viewer[viewer.length];
	for (int i = 0; i < viewer.length; i++) {
	    ret[i] = viewer[i];
	}
        return ret;
    }

    /**
     * Call <code>setVisible()</code> on all AWT components created by this
     * ConfiguredUniverse instance.<p>
     *
     * @param visible boolean to be passed to the <code>setVisible()</code>
     *  calls on the window components created by this
     *  ConfiguredUniverse instance
     */
    public void setVisible(boolean visible) {
	for (int i = 0; i < viewer.length; i++)
	    if (viewer[i] != null)
		viewer[i].setVisible(visible);
    }

    /**
     * Returns the config file URL based on system properties.  This is
     * equivalent to calling <code>ConfigContainer.getConfigURL()</code>.  The
     * current implementation of this method parses the j3d.configURL property
     * as a URL string.  For example, the following command line would specify
     * that the config file is taken from the file "j3dconfig" in the current
     * directory:
     * <ul>
     * <code>java -Dj3d.configURL=file:j3dconfig ...</code>
     * </ul>
     *
     * @return the URL of the config file; null is returned if no valid
     *  URL is defined by the system properties
     */
    public static URL getConfigURL() {
	return ConfigContainer.getConfigURL(null);
    }

    /**
     * Returns the config file URL based on system properties.  This is the
     * same as calling <code>ConfigContainer.getConfigURL(String)</code>.  The
     * current implementation of this method parses the j3d.configURL property
     * as a URL string.  For example, the following command line would specify
     * that the config file is taken from the file "j3dconfig" in the current
     * directory:
     * <ul>
     * <code>java -Dj3d.configURL=file:j3dconfig ...</code>
     * </ul>
     *
     * @param defaultURLString the default string used to construct
     *  the URL if the appropriate system properties are not defined
     * @return the URL of the config file; null is returned if no
     *  valid URL is defined either by the system properties or the
     *  default URL string
     */
    public static URL getConfigURL(String defaultURLString) {
	return ConfigContainer.getConfigURL(defaultURLString);
    }

    /**
     * Returns all named Sensors defined by the configuration file used to
     * create the ConfiguredUniverse, if any.  Equivalent to
     * <code>getConfigContainer().getNamedSensors()</code>.<p>
     *
     * With the sole exception of the Sensor assigned to the head tracker,
     * none of the Sensors defined in the configuration file are placed into
     * the Sensor array maintained by PhysicalEnvironment.  The head tracker
     * Sensor is the only one read by the Java 3D core and must generate reads
     * with a full 6 degrees of freedom (3D position and 3D orientation).<p>
     * 
     * Other Sensors need not generate reads with a full 6 degrees of freedom,
     * although their reads must be expressed using Transform3D.  Some
     * joysticks may provide only 2D relative X and Y axis movement; dials,
     * levers, and sliders are 1D devices, and some devices may combine dials
     * and levers to generate 3D positional data.<p>
     *
     * The index names to identify left / right / dominant / non-dominant hand
     * Sensors in the PhysicalEnvironement Sensor array are not adequate to
     * distinguish these differences, so this method allows applications to
     * look up Sensors based on the names bound to them in the configuration
     * file.  There are no set rules on naming.  Applications that use Sensors
     * may set up conventions for generic devices such as "mouse6D" or
     * "joystick2D" or specific product names.<p>
     *
     * @return read-only Map which maps Sensor names to the associated Sensors,
     *  or null if no Sensors have been named
     */
    public Map getNamedSensors() {
	if (configContainer == null)
	    return null;
	else
	    return configContainer.getNamedSensors();
    }

    /**
     * Returns all named ViewPlatformBehaviors defined by the configuration
     * file used to create the ConfiguredUniverse, if any.  Equivalent
     * to <code>getConfigContainer().getNamedViewPlatformBehaviors()</code>.<p>
     *
     * @return read-only Map which maps behavior names to the associated
     *  ViewPlatformBehavior instances, or null if none have been named.
     * @since Java 3D 1.3.1
     */
    public Map getNamedBehaviors() {
	if (configContainer == null)
	    return null;
	else
	    return configContainer.getNamedViewPlatformBehaviors();
    }

    /**
     * Returns a container holding all the objects defined by the
     * configuration file used to create the ConfiguredUniverse.
     *
     * @return the container
     * @since Java 3D 1.3.1
     */
    public ConfigContainer getConfigContainer() {
	return configContainer;
    }

    /**
     * Cleanup memory references used by ConfiguredUniverse.
     * @since Java 3D 1.3.1
     */
    public void cleanup() {
	if (viewer != null) {
	    for (int i = 0 ; i < viewer.length ; i++) {
		viewer[i].getView().removeAllCanvas3Ds();
		viewer[i].setViewingPlatform(null);
		viewer[i] = null;
	    }
	}

	locale = null;
	removeAllLocales();
	Viewer.clearViewerMap();

	configContainer.clear();
	configContainer = null;
    }
}
