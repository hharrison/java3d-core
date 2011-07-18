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

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

/**
 * This class represents a color in a compression stream. It maintains both
 * floating-point and quantized representations.  This color may be bundled
 * with a vertex or exist separately as a global color.
 */
class CompressionStreamColor extends CompressionStreamElement {
    private int R, G, B, A ;
    private boolean color3 ;
    private boolean color4 ;
    private float colorR, colorG, colorB, colorA ;
	    
    int rAbsolute, gAbsolute, bAbsolute, aAbsolute ;

    /**
     * Create a CompressionStreamColor.
     *
     * @param stream CompressionStream associated with this element
     * @param color3 floating-point representation to be encoded
     */
    CompressionStreamColor(CompressionStream stream, Color3f c3) {
	this.color4 = false ;
	this.color3 = true ;
	colorR = c3.x ;
	colorG = c3.y ;
	colorB = c3.z ;
	colorA = 0.0f ;
	stream.byteCount += 12 ;
    }

    /**
     * Create a CompressionStreamColor.
     *
     * @param stream CompressionStream associated with this element
     * @param color4 floating-point representation to be encoded
     */
    CompressionStreamColor(CompressionStream stream, Color4f c4) {
	this.color3 = false ;
	this.color4 = true ;
	colorR = c4.x ;
	colorG = c4.y ;
	colorB = c4.z ;
	colorA = c4.w ;
	stream.byteCount += 16 ;
    }

    /**
     * Quantize a floating point color to fixed point integer components of
     * the specified number of bits.  The bit length can range from a maximum
     * of 16 to a minimum of 2 bits since negative colors are not defined.<p>
     * 
     * The bit length is the total number of bits in the signed version of the
     * fixed point representation of the input color, which is assumed to
     * be normalized into the [0..1) range.  With the maximum bit length of
     * 16, 15 bits of positive colors can be represented; a bit length of 9 is
     * needed to get the 8 bit positive color size in common use.<p>
     *
     * @param stream CompressionStream associated with this element
     * @param table HuffmanTable for collecting data about the quantized
     * representation of this element
     */
    void quantize(CompressionStream stream, HuffmanTable huffmanTable) {
	// Clamp quantization.
	int quant = 
	    (stream.colorQuant < 2? 2 :
	     (stream.colorQuant > 16? 16 : stream.colorQuant)) ;

	absolute = false ;
	if (stream.firstColor || stream.colorQuantChanged) {
	    absolute = true ;
	    stream.lastColor[0] = 0 ;
	    stream.lastColor[1] = 0 ;
	    stream.lastColor[2] = 0 ;
	    stream.lastColor[3] = 0 ;
	    stream.firstColor = false ;
	    stream.colorQuantChanged = false ;
	}

	// Convert the floating point position to s.15 2's complement.
	if (color3) {
	    R = (int)(colorR * 32768.0) ;
	    G = (int)(colorG * 32768.0) ;
	    B = (int)(colorB * 32768.0) ;
	    A = 0 ;
	} else if (color4) {
	    R = (int)(colorR * 32768.0) ;
	    G = (int)(colorG * 32768.0) ;
	    B = (int)(colorB * 32768.0) ;
	    A = (int)(colorA * 32768.0) ;
	}

	// Clamp color components.
	R = (R > 32767? 32767: (R < 0? 0: R)) ;
	G = (G > 32767? 32767: (G < 0? 0: G)) ;
	B = (B > 32767? 32767: (B < 0? 0: B)) ;
	A = (A > 32767? 32767: (A < 0? 0: A)) ;
	    
	// Compute quantized values.
	R &= quantizationMask[quant] ;
	G &= quantizationMask[quant] ;
	B &= quantizationMask[quant] ;
	A &= quantizationMask[quant] ;

	// Copy and retain absolute color for mesh buffer lookup.
	rAbsolute = R ;
	gAbsolute = G ;
	bAbsolute = B ;
	aAbsolute = A ;

	// Compute deltas.
	R -= stream.lastColor[0] ;
	G -= stream.lastColor[1] ;
	B -= stream.lastColor[2] ;
	A -= stream.lastColor[3] ;

	// Update last values.
	stream.lastColor[0] += R ;
	stream.lastColor[1] += G ;
	stream.lastColor[2] += B ;
	stream.lastColor[3] += A ;

	// Compute length and shift common to all components.
	if (color3)
	    computeLengthShift(R, G, B) ;

	else if (color4)
	    computeLengthShift(R, G, B, A) ;
	
	// 0-length components are allowed only for normals.
	if (length == 0)
	    length = 1 ;
	
	// Add this element to the Huffman table associated with this stream.
	huffmanTable.addColorEntry(length, shift, absolute) ;
    }

    /**
     * Output a setColor command.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputCommand(HuffmanTable table, CommandStream output) {
	outputColor(table, output, CommandStream.SET_COLOR, 8) ;
    }

    /**
     * Output a color subcommand.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputSubcommand(HuffmanTable table, CommandStream output) {

	outputColor(table, output, 0, 6) ;
    }

    //
    // Output the final compressed bits to the output command stream.  
    //
    private void outputColor(HuffmanTable table, CommandStream output,
			     int header, int headerLength) {
 	HuffmanNode t ;

	// Look up the Huffman token for this compression stream element.
	t = table.getColorEntry(length, shift, absolute) ;

	// Construct the color subcommand components.  The maximum length of a
	// color subcommand is 70 bits (a tag with a length of 6 followed by 4
	// components of 16 bits each).  The subcommand is therefore
	// constructed initially using just the first 3 components, with the
	// 4th component added later after the tag has been shifted into the
	// subcommand header.
	int componentLength = t.dataLength - t.shift ;
	int subcommandLength = t.tagLength + (3 * componentLength) ;

	R = (R >> t.shift) & (int)lengthMask[componentLength] ;
	G = (G >> t.shift) & (int)lengthMask[componentLength] ;
	B = (B >> t.shift) & (int)lengthMask[componentLength] ;

	long colorSubcommand = 
	    (((long)t.tag) << (3 * componentLength)) |
	    (((long)R)     << (2 * componentLength)) |
	    (((long)G)     << (1 * componentLength)) |
	    (((long)B)     << (0 * componentLength)) ;

	if (subcommandLength < 6) {
	    // The header will have some empty bits.  The Huffman tag
	    // computation will prevent this if necessary.
	    header |= (int)(colorSubcommand << (6 - subcommandLength)) ;
	    subcommandLength = 0 ;
	}
	else {
	    // Move the 1st 6 bits of the subcommand into the header.
	    header |= (int)(colorSubcommand >>> (subcommandLength - 6)) ;
	    subcommandLength -= 6 ;
	}

	// Add alpha if present.
	if (color4) {
	    A = (A >> t.shift) & (int)lengthMask[componentLength] ;
	    colorSubcommand = (colorSubcommand << componentLength) | A ;
	    subcommandLength += componentLength ;
	}

	// Add the header and body to the output buffer.
	output.addCommand(header, headerLength,
			  colorSubcommand, subcommandLength)  ;
    }

    public String toString() {
	String d = absolute? "" : "delta " ;
	String c = (colorR + " " + colorG + " " + colorB +
		    (color4? (" " + colorA): "")) ;

	return
	    "color: " + c + "\n" +
	    " fixed point " + d + + R + " " + G + " " + B + "\n" +
	    " length " + length + " shift " + shift +
	    (absolute? " absolute" : " relative") ;
    }
}
