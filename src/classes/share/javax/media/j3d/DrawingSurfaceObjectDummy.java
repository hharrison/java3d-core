/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
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

class DrawingSurfaceObjectDummy extends DrawingSurfaceObject {

    DrawingSurfaceObjectDummy(Canvas3D cv) {
        super(cv);
        
        System.err.println("DrawingSurfaceObjectDummy constructed");
    }

    synchronized boolean renderLock() {
        System.err.println("DrawingSurfaceObjectDummy.renderLock()");
        gotDsiLock = true;
        return true;
    }

    synchronized void unLock() {
        System.err.println("DrawingSurfaceObjectDummy.unLock()");
        gotDsiLock = false;
    }

    synchronized void getDrawingSurfaceObjectInfo() {
        System.err.println("DrawingSurfaceObjectDummy.getDrawingSurfaceObjectInfo()");
        canvas.window = 1;
    }

    synchronized void invalidate() {
        System.err.println("DrawingSurfaceObjectDummy.invalidate()");
    }
}
