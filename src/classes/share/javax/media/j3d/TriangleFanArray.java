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
 * The TriangleFanArray object draws an array of vertices as a set of
 * connected triangle fans.  An array of per-strip
 * vertex counts specifies where the separate strips (fans) appear
 * in the vertex array.  For every strip in the set,
 * each vertex, beginning with the third vertex in the array,
 * defines a triangle to be drawn using the current vertex,
 * the previous vertex and the first vertex.  This can be thought of
 * as a collection of convex polygons.
 */

public class TriangleFanArray extends GeometryStripArray {

    // non-public, no parameter constructor
    TriangleFanArray() {}

    /**
     * Constructs an empty TriangleFanArray object with the specified
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
     * TEXTURE_COORDINATE_2, TEXTURE_COORDINATE_3 or TEXTURE_COORDINATE_4, 
     * to signal the
     * inclusion of per-vertex texture coordinates 2D, 3D or 4D.
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     *
     * @exception IllegalArgumentException if vertexCount is less than 3
     * or any element in the stripVertexCounts array is less than 3
     */
    public TriangleFanArray(int vertexCount,
			    int vertexFormat,
			    int stripVertexCounts[]) {

	super(vertexCount, vertexFormat, stripVertexCounts);

        if (vertexCount < 3 )
	    throw new IllegalArgumentException(J3dI18N.getString("TriangleFanArray0"));
    }

    /**
     * Constructs an empty TriangleFanArray object with the specified
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
     * @param stripVertexCounts array that specifies
     * the count of the number of vertices for each separate strip.
     * The length of this array is the number of separate strips.
     *
     * @exception IllegalArgumentException if vertexCount is less than 3
     * or any element in the stripVertexCounts array is less than 3
     *
     * @since Java 3D 1.2
     */
    public TriangleFanArray(int vertexCount,
			    int vertexFormat,
			    int texCoordSetCount,
			    int[] texCoordSetMap,
			    int stripVertexCounts[]) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      stripVertexCounts);

        if (vertexCount < 3 )
        throw new IllegalArgumentException(J3dI18N.getString("TriangleFanArray0"));
    }

    /**
     * Creates the retained mode TriangleFanArrayRetained object that this
     * TriangleFanArray object will point to.
     */
    void createRetained() {
	this.retained = new TriangleFanArrayRetained();
	this.retained.setSource(this);
    }

   
    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	TriangleFanArrayRetained rt = (TriangleFanArrayRetained) retained;
	int stripcounts[] = new int[rt.getNumStrips()];
	rt.getStripVertexCounts(stripcounts);
	int texSetCount = rt.getTexCoordSetCount();
        TriangleFanArray t;
	if (texSetCount == 0) {
	    t = new TriangleFanArray(rt.getVertexCount(), 
				     rt.getVertexFormat(),
				     stripcounts);
	} else {
	    int texMap[] = new int[rt.getTexCoordSetMapLength()];
	    rt.getTexCoordSetMap(texMap);	    
	    t = new TriangleFanArray(rt.getVertexCount(), 
				     rt.getVertexFormat(),
				     texSetCount,
				     texMap,
				     stripcounts);
	}
	t.duplicateNodeComponent(this);
        return t;
     }

}
