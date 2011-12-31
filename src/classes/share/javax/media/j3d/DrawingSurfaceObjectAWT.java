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


/**
 * The DrawingSurfaceObject class is used to manage native drawing surface
 */

class DrawingSurfaceObjectAWT extends DrawingSurfaceObject {

    // drawing surface
    private long nativeDS = 0;
    private long dsi = 0;

    private boolean doLastUnlock = false;
    private boolean xineramaDisabled = false;

    private long display = 0;
    private int screenID = 0;

    private static long nativeAWT = 0;

    private native boolean lockAWT(long ds);
    private native void unlockAWT(long ds);
    private static native void lockGlobal(long awt);
    private static native void unlockGlobal(long awt);
    private native long getDrawingSurfaceAWT(Canvas3D cv, long awt);
    private native long getDrawingSurfaceInfo(long ds);
    private static native void freeResource(long awt, long ds, long dsi);

    // TODO: long window
    private native int getDrawingSurfaceWindowIdAWT(Canvas3D cv, long ds, long dsi,
            long display, int screenID,
            boolean xineramaDisabled);

    DrawingSurfaceObjectAWT(Canvas3D cv, long awt,
			    long display, int screenID,
			    boolean xineramaDisabled) {
        super(cv);
	nativeAWT = awt;

	this.display = display;
	this.screenID = screenID;
	this.xineramaDisabled = xineramaDisabled;
    }

    synchronized boolean renderLock() {

        if (onScreen) {
	    if (nativeDS == 0) {
		return false;
	    } else {
                if (lockAWT(nativeDS)) {
                    gotDsiLock = true;
		    return true;
	        } else {
		    return false;
                }
	    }
        } else {
	    gotDsiLock = true;
	    lockGlobal(nativeAWT);
	}
        return true;
    }

    synchronized void unLock() {

	if (gotDsiLock) {
	    if (onScreen) {
		if (nativeDS != 0) {
		    unlockAWT(nativeDS);
		    gotDsiLock = false;
		    if (doLastUnlock) {
			nativeDS = 0;
			dsi = 0;
			doLastUnlock = false;
		    }
		}
	    } else {
		unlockGlobal(nativeAWT);
		gotDsiLock = false;
	    }
	}
    }


    synchronized void getDrawingSurfaceObjectInfo() {
        // Free old DS and DSI
        if (nativeDS != 0 && dsi != 0) {
            freeResource(nativeAWT, nativeDS, dsi);
            nativeDS = 0;
            dsi = 0;
        }

        // get native drawing surface - ds
	nativeDS = getDrawingSurfaceAWT(canvas, nativeAWT);

	// get window id
	if (nativeDS != 0) {
	    dsi = getDrawingSurfaceInfo(nativeDS);
	    if (dsi != 0) {
                long nativeDrawable = getDrawingSurfaceWindowIdAWT
		    (canvas, nativeDS, dsi, display, screenID,
		     xineramaDisabled);
                canvas.drawable = new NativeDrawable(nativeDrawable);
	    }
	}
    }


    synchronized void invalidate() {
	if (gotDsiLock && (nativeDS != 0)) {
	    // Should not call unlock in AWT thread
	    // Otherwise IllegalMonitorException will throw
	    //	    unlockAWT(nativeDS);
	    // We don't reset  nativeDS & dsi to 0 here.
	    // This allow Renderer to continue unLock.
	    doLastUnlock = true;
	} else {
	    nativeDS = 0;
	    dsi = 0;
	}
    }

    static void freeDrawingSurface(Object obj) {
	long p[] = (long[]) obj;
        freeResource(nativeAWT, p[0], p[1]);
    }

    long getDSI() {
	return dsi;
    }

    long getDS() {
	return nativeDS;
    }

}
