/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.sun.j3d.internal.HashCodeUtil;

/**
 * This class defines a spherical bounding region which is defined by a
 * center point and a radius.
 */

public class BoundingSphere extends Bounds {

    /**
     * The center of the bounding sphere.
     */
    Point3d	center;

    /**
     * The radius of the bounding sphere.
     */
    double	radius;

    Point3d boxVerts[];
    boolean allocBoxVerts = false;

    // reusable temp objects
    private BoundingBox tmpBox = null;
    private BoundingPolytope tmpPolytope = null;

    /**
     * Constructs and initializes a BoundingSphere from a center and radius.
     * @param center the center of the bounding sphere
     * @param radius the radius of the bounding sphere
     */
    public BoundingSphere(Point3d center, double radius) {
	this.center = new Point3d(center);
	this.radius = radius;
	boundId = BOUNDING_SPHERE;
	updateBoundsStates();
    }
    /**
     * Constructs and initializes a BoundingSphere with radius = 1 at 0 0 0.
     */
    public BoundingSphere() {
	boundId = BOUNDING_SPHERE;
	center = new Point3d();
	radius = 1.0;
    }

    /**
     * Constructs and initializes a BoundingSphere from a bounding object.
     * @param boundsObject  a bounds object
     */
    public BoundingSphere(Bounds boundsObject) {
	int i;

	boundId = BOUNDING_SPHERE;
	if (boundsObject == null) {
	    // Negative volume.
	    center = new Point3d();
	    radius = -1.0;
	}
	else if( boundsObject.boundsIsInfinite ) {
	    center = new Point3d();
	    radius = Double.POSITIVE_INFINITY;

	} else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;
	    center = new Point3d();
	    center.x = (box.upper.x+box.lower.x)/2.0;
	    center.y = (box.upper.y+box.lower.y)/2.0;
	    center.z = (box.upper.z+box.lower.z)/2.0;
	    radius = 0.5*(Math.sqrt((box.upper.x-box.lower.x)*
				    (box.upper.x-box.lower.x)+
				    (box.upper.y-box.lower.y)*
				    (box.upper.y-box.lower.y)+
				    (box.upper.z-box.lower.z)*
				    (box.upper.z-box.lower.z)));

	} else if (boundsObject.boundId == BOUNDING_SPHERE) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    center = new Point3d(sphere.center);
	    radius = sphere.radius;

	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    double t,dis,dis_sq,rad_sq,oldc_to_new_c;
	    center = new Point3d();
	    center.x = polytope.centroid.x;
	    center.y = polytope.centroid.y;
	    center.z = polytope.centroid.z;
	    radius = Math.sqrt( (polytope.verts[0].x - center.x)*
				(polytope.verts[0].x - center.x) +
				(polytope.verts[0].y - center.y)*
				(polytope.verts[0].y - center.y) +
				(polytope.verts[0].z - center.z)*
				(polytope.verts[0].z - center.z));

