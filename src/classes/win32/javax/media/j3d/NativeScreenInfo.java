/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.awt.GraphicsDevice;
import sun.awt.Win32GraphicsDevice;

class NativeScreenInfo {
    private static final long display = 0; // unused for Win32

    private static boolean wglARBChecked = false;
    private static boolean isWglARB;

    private static native boolean queryWglARB();
    
    private NativeScreenInfo() {
        throw new AssertionError("constructor should never be called");
    }

    // This method will return true if wglGetExtensionsStringARB is supported, 
    // else return false
    static synchronized boolean isWglARB() {

	if (!wglARBChecked) {
	    // Query for wglGetExtensionsStringARB support.
	    isWglARB = queryWglARB();
	    wglARBChecked = true;
	}
	return isWglARB;
    }

    static long getDisplay() {
	return display;
    }

    static int getScreen(GraphicsDevice graphicsDevice) {
	return ((Win32GraphicsDevice)graphicsDevice).getScreen();
    }

    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
