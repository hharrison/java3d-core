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
static public double segmentToSegment(Point3d s0start, Point3d s0end,
                                      Point3d s1start, Point3d s1end,
                                      Point3d s0int, Point3d s1int, double[] param) {
	double s, t;

	Vector3d diff = new Vector3d();
	diff.sub(s0start, s1start);

	Vector3d seg0dir = new Vector3d();
	seg0dir.sub(s0end, s0start);
	Vector3d seg1dir = new Vector3d();
	seg1dir.sub(s1end, s1start);

	double A = seg0dir.dot(seg0dir); // Dot(seg0dir,seg0dir);
	double B = -seg0dir.dot(seg1dir); // -Dot(seg0dir,seg1dir);
	double C = seg1dir.dot(seg1dir); // Dot(seg1dir,seg1dir);
	double D = seg0dir.dot(diff); // Dot(seg0dir,diff);
	double E; // -Dot(seg1dir,diff), defer until needed
	double F = diff.dot(diff); // Dot(diff,diff);
	double det = Math.abs(A * C - B * B); // A*C-B*B = |Cross(M0,M1)|^2 >= 0

	double tmp;

	if (det >= ZERO_TOL) {
		// line segments are not parallel
		E = -seg1dir.dot(diff); // -Dot(seg1dir,diff);
		s = B * E - C * D;
		t = B * D - A * E;

		if (s >= 0) {
			if (s <= det) {
				if (t >= 0) {
					if (t <= det) { // region 0 (interior)
						// minimum at two interior points of 3D lines
						double invDet = 1.0f / det;
						s *= invDet;
						t *= invDet;
						if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
						if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(s * (A * s + B * t + 2 * D) + t
								* (B * s + C * t + 2 * E) + F);
					}
					else { // region 3 (side)
						t = 1;
						tmp = B + D;
						if (tmp >= 0) {
							s = 0;
							if (s0int != null) s0int.set(s0start);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(C + 2 * E + F);
						}
						else if (-tmp >= A) {
							s = 1;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + C + F + 2 * (E + tmp));
						}
						else {
							s = -tmp / A;
							if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(tmp * s + C + 2 * E + F);
						}
					}
				}
				else { // region 7 (side)
					t = 0;
					if (D >= 0) {
						s = 0;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(F);
					}
					else if (-D >= A) {
						s = 1;
						if (s0int != null) s0int.set(s0end);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(A + 2 * D + F);
					}
					else {
						s = -D / A;
						if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(D * s + F);
					}
				}
			}
			else {
				if (t >= 0) {
					if (t <= det) { // region 1 (side)
						s = 1;
						tmp = B + E;
						if (tmp >= 0) {
							t = 0;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + 2 * D + F);
						}
						else if (-tmp >= C) {
							t = 1;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + C + F + 2 * (D + tmp));
						}
						else {
							t = -tmp / C;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(tmp * t + A + 2 * D + F);
						}
					}
					else { // region 2 (corner)
						tmp = B + D;
						if (-tmp <= A) {
							t = 1;
							if (tmp >= 0) {
								s = 0;
								if (s0int != null) s0int.set(s0start);
								if (s1int != null) s1int.set(s1end);
								if (param != null) { param[0] = s; param[1] = t; }
								return Math.abs(C + 2 * E + F);
							}
							else {
								s = -tmp / A;
								if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
								if (s1int != null) s1int.set(s1end);
								if (param != null) { param[0] = s; param[1] = t; }
								return Math.abs(tmp * s + C + 2 * E + F);
							}
						}
						else {
							s = 1;
							tmp = B + E;
							if (tmp >= 0) {
								t = 0;
								if (s0int != null) s0int.set(s0end);
								if (s1int != null) s1int.set(s1start);
								if (param != null) { param[0] = s; param[1] = t; }
								return Math.abs(A + 2 * D + F);
							}
							else if (-tmp >= C) {
								t = 1;
								if (s0int != null) s0int.set(s0end);
								if (s1int != null) s1int.set(s1end);
								if (param != null) { param[0] = s; param[1] = t; }
								return Math.abs(A + C + F + 2 * (D + tmp));
							}
							else {
								t = -tmp / C;
								if (s0int != null) s0int.set(s0end);
								if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
								if (param != null) { param[0] = s; param[1] = t; }
								return Math.abs(tmp * t + A + 2 * D + F);
							}
						}
					}
				}
				else { // region 8 (corner)
					if (-D < A) {
						t = 0;
						if (D >= 0) {
							s = 0;
							if (s0int != null) s0int.set(s0start);
							if (s1int != null) s1int.set(s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(F);
						}
						else {
							s = -D / A;
							if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
							if (s1int != null) s1int.set(s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(D * s + F);
						}
					}
					else {
						s = 1;
						tmp = B + E;
						if (tmp >= 0) {
							t = 0;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + 2 * D + F);
						}
						else if (-tmp >= C) {
							t = 1;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + C + F + 2 * (D + tmp));
						}
						else {
							t = -tmp / C;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(tmp * t + A + 2 * D + F);
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
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(F);
					}
					else if (-E >= C) {
						t = 1;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.set(s1end);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(C + 2 * E + F);
					}
					else {
						t = -E / C;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(E * t + F);
					}
				}
				else { // region 4 (corner)
					tmp = B + D;
					if (tmp < 0) {
						t = 1;
						if (-tmp >= A) {
							s = 1;
							if (s0int != null) s0int.set(s0end);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(A + C + F + 2 * (E + tmp));
						}
						else {
							s = -tmp / A;
							if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(tmp * s + C + 2 * E + F);
						}
					}
					else {
						s = 0;
						if (E >= 0) {
							t = 0;
							if (s0int != null) s0int.set(s0start);
							if (s1int != null) s1int.set(s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(F);
						}
						else if (-E >= C) {
							t = 1;
							if (s0int != null) s0int.set(s0start);
							if (s1int != null) s1int.set(s1end);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(C + 2 * E + F);
						}
						else {
							t = -E / C;
							if (s0int != null) s0int.set(s0start);
							if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
							if (param != null) { param[0] = s; param[1] = t; }
							return Math.abs(E * t + F);
						}
					}
				}
			}
			else { // region 6 (corner)
				if (D < 0) {
					t = 0;
					if (-D >= A) {
						s = 1;
						if (s0int != null) s0int.set(s0end);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(A + 2 * D + F);
					}
					else {
						s = -D / A;
						if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(D * s + F);
					}
				}
				else {
					s = 0;
					if (E >= 0) {
						t = 0;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.set(s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(F);
					}
					else if (-E >= C) {
						t = 1;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.set(s1end);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(C + 2 * E + F);
					}
					else {
						t = -E / C;
						if (s0int != null) s0int.set(s0start);
						if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
						if (param != null) { param[0] = s; param[1] = t; }
						return Math.abs(E * t + F);
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
				if (s0int != null) s0int.set(s0start);
				if (s1int != null) s1int.set(s1start);
				if (param != null) { param[0] = s; param[1] = t; }
				return Math.abs(F);
			}
			else if (-D <= A) {
				s = -D / A;
				t = 0;
				if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
				if (s1int != null) s1int.set(s1start);
				if (param != null) { param[0] = s; param[1] = t; }
				return Math.abs(D * s + F);
			}
			else {
				E = -seg1dir.dot(diff); // -Dot(seg1dir,diff);
				s = 1;
				tmp = A + D;
				if (-tmp >= B) {
					t = 1;
					if (s0int != null) s0int.set(s0end);
					if (s1int != null) s1int.set(s1end);
					if (param != null) { param[0] = s; param[1] = t; }
					return Math.abs(A + C + F + 2 * (B + D + E));
				}
				else {
					t = -tmp / B;
					if (s0int != null) s0int.set(s0end);
					if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
					if (param != null) { param[0] = s; param[1] = t; }
					return Math.abs(A + 2 * D + F + t * (C * t + 2 * (B + E)));
				}
			}
		}
		else {
			// direction vectors form an acute angle
			if (-D >= A) {
				s = 1;
				t = 0;
				if (s0int != null) s0int.set(s0end);
				if (s1int != null) s1int.set(s1start);
				if (param != null) { param[0] = s; param[1] = t; }
				return Math.abs(A + 2 * D + F);
			}
			else if (D <= 0) {
				s = -D / A;
				t = 0;
				if (s0int != null) s0int.scaleAdd(s, seg0dir, s0start);
				if (s1int != null) s1int.set(s1start);
				if (param != null) { param[0] = s; param[1] = t; }
				return Math.abs(D * s + F);
			}
			else {
				E = -seg1dir.dot(diff); // -Dot(seg1dir,diff);
				s = 0;
				if (D >= -B) {
					t = 1;
					if (s0int != null) s0int.set(s0start);
					if (s1int != null) s1int.set(s1end);
					if (param != null) { param[0] = s; param[1] = t; }
					return Math.abs(C + 2 * E + F);
				}
				else {
					t = -D / B;
					if (s0int != null) s0int.set(s0start);
					if (s1int != null) s1int.scaleAdd(t, seg1dir, s1start);
					if (param != null) { param[0] = s; param[1] = t; }
					return Math.abs(F + t * (2 * E + C * t));
				}
			}
		}
	}
}
}
