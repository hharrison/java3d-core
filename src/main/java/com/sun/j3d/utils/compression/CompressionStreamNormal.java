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

import javax.vecmath.Vector3f;

/**
 * This class represents a normal in a compression stream. It maintains both
 * floating-point and quantized representations.  This normal may be bundled
 * with a vertex or exist separately as a global normal.
 */
class CompressionStreamNormal extends CompressionStreamElement {
    private int u, v ;
    private int specialOctant, specialSextant ;
    private float normalX, normalY, normalZ ;

    int octant, sextant ;
    boolean specialNormal ;
    int uAbsolute, vAbsolute ;
	    
    /**
     * Create a CompressionStreamNormal.
     *
     * @param stream CompressionStream associated with this element
     * @param normal floating-point representation to be encoded
     */
    CompressionStreamNormal(CompressionStream stream, Vector3f normal) {
	this.normalX = normal.x ;
	this.normalY = normal.y ;
	this.normalZ = normal.z ;
	stream.byteCount += 12 ;
    }

    //
    // Normal Encoding Parameterization
    // 
    // A floating point normal is quantized to a desired number of bits by
    // comparing it to candidate entries in a table of every possible normal
    // at that quantization and finding the closest match.  This table of
    // normals is indexed by the following encoding:
    //
    // First, points on a unit radius sphere are parameterized by two angles,
    // th and psi, using usual spherical coordinates. th is the angle about
    // the y axis, psi is the inclination to the plane containing the point.
    // The mapping between rectangular and spherical coordinates is:
    // 
    // x = cos(th)*cos(psi)
    // y = sin(psi)
    // z = sin(th)*cos(psi)
    // 
    // Points on sphere are folded first by octant, and then by sort order
    // of xyz into one of six sextants. All the table encoding takes place in
    // the positive octant, in the region bounded by the half spaces:
    // 
    // x >= z
    // z >= y
    // y >= 0
    // 
    // This triangular shaped patch runs from 0 to 45 degrees in th, and
    // from 0 to as much as 0.615479709 (MAX_Y_ANG) in psi. The xyz bounds
    // of the patch is:
    // 
    // (1, 0, 0)  (1/sqrt(2), 0, 1/sqrt(2))  (1/sqrt(3), 1/sqrt(3), 1/sqrt(3))
    // 
    // When dicing this space up into discrete points, the choice for y is
    // linear quantization in psi.  This means that if the y range is to be
    // divided up into n segments, the angle of segment j is:
    // 
    // psi(j) = MAX_Y_ANG*(j/n)
    // 
    // The y height of the patch (in arc length) is *not* the same as the xz
    // dimension. However, the subdivision quantization needs to treat xz and
    // y equally. To achieve this, the th angles are re-parameterized as
    // reflected psi angles.  That is, the i-th point's th is:
    // 
    // th(i) = asin(tan(psi(i))) = asin(tan(MAX_Y_ANG*(i/n)))
    // 
    // To go the other direction, the angle th corresponds to the real index r
    // (in the same 0-n range as i):
    // 
    // r(th) = n*atan(sin(th))/MAX_Y_ANG
    // 
    // Rounded to the nearest integer, this gives the closest integer index i
    // to the xz angle th. Because the triangle has a straight edge on the
    // line x=z, it is more intuitive to index the xz angles in reverse
    // order.  Thus the two equations above are replaced by:
    // 
    // th(i) = asin(tan(psi(i))) = asin(tan(MAX_Y_ANG*((n-i)/n)))
    // 
    // r(th) = n*(1 - atan(sin(th))/MAX_Y_ANG)
    // 
    // Each level of quantization subdivides the triangular patch twice as
    // densely.  The case in which only the three vertices of the triangle are
    // present is the first logical stage of representation, but because of
    // how the table is encoded the first usable case starts one level of
    // sub-division later.  This three point level has an n of 2 by the above
    // conventions.
    //
    private static final int MAX_UV_BITS = 6 ;
    private static final int MAX_UV_ENTRIES = 64 ;

    private static final double cgNormals[][][][] =
	new double[MAX_UV_BITS+1][MAX_UV_ENTRIES+1][MAX_UV_ENTRIES+1][3] ;

    private static final double MAX_Y_ANG = 0.615479709 ;
    private static final double UNITY_14 = 16384.0 ;

