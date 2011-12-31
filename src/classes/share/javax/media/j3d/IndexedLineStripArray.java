/*
 * $RCSfile$
 *
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
     * Constructs an empty IndexedLineStripArray object using the
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
     * or indexCount is less than 2,
     * or any element in the stripIndexCounts array is less than 2
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[])}
     * for more exceptions that can be thrown
     */
    public IndexedLineStripArray(int vertexCount,
				     int vertexFormat,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat, indexCount, stripIndexCounts);

        if (vertexCount < 1)
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray0"));

        if (indexCount < 2 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray1"));
    }

    /**
     * Constructs an empty IndexedLineStripArray object using the
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
     * or indexCount is less than 2,
     * or any element in the stripIndexCounts array is less than 2
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.2
     */
    public IndexedLineStripArray(int vertexCount,
				     int vertexFormat,
				     int texCoordSetCount,
				     int[] texCoordSetMap,
				     int indexCount,
				     int[] stripIndexCounts) {

	super(vertexCount, vertexFormat,
	      texCoordSetCount, texCoordSetMap,
	      indexCount, stripIndexCounts);

        if (vertexCount < 1)
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray0"));

        if (indexCount < 2 )
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArray1"));
    }

    /**
     * Constructs an empty IndexedLineStripArray object using the
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
     * or indexCount is less than 2,
     * or any element in the stripIndexCounts array is less than 2
     * ;<br>
     * See {@link IndexedGeometryStripArray#IndexedGeometryStripArray(int,int,int,int[],int,int[],int,int[])}
     * for more exceptions that can be thrown
     *
     * @since Java 3D 1.4
     */
    public IndexedLineStripArray(int vertexCount,
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
        IndexedLineStripArray l = new IndexedLineStripArray(rt.getVertexCount(),
                rt.getVertexFormat(),
                texSetCount,
                texMap,
                vertexAttrCount,
                vertexAttrSizes,
                rt.getIndexCount(),
                stripIndexCounts);
        l.duplicateNodeComponent(this);
        return l;
    }
}
