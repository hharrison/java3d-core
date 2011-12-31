/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Point3d;

/**
 * The IndexedQuadArray object draws the array of vertices as individual
 * quadrilaterals.  Each group
 * of four vertices defines a quadrilateral to be drawn.
 */

class IndexedQuadArrayRetained extends IndexedGeometryArrayRetained {

    IndexedQuadArrayRetained() {
	this.geoType = GEO_TYPE_INDEXED_QUAD_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Point3d pnts[] = new Point3d[4];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
        int[] vtxIndexArr = new int[4];

        //NVaidya
        // Bug 447: While loops below now traverse over all
        // elements in the valid index range from initialIndexIndex
        // to initialIndexInex + validIndexCount - 1
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;
        pnts[0] = new Point3d();
        pnts[1] = new Point3d();
        pnts[2] = new Point3d();
	pnts[3] = new Point3d();

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
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

	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
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
	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
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
	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
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
	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
                }
		if (intersectBoundingPolytope(pnts, bpolytope, sdist, iPnt)) {
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
	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
                }
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
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;
	    while (i < loopStopIndex) {
                for(int j=0; j<4; j++) {
                    vtxIndexArr[j] = indexCoord[i];
                    getVertexData(indexCoord[i++], pnts[j]);
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
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedQuadArrayRetained0"));
	default:
	    throw new RuntimeException("PickShape not supported for intersection ");
	}

	if (minDist < Double.MAX_VALUE) {
	    iPnt.x = x;
	    iPnt.y = y;
	    iPnt.z = z;
	    return true;
	}
	return false;

    }

    // intersect pnts[] with every quad in this object
    boolean intersect(Point3d[] pnts) {
	Point3d[] points = new Point3d[4];
        double dist[] = new double[1];
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;

        points[0] = new Point3d();
        points[1] = new Point3d();
	points[2] = new Point3d();
	points[3] = new Point3d();

	switch (pnts.length) {
	case 3: // Triangle
	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[1], pnts[2]) ||
		    intersectTriTri(points[0], points[2], points[3],
				    pnts[0], pnts[1], pnts[2])) {
		    return true;
		}
	    }
	    break;
	case 4: // Quad
	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[1], pnts[2]) ||
		    intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[2], pnts[3]) ||
		    intersectTriTri(points[0], points[2], points[3],
				    pnts[0], pnts[1], pnts[2]) ||
		    intersectTriTri(points[0], points[2], points[3],
				    pnts[0], pnts[2], pnts[3])) {
		    return true;
		}
	    }
	    break;
	case 2: // Line
	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectSegment(points, pnts[0], pnts[1], dist,
				     null)) {
		    return true;
		}
	    }
	    break;
	case 1: // Point
	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectTriPnt(points[0], points[1], points[2],
				    pnts[0]) ||
		    intersectTriPnt(points[0], points[2], points[3],
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

        Point3d[] points = new Point3d[4];
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;

        points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();
	points[3] = new Point3d();

	while (i < loopStopIndex) {
	    getVertexData(indexCoord[i++], points[0]);
	    getVertexData(indexCoord[i++], points[1]);
	    getVertexData(indexCoord[i++], points[2]);
	    getVertexData(indexCoord[i++], points[3]);
	    thisToOtherVworld.transform(points[0]);
	    thisToOtherVworld.transform(points[1]);
	    thisToOtherVworld.transform(points[2]);
	    thisToOtherVworld.transform(points[3]);
	    if (geom.intersect(points)) {
		return true;
	    }
	}  // for each quad
	return false;
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
        Point3d[] points = new Point3d[4];
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;

        points[0] = new Point3d();
        points[1] = new Point3d();
        points[2] = new Point3d();
        points[3] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectBoundingBox(points, box, null, null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;

	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectBoundingSphere(points, bsphere, null,
					    null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;
	    while (i < loopStopIndex) {
		getVertexData(indexCoord[i++], points[0]);
		getVertexData(indexCoord[i++], points[1]);
		getVertexData(indexCoord[i++], points[2]);
		getVertexData(indexCoord[i++], points[3]);
		if (intersectBoundingPolytope(points, bpolytope, null, null)) {
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


    int getClassType() {
	return QUAD_TYPE;
    }
}
