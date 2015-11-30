/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package org.jogamp.java3d;

import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point4d;
import org.jogamp.vecmath.Vector3d;

/**
 * PickRay is an infinite ray pick shape.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 */
public final class PickRay extends PickShape {

    Point3d origin;
    Vector3d direction;

    /**
     * Constructs an empty PickRay.  The origin and direction of the
     * ray are initialized to (0,0,0).
     */
    public PickRay() {
	origin = new Point3d();
	direction = new Vector3d();
    }

    /**
     * Constructs an infinite ray pick shape from the specified
     * parameters.
     * @param origin the origin of the ray.
     * @param direction the direction of the ray.
     */
    public PickRay(Point3d origin, Vector3d direction) {
	this.origin = new Point3d(origin);
	this.direction = new Vector3d(direction);
    }


    /**
     * Sets the parameters of this PickRay to the specified values.
     * @param origin the origin of the ray.
     * @param direction the direction of the ray.
     */
    public void set(Point3d origin, Vector3d direction) {
	this.origin.x = origin.x;
	this.origin.y = origin.y;
	this.origin.z = origin.z;
	this.direction.x = direction.x;
	this.direction.y = direction.y;
	this.direction.z = direction.z;
    }

    /**
     * Retrieves the parameters from this PickRay.
     * @param origin the Point3d object into which the origin will be copied.
     * @param direction the Vector3d object into which the direction
     * will be copied.
     */
    public void get(Point3d origin, Vector3d direction) {
	origin.x = this.origin.x;
	origin.y = this.origin.y;
	origin.z = this.origin.z;
	direction.x = this.direction.x;
	direction.y = this.direction.y;
	direction.z = this.direction.z;
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    @Override
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(origin, direction,  pickPos);
    }


    // Only use within J3D.
    // Return a new PickRay that is the transformed (t3d) of this pickRay.
    @Override
    PickShape transform(Transform3D t3d) {

	Point3d end = new Point3d();

	PickRay newPR = new PickRay(origin, direction);

	end.x = origin.x + direction.x;
	end.y = origin.y + direction.y;
	end.z = origin.z + direction.z;

	t3d.transform(newPR.origin);
	t3d.transform(end);

	newPR.direction.x = end.x - newPR.origin.x;
	newPR.direction.y = end.y - newPR.origin.y;
	newPR.direction.z = end.z - newPR.origin.z;
	newPR.direction.normalize();

	return newPR;
    }


    @Override
    Point3d getStartPoint() {
	return origin;
    }

    @Override
    int getPickType() {
	return PICKRAY;
    }
}
