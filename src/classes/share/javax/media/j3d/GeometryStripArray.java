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
     * @param vertexCount the number of vertex elements in this array
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: COORDINATES, to signal the inclusion of
     * vertex positions--always present; NORMALS, to signal 
     * the inclusion of per vertex normals; one of COLOR_3,
     * COLOR_4, to signal the inclusion of per vertex
     * colors (without or with color information); and one of 
     * TEXTURE_COORDINATE_2 or TEXTURE_COORDINATE_3, to signal the
     * inclusion of per-vertex texture coordinates 2D or 3D.
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validVertexCount > vertexCount</code>
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
     * @param vertexCount the number of vertex elements in this array<p>
     *
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: COORDINATES, to signal the inclusion of
     * vertex positions--always present; NORMALS, to signal 
     * the inclusion of per vertex normals; one of COLOR_3,
     * COLOR_4, to signal the inclusion of per vertex
     * colors (without or with color information); and one of 
     * TEXTURE_COORDINATE_2 or TEXTURE_COORDINATE_3, to signal the
     * inclusion of per-vertex texture coordinates 2D or 3D.<p>
     *
     * @param texCoordSetCount the number of texture coordinate sets
     * in this GeometryArray object.  If <code>vertexFormat</code>
     * does not include one of <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code>, the
     * <code>texCoordSetCount</code> parameter is not used.<p>
     *
     * @param texCoordSetMap an array that maps texture coordinate
     * sets to texture units.  The array is indexed by texture unit
     * number for each texture unit in the associated Appearance
     * object.  The values in the array specify the texture coordinate
     * set within this GeometryArray object that maps to the
     * corresponding texture
     * unit.  All elements within the array must be less than
     * <code>texCoordSetCount</code>.  A negative value specifies that
     * no texture coordinate set maps to the texture unit
     * corresponding to the index.  If there are more texture units in
     * any associated Appearance object than elements in the mapping
     * array, the extra elements are assumed to be -1.  The same
     * texture coordinate set may be used for more than one texture
     * unit.  Each texture unit in every associated Appearance must
     * have a valid source of texture coordinates: either a
     * non-negative texture coordinate set must be specified in the
     * mapping array or texture coordinate generation must be enabled.
     * Texture coordinate generation will take precedence for those
     * texture units for which a texture coordinate set is specified
     * and texture coordinate generation is enabled.  If
     * <code>vertexFormat</code> does not include one of
     * <code>TEXTURE_COORDINATE_2</code> or
     * <code>TEXTURE_COORDINATE_3</code>, the
     * <code>texCoordSetMap</code> array is not used.<p>
     *
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     * The sum of the elements in this array defines the total number
     * of valid vertices that are rendered (validVertexCount).
     *
     * @exception IllegalArgumentException if
     * <code>validVertexCount > vertexCount</code>
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
