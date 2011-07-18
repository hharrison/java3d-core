/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.picking;

import javax.vecmath.*;
import javax.media.j3d.*;
import java.util.ArrayList;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.internal.*;

/**
 * Stores information about a pick hit.
 * Detailed information about the pick and each intersection of the PickShape 
 * with the picked Node can be inquired.  The PickResult is constructed with
 * basic information and more detailed information is generated as needed.  The
 * additional information is only available if capability bits on the scene 
 * graph Nodes are set properly; 
 * <A HREF="PickTool.html#setCapabilities(javax.media.j3d.Node, int)">
 * <code>PickTool.setCapabilties(Node, int)</code></A> 
 * can
 * be used to ensure correct capabilites are set. Inquiring data which is not
 * available due to capabilties not being set will generate a
 * <code>CapabilityNotSet</code> exception.
 * <p>
 * A PickResult can be used to calculate intersections on Node which is not part
 * of a live scene graph using the constructor which takes a local to VWorld
 * transformation for the Node.
 * <p>
 * Pick hits on TriangleStrip primitives will store the triangle points in the
 * PickIntersection with 
 * the verticies in counter-clockwise order. For triangles which start with
 * an odd numbered vertex this will be the the opposite of the
 * order of the points in the TriangleStrip.
 * This way the triangle in
 * the PickIntersection will display the same was as the triangle in the
 * strip.
 * <p>
 * If the Shape3D being picked has multiple geometry arrays, the arrays are
 * stored in the PickResult and referred to by a geometry index.
 * <p> 
 * If the Shape3D refers to a CompressedGeometry, the geometry is decompressed
 * into an array of Shape3D nodes which can be inquired.  The geometry
 * NodeComponents for the Shape3D nodes are stored and used as if the Shape3D
 * had multiple geometries.  If there are multiple CompressedGeometries on the
 * Shape3D, the decompressed Shape3Ds and GeometryArrays will be stored
 * sequentially.
 * <p>
 * The intersection point for Morph nodes cannot be calculated using the 
 * displayed geometry 
 * due to limitations in  the current Java3D core API (the current
 * geometry of the the Morph cannot be inquired).  Instead 
 * the geometry at index 0 in the Morph is used. This limitation may
 * be eliminated in a future release of Java3D.
 */ 
public class PickResult {
    
    /* OPEN ISSUES:  
       -- getInterpolatedTextureCoordinates uses the depricated API faor 
       getTextureCoordinate(), need to update.
       -- Bounds tests don't fill in any picking info.  
       -- Can't do any intersections with the PickPoint shape.
       */


    // Externally used constants

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Shape3D</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int SHAPE3D = 0x1;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Morph</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int MORPH = 0x2;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>

     * to return a
     * <code>Primitive</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int PRIMITIVE = 0x4;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Link</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int LINK = 0x8;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Group</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int GROUP = 0x10;
  
    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>TransformGroup</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int TRANSFORM_GROUP = 0x20;
 
    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>BranchGroup</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int BRANCH_GROUP = 0x40;

    /**
     * Flag to pass to 
     * <CODE>getNode(int)</CODE>
     * to return a
     * <code>Switch</code> node from 
     * the <code>SceneGraphPath</code>. 
     */
    public static final int SWITCH = 0x80;



    /* =================== ATTRIBUTES ======================= */
    static boolean debug = false; 

    /** if true, find only the first intersection */
    private boolean 	firstIntersectOnly = false;

    /** Stored SceneGraphPath */
    private SceneGraphPath pickedSceneGraphPath = null;

    /** Picked node: shape3d, text3d, etc. */
    private Node pickedNode = null;

    /** GeometryArray(s) of the picked node */
    private GeometryArray[] geometryArrays = null;

    /** Shape3Ds from CompressedGeometry on the picked node */
    private Shape3D[]	compressGeomShape3Ds = null;

    /** Transform to World Coordinates */
    private Transform3D 	localToVWorld = null;

    /** the pick shape to use for intersections */
    private PickShape pickShape = null;
    /* data derived from the pick shape */
    private int 		pickShapeType = -1;
    private Vector3d 	pickShapeDir = null;
    private Point3d 	pickShapeStart = null;
    private Point3d 	pickShapeEnd = null;
    private Bounds 	pickShapeBounds = null;

    static final Point3d zeroPnt = new Point3d();

    /** ArrayList to store intersection results
      * Used in PickTool
      */
    ArrayList 	intersections = null;
    
    // internal constants used for intersections
    static final double FUZZ = 1E-6; /* fuzziness factor used to determine
					if two lines are parallel */
    static final int PICK_SHAPE_RAY = 1;
    static final int PICK_SHAPE_SEGMENT = 2;
    static final int PICK_SHAPE_POINT = 3;
    static final int PICK_SHAPE_BOUNDING_BOX = 4;
    static final int PICK_SHAPE_BOUNDING_SPHERE = 5;
    static final int PICK_SHAPE_BOUNDING_POLYTOPE = 6;
    static final int PICK_SHAPE_CYLINDER = 7;
    static final int PICK_SHAPE_CONE = 8;

    static final double EPS = 1.0e-13;


    /* ===================   METHODS  ======================= */

    /** Default constructor. */
    PickResult () { }

    /** Construct a PickResult using a SceneGraphPath
      @param sgp SceneGraphPath associated with this PickResult 
      @param ps The pickShape to intersect against
      */
    public PickResult (SceneGraphPath sgp, PickShape ps) {
	pickedSceneGraphPath = sgp;
	pickedNode = sgp.getObject();
	localToVWorld = sgp.getTransform();
	pickShape = ps;
	initPickShape();
    }


    /** Construct a PickResult using the Node and localToVWorld transform
      @param pn The picked node.
      @param l2vw The local to VWorld transformation for the node
      @param ps The PickShape to intersect against
      @throws IllegalArgumentException If the node is not a Morph or Shape3D.
      */
    public PickResult (Node pn, Transform3D l2vw, PickShape ps) {
	if ((pn instanceof Shape3D) || (pn instanceof Morph)) { 
	    pickedNode = pn;
	    localToVWorld = l2vw;
	    pickShape = ps;
	    initPickShape();
	} else {
	    throw new IllegalArgumentException();
	}
    }

    void initPickShape() {
	if(pickShape instanceof PickRay) {
	    if (pickShapeStart == null) pickShapeStart = new Point3d();
	    if (pickShapeDir == null) pickShapeDir = new Vector3d();
	    ((PickRay) pickShape).get (pickShapeStart, pickShapeDir);
	    pickShapeType = PICK_SHAPE_RAY;
	} else if (pickShape instanceof PickSegment) {
	    if (pickShapeStart == null) pickShapeStart = new Point3d();
	    if (pickShapeEnd == null) pickShapeEnd = new Point3d();
	    if (pickShapeDir == null) pickShapeDir = new Vector3d();
	    ((PickSegment)pickShape).get(pickShapeStart, pickShapeEnd);
	    pickShapeDir.set (pickShapeEnd.x - pickShapeStart.x, 
			      pickShapeEnd.y - pickShapeStart.y,
			      pickShapeEnd.z - pickShapeStart.z);
	    pickShapeType = PICK_SHAPE_SEGMENT;
	} else if (pickShape instanceof PickBounds) {
	    pickShapeBounds = ((PickBounds) pickShape).get();
	    if ( pickShapeBounds instanceof BoundingBox )
		pickShapeType = PICK_SHAPE_BOUNDING_BOX;
	    else if( pickShapeBounds instanceof BoundingSphere )
		pickShapeType = PICK_SHAPE_BOUNDING_SPHERE;
	    else if( pickShapeBounds instanceof BoundingPolytope )
		pickShapeType = PICK_SHAPE_BOUNDING_POLYTOPE;       
	} else if(pickShape instanceof PickPoint) {
	    throw new RuntimeException ("PickPoint doesn't make sense for geometry-based picking. Java 3D doesn't have spatial information of the surface. Should use PickBounds with BoundingSphere and set radius to a epsilon tolerance.");
	} else if (pickShape instanceof PickCylinder) {
	    pickShapeType = PICK_SHAPE_CYLINDER;
	} else if (pickShape instanceof PickCone) {
	    pickShapeType = PICK_SHAPE_CONE;
	} else {
	    throw new 
		RuntimeException("PickShape not supported for intersection"); 
	}
    }

    /** Get the SceneGraphPath. This will be null if the non SceneGraphPath
     *  constructor was used.
     */
    public SceneGraphPath getSceneGraphPath() {
	/* Q: should this return a copy */
	return pickedSceneGraphPath;
    }


    /** Get the localToVworld transform for the Node 
     */
    public Transform3D getLocalToVworld() {
	return localToVWorld;
    }

    /** Get the GeometryArray at index 0 for the picked node
     */
    public GeometryArray getGeometryArray() {
	if (geometryArrays == null) {
	    storeGeometry();
	}
	return geometryArrays[0];
    }

    /** Get the array of GeometryArrays for the picked node
     */
    public GeometryArray[] getGeometryArrays() {
	if (geometryArrays == null) {
	    storeGeometry();
	}
	return geometryArrays;
    }

    /** Get the number of GeometryArrays for the picked node
     */
    public int numGeometryArrays() {
	if (geometryArrays == null) {
	    storeGeometry();
	}
	return geometryArrays.length;
    }

    /** Get the number of Shape3Ds that came from decompressing a
      CompressedGeometry on the picked node.
      */
    public int numCompressedGeometryShape3Ds() {
	if (geometryArrays == null) {
	    storeGeometry();
	}
	if (compressGeomShape3Ds == null) {
	    return 0;
	} else {
	    return compressGeomShape3Ds.length;
	}
    }

    /** Get the array of Shape3Ds that came from decompressing a
      CompressedGeometry on the picked node.
      */
    public Shape3D[] getCompressedGeometryShape3Ds() {
	if (geometryArrays == null) {
	    storeGeometry();
	}
	if (compressGeomShape3Ds == null) {
	    return null;
	} else {
	    return compressGeomShape3Ds;
	}
    }

    /** Get the PickShape used for intersections 
     */
    public PickShape getPickShape() {
	return pickShape;
    }

    /** Set the PickResult to find only the first intersection of the PickShape
     * with the Node. The default is <code>false</code> (all intersections are 
     * found)
     */
    public void setFirstIntersectOnly(boolean flag) {
	firstIntersectOnly = flag;
    }

    /** Return the "first intersection only" value. */
    public boolean getFirstPickEnable() {
	return firstIntersectOnly;
    }

    /** Returns the number of PickIntersections in the PickResult.
      @return the number of intersections
      */
    public int numIntersections () {
	if (intersections == null) {
	    generateIntersections();
	}
	return intersections.size();
    }

    /** Returns a specific PickIntersection object
      @param index the index number
      @return the PickIntersection referenced by the index number
      */
    public PickIntersection getIntersection (int index) {
	if (intersections == null) {
	    generateIntersections();
	}
	return (PickIntersection) intersections.get (index);
    }

    /** Gets the PickIntersection in this PickResult that is closest to a point
      @param pt the point to use for distance calculations
      @return the closest PickIntersection object
      */
    public PickIntersection getClosestIntersection (Point3d pt) {
	PickIntersection pi = null;
	PickIntersection curPi = null;
	Point3d curPt = null;
	double minDist = Double.MAX_VALUE;
	double curDist = 0.0;

	if (pt == null) return null;

	if (intersections == null) {
	    generateIntersections();
	}

	for (int i=0;i<intersections.size();i++) {
	    if ((null != (curPi = getIntersection(i))) && 
		(null != (curPt = curPi.getPointCoordinatesVW()))) {
		curDist = pt.distance (curPt);
		if (curDist < minDist) {
		    pi = curPi;
		    minDist = curDist;
		}
	    }
	}
	return pi;
    }


