/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package javax.media.j3d;

import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import sun.awt.Win32GraphicsDevice;
import sun.awt.Win32GraphicsConfig;
import java.awt.GraphicsConfigTemplate;

/**
 * Native config template class. A singleton instance of this class is
 * created by a factory method in the base class using reflection.
 */
class Win32NativeConfigTemplate3D extends NativeConfigTemplate3D {
    private final static boolean debug = false;

    Win32NativeConfigTemplate3D() {
    }

    /**
     * selects the proper visual
     */
    native int
	choosePixelFormat(long ctx, int screen, int[] attrList, long[] pFormatInfo);

    // Native method to free an PixelFormatInfo struct.  This is static since it
    // may need to be called to clean up the Canvas3D graphicsConfigTable after the
    // Win32NativeConfigTemplate3D has been disposed of.
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
    @Override
    GraphicsConfiguration
      getBestConfiguration(GraphicsConfigTemplate3D template,
                           GraphicsConfiguration[] gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig)gc[0]).getDevice();

	/*  Not ready to enforce ARB extension in J3D1.3.2, but will likely to 
	    do so in J3D 1.4.
	System.out.println("getBestConfiguration : Checking WGL ARB support\n");
	
	if (!Win32NativeScreenInfo.isWglARB()) {
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
	// System.out.println("Win32NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);

        int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);	

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
    @Override
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc) {

        Win32GraphicsDevice gd =
            (Win32GraphicsDevice)((Win32GraphicsConfig) gc).getDevice();

	/*  Not ready to enforce ARB extension in J3D1.3.2, but will likely to 
	    do so in J3D 1.4.
	System.out.println("isGraphicsConfigSupported : Checking WGL ARB support\n");

	if (!Win32NativeScreenInfo.isWglARB()) {
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
	// System.out.println("Win32NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);

	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);	

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
    @Override
    boolean hasStereo(Canvas3D c3d) {
	return isStereoAvailable(c3d.fbConfig, c3d.offScreen);
    }

    // Return the stencil of this canvas.
    @Override
    int getStencilSize(Canvas3D c3d) {
        return getStencilSize(c3d.fbConfig, c3d.offScreen);
    }
    
    // Return whether a double buffer is available.
    @Override
    boolean hasDoubleBuffer(Canvas3D c3d) {
	return isDoubleBufferAvailable(c3d.fbConfig, c3d.offScreen);
    }

    // Return whether scene antialiasing is available.
    @Override
    boolean hasSceneAntialiasingAccum(Canvas3D c3d) {
	return isSceneAntialiasingAccumAvailable(c3d.fbConfig, c3d.offScreen);
    }
    
    // Return whether scene antialiasing is available.
    @Override
    boolean hasSceneAntialiasingMultisample(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        Win32GraphicsDevice gd =
	    (Win32GraphicsDevice)((Win32GraphicsConfig)gc).getDevice();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	/* Fix to issue 77 */ 
	return isSceneAntialiasingMultisampleAvailable(c3d.fbConfig, c3d.offScreen, screen);
    }
       
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
