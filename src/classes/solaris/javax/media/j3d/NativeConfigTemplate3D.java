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

import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsConfigTemplate;
import java.awt.Rectangle;
import sun.awt.X11GraphicsDevice;
import sun.awt.X11GraphicsConfig;

class NativeConfigTemplate3D {
    private final static boolean debug = false;

    NativeConfigTemplate3D() {
	VirtualUniverse.createMC();
    }

    //  These definitions should match those in win32 NativeConfigTemplate3D.java
    final static int RED_SIZE		= 0;
    final static int GREEN_SIZE		= 1;
    final static int BLUE_SIZE		= 2;
    final static int ALPHA_SIZE		= 3;
    final static int ACCUM_BUFFER	= 4;
    final static int DEPTH_SIZE		= 5;
    final static int DOUBLEBUFFER	= 6;
    final static int STEREO		= 7;
    final static int ANTIALIASING	= 8;
    final static int NUM_ITEMS		= 9;

    // Native method to get an OpenGL visual id and a pointer to the
    // GLXFBConfig structure list itself.
    native int chooseOglVisual(long display, int screen,
			       int[] attrList, long[] fbConfig);

    // Native method to free an GLXFBConfig struct.  This is static since it
    // may need to be called to clean up the Canvas3D fbConfigTable after the
    // NativeConfigTemplate3D has been disposed of.
    static native void freeFbConfig(long fbConfig);

    // Native methods to return whether a particular attribute is available
    native boolean isStereoAvailable(long display, int screen, int vid);
    native boolean isDoubleBufferAvailable(long display, int screen, int vid);
    native boolean isSceneAntialiasingAccumAvailable(long display, int screen, int vid);
    native boolean isSceneAntialiasingMultiSamplesAvailable(long display, int screen, int vid);


    /*
     *  Chooses the best FBConfig for Java 3D apps.
     */
    GraphicsConfiguration
      getBestConfiguration(GraphicsConfigTemplate3D template,
                           GraphicsConfiguration[] gc) {

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc[0]).getDevice();

	if (!NativeScreenInfo.isGLX13()) {
	    return null;
	}

	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();

	if (debug) {
	    System.out.println("  NativeConfigTemplate3D: using device " + gd);
	    System.out.println("    display " + display + " screen " + screen);
	    System.out.println("    configuration count: " + gc.length);
	    for (int i = 0 ; i < gc.length ; i++) {
		System.out.println("      visual id at index " + i + ": " + 
				   ((X11GraphicsConfig)gc[i]).getVisual());
	    }
	}

	Rectangle bounds = gc[0].getBounds();
	if ((bounds.x != 0 || bounds.y != 0) &&
	    (! VirtualUniverse.mc.xineramaDisabled)) {
	    // Xinerama is being used.  The screen needs to be set to 0 since
	    // glxChooseFBConfig will not return a valid visual otherwise.
	    screen = 0;
	    if (debug) {
		System.out.println("  Non-primary Xinerama screen:");
		System.out.println("    bounds = " + bounds);
		System.out.println("    using screen 0 visual");
	    }
	}

        int[] attrList;   // holds the list of attributes to be translated
                          // for glxChooseFBConfig call

        attrList = new int[NUM_ITEMS];

        // assign template values to array
        attrList[RED_SIZE] = template.getRedSize();
        attrList[GREEN_SIZE] = template.getGreenSize();
        attrList[BLUE_SIZE] = template.getBlueSize();

        attrList[DEPTH_SIZE] = template.getDepthSize();
        attrList[DOUBLEBUFFER] = template.getDoubleBuffer();
        attrList[STEREO] = template.getStereo();
        attrList[ANTIALIASING] = template.getSceneAntialiasing();

	long[] fbConfig = new long[1];
        int visID = chooseOglVisual(display, screen, attrList, fbConfig);
	if (debug) {
	    System.out.println("  chooseOglVisual() returns " + visID);
	    System.out.println("  pointer to GLXFBConfig is " + fbConfig[0]);
	    System.out.println();
	}

        if (visID == 0 || fbConfig[0] == 0) {
	    return null;  // no valid visual was found
	}	

        // search list of graphics configurations for config
        // with matching visualId
        GraphicsConfiguration gc1 = null;
        for (int i = 0; i < gc.length; i++)
            if (((X11GraphicsConfig)gc[i]).getVisual() == visID) {
                gc1 = gc[i];
                break;
            }
	
	// To support disabling Solaris OpenGL Xinerama mode, we need to cache
	// the pointer to the actual GLXFBConfig that glXChooseFBConfig()
	// returns, since this is not cached with X11GraphicsConfig and there
	// are no public constructors to allow us to extend it.	
	synchronized (Canvas3D.fbConfigTable) {
	    if (Canvas3D.fbConfigTable.get(gc1) == null)
		Canvas3D.fbConfigTable.put(gc1, new Long(fbConfig[0]));
	    else
		freeFbConfig(fbConfig[0]);
	}

        return gc1;
    }

    /*
     * Determine if a given GraphicsConfiguration object can be used
     * by Java 3D.
     */
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc) {

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	if (!NativeScreenInfo.isGLX13()) {
	    return false;
	}

	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();

        int[] attrList;   // holds the list of attributes to be tramslated
                          // for glxChooseVisual call

        attrList = new int[NUM_ITEMS];

        // assign template values to array
        attrList[RED_SIZE] = template.getRedSize();
        attrList[GREEN_SIZE] = template.getGreenSize();
        attrList[BLUE_SIZE] = template.getBlueSize();

        attrList[DEPTH_SIZE] = template.getDepthSize();
        attrList[DOUBLEBUFFER] = template.getDoubleBuffer();
        attrList[STEREO] = template.getStereo();
        attrList[ANTIALIASING] = template.getSceneAntialiasing();
	
	long[] fbConfig = new long[1];
        int visID = chooseOglVisual(display, screen, attrList, fbConfig);
	
        if (visID == 0 || fbConfig[0] == 0)
	    return false;  // no valid visual was found
	else
	    return true;	
    }


    // Return whether stereo is available.
    boolean hasStereo(GraphicsConfiguration gc) {
        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isStereoAvailable(display, screen, vid);
    }

    // Return whether a double buffer is available.
    boolean hasDoubleBuffer(GraphicsConfiguration gc) {
        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isDoubleBufferAvailable(display, screen, vid);
    }

    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingAccum(GraphicsConfiguration gc) {
        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isSceneAntialiasingAccumAvailable(display, screen, vid);
    }


    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingMultiSamples(GraphicsConfiguration gc) {
        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	long display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isSceneAntialiasingMultiSamplesAvailable(display, screen, vid);
    }
    
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
