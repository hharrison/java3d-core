/*
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
 */

package javax.media.j3d;

import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

/**
 *  This class defines an axis aligned bounding box which is used for
 *  bounding regions.
 *
 */

public class BoundingBox extends Bounds {

/**
 * The corner of the bounding box with the numerically smallest values.
 */
final Point3d lower;

/**
 * The corner of the bounding box with the numerically largest values.
 */
final Point3d upper;
    private static final double EPS = 1.0E-8;

/**
 * Constructs and initializes a BoundingBox given min,max in x,y,z.
 * @param lower the "small" corner
 * @param upper the "large" corner
 */
public BoundingBox(Point3d lower, Point3d upper) {
	boundId = BOUNDING_BOX;
	this.lower = new Point3d(lower);
	this.upper = new Point3d(upper);
	updateBoundsStates();
}

/**
 * Constructs and initializes a 2X bounding box about the origin. The lower
 * corner is initialized to (-1.0d, -1.0d, -1.0d) and the upper corner is
 * initialized to (1.0d, 1.0d, 1.0d).
 */
public BoundingBox() {
	boundId = BOUNDING_BOX;
	lower = new Point3d(-1.0d, -1.0d, -1.0d);
	upper = new Point3d( 1.0d,  1.0d,  1.0d);
	boundsIsEmpty = false;
	boundsIsInfinite = false;
}

/**
 * Constructs a BoundingBox from a bounding object.
 * @param boundsObject a bounds object
 */
public BoundingBox(Bounds boundsObject) {
	boundId = BOUNDING_BOX;
	lower = new Point3d();
	upper = new Point3d();

	if (boundsObject == null || boundsObject.boundsIsEmpty) {
		setEmptyBounds();
		return;
	}

	if (boundsObject.boundsIsInfinite) {
		setInfiniteBounds();
		return;
	}

	if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;

		lower.set(box.lower);
		upper.set(box.upper);
	}
	else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;

		lower.set(sphere.center.x - sphere.radius,
		          sphere.center.y - sphere.radius,
		          sphere.center.z - sphere.radius);

		upper.set(sphere.center.x + sphere.radius,
		          sphere.center.y + sphere.radius,
		          sphere.center.z + sphere.radius);
	}
	else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    if( polytope.nVerts < 1 ) { // handle degenerate case
		lower.set(-1.0d, -1.0d, -1.0d);
		upper.set( 1.0d,  1.0d,  1.0d);
	    } else {
			lower.set(polytope.verts[0]);
			upper.set(polytope.verts[0]);

		for(int i=1;i<polytope.nVerts;i++) {
		    if( polytope.verts[i].x < lower.x )
			lower.x = polytope.verts[i].x;
		    if( polytope.verts[i].y < lower.y )
			lower.y = polytope.verts[i].y;
		    if( polytope.verts[i].z < lower.z )
			lower.z = polytope.verts[i].z;
		    if( polytope.verts[i].x > upper.x )
			upper.x = polytope.verts[i].x;
		    if( polytope.verts[i].y > upper.y )
			upper.y = polytope.verts[i].y;
		    if( polytope.verts[i].z > upper.z )
			upper.z = polytope.verts[i].z;
		}
	    }

	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingBox0"));
	}

        updateBoundsStates();
    }

/**
 * Constructs a BoundingBox from an array of bounding objects.
 * @param bounds an array of bounding objects
 */
public BoundingBox(Bounds[] bounds) {
	boundId = BOUNDING_BOX;
	upper = new Point3d();
	lower = new Point3d();

	if (bounds == null || bounds.length <= 0) {
		setEmptyBounds();
		return;
	}

	int i = 0;
       // find first non empty bounds object
       while ((i < bounds.length) && ((bounds[i] == null) || bounds[i].boundsIsEmpty)) {
	   i++;
       }

	if (i >= bounds.length) {
		// all bounds objects were empty
		setEmptyBounds();
		return;
	}

       this.set(bounds[i++]);
       if(boundsIsInfinite)
	   return;

       for(;i<bounds.length;i++) {
           if( bounds[i] == null );   // do nothing
           else if( bounds[i].boundsIsEmpty); // do nothing
	   else if( bounds[i].boundsIsInfinite ) {
			setInfiniteBounds();
			return; // We're done.
	   }
	   else if(bounds[i].boundId == BOUNDING_BOX){
	       BoundingBox box = (BoundingBox)bounds[i];

	       if( lower.x > box.lower.x) lower.x = box.lower.x;
	       if( lower.y > box.lower.y) lower.y = box.lower.y;
	       if( lower.z > box.lower.z) lower.z = box.lower.z;
	       if( upper.x < box.upper.x) upper.x = box.upper.x;
	       if( upper.y < box.upper.y) upper.y = box.upper.y;
	       if( upper.z < box.upper.z) upper.z = box.upper.z;

	   }
	   else if(bounds[i].boundId == BOUNDING_SPHERE) {
	       BoundingSphere sphere = (BoundingSphere)bounds[i];
	       if( lower.x > (sphere.center.x - sphere.radius))
		   lower.x = sphere.center.x - sphere.radius;
	       if( lower.y > (sphere.center.y - sphere.radius))
		   lower.y = sphere.center.y - sphere.radius;
	       if( lower.z > (sphere.center.z - sphere.radius))
		   lower.z = sphere.center.z - sphere.radius;
	       if( upper.x < (sphere.center.x + sphere.radius))
		   upper.x = sphere.center.x + sphere.radius;
	       if( upper.y < (sphere.center.y + sphere.radius))
		   upper.y = sphere.center.y + sphere.radius;
	       if( upper.z < (sphere.center.z + sphere.radius))
		   upper.z = sphere.center.z + sphere.radius;
	   }
	   else if(bounds[i].boundId == BOUNDING_POLYTOPE) {
	       BoundingPolytope polytope = (BoundingPolytope)bounds[i];

	       for(i=0;i<polytope.nVerts;i++) { // XXXX: handle polytope with no verts
		   if( polytope.verts[i].x < lower.x )
		       lower.x = polytope.verts[i].x;
		   if( polytope.verts[i].y < lower.y )
		       lower.y = polytope.verts[i].y;
		   if( polytope.verts[i].z < lower.z )
		       lower.z = polytope.verts[i].z;
		   if( polytope.verts[i].x > upper.x )
		       upper.x = polytope.verts[i].x;
		   if( polytope.verts[i].y > upper.y )
		       upper.y = polytope.verts[i].y;
		   if( polytope.verts[i].z > upper.z )
		       upper.z = polytope.verts[i].z;
	       }
	   }
	   else {
	       throw new IllegalArgumentException(J3dI18N.getString("BoundingBox1"));
	   }
       }
       updateBoundsStates();
    }

/**
 * Gets the lower corner of this bounding box.
 * @param p1 a Point to receive the lower corner of the bounding box
 */
public void getLower(Point3d p1) {
	p1.set(lower);
}

