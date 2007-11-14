/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.lang.Math;

/**
 * The abstract base class for bounds objects.  Bounds objects define
 * a convex, closed volume that is used for various intersection and
 * culling operations.
 */

public abstract class Bounds extends Object implements Cloneable {
    static final double EPSILON = .000001;
    static final boolean debug = false;

    static final int BOUNDING_BOX = 0x1;
    static final int BOUNDING_SPHERE = 0x2;
    static final int BOUNDING_POLYTOPE = 0x4;

    boolean boundsIsEmpty = false;
    boolean boundsIsInfinite = false;
    int  boundId = 0;

    /**
     * Constructs a new Bounds object.
     */
    public Bounds() {
    }


    /**
     * Makes a copy of a bounds object.
     */
    public abstract Object clone();


    /**
     * Indicates whether the specified <code>bounds</code> object is
     * equal to this Bounds object.  They are equal if both the
     * specified <code>bounds</code> object and this Bounds are
     * instances of the same Bounds subclass and all of the data
     * members of <code>bounds</code> are equal to the corresponding
     * data members in this Bounds.
     * @param bounds the object with which the comparison is made.
     * @return true if this Bounds object is equal to <code>bounds</code>;
     * otherwise false
     *
     * @since Java 3D 1.2
     */
    public abstract boolean equals(Object bounds);


    /**
     * Returns a hash code for this Bounds object based on the
     * data values in this object.  Two different Bounds objects of
     * the same type with identical data values (i.e., Bounds.equals
     * returns true) will return the same hash code.  Two Bounds
     * objects with different data members may return the same hash code
     * value, although this is not likely.
     * @return a hash code for this Bounds object.
     *
     * @since Java 3D 1.2
     */
    public abstract int hashCode();


  /**
   * Test for intersection with a ray.
   * @param origin the starting point of the ray
   * @param direction the direction of the ray
   * @return true or false indicating if an intersection occured
   */
  public abstract boolean intersect( Point3d origin, Vector3d direction );

   /**
    * Test for intersection with a point.
    * @param point a point defining a position in 3-space
    * @return true or false indicating if an intersection occured
    */
  public abstract boolean intersect( Point3d point );

  /**
   * Test for intersection with a ray
   * @param origin is a the starting point of the ray
   * @param direction is the direction of the ray
   * @param position is a point defining the location  of the pick w= distance to pick
   * @return true or false indicating if an intersection occured
   */
  abstract boolean intersect( Point3d origin, Vector3d direction, Point4d position );

   /**
    * Test for intersection with a point
    * @param point is a point defining a position in 3-space
    * @param position is a point defining the location  of the pick w= distance to pick
    * @return true or false indicating if an intersection occured
    */
  abstract boolean intersect( Point3d point, Point4d position);

   /**
    * Test for intersection with a segment
    * @param start is a point defining  the start of the line segment
    * @param end is a point defining the end of the line segment
    * @param position is a point defining the location  of the pick w= distance to pick
    * @return true or false indicating if an intersection occured
    */
  abstract boolean intersect( Point3d start, Point3d end, Point4d position );

   /**
     * Test for intersection with another bounds object
     *
     * Test for intersection with another bounds object
     * @param boundsObject is another bounds object
     * @return true or false indicating if an intersection occured
     */
  abstract boolean intersect( Bounds boundsObject, Point4d position );

    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @return true or false indicating if an intersection occurred
     */
  public abstract boolean intersect( Bounds boundsObject );

    /**
     * Test for intersection with another bounds object.
     * @param boundsObjects an array of bounding objects
     * @return true or false indicating if an intersection occured
     */
  public abstract boolean intersect( Bounds[] boundsObjects );


     /**
     * Finds closest bounding object that intersects this bounding object.
     * @param boundsObjects an array of  bounds objects
     * @return closest bounding object
     */
  public abstract Bounds closestIntersection( Bounds[] boundsObjects);

