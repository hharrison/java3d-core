/*
 * $RCSfile$
 *
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
