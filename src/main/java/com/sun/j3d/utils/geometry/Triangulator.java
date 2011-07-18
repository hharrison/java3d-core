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

// ----------------------------------------------------------------------
//
// The reference to Fast Industrial Strength Triangulation (FIST) code
// in this release by Sun Microsystems is related to Sun's rewrite of
// an early version of FIST. FIST was originally created by Martin
// Held and Joseph Mitchell at Stony Brook University and is
// incorporated by Sun under an agreement with The Research Foundation
// of SUNY (RFSUNY). The current version of FIST is available for
// commercial use under a license agreement with RFSUNY on behalf of
// the authors and Stony Brook University.  Please contact the Office
// of Technology Licensing at Stony Brook, phone 631-632-9009, for
// licensing information.
//
// ----------------------------------------------------------------------

package com.sun.j3d.utils.geometry;

import javax.vecmath.*;
import java.util.*;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * Triangulator is a utility for turning arbitrary polygons into triangles
 * so they can be rendered by Java 3D.
 * Polygons can be concave, nonplanar, and can contain holes.
 * @see GeometryInfo
 */
public class Triangulator extends Object {

    GeometryInfo gInfo = null;

    int faces[] = null;
    int loops[] = null;
    int chains[] = null;
    Point2f points[] = null;
    Triangle triangles[] = null;
    ListNode list[] = null;

    Random randomGen = null;

    int numPoints = 0;
    int maxNumPoints = 0;
    int numList = 0;
    int maxNumList = 0;
    int numLoops = 0;
    int maxNumLoops = 0;
    int numTriangles = 0;
    int maxNumTriangles = 0;

    int numFaces = 0;
    int numTexSets = 0;
    //  int maxNumFaces = 0;

    int firstNode = 0;

    int numChains = 0;
    int maxNumChains = 0;

    // For Clean class.
    Point2f[] pUnsorted = null;
    int maxNumPUnsorted = 0;

    // For NoHash class.
    boolean noHashingEdges = false;
    boolean noHashingPnts = false;
    int loopMin, loopMax;
    PntNode vtxList[] = null;
    int numVtxList = 0;
    int numReflex = 0;
    int reflexVertices;

    // For Bridge class.
    Distance distances[] = null;
    int maxNumDist = 0;
    Left leftMost[] = null;
    int maxNumLeftMost = 0;

    // For Heap class.
    HeapNode heap[] = null;
    int numHeap = 0;
    int maxNumHeap = 0;
    int numZero = 0;

    // For Orientation class.
    int maxNumPolyArea = 0;
    double polyArea[] = null;

    int stripCounts[] = null;
    int vertexIndices[] = null;
    Point3f vertices[] = null;
    Object colors[] = null;
    Vector3f normals[] = null;

    boolean ccwLoop = true;

    boolean earsRandom = true;
    boolean earsSorted = true;

    int identCntr;  // Not sure what is this for. (Ask Martin)

    //  double epsilon = 1.0e-12;
    double epsilon = 1.0e-12;

    static final double ZERO = 1.0e-8;
    static final int EARS_SEQUENCE = 0;
    static final int EARS_RANDOM = 1;
    static final int EARS_SORTED = 2;


    static final int INC_LIST_BK = 100;
    static final int INC_LOOP_BK = 20;
    static final int INC_TRI_BK = 50;
    static final int INC_POINT_BK = 100;
    static final int INC_DIST_BK = 50;

    private static final int DEBUG = 0;

    /**
     * Creates a new instance of the Triangulator.
     * @deprecated This class is created automatically when needed in
     * GeometryInfo and never needs to be used directly.  Putting data
     * into a GeometryInfo with primitive POLYGON_ARRAY automatically
     * causes the triangulator to be created and used.
     */
    public Triangulator() {
	earsRandom = false;
	earsSorted = false;
    }