     /**
     * Returns the center of the bounds
     * @return bounds center
     */
  abstract Point3d getCenter();

     /**
      * Combines this bounding object with a bounding object so that the
      * resulting bounding object encloses the original bounding object and the
      * given bounds object.
      * @param boundsObject another bounds object
      */
  public abstract void combine( Bounds   boundsObject );


     /**
      * Combines this bounding object with an array of bounding objects so that the
      * resulting bounding object encloses the original bounding object and the
      * given array of bounds object.
      * @param boundsObjects an array of bounds objects
      */
  public abstract void combine( Bounds[] boundsObjects);

     /**
      * Combines this bounding object with a point.
      * @param point a 3d point in space
      */
  public abstract void combine( Point3d    point);

     /**
      * Combines this bounding object with an array of points.
      * @param points an array of 3d points in space
      */
  public abstract void combine( Point3d[]  points);

  /**
   * Transforms this bounding object by the given matrix.
   * @param trans the transformation matrix
   */
  public abstract void transform(Transform3D trans);

     /**
      * Modifies the bounding object so that it bounds the volume
      * generated by transforming the given bounding object.
      * @param bounds the bounding object to be transformed
      * @param trans the transformation matrix
      */
  public abstract void transform( Bounds bounds, Transform3D trans);

    /**
     * Tests whether the bounds is empty.  A bounds is
     * empty if it is null (either by construction or as the result of
     * a null intersection) or if its volume is negative.  A bounds
     * with a volume of zero is <i>not</i> empty.
     * @return true if the bounds is empty; otherwise, it returns false
     */
    public abstract boolean isEmpty();

  /**
   * Sets the value of this Bounds object.
   * @param boundsObject another bounds object.
   */
  public abstract void set( Bounds boundsObject);


    abstract Bounds copy(Bounds region);


    private void test_point(Vector4d[] planes, Point3d new_point) {
	for (int i = 0; i < planes.length; i++){
	    double dist = (new_point.x*planes[i].x + new_point.y*planes[i].y +
		    new_point.z*planes[i].z + planes[i].w ) ;
	    if (dist > EPSILON ){
		System.err.println("new point is outside of" +
			" plane["+i+"] dist = " + dist);
	    }
	}
    }

