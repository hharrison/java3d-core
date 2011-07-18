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

package com.sun.j3d.utils.geometry.compression;

/**
 * This class is used to build the bit-level compression command stream which
 * is the final result of the compression process.  It defines the bit
 * representations of the compression commands and provides a mechanism for
 * the interleaving and forwarding of command headers and bodies required by
 * the geometry compression specification.
 */
class CommandStream {
    // Geometry compression commands.
    static final int SET_NORM     = 0xC0 ;
    static final int SET_COLOR    = 0x80 ;
    static final int VERTEX       = 0x40 ;
    static final int MESH_B_R     = 0x20 ;
    static final int SET_STATE    = 0x18 ;
    static final int SET_TABLE    = 0x10 ;
    static final int V_NO_OP      = 0x01 ;

    // Huffman table indices.
    static final int POSITION_TABLE = 0 ;
    static final int COLOR_TABLE    = 1 ;
    static final int NORMAL_TABLE   = 2 ;

    // The buffer of compressed data and the current offset.
    private byte bytes[] ;
    private int byteOffset ;
    private int bitOffset ;

    // Last command body for header forwarding.
    private long lastBody ;
    private int lastBodyLength ;

    /**
     * Create an empty CommandStream with a default initial size.
     */
    CommandStream() {
	this(65536) ;
    }

    /**
     * Create an empty CommandStream with the given initial size.
     *
     * @param initSize initial capacity of CommandStream in bytes
     */
    CommandStream(int initSize) {
	bytes = new byte[initSize] ;
	clear() ;
    }

    /**
     * Mark the CommandStream as empty so that its storage will be reused.
     */
    void clear() {
	// Initialize the first byte to 0.
	// Subsequent bytes are cleared as they are written.
	bytes[0] = 0 ;

	// Reset the number of valid bits.
	bitOffset = 0 ;
	byteOffset = 0 ;

	// The first command header is always followed by the body of an
	// implicit variable length no-op to start the header-forwarding
	// interleave required by hardware decompressor implementations.  The
	// only necessary bits are 5 bits of length set to zeros to indicate a
	// fill of zero length.
	lastBody = 0 ;
	lastBodyLength = 5 ;
    }

    /**
     * Add a compression command to this instance.<p>
     * 
     * A compression command includes an 8-bit header and can range up to 72
     * bits in length.  The command with the maximum length is a 2-bit color
     * command with a 6-bit tag in the header, followed by four 16-bit color
     * components of data.<p>
     * 
     * A subcommand is either a position, normal, or color, though in practice
     * a position subcommand can only be part of a vertex command.  Normal and
     * color subcommands can be parts of separate global normal and color
     * commands as well as parts of a vertex command.<p>
     * 
     * A subcommand includes a 6-bit header.  Its length is 2 bits less than
     * the length of the corresponding command.
     *
     * @param header contains compression command header bits, right-justified
     * within the bits of the int
     * @param headerLength number of bits in header, either 8 for commands or
     * 6 for subcommands
     * @param body contains the body of the compression command,
     * right-justified within the bits of the long
     * @param bodyLength number of bits in the body
     */
    void addCommand(int header, int headerLength, long body, int bodyLength) {
	addByte(header, headerLength) ;
	addLong(lastBody, lastBodyLength) ;

	lastBody = body ;
	lastBodyLength = bodyLength ;
    }

    //
    // Add the rightmost bitCount bits of b to the end of the command stream.
    // 
    private void addByte(int b, int bitCount) {
	int bitsEmpty = 8 - bitOffset ;
	b &= (int)CompressionStreamElement.lengthMask[bitCount] ;

	if (bitCount <= bitsEmpty) {
	    bytes[byteOffset] |= (b << (bitsEmpty - bitCount)) ;
	    bitOffset += bitCount ;
	    return ;
	}

	if (bytes.length == byteOffset + 1) {
	    byte newBytes[] = new byte[bytes.length * 2] ;
	    System.arraycopy(bytes, 0, newBytes, 0, bytes.length) ;
	    bytes = newBytes ;
	}

	bitOffset = bitCount - bitsEmpty ;
	bytes[byteOffset] |= (b >>> bitOffset) ;

	byteOffset++ ;
	bytes[byteOffset] = (byte)(b << (8 - bitOffset)) ;
    }

    //
    // Add the rightmost bitCount bits of l to the end of the command stream.
    // 
    private void addLong(long l, int bitCount) {
	int byteCount = bitCount / 8 ;
	int excessBits = bitCount - byteCount * 8 ;

	if (excessBits > 0)
	    addByte((int)(l >>> (byteCount * 8)), excessBits) ;
	
	while (byteCount > 0) {
	    addByte((int)((l >>> ((byteCount - 1) * 8)) & 0xff), 8) ;
	    byteCount-- ;
	}
    }

    /**
     * Add a no-op and the last command body.  Pad out with additional no-ops
     * to a 64-bit boundary if necessary.  A call to this method is required
     * in order to create a valid compression command stream.
     */
    void end() {
	int excessBytes, padBits ;

	// Add the 1st no-op and the last body.
	addByte(V_NO_OP, 8) ;
	addLong(lastBody, lastBodyLength) ;

	excessBytes = (byteOffset + 1) % 8 ;
	if (excessBytes == 0 && bitOffset == 8)
	    // No padding necessary.
	    return ;

	// Need to add padding with a 2nd no-op.
	addByte(V_NO_OP, 8) ;
	excessBytes = (byteOffset + 1) % 8 ;

	if (excessBytes == 0)
	    padBits = 8 - bitOffset ;
	else {
	    int fillBytes = 8 - excessBytes ;
	    padBits = (8 * fillBytes) + (8 - bitOffset) ;
	}
	
	// The minimum length for a no-op command body is 5 bits.
	if (padBits < 5)
	    // Have to cross the next 64-bit boundary.
	    padBits += 64 ;

	// The maximum length of a no-op body is a 5-bit length + 31 bits of
	// fill for a total of 36.
	if (padBits < 37) {
	    // Pad with the body of the 1st no-op.
	    addLong((padBits - 5) << (padBits - 5), padBits) ;
	    return ;
	}
	
	// The number of bits to pad at this point is [37..68].  Knock off 24
	// bits with the body of the 1st no-op to reduce the number of pad
	// bits to [13..44], which can be filled with 1 more no-op.
	addLong(19 << 19, 24) ;
	padBits -= 24 ;

	// Add a 3rd no-op.
	addByte(V_NO_OP, 8) ;
	padBits -= 8 ;

	// Complete padding with the body of the 2nd no-op.
	addLong((padBits - 5) << (padBits - 5), padBits) ;
    }

    /**
     * Get the number of bytes in the compression command stream.
     *
     * @return size of compressed data in bytes
     */
    int getByteCount() {
	if (byteOffset + bitOffset == 0)
	    return 0 ;
	else
	    return byteOffset + 1 ;
    }

    /**
     * Get the bytes composing the compression command stream.
     *
     * @return reference to array of bytes containing the compressed data
     */
    byte[] getBytes() {
	return bytes ;
    }
}
