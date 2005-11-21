/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
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
 * The IndexedTriangleFanArray object draws an array of vertices as a set of
 * connected triangle fans.  An array of per-strip
 * vertex counts specifies where the separate strips (fans) appear
 * in the vertex array.  For every strip in the set,
 * each vertex, beginning with the third vertex in the array,
 * defines a triangle to be drawn using the current vertex,
 * the previous vertex and the first vertex.  This can be thought of
 * as a collection of convex polygons.
 */

class IndexedTriangleFanArrayRetained extends IndexedGeometryStripArrayRetained {
  
    IndexedTriangleFanArrayRetained(){
	geoType = GEO_TYPE_INDEXED_TRI_FAN_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Point3d pnts[] = new Point3d[3];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
	int i = 0;
	int j, scount, count = 0;
        pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();    
        int[] vtxIndexArr = new int[3];

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;
	
            while (i < stripIndexCounts.length) {
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[2]);
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKSEGMENT:
	    PickSegment pickSegment = (PickSegment) pickShape;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox bbox = (BoundingBox) 
		((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) 
		                     ((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectBoundingSphere(pnts, bsphere, sdist,
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) 
		((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectBoundingPolytope(pnts, bpolytope,
						  sdist, iPnt)) {
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKCYLINDER:
	    PickCylinder pickCylinder= (PickCylinder) pickShape;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectCylinder(pnts, pickCylinder, sdist, iPnt)) {
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;

	    while (i < stripIndexCounts.length) {  
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = indexCoord[count];
                    getVertexData(indexCoord[count++], pnts[k]);
                }
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
                    vtxIndexArr[2] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[2]);
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
		    pnts[1].set(pnts[2]);
                    vtxIndexArr[1] = vtxIndexArr[2];
		}
	    }
	    break;
	case PickShape.PICKPOINT:
	    // Should not happen since API already check for this
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedTriangleFanArrayRetained0"));
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
 
    // intersect pnts[] with every triangle in this object
    boolean intersect(Point3d[] pnts) {
	int j, end;
	Point3d[] points = new Point3d[3];
	double dist[] = new double[1];
	int i = 0, scount, count = 0;

	points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();

	switch (pnts.length) {
	case 3: // Triangle
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		getVertexData(indexCoord[count++], points[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[2]);
		    if (intersectTriTri(points[0], points[1], points[2],
					pnts[0], pnts[1], pnts[2])) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	case 4: // Quad
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		getVertexData(indexCoord[count++], points[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[2]);
		    if (intersectTriTri(points[0], points[1], points[2],
					pnts[0], pnts[1], pnts[2]) ||
			intersectTriTri(points[0], points[1], points[2],
					pnts[0], pnts[2], pnts[3])) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	case 2: // Line
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		getVertexData(indexCoord[count++], points[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[2]);
		    if (intersectSegment(points, pnts[0], pnts[1],
					 dist, null)) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	case 1: // Point
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		getVertexData(indexCoord[count++], points[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[2]);
		    if (intersectTriPnt(points[0], points[1], points[2],
					pnts[0])) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	}
	return false;
    }
    
    boolean intersect(Transform3D thisToOtherVworld, GeometryRetained geom) {
	int i = 0, j, scount, count = 0;
	Point3d[] pnts = new Point3d[3];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();

	while (i < stripIndexCounts.length) {
	    getVertexData(indexCoord[count++], pnts[0]);
	    getVertexData(indexCoord[count++], pnts[1]);
	    thisToOtherVworld.transform(pnts[0]);
	    thisToOtherVworld.transform(pnts[1]);
	    scount = stripIndexCounts[i++];
	    for (j=2; j < scount; j++) {
		getVertexData(indexCoord[count++], pnts[2]);
		thisToOtherVworld.transform(pnts[2]);
		if (geom.intersect(pnts)) {
		    return true;
		}
		pnts[1].set(pnts[2]);
	    }
	}
	return false;
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
	int i = 0;
	int j, scount, count = 0;
	Point3d[] pnts = new Point3d[3];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		getVertexData(indexCoord[count++], pnts[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectBoundingBox(pnts, box, null, null)) {
			return true;
		    }
		    pnts[1].set(pnts[2]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;
	    
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		getVertexData(indexCoord[count++], pnts[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectBoundingSphere(pnts, bsphere, null, null)) {
			return true;
		    }
		    pnts[1].set(pnts[2]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;

	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		getVertexData(indexCoord[count++], pnts[1]);
		scount = stripIndexCounts[i++];
		for (j=2; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[2]);
		    if (intersectBoundingPolytope(pnts, bpolytope, null, null)) {
			return true;
		    }
		    pnts[1].set(pnts[2]);
		}
	    }
	    break;
	default:
	    throw new RuntimeException("Bounds not supported for intersection "
				       + targetBound); 
	}
	return false;
    }

    int getClassType() {
	return TRIANGLE_TYPE;
    }
}
