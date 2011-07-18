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

package com.sun.j3d.utils.compression;

/**
 * Instances of this class are used as elements in a CompressionStream.
 * @see CompressionStream
 */
abstract class CompressionStreamElement {
    /**
     * Bit length of quantized geometric components.
     */
    int length ;

    /**
     * Number of trailing zeros in quantized geometric components.
     */
    int shift ;

    /**
     * If false, geometric component values are represented as differences
     * from those of the preceding element in the stream.
     */
    boolean absolute ;

    /**
     * Array with elements that can be used as masks to apply a quantization
     * to the number of bits indicated by the referencing index [0..16].
     */
    static final int quantizationMask[] = {
	0xFFFF0000, 0xFFFF8000, 0xFFFFC000, 0xFFFFE000,
	0xFFFFF000, 0xFFFFF800, 0xFFFFFC00, 0xFFFFFE00,
	0xFFFFFF00, 0xFFFFFF80, 0xFFFFFFC0, 0xFFFFFFE0,
	0xFFFFFFF0, 0xFFFFFFF8, 0xFFFFFFFC, 0xFFFFFFFE,
	0xFFFFFFFF
    } ;
    
    /**
     * Array with elements that can be used as masks to retain the number of
     * trailing bits of data indicated by the referencing index [0..64].  Used
     * to clear the leading sign bits of fixed-point 2's complement numbers
     * and in building the compressed output stream.
     */
    static final long lengthMask[] = {
	0x0000000000000000L, 0x0000000000000001L,
	0x0000000000000003L, 0x0000000000000007L,
	0x000000000000000FL, 0x000000000000001FL,
	0x000000000000003FL, 0x000000000000007FL,
	0x00000000000000FFL, 0x00000000000001FFL,
	0x00000000000003FFL, 0x00000000000007FFL,
	0x0000000000000FFFL, 0x0000000000001FFFL,
	0x0000000000003FFFL, 0x0000000000007FFFL,
	0x000000000000FFFFL, 0x000000000001FFFFL,
	0x000000000003FFFFL, 0x000000000007FFFFL,
	0x00000000000FFFFFL, 0x00000000001FFFFFL,
	0x00000000003FFFFFL, 0x00000000007FFFFFL,
	0x0000000000FFFFFFL, 0x0000000001FFFFFFL,
	0x0000000003FFFFFFL, 0x0000000007FFFFFFL,
	0x000000000FFFFFFFL, 0x000000001FFFFFFFL,
	0x000000003FFFFFFFL, 0x000000007FFFFFFFL,
	0x00000000FFFFFFFFL, 0x00000001FFFFFFFFL,
	0x00000003FFFFFFFFL, 0x00000007FFFFFFFFL,
	0x0000000FFFFFFFFFL, 0x0000001FFFFFFFFFL,
	0x0000003FFFFFFFFFL, 0x0000007FFFFFFFFFL,
	0x000000FFFFFFFFFFL, 0x000001FFFFFFFFFFL,
	0x000003FFFFFFFFFFL, 0x000007FFFFFFFFFFL,
	0x00000FFFFFFFFFFFL, 0x00001FFFFFFFFFFFL,
	0x00003FFFFFFFFFFFL, 0x00007FFFFFFFFFFFL,
	0x0000FFFFFFFFFFFFL, 0x0001FFFFFFFFFFFFL,
	0x0003FFFFFFFFFFFFL, 0x0007FFFFFFFFFFFFL,
	0x000FFFFFFFFFFFFFL, 0x001FFFFFFFFFFFFFL,
	0x003FFFFFFFFFFFFFL, 0x007FFFFFFFFFFFFFL,
	0x00FFFFFFFFFFFFFFL, 0x01FFFFFFFFFFFFFFL,
	0x03FFFFFFFFFFFFFFL, 0x07FFFFFFFFFFFFFFL,
	0x0FFFFFFFFFFFFFFFL, 0x1FFFFFFFFFFFFFFFL,
	0x3FFFFFFFFFFFFFFFL, 0x7FFFFFFFFFFFFFFFL,
	0xFFFFFFFFFFFFFFFFL
    } ;


