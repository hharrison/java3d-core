/*
 * $RCSfile$
 *
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