    /**
     * Creates a new instance of a Triangulator.
     * @deprecated This class is created automatically when needed in
     * GeometryInfo and never needs to be used directly.  Putting data
     * into a GeometryInfo with primitive POLYGON_ARRAY automatically
     * causes the triangulator to be created and used.
     */
    public Triangulator(int earOrder) {
	switch(earOrder) {
	case EARS_SEQUENCE:
	    earsRandom = false;
	    earsSorted = false;
	    break;
	case EARS_RANDOM:
	    randomGen = new Random();
	    earsRandom = true;
	    earsSorted = false;
	    break;
	case EARS_SORTED:
	    earsRandom = false;
	    earsSorted = true;
	    break;
	default:
	    earsRandom = false;
	    earsSorted = false;
	}

    }

    /**
     * This routine converts the GeometryInfo object from primitive type
     * POLYGON_ARRAY to primitive type TRIANGLE_ARRAY using polygon
     * decomposition techniques.
     * <p>
     * <pre>
     * Example of usage:
     *   Triangulator tr = new Triangulator();
     *   tr.triangulate(ginfo); // ginfo contains the geometry.
     *   shape.setGeometry(ginfo.getGeometryArray()); // shape is a Shape3D.
     *<p></pre>
     * @param gi Geometry to be triangulated
     **/
    public void triangulate(GeometryInfo gi) {
	int i, j, k;
	int sIndex = 0, index, currLoop, lastInd, ind;
	boolean proceed;
	boolean reset = false, troubles = false;

	boolean done[] = new boolean[1];
	boolean gotIt[] = new boolean[1];

	if (gi.getPrimitive() != GeometryInfo.POLYGON_ARRAY){
	    throw new IllegalArgumentException(J3dUtilsI18N.getString("Triangulator0"));
	}

	gi.indexify();

	vertices = gi.getCoordinates();
	if(vertices != null)
	    vertexIndices = gi.getCoordinateIndices();
	else
	    vertexIndices = null;

	colors = gi.getColors();
	normals = gi.getNormals();
	this.gInfo= gi;


	stripCounts = gi.getStripCounts();

	faces = gi.getContourCounts();
	if(faces == null) {
	    if(stripCounts == null)
		System.out.println("StripCounts is null! Don't know what to do.");

	    faces = new int[stripCounts.length];
	    for(i=0; i<stripCounts.length; i++)
		faces[i] = 1;
	}

	numFaces = faces.length;
	numTexSets = gInfo.getTexCoordSetCount();


	// For debugging ...
	/*
	  System.out.println("Faces (number " + faces.length + ") : ");
	  for(i=0; i<faces.length; i++) {
	  System.out.println(faces[i]  + ", ");
	  }

	  System.out.println("StripCounts (number " + stripCounts.length + ") : ");
	  for(i=0; i<stripCounts.length; i++) {
	  System.out.println(stripCounts[i]  + ", ");
	  }

	  System.out.println("Vertices (number " + vertices.length + ") : ");
	  for(i=0; i<vertices.length; i++) {
	  System.out.println(i + " x " + vertices[i].x  + " y " + vertices[i].y +
	  " z " + vertices[i].z);
	  }


	  System.out.println("VertexIndices (number " + vertexIndices.length + ") : ");

	  for(i=0; i<vertexIndices.length; i++) {
	  System.out.print(vertexIndices[i]  + ", ");
	  }
	*/

	maxNumLoops = 0;
	maxNumList = 0;
	maxNumPoints = 0;
	maxNumDist = 0;
	maxNumLeftMost = 0;
	maxNumPUnsorted = 0;

	// Compute the length of loops and list.
	for(i=0; i<faces.length; i++) {
	    maxNumLoops += faces[i];
	    for(j=0; j<faces[i]; j++, sIndex++) {
		maxNumList += (stripCounts[sIndex]+1);
	    }
	}

	// Add some incase of bridges.
	maxNumList += 20;

	loops = new int[maxNumLoops];
	list = new ListNode[maxNumList];
	// maxNumPoints = vertices.length;
	//points = new Point2f[maxNumPoints];


	// Construct data for use in triangulation.
	numVtxList = 0;
	numReflex = 0;

	numTriangles = 0;
	numChains = 0;
	numPoints = 0;
	numLoops = 0;
	numList = 0;
	sIndex = 0;
	index = 0;

	for(i=0; i<faces.length; i++) {
	    for(j=0; j<faces[i]; j++, sIndex++) {

		currLoop = makeLoopHeader();
		lastInd = loops[currLoop];

		for(k=0; k<stripCounts[sIndex]; k++) {
		    list[numList] = new ListNode(vertexIndices[index]);
		    ind = numList++;

		    insertAfter(lastInd, ind);
		    list[ind].setCommonIndex(index);

		    lastInd = ind;
		    index++;

		} // index k.

		deleteHook(currLoop);

	    } // index j.
	} // index i.


	// Done with constructing data. We can start to triangulate now.

	maxNumTriangles = maxNumList/2;
	triangles = new Triangle[maxNumTriangles];

	// set the numerical precision threshold
	setEpsilon(ZERO);

	// process the polygonal faces of the polyhedron. for every face, we
	// check whether it is a triangle or a quadrangle in order to weed out
	// simple cases which do not require the full triangulation algorithm

	/*
	  for(i=0; i<numLoops; i++)
	  System.out.println("loops[" + i + "] " + loops[i]);
	  printListData();
	*/

	int i1 = 0;
	int i2 = 0;
	for( j = 0;  j < numFaces;  ++j) {
	    ccwLoop = true;
	    done[0] = false;
	    i2 = i1 + faces[j];

	    if (faces[j] > 1) {
		proceed = true;
	    }
	    else if(Simple.simpleFace(this, loops[i1]))
		proceed = false;
	    else
		proceed = true;

	    if (proceed) {


		// Do some preprocessing here.

		// System.out.println("faces["+j+"] "+faces[j]);
		for(int lpIndex = 0; lpIndex<faces[j]; lpIndex++)
		    preProcessList(i1 + lpIndex);

		// project the polygonal face onto a plane
		Project.projectFace(this, i1, i2);

		// sort the points of this face in lexicographic order and  discard
		// duplicates

		// System.out.println("Before cleaning ...");
		// printListData();


		int removed = Clean.cleanPolyhedralFace(this, i1, i2);

		// For debugging.
		/*
		  System.out.println("After cleaning ...");
		  printListData();

		  System.out.println("\n***** " + removed +
		  " duplicate points removed for face " +
		  i1 + " *****\n");
		*/

		// determine the orientation of the polygon; the default
		// orientation is CCW for the outer polygon
		if (faces[j] == 1) {
		    // System.out.println("determineOrientation");
		    Orientation.determineOrientation(this, loops[i1]);
		}
		else {
		    // System.out.println("adjustOrientation");
		    Orientation.adjustOrientation(this, i1, i2);
		}

		// CE2 only
		if (faces[j] > 1) {
		    NoHash.prepareNoHashEdges(this, i1, i2);
		}
		else {
		    noHashingEdges = false;
		    noHashingPnts  = false;
		}


		// mark those vertices whose interior angle is convex
		for (i = i1;  i < i2;  ++i) {
		    EarClip.classifyAngles(this, loops[i]);
		}

		/*
		  System.out.println("After classifyAngles ...");
		  printListData();
		*/


		// link the holes with the outer boundary by means of "bridges"
		if (faces[j] > 1)  Bridge.constructBridges(this, i1, i2);

		// put all ears into a circular linked list
		resetPolyList(loops[i1]);
		NoHash.prepareNoHashPnts(this, i1);
		EarClip.classifyEars(this, loops[i1]);
		done[0]  = false;

		/*
		  System.out.println("Before clipEar (List)...");
		  printListData();
		  System.out.println("Before clipEar (vtxList)...");
		  printVtxList();

		  int counter = 0;
		*/

		// triangulate the polygon
		while (!done[0]) {
		    if (!EarClip.clipEar(this, done)) {
			/*
			  System.out.println(" (False case) clipEar (vtxList)...");
			  printListData();
			  printVtxList();
			*/

			if (reset) {
			    // For debugging.

			    // System.out.println("***** no further ear to clip! ***** \n");
			    // System.out.println("***** not a simple polygon, isn't it? *****\n");


			    ind = getNode();
			    resetPolyList(ind);

			    loops[i1] = ind;
			    if (Desperate.desperate(this, ind, i1, done)) {
				//  System.out.println("***** let's hope for the best *****\n");
				if (!Desperate.letsHope(this, ind)) {
				    /*
				      System.out.println("***** sorry, I can't do it! ***** \n");
				      System.out.println("***** ask a triangulation wizard, or ");
				      System.out.println("clean-up your polyhedron! ***** \n");
				    */
				    return;
				}
			    }
			    else {
				reset = false;
			    }
			}
			else {
			    // try again from scratch
			    troubles = true;
			    // System.out.println("\n***** re-classifying the ears! ***** \n");
			    ind = getNode();
			    resetPolyList(ind);

			    //	System.out.println("Before classifyEars(" + ind + ")");
			    //	printListData();

			    EarClip.classifyEars(this, ind);
			    reset = true;
			}
		    }
		    else {
			reset = false;
			/*
			  System.out.println(" (True case) clipEar (vtxList)...");

			  printVtxList();
			*/

		    }

		    if (done[0]) {
			// System.out.println("In done[0] is true");
			ind = getNextChain(gotIt);
			if (gotIt[0]) {
			    // at some point of the triangulation, we could not find
			    // any ear and the polygon was split into two parts. now
			    // we have to handle (one of) the remaining parts.
			    resetPolyList(ind);
			    loops[i1] = ind;
			    noHashingPnts = false;
			    NoHash.prepareNoHashPnts(this, i1);
			    EarClip.classifyEars(this, ind);
			    reset = false;
			    done[0]  = false;
			}
		    }
		}
	    }

	    i1 = i2;

	}

	/*
	  if (troubles)
	  System.out.println("\n\nTriangulation completed!\n");
	  else
	  System.out.println("\n\nTriangulation successfully completed!\n");
	*/
	// System.out.println("\n...writing the output data: ");

	// Output triangles here.
	writeTriangleToGeomInfo();

    }

