/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;
import javax.vecmath.* ;

/**
 * This abstract class provides the base methods needed to create a geometry
 * decompressor.  Subclasses must implement a backend to handle the output,
 * consisting of a generalized triangle strip, line strip, or point array,
 * along with possible global color and normal changes.
 */
abstract class GeometryDecompressor {
    private static final boolean debug = false ;
    private static final boolean benchmark = false ;

    /**
     * Compressed geometry format version supported.
     */
    static final int majorVersionNumber = 1 ;
    static final int minorVersionNumber = 0 ;
    static final int minorMinorVersionNumber = 2 ;

    /**
     * This method is called when a SetState command is encountered in the
     * decompression stream.  
     *
     * @param bundlingNorm true indicates normals are bundled with vertices
     * @param bundlingColor true indicates colors are bundled with vertices
     * @param doingAlpha true indicates alpha values are bundled with vertices
     */
    abstract void outputVertexFormat(boolean bundlingNorm,
				     boolean bundlingColor,
				     boolean doingAlpha) ;

    /**
     * This method captures the vertex output of the decompressor.  The normal
     * or color references may be null if the corresponding data is not
     * bundled with the vertices in the compressed geometry buffer.  Alpha
     * values may be included in the color.  
     * 
     * @param position The coordinates of the vertex.
     * @param normal The normal bundled with the vertex.  May be null.
     * @param color The color bundled with the vertex.  May be null.  
     * Alpha may be present.
     * @param vertexReplaceCode Specifies the generalized strip flag
     * that is bundled with each vertex.
     * @see GeneralizedStripFlags
     * @see CompressedGeometryHeader
     */
    abstract void outputVertex(Point3f position, Vector3f normal,
			       Color4f color, int vertexReplaceCode) ;

    /**
     * This method captures the global color output of the decompressor.  It
     * is only invoked if colors are not bundled with the vertex data.  The
     * global color applies to all succeeding vertices until the next time the
     * method is invoked.
     *
     * @param color The current global color.
     */
    abstract void outputColor(Color4f color) ;

    /**
     * This method captures the global normal output of the decompressor.  It
     * is only invoked if normals are not bundled with the vertex data.  The
     * global normal applies to all succeeding vertices until the next time the
     * method is invoked.
     *
     * @param normal The current global normal.
     */
    abstract void outputNormal(Vector3f normal) ;

    // Geometry compression opcodes.
    private static final int GC_VERTEX       = 0x40 ;
    private static final int GC_SET_NORM     = 0xC0 ;
    private static final int GC_SET_COLOR    = 0x80 ;
    private static final int GC_MESH_B_R     = 0x20 ;
    private static final int GC_SET_STATE    = 0x18 ;
    private static final int GC_SET_TABLE    = 0x10 ;
    private static final int GC_PASS_THROUGH = 0x08 ;
    private static final int GC_EOS          = 0x00 ;
    private static final int GC_V_NO_OP      = 0x01 ;
    private static final int GC_SKIP_8       = 0x07 ;

    // Three 64-entry decompression tables are used: gctables[0] for
    // positions, gctables[1] for colors, and gctables[2] for normals.
    private HuffmanTableEntry gctables[][] ;

    /**
     * Decompression table entry.
     */
    static class HuffmanTableEntry {
	int tagLength, dataLength ;
	int rightShift, absolute ;

	public String toString() {
	    return
		" tag length: "  + tagLength  +
		" data length: " + dataLength +
		" shift: "       + rightShift +
		" abs/rel: "     + absolute ;
	}
    } 

    // A 16-entry mesh buffer is used.
    private MeshBufferEntry meshBuffer[] ;
    private int	meshIndex = 15 ;
    private int meshState ;

    // meshState values.  These are needed to determine if colors and/or
    // normals should come from meshBuffer or from SetColor or SetNormal.
    private static final int USE_MESH_NORMAL = 0x1 ;
    private static final int USE_MESH_COLOR  = 0x2 ;

    /**
     * Mesh buffer entry containing position, normal, and color.
     */
    static class MeshBufferEntry {
	short x, y, z ;
	short octant, sextant, u, v ;
	short r, g, b, a ;
    }

    // Geometry compression state variables.
    private short curX, curY, curZ ;
    private short curR, curG, curB, curA ;
    private int	curSex, curOct, curU, curV ;

    // Current vertex data.
    private Point3f curPos ;
    private Vector3f curNorm ;
    private Color4f curColor ;
    private int repCode ;

    // Flags indicating what data is bundled with the vertex.
    private boolean bundlingNorm ;
    private boolean bundlingColor ;
    private boolean doingAlpha ;

    // Internal decompression buffering variables.
    private int currentHeader = 0 ;
    private int nextHeader = 0 ;
    private int bitBuffer = 0 ;
    private int bitBufferCount = 32 ;

    // Used for benchmarking if so configured.
    private long startTime ;
    private int vertexCount ;

    // Bit-field masks: BMASK[i] = (1<<i)-1
    private static final int BMASK[] = {
	0x0,        0x1,        0x3,        0x7,
	0xF,        0x1F,       0x3F,       0x7F,
	0xFF,       0x1FF,      0x3FF,      0x7FF,
	0xFFF,      0x1FFF,     0x3FFF,     0x7FFF,
	0xFFFF,     0x1FFFF,    0x3FFFF,    0x7FFFF,
	0xFFFFF,    0x1FFFFF,   0x3FFFFF,   0x7FFFFF,
	0xFFFFFF,   0x1FFFFFF,  0x3FFFFFF,  0x7FFFFFF,
	0xFFFFFFF,  0x1FFFFFFF, 0x3FFFFFFF, 0x7FFFFFFF,
	0xFFFFFFFF, 
    } ;

