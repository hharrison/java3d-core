/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A VirtualUniverse object is the top-level container for all scene
 * graphs.  A virtual universe consists of a set of Locale objects,
 * each of which has a high-resolution position within the virtual
 * universe.  An application or applet may have more than one
 * VirtualUniverse objects, but many applications will need only one.
 * Virtual universes are separate entities in that no node object may
 * exist in more than one virtual universe at any one time. Likewise,
 * the objects in one virtual universe are not visible in, nor do they
 * interact with objects in, any other virtual universe.
 * <p>
 * A VirtualUniverse object defines methods to enumerate its Locale
 * objects and to remove them from the virtual universe.
 *
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a> and
 * <a href="doc-files/VirtualUniverse.html">Scene Graph Superstructure</a>
 * documents.
 *
 * @see Locale
 */

public class VirtualUniverse extends Object {
    // NOTE TO DEVELOPERS:
    //
    // Developers who modify Java 3D in any way should modify
    // the auxiliary implementation vendor string in VersionInfo.java.
    // See that file for instructions.

    // The global MasterControl object.  There is only one of these
    // for all of Java 3D.
    static MasterControl mc = null;

    // The lock to acquire before traversing the scene graph
    Object sceneGraphLock = new Object();
    Object behaviorLock = new Object();

// A list of locales that are contained within this universe
Vector<Locale> listOfLocales = new Vector<Locale>();

// The list of view platforms.
ArrayList<ViewPlatformRetained> viewPlatforms = new ArrayList<ViewPlatformRetained>();

// The cached list of vp's
ViewPlatformRetained[] viewPlatformList = null;

    // A flag that indicates that the list of view platforms has changed
    boolean vpChanged = false;

    // The list of backgrounds
    Vector backgrounds = new Vector();

    // The list of clips
    Vector clips = new Vector();

    // The list of sounds
    Vector sounds = new Vector();

    // The list of soundscapes
    Vector soundscapes = new Vector();

    // The Behavior Scheduler Thread for this Virtual Universe.
    BehaviorScheduler behaviorScheduler = null;


    // The geometry structure for this Universe
    GeometryStructure geometryStructure = null;

    // The transform structure for this Universe
    TransformStructure transformStructure = null;

    // The behavior structure for this Universe
    BehaviorStructure behaviorStructure = null;

    // The sound structure for this Universe
    SoundStructure soundStructure = null;

    // The rendering attributes structure for this Universe
    RenderingEnvironmentStructure renderingEnvironmentStructure = null;

    // Reference count of users of the RenderingEnvironmentStructure
    int renderingEnvironmentStructureRefCount = 0;

    // This is a global counter for node id's.
    long nodeIdCount = 0;

    // This is a global counter for view id's.
    int viewIdCount = 0;

    // This is a vector of free nodeid's
    Vector nodeIdFreeList = new Vector();

    // This is a vector of free viewid's
    ArrayList viewIdFreeList = new ArrayList();

    // The number of nodes in this universe
    int numNodes = 0;

    // The State object used when branch graphs are added
    SetLiveState setLiveState;

    // This is an array of references to objects that need their mirror
    // copies updated.  It is updated by the traverser and emptied by
    // the view thread.
    ObjectUpdate[] updateObjects = new ObjectUpdate[16];

    // The number of valid entries in updateObjects
    int updateObjectsLen = 0;

    // A list of all mirror geometry object that are dirty
    ArrayList dirtyGeomList = new ArrayList();

    // The current primary view for this universe
    View currentView;

    // A flag to indicate that we are in a behavior routine
    boolean inBehavior = false;

    // Flags to indicate if events need to be delivered
    boolean enableComponent = false;
    boolean enableFocus = false;
    boolean enableKey = false;
    boolean enableMouse = false;
    boolean enableMouseMotion = false;
    boolean enableMouseWheel = false;

    // Keep track of how many active View use this universe
    int activeViewCount = 0;

