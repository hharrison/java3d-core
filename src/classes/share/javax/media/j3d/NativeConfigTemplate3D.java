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

import java.awt.GraphicsConfiguration;

/**
 * Native config template class. A singleton instance of the appropriate
 * concrete subclass is created by a factory method using reflection.
 */
abstract class NativeConfigTemplate3D {
    //  These definitions are used by both the X11 and Win32 subclasses
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

    private static final String x11ClassName = "javax.media.j3d.X11NativeConfigTemplate3D";
    private static final String win32ClassName = "javax.media.j3d.Win32NativeConfigTemplate3D";

    // The singleton instance of this class
    private static NativeConfigTemplate3D nativeConfigTemplate3D = null;

    protected NativeConfigTemplate3D() {
    }

    // This method is called exactly once by the initialization method of
    // the NativePipeline class
    synchronized static void createNativeConfigTemplate3D() {
        String className;
        if (MasterControl.isWindows()) {
            className = win32ClassName;
        } else {
            className = x11ClassName;
        }

        final String templateClassName = className;
        nativeConfigTemplate3D = (NativeConfigTemplate3D)
            java.security.AccessController.doPrivileged(new
                java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            Class templateClass = Class.forName(templateClassName);
                            return templateClass.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    static NativeConfigTemplate3D getNativeConfigTemplate3D() {
        return nativeConfigTemplate3D;
    }


    /*
     *  Chooses the best FBConfig for Java 3D apps.
     */
    abstract GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D template,
            GraphicsConfiguration[] gc);

    /*
     * Determine if a given GraphicsConfiguration object can be used
     * by Java 3D.
     */
    abstract boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D template,
                                      GraphicsConfiguration gc);


    // Return whether stereo is available.
    abstract boolean hasStereo(Canvas3D c3d);

    // Return the stencil of this canvas.
    abstract int getStencilSize(Canvas3D c3d);

    // Return whether a double buffer is available.
    abstract boolean hasDoubleBuffer(Canvas3D c3d);

    // Return whether scene antialiasing is available.
    abstract boolean hasSceneAntialiasingAccum(Canvas3D c3d);


    // Return whether scene antialiasing is available.
    abstract boolean hasSceneAntialiasingMultisample(Canvas3D c3d);
    
    // Ensure that the native libraries are loaded
    static {
 	VirtualUniverse.loadLibraries();
    }
}
