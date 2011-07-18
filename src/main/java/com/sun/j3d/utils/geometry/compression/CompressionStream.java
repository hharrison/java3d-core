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

import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.utils.geometry.GeometryInfo;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.GeometryStripArray;
import javax.media.j3d.IndexedGeometryArray;
import javax.media.j3d.IndexedGeometryStripArray;
import javax.media.j3d.IndexedLineArray;
import javax.media.j3d.IndexedLineStripArray;
import javax.media.j3d.IndexedQuadArray;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.IndexedTriangleFanArray;
import javax.media.j3d.IndexedTriangleStripArray;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.LineArray;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Material;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 * This class is used as input to a geometry compressor.  It collects elements
 * such as vertices, normals, colors, mesh references, and quantization
 * parameters in an ordered stream.  This stream is then traversed during
 * the compression process and used to build the compressed output buffer.
 *
 * @see GeometryCompressor
 *
 * @since Java 3D 1.5
 */
public class CompressionStream {
    //
    // NOTE: For now, copies are made of all GeometryArray vertex components
    // even when by-reference access is available.
    //
    // TODO: Retrofit all CompressionStreamElements and MeshBuffer to handle
    // offsets to vertex data array references so that vertex components don't
    // have to be copied.  New CompressionStreamElements could be defined to
    // set the current array reference during the quantization pass, or the
    // reference could be included in every CompressionStreamElement along
    // with the data offsets.
    //
    // TODO: Quantize on-the-fly when adding GeometryArray vertex data so that
    // CompressionStreamElements don't need references to the original float,
    // double, or byte data.  Quantization is currently a separate pass since
    // the 1st pass adds vertex data and gets the total object bounds, but
    // this can be computed by merging the bounds of each GeometryArray
    // compressed into a single object.  The 2nd pass quantization is still
    // needed for vertex data which isn't retrieved from a GeometryArray; for
    // example, apps that might use the addVertex() methods directly instead
    // of addGeometryArray().
    //
    // TODO: To further optimize memory, create new subclasses of
    // CompressionStream{Color, Normal} for bundled attributes and add them as
    // explicit stream elements.  Then CompressionStreamVertex won't need to
    // carry references to them.  This memory savings might be negated by the
    // extra overhead of adding more elements to the stream, however.
    //
    // TODO: Keep the absolute quantized values in the mesh buffer mirror so
    // that unmeshed CompressionStreamElements don't need to carry them.
    //
    // TODO: Support texture coordinate compression even though Level II is
    // not supported by any hardware decompressor on any graphics card.
    // Software decompression is still useful for applications interested in
    // minimizing file space, transmission time, and object loading time.
    //
    private static final boolean debug = false ;
    private static final boolean benchmark = false ;

    // Mesh buffer normal substitution is unavailable in Level I.
    private static final boolean noMeshNormalSubstitution = true ;

    /**
     * This flag indicates that a vertex starts a new triangle or line strip.
     */
    static final int RESTART = 1 ;

    /**
     * This flag indicates that the next triangle in the strip is defined by
     * replacing the middle vertex of the previous triangle in the strip.
     * Equivalent to REPLACE_OLDEST for line strips.
     */
    static final int REPLACE_MIDDLE = 2 ;

    /**
     * This flag indicates that the next triangle in the strip is defined by
     * replacing the oldest vertex of the previous triangle in the strip.
     * Equivalent to REPLACE_MIDDLE for line strips.
     */
    static final int REPLACE_OLDEST = 3 ;

    /**
     * This flag indicates that a vertex is to be pushed into the mesh buffer.
     */
    static final int MESH_PUSH = 1 ;

    /**
     * This flag indicates that a vertex does not use the mesh buffer.
     */
    static final int NO_MESH_PUSH = 0 ;

    /**
     * Byte to float scale factor for scaling byte color components.
     */
    static final float ByteToFloatScale = 1.0f/255.0f;

    /**
     * Type of this stream, either CompressedGeometryData.Header.POINT_BUFFER,
     * CompressedGeometryData.Header.LINE_BUFFER, or
     * CompressedGeometryData.Header.TRIANGLE_BUFFER
     */
    int streamType ;

    /**
     * A mask indicating which components are present in each vertex, as
     * defined by GeometryArray.
     */
    int vertexComponents ;

    /**
     * Boolean indicating colors are bundled with the vertices.
     */
    boolean vertexColors ;

    /**
     * Boolean indicating RGB colors are bundled with the vertices.
     */
    boolean vertexColor3 ;

    /**
     * Boolean indicating RGBA colors are bundled with the vertices.
     */
    boolean vertexColor4 ;

    /**
     * Boolean indicating normals are bundled with the vertices.
     */
    boolean vertexNormals ;

    /**
     * Boolean indicating texture coordinates are present.
     */
    boolean vertexTextures ;

    /**
     * Boolean indicating that 2D texture coordinates are used.
     * Currently only used to skip over textures in interleaved data.
     */
    boolean vertexTexture2 ;

    /**
     * Boolean indicating that 3D texture coordinates are used.
     * Currently only used to skip over textures in interleaved data.
     */
    boolean vertexTexture3 ;

    /**
     * Boolean indicating that 4D texture coordinates are used.
     * Currently only used to skip over textures in interleaved data.
     */
    boolean vertexTexture4 ;

    /**
     * Axes-aligned box enclosing all vertices in model coordinates.
     */
    Point3d mcBounds[] = new Point3d[2] ;

    /**
     * Axes-aligned box enclosing all vertices in normalized coordinates.
     */
    Point3d ncBounds[] = new Point3d[2] ;

    /**
     * Axes-aligned box enclosing all vertices in quantized coordinates.
     */
    Point3i qcBounds[] = new Point3i[2] ;

    /**
     * Center for normalizing positions to the unit cube.
     */
    double center[] = new double[3] ;

    /**
     * Maximum position range along the 3 axes.
     */
    double positionRangeMaximum ;

    /**
     * Scale for normalizing positions to the unit cube.
     */
    double scale ;

    /**
     * Current position component (X, Y, and Z) quantization value.  This can
     * range from 1 to 16 bits and has a default of 16.<p>
     *
     * At 1 bit of quantization it is not possible to express positive
     * absolute or delta positions.
     */
    int positionQuant ;

    /**
     * Current color component (R, G, B, A) quantization value.  This can
     * range from 2 to 16 bits and has a default of 9.<p>
     *
     * A color component is represented with a signed fixed-point value in
     * order to be able express negative deltas; the default of 9 bits
     * corresponds to the 8-bit color component range of the graphics hardware
     * commonly available.  Colors must be non-negative, so the lower limit of
     * quantization is 2 bits.
     */
    int colorQuant ;

    /**
     * Current normal component (U and V) quantization value.  This can range
     * from 0 to 6 bits and has a default of 6.<p>
     *
     * At 0 bits of quantization normals are represented only as 6 bit
     * sextant/octant pairs and 14 specially encoded normals (the 6 axis
     * normals and the 8 octant midpoint normals); since U and V can only be 0
     * at the minimum quantization, the totally number of unique normals is 
     * 12 + 14 = 26.
     */
    int normalQuant ;

    /**
     * Flag indicating position quantization change.
     */
    boolean positionQuantChanged ;

    /**
     * Flag indicating color quantization change.
     */
    boolean colorQuantChanged ;

    /**
     * Flag indicating normal quantization change.
     */
    boolean normalQuantChanged ;

    /**
     * Last quantized position.
     */
    int lastPosition[] = new int[3] ;

    /**
     * Last quantized color.
     */
    int lastColor[] = new int[4] ;

    /**
     * Last quantized normal's sextant.
     */
    int lastSextant ;

    /**
     * Last quantized normal's octant.
     */
    int lastOctant ;

    /**
     * Last quantized normal's U encoding parameter.
     */
    int lastU ;

    /**
     * Last quantized normal's V encoding parameter.
     */
    int lastV ;

    /**
     * Flag indicating last normal used a special encoding.
     */
    boolean lastSpecialNormal ;

    /**
     * Flag indicating the first position in this stream.
     */
    boolean firstPosition ;

    /**
     * Flag indicating the first color in this stream.
     */
    boolean firstColor ;

    /**
     * Flag indicating the first normal in this stream.
     */
    boolean firstNormal ;

    /**
     * The total number of bytes used to create the uncompressed geometric
     * elements in this stream, useful for performance analysis.  This
     * excludes mesh buffer references.
     */
    int byteCount ;

    /**
     * The number of vertices created for this stream, excluding mesh buffer
     * references.
     */
    int vertexCount ;

    /**
     * The number of mesh buffer references created for this stream.
     */
    int meshReferenceCount ;

    /**
     * Mesh buffer mirror used for computing deltas during quantization pass
     * and a limited meshing algorithm for unstripped data.
     */
    MeshBuffer meshBuffer = new MeshBuffer() ;


    // Collection which holds the elements of this stream.
    private Collection stream ;

