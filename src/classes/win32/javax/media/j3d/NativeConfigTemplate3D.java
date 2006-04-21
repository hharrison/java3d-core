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

package javax.media.j3d;

import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsConfig;
import java.awt.GraphicsConfigTemplate;

class NativeConfigTemplate3D {
    private final static boolean debug = false;

    NativeConfigTemplate3D() {
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
    final static int STENCIL_SIZE       = 9;
    final static int NUM_ITEMS		= 10;
    
    /**
     * selects the proper visual
     */
    native int
	choosePixelFormat(long ctx, int screen, int[] attrList, long[] pFormatInfo);

    // Native method to free an PixelFormatInfo struct.  This is static since it
    // may need to be called to clean up the Canvas3D graphicsConfigTable after the
    // NativeConfigTemplate3D has been disposed of.
    static native void freePixelFormatInfo(long pFormatInfo);

    // Native methods to return whether a particular attribute is available
    native boolean isStereoAvailable(long pFormatInfo, boolean offScreen);
    native boolean isDoubleBufferAvailable(long pFormatInfo, boolean offScreen);
    native boolean isSceneAntialiasingAccumAvailable(long pFormatInfo, boolean offScreen);
    native boolean isSceneAntialiasingMultisampleAvailable(long pFormatInfo, boolean offScreen, int screen);
    native int getStencilSize(long pFormatInfo, boolean offScreen);

    /**
     *  Chooses the best PixelFormat for Java 3D apps.
     */
    GraphicsConfiguration
      getBestConfiguration(GraphicsConfigTemplate3D template,
                           GraphicsConfiguration[] gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc[0]).getDevice();

	/*  Not ready to enforce ARB extension in J3D1.3.2, but will likely to 
	    do so in J3D 1.4.
	System.out.println("getBestConfiguration : Checking WGL ARB support\n");
	
	if (!NativeScreenInfo.isWglARB()) {
	    Thread.dumpStack();
	    System.out.println("getBestConfiguration : WGL ARB support fail\n");
	    return null;
	}
	*/

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
    	attrList[STENCIL_SIZE] = template.getStencilSize();
	// System.out.println("NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);
	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int screen = nativeScreenInfo.getScreen();	

	long[] pFormatInfo = new long[1];

	/* Deliberately set this to -1. pFormatInfo is not use in 
	   D3D, so this value will be unchange in the case of D3D.
	   In the case of OGL, the return value should be 0 or a 
	   positive valid address.
	*/
	pFormatInfo[0] = -1;

	int pixelFormat = choosePixelFormat(0, screen, attrList, pFormatInfo);
	if (debug) {
	    System.out.println("  choosePixelFormat() returns " + pixelFormat);
	    System.out.println("  pFormatInfo is " + pFormatInfo[0]);
	    System.out.println();
	}

	if (pixelFormat  < 0) {
	    // current mode don't support the minimum config
	    return null;
	}	    

	// Fix to issue 104 -- 
	// Pass in 0 for pixel format to the AWT. 
	// ATI driver will lockup pixelFormat, if it is passed to AWT.
        GraphicsConfiguration gc1 = Win32GraphicsConfig.getConfig(gd, 0);

	// We need to cache the GraphicsTemplate3D and the private
        // pixel format info.
	synchronized (Canvas3D.graphicsConfigTable) {
	    if (Canvas3D.graphicsConfigTable.get(gc1) == null) {
                GraphicsConfigInfo gcInfo = new GraphicsConfigInfo(template);
                gcInfo.setPrivateData(new Long(pFormatInfo[0]));
		Canvas3D.graphicsConfigTable.put(gc1, gcInfo);
            } else {
		freePixelFormatInfo(pFormatInfo[0]);
            }
	}

	return gc1;
    }

    /**
     * Determine if a given GraphicsConfiguration object can be used
     * by Java 3D.
     */
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig) gc).getDevice();

	/*  Not ready to enforce ARB extension in J3D1.3.2, but will likely to 
	    do so in J3D 1.4.
	System.out.println("isGraphicsConfigSupported : Checking WGL ARB support\n");

	if (!NativeScreenInfo.isWglARB()) {
	    Thread.dumpStack();
	    System.out.println("isGraphicsConfigSupported : WGL ARB support fail\n");
	    return false;
	}
	*/

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
    	attrList[STENCIL_SIZE] = template.getStencilSize();
	// System.out.println("NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);

	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int screen = nativeScreenInfo.getScreen();	

	long[] pFormatInfo = new long[1];

	int pixelFormat = choosePixelFormat(0, screen, attrList, pFormatInfo);
	if (debug) {
	    System.out.println("  choosePixelFormat() returns " + pixelFormat);
	    System.out.println("  pFormatInfo is " + pFormatInfo[0]);
	    System.out.println();
	}

	if (pixelFormat < 0) {
	    // current mode don't support the minimum config
	    return false;
	} else 
            return true;
    }


    // Return whether stereo is available.
    boolean hasStereo(Canvas3D c3d) {
	return isStereoAvailable(c3d.fbConfig, c3d.offScreen);
    }

    // Return the stencil of this canvas.
    int getStencilSize(Canvas3D c3d) {
        return getStencilSize(c3d.fbConfig, c3d.offScreen);
    }
    
    // Return whether a double buffer is available.
    boolean hasDoubleBuffer(Canvas3D c3d) {
	return isDoubleBufferAvailable(c3d.fbConfig, c3d.offScreen);
    }

    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingAccum(Canvas3D c3d) {
	return isSceneAntialiasingAccumAvailable(c3d.fbConfig, c3d.offScreen);
    }
    
    // Return whether scene antialiasing is available.
    boolean hasSceneAntialiasingMultisample(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        Win32GraphicsDevice gd =
	    (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
 	NativeScreenInfo nativeScreenInfo = new NativeScreenInfo(gd);
	int screen = nativeScreenInfo.getScreen();
	/* Fix to issue 77 */ 
	return isSceneAntialiasingMultisampleAvailable(c3d.fbConfig, c3d.offScreen, screen);
    }
       
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