    // A reference to the compressed data and the current offset.
    private byte gcData[] ;
    private int gcIndex ;

    // The normals table for decoding 6-bit [u,v] spherical sextant coordinates.
    private static final double gcNormals[][][] ;
    private static final double NORMAL_MAX_Y_ANG = 0.615479709 ;
    private static final boolean printNormalTable = false ;

    /**
     * Initialize the normals table.
     */
    static {
	int i, j, inx, iny, inz ;
	double th, psi, qnx, qny, qnz ;

	gcNormals = new double[65][65][3] ;

	for (i = 0 ; i < 65 ; i++) {
	    for (j = 0 ; j < 65 ; j++) {
		if (i+j > 64) continue ;

		psi = NORMAL_MAX_Y_ANG * (i / 64.0) ;
		th = Math.asin(Math.tan(NORMAL_MAX_Y_ANG * ((64-j)/64.0))) ;

		qnx = Math.cos(th) * Math.cos(psi) ;
		qny = Math.sin(psi) ;
		qnz = Math.sin(th) * Math.cos(psi) ;

		//  Convert the floating point normal to s1.14 bit notation,
		//  then back again.
		qnx = qnx*16384.0 ; inx = (int)qnx ;
		qnx = (double)inx ; qnx = qnx/16384.0 ;

		qny = qny*16384.0 ; iny = (int)qny ;
		qny = (double)iny ; qny = qny/16384.0 ;

		qnz = qnz*16384.0 ; inz = (int)qnz ;
		qnz = (double)inz ; qnz = qnz/16384.0 ;

		gcNormals[i][j][0] = qnx ;
		gcNormals[i][j][1] = qny ;
		gcNormals[i][j][2] = qnz ;
	    }
	}

	if (printNormalTable) {
	    System.out.println("struct {") ;
	    System.out.println("    double nx, ny, nz ;") ;
	    System.out.println("} gcNormals[65][65] = {");
	    for (i = 0 ; i <= 64 ; i++) {
		System.out.println("{") ;
		for (j = 0 ; j <= 64 ; j++) {
		    if (j+i > 64) continue ;
		    System.out.println("{ " + gcNormals[i][j][0] +
				       ", " + gcNormals[i][j][1] +
				       ", " + gcNormals[i][j][2] + " }") ;
		}
		System.out.println("},") ;
	    }
	    System.out.println("}") ;
	}
    }

    //
    // The constructor.
    //
    GeometryDecompressor() {
	curPos = new Point3f() ;
	curNorm = new Vector3f() ;
	curColor = new Color4f() ;
	gctables = new HuffmanTableEntry[3][64] ;

	for (int i = 0 ; i < 64 ; i++) {
	    gctables[0][i] = new HuffmanTableEntry() ;
	    gctables[1][i] = new HuffmanTableEntry() ;
	    gctables[2][i] = new HuffmanTableEntry() ;
	}
	
	meshBuffer = new MeshBufferEntry[16] ;
	for (int i = 0 ; i < 16 ; i++)
	    meshBuffer[i] = new MeshBufferEntry() ;
    }

    /**
     * Check version numbers and return true if compatible.
     */
    boolean checkVersion(int majorVersionNumber, int minorVersionNumber) {
	return ((majorVersionNumber < this.majorVersionNumber) ||
		((majorVersionNumber == this.majorVersionNumber) &&
		 (minorVersionNumber <= this.minorVersionNumber))) ;
    }

    /**
     * Decompress data and invoke abstract output methods.
     *
     * @param start byte offset to start of compressed geometry in data array
     * @param length size of compressed geometry in bytes
     * @param data array containing compressed geometry buffer of the
     * specified length at the given offset from the start of the array
     * @exception ArrayIndexOutOfBoundsException if start+length > data size
     */
    void decompress(int start, int length, byte data[]) {
	if (debug)
	    System.out.println("GeometryDecompressor.decompress\n" +
			       " start: " + start +
			       " length: " + length +
			       " data array size: " + data.length) ;
	if (benchmark) 
	    benchmarkStart(length) ;
	    
	if (start+length > data.length)
	    throw new ArrayIndexOutOfBoundsException
		(J3dI18N.getString("GeometryDecompressor0")) ;

	// Set reference to compressed data and skip to start of data.
	gcData = data ;
	gcIndex = start ;

	// Initialize state.
	bitBufferCount = 0 ;
	meshState = 0 ;
	bundlingNorm = false ;
	bundlingColor = false ;
	doingAlpha = false ;
	repCode = 0 ;

	// Headers are interleaved for hardware implementations, so the
	// first is always a nullop.
	nextHeader = GC_V_NO_OP ;

	// Enter decompression loop.
	while (gcIndex < start+length)
	    processDecompression() ;

	// Finish out any bits left in bitBuffer.
	while (bitBufferCount > 0)
	    processDecompression() ;

	if (benchmark)
	    benchmarkPrint(length) ;
    }

