/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
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
    final boolean intersect(Bounds bounds, Point4d pickPos) {
	return bounds.intersect(this.bounds, pickPos);
    }
    
    // Only use within J3D.
    // Return a new PickBounds that is the transformed (t3d) of this pickBounds.  
    PickShape transform(Transform3D t3d) {
	// If the bounds is a BoundingBox, then the transformed bounds will
	// get bigger. So this is a potential bug, and we'll have to deal with
	// if there is a complain. 
	Bounds newBds = (Bounds)bounds.clone();
	newBds.transform(t3d);
	PickBounds newPB = new PickBounds(newBds);
	
	return newPB;
    }

    Point3d getStartPoint() {
	return bounds.getCenter();
    }    

    int getPickType() {
	return (bounds != null ? bounds.getPickType() : 
		                 PickShape.PICKUNKNOWN);
    }
}
