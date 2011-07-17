/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * The DrawingSurfaceObject class is used to manage native drawing surface 
 */
class NoopDrawingSurfaceObject extends DrawingSurfaceObject {
    
    NoopDrawingSurfaceObject(Canvas3D cv) {
        super(cv);
        
        System.err.println("NoopDrawingSurfaceObject constructed");
    }

    synchronized boolean renderLock() {
//        System.err.println("NoopDrawingSurfaceObject.renderLock()");
        gotDsiLock = true;
        return true;
    }

    synchronized void unLock() {
//        System.err.println("NoopDrawingSurfaceObject.unLock()");
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
