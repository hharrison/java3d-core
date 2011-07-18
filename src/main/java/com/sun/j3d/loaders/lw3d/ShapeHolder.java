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

package com.sun.j3d.loaders.lw3d;

import java.util.Vector;
import javax.vecmath.Vector3f;


/**
 * This class holds all of the vertex/facet/normal/surface-reference
 * data for a particular object. It has utilities to calculate facet normals,
 * but this is no longer in use since using the new GeomInfo utilities.
 */

class ShapeHolder extends ParserObject {

    
    Vector facetSizesList;
    Vector facetIndicesList;
    int facetIndicesArray[];
    int currentNumIndices = 0;
    int numSurf;
    int numVerts;
    int facetIndices[];
    int facetSizes[];
    int normalIndices[];
    float normalCoords[];
    float coordsArray[];
    
    ShapeHolder() {
    }

    ShapeHolder(int debugVals) {
	super(debugVals);
    }


    /**
     * Print out (to stdout) the geometry data (coords, indices, 
     * and facet sizes).  This is a debugging utility.
     */
    void printGeometryData(LwoSurface surface) {
	int i, j;
	int indicesIndex = 0;
	System.out.println("\nPolygon Data:");
	System.out.println("  Surface color = " + surface.color);
	System.out.println("  Surface diffuse = " + surface.diffuseColor);
	for (i = 0; i < facetSizes.length; ++i) {
	    int polySize = facetSizes[i];
	    System.out.println("Facet of size " + polySize);
	    for (j = 0; j < polySize; ++j) {
		int coordIndex = 3 * facetIndices[indicesIndex++];
		System.out.println("x, y, z = " + 
				   coordsArray[coordIndex] + ", " +
				   coordsArray[coordIndex+1] + ", " +
				   coordsArray[coordIndex+2]);
	    }
	}
    }

    /**
     * Constructs geometry arrays given a winding rule (it turns out that
     * lw3d winding is opposite of j3d winding, so this is always set to 
     * true in J3dLwoParser)
     */
    void createArrays(boolean reverseWinding) {
	debugOutputLn(TRACE, "createArrays()");
	//	debugOutputLn(VALUES, "facetIndices, faceSizesList = " + 
	//		      facetIndicesList +  facetSizesList);
	//debugOutputLn(VALUES, "ind  and sizes size " +
	//	      facetIndicesList.size() + ", " + 
	//	      facetSizesList.size());
	//facetIndices = 
	//	    new int[facetIndicesList.size()];
	facetIndices = new int[currentNumIndices];
	if (reverseWinding) {
	    int facetBeginIndex = 0;
	    for (int facetIndex = 0;
		 facetIndex < facetSizesList.size();
		 ++facetIndex) {
		int currFaceSize =
		    ((Integer)facetSizesList.elementAt(facetIndex)).intValue();
		int tempFace[] = new int[currFaceSize];
		for (int j = 0; j < currFaceSize; ++j) {
		    facetIndices[facetBeginIndex  + j] =
			facetIndicesArray[facetBeginIndex +
					 currFaceSize - j - 1];
		}
		facetBeginIndex += currFaceSize;
	    }

	}
	else {
	    for (int i = 0; i < facetIndices.length; ++i) {
		facetIndices[i] = facetIndicesArray[i];
	    }
	}

	debugOutputLn(LINE_TRACE, "facetIndices.len and coordsArray.len = " +
		      facetIndices.length + ", " + coordsArray.length);
	if (((Integer)facetSizesList.elementAt(0)).intValue() < 3) {
	    // if we're dealing with point/line primitives, then let's abandon
	    // the indexed route and simply construct a new coordsArray
	    // that holds the direct values we need for a GeometryArray
	    // object
	    debugOutputLn(LINE_TRACE, "Using direct geometry because " +
			  "facetIndices is of size " +
			  facetIndices.length +
			  " and coordsArray is of length "+
			  coordsArray.length);
	    float newCoordsArray[] = new float[facetIndices.length * 3];
	    int newCoordsIndex = 0;
	    for (int i = 0; i < facetIndices.length; ++i) {
		newCoordsArray[newCoordsIndex++] =
		    coordsArray[facetIndices[i]*3];
		newCoordsArray[newCoordsIndex++] =
		    coordsArray[facetIndices[i]*3+1];
		newCoordsArray[newCoordsIndex++] =
		    coordsArray[facetIndices[i]*3+2];
	    }
	    coordsArray = newCoordsArray;
	    facetIndices = null;
	}

	facetSizes = 
	    new int[facetSizesList.size()];
	for (int i = 0; i < facetSizes.length; ++i) {
	    facetSizes[i] = 
		((Integer)facetSizesList.elementAt(i)).intValue();
	}
	
	facetSizesList = null;  // Force garbage collection on Vectors
	facetIndicesList = null;
	facetIndicesArray = null;
    }