    /**
     * Computes the quantized representation of this stream element.
     *
     * @param stream CompressionStream associated with this element
     * @param table HuffmanTable for collecting data about the quantized
     * representation of this element
     */
    abstract void quantize(CompressionStream stream, HuffmanTable table) ;

    /**
     * Outputs the compressed bits representing this stream element.
     * Some instances of CompressionStreamElement don't require an
     * implementation and will inherit the stub provided here.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputCommand(HuffmanTable table, CommandStream output) {
    }

    /**
     * Finds the minimum bits needed to represent the given 16-bit signed 2's
     * complement integer.  For positive integers, this include the first
     * 1 starting from the left, plus a 0 sign bit; for negative integers,
     * this includes the first 0 starting from the left, plus a 1 sign bit.
     * 0 is a special case returning 0; however, 0-length components are valid
     * ONLY for normals.
     * 
     * The decompressor uses the data length to determine how many bits of
     * sign extension to add to the data coming in from the compressed stream
     * in order to create a 16-bit signed 2's complement integer.  E.g., a data
     * length of 12 indicates that 16-12=4 bits of sign are to be extended.<p>
     *
     * @param number a signed 2's complement integer representable in 16 bits
     * or less
     * @return minimum number of bits to represent the number
     */
    private static final int getLength(int number) {
	if (number == 0)
	    return 0 ;

	else if ((number & 0x8000) > 0) {
	    // negative numbers
	    if ((number & 0x4000) == 0) return 16 ;
	    if ((number & 0x2000) == 0) return 15 ;
	    if ((number & 0x1000) == 0) return 14 ;
	    if ((number & 0x0800) == 0) return 13 ;
	    if ((number & 0x0400) == 0) return 12 ;
	    if ((number & 0x0200) == 0) return 11 ;
	    if ((number & 0x0100) == 0) return 10 ;
	    if ((number & 0x0080) == 0) return  9 ;
	    if ((number & 0x0040) == 0) return  8 ;
	    if ((number & 0x0020) == 0) return  7 ;
	    if ((number & 0x0010) == 0) return  6 ;
	    if ((number & 0x0008) == 0) return  5 ;
	    if ((number & 0x0004) == 0) return  4 ;
	    if ((number & 0x0002) == 0) return  3 ;
	    if ((number & 0x0001) == 0) return  2 ;

	    return 1 ;

	} else {
	    // positive numbers
	    if ((number & 0x4000) > 0) return 16 ;
	    if ((number & 0x2000) > 0) return 15 ;
	    if ((number & 0x1000) > 0) return 14 ;
	    if ((number & 0x0800) > 0) return 13 ;
	    if ((number & 0x0400) > 0) return 12 ;
	    if ((number & 0x0200) > 0) return 11 ;
	    if ((number & 0x0100) > 0) return 10 ;
	    if ((number & 0x0080) > 0) return  9 ;
	    if ((number & 0x0040) > 0) return  8 ;
	    if ((number & 0x0020) > 0) return  7 ;
	    if ((number & 0x0010) > 0) return  6 ;
	    if ((number & 0x0008) > 0) return  5 ;
	    if ((number & 0x0004) > 0) return  4 ;
	    if ((number & 0x0002) > 0) return  3 ;

	    return 2 ;
	}
    }

