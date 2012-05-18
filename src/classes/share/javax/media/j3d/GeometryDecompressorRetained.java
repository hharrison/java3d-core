/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package javax.media.j3d;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * This class implements a retained geometry backend for the abstract
 * GeometryDecompressor.
 */
class GeometryDecompressorRetained extends GeometryDecompressor {
    private static final boolean debug = false ;
    private static final boolean benchmark = false ;
    private static final boolean statistics = false ;
    private static final boolean printInfo = debug || benchmark || statistics ;

    // Type of connections in the compressed data:
    // TYPE_POINT (1), TYPE_LINE (2), or TYPE_TRIANGLE (4).
    private int bufferDataType ;

    // Data bundled with each vertex: bitwise combination of
    // NORMAL_IN_BUFFER (1), COLOR_IN_BUFFER (2), ALPHA_IN_BUFFER (4).
    private int dataPresent ;

    // Size of the compressed geometry in bytes.
    private int size ;

    // Decompressor output state variables.
    private Color4f curColor ;
    private Vector3f curNormal ;

    // List for accumulating the output of the decompressor and converting to
    // GeometryArray representations.
    private GeneralizedVertexList vlist ;

    // Geometric bounds.
    private Point3d lbounds = new Point3d() ;
    private Point3d ubounds = new Point3d() ;

    // Decompression output constraints.  The decompressor will still process
    // all data contained in the compressed buffer, but will only retain data
    // for output subject to these booleans.
    private boolean boundsOnly = false ;
    private boolean positionsOnly = false ;

    // A very rough gauge used to initialize the size of vlist, based on
    // normal-per-vertex data collected from the HelloUniverse.cg file
    // (seagull, '57 Chevy, dinosaur).
    //
    // XXXX: get fudge values for other vertex combinations
    private static final float bytesPerVertexFudge = 5.3f ;

    // Used for benchmarking if so configured.
    private long startTime ;
    private long endTime ;

    // Private convenience copies of various constants.
    private static final int TYPE_POINT =
	CompressedGeometryRetained.TYPE_POINT ;
    private static final int TYPE_LINE =
	CompressedGeometryRetained.TYPE_LINE ;
    private static final int TYPE_TRIANGLE =
	CompressedGeometryRetained.TYPE_TRIANGLE ;
    private static final int FRONTFACE_CCW =
	GeneralizedStripFlags.FRONTFACE_CCW ;

    /**
     * If the given argument is true, sets the decompressor to output only the
     * bounding box of the decompressed geometry.
     * @param boundsOnly set to true if the decompressor should output only the
     * geometric bounding box.
     */
    void setDecompressBoundsOnly(boolean boundsOnly) {
	this.boundsOnly = boundsOnly ;
	if (boundsOnly) this.positionsOnly = false ;
    }

    /**
     * If the given argument is true, sets the decompressor to output only the
     * decompressed positions, their connections, and the bounding box.
     * @param positionsOnly set to true if the decompressor should output only
     * position, connection, and bounding box data.
     */
    void setDecompressPositionsOnly(boolean positionsOnly) {
	this.positionsOnly = positionsOnly ;
	if (positionsOnly) this.boundsOnly = false ;
    }

    /**
     * Decompress the geometry data in a CompressedGeometryRetained.  The
     * GeometryArray output is intended to be cached as retained data for
     * efficient rendering.  Global color and normal changes output by the
     * decompressor are stored as redundant per-vertex data so that a single
     * GeometryArray can be used.
     *
     * Since only one GeometryArray is created, if the bundling attributes
     * change while building the vertex list then the decompression is
     * aborted.  There should be only one SetState bundling attribute command
     * per CompressedGeometry.
     *
     * @param cgr CompressedGeometryRetained containing compressed geometry
     * @return GeometryArrayRetained containing the results of the
     * decompression, or null if only the bounds are computed.
     */
    GeometryRetained decompress(CompressedGeometryRetained cgr) {

	if (! checkVersion(cgr.majorVersionNumber, cgr.minorVersionNumber)) {
	    return null ;
	}

	vlist = null ;
	curColor = null ;
	curNormal = null ;
	lbounds.set( 1.0, 1.0, 1.0) ;
	ubounds.set(-1.0,-1.0,-1.0) ;

	// Get the descriptors for the compressed data.
	bufferDataType = cgr.bufferType ;
	dataPresent = cgr.bufferContents ;
	if (printInfo) beginPrint() ;

	// Call the superclass decompress() method which calls the output
	// methods of this subclass.  The results are stored in vlist.
	size = cgr.size ;
	super.decompress(cgr.offset, size, cgr.compressedGeometry) ;

	if (boundsOnly) {
	    if (printInfo) endPrint() ;
	    return null ;
	}

	// Convert the output to a GeometryRetained.
	GeometryArray ga ;
	switch(bufferDataType) {
	  case TYPE_TRIANGLE:
	    ga = vlist.toTriangleStripArray() ;
	    break ;
	  case TYPE_LINE:
	    ga = vlist.toLineStripArray() ;
	    break ;
	  case TYPE_POINT:
	    ga = vlist.toPointArray() ;
	    break ;
	  default:
	    throw new IllegalArgumentException
		(J3dI18N.getString("GeometryDecompressorRetained0")) ;
	}

	// Release the reference to the non-retained data.
	ga.retained.setSource(null) ;

	if (printInfo) endPrint() ;
	return (GeometryRetained)ga.retained ;
    }

