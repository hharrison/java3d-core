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

/**
 * The DrawingSurfaceObject class is used to manage native drawing surface
 */

abstract class DrawingSurfaceObject extends Object {

    Canvas3D canvas;
    boolean gotDsiLock = false;
    boolean onScreen;

    abstract boolean renderLock();
    abstract void unLock();
    abstract void getDrawingSurfaceObjectInfo();
    abstract void invalidate();

    DrawingSurfaceObject(Canvas3D cv) {
        canvas = cv;
	onScreen = !cv.offScreen;
    }

    synchronized boolean isLocked() {
	return gotDsiLock;
    }

    synchronized void contextValidated() {
        canvas.validCtx = true;
    }
}