    //
    // Return the next bitCount bits of compressed data.
    //
    private int getBits(int bitCount, String d) {
	int bits ;

	if (debug)
	    System.out.print(" getBits(" + bitCount + ") " + d + ", " +
			     bitBufferCount + " available at gcIndex " +
			     gcIndex) ;

	if (bitCount == 0) {
	    if (debug) System.out.println(": got 0x0") ;
	    return 0 ;
	}
	
	if (bitBufferCount == 0) {
	    bitBuffer = (((gcData[gcIndex++] & 0xff) << 24) |
			 ((gcData[gcIndex++] & 0xff) << 16) |
			 ((gcData[gcIndex++] & 0xff) <<  8) |
			 ((gcData[gcIndex++] & 0xff))) ;

	    bitBufferCount = 32 ;
	}

	if (bitBufferCount >= bitCount) {
	    bits = (bitBuffer >>> (32 - bitCount)) & BMASK[bitCount] ;
	    bitBuffer = bitBuffer << bitCount ;
	    bitBufferCount -= bitCount ;
	} else {
	    bits = (bitBuffer >>> (32 - bitCount)) & BMASK[bitCount] ;
	    bits = bits >>> (bitCount - bitBufferCount) ;
	    bits = bits  << (bitCount - bitBufferCount) ;

	    bitBuffer = (((gcData[gcIndex++] & 0xff) << 24) |
			 ((gcData[gcIndex++] & 0xff) << 16) |
			 ((gcData[gcIndex++] & 0xff) <<  8) |
			 ((gcData[gcIndex++] & 0xff))) ;

	    bits = bits |
		((bitBuffer >>> (32 - (bitCount - bitBufferCount))) &
		 BMASK[bitCount - bitBufferCount]) ;

	    bitBuffer = bitBuffer << (bitCount - bitBufferCount) ;
	    bitBufferCount = 32 - (bitCount - bitBufferCount) ;
	}

	if (debug)
	    System.out.println(": got 0x" + Integer.toHexString(bits)) ;

	return bits ;
    }

    //
    // Shuffle interleaved headers and opcodes.
    //
    private void processDecompression() {
	int mbp ;
	currentHeader = nextHeader ;

	if ((currentHeader & 0xC0) == GC_VERTEX) {
	    // Process a vertex.
	    if (!bundlingNorm && !bundlingColor) {
		// get next opcode, process current position opcode
		nextHeader = getBits(8, "header") ;
		mbp = processDecompressionOpcode(0) ;

	    } else if (bundlingNorm && !bundlingColor) {
		// get normal header, process current position opcode
		nextHeader = getBits(6, "normal") ;
		mbp = processDecompressionOpcode(0) ;
		currentHeader = nextHeader | GC_SET_NORM ;

		// get next opcode, process current normal opcode
		nextHeader = getBits(8, "header") ;
		processDecompressionOpcode(mbp) ;

	    } else if (!bundlingNorm && bundlingColor) {
		// get color header, process current position opcode
		nextHeader = getBits(6, "color") ;
		mbp = processDecompressionOpcode(0) ;
		currentHeader = nextHeader | GC_SET_COLOR ;

		// get next opcode, process current color opcode
		nextHeader = getBits(8, "header") ;
		processDecompressionOpcode(mbp) ;

	    } else {
		// get normal header, process current position opcode
		nextHeader = getBits(6, "normal") ;
		mbp = processDecompressionOpcode(0) ;
		currentHeader = nextHeader | GC_SET_NORM ;

		// get color header, process current normal opcode
		nextHeader = getBits(6, "color") ;
		processDecompressionOpcode(mbp) ;
		currentHeader = nextHeader | GC_SET_COLOR ;

		// get next opcode, process current color opcode
		nextHeader = getBits(8, "header") ;
		processDecompressionOpcode(mbp) ;
	    }

	    // Send out the complete vertex.
	    outputVertex(curPos, curNorm, curColor, repCode) ;
	    if (benchmark) vertexCount++ ;

	    // meshState bits get turned off in the setColor and setNormal
	    // routines in order to keep track of what data a mesh buffer
	    // reference should use.
	    meshState |= USE_MESH_NORMAL ;
	    meshState |= USE_MESH_COLOR ;

	} else {
	    // Non-vertex case: get next opcode, then process current opcode.
	    nextHeader = getBits(8, "header") ;
	    processDecompressionOpcode(0) ;
	}
    }

    //
    // Decode the opcode in currentHeader, and dispatch to the appropriate
    // processing method.  
    //
    private int processDecompressionOpcode(int mbp) {
	if ((currentHeader & 0xC0) == GC_SET_NORM)
	    processSetNormal(mbp) ;
	else if ((currentHeader & 0xC0) == GC_SET_COLOR)
	    processSetColor(mbp) ;
	else if ((currentHeader & 0xC0) == GC_VERTEX)
	    // Return the state of the mesh buffer push bit
	    // when processing a vertex.
	    return processVertex() ;
	else if ((currentHeader & 0xE0) == GC_MESH_B_R) {
	    processMeshBR() ;

	    // Send out the complete vertex.
	    outputVertex(curPos, curNorm, curColor, repCode) ;
	    if (benchmark) vertexCount++ ;

	    // meshState bits get turned off in the setColor and setNormal
	    // routines in order to keep track of what data a mesh buffer
	    // reference should use.
	    meshState |= USE_MESH_NORMAL ;
	    meshState |= USE_MESH_COLOR ;
	}
	else if ((currentHeader & 0xF8) == GC_SET_STATE)
	    processSetState() ;
	else if ((currentHeader & 0xF8) == GC_SET_TABLE)
	    processSetTable() ;
	else if ((currentHeader & 0xFF) == GC_EOS)
	    processEos() ;
	else if ((currentHeader & 0xFF) == GC_V_NO_OP)
	    processVNoop() ;
	else if ((currentHeader & 0xFF) == GC_PASS_THROUGH)
	    processPassThrough() ;
	else if ((currentHeader & 0xFF) == GC_SKIP_8)
	    processSkip8() ;
    
	return 0 ;
    }

