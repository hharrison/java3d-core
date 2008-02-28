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
import javax.vecmath.Point4d;
import javax.vecmath.Point3d;

/**
 * An abstract class for describing a pick shape that can be used with
 * the BranchGroup and Locale picking methods.
 *
 * @see BranchGroup#pickAll
 * @see Locale#pickAll
 */
public abstract class PickShape {

   // Use for picking
    static final int PICKRAY              = 1;
    static final int PICKSEGMENT          = 2;
    static final int PICKPOINT            = 3;    
    static final int PICKCYLINDER         = 4;    
    static final int PICKCONE             = 5;    
    static final int PICKBOUNDINGBOX      = 6;    
    static final int PICKBOUNDINGSPHERE   = 7;    
    static final int PICKBOUNDINGPOLYTOPE = 8;    
    static final int PICKUNKNOWN          = 9;

    /**
     * Constructs a PickShape object.
     */
    public PickShape() {
    }

    /**
     * Return true if shape intersect with bounds.
     * The point of intersection is stored in pickPos.
     */
    abstract boolean intersect(Bounds bounds, Point4d pickPos); 

    // Only use within J3D.
    // Return a new PickShape that is the transformed (t3d) of this pickShape.
    abstract PickShape transform(Transform3D t3d);

    // Get the start point use to compute the distance
    // with intersect point for this shape.
    abstract Point3d getStartPoint();

    // Return the distance between the original of this
    // pickShape and iPnt
    double distance(Point3d iPnt) {
	Point3d p = getStartPoint();
	double x = iPnt.x - p.x;
	double y = iPnt.y - p.y;
	double z = iPnt.z - p.z;
	return Math.sqrt(x*x + y*y + z*z);
    } 

    // Return one of PickShape type constant define above
    abstract int getPickType();

}

