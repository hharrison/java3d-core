/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.GraphicsDevice;
import sun.awt.X11GraphicsDevice;

/**
 * Native screen info class. A singleton instance of this class is created by
 * a factory method in the base class using reflection.
 */
class X11NativeScreenInfo extends NativeScreenInfo {
    private static long display = 0;
    private static boolean glxChecked = false;
    private static boolean isGLX13;

    private static native long openDisplay();
    private static native boolean queryGLX13(long display);

    X11NativeScreenInfo() {
    }

    // Fix for issue 20.
    // This method will return true if glx version is 1.3 or higher, 
    // else return false.
    static synchronized boolean isGLX13() {
	if (!glxChecked) {
	    // Open a new static display connection if one is not already opened.
	    getStaticDisplay();

            // Query for glx1.3 support.
	    isGLX13 = queryGLX13(getStaticDisplay());
	    glxChecked = true;
	}

	return isGLX13;
    }

    private static synchronized long getStaticDisplay() {
	if (display == 0) {
	    display = openDisplay();
	}
	return display;
    }

    @Override
    long getDisplay() {
	// Open a new static display connection if one is not already opened
        return getStaticDisplay();
    }

    @Override
    int getScreen(GraphicsDevice graphicsDevice) {
	// Get the screen number
	return ((X11GraphicsDevice)graphicsDevice).getScreen();
    }

    // Ensure that the native libraries are loaded
    static {
        VirtualUniverse.loadLibraries();
    }
}
