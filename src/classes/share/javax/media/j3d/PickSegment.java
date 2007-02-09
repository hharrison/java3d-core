/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
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
 * PickSegment is a line segment pick shape.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 */
public final class PickSegment extends PickShape {

    Point3d start;
    Point3d end;

    /** 
     * Constructs an empty PickSegment.
     * The start and end points of the line segment are
     * initialized to (0,0,0).
     */
    public PickSegment() {
	this.start = new Point3d();
	this.end = new Point3d();
    }

    /**
     * Constructs a line segment pick shape from the specified
     * parameters.
     * @param start the start point of the line segment.
     * @param end the end point of the line segment.
     */
    public PickSegment(Point3d start, Point3d end) {
	this.start = new Point3d(start);
	this.end = new Point3d(end);
    }

    /**
     * Sets the parameters of this PickSegment to the specified values.
     * @param start the start point of the line segment.
     * @param end the end point of the line segment.
     */
    public void set(Point3d start, Point3d end) {
	this.start.x = start.x;
	this.start.y = start.y;
	this.start.z = start.z;
	this.end.x = end.x;
	this.end.y = end.y;
	this.end.z = end.z;
    }

    /**
     * Gets the parameters from this PickSegment.
     * @param start the Point3d object into which the start
     * point will be copied.
     * @param end the Point3d object into which the end point
     * will be copied.
     */
    public void get(Point3d start, Point3d end) {
	start.x = this.start.x;
	start.y = this.start.y;
	start.z = this.start.z;
	end.x = this.end.x;
	end.y = this.end.y;
	end.z = this.end.z;
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(start, end, pickPos);
    }


    
    // Only use within J3D.
    // Return a new PickSegment that is the transformed (t3d) of this pickSegment.  
    PickShape transform(Transform3D t3d) {
	PickSegment newPS = new PickSegment(start, end);
	t3d.transform(newPS.start);
	t3d.transform(newPS.end);
	return newPS;
    }

    Point3d getStartPoint() {
	return start;
    }

    int getPickType() {
	return PICKSEGMENT;
    }
}