    /**
     * Get the bounds of the decompressed geometry.
     * @param bb BoundingBox to receive bounds
     */
    void getBoundingBox(BoundingBox bb) {
	bb.setLower(lbounds) ;
	bb.setUpper(ubounds) ;
    }

    /**
     * Initialize the vertex output list based on the vertex format provided
     * by the SetState decompression command.
     */
    void outputVertexFormat(boolean bundlingNorm, boolean bundlingColor,
			    boolean doingAlpha) {

	if (boundsOnly) return ;

	if (vlist != null)
	    throw new IllegalStateException
		(J3dI18N.getString("GeometryDecompressorRetained1")) ;

	int vertexFormat = GeometryArray.COORDINATES ;

	if (! positionsOnly) {
	    if (bundlingNorm)  vertexFormat |= GeometryArray.NORMALS ;
	    if (bundlingColor) vertexFormat |= GeometryArray.COLOR ;
	    if (doingAlpha)    vertexFormat |= GeometryArray.WITH_ALPHA ;
	}

	vlist = new GeneralizedVertexList(vertexFormat, FRONTFACE_CCW,
					  (int)(size/bytesPerVertexFudge)) ;
    }

    /**
     * Process a decompressed vertex.
     */
    void outputVertex(Point3f position, Vector3f normal,
		      Color4f color, int vertexReplaceCode) {

	if (position.x < lbounds.x) lbounds.x = position.x ;
	if (position.y < lbounds.y) lbounds.y = position.y ;
	if (position.z < lbounds.z) lbounds.z = position.z ;

	if (position.x > ubounds.x) ubounds.x = position.x ;
	if (position.y > ubounds.y) ubounds.y = position.y ;
	if (position.z > ubounds.z) ubounds.z = position.z ;

	if (boundsOnly) return ;
	if (curColor != null) color = curColor ;
	if (curNormal != null) normal = curNormal ;

	vlist.addVertex(position, normal, color, vertexReplaceCode) ;

	if (debug) {
	    System.err.println("outputVertex: flag " + vertexReplaceCode) ;
	    System.err.println(" position " + position.toString()) ;
	    if (normal != null)
		System.err.println(" normal " + normal.toString()) ;
	    if (color != null)
		System.err.println(" color " + color.toString()) ;
	}
    }

    /**
     * Any global colors output by the decompressor are stored as per-vertex
     * color in the retained data used internally by the renderer.  This is
     * done for performance and simplicity reasons, at the expense of
     * replicating colors.
     *
     * The next method sets the current color that will be copied to each
     * succeeding vertex.  The outputColor() method is never called if
     * colors are bundled with each vertex in the compressed buffer.
     */
    void outputColor(Color4f color) {
	if (boundsOnly || positionsOnly) return ;
	if (debug) System.err.println("outputColor: " + color.toString()) ;

	if ((vlist.vertexFormat & GeometryArray.COLOR) == 0) {
	    if (vlist.size() > 0)
		throw new IllegalStateException
		    (J3dI18N.getString("GeometryDecompressorRetained2")) ;

	    vlist.setVertexFormat(vlist.vertexFormat | GeometryArray.COLOR) ;
	}

	if (curColor == null) curColor = new Color4f() ;
	curColor.set(color) ;
    }

