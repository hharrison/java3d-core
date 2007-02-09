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
 * The IndexedTriangleStripArray object draws an array of vertices as a set of
 * connected triangle strips.  An array of per-strip index counts specifies
 * where the separate strips appear in the indexed vertex array.
 * For every strip in the set,
 * each vertex, beginning with the third vertex in the array,
 * defines a triangle to be drawn using the current vertex and
 * the two previous vertices.
 */

public class IndexedTriangleStripArray extends IndexedGeometryStripArray {

    /**
     * Package scoped default constructor
     */
    IndexedTriangleStripArray() {
    }

    /**
     * Constructs an empty IndexedTriangleStripArray object using the
     * specified parameters.
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
     * @param stripIndexCounts
     * see {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[])}
     * for a description of this parameter.
     *
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3,
     * or any element in the stripIndexCounts array is less than 3
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     */
    public IndexedTriangleStripArray(int vertexCount,
				     int vertexFormat,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat, indexCount, stripIndexCounts);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray0")); 

        if (indexCount < 3 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray1"));
    }

    /**
     * Constructs an empty IndexedTriangleStripArray object using the
     * specified parameters.
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
     * @param stripIndexCounts
     * see {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3,
     * or any element in the stripIndexCounts array is less than 3
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedTriangleStripArray(int vertexCount,
				     int vertexFormat,
				     int texCoordSetCount,
				     int[] texCoordSetMap,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount, stripIndexCounts);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray0")); 

        if (indexCount < 3 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray1"));
    }

    /**
     * Constructs an empty IndexedTriangleStripArray object using the
     * specified parameters.
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
     * @param stripIndexCounts
     * see {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[],int,int[])}
     * for a description of this parameter.
     *
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3,
     * or any element in the stripIndexCounts array is less than 3
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedTriangleStripArray(int vertexCount,
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
	      indexCount, stripIndexCounts);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray0")); 

        if (indexCount < 3 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArray1"));
    }

    /**
     * Creates the retained mode IndexedTriangleStripArrayRetained object that this
     * IndexedTriangleStripArray object will point to.
     */
    void createRetained() {
	this.retained = new IndexedTriangleStripArrayRetained();
	this.retained.setSource(this);
    }

  
    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        IndexedTriangleStripArrayRetained rt =
                (IndexedTriangleStripArrayRetained) retained;
        int stripIndexCounts[] = new int[rt.getNumStrips()];
        rt.getStripIndexCounts(stripIndexCounts);
        int texSetCount = rt.getTexCoordSetCount();
        int[] texMap = null;
        int vertexAttrCount = rt.getVertexAttrCount();
        int[] vertexAttrSizes = null;
        if (texSetCount > 0) {
            texMap = new int[rt.getTexCoordSetMapLength()];
            rt.getTexCoordSetMap(texMap);
        }
        if (vertexAttrCount > 0) {
            vertexAttrSizes = new int[vertexAttrCount];
            rt.getVertexAttrSizes(vertexAttrSizes);
        }
        IndexedTriangleStripArray t = new IndexedTriangleStripArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount,
                vertexAttrSizes,
                rt.getIndexCount(),
                stripIndexCounts);
        t.duplicateNodeComponent(this);
        return t;
    }
}
