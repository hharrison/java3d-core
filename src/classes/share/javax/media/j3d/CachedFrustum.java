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
 * The CachedFrustum class is used to pre compute data for a set of view
 * frustum planes which allows more efficient intersection testing.
 *
 * The CachedFrustum caches data for frustums for effecient intersection
 * testing.
 */

// this class assumes the planes are in the following order:
        // left plane =   frustumPlanes[0]
        // right plane =  frustumPlanes[1]
        // top plane =    frustumPlanes[2]
        // bottom plane = frustumPlanes[3]
        // front plane =  frustumPlanes[4]
        // back plane =   frustumPlanes[5]

class CachedFrustum {

    static final double EPSILON = .000001;
    Vector4d[] clipPlanes;
    Point3d[]  verts;
    Point4d[]  xEdges; // silhouette edges in yz plane
    Point4d[]  yEdges; // silhouette edges in xz plane
    Point4d[]  zEdges; // silhouette edges in xy plane
    int nxEdges, nyEdges, nzEdges;
    int[] xEdgeList;
    int[] yEdgeList;
    int[] zEdgeList;
    Point3d upper,lower;  // bounding box of frustum
    Point3d center;  //  center of frustum

    // re-used temporary from computeEdges
    Point3d edge = new Point3d();

    /**
     * Constructs and initializes a new CachedFrustum using the values
     * provided in the argument.
     * @param planes array specifying the frustum's clip plane position
     */
    CachedFrustum(Vector4d[] planes) {
      int i;
      if( planes.length < 6 ) {
	 throw new IllegalArgumentException(J3dI18N.getString("CachedFrustum0"));
      }
      clipPlanes = new Vector4d[6];
      xEdges     = new Point4d[12];
      yEdges     = new Point4d[12];
      zEdges     = new Point4d[12];
      verts      = new Point3d[8];
      upper      = new Point3d();
      lower      = new Point3d();
      xEdgeList  = new int[12];
      yEdgeList  = new int[12];
      zEdgeList  = new int[12];
      center     = new Point3d();

      for(i=0;i<8;i++){
        verts[i] = new Point3d();
      }

      for(i=0;i<6;i++){
	 clipPlanes[i] = new Vector4d( planes[i] );
      }
      for(i=0;i<12;i++){
	xEdges[i] = new Point4d();
	yEdges[i] = new Point4d();
	zEdges[i] = new Point4d();
      }
      computeValues(clipPlanes);
    }

    /**
     * Constructs and initializes a new default CachedFrustum
     * @param planes array specifying the frustum's clip planes
     */
    CachedFrustum() {
      int i;
      clipPlanes = new Vector4d[6];
      upper      = new Point3d();
      lower      = new Point3d();
      xEdges     = new Point4d[12];
      yEdges     = new Point4d[12];
      zEdges     = new Point4d[12];
      verts      = new Point3d[8];
      xEdgeList  = new int[12];
      yEdgeList  = new int[12];
      zEdgeList  = new int[12];
      center     = new Point3d();

      for(i=0;i<8;i++){
        verts[i] = new Point3d();
      }
      for(i=0;i<6;i++){
	 clipPlanes[i] = new Vector4d();
      }
      for(i=0;i<12;i++){
	xEdges[i] = new Point4d();
	yEdges[i] = new Point4d();
	zEdges[i] = new Point4d();
      }

    }

    /**
     * Returns a string containing the values of the CachedFrustum.
     */
    public String toString() {
	return( clipPlanes[0].toString()+"\n"+
	        clipPlanes[1].toString()+"\n"+
	        clipPlanes[2].toString()+"\n"+
	        clipPlanes[3].toString()+"\n"+
	        clipPlanes[4].toString()+"\n"+
	        clipPlanes[5].toString()+"\n"+
		"corners="+"\n"+
	        verts[0].toString()+"\n"+
	        verts[1].toString()+"\n"+
	        verts[2].toString()+"\n"+
	        verts[3].toString()+"\n"+
	        verts[4].toString()+"\n"+
	        verts[5].toString()+"\n"+
	        verts[6].toString()+"\n"+
	        verts[7].toString()
		);
    }


