/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d ;
import javax.vecmath.* ;

/**
 * The compressed geometry object is used to store geometry in a
 * compressed format.  Using compressed geometry reduces the amount
 * of memory needed by a Java 3D application and increases the speed
 * objects can be sent over the network.  Once geometry decompression
 * hardware support becomes available, increased rendering performance
 * will also result from the use of compressed geometry.
 */
class CompressedGeometryRetained extends GeometryRetained {

    // If not in by-reference mode, a 48-byte header as defined by the
    // GL_SUNX_geometry_compression OpenGL extension is always concatenated to
    // the beginning of the compressed geometry data and copied along with the
    // it into a contiguous array.  This allows hardware decompression using
    // the obsolete experimental GL_SUNX_geometry_compression extension if
    // that is all that is available.
    //
    // This is completely distinct and not to be confused with the cgHeader
    // field on the non-retained side, although much of the data is
    // essentially the same.
    private static final int HEADER_LENGTH = 48 ;

    // These are the header locations examined.
    private static final int HEADER_MAJOR_VERSION_OFFSET = 0 ;
    private static final int HEADER_MINOR_VERSION_OFFSET = 1 ;
    private static final int HEADER_MINOR_MINOR_VERSION_OFFSET = 2 ;
    private static final int HEADER_BUFFER_TYPE_OFFSET = 3 ;
    private static final int HEADER_BUFFER_DATA_OFFSET = 4 ;

    // The OpenGL compressed geometry extensions use bits instead of
    // enumerations to represent the type of compressed geometry.
    static final byte TYPE_POINT = 1 ;
    static final byte TYPE_LINE = 2 ;
    static final byte TYPE_TRIANGLE = 4 ;

    // Version number of this compressed geometry object.
    int majorVersionNumber ;
    int minorVersionNumber ;
    int minorMinorVersionNumber ;

    // These fields are used by the native execute() method.
    int packedVersion ;
    int bufferType ;
    int bufferContents ;
    int renderFlags ;
    int offset ;
    int size ;
    byte[] compressedGeometry ;

    // True if by-reference data access mode is in effect.
    private boolean byReference = false ;

    // A reference to the original byte array with which this object was
    // created.  If hardware decompression is available but it doesn't support
    // by-reference semantics, then an internal copy of the original byte array
    // is made even when by-reference semantics have been requested.
    private byte[] originalCompressedGeometry = null ;

    // True if the platform supports hardware decompression.
    private static boolean hardwareDecompression = false ;
    
    // This field retains a reference to the GeometryRetained object used for
    // geometry-based picking.  It is normally the same reference as the
    // mirror geometry used for rendering unless hardware decompression is
    // supported. 
    private GeometryRetained pickGeometry = null ;

    /**
     * Native method that returns availability of a native by-reference
     * rendering API for compressed geometry.
     */
    native boolean decompressByRef(long ctx) ;

    /**
     * Native method that returns availability of hardware acceleration for
     * compressed geometry of the given version.
     */
    native boolean decompressHW(long ctx, int majorVersion, int minorVersion) ;

    /**
     * Native method that does the rendering
     */
    native void execute(long  ctx, int version, int bufferType,
			int bufferContents, int renderFlags,
			int offset, int size, byte[] geometry) ;

