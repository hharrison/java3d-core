/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
import java.util.ArrayList;

abstract class GeometryRetained extends NodeComponentRetained {

    static final int GEO_TYPE_NONE                        = -1;

    static final int GEO_TYPE_QUAD_SET                    = 1;
    static final int GEO_TYPE_TRI_SET                     = 2;
    static final int GEO_TYPE_POINT_SET                   = 3;
    static final int GEO_TYPE_LINE_SET                    = 4;
    static final int GEO_TYPE_TRI_STRIP_SET               = 5;
    static final int GEO_TYPE_TRI_FAN_SET                 = 6;
    static final int GEO_TYPE_LINE_STRIP_SET              = 7;

    static final int GEO_TYPE_INDEXED_QUAD_SET            = 8;
    static final int GEO_TYPE_INDEXED_TRI_SET             = 9;
    static final int GEO_TYPE_INDEXED_POINT_SET           = 10;
    static final int GEO_TYPE_INDEXED_LINE_SET            = 11;
    static final int GEO_TYPE_INDEXED_TRI_STRIP_SET       = 12;
    static final int GEO_TYPE_INDEXED_TRI_FAN_SET         = 13;
    static final int GEO_TYPE_INDEXED_LINE_STRIP_SET      = 14;

    static final int GEO_TYPE_RASTER                      = 15;
    static final int GEO_TYPE_TEXT3D                      = 16;
    static final int GEO_TYPE_COMPRESSED                  = 17;

    static final int GEO_TYPE_TOTAL                       = 17;
    static final int GEO_TYPE_GEOMETRYARRAY               = 14;

    BoundingBox geoBounds = new BoundingBox();

    // Indicates whether bounds need to be computed.
    // Checked when a user does addUser/removeUser and count goes from 0 to one
    // but geometry has not changed and there is no need to recompute
    boolean boundsDirty = true; // Changed while holding the geoBounds lock

    int computeGeoBounds = 0; // Changed while holding the geoBounds lock
 
    // The "type" of this object
    int geoType = GEO_TYPE_NONE;

    // The id used by the native code when building this object
    int nativeId = -1;

    // A mask that indicates that something has changed in this object
    int isDirty = 0xffff;


    // Geometry Lock (used only by GeometryArrayRetained and RasterRetained)
    GeometryLock geomLock = new GeometryLock();

    // Lock used for synchronization of live state
    Object liveStateLock = new Object();

    abstract void update();

    // A reference to the mirror copy of the geometry
    GeometryRetained mirrorGeometry = null;

    // indicates whether the geometry in editable
    boolean isEditable = true;

    // A list of Universes that this Geometry is referenced from
    ArrayList universeList = new ArrayList();

    // A list of ArrayLists which contain all the Shape3DRetained objects
    // refering to this geometry.  Each list corresponds to the universe
    // above.
    ArrayList<ArrayList<Shape3DRetained>> userLists = new ArrayList();

    // true if color not specified with alpha channel 
    boolean noAlpha = false;
    static final double EPSILON = 1.0e-6;

    Point3d centroid = new Point3d();
    boolean recompCentroid = true;
    // The cached value is evaluated by renderBin and used in determining
    // whether to put it in display list or not
    int cachedChangedFrequent = 0;
    
    static final int POINT_TYPE        = 1;
    static final int LINE_TYPE         = 2;
    static final int TRIANGLE_TYPE     = 3;
    static final int QUAD_TYPE         = 4;
    static final int RASTER_TYPE       = 5;    
    static final int TEXT3D_TYPE       = 6;    
    static final int COMPRESS_TYPE     = 7;    

    
    boolean isEquivalenceClass( GeometryRetained geometry ) {
	int t1 = getClassType();
	int t2 = geometry.getClassType();

	if (t1 == QUAD_TYPE) {
	    t1 = TRIANGLE_TYPE;
	}
	if (t2 == QUAD_TYPE) {
	    t2 =  TRIANGLE_TYPE;
	}
	return (t1 == t2);
    }

