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
import java.lang.Math;

/**
 * A BoundingPolytope defines a polyhedral bounding region using the
 * intersection of four or more half spaces.  The region defined by a
 * BoundingPolytope is always convex and must be closed.
 * <p>
 * Each plane in the BoundingPolytope specifies a half-space defined
 * by the equation:
 * <ul>
 * Ax + By + Cz + D <= 0
 * </ul>
 * where A, B, C, D are the parameters that specify the plane.  The
 * parameters are passed in the x, y, z, and w fields, respectively,
 * of a Vector4d object.  The intersection of the set of half-spaces
 * corresponding to the planes in this BoundingPolytope defines the
 * bounding region.
 */

public class BoundingPolytope extends Bounds {
	
    /**
     * An array of bounding planes.
     */
    Vector4d[]    planes;     
    double[]      mag;        // magnitude of plane vector
    double[]      pDotN;      // point on plane dotted with normal
    Point3d[]     verts;      // vertices of polytope
    int           nVerts;     // number of verts in polytope
    Point3d       centroid = new Point3d();   // centroid of polytope
    
    Point3d boxVerts[];
    boolean allocBoxVerts = false;
	
    /**
     * Constructs a BoundingPolytope using the specified planes.
     * @param planes a set of planes defining the polytope.
     * @exception IllegalArgumentException if the length of the
     * specified array of planes is less than 4.
     */
    public BoundingPolytope(Vector4d[] planes) {
	if (planes.length < 4) {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope11"));
	}

	boundId = BOUNDING_POLYTOPE;
	int i;
	double invMag;
	this.planes = new Vector4d[planes.length];
	mag = new double[planes.length];
	pDotN  = new double[planes.length];
	
	for(i=0;i<planes.length;i++) {
	    
	    // normalize the plane normals
	    mag[i] = Math.sqrt(planes[i].x*planes[i].x + planes[i].y*planes[i].y +
			       planes[i].z*planes[i].z);
	    invMag = 1.0/mag[i];
	    this.planes[i] = new Vector4d( planes[i].x*invMag, planes[i].y*invMag,
					   planes[i].z*invMag, planes[i].w*invMag );
	    
	}
	computeAllVerts();  // TODO lazy evaluate
    }

    /**
     * Constructs a BoundingPolytope and initializes it to a set of 6
     * planes that defines a cube such that -1 <= x,y,z <= 1.  The
     * values of the planes are as follows:
     * <ul>
     * planes[0] : x <= 1 (1,0,0,-1)<br>
     * planes[1] : -x <= 1 (-1,0,0,-1)<br>
     * planes[2] : y <= 1 (0,1,0,-1)<br>
     * planes[3] : -y <= 1 (0,-1,0,-1)<br>
     * planes[4] : z <= 1 (0,0,1,-1)<br>
     * planes[5] : -z <= 1 (0,0,-1,-1)<br>
     * </ul>
     */
    public BoundingPolytope() {
	boundId = BOUNDING_POLYTOPE;
	planes = new Vector4d[6];
	mag = new double[planes.length];
	pDotN  = new double[planes.length];

	planes[0] = new Vector4d( 1.0, 0.0, 0.0, -1.0 );
	planes[1] = new Vector4d(-1.0, 0.0, 0.0, -1.0 );
	planes[2] = new Vector4d( 0.0, 1.0, 0.0, -1.0 );
	planes[3] = new Vector4d( 0.0,-1.0, 0.0, -1.0 );
	planes[4] = new Vector4d( 0.0, 0.0, 1.0, -1.0 );
	planes[5] = new Vector4d( 0.0, 0.0,-1.0, -1.0 );
	mag[0] = 1.0;
	mag[1] = 1.0;
	mag[2] = 1.0;
	mag[3] = 1.0;
	mag[4] = 1.0;
	mag[5] = 1.0;

	computeAllVerts(); // TODO lazy evaluate
    }


