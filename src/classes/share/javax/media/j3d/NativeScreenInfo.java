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

/**
 * Native screen info class. A singleton instance of the appropriate
 * concrete subclass is created by a factory method using reflection.
 */
abstract class NativeScreenInfo {
    private static final String x11ClassName = "javax.media.j3d.X11NativeScreenInfo";
    private static final String win32ClassName = "javax.media.j3d.Win32NativeScreenInfo";

    // The singleton instance of this class
    private static NativeScreenInfo nativeScreenInfo = null;

    protected NativeScreenInfo() {
    }

    // This method is called exactly once by the initialization method of
    // the NativePipeline class
    synchronized static void createNativeScreenInfo() {
        String className;
        if (MasterControl.isWindows()) {
            className = win32ClassName;
        } else {
            className = x11ClassName;
        }

        final String scrInfoClassName = className;
        nativeScreenInfo = (NativeScreenInfo)
            java.security.AccessController.doPrivileged(new
                java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            Class scrInfoClass = Class.forName(scrInfoClassName);
                            return scrInfoClass.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    static NativeScreenInfo getNativeScreenInfo() {
        return nativeScreenInfo;
    }

    /**
     * Get the display handle
     */
    abstract long getDisplay();

    /**
     * Get the screen number for the given graphics device
     */
    abstract int getScreen(GraphicsDevice graphicsDevice);
}