    // Root ThreadGroup for creating Java 3D threads
    static ThreadGroup rootThreadGroup;

    // Properties object for getProperties
    private static J3dQueryProps properties = null;

    // Flag to indicate that user thread has to
    // stop until MC completely register/unregister View.
    View regViewWaiting = null;
    View unRegViewWaiting = null;
    boolean isSceneGraphLock = false;

    private Object waitLock = new Object();

    // Set of scene graph structure change listeners
    private HashSet<GraphStructureChangeListener> structureChangeListenerSet = null;

    // Set of shader error listeners
    private HashSet<ShaderErrorListener> shaderErrorListenerSet = null;
    private ShaderErrorListener defaultShaderErrorListener =
	ShaderProgram.getDefaultErrorListener();

    // Set of rendering error listeners
    private static HashSet<RenderingErrorListener> renderingErrorListenerSet = null;
    private static RenderingErrorListener defaultRenderingErrorListener =
	Renderer.getDefaultErrorListener();

    /**
     * Constructs a new VirtualUniverse.
     */
    public VirtualUniverse() {
	setLiveState = new SetLiveState(this);
	initMCStructure();
    }


    void initMCStructure() {
	if (geometryStructure != null) {
	    geometryStructure.cleanup();
	}
        geometryStructure = new GeometryStructure(this);
	if (transformStructure != null) {
	    transformStructure.cleanup();
	}
        transformStructure = new TransformStructure(this);
	if (behaviorStructure != null) {
	    behaviorStructure.cleanup();
	}
        behaviorStructure = new BehaviorStructure(this);
	if (soundStructure != null) {
	    soundStructure.cleanup();
	}
        soundStructure = new SoundStructure(this);
	if (renderingEnvironmentStructure != null) {
	    renderingEnvironmentStructure.cleanup();
	}
        renderingEnvironmentStructure = new
	            RenderingEnvironmentStructure(this);

    }

    /**
     * Initialize the native interface and anything else that needs
     * to be initialized.
     */
    static void loadLibraries() {
	// No need to do anything.  The act of calling any method in this
	// class is sufficient to cause the static MasterControl object
	// to be created which, in turn, loads the native libraries.
    }

    static {
        boolean isLoggableConfig = MasterControl.isCoreLoggable(Level.CONFIG);
        Logger logger = MasterControl.getCoreLogger();

        // Print out version information unless this is a
        // non-debuggable, release (fcs) build
        if (isLoggableConfig || J3dDebug.devPhase || VersionInfo.isDebug) {
            StringBuffer strBuf = new StringBuffer("3D ");
	    if (J3dDebug.devPhase) {
		strBuf.append("[dev] ");
	    }
            strBuf.append(VersionInfo.getVersion());
            String str = strBuf.toString();
            if (isLoggableConfig) {
                logger.config(str);
            } else {
                System.err.println(str);
                System.err.println();
            }
	}

	// Print out debugging information for debug builds
	if (isLoggableConfig || VersionInfo.isDebug) {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("Initializing 3D runtime system:\n").
                    append("    version = ").
                    append(VersionInfo.getVersion()).
                    append("\n").
                    append("    vendor = ").
                    append(VersionInfo.getVendor()).
                    append("\n").
                    append("    specification.version = ").
                    append(VersionInfo.getSpecificationVersion()).
                    append("\n").
                    append("    specification.vendor = ").
                    append(VersionInfo.getSpecificationVendor());
            String str = strBuf.toString();
            if (isLoggableConfig) {
                logger.config(str);
            } else {
                System.err.println(str);
                System.err.println();
            }
	}

	// Java 3D cannot run in headless mode, so we will throw a
	// HeadlessException if isHeadless() is true. This avoids a
	// cryptic error message from MasterControl.loadLibraries().
	if (java.awt.GraphicsEnvironment.isHeadless()) {
	    throw new java.awt.HeadlessException();
	}

	// Load the native libraries and create the static
	// MasterControl object
	MasterControl.loadLibraries();
	mc = new MasterControl();

        // Print out debugging information for debug builds
        if (isLoggableConfig || VersionInfo.isDebug) {
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("3D system initialized\n").
                    append("    rendering pipeline = ").
                    append(Pipeline.getPipeline().getPipelineName());
            String str = strBuf.toString();
            if (isLoggableConfig) {
                logger.config(str);
            } else {
                System.err.println(str);
                System.err.println();
            }
        }
    }

