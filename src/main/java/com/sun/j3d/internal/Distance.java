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

// --------------------------------------------------
//
// Distance routines, ported from:
//
// Magic Software, Inc.
// http://www.magic-software.com
// http://www.wild-magic.com
// Copyright (c) 2004.  All Rights Reserved
//
// The Wild Magic Library (WML) source code is supplied under the terms of
// the license agreement http://www.magic-software.com/License/WildMagic.pdf
// and may not be copied or disclosed except in accordance with the terms of
// that agreement.
//
// --------------------------------------------------

package com.sun.j3d.internal;

import javax.vecmath.*;

/**
 * Utility class used to calculate distance. Contains static methods
 * used by picking method to determine intersections.
 */

public class Distance {
  /* Threshold factor to determine if two lines are parallel */
  static final double FUZZ = 1E-5;

  /* Utility method, for easy switch between distance and squared distance */
  private static final double DIST (double in) {
    //    return Math.sqrt (Math.abs (in));
    return Math.abs (in);
  }

  /**
   * Minimum ray to segment distance.
   *
   * @param rayorig Origin of the ray
   * @param raydir Direction of the ray
   * @param segstart Segment start point
   * @param segend Segment end point
   * @return the square of the minimum distance from the ray to the segment
   */
  static public double rayToSegment (Point3d rayorig, 
				     Vector3d raydir,
				     Point3d segstart, 
				     Point3d segend) {
    return rayToSegment (rayorig, raydir, segstart, segend, null, null, null);
  }

