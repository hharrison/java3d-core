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

class Desperate {

    /**
     * the functions in this file try to ensure that we always end up with
     * something that (topologically) is a triangulation.
     *
     * the more desperate we get, the more aggressive means we choose for making
     * diagonals "valid".
     */
    static boolean desperate(Triangulator triRef, int ind, int i, boolean[] splitted) {
	int[] i1 = new int[1];
	int[] i2 = new int[1];
	int[] i3 = new int[1];
	int[] i4 = new int[1];
	int[] ind1 = new int[1];
	int[] ind2 = new int[1];
	int[] ind3 = new int[1];
	int[] ind4 = new int[1];

	splitted[0] = false;

	// check whether there exist consecutive vertices  i1, i2, i3, i4   such
	// that  i1, i2  and  i3, i4  intersect
	if (existsCrossOver(triRef, ind, ind1, i1, ind2, i2, ind3, i3, ind4, i4)) {
	    // insert two new diagonals around the cross-over without checking
	    // whether they are intersection-free
	    handleCrossOver(triRef, ind1[0], i1[0], ind2[0], i2[0], ind3[0], i3[0],
			    ind4[0], i4[0]);
	    return false;
	}

	NoHash.prepareNoHashEdges(triRef, i, i+1);

	// check whether there exists a valid diagonal that splits the polygon
	// into two parts
	if (existsSplit(triRef, ind, ind1, i1, ind2, i2)) {
	    // break up the polygon by inserting this diagonal (which can't be an
	    // ear -- otherwise, we would not have ended up in this part of the
	    // code). then, let's treat the two polygons separately. hopefully,
	    // this will help to handle self-overlapping polygons in the "correct"
	    // way.
	    handleSplit(triRef, ind1[0], i1[0], ind2[0], i2[0]);
	    splitted[0] = true;
	    return false;
	}

	return true;
    }


    static boolean existsCrossOver(Triangulator triRef, int ind, int[] ind1, int[] i1,
				   int[] ind2, int[] i2, int[] ind3, int[] i3,
				   int[] ind4, int[] i4) {
	BBox bb1, bb2;

	ind1[0] = ind;
	i1[0] = triRef.fetchData(ind1[0]);
	ind2[0] = triRef.fetchNextData(ind1[0]);
	i2[0] = triRef.fetchData(ind2[0]);
	ind3[0] = triRef.fetchNextData(ind2[0]);
	i3[0] = triRef.fetchData(ind3[0]);
	ind4[0] = triRef.fetchNextData(ind3[0]);
	i4[0] = triRef.fetchData(ind4[0]);

	do {
	    bb1 = new BBox(triRef, i1[0], i2[0]);
	    bb2 = new BBox(triRef, i3[0], i4[0]);
	    if (bb1.BBoxOverlap(bb2)) {
		if (Numerics.segIntersect(triRef, bb1.imin, bb1.imax, bb2.imin, bb2.imax, -1))
		    return true;
	    }
	    ind1[0] = ind2[0];
	    i1[0]   = i2[0];
	    ind2[0] = ind3[0];
	    i2[0]   = i3[0];
	    ind3[0] = ind4[0];
	    i3[0]   = i4[0];
	    ind4[0] = triRef.fetchNextData(ind3[0]);
	    i4[0] = triRef.fetchData(ind4[0]);

	} while (ind1[0] != ind);

	return false;
    }


    static void handleCrossOver(Triangulator triRef, int ind1, int i1, int ind2,
				int i2, int ind3, int i3, int ind4, int i4) {
	double ratio1, ratio4;
	boolean first;
	int angle1, angle4;

	// which pair of triangles shall I insert?? we can use either  i1, i2, i3
	// and  i1, i3, i4,  or we can use  i2, i3, i4  and  i1, i2, i4...
	angle1 = triRef.getAngle(ind1);
	angle4 = triRef.getAngle(ind4);
	if (angle1 < angle4)       first = true;
	else if (angle1 > angle4)  first = false;
	else if (triRef.earsSorted) {
	    ratio1 = Numerics.getRatio(triRef, i3, i4, i1);
	    ratio4 = Numerics.getRatio(triRef, i1, i2, i4);
	    if (ratio4 < ratio1)    first = false;
	    else                    first = true;
	}
	else {
	    first = true;
	}

	if (first) {
	    // first clip  i1, i2, i3,  then clip  i1, i3, i4
	    triRef.deleteLinks(ind2);
	    // StoreTriangle(GetOriginal(ind1), GetOriginal(ind2), GetOriginal(ind3));
	    triRef.storeTriangle(ind1, ind2, ind3);
	    triRef.setAngle(ind3, 1);
	    Heap.insertIntoHeap(triRef, 0.0, ind3, ind1, ind4);
	}
	else {
	    // first clip  i2, i3, i4,  then clip  i1, i2, i4
	    triRef.deleteLinks(ind3);
	    //StoreTriangle(GetOriginal(ind2), GetOriginal(ind3), GetOriginal(ind4));
	    triRef.storeTriangle(ind2, ind3, ind4);
	    triRef.setAngle(ind2, 1);
	    Heap.insertIntoHeap(triRef, 0.0, ind2, ind1, ind4);
	}
    }


