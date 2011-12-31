/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Vector3d;

/**
 * High resolution coordinate object.
 *
 */

/**
 * The HiResCoord object specifies the location of scene
 * components within the Virtual Universe.
 * The coordinates of all scene graph objects are relative to
 * the HiResCoord of the Locale in which they are contained.
 * <P>
 * The HiResCoord defines a point using a set of three
 * high-resolution coordinates, each of which consists of three
 * two's-complement fixed-point numbers.
 * Each high-resolution number consists of 256 total bits with a
 * binary point at bit 128, or between the integers at index
* 3 and 4. A high-resolution coordinate of 1.0 is defined to be exactly
 * 1 meter. This coordinate system is sufficient to describe a
 * universe in excess of several billion light years across, yet
 * still define objects smaller than a proton.
 * <P>
 * Java 3D uses integer arrays of length
 * eight to define or extract a single 256-bit coordinate value.
 * Java 3D interprets the integer at index 0 as the 32
 * most-significant bits and the integer at index 7 as the 32
 * least-significant bits.
 */

public class HiResCoord {
    /**
     * The eight-element array containing the high resolution coordinate's
     * x value.
     */
    int		x[];

    /**
     * The eight-element array containing the high resolution coordinate's
     * y value.
     */
    int		y[];

    /**
     * The eight-element array containing the high resolution coordinate's
     * z value.
     */
    int		z[];

private double scales[] = {
      79228162514264337593543950336.0,                                             // 2^96
      18446744073709551616.0,                                                      // 2^64
      4294967296.0,                                                                // 2^32
      1.0,                                                                         // 2^0
      2.3283064365386962890625e-10,                                                // 2^-32
      5.421010862427522170037264004349708557128906250000000000000000e-20,          // 2^-64
      1.26217744835361888865876570445245796747713029617443680763244628906250e-29,  // 2^-96
      2.938735877055718769921841343055614194546663891930218803771879265696043148636817932128906250e-39 };



    /**
     * Constructs and initializes a new HiResCoord using the values
     * provided in the argument.
     * The HiResCoord represents 768 bits of floating point 3-Space.
     * @param X an eight element array specifying the x position
     * @param Y an eight element array specifying the y position
     * @param Z an eight element array specifying the z position
     */
    public HiResCoord(int[] X, int[] Y, int[] Z) {
    int i;

	this.x = new int[8];
	this.y = new int[8];
	this.z = new int[8];

        for(i=0;i<8;i++) {
           this.x[i] = X[i];
           this.y[i] = Y[i];
           this.z[i] = Z[i];
        }

    }

    /**
     * Constructs and initializes a new HiResCoord using the values
     * provided in the argument.
     * The HiResCoord represents 768 bits of floating point 3-Space.
     * @param hc the HiResCoord to copy
     */
    public HiResCoord(HiResCoord hc) {
	this.x = new int[8];
	this.y = new int[8];
	this.z = new int[8];

	this.x[0] = hc.x[0];
	this.y[0] = hc.y[0];
	this.z[0] = hc.z[0];

	this.x[1] = hc.x[1];
	this.y[1] = hc.y[1];
	this.z[1] = hc.z[1];

	this.x[2] = hc.x[2];
	this.y[2] = hc.y[2];
	this.z[2] = hc.z[2];

	this.x[3] = hc.x[3];
	this.y[3] = hc.y[3];
	this.z[3] = hc.z[3];

	this.x[4] = hc.x[4];
	this.y[4] = hc.y[4];
	this.z[4] = hc.z[4];

	this.x[5] = hc.x[5];
	this.y[5] = hc.y[5];
	this.z[5] = hc.z[5];

	this.x[6] = hc.x[6];
	this.y[6] = hc.y[6];
	this.z[6] = hc.z[6];

	this.x[7] = hc.x[7];
	this.y[7] = hc.y[7];
	this.z[7] = hc.z[7];
    }

    /**
     * Constructs and initializes a new HiResCoord located at (0, 0, 0).
     * The HiResCoord represents 768 bits of floating point 3-Space.
     */
    public HiResCoord() {
	this.x = new int[8];
	this.y = new int[8];
	this.z = new int[8];
    }

    /**
     * Sets this HiResCoord to the location specified by the
     * parameters provided.
     * @param X an eight-element array specifying the x position
     * @param Y an eight-element array specifying the y position
     * @param Z an eight-element array specifying the z position
     */
    public void setHiResCoord(int[] X, int[] Y, int[] Z) {
    int i;

        for(i=0;i<8;i++) {
           this.x[i] = X[i];
           this.y[i] = Y[i];
           this.z[i] = Z[i];
        }

    }