    //
    //  Process a set state opcode.
    //
    private void processSetState() {
	int ii ;
	if (debug)
	    System.out.println("GeometryDecompressor.processSetState") ;

	ii = getBits(3, "bundling") ;

	bundlingNorm  = ((currentHeader & 0x1) != 0) ;
	bundlingColor = (((ii >>> 2) & 0x1) != 0) ;
	doingAlpha    = (((ii >>> 1) & 0x1) != 0) ;

	if (debug)
	    System.out.println(" bundling normal: " + bundlingNorm  +
			       " bundling color: "  + bundlingColor +
			       " alpha present: "   + doingAlpha) ;

	// Call the abstract output implementation.
	outputVertexFormat(bundlingNorm, bundlingColor, doingAlpha) ;
    }

    //
    // Process a set decompression table opcode.
    //
    // Extract the parameters of the table set command,
    // and set the approprate table entries.
    //
    private void processSetTable() {
	HuffmanTableEntry gct[] ;
	int i, adr, tagLength, dataLength, rightShift, absolute ;
	int ii, index ;

	if (debug)
	    System.out.println("GeometryDecompressor.processSetTable") ;

	// Get reference to approprate 64 entry table.
	index = (currentHeader & 0x6) >>> 1 ;
	gct = gctables[index] ;

	// Get the remaining bits of the set table command.
	ii = getBits(15, "set table") ;

	// Extract the individual fields from the two bit strings.
	adr = ((currentHeader & 0x1) << 6) | ((ii >>> 9) & 0x3F) ;

	// Get data length.  For positions and colors, 0 really means 16, as 0
	// lengths are meaningless for them.  Normal components are allowed to
	// have lengths of 0.
	dataLength = (ii >>> 5) & 0x0F ;
	if (dataLength == 0 && index != 2)
	    dataLength = 16 ;

	rightShift = ii & 0x0F ;
	absolute = (ii >>> 4) & 0x1 ;

	//
	// Decode the tag length from the address field by finding the
	// first set 1 from the left in the bitfield.
	//
	for (tagLength = 6 ; tagLength > 0 ; tagLength--) {
	    if ((adr >> tagLength) != 0) break ;
	}

	// Shift the address bits up into place, and off the leading 1.
	adr = (adr << (6 - tagLength)) & 0x3F ;

	if (debug)
	    System.out.println(" table " + ((currentHeader & 0x6) >>> 1) +
			       " address "     + adr +
			       " tag length "  + tagLength +
			       " data length " + dataLength +
			       " shift "       + rightShift +
			       " absolute "    + absolute) ;

	// Fill in the table fields with the specified values.
	for (i = 0 ; i < (1 << (6 - tagLength)) ; i++) {
	    gct[adr+i].tagLength = tagLength ;
	    gct[adr+i].dataLength = dataLength ;
	    gct[adr+i].rightShift = rightShift ;
	    gct[adr+i].absolute = absolute ;
	}
    }


