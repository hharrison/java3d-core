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
 * The IndexedLineStripArray object draws an array of vertices as a set of
 * connected line strips.  An array of per-strip vertex counts specifies
 * where the separate strips appear in the vertex array.
 * For every strip in the set, each vertex, beginning with
 * the second vertex in the array, defines a line segment to be drawn
 * from the previous vertex to the current vertex.
 */

class IndexedLineStripArrayRetained extends IndexedGeometryStripArrayRetained {
	
    IndexedLineStripArrayRetained() {
        geoType = GEO_TYPE_INDEXED_LINE_STRIP_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
	Point3d pnts[] = new Point3d[2];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
	int scount, j, i = 0;
	int count = 0;
        int[] vtxIndexArr = new int[2];
        
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();
    
	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	   while (i < stripIndexCounts.length) {
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKSEGMENT:
	    PickSegment pickSegment = (PickSegment) pickShape;
	    Vector3d dir = 
		new Vector3d(pickSegment.end.x - pickSegment.start.x, 
			     pickSegment.end.y - pickSegment.start.y,
			     pickSegment.end.z - pickSegment.start.z);
	    
	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {	 
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox bbox = (BoundingBox) 
		               ((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {	 
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) 
		                     ((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) 
		                      ((PickBounds) pickShape).bounds;

	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKCYLINDER:
	    PickCylinder pickCylinder= (PickCylinder) pickShape;

	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;

	    while (i < stripIndexCounts.length) {  
                vtxIndexArr[0] = indexCoord[count];
		getVertexData(indexCoord[count++], pnts[0]);
		scount =  stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
                    vtxIndexArr[1] = indexCoord[count];
		    getVertexData(indexCoord[count++], pnts[1]);
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
		    pnts[0].set(pnts[1]);
                    vtxIndexArr[0] = vtxIndexArr[1];
		}
	    }
	    break;
	case PickShape.PICKPOINT:
	    // Should not happen since API already check for this
	    throw new IllegalArgumentException(J3dI18N.getString("IndexedLineStripArrayRetained0"));
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
	int i = 0;
	int j, count=0;
	int scount;
	Point3d[] points = new Point3d[2];
	double dist[] = new double[1];
	Vector3d dir;
	
	points[0] = new Point3d();
	points[1] = new Point3d();
	
	switch (pnts.length) {
	case 3:
	case 4: // Triangle, Quad
	   while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[1]);
		    if (intersectSegment(pnts, points[0], points[1],
					 dist, null)) {
			return true;
		    }
		    points[0].set(points[1]);
		}
	    }
	    break;
	case 2: // line
	    dir = new Vector3d();
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[1]);
		    dir.x = points[1].x - points[0].x;
		    dir.y = points[1].y - points[0].y;
		    dir.z = points[1].z - points[0].z;
		    if (intersectLineAndRay(pnts[0], pnts[1],
					   points[0], dir, dist, null)
			&& (dist[0] <= 1.0)) {
			return true;
		    }
		    points[0].set(points[1]);
		}
	    }
	    break;
	case 1: // point
	    dir = new Vector3d();
	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], points[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], points[1]);
		    dir.x = points[1].x - points[0].x;
		    dir.y = points[1].y - points[0].y;
		    dir.z = points[1].z - points[0].z;
		    if (intersectPntAndRay(pnts[0], points[0], dir,
					   dist) &&
			(dist[0] <= 1.0)) {
			return true;
		    }
		    points[0].set(points[1]);
		}
	    }
	    break;
	}
	
	return false;
    }
    
    boolean intersect(Transform3D thisToOtherVworld,
		      GeometryRetained geom) {
	int i = 0;
	int j, count=0;
	Point3d[] pnts = new Point3d[2];
	int scount;
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	while (i < stripIndexCounts.length) {
	    getVertexData(indexCoord[count++], pnts[0]);
	    thisToOtherVworld.transform(pnts[0]);
	    scount = stripIndexCounts[i++];

	    for (j = 1; j < scount; j++) {
		getVertexData(indexCoord[count++], pnts[1]);
		thisToOtherVworld.transform(pnts[1]);
		if (geom.intersect( pnts)) {
		    return true;
		}
		pnts[0].set(pnts[1]);
	    }
	}
	return false;
    }

    // the bounds argument is already transformed
    boolean intersect(Bounds targetBound) {
	int i = 0;
	int j, count=0;
	Point3d[] pnts = new Point3d[2];
	int scount;
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[1]);
		    if (intersectBoundingBox(pnts, box, null, null)) {
			return true;
		    }
		    pnts[0].set(pnts[1]);
		}

	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;

	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[1]);
		    if (intersectBoundingSphere(pnts, bsphere, null, null)) {
			return true;
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;

	    while (i < stripIndexCounts.length) {
		getVertexData(indexCoord[count++], pnts[0]);
		scount = stripIndexCounts[i++];
		for (j=1; j < scount; j++) {
		    getVertexData(indexCoord[count++], pnts[1]);
		    if (intersectBoundingPolytope(pnts, bpolytope, null, null)) {
			return true;
		    }
		    pnts[0].set(pnts[1]);
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
	return LINE_TYPE; 
    }
}