    /**
     * Sets this HiResCoord to the location specified by the
     * hires provided.
     * @param hires the hires coordinate to copy
     */
    public void setHiResCoord(HiResCoord hires) {
	this.x[0] = hires.x[0];
	this.y[0] = hires.y[0];
	this.z[0] = hires.z[0];

	this.x[1] = hires.x[1];
	this.y[1] = hires.y[1];
	this.z[1] = hires.z[1];

	this.x[2] = hires.x[2];
	this.y[2] = hires.y[2];
	this.z[2] = hires.z[2];

	this.x[3] = hires.x[3];
	this.y[3] = hires.y[3];
	this.z[3] = hires.z[3];

	this.x[4] = hires.x[4];
	this.y[4] = hires.y[4];
	this.z[4] = hires.z[4];

	this.x[5] = hires.x[5];
	this.y[5] = hires.y[5];
	this.z[5] = hires.z[5];

	this.x[6] = hires.x[6];
	this.y[6] = hires.y[6];
	this.z[6] = hires.z[6];

	this.x[7] = hires.x[7];
	this.y[7] = hires.y[7];
	this.z[7] = hires.z[7];
    }


    /**
     * Sets this HiResCoord's X value to that specified by the argument.
     * @param X an eight-element array specifying the x position
     */
    public void setHiResCoordX(int[] X) {
	this.x[0] = X[0];
	this.x[1] = X[1];
	this.x[2] = X[2];
	this.x[3] = X[3];
	this.x[4] = X[4];
	this.x[5] = X[5];
	this.x[6] = X[6];
	this.x[7] = X[7];
    }

    /**
     * Sets this HiResCoord's Y value to that specified by the argument.
     * @param Y an eight-element array specifying the y position
     */
    public void setHiResCoordY(int[] Y) {
	this.y[0] = Y[0];
	this.y[1] = Y[1];
	this.y[2] = Y[2];
	this.y[3] = Y[3];
	this.y[4] = Y[4];
	this.y[5] = Y[5];
	this.y[6] = Y[6];
	this.y[7] = Y[7];
    }

    /**
     * Sets this HiResCoord's Z value to that specified by the argument.
     * @param Z an eight-element array specifying the z position
     */
    public void setHiResCoordZ(int[] Z) {
	this.z[0] = Z[0];
	this.z[1] = Z[1];
	this.z[2] = Z[2];
	this.z[3] = Z[3];
	this.z[4] = Z[4];
	this.z[5] = Z[5];
	this.z[6] = Z[6];
	this.z[7] = Z[7];
    }

    /**
     * Retrieves this HiResCoord's location and saves the coordinates
     * in the specified arrays. The arrays must be large enough
     * to hold all of the ints.
     * @param X an eight element array that will receive the x position
     * @param Y an eight element array that will receive the y position
     * @param Z an eight element array that will receive the z position
     */
    public void getHiResCoord(int[] X, int[] Y, int[] Z) {
	X[0] = this.x[0];
	X[1] = this.x[1];
	X[2] = this.x[2];
	X[3] = this.x[3];
	X[4] = this.x[4];
	X[5] = this.x[5];
	X[6] = this.x[6];
	X[7] = this.x[7];

	Y[0] = this.y[0];
	Y[1] = this.y[1];
	Y[2] = this.y[2];
	Y[3] = this.y[3];
	Y[4] = this.y[4];
	Y[5] = this.y[5];
	Y[6] = this.y[6];
	Y[7] = this.y[7];

	Z[0] = this.z[0];
	Z[1] = this.z[1];
	Z[2] = this.z[2];
	Z[3] = this.z[3];
	Z[4] = this.z[4];
	Z[5] = this.z[5];
	Z[6] = this.z[6];
	Z[7] = this.z[7];
    }

    /**
     * Retrieves this HiResCoord's location and places it into the hires
     * argument.
     * @param hc the hires coordinate that will receive this node's location
     */
    public void getHiResCoord(HiResCoord hc) {
	hc.x[0] = this.x[0];
	hc.x[1] = this.x[1];
	hc.x[2] = this.x[2];
	hc.x[3] = this.x[3];
	hc.x[4] = this.x[4];
	hc.x[5] = this.x[5];
	hc.x[6] = this.x[6];
	hc.x[7] = this.x[7];

	hc.y[0] = this.y[0];
	hc.y[1] = this.y[1];
	hc.y[2] = this.y[2];
	hc.y[3] = this.y[3];
	hc.y[4] = this.y[4];
	hc.y[5] = this.y[5];
	hc.y[6] = this.y[6];
	hc.y[7] = this.y[7];

	hc.z[0] = this.z[0];
	hc.z[1] = this.z[1];
	hc.z[2] = this.z[2];
	hc.z[3] = this.z[3];
	hc.z[4] = this.z[4];
	hc.z[5] = this.z[5];
	hc.z[6] = this.z[6];
	hc.z[7] = this.z[7];
    }