    /**
     * Sets the values of the CachedFrustum based on a new set of frustum planes.
     * @param planes array specifying the frustum's clip planes
     */
    void  set(Vector4d[] planes) {
      int i;

      if( planes.length != 6 ) {
	 throw new IllegalArgumentException(J3dI18N.getString("CachedFrustum1"));
      }

      for(i=0;i<6;i++){
	 clipPlanes[i].set( planes[i] );
      }

      computeValues(clipPlanes);
    }

    /**
     * Computes cached values.
     * @param planes array specifying the frustum's clip planes
     */
    private void computeValues(Vector4d[] planes) {

        int i;

	// compute verts

	computeVertex( 0, 3, 4, verts[0]);  // front-bottom-left
	computeVertex( 0, 2, 4, verts[1]);  // front-top-left
	computeVertex( 1, 2, 4, verts[2]);  // front-top-right
	computeVertex( 1, 3, 4, verts[3]);  // front-bottom-right
	computeVertex( 0, 3, 5, verts[4]);  // back-bottom-left
	computeVertex( 0, 2, 5, verts[5]);  // back-top-left
	computeVertex( 1, 2, 5, verts[6]);  // back-top-right
	computeVertex( 1, 3, 5, verts[7]);  // back-bottom-right

	// compute bounding box

	upper.x = verts[0].x;
	upper.y = verts[0].y;
	upper.z = verts[0].z;
	lower.x = verts[0].x;
	lower.y = verts[0].y;
	lower.z = verts[0].z;

	center.x = verts[0].x;
	center.y = verts[0].y;
	center.z = verts[0].z;

	// find min and max in x-y-z directions

	for(i=1;i<8;i++) {
	    if( verts[i].x > upper.x) upper.x = verts[i].x; // xmax
	    if( verts[i].x < lower.x) lower.x = verts[i].x; // xmin

	    if( verts[i].y > upper.y) upper.y = verts[i].y; // ymay
	    if( verts[i].y < lower.y) lower.y = verts[i].y; // ymin

	    if( verts[i].z > upper.z) upper.z = verts[i].z; // zmaz
	    if( verts[i].z < lower.z) lower.z = verts[i].z; // zmin

	    center.x += verts[i].x;
	    center.y += verts[i].y;
	    center.z += verts[i].z;
	}

	center.x = center.x*0.125;
	center.y = center.y*0.125;
	center.z = center.z*0.125;

	// to find the sil. edges in the xz plane check the sign y component of the normals
	// from the plane equation and see if they are opposite
	// xz plane
	i = 0;
	if( (clipPlanes[0].y * clipPlanes[4].y) <= 0.0 ) yEdgeList[i++] = 0; // front-left
	if( (clipPlanes[2].y * clipPlanes[4].y) <= 0.0 ) yEdgeList[i++] = 1; // front-top
	if( (clipPlanes[1].y * clipPlanes[4].y) <= 0.0 ) yEdgeList[i++] = 2; // front-right
	if( (clipPlanes[3].y * clipPlanes[4].y) <= 0.0 ) yEdgeList[i++] = 3; // front-bottom
	if( (clipPlanes[0].y * clipPlanes[3].y) <= 0.0 ) yEdgeList[i++] = 4; // middle-left
	if( (clipPlanes[0].y * clipPlanes[2].y) <= 0.0 ) yEdgeList[i++] = 5; // middle-top
	if( (clipPlanes[1].y * clipPlanes[2].y) <= 0.0 ) yEdgeList[i++] = 6; // middle-right
	if( (clipPlanes[1].y * clipPlanes[3].y) <= 0.0 ) yEdgeList[i++] = 7; // middle-bottom
	if( (clipPlanes[0].y * clipPlanes[5].y) <= 0.0 ) yEdgeList[i++] = 8; // back-left
	if( (clipPlanes[2].y * clipPlanes[5].y) <= 0.0 ) yEdgeList[i++] = 9; // back-top
	if( (clipPlanes[1].y * clipPlanes[5].y) <= 0.0 ) yEdgeList[i++] =10; // back-right
	if( (clipPlanes[3].y * clipPlanes[5].y) <= 0.0 ) yEdgeList[i++] =11; // back-bottom
	nyEdges = i;

	// yz plane
	i = 0;
	if( (clipPlanes[0].x * clipPlanes[4].x) <= 0.0 ) xEdgeList[i++] = 0; // front-left
	if( (clipPlanes[2].x * clipPlanes[4].x) <= 0.0 ) xEdgeList[i++] = 1; // front-top
	if( (clipPlanes[1].x * clipPlanes[4].x) <= 0.0 ) xEdgeList[i++] = 2; // front-right
	if( (clipPlanes[3].x * clipPlanes[4].x) <= 0.0 ) xEdgeList[i++] = 3; // front-bottom
	if( (clipPlanes[0].x * clipPlanes[3].x) <= 0.0 ) xEdgeList[i++] = 4; // middle-left
	if( (clipPlanes[0].x * clipPlanes[2].x) <= 0.0 ) xEdgeList[i++] = 5; // middle-top
	if( (clipPlanes[1].x * clipPlanes[2].x) <= 0.0 ) xEdgeList[i++] = 6; // middle-right
	if( (clipPlanes[1].x * clipPlanes[3].x) <= 0.0 ) xEdgeList[i++] = 7; // middle-bottom
	if( (clipPlanes[0].x * clipPlanes[5].x) <= 0.0 ) xEdgeList[i++] = 8; // back-left
	if( (clipPlanes[2].x * clipPlanes[5].x) <= 0.0 ) xEdgeList[i++] = 9; // back-top
	if( (clipPlanes[1].x * clipPlanes[5].x) <= 0.0 ) xEdgeList[i++] =10; // back-right
	if( (clipPlanes[3].x * clipPlanes[5].x) <= 0.0 ) xEdgeList[i++] =11; // back-bottom
	nxEdges = i;

	// xy plane
	i = 0;
	if( (clipPlanes[0].z * clipPlanes[4].z) <= 0.0 ) zEdgeList[i++] = 0; // front-left
	if( (clipPlanes[2].z * clipPlanes[4].z) <= 0.0 ) zEdgeList[i++] = 1; // front-top
	if( (clipPlanes[1].z * clipPlanes[4].z) <= 0.0 ) zEdgeList[i++] = 2; // front-right
	if( (clipPlanes[3].z * clipPlanes[4].z) <= 0.0 ) zEdgeList[i++] = 3; // front-bottom
	if( (clipPlanes[0].z * clipPlanes[3].z) <= 0.0 ) zEdgeList[i++] = 4; // middle-left
	if( (clipPlanes[0].z * clipPlanes[2].z) <= 0.0 ) zEdgeList[i++] = 5; // middle-top
	if( (clipPlanes[1].z * clipPlanes[2].z) <= 0.0 ) zEdgeList[i++] = 6; // middle-right
	if( (clipPlanes[1].z * clipPlanes[3].z) <= 0.0 ) zEdgeList[i++] = 7; // middle-bottom
	if( (clipPlanes[0].z * clipPlanes[5].z) <= 0.0 ) zEdgeList[i++] = 8; // back-left
	if( (clipPlanes[2].z * clipPlanes[5].z) <= 0.0 ) zEdgeList[i++] = 9; // back-top
	if( (clipPlanes[1].z * clipPlanes[5].z) <= 0.0 ) zEdgeList[i++] =10; // back-right
	if( (clipPlanes[3].z * clipPlanes[5].z) <= 0.0 ) zEdgeList[i++] =11; // back-bottom
	nzEdges = i;

	// compute each edge
	computeEdges( clipPlanes, 0, 4, xEdges[0], yEdges[0], zEdges[0]); // front-left
	computeEdges( clipPlanes, 2, 4, xEdges[1], yEdges[1], zEdges[1]); // front-top
	computeEdges( clipPlanes, 1, 4, xEdges[2], yEdges[2], zEdges[2]);
	computeEdges( clipPlanes, 3, 4, xEdges[3], yEdges[3], zEdges[3]);
	computeEdges( clipPlanes, 0, 3, xEdges[4], yEdges[4], zEdges[4]);
	computeEdges( clipPlanes, 0, 2, xEdges[5], yEdges[5], zEdges[5]);
	computeEdges( clipPlanes, 1, 2, xEdges[6], yEdges[6], zEdges[6]);
	computeEdges( clipPlanes, 1, 3, xEdges[7], yEdges[7], zEdges[7]);
	computeEdges( clipPlanes, 0, 5, xEdges[8], yEdges[8], zEdges[8]);
	computeEdges( clipPlanes, 2, 5, xEdges[9], yEdges[9], zEdges[9]);
	computeEdges( clipPlanes, 1, 5, xEdges[10], yEdges[10], zEdges[10]);
	computeEdges( clipPlanes, 3, 5, xEdges[11], yEdges[11], zEdges[11]);

	/*
      int k;
      System.out.println("clipPlanes=");
      for( k=0;k<6;k++){
      System.out.println(clipPlanes[k].toString());
      }
      System.out.println("corners="+"\n"+
      verts[0].toString()+"\n"+
      verts[1].toString()+"\n"+
      verts[2].toString()+"\n"+
      verts[3].toString()+"\n"+
      verts[4].toString()+"\n"+
      verts[5].toString()+"\n"+
      verts[6].toString()+"\n"+
      verts[7].toString());
      System.out.println("\nxEdges=");
      for(k=0;k<nxEdges;k++){
      System.out.println(xEdges[xEdgeList[k]].toString());
      }
      System.out.println("\nyEdges=");
      for(k=0;k<nxEdges;k++){
      System.out.println(yEdges[xEdgeList[k]].toString());
      }
      System.out.println("\nzEdges=");
      for(k=0;k<nxEdges;k++){
      System.out.println(zEdges[xEdgeList[k]].toString());
      }
      */

    }
    private void computeEdges( Vector4d[] planes, int i, int j, Point4d xEdge,
			       Point4d yEdge, Point4d zEdge) {

	double mag,x,y,z,xm,ym,zm,w;

	// compute vector that is intersection of two planes

	edge.x = planes[i].y*planes[j].z - planes[j].y*planes[i].z;
	edge.y = planes[j].x*planes[i].z - planes[i].x*planes[j].z;
	edge.z = planes[i].x*planes[j].y - planes[j].x*planes[i].y;

	mag = 1.0/Math.sqrt( edge.x*edge.x + edge.y*edge.y + edge.z*edge.z);

	edge.x = mag*edge.x;
	edge.y = mag*edge.y;
	edge.z = mag*edge.z;

	xm = Math.abs(edge.x);
	ym = Math.abs(edge.y);
	zm = Math.abs(edge.z);

	// compute point on the intersection vector
	// see Graphics Gems III pg. 233

	if( zm >= xm && zm >= ym ){                // z greatest magnitude
	    w = (planes[i].x*planes[j].y + planes[i].z*planes[j].y);
	    if( w == 0.0)
		w = 1.0;
	    else
		w = 1.0/w;
	    x = (planes[i].y*planes[j].w - planes[j].y*planes[i].w) * w;
	    y = (planes[j].x*planes[i].w - planes[i].x*planes[j].w) * w;
	    z = 0.0;
	} else if( xm >= ym && xm >= zm){          // x greatest magnitude
	    w = (planes[i].y*planes[j].z + planes[i].z*planes[j].y);
	    if( w == 0.0)
		w = 1.0;
	    else
		w = 1.0/w;
	    x = 0.0;
	    y = (planes[i].z*planes[j].w - planes[j].z*planes[i].w) * w;
	    z = (planes[j].y*planes[i].w - planes[i].y*planes[j].w) * w;
	} else {                                                  // y greatest magnitude
	    w = (planes[i].x*planes[j].z + planes[i].z*planes[j].x);
	    if( w == 0.0)
		w = 1.0;
	    else
		w = 1.0/w;
	    x = (planes[i].z*planes[j].w - planes[j].z*planes[i].w) * w;
	    y = 0.0;
	    z = (planes[j].x*planes[i].w - planes[i].x*planes[j].w) * w;
	}

	// compute the noramls to the edges in for silhouette testing
	/*
	  System.out.println("\nplane1="+planes[i].toString());
	  System.out.println("plane2="+planes[j].toString());
	  System.out.println("point="+x+" "+y+" "+z);
	  System.out.println("edge="+edge.x+" "+edge.y+" "+edge.z);
	  */
	// x=0 edges

	xEdge.x = 0.0;
	xEdge.y = -edge.z;
	xEdge.z = edge.y;
	xEdge.w = -(xEdge.y*y + xEdge.z*z);

	if( (center.y*xEdge.y + center.z*xEdge.z + xEdge.w) > 0.0 ){
	    xEdge.y = edge.z;
	    xEdge.z = -edge.y;
	    xEdge.w = -(xEdge.y*y + xEdge.z*z);
	}

	// y=0 edges
	yEdge.x = -edge.z;
	yEdge.y = 0.0;
	yEdge.z = edge.x;
	yEdge.w = -(yEdge.x*x + yEdge.z*z);
	if( (center.y*yEdge.y + center.z*yEdge.z + yEdge.w) > 0.0 ){
            yEdge.x = edge.z;
            yEdge.z = -edge.x;
            yEdge.w = -(yEdge.x*x + yEdge.z*z);
	}

	// z=0 edges
	zEdge.x = -edge.y;
	zEdge.y = edge.x;
	zEdge.z = 0.0;
	zEdge.w = -(zEdge.y*y + zEdge.x*x);

	if( (center.x*zEdge.x + center.y*zEdge.y + zEdge.w) > 0.0 ){
            zEdge.x = edge.y;
            zEdge.y = -edge.x;
            zEdge.w = -(zEdge.y*y + zEdge.x*x);
	}
	/*
	  System.out.println("xedge="+xEdge.x+" "+xEdge.y+" "+xEdge.z+" "+xEdge.w);
	  System.out.println("yedge="+yEdge.y+" "+yEdge.y+" "+yEdge.z+" "+yEdge.w);
	  System.out.println("zedge="+zEdge.z+" "+zEdge.y+" "+zEdge.z+" "+zEdge.w);
	  */
    }
    private void computeVertex( int a, int b, int c, Point3d vert) {
	double det,x,y,z;

	det = clipPlanes[a].x*clipPlanes[b].y*clipPlanes[c].z + clipPlanes[a].y*clipPlanes[b].z*clipPlanes[c].x +
	    clipPlanes[a].z*clipPlanes[b].x*clipPlanes[c].y - clipPlanes[a].z*clipPlanes[b].y*clipPlanes[c].x -
	    clipPlanes[a].y*clipPlanes[b].x*clipPlanes[c].z - clipPlanes[a].x*clipPlanes[b].z*clipPlanes[c].y;

	if( det*det < EPSILON ){
	    return;       // two planes are parallel
	}

	det = 1.0/det;

	vert.x = (clipPlanes[b].y*clipPlanes[c].z - clipPlanes[b].z*clipPlanes[c].y) * -clipPlanes[a].w;
	vert.y = (clipPlanes[b].z*clipPlanes[c].x - clipPlanes[b].x*clipPlanes[c].z) * -clipPlanes[a].w;
	vert.z = (clipPlanes[b].x*clipPlanes[c].y - clipPlanes[b].y*clipPlanes[c].x) * -clipPlanes[a].w;

   vert.x += (clipPlanes[c].y*clipPlanes[a].z - clipPlanes[c].z*clipPlanes[a].y) * -clipPlanes[b].w;
   vert.y += (clipPlanes[c].z*clipPlanes[a].x - clipPlanes[c].x*clipPlanes[a].z) * -clipPlanes[b].w;
   vert.z += (clipPlanes[c].x*clipPlanes[a].y - clipPlanes[c].y*clipPlanes[a].x) * -clipPlanes[b].w;

   vert.x += (clipPlanes[a].y*clipPlanes[b].z - clipPlanes[a].z*clipPlanes[b].y) * -clipPlanes[c].w;
   vert.y += (clipPlanes[a].z*clipPlanes[b].x - clipPlanes[a].x*clipPlanes[b].z) * -clipPlanes[c].w;
   vert.z += (clipPlanes[a].x*clipPlanes[b].y - clipPlanes[a].y*clipPlanes[b].x) * -clipPlanes[c].w;

   vert.x = vert.x*det;
   vert.y = vert.y*det;
   vert.z = vert.z*det;

  }


