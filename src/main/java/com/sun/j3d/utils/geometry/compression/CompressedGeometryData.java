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

import com.sun.j3d.internal.J3dUtilsI18N;
import javax.media.j3d.J3DBuffer;
import javax.media.j3d.Shape3D;
import javax.vecmath.Point3d;

/**
 * The compressed geometry object is used to store geometry in a
 * compressed format. Using compressed geometry may increase the speed
 * objects can be sent over the network. Note that the geometry will
 * be decompressed in memory, so the application will not see any
 * memory savings.
 * <p>
 * Compressed geometry may be passed to this CompressedGeometryData object
 * in one of two ways: by copying the data into this object using the
 * existing constructor, or by passing a reference to the data.
 * <p>
 * <ul>
 * <li>
 * <b>By Copying:</b>
 * In by-copy mode, the CompressedGeometryData constructor copies the buffer of
 * compressed geometry data into this CompressedGeometryData object.  This
 * is appropriate for many applications, and allows Java 3D to verify
 * the data once and then not worry about it again.
 * </li>
 * <li><b>By Reference:</b>
 * In by-reference mode, the
 * compressed geometry data is accessed by reference, directly from
 * the user's array.  To use this feature, you need to construct a
 * CompressedGeometryData object with the <code>byReference</code> flag
 * set to <code>true</code>.  In this mode, a reference to the input
 * data is saved, but the data itself is not necessarily copied.  Note
 * that the compressed geometry header is still copied into this
 * compressed geometry object.  Data referenced by a
 * CompressedGeometryData object must not be modified after the
 * CompressedGeometryData object is constructed.
 * Applications
 * must exercise care not to violate this rule.  If any referenced
 * compressed geometry data is modified after construction,
 * the results are undefined.
 * </li>
 * </ul>
 *
 * @since Java 3D 1.5
 */
public class CompressedGeometryData extends Object {

    private Header cgHeader;
    private CompressedGeometryRetained retained;


    /**
     * Creates a new CompressedGeometryData object by copying
     * the specified compressed geometry data into this object.
     * If the version number of compressed geometry, as specified by
     * the Header, is incompatible with the
     * supported version of compressed geometry, then an exception
     * will be thrown.
     *
     * @param hdr the compressed geometry header.  This is copied
     * into this CompressedGeometryData object.
     *
     * @param compressedGeometry the compressed geometry data.  The
     * geometry must conform to the format described in Appendix B of
     * the <i>Java 3D API Specification</i>.
     *
     * @exception IllegalArgumentException if a problem is detected with the
     * header.
     */ 
    public CompressedGeometryData(Header hdr,
            byte[] compressedGeometry) {

        this(hdr, compressedGeometry, false);
    }

    /**
     * Creates a new CompressedGeometryData object.  The
     * specified compressed geometry data is either copied into this
     * object or is accessed by reference.
     * If the version number of compressed geometry, as specified by
     * the Header, is incompatible with the
     * supported version of compressed geometry, then an exception
     * will be thrown.
     *
     * @param hdr the compressed geometry header.  This is copied
     * into the CompressedGeometryData object.
     *
     * @param compressedGeometry the compressed geometry data.  The
     * geometry must conform to the format described in Appendix B of
     * the <i>Java 3D API Specification</i>.
     *
     * @param byReference a flag that indicates whether the data is copied
     * into this compressed geometry object or is accessed by reference.
     *
     * @exception IllegalArgumentException if a problem is detected with the
     * header.
     */
    public CompressedGeometryData(Header hdr,
            byte[] compressedGeometry,
            boolean byReference) {

        if ((hdr.size + hdr.start) > compressedGeometry.length) {
            throw new IllegalArgumentException(J3dUtilsI18N.getString("CompressedGeometry0"));
        }

        // Create a separate copy of the given header.
        cgHeader = new Header();
        hdr.copy(cgHeader);

        // Create the retained object.
        retained = new CompressedGeometryRetained();
        this.retained.createCompressedGeometry(cgHeader, compressedGeometry, byReference);

        // This constructor is designed to accept byte arrays that may contain
        // possibly many large compressed geometry blocks interspersed with
        // non-J3D-specific metadata.  Only one of these blocks is used per
        // CompressedGeometry object, so set the geometry offset to zero in
        // the header if the data itself is copied.
        if (!byReference)
            cgHeader.start = 0;
    }