    // True if preceding stream elements were colors or normals.  Used to flag
    // color and normal mesh buffer substitution when computing deltas during
    // quantization pass.
    private boolean lastElementColor = false ;
    private boolean lastLastElementColor = false ;
    private boolean lastElementNormal = false ;
    private boolean lastLastElementNormal = false ;

    // Some convenient temporary holding variables.
    private Point3f p3f = new Point3f() ;
    private Color3f c3f = new Color3f() ;
    private Color4f c4f = new Color4f() ;
    private Vector3f n3f = new Vector3f() ;


    // Private constructor for common initializations.
    private CompressionStream() {
	this.stream = new LinkedList() ;

	byteCount = 0 ;
	vertexCount = 0 ;
	meshReferenceCount = 0 ;

	mcBounds[0] = new Point3d(Double.POSITIVE_INFINITY,
				  Double.POSITIVE_INFINITY,
				  Double.POSITIVE_INFINITY) ;
	mcBounds[1] = new Point3d(Double.NEGATIVE_INFINITY,
				  Double.NEGATIVE_INFINITY,
				  Double.NEGATIVE_INFINITY) ;

	qcBounds[0] = new Point3i(Integer.MAX_VALUE,
				  Integer.MAX_VALUE,
				  Integer.MAX_VALUE) ;
	qcBounds[1] = new Point3i(Integer.MIN_VALUE,
				  Integer.MIN_VALUE,
				  Integer.MIN_VALUE) ;

	/* normalized bounds computed from quantized bounds */
	ncBounds[0] = new Point3d() ;
	ncBounds[1] = new Point3d() ;
    }

    /**
     * Creates a new CompressionStream for the specified geometry type and
     * vertex format.<p>
     * 
     * @param streamType type of data in this stream, either
     * CompressedGeometryData.Header.POINT_BUFFER,
     * CompressedGeometryData.Header.LINE_BUFFER, or
     * CompressedGeometryData.Header.TRIANGLE_BUFFER
     * @param vertexComponents a mask indicating which components are present
     * in each vertex, as defined by GeometryArray: COORDINATES, NORMALS, and
     * COLOR_3 or COLOR_4.
     * @see GeometryCompressor
     * @see GeometryArray
     */
    CompressionStream(int streamType, int vertexComponents) {
	this() ;
	this.streamType = streamType ;
	this.vertexComponents = getVertexComponents(vertexComponents) ;
    }

