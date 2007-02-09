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
import java.util.Vector;
import java.util.ArrayList;

/**
 * The IndexedGeometryStripArray object is an abstract class that is extended for
 * a set of IndexedGeometryArray strip primitives.  These include LINE_STRIP,
 * TRIANGLE_STRIP, and TRIANGLE_FAN.
 */

abstract class IndexedGeometryStripArrayRetained extends IndexedGeometryArrayRetained {

    // Array of per-strip vertex counts
    int stripIndexCounts[];

    // Following variables are only used in compile mode
    int[] compileStripICOffset;
    int[] compileIndexLength;

   /**
    * Set stripIndexCount data into local array
    */
   void setStripIndexCounts(int stripIndexCounts[]) {
	int i, num = stripIndexCounts.length, total = 0;

        for (i=0; i < num; i++) {
	    total += stripIndexCounts[i];
	    if (this instanceof IndexedLineStripArrayRetained) {
		if (stripIndexCounts[i] < 2) {
		    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArrayRetained1"));
		}
	    }
	    else if (this instanceof IndexedTriangleStripArrayRetained) {
		if (stripIndexCounts[i] < 3) {
		    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleStripArrayRetained1"));
		}
	    }
	    else if (this instanceof IndexedTriangleFanArrayRetained) {
		if (stripIndexCounts[i] < 3) {
		    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleFanArrayRetained1"));
		}
	    }
	}
	
        // Sum of all stripIndexCounts MUST be same as indexCount 
        if ((initialIndexIndex + total) > indexCount) 
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedGeometryStripArrayRetained0"));
	int newCoordMax =0;
	int newColorIndex=0;
	int newNormalIndex=0;
	int[] newTexCoordIndex = null;
        int[] newVertexAttrIndex = null;
	
	newCoordMax = computeMaxIndex(initialIndexIndex, total, indexCoord);
	doErrorCheck(newCoordMax);
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    if ((vertexFormat & GeometryArray.COLOR) != 0) {
		newColorIndex = computeMaxIndex(initialIndexIndex, total, indexColor);
		doColorCheck(newColorIndex);
	    }
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		newTexCoordIndex = new int[texCoordSetCount];
		for (i = 0; i < texCoordSetCount; i++) {
		   newTexCoordIndex[i] =  computeMaxIndex(initialIndexIndex,total,
								  indexTexCoord[i]);
		   doTexCoordCheck(newTexCoordIndex[i], i);
		}
	    }
	    if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		newVertexAttrIndex = new int[vertexAttrCount];
		for (i = 0; i < vertexAttrCount; i++) {
		   newVertexAttrIndex[i] = computeMaxIndex(initialIndexIndex,
                                                           total,
                                                           indexVertexAttr[i]);
		   doTexCoordCheck(newVertexAttrIndex[i], i);
		}
	    }
	    if ((vertexFormat & GeometryArray.NORMALS) != 0) {
		newNormalIndex = computeMaxIndex(initialIndexIndex, total, indexNormal);
		doNormalCheck(newNormalIndex);
	    }
	}

	geomLock.getLock();
	validIndexCount = total;
	this.stripIndexCounts = new int[num];
	for (i=0;i < num;i++)
	{
	    this.stripIndexCounts[i] = stripIndexCounts[i];
	}
	maxCoordIndex = newCoordMax;
	if ((vertexFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	    maxColorIndex = newColorIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = newTexCoordIndex[i];
		}
	    }
	    if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		for (i = 0; i < vertexAttrCount; i++) {
		    maxVertexAttrIndices[i] = newVertexAttrIndex[i];
		}
	    }
	    maxNormalIndex = newNormalIndex;
	}
	else {
	    maxColorIndex = maxCoordIndex;
	    maxNormalIndex = maxCoordIndex;
	    if ((vertexFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		for (i = 0; i < texCoordSetCount; i++) {
		    maxTexCoordIndices[i] = maxCoordIndex;
		}
	    }
	    if ((vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
		for (i = 0; i < vertexAttrCount; i++) {
		    maxVertexAttrIndices[i] = maxCoordIndex;
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

  GeometryArrayRetained cloneNonIndexedGeometry() {
      GeometryStripArrayRetained obj = null;
      int i;
      switch (this.geoType) {
      case GEO_TYPE_INDEXED_LINE_STRIP_SET:
          obj = new LineStripArrayRetained();
          break;
      case GEO_TYPE_INDEXED_TRI_FAN_SET:
          obj = new TriangleFanArrayRetained();
          break;
      case GEO_TYPE_INDEXED_TRI_STRIP_SET:
          obj = new TriangleStripArrayRetained();
          break;
      }
      obj.createGeometryArrayData(validIndexCount,
              (vertexFormat & ~(GeometryArray.BY_REFERENCE|GeometryArray.INTERLEAVED|GeometryArray.USE_NIO_BUFFER)),
              texCoordSetCount, texCoordSetMap,
              vertexAttrCount, vertexAttrSizes);
      obj.unIndexify(this); 
      obj.setStripVertexCounts(stripIndexCounts);

      return obj;
    }    


  /**
   * Get number of strips in the GeometryStripArray
   * @return numStrips number of strips
   */
  int getNumStrips(){
    return stripIndexCounts.length;
  }

  /**
   * Get a list of vertexCounts for each strip
   * @param stripIndexCounts an array that will receive vertexCounts
   */
  void getStripIndexCounts(int stripIndexCounts[]){
      for (int i=stripIndexCounts.length-1;i >= 0; i--) {
	  stripIndexCounts[i] = this.stripIndexCounts[i];
      }
  }

    void mergeGeometryArrays(ArrayList list) {
	int numMerge = list.size();
	int numCount = 0;
	int i, j;
	    
	for (i = 0; i < numMerge; i++) {
	    IndexedGeometryStripArrayRetained geo = (IndexedGeometryStripArrayRetained) list.get(i);
	    numCount += geo.stripIndexCounts.length;
	}
	    
	stripIndexCounts = new int[numCount];
	compileIndexLength = new int[numCount];
	compileStripICOffset = new int[numMerge];
	int curICOffset = 0;
	for (i = 0; i < numMerge; i++) {
	    IndexedGeometryStripArrayRetained geo = (IndexedGeometryStripArrayRetained) list.get(i);
	    compileStripICOffset[i] = curICOffset;
	    compileIndexLength[i] = geo.stripIndexCounts.length;
	    System.arraycopy(geo.stripIndexCounts, 0, stripIndexCounts,
			     curICOffset, geo.stripIndexCounts.length);
	    curICOffset += geo.stripIndexCounts.length;
	}
	super.mergeGeometryArrays(list);

    }
  int getNumStrips(int id){
    return compileIndexLength[id];
  }

  /**
   * Get a list of vertexCounts for each strip
   * @param stripIndexCounts an array that will receive vertexCounts
   */
  void getStripIndexCounts(int id, int stripIndexCounts[]){
      int count = compileIndexLength[id];
      int coffset = compileStripICOffset[id];
      for (int i=0;i < count; i++) {
	  stripIndexCounts[i] = this.stripIndexCounts[coffset+1];
      }
  }
    
}
