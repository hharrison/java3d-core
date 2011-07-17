/*
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
 */

package javax.media.j3d;

import java.awt.GraphicsDevice;
import sun.awt.Win32GraphicsDevice;

/**
 * Native screen info class. A singleton instance of this class is created by
 * a factory method in the base class using reflection.
 */
class Win32NativeScreenInfo extends NativeScreenInfo {
    private static final long display = 0; // unused for Win32

    private static boolean wglARBChecked = false;
    private static boolean isWglARB;

    private static native boolean queryWglARB();
    
    Win32NativeScreenInfo() {
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

    @Override
    long getDisplay() {
	return display;
    }

    @Override
    int getScreen(GraphicsDevice graphicsDevice) {
	return ((Win32GraphicsDevice)graphicsDevice).getScreen();
    }

    // Ensure that the native libraries are loaded
    static {
        VirtualUniverse.loadLibraries();
    }
}
