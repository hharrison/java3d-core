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
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * This class represents a vertex in a compression stream.  It maintains both
 * floating-point and quantized representations of the vertex position along
 * with meshing and vertex replacement flags for line and surface
 * primitives. If normals or colors are bundled with geometry vertices then
 * instances of this class will also contain references to normal or color
 * stream elements.
 */
class CompressionStreamVertex extends CompressionStreamElement {
    private int X, Y, Z ;
    private int meshFlag ;
    private int stripFlag ;
    private float floatX, floatY, floatZ ;

    int xAbsolute, yAbsolute, zAbsolute ;
    CompressionStreamColor color = null ;
    CompressionStreamNormal normal = null ;
	    
    /**
     * Create a CompressionStreamVertex with the given parameters.
     *
     * @param stream CompressionStream associated with this vertex
     * @param p position
     * @param n normal bundled with this vertex or null if not bundled
     * @param c color bundled with this vertex or null if not bundled
     * @param stripFlag CompressionStream.RESTART,
     * CompressionStream.REPLACE_OLDEST, or CompressionStream.REPLACE_MIDDLE
     * @param meshFlag CompressionStream.MESH_PUSH or
     * CompressionStream.NO_MESH_PUSH
     */
    CompressionStreamVertex(CompressionStream stream,
			    Point3f p, Vector3f n, Color3f c,
			    int stripFlag, int meshFlag) {

	this(stream, p, n, stripFlag, meshFlag) ;

	if (stream.vertexColor3)
	    color = new CompressionStreamColor(stream, c) ;
    }

    /**
     * Create a CompressionStreamVertex with the given parameters.
     *
     * @param stream CompressionStream associated with this vertex
     * @param p position
     * @param n normal bundled with this vertex or null if not bundled
     * @param c color bundled with this vertex or null if not bundled
     * @param stripFlag CompressionStream.RESTART,
     * CompressionStream.REPLACE_OLDEST, or CompressionStream.REPLACE_MIDDLE
     * @param meshFlag CompressionStream.MESH_PUSH or
     * CompressionStream.NO_MESH_PUSH
     */
    CompressionStreamVertex(CompressionStream stream,
			    Point3f p, Vector3f n, Color4f c,
			    int stripFlag, int meshFlag) {

	this(stream, p, n, stripFlag, meshFlag) ;

	if (stream.vertexColor4)
	    color = new CompressionStreamColor(stream, c) ;
    }

    /**
     * Create a CompressionStreamVertex with the given parameters.
     *
     * @param stream CompressionStream associated with this vertex
     * @param p position
     * @param n normal bundled with this vertex or null if not bundled
     * @param stripFlag CompressionStream.RESTART,
     * CompressionStream.REPLACE_OLDEST, or CompressionStream.REPLACE_MIDDLE
     * @param meshFlag CompressionStream.MESH_PUSH or
     * CompressionStream.NO_MESH_PUSH
     */
    CompressionStreamVertex(CompressionStream stream, Point3f p, Vector3f n,
			    int stripFlag, int meshFlag) {

	this.stripFlag = stripFlag ;
	this.meshFlag = meshFlag ;
	this.floatX = p.x ;
	this.floatY = p.y ;
	this.floatZ = p.z ;

	stream.byteCount += 12 ;
	stream.vertexCount++ ;
		
	if (p.x < stream.mcBounds[0].x) stream.mcBounds[0].x = p.x ;
	if (p.y < stream.mcBounds[0].y) stream.mcBounds[0].y = p.y ;
	if (p.z < stream.mcBounds[0].z) stream.mcBounds[0].z = p.z ;

	if (p.x > stream.mcBounds[1].x) stream.mcBounds[1].x = p.x ;
	if (p.y > stream.mcBounds[1].y) stream.mcBounds[1].y = p.y ;
	if (p.z > stream.mcBounds[1].z) stream.mcBounds[1].z = p.z ;

	if (stream.vertexNormals)
	    normal = new CompressionStreamNormal(stream, n) ;
    }