    /**
     * Adds a locale at the end of list of locales
     * @param locale the locale to be added
     */
    void addLocale(Locale locale) {
	listOfLocales.addElement(locale);
    }

    /**
     * Removes a Locale and its associates branch graphs from this
     * universe.  All branch graphs within the specified Locale are
     * detached, regardless of whether their ALLOW_DETACH capability
     * bits are set.  The Locale is then marked as being dead: no
     * branch graphs may subsequently be attached.
     *
     * @param locale the Locale to be removed.
     *
     * @exception IllegalArgumentException if the specified Locale is not
     * attached to this VirtualUniverse.
     *
     * @since Java 3D 1.2
     */
    public void removeLocale(Locale locale) {
	if (locale.getVirtualUniverse() != this) {
	    throw new IllegalArgumentException(J3dI18N.getString("VirtualUniverse0"));
	}

	listOfLocales.removeElement(locale);
	locale.removeFromUniverse();
	if (isEmpty()) {
	    VirtualUniverse.mc.postRequest(MasterControl.EMPTY_UNIVERSE,
					   this);
	}
	setLiveState.reset(null);
    }


    /**
     * Removes all Locales and their associates branch graphs from
     * this universe.  All branch graphs within each Locale are
     * detached, regardless of whether their ALLOW_DETACH capability
     * bits are set.  Each Locale is then marked as being dead: no
     * branch graphs may subsequently be attached.  This method
     * should be called by applications and applets to allow
     * Java 3D to cleanup its resources.
     *
     * @since Java 3D 1.2
     */
    public void removeAllLocales() {
	// NOTE: this is safe because Locale.removeFromUniverse does not
	// remove the Locale from the listOfLocales
	int i;


	for (i = listOfLocales.size() - 1; i > 0; i--) {
		listOfLocales.get(i).removeFromUniverse();
	}

	if (i >= 0) {
	    // We have to clear() the listOfLocales first before
	    // invoke the last removeFromUniverse() so that isEmpty()
	    // (call from View.deactivate() ) will return true and
	    // threads can destroy from MC.
		Locale loc = listOfLocales.get(0);
	    listOfLocales.clear();
	    loc.removeFromUniverse();
	}
	VirtualUniverse.mc.postRequest(MasterControl.EMPTY_UNIVERSE,
				       this);

	setLiveState.reset(null);
    }


/**
 * Returns the enumeration object of all locales in this virtual universe.
 * 
 * @return the enumeration object
 */
public Enumeration<Locale> getAllLocales() {
	return this.listOfLocales.elements();
}

    /**
     * Returns the number of locales.
     * @return the count of locales
     */
    public int numLocales() {
	return this.listOfLocales.size();
    }


    /**
     * Sets the priority of all Java 3D threads to the specified
     * value.  The default value is the priority of the thread that
     * started Java 3D.
     *
     * @param priority the new thread priority
     *
     * @exception IllegalArgumentException if the priority is not in
     * the range MIN_PRIORITY to MAX_PRIORITY
     *
     * @exception SecurityException if the priority is greater than
     * that of the calling thread
     *
     * @since Java 3D 1.2
     */
    public static void setJ3DThreadPriority(int priority) {
	if (priority > Thread.MAX_PRIORITY) {
	    priority = Thread.MAX_PRIORITY;
	} else if (priority < Thread.MIN_PRIORITY) {
	    priority = Thread.MIN_PRIORITY;
	}
	VirtualUniverse.mc.setThreadPriority(priority);
    }