    void printVtxList() {
	int i;
	System.out.println("numReflex " + numReflex + " reflexVertices " +
			   reflexVertices);
	for(i= 0; i<numVtxList; i++)
	    System.out.println(i + " pnt " + vtxList[i].pnt +
			       ", next " + vtxList[i].next );

    }

    void printListData() {
	for(int i=0; i<numList; i++)
	    System.out.println("list[" + i + "].index " + list[i].index +
			       ", prev " + list[i].prev +
			       ", next " + list[i].next +
			       ", convex " + list[i].convex +
			       ", vertexIndex " + list[i].vcntIndex);

    }

    void preProcessList(int i1) {
	int tInd, tInd1, tInd2;

	resetPolyList(loops[i1]);
	tInd = loops[i1];
	tInd1 = tInd;
	tInd2 = list[tInd1].next;
	while(tInd2 != tInd) {
	    if(list[tInd1].index == list[tInd2].index) {
		if(tInd2 == loops[i1])
		    loops[i1] = list[tInd2].next;
		deleteLinks(tInd2);
	    }
	    tInd1 = list[tInd1].next;
	    tInd2 = list[tInd1].next;
	}

    }

    void writeTriangleToGeomInfo() {
	int i, currIndex;

	// There are 2 approaches to take here : (1) Output all triangles as
	// a single face.(Easy) (2) Preserve the faces of the polyhedron and
	// sets of triangles per face. ( Seems to be the preferred approach, but
	// a check in GeometryInfo and the old GeomInfoConverter doesn't seems
	// to support this. Will check with Dan and Paul. Will take the easy way first.

	// For debugging ....
	if(DEBUG == 1) {
	    System.out.println("List (number " + numList + ") : ");
	    for(i=0; i<numList; i++) {
		System.out.println("index " + list[i].index + " prev " + list[i].prev +
				   " next " + list[i].next + " convex " + list[i].convex);
		System.out.println(" vertexIndex " + list[i].vcntIndex + " colorIndex " +
				   list[i].vcntIndex + " normalIndex " + list[i].vcntIndex +
				   " textureIndex " + list[i].vcntIndex);
	    }

	    System.out.println("Points (number " + numPoints + ") : ");

	    for(i=0; i<numPoints; i++) {
		System.out.println("x " + points[i].x + " y " + points[i].y);
	    }

	    System.out.println("Triangles (number " + numTriangles + ") : ");

	    for(i=0; i<numTriangles; i++) {
		System.out.println("v1 " + triangles[i].v1 + " v2 " + triangles[i].v2 +
				   " v3 " + triangles[i].v3);
	    }
	}

	gInfo.setPrimitive(GeometryInfo.TRIANGLE_ARRAY);
	gInfo.setContourCounts(null);
	gInfo.forgetOldPrim();
	gInfo.setStripCounts(null);

	currIndex = 0;
	int newVertexIndices[] = new int[numTriangles*3];
	int index;
	for(i=0; i<numTriangles; i++) {
	    index = list[triangles[i].v1].getCommonIndex();
	    newVertexIndices[currIndex++] = vertexIndices[index];
	    index = list[triangles[i].v2].getCommonIndex();
	    newVertexIndices[currIndex++] = vertexIndices[index];
	    index = list[triangles[i].v3].getCommonIndex();
	    newVertexIndices[currIndex++] = vertexIndices[index];
	}
	gInfo.setCoordinateIndices(newVertexIndices);

	/*
	  for(i=0;i<newVertexIndices.length;) {
	  System.out.println("v1 " + newVertexIndices[i++] +
	  ", v2 " + newVertexIndices[i++] +
	  ", v3 " + newVertexIndices[i++]);
	  }

	  System.out.println("Pysical point:");
	  for(i=0;i<newVertexIndices.length;) {
	  System.out.println("v1 " + vertices[newVertexIndices[i++]] +
	  ", v2 " + vertices[newVertexIndices[i++]] +
	  ", v3 " + vertices[newVertexIndices[i++]]);
	  }
	*/

	if(normals != null) {
	    int oldNormalIndices[] = gInfo.getNormalIndices();
	    int newNormalIndices[] = new int[numTriangles*3];
	    currIndex = 0;
	    for(i=0; i<numTriangles; i++) {
		index = list[triangles[i].v1].getCommonIndex();
		newNormalIndices[currIndex++] = oldNormalIndices[index];
		index = list[triangles[i].v2].getCommonIndex();
		newNormalIndices[currIndex++] = oldNormalIndices[index];
		index = list[triangles[i].v3].getCommonIndex();
		newNormalIndices[currIndex++] = oldNormalIndices[index];
	    }
	    gInfo.setNormalIndices(newNormalIndices);
	}

	if(colors != null) {
	    currIndex = 0;
	    int oldColorIndices[] = gInfo.getColorIndices();
	    int newColorIndices[] = new int[numTriangles*3];
	    for(i=0; i<numTriangles; i++) {
		index = list[triangles[i].v1].getCommonIndex();
		newColorIndices[currIndex++] = oldColorIndices[index];
		index = list[triangles[i].v2].getCommonIndex();
		newColorIndices[currIndex++] = oldColorIndices[index];
		index = list[triangles[i].v3].getCommonIndex();
		newColorIndices[currIndex++] = oldColorIndices[index];
	    }
	    gInfo.setColorIndices(newColorIndices);
	}

	for(int j = 0; j < numTexSets; j++) {
	    int newTextureIndices[] = new int[numTriangles*3];
	    int oldTextureIndices[] = gInfo.getTextureCoordinateIndices(j);
	    currIndex = 0;
	    for(i=0; i<numTriangles; i++) {
		index = list[triangles[i].v1].getCommonIndex();
		newTextureIndices[currIndex++] = oldTextureIndices[index];
		index = list[triangles[i].v2].getCommonIndex();
		newTextureIndices[currIndex++] = oldTextureIndices[index];
		index = list[triangles[i].v3].getCommonIndex();
		newTextureIndices[currIndex++] = oldTextureIndices[index];
	    }
	    gInfo.setTextureCoordinateIndices(j, newTextureIndices);
	}
    }