    /**
     * Creates a new CompressedGeometryData object.  The
     * specified compressed geometry data is accessed by reference
     * from the specified buffer.
     * If the version number of compressed geometry, as specified by
     * the Header, is incompatible with the
     * supported version of compressed geometry, then an exception
     * will be thrown.
     *
     * @param hdr the compressed geometry header.  This is copied
     * into the CompressedGeometryData object.
     *
     * @param compressedGeometry a buffer containing an NIO byte buffer
     * of compressed geometry data.  The
     * geometry must conform to the format described in Appendix B of
     * the <i>Java 3D API Specification</i>.
     *
     * @exception UnsupportedOperationException this method is not
     * yet implemented
     *
     * @exception IllegalArgumentException if a problem is detected with the
     * header,
     * or if the java.nio.Buffer contained in the specified J3DBuffer
     * is not a java.nio.ByteBuffer object.
     *
     * @see Header
     */
    public CompressedGeometryData(Header hdr,
            J3DBuffer compressedGeometry) {

        throw new UnsupportedOperationException("not implemented");
    }


    /**
     * Returns the size, in bytes, of the compressed geometry buffer.
     * The size of the compressed geometry header is not included.
     *
     * @return the size, in bytes, of the compressed geometry buffer.
     */
    public int getByteCount() {
	return cgHeader.size;
    }

    /**
     * Copies the compressed geometry header from the CompressedGeometryData
     * object into the passed in parameter.
     *
     * @param hdr the Header object into which to copy the
     * CompressedGeometryData object's header; the offset field may differ
     * from that which was originally specified if a copy of the original
     * compressed geometry byte array was created.
     */
    public void getCompressedGeometryHeader(Header hdr) {
	cgHeader.copy(hdr);
    }

    /**
     * Retrieves the compressed geometry associated with the
     * CompressedGeometryData object.  Copies the compressed
     * geometry from the CompressedGeometryData node into the given array.
     * The array must be large enough to hold all of the bytes. 
     * The individual array elements must be allocated by the caller.
     *
     * @param compressedGeometry the array into which to copy the compressed
     * geometry.
     *
     * @exception IllegalStateException if the data access mode for this
     * object is by-reference.
     *
     * @exception ArrayIndexOutOfBoundsException if compressedGeometry byte
     * array is not large enough to receive the compressed geometry
     */
    public void getCompressedGeometry(byte[] compressedGeometry) {
	if (isByReference()) {
	    throw new IllegalStateException(
                    J3dUtilsI18N.getString("CompressedGeometry7"));
        }

	if (cgHeader.size > compressedGeometry.length) {
	    throw new ArrayIndexOutOfBoundsException(
                    J3dUtilsI18N.getString("CompressedGeometry4"));
        }

	this.retained.copy(compressedGeometry);
    }

    /**
     * Decompresses the compressed geometry.  Returns an array of Shape nodes
     * containing the decompressed geometry objects, or null if the version
     * number of the compressed geometry is incompatible with the decompressor
     * in the current version of Java 3D.
     *
     * @return an array of Shape nodes containing the
     * geometry decompressed from this CompressedGeometryData
     * object, or null if its version is incompatible
     */
    public Shape3D[] decompress() {
	CompressedGeometryRetained cgr = this.retained;

	GeometryDecompressorShape3D decompressor =
                new GeometryDecompressorShape3D();

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
	return decompressor.toTriangleStripArrays(cgr);
    }


    /**
     * Retrieves the data access mode for this CompressedGeometryData object.
     * 
     * @return <code>true</code> if the data access mode for this
     * CompressedGeometryData object is by-reference;
     * <code>false</code> if the data access mode is by-copying.
     */
    public boolean isByReference() {
	return this.retained.isByReference();
    }


