/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import com.sun.j3d.internal.Distance;

/**
 * PickConeSegment is a finite cone segment pick shape.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 *
 * @since Java 3D 1.2
 */
public final class PickConeSegment extends PickCone {

    Point3d end;

    /**
     * Constructs an empty PickConeSegment.
     * The origin and end point of the cone are
     * initialized to (0,0,0).  The spread angle is initialized
     * to <code>PI/64</code> radians.
     */
    public PickConeSegment() {
	end = new Point3d();
    }

    /**
     * Constructs a finite cone pick shape from the specified
     * parameters.
     * @param origin the origin of the cone
     * @param end the end of the cone along the direction vector
     * @param spreadAngle the spread angle of the cone in radians
     */
    public PickConeSegment (Point3d origin, Point3d end, double spreadAngle) {
	this.origin = new Point3d(origin);
	this.end = new Point3d(end);
	this.direction = new Vector3d();
	this.spreadAngle = spreadAngle;
	calcDirection(); // calculate direction, based on start and end
    }

    /**
     * Sets the parameters of this PickCone to the specified values.
     * @param origin the origin of the cone
     * @param end the end of the cone
     * @param spreadAngle the spread angle of the cone in radians
     */
    public void set(Point3d origin, Point3d end, double spreadAngle) {
	this.origin.set(origin);
	this.end.set (end);
	this.spreadAngle = spreadAngle;
	calcDirection(); // calculate direction, based on start and end
    }

    /**
     * Gets the end point of this PickConeSegment.
     * @param end the Point3d object into which the end point
     * will be copied.
     */
    public void getEnd(Point3d end) {
	end.set(this.end);
    }

