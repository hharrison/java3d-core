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

class Degenerate {

    /**
     * This function checks whether the triangle  i1, i2, i3  is an ear, where
     * the vertex  i4  lies on at least one of the two edges  i1, i2  or  i3, i1.
     * basically, we can cut the polygon at  i4  into two pieces. the polygon
     * touches at  i4  back to back if following the next-pointers in one
     * subpolygon and following the prev-pointers in the other subpolygon yields
     * the same orientation for both subpolygons. otherwise,  i4  forms a
     * bottle neck of the polygon, and  i1, i2, i3  is no valid ear.
     *
     * Note that this function may come up with the incorrect answer if the
     * polygon has self-intersections.
     */
    static boolean handleDegeneracies(Triangulator triRef, int i1, int ind1, int i2,
				      int i3, int i4, int ind4) {
	int i0, i5;
	int type[] = new int[1];
	int ind0, ind2, ind5;
	boolean flag;
	double area = 0.0, area1 = 0, area2 = 0.0;

	/* assert(InPointsList(i1));
	   assert(InPointsList(i2));
	   assert(InPointsList(i3));
	   assert(InPointsList(i4));
	*/

	// first check whether the successor or predecessor of  i4  is inside the
	// triangle, or whether any of the two edges incident at  i4  intersects
	// i2, i3.
	ind5 = triRef.fetchPrevData(ind4);
	i5 = triRef.fetchData(ind5);

	// assert(ind4 != ind5);
	//assert(InPointsList(i5));
	if ((i5 != i2)  &&  (i5 != i3)) {
	    flag = Numerics.vtxInTriangle(triRef, i1, i2, i3, i5, type);
	    if (flag  &&  (type[0] == 0))  return true;
	    if (i2 <= i3) {
		if (i4 <= i5)
		    flag = Numerics.segIntersect(triRef, i2, i3, i4, i5, -1);
		else
		    flag = Numerics.segIntersect(triRef, i2, i3, i5, i4, -1);
	    }
	    else {
		if (i4 <= i5)
		    flag = Numerics.segIntersect(triRef, i3, i2, i4, i5, -1);
		else
		    flag = Numerics.segIntersect(triRef, i3, i2, i5, i4, -1);
	    }
	    if (flag)
		return true;
	}

	ind5 = triRef.fetchNextData(ind4);
	i5 = triRef.fetchData(ind5);
	// assert(ind4 != ind5);
	// assert(InPointsList(i5));
	if ((i5 != i2)  &&  (i5 != i3)) {
	    flag = Numerics.vtxInTriangle(triRef, i1, i2, i3, i5, type);
	    if (flag  &&  (type[0] == 0))  return true;
	    if (i2 <= i3) {
		if (i4 <= i5)      flag = Numerics.segIntersect(triRef, i2, i3, i4, i5, -1);
		else               flag = Numerics.segIntersect(triRef, i2, i3, i5, i4, -1);
	    }
	    else {
		if (i4 <= i5)      flag = Numerics.segIntersect(triRef, i3, i2, i4, i5, -1);
		else               flag = Numerics.segIntersect(triRef, i3, i2, i5, i4, -1);
	    }
	    if (flag)  return true;
	}

	i0   = i1;
	ind0 = ind1;
	ind1 = triRef.fetchNextData(ind1);
	i1 = triRef.fetchData(ind1);
	while (ind1 != ind4) {
	    ind2  = triRef.fetchNextData(ind1);
	    i2 = triRef.fetchData(ind2);
	    area = Numerics.stableDet2D(triRef, i0, i1, i2);
	    area1 += area;
	    ind1   = ind2;
	    i1     = i2;
	}

	ind1 = triRef.fetchPrevData(ind0);
	i1 = triRef.fetchData(ind1);
	while (ind1 != ind4) {
	    ind2  = triRef.fetchPrevData(ind1);
	    i2 = triRef.fetchData(ind2);
	    area = Numerics.stableDet2D(triRef, i0, i1, i2);
	    area2 += area;
	    ind1   = ind2;
	    i1     = i2;
	}

	if (Numerics.le(area1, triRef.ZERO)  &&  Numerics.le(area2, triRef.ZERO))
	    return false;
	else if (Numerics.ge(area1, triRef.ZERO)  &&  Numerics.ge(area2, triRef.ZERO))
	    return false;
	else
	    return true;
    }
}
