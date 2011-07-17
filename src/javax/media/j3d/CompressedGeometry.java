/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * The compressed geometry object is used to store geometry in a
 * compressed format. Using compressed geometry may increase the speed
 * objects can be sent over the network. Note that the geometry will
 * be decompressed in memory, so the application will not see any
 * memory savings.
 * <p>
 * Compressed geometry may be passed to this CompressedGeometry object
 * in one of two ways: by copying the data into this object using the
 * existing constructor, or by passing a reference to the data.
 * <p>
 * <ul>
 * <li>
 * <b>By Copying:</b>
 * The existing CompressedGeometry constructor copies the buffer of
 * compressed geometry data into this CompressedGeometry object.  This
 * is appropriate for many applications, and allows Java 3D to verify
 * the data once and then not worry about it again.
 * </li>
 * <li><b>By Reference:</b>
 * A new constructor and set of methods in Java 3D version 1.2 allows
 * compressed geometry data to be accessed by reference, directly from
 * the user's array.  To use this feature, you need to construct a
 * CompressedGeometry object with the <code>byReference</code> flag
 * set to <code>true</code>.  In this mode, a reference to the input
 * data is saved, but the data itself is not necessarily copied.  Note
 * that the compressed geometry header is still copied into this
 * compressed geometry object.  Data referenced by a
 * CompressedGeometry object must not be modified after the
 * CompressedGeometry object is constructed.
 * Applications
 * must exercise care not to violate this rule.  If any referenced
 * compressed geometry data is modified after construction,
 * the results are undefined.
 * </li>
 * </ul>
 *
 * @deprecated As of Java 3D version 1.4.
 */
public class CompressedGeometry extends Geometry {

    CompressedGeometryHeader cgHeader ;

    /**
     * Specifies that this CompressedGeometry object allows reading its
     * byte count information.
     */
    public static final int
    ALLOW_COUNT_READ = CapabilityBits.COMPRESSED_GEOMETRY_ALLOW_COUNT_READ ;

    /**
     * Specifies that this CompressedGeometry object allows reading its
     * header information.
     */
    public static final int
    ALLOW_HEADER_READ = CapabilityBits.COMPRESSED_GEOMETRY_ALLOW_HEADER_READ ;

    /**
     * Specifies that this CompressedGeometry object allows reading its
     * geometry data component information.
     */
    public static final int
    ALLOW_GEOMETRY_READ =
	CapabilityBits.COMPRESSED_GEOMETRY_ALLOW_GEOMETRY_READ ;

