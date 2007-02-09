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
 * The QuadArray object draws the array of vertices as individual
 * quadrilaterals.  Each group
 * of four vertices defines a quadrilateral to be drawn.
 */

public class QuadArray extends GeometryArray {

    // non-public, no parameter constructor
    QuadArray() {}

    /**
     * Constructs an empty QuadArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 4
     * or vertexCount is <i>not</i> a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public QuadArray(int vertexCount, int vertexFormat) {
	super(vertexCount,vertexFormat);

        if (vertexCount < 4 || ((vertexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("QuadArray0"));
    }

    /**
     * Constructs an empty QuadArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 4
     * or vertexCount is <i>not</i> a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public QuadArray(int vertexCount,
		     int vertexFormat,
		     int texCoordSetCount,
		     int[] texCoordSetMap) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap);

        if (vertexCount < 4 || ((vertexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("QuadArray0"));
    }

    /**
     * Constructs an empty QuadArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 4
     * or vertexCount is <i>not</i> a multiple of 4
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public QuadArray(int vertexCount,
		     int vertexFormat,
		     int texCoordSetCount,
		     int[] texCoordSetMap,
		     int vertexAttrCount,
		     int[] vertexAttrSizes) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes);

        if (vertexCount < 4 || ((vertexCount%4) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("QuadArray0"));
    }

    /**
     * Creates the retained mode QuadArrayRetained object that this
     * QuadArray object will point to.
     */
    void createRetained() {
	this.retained = new QuadArrayRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        QuadArrayRetained rt = (QuadArrayRetained) retained;
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
        QuadArray q = new QuadArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount,
                vertexAttrSizes);
        q.duplicateNodeComponent(this);
        return q;
     }


}
