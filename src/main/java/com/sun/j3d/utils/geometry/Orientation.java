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

class Orientation {

    /**
     * determine the outer polygon and the orientation of the polygons; the
     * default orientation is CCW for the outer-most polygon, and CW for the
     * inner polygons. the polygonal loops are referenced by  loops[i1,..,i2-1].
     */
    static void adjustOrientation(Triangulator triRef, int i1, int i2) {

	double area;
	int i, outer;
	int ind;

	if(i1 >= i2)
	    System.out.println("Orientation:adjustOrientation Problem i1>=i2 !!!");

	if (triRef.numLoops >= triRef.maxNumPolyArea)  {
	    // System.out.println("Orientation:adjustOrientation Expanding polyArea array .");
	    triRef.maxNumPolyArea = triRef.numLoops;
	    double old[] = triRef.polyArea;
	    triRef.polyArea = new double[triRef.maxNumPolyArea];
	    if(old != null)
		System.arraycopy(old, 0, triRef.polyArea, 0, old.length);
	}

	// for each contour, compute its signed area, i.e., its orientation. the
	// contour with largest area is assumed to be the outer-most contour.
	for (i = i1;  i < i2;  ++i) {
	    ind = triRef.loops[i];
	    triRef.polyArea[i] = polygonArea(triRef, ind);
	}

	// determine the outer-most contour
	area  = Math.abs(triRef.polyArea[i1]);
	outer = i1;
	for (i = i1 + 1;  i < i2;  ++i) {
	    if (area < Math.abs(triRef.polyArea[i]))  {
		area  = Math.abs(triRef.polyArea[i]);
		outer = i;
	    }
	}

	// default: the outer contour is referenced by  loops[i1]
	if (outer != i1) {
	    ind = triRef.loops[i1];
	    triRef.loops[i1] = triRef.loops[outer];
	    triRef.loops[outer] = ind;

	    area = triRef.polyArea[i1];
	    triRef.polyArea[i1] =  triRef.polyArea[outer];
	    triRef.polyArea[outer] = area;
	}

	// adjust the orientation
	if (triRef.polyArea[i1] < 0.0)   triRef.swapLinks(triRef.loops[i1]);
	for (i = i1 + 1;  i < i2;  ++i) {
	    if (triRef.polyArea[i] > 0.0)   triRef.swapLinks(triRef.loops[i]);
	}
    }

    /**
     * This function computes twice the signed area of a simple closed polygon.
     */
    static double polygonArea(Triangulator triRef, int ind) {
	int hook = 0;
	int ind1, ind2;
	int i1, i2;
	double area = 0.0, area1 = 0;

	ind1 = ind;
	i1 = triRef.fetchData(ind1);
	ind2 = triRef.fetchNextData(ind1);
	i2 = triRef.fetchData(ind2);
	area = Numerics.stableDet2D(triRef, hook, i1, i2);

	ind1 = ind2;
	i1   = i2;
	while (ind1 != ind) {
	    ind2  = triRef.fetchNextData(ind1);
	    i2 = triRef.fetchData(ind2);
	    area1 = Numerics.stableDet2D(triRef, hook, i1, i2);
	    area += area1;
	    ind1  = ind2;
	    i1    = i2;
	}

	return  area;
    }


    /**
     * Determine the orientation of the polygon. The default orientation is CCW.
     */
    static void determineOrientation(Triangulator triRef, int ind) {
	double area;

	// compute the polygon's signed area, i.e., its orientation.
	area = polygonArea(triRef, ind);

	// adjust the orientation (i.e., make it CCW)
	if (area < 0.0)   {
	    triRef.swapLinks(ind);
	    triRef.ccwLoop = false;
	}

    }

}
