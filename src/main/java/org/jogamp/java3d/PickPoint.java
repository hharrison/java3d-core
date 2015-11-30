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

/**
 * PickPoint is a pick shape defined as a single point.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 * @see PickBounds
 *
 * @deprecated As of Java 3D version 1.4, use PickBounds with a
 * BoundingSphere that has a small radius.
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
    @Override
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(location, pickPos);
    }

    // Only use within J3D.
    // Return a new PickPoint that is the transformed (t3d) of this pickPoint.
    @Override
    PickShape transform(Transform3D t3d) {

	PickPoint newPPt = new PickPoint();

	newPPt.location.x = location.x;
	newPPt.location.y = location.y;
	newPPt.location.z = location.z;

	t3d.transform(newPPt.location);

	return newPPt;
    }

    @Override
    Point3d getStartPoint() {
	return location;
    }

    @Override
    int getPickType() {
	return PICKPOINT;
    }
}
