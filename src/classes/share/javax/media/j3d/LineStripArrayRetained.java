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
 * The LineStripArray object draws an array of vertices as a set of
 * connected line strips.  An array of per-strip vertex counts specifies
 * where the separate strips appear in the vertex array.
 * For every strip in the set, each vertex, beginning with
 * the second vertex in the array, defines a line segment to be drawn
 * from the previous vertex to the current vertex.
 */

class LineStripArrayRetained extends GeometryStripArrayRetained {

    LineStripArrayRetained() {
	this.geoType = GEO_TYPE_LINE_STRIP_SET;
    }

    boolean intersect(PickShape pickShape, PickInfo.IntersectionInfo iInfo,  int flags, Point3d iPnt) {
	Point3d pnts[] = new Point3d[2];
	double sdist[] = new double[1];
	double minDist = Double.MAX_VALUE;
	double x = 0, y = 0, z = 0;
	int j, end;
	int i = 0;
        int count = 0;
        int minICount = 0; 
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();

	switch (pickShape.getPickType()) {
	case PickShape.PICKRAY:
	    PickRay pickRay= (PickRay) pickShape;

	    while(i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectLineAndRay(pnts[0], pnts[1], pickRay.origin,
					    pickRay.direction, sdist,
					    iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKSEGMENT:
	    PickSegment pickSegment = (PickSegment) pickShape;
	    Vector3d dir = 
		new Vector3d(pickSegment.end.x - pickSegment.start.x, 
			     pickSegment.end.y - pickSegment.start.y,
			     pickSegment.end.z - pickSegment.start.z);
	    
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
                while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectLineAndRay(pnts[0], pnts[1],
					    pickSegment.start, 
					    dir, sdist, iPnt) &&
			(sdist[0] <= 1.0)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);			
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox bbox = (BoundingBox) 
		               ((PickBounds) pickShape).bounds;

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {	 
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectBoundingBox(pnts, bbox, sdist, iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);			   
		}
	    }

	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) 
		                     ((PickBounds) pickShape).bounds;

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectBoundingSphere(pnts, bsphere, sdist, iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) 
		                      ((PickBounds) pickShape).bounds;

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectBoundingPolytope(pnts, bpolytope, sdist, iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKCYLINDER:
	    PickCylinder pickCylinder= (PickCylinder) pickShape;

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectCylinder(pnts, pickCylinder, sdist, iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKCONE:
	    PickCone pickCone= (PickCone) pickShape;

	    while (i < stripVertexCounts.length) {  
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);
                count++;
		while (j < end) {
		    getVertexData(j++, pnts[1]);
                    count++;
		    if (intersectCone(pnts, pickCone, sdist, iPnt)) {
			if (flags == 0) {
			    return true;
			}
			if (sdist[0] < minDist) {
			    minDist = sdist[0];
                            minICount = count;
                            x = iPnt.x;
                            y = iPnt.y;
                            z = iPnt.z;
			}
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKPOINT:
	    // Should not happen since API already check for this
	    throw new IllegalArgumentException(J3dI18N.getString("LineStripArrayRetained0"));
	default:
	    throw new RuntimeException ("PickShape not supported for intersection"); 
	} 

	if (minDist < Double.MAX_VALUE) {
            assert(minICount >= 2);
            int[] vertexIndices = iInfo.getVertexIndices();
            if (vertexIndices == null) {
                vertexIndices = new int[2];
                iInfo.setVertexIndices(vertexIndices);
            }
            vertexIndices[0] = minICount - 2;
            vertexIndices[1] = minICount - 1;
	    iPnt.x = x;
	    iPnt.y = y;
	    iPnt.z = z;
	    return true;
	}
	return false;  
    }
  
    boolean intersect(Point3d[] pnts) {
	int j, end;
	Point3d[] points = new Point3d[2];
	double dist[] = new double[1];
	Vector3d dir;
	int i = 0;

	points[0] = new Point3d();
	points[1] = new Point3d();



	switch (pnts.length) {
	case 3:
	case 4: // Triangle, Quad
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		while (j < end) {
		    getVertexData(j++, points[1]);		
		    if (intersectSegment(pnts, points[0], points[1],
					 dist, null)) {
			return true;
		    }
		    points[0].set(points[1]);
		}
	    }
	    break;
	case 2: // Line
	    dir = new Vector3d();
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		while (j < end) {
		    getVertexData(j++, points[1]);		
		    dir.x = points[1].x - points[0].x;
		    dir.y = points[1].y - points[0].y;
		    dir.z = points[1].z - points[0].z;
		    if (intersectLineAndRay(pnts[0], pnts[1],
					   points[0], dir, dist, null) &&
			(dist[0] <= 1.0)) {
			return true;
		    }
		    points[0].set(points[1]);
		}		
	    }
	    break;
	case 1: // Point
	    dir = new Vector3d();
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, points[0]);		
		while (j < end) {
		    getVertexData(j++, points[1]);		
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
	int j, end;
	Point3d[] pnts = new Point3d[2];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();


	while (i < stripVertexCounts.length) {
	    j = stripStartVertexIndices[i];
	    end = j + stripVertexCounts[i++];
	    getVertexData(j++, pnts[0]);		
	    thisToOtherVworld.transform(pnts[0]);
	    while (j < end) {
		getVertexData(j++, pnts[1]);		
		thisToOtherVworld.transform(pnts[1]);
		if (geom.intersect(pnts)) {
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
	int j, offset, end;
	Point3d[] pnts = new Point3d[2];
	pnts[0] = new Point3d();
	pnts[1] = new Point3d();



	switch(targetBound.getPickType()) {
	case PickShape.PICKBOUNDINGBOX:
	    BoundingBox box = (BoundingBox) targetBound;

	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);		
		while ( j < end) {
		    getVertexData(j++, pnts[1]);		
		    if (intersectBoundingBox(pnts, box, null, null)) {
			return true;
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGSPHERE:
	    BoundingSphere bsphere = (BoundingSphere) targetBound;
	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);		
		while ( j < end) {
		    getVertexData(j++, pnts[1]);		
		    if (intersectBoundingSphere(pnts, bsphere, null,
						null)) {
			return true;
		    }
		    pnts[0].set(pnts[1]);
		}
	    }
	    break;
	case PickShape.PICKBOUNDINGPOLYTOPE:
	    BoundingPolytope bpolytope = (BoundingPolytope) targetBound;

	    while (i < stripVertexCounts.length) {
		j = stripStartVertexIndices[i];
		end = j + stripVertexCounts[i++];
		getVertexData(j++, pnts[0]);		
		while ( j < end) {
		    getVertexData(j++, pnts[1]);		
		    if (intersectBoundingPolytope(pnts, bpolytope,
						  null, null)) {
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

    // From Graphics Gems IV (pg5) and Graphics Gems II, Pg170
    void computeCentroid() {
	int i = 0;
	int j;
	double length;
	double totallength = 0;
	int start, end;
	boolean replaceVertex1;
	Point3d pnt0 = new Point3d();
	Point3d pnt1 = new Point3d();

	centroid.x = 0;
	centroid.y = 0;
	centroid.z = 0;



	while (i < stripVertexCounts.length) {  
	    j = stripStartVertexIndices[i];
	    end = j + stripVertexCounts[i++];
	    getVertexData(j++, pnt0);
	    replaceVertex1 = true;
	    while (j < end) {
		if (replaceVertex1) {
		    getVertexData(j++, pnt1);
		    replaceVertex1 = false;
		} else {
		    getVertexData(j++, pnt0);
		    replaceVertex1 = true;
		}
		length = pnt0.distance(pnt1);
		centroid.x += (pnt0.x + pnt1.x) * length;
		centroid.y += (pnt0.y + pnt1.y) * length;
		centroid.z += (pnt0.z + pnt1.z) * length;
		totallength += length;
	    }
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