    /** 
      Returns String representation 
      @return string representation of this object
      */
    public String toString () {
	String rt = new String ("PickResult: sgp:"+pickedSceneGraphPath+"\n");
	if (pickedNode != null) rt += " node:"+pickedNode;

	
	// TODO: catch cap not set exceptions and return no intersection info
	if (intersections == null) {
	    generateIntersections();
	}

	if (intersections.size() > 0) {
	    for (int i=0;i<intersections.size();i++) {
		rt +="\n";
		rt += ((PickIntersection)intersections.get(i)).toString2();
	    }
	}
	
	return rt;
    }

    /** Store the geometry for the node in this PickResult */
    private void storeGeometry () {
	if (pickedNode instanceof Morph) {
	    geometryArrays = new GeometryArray[1];
	    geometryArrays[0] = 
		(GeometryArray) ((Morph)pickedNode).getGeometryArray (0);
	} else if (pickedNode instanceof Shape3D) {
	    Shape3D shape = ((Shape3D)pickedNode);
	    ArrayList geoArrays = new ArrayList();
	    for (int k = 0; k < shape.numGeometries(); k++) {
		Geometry geometry = shape.getGeometry(k);
		if (geometry instanceof CompressedGeometry) {
		    Shape3D[] sa = ((CompressedGeometry)geometry).decompress();
		    // System.out.println ("Decompressed geometry has "+sa.length+
		    //  " Shape3Ds");
		    if (sa != null) {
			for (int j = 0; j < sa.length; j++) {
			    for (int i = 0; i < sa[j].numGeometries(); i++) {
				geoArrays.add(sa[j].getGeometry(i));
			    }
			}
		    }
		    if (compressGeomShape3Ds == null) {
			// just save the new one
			compressGeomShape3Ds = sa;
		    } else {
			// append the the new one on the end of the old array
			Shape3D[] save = compressGeomShape3Ds;
			int newLength = save.length + sa.length;
			compressGeomShape3Ds = new Shape3D[newLength];
			System.arraycopy(save, 0, compressGeomShape3Ds, 0,
					 save.length);
			System.arraycopy(sa, 0, compressGeomShape3Ds, save.length,
					 sa.length);
		    }
		} else if (geometry instanceof GeometryArray) {
		    geoArrays.add(geometry);
		}
	    }
	    geometryArrays = new GeometryArray[geoArrays.size()];
	    for (int i = 0; i < geoArrays.size(); i++) {
		geometryArrays[i] = (GeometryArray) geoArrays.get(i);
	    }
	} 
	if (geometryArrays == null) {
	    if (pickedNode instanceof Shape3D) {
		Shape3D shape = (Shape3D) pickedNode;
	    }
	    throw new RuntimeException ("Type of the picked node is not supported");
	}
    }

    /** Get the picked node */
    public Node getObject () {
	// get node from scenegraphpath
	if (pickedNode == null) {
	    storeNode ();
	}
	return pickedNode;
    }

    /** Set the picked node */
    void setObject (Node n) {
	pickedNode = n;
    }

    /** Get the first node of a certain type up the SceneGraphPath 
      @param flags the type of node we are interested in
      @return a Node object
      */
    public Node getNode (int flags) {
	if (pickedNode == null) {
	    storeNode ();
	}
	if ((pickedNode instanceof Shape3D) && ((flags & SHAPE3D) != 0)){
	    if (debug) System.out.println("Shape3D found");
	    return pickedNode;
	} 
	else if ((pickedNode instanceof Morph) && ((flags & MORPH) != 0)){
	    if (debug) System.out.println("Morph found"); 
	    return pickedNode;
	}
	else {	  
	    if (pickedSceneGraphPath == null) {
		return null;
	    }
	    for (int j=pickedSceneGraphPath.nodeCount()-1; j>=0; j--){
		Node pNode = pickedSceneGraphPath.getNode(j); 
		if (debug) System.out.println("looking at node " + pNode);
	    
		if ((pNode instanceof Primitive) &&
		    ((flags & PRIMITIVE) != 0)){
		    if (debug) System.out.println("Primitive found");
		    return pNode;
		}
		else if ((pNode instanceof Link) && ((flags & LINK) != 0)){
		    if (debug) System.out.println("Link found");
		    return pNode;
		}
		else if ((pNode instanceof Switch) && ((flags & SWITCH) != 0)){
		    if (debug) System.out.println("Switch found");
		    return pNode;
		}
		else if ((pNode instanceof TransformGroup) &&
			 ((flags & TRANSFORM_GROUP) != 0)){
		    if (debug) System.out.println("xform group found");
		    return pNode;
		}
		else if ((pNode instanceof BranchGroup) &&
			 ((flags & BRANCH_GROUP) != 0)){
		    if (debug) System.out.println("Branch group found");
		    return pNode;
		}
		else if ((pNode instanceof Group) && ((flags & GROUP) != 0)){
		    if (debug) System.out.println("Group found");
		    return pNode;
		}	     
	    }
	}
	return null; // should not be reached
    }

    /** Extract the picked node from the SceneGraphPath */
    void storeNode () {
	if (pickedSceneGraphPath == null) {
	    throw new RuntimeException ("SceneGraphPath missing");
	}
	pickedNode = pickedSceneGraphPath.getObject();
    }

    /** Fill in the intersections of the Node with the PickShape */
    boolean generateIntersections() {
	if (geometryArrays == null) { 
	    storeGeometry();
	}
	intersections = new ArrayList();
	int hits = 0;

	for (int i = 0; i < geometryArrays.length; i++) {
	    if (intersect(i, firstIntersectOnly)) {
		if (firstIntersectOnly) {
		    return true;
		} else {
		    hits++;
		}
	    }
	}
	return (hits > 0);
    }



