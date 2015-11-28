/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package org.jogamp.java3d;

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
    @Override
    void createRetained() {
	this.retained = new IndexedTriangleArrayRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
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
