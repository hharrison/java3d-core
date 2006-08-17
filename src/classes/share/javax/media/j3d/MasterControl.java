/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

package javax.media.j3d;

import java.util.*;
import java.awt.*;

class MasterControl {

    /**
     * Options for the runMonitor 
     */
    static final int CHECK_FOR_WORK = 0;
    static final int SET_WORK       = 1;
    static final int RUN_THREADS    = 2;
    static final int THREAD_DONE    = 3;
    static final int SET_WORK_FOR_REQUEST_RENDERER   = 5;
    static final int RUN_RENDERER_CLEANUP            = 6;    

    // The thread states for MC
    static final int SLEEPING            = 0;
    static final int RUNNING             = 1;
    static final int WAITING_FOR_THREADS = 3;
    static final int WAITING_FOR_CPU     = 4;
    static final int WAITING_FOR_RENDERER_CLEANUP = 5;

    // Constants used in renderer thread argument
    static final Integer REQUESTRENDER = new Integer(Renderer.REQUESTRENDER);
    static final Integer RENDER = new Integer(Renderer.RENDER);
    static final Integer SWAP = new Integer(Renderer.SWAP);

    // Constants used for request from user threads
    static final Integer ACTIVATE_VIEW = new Integer(1);
    static final Integer DEACTIVATE_VIEW = new Integer(2);
    static final Integer START_VIEW = new Integer(3);
    static final Integer STOP_VIEW = new Integer(4);
    static final Integer REEVALUATE_CANVAS = new Integer(5);
    static final Integer UNREGISTER_VIEW = new Integer(6);
    static final Integer PHYSICAL_ENV_CHANGE = new Integer(7);
    static final Integer INPUTDEVICE_CHANGE = new Integer(8);
    static final Integer EMPTY_UNIVERSE = new Integer(9);
    static final Integer START_RENDERER = new Integer(10);
    static final Integer STOP_RENDERER = new Integer(11);
    static final Integer RENDER_ONCE = new Integer(12);
    static final Integer FREE_CONTEXT = new Integer(13);
    static final Integer FREE_DRAWING_SURFACE = new Integer(14);
    static final Integer FREE_MESSAGE = new Integer(15);
    static final Integer RESET_CANVAS = new Integer(16);
    static final Integer GETBESTCONFIG = new Integer(17);
    static final Integer ISCONFIGSUPPORT = new Integer(18);
    static final Integer SET_GRAPHICSCONFIG_FEATURES = new Integer(19);
    static final Integer SET_QUERYPROPERTIES = new Integer(20);    
    static final Integer SET_VIEW = new Integer(21);

    private static boolean librariesLoaded = false;

    /**
     * reference to MasterControl thread
     */
    private MasterControlThread mcThread = null;

    /**
     * The list of views that are currently registered
     */
    private UnorderList views = new UnorderList(1, View.class);

    
    /**
     * by MIK OF CLASSX
     *
     * the flag to indicate whether the background of the offscreen
     * canvas must be transparent or not false by default
     */
    boolean transparentOffScreen = false;

    /**
     * Flag to indicate whether Pbuffers are used for off-screen
     * rendering; true by default.  Set by the "j3d.usePbuffer"
     * property, When this flag is set to false, Bitmap (Windows) or
     * Pixmap (UNIX) rendering will be used
     */
    boolean usePbuffer = true;

    /**
     * Flag to indicate whether should renderer view frustum culling is done;
     * true by default.
     * Set by the -Dj3d.viewFrustumCulling property, When this flag is
     * set to false, the renderer view frustum culling is turned off.
     */
    boolean viewFrustumCulling = true;

    /**
     * the flag to indicate whether the geometry should be locked or not
     */

    private boolean lockGeometry = false;
    
    /**
     * The number of registered views that are active 
     */
    private int numActiveViews = 0;
    
    // A freelist for ImageComponentUpdateInfo
    private ImageComponentUpdateInfo[] imageUpdateInfoList = 
				new ImageComponentUpdateInfo[2];
    private int numFreeImageUpdateInfo = 0;


    /**
     * The list of active universes get from View
     */
    private UnorderList activeUniverseList = new UnorderList(VirtualUniverse.class);

    /**
     * The list of universes register from View
     */
    private UnorderList regUniverseList = new UnorderList(VirtualUniverse.class);

    /**
     * A lock used for accessing time structures.
     */
    private Object timeLock = new Object();


    /**
     * The current "time" value
     */
    private long time = 0;

    /**
     * Use to assign threadOpts in Renderer thread. 
     */
    private long waitTimestamp = 0;

    /**
     * The current list of work threads
     */
    private UnorderList stateWorkThreads = 
                               new UnorderList(J3dThreadData.class);
    private UnorderList renderWorkThreads = 
                               new UnorderList(J3dThreadData.class);
    private UnorderList requestRenderWorkThreads =
			       new UnorderList(J3dThreadData.class);

    /**
     * The current list of work threads
     */
    private UnorderList renderThreadData = new UnorderList(J3dThreadData.class);

    /**
     * The list of input device scheduler thread
     */
    private UnorderList inputDeviceThreads = 
                             new UnorderList(1, InputDeviceScheduler.class);

    /**
     * A flag that is true when the thread lists need updating
     */
    private boolean threadListsChanged;


    /**
     * Markers for the last transform structure update thread
     * and the last update thread.
     */
    private int lastTransformStructureThread = 0;
    private int lastStructureUpdateThread = 0;

    /**
     * The current time snapshots
     */
    private long currentTime;

    // Only one Timer thread in the system.
    TimerThread timerThread;

    // Only one Notification thread in the system.
    private NotificationThread notificationThread;

    /**
     * This flag indicates that MC is running
     */
    volatile boolean running = true;

    /**
     * This flag indicates that MC has work to do
     */
    private boolean workToDo = false;

    /**
     * This flag indicates that there is work for requestRenderer
     */
    private boolean requestRenderWorkToDo = false;

   /** 
     * The number of THREAD_DONE messages pending
     */
    private int threadPending = 0;
    private int renderPending = 0;
    private int statePending = 0;
   
    /**
     * State variables for work lists
     */
    private boolean renderWaiting = false;
    private boolean stateWaiting = false;

    /**
     * The current state of the MC thread
     */
    private int state = SLEEPING;

    // time for sleep in order to met the minimum frame duration
    private long sleepTime = 0;


    /**
     * The number of cpu's Java 3D may use
     */
    private int cpuLimit;
    
    /**
     * A list of mirror objects to be updated
     */
    private UnorderList mirrorObjects = new UnorderList(ObjectUpdate.class);

    /**
     * The renderingAttributesStructure for updating node component 
     * objects
     */
    private RenderingAttributesStructure renderingAttributesStructure = 
                        new RenderingAttributesStructure();

    /**
     * The default rendering method
     */
    private DefaultRenderMethod defaultRenderMethod = null;

    /**
     * The text3D rendering method
     */
    private Text3DRenderMethod text3DRenderMethod = null;

    /**
     * The vertex array rendering method
     */
    private VertexArrayRenderMethod vertexArrayRenderMethod = null;

    /**
     * The displayList rendering method
     */
    private DisplayListRenderMethod displayListRenderMethod = null;

    /**
     * The compressed geometry rendering method
     */
    private CompressedGeometryRenderMethod compressedGeometryRenderMethod = null;

    /**
     * The oriented shape3D rendering method
     */
    private OrientedShape3DRenderMethod orientedShape3DRenderMethod = null;

    /**
     * This is the start time upon which alpha's and behaviors
     * are synchronized to. It is initialized once, the first time
     * that a MasterControl object is created.
     */
    static long systemStartTime = 0L;

    // Flag indicating that we are on a Windows OS
    private static boolean isWindowsOs = false;
    
    // Flag indicating we are on MacOS
    private static boolean isMacOs = false;

    // This is a counter for texture id's, valid id starts from 1
    private int textureIdCount = 0;

    // This is lock for both 2D/3D textureIds;
    private Object textureIdLock = new Object();

    // This is a time stamp used when context is created
    private long contextTimeStamp = 0;
    
    // This is an array of  canvasIds in used
    private boolean[] canvasIds = null;
    private int canvasFreeIndex = 0;
    private Object canvasIdLock = new Object();

    // This is a counter for rendererBit
    private int rendererCount = 0;

    // Flag that indicates whether to shared display context or not
    boolean isSharedCtx = false;

    // Flag that tells us to use NV_register_combiners
    boolean useCombiners = false;

    // Flag that indicates whether compile is disabled or not
    boolean disableCompile = false;

    // Flag that indicates whether or not compaction occurs
    boolean doCompaction = true;

    // Flag that indicates whether separate specular color is disabled or not
    boolean disableSeparateSpecularColor = false;

    // Flag that indicates whether DisplayList is used or not
    boolean isDisplayList = true;

    // If this flag is set, then by-ref geometry will not be
    // put in display list
    boolean buildDisplayListIfPossible = false;
    
    // If this flag is set, then geometry arrays with vertex attributes can
    // be in display list.
    boolean vertexAttrsInDisplayList = false;

    // Issue 249 - flag that indicates whether the soleUser optimization is permitted
    boolean allowSoleUser = false;

    // Issue 266 - Flag indicating whether null graphics configs are allowed
    // Set by -Dj3d.allowNullGraphicsConfig property
    // Setting this flag causes Canvas3D to allow a null GraphicsConfiguration
    // for on-screen canvases. This is only for backward compatibility with
    // legacy applications.
    boolean allowNullGraphicsConfig = false;
    
    // Issue 288 & 123 - flag indicating if freelists should be used
    boolean useFreeLists = false;

    // The global shading language being used. Using a ShaderProgram
    // with a shading language other than the one specified by
    // globalShadingLanguage will cause a ShaderError to be generated,
    static int globalShadingLanguage = Shader.SHADING_LANGUAGE_GLSL;

    // Flags indicating whether the Cg or GLSL libraries are available; we still need
    // to check for the actual extension support when the Canvas3D with its associated context
    // is created. Note that these are qualifed by the above globalShadingLanguage, so at
    // most one of these two flags will be true;
    static boolean cgLibraryAvailable = false;
    static boolean glslLibraryAvailable = false;


    // REQUESTCLEANUP messages argument
    static Integer REMOVEALLCTXS_CLEANUP = new Integer(1);
    static Integer REMOVECTX_CLEANUP     = new Integer(2);
    static Integer REMOVENOTIFY_CLEANUP  = new Integer(3);
    static Integer RESETCANVAS_CLEANUP   = new Integer(4);
    static Integer FREECONTEXT_CLEANUP   = new Integer(5);
    
    // arguments for renderer resource cleanup run
    Object rendererCleanupArgs[] = {new Integer(Renderer.REQUESTCLEANUP),
				    null, null};


    // Context creation should obtain this lock, so that
    // first_time and all the extension initilialization
    // are done in the MT safe manner
    Object contextCreationLock = new Object();
    
    // Flag that indicates whether to lock the DSI while rendering
    boolean doDsiRenderLock = false;
    
    // Flag that indicates the pre-1.5 behavior of enforcing power-of-two
    // textures. If set, then any non-power-of-two textures will throw an
    // exception.
    boolean enforcePowerOfTwo = false;
    
    // Flag that indicates whether the framebuffer is sharing the
    // Z-buffer with both the left and right eyes when in stereo mode.
    // If this is true, we need to clear the Z-buffer between rendering
    // to the left and right eyes.
    boolean sharedStereoZBuffer = true;

    // True to disable all underlying multisampling API so it uses
    // the setting in the driver. 
    boolean implicitAntialiasing = false;

    // False to disable compiled vertex array extensions if support
    boolean isCompiledVertexArray = true;

    // Number of reserved vertex attribute locations for GLSL (must be at
    // least 1).
    // Issue 269 - need to reserve up to 6 vertex attribtue locations to ensure
    // that we don't collide with a predefined gl_* attribute on nVidia cards.
    int glslVertexAttrOffset = 6;

    // Hashtable that maps a GraphicsDevice to its associated
    // Screen3D--this is only used for on-screen Canvas3Ds
    Hashtable deviceScreenMap = new Hashtable();

    // Use to store all requests from user threads.
    UnorderList requestObjList = new UnorderList();
    private UnorderList requestTypeList = new UnorderList(Integer.class);

    // Temporary storage to store stop request for requestViewList
    private UnorderList tempViewList = new UnorderList();
    private UnorderList renderOnceList = new UnorderList();

    // This flag is true when there is pending request
    // i.e. false when the above requestxxx Lists are all empty.
    private boolean pendingRequest = false;

    // Root ThreadGroup for creating Java 3D threads
    private static ThreadGroup rootThreadGroup;

    // Thread priority for all Java 3D threads
    private static int threadPriority;

    static private Object mcThreadLock = new Object();

    private ArrayList timestampUpdateList = new ArrayList(3);

    private UnorderList freeMessageList = new UnorderList(8);

    // Native AWT object
    long awt;

    // Maximum number of lights
    int maxLights;

    // This is used for D3D only
    int resendTexTimestamp = 0;

    // Indicates that the property to disable Xinerama mode was set and
    // successfully disabled.
    boolean xineramaDisabled = false;

    // Set by the -Dj3d.sortShape3DBounds property, When this flag is
    // set to true, the bounds of the Shape3D node will be used in
    // place of the computed GeometryArray bounds for transparency
    // sorting for those Shape3D nodes whose boundsAutoCompute
    // attribute is set to false.
    boolean sortShape3DBounds = false;

    //Set by -Dj3d.forceReleaseView property.
    //Setting this flag as true disables the bug fix 4267395 in View deactivate().
    //The bug 4267395 can lock-up *some* systems, but the bug fix can
    //produce memory leaks in applications which creates and destroy Canvas3D
    //from time to time.
    //Set as true if you have memory leaks after disposing Canvas3D.
    //Default false value does affect Java3D View dispose behavior.
    boolean forceReleaseView = false;