/**
 * Sets the lower corner of this bounding box.
 * @param xmin minimum x value of bounding box
 * @param ymin minimum y value of bounding box
 * @param zmin minimum z value of bounding box
 */
public void setLower(double xmin, double ymin, double zmin) {
	lower.set(xmin, ymin, zmin);
	updateBoundsStates();
}

/**
 * Sets the lower corner of this bounding box.
 * @param p1 a Point defining the new lower corner of the bounding box
 */
public void setLower(Point3d p1) {
	lower.set(p1);
	updateBoundsStates();
}

/**
 * Gets the upper corner of this bounding box.
 * @param p1 a Point to receive the upper corner of the bounding box
 */
public void getUpper(Point3d p1) {
	p1.set(upper);
}

/**
 * Sets the upper corner of this bounding box.
 * @param xmax max x value of bounding box
 * @param ymax max y value of bounding box
 * @param zmax max z value of bounding box
 */
public void setUpper(double xmax, double ymax, double zmax) {
	upper.set(xmax, ymax, zmax);
	updateBoundsStates();
}

/**
 * Sets the upper corner of this bounding box.
 * @param p1 a Point defining the new upper corner of the bounding box
 */
public void setUpper(Point3d p1) {
	upper.set(p1);
	updateBoundsStates();
}

    /**
     * Sets the the value of this BoundingBox
     * @param boundsObject another bounds object
     */
    @Override
    public void set(Bounds  boundsObject) {
      int i;

	if (boundsObject == null || boundsObject.boundsIsEmpty) {
		setEmptyBounds();
		return;
	}

	if (boundsObject.boundsIsInfinite) {
		setInfiniteBounds();
		return;
	}

      if( boundsObject.boundId == BOUNDING_BOX){
	  BoundingBox box = (BoundingBox)boundsObject;

	  lower.x = box.lower.x;
	  lower.y = box.lower.y;
	  lower.z = box.lower.z;
	  upper.x = box.upper.x;
	  upper.y = box.upper.y;
	  upper.z = box.upper.z;

      } else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	  BoundingSphere sphere = (BoundingSphere)boundsObject;
	  lower.x = sphere.center.x - sphere.radius;
	  lower.y = sphere.center.y - sphere.radius;
	  lower.z = sphere.center.z - sphere.radius;
	  upper.x = sphere.center.x + sphere.radius;
	  upper.y = sphere.center.y + sphere.radius;
	  upper.z = sphere.center.z + sphere.radius;

      } else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	  BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	  lower.x = upper.x = polytope.verts[0].x;
	  lower.y = upper.y = polytope.verts[0].y;
	  lower.z = upper.z = polytope.verts[0].z;

	  for(i=1;i<polytope.nVerts;i++) {
	      if( polytope.verts[i].x < lower.x ) lower.x = polytope.verts[i].x;
	      if( polytope.verts[i].y < lower.y ) lower.y = polytope.verts[i].y;
	      if( polytope.verts[i].z < lower.z ) lower.z = polytope.verts[i].z;
	      if( polytope.verts[i].x > upper.x ) upper.x = polytope.verts[i].x;
	      if( polytope.verts[i].y > upper.y ) upper.y = polytope.verts[i].y;
	      if( polytope.verts[i].z > upper.z ) upper.z = polytope.verts[i].z;
	  }

      } else {
	  throw new IllegalArgumentException(J3dI18N.getString("BoundingBox0"));
      }

      updateBoundsStates();
    }


    /**
     * Creates a copy of this bounding box.
     * @return a new bounding box
     */
    @Override
    public Object clone() {
	return new BoundingBox(this.lower, this.upper);
    }


    /**
     * Indicates whether the specified <code>bounds</code> object is
     * equal to this BoundingBox object.  They are equal if the
     * specified <code>bounds</code> object is an instance of
     * BoundingBox and all of the data
     * members of <code>bounds</code> are equal to the corresponding
     * data members in this BoundingBox.
     * @param bounds the object with which the comparison is made.
     * @return true if this BoundingBox is equal to <code>bounds</code>;
     * otherwise false
     *
     * @since Java 3D 1.2
     */
    @Override
    public boolean equals(Object bounds) {
	try {
	    BoundingBox box = (BoundingBox)bounds;
	    return (lower.equals(box.lower) &&
		    upper.equals(box.upper));
	}
	catch (NullPointerException e) {
	    return false;
	}
        catch (ClassCastException e) {
	    return false;
	}
    }


    /**
     * Returns a hash code value for this BoundingBox object
     * based on the data values in this object.  Two different
     * BoundingBox objects with identical data values (i.e.,
     * BoundingBox.equals returns true) will return the same hash
     * code value.  Two BoundingBox objects with different data
     * members may return the same hash code value, although this is
     * not likely.
     * @return a hash code value for this BoundingBox object.
     *
     * @since Java 3D 1.2
     */
    @Override
    public int hashCode() {
	long bits = 1L;
	bits = J3dHash.mixDoubleBits(bits, lower.x);
	bits = J3dHash.mixDoubleBits(bits, lower.y);
	bits = J3dHash.mixDoubleBits(bits, lower.z);
	bits = J3dHash.mixDoubleBits(bits, upper.x);
	bits = J3dHash.mixDoubleBits(bits, upper.y);
	bits = J3dHash.mixDoubleBits(bits, upper.z);
	return J3dHash.finish(bits);
    }


    /**
     * Combines this bounding box with a bounding object   so that the
     * resulting bounding box encloses the original bounding box and the
     * specified bounds object.
     * @param boundsObject another bounds object
     */
    @Override
    public void combine(Bounds boundsObject) {

	if((boundsObject == null) || (boundsObject.boundsIsEmpty)
	   || (boundsIsInfinite))
	    return;

	if((boundsIsEmpty) || (boundsObject.boundsIsInfinite)) {
	    this.set(boundsObject);
	    return;
	}

	if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;

	    if( lower.x > box.lower.x) lower.x = box.lower.x;
	    if( lower.y > box.lower.y) lower.y = box.lower.y;
	    if( lower.z > box.lower.z) lower.z = box.lower.z;
	    if( upper.x < box.upper.x) upper.x = box.upper.x;
	    if( upper.y < box.upper.y) upper.y = box.upper.y;
	    if( upper.z < box.upper.z) upper.z = box.upper.z;

	}
	else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    if( lower.x > (sphere.center.x - sphere.radius))
		lower.x = sphere.center.x - sphere.radius;
	    if( lower.y > (sphere.center.y - sphere.radius))
		lower.y = sphere.center.y - sphere.radius;
	    if( lower.z > (sphere.center.z - sphere.radius))
		lower.z = sphere.center.z - sphere.radius;
	    if( upper.x < (sphere.center.x + sphere.radius))
		upper.x = sphere.center.x + sphere.radius;
	    if( upper.y < (sphere.center.y + sphere.radius))
		upper.y = sphere.center.y + sphere.radius;
	    if( upper.z < (sphere.center.z + sphere.radius))
		upper.z = sphere.center.z + sphere.radius;

	}
	else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    int i;
	    for(i=1;i<polytope.nVerts;i++) {
                if( polytope.verts[i].x < lower.x ) lower.x = polytope.verts[i].x;
                if( polytope.verts[i].y < lower.y ) lower.y = polytope.verts[i].y;
                if( polytope.verts[i].z < lower.z ) lower.z = polytope.verts[i].z;
                if( polytope.verts[i].x > upper.x ) upper.x = polytope.verts[i].x;
                if( polytope.verts[i].y > upper.y ) upper.y = polytope.verts[i].y;
                if( polytope.verts[i].z > upper.z ) upper.z = polytope.verts[i].z;
	    }
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingBox3"));
	}

        updateBoundsStates();
    }

    /**
     * Combines this bounding box with an array of bounding objects
     * so that the resulting bounding box encloses the original bounding
     * box and the array of bounding objects.
     * @param bounds an array of bounds objects
     */
    @Override
    public void combine(Bounds[] bounds) {
       int i=0;

       if( (bounds == null) || (bounds.length <= 0)
	   || (boundsIsInfinite))
	   return;

       // find first non empty bounds object
       while( (i<bounds.length) && ((bounds[i]==null) || bounds[i].boundsIsEmpty)) {
	   i++;
       }
       if( i >= bounds.length)
	   return;   // no non empty bounds so do not modify current bounds

       if(boundsIsEmpty)
	   this.set(bounds[i++]);

       if(boundsIsInfinite)
	   return;

       for(;i<bounds.length;i++) {
          if( bounds[i] == null );  // do nothing
          else if( bounds[i].boundsIsEmpty); // do nothing
	  else if( bounds[i].boundsIsInfinite ) {
	      lower.x = lower.y = lower.z = Double.NEGATIVE_INFINITY;
	      upper.x = upper.y = upper.z = Double.POSITIVE_INFINITY;
	      break;  // We're done.
	  }
	  else if( bounds[i].boundId == BOUNDING_BOX){
 	      BoundingBox box = (BoundingBox)bounds[i];

              if( lower.x > box.lower.x) lower.x = box.lower.x;
              if( lower.y > box.lower.y) lower.y = box.lower.y;
              if( lower.z > box.lower.z) lower.z = box.lower.z;
              if( upper.x < box.upper.x) upper.x = box.upper.x;
              if( upper.y < box.upper.y) upper.y = box.upper.y;
              if( upper.z < box.upper.z) upper.z = box.upper.z;
	  }
	  else if( bounds[i].boundId == BOUNDING_SPHERE ) {
	      BoundingSphere sphere = (BoundingSphere)bounds[i];
              if( lower.x > (sphere.center.x - sphere.radius))
		  lower.x = sphere.center.x - sphere.radius;
              if( lower.y > (sphere.center.y - sphere.radius))
		  lower.y = sphere.center.y - sphere.radius;
              if( lower.z > (sphere.center.z - sphere.radius))
		  lower.z = sphere.center.z - sphere.radius;
              if( upper.x < (sphere.center.x + sphere.radius))
		  upper.x = sphere.center.x + sphere.radius;
              if( upper.y < (sphere.center.y + sphere.radius))
		  upper.y = sphere.center.y + sphere.radius;
              if( upper.z < (sphere.center.z + sphere.radius))
		  upper.z = sphere.center.z + sphere.radius;
	  }
	  else if(bounds[i].boundId == BOUNDING_POLYTOPE) {
 	      BoundingPolytope polytope = (BoundingPolytope)bounds[i];
              for(i=1;i<polytope.nVerts;i++) {
                if( polytope.verts[i].x < lower.x ) lower.x = polytope.verts[i].x;
                if( polytope.verts[i].y < lower.y ) lower.y = polytope.verts[i].y;
                if( polytope.verts[i].z < lower.z ) lower.z = polytope.verts[i].z;
                if( polytope.verts[i].x > upper.x ) upper.x = polytope.verts[i].x;
                if( polytope.verts[i].y > upper.y ) upper.y = polytope.verts[i].y;
                if( polytope.verts[i].z > upper.z ) upper.z = polytope.verts[i].z;
              }
	  } else {
	      throw new IllegalArgumentException(J3dI18N.getString("BoundingBox4"));
 	  }
       }

       updateBoundsStates();
    }

    /**
     * Combines this bounding box with a point so that the resulting
     * bounding box encloses the original bounding box and the point.
     * @param point a 3d point in space
     */
    @Override
    public void combine(Point3d point) {

	if( boundsIsInfinite) {
	    return;
	}

	if( boundsIsEmpty) {
            upper.x = lower.x = point.x;
            upper.y = lower.y = point.y;
            upper.z = lower.z = point.z;
	} else {
	    if( point.x > upper.x) upper.x = point.x;
   	    if( point.y > upper.y) upper.y = point.y;
	    if( point.z > upper.z) upper.z = point.z;

	    if( point.x < lower.x) lower.x = point.x;
	    if( point.y < lower.y) lower.y = point.y;
	    if( point.z < lower.z) lower.z = point.z;
	}

         updateBoundsStates();
    }

    /**
     * Combines this bounding box with an array of points so that the
     * resulting bounding box encloses the original bounding box and the
     * array of points.
     * @param points an array of 3d points in space
     */
    @Override
    public void combine(Point3d[] points) {

	int i;

	if( boundsIsInfinite) {
	    return;
	}

	if( boundsIsEmpty) {
	    this.setUpper(points[0]);
	    this.setLower(points[0]);
	}

	for(i=0;i<points.length;i++) {
	    if( points[i].x > upper.x) upper.x = points[i].x;
	    if( points[i].y > upper.y) upper.y = points[i].y;
	    if( points[i].z > upper.z) upper.z = points[i].z;

	    if( points[i].x < lower.x) lower.x = points[i].x;
	    if( points[i].y < lower.y) lower.y = points[i].y;
	    if( points[i].z < lower.z) lower.z = points[i].z;
	}

        updateBoundsStates();
    }

    /**
     * Modifies the bounding box so that it bounds the volume
     * generated by transforming the given bounding object.
     * @param boundsObject the bounding object to be transformed
     * @param matrix a transformation matrix
     */
    @Override
    public void transform( Bounds boundsObject, Transform3D matrix) {

	if (boundsObject == null || boundsObject.boundsIsEmpty) {
		setEmptyBounds();
		return;
	}

	if (boundsObject.boundsIsInfinite) {
		setInfiniteBounds();
		return;
	}

	if(boundsObject.boundId == BOUNDING_BOX){
		this.set(boundsObject);
		this.transform(matrix);
	}
	else if(boundsObject.boundId == BOUNDING_SPHERE) {
		BoundingSphere tmpSphere = new BoundingSphere(boundsObject);
		tmpSphere.transform(matrix);
		this.set(tmpSphere);
	}
	else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
		BoundingPolytope tmpPolytope = new BoundingPolytope(boundsObject);
		tmpPolytope.transform(matrix);
		this.set(tmpPolytope);
	}
	else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingBox5"));
	}
    }

    /**
     * Transforms this bounding box by the given matrix.
     * @param matrix a transformation matrix
     */
    @Override
    public void transform(Transform3D matrix) {

	if (boundsIsInfinite)
		return;

	Point3d tmpP3d = new Point3d();

	double ux, uy, uz, lx, ly, lz;
	ux = upper.x; uy = upper.y; uz = upper.z;
	lx = lower.x; ly = lower.y; lz = lower.z;

	tmpP3d.set(ux, uy, uz);
	matrix.transform( tmpP3d );
	upper.x = tmpP3d.x;
	upper.y = tmpP3d.y;
	upper.z = tmpP3d.z;
	lower.x = tmpP3d.x;
	lower.y = tmpP3d.y;
	lower.z = tmpP3d.z;

	tmpP3d.set(lx, uy, uz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x  > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y  > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z  > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x  < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y  < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z  < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(lx, ly, uz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x  > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y  > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z  > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x  < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y  < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z  < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(ux, ly, uz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(lx, uy, lz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(ux, uy, lz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(lx, ly, lz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z < lower.z ) lower.z = tmpP3d.z;

	tmpP3d.set(ux, ly, lz);
	matrix.transform( tmpP3d );
	if ( tmpP3d.x > upper.x ) upper.x = tmpP3d.x;
	if ( tmpP3d.y > upper.y ) upper.y = tmpP3d.y;
	if ( tmpP3d.z > upper.z ) upper.z = tmpP3d.z;
	if ( tmpP3d.x < lower.x ) lower.x = tmpP3d.x;
	if ( tmpP3d.y < lower.y ) lower.y = tmpP3d.y;
	if ( tmpP3d.z < lower.z ) lower.z = tmpP3d.z;

    }

    /**
     * Test for intersection with a ray.
     * @param origin the starting point of the ray
     * @param direction the direction of the ray
     * @param position3 a point defining the location of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    @Override
    boolean intersect(Point3d origin, Vector3d direction, Point4d position ) {
        double t1,t2,tmp,tnear,tfar,invDir,invMag;
	double dirx, diry, dirz;

	/*
	  System.err.println("BoundingBox.intersect(p,d,p) called\n");
	  System.err.println("bounds = " + lower + " -> " + upper);
	  */

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

	double dirLen = direction.x*direction.x + direction.y*direction.y +
	    direction.z*direction.z;

	// Handle zero length direction vector.
	if(dirLen == 0.0)
	    return intersect(origin, position);

	invMag = 1.0/Math.sqrt(dirLen);
	dirx = direction.x*invMag;
	diry = direction.y*invMag;
	dirz = direction.z*invMag;

	/*
	  System.err.println("dir = " + dirx + ", " + diry + ", " + dirz);
	  System.err.println("origin = " + origin);
	  */

	// initialize tnear and tfar to handle dir.? == 0 cases
	tnear = -Double.MAX_VALUE;
	tfar = Double.MAX_VALUE;

	if(dirx == 0.0) {
	    //System.err.println("dirx == 0.0");
	    if (origin.x < lower.x || origin.x > upper.x ) {
		//System.err.println( "parallel to x plane and outside");
		return false;
	    }
	} else {
	    invDir = 1.0/dirx;
	    t1 = (lower.x-origin.x)*invDir;
	    t2 = (upper.x-origin.x)*invDir;

	    //System.err.println("x t1 = " + t1 + " t2 = " + t2);
	    if( t1 > t2) {
		tnear = t2;
		tfar = t1;
	    }else {
		tnear = t1;
		tfar = t2;
	    }
	    if( tfar < 0.0 ) {
		//System.err.println( "x failed: tnear="+tnear+"  tfar="+tfar);
		return false;
	    }
	    //System.err.println("x tnear = " + tnear + " tfar = " + tfar);
	}
	// y
	if (diry == 0.0) {
	    //System.err.println("diry == 0.0");
	    if( origin.y < lower.y || origin.y > upper.y ){
		//System.err.println( "parallel to y plane and outside");
		return false;
            }
	} else {
	    invDir = 1.0/diry;
	    //System.err.println("invDir = " + invDir);
	    t1 = (lower.y-origin.y)*invDir;
	    t2 = (upper.y-origin.y)*invDir;

	    if( t1 > t2) {
		tmp = t1;
		t1 = t2;
		t2 = tmp;
	    }
	    //System.err.println("y t1 = " + t1 + " t2 = " + t2);
	    if( t1 > tnear) tnear = t1;
	    if( t2 < tfar ) tfar  = t2;

	    if( (tfar < 0.0) ||  (tnear > tfar)){
		//System.err.println( "y failed: tnear="+tnear+"  tfar="+tfar);
		return false;
	    }
	    //System.err.println("y tnear = " + tnear + " tfar = " + tfar);
	}

	// z
	if (dirz == 0.0) {
	    //System.err.println("dirz == 0.0");
	    if( origin.z < lower.z || origin.z > upper.z ) {
		//System.err.println( "parallel to z plane and outside");
		return false;
	    }
	}  else {
	    invDir = 1.0/dirz;
	    t1 = (lower.z-origin.z)*invDir;
	    t2 = (upper.z-origin.z)*invDir;

	    if( t1 > t2) {
		tmp = t1;
		t1 = t2;
		t2 = tmp;
	    }
	    //System.err.println("z t1 = " + t1 + " t2 = " + t2);
	    if( t1 > tnear) tnear = t1;
	    if( t2 < tfar ) tfar  = t2;

	    if( (tfar < 0.0) ||  (tnear > tfar)){
		//System.err.println( "z failed: tnear="+tnear+"  tfar="+tfar);
		return false;
	    }
	    //System.err.println("z tnear = " + tnear + " tfar = " + tfar);
	}

	if((tnear < 0.0) && (tfar >= 0.0)) {
	    // origin is inside the BBox.
	    position.x = origin.x + dirx*tfar;
	    position.y = origin.y + diry*tfar;
	    position.z = origin.z + dirz*tfar;
	    position.w = tfar;
	}
	else {
	    position.x = origin.x + dirx*tnear;
	    position.y = origin.y + diry*tnear;
	    position.z = origin.z + dirz*tnear;
	    position.w = tnear;
	}

	return true;

    }


    /**
     * Test for intersection with a point.
     * @param point the pick point
     * @param position a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    @Override
    boolean intersect(Point3d point,  Point4d position ) {

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

       if( point.x <= upper.x && point.x >= lower.x &&
	   point.y <= upper.y && point.y >= lower.y &&
	   point.z <= upper.z && point.z >= lower.z)  {
	   position.x = point.x;
	   position.y = point.y;
	   position.z = point.z;
	   position.w = 0.0;
	   return true;
       } else
	   return false;

    }

   /**
    * Test for intersection with a segment.
    * @param start a point defining  the start of the line segment
    * @param end a point defining the end of the line segment
    * @param position a point defining the location  of the pick w= distance to pick
    * @return true or false indicating if an intersection occured
    */
  @Override
  boolean intersect( Point3d start, Point3d end, Point4d position ) {
      double t1,t2,tmp,tnear,tfar,invDir,invMag;
      double dirx, diry, dirz;

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

      dirx = end.x - start.x;
      diry = end.y - start.y;
      dirz = end.z - start.z;

      double dirLen = dirx*dirx + diry*diry + dirz*dirz;

      // Optimization : Handle zero length direction vector.
      if(dirLen == 0.0)
	  return intersect(start, position);

      dirLen = Math.sqrt(dirLen);
      // System.err.println("dirLen is " + dirLen);
      invMag = 1.0/dirLen;
      dirx = dirx*invMag;
      diry = diry*invMag;
      dirz = dirz*invMag;

      /*
	System.err.println("dir = " + dir);
	System.err.println("start = " + start);
	System.err.println("lower = " + lower);
	System.err.println("upper = " + upper);
	*/

      // initialize tnear and tfar to handle dir.? == 0 cases
      tnear = -Double.MAX_VALUE;
      tfar = Double.MAX_VALUE;

        if(dirx == 0.0) {
            //System.err.println("dirx == 0.0");
            if (start.x < lower.x || start.x > upper.x ) {
		//System.err.println( "parallel to x plane and outside");
		return false;
            }
	} else {
            invDir = 1.0/dirx;
            t1 = (lower.x-start.x)*invDir;
            t2 = (upper.x-start.x)*invDir;

            //System.err.println("x t1 = " + t1 + " t2 = " + t2);
	    if( t1 > t2) {
		tnear = t2;
		tfar = t1;
	    }else {
		tnear = t1;
		tfar = t2;
	    }
	    if( tfar < 0.0 ) {
		//System.err.println( "x failed: tnear="+tnear+"  tfar="+tfar);
		return false;
	    }
            //System.err.println("x tnear = " + tnear + " tfar = " + tfar);
        }
	// y
        if (diry == 0.0) {
            //System.err.println("diry == 0.0");
            if( start.y < lower.y || start.y > upper.y ){
		//System.err.println( "parallel to y plane and outside");
		return false;
            }
        } else {
            invDir = 1.0/diry;
            //System.err.println("invDir = " + invDir);
            t1 = (lower.y-start.y)*invDir;
            t2 = (upper.y-start.y)*invDir;

            if( t1 > t2) {
		tmp = t1;
		t1 = t2;
		t2 = tmp;
            }
            //System.err.println("y t1 = " + t1 + " t2 = " + t2);
            if( t1 > tnear) tnear = t1;
            if( t2 < tfar ) tfar  = t2;

            if( (tfar < 0.0) ||  (tnear > tfar)){
		//System.err.println( "y failed: tnear="+tnear+"  tfar="+tfar);
		return false;
            }
            //System.err.println("y tnear = " + tnear + " tfar = " + tfar);
        }

	// z
        if (dirz == 0.0) {
            //System.err.println("dirz == 0.0");
            if( start.z < lower.z || start.z > upper.z ) {
		//System.err.println( "parallel to z plane and outside");
		return false;
            }
        }  else {
            invDir = 1.0/dirz;
            t1 = (lower.z-start.z)*invDir;
            t2 = (upper.z-start.z)*invDir;

            if( t1 > t2) {
		tmp = t1;
		t1 = t2;
		t2 = tmp;
            }
            //System.err.println("z t1 = " + t1 + " t2 = " + t2);
            if( t1 > tnear) tnear = t1;
            if( t2 < tfar ) tfar  = t2;

            if( (tfar < 0.0) ||  (tnear > tfar)){
		//System.err.println( "z failed: tnear="+tnear+"  tfar="+tfar);
		return false;
            }
            //System.err.println("z tnear = " + tnear + " tfar = " + tfar);
        }

	if((tnear < 0.0) && (tfar >= 0.0)) {
	    // origin is inside the BBox.
 	    position.x = start.x + dirx*tfar;
	    position.y = start.y + diry*tfar;
	    position.z = start.z + dirz*tfar;
	    position.w = tfar;
	}
	else {
	    if(tnear>dirLen) {
		// Segment is behind BBox.
		/*
		  System.err.println("PickSegment : intersected postion : " + position
		  + " tnear " + tnear + " tfar " + tfar );
		  */
		return false;
	    }
	    position.x = start.x + dirx*tnear;
            position.y = start.y + diry*tnear;
            position.z = start.z + dirz*tnear;

            position.w = tnear;
        }

	/*
	    System.err.println("tnear = " + tnear + " tfar = " + tfar + " w " +
	    position.w);
	    System.err.println("lower = " + lower);
	    System.err.println("upper = " + upper + "\n");
	*/
        return true;

  }

    /**
     * Test for intersection with a ray.
     * @param origin the starting point of the ray
     * @param direction the direction of the ray
     * @return true or false indicating if an intersection occured
     */
    @Override
    public boolean intersect(Point3d origin, Vector3d direction ) {

        if( boundsIsEmpty ) {
	    return false;
        }

	if( boundsIsInfinite ) {
	    return true;
	}

	Point3d p=new Point3d();
	return intersect( origin, direction, p );
    }

    /**
     * A protected intersect method that returns the point of intersection.
     * Used by Picking methods to sort or return closest picked item.
     */
    boolean intersect(Point3d origin, Vector3d direction, Point3d intersect ) {
	double theta=0.0;

        if( boundsIsEmpty ) {
	   return false;
        }

	if( boundsIsInfinite ) {
	  intersect.x = origin.x;
	  intersect.y = origin.y;
	  intersect.z = origin.z;
	  return true;
	}

	if (direction.x > 0.0 )
	    theta = Math.max( theta, (lower.x - origin.x)/direction.x );
	if (direction.x < 0.0 )
	    theta = Math.max( theta, (upper.x - origin.x)/direction.x );
	if (direction.y > 0.0 )
	    theta = Math.max( theta, (lower.y - origin.y)/direction.y );
	if (direction.y < 0.0 )
	    theta = Math.max( theta, (upper.y - origin.y)/direction.y );
	if (direction.z > 0.0 )
	    theta = Math.max( theta, (lower.z - origin.z)/direction.z );
	if (direction.z < 0.0 )
	    theta = Math.max( theta, (upper.z - origin.z)/direction.z );

	intersect.x = origin.x + theta*direction.x;
	intersect.y = origin.y + theta*direction.y;
	intersect.z = origin.z + theta*direction.z;

	if (intersect.x < (lower.x-EPS)) return false;
	if (intersect.x > (upper.x+EPS)) return false;
	if (intersect.y < (lower.y-EPS)) return false;
	if (intersect.y > (upper.y+EPS)) return false;
	if (intersect.z < (lower.z-EPS)) return false;
	if (intersect.z > (upper.z+EPS)) return false;

        return true;

    }

    /**
     * Test for intersection with a point.
     * @param point a point defining a position in 3-space
     * @return true or false indicating if an intersection occured
     */
    @Override
    public boolean intersect(Point3d point ) {

        if( boundsIsEmpty ) {
	   return false;
        }
	if( boundsIsInfinite ) {
	  return true;
	}

	if( point.x <= upper.x && point.x >= lower.x &&
	    point.y <= upper.y && point.y >= lower.y &&
	    point.z <= upper.z && point.z >= lower.z)
	    return true;
        else
	    return false;
    }
    /**
     * Tests whether the bounding box is empty.  A bounding box is
     * empty if it is null (either by construction or as the result of
     * a null intersection) or if its volume is negative.  A bounding box
     * with a volume of zero is <i>not</i> empty.
     * @return true if the bounding box is empty; otherwise, it returns false
     */
    @Override
    public boolean isEmpty() {

	 return boundsIsEmpty;
    }

   /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @return true or false indicating if an intersection occured
     */
    @Override
    boolean intersect(Bounds boundsObject, Point4d position) {
	return intersect(boundsObject);
    }

    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @return true or false indicating if an intersection occured
     */
    @Override
    public boolean intersect(Bounds boundsObject) {

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
	    // both boxes are axis aligned
	    if( upper.x > box.lower.x && box.upper.x > lower.x &&
		upper.y > box.lower.y && box.upper.y > lower.y &&
		upper.z > box.lower.z && box.upper.z > lower.z )
		return true;
	    else
		return false;
	} else if( boundsObject.boundId == BOUNDING_SPHERE) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    double rad_sq = sphere.radius*sphere.radius;
	    double dis = 0.0;

	    if( sphere.center.x < lower.x )
		dis = (sphere.center.x-lower.x)*(sphere.center.x-lower.x);
	    else
		if( sphere.center.x > upper.x )
		    dis = (sphere.center.x-upper.x)*(sphere.center.x-upper.x);

	    if( sphere.center.y < lower.y )
		dis += (sphere.center.y-lower.y)*(sphere.center.y-lower.y);
	    else
		if( sphere.center.y > upper.y )
		    dis += (sphere.center.y-upper.y)*(sphere.center.y-upper.y);

	    if( sphere.center.z < lower.z )
		dis += (sphere.center.z-lower.z)*(sphere.center.z-lower.z);
	    else
		if( sphere.center.z > upper.z )
		    dis += (sphere.center.z-upper.z)*(sphere.center.z-upper.z);

	    if( dis <= rad_sq )
		return true;
	    else
		return false;
        } else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    // intersect an axis aligned box with a polytope
	    return intersect_ptope_abox ( (BoundingPolytope)boundsObject, this );
        } else {
            throw new IllegalArgumentException(J3dI18N.getString("BoundingBox6"));
        }

    }

    /**
     * Test for intersection with an array of bounds objects.
     * @param boundsObjects an array of bounding objects
     * @return true or false indicating if an intersection occured
     */
    @Override
    public boolean intersect(Bounds[] boundsObjects) {

	int i;

	if( boundsObjects == null || boundsObjects.length <= 0  )  {
	    return false;
	}

	if( boundsIsEmpty ) {
	    return false;
	}

	for(i = 0; i < boundsObjects.length; i++){
	    if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty) ;
	    else if( boundsIsInfinite || boundsObjects[i].boundsIsInfinite ) {
		return true; // We're done here.
	    }
	    else if( boundsObjects[i].boundId == BOUNDING_BOX){
		BoundingBox box = (BoundingBox)boundsObjects[i];
		// both boxes are axis aligned
		if( upper.x > box.lower.x && box.upper.x > lower.x &&
		    upper.y > box.lower.y && box.upper.y > lower.y &&
		    upper.z > box.lower.z && box.upper.z > lower.z )
		    return true;
	    }
	    else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		double rad_sq = sphere.radius*sphere.radius;
		double dis = 0.0;

		if( sphere.center.x < lower.x )
		    dis = (sphere.center.x-lower.x)*(sphere.center.x-lower.x);
		else
		    if( sphere.center.x > upper.x )
			dis = (sphere.center.x-upper.x)*(sphere.center.x-upper.x);

		if( sphere.center.y < lower.y )
		    dis += (sphere.center.y-lower.y)*(sphere.center.y-lower.y);
		else
		    if( sphere.center.y > upper.y )
			dis += (sphere.center.y-upper.y)*(sphere.center.y-upper.y);

		if( sphere.center.z < lower.z )
		    dis += (sphere.center.z-lower.z)*(sphere.center.z-lower.z);
		else
		    if( sphere.center.z > upper.z )
			dis += (sphere.center.z-upper.z)*(sphere.center.z-upper.z);

		if( dis <= rad_sq )
		    return true;

	    }
	    else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		if( intersect_ptope_abox ( (BoundingPolytope)boundsObjects[i], this ))
		    return true;
	    }
	    else {
		//	       System.err.println("intersect ?? ");
	    }
	}

	return false;
    }

    /**
     * Test for intersection with another bounding box.
     * @param boundsObject another bounding object
     * @param newBoundBox the new bounding box which is the intersection of
     *        the boundsObject and this BoundingBox
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds boundsObject, BoundingBox newBoundBox) {

	if((boundsObject == null) || boundsIsEmpty || boundsObject.boundsIsEmpty ) {
		newBoundBox.set((Bounds)null);
	    return false;
        }


	if(boundsIsInfinite && (!boundsObject.boundsIsInfinite)) {
	    newBoundBox.set(boundsObject);
	    return true;
	}
	else if((!boundsIsInfinite) && boundsObject.boundsIsInfinite) {
	    newBoundBox.set(this);
	    return true;
	}
	else if(boundsIsInfinite && boundsObject.boundsIsInfinite) {
	    newBoundBox.set(this);
	    return true;
	}
	else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;
	    // both boxes are axis aligned
	    if( upper.x > box.lower.x && box.upper.x > lower.x &&
		upper.y > box.lower.y && box.upper.y > lower.y &&
		upper.z > box.lower.z && box.upper.z > lower.z ){


		if(upper.x > box.upper.x)
		    newBoundBox.upper.x = box.upper.x;
		else
		    newBoundBox.upper.x = upper.x;

		if(upper.y > box.upper.y)
		    newBoundBox.upper.y = box.upper.y;
		else
		    newBoundBox.upper.y = upper.y;

		if(upper.z > box.upper.z)
		    newBoundBox.upper.z = box.upper.z;
		else
		    newBoundBox.upper.z = upper.z;

		if(lower.x < box.lower.x)
		    newBoundBox.lower.x = box.lower.x;
		else
		    newBoundBox.lower.x = lower.x;

		if(lower.y < box.lower.y)
		    newBoundBox.lower.y = box.lower.y;
		else
		    newBoundBox.lower.y = lower.y;

		if(lower.z < box.lower.z)
		    newBoundBox.lower.z = box.lower.z;
		else
		    newBoundBox.lower.z = lower.z;

		newBoundBox.updateBoundsStates();
		return true;
	    } else {
		// Negative volume.
			newBoundBox.set((Bounds)null);
		return false;
	    }
        }
	else if( boundsObject.boundId == BOUNDING_SPHERE) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    if( this.intersect( sphere) ) {
		BoundingBox sbox = new BoundingBox( sphere );
		this.intersect( sbox, newBoundBox );
		return true;
	    } else {
		// Negative volume.
			newBoundBox.set((Bounds)null);
		return false;
	    }

            //      System.err.println("intersect Sphere ");
        }
	else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    if( this.intersect( polytope)) {
		BoundingBox pbox = new BoundingBox( polytope); // convert polytope to box
		this.intersect( pbox, newBoundBox );
		return true;
	    } else {
		// Negative volume.
			newBoundBox.set((Bounds)null);
		return false;
	    }
        } else {
            throw new IllegalArgumentException(J3dI18N.getString("BoundingBox7"));
        }
    }

    /**
     * Test for intersection with an array of  bounds objects.
     * @param boundsObjects an array of  bounds objects
     * @param newBoundBox the new bounding box which is the intersection of
     *	      the boundsObject and this BoundingBox
     * @return true or false indicating if an intersection occured
     */
    public boolean intersect(Bounds[] boundsObjects, BoundingBox newBoundBox) {

       if( boundsObjects == null || boundsObjects.length <= 0 ||  boundsIsEmpty ) {
		// Negative volume.
		newBoundBox.set((Bounds)null);
		return false;
       }

       int i=0;
       // find first non null bounds object
       while( boundsObjects[i] == null && i < boundsObjects.length) {
	   i++;
       }

       if( i >= boundsObjects.length ) { // all bounds objects were empty
		// Negative volume.
		newBoundBox.set((Bounds)null);
	   return false;
       }


       boolean status = false;
       BoundingBox tbox = new BoundingBox();

       for(;i<boundsObjects.length;i++) {
	   if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty) ;
	   else if( boundsObjects[i].boundId == BOUNDING_BOX){
	       BoundingBox box = (BoundingBox)boundsObjects[i];
	       // both boxes are axis aligned
	       if( upper.x > box.lower.x && box.upper.x > lower.x &&
		   upper.y > box.lower.y && box.upper.y > lower.y &&
		   upper.z > box.lower.z && box.upper.z > lower.z ){

		   if(upper.x > box.upper.x)
		       newBoundBox.upper.x = box.upper.x;
		   else
		       newBoundBox.upper.x = upper.x;

		   if(upper.y > box.upper.y)
		       newBoundBox.upper.y = box.upper.y;
		   else
		       newBoundBox.upper.y = upper.y;

		   if(upper.z > box.upper.z)
		       newBoundBox.upper.z = box.upper.z;
		   else
		       newBoundBox.upper.z = upper.z;

		   if(lower.x < box.lower.x)
		       newBoundBox.lower.x = box.lower.x;
		   else
		       newBoundBox.lower.x = lower.x;

		   if(lower.y < box.lower.y)
		       newBoundBox.lower.y = box.lower.y;
		   else
		       newBoundBox.lower.y = lower.y;

		   if(lower.z < box.lower.z)
		       newBoundBox.lower.z = box.lower.z;
		   else
		       newBoundBox.lower.z = lower.z;
		   status = true;
		   newBoundBox.updateBoundsStates();
	       }
	   }
	   else if( boundsObjects[i].boundId == BOUNDING_SPHERE) {
	       BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
	       if( this.intersect(sphere)) {
		   BoundingBox sbox = new BoundingBox( sphere ); // convert sphere to box
		   this.intersect(sbox,tbox); // insersect two boxes
		   if( status ) {
		       newBoundBox.combine( tbox );
		   } else {
		       newBoundBox.set( tbox );
		       status = true;
		   }
	       }

	   }
	   else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
	       BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
	       if( this.intersect( polytope)) {
		   BoundingBox pbox = new BoundingBox( polytope ); // convert polytope to box
		   this.intersect(pbox,tbox); // insersect two boxes
		   if ( status ) {
		       newBoundBox.combine( tbox );
		   } else {
		       newBoundBox.set( tbox );
		       status = true;
		   }
	       }
	   } else {
	       throw new IllegalArgumentException(J3dI18N.getString("BoundingBox6"));
	   }

	   if(newBoundBox.boundsIsInfinite)
	       break; // We're done.
       }
       if( status == false ) {
	   // Negative volume.
		newBoundBox.set((Bounds)null);
       }
       return status;
    }


    /**
     * Finds closest bounding object that intersects this bounding box.
     * @param boundsObjects an array of bounds objects
     * @return closest bounding object
     */
    @Override
    public Bounds closestIntersection( Bounds[] boundsObjects) {

	if( boundsObjects == null || boundsObjects.length <= 0  ) {
	    return null;
        }

        if( boundsIsEmpty ) {
	    return null;
        }

	Point3d centroid = getCenter();

	double dis;
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
		if( boundsObjects[i].boundId == BOUNDING_BOX){
		    BoundingBox box = (BoundingBox)boundsObjects[i];
		    cenX = (box.upper.x+box.lower.x)/2.0;
		    cenY = (box.upper.y+box.lower.y)/2.0;
		    cenZ = (box.upper.z+box.lower.z)/2.0;
		    dis = Math.sqrt( (centroid.x-cenX)*(centroid.x-cenX) +
				     (centroid.y-cenY)*(centroid.y-cenY) +
				     (centroid.z-cenZ)*(centroid.z-cenZ) );
		    inside = false;

		    if( lower.x <= box.lower.x &&
			lower.y <= box.lower.y &&
			lower.z <= box.lower.z &&
			upper.x >= box.upper.x &&
			upper.y >= box.upper.y &&
			upper.z >= box.upper.z ) { // box is contained
			inside = true;
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

		}
		else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		    BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		    dis = Math.sqrt( (centroid.x-sphere.center.x)*
				     (centroid.x-sphere.center.x) +
				     (centroid.y-sphere.center.y)*
				     (centroid.y-sphere.center.y) +
				     (centroid.z-sphere.center.z)*
				     (centroid.z-sphere.center.z) );

		    inside = false;

		    // sphere sphere.center is inside box
		    if(sphere.center.x <= upper.x && sphere.center.x >= lower.x &&
		       sphere.center.y <= upper.y && sphere.center.y >= lower.y &&
		       sphere.center.z <= upper.z && sphere.center.z >= lower.z ) {
			// check if sphere intersects any side
			if (sphere.center.x - lower.x >= sphere.radius &&
			    upper.x - sphere.center.x >= sphere.radius &&
			    sphere.center.y - lower.y >= sphere.radius &&
			    upper.y - sphere.center.y >= sphere.radius &&
			    sphere.center.z - lower.z >= sphere.radius &&
			    upper.z - sphere.center.z >= sphere.radius  ) {
			    // contains the sphere
			    inside = true;
			}
		    }
		    if (inside ) {
			// initialize smallest_distance for the first containment
			if( !contains ){
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
		}
		else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		    BoundingPolytope polytope =
			(BoundingPolytope)boundsObjects[i];
		    dis = Math.sqrt( (centroid.x-polytope.centroid.x)*
				     (centroid.x-polytope.centroid.x) +
				     (centroid.y-polytope.centroid.y)*
				     (centroid.y-polytope.centroid.y) +
				     (centroid.z-polytope.centroid.z)*
				     (centroid.z-polytope.centroid.z) );
		    inside = true;
		    for(j=0;j<polytope.nVerts;j++) {
			if( polytope.verts[j].x < lower.x ||
			    polytope.verts[j].y < lower.y ||
			    polytope.verts[j].z < lower.z ||
			    polytope.verts[j].x > upper.x ||
			    polytope.verts[j].y > upper.y ||
			    polytope.verts[j].z > upper.z ) { // box contains polytope
			    inside = false;

			}

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
		    throw new IllegalArgumentException(J3dI18N.getString("BoundingBox9"));
		}
	    }
	}

	if ( intersect )
            return boundsObjects[index];
	else
	    return null;
    }

    /**
     * Tests for intersection of box and frustum.
     * @param frustum
     * @return true if they intersect
     */
    boolean intersect(CachedFrustum frustum ) {

	if (boundsIsEmpty)
	    return false;

	if(boundsIsInfinite)
	    return true;

	// System.err.println("intersect frustum with box="+this.toString());
	// System.err.println("frustum "+frustum.toString());
	// check if box and bounding box  of frustum intersect
        if ((upper.x < frustum.lower.x) ||
	    (lower.x > frustum.upper.x) ||
            (upper.y < frustum.lower.y) ||
	    (lower.y > frustum.upper.y) ||
            (upper.z < frustum.lower.z) ||
	    (lower.z > frustum.upper.z) ) {

	    // System.err.println("*** box and bounding box of frustum do not intersect");
	    return false;
	}

	// check if all box points out any frustum plane
	int i = 5;
	while (i>=0){
	    Vector4d vc = frustum.clipPlanes[i--];
	    if ((( upper.x*vc.x + upper.y*vc.y +
		   upper.z*vc.z + vc.w ) < 0.0 ) &&
		(( upper.x*vc.x + lower.y*vc.y +
		   upper.z*vc.z + vc.w ) < 0.0 ) &&
		(( upper.x*vc.x + lower.y*vc.y +
		   lower.z*vc.z + vc.w ) < 0.0 ) &&
		(( upper.x*vc.x + upper.y*vc.y +
		   lower.z*vc.z + vc.w ) < 0.0 ) &&
		(( lower.x*vc.x + upper.y*vc.y +
		   upper.z*vc.z + vc.w ) < 0.0 ) &&
		(( lower.x*vc.x + lower.y*vc.y +
		   upper.z*vc.z + vc.w ) < 0.0 ) &&
		(( lower.x*vc.x + lower.y*vc.y +
		   lower.z*vc.z + vc.w ) < 0.0 ) &&
		(( lower.x*vc.x +  upper.y*vc.y +
		   lower.z*vc.z + vc.w ) < 0.0 )) {
		// all corners outside this frustum plane
		// System.err.println("*** all corners outside this frustum plane");
		return false;
	    }
	}

	return true;
    }

    /**
     * Returns a string representation of this class.
     */
    @Override
    public String toString() {
	return new String( "Bounding box: Lower="+lower.x+" "+
			   lower.y+" "+lower.z+" Upper="+upper.x+" "+
			   upper.y+" "+upper.z  );
    }

