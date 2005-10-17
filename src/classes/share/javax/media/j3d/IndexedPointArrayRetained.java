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
 * The IndexedPointArray object draws the array of vertices as individual points.
 */

class IndexedPointArrayRetained extends IndexedGeometryArrayRetained {

    IndexedPointArrayRetained() {
        this.geoType = GEO_TYPE_INDEXED_POINT_SET;
    } 

    boolean intersect(PickShape pickShape, PickInfo.IntersectionInfo iInfo,  int flags, Point3d iPnt) {
 	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
        int count = 0;
        int minICount = 0; 
	Point3d pnt = new Point3d();
	int i = ((vertexFormat & GeometryArray.BY_REFERENCE) == 0 ?
		 initialVertexIndex : initialCoordIndex);
    
	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], pnt);
                count++;
		if (intersectPntAndRay(pnt, pickRay.origin,
				       pickRay.direction, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
                        minICount = count;
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
		getVertexData(indexCoord[i++], pnt);
                count++;
		if (intersectPntAndRay(pnt, pickSegment.start, 
					dir, sdist) &&
		    (sdist[0] <= 1.0)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
                        minICount = count;
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
		    }
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGBOX:
	case PickShape.PICKBOUNDINGSPHERE:
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    Bounds bounds = ((PickBounds) pickShape).bounds;

	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], pnt);
                count++;
		if (bounds.intersect(pnt)) {
		    if (flags == 0) {
			return true;
		    }
		    sdist[0] = pickShape.distance(pnt);
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
                        minICount = count;
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
		    }
		}
	    }
	    break;
	case PickShape.PICKCYLINDER:
	    PickCylinder pickCylinder= (PickCylinder) pickShape;

	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], pnt);
                count++;
		if (intersectCylinder(pnt, pickCylinder, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
                        minICount = count;
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
		    }
		}
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;

	    while (i < validVertexCount) {
		getVertexData(indexCoord[i++], pnt);
                count++;
		if (intersectCone(pnt, pickCone, sdist)) {
		    if (flags == 0) {
			return true;
		    }
		    if (sdist[0] < minDist) {
			minDist = sdist[0];
                        minICount = count;
			x = pnt.x;
			y = pnt.y;
			z = pnt.z;
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
            assert(minICount >=1);
            int[] vertexIndices = iInfo.getVertexIndices();
            if (vertexIndices == null) {
                vertexIndices = new int[1];
                iInfo.setVertexIndices(vertexIndices);
            }
            vertexIndices[0] = minICount - 1;
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