    /**
     * Specifies that this CompressedGeometry allows reading the geometry
     * data reference information for this object.  This is only used in
     * by-reference geometry mode.
     *
     * @since Java 3D 1.2
     */
    public static final int
    ALLOW_REF_DATA_READ =
	CapabilityBits.COMPRESSED_GEOMETRY_ALLOW_REF_DATA_READ;


    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_COUNT_READ,
	ALLOW_HEADER_READ,
	ALLOW_GEOMETRY_READ,
	ALLOW_REF_DATA_READ
    };

    /**
     * Package scoped default constructor for use by cloneNodeComponent.
     */
    CompressedGeometry() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Creates a new CompressedGeometry NodeComponent by copying
     * the specified compressed geometry data into this object.
     * If the version number of compressed geometry, as specified by
     * the CompressedGeometryHeader, is incompatible with the
     * supported version of compressed geometry in the current version
     * of Java 3D, then the compressed geometry object will not be
     * rendered.
     *
     * @param hdr the compressed geometry header.  This is copied
     * into the CompressedGeometry NodeComponent.
     *
     * @param compressedGeometry the compressed geometry data.  The
     * geometry must conform to the format described in Appendix B of
     * the <i>Java 3D API Specification</i>.
     *
     * @exception IllegalArgumentException if a problem is detected with the
     * header
     *
     * @see CompressedGeometryHeader
     * @see Canvas3D#queryProperties
     */ 
    public CompressedGeometry(CompressedGeometryHeader hdr,
			      byte[] compressedGeometry) {
	this(hdr, compressedGeometry, false) ;
    }

    /**
     * Creates a new CompressedGeometry NodeComponent.  The
     * specified compressed geometry data is either copied into this
     * object or is accessed by reference.
     * If the version number of compressed geometry, as specified by
     * the CompressedGeometryHeader, is incompatible with the
     * supported version of compressed geometry in the current version
     * of Java 3D, the compressed geometry object will not be
     * rendered.
     *
     * @param hdr the compressed geometry header.  This is copied
     * into the CompressedGeometry NodeComponent.
     *
     * @param compressedGeometry the compressed geometry data.  The
     * geometry must conform to the format described in Appendix B of
     * the <i>Java 3D API Specification</i>.
     *
     * @param byReference a flag that indicates whether the data is copied
     * into this compressed geometry object or is accessed by reference.
     *
     * @exception IllegalArgumentException if a problem is detected with the
     * header
     *
     * @see CompressedGeometryHeader
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.2
     */
    public CompressedGeometry(CompressedGeometryHeader hdr,
			      byte[] compressedGeometry,
			      boolean byReference) {

	if ((hdr.size + hdr.start) > compressedGeometry.length)
	    throw new IllegalArgumentException
		(J3dI18N.getString("CompressedGeometry0")) ;

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	// Create a separate copy of the given header.
	cgHeader = new CompressedGeometryHeader() ;
	hdr.copy(cgHeader) ;

	// Create the retained object.
	((CompressedGeometryRetained)this.retained).createCompressedGeometry
	    (cgHeader, compressedGeometry, byReference) ;

	// This constructor is designed to accept byte arrays that may contain
	// possibly many large compressed geometry blocks interspersed with
	// non-J3D-specific metadata.  Only one of these blocks is used per
	// CompressedGeometry object, so set the geometry offset to zero in
	// the header if the data itself is copied.
	if (!byReference)
	    cgHeader.start = 0 ;
    }

    /**
     * This constructor is not implemented.
     *
     * @exception UnsupportedOperationException this constructor is not
     * implemented
     *
     * @since Java 3D 1.3
     */
    public CompressedGeometry(CompressedGeometryHeader hdr,
			      J3DBuffer compressedGeometry) {
	throw new UnsupportedOperationException(J3dI18N.getString("CompressedGeometry9")) ;
    }


    /**
     * Returns the size, in bytes, of the compressed geometry buffer.
     * The size of the compressed geometry header is not included.
     *
     * @return the size, in bytes, of the compressed geometry buffer.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getByteCount() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_COUNT_READ))
	    throw new CapabilityNotSetException
		(J3dI18N.getString("CompressedGeometry1")) ;

	return cgHeader.size ;
    }

    /**
     * Copies the compressed geometry header from the CompressedGeometry
     * NodeComponent into the passed in parameter.
     *
     * @param hdr the CompressedGeometryHeader object into which to copy the
     * CompressedGeometry NodeComponent's header; the offset field may differ
     * from that which was originally specified if a copy of the original
     * compressed geometry byte array was created.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see CompressedGeometryHeader
     */
    public void getCompressedGeometryHeader(CompressedGeometryHeader hdr) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_HEADER_READ))
	    throw new CapabilityNotSetException
		(J3dI18N.getString("CompressedGeometry2")) ;

	cgHeader.copy(hdr) ;
    }

    /**
     * Retrieves the compressed geometry associated with the
     * CompressedGeometry NodeComponent object.  Copies the compressed
     * geometry from the CompressedGeometry node into the given array.
     * The array must be large enough to hold all of the bytes. 
     * The individual array elements must be allocated by the caller.
     *
     * @param compressedGeometry the array into which to copy the compressed
     * geometry.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data access mode for this
     * object is by-reference.
     *
     * @exception ArrayIndexOutOfBoundsException if compressedGeometry byte
     * array is not large enough to receive the compressed geometry
     */
    public void getCompressedGeometry(byte[] compressedGeometry) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_GEOMETRY_READ))
	    throw new CapabilityNotSetException
		(J3dI18N.getString("CompressedGeometry3")) ;
	
	if (isByReference())
	    throw new IllegalStateException
		(J3dI18N.getString("CompressedGeometry7")) ;

	if (cgHeader.size > compressedGeometry.length)
	    throw new ArrayIndexOutOfBoundsException
		(J3dI18N.getString("CompressedGeometry4")) ;
	
	((CompressedGeometryRetained)this.retained).copy(compressedGeometry) ;
    }

    /**
     * Decompresses the compressed geometry.  Returns an array of Shape nodes
     * containing the decompressed geometry objects, or null if the version
     * number of the compressed geometry is incompatible with the decompressor
     * in the current version of Java 3D.
     *
     * @return an array of Shape nodes containing the
     *  geometry decompressed from this CompressedGeometry NodeComponent
     *  object, or null if its version is incompatible
     *
     * @exception CapabilityNotSetException if appropriate capability is
     *  not set and this object is part of live or compiled scene graph
     */
    public Shape3D[] decompress() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_GEOMETRY_READ))
	    throw new CapabilityNotSetException
		(J3dI18N.getString("CompressedGeometry5")) ;

	CompressedGeometryRetained cgr =
	    (CompressedGeometryRetained)this.retained ;

	GeometryDecompressorShape3D decompressor =
	    new GeometryDecompressorShape3D() ;

	// Decompress the geometry as TriangleStripArrays.  A combination of
	// TriangleStripArrays and TrianglesFanArrays is more compact but
	// requires twice as many Shape3D objects, resulting in slower
	// rendering performance.
	//
	// Using TriangleArray output is currently the fastest, given the
	// strip sizes observed from various compressed geometry objects, but
	// produces about twice as many vertices.  TriangleStripArray produces
	// the same number of Shape3D objects as TriangleArray using 1/2
	// to 2/3 of the vertices, with only a marginal performance penalty.
	//
	return decompressor.toTriangleStripArrays(cgr) ;
    }


    /**
     * Retrieves the data access mode for this CompressedGeometry object.
     *
     * @return <code>true</code> if the data access mode for this
     * CompressedGeometry object is by-reference;
     * <code>false</code> if the data access mode is by-copying.
     *
     * @since Java 3D 1.2
     */
    public boolean isByReference() {
	return ((CompressedGeometryRetained)this.retained).isByReference() ;
    }


    /**
     * Gets the compressed geometry data reference.
     *
     * @return the current compressed geometry data reference.
     *
     * @exception IllegalStateException if the data access mode for this
     * object is not by-reference.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     *  not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public byte[] getCompressedGeometryRef() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ))
		throw new CapabilityNotSetException
		    (J3dI18N.getString("CompressedGeometry6")) ;

	if (!isByReference())
	    throw new IllegalStateException
		(J3dI18N.getString("CompressedGeometry8")) ;

	return ((CompressedGeometryRetained)this.retained).getReference() ;
    }


    /**
     * Gets the compressed geometry data buffer reference, which is
     * always null since NIO buffers are not supported for
     * CompressedGeometry objects.
     *
     * @return null
     *
     * @exception CapabilityNotSetException if appropriate capability is
     *  not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public J3DBuffer getCompressedGeometryBuffer() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ))
		throw new CapabilityNotSetException
		    (J3dI18N.getString("CompressedGeometry6")) ;

	return null;
    }


    /**
     * Creates the retained mode CompressedGeometryRetained object that this
     * CompressedGeometry object will point to.
     */
    void createRetained() {
  	this.retained = new CompressedGeometryRetained() ;
	this.retained.setSource(this) ;
    }

    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	CompressedGeometry cg = new CompressedGeometry() ;

	// Duplicate data specific to this class.
	cg.cgHeader = new CompressedGeometryHeader() ;
        cgHeader.copy(cg.cgHeader) ;

	// Duplicate the retained side.
	CompressedGeometryRetained cgr = (CompressedGeometryRetained)retained ;
	cgr.duplicate((CompressedGeometryRetained)cg.retained) ;

	// Duplicate superclass data and return.
        cg.duplicateNodeComponent(this) ;
        return cg ;
    }
}
