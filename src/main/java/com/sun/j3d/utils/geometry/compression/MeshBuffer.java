/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.geometry.compression;

import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * This class mirrors the vertex mesh buffer stack supported by the geometry
 * compression semantics.
 */
class MeshBuffer {
    // 
    // The fixed-length mesh buffer stack is represented by circular buffers.
    // Three stack representations are provided: vertices, positions, and
    // indices.
    // 
    // The vertex representation stores references to CompressionStreamVertex
    // objects.  The position representation stores references to Point3f,
    // Vector3f, Color3f, and Color4f objects, while the index representation
    // stores indices into externally maintained arrays of those objects.  All
    // these representations may be used independently and all provide access
    // to the stored references via a mesh buffer index.
    //
    // In addition, the position and index representations provide lookup
    // mechanisms to check if positions or indices exist in the mesh buffer
    // and return their mesh buffer indices if they do.  This is used to
    // implement a limited meshing algorithm which reduces the number of
    // vertices when non-stripped abutting facets are added to a compression
    // stream.
    //
    static final int NOT_FOUND = -1 ;

    private static final int SIZE = 16 ;
    private static final int NAN_HASH =
	new Point3f(Float.NaN, Float.NaN, Float.NaN).hashCode() ;

    private int topIndex = SIZE - 1 ;
    private int positionIndices[] = new int[SIZE] ;
    private int normalIndices[] = new int[SIZE] ;
    private int colorIndices[] = new int[SIZE] ;

    private int topPosition = SIZE - 1 ;
    private int positionHashCodes[] = new int[SIZE] ;
    private Point3f positions[] = new Point3f[SIZE] ;
    private Vector3f normals[] = new Vector3f[SIZE] ;
    private Color3f colors3[] = new Color3f[SIZE] ;
    private Color4f colors4[] = new Color4f[SIZE] ;

    private int topVertex = SIZE - 1 ;
    private CompressionStreamVertex vertices[] =
	new CompressionStreamVertex[SIZE] ;

    MeshBuffer() {
	for (int i = 0 ; i < SIZE ; i++) {
	    positionHashCodes[i] = NAN_HASH ;

	    positionIndices[i] = NOT_FOUND ;
	    normalIndices[i] = NOT_FOUND ;
	    colorIndices[i] = NOT_FOUND ;
	}
    }

    private static int nextTop(int top) {
	// The stack top references an element in the fixed-length backing
	// array in which the stack is stored.  Stack elements below it have
	// decreasing indices into the backing array until element 0, at which
	// point the indices wrap to the end of the backing array and back to
	// the top.
	//
	// A push is accomplished by incrementing the stack top in a circular
	// buffer and storing the data into the new stack element it
	// references.  The bottom of the stack is the element with the next
	// higher index from the top in the backing array, and is overwritten
	// with each new push.
	return (top + 1) % SIZE ;
    }

    private static int flipOffset(int top, int offset) {
	// Flips an offset relative to the beginning of the backing array to
	// an offset from the top of the stack.  Also works in reverse, from
	// an offset from the top of the stack to an offset from the beginning
	// of the backing array.
	if (offset > top) offset -= SIZE ;
	return top - offset ;
    }

    //
    // Mesh buffer vertex stack.  This is currently only used for vertex
    // lookup during the quantization pass in order to compute delta values;
    // no mesh reference lookup is necessary.
    //
    void push(CompressionStreamVertex v) {
	topVertex = nextTop(topVertex) ;
	vertices[topVertex] = v ;
    }

    CompressionStreamVertex getVertex(int meshReference) {
	return vertices[flipOffset(topVertex, meshReference)] ;
    }


    //
    // Mesh buffer index stack and index reference lookup support.
    //
    void push(int positionIndex, int normalIndex) {
	topIndex = nextTop(topIndex) ;

	positionIndices[topIndex] = positionIndex ;
	normalIndices[topIndex] = normalIndex ;
    }

    void push(int positionIndex, int colorIndex, int normalIndex) {
	push(positionIndex, normalIndex) ;
	colorIndices[topIndex] = colorIndex ;
    }

    int getMeshReference(int positionIndex) {
	int index ;
	for (index = 0 ; index < SIZE ; index++)
	    if (positionIndices[index] == positionIndex)
		break ;

	if (index == SIZE) return NOT_FOUND ;
	return flipOffset(topIndex, index) ;
    }

    int getPositionIndex(int meshReference) {
	return positionIndices[flipOffset(topIndex, meshReference)] ;
    }

    int getColorIndex(int meshReference) {
	return colorIndices[flipOffset(topIndex, meshReference)] ;
    }

    int getNormalIndex(int meshReference) {
	return normalIndices[flipOffset(topIndex, meshReference)] ;
    }


    //
    // Mesh buffer position stack and position reference lookup support.
    //
    void push(Point3f position, Vector3f normal) {
	topPosition = nextTop(topPosition) ;

	positionHashCodes[topPosition] = position.hashCode() ;
	positions[topPosition] = position ;
	normals[topPosition] = normal ;
    }

    void push(Point3f position, Color3f color, Vector3f normal) {
	push(position, normal) ;
	colors3[topPosition] = color ;
    }

    void push(Point3f position, Color4f color, Vector3f normal) {
	push(position, normal) ;
	colors4[topPosition] = color ;
    }

    void push(Point3f position, Object color, Vector3f normal) {
	push(position, normal) ;
	if (color instanceof Color3f)
	    colors3[topPosition] = (Color3f)color ;
	else
	    colors4[topPosition] = (Color4f)color ;
    }

    int getMeshReference(Point3f position) {
	int index ;
	int hashCode = position.hashCode() ;

	for (index = 0 ; index < SIZE ; index++)
	    if (positionHashCodes[index] == hashCode)
		if (positions[index].equals(position))
		    break ;

	if (index == SIZE) return NOT_FOUND ;
	return flipOffset(topPosition, index) ;
    }

    Point3f getPosition(int meshReference) {
	return positions[flipOffset(topPosition, meshReference)] ;
    }

    Color3f getColor3(int meshReference) {
	return colors3[flipOffset(topPosition, meshReference)] ;
    }

    Color4f getColor4(int meshReference) {
	return colors4[flipOffset(topPosition, meshReference)] ;
    }

    Vector3f getNormal(int meshReference) {
	return normals[flipOffset(topPosition, meshReference)] ;
    }
}