    /**
     * Constructs a new MasterControl object.  Note that there is
     * exatly one MasterControl object, created statically by
     * VirtualUniverse.
     */
    MasterControl() {
        assert librariesLoaded;

	// Get AWT handle
	awt = Pipeline.getPipeline().getAWT();

        // Initialize the start time upon which alpha's and behaviors
        // are synchronized to (if it isn't already set).
        if (systemStartTime == 0L) {
            systemStartTime = J3dClock.currentTimeMillis();
        }

	if(J3dDebug.devPhase) {
	    // Check to see whether debug mode is allowed
	    J3dDebug.debug = getBooleanProperty("j3d.debug", false,
						"J3dDebug.debug");
	}

	// Check to see whether shared contexts are allowed
	if (!isD3D()) {
	    isSharedCtx =
		getBooleanProperty("j3d.sharedctx", isSharedCtx, "shared contexts");
	}

	doCompaction = getBooleanProperty("j3d.docompaction", doCompaction,
					  "compaction");

	// by MIK OF CLASSX
	transparentOffScreen = getBooleanProperty("j3d.transparentOffScreen", transparentOffScreen, "transparent OffScreen");

	usePbuffer = getBooleanProperty("j3d.usePbuffer",
					usePbuffer,
					"Off-screen Pbuffer");

	viewFrustumCulling = getBooleanProperty("j3d.viewFrustumCulling", viewFrustumCulling,"View frustum culling in the renderer is");
	
	sortShape3DBounds =
	    getBooleanProperty("j3d.sortShape3DBounds", sortShape3DBounds,
			       "Shape3D bounds enabled for transparency sorting",
			       "Shape3D bounds *ignored* for transparency sorting");

	forceReleaseView =
	    getBooleanProperty("j3d.forceReleaseView", forceReleaseView,
			       "forceReleaseView  after Canvas3D dispose enabled",
			       "forceReleaseView  after Canvas3D dispose disabled");

	useCombiners = getBooleanProperty("j3d.usecombiners", useCombiners,
					  "Using NV_register_combiners if available",
					  "NV_register_combiners disabled");

	if (getProperty("j3d.disablecompile") != null) {
	    disableCompile = true;
	    System.err.println("Java 3D: BranchGroup.compile disabled");
	}

	if (getProperty("j3d.disableSeparateSpecular") != null) {
	    disableSeparateSpecularColor = true;
	    System.err.println("Java 3D: separate specular color disabled if possible");
	}

	isDisplayList = getBooleanProperty("j3d.displaylist", isDisplayList,
					   "display list");

	implicitAntialiasing =
	    getBooleanProperty("j3d.implicitAntialiasing",
			       implicitAntialiasing,
			       "implicit antialiasing");

	isCompiledVertexArray =
	    getBooleanProperty("j3d.compiledVertexArray",
			       isCompiledVertexArray,
			       "compiled vertex array");

        boolean j3dOptimizeSpace =
	    getBooleanProperty("j3d.optimizeForSpace", true,
			       "optimize for space");

        if (isDisplayList) {
            // Build Display list for by-ref geometry
            // ONLY IF optimizeForSpace is false
            if (!j3dOptimizeSpace) {
                buildDisplayListIfPossible = true;
            }

            // Build display lists for geometry with vertex attributes
            // ONLY if we are in GLSL mode and GLSL shaders are available
            if (glslLibraryAvailable) {
                vertexAttrsInDisplayList = true;
            }
        }

        // Check to see whether Renderer can run without DSI lock
	doDsiRenderLock = getBooleanProperty("j3d.renderLock",
					     doDsiRenderLock,
					     "render lock");
        
        // Check to see whether we enforce power-of-two textures
        enforcePowerOfTwo = getBooleanProperty("j3d.textureEnforcePowerOfTwo",
					       enforcePowerOfTwo,
					       "checking power-of-two textures");

        // Issue 249 - check to see whether the soleUser optimization is permitted
        allowSoleUser = getBooleanProperty("j3d.allowSoleUser",
					   allowSoleUser,
					   "sole-user mode");

        // Issue 266 - check to see whether null graphics configs are allowed
        allowNullGraphicsConfig = getBooleanProperty("j3d.allowNullGraphicsConfig",
						     allowNullGraphicsConfig,
						     "null graphics configs");

        // Issue 288 & 123 - check to see if free lists should be used
        useFreeLists = getBooleanProperty("j3d.useFreeLists",
						     useFreeLists,
						     "Use Free Lists");

        // Check to see if stereo mode is sharing the Z-buffer for both eyes.
	sharedStereoZBuffer = 
	    getBooleanProperty("j3d.sharedstereozbuffer",
			       sharedStereoZBuffer,
			       "shared stereo Z buffer");

	// Get the maximum number of concurrent threads (CPUs)
	final int defaultThreadLimit = getNumberOfProcessors() + 1;
	Integer threadLimit =
	    (Integer) java.security.AccessController.doPrivileged(
	    new java.security.PrivilegedAction() {
		public Object run() {
		    return Integer.getInteger("j3d.threadLimit",
					      defaultThreadLimit);
		}
	    });

	cpuLimit = threadLimit.intValue();
	if (cpuLimit < 1)
	    cpuLimit = 1;
	if (J3dDebug.debug || cpuLimit != defaultThreadLimit) {
	    System.err.println("Java 3D: concurrent threadLimit = " +
			       cpuLimit);
	}

	// Get the input device scheduler sampling time
	Integer samplingTime  =
	    (Integer) java.security.AccessController.doPrivileged(
	    new java.security.PrivilegedAction() {
		public Object run() {
		    return Integer.getInteger("j3d.deviceSampleTime", 0);
		}
	    });
	
	if (samplingTime.intValue() > 0) {
	    InputDeviceScheduler.samplingTime =
		samplingTime.intValue();
	    System.err.println("Java 3D: Input device sampling time = "
			       + samplingTime + " ms");
	}

	// Get the glslVertexAttrOffset
	final int defaultGLSLVertexAttrOffset = glslVertexAttrOffset;
	Integer vattrOffset =
	    (Integer) java.security.AccessController.doPrivileged(
	    new java.security.PrivilegedAction() {
		public Object run() {
		    return Integer.getInteger("j3d.glslVertexAttrOffset",
					      defaultGLSLVertexAttrOffset);
		}
	    });

	glslVertexAttrOffset = vattrOffset.intValue();
        if (glslVertexAttrOffset < 1) {
            glslVertexAttrOffset = 1;
        }
	if (J3dDebug.debug || glslVertexAttrOffset != defaultGLSLVertexAttrOffset) {
	    System.err.println("Java 3D: glslVertexAttrOffset = " +
			       glslVertexAttrOffset);
	}

        // See if Xinerama should be disabled for better performance.
	boolean disableXinerama = false;
	if (getProperty("j3d.disableXinerama") != null) {
	    disableXinerama = true;
	}

	// Initialize the native J3D library
	if (!Pipeline.getPipeline().initializeJ3D(disableXinerama)) {
	    throw new RuntimeException(J3dI18N.getString("MasterControl0"));
	}

	if (xineramaDisabled) {
	    // initializeJ3D() successfully disabled Xinerama.
	    System.err.println("Java 3D: Xinerama disabled");
	}
	else if (disableXinerama) {
	    // j3d.disableXinerama is true, but attempt failed.
	    System.err.println("Java 3D: could not disable Xinerama");
	}

        // Check for obsolete properties
        String[] obsoleteProps = {
            "j3d.simulatedMultiTexture",
            "j3d.forceNormalized",
            "j3d.backgroundtexture",
            "j3d.g2ddrawpixel",
        };
        for (int i = 0; i < obsoleteProps.length; i++) {
            if (getProperty(obsoleteProps[i]) != null) {
                System.err.println(obsoleteProps[i] + " : property ignored");
            }
        }

	// Get the maximum Lights
	maxLights = Pipeline.getPipeline().getMaximumLights();

	// create the freelists
	FreeListManager.createFreeLists(useFreeLists);

	// create an array canvas use registers
	// The 32 limit can be lifted once the
	// resourceXXXMasks in other classes 
	// are change not to use integer.
	canvasIds = new boolean[32];
	for(int i=0; i<canvasIds.length; i++) {
	    canvasIds[i] = false;
	}
	canvasFreeIndex = 0;
    }

    private static String getProperty(final String prop) {
	return (String) java.security.AccessController.doPrivileged(
	    new java.security.PrivilegedAction() {
		public Object run() {
		    return System.getProperty(prop);
		}
   	    });
    }

    private static boolean getBooleanProperty(String prop,
					      boolean defaultValue,
					      String trueMsg,
					      String falseMsg) {
	boolean value = defaultValue;
	String propValue = getProperty(prop);

	if (propValue != null) {
	    value = Boolean.valueOf(propValue).booleanValue();
	    System.err.println("Java 3D: " + (value ? trueMsg : falseMsg));
	}
	return value;
    }

    private static boolean getBooleanProperty(String prop,
					      boolean defaultValue,
					      String msg) {
	return getBooleanProperty(prop,
				  defaultValue,
				  (msg + " enabled"),
				  (msg + " disabled"));
    }

    /**
     * Method to create and initialize the rendering Pipeline object,
     * and to load the native libraries needed by Java 3D. This is
     * called by the static initializer in VirtualUniverse <i>before</i>
     * the MasterControl object is created.
     */
    static void loadLibraries() {
        assert !librariesLoaded;

        // Set global flags indicating whether we are running on Windows or MacOS
        String osName = getProperty("os.name");
        isWindowsOs = osName != null && osName.startsWith("Windows");
        isMacOs = osName != null && osName.startsWith("Mac");

//KCR:        System.err.println("MasterControl.loadLibraries()");
//KCR:        System.err.println("    osName = \"" + osName + "\"" +
//KCR:                ", isWindowsOs = " + isWindowsOs +
//KCR:                ", isMacOs = " + isMacOs);

        // Initialize the Pipeline object associated with the
        // renderer specified by the "j3d.rend" system property.
        //
        // XXXX : We should consider adding support for a more flexible,
        // dynamic selection scheme via an API call.

        // Default rendering pipeline is the JOGL pipeline on MacOS and the
        // native OpenGL pipeline on all other platforms.
        Pipeline.Type rendererType =
                isMacOs ? Pipeline.Type.JOGL : Pipeline.Type.NATIVE_OGL;

        String rendStr = getProperty("j3d.rend");
        if (rendStr == null) {
            // Use default pipeline
        } else if (rendStr.equals("ogl") && !isMacOs) {
            rendererType = Pipeline.Type.NATIVE_OGL;
        } else if (rendStr.equals("d3d") && isWindowsOs) {
            rendererType = Pipeline.Type.NATIVE_D3D;
        } else if (rendStr.equals("jogl")) {
            rendererType = Pipeline.Type.JOGL;
        } else if (rendStr.equals("noop")) {
            rendererType = Pipeline.Type.NOOP;
        } else {
            System.err.println("Java 3D: Unrecognized renderer: " + rendStr);
            // Use default pipeline
        }

//KCR:        System.err.println("    using " + rendererType + " pipeline");

        // Construct the singleton Pipeline instance
        Pipeline.createPipeline(rendererType);

        // Get the global j3d.shadingLanguage system property
        final String slStr = getProperty("j3d.shadingLanguage");
        if (slStr != null) {
            boolean found = false;
            if (slStr.equals("GLSL")) {
                globalShadingLanguage = Shader.SHADING_LANGUAGE_GLSL;
                found = true;
            } else if (slStr.equals("Cg")) {
                globalShadingLanguage = Shader.SHADING_LANGUAGE_CG;
                found = true;
            }

            if (found) {
                System.err.println("Java 3D: Setting global shading language to " + slStr);
            } else {
                System.err.println("Java 3D: Unrecognized shading language: " + slStr);
            }
        }

        // Load all required libraries
        Pipeline.getPipeline().loadLibraries(globalShadingLanguage);

        // Check whether the Cg library is available
        if (globalShadingLanguage == Shader.SHADING_LANGUAGE_CG) {
            cgLibraryAvailable = Pipeline.getPipeline().isCgLibraryAvailable();
        }

        // Check whether the GLSL library is available
        if (globalShadingLanguage == Shader.SHADING_LANGUAGE_GLSL) {
            glslLibraryAvailable = Pipeline.getPipeline().isGLSLLibraryAvailable();
        }

        assert !(glslLibraryAvailable && cgLibraryAvailable) :
            "ERROR: cannot support both GLSL and CG at the same time";

        librariesLoaded = true;
    }


    /**
     * Invoke from InputDeviceScheduler to create an
     * InputDeviceBlockingThread. 
     */
    InputDeviceBlockingThread getInputDeviceBlockingThread(
					   final InputDevice device) {

	return (InputDeviceBlockingThread)
	    java.security.AccessController.doPrivileged(
		 new java.security.PrivilegedAction() {
                     public Object run() {
			 synchronized (rootThreadGroup) {
			     Thread thread = new InputDeviceBlockingThread(
				 	    rootThreadGroup, device);
			     thread.setPriority(threadPriority);
			     return thread;
			 }
		     }
		 }
        );
    }

