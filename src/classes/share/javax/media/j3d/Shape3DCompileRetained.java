/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.*;

/**
 * A leaf node that holds a merged shapes in compile mode
 */
class Shape3DCompileRetained extends Shape3DRetained {


    int numShapes = 0;
    
    // Each element in the arraylist is an array of geometries for a
    // particular merged shape 
    ArrayList geometryInfo = null;


    Object[] srcList = null;
    
    Shape3DCompileRetained(Shape3DRetained[] shapes, int nShapes, int compileFlags) {
	int i, j;
	Shape3DRetained shape;
	GeometryArrayRetained geo;
	Vector list;
	// Merged list, only merged if geometry is mergeable
	Object[] mergedList = new Object[GeometryRetained.GEO_TYPE_GEOMETRYARRAY+1];
	// Sorted list of separate geometry by geoType
	Object[] separateList = new Object[GeometryRetained.GEO_TYPE_GEOMETRYARRAY+1];

	// Assign the num of shapes
	numShapes = nShapes;

	Bounds shapeBounds;

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
	    geometryInfo = new ArrayList();
	
	for (i = 0; i < nShapes; i++) {
	    shape = shapes[i];
	    ((Shape3D)shape.source).id = i;
	    shape.source.retained = this;
	    srcList[i] = shape.source;
	    // If the transform has been pushd down
	    // to the shape, don't merge its geometry with other shapes
	    // geometry
	    // Put it in a separate list sorted by geo_type
	    // Have to handle shape.isPickable

	    for (j = 0; j < shape.geometryList.size(); j++) {
		geo = (GeometryArrayRetained)shape.geometryList.get(j);
		if (geo != null) {
		    if (shape.willRemainOpaque(geo.geoType) && geo.isMergeable()) {
			if (mergedList[geo.geoType] == null) {
			    mergedList[geo.geoType] = new ArrayList();
			}
			((ArrayList)mergedList[geo.geoType]).add(geo);
		    }
		    else {
			// Keep a sorted list based on geoType;
			if (separateList[geo.geoType] == null) {
			    separateList[geo.geoType] = new ArrayList();
			}
			// add it to the geometryList separately
			((ArrayList)separateList[geo.geoType]).add(geo);
		    }
		}

	    }
	    
	    // Point to the geometryList's source, so the
	    // retained side will be garbage collected
	    if ((compileFlags & CompileState.GEOMETRY_READ) != 0) {
		ArrayList sList = new ArrayList();
		for (j = 0; j < shape.geometryList.size(); j++) {
		    GeometryRetained g = (GeometryRetained)shape.geometryList.get(j);
		    if (g != null)
			sList.add(g.source);
		    else
			sList.add(null);
		}
		geometryInfo.add(sList);
	    }

	}
	// Now, merged the mergelist and separate list based on geoType,
	// this enables dlist optmization
	for (i = 1; i <= GeometryRetained.GEO_TYPE_GEOMETRYARRAY;  i++) {
	    GeometryArrayRetained cgeo = null;
	    ArrayList curList;
	    switch (i) {
	    case GeometryArrayRetained.GEO_TYPE_QUAD_SET:
		if (mergedList[i] != null) {
		    cgeo = new QuadArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_TRI_SET:
		if (mergedList[i] != null) {
		    cgeo = new TriangleArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_POINT_SET:
		if (mergedList[i] != null) {
		    cgeo = new PointArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_LINE_SET:
		if (mergedList[i] != null) {
		    cgeo = new LineArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_TRI_STRIP_SET:
		if (mergedList[i] != null) {
		    cgeo = new TriangleStripArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_TRI_FAN_SET:
		if (mergedList[i] != null) {
		    cgeo = new TriangleFanArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_LINE_STRIP_SET:
		if (mergedList[i] != null) {
		    cgeo = new LineStripArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_QUAD_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedQuadArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedTriangleArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_POINT_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedPointArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_LINE_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedLineArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedTriangleStripArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedTriangleFanArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    case GeometryArrayRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
		if (mergedList[i] != null) {
		    cgeo = new IndexedLineStripArrayRetained();
		    curList = (ArrayList)mergedList[i];
		    cgeo.setCompiled(curList);
		    geometryList.add(cgeo);
		    cgeo.setSource(((SceneGraphObjectRetained)curList.get(0)).source);
		}
		if (separateList[i] != null) {
		    ArrayList glist = (ArrayList)separateList[i];
		    for (int k = 0; k < glist.size(); k++) {
			geometryList.add(glist.get(k));
		    }

		}
		break;
	    }
	}
		

    }


    Bounds getCollisionBounds(int childIndex) {
	return collisionBound;
    }
    

    int numGeometries(int childIndex) {
	ArrayList geo = (ArrayList) geometryInfo.get(childIndex);
	return geo.size();
    }


    Geometry getGeometry(int i, int childIndex) {
	ArrayList geoInfo = (ArrayList) geometryInfo.get(childIndex);
	return (Geometry)geoInfo.get(i);

	
    }

    Enumeration getAllGeometries(int childIndex) {
	ArrayList geoInfo = (ArrayList) geometryInfo.get(childIndex);
	Vector geomList = new Vector();
	
	for(int i=0; i<geoInfo.size(); i++) {
	    geomList.add(geoInfo.get(i));
	}
	
	return geomList.elements();
    }

    Bounds getBounds(int childIndex) {
        if(boundsAutoCompute) {
	    ArrayList glist = (ArrayList) geometryInfo.get(childIndex);

	    if(glist != null) {
		BoundingBox bbox = new BoundingBox((Bounds) null);
		for(int i=0; i<glist.size(); i++) {
		    Geometry g = (Geometry) glist.get(i);
		    if (g != null) {
			GeometryRetained geometry = (GeometryRetained)g.retained;
			if (geometry.geoType != GeometryRetained.GEO_TYPE_NONE) {
			    geometry.computeBoundingBox();
			    synchronized(geometry.geoBounds) {
				bbox.combine(geometry.geoBounds);
			    }
			}
		    }
		}

		return (Bounds) bbox;
		
	    } else {
		return null;
            }
	    
        } else {
            return super.getBounds();
        }
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
	ArrayList glist =  (ArrayList) geometryInfo.get(shape.id);	        
        
        // System.out.println("Shape3DCompileRetained.intersect() : ");
        if (dist == null) {
            // System.out.println("      no dist request ....");
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