    /**
     * Constructs a BoundingPolytope from the specified bounds object.
     * The new polytope will circumscribe the region specified by the
     * input bounds.
     * @param boundsObject the bounds object from which this polytope
     * is constructed.
     */
    public BoundingPolytope(Bounds boundsObject ) {
	int i;

	boundId = BOUNDING_POLYTOPE;

	if( boundsObject == null )  {
	    boundsIsEmpty = true;
	    boundsIsInfinite = false;
	    initEmptyPolytope();
	    computeAllVerts(); // TODO lazy evaluate
	    return;
	}
       
	boundsIsEmpty = boundsObject.boundsIsEmpty;
	boundsIsInfinite = boundsObject.boundsIsInfinite;

	if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    planes = new Vector4d[6];
	    mag = new double[planes.length];
	    pDotN  = new double[planes.length];
	   
	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -(sphere.center.x+sphere.radius) );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,   sphere.center.x-sphere.radius );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -(sphere.center.y+sphere.radius) );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,   sphere.center.y-sphere.radius );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -(sphere.center.z+sphere.radius) );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,   sphere.center.z-sphere.radius );
	    mag[0] = 1.0;
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;
	    computeAllVerts(); // TODO lazy evaluate
	   
	} else if( boundsObject.boundId == BOUNDING_BOX ){
	    BoundingBox box = (BoundingBox)boundsObject;
	    planes = new Vector4d[6];
	    pDotN  = new double[planes.length];
	    mag = new double[planes.length];
	   
	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -box.upper.x );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,  box.lower.x );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -box.upper.y );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,  box.lower.y );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -box.upper.z );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,  box.lower.z );
	    mag[0] = 1.0;
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;
	    computeAllVerts(); // TODO lazy evaluate
	   
	} else if( boundsObject.boundId == BOUNDING_POLYTOPE ) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    planes = new Vector4d[polytope.planes.length];
	    mag = new double[planes.length];
	    pDotN  = new double[planes.length];
	    nVerts = polytope.nVerts;
	    verts  = new Point3d[nVerts];  
	    for(i=0;i<planes.length;i++) {
		planes[i] = new Vector4d(polytope.planes[i]);
		mag[i] = polytope.mag[i];
		pDotN[i] = polytope.pDotN[i];
	    }
	    for(i=0;i<verts.length;i++) {
		verts[i] = new Point3d(polytope.verts[i]);
	    }
	    centroid = polytope.centroid;

	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope0"));
	}
    }

    /**
     * Constructs a BoundingPolytope from the specified array of bounds
     * objects.  The new polytope will circumscribe the union of the
     * regions specified by the input bounds objects.
     * @param boundsObjects the array bounds objects from which this
     * polytope is constructed.
     */
    public BoundingPolytope(Bounds[] boundsObjects) {
	int i=0;
       
	boundId = BOUNDING_POLYTOPE;
	if( boundsObjects ==  null || boundsObjects.length <= 0  ) {
	    boundsIsEmpty = true;
	    boundsIsInfinite = false;
	    initEmptyPolytope();
	    computeAllVerts(); // TODO lazy evaluate
	    return;
	}
	// find first non empty bounds object
	while( boundsObjects[i] == null && i < boundsObjects.length) {
	    i++;
	}
       
	if( i >= boundsObjects.length ) { // all bounds objects were empty
	    boundsIsEmpty = true;
	    boundsIsInfinite = false;
	    initEmptyPolytope();
	    computeAllVerts(); // TODO lazy evaluate
	    return; 
	}
       
	boundsIsEmpty = boundsObjects[i].boundsIsEmpty;
	boundsIsInfinite = boundsObjects[i].boundsIsInfinite;
       
	if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
	    planes = new Vector4d[6];
	    mag    = new double[planes.length];
	    pDotN  = new double[planes.length];

	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -(sphere.center.x+sphere.radius) );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,   sphere.center.x-sphere.radius );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -(sphere.center.y+sphere.radius) );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,   sphere.center.y-sphere.radius );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -(sphere.center.z+sphere.radius) );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,   sphere.center.z-sphere.radius );
	    mag[0] = 1.0;
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;

	    computeAllVerts(); // TODO lazy evaluate
	} else if( boundsObjects[i].boundId == BOUNDING_BOX ){
	    BoundingBox box = (BoundingBox)boundsObjects[i];
	    planes = new Vector4d[6];
	    mag    = new double[planes.length];
	    pDotN  = new double[planes.length];
	   
	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -box.upper.x );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,  box.lower.x );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -box.upper.y );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,  box.lower.y );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -box.upper.z );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,  box.lower.z );
	    mag[0] = 1.0;
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;

	    computeAllVerts(); // TODO lazy evaluate
	} else if( boundsObjects[i].boundId == BOUNDING_POLYTOPE ) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
	    planes = new Vector4d[polytope.planes.length];
	    mag    = new double[planes.length];
	    pDotN  = new double[planes.length];
	    nVerts = polytope.nVerts;
	    verts  = new Point3d[nVerts];  
	    for(i=0;i<planes.length;i++) {
		planes[i] = new Vector4d(polytope.planes[i]);
		pDotN[i] = polytope.pDotN[i];
		mag[i] = polytope.mag[i];
	    }
	    for(i=0;i<verts.length;i++) {
		verts[i] = new Point3d(polytope.verts[i]);
	    }
	    centroid = polytope.centroid;
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope1"));
	}
	for(i+=1;i<boundsObjects.length;i++) {
	    this.combine(boundsObjects[i]);
	}
    }

    /**
     * Sets the bounding planes for this polytope.
     * @param planes the new set of planes for this  polytope
     * @exception IllegalArgumentException if the length of the
     * specified array of planes is less than 4.
     */
    public void setPlanes(Vector4d[] planes) {
	if (planes.length < 4) {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope11"));
	}

	    int i;
	    double invMag;

	    this.planes = new Vector4d[planes.length];
	    pDotN  = new double[planes.length];
	    mag  = new double[planes.length];
	    boundsIsEmpty = false;

	    if( planes.length <= 0 ) {
		boundsIsEmpty = true;
		boundsIsInfinite = false;
		computeAllVerts(); // TODO lazy evaluate
		return;
	    }

	    for(i=0;i<planes.length;i++) {
		// normalize the plane normals
		mag[i] = Math.sqrt(planes[i].x*planes[i].x + planes[i].y*planes[i].y +
				   planes[i].z*planes[i].z);
		invMag = 1.0/mag[i];
		this.planes[i] = new Vector4d( planes[i].x*invMag, planes[i].y*invMag,
					       planes[i].z*invMag, planes[i].w*invMag );
	    } 
	    computeAllVerts();  // TODO lazy evaluate

	}
    
    /**
     * Returns the equations of the bounding planes for this bounding polytope.
     * The equations are copied into the specified array.
     * The array must be large enough to hold all of the vectors. 
     * The individual array elements must be allocated by the caller.
     * @param planes an array  Vector4d to receive the bounding planes 
     */
    public void getPlanes(Vector4d[] planes)
	{
	    int i;

	    for(i=0;i<planes.length;i++) {
		planes[i].x = this.planes[i].x*mag[i];
		planes[i].y = this.planes[i].y*mag[i];
		planes[i].z = this.planes[i].z*mag[i];
		planes[i].w = this.planes[i].w*mag[i];
	    }
	}

    public int getNumPlanes() {
	return planes.length;
    }

    /**
     * Sets the planes for this BoundingPolytope by keeping its current
     * number and position of planes and computing new planes positions
     * to enclose the given bounds object.
     * @param boundsObject another bounds object
     */
    public void set(Bounds  boundsObject) {
	int i,k;
	double dis;

	// no polytope exists yet so initialize one using the boundsObject
	if( boundsObject == null )  {
	    boundsIsEmpty = true;
	    boundsIsInfinite = false;
	    computeAllVerts(); // TODO lazy evaluate
	      
	}else if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	      
	    if( boundsIsEmpty) {
		initEmptyPolytope();  // no ptope exist so must initialize to default 
		computeAllVerts(); 
	    }
	     
	    for(i=0;i<planes.length;i++) { // D = -(N dot C + radius)
	        planes[i].w = -(sphere.center.x*planes[i].x + 
	 		        sphere.center.y*planes[i].y + 
			        sphere.center.z*planes[i].z + sphere.radius);
	    }

	    boundsIsEmpty = boundsObject.boundsIsEmpty;
	    boundsIsInfinite = boundsObject.boundsIsInfinite;
	    computeAllVerts(); // TODO lazy evaluate

	} else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject;
	    double ux,uy,uz,lx,ly,lz,newD;

	    if( boundsIsEmpty) {
                initEmptyPolytope();  // no ptope exist so must initialize to default 
                computeAllVerts(); 
	    }

	    for(i=0;i<planes.length;i++) { 
	        ux = box.upper.x*planes[i].x;  
   	        uy = box.upper.y*planes[i].y;
	        uz = box.upper.z*planes[i].z;
	        lx = box.lower.x*planes[i].x;  
	        ly = box.lower.y*planes[i].y;
	        lz = box.lower.z*planes[i].z;
 	        planes[i].w = -(ux + uy + uz ); // initalize plane to upper vert
		if( (newD = ux + uy + lz ) + planes[i].w > 0.0) planes[i].w = -newD;
		if( (newD = ux + ly + uz ) + planes[i].w > 0.0) planes[i].w = -newD;
		if( (newD = ux + ly + lz ) + planes[i].w > 0.0) planes[i].w = -newD;

		if( (newD = lx + uy + uz ) + planes[i].w > 0.0) planes[i].w = -newD;
		if( (newD = lx + uy + lz ) + planes[i].w > 0.0) planes[i].w = -newD;
		if( (newD = lx + ly + uz ) + planes[i].w > 0.0) planes[i].w = -newD;
		if( (newD = lx + ly + lz ) + planes[i].w > 0.0) planes[i].w = -newD;
	    }
	    
	    boundsIsEmpty = boundsObject.boundsIsEmpty;
	    boundsIsInfinite = boundsObject.boundsIsInfinite;
	    computeAllVerts(); // TODO lazy evaluate

	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    if( planes.length != polytope.planes.length) {
	        planes = new Vector4d[polytope.planes.length];
                for(k=0;k<polytope.planes.length;k++) planes[k] = new Vector4d();
		mag    = new double[polytope.planes.length];
		pDotN  = new double[polytope.planes.length];
	    }

             
	    for(i=0;i<polytope.planes.length;i++) {
		planes[i].x = polytope.planes[i].x;
		planes[i].y = polytope.planes[i].y;
		planes[i].z = polytope.planes[i].z;
		planes[i].w = polytope.planes[i].w;
		mag[i] = polytope.mag[i];
	    }
	    nVerts = polytope.nVerts;
	    verts  = new Point3d[nVerts];
	    for (k=0; k<nVerts; k++) {
		verts[k] = new Point3d(polytope.verts[k]);
	    }

	    boundsIsEmpty = boundsObject.boundsIsEmpty;
	    boundsIsInfinite = boundsObject.boundsIsInfinite;
	    
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope2"));
	}

    }


    /**
     * Creates a copy of a polytope.
     * @return a new BoundingPolytope
     */
    public Object clone() {
	return new BoundingPolytope(planes);
    }


    /**
     * Indicates whether the specified <code>bounds</code> object is
     * equal to this BoundingPolytope object.  They are equal if the
     * specified <code>bounds</code> object is an instance of
     * BoundingPolytope and all of the data
     * members of <code>bounds</code> are equal to the corresponding
     * data members in this BoundingPolytope.
     * @param bounds the object with which the comparison is made.
     * @return true if this BoundingPolytope is equal to <code>bounds</code>;
     * otherwise false
     *
     * @since Java 3D 1.2
     */
    public boolean equals(Object bounds) {
	try {
	    BoundingPolytope polytope = (BoundingPolytope)bounds;
	    if (planes.length != polytope.planes.length)
		return false;
	    for (int i = 0; i < planes.length; i++)
		if (!planes[i].equals(polytope.planes[i]))
		    return false;

	    return true;
	}
	catch (NullPointerException e) {
	    return false;
	}
        catch (ClassCastException e) {
	    return false;
	}
    }


    /**
     * Returns a hash code value for this BoundingPolytope object
     * based on the data values in this object.  Two different
     * BoundingPolytope objects with identical data values (i.e.,
     * BoundingPolytope.equals returns true) will return the same hash
     * code value.  Two BoundingPolytope objects with different data
     * members may return the same hash code value, although this is
     * not likely.
     * @return a hash code value for this BoundingPolytope object.
     *
     * @since Java 3D 1.2
     */
    public int hashCode() {
	long bits = 1L;

	for (int i = 0; i < planes.length; i++) {
	    bits = 31L * bits + Double.doubleToLongBits(planes[i].x);
	    bits = 31L * bits + Double.doubleToLongBits(planes[i].y);
	    bits = 31L * bits + Double.doubleToLongBits(planes[i].z);
	    bits = 31L * bits + Double.doubleToLongBits(planes[i].w);
	}

	return (int) (bits ^ (bits >> 32));
    }


    /**
     * Combines this bounding polytope with a bounding object so that the
     * resulting bounding polytope encloses the original bounding polytope and the
     * given bounds object.
     * @param boundsObject another bounds object
     */
    public void combine(Bounds boundsObject) {
        BoundingSphere sphere;

	if((boundsObject == null) || (boundsObject.boundsIsEmpty)
	   || (boundsIsInfinite)) 
	    return;

		
	if((boundsIsEmpty) || (boundsObject.boundsIsInfinite)) {
	    this.set(boundsObject);
	    return;
	}
	
	boundsIsEmpty = boundsObject.boundsIsEmpty;
	boundsIsInfinite = boundsObject.boundsIsInfinite;
	
	if( boundsObject.boundId == BOUNDING_SPHERE ) {
            sphere = (BoundingSphere)boundsObject;
 	    int i;
	    double dis;
	    for(i = 0; i < planes.length; i++){
	        dis = sphere.radius+ sphere.center.x*planes[i].x + 
		    sphere.center.y*planes[i].y + sphere.center.z *
		    planes[i].z + planes[i].w;
	        if( dis > 0.0 ) {
		    planes[i].w += -dis;
                }
            }
	} else if( boundsObject  instanceof BoundingBox){
	    BoundingBox b = (BoundingBox)boundsObject;
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
	    
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    this.combine(polytope.verts);
	}   else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope3"));
	}
	
	computeAllVerts();
    }
    
    /**
     * Combines this bounding polytope with an array of bounding objects so that the
     * resulting bounding polytope encloses the original bounding polytope and the
     * given array of bounds object.
     * @param boundsObjects an array of bounds objects
     */
    public void combine(Bounds[] boundsObjects) {
        int i=0;
	double dis;

	if( (boundsObjects == null) || (boundsObjects.length <= 0)
	    || (boundsIsInfinite))
	    return;
	
	// find first non empty bounds object
	while( (i<boundsObjects.length) && ((boundsObjects[i]==null)
					    || boundsObjects[i].boundsIsEmpty)) {
	    i++;
	}
	if( i >= boundsObjects.length)
	    return;   // no non empty bounds so do not modify current bounds
       
	if(boundsIsEmpty)
	    this.set(boundsObjects[i++]);
      
	if(boundsIsInfinite)
	    return;
 
	for(;i<boundsObjects.length;i++) {
	    if( boundsObjects[i] == null );  // do nothing
	    else if( boundsObjects[i].boundsIsEmpty ); // do nothing
	    else if( boundsObjects[i].boundsIsInfinite ) {
		this.set(boundsObjects[i]);
		break; // We're done;
	    }
	    else if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		for(int j = 0; j < planes.length; j++){
		    dis = sphere.radius+ sphere.center.x*planes[j].x + 
			sphere.center.y*planes[j].y + sphere.center.z*
			planes[j].z + planes[j].w;
		    if( dis > 0.0 ) {
			planes[j].w += -dis;
		    }
		}
	    } else if( boundsObjects[i].boundId == BOUNDING_BOX){
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
	   
	    } else if(boundsObjects[i] instanceof BoundingPolytope) {
		BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		this.combine(polytope.verts);
	   
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope4"));
	    }
       
	    computeAllVerts();
	}
    }
 
    /**
     * Combines this bounding polytope with a point.
     * @param point a 3d point in space
     */ 
    public void combine(Point3d point) {
	int i;
	double dis;

	if(boundsIsInfinite) {
	    return;
	}
	
	if( boundsIsEmpty ){
	    planes = new Vector4d[6];
	    mag = new double[planes.length];
	    pDotN  = new double[planes.length];
	    nVerts = 1;
	    verts = new Point3d[nVerts];
	    verts[0] = new Point3d( point.x, point.y, point.z);

	    for(i=0;i<planes.length;i++) {
		pDotN[i] =  0.0;
	    }
	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -point.x );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,  point.x );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -point.y );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,  point.y );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -point.z );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,  point.z );
	    mag[0] = 1.0;    
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;
	    centroid.x = point.x;
	    centroid.y = point.y;
	    centroid.z = point.z;
	    boundsIsEmpty = false;
	    boundsIsInfinite = false;
	} else {

	    for(i = 0; i < planes.length; i++){
		dis = point.x*planes[i].x + point.y*planes[i].y + point.z*
		    planes[i].z + planes[i].w;
		if( dis > 0.0 ) {
		    planes[i].w += -dis;
		}
	    }
	    computeAllVerts();
	}
    }  
    
    /**
     * Combines this bounding polytope with an array of points.
     * @param points an array of 3d points in space
     */  
    public void combine(Point3d[] points) {
	int i,j;
	double dis;

	if( boundsIsInfinite) {
	    return;
	}

	if( boundsIsEmpty ){
	    planes = new Vector4d[6];
	    mag = new double[planes.length];
	    pDotN  = new double[planes.length];
	    nVerts = points.length;
	    verts = new Point3d[nVerts];
	    verts[0] = new Point3d( points[0].x, points[0].y, points[0].z);

	    for(i=0;i<planes.length;i++) {
		pDotN[i] =  0.0;
	    }
	    planes[0] = new Vector4d( 1.0, 0.0, 0.0, -points[0].x );
	    planes[1] = new Vector4d(-1.0, 0.0, 0.0,  points[0].x );
	    planes[2] = new Vector4d( 0.0, 1.0, 0.0, -points[0].y );
	    planes[3] = new Vector4d( 0.0,-1.0, 0.0,  points[0].y );
	    planes[4] = new Vector4d( 0.0, 0.0, 1.0, -points[0].z );
	    planes[5] = new Vector4d( 0.0, 0.0,-1.0,  points[0].z );
	    mag[0] = 1.0;    
	    mag[1] = 1.0;
	    mag[2] = 1.0;
	    mag[3] = 1.0;
	    mag[4] = 1.0;
	    mag[5] = 1.0;
	    centroid.x = points[0].x;
	    centroid.y = points[0].y;
	    centroid.z = points[0].z;
	    boundsIsEmpty = false;
	    boundsIsInfinite = false;
	}
	
	for(j = 0; j < points.length; j++){
	    for(i = 0; i < planes.length; i++){
		dis = points[j].x*planes[i].x + points[j].y*planes[i].y +
		    points[j].z*planes[i].z + planes[i].w;
		if( dis > 0.0 ) {
		    planes[i].w += -dis;
		}
	    }
	}
	
	computeAllVerts();
    }

    /**
     * Modifies the bounding polytope so that it bounds the volume
     * generated by transforming the given bounding object.
     * @param boundsObject the bounding object to be transformed 
     * @param matrix a transformation matrix
     */
    public void transform( Bounds boundsObject, Transform3D matrix) {
	
	if( boundsObject == null || boundsObject.boundsIsEmpty)  {
	    boundsIsEmpty = true;
	    boundsIsInfinite = false;
	    computeAllVerts();
	    return;
	}
	
	if(boundsObject.boundsIsInfinite) {
	    this.set(boundsObject);
	    return;
	}
	
	if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = new BoundingSphere((BoundingSphere)boundsObject);
	    sphere.transform(matrix);
	    this.set(sphere);
	} else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = new BoundingBox( (BoundingBox)boundsObject);
	    box.transform(matrix);
	    this.set(box);
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = new BoundingPolytope( (BoundingPolytope)boundsObject);
	    polytope.transform(matrix);
	    this.set(polytope);
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope5"));
	}
    }
    
    /** 
     * Transforms this  bounding polytope by the given transformation matrix.
     * @param matrix a transformation matrix
     */
    public void transform( Transform3D matrix) {

	if(boundsIsInfinite)
	    return;
	
	int i;
	double invMag;
	Transform3D invTrans = VirtualUniverse.mc.getTransform3D(matrix);
	
	invTrans.invert(); 
	invTrans.transpose();

	for(i = 0; i < planes.length; i++){
	    planes[i].x = planes[i].x * mag[i];
	    planes[i].y = planes[i].y * mag[i];
	    planes[i].z = planes[i].z * mag[i];
	    planes[i].w = planes[i].w * mag[i];
	    invTrans.transform( planes[i] ); 
	}
       
	VirtualUniverse.mc.addToTransformFreeList(invTrans);
	
	for(i=0;i<planes.length;i++) {

	    // normalize the plane normals
	    mag[i] = Math.sqrt(planes[i].x*planes[i].x + planes[i].y*planes[i].y +
			       planes[i].z*planes[i].z);
	    invMag = 1.0/mag[i];
	    this.planes[i] = new Vector4d( planes[i].x*invMag, planes[i].y*invMag,
					   planes[i].z*invMag, planes[i].w*invMag );
	    
	}
	
	for (i=0; i < verts.length; i++) {
	    matrix.transform(verts[i]);
	}
	
    }

    /** 
     * Test for intersection with a ray.
     * @param origin is a the starting point of the ray   
     * @param direction is the direction of the ray
     * @param intersectPoint is a point defining the location  of the intersection
     * @return true or false indicating if an intersection occured 
     */
    boolean intersect(Point3d origin, Vector3d direction, Point3d intersectPoint ) {

	double t,v0,vd,x,y,z,invMag;
	double dx, dy, dz;
	int i;

	if( boundsIsEmpty ) {
	    return false;
	}

	if( boundsIsInfinite ) {
	    intersectPoint.x = origin.x;
	    intersectPoint.y = origin.y;
	    intersectPoint.z = origin.z;
	    return true;
	}
	
	invMag = 1.0/Math.sqrt(direction.x*direction.x +
			       direction.y*direction.y + direction.z*direction.z);
	dx = direction.x*invMag;
	dy = direction.y*invMag;
	dz = direction.z*invMag;
	
	// compute intersection point of ray and each plane then test if point is in polytope
	for(i=0;i<planes.length;i++) {
	    vd = planes[i].x*dx + planes[i].y*dy + planes[i].z*dz;
	    v0 = -(planes[i].x*origin.x + planes[i].y*origin.y +
		   planes[i].z*origin.z + planes[i].w);
	    if(vd != 0.0) { // ray is parallel to plane
		t = v0/vd;
		
		if( t >= 0.0) { // plane is behind origin
		    
		    x = origin.x + dx*t;   // compute intersection point
		    y = origin.y + dy*t;
		    z = origin.z + dz*t;
		    
		    if( pointInPolytope(x,y,z) ) {
			intersectPoint.x = x;
			intersectPoint.y = y;
			intersectPoint.z = z;
			return true;  // ray intersects a face of polytope
		    } 
		} 
	    }
	} 
	
	return false;	
    }

    /** 
     * Test for intersection with a ray 
     * @param origin is a the starting point of the ray   
     * @param direction is the direction of the ray
     * @param position is a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured 
     */
    boolean intersect(Point3d origin, Vector3d direction, Point4d position ) {
	double t,v0,vd,x,y,z,invMag;
	double dx, dy, dz;
	int i,j;
	
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
	
	invMag = 1.0/Math.sqrt(direction.x*direction.x + direction.y*
			       direction.y + direction.z*direction.z);
	dx = direction.x*invMag;
	dy = direction.y*invMag;
	dz = direction.z*invMag;
	
	for(i=0;i<planes.length;i++) {
	    vd = planes[i].x*dx + planes[i].y*dy + planes[i].z*dz;
	    v0 = -(planes[i].x*origin.x + planes[i].y*origin.y +
		   planes[i].z*origin.z + planes[i].w);
	    // System.out.println("v0="+v0+" vd="+vd);
	    if(vd != 0.0) { // ray is parallel to plane
		t = v0/vd;
 
		if( t >= 0.0) { // plane is behind origin
 
		    x = origin.x + dx*t;   // compute intersection point
		    y = origin.y + dy*t;
		    z = origin.z + dz*t;
		    // System.out.println("t="+t+" point="+x+" "+y+" "+z);
 
		    if( pointInPolytope(x,y,z) ) {
			position.x = x;
			position.y = y;
			position.z = z;
			position.w = t;
			return true;  // ray intersects a face of polytope
		    } 
		} 
	    }
	} 
 
	return false;

    }

    /** 
     * Test for intersection with a point 
     * @param point is the pick point   
     * @param position is a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured 
     */
    boolean intersect(Point3d point,  Point4d position ) {
	int i;
    
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

	for(i = 0; i < this.planes.length; i++){
	    if(( point.x*this.planes[i].x +
		 point.y*this.planes[i].y +
		 point.z*this.planes[i].z + planes[i].w ) > 0.0 )
		return false;
	   
	}   
	return true;

    }

    /**
     * Test for intersection with a segment
     * @param start is a point defining  the start of the line segment 
     * @param end is a point defining the end of the line segment 
     * @param position is a point defining the location  of the pick w= distance to pick
     * @return true or false indicating if an intersection occured
     */
    boolean intersect( Point3d start, Point3d end, Point4d position ) {
	double t,v0,vd,x,y,z;
	int i,j;

	//System.out.println("line segment intersect : planes.length " + planes.length);
	
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

	Point3d direction = new Point3d();
	
	direction.x = end.x - start.x;
	direction.y = end.y - start.y;
	direction.z = end.z - start.z;
       
	for(i=0;i<planes.length;i++) {
	    vd = planes[i].x*direction.x + planes[i].y*direction.y +
		planes[i].z*direction.z;
	    v0 = -(planes[i].x*start.x + planes[i].y*start.y +
		   planes[i].z*start.z + planes[i].w);
	    // System.out.println("v0="+v0+" vd="+vd);
	    if(vd != 0.0) { // ray is parallel to plane
		t = v0/vd;
	     
		// System.out.println("t is  " + t);
	     
		if( t >= 0.0) { // plane is behind start
	       
		    x = start.x + direction.x*t;   // compute intersection point
		    y = start.y + direction.y*t;
		    z = start.z + direction.z*t;
		    // System.out.println("t="+t+" point="+x+" "+y+" "+z);
	       
		    if( pointInPolytope(x,y,z) ) {
			//                   if((t*t) > (end.x-start.x)*(end.x-start.x) +
			//                              (end.y-start.y)*(end.y-start.y) +
			//                              (end.z-start.z)*(end.z-start.z)) {
			if(t <= 1.0) {
			    position.x = x;
			    position.y = y;
			    position.z = z;
			    position.w = t;
			    return true;  // ray intersects a face of polytope
			}
		    }
		} 
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

	// compute intersection point of ray and each plane then test if point is in polytope
	
	double t,v0,vd,x,y,z;
	int i,j;
 
        if( boundsIsEmpty ) {
	    return false;
        }
	
	if( boundsIsInfinite ) {
	    return true;
	}

	for(i=0;i<planes.length;i++) {
	    vd = planes[i].x*direction.x + planes[i].y*direction.y +
		planes[i].z*direction.z;
	    v0 = -(planes[i].x*origin.x + planes[i].y*origin.y +
		   planes[i].z*origin.z + planes[i].w);
	    if(vd != 0.0) { // ray is parallel to plane
		t = v0/vd;
		
		if( t >= 0.0) { // plane is behind origin

		    x = origin.x + direction.x*t;   // compute intersection point
		    y = origin.y + direction.y*t;
		    z = origin.z + direction.z*t;

		    if( pointInPolytope(x,y,z) ) {
			return true;  // ray intersects a face of polytope
		    } else {
			// System.out.println("point outside polytope");
		    }
		} 
	    }
	}
 
	return false;
 
    }   

    /**
     * Tests whether the bounding polytope is empty.  A bounding polytope is
     * empty if it is null (either by construction or as the result of
     * a null intersection) or if its volume is negative.  A bounding polytope
     * with a volume of zero is <i>not</i> empty.
     * @return true if the bounding polytope is empty;
     * otherwise, it returns false
     */
    public boolean isEmpty() {
	// if nVerts > 0 after computeAllVerts(), that means
	// there is some intersection between 3 planes.
	return (boundsIsEmpty || (nVerts <= 0));
    }

    /**
     * Test for intersection with a point.
     * @param point a Point defining a position in 3-space
     * @return true or false indicating if an intersection occured
     */  
    public boolean intersect(Point3d point ) {

	int i;
        if( boundsIsEmpty ) {
	   return false;
        }
	if( boundsIsInfinite ) {
	  return true;
	}
 
	for(i = 0; i < this.planes.length; i++){
	    if(( point.x*this.planes[i].x +
		 point.y*this.planes[i].y +
		 point.z*this.planes[i].z + planes[i].w ) > 0.0 )
		return false;
 
	}   
	return true;
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
 
	if( boundsObject == null ) {
	    return false;
	} 

        if( boundsIsEmpty || boundsObject.boundsIsEmpty ) {
	    return false;
        }

	if( boundsIsInfinite || boundsObject.boundsIsInfinite ) {
	    return true;
	}

	if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    return intersect_ptope_sphere( this, (BoundingSphere)boundsObject);
	} else if( boundsObject.boundId == BOUNDING_BOX){
	    return intersect_ptope_abox( this, (BoundingBox)boundsObject);
	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    return intersect_ptope_ptope( this, (BoundingPolytope)boundsObject);
	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope6"));
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
	    if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty) ;	    
	    else if( boundsIsInfinite || boundsObjects[i].boundsIsInfinite ) {
		return true; // We're done here.
	    }
	    if( boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		sphere = (BoundingSphere)boundsObjects[i];
		radsq =  sphere.radius;
		radsq *= radsq;
		distsq = sphere.center.distanceSquared(sphere.center);
		if (distsq < radsq) {
		    return true;
		} 
	    } else if(boundsObjects[i].boundId == BOUNDING_BOX){
		if( this.intersect(boundsObjects[i])) return true;
	    } else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		if( this.intersect(boundsObjects[i])) return true;
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope7"));
	    }
	}

	return false;
    }
    /**
     * Test for intersection with another bounds object.
     * @param boundsObject another bounds object
     * @param newBoundPolytope the new bounding polytope, which is the intersection of
     *      the boundsObject and this BoundingPolytope
     * @return true or false indicating if an intersection occured
     */  
    public boolean intersect(Bounds boundsObject, BoundingPolytope newBoundPolytope) {
	int i;

	if((boundsObject == null) || boundsIsEmpty || boundsObject.boundsIsEmpty ) {
	    newBoundPolytope.boundsIsEmpty = true;
	    newBoundPolytope.boundsIsInfinite = false;
	    newBoundPolytope.computeAllVerts();
	    return false;
	}
	if(boundsIsInfinite && (!boundsObject.boundsIsInfinite)) {
	    newBoundPolytope.set(boundsObject);
	    return true;
	}
	else if((!boundsIsInfinite) && boundsObject.boundsIsInfinite) {
	    newBoundPolytope.set(this);
	    return true;
	}
	else if(boundsIsInfinite && boundsObject.boundsIsInfinite) {
	    newBoundPolytope.set(this);
	    return true;
	}

	
	BoundingBox tbox = new BoundingBox(); // convert sphere to box        
	
	if( boundsObject.boundId == BOUNDING_SPHERE ) {
	    BoundingSphere sphere = (BoundingSphere)boundsObject;
	    if( this.intersect( sphere)) {
		BoundingBox sbox = new BoundingBox( sphere ); // convert sphere to box 
		BoundingBox pbox = new BoundingBox( this ); // convert polytope to box 
		pbox.intersect(sbox, tbox);                        // insersect two boxes
		newBoundPolytope.set( tbox );
		return true;
	    } 
	} else if( boundsObject.boundId == BOUNDING_BOX){
	    BoundingBox box = (BoundingBox)boundsObject; 
	    if( this.intersect( box)) {
		BoundingBox pbox = new BoundingBox( this ); // convert polytope to box 
		pbox.intersect(box, tbox);                        // insersect two boxes
		newBoundPolytope.set( tbox );
		return true;
	    } 

	} else if(boundsObject.boundId == BOUNDING_POLYTOPE) {
	    BoundingPolytope polytope = (BoundingPolytope)boundsObject;
	    if( this.intersect( polytope)) {
		Vector4d newPlanes[] = new Vector4d[planes.length + polytope.planes.length];
		for(i=0;i<planes.length;i++) {
		    newPlanes[i] = new Vector4d(planes[i]);
		}
		for(i=0;i<polytope.planes.length;i++) {
		    newPlanes[planes.length + i] = new Vector4d(polytope.planes[i]);
		}
		BoundingPolytope newPtope= new BoundingPolytope( newPlanes );

		newBoundPolytope.set(newPtope);
		return true;
	    } 

	} else {
	    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope8"));
	}
 
	newBoundPolytope.boundsIsEmpty = true;
	newBoundPolytope.boundsIsInfinite = false;
	newBoundPolytope.computeAllVerts();

	return false;
    }
    
    /**
     * Test for intersection with an array of  bounds objects.
     * @param boundsObjects an array of bounds objects
     * @param newBoundingPolytope the new bounding polytope, which is the intersection of
     *      the boundsObject and this BoundingPolytope
     * @return true or false indicating if an intersection occured
     */  
    public boolean intersect(Bounds[] boundsObjects, BoundingPolytope newBoundingPolytope) {
	
	if( boundsObjects == null || boundsObjects.length <= 0 || boundsIsEmpty ) {
	    newBoundingPolytope.boundsIsEmpty = true;
	    newBoundingPolytope.boundsIsInfinite = false;
	    newBoundingPolytope.computeAllVerts();
	    return false;
	} 

	int i=0;
	// find first non null bounds object
	while( boundsObjects[i] == null && i < boundsObjects.length) {
	    i++;
	}
	
	if( i >= boundsObjects.length ) { // all bounds objects were empty
	    newBoundingPolytope.boundsIsEmpty = true;
	    newBoundingPolytope.boundsIsInfinite = false;
	    newBoundingPolytope.computeAllVerts();
	    return false;
	}
	
	boolean status = false;
	BoundingBox tbox = new BoundingBox(); // convert sphere to box 

	for(i=0;i<boundsObjects.length;i++) {
	    if( boundsObjects[i] == null || boundsObjects[i].boundsIsEmpty) ;
	    else if(  boundsObjects[i].boundId == BOUNDING_SPHERE ) {
		BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		if( this.intersect( sphere)) {
		    BoundingBox sbox = new BoundingBox( sphere ); // convert sphere to box 
		    BoundingBox pbox = new BoundingBox( this ); // convert polytope to box 
		    pbox.intersect(sbox, tbox);                        // insersect two boxes
		    if ( status ) {
			newBoundingPolytope.combine( tbox );                       
		    } else {
			newBoundingPolytope.set( tbox );                       
			status = true;
		    }
		}
	    } else if( boundsObjects[i].boundId == BOUNDING_BOX){
		BoundingBox box = (BoundingBox)boundsObjects[i];
		if( this.intersect( box) ){
		    BoundingBox pbox = new BoundingBox( this ); // convert polytope to box 
		    pbox.intersect(box,tbox);                        // insersect two boxes
		    if ( status ) {
			newBoundingPolytope.combine( tbox );                       
		    } else {
			newBoundingPolytope.set( tbox );                       
			status = true;
		    }
		} else {
		}

	    } else if(boundsObjects[i].boundId == BOUNDING_POLYTOPE) {
		BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		if( this.intersect( polytope)) {
		    Vector4d newPlanes[] = new Vector4d[planes.length + polytope.planes.length];
		    for(i=0;i<planes.length;i++) {
			newPlanes[i] = new Vector4d(planes[i]);
		    }
		    for(i=0;i<polytope.planes.length;i++) {
			newPlanes[planes.length + i] = new Vector4d(polytope.planes[i]);
		    }
		    BoundingPolytope newPtope= new BoundingPolytope( newPlanes );
		    if ( status ) {
			newBoundingPolytope.combine( newPtope );
		    } else {
			newBoundingPolytope.set( newPtope );
			status = true;
		    }
		}
	    } else {
		throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope8"));
	    }
	    
	    if(newBoundingPolytope.boundsIsInfinite)
		break; // We're done. 
	    
	}

	if( status == false ) {
	    newBoundingPolytope.boundsIsEmpty = true;
	    newBoundingPolytope.boundsIsInfinite = false;
	    newBoundingPolytope.computeAllVerts();
	}
	return status;
 
    }   
    /** 
     * Finds closest bounding object that intersects this bounding polytope.
     * @param boundsObjects is an array of  bounds objects 
     * @return closest bounding object 
     */
    public Bounds closestIntersection( Bounds[] boundsObjects) {

	if( boundsObjects == null || boundsObjects.length <= 0  ) {
	    return null;
        }

        if( boundsIsEmpty ) {
	    return null;
        }

	double dis,disToPlane;
	boolean contains = false;
	boolean inside;
	double smallest_distance = Double.MAX_VALUE; 
	int i,j,index=0;
	double cenX = 0.0, cenY = 0.0, cenZ = 0.0;

	for(i = 0; i < boundsObjects.length; i++){
	    if( boundsObjects[i] == null );

	    else if( this.intersect( boundsObjects[i])) {
		if( boundsObjects[i] instanceof BoundingSphere ) {
		    BoundingSphere sphere = (BoundingSphere)boundsObjects[i];
		    dis = Math.sqrt( (centroid.x-sphere.center.x)*(centroid.x-sphere.center.x) +
				     (centroid.y-sphere.center.y)*(centroid.y-sphere.center.y) +
				     (centroid.z-sphere.center.z)*(centroid.z-sphere.center.z) );
		    inside = true;
		    for(j=0;j<planes.length;j++) {
			if( ( sphere.center.x*planes[j].x +
			      sphere.center.y*planes[j].y +
			      sphere.center.z*planes[j].z + planes[i].w ) > 0.0 ) { // check if sphere center in polytope 
			    disToPlane = sphere.center.x*planes[j].x + 
				sphere.center.y*planes[j].y +
				sphere.center.z*planes[j].z + planes[j].w;

				// check if distance from center to plane is larger than radius
			    if( disToPlane > sphere.radius ) inside = false; 
			}
		    }
		    if( inside) { // contains the sphere
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
		} else if( boundsObjects[i] instanceof BoundingBox){
		    BoundingBox box = (BoundingBox)boundsObjects[i];
		    cenX = (box.upper.x+box.lower.x)/2.0;
		    cenY = (box.upper.y+box.lower.y)/2.0;
		    cenZ = (box.upper.z+box.lower.z)/2.0;
		    dis = Math.sqrt( (centroid.x-cenX)*(centroid.x-cenX) +
				     (centroid.y-cenY)*(centroid.y-cenY) +
				     (centroid.z-cenZ)*(centroid.z-cenZ) );
		    inside = true;
		    if( !pointInPolytope( box.upper.x, box.upper.y, box.upper.z ) ) inside = false;
		    if( !pointInPolytope( box.upper.x, box.upper.y, box.lower.z ) ) inside = false;
		    if( !pointInPolytope( box.upper.x, box.lower.y, box.upper.z ) ) inside = false;
		    if( !pointInPolytope( box.upper.x, box.lower.y, box.lower.z ) ) inside = false;
		    if( !pointInPolytope( box.lower.x, box.upper.y, box.upper.z ) ) inside = false;
		    if( !pointInPolytope( box.lower.x, box.upper.y, box.lower.z ) ) inside = false;
		    if( !pointInPolytope( box.lower.x, box.lower.y, box.upper.z ) ) inside = false;
		    if( !pointInPolytope( box.lower.x, box.lower.y, box.lower.z ) ) inside = false;

		    if( inside )  { // contains box
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

		} else if(boundsObjects[i] instanceof BoundingPolytope) {
		    BoundingPolytope polytope = (BoundingPolytope)boundsObjects[i];
		    dis = Math.sqrt( (centroid.x-polytope.centroid.x)*(centroid.x-polytope.centroid.x) +
				     (centroid.y-polytope.centroid.y)*(centroid.y-polytope.centroid.y) +
				     (centroid.z-polytope.centroid.z)*(centroid.z-polytope.centroid.z) );
		    inside = true;
		    for(j=0;j<polytope.nVerts;j++) {
			if ( !pointInPolytope( polytope.verts[j].x, polytope.verts[j].y, polytope.verts[j].z ) ) 
			    inside = false;
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
		    throw new IllegalArgumentException(J3dI18N.getString("BoundingPolytope10"));
		}
	    }
	}

	return boundsObjects[index];
    }

    /** 
     * Returns a string representation of this class
     */
    public String toString() {
	int i;
	
	String description = new String("BoundingPolytope:\n Num Planes ="+planes.length);
	for(i = 0; i < planes.length; i++){
	    description = description+"\n"+mag[i]*planes[i].x+" "+
		mag[i]*planes[i].y+" "+mag[i]*planes[i].z+" "+mag[i]*planes[i].w;
	}
	
	return description;
    }

    private void computeVertex( int a, int b, int c ) {
	double det,x,y,z;

	det = planes[a].x*planes[b].y*planes[c].z + planes[a].y*planes[b].z*planes[c].x +
	    planes[a].z*planes[b].x*planes[c].y - planes[a].z*planes[b].y*planes[c].x -
	    planes[a].y*planes[b].x*planes[c].z - planes[a].x*planes[b].z*planes[c].y; 

	// System.out.println("\n det="+det);
	if( det*det < EPSILON ){
	    // System.out.println("parallel planes="+a+" "+b+" "+c);
	    return; // two planes are parallel
	}
	
	det = 1.0/det;
	
	x = (planes[b].y*planes[c].z - planes[b].z*planes[c].y) * pDotN[a];
	y = (planes[b].z*planes[c].x - planes[b].x*planes[c].z) * pDotN[a];
	z = (planes[b].x*planes[c].y - planes[b].y*planes[c].x) * pDotN[a];
	
	x += (planes[c].y*planes[a].z - planes[c].z*planes[a].y) * pDotN[b];
	y += (planes[c].z*planes[a].x - planes[c].x*planes[a].z) * pDotN[b];
	z += (planes[c].x*planes[a].y - planes[c].y*planes[a].x) * pDotN[b];
	
	x += (planes[a].y*planes[b].z - planes[a].z*planes[b].y) * pDotN[c];
	y += (planes[a].z*planes[b].x - planes[a].x*planes[b].z) * pDotN[c];
	z += (planes[a].x*planes[b].y - planes[a].y*planes[b].x) * pDotN[c];
	
	x = x*det;
	y = y*det;
	z = z*det;
	
	if (pointInPolytope( x, y, z ) ) {
	    if (nVerts >= verts.length) {
		Point3d newVerts[] = new Point3d[nVerts << 1];
		for(int i=0;i<nVerts;i++) {
		    newVerts[i] = verts[i];
		}
		verts = newVerts; 
	    }
	    verts[nVerts++] = new Point3d( x,y,z);
	}
    }


    private void computeAllVerts() {
	int i,a,b,c;
	double x,y,z;
	
	nVerts = 0;
	
	if( boundsIsEmpty) {
	    verts = null;
	    return;
	}
	
	verts = new Point3d[planes.length*planes.length];	

	for(i=0;i<planes.length;i++) {
	    pDotN[i] = -planes[i].x*planes[i].w*planes[i].x - 
		planes[i].y*planes[i].w*planes[i].y -
		planes[i].z*planes[i].w*planes[i].z;
	}

	for(a=0;a<planes.length-2;a++) {
	    for(b=a+1;b<planes.length-1;b++) {
		for(c=b+1;c<planes.length;c++) {
		    computeVertex(a,b,c);
		}
	    }
	}
	// TODO correctly compute centroid
	
	x=y=z=0.0; 
	Point3d newVerts[] = new Point3d[nVerts];

	for(i=0;i<nVerts;i++) {
	    x += verts[i].x;
	    y += verts[i].y;
	    z += verts[i].z;
	    // copy the verts into an array of the correct size
	    newVerts[i] = verts[i];
	}
	
	this.verts = newVerts; // copy the verts into an array of the correct size
	
	centroid.x = x/nVerts;
	centroid.y = y/nVerts;
	centroid.z = z/nVerts;
	
	checkBoundsIsEmpty();
	
    }

    private boolean pointInPolytope( double x, double y, double z ){

	for (int i = 0; i < planes.length; i++){
	    if(( x*planes[i].x +
		 y*planes[i].y +
		 z*planes[i].z + planes[i].w ) > EPSILON ) {
		return false;
	    }
	    
	}   
	return true;
    }

    private void checkBoundsIsEmpty() {
	boundsIsEmpty = (planes.length < 4);
    }
    
    private void initEmptyPolytope() {
	planes = new Vector4d[6];
	pDotN  = new double[6];
	mag    = new double[6];
	verts  = new Point3d[planes.length*planes.length];
	nVerts = 0;
	
	planes[0] = new Vector4d( 1.0, 0.0, 0.0, -1.0 );
	planes[1] = new Vector4d(-1.0, 0.0, 0.0, -1.0 );
	planes[2] = new Vector4d( 0.0, 1.0, 0.0, -1.0 );
	planes[3] = new Vector4d( 0.0,-1.0, 0.0, -1.0 );
	planes[4] = new Vector4d( 0.0, 0.0, 1.0, -1.0 );
	planes[5] = new Vector4d( 0.0, 0.0,-1.0, -1.0 );
	mag[0] = 1.0;
	mag[1] = 1.0;
	mag[2] = 1.0;
	mag[3] = 1.0;
	mag[4] = 1.0;
	mag[5] = 1.0;
	
	checkBoundsIsEmpty();
    }

    Point3d getCenter() {
	return centroid;
    }

    /**
     * if the passed the "region" is same type as this object
     * then do a copy, otherwise clone the Bounds  and
     * return
     */
    Bounds copy(Bounds r) {
	int i, k;
	
	if (r != null && this.boundId == r.boundId) {
	    BoundingPolytope region = (BoundingPolytope) r;
	    if( region.planes.length !=planes.length) {
		region.planes = new Vector4d[planes.length];

		for(k=0;k< region.planes.length;k++)
		    region.planes[k] = new Vector4d();

		region.mag    = new double[planes.length];
		region.pDotN  = new double[planes.length];
		region.verts  = new Point3d[nVerts];
		region.nVerts = nVerts;
		for(k=0;k<nVerts;k++)
		    region.verts[k] = new Point3d(verts[k]);
	    }
	    
             
	    for(i=0;i<planes.length;i++) {
		region.planes[i].x = planes[i].x;
		region.planes[i].y = planes[i].y;
		region.planes[i].z = planes[i].z;
		region.planes[i].w = planes[i].w;
		region.mag[i] = mag[i];
	    }

	    region.boundsIsEmpty = boundsIsEmpty;
	    region.boundsIsInfinite = boundsIsInfinite;
	    return region;
	}
	else {
	    return (Bounds) this.clone();
	}
    }

    int getPickType() {
	return PickShape.PICKBOUNDINGPOLYTOPE;
    }
}