    private static void computeNormals() {
	int inx, iny, inz, n ;
	double th, psi, qnx, qny, qnz ;

	for (int quant = 0 ; quant <= MAX_UV_BITS ; quant++) {
	    n = 1 << quant ;

	    for (int j = 0 ; j <= n ; j++) {
		for (int i = 0 ; i <= n ; i++) {
		    if (i+j > n) continue ;

		    psi = MAX_Y_ANG*(j/((double) n)) ;
		    th = Math.asin(Math.tan(MAX_Y_ANG*((n-i)/((double) n)))) ;

		    qnx = Math.cos(th)*Math.cos(psi) ;
		    qny = Math.sin(psi) ;
		    qnz = Math.sin(th)*Math.cos(psi) ;

		    // The normal table uses 16-bit components and must be
		    // able to represent both +1.0 and -1.0, so convert the
		    // floating point normal components to fixed point with 14
		    // fractional bits, a unity bit, and a sign bit (s1.14).
		    // Set them back to get the float equivalent.
		    qnx = qnx*UNITY_14 ; inx = (int)qnx ;
		    qnx = inx ; qnx = qnx/UNITY_14 ;

		    qny = qny*UNITY_14 ; iny = (int)qny ;
		    qny = iny ; qny = qny/UNITY_14 ;

		    qnz = qnz*UNITY_14 ; inz = (int)qnz ;
		    qnz = inz ; qnz = qnz/UNITY_14 ;

		    cgNormals[quant][j][i][0] = qnx ;
		    cgNormals[quant][j][i][1] = qny ;
		    cgNormals[quant][j][i][2] = qnz ;
		}
	    }
	}
    }

    //
    // An inverse sine table is used for each quantization level to take the Y
    // component of a normal (which is the sine of the inclination angle) and
    // obtain the closest quantized Y angle.
    // 
    // At any level of compression, there are a fixed number of different Y
    // angles (between 0 and MAX_Y_ANG).  The inverse table is built to have
    // slightly more than twice as many entries as y angles at any particular
    // level; this ensures that the inverse look-up will get within one angle
    // of the right one.  The size of the table should be as small as
    // possible, but with its delta sine still smaller than the delta sine
    // between the last two angles to be encoded.
    // 
    // Example: the inverse sine table has a maximum angle of 0.615479709.  At
    // the maximum resolution of 6 bits there are 65 discrete angles used,
    // but twice as many are needed for thresholding between angles, so the
    // delta angle is 0.615479709/128. The difference then between the last
    // two angles to be encoded is:
    // sin(0.615479709*128.0/128.0) - sin(0.615479709*127.0/128.0) = 0.003932730
    // 
    // Using 8 significent bits below the binary point, fixed point can
    // represent sines in increments of 0.003906250, just slightly smaller.
    // However, because the maximum Y angle sine is 0.577350269, only 148
    // instead of 256 table entries are needed.
    // 
    private static final short inverseSine[][] = new short[MAX_UV_BITS+1][] ;

    // UNITY_14 * sin(MAX_Y_ANGLE)
    private static final short MAX_SIN_14BIT = 9459 ;

