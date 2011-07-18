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

class BottleNeck {

    static boolean checkArea(Triangulator triRef, int ind4, int ind5) {
	int  ind1, ind2;
	int i0, i1, i2;
	double area = 0.0, area1 = 0, area2 = 0.0;

	i0 = triRef.fetchData(ind4);
	ind1 = triRef.fetchNextData(ind4);
	i1 = triRef.fetchData(ind1);

	while (ind1 != ind5) {
	    ind2  = triRef.fetchNextData(ind1);
	    i2 = triRef.fetchData(ind2);
	    area =Numerics.stableDet2D(triRef, i0, i1, i2);
	    area1 += area;
	    ind1   = ind2;
	    i1     = i2;
	}

	if (Numerics.le(area1, triRef.ZERO))  return false;

	ind1 = triRef.fetchNextData(ind5);
	i1 = triRef.fetchData(ind1);
	while (ind1 != ind4) {
	    ind2  = triRef.fetchNextData(ind1);
	    i2 = triRef.fetchData(ind2);
	    area = Numerics.stableDet2D(triRef, i0, i1, i2);
	    area2 += area;
	    ind1   = ind2;
	    i1     = i2;
	}

	if (Numerics.le(area2, triRef.ZERO))  return false;
	else                  return true;
    }


    // Yet another check needed in order to handle degenerate cases!
    static boolean checkBottleNeck(Triangulator triRef,
				   int i1, int i2, int i3, int ind4) {
	int ind5;
	int i4, i5;
	boolean flag;

	i4 = i1;

	ind5 = triRef.fetchPrevData(ind4);
	i5 = triRef.fetchData(ind5);
	if ((i5 != i2)  &&  (i5 != i3)) {
	    flag = Numerics.pntInTriangle(triRef, i1, i2, i3, i5);
	    if (flag)  return true;
	}

	if (i2 <= i3) {
	    if (i4 <= i5)      flag = Numerics.segIntersect(triRef, i2, i3, i4, i5, -1);
	    else               flag = Numerics.segIntersect(triRef, i2, i3, i5, i4, -1);
	}
	else {
	    if (i4 <= i5)      flag = Numerics.segIntersect(triRef, i3, i2, i4, i5, -1);
	    else               flag = Numerics.segIntersect(triRef, i3, i2, i5, i4, -1);
	}
	if (flag)  return true;

	ind5 = triRef.fetchNextData(ind4);
	i5 = triRef.fetchData(ind5);

	if ((i5 != i2)  &&  (i5 != i3)) {
	    flag = Numerics.pntInTriangle(triRef, i1, i2, i3, i5);
	    if (flag)  return true;
	}

	if (i2 <= i3) {
	    if (i4 <= i5)     flag = Numerics.segIntersect(triRef, i2, i3, i4, i5, -1);
	    else              flag = Numerics.segIntersect(triRef, i2, i3, i5, i4, -1);
	}
	else {
	    if (i4 <= i5)     flag = Numerics.segIntersect(triRef, i3, i2, i4, i5, -1);
	    else              flag = Numerics.segIntersect(triRef, i3, i2, i5, i4, -1);
	}

	if (flag)  return true;

	ind5 = triRef.fetchNextData(ind4);
	i5 = triRef.fetchData(ind5);
	while (ind5 != ind4) {
	    if (i4 == i5) {
		if (checkArea(triRef, ind4, ind5))  return true;
	    }
	    ind5 = triRef.fetchNextData(ind5);
	    i5 = triRef.fetchData(ind5);
	}

	return  false;
    }
}






