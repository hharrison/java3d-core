/*
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.vecmath.Point3d;

/**
 * A leaf node that holds a merged shapes in compile mode
 */
class Shape3DCompileRetained extends Shape3DRetained {


    int numShapes = 0;

// Each element in the arraylist is an array of geometries for a
// particular merged shape
ArrayList<ArrayList<Geometry>> geometryInfo = null;

    Object[] srcList = null;

Shape3DCompileRetained(Shape3DRetained[] shapes, int nShapes, int compileFlags) {
	int i, j;
	// Merged list, only merged if geometry is mergeable
	ArrayList<GeometryArrayRetained>[] mergedList = new ArrayList[GeometryRetained.GEO_TYPE_GEOMETRYARRAY + 1];
	// Sorted list of separate geometry by geoType
	ArrayList<GeometryArrayRetained>[] separateList = new ArrayList[GeometryRetained.GEO_TYPE_GEOMETRYARRAY + 1];

	// Assign the num of shapes
	numShapes = nShapes;

	srcList = new Object[nShapes];

	if (nShapes > 0) {
		boundsAutoCompute = shapes[0].boundsAutoCompute;
		source = shapes[0].source;
	}

	// Remove the null that was added by Shape3DRetained constructor
	geometryList.remove(0);
	int geoIndex = 0;

	// Assign the fields for this compile shape
	boundsAutoCompute = shapes[0].boundsAutoCompute;
	isPickable = shapes[0].isPickable;
	isCollidable = shapes[0].isCollidable;
	appearanceOverrideEnable = shapes[0].appearanceOverrideEnable;
	appearance = shapes[0].appearance;
	collisionBound = shapes[0].collisionBound;
	localBounds = shapes[0].localBounds;

	if ((compileFlags & CompileState.GEOMETRY_READ) != 0)
		geometryInfo = new ArrayList<ArrayList<Geometry>>();

	for (i = 0; i < nShapes; i++) {
		Shape3DRetained shape = shapes[i];
		((Shape3D)shape.source).id = i;
		shape.source.retained = this;
		srcList[i] = shape.source;
		// If the transform has been pushd down
		// to the shape, don't merge its geometry with other shapes
		// geometry
		// Put it in a separate list sorted by geo_type
		// Have to handle shape.isPickable

		for (j = 0; j < shape.geometryList.size(); j++) {
			GeometryArrayRetained geo = (GeometryArrayRetained)shape.geometryList.get(j);
			if (geo == null)
				continue;

			if (shape.willRemainOpaque(geo.geoType) && geo.isMergeable()) {
				if (mergedList[geo.geoType] == null) {
					mergedList[geo.geoType] = new ArrayList<GeometryArrayRetained>();
				}
				mergedList[geo.geoType].add(geo);
			}
			else {
				// Keep a sorted list based on geoType;
				if (separateList[geo.geoType] == null) {
					separateList[geo.geoType] = new ArrayList<GeometryArrayRetained>();
				}
				// add it to the geometryList separately
				separateList[geo.geoType].add(geo);
			}
		}

		// Point to the geometryList's source, so the
		// retained side will be garbage collected
		if ((compileFlags & CompileState.GEOMETRY_READ) != 0) {
			ArrayList<Geometry> sList = new ArrayList<Geometry>();
			for (j = 0; j < shape.geometryList.size(); j++) {
				GeometryRetained g = (GeometryRetained)shape.geometryList.get(j);
				if (g != null)
					sList.add((Geometry)g.source);
				else
					sList.add(null);
			}
			geometryInfo.add(sList);
		}
	}

	// Now, merged the mergelist and separate list based on geoType,
	// this enables dlist optmization
	for (i = 1; i <= GeometryRetained.GEO_TYPE_GEOMETRYARRAY; i++) {
		switch (i) {
		case GeometryArrayRetained.GEO_TYPE_QUAD_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new QuadArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_TRI_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new TriangleArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_POINT_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new PointArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_LINE_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new LineArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_TRI_STRIP_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new TriangleStripArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_TRI_FAN_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new TriangleFanArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_LINE_STRIP_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new LineStripArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_QUAD_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new IndexedQuadArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new IndexedTriangleArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_POINT_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new IndexedPointArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_LINE_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i], new IndexedLineArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i],
						new IndexedTriangleStripArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i],
						new IndexedTriangleFanArrayRetained());
			addSeparateList(separateList[i]);
			break;
		case GeometryArrayRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
			if (mergedList[i] != null)
				addMergedList(mergedList[i],
						new IndexedLineStripArrayRetained());
			addSeparateList(separateList[i]);
			break;
		}
	}
}

private void addMergedList(ArrayList<GeometryArrayRetained> glist,
                           GeometryArrayRetained cgeo) {
	cgeo.setCompiled(glist);
	geometryList.add(cgeo);
	cgeo.setSource(((SceneGraphObjectRetained) glist.get(0)).source);
}

private void addSeparateList(ArrayList<GeometryArrayRetained> glist) {
	if (glist == null)
		return;

	for (int k = 0; k < glist.size(); k++) {
		geometryList.add(glist.get(k));
	}
}
    Bounds getCollisionBounds(int childIndex) {
	return collisionBound;
    }