    /**
     * Retrieves this HiResCoord's X value and stores it in the specified
     * array. The array must be large enough to hold all of the ints.
     * @param X an eight-element array that will receive the x position
     */
    public void getHiResCoordX(int[] X) {
	X[0] = this.x[0];
	X[1] = this.x[1];
	X[2] = this.x[2];
	X[3] = this.x[3];
	X[4] = this.x[4];
	X[5] = this.x[5];
	X[6] = this.x[6];
	X[7] = this.x[7];
    }

    /**
     * Retrieves this HiResCoord's Y value and stores it in the specified
     * array. The array must be large enough to hold all of the ints.
     * @param Y an eight-element array that will receive the y position
     */
    public void getHiResCoordY(int[] Y) {
	Y[0] = this.y[0];
	Y[1] = this.y[1];
	Y[2] = this.y[2];
	Y[3] = this.y[3];
	Y[4] = this.y[4];
	Y[5] = this.y[5];
	Y[6] = this.y[6];
	Y[7] = this.y[7];
    }

    /**
     * Retrieves this HiResCoord's Z value and stores it in the specified
     * array. The array must be large enough to hold all of the ints.
     * @param Z an eight-element array that will receive the z position
     */
    public void getHiResCoordZ(int[] Z) {
	Z[0] = this.z[0];
	Z[1] = this.z[1];
	Z[2] = this.z[2];
	Z[3] = this.z[3];
	Z[4] = this.z[4];
	Z[5] = this.z[5];
	Z[6] = this.z[6];
	Z[7] = this.z[7];
    }

    /**
     * Compares the specified HiResCoord to this HiResCoord.
     * @param h1 the second HiResCoord
     * @return true if equal, false if not equal
     */
    public boolean equals(HiResCoord h1) {
        try {
	   return ((this.x[0] == h1.x[0])
		&& (this.x[1] == h1.x[1])
		&& (this.x[2] == h1.x[2])
		&& (this.x[3] == h1.x[3])
		&& (this.x[4] == h1.x[4])
		&& (this.x[5] == h1.x[5])
		&& (this.x[6] == h1.x[6])
		&& (this.x[7] == h1.x[7])
		&& (this.y[0] == h1.y[0])
		&& (this.y[1] == h1.y[1])
		&& (this.y[2] == h1.y[2])
		&& (this.y[3] == h1.y[3])
		&& (this.y[4] == h1.y[4])
		&& (this.y[5] == h1.y[5])
		&& (this.y[6] == h1.y[6])
		&& (this.y[7] == h1.y[7])
		&& (this.z[0] == h1.z[0])
		&& (this.z[1] == h1.z[1])
		&& (this.z[2] == h1.z[2])
		&& (this.z[3] == h1.z[3])
		&& (this.z[4] == h1.z[4])
		&& (this.z[5] == h1.z[5])
		&& (this.z[6] == h1.z[6])
		&& (this.z[7] == h1.z[7]));
        }
        catch (NullPointerException e2) {return false;}

    }

    /**
     * Returns true if the Object o1 is of type HiResCoord and all of the
     * data members of o1 are equal to the corresponding data members in
     * this HiResCoord.
     * @param o1 the second HiResCoord
     * @return true if equal, false if not equal
     */
    public boolean equals(Object  o1) {
        try {
	   HiResCoord h1 = (HiResCoord)o1;
   	   return ((this.x[0] == h1.x[0])
		&& (this.x[1] == h1.x[1])
		&& (this.x[2] == h1.x[2])
		&& (this.x[3] == h1.x[3])
		&& (this.x[4] == h1.x[4])
		&& (this.x[5] == h1.x[5])
		&& (this.x[6] == h1.x[6])
		&& (this.x[7] == h1.x[7])
		&& (this.y[0] == h1.y[0])
		&& (this.y[1] == h1.y[1])
		&& (this.y[2] == h1.y[2])
		&& (this.y[3] == h1.y[3])
		&& (this.y[4] == h1.y[4])
		&& (this.y[5] == h1.y[5])
		&& (this.y[6] == h1.y[6])
		&& (this.y[7] == h1.y[7])
		&& (this.z[0] == h1.z[0])
		&& (this.z[1] == h1.z[1])
		&& (this.z[2] == h1.z[2])
		&& (this.z[3] == h1.z[3])
		&& (this.z[4] == h1.z[4])
		&& (this.z[5] == h1.z[5])
		&& (this.z[6] == h1.z[6])
		&& (this.z[7] == h1.z[7]));
        }
        catch (NullPointerException e2) {return false;}
        catch (ClassCastException   e1) {return false;}


    }
    /**
     * Adds two HiResCoords placing the results into this HiResCoord.
     * @param h1 the first HiResCoord
     * @param h2 the second HiResCoord
     */
    public void add(HiResCoord h1, HiResCoord h2) {
    // needs to handle carry bits
    // move to long, add, add in carry bit

        hiResAdd( this, h1, h2 );

    }

