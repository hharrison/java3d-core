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
    ArrayList userLists = new ArrayList();

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
			  boolean multiScreen, int screen,
			  boolean ignoreVertexColors, 
			  int pass);

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

    abstract boolean intersect(PickShape pickShape, double dist[], Point3d iPnt);
    abstract boolean intersect(Bounds targetBound);
    abstract boolean intersect(Point3d[] pnts);
    abstract boolean intersect(Transform3D thisToOtherVworld, GeometryRetained geom);


    boolean intersect(Transform3D thisLocalToVworld, 
		      Transform3D otherLocalToVworld, GeometryRetained  geom) {
	Transform3D tg =  VirtualUniverse.mc.getTransform3D(null);
	tg.invert(otherLocalToVworld);
	tg.mul(thisLocalToVworld);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, tg);
	return intersect(tg, geom);
    }


    boolean intersect(Transform3D thisLocalToVworld, Bounds targetBound) {
	Bounds transBound = (Bounds) targetBound.clone();

	Transform3D tg =  VirtualUniverse.mc.getTransform3D(null);
	tg.invert(thisLocalToVworld);
	transBound.transform(tg);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, tg);
	return intersect(transBound);
    }


    boolean canBeInDisplayList(boolean alphaEditable) {

	return (VirtualUniverse.mc.isDisplayList) &&
	    !(this.isEditable || 
	      (!(this instanceof GeometryArrayRetained) && alphaEditable)||
	      (alphaEditable && ((((GeometryArrayRetained)this).vertexFormat&
				  GeometryArray.COLOR) != 0)) ||
	      (((((GeometryArrayRetained)this).vertexFormat & 
		 GeometryArray.BY_REFERENCE) != 0) && !VirtualUniverse.mc.buildDisplayListIfPossible));
    }

    void computeCentroid() {
	this.centroid.set(geoBounds.getCenter());
    }

    abstract int getClassType();

}