    //
    // Process a vertex opcode.  Any bundled normal and/or color will be
    // processed by separate methods.  Return the mesh buffer push indicator.
    //
    private int processVertex() {
	HuffmanTableEntry gct ;
	float fX, fY, fZ ;
	short dx, dy, dz ;
	int mbp, x, y, z, dataLen ;
	int ii ;

	// If the next command is a mesh buffer reference
	// then use colors and normals from the mesh buffer.
	meshState = 0 ;

	// Get a reference to the approprate tag table entry.
	gct = gctables[0][currentHeader & 0x3F] ;

	if (debug) System.out.println("GeometryDecompressor.processVertex\n" +
				      gct.toString()) ;

	// Get the true length of the data.
	dataLen = gct.dataLength - gct.rightShift ;

	// Read in the replace code and mesh buffer push bits,
	// if they're not in the current header.
	if (6 - (3 * dataLen) - gct.tagLength > 0) {
	    int numBits = 6 - (3 * dataLen) - gct.tagLength ;
	    int jj ;

	    jj = currentHeader & BMASK[numBits] ;
	    ii = getBits(3 - numBits, "repcode/mbp") ;
	    ii |= (jj << (3 - numBits)) ;
	    }
	else
	    ii = getBits(3, "repcode/mbp") ;

	repCode = ii >>> 1 ;
	mbp = ii & 0x1 ;

	// Read in x, y, and z components.
	x = currentHeader & BMASK[6-gct.tagLength] ;

	if (gct.tagLength + dataLen == 6) {
	    y = getBits(dataLen, "y") ;
	    z = getBits(dataLen, "z") ;
	} else if (gct.tagLength + dataLen <  6) {
	    x = x >> (6 - gct.tagLength - dataLen) ;

	    y = currentHeader & BMASK[6 - gct.tagLength - dataLen] ;
	    if (gct.tagLength + 2*dataLen == 6) {
		z = getBits(dataLen, "z") ;
	    } else if (gct.tagLength + 2*dataLen <  6) {
		y = y >> (6 - gct.tagLength - 2*dataLen) ;

		z = currentHeader & BMASK[6 - gct.tagLength - 2*dataLen] ;
		if (gct.tagLength + 3*dataLen <  6) {
		    z = z >> (6 - gct.tagLength - 3*dataLen) ;
		} else if (gct.tagLength + 3*dataLen >  6) {
		    ii = getBits(dataLen - (6 - gct.tagLength - 2*dataLen),
				 "z") ;
		    z = (z << (dataLen - (6 - gct.tagLength - 2*dataLen)))
			| ii ;
		}
	    } else {
		ii = getBits(dataLen - (6 - gct.tagLength - dataLen), "y") ;
		y = (y << (dataLen - (6 - gct.tagLength - dataLen))) | ii ;
		z = getBits(dataLen, "z") ;
	    }
	} else {
	    ii = getBits(dataLen - (6 - gct.tagLength), "x") ;
	    x = (x << (dataLen - (6 - gct.tagLength))) | ii ;
	    y = getBits(dataLen, "y") ;
	    z = getBits(dataLen, "z") ;
	}

	// Sign extend delta x y z components.
	x = x << (32 - dataLen) ; x = x >> (32 - dataLen) ;
	y = y << (32 - dataLen) ; y = y >> (32 - dataLen) ;
	z = z << (32 - dataLen) ; z = z >> (32 - dataLen) ;

	// Normalize values.
	dx = (short)(x << gct.rightShift) ;
	dy = (short)(y << gct.rightShift) ;
	dz = (short)(z << gct.rightShift) ;

	// Update current position, first adding deltas if in relative mode.
	if (gct.absolute != 0) {
	    curX = dx ; curY = dy ; curZ = dz ;
	    if (debug) System.out.println(" absolute position: " +
					  curX + " " + curY + " " + curZ) ;
	} else {
	    curX += dx ; curY += dy ; curZ += dz ;
	    if (debug) System.out.println(" delta position: " +
					  dx + " " + dy + " " + dz) ;
	}

	// Do optional mesh buffer push.
	if (mbp != 0) {
	    // Increment to next position (meshIndex is initialized to 15).
	    meshIndex = (meshIndex + 1) & 0xF ;
	    meshBuffer[meshIndex].x = curX ;
	    meshBuffer[meshIndex].y = curY ;
	    meshBuffer[meshIndex].z = curZ ;
	    if (debug)
		System.out.println(" pushed position into mesh buffer at " +
				   meshIndex) ;
	}

	// Convert point back to [-1..1] floating point.
	fX = curX ; fX /= 32768.0 ;
	fY = curY ; fY /= 32768.0 ;
	fZ = curZ ; fZ /= 32768.0 ;
	if (debug)
	    System.out.println(" result position " + fX + " " + fY + " " + fZ) ;

	curPos.set(fX, fY, fZ) ;
	return mbp ;
    }