private void setEmptyBounds() {
	lower.set( 1.0d,  1.0d,  1.0d);
	upper.set(-1.0d, -1.0d, -1.0d);
	boundsIsInfinite = false;
	boundsIsEmpty = true;
}

private void setInfiniteBounds() {
	lower.set(Double.NEGATIVE_INFINITY,
	          Double.NEGATIVE_INFINITY,
	          Double.NEGATIVE_INFINITY);
	upper.set(Double.POSITIVE_INFINITY,
	          Double.POSITIVE_INFINITY,
	          Double.POSITIVE_INFINITY);

	boundsIsInfinite = true;
	boundsIsEmpty = false;
}

    private void updateBoundsStates() {
	if((lower.x == Double.NEGATIVE_INFINITY) &&
	   (lower.y == Double.NEGATIVE_INFINITY) &&
	   (lower.z == Double.NEGATIVE_INFINITY) &&
	   (upper.x == Double.POSITIVE_INFINITY) &&
	   (upper.y == Double.POSITIVE_INFINITY) &&
	   (upper.z == Double.POSITIVE_INFINITY)) {
	    boundsIsEmpty = false;
	    boundsIsInfinite = true;
	    return;
	}

	if (Double.isNaN(lower.x + lower.y + lower.z + upper.x + upper.y + upper.z)) {
	     boundsIsEmpty = true;
	     boundsIsInfinite = false;
	     return;
	}
	else {
	    boundsIsInfinite = false;
	    if( lower.x > upper.x ||
		lower.y > upper.y ||
		lower.z > upper.z ) {
		boundsIsEmpty = true;
	    } else {
		boundsIsEmpty = false;
	    }
	}
    }