    static boolean letsHope(Triangulator triRef, int ind) {
	int ind0, ind1, ind2;
	int i0, i1, i2;

	// let's clip the first convex corner. of course, we know that this is no
	// ear in an ideal world. but this polygon isn't ideal, either!
	ind1 = ind;
	i1 = triRef.fetchData(ind1);

	do  {
	    if (triRef.getAngle(ind1) > 0) {
		ind0 = triRef.fetchPrevData(ind1);
		i0 = triRef.fetchData(ind0);
		ind2 = triRef.fetchNextData(ind1);
		i2 = triRef.fetchData(ind2);
		Heap.insertIntoHeap(triRef, 0.0, ind1, ind0, ind2);
		return true;
	    }
	    ind1 = triRef.fetchNextData(ind1);
	    i1 = triRef.fetchData(ind1);
	}  while (ind1 != ind);

	// no convex corners? so, let's cheat! this code won't stop without some
	// triangulation...  ;-)    g-i-g-o? right! perhaps, this is what you
	// call a robust code?!
	triRef.setAngle(ind, 1);
	ind0 = triRef.fetchPrevData(ind);
	i0 = triRef.fetchData(ind0);
	ind2 = triRef.fetchNextData(ind);
	i2 = triRef.fetchData(ind2);
	Heap.insertIntoHeap(triRef, 0.0, ind, ind0, ind2);
	i1 = triRef.fetchData(ind);

	return true;

	// see, we never have to return "false"...
	/*
	  return false;
	*/
    }


    static boolean existsSplit(Triangulator triRef, int ind, int[] ind1, int[] i1,
			       int[] ind2, int[] i2) {
	int ind3, ind4, ind5;
	int i3, i4, i5;

	if (triRef.numPoints > triRef.maxNumDist) {
	    // System.out.println("Desperate: Expanding distances array ...");
	    triRef.maxNumDist = triRef.numPoints;
	    triRef.distances = new Distance[triRef.maxNumDist];
	    for (int k = 0; k < triRef.maxNumDist; k++)
		triRef.distances[k] = new Distance();
	}
	ind1[0] = ind;
	i1[0] = triRef.fetchData(ind1[0]);
	ind4 = triRef.fetchNextData(ind1[0]);
	i4 = triRef.fetchData(ind4);
	// assert(*ind1 != ind4);
	ind5 = triRef.fetchNextData(ind4);
	i5 = triRef.fetchData(ind5);
	// assert(*ind1 != *ind2);
	ind3  = triRef.fetchPrevData(ind1[0]);
	i3 = triRef.fetchData(ind3);
	// assert(*ind2 != ind3);
	if (foundSplit(triRef, ind5, i5, ind3, ind1[0], i1[0], i3, i4, ind2, i2))
	    return true;
	i3      = i1[0];
	ind1[0] = ind4;
	i1[0]   = i4;
	ind4    = ind5;
	i4      = i5;
	ind5    = triRef.fetchNextData(ind4);
	i5      = triRef.fetchData(ind5);

	while (ind5 != ind) {
	    if (foundSplit(triRef, ind5, i5, ind, ind1[0], i1[0], i3, i4, ind2, i2))
		return true;
	    i3    = i1[0];
	    ind1[0] = ind4;
	    i1[0]   = i4;
	    ind4  = ind5;
	    i4    = i5;
	    ind5  = triRef.fetchNextData(ind4);
	    i5    = triRef.fetchData(ind5);
	}

	return false;
    }