    //
    // Process a set current normal opcode.
    //
    private void processSetNormal(int mbp) {
	HuffmanTableEntry gct ;
	int index, du, dv, n, dataLength ;
	int ii ;

	// if next command is a mesh buffer reference, use this normal
	meshState &= ~USE_MESH_NORMAL ;

	// use table 2 for normals
	gct = gctables[2][currentHeader & 0x3F] ;

	if (debug)
	    System.out.println("GeometryDecompressor.processSetNormal\n" +
			       gct.toString()) ;

	// subtract up-shift amount to get true data (u, v) length
	dataLength = gct.dataLength - gct.rightShift ;

	if (gct.absolute != 0) {
	    //
	    // Absolute normal case.  Extract index from 6-bit tag.
	    //
	    index = currentHeader & BMASK[6-gct.tagLength] ;

	    if (gct.tagLength != 0) {
		// read in the rest of the 6-bit sex/oct pair (index)
		ii = getBits(6 - (6 - gct.tagLength), "sex/oct") ;
		index = (index << (6 - (6 - gct.tagLength))) | ii ;
	    }

	    // read in u and v data
	    curU = getBits(dataLength, "u") ;
	    curV = getBits(dataLength, "v") ;

	    // normalize u, v, sextant, and octant
	    curU = curU << gct.rightShift ;
	    curV = curV << gct.rightShift ;

	    curSex = (index >> 3) & 0x7 ;
	    curOct = index & 0x7 ;

	    if (debug) {
		if (curSex < 6)
		    System.out.println(" absolute normal: sex " + curSex +
				       " oct " + curOct +
				       " u "   + curU   + " v " + curV) ;
		else
		    System.out.println(" special normal: sex " + curSex +
				       " oct " + curOct) ;
	    }
	} else {
	    //
	    // Relative normal case.  Extract du from 6-bit tag.
	    //
	    du = currentHeader & BMASK[6-gct.tagLength] ;

	    if (gct.tagLength + dataLength < 6) {
		// normalize du, get dv
		du = du >> (6 - gct.tagLength - dataLength) ;
		dv = currentHeader & BMASK[6 - gct.tagLength - dataLength] ;

		if (gct.tagLength + 2*dataLength <  6) {
		    // normalize dv
		    dv = dv >> (6 - gct.tagLength - 2*dataLength) ;
		} else if (gct.tagLength + 2*dataLength >  6) {
		    // read in rest of dv and normalize it
		    ii = getBits(dataLength -
				 (6 - gct.tagLength - dataLength), "dv") ;
		    dv = (dv << (dataLength -
				 (6 - gct.tagLength - dataLength))) | ii ;
		}
	    } else if (gct.tagLength + dataLength > 6) {
		// read in rest of du and normalize it
		ii = getBits(dataLength - (6 - gct.tagLength), "du") ;
		du = (du << (dataLength - (6 - gct.tagLength))) | ii ;
		// read in dv
		dv = getBits(dataLength, "dv") ;
	    } else {
		// read in dv
		dv = getBits(dataLength, "dv") ;
	    }

	    // Sign extend delta uv components.
	    du = du << (32 - dataLength) ; du = du >> (32 - dataLength) ;
	    dv = dv << (32 - dataLength) ; dv = dv >> (32 - dataLength) ;

	    // normalize values
	    du = du << gct.rightShift ;
	    dv = dv << gct.rightShift ;

	    // un-delta
	    curU += du ;
	    curV += dv ;

	    if (debug)
		System.out.println(" delta normal: du " + du + " dv " + dv) ;

	    //
	    // Check for normal wrap.
	    //
	    if (! ((curU >=  0) && (curV >= 0) && (curU + curV <= 64)))
		if ((curU < 0) && (curV >= 0)) {
		    // wrap on u, same octant, different sextant
		    curU = -curU ;
		    switch (curSex) {
		    case 0: curSex = 4 ; break ;
		    case 1: curSex = 5 ; break ;
		    case 2: curSex = 3 ; break ;
		    case 3: curSex = 2 ; break ;
		    case 4: curSex = 0 ; break ;
		    case 5: curSex = 1 ; break ;
		    }
		} else if ((curU >= 0) && (curV <  0)) {
		    // wrap on v, same sextant, different octant
		    curV = -curV ;
		    switch (curSex) {
		    case 1: case 5:
			curOct = curOct ^ 4 ;  // invert x axis
			break ;
		    case 0: case 4:
			curOct = curOct ^ 2 ;  // invert y axis
			break ;
		    case 2: case 3:
			curOct = curOct ^ 1 ;  // invert z axis
			break ;
		    }
		} else if (curU + curV > 64) {
		    // wrap on uv, same octant, different sextant
		    curU = 64 - curU ;
		    curV = 64 - curV ;
		    switch (curSex) {
		    case 0: curSex = 2 ; break ;
		    case 1: curSex = 3 ; break ;
		    case 2: curSex = 0 ; break ;
		    case 3: curSex = 1 ; break ;
		    case 4: curSex = 5 ; break ;
		    case 5: curSex = 4 ; break ;
		    }
		} else {
		    throw new IllegalArgumentException
			(J3dI18N.getString("GeometryDecompressor1")) ;
		}
	}

	// do optional mesh buffer push
	if (mbp != 0) {
	    if (debug)
		System.out.println(" pushing normal into mesh buffer at " +
				   meshIndex) ;

	    meshBuffer[meshIndex].sextant = (short)curSex ;
	    meshBuffer[meshIndex].octant = (short)curOct ;
	    meshBuffer[meshIndex].u = (short)curU ;
	    meshBuffer[meshIndex].v = (short)curV ;
	}

	// convert normal back to [-1..1] floating point
	indexNormal(curSex, curOct, curU, curV, curNorm) ;

	// a set normal opcode when normals aren't bundled with the vertices
	// is a global normal change.
	if (! bundlingNorm) outputNormal(curNorm) ;
    }


    //
    // Get the floating point normal from its sextant, octant, u, and v.
    //
    private void indexNormal(int sex, int oct, int u, int v, Vector3f n) {
	float nx, ny, nz, t ;

	if (debug) System.out.println(" sextant " + sex + " octant " + oct  +
				      " u " + u + " v " + v) ;
	if (sex > 5) {
	    // special normals
	    switch (oct & 0x1) {
	      case 0: // six coordinate axes
		switch (((sex & 0x1) << 1) | ((oct & 0x4) >> 2)) {
		  case 0: nx = 1.0f ; ny = nz = 0.0f ; break ;
		  case 1: ny = 1.0f ; nx = nz = 0.0f ; break ;
		  default:
		  case 2: nz = 1.0f ; nx = ny = 0.0f ; break ;
		}
		sex = 0 ; oct = (oct & 0x2) >> 1 ;
		oct = (oct << 2) | (oct << 1) | oct ;
		break ;
	      case 1: // eight mid
	      default:
		oct = ((sex & 0x1) << 2) | (oct >> 1) ;
		sex = 0 ;
		nx = ny = nz = (float)(1.0/Math.sqrt(3.0)) ;
		break ;
	    }
	    if ((oct & 0x1) != 0) nz = -nz ;
	    if ((oct & 0x2) != 0) ny = -ny ;
	    if ((oct & 0x4) != 0) nx = -nx ;

	} else {
	    // regular normals
	    nx = (float)gcNormals[v][u][0] ;
	    ny = (float)gcNormals[v][u][1] ;
	    nz = (float)gcNormals[v][u][2] ;

	    // reverse the swap 
	    if ((sex & 0x4) != 0) { t = nx ; nx = nz ; nz = t ; }
	    if ((sex & 0x2) != 0) { t = ny ; ny = nz ; nz = t ; }
	    if ((sex & 0x1) != 0) { t = nx ; nx = ny ; ny = t ; }

	    // reverse the sign flip 
	    if ((oct & 0x1) != 0) nz = -nz ;
	    if ((oct & 0x2) != 0) ny = -ny ;
	    if ((oct & 0x4) != 0) nx = -nx ;
	}

	// return resulting normal
	n.set(nx, ny, nz) ;
	if (debug)
	    System.out.println(" result normal: " + nx + " " + ny + " " + nz) ;
    }


