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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;


/**
 * The J3DGraphics2D class extends Graphics2D to provide 2D rendering
 * into a Canvas3D.  It is an abstract base class that is further
 * extended by a non-public Java 3D implementation class.  This class
 * allows Java 2D rendering to be mixed with Java 3D rendering in the
 * same Canvas3D, subject to the same restrictions as imposed for 3D
 * immediate-mode rendering: In mixed-mode rendering, all Java 2D
 * requests must be done from one of the Canvas3D callback methods; in
 * pure-immediate mode, the Java 3D renderer must be stopped for the
 * Canvas3D being rendered into.
 *
 * <p>
 * An application obtains a J3D 2D graphics context object from the
 * Canvas3D object that the application wishes to render into by using
 * the getGraphics2D method. A new J3DGraphics2D object is created if
 * one does not already exist.
 *
 * <p>
 * Note that the drawing methods in this class, including those
 * inherited from Graphics2D, are not necessarily executed
 * immediately.  They may be buffered up for future execution.
 * Applications must call the <code><a
 * href="#flush(boolean)">flush</a>(boolean)</code> method to ensure
 * that the rendering actually happens. The flush method is implicitly
 * called in the following cases:
 *
 * <ul>
 * <li>The <code>Canvas3D.swap</code> method calls
 * <code>flush(true)</code></li>
 * <li>The Java 3D renderer calls <code>flush(true)</code> prior to
 * swapping the buffer for a double buffered on-screen Canvas3D</li>
 * <li>The Java 3D renderer calls <code>flush(true)</code> prior to
 * copying into the off-screen buffer of an off-screen Canvas3D</li>
 * <li>The Java 3D renderer calls <code>flush(false)</code> after
 * calling the preRender, renderField, postRender, and postSwap
 * Canvas3D callback methods.</li>
 * </ul>
 *
 * <p>
 * A single-buffered, pure-immediate mode application must explicitly
 * call flush to ensure that the graphics will be rendered to the
 * Canvas3D.
 *
 * @see Canvas3D#getGraphics2D
 *
 * @since Java 3D 1.2
 */

public abstract class J3DGraphics2D extends Graphics2D {

    // Package scope contructor
    J3DGraphics2D() {
    }

    /**
     * This method is not supported.  The only way to obtain a
     * J3DGraphics2D is from the associated Canvas3D.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @see Canvas3D#getGraphics2D
     */
    public final Graphics create() {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.  The only way to obtain a
     * J3DGraphics2D is from the associated Canvas3D.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @see Canvas3D#getGraphics2D
     */
    public final Graphics create(int x, int y, int width, int height) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.  Clearing a Canvas3D is done implicitly
     * via a Background node in the scene graph or explicitly via the clear
     * method in a 3D graphics context.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @see Background
     * @see GraphicsContext3D#setBackground
     * @see GraphicsContext3D#clear
     */
    public final void setBackground(Color color) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.  Clearing a Canvas3D is done implicitly
     * via a Background node in the scene graph or explicitly via the clear
     * method in a 3D graphics context.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @see Background
     * @see GraphicsContext3D#getBackground
     * @see GraphicsContext3D#clear
     */
    public final Color getBackground() {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported.  Clearing a Canvas3D is done implicitly
     * via a Background node in the scene graph or explicitly via the clear
     * method in a 3D graphics context.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @see Background
     * @see GraphicsContext3D#setBackground
     * @see GraphicsContext3D#clear
     */
    public final void clearRect(int x, int y, int width, int height) {
	throw new UnsupportedOperationException();
    }


    /**
     * Flushes all previously executed rendering operations to the
     * drawing buffer for this 2D graphics object.
     *
     * @param wait flag indicating whether or not to wait for the
     * rendering to be complete before returning from this call.
     */
    public abstract void flush(boolean wait);

    /**
     * Draws the specified image and flushes the buffer.  This is
     * functionally equivalent to calling <code>drawImage(...)</code>
     * followed by <code>flush(false)</code>, but can avoid the cost
     * of making an extra copy of the image in some cases.  Anything
     * previously drawn to this J3DGraphics2D will be flushed before
     * the image is drawn.
     *
     * @param img The image to draw
     * @param x The x location to draw at
     * @param y The y location to draw at
     * @param observer The ImageObserver
     *
     * @since Java 3D 1.3
     */
    public abstract void drawAndFlushImage(BufferedImage img, int x, int y,
					   ImageObserver observer);

}
