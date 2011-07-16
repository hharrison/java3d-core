/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
import javax.vecmath.* ;
import java.util.* ;

/**
 * The GeneralizedVertexList class is a variable-size list used to
 * collect the vertices for a generalized strip of points, lines, or
 * triangles.  This is used by the GeometryDecompressor.  This class
 * implements the GeneralizedStripFlags interface and provides methods
 * for copying instance vertex data into various fixed-size
 * GeometryArray representations.
 *
 * @see GeneralizedStrip
 * @see GeometryDecompressor
 */
class GeneralizedVertexList implements GeneralizedStripFlags {

    // The ArrayList containing the vertices.
    private ArrayList vertices ;

    // Booleans for individual vertex components.
    private boolean hasColor3 = false ;
    private boolean hasColor4 = false ;
    private boolean hasNormals = false ;
    
    // Indicates the vertex winding of front-facing triangles in this strip.
    private int frontFace ;
    
    /**
     * Count of number of strips generated after conversion to GeometryArray.
     */
    int stripCount ;

    /**
     * Count of number of vertices generated after conversion to GeometryArray.
     */
    int vertexCount ;

    /**
     * Count of number of triangles generated after conversion to GeometryArray.
     */
    int triangleCount ;

    /**
     * Bits describing the data bundled with each vertex.  This is specified
     * using the GeometryArray mask components.
     */
    int vertexFormat ;

    /**
     * Creates a new GeneralizedVertexList for the specified vertex format.
     * @param vertexFormat a mask indicating which components are
     * present in each vertex, as used by GeometryArray.
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @param initSize initial number of elements
     * @see GeometryArray
     */
    GeneralizedVertexList(int vertexFormat, int frontFace, int initSize) {
	this.frontFace = frontFace ;
	setVertexFormat(vertexFormat) ;
	
	if (initSize == 0)
	    vertices = new ArrayList() ;
	else
	    vertices = new ArrayList(initSize) ;

	stripCount = 0 ;
	vertexCount = 0 ;
	triangleCount = 0 ;
    }

    /**
     * Creates a new GeneralizedVertexList for the specified vertex format.
     * @param vertexFormat a mask indicating which components are
     * present in each vertex, as used by GeometryArray.
     * @param frontFace a flag, either GeneralizedStripFlags.FRONTFACE_CW or
     * GeneralizedStripFlags.FRONTFACE_CCW, indicating front face winding
     * @see GeometryArray
     */
    GeneralizedVertexList(int vertexFormat, int frontFace) {
	this(vertexFormat, frontFace, 0) ;
    }

    /**
     * Sets the vertex format for this vertex list.
     * @param vertexFormat a mask indicating which components are
     * present in each vertex, as used by GeometryArray.
     */
    void setVertexFormat(int vertexFormat) {
	this.vertexFormat = vertexFormat ;

	if ((vertexFormat & GeometryArray.NORMALS) != 0)
	    hasNormals = true ;

	if ((vertexFormat & GeometryArray.COLOR) != 0)
	    if ((vertexFormat & GeometryArray.WITH_ALPHA) != 0)
		hasColor4 = true ;
	    else
		hasColor3 = true ;
    }
    
    /**
     * A class with fields corresponding to all the data that can be bundled
     * with the vertices of generalized strips.
     */
    class Vertex {
	int flag ;
	Point3f coord ;
	Color3f color3 ;
	Color4f color4 ;
	Vector3f normal ;
	    
	Vertex(Point3f p, Vector3f n, Color4f c, int flag) {
	    this.flag = flag ;
	    coord = new Point3f(p) ;
		
	    if (hasNormals)
		normal = new Vector3f(n) ;

	    if (hasColor3)
		color3 = new Color3f(c.x, c.y, c.z) ;

	    else if (hasColor4)
		color4 = new Color4f(c) ;
	}
    }

    /**
     * Copy vertex data to a new Vertex object and add it to this list.
     */
    void addVertex(Point3f pos, Vector3f norm, Color4f color, int flag) {
	vertices.add(new Vertex(pos, norm, color, flag)) ;
    }
				
    /**
     * Return the number of vertices in this list.
     */
    int size() {
	return vertices.size() ;
    }

    // GeneralizedStripFlags interface implementation
    public int getFlagCount() {
	return vertices.size() ;
    }

    // GeneralizedStripFlags interface implementation
    public int getFlag(int index) {
	return ((Vertex)vertices.get(index)).flag ;
    }