    void setEpsilon(double eps) {
	epsilon = eps;
    }

    // Methods of handling ListNode.

    boolean inPolyList(int ind) {
	return  ((ind >= 0)  &&  (ind < numList)  &&  (numList <= maxNumList));
    }



    void updateIndex(int ind, int index) {
	//  assert(InPolyList(ind));
	list[ind].index = index;
    }

    int getAngle(int ind) {
	return  list[ind].convex;
    }

    void setAngle(int ind, int convex) {
	list[ind].convex = convex;
    }


    void resetPolyList(int ind) {
	// assert(InPolyList(ind));
	firstNode = ind;
    }

    int getNode() {
	// assert(InPolyList(first_node));
	return firstNode;
    }

    boolean inLoopList(int loop) {
	return  ((loop >= 0)  &&  (loop < numLoops)  && (numLoops <= maxNumLoops));
    }


    void deleteHook(int currLoop) {
	int ind1, ind2;

	if(inLoopList(currLoop)==false)
	    System.out.println("Triangulator:deleteHook : Loop access out of range.");

	ind1 = loops[currLoop];
	ind2 = list[ind1].next;
	if((inPolyList(ind1))&&(inPolyList(ind2))) {

	    deleteLinks(ind1);
	    loops[currLoop] = ind2;

	}
	else
	    System.out.println("Triangulator:deleteHook : List access out of range.");
    }