    // See what vertex geometry components are present.  The byReference,
    // interleaved, useNIOBuffer, and useCoordIndexOnly flags are not
    // examined.
    private int getVertexComponents(int vertexFormat) {
	int components = 0 ;

	vertexColors = vertexColor3 = vertexColor4 = vertexNormals =
	    vertexTextures = vertexTexture2 = vertexTexture3 = vertexTexture4 =
	    false ;

	if ((vertexFormat & GeometryArray.NORMALS) != 0) {
	    vertexNormals = true ;
	    components &= GeometryArray.NORMALS ;
	    if (debug) System.out.println("vertexNormals") ;
	}

	if ((vertexFormat & GeometryArray.COLOR_3) != 0) {
	    vertexColors = true ;

	    if ((vertexFormat & GeometryArray.COLOR_4) != 0) {
		vertexColor4 = true ;
		components &= GeometryArray.COLOR_4 ;
		if (debug) System.out.println("vertexColor4") ;
	    }
	    else {
		vertexColor3 = true ;
		components &= GeometryArray.COLOR_3 ;
		if (debug) System.out.println("vertexColor3") ;
	    }
	}

	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
	    vertexTextures = true ;
	    vertexTexture2 = true ;
	    components &= GeometryArray.TEXTURE_COORDINATE_2 ;
	    if (debug) System.out.println("vertexTexture2") ;
	}
	else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
	    vertexTextures = true ;
	    vertexTexture3 = true ;
	    components &= GeometryArray.TEXTURE_COORDINATE_3 ;
	    if (debug) System.out.println("vertexTexture3") ;
	}
	else if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
	    vertexTextures = true ;
	    vertexTexture4 = true ;
	    components &= GeometryArray.TEXTURE_COORDINATE_4 ;
	    if (debug) System.out.println("vertexTexture4") ;
	}

	if (vertexTextures)
	    // Throw exception for now until texture is supported.
	    throw new UnsupportedOperationException
		("\ncompression of texture coordinates is not supported") ;

	return components ;
    }

    // Get the streamType associated with a GeometryArray instance.
    private int getStreamType(GeometryArray ga) {
	if (ga instanceof TriangleStripArray ||
	    ga instanceof IndexedTriangleStripArray ||
	    ga instanceof TriangleFanArray ||
	    ga instanceof IndexedTriangleFanArray ||
	    ga instanceof TriangleArray ||
	    ga instanceof IndexedTriangleArray ||
	    ga instanceof QuadArray ||
	    ga instanceof IndexedQuadArray)

	    return CompressedGeometryData.Header.TRIANGLE_BUFFER ;

	else if (ga instanceof LineArray ||
		 ga instanceof IndexedLineArray ||
		 ga instanceof LineStripArray ||
		 ga instanceof IndexedLineStripArray)

	    return CompressedGeometryData.Header.LINE_BUFFER ;

	else
	    return CompressedGeometryData.Header.POINT_BUFFER ;
    }

    /**
     * Iterates across all compression stream elements and applies
     * quantization parameters, encoding consecutive vertices as delta values
     * whenever possible.  Each geometric element is mapped to a HuffmanNode
     * object containing its resulting bit length, right shift (trailing 0
     * count), and absolute or relative status.<p>
     * 
     * Positions are normalized to span a unit cube via an offset and a
     * uniform scale factor that maps the midpoint of the object extents along
     * each dimension to the origin, and the longest dimension of the object to
     * the open interval (-1.0 .. +1.0).  The geometric endpoints along that
     * dimension are both one quantum away from unity; for example, at a
     * position quantization of 6 bits, an object would be normalized so that
     * its most negative dimension is at (-1 + 1/64) and the most positive is
     * at (1 - 1/64).<p>
     * 
     * Normals are assumed to be of unit length.  Color components are clamped
     * to the [0..1) range, where the right endpoint is one quantum less
     * than 1.0.<p>
     *
     * @param huffmanTable Table which will map geometric compression stream
     * elements to HuffmanNode objects describing each element's data
     * representation.  This table can then be processed with Huffman's
     * algorithm to optimize the bit length of descriptor tags according to
     * the number of geometric elements mapped to each tag.
     */
    void quantize(HuffmanTable huffmanTable) {
	// Set up default initial quantization parameters.  The position and
	// color parameters specify the number of bits for each X, Y, Z, R, G,
	// B, or A component.  The normal quantization parameter specifies the
	// number of bits for each U and V component.
	positionQuant = 16 ;
	colorQuant = 9 ;
	normalQuant = 6 ;

	// Compute position center and scaling for normalization to the unit
	// cube.  This is a volume bounded by the open intervals (-1..1) on
	// each axis.
	center[0] = (mcBounds[1].x + mcBounds[0].x) / 2.0 ;
	center[1] = (mcBounds[1].y + mcBounds[0].y) / 2.0 ;
	center[2] = (mcBounds[1].z + mcBounds[0].z) / 2.0 ;

	double xRange = mcBounds[1].x - mcBounds[0].x ;
	double yRange = mcBounds[1].y - mcBounds[0].y ;
	double zRange = mcBounds[1].z - mcBounds[0].z ;

	if (xRange > yRange)
	    positionRangeMaximum = xRange ;
	else
	    positionRangeMaximum = yRange ;

	if (zRange > positionRangeMaximum)
	    positionRangeMaximum = zRange ;

	// Adjust the range of the unit cube to match the default
	// quantization.
	//
	// This scale factor along with the center values computed above will
	// produce 16-bit integer representations of the floating point
	// position coordinates ranging symmetrically about 0 from -32767 to
	// +32767.  -32768 is not used and the normalized floating point
	// position coordinates of -1.0 as well as +1.0 will not be
	// represented.
	//
	// Applications which wish to seamlessly stitch together compressed
	// objects will need to be aware that the range of normalized
	// positions will be one quantum away from the [-1..1] endpoints of
	// the unit cube and should adjust scale factors accordingly.
	scale = (2.0 / positionRangeMaximum) * (32767.0 / 32768.0) ;

	// Flag quantization change.
	positionQuantChanged = colorQuantChanged = normalQuantChanged = true ;

	// Flag first position, color, and normal.
	firstPosition = firstColor = firstNormal = true ;

	// Apply quantization.
	Iterator i = stream.iterator() ;
	while (i.hasNext()) {
	    Object o = i.next() ;

	    if (o instanceof CompressionStreamElement) {
		((CompressionStreamElement)o).quantize(this, huffmanTable) ;

		// Keep track of whether last two elements were colors or
		// normals for mesh buffer component substitution semantics.
		lastLastElementColor = lastElementColor ;
		lastLastElementNormal = lastElementNormal ;
		lastElementColor = lastElementNormal = false ;

		if (o instanceof CompressionStreamColor)
		    lastElementColor = true ;
		else if (o instanceof CompressionStreamNormal)
		    lastElementNormal = true ;
	    }
	}

	// Compute the bounds in normalized coordinates.
	ncBounds[0].x = (double)qcBounds[0].x / 32768.0 ;
	ncBounds[0].y = (double)qcBounds[0].y / 32768.0 ;
	ncBounds[0].z = (double)qcBounds[0].z / 32768.0 ;

	ncBounds[1].x = (double)qcBounds[1].x / 32768.0 ;
	ncBounds[1].y = (double)qcBounds[1].y / 32768.0 ;
	ncBounds[1].z = (double)qcBounds[1].z / 32768.0 ;
    }

    /**
     * Iterates across all compression stream elements and builds the
     * compressed geometry command stream output.<p>
     *
     * @param huffmanTable Table which maps geometric elements in this stream
     * to tags describing the encoding parameters (length, shift, and
     * absolute/relative status) to be used for their representations in the
     * compressed output.  All tags must be 6 bits or less in length, and the
     * sum of the number of bits in the tag plus the number of bits in the
     * data it describes must be at least 6 bits in length.
     *
     * @param outputBuffer CommandStream to use for collecting the compressed
     * bits.
     */
    void outputCommands(HuffmanTable huffmanTable, CommandStream outputBuffer) {
	//
	// The first command output is setState to indicate what data is
	// bundled with each vertex.  Although the semantics of geometry
	// decompression allow setState to appear anywhere in the stream, this
	// cannot be handled by the current Java 3D software decompressor,
	// which internally decompresses an entire compressed buffer into a
	// single retained object sharing a single consistent vertex format.
	// This limitation may be removed in subsequent releases of Java 3D.
	//
	int bnv = (vertexNormals? 1 : 0) ;
	int bcv = ((vertexColor3 || vertexColor4)? 1 : 0) ;
	int cap = (vertexColor4? 1 : 0) ;

	int command = CommandStream.SET_STATE | bnv ;
	long data = (bcv << 2) | (cap << 1) ;

	// Output the setState command.
	outputBuffer.addCommand(command, 8, data, 3) ;

	// Output the Huffman table commands.
	huffmanTable.outputCommands(outputBuffer) ;

	// Output each compression stream element's data.
	Iterator i = stream.iterator() ;
	while (i.hasNext()) {
	    Object o = i.next() ;
	    if (o instanceof CompressionStreamElement)
		((CompressionStreamElement)o).outputCommand(huffmanTable,
							    outputBuffer) ;
	}

	// Finish the header-forwarding interleave and long-word align.
	outputBuffer.end() ;
    }

    /**
     * Retrieve the total size of the uncompressed geometric data in bytes,
     * excluding mesh buffer references.
     * @return uncompressed byte count
     */
    int getByteCount() {
	return byteCount ;
    }

    /**
     * Retrieve the the number of vertices created for this stream, excluding
     * mesh buffer references.
     * @return vertex count
     */
    int getVertexCount() {
	return vertexCount ;
    }

    /**
     * Retrieve the number of mesh buffer references created for this stream. 
     * @return mesh buffer reference count
     */
    int getMeshReferenceCount() {
	return meshReferenceCount ;
    }

    /**
     * Stream element that sets position quantization during quantize pass.
     */
    private class PositionQuant extends CompressionStreamElement {
	int value ;

	PositionQuant(int value) {
	    this.value = value ;
	}

	void quantize(CompressionStream s, HuffmanTable t) {
	    positionQuant = value ;
	    positionQuantChanged = true ;

	    // Adjust range of unit cube scaling to match quantization.
	    scale = (2.0 / positionRangeMaximum) *
		(((double)((1 << (value-1)) - 1))/((double)(1 << (value-1)))) ;
	}

	public String toString() {
	    return "positionQuant: " + value ;
	}
    }

    /**
     * Stream element that sets normal quantization during quantize pass.
     */
    private class NormalQuant extends CompressionStreamElement {
	int value ;

	NormalQuant(int value) {
	    this.value = value ;
	}

	void quantize(CompressionStream s, HuffmanTable t) {
	    normalQuant = value ;
	    normalQuantChanged = true ;
	}

	public String toString() {
	    return "normalQuant: " + value ;
	}
    }

    /**
     * Stream element that sets color quantization during quantize pass.
     */
    private class ColorQuant extends CompressionStreamElement {
	int value ;

	ColorQuant(int value) {
	    this.value = value ;
	}

	void quantize(CompressionStream s, HuffmanTable t) {
	    colorQuant = value ;
	    colorQuantChanged = true ;
	}

	public String toString() {
	    return "colorQuant: " + value ;
	}
    }

    /**
     * Stream element that references the mesh buffer.
     */
    private class MeshReference extends CompressionStreamElement {
	int stripFlag, meshIndex ;

	MeshReference(int stripFlag, int meshIndex) {
	    this.stripFlag = stripFlag ;
	    this.meshIndex = meshIndex ;
	    meshReferenceCount++ ;
	}

	void quantize(CompressionStream s, HuffmanTable t) {
	    // Retrieve the vertex from the mesh buffer mirror and set up the
	    // data needed for the next stream element to compute its deltas.
	    CompressionStreamVertex v = meshBuffer.getVertex(meshIndex) ;
	    lastPosition[0] = v.xAbsolute ;
	    lastPosition[1] = v.yAbsolute ;
	    lastPosition[2] = v.zAbsolute ;

	    // Set up last color data if it exists and previous elements
	    // don't override it.
	    if (v.color != null && !lastElementColor &&
		!(lastElementNormal && lastLastElementColor)) {
		lastColor[0] = v.color.rAbsolute ;
		lastColor[1] = v.color.gAbsolute ;
		lastColor[2] = v.color.bAbsolute ;
		lastColor[3] = v.color.aAbsolute ;
	    }

	    // Set up last normal data if it exists and previous element
	    // doesn't override it.
	    if (v.normal != null && !lastElementNormal &&
		!(lastElementColor && lastLastElementNormal)) {
		lastSextant = v.normal.sextant ;
		lastOctant = v.normal.octant ;
		lastU = v.normal.uAbsolute ;
		lastV = v.normal.vAbsolute ;
		lastSpecialNormal = v.normal.specialNormal ;
	    }
	}

	void outputCommand(HuffmanTable t, CommandStream outputBuffer) {
	    int command = CommandStream.MESH_B_R ;
	    long data = stripFlag & 0x1 ;

	    command |= (((meshIndex & 0xf) << 1) | (stripFlag >> 1)) ;
	    outputBuffer.addCommand(command, 8, data, 1) ;
	}

	public String toString() {
	    return
		"meshReference: stripFlag " + stripFlag +
		" meshIndex " + meshIndex ;
	}
    }


    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, int stripFlag) {
	stream.add(new CompressionStreamVertex(this, pos,
					       (Vector3f)null, (Color3f)null,
					       stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, Vector3f norm, int stripFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, (Color3f)null, stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, Color3f color, int stripFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, (Vector3f)null, color, stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, Color4f color, int stripFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, (Vector3f)null, color, stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, Vector3f norm, Color3f color,
			  int stripFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, color, stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART,
     * REPLACE_OLDEST, or REPLACE_MIDDLE
     */
    void addVertex(Point3f pos, Vector3f norm, Color4f color,
			  int stripFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, color, stripFlag, NO_MESH_PUSH)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, (Vector3f)null, (Color3f)null, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Vector3f norm,
			  int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, (Color3f)null, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Color3f color,
			  int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, (Vector3f)null, color, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Color4f color,
			  int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, (Vector3f)null, color, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Vector3f norm, Color3f color,
			  int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, color, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param color color data
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Vector3f norm, Color4f color,
			  int stripFlag, int meshFlag) {
	stream.add(new CompressionStreamVertex
	    (this, pos, norm, color, stripFlag, meshFlag)) ;
    }

    /**
     * Copy vertex data and add it to the end of this stream.
     * @param pos position data
     * @param norm normal data
     * @param color color data, either Color3f or Color4f, determined by
     * current vertex format
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshFlag if MESH_PUSH the vertex is pushed into the mesh buffer
     */
    void addVertex(Point3f pos, Vector3f norm,
		   Object color, int stripFlag, int meshFlag) {

	if (vertexColor3) 
	    stream.add(new CompressionStreamVertex
		       (this, pos, norm, (Color3f)color, stripFlag, meshFlag)) ;
	else
	    stream.add(new CompressionStreamVertex
		       (this, pos, norm, (Color4f)color, stripFlag, meshFlag)) ;
    }

    /**
     * Add a mesh buffer reference to this stream.
     * @param stripFlag vertex replacement flag, either RESTART, REPLACE_OLDEST,
     * or REPLACE_MIDDLE
     * @param meshIndex index of vertex to retrieve from the mesh buffer
     */
    void addMeshReference(int stripFlag, int meshIndex) {
	stream.add(new MeshReference(stripFlag, meshIndex)) ;
    }

    /**
     * Copy the given color to the end of this stream and use it as a global
     * state change that applies to all subsequent vertices.
     */
    void addColor(Color3f c3f) {
	stream.add(new CompressionStreamColor(this, c3f)) ;
    }

    /**
     * Copy the given color to the end of this stream and use it as a global
     * state change that applies to all subsequent vertices.
     */
    void addColor(Color4f c4f) {
	stream.add(new CompressionStreamColor(this, c4f)) ;
    }

    /**
     * Copy the given normal to the end of this stream and use it as a global
     * state change that applies to all subsequent vertices.
     */
    void addNormal(Vector3f n) {
	stream.add(new CompressionStreamNormal(this, n)) ;
    }

    /**
     * Add a new position quantization value to the end of this stream that
     * will apply to all subsequent vertex positions.
     *
     * @param value number of bits to quantize each position's X, Y,
     * and Z components, ranging from 1 to 16 with a default of 16
     */
    void addPositionQuantization(int value) {
	stream.add(new PositionQuant(value)) ;
    }

    /**
     * Add a new color quantization value to the end of this stream that will
     * apply to all subsequent colors.
     *
     * @param value number of bits to quantize each color's R, G, B, and
     * alpha components, ranging from 2 to 16 with a default of 9
     */
    void addColorQuantization(int value) {
	stream.add(new ColorQuant(value)) ;
    }

    /**
     * Add a new normal quantization value to the end of this stream that will
     * apply to all subsequent normals.  This value specifies the number of
     * bits for each normal's U and V components.
     *
     * @param value number of bits for quantizing U and V, ranging from 0 to
     * 6 with a default of 6
     */
    void addNormalQuantization(int value) {
	stream.add(new NormalQuant(value)) ;
    }

    /**
     * Interface to access GeometryArray vertex components and add them to the
     * compression stream.
     * 
     * A processVertex() implementation retrieves vertex components using the
     * appropriate access semantics of a particular GeometryArray, and adds
     * them to the compression stream.
     * 
     * The implementation always pushes vertices into the mesh buffer unless
     * they match ones already there; if they do, it generates mesh buffer
     * references instead.  This reduces the number of vertices when
     * non-stripped abutting facets are added to the stream.
     * 
     * Note: Level II geometry compression semantics allow the mesh buffer
     * normals to be substituted with the value of an immediately
     * preceding SetNormal command, but this is unavailable in Level I.
     *
     * @param index vertex offset from the beginning of its data array
     * @param stripFlag RESTART, REPLACE_MIDDLE, or REPLACE_OLDEST
     */
    private interface GeometryAccessor {
	void processVertex(int index, int stripFlag) ;
    }

    /**
     * This class implements the GeometryAccessor interface for geometry
     * arrays accessed with by-copy semantics.
     */
    private class ByCopyGeometry implements GeometryAccessor {
	Point3f[] positions = null ;
	Vector3f[] normals = null ;
	Color3f[] colors3 = null ;
	Color4f[] colors4 = null ;

	ByCopyGeometry(GeometryArray ga) {
	    this(ga, ga.getInitialVertexIndex(), ga.getValidVertexCount()) ;
	}

	ByCopyGeometry(GeometryArray ga,
		       int firstVertex, int validVertexCount) {
	    int i ;
	    positions = new Point3f[validVertexCount] ;
	    for (i = 0 ; i < validVertexCount ; i++)
		positions[i] = new Point3f() ;

	    ga.getCoordinates(firstVertex, positions) ;

	    if (vertexNormals) {
		normals = new Vector3f[validVertexCount] ;
		for (i = 0 ; i < validVertexCount ; i++)
		    normals[i] = new Vector3f() ;

		ga.getNormals(firstVertex, normals) ;
	    }

	    if (vertexColor3) {
		colors3 = new Color3f[validVertexCount] ;
		for (i = 0 ; i < validVertexCount ; i++)
		    colors3[i] = new Color3f() ;

		ga.getColors(firstVertex, colors3) ;
	    }
	    else if (vertexColor4) {
		colors4 = new Color4f[validVertexCount] ;
		for (i = 0 ; i < validVertexCount ; i++)
		    colors4[i] = new Color4f() ;

		ga.getColors(firstVertex, colors4) ;
	    }
	}

	public void processVertex(int v, int stripFlag) {
	    Point3f p = positions[v] ;
	    int r = meshBuffer.getMeshReference(p) ;

	    if ((r == meshBuffer.NOT_FOUND) ||
		(vertexNormals && noMeshNormalSubstitution &&
		 (! normals[v].equals(meshBuffer.getNormal(r))))) {

		Vector3f n = vertexNormals? normals[v] : null ;
		Object c = vertexColor3? (Object)colors3[v] :
		    vertexColor4? (Object)colors4[v] : null ;

		addVertex(p, n, c, stripFlag, MESH_PUSH) ;
		meshBuffer.push(p, c, n) ;
	    }
	    else {
		if (vertexNormals && !noMeshNormalSubstitution &&
		    (! normals[v].equals(meshBuffer.getNormal(r))))
		    addNormal(normals[v]) ;

		if (vertexColor3 &&
		    (! colors3[v].equals(meshBuffer.getColor3(r))))
		    addColor(colors3[v]) ;

		else if (vertexColor4 &&
			 (! colors4[v].equals(meshBuffer.getColor4(r))))
		    addColor(colors4[v]) ;

		addMeshReference(stripFlag, r) ;
	    }
	}
    }

    /**
     * Class which holds index array references for a geometry array.
     */
    private static class IndexArrays {
	int colorIndices[] = null ;
	int normalIndices[] = null ;
	int positionIndices[] = null ;
    }

    /**
     * Retrieves index array references for the specified IndexedGeometryArray.
     * Index arrays are copied starting from initialIndexIndex.
     */
    private void getIndexArrays(GeometryArray ga, IndexArrays ia) {
	IndexedGeometryArray iga = (IndexedGeometryArray)ga ;

	int initialIndexIndex = iga.getInitialIndexIndex() ;
	int indexCount = iga.getValidIndexCount() ;
	int vertexFormat = iga.getVertexFormat() ;

	boolean useCoordIndexOnly = false ;
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
	    if (debug) System.out.println("useCoordIndexOnly") ;
	    useCoordIndexOnly = true ;
	}

	ia.positionIndices = new int[indexCount] ;
	iga.getCoordinateIndices(initialIndexIndex, ia.positionIndices) ;

	if (vertexNormals) {
	    if (useCoordIndexOnly) {
		ia.normalIndices = ia.positionIndices ;
	    }
	    else {
		ia.normalIndices = new int[indexCount] ;
		iga.getNormalIndices(initialIndexIndex, ia.normalIndices) ;
	    }
	}
	if (vertexColor3 || vertexColor4) {
	    if (useCoordIndexOnly) {
		ia.colorIndices = ia.positionIndices ;
	    }
	    else {
		ia.colorIndices = new int[indexCount] ;
		iga.getColorIndices(initialIndexIndex, ia.colorIndices) ;
	    }
	}
    }

    /**
     * Class which holds indices for a specific vertex of an
     * IndexedGeometryArray. 
     */
    private static class VertexIndices {
	int pi, ni, ci ;
    }

    /**
     * Retrieves vertex indices for a specific vertex in an
     * IndexedGeometryArray.
     */
    private void getVertexIndices(int v, IndexArrays ia, VertexIndices vi) {
	vi.pi = ia.positionIndices[v] ;
	if (vertexNormals)
	    vi.ni = ia.normalIndices[v] ;
	if (vertexColors)
	    vi.ci = ia.colorIndices[v] ;
    }

    /**
     * This class implements the GeometryAccessor interface for indexed
     * geometry arrays accessed with by-copy semantics.
     */
    private class IndexedByCopyGeometry extends ByCopyGeometry {
	IndexArrays ia = new IndexArrays() ;
	VertexIndices vi = new VertexIndices() ;

	IndexedByCopyGeometry(GeometryArray ga) {
	    super(ga, 0, ga.getVertexCount()) ;
	    getIndexArrays(ga, ia) ;
	}

	public void processVertex(int v, int stripFlag) {
	    getVertexIndices(v, ia, vi) ;
	    int r = meshBuffer.getMeshReference(vi.pi) ;

	    if ((r == meshBuffer.NOT_FOUND) ||
		(vertexNormals && noMeshNormalSubstitution &&
		 (vi.ni != meshBuffer.getNormalIndex(r)))) {

		Point3f p = positions[vi.pi] ;
		Vector3f n = vertexNormals? normals[vi.ni] : null ;
		Object c = vertexColor3? (Object)colors3[vi.ci] :
		    vertexColor4? (Object)colors4[vi.ci] : null ;

		addVertex(p, n, c, stripFlag, MESH_PUSH) ;
		meshBuffer.push(vi.pi, vi.ci, vi.ni) ;
	    }
	    else {
		if (vertexNormals && !noMeshNormalSubstitution &&
		    vi.ni != meshBuffer.getNormalIndex(r))
		    addNormal(normals[vi.ni]) ;

		if (vertexColor3 && vi.ci != meshBuffer.getColorIndex(r))
		    addColor(colors3[vi.ci]) ;
			
		else if (vertexColor4 && vi.ci != meshBuffer.getColorIndex(r))
		    addColor(colors4[vi.ci]) ;

		addMeshReference(stripFlag, r) ;
	    }
	}
    }

    //
    // NOTE: For now, copies are made of all GeometryArray vertex components
    // even when by-reference access is available.  
    //
    private static class VertexCopy {
	Object c = null ;
	Point3f p = null ;
	Vector3f n = null ;
	Color3f c3 = null ;
	Color4f c4 = null ;
    }

    private void processVertexCopy(VertexCopy vc, int stripFlag) {
	int r = meshBuffer.getMeshReference(vc.p) ;

	if ((r == meshBuffer.NOT_FOUND) ||
	    (vertexNormals && noMeshNormalSubstitution &&
	     (! vc.n.equals(meshBuffer.getNormal(r))))) {

	    addVertex(vc.p, vc.n, vc.c, stripFlag, MESH_PUSH) ;
	    meshBuffer.push(vc.p, vc.c, vc.n) ;
	}
	else {
	    if (vertexNormals && !noMeshNormalSubstitution &&
		(! vc.n.equals(meshBuffer.getNormal(r))))
		addNormal(vc.n) ;
		
	    if (vertexColor3 && (! vc.c3.equals(meshBuffer.getColor3(r))))
		addColor(vc.c3) ;

	    else if (vertexColor4 && (! vc.c4.equals(meshBuffer.getColor4(r))))
		addColor(vc.c4) ;

	    addMeshReference(stripFlag, r) ;
	}
    }

    private void processIndexedVertexCopy(VertexCopy vc,
					  VertexIndices vi,
					  int stripFlag) {

	int r = meshBuffer.getMeshReference(vi.pi) ;

	if ((r == meshBuffer.NOT_FOUND) ||
	    (vertexNormals && noMeshNormalSubstitution &&
	     (vi.ni != meshBuffer.getNormalIndex(r)))) {

	    addVertex(vc.p, vc.n, vc.c, stripFlag, MESH_PUSH) ;
	    meshBuffer.push(vi.pi, vi.ci, vi.ni) ;
	}
	else {
	    if (vertexNormals && !noMeshNormalSubstitution &&
		vi.ni != meshBuffer.getNormalIndex(r))
		addNormal(vc.n) ;

	    if (vertexColor3 && vi.ci != meshBuffer.getColorIndex(r))
		addColor(vc.c3) ;
			
	    else if (vertexColor4 && vi.ci != meshBuffer.getColorIndex(r))
		addColor(vc.c4) ;

	    addMeshReference(stripFlag, r) ;
	}
    }

    /**
     * This abstract class implements the GeometryAccessor interface for
     * concrete subclasses which handle float and NIO interleaved geometry
     * arrays.
     */
    private abstract class InterleavedGeometry implements GeometryAccessor {
	VertexCopy vc = new VertexCopy() ;

	int vstride = 0 ;
	int coffset = 0 ;
	int noffset = 0 ;
	int poffset = 0 ;
	int tstride = 0 ;
	int tcount = 0 ;

	InterleavedGeometry(GeometryArray ga) {
	    if (vertexTextures) {
		if (vertexTexture2) tstride = 2 ;
		else if (vertexTexture3) tstride = 3 ;
		else if (vertexTexture4) tstride = 4 ;

		tcount = ga.getTexCoordSetCount() ;
		vstride += tcount * tstride ;
	    }

	    if (vertexColors) {
		coffset = vstride ;
		if (vertexColor3) vstride += 3 ;
		else vstride += 4 ;
	    }

	    if (vertexNormals) {
		noffset = vstride ;
		vstride += 3 ;
	    }

	    poffset = vstride ;
	    vstride += 3 ;
	}

	abstract void copyVertex(int pi, int ni, int ci, VertexCopy vc) ;

	public void processVertex(int v, int stripFlag) {
	    copyVertex(v, v, v, vc) ;
	    processVertexCopy(vc, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for float
     * interleaved geometry arrays.
     */
    private class InterleavedGeometryFloat extends InterleavedGeometry {
	float[] vdata = null ;
	
	InterleavedGeometryFloat(GeometryArray ga) {
	    super(ga) ;
	    vdata = ga.getInterleavedVertices() ;
	}

	void copyVertex(int pi, int ni, int ci, VertexCopy vc) {
	    int voffset ;
	    voffset = pi * vstride ;
	    vc.p = new Point3f(vdata[voffset + poffset + 0],
			       vdata[voffset + poffset + 1],
			       vdata[voffset + poffset + 2]) ;

	    if (vertexNormals) {
		voffset = ni * vstride ;
		vc.n = new Vector3f(vdata[voffset + noffset + 0],
				    vdata[voffset + noffset + 1],
				    vdata[voffset + noffset + 2]) ;
	    }
	    if (vertexColor3) {
		voffset = ci * vstride ;
		vc.c3 = new Color3f(vdata[voffset + coffset + 0],
				    vdata[voffset + coffset + 1],
				    vdata[voffset + coffset + 2]) ;
		vc.c = vc.c3 ;
	    }
	    else if (vertexColor4) {
		voffset = ci * vstride ;
		vc.c4 = new Color4f(vdata[voffset + coffset + 0],
				    vdata[voffset + coffset + 1],
				    vdata[voffset + coffset + 2],
				    vdata[voffset + coffset + 3]) ;
		vc.c = vc.c4 ;
	    }
	}
    }

    /**
     * This class implements the GeometryAccessor interface for indexed
     * interleaved geometry arrays.
     */
    private class IndexedInterleavedGeometryFloat
	extends InterleavedGeometryFloat {

	IndexArrays ia = new IndexArrays() ;
	VertexIndices vi = new VertexIndices() ;

	IndexedInterleavedGeometryFloat(GeometryArray ga) {
	    super(ga) ;
	    getIndexArrays(ga, ia) ;
	}

	public void processVertex(int v, int stripFlag) {
	    getVertexIndices(v, ia, vi) ;
	    copyVertex(vi.pi, vi.ni, vi.ci, vc) ;
	    processIndexedVertexCopy(vc, vi, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for 
     * interleaved NIO geometry arrays.
     */
    private class InterleavedGeometryNIO extends InterleavedGeometry {
	FloatBufferWrapper fbw = null ;

	InterleavedGeometryNIO(GeometryArray ga) {
	    super(ga) ;
	    J3DBuffer buffer = ga.getInterleavedVertexBuffer() ;
	    if (BufferWrapper.getBufferType(buffer) ==
		BufferWrapper.TYPE_FLOAT) {
		fbw = new FloatBufferWrapper(buffer) ;
	    }
	    else {
		throw new IllegalArgumentException
		    ("\ninterleaved vertex buffer must be FloatBuffer") ;
	    }
	}

	void copyVertex(int pi, int ni, int ci, VertexCopy vc) {
	    int voffset ;
	    voffset = pi * vstride ;
	    vc.p = new Point3f(fbw.get(voffset + poffset + 0),
			       fbw.get(voffset + poffset + 1),
			       fbw.get(voffset + poffset + 2)) ;

	    if (vertexNormals) {
		voffset = ni * vstride ;
		vc.n = new Vector3f(fbw.get(voffset + noffset + 0),
				    fbw.get(voffset + noffset + 1),
				    fbw.get(voffset + noffset + 2)) ;
	    }
	    if (vertexColor3) {
		voffset = ci * vstride ;
		vc.c3 = new Color3f(fbw.get(voffset + coffset + 0),
				    fbw.get(voffset + coffset + 1),
				    fbw.get(voffset + coffset + 2)) ;
		vc.c = vc.c3 ;
	    }
	    else if (vertexColor4) {
		voffset = ci * vstride ;
		vc.c4 = new Color4f(fbw.get(voffset + coffset + 0),
				    fbw.get(voffset + coffset + 1),
				    fbw.get(voffset + coffset + 2),
				    fbw.get(voffset + coffset + 3)) ;
		vc.c = vc.c4 ;
	    }
	}
    }

    /**
     * This class implements the GeometryAccessor interface for indexed
     * interleaved NIO geometry arrays.
     */
    private class IndexedInterleavedGeometryNIO extends InterleavedGeometryNIO {
	IndexArrays ia = new IndexArrays() ;
	VertexIndices vi = new VertexIndices() ;

	IndexedInterleavedGeometryNIO(GeometryArray ga) {
	    super(ga) ;
	    getIndexArrays(ga, ia) ;
	}

	public void processVertex(int v, int stripFlag) {
	    getVertexIndices(v, ia, vi) ;
	    copyVertex(vi.pi, vi.ni, vi.ci, vc) ;
	    processIndexedVertexCopy(vc, vi, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for
     * non-interleaved geometry arrays accessed with by-reference semantics.
     */
    private class ByRefGeometry implements GeometryAccessor {
	VertexCopy vc = new VertexCopy() ;

	byte[]   colorsB    = null ;
	float[]  colorsF    = null ;
	float[]  normals    = null ;
	float[]  positionsF = null ;
	double[] positionsD = null ;

	int initialPositionIndex = 0 ;
	int initialNormalIndex   = 0 ;
	int initialColorIndex    = 0 ;

	ByRefGeometry(GeometryArray ga) {
	    positionsF = ga.getCoordRefFloat() ;
	    if (debug && positionsF != null)
		System.out.println("float positions") ;

	    positionsD = ga.getCoordRefDouble() ;
	    if (debug && positionsD != null)
		System.out.println("double positions") ;

	    if (positionsF == null && positionsD == null)
		throw new UnsupportedOperationException
		    ("\nby-reference access to Point3{d,f} arrays") ;

	    initialPositionIndex = ga.getInitialCoordIndex() ;

	    if (vertexColors) {
		colorsB = ga.getColorRefByte() ;
		if (debug && colorsB != null)
		    System.out.println("byte colors") ;

		colorsF = ga.getColorRefFloat() ;
		if (debug && colorsF != null)
		    System.out.println("float colors") ;

		if (colorsB == null && colorsF == null)
		    throw new UnsupportedOperationException
			("\nby-reference access to Color{3b,3f,4b,4f} arrays") ;

		initialColorIndex = ga.getInitialColorIndex() ;
	    }

	    if (vertexNormals) {
		normals = ga.getNormalRefFloat() ;
		if (debug && normals != null)
		    System.out.println("float normals") ;

		if (normals == null)
		    throw new UnsupportedOperationException
			("\nby-reference access to Normal3f array") ;
		
		initialNormalIndex = ga.getInitialNormalIndex() ;
	    }
	}

	void copyVertex(int pi, int ni, int ci, VertexCopy vc) {
	    pi *= 3 ;
	    if (positionsF != null) {
		vc.p = new Point3f(positionsF[pi + 0],
				   positionsF[pi + 1],
				   positionsF[pi + 2]) ;
	    }
	    else {
		vc.p = new Point3f((float)positionsD[pi + 0],
				   (float)positionsD[pi + 1],
				   (float)positionsD[pi + 2]) ;
	    }

	    ni *= 3 ;
	    if (vertexNormals) {
		vc.n = new Vector3f(normals[ni + 0],
				    normals[ni + 1],
				    normals[ni + 2]) ;
	    }

	    if (vertexColor3) {
		ci *= 3 ;
		if (colorsB != null) {
		    vc.c3 = new Color3f
			((colorsB[ci + 0] & 0xff) * ByteToFloatScale,
			 (colorsB[ci + 1] & 0xff) * ByteToFloatScale,
			 (colorsB[ci + 2] & 0xff) * ByteToFloatScale) ;
		}
		else {
		    vc.c3 = new Color3f(colorsF[ci + 0],
					colorsF[ci + 1],
					colorsF[ci + 2]) ;
		}
		vc.c = vc.c3 ;
	    }
	    else if (vertexColor4) {
		ci *= 4 ;
		if (colorsB != null) {
		    vc.c4 = new Color4f
			((colorsB[ci + 0] & 0xff) * ByteToFloatScale,
			 (colorsB[ci + 1] & 0xff) * ByteToFloatScale,
			 (colorsB[ci + 2] & 0xff) * ByteToFloatScale,
			 (colorsB[ci + 3] & 0xff) * ByteToFloatScale) ;
		}
		else {
		    vc.c4 = new Color4f(colorsF[ci + 0],
					colorsF[ci + 1],
					colorsF[ci + 2],
					colorsF[ci + 3]) ;
		}
		vc.c = vc.c4 ;
	    }
	}

	public void processVertex(int v, int stripFlag) {
	    copyVertex(v + initialPositionIndex,
		       v + initialNormalIndex,
		       v + initialColorIndex, vc) ;

	    processVertexCopy(vc, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for indexed
     * non-interleaved geometry arrays accessed with by-reference semantics.
     */
    private class IndexedByRefGeometry extends ByRefGeometry {
	IndexArrays ia = new IndexArrays() ;
	VertexIndices vi = new VertexIndices() ;

	IndexedByRefGeometry(GeometryArray ga) {
	    super(ga) ;
	    getIndexArrays(ga, ia) ;
	}

	public void processVertex(int v, int stripFlag) {
	    getVertexIndices(v, ia, vi) ;
	    copyVertex(vi.pi, vi.ni, vi.ci, vc) ;
	    processIndexedVertexCopy(vc, vi, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for
     * non-interleaved geometry arrays accessed with NIO.
     */
    private class ByRefGeometryNIO implements GeometryAccessor {
	VertexCopy vc = new VertexCopy() ;

	ByteBufferWrapper   colorsB    = null ;
	FloatBufferWrapper  colorsF    = null ;
	FloatBufferWrapper  normals    = null ;
	FloatBufferWrapper  positionsF = null ;
	DoubleBufferWrapper positionsD = null ;

	int initialPositionIndex = 0 ;
	int initialNormalIndex   = 0 ;
	int initialColorIndex    = 0 ;

	ByRefGeometryNIO(GeometryArray ga) {
	    J3DBuffer buffer ;
	    buffer = ga.getCoordRefBuffer() ;
	    initialPositionIndex = ga.getInitialCoordIndex() ;

	    switch (BufferWrapper.getBufferType(buffer)) {
	    case BufferWrapper.TYPE_FLOAT:
		positionsF = new FloatBufferWrapper(buffer) ;
		if (debug) System.out.println("float positions buffer") ;
		break ;
	    case BufferWrapper.TYPE_DOUBLE:
		positionsD = new DoubleBufferWrapper(buffer) ;
		if (debug) System.out.println("double positions buffer") ;
		break ;
	    default:
		throw new IllegalArgumentException
		    ("\nposition buffer must be FloatBuffer or DoubleBuffer") ;
	    }

	    if (vertexColors) {
		buffer = ga.getColorRefBuffer() ;
		initialColorIndex = ga.getInitialColorIndex() ;
		
		switch (BufferWrapper.getBufferType(buffer)) {
		case BufferWrapper.TYPE_BYTE:
		    colorsB = new ByteBufferWrapper(buffer) ;
		    if (debug) System.out.println("byte colors buffer") ;
		    break ;
		case BufferWrapper.TYPE_FLOAT:
		    colorsF = new FloatBufferWrapper(buffer) ;
		    if (debug) System.out.println("float colors buffer") ;
		    break ;
		default:
		    throw new IllegalArgumentException
			("\ncolor buffer must be ByteBuffer or FloatBuffer") ;
		}
	    }

	    if (vertexNormals) {
		buffer = ga.getNormalRefBuffer() ;
		initialNormalIndex = ga.getInitialNormalIndex() ;

		switch (BufferWrapper.getBufferType(buffer)) {
		case BufferWrapper.TYPE_FLOAT:
		    normals = new FloatBufferWrapper(buffer) ;
		    if (debug) System.out.println("float normals buffer") ;
		    break ;
		default:
		    throw new IllegalArgumentException
			("\nnormal buffer must be FloatBuffer") ;
		}
	    }
	}

	void copyVertex(int pi, int ni, int ci, VertexCopy vc) {
	    pi *= 3 ;
	    if (positionsF != null) {
		vc.p = new Point3f(positionsF.get(pi + 0),
				   positionsF.get(pi + 1),
				   positionsF.get(pi + 2)) ;
	    }
	    else {
		vc.p = new Point3f((float)positionsD.get(pi + 0),
				   (float)positionsD.get(pi + 1),
				   (float)positionsD.get(pi + 2)) ;
	    }

	    ni *= 3 ;
	    if (vertexNormals) {
		vc.n = new Vector3f(normals.get(ni + 0),
				    normals.get(ni + 1),
				    normals.get(ni + 2)) ;
	    }

	    if (vertexColor3) {
		ci *= 3 ;
		if (colorsB != null) {
		    vc.c3 = new Color3f
			((colorsB.get(ci + 0) & 0xff) * ByteToFloatScale,
			 (colorsB.get(ci + 1) & 0xff) * ByteToFloatScale,
			 (colorsB.get(ci + 2) & 0xff) * ByteToFloatScale) ;
		}
		else {
		    vc.c3 = new Color3f(colorsF.get(ci + 0),
					colorsF.get(ci + 1),
					colorsF.get(ci + 2)) ;
		}
		vc.c = vc.c3 ;
	    }
	    else if (vertexColor4) {
		ci *= 4 ;
		if (colorsB != null) {
		    vc.c4 = new Color4f
			((colorsB.get(ci + 0) & 0xff) * ByteToFloatScale,
			 (colorsB.get(ci + 1) & 0xff) * ByteToFloatScale,
			 (colorsB.get(ci + 2) & 0xff) * ByteToFloatScale,
			 (colorsB.get(ci + 3) & 0xff) * ByteToFloatScale) ;
		}
		else {
		    vc.c4 = new Color4f(colorsF.get(ci + 0),
					colorsF.get(ci + 1),
					colorsF.get(ci + 2),
					colorsF.get(ci + 3)) ;
		}
		vc.c = vc.c4 ;
	    }
	}

	public void processVertex(int v, int stripFlag) {
	    copyVertex(v + initialPositionIndex,
		       v + initialNormalIndex,
		       v + initialColorIndex, vc) ;

	    processVertexCopy(vc, stripFlag) ;
	}
    }

    /**
     * This class implements the GeometryAccessor interface for
     * non-interleaved indexed geometry arrays accessed with NIO.
     */
    private class IndexedByRefGeometryNIO extends ByRefGeometryNIO {
	IndexArrays ia = new IndexArrays() ;
	VertexIndices vi = new VertexIndices() ;

	IndexedByRefGeometryNIO(GeometryArray ga) {
	    super(ga) ;
	    getIndexArrays(ga, ia) ;
	}

	public void processVertex(int v, int stripFlag) {
	    getVertexIndices(v, ia, vi) ;
	    copyVertex(vi.pi, vi.ni, vi.ci, vc) ;
	    processIndexedVertexCopy(vc, vi, stripFlag) ;
	}
    }

    /**
     * Convert a GeometryArray to compression stream elements and add them to
     * this stream.
     *
     * @param ga GeometryArray to convert
     * @exception IllegalArgumentException if GeometryArray has a
     * dimensionality or vertex format inconsistent with the CompressionStream
     */
    void addGeometryArray(GeometryArray ga) {
	int firstVertex = 0 ;
	int validVertexCount = 0 ;
	int vertexFormat = ga.getVertexFormat() ;
	GeometryAccessor geometryAccessor = null ;

	if (streamType != getStreamType(ga))
	    throw new IllegalArgumentException
		("GeometryArray has inconsistent dimensionality") ;

	if (vertexComponents != getVertexComponents(vertexFormat))
	    throw new IllegalArgumentException
		("GeometryArray has inconsistent vertex components") ;

	// Set up for vertex data access semantics.
	boolean NIO = (vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0 ;
	boolean byRef = (vertexFormat & GeometryArray.BY_REFERENCE) != 0 ;
	boolean interleaved = (vertexFormat & GeometryArray.INTERLEAVED) != 0 ;
	boolean indexedGeometry = ga instanceof IndexedGeometryArray ;

	if (indexedGeometry) {
	    if (debug) System.out.println("indexed") ;
	    // Index arrays will be copied such that valid indices start at
	    // offset 0 in the copied arrays.
	    firstVertex = 0 ;
	    validVertexCount = ((IndexedGeometryArray)ga).getValidIndexCount() ;
	}

	if (!byRef) {
	    if (debug) System.out.println("by-copy") ;
	    if (indexedGeometry) {
		geometryAccessor = new IndexedByCopyGeometry(ga) ;
	    }
	    else {
		firstVertex = 0 ;
		validVertexCount = ga.getValidVertexCount() ;
		geometryAccessor = new ByCopyGeometry(ga) ;
	    }
	}
	else if (interleaved && NIO) {
	    if (debug) System.out.println("interleaved NIO") ;
	    if (indexedGeometry) {
		geometryAccessor = new IndexedInterleavedGeometryNIO(ga) ;
	    }
	    else {
		firstVertex = ga.getInitialVertexIndex() ;
		validVertexCount = ga.getValidVertexCount() ;
		geometryAccessor = new InterleavedGeometryNIO(ga) ;
	    }
	}
	else if (interleaved && !NIO) {
	    if (debug) System.out.println("interleaved") ;
	    if (indexedGeometry) {
		geometryAccessor = new IndexedInterleavedGeometryFloat(ga) ;
	    }
	    else {
		firstVertex = ga.getInitialVertexIndex() ;
		validVertexCount = ga.getValidVertexCount() ;
		geometryAccessor = new InterleavedGeometryFloat(ga) ;
	    }
	}
	else if (!interleaved && NIO) {
	    if (debug) System.out.println("non-interleaved NIO") ;
	    if (indexedGeometry) {
		geometryAccessor = new IndexedByRefGeometryNIO(ga) ;
	    }
	    else {
		firstVertex = 0 ;
		validVertexCount = ga.getValidVertexCount() ;
		geometryAccessor = new ByRefGeometryNIO(ga) ;
	    }
	}
	else if (!interleaved && !NIO) {
	    if (debug) System.out.println("non-interleaved by-ref") ;
	    if (indexedGeometry) {
		geometryAccessor = new IndexedByRefGeometry(ga) ;
	    }
	    else {
		firstVertex = 0 ;
		validVertexCount = ga.getValidVertexCount() ;
		geometryAccessor = new ByRefGeometry(ga) ;
	    }
	}

	// Set up for topology.
	int stripCount = 0 ;
	int stripCounts[] = null ;
	int constantStripLength = 0 ;
	int replaceCode = RESTART ;
	boolean strips = false ;
	boolean implicitStrips = false ;

	if (ga instanceof TriangleStripArray ||
	    ga instanceof IndexedTriangleStripArray ||
	    ga instanceof LineStripArray ||
	    ga instanceof IndexedLineStripArray) {

	    strips = true ;
	    replaceCode = REPLACE_OLDEST ;
	    if (debug) System.out.println("strips") ;
	}
	else if (ga instanceof TriangleFanArray ||
		 ga instanceof IndexedTriangleFanArray) {
	    
	    strips = true ;
	    replaceCode = REPLACE_MIDDLE ;
	    if (debug) System.out.println("fans") ;
	}
	else if (ga instanceof QuadArray ||
		 ga instanceof IndexedQuadArray) {

	    // Handled as fan arrays with 4 vertices per fan.
	    implicitStrips = true ;
	    constantStripLength = 4 ;
	    replaceCode = REPLACE_MIDDLE ;
	    if (debug) System.out.println("quads") ;
	}

	// Get strip counts.
	if (strips) {
	    if (indexedGeometry) {
		IndexedGeometryStripArray igsa ;
		igsa = (IndexedGeometryStripArray)ga ;

		stripCount = igsa.getNumStrips() ;
		stripCounts = new int[stripCount] ;
		igsa.getStripIndexCounts(stripCounts) ;

	    } else {
		GeometryStripArray gsa ;
		gsa = (GeometryStripArray)ga ;

		stripCount = gsa.getNumStrips() ;
		stripCounts = new int[stripCount] ;
		gsa.getStripVertexCounts(stripCounts) ;
	    }
	}

	// Build the compression stream for this shape's geometry.
	int v = firstVertex ;
	if (strips) {
	    for (int i = 0 ; i < stripCount ; i++) {
		geometryAccessor.processVertex(v++, RESTART) ;
		for (int j = 1 ; j < stripCounts[i] ; j++) {
		    geometryAccessor.processVertex(v++, replaceCode) ;
		}
	    }
	}
	else if (implicitStrips) {
	    while (v < firstVertex + validVertexCount) {
		geometryAccessor.processVertex(v++, RESTART) ;
		for (int j = 1 ; j < constantStripLength ; j++) {
		    geometryAccessor.processVertex(v++, replaceCode) ;
		}
	    }
	}
	else {
	    while (v < firstVertex + validVertexCount) {
		geometryAccessor.processVertex(v++, RESTART) ;
	    }
	}
    }

    /**
     * Print the stream to standard output.
     */
    void print() {
	System.out.println("\nstream has " + stream.size() + " entries") ;
	System.out.println("uncompressed size " + byteCount + " bytes") ;
	System.out.println("upper position bound: " + mcBounds[1].toString()) ;
	System.out.println("lower position bound: " + mcBounds[0].toString()) ;
	System.out.println("X, Y, Z centers (" +
			   ((float)center[0]) + " " +
			   ((float)center[1]) + " " +
			   ((float)center[2]) + ")\n" +
			   "scale " + ((float)scale) + "\n") ;

	Iterator i = stream.iterator() ;
	while (i.hasNext()) {
	    System.out.println(i.next().toString() + "\n") ;
	}
    }


    ////////////////////////////////////////////////////////////////////////////
    //									      //
    // The following constructors and methods are currently the only public   //
    // members of this class.  All other members are subject to revision.     //
    //									      //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a CompressionStream from an array of Shape3D scene graph
     * objects.  These Shape3D objects may only consist of a GeometryArray
     * component and an optional Appearance component.  The resulting stream
     * may be used as input to the GeometryCompressor methods.<p>
     *
     * Each Shape3D in the array must be of the same dimensionality (point,
     * line, or surface) and have the same vertex format as the others.
     * Texture coordinates are ignored.<p>
     *
     * If a color is specified in the material attributes for a Shape3D then
     * that color is added to the CompressionStream as the current global
     * color.  Subsequent colors as well as any colors bundled with vertices
     * will override it.  Only the material diffuse colors are used; all other
     * appearance attributes are ignored.<p>
     *
     * @param positionQuant
     * number of bits to quantize each position's X, Y,
     * and Z components, ranging from 1 to 16
     *
     * @param colorQuant
     * number of bits to quantize each color's R, G, B, and
     * alpha components, ranging from 2 to 16
     *
     * @param normalQuant
     * number of bits for quantizing each normal's U and V components, ranging
     * from 0 to 6
     *
     * @param shapes
     * an array of Shape3D scene graph objects containing
     * GeometryArray objects, all with the same vertex format and
     * dimensionality
     *
     * @exception IllegalArgumentException if any Shape3D has an inconsistent
     * dimensionality or vertex format, or if any Shape3D contains a geometry
     * component that is not a GeometryArray
     *
     * @see Shape3D
     * @see GeometryArray
     * @see GeometryCompressor
     */
    public CompressionStream(int positionQuant, int colorQuant,
			     int normalQuant, Shape3D shapes[]) {
	this() ;
	if (debug) System.out.println("CompressionStream(Shape3D[]):") ;

	if (shapes == null)
	    throw new IllegalArgumentException("null Shape3D array") ;

	if (shapes.length == 0)
	    throw new IllegalArgumentException("zero-length Shape3D array") ;

	if (shapes[0] == null)
	    throw new IllegalArgumentException("Shape3D at index 0 is null") ;

	long startTime = 0 ;
	if (benchmark) startTime = System.currentTimeMillis() ;

	Geometry g = shapes[0].getGeometry() ;
	if (! (g instanceof GeometryArray))
	    throw new IllegalArgumentException
		("Shape3D at index 0 is not a GeometryArray") ;

	GeometryArray ga = (GeometryArray)g ;
	this.streamType = getStreamType(ga) ;
	this.vertexComponents = getVertexComponents(ga.getVertexFormat()) ;

	// Add global quantization parameters to the start of the stream.
	addPositionQuantization(positionQuant) ;
	addColorQuantization(colorQuant) ;
	addNormalQuantization(normalQuant) ;

	// Loop through all shapes.
	for (int s = 0 ; s < shapes.length ; s++) {
	    if (debug) System.out.println("\nShape3D " + s + ":") ;

	    g = shapes[s].getGeometry() ;
	    if (! (g instanceof GeometryArray))
		throw new IllegalArgumentException
		    ("Shape3D at index " + s + " is not a GeometryArray") ;

	    // Check for material color and add it to the stream if it exists.
	    Appearance a = shapes[s].getAppearance() ;
	    if (a != null) {
		Material m = a.getMaterial() ;
		if (m != null) {
		    m.getDiffuseColor(c3f) ;
		    if (vertexColor4) {
			c4f.set(c3f.x, c3f.y, c3f.z, 1.0f) ;
			addColor(c4f) ;
		    } else
			addColor(c3f) ;
		}
	    }

	    // Add the geometry array to the stream.
	    addGeometryArray((GeometryArray)g) ;
	}

	if (benchmark) {
	    long t = System.currentTimeMillis() - startTime ;
	    System.out.println
		("\nCompressionStream:\n" + shapes.length + " shapes in " +
		 (t / 1000f) + " sec") ;
	}
    }

    /**
     * Creates a CompressionStream from an array of Shape3D scene graph
     * objects.  These Shape3D objects may only consist of a GeometryArray
     * component and an optional Appearance component.  The resulting stream
     * may be used as input to the GeometryCompressor methods.<p>
     *
     * Each Shape3D in the array must be of the same dimensionality (point,
     * line, or surface) and have the same vertex format as the others.
     * Texture coordinates are ignored.<p>
     *
     * If a color is specified in the material attributes for a Shape3D then
     * that color is added to the CompressionStream as the current global
     * color.  Subsequent colors as well as any colors bundled with vertices
     * will override it.  Only the material diffuse colors are used; all other
     * appearance attributes are ignored.<p>
     *
     * Defaults of 16, 9, and 6 bits are used as the quantization values for
     * positions, colors, and normals respectively.  These are the maximum
     * resolution values defined for positions and normals; the default of 9
     * for color is the equivalent of the 8 bits of RGBA component resolution
     * commonly available in graphics frame buffers.<p>
     *
     * @param shapes
     * an array of Shape3D scene graph objects containing
     * GeometryArray objects, all with the same vertex format and
     * dimensionality.
     *
     * @exception IllegalArgumentException if any Shape3D has an inconsistent
     * dimensionality or vertex format, or if any Shape3D contains a geometry
     * component that is not a GeometryArray
     *
     * @see Shape3D
     * @see GeometryArray
     * @see GeometryCompressor
     */
    public CompressionStream(Shape3D shapes[]) {
	this(16, 9, 6, shapes) ;
    }

    /**
     * Creates a CompressionStream from an array of GeometryInfo objects.  The
     * resulting stream may be used as input to the GeometryCompressor
     * methods.<p>
     *
     * Each GeometryInfo in the array must be of the same dimensionality
     * (point, line, or surface) and have the same vertex format as the
     * others.  Texture coordinates are ignored.<p>
     *
     * @param positionQuant
     * number of bits to quantize each position's X, Y,
     * and Z components, ranging from 1 to 16
     *
     * @param colorQuant
     * number of bits to quantize each color's R, G, B, and
     * alpha components, ranging from 2 to 16
     *
     * @param normalQuant
     * number of bits for quantizing each normal's U and V components, ranging
     * from 0 to 6
     *
     * @param geometry
     * an array of GeometryInfo objects, all with the same
     * vertex format and dimensionality
     *
     * @exception IllegalArgumentException if any GeometryInfo object has an
     * inconsistent dimensionality or vertex format
     *
     * @see GeometryInfo
     * @see GeometryCompressor
     */
    public CompressionStream(int positionQuant, int colorQuant,
			     int normalQuant, GeometryInfo geometry[]) {
	this() ;
	if (debug) System.out.println("CompressionStream(GeometryInfo[])") ;

	if (geometry == null)
	    throw new IllegalArgumentException("null GeometryInfo array") ;

	if (geometry.length == 0)
	    throw new IllegalArgumentException
		("zero-length GeometryInfo array") ;

	if (geometry[0] == null)
	    throw new IllegalArgumentException
		("GeometryInfo at index 0 is null") ;

	long startTime = 0 ;
	if (benchmark) startTime = System.currentTimeMillis() ;

	GeometryArray ga = geometry[0].getGeometryArray() ;
	this.streamType = getStreamType(ga) ;
	this.vertexComponents = getVertexComponents(ga.getVertexFormat()) ;

	// Add global quantization parameters to the start of the stream.
	addPositionQuantization(positionQuant) ;
	addColorQuantization(colorQuant) ;
	addNormalQuantization(normalQuant) ;

	// Loop through all GeometryInfo objects and add them to the stream.
	for (int i = 0 ; i < geometry.length ; i++) {
	    if (debug) System.out.println("\nGeometryInfo " + i + ":") ;
	    addGeometryArray(geometry[i].getGeometryArray()) ;
	}

	if (benchmark) {
	    long t = System.currentTimeMillis() - startTime ;
	    System.out.println
		("\nCompressionStream:\n" + geometry.length +
		 " GeometryInfo objects in " + (t / 1000f) + " sec") ;
	}
    }

    /**
     * Creates a CompressionStream from an array of GeometryInfo objects.  The
     * resulting stream may be used as input to the GeometryCompressor
     * methods.<p>
     *
     * Each GeometryInfo in the array must be of the same dimensionality
     * (point, line, or surface) and have the same vertex format as the
     * others.  Texture coordinates are ignored.<p>
     *
     * Defaults of 16, 9, and 6 bits are used as the quantization values for
     * positions, colors, and normals respectively.  These are the maximum
     * resolution values defined for positions and normals; the default of 9
     * for color is the equivalent of the 8 bits of RGBA component resolution
     * commonly available in graphics frame buffers.<p>
     *
     * @param geometry
     * an array of GeometryInfo objects, all with the same
     * vertex format and dimensionality
     *
     * @exception IllegalArgumentException if any GeometryInfo object has an
     * inconsistent dimensionality or vertex format
     *
     * @see GeometryInfo
     * @see GeometryCompressor
     */
    public CompressionStream(GeometryInfo geometry[]) {
	this(16, 9, 6, geometry) ;
    }

    /**
     * Get the original bounds of the coordinate data, in modeling coordinates.
     * Coordinate data is positioned and scaled to a normalized cube after
     * compression.  
     * 
     * @return Point3d array of length 2, where the 1st Point3d is the lower
     * bounds and the 2nd Point3d is the upper bounds.
     * @since Java 3D 1.3
     */
    public Point3d[] getModelBounds() {
	Point3d[] bounds = new Point3d[2] ;
	bounds[0] = new Point3d(mcBounds[0]) ;
	bounds[1] = new Point3d(mcBounds[1]) ;
	return bounds ;
    }

    /**
     * Get the bounds of the compressed object in normalized coordinates.
     * These have an maximum bounds by [-1.0 .. +1.0] across each axis.
     * 
     * @return Point3d array of length 2, where the 1st Point3d is the lower
     * bounds and the 2nd Point3d is the upper bounds.
     * @since Java 3D 1.3
     */
    public Point3d[] getNormalizedBounds() {
	Point3d[] bounds = new Point3d[2] ;
	bounds[0] = new Point3d(ncBounds[0]) ;
	bounds[1] = new Point3d(ncBounds[1]) ;
	return bounds ;
    }
}