    /** Calculates the direction for this PickCylinderSegment, based on start
      and end points.
      */
    private void calcDirection() {
	this.direction.x = end.x - origin.x;
	this.direction.y = end.y - origin.y;
	this.direction.z = end.z - origin.z;
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     * @param bounds the bounds object to check
     * @param pickPos the location of the point of intersection (not used for
     * method. Provided for compatibility).
     */
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	Point4d iPnt = new Point4d();
	Vector3d vector = new Vector3d();
	Point3d rayPt = new Point3d();

	double distance;
	double radius;

	//
	// ================ BOUNDING SPHERE ================
	//
	if (bounds instanceof BoundingSphere) {
	    Point3d sphCenter = ((BoundingSphere)bounds).getCenter();
	    double sphRadius = ((BoundingSphere)bounds).getRadius();
	    double sqDist =
		Distance.pointToSegment (sphCenter, origin, end, rayPt, null);

	    vector.sub (rayPt, origin);
	    distance = vector.length();
	    radius = getRadius (distance);
	    if (sqDist <= (sphRadius+radius)*(sphRadius+radius)) {
		return true;
	    }
	    return false; // we are too far to intersect
	}
	//
	// ================ BOUNDING BOX ================
	//
	else if (bounds instanceof BoundingBox) {
	    // Calculate radius of BoundingBox
	    Point3d lower = new Point3d();
	    ((BoundingBox)bounds).getLower (lower);

	    Point3d center = ((BoundingBox)bounds).getCenter ();

	    // First, see if cone is too far away from BoundingBox
	    double sqDist =
		Distance.pointToSegment (center, origin, end, rayPt, null);

	    vector.sub (rayPt, origin);
	    distance = vector.length();
	    radius = getRadius (distance);

	    double temp = (center.x - lower.x + radius);
	    double boxRadiusSquared = temp*temp;
	    temp = (center.y - lower.y + radius);
	    boxRadiusSquared += temp*temp;
	    temp = (center.z - lower.z + radius);
	    boxRadiusSquared += temp*temp;

	    if (sqDist > boxRadiusSquared) {
		return false; // we are too far to intersect
	    }
	    else if (sqDist < (radius*radius)) {
		return true; // center is in cone
	    }

	    // Then, see if ray intersects
	    if (((BoundingBox)bounds).intersect (origin, direction, iPnt)) {
		return true;
	    }

	    // Ray does not intersect, test for distance with each edge
	    Point3d upper = new Point3d();
	    ((BoundingBox)bounds).getUpper (upper);

	    Point3d[][] edges = {
		// Top horizontal 4
		{upper, new Point3d (lower.x, upper.y, upper.z)},
		{new Point3d(lower.x, upper.y, upper.z), new Point3d(lower.x, lower.y, upper.z)},
		{new Point3d(lower.x, lower.y, upper.z), new Point3d(upper.x, lower.y, upper.z)},
		{new Point3d(upper.x, lower.y, upper.z), upper},
		// Bottom horizontal 4
		{lower, new Point3d(lower.x, upper.y, lower.z)},
		{new Point3d(lower.x, upper.y, lower.z), new Point3d(upper.x, upper.y, lower.z)},
		{new Point3d(upper.x, upper.y, lower.z), new Point3d(upper.x, lower.y, lower.z)},
		{new Point3d(upper.x, lower.y, lower.z), lower},
		// Vertical 4
		{lower, new Point3d(lower.x, lower.y, upper.z)},
		{new Point3d(lower.x, upper.y, lower.z), new Point3d(lower.x, upper.y, upper.z)},
		{new Point3d(upper.x, upper.y, lower.z), new Point3d(upper.x, upper.y, upper.z)},
		{new Point3d(upper.x, lower.y, lower.z), new Point3d(upper.x, lower.y, upper.z)}
	    };
	    for (int i=0;i<edges.length;i++) {
		//	System.err.println ("Testing edge: "+edges[i][0]+" - "+edges[i][1]);
		double distToEdge =
		    Distance.segmentToSegment (origin, end, edges[i][0], edges[i][1],
					       rayPt, null, null);

		vector.sub (rayPt, origin);
		distance = vector.length();
		radius = getRadius (distance);
		/* System.err.println ("PickConeSegment: distance: " +
		   distance+" radius: " + radius +
		   " distToEdge:" +Math.sqrt(distToEdge));
		*/

		if (distToEdge <= radius*radius) {
		    //	  System.err.println ("Intersects!");
		    return true;
		}
	    }
	    return false; // Not close enough
	}
	//
	// ================ BOUNDING POLYTOPE ================
	//
	else if (bounds instanceof BoundingPolytope) {
	    int i, j;

	    // First, check to see if we are too far to intersect the polytope's
	    // bounding sphere
	    Point3d sphCenter = new Point3d();
	    BoundingSphere bsphere = new BoundingSphere (bounds);

	    bsphere.getCenter (sphCenter);
	    double sphRadius = bsphere.getRadius();

	    double sqDist =
		Distance.pointToSegment (sphCenter, origin, end, rayPt, null);

	    vector.sub (rayPt, origin);
	    distance = vector.length();
	    radius = getRadius (distance);

	    if (sqDist > (sphRadius+radius)*(sphRadius+radius)) {
		return false; // we are too far to intersect
	    }

	    // Now check to see if ray intersects with polytope
	    if (bounds.intersect (origin, direction, iPnt)) {
		return true;
	    }

	    // Now check distance to edges. Since we don't know a priori how
	    // the polytope is structured, we will cycle through. We discard edges
	    // when their center is not on the polytope surface.
	    BoundingPolytope ptope = (BoundingPolytope)bounds;
	    Point3d midpt = new Point3d();
	    double distToEdge;
	    for (i=0;i<ptope.nVerts;i++) {
		for (j=i;i<ptope.nVerts;i++) {
		    // XXXX: make BoundingPolytope.pointInPolytope available to package
		    // scope
		    midpt.x = (ptope.verts[i].x + ptope.verts[j].x) * 0.5;
		    midpt.y = (ptope.verts[i].y + ptope.verts[j].y) * 0.5;
		    midpt.z = (ptope.verts[i].z + ptope.verts[j].z) * 0.5;

		    if (! PickCylinder.pointInPolytope (ptope,
							midpt.x, midpt.y, midpt.z)) {
			continue;
		    }
		    distToEdge =
			Distance.segmentToSegment (origin, end,
						   ptope.verts[i], ptope.verts[j],
						   rayPt, null, null);
		    vector.sub (rayPt, origin);
		    distance = vector.length();
		    radius = getRadius (distance);
		    if (distToEdge <= radius*radius) {
			return true;
		    }
		}
	    }
	    return false;
	}
	/*
	else {
	    throw new RuntimeException("intersect method not implemented");
	}
	*/
	return false;
    }

    // Only use within J3D.
    // Return a new PickConeSegment that is the transformed (t3d) of this pickConeSegment.
    PickShape transform(Transform3D t3d) {

	PickConeSegment newPCS = new PickConeSegment();

	newPCS.origin.x = origin.x;
	newPCS.origin.y = origin.y;
	newPCS.origin.z = origin.z;
	newPCS.spreadAngle = spreadAngle;
	newPCS.end.x = end.x;
	newPCS.end.y = end.y;
	newPCS.end.z = end.z;

	t3d.transform(newPCS.origin);
	t3d.transform(newPCS.end);

	newPCS.direction.x = newPCS.end.x - newPCS.origin.x;
	newPCS.direction.y = newPCS.end.y - newPCS.origin.y;
	newPCS.direction.z = newPCS.end.z - newPCS.origin.z;
	newPCS.direction.normalize();

	return newPCS;
    }
}