    /**
     * Subtracts two HiResCoords placing the results into this HiResCoord.
     * @param h1 the first HiResCoord
     * @param h2 the second HiResCoord
     */
    public void sub(HiResCoord h1, HiResCoord h2) {
       HiResCoord tmpHc = new HiResCoord();

    // negate via two's complement then add
    //
        hiResNegate( tmpHc, h2);
        hiResAdd( this, h1, tmpHc);

    }

    /**
     * Negates the specified HiResCoords and places the
     * results into this HiResCoord.
     * @param h1 the source HiResCoord
     */
    public void negate(HiResCoord h1) {

        hiResNegate( this, h1);

    }

    /**
     * Negates this HiResCoord
     */
    public void negate() {

      hiResNegate( this, this );

    }

    /**
     * Scales the specified HiResCoords by the specified value and
     * places the results into this HiResCoord.
     * @param scale the amount to scale the specified HiResCoord
     * @param h1 the source HiResCoord
     */
    public void scale(int scale, HiResCoord h1) {
	 hiResScale( h1.x, this.x, scale);
	 hiResScale( h1.y, this.y, scale);
	 hiResScale( h1.z, this.z, scale);
    }

    /**
     * Scales this HiResCoord by the specified value.
     * @param scale the amount to scale the specified HiResCoord
     */
    public void scale(int scale) {
	 hiResScale( this.x, this.x, scale);
	 hiResScale( this.y, this.y, scale);
	 hiResScale( this.z, this.z, scale);
	 return;
    }

    /**
     * Subtracts the specified HiResCoord from this HiResCoord
     * placing the difference vector into the specified
     * double-precision vector.
     * @param h1 the HiResCoord to be subtracted from this
     * @param v the vector that will receive the result
     */
    public void difference(HiResCoord h1, Vector3d v) {
       // negate coord via two compliment, add, convert result to double
       // by scaling each bit set appropriately

        hiResDiff( this, h1, v);
	return;
    }

    /**
     * The floating point distance between the specified
     * HiResCoord and this HiResCoord.
     * @param h1 the second HiResCoord
     */
    public double distance(HiResCoord h1) {
        Vector3d diff = new Vector3d();

        hiResDiff( this, h1, diff);

        return( Math.sqrt( diff.x*diff.x + diff.y*diff.y + diff.z*diff.z));
    }

    private void hiResNegate( HiResCoord ho, HiResCoord hi) {

        negateCoord( ho.x, hi.x);
        negateCoord( ho.y, hi.y);
       negateCoord( ho.z, hi.z);

	return;
    }

   private void negateCoord( int cout[], int cin[] ) {
     int i;

     for(i=0;i<8;i++) {
        cout[i] = ~cin[i];  // take compliment of each
     }

     for(i=7;i>=0;i--) {             // add one
       if( cout[i] == 0xffffffff) {
          cout[i] = 0;
       } else {
          cout[i] += 1;
          break;
       }
     }
     return;
   }

