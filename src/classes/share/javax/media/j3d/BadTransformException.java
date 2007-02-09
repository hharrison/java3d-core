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
