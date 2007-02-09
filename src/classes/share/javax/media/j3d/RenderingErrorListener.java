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

/**
 * Listener interface for monitoring Java 3D rendering errors.
 *
 * @see VirtualUniverse#addRenderingErrorListener
 *
 * @since Java 3D 1.5
 */
public interface RenderingErrorListener {
    /**
     * Invoked when an error occurs in the Java 3D rendering system.
     *
     * @param error object that contains the details of the error.
     */
    public void errorOccurred(RenderingError error);
}