    /**
     * Tests for intersection of six sided hull  and the frustum
     * @param six sided bounding box ( lower (x,y,z), upper (x,y,z))
     * @return true if they intersect
     */
    boolean intersect( double lx, double ly, double lz,
		       double ux, double uy, double uz) {
	int i,index;

	// System.out.println("intersect frustum with box : lower ( " + lx + ", " + ly + ", " + lz +
	// ") upper( " + ux + ", " + uy + ", " + uz + ")");
	//      System.out.println("frustum "+this.toString());
	//     check if box and bounding box  of frustum intersect
	if( ux > this.lower.x &&
	    lx < this.upper.x &&
	    uy > this.lower.y &&
	    ly < this.upper.y &&
	    uz > this.lower.z &&
	    lz < this.upper.z ) {
	} else {
	    //      System.out.println("false box and bounding box  of frustum do not intersect");
	    return false;
	}

	// check if all box points out any frustum plane
	for(i=0;i<6;i++){
	    if(( ux*this.clipPlanes[i].x +
		 uy*this.clipPlanes[i].y +
		 uz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue; // corner inside plane
	    }
	    if(( ux*this.clipPlanes[i].x +
		 ly*this.clipPlanes[i].y +
		 uz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ){
		continue;
	    }
	    if(( ux*this.clipPlanes[i].x +
		 ly*this.clipPlanes[i].y +
		 lz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ){
		continue;
	    }
	    if(( ux*this.clipPlanes[i].x +
		 uy*this.clipPlanes[i].y +
		 lz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue;
	    }
	    if(( lx*this.clipPlanes[i].x +
		 uy*this.clipPlanes[i].y +
		 uz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue;
	    }
	    if(( lx*this.clipPlanes[i].x +
		 ly*this.clipPlanes[i].y +
		 uz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue;
	    }
	    if(( lx*this.clipPlanes[i].x +
		 ly*this.clipPlanes[i].y +
		 lz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue;
	    }
	    if(( lx*this.clipPlanes[i].x +
		 uy*this.clipPlanes[i].y +
		 lz*this.clipPlanes[i].z + this.clipPlanes[i].w ) > -EPSILON ) {
		continue;
	    }
	    //      System.out.println("false all corners outside this frustum plane"+frustum.clipPlanes[i].toString());
	    return false; // all corners outside this frustum plane
	}

	// check if any box corner  is inside of the frustum silhoette edges in the 3 views
	// y-z
	for(i=0;i<this.nxEdges;i++){
	    index = this.xEdgeList[i];
      if(( uy*this.xEdges[index].y +
	   uz*this.xEdges[index].z + this.xEdges[index].w ) < EPSILON ) break; // corner inside ege
      if(( uy*this.xEdges[index].y +
	   lz*this.xEdges[index].z + this.xEdges[index].w ) < EPSILON ) break;
      if(( ly*this.xEdges[index].y +
	   uz*this.xEdges[index].z + this.xEdges[index].w ) < EPSILON ) break;
      if(( ly*this.xEdges[index].y +
	   lz*this.xEdges[index].z + this.xEdges[index].w ) < EPSILON ) break;
      if( i == this.nxEdges-1) {
	//      System.out.println("false all box corners outside yz silhouette edges ");
	return false; // all box corners outside yz silhouette edges
      }
    }
    // x-z
    for(i=0;i<this.nyEdges;i++){
      index = this.yEdgeList[i];
      if(( ux*this.yEdges[index].x +
	   uz*this.yEdges[index].z + this.yEdges[index].w ) < EPSILON ) break;
      if(( ux*this.yEdges[index].x +
	   lz*this.yEdges[index].z + this.yEdges[index].w ) < EPSILON ) break;
      if(( lx*this.yEdges[index].x +
	   uz*this.yEdges[index].z + this.yEdges[index].w ) < EPSILON ) break;
      if(( lx*this.yEdges[index].x +
	   lz*this.yEdges[index].z + this.yEdges[index].w ) < EPSILON ) break;
      if( i == this.nyEdges-1) {
	//      System.out.println("false all box corners outside xz silhouette edges");
	return false; // all box corners outside xz silhouette edges
      }
    }
    // x-y
    for(i=0;i<this.nzEdges;i++){
      index = this.zEdgeList[i];
      if(( uy*this.zEdges[index].y +
	   uz*this.zEdges[index].z + this.zEdges[index].w ) < EPSILON ) break;
      if(( uy*this.zEdges[index].y +
	   lz*this.zEdges[index].z + this.zEdges[index].w ) < EPSILON ) break;
      if(( ly*this.zEdges[index].y +
	   uz*this.zEdges[index].z + this.zEdges[index].w ) < EPSILON ) break;
      if(( ly*this.zEdges[index].y +
	   lz*this.zEdges[index].z + this.zEdges[index].w ) < EPSILON ) break;
      if( i == this.nzEdges-1) {
	/*
	  System.out.println("false all box corners outside xy silhouette edges");
	  System.out.println("xy silhouette edges=");
	  for(int j=0;j<this.nzEdges;j++){
	  System.out.println(this.zEdges[j].toString());
	  }
	  */
	return false; // all box corners outside xy silhouette edges
      }
    }
    //      System.out.println("true");
    return true;
  }


}