int numGeometries(int childIndex) {
	return geometryInfo.get(childIndex).size();
}

Geometry getGeometry(int i, int childIndex) {
	return geometryInfo.get(childIndex).get(i);
}

Enumeration<Geometry> getAllGeometries(int childIndex) {
	ArrayList<Geometry> geoInfo = geometryInfo.get(childIndex);
	Vector<Geometry> geomList = new Vector<Geometry>();

	for (int i = 0; i < geoInfo.size(); i++) {
		geomList.add(geoInfo.get(i));
	}

	return geomList.elements();
}

Bounds getBounds(int childIndex) {
	if (!boundsAutoCompute)
		return super.getBounds();

	ArrayList<Geometry> glist = geometryInfo.get(childIndex);
	if (glist == null)
		return null;

	BoundingBox bbox = new BoundingBox((Bounds)null);
	for (int i = 0; i < glist.size(); i++) {
		Geometry g = glist.get(i);
		if (g == null)
			continue;

		GeometryRetained geometry = (GeometryRetained)g.retained;
		if (geometry.geoType == GeometryRetained.GEO_TYPE_NONE)
			continue;

		geometry.computeBoundingBox();
		synchronized (geometry.geoBounds) {
			bbox.combine(geometry.geoBounds);
		}
	}

	return bbox;
}


    /**
     * Check if the geometry component of this shape node under path
     * intersects with the pickRay.
     * @return true if intersected else false. If return is true, dist
     *  contains the closest
     * distance of intersection.
     * @exception IllegalArgumentException if <code>path</code> is
     * invalid.
     */
    boolean intersect(SceneGraphPath path,
            PickShape pickShape, double[] dist) {

        int flags;
        PickInfo pickInfo = new PickInfo();

        Transform3D localToVworld = path.getTransform();
        if (localToVworld == null) {
	    throw new IllegalArgumentException(J3dI18N.getString("Shape3DRetained3"));
	}
        pickInfo.setLocalToVWorldRef( localToVworld);

        Shape3D shape  = (Shape3D) path.getObject();
	// Get the geometries for this shape only, since the compiled
	// geomtryList contains several shapes
	ArrayList<Geometry> glist = geometryInfo.get(shape.id);

        // System.err.println("Shape3DCompileRetained.intersect() : ");
        if (dist == null) {
            // System.err.println("      no dist request ....");
            return intersect(pickInfo, pickShape, 0, glist);
        }

        flags = PickInfo.CLOSEST_DISTANCE;
        if (intersect(pickInfo, pickShape, flags, glist)) {
            dist[0] = pickInfo.getClosestDistance();
            return true;
        }

        return false;

      }

      boolean intersect(PickInfo pickInfo, PickShape pickShape, int flags,
              ArrayList geometryList) {

        Transform3D localToVworld = pickInfo.getLocalToVWorldRef();

 	Transform3D t3d = new Transform3D();
	t3d.invert(localToVworld);
	PickShape newPS = pickShape.transform(t3d);

	int geomListSize = geometryList.size();
	GeometryRetained geometry;

        if (((flags & PickInfo.CLOSEST_INTERSECTION_POINT) == 0) &&
            ((flags & PickInfo.CLOSEST_DISTANCE) == 0) &&
            ((flags & PickInfo.CLOSEST_GEOM_INFO) == 0) &&
            ((flags & PickInfo.ALL_GEOM_INFO) == 0)) {

	    for (int i=0; i < geomListSize; i++) {
		geometry =  (GeometryRetained) geometryList.get(i);
		if (geometry != null) {
		    if (geometry.mirrorGeometry != null) {
			geometry = geometry.mirrorGeometry;
		    }
                    // Need to modify this method
		    // if (geometry.intersect(newPS, null, null)) {
                    if (geometry.intersect(newPS, null, 0, null, null, 0)) {
			return true;
		    }
		}
	    }
	}
        else {
            double distance;
	    double minDist = Double.POSITIVE_INFINITY;
            Point3d closestIPnt = new Point3d();
            Point3d iPnt = new Point3d();
            Point3d iPntVW = new Point3d();

	    for (int i=0; i < geomListSize; i++) {
		geometry =  (GeometryRetained) geometryList.get(i);
		if (geometry != null) {
		    if (geometry.mirrorGeometry != null) {
			geometry = geometry.mirrorGeometry;
		    }
                    if (geometry.intersect(newPS, pickInfo, flags, iPnt, geometry, i)) {

                        iPntVW.set(iPnt);
                        localToVworld.transform(iPntVW);
			distance = pickShape.distance(iPntVW);

			if (minDist > distance) {
			    minDist = distance;
                            closestIPnt.set(iPnt);
                        }
                    }
		}
	    }

	    if (minDist < Double.POSITIVE_INFINITY) {
                if ((flags & PickInfo.CLOSEST_DISTANCE) != 0) {
                    pickInfo.setClosestDistance(minDist);
                }
                if((flags & PickInfo.CLOSEST_INTERSECTION_POINT) != 0) {
                    pickInfo.setClosestIntersectionPoint(closestIPnt);
                }
		return true;
	    }
	}

	return false;

    }

}
