/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * The IndexedGeometryArray object contains separate integer arrays
 * that index into the arrays of positional coordinates, colors,
 * normals, and texture coordinates.  These index arrays specify how
 * vertices are connected to form geometry primitives.  This class is
 * extended to create the various indexed primitive types (e.g.,
 * lines, triangle strips, etc.).
 */

public abstract class IndexedGeometryArray extends GeometryArray {

    // non-public, no parameter constructor
    IndexedGeometryArray() {}
    
  /**
   * Specifies that this IndexedGeometryArray allows reading the array of
   * coordinate indices.
   */
  public static final int
    ALLOW_COORDINATE_INDEX_READ = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_COORDINATE_INDEX_READ;

  /**
   * Specifies that this IndexedGeometryArray allows writing the array of
   * coordinate indices.
   */
  public static final int
    ALLOW_COORDINATE_INDEX_WRITE = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_COORDINATE_INDEX_WRITE;

  /**
   * Specifies that this IndexedGeometryArray allows reading the array of
   * color indices.
   */
  public static final int
    ALLOW_COLOR_INDEX_READ = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_COLOR_INDEX_READ;

  /**
   * Specifies that this IndexedGeometryArray allows writing the array of
   * color indices.
   */
  public static final int
    ALLOW_COLOR_INDEX_WRITE = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_COLOR_INDEX_WRITE;

  /**
   * Specifies that this IndexedGeometryArray allows reading the array of
   * normal indices.
   */
  public static final int
    ALLOW_NORMAL_INDEX_READ = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_NORMAL_INDEX_READ;

  /**
   * Specifies that this IndexedGeometryArray allows writing the array of
   * normal indices.
   */
  public static final int
    ALLOW_NORMAL_INDEX_WRITE = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_NORMAL_INDEX_WRITE;

  /**
   * Specifies that this IndexedGeometryArray allows reading the array of
   * texture coordinate indices.
   */
  public static final int
    ALLOW_TEXCOORD_INDEX_READ = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_TEXCOORD_INDEX_READ;

  /**
   * Specifies that this IndexedGeometryArray allows writing the array of
   * texture coordinate indices.
   */
  public static final int
    ALLOW_TEXCOORD_INDEX_WRITE = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_TEXCOORD_INDEX_WRITE;

    /**
     * Constructs an empty IndexedGeometryArray object with the specified
     * number of vertices, vertex format, and number of indices.
     * Defaults are used for all other parameters.  The default values
     * are as follows:
     *
     * <ul>
     * validIndexCount : indexCount<br>
     * initialIndexIndex : 0<br>
     * all index array values : 0<br>
     * </ul>
     *
     * @param vertexCount the number of vertex elements in this
     * IndexedGeometryArray
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: COORDINATES, to signal the inclusion of
     * vertex positions--always present; NORMALS, to signal 
     * the inclusion of per vertex normals; one of COLOR_3,
     * COLOR_4, to signal the inclusion of per vertex
     * colors (without or with color information); one of 
     * TEXTURE_COORDINATE_2, TEXTURE_COORDINATE_3 or
     * TEXTURE_COORDINATE_4, to signal the
     * inclusion of per-vertex texture coordinates 2D, 3D or 4D.
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     */
    public IndexedGeometryArray(int vertexCount,
				int vertexFormat,
				int indexCount) {
	super(vertexCount, vertexFormat);
	((IndexedGeometryArrayRetained)this.retained).createIndexedGeometryArrayData(indexCount);
    }

    /**
     * Constructs an empty IndexedGeometryArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, and number of indices.
     * Defaults are used for all other parameters.
     *
     * @param vertexCount the number of vertex elements in this
     * IndexedGeometryArray<p>
     *
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: COORDINATES, to signal the inclusion of
     * vertex positions--always present; NORMALS, to signal 
     * the inclusion of per vertex normals; one of COLOR_3,
     * COLOR_4, to signal the inclusion of per vertex
     * colors (without or with color information); one of 
     * TEXTURE_COORDINATE_2, TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4,
     * to signal the
     * inclusion of per-vertex texture coordinates 2D , 3D or 4D.<p>
     *
     * @param texCoordSetCount the number of texture coordinate sets
     * in this GeometryArray object.  If <code>vertexFormat</code>
     * does not include one of <code>TEXTURE_COORDINATE_2</code>,
     * <code>TEXTURE_COORDINATE_3</code> or
     * <code>TEXTURE_COORDINATE_4</code>, the
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
     * <code>TEXTURE_COORDINATE_2</code>,
     * <code>TEXTURE_COORDINATE_3</code> or
     * <code>TEXTURE_COORDINATE_4</code>, the
     * <code>texCoordSetMap</code> array is not used.<p>
     *
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     *
     * @since Java 3D 1.2
     */
    public IndexedGeometryArray(int vertexCount,
				int vertexFormat,
				int texCoordSetCount,
				int[] texCoordSetMap,
				int indexCount) {
	super(vertexCount, vertexFormat, texCoordSetCount, texCoordSetMap);
	((IndexedGeometryArrayRetained)this.retained).createIndexedGeometryArrayData(indexCount);
    }

