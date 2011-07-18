/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.internal;

/**
 * The FastVector object is a growable array of ints.  It's much faster
 * than the Java Vector class because it isn't synchronized.  This class
 * was created because it is needed in several places by graphics
 * utilities.
 */
public class FastVector {

  private int data[];
  private int capacity;
  private int increment;
  private int size;

  /**
   * Add an element to the end of the array.
   */
  public void addElement(int element)
  {
    if (size >= capacity) {
      capacity += (increment == 0) ? capacity : increment;
      int newData[] = new int[capacity];
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
    data[size++] = element;
  } // End of addElement



  /**
   * Get number of ints currently stored in the array;
   */
  public int getSize()
  {
    return size;
  } // End of getSize



  /**
   * Get access to array data
   */
  public int[] getData()
  {
    return data;
  } // End of getData



  /**
   * Constructor.
   * @param initialCapacity Number of ints the object can hold
   * without reallocating the array.
   * @param capacityIncrement Once the array has grown beyond
   * its capacity, how much larger the reallocated array should be.
   */
  public FastVector(int initialCapacity, int capacityIncrement)
  {
    data = new int[initialCapacity];
    capacity = initialCapacity;
    increment = capacityIncrement;
    size = 0;
  } // End of FastVector(int, int)



  /**
   * Constructor.
   * When the array runs out of space, its size is doubled.
   * @param initialCapacity Number of ints the object can hold
   * without reallocating the array.
   */
  public FastVector(int initialCapacity)
  {
    data = new int[initialCapacity];
    capacity = initialCapacity;
    increment = 0;
    size = 0;
  } // End of FastVector(int)



  /**
   * Constructor.
   * The array is constructed with initial capacity of one integer.
   * When the array runs out of space, its size is doubled.
   */
  public FastVector()
  {
    data = new int[1];
    capacity = 1;
    increment = 0;
    size = 0;
  } // End of FastVector()
} // End of class FastVector

// End of file FastVector.java
