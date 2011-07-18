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

class EarClip {

    /**
     * Classifies all the internal angles of the loop referenced by  ind.
     * the following classification is used:
     *            0 ... if angle is 180 degrees
     *            1 ... if angle between 0 and 180 degrees
     *            2 ... if angle is 0 degrees
     *           -1 ... if angle between 180 and 360 degrees
     *           -2 ... if angle is 360 degrees
     */
    static void classifyAngles(Triangulator triRef, int ind) {
	int ind0, ind1, ind2;
	int i0, i1, i2;
	int angle;

	ind1 = ind;
	i1 = triRef.fetchData(ind1);
	ind0 = triRef.fetchPrevData(ind1);
	i0 = triRef.fetchData(ind0);

	do {
	    ind2 = triRef.fetchNextData(ind1);
	    i2 = triRef.fetchData(ind2);
	    angle = Numerics.isConvexAngle(triRef, i0, i1, i2, ind1);
	    triRef.setAngle(ind1, angle);
	    i0   = i1;
	    i1   = i2;
	    ind1 = ind2;
	} while (ind1 != ind);

    }


    static void classifyEars(Triangulator triRef, int ind) {
	int ind1;
	int i1;
	int[] ind0, ind2;
	double[] ratio;

	ind0 = new int[1];
	ind2 = new int[1];
	ratio = new double[1];

	Heap.initHeap(triRef);

	ind1 = ind;
	i1 = triRef.fetchData(ind1);

	do {
	    if ((triRef.getAngle(ind1) > 0)  &&
		isEar(triRef, ind1, ind0, ind2, ratio))   {

		Heap.dumpOnHeap(triRef, ratio[0], ind1, ind0[0], ind2[0]);
	    }
	    ind1 = triRef.fetchNextData(ind1);
	    i1 = triRef.fetchData(ind1);
	} while (ind1 != ind);

	// Not using sorted_ear so don't have to do MakeHeap();
	// MakeHeap();

	// Heap.printHeapData(triRef);

    }


    /**                                                                         
     * This function checks whether a diagonal is valid, that is, whether it is
     * locally within the polygon, and whether it does not intersect any other
     * segment of the polygon. also, some degenerate cases get a special
     * handling.
     */
    static boolean isEar(Triangulator triRef, int ind2, int[] ind1, int[] ind3,
			 double[] ratio) {
	int i0, i1, i2, i3, i4;
	int ind0, ind4;
	BBox bb;
	boolean convex, coneOk;

	i2 = triRef.fetchData(ind2);
	ind3[0] = triRef.fetchNextData(ind2);
	i3 = triRef.fetchData(ind3[0]);
	ind4 = triRef.fetchNextData(ind3[0]);
	i4 = triRef.fetchData(ind4);
	ind1[0] = triRef.fetchPrevData(ind2);
	i1 = triRef.fetchData(ind1[0]);
	ind0 = triRef.fetchPrevData(ind1[0]);
	i0 = triRef.fetchData(ind0);

	/*
	  System.out.println("isEar : i0 " + i0 + " i1 " + i1 + " i2 " + i2 +
	  " i3 " + i3 + " i4 " + i4);
	*/

	if ((i1 == i3)  ||  (i1 == i2)  ||  (i2 == i3)  ||  (triRef.getAngle(ind2) == 2)) {
	    // oops, this is not a simple polygon!
	    ratio[0] = 0.0;
	    return  true;
	}

	if (i0 == i3) {
	    // again, this is not a simple polygon!
	    if ((triRef.getAngle(ind0) < 0)  ||  (triRef.getAngle(ind3[0]) < 0)) {
		ratio[0] = 0.0;
		return  true;
	    }
	    else
		return  false;
	}

	if (i1 == i4) {
	    // again, this is not a simple polygon!
	    if ((triRef.getAngle(ind1[0]) < 0)  ||  (triRef.getAngle(ind4) < 0)) {
		ratio[0] = 0.0;
		return  true;
	    }
	    else
		return  false;
	}

	// check whether the new diagonal  i1, i3  locally is within the polygon
	convex = triRef.getAngle(ind1[0]) > 0;
	coneOk = Numerics.isInCone(triRef, i0, i1, i2, i3, convex);
	// System.out.println("isEar :(1) convex " + convex + " coneOk " + coneOk );

	if (!coneOk)  return false;
	convex = triRef.getAngle(ind3[0]) > 0;
	coneOk = Numerics.isInCone(triRef, i2, i3, i4, i1, convex);
	// System.out.println("isEar :(2) convex " + convex + " coneOk " + coneOk );

	if (coneOk) {
	    // check whether this diagonal is a valid diagonal. this translates to
	    // checking either condition CE1 or CE2 (see my paper). If CE1 is to
	    // to be checked, then we use a BV-tree or a grid. Otherwise, we use
	    // "buckets" (i.e., a grid) or no hashing at all.
	    bb = new BBox(triRef, i1, i3);
	    // use CE2 + no_hashing
	    if(!NoHash.noHashIntersectionExists(triRef, i2, ind2, i3, i1, bb)) {
		if (triRef.earsSorted)  {
		    // determine the quality of the triangle
		    ratio[0] = Numerics.getRatio(triRef, i1, i3, i2);
		}
		else {
		    ratio[0] = 1.0;
		}
		return true;
	    }
	}

	// System.out.println("isEar : false");
	return  false;
    }