    /**
     * Finds the rightmost 1 bit in the given 16-bit integer.  This value is
     * used by the decompressor to indicate the number of trailing zeros to be
     * added to the end of the data coming in from the compressed stream,
     * accomplished by left shifting the data by the indicated amount.
     * 0 is a special case returning 0.<p>
     *
     * @param number an integer representable in 16 bits or less
     * @return number of trailing zeros
     */
    private static final int getShift(int number) {
	if (number == 0) return 0 ;

	if ((number & 0x0001) > 0) return  0 ;
	if ((number & 0x0002) > 0) return  1 ;
	if ((number & 0x0004) > 0) return  2 ;
	if ((number & 0x0008) > 0) return  3 ;
	if ((number & 0x0010) > 0) return  4 ;
	if ((number & 0x0020) > 0) return  5 ;
	if ((number & 0x0040) > 0) return  6 ;
	if ((number & 0x0080) > 0) return  7 ;
	if ((number & 0x0100) > 0) return  8 ;
	if ((number & 0x0200) > 0) return  9 ;
	if ((number & 0x0400) > 0) return 10 ;
	if ((number & 0x0800) > 0) return 11 ;
	if ((number & 0x1000) > 0) return 12 ;
	if ((number & 0x2000) > 0) return 13 ;
	if ((number & 0x4000) > 0) return 14 ;

	return 15 ;
    }

    /**
     * Computes common length and shift of 2 numbers.
     */
    final void computeLengthShift(int n0, int n1) {
	int s0 = n0 & 0x8000 ;
	int s1 = n1 & 0x8000 ;

	// equal sign optimization
	if (s0 == s1) 
	    if (s0 == 0)
		this.length = getLength(n0 | n1) ;
	    else
		this.length = getLength(n0 & n1) ;
	else
	    this.length = getMaximum(getLength(n0), getLength(n1)) ;

	this.shift = getShift(n0 | n1) ;
    }
    

    /**
     * Computes common length and shift of 3 numbers.
     */
    final void computeLengthShift(int n0, int n1, int n2) {
	int s0 = n0 & 0x8000 ;
	int s1 = n1 & 0x8000 ;
	int s2 = n2 & 0x8000 ;

	// equal sign optimization
	if (s0 == s1)
	    if (s1 == s2)
		if (s2 == 0)
		    this.length = getLength(n0 | n1 | n2) ;
		else
		    this.length = getLength(n0 & n1 & n2) ;
	    else
		if (s1 == 0)
		    this.length = getMaximum(getLength(n0 | n1),
					     getLength(n2)) ;
		else
		    this.length = getMaximum(getLength(n0 & n1),
					     getLength(n2)) ;
	else
	    if (s1 == s2)
		if (s2 == 0)
		    this.length = getMaximum(getLength(n1 | n2),
					     getLength(n0)) ;
		else
		    this.length = getMaximum(getLength(n1 & n2),
					     getLength(n0)) ;
	    else
		if (s0 == 0)
		    this.length = getMaximum(getLength(n0 | n2),
					     getLength(n1)) ;
		else
		    this.length = getMaximum(getLength(n0 & n2),
					     getLength(n1)) ;

	this.shift = getShift(n0 | n1 | n2) ;
    }
    

    /**
     * Computes common length and shift of 4 numbers.
     */
    final void computeLengthShift(int n0, int n1, int n2, int n3) {
	this.length = getMaximum(getLength(n0), getLength(n1),
				 getLength(n2), getLength(n3)) ;

	this.shift = getShift(n0 | n1 | n2 | n3) ;
    }
    

    /**
     * Finds the maximum of two integers.
     */
    private static final int getMaximum(int x, int y) {
	if (x > y)
	    return x ;
	else
	    return y ;
    }

    /**
     * Finds the maximum of three integers.
     */
    private static final int getMaximum(int x, int y, int z) {
	if (x > y)
	    if (x > z)
		return x ;
	    else
		return z ;
	else
	    if (y > z)
		return y ;
	    else
		return z ;
    }

    /**
     * Finds the maximum of four integers.
     */
    private static final int getMaximum(int x, int y, int z, int w) {
	int n0, n1 ;

	if (x > y)
	    n0 = x ;
	else
	    n0 = y ;

	if (z > w)
	    n1 = z ;
	else
	    n1 = w ;

	if (n0 > n1)
	    return n0 ;
	else
	    return n1 ;
    }
}

