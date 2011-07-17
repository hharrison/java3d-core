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
import java.awt.GraphicsConfigTemplate;
import java.awt.Rectangle;
import sun.awt.X11GraphicsDevice;
import sun.awt.X11GraphicsConfig;

/**
 * Native config template class. A singleton instance of this class is
 * created by a factory method in the base class using reflection.
 */
class X11NativeConfigTemplate3D extends NativeConfigTemplate3D {
    private final static boolean debug = false;

    X11NativeConfigTemplate3D() {
    }

    // Native method to get an OpenGL visual id and a pointer to the
    // GLXFBConfig structure list itself.
    native int chooseOglVisual(long display, int screen,
			       int[] attrList, long[] fbConfig);

    // Native method to free an GLXFBConfig struct.  This is static since it
    // may need to be called to clean up the Canvas3D graphicsConfigTable
    // after the X11NativeConfigTemplate3D has been disposed of.
    static native void freeFBConfig(long fbConfig);

    // Native methods to return whether a particular attribute is available
    native boolean isStereoAvailable(long display, int screen, int vid);
    native boolean isDoubleBufferAvailable(long display, int screen, int vid);
    native boolean isSceneAntialiasingAccumAvailable(long display, int screen, int vid);
    native boolean isSceneAntialiasingMultisampleAvailable(long display, int screen, int vid);
    native int getStencilSize(long display, int screen, int vid);

    /*
     *  Chooses the best FBConfig for Java 3D apps.
     */
    @Override
    GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D template,
            GraphicsConfiguration[] gc) {

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc[0]).getDevice();

	if (!X11NativeScreenInfo.isGLX13()) {
	    return null;
	}

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);

	if (debug) {
	    System.out.println("  X11NativeConfigTemplate3D: using device " + gd);
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
    	attrList[STENCIL_SIZE] = template.getStencilSize();
	// System.out.println("X11NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);

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
        X11GraphicsConfig gc0 = null;
        for (int i = 0; i < gc.length; i++) {
            if (((X11GraphicsConfig)gc[i]).getVisual() == visID) {
                gc0 = (X11GraphicsConfig)gc[i];
                break;
            }
        }

        // Just return if we didn't find a match
        if (gc0 == null) {
            return null;
        }

        // Create a new GraphicsConfig object based on the one we found
        X11GraphicsConfig gc1 =
            X11GraphicsConfig.getConfig(gd, gc0.getVisual(),
                gc0.getDepth(), gc0.getColormap(), false);

	// We need to cache the GraphicsTemplate3D and the private
        // fbconfig info.
	synchronized (Canvas3D.graphicsConfigTable) {
	    if (Canvas3D.graphicsConfigTable.get(gc1) == null) {
                GraphicsConfigInfo gcInfo = new GraphicsConfigInfo(template);
                gcInfo.setPrivateData(new Long(fbConfig[0]));
		Canvas3D.graphicsConfigTable.put(gc1, gcInfo);
            } else {
		freeFBConfig(fbConfig[0]);
            }
	}   
	return gc1;
    }

    /*
     * Determine if a given GraphicsConfiguration object can be used
     * by Java 3D.
     */
    @Override
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc) {

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	if (!X11NativeScreenInfo.isGLX13()) {
	    return false;
	}

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);

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
    	attrList[STENCIL_SIZE] = template.getStencilSize();
	// System.out.println("X11NativeConfigTemplate3D : getStencilSize " + 
	// attrList[STENCIL_SIZE]);
	
	long[] fbConfig = new long[1];
        int visID = chooseOglVisual(display, screen, attrList, fbConfig);
	
        if (visID == 0 || fbConfig[0] == 0)
	    return false;  // no valid visual was found
	else
	    return true;	
    }


    // Return whether stereo is available.
    @Override
    boolean hasStereo(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isStereoAvailable(display, screen, vid);
    }

    // Return the stencil of this canvas.
    @Override
    int getStencilSize(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return getStencilSize(display, screen, vid);
    }

    // Return whether a double buffer is available.
    @Override
    boolean hasDoubleBuffer(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isDoubleBufferAvailable(display, screen, vid);
    }

    // Return whether scene antialiasing is available.
    @Override
    boolean hasSceneAntialiasingAccum(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isSceneAntialiasingAccumAvailable(display, screen, vid);
    }


    // Return whether scene antialiasing is available.
    @Override
    boolean hasSceneAntialiasingMultisample(Canvas3D c3d) {
	GraphicsConfiguration gc = c3d.graphicsConfiguration;

        X11GraphicsDevice gd =
            (X11GraphicsDevice)((X11GraphicsConfig)gc).getDevice();

	long display = NativeScreenInfo.getNativeScreenInfo().getDisplay();
	int screen = NativeScreenInfo.getNativeScreenInfo().getScreen(gd);
	int vid = ((X11GraphicsConfig)gc).getVisual();

	return isSceneAntialiasingMultisampleAvailable(display, screen, vid);
    }
    
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
