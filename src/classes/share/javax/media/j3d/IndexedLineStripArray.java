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
 * The IndexedLineStripArray object draws an array of vertices as a set of
 * connected line strips.  An array of per-strip index counts specifies
 * where the separate strips appear in the indexed vertex array.
 * For every strip in the set, each vertex, beginning with
 * the second vertex in the array, defines a line segment to be drawn
 * from the previous vertex to the current vertex.
 */

public class IndexedLineStripArray extends IndexedGeometryStripArray {

    /**
     * Package scoped default constructor.
     */
    IndexedLineStripArray() {
    }

    /**
     * Constructs an empty IndexedLineStripArray object with the specified
     * number of vertices, vertex format, number of indices, and
     * array of per-strip index counts.
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
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     * The length of this array is the number of separate strips.
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 2,
     * or any element in the stripIndexCounts array is less than 2
     */
    public IndexedLineStripArray(int vertexCount,
			int vertexFormat,
			int indexCount,
			int stripIndexCounts[]) {

	super(vertexCount, vertexFormat, indexCount, stripIndexCounts);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray0")); 

        if (indexCount < 2 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray1"));
    }

    /**
     * Constructs an empty IndexedLineStripArray object with the specified
     * number of vertices, vertex format, number of texture coordinate
     * sets, texture coordinate mapping array, number of indices, and
     * array of per-strip index counts.
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
     * count is the maximum number of vertices that will be rendered.<p>
     *
     * @param stripIndexCounts array that specifies
     * the count of the number of indices for each separate strip.
     * The length of this array is the number of separate strips.
     *
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 2,
     * or any element in the stripIndexCounts array is less than 2
     *
     * @since Java 3D 1.2
     */
    public IndexedLineStripArray(int vertexCount,
				 int vertexFormat,
				 int texCoordSetCount,
				 int[] texCoordSetMap,
				 int indexCount,
				 int stripIndexCounts[]) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount, stripIndexCounts);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray0")); 

        if (indexCount < 2 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray1"));
    }

    /**
     * Creates the retained mode IndexedLineStripArrayRetained object that this
     * IndexedLineStripArray object will point to.
     */
    void createRetained() {
	this.retained = new IndexedLineStripArrayRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	IndexedLineStripArrayRetained rt =
	    (IndexedLineStripArrayRetained) retained;
        int stripIndexCounts[] = new int[rt.getNumStrips()];
	rt.getStripIndexCounts(stripIndexCounts);
	int texSetCount = rt.getTexCoordSetCount();
        IndexedLineStripArray l; 

	if (texSetCount == 0) {
	    l = new IndexedLineStripArray(rt.getVertexCount(),
					  rt.getVertexFormat(),
					  rt.getIndexCount(),
					  stripIndexCounts);
	} else {
	    int texMap[] = new int[rt.getTexCoordSetMapLength()];
	    rt.getTexCoordSetMap(texMap);
	    l = new IndexedLineStripArray(rt.getVertexCount(),
					  rt.getVertexFormat(),
					  texSetCount,
					  texMap,
					  rt.getIndexCount(),
					  stripIndexCounts);
	    
	}
        l.duplicateNodeComponent(this);
        return l;
    }
}