  /**
   * Minimum ray to segment distance. Returns the square of the distance.
   *
   * @param rayorig Origin of the ray
   *
   * @param raydir Direction of the ray
   *
   * @param segstart Segment start point
   *
   * @param segend Segment end point
   *
   * @param rayint If non-null, will be filled with the coordinates of
   * the point corresponding to the minimum distance on the ray.
   *
   * @param segint If non-null, will be filled with the coordinates of
   * the point corresponding to the minimum distance on the segment.
   *
   * @param param An array of two doubles, will be filled with the
   * parametric factors used to find the point of shortest distance on
   * each primitive (ray = O +sD, with O=origin and
   * D=direction). param[0] will contain the parameter for the ray,
   * and param[1] the parameter for the segment.
   *
   * @return the square of the minimum distance from the ray to the
   * segment
   */
  static public double rayToSegment (Point3d rayorig, 
				     Vector3d raydir,
				     Point3d segstart, 
				     Point3d segend,
				     Point3d rayint,
				     Point3d segint,
				     double[] param) {
    double s, t;

    Vector3d diff = new Vector3d();
    diff.sub (rayorig,segstart);
    Vector3d segdir = new Vector3d();
    segdir.sub (segend, segstart);
    /*
      System.out.println (rayorig + "\n" + raydir + "\n" + segstart + "\n" +
      segdir);
      */
    double A = raydir.dot (raydir);//Dot(ray.m,ray.m);
    double B = -raydir.dot (segdir);//-Dot(ray.m,seg.m);
    double C = segdir.dot (segdir);//Dot(seg.m,seg.m);
    double D = raydir.dot (diff);//Dot(ray.m,diff);
    double E;  // -Dot(seg.m,diff), defer until needed
    double F = diff.dot (diff);//Dot(diff,diff);
    double det = Math.abs(A*C-B*B);  // A*C-B*B = |Cross(M0,M1)|^2 >= 0

    double tmp;

    if (det >= FUZZ) {
      // ray and segment are not parallel
      E = -segdir.dot (diff);//-Dot(seg.m,diff);
      s = B*E-C*D;
      t = B*D-A*E;

      if (s >= 0) {
	if (t >= 0) {
	  if (t <= det) { // region 0
	    // minimum at interior points of ray and segment
	    double invDet = 1.0f/det;
	    s *= invDet;
	    t *= invDet;
	    if (rayint!=null) rayint.scaleAdd (s, raydir, rayorig);
	    if (segint!=null) segint.scaleAdd (t, segdir, segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(s*(A*s+B*t+2*D)+t*(B*s+C*t+2*E)+F);
	  }
	  else {  // region 1
		  
	    t = 1;
	    if (D >= 0) {
	      s = 0;
	      if (rayint!=null) rayint.set (rayorig);
	      if (segint!=null) segint.set (segend);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(C+2*E+F);
	    }
	    else {
	      s = -D/A;
	      if (rayint!=null) rayint.scaleAdd (s, raydir, rayorig);
	      if (segint!=null) segint.set (segend);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST((D+2*B)*s+C+2*E+F);
	    }
	  }
	}
	else { // region 5
	  t = 0;
	  if (D >= 0) {
	    s = 0;
	    if (rayint != null) rayint.set (rayorig);
	    if (segint != null) segint.set (segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(F);
	  }
	  else {
	    s = -D/A;
	    if (rayint != null) rayint.scaleAdd (s, raydir, rayorig);
	    if (segint != null) segint.set (segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(D*s+F);
	  }
	}
      }
      else {
	if (t <= 0) { // region 4
	  if (D < 0) {
	    s = -D/A;
	    t = 0;
	    if (rayint != null) rayint.scaleAdd (s, raydir, rayorig);
	    if (segint != null) segint.set (segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(D*s+F);
	  }
	  else {
	    s = 0;
	    if (E >= 0) {
	      t = 0;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.set (segstart);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else if (-E >= C) {
	      t = 1;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.set (segend);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(C+2*E+F);
	    }
	    else {
	      t = -E/C;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.scaleAdd (t, segdir, segstart);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(E*t+F);
	    }
	  }
	}
	else if (t <= det) { // region 3
	  s = 0;
	  if (E >= 0) {
	    t = 0;
	    if (rayint != null) rayint.set (rayorig);
	    if (segint != null) segint.set (segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(F);
	  }
	  else if (-E >= C) {
	    t = 1;
	    if (rayint != null) rayint.set (rayorig);
	    if (segint != null) segint.set (segend);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(C+2*E+F);
	  }
	  else {
	    t = -E/C;
	    if (rayint != null) rayint.set (rayorig);
	    if (segint != null) segint.scaleAdd (t, segdir, segstart);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(E*t+F);
	  }
	}
	else { // region 2
	  tmp = B+D;
	  if (tmp < 0) {
	    s = -tmp/A;
	    t = 1;
	    if (rayint != null) rayint.scaleAdd (s, raydir, rayorig);
	    if (segint != null) segint.set (segend);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(tmp*s+C+2*E+F);
	  }
	  else {
	    s = 0;
	    if (E >= 0) {
	      t = 0;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.set (segstart);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else if (-E >= C) {
	      t = 1;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.set (segend);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(C+2*E+F);
	    }
	    else {
	      t = -E/C;
	      if (rayint != null) rayint.set (rayorig);
	      if (segint != null) segint.scaleAdd (t, segdir, segstart);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(E*t+F);
	    }
	  }
	}
      }
    }
    else {
      // ray and segment are parallel
      if (B > 0) {
	// opposite direction vectors
	t = 0;
	if (D >= 0) {
	  s = 0;
	  if (rayint != null) rayint.set (rayorig);
	  if (segint != null) segint.set (segstart);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(F);
	}
	else {
	  s = -D/A;
	  if (rayint != null) rayint.scaleAdd (s, raydir, rayorig);
	  if (segint != null) segint.set (segstart);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(D*s+F);
	}
      }
      else {
	// same direction vectors
	E = segdir.dot (diff);//-Dot(seg.m,diff);
	t = 1;
	tmp = B+D;
	if (tmp >= 0) {
	  s = 0;
	  if (rayint != null) rayint.set (rayorig);
	  if (segint != null) segint.set (segend);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(C+2*E+F);
	}
	else {
	  s = -tmp/A;
	  if (rayint != null) rayint.scaleAdd (s, raydir, rayorig);
	  if (segint != null) segint.set (segend);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(tmp*s+C+2*E+F);
	}
      }
    }
  }

  /**
   * Minimum ray to ray distance. Returns the square of the distance.
   *
   * @param ray0orig Origin of ray 0
   * @param ray0dir Direction of ray 0
   * @param ray1orig Origin of ray 1
   * @param ray1dir Direction of ray 1
   * @return the square of the minimum distance from the ray to the segment    
   */
  static public double rayToRay (Point3d ray0orig, 
				 Vector3d ray0dir,
				 Point3d ray1orig, 
				 Vector3d ray1dir) {
    return rayToRay (ray0orig, ray0dir, ray1orig, ray1dir, null, null, null);
  }

  /**
   * Minimum ray to ray distance. Returns the square of the distance.
   *
   * @param ray0orig Origin of ray 0
   *
   * @param ray0dir Direction of ray 0
   *
   * @param ray1orig Origin of ray 1
   *
   * @param ray1dir Direction of ray 1
   *
   * @param ray0int If non-null, will be filled with the coordinates
   * of the point corresponding to the minimum distance on ray 0.
   *
   * @param ray1int If non-null, will be filled with the coordinates
   * of the point corresponding to the minimum distance on ray 1.
   *
   * @param param An array of two doubles, will be filled with the
   * parametric factors used to find the point of shortest distance on
   * each primitive (ray = O +sD, with O=origin and
   * D=direction). param[0] will contain the parameter for ray0, and
   * param[1] the parameter for ray1.
   *
   * @return the square of the minimum distance from the ray to the segment
   */
  static public double rayToRay (Point3d ray0orig, 
				 Vector3d ray0dir,
				 Point3d ray1orig, 
				 Vector3d ray1dir,
				 Point3d ray0int,
				 Point3d ray1int,
				 double[] param) {

    double s, t;

    Vector3d diff = new Vector3d();
    diff.sub (ray0orig, ray1orig);

    double A = ray0dir.dot (ray0dir); //Dot(ray0.m,ray0.m);
    double B = -ray0dir.dot (ray1dir); //-Dot(ray0.m,ray1.m);
    double C = ray1dir.dot (ray1dir); //Dot(ray1.m,ray1.m);
    double D = ray0dir.dot (diff); //Dot(ray0.m,diff);
    double E;  // -Dot(ray1.m,diff), defer until needed
    double F = diff.dot (diff); //Dot(diff,diff);
    double det = Math.abs(A*C-B*B);  // A*C-B*B = |Cross(M0,M1)|^2 >= 0
    /*
      System.out.println (ray0orig + "\n" + ray0dir + "\n" +
      ray1orig + "\n" + ray1dir);
      System.out.println (A + " " + B + " " + C + " " + D + " " + F + " " + det);
      */
    if (det >= FUZZ) {
      // rays are not parallel
      E = -ray1dir.dot (diff); //-Dot(ray1.m,diff);
      s = B*E-C*D;
      t = B*D-A*E;

      if (s >= 0) {
	if (t >= 0) { // region 0 (interior)
	  // minimum at two interior points of rays
	  double invDet = 1.0f/det;
	  s *= invDet;
	  t *= invDet;
	  if (ray0int != null) ray0int.scaleAdd (s, ray0dir, ray0orig);
	  if (ray1int != null) ray1int.scaleAdd (t, ray1dir, ray1orig);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(s*(A*s+B*t+2*D)+t*(B*s+C*t+2*E)+F);
	}
	else { // region 3 (side)
	  t = 0;
	  if (D >= 0) {
	    s = 0;
	    if (ray0int != null) ray0int.set (ray0orig);
	    if (ray1int != null) ray1int.set (ray1orig);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(F);
	  }
	  else {
	    s = -D/A;
	    if (ray0int != null) ray0int.scaleAdd (s, ray0dir, ray0orig);
	    if (ray1int != null) ray1int.set (ray1orig);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(D*s+F);
	  }
	}
      }
      else {
	if (t >= 0) {  // region 1 (side)
	  s = 0;
	  if (E >= 0) {
	    t = 0;
	    if (ray0int != null) ray0int.set (ray0orig);
	    if (ray1int != null) ray1int.set (ray1orig);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(F);
	  }
	  else {
	    t = -E/C;
	    if (ray0int != null) ray0int.set (ray0orig);
	    if (ray1int != null) ray1int.scaleAdd (t, ray1dir, ray1orig);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(E*t+F);
	  }
	}
	else { // region 2 (corner)
	  if (D < 0) {
	    s = -D/A;
	    t = 0;
	    if (ray0int != null) ray0int.scaleAdd (s, ray0dir, ray0orig);
	    if (ray1int != null) ray1int.set (ray1orig);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(D*s+F);
	  }
	  else {
	    s = 0;
	    if (E >= 0) {
	      t = 0;
	      if (ray0int != null) ray0int.set (ray0orig);
	      if (ray1int != null) ray1int.set (ray1orig);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else {
	      t = -E/C;
	      if (ray0int != null) ray0int.set (ray0orig);
	      if (ray1int != null) ray1int.scaleAdd (t, ray1dir, ray1orig);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(E*t+F);
	    }
	  }
	}
      }
    }
    else {
      // rays are parallel
      if (B > 0) {
	// opposite direction vectors
	t = 0;
	if (D >= 0) {
	  s = 0;
	  if (ray0int != null) ray0int.set (ray0orig);
	  if (ray1int != null) ray1int.set (ray1orig);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(F);
	}
	else {
	  s = -D/A;
	  if (ray0int != null) ray0int.scaleAdd (s, ray0dir, ray0orig);
	  if (ray1int != null) ray1int.set (ray1orig);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(D*s+F);
	}
      }
      else {
	// same direction vectors
	if (D >= 0) {
	  E = ray1dir.dot (diff); //-Dot(ray1.m,diff);
	  s = 0;
	  t = -E/C;
	  if (ray0int != null) ray0int.set (ray0orig);
	  if (ray1int != null) ray1int.scaleAdd (t, ray1dir, ray1orig);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(E*t+F);
	}
	else {
	  s = -D/A;
	  t = 0;
	  if (ray0int != null) ray0int.scaleAdd (s, ray0dir, ray0orig);
	  if (ray1int != null) ray1int.set (ray1orig);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(D*s+F);
	}
      }
    }
  }

  /**
   * Minimum pt to ray distance. Returns the square of the distance.
   * @param pt The point
   * @param rayorig Origin of the ray
   * @param raydir Direction of the ray
   * @return the square of the minimum distance between the point and the ray
   */
  static public double pointToRay (Point3d pt, 
				   Point3d rayorig, 
				   Vector3d raydir) {
    return pointToRay (pt, rayorig, raydir, null, null);
  }

  /**
   * Minimum pt to ray distance. Returns the square of the distance.
   *
   * @param pt The point
   *
   * @param rayorig Origin of the ray
   *
   * @param raydir Direction of the ray
   *
   * @param rayint If non-null, will be filled with the coordinates of
   * the point corresponding to the minimum distance on the ray.
   *
   * @param param An array of one double, will be filled with the
   * parametric factors used to find the point of shortest distance on
   * the ray (ray = O +sD, with O=origin and D=direction). param[0]
   * will contain the parameter for the ray.
   *
   * @return the square of the minimum distance between the point and the ray
   */
  static public double pointToRay (Point3d pt, 
				   Point3d rayorig, 
				   Vector3d raydir,
				   Point3d rayint,
				   double[] param) {

    double t;

    Vector3d diff = new Vector3d();
    diff.sub (pt, rayorig);
    t = raydir.dot (diff); //Dot(ray.m,diff);

    if (t <= 0.0) {
      t = 0.0; // behind start of ray
      if (rayint != null) rayint.set (rayorig);
      if (param != null) { param[0] = t; }
    } else {
      t /= raydir.dot (raydir); //Dot(ray.m,ray.m);
      diff.scaleAdd (-t, raydir, diff); // diff = diff - t*ray.m;
      if (rayint != null) rayint.scaleAdd (t, raydir, rayorig);
      if (param != null) { param[0] = t; }
    }
    return diff.dot(diff);
  }

  /**
   * Minimum pt to segment distance. Returns the square of the distance.
   */
  static public double pointToSegment (Point3d pt, 
				       Point3d segstart, 
				       Point3d segend) {
    return pointToSegment (pt, segstart, segend, null, null);
  }

  /**
   * Minimum pt to segment distance. Returns the square of the distance.
   */
  static public double pointToSegment (Point3d pt, 
				       Point3d segstart, 
				       Point3d segend,
				       Point3d segint,
				       double[] param) {

    double t;
    Vector3d segdir = new Vector3d ();
    segdir.sub (segend, segstart);
    Vector3d diff = new Vector3d();
    diff.sub (pt,segstart);
    t = segdir.dot (diff); //Dot(seg.m,diff);

    if (t <= 0.0) {
      t = 0.0f;
      if (segint != null) segint.set (segstart);
      if (param != null) { param[0] = t; }
    } 
    else {
      double mDotm = segdir.dot (segdir); //Dot(seg.m,seg.m);
      if (t >= mDotm) {
	t = 1.0f;
	diff.sub (segdir);
	if (segint != null) segint.set (segend);
	if (param != null) { param[0] = t; }
      }
      else {
	t /= mDotm;
	diff.scaleAdd (-t, segdir, diff); //diff = diff - t*seg.m;
	if (segint != null) segint.scaleAdd (t, segdir, segstart);
	if (param != null) { param[0] = t; }
      }
    }
    return diff.dot(diff); //DIST(diff);
  }


  /**
   * Minimum segment to segment distance. Returns the square of the distance.
   * @param seg0start the start of segment 0
   * @param seg0end the end of segment 0
   * @param seg1start the start of segment 1
   * @param seg1end the end of segment 1
   * @return the square of the minimum distance from segment to segment    
   */
  static public double segmentToSegment (Point3d seg0start,
					 Point3d seg0end,
					 Point3d seg1start, 
					 Point3d seg1end) {
    return segmentToSegment (seg0start, seg0end, seg1start, seg1end, 
			     null, null, null);
  }

  /**
   * Minimum segment to segment distance. Returns the square of the distance.
   *
   * @param seg0start the start of segment 0
   *
   * @param seg0end the end of segment 0
   *
   * @param seg1start the start of segment 1
   *
   * @param seg1end the end of segment 1
   *
   * @param seg0int If non-null, will be filled with the coordinates
   * of the point corresponding to the minimum distance on segment 0.
   *
   * @param seg1int If non-null, will be filled with the coordinates
   * of the point corresponding to the minimum distance on segment 1.
   *
   * @param param An array of two doubles, will be filled with the
   * parametric factors used to find the point of shortest distance on
   * each primitive (segment = O +sD, with O=origin and
   * D=direction). param[0] will contain the parameter for segment 0,
   * and param[1] the parameter for segment 1.
   *
   * @return the square of the minimum distance from segment to segment
   */
  static public double segmentToSegment (Point3d seg0start,
					 Point3d seg0end,
					 Point3d seg1start, 
					 Point3d seg1end,
					 Point3d seg0int,
					 Point3d seg1int,
					 double[] param) {
    double s,t;

    Vector3d diff = new Vector3d();
    diff.sub (seg0start,seg1start);
    
    Vector3d seg0dir = new Vector3d();
    seg0dir.sub (seg0end, seg0start);
    Vector3d seg1dir = new Vector3d();
    seg1dir.sub (seg1end, seg1start);

    double A = seg0dir.dot (seg0dir); //Dot(seg0dir,seg0dir);
    double B = -seg0dir.dot (seg1dir); //-Dot(seg0dir,seg1dir);
    double C = seg1dir.dot (seg1dir); //Dot(seg1dir,seg1dir);
    double D = seg0dir.dot (diff); //Dot(seg0dir,diff);
    double E;  // -Dot(seg1dir,diff), defer until needed
    double F = diff.dot (diff); //Dot(diff,diff);
    double det = Math.abs(A*C-B*B);  // A*C-B*B = |Cross(M0,M1)|^2 >= 0

    double tmp;

    if (det >= FUZZ) {
      // line segments are not parallel
      E = -seg1dir.dot (diff); //-Dot(seg1dir,diff);
      s = B*E-C*D;
      t = B*D-A*E;
        
      if (s >= 0) {
	if (s <= det) {
	  if (t >= 0) {
	    if (t <= det) {  // region 0 (interior)
	      // minimum at two interior points of 3D lines
	      double invDet = 1.0f/det;
	      s *= invDet;
	      t *= invDet;
	      if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
	      if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(s*(A*s+B*t+2*D)+t*(B*s+C*t+2*E)+F);
	    }
	    else { // region 3 (side)
	      t = 1;
	      tmp = B+D;
	      if (tmp >= 0) {
		s = 0;
		if (seg0int != null) seg0int.set (seg0start);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(C+2*E+F);
	      }
	      else if (-tmp >= A) {
		s = 1;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+C+F+2*(E+tmp));
	      }
	      else {
		s = -tmp/A;
		if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(tmp*s+C+2*E+F);
	      }
	    }
	  }
	  else { // region 7 (side)
	    t = 0;
	    if (D >= 0) {
	      s = 0;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else if (-D >= A) {
	      s = 1;
	      if (seg0int != null) seg0int.set (seg0end);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(A+2*D+F);
	    }
	    else {
	      s = -D/A;
	      if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(D*s+F);
	    }
	  }
	}
	else {
	  if (t >= 0) {
	    if (t <= det) { // region 1 (side)
	      s = 1;
	      tmp = B+E;
	      if (tmp >= 0) {
		t = 0;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+2*D+F);
	      }
	      else if (-tmp >= C) {
		t = 1;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+C+F+2*(D+tmp));
	      }
	      else {
		t = -tmp/C;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(tmp*t+A+2*D+F);
	      }
	    }
	    else { // region 2 (corner)
	      tmp = B+D;
	      if (-tmp <= A) {
		t = 1;
		if (tmp >= 0) {
		  s = 0;
		  if (seg0int != null) seg0int.set (seg0start);
		  if (seg1int != null) seg1int.set (seg1end);
		  if (param != null) { param[0] = s; param[1] = t; }
		  return DIST(C+2*E+F);
		}
		else {
		  s = -tmp/A;
		  if (seg0int!=null) seg0int.scaleAdd (s, seg0dir, seg0start);
		  if (seg1int!=null) seg1int.set (seg1end);
		  if (param != null) { param[0] = s; param[1] = t; }
		  return DIST(tmp*s+C+2*E+F);
		}
	      }
	      else {
		s = 1;
		tmp = B+E;
		if (tmp >= 0) {
		  t = 0;
		  if (seg0int!=null) seg0int.set (seg0end);
		  if (seg1int!=null) seg1int.set (seg1start);
		  if (param != null) { param[0] = s; param[1] = t; }
		  return DIST(A+2*D+F);
		}
		else if (-tmp >= C) {
		  t = 1;
		  if (seg0int != null) seg0int.set (seg0end);
		  if (seg1int != null) seg1int.set (seg1end);
		  if (param != null) { param[0] = s; param[1] = t; }
		  return DIST(A+C+F+2*(D+tmp));
		}
		else {
		  t = -tmp/C;
		  if (seg0int!=null) seg0int.set (seg0end);
		  if (seg1int!=null) seg1int.scaleAdd (t, seg1dir, seg1start);
		  if (param != null) { param[0] = s; param[1] = t; }
		  return DIST(tmp*t+A+2*D+F);
		}
	      }
	    }
	  }
	  else { // region 8 (corner)
	    if (-D < A) {
	      t = 0;
	      if (D >= 0) {
		s = 0;
		if (seg0int != null) seg0int.set (seg0start);
		if (seg1int != null) seg1int.set (seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(F);
	      }
	      else {
		s = -D/A;
		if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
		if (seg1int != null) seg1int.set (seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(D*s+F);
	      }
	    }
	    else {
	      s = 1;
	      tmp = B+E;
	      if (tmp >= 0) {
		t = 0;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+2*D+F);
	      }
	      else if (-tmp >= C) {
		t = 1;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+C+F+2*(D+tmp));
	      }
	      else {
		t = -tmp/C;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(tmp*t+A+2*D+F);
	      }
	    }
	  }
	}
      }
      else {
	if (t >= 0) {
	  if (t <= det) { // region 5 (side)
	    s = 0;
	    if (E >= 0) {
	      t = 0;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else if (-E >= C) {
	      t = 1;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.set (seg1end);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(C+2*E+F);
	    }
	    else {
	      t = -E/C;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(E*t+F);
	    }
	  }
	  else { // region 4 (corner)
	    tmp = B+D;
	    if (tmp < 0) {
	      t = 1;
	      if (-tmp >= A) {
		s = 1;
		if (seg0int != null) seg0int.set (seg0end);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(A+C+F+2*(E+tmp));
	      }
	      else {
		s = -tmp/A;
		if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(tmp*s+C+2*E+F);
	      }
	    }
	    else {
	      s = 0;
	      if (E >= 0) {
		t = 0;
		if (seg0int != null) seg0int.set (seg0start);
		if (seg1int != null) seg1int.set (seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(F);
	      }
	      else if (-E >= C) {
		t = 1;
		if (seg0int != null) seg0int.set (seg0start);
		if (seg1int != null) seg1int.set (seg1end);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(C+2*E+F);
	      }
	      else {
		t = -E/C;
		if (seg0int != null) seg0int.set (seg0start);
		if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
		if (param != null) { param[0] = s; param[1] = t; }
		return DIST(E*t+F);
	      }
	    }
	  }
	}
	else {  // region 6 (corner)
	  if (D < 0) {
	    t = 0;
	    if (-D >= A) {
	      s = 1;
	      if (seg0int != null) seg0int.set (seg0end);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(A+2*D+F);
	    }
	    else {
	      s = -D/A;
	      if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(D*s+F);
	    }
	  }
	  else {
	    s = 0;
	    if (E >= 0) {
	      t = 0;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.set (seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(F);
	    }
	    else if (-E >= C) {
	      t = 1;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.set (seg1end);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(C+2*E+F);
	    }
	    else {
	      t = -E/C;
	      if (seg0int != null) seg0int.set (seg0start);
	      if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
	      if (param != null) { param[0] = s; param[1] = t; }
	      return DIST(E*t+F);
	    }
	  }
	}
      }
    }
    else {
      // line segments are parallel
      if (B > 0) {
	// direction vectors form an obtuse angle
	if (D >= 0) {
	  s = 0;
	  t = 0;
	  if (seg0int != null) seg0int.set (seg0start);
	  if (seg1int != null) seg1int.set (seg1start);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(F);
	}
	else if (-D <= A) {
	  s = -D/A;
	  t = 0;
	  if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
	  if (seg1int != null) seg1int.set (seg1start);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(D*s+F);
	}
	else {
	  E = -seg1dir.dot (diff); //-Dot(seg1dir,diff);
	  s = 1;
	  tmp = A+D;
	  if (-tmp >= B) {
	    t = 1;
	    if (seg0int != null) seg0int.set (seg0end);
	    if (seg1int != null) seg1int.set (seg1end);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(A+C+F+2*(B+D+E));
	  }
	  else {
	    t = -tmp/B;
	    if (seg0int != null) seg0int.set (seg0end);
	    if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(A+2*D+F+t*(C*t+2*(B+E)));
	  }
	}
      }
      else {
	// direction vectors form an acute angle
	if (-D >= A) {
	  s = 1;
	  t = 0;
	  if (seg0int != null) seg0int.set (seg0end);
	  if (seg1int != null) seg1int.set (seg1start);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(A+2*D+F);
	}
	else if (D <= 0) {
	  s = -D/A;
	  t = 0;
	  if (seg0int != null) seg0int.scaleAdd (s, seg0dir, seg0start);
	  if (seg1int != null) seg1int.set (seg1start);
	  if (param != null) { param[0] = s; param[1] = t; }
	  return DIST(D*s+F);
	}
	else {
	  E = -seg1dir.dot (diff); //-Dot(seg1dir,diff);
	  s = 0;
	  if (D >= -B) {
	    t = 1;
	    if (seg0int != null) seg0int.set (seg0start);
	    if (seg1int != null) seg1int.set (seg1end);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(C+2*E+F);
	  }
	  else {
	    t = -D/B;
	    if (seg0int != null) seg0int.set (seg0start);
	    if (seg1int != null) seg1int.scaleAdd (t, seg1dir, seg1start);
	    if (param != null) { param[0] = s; param[1] = t; }
	    return DIST(F+t*(2*E+C*t));
	  }
	}
      }
    }
  }
}




