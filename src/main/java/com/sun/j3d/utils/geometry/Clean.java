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

class Clean {

    static void initPUnsorted(Triangulator triRef, int number) {
	if (number > triRef.maxNumPUnsorted) {
	    triRef.maxNumPUnsorted = number;
	    triRef.pUnsorted = new Point2f[triRef.maxNumPUnsorted];
	    for(int i = 0; i<triRef.maxNumPUnsorted; i++)
		triRef.pUnsorted[i] = new Point2f();
	}
    }


    static int cleanPolyhedralFace(Triangulator triRef, int i1, int i2) {
	int removed;
	int i, j, numSorted, index;
	int ind1, ind2;

	initPUnsorted(triRef, triRef.numPoints);

	for (i = 0;  i < triRef.numPoints;  ++i)
	    triRef.pUnsorted[i].set(triRef.points[i]);

	// sort points according to lexicographical order
	/*
	   System.out.println("Points : (Unsorted)");
	   for(i=0; i<triRef.numPoints; i++)
	   System.out.println( i + "pt ( " + triRef.points[i].x + ", " +
	   triRef.points[i].y + ")");
	*/

	//    qsort(points, num_pnts, sizeof(point), &p_comp);

	sort(triRef.points, triRef.numPoints);

	/*
	   System.out.println("Points : (Sorted)");
	   for(i=0; i<triRef.numPoints; i++)
	   System.out.println( i +"pt ( " + triRef.points[i].x + ", " +
	   triRef.points[i].y + ")");
	*/

	// eliminate duplicate vertices
	i = 0;
	for (j = 1;  j < triRef.numPoints;  ++j) {
	    if (pComp(triRef.points[i], triRef.points[j])  != 0) {
		++i;
		triRef.points[i] = triRef.points[j];
	    }
	}
	numSorted = i + 1;
	removed = triRef.numPoints - numSorted;

	/*
	  System.out.println("Points : (Sorted and eliminated)");
	  for(i=0; i<triRef.numPoints; i++)
	  System.out.println( i + "pt ( " + triRef.points[i].x + ", " +
	  triRef.points[i].y + ")");
	*/

	// renumber the vertices of the polygonal face
	for (i = i1;  i < i2;  ++i) {
	    ind1 = triRef.loops[i];
	    ind2 = triRef.fetchNextData(ind1);
	    index = triRef.fetchData(ind2);
	    while (ind2 != ind1) {
		j = findPInd(triRef.points, numSorted, triRef.pUnsorted[index]);
		triRef.updateIndex(ind2, j);
		ind2 = triRef.fetchNextData(ind2);
		index = triRef.fetchData(ind2);
	    }
	    j = findPInd(triRef.points, numSorted, triRef.pUnsorted[index]);
	    triRef.updateIndex(ind2, j);
	}

	triRef.numPoints = numSorted;

	return removed;
    }


    static void sort(Point2f points[], int numPts) {
	int i,j;
	Point2f swap = new Point2f();

	for (i = 0; i < numPts; i++){
	    for (j = i + 1; j < numPts; j++){
		if (pComp(points[i], points[j]) > 0){
		    swap.set(points[i]);
		    points[i].set(points[j]);
		    points[j].set(swap);
		}
	    }
	}
	/*
	   for (i = 0; i < numPts; i++) {
		System.out.println("pt " + points[i]);
	   }
	*/
    }

    static int findPInd(Point2f sorted[], int numPts, Point2f pnt) {
	int i;

	for (i = 0; i < numPts; i++){
	    if ((pnt.x == sorted[i].x) &&
		(pnt.y == sorted[i].y)){
		return i;
	    }
	}
	return -1;
    }

    static int pComp(Point2f a, Point2f b) {
	if (a.x < b.x)
	    return -1;
	else if (a.x > b.x)
	    return  1;
	else  {
	    if (a.y < b.y)
		return -1;
	    else if (a.y > b.y)
		return  1;
	    else
		return  0;
	}
    }

}
