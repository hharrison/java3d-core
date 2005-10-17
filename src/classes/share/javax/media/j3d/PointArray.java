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
 * The PointArray object draws the array of vertices as individual points.
 */

public class PointArray extends GeometryArray {

    // non-public, no parameter constructor
    PointArray() {}

    /**
     * Constructs an empty PointArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 1
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public PointArray(int vertexCount, int vertexFormat) {
	super(vertexCount,vertexFormat);

        if (vertexCount < 1 )
	    throw new IllegalArgumentException(J3dI18N.getString("PointArray0"));
    }

    /**
     * Constructs an empty PointArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 1
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public PointArray(int vertexCount,
		      int vertexFormat,
		      int texCoordSetCount,
		      int[] texCoordSetMap) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap);

        if (vertexCount < 1 )
	    throw new IllegalArgumentException(J3dI18N.getString("PointArray0"));
    }

    /**
     * Constructs an empty PointArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 1
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public PointArray(int vertexCount,
		      int vertexFormat,
		      int texCoordSetCount,
		      int[] texCoordSetMap,
		      int vertexAttrCount,
		      int[] vertexAttrSizes) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes);

        if (vertexCount < 1 )
	    throw new IllegalArgumentException(J3dI18N.getString("PointArray0"));
    }

    /**
     * Creates the retained mode PointArrayRetained object that this
     * PointArray object will point to.
     */
    void createRetained() {
	this.retained = new PointArrayRetained();
	this.retained.setSource(this);
    }

  
    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        PointArrayRetained rt = (PointArrayRetained) retained;
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
        PointArray p = new PointArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount,
                vertexAttrSizes);
        p.duplicateNodeComponent(this);
        return p;
     }
}
