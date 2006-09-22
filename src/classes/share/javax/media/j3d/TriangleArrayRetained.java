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

import javax.vecmath.*;
import java.lang.Math;

/**
 * The TriangleArray object draws the array of vertices as individual
 * triangles.  Each group
 * of three vertices defines a triangle to be drawn.
 */

class TriangleArrayRetained extends GeometryArrayRetained {

    TriangleArrayRetained() {
	this.geoType = GEO_TYPE_TRI_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
 	Point3d pnts[] = new Point3d[3];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
        int[] vtxIndexArr = new int[3];
        
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();
    
	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectRay(pnts, pickRay, sdist, iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];                        
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);
		    }
		}
	    }
	    break;
	case PickShape.PICKSEGMENT:
	    PickSegment pickSegment = (PickSegment) pickShape;

            while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectSegment(pnts, pickSegment.start,
				     pickSegment.end, sdist, iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox bbox = (BoundingBox) 
		((PickBounds) pickShape).bounds;
	    
	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectBoundingBox(pnts, bbox, sdist, iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) 
		((PickBounds) pickShape).bounds;
	    
	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectBoundingSphere(pnts, bsphere, sdist, iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) 
		((PickBounds) pickShape).bounds;
	    
	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectBoundingPolytope(pnts, bpolytope,
					      sdist,iPnt)) { 
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKCYLINDER:
	    PickCylinder pickCylinder= (PickCylinder) pickShape;
	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectCylinder(pnts, pickCylinder, sdist,
				      iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;
	    
	    while (i < validVertexCount) {
                for(int j=0; j<3; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectCone(pnts, pickCone, sdist, iPnt)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = iPnt.x;
			y = iPnt.y;
			z = iPnt.z;
                	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
                            storeInterestData(pickInfo, flags, geom, geomIndex, 
                                              vtxIndexArr, iPnt, sdist[0]);
                        }
		    }
                    if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
                        storeInterestData(pickInfo, flags, geom, geomIndex, 
                                          vtxIndexArr, iPnt, sdist[0]);                      
                    }
		}
	    }
	    break;
	case PickShape.PICKPOINT:
	    // Should not happen since API already check for this
	    throw new IllegalArgumentException(J3dI18N.getString("TriangleArrayRetained0"));
	default:
	    throw new RuntimeException ("PickShape not supported for intersection"); 
	} 

    
	if (minDist < Double.MAX_VALUE) {
	    iPnt.x = x;
	    iPnt.y = y;
	    iPnt.z = z;
	    return true;
	}
	return false;
    }
  
  
    boolean intersect(Point3d[] pnts) {
	Point3d[] points = new Point3d[3];
	double dist[] = new double[1];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	
	points[0] = new Point3d();
	points[1] = new Point3d();	
	points[2] = new Point3d();	

	switch (pnts.length) {
	case 3: // Triangle
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		if (intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[1], pnts[2])) {
		    return true;
		}
	    }
	    break;
	case 4: // Quad
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		if (intersectTriTri(points[0], points[1], points[2],
				   pnts[0], pnts[1], pnts[2]) ||
		    intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[2], pnts[3])) {
		    return true;
		}
	    }
	    break;
	case 2: // Line
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		if (intersectSegment(points, pnts[0], pnts[1], dist,
				     null)) {
		    return true;
		}
	    }
	    break;
	case 1: // Point
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		if (intersectTriPnt(points[0], points[1], points[2],
				    pnts[0])) {
		    return true;
		}
	    }
	    break;
	}
	return false;
    }    
  
    boolean intersect(Transform3D thisToOtherVworld,
		      GeometryRetained geom) {
    
	Point3d[] pnts = new Point3d[3];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();

	while (i < validVertexCount) {
	    getVertexData(i++, pnts[0]);
	    getVertexData(i++, pnts[1]);
	    getVertexData(i++, pnts[2]);
	    thisToOtherVworld.transform(pnts[0]);
	    thisToOtherVworld.transform(pnts[1]);
	    thisToOtherVworld.transform(pnts[2]);
	    if (geom.intersect(pnts)) {
		return true;
	    }
	}
	return false;
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
	Point3d[] pnts = new Point3d[3];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;
	    
	    while (i < validVertexCount) {
		getVertexData(i++, pnts[0]);
		getVertexData(i++, pnts[1]);
		getVertexData(i++, pnts[2]);
		if (intersectBoundingBox(pnts, box, null, null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;
	    
	    while (i < validVertexCount) {
		getVertexData(i++, pnts[0]);
		getVertexData(i++, pnts[1]);
		getVertexData(i++, pnts[1]);
		if (intersectBoundingSphere(pnts, bsphere, null,
					    null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;
	    
	    while (i < validVertexCount) {
		getVertexData(i++, pnts[0]);
		getVertexData(i++, pnts[1]);
		getVertexData(i++, pnts[2]);
		if (intersectBoundingPolytope(pnts, bpolytope,
					      null, null)) {
		    return true;
		}
	    }
	    break;
	default:
	    throw new RuntimeException("Bounds not supported for intersection "
				       + targetBound); 
	}

	return false;
    }

    // From Graphics Gems IV (pg5) and Graphics Gems II, Pg170
    void computeCentroid() {
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	Point3d pnt0 = new Point3d();
	Point3d pnt1 = new Point3d();
	Point3d pnt2 = new Point3d();
	Vector3d vec = new Vector3d();
	Vector3d normal = new Vector3d();
	Vector3d tmpvec = new Vector3d();

	double area;
	double totalarea = 0;

	centroid.x = 0;
	centroid.y = 0;
	centroid.z = 0;


	while(i < validVertexCount) {
	    getVertexData(i++, pnt0); 
	    getVertexData(i++, pnt1); 
	    getVertexData(i++, pnt2);

	    // Determine the normal
	    vec.sub(pnt0, pnt1);
	    tmpvec.sub(pnt1, pnt2);

	    // Do the cross product
	    normal.cross(vec, tmpvec);
	    normal.normalize();

	    // If a degenerate triangle, don't include
	    if (Double.isNaN(normal.x + normal.y + normal.z))
		continue;

	    // compute the area
	    getCrossValue(pnt0, pnt1, tmpvec);
	    getCrossValue(pnt1, pnt2, tmpvec);
	    getCrossValue(pnt2, pnt0, tmpvec);
	    area = normal.dot(tmpvec);
	    centroid.x += (pnt0.x + pnt1.x + pnt2.x)* area;
	    centroid.y += (pnt0.y + pnt1.y + pnt2.y)* area;
	    centroid.z += (pnt0.z + pnt1.z + pnt2.z)* area;
	    totalarea += area;

	}
	if (totalarea != 0.0) {
	    area = 1.0/(3.0 * totalarea);
	    centroid.x *= area;
	    centroid.y *= area;
	    centroid.z *= area;
	}
    }

    int getClassType() {
	return TRIANGLE_TYPE;
    }
}
