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
 * The IndexedLineArray object draws the array of vertices as individual
 * line segments.  Each pair of vertices defines a line to be drawn.
 */

class IndexedLineArrayRetained extends IndexedGeometryArrayRetained {

    IndexedLineArrayRetained() {
        this.geoType = GEO_TYPE_INDEXED_LINE_SET;
    }
    
    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
            GeometryRetained geom, int geomIndex) {
        Point3d pnts[] = new Point3d[2];
        double sdist[] = new double[1];
        double minDist = Double.MAX_VALUE;
        double x = 0, y = 0, z = 0;
        //NVaidya
        // Bug 447: While loops below now traverse over all
        // elements in the valid index range from initialIndexIndex
        // to initialIndexInex + validIndexCount - 1
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;
        pnts[0] = new Point3d();
        pnts[1] = new Point3d();
        int[] vtxIndexArr = new int[2];
        
        switch (pickShape.getPickType()) {
            case PickShape.PICKRAY:
                PickRay pickRay= (PickRay) pickShape;
                
                while (i < loopStopIndex) {
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                
                while (i < loopStopIndex) {
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                
                while (i < loopStopIndex) {
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                    for(int k=0; k<2; k++) {
                        vtxIndexArr[k] = indexCoord[i];
                        getVertexData(indexCoord[i++], pnts[k]);
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
                throw new IllegalArgumentException(J3dI18N.getString("IndexedLineArrayRetained0"));
            default:
                throw new RuntimeException("PickShape not supported for intersection");
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
        Vector3d dir;
        double dist[] = new double[1];
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;
        points[0] = new Point3d();
        points[1] = new Point3d();
        
        switch (pnts.length) {
            case 3:  // Triangle/Quad , common case first
            case 4:
                while (i < loopStopIndex) {
                    getVertexData(indexCoord[i++], points[0]);
                    getVertexData(indexCoord[i++], points[1]);
                    if (intersectSegment(pnts, points[0], points[1], dist,
                            null)) {
                        return true;
                    }
                }
                break;
            case 2: // Line
                dir = new Vector3d();
                while (i < loopStopIndex) {
                    getVertexData(indexCoord[i++], points[0]);
                    getVertexData(indexCoord[i++], points[1]);
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
                while (i < loopStopIndex) {
                    getVertexData(indexCoord[i++], points[0]);
                    getVertexData(indexCoord[i++], points[1]);
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
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;
        pnts[0] = new Point3d();
        pnts[1] = new Point3d();
        
        while (i < loopStopIndex) {
            getVertexData(indexCoord[i++], pnts[0]);
            getVertexData(indexCoord[i++], pnts[1]);
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
        //NVaidya
        // Bug 447: correction for loop indices
        int i = initialIndexIndex;
        int loopStopIndex = initialIndexIndex + validIndexCount;
        pnts[0] = new Point3d();
        pnts[1] = new Point3d();
        
        switch(targetBound.getPickType()) {
            case PickShape.PICKBOUNDINGBOX:
                BoundingBox box = (BoundingBox) targetBound;
                
                while(i < loopStopIndex) {
                    getVertexData(indexCoord[i++], pnts[0]);
                    getVertexData(indexCoord[i++], pnts[1]);
                    if (intersectBoundingBox(pnts, box, null, null)) {
                        return true;
                    }
                }
                break;
            case PickShape.PICKBOUNDINGSPHERE:
                BoundingSphere bsphere = (BoundingSphere) targetBound;
                
                while(i < loopStopIndex) {
                    getVertexData(indexCoord[i++], pnts[0]);
                    getVertexData(indexCoord[i++], pnts[1]);
                    if (intersectBoundingSphere(pnts, bsphere, null, null)) {
                        return true;
                    }
                }
                break;
            case PickShape.PICKBOUNDINGPOLYTOPE:
                BoundingPolytope bpolytope = (BoundingPolytope) targetBound;
                
                while(i < loopStopIndex) {
                    getVertexData(indexCoord[i++], pnts[0]);
                    getVertexData(indexCoord[i++], pnts[1]);
                    if (intersectBoundingPolytope(pnts, bpolytope, null, null)) {
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
	return LINE_TYPE; 
    }
}
