/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Listener interface for monitoring errors in Shader Programs.
 * Compile and link errors are reported by the shader compiler, as are
 * runtime errors, such as those resulting from shader attributes that
 * aren't found or are of the wrong type.
 *
 * @see VirtualUniverse#addShaderErrorListener
 *
 * @since Java 3D 1.4
 */
public interface ShaderErrorListener {
    /**
     * Invoked when an error occurs while compiling, linking or
     * executing a programmable shader.
     *
     * @param error object that contains the details of the error.
     */
    public void errorOccurred(ShaderError error);
}
