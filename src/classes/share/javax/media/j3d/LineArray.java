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
     * Constructs an empty LineArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 2
     * or vertexCount is <i>not</i> a multiple of 2
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int)}
     * for more exceptions that can be thrown
     */
    public LineArray(int vertexCount, int vertexFormat) {
	super(vertexCount,vertexFormat);

        if (vertexCount < 2 || ((vertexCount%2) != 0))
	    throw new IllegalArgumentException(J3dI18N.getString("LineArray0"));
    }

    /**
     * Constructs an empty LineArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 2
     * or vertexCount is <i>not</i> a multiple of 2
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[])}
     * for more exceptions that can be thrown
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
     * Constructs an empty LineArray object using the specified
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
     * @exception IllegalArgumentException if vertexCount is less than 2
     * or vertexCount is <i>not</i> a multiple of 3
     * ;<br>
     * See {@link GeometryArray#GeometryArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public LineArray(int vertexCount,
		     int vertexFormat,
		     int texCoordSetCount,
		     int[] texCoordSetMap,
		     int vertexAttrCount,
		     int[] vertexAttrSizes) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      vertexAttrCount, vertexAttrSizes);

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
        LineArray l = new LineArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap, 
                vertexAttrCount,
                vertexAttrSizes);
	l.duplicateNodeComponent(this);
        return l;
     }

}
