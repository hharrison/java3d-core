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

import java.io.*;
import java.util.*;
import javax.vecmath.*;

class Bridge {

    static void constructBridges(Triangulator triRef, int loopMin, int loopMax) {
	int i, j, numDist, numLeftMost;

	int[] i0 = new int[1];
	int[] ind0 = new int[1];
	int[] i1 = new int[1];
	int[] ind1 = new int[1];

	int[] iTmp = new int[1];
	int[] indTmp = new int[1];

	if(triRef.noHashingEdges != true)
	    System.out.println("Bridge:constructBridges noHashingEdges is false");
	if(loopMax <= loopMin)
	    System.out.println("Bridge:constructBridges loopMax<=loopMin");
	if(loopMin < 0)
	    System.out.println("Bridge:constructBridges loopMin<0");
	if(loopMax > triRef.numLoops)
	    System.out.println("Bridge:constructBridges loopMax>triRef.numLoops");

	numLeftMost = loopMax - loopMin - 1;

	if (numLeftMost > triRef.maxNumLeftMost) {
	    triRef.maxNumLeftMost = numLeftMost;
	    triRef.leftMost = new Left[numLeftMost];
	}

	// For each contour, find the left-most vertex. (we will use the fact
	// that the vertices appear in sorted order!)
	findLeftMostVertex(triRef, triRef.loops[loopMin], ind0, i0);
	j = 0;
	for (i = loopMin + 1;  i < loopMax;  ++i) {
	    findLeftMostVertex(triRef, triRef.loops[i], indTmp, iTmp);
	    triRef.leftMost[j] = new Left();
	    triRef.leftMost[j].ind = indTmp[0];
	    triRef.leftMost[j].index = iTmp[0];

	    ++j;
	}

	// sort the inner contours according to their left-most vertex
	sortLeft(triRef.leftMost, numLeftMost);

	// construct bridges. every bridge will eminate at the left-most point of
	// its corresponding inner loop.
	numDist = triRef.numPoints + 2 * triRef.numLoops;
	triRef.maxNumDist = numDist;
	triRef.distances = new Distance[numDist];
	for (int k = 0; k < triRef.maxNumDist; k++)
	    triRef.distances[k] = new Distance();


	for (j = 0; j < numLeftMost;  ++j) {
	    if (!findBridge(triRef, ind0[0], i0[0], triRef.leftMost[j].index, ind1, i1)) {
		//  if (verbose)
		// fprintf(stderr, "\n\n***** yikes! the loops intersect! *****\n");
	    }
	    if (i1[0] == triRef.leftMost[j].index)
		// the left-most node of the hole coincides with a node of the
		// boundary
		simpleBridge(triRef, ind1[0], triRef.leftMost[j].ind);
	    else
		// two bridge edges need to be inserted
		insertBridge(triRef, ind1[0], i1[0], triRef.leftMost[j].ind,
			     triRef.leftMost[j].index);
	}

    }


    /**
     * We try to find a vertex  i1  on the loop which contains  i  such that  i1
     * is close to  start,  and such that  i1, start  is a valid diagonal.
     */
    static boolean findBridge(Triangulator triRef, int ind, int i, int start,
			      int[] ind1, int[] i1) {
	int i0, i2, j, numDist = 0;
	int ind0, ind2;
	BBox bb;
	Distance old[] = null;
	boolean convex, coneOk;

	// sort the points according to their distance from  start.
	ind1[0] = ind;
	i1[0]   = i;
	if (i1[0] == start)  return true;
	if (numDist >= triRef.maxNumDist) {
	    // System.out.println("(1) Expanding distances array ...");
	    triRef.maxNumDist += triRef.INC_DIST_BK;
	    old = triRef.distances;
	    triRef.distances = new Distance[triRef.maxNumDist];
	    System.arraycopy(old, 0, triRef.distances, 0, old.length);
	    for (int k = old.length; k < triRef.maxNumDist; k++)
		triRef.distances[k] = new Distance();
	}

	triRef.distances[numDist].dist = Numerics.baseLength(triRef.points[start],
							     triRef.points[i1[0]]);
	triRef.distances[numDist].ind = ind1[0];
	++numDist;


	ind1[0] = triRef.fetchNextData(ind1[0]);
	i1[0] = triRef.fetchData(ind1[0]);
	while (ind1[0] != ind) {
	    if (i1[0] == start)  return true;
	    if (numDist >= triRef.maxNumDist) {
		// System.out.println("(2) Expanding distances array ...");
		triRef.maxNumDist += triRef.INC_DIST_BK;
		old = triRef.distances;
		triRef.distances = new Distance[triRef.maxNumDist];
		System.arraycopy(old, 0, triRef.distances, 0, old.length);
		for (int k = old.length; k < triRef.maxNumDist; k++)
		    triRef.distances[k] = new Distance();
	    }

	    triRef.distances[numDist].dist = Numerics.baseLength(triRef.points[start],
								 triRef.points[i1[0]]);
	    triRef.distances[numDist].ind = ind1[0];
	    ++numDist;
	    ind1[0] = triRef.fetchNextData(ind1[0]);
	    i1[0] = triRef.fetchData(ind1[0]);
	}

	// qsort(distances, num_dist, sizeof(distance), &d_comp);
	sortDistance(triRef.distances, numDist);

	// find a valid diagonal. note that no node with index  i1 > start  can
	// be feasible!
	for (j = 0;  j < numDist;  ++j) {
	    ind1[0] = triRef.distances[j].ind;
	    i1[0] = triRef.fetchData(ind1[0]);
	    if (i1[0] <= start) {
		ind0   = triRef.fetchPrevData(ind1[0]);
		i0 = triRef.fetchData(ind0);
		ind2   = triRef.fetchNextData(ind1[0]);
		i2 = triRef.fetchData(ind2);
		convex = triRef.getAngle(ind1[0]) > 0;

		coneOk = Numerics.isInCone(triRef, i0, i1[0], i2, start, convex);
		if (coneOk) {
		    bb = new BBox(triRef, i1[0], start);
		    if (!NoHash.noHashEdgeIntersectionExists(triRef, bb, -1, -1, ind1[0], -1))
			return true;
		}
	    }
	}

	// the left-most point of the hole does not lie within the outer
	// boundary!  what is the best bridge in this case??? I make a
	// brute-force decision...  perhaps this should be refined during a
	// revision of the code...
	for (j = 0;  j < numDist;  ++j) {
	    ind1[0] = triRef.distances[j].ind;
	    i1[0] = triRef.fetchData(ind1[0]);
	    ind0  = triRef.fetchPrevData(ind1[0]);
	    i0 = triRef.fetchData(ind0);
	    ind2  = triRef.fetchNextData(ind1[0]);
	    i2 = triRef.fetchData(ind2);
	    bb = new BBox(triRef, i1[0], start);
	    if (!NoHash.noHashEdgeIntersectionExists(triRef, bb, -1, -1, ind1[0], -1))
		return true;
	}

	// still no diagonal??? yikes! oh well, this polygon is messed up badly!
	ind1[0] = ind;
	i1[0]   = i;

	return false;
    }


