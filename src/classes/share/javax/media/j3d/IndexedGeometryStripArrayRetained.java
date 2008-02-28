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

	boolean isLive = source!=null && source.isLive();
        if(isLive){
            geomLock.getLock();
        }
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
	if(isLive) {
            geomLock.unLock();
	}
	// bbox is computed for the entries list.
	// so, send as false
	if (!inUpdater && isLive) {
	    sendDataChangedMessage(true);
	}

    }

    @Override
    GeometryArrayRetained cloneNonIndexedGeometry() {
        GeometryStripArrayRetained obj = null;

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
        obj.cloneSourceArray = this;
        obj.source = source;

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