    /**
     * computes the closest point from the given point to a set of planes
     * (polytope)
     * @param g the point
     * @param planes array of bounding planes
     * @param new_point point on planes closest g
     */
    boolean closest_point( Point3d g, Vector4d[] planes, Point3d new_point ) {

	double t,s,dist,w;
	boolean converged, inside, firstPoint, firstInside;
	int i,count;
	double ab,ac,bc,ad,bd,cd,aa,bb,cc;
	double b1,b2,b3,d1,d2,d3,y1,y2,y3;
	double h11,h12,h13,h22,h23,h33;
	double l12,l13,l23;
	Point3d n = new Point3d();
	Point3d p = new Point3d();
	Vector3d delta = null;

	// These are temporary until the solve code is working


	/*
	 * The algorithm:
	 * We want to find the point "n", closest to "g", while still within
	 * the the polytope defined by "planes".  We find the solution by
	 * minimizing the value for a "penalty function";
	 *
	 * f = distance(n,g)^2 + sum for each i: w(distance(n, planes[i]))
	 *
	 * Where "w" is a weighting which indicates how much more important
	 * it is to be close to the planes than it is to be close to "g".
	 *
	 * We minimize this function by taking it's derivitive, and then
	 * solving for the value of n when the derivitive equals 0.
	 *
	 * For the 1D case with a single plane (a,b,c,d),  x = n.x and g = g.x,
	 * this looks like:
	 *
	 *    f(x) = (x - g) ^ 2 + w(ax + d)^2
	 *    f'(x) = 2x -2g + 2waax + 2wad
	 *
	 * (note aa = a^2) setting f'(x) = 0 gives:
	 *
	 *    (1 + waa)x = g - wad
	 *
	 * Note that the solution is just outside the plane [a, d]. With the
	 * correct choice of w, this should be inside of the EPSILON tolerance
	 * outside the planes.
	 *
	 * Extending to 3D gives the matrix solution:
	 *
	 *      | (1 + waa)  wab        wac       |
	 *  H = | wab        (1 + wbb)  wbc	  |
	 *      | wac        wbc        (1 + wcc) |
	 *
	 *  b = [g.x - wad, g.y - wbd, g.z - wcd]
	 *
	 *  H * n = b
	 *
	 *  n = b * H.inverse()
	 *
	 *  The implementation speeds this process up by recognizing that
	 *  H is symmetric, so that it can be decomposed into three matrices:
	 *
	 *  H = L * D * L.transpose()
	 *
	 *      1.0 0.0 0.0       d1  0.0 0.0
	 *  L = l12 1.0 0.0  D =  0.0 d2  0.0
	 *      l13 l23 1.0       0.0 0.0 d3
	 *
	 *  n can then be derived by back-substitution, where the original
	 *  problem is decomposed as:
	 *
	 *  H * n = b
	 *  L * D * L.transpose() * n = b
	 *  L * D * y = b;   L.transpose() * n = y
	 *
	 *  We can then multiply out the terms of L * D and solve for y, and
	 *  then use y to solve for n.
	 */

	w=100.0 / EPSILON;  // must be large enough to ensure that solution
			    // is within EPSILON of planes

	count = 0;
	p.set(g);

	if (debug) {
	    System.err.println("closest_point():\nincoming g="+" "+g.x+" "+g.y+
		    " "+g.z);
	}

	converged = false;
	firstPoint = true;
	firstInside = false;

	Vector4d pln;

	while( !converged ) {
	    if (debug) {
		System.err.println("start: p="+" "+p.x+" "+p.y+" "+p.z);
	    }

	    // test the current point against the planes, for each
	    // plane that is violated, add it's contribution to the
	    // penalty function
	    inside = true;
	    aa=0.0; bb=0.0; cc=0.0;
	    ab=0.0; ac=0.0; bc=0.0; ad=0.0; bd=0.0; cd=0.0;
	    for(i = 0; i < planes.length; i++){
		pln = planes[i];
		dist = (p.x*pln.x + p.y*pln.y +
			p.z*pln.z + pln.w ) ;
		// if point is outside or within EPSILON of the boundary, add
		// the plane to the penalty matrix.  We do this even if the
		// point is already inside the polytope to prevent numerical
		// instablity in cases where the point is just outside the
		// boundary of several planes of the polytope
		if (dist > -EPSILON ){
		    aa = aa + pln.x * pln.x;
		    bb = bb + pln.y * pln.y;
		    cc = cc + pln.z * pln.z;
		    ab = ab + pln.x * pln.y;
		    ac = ac + pln.x * pln.z;
		    bc = bc + pln.y * pln.z;
		    ad = ad + pln.x * pln.w;
		    bd = bd + pln.y * pln.w;
		    cd = cd + pln.z * pln.w;
		}
		// If the point is inside if dist is <= EPSILON
		if (dist > EPSILON ){
		    inside = false;
		    if (debug) {
			System.err.println("point outside plane["+i+"]=("+
			    pln.x+ ","+pln.y+",\n\t"+pln.z+
			    ","+ pln.w+")\ndist = " + dist);
		    }
		}
	    }
	    // see if we are done
	    if (inside) {
		if (debug) {
		    System.err.println("p is inside");
		}
		if (firstPoint) {
		    firstInside = true;
		}
		new_point.set(p);
		converged = true;
	    } else { // solve for a closer point
		firstPoint = false;

		// this is the upper right corner of H, which is all we
		// need to do the decomposition since the matrix is symetric
		h11 = 1.0 + aa * w;
		h12 =       ab * w;
		h13 =       ac * w;
		h22 = 1.0 + bb * w;
		h23 =       bc * w;
		h33 = 1.0 + cc * w;

		if (debug) {
		    System.err.println(" hessin= ");
		    System.err.println(h11+" "+h12+" "+h13);
		    System.err.println("     "+h22+" "+h23);
		    System.err.println("           "+h33);
		}

		// these are the constant terms
		b1 = g.x - w * ad;
		b2 = g.y - w * bd;
		b3 = g.z - w * cd;

		if (debug) {
		    System.err.println(" b1,b2,b3 = "+b1+" "+b2+" " +b3);
		}

		// solve, d1, d2, d3 actually 1/dx, which is more useful
		d1 = 1/h11;
		l12 = d1 * h12;
		l13 = d1 * h13;
		s = h22-l12*h12;
		d2 = 1/s;
		t = h23-h12*l13;
		l23 = d2 * t;
		d3 = 1/(h33 - h13*l13 - t*l23);

		if (debug) {
		    System.err.println(" l12,l13,l23 "+l12+" "+l13+" "+l23);
		    System.err.println(" d1,d2,d3 "+ d1+" "+d2+" "+d3);
		}

		// we have L and D, now solve for y
		y1 = d1 * b1;
		y2 = d2 * (b2 - h12*y1);
		y3 = d3 * (b3 - h13*y1 - t*y2);

		if (debug) {
		    System.err.println(" y1,y2,y3 = "+y1+" "+y2+" "+y3);
		}

		// we have y, solve for n
		n.z = y3;
		n.y = (y2 - l23*n.z);
		n.x = (y1 - l13*n.z - l12*n.y);

		if (debug) {
		    System.err.println("new point = " + n.x+" " + n.y+" " +
			n.z);
		    test_point(planes, n);

		    if (delta == null) delta = new Vector3d();
		    delta.sub(n, p);
		    delta.normalize();
		    System.err.println("p->n direction: " + delta);
		    Matrix3d hMatrix = new Matrix3d();
		    // check using the the javax.vecmath routine
		    hMatrix.m00 = h11;
		    hMatrix.m01 = h12;
		    hMatrix.m02 = h13;
		    hMatrix.m10 = h12; // h21 = h12
		    hMatrix.m11 = h22;
		    hMatrix.m12 = h23;
		    hMatrix.m20 = h13; // h31 = h13
		    hMatrix.m21 = h23; // h32 = h22
		    hMatrix.m22 = h33;
		    hMatrix.invert();
		    Point3d check = new Point3d(b1, b2, b3);
		    hMatrix.transform(check);

		    System.err.println("check point = " + check.x+" " +
			check.y+" " + check.z);
		}

		// see if we have converged yet
		dist = (p.x-n.x)*(p.x-n.x) + (p.y-n.y)*(p.y-n.y) +
		    (p.z-n.z)*(p.z-n.z);

		if (debug) {
		    System.err.println("p->n distance =" + dist );
		}

		if( dist < EPSILON) { // close enough
		    converged = true;
		    new_point.set(n);
		} else {
		    p.set(n);
		    count++;
		    if(count > 4 ){ // watch for cycling between two minimums
			new_point.set(n);
			converged = true;
		    }
		}
	    }
	}
	if (debug) {
	    System.err.println("returning pnt ("+new_point.x+" "+
		    new_point.y+" "+new_point.z+")");

	    if(firstInside) System.err.println("input point inside polytope ");
	}
	return firstInside;
    }