   private void hiResAdd(HiResCoord ho, HiResCoord h1, HiResCoord h2 ){
    int i;
    long tmp1, tmp2,carry;
    long signMask = Integer.MAX_VALUE;
    long signBit = 1;
    signBit =  signBit << 31;
    long carryMask = 0x7fffffff;
    carryMask = carryMask <<1;
    carryMask += 1;


    carry = 0;
    for(i=7;i>0;i--) {
        tmp1 = 0;
        tmp1 = signMask & h1.x[i];   // mask off sign bit so will not get put in msb
        if( h1.x[i] < 0 ) tmp1 |= signBit; // add sign bit back

        tmp2 = 0;
        tmp2 = signMask & h2.x[i];   // mask off sign bit so will not get put in msb
        if( h2.x[i] < 0 ) tmp2 |= signBit; // add sign bit back

        tmp2 = tmp2+tmp1 + carry;
        carry = tmp2 >> 32;  // get carry bits for next operation
        ho.x[i] = (int)(tmp2 & carryMask); // mask off high bits
    }
    ho.x[0] = h1.x[0] + h2.x[0] + (int)carry;


    carry = 0;
    for(i=7;i>0;i--) {
        tmp1 = 0;
        tmp1 = signMask & h1.y[i];   // mask off sign bit so will not get put in msb
        if( h1.y[i] < 0 ) tmp1 |= signBit; // add sign bit back

        tmp2 = 0;
        tmp2 = signMask & h2.y[i];   // mask off sign bit so will not get put in msb
        if( h2.y[i] < 0 ) tmp2 |= signBit; // add sign bit back

        tmp2 = tmp2+tmp1 + carry;
        carry = tmp2 >> 32;  // get carry bits for next operation
        ho.y[i] = (int)(tmp2 & carryMask); // mask off high bits
    }
    ho.y[0] = h1.y[0] + h2.y[0] + (int)carry;

    carry = 0;
    for(i=7;i>0;i--) {
        tmp1 = 0;
        tmp1 = signMask & h1.z[i];   // mask off sign bit so will not get put in msb
        if( h1.z[i] < 0 ) tmp1 |= signBit; // add sign bit back

        tmp2 = 0;
        tmp2 = signMask & h2.z[i];   // mask off sign bit so will not get put in msb
        if( h2.z[i] < 0 ) tmp2 |= signBit; // add sign bit back

        tmp2 = tmp2+tmp1 + carry;
        carry = tmp2 >> 32;  // get carry bits for next operation
        ho.z[i] = (int)(tmp2 & carryMask); // mask off high bits
    }
    ho.z[0] = h1.z[0] + h2.z[0] + (int)carry;
    return;
  }

  private void hiResScale( int tin[], int tout[], double scale) {
      int i;
      long tmp,carry;
      int signMask = Integer.MAX_VALUE;
      long carryMask = 0x7fffffff;
      carryMask = carryMask <<1;
      carryMask += 1;
      long signBit = 1;
      signBit =  signBit << 31;

      carry = 0;
      for(i=7;i>0;i--) {
        tmp = 0;
        tmp = (long)(signMask & tin[i]);   // mask off sign bit
        if( tin[i] < 0 ) tmp |= signBit; // add sign bit back
        tmp = (long)(tmp*scale + carry);
        carry = tmp >> 32;  // get carry bits for next operation
        tout[i] = (int)(tmp & carryMask); // mask off high bits
      }
      tout[0] = (int)(tin[0]*scale + carry);
      return;
  }
  private void hiResDiff( HiResCoord h1, HiResCoord h2, Vector3d diff) {
       int i;
       HiResCoord diffHi = new HiResCoord();
       long value;
       int coordSpace[] = new int[8];
       int[] tempCoord;
       int signMask = Integer.MAX_VALUE;
       long signBit = 1;
       signBit =  signBit << 31;

    // negate via two's complement then add
    //
        hiResNegate( diffHi, h2);
        hiResAdd( diffHi, h1, diffHi);


        if( diffHi.x[0] < 0 ) {
	   tempCoord = coordSpace;
	   negateCoord( tempCoord, diffHi.x );
        } else {
	   tempCoord = diffHi.x;
        }
        diff.x = 0;
        for(i=7;i>0;i--) {
	   value = (long)(tempCoord[i] & signMask);
	   if( tempCoord[i] < 0) value |= signBit;
	   diff.x += (double)(scales[i]*value);
	}
	diff.x += scales[0]*tempCoord[0];
        if( diffHi.x[0] < 0 )diff.x = -diff.x;

        if( diffHi.y[0] < 0 ) {
	   tempCoord = coordSpace;
	   negateCoord( tempCoord, diffHi.y );
        } else {
	   tempCoord = diffHi.y;
        }
        diff.y = 0;
        for(i=7;i>0;i--) {
	   value = (long)(tempCoord[i] & signMask);
	   if( tempCoord[i] < 0) value |= signBit;
	   diff.y += scales[i]*value;
	}
	diff.y += scales[0]*tempCoord[0];
        if( diffHi.y[0] < 0 )diff.y = -diff.y;

        if( diffHi.z[0] < 0 ) {
	   tempCoord = coordSpace;
	   negateCoord( tempCoord, diffHi.z );
        } else {
	   tempCoord = diffHi.z;
        }
        diff.z = 0;
        for(i=7;i>0;i--) {
	   value = (long)(tempCoord[i] & signMask);
	   if( tempCoord[i] < 0) value |= signBit;
	   diff.z += scales[i]*value;
	}
	diff.z += scales[0]*tempCoord[0];
        if( diffHi.z[0] < 0 )diff.z = -diff.z;
   return;
  }
}