    private static void computeInverseSineTables() {
	int intSin, deltaSin, intAngle ;
	double floatSin, floatAngle ;
	short sin14[] = new short[MAX_UV_ENTRIES+1] ;

	// Build table of sines in s1.14 fixed point for each of the
	// discrete angles used at maximum resolution.
	for (int i = 0 ; i <= MAX_UV_ENTRIES ; i++) {
	    sin14[i] = (short)(UNITY_14*Math.sin(i*MAX_Y_ANG/MAX_UV_ENTRIES)) ;
	}

	for (int quant = 0 ; quant <= MAX_UV_BITS ; quant++) {
	    switch (quant) {
	    default:
	    case 6:
		// Delta angle: MAX_Y_ANGLE/128.0
		// Bits below binary point for fixed point delta sine: 8
		// Integer delta sine: 64
		// Inverse sine table size: 148 entries
		deltaSin = 1 << (14 - 8) ; 
		break ;
	    case 5:
		// Delta angle: MAX_Y_ANGLE/64.0
		// Bits below binary point for fixed point delta sine: 7
		// Integer delta sine: 128
		// Inverse sine table size: 74 entries
		deltaSin = 1 << (14 - 7) ; 
		break ;
	    case 4:
		// Delta angle: MAX_Y_ANGLE/32.0
		// Bits below binary point for fixed point delta sine: 6
		// Integer delta sine: 256
		// Inverse sine table size: 37 entries
		deltaSin = 1 << (14 - 6) ; 
		break ;
	    case 3:
		// Delta angle: MAX_Y_ANGLE/16.0
		// Bits below binary point for fixed point delta sine: 5
		// Integer delta sine: 512
		// Inverse sine table size: 19 entries
		deltaSin = 1 << (14 - 5) ; 
		break ;
	    case 2:
		// Delta angle: MAX_Y_ANGLE/8.0
		// Bits below binary point for fixed point delta sine: 4
		// Integer delta sine: 1024
		// Inverse sine table size: 10 entries
		deltaSin = 1 << (14 - 4) ; 
		break ;
	    case 1:
		// Delta angle: MAX_Y_ANGLE/4.0
		// Bits below binary point for fixed point delta sine: 3
		// Integer delta sine: 2048
		// Inverse sine table size: 5 entries
		deltaSin = 1 << (14 - 3) ; 
		break ;
	    case 0:
		// Delta angle: MAX_Y_ANGLE/2.0
		// Bits below binary point for fixed point delta sine: 2
		// Integer delta sine: 4096
		// Inverse sine table size: 3 entries
		deltaSin = 1 << (14 - 2) ; 
		break ;
	    }

	    inverseSine[quant] = new short[(MAX_SIN_14BIT/deltaSin) + 1] ;
	    
	    intSin = 0 ;
	    for (int i = 0 ; i < inverseSine[quant].length ; i++) {
		// Compute float representation of integer sine with desired
		// number of fractional bits by effectively right shifting 14.
		floatSin = intSin/UNITY_14 ;

		// Compute the angle with this sine value and quantize it.
		floatAngle = Math.asin(floatSin) ;
		intAngle = (int)((floatAngle/MAX_Y_ANG) * (1 << quant)) ;

		// Choose the closest of the three nearest quantized values
		// intAngle-1, intAngle, and intAngle+1.
		if (intAngle > 0) {
		    if (Math.abs(sin14[intAngle << (6-quant)] - intSin) >
			Math.abs(sin14[(intAngle-1) << (6-quant)] - intSin))
			intAngle = intAngle-1 ;
		}

		if (intAngle < (1 << quant)) {
		    if (Math.abs(sin14[intAngle << (6-quant)] - intSin) >
			Math.abs(sin14[(intAngle+1) << (6-quant)] - intSin))
			intAngle = intAngle+1 ;
		}

		inverseSine[quant][i] = (short)intAngle ;
		intSin += deltaSin ;
	    }
	}
    }

    /**
     * Compute static tables needed for normal quantization.
     */
    static {
	computeNormals() ;
	computeInverseSineTables() ;
    }