    /**
     * Method for calling native execute() method on behalf of the J3D renderer.
     */
    void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale,
		 boolean updateAlpha, float alpha,
		 boolean multiScreen, int screen,
		 boolean ignoreVertexColors, int pass) {

	// TODO: alpha udpate
	execute(cv.ctx, packedVersion, bufferType, bufferContents,
		renderFlags, offset, size, compressedGeometry) ;
    }
  
    /**
     * The package-scoped constructor.
     */
    CompressedGeometryRetained() {
	this.geoType = GEO_TYPE_COMPRESSED ;
    
	// Compressed geometry is always bounded by [-1..1] on each axis, so
	// set that as the initial bounding box.
	geoBounds.setUpper( 1.0, 1.0, 1.0) ;
	geoBounds.setLower(-1.0,-1.0,-1.0) ;
    }
  
    /**
     * Compressed geometry is immutable so this method does nothing.
     */
    void computeBoundingBox() {
    }
  
    /**
     * Update this object.  Compressed geometry is immutable so there's
     * nothing to do.
     */
    void update() {
	isDirty = 0 ;
    }

    /**
     * Return true if the data access mode is by-reference.
     */
    boolean isByReference() {
	return this.byReference ;
    }

    private void createByCopy(byte[] geometry) {
	// Always copy a header along with the compressed geometry into a
	// contiguous array in order to support hardware acceleration with the
	// GL_SUNX_geometry_compression extension.  The header is unnecessary
	// if only the newer GL_SUN_geometry_compression API needs support.
	compressedGeometry = new byte[HEADER_LENGTH + this.size] ;
	
	compressedGeometry[HEADER_MAJOR_VERSION_OFFSET] =
	    (byte)this.majorVersionNumber ;

	compressedGeometry[HEADER_MINOR_VERSION_OFFSET] =
	    (byte)this.minorVersionNumber ;

	compressedGeometry[HEADER_MINOR_MINOR_VERSION_OFFSET] =
	    (byte)this.minorMinorVersionNumber ;

	compressedGeometry[HEADER_BUFFER_TYPE_OFFSET] =
	    (byte)this.bufferType ;

	compressedGeometry[HEADER_BUFFER_DATA_OFFSET] =
	    (byte)this.bufferContents ;
	
	System.arraycopy(geometry, this.offset,
			 compressedGeometry, HEADER_LENGTH, this.size) ;

	this.offset = HEADER_LENGTH ;
    }

    /**
     * Creates the retained compressed geometry data.  Data from the header is
     * always copied; the compressed geometry is copied as well if the data
     * access mode is not by-reference.
     *
     * @param hdr the compressed geometry header
     * @param geometry the compressed geometry
     * @param byReference if true then by-reference semantics requested
     */
    void createCompressedGeometry(CompressedGeometryHeader hdr,
				  byte[] geometry, boolean byReference) {

	this.byReference = byReference ;

	if (hdr.lowerBound != null)
	    this.geoBounds.setLower(hdr.lowerBound) ;

	if (hdr.upperBound != null)
	    this.geoBounds.setUpper(hdr.upperBound) ;

	this.centroid.set(geoBounds.getCenter());
	recompCentroid = false;
	this.majorVersionNumber = hdr.majorVersionNumber ;
	this.minorVersionNumber = hdr.minorVersionNumber ;
	this.minorMinorVersionNumber = hdr.minorMinorVersionNumber ;

	this.packedVersion =
	    (hdr.majorVersionNumber << 24) |
	    (hdr.minorVersionNumber << 16) |
	    (hdr.minorMinorVersionNumber << 8) ;

	switch(hdr.bufferType) {
	case CompressedGeometryHeader.POINT_BUFFER:
	    this.bufferType = TYPE_POINT ;
	    break ;
	case CompressedGeometryHeader.LINE_BUFFER:
	    this.bufferType = TYPE_LINE ;
	    break ;
	case CompressedGeometryHeader.TRIANGLE_BUFFER:
	    this.bufferType = TYPE_TRIANGLE ;
	    break ;
	}

	this.bufferContents = hdr.bufferDataPresent ;
	this.renderFlags = 0 ;

	this.size = hdr.size ;
	this.offset = hdr.start ;

	if (byReference) {
	    // Assume we can use the given reference, but maintain a second
	    // reference in case a copy is later needed.
	    this.compressedGeometry = geometry ;
	    this.originalCompressedGeometry = geometry ;
	} else {
	    // Copy the original data into a format that can be used by both
	    // the software and native hardware decompressors.
	    createByCopy(geometry) ;
	    this.originalCompressedGeometry = null ;
	}
    }

    /**
     * Decompress this object into a GeometryArrayRetained if hardware
     * decompression is not available.  Once decompressed the resulting
     * geometry replaces the geometry reference in the associated RenderAtom
     * as well as the mirror geometry reference in this object.
     */
    GeometryRetained getGeometry(boolean forceDecompression, Canvas3D cv ) {

	if (forceDecompression) {
	    // forceDecompression is set to true if lighting is disabled and
	    // ignoreVertexColors is true, since there is no way for openGL to
	    // ignore vertexColors in this case.
	    GeometryDecompressorRetained gdr =
		new GeometryDecompressorRetained() ;

	    mirrorGeometry = gdr.decompress(this) ;
	    gdr.getBoundingBox(geoBounds) ;
	    pickGeometry = mirrorGeometry ;
	}
	else {
	    // Return this object if hardware decompression is available.
	    if (hardwareDecompression)
		return (GeometryRetained)this ;
	
	    // Check to see if hardware decompression is available.
	    if (decompressHW(cv.ctx, majorVersionNumber, minorVersionNumber)) {
		hardwareDecompression = true ;

		// If hardware can't handle by-reference, punt to by-copy.
		if (isByReference() && !decompressByRef(cv.ctx)) {
		    createByCopy(compressedGeometry) ;
		} 

		return (GeometryRetained)this ;
	    }

	    // Decompress the data into a GeometryArrayRetained representation
	    // for the mirror geometry reference.
	    GeometryDecompressorRetained gdr =
		new GeometryDecompressorRetained() ;

	    mirrorGeometry = gdr.decompress(this) ;
	    gdr.getBoundingBox(geoBounds) ;

	    // The mirror geometry contains a superset of the pick geometry
	    // data. Since hardware decompression isn't available, there's no
	    // need to retain separate pick geometry.
	    pickGeometry = mirrorGeometry ;
	}

	return mirrorGeometry ;
    }

    /**
     * This method always decompresses the geometry and retains the result in
     * order to support geometry-based picking and collision detection.  The
     * returned GeometryRetained object will contain only positions and
     * connections.
     */
    GeometryRetained getPickGeometry() {
	// Return the pick geometry if available.
	if (pickGeometry != null)
	    return pickGeometry ;

	// Decompress the data into a GeometryArrayRetained representation for
	// the pick geometry reference.  Retain it and its bounding box.
	GeometryDecompressorRetained gdr = new GeometryDecompressorRetained() ;
	gdr.setDecompressPositionsOnly(true) ;

	pickGeometry = gdr.decompress(this) ;
	gdr.getBoundingBox(geoBounds) ;
	return pickGeometry ;
    }

    //
    // The following intersect() methods are used to implement geometry-based
    // picking and collision.
    //
    boolean intersect(PickShape pickShape, double dist[], Point3d iPnt) {
	GeometryRetained geom = getPickGeometry() ;
	return (geom != null ?
		geom.intersect(pickShape, dist, iPnt) : false);
    }

    boolean intersect(Bounds targetBound) {
	GeometryRetained geom = getPickGeometry() ;
	return (geom != null ? geom.intersect(targetBound) : false);
    }

    boolean intersect(Transform3D thisToOtherVworld,  GeometryRetained g) {
	GeometryRetained geom = getPickGeometry() ;
	return (geom != null ? 
		geom.intersect(thisToOtherVworld, g) : false);
    }

    boolean intersect(Point3d[] pnts) {
	GeometryRetained geom = getPickGeometry() ;
	return (geom != null ? geom.intersect(pnts) : false);	
    }

    /**
     * Return a vertex format mask that's compatible with GeometryArray
     * objects.
     */
    int getVertexFormat() {
	int vertexFormat = GeometryArray.COORDINATES ;

	if ((this.bufferContents &
	     CompressedGeometryHeader.NORMAL_IN_BUFFER) != 0)
	    vertexFormat |= GeometryArray.NORMALS ;
	
	if ((this.bufferContents &
	     CompressedGeometryHeader.COLOR_IN_BUFFER) != 0)
	    vertexFormat |= GeometryArray.COLOR ;
	
	if ((this.bufferContents &
	     CompressedGeometryHeader.ALPHA_IN_BUFFER) != 0)
	    vertexFormat |= GeometryArray.WITH_ALPHA ;
	
	return vertexFormat ;
    }

    /**
     * Return a buffer type that's compatible with CompressedGeometryHeader.
     */
    int getBufferType() {
	switch(this.bufferType) {
	case TYPE_POINT:
	    return CompressedGeometryHeader.POINT_BUFFER ;
	case TYPE_LINE:
	    return CompressedGeometryHeader.LINE_BUFFER ;
	default:
	case TYPE_TRIANGLE:
	    return CompressedGeometryHeader.TRIANGLE_BUFFER ;
	}
    }

    /**
     * Copies compressed geometry data into the given array of bytes.
     * The internal header information is not copied.
     *
     * @param buff array of bytes into which to copy compressed geometry
     */
    void copy(byte[] buff) {
	System.arraycopy(compressedGeometry, offset, buff, 0, size) ;
    }

    /**
     * Returns a reference to the original compressed geometry byte array,
     * which may have been copied even if by-reference semantics have been
     * requested.  It will be null if byCopy is in effect.
     *
     * @return reference to array of bytes containing the compressed geometry.
     */
    byte[] getReference() {
	return originalCompressedGeometry ;
    }

    /**
     * Copies all retained data for cloneNodeComponent() on the non-retained
     * side.  This is unlike GeometryArray subclasses which just call the
     * public API constructors and then duplicateNodeComponent() to invoke the
     * GeometryArray implementation of duplicateAttributes(), since the
     * CompressedGeometry class directly subclasses Geometry and calling the
     * public constructors would cause a lot of redundant data copying.
     */
    void duplicate(CompressedGeometryRetained cgr) {
	cgr.majorVersionNumber = this.majorVersionNumber ;
	cgr.minorVersionNumber = this.minorVersionNumber ;
	cgr.minorMinorVersionNumber = this.minorMinorVersionNumber ;

	cgr.packedVersion = this.packedVersion ;
	cgr.bufferType = this.bufferType ;
	cgr.bufferContents = this.bufferContents ;
	cgr.renderFlags = this.renderFlags ;

	cgr.offset = this.offset ;
	cgr.size= this.size ;

	cgr.geoBounds.setLower(this.geoBounds.lower) ;
	cgr.geoBounds.setUpper(this.geoBounds.upper) ;
	cgr.pickGeometry = this.pickGeometry ;
	cgr.byReference = this.byReference ;

	if (this.byReference) {
	    // Copy references only.
	    cgr.compressedGeometry = this.compressedGeometry ;
	    cgr.originalCompressedGeometry = this.originalCompressedGeometry ;
	} else {
	    // Copy entire byte array including 48-byte native OpenGL header.
	    cgr.compressedGeometry = new byte[this.compressedGeometry.length] ;
	    System.arraycopy(this.compressedGeometry, 0,
			     cgr.compressedGeometry, 0,
			     this.compressedGeometry.length) ;
	    cgr.originalCompressedGeometry = null ;
	}
    }

    int getClassType() {
	return COMPRESS_TYPE;
    }
}
