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
 * PickCylinderRay is an infinite cylindrical ray pick shape.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 *
 * @since Java 3D 1.2
 */

public final class PickCylinderRay extends PickCylinder {

    /**
     * Constructs an empty PickCylinderRay.
     * The origin and direction of the cylindrical ray are
     * initialized to (0,0,0).  The radius is initialized
     * to 0.
     */
    public PickCylinderRay() {
    }
    
    /**
     * Constructs an infinite cylindrical ray pick shape from the specified
     * parameters.
     * @param origin the origin of the cylindrical ray.
     * @param direction the direction of the cylindrical ray.
     * @param radius the radius of the cylindrical ray.
     */
    public PickCylinderRay(Point3d origin, Vector3d direction, double radius) {
	this.origin = new Point3d(origin);
	this.direction = new Vector3d(direction);
	this.radius = radius;
    }


    /**
     * Sets the parameters of this PickCylinderRay to the specified values.
     * @param origin the origin of the cylindrical ray.
     * @param direction the direction of the cylindrical ray.
     * @param radius the radius of the cylindrical ray.
     */
    public void set(Point3d origin, Vector3d direction, double radius) {
	this.origin.set(origin);
	this.direction.set(direction);
	this.radius = radius;
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

	//
	// ================ BOUNDING SPHERE ================
	//
	if (bounds instanceof BoundingSphere) {
	    Point3d sphCenter = ((BoundingSphere)bounds).getCenter();
	    double sphRadius = ((BoundingSphere)bounds).getRadius();
	    double sqDist = 
		Distance.pointToRay (sphCenter, origin, direction);
	    if (sqDist <= (sphRadius+radius)*(sphRadius+radius)) {
		return true; 
	    }
	    return false;
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
		Distance.pointToRay (center, origin, direction);

	    if (sqDist > boxRadiusSquared ) {
		return false; // we are too far to intersect
	    }
	    else if (sqDist < (radius*radius)) {
		return true; // center is in cylinder
	    }

	    // Then, see if ray intersects
	    if (bounds.intersect (origin, direction, iPnt)) {
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
		    Distance.rayToSegment (origin, direction, edges[i][0], edges[i][1]);
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
		Distance.pointToRay (sphCenter, origin, direction);
	    if (sqDist > (sphRadius+radius) * (sphRadius+radius)) {
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
			Distance.rayToSegment (origin, direction, 
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
    // Return a new PickCylinderRay that is the transformed (t3d) of this pickCylinderRay.  
    PickShape transform(Transform3D t3d) {
	
	PickCylinderRay newPCR = new PickCylinderRay();
	Point3d end = new Point3d();
	/*
	  System.out.println("t3d : ");
	  System.out.println(t3d);
	*/
	newPCR.origin.x = origin.x;
	newPCR.origin.y = origin.y;
	newPCR.origin.z = origin.z;
	newPCR.radius = radius * t3d.getScale();

	end.x = origin.x + direction.x;
	end.y = origin.y + direction.y;
	end.z = origin.z + direction.z;
	
	t3d.transform(newPCR.origin);
	t3d.transform(end);
	
	newPCR.direction.x = end.x - newPCR.origin.x;
	newPCR.direction.y = end.y - newPCR.origin.y;
	newPCR.direction.z = end.z - newPCR.origin.z;
	newPCR.direction.normalize();
	
	return newPCR;
    }

}