    /**
     * Quantize the floating point normal to a 6-bit octant/sextant plus u,v
     * components of [0..6] bits.  Full resolution is 18 bits and the minimum
     * is 6 bits.
     *
     * @param stream CompressionStream associated with this element
     * @param table HuffmanTable for collecting data about the quantized
     * representation of this element
     */
    void quantize(CompressionStream stream, HuffmanTable huffmanTable) {
	double nx, ny, nz, t ;

	// Clamp UV quantization.
	int quant = 
	    (stream.normalQuant < 0? 0 :
	     (stream.normalQuant > 6? 6 : stream.normalQuant)) ;

	nx = normalX ;
	ny = normalY ;
	nz = normalZ ;

	octant = 0 ;
	sextant = 0 ;
	u = 0 ;
	v = 0 ;

	// Normalize the fixed point normal to the positive signed octant.
	if (nx < 0.0) {
	    octant |= 4 ;
	    nx = -nx ;
	}
	if (ny < 0.0) {
	    octant |= 2 ;
	    ny = -ny ;
	}
	if (nz < 0.0) {
	    octant |= 1 ;
	    nz = -nz ;
	}

	// Normalize the fixed point normal to the proper sextant of the octant.
	if (nx < ny) {
	    sextant |= 1 ;
	    t = nx ;
	    nx = ny ;
	    ny = t ;
	}
	if (nz < ny) {
	    sextant |= 2 ;
	    t = ny ;
	    ny = nz ;
	    nz = t ;
	}
	if (nx < nz) {
	    sextant |= 4 ;
	    t = nx ;
	    nx = nz ;
	    nz = t ;
	}

	// Convert the floating point y component to s1.14 fixed point.
	int yInt = (int)(ny * UNITY_14) ;

	// The y component of the normal is the sine of the y angle.  Quantize
	// the y angle by using the fixed point y component as an index into
	// the inverse sine table of the correct size for the quantization
	// level.  (12 - quant) bits of the s1.14 y normal component are
	// rolled off with a right shift; the remaining bits then match the
	// number of bits used to represent the delta sine of the table.
	int yIndex = inverseSine[quant][yInt >> (12-quant)] ;

	// Search the two xz rows near y for the best match.
	int ii = 0 ;
	int jj = 0 ;
	int n = 1 << quant ;
	double dot, bestDot = -1 ;

	for (int j = yIndex-1 ; j < yIndex+1 && j <= n ; j++) {
	    if (j < 0)
		continue ;

	    for (int i = 0 ; i <= n ; i++) {
		if (i+j > n)
		    continue ;

		dot = nx * cgNormals[quant][j][i][0] +
		      ny * cgNormals[quant][j][i][1] +
		      nz * cgNormals[quant][j][i][2] ;

		if (dot > bestDot) {
		    bestDot = dot ;
		    ii = i ;
		    jj = j ;
		}
	    }
	}

	// Convert u and v to standard grid form.
	u = ii << (6 - quant) ;
	v = jj << (6 - quant) ;

	// Check for special normals and specially encode them.
	specialNormal = false ;
	if (u == 64 && v ==  0) {
	    // six coordinate axes case
	    if (sextant == 0 || sextant == 2) {
		// +/- x-axis
		specialSextant = 0x6 ;
		specialOctant = ((octant & 4) != 0)? 0x2 : 0 ;

	    } else if (sextant == 3 || sextant == 1) {
		// +/- y-axis
		specialSextant = 0x6 ;
		specialOctant = 4 | (((octant & 2) != 0)? 0x2 : 0) ;

	    } else if (sextant == 5 || sextant == 4) {
		// +/- z-axis
		specialSextant = 0x7 ;
		specialOctant = ((octant & 1) != 0)? 0x2 : 0 ;
	    }
	    specialNormal = true ;
	    u = v = 0 ;

	} else if (u ==  0 && v == 64) {
	    // eight mid point case
	    specialSextant = 6 | (octant >> 2) ;
	    specialOctant = ((octant & 0x3) << 1) | 1 ;
	    specialNormal = true ;
	    u = v = 0 ;
	}
	
	// Compute deltas if possible.
	// Use the non-normalized ii and jj indices.
	int du = 0 ;
	int dv = 0 ;
	int uv64 = 64 >> (6 - quant) ;

	absolute = false ;
	if (stream.firstNormal || stream.normalQuantChanged || 
	    stream.lastSpecialNormal || specialNormal) {
	    // The first normal by definition is absolute, and normals cannot
	    // be represented as deltas to or from special normals, nor from
	    // normals with a different quantization.
	    absolute = true ;
	    stream.firstNormal = false ;
	    stream.normalQuantChanged = false ;

	} else if (stream.lastOctant == octant &&
		   stream.lastSextant == sextant) {
	    // Deltas are always allowed within the same sextant/octant.
	    du = ii - stream.lastU ;
	    dv = jj - stream.lastV ;

	} else if (stream.lastOctant != octant &&
		   stream.lastSextant == sextant &&
		   (((sextant == 1 || sextant == 5) &&
		     (stream.lastOctant & 3) == (octant & 3)) ||
		    ((sextant == 0 || sextant == 4) &&
		     (stream.lastOctant & 5) == (octant & 5)) ||
		    ((sextant == 2 || sextant == 3) &&
		     (stream.lastOctant & 6) == (octant & 6)))) {
	    // If the sextants are the same, the octants can differ only when
	    // they are bordering each other on the same edge that the
	    // sextant has.
	    du =  ii - stream.lastU ;
	    dv = -jj - stream.lastV ;
	    
	    // Can't delta by less than -64.
	    if (dv < -uv64) absolute = true ;

	    // Can't delta doubly defined points.
	    if (jj == 0) absolute = true ;

	} else if (stream.lastOctant == octant &&
		   stream.lastSextant != sextant &&
		   ((sextant == 0 && stream.lastSextant == 4) ||
		    (sextant == 4 && stream.lastSextant == 0) ||
		    (sextant == 1 && stream.lastSextant == 5) ||
		    (sextant == 5 && stream.lastSextant == 1) ||
		    (sextant == 2 && stream.lastSextant == 3) ||
		    (sextant == 3 && stream.lastSextant == 2))) {
	    // If the octants are the same, the sextants must border on
	    // the i side (this case) or the j side (next case).
	    du = -ii - stream.lastU ;
	    dv =  jj - stream.lastV ;

	    // Can't delta by less than -64.
	    if (du < -uv64) absolute = true ;

	    // Can't delta doubly defined points.
	    if (ii == 0) absolute = true ;

	} else if (stream.lastOctant == octant &&
		   stream.lastSextant != sextant &&
		   ((sextant == 0 && stream.lastSextant == 2) ||
		    (sextant == 2 && stream.lastSextant == 0) ||
		    (sextant == 1 && stream.lastSextant == 3) ||
		    (sextant == 3 && stream.lastSextant == 1) ||
		    (sextant == 4 && stream.lastSextant == 5) ||
		    (sextant == 5 && stream.lastSextant == 4))) {
	    // If the octants are the same, the sextants must border on
	    // the j side (this case) or the i side (previous case).
	    if (((ii + jj ) != uv64) && (ii != 0) && (jj != 0)) {
		du = uv64 - ii - stream.lastU ;
		dv = uv64 - jj - stream.lastV ;

		// Can't delta by greater than +63.
		if ((du >= uv64) || (dv >= uv64))
		    absolute = true ;
	    } else
		// Can't delta doubly defined points.
		absolute = true ;

	} else
	    // Can't delta this normal.
	    absolute = true ;

	if (absolute == false) {
	    // Convert du and dv to standard grid form.
	    u = du << (6 - quant) ;
	    v = dv << (6 - quant) ;
	}

	// Compute length and shift common to all components.
	computeLengthShift(u, v) ;

	if (absolute && length > 6) {
	    // Absolute normal u, v components are unsigned 6-bit integers, so
	    // truncate the 0 sign bit for values > 0x001f.
	    length = 6 ;
	}
	
	// Add this element to the Huffman table associated with this stream.
	huffmanTable.addNormalEntry(length, shift, absolute) ;

	// Save current normal as last.
	stream.lastSextant = sextant ;
	stream.lastOctant = octant ;
	stream.lastU = ii ;
	stream.lastV = jj ;
	stream.lastSpecialNormal = specialNormal ;

	// Copy and retain absolute normal for mesh buffer lookup.
	uAbsolute = ii ;
	vAbsolute = jj ;
    }