    // Copy vertices in the given order to a fixed-length GeometryArray.
    // Using the array versions of the GeometryArray set() methods results in
    // a significant performance improvement despite needing to create
    // fixed-length arrays to hold the vertex elements.
    private void copyVertexData(GeometryArray ga,
				GeneralizedStrip.IntList indices) {
	Vertex v ;
	Point3f p3f[] = new Point3f[indices.count] ;

	if (hasNormals) {
	    Vector3f v3f[] = new Vector3f[indices.count] ;
	    if (hasColor3) {
		Color3f c3f[] = new Color3f[indices.count] ;
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		    v3f[i] = v.normal ;
		    c3f[i] = v.color3 ;
		}
		ga.setColors(0, c3f) ;

	    } else if (hasColor4) {
		Color4f c4f[] = new Color4f[indices.count] ;
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		    v3f[i] = v.normal ;
		    c4f[i] = v.color4 ;
		}
		ga.setColors(0, c4f) ;

	    } else {
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		    v3f[i] = v.normal ;
		}
	    }
	    ga.setNormals(0, v3f) ;

	} else {
	    if (hasColor3) {
		Color3f c3f[] = new Color3f[indices.count] ;
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		    c3f[i] = v.color3 ;
		}
		ga.setColors(0, c3f) ;
	    
	    } else if (hasColor4) {
		Color4f c4f[] = new Color4f[indices.count] ;
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		    c4f[i] = v.color4 ;
		}
		ga.setColors(0, c4f) ;

	    } else {
		for (int i = 0 ; i < indices.count ; i++) {
		    v = (Vertex)vertices.get(indices.ints[i]) ;
		    p3f[i] = v.coord ;
		}
	    }
	}
	ga.setCoordinates(0, p3f) ;
    }

    /**
     * Output a PointArray.
     */
    PointArray toPointArray() {
	int size = vertices.size() ;

	if (size > 0) {
	    PointArray pa = new PointArray(size, vertexFormat) ;
	    GeneralizedStrip.IntList il = new GeneralizedStrip.IntList(size) ;

	    il.fillAscending() ;
	    copyVertexData(pa, il) ;

	    vertexCount += size ;
	    return pa ;
	}
	else
	    return null ;
    }

    /**
     * Output a TriangleArray.
     */
    TriangleArray toTriangleArray() {
	int vertices[] = GeneralizedStrip.toTriangles(this, frontFace) ;

	if (vertices != null) {
	    TriangleArray ta ;
	    GeneralizedStrip.IntList il ;

	    ta = new TriangleArray(vertices.length, vertexFormat) ;
	    il = new GeneralizedStrip.IntList(vertices) ;
	    copyVertexData(ta, il) ;

	    vertexCount += vertices.length ;
	    triangleCount += vertices.length/3 ;
	    return ta ;
	} else
	    return null ;
    }

    /**
     * Output a LineStripArray.
     */
    LineStripArray toLineStripArray() {
	GeneralizedStrip.StripArray stripArray =
	    GeneralizedStrip.toLineStrips(this) ;

	if (stripArray != null) {
	    LineStripArray lsa ;
	    lsa = new LineStripArray(stripArray.vertices.count,
				     vertexFormat,
				     stripArray.stripCounts.trim()) ;

	    copyVertexData(lsa, stripArray.vertices) ;

	    vertexCount += stripArray.vertices.count ;
	    stripCount += stripArray.stripCounts.count ;
	    return lsa ;
	} else
	    return null ;
    }

    /**
     * Output a TriangleStripArray.
     */
    TriangleStripArray toTriangleStripArray() {
	GeneralizedStrip.StripArray stripArray =
	    GeneralizedStrip.toTriangleStrips(this, frontFace) ;

	if (stripArray != null) {
	    TriangleStripArray tsa ;
	    tsa = new TriangleStripArray(stripArray.vertices.count,
					 vertexFormat,
					 stripArray.stripCounts.trim()) ;

	    copyVertexData(tsa, stripArray.vertices) ;

	    vertexCount += stripArray.vertices.count ;
	    stripCount += stripArray.stripCounts.count ;
	    return tsa ;
	} else
	    return null ;
    }

    /**
     * Output triangle strip and triangle fan arrays.
     * @return a 2-element array of GeometryStripArray; element 0 if non-null
     * will contain a TriangleStripArray, and element 1 if non-null will
     * contain a TriangleFanArray.
     */
    GeometryStripArray[] toStripAndFanArrays() {
	GeneralizedStrip.StripArray stripArray[] =
	    GeneralizedStrip.toStripsAndFans(this, frontFace) ;

	GeometryStripArray gsa[] = new GeometryStripArray[2] ;

	if (stripArray[0] != null) {
	    gsa[0] = new TriangleStripArray(stripArray[0].vertices.count,
					    vertexFormat,
					    stripArray[0].stripCounts.trim()) ;

	    copyVertexData(gsa[0], stripArray[0].vertices) ;

	    vertexCount += stripArray[0].vertices.count ;
	    stripCount += stripArray[0].stripCounts.count ;
	}

	if (stripArray[1] != null) {
	    gsa[1] = new TriangleFanArray(stripArray[1].vertices.count,
					  vertexFormat,
					  stripArray[1].stripCounts.trim()) ;

	    copyVertexData(gsa[1], stripArray[1].vertices) ;

	    vertexCount += stripArray[1].vertices.count ;
	    stripCount += stripArray[1].stripCounts.count ;
	}
	return gsa ;
    }

    /**
     * Output triangle strip and and triangle arrays.
     * @return a 2-element array of GeometryArray; element 0 if non-null
     * will contain a TriangleStripArray, and element 1 if non-null will
     * contain a TriangleArray.
     */
    GeometryArray[] toStripAndTriangleArrays() {
	GeneralizedStrip.StripArray stripArray[] =
	    GeneralizedStrip.toStripsAndTriangles(this, frontFace, 4, 12) ;

	GeometryArray ga[] = new GeometryArray[2] ;

	if (stripArray[0] != null) {
	    ga[0] = new TriangleStripArray(stripArray[0].vertices.count,
					   vertexFormat,
					   stripArray[0].stripCounts.trim()) ;

	    copyVertexData(ga[0], stripArray[0].vertices) ;

	    vertexCount += stripArray[0].vertices.count ;
	    stripCount += stripArray[0].stripCounts.count ;
	}

	if (stripArray[1] != null) {
	    ga[1] = new TriangleArray(stripArray[1].vertices.count,
				      vertexFormat) ;

	    copyVertexData(ga[1], stripArray[1].vertices) ;
	    triangleCount += stripArray[1].vertices.count/3 ;
	}
	return ga ;
    }
}
