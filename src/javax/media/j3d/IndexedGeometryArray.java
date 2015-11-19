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


/**
 * The IndexedGeometryArray object contains separate integer arrays
 * that index into the arrays of positional coordinates, colors,
 * normals, texture coordinates, and vertex attributes.
 * These index arrays specify how
 * vertices are connected to form geometry primitives.  This class is
 * extended to create the various indexed primitive types (e.g.,
 * lines, triangle strips, etc.).
 */

public abstract class IndexedGeometryArray extends GeometryArray {

    // non-public, no parameter constructor
    IndexedGeometryArray() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

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
     * Specifies that this IndexedGeometryArray allows reading the array of
     * vertex attribute indices.
     *
     * @since Java 3D 1.4
     */
    public static final int
        ALLOW_VERTEX_ATTR_INDEX_READ = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_VERTEX_ATTR_INDEX_READ;

    /**
     * Specifies that this IndexedGeometryArray allows writing the array of
     * vertex attribute indices.
     *
     * @since Java 3D 1.4
     */
    public static final int
        ALLOW_VERTEX_ATTR_INDEX_WRITE = CapabilityBits.INDEXED_GEOMETRY_ARRAY_ALLOW_VERTEX_ATTR_INDEX_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_COLOR_INDEX_READ,
        ALLOW_COORDINATE_INDEX_READ,
        ALLOW_NORMAL_INDEX_READ,
        ALLOW_TEXCOORD_INDEX_READ,
        ALLOW_VERTEX_ATTR_INDEX_READ
    };

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
     * @param vertexCount
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param vertexFormat
     * see {@link GeometryArray#GeometryArray(int,int)}
     * for a description of this parameter.
     *
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     *
     * @exception IllegalArgumentException if <code>indexCount &lt; 0</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public IndexedGeometryArray(int vertexCount,
				int vertexFormat,
				int indexCount) {
	super(vertexCount, vertexFormat);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((IndexedGeometryArrayRetained)this.retained).createIndexedGeometryArrayData(indexCount);
    }

    /**
     * Constructs an empty IndexedGeometryArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, and number of indices.
     * Defaults are used for all other parameters.
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
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     *
     * @exception IllegalArgumentException if <code>indexCount &lt; 0</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedGeometryArray(int vertexCount,
				int vertexFormat,
				int texCoordSetCount,
				int[] texCoordSetMap,
				int indexCount) {
	this(vertexCount, vertexFormat, texCoordSetCount, texCoordSetMap, 0, null, indexCount);
    }

    /**
     * Constructs an empty IndexedGeometryArray object with the
     * specified number of vertices, vertex format, number of texture
     * coordinate sets, texture coordinate mapping array, vertex
     * attribute count, vertex attribute sizes array, and number of
     * indices.
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
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     *
     * @exception IllegalArgumentException if <code>indexCount &lt; 0</code>
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedGeometryArray(int vertexCount,
				int vertexFormat,
				int texCoordSetCount,
				int[] texCoordSetMap,
				int vertexAttrCount,
				int[] vertexAttrSizes,
				int indexCount) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

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
     * @exception ArrayIndexOutOfBoundsException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code> and
     * <code>coordIndices.length &lt; (initialIndexIndex + validIndexCount)</code>.
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
     * @exception ArrayIndexOutOfBoundsException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code> and
     * <code>coordIndices.length &lt; (initialIndexIndex + validIndexCount)</code>.
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void setInitialTexCoordIndex(int texCoordSet,
					int initialTexCoordIndex) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for indexed geometry arrays.
     * Indexed primitives use an array of indices to determine how
     * to access the vertex array.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.4
     */
    @Override
    public void setInitialVertexAttrIndex(int vertexAttrNum,
					  int initialVertexAttrIndex) {
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
    @Override
    public void setValidVertexCount(int validVertexCount) {
	throw new UnsupportedOperationException();
    }


    //NVaidya
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
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code>.
     */
  public void setCoordinateIndex(int index, int coordinateIndex) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray1"));

    //NVaidya
    int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE_INDICES) != 0)
      throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray31"));

    ((IndexedGeometryArrayRetained)this.retained).setCoordinateIndex(index, coordinateIndex);
  }


    //NVaidya
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
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code>.
     */
  public void setCoordinateIndices(int index, int coordinateIndices[]) {
    if (isLiveOrCompiled())
	if(!this.getCapability(ALLOW_COORDINATE_INDEX_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray1"));

    //NVaidya
    int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE_INDICES) != 0)
      throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray31"));

    ((IndexedGeometryArrayRetained)this.retained).setCoordinateIndices(index, coordinateIndices);
  }

    //NVaidya
    /**
     * Sets the coordinate indices array reference to the specified array.
     * If the coordinate indices array reference is null, the entire
     * geometry array object is treated as if it were null--any
     * Shape3D or Morph node that uses this geometry array will not be drawn.
     *
     * @param coordIndices an array of indices to which a reference
     * will be set.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE_INDICES</code>.
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * coordIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the coordinate array.
     *
     * @exception ArrayIndexOutOfBoundsException if
     * <code>coordIndices.length &lt; (initialIndexIndex + validIndexCount)</code>.
     *
     * @since Java 3D 1.5
     */
    public void setCoordIndicesRef(int coordIndices[]) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REF_DATA_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray86"));

        //NVaidya
        int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
        if ((format & BY_REFERENCE_INDICES) == 0)
            throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray32"));

        ((IndexedGeometryArrayRetained)this.retained).setCoordIndicesRef(coordIndices);
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinateIndex(int texCoordSet,
					  int index,
					  int texCoordIndex) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_INDEX_WRITE))
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
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.2
     */
    public void setTextureCoordinateIndices(int texCoordSet,
					    int index,
					    int texCoordIndices[]) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TEXCOORD_INDEX_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray7"));

	((IndexedGeometryArrayRetained)this.retained).setTextureCoordinateIndices(texCoordSet, index, texCoordIndices);
    }

    /**
     * Sets the vertex attribute index associated with the vertex at
     * the specified index for the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index the vertex index
     * @param vertexAttrIndex the new vertex attribute index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception ArrayIndexOutOfBoundsException if index is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * and the specified vertexAttrIndex is out of range.  The
     * vertexAttrIndex is out of range if it is less than 0 or is
     * greater than or equal to the number of vertices actually
     * defined for the vertex attribute array.
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrIndex(int vertexAttrNum,
                                   int index,
                                   int vertexAttrIndex) {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_VERTEX_ATTR_INDEX_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray28"));
            }
        }

        ((IndexedGeometryArrayRetained)this.retained).setVertexAttrIndex(vertexAttrNum, index, vertexAttrIndex);
    }

    /**
     * Sets the vertex attribute indices associated with the vertices
     * starting at the specified index for the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index the vertex index
     * @param vertexAttrIndices an array of vertex attribute indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception ArrayIndexOutOfBoundsException if any element of the
     * vertexAttrIndices array whose destination position is in the range
     * <code>[initialIndexIndex, initialIndexIndex+validIndexCount-1]</code>
     * is out of range.  An element is out of range if it is less than 0
     * or is greater than or equal to the number of vertices actually
     * defined for the vertex attribute array.
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.4
     */
    public void setVertexAttrIndices(int vertexAttrNum,
                                     int index,
                                     int[] vertexAttrIndices) {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_VERTEX_ATTR_INDEX_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray28"));
            }
        }

	((IndexedGeometryArrayRetained)this.retained).setVertexAttrIndices(vertexAttrNum, index, vertexAttrIndices);
    }

  //NVaidya
  /**
     * Retrieves the coordinate index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @return the coordinate index
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code>.
     */
  public int getCoordinateIndex(int index) {
    if (isLiveOrCompiled())
        if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
      	   throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray9"));

    //NVaidya
    int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE_INDICES) != 0)
      throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray31"));

    return ((IndexedGeometryArrayRetained)this.retained).getCoordinateIndex(index);
  }

    //NVaidya
    /**
     * Retrieves the coordinate indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndices array that will receive the coordinate indices
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is <code>BY_REFERENCE_INDICES</code>.
     */
  public void getCoordinateIndices(int index, int coordinateIndices[]) {
    if (isLiveOrCompiled())
    if(!this.getCapability(ALLOW_COORDINATE_INDEX_READ))
      throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray9"));

    //NVaidya
    int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
    if ((format & BY_REFERENCE_INDICES) != 0)
      throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray31"));

    ((IndexedGeometryArrayRetained)this.retained).getCoordinateIndices(index, coordinateIndices);
  }

  //NVaidya
  /**
     * Returns a reference to the coordinate indices associated with
     * the vertices
     * @return the coordinate indices array
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data mode for this geometry
     * array object is not <code>BY_REFERENCE_INDICES</code>.
     *
     * @since Java 3D 1.5
     */
  public int[] getCoordIndicesRef() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_REF_DATA_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("GeometryArray87"));

	int format = ((IndexedGeometryArrayRetained)this.retained).vertexFormat;
	if ((format & BY_REFERENCE_INDICES) == 0)
	    throw new IllegalStateException(J3dI18N.getString("IndexedGeometryArray32"));

    return ((IndexedGeometryArrayRetained)this.retained).getCoordIndicesRef();
  }

  /**
   * Retrieves the color index associated with the vertex at
   * the specified index for this object.
   * @param index the vertex index
   * @return the color index
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
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
     * Retrieves the vertex attribute index associated with the vertex at
     * the specified index for the specified vertex attribute number
     * for this object.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index the vertex index
     *
     * @return the vertex attribute index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.4
     */
    public int getVertexAttrIndex(int vertexAttrNum,
                                  int index) {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_VERTEX_ATTR_INDEX_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray29"));
            }
        }

	return ((IndexedGeometryArrayRetained)this.retained).getVertexAttrIndex(vertexAttrNum, index);
    }

    /**
     * Retrieves the vertex attribute indices associated with the vertices
     * starting at the specified index for the specified vertex attribute number
     * for this object. The vertex attribute indices
     * are copied into the specified array. The array
     * must be large enough to hold all of the indices.
     *
     * @param vertexAttrNum vertex attribute number in this geometry array
     * @param index the vertex index
     * @param vertexAttrIndices array that will receive the vertex attribute indices
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception ArrayIndexOutOfBoundsException if the index or
     * vertexAttrNum is out of range.
     *
     * @exception NullPointerException if the <code>USE_COORD_INDEX_ONLY</code>
     * bit is set in <code>vertexFormat</code>.
     *
     * @since Java 3D 1.4
     */
    public void getVertexAttrIndices(int vertexAttrNum,
                                     int index,
                                     int[] vertexAttrIndices) {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_VERTEX_ATTR_INDEX_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("IndexedGeometryArray29"));
            }
        }

	((IndexedGeometryArrayRetained)this.retained).getVertexAttrIndices(vertexAttrNum, index, vertexAttrIndices);
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
    @Override
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

        if ((vformat & COORDINATES) != 0) {
            ga.getCoordinateIndices(0, buffer);
            rt.setCoordinateIndices(0, buffer);
        }

        if ((vformat & USE_COORD_INDEX_ONLY) == 0) {
            if ((vformat & NORMALS) != 0) {
                ga.getNormalIndices(0, buffer);
                rt.setNormalIndices(0, buffer);
            }

            if ((vformat & COLOR) != 0) {
                ga.getColorIndices(0, buffer);
                rt.setColorIndices(0, buffer);
            }

            if ((vformat & VERTEX_ATTRIBUTES) != 0) {
                for (int i = 0; i < ga.vertexAttrCount; i++) {
                    ga.getVertexAttrIndices(i, 0, buffer);
                    rt.setVertexAttrIndices(i, 0, buffer);
                }
            }

            if ((vformat & TEXTURE_COORDINATE) != 0) {
                for (int i = 0; i < ga.texCoordSetCount; i++) {
                    ga.getTextureCoordinateIndices(i, 0, buffer);
                    rt.setTextureCoordinateIndices(i, 0, buffer);
                }
            }
        }
    }

}