    /**
     * Deletes node ind from list (with destroying its data fields)
     */
    void deleteLinks(int ind) {

	if((inPolyList(ind))&&(inPolyList(list[ind].prev))&&
	   (inPolyList(list[ind].next))) {

	    if (firstNode == ind)
		firstNode = list[ind].next;

	    list[list[ind].next].prev = list[ind].prev;
	    list[list[ind].prev].next = list[ind].next;
	    list[ind].prev = list[ind].next = ind;

	}
	else
	    System.out.println("Triangulator:deleteLinks : Access out of range.");

    }

    void rotateLinks(int ind1, int ind2) {
	int ind;
	int ind0, ind3;

	// assert(InPolyList(ind1));
	// assert(InPolyList(ind2));
	ind0 = list[ind1].next;
	ind3 = list[ind2].next;
	// assert(InPolyList(ind0));
	// assert(InPolyList(ind3));

	// Swap.
	ind = list[ind1].next;
	list[ind1].next = list[ind2].next;
	list[ind2].next = ind;

	list[ind0].prev = ind2;
	list[ind3].prev = ind1;

    }


    void storeChain(int ind) {
	if (numChains >= maxNumChains)  {
	    // System.out.println("Triangulator:storeChain  Expanding chain array ...");
	    maxNumChains += 20;
	    int old[] = chains;
	    chains = new int[maxNumChains];
	    if(old != null)
		System.arraycopy(old, 0, chains, 0, old.length);
	}
	chains[numChains] = ind;
	++numChains;

    }

