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
 * Indicates a problem in loading or playing a sound sample.
 */
public class SoundException extends RuntimeException{

/**
 * Create the exception object with default values.
 */
  public SoundException(){
  }

/**
 * Create the exception object that outputs message.
 * @param str the message string to be output.
 */
  public SoundException(String str){

    super(str);
  }

}