    /**
     * Gets the compressed geometry data reference.
     *
     * @return the current compressed geometry data reference.
     *
     * @exception IllegalStateException if the data access mode for this
     * object is not by-reference.
     */
    public byte[] getCompressedGeometryRef() {
	if (!isByReference()) {
	    throw new IllegalStateException(
                    J3dUtilsI18N.getString("CompressedGeometry8"));
        }

	return this.retained.getReference();
    }


    /**
     * Gets the compressed geometry data buffer reference, which is
     * always null since NIO buffers are not supported for
     * CompressedGeometryData objects.
     * 
     * @return null
     */
    public J3DBuffer getCompressedGeometryBuffer() {
        return null;
    }


    /**
     * The Header class is a data container for the header information,
     * used in conjunction with a CompressedGeometryData object.
     * This information is used to aid the decompression of the compressed geometry.
     *
     * <p>
     * All instance data is declared public and no get or set methods are
     * provided.
     *
     * @since Java 3D 1.5
     */
    public static class Header extends Object {

        /**
         * bufferType: compressed geometry is made up of individual points.
         */
        public static final int POINT_BUFFER = 0;

        /**
         * bufferType: compressed geometry is made up of line segments.
         */
        public static final int LINE_BUFFER = 1;

        /**
         * bufferType: compressed geometry is made up of triangles.
         */
        public static final int TRIANGLE_BUFFER = 2;

        // Valid values for the bufferDataPresent field.

        /**
         * bufferDataPresent: bit indicating that normal information is
         * bundled with the vertices in the compressed geometry buffer.
         */
        public static final int NORMAL_IN_BUFFER = 1;

        /**
         * bufferDataPresent: bit indicating that RGB color information is
         * bundled with the vertices in the compressed geometry buffer.
         */
        public static final int COLOR_IN_BUFFER = 2;

        /**
         * bufferDataPresent: bit indicating that alpha information is
         * bundled with the vertices in the compressed geometry buffer.
         */
        public static final int ALPHA_IN_BUFFER = 4;

        /**
         * The major version number for the compressed geometry format that
         * was used to compress the geometry.
         * If the version number of compressed geometry is incompatible
         * with the supported version of compressed geometry in the
         * current version of Java 3D, the compressed geometry obejct will
         * not be rendered.
         *
         * @see Canvas3D#queryProperties
         */
        public int majorVersionNumber;

        /**
         * The minor version number for the compressed geometry format that
         * was used to compress the geometry.
         * If the version number of compressed geometry is incompatible
         * with the supported version of compressed geometry in the
         * current version of Java 3D, the compressed geometry obejct will
         * not be rendered.
         *
         * @see Canvas3D#queryProperties
         */
        public int minorVersionNumber;

        /**
         * The minor-minor version number for the compressed geometry format
         * that was used to compress the geometry.
         * If the version number of compressed geometry is incompatible
         * with the supported version of compressed geometry in the
         * current version of Java 3D, the compressed geometry obejct will
         * not be rendered.
         *
         * @see Canvas3D#queryProperties
         */
        public int minorMinorVersionNumber;

        /**
         * Describes the type of data in the compressed geometry buffer.
         * Only one type may be present in any given compressed geometry
         * buffer.
         */
        public int bufferType;

        /**
         * Contains bits indicating what data is bundled with the vertices in the
         * compressed geometry buffer.  If this data is not present (e.g. color)
         * then this info will be inherited from the Appearance node.
         */
        public int bufferDataPresent;

        /**
         * Size of the compressed geometry in bytes.
         */
        public int size;

