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

/**
 * PickCone is the abstract base class of all cone pick shapes.
 *
 * @since Java 3D 1.2
 */
public abstract class PickCone extends PickShape {

    Point3d origin;
    Vector3d direction;
    double spreadAngle;

    /**
     * Constructs an empty PickCone.
     * The origin and direction of the cone are
     * initialized to (0,0,0).  The spread angle is initialized
     * to <code>PI/64</code>.
     */
    public PickCone() {
	this.origin = new Point3d();
	this.direction = new Vector3d();
	this.spreadAngle = Math.PI / 64.0;
    }

    /**
     * Gets the origin of this PickCone.
     * @param origin the Point3d object into which the origin will be copied.
     */
    public void getOrigin(Point3d origin) {
	origin.set(this.origin);
    }
    
    /**
     * Gets the direction of this PickCone.
     * @param direction the Vector3d object into which the direction
     * will be copied.
     */
    public void getDirection(Vector3d direction) {
	direction.set(this.direction);
    }


    /**
     * Gets the spread angle of this PickCone.
     * @return the spread angle.
     */
    public double getSpreadAngle() {
	return spreadAngle;
    }

    /**
     * Gets the radius of this PickCone at the specified distance.
     * @param distance the distance from the origin at which we want 
     * the radius of the cone
     * @return the radius at the specified distance
     */
    double getRadius(double distance) {
	return distance * Math.tan (spreadAngle);
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    abstract boolean intersect(Bounds bounds, Point4d pickPos);

    Point3d getStartPoint() {
	return origin;
    }

    int getPickType() {
	return PICKCONE;
    }
}
