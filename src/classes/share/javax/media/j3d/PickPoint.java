/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * PickPoint is a pick shape defined as a single point.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 */
public final class PickPoint extends PickShape {

    Point3d location;

    /**
     * Constructs a PickPoint using a default point.
     * The point is initialized to (0,0,0).
     */
    public PickPoint() {
	location = new Point3d();
    }

    /**
     * Constructs a PickPoint from the specified parameter.
     * @param location the pick point.
     */
    public PickPoint(Point3d location) {
	this.location = new Point3d(location);
    }

    /**
     * Sets the position of this PickPoint to the specified value.
     * @param location the new pick point.
     */
    public void set(Point3d location) {
	this.location.x = location.x;
	this.location.y = location.y;
	this.location.z = location.z;
    }

    /**
     * Gets the position of this PickPoint.
     * @param location returns the current pick point.
     */
    public void get(Point3d location) {
	location.x = this.location.x;
	location.y = this.location.y;
	location.z = this.location.z;
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(location, pickPos);
    }

    // Only use within J3D.
    // Return a new PickPoint that is the transformed (t3d) of this pickPoint.  
    PickShape transform(Transform3D t3d) {
	
	PickPoint newPPt = new PickPoint();
	
	newPPt.location.x = location.x;
	newPPt.location.y = location.y;
	newPPt.location.z = location.z;
	
	t3d.transform(newPPt.location);
	
	return newPPt;
    }

    Point3d getStartPoint() {
	return location;
    }    

    int getPickType() {
	return PICKPOINT;
    }
}