        /**
         * Offset in bytes of the start of the compressed geometry from the
         * beginning of the compressed geometry byte array passed to the
         * CompressedGeometryData constructor. <p>
         *
         * If the CompressedGeometryData is created with reference access semantics,
         * then this allow external compressors or file readers to embed several
         * blocks of compressed geometry in a single large byte array, possibly
         * interspersed with metadata that is not specific to Java 3D, without
         * having to copy each block to a separate byte array. <p>
         *
         * If the CompressedGeometryData is created with copy access semantics, then
         * <code>size</code> bytes of compressed geometry data are copied from the
         * offset indicated by <code>start</code> instead of copying the entire
         * byte array.  The getCompressedGeometry() method will return only the
         * bytes used to construct the object, and the getCompressedGeometryHeader()
         * method will return a header with the <code>start</code> field set to 0.
         */
        public int start;

        /**
         * A point that defines the lower bound of the <i>x</i>,
         * <i>y</i>, and <i>z</i> components for all positions in the
         * compressed geometry buffer.  If null, a lower bound of
         * (-1,-1,-1) is assumed.  Java 3D will use this information to
         * construct a bounding box around compressed geometry objects
         * that are used in nodes for which the auto compute bounds flag
         * is true.  The default value for this point is null.
         */
        public Point3d lowerBound = null;

        /**
         * A point that defines the upper bound of the <i>x</i>,
         * <i>y</i>, and <i>z</i> components for all positions in the
         * compressed geometry buffer.  If null, an upper bound of (1,1,1)
         * is assumed.  Java 3D will use this information to construct a
         * bounding box around compressed geometry objects that are used
         * in nodes for which the auto compute bounds flag is true.  The
         * default value for this point is null.
         */
        public Point3d upperBound = null;

        /**
         * Creates a new Header object used for the
         * creation of a CompressedGeometryData object.
         * All instance data is declared public and no get or set methods are
         * provided.  All values are set to 0 by default and must be filled
         * in by the application.
         *
         * @see CompressedGeometryData
         */
        public Header() {
        }

        /**
         * Package-scoped method to copy current Header object
         * to the passed-in Header object.
         *
         * @param hdr the Header object into which to copy the
         * current Header.
         */
        void copy(Header hdr) {
            hdr.majorVersionNumber = this.majorVersionNumber;
            hdr.minorVersionNumber = this.minorVersionNumber;
            hdr.minorMinorVersionNumber = this.minorMinorVersionNumber;
            hdr.bufferType = this.bufferType;
            hdr.bufferDataPresent = this.bufferDataPresent;
            hdr.size = this.size;
            hdr.start = this.start;
            hdr.lowerBound = this.lowerBound;
            hdr.upperBound = this.upperBound;
        }

        /**
         * Returns a String describing the contents of the
         * Header object.
         *
         * @return a String describing contents of the compressed geometry header
         */
        public String toString() {
            String type = "UNKNOWN";
            switch (bufferType) {
                case POINT_BUFFER:    type = "POINT_BUFFER";    break;
                case LINE_BUFFER:     type = "LINE_BUFFER";     break;
                case TRIANGLE_BUFFER: type = "TRIANGLE_BUFFER"; break;
            }

            String data = "";
            if ((bufferDataPresent & NORMAL_IN_BUFFER) != 0)
                data = data + "NORMALS ";
            if ((bufferDataPresent & COLOR_IN_BUFFER) != 0)
                data = data + "COLORS ";
            if ((bufferDataPresent & ALPHA_IN_BUFFER) != 0)
                data = data + "ALPHA ";

            String lbound = "null";
            if (lowerBound != null)
                lbound = lowerBound.toString();

            String ubound = "null";
            if (upperBound != null)
                ubound = upperBound.toString();

            return
                    "majorVersionNumber: "      + majorVersionNumber      + "  " +
                    "minorVersionNumber: "      + minorVersionNumber      + "  " +
                    "minorMinorVersionNumber: " + minorMinorVersionNumber + "\n" +
                    "bufferType: "              + type                    + "  " +
                    "bufferDataPresent: "       + data                    + "\n" +
                    "size: "                    + size                    + "  " +
                    "start: "                   + start                   + "\n" +
                    "lower bound: "             + lbound                  + "\n" +
                    "upper bound: "             + ubound                  + "  ";
        }
    }
}
