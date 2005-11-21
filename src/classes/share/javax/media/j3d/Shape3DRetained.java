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
import java.util.Enumeration;
import java.util.Vector;

/**
 * A shape leaf node consisting of geometry and appearance properties.
 */

class Shape3DRetained extends LeafRetained {

    static final int GEOMETRY_CHANGED		= 0x00001;
    static final int APPEARANCE_CHANGED 	= 0x00002;
    static final int COLLISION_CHANGED		= 0x00004;
    static final int BOUNDS_CHANGED		= 0x00008;    
    static final int APPEARANCEOVERRIDE_CHANGED	= 0x00010;
    static final int LAST_DEFINED_BIT        = 0x00010;

    
    // Target threads to be notified when light changes
    static final int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
                                     J3dThread.UPDATE_RENDER;

    /**
     * The appearance component of the shape node.
     */
    AppearanceRetained appearance = null;

    /**
     * The arraylist of geometry component of the shape node.
     */
    ArrayList geometryList = null;
    
    /**
     * A 2D storage of all geometry atoms associated with this shape node.  
     * There may be more than one geometry for a Shape3D node. 
     * Do not change the following private variables to public, its access need to synchronize
     * via mirrorShape3DLock.
     */

    // geomAtomArr should always be a 1 element array, unless S3D contains multiple Text3Ds.
    private GeometryAtom geomAtom = null;

    /**
     * To sychronize access of the mirrorShape3D's geomAtomArray*. 
     * A multiple read single write Lock to sychronize access into mirrorShape3D.
     * To prevent deadlock a call to read/write lock must end with a read/write unlock
     * respectively.
     */
    private MRSWLock mirrorShape3DLock = null;

    /**
     * The mirror Shape3DRetained nodes for this object.  There is one
     * mirror for each instance of this Shape3D node.  If it is not in
     * a SharedGroup, only index 0 is valid.
     * Do not change the following private variables to public, its access need to synchronize
     * via mirrorShape3DLock.
     */
    ArrayList mirrorShape3D = new ArrayList(1); 
    
    /**
     * This field is used for mirror Shape3D nodes accessing their
     * original nodes.  It is a NodeRetained because the original 
     * node may be a Shape3DRetained or a MorphRetained node.
     */
    NodeRetained sourceNode = null;

    /**
     * The hashkey for this Shape3DRetained mirror object
     */
    HashKey key = null;

    // This is true when this geometry is referenced in an IMM mode context
    boolean inImmCtx = false;

    // A bitmask to indicate when something has changed
    int isDirty = 0xffff;

    // The list of lights that are scoped to this node
    LightRetained[] lights =null;

    // The number of lights in the above array, may be less than lights.length
    int numlights = 0;

    // The list of fogs that are scoped to this node
    FogRetained[] fogs = null;

    // The number of fogs in the above array, may be less than fogs.length
    int numfogs = 0;

    // The list of modelClips that are scoped to this node
    ModelClipRetained[] modelClips = null;

    // The number of modelClips in the above array, may be less than modelClips.length
    int numModelClips = 0;

    // The list of alt app that are scoped to this node
    AlternateAppearanceRetained[] altApps = null;

    //The number of alt app in the above array, may be less than alt app.length
    int numAltApps = 0;

    /**
     * Reference to the  BranchGroup path of this mirror shape
     * This is used for picking and GeometryStructure only.
     */
    BranchGroupRetained branchGroupPath[];

    // cache value for picking in mirror shape. 
    // True if all the node of the path from this to root are all pickable
    boolean isPickable = true;

    // cache value for collidable in mirror shape. 
    // True if all the node of the path from this to root are all collidable
    boolean isCollidable = true;

    // closest switch parent
    SwitchRetained  closestSwitchParent = null;

    // the child index from the closest switch parent
    int closestSwitchIndex = -1;

    // Is this S3D visible ? The default is true.  
    boolean visible = true;

    // Whether the normal appearance is overrided by the alternate app
    boolean appearanceOverrideEnable = false;

    // AlternateAppearance retained that is applicable to this
    // mirror shape when the override flag is true
    AppearanceRetained otherAppearance = null;

    // geometry Bounds in local coordinate
    Bounds bounds = null;

    // geometry Bounds in virtual world coordinate
    BoundingBox vwcBounds = null;

    // collision Bounds in local coordinate
    Bounds collisionBound = null;

    // collision Bounds in virtual world coordinate
    Bounds collisionVwcBound = null;

    // a path of OrderedGroup, childrenId pairs which leads to this node
    OrderedPath orderedPath = null;

    // List of views that a mirror object is scoped to
    ArrayList viewList = null;

    int changedFrequent = 0;

    Shape3DRetained() {
	super();
        this.nodeType = NodeRetained.SHAPE;
	numlights = 0;
	numfogs = 0;
	numModelClips = 0;
	numAltApps = 0;
	localBounds = new BoundingBox((BoundingBox) null);

	mirrorShape3DLock = new MRSWLock();
	geometryList = new ArrayList(1);
	geometryList.add(null);
    }
    
    /**
     * Sets the collision bounds of a node.
     * @param bounds the bounding object for the node
     */
    void setCollisionBounds(Bounds bounds) {
        if (bounds == null) {
            this.collisionBound = null;
	} else {
	    this.collisionBound = (Bounds)bounds.clone();
	}
	
	if (source.isLive()) {
	    // Notify Geometry Structure to check for collision
	    J3dMessage message = VirtualUniverse.mc.getMessage();
	    message.type = J3dMessage.COLLISION_BOUND_CHANGED;
	    message.threads = J3dThread.UPDATE_TRANSFORM;
	    message.universe = universe;
	    message.args[0] = getGeomAtomsArray(mirrorShape3D);
	    // no need to clone collisionBound
	    message.args[1] = collisionBound;
	    VirtualUniverse.mc.processMessage(message);
	}
    } 

    Bounds getLocalBounds(Bounds bounds) {
	if(localBounds != null) { 
	    localBounds.set(bounds);
	}
	else {
	    localBounds = new BoundingBox(bounds);
	}
	return localBounds;
    }


    /**
     * Sets the geometric bounds of a node.
     * @param bounds the bounding object for the node
     */
    void setBounds(Bounds bounds) {
	super.setBounds(bounds);	
	
	if (source.isLive() && !boundsAutoCompute) { 
	    J3dMessage message = VirtualUniverse.mc.getMessage();
	    message.type = J3dMessage.REGION_BOUND_CHANGED;
            message.threads = J3dThread.UPDATE_TRANSFORM |
                              J3dThread.UPDATE_GEOMETRY |
                              J3dThread.UPDATE_RENDER;

	    message.universe = universe;
 	    message.args[0] = getGeomAtomsArray(mirrorShape3D);
	    // no need to clone localBounds
            message.args[1] = localBounds;
	    VirtualUniverse.mc.processMessage(message);
	}
    }
    
    /**
     * Gets the collision bounds of a node.
     * @return the node's bounding object
     */
    Bounds getCollisionBounds(int id) {
      return (collisionBound == null ?
	      null: (Bounds)collisionBound.clone());
    } 
	    
    /**
     * Appends the specified geometry component to this Shape3D
     * node's list of geometry components.
     * If there are existing geometry components in the list, the new
     * geometry component must be of the same equivalence class
     * (point, line, polygon, CompressedGeometry, Raster, Text3D) as
     * the others.
     * @param geometry the geometry component to be appended.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     *
     * @since Java 3D 1.2
     */
    void addGeometry(Geometry geometry) {
	int i;
	Shape3DRetained s;
	GeometryRetained newGeom = null;
	
	checkEquivalenceClass(geometry, -1);
	
	if(((Shape3D)this.source).isLive()) {
	    if (geometry != null) {
		
		newGeom = ((GeometryRetained)geometry.retained);	
                newGeom.setLive(inBackgroundGroup, refCount);

		geometryList.add(newGeom);
		
	    } else {
		geometryList.add(null);
		newGeom = null;
	    }
	    sendDataChangedMessage(newGeom);
	    
        } else {
	    if (geometry != null) {
                geometryList.add((GeometryRetained) geometry.retained);
	    } else {
	        geometryList.add(null);
	    }
	}

    }    

    /**
     * Replaces the geometry component at the specified index in this
     * Shape3D node's list of geometry components with the specified
     * geometry component.
     * If there are existing geometry components in the list (besides
     * the one being replaced), the new geometry component must be of
     * the same equivalence class (point, line, polygon, CompressedGeometry,
     * Raster, Text3D) as the others.
     * @param geometry the geometry component to be stored at the
     * specified index.
     * @param index the index of the geometry component to be replaced.
     * @exception IllegalArgumentException if the new geometry
     * component is not of of the same equivalence class as the
     * existing geometry components.
     *
     * @since Java 3D 1.2
     */
    void setGeometry(Geometry geometry, int index) {
	int i;
	Shape3DRetained mShape;
	GeometryRetained newGeom = null;
	GeometryRetained oldGeom = null;

	checkEquivalenceClass(geometry, index);
	
	if (((Shape3D)this.source).isLive()) {
	    
	    oldGeom = (GeometryRetained) (geometryList.get(index));
	    if (oldGeom != null) {
		oldGeom.clearLive(refCount);
		for (i=0; i<mirrorShape3D.size(); i++) {
		    mShape = (Shape3DRetained)mirrorShape3D.get(i);
		    oldGeom.removeUser(mShape);
		}
		oldGeom.decRefCnt();
	    }

            if (geometry != null) {
		newGeom = (GeometryRetained) geometry.retained;
		newGeom.incRefCnt();
                newGeom.setLive(inBackgroundGroup, refCount);
		geometryList.set(index, newGeom);	
		sendDataChangedMessage(newGeom);
	    } else {
		geometryList.set(index, null);
		sendDataChangedMessage(null);
	    }
	    
        } else {

	    oldGeom = (GeometryRetained) (geometryList.get(index));
	    if (oldGeom != null) {
		oldGeom.decRefCnt();
	    }
	    if (geometry != null) {
                geometryList.set(index,(GeometryRetained) geometry.retained);
		((GeometryRetained)geometry.retained).incRefCnt();
	    } else {
	        geometryList.set(index,null);
	    }
	}
    }