// For a infinite bounds. What is the centroid ?
@Override
Point3d getCenter() {
	Point3d cent = new Point3d();
	cent.add(upper, lower);
	cent.scale(0.5d);
	return cent;
}

@Override
public void getCenter(Point3d center) {
	center.add(lower, upper);
	center.scale(0.5d);
}

    void translate(BoundingBox bbox, Vector3d value) {
	if (bbox == null || bbox.boundsIsEmpty) {
		setEmptyBounds();
		return;
	}

	if (bbox.boundsIsInfinite) {
		setInfiniteBounds();
		return;
	}

	lower.x = bbox.lower.x + value.x;
	lower.y = bbox.lower.y + value.y;
	lower.z = bbox.lower.z + value.z;
	upper.x = bbox.upper.x + value.x;
	upper.y = bbox.upper.y + value.y;
	upper.z = bbox.upper.z + value.z;
    }


    /**
     * if the passed the "region" is same type as this object
     * then do a copy, otherwise clone the Bounds  and
     * return
     */
    @Override
    Bounds copy(Bounds r) {
	if (r != null && this.boundId == r.boundId) {
	    BoundingBox region = (BoundingBox) r;
	    region.lower.x = lower.x;
	    region.lower.y = lower.y;
	    region.lower.z = lower.z;
	    region.upper.x = upper.x;
	    region.upper.y = upper.y;
	    region.upper.z = upper.z;
	    region.boundsIsEmpty = boundsIsEmpty;
	    region.boundsIsInfinite = boundsIsInfinite;
	    return region;
	}
	else {
	    return (Bounds) this.clone();
	}
    }

    @Override
    int getPickType() {
	return PickShape.PICKBOUNDINGBOX;
    }
}

