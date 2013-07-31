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

import java.awt.IllegalComponentStateException;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The CanvasViewEventCatcher class is used to track events on a Canvas3D that
 * may cause view matries to change.
 *
 */
class CanvasViewEventCatcher extends ComponentAdapter {

    // The canvas associated with this event catcher
    private Canvas3D canvas;
    private static final boolean DEBUG = false;

    CanvasViewEventCatcher(Canvas3D c) {
	canvas = c;
    }

    @Override
    public void componentResized(ComponentEvent e) {
	if (DEBUG) {
	    System.err.println("Component resized " + e);
	}

	if(e.getComponent() == canvas ) {
	    if (DEBUG) {
		System.err.println("It is canvas!");
	    }
	    synchronized(canvas) {
                synchronized (canvas.dirtyMaskLock) {
                    canvas.cvDirtyMask[0] |= Canvas3D.MOVED_OR_RESIZED_DIRTY;
                    canvas.cvDirtyMask[1] |= Canvas3D.MOVED_OR_RESIZED_DIRTY;
                }
		canvas.resizeGraphics2D = true;
	    }

	    // see comment below
	    try {
		canvas.newSize = canvas.getSize();
		canvas.newPosition = canvas.getLocationOnScreen();
	    } catch (IllegalComponentStateException ex) {}

	}
    }

    @Override
    public void componentMoved(ComponentEvent e) {
	if (DEBUG) {
	    System.err.println("Component moved " + e);
	}

        synchronized(canvas) {
            synchronized (canvas.dirtyMaskLock) {
                canvas.cvDirtyMask[0] |= Canvas3D.MOVED_OR_RESIZED_DIRTY;
                canvas.cvDirtyMask[1] |= Canvas3D.MOVED_OR_RESIZED_DIRTY;
            }
        }
	// Can't sync. with canvas lock since canvas.getLocationOnScreen()
	// required Component lock. The order is reverse of
	// removeNotify() lock sequence which required Component lock
	// first, then canvas lock in removeComponentListener()

	try {
	    canvas.newSize = canvas.getSize();
	    canvas.newPosition = canvas.getLocationOnScreen();
	} catch (IllegalComponentStateException ex) {}

    }

}