    /**
     * Retrieves that priority of Java 3D's threads.
     *
     * @return the current priority of Java 3D's threads
     *
     * @since Java 3D 1.2
     */
    public static int getJ3DThreadPriority() {
	return VirtualUniverse.mc.getThreadPriority();
    }


    /**
     * Returns a read-only Map object containing key-value pairs that
     * define various global properties for Java 3D.  All of the keys
     * are String objects.  The values are key-specific, but most will
     * be String objects.
     *
     * <p>
     * The set of global Java 3D properties always includes values for
     * the following keys:
     *
     * <p>
     * <ul>
     * <table BORDER=1 CELLSPACING=1 CELLPADDING=1>
     * <tr>
     * <td><b>Key (String)</b></td>
     * <td><b>Value Type</b></td>
     * </tr>
     * <tr>
     * <td><code>j3d.version</code></td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td><code>j3d.vendor</code></td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td><code>j3d.specification.version</code></td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td><code>j3d.specification.vendor</code></td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td><code>j3d.pipeline</code></td>
     * <td>String</td>
     * </tr>
     * <tr>
     * <td><code>j3d.renderer</code></td>
     * <td>String</td>
     * </tr>
     * </table>
     * </ul>
     *
     * <p>
     * The descriptions of the values returned for each key are as follows:
     *
     * <p>
     * <ul>
     *
     * <li>
     * <code>j3d.version</code>
     * <ul>
     * A String that defines the Java 3D implementation version.
     * The portion of the implementation version string before the first
     * space must adhere to one of the the following three formats
     * (anything after the first space is an optional free-form addendum
     * to the version):
     * <ul>
     * <i>x</i>.<i>y</i>.<i>z</i><br>
     * <i>x</i>.<i>y</i>.<i>z</i>_<i>p</i><br>
     * <i>x</i>.<i>y</i>.<i>z</i>-<i>ssss</i><br>
     * </ul>
     * where:
     * <ul>
     * <i>x</i> is the major version number<br>
     * <i>y</i> is the minor version number<br>
     * <i>z</i> is the sub-minor version number<br>
     * <i>p</i> is the patch revision number <br>
     * <i>ssss</i> is a string, identifying a non-release build
     * (e.g., beta1, build47, rc1, etc.).  It may only
     * contain letters, numbers, periods, dashes, or
     * underscores.
     * </ul>
     * </ul>
     * </li>
     * <p>
     *
     * <li>
     * <code>j3d.vendor</code>
     * <ul>
     * String that specifies the Java 3D implementation vendor.
     * </ul>
     * </li>
     * <p>
     *
     * <li>
     * <code>j3d.specification.version</code>
     * <ul>
     * A String that defines the Java 3D specification version.
     * This string must be of the following form:
     * <ul>
     * <i>x</i>.<i>y</i>
     * </ul>
     * where:
     * <ul>
     * <i>x</i> is the major version number<br>
     * <i>y</i> is the minor version number<br>
     * </ul>
     * No other characters are allowed in the specification version string.
     * </ul>
     * </li>
     * <p>
     *
     * <li>
     * <code>j3d.specification.vendor</code>
     * <ul>
     * String that specifies the Java 3D specification vendor.
     * </ul>
     * </li>
     * <p>
     *
     * <li>
     * <code>j3d.pipeline</code>
     * <ul>
     * String that specifies the Java 3D rendering pipeline. This could
     * be one of: "NATIVE_OGL", "NATIVE_D3D", or "JOGL". Others could be
     * added in the future.
     * </ul>
     * </li>
     * <p>
     *
     * <li>
     * <code>j3d.renderer</code>
     * <ul>
     * String that specifies the underlying rendering library.  This could
     * be one of: "OpenGL" or "DirectX". Others could be added in the future.
     * </ul>
     * </li>
     * <p>
     *
     * </ul>
     *
     * @return the global Java 3D properties
     *
     * @since Java 3D 1.3
     */
    public static final Map getProperties() {
	if (properties == null) {
	    // Create lists of keys and values
	    ArrayList keys = new ArrayList();
	    ArrayList values = new ArrayList();

            // Implementation version string is obtained from the
            // ImplementationVersion class.
	    keys.add("j3d.version");
	    values.add(VersionInfo.getVersion());

	    keys.add("j3d.vendor");
	    values.add(VersionInfo.getVendor());

	    keys.add("j3d.specification.version");
	    values.add(VersionInfo.getSpecificationVersion());

	    keys.add("j3d.specification.vendor");
	    values.add(VersionInfo.getSpecificationVendor());

	    keys.add("j3d.renderer");
            values.add(Pipeline.getPipeline().getRendererName());

            keys.add("j3d.pipeline");
            values.add(Pipeline.getPipeline().getPipelineName());

	    // Now Create read-only properties object
	    properties =
		new J3dQueryProps((String[]) keys.toArray(new String[0]),
				  values.toArray());
	}
  	return properties;
    }


