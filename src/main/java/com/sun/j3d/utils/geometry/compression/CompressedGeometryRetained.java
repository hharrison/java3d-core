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

import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PickInfo;
import javax.media.j3d.PickShape;
import javax.media.j3d.Transform3D;
import javax.vecmath.Point3d;

/**
 * The compressed geometry object is used to store geometry in a
 * compressed format.  Using compressed geometry reduces the amount
 * of memory needed by a Java 3D application and increases the speed
 * objects can be sent over the network.  Once geometry decompression
 * hardware support becomes available, increased rendering performance
 * will also result from the use of compressed geometry.
 */
class CompressedGeometryRetained extends Object {

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

    // A reference to the original byte array with which this object was
    // created.  If hardware decompression is available but it doesn't support
    // by-reference semantics, then an internal copy of the original byte array
    // is made even when by-reference semantics have been requested.
    private byte[] originalCompressedGeometry = null ;

    // Geometric bounds
    private BoundingBox geoBounds = new BoundingBox();

    // True if by-reference data access mode is in effect.
    private boolean byReference = false ;

    /**
     * The package-scoped constructor.
     */
    CompressedGeometryRetained() {
	// Compressed geometry is always bounded by [-1..1] on each axis, so
	// set that as the initial bounding box.
	geoBounds.setUpper( 1.0, 1.0, 1.0) ;
	geoBounds.setLower(-1.0,-1.0,-1.0) ;
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
    void createCompressedGeometry(CompressedGeometryData.Header hdr,
				  byte[] geometry, boolean byReference) {

	this.byReference = byReference ;

	if (hdr.lowerBound != null)
	    this.geoBounds.setLower(hdr.lowerBound) ;

	if (hdr.upperBound != null)
	    this.geoBounds.setUpper(hdr.upperBound) ;

////	this.centroid.set(geoBounds.getCenter());
////	recompCentroid = false;
	this.majorVersionNumber = hdr.majorVersionNumber ;
	this.minorVersionNumber = hdr.minorVersionNumber ;
	this.minorMinorVersionNumber = hdr.minorMinorVersionNumber ;

	this.packedVersion =
	    (hdr.majorVersionNumber << 24) |
	    (hdr.minorVersionNumber << 16) |
	    (hdr.minorMinorVersionNumber << 8) ;

	switch(hdr.bufferType) {
	case CompressedGeometryData.Header.POINT_BUFFER:
	    this.bufferType = TYPE_POINT ;
	    break ;
	case CompressedGeometryData.Header.LINE_BUFFER:
	    this.bufferType = TYPE_LINE ;
	    break ;
	case CompressedGeometryData.Header.TRIANGLE_BUFFER:
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
	    this.compressedGeometry = geometry;
            this.originalCompressedGeometry = geometry;
	} else {
	    // Copy the original data into a format that can be used by both
	    // the software and native hardware decompressors.
	    createByCopy(geometry);
            this.originalCompressedGeometry = null;
	}
    }

    /**
     * Return a vertex format mask that's compatible with GeometryArray
     * objects.
     */
    int getVertexFormat() {
	int vertexFormat = GeometryArray.COORDINATES;

	if ((bufferContents & CompressedGeometryData.Header.NORMAL_IN_BUFFER) != 0) {
	    vertexFormat |= GeometryArray.NORMALS;
        }
	
	if ((bufferContents & CompressedGeometryData.Header.COLOR_IN_BUFFER) != 0) {
            if ((bufferContents & CompressedGeometryData.Header.ALPHA_IN_BUFFER) != 0) {
                vertexFormat |= GeometryArray.COLOR_4;
            } else {
                vertexFormat |= GeometryArray.COLOR_3;
            }
        }

	return vertexFormat ;
    }

    /**
     * Return a buffer type that's compatible with CompressedGeometryData.Header.
     */
    int getBufferType() {
	switch(this.bufferType) {
	case TYPE_POINT:
	    return CompressedGeometryData.Header.POINT_BUFFER ;
	case TYPE_LINE:
	    return CompressedGeometryData.Header.LINE_BUFFER ;
	default:
	case TYPE_TRIANGLE:
	    return CompressedGeometryData.Header.TRIANGLE_BUFFER ;
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

}
