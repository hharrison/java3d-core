/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import com.sun.j3d.internal.Distance;

/**
 * PickCylinderSegment is a finite cylindrical segment pick shape.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 *
 * @since Java 3D 1.2
 */

public final class PickCylinderSegment extends PickCylinder {

    Point3d end;

    /**
     * Constructs an empty PickCylinderSegment.
     * The origin and end points of the cylindrical segment are
     * initialized to (0,0,0).  The radius is initialized
     * to 0.
     */
    public PickCylinderSegment() {
	this.end = new Point3d();
    }

    /**
     * Constructs a finite cylindrical segment pick shape from the specified
     * parameters.
     * @param origin the origin point of the cylindrical segment.
     * @param end the end point of the cylindrical segment.
     * @param radius the radius of the cylindrical segment.
     */
    public PickCylinderSegment(Point3d origin, Point3d end, double radius) {
	this.origin = new Point3d(origin);
	this.end = new Point3d(end);
	this.radius = radius;
	calcDirection(); // calculate direction, based on start and end
    }
    
    /**
     * Sets the parameters of this PickCylinderSegment to the specified values.
     * @param origin the origin point of the cylindrical segment.
     * @param end the end point of the cylindrical segment.
     * @param radius the radius of the cylindrical segment.
     */
    public void set(Point3d origin, Point3d end, double radius) {
	this.origin.set(origin);
	this.end.set(end);
	this.radius = radius;
	calcDirection(); // calculate direction, based on start and end
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
     * Gets the end point of this PickCylinderSegment.
     * @param end the Point3d object into which the end point
     * will be copied.
     */
    public void getEnd(Point3d end) {
	end.set(this.end);
    }

    /**
     * Returns true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     * @param bounds the bounds object to check
     * @param pickPos the location of the point of intersection (not used for
     * method. Provided for compatibility).
     */
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	Point4d iPnt = new Point4d();

	//
	// ================ BOUNDING SPHERE ================
	//
	if (bounds instanceof BoundingSphere) {
	    Point3d sphCenter = ((BoundingSphere)bounds).getCenter();
	    double sphRadius = ((BoundingSphere)bounds).getRadius();
	    double sqDist = 
		Distance.pointToSegment (sphCenter, origin, end);
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

	    double temp = (center.x - lower.x + radius);
	    double boxRadiusSquared = temp*temp;
	    temp = (center.y - lower.y + radius);
	    boxRadiusSquared += temp*temp;
	    temp = (center.z - lower.z + radius);
	    boxRadiusSquared += temp*temp;		

	    // First, see if cylinder is too far away from BoundingBox
	    double sqDist = 
		Distance.pointToSegment (center, origin, end);
	    if (sqDist > boxRadiusSquared) {
		return false; // we are too far to intersect
	    }
	    else if (sqDist < (radius*radius)) {
		return true; // center is in cylinder
	    }

	    // Then, see if ray intersects
	    if (((BoundingBox)bounds).intersect (origin, end, iPnt)) {
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
		//	System.out.println ("Testing edge: "+edges[i][0]+" - "+edges[i][1]);
		double distToEdge = 
		    Distance.segmentToSegment (origin, end, 
					       edges[i][0], edges[i][1]);
		if (distToEdge <= radius*radius) {
		    //	  System.out.println ("Intersects!");
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
		Distance.pointToSegment (sphCenter, origin, end);
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
						   ptope.verts[i], ptope.verts[j]);
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
    // Return a new PickCylinderSegment that is the transformed (t3d) of this
    // pickCylinderSegment.  
    PickShape transform(Transform3D t3d) {
	
	PickCylinderSegment newPCS = new PickCylinderSegment();
	
	newPCS.origin.x = origin.x;
	newPCS.origin.y = origin.y;
	newPCS.origin.z = origin.z;
	newPCS.radius = radius * t3d.getScale();
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