    boolean intersect_ptope_sphere( BoundingPolytope polyTope,
				BoundingSphere sphere) {
	Point3d p = new Point3d();
	boolean inside;


	if (debug) {
	    System.err.println("ptope_sphere intersect sphere ="+sphere);
	}
	inside = closest_point( sphere.center, polyTope.planes, p );
	if (debug) {
	    System.err.println("ptope sphere intersect point ="+p);
	}
	if (!inside){
	    // if distance between polytope and sphere center is greater than
	    // radius then no intersection
	    if (p.distanceSquared( sphere.center) >
					sphere.radius*sphere.radius){
		if (debug) {
		    System.err.println("ptope_sphere returns false");
		}
		return false;
	    } else {
		if (debug) {
		    System.err.println("ptope_sphere returns true");
		}
		return true;
	    }
	} else {
	    if (debug) {
		System.err.println("ptope_sphere returns true");
	    }
	    return true;
	}
    }

    boolean intersect_ptope_abox( BoundingPolytope polyTope, BoundingBox box) {
         Vector4d planes[] = new Vector4d[6];

	if (debug) {
	    System.err.println("ptope_abox, box = " + box);
	}
	planes[0] = new Vector4d( -1.0, 0.0, 0.0, box.lower.x);
	planes[1] = new Vector4d(  1.0, 0.0, 0.0,-box.upper.x);
	planes[2] = new Vector4d(  0.0,-1.0, 0.0, box.lower.y);
	planes[3] = new Vector4d(  0.0, 1.0, 0.0,-box.upper.y);
	planes[4] = new Vector4d(  0.0, 0.0,-1.0, box.lower.z);
	planes[5] = new Vector4d(  0.0, 0.0, 1.0,-box.upper.z);


	BoundingPolytope pbox = new BoundingPolytope( planes);

	boolean result = intersect_ptope_ptope( polyTope, pbox );
	if (debug) {
	    System.err.println("ptope_abox returns " + result);
	}
	return(result);
    }