    /**
     * This returns the next available nodeId as a string.
     */
    // XXXX: reuse of id's imply a slight collision problem in the
    // render queue's.
    // BUG 4181362
    String getNodeId() {
        String str;

	if (nodeIdFreeList.size() == 0) {
	   str = Long.toString(nodeIdCount);
           nodeIdCount++;
	} else {
            // Issue 496: Remove last object using index to avoid performance
            // hit of a needless linear search.
           int idx = nodeIdFreeList.size() - 1;
           str = (String) nodeIdFreeList.remove(idx);
	}
        return(str);
    }

    /**
     * This returns the next available viewId
     */
    Integer getViewId() {
	Integer id;
	int size;

	synchronized (viewIdFreeList) {
	    size = viewIdFreeList.size();
	    if (size == 0) {
		id = new Integer(viewIdCount++);
	    } else {
		id = (Integer) viewIdFreeList.remove(size-1);
	    }
	}
        return(id);
    }

    /**
     * This returns a viewId to the freelist
     */
    void addViewIdToFreeList(Integer viewId) {
	synchronized (viewIdFreeList) {
	    viewIdFreeList.add(viewId);
	}
    }

    void addViewPlatform(ViewPlatformRetained vp) {
	vpChanged = true;
	viewPlatforms.add(vp);
    }

    void removeViewPlatform(ViewPlatformRetained vp) {
	vpChanged = true;
	viewPlatforms.remove(viewPlatforms.indexOf(vp));
    }

synchronized ViewPlatformRetained[] getViewPlatformList() {
	if (vpChanged) {
		viewPlatformList = viewPlatforms.toArray(new ViewPlatformRetained[viewPlatforms.size()]);
		vpChanged = false;
	}
	return viewPlatformList;
}

    void checkForEnableEvents() {
	enableComponentEvents();
	if (enableFocus) {
	    enableFocusEvents();
	}
	if (enableKey) {
	    enableKeyEvents();
	}
	if (enableMouse) {
	    enableMouseEvents();
	}
	if (enableMouseMotion) {
	    enableMouseMotionEvents();
	}
	if (enableMouseWheel) {
	    enableMouseWheelEvents();
	}

    }

    void enableComponentEvents() {
        // Issue 458 - This method is now a noop
        /*
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	View views[];
	Object[] vps = getViewPlatformList();

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
	            cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null) {
		            cv.eventCatcher.enableComponentEvents();
                        }
	            }
		}
	    }
	}
        */
    }

