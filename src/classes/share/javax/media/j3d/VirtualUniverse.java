/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

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
 * @see Locale
 */

public class VirtualUniverse extends Object {
    // NOTE TO DEVELOPERS:
    //
    // Developers who modify Java 3D in any way are required to modify
    // the auxiliary implementation vendor string in VersionInfo.java.
    // See that file for instructions.

    // The global MasterControl object.  There is only one of these
    // for all of Java 3D.
    static MasterControl mc = null;

    // The lock to acquire before traversing the scene graph
    Object sceneGraphLock = new Object();
    Object behaviorLock = new Object();

    // A list of locales that are contained within this universe
    Vector listOfLocales = new Vector();

    // The list of view platforms.
    ArrayList viewPlatforms = new ArrayList();


    // The cached list of vp's
    Object[] viewPlatformList = null;

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

    // Keep track of how many active View use this universe
    int activeViewCount = 0;

    // Root ThreadGroup for creating Java 3D threads
    static ThreadGroup rootThreadGroup;

    // Lock for MasterControl Object creation
    static Object mcLock = new Object();
    
    // Properties object for getProperties
    private static J3dQueryProps properties = null;

    // Flag to indicate that user thread has to
    // stop until MC completely register/unregister View.
    View regViewWaiting = null;
    View unRegViewWaiting = null;
    boolean isSceneGraphLock = false;

    private Object waitLock = new Object();

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

    static void createMC() {
	synchronized (mcLock) {
	    if (mc == null) {
		mc = new MasterControl();
	    }
	}
    }

    static void destroyMC() {
	synchronized (mcLock) {
	    mc = null;
	}
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
	// Print out version information unless this is a
	// non-debuggable, release (fcs) build
	if(J3dDebug.devPhase || VersionInfo.isDebug) {
	    String versionStr = VersionInfo.getVersion();
	    if (J3dDebug.devPhase) {
		System.err.println("Java 3D [dev] " + versionStr);
	    }
	    else {
		System.err.println("Java 3D " + versionStr);
	    }
	    System.err.println();
	}

	// Print out debugging information for debug builds
	if(VersionInfo.isDebug) {
	    System.err.println("Initializing Java 3D runtime system:");
	    System.err.println("    version = " + VersionInfo.getVersion());
	    System.err.println("    vendor = " + VersionInfo.getVendor());
	    System.err.println("    specification.version = " +
			       VersionInfo.getSpecificationVersion());
	    System.err.println("    specification.vendor = " +
			       VersionInfo.getSpecificationVendor());
	    System.err.println();
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
	createMC();

	// Print out debugging information for debug builds
	if(VersionInfo.isDebug) {
	    System.err.println("Java 3D system initialized");
	    System.err.print("    graphics library = ");
	    switch (mc.getRenderingAPI()) {
	    case MasterControl.RENDER_OPENGL_SOLARIS:
		System.err.println("Solaris OpenGL");
		break;
	    case MasterControl.RENDER_OPENGL_LINUX:
		System.err.println("Linux OpenGL");
		break;
	    case MasterControl.RENDER_OPENGL_WIN32:
		System.err.print("Windows OpenGL");
		break;
	    case MasterControl.RENDER_DIRECT3D:
		System.err.println("Windows Direct3D");
		break;
	    default:
		System.err.println("UNKNOWN");
		break;
	    }
	    System.err.println();
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


	for (i = listOfLocales.size()-1; i > 0;  i--) {
	    ((Locale)listOfLocales.get(i)).removeFromUniverse();
	}

	if (i >= 0) {
	    // We have to clear() the listOfLocales first before
	    // invoke the last removeFromUniverse() so that isEmpty()
	    // (call from View.deactivate() ) will return true and
	    // threads can destroy from MC.
	    Locale loc = (Locale) listOfLocales.get(0);
	    listOfLocales.clear();
	    loc.removeFromUniverse();
	}
	VirtualUniverse.mc.postRequest(MasterControl.EMPTY_UNIVERSE,
				       this);

	setLiveState.reset(null);
    }


    /**
     * Returns the enumeration object of all locales in this virtual universe.
     * @return the enumeration object 
     */
    public Enumeration getAllLocales() {
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
     * <code>j3d.renderer</code>
     * <ul>
     * String that specifies the Java 3D rendering library.  This could
     * be one of: "OpenGL" or "DirectX".
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
	    values.add(mc.isD3D() ? "DirectX" : "OpenGL");

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
    // TODO: reuse of id's imply a slight collision problem in the
    // render queue's.
    // BUG 4181362
    String getNodeId() {
        String str;
 
	if (nodeIdFreeList.size() == 0) {
	   str = Long.toString(nodeIdCount);
           nodeIdCount++;
	} else {
	   str = (String) nodeIdFreeList.lastElement();
	   nodeIdFreeList.removeElement(str);
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

    synchronized Object[] getViewPlatformList() {
	if (vpChanged) {
	    viewPlatformList = viewPlatforms.toArray();
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
    }

    void enableComponentEvents() {
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
                        if (cv.eventCatcher != null)
		            cv.eventCatcher.enableComponentEvents();
	            }
		}
	    }
	}
    }

    void disableFocusEvents() {
	Enumeration cvs;
	Canvas3D cv;
        ViewPlatformRetained vp;
	View views[];
	Object[] vps = getViewPlatformList();
	enableFocus = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();
	enableFocus = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();
	View views[];

	enableKey = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();
	View views[];

	enableKey = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();

	enableMouse = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();

	enableMouse = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();

	enableMouseMotion = false;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Object[] vps = getViewPlatformList();

	enableMouseMotion = true;

	if (vps != null) {
	    for (int i=0; i<vps.length; i++) {
                vp =(ViewPlatformRetained)vps[i];               
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
	Enumeration elm = listOfLocales.elements();

	while (elm.hasMoreElements()) {
	    Locale loc = (Locale) elm.nextElement();
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
}
