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
 * The IndexedQuadArray object draws the array of vertices as individual
 * quadrilaterals.  Each group
 * of four vertices defines a quadrilateral to be drawn.
 */

public class IndexedQuadArray extends IndexedGeometryArray {

    /**
    * Package scoped default constructor.
    */
    IndexedQuadArray() {
    }

    /**
     * Constructs an empty IndexedQuadArray object using the specified
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
     * or indexCount is less than 4, or indexCount is <i>not</i>
     * a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public IndexedQuadArray(int vertexCount, int vertexFormat, int indexCount) {
	super(vertexCount,vertexFormat, indexCount);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray0")); 

        if (indexCount < 4 || ((indexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray1"));
    }

    /**
     * Constructs an empty IndexedQuadArray object using the specified
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
     * or indexCount is less than 4, or indexCount is <i>not</i>
     * a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedQuadArray(int vertexCount,
			     int vertexFormat,
			     int texCoordSetCount,
			     int[] texCoordSetMap,
			     int indexCount) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount);

        if (vertexCount < 1) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray0")); 

        if (indexCount < 4 || ((indexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray1"));
    }

    /**
     * Constructs an empty IndexedQuadArray object using the specified
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
     * or indexCount is less than 4, or indexCount is <i>not</i>
     * a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedQuadArray(int vertexCount,
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
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray0")); 

        if (indexCount < 4 || ((indexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArray1"));
    }

    /**
     * Creates the retained mode IndexedQuadArrayRetained object that this
     * IndexedQuadArray object will point to.
     */
    void createRetained() {
	this.retained = new IndexedQuadArrayRetained();
	this.retained.setSource(this);
    }

  
    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        IndexedQuadArrayRetained rt = (IndexedQuadArrayRetained) retained;
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
        IndexedQuadArray q = new IndexedQuadArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount,
                vertexAttrSizes,
                rt.getIndexCount());
        q.duplicateNodeComponent(this);
        return q;
    }
}
