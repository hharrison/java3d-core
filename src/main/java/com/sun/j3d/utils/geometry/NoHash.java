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

class NoHash {

    static final int NIL = -1;


    static void insertAfterVtx(Triangulator triRef, int iVtx) {
	int size;

	if (triRef.vtxList == null) {
	    size = Math.max(triRef.numVtxList+1, 100);
	    triRef.vtxList = new PntNode[size];
	} else if (triRef.numVtxList >= triRef.vtxList.length)  {
	    size = Math.max(triRef.numVtxList+1,
			    triRef.vtxList.length + 100);
	    PntNode old[] = triRef.vtxList;
	    triRef.vtxList = new PntNode[size];
	    System.arraycopy(old, 0, triRef.vtxList, 0, old.length);
	}

	triRef.vtxList[triRef.numVtxList] = new PntNode();
	triRef.vtxList[triRef.numVtxList].pnt = iVtx;
	triRef.vtxList[triRef.numVtxList].next = triRef.reflexVertices;
	triRef.reflexVertices = triRef.numVtxList;
	++triRef.numVtxList;
	++triRef.numReflex;
    }

    static void deleteFromList(Triangulator triRef, int i) {
	int indPnt, indPnt1;
	int indVtx;

	if(triRef.numReflex == 0) {
	    // System.out.println("NoHash:deleteFromList. numReflex is 0.");
	    return;

	}
	indPnt = triRef.reflexVertices;
	if(inVtxList(triRef, indPnt)==false)
	    System.out.println("NoHash:deleteFromList. Problem :Not is InVtxList ..." +
			       indPnt);

	indVtx = triRef.vtxList[indPnt].pnt;

	if (indVtx == i) {
	    triRef.reflexVertices = triRef.vtxList[indPnt].next;
	    --triRef.numReflex;
	}
	else {
	    indPnt1 = triRef.vtxList[indPnt].next;
	    while (indPnt1 != NIL) {
		if(inVtxList(triRef, indPnt1)==false)
		    System.out.println("NoHash:deleteFromList. Problem :Not is InVtxList ..."+
				       indPnt1);

		indVtx = triRef.vtxList[indPnt1].pnt;
		if (indVtx == i) {
		    triRef.vtxList[indPnt].next = triRef.vtxList[indPnt1].next;
		    indPnt1 = NIL;
		    --triRef.numReflex;
		}
		else {
		    indPnt = indPnt1;
		    indPnt1 = triRef.vtxList[indPnt].next;
		}
	    }
	}
    }


    static boolean inVtxList(Triangulator triRef, int vtx) {
	return ((0 <= vtx)  &&  (vtx < triRef.numVtxList));
    }


    static void freeNoHash(Triangulator triRef) {

	triRef.noHashingEdges  = false;
	triRef.noHashingPnts   = false;

	triRef.numVtxList      = 0;
    }



    static void prepareNoHashEdges(Triangulator triRef,
				   int currLoopMin, int currLoopMax) {
	triRef.loopMin = currLoopMin;
	triRef.loopMax = currLoopMax;

	triRef.noHashingEdges = true;

	return;
    }


    static void prepareNoHashPnts(Triangulator triRef, int currLoopMin) {
	int ind, ind1;
	int i1;

	triRef.numVtxList    = 0;
	triRef.reflexVertices = NIL;

	// insert the reflex vertices into a list
	ind = triRef.loops[currLoopMin];
	ind1 = ind;
	triRef.numReflex = 0;
	i1 = triRef.fetchData(ind1);

	do {
	    if (triRef.getAngle(ind1) < 0)
		insertAfterVtx(triRef, ind1);

	    ind1 = triRef.fetchNextData(ind1);
	    i1 = triRef.fetchData(ind1);
	} while (ind1 != ind);

	triRef.noHashingPnts = true;

    }


    static boolean noHashIntersectionExists(Triangulator triRef, int i1, int ind1,
					    int i2, int i3, BBox bb) {
	int indVtx, ind5;
	int indPnt;
	int i4, i5;
	int type[] = new int[1];
	boolean flag;
	double y;

	if(triRef.noHashingPnts==false)
	    System.out.println("NoHash:noHashIntersectionExists noHashingPnts is false");

	// assert(InPointsList(i1));
	// assert(InPointsList(i2));
	// assert(InPointsList(i3));

	if (triRef.numReflex <= 0)  return false;

	// first, let's extend the BBox of the line segment  i2, i3  to  a BBox
	// of the entire triangle.
	if (i1 < bb.imin)       bb.imin = i1;
	else if (i1 > bb.imax)  bb.imax = i1;
	y = triRef.points[i1].y;
	if (y < bb.ymin)        bb.ymin = y;
	else if (y > bb.ymax)   bb.ymax = y;

	// check whether the triangle  i1, i2, i3  contains any reflex vertex; we
	// assume that  i2, i3  is the new diagonal, and that the triangle is
	// oriented CCW.
	indPnt = triRef.reflexVertices;
	flag    = false;
	do {
	    // assert(InVtxList(ind_pnt));
	    indVtx = triRef.vtxList[indPnt].pnt;
	    // assert(InPolyList(ind_vtx));
	    i4 = triRef.fetchData(indVtx);


	    if (bb.pntInBBox(triRef, i4)) {
		// only if the reflex vertex lies inside the BBox of the triangle.
		ind5 = triRef.fetchNextData(indVtx);
		i5 = triRef.fetchData(ind5);
		if ((indVtx != ind1)  &&  (indVtx != ind5)) {
		    // only if this node isn't  i1,  and if it still belongs to the
		    // polygon
		    if (i4 == i1) {
			if (Degenerate.handleDegeneracies(triRef, i1, ind1, i2, i3, i4, indVtx))
			    return  true;
		    }
		    else if ((i4 != i2)  &&  (i4 != i3)) {
			flag = Numerics.vtxInTriangle(triRef, i1, i2, i3, i4, type);
			if (flag)   return  true;
		    }
		}
	    }
	    indPnt = triRef.vtxList[indPnt].next;

	} while (indPnt != NIL);

	return false;
    }




    static void deleteReflexVertex(Triangulator triRef, int ind) {
	// assert(InPolyList(ind));
	deleteFromList(triRef, ind);
    }




    static boolean noHashEdgeIntersectionExists(Triangulator triRef, BBox bb, int i1,
						int i2, int ind5, int i5) {
	int ind, ind2;
	int i, i3, i4;
	BBox bb1;

	if(triRef.noHashingEdges==false)
	    System.out.println("NoHash:noHashEdgeIntersectionExists noHashingEdges is false");

	triRef.identCntr = 0;

	// check the boundary segments.
	for (i = triRef.loopMin;  i < triRef.loopMax;  ++i) {
	    ind  = triRef.loops[i];
	    ind2 = ind;
	    i3 = triRef.fetchData(ind2);

	    do {
		ind2 = triRef.fetchNextData(ind2);
		i4 = triRef.fetchData(ind2);
		// check this segment. we first compute its bounding box.
		bb1 = new BBox(triRef, i3, i4);
		if (bb.BBoxOverlap(bb1)) {
		    if (Numerics.segIntersect(triRef, bb.imin, bb.imax, bb1.imin, bb1.imax, i5))
			return  true;
		}
		i3 = i4;
	    } while (ind2 != ind);
	}

	// oops! this segment shares one endpoint with at least four other
	// boundary segments! oh well, yet another degenerate situation...
	if (triRef.identCntr >= 4) {
	    if (BottleNeck.checkBottleNeck(triRef, i5, i1, i2, ind5))
		return true;
	    else
		return false;
	}

	return false;
    }

}