  /**
   * Gets number of indices for this IndexedGeometryArray.
   * @return indexCount the number of indices
   */
  public int getIndexCount(){
    if (isLiveOrCompiled())
        if(!this.getCapability(GeometryArray.ALLOW_COUNT_READ))
          throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray0"));

	return ((IndexedGeometryArrayRetained)this.retained).getIndexCount();
  }

    /**
     * Sets the valid index count for this IndexedGeometryArray object.
     * This count specifies the number of indexed vertices actually used
     * in rendering or other operations such as picking and collision.
     * This attribute is initialized to <code>indexCount</code>.
     *
     * @param validIndexCount the new valid index count.
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @exception IllegalArgumentException if either of the following is true:
     * <ul>
     * <code>validIndexCount < 0</code>, or<br>
     * <code>initialIndexIndex + validIndexCount > indexCount</code><br>
     * </ul>
     *
     * @exception ArrayIndexOutOfBoundsException if any element in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the index array associated with any of the enabled vertex
     * components (coord, color, normal, texcoord) is out of range.
     * An element is out of range if it is less than 0 or is greater
     * than or equal to the number of vertices actually defined for
     * the particular component's array.
     *
     * @since Java 3D 1.3
     */
    public void setValidIndexCount(int validIndexCount) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray16"));