    void incrComputeGeoBounds() {
	synchronized(geoBounds) {
	    computeGeoBounds++;
	    // When it goes from zero to one, compute it ..
	    if (computeGeoBounds == 1 && source.isLive()) {
		computeBoundingBox();
	    }
	}
    }

    void decrComputeGeoBounds() {
	synchronized(geoBounds) {
	    computeGeoBounds--;
	}
    }


    // This adds a Shape3DRetained to the list of users of this geometry
    void addUser(Shape3DRetained s) {
	int index;
	ArrayList shapeList;
	    
	if (s.sourceNode.boundsAutoCompute) {
	    incrComputeGeoBounds();
	}
	
	// If static, no need to maintain a userlist
	if (this instanceof GeometryArrayRetained) {
	    if (((GeometryArrayRetained)this).isWriteStatic()) {
		return;
	    }
	}
	synchronized (universeList) {
	    if (universeList.contains(s.universe)) {
		index = universeList.indexOf(s.universe);
		shapeList = (ArrayList)userLists.get(index);
		shapeList.add(s);
	    } else {
		universeList.add(s.universe);
		shapeList = new ArrayList();
		shapeList.add(s);
		userLists.add(shapeList);
	    }
	}

    }

    // This adds a Shape3DRetained to the list of users of this geometry
    void removeUser(Shape3DRetained s) {
	int index;
	ArrayList shapeList;

	if (s.sourceNode.boundsAutoCompute) {
	    decrComputeGeoBounds();
	}
	
	if (this instanceof GeometryArrayRetained) {
	    if (((GeometryArrayRetained)this).isWriteStatic()) {
		return;
	    }
	}
	
	synchronized (universeList) {
	    index = universeList.indexOf(s.universe);
	    shapeList = (ArrayList)userLists.get(index);
	    shapeList.remove(shapeList.indexOf(s));
	    if (shapeList.size() == 0) {
		userLists.remove(index);
		universeList.remove(index);
	    }
	}

    }
    
    public void updateObject() {
	this.update();
    }

  
    abstract void computeBoundingBox();

    void setLive(boolean inBackgroundGroup, int refCount) {
	doSetLive(inBackgroundGroup,refCount);
	super.markAsLive();
    }

    /**
     * This setLive routine calls the superclass's method when reference
     * count is 1
     */  
    void doSetLive(boolean inBackgroundGroup, int refCount) {
        super.doSetLive(inBackgroundGroup, refCount);
	this.update();
	this.computeBoundingBox();
    }