    /**
     * Force gc on all array objects
     */
    void nullify() {
	facetSizesList = null;  // Force garbage collection on everything
	facetIndicesList = null;
	facetIndicesArray = null;
	facetSizes = null;
	facetIndices = null;
	normalCoords = null;
	normalIndices = null;
    }

    /**
     * This method calculates facet normals for the geometry.  It is no
     * longer used, as we're now using the GeometryInfo utility to calculate
     * smooth normals
     */ 
    void calcNormals() {
	debugOutputLn(TRACE, "calcNormals()");
	debugOutputLn(LINE_TRACE, "coordsLength, facetsizes.len = " + 
        coordsArray.length + ", " + facetSizes.length);
	if (facetSizes[0] > 2) {
	    // points and lines don't need normals, polys do
        if (facetIndices != null) {
	        normalIndices = new int[facetIndices.length];
	        normalCoords = new float[facetIndices.length * 3];
        }
        else {
            normalCoords = new float[coordsArray.length];
        }
        debugOutputLn(LINE_TRACE, "normalCoords, incides len = " +
            normalCoords.length + ", " + 
            ((facetIndices == null) ? 0 : normalIndices.length));
	    int facetIndex = 0;
	    int tempIndex = -1;
	    for (int i = 0; i < facetSizes.length; i += 1) {
		Vector3f norm;
		int currFacetSize = facetSizes[i];
		//debugOutputLn(LINE_TRACE, "    i, facetIndex, currSize = " +
		//		  i + ", " + facetIndex + ", " + currFacetSize);
		if (currFacetSize < 3) {
            // This shouldn't occur
		    norm = new Vector3f(0f, 0f, 1f);
		}
		else {
            Vector3f v1, v2;
            int index1, index2, index3;
            if (facetIndices != null) {
		        index1 = facetIndices[facetIndex];
		        index2 = facetIndices[facetIndex+1];
		        index3 = facetIndices[facetIndex+2];
				//debugOutputLn(VALUES, "    index123 = " +
				//		  index1 + ", " + index2 + ", " + index3);
            }
            else {
                index1 = facetIndex;
                index2 = facetIndex+1;
                index3 = facetIndex+2;
            }
		    v1 = new 
			Vector3f(coordsArray[index2*3] - coordsArray[index1*3],
				 coordsArray[index2*3+1] - coordsArray[index1*3+1],
				 coordsArray[index2*3+2] - coordsArray[index1*3+2]);
		    v2 = new 
			Vector3f(coordsArray[index3*3] - coordsArray[index1*3],
				 coordsArray[index3*3+1] - coordsArray[index1*3+1],
				 coordsArray[index3*3+2] - coordsArray[index1*3+2]);
				//debugOutputLn(VALUES, "v1, v2 = " + v1 + v2);
		    norm = new Vector3f();
		    norm.cross(v1, v2);
		    norm.normalize(norm);
		}
        
		for (int j = 0; j < currFacetSize; ++j) {
		    int normIndex = facetIndex + j;
            normalCoords[normIndex*3] = norm.x;
		    normalCoords[normIndex*3+1] = norm.y;
		    normalCoords[normIndex*3+2] = norm.z;
            if (facetIndices != null)
		        normalIndices[normIndex] = normIndex;
		}
        facetIndex += currFacetSize;
	    }
	}
	debugOutputLn(TRACE, "done with calcNormals()");
    }

}