    /**
     * Inserts the specified geometry component into this Shape3D
     * node's list of geometry components at the specified index.
     * If there are existing geometry components in the list, the new
     * geometry component must be of the same equivalence class
     * (point, line, polygon, CompressedGeometry, Raster, Text3D) as
     * the others.
     * @param geometry the geometry component to be inserted at the
     * specified index.
     * @param index the index at which the geometry component is inserted.
     *
     * @since Java 3D 1.2
     */
    void insertGeometry(Geometry geometry, int index) {
	int i;
	Shape3DRetained mShape;
	GeometryRetained newGeom = null;
	GeometryRetained oldGeom = null;
	
	checkEquivalenceClass(geometry, -1);
	
	if (((Shape3D)this.source).isLive()) {
	    
	    if (geometry != null) {
		// Note : The order of the statements in important. Want ArrayList class to do index bounds
		// check before creating internal object.
		newGeom = (GeometryRetained) geometry.retained;
		newGeom.incRefCnt();
		geometryList.add(index, newGeom);		
		newGeom.setLive(inBackgroundGroup, refCount);
		sendDataChangedMessage(newGeom);
	    } else {
		geometryList.add(index, null);
		sendDataChangedMessage(null);
	    }

        } else {
	    
	    if (geometry != null) {
		geometryList.add(index,(GeometryRetained) geometry.retained);
		((GeometryRetained)geometry.retained).incRefCnt();
	    } else {
		geometryList.add(index,null);
	    }
	}	
    }

    /**
     * Removes the geometry component at the specified index from
     * this Shape3D node's list of geometry components.
     * @param index the index of the geometry component to be removed.
     *
     * @since Java 3D 1.2
     */
    void removeGeometry(int index) {
	int i;
	Shape3DRetained mShape;
	GeometryRetained oldGeom = null;
	
	if (((Shape3D)this.source).isLive()) {
	    
	    oldGeom = (GeometryRetained) (geometryList.get(index));
	    if (oldGeom != null) {
		oldGeom.clearLive(refCount);
		oldGeom.decRefCnt();
		for (i=0; i<mirrorShape3D.size(); i++) {
		    mShape = (Shape3DRetained)mirrorShape3D.get(i);
		    oldGeom.removeUser(mShape);
		    
		}
	    }
	    
	    geometryList.remove(index);		    
	    sendDataChangedMessage(null);
	    
	} else {
	    oldGeom = (GeometryRetained) (geometryList.get(index));
	    if (oldGeom != null) {
		oldGeom.decRefCnt();
	    }
	    geometryList.remove(index);	
	}
	

	
    }
    
    /**
     * Retrieves the geometry component of this Shape3D node.
     * @return the geometry component of this shape node
     *
     * @since Java 3D 1.2
     */
    Geometry getGeometry(int index, int id) {
	GeometryRetained ga = (GeometryRetained) geometryList.get(index);
        return (ga == null ? null : (Geometry)ga.source);
    }
    
    
    /**
     * Returns an enumeration of this Shape3D node's list of geometry
     * components.
     * @return an Enumeration object containing all geometry components in
     * this Shape3D node's list of geometry components.
     *
     * @since Java 3D 1.2
     */
    Enumeration getAllGeometries(int id) {
	GeometryRetained ga = null;
	Vector geomList = new Vector(geometryList.size());
	
	for(int i=0; i<geometryList.size(); i++) {
	    ga = (GeometryRetained) geometryList.get(i);
	    if(ga != null) 
		geomList.add((Geometry)ga.source);
	    else
		geomList.add(null);
	}
	
	return geomList.elements();
    }

    /**
     * Returns the number of geometry components in this Shape3D node's
     * list of geometry components.
     * @return the number of geometry components in this Shape3D node's
     * list of geometry components.
     *
     * @since Java 3D 1.2
     */
    int numGeometries(int id) {

	return geometryList.size();
    }
	
    /**
     * Sets the appearance component of this Shape3D node.
     * @param appearance the new apearance component for this shape node
     */
    void setAppearance(Appearance newAppearance) {

	Shape3DRetained s;
	boolean visibleIsDirty = false;
	
	if (((Shape3D)this.source).isLive()) {	    
	    if (appearance != null) {
	        appearance.clearLive(refCount);
		for (int i=0; i<mirrorShape3D.size(); i++) {
		    s = (Shape3DRetained)mirrorShape3D.get(i);
		    appearance.removeAMirrorUser(s);
		}	   
	    }
	    
	    if (newAppearance != null) {
	       ((AppearanceRetained)newAppearance.retained).setLive(inBackgroundGroup, refCount);
		appearance = ((AppearanceRetained)newAppearance.retained);
		for (int i=0; i<mirrorShape3D.size(); i++) {
		    s = (Shape3DRetained)mirrorShape3D.get(i);
		    appearance.addAMirrorUser(s);
		}
		if((appearance.renderingAttributes != null) &&
		   (visible !=  appearance.renderingAttributes.visible)) {
		    visible = appearance.renderingAttributes.visible;
		    visibleIsDirty = true;
		}
	    }
	    else {
		if(visible == false) {
		    visible = true;
		    visibleIsDirty = true;
	       }
	    }
	    int size = 0;
	    if (visibleIsDirty)
		size = 2;
	    else
		size = 1;
	    J3dMessage[] createMessage = new J3dMessage[size];
	    // Send a message
	    createMessage[0] = VirtualUniverse.mc.getMessage();
	    createMessage[0].threads = targetThreads;
	    createMessage[0].type = J3dMessage.SHAPE3D_CHANGED;
	    createMessage[0].universe = universe;
	    createMessage[0].args[0] = this;
	    createMessage[0].args[1]= new Integer(APPEARANCE_CHANGED);
	    Shape3DRetained[] s3dArr = new Shape3DRetained[mirrorShape3D.size()];
	    mirrorShape3D.toArray(s3dArr);
	    createMessage[0].args[2] = s3dArr;
	    Object[] obj = new Object[2];
	    if (newAppearance == null) {
		obj[0] = null;
	    }
	    else {
		obj[0]  = appearance.mirror;
	    }
	    obj[1] = new Integer(changedFrequent);
	    createMessage[0].args[3] = obj;
	    createMessage[0].args[4] = getGeomAtomsArray(mirrorShape3D);
	    if(visibleIsDirty) {
		createMessage[1] = VirtualUniverse.mc.getMessage();
		createMessage[1].threads = J3dThread.UPDATE_GEOMETRY;	    
		createMessage[1].type = J3dMessage.SHAPE3D_CHANGED;
		createMessage[1].universe = universe;
		createMessage[1].args[0] = this;
		createMessage[1].args[1]= new Integer(APPEARANCE_CHANGED);
		createMessage[1].args[2]= visible?Boolean.TRUE:Boolean.FALSE;
		createMessage[1].args[3]= createMessage[0].args[4];
	    }
	    VirtualUniverse.mc.processMessage(createMessage);
	
        }
	else { // not live.
	    if (newAppearance == null) {
		appearance = null;
	    } else {
		appearance = (AppearanceRetained) newAppearance.retained;    
	    }
	}
    }
    
    /**
     * Retrieves the shape node's appearance component.
     * @return the shape node's appearance
     */
    Appearance getAppearance() {
        return (appearance == null ? null: (Appearance) appearance.source);
    }

    void setAppearanceOverrideEnable(boolean flag) {
	if (((Shape3D)this.source).isLive()) {
	    
	    // Send a message
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = targetThreads;
	    createMessage.type = J3dMessage.SHAPE3D_CHANGED;
	    createMessage.universe = universe;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(APPEARANCEOVERRIDE_CHANGED);
	    Shape3DRetained[] s3dArr = new Shape3DRetained[mirrorShape3D.size()];
	    mirrorShape3D.toArray(s3dArr);
	    createMessage.args[2] = s3dArr;
	    Object[] obj = new Object[2];
	    if (flag) {
		obj[0] = Boolean.TRUE;
	    }
	    else {
		obj[0]  = Boolean.FALSE;
	    }
	    obj[1] = new Integer(changedFrequent);
	    createMessage.args[3] = obj;
	    createMessage.args[4] = getGeomAtomsArray(mirrorShape3D);

	    VirtualUniverse.mc.processMessage(createMessage);
	}
	appearanceOverrideEnable = flag;
    }

    boolean getAppearanceOverrideEnable() {
	return appearanceOverrideEnable;
    }
    