    abstract void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale,
                          boolean updateAlpha, float alpha,
			  int screen, boolean ignoreVertexColors);

    /**
     * This method should return an int indicating the format of the vertices,
     * if any, stored in the geometry. Instances that can return a valid value
     * should override this method, otherwise it will be assumed that no
     * valid vertex components exist.
     * @return format of vertices in the GeometryRetained as specified by
     * GeometryArray, if appropriate to this instance.
     */
    int getVertexFormat() {
	return 0 ;
    }

    // Issue 199 -- Chien
    abstract boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                               GeometryRetained geom, int geomIndex);
    
    // Old stuff -- Chien
    //abstract boolean intersect(PickShape pickShape, PickInfo.IntersectionInfo iInfo, int flags, Point3d iPnt);   
    
    abstract boolean intersect(Bounds targetBound);
    abstract boolean intersect(Point3d[] pnts);
    abstract boolean intersect(Transform3D thisToOtherVworld, GeometryRetained geom);

    void storeInterestData(PickInfo pickInfo, int flags, GeometryRetained geom, int geomIndex,
			   int[] vtxIndexArr, Point3d iPnt, double dist) {

	PickInfo.IntersectionInfo iInfo = null;
	
	if((flags & PickInfo.CLOSEST_GEOM_INFO) != 0) {
	    PickInfo.IntersectionInfo iInfoArr[] = pickInfo.getIntersectionInfos();
	    if((iInfoArr == null) || (iInfoArr.length == 0)) {
		iInfo = pickInfo.createIntersectionInfo();
		pickInfo.insertIntersectionInfo(iInfo);		
	    }
	    else {
		assert(iInfoArr.length == 1);
		iInfo = iInfoArr[0];
	    }
	}
	else if((flags & PickInfo.ALL_GEOM_INFO) != 0) {
		iInfo = pickInfo.createIntersectionInfo();
		pickInfo.insertIntersectionInfo(iInfo);
	}
	else {
            assert(false);
	}
	// This only set the reference to geometry.source.
	iInfo.setGeometry((Geometry) geom.source);
	// The rest are by copy.
	iInfo.setGeometryIndex(geomIndex);
	iInfo.setDistance(dist);
	iInfo.setIntersectionPoint(iPnt);
	iInfo.setVertexIndices(vtxIndexArr);
    }

    boolean intersect(Transform3D thisLocalToVworld, 
		      Transform3D otherLocalToVworld, GeometryRetained  geom) {
	Transform3D t3d =  new Transform3D();
	t3d.invert(otherLocalToVworld);
	t3d.mul(thisLocalToVworld);
	return intersect(t3d, geom);
    }


    boolean intersect(Transform3D thisLocalToVworld, Bounds targetBound) {
	Bounds transBound = (Bounds) targetBound.clone();

	Transform3D t3d =  new Transform3D();
	t3d.invert(thisLocalToVworld);
	transBound.transform(t3d);
	return intersect(transBound);
    }


    // Return a flag indicating whether or not this Geometry object can be in
    // a display list.
    //
    // XXXX: Note that for IndexedGeometryArray objects, the original
    // vertex format is used in making this determination, even when it has
    // been unindexified. This should be fixed by using the vertex format of
    // the mirror geometry if there is one.
    boolean canBeInDisplayList(boolean alphaEditable) {
        // Check global flag to see whether we can build display lists
        if (!VirtualUniverse.mc.isDisplayList) {
            return false;
        }

        // Can't build display lists if geometry is frequently writable
        //
        // Issue 181 : to fix performance regression from 1.3.2, we will allow
        // editable geometry if the optimizeForSpace property is set false and
        // the cachedChangedFrequent flag is set; note this will basically
        // restore the 1.3.2 behavior, which isn't completely correct.
        // Eventually, we should fix the bug that is causing the
        // cachedChangedFrequent bit to be wrong; we can then remove the
        // erroneous dependency on optimizeForSpace.
        if (this.isEditable) {
            if (cachedChangedFrequent != 0) {
                return false;
            }

            // TODO: remove the following when cachedChangedFrequent is fixed
            // to correctly reflect the state
            if (!VirtualUniverse.mc.buildDisplayListIfPossible) {
                return false;
            }
        }

        if (this instanceof GeometryArrayRetained) {
            int vFormat = ((GeometryArrayRetained)this).vertexFormat;

            // If geometry has vertex attributes, check whether
            // vertex attributes are allowed in display lists
            if (((vFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) &&
                    !VirtualUniverse.mc.vertexAttrsInDisplayList) {
                return false;
            }

            // Can't build display lists if alpha is editable and
            // geometry array has colors
            if (alphaEditable && ((vFormat & GeometryArray.COLOR) != 0)) {
                return false;
            }

            // Only build DL for by-ref geometry when system property is set.
            // Exclude NIO buffers and use-coord-index-only
            if ((vFormat &  GeometryArray.BY_REFERENCE) != 0) {
                if (!VirtualUniverse.mc.buildDisplayListIfPossible) {
                    return false;
                }

                // XXXX: we could change this to allow display lists for
                // non-interleaved NIO buffers, but we would first need to
                // update the now-obsolete buildGAForBuffer method to handle
                // vertex attrs
                if ((vFormat & GeometryArray.USE_NIO_BUFFER) != 0) {
                    return false;
                }

                if ((vFormat & GeometryArray.USE_COORD_INDEX_ONLY) != 0) {
                    return false;
                }
            }

            return true;
        } else {
            // Can't build display lists for other kind of geometry
            // NOTE: This method is not called for any type of Geometry
            // other than GeometryArray, so we shouldn't even get here.
            return false;
        }
    }

    void computeCentroid() {
	this.centroid.set(geoBounds.getCenter());
    }

    abstract int getClassType();

}
