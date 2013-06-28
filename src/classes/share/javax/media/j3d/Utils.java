/*
 * Copyright 2013 Harvey Harrison <harvey.harrison@gmail.com>
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 */
package javax.media.j3d;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * A small utility class for internal use.  Mainly contains some distance-calculation
 * methods.
 *
 */
class Utils {

/**
 * Returns the square of the minimum distance from the given point to the segment
 * defined by start, end.
 */
static final double ptToSegSquare(Point3d pt, Point3d start, Point3d end, Point3d closest) {
	Vector3d dir = new Vector3d();
	dir.sub(end, start);

	Vector3d dt = new Vector3d();
	dt.sub(pt, start);

	// Project the point onto the line defined by the segment
	double proj = dir.dot(dt);

	// We projected 'before' the start point, just return the dSquared between
	// the point and the start
	if (proj <= 0.0d) {
		if (closest != null) closest.set(start);
		return dt.lengthSquared();
	}

	// Project the segment onto itself
	double segSquared = dir.lengthSquared();

	// If our point projected off the end of the segment, return the dSquared between
	// the point and the end
	if (proj >= segSquared) {
		if (closest != null) closest.set(end);
		dt.sub(pt, end);
		return dt.lengthSquared();
	}

	// We projected somewhere along the segment, calculate the closest point
	dt.scaleAdd(proj / segSquared, dir, start);
	if (closest != null) closest.set(dt);

	// return the distance from the point to the closest point on the segment
	dt.sub(pt, dt);
	return dt.lengthSquared();
}

/**
 * Returns the square of the minimum distance from the given point to the ray
 * defined by start, dir.
 */
static final double ptToRaySquare(Point3d pt, Point3d start, Vector3d dir, Point3d closest) {
	Vector3d dt = new Vector3d();
	dt.sub(pt, start);

	// Project the point onto the ray
	double proj = dir.dot(dt);

	// We projected 'before' the start point, just return the dSquared between
	// the point and the start
	if (proj <= 0.0d) {
		if (closest != null) closest.set(start);
		return dt.lengthSquared();
	}

	// Project the ray onto itself
	double raySquared = dir.lengthSquared();

	// We projected somewhere along the ray, calculate the closest point
	dt.scaleAdd(proj / raySquared, dir, start);
	if (closest != null) closest.set(dt);

	// return the distance from the point to the closest point on the ray
	dt.sub(pt, dt);
	return dt.lengthSquared();
}

private static final double ZERO_TOL = 1e-5d;

/**
 * Return the square of the minimum distance between two line segments.
 *
 * Code in this method adapted from:
 * Geometric Tools, LLC
 * Copyright (c) 1998-2012
 * Distributed under the Boost Software License, Version 1.0.
 * http://www.boost.org/LICENSE_1_0.txt
 * http://www.geometrictools.com/License/Boost/LICENSE_1_0.txt
 * http://www.geometrictools.com/LibMathematics/Distance/Wm5DistSegment3Segment3.cpp
 * File Version: 5.0.1 (2010/10/01)
 */
static public double segmentToSegment (Point3d s0start, Point3d s0end,
                                       Point3d s1start, Point3d s1end,
                                       Point3d s0int, Point3d s1int, double[] param) {
//    Vector3<Real> diff = mSegment0->Center - mSegment1->Center;
//    Real a01 = -mSegment0->Direction.Dot(mSegment1->Direction);
//    Real b0 = diff.Dot(mSegment0->Direction);
//    Real b1 = -diff.Dot(mSegment1->Direction);
//    Real c = diff.SquaredLength();
//    Real det = Math<Real>::FAbs((Real)1 - a01*a01);
//    Real s0, s1, sqrDist, extDet0, extDet1, tmpS0, tmpS1;
//
//    if (det >= Math<Real>::ZERO_TOLERANCE)
//    {
//        // Segments are not parallel.
//        s0 = a01*b1 - b0;
//        s1 = a01*b0 - b1;
//        extDet0 = mSegment0->Extent*det;
//        extDet1 = mSegment1->Extent*det;
//
//        if (s0 >= -extDet0)
//        {
//            if (s0 <= extDet0)
//            {
//                if (s1 >= -extDet1)
//                {
//                    if (s1 <= extDet1)  // region 0 (interior)
//                    {
//                        // Minimum at interior points of segments.
//                        Real invDet = ((Real)1)/det;
//                        s0 *= invDet;
//                        s1 *= invDet;
//                        sqrDist = s0*(s0 + a01*s1 + ((Real)2)*b0) +
//                            s1*(a01*s0 + s1 + ((Real)2)*b1) + c;
//                    }
//                    else  // region 3 (side)
//                    {
//                        s1 = mSegment1->Extent;
//                        tmpS0 = -(a01*s1 + b0);
//                        if (tmpS0 < -mSegment0->Extent)
//                        {
//                            s0 = -mSegment0->Extent;
//                            sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                                s1*(s1 + ((Real)2)*b1) + c;
//                        }
//                        else if (tmpS0 <= mSegment0->Extent)
//                        {
//                            s0 = tmpS0;
//                            sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                        }
//                        else
//                        {
//                            s0 = mSegment0->Extent;
//                            sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                                s1*(s1 + ((Real)2)*b1) + c;
//                        }
//                    }
//                }
//                else  // region 7 (side)
//                {
//                    s1 = -mSegment1->Extent;
//                    tmpS0 = -(a01*s1 + b0);
//                    if (tmpS0 < -mSegment0->Extent)
//                    {
//                        s0 = -mSegment0->Extent;
//                        sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                            s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else if (tmpS0 <= mSegment0->Extent)
//                    {
//                        s0 = tmpS0;
//                        sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else
//                    {
//                        s0 = mSegment0->Extent;
//                        sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                            s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                }
//            }
//            else
//            {
//                if (s1 >= -extDet1)
//                {
//                    if (s1 <= extDet1)  // region 1 (side)
//                    {
//                        s0 = mSegment0->Extent;
//                        tmpS1 = -(a01*s0 + b1);
//                        if (tmpS1 < -mSegment1->Extent)
//                        {
//                            s1 = -mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else if (tmpS1 <= mSegment1->Extent)
//                        {
//                            s1 = tmpS1;
//                            sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else
//                        {
//                            s1 = mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                    }
//                    else  // region 2 (corner)
//                    {
//                        s1 = mSegment1->Extent;
//                        tmpS0 = -(a01*s1 + b0);
//                        if (tmpS0 < -mSegment0->Extent)
//                        {
//                            s0 = -mSegment0->Extent;
//                            sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                                s1*(s1 + ((Real)2)*b1) + c;
//                        }
//                        else if (tmpS0 <= mSegment0->Extent)
//                        {
//                            s0 = tmpS0;
//                            sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                        }
//                        else
//                        {
//                            s0 = mSegment0->Extent;
//                            tmpS1 = -(a01*s0 + b1);
//                            if (tmpS1 < -mSegment1->Extent)
//                            {
//                                s1 = -mSegment1->Extent;
//                                sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                    s0*(s0 + ((Real)2)*b0) + c;
//                            }
//                            else if (tmpS1 <= mSegment1->Extent)
//                            {
//                                s1 = tmpS1;
//                                sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                            }
//                            else
//                            {
//                                s1 = mSegment1->Extent;
//                                sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                    s0*(s0 + ((Real)2)*b0) + c;
//                            }
//                        }
//                    }
//                }
//                else  // region 8 (corner)
//                {
//                    s1 = -mSegment1->Extent;
//                    tmpS0 = -(a01*s1 + b0);
//                    if (tmpS0 < -mSegment0->Extent)
//                    {
//                        s0 = -mSegment0->Extent;
//                        sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                            s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else if (tmpS0 <= mSegment0->Extent)
//                    {
//                        s0 = tmpS0;
//                        sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else
//                    {
//                        s0 = mSegment0->Extent;
//                        tmpS1 = -(a01*s0 + b1);
//                        if (tmpS1 > mSegment1->Extent)
//                        {
//                            s1 = mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else if (tmpS1 >= -mSegment1->Extent)
//                        {
//                            s1 = tmpS1;
//                            sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else
//                        {
//                            s1 = -mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                    }
//                }
//            }
//        }
//        else
//        {
//            if (s1 >= -extDet1)
//            {
//                if (s1 <= extDet1)  // region 5 (side)
//                {
//                    s0 = -mSegment0->Extent;
//                    tmpS1 = -(a01*s0 + b1);
//                    if (tmpS1 < -mSegment1->Extent)
//                    {
//                        s1 = -mSegment1->Extent;
//                        sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                            s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                    else if (tmpS1 <= mSegment1->Extent)
//                    {
//                        s1 = tmpS1;
//                        sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                    else
//                    {
//                        s1 = mSegment1->Extent;
//                        sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                            s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                }
//                else  // region 4 (corner)
//                {
//                    s1 = mSegment1->Extent;
//                    tmpS0 = -(a01*s1 + b0);
//                    if (tmpS0 > mSegment0->Extent)
//                    {
//                        s0 = mSegment0->Extent;
//                        sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                            s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else if (tmpS0 >= -mSegment0->Extent)
//                    {
//                        s0 = tmpS0;
//                        sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                    }
//                    else
//                    {
//                        s0 = -mSegment0->Extent;
//                        tmpS1 = -(a01*s0 + b1);
//                        if (tmpS1 < -mSegment1->Extent)
//                        {
//                            s1 = -mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else if (tmpS1 <= mSegment1->Extent)
//                        {
//                            s1 = tmpS1;
//                            sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                        else
//                        {
//                            s1 = mSegment1->Extent;
//                            sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                                s0*(s0 + ((Real)2)*b0) + c;
//                        }
//                    }
//                }
//            }
//            else   // region 6 (corner)
//            {
//                s1 = -mSegment1->Extent;
//                tmpS0 = -(a01*s1 + b0);
//                if (tmpS0 > mSegment0->Extent)
//                {
//                    s0 = mSegment0->Extent;
//                    sqrDist = s0*(s0 - ((Real)2)*tmpS0) +
//                        s1*(s1 + ((Real)2)*b1) + c;
//                }
//                else if (tmpS0 >= -mSegment0->Extent)
//                {
//                    s0 = tmpS0;
//                    sqrDist = -s0*s0 + s1*(s1 + ((Real)2)*b1) + c;
//                }
//                else
//                {
//                    s0 = -mSegment0->Extent;
//                    tmpS1 = -(a01*s0 + b1);
//                    if (tmpS1 < -mSegment1->Extent)
//                    {
//                        s1 = -mSegment1->Extent;
//                        sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                            s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                    else if (tmpS1 <= mSegment1->Extent)
//                    {
//                        s1 = tmpS1;
//                        sqrDist = -s1*s1 + s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                    else
//                    {
//                        s1 = mSegment1->Extent;
//                        sqrDist = s1*(s1 - ((Real)2)*tmpS1) +
//                            s0*(s0 + ((Real)2)*b0) + c;
//                    }
//                }
//            }
//        }
//    }
//    else
//    {
//        // The segments are parallel.  The average b0 term is designed to
//        // ensure symmetry of the function.  That is, dist(seg0,seg1) and
//        // dist(seg1,seg0) should produce the same number.
//        Real e0pe1 = mSegment0->Extent + mSegment1->Extent;
//        Real sign = (a01 > (Real)0 ? (Real)-1 : (Real)1);
//        Real b0Avr = ((Real)0.5)*(b0 - sign*b1);
//        Real lambda = -b0Avr;
//        if (lambda < -e0pe1)
//        {
//            lambda = -e0pe1;
//        }
//        else if (lambda > e0pe1)
//        {
//            lambda = e0pe1;
//        }
//
//        s1 = -sign*lambda*mSegment1->Extent/e0pe1;
//        s0 = lambda + sign*s1;
//        sqrDist = lambda*(lambda + ((Real)2)*b0Avr) + c;
//    }
//
//    mClosestPoint0 = mSegment0->Center + s0*mSegment0->Direction;
//    mClosestPoint1 = mSegment1->Center + s1*mSegment1->Direction;
//    mSegment0Parameter = s0;
//    mSegment1Parameter = s1;
//
//    // Account for numerical round-off errors.
//    if (sqrDist < (Real)0)
//    {
//        sqrDist = (Real)0;
//    }
//    return sqrDist;
//}
	return 0.0d;
}
}