    int getNextChain(boolean[] done) {
	if (numChains > 0) {
	    done[0] = true;
	    --numChains;
	    return  chains[numChains];
	}
	else {
	    done[0]      = false;
	    numChains = 0;
	    return 0;
	}
    }

    void splitSplice(int ind1, int ind2, int ind3, int ind4) {
	list[ind1].next = ind4;
	list[ind4].prev = ind1;
	list[ind2].prev = ind3;
	list[ind3].next = ind2;

    }

    /**
     * Allocates storage for a dummy list node; pointers are set to itself.
     * @return pointer to node
     */
    int makeHook() {
	int ind;

	ind = numList;
	if (numList >= maxNumList)  {
	    maxNumList += INC_LIST_BK;
	    // System.out.println("Triangulator: Expanding list array ....");
	    ListNode old[] = list;
	    list = new ListNode[maxNumList];
	    System.arraycopy(old, 0, list, 0, old.length);
	}

	list[numList] = new ListNode(-1);
	list[numList].prev  = ind;
	list[numList].next  = ind;
	list[numList].index = -1;
	++numList;

	return ind;
    }

    int makeLoopHeader() {
	int i;
	int ind;

	ind = makeHook();
	if(numLoops >= maxNumLoops) {
	    maxNumLoops += INC_LOOP_BK;
	    // System.out.println("Triangulator: Expanding loops array ....");
	    int old[] = loops;
	    loops = new int[maxNumLoops];
	    System.arraycopy(old, 0, loops, 0, old.length);
	}

	loops[numLoops] = ind;
	i = numLoops;
	++numLoops;

	return  i;
    }