    boolean intersect_ptope_ptope( BoundingPolytope poly1,
					BoundingPolytope poly2) {
	boolean intersect;
	Point3d p = new Point3d();
	Point3d g = new Point3d();
	Point3d gnew = new Point3d();
	Point3d pnew = new Point3d();

	intersect = false;

	p.x = 0.0;
	p.y = 0.0;
	p.z = 0.0;

	//  start from an arbitrary point on poly1
	closest_point( p, poly1.planes, g);

	// get the closest points on each polytope
	if (debug) {
	    System.err.println("ptope_ptope: first g = "+g);
	}
	intersect = closest_point( g, poly2.planes, p);

	if (intersect) {
	    return true;
	}

	if (debug) {
	    System.err.println("first p = "+p+"\n");
	}
	 intersect = closest_point( p, poly1.planes, gnew);
	if (debug) {
	    System.err.println("gnew = "+gnew+" intersect="+intersect);
	}

	// loop until the closest points on the two polytopes are not changing

	double prevDist = p.distanceSquared(g);
	double dist;

	while( !intersect ) {

	    dist = p.distanceSquared(gnew);

	    if (dist < prevDist) {
		g.set(gnew);
		intersect = closest_point( g, poly2.planes, pnew );
		if (debug) {
		    System.err.println("pnew = "+pnew+" intersect="+intersect);
		}
	    } else {
		g.set(gnew);
		break;
	    }
	    prevDist = dist;
	    dist =  pnew.distanceSquared(g);

	    if (dist < prevDist) {
		p.set(pnew);
		if( !intersect ) {
		    intersect = closest_point( p, poly1.planes, gnew );
		    if (debug) {
			System.err.println("gnew = "+gnew+" intersect="+
			    intersect);
		    }
		}
	    } else {
		p.set(pnew);
		break;
	    }
	    prevDist = dist;
	}

	if (debug) {
	    System.err.println("gnew="+" "+gnew.x+" "+gnew.y+" "+gnew.z);
	    System.err.println("pnew="+" "+pnew.x+" "+pnew.y+" "+pnew.z);
	}
	return intersect;
    }


    synchronized void setWithLock(Bounds b) {
	this.set(b);
    }

    synchronized void getWithLock(Bounds b) {
	b.set(this);
    }

    // Return one of Pick Bounds type define in PickShape
    abstract int getPickType();
}
