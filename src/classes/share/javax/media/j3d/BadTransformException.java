/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * Indicates an attempt to use a Tranform3D object that is
 * inappropriate for the object in which it is being used.
 * For example:
 * <ul>
 * <li>
 * Transforms that are used in the scene graph, within a TransformGroup
 * node, must be affine.  They may optionally contain a non-uniform
 * scale and/or a shear, subject to other listed restrictions.
 * <li>
 * All transforms in the TransformGroup nodes above a ViewPlatform
 * object must be congruent.  This ensures that the Vworld coordinates to
 * ViewPlatform coordinates transform is angle and length-preserving with
 * no shear and only uniform scale.
 * <li>
 * Most viewing transforms other than those in the scene graph can
 * only contain translation and rotation.
 * <li>
 * The projection transform is allowed to be non-affine, but it
 * must either be a single point perspective projection or a parallel
 * projection.
 * </ul>
 */
public class BadTransformException extends RuntimeException{

/**
 * Create the exception object with default values.
 */
  public BadTransformException(){
  }

/**
 * Create the exception object that outputs message.
 * @param str the message string to be output.
 */
  public BadTransformException(String str){

    super(str);
  }

}
