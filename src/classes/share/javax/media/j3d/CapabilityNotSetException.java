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
 * Indicates an access to a live or
 * compiled Scene Graph object without the required capability
 * set.
 */
public class CapabilityNotSetException extends RestrictedAccessException {

/**
 * Create the exception object with default values.
 */
  public CapabilityNotSetException(){
  }

/**
 * Create the exception object that outputs message.
 * @param str the message string to be output.
 */
  public CapabilityNotSetException(String str){

    super(str);
  }

}
