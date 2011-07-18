/*
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
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * PickCylinder is the abstract base class of all cylindrical pick shapes.
 *
 * @since Java 3D 1.2
 */
public abstract class PickCylinder extends PickShape {

    Point3d origin;
    Vector3d direction;
    double radius;

    /**
     * Constructs an empty PickCylinder.
     * The origin of the cylinder is
     * initialized to (0,0,0).  The radius is initialized
     * to 0.
     */
    public PickCylinder() {
	origin = new Point3d();
	direction = new Vector3d();
	radius = 0.0;	
    }

    /**
     * Gets the origin point of this cylinder object.
     * @param origin the Point3d object into which the origin
     * point will be copied
     */
    public void getOrigin(Point3d origin) {
	origin.set(this.origin);
    }

    /**
     * Gets the radius of this cylinder object
     * @return the radius in radians
     */
    public double getRadius() {
	return radius;
    }

    /**
     * Gets the direction of this cylinder.
     * @param direction the Vector3d object into which the direction
     * will be copied
     */
    public void getDirection(Vector3d direction) {
	direction.set(this.direction);
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    abstract boolean intersect(Bounds bounds, Point4d pickPos);

    // This is a duplicate of the same method, declared private inside of 
    // BoundingPolytope
    // XXXX: remove this once the original method is available (public) in
    // BoundingPolytope
    static boolean pointInPolytope(BoundingPolytope ptope, 
				   double x, double y, double z ){
	Vector4d p;
	int i = ptope.planes.length - 1;

	while (i >= 0) {
	    p = ptope.planes[i--];
	    if (( x*p.x + y*p.y + z*p.z + p.w ) > Bounds.EPSILON) {
		return false;
	    }
	}   
	return true;
    }

    Point3d getStartPoint() {
	return origin;
    }

    int getPickType() {
	return PICKCYLINDER;
    }
}
