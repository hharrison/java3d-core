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
 * The LineArray object draws the array of vertices as individual
 * line segments.  Each pair of vertices defines a line to be drawn.
 */

public class LineArray extends GeometryArray {

    /**
     * packaged scope default constructor.
     */
    LineArray() {
    }

    /**
     * Constructs an empty LineArray object with the specified
     * number of vertices, and vertex format.
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
     * @exception IllegalArgumentException if vertexCount is less than 2
     * or vertexCount is <i>not</i> a multiple of 2
     */
    public LineArray(int vertexCount, int vertexFormat) {
	super(vertexCount,vertexFormat);

        if (vertexCount < 2 || ((vertexCount%2) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("LineArray0"));
    }

    /**
     * Constructs an empty LineArray object with the specified
     * number of vertices, and vertex format, number of texture coordinate
     * sets, and texture coordinate mapping array.
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
     * <code>texCoordSetMap</code> array is not used.
     *
     * @exception IllegalArgumentException if vertexCount is less than 2
     * or vertexCount is <i>not</i> a multiple of 2
     *
     * @since Java 3D 1.2
     */
    public LineArray(int vertexCount,
		     int vertexFormat,
		     int texCoordSetCount,
		     int[] texCoordSetMap) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap);

        if (vertexCount < 2 || ((vertexCount%2) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("LineArray0"));
    }

    /**
     * Creates the retained mode LineArrayRetained object that this
     * LineArray object will point to.
     */
    void createRetained() {
	this.retained = new LineArrayRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	LineArrayRetained rt = (LineArrayRetained) retained;


	int texSetCount = rt.getTexCoordSetCount();
	LineArray l;
	if (texSetCount == 0) {
	    l = new LineArray(rt.getVertexCount(), 
			      rt.getVertexFormat());
	} else {
	    int texMap[] = new int[rt.getTexCoordSetMapLength()];
	    rt.getTexCoordSetMap(texMap);
	    l = new LineArray(rt.getVertexCount(), 
			      rt.getVertexFormat(),
			      texSetCount,
			      texMap);

	}

	l.duplicateNodeComponent(this);
        return l;
     }

}