    boolean intersect(PickInfo pickInfo, PickShape pickShape, int flags ) {
          
        Transform3D localToVworld = pickInfo.getLocalToVWorldRef();
        
        // Support OrientedShape3D here.
	// Note - BugId : 4363899 - APIs issue : OrientedShape3D's intersect needs view
	//                          info. temp. fix use the primary view.
	if (this instanceof OrientedShape3DRetained) {
	    Transform3D orientedTransform = ((OrientedShape3DRetained)this).
		getOrientedTransform(getPrimaryViewIdx());
	    localToVworld.mul(orientedTransform);
	}
        
 	Transform3D t3d = new Transform3D();
	t3d.invert(localToVworld);
	PickShape newPS = pickShape.transform(t3d);

	// Note: For optimization - Should do a geobounds check of
	// each geometry first. But this doesn't work for
	// OrientedShape3D case...
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
                    if (geometry.intersect(newPS, null, 0, null, null, 0)) {
                        return true;
                    }
                }
            }
        } else {
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
                    //if (geometry.intersect(newPS, intersectionInfo, flags, iPnt)) {
                    if(geometry.intersect(newPS, pickInfo, flags, iPnt, geometry, i)) {
                        
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
    
    
    /**
     * Check if the geometry component of this shape node under path
     * intersects with the pickShape.
     * This is an expensive method. It should only be called if and only
     * if the path's bound intersects pickShape.  
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
        //System.out.println("Shape3DRetained.intersect() : ");
        if (dist == null) {
            //System.out.println("      no dist request ....");
            return intersect(pickInfo, pickShape, 0);
        }
        
        flags = PickInfo.CLOSEST_DISTANCE;
        if (intersect(pickInfo, pickShape, flags)) {
            dist[0] = pickInfo.getClosestDistance();
            return true;
        }
        
        return false;
          
      }

    /**
     * This sets the immedate mode context flag
     */  
    void setInImmCtx(boolean inCtx) {
        inImmCtx = inCtx;
    }

    /**
     * This gets the immedate mode context flag
     */  
    boolean getInImmCtx() {
        return (inImmCtx);
    }

    /**
     * This updates the mirror shape to reflect the state of the
     * real shape3d.
     */
    private void initMirrorShape3D(SetLiveState s, Shape3DRetained ms, int index) {

	// New 1.2.1 code
	
        ms.inBackgroundGroup = inBackgroundGroup;
        ms.geometryBackground = geometryBackground;
	ms.source = source;
	ms.universe = universe;
	// Has to be false. We have a instance of mirror for every link to the shape3d.
	ms.inSharedGroup = false;
	ms.locale = locale;
	ms.parent = parent;

	// New 1.3.2
	// Used when user supplied their own bounds for transparency sorting
	// GeometryAtom uses this to change how it computes the centroid
	ms.boundsAutoCompute = boundsAutoCompute;
	ms.localBounds = localBounds;
	// End new 1.3.2

        OrderedPath op = (OrderedPath)s.orderedPaths.get(index);
        if (op.pathElements.size() == 0) {
            ms.orderedPath = null;
        } else {
            ms.orderedPath = op;
/*
            System.out.println("initMirrorShape3D ms.orderedPath ");
            ms.orderedPath.printPath();
*/
        }

	// all mirror shapes point to the same transformGroupRetained
	// for the static transform
	ms.staticTransform = staticTransform;


	ms.appearanceOverrideEnable = appearanceOverrideEnable;
	
	ms.geometryList = geometryList;
	
	// Assign the parent of this mirror shape node
	ms.sourceNode = this;

        if (this instanceof OrientedShape3DRetained) {
            OrientedShape3DRetained os = (OrientedShape3DRetained)this;
            OrientedShape3DRetained oms = (OrientedShape3DRetained)ms;
            oms.initAlignmentMode(os.mode);
            oms.initAlignmentAxis(os.axis);
            oms.initRotationPoint(os.rotationPoint);
	    oms.initConstantScaleEnable(os.constantScale);
	    oms.initScale(os.scaleFactor);
        }
	
    }

    void updateImmediateMirrorObject(Object[] objs) {
	int component = ((Integer)objs[1]).intValue();
	GeometryArrayRetained ga;
	
	Shape3DRetained[] msArr = (Shape3DRetained[]) objs[2];
	int i, j;
	if ((component & APPEARANCE_CHANGED) != 0) {
	    Object[] arg = (Object[])objs[3];
	    int val = ((Integer)arg[1]).intValue();
	    for ( i = msArr.length-1; i >=0; i--) {
		msArr[i].appearance = (AppearanceRetained)arg[0];
		msArr[i].changedFrequent = val;
	    }
	}
	if ((component & APPEARANCEOVERRIDE_CHANGED) != 0) {
	    Object[] arg = (Object[])objs[3];
	    int val = ((Integer)arg[1]).intValue();
	    for ( i = msArr.length-1; i >=0; i--) {
		msArr[i].appearanceOverrideEnable = ((Boolean)arg[0]).booleanValue();
		msArr[i].changedFrequent = val;
	    }
	}
    }

    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    
    Bounds getBounds() {

        if(boundsAutoCompute) {
	    // System.out.println("getBounds ---- localBounds is " + localBounds);
	    

	    if(geometryList != null) {
		BoundingBox bbox = new BoundingBox((Bounds) null);
		GeometryRetained geometry;    
		for(int i=0; i<geometryList.size(); i++) {
		    geometry = (GeometryRetained) geometryList.get(i);
		    if ((geometry != null) && 
			(geometry.geoType != GeometryRetained.GEO_TYPE_NONE)) {
			geometry.computeBoundingBox();
			synchronized(geometry.geoBounds) {
			    bbox.combine(geometry.geoBounds);
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
    
    Bounds getEffectiveBounds() {
        if(boundsAutoCompute) {
	    return getBounds();
	}
	else {
	    return super.getEffectiveBounds();
	}
    }
    
    
    /**
     * ONLY needed for SHAPE, MORPH, and LINK node type.
     * Compute the combine bounds of bounds and its localBounds.
     */
    void computeCombineBounds(Bounds bounds) {
	
	if(boundsAutoCompute) {
	    
	    if(geometryList != null) {
		GeometryRetained geometry;    
		BoundingBox bbox = null;
		
		if (staticTransform != null) {
		    bbox = new BoundingBox((BoundingBox) null);
		}
		
		for(int i=0; i<geometryList.size(); i++) {
		    geometry = (GeometryRetained) geometryList.get(i);
		    if ((geometry != null) &&
			(geometry.geoType != GeometryRetained.GEO_TYPE_NONE)) {
			geometry.computeBoundingBox();
			// Should this be lock too ? ( MT safe  ? )
			synchronized(geometry.geoBounds) {
			    if (staticTransform != null) {
			        bbox.set(geometry.geoBounds);
				bbox.transform(staticTransform.transform);
				bounds.combine((Bounds)bbox);
			    } else {
			        bounds.combine((Bounds)geometry.geoBounds);
			    }
			}
		    }
		}
	    }
	    
	} else {
	    
	    // Should this be lock too ? ( MT safe  ? )
	    synchronized(localBounds) {
		bounds.combine((Bounds) localBounds);
	    }
	}
    } 

  /**
   * assign a name to this node when it is made live.
   */

    void setLive(SetLiveState s) {
	doSetLive(s);
	markAsLive();
    }
    
    void doSetLive(SetLiveState s) {
	// System.out.println("S3DRetained : setLive " + s);
	Shape3DRetained shape;
	GeometryRetained geometry;
	int i, j, k, gaCnt;
	ArrayList msList = new ArrayList();
	
	super.doSetLive(s);
	
	nodeId = universe.getNodeId();

	
	if (inSharedGroup) {
	    for (i=0; i<s.keys.length; i++) {
                if (this instanceof OrientedShape3DRetained) {
                    shape = new OrientedShape3DRetained();
                } else {
                    shape = new Shape3DRetained();
                }
		shape.key = s.keys[i];
		shape.localToVworld = new Transform3D[1][];
		shape.localToVworldIndex = new int[1][];

		j = s.keys[i].equals(localToVworldKeys, 0,
				     localToVworldKeys.length);
		/*
		    System.out.print("s.keys[i] = "+s.keys[i]+" j = "+j);
		    if(j < 0) {
		    System.out.println("Shape3dRetained : Can't find hashKey"); 
		    }
		*/
		shape.localToVworld[0] = localToVworld[j];
		shape.localToVworldIndex[0] = localToVworldIndex[j];
		shape.branchGroupPath = (BranchGroupRetained []) branchGroupPaths.get(j);
		shape.isPickable = s.pickable[i];
		shape.isCollidable = s.collidable[i];
		
		initMirrorShape3D(s, shape, j);

                if (s.switchTargets != null &&
                        s.switchTargets[i] != null) {
		    s.switchTargets[i].addNode(shape, Targets.GEO_TARGETS);
                    shape.closestSwitchParent = s.closestSwitchParents[i];
                    shape.closestSwitchIndex = s.closestSwitchIndices[i];
                }
        	shape.switchState = (SwitchState)s.switchStates.get(j);


		// Add any scoped lights to the mirror shape
		if (s.lights != null) {
		    ArrayList l = (ArrayList)s.lights.get(j);
		    if (l != null) {
			for (int m = 0; m < l.size(); m++) {
			    shape.addLight((LightRetained)l.get(m));
			}
		    }
		}
		
		// Add any scoped fog
		if (s.fogs != null) {
		    ArrayList l = (ArrayList)s.fogs.get(j);
		    if (l != null) {
			for (int m = 0; m < l.size(); m++) {
			    shape.addFog((FogRetained)l.get(m));
			}
		    }
		}

		// Add any scoped modelClip
		if (s.modelClips != null) {
		    ArrayList l = (ArrayList)s.modelClips.get(j);
		    if (l != null) {
			for (int m = 0; m < l.size(); m++) {
			    shape.addModelClip((ModelClipRetained)l.get(m));
			}
		    }
		}
		    
		// Add any scoped alt app
		if (s.altAppearances != null) {
		    ArrayList l = (ArrayList)s.altAppearances.get(j);
		    if (l != null) {
			for (int m = 0; m < l.size(); m++) {
			    shape.addAltApp((AlternateAppearanceRetained)l.get(m));
			}
		    }
		}
		synchronized(mirrorShape3D) {
		    mirrorShape3D.add(j,shape);
		}

		msList.add(shape);
		if (s.viewLists != null) {
		    shape.viewList = (ArrayList)s.viewLists.get(j);
		} else {
		    shape.viewList = null;
		}
	    }
	} else {
            if (this instanceof OrientedShape3DRetained) {
                shape = new OrientedShape3DRetained();
            } else {
                shape = new Shape3DRetained();
            }

	    shape.localToVworld = new Transform3D[1][];
	    shape.localToVworldIndex = new int[1][];
	    shape.localToVworld[0] = localToVworld[0];
	    shape.localToVworldIndex[0] = localToVworldIndex[0];
	    shape.branchGroupPath = (BranchGroupRetained []) branchGroupPaths.get(0);
	    shape.isPickable = s.pickable[0];
	    shape.isCollidable = s.collidable[0];
	    initMirrorShape3D(s, shape, 0);
	    
	    // Add any scoped lights to the mirror shape
	    if (s.lights != null) {
		ArrayList l = (ArrayList)s.lights.get(0);
		for (i = 0; i < l.size(); i++) {
		    shape.addLight((LightRetained)l.get(i));
		}
	    }
	    
	    // Add any scoped fog
	    if (s.fogs != null) {
		ArrayList l = (ArrayList)s.fogs.get(0);
		for (i = 0; i < l.size(); i++) {
		    shape.addFog((FogRetained)l.get(i));
		}
	    }
	    
	    // Add any scoped modelClip
	    if (s.modelClips != null) {
		ArrayList l = (ArrayList)s.modelClips.get(0);
		for (i = 0; i < l.size(); i++) {
		    shape.addModelClip((ModelClipRetained)l.get(i));
		}

	    }
	    
	    // Add any scoped alt app
	    if (s.altAppearances != null) {
		ArrayList l = (ArrayList)s.altAppearances.get(0);
		for (i = 0; i < l.size(); i++) {
		    shape.addAltApp((AlternateAppearanceRetained)l.get(i));
		}
	    }
	    synchronized(mirrorShape3D) {
		mirrorShape3D.add(shape);
	    }

	    msList.add(shape);
	    if (s.viewLists != null)
		shape.viewList = (ArrayList)s.viewLists.get(0);
	    else
		shape.viewList = null;
	    
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
		s.switchTargets[0].addNode(shape, Targets.GEO_TARGETS);
                shape.closestSwitchParent = s.closestSwitchParents[0];
                shape.closestSwitchIndex = s.closestSwitchIndices[0];
            }
            shape.switchState = (SwitchState)s.switchStates.get(0);
	}

	for (k = 0; k < msList.size(); k++) {
	    Shape3DRetained sh = (Shape3DRetained)msList.get(k);

	    if (appearance != null) {
		synchronized(appearance.liveStateLock) {
		    if (k == 0) { // Do only first time 
			appearance.setLive(inBackgroundGroup, s.refCount);
			appearance.initMirrorObject();
			if (appearance.renderingAttributes != null)
			    visible = appearance.renderingAttributes.visible;
		    }
		    sh.appearance = (AppearanceRetained)appearance.mirror;
		    appearance.addAMirrorUser(sh);

		}
	    }
	    else {
		sh.appearance = null;
	    }

	    if (geometryList != null) {
		for(gaCnt=0; gaCnt<geometryList.size(); gaCnt++) {
		    geometry = (GeometryRetained) geometryList.get(gaCnt);
		    if(geometry != null) {
			synchronized(geometry.liveStateLock) {
			    if (k == 0) { // Do only first time 
				geometry.setLive(inBackgroundGroup, s.refCount);
			    }
			    geometry.addUser(sh);
			}
		    }
		}

	    }

	    // after the geometry has been setLived and bounds computed
	    if (k== 0 && boundsAutoCompute) { // Do only once 
		// user may call setBounds with a bounds other than boundingBox
		if (! (localBounds instanceof BoundingBox)) {
		    localBounds = new BoundingBox((BoundingBox) null);
		}
		getCombineBounds((BoundingBox)localBounds);
		    
	    }
	    // Assign GAtom and set the bounds if we are not using switch
	    initializeGAtom(sh);

            GeometryAtom ga = getGeomAtom(sh);
		
	    // Add the geometry atom for this shape to the nodeList
	    s.nodeList.add(ga);
		
            if (s.transformTargets != null &&
            		s.transformTargets[k] != null) {
		// Add the geometry atom for this shape to the transformTargets

		s.transformTargets[k].addNode(ga, Targets.GEO_TARGETS);
	    }
	}
	
	s.notifyThreads |= (J3dThread.UPDATE_GEOMETRY | 
			    J3dThread.UPDATE_TRANSFORM |
			    J3dThread.UPDATE_RENDER |
			    J3dThread.UPDATE_RENDERING_ENVIRONMENT);

    }
 
    /**
     * This clears all references in a mirror shape
     */
    // This is call in RenderingEnvironmentStructure.removeNode() because that is the
    // last point that will reference this ms.
    // called on the mirror shape ..
    void clearMirrorShape() {
	int i;

	source = null;
	sourceNode = null;
	parent = null;

	if (otherAppearance != null) {
	    otherAppearance.sgApp.removeAMirrorUser(this);
	    otherAppearance = null;
        }

	appearance = null;

	branchGroupPath = null;
	isPickable = true;
	isCollidable = true;
	branchGroupPath = null;
	// No locking needed. Owner, s3dR, has already been destory.
	// DO NOT clear geometryList, ie. geometryList.clear().
	// It is referred by the source s3DRetained.
	 geometryList = null;

	// Clear the mirror scoping info
	// Remove all the fogs
	for (i = 0; i <  numfogs; i++)
	     fogs[i] = null;
	 numfogs = 0;
	    
	// Remove all the modelClips
	for (i = 0; i <  numModelClips; i++)
	     modelClips[i] = null;
	 numModelClips = 0;
	    
	// Remove all the lights
	for (i = 0; i < numlights; i++)
	     lights[i] = null;
	 numlights = 0;
	    
	// Remove all the al app
	for (i = 0; i <  numAltApps; i++)
	     altApps[i] = null;
	numAltApps = 0;
	
	viewList = null;
	
    }

    /**
     * assign a name to this node when it is made live.
     */
    void clearLive(SetLiveState s) {

	//System.out.println("S3DRetained : clearLive " + s);

	int i, j, gaCnt;
	Shape3DRetained shape;
	GeometryRetained geometry;
	Object[] shapes;
	ArrayList msList = new ArrayList();
	
	super.clearLive(s);
	

	
	if (inSharedGroup) {
	    synchronized(mirrorShape3D) {
		shapes = mirrorShape3D.toArray();
		for (i=0; i<s.keys.length; i++) {
		    for (j=0; j<shapes.length; j++) {
			shape = (Shape3DRetained)shapes[j];
			if (shape.key.equals(s.keys[i])) {
			    mirrorShape3D.remove(mirrorShape3D.indexOf(shape));
            		    if (s.switchTargets != null &&
                        		s.switchTargets[i] != null) {
                		s.switchTargets[i].addNode(
						shape, Targets.GEO_TARGETS);
			    }
			    msList.add(shape);
                            GeometryAtom ga = getGeomAtom(shape);

			    // Add the geometry atom for this shape to the nodeList
			    s.nodeList.add(ga);
            		    if (s.transformTargets != null &&
                        		s.transformTargets[i] != null) {
                                s.transformTargets[i].addNode(ga, Targets.GEO_TARGETS);
                            }
			}
		    }
		}
	    }
	} else {
	    // Only entry 0 is valid
	    shape = (Shape3DRetained)mirrorShape3D.get(0);
	    synchronized(mirrorShape3D) {
		mirrorShape3D.remove(0);
	    }

            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(shape, Targets.GEO_TARGETS);
            }


	    msList.add(shape);

            GeometryAtom ga = getGeomAtom(shape);
	    
	    // Add the geometry atom for this shape to the nodeList
	    s.nodeList.add(ga);
            if (s.transformTargets != null &&
		s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ga, Targets.GEO_TARGETS);
            }
	}


	for (int k = 0; k < msList.size(); k++) {
	    Shape3DRetained sh = (Shape3DRetained)msList.get(k); 
	    if (appearance != null) {
		synchronized(appearance.liveStateLock) {
		    if (k == 0) {
			appearance.clearLive(s.refCount);
		    }
		    appearance.removeAMirrorUser(sh);
		}
	    }
	    if (geometryList != null) {
		for(gaCnt=0; gaCnt<geometryList.size(); gaCnt++) {
		    geometry = (GeometryRetained) geometryList.get(gaCnt);
		    if(geometry != null) {
			synchronized(geometry.liveStateLock) {
			    if (k == 0) {
				geometry.clearLive(s.refCount);
			    }
			    geometry.removeUser(sh);
			}
		    }
		}
	    }
	}

	s.notifyThreads |= (J3dThread.UPDATE_GEOMETRY |
			    J3dThread.UPDATE_TRANSFORM |
			    // This is used to clear the scope info
			    // of all the mirror shapes
			    J3dThread.UPDATE_RENDERING_ENVIRONMENT | 
			    J3dThread.UPDATE_RENDER);

	if (!source.isLive()) {
	    // Clear the mirror scoping info
	    // Remove all the fogs
	    for (i = 0; i < numfogs; i++)
		fogs[i] = null;
	    numfogs = 0;
	    
	    // Remove all the modelClips
	    for (i = 0; i < numModelClips; i++)
		modelClips[i] = null;
	    numModelClips = 0;
	    
	    // Remove all the lights
	    for (i = 0; i < numlights; i++)
		lights[i] = null;
	    numlights = 0;
	    
	    // Remove all the al app
	    for (i = 0; i < numAltApps; i++)
		altApps[i] = null;
	    numAltApps = 0;
	}
    }

    boolean isStatic() {
	if (source.getCapability(Shape3D.ALLOW_APPEARANCE_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_GEOMETRY_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE)) {
	    return false;
	} else {
	    return true;
	}
    }

    boolean staticXformCanBeApplied() {

	// static xform can be applied if
	// . shape is not pickable or collidable
	// . geometry is not being shared by more than one shape nodes
	// . geometry will be put in display list
	// . geometry is not readable

        // no static xform if shape is pickable or collidable because
	// otherwise the static xform will have to be applied to the
	// currentLocalToVworld in the intersect test, it will then
	// be more costly and really beat the purpose of eliminating
	// the static transform group
        if (isPickable || isCollidable ||
	    	source.getCapability(Shape3D.ALLOW_PICKABLE_WRITE) ||
	    	source.getCapability(Shape3D.ALLOW_COLLIDABLE_WRITE)) {
	    return false;
	}

	if (appearance != null &&
	    (appearance.transparencyAttributes != null && appearance.transparencyAttributes.transparencyMode != TransparencyAttributes.NONE))
	    return false;
	
	GeometryRetained geo;
	boolean alphaEditable;

	for (int i=0; i<geometryList.size(); i++) {
	    geo = (GeometryRetained) geometryList.get(i);
	    if (geo != null) {
		if (geo.refCnt > 1) {
		    return false;
		}
		alphaEditable = isAlphaEditable(geo);
		if (geo instanceof GeometryArrayRetained) {
		    geo.isEditable = !((GeometryArrayRetained)geo).isWriteStatic();

		    // TODO: for now if vertex data can be returned, then
		    // don't apply static transform
		    if (geo.source.getCapability(
				GeometryArray.ALLOW_COORDINATE_READ) ||
			geo.source.getCapability(
				GeometryArray.ALLOW_NORMAL_READ))
			return false;

		}

		if (!geo.canBeInDisplayList(alphaEditable)) {
		    return false;
		}
	    }
 	}
	return true;
    }


    void compile(CompileState compState) {
	AppearanceRetained newApp;

	super.compile(compState);

	if (isStatic() && staticXformCanBeApplied()) {
	    mergeFlag = SceneGraphObjectRetained.MERGE;
            if (J3dDebug.devPhase && J3dDebug.debug) {
	        compState.numShapesWStaticTG++;
	    }
	} else 
	{
	    mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
	    compState.keepTG = true;
	}

        if (J3dDebug.devPhase && J3dDebug.debug) {
	    compState.numShapes++;
	}

	if (appearance != null) {
	    appearance.compile(compState);
	    // Non-static apperanace can still be compiled, since in compile
	    // state we will be grouping all shapes that have same appearance
	    // so, when the appearance changes, all the shapes will be affected
	    // For non-static appearances, we don't get an equivalent appearance
	    // from the compile state
	    if (appearance.isStatic()) {
		newApp = compState.getAppearance(appearance);
		appearance = newApp;
	    }
	}

	for (int i = 0; i < geometryList.size(); i++) {
	    GeometryRetained geo = (GeometryRetained)geometryList.get(i);
	    if (geo != null)
		geo.compile(compState);
	}	
	
    }

    void merge(CompileState compState) {
	
	
	if (mergeFlag == SceneGraphObjectRetained.DONT_MERGE) {

	    // no need to save the staticTransform here

	    TransformGroupRetained saveStaticTransform = 
					compState.staticTransform;
	    compState.staticTransform = null;
	    super.merge(compState);
	    compState.staticTransform = saveStaticTransform;
        } else {
	    super.merge(compState);
	}

	if (shapeIsMergeable(compState)) {
	    compState.addShape(this);
	}
    }


    boolean shapeIsMergeable(CompileState  compState) {
	boolean mergeable = true;
	AppearanceRetained newApp;
	int i;
	    
	GeometryRetained geometry = null;
	int index = 0;
	i = 0;
	/*
	if (isPickable)
	    return false;
	*/

	// For now, don't merge if the shape has static transform
	if (staticTransform != null)
	    return false;

	// If this shape's to be immediate parent is orderedGroup or a switchNode
	// this shape is not mergerable
	if (parent instanceof OrderedGroupRetained ||
	    parent instanceof SwitchRetained)
	    return false;

	// Get the first geometry that is non-null
	while (geometry == null && index < geometryList.size()) {
	    geometry = (GeometryRetained) geometryList.get(index);
	    index++;
	}

	if (!(geometry  instanceof GeometryArrayRetained)) {
	    return false;
	}

	GeometryArrayRetained firstGeo = (GeometryArrayRetained) geometry;
	
	for(i=index; (i<geometryList.size() && mergeable); i++) {
	    geometry = (GeometryRetained) geometryList.get(i);
	    if (geometry != null) {
		GeometryArrayRetained geo = (GeometryArrayRetained)geometry;

		if (! geo.isWriteStatic())
		    mergeable = false;

		if (geo.vertexFormat != firstGeo.vertexFormat)
		    mergeable = false;
		    

	    }
	}

	// For now, turn off lots of capability bits 
	if (source.getCapability(Shape3D.ALLOW_COLLISION_BOUNDS_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_APPEARANCE_WRITE) || 
	    source.getCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_AUTO_COMPUTE_BOUNDS_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_BOUNDS_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_COLLIDABLE_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_PICKABLE_WRITE) ||
	    source.getCapability(Shape3D.ALLOW_GEOMETRY_WRITE)) {
	    mergeable = false;
	}
	  
	return mergeable;
      
    }


    void getMirrorObjects( ArrayList list, HashKey k) {
	Shape3DRetained ms;
	if (inSharedGroup) {
	    if (k.count == 0) {
		//		System.out.println("===> CAN NEVER BE TRUE");
		return;
	    }
	    else {
		ms = getMirrorShape(k);
	    }
	}
	else {
	    ms = (Shape3DRetained)mirrorShape3D.get(0);
	}
	
	list.add(getGeomAtom(ms));
	
    }

    
    // Called on the mirror Object
    void addLight(LightRetained light) {
	 LightRetained[] newlights;
	 int i, n;
	 Shape3DRetained ms;

	 if (lights == null) {
	     lights = new LightRetained[10];
	 }
	 else if (lights.length == numlights) {
	     newlights = new LightRetained[numlights*2];
	     for (i=0; i<numlights; i++) {
		 newlights[i] = lights[i];
	     }
	     lights = newlights;
	 }
	 lights[numlights] = light;
	 numlights++;
    }

    // called on the mirror object
    void removeLight(LightRetained light) {
	 int i;

	 for (i=0; i<numlights; i++) {
	     if (lights[i] == light) {
		 lights[i] = null;
		 break;
	     }
	 }

	 // Shift everyone down one.
	 for (i++; i<numlights; i++) {
	     lights[i-1] = lights[i];
	 }
	 numlights--;
    }

    // Called on the mirror object
    void addFog(FogRetained fog) {
	 FogRetained[] newfogs;
	 int i;

	 if (fogs == null) {
	     fogs = new FogRetained[10];
	 }
	 else if (fogs.length == numfogs) {
	     newfogs = new FogRetained[numfogs*2];
	     for (i=0; i<numfogs; i++) {
		 newfogs[i] = fogs[i];
	     }
	     fogs = newfogs;
	 }
	 fogs[numfogs] = fog;
	 numfogs++;
    }

    // called on the mirror object
    void removeFog(FogRetained fog) {
		 int i;

	 for (i=0; i<numfogs; i++) {
	     if (fogs[i] == fog) {
		 fogs[i] = null;
		 break;
	     }
	 }

	 // Shift everyone down one.
	 for (i++; i<numfogs; i++) {
	     fogs[i-1] = fogs[i];
	 }
	 numfogs--;

    }

    // Called on the mirror object
    void addModelClip(ModelClipRetained modelClip) {
	 ModelClipRetained[] newModelClips;
	 int i;

	 
	 if (modelClips == null) {
	     modelClips = new ModelClipRetained[10];
	 }
	 else if (modelClips.length == numModelClips) {
	     newModelClips = new ModelClipRetained[numModelClips*2];
	     for (i=0; i<numModelClips; i++) {
		 newModelClips[i] = modelClips[i];
	     }
	     modelClips = newModelClips;
	 }
	 modelClips[numModelClips] = modelClip;
	 numModelClips++;
    }

    // called on the mirror object
    void removeModelClip(ModelClipRetained modelClip) {
		 int i;

	 for (i=0; i<numModelClips; i++) {
	     if (modelClips[i] == modelClip) {
		 modelClips[i] = null;
		 break;
	     }
	 }

	 // Shift everyone down one.
	 for (i++; i<numModelClips; i++) {
	     modelClips[i-1] = modelClips[i];
	 }
	 numModelClips--;

    }

    // Called on the mirror object
    void addAltApp(AlternateAppearanceRetained aApp) {
	 AlternateAppearanceRetained[] newAltApps;
	 int i;
	 if (altApps == null) {
	     altApps = new AlternateAppearanceRetained[10];
	 }
	 else if (altApps.length == numAltApps) {
	     newAltApps = new AlternateAppearanceRetained[numAltApps*2];
	     for (i=0; i<numAltApps; i++) {
		 newAltApps[i] = altApps[i];
	     }
	     altApps = newAltApps;
	 }
	 altApps[numAltApps] = aApp;
	 numAltApps++;
    }

    // called on the mirror object
    void removeAltApp(AlternateAppearanceRetained aApp) {
	int i;

	 for (i=0; i<numAltApps; i++) {
	     if (altApps[i] == aApp) {
		 altApps[i] = null;
		 break;
	     }
	 }

	 // Shift everyone down one.
	 for (i++; i<numAltApps; i++) {
	     altApps[i-1] = altApps[i];
	 }
	 numAltApps--;

    }



    void updatePickable(HashKey keys[], boolean pick[]) { 
      super.updatePickable(keys, pick);
      Shape3DRetained shape;

      if (!inSharedGroup) {
	  shape = (Shape3DRetained) mirrorShape3D.get(0);
	  shape.isPickable = pick[0];
      } else {
	  int size = mirrorShape3D.size();
	  for (int j=0; j< keys.length; j++) {
	      for (int i=0; i < size; i++) {
		  shape = (Shape3DRetained) mirrorShape3D.get(i);
		  if (keys[j].equals(shape.key)) {
		      shape.isPickable = pick[j];
		      break;
		  }

	      }
	  }
      }
    }


    void updateCollidable(HashKey keys[], boolean collide[]) { 
      super.updateCollidable(keys, collide);
      Shape3DRetained shape;

      if (!inSharedGroup) {
	  shape = (Shape3DRetained) mirrorShape3D.get(0);
	  shape.isCollidable = collide[0];
      } else {
	  int size = mirrorShape3D.size();
	  for (int j=0; j< keys.length; j++) {
	      for (int i=0; i < size; i++) {
		  shape = (Shape3DRetained) mirrorShape3D.get(i);
		  if (keys[j].equals(shape.key)) {
		      shape.isCollidable = collide[j];
		      break;
		  }

	      }
	  }
      }
    }




    // New 1.2.1 code ....

    // Remove the old geometry atoms and reInsert
    // the new geometry atoms and update the transform
    // target list
    
    private void sendDataChangedMessage( GeometryRetained newGeom ) {
	
	int i, j, gaCnt;
	GeometryAtom[] newGAArray =  null;
	GeometryAtom[] oldGAArray = null;
	GeometryAtom[] newGeometryAtoms = null;
	int geometryCnt = 0;
	GeometryRetained geometry = null;
	
	int s3dMSize = mirrorShape3D.size();

	if(s3dMSize < 1)
	    return;
	
	Shape3DRetained mS3d = (Shape3DRetained) mirrorShape3D.get(0);
	
	mS3d.mirrorShape3DLock.writeLock();
	
	GeometryAtom oldGA = mS3d.geomAtom;
	
	GeometryAtom newGA = new GeometryAtom();
	
	if(newGeom != null) {
	    newGeom.addUser(mS3d);
	}
	
	int gSize = geometryList.size();    

	for(i=0; i<gSize; i++) {
	    geometry = (GeometryRetained) geometryList.get(i);
	    if(geometry != null) {
		newGA.geoType = geometry.geoType;
		newGA.alphaEditable = mS3d.isAlphaEditable(geometry);
		break;
	    }
	}
	
	if((geometry != null) &&
	   (geometry.geoType == GeometryRetained.GEO_TYPE_TEXT3D)) {
	    
	    for(i = 0; i<gSize; i++) {
		geometry = (GeometryRetained) geometryList.get(i);
		if(geometry != null) {
		    Text3DRetained tempT3d = (Text3DRetained)geometry;
		    geometryCnt += tempT3d.numChars;
		}
		else {
		    // This is slightly wasteful, but not quite worth to optimize yet. 
		    geometryCnt++;
		}
	    }
	    newGA.geometryArray = new GeometryRetained[geometryCnt];
	    newGA.lastLocalTransformArray = new Transform3D[geometryCnt];
	    // Reset geometryCnt;
	    geometryCnt = 0;

	}
	else {
	    newGA.geometryArray = new GeometryRetained[gSize];
	}
	
	newGA.locale = mS3d.locale;
	newGA.visible = visible;
	newGA.source = mS3d;
		    

	for(gaCnt = 0; gaCnt<gSize; gaCnt++) {
	    geometry = (GeometryRetained) geometryList.get(gaCnt);
	    if(geometry == null) {
		newGA.geometryArray[geometryCnt++] = null;
	    }
	    else {
		if (geometry.geoType == GeometryRetained.GEO_TYPE_TEXT3D) {
		    Text3DRetained t = (Text3DRetained)geometry;
		    GeometryRetained geo;
		    for (i=0; i<t.numChars; i++, geometryCnt++) {
			geo = t.geometryList[i];
			if (geo!= null) {
			    newGA.geometryArray[geometryCnt] = geo;
			    newGA.lastLocalTransformArray[geometryCnt] =
				t.charTransforms[i];
			    
			} else {
			    newGA.geometryArray[geometryCnt] = null;
			    newGA.lastLocalTransformArray[geometryCnt] = null;
			}
			
		    }
		    
		} else {
		    newGA.geometryArray[geometryCnt++] = geometry;
		}
	    }
	}

	oldGAArray = new GeometryAtom[s3dMSize];
	newGAArray = new GeometryAtom[s3dMSize];
	oldGAArray[0] = oldGA;
	newGAArray[0] = newGA;

	mS3d.geomAtom = newGA;
	mS3d.mirrorShape3DLock.writeUnlock();
	
	// ..... clone the rest of mirrorS3D's GA with the above newGA, but modify
	// its source.
	
	for (i = 1; i < s3dMSize; i++) {	
	    mS3d = (Shape3DRetained) mirrorShape3D.get(i);
	    mS3d.mirrorShape3DLock.writeLock();
	    oldGA = mS3d.geomAtom;	    
	    newGA = new GeometryAtom();

	    if(newGeom != null) {
		newGeom.addUser(mS3d);
	    }
	    
	    newGA.geoType = newGAArray[0].geoType;
	    newGA.locale = mS3d.locale;
	    newGA.visible = visible;
	    newGA.source = mS3d;
	    newGA.alphaEditable = newGAArray[0].alphaEditable;
	    
	    newGA.geometryArray = new GeometryRetained[newGAArray[0].geometryArray.length];
	    for(j=0; j<newGA.geometryArray.length; j++) {
		newGA.geometryArray[j] = newGAArray[0].geometryArray[j];
	    }

	    oldGAArray[i] = oldGA;
	    newGAArray[i] = newGA;
	    
	    mS3d.geomAtom = newGA;
	    mS3d.mirrorShape3DLock.writeUnlock();	    
	}
	
        TargetsInterface ti = 
		((GroupRetained)parent).getClosestTargetsInterface(
                                        TargetsInterface.TRANSFORM_TARGETS);
	CachedTargets[] newCtArr = null;

        if (ti != null) {
	    CachedTargets ct;
	    newCtArr = new CachedTargets[s3dMSize];

            for (i=0; i<s3dMSize; i++) {

                ct = ti.getCachedTargets(
                                TargetsInterface.TRANSFORM_TARGETS, i, -1);
                if (ct != null) {
		    newCtArr[i] = new CachedTargets();
		    newCtArr[i].copy(ct);
		    newCtArr[i].replace(oldGAArray[i], newGAArray[i], 
					Targets.GEO_TARGETS);
                } else {
		    newCtArr[i] = null;
                }
            }
            ti.resetCachedTargets(TargetsInterface.TRANSFORM_TARGETS, 
							newCtArr, -1);
	}

	
	J3dMessage changeMessage  = VirtualUniverse.mc.getMessage();
	changeMessage.type = J3dMessage.SHAPE3D_CHANGED;
	// Who to send this message to ?	
	changeMessage.threads = J3dThread.UPDATE_RENDER |
	    J3dThread.UPDATE_TRANSFORM |
	    J3dThread.UPDATE_GEOMETRY;
	changeMessage.universe = universe;
	changeMessage.args[0] = this;
	changeMessage.args[1] = new Integer(GEOMETRY_CHANGED);
	changeMessage.args[2] = oldGAArray;
	changeMessage.args[3] = newGAArray;
	if (ti != null) {
	    changeMessage.args[4] = ti;
	    changeMessage.args[5] = newCtArr;
	}
	if (boundsAutoCompute) {
	    getCombineBounds((BoundingBox)localBounds);
	}
	VirtualUniverse.mc.processMessage(changeMessage);  
	
    }


    // ********** End of New 1.2.1 code ....
    


    

    Shape3DRetained getMirrorShape(SceneGraphPath path) {
	if (!inSharedGroup) {
	    return (Shape3DRetained) mirrorShape3D.get(0);
	}
	HashKey key = new HashKey("");
	path.getHashKey(key);	
	return getMirrorShape(key);
    }

    Shape3DRetained getMirrorShape(HashKey key) {
	if (key == null) {
	    return (Shape3DRetained) mirrorShape3D.get(0);
	} else {
	    int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);

	    if (i>=0) {
		return (Shape3DRetained) mirrorShape3D.get(i);
	    }
	}
	// Not possible
	throw new RuntimeException("Shape3DRetained: MirrorShape Not found!");
    }
    
    void setBoundsAutoCompute(boolean autoCompute) {
	GeometryRetained geometry;
        if (autoCompute != boundsAutoCompute) {
            if (autoCompute) {
                // localBounds may not have been set to bbox
                localBounds = new BoundingBox((BoundingBox) null);
		if (source.isLive() && geometryList != null) {
		    int size = geometryList.size()*mirrorShape3D.size();
		    for (int i=0; i<size; i++) {
			geometry = (GeometryRetained) geometryList.get(i);
			geometry.incrComputeGeoBounds();
		    }
		}

		getCombineBounds((BoundingBox)localBounds);
            }
	    else {
		if (source.isLive() && geometryList != null) {
		    int size = geometryList.size()*mirrorShape3D.size();
		    for (int i=0; i<size; i++) {
			geometry = (GeometryRetained) geometryList.get(i);
			geometry.decrComputeGeoBounds();
		    }

		}
            }
            super.setBoundsAutoCompute(autoCompute);
            if (source.isLive()) {
                J3dMessage message = VirtualUniverse.mc.getMessage();
                message.type = J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED;
                message.threads = J3dThread.UPDATE_TRANSFORM |
		    J3dThread.UPDATE_GEOMETRY |
		    J3dThread.UPDATE_RENDER;
                message.universe = universe;
                message.args[0] = getGeomAtomsArray(mirrorShape3D);
		// no need to clone localBounds
                message.args[1] = localBounds;
		VirtualUniverse.mc.processMessage(message);
	    }
        }
    }
    // This method is called when coordinates of a geometry in the geometrylist
    // changed and autoBoundsCompute is true
    
    void updateBounds() {
	localBounds = new BoundingBox((BoundingBox) null);
	getCombineBounds((BoundingBox)localBounds);
	synchronized(mirrorShape3D) {
	    if (source.isLive()) {
		J3dMessage message = VirtualUniverse.mc.getMessage();
		message.type = J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED;
		message.threads = J3dThread.UPDATE_TRANSFORM |
		    J3dThread.UPDATE_GEOMETRY |
		    J3dThread.UPDATE_RENDER;
		message.universe = universe;
		message.args[0] = getGeomAtomsArray(mirrorShape3D);
		// no need to clone localBounds
		message.args[1] = localBounds;
		VirtualUniverse.mc.processMessage(message);
	    }
	}
    }

    boolean allowIntersect() {
	GeometryRetained ga = null;
	
	for(int i=0; i<geometryList.size(); i++) {
	    ga = (GeometryRetained) geometryList.get(i);
	    if(ga != null)
		if (!ga.source.getCapability(Geometry.ALLOW_INTERSECT)) {
		    return false;
		}
	}
	return true;
    }
    boolean intersectGeometryList(Shape3DRetained otherShape) {
	GeometryRetained geom1, geom2;
	ArrayList gaList = otherShape.geometryList;
	int gaSize =  gaList.size();
	Transform3D otherLocalToVworld = otherShape.getCurrentLocalToVworld(); 	
	Transform3D thisLocalToVworld = getCurrentLocalToVworld();
	View views = null;
	int primaryViewIdx = -1;


	if (this instanceof OrientedShape3DRetained) {
	    primaryViewIdx = getPrimaryViewIdx();
	    thisLocalToVworld.mul(((OrientedShape3DRetained)this).
				  getOrientedTransform(primaryViewIdx));
	}
	
	if (otherShape instanceof OrientedShape3DRetained) {
	    if (primaryViewIdx < 0) {
		primaryViewIdx = getPrimaryViewIdx();
	    }
	    otherLocalToVworld.mul(((OrientedShape3DRetained)otherShape).
				   getOrientedTransform(primaryViewIdx));
	}

	for (int i=geometryList.size()-1; i >=0; i--) {
	    geom1 = (GeometryRetained) geometryList.get(i);
	    if (geom1 != null) {
		for (int j=gaSize-1; j >=0; j--) {
		    geom2 = (GeometryRetained) gaList.get(j);
		    if ((geom2 != null) &&
			geom1.intersect(thisLocalToVworld,
					otherLocalToVworld, geom2)) {
			return true;
		    }
		}
	    }
	}
	
	return false;
    }

    boolean intersectGeometryList(Transform3D thisLocalToVworld, Bounds targetBound) {

	GeometryRetained geometry;

	if (this instanceof OrientedShape3DRetained) {
	    Transform3D orientedTransform = 
		((OrientedShape3DRetained)this).
		getOrientedTransform(getPrimaryViewIdx());
	    thisLocalToVworld.mul(orientedTransform);
	}

	for (int i=geometryList.size() - 1; i >=0; i--) {
	    geometry = (GeometryRetained) geometryList.get(i);
	    if ((geometry != null) &&
		geometry.intersect(thisLocalToVworld, targetBound)) {
		return true;
	    }
	}
	
	return false;
	
    }


    /**
     * This initialize the mirror shape to reflect the state of the
     * real Morph.
     */
    void initMirrorShape3D(SetLiveState s, MorphRetained morph, int index) {

	GeometryRetained geometry;

	GeometryAtom[] newGeometryAtoms = null;

	universe = morph.universe;
	inSharedGroup = morph.inSharedGroup;
        inBackgroundGroup = morph.inBackgroundGroup;
        geometryBackground = morph.geometryBackground;
        parent = morph.parent;
	locale = morph.locale;

        OrderedPath op = (OrderedPath)s.orderedPaths.get(index);
        if (op.pathElements.size() == 0) {
            orderedPath = null;
        } else {
            orderedPath = op;
        }

	staticTransform = morph.staticTransform;
        if (morph.boundsAutoCompute) {
            localBounds.set(morph.localBounds);
        }
        bounds = localBounds;
        vwcBounds = new BoundingBox((BoundingBox) null);
        vwcBounds.transform(bounds, getCurrentLocalToVworld(0));

        if (morph.collisionBound == null) {
            collisionBound = null;
            collisionVwcBound = vwcBounds;
        } else {
            collisionBound = morph.collisionBound;
            collisionVwcBound = (Bounds)collisionBound.clone();
            collisionVwcBound.transform(getCurrentLocalToVworld(0));
        }

	appearanceOverrideEnable = morph.appearanceOverrideEnable;

	// mga is the final geometry we're interested.
	geometryList = new ArrayList(1);
	geometryList.add((GeometryArrayRetained)morph.morphedGeometryArray.retained);
	
	GeometryAtom gAtom = new GeometryAtom();
	gAtom.geometryArray = new GeometryRetained[1];
	
	gAtom.locale = locale;
	gAtom.visible = morph.visible;
	gAtom.source = this;
	
	geometry = (GeometryRetained) geometryList.get(0);
	
	if(geometry ==null) {
	    gAtom.geometryArray[0] = null;
	} else {
	    gAtom.geometryArray[0] = (GeometryArrayRetained)morph.
		morphedGeometryArray.retained;
	    gAtom.geoType = gAtom.geometryArray[0].geoType;
	}
	geomAtom = gAtom;

	// Assign the parent of this mirror shape node
	sourceNode = morph;
    }

    // geometries in morph object is modified, update the geometry
    // list in the mirror shapes and the geometry array in the geometry atom

    void setMorphGeometry(Geometry geometry, ArrayList mirrorShapes) {
        GeometryAtom oldGA, newGA;
	Shape3DRetained ms;
        TransformGroupRetained tg;
        int nMirrorShapes = mirrorShapes.size();
	int i;

        GeometryAtom oldGAArray[] = new GeometryAtom[nMirrorShapes];
        GeometryAtom newGAArray[] = new GeometryAtom[nMirrorShapes];


	for (i = 0; i < nMirrorShapes; i++) {
	    ms = (Shape3DRetained) mirrorShapes.get(i);

	    oldGA = Shape3DRetained.getGeomAtom(ms);

            ms.geometryList = new ArrayList(1);
            ms.geometryList.add((GeometryArrayRetained)geometry.retained);

            newGA = new GeometryAtom();
            newGA.geometryArray = new GeometryRetained[1];

            if (geometry ==null) {
                newGA.geometryArray[0] = null;
            } else {
                newGA.geometryArray[0] = 
			(GeometryArrayRetained)geometry.retained;
                newGA.geoType = newGA.geometryArray[0].geoType;
            }

            newGA.locale = locale;
            newGA.visible = oldGA.visible;
            newGA.source = this;

            oldGAArray[i] = oldGA;
            newGAArray[i] = newGA;

	    Shape3DRetained.setGeomAtom(ms, newGA);
	}

        TargetsInterface ti = 
		((GroupRetained)parent).getClosestTargetsInterface(
                                        TargetsInterface.TRANSFORM_TARGETS);
	CachedTargets[] newCtArr = null;

        if (ti != null) {
	    CachedTargets ct;
	    newCtArr = new CachedTargets[nMirrorShapes];

            for (i=0; i<nMirrorShapes; i++) {

                ct = ti.getCachedTargets(
                                TargetsInterface.TRANSFORM_TARGETS, i, -1);
                if (ct != null) {
		    newCtArr[i] = new CachedTargets();
		    newCtArr[i].copy(ct);
		    newCtArr[i].replace(oldGAArray[i], newGAArray[i], 
					Targets.GEO_TARGETS);
                } else {
		    newCtArr[i] = null;
                }
            }
        }

	// send a Shape GEOMETRY_CHANGED message for all geometry atoms

	J3dMessage changeMessage  = VirtualUniverse.mc.getMessage();
	changeMessage.type = J3dMessage.SHAPE3D_CHANGED;
	changeMessage.threads = J3dThread.UPDATE_RENDER |
	    J3dThread.UPDATE_TRANSFORM |
	    J3dThread.UPDATE_GEOMETRY;
	changeMessage.universe = universe;
	changeMessage.args[0] = this;
	changeMessage.args[1] = new Integer(GEOMETRY_CHANGED);
	changeMessage.args[2] = oldGAArray;
	changeMessage.args[3] = newGAArray;
        if (ti != null) {
            changeMessage.args[4] = ti;
            changeMessage.args[5] = newCtArr;
        }
	VirtualUniverse.mc.processMessage(changeMessage);  
    }
    

    /**
     * Return an array of geometry atoms belongs to userList.
     * The input is an arraylist of Shape3DRetained type.
     * This is used to send a message of the snapshot of the 
     * geometry atoms that are affected by this change.
     */
    final static GeometryAtom[] getGeomAtomsArray(ArrayList userList) {
	Shape3DRetained ms = null;
	GeometryAtom[] gaArr = null;
	int size, nullCnt=0, i, j;
	
	synchronized(userList) {
	    size = userList.size();
	    gaArr = new GeometryAtom[size];
	    for (i = 0; i < size; i++) {
		ms = (Shape3DRetained) userList.get(i);
		ms.mirrorShape3DLock.readLock();
		if(ms.geomAtom == null) { 
		    nullCnt++;
		}
		gaArr[i] = ms.geomAtom;
		ms.mirrorShape3DLock.readUnlock();
	    }
	}
	if(nullCnt == 0) {
	    return gaArr;
	}
	else if(nullCnt == size) {
	    return null;
	}
	else {
	    GeometryAtom[] newGaArr = new GeometryAtom[size - nullCnt];
	    
	    for (i=0, j=0; i < size; i++) {
		if(gaArr[i] != null) {
		    newGaArr[j++] = gaArr[i];
		}
	    }
	    return newGaArr;
	}
    }

    /**
     * Return a list of geometry atoms belongs to userList and places a list of
     * universe found in userList in univList.
     * The input is an array of Shape3DRetained type.
     * univList is assume to be empty.
     * This is used to send a message of the snapshot of the 
     * geometry atoms that are affected by this change.
     */
    final static ArrayList getGeomAtomsList(ArrayList userList, ArrayList univList) {
	ArrayList listPerUniverse = new ArrayList();
	int index;
	ArrayList gaList = null;
	Shape3DRetained ms = null;
	boolean moreThanOneUniv = false;
	VirtualUniverse firstFndUniv = null;
	
	synchronized(userList) {
	    for (int i = userList.size()-1; i >=0; i--) {
		ms = (Shape3DRetained) userList.get(i);

		if(moreThanOneUniv == false) {
		    if(firstFndUniv == null) {
			firstFndUniv = ms.universe;
			univList.add(ms.universe);

			gaList = new ArrayList();
			listPerUniverse.add(gaList);
		    }
		    else if(firstFndUniv != ms.universe) {
			moreThanOneUniv = true;
			univList.add(ms.universe);
			gaList = new ArrayList();
			listPerUniverse.add(gaList);
		    }
		}
		else {
		    index = univList.indexOf(ms.universe);
		    if (index < 0) {
			univList.add(ms.universe);
			gaList = new ArrayList();
			listPerUniverse.add(gaList);
		    }
		    else {
			gaList = (ArrayList) listPerUniverse.get(index);
		    }
		}


		ms.mirrorShape3DLock.readLock();

		if(ms.geomAtom != null) {
		    gaList.add(ms.geomAtom);
		}
		ms.mirrorShape3DLock.readUnlock();

	    }
	}
	return listPerUniverse;
    }
    
    final static GeometryAtom getGeomAtom(Shape3DRetained shape) {
	GeometryAtom ga;
	
	shape.mirrorShape3DLock.readLock();
	ga = shape.geomAtom;
	shape.mirrorShape3DLock.readUnlock();

	return ga;
    }

    final static void setGeomAtom(Shape3DRetained shape, GeometryAtom ga) {
	shape.mirrorShape3DLock.writeLock();
	shape.geomAtom = ga;
	shape.mirrorShape3DLock.writeUnlock();
    }
    

    // Alpha is editable due to the appearance
    boolean isAlphaEditable(GeometryRetained geo) {

        boolean alphaEditable = false;

	if (appearanceOverrideEnable) {
	    alphaEditable = true;
        } else if (geo != null &&
		   appearance != null) {

            AppearanceRetained app = appearance;

            if (source.getCapability(
                        Shape3D.ALLOW_APPEARANCE_WRITE) ||
		source.getCapability(
                        Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE) ||

                app.source.getCapability(
                        Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE) ||

                app.source.getCapability(
                        Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE) ||

                (app.renderingAttributes != null &&
                 (app.renderingAttributes.source.getCapability(
                        RenderingAttributes.ALLOW_ALPHA_TEST_FUNCTION_WRITE) ||
		  app.renderingAttributes.source.getCapability(
                        RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE))) ||

                (app.transparencyAttributes != null &&
                 (app.transparencyAttributes.source.getCapability(
                        TransparencyAttributes.ALLOW_MODE_WRITE) ||
                  app.transparencyAttributes.source.getCapability(
                        TransparencyAttributes.ALLOW_VALUE_WRITE)))) {

                alphaEditable = true;

            } else if (geo instanceof GeometryArrayRetained &&
                       (app.source.getCapability(
                        Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE) ||

                        (app.textureAttributes != null &&
                         app.textureAttributes.source.getCapability(
                        TextureAttributes.ALLOW_MODE_WRITE)))) {

                alphaEditable = true;

            } else if (geo instanceof RasterRetained) {
                if ((((RasterRetained)geo).type & Raster.RASTER_COLOR) !=
0
                    && ((RasterRetained)geo).source.getCapability(
                        Raster.ALLOW_IMAGE_WRITE)) {

                    alphaEditable = true;
                }
            }
        }
	return alphaEditable;
    }

    // getCombineBounds is faster than computeCombineBounds since it
    // does not recompute the geometry.geoBounds
    void getCombineBounds(BoundingBox bounds) {

        if(geometryList != null) {
	    BoundingBox bbox = null;
            GeometryRetained geometry;

	    if (staticTransform != null) {
		bbox = new BoundingBox((BoundingBox) null);
	    }
	    
            synchronized(bounds) {
                bounds.setLower( 1.0, 1.0, 1.0);
                bounds.setUpper(-1.0,-1.0,-1.0);
                for(int i=0; i<geometryList.size(); i++) {
                    geometry = (GeometryRetained) geometryList.get(i);
                    if ((geometry != null) &&
                        (geometry.geoType != GeometryRetained.GEO_TYPE_NONE)) {
                        synchronized(geometry.geoBounds) {
                            if (staticTransform != null) {
                                bbox.set(geometry.geoBounds);
                                bbox.transform(staticTransform.transform);
                                bounds.combine((Bounds)bbox);
                            } else {
                                bounds.combine((Bounds)geometry.geoBounds);
                            }
                        }
                    }
                }
            }
	    
	    // System.out.println("Shape3DRetained - getCombineBounds");
	    // Enlarge boundingBox to the "minmium bounds" that encompasses all possible
	    // orientation.
	    if (this instanceof OrientedShape3DRetained) {
		double maxVal = Math.abs(bounds.lower.x);
		double tempVal = Math.abs(bounds.upper.x);
		if(tempVal > maxVal)
		    maxVal = tempVal;
		tempVal = Math.abs(bounds.lower.y);
		if(tempVal > maxVal)
		    maxVal = tempVal;
		tempVal = Math.abs(bounds.upper.y);
		if(tempVal > maxVal)
		    maxVal = tempVal;
		tempVal = Math.abs(bounds.lower.z);
		if(tempVal > maxVal)
		    maxVal = tempVal;
		tempVal = Math.abs(bounds.upper.z);
		if(tempVal > maxVal)
		    maxVal = tempVal;

		// System.out.println("Shape3DRetained - bounds (Before) " + bounds);
		bounds.setLower(-maxVal, -maxVal, -maxVal);
		bounds.setUpper(maxVal, maxVal, maxVal);
		// System.out.println("Shape3DRetained - bounds (After) " + bounds);
	    }
	    
        }
    }


    boolean isEquivalent(Shape3DRetained shape) {
	if (this.appearance != shape.appearance ||
	    // Scoping info should be same since they are under same group
	    this.appearanceOverrideEnable != shape.appearanceOverrideEnable ||
	    this.isPickable != shape.isPickable ||
	    this.isCollidable != shape.isCollidable) {

	    return false;
	}
	if (this.boundsAutoCompute) {
	    if (!shape.boundsAutoCompute)
		return false;
	}
	else {
	    // If bounds autoCompute is false
	    // Then check if both bounds are equal
	    if (this.localBounds != null) {
		if (shape.localBounds != null) {
		    return this.localBounds.equals(shape.localBounds);
		}
	    }
	    else if (shape.localBounds != null) {
		return false;
	    }
	}
	if (collisionBound != null) {
	    if (shape.collisionBound == null)
		return false;
	    else
		return collisionBound.equals(shape.collisionBound);
	}
	else if (shape.collisionBound != null)
	    return false;
	
	return true;
    }

    // Bounds can only be set after the geometry is setLived, so has to be done
    // here, if we are not using switchVwcBounds
    void initializeGAtom(Shape3DRetained ms) {
	int i, gaCnt;
	int geometryCnt = 0;
	int gSize = geometryList.size();
	GeometryRetained geometry = null;

	ms.bounds = localBounds;
	ms.vwcBounds = new BoundingBox((BoundingBox) null);
	ms.vwcBounds.transform(ms.bounds, ms.getCurrentLocalToVworld(0));

	if (collisionBound == null) {
	    ms.collisionBound = null;
	    ms.collisionVwcBound = ms.vwcBounds;
	} else {
	    ms.collisionBound = collisionBound;
	    ms.collisionVwcBound = (Bounds)ms.collisionBound.clone();
	    ms.collisionVwcBound.transform(ms.getCurrentLocalToVworld(0));
	}
	GeometryAtom gAtom = new GeometryAtom();
	for(gaCnt=0; gaCnt<gSize; gaCnt++) {
	    geometry = (GeometryRetained) geometryList.get(gaCnt);
	    if(geometry != null) {
		gAtom.geoType = geometry.geoType;
		gAtom.alphaEditable = ms.isAlphaEditable(geometry);
		break;
	    }
	}
	if((geometry != null) &&
	   (geometry.geoType == GeometryRetained.GEO_TYPE_TEXT3D)) {

	    for(gaCnt = 0; gaCnt<gSize; gaCnt++) {
		geometry = (GeometryRetained) geometryList.get(gaCnt);
		if(geometry != null) {
		    Text3DRetained tempT3d = (Text3DRetained)geometry;
		    geometryCnt += tempT3d.numChars;
		}
		else {
		    // This is slightly wasteful, but not quite worth to optimize yet. 
		    geometryCnt++;
		}
	    }
	    gAtom.geometryArray = new GeometryRetained[geometryCnt];
	    gAtom.lastLocalTransformArray = new Transform3D[geometryCnt];
	    // Reset geometryCnt;
	    geometryCnt = 0;
	    
	}
	else {
	    gAtom.geometryArray = new GeometryRetained[gSize];
	}


	for(gaCnt = 0; gaCnt<geometryList.size(); gaCnt++) {
	    geometry = (GeometryRetained) geometryList.get(gaCnt);
	    if(geometry == null) {
		gAtom.geometryArray[gaCnt] = null;
	    }
	    else {
		if (geometry.geoType == GeometryRetained.GEO_TYPE_TEXT3D) {
		    Text3DRetained t = (Text3DRetained)geometry;
		    GeometryRetained geo;
		    for (i=0; i<t.numChars; i++, geometryCnt++) {
			geo = t.geometryList[i];
			if (geo != null) {
			    gAtom.geometryArray[geometryCnt] = geo;
			    gAtom.lastLocalTransformArray[geometryCnt] =
				t.charTransforms[i];
			} else {
			    gAtom.geometryArray[geometryCnt] = null;
			    gAtom.lastLocalTransformArray[geometryCnt] = null;
			}
			
		    }
		    
		} else {
		    gAtom.geometryArray[gaCnt] = geometry;
		}
	    }
	}
	gAtom.locale = ms.locale;
	gAtom.visible = visible;
	gAtom.source = ms;
	ms.geomAtom = gAtom;
    }

    // Check if geomRetained's class is equivalence with the geometry class.
    void checkEquivalenceClass(Geometry geometry, int index) {

	if (geometry != null) {
	    for (int i=geometryList.size()-1; i >= 0; i--) {  
		GeometryRetained geomRetained = (GeometryRetained) geometryList.get(i);
		if ((geomRetained != null) &&
		    (index != i)) { // this geometry will replace
		    // current one so there is no need to check
		    if (!geomRetained.isEquivalenceClass((GeometryRetained)geometry.retained)) {
			throw new IllegalArgumentException(J3dI18N.getString("Shape3DRetained5"));	
		    }
		    break;
		}
	    }
	}
    }

    int indexOfGeometry(Geometry geometry) {
      if(geometry != null) 
	return geometryList.indexOf(geometry.retained);
      else
	return geometryList.indexOf(null);
    }


  // Removes the specified geometry from this Shape3DRetained's list of geometries 
    void removeGeometry(Geometry geometry) {
      int ind = indexOfGeometry(geometry);
      if(ind >= 0)
	removeGeometry(ind);
    }

  // Removes all the geometries from this node
    void removeAllGeometries() {
      int n = geometryList.size();
      
      int i;
      Shape3DRetained mShape;
      GeometryRetained oldGeom = null;
	
      if (((Shape3D)this.source).isLive()) {
	for(int index = n-1; index >= 0; index--) {	    
	  oldGeom = (GeometryRetained) (geometryList.get(index));
	  if (oldGeom != null) {
	    oldGeom.clearLive(refCount);
	    oldGeom.decRefCnt();
	    for (i=0; i<mirrorShape3D.size(); i++) {
	      mShape = (Shape3DRetained)mirrorShape3D.get(i);
	      oldGeom.removeUser(mShape);
	    }
	  }
	  geometryList.remove(index);		    
	}
	sendDataChangedMessage(null);
      } else {
	for(int index = n-1; index >= 0; index--) {	    
	  oldGeom = (GeometryRetained) (geometryList.get(index));
	  if (oldGeom != null) {
	    oldGeom.decRefCnt();
	  }
	  geometryList.remove(index);	
	}
      }
    }

    boolean willRemainOpaque(int geoType) {
	if (appearance == null ||
	    (appearance.isStatic() &&
	     appearance.isOpaque(geoType))) {
	    return true;
	}
	else {
	    return false;
	}

    }

    static Point3d getPoint3d() {
	return (Point3d)FreeListManager.getObject(FreeListManager.POINT3D);
    }

    static void freePoint3d(Point3d p) {
	FreeListManager.freeObject(FreeListManager.POINT3D, p);
    }

    void handleFrequencyChange(int bit) {
	int mask = 0;
	if (bit == Shape3D.ALLOW_GEOMETRY_WRITE) {
	    mask = GEOMETRY_CHANGED;
	}
	else if (bit == Shape3D.ALLOW_APPEARANCE_WRITE) {
	    mask = APPEARANCE_CHANGED;
	}
	else if (bit == Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE) {
	    mask = APPEARANCEOVERRIDE_CHANGED;
	}
	if (mask != 0) {
	    if (source.getCapabilityIsFrequent(bit))
		changedFrequent |= mask;
	    else if (!source.isLive()) {
		changedFrequent &= ~mask;
	    }
	}
    }    


    // Alpha is editable due to the appearance(Called on the MirrorShape3D)
    boolean isAlphaFrequentlyEditable(GeometryRetained geo) {

        boolean alphaFrequentlyEditable = false;
	if (appearanceOverrideEnable) {
	    alphaFrequentlyEditable = true;
        } else if (geo != null &&
		   appearance != null) {
            AppearanceRetained app = appearance;

            if (((changedFrequent &(APPEARANCE_CHANGED|APPEARANCEOVERRIDE_CHANGED)) != 0)||
                ((app.changedFrequent &(AppearanceRetained.RENDERING|AppearanceRetained.TRANSPARENCY)) != 0) ||
                (app.renderingAttributes != null &&
                 (((app.renderingAttributes.changedFrequent & (RenderingAttributesRetained.IGNORE_VCOLOR |RenderingAttributesRetained.ALPHA_TEST_FUNC)) != 0))) ||
		
                (app.transparencyAttributes != null &&
                 ((app.transparencyAttributes.changedFrequent != 0)))) {
		
                alphaFrequentlyEditable = true;

	    } else if (geo instanceof GeometryArrayRetained &&
		       ((app.changedFrequent & AppearanceRetained.TEXTURE_ATTR)  != 0) ||
		       (app.textureAttributes != null &&
			((app.textureAttributes.changedFrequent & TextureAttributes.ALLOW_MODE_WRITE) != 0))) {
                alphaFrequentlyEditable = true;

            } else if (geo instanceof RasterRetained) {
                if (((((RasterRetained)geo).type & Raster.RASTER_COLOR) !=
0)
                    && (((RasterRetained)geo).cachedChangedFrequent != 0)) {

                    alphaFrequentlyEditable = true;
                }
            }
	}
	//	System.out.println("changedFrequent="+changedFrequent+" sourceNode = "+sourceNode+" isAlphaFrequentlyEditable, = "+alphaFrequentlyEditable);
	return alphaFrequentlyEditable;
    }

    
    int getPrimaryViewIdx() {
	// To avoid MT-safe issues when using View, just clone it.
	UnorderList viewList  = VirtualUniverse.mc.cloneView();
	View views[] = (View []) viewList.toArray(false);
	int size = viewList.arraySize();
	
	for (int i=0; i < size; i++) {
	    if (views[i].primaryView) {
		return views[i].viewIndex;
	    }
	}
	return 0;
    }
    
    void searchGeometryAtoms(UnorderList list) {
	list.add(getGeomAtom(getMirrorShape(key)));
    }
}