    /**
     * Output a setNormal command.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputCommand(HuffmanTable table, CommandStream output) {
	outputNormal(table, output, CommandStream.SET_NORM, 8) ;
    }

    /**
     * Output a normal subcommand.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputSubcommand(HuffmanTable table, CommandStream output) {
	outputNormal(table, output, 0, 6) ;
    }

    //
    // Output the final compressed bits to the output command stream.
    //
    private void outputNormal(HuffmanTable table, CommandStream output,
			      int header, int headerLength) {

 	HuffmanNode t ;

	// Look up the Huffman token for this compression stream element.
	t = table.getNormalEntry(length, shift, absolute) ;

	// Construct the normal subcommand.
	int componentLength = t.dataLength - t.shift ;
	int subcommandLength = 0 ;
	long normalSubcommand = 0 ;

	if (absolute) {
	    // A 3-bit sextant and a 3-bit octant are always present.
	    subcommandLength = t.tagLength + 6 ;

	    if (specialNormal)
		// Use the specially-encoded sextant and octant.
		normalSubcommand =
		    (t.tag << 6) | (specialSextant << 3) | specialOctant ;
	    else
		// Use the general encoding rule.
		normalSubcommand =
		    (t.tag << 6) | (sextant << 3) | octant ;
	} else {
	    // The tag is immediately followed by the u and v delta components.
	    subcommandLength = t.tagLength ;
	    normalSubcommand = t.tag ;
	}

	// Add the u and v values to the subcommand.
	subcommandLength += (2 * componentLength) ;

	u = (u >> t.shift) & (int)lengthMask[componentLength] ;
	v = (v >> t.shift) & (int)lengthMask[componentLength] ;

	normalSubcommand =
	    (normalSubcommand << (2 * componentLength)) |
	    (u                << (1 * componentLength)) |
	    (v                << (0 * componentLength)) ;

	if (subcommandLength < 6) {
	    // The header will have some empty bits. The Huffman tag
	    // computation will prevent this if necessary.
	    header |= (int)(normalSubcommand << (6 - subcommandLength)) ;
	    subcommandLength = 0 ;
	}
	else {
	    // Move the 1st 6 bits of the subcommand into the header.
	    header |= (int)(normalSubcommand >>> (subcommandLength - 6)) ;
	    subcommandLength -= 6 ;
	}
	
	// Add the header and body to the output buffer.
	output.addCommand(header, headerLength,
			  normalSubcommand, subcommandLength)  ;
    }

    public String toString() {
	String fixed ;

	if (specialNormal)
	    fixed = " special normal, sextant " + specialSextant +
		    " octant " + specialOctant ;

	else if (absolute)
	    fixed = " sextant " + sextant + " octant " + octant +
		    " u " + u + " v " + v ;
	else
	    fixed = " du " + u + " dv " + v ;

	return
	    "normal: " + normalX + " " + normalY + " " + normalZ + "\n"
	    + fixed + "\n" + " length " + length + " shift " + shift +
	    (absolute? " absolute" : " relative") ;
    }
}
