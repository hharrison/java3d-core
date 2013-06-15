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

}
