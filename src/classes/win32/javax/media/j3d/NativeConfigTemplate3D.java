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
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsConfig;
import java.awt.GraphicsConfigTemplate;

class NativeConfigTemplate3D {

    NativeConfigTemplate3D() {
	VirtualUniverse.createMC();
    }

    // This definition should match those in solaris NativeConfigTemplate3D.java
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
    
    /**
     * selects the proper visual
     */
    native int
      choosePixelFormat(long ctx, int screen, int[] attrList);

    // Native methods to return whether a particular attribute is available
    native boolean isStereoAvailable(long ctx, long display, int screen, int pixelFormat);
    native boolean isDoubleBufferAvailable(long ctx, long display, int screen, int pixelFormat);
    native boolean isSceneAntialiasingAccumAvailable(long ctx, long display, int screen, int pixelFormat);
    native boolean isSceneAntialiasingMultiSamplesAvailable(long ctx, long display, int screen, int pixelFormat);

    /**
     *  Chooses the best PixelFormat for Java 3D apps.
     */
    GraphicsConfiguration
      getBestConfiguration(GraphicsConfigTemplate3D template,
                           GraphicsConfiguration[] gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc[0]).getDevice();

	// holds the list of attributes to be tramslated
	//	    for glxChooseVisual call
	int attrList[] = new int[NUM_ITEMS];
	// assign template values to array
	attrList[RED_SIZE] = template.getRedSize();
	attrList[GREEN_SIZE] = template.getGreenSize();
	attrList[BLUE_SIZE] = template.getBlueSize();
	
	attrList[DEPTH_SIZE] = template.getDepthSize();
	attrList[DOUBLEBUFFER] = template.getDoubleBuffer();
	attrList[STEREO] = template.getStereo();
	attrList[ANTIALIASING] = template.getSceneAntialiasing();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int screen = nativeScreenInfo.getScreen();	
	int pixelFormat = choosePixelFormat(0, screen, attrList);
	if (pixelFormat  == -1) {
	    // current mode don't support the minimum config
	    return null;
	}	    
	return new J3dGraphicsConfig(gd, pixelFormat);
    }

    /**
     * Determine if a given GraphicsConfiguration object can be used
     * by Java 3D.
     */
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig) gc).getDevice();

	// holds the list of attributes to be tramslated
	//	    for glxChooseVisual call
	int attrList[] = new int[NUM_ITEMS];
	// assign template values to array
	attrList[RED_SIZE] = template.getRedSize();
	attrList[GREEN_SIZE] = template.getGreenSize();
	attrList[BLUE_SIZE] = template.getBlueSize();
	
	attrList[DEPTH_SIZE] = template.getDepthSize();
	attrList[DOUBLEBUFFER] = template.getDoubleBuffer();
	attrList[STEREO] = template.getStereo();
        attrList[ANTIALIASING] = template.getSceneAntialiasing();

	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int screen = nativeScreenInfo.getScreen();	

	int pixelFormat = choosePixelFormat(0, screen, attrList);

	if (pixelFormat  == -1) {
	    // current mode don't support the minimum config
	    return false;
	} else return true;
    }


    // Return whether stereo is available.
    boolean hasStereo(GraphicsConfiguration gc) {
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);

	int display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	// Temporary until Win32 GraphicsConfig stuff complete
	int pixelFormat = ((J3dGraphicsConfig) gc).getPixelFormat();

	return isStereoAvailable(0, display, screen, pixelFormat);
    }

    // Return whether a double buffer is available.
    boolean hasDoubleBuffer(GraphicsConfiguration gc) {
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	// Temporary until Win32 GraphicsConfig stuff complete
	int pixelFormat = ((J3dGraphicsConfig) gc).getPixelFormat();

	return isDoubleBufferAvailable(0, display, screen, pixelFormat);
    }

    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingAccum(GraphicsConfiguration gc) {
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	// Temporary until Win32 GraphicsConfig stuff complete
	int pixelFormat = ((J3dGraphicsConfig) gc).getPixelFormat();

	return isSceneAntialiasingAccumAvailable(0, display, screen, pixelFormat);
    }
    
    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingMultiSamples(GraphicsConfiguration gc) {
        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int display = nativeScreenInfo.getDisplay();
	int screen = nativeScreenInfo.getScreen();
	// Temporary until Win32 GraphicsConfig stuff complete
	int pixelFormat = ((J3dGraphicsConfig) gc).getPixelFormat();

	return isSceneAntialiasingMultiSamplesAvailable(0, display, screen, pixelFormat);
    }
       
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
