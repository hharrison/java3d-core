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
 * The TriangleFanArray object draws an array of vertices as a set of
 * connected triangle fans.  An array of per-strip
 * vertex counts specifies where the separate strips (fans) appear
 * in the vertex array.  For every strip in the set,
 * each vertex, beginning with the third vertex in the array,
 * defines a triangle to be drawn using the current vertex,
 * the previous vertex and the first vertex.  This can be thought of
 * as a collection of convex polygons.
 */

class TriangleFanArrayRetained extends GeometryStripArrayRetained {

    TriangleFanArrayRetained() {
	this.geoType = GEO_TYPE_TRI_FAN_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Point3d pnts[] = new Point3d[3];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
	int i = 0;
	int j, end;
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();
        int[] vtxIndexArr = new int[3];

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;
	
	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
                for(int k=0; k<2; k++) {
                    vtxIndexArr[k] = j;
                    getVertexData(j++, pnts[k]);
                }
		while (j < end) {
                    vtxIndexArr[2] = j;
		    getVertexData(j++, pnts[2]);
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
	    throw new IllegalArgumentException(J3dI18N.getString("TriangleFanArrayRetained0"));
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
	int i = 0;

	points[0] = new Point3d();
	points[1] = new Point3d();
	points[2] = new Point3d();



	switch (pnts.length) {
	case 3: // Triangle
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		getVertexData(j++, points[1]);		
		while (j < end) {
		    getVertexData(j++, points[2]);		
		    if (intersectTriTri(points[0], points[1], points[2],
					pnts[0], pnts[1], pnts[2])) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	case 4: // Quad
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		getVertexData(j++, points[1]);		
		while (j < end) {
		    getVertexData(j++, points[2]);		
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
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		getVertexData(j++, points[1]);		
		while (j < end) {
		    getVertexData(j++, points[2]);		
		    if (intersectSegment(points, pnts[0], pnts[1],
					 dist, null)) {
			return true;
		    }
		    points[1].set(points[2]);
		}
	    }
	    break;
	case 1: // Point
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		getVertexData(j++, points[1]);		
		while (j < end) {
		    getVertexData(j++, points[2]);		
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
	int i = 0, j, end;
	Point3d[] pnts = new Point3d[3];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();

	while (i < stripVertexCounts.length) {
	    j = stripStartVertexIndices[i];
	    end = j + stripVertexCounts[i++];
	    getVertexData(j++, pnts[0]);		
	    getVertexData(j++, pnts[1]);	
	    thisToOtherVworld.transform(pnts[0]);
	    thisToOtherVworld.transform(pnts[1]);
	    while (j < end) {
		getVertexData(j++, pnts[2]);		
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
	int j, end;
	Point3d[] pnts = new Point3d[3];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
	pnts[2] = new Point3d();



	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		getVertexData(j++, pnts[0]);		
		getVertexData(j++, pnts[1]);		
		end = j + stripVertexCounts[i++];
		while ( j < end) {
		    getVertexData(j++, pnts[2]);		
		    if (intersectBoundingBox(pnts, box, null, null)) {
			return true;
		    }
		    pnts[1].set(pnts[2]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;

	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);		
		getVertexData(j++, pnts[1]);		
		while ( j < end) {
		    getVertexData(j++, pnts[2]);		
		    if (intersectBoundingSphere(pnts, bsphere, null, null)) {
			return true;
		    }
		    pnts[1].set(pnts[2]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;

	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);		
		getVertexData(j++, pnts[1]);		
		while ( j < end) {
		    getVertexData(j++, pnts[2]);		
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

    // From Graphics Gems IV (pg5) and Graphics Gems II, Pg170
    void computeCentroid() {
	Vector3d vec = new Vector3d();
	Vector3d normal = new Vector3d();
	Vector3d tmpvec = new Vector3d();
	Point3d pnt0 = new Point3d();	
	Point3d pnt1 = new Point3d();	
	Point3d pnt2 = new Point3d();	
	double area, totalarea = 0;
	int end, replaceIndex, j, i = 0;
	centroid.x = 0;
	centroid.y = 0;
	centroid.z = 0;

	while( i < stripVertexCounts.length) {
	    j = stripStartVertexIndices[i];
	    end = j + stripVertexCounts[i++];
	    getVertexData(j++, pnt0); 
	    getVertexData(j++, pnt1);
	    replaceIndex = 2;
	    while (j < end) {
		area = 0; 
		if (replaceIndex == 2) {
		    getVertexData(j++, pnt2);
		    replaceIndex = 1;
		} else {
		    getVertexData(j++, pnt1);
		    replaceIndex = 2;
		}

		// Determine the normal 
		vec.sub(pnt0, pnt1); 
		tmpvec.sub(pnt1, pnt2); 

		// Do the cross product
		normal.cross(vec, tmpvec); 
		normal.normalize();
		// If a degenerate triangle, don't include
		if (Double.isNaN(normal.x + normal.y + normal.z))
		    continue;

		tmpvec.set(0,0,0);
		
		// compute the area 
		getCrossValue(pnt0, pnt1, tmpvec);
		getCrossValue(pnt1, pnt2, tmpvec);
		getCrossValue(pnt2, pnt0, tmpvec);
		area = normal.dot(tmpvec);
		totalarea += area;
		centroid.x += (pnt0.x + pnt1.x + pnt2.x) * area;
		centroid.y += (pnt0.y + pnt1.y + pnt2.y) * area;
		centroid.z += (pnt0.z + pnt1.z + pnt2.z) * area;

	    }
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