    //
    // Process a set current color command.
    //
    private void processSetColor(int mbp) {
	HuffmanTableEntry gct ;
	short dr, dg, db, da ;
	float fR, fG, fB, fA ;
	int r, g, b, a, index, dataLength ;
	int ii ;

	// If the next command is a mesh buffer reference, use this color.
	meshState &= ~USE_MESH_COLOR ;

	// Get the huffman table entry.
	gct = gctables[1][currentHeader & 0x3F] ;

	if (debug)
	    System.out.println("GeometryDecompressor.processSetColor\n" +
			       gct.toString()) ;

	// Get the true length of the data.
	dataLength = gct.dataLength - gct.rightShift ;

	// Read in red, green, blue, and possibly alpha.
	r = currentHeader & BMASK[6 - gct.tagLength] ;
	a = 0 ;

	if (gct.tagLength + dataLength == 6) {
	    g = getBits(dataLength, "g") ;
	    b = getBits(dataLength, "b") ;
	    if (doingAlpha)
		a = getBits(dataLength, "a") ;
	}
	else if (gct.tagLength + dataLength <  6) {
	    r = r >> (6 - gct.tagLength - dataLength) ;

	    g = currentHeader & BMASK[6-gct.tagLength-dataLength] ;
	    if (gct.tagLength + 2*dataLength == 6) {
		b = getBits(dataLength, "b") ;
		if (doingAlpha)
		    a = getBits(dataLength, "a") ;
	    }
	    else if (gct.tagLength + 2*dataLength <  6) {
		g = g >> (6 - gct.tagLength - 2*dataLength) ;

		b = currentHeader & BMASK[6-gct.tagLength-2*dataLength] ;
		if (gct.tagLength + 3*dataLength == 6) {
		    if (doingAlpha)
			a = getBits(dataLength, "a") ;
		}
		else if (gct.tagLength + 3*dataLength <  6) {
		    b = b >> (6 - gct.tagLength - 3*dataLength) ;

		    if (doingAlpha) {
			a = currentHeader &
			    BMASK[6 - gct.tagLength - 4*dataLength] ;
			if (gct.tagLength + 4 * dataLength < 6) {
			    a = a >> (6 - gct.tagLength - 3*dataLength) ;
			}
			else if (gct.tagLength + 4 * dataLength > 6) {
			    ii = getBits(dataLength -
				       (6-gct.tagLength - 3*dataLength), "a") ;
			    a = (a << (dataLength -
				       (6-gct.tagLength - 3*dataLength))) | ii ;
			}
		    } 
		} else {
		    ii = getBits(dataLength -
				 (6 - gct.tagLength - 2*dataLength), "b") ;
		    b = (b << (dataLength -
			       (6 - gct.tagLength - 2*dataLength))) | ii ;
		    if (doingAlpha)
			a = getBits(dataLength, "a") ;
		}
	    } else {
		ii = getBits(dataLength - (6 - gct.tagLength - dataLength),
			     "g") ;
		g = (g << (dataLength -
			   (6 - gct.tagLength - dataLength))) | ii ;
		b = getBits(dataLength, "b") ;
		if (doingAlpha)
		    a = getBits(dataLength, "a") ;
	    }
	} else {
	    ii = getBits(dataLength - (6 - gct.tagLength), "r") ;
	    r = (r << (dataLength - (6 - gct.tagLength))) | ii ;
	    g = getBits(dataLength, "g") ;
	    b = getBits(dataLength, "b") ;
	    if (doingAlpha)
		a = getBits(dataLength, "a") ;
	}

	// Sign extend delta x y z components.
	r <<= (32 - dataLength) ;  r >>= (32 - dataLength) ;
	g <<= (32 - dataLength) ;  g >>= (32 - dataLength) ;
	b <<= (32 - dataLength) ;  b >>= (32 - dataLength) ;
	a <<= (32 - dataLength) ;  a >>= (32 - dataLength) ;

	// Normalize values.
	dr = (short)(r << gct.rightShift) ;
	dg = (short)(g << gct.rightShift) ;
	db = (short)(b << gct.rightShift) ;
	da = (short)(a << gct.rightShift) ;

	// Update current position, first adding deltas if in relative mode.
	if (gct.absolute != 0) {
	    curR = dr ; curG = dg ; curB = db ;
	    if (doingAlpha) curA = da ;
	    if (debug) System.out.println(" absolute color: r " + curR +
					  " g " + curG + " b " + curB +
					  " a " + curA) ;
	} else {
	    curR += dr ; curG += dg ; curB += db ;
	    if (doingAlpha) curA += da ;
	    if (debug) System.out.println(" delta color: dr "  + dr +
					  " dg " + dg + " db " + db +
					  " da " + da) ;
	}

	// Do optional mesh buffer push.
	if (mbp != 0) {
	    if (debug)
		System.out.println(" pushing color into mesh buffer at " +
				   meshIndex) ;

	    meshBuffer[meshIndex].r = curR ;
	    meshBuffer[meshIndex].g = curG ;
	    meshBuffer[meshIndex].b = curB ;
	    meshBuffer[meshIndex].a = curA ;
	}

	// Convert point back to [-1..1] floating point.
	fR = curR ; fR /= 32768.0 ;
	fG = curG ; fG /= 32768.0 ;
	fB = curB ; fB /= 32768.0 ;
	fA = curA ; fA /= 32768.0 ;

	curColor.set(fR, fG, fB, fA) ;
	if (debug) System.out.println(" result color: " + fR +
				      " " + fG + " " + fB + " " + fA) ;

	// A set color opcode when colors aren't bundled with the vertices
	// is a global color change.
	if (! bundlingColor) outputColor(curColor) ;
    }