    /**
     * This is the main function that drives the ear-clipping. it obtains an ear
     * from set of ears maintained in a priority queue, clips this ear, and
     * updates all data structures appropriately. (ears are arranged in the
     * priority queue (i.e., heap) according to a quality criterion that tries
     * to avoid skinny triangles.)
     */
    static boolean clipEar(Triangulator triRef, boolean[] done) {

	int ind0, ind1, ind3, ind4;

	int i0, i1, i2, i3, i4;
	int angle1, angle3;

	double ratio[] = new double[1];
	int index0[] = new int[1];
	int index1[] = new int[1];
	int index2[] = new int[1];
	int index3[] = new int[1];
	int index4[] = new int[1];
	int ind2[] = new int[1];

	int testCnt = 0;

	// Heap.printHeapData(triRef);

	do {

	    //	System.out.println("In clipEarloop " + testCnt++);

	    if (!Heap.deleteFromHeap(triRef, ind2, index1, index3))
		// no ear exists?!
		return false;

	    // get the successors and predecessors in the list of nodes and check
	    // whether the ear still is part of the boundary
	    ind1 = triRef.fetchPrevData(ind2[0]);
	    i1 = triRef.fetchData(ind1);
	    ind3 = triRef.fetchNextData(ind2[0]);
	    i3 = triRef.fetchData(ind3);

	} while ((index1[0] != ind1)  ||  (index3[0] != ind3));

	//System.out.println("Out of clipEarloop ");

	i2 = triRef.fetchData(ind2[0]);

	// delete the clipped ear from the list of nodes, and update the bv-tree
	triRef.deleteLinks(ind2[0]);

	// store the ear in a list of ears which have already been clipped
	// StoreTriangle(GetOriginal(ind1), GetOriginal(ind2), GetOriginal(ind3));
	triRef.storeTriangle(ind1, ind2[0], ind3);

	/*                                                                        */
	/* update the angle classification at  ind1  and  ind3                    */
	/*                                                                        */
	ind0 = triRef.fetchPrevData(ind1);
	i0 = triRef.fetchData(ind0);
	if (ind0 == ind3) {
	    // nothing left
	    done[0] = true;
	    return  true;
	}
	angle1 = Numerics.isConvexAngle(triRef, i0, i1, i3, ind1);

	ind4 = triRef.fetchNextData(ind3);
	i4 = triRef.fetchData(ind4);

	angle3 = Numerics.isConvexAngle(triRef, i1, i3, i4, ind3);

	if (i1 != i3) {
	    if ((angle1 >= 0)  &&  (triRef.getAngle(ind1) < 0))
		NoHash.deleteReflexVertex(triRef, ind1);
	    if ((angle3 >= 0)  &&  (triRef.getAngle(ind3) < 0))
		NoHash.deleteReflexVertex(triRef, ind3);
	}
	else {
	    if ((angle1 >= 0)  &&  (triRef.getAngle(ind1) < 0))
		NoHash.deleteReflexVertex(triRef, ind1);
	    else if ((angle3 >= 0)  &&  (triRef.getAngle(ind3) < 0))
		NoHash.deleteReflexVertex(triRef, ind3);

	}

	triRef.setAngle(ind1, angle1);
	triRef.setAngle(ind3, angle3);

	// check whether either of  ind1  and  ind3  is an ear. (the "ratio" is
	// the length of the triangle's longest side divided by the length of the
	// height normal onto this side; it is used as a quality criterion.)
	if (angle1 > 0) {
	    if (isEar(triRef, ind1, index0, index2, ratio)) {
		// insert the new ear into the priority queue of ears
		Heap.insertIntoHeap(triRef, ratio[0], ind1, index0[0], index2[0]);
	    }
	}

	if (angle3 > 0) {
	    if(isEar(triRef, ind3, index2, index4, ratio)) {
		Heap.insertIntoHeap(triRef, ratio[0], ind3, index2[0], index4[0]);
	    }
	}

	// check whether the triangulation is finished.
	ind0 = triRef.fetchPrevData(ind1);
	i0 = triRef.fetchData(ind0);
	ind4 = triRef.fetchNextData(ind3);
	i4 = triRef.fetchData(ind4);
	if (ind0 == ind4) {
	    // only one triangle left -- clip it!
	    triRef.storeTriangle(ind1, ind3, ind4);
	    done[0] = true;
	}
	else {
	    done[0] = false;
	}

	return true;
    }

}





