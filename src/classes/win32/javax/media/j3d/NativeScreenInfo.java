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
import sun.awt.Win32GraphicsDevice;

class NativeScreenInfo {
    private int display = 0;
    private int screen = 0;

    NativeScreenInfo(GraphicsDevice graphicsDevice) {
	// Get the screen number
	screen = ((sun.awt.Win32GraphicsDevice)graphicsDevice).getScreen();
	display = screen;
    }

    int getDisplay() {
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
