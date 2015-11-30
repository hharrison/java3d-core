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

package org.jogamp.java3d;

/**
 * The IndexedGeometryStripArray object is an abstract class that is extended for
 * a set of IndexedGeometryArray strip primitives.  These include LINE_STRIP,
 * TRIANGLE_STRIP, and TRIANGLE_FAN.
 */

public abstract class IndexedGeometryStripArray extends IndexedGeometryArray {

    // non-public, no parameter constructor
    IndexedGeometryStripArray() {}

    /**
     * Constructs an empty IndexedGeometryStripArray object with the specified
     * number of vertices, vertex format, number of indices, and
     * array of per-strip index counts.
     *
     * @param vertexCount
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param vertexFormat
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param indexCount
     * see {@link IndexedGeometryArray#IndexedGeometryArray(int,int,int)}
     * for a description of this parameter.
     *
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid indexed vertices that are rendered (validIndexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validIndexCount &gt; indexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public IndexedGeometryStripArray(int vertexCount,
				     int vertexFormat,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat, indexCount);
	((IndexedGeometryStripArrayRetained)this.retained).
	    setStripIndexCounts(stripIndexCounts);
    }

    /**
     * Constructs an empty IndexedGeometryStripArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, number of indices, and
     * array of per-strip index counts.
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
     * @param indexCount
     * see {@link IndexedGeometryArray#IndexedGeometryArray(int,int,int,int[],int)}
     * for a description of this parameter.
     *
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid indexed vertices that are rendered (validIndexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validIndexCount &gt; indexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedGeometryStripArray(int vertexCount,
				     int vertexFormat,
				     int texCoordSetCount,
				     int[] texCoordSetMap,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount);
	((IndexedGeometryStripArrayRetained)this.retained).
	    setStripIndexCounts(stripIndexCounts);
    }

    /**
     * Constructs an empty IndexedGeometryStripArray object with the
     * specified number of vertices, vertex format, number of texture
     * coordinate sets, texture coordinate mapping array, vertex
     * attribute count, vertex attribute sizes array, number of
     * indices, and array of per-strip index counts.
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
     * @param indexCount
     * see {@link IndexedGeometryArray#IndexedGeometryArray(int,int,int,int[],int,int[],int)}
     * for a description of this parameter.
     *
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid indexed vertices that are rendered (validIndexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validIndexCount &gt; indexCount</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedGeometryStripArray(int vertexCount,
				     int vertexFormat,
				     int texCoordSetCount,
				     int[] texCoordSetMap,
				     int vertexAttrCount,
				     int[] vertexAttrSizes,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes,
	      indexCount);

	((IndexedGeometryStripArrayRetained)this.retained).
	    setStripIndexCounts(stripIndexCounts);
    }

  /**
   * Get number of strips in the GeometryStripArray
   * @return numStrips number of strips
   */
    public int getNumStrips(){
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryStripArray0"));

	return ((IndexedGeometryStripArrayRetained)this.retained).getNumStrips();
    }

    /**
     * Sets the array of strip index counts.  The length of this
     * array is the number of separate strips.  The elements in this
     * array specify the number of indices for each separate strip.
     * The sum of the elements in this array defines the total number
     * of valid indexed vertices that are rendered (validIndexCount).
     *
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     *
     * @exception IllegalArgumentException if
     * <code>initialIndexIndex + validIndexCount > indexCount</code>
     *
     * @since Java 3D 1.3
     */
    public void setStripIndexCounts(int[] stripIndexCounts) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryStripArray2"));

	((IndexedGeometryStripArrayRetained)this.retained).setStripIndexCounts(stripIndexCounts);

    }

    /**
     * Gets a list of indexCounts for each strip. The list is
     * copied into the specified array. The array must be
     * large enough to hold all of the ints.
     * @param stripIndexCounts an array that will receive indexCounts
     */
    public void getStripIndexCounts(int[] stripIndexCounts) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(GeometryArray.ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryStripArray1"));

	((IndexedGeometryStripArrayRetained)this.retained).
	    getStripIndexCounts(stripIndexCounts);
    }

    /**
     * This method is not supported for indexed geometry strip arrays.
     * The sum of the elements in the strip index counts array defines
     * the valid index count.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    @Override
    public void setValidIndexCount(int validIndexCount) {
	throw new UnsupportedOperationException();
    }

}
