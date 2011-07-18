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
 * Utility class used when computing the hash code for
 * objects containing float or double values. This fixes Issue 36.
 */
public class HashCodeUtil {
    /**
     * Returns the representation of the specified floating-point
     * value according to the IEEE 754 floating-point "single format"
     * bit layout, after first mapping -0.0 to 0.0. This method is
     * identical to Float.floatToIntBits(float) except that an integer
     * value of 0 is returned for a floating-point value of
     * -0.0f. This is done for the purpose of computing a hash code
     * that satisfies the contract of hashCode() and equals(). The
     * equals() method in some Java&nbsp;3D classes does a pair-wise
     * "==" test on each floating-point field in the class. Since
     * 0.0f&nbsp;==&nbsp;-0.0f returns true, we must also return the
     * same hash code for two objects, one of which has a field with a
     * value of -0.0f and the other of which has a cooresponding field
     * with a value of 0.0f.
     *
     * @param f an input floating-point number
     * @return the integer bits representing that floating-point
     * number, after first mapping -0.0f to 0.0f
     */
    public static int floatToIntBits(float f) {
	// Check for +0 or -0
	if (f == 0.0f) {
	    return 0;
	}
	else {
	    return Float.floatToIntBits(f);
	}
    }

    /**
     * Returns the representation of the specified floating-point
     * value according to the IEEE 754 floating-point "double format"
     * bit layout, after first mapping -0.0 to 0.0. This method is
     * identical to Double.doubleToLongBits(double) except that an
     * integer value of 0L is returned for a floating-point value of
     * -0.0. This is done for the purpose of computing a hash code
     * that satisfies the contract of hashCode() and equals(). The
     * equals() method in some Java&nbsp;3D classes does a pair-wise
     * "==" test on each floating-point field in the class. Since
     * 0.0&nbsp;==&nbsp;-0.0 returns true, we must also return the
     * same hash code for two objects, one of which has a field with a
     * value of -0.0 and the other of which has a cooresponding field
     * with a value of 0.0.
     *
     * @param d an input double precision floating-point number
     * @return the integer bits representing that floating-point
     * number, after first mapping -0.0f to 0.0f
     */
    public static long doubleToLongBits(double d) {
	// Check for +0 or -0
	if (d == 0.0) {
	    return 0L;
	}
	else {
	    return Double.doubleToLongBits(d);
	}
    }


    /**
     * Do not construct an instance of this class.
     */
    private HashCodeUtil() {
    }
}
