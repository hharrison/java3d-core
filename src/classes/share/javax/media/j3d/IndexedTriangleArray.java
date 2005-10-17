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
 * The IndexedTriangleArray object draws the array of vertices as individual
 * triangles.  Each group
 * of three vertices defines a triangle to be drawn.
 */

public class IndexedTriangleArray extends IndexedGeometryArray {

    /**
     * Package scoped default constructor.
     */
    IndexedTriangleArray() {
    }

    /**
     * Constructs an empty IndexedTriangleArray object using the specified
     * parameters.
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
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3, or indexCount is <i>not</i>
     * a multiple of 3
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public IndexedTriangleArray(int vertexCount, int vertexFormat, int indexCount) {
	super(vertexCount,vertexFormat, indexCount);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray0")); 

        if (indexCount < 3 || ((indexCount%3) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray1"));
    }

    /**
     * Constructs an empty IndexedTriangleArray object using the specified
     * parameters.
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
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3, or indexCount is <i>not</i>
     * a multiple of 3
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedTriangleArray(int vertexCount,
				int vertexFormat,
				int texCoordSetCount,
				int[] texCoordSetMap,
				int indexCount) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray0")); 

        if (indexCount < 3 || ((indexCount%3) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray1"));
    }

    /**
     * Constructs an empty IndexedTriangleArray object using the specified
     * parameters.
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
     * @exception IllegalArgumentException if vertexCount is less than 1,
     * or indexCount is less than 3, or indexCount is <i>not</i>
     * a multiple of 3
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedTriangleArray(int vertexCount,
				int vertexFormat,
				int texCoordSetCount,
				int[] texCoordSetMap,
				int vertexAttrCount,
				int[] vertexAttrSizes,
				int indexCount) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes,
	      indexCount);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray0")); 

        if (indexCount < 3 || ((indexCount%3) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleArray1"));
    }

    /**
     * Creates the retained mode IndexedTriangleArrayRetained object that this
     * IndexedTriangleArray object will point to.
     */
    void createRetained() {
	this.retained = new IndexedTriangleArrayRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        IndexedTriangleArrayRetained rt = (IndexedTriangleArrayRetained) retained;
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
        IndexedTriangleArray t = new IndexedTriangleArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount, 
                vertexAttrSizes,
                rt.getIndexCount());
        t.duplicateNodeComponent(this);
        return t;
    }
}
