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

import javax.vecmath.*;
import java.lang.Math;

/**
 * The LineArray object draws the array of vertices as individual
 * line segments.  Each pair of vertices defines a line to be drawn.
 */

class LineArrayRetained extends GeometryArrayRetained implements Cloneable {

    LineArrayRetained() {
	this.geoType = GEO_TYPE_LINE_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Point3d pnts[] = new Point3d[2];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
        int[] vtxIndexArr = new int[2];

	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < validVertexCount) {
                for(int j=0; j<2; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectLineAndRay(pnts[0], pnts[1], pickRay.origin,
					pickRay.direction, sdist,
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
	case PickShape.PICKSEGMENT:
	    PickSegment pickSegment = (PickSegment) pickShape;
	    Vector3d dir =
		new Vector3d(pickSegment.end.x - pickSegment.start.x,
			     pickSegment.end.y - pickSegment.start.y,
			     pickSegment.end.z - pickSegment.start.z);

	    while (i < validVertexCount) {
                for(int j=0; j<2; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
                }
		if (intersectLineAndRay(pnts[0], pnts[1],
					pickSegment.start,
					dir, sdist, iPnt) &&
		    (sdist[0] <= 1.0)) {
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
                for(int j=0; j<2; j++) {
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
                for(int j=0; j<2; j++) {
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
                for(int j=0; j<2; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
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

	    while (i < validVertexCount) {
                for(int j=0; j<2; j++) {
                    vtxIndexArr[j] = i;
                    getVertexData(i++, pnts[j]);
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

	    while (i < validVertexCount) {
                for(int j=0; j<2; j++) {
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
	    throw new IllegalArgumentException(J3dI18N.getString("LineArrayRetained0"));
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
	Point3d[] points = new Point3d[2];
	double dist[] = new double[1];
	Vector3d dir;
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	points[0] = new Point3d();
	points[1] = new Point3d();

	switch (pnts.length) {
	case 3: // Triangle
	case 4: // Quad
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		if (intersectSegment(pnts, points[0], points[1], dist,
				     null)) {
		    return true;
		}
	    }
	    break;
	case 2: // Line
	    dir = new Vector3d();
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		dir.x = points[1].x - points[0].x;
		dir.y = points[1].y - points[0].y;
		dir.z = points[1].z - points[0].z;
		if (intersectLineAndRay(pnts[0], pnts[1], points[0],
					dir, dist, null) &&
		    (dist[0] <= 1.0)) {
			return true;
		}
	    }
	    break;
	case 1: // Point
	    dir = new Vector3d();
	    while (i<validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		dir.x = points[1].x - points[0].x;
		dir.y = points[1].y - points[0].y;
		dir.z = points[1].z - points[0].z;
		if (intersectPntAndRay(pnts[0], points[0], dir, dist) &&
		    (dist[0] <= 1.0)) {
		    return true;
		}
	    }
	    break;
	}
	return false;
    }

    boolean intersect(Transform3D thisToOtherVworld,
		      GeometryRetained geom) {
	Point3d[] pnts = new Point3d[2];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	while (i < validVertexCount) {
	    getVertexData(i++, pnts[0]);
	    getVertexData(i++, pnts[1]);
	    thisToOtherVworld.transform(pnts[0]);
	    thisToOtherVworld.transform(pnts[1]);
	    if (geom.intersect(pnts)) {
		return true;
	    }
	}
	return false;
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
	Point3d[] pnts = new Point3d[2];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < validVertexCount) {
		getVertexData(i++, pnts[0]);
		getVertexData(i++, pnts[1]);
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
	Point3d pnt0 = new Point3d();
	Point3d pnt1 = new Point3d();
	double length;
	double totallength = 0;
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	centroid.x = 0;
	centroid.y = 0;
	centroid.z = 0;

	while (i < validVertexCount) {
	    getVertexData(i++, pnt0);
	    getVertexData(i++, pnt1);
	    length = pnt0.distance(pnt1);
	    centroid.x += (pnt0.x + pnt1.x) * length;
	    centroid.y += (pnt0.y + pnt1.y) * length;
	    centroid.z += (pnt0.z + pnt1.z) * length;
	    totallength += length;
	}

	if (totallength != 0.0) {
	    length = 1.0/(2.0 * totallength);
	    centroid.x *= length;
	    centroid.y *= length;
	    centroid.z *= length;
	}
    }

    int getClassType() {
	return LINE_TYPE;
    }
}
