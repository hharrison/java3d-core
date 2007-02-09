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
    public void setValidIndexCount(int validIndexCount) {
	throw new UnsupportedOperationException();
    }

}
