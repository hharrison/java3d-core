/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The GeometryStripArray object is an abstract class that is extended for
 * a set of GeometryArray strip primitives.  These include LINE_STRIP,
 * TRIANGLE_STRIP, and TRIANGLE_FAN. In addition to specifying the array
 * of vertex elements, which is inherited from GeometryArray, the
 * GeometryStripArray class specifies the number of strips and an
 * array of per-strip vertex counts that specify where the separate strips
 * appear in the vertex array.
 */

public abstract class GeometryStripArray extends GeometryArray {

    // non-public, no parameter constructor
    GeometryStripArray() {}

    /**
     * Constructs an empty GeometryStripArray object with the specified
     * number of vertices, vertex format, and
     * array of per-strip vertex counts.
     *
     * @param vertexCount
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param vertexFormat
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validVertexCount &gt; vertexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public GeometryStripArray(int vertexCount,
			      int vertexFormat,
			      int[] stripVertexCounts) {

	super(vertexCount, vertexFormat);
	((GeometryStripArrayRetained)this.retained).setStripVertexCounts(stripVertexCounts);
    }

    /**
     * Constructs an empty GeometryStripArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, and
     * array of per-strip vertex counts.
     *
     * @param vertexCount
     * see {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for a description of this parameter.
     *
     * @param vertexFormat
     * see {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for a description of this parameter.
     *
     * @param texCoordSetCount
     * see {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for a description of this parameter.
     *
     * @param texCoordSetMap
     * see {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for a description of this parameter.
     *
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validVertexCount &gt; vertexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public GeometryStripArray(int vertexCount,
			      int vertexFormat,
			      int texCoordSetCount,
			      int[] texCoordSetMap,
			      int[] stripVertexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap);
	((GeometryStripArrayRetained)this.retained).setStripVertexCounts(stripVertexCounts);
    }

    /**
     * Constructs an empty GeometryStripArray object with the
     * specified number of vertices, vertex format, number of texture
     * coordinate sets, texture coordinate mapping array, vertex
     * attribute count, vertex attribute sizes array, and array of
     * per-strip vertex counts.
     *
     * @param vertexCount
     * see {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @param vertexFormat
     * see {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @param texCoordSetMap
     * see {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @param vertexAttrCount
     * see {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @param vertexAttrSizes
     * see {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validVertexCount &gt; vertexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public GeometryStripArray(int vertexCount,
			      int vertexFormat,
			      int texCoordSetCount,
			      int[] texCoordSetMap,
			      int vertexAttrCount,
			      int[] vertexAttrSizes,
			      int[] stripVertexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes);

	((GeometryStripArrayRetained)this.retained).setStripVertexCounts(stripVertexCounts);
    }

    /**
     * Get number of strips in the GeometryStripArray.
     * @return numStrips number of strips
     */
    public int getNumStrips() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryStripArray0"));

	return ((GeometryStripArrayRetained)this.retained).getNumStrips();
    }

    /**
     * Sets the array of strip vertex counts.  The length of this
     * array is the number of separate strips.  The elements in this
     * array specify the number of vertices for each separate strip.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     *
     * @exception IllegalArgumentException if any of the following are
     * true:
     * <ul>
     * <code>initialVertexIndex + validVertexCount > vertexCount</code>,<br>
     * <code>initialCoordIndex + validVertexCount > vertexCount</code>,<br>
     * <code>initialColorIndex + validVertexCount > vertexCount</code>,<br>
     * <code>initialNormalIndex + validVertexCount > vertexCount</code>,<br>
     * <code>initialTexCoordIndex + validVertexCount > vertexCount</code>
     * </ul>
     * <p>
     *
     * @exception ArrayIndexOutOfBoundsException if the geometry data format
     * is <code>BY_REFERENCE</code> and any the following
     * are true for non-null array references:
     * <ul>
     * <code>CoordRef.length</code> < <i>num_words</i> *
     * (<code>initialCoordIndex + validVertexCount</code>)<br>
     * <code>ColorRef.length</code> < <i>num_words</i> *
     * (<code>initialColorIndex + validVertexCount</code>)<br>
     * <code>NormalRef.length</code> < <i>num_words</i> *
     * (<code>initialNormalIndex + validVertexCount</code>)<br>
     * <code>TexCoordRef.length</code> < <i>num_words</i> *
     * (<code>initialTexCoordIndex + validVertexCount</code>)<br>
     * </ul>
     * where <i>num_words</i> depends on which variant of
     * <code>set</code><i>Array</i><code>Ref</code> is used.
     *
     * @since Java 3D 1.3
     */
    public void setStripVertexCounts(int[] stripVertexCounts) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryStripArray2"));

	((GeometryStripArrayRetained)this.retained).setStripVertexCounts(stripVertexCounts);

    }

    /**
     * Get a list of vertexCounts for each strip. The list is copied
     * into the specified array. The array must be large enough to hold
     * all of the ints.
     * @param stripVertexCounts an array that will receive vertexCounts
     */
    public void getStripVertexCounts(int[] stripVertexCounts) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryStripArray1"));

	((GeometryStripArrayRetained)this.retained).getStripVertexCounts(stripVertexCounts);
    }

    /**
     * This method is not supported for geometry strip arrays.
     * The sum of the elements in the strip vertex counts array
     * defines the valid vertex count.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setValidVertexCount(int validVertexCount) {
	throw new UnsupportedOperationException();
    }

}