    /**
     * Quantize the floating point position to fixed point integer components
     * of the specified number of bits.  The bit length can range from 1 to 16.
     *
     * @param stream CompressionStream associated with this element
     * @param table HuffmanTable for collecting data about the quantized
     * representation of this element
     */
    void quantize(CompressionStream stream, HuffmanTable huffmanTable) {
	double px, py, pz ;

	// Clamp quantization.
	int quant = 
	    (stream.positionQuant < 1? 1 :
	     (stream.positionQuant > 16? 16 : stream.positionQuant)) ;

	absolute = false ;
	if (stream.firstPosition || stream.positionQuantChanged) {
	    absolute = true ;
	    stream.lastPosition[0] = 0 ;
	    stream.lastPosition[1] = 0 ;
	    stream.lastPosition[2] = 0 ;
	    stream.firstPosition = false ;
	    stream.positionQuantChanged = false ;
	}

	// Normalize position to the unit cube.  This is bounded by the open
	// intervals (-1..1) on each axis.
	px = (floatX - stream.center[0]) * stream.scale ;
	py = (floatY - stream.center[1]) * stream.scale ;
	pz = (floatZ - stream.center[2]) * stream.scale ;

	// Convert the floating point position to s.15 2's complement.
	//  ~1.0 ->  32767 (0x00007fff) [ ~1.0 =  32767.0/32768.0]
	// ~-1.0 -> -32767 (0xffff8001) [~-1.0 = -32767.0/32768.0]
	X = (int)(px * 32768.0) ;
	Y = (int)(py * 32768.0) ;
	Z = (int)(pz * 32768.0) ;

	// Compute quantized values.
	X &= quantizationMask[quant] ;
	Y &= quantizationMask[quant] ;
	Z &= quantizationMask[quant] ;

	// Update quantized bounds.
	if (X < stream.qcBounds[0].x) stream.qcBounds[0].x = X ;
	if (Y < stream.qcBounds[0].y) stream.qcBounds[0].y = Y ;
	if (Z < stream.qcBounds[0].z) stream.qcBounds[0].z = Z ;

	if (X > stream.qcBounds[1].x) stream.qcBounds[1].x = X ;
	if (Y > stream.qcBounds[1].y) stream.qcBounds[1].y = Y ;
	if (Z > stream.qcBounds[1].z) stream.qcBounds[1].z = Z ;

	// Copy and retain absolute position for mesh buffer lookup.
	xAbsolute = X ;
	yAbsolute = Y ;
	zAbsolute = Z ;

	// Compute deltas.
	X -= stream.lastPosition[0] ;
	Y -= stream.lastPosition[1] ;
	Z -= stream.lastPosition[2] ;

	// Update last values.
	stream.lastPosition[0] += X ;
	stream.lastPosition[1] += Y ;
	stream.lastPosition[2] += Z ;

	// Deltas which exceed the range of 16-bit signed 2's complement
	// numbers are handled by sign-extension of the 16th bit in order to
	// effect a 16-bit wrap-around.
	X = (X << 16) >> 16 ;
	Y = (Y << 16) >> 16 ;
	Z = (Z << 16) >> 16 ;

	// Compute length and shift common to all components.
	computeLengthShift(X, Y, Z) ;

	// 0-length components are allowed only for normals.
	if (length == 0)
	    length = 1 ;
	
	// Add this element to the Huffman table associated with this stream.
	huffmanTable.addPositionEntry(length, shift, absolute) ;

	// Quantize any bundled color or normal.
	if (color != null)
	    color.quantize(stream, huffmanTable) ;

	if (normal != null)
	    normal.quantize(stream, huffmanTable) ;

	// Push this vertex into the mesh buffer mirror, if necessary, so it
	// can be retrieved for computing deltas when mesh buffer references
	// are subsequently encountered during the quantization pass.
	if (meshFlag == stream.MESH_PUSH)
	    stream.meshBuffer.push(this) ;
    }

    /**
     * Output the final compressed bits to the compression command stream.
     *
     * @param table HuffmanTable mapping quantized representations to
     * compressed encodings
     * @param output CommandStream for collecting compressed output
     */
    void outputCommand(HuffmanTable huffmanTable, CommandStream outputBuffer) {

	HuffmanNode t ;
	int command = CommandStream.VERTEX ;

	// Look up the Huffman token for this compression stream element.  The
	// values of length and shift found there will override the
	// corresponding fields in this element, which represent best-case
	// compression without regard to tag length.
	t = huffmanTable.getPositionEntry(length, shift, absolute) ;

	// Construct the position subcommand.
	int componentLength = t.dataLength - t.shift ;
	int subcommandLength = t.tagLength + (3 * componentLength) ;

	X = (X >> t.shift) & (int)lengthMask[componentLength] ;
	Y = (Y >> t.shift) & (int)lengthMask[componentLength] ;
	Z = (Z >> t.shift) & (int)lengthMask[componentLength] ;

	long positionSubcommand = 
	    (((long)t.tag) << (3 * componentLength)) |
	    (((long)X)     << (2 * componentLength)) |
	    (((long)Y)     << (1 * componentLength)) |
	    (((long)Z)     << (0 * componentLength)) ;
	    
	if (subcommandLength < 6) {
	    // The header will have some empty bits.  The Huffman tag
	    // computation will prevent this if necessary.
	    command |= (int)(positionSubcommand << (6 - subcommandLength)) ;
	    subcommandLength = 0 ;
	}
	else {
	    // Move the 1st 6 bits of the subcommand into the header.
	    command |= (int)(positionSubcommand >>> (subcommandLength - 6)) ;
	    subcommandLength -= 6 ;
	}

	// Construct the vertex command body.
	long body =
	    (((long)stripFlag) << (subcommandLength + 1)) |
	    (((long)meshFlag)  << (subcommandLength + 0)) |
	    (positionSubcommand & lengthMask[subcommandLength]) ;

	// Add the vertex command to the output buffer.
	outputBuffer.addCommand(command, 8, body, subcommandLength + 3) ;

	// Output any normal and color subcommands.
	if (normal != null)
	    normal.outputSubcommand(huffmanTable, outputBuffer) ;

	if (color != null)
	    color.outputSubcommand(huffmanTable, outputBuffer) ;
    }

    public String toString() {
	String d = absolute? "" : "delta " ;
	String c = (color  == null? "": "\n\n " + color.toString()) ;
	String n = (normal == null? "": "\n\n " + normal.toString()) ;

	return
	    "position: " + floatX + " " + floatY + " " + floatZ + "\n" +
	    "fixed point " + d + + X + " " + Y + " " + Z + "\n" +
	    "length " + length + " shift " + shift +
	    (absolute? " absolute" : " relative") + "\n" +
	    "strip flag " + stripFlag + " mesh flag " + meshFlag + 
	    c + n ;
    }
}