    static void findLeftMostVertex(Triangulator triRef, int ind, int[] leftInd,
				   int[] leftI) {
	int ind1, i1;

	ind1 = ind;
	i1 = triRef.fetchData(ind1);
	leftInd[0] = ind1;
	leftI[0]   = i1;
	ind1 = triRef.fetchNextData(ind1);
	i1 = triRef.fetchData(ind1);
	while (ind1 != ind) {
	    if (i1 < leftI[0]) {
		leftInd[0] = ind1;
		leftI[0]  = i1;
	    }
	    else if (i1 == leftI[0]) {
		if (triRef.getAngle(ind1) < 0) {
		    leftInd[0] = ind1;
		    leftI[0]   = i1;
		}
	    }
	    ind1 = triRef.fetchNextData(ind1);
	    i1 = triRef.fetchData(ind1);
	}

    }

    static void simpleBridge(Triangulator triRef, int ind1, int ind2) {
	int prev, next;
	int i1, i2, prv, nxt;
	int angle;


	// change the links
	triRef.rotateLinks(ind1, ind2);

	// reset the angles
	i1 = triRef.fetchData(ind1);
	next = triRef.fetchNextData(ind1);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind1);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i1, nxt, ind1);
	triRef.setAngle(ind1, angle);

	i2 = triRef.fetchData(ind2);
	next = triRef.fetchNextData(ind2);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind2);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i2, nxt, ind2);
	triRef.setAngle(ind2, angle);

    }


    static void insertBridge(Triangulator triRef, int ind1, int i1,
			     int ind3, int i3) {
	int ind2, ind4, prev, next;
	int prv, nxt, angle;
	int vcntIndex;

	// duplicate nodes in order to form end points of the bridge edges
	ind2 = triRef.makeNode(i1);
	triRef.insertAfter(ind1, ind2);

	// Need to get the original data, before setting it.

	vcntIndex = triRef.list[ind1].getCommonIndex();

	triRef.list[ind2].setCommonIndex(vcntIndex);


	ind4 = triRef.makeNode(i3);
	triRef.insertAfter(ind3, ind4);

	vcntIndex = triRef.list[ind3].getCommonIndex();
	triRef.list[ind4].setCommonIndex(vcntIndex);

	// insert the bridge edges into the boundary loops
	triRef.splitSplice(ind1, ind2, ind3, ind4);

	// reset the angles
	next = triRef.fetchNextData(ind1);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind1);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i1, nxt, ind1);
	triRef.setAngle(ind1, angle);

	next = triRef.fetchNextData(ind2);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind2);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i1, nxt, ind2);
	triRef.setAngle(ind2, angle);

	next = triRef.fetchNextData(ind3);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind3);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i3, nxt, ind3);
	triRef.setAngle(ind3, angle);

	next = triRef.fetchNextData(ind4);
	nxt = triRef.fetchData(next);
	prev = triRef.fetchPrevData(ind4);
	prv = triRef.fetchData(prev);
	angle = Numerics.isConvexAngle(triRef, prv, i3, nxt, ind4);
	triRef.setAngle(ind4, angle);

    }


    static int l_comp(Left a, Left b) {
	if      (a.index < b.index)  return -1;
	else if (a.index > b.index)  return  1;
	else                           return 0;
    }

    static int d_comp(Distance a, Distance b) {
	if      (a.dist < b.dist)  return -1;
	else if (a.dist > b.dist)  return  1;
	else                         return 0;
    }


    static void sortLeft(Left[] lefts, int numPts) {
	int i,j;
	Left swap = new Left();

	for (i = 0; i < numPts; i++){
	    for (j = i + 1; j < numPts; j++){
		if (l_comp(lefts[i], lefts[j]) > 0){
		    swap.copy(lefts[i]);
		    lefts[i].copy(lefts[j]);
		    lefts[j].copy(swap);
		}
	    }
	}
    }


    static void sortDistance(Distance[] distances, int numPts) {
	int i,j;
	Distance swap = new Distance();

	for (i = 0; i < numPts; i++){
	    for (j = i + 1; j < numPts; j++){
		if (d_comp(distances[i], distances[j]) > 0){
		    swap.copy(distances[i]);
		    distances[i].copy(distances[j]);
		    distances[j].copy(swap);
		}
	    }
	}
    }

}