	((IndexedGeometryArrayRetained)this.retained).setValidIndexCount(validIndexCount);
    }

    /**
     * Gets the valid index count for this IndexedGeometryArray
     * object.  For geometry strip primitives (subclasses of
     * IndexedGeometryStripArray), the valid index count is defined
     * to be the sum of the stripIndexCounts array.
     *
     * @return the current valid index count
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getValidIndexCount() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray17"));

	return ((IndexedGeometryArrayRetained)this.retained).getValidIndexCount();
    }

    /**
     * Sets the initial index index for this IndexedGeometryArray object.
     * This index specifies the first index within this indexed geometry
     * array that is actually used in rendering or other operations
     * such as picking and collision.  This attribute is initialized
     * to 0.
     *
     * @param initialIndexIndex the new initial index index.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     * @exception IllegalArgumentException if either of the following is true:
     * <ul>
     * <code>initialIndexIndex < 0</code>, or<br>
     * <code>initialIndexIndex + validIndexCount > indexCount</code><br>
     * </ul>
     *
     * @exception ArrayIndexOutOfBoundsException if any element in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * in the index array associated with any of the enabled vertex
     * components (coord, color, normal, texcoord) is out of range.
     * An element is out of range if it is less than 0 or is greater
     * than or equal to the number of vertices actually defined for
     * the particular component's array.
     *
     * @since Java 3D 1.3
     */
    public void setInitialIndexIndex(int initialIndexIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray18"));

        if (initialIndexIndex < 0)
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedGeometryArray20"));


	((IndexedGeometryArrayRetained)this.retained).setInitialIndexIndex(initialIndexIndex);
    }

    /**
     * Gets the initial index index for this IndexedGeometryArray object.
     * @return the current initial index index
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this object is part of a live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getInitialIndexIndex() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COUNT_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray19"));

     return ((IndexedGeometryArrayRetained)this.retained).getInitialIndexIndex();

    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     * The initialIndexIndex attribute can be used to set the starting
     * index within the index arrays.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setInitialVertexIndex(int initialVertexIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setInitialCoordIndex(int initialCoordIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setInitialColorIndex(int initialColorIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setInitialNormalIndex(int initialNormalIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setInitialTexCoordIndex(int texCoordSet,
					int initialTexCoordIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     * The validIndexCount attribute can be used to set the number of
     * valid indexed vertices rendered.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setValidVertexCount(int validVertexCount) {
	throw new UnsupportedOperationException();
    }


    /**
     * Sets the coordinate index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndex the new coordinate index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if index is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * and the specified coordinateIndex is out of range.  The
     * coordinateIndex is out of range if it is less than 0 or is
     * greater than or equal to the number of vertices actually
     * defined for the coordinate array.
     */
  public void setCoordinateIndex(int index, int coordinateIndex) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray1"));
  
    ((IndexedGeometryArrayRetained)this.retained).setCoordinateIndex(index, coordinateIndex);
  }

    /**
     * Sets the coordinate indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndices an array of coordinate indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * coordinateIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the coordinate array.
     */
  public void setCoordinateIndices(int index, int coordinateIndices[]) {
    if (isLiveOrCompiled())
	if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray1"));
  
    ((IndexedGeometryArrayRetained)this.retained).setCoordinateIndices(index, coordinateIndices);
  }

    /**
     * Sets the color index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param colorIndex the new color index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if index is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * and the specified colorIndex is out of range.  The
     * colorIndex is out of range if it is less than 0 or is
     * greater than or equal to the number of vertices actually
     * defined for the color array.
     */
  public void setColorIndex(int index, int colorIndex) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray3"));
  
    ((IndexedGeometryArrayRetained)this.retained).setColorIndex(index, colorIndex);
  }

    /**
     * Sets the color indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param colorIndices an array of color indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * colorIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the color array.
     */
  public void setColorIndices(int index, int colorIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray3"));
  
    ((IndexedGeometryArrayRetained)this.retained).setColorIndices(index, colorIndices);
  }

    /**
     * Sets the normal index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param normalIndex the new normal index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if index is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * and the specified normalIndex is out of range.  The
     * normalIndex is out of range if it is less than 0 or is
     * greater than or equal to the number of vertices actually
     * defined for the normal array.
     */
  public void setNormalIndex(int index, int normalIndex) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray5"));
  
    ((IndexedGeometryArrayRetained)this.retained).setNormalIndex(index, normalIndex);
  }

    /**
     * Sets the normal indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param normalIndices an array of normal indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if index is less than 0
     * or is greater than or equal to indexCount
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * normalIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the normal array.
     */
  public void setNormalIndices(int index, int normalIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray5"));
  
    ((IndexedGeometryArrayRetained)this.retained).setNormalIndices(index, normalIndices);
  }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinateIndex(int texCoordSet, ...)</code>
     */
    public void setTextureCoordinateIndex(int index, int texCoordIndex) {
	setTextureCoordinateIndex(0, index, texCoordIndex);
    }

    /**
     * Sets the texture coordinate index associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index the vertex index
     * @param texCoordIndex the new texture coordinate index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if neither of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception ArrayIndexOutOfBoundsException if index is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * and the specified texCoordIndex is out of range.  The
     * texCoordIndex is out of range if it is less than 0 or is
     * greater than or equal to the number of vertices actually
     * defined for the texture coordinate array.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinateIndex(int texCoordSet,
					  int index,
					  int texCoordIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray7"));
  
	((IndexedGeometryArrayRetained)this.retained).setTextureCoordinateIndex(texCoordSet, index, texCoordIndex);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>setTextureCoordinateIndices(int texCoordSet, ...)</code>
     */
    public void setTextureCoordinateIndices(int index, int texCoordIndices[]) {
	setTextureCoordinateIndices(0, index, texCoordIndices);
    }

    /**
     * Sets the texture coordinate indices associated with the vertices
     * starting at the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index the vertex index
     * @param texCoordIndices an array of texture coordinate indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if neither of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * texCoordIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the texture coordinate array.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinateIndices(int texCoordSet,
					    int index,
					    int texCoordIndices[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray7"));
  
	((IndexedGeometryArrayRetained)this.retained).setTextureCoordinateIndices(texCoordSet, index, texCoordIndices);
    }

  /**
   * Retrieves the coordinate index associated with the vertex at
   * the specified index for this object.
   * @param index the vertex index
   * @return the coordinate index
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public int getCoordinateIndex(int index) {
    if (isLiveOrCompiled())
        if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
      	   throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray9"));
  
    return ((IndexedGeometryArrayRetained)this.retained).getCoordinateIndex(index);
  }

  /**
   * Retrieves the coordinate indices associated with the vertices starting at
   * the specified index for this object.
   * @param index the vertex index
   * @param coordinateIndices array that will receive the coordinate indices
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public void getCoordinateIndices(int index, int coordinateIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray9"));
  
    ((IndexedGeometryArrayRetained)this.retained).getCoordinateIndices(index, coordinateIndices);
  }

  /**
   * Retrieves the color index associated with the vertex at
   * the specified index for this object.
   * @param index the vertex index
   * @return the color index
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public int getColorIndex(int index) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray11"));
  
    return ((IndexedGeometryArrayRetained)this.retained).getColorIndex(index);
  }

  /**
   * Retrieves the color indices associated with the vertices starting at
   * the specified index for this object. The color indicies are
   * copied into the specified array. The array must be large enough
   * to hold all of the indices.
   * @param index the vertex index
   * @param colorIndices array that will receive the color indices
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public void getColorIndices(int index, int colorIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COLOR_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray11"));
  
    ((IndexedGeometryArrayRetained)this.retained).getColorIndices(index, colorIndices);
  }

  /**
   * Retrieves the normal index associated with the vertex at
   * the specified index for this object.
   * @param index the vertex index
   * @return the normal index
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public int getNormalIndex(int index) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray13"));
  
    return ((IndexedGeometryArrayRetained)this.retained).getNormalIndex(index);
  }

  /**
   * Retrieves the normal indices associated with the vertices starting at
   * the specified index for this object. The normal indicies are
   * copied into the specified array. The array must be large enough
   * to hold all of the normal indicies.
   * 
   * @param index the vertex index
   * @param normalIndices array that will receive the normal indices
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
  public void getNormalIndices(int index, int normalIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_NORMAL_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray13"));
  
    ((IndexedGeometryArrayRetained)this.retained).getNormalIndices(index, normalIndices);
  }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinateIndex(int texCoordSet, ...)</code>
     */
    public int getTextureCoordinateIndex(int index) {
	return (getTextureCoordinateIndex(0, index));
    }

    /**
     * Retrieves the texture coordinate index associated with the vertex at
     * the specified index in the specified texture coordinate set
     * for this object.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index the vertex index
     *
     * @return the texture coordinate index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if neither of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @since Java 3D 1.2
     */
    public int getTextureCoordinateIndex(int texCoordSet, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray15"));

	return ((IndexedGeometryArrayRetained)this.retained).getTextureCoordinateIndex(texCoordSet, index);
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>getTextureCoordinateIndices(int texCoordSet, ...)</code>
     */
    public void getTextureCoordinateIndices(int index, int texCoordIndices[]) {
	getTextureCoordinateIndices(0, index, texCoordIndices);
    }


    /**
     * Retrieves the texture coordinate indices associated with the vertices
     * starting at the specified index in the specified texture coordinate set
     * for this object. The texture
     * coordinate indices are copied into the specified array. The array
     * must be large enough to hold all of the indices.
     *
     * @param texCoordSet texture coordinate set in this geometry array
     * @param index the vertex index
     * @param texCoordIndices array that will receive the texture coordinate 
     * indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if neither of the
     * <code>TEXTURE_COORDINATE</code> bits are set in the
     * <code>vertexFormat</code> or if the index or
     * texCoordSet is out of range.
     *
     * @since Java 3D 1.2
     */
    public void getTextureCoordinateIndices(int texCoordSet,
					    int index,
					    int texCoordIndices[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray15"));
  
	((IndexedGeometryArrayRetained)this.retained).getTextureCoordinateIndices(texCoordSet, index, texCoordIndices);
    }

   /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent, 
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);
	// vertexFormat, vertexCount and indexCount are copied in 
	//  subclass when constructor
	//  public IndexedGeometryArray(int vertexCount, int vertexFormat, 
	//                              int indexCount) 
	// is used in cloneNodeComponent()
	IndexedGeometryArrayRetained ga = 
	    (IndexedGeometryArrayRetained) originalNodeComponent.retained;
	IndexedGeometryArrayRetained rt = 
	    (IndexedGeometryArrayRetained) retained;

	int vformat = ga.getVertexFormat();
	int buffer[] = new int[ga.getIndexCount()];

	if ((vformat & GeometryArray.COORDINATES) != 0) {
	    ga.getCoordinateIndices(0, buffer);
	    rt.setCoordinateIndices(0, buffer);
	}
	
	if ((vformat & GeometryArray.NORMALS) != 0) {
	    ga.getNormalIndices(0, buffer);
	    rt.setNormalIndices(0, buffer);
	}
	
	if ((vformat & GeometryArray.COLOR) != 0) {
	    ga.getColorIndices(0, buffer);
	    rt.setColorIndices(0, buffer);
	}
	
	if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	    for (int i = 0; i < ga.texCoordSetCount; i++) {
	         ga.getTextureCoordinateIndices(i, 0, buffer);
	         rt.setTextureCoordinateIndices(i, 0, buffer);
	    }
	}
    }

}
