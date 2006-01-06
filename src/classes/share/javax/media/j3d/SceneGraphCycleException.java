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
 * Indicates a graph that contains a cycle.
 * Java 3D scene graphs are directed acyclic graphs and, as such, do not 
 * permit cycles.
 * This exception is thrown when a graph containing a cycle:
 * <ul>
 * <li> is made live
 * <li> is compiled
 * <li> is cloned
 * <li> has getBounds() called on it.
 * </ul>
 */
public class SceneGraphCycleException extends IllegalSceneGraphException{

/**
 * Create the exception object with default values.
 */
  public SceneGraphCycleException(){
  }

/**
 * Create the exception object that outputs message.
 * @param str the message string to be output.
 */
  public SceneGraphCycleException(String str){

    super(str);
  }

}