    /**
     * Set the current normal that will be copied to each succeeding vertex
     * output by the decompressor.  The per-vertex copy is always needed since
     * in Java 3D a normal is always associated with a vertex.  This is never
     * called if normals are bundled with each vertex in the compressed
     * buffer.
     */
    void outputNormal(Vector3f normal) {
	if (boundsOnly || positionsOnly) return ;
	if (debug) System.err.println("outputNormal: " + normal.toString()) ;

	if ((vlist.vertexFormat & GeometryArray.NORMALS) == 0) {
	    if (vlist.size() > 0)
		throw new IllegalStateException
		    (J3dI18N.getString("GeometryDecompressorRetained3")) ;

	    vlist.setVertexFormat(vlist.vertexFormat | GeometryArray.NORMALS) ;
	}

	if (curNormal == null) curNormal = new Vector3f() ;
	curNormal.set(normal) ;
    }

    private void beginPrint() {
	System.err.println("\nGeometryDecompressorRetained") ;

	switch(bufferDataType) {
	  case TYPE_TRIANGLE:
	    System.err.println(" buffer TYPE_TRIANGLE") ;
 	    break ;
	  case TYPE_LINE:
	    System.err.println(" buffer TYPE_LINE") ;
	    break ;
	  case TYPE_POINT:
	    System.err.println(" buffer TYPE_POINT") ;
	    break ;
	  default:
	    throw new IllegalArgumentException
		(J3dI18N.getString("GeometryDecompressorRetained4")) ;
	}

	System.err.print(" buffer data present: coords") ;

	if ((dataPresent & CompressedGeometryHeader.NORMAL_IN_BUFFER) != 0)
	    System.err.print(" normals") ;
	if ((dataPresent & CompressedGeometryHeader.COLOR_IN_BUFFER) != 0)
	    System.err.print(" colors") ;
	if ((dataPresent & CompressedGeometryHeader.ALPHA_IN_BUFFER) != 0)
	    System.err.print(" alpha") ;

	System.err.println() ;
	if (boundsOnly) System.err.println(" computing bounds only") ;
	if (positionsOnly) System.err.println(" computing positions only") ;

	startTime = J3dClock.currentTimeMillis() ;
    }

    private void endPrint() {
	endTime = J3dClock.currentTimeMillis() ;

	if (benchmark || statistics)
	    printBench() ;

	if (statistics)
	    printStats() ;
    }

    private void printBench() {
	float t = (endTime - startTime) / 1000.0f ;

	if (boundsOnly) {
	    System.err.println(" decompression took " + t + " sec.\n") ;
	    return ;
	}

	System.err.println
	    (" decompression + strip conversion took " + t + " sec.") ;

	switch(bufferDataType) {
  	  case TYPE_POINT:
	    System.err.println
		(" decompressed " + (vlist.size()) +
		 " points at " + (vlist.size()/t) +
		 " points/sec.\n") ;
	    break ;
	  case TYPE_LINE:
	    System.err.println
		(" decompressed " + (vlist.vertexCount - vlist.stripCount) +
		 " lines at " + ((vlist.vertexCount - vlist.stripCount)/t) +
		 " lines/sec.\n") ;
	    break ;
	  case TYPE_TRIANGLE:
	      System.err.println
		  (" decompressed " +
		   (vlist.vertexCount - 2*vlist.stripCount) +
		   " triangles at " +
		   ((vlist.vertexCount - 2*vlist.stripCount)/t) +
		   " triangles/sec.\n") ;
	    break ;
	}
    }

    private void printStats() {
	System.err.println(" bounding box:\n  lower " + lbounds.toString() +
			   "\n  upper " + ubounds.toString()) ;

	if (boundsOnly) return ;

	System.err.print
	    (" number of vertices in GeometryArray output: " +
	     vlist.vertexCount + "\n" +
	     " GeometryArray vertex data present: coords") ;

	if ((vlist.vertexFormat & GeometryArray.NORMALS) != 0)
	    System.err.print(" normals") ;

	if ((vlist.vertexFormat & GeometryArray.COLOR) != 0)
	    System.err.print(" colors") ;

	if ((vlist.vertexFormat & GeometryArray.WITH_ALPHA) != 0)
	    System.err.print(" alpha") ;

	System.err.println("\n number of strips: " + vlist.stripCount) ;
	if (vlist.stripCount > 0)
	    System.err.println
		(" vertices/strip: " +
		 ((float)vlist.vertexCount / (float)vlist.stripCount)) ;
    }
}
