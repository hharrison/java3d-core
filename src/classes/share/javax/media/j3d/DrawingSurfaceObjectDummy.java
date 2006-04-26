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
    
    static long windowId = 0L;
    private static synchronized long nextWindow() {
        return ++windowId;
    }

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
        if (canvas.window == 0) {
            System.err.println(
                    "DrawingSurfaceObjectDummy.getDrawingSurfaceObjectInfo: window = "
                    + canvas.window);
            canvas.window = nextWindow();
        }
    }

    synchronized void invalidate() {
        System.err.println("DrawingSurfaceObjectDummy.invalidate()");
    }
}
