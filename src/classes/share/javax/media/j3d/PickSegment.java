/*
 * $RCSfile$
 *
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
