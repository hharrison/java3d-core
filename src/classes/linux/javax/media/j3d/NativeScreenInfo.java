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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

package javax.media.j3d;

import java.awt.GraphicsDevice;
import sun.awt.X11GraphicsDevice;

class NativeScreenInfo {
    private int screen;
    private static long display = 0;

    private native static long openDisplay();
    private native static int getDefaultScreen(long display);

    synchronized static long getStaticDisplay() {
	if (display == 0) {
	    display = openDisplay();
	}
	return display;
    }

    NativeScreenInfo(GraphicsDevice graphicsDevice) {
	VirtualUniverse.createMC();
	// Open a new static display connection if one is not already opened
	getStaticDisplay();

	// Get the screen number
	screen = ((X11GraphicsDevice)graphicsDevice).getScreen();
    }

    long getDisplay() {
	return display;
    }

    int getScreen() {
	return screen;
    }


    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
