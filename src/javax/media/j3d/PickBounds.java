/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;

/**
 * PickBounds is a finite pick shape defined with a Bounds object.  It can
 * be used as an argument to the picking methods in BranchGroup and Locale.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 */
public final class PickBounds extends PickShape {

    Bounds bounds;

    /**
     * Constructs an empty PickBounds.  The bounds object is set to null.
     */
    public PickBounds() {
	bounds = null;
    }

    /**
     * Constructs a PickBounds from the specified bounds object.
     * @param boundsObject the bounds of this PickBounds.
     */
    public PickBounds(Bounds boundsObject) {
	bounds = boundsObject;
    }


    /**
     * Sets the bounds object of this PickBounds to the specified object.
     * @param boundsObject the new bounds of this PickBounds.
     */
    public void set(Bounds boundsObject) {
	bounds = boundsObject;
    }

    /**
     * Gets the bounds object from this PickBounds.
     * @return the bounds.
     */
    public Bounds get() {
	return bounds;
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    @Override
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(this.bounds, pickPos);
    }

    // Only use within J3D.
    // Return a new PickBounds that is the transformed (t3d) of this pickBounds.
    @Override
    PickShape transform(Transform3D t3d) {
	// If the bounds is a BoundingBox, then the transformed bounds will
	// get bigger. So this is a potential bug, and we'll have to deal with
	// if there is a complain.
	Bounds newBds = (Bounds)bounds.clone();
	newBds.transform(t3d);
	PickBounds newPB = new PickBounds(newBds);

	return newPB;
    }

    @Override
    Point3d getStartPoint() {
	return bounds.getCenter();
    }

    @Override
    int getPickType() {
	return (bounds != null ? bounds.getPickType() :
		                 PickShape.PICKUNKNOWN);
    }
}