    /**
      * Allocates storage for a new list node, and stores the index of the point
      * at this node. Pointers are set to -1.
      * @return pointer to node
      */
    int  makeNode(int index) {
	int ind;

	if (numList >= maxNumList)  {
	    maxNumList += INC_LIST_BK;
	    //System.out.println("Triangulator: Expanding list array ....");
	    ListNode old[] = list;
	    list = new ListNode[maxNumList];
	    System.arraycopy(old, 0, list, 0, old.length);
	}

	list[numList] = new ListNode(index);

	ind  = numList;
	list[numList].index  = index;
	list[numList].prev   = -1;
	list[numList].next   = -1;
	++numList;

	return ind;
    }


    /**
     * Inserts node ind2 after node ind1.
     */
    void insertAfter(int ind1, int ind2) {
	int ind3;

	if((inPolyList(ind1))&&(inPolyList(ind2))) {

	    list[ind2].next = list[ind1].next;
	    list[ind2].prev = ind1;
	    list[ind1].next = ind2;
	    ind3            = list[ind2].next;

	    if(inPolyList(ind3))
		list[ind3].prev = ind2;
	    else
		System.out.println("Triangulator:deleteHook : List access out of range.");

	    return;
	}
	else
	    System.out.println("Triangulator:deleteHook : List access out of range.");

    }

    /**
     * Returns pointer to the successor of ind1.
     */
    int fetchNextData(int ind1) {
	return list[ind1].next;
    }

    /**
     * obtains the data store at ind1
     */
    int fetchData(int ind1) {
	return list[ind1].index;
    }

    /**
     * returns pointer to the successor of ind1.
     */
    int fetchPrevData(int ind1) {
	return list[ind1].prev;
    }

    /**
     * swap the list pointers in order to change the orientation.
     */
    void swapLinks(int ind1) {
	int ind2, ind3;

	ind2 = list[ind1].next;
	list[ind1].next = list[ind1].prev;
	list[ind1].prev = ind2;
	ind3 = ind2;
	while (ind2 != ind1) {
	    ind3 = list[ind2].next;
	    list[ind2].next = list[ind2].prev;
	    list[ind2].prev = ind3;
	    ind2 = ind3;
	}
    }

    // Methods for handling Triangle.

    void storeTriangle(int i, int j, int k) {
	/*
	  if (ccwLoop)
	  triangles.add(new Triangle(i,j,k));
	  else
	  triangles.add(new Triangle(j,i,k));
	*/

	if(numTriangles >= maxNumTriangles) {
	    // System.out.println("Triangulator:storeTriangle Expanding triangle array..");
	    maxNumTriangles += INC_TRI_BK;
	    Triangle old[] = triangles;
	    triangles = new Triangle[maxNumTriangles];
	    if(old != null)
		System.arraycopy(old, 0, triangles, 0, old.length);
	}

	if (ccwLoop)
	    triangles[numTriangles] = new Triangle(i,j,k);
	else
	    triangles[numTriangles] = new Triangle(j,i,k);
	numTriangles++;

    }

    // Methods for handling Point.

    void initPnts(int number) {
	if (maxNumPoints < number) {
	    maxNumPoints = number;
	    points = new Point2f[maxNumPoints];
	}

	for(int i = 0; i<number; i++)
	    points[i] = new Point2f(0.0f, 0.0f);

	numPoints = 0;
    }

    boolean inPointsList(int index) {
	return  ((index >= 0)  &&  (index < numPoints)  &&
		 (numPoints <= maxNumPoints));
    }

    int storePoint(double x, double y) {
	int i;

	if (numPoints >= maxNumPoints)  {
	    // System.out.println("Triangulator:storePoint  Expanding points array ...");
	    maxNumPoints += INC_POINT_BK;
	    Point2f old[] = points;
	    points = new Point2f[maxNumPoints];
	    if(old != null)
		System.arraycopy(old, 0, points, 0, old.length);
	}

	points[numPoints] = new Point2f((float)x, (float)y);
	// points[numPoints].x = (float)x;
	// points[numPoints].y = (float)y;
	i                   = numPoints;
	++numPoints;

	return  i;
    }

}