    void disableFocusEvents() {
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	View views[];
	ViewPlatformRetained[] vps = getViewPlatformList();
	enableFocus = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.disableFocusEvents();
	            }
	        }
	    }
	}

    }

    void enableFocusEvents() {
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	View views[];
	ViewPlatformRetained[] vps = getViewPlatformList();
	enableFocus = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.enableFocusEvents();
	            }
	        }
	    }
	}
    }


    void disableKeyEvents() {
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();
	View views[];

	enableKey = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
			    cv.eventCatcher.disableKeyEvents();
	            }
	        }
	    }
	}
    }


    void enableKeyEvents() {
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();
	View views[];

	enableKey = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
			    cv.eventCatcher.enableKeyEvents();
	            }
	        }
	    }
	}
    }


   void disableMouseEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouse = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.disableMouseEvents();
	            }
	        }
	    }
	}
    }

    void enableMouseEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouse = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.enableMouseEvents();
	            }
	        }
	    }
	}
    }


    void disableMouseMotionEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouseMotion = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.disableMouseMotionEvents();
	            }
	        }
	    }
	}
    }

    void enableMouseMotionEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouseMotion = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.enableMouseMotionEvents();
	            }
	        }
	    }
	}
    }

    void disableMouseWheelEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouseWheel = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.disableMouseWheelEvents();
	            }
	        }
	    }
	}
    }

    void enableMouseWheelEvents() {
	Enumeration cvs;
	Canvas3D cv;
	View views[];
        ViewPlatformRetained vp;
	ViewPlatformRetained[] vps = getViewPlatformList();

	enableMouseWheel = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
			vp = vps[i];
		views = vp.getViewList();
		for (int j=views.length-1; j>=0; j--) {
                    cvs = views[j].getAllCanvas3Ds();
	            while(cvs.hasMoreElements()) {
		        cv = (Canvas3D) cvs.nextElement();
                        // offscreen canvas does not have event catcher
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.enableMouseWheelEvents();
	            }
	        }
	    }
	}
    }

    /**
     * Sets the "current" view (during view activation) for this virtual
     * universe.
     * @param last activated view
     */
    final void setCurrentView(View view) {
        this.currentView = view;
    }

    /**
     * Returns the "current" view (the last view activated for this virtual
     * universe.
     * @return last activated view
     */
    final View getCurrentView() {
        return this.currentView;
    }


    /**
     * Method to return the root thread group.  This must be called from
     * within a doPrivileged block.
     */
    static ThreadGroup getRootThreadGroup() {
	return rootThreadGroup;
    }

    /**
     * return true if all Locales under it don't have branchGroup
     * attach to it.
     */
    boolean isEmpty() {
	Enumeration<Locale> elm = listOfLocales.elements();

	while (elm.hasMoreElements()) {
		Locale loc = elm.nextElement();
	    if (!loc.branchGroups.isEmpty()) {
		return false;
	    }
	}
	return true;
    }

    void resetWaitMCFlag() {
	synchronized (waitLock) {
	    regViewWaiting = null;
	    unRegViewWaiting = null;
	    isSceneGraphLock = true;
	}
    }

    void waitForMC() {
	synchronized (waitLock) {
	    if (unRegViewWaiting != null) {
		if ((regViewWaiting == null) ||
		    (regViewWaiting != unRegViewWaiting)) {
		    while (!unRegViewWaiting.doneUnregister) {
		        MasterControl.threadYield();
		    }
		    unRegViewWaiting.doneUnregister = false;
		    unRegViewWaiting = null;
		}
	    }

	    if (regViewWaiting != null) {
		while (!VirtualUniverse.mc.isRegistered(regViewWaiting)) {
		    MasterControl.threadYield();
		}
		regViewWaiting = null;
	    }
	    isSceneGraphLock = false;
	}
    }

    /**
     * Adds the specified GraphStructureChangeListener to the set of listeners
     * that will be notified when the graph structure is changed on a live
     * scene graph. If the specifed listener is null no action is taken and no
     * exception is thrown.
     *
     * @param listener the listener to add to the set.
     *
     * @since Java 3D 1.4
     */
    public void addGraphStructureChangeListener(GraphStructureChangeListener listener) {
        if (listener == null) {
            return;
        }

        if (structureChangeListenerSet == null) {
            structureChangeListenerSet = new HashSet();
        }

        synchronized(structureChangeListenerSet) {
            structureChangeListenerSet.add(listener);
        }
    }

    /**
     * Removes the specified GraphStructureChangeListener from the set of listeners. This
     * method performs no function, nor does it throw an exception if the specified listener
     * is not currently in the set or is null.
     *
     * @param listener the listener to remove from the set.
     *
     * @since Java 3D 1.4
     */
    public void removeGraphStructureChangeListener(GraphStructureChangeListener listener) {
        if (structureChangeListenerSet == null) {
	    return;
	}

        synchronized(structureChangeListenerSet) {
            structureChangeListenerSet.remove(listener);
        }
    }

    /**
     * Processes all live BranchGroup add and removes and notifies
     * any registered listeners. Used for add and remove
     */
    void notifyStructureChangeListeners(boolean add, Object parent, BranchGroup child) {
        if (structureChangeListenerSet == null) {
            return;
	}

        synchronized(structureChangeListenerSet) {
            Iterator<GraphStructureChangeListener> it = structureChangeListenerSet.iterator();
            while(it.hasNext()) {
                GraphStructureChangeListener listener = it.next();
                try {
                    if (add) {
                        listener.branchGroupAdded(parent, child);
                    } else {
                        listener.branchGroupRemoved(parent, child);
                    }
                }
                catch (RuntimeException e) {
                    System.err.println("Exception occurred in GraphStructureChangeListener:");
                    e.printStackTrace();
                }
                catch (Error e) {
                    // Issue 264 - catch Error
                    System.err.println("Error occurred in GraphStructureChangeListener:");
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Processes all live BranchGroup moves and notifies
     * any registered listeners. Used for moveTo
     */
    void notifyStructureChangeListeners(Object oldParent, Object newParent, BranchGroup child) {
        if (structureChangeListenerSet == null) {
            return;
	}

        synchronized(structureChangeListenerSet) {
            Iterator<GraphStructureChangeListener> it = structureChangeListenerSet.iterator();
            while(it.hasNext()) {
                GraphStructureChangeListener listener = it.next();
                try {
                    listener.branchGroupMoved(oldParent, newParent, child);
                }
                catch (RuntimeException e) {
                    System.err.println("Exception occurred in GraphStructureChangeListener:");
                    e.printStackTrace();
                }
                catch (Error e) {
                    // Issue 264 - catch Error
                    System.err.println("Error occurred in GraphStructureChangeListener:");
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Adds the specified ShaderErrorListener to the set of listeners
     * that will be notified when a programmable shader error is
     * detected on a live scene graph. If the specifed listener is
     * null no action is taken and no exception is thrown.
     * If a shader error occurs, the listeners will be called
     * asynchronously from a separate notification thread. The Java 3D
     * renderer and behavior scheduler will continue to run as if the
     * error had not occurred, except that shading will be disabled
     * for the objects in error. If applications desire to detach or
     * modify the scene graph as a result of the error, they should
     * use a behavior post if they want that change to be
     * synchronous with the renderer.
     *
     * @param listener the listener to add to the set.
     *
     * @since Java 3D 1.4
     */
    public void addShaderErrorListener(ShaderErrorListener listener) {
        if (listener == null) {
            return;
        }

        if (shaderErrorListenerSet == null) {
            shaderErrorListenerSet = new HashSet();
        }

        synchronized(shaderErrorListenerSet) {
            shaderErrorListenerSet.add(listener);
        }
    }

    /**
     * Removes the specified ShaderErrorListener from the set of
     * listeners. This method performs no function, nor does it throw
     * an exception if the specified listener is not currently in the
     * set or is null.
     *
     * @param listener the listener to remove from the set.
     *
     * @since Java 3D 1.4
     */
    public void removeShaderErrorListener(ShaderErrorListener listener) {
        if (shaderErrorListenerSet == null) {
	    return;
	}

        synchronized(shaderErrorListenerSet) {
            shaderErrorListenerSet.remove(listener);
        }
    }

    /**
     * Notifies all listeners of a shader error. If no listeners exist, a default
     * listener is notified.
     */
    void notifyShaderErrorListeners(ShaderError error) {
	boolean errorReported = false;

	// Notify all error listeners in the set
        if (shaderErrorListenerSet != null) {
            synchronized(shaderErrorListenerSet) {
                Iterator<ShaderErrorListener> it = shaderErrorListenerSet.iterator();
                while(it.hasNext()) {
                    ShaderErrorListener listener = it.next();
                    try {
                        listener.errorOccurred(error);
                    }
                    catch (RuntimeException e) {
                        System.err.println("Exception occurred in ShaderErrorListener:");
                        e.printStackTrace();
                    }
                    catch (Error e) {
                        // Issue 264 - catch Error
                        System.err.println("Error occurred in ShaderErrorListener:");
                        e.printStackTrace();
                    }
                    errorReported = true;
                }
            }
        }

        // Notify the default error listener if the set is null or empty
        if (!errorReported) {
            defaultShaderErrorListener.errorOccurred(error);
        }
    }


    // Issue 260 : rendering error listeners.

    /**
     * Adds the specified RenderingErrorListener to the set of listeners
     * that will be notified when a rendering error is detected.
     * If the specifed listener is null no action is taken and no exception
     * is thrown.
     * If a rendering error occurs, the listeners will be called
     * asynchronously from a separate notification thread.  If the set
     * of listeners is empty, a default listener is notified. The
     * default listener prints the error information to System.err and
     * then calls System.exit().
     *
     * @param listener the listener to add to the set.
     *
     * @since Java 3D 1.5
     */
    public static void addRenderingErrorListener(RenderingErrorListener listener) {
        if (listener == null) {
            return;
        }

        if (renderingErrorListenerSet == null) {
            renderingErrorListenerSet = new HashSet();
        }

        synchronized(renderingErrorListenerSet) {
            renderingErrorListenerSet.add(listener);
        }
    }

    /**
     * Removes the specified RenderingErrorListener from the set of
     * listeners. This method performs no function, nor does it throw
     * an exception if the specified listener is not currently in the
     * set or is null.
     *
     * @param listener the listener to remove from the set.
     *
     * @since Java 3D 1.5
     */
    public static void removeRenderingErrorListener(RenderingErrorListener listener) {
        if (renderingErrorListenerSet == null) {
	    return;
	}

        synchronized(renderingErrorListenerSet) {
            renderingErrorListenerSet.remove(listener);
        }
    }

    /**
     * Notifies all listeners of a rendering error. If no listeners exist,
     * a default listener is notified.
     */
    static void notifyRenderingErrorListeners(RenderingError error) {
	boolean errorReported = false;

	// Notify all error listeners in the set
        if (renderingErrorListenerSet != null) {
            synchronized(renderingErrorListenerSet) {
                Iterator<RenderingErrorListener> it = renderingErrorListenerSet.iterator();
                while(it.hasNext()) {
                    RenderingErrorListener listener = it.next();
                    try {
                        listener.errorOccurred(error);
                    }
                    catch (RuntimeException e) {
                        System.err.println("Exception occurred in RenderingErrorListener:");
                        e.printStackTrace();
                    }
                    catch (Error e) {
                        // Issue 264 - catch Error
                        System.err.println("Error occurred in RenderingErrorListener:");
                        e.printStackTrace();
                    }
                    errorReported = true;
                }
            }
        }

        // Notify the default error listener if the set is null or empty
        if (!errorReported) {
            defaultRenderingErrorListener.errorOccurred(error);
        }
    }

}
