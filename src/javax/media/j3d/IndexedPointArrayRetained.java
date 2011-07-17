/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.*;
import java.lang.Math;

/**
 * The IndexedPointArray object draws the array of vertices as individual points.
 */

class IndexedPointArrayRetained extends IndexedGeometryArrayRetained {

    IndexedPointArrayRetained() {
        this.geoType = GEO_TYPE_INDEXED_POINT_SET;
    } 

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
	Point3d pnt = new Point3d();
        int[] vtxIndexArr = new int[1];

        int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
    
	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < validVertexCount) {
                vtxIndexArr[0] = indexCoord[i];
		getVertexData(indexCoord[i++], pnt);
		if (intersectPntAndRay(pnt, pickRay.origin,
				       pickRay.direction, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
	    Vector3d dir = 
		new Vector3d(pickSegment.end.x - pickSegment.start.x, 
			     pickSegment.end.y - pickSegment.start.y,
			     pickSegment.end.z - pickSegment.start.z);
	    
	    while (i < validVertexCount) {
                vtxIndexArr[0] = indexCoord[i];
		getVertexData(indexCoord[i++], pnt);
		if (intersectPntAndRay(pnt, pickSegment.start, 
					dir, sdist) &&
		    (sdist[0] <= 1.0)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
	case PickShape.PICKBOUNDINGSPHERE:
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    Bounds bounds = ((PickBounds) pickShape).bounds;

	    while (i < validVertexCount) {
                vtxIndexArr[0] = indexCoord[i];
		getVertexData(indexCoord[i++], pnt);
		if (bounds.intersect(pnt)) {
		    if (flags == 0) {
			return true;
		    }
		    sdist[0] = pickShape.distance(pnt);
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
                vtxIndexArr[0] = indexCoord[i];
		getVertexData(indexCoord[i++], pnt);
		if (intersectCylinder(pnt, pickCylinder, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
                vtxIndexArr[0] = indexCoord[i];
		getVertexData(indexCoord[i++], pnt);
		if (intersectCone(pnt, pickCone, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedPointArrayRetained0"));
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
	Point3d point = new Point3d();
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	
	switch (pnts.length) {
	case 3: // Triangle
	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], point);
		if (intersectTriPnt(pnts[0], pnts[1], pnts[2], point)) { 
		    return true;
		}
	    }
	    break;
	case 4: // Quad
	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], point);
		if (intersectTriPnt(pnts[0], pnts[1], pnts[2], point) || 
		    intersectTriPnt(pnts[0], pnts[2], pnts[3], point)) { 
		    return true;
		}
	    }
	    break;
	case 2: // Line
	    double dist[] = new double[1];
	    Vector3d dir = new Vector3d();

	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], point);
		dir.x = pnts[1].x - pnts[0].x;
		dir.y = pnts[1].y - pnts[0].y;
		dir.z = pnts[1].z - pnts[0].z;
		if (intersectPntAndRay(point, pnts[0], dir, dist) &&
		    (dist[0] <= 1.0)) {
		    return true;
		}
	    }
	    break;
	case 1: // Point
	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], point);
		if ((pnts[0].x == point.x) && 
		    (pnts[0].y == point.y) && 
		    (pnts[0].z == point.z)) {
		    return true;
		}
	    }
	    break;
	}
	return false;
    }

    boolean intersect(Transform3D thisToOtherVworld,
		      GeometryRetained geom) {
	Point3d[] pnt = new Point3d[1];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnt[0] = new Point3d();

	while (i < validVertexCount) {
	    getVertexData(indexCoord[i++], pnt[0]);
	    thisToOtherVworld.transform(pnt[0]);
	    if (geom.intersect(pnt)) {
		return true;
	    }
	}
	return false;
	
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	Point3d pnt = new Point3d();
	
	while (i < validVertexCount) {
	    getVertexData(indexCoord[i++], pnt);
	    if (targetBound.intersect(pnt)) {
		return true;
	    }
	}
	return false;
    }	

    int getClassType() { 
	return POINT_TYPE; 
    }
}