    //
    // Process a mesh buffer reference command.
    //
    private void processMeshBR() {
	MeshBufferEntry entry ;
	int index, normal ;
	int ii ;

	if (debug)
	    System.out.println("GeometryDecompressor.processMeshBR") ;

	ii = getBits(1, "mbr") ;

	index = (currentHeader >>> 1) & 0xF ;
	repCode = ((currentHeader & 0x1) << 1) | ii ;

	// Adjust index to proper place in fifo.
	index = (meshIndex - index) & 0xf ;
	if (debug)
	    System.out.println(" using index " + index) ;

	// Get reference to mesh buffer entry.
	entry = meshBuffer[index] ;
	curX = entry.x ;
	curY = entry.y ;
	curZ = entry.z ;

	// Convert point back to [-1..1] floating point.
	curPos.set(((float)curX)/32768.0f,
		   ((float)curY)/32768.0f,
		   ((float)curZ)/32768.0f) ;

	if (debug) System.out.println(" retrieved position " + curPos.x +
				      " " + curPos.y + " " + curPos.z +
				      " replace code " + repCode) ;

	// Get mesh buffer normal if previous opcode was not a setNormal.
	if (bundlingNorm && ((meshState & USE_MESH_NORMAL) != 0)) {
	    curSex = entry.sextant ;
	    curOct = entry.octant ;
	    curU = entry.u ;
	    curV = entry.v ;

	    // Convert normal back to -1.0 - 1.0 floating point from index.
	    normal = (curSex<<15) | (curOct<<12) | (curU<<6) | curV ;

	    if (debug) System.out.println(" retrieving normal") ;
	    indexNormal(curSex, curOct, curU, curV, curNorm) ;
	}

	// Get mesh buffer color if previous opcode was not a setColor.
	if (bundlingColor && ((meshState & USE_MESH_COLOR) != 0)) {
	    curR = entry.r ;
	    curG = entry.g ;
	    curB = entry.b ;

	    // Convert point back to -1.0 - 1.0 floating point.
	    curColor.x = curR ; curColor.x /= 32768.0 ;
	    curColor.y = curG ; curColor.y /= 32768.0 ;
	    curColor.z = curB ; curColor.z /= 32768.0 ;

	    if (doingAlpha) {
		curA = entry.a ;
		curColor.w = curA ; curColor.w /= 32768.0 ;
	    }
	    if (debug)
		System.out.println(" retrieved color "    + curColor.x + 
				   " " + curColor.y + " " + curColor.z +
				   " " + curColor.w) ;
	}

        // Reset meshState.
	meshState = 0 ;
    }


    // Process a end-of-stream opcode.
    private void processEos() {
	if (debug) System.out.println("GeometryDecompressor.processEos") ;
    }

    // Process a variable length no-op opcode.
    private void processVNoop() {
	int ii, ct ;
	if (debug) System.out.println("GeometryDecompressor.processVNoop") ;

	ct = getBits(5, "noop count") ;
	ii = getBits(ct, "noop bits") ;
    }

    // Process a pass-through opcode.
    private void processPassThrough() {
	int ignore ;
	if (debug)
	    System.out.println("GeometryDecompressor.processPassThrough") ;

	ignore = getBits(24, "passthrough") ;
	ignore = getBits(32, "passthrough") ;
    }

    // Process a skip-8 opcode.
    private void processSkip8() {
	int skip ;
	if (debug) System.out.println("GeometryDecompressor.processSkip8") ;

	skip = getBits(8, "skip8") ;
    }

    private void benchmarkStart(int length) {
	vertexCount = 0 ;
	System.out.println(" GeometryDecompressor: decompressing " +
			   length + " bytes...") ;
	startTime = J3dClock.currentTimeMillis() ;
    }

    private void benchmarkPrint(int length) {
	float t = (J3dClock.currentTimeMillis() - startTime) / 1000.0f ;
	System.out.println
	    ("  done in " + t + " sec." + "\n" +
	     "  decompressed " + vertexCount + " vertices at " + 
	     (vertexCount/t) + " vertices/sec\n") ;

	System.out.print("  vertex data present: coords") ;
	int floatVertexSize = 12 ;
	if (bundlingNorm) {
	    System.out.print(" normals") ;
	    floatVertexSize += 12 ;
	}
	if (bundlingColor) {
	    System.out.println(" colors") ;
	    floatVertexSize += 12 ;
	}
	if (doingAlpha) {
	    System.out.println(" alpha") ;
	    floatVertexSize +=  4 ;
	}
	System.out.println() ;

	System.out.println
	    ("  bytes of data in generalized strip output: " +
	     (vertexCount * floatVertexSize) + "\n" +
	     "  compression ratio: " +
	     (length / (float)(vertexCount * floatVertexSize)) + "\n") ;
    }
}
