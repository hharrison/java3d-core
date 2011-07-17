/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.*;

/**
 * The CompressedGeometrHeader object is used in conjunction with
 * the CompressedGeometry object.  The CompressedGeometrHeader object
 * contains information specific to the compressed geometry stored in
 * CompressedGeometry NodeComponent object.  This information
 * is used to aid the decompression of the compressed geometry.
 * <P>
 * All instance data is declared public and no get or set methods are
 * provided.
 *
 * @see CompressedGeometry
 *
 * @deprecated As of Java 3D version 1.4.
 */
public class CompressedGeometryHeader extends Object {

    /**
     * bufferType: compressed geometry is made up of individual points.
     */
    public static final int POINT_BUFFER = 0 ;

    /**
     * bufferType: compressed geometry is made up of line segments.
     */
    public static final int LINE_BUFFER = 1 ;

    /**
     * bufferType: compressed geometry is made up of triangles.
     */
    public static final int TRIANGLE_BUFFER = 2 ;
    
    // Valid values for the bufferDataPresent field.

    /**
     * bufferDataPresent: bit indicating that normal information is
     * bundled with the vertices in the compressed geometry buffer.
     */
    public static final int NORMAL_IN_BUFFER = 1 ;

    /**
     * bufferDataPresent: bit indicating that RGB color information is
     * bundled with the vertices in the compressed geometry buffer.
     */
    public static final int COLOR_IN_BUFFER = 2 ;

    /**
     * bufferDataPresent: bit indicating that alpha information is
     * bundled with the vertices in the compressed geometry buffer.
     */
    public static final int ALPHA_IN_BUFFER = 4 ;

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
    public int majorVersionNumber ;

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
    public int minorVersionNumber ;

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
    public int minorMinorVersionNumber ;

    /**
     * Describes the type of data in the compressed geometry buffer.
     * Only one type may be present in any given compressed geometry
     * buffer.
     */
    public int bufferType ;

    /**
     * Contains bits indicating what data is bundled with the vertices in the
     * compressed geometry buffer.  If this data is not present (e.g. color)
     * then this info will be inherited from the Appearance node.
     */
    public int bufferDataPresent ;

    /**
     * Size of the compressed geometry in bytes.
     */
    public int size ;

    /**
     * Offset in bytes of the start of the compressed geometry from the
     * beginning of the compressed geometry byte array passed to the
     * CompressedGeometry constructor. <p>
     * 
     * If the CompressedGeometry is created with reference access semantics,
     * then this allow external compressors or file readers to embed several
     * blocks of compressed geometry in a single large byte array, possibly
     * interspersed with metadata that is not specific to Java 3D, without
     * having to copy each block to a separate byte array. <p>
     *
     * If the CompressedGeometry is created with copy access semantics, then
     * <code>size</code> bytes of compressed geometry data are copied from the
     * offset indicated by <code>start</code> instead of copying the entire
     * byte array.  The getCompressedGeometry() method will return only the
     * bytes used to construct the object, and the getCompressedGeometryHeader()
     * method will return a header with the <code>start</code> field set to 0.
     */
    public int start ;

    /**
     * A point that defines the lower bound of the <i>x</i>,
     * <i>y</i>, and <i>z</i> components for all positions in the
     * compressed geometry buffer.  If null, a lower bound of
     * (-1,-1,-1) is assumed.  Java 3D will use this information to
     * construct a bounding box around compressed geometry objects
     * that are used in nodes for which the auto compute bounds flag
     * is true.  The default value for this point is null.
     *
     * @since Java 3D 1.2
     */
    public Point3d lowerBound = null ;

    /**
     * A point that defines the upper bound of the <i>x</i>,
     * <i>y</i>, and <i>z</i> components for all positions in the
     * compressed geometry buffer.  If null, an upper bound of (1,1,1)
     * is assumed.  Java 3D will use this information to construct a
     * bounding box around compressed geometry objects that are used
     * in nodes for which the auto compute bounds flag is true.  The
     * default value for this point is null.
     *
     * @since Java 3D 1.2
     */
    public Point3d upperBound = null ;

    /**
     * Creates a new CompressedGeometryHeader object used for the
     * creation of a CompressedGeometry NodeComponent object.
     * All instance data is declared public and no get or set methods are
     * provided.  All values are set to 0 by default and must be filled
     * in by the application.
     *
     * @see CompressedGeometry
     */ 
    public CompressedGeometryHeader() {
    }

    /**
     * Package-scoped method to copy current CompressedGeometryHeader object
     * to the passed-in CompressedGeometryHeader object.
     *
     * @param hdr the CompressedGeometryHeader object into which to copy the
     * current CompressedGeometryHeader.
     */
    void copy(CompressedGeometryHeader hdr) {
	hdr.majorVersionNumber = this.majorVersionNumber ;
	hdr.minorVersionNumber = this.minorVersionNumber ;
	hdr.minorMinorVersionNumber = this.minorMinorVersionNumber ;
	hdr.bufferType = this.bufferType ;
	hdr.bufferDataPresent = this.bufferDataPresent ;
	hdr.size = this.size ;
	hdr.start = this.start ;
	hdr.lowerBound = this.lowerBound ;
	hdr.upperBound = this.upperBound ;
    }

    /**
     * Returns a String describing the contents of the
     * CompressedGeometryHeader object.
     *
     * @return a String describing contents of the compressed geometry header
     */
    public String toString() {
	String type = "UNKNOWN" ;
	switch (bufferType) {
	case POINT_BUFFER:    type = "POINT_BUFFER" ;    break ;
	case LINE_BUFFER:     type = "LINE_BUFFER" ;     break ;
	case TRIANGLE_BUFFER: type = "TRIANGLE_BUFFER" ; break ;
	}
	
	String data = "" ;
	if ((bufferDataPresent & NORMAL_IN_BUFFER) != 0)
	    data = data + "NORMALS " ;
	if ((bufferDataPresent & COLOR_IN_BUFFER) != 0)
	    data = data + "COLORS " ;
	if ((bufferDataPresent & ALPHA_IN_BUFFER) != 0)
	    data = data + "ALPHA " ;
	
	String lbound = "null" ;
	if (lowerBound != null)
	    lbound = lowerBound.toString() ;

	String ubound = "null" ;
	if (upperBound != null)
	    ubound = upperBound.toString() ;

	return
	    "majorVersionNumber: "      + majorVersionNumber      + "  " +
	    "minorVersionNumber: "      + minorVersionNumber      + "  " +
	    "minorMinorVersionNumber: " + minorMinorVersionNumber + "\n" +
	    "bufferType: "              + type                    + "  " +
	    "bufferDataPresent: "       + data                    + "\n" +
	    "size: "                    + size                    + "  " +
	    "start: "                   + start                   + "\n" +
	    "lower bound: "             + lbound                  + "\n" +
	    "upper bound: "             + ubound                  + "  " ;
    }
}