    /**
     * Set thread priority to all threads under Java3D thread group.
     */
    void setThreadPriority(final int pri) {
	synchronized (rootThreadGroup) {
	    threadPriority = pri;
	    java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
			Thread list[] = new
			    Thread[rootThreadGroup.activeCount()];
			int count = rootThreadGroup.enumerate(list);
			for (int i=count-1; i >=0; i--) {
			    list[i].setPriority(pri);
			}
			return null;
		    }
	    });
	}
    }


    /**
     * Return Java3D thread priority
     */
    int getThreadPriority() {
	return threadPriority;
    }

    /**
     * This returns the a unused renderer bit
     */
    int getRendererBit() {
        return (1 << rendererCount++);
    }


    /**
     * This returns the a unused renderer bit
     */
    int getRendererId() {
        return rendererCount++;
    }

    /**
     * This returns a context creation time stamp
     * Note: this has to be called under the contextCreationLock
     */
    long getContextTimeStamp() {
	return (++contextTimeStamp);
    }


    /**
     * This returns the a unused displayListId
     */
    Integer getDisplayListId() {
        return (Integer) FreeListManager.getObject(FreeListManager.DISPLAYLIST);
    }

    void freeDisplayListId(Integer id) {
	FreeListManager.freeObject(FreeListManager.DISPLAYLIST, id);
    }

    /**
     * This returns the a unused textureId
     */
    int getTexture2DId() {
	// MasterControl has to handle the ID itself.  2D and 3D ideas must
	// never be the same, so the counter has to be in the MasterControl
	MemoryFreeList textureIds =
	    FreeListManager.getFreeList(FreeListManager.TEXTURE2D);
	int id;

	synchronized (textureIdLock) {
	    if (textureIds.size() > 0) {
		return ((Integer)FreeListManager.
			getObject(FreeListManager.TEXTURE2D)).intValue();
	    } else {
		return (++textureIdCount);
	    }
	}
    }

    int getTexture3DId() {
	// MasterControl has to handle the ID itself.  2D and 3D ideas must
	// never be the same, so the counter has to be in the MasterControl
	MemoryFreeList textureIds =
	    FreeListManager.getFreeList(FreeListManager.TEXTURE3D);
	synchronized (textureIdLock) {
	    if (textureIds.size > 0) {
		return ((Integer)FreeListManager.
			getObject(FreeListManager.TEXTURE3D)).intValue();
	    }
	    else return (++textureIdCount);
	}
    }

    void freeTexture2DId(int id) {
	FreeListManager.freeObject(FreeListManager.TEXTURE2D, new Integer(id));
    }

    void freeTexture3DId(int id) {
	FreeListManager.freeObject(FreeListManager.TEXTURE3D, new Integer(id));
    }

    int getCanvasId() {
        int i;

	synchronized(canvasIdLock) {
	    // Master control need to keep count itself        
	    for(i=canvasFreeIndex; i<canvasIds.length; i++) {
		if(canvasIds[i] == false)
		    break;
	    }

	    if ( canvasFreeIndex >= canvasIds.length) {
		throw new RuntimeException("Cannot render to more than 32 Canvas3Ds");
	    }

	    canvasIds[i] = true;
	    canvasFreeIndex = i + 1;
	}

        return i;
        
    }

    void freeCanvasId(int canvasId) {
        // Valid range is [0, 31]
	synchronized(canvasIdLock) {

	    canvasIds[canvasId] = false;
	    if(canvasFreeIndex > canvasId) {
		canvasFreeIndex = canvasId;
	    }
	}
    }

    // Fix to Issue 123
    Transform3D getTransform3D(Transform3D val) {
	Transform3D t;
        if (VirtualUniverse.mc.useFreeLists) {
            t = (Transform3D) FreeListManager.getObject(FreeListManager.TRANSFORM3D);
        }
        else {
            t = new Transform3D();
        }
	if (val != null) t.set(val);
	return t;
    }

    // Fix to Issue 123
    void addToTransformFreeList(Transform3D t) {
        if (VirtualUniverse.mc.useFreeLists) {
            FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t);
        }
    }


    ImageComponentUpdateInfo getFreeImageUpdateInfo() {
	ImageComponentUpdateInfo info;

	synchronized (imageUpdateInfoList) {
	    if (numFreeImageUpdateInfo > 0) {
		numFreeImageUpdateInfo--;
		info = (ImageComponentUpdateInfo)
			imageUpdateInfoList[numFreeImageUpdateInfo];
	    } else {
		info = new ImageComponentUpdateInfo();
	    }
	}
	return (info);
    }

    void addFreeImageUpdateInfo(ImageComponentUpdateInfo info) {
	synchronized (imageUpdateInfoList) {
	    if (imageUpdateInfoList.length == numFreeImageUpdateInfo) {
		ImageComponentUpdateInfo[] newFreeList =
		    new ImageComponentUpdateInfo[numFreeImageUpdateInfo * 2];
		System.arraycopy(imageUpdateInfoList, 0, newFreeList, 0,
					numFreeImageUpdateInfo);
		newFreeList[numFreeImageUpdateInfo++] = info;
		imageUpdateInfoList = newFreeList;
	    } else {
		imageUpdateInfoList[numFreeImageUpdateInfo++] = info;
	    }
	}
    }

    void addFreeImageUpdateInfo(ArrayList freeList) {
	ImageComponentUpdateInfo info;

	synchronized (imageUpdateInfoList) {
	    int len = numFreeImageUpdateInfo + freeList.size();

	    if (imageUpdateInfoList.length <= len) {
		ImageComponentUpdateInfo[] newFreeList =
		    new ImageComponentUpdateInfo[len * 2];
		System.arraycopy(imageUpdateInfoList, 0, newFreeList, 0,
					numFreeImageUpdateInfo);
		imageUpdateInfoList = newFreeList;
	    }

	    for (int i = 0; i < freeList.size(); i++) {
		info = (ImageComponentUpdateInfo) freeList.get(i);
		if (info != null) {
		    imageUpdateInfoList[numFreeImageUpdateInfo++] = info;
		}
	    }
	}
    }


    /**
     * Create a Renderer if it is not already done so.
     * This is used for GraphicsConfigTemplate3D passing 
     * graphics call to RequestRenderer, and for creating
     * an off-screen buffer for an off-screen Canvas3D.
     */
    private Renderer createRenderer(GraphicsConfiguration gc) {
	final GraphicsDevice gd = gc.getDevice();

	Renderer rdr = (Renderer) Screen3D.deviceRendererMap.get(gd);
	if (rdr != null) {
	    return rdr;
	}


	java.security.AccessController.doPrivileged(
	     new java.security.PrivilegedAction() {
                    public Object run() {
			Renderer r;
		        synchronized (rootThreadGroup) {
			    r = new Renderer(rootThreadGroup);
			    r.initialize();
			    r.setPriority(threadPriority);
			    Screen3D.deviceRendererMap.put(gd, r);
			}
			return null;
		   }
	});

	threadListsChanged = true;

	return (Renderer) Screen3D.deviceRendererMap.get(gd);
    }

    /**
     * Post the request in queue
     */
    void postRequest(Integer type, Object obj) {

	synchronized (mcThreadLock) {
	    synchronized (requestObjList) {
		if (mcThread == null) {
		    if ((type == ACTIVATE_VIEW) || 
			(type == GETBESTCONFIG) ||
			(type == SET_VIEW) ||
			(type == ISCONFIGSUPPORT) ||
			(type == SET_QUERYPROPERTIES) ||
			(type == SET_GRAPHICSCONFIG_FEATURES)) {
			createMasterControlThread();
			requestObjList.add(obj);
			requestTypeList.add(type);
			pendingRequest = true;
		    } else if (type == EMPTY_UNIVERSE) {
			destroyUniverseThreads((VirtualUniverse) obj);
		    } else if (type == STOP_VIEW) {
			View v = (View) obj;
			v.stopViewCount = -1;
			v.isRunning = false;
		    } else if (type == STOP_RENDERER) {
			if (obj instanceof Canvas3D) {
			    ((Canvas3D) obj).isRunningStatus = false;
			} else {
			    ((Renderer) obj).userStop = true;
			}
		    } else if (type == UNREGISTER_VIEW) {
			((View) obj).doneUnregister = true;
		    } else {
			requestObjList.add(obj);
			requestTypeList.add(type);
			pendingRequest = true;
		    }
		} else {
		    requestObjList.add(obj);
		    requestTypeList.add(type);
		    pendingRequest = true;
		}
	    }
	}

	setWork();
    }




    /**
     * This procedure is invoked when isRunning is false.  
     * Return true when there is no more pending request so that 
     * Thread can terminate. Otherwise we have to recreate
     * the MC related threads.
     */
    boolean mcThreadDone() {
	synchronized (mcThreadLock) {
	    synchronized (requestObjList) {
		if (!pendingRequest) {
		    mcThread = null;
		    if (renderingAttributesStructure.updateThread !=
			null) {
			renderingAttributesStructure.updateThread.finish();
			renderingAttributesStructure.updateThread =
			    null;
		    }
		    renderingAttributesStructure = new RenderingAttributesStructure();
		    if (timerThread != null) {
			timerThread.finish();
			timerThread = null;		
		    }    
		    if (notificationThread != null) {
			notificationThread.finish();
			notificationThread = null;
		    }
		    requestObjList.clear();
		    requestTypeList.clear();
		    return true;
		}
		running = true;
		createMCThreads();
		return false;
	    }
	}
    }

    /**
     * Returns whether we are using D3D.
     * TODO: most code that cares about this should move into the pipeline
     */
    final boolean isD3D() {
	return Pipeline.getPipeline().getRendererType() == Pipeline.Type.NATIVE_D3D;
    }

    /**
     * Returns whether we are running on Windows
     * TODO: most code that cares about this should move into the pipeline
     */
    final boolean isWindows() {
	return isWindowsOs;
    }

    /**
     * This method increments and returns the next time value
     * timeLock must get before this procedure is invoked
     */
    final long getTime() {
	return (time++);
    }

    

    /** 
     * This adds a BHNode to one of the list of BHNodes
     */
    // Fix to Issue 123
    void addBHNodeToFreelists(BHNode bH) {
	bH.parent = null;
	bH.mark = false;
	
	if (bH.nodeType == BHNode.BH_TYPE_INTERNAL) {
	    ((BHInternalNode)bH).lChild = null;
	    ((BHInternalNode)bH).rChild = null;
            if (VirtualUniverse.mc.useFreeLists) {
                FreeListManager.freeObject(FreeListManager.BHINTERNAL, bH);
            }
        }
	else if (bH.nodeType == BHNode.BH_TYPE_LEAF) {
	    ((BHLeafNode)(bH)).leafIF = null;
            if (VirtualUniverse.mc.useFreeLists) {
                FreeListManager.freeObject(FreeListManager.BHLEAF, bH);
            }
	}
    }
    
    /**
     * This gets a message from the free list.  If there isn't any,
     * it creates one.
     */
    // Fix to Issue 123
    BHNode getBHNode(int type) {
	
	if (type == BHNode.BH_TYPE_LEAF) {
            if (VirtualUniverse.mc.useFreeLists) {
                return (BHNode) FreeListManager.getObject(FreeListManager.BHLEAF);
            }
            else {
                return (BHNode) new BHLeafNode();
            }
	} 

	if (type == BHNode.BH_TYPE_INTERNAL) {
            if (VirtualUniverse.mc.useFreeLists) {
                return (BHNode) FreeListManager.getObject(FreeListManager.BHINTERNAL);
            }
            else {
                return (BHNode) new BHInternalNode();
            }
	}
	return null;
    }
    
    /** 
     * This adds a message to the list of messages
     */
    // Fix to Issue 123
    final void addMessageToFreelists(J3dMessage m) {
        if (VirtualUniverse.mc.useFreeLists) {
            FreeListManager.freeObject(FreeListManager.MESSAGE, m);
        }
    }

    /**
     * This gets a message from the free list.  If there isn't any,
     * it creates one.
     */
    // Fix to Issue 123
    final J3dMessage getMessage() {
        if (VirtualUniverse.mc.useFreeLists) {        
            return (J3dMessage) FreeListManager.getObject(FreeListManager.MESSAGE);
        }
        else {
            return new J3dMessage();
        }
    }
  
    /** 
     * This takes a given message and parses it out to the structures and
     * marks its time value.
     */
    void processMessage(J3dMessage message) {

        synchronized (timeLock) {
	    message.time = getTime();
	    sendMessage(message);
	}
	setWork();
    }
		
    /** 
     * This takes an array of messages and parses them out to the structures and
     * marks the time value. Make sure, setWork() is done at the very end
     * to make sure all the messages will be processed in the same frame
     */
    void processMessage(J3dMessage[] messages) {

        synchronized (timeLock) {
	    long time = getTime();

	    for (int i = 0; i < messages.length; i++) {
		messages[i].time = time;
		sendMessage(messages[i]);
	    }
	}
	setWork();
    }

    /**
     * This takes the specified notification message and sends it to the
     * notification thread for processing.
     */
    void sendNotification(J3dNotification notification) {
        notificationThread.addNotification(notification);
    }

    /**
     * Create and start the MasterControl Thread.
     */
    void createMasterControlThread() {
	running = true;
	workToDo = true;
	state = RUNNING;
	java.security.AccessController.doPrivileged(
	    new java.security.PrivilegedAction() {
                public Object run() {
		    synchronized (rootThreadGroup) {
			mcThread = new
			    MasterControlThread(rootThreadGroup);
			mcThread.setPriority(threadPriority);
		    }
		    return null;
		}
	});
    }

    // assuming the timeLock is already acquired

    /**
     * Send a message to another Java 3D thread.
     */
    void sendMessage(J3dMessage message) {

 	  synchronized (message) {
	    VirtualUniverse u = message.universe;
	    int targetThreads = message.threads;


	    //	    System.out.println("============> sendMessage");
	    //		dumpmsg(message);
	    if ((targetThreads & J3dThread.UPDATE_RENDERING_ATTRIBUTES) != 0) {
		renderingAttributesStructure.addMessage(message);
	    }

	    // GraphicsContext3D send message with universe = null
	    if (u != null) {
		if ((targetThreads & J3dThread.UPDATE_GEOMETRY) != 0) {
		    u.geometryStructure.addMessage(message);
		}
		if ((targetThreads & J3dThread.UPDATE_TRANSFORM) != 0) {
		    u.transformStructure.addMessage(message);
		}
		if ((targetThreads & J3dThread.UPDATE_BEHAVIOR) != 0) {
		    u.behaviorStructure.addMessage(message);
		}
		if ((targetThreads & J3dThread.UPDATE_SOUND) != 0) {
		    u.soundStructure.addMessage(message);
		}
		if ((targetThreads & J3dThread.UPDATE_RENDERING_ENVIRONMENT) != 0) {
		    u.renderingEnvironmentStructure.addMessage(message);
		}
	    }
	    
            if ((targetThreads & J3dThread.SOUND_SCHEDULER) != 0) {
                // Note that we don't check for active view
                if (message.view != null && message.view.soundScheduler != null ) {
                    // This make sure that message won't lost even
                    // though this view not yet register
                    message.view.soundScheduler.addMessage(message);
                } else {
		    synchronized (views) {
			View v[] = (View []) views.toArray(false);
			int i = views.arraySize()-1;
			if (u == null) {
			    while (i>=0) {
				v[i--].soundScheduler.addMessage(message);
			    }
			} else {
			    while (i>=0) {
				if (v[i].universe == u) {
				    v[i].soundScheduler.addMessage(message);
				}
				i--;
			    }
			}
		    }
                }    
            }	    

	    if ((targetThreads & J3dThread.UPDATE_RENDER) != 0) {
		// Note that we don't check for active view
		if (message.view != null && message.view.renderBin != null) {
		    // This make sure that message won't lost even
		    // though this view not yet register
		    message.view.renderBin.addMessage(message);
		} else {
		    synchronized (views) {
			View v[] = (View []) views.toArray(false);
			int i = i=views.arraySize()-1;
			if (u == null) {
			    while (i>=0) {
				v[i--].renderBin.addMessage(message);
			    }
			}
			else {
			    while (i>=0) {
				if (v[i].universe == u) { 
				    v[i].renderBin.addMessage(message);
				}
				i--;
			    }
			}
		    }
		}
	    }

	    if (message.getRefcount() == 0) {
		message.clear();
		addMessageToFreelists(message);
	    }
	  }
    }


    /**
     * Send a message to another Java 3D thread.
     * This variant is only call by TimerThread for Input Device Scheduler
     * or to redraw all View for RenderThread
     */
    void sendRunMessage(int targetThreads) {

	synchronized (timeLock) {

	    long time = getTime();

	    if ((targetThreads & J3dThread.INPUT_DEVICE_SCHEDULER) != 0) {
		synchronized (inputDeviceThreads) {
		    InputDeviceScheduler ds[] = (InputDeviceScheduler []) 
			inputDeviceThreads.toArray(false);
		    for (int i=inputDeviceThreads.size()-1; i >=0; i--) {
			if (ds[i].physicalEnv.activeViewRef > 0) {
			    ds[i].getThreadData().lastUpdateTime =
				time;
			}
		    }

		    // timerThread instance in MC will set to null in
		    // destroyUniverseThreads() so we need to check if
		    // TimerThread kick in to sendRunMessage() after that.
		    // It happens because TimerThread is the only thread run
		    // asychronizously with MasterControl thread. 

		    if (timerThread != null) {
			// Notify TimerThread to wakeup this procedure 
			// again next time. 
			timerThread.addInputDeviceSchedCond();
		    }
		}
	    }
	    if ((targetThreads & J3dThread.RENDER_THREAD) != 0) {
		synchronized (renderThreadData) {
		    J3dThreadData[] threads = (J3dThreadData []) 
			renderThreadData.toArray(false);
		    int i=renderThreadData.arraySize()-1;
		    J3dThreadData thr;
		    while (i>=0) {
			thr = threads[i--];
			if ( thr.view.renderBinReady) {
			    thr.lastUpdateTime = time;
			}
		    }
		}
	    }
	}
	setWork();
    }

    /**
     * Send a message to another Java 3D thread.
     * This variant is only call by TimerThread for Sound Scheduler
     */
    void sendRunMessage(long waitTime, View view, int targetThreads) {

	synchronized (timeLock) {

	    long time = getTime();

	    if ((targetThreads & J3dThread.SOUND_SCHEDULER) != 0) {
		if (view.soundScheduler != null)  {
		    view.soundScheduler.threadData.lastUpdateTime = time;
		}
		// wakeup this procedure next time
		// QUESTION: waitTime calculated some milliseconds BEFORE
		//     this methods getTime() called - shouldn't actual 
		//     sound Complete time be passed by SoundScheduler
		// QUESTION: will this wake up only soundScheduler associated
		//     with this view?? (since only it's lastUpdateTime is set)
		//     or all soundSchedulers??
		timerThread.addSoundSchedCond(time+waitTime);
	    }
	}
	setWork();
    }

    /**
     * Send a message to another Java 3D thread.
     * This variant is only called to update Render Thread
     */
    void sendRunMessage(View v, int targetThreads) {

	synchronized (timeLock) {
	    long time = getTime();

	    if ((targetThreads & J3dThread.RENDER_THREAD) != 0) {
		synchronized (renderThreadData) {
		    J3dThreadData[] threads = (J3dThreadData []) 
			renderThreadData.toArray(false);
		    int i=renderThreadData.arraySize()-1;
		    J3dThreadData thr;
		    while (i>=0) {
			thr = threads[i--];
			if (thr.view == v && v.renderBinReady) {
			    thr.lastUpdateTime = time;
			}
		    }
		}
	    }
	}
	setWork();
    }


    /**
     * This sends a run message to the given threads.
     */
    void sendRunMessage(VirtualUniverse u, int targetThreads) {
	// We don't sendRunMessage to update structure except Behavior

	synchronized (timeLock) {
	    long time = getTime();

	    if ((targetThreads & J3dThread.BEHAVIOR_SCHEDULER) != 0) {
		if (u.behaviorScheduler != null) {
		    u.behaviorScheduler.getThreadData(null,
						      null).lastUpdateTime = time;
		}
	    }
	    
	    if ((targetThreads & J3dThread.UPDATE_BEHAVIOR) != 0) {
		u.behaviorStructure.threadData.lastUpdateTime = time;
	    }
	    
	    if ((targetThreads & J3dThread.UPDATE_GEOMETRY) != 0) {
		u.geometryStructure.threadData.lastUpdateTime = time;
	    }
	    
	    if ((targetThreads & J3dThread.UPDATE_SOUND) != 0) {
		u.soundStructure.threadData.lastUpdateTime = time;
	    }
	    
	    if ((targetThreads & J3dThread.SOUND_SCHEDULER) != 0) {
		synchronized (views) {
		    View v[] = (View []) views.toArray(false);
		    for (int i= views.arraySize()-1; i >=0; i--) {
			if ((v[i].soundScheduler != null) &&
			    (v[i].universe == u)) {
			    v[i].soundScheduler.threadData.lastUpdateTime = time;
			}
		    }
		}
	    }

	    if ((targetThreads & J3dThread.RENDER_THREAD) != 0) {
		
		synchronized (renderThreadData) {
		    J3dThreadData[] threads = (J3dThreadData []) 
			renderThreadData.toArray(false);
		    int i=renderThreadData.arraySize()-1;
		    J3dThreadData thr;
		    while (i>=0) {
			thr = threads[i--];
			if (thr.view.universe == u && thr.view.renderBinReady) {
			    thr.lastUpdateTime = time;
			}
		    }
		}
	    }
	}

	setWork();
    }


    /**
     * Return a clone of View, we can't access 
     * individual element of View after getting the size
     * in separate API call without synchronized views.
     */
    UnorderList cloneView() {
	return (UnorderList) views.clone();
    }
	
    /**
     * Return true if view is already registered with MC
     */
    boolean isRegistered(View view) {
	return views.contains(view);
    }

    /**
     * This snapshots the time values to be used for this iteration.
     * Note that this method is called without the timeLock held.
     * We must synchronize on timeLock to prevent updating
     * thread.lastUpdateTime from user thread in sendMessage()
     * or sendRunMessage().
     */
    private void updateTimeValues() {
        synchronized (timeLock) {
            int i=0;
            J3dThreadData lastThread=null;
            J3dThreadData thread=null;
            long lastTime = currentTime;

            currentTime = getTime();

            J3dThreadData threads[] = (J3dThreadData []) 
                                    stateWorkThreads.toArray(false);
            int size = stateWorkThreads.arraySize();

            while (i<lastTransformStructureThread) {
                thread = threads[i++];

                if ((thread.lastUpdateTime > thread.lastRunTime) && 
                    !thread.thread.userStop) {
                    lastThread = thread;
                    thread.needsRun = true;
                    thread.threadOpts = J3dThreadData.CONT_THREAD;
                    thread.lastRunTime =  currentTime;
                } else {
                    thread.needsRun = false;
                }
            }

            if (lastThread != null) {
                lastThread.threadOpts =  J3dThreadData.WAIT_ALL_THREADS;
                lastThread = null;
            }

            while (i<lastStructureUpdateThread) {
                thread = threads[i++];
                if ((thread.lastUpdateTime > thread.lastRunTime) &&
                    !thread.thread.userStop) {
                    lastThread = thread;
                    thread.needsRun = true;
                    thread.threadOpts = J3dThreadData.CONT_THREAD;
                    thread.lastRunTime = currentTime;
                } else {
                    thread.needsRun = false;
                }
            }
            if (lastThread != null) {
                lastThread.threadOpts = J3dThreadData.WAIT_ALL_THREADS;
                lastThread = null;
            }

            while (i<size) {
                thread = threads[i++];
                if ((thread.lastUpdateTime > thread.lastRunTime) && 
                    !thread.thread.userStop) {
                    lastThread = thread;
                    thread.needsRun = true;
                    thread.threadOpts = J3dThreadData.CONT_THREAD;
                    thread.lastRunTime = currentTime;
                } else {
                    thread.needsRun = false;
                }
            }
            if (lastThread != null) {
                lastThread.threadOpts = J3dThreadData.WAIT_ALL_THREADS;
                lastThread = null;
            }


            threads = (J3dThreadData []) renderWorkThreads.toArray(false);
            size = renderWorkThreads.arraySize();
            View v;
            J3dThreadData lastRunThread = null;
            waitTimestamp++;
            sleepTime = 0L;

            boolean threadToRun = false; // Not currently used

            // Fix for Issue 12: loop through the list of threads, calling
            // computeCycleTime() exactly once per view. This ensures that
            // all threads for a given view see consistent values for
            // isMinCycleTimeAchieve and sleepTime.
            v = null;
            for (i=0; i<size; i++) {
                thread = threads[i];
                if (thread.view != v) {
                    thread.view.computeCycleTime();
                    // Set sleepTime to the value needed to satify the
                    // minimum cycle time of the slowest view
                    if (thread.view.sleepTime > sleepTime) {
                        sleepTime = thread.view.sleepTime;
                    }
                }
                v = thread.view;
            }

            v = null;
            for (i=0; i<size; i++) {
                thread = threads[i];
                if (thread.canvas == null) { // Only for swap thread
                    ((Object []) thread.threadArgs)[3] = null;
                }
                if ((thread.lastUpdateTime > thread.lastRunTime) &&
                    !thread.thread.userStop) {

                    if (thread.thread.lastWaitTimestamp == waitTimestamp) {
                        // This renderer thread is repeated. We must wait 
                        // until all previous renderer threads done before
                        // allowing this thread to continue. Note that
                        // lastRunThread can't be null in this case.
                        waitTimestamp++;
                        if (thread.view != v) {
                            // A new View is start
                            v = thread.view;
                            threadToRun = true;
                            lastRunThread.threadOpts =
                                (J3dThreadData.STOP_TIMER |
                                 J3dThreadData.WAIT_ALL_THREADS);
                            ((Object []) lastRunThread.threadArgs)[3] = lastRunThread.view;
                            thread.threadOpts = (J3dThreadData.START_TIMER |
                                                 J3dThreadData.CONT_THREAD);
                        } else {
                            if ((lastRunThread.threadOpts &
                                 J3dThreadData.START_TIMER) != 0) {
                                lastRunThread.threadOpts = 
                                    (J3dThreadData.START_TIMER |
                                     J3dThreadData.WAIT_ALL_THREADS);

                            } else {
                                lastRunThread.threadOpts =
                                    J3dThreadData.WAIT_ALL_THREADS;
                            }
                            thread.threadOpts = J3dThreadData.CONT_THREAD;

                        }
                    } else {
                        if (thread.view != v) {
                            v = thread.view;
                            threadToRun = true;
                            // Although the renderer thread is not
                            // repeated. We still need to wait all
                            // previous renderer threads if new View
                            // start.
                            if (lastRunThread != null) {
                                lastRunThread.threadOpts =
                                    (J3dThreadData.STOP_TIMER |
                                     J3dThreadData.WAIT_ALL_THREADS);
                                ((Object []) lastRunThread.threadArgs)[3]
                                    = lastRunThread.view;
                            }
                            thread.threadOpts = (J3dThreadData.START_TIMER |
                                                 J3dThreadData.CONT_THREAD);
                        } else {
                            thread.threadOpts = J3dThreadData.CONT_THREAD;
                        }
                    }
                    thread.thread.lastWaitTimestamp = waitTimestamp;
                    thread.needsRun = true;
                    thread.lastRunTime = currentTime;
                    lastRunThread = thread;
                } else {
                    thread.needsRun = false;
                }
            }


            if (lastRunThread != null) {
                lastRunThread.threadOpts = 
                                    (J3dThreadData.STOP_TIMER |
                                     J3dThreadData.WAIT_ALL_THREADS|
                                     J3dThreadData.LAST_STOP_TIMER);
                lockGeometry = true; 
                ((Object []) lastRunThread.threadArgs)[3] = lastRunThread.view;
            }  else {
                lockGeometry = false;
            }
        }

        // Issue 275 - go to sleep without holding timeLock
	// Sleep for the amount of time needed to satisfy the minimum
	// cycle time for all views.
	if (sleepTime > 0) {
	    // System.err.println("MasterControl: sleep(" + sleepTime + ")");
	    try {
		Thread.sleep(sleepTime);
	    } catch (InterruptedException e) {
		System.err.println(e);
	    }
	    // System.err.println("MasterControl: done sleeping");
	}
    }

    private void createUpdateThread(J3dStructure structure) {
	final J3dStructure s = structure;

	if (s.updateThread == null) {
	    java.security.AccessController.doPrivileged(
	        new java.security.PrivilegedAction() {
                   public Object run() {
		       synchronized (rootThreadGroup) {
                           s.updateThread = new StructureUpdateThread(
                   	        rootThreadGroup, s, s.threadType);
			   s.updateThread.setPriority(threadPriority);
		       }
		       return null;
		   }
	    });
	    s.updateThread.initialize();
	    s.threadData.thread = s.updateThread;
	    // This takes into accout for thread that just destroy and
	    // create again. In this case the threadData may receive
	    // message before the thread actually created. We don't want 
	    // the currentTime to overwrite the update time of which
	    // is set by threadData when get message.
	    s.threadData.lastUpdateTime = Math.max(currentTime,
						   s.threadData.lastUpdateTime);
	} 
    }

    private void emptyMessageList(J3dStructure structure, View v) {
	if (structure != null) {
	    if (v == null) {
		if (structure.threadData != null) {
		    structure.threadData.thread = null;
		}
	    
		if (structure.updateThread != null) {
		    structure.updateThread.structure = null;
		}
		structure.updateThread = null;
	    }
	    boolean otherViewExist = false;
	    if ((v != null) && (v.universe != null)) {
		// Check if there is any other View register with the
		// same universe
		for (int i=views.size()-1; i >= 0; i--) {
		    if (((View) views.get(i)).universe == v.universe) {
			otherViewExist = true;
			break;
		    }
		}
	    }


	    UnorderList mlist = structure.messageList;
	    // Note that message is add at the end of array
	    synchronized (mlist) {
		int size = mlist.size();
		if (size > 0) {
		    J3dMessage mess[] = (J3dMessage []) mlist.toArray(false);
		    J3dMessage m;
		    int i = 0;

		    Object oldRef= null;
		    while (i < size) {
			m = mess[i];
			if ((v == null) || (m.view == v) || 
			    ((m.view == null) && !otherViewExist)) {
			    if (m.type == J3dMessage.INSERT_NODES) {
				// There is another View register request
				// immediately following, so no need 
				// to remove message.
				break;
			    }
			    // Some other thread may still using this
			    // message so we should not directly
			    // add this message to free lists
			    m.decRefcount();
			    mlist.removeOrdered(i);
			    size--;
			} else {
			    i++;
			}
		    }
		}
	    }
	}
    }

    private void destroyUpdateThread(J3dStructure structure) {
	// If unregisterView message got before EMPTY_UNIVERSE
	// message, then updateThread is already set to null.
	if (structure.updateThread != null) {
	    structure.updateThread.finish();
	    structure.updateThread.structure = null;
	    structure.updateThread = null;
	}
	structure.threadData.thread = null;
	structure.clearMessages();
    }

    /**
     * This register a View with MasterControl.  
     * The View has at least one Canvas3D added to a container.
     */
    private void registerView(View v) {
	final VirtualUniverse univ = v.universe;

	if (views.contains(v) && regUniverseList.contains(univ)) {
	    return;  // already register
	}

	if (timerThread == null) {
	    // This handle the case when MC shutdown and restart in
	    // a series of pending request
	    running = true;
	    createMCThreads();
	}
	// If viewId is null, assign one ..
	v.assignViewId();

	// Create thread if not done before
	createUpdateThread(univ.behaviorStructure);
	createUpdateThread(univ.geometryStructure);
	createUpdateThread(univ.soundStructure);
	createUpdateThread(univ.renderingEnvironmentStructure);
	createUpdateThread(univ.transformStructure);
	
	// create Behavior scheduler
	J3dThreadData threadData = null;
	
	if (univ.behaviorScheduler == null) {
	    java.security.AccessController.doPrivileged(
		new java.security.PrivilegedAction() {
                       public Object run() {
			   synchronized (rootThreadGroup) {
			       univ.behaviorScheduler = new BehaviorScheduler(
						      rootThreadGroup, univ);
			       univ.behaviorScheduler.setPriority(threadPriority);
			   }
			   return null;
		       }
	    });
	    univ.behaviorScheduler.initialize();
	    univ.behaviorScheduler.userStop = v.stopBehavior;
	    threadData = univ.behaviorScheduler.getThreadData(null, null);
	    threadData.thread = univ.behaviorScheduler;		
	    threadData.threadType = J3dThread.BEHAVIOR_SCHEDULER;
	    threadData.lastUpdateTime = Math.max(currentTime,
						 threadData.lastUpdateTime);
	} 
	
	createUpdateThread(v.renderBin);
	createUpdateThread(v.soundScheduler);

	if (v.physicalEnvironment != null) {
	    v.physicalEnvironment.addUser(v);
	}
	// create InputDeviceScheduler
	evaluatePhysicalEnv(v);	

	regUniverseList.addUnique(univ);
	views.addUnique(v);	
    }



    /**
     * This unregister a View with MasterControl.  
     * The View no longer has any Canvas3Ds in a container.
     */
    private void unregisterView(View v) {

	if (!views.remove(v)) {
	    v.active = false;
	    v.doneUnregister = true;
	    return; // already unregister
	}

	if (v.active) {
	    viewDeactivate(v);
	}

	if(J3dDebug.devPhase) {
	    J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
			     "MC: Destroy Sound Scheduler and RenderBin Update thread");
	}

	v.soundScheduler.updateThread.finish();
	v.renderBin.updateThread.finish();
	v.soundScheduler.updateThread = null;
	v.renderBin.updateThread = null;

	// remove VirtualUniverse related threads if Universe
	// is empty
        VirtualUniverse univ = v.universe;
	int i;

	synchronized (timeLock) {
	    // The reason we need to sync. with timeLock is because we
	    // don't want user thread running sendMessage() to 
	    // dispatch it in different structure queue when
	    // part of the structure list is empty at the same time.
	    // This will cause inconsistence in the message reference
	    // count.
	    emptyMessageList(v.soundScheduler, v);
	    emptyMessageList(v.renderBin, v);
	    
	    if (univ.isEmpty()) {
		destroyUniverseThreads(univ);
	    }  else {
		emptyMessageList(univ.behaviorStructure, v);
		emptyMessageList(univ.geometryStructure, v);
		emptyMessageList(univ.soundStructure, v);
		emptyMessageList(univ.renderingEnvironmentStructure, v);
		emptyMessageList(univ.transformStructure, v);
	    }
	}

	if (v.physicalEnvironment != null) {
	    v.physicalEnvironment.removeUser(v);
	}
	    
	// remove all InputDeviceScheduler if this is the last View
	UnorderList list = new UnorderList(1, PhysicalEnvironment.class);
	for (Enumeration e = PhysicalEnvironment.physicalEnvMap.keys();
	     e.hasMoreElements(); ) {
	    PhysicalEnvironment phyEnv = (PhysicalEnvironment) e.nextElement();
	    InputDeviceScheduler sched = (InputDeviceScheduler)
		PhysicalEnvironment.physicalEnvMap.get(phyEnv);
	    for (i=phyEnv.users.size()-1; i>=0; i--) {
		if (views.contains((View) phyEnv.users.get(i))) {
		    // at least one register view refer to it.
		    break;
		}
	    }
	    if (i < 0) {
		if(J3dDebug.devPhase) {
		    J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
				     "MC: Destroy InputDeviceScheduler thread "
				     + sched);
		}
		sched.finish();
		phyEnv.inputsched = null;
		list.add(phyEnv);
	    }
	}
	for (i=list.size()-1; i>=0; i--) {
	    PhysicalEnvironment.physicalEnvMap.remove(list.get(i));
	}

	
	freeContext(v);

	if (views.isEmpty()) {
	    if(J3dDebug.devPhase) {
		J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
				 "MC: Destroy all Renderers");
	    }
	    // remove all Renderers if this is the last View
	    for (Enumeration e = Screen3D.deviceRendererMap.elements();
		 e.hasMoreElements(); ) {
		Renderer rdr = (Renderer) e.nextElement();
		Screen3D scr;
		
		rendererCleanupArgs[2] = REMOVEALLCTXS_CLEANUP;
		runMonitor(RUN_RENDERER_CLEANUP, null, null, null, rdr);
		scr = rdr.onScreen;
		if (scr != null) {
		    if (scr.renderer != null) {
			rendererCleanupArgs[2] = REMOVEALLCTXS_CLEANUP;
			runMonitor(RUN_RENDERER_CLEANUP, null, null,
				   null, scr.renderer);
			scr.renderer = null;
		    }
			
		}
		scr = rdr.offScreen;
		if (scr != null) {
		    if (scr.renderer != null) {
			rendererCleanupArgs[2] = REMOVEALLCTXS_CLEANUP;
			runMonitor(RUN_RENDERER_CLEANUP, null, null, 
				   null, scr.renderer);
			scr.renderer = null;
		    }
		}
		rdr.onScreen = null;
		rdr.offScreen = null;
	    }

	    // cleanup ThreadData corresponds to the view in renderer
	    for (Enumeration e = Screen3D.deviceRendererMap.elements();
		 e.hasMoreElements(); ) {
		Renderer rdr = (Renderer) e.nextElement();
		rdr.cleanup();
	    }
	    // We have to reuse renderer even though MC exit
	    // see bug 4363279
	    //  Screen3D.deviceRendererMap.clear();

	} else {
	    // cleanup ThreadData corresponds to the view in renderer
	    for (Enumeration e = Screen3D.deviceRendererMap.elements();
		 e.hasMoreElements(); ) {
		Renderer rdr = (Renderer) e.nextElement();
		rdr.cleanupView();
	    }
	}


	freeMessageList.add(univ);
	freeMessageList.add(v);

	evaluateAllCanvases();
	stateWorkThreads.clear();
	renderWorkThreads.clear();
	requestRenderWorkThreads.clear();
	threadListsChanged = true;
	
	// This notify VirtualUniverse waitForMC() thread to continue
	v.doneUnregister = true;
    }


    /**
     * This procedure create MC thread that start together with MC.
     */
    void createMCThreads() {

	// There is only one renderingAttributesUpdate Thread globally
	createUpdateThread(renderingAttributesStructure);

	// Create timer thread
	java.security.AccessController.doPrivileged(
			    new java.security.PrivilegedAction() {
              public Object run() {
		  synchronized (rootThreadGroup) {
		      timerThread = new TimerThread(rootThreadGroup);
		      timerThread.setPriority(threadPriority);
		  }
		  return null;
	      }
	});
	timerThread.start();

        // Create notification thread
	java.security.AccessController.doPrivileged(
			    new java.security.PrivilegedAction() {
              public Object run() {
		  synchronized (rootThreadGroup) {
		      notificationThread = new NotificationThread(rootThreadGroup);
		      notificationThread.setPriority(threadPriority);
		  }
		  return null;
	      }
	});
	notificationThread.start();
    }

    /**
     * Destroy all VirtualUniverse related threads.
     * This procedure may call two times when Locale detach in a
     * live viewPlatform.
     */
    private void destroyUniverseThreads(VirtualUniverse univ) {

	if (regUniverseList.contains(univ)) {
	    if (J3dDebug.devPhase) {
		J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
				 "MC: Destroy universe threads " + univ);
	    }
	    destroyUpdateThread(univ.behaviorStructure);
	    destroyUpdateThread(univ.geometryStructure);
	    destroyUpdateThread(univ.soundStructure);
	    destroyUpdateThread(univ.renderingEnvironmentStructure);
	    destroyUpdateThread(univ.transformStructure);
	    univ.behaviorScheduler.finish();
	    univ.behaviorScheduler.free();
	    univ.behaviorScheduler = null;
	    univ.initMCStructure();
	    activeUniverseList.remove(univ);
	    regUniverseList.remove(univ);	
	} else {
	    emptyMessageList(univ.behaviorStructure, null);
	    emptyMessageList(univ.geometryStructure, null);
	    emptyMessageList(univ.soundStructure, null);
	    emptyMessageList(univ.renderingEnvironmentStructure, null);
	    emptyMessageList(univ.transformStructure, null);
	}

	if (regUniverseList.isEmpty() && views.isEmpty()) {
	    if(J3dDebug.devPhase) {
		J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
				 "MC: Destroy RenderingAttributes Update and Timer threads");
	    }	    
	    if (renderingAttributesStructure.updateThread != null) {
		renderingAttributesStructure.updateThread.finish();
		renderingAttributesStructure.updateThread = null;
	    }
	    renderingAttributesStructure.messageList.clear();
	    renderingAttributesStructure.objList = new ArrayList();
	    renderingAttributesStructure = new RenderingAttributesStructure();
	    if (timerThread != null) {
		timerThread.finish();
		timerThread = null;
	    }
            if (notificationThread != null) {
                notificationThread.finish();
                notificationThread = null;
            }

	    // shouldn't all of these be synchronized ???
	    synchronized (VirtualUniverse.mc.deviceScreenMap) {
		deviceScreenMap.clear();
	    }
            if (VirtualUniverse.mc.useFreeLists) {
                FreeListManager.clearList(FreeListManager.MESSAGE);
                FreeListManager.clearList(FreeListManager.BHLEAF);
                FreeListManager.clearList(FreeListManager.BHINTERNAL);
                FreeListManager.clearList(FreeListManager.TRANSFORM3D);
            }
	    mirrorObjects.clear();
	    // Note: We should not clear the DISPLAYLIST/TEXTURE
	    // list here because other structure may release them
	    // later 

	    for(int i=0; i<canvasIds.length; i++) {
		canvasIds[i] = false;
	    }
	    canvasFreeIndex = 0;
	    
	    renderOnceList.clear();
	    timestampUpdateList.clear();

	    defaultRenderMethod = null;
	    text3DRenderMethod = null;
	    vertexArrayRenderMethod = null;
	    displayListRenderMethod = null;
	    compressedGeometryRenderMethod = null;
	    orientedShape3DRenderMethod = null;
	    // Terminate MC thread
	    running = false;
	}
    }

    /**
     * Note that we have to go through all views instead of
     * evaluate only the canvas in a single view since each screen
     * may share by multiple view
     */
    private void evaluateAllCanvases() {

	synchronized (renderThreadData) {
	    // synchronized to prevent lost message when
	    // renderThreadData is clear

	    // First remove all renderrenderThreadData
	    renderThreadData.clear();

	    // Second reset canvasCount to zero
	    View viewArr[] = (View []) views.toArray(false);
	    for (int i=views.size()-1; i>=0; i--) {
		viewArr[i].getCanvasList(true); // force canvas cache update
		Screen3D screens[] = viewArr[i].getScreens();
		for (int j=screens.length-1; j>=0; j--) {
		    screens[j].canvasCount = 0;
		}
	    }


	    // Third create render thread and message thread
	    for (int i=views.size()-1; i>=0; i--) {
		View v = viewArr[i];
		Canvas3D canvasList[][] = v.getCanvasList(false);
		if (!v.active) {
		    continue;	    
		}
		
		for (int j=canvasList.length-1; j>=0; j--) {
		    boolean added = false;

		    for (int k=canvasList[j].length-1; k>=0; k--) {
			Canvas3D cv = canvasList[j][k];
			
			final Screen3D screen  = cv.screen;

			if (cv.active) {
			    if (screen.canvasCount++ == 0) {
				// Create Renderer, one per screen
				if (screen.renderer == null) {
                    	        // get the renderer created for the graphics 
			        // device of the screen of the canvas
				// No need to synchronized since only
				// MC use it. 
				    Renderer rdr = 
					(Renderer) screen.deviceRendererMap.get(
							cv.screen.graphicsDevice);
				    if (rdr == null) {
					java.security.AccessController.doPrivileged(
					    new java.security.PrivilegedAction() {
                                              public Object run() {
						  
						  synchronized (rootThreadGroup) {
						      screen.renderer
							  = new Renderer(
 							      rootThreadGroup);
						      screen.renderer.setPriority(threadPriority);
						  }
                                                 return null;
                                          }
					});
					screen.renderer.initialize();
					screen.deviceRendererMap.put(
					     screen.graphicsDevice, screen.renderer);
				    } else {
					screen.renderer = rdr;
				    }
				} 
			    }
		    	    // offScreen canvases will be handled by the
			    // request renderer, so don't add offScreen canvas
			    // the render list
                            //
                            // Issue 131: Automatic offscreen canvases need to
                            // be added to onscreen list. Special case.
                            //
                            // TODO KCR Issue 131: this should probably be
                            // changed to a list of screens since multiple
                            // off-screen canvases (either auto or manual) can
                            // be used by the same renderer
		            if (!cv.manualRendering) {
				screen.renderer.onScreen = screen;
			    } else {
				screen.renderer.offScreen = screen;
				continue;
			    }

			    if (!added) {
				// Swap message data thread, one per 
				// screen only. Note that we don't set
				// lastUpdateTime for this thread so
				// that it won't run in the first round
			        J3dThreadData renderData = 
			      	    screen.renderer.getThreadData(v, null);
				renderThreadData.add(renderData);

				// only if renderBin is ready then we
				// update the lastUpdateTime to make it run 
				if (v.renderBinReady) {
	    		    	    renderData.lastUpdateTime = 
					Math.max(currentTime,
						   renderData.lastUpdateTime);
				}
				added = true;
			    }
			    // Renderer message data thread
			    J3dThreadData renderData = 
			      	    screen.renderer.getThreadData(v, cv);
			    renderThreadData.add(renderData);
			    if (v.renderBinReady) {
	    		    	renderData.lastUpdateTime = 
					Math.max(currentTime,
						   renderData.lastUpdateTime);
			    }
			}
		    }
		}
		
	    }
	}

	threadListsChanged = true;
    }

    private void evaluatePhysicalEnv(View v) {
	final PhysicalEnvironment env = v.physicalEnvironment;

	if (env.inputsched == null) {
	    java.security.AccessController.doPrivileged(
		 new java.security.PrivilegedAction() {
                      public Object run() {
			  synchronized (rootThreadGroup) {
			      env.inputsched = new InputDeviceScheduler(
							rootThreadGroup,
							env);
			      env.inputsched.setPriority(threadPriority);
			  }
			  return null;
		      }
	    });
	    env.inputsched.start();
	    PhysicalEnvironment.physicalEnvMap.put(env, env.inputsched);
	}
	threadListsChanged = true;
    }
   
    final private void addToStateThreads(J3dThreadData threadData) {
	if (threadData.thread.active) {
	    stateWorkThreads.add(threadData);
	}
    }


    private void assignNewPrimaryView(VirtualUniverse univ) {

	View currentPrimary = univ.getCurrentView();

	if (currentPrimary != null) {
	    currentPrimary.primaryView = false;
	}

	View v[] = (View []) views.toArray(false);
	int nviews = views.size();
	for (int i=0; i<nviews; i++) {
	    View view = v[i];
	    if (view.active && view.isRunning &&
		(univ == view.universe)) {
		view.primaryView = true;
		univ.setCurrentView(view);
		return;
	    }
	}
	univ.setCurrentView(null);
    }
    

    /**
     * This returns the default RenderMethod
     */
    RenderMethod getDefaultRenderMethod() {
	if (defaultRenderMethod == null) {
	    defaultRenderMethod = new DefaultRenderMethod();
	}
	return defaultRenderMethod;
    }

    /**
     * This returns the text3d RenderMethod
     */
    RenderMethod getText3DRenderMethod() {
	if (text3DRenderMethod == null) {
	    text3DRenderMethod = new Text3DRenderMethod();
	}
	return text3DRenderMethod;
    }


    /**
     * This returns the vertexArray RenderMethod
     */
    RenderMethod getVertexArrayRenderMethod() {
	if (vertexArrayRenderMethod == null) {
	    vertexArrayRenderMethod = new VertexArrayRenderMethod();
	}
	return vertexArrayRenderMethod;
    }

    /**
     * This returns the displayList RenderMethod
     */
    RenderMethod getDisplayListRenderMethod() {
        if (displayListRenderMethod == null) {
            displayListRenderMethod = new DisplayListRenderMethod();
        }
        return displayListRenderMethod;
    }

    /**
     * This returns the compressed geometry RenderMethod
     */
    RenderMethod getCompressedGeometryRenderMethod() {
        if (compressedGeometryRenderMethod == null) {
            compressedGeometryRenderMethod =
		new CompressedGeometryRenderMethod();
        }
        return compressedGeometryRenderMethod;
    }

    /**
     * This returns the oriented shape3d RenderMethod
     */
    RenderMethod getOrientedShape3DRenderMethod() {
	if (orientedShape3DRenderMethod == null) {
	    orientedShape3DRenderMethod = new OrientedShape3DRenderMethod();
	}
	return orientedShape3DRenderMethod;
    }

    /** 
     * This notifies MasterControl that the given view has been activated
     */
    private void viewActivate(View v) {

	VirtualUniverse univ = v.universe;

	if (univ == null) {
	    return;
	}

	if (!views.contains(v) || !regUniverseList.contains(univ)) {
	    registerView(v);
	} else if (v.active) {
	    evaluateAllCanvases();
	    return;
	}

	if ((univ.activeViewCount == 0)) {
	    univ.geometryStructure.resetConditionMet();
	    univ.behaviorStructure.resetConditionMet();
	}

	if (v.isRunning) {
	    numActiveViews++;
	    univ.activeViewCount++;
	    renderingAttributesStructure.updateThread.active = true;
	    univ.transformStructure.updateThread.active = true;
	    univ.geometryStructure.updateThread.active = true;
	    univ.soundStructure.updateThread.active = true;
	    univ.renderingEnvironmentStructure.updateThread.active = true;
	}
	univ.behaviorScheduler.active = true;
	univ.behaviorStructure.updateThread.active = true;


	activeUniverseList.addUnique(univ);

	if (v.isRunning) {
	    v.soundScheduler.activate();
	    v.renderBin.updateThread.active = true;
	}
	v.active = true;

	if (v.physicalEnvironment.activeViewRef++ == 0) {
	    v.physicalEnvironment.inputsched.activate();
	}


	if (univ.getCurrentView() == null) {
	    assignNewPrimaryView(univ);
	}

	evaluateAllCanvases();
	v.inRenderThreadData = true;
	threadListsChanged = true;
	// Notify GeometryStructure to query visible atom again
	// We should send message instead of just setting
	// v.vDirtyMask = View.VISIBILITY_POLICY_DIRTY;  
	// since RenderBin may not run immediately next time.
	// In this case the dirty flag will lost since 
	// updateViewCache() will reset it to 0.
	v.renderBin.reactivateView = true;
    }

    /**
     * Release context associate with view
     */
    private void freeContext(View v) {
	Canvas3D[][] canvasList = v.getCanvasList(false);

	for (int j=canvasList.length-1; j>=0; j--) {
	    for (int k=canvasList[j].length-1; k>=0; k--) {
		Canvas3D cv = canvasList[j][k];
		if (!cv.validCanvas) {
		    if ((cv.screen != null) && 
			(cv.screen.renderer != null)) {
			rendererCleanupArgs[1] = cv;
			rendererCleanupArgs[2] = FREECONTEXT_CLEANUP;
			runMonitor(RUN_RENDERER_CLEANUP, null, null, null,
				   cv.screen.renderer);
			rendererCleanupArgs[1] = null;
		    }
		}
	    }
	}
    }

    /** 
     * This notifies MasterControl that the given view has been deactivated
     */
    private void viewDeactivate(View v) {

	if (!views.contains(v) || !v.active) {
	    v.active = false;
	    evaluateAllCanvases();
	    return;
	}

	VirtualUniverse univ = v.universe;

	if (v.isRunning) {
	    // if stopView() invoke before, no need to decrement count
	    --numActiveViews;
	    --univ.activeViewCount;
	}
	 
	if (numActiveViews == 0) {
	    renderingAttributesStructure.updateThread.active = false;
	}

	if (univ.activeViewCount == 0) {
	    // check if destroyUniverseThread invoked before
	    if (univ.behaviorScheduler != null) {
		univ.behaviorScheduler.deactivate();
		univ.transformStructure.updateThread.active = false;
		univ.geometryStructure.updateThread.active = false;
		univ.behaviorStructure.updateThread.active = false;
		univ.soundStructure.updateThread.active = false;
		univ.renderingEnvironmentStructure.updateThread.active
		    = false;
		activeUniverseList.remove(univ);
	    }
	}

	v.soundScheduler.deactivate();
	v.renderBin.updateThread.active = false;
	v.active = false;
	if (--v.physicalEnvironment.activeViewRef == 0) {
	    v.physicalEnvironment.inputsched.deactivate();
	}
	assignNewPrimaryView(univ);


	evaluateAllCanvases();

	freeContext(v);

	v.inRenderThreadData = false;
	threadListsChanged = true;
    }


   /** 
     * This notifies MasterControl to start given view
     */
    private void startView(View v) {

	if (!views.contains(v) || v.isRunning || !v.active) {
	    v.isRunning = true;
	    return;
	}

	numActiveViews++;
	renderingAttributesStructure.updateThread.active = true;

	VirtualUniverse univ = v.universe;

	univ.activeViewCount++;
	univ.transformStructure.updateThread.active = true;
	univ.geometryStructure.updateThread.active = true;
	univ.soundStructure.updateThread.active = true;
	univ.renderingEnvironmentStructure.updateThread.active = true;
	v.renderBin.updateThread.active = true;
	v.soundScheduler.activate();
	v.isRunning = true;
	if (univ.getCurrentView() == null) {
	    assignNewPrimaryView(univ);
	}
	threadListsChanged = true;
    }


   /** 
     * This notifies MasterControl to stop given view
     */
    private void stopView(View v) {
	if (!views.contains(v) || !v.isRunning || !v.active) {
	    v.isRunning = false;
	    return;
	}

	if (--numActiveViews == 0) {
	    renderingAttributesStructure.updateThread.active = false;
	}
	VirtualUniverse univ = v.universe;

	if (--univ.activeViewCount == 0) {
		univ.transformStructure.updateThread.active = false;
		univ.geometryStructure.updateThread.active = false;
		univ.renderingEnvironmentStructure.updateThread.active = false;
		univ.soundStructure.updateThread.active = false;
	}

	v.renderBin.updateThread.active = false;
	v.soundScheduler.deactivate();
	v.isRunning = false;
	assignNewPrimaryView(univ);
	threadListsChanged = true;
    }

    // Call from user thread
    void addInputDeviceScheduler(InputDeviceScheduler ds) {
	synchronized (inputDeviceThreads) {
	    inputDeviceThreads.add(ds);
	    if (inputDeviceThreads.size() == 1) {
		timerThread.addInputDeviceSchedCond();	
	    }
	}
	postRequest(INPUTDEVICE_CHANGE, null);
    }

    // Call from user thread
    void removeInputDeviceScheduler(InputDeviceScheduler ds) {
	inputDeviceThreads.remove(ds);
	postRequest(INPUTDEVICE_CHANGE, null);
    }

    /**
     * Add an object to the mirror object list
     */
    void addMirrorObject(ObjectUpdate o) {
	mirrorObjects.add(o);
    }

    /**
     * This updates any mirror objects.  It is called when threads 
     * are done.
     */
    void updateMirrorObjects() {
	ObjectUpdate objs[] = (ObjectUpdate []) mirrorObjects.toArray(false);
	int sz = mirrorObjects.arraySize();

	for (int i = 0; i< sz; i++) {
	    objs[i].updateObject();
	} 
	mirrorObjects.clear();
    }


    /**
     * This fun little method does all the hard work of setting up the
     * work thread list.
     */
    private void updateWorkThreads() {
	
	stateWorkThreads.clear();
	renderWorkThreads.clear();
	requestRenderWorkThreads.clear();

	// First the global rendering attributes structure update
	if (numActiveViews > 0) {
	    addToStateThreads(renderingAttributesStructure.getUpdateThreadData());
	}

	// Next, each of the transform structure updates
	VirtualUniverse universes[] = (VirtualUniverse []) 
	                         activeUniverseList.toArray(false);
	VirtualUniverse univ;
	int i;
	int size = activeUniverseList.arraySize();

	for (i=size-1; i>=0; i--) {
	    addToStateThreads(universes[i].transformStructure.getUpdateThreadData());
	}
	lastTransformStructureThread = stateWorkThreads.size();

	// Next, the GeometryStructure, BehaviorStructure, 
	//       RenderingEnvironmentStructure, and SoundStructure
	for (i=size-1; i>=0; i--) {
	    univ = universes[i];
	    addToStateThreads(univ.geometryStructure.getUpdateThreadData());
	    addToStateThreads(univ.behaviorStructure.getUpdateThreadData());
	    addToStateThreads(univ.renderingEnvironmentStructure.getUpdateThreadData());
	    addToStateThreads(univ.soundStructure.getUpdateThreadData());
	}

	lastStructureUpdateThread = stateWorkThreads.size();

	// Next, the BehaviorSchedulers
	for (i=size-1; i>=0; i--) {
	    addToStateThreads(universes[i].behaviorScheduler.
			      getThreadData(null, null));
	}


	// Now InputDeviceScheduler

	InputDeviceScheduler ds[] = (InputDeviceScheduler []) 
	                              inputDeviceThreads.toArray(true);
	for (i=inputDeviceThreads.size()-1; i >=0; i--) {
	    J3dThreadData threadData = ds[i].getThreadData();
	    threadData.thread.active = true;
	    addToStateThreads(threadData);
	}

	// Now the RenderBins and SoundSchedulers
	View viewArr[] = (View []) views.toArray(false);
	J3dThreadData thread;

	for (i=views.size()-1; i>=0; i--) {
	    View v = viewArr[i];
	    if (v.active && v.isRunning) {
		addToStateThreads(v.renderBin.getUpdateThreadData());
		addToStateThreads(v.soundScheduler.getUpdateThreadData());
	        Canvas3D canvasList[][] = v.getCanvasList(false);
	        int longestScreenList = v.getLongestScreenList();
		Object args[] = null;
		// renderer render 
	        for (int j=0; j<longestScreenList; j++) {
	            for (int k=0; k < canvasList.length; k++) {
	                if (j < canvasList[k].length) {
	                    Canvas3D cv = canvasList[k][j];
                            // Issue 131: setup renderer unless manualRendering
			    if (cv.active && cv.isRunningStatus && !cv.manualRendering ) {
				if (cv.screen.renderer == null) {
				    continue;
				}
				thread = cv.screen.renderer.getThreadData(v, cv);
				renderWorkThreads.add(thread);
				args =	(Object []) thread.threadArgs;
				args[0] = RENDER;
				args[1] = cv;
				args[2] = v;
			    }
			}
		    }
		}
    
		// renderer swap
	        for (int j=0; j<canvasList.length; j++) {	           
		    for (int k=0; k < canvasList[j].length; k++) {
			Canvas3D cv = canvasList[j][k];
			// create swap thread only if there is at
			// least one active canvas
                        // Issue 131: only if not manualRendering
			if (cv.active && cv.isRunningStatus && !cv.manualRendering) {
			    if (cv.screen.renderer == null) {
				// Should not happen
				continue;
			    }
			    thread = cv.screen.renderer.getThreadData(v, null);
			    renderWorkThreads.add(thread);
			    args = (Object []) thread.threadArgs;
			    args[0] = SWAP;
			    args[1] = v;
			    args[2] = canvasList[j];
			    break;
			}
		    }
		}
	    }
	}

	thread = null;

	for (Enumeration e = Screen3D.deviceRendererMap.elements();
	     e.hasMoreElements(); ) {
	    Renderer rdr = (Renderer) e.nextElement();
	    thread = rdr.getThreadData(null, null);
	    requestRenderWorkThreads.add(thread);
	    thread.threadOpts = J3dThreadData.CONT_THREAD;
	    ((Object[]) thread.threadArgs)[0] = REQUESTRENDER;
	}

	if (thread != null) {
	    thread.threadOpts |= J3dThreadData.WAIT_ALL_THREADS;
	}

	threadListsChanged = false;

	//	 dumpWorkThreads();
    }


    void dumpWorkThreads() {
	System.err.println("-----------------------------");
	System.err.println("MasterControl/dumpWorkThreads");
	
	J3dThreadData threads[];
	int size = 0;
	
	for (int k=0; k<3; k++) {
	    switch (k) {
	    case 0:
		threads = (J3dThreadData []) stateWorkThreads.toArray(false);
		size = stateWorkThreads.arraySize();
		break;
	    case 1:
		threads = (J3dThreadData []) renderWorkThreads.toArray(false);
		size = renderWorkThreads.arraySize();
		break;
	    default:
		threads = (J3dThreadData []) requestRenderWorkThreads.toArray(false);
		size = requestRenderWorkThreads.arraySize();
		break;
	    }

	    for (int i=0; i<size; i++) {
		J3dThreadData thread = threads[i];
		System.err.println("Thread " + i + ": " + thread.thread);
		System.err.println("\tOps: " + thread.threadOpts);
		if (thread.threadArgs != null) {
		    Object[] args = (Object[]) thread.threadArgs;
		    System.err.print("\tArgs: ");
		    for (int j=0; j<args.length; j++) {
			System.err.print(args[j] + " ");
		    }
		}
		System.err.println("");
	    }
	}
	System.err.println("-----------------------------");
    }


    /**
     * A convienence wrapper function for various parts of the system
     * to force MC to run.
     */
    final void setWork() {
	runMonitor(SET_WORK, null, null, null, null);
    }

    final void setWorkForRequestRenderer() {
        runMonitor(SET_WORK_FOR_REQUEST_RENDERER, null, null, null, null);
    }

    /**
     * Call from GraphicsConfigTemplate to evaluate current
     * capabilities using Renderer thread to invoke native
     * graphics library functions. This avoid MT-safe problem
     * when using thread directly invoke graphics functions.
     */
    void sendRenderMessage(GraphicsConfiguration gc,
			   Object arg, Integer mtype) {
	Renderer rdr = createRenderer(gc);
	J3dMessage renderMessage = VirtualUniverse.mc.getMessage();
	renderMessage.threads = J3dThread.RENDER_THREAD;
	renderMessage.type = J3dMessage.RENDER_IMMEDIATE;
	renderMessage.universe = null;
	renderMessage.view = null;
	renderMessage.args[0] = null;
	renderMessage.args[1] = arg;
	renderMessage.args[2] = mtype;
	rdr.rendererStructure.addMessage(renderMessage);
	setWorkForRequestRenderer();
    }

    // Issue for Issue 175
    // Pass DestroyCtxAndOffScreenBuffer to the Renderer thread for execution.
    void sendDestroyCtxAndOffScreenBuffer(Canvas3D c) {
        // Assertion check. Look for comment in sendCreateOffScreenBuffer.
        GraphicsDevice gd = c.graphicsConfiguration.getDevice();
	J3dDebug.doAssert((Screen3D.deviceRendererMap.get(gd) != null),
			  "Screen3D.deviceRendererMap.get(gd) != null");
	synchronized (mcThreadLock) {   
            // Assert the master control thread is created.
            J3dDebug.doAssert((mcThread != null), "mcThread != null");
	    Renderer rdr = createRenderer(c.graphicsConfiguration);
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.RENDER_THREAD;
	    createMessage.type = J3dMessage.DESTROY_CTX_AND_OFFSCREENBUFFER;
	    createMessage.universe = null;
	    createMessage.view = null;
	    createMessage.args[0] = c;
            // Fix for issue 340: send display, drawable & ctx in msg
            createMessage.args[1] = new Long(c.screen.display);
            createMessage.args[2] = c.drawable;
            createMessage.args[3] = c.ctx;
	    rdr.rendererStructure.addMessage(createMessage);
	    synchronized (requestObjList) {
		setWorkForRequestRenderer();
		pendingRequest = true;
	    }            
        }
    }
    
    // Fix for Issue 18
    // Pass CreateOffScreenBuffer to the Renderer thread for execution.
    void sendCreateOffScreenBuffer(Canvas3D c) {
	// Assertion check that the renderer has already been created.
	// If it hasn't, this is very, very bad because it opens up
	// the possibility of an MT race condition since this method
	// can be called from the user's thread, possibly at the same
	// time as the MasterControl thread is trying to create a new
	// Renderer.  Fortunately, this should never happen since both
	// the GraphicsTemplate3D methods that return a valid Graphics
	// Configuration and the Canvas3D constructor will ultimately
	// cause a renderer to be created via sendRenderMessage().
	GraphicsDevice gd = c.graphicsConfiguration.getDevice();
	J3dDebug.doAssert((Screen3D.deviceRendererMap.get(gd) != null),
			  "Screen3D.deviceRendererMap.get(gd) != null");

	synchronized (mcThreadLock) {
	    // Create the master control thread if it isn't already created
	    if (mcThread == null) {
		//System.err.println("Calling createMasterControlThread()");
		createMasterControlThread();
	    }

	    // Fix for Issue 72 : call createRenderer rather than getting
	    // the renderer from the canvas.screen object
	    Renderer rdr = createRenderer(c.graphicsConfiguration);
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.RENDER_THREAD;
	    createMessage.type = J3dMessage.CREATE_OFFSCREENBUFFER;
	    createMessage.universe = null;
	    createMessage.view = null;
	    createMessage.args[0] = c;
	    rdr.rendererStructure.addMessage(createMessage);
	    synchronized (requestObjList) {
		setWorkForRequestRenderer();
		pendingRequest = true;
	    }
	}
    }


    /**
     * This is the MasterControl work method for Java 3D
     */
    void doWork() {
	runMonitor(CHECK_FOR_WORK, null, null, null, null);
        
	synchronized (timeLock) {
	    synchronized (requestObjList) {
		if (pendingRequest) {
		    handlePendingRequest();
		}
	    }
	}

	if (!running) {
	    return;
	}

	if (threadListsChanged) { // Check for new Threads
	    updateWorkThreads();
	}

	synchronized (timeLock) {
	    // This is neccesary to prevent updating 
	    // thread.lastUpdateTime from user thread
	    // in sendMessage() or sendRunMessage()
	    updateTimeValues();
	}

	//This is temporary until the view model is updated
	View v[] = (View []) views.toArray(false);
	for (int i=views.size()-1; i>=0; i--) {
	    if (v[i].active) {
		v[i].updateViewCache();
		// update OrientedShape3D
		if ((v[i].viewCache.vcDirtyMask != 0 && 
		     !v[i].renderBin.orientedRAs.isEmpty()) ||
		    (v[i].renderBin.cachedDirtyOrientedRAs != null && 
		     !v[i].renderBin.cachedDirtyOrientedRAs.isEmpty())) {
		    v[i].renderBin.updateOrientedRAs();
		}
	    }
	}

	runMonitor(RUN_THREADS, stateWorkThreads, renderWorkThreads, 
		   requestRenderWorkThreads, null);

	if (renderOnceList.size() > 0) {
	    clearRenderOnceList();
	}

	manageMemory();

    }

    private void handlePendingRequest() {
	
	Object objs[];
	Integer types[];
	int size;
	boolean rendererRun = false;

	objs = requestObjList.toArray(false);
	types = (Integer []) requestTypeList.toArray(false);	    
	size = requestObjList.size();

	for (int i=0; i < size; i++) {
	    // need to process request in order
	    Integer type = types[i];
	    Object o = objs[i];
           if (type == RESET_CANVAS) {
               Canvas3D cv = (Canvas3D) o;
		if ((cv.screen != null) && 
		    (cv.screen.renderer != null)) {
		    rendererCleanupArgs[1] = o;
		    rendererCleanupArgs[2] = RESETCANVAS_CLEANUP;
		    runMonitor(RUN_RENDERER_CLEANUP, null, null, null,
			       cv.screen.renderer);
		    rendererCleanupArgs[1] = null;
		} 
               cv.reset();
	       cv.view = null;
	       cv.computeViewCache();
           }
	   else if (type == ACTIVATE_VIEW) {
		viewActivate((View) o);
	   } 
	   else if (type == DEACTIVATE_VIEW) {
		viewDeactivate((View) o);
	    } else if (type == REEVALUATE_CANVAS) {
		evaluateAllCanvases();
	    } else if (type == INPUTDEVICE_CHANGE) {
		inputDeviceThreads.clearMirror();
		threadListsChanged = true;
	    } else if (type == START_VIEW) {
		startView((View) o);
	    } else if (type == STOP_VIEW) {
		View v = (View) o;
		// Collision takes 3 rounds to finish its request
		if (++v.stopViewCount > 4) {
		    v.stopViewCount = -1; // reset counter
		    stopView(v);	
		} else {
		    tempViewList.add(v);
		}
	    } else if (type == UNREGISTER_VIEW) {
		unregisterView((View) o);
	    } else if (type == PHYSICAL_ENV_CHANGE) {
		evaluatePhysicalEnv((View) o);
	    } else if (type == EMPTY_UNIVERSE) {
		// Issue 81: We need to process this message as long
		// as there are no views associated with this
		// universe. Previously, this message was ignored if
		// there were views associated with *any* universe,
		// which led to a memory / thread leak.
	        boolean foundView = false;
		VirtualUniverse univ = (VirtualUniverse) o;
		View v[] = (View []) views.toArray(false);
		for (int j = views.size() - 1; j >= 0; j--) {
		    if (v[j].universe == univ) {
			foundView = true;
			break;
		    }
		}
		if (!foundView) {
		    destroyUniverseThreads(univ);
		    threadListsChanged = true;
		}
	    } else if (type == START_RENDERER) {
		((Canvas3D) o).isRunningStatus = true;
		threadListsChanged = true;
	    } else if (type == STOP_RENDERER) {
		if (o instanceof Canvas3D) {
		    ((Canvas3D) o).isRunningStatus = false;
		} else {
		    ((Renderer) o).userStop = true;
		}
		threadListsChanged = true;
	    } else if (type == RENDER_ONCE) {
		View v = (View) o;
		// temporary start View for renderonce
		// it will stop afterwards
		startView(v);
		renderOnceList.add(v);
		sendRunMessage(v, J3dThread.UPDATE_RENDER);
		threadListsChanged = true;		
		rendererRun = true;
	    } else if (type == FREE_CONTEXT) {
		Canvas3D cv = (Canvas3D ) ((Object []) o)[0];
		if ((cv.screen != null) && 
		    (cv.screen.renderer != null)) {
		    rendererCleanupArgs[1] = o;
		    rendererCleanupArgs[2] = REMOVECTX_CLEANUP;
		    runMonitor(RUN_RENDERER_CLEANUP, null, null, null,
			       cv.screen.renderer);
		    rendererCleanupArgs[1] = null;
		} 
		rendererRun = true;
            } else if (type == FREE_DRAWING_SURFACE) {
                Pipeline.getPipeline().freeDrawingSurfaceNative(o);
            } else if (type == GETBESTCONFIG) {
		GraphicsConfiguration gc = ((GraphicsConfiguration [])
			  ((GraphicsConfigTemplate3D) o).testCfg)[0];
		sendRenderMessage(gc, o, type);
		rendererRun = true;
	    } else if (type == ISCONFIGSUPPORT) {
		GraphicsConfiguration  gc = (GraphicsConfiguration) 
			  ((GraphicsConfigTemplate3D) o).testCfg;
		sendRenderMessage(gc, o, type);
		rendererRun = true;
	    } else if ((type == SET_GRAPHICSCONFIG_FEATURES) ||
		       (type == SET_QUERYPROPERTIES)) {
		GraphicsConfiguration gc = (GraphicsConfiguration) 
		          ((Canvas3D) o).graphicsConfiguration; 
		sendRenderMessage(gc, o, type);
		rendererRun = true;
	    } else if (type == SET_VIEW) {
		Canvas3D cv = (Canvas3D) o;
		cv.view = cv.pendingView;
		cv.computeViewCache();
	    }
	}

	// Do it only after all universe/View is register
	for (int i=0; i < size; i++) {
	    Integer type = types[i];
	    if (type == FREE_MESSAGE) {
		if (objs[i] instanceof VirtualUniverse) {
		    VirtualUniverse u = (VirtualUniverse) objs[i];
		    if (!regUniverseList.contains(u)) {
			emptyMessageList(u.behaviorStructure, null);
			emptyMessageList(u.geometryStructure, null);			
			emptyMessageList(u.soundStructure, null);
			emptyMessageList(u.renderingEnvironmentStructure, null);
		    }
		} else if (objs[i] instanceof View) {
		    View v = (View) objs[i];
		    if (!views.contains(v)) {
			emptyMessageList(v.soundScheduler, v);
			emptyMessageList(v.renderBin, v);
			if (v.resetUnivCount == v.universeCount) {
			    v.reset();
			    v.universe = null;
			    if (running == false) {
				// MC is about to terminate

				/*
				// Don't free list cause there may
				// have some other thread returning ID
				// after it.
				FreeListManager.clearList(FreeListManager.DISPLAYLIST);
				FreeListManager.clearList(FreeListManager.TEXTURE2D);
				FreeListManager.clearList(FreeListManager.TEXTURE3D);
	    
				synchronized (textureIdLock) {
				    textureIdCount = 0;
				}
				*/
			    }
			}
		    }
		}

	    }

	}
	requestObjList.clear();
	requestTypeList.clear();

	size = tempViewList.size();
	if (size > 0) {
	    if (running) {
		for (int i=0; i < size; i++) {
		    requestTypeList.add(STOP_VIEW);
		    requestObjList.add(tempViewList.get(i));
		}
		setWork();
	    } else { // MC will shutdown
		for (int i=0; i < size; i++) {
		    View v = (View) tempViewList.get(i); 
		    v.stopViewCount = -1;
		    v.isRunning = false;
		}
	    }
	    tempViewList.clear();
	    pendingRequest = true;
	} else {
	    pendingRequest = rendererRun || (requestObjList.size() > 0);

	}
	
	size = freeMessageList.size();
	if (size > 0) {
	    for (int i=0; i < size; i++) {
		requestTypeList.add(FREE_MESSAGE);
		requestObjList.add(freeMessageList.get(i));
	    }
	    pendingRequest = true;
	    freeMessageList.clear();
	}
	if (!running && (renderOnceList.size() > 0)) {
	    clearRenderOnceList();
	}
	
	if (pendingRequest) {
	    setWork();
	}

	if (rendererRun || requestRenderWorkToDo) {
	    running = true;
	}

    }

    private void clearRenderOnceList() {
	for (int i=renderOnceList.size()-1; i>=0; i--) {
	    View v = (View) renderOnceList.get(i);
	    v.renderOnceFinish = true;
	    // stop after render once
	    stopView(v);
	}
	renderOnceList.clear();
	threadListsChanged = true;

    }

    synchronized void runMonitor(int action, 
				 UnorderList stateThreadList, 
				 UnorderList renderThreadList,
				 UnorderList requestRenderThreadList,
				 J3dThread nthread) {

        switch (action) {
	case RUN_THREADS:
	    int currentStateThread = 0;
	    int currentRenderThread = 0;
	    int currentRequestRenderThread = 0;
	    View view;
	    boolean done;
	    J3dThreadData thread;
	    J3dThreadData renderThreads[] = (J3dThreadData [])
		                             renderThreadList.toArray(false);
	    J3dThreadData stateThreads[] = (J3dThreadData []) 
		                            stateThreadList.toArray(false);
	    J3dThreadData requestRenderThreads[] = (J3dThreadData []) 
		                       requestRenderThreadList.toArray(false);
	    int renderThreadSize = renderThreadList.arraySize();
	    int stateThreadSize = stateThreadList.arraySize();
	    int requestRenderThreadSize = requestRenderThreadList.arraySize();

	    done = false;

	    //lock all the needed geometry and image component
	    View[] allView = (View []) views.toArray(false);
	    View currentV;
	    int i;

	    if (lockGeometry)
	    {
		for( i = views.arraySize()-1; i >= 0; i--) {
		    currentV = allView[i];
		    currentV.renderBin.lockGeometry();
		}
	    }

	    while (!done) {
		// First try a RenderThread
		while (!renderWaiting &&
		       currentRenderThread != renderThreadSize) {
		    thread = renderThreads[currentRenderThread++];
		    if (!thread.needsRun) {
			continue;
		    }
		    if ((thread.threadOpts & J3dThreadData.START_TIMER) != 0) {
		        view = (View)((Object[])thread.threadArgs)[2];
		        view.frameNumber++;
		        view.startTime = J3dClock.currentTimeMillis();
		    }


		    renderPending++;

		    if (cpuLimit == 1) {
			thread.thread.args = (Object[])thread.threadArgs;
			thread.thread.doWork(currentTime);
		    } else {
			threadPending++;
			thread.thread.runMonitor(J3dThread.RUN, 
						 currentTime, 
						 (Object[])thread.threadArgs);
		    }		    

		    if ((thread.threadOpts & J3dThreadData.STOP_TIMER) != 0) {
			view = (View)((Object[])thread.threadArgs)[3];
			timestampUpdateList.add(view);
		    }

		    if ((thread.threadOpts & J3dThreadData.LAST_STOP_TIMER) != 0) {
			// release lock on locked geometry and image component
			for( i = 0; i < views.arraySize(); i++) {
			    currentV = allView[i];
			    currentV.renderBin.releaseGeometry();
			}
		    }
		    
		    if ((cpuLimit != 1) && 
			(thread.threadOpts &
			 J3dThreadData.WAIT_ALL_THREADS) != 0) {

			renderWaiting = true;
		    }


		    if ((cpuLimit != 1) && (cpuLimit <= threadPending)) {
			state = WAITING_FOR_CPU;
			try {
			    wait();
			} catch (InterruptedException e) {
			    System.err.println(e);
			}
			state = RUNNING;
		    }

		}
		// Now try state threads
		while (!stateWaiting &&
		       currentStateThread != stateThreadSize) {
		    thread = stateThreads[currentStateThread++];
		    
		    if (!thread.needsRun) {
			continue;
		    }

		    statePending++;

		    if (cpuLimit == 1) {
			thread.thread.args = (Object[])thread.threadArgs;
			thread.thread.doWork(currentTime);
		    } else {
			threadPending++;
			thread.thread.runMonitor(J3dThread.RUN, 
						 currentTime, 
						 (Object[])thread.threadArgs);
		    }
		    if (cpuLimit != 1 && (thread.threadOpts & 
			 J3dThreadData.WAIT_ALL_THREADS) != 0) {
			stateWaiting = true;
		    }

		    if ((cpuLimit != 1) && (cpuLimit <= threadPending)) {
			// Fix bug 4686766 - always allow
			// renderer thread to continue if not finish 
			// geomLock can release for Behavior thread to 
			// continue.
			if (currentRenderThread == renderThreadSize) {
			    state = WAITING_FOR_CPU;
			    try {
				wait();
			    } catch (InterruptedException e) {
				System.err.println(e);
			    }
			    state = RUNNING;
			} else {
			    // Run renderer thread next time
			    break;
			}

		    }
		}

		// Now try requestRender threads
                if (!renderWaiting &&
                     (currentRenderThread == renderThreadSize)) {
                    currentRequestRenderThread = 0;
                    while (!renderWaiting &&
                           (currentRequestRenderThread !=
                                requestRenderThreadSize)) {

                        thread = 
			   requestRenderThreads[currentRequestRenderThread++];

                        renderPending++;

                        if (cpuLimit == 1) {
                            thread.thread.args = (Object[])thread.threadArgs;
                            thread.thread.doWork(currentTime);
                        } else {
                            threadPending++;
                            thread.thread.runMonitor(J3dThread.RUN, 
                                                currentTime,
                                                (Object[])thread.threadArgs);
                        }
                        if (cpuLimit != 1 && (thread.threadOpts &
                                J3dThreadData.WAIT_ALL_THREADS) != 0) {
                            renderWaiting = true;
                        }
                        if (cpuLimit != 1 && cpuLimit <= threadPending) {
                            state = WAITING_FOR_CPU;
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                System.err.println(e);
                            }
			    state = RUNNING;
                        }
                    }
                }

		if (cpuLimit != 1) {
		    if ((renderWaiting && 
			 (currentStateThread == stateThreadSize)) ||
		          (stateWaiting && 
		            currentRenderThread == renderThreadSize) ||
		    	    (renderWaiting && stateWaiting)) {
			if (!requestRenderWorkToDo) {
		            state = WAITING_FOR_THREADS;
		            try {
			        wait();
		            } catch (InterruptedException e) {
			        System.err.println(e);
		            }
			    state = RUNNING;
			}
			requestRenderWorkToDo = false;
		    }
		}

                if ((currentStateThread == stateThreadSize) &&
                    (currentRenderThread == renderThreadSize) &&
                    (currentRequestRenderThread == requestRenderThreadSize) &&
		    (threadPending == 0)) {
		    for (int k=timestampUpdateList.size()-1; k >=0;
			 k--) {
			View v = (View) timestampUpdateList.get(k);
			v.setFrameTimingValues();
			v.universe.behaviorStructure.incElapsedFrames();
		    }
		    timestampUpdateList.clear();
		    updateMirrorObjects();
		    done = true;
		}
	    }
	    break;

	case THREAD_DONE:
	    if (state != WAITING_FOR_RENDERER_CLEANUP) {

		threadPending--;
                assert threadPending >= 0 : ("threadPending = " + threadPending);
                if (nthread.type == J3dThread.RENDER_THREAD) {
		    View v = (View) nthread.args[3];
		    if (v != null) { // STOP_TIMER
			v.stopTime = J3dClock.currentTimeMillis();
		    }

		    if (--renderPending == 0) {
			renderWaiting = false;
		    }
                    assert renderPending >= 0 : ("renderPending = " + renderPending);
		} else {
		    if (--statePending == 0) {
			stateWaiting = false;
		    }
                    assert statePending >= 0 : ("statePending = " + statePending);
		}
		if (state == WAITING_FOR_CPU || state == WAITING_FOR_THREADS) {
		    notify();
		}
	    } else {
		notify();
		state = RUNNING;
	    }
	    break;

	case CHECK_FOR_WORK:
	    if (!workToDo) {
		state = SLEEPING;
                // NOTE: this could wakeup spuriously (see issue 279), but it
                // will not cause any problems.
		try {
		    wait();
		} catch (InterruptedException e) {
		    System.err.println(e);
		}
	        state = RUNNING;
	    }
	    workToDo = false;
	    break;

	case SET_WORK:
	    workToDo = true;
	    if (state == SLEEPING) {
		notify();
	    }
	    break;

        case SET_WORK_FOR_REQUEST_RENDERER:
	    requestRenderWorkToDo = true;
	    workToDo = true;
	    if (state == WAITING_FOR_CPU || state == WAITING_FOR_THREADS ||
			state == SLEEPING) {
		notify();
	    }
	    break;

	case RUN_RENDERER_CLEANUP:
	    nthread.runMonitor(J3dThread.RUN, currentTime,
			       rendererCleanupArgs);
	    state = WAITING_FOR_RENDERER_CLEANUP;
            // Issue 279 - loop until state is set to running
            while (state != RUNNING) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
            }
	    break;

        default:
            // Should never get here
            assert false : "missing case in switch statement";
        }
    }

    // Static initializer
    static {
	// create ThreadGroup
	java.security.AccessController.doPrivileged(
  	    new java.security.PrivilegedAction() {
                public Object run() {
		    ThreadGroup parent;
		    Thread thread = Thread.currentThread();
		    threadPriority = thread.getPriority();
		    rootThreadGroup = thread.getThreadGroup();
		    while ((parent = rootThreadGroup.getParent()) != null) {
			rootThreadGroup = parent;
		    }
		    rootThreadGroup = new ThreadGroup(rootThreadGroup,
						      "Java3D");
		    // use the default maximum group priority
		    return null;
		}
	});
    }


    static String mtype[] = {
	"-INSERT_NODES                   ",
	"-REMOVE_NODES                   ",
	"-RUN                            ",
	"-TRANSFORM_CHANGED              ",
	"-UPDATE_VIEW                    ",
	"-STOP_THREAD                    ",
	"-COLORINGATTRIBUTES_CHANGED     ",
	"-LINEATTRIBUTES_CHANGED         ",
	"-POINTATTRIBUTES_CHANGED        ",
	"-POLYGONATTRIBUTES_CHANGED      ",

	"-RENDERINGATTRIBUTES_CHANGED    ",
	"-TEXTUREATTRIBUTES_CHANGED      ",
	"-TRANSPARENCYATTRIBUTES_CHANGED ",
	"-MATERIAL_CHANGED               ",
	"-TEXCOORDGENERATION_CHANGED     ",
	"-TEXTURE_CHANGED                ",
	"-MORPH_CHANGED                  ",
	"-GEOMETRY_CHANGED               ",
	"-APPEARANCE_CHANGED             ",
	"-LIGHT_CHANGED                  ",

	"-BACKGROUND_CHANGED             ",
	"-CLIP_CHANGED                   ",
	"-FOG_CHANGED                    ",
	"-BOUNDINGLEAF_CHANGED           ",
	"-SHAPE3D_CHANGED                ",
	"-TEXT3D_TRANSFORM_CHANGED       ",
	"-TEXT3D_DATA_CHANGED            ",
	"-SWITCH_CHANGED                 ",
	"-COND_MET                       ",
	"-BEHAVIOR_ENABLE                ",

	"-BEHAVIOR_DISABLE               ",
	"-INSERT_RENDERATOMS             ",
	"-ORDERED_GROUP_INSERTED         ",
	"-ORDERED_GROUP_REMOVED          ",
	"-COLLISION_BOUND_CHANGED        ",
	"-REGION_BOUND_CHANGED           ",
	"-MODELCLIP_CHANGED              ",
	"-BOUNDS_AUTO_COMPUTE_CHANGED    ",

	"-SOUND_ATTRIB_CHANGED           ",
	"-AURALATTRIBUTES_CHANGED        ",
	"-SOUNDSCAPE_CHANGED             ",
	"-ALTERNATEAPPEARANCE_CHANGED    ",
	"-RENDER_OFFSCREEN               ",
	"-RENDER_RETAINED                ",
	"-RENDER_IMMEDIATE               ",
	"-SOUND_STATE_CHANGED            ",
	"-ORIENTEDSHAPE3D_CHANGED        ",
	"-TEXTURE_UNIT_STATE_CHANGED     ",
	"-UPDATE_VIEWPLATFORM            ",
	"-BEHAVIOR_ACTIVATE              ",
	"-GEOMETRYARRAY_CHANGED          ",
	"-MEDIA_CONTAINER_CHANGED        ",
    	"-RESIZE_CANVAS                  ",
    	"-TOGGLE_CANVAS                  ",
    	"-IMAGE_COMPONENT_CHANGED        ",
    	"-SCHEDULING_INTERVAL_CHANGED    ",
    	"-VIEWSPECIFICGROUP_CHANGED      ",
    	"-VIEWSPECIFICGROUP_INIT         ",
    	"-VIEWSPECIFICGROUP_CLEAR        ",
	"-ORDERED_GROUP_TABLE_CHANGED"};


    static void dumpThreads(int threads) {
	//dump Threads type
        if ((threads & J3dThread.BEHAVIOR_SCHEDULER) != 0)
            System.out.println("  BEHAVIOR_SCHEDULER");
        if ((threads & J3dThread.SOUND_SCHEDULER) != 0)
            System.out.println("  SOUND_SCHEDULER");
        if ((threads & J3dThread.INPUT_DEVICE_SCHEDULER) != 0)
            System.out.println("  INPUT_DEVICE_SCHEDULER");

        if ((threads & J3dThread.RENDER_THREAD) != 0)
            System.out.println("  RENDER_THREAD");

        if ((threads & J3dThread.UPDATE_GEOMETRY) != 0)
            System.out.println("  UPDATE_GEOMETRY");
        if ((threads & J3dThread.UPDATE_RENDER) != 0)
            System.out.println("  UPDATE_RENDER");
        if ((threads & J3dThread.UPDATE_BEHAVIOR) != 0)
            System.out.println("  UPDATE_BEHAVIOR");
        if ((threads & J3dThread.UPDATE_SOUND) != 0)
            System.out.println("  UPDATE_SOUND");
        if ((threads & J3dThread.UPDATE_RENDERING_ATTRIBUTES) != 0)
            System.out.println("  UPDATE_RENDERING_ATTRIBUTES");
        if ((threads & J3dThread.UPDATE_RENDERING_ENVIRONMENT) != 0)
            System.out.println("  UPDATE_RENDERING_ENVIRONMENT");
        if ((threads & J3dThread.UPDATE_TRANSFORM) != 0)
            System.out.println("  UPDATE_TRANSFORM");
    }

    static void dumpmsg(J3dMessage m) {
	//dump message type
        System.out.println(mtype[m.type]);

	dumpThreads(m.threads);
    }


    int frameCount = 0;
    private int frameCountCutoff = 100;

    private void manageMemory() {
	if (++frameCount > frameCountCutoff) {
	    FreeListManager.manageLists();
	    frameCount = 0;
	}
    }

    /**
     * Yields the current thread, by sleeping for a small amount of
     * time.  Unlike <code>Thread.yield()</code>, this method
     * guarantees that the current thread will yield to another thread
     * waiting to run.  It also ensures that the other threads will
     * run for at least a small amount of time before the current
     * thread runs again.
     */
    static final void threadYield() {
	// Note that we can't just use Thread.yield(), since it
	// doesn't guarantee that it will actually yield the thread
	// (and, in fact, it appears to be a no-op under Windows). So
	// we will sleep for 1 msec instead. Since most threads use
	// wait/notify, and only use this when they are waiting for
	// another thread to finish something, this shouldn't be a
	// performance concern.

	//Thread.yield();
	try {
	    Thread.sleep(1);
	}
	catch (InterruptedException e) {
	    // Do nothing, since we really don't care how long (or
	    // even whether) we sleep
	}
    }

    // Return the number of available processors
    private int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }
}
