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

/**
 * The IndexedLineArray object draws the array of vertices as individual
 * line segments.  Each pair of vertices defines a line to be drawn.
 */

public class IndexedLineArray extends IndexedGeometryArray {
    /**
     * Package scoped default constructor.
     */
    IndexedLineArray() {
    }

    /**
     * Constructs an empty IndexedLineArray object with the specified
     * number of vertices, vertex format, and number of indices.
     * @param vertexCount the number of vertex elements in this object
     * @param vertexFormat a mask indicating which components are
     * present in each vertex.  This is specified as one or more
     * individual flags that are bitwise "OR"ed together to describe
     * the per-vertex data.
     * The flags include: COORDINATES, to signal the inclusion of
     * vertex positions--always present; NORMALS, to signal 
     * the inclusion of per vertex normals; one of COLOR_3,
     * COLOR_4, to signal the inclusion of per vertex
     * colors (without or with color information); and one of 
     * TEXTURE_COORDINATE_2, TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4, 
     * to signal the
     * inclusion of per-vertex texture coordinates 2D, 3D or 4D.
     * @param indexCount the number of indices in this object.  This
     * count is the maximum number of vertices that will be rendered.
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 2, or indexCount is <i>not</i>
     * a multiple of 2
     */
    public IndexedLineArray(int vertexCount, int vertexFormat, int indexCount) {
	super(vertexCount,vertexFormat, indexCount);

	if (vertexCount < 1)
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineArray0"));

	if (indexCount < 2 || ((indexCount%2) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineArray1"));
    }

    /**
     * Constructs an empty IndexedLineArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, and number of indices.
     *
     * @param vertexCount the number of vertex elements in this object<p>
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
     * TEXTURE_COORDINATE_2, TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4, 
     * to signal the
     * inclusion of per-vertex texture coordinates 2D, 3D or 4D.<p>
     *
     * @param texCoordSetCount the number of texture coordinate sets
     * in this GeometryArray object.  If <code>vertexFormat</code>
     * does not include one of <code>TEXTURE_COORDINATE_2</code>,
     * <code>TEXTURE_COORDINATE_3 or
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
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 2, or indexCount is <i>not</i>
     * a multiple of 2
     *
     * @since Java 3D 1.2
     */
    public IndexedLineArray(int vertexCount,
			    int vertexFormat,
			    int texCoordSetCount,
			    int[] texCoordSetMap,
			    int indexCount) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount);

	if (vertexCount < 1)
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineArray0"));

	if (indexCount < 2 || ((indexCount%2) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineArray1"));
    }

    /**
     * Creates the retained mode IndexedLineArrayRetained object that this
     * IndexedLineArray object will point to.
     */
    void createRetained() {
	this.retained = new IndexedLineArrayRetained();
	this.retained.setSource(this);
    }

    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	IndexedLineArrayRetained rt = (IndexedLineArrayRetained) retained;
	int texSetCount = rt.getTexCoordSetCount();	
        IndexedLineArray l;
	if (texSetCount == 0) {
	    l = new IndexedLineArray(rt.getVertexCount(),
				     rt.getVertexFormat(),
				     rt.getIndexCount());
	} else {
	    int texMap[] = new int[rt.getTexCoordSetMapLength()];
	    rt.getTexCoordSetMap(texMap);	    
	    l = new IndexedLineArray(rt.getVertexCount(),
				     rt.getVertexFormat(),
				     texSetCount,
				     texMap,
				     rt.getIndexCount());
	}
        l.duplicateNodeComponent(this);
        return l;
    }
}
