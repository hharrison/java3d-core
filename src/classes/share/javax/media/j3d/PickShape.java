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