    /**
     * This function computes the winding number of a polygon with respect to a
     * point  p.  no care is taken to handle cases where  p  lies on the
     * boundary of the polygon. (this is no issue in our application, as we will
     * always compute the winding number with respect to the mid-point of a
     * valid diagonal.)
     */
    static int windingNumber(Triangulator triRef, int ind, Point2f p) {
	double angle;
	int ind2;
	int i1, i2, number;

	i1 = triRef.fetchData(ind);
	ind2 = triRef.fetchNextData(ind);
	i2 = triRef.fetchData(ind2);
	angle = Numerics.angle(triRef, p, triRef.points[i1], triRef.points[i2]);
	while (ind2 != ind) {
	    i1     = i2;
	    ind2   = triRef.fetchNextData(ind2);
	    i2 = triRef.fetchData(ind2);
	    angle += Numerics.angle(triRef, p, triRef.points[i1], triRef.points[i2]);
	}

	angle += Math.PI;
	number = (int)(angle / (Math.PI*2.0));

	return  number;
    }




    static boolean foundSplit(Triangulator triRef, int ind5, int i5, int ind, int ind1,
			      int i1, int i3, int i4, int[] ind2, int[] i2) {
	Point2f center;
	int numDist = 0;
	int j, i6, i7;
	int ind6, ind7;
	BBox bb;
	boolean convex, coneOk;

	// Sort the points according to their distance from  i1
	do {
	    // assert(numDist < triRef.maxNumDist);
	    triRef.distances[numDist].dist = Numerics.baseLength(triRef.points[i1],
								 triRef.points[i5]);
	    triRef.distances[numDist].ind = ind5;
	    ++numDist;
	    ind5 = triRef.fetchNextData(ind5);
	    i5 = triRef.fetchData(ind5);
	} while (ind5 != ind);

	Bridge.sortDistance(triRef.distances, numDist);

	// find a valid diagonal.
	for (j = 0;  j < numDist;  ++j) {
	    ind2[0] = triRef.distances[j].ind;
	    i2[0] = triRef.fetchData(ind2[0]);
	    if (i1 != i2[0]) {
		ind6  = triRef.fetchPrevData(ind2[0]);
		i6 = triRef.fetchData(ind6);
		ind7  = triRef.fetchNextData(ind2[0]);
		i7 = triRef.fetchData(ind7);

		convex = triRef.getAngle(ind2[0]) > 0;
		coneOk =  Numerics.isInCone(triRef, i6, i2[0], i7, i1, convex);
		if (coneOk) {
		    convex = triRef.getAngle(ind1) > 0;
		    coneOk = Numerics.isInCone(triRef, i3, i1, i4, i2[0], convex);
		    if (coneOk) {
			bb = new BBox(triRef, i1, i2[0]);
			if (!NoHash.noHashEdgeIntersectionExists(triRef, bb, -1, -1, ind1, -1)) {
			    // check whether this is a good diagonal; we do not want a
			    // diagonal that may create figure-8's!
			    center = new Point2f();
			    Basic.vectorAdd2D(triRef.points[i1], triRef.points[i2[0]], center);
			    Basic.multScalar2D(0.5, center);
			    if (windingNumber(triRef, ind, center) == 1)  return true;
			}
		    }
		}
	    }
	}

	return false;
    }


    static void handleSplit(Triangulator triRef, int ind1, int i1, int ind3, int i3) {
	int ind2, ind4, prev, next;
	int prv, nxt, angle;
	int vIndex, comIndex = -1;

	// duplicate nodes in order to form end points of the new diagonal
	ind2 = triRef.makeNode(i1);
	triRef.insertAfter(ind1, ind2);

	// Need to get the original data, before setting it.

	comIndex = triRef.list[ind1].getCommonIndex();

	triRef.list[ind2].setCommonIndex(comIndex);

	ind4 = triRef.makeNode(i3);
	triRef.insertAfter(ind3, ind4);

	comIndex = triRef.list[ind3].getCommonIndex();
	triRef.list[ind4].setCommonIndex(comIndex);

	// insert the diagonal into the boundary loop, thus splitting the loop
	// into two loops
	triRef.splitSplice(ind1, ind2, ind3, ind4);

	// store pointers to the two new loops
	triRef.storeChain(ind1);
	triRef.storeChain(ind3);

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
}

