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

class Basic {

    static final double D_RND_MAX = 2147483647.0;


    static double detExp(double u_x, double u_y, double u_z,
			 double v_x, double v_y, double v_z,
			 double w_x, double w_y, double w_z) {

	return ((u_x) * ((v_y) * (w_z)  -  (v_z) * (w_y))  -
		(u_y) * ((v_x) * (w_z)  -  (v_z) * (w_x))  +
		(u_z) * ((v_x) * (w_y)  -  (v_y) * (w_x)));
    }


    static double det3D(Tuple3f u, Tuple3f v, Tuple3f w) {
	return ((u).x * ((v).y * (w).z  -  (v).z * (w).y)  -
		(u).y * ((v).x * (w).z  -  (v).z * (w).x)  +
		(u).z * ((v).x * (w).y  -  (v).y * (w).x));
    }


    static double det2D(Tuple2f u, Tuple2f v, Tuple2f w) {
	return (((u).x - (v).x) * ((v).y - (w).y) + ((v).y - (u).y) * ((v).x - (w).x));
    }


    static double length2(Tuple3f u) {
	return (((u).x * (u).x) + ((u).y * (u).y) + ((u).z * (u).z));
    }

    static double lengthL1(Tuple3f u) {
	return (Math.abs((u).x) + Math.abs((u).y) + Math.abs((u).z));
    }

    static double lengthL2(Tuple3f u) {
	return Math.sqrt(((u).x * (u).x) + ((u).y * (u).y) + ((u).z * (u).z));
    }


    static double dotProduct(Tuple3f u, Tuple3f v) {
	return (((u).x * (v).x) + ((u).y * (v).y) + ((u).z * (v).z));
    }


    static double dotProduct2D(Tuple2f u, Tuple2f v) {
	return (((u).x * (v).x) + ((u).y * (v).y));
    }


    static void vectorProduct(Tuple3f p, Tuple3f q, Tuple3f r) {
	(r).x = (p).y * (q).z  -  (q).y * (p).z;
	(r).y = (q).x * (p).z  -  (p).x * (q).z;
	(r).z = (p).x * (q).y  -  (q).x * (p).y;
    }


    static void vectorAdd( Tuple3f p, Tuple3f q, Tuple3f r) {
	(r).x = (p).x + (q).x;
	(r).y = (p).y + (q).y;
	(r).z = (p).z + (q).z;
    }

    static void vectorSub( Tuple3f p, Tuple3f q, Tuple3f r) {
	(r).x = (p).x - (q).x;
	(r).y = (p).y - (q).y;
	(r).z = (p).z - (q).z;
    }


    static void vectorAdd2D( Tuple2f p, Tuple2f q, Tuple2f r) {
	(r).x = (p).x + (q).x;
	(r).y = (p).y + (q).y;
    }


    static void vectorSub2D( Tuple2f p, Tuple2f q, Tuple2f r) {
	(r).x = (p).x - (q).x;
	(r).y = (p).y - (q).y;
    }

    static void invertVector(Tuple3f p) {
	(p).x = -(p).x;
	(p).y = -(p).y;
	(p).z = -(p).z;
    }

    static void divScalar(double scalar, Tuple3f u) {
	(u).x /= scalar;
	(u).y /= scalar;
	(u).z /= scalar;
    }

    static void multScalar2D(double scalar, Tuple2f u) {
	(u).x *= scalar;
	(u).y *= scalar;
    }


    static int signEps(double x, double eps) {
	return ((x <= eps) ? ((x < -eps) ? -1 : 0) : 1);
    }
}