    /*  Takes a GeometryArray object, determines what actual type 
     *  it is (RTTI) and casts it to call the appropriate intersect method.
     */
    final boolean intersect(int geomIndex, boolean firstpick) {
	int offset;
	GeometryArray geom = geometryArrays[geomIndex];
	int numPts = geom.getVertexCount();
	double[] doubleData = null;
	float[] floatData = null;
	Point3d[] p3dData = null;
	Point3f[] p3fData = null;
	int vformat = geom.getVertexFormat();
	int stride;
	boolean retFlag = false;

	if ((vformat & GeometryArray.BY_REFERENCE) == 0) {
	    doubleData = new double [numPts * 3];
	    geom.getCoordinates (0, doubleData);
	}
	else {
	    if ((vformat & GeometryArray.INTERLEAVED) == 0) {
		doubleData = geom.getCoordRefDouble();
		// If data was set as float then ..
		if (doubleData == null) {
		    floatData = geom.getCoordRefFloat();
		    if (floatData == null) {
			p3fData = geom.getCoordRef3f();
			if (p3fData == null) {
			    p3dData = geom.getCoordRef3d();
			}
		    }
		}
	    }
	    else {
		floatData = geom.getInterleavedVertices();
	    }
	}


	Point3d[] pnts = new Point3d[numPts];	

	/*
	  System.out.println("geomIndex : " + geomIndex);
	  System.out.println("numPts : " + numPts);
	  System.out.println("firstpick : " + firstpick);
	  System.out.println("localToVWorld : ");
	  System.out.println(localToVWorld);
	*/
	
	if (debug) {
	    System.out.println("localToVWorld = " + localToVWorld);
	}
	if ((vformat & GeometryArray.INTERLEAVED) == 0) {
	    if (doubleData != null) {
		offset = 0;
		for (int i=0; i < numPts; i++) {

		    // Need to transform each pnt by localToVWorld.
		    pnts[i] = new Point3d();
		    pnts[i].x = doubleData[offset++];
		    pnts[i].y = doubleData[offset++];
		    pnts[i].z = doubleData[offset++];

		    localToVWorld.transform(pnts[i]);
		}
	    }
	    else  if (floatData != null) { // by reference and float data is defined ..
		offset = 0;
		for (int i=0; i < numPts; i++) {

		    // Need to transform each pnt by localToVWorld.
		    pnts[i] = new Point3d();
		    pnts[i].x = floatData[offset++];
		    pnts[i].y = floatData[offset++];
		    pnts[i].z = floatData[offset++];

		    localToVWorld.transform(pnts[i]);
		}
	    }
	    else if (p3fData != null) {
		for (int i=0; i < numPts; i++) {

		    // Need to transform each pnt by localToVWorld.
		    pnts[i] = new Point3d();
		    pnts[i].set(p3fData[i]);
		    localToVWorld.transform(pnts[i]);
		}
	    }
	    else { // p3dData
		for (int i=0; i < numPts; i++) {

		    // Need to transform each pnt by localToVWorld.
		    pnts[i] = new Point3d();
		    pnts[i].set(p3dData[i]);
		    localToVWorld.transform(pnts[i]);
		}
	    }
	}
	// Its an interleaved type ..
	else {
	    offset = 0;
	    if ((vformat & GeometryArray.COLOR_3) == GeometryArray.COLOR_3) {
		offset += 3;
	    }
	    else if ((vformat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		offset += 4;
	    }
	    if ((vformat & GeometryArray.NORMALS) != 0)
		offset += 3;
	    if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) == GeometryArray.TEXTURE_COORDINATE_2) {
		offset += 2 * geom.getTexCoordSetCount();
	    }
	    else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) == GeometryArray.TEXTURE_COORDINATE_3) {
		offset += 3 * geom.getTexCoordSetCount();
	    }
	    stride = offset + 3; // for the vertices .
	    for (int i=0; i < numPts; i++) {

		// Need to transform each pnt by localToVWorld.
		pnts[i] = new Point3d();
		pnts[i].x = floatData[offset];
		pnts[i].y = floatData[offset+1];
		pnts[i].z = floatData[offset+2];

		localToVWorld.transform(pnts[i]);
		offset += stride;
	    }
	}

	PickIntersection pi = new PickIntersection(this, geom);

	if (geom instanceof PointArray) {
	    retFlag = intersectPA ((PointArray)geom, geomIndex, pnts, firstpick, pi);
	} else if (geom instanceof IndexedPointArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectIPA ((IndexedPointArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else if (geom instanceof LineArray) {
	    retFlag = intersectLA ((LineArray)geom, geomIndex, pnts, firstpick, pi);
	} else if (geom instanceof LineStripArray) {
	    retFlag = intersectLSA ((LineStripArray)geom, geomIndex, pnts, 	
				 firstpick, pi);
	} else if (geom instanceof IndexedLineArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectILA ((IndexedLineArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else if (geom instanceof IndexedLineStripArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectILSA ((IndexedLineStripArray)geom, geomIndex, pnts, 
				  firstpick, pi);
	} else if (geom instanceof TriangleArray) {
	    retFlag = intersectTA ((TriangleArray)geom, geomIndex, pnts, 
				firstpick, pi);
	} else if (geom instanceof TriangleStripArray) {
	    retFlag = intersectTSA ((TriangleStripArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else if (geom instanceof TriangleFanArray) {
	    retFlag = intersectTFA ((TriangleFanArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else if (geom instanceof IndexedTriangleArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectITA ((IndexedTriangleArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else if (geom instanceof IndexedTriangleStripArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectITSA ((IndexedTriangleStripArray)geom, geomIndex, 
				  pnts, firstpick, pi);
	} else if (geom instanceof IndexedTriangleFanArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectITFA ((IndexedTriangleFanArray)geom, geomIndex, 
				  pnts, firstpick, pi);
	} else if (geom instanceof QuadArray) {
	    retFlag = intersectQA ((QuadArray)geom, geomIndex, pnts, firstpick, pi);
	} else if (geom instanceof IndexedQuadArray) {
	    pi.iGeom = (IndexedGeometryArray) geom;
	    retFlag = intersectIQA ((IndexedQuadArray)geom, geomIndex, pnts,
				 firstpick, pi);
	} else {
	    throw new RuntimeException ("incorrect class type");
	}
	return retFlag;

    }



    /* ==================================================================== */
    /*                 INTERSECT METHODS BY PRIMITIVE TYPE                  */
    /* ==================================================================== */

    boolean intersectPoint(int[] vertidx, int[] coordidx, int geomIndex, 
			   Point3d[] pnts, PickIntersection pi) {
	// PickIntersection pi = new PickIntersection(this);

	Point3d[] point = new Point3d[1];
	point[0] = pnts[coordidx[0]];

	if (debug) {
	    System.out.println("intersect point, point = " + point[0]);
	}

	boolean intersect = false;
	switch(pickShapeType) {
	case PICK_SHAPE_RAY:
	    intersect = intersectPntAndRay(point[0], pickShapeStart, 
					   pickShapeDir, pi);
	    break;
	case PICK_SHAPE_SEGMENT:
	    if (intersectPntAndRay(point[0], pickShapeStart, pickShapeDir, pi)){
		if(pi.getDistance() <= 1.0) { // TODO: why 1.0?
		    intersect = true;
		}
	    }
	    break;
	    /* case PICK_SHAPE_POINT:
	       intersect = intersectPntAndPnt(point[0],
	       ((PickPoint) pickShape).location );
	       break;
	       */
	case PICK_SHAPE_BOUNDING_BOX:
	    intersect = ((BoundingBox)pickShapeBounds).intersect(point[0]);
	    pi.setPointCoordinatesVW(point[0]);
	    break;
	case PICK_SHAPE_BOUNDING_SPHERE:
	    intersect = ((BoundingSphere)pickShapeBounds).intersect(point[0]);
	    pi.setPointCoordinatesVW(point[0]);
	    break;
	case PICK_SHAPE_BOUNDING_POLYTOPE:
	    intersect = ((BoundingPolytope)pickShapeBounds).intersect(point[0]);
	    pi.setPointCoordinatesVW(point[0]);
	    break;
	case PICK_SHAPE_CYLINDER:
	    intersect = intersectCylinder(point[0], (PickCylinder)pickShape,pi);
	    break;
	case PICK_SHAPE_CONE:
	    intersect = intersectCone (point[0], (PickCone)pickShape, pi);
	    break;
	}
	if (intersect) {
	    PickIntersection newpi = new PickIntersection(this, pi.geom);
	    newpi.iGeom = pi.iGeom;
	    newpi.setDistance(pi.distance);
	    newpi.setPointCoordinatesVW(pi.getPointCoordinatesVW());
	    
	    // Set PickIntersection parameters
	    newpi.setGeomIndex(geomIndex);
	    newpi.setVertexIndices (vertidx);
	    newpi.setPrimitiveCoordinatesVW(point);
	    intersections.add (newpi);
	    return true;
	}
	return false;
    } 

    boolean intersectLine(int[] vertidx, int[] coordidx, int geomIndex, 
			  Point3d[] pnts, PickIntersection pi) {
	
	Point3d[] linePts = new Point3d[2];
	linePts[0] = pnts[coordidx[0]];
	linePts[1] = pnts[coordidx[1]];
	
	boolean intersect = false;
	switch(pickShapeType) {
	case PICK_SHAPE_RAY:
	    intersect = intersectLineAndRay(linePts[0], linePts[1], 
					    pickShapeStart, pickShapeDir, pi);
	    break;
	case PICK_SHAPE_SEGMENT:
	    if (intersectLineAndRay(linePts[0], linePts[1], pickShapeStart, 
				    pickShapeDir, pi)) {
		if (pi.getDistance() <= 1.0) {
		    intersect = true;
		}
	    }
	    break;
	    /* case PICK_SHAPE_POINT:
	       dir.x = linePts[1].x - linePts[0].x;       
	       dir.y = linePts[1].y - linePts[0].y;       
	       dir.z = linePts[1].z - linePts[0].z;       
	       if (intersectPntAndRay(((PickPoint)pickShape).location, 
	       pnts[0], dir, dist)) {
	       if(dist[0] <= 1.0) {
	       intersect = true;
	       }
	       }
	       break;
	       */
	case PICK_SHAPE_BOUNDING_BOX:
	    intersect = intersectBoundingBox(linePts,
					     (BoundingBox)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_SPHERE:
	    intersect = intersectBoundingSphere(linePts, 
						(BoundingSphere)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_POLYTOPE:
	    intersect = intersectBoundingPolytope(linePts,
						  (BoundingPolytope)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;    
	case PICK_SHAPE_CYLINDER:
	    intersect = intersectCylinder (linePts, (PickCylinder)pickShape,pi);
	    break;
	case PICK_SHAPE_CONE:
	    intersect = intersectCone (linePts, (PickCone) pickShape, pi);
	    break;
	}
	if (intersect) {
	    PickIntersection newpi = new PickIntersection(this, pi.geom);
	    newpi.iGeom = pi.iGeom;
	    newpi.setDistance(pi.distance);
	    newpi.setPointCoordinatesVW(pi.getPointCoordinatesVW());

	    // Set PickIntersection parameters
	    newpi.setGeomIndex(geomIndex);
	    newpi.setVertexIndices (vertidx);
	    newpi.setPrimitiveCoordinatesVW(linePts);
	    intersections.add (newpi);
	    return true;
	}
	return false;
    }

    boolean intersectTri(int[] vertidx, int[] coordidx, int geomIndex, 
			 Point3d[] pnts, PickIntersection pi) {

	Point3d[] triPts = new Point3d[3];
	
	triPts[0] = pnts[coordidx[0]];
	triPts[1] = pnts[coordidx[1]];
	triPts[2] = pnts[coordidx[2]];
	

	boolean intersect = false;
	switch(pickShapeType) {
	case PICK_SHAPE_RAY:
	    intersect = intersectRay(triPts, (PickRay) pickShape, pi);
	    break;
	case PICK_SHAPE_SEGMENT:
	    intersect = intersectSegment(triPts, (PickSegment) pickShape, pi);
	    break;
	    /* case PICK_SHAPE_POINT:
	       if(inside(triPts, (PickPoint) pickShape, ccw)==false)
	       return false;  
	       break;
	       */
	case PICK_SHAPE_BOUNDING_BOX:
	    intersect = intersectBoundingBox (triPts, 
					      (BoundingBox)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_SPHERE:
	    intersect = intersectBoundingSphere (triPts, 
						 (BoundingSphere)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_POLYTOPE:
	    intersect = intersectBoundingPolytope (triPts, 
						   (BoundingPolytope)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_CYLINDER:
	    intersect = intersectCylinder (triPts, (PickCylinder) pickShape,pi);
	    break;
	case PICK_SHAPE_CONE:
	    intersect = intersectCone (triPts, (PickCone)pickShape, pi);
	    break;
	}
	if (intersect) {
	    PickIntersection newpi = new PickIntersection(this, pi.geom);
	    newpi.iGeom = pi.iGeom;
	    newpi.setDistance(pi.distance);
	    newpi.setPointCoordinatesVW(pi.getPointCoordinatesVW());

	    // Set PickIntersection parameters
	    newpi.setGeomIndex(geomIndex);
	    newpi.setVertexIndices (vertidx);

	    newpi.setPrimitiveCoordinatesVW(triPts);
	    intersections.add (newpi);
	    return true;
	}
	return false;
    }

    boolean intersectQuad(int[] vertidx, int[] coordidx, int geomIndex, 
			  Point3d[] pnts, PickIntersection pi) {

	Point3d[] quadPts = new Point3d[4];
	
	quadPts[0] = pnts[coordidx[0]];
	quadPts[1] = pnts[coordidx[1]];
	quadPts[2] = pnts[coordidx[2]];
	quadPts[3] = pnts[coordidx[3]];

	// PickIntersection pi = new PickIntersection(this);

	boolean intersect = false;
	switch(pickShapeType) {
	case PICK_SHAPE_RAY:
	    intersect = intersectRay(quadPts, (PickRay) pickShape, pi);
	    break;
	case PICK_SHAPE_SEGMENT:
	    intersect = intersectSegment(quadPts, (PickSegment) pickShape, pi);
	    break;
	    /* case PICK_SHAPE_POINT:
	       if(inside(quadPts, (PickPoint) pickShape, ccw)==false)
	       return false;  
	       break;
	       */
	case PICK_SHAPE_BOUNDING_BOX:
	    intersect = intersectBoundingBox (quadPts, 
					      (BoundingBox)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_SPHERE:
	    intersect = intersectBoundingSphere (quadPts, 
						 (BoundingSphere)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_BOUNDING_POLYTOPE:
	    intersect = intersectBoundingPolytope (quadPts, 
						   (BoundingPolytope)pickShapeBounds);
	    pi.setPointCoordinatesVW(zeroPnt);
	    break;
	case PICK_SHAPE_CYLINDER:
	    intersect = intersectCylinder (quadPts, (PickCylinder)pickShape,pi);
	    break;
	case PICK_SHAPE_CONE:
	    intersect = intersectCone (quadPts, (PickCone)pickShape, pi);
	    break;
	}
	if (intersect) {	    
	    PickIntersection newpi = new PickIntersection(this, pi.geom);
	    newpi.iGeom = pi.iGeom;
	    newpi.setDistance(pi.distance);
	    newpi.setPointCoordinatesVW(pi.getPointCoordinatesVW());
	    
	    // Set PickIntersection parameters
	    newpi.setGeomIndex(geomIndex);
	    newpi.setVertexIndices (vertidx);
	    newpi.setPrimitiveCoordinatesVW(quadPts);
	    intersections.add (newpi);
	    return true;
	}
	return false;
    }

    /* ==================================================================== */
    /*                 INTERSECT METHODS BY GEOMETRY TYPE                   */
    /* ==================================================================== */

    /** 
      Intersect method for PointArray 
      */
    boolean intersectPA (PointArray geom, int geomIndex, Point3d[] pnts, 
			 boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println ("intersect: PointArray");

	int[] pntVertIdx = new int[1];
	int numint = 0;

	for (int i = 0; i < pnts.length; i++) {
	    pntVertIdx[0] = i;
	    if (intersectPoint(pntVertIdx, pntVertIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedPointArray 
      */
    boolean intersectIPA (IndexedPointArray geom, int geomIndex, Point3d[] pnts,
			  boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println ("intersect: IndexedPointArray");

	int[] pntVertIdx = new int[1];
	int[] pntCoordIdx = new int[1];

	int numint = 0;
	int indexCount = geom.getIndexCount();
	
	for (int i=0; i< indexCount; i++) {
	    pntVertIdx[0] = i;
	    pntCoordIdx[0] = geom.getCoordinateIndex(i);
	    if (intersectPoint(pntVertIdx, pntCoordIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	}
	if (numint > 0) return true;
	return false;
    }


    /** 
      Intersect method for LineArray 
      */
    /** 
      Intersect method for LineArray 
      */
    boolean intersectLA (LineArray geom, int geomIndex, Point3d[] pnts,
			 boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println ("intersect: LineArray");

	int[] lineVertIdx = new int[2];

	int numint = 0;

	for (int i=0; i< pnts.length;) {
	    /* set up the parameters for the current line */
	    lineVertIdx[0] = i++;
	    lineVertIdx[1] = i++;
	    if (intersectLine(lineVertIdx, lineVertIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	} 
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for LineStripArray 
      */
    boolean intersectLSA (LineStripArray geom, int geomIndex, Point3d[] pnts,
			  boolean firstpick, PickIntersection pi) {
	int numint = 0;

	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripVertexCounts (stripVertexCounts);
	int stripStart = 0;
	
	if (debug) System.out.println ("intersect: LineStripArray");

	int[] lineVertIdx = new int[2];

	for (int i=0; i < stripVertexCounts.length; i++) {  
	    lineVertIdx[0] = stripStart;
	    int end = stripStart + stripVertexCounts[i];
	    
	    for (int j=stripStart+1; j<end; j++) {
		lineVertIdx[1] = j;
		if (intersectLine(lineVertIdx, lineVertIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}
		lineVertIdx[0] = lineVertIdx[1];
	    }
	    stripStart += stripVertexCounts[i];
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedLineArray 
      */
    boolean intersectILA (IndexedLineArray geom, int geomIndex, Point3d[] pnts,
			  boolean firstpick, PickIntersection pi) {

	int numint = 0;
	int indexCount = geom.getIndexCount();
	if (debug) System.out.println ("intersect: IndexedLineArray");

	int[] lineVertIdx = new int[2];
	int[] lineCoordIdx = new int[2];

	for (int i=0; i<indexCount;) {
	    lineVertIdx[0] = i;
	    lineCoordIdx[0] = geom.getCoordinateIndex(i++);
	    lineVertIdx[1] = i;
	    lineCoordIdx[1] = geom.getCoordinateIndex(i++);
	    if (intersectLine(lineVertIdx, lineCoordIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedLineStripArray 
      */
    boolean intersectILSA (IndexedLineStripArray geom, int geomIndex, 
			   Point3d[] pnts, boolean firstpick, PickIntersection pi) {
	if (debug) System.out.println ("intersect: IndexedLineStripArray");

	int[] lineVertIdx = new int[2];
	int[] lineCoordIdx = new int[2];

	int numint = 0;
	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripIndexCounts (stripVertexCounts);
	int stripStart = 0;
	
	for (int i=0; i < stripVertexCounts.length; i++) {  

	    lineVertIdx[0] = stripStart;
	    lineCoordIdx[0] = geom.getCoordinateIndex(stripStart);
	    int end = stripStart + stripVertexCounts[i];
	    for (int j=stripStart+1; j<end; j++) {
		lineVertIdx[1] = j;
		lineCoordIdx[1] = geom.getCoordinateIndex(j);
		if (intersectLine(lineVertIdx, lineCoordIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}
		lineVertIdx[0] = lineVertIdx[1];
		lineCoordIdx[0] = lineCoordIdx[1];
	    }
	    stripStart += stripVertexCounts[i];
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for TriangleArray 
      */
    boolean intersectTA (TriangleArray geom, int geomIndex, Point3d[] pnts,
			 boolean firstpick, PickIntersection pi) {

	if (debug) 
	    System.out.println ("intersect: TriangleArray");

	int[] triVertIdx = new int[3];

	int numint = 0;
	for (int i=0; i<pnts.length;) {
	    triVertIdx[0] = i++;
	    triVertIdx[1] = i++;
	    triVertIdx[2] = i++;
	    if (intersectTri(triVertIdx, triVertIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	} 

	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedTriangleArray 
      */
    boolean intersectITA (IndexedTriangleArray geom, int geomIndex, 
			  Point3d[] pnts, boolean firstpick, PickIntersection pi) {
	
	if (debug)
	    System.out.println ("intersect: IndexedTriangleArray");

	int[] triVertIdx = new int[3];
	int[] triCoordIdx = new int[3];
	
	int numint = 0;
	int indexCount = geom.getIndexCount();
	for (int i=0; i<indexCount;) {
	    triVertIdx[0] = i;
	    triCoordIdx[0] = geom.getCoordinateIndex(i++);
	    triVertIdx[1] = i;
	    triCoordIdx[1] = geom.getCoordinateIndex(i++);
	    triVertIdx[2] = i;
	    triCoordIdx[2] = geom.getCoordinateIndex(i++);
	    if (intersectTri(triVertIdx, triCoordIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	} 

	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for TriangleStripArray 
      */
    boolean intersectTSA (TriangleStripArray geom, int geomIndex, 
			  Point3d[] pnts, boolean firstpick, PickIntersection pi) {
	if (debug) 
	    System.out.println ("intersect: TriangleStripArray");

	boolean ccw;
	int numint = 0;
	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripVertexCounts (stripVertexCounts);
	int stripStart = 0;
	int start;
	int[] triVertIdx = new int[3];

	for (int i=0; i<stripVertexCounts.length; i++) {  

	    start = stripStart;
	    // start a new strip
	    ccw = true;
	    triVertIdx[0] = start++;
	    triVertIdx[1] = start++;

	    int end = start + stripVertexCounts[i] - 2;
	    for (int j=start; j< end; j++) {	 
		/*
		if (ccw) {
		    triVertIdx[2] = j;   
		} else {
		    triVertIdx[1] = j;   
		}
		*/
		triVertIdx[2] = j;
		if (intersectTri(triVertIdx, triVertIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}

		// Advance to the next triangle, keeping the winding of the test
		// triangle correct.  
		/*
		if (ccw) {
		    triVertIdx[0] = triVertIdx[1];
		    // triVertIdx[2] remains, triVertIdx[1] will be replaced
		    ccw = false;
		} else {
		    triVertIdx[0] = triVertIdx[2];
		    // triVertIdx[1] remains, triVertIdx[2] will be replaced
		    ccw = true;
		}
		*/
		triVertIdx[0] = triVertIdx[1];
		triVertIdx[1] = triVertIdx[2];

	    }
	    stripStart += stripVertexCounts[i];
	}

	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedTriangleStripArray 
      */
    boolean intersectITSA (IndexedTriangleStripArray geom, int geomIndex, 
			   Point3d[] pnts, boolean firstpick, PickIntersection pi) {

	if (debug) 
	    System.out.println ("intersect: IndexedTriangleStripArray");
	int numint = 0;
	boolean ccw;

	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripIndexCounts (stripVertexCounts);
	int stripStart = 0;
	int start;
	int[] triVertIdx = new int[3];
	int[] triCoordIdx = new int[3];
	
	for (int i=0; i<stripVertexCounts.length; i++) {  

	    start = stripStart;
	    // start a new strip
	    ccw = true;
	    triCoordIdx[0] = geom.getCoordinateIndex(start);
	    triVertIdx[0] = start++;
	    triCoordIdx[1] = geom.getCoordinateIndex(start);	    
	    triVertIdx[1] = start++;
	    
	    int end = start + stripVertexCounts[i] - 2;
	    for (int j=start; j<end; j++) {	 
		if (ccw) {
		    triVertIdx[2] = j;   
		    triCoordIdx[2] = geom.getCoordinateIndex(j);
		} else {
		    triVertIdx[1] = j;   
		    triCoordIdx[1] = geom.getCoordinateIndex(j);
		}
		
		if (intersectTri(triVertIdx, triCoordIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}

		// Advance to the next triangle, keeping the winding of the test
		// triangle correct.  
		if (ccw) {
		    triVertIdx[0] = triVertIdx[1];
		    // triVertIdx[2] remains, triVertIdx[1] will be replaced
		    triCoordIdx[0] = triCoordIdx[1];
		    ccw = false; 
		} else { 
		    triVertIdx[0] = triVertIdx[2];
		    // triVertIdx[1] remains, triVertIdx[2] will be replaced
		    triCoordIdx[0] = triCoordIdx[2];
		    ccw = true;
		}
	    }
	    stripStart += stripVertexCounts[i];
	}

	if (numint > 0) return true;
	return false;

    }

    /** 
      Intersect method for TriangleFanArray 
      */
    boolean intersectTFA (TriangleFanArray geom, int geomIndex, Point3d[] pnts,
			  boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println("intersect: TriangleFanArray");

	int numint = 0;

	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripVertexCounts (stripVertexCounts);
	int fanStart = 0;
	int start;
	int[] triVertIdx = new int[3];
	
	// System.out.println("stripVertexCounts.length " + stripVertexCounts.length);
	for (int i=0; i<stripVertexCounts.length; i++) {

	    start = fanStart;
	    triVertIdx[0] = start++;
	    triVertIdx[1] = start++;
	    
	    int end = start + stripVertexCounts[i] - 2;
	    for (int j=start; j<end; j++) {	 
		triVertIdx[2] = j;
		if (intersectTri(triVertIdx, triVertIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}
		triVertIdx[1] = triVertIdx[2];
	    }    
	    fanStart += stripVertexCounts[i];
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedTriangleFanArray 
      */
    boolean intersectITFA (IndexedTriangleFanArray geom, int geomIndex, 
			   Point3d[] pnts, boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println ("intersect: IndexedTriangleFanArray");

	int numint = 0;
	int[] stripVertexCounts = new int [geom.getNumStrips()];
	geom.getStripIndexCounts (stripVertexCounts);
	int fanStart = 0;
	int start;
	int[] triVertIdx = new int[3];
	int[] triCoordIdx = new int[3];
	
	for (int i=0; i<stripVertexCounts.length; i++) {

	    start = fanStart;
	    triCoordIdx[0] = geom.getCoordinateIndex(start);
	    triVertIdx[0] = start++;
	    triCoordIdx[1] = geom.getCoordinateIndex(start);
	    triVertIdx[1] = start++;
	    
	    int end = start + stripVertexCounts[i] - 2;
	    for (int j=start; j<end; j++) {	 
		triVertIdx[2] = j;
		triCoordIdx[2] = geom.getCoordinateIndex(j);
		if (intersectTri(triVertIdx, triCoordIdx, geomIndex, pnts, pi)) {
		    numint++;
		    if (firstpick) return true;
		}
		triVertIdx[1] = triVertIdx[2];
		triCoordIdx[1] = triCoordIdx[2];
	    }    
	    fanStart += stripVertexCounts[i];
	}
	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for QuadArray 
      */
    boolean intersectQA (QuadArray geom, int geomIndex, Point3d[] pnts,
			 boolean firstpick, PickIntersection pi) {

	if (debug) System.out.println ("intersect: QuadArray");

	int[] quadVertIdx = new int[4];

	int numint = 0;
	for (int i=0; i<pnts.length;) {
	    quadVertIdx[0] = i++;
	    quadVertIdx[1] = i++;
	    quadVertIdx[2] = i++;
	    quadVertIdx[3] = i++;
	    if (intersectQuad(quadVertIdx, quadVertIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	} 

	if (numint > 0) return true;
	return false;
    }

    /** 
      Intersect method for IndexedQuadArray 
      */
    final boolean intersectIQA (IndexedQuadArray geom, int geomIndex, 
				Point3d[] pnts, boolean firstpick,
				PickIntersection pi) {

	if (debug) System.out.println ("intersect: IndexedQuadArray");

	int[] quadVertIdx = new int[4];
	int[] quadCoordIdx = new int[4];

	int numint = 0;
	int indexCount = geom.getIndexCount();
	// System.out.println ("intersect: IndexedQuadArray : indexCount " + indexCount);
	for (int i=0; i<indexCount;) {
	    quadVertIdx[0] = i;
	    quadCoordIdx[0] = geom.getCoordinateIndex(i++);
	    quadVertIdx[1] = i;
	    quadCoordIdx[1] = geom.getCoordinateIndex(i++);
	    quadVertIdx[2] = i;
	    quadCoordIdx[2] = geom.getCoordinateIndex(i++);
	    quadVertIdx[3] = i;
	    quadCoordIdx[3] = geom.getCoordinateIndex(i++);

	    if (intersectQuad(quadVertIdx, quadCoordIdx, geomIndex, pnts, pi)) {
		numint++;
		if (firstpick) return true;
	    }
	} 

	if (numint > 0) return true;
	return false;

    }

    /* ==================================================================== */
    /*                      GENERAL INTERSECT METHODS                       */
    /* ==================================================================== */
    static boolean intersectBoundingBox (Point3d coordinates[], 
					 BoundingBox box) {
	int i, j;
	int out[] = new int[6];      

	Point3d lower = new Point3d();
	Point3d upper = new Point3d();
	box.getLower (lower);
	box.getUpper (upper);
       
	//Do trivial vertex test.
	for (i=0; i<6; i++) out[i] = 0;
	for (i=0; i<coordinates.length; i++) {
	    if ((coordinates[i].x >= lower.x) && 
		(coordinates[i].x <= upper.x) &&
		(coordinates[i].y >= lower.y) && 
		(coordinates[i].y <= upper.y) &&
		(coordinates[i].z >= lower.z) && 
		(coordinates[i].z <= upper.z)) {
		// We're done! It's inside the boundingbox.
		return true;	  
	    } else {
		if (coordinates[i].x < lower.x) out[0]++; // left
		if (coordinates[i].y < lower.y) out[1]++; // bottom
		if (coordinates[i].z < lower.z) out[2]++; // back
		if (coordinates[i].x > upper.x) out[3]++; // right
		if (coordinates[i].y > upper.y) out[4]++; // top
		if (coordinates[i].z > upper.z) out[5]++; // front	  
	    }
	}
      
	if ((out[0] == coordinates.length) || (out[1] == coordinates.length) ||
	    (out[2] == coordinates.length) || (out[3] == coordinates.length) ||
	    (out[4] == coordinates.length) || (out[5] == coordinates.length)){
	    // we're done. primitive is outside of boundingbox.
	    return false;
	}      
	// Setup bounding planes.
	Point3d pCoor[] = new Point3d[4];
	for (i=0; i<4; i++) pCoor[i] = new Point3d();
      
	// left plane.
	pCoor[0].set(lower.x, lower.y, lower.z);
	pCoor[1].set(lower.x, lower.y, upper.z);
	pCoor[2].set(lower.x, upper.y, upper.z);
	pCoor[3].set(lower.x, upper.y, lower.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;

	// right plane.
	pCoor[0].set(upper.x, lower.y, lower.z);
	pCoor[1].set(upper.x, upper.y, lower.z);
	pCoor[2].set(upper.x, upper.y, upper.z);
	pCoor[3].set(upper.x, lower.y, upper.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;

	// bottom plane.
	pCoor[0].set(upper.x, lower.y, upper.z);
	pCoor[1].set(lower.x, lower.y, upper.z);
	pCoor[2].set(lower.x, lower.y, lower.z);
	pCoor[3].set(upper.x, lower.y, lower.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;

	// top plane.
	pCoor[0].set(upper.x, upper.y, upper.z);
	pCoor[1].set(upper.x, upper.y, lower.z);
	pCoor[2].set(lower.x, upper.y, lower.z);
	pCoor[3].set(lower.x, upper.y, upper.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;

	// front plane.
	pCoor[0].set(upper.x, upper.y, upper.z);
	pCoor[1].set(lower.x, upper.y, upper.z);
	pCoor[2].set(lower.x, lower.y, upper.z);
	pCoor[3].set(upper.x, lower.y, upper.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;
      
	// back plane.
	pCoor[0].set(upper.x, upper.y, lower.z);
	pCoor[1].set(upper.x, lower.y, lower.z);
	pCoor[2].set(lower.x, lower.y, lower.z);
	pCoor[3].set(lower.x, upper.y, lower.z);
	if (intersectPolygon(pCoor, coordinates, false) == true) return true;

	return false;
    }

    static boolean intersectBoundingSphere (Point3d coordinates[], 
					    BoundingSphere sphere) {


	int i, j;
	Vector3d tempV3D = new Vector3d();
	boolean esFlag;
	Point3d center = new Point3d();
	sphere.getCenter (center);
	double radius = sphere.getRadius ();
	//Do trivial vertex test.

	for (i=0; i<coordinates.length; i++) {
	    tempV3D.x = coordinates[i].x - center.x;
	    tempV3D.y = coordinates[i].y - center.y;
	    tempV3D.z = coordinates[i].z - center.z;
	
	    if (tempV3D.length() <= radius) {
		// We're done! It's inside the boundingSphere.
		return true;
	    }
	}

	for (i=0; i<coordinates.length; i++) {
	    if (i < (coordinates.length-1)) {
		esFlag = edgeIntersectSphere(sphere, coordinates[i], 
					     coordinates[i+1]);
	    } else {
		esFlag = edgeIntersectSphere(sphere, coordinates[i], 
					     coordinates[0]);
	    }
	    if (esFlag == true) {
		return true;
	    }
	}

	if (coordinates.length < 3) return false; // We're done with line.

	// Find rho.
	// Compute plane normal.
	Vector3d vec0 = new Vector3d(); //Edge vector from point 0 to point 1;
	Vector3d vec1 = new Vector3d(); //Edge vector from point 0 to point 2 or 3;
	Vector3d pNrm = new Vector3d();
	Vector3d pa = new Vector3d();
	Point3d q = new Point3d();
	double nLenSq, pqLen, pNrmDotPa, tq;

	// compute plane normal for coordinates.
	for (i=0; i<coordinates.length-1;) {
	    vec0.x = coordinates[i+1].x - coordinates[i].x;
	    vec0.y = coordinates[i+1].y - coordinates[i].y;
	    vec0.z = coordinates[i+1].z - coordinates[i++].z;
	    if (vec0.length() > 0.0) break;
	}
        
	for (j=i; j<coordinates.length-1; j++) {
	    vec1.x = coordinates[j+1].x - coordinates[j].x;
	    vec1.y = coordinates[j+1].y - coordinates[j].y;
	    vec1.z = coordinates[j+1].z - coordinates[j].z;
	    if (vec1.length() > 0.0) break;
	}
      
	if (j == (coordinates.length-1)) {
	    // System.out.println("(1) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}

	/*
	  for (i=0; i<coordinates.length; i++) 
	  System.out.println("coordinates P" + i + " " + coordinates[i]);
	  for (i=0; i<coord2.length; i++) 
	  System.out.println("coord2 P" + i + " " + coord2[i]);
	  */
      
	pNrm.cross(vec0,vec1);
      
	nLenSq = pNrm.lengthSquared(); 
	if ( nLenSq == 0.0) {
	    // System.out.println("(2) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}

	pa.x = coordinates[0].x - center.x;
	pa.y = coordinates[0].y - center.y;
	pa.z = coordinates[0].z - center.z;

	pNrmDotPa = pNrm.dot(pa);
      
	pqLen = Math.sqrt(pNrmDotPa * pNrmDotPa/ nLenSq);
      
	if (pqLen > radius)
	    return false;

	tq = pNrmDotPa / nLenSq;

	q.x = center.x + tq * pNrm.x;
	q.y = center.y + tq * pNrm.y;
	q.z = center.z + tq * pNrm.z;

	// PolyPnt2D Test.
	return pointIntersectPolygon2D( pNrm, coordinates, q);
    }

    static boolean intersectBoundingPolytope (Point3d coordinates[], 
					      BoundingPolytope polytope) {
      
	boolean debug = false;    
    
	// this is a multiplier to the halfplane distance coefficients
	double distanceSign = -1.0;
	// Variable needed for intersection.
	Point4d tP4d = new Point4d();

	Vector4d[] planes = new Vector4d [polytope.getNumPlanes()];

	for(int i=0; i<planes.length; i++)
	    planes[i]=new Vector4d();

	polytope.getPlanes (planes);

	if (coordinates.length == 2) {
	    // we'll handle line separately.
	    throw new java.lang.RuntimeException ("TODO: must make polytope.intersect(coordinates[0], coordinates[1], tP4d) public!");
	    // TODO: must make this public !!!
	    //      return polytope.intersect(coordinates[0], coordinates[1], tP4d );
	}

	// It is a triangle or a quad.
      
	// first test to see if any of the coordinates are all inside of the
	// intersection polytope's half planes
	// essentially do a matrix multiply of the constraintMatrix K*3 with
	// the input coordinates 3*1 = K*1 vector

	if (debug) { 
	    System.out.println("The value of the input vertices are: ");
	    for (int i=0; i < coordinates.length; i++) {
		System.out.println("The " +i+ " th vertex is: " + coordinates[i]);
	    }
      
	    System.out.println("The value of the input bounding Polytope's planes =");
	    for (int i=0; i < planes.length; i++) {
		System.out.println("The " +i+ " th plane is: " + planes[i]);
	    }
      
	}
    
	// the direction for the intersection cost function
	double centers[] = new double[4];
	centers[0] = 0.8; centers[1] = 0.9; centers[2] = 1.1; centers[3] = 1.2;
      
	boolean intersection = true;
	boolean PreTest = false;
    
	if (PreTest) {
	    // for each coordinate, test it with each half plane
	    for (int i=0; i < coordinates.length; i++) {
		for (int j=0; j < planes.length; j++) {
		    if ((planes[j].x * coordinates[i].x +
			 planes[j].y * coordinates[i].y +
			 planes[j].z*coordinates[i].z) <= 
			(distanceSign)*planes[j].w){
			// the point satisfies this particular hyperplane
			intersection = true;
		    } else {
			// the point fails this hyper plane try with a new hyper plane
			intersection = false;
			break;
		    }
		}
		if (intersection) {
		    // a point was found to be completely inside the bounding hull
		    return true;
		}
	    }
	}  // end of pretest
    
	// at this point all points are outside of the bounding hull
	// build the problem tableau for the linear program
    
	int numberCols = planes.length + 2 + coordinates.length + 1;
	int numberRows = 1 + coordinates.length;
    
	double problemTableau[][] = new double[numberRows][numberCols];
    
	// compute -Mtrans = -A*P
	for ( int i = 0; i < planes.length; i++) {
	    for ( int j=0; j < coordinates.length;  j++) {
		problemTableau[j][i] = (-1.0)* (planes[i].x*coordinates[j].x+
						planes[i].y*coordinates[j].y+
						planes[i].z*coordinates[j].z);
	    }
	}
    
	// add the other rows
	for (int i = 0; i < coordinates.length; i++) {
	    problemTableau[i][planes.length] = -1.0;
	    problemTableau[i][planes.length + 1] =  1.0;
      
	    for (int j=0; j < coordinates.length; j++) {
		if ( i==j ) {
		    problemTableau[i][j + planes.length + 2] = 1.0;
		} else {
		    problemTableau[i][j + planes.length + 2] = 0.0;
		}
	
		// place the last column elements the Ci's
		problemTableau[i][numberCols - 1] = centers[i];
	    }
	}
    
	// place the final rows value
	for (int j = 0; j < planes.length; j++) {
	    problemTableau[numberRows - 1][j] = 
		(distanceSign)*planes[j].w;
	}
	problemTableau[numberRows - 1][planes.length] =  1.0;
	problemTableau[numberRows - 1][planes.length+1] = -1.0;
	for (int j = 0; j < coordinates.length; j++) {
	    problemTableau[numberRows - 1][planes.length+2+j] = 0.0;
	}
    
	if (debug) {
	    System.out.println("The value of the problem tableau is: " );
	    for (int i=0; i < problemTableau.length; i++) {
		for (int j=0; j < problemTableau[0].length; j++) {
		    System.out.print(problemTableau[i][j] + "  ");
		}
		System.out.println();
	    }
	}
    
	double distance = generalStandardSimplexSolver(problemTableau, 
						       Float.NEGATIVE_INFINITY);
	if (debug) {
	    System.out.println("The value returned by the general standard simplex = " +
			       distance);
	}
	if (distance == Float.POSITIVE_INFINITY) {
	    return false;
	} 
	return true;
    }


    // optimized version using arrays of doubles, but using the standard simplex
    // method to solve the LP tableau.  This version has not been optimized to
    // work with a particular size input tableau and is much slower than some
    // of the other variants...supposedly
    static double generalStandardSimplexSolver(double problemTableau[][], 
					       double stopingValue) {
	boolean debug = false;
	int numRow = problemTableau.length;
	int numCol = problemTableau[0].length;
	boolean optimal = false;
	int i, pivotRowIndex, pivotColIndex;
	double maxElement, element, endElement, ratio, prevRatio;
	int count = 0;
	double multiplier;
    
	if (debug) {
	    System.out.println("The number of rows is : " + numRow);
	    System.out.println("The number of columns is : " + numCol);
	}
    
	// until the optimal solution is found continue to do
	// iterations of the simplex method
	while(!optimal) {

	    if (debug) {
		System.out.println("input problem tableau is:");
		for (int k=0; k < numRow; k++) {
		    for (int j=0; j < numCol; j++) {
			System.out.println("kth, jth value is:" +k+" "+j+" : " +
					   problemTableau[k][j]);
		    }
		}
	    }
      
	    // test to see if the current solution is optimal
	    // check all bottom row elements except the right most one and
	    // if all positive or zero its optimal
	    for (i = 0, maxElement = 0, pivotColIndex = -1; i < numCol - 1; i++) {
		// a bottom row element
		element = problemTableau[numRow - 1][i];
		if ( element < maxElement) {
		    maxElement = element;
		    pivotColIndex = i;
		}
	    }
      
	    // if there is no negative non-zero element then we
	    // have found an optimal solution (the last row of the tableau)
	    if (pivotColIndex == -1) {
		// found an optimal solution
		//System.out.println("Found an optimal solution");
		optimal = true;
	    }
      
	    //System.out.println("The value of maxElement is:" + maxElement);
      
	    if (!optimal) {
		// Case when the solution is not optimal but not known to be
		// either unbounded or infeasable
	
		// from the above we have found the maximum negative element in
		// bottom row, we have also found the column for this value
		// the pivotColIndex represents this
	
		// initialize the values for the algorithm, -1 for pivotRowIndex
		// indicates no solution
	
		prevRatio = Float.POSITIVE_INFINITY;
		ratio = 0.0;
		pivotRowIndex = -1;
	
		// note if all of the elements in the pivot column are zero or
		// negative the problem is unbounded.
		for (i = 0; i < numRow - 1; i++) {
		    element = problemTableau[i][pivotColIndex]; // r value
		    endElement = problemTableau[i][numCol-1]; // s value

		    // pivot according to the rule that we want to choose the row
		    // with smallest s/r ratio see third case
		    // currently we ignore valuse of r==0 (case 1) and cases where the
		    // ratio is negative, i.e. either r or s are negative (case 2)
		    if (element == 0) {
			if (debug) {
			    System.out.println("Division by zero has occurred");
			    System.out.println("Within the linear program solver");
			    System.out.println("Ignoring the zero as a potential pivot");
			}
		    } else if ( (element < 0.0) || (endElement < 0.0) ){
			if (debug) {
			    System.out.println("Ignoring cases where element is negative");
			    System.out.println("The value of element is: " + element);
			    System.out.println("The value of end Element is: " + endElement);
			}
		    } else {
			ratio = endElement/element;  // should be s/r
			if (debug) {
			    System.out.println("The value of element is: " + element);
			    System.out.println("The value of endElement is: " + endElement);
			    System.out.println("The value of ratio is: " + ratio);
			    System.out.println("The value of prevRatio is: " + prevRatio);
			    System.out.println("Value of ratio <= prevRatio is :" + 
					       (ratio <= prevRatio));
			}
			if (ratio <= prevRatio) {
			    if (debug) {
				System.out.println("updating prevRatio with ratio");
			    }
			    prevRatio = ratio;
			    pivotRowIndex = i;
			}
		    }
		}
	
		// if the pivotRowIndex is still -1 then we know the pivotColumn
		// has no viable pivot points and the solution is unbounded or
		// infeasable (all pivot elements were either zero or negative or
		// the right most value was negative (the later shouldn't happen?)
		if (pivotRowIndex == -1) {
		    if (debug) {
			System.out.println("UNABLE TO FIND SOLUTION");
			System.out.println("The system is infeasable or unbounded");
		    }
		    return(Float.POSITIVE_INFINITY);
		}
	
		// we now have the pivot row and col all that remains is
		// to divide through by this value and subtract the appropriate
		// multiple of the pivot row from all other rows to obtain
		// a tableau which has a column of all zeros and one 1 in the
		// intersection of pivot row and col
	
		// divide through by the pivot value
		double pivotValue = problemTableau[pivotRowIndex][pivotColIndex];
	
		if (debug) {
		    System.out.println("The value of row index is: " + pivotRowIndex);
		    System.out.println("The value of col index is: " + pivotColIndex);
		    System.out.println("The value of pivotValue is: " + pivotValue);
		}
		// divide through by s on the pivot row to obtain a 1 in pivot col
		for (i = 0; i < numCol; i++) {
		    problemTableau[pivotRowIndex][i] =
			problemTableau[pivotRowIndex][i] / pivotValue;
		}
	
		// subtract appropriate multiple of pivot row from all other rows
		// to zero out all rows except the final row and the pivot row
		for (i = 0; i < numRow; i++) {
		    if (i != pivotRowIndex) {
			multiplier = problemTableau[i][pivotColIndex];
			for (int j=0; j < numCol; j++) {
			    problemTableau[i][j] = problemTableau[i][j] -
				multiplier * problemTableau[pivotRowIndex][j];
			}
		    }
		}
	    }
	    // case when the element is optimal
	}
	return(problemTableau[numRow - 1][numCol - 1]);
    }

    static boolean edgeIntersectSphere (BoundingSphere sphere, Point3d start, 
					Point3d end) { 

	double abLenSq, acLenSq, apLenSq, abDotAp, radiusSq;
	Vector3d ab = new Vector3d();
	Vector3d ap = new Vector3d();

	Point3d center = new Point3d();
	sphere.getCenter (center);
	double radius = sphere.getRadius ();
      
	ab.x = end.x - start.x;
	ab.y = end.y - start.y;
	ab.z = end.z - start.z;
      
	ap.x = center.x - start.x;
	ap.y = center.y - start.y;
	ap.z = center.z - start.z;
      
	abDotAp = ab.dot(ap);
      
	if (abDotAp < 0.0)
	    return false; // line segment points away from sphere.

	abLenSq = ab.lengthSquared();
	acLenSq = abDotAp * abDotAp / abLenSq;

	if (acLenSq < abLenSq)
	    return false; // C doesn't lies between end points of edge.

	radiusSq = radius * radius;
	apLenSq = ap.lengthSquared();
     
	if ((apLenSq - acLenSq) <= radiusSq)
	    return true;      

	return false;
    }


    static double det2D(Point2d a, Point2d b, Point2d p) {
	return (((p).x - (a).x) * ((a).y - (b).y) + 
		((a).y - (p).y) * ((a).x - (b).x));
    }

    // Assume coord is CCW.
    static boolean pointIntersectPolygon2D(Vector3d normal, Point3d[] coord, 
					   Point3d point) {

	double  absNrmX, absNrmY, absNrmZ;
	Point2d coord2D[] = new Point2d[coord.length];
	Point2d pnt = new Point2d();

	int i, j, axis;
      
	// Project 3d points onto 2d plane.
	// Note : Area of polygon is not preserve in this projection, but
	// it doesn't matter here. 
    
	// Find the axis of projection.
	absNrmX = Math.abs(normal.x);
	absNrmY = Math.abs(normal.y);
	absNrmZ = Math.abs(normal.z);
      
	if (absNrmX > absNrmY)
	    axis = 0;
	else 
	    axis = 1;
      
	if (axis == 0) {
	    if (absNrmX < absNrmZ)
		axis = 2;
	}    
	else if (axis == 1) {
	    if (absNrmY < absNrmZ)
		axis = 2;
	}    
    
	// System.out.println("Normal " + normal + " axis " + axis );
     	
	for (i=0; i<coord.length; i++) {
	    coord2D[i] = new Point2d();
	
	    switch (axis) {
	    case 0:
		coord2D[i].x = coord[i].y;
		coord2D[i].y = coord[i].z;
		break;
	
	    case 1:
		coord2D[i].x = coord[i].x;
		coord2D[i].y = coord[i].z;
		break;
	
	    case 2:
		coord2D[i].x = coord[i].x;
		coord2D[i].y = coord[i].y;
		break;      
	    } 
	    // System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
	}

	switch (axis) {
	case 0:
	    pnt.x = point.y;
	    pnt.y = point.z;
	    break;
	
	case 1:
	    pnt.x = point.x;
	    pnt.y = point.z;
	    break;
	
	case 2:
	    pnt.x = point.x;
	    pnt.y = point.y;
	    break;      
	}

	// Do determinant test.
	for (j=0; j<coord.length; j++) {
	    if (j<(coord.length-1))
		if (det2D(coord2D[j], coord2D[j+1], pnt)>0.0)
		    ;
		else
		    return false;
	    else
		if (det2D(coord2D[j], coord2D[0], pnt)>0.0)
		    ;
		else
		    return false;
	}
	return true;
    }


    static boolean edgeIntersectPlane(Vector3d normal, Point3d pnt, 
				      Point3d start, Point3d end, Point3d iPnt){
      
	Vector3d tempV3d = new Vector3d();
	Vector3d direction = new Vector3d();
	double pD, pNrmDotrDir, tr;
      
	// Compute plane D.
	tempV3d.set((Tuple3d) pnt);
	pD = normal.dot(tempV3d);
      
	direction.x = end.x - start.x;
	direction.y = end.y - start.y;
	direction.z = end.z - start.z;

	pNrmDotrDir = normal.dot(direction);
    
	// edge is parallel to plane. 
	if (pNrmDotrDir== 0.0) {
	    // System.out.println("Edge is parallel to plane.");
	    return false;        
	}

	tempV3d.set((Tuple3d) start);
      
	tr = (pD - normal.dot(tempV3d))/ pNrmDotrDir;
      
	// Edge intersects the plane behind the edge's start.
	// or exceed the edge's length.
	if ((tr < 0.0 ) || (tr > 1.0 )) {
	    // System.out.println("Edge intersects the plane behind the start or exceed end.");
	    return false;
	}

	iPnt.x = start.x + tr * direction.x;
	iPnt.y = start.y + tr * direction.y;
	iPnt.z = start.z + tr * direction.z;

	return true;
    }

    // Assume coord is CCW.
    static boolean edgeIntersectPolygon2D(Vector3d normal, Point3d[] coord, 
					  Point3d[] seg) {

	double  absNrmX, absNrmY, absNrmZ;
	Point2d coord2D[] = new Point2d[coord.length];
	Point2d seg2D[] = new Point2d[2];

	int i, j, axis;
      
	// Project 3d points onto 2d plane.
	// Note : Area of polygon is not preserve in this projection, but
	// it doesn't matter here. 
    
	// Find the axis of projection.
	absNrmX = Math.abs(normal.x);
	absNrmY = Math.abs(normal.y);
	absNrmZ = Math.abs(normal.z);
      
	if (absNrmX > absNrmY)
	    axis = 0;
	else 
	    axis = 1;
      
	if (axis == 0) {
	    if (absNrmX < absNrmZ)
		axis = 2;
	}    
	else if (axis == 1) {
	    if (absNrmY < absNrmZ)
		axis = 2;
	}    
    
	// System.out.println("Normal " + normal + " axis " + axis );
     	
	for (i=0; i<coord.length; i++) {
	    coord2D[i] = new Point2d();
	
	    switch (axis) {
	    case 0:
		coord2D[i].x = coord[i].y;
		coord2D[i].y = coord[i].z;
		break;
	
	    case 1:
		coord2D[i].x = coord[i].x;
		coord2D[i].y = coord[i].z;
		break;
	
	    case 2:
		coord2D[i].x = coord[i].x;
		coord2D[i].y = coord[i].y;
		break;      
	    } 
	    // System.out.println("i " + i + " u " + uCoor[i] + " v " + vCoor[i]); 
	}

	for (i=0; i<2; i++) {
	    seg2D[i] = new Point2d();
	    switch (axis) {
	    case 0:
		seg2D[i].x = seg[i].y;
		seg2D[i].y = seg[i].z;
		break;
	
	    case 1:
		seg2D[i].x = seg[i].x;
		seg2D[i].y = seg[i].z;
		break;
	
	    case 2:
		seg2D[i].x = seg[i].x;
		seg2D[i].y = seg[i].y;
		break;      
	    } 
	    // System.out.println("i " + i + " u " + uSeg[i] + " v " + vSeg[i]); 
	}

	// Do determinant test.
	boolean pntTest[][] = new boolean[2][coord.length];
	boolean testFlag;

	for (j=0; j<coord.length; j++) {
	    for (i=0; i<2; i++) {
		if (j<(coord.length-1))
		    pntTest[i][j] = (det2D(coord2D[j], coord2D[j+1], seg2D[i])<0.0);
		else
		    pntTest[i][j] = (det2D(coord2D[j], coord2D[0], seg2D[i])<0.0);
	    }

	    if ((pntTest[0][j]==false) && (pntTest[1][j]==false))
		return false;
	}
      
	testFlag = true;
	for (i=0; i<coord.length; i++) {
	    if (pntTest[0][i]==false) {
		testFlag = false;
		break;
	    }
	}
      
	if (testFlag == true)
	    return true; // start point is inside polygon.

	testFlag = true;
	for (i=0; i<coord.length; i++) {
	    if (pntTest[1][i]==false) {
		testFlag = false;
		break;
	    }
	}
      
	if (testFlag == true)
	    return true; // end point is inside polygon.
      

	int cnt = 0;
	for (i=0; i<coord.length; i++) {
	    if (det2D(seg2D[0], seg2D[1], coord2D[i])<0.0)
		cnt++;
	}

	if ((cnt==0)||(cnt==coord.length))
	    return false;

	return true;
    }

    static boolean intersectPolygon(Point3d coord1[], Point3d coord2[], 
				    boolean doTrivialTest) {
	int i, j;
	Vector3d vec0 = new Vector3d(); //Edge vector from point 0 to point 1;
	Vector3d vec1 = new Vector3d(); //Edge vector from point 0 to point 2 or 3;
	Vector3d pNrm = new Vector3d();
	boolean epFlag;

	// compute plane normal for coord1.
	for (i=0; i<coord1.length-1;) {
	    vec0.x = coord1[i+1].x - coord1[i].x;
	    vec0.y = coord1[i+1].y - coord1[i].y;
	    vec0.z = coord1[i+1].z - coord1[i++].z;
	    if (vec0.length() > 0.0)
		break;
	}
        
	for (j=i; j<coord1.length-1; j++) {
	    vec1.x = coord1[j+1].x - coord1[j].x;
	    vec1.y = coord1[j+1].y - coord1[j].y;
	    vec1.z = coord1[j+1].z - coord1[j].z;
	    if (vec1.length() > 0.0)
		break;
	}
      
	if (j == (coord1.length-1)) {
	    // System.out.println("(1) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}

	/*
	  for (i=0; i<coord1.length; i++) 
	  System.out.println("coord1 P" + i + " " + coord1[i]);
	  for (i=0; i<coord2.length; i++) 
	  System.out.println("coord2 P" + i + " " + coord2[i]);
	  */
      
	pNrm.cross(vec0,vec1);
      
	if (pNrm.length() == 0.0) {
	    // System.out.println("(2) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}
      
	// Do trivial test here.
	if ( doTrivialTest == true) {
	    // Not implemented yet.
	}

	j = 0;      
	Point3d seg[] = new Point3d[2];
	seg[0] = new Point3d();
	seg[1] = new Point3d();

	for (i=0; i<coord2.length; i++) {
	    if (i < (coord2.length-1))
		epFlag = edgeIntersectPlane(pNrm, coord1[0], coord2[i], 
					    coord2[i+1], seg[j]);
	    else
		epFlag = edgeIntersectPlane(pNrm, coord1[0], coord2[i], 
					    coord2[0], seg[j]);
	    if (epFlag == true) {
		j++;
		if (j>1)
		    break;
	    }
	}

	if (j==0)
	    return false;
      
	if (coord2.length < 3)
	    return pointIntersectPolygon2D(pNrm, coord1, seg[0]);

	return edgeIntersectPolygon2D(pNrm, coord1, seg);
    }


    static final boolean isNonZero(double v) {
	return ((v > EPS) || (v < -EPS));
	
    }


    static boolean intersectRay(Point3d coordinates[], 
				PickRay ray, PickIntersection pi) {
	Point3d origin = new Point3d(); 
	Vector3d direction = new Vector3d(); 
	boolean result;
	ray.get (origin, direction);
	result = intersectRayOrSegment(coordinates, direction, origin, pi, false);
	return result;
    }

    /**
     *  Return true if triangle or quad intersects with ray and the distance is 
     *  stored in pr.
     * */
    static boolean intersectRayOrSegment(Point3d coordinates[], 
					 Vector3d direction, Point3d origin,
					 PickIntersection pi, boolean isSegment) {
	Vector3d vec0, vec1, pNrm, tempV3d;
	Point3d iPnt;

	vec0 = new Vector3d();
	vec1 = new Vector3d();
	pNrm = new Vector3d();

	double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
	double pNrmDotrDir = 0.0; 

	boolean isIntersect = false;
	int i, j, k=0, l = 0;

	// Compute plane normal.
	for (i=0; i<coordinates.length; i++) {
	    if (i != coordinates.length-1) {
		l = i+1;
	    } else {
		l = 0;
	    }
	    vec0.x = coordinates[l].x - coordinates[i].x;
	    vec0.y = coordinates[l].y - coordinates[i].y;
	    vec0.z = coordinates[l].z - coordinates[i].z;
	    if (vec0.length() > 0.0) {
		break;
	    }
	}
		

	for (j=l; j<coordinates.length; j++) {
	    if (j != coordinates.length-1) {
		k = j+1;
	    } else {
		k = 0;
	    }
	    vec1.x = coordinates[k].x - coordinates[j].x;
	    vec1.y = coordinates[k].y - coordinates[j].y;
	    vec1.z = coordinates[k].z - coordinates[j].z;
	    if (vec1.length() > 0.0) {
		break;
	    }
	}		

	pNrm.cross(vec0,vec1);

	if ((vec1.length() == 0) || (pNrm.length() == 0)) {
	    // degenerated to line if vec0.length() == 0
	    // or vec0.length > 0 and vec0 parallel to vec1
	    k = (l == 0 ? coordinates.length-1: l-1);
	    isIntersect = intersectLineAndRay(coordinates[l],
					      coordinates[k],
					      origin,
					      direction, 
					      pi);
	    return isIntersect;
	}

	// It is possible that Quad is degenerate to Triangle 
	// at this point

	pNrmDotrDir = pNrm.dot(direction);

    	// Ray is parallel to plane. 
	if (pNrmDotrDir == 0.0) {
	    // Ray is parallel to plane
	    // Check line/triangle intersection on plane.
	    for (i=0; i < coordinates.length ;i++) {
		if (i != coordinates.length-1) {
		    k = i+1;
		} else {
		    k = 0;
		}
		if (intersectLineAndRay(coordinates[i],
					coordinates[k],
					origin,
					direction, 
					pi)) {
		    isIntersect = true;
		    break;
		}
	    }
	    return isIntersect;
	}

	// Plane equation: (p - p0)*pNrm = 0 or p*pNrm = pD;
	tempV3d = new Vector3d();
	tempV3d.set((Tuple3d) coordinates[0]);
	pD = pNrm.dot(tempV3d);
	tempV3d.set((Tuple3d) origin);

	// Substitute Ray equation:
	// p = origin + pi.distance*direction
	// into the above Plane equation 

	double dist = (pD - pNrm.dot(tempV3d))/ pNrmDotrDir;

	// Ray intersects the plane behind the ray's origin.
	if ((dist < -EPS ) ||
	    (isSegment && (dist > 1.0+EPS))) {
	    // Ray intersects the plane behind the ray's origin
	    // or intersect point not fall in Segment 
	    return false;
	}

	// Now, one thing for sure the ray intersect the plane.
	// Find the intersection point.
	iPnt = new Point3d();
	iPnt.x = origin.x + direction.x * dist;
	iPnt.y = origin.y + direction.y * dist;
	iPnt.z = origin.z + direction.z * dist;

	// Project 3d points onto 2d plane.
	// Find the axis so that area of projection is maximize.
	absNrmX = Math.abs(pNrm.x);
	absNrmY = Math.abs(pNrm.y);
	absNrmZ = Math.abs(pNrm.z);

	// Check out
	// http://astronomy.swin.edu.au/~pbourke/geometry/insidepoly/ 
	// Solution 3:
	// All sign of (y - y0) (x1 - x0) - (x - x0) (y1 - y0)
	// must agree. 
	double sign, t, lastSign = 0;
	Point3d p0 = coordinates[coordinates.length-1];
	Point3d p1 = coordinates[0];

	isIntersect = true;

	if (absNrmX > absNrmY) {
	    if (absNrmX < absNrmZ) {
		for (i=0; i < coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
 		    sign = (iPnt.y - p0.y)*(p1.x - p0.x) - 
			   (iPnt.x - p0.x)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;
			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.x - p0.x;
			    if (isNonZero(t)) {
				t = (iPnt.x - p0.x)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		} 
	    } else {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.y - p0.y)*(p1.z - p0.z) - 
			   (iPnt.z - p0.z)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;

			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;

			} else {
			    t = p1.z - p0.z;
			    if (isNonZero(t)) {
				t = (iPnt.z - p0.z)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		} 
	    }
	} else {
	    if (absNrmY < absNrmZ) {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.y - p0.y)*(p1.x - p0.x) - 
			   (iPnt.x - p0.x)*(p1.y - p0.y);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.y - p0.y;
			if (isNonZero(t)) {
			    t = (iPnt.y - p0.y)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.x - p0.x;
			    if (isNonZero(t)) {
				t = (iPnt.x - p0.x)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		}
	    } else {
		for (i=0; i<coordinates.length; i++) {
		    p0 = coordinates[i];
		    p1 = (i != coordinates.length-1 ? coordinates[i+1]: coordinates[0]);
		    sign = (iPnt.x - p0.x)*(p1.z - p0.z) - 
			   (iPnt.z - p0.z)*(p1.x - p0.x);
		    if (isNonZero(sign)) {
			if (sign*lastSign < 0) {
			    isIntersect = false;
			    break;
			}
			lastSign = sign;
		    } else { // point on line, check inside interval
			t = p1.x - p0.x;
			if (isNonZero(t)) {
			    t = (iPnt.x - p0.x)/t;
			    isIntersect = ((t > -EPS) && (t < 1+EPS));
			    break;
			} else {
			    t = p1.z - p0.z;
			    if (isNonZero(t)) {
				t = (iPnt.z - p0.z)/t;
				isIntersect = ((t > -EPS) && (t < 1+EPS));
				break;
			    } else {
				//degenerate line=>point
			    }
			}
		    }
		}
	    }
	}

	if (isIntersect) {
	    pi.setDistance(dist*direction.length());
	    pi.setPointCoordinatesVW(iPnt);
	} 
	return isIntersect;
    }


    /**
      Return true if triangle or quad intersects with segment and the distance is
      stored in dist.
      */
    static boolean intersectSegment (Point3d coordinates[], PickSegment segment,
				     PickIntersection pi) {
    	Point3d start = new Point3d();
	Point3d end = new Point3d();
	Vector3d direction = new Vector3d();
	boolean result;
	segment.get(start, end);
	direction.x = end.x - start.x;
	direction.y = end.y - start.y;
	direction.z = end.z - start.z;
	result = intersectRayOrSegment(coordinates, direction, start, pi, true);
	return result;
    }

    
    /**
      Return true if point is on the inside of halfspace test. The halfspace is 
      partition by the plane of triangle or quad.
      */
     
    static boolean inside (Point3d coordinates[], PickPoint point, int ccw) {
    
	Vector3d vec0 = new Vector3d(); //Edge vector from point 0 to point 1;
	Vector3d vec1 = new Vector3d(); //Edge vector from point 0 to point 2 or 3;
	Vector3d pNrm = new Vector3d();
	double  absNrmX, absNrmY, absNrmZ, pD = 0.0;
	Vector3d tempV3d = new Vector3d();
	double pNrmDotrDir = 0.0; 
    
	double tempD;

	int i, j;

	Point3d location = new Point3d ();
	point.get (location);

	// Compute plane normal.
	for (i=0; i<coordinates.length-1;) {
	    vec0.x = coordinates[i+1].x - coordinates[i].x;
	    vec0.y = coordinates[i+1].y - coordinates[i].y;
	    vec0.z = coordinates[i+1].z - coordinates[i++].z;
	    if (vec0.length() > 0.0)
		break;
	}
        
	for (j=i; j<coordinates.length-1; j++) {
	    vec1.x = coordinates[j+1].x - coordinates[j].x;
	    vec1.y = coordinates[j+1].y - coordinates[j].y;
	    vec1.z = coordinates[j+1].z - coordinates[j].z;
	    if (vec1.length() > 0.0)
		break;
	}
    
	if (j == (coordinates.length-1)) {
	    // System.out.println("(1) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}

	/* 
	   System.out.println("Ray orgin : " + origin + " dir " + direction);
	   System.out.println("Triangle/Quad :");
	   for (i=0; i<coordinates.length; i++) 
	   System.out.println("P" + i + " " + coordinates[i]);
	   */

	if ( ccw == 0x1)
	    pNrm.cross(vec0,vec1);
	else
	    pNrm.cross(vec1,vec0);
    
	if (pNrm.length() == 0.0) {
	    // System.out.println("(2) Degenerated polygon.");
	    return false;  // Degenerated polygon.
	}
	// Compute plane D.
	tempV3d.set((Tuple3d) coordinates[0]);
	pD = pNrm.dot(tempV3d);

	tempV3d.set((Tuple3d) location);

	if ((pD - pNrm.dot(tempV3d)) > 0.0 ) {
	    // System.out.println("point is on the outside of plane.");
	    return false;
	}
	else 
	    return true;
    }

    static boolean intersectPntAndPnt (Point3d pnt1, Point3d pnt2, 
				       PickIntersection pi) {
  
	if ((pnt1.x == pnt2.x) && (pnt1.y == pnt2.y) && (pnt1.z == pnt2.z)) {
	    pi.setPointCoordinatesVW (pnt1);
	    pi.setDistance (0.0);
	    return true;
	}
	else 
	    return false;
    }

    static boolean intersectPntAndRay (Point3d pnt, Point3d ori, Vector3d dir, 
				       PickIntersection pi) {
	int flag = 0;
	double temp;
	double dist;

	if (dir.x != 0.0) {
	    flag = 0;
	    dist = (pnt.x - ori.x)/dir.x;
	}
	else if (dir.y != 0.0) {
	    if (pnt.x != ori.x)
		return false;
	    flag = 1;
	    dist = (pnt.y - ori.y)/dir.y;
	}
	else if (dir.z != 0.0) {
	    if ((pnt.x != ori.x)||(pnt.y != ori.y))
		return false;
	    flag = 2;
	    dist = (pnt.z - ori.z)/dir.z;
           
	}
	else
	    return false;

	if (dist < 0.0)
	    return false;

	if (flag == 0) {
	    temp = ori.y + dist * dir.y;
	    if ((pnt.y < (temp - Double.MIN_VALUE)) || (pnt.y > (temp + Double.MIN_VALUE)))
		return false;    
	}
    
	if (flag < 2) {
	    temp = ori.z + dist * dir.z;
	    if ((pnt.z < (temp - Double.MIN_VALUE)) || (pnt.z > (temp + Double.MIN_VALUE)))
		return false;
	}

	pi.setPointCoordinatesVW (pnt);
	pi.setDistance (dist);

	return true;
    
    }

    static boolean intersectLineAndRay(Point3d start, Point3d end, 
				       Point3d ori, Vector3d dir, 
				       PickIntersection pi) {
    
	double m00, m01, m10, m11;
	double mInv00, mInv01, mInv10, mInv11;
	double dmt, t, s, tmp1, tmp2;
	Vector3d lDir;
	double dist;

	//     System.out.println("Intersect : intersectLineAndRay");
	//     System.out.println("start " + start + " end " + end );
	//     System.out.println("ori " + ori + " dir " + dir);
    
	lDir = new Vector3d(end.x - start.x, end.y - start.y,
			    end.z - start.z);
    
	m00 = lDir.x;
	m01 = -dir.x;
	m10 = lDir.y;
	m11 = -dir.y;

	// Get the determinant.
	dmt = (m00 * m11) - (m10 * m01);

	if (dmt==0.0) { // No solution, hence no intersect.
	    // System.out.println("dmt is zero");
	    boolean isIntersect = false;
	    if ((lDir.x == 0) && (lDir.y == 0) && (lDir.z == 0)) {
		isIntersect = intersectPntAndRay(start, ori, dir, pi);
		if (isIntersect) {
		    pi.setPointCoordinatesVW(start);
		    pi.setDistance(0);
		}
	    }
	    return isIntersect;
	}
	// Find the inverse.
	tmp1 = 1/dmt;

	mInv00 = tmp1 * m11;
	mInv01 = tmp1 * (-m01);
	mInv10 = tmp1 * (-m10);
	mInv11 = tmp1 * m00;

	tmp1 = ori.x - start.x;
	tmp2 = ori.y - start.y;

	t = mInv00 * tmp1 + mInv01 * tmp2;
	s = mInv10 * tmp1 + mInv11 * tmp2;
    
	if (s<0.0) { // Before the origin of ray.
	    // System.out.println("Before the origin of ray " + s);
	    return false;
	}
	if ((t<0)||(t>1.0)) {// Before or after the end points of line.
	    // System.out.println("Before or after the end points of line. " + t);
	    return false;
	}

	tmp1 = ori.z + s * dir.z;
	tmp2 = start.z + t * lDir.z;
  
	if ((tmp1 < (tmp2 - Double.MIN_VALUE)) || 
	    (tmp1 > (tmp2 + Double.MIN_VALUE))) {
	    // System.out.println("No intersection : tmp1 " + tmp1 + " tmp2 " + tmp2);
	    return false;
	}
	dist = s;

	pi.setDistance (dist);
	Point3d iPnt = new Point3d ();
	iPnt.scaleAdd (s, dir, ori);
	pi.setPointCoordinatesVW (iPnt);
    
	// System.out.println("Intersected : tmp1 " + tmp1 + " tmp2 " + tmp2);
	return true;
    }

    /**
      Return true if triangle or quad intersects with cylinder and the 
      distance is stored in pr.
      */
    static boolean intersectCylinder (Point3d coordinates[], 
				      PickCylinder cyl, PickIntersection pi) {
    
	Point3d origin = new Point3d();
	Point3d end = new Point3d();
	Vector3d direction = new Vector3d();
	Point3d iPnt1 = new Point3d();
	Point3d iPnt2 = new Point3d();
	Vector3d originToIpnt = new Vector3d();

	// Get cylinder information
	cyl.getOrigin (origin);
	cyl.getDirection (direction);
	double radius = cyl.getRadius ();

	if (cyl instanceof PickCylinderSegment) {
	    ((PickCylinderSegment)cyl).getEnd (end);
	}

	// If the ray intersects, we're good (do not do this if we only have
	// a segment
	if (coordinates.length > 2) {
	    if (cyl instanceof PickCylinderRay) {
		if (intersectRay (coordinates, new PickRay (origin, direction), pi)) {
		    return true;
		}
	    }
	    else {
		if (intersectSegment (coordinates, new PickSegment (origin, end), pi)) {
		    return true;
		}
	    }
	}

	// Ray doesn't intersect, check distance to edges
	double sqDistToEdge;
	for (int i=0; i<coordinates.length-1;i++) {
	    if (cyl instanceof PickCylinderSegment) {
		sqDistToEdge = 
		    Distance.segmentToSegment (origin, end, 
					       coordinates[i], coordinates[i+1],
					       iPnt1, iPnt2, null);
	    }
	    else {
		sqDistToEdge = 
		    Distance.rayToSegment (origin, direction, 
					   coordinates[i], coordinates[i+1],
					   iPnt1, iPnt2, null);
	    }
	    if (sqDistToEdge <= radius*radius) {
		pi.setPointCoordinatesVW (iPnt2);
		originToIpnt.sub (iPnt1, origin);
		pi.setDistance (originToIpnt.length());
		return true;
	    }
	}
	return false;
    }

    /**
      Return true if triangle or quad intersects with cone. The 
      distance is stored in pr.
      */
    static boolean intersectCone (Point3d coordinates[], 
				  PickCone cone, PickIntersection pi) {

	Point3d origin = new Point3d();
	Point3d end = new Point3d();
	Vector3d direction = new Vector3d();
	Vector3d originToIpnt = new Vector3d();
	double distance;
    
	Point3d iPnt1 = new Point3d();
	Point3d iPnt2 = new Point3d();
	Vector3d vector = new Vector3d();

	// Get cone information
	cone.getOrigin (origin);
	cone.getDirection (direction);
	double radius;

	if (cone instanceof PickConeSegment) {
	    ((PickConeSegment)cone).getEnd (end);
	}

	// If the ray intersects, we're good (do not do this if we only have
	// a segment
	if (coordinates.length > 2) {
	    if (cone instanceof PickConeRay) {
		if (intersectRay (coordinates, new PickRay (origin, direction), pi)) {
		    return true;
		}
	    }
	    else {
		if (intersectSegment (coordinates, new PickSegment (origin, end), 
				      pi)) {
		    return true;
		}
	    }
	}

	// Ray doesn't intersect, check distance to edges
	double sqDistToEdge;
	for (int i=0; i<coordinates.length-1;i++) {
	    if (cone instanceof PickConeSegment) {
		sqDistToEdge = 
		    Distance.segmentToSegment (origin, end, 
					       coordinates[i], coordinates[i+1],
					       iPnt1, iPnt2, null);
	    }
	    else {
		sqDistToEdge = 
		    Distance.rayToSegment (origin, direction, 
					   coordinates[i], coordinates[i+1],
					   iPnt1, iPnt2, null);
	    }
	    originToIpnt.sub (iPnt1, origin);      
	    distance = originToIpnt.length();
	    radius = Math.tan (cone.getSpreadAngle()) * distance;
	    if (sqDistToEdge <= radius*radius) {
		//	System.out.println ("intersectCone: edge "+i+" intersected");
		pi.setPointCoordinatesVW (iPnt2);
		pi.setDistance (distance);
		return true;
	    }
	}
	return false;
    }


    /**
      Return true if point intersects with cylinder and the 
      distance is stored in pi.
      */
    static boolean intersectCylinder (Point3d pt, 
				      PickCylinder cyl, PickIntersection pi) {

	Point3d origin = new Point3d();
	Point3d end = new Point3d();
	Vector3d direction = new Vector3d();
	Point3d iPnt = new Point3d();
	Vector3d originToIpnt = new Vector3d();

	// Get cylinder information
	cyl.getOrigin (origin);
	cyl.getDirection (direction);
	double radius = cyl.getRadius ();
	double sqDist;

	if (cyl instanceof PickCylinderSegment) {
	    ((PickCylinderSegment)cyl).getEnd (end);
	    sqDist = Distance.pointToSegment (pt, origin, end, iPnt, null);
	}
	else {
	    sqDist = Distance.pointToRay (pt, origin, direction, iPnt, null);
	}
	if (sqDist <= radius*radius) {
	    pi.setPointCoordinatesVW (pt);
	    originToIpnt.sub (iPnt, origin);
	    pi.setDistance (originToIpnt.length());
	    return true;
	}
	return false;
    }
    /**
      Return true if point intersects with cone and the 
      distance is stored in pi.
      */
    static boolean intersectCone (Point3d pt, 
				  PickCone cone, PickIntersection pi) {

	//    System.out.println ("Intersect.intersectCone point");

	Point3d origin = new Point3d();
	Point3d end = new Point3d();
	Vector3d direction = new Vector3d();
	Point3d iPnt = new Point3d();// the closest point on the cone vector
	Vector3d originToIpnt = new Vector3d();

	// Get cone information
	cone.getOrigin (origin);
	cone.getDirection (direction);
	double radius;
	double distance;
	double sqDist;

	if (cone instanceof PickConeSegment) {
	    ((PickConeSegment)cone).getEnd (end);
	    sqDist = Distance.pointToSegment (pt, origin, end, iPnt, null);
	}
	else {
	    sqDist = Distance.pointToRay (pt, origin, direction, iPnt, null);
	}
	originToIpnt.sub (iPnt, origin);
	distance = originToIpnt.length();
	radius = Math.tan (cone.getSpreadAngle()) * distance;
	if (sqDist <= radius*radius) {
	    pi.setPointCoordinatesVW (pt);
	    pi.setDistance (distance);
	    return true;
	}
	return false;
    }

} // PickResult
