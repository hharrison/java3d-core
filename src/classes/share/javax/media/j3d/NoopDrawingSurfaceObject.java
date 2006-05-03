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
class NoopDrawingSurfaceObject extends DrawingSurfaceObject {
    
    NoopDrawingSurfaceObject(Canvas3D cv) {
        super(cv);
        
        System.err.println("NoopDrawingSurfaceObject constructed");
    }

    synchronized boolean renderLock() {
        System.err.println("NoopDrawingSurfaceObject.renderLock()");
        gotDsiLock = true;
        return true;
    }

    synchronized void unLock() {
        System.err.println("NoopDrawingSurfaceObject.unLock()");
        gotDsiLock = false;
    }

    synchronized void getDrawingSurfaceObjectInfo() {
        if (canvas.drawable == null) {
            System.err.println(
                    "NoopDrawingSurfaceObject.getDrawingSurfaceObjectInfo: window = "
                    + canvas.drawable);

            canvas.drawable = new NoopDrawable();
        }
    }

    synchronized void invalidate() {
        System.err.println("NoopDrawingSurfaceObject.invalidate()");
    }

    /**
     * Dummy drawable for noop pipeline
     */
    static class NoopDrawable implements Drawable {
    }

}
