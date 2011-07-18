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

/**
 * Bounding Box class for Triangulator.
 */
class BBox {
    int imin;           /* lexicographically smallest point, determines min-x */
    int imax;           /* lexicographically largest point, determines max-x  */
    double ymin;        /* minimum y-coordinate                               */
    double ymax;        /* maximum y-coordinate                               */

    /**
     * This constructor computes the bounding box of a line segment whose end
     * points  i, j  are sorted according to x-coordinates.
     */
    BBox(Triangulator triRef, int i, int j) {
	// assert(InPointsList(i));
	// assert(InPointsList(j));

	imin = Math.min(i, j);
	imax = Math.max(i, j);
	ymin = Math.min(triRef.points[imin].y, triRef.points[imax].y);
	ymax = Math.max(triRef.points[imin].y, triRef.points[imax].y);
    }


    boolean pntInBBox(Triangulator triRef, int i) {
	return (((imax < i) ? false :
		 ((imin > i) ? false :
		  ((ymax < triRef.points[i].y) ? false :
		   ((ymin > triRef.points[i].y) ? false : true)))));
    }



    boolean BBoxOverlap(BBox bb) {
	return (((imax < (bb).imin) ? false :
		 ((imin > (bb).imax) ? false :
		  ((ymax < (bb).ymin) ? false :
		   ((ymin > (bb).ymax) ? false : true)))));
    }

    boolean BBoxContained(BBox bb) {
	return ((imin <= (bb).imin)  &&  (imax >= (bb).imax)  &&
		(ymin <= (bb).ymin)  &&  (ymax >= (bb).ymax));
    }


    boolean BBoxIdenticalLeaf(BBox bb) {
	return ((imin == (bb).imin)  &&  (imax == (bb).imax));
    }


    void BBoxUnion(BBox bb1, BBox bb3) {
	(bb3).imin = Math.min(imin, (bb1).imin);
	(bb3).imax = Math.max(imax, (bb1).imax);
	(bb3).ymin = Math.min(ymin, (bb1).ymin);
	(bb3).ymax = Math.max(ymax, (bb1).ymax);
    }


    double BBoxArea(Triangulator triRef) {
	return (triRef.points[imax].x - triRef.points[imin].x) * (ymax - ymin);
    }
}
