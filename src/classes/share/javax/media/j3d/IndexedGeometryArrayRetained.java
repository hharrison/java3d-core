/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.Vector;
import java.util.ArrayList;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;


/**
 * The IndexedGeometryArray object contains arrays of positional coordinates,
 * colors, normals and/or texture coordinates that describe
 * point, line, or surface geometry.  It is extended to create
 * the various primitive types (e.g., lines, triangle_strips, etc.)
 */

abstract class IndexedGeometryArrayRetained extends GeometryArrayRetained {

    // arrays to save indices for coord, color, normal, texcoord
    int		indexCoord[], indexColor[], indexNormal[];
    Object	indexTexCoord[];

    int		indexCount;

    int initialIndexIndex = 0;
    int validIndexCount = 0;

    // Following variables are only used in compile mode
    int[] compileIndexCount;
    int[] compileIndexOffset;

    int maxCoordIndex = 0;
    int maxColorIndex = 0;
    int maxNormalIndex = 0;
    int[] maxTexCoordIndices = null;
    
  void createIndexedGeometryArrayData(int indexCount) {
    this.indexCount    = indexCount;
    this.validIndexCount    = indexCount;
    
    if((this.vertexFormat & GeometryArray.COORDINATES) != 0)
    	this.indexCoord    = new int[indexCount];
    
    if((this.vertexFormat & GeometryArray.NORMALS) != 0)
    	this.indexNormal    = new int[indexCount];
    
    if((this.vertexFormat & GeometryArray.COLOR) != 0)
    	this.indexColor   = new int[indexCount];
    
    if((this.vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	this.indexTexCoord = new Object[this.texCoordSetCount];
	for (int i = 0; i < this.texCoordSetCount; i++) {
	    this.indexTexCoord[i] = new int[indexCount];
	}
	maxTexCoordIndices = new int[texCoordSetCount];
    }
  }

  
  Object cloneNonIndexedGeometry() {
     GeometryArrayRetained obj = null;
     int vOffset;
     
     switch (this.geoType) {
     case GEO_TYPE_INDEXED_LINE_SET:
        obj = new LineArrayRetained();
        break;
     case GEO_TYPE_INDEXED_POINT_SET:
        obj = new PointArrayRetained();
        break;
     case GEO_TYPE_INDEXED_QUAD_SET:
        obj = new QuadArrayRetained();
        break;
     case GEO_TYPE_INDEXED_TRI_SET:
        obj = new TriangleArrayRetained();
        break;
     }
     obj.createGeometryArrayData(validIndexCount, (vertexFormat & ~(GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED|GeometryArray.USE_NIO_BUFFER)),
				 texCoordSetCount, texCoordSetMap);
     obj.cloneSourceArray = this;
     obj.unIndexify(this);
 
     return (Object)obj;
  }

  void execute(long ctx, RenderAtom ra, boolean isNonUniformScale,
		boolean updateAlpha, float alpha) {
        throw new RuntimeException(J3dI18N.getString("IndexedGeometryArrayRetained0"));
  }


  /**
   * Gets current number of indices
   * @return indexCount
   */
  int getIndexCount(){
	return indexCount;
  }

    void doErrorCheck(int newMax) {
	doCoordCheck(newMax);
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		doColorCheck(newMax);
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    doTexCoordCheck(newMax, i);
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		doNormalCheck(newMax);
	    }	    
	}	
    }
    
    void doCoordCheck(int newMax) {
	// Check to make sure that the array length defined by the user is ateast maxCoordIndex long
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    if (newMax >= vertexCount) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
	    }
	}
	else {
	    if(( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
		    case PF:
			if(floatBufferRefCoords != null && 3 * newMax >= floatBufferRefCoords.limit() ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}
			break;
		    case PD:
			if(doubleBufferRefCoords != null && 3 * newMax >= doubleBufferRefCoords.limit() ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}
			break;
		    }
		}
		else {
		    if(interleavedFloatBufferImpl != null && stride * newMax >= interleavedFloatBufferImpl.limit() ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		    }
		}
	    } else {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & VERTEX_DEFINED)) {
		    case PF:
			if (floatRefCoords != null && (3 * newMax >= floatRefCoords.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}
			break;
		    case PD: 
			if (doubleRefCoords != null && (3 * newMax >= doubleRefCoords.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}

			break;
		    case P3F: 
			if (p3fRefCoords != null && (newMax >= p3fRefCoords.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}
			break;
		    case P3D: 
			if (p3dRefCoords != null && (newMax >= p3dRefCoords.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
			}
			break;
		    default:
			break;
		    }
		}
		else {
		    if (interLeavedVertexData != null && (stride * newMax >= interLeavedVertexData.length)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray23"));
		    }
		}
	    }
	}

    }	

    void doColorCheck(int newMax) {
	// If the new Value is greater than the old value, make sure there is array length
	// to support the change
	// Check to make sure that the array length defined by the user is ateast maxCoordIndex long
	if ((vertexFormat & GeometryArray.COLOR) == 0)
	    return;
	
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    if (newMax >= vertexCount) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
	    }
	}
	else {
	    int multiplier = getColorStride(); 

	    if(( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & COLOR_DEFINED)) {
		    case CF:
			if (floatBufferRefColors != null && multiplier * newMax >= floatBufferRefColors.limit()) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			} 
			break;
		    case CUB: 
			if (byteBufferRefColors != null && multiplier * newMax >= byteBufferRefColors.limit()) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			} 
			break;
		    }
		}
		else {
		    if(interleavedFloatBufferImpl != null &&
		       stride * newMax >= interleavedFloatBufferImpl.limit()) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		    }
		}
	    } else {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & COLOR_DEFINED)) {
		    case CF:
			if (floatRefColors != null && (multiplier * newMax >= floatRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}
			break;
		    case CUB: 
			if (byteRefColors != null && (multiplier * newMax >= byteRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}

			break;
		    case C3F: 
			if (c3fRefColors != null && (newMax >= c3fRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}
			break;
		    case C4F: 
			if (c4fRefColors != null && (newMax >= c4fRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}
			break;
		    case C3UB: 
			if (c3bRefColors != null && (newMax >= c3bRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}
			break;
		    case C4UB: 
			if (c4bRefColors != null && (newMax >= c4bRefColors.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
			}
			break;
		    default:
			break;
		    }
		} else {
		    if (interLeavedVertexData != null && (stride * newMax >= interLeavedVertexData.length)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray24"));
		    }
		}
	    }
	}

    }


    void doNormalCheck(int newMax) {
	if ((vertexFormat & GeometryArray.NORMALS) == 0)
	    return;

	// Check to make sure that the array length defined by the user is ateast maxCoordIndex long
	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    if (newMax >= vertexCount) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
	    }
	}
	else {
	    if(( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & GeometryArrayRetained.NORMAL_DEFINED)) {
		    case NF:
			if(floatBufferRefNormals != null && 3 * newMax >= floatBufferRefNormals.limit() ) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
			}
			break;
		    }
		}
		else {
		    if(interleavedFloatBufferImpl != null && stride * newMax >= interleavedFloatBufferImpl.limit() ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		    }
		}
	    } else {    
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & NORMAL_DEFINED)) {
		    case NF:
			if (floatRefNormals != null && (3 * newMax >= floatRefNormals.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
			}
			break;
		    case N3F: 
			if (v3fRefNormals != null && (newMax >= v3fRefNormals.length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
			}

			break;
		    default:
			break;
		    }
		}
		else {
		    if (interLeavedVertexData != null && (stride * newMax >= interLeavedVertexData.length)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray26"));
		    }
		}
	    }
	}

    }



    void doTexCoordCheck(int newMax, int texCoordSet) {

	// Check to make sure that the array length defined by the user is ateast maxCoordIndex long
	if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) == 0)
	    return;

	if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
	    if (newMax >= vertexCount) {
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
	    }
	}
	else {
	    int multiplier = getTexStride();

	    if(( vertexFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & GeometryArrayRetained.TEXCOORD_DEFINED)) {
		    case TF:
			FloatBufferWrapper texBuffer;
			texBuffer = (FloatBufferWrapper)(((J3DBuffer) refTexCoordsBuffer[texCoordSet]).getBufferImpl());
			if(refTexCoords[texCoordSet] != null &&  multiplier * newMax >= texBuffer.limit()) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
			}
			break;
		    }
		}
		else {
		    if(interleavedFloatBufferImpl != null && stride * newMax >= interleavedFloatBufferImpl.limit() ) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		    }
		}
	    } else {

		if ((vertexFormat & GeometryArray.INTERLEAVED) == 0) {
		    switch ((vertexType & TEXCOORD_DEFINED)) {
		    case TF:
			if (refTexCoords[texCoordSet] != null && (multiplier * newMax >= ((float[])refTexCoords[texCoordSet]).length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
			}
			break;
		    case T2F: 
			if (refTexCoords[texCoordSet] != null && (newMax >= ((TexCoord2f[])refTexCoords[texCoordSet]).length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
			}

			break;
		    case T3F: 
			if (refTexCoords[texCoordSet] != null && (newMax >= ((TexCoord3f[])refTexCoords[texCoordSet]).length)) {
			    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
			}
			break;
		    default:
			break;
		    }
		}
		else {
		    if (interLeavedVertexData != null && (stride * newMax >= interLeavedVertexData.length)) {
			throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray25"));
		    }
		}
	    }
	}

    }


    /**
     * Sets the coordinate index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndex the new coordinate index
     */
    final void setCoordinateIndex(int index, int coordinateIndex) {
	int newMax;
	newMax = doIndexCheck(index, maxCoordIndex, indexCoord, coordinateIndex);
	if (newMax > maxCoordIndex) {
	    doErrorCheck(newMax);
	}
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		maxColorIndex = newMax;
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = newMax;
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		maxNormalIndex = newMax;
	    }	    
	}
	
	geomLock.getLock();
	dirtyFlag |= INDEX_CHANGED;
	this.indexCoord[index] = coordinateIndex;
	maxCoordIndex = newMax;
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(true);
	}
    }

    int doIndexCheck(int index, int maxIndex, int[] indices, int dataValue) {
	int newMax = maxIndex;
	if (index < initialIndexIndex)
	    return newMax;

	if (index >= (initialIndexIndex+validIndexCount))
	    return newMax;

	if (dataValue < 0) {
	    // Throw an exception, since index is negative
	    throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray27"));
	    
	}
	
	if (newMax == indices[index]) {
	    if (dataValue >= newMax) {
		newMax = dataValue;
	    }
	    // Go thru the entire list and look for the max
	    else {
		for (int i = 0; i < indices.length; i++) {
		    if (indices[i] > newMax) {
			newMax = indices[i];
		    }
		}
	    }
	}
	else if (dataValue  > newMax)  {
	    newMax = dataValue;
	}
	return newMax;
    }

    int doIndicesCheck(int index, int maxIndex, int[] indices, int[] newIndices) {
	int newMax = maxIndex;
	boolean computeNewMax = false;
	int i, j, num = newIndices.length;
	boolean maxReset = false;
	for (j = 0; j < num; j++) {
	    if ((index+j) < initialIndexIndex)
		continue;

	    if ((index+j) >= (initialIndexIndex+validIndexCount))
		continue;
	    if (newIndices[j] < 0) {
		// Throw an exception, since index is negative
		throw new ArrayIndexOutOfBoundsException(J3dI18N.getString("IndexedGeometryArray27"));
	    
	    }
	    if (indices[index+j] == maxIndex) {
		if (newIndices[j] >= newMax) {
		    newMax = newIndices[j];
		    computeNewMax = false;
		    maxReset = true;
		}
		// Go thru the entire list and look for the max
		// If in the new list there is no value that is >=
		// to the old maximum
		else if (!maxReset){
		    computeNewMax = true;
		}
	    }
	    else if (newIndices[j]  >= newMax)  {
		newMax = newIndices[j];
		computeNewMax = false;
		maxReset = true;
	    }
	}
	if (computeNewMax) {
	    for (i = 0; i < indices.length; i++) {
		if (indices[i] > newMax) {
		    newMax = indices[i];
		}
	    }
	}
	return newMax;
    }


    /**
     * Sets the coordinate indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndices an array of coordinate indices
     */
    final void setCoordinateIndices(int index, int coordinateIndices[]) {
	int newMax;
	int i, j, num = coordinateIndices.length;
	newMax = doIndicesCheck(index, maxCoordIndex, indexCoord, coordinateIndices);
	if (newMax > maxCoordIndex) {
	    doErrorCheck(newMax);
	}
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		maxColorIndex = newMax;
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = newMax;
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		maxNormalIndex = newMax;
	    }	    
	}
	
	geomLock.getLock();
	dirtyFlag |= INDEX_CHANGED;
	maxCoordIndex = newMax;
	for (i=0, j = index; i < num;i++, j++) {
	    this.indexCoord[j] = coordinateIndices[i];
	}
	geomLock.unLock();
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(true);
	}
    }

    /**
     * Sets the color index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param colorIndex the new color index
     */
    final void setColorIndex(int index, int colorIndex) {
	int newMax = maxColorIndex;

	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    newMax = doIndexCheck(index, maxColorIndex, indexColor, colorIndex);
	    if (newMax > maxColorIndex) {
		doColorCheck(newMax);
	    }
	    geomLock.getLock();
	    // No need to set INDEX_CHANGED since IndexBuffer
	    // is used only when USE_COORD_INDEX_ONLY specified.
	    // In this case only coordinate index array is 
	    // considered.
	    this.indexColor[index] = colorIndex;
	    maxColorIndex = newMax;
	    geomLock.unLock();
	    if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	    }
	}
	else {
	    maxColorIndex = maxCoordIndex;
	    this.indexColor[index] = colorIndex;
	}

    }

    /**
     * Sets the color indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param colorIndices an array of color indices
     */
    final void setColorIndices(int index, int colorIndices[]) {
	int i, j, num = colorIndices.length;
	int newMax;
	
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    newMax = doIndicesCheck(index, maxColorIndex, indexColor, colorIndices);
	    if (newMax > maxColorIndex) {
		doColorCheck(newMax);
	    }
	    geomLock.getLock();
	    maxColorIndex = newMax;
	    for (i=0, j = index; i < num;i++, j++) {
		this.indexColor[j] = colorIndices[i];
	    }
	    geomLock.unLock();
	    if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	    }
	}
	else {
	    maxColorIndex = maxCoordIndex;
	    for (i=0, j = index; i < num;i++, j++) {
		this.indexColor[j] = colorIndices[i];
	    }
	}

    }

    /**
     * Sets the normal index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @param normalIndex the new normal index
     */
    final void setNormalIndex(int index, int normalIndex) {
	int newMax;
	
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    newMax = doIndexCheck(index, maxNormalIndex, indexNormal, normalIndex);
	    if (newMax > maxNormalIndex) {
		doNormalCheck(newMax);
	    }
	    geomLock.getLock();
	    maxNormalIndex = newMax;
	    this.indexNormal[index] = normalIndex;
	    geomLock.unLock();
	    if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	    }
	}
	else {
	    maxNormalIndex = maxCoordIndex;
	    this.indexNormal[index] = normalIndex;
	}

    }

    /**
     * Sets the normal indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param normalIndices an array of normal indices
     */
    final void setNormalIndices(int index, int normalIndices[]) {
	int i, j, num = normalIndices.length;
	int newMax;
	
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    newMax = doIndicesCheck(index, maxNormalIndex, indexNormal, normalIndices);
	    if (newMax > maxNormalIndex) {
		doNormalCheck(newMax);
	    }
	    geomLock.getLock();
	    for (i=0, j = index; i < num;i++, j++) {
		this.indexNormal[j] = normalIndices[i];
	    }
	    maxNormalIndex = newMax;
	    geomLock.unLock();
	    if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	    }
	}
	else {
	    maxNormalIndex = maxCoordIndex;
	    for (i=0, j = index; i < num;i++, j++) {
		this.indexNormal[j] = normalIndices[i];
	    }
	}

    }

    /**
     * Sets the texture coordinate index associated with the vertex at
     * the specified index for this object.
     * @param texCoordSet the texture coordinate set
     * @param index the vertex index
     * @param texCoordIndex the new texture coordinate index
     */
    final void setTextureCoordinateIndex(int texCoordSet, int index, int texCoordIndex) {
	int newMax;
	int [] indices = (int[])this.indexTexCoord[texCoordSet];
	
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    newMax = doIndexCheck(index, maxTexCoordIndices[texCoordSet],indices, texCoordIndex);
	    if (newMax > maxTexCoordIndices[texCoordSet]) {
		doTexCoordCheck(newMax, texCoordSet);
	    }
	    geomLock.getLock();
	    maxTexCoordIndices[texCoordSet] = newMax;
	    indices[index] = texCoordIndex;
	    geomLock.unLock();
	    if (!inUpdater && source != null && source.isLive()) {
		sendDataChangedMessage(false);
	    }
	}
	else {
	    maxTexCoordIndices[texCoordSet] = maxCoordIndex;
	    indices[index] = texCoordIndex;
	}


    }

    /**
     * Sets the texture coordinate indices associated with the vertices
     * starting at the specified index for this object.
     * @param texCoordSet the texture coordinate set
     * @param index the vertex index
     * @param texCoordIndices an array of texture coordinate indices
     */
  final void setTextureCoordinateIndices(int texCoordSet, int index, int texCoordIndices[]) {
      int i, j, num = texCoordIndices.length;
      int [] indices = (int[])this.indexTexCoord[texCoordSet];

      int newMax;
	
      if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	  newMax = doIndicesCheck(index, maxTexCoordIndices[texCoordSet], indices, texCoordIndices);
	  if (newMax > maxTexCoordIndices[texCoordSet]) {
	      doTexCoordCheck(newMax, texCoordSet);
	  }
	  geomLock.getLock();
	  maxTexCoordIndices[texCoordSet] = newMax;
	  for (i=0, j = index; i < num;i++, j++) {
	      indices[j] = texCoordIndices[i];
	  }
	  geomLock.unLock();
	  if (!inUpdater && source != null && source.isLive()) {
	      sendDataChangedMessage(false);
	  }

      }
      else {
	  maxTexCoordIndices[texCoordSet] = maxCoordIndex;
	  for (i=0, j = index; i < num;i++, j++) {
	      indices[j] = texCoordIndices[i];
	  }
      }

  }

    /**
     * Retrieves the coordinate index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @return the coordinate index
     */
    final int getCoordinateIndex(int index) {
	return this.indexCoord[index];
    }

    /**
     * Retrieves the coordinate indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param coordinateIndices array that will receive the coordinate indices
     */
  final void getCoordinateIndices(int index, int coordinateIndices[]) {
    int i, j, num = coordinateIndices.length;

    for (i=0, j = index;i < num;i++, j++)
      {
	coordinateIndices[i] = this.indexCoord[j];
      }
  }

    /**
     * Retrieves the color index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @return the color index
     */
    final int getColorIndex(int index) {
	return this.indexColor[index];
    }

    /**
     * Retrieves the color indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param colorIndices array that will receive the color indices
     */
  final void getColorIndices(int index, int colorIndices[]) {
    int i, j, num = colorIndices.length;

    for (i=0, j = index;i < num;i++, j++)
      {
	colorIndices[i] = this.indexColor[j];
      }
  }

    /**
     * Retrieves the normal index associated with the vertex at
     * the specified index for this object.
     * @param index the vertex index
     * @return the normal index
     */
    final int getNormalIndex(int index) {
	return this.indexNormal[index];
    }

    /**
     * Retrieves the normal indices associated with the vertices starting at
     * the specified index for this object.
     * @param index the vertex index
     * @param normalIndices array that will receive the normal indices
     */
  final void getNormalIndices(int index, int normalIndices[]) {
    int i, j, num = normalIndices.length;

    for (i=0, j = index;i < num;i++, j++)
      {
	normalIndices[i] = this.indexNormal[j];
      }
  }

    /**
     * Retrieves the texture coordinate index associated with the vertex at
     * the specified index for this object.
     * @param texCoordSet the texture coordinate set
     * @param index the vertex index
     * @return the texture coordinate index
     */
    final int getTextureCoordinateIndex(int texCoordSet, int index) {
	return ((int[])this.indexTexCoord[texCoordSet])[index];
    }

    /**
     * Retrieves the texture coordinate indices associated with the vertices
     * starting at the specified index for this object.
     * @param texCoordSet the texture coordinate set
     * @param index the vertex index
     * @param texCoordIndices array that will receive the texture coordinate indices
     */
  final void getTextureCoordinateIndices(int texCoordSet, int index, int texCoordIndices[]) {
    int i, j, num = texCoordIndices.length;
    int [] indices = (int[])this.indexTexCoord[texCoordSet];

    for (i=0, j = index;i < num;i++, j++)
      {
	texCoordIndices[i] = indices[j];
      }
  }

    // used for GeometryArrays
    native void executeIndexedGeometry(long ctx,
			GeometryArrayRetained geo, int geo_type, 
			boolean isNonUniformScale,
			boolean useAlpha,
			boolean multiScreen,
			boolean ignoreVertexColors,
		        int initialIndexIndex,
			int indexCount,
			int vertexCount, int vformat, 
			int texCoordSetCount, int texCoordSetMap[],
			int texCoordSetMapLen,
		        int[] texCoordSetOffset,
			int numActiveTexUnitState,
			int[] texUnitStateMap,
			float[] varray, float[] cdata, 
		        int texUnitIndex, int cdirty, 
			int[] indexCoord);

    // used for interleaved, by reference, nio buffer 
    native void executeIndexedGeometryBuffer(long ctx,
				       GeometryArrayRetained geo, int geo_type, 
				       boolean isNonUniformScale,
				       boolean useAlpha,
				       boolean multiScreen,
				       boolean ignoreVertexColors,
				       int initialIndexIndex,
				       int indexCount,
				       int vertexCount, int vformat, 
				       int texCoordSetCount, int texCoordSetMap[],
				       int texCoordSetMapLen,
				       int[] texCoordSetOffset,
				       int numActiveTexUnitState,
				       int[] texUnitStateMap,
				       Object varray, float[] cdata, 
				       int texUnitIndex, int cdirty, 
				       int[] indexCoord);


	
    native void executeIndexedGeometryVA(long ctx,
					 GeometryArrayRetained geo, int geo_type, 
					 boolean isNonUniformScale, 
					 boolean multiScreen,
					 boolean ignoreVertexColors,
					 int initialIndexIndex,
					 int validIndexCount,
					 int vertexCount,		 
					 int vformat,
					 int vdefined,
					 float[] vfcoords, double[] vdcoords,
					 float[] cfdata, byte[] cbdata,
					 float[] ndata,
					 int pass, int texcoordmaplength, 
					 int[] texcoordoffset, 
					 int numActiveTexUnitState, int[] texunitstatemap,
					 int texstride, Object[] texCoords,
					 int cdirty, 
					 int[] indexCoord);

    // non interleaved, by reference, nio buffer
    native void executeIndexedGeometryVABuffer(long ctx,
					       GeometryArrayRetained geo, int geo_type, 
					       boolean isNonUniformScale, 
					       boolean multiScreen,
					       boolean ignoreVertexColors,
					       int initialIndexIndex,
					       int validIndexCount,
					       int vertexCount,		 
					       int vformat,
					       int vdefined,
					       Object vcoords,
					       Object cdataBuffer,	       
					       float[] cfdata, byte[] cbdata,
					       Object normal,
					       int pass, int texcoordmaplength, 
					       int[] texcoordoffset, 
					       int numActiveTexUnitState, int[] texunitstatemap,
					       int texstride, Object[] texCoords,
					       int cdirty, 
					       int[] indexCoord);

    // used for IndexedGeometry
    native void buildIndexedGeometry(long ctx, GeometryArrayRetained geo, int geo_type, 
				     boolean isNonUniformScale, boolean updateAlpha,
				     float alpha,
				     boolean ignoreVertexColors,
				     int initialIndexIndex,
				     int validIndexCount,
				     int vertexCount,
				     int vformat, 
				     int texCoordSetCount, int texCoordSetMap[],
				     int texCoordSetMapLen,
				     int[] texCoordSetMapOffset, 
				     double[] xform, double[] nxform,
				     float[] varray, int[] indexCoord);


    void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
		 boolean updateAlpha, float alpha,
		 boolean multiScreen, int screen,
		 boolean ignoreVertexColors, int pass) {
	int cdirty;
	boolean useAlpha = false;
	Object[] retVal;
	if (mirrorGeometry != null) {
	    mirrorGeometry.execute(cv, ra, isNonUniformScale, updateAlpha, alpha,
				   multiScreen, screen,
				   ignoreVertexColors, pass);
	    return;
	}
	//By reference with java array
	if ((vertexFormat & GeometryArray.USE_NIO_BUFFER) == 0) {
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
		float[] vdata;
		//	    System.out.println("by-copy");
		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			retVal = updateAlphaInVertexData(cv, screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			vdata = (float[])retVal[1];

			// D3D only
			if (alpha != lastScreenAlpha) {
			    // handle multiple screen case
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			vdata = vertexData;
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    // geomLock is get in MasterControl when
		    // RenderBin render the geometry. So it is safe
		    // just to set the dirty flag here
		    dirtyFlag = 0;
		}
	    
		executeIndexedGeometry(cv.ctx, this, geoType, isNonUniformScale, 
				       useAlpha,
				       multiScreen,
				       ignoreVertexColors,
				       initialIndexIndex, 
				       validIndexCount,
                // Vertex Count is maxCoordIndex + 1
				       maxCoordIndex + 1, 
				       ((vertexFormat & GeometryArray.COLOR) != 0)?(vertexFormat|GeometryArray.COLOR_4):vertexFormat,
				       texCoordSetCount, texCoordSetMap,
				       (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
				       texCoordSetMapOffset, 
				       cv.numActiveTexUnit, cv.texUnitStateMap, 
				       vdata, null,
				       pass, cdirty, indexCoord);


	    } // end of non by reference
	    else if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		if(interLeavedVertexData == null)
		    return;
		
		float[] cdata = null;

		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			retVal = updateAlphaInInterLeavedData(cv, screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			cdata = (float[])retVal[1];
			if (alpha != lastScreenAlpha) { 
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    dirtyFlag = 0;
		}
		
		executeIndexedGeometry(cv.ctx, this, geoType, isNonUniformScale, 
				       useAlpha,
				       multiScreen,
				       ignoreVertexColors,
				       initialIndexIndex, 
				       validIndexCount, 
				       maxCoordIndex + 1,
				       vertexFormat,
				       texCoordSetCount, texCoordSetMap,
				       (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
				       texCoordSetMapOffset, 
				       cv.numActiveTexUnit, cv.texUnitStateMap, 
				       interLeavedVertexData, cdata,
				       pass, cdirty, indexCoord);
	    }  //end of interleaved
	    else { 
		// Check if a vertexformat is set, but the array is null
		// if yes, don't draw anything
		if ((vertexType == 0) ||
		    ((vertexType & VERTEX_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.COLOR) != 0) &&
		     (vertexType & COLOR_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.NORMALS) != 0) &&
		     (vertexType & NORMAL_DEFINED) == 0) || 
		    (((vertexFormat& GeometryArray.TEXTURE_COORDINATE) != 0) &&
		     (vertexType & TEXCOORD_DEFINED) == 0)) {
		    return;  
		} else {
		    byte[] cbdata = null;
		    float[] cfdata = null;
		    
		    if ((vertexType & (CF | C3F | C4F )) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cfdata = updateAlphaInFloatRefColors(cv,
								     screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				cfdata = mirrorFloatRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    
			    }
			    dirtyFlag = 0;
			}
		    } else if ((vertexType & (CUB| C3UB | C4UB)) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cbdata = updateAlphaInByteRefColors(
								    cv, screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				cbdata = mirrorUnsignedByteRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    }
			    dirtyFlag = 0;
			}
		    } else {
			cdirty = dirtyFlag;
		    }
		    
		    int vdefined = 0;
		    if((vertexType & (PF | P3F)) != 0)
			vdefined |= COORD_FLOAT;
		    if((vertexType & (PD | P3D)) != 0)
			vdefined |= COORD_DOUBLE;
		    if((vertexType & (CF | C3F | C4F)) != 0)
			vdefined |= COLOR_FLOAT;
		    if((vertexType & (CUB| C3UB | C4UB)) != 0)
			vdefined |= COLOR_BYTE;
		    if((vertexType & NORMAL_DEFINED) != 0)
			vdefined |= NORMAL_FLOAT;
		    if((vertexType & TEXCOORD_DEFINED) != 0)
			vdefined |= TEXCOORD_FLOAT;
		    
		    executeIndexedGeometryVA(cv.ctx, this, geoType, isNonUniformScale, 
					     multiScreen, 
					     ignoreVertexColors,
					     initialIndexIndex,
					     validIndexCount,
					     maxCoordIndex + 1,
					     (vertexFormat | c4fAllocated),
					     vdefined,
					     mirrorFloatRefCoords, mirrorDoubleRefCoords,
					     cfdata, cbdata,
					     mirrorFloatRefNormals,
					     pass,
					     ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
					     texCoordSetMap,
					     cv.numActiveTexUnit,
					     cv.texUnitStateMap,
					     texCoordStride,
					     mirrorRefTexCoords, cdirty, indexCoord);

		}
	    } // end of non interleaved and by reference
	}//end of non io buffer

	else {
	    if ((vertexFormat & GeometryArray.INTERLEAVED) != 0) {
		if( interleavedFloatBufferImpl == null)
		    return;
		
		float[] cdata = null;

		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			retVal = updateAlphaInInterLeavedData(cv, screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			cdata = (float[])retVal[1];
			if (alpha != lastScreenAlpha) { 
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    dirtyFlag = 0;
		}
		
		executeIndexedGeometryBuffer(cv.ctx, this, geoType, isNonUniformScale, 
				       useAlpha,
				       multiScreen,
				       ignoreVertexColors,
				       initialIndexIndex, 
				       validIndexCount, 
				       maxCoordIndex + 1,
				       vertexFormat,
				       texCoordSetCount, texCoordSetMap,
				       (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
				       texCoordSetMapOffset, 
				       cv.numActiveTexUnit, cv.texUnitStateMap, 
				       interleavedFloatBufferImpl.getBufferAsObject(), cdata,
				       pass, cdirty, indexCoord);
	    }  //end of interleaved
	    else { 
		// Check if a vertexformat is set, but the array is null
		// if yes, don't draw anything
		if ((vertexType == 0) ||
		    ((vertexType & VERTEX_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.COLOR) != 0) &&
		     (vertexType & COLOR_DEFINED) == 0) ||
		    (((vertexFormat & GeometryArray.NORMALS) != 0) &&
		     (vertexType & NORMAL_DEFINED) == 0) || 
		    (((vertexFormat& GeometryArray.TEXTURE_COORDINATE) != 0) &&
		     (vertexType & TEXCOORD_DEFINED) == 0)) {
		    return;  
		} else {
		    byte[] cbdata = null;
		    float[] cfdata = null;
		    
		    if ((vertexType & CF ) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cfdata = updateAlphaInFloatRefColors(cv,
								     screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				// TODO: handle transparency case
				//cfdata = null;
				cfdata = mirrorFloatRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    
			    }
			    dirtyFlag = 0;
			}
		    } else if ((vertexType & CUB ) != 0) {
			synchronized (this) {
			    cdirty = dirtyFlag;
			    if (updateAlpha && !ignoreVertexColors) {
				cbdata = updateAlphaInByteRefColors(
								    cv, screen, alpha);
				if (alpha != lastScreenAlpha) {
				    lastScreenAlpha = alpha;
				    cdirty |= COLOR_CHANGED;
				}
			    } else {
				// TODO: handle transparency case
				// cbdata = null;
				cbdata = mirrorUnsignedByteRefColors[0];
				// if transparency switch between on/off
				if (lastScreenAlpha != -1) {
				    lastScreenAlpha = -1;
				    cdirty |= COLOR_CHANGED;
				}
			    }
			    dirtyFlag = 0;
			}
		    } else {
			cdirty = dirtyFlag;
		    }

		    Object vcoord = null, cdataBuffer=null, normal=null;
		    
		    int vdefined = 0;
		    if((vertexType & PF)  != 0) {
			vdefined |= COORD_FLOAT;
			vcoord = floatBufferRefCoords.getBufferAsObject();
		    } else if((vertexType & PD ) != 0) {
			vdefined |= COORD_DOUBLE;
			vcoord = doubleBufferRefCoords.getBufferAsObject();
		    }
		    if((vertexType & CF ) != 0) {
			vdefined |= COLOR_FLOAT;
			cdataBuffer = floatBufferRefColors.getBufferAsObject();
		    } else if((vertexType & CUB) != 0) {
			vdefined |= COLOR_BYTE;
			cdataBuffer = byteBufferRefColors.getBufferAsObject();
		    }
		    
		    if((vertexType & NORMAL_DEFINED) != 0) {
			vdefined |= NORMAL_FLOAT;
			normal = floatBufferRefNormals.getBufferAsObject();
		    }
		    
		    if((vertexType & TEXCOORD_DEFINED) != 0)
		       vdefined |= TEXCOORD_FLOAT;

		    executeIndexedGeometryVABuffer(cv.ctx, this, geoType, isNonUniformScale, 
						   multiScreen, 
						   ignoreVertexColors,
						   initialIndexIndex,
						   validIndexCount,
						   maxCoordIndex + 1,
						   (vertexFormat | c4fAllocated),
						   vdefined,
						   vcoord,
						   cdataBuffer,
						   cfdata, cbdata,
						   normal,
						   pass,
						   ((texCoordSetMap == null) ? 0:texCoordSetMap.length),
						   texCoordSetMap,
						   cv.numActiveTexUnit,
						   cv.texUnitStateMap,
						   texCoordStride,
						   refTexCoords, cdirty, indexCoord);

		}
	    } // end of non interleaved and by reference
	} // end of nio buffer
    }

    void buildGA(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
		 boolean updateAlpha, float alpha, boolean ignoreVertexColors,
		 Transform3D xform, Transform3D nxform) {
	int cdirty;
	boolean useAlpha = false;
	Object[] retVal;
	if (mirrorGeometry != null) {
	    ((GeometryArrayRetained)mirrorGeometry).buildGA(cv, ra, isNonUniformScale, updateAlpha, alpha,
		    ignoreVertexColors, xform, nxform);
	}
	else {
	    
	    if ((vertexFormat & GeometryArray.BY_REFERENCE) == 0) {
		float[] vdata;
		//	    System.out.println("by-copy");
		synchronized (this) {
		    cdirty = dirtyFlag;
		    if (updateAlpha && !ignoreVertexColors) {
			// update the alpha values
			retVal = updateAlphaInVertexData(cv, cv.screen.screen, alpha);
			useAlpha = (retVal[0] == Boolean.TRUE);
			vdata = (float[])retVal[1];

			// D3D only
			if (alpha != lastScreenAlpha) {
			    // handle multiple screen case
			    lastScreenAlpha = alpha;
			    cdirty |= COLOR_CHANGED;
			}
		    } else {
			vdata = vertexData;
			// if transparency switch between on/off
			if (lastScreenAlpha != -1) {
			    lastScreenAlpha = -1;
			    cdirty |= COLOR_CHANGED;
			}
		    }
		    // geomLock is get in MasterControl when
		    // RenderBin render the geometry. So it is safe
		    // just to set the dirty flag here
		    dirtyFlag = 0;
		}

		buildIndexedGeometry(cv.ctx, this, geoType, isNonUniformScale, 
				     updateAlpha, alpha, ignoreVertexColors,
				     initialIndexIndex,
				     validIndexCount,
				     maxCoordIndex + 1,
				     vertexFormat, 
				     texCoordSetCount, texCoordSetMap,
				     (texCoordSetMap == null) ? 0 : texCoordSetMap.length,
				     texCoordSetMapOffset, 
				     (xform == null) ? null : xform.mat,
				     (nxform == null) ? null : nxform.mat,
				     vdata, indexCoord);
	    }
	}
    }
    
    void mergeGeometryArrays(ArrayList list) {
	int numMerge = list.size();
	int[] texCoord = null;
	indexCount = 0;
	for (int i=0; i < numMerge; i++) {
	    IndexedGeometryArrayRetained geo= (IndexedGeometryArrayRetained)list.get(i);
	    indexCount += geo.validIndexCount;
	}
	validIndexCount = indexCount;
	initialIndexIndex = 0;
	compileIndexCount = new int[numMerge];
	compileIndexOffset = new int[numMerge];
	indexCoord = new int[indexCount];
	if ((vertexFormat  & GeometryArray.COLOR) != 0) 
	    indexColor = new int[indexCount];
	if ((vertexFormat  &  GeometryArray.NORMALS) != 0) 
	    indexNormal = new int[indexCount];
	// We only merge if texCoordSetCount = 1
	if ((vertexFormat  &  GeometryArray.TEXTURE_COORDINATE) != 0) {
	    indexTexCoord = new Object[1];
	    indexTexCoord[0] = new int[indexCount];
	    texCoord = (int[])indexTexCoord[0];
	}
	int curDataOffset = 0;
	int curIndexOffset = 0;
	for (int i = 0; i < numMerge; i++) {
	    IndexedGeometryArrayRetained geo= (IndexedGeometryArrayRetained)list.get(i);
	    int curIndexCount = geo.validIndexCount;
	    compileIndexCount[i] = curIndexCount;
	    // Copy all the indices
	    for (int j = 0; j < curIndexCount; j++) {
		indexCoord[j+curIndexOffset] = geo.indexCoord[j+geo.initialIndexIndex]+curDataOffset;
		if ((vertexFormat  & GeometryArray.COLOR) != 0) 
		    indexColor[j+curIndexOffset] = geo.indexColor[j+geo.initialIndexIndex]+curDataOffset;
		if ((vertexFormat  &  GeometryArray.NORMALS) != 0) 
		    indexNormal[j+curIndexOffset] = geo.indexNormal[j+geo.initialIndexIndex]+curDataOffset;
		if ((vertexFormat  &  GeometryArray.TEXTURE_COORDINATE) != 0) 
		    texCoord[j+curIndexOffset] = ((int[])geo.indexTexCoord[0])[j+geo.initialIndexIndex]+curDataOffset;
	    }
	    maxCoordIndex = geo.maxCoordIndex +curDataOffset;
	    compileIndexOffset[i] = curIndexOffset;
	    curDataOffset += geo.vertexCount;
	    curIndexOffset += curIndexCount;
	}
	// reset the max Values

	// call the super to merge the vertex data
	super.mergeGeometryArrays(list);	
    }


    boolean isWriteStatic() {

	if (!super.isWriteStatic() ||
	    source.getCapability(IndexedGeometryArray.ALLOW_COORDINATE_INDEX_WRITE ) ||
	    source.getCapability(IndexedGeometryArray.ALLOW_COLOR_INDEX_WRITE) || 
	    source.getCapability(IndexedGeometryArray.ALLOW_NORMAL_INDEX_WRITE) ||
	    source.getCapability(IndexedGeometryArray.ALLOW_TEXCOORD_INDEX_WRITE)) 
	    return false;

	return true;
    }
    
    /**
     * Gets current number of indices
     * @return indexCount
     */
    int getIndexCount(int id){
	return compileIndexCount[id];
    }

    int computeMaxIndex(int initial, int count, int[] indices) {
	int maxIndex = 0;
	for (int i = initial; i < (initial+count); i++) {
	    if (indices[i] > maxIndex) {
		maxIndex = indices[i];
	    }
	}
	return maxIndex;
	
    }

    void setValidIndexCount(int validIndexCount) {
	if (validIndexCount < 0) {
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedGeometryArray21"));
	}
	if ((initialIndexIndex + validIndexCount) > indexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedGeometryArray22"));
	}
	int newCoordMax =0;
	int newColorIndex=0;
	int newNormalIndex=0;
	int newTexCoordIndex[]=null;
	
	newCoordMax = computeMaxIndex(initialIndexIndex, validIndexCount,indexCoord );
	doErrorCheck(newCoordMax);
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		newColorIndex = computeMaxIndex(initialIndexIndex, validIndexCount, indexColor);
		doColorCheck(newColorIndex);
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		newTexCoordIndex = new int[texCoordSetCount];
		for (int i = 0; i < texCoordSetCount; i++) {
		   newTexCoordIndex[i] =  computeMaxIndex(initialIndexIndex,validIndexCount,
								  (int[])indexTexCoord[i]);
		   doTexCoordCheck(newTexCoordIndex[i], i);
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		newNormalIndex = computeMaxIndex(initialIndexIndex, validIndexCount, indexNormal);
		doNormalCheck(newNormalIndex);
	    }
	}

	geomLock.getLock();
	this.validIndexCount = validIndexCount;
	maxCoordIndex = newCoordMax;
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    maxColorIndex = newColorIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = newTexCoordIndex[i];
		}
	    }
	    maxNormalIndex = newNormalIndex;
	}
	else {
	    maxColorIndex = maxCoordIndex;
	    maxNormalIndex = maxCoordIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = maxCoordIndex;
		}
	    }
	}
	geomLock.unLock();
	// bbox is computed for the entries list.
	// so, send as false
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(true);
	}
    
    }

    void setInitialIndexIndex(int initialIndexIndex) {
	if ((initialIndexIndex + validIndexCount) > indexCount) {
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedGeometryArray22"));
	}
	int newCoordMax =0;
	int newColorIndex=0;
	int newNormalIndex=0;
	int newTexCoordIndex[]=null;
	
	newCoordMax = computeMaxIndex(initialIndexIndex, validIndexCount, indexCoord);
	doErrorCheck(newCoordMax);
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		newColorIndex = computeMaxIndex(initialIndexIndex, validIndexCount, indexColor);
		doColorCheck(newColorIndex);
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		newTexCoordIndex = new int[texCoordSetCount];
		for (int i = 0; i < texCoordSetCount; i++) {
		   newTexCoordIndex[i] =  computeMaxIndex(initialIndexIndex,validIndexCount,
							  (int[])indexTexCoord[i]);
		   doTexCoordCheck(newTexCoordIndex[i], i);
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		newNormalIndex = computeMaxIndex(initialIndexIndex, validIndexCount, indexNormal);
		doNormalCheck(newNormalIndex);
	    }
	}
	    
 	geomLock.getLock();
	dirtyFlag |= INDEX_CHANGED;
	this.initialIndexIndex = initialIndexIndex;
	maxCoordIndex = newCoordMax;
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    maxColorIndex = newColorIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = newTexCoordIndex[i];
		}
	    }
	    maxNormalIndex = newNormalIndex;
	}
	else {
	    maxColorIndex = maxCoordIndex;
	    maxNormalIndex = maxCoordIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (int i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = maxCoordIndex;
		}
	    }
	}
	geomLock.unLock();
	// bbox is computed for the entries list.
	// so, send as false
	if (!inUpdater && source != null && source.isLive()) {
	    sendDataChangedMessage(true);
	}
    }

    int getInitialIndexIndex() {
	return initialIndexIndex;
    }
    
    int getValidIndexCount() {
	return validIndexCount;
    }
    void handleFrequencyChange(int bit) {
	if ((bit == IndexedGeometryArray.ALLOW_COORDINATE_INDEX_WRITE) ||
	    (((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) &&
	     ((vertexFormat & GeometryArray.COLOR) != 0) &&
	     bit == IndexedGeometryArray.ALLOW_COLOR_INDEX_WRITE) ||
	    (((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) &&
	     ((vertexFormat & GeometryArray.NORMALS) != 0) &&
	     bit == IndexedGeometryArray.ALLOW_NORMAL_INDEX_WRITE) ||
	    (((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0)&&
	     ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0)&&
	     bit == IndexedGeometryArray.ALLOW_TEXCOORD_INDEX_WRITE)) {
	    setFrequencyChangeMask(bit, 0x1);
	}
	else {
	    super.handleFrequencyChange(bit);
	}
    }
}
