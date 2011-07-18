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


class Simple {

    /**
     * Handle a triangle or a quadrangle in a simple and efficient way. if the
     * face is more complex than  false  is returned.
     *
     * warning: the correctness of this function depends upon the fact that
     *          `CleanPolyhedralFace' has not yet been executed; i.e., the
     *          vertex indices have not been changed since the execution of
     *          `CleanPolyhedron'! (otherwise, we would have to get the original
     *          indices via calls to `GetOriginal'...)
     */
    static boolean simpleFace(Triangulator triRef, int ind1) {
	int ind0, ind2, ind3, ind4;
	int i1, i2, i3, i0, i4;

	Point3f pq, pr, nr;

	double x, y, z;
	int ori2, ori4;

	ind0 = triRef.fetchPrevData(ind1);
	i0 = triRef.fetchData(ind0);

	if (ind0 == ind1) {
	    // this polygon has only one vertex! nothing to triangulate...
	    System.out.println("***** polygon with only one vertex?! *****\n");
	    return  true;
	}

	ind2 = triRef.fetchNextData(ind1);
	i2 = triRef.fetchData(ind2);
	if (ind0 == ind2) {
	    // this polygon has only two vertices! nothing to triangulate...
	    System.out.println("***** polygon with only two vertices?! *****\n");
	    return  true;
	}

	ind3 = triRef.fetchNextData(ind2);
	i3 = triRef.fetchData(ind3);
	if (ind0 == ind3) {
	    // this polygon is a triangle! let's triangulate it!
	    i1 = triRef.fetchData(ind1);
	    // triRef.storeTriangle(i1, i2, i3);
	    triRef.storeTriangle(ind1, ind2, ind3);
	    return  true;
	}

	ind4 = triRef.fetchNextData(ind3);
	i4 = triRef.fetchData(ind4);
	if (ind0 == ind4) {
	    // this polygon is a quadrangle! not too hard to triangulate it...
	    // we project the corners of the quadrangle onto one of the coordinate
	    // planes
	    triRef.initPnts(5);
	    i1 = triRef.fetchData(ind1);

	    pq = new Point3f();
	    pr = new Point3f();
	    nr = new Point3f();
	    /*
	      System.out.println("ind0 " + ind0 + ", ind1 " + ind1 + ", ind2 " +
	      ind2 + ", ind3 " + ind3 + ", ind4 " + ind4);
	      System.out.println("i0 " + i0 +", i1 " + i1 + ", i2 " + i2 +
	      ", i3 " + i3 + ", i4 " + i4);

	      System.out.println("vert[i1] " + triRef.vertices[i1] +
	      "vert[i2] " + triRef.vertices[i2] +
	      "vert[i3] " + triRef.vertices[i3]);
	    */

	    Basic.vectorSub(triRef.vertices[i1], triRef.vertices[i2], pq);
	    Basic.vectorSub(triRef.vertices[i3], triRef.vertices[i2], pr);
	    Basic.vectorProduct(pq, pr, nr);

	    // System.out.println("pq " + pq + " pr " + pr + " nr " + nr);
	    x = Math.abs(nr.x);
	    y = Math.abs(nr.y);
	    z = Math.abs(nr.z);
	    if ((z >= x)  &&  (z >= y)) {
		// System.out.println("((z >= x)  &&  (z >= y))");
		triRef.points[1].x = triRef.vertices[i1].x;
		triRef.points[1].y = triRef.vertices[i1].y;
		triRef.points[2].x = triRef.vertices[i2].x;
		triRef.points[2].y = triRef.vertices[i2].y;
		triRef.points[3].x = triRef.vertices[i3].x;
		triRef.points[3].y = triRef.vertices[i3].y;
		triRef.points[4].x = triRef.vertices[i4].x;
		triRef.points[4].y = triRef.vertices[i4].y;
	    }
	    else if ((x >= y)  &&  (x >= z)) {
		// System.out.println("((x >= y)  &&  (x >= z))");
		triRef.points[1].x = triRef.vertices[i1].z;
		triRef.points[1].y = triRef.vertices[i1].y;
		triRef.points[2].x = triRef.vertices[i2].z;
		triRef.points[2].y = triRef.vertices[i2].y;
		triRef.points[3].x = triRef.vertices[i3].z;
		triRef.points[3].y = triRef.vertices[i3].y;
		triRef.points[4].x = triRef.vertices[i4].z;
		triRef.points[4].y = triRef.vertices[i4].y;
	    }
	    else {
		triRef.points[1].x = triRef.vertices[i1].x;
		triRef.points[1].y = triRef.vertices[i1].z;
		triRef.points[2].x = triRef.vertices[i2].x;
		triRef.points[2].y = triRef.vertices[i2].z;
		triRef.points[3].x = triRef.vertices[i3].x;
		triRef.points[3].y = triRef.vertices[i3].z;
		triRef.points[4].x = triRef.vertices[i4].x;
		triRef.points[4].y = triRef.vertices[i4].z;
	    }
	    triRef.numPoints = 5;

	    // find a valid diagonal
	    ori2 = Numerics.orientation(triRef, 1, 2, 3);
	    ori4 = Numerics.orientation(triRef, 1, 3, 4);

	    /*
	      for(int i=0; i<5; i++)
	      System.out.println("point " + i + ", " + triRef.points[i]);
	      System.out.println("ori2 : " + ori2 + " ori4 : " + ori4);
	    */

	    if (((ori2 > 0)  &&  (ori4 > 0))  ||
		((ori2 < 0)  &&  (ori4 < 0))) {

		// i1, i3  is a valid diagonal;
		//
		// encode as a 2-triangle strip: the triangles are  (2, 3, 1)
		// and  (1, 3, 4).

		// triRef.storeTriangle(i1, i2, i3);
		// triRef.storeTriangle(i1, i3, i4);
		triRef.storeTriangle(ind1, ind2, ind3);
		triRef.storeTriangle(ind1, ind3, ind4);
	    }
	    else {
		// i2, i4  has to be a valid diagonal. (if this is no valid
		// diagonal then the corners of the quad form a figure of eight;
		// shall we apply any heuristics in order to guess which diagonal
		// is more likely to be the better choice? alternatively, we could
		// return  false  and subject it to the standard triangulation
		// algorithm. well, let's see how this brute-force solution works.)

		// encode as a 2-triangle strip: the triangles are  (1, 2, 4)
		// and (4, 2, 3).

		// triRef.storeTriangle(i2, i3, i4);
		// triRef.storeTriangle(i2, i4, i1);
		triRef.storeTriangle(ind2, ind3, ind4);
		triRef.storeTriangle(ind2, ind4, ind1);
	    }
	    return  true;
	}

	return false;
    }

}