	    for(i=1;i<polytope.nVerts;i++) {
	        rad_sq = radius * radius;

                dis_sq =  (polytope.verts[i].x - center.x)*
		    (polytope.verts[i].x - center.x) +
                    (polytope.verts[i].y - center.y)*
		    (polytope.verts[i].y - center.y) +
                    (polytope.verts[i].z - center.z)*
		    (polytope.verts[i].z - center.z);

		// change sphere so one side passes through the point
		// and other passes through the old sphere
   	        if( dis_sq > rad_sq) {
		    dis = Math.sqrt( dis_sq);
		    radius = (radius + dis)*.5;
		    oldc_to_new_c = dis - radius;
		    t = oldc_to_new_c/dis;
		    center.x = center.x + (polytope.verts[i].x - center.x)*t;
		    center.y = center.y + (polytope.verts[i].y - center.y)*t;
		    center.z = center.z + (polytope.verts[i].z - center.z)*t;
	        }
            }
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere0"));
	}

	updateBoundsStates();
    }

    /**
     * Constructs and initializes a BoundingSphere from an array of bounding objects.
     * @param boundsObjects an array of bounds objects
     */
    public BoundingSphere(Bounds[] boundsObjects) {
	int i=0;
	double dis,t,d1;

	boundId = BOUNDING_SPHERE;
	center = new Point3d();

	if( boundsObjects == null || boundsObjects.length <= 0  ) {
	   // Negative volume.
	    radius = -1.0;
	    updateBoundsStates();
	    return;
	}

	// find first non empty bounds object
	while( boundsObjects[i] == null && i < boundsObjects.length) {
	    i++;
	}

	if( i >= boundsObjects.length ) { // all bounds objects were empty
	   // Negative volume.
	    radius = -1.0;
	    updateBoundsStates();
	    return;
	}

	this.set(boundsObjects[i++]);
	if(boundsIsInfinite)
	    return;

	for(;i<boundsObjects.length;i++) {
	    if( boundsObjects[i] == null ); // do nothing
	    else if( boundsObjects[i].boundsIsEmpty); // do nothing
	    else if( boundsObjects[i].boundsIsInfinite ) {
		radius = Double.POSITIVE_INFINITY;
		break;  // We're done.
	    }
	    else if( boundsObjects[i].boundId == BOUNDING_BOX){
		BoundingBox b = (BoundingBox)boundsObjects[i];
		if( !allocBoxVerts){
		    boxVerts = new Point3d[8];
		    for(int j=0;j<8;j++)boxVerts[j] = new Point3d();
		    allocBoxVerts = true;
		}
		boxVerts[0].set(b.lower.x, b.lower.y, b.lower.z );
		boxVerts[1].set(b.lower.x, b.upper.y, b.lower.z );
		boxVerts[2].set(b.upper.x, b.lower.y, b.lower.z );
		boxVerts[3].set(b.upper.x, b.upper.y, b.lower.z );
		boxVerts[4].set(b.lower.x, b.lower.y, b.upper.z );
		boxVerts[5].set(b.lower.x, b.upper.y, b.upper.z );
		boxVerts[6].set(b.upper.x, b.lower.y, b.upper.z );
		boxVerts[7].set(b.upper.x, b.upper.y, b.upper.z );
		this.combine(boxVerts);
	    }
	    else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		dis = Math.sqrt( (center.x - sphere.center.x)*
				 (center.x - sphere.center.x) +
				 (center.y - sphere.center.y)*
				 (center.y - sphere.center.y) +
				 (center.z - sphere.center.z)*
				 (center.z - sphere.center.z) );
		if( radius > sphere.radius) {
		    if( (dis+sphere.radius) > radius) {
			d1 = .5*(radius-sphere.radius+dis);
			t = d1/dis;
			radius = d1+sphere.radius;
			center.x = sphere.center.x + (center.x-sphere.center.x)*t;
			center.y = sphere.center.y + (center.y-sphere.center.y)*t;
			center.z = sphere.center.z + (center.z-sphere.center.z)*t;
		    }
		}else {
		    if( (dis+radius) <= sphere.radius) {
			center.x = sphere.center.x;
			center.y = sphere.center.y;
			center.z = sphere.center.z;
			radius = sphere.radius;
		    }else {
			d1 = .5*(sphere.radius-radius+dis);
			t = d1/dis;
			radius = d1+radius;
			center.x = center.x + (sphere.center.x-center.x)*t;
			center.y = center.y + (sphere.center.y-center.y)*t;
			center.z = center.z + (sphere.center.z-center.z)*t;
		    }
		}
	    }
	    else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		this.combine(polytope.verts);

	    }
	    else {
		if( boundsObjects[i] != null )
		    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere0"));
	    }
	}
	updateBoundsStates();
    }

    /**
     * Returns the radius of this bounding sphere as a double.
     * @return the radius of the bounding sphere
     */
    public double getRadius() {
	return radius;
    }

    /**
     * Sets the radius of this bounding sphere from a double.
     * @param r the new radius for the bounding sphere
     */
    public void setRadius(double r) {
	radius = r;
	updateBoundsStates();
    }

    /**
     * Returns the position of this bounding sphere as a point.
     * @param center a Point to receive the center of the bounding sphere

     */
    public void getCenter(Point3d center) {
	center.x = this.center.x;
	center.y = this.center.y;
	center.z = this.center.z;
    }

    /**
     * Sets the position of this bounding sphere from a point.
     * @param center a Point defining the new center of the bounding sphere
     */
    public void setCenter(Point3d center) {
	this.center.x = center.x;
	this.center.y = center.y;
	this.center.z = center.z;
	checkBoundsIsNaN();
    }

    /**
     * Sets the value of this BoundingSphere.
     * @param boundsObject another bounds object
     */
    public void set(Bounds  boundsObject){
	int i;

	if ((boundsObject == null) || boundsObject.boundsIsEmpty) {
	    center.x = 0.0;
	    center.y = 0.0;
	    center.z = 0.0;
	    radius = -1.0;
	} else if( boundsObject.boundsIsInfinite ) {
	    center.x = 0.0;
	    center.y = 0.0;
	    center.z = 0.0;
	    radius = Double.POSITIVE_INFINITY;
	} else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;
	    center.x = (box.upper.x + box.lower.x )/2.0;
	    center.y = (box.upper.y + box.lower.y )/2.0;
	    center.z = (box.upper.z + box.lower.z )/2.0;
	    radius = 0.5*Math.sqrt((box.upper.x-box.lower.x)*
				   (box.upper.x-box.lower.x)+
				   (box.upper.y-box.lower.y)*
				   (box.upper.y-box.lower.y)+
				   (box.upper.z-box.lower.z)*
				   (box.upper.z-box.lower.z));
	} else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    radius = sphere.radius;
	    center.x = sphere.center.x;
	    center.y = sphere.center.y;
	    center.z = sphere.center.z;
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    double t,dis,dis_sq,rad_sq,oldc_to_new_c;
	    center.x = polytope.centroid.x;
	    center.y = polytope.centroid.y;
	    center.z = polytope.centroid.z;
	    radius = Math.sqrt((polytope.verts[0].x - center.x)*
			       (polytope.verts[0].x - center.x) +
			       (polytope.verts[0].y - center.y)*
			       (polytope.verts[0].y - center.y) +
			       (polytope.verts[0].z - center.z)*
			       (polytope.verts[0].z - center.z));

	    for(i=1;i<polytope.nVerts;i++) {
	        rad_sq = radius * radius;

                dis_sq =  (polytope.verts[i].x - center.x)*
		    (polytope.verts[i].x - center.x) +
                    (polytope.verts[i].y - center.y)*
		    (polytope.verts[i].y - center.y) +
                    (polytope.verts[i].z - center.z)*
		    (polytope.verts[i].z - center.z);

		// change sphere so one side passes through the point
		// and other passes through the old sphere
   	        if( dis_sq > rad_sq) { // point is outside sphere
		    dis = Math.sqrt( dis_sq);
		    radius = (radius + dis)*.5;
		    oldc_to_new_c = dis - radius;
		    t = oldc_to_new_c/dis;
		    center.x = center.x + (polytope.verts[i].x - center.x)*t;
		    center.y = center.y + (polytope.verts[i].y - center.y)*t;
		    center.z = center.z + (polytope.verts[i].z - center.z)*t;
	        }
            }
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere2"));
	}
	updateBoundsStates();
    }

    /**
     * Creates a copy of the bounding sphere.
     * @return a BoundingSphere
     */
    public Object clone() {
	return new BoundingSphere(this.center, this.radius);
    }


    /**
     * Indicates whether the specified <code>bounds</code> object is
     * equal to this BoundingSphere object.  They are equal if the
     * specified <code>bounds</code> object is an instance of
     * BoundingSphere and all of the data
     * members of <code>bounds</code> are equal to the corresponding
     * data members in this BoundingSphere.
     * @param bounds the object with which the comparison is made.
     * @return true if this BoundingSphere is equal to <code>bounds</code>;
     * otherwise false
     *
     * @since Java 3D 1.2
     */
    public boolean equals(Object bounds) {
	try {
	    BoundingSphere sphere = (BoundingSphere)bounds;
	    return (center.equals(sphere.center) &&
		    radius == sphere.radius);
	}
	catch (NullPointerException e) {
	    return false;
	}
        catch (ClassCastException e) {
	    return false;
	}
    }


    /**
     * Returns a hash code value for this BoundingSphere object
     * based on the data values in this object.  Two different
     * BoundingSphere objects with identical data values (i.e.,
     * BoundingSphere.equals returns true) will return the same hash
     * code value.  Two BoundingSphere objects with different data
     * members may return the same hash code value, although this is
     * not likely.
     * @return a hash code value for this BoundingSphere object.
     *
     * @since Java 3D 1.2
     */
    public int hashCode() {
	long bits = 1L;
	bits = 31L * bits + HashCodeUtil.doubleToLongBits(radius);
	bits = 31L * bits + HashCodeUtil.doubleToLongBits(center.x);
	bits = 31L * bits + HashCodeUtil.doubleToLongBits(center.y);
	bits = 31L * bits + HashCodeUtil.doubleToLongBits(center.z);
	return (int) (bits ^ (bits >> 32));
    }


    /**
     * Combines this bounding sphere with a bounding object so that the
     * resulting bounding sphere encloses the original bounding sphere and the
     * given bounds object.
     * @param boundsObject another bounds object
     */
    public void combine(Bounds boundsObject) {
        double t,dis,d1,u,l,x,y,z,oldc_to_new_c;
        BoundingSphere sphere;

	if((boundsObject == null) || (boundsObject.boundsIsEmpty)
	   || (boundsIsInfinite))
	    return;

	if((boundsIsEmpty) || (boundsObject.boundsIsInfinite)) {
	    this.set(boundsObject);
	    return;
	}


	if( boundsObject.boundId == BOUNDING_BOX){
            BoundingBox b = (BoundingBox)boundsObject;

	    //       start with point furthest from sphere
	    u = b.upper.x-center.x;
	    l = b.lower.x-center.x;
	    if( u*u > l*l)
                x = b.upper.x;
	    else
                x = b.lower.x;

	    u = b.upper.y-center.y;
	    l = b.lower.y-center.y;
	    if( u*u > l*l)
                y = b.upper.y;
	    else
                y = b.lower.y;

	    u = b.upper.z-center.z;
	    l = b.lower.z-center.z;
	    if( u*u > l*l)
                z = b.upper.z;
	    else
                z = b.lower.z;

	    dis = Math.sqrt( (x - center.x)*(x - center.x) +
			     (y - center.y)*(y - center.y) +
			     (z - center.z)*(z - center.z) );

	    if( dis > radius) {
                radius = (dis + radius)*.5;
                oldc_to_new_c = dis - radius;
                center.x = (radius*center.x + oldc_to_new_c*x)/dis;
                center.y = (radius*center.y + oldc_to_new_c*y)/dis;
                center.z = (radius*center.z + oldc_to_new_c*z)/dis;
                combinePoint( b.upper.x, b.upper.y, b.upper.z);
                combinePoint( b.upper.x, b.upper.y, b.lower.z);
                combinePoint( b.upper.x, b.lower.y, b.upper.z);
                combinePoint( b.upper.x, b.lower.y, b.lower.z);
                combinePoint( b.lower.x, b.upper.y, b.upper.z);
                combinePoint( b.lower.x, b.upper.y, b.lower.z);
                combinePoint( b.lower.x, b.lower.y, b.upper.z);
                combinePoint( b.lower.x, b.lower.y, b.lower.z);
	    }
	} else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    sphere = (BoundingSphere)boundsObject;
	    dis = Math.sqrt( (center.x - sphere.center.x)*
			     (center.x - sphere.center.x) +
			     (center.y - sphere.center.y)*
			     (center.y - sphere.center.y) +
			     (center.z - sphere.center.z)*
			     (center.z - sphere.center.z) );
	    if( radius > sphere.radius) {
		if( (dis+sphere.radius) > radius) {
		    d1 = .5*(radius-sphere.radius+dis);
		    t = d1/dis;
		    radius = d1+sphere.radius;
		    center.x = sphere.center.x + (center.x-sphere.center.x)*t;
		    center.y = sphere.center.y + (center.y-sphere.center.y)*t;
		    center.z = sphere.center.z + (center.z-sphere.center.z)*t;
		}
	    }else {
		if( (dis+radius) <= sphere.radius) {
		    center.x = sphere.center.x;
		    center.y = sphere.center.y;
		    center.z = sphere.center.z;
		    radius = sphere.radius;
		}else {
		    d1 = .5*(sphere.radius-radius+dis);
		    t = d1/dis;
		    radius = d1+radius;
		    center.x = center.x + (sphere.center.x-center.x)*t;
		    center.y = center.y + (sphere.center.y-center.y)*t;
		    center.z = center.z + (sphere.center.z-center.z)*t;
		}
	    }

	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    this.combine(polytope.verts);
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere3"));
	}
	updateBoundsStates();
    }

    private void combinePoint( double x, double y, double z) {
	double dis,oldc_to_new_c;
	dis = Math.sqrt( (x - center.x)*(x - center.x) +
			 (y - center.y)*(y - center.y) +
			 (z - center.z)*(z - center.z) );

	if( dis > radius) {
	    radius = (dis + radius)*.5;
	    oldc_to_new_c = dis - radius;
	    center.x = (radius*center.x + oldc_to_new_c*x)/dis;
	    center.y = (radius*center.y + oldc_to_new_c*y)/dis;
	    center.z = (radius*center.z + oldc_to_new_c*z)/dis;
	}
    }

    /**
     * Combines this bounding sphere with an array of bounding objects so that the
     * resulting bounding sphere encloses the original bounding sphere and the
     * given array of bounds object.
     * @param boundsObjects an array of bounds objects
     */
    public void combine(Bounds[] boundsObjects) {
	BoundingSphere sphere;
	BoundingBox b;
	BoundingPolytope polytope;
	double t,dis,d1,u,l,x,y,z,oldc_to_new_c;
	int i=0;


	if((boundsObjects == null) || (boundsObjects.length <= 0)
	   || (boundsIsInfinite))
	    return;

	// find first non empty bounds object
	while((i<boundsObjects.length) &&
	      ((boundsObjects[i] == null) || boundsObjects[i].boundsIsEmpty)) {
	    i++;
	}
	if( i >= boundsObjects.length)
	    return;   // no non empty bounds so do not modify current bounds

	if( boundsIsEmpty)
	    this.set(boundsObjects[i++]);

	if(boundsIsInfinite)
	    return;

	for(;i<boundsObjects.length;i++) {
	    if( boundsObjects[i] == null );  // do nothing
	    else if( boundsObjects[i].boundsIsEmpty); // do nothing
	    else if( boundsObjects[i].boundsIsInfinite ) {
		center.x = 0.0;
		center.y = 0.0;
		center.z = 0.0;
		radius = Double.POSITIVE_INFINITY;
		break;  // We're done.
	    } else if( boundsObjects[i].boundId == BOUNDING_BOX){
		b = (BoundingBox)boundsObjects[i];

		//       start with point furthest from sphere
		u = b.upper.x-center.x;
		l = b.lower.x-center.x;
		if( u*u > l*l)
		    x = b.upper.x;
		else
		    x = b.lower.x;

		u = b.upper.y-center.y;
		l = b.lower.y-center.y;
		if( u*u > l*l)
		    y = b.upper.y;
		else
		    y = b.lower.y;

		u = b.upper.z-center.z;
		l = b.lower.z-center.z;
		if( u*u > l*l)
		    z = b.upper.z;
		else
		    z = b.lower.z;

		dis = Math.sqrt( (x - center.x)*(x - center.x) +
				 (y - center.y)*(y - center.y) +
				 (z - center.z)*(z - center.z) );

		if( dis > radius) {
		    radius = (dis + radius)*.5;
		    oldc_to_new_c = dis - radius;
		    center.x = (radius*center.x + oldc_to_new_c*x)/dis;
		    center.y = (radius*center.y + oldc_to_new_c*y)/dis;
		    center.z = (radius*center.z + oldc_to_new_c*z)/dis;
		    combinePoint( b.upper.x, b.upper.y, b.upper.z);
		    combinePoint( b.upper.x, b.upper.y, b.lower.z);
		    combinePoint( b.upper.x, b.lower.y, b.upper.z);
		    combinePoint( b.upper.x, b.lower.y, b.lower.z);
		    combinePoint( b.lower.x, b.upper.y, b.upper.z);
		    combinePoint( b.lower.x, b.upper.y, b.lower.z);
		    combinePoint( b.lower.x, b.lower.y, b.upper.z);
		    combinePoint( b.lower.x, b.lower.y, b.lower.z);
		}
	    } else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		sphere = (BoundingSphere)boundsObjects[i];
		dis = Math.sqrt( (center.x - sphere.center.x)*
				 (center.x - sphere.center.x) +
				 (center.y - sphere.center.y)*
				 (center.y - sphere.center.y) +
				 (center.z - sphere.center.z)*
				 (center.z - sphere.center.z) );
		if( radius > sphere.radius) {
		    if( (dis+sphere.radius) > radius) {
			d1 = .5*(radius-sphere.radius+dis);
			t = d1/dis;
			radius = d1+sphere.radius;
			center.x = sphere.center.x + (center.x-sphere.center.x)*t;
			center.y = sphere.center.y + (center.y-sphere.center.y)*t;
			center.z = sphere.center.z + (center.z-sphere.center.z)*t;
		    }
		}else {
		    if( (dis+radius) <= sphere.radius) {
			center.x = sphere.center.x;
			center.y = sphere.center.y;
			center.z = sphere.center.z;
			radius = sphere.radius;
		    }else {
			d1 = .5*(sphere.radius-radius+dis);
			t = d1/dis;
			radius = d1+radius;
			center.x = center.x + (sphere.center.x-center.x)*t;
			center.y = center.y + (sphere.center.y-center.y)*t;
			center.z = center.z + (sphere.center.z-center.z)*t;
		    }
		}
	    } else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		polytope = (BoundingPolytope)boundsObjects[i];
		this.combine(polytope.verts);
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere4"));
	    }
	}

	updateBoundsStates();
    }

    /**
     * Combines this bounding sphere with a point.
     * @param point a 3D point in space
     */
    public void combine(Point3d point) {
	double t,dis,oldc_to_new_c;

	if( boundsIsInfinite) {
	    return;
	}

	if( boundsIsEmpty) {
	    radius = 0.0;
	    center.x = point.x;
	    center.y = point.y;
	    center.z = point.z;
	} else {
	    dis = Math.sqrt( (point.x - center.x)*(point.x - center.x) +
			     (point.y - center.y)*(point.y - center.y) +
			     (point.z - center.z)*(point.z - center.z) );

	    if( dis > radius) {
	        radius = (dis + radius)*.5;
      	        oldc_to_new_c = dis - radius;
                center.x = (radius*center.x + oldc_to_new_c*point.x)/dis;
                center.y = (radius*center.y + oldc_to_new_c*point.y)/dis;
                center.z = (radius*center.z + oldc_to_new_c*point.z)/dis;
	    }
	}

	updateBoundsStates();
    }

    /**
     * Combines this bounding sphere with an array of points.
     * @param points an array of 3D points in space
     */
    public void combine(Point3d[] points) {
	int i;
	double dis,dis_sq,rad_sq,oldc_to_new_c;

	if( boundsIsInfinite) {
	    return;
	}

	if( boundsIsEmpty ) {
	    center.x = points[0].x;
	    center.y = points[0].y;
	    center.z = points[0].z;
	    radius = 0.0;
	}

	for(i=0;i<points.length;i++) {
	    rad_sq = radius * radius;
	    dis_sq =  (points[i].x - center.x)*(points[i].x - center.x) +
		(points[i].y - center.y)*(points[i].y - center.y) +
		(points[i].z - center.z)*(points[i].z - center.z);

	    // change sphere so one side passes through the point and
	    // other passes through the old sphere
	    if( dis_sq > rad_sq) {
		dis = Math.sqrt( dis_sq);
		radius = (radius + dis)*.5;
		oldc_to_new_c = dis - radius;
                center.x = (radius*center.x + oldc_to_new_c*points[i].x)/dis;
                center.y = (radius*center.y + oldc_to_new_c*points[i].y)/dis;
                center.z = (radius*center.z + oldc_to_new_c*points[i].z)/dis;
	    }
	}

	updateBoundsStates();
    }


    /**
     * Modifies the bounding sphere so that it bounds the volume
     * generated by transforming the given bounding object.
     * @param boundsObject the bounding object to be transformed
     * @param matrix a transformation matrix
     */
    public void transform( Bounds boundsObject, Transform3D matrix) {
	double scale;

	if( boundsObject == null || boundsObject.boundsIsEmpty)  {
	    // Negative volume.
	    center.x = center.y = center.z = 0.0;
	    radius = -1.0;
	    updateBoundsStates();
	    return;
	}

	if(boundsObject.boundsIsInfinite) {
	    center.x = center.y = center.z = 0.0;
	    radius = Double.POSITIVE_INFINITY;
	    updateBoundsStates();
	    return;
	}

	if( boundsObject.boundId == BOUNDING_BOX){
	    if (tmpBox == null) {
                tmpBox = new BoundingBox( (BoundingBox)boundsObject);
	    } else {
                tmpBox.set((BoundingBox)boundsObject);
	    }
            tmpBox.transform(matrix);
            this.set(tmpBox);
	}else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    matrix.transform(((BoundingSphere)boundsObject).center, this.center);
	    // A very simple radius scale.
	    scale = matrix.getDistanceScale();
	    this.radius = ((BoundingSphere)boundsObject).radius * scale;
	    if (Double.isNaN(radius)) {
		// Negative volume.
		center.x = center.y = center.z = 0.0;
		radius = -1.0;
		updateBoundsStates();
		return;
	    }
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    if (tmpPolytope == null) {
            	tmpPolytope = new BoundingPolytope((BoundingPolytope)boundsObject);
	    } else {
            	tmpPolytope.set((BoundingPolytope)boundsObject);
	    }
            tmpPolytope.transform(matrix);
            this.set(tmpPolytope);
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere5"));
	}
    }

    /**
     * Transforms this bounding sphere by the given matrix.
     */
    public void transform( Transform3D trans) {
	double scale;

	if(boundsIsInfinite)
	    return;

	trans.transform(center);
	scale = trans.getDistanceScale();
	radius = radius * scale;
	if (Double.isNaN(radius)) {
	    // Negative volume.
	    center.x = center.y = center.z = 0.0;
	    radius = -1.0;
	    updateBoundsStates();
	    return;
	}
    }

    /**
     * Test for intersection with a ray
     * @param origin the starting point of the ray
     * @param direction the direction of the ray
     * @param position3 a point defining the location of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    boolean intersect(Point3d origin, Vector3d direction, Point4d position ) {

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	    position.x = origin.x;
	    position.y = origin.y;
	    position.z = origin.z;
	    position.w = 0.0;
	    return true;
	}

	double l2oc,rad2,tca,t2hc,mag,t,invMag;
	Vector3d dir = new Vector3d();  // normalized direction of ray
	Point3d oc  = new Point3d();  // vector from sphere center to ray origin

	oc.x = center.x - origin.x;
	oc.y = center.y - origin.y;
	oc.z = center.z - origin.z;

	l2oc = oc.x*oc.x + oc.y*oc.y + oc.z*oc.z; // center to origin squared

	rad2 = radius*radius;
	if( l2oc < rad2 ){
	    //      System.err.println("ray origin inside sphere" );
	    return true;   // ray origin inside sphere
	}

	invMag = 1.0/Math.sqrt(direction.x*direction.x +
			       direction.y*direction.y +
			       direction.z*direction.z);
	dir.x = direction.x*invMag;
	dir.y = direction.y*invMag;
	dir.z = direction.z*invMag;
	tca = oc.x*dir.x + oc.y*dir.y + oc.z*dir.z;

	if( tca <= 0.0 ) {
	    //      System.err.println("ray points away from sphere" );
	    return false;  // ray points away from sphere
	}

	t2hc = rad2 - l2oc + tca*tca;

	if( t2hc > 0.0 ){
	    t = tca - Math.sqrt(t2hc);
	    //      System.err.println("ray  hits sphere:"+this.toString()+" t="+t+" direction="+dir );
	    position.x = origin.x + dir.x*t;
	    position.y = origin.y + dir.y*t;
	    position.z = origin.z + dir.z*t;
	    position.w = t;
	    return true;   // ray hits sphere
	}else {
	    //      System.err.println("ray does not hit sphere" );
	    return false;
	}

    }

    /**
     * Test for intersection with a point
     * @param point the pick point
     * @param position a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    boolean intersect(Point3d point,  Point4d position ) {
	double x,y,z,dist;

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	   position.x = point.x;
	   position.y = point.y;
	   position.z = point.z;
	   position.w = 0.0;
	   return true;
	}

	x = point.x - center.x;
	y = point.y - center.y;
	z = point.z - center.z;

	dist = x*x + y*y + z*z;
	if( dist > radius*radius)
	    return false;
	else {
	    position.x = point.x;
	    position.y = point.y;
	    position.z = point.z;
	    position.w = Math.sqrt(dist);
	    return true;
	}

    }

    /**
     * Test for intersection with a segment
     * @param start a point defining  the start of the line segment
     * @param end a point defining the end of the line segment
     * @param position a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    boolean intersect( Point3d start, Point3d end, Point4d position ) {

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	    position.x = start.x;
	    position.y = start.y;
	    position.z = start.z;
	    position.w = 0.0;
	    return true;
	}

	double l2oc,rad2,tca,t2hc,mag,invMag,t;
	Vector3d dir = new Vector3d();  // normalized direction of ray
	Point3d oc  = new Point3d();  // vector from sphere center to ray origin
	Vector3d direction = new Vector3d();

	oc.x = center.x - start.x;
	oc.y = center.y - start.y;
	oc.z = center.z - start.z;
	direction.x = end.x - start.x;
	direction.y = end.y - start.y;
	direction.z = end.z - start.z;
	invMag = 1.0/Math.sqrt( direction.x*direction.x +
				direction.y*direction.y +
				direction.z*direction.z);
	dir.x = direction.x*invMag;
	dir.y = direction.y*invMag;
	dir.z = direction.z*invMag;


	l2oc = oc.x*oc.x + oc.y*oc.y + oc.z*oc.z; // center to origin squared

	rad2 = radius*radius;
	if( l2oc < rad2 ){
	    //      System.err.println("ray origin inside sphere" );
	    return true;   // ray origin inside sphere
	}

	tca = oc.x*dir.x + oc.y*dir.y + oc.z*dir.z;

	if( tca <= 0.0 ) {
	    //      System.err.println("ray points away from sphere" );
	    return false;  // ray points away from sphere
	}

	t2hc = rad2 - l2oc + tca*tca;

	if( t2hc > 0.0 ){
	    t = tca - Math.sqrt(t2hc);
	    if( t*t <= ((end.x-start.x)*(end.x-start.x)+
			(end.y-start.y)*(end.y-start.y)+
			(end.z-start.z)*(end.z-start.z))){

		position.x = start.x + dir.x*t;
		position.y = start.y + dir.x*t;
		position.z = start.z + dir.x*t;
		position.w = t;
		return true;   // segment hits sphere
	    }
	}
	return false;
    }

    /**
     * Test for intersection with a ray.
     * @param origin the starting point of the ray
     * @param direction the direction of the ray
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Point3d origin, Vector3d direction ) {

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	    return true;
	}

	double l2oc,rad2,tca,t2hc,mag;
	Vector3d dir = new Vector3d();  // normalized direction of ray
	Point3d oc  = new Point3d();  // vector from sphere center to ray origin

	oc.x = center.x - origin.x;
	oc.y = center.y - origin.y;
	oc.z = center.z - origin.z;

	l2oc = oc.x*oc.x + oc.y*oc.y + oc.z*oc.z; // center to origin squared

	rad2 = radius*radius;
	if( l2oc < rad2 ){
	    //	System.err.println("ray origin inside sphere" );
	    return true;   // ray origin inside sphere
	}

	mag = Math.sqrt(direction.x*direction.x +
			direction.y*direction.y +
			direction.z*direction.z);
	dir.x = direction.x/mag;
	dir.y = direction.y/mag;
	dir.z = direction.z/mag;
	tca = oc.x*dir.x + oc.y*dir.y + oc.z*dir.z;

	if( tca <= 0.0 ) {
	    //	System.err.println("ray points away from sphere" );
	    return false;  // ray points away from sphere
	}

	t2hc = rad2 - l2oc + tca*tca;

	if( t2hc > 0.0 ){
	    //	System.err.println("ray hits sphere" );
	    return true;   // ray hits sphere
	}else {
	    //	System.err.println("ray does not hit sphere" );
	    return false;
	}
    }


    /**
     *	Returns the position of the intersect point if the ray intersects with
     * the sphere.
     *
     */
    boolean intersect(Point3d origin, Vector3d direction, Point3d intersectPoint ) {

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	  intersectPoint.x = origin.x;
	  intersectPoint.y = origin.y;
	  intersectPoint.z = origin.z;
	  return true;
	}

	double l2oc,rad2,tca,t2hc,mag,t;
	Point3d dir = new Point3d();  // normalized direction of ray
	Point3d oc  = new Point3d();  // vector from sphere center to ray origin

	oc.x = center.x - origin.x;   // XXXX: check if this method is still needed
	oc.y = center.y - origin.y;
	oc.z = center.z - origin.z;

	l2oc = oc.x*oc.x + oc.y*oc.y + oc.z*oc.z; // center to origin squared

	rad2 = radius*radius;
	if( l2oc < rad2 ){
	    //	System.err.println("ray origin inside sphere" );
	    return true;   // ray origin inside sphere
	}

	mag = Math.sqrt(direction.x*direction.x +
			direction.y*direction.y +
			direction.z*direction.z);
	dir.x = direction.x/mag;
	dir.y = direction.y/mag;
	dir.z = direction.z/mag;
	tca = oc.x*dir.x + oc.y*dir.y + oc.z*dir.z;

	if( tca <= 0.0 ) {
	    //	System.err.println("ray points away from sphere" );
	    return false;  // ray points away from sphere
	}

	t2hc = rad2 - l2oc + tca*tca;

	if( t2hc > 0.0 ){
	    t = tca - Math.sqrt(t2hc);
	    intersectPoint.x = origin.x + direction.x*t;
	    intersectPoint.y = origin.y + direction.y*t;
	    intersectPoint.z = origin.z + direction.z*t;
	    //	System.err.println("ray hits sphere" );
	    return true;   // ray hits sphere
	}else {
	    //	System.err.println("ray does not hit sphere" );
	    return false;
	}
    }


    /**
     * Test for intersection with a point.
     * @param point a point defining a position in 3-space
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Point3d point ) {
	double x,y,z,dist;

	if( boundsIsEmpty ) {
	    return false;
	}
	if( boundsIsInfinite ) {
	    return true;
	}

	x = point.x - center.x;
	y = point.y - center.y;
	z = point.z - center.z;

	dist = x*x + y*y + z*z;
	if( dist > radius*radius)
	    return false;
	else
	    return true;

    }

    /**
     * Tests whether the bounding sphere is empty.  A bounding sphere is
     * empty if it is null (either by construction or as the result of
     * a null intersection) or if its volume is negative.  A bounding sphere
     * with a volume of zero is <i>not</i> empty.
     * @return true if the bounding sphere is empty;
     * otherwise, it returns false
     */
    public boolean isEmpty() {
	return boundsIsEmpty;
    }

    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @return true or false indicating if an intersection occured
     */
    boolean intersect(Bounds boundsObject, Point4d position) {
	return intersect(boundsObject);
    }

    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds boundsObject) {
	double distsq, radsq;
	BoundingSphere sphere;
	boolean intersect;

	if( boundsObject == null ) {
	    return false;
	}

        if( boundsIsEmpty || boundsObject.boundsIsEmpty ) {
	    return false;
        }

	if( boundsIsInfinite || boundsObject.boundsIsInfinite ) {
	    return true;
	}

	if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;
	    double dis = 0.0;
	    double rad_sq = radius*radius;

	    // find the corner closest to the center of sphere

	    if( center.x < box.lower.x )
		dis = (center.x-box.lower.x)*(center.x-box.lower.x);
	    else
		if( center.x > box.upper.x )
		    dis = (center.x-box.upper.x)*(center.x-box.upper.x);

	    if( center.y < box.lower.y )
		dis += (center.y-box.lower.y)*(center.y-box.lower.y);
	    else
		if( center.y > box.upper.y )
		    dis += (center.y-box.upper.y)*(center.y-box.upper.y);

	    if( center.z < box.lower.z )
		dis += (center.z-box.lower.z)*(center.z-box.lower.z);
	    else
		if( center.z > box.upper.z )
		    dis += (center.z-box.upper.z)*(center.z-box.upper.z);

	    return ( dis <= rad_sq );
	} else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    sphere = (BoundingSphere)boundsObject;
	    radsq = radius + sphere.radius;
	    radsq *= radsq;
	    distsq = center.distanceSquared(sphere.center);
	    return (distsq <= radsq);
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    return intersect_ptope_sphere( (BoundingPolytope)boundsObject, this);
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere6"));
	}
    }

    /**
     * Test for intersection with another bounds object.
     * @param boundsObjects an array of bounding objects
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds[] boundsObjects) {
	double distsq, radsq;
	BoundingSphere sphere;
	int i;

	if( boundsObjects == null || boundsObjects.length <= 0  )  {
	    return false;
	}

	if( boundsIsEmpty ) {
	    return false;
	}

	for(i = 0; i < boundsObjects.length; i++){
	    if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty);
	    else if( boundsIsInfinite || boundsObjects[i].boundsIsInfinite ) {
		return true; // We're done here.
	    } else if( boundsObjects[i].boundId == BOUNDING_BOX){
		if( this.intersect( boundsObjects[i])) return true;
	    } else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		sphere = (BoundingSphere)boundsObjects[i];
		radsq = radius + sphere.radius;
		radsq *= radsq;
		distsq = center.distanceSquared(sphere.center);
		if (distsq <= radsq) {
		    return true;
		}
	    } else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		if( this.intersect( boundsObjects[i])) return true;
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere7"));
	    }
	}

	return false;

    }

    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @param newBoundSphere the new bounding sphere which is the intersection of
     *      the boundsObject and this BoundingSphere
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds boundsObject, BoundingSphere newBoundSphere) {

	if((boundsObject == null ) || boundsIsEmpty || boundsObject.boundsIsEmpty) {
	    // Negative volume.
	    newBoundSphere.center.x = newBoundSphere.center.y =
		newBoundSphere.center.z = 0.0;
	    newBoundSphere.radius = -1.0;
	    newBoundSphere.updateBoundsStates();
	    return false;
	}

	if(boundsIsInfinite && (!boundsObject.boundsIsInfinite)) {
	    newBoundSphere.set(boundsObject);
	    return true;
	}
	else if((!boundsIsInfinite) && boundsObject.boundsIsInfinite) {
	    newBoundSphere.set(this);
	    return true;
	}
	else if(boundsIsInfinite && boundsObject.boundsIsInfinite) {
	    newBoundSphere.set(this);
	    return true;
	} else if(boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox tbox =  new BoundingBox();
	    BoundingBox box = (BoundingBox)boundsObject;
	    if( this.intersect( box) ){
		BoundingBox sbox = new BoundingBox( this ); // convert sphere to box
		sbox.intersect(box, tbox);  // insersect two boxes
		newBoundSphere.set( tbox ); // set sphere to the intersection of 2 boxes
		return true;
	    } else {
		// Negative volume.
		newBoundSphere.center.x = newBoundSphere.center.y =
		    newBoundSphere.center.z = 0.0;
		newBoundSphere.radius = -1.0;
		newBoundSphere.updateBoundsStates();
		return false;
	    }
	} else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    double dis,t,d2;
	    boolean status;
	    dis = Math.sqrt( (center.x-sphere.center.x)*(center.x-sphere.center.x) +
			     (center.y-sphere.center.y)*(center.y-sphere.center.y) +
			     (center.z-sphere.center.z)*(center.z-sphere.center.z) );
	    if ( dis > radius+sphere.radius) {
		// Negative volume.
		newBoundSphere.center.x = newBoundSphere.center.y =
		    newBoundSphere.center.z = 0.0;
		newBoundSphere.radius = -1.0;
		status = false;
	    } else if( dis+radius <= sphere.radius ) { // this sphere is contained within boundsObject
		newBoundSphere.center.x = center.x;
		newBoundSphere.center.y = center.y;
		newBoundSphere.center.z = center.z;
		newBoundSphere.radius = radius;
		status = true;
	    } else if( dis+sphere.radius <= radius ) { // boundsObject is containted within this sphere
		newBoundSphere.center.x = sphere.center.x;
		newBoundSphere.center.y = sphere.center.y;
		newBoundSphere.center.z = sphere.center.z;
		newBoundSphere.radius = sphere.radius;
		status = true;
	    } else  {
		// distance from this center to center of overlapped volume
		d2 = (dis*dis + radius*radius - sphere.radius*sphere.radius)/(2.0*dis);
		newBoundSphere.radius = Math.sqrt( radius*radius - d2*d2);
		t = d2/dis;
		newBoundSphere.center.x = center.x + (sphere.center.x - center.x)*t;
		newBoundSphere.center.y = center.y + (sphere.center.y - center.y)*t;
		newBoundSphere.center.z = center.z + (sphere.center.z - center.z)*t;
		status =  true;
	    }

	    newBoundSphere.updateBoundsStates();
	    return status;

	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingBox tbox =  new BoundingBox();

	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    if( this.intersect( polytope) ){
		BoundingBox sbox = new BoundingBox( this ); // convert sphere to box
		BoundingBox pbox = new BoundingBox( polytope ); // convert polytope to box
		sbox.intersect(pbox,tbox);  // insersect two boxes
		newBoundSphere.set( tbox ); // set sphere to the intersection of 2 boxesf
		return true;
	    } else {
		// Negative volume.
		newBoundSphere.center.x = newBoundSphere.center.y =
		    newBoundSphere.center.z = 0.0;
		newBoundSphere.radius = -1.0;
		newBoundSphere.updateBoundsStates();
		return false;
	    }
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere8"));
	}
    }

    /**
     * Test for intersection with an array of  bounds objects.
     * @param boundsObjects an array of bounds objects
     * @param newBoundSphere the new bounding sphere which is the intersection of
     *      the boundsObject and this BoundingSphere
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds[] boundsObjects, BoundingSphere newBoundSphere) {

	if( boundsObjects == null || boundsObjects.length <= 0 ||  boundsIsEmpty ) {
	    // Negative volume.
	    newBoundSphere.center.x = newBoundSphere.center.y =
		newBoundSphere.center.z = 0.0;
	    newBoundSphere.radius = -1.0;
	    newBoundSphere.updateBoundsStates();
	    return false;
	}

	int i=0;

	// find first non null bounds object
	while( boundsObjects[i] == null && i < boundsObjects.length) {
	    i++;
	}

	if( i >= boundsObjects.length ) { // all bounds objects were empty
	    // Negative volume.
	    newBoundSphere.center.x = newBoundSphere.center.y =
		newBoundSphere.center.z = 0.0;
	    newBoundSphere.radius = -1.0;
	    newBoundSphere.updateBoundsStates();
	    return false;
	}

	boolean status = false;
	double newRadius;
	Point3d newCenter = new Point3d();
	BoundingBox tbox = new BoundingBox();

	for(i=0;i<boundsObjects.length;i++) {
	    if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty) ;
	    else if( boundsObjects[i].boundId == BOUNDING_BOX) {
		BoundingBox box = (BoundingBox)boundsObjects[i];
		if( this.intersect( box) ){
		    BoundingBox sbox = new BoundingBox( this ); // convert sphere to box
		    sbox.intersect(box,tbox); // insersect two boxes
		    if( status ) {
			newBoundSphere.combine( tbox );
		    } else {
			newBoundSphere.set( tbox ); // set sphere to the intersection of 2 boxesf
			status = true;
		    }
		}
	    } else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		double dis,t,d2;
		dis = Math.sqrt( (center.x-sphere.center.x)*(center.x-sphere.center.x) +
				 (center.y-sphere.center.y)*(center.y-sphere.center.y) +
				 (center.z-sphere.center.z)*(center.z-sphere.center.z) );
		if ( dis > radius+sphere.radius) {
		} else if( dis+radius <= sphere.radius ) { // this sphere is contained within boundsObject
		    if( status ) {
			newBoundSphere.combine( this );
		    } else {
			newBoundSphere.center.x = center.x;
			newBoundSphere.center.y = center.y;
			newBoundSphere.center.z = center.z;
			newBoundSphere.radius = radius;
			status = true;
			newBoundSphere.updateBoundsStates();
		    }
		} else if( dis+sphere.radius <= radius ) { // boundsObject is containted within this sphere
		    if( status ) {
			newBoundSphere.combine( sphere );
		    } else {
			newBoundSphere.center.x = center.x;
			newBoundSphere.center.y = center.y;
			newBoundSphere.center.z = center.z;
			newBoundSphere.radius = sphere.radius;
			status = true;
			newBoundSphere.updateBoundsStates();
		    }
		} else  {
		    // distance from this center to center of overlapped volume
		    d2 = (dis*dis + radius*radius - sphere.radius*sphere.radius)/(2.0*dis);
		    newRadius = Math.sqrt( radius*radius - d2*d2);
		    t = d2/dis;
		    newCenter.x = center.x + (sphere.center.x - center.x)*t;
		    newCenter.y = center.y + (sphere.center.y - center.y)*t;
		    newCenter.z = center.z + (sphere.center.z - center.z)*t;
		    if( status ) {
			BoundingSphere newSphere = new BoundingSphere( newCenter,
								       newRadius );
			newBoundSphere.combine( newSphere );
		    } else {
			newBoundSphere.setRadius( newRadius );
			newBoundSphere.setCenter( newCenter );
			status = true;
		    }
		}

	    } else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		if( this.intersect( polytope) ){
		    BoundingBox sbox = new BoundingBox( this ); // convert sphere to box
		    BoundingBox pbox = new BoundingBox( polytope ); // convert polytope to box
		    sbox.intersect(pbox, tbox);            // insersect two boxes
		    if( status ) {
			newBoundSphere.combine( tbox );
		    } else {
			newBoundSphere.set( tbox );                // set sphere to the intersection of 2 boxesf
			status = true;
		    }
		}
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere9"));
	    }
	}
	if( status  == false) {
	    // Negative volume.
	    newBoundSphere.center.x = newBoundSphere.center.y =
		newBoundSphere.center.z = 0.0;
	    newBoundSphere.radius = -1.0;
	    newBoundSphere.updateBoundsStates();
	}
	return status;
    }

    /**
     * Finds closest bounding object that intersects this bounding sphere.
     * @param boundsObjects an array of  bounds objects
     * @return closest bounding object
     */
    public Bounds closestIntersection( Bounds[] boundsObjects) {

	if( boundsObjects == null || boundsObjects.length <= 0  ) {
	    return null;
        }

	if( boundsIsEmpty ) {
	    return null;
	}

	double dis,far_dis,pdist,x,y,z,rad_sq;
	double cenX = 0.0, cenY = 0.0, cenZ = 0.0;
	boolean contains = false;
	boolean inside;
	boolean intersect = false;
	double smallest_distance = Double.MAX_VALUE;
	int i,j,index=0;


	for(i = 0; i < boundsObjects.length; i++){
	    if( boundsObjects[i] == null ) ;

	    else if( this.intersect( boundsObjects[i])) {
		intersect = true;
		if(boundsObjects[i].boundId == BOUNDING_BOX){
		    BoundingBox box = (BoundingBox)boundsObjects[i];
		    cenX = (box.upper.x+box.lower.x)/2.0;
		    cenY = (box.upper.y+box.lower.y)/2.0;
		    cenZ = (box.upper.z+box.lower.z)/2.0;
		    dis = Math.sqrt( (center.x-cenX)*(center.x-cenX) +
				     (center.y-cenY)*(center.y-cenY) +
				     (center.z-cenZ)*(center.z-cenZ) );
		    if( (center.x-box.lower.x)*(center.x-box.lower.x) >
			(center.x-box.upper.x)*(center.x-box.upper.x) )
			far_dis = (center.x-box.lower.x)*(center.x-box.lower.x);
		    else
			far_dis = (center.x-box.upper.x)*(center.x-box.upper.x);

		    if( (center.y-box.lower.y)*(center.y-box.lower.y) >
			(center.y-box.upper.y)*(center.y-box.upper.y) )
			far_dis += (center.y-box.lower.y)*(center.y-box.lower.y);
		    else
			far_dis += (center.y-box.upper.y)*(center.y-box.upper.y);

		    if( (center.z-box.lower.z)*(center.z-box.lower.z) >
			(center.z-box.upper.z)*(center.z-box.upper.z) )
			far_dis += (center.z-box.lower.z)*(center.z-box.lower.z);
		    else
			far_dis += (center.z-box.upper.z)*(center.z-box.upper.z);

		    rad_sq = radius * radius;
		    if( far_dis <= rad_sq )  { // contains box
			if( !contains ){ // initialize smallest_distance for the first containment
			    index = i;
			    smallest_distance = dis;
			    contains = true;
			} else{
			    if( dis < smallest_distance){
				index = i;
				smallest_distance = dis;
			    }
			}
		    } else if (!contains) {
			if( dis < smallest_distance){
			    index = i;
			    smallest_distance = dis;
			}
		    }
		} else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		    BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		    dis = Math.sqrt( (center.x-sphere.center.x)*(center.x-sphere.center.x) +
				     (center.y-sphere.center.y)*(center.y-sphere.center.y) +
				     (center.z-sphere.center.z)*(center.z-sphere.center.z) );
		    if( (dis+sphere.radius) <= radius) { // contains the sphere
			if( !contains ){ // initialize smallest_distance for the first containment
			    index = i;
			    smallest_distance = dis;
			    contains = true;
			} else{
			    if( dis < smallest_distance){
				index = i;
				smallest_distance = dis;
			    }
			}
		    } else if (!contains) {
			if( dis < smallest_distance){
			    index = i;
			    smallest_distance = dis;
			}
		    }

		} else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		    BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		    dis = Math.sqrt( (center.x-polytope.centroid.x)*(center.x-polytope.centroid.x) +
				     (center.y-polytope.centroid.y)*(center.y-polytope.centroid.y) +
				     (center.z-polytope.centroid.z)*(center.z-polytope.centroid.z) );
		    inside = true;
		    for(j=0;j<polytope.nVerts;j++) {
			x = polytope.verts[j].x - center.x;
			y = polytope.verts[j].y - center.y;
			z = polytope.verts[j].z - center.z;

			pdist = x*x + y*y + z*z;
			if( pdist > radius*radius)
			    inside=false;
		    }
		    if( inside ) {
			if( !contains ){ // initialize smallest_distance for the first containment
			    index = i;
			    smallest_distance = dis;
			    contains = true;
			} else{
			    if( dis < smallest_distance){
				index = i;
				smallest_distance = dis;
			    }
			}
		    } else if (!contains) {
			if( dis < smallest_distance){
			    index = i;
			    smallest_distance = dis;
			}
		    }

		} else {
		    throw new IllegalArgumentException(J3dI18N.getString("BoundingSphere10"));
		}
	    }
	}

	if ( intersect )
	    return boundsObjects[index];
	else
	    return null;

    }


    /**
     * Intersects this bounding sphere with preprocessed  frustum.
     * @return true if the bounding sphere and frustum intersect.
     */
    boolean intersect(CachedFrustum frustum) {
	int i;
	double dist;

	if( boundsIsEmpty ) {
	    return false;
	}

	if(boundsIsInfinite)
	    return true;

	for (i=0; i<6; i++) {
	    dist = frustum.clipPlanes[i].x*center.x + frustum.clipPlanes[i].y*center.y +
	        frustum.clipPlanes[i].z*center.z + frustum.clipPlanes[i].w;
	    if (dist < 0.0 && (dist + radius) < 0.0) {
		return(false);
	    }
	}
	return true;
    }

    /**
     * This intersects this bounding sphere with 6 frustum plane equations
     * @return returns true if the bounding sphere and frustum intersect.
     */
    boolean intersect(Vector4d[] planes) {
	int i;
	double dist;

	if( boundsIsEmpty ) {
	    return false;
	}

	if(boundsIsInfinite)
	    return true;

	for (i=0; i<6; i++) {
	    dist = planes[i].x*center.x + planes[i].y*center.y +
	        planes[i].z*center.z + planes[i].w;
	    if (dist < 0.0 && (dist + radius) < 0.0) {
		//System.err.println("Tossing " + i + " " + dist + " " + radius);
		return(false);
	    }
	}
	return true;
    }

    /**
     * Returns a string representation of this class.
     */
    public String toString() {
	return new String( "Center="+center+"  Radius="+radius);
    }

    private void updateBoundsStates() {

	if (checkBoundsIsNaN()) {
	     boundsIsEmpty = true;
	     boundsIsInfinite = false;
	     return;
	}

	if(radius == Double.POSITIVE_INFINITY) {
	    boundsIsEmpty = false;
	    boundsIsInfinite = true;
	}
	else {
	    boundsIsInfinite = false;
	    if( radius < 0.0 ) {
		boundsIsEmpty = true;
	    } else {
		boundsIsEmpty = false;
	    }
	}
    }

    Point3d getCenter() {
	return center;
    }

    /**
     * if the passed the "region" is same type as this object
     * then do a copy, otherwise clone the Bounds  and
     * return
     */
    Bounds copy(Bounds r) {
	if (r != null && this.boundId == r.boundId) {
	    BoundingSphere region = (BoundingSphere)r;
	    region.radius = radius;
	    region.center.x = center.x;
	    region.center.y = center.y;
	    region.center.z = center.z;
	    region.boundsIsEmpty = boundsIsEmpty;
	    region.boundsIsInfinite = boundsIsInfinite;
	    return region;
	}
	else {
	    return (Bounds) this.clone();
	}
    }

    boolean checkBoundsIsNaN() {
	if (Double.isNaN(radius+center.x+center.y+center.z)) {
	    return true;
	}
	return false;
    }

    int getPickType() {
	return PickShape.PICKBOUNDINGSPHERE;
    }
}




