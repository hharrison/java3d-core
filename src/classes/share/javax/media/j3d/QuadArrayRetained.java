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
import javax.vecmath.Vector3d;

/**
 * The QuadArray object draws the array of vertices as individual
 * quadrilaterals.  Each group
 * of four vertices defines a quadrilateral to be drawn.
 */

class QuadArrayRetained extends GeometryArrayRetained {

    QuadArrayRetained() {
	this.geoType = GEO_TYPE_QUAD_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {

	Point3d pnts[] = new Point3d[4];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
        int[] vtxIndexArr = new int[4];

	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();
	pnts[3] = new Point3d();

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < validVertexCount) {
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
                for(int j=0; j<4; j++) {
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
	    throw new IllegalArgumentException(J3dI18N.getString("QuadArrayRetained0"));
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
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();
	points[3] = new Point3d();

	switch (pnts.length) {
	case 3: // Triangle
	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
		if (intersectTriTri(points[0], points[1], points[2],
				    pnts[0], pnts[1], pnts[2]) ||
		    intersectTriTri(points[0], points[2], points[3],
				    pnts[0], pnts[1], pnts[2])) {
		    return true;
		}
	    }
	    break;
	case 4: // Quad

	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
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
	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
		if (intersectSegment(points, pnts[0], pnts[1], dist,
				     null)) {
		    return true;
		}
	    }
	    break;
	case 1: // Point
	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
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


    boolean intersect(Transform3D thisToOtherVworld,  GeometryRetained geom) {

	Point3d[] points = new Point3d[4];
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();
	points[3] = new Point3d();

	while (i < validVertexCount) {
	    getVertexData(i++, points[0]);
	    getVertexData(i++, points[1]);
	    getVertexData(i++, points[2]);
	    getVertexData(i++, points[3]);
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
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();
	points[3] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
		if (intersectBoundingBox(points, box, null, null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;

	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
		if (intersectBoundingSphere(points, bsphere, null,
					    null)) {
		    return true;
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;

	    while (i < validVertexCount) {
		getVertexData(i++, points[0]);
		getVertexData(i++, points[1]);
		getVertexData(i++, points[2]);
		getVertexData(i++, points[3]);
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

    // From Graphics Gems IV (pg5) and Graphics Gems II, Pg170
    // The centroid is the area-weighted sum of the centroids of
    // disjoint triangles that make up the polygon.
    void computeCentroid() {
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);

	Point3d pnt0 = new Point3d();
	Point3d pnt1 = new Point3d();
	Point3d pnt2 = new Point3d();
	Point3d pnt3 = new Point3d();
	Vector3d vec = new Vector3d();
	Vector3d normal = new Vector3d();
	Vector3d tmpvec = new Vector3d();

	double area;
	double totalarea = 0;

	centroid.x = 0;
	centroid.y = 0;
	centroid.z = 0;

	while (i < validVertexCount) {
	    getVertexData(i++, pnt0);
	    getVertexData(i++, pnt1);
	    getVertexData(i++, pnt2);
	    getVertexData(i++, pnt3);

	    // Determine the normal
	    tmpvec.sub(pnt0, pnt1);
	    vec.sub(pnt1, pnt2);

	    // Do the cross product
	    normal.cross(tmpvec, vec);
	    normal.normalize();
	    // If a degenerate triangle, don't include
	    if (Double.isNaN(normal.x+normal.y+normal.z))
		continue;
	    tmpvec.set(0,0,0);
	    // compute the area of each triangle
	    getCrossValue(pnt0, pnt1, tmpvec);
	    getCrossValue(pnt1, pnt2, tmpvec);
	    getCrossValue(pnt2, pnt0, tmpvec);
	    area = normal.dot(tmpvec);
	    totalarea += area;
	    centroid.x += (pnt0.x+pnt1.x+pnt2.x) * area;
	    centroid.y += (pnt0.y+pnt1.y+pnt2.y) * area;
	    centroid.z += (pnt0.z+pnt1.z+pnt2.z) * area;

	    // compute the area of each triangle
	    tmpvec.set(0,0,0);
	    getCrossValue(pnt0, pnt2, tmpvec);
	    getCrossValue(pnt2, pnt3, tmpvec);
	    getCrossValue(pnt3, pnt0, tmpvec);
	    area = normal.dot(tmpvec);
	    totalarea += area;
	    centroid.x += (pnt3.x+pnt0.x+pnt2.x) * area;
	    centroid.y += (pnt3.y+pnt0.y+pnt2.y) * area;
	    centroid.z += (pnt3.z+pnt0.z+pnt2.z) * area;
	}
	if (totalarea != 0.0) {
	    area = 1.0/(3.0 * totalarea);
	    centroid.x *= area;
	    centroid.y *= area;
	    centroid.z *= area;
	}
    }

    int getClassType() {
	return QUAD_TYPE;
    }
}
