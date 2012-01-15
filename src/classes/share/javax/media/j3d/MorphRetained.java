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

import java.util.ArrayList;

import javax.vecmath.Color3b;
import javax.vecmath.Color3f;
import javax.vecmath.Color4b;
import javax.vecmath.Color4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.TexCoord3f;
import javax.vecmath.Vector3f;

/**
 * A morph leaf node consisting of geometery and appearance properties.
 */

class MorphRetained extends LeafRetained implements GeometryUpdater {

    // These bits should match the Shape3D bits ...(Since the mirrors for
    // both are Shape3DRetained
    static final int GEOMETRY_CHANGED		= 0x00001;
    static final int APPEARANCE_CHANGED		= 0x00002;
    static final int COLLISION_CHANGED		= 0x00004;
    static final int BOUNDS_CHANGED		= 0x00008;
    static final int APPEARANCEOVERRIDE_CHANGED	= 0x00010;
    static final int UPDATE_MORPH		= 0x00020;

    private static final double TOLERANCE = 1.0e-4;


    /**
     * The mirror Shape3DRetained nodes for this object.  There is one
     * mirror for each instance of this Shape3D node.  If it is not in
     * a SharedGroup, only index 0 is valid.
     */
    ArrayList mirrorShape3D = new ArrayList();


    // Target threads to be notified when morph changes
    final static int targetThreads = (J3dThread.UPDATE_RENDER |
				      J3dThread.UPDATE_GEOMETRY);

    /**
     * The appearance component of the morph node.
     */
    AppearanceRetained appearance = null;

    /**
     * The Geosets associated with the morph node.
     */
    GeometryArrayRetained geometryArrays[];

    private int numGeometryArrays = 0;

    /**
     * The weight vector the morph node.
     */
    double weights[];

    /**
     * Reference to the  BranchGroup path of this mirror shape
     * This is used for picking only.
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

    // Is this Morph visible ? The default is true.
    boolean visible = true;

    // geometry Bounds in local coordinate
    Bounds bounds = null;

    // geometry Bounds in virtual world coordinate
    BoundingBox vwcBounds = new BoundingBox();

    // collision Bound in local coordinate
    Bounds collisionBound = null;

    // collision Bounds in virtual world coordinate
    Bounds collisionVwcBound = null;


    GeometryArray morphedGeometryArray = null;

    // Morph data
    float[] Mcoord = null;
    float[] Mcolor = null;
    float[] Mnormal = null;
    // First dimension is the coordSet, second dimenension is the vertex index
    // each vertex has 2 or 3floats
    float[][]MtexCoord = null;

    // Whether the normal appearance is overrided by the alternate app
    boolean appearanceOverrideEnable = false;

    int changedFrequent = 0;


    MorphRetained() {
        this.nodeType = NodeRetained.MORPH;
	localBounds = new BoundingBox((Bounds)null);
    }

    /**
     * Sets the collision bounds of a node.
     * @param bounds the bounding object for the node
     */
    void setCollisionBounds(Bounds bounds) {
        if (bounds != null) {
	    collisionBound = (Bounds)bounds.clone();
	} else {
            collisionBound = null;
	}
	if (source.isLive()) {
	    // Notify Geometry Structure to set mirror shape collision
	    // bound and check for collision
	    J3dMessage message = new J3dMessage();
	    message.type = J3dMessage.COLLISION_BOUND_CHANGED;
            message.threads = J3dThread.UPDATE_TRANSFORM;
	    message.universe = universe;
            message.args[1] = collisionBound;
	    VirtualUniverse.mc.processMessage(message);
	}
    }

    /**
     * Sets the geometric bounds of a node.
     * @param bounds the bounding object for the node
     */
    void setBounds(Bounds bounds) {
	super.setBounds(bounds);
	if (source.isLive() && !boundsAutoCompute) {
	    J3dMessage message = new J3dMessage();
	    message.type = J3dMessage.REGION_BOUND_CHANGED;
	    message.threads = J3dThread.UPDATE_TRANSFORM |
		              targetThreads;
	    message.universe = universe;
	    message.args[0] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
	    message.args[1] = localBounds;
	    VirtualUniverse.mc.processMessage(message);
	}
    }

    /**
     * Gets the collision bounds of a node.
     * @return the node's bounding object
     */
    Bounds getCollisionBounds() {
        return (collisionBound == null? null : (Bounds)collisionBound.clone());
    }

    /**
     * Sets the geometryArrays component of the Morph node.
     * @param geometryArrays the new vector of geometryArrays for the morph node
     */
    void setGeometryArrays(GeometryArray geometryArrays[]) {
        int i;

	if ((geometryArrays == null || geometryArrays.length == 0) && numGeometryArrays == 0)
	    return;

        GeometryArrayRetained geo, prevGeo;

        if (numGeometryArrays != 0 && (geometryArrays == null || numGeometryArrays != geometryArrays.length))
	    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained0"));


        for (i=1;i < geometryArrays.length;i++) {
	    if (geometryArrays[i] == null || geometryArrays[i-1] == null)
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	    geo = (GeometryArrayRetained)geometryArrays[i].retained;
	    prevGeo = (GeometryArrayRetained)geometryArrays[i-1].retained;
	    if (prevGeo == null || geo == null) {
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));

	    }
	    doErrorCheck(prevGeo, geo);
	}

	// Check the first one for vertex attributes
	geo = (GeometryArrayRetained)geometryArrays[0].retained;
	if ((geo.vertexFormat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
	    throw new UnsupportedOperationException(J3dI18N.getString("MorphRetained9"));
	}

	// Check if the first one is in Immediate context
	if (geometryArrays[0] != null) {
	    geo = (GeometryArrayRetained)geometryArrays[0].retained;
	}

	if (numGeometryArrays == 0) {
            this.geometryArrays = new GeometryArrayRetained[geometryArrays.length];
	    numGeometryArrays = geometryArrays.length;
	}

        for (i=0;i < numGeometryArrays;i++) {
	    geo = (GeometryArrayRetained)geometryArrays[i].retained;
	    if (((Morph)this.source).isLive()) {
		if (this.geometryArrays[i] != null) {
		    this.geometryArrays[i].clearLive(refCount);
		    this.geometryArrays[i].removeMorphUser(this);
		}
		if (geo != null) {
		    geo.setLive(inBackgroundGroup, refCount);
		    geo.addMorphUser(this);
		}
	    }

	    this.geometryArrays[i] = geo;
	}
	if (this.geometryArrays[0] == null)
	    return;


        if (weights == null) {
	    weights = new double[numGeometryArrays];
	    weights[0] = 1.0;
	    int vFormat = this.geometryArrays[0].vertexFormat;
	    // default is zero when new array
	    //for (i=1; i < numGeometryArrays;i++)  weights[i] = 0.0;

	    int texCoordSetCount = this.geometryArrays[0].getTexCoordSetCount();
	    if (this.geometryArrays[0] instanceof IndexedGeometryArrayRetained) {
		Mcoord = new float[this.geometryArrays[0].getNumCoordCount()* 3];

		if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3)
		    Mcolor = new float[this.geometryArrays[0].getNumColorCount()* 3];
		else if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4)
		    Mcolor = new float[this.geometryArrays[0].getNumColorCount()* 4];

		MtexCoord = new float[texCoordSetCount][];
		if ((vFormat & GeometryArray.NORMALS) != 0)
		    Mnormal = new float[this.geometryArrays[0].getNumNormalCount() *3];
		for (int k = 0; k < texCoordSetCount; k++) {
		    if ((vFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0)
			MtexCoord[k] = new float[this.geometryArrays[0].getNumTexCoordCount(k) * 2];
		    else if (((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0))
			MtexCoord[k] = new float[this.geometryArrays[0].getNumTexCoordCount(k) * 3];
		    else if (((vFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0))
			MtexCoord[k] = new float[this.geometryArrays[0].getNumTexCoordCount(k) * 4];
		}
	    }
	    else {
		Mcoord = new float[this.geometryArrays[0].validVertexCount* 3];

		if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3) {
		    Mcolor = new float[this.geometryArrays[0].validVertexCount* 3];
		} else if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4) {
		    Mcolor = new float[this.geometryArrays[0].validVertexCount* 4];
		}
		MtexCoord = new float[texCoordSetCount][];
		if ((vFormat & GeometryArray.NORMALS) != 0) {
		    Mnormal = new float[this.geometryArrays[0].validVertexCount *3];
		}
		for (int k = 0; k < texCoordSetCount; k++) {
		    if ((vFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0)
			MtexCoord[k] = new float[this.geometryArrays[0].validVertexCount * 2];
		    else if (((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0))
			MtexCoord[k] = new float[this.geometryArrays[0].validVertexCount * 3];
		    else if (((vFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0))
			MtexCoord[k] = new float[this.geometryArrays[0].validVertexCount * 4];
		}
	    }
	}

	//  create a new morphedGeometryArray
	initMorphedGeometry();

	if (source.isLive()) {

	    Shape3DRetained shape = (Shape3DRetained)mirrorShape3D.get(0);

	    shape.setMorphGeometry(morphedGeometryArray, mirrorShape3D);

	    J3dMessage mChangeMessage = null;
	    mChangeMessage = new J3dMessage();
	    mChangeMessage.type = J3dMessage.MORPH_CHANGED;
	    mChangeMessage.threads = (J3dThread.UPDATE_GEOMETRY |
				      J3dThread.UPDATE_TRANSFORM);
	    // If its a indexed geometry array, unindexify in renderBin
	    if (this.geometryArrays[0] instanceof IndexedGeometryArrayRetained)
		mChangeMessage.threads |= J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	    mChangeMessage.args[0] = this;
	    mChangeMessage.args[1]= new Integer(GEOMETRY_CHANGED);
	  // a shadow copy of this ArrayList instance. (The elements themselves are not copied.)
	    mChangeMessage.args[3] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
	    mChangeMessage.universe = universe;
	    VirtualUniverse.mc.processMessage(mChangeMessage);

	    if (boundsAutoCompute) {
		GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;
		// Compute the bounds once
		mga.incrComputeGeoBounds();// This compute the bbox if dirty
		mga.decrComputeGeoBounds();
	    }
	}



    }

    /**
     * Retrieves the geometryArrays component of this Morph node.
     * @param index the index of GeometryArray to be returned
     * @return the geometryArray component of this morph node
     */
    GeometryArray getGeometryArray(int index) {
        return (GeometryArray)this.geometryArrays[index].source;
    }

    /**
     * Sets the appearance component of this Morph node.
     * @param appearance the new apearance component for this morph node
     */
    void setAppearance(Appearance newAppearance) {
	boolean visibleIsDirty = false;

        if (((Morph)this.source).isLive()) {

	    if (appearance != null) {
		this.appearance.clearLive(refCount);
		for (int i=mirrorShape3D.size()-1; i>=0; i--) {
		    this.appearance.removeAMirrorUser(
					(Shape3DRetained)mirrorShape3D.get(i));
		}
	    }

	    if (newAppearance != null) {
		((AppearanceRetained)newAppearance.retained).setLive(inBackgroundGroup, refCount);
		appearance = ((AppearanceRetained)newAppearance.retained);
		int size= mirrorShape3D.size();
		for (int i=0; i<size; i++) {
		    appearance.addAMirrorUser((Shape3DRetained)mirrorShape3D.get(i));
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

	    // Send a message
	    int size = 0;

	    if (visibleIsDirty)
		size = 2;
	    else
		size = 1;
	    J3dMessage[] createMessage = new J3dMessage[size];
	    createMessage[0] = new J3dMessage();
	    createMessage[0].threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
		J3dThread.UPDATE_RENDER;
	    createMessage[0].type = J3dMessage.MORPH_CHANGED;
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
	    createMessage[0].args[4] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
	    if(visibleIsDirty) {
		createMessage[1] = new J3dMessage();
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
	else {
	    if (newAppearance == null) {
		appearance = null;
	    } else {
		appearance = (AppearanceRetained) newAppearance.retained;
	    }
	}
    }

    /**
     * Retrieves the morph node's appearance component.
     * @return the morph node's appearance
     */
    Appearance getAppearance() {
        return (appearance == null ? null :
		(Appearance) this.appearance.source);
    }

    void setAppearanceOverrideEnable(boolean flag) {
        if (((Morph)this.source).isLive()) {

	    // Send a message
	    J3dMessage createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
		J3dThread.UPDATE_RENDER;;
	    createMessage.type = J3dMessage.MORPH_CHANGED;
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
	    createMessage.args[4] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
	    VirtualUniverse.mc.processMessage(createMessage);
	}
	appearanceOverrideEnable = flag;
    }

    boolean getAppearanceOverrideEnable() {
	return appearanceOverrideEnable;
    }

    boolean intersect(PickInfo pickInfo, PickShape pickShape, int flags ) {

        Transform3D localToVworld = pickInfo.getLocalToVWorldRef();

	Transform3D vworldToLocal = new Transform3D();
	vworldToLocal.invert(localToVworld);
	PickShape newPS = pickShape.transform(vworldToLocal);

	GeometryRetained geo = (GeometryRetained) (morphedGeometryArray.retained);

        if (geo.mirrorGeometry != null) {
            geo = geo.mirrorGeometry;
        }

        if (((flags & PickInfo.CLOSEST_INTERSECTION_POINT) == 0) &&
                ((flags & PickInfo.CLOSEST_DISTANCE) == 0) &&
                ((flags & PickInfo.CLOSEST_GEOM_INFO) == 0) &&
                ((flags & PickInfo.ALL_GEOM_INFO) == 0)) {
            return geo.intersect(newPS, null, 0, null, null, 0);
        } else {
            Point3d closestIPnt = new Point3d();
            Point3d iPnt = new Point3d();
            Point3d iPntVW = new Point3d();

            if (geo.intersect(newPS, pickInfo, flags, iPnt, geo, 0)) {

                iPntVW.set(iPnt);
                localToVworld.transform(iPntVW);
                double distance = pickShape.distance(iPntVW);

                if ((flags & PickInfo.CLOSEST_DISTANCE) != 0) {
                    pickInfo.setClosestDistance(distance);
                }
                if((flags & PickInfo.CLOSEST_INTERSECTION_POINT) != 0) {
                    pickInfo.setClosestIntersectionPoint(iPnt);
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Check if the geometry component of this shape node under path
     *  intersects with the pickShape.
     * @return true if intersected else false. If return is true, dist
     *  contains the closest distance of intersection if it is not
     *  equal to null.
     */
    boolean intersect(SceneGraphPath path,
            PickShape pickShape, double[] dist) {

	// This method will not do bound intersect check, as it assume caller
	// has already done that. ( For performance and code simplification
	// reasons. )

        int flags;
        PickInfo pickInfo = new PickInfo();

        Transform3D localToVworld = path.getTransform();
	if (localToVworld == null) {
	    throw new RuntimeException(J3dI18N.getString("MorphRetained5"));
	}

        pickInfo.setLocalToVWorldRef( localToVworld);
        //System.err.println("MorphRetained.intersect() : ");
        if (dist == null) {
            //System.err.println("      no dist request ....");
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
     * Sets the Morph node's weight vector
     * @param wieghts the new vector of weights for the morph node
     */
     void setWeights(double weights[]) {
	int i;
	double sum= 0.0;

	if (weights.length != numGeometryArrays)
	    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained7"));

	for (i=weights.length-1; i>=0; i--)  {
	    sum += weights[i];
	}

	if (Math.abs(sum - 1.0) > TOLERANCE)
	    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained8"));

	// Weights array is ALWAYS malloced in setGeometryArrays method
	for (i=numGeometryArrays-1; i>=0; i--)
	    this.weights[i] = weights[i];


	if (source.isLive()) {
	    ((GeometryArrayRetained)morphedGeometryArray.retained).updateData(this);
	    J3dMessage mChangeMessage = null;
	    mChangeMessage = new J3dMessage();
	    mChangeMessage.type = J3dMessage.MORPH_CHANGED;
	    mChangeMessage.threads = (J3dThread.UPDATE_GEOMETRY |
				      J3dThread.UPDATE_TRANSFORM);
	    // If its a indexed geometry array, unindexify in renderBin
	    if (this.geometryArrays[0] instanceof IndexedGeometryArrayRetained)
		mChangeMessage.threads |= J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	    mChangeMessage.args[0] = this;
	    mChangeMessage.args[1]= new Integer(GEOMETRY_CHANGED);
	    mChangeMessage.args[3] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
	    mChangeMessage.universe = universe;
	    VirtualUniverse.mc.processMessage(mChangeMessage);
	}

    }

    /**
     * Retrieves the Morph node's weight vector
     * @return the morph node's weight vector.
     */
    double[] getWeights() {
	return (double[]) weights.clone();
    }

    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    Bounds getBounds() {
        if(boundsAutoCompute) {
            GeometryArrayRetained mga =
		(GeometryArrayRetained)morphedGeometryArray.retained;
            if (mga != null) {
                synchronized(mga.geoBounds) {
                    return (Bounds) mga.geoBounds.clone();
                }
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
	    GeometryArrayRetained mga =
		(GeometryArrayRetained)morphedGeometryArray.retained;
	    if (mga != null) {
		synchronized(mga.geoBounds) {
		    bounds.combine((Bounds) mga.geoBounds);
		}
	    }
	} else {
	    // Should this be lock too ? ( MT safe  ? )
	    synchronized(localBounds) {
		bounds.combine((Bounds) localBounds);
	    }
	}
    }

    // Return the number of geometry arrays in this MorphRetained object.
    int getNumGeometryArrays() {
	return numGeometryArrays;
    }

    // If the geometry of a morph changes, make sure that the
    // validVertexCount has not changed
    void updateMorphedGeometryArray(GeometryArrayRetained geo, boolean coordinatesChanged) {
	if (numGeometryArrays > 0) {
	    // check if not the first geo, then compare with the first geometry
	    if (geometryArrays[0] != geo) {
		doErrorCheck(geo, geometryArrays[0]);
	    }
	    else {
		// if first geo, compare with the second geo
		if (numGeometryArrays > 1) {
		    doErrorCheck(geo, geometryArrays[1]);
		}

	    }
	}


	((GeometryArrayRetained)morphedGeometryArray.retained).updateData(this);
	// Compute the bounds once
	if (boundsAutoCompute && coordinatesChanged) {
	    GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;
	    mga.incrComputeGeoBounds();  // This compute the bbox if dirty
	    mga.decrComputeGeoBounds();
	}
    }

    /**
     * Update GeometryArray computed by morphing input GeometryArrays
     * with weights
     */
    public void updateData(Geometry mga) {

	int i,j,k, vFormat, geoType, stripVCount[];
	int iCount = 0;
	int numStrips = 0;
	int texCoordSetCount = 0;
	float coord[] = new float[3], color[] = new float[4],
	    normal[] = new float[3], texCoord[] = new float[3];

	vFormat = geometryArrays[0].vertexFormat;
	geoType = ((GeometryArrayRetained)geometryArrays[0]).geoType;
	texCoordSetCount = geometryArrays[0].getTexCoordSetCount();



	int vc = 0, nc = 0, cc = 0, n = 0;
	int count = 0;
	if (geometryArrays[0] instanceof IndexedGeometryArrayRetained){
	     count = geometryArrays[0].getNumCoordCount();
	} else {
	     count = geometryArrays[0].validVertexCount;
	}

	for (i=0; i < count; i++) {
	    Mcoord[vc++] = Mcoord[vc++] = Mcoord[vc++] = 0.0f;
	}


	if ((vFormat & GeometryArray.COLOR) != 0) {
	    if (geometryArrays[0] instanceof IndexedGeometryArrayRetained){
		count = geometryArrays[0].getNumColorCount();
	    } else {
		count = geometryArrays[0].validVertexCount;
	    }
	    for (i=0; i < count; i++) {
		if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_3)
		    Mcolor[cc++] = Mcolor[cc++] = Mcolor[cc++] = 0.0f;

		else if ((vFormat & GeometryArray.COLOR_4) == GeometryArray.COLOR_4)
		    Mcolor[cc++] = Mcolor[cc++] = Mcolor[cc++] = Mcolor[cc++] = 0.0f;
	    }
	}


	if ((vFormat & GeometryArray.NORMALS) != 0) {
	    if (geometryArrays[0] instanceof IndexedGeometryArrayRetained){
		count = geometryArrays[0].getNumNormalCount();
	    } else {
		count = geometryArrays[0].validVertexCount;
	    }
	    for (i=0; i < count; i++) {
		Mnormal[nc++] = Mnormal[nc++] = Mnormal[nc++] = 0.0f;
	    }
	}

	if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	    for (k = 0; k < texCoordSetCount; k++) {
		if (geometryArrays[0] instanceof IndexedGeometryArrayRetained){
		    count = geometryArrays[0].getNumTexCoordCount(k);
		} else {
		    count = geometryArrays[0].validVertexCount;
		}
		int tcount = 0;
		for (i=0; i < count; i++) {
		    MtexCoord[k][tcount++] = MtexCoord[k][tcount++] = 0.0f;
		    if ((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
			MtexCoord[k][tcount++] = 0.0f;
		    } else if ((vFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
			MtexCoord[k][tcount++] = 0.0f;
			MtexCoord[k][tcount++] = 0.0f;
		    }
		}
	    }
	}
	// If by copy, then ...
	if ((vFormat & GeometryArray.BY_REFERENCE) == 0) {
	    count = 0;
	    for (j=0;j < numGeometryArrays;j++) {
		double w = weights[j];
		if (w != 0) {
		    vc = 0; nc = 0; cc = 0;
		    int initialVertex = 0;
		    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
			initialVertex = 0;
			count =  geometryArrays[j].getNumCoordCount();
		    }
		    else {
			initialVertex = geometryArrays[j].getInitialVertexIndex();
			count = geometryArrays[j].validVertexCount;
		    }
		    int endVertex = initialVertex + count;
		    for (i=initialVertex; i< endVertex; i++) {
			geometryArrays[j].getCoordinate(i, coord);
			Mcoord[vc++] += coord[0]*w;
			Mcoord[vc++] += coord[1]*w;
			Mcoord[vc++] += coord[2]*w;
		    }

		    if ((vFormat & GeometryArray.COLOR) != 0) {
			if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
			    count =  geometryArrays[j].getNumColorCount();
			}
			endVertex = initialVertex + count;
			for (i=initialVertex; i<  endVertex; i++) {
			    geometryArrays[j].getColor(i, color);
			    Mcolor[cc++] += color[0]*w;
			    Mcolor[cc++] += color[1]*w;
			    Mcolor[cc++] += color[2]*w;
			    if ((vFormat & GeometryArray.WITH_ALPHA) != 0)
				Mcolor[cc++] += color[3]*w;
			}
		    }
		    if ((vFormat & GeometryArray.NORMALS) != 0) {
			if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
			    count =  geometryArrays[j].getNumNormalCount();
			}
			endVertex = initialVertex + count;
			for (i=initialVertex; i<  endVertex; i++) {
			    geometryArrays[j].getNormal(i, normal);
			    Mnormal[nc++] += normal[0]*w;
			    Mnormal[nc++] += normal[1]*w;
			    Mnormal[nc++] += normal[2]*w;
			}
		    }

		    if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
			for (k = 0; k < texCoordSetCount; k++) {
			    int tcount = 0;
			    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				count =  geometryArrays[j].getNumTexCoordCount(i);
			    }
			    endVertex = initialVertex + count;
			    for (i=initialVertex; i<  endVertex; i++) {
				geometryArrays[j].getTextureCoordinate(k, i, texCoord);
				MtexCoord[k][tcount++] += texCoord[0]*w;
				MtexCoord[k][tcount++] += texCoord[1]*w;
				if ((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
				    MtexCoord[k][tcount++] += texCoord[2]*w;
				} else if ((vFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
				    MtexCoord[k][tcount++] += texCoord[2]*w;
				    MtexCoord[k][tcount++] += texCoord[3]*w;
				}
			    }
			}
		    }
		}
	    }
	}
	else {
	    int vIndex, tIndex, cIndex, nIndex, tstride = 0, cstride = 0;
	    if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
		if ((vFormat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
		    tstride = 2;
		} else if ((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
		    tstride = 3;
		} else {
		    tstride = 4;
		}
	    }

	    if ((vFormat & GeometryArray.COLOR) != 0) {
		cstride = 3;
		if ((vFormat & GeometryArray.WITH_ALPHA) != 0)
		    cstride = 4;
	    }

	    if ((vFormat & GeometryArray.INTERLEAVED) != 0) {
		float[] vdata;
		int stride;

		stride = geometryArrays[0].stride();
		int coffset = geometryArrays[0].colorOffset();
		int noffset = geometryArrays[0].normalOffset();
		int voffset = geometryArrays[0].coordinateOffset();
		int offset = 0;

		int initialVertex = 0;
		for (j=0;j < numGeometryArrays;j++) {
		    double w = weights[j];
		    if (w != 0) {
			vc = 0; nc = 0; cc = 0; n = 0;
			vdata = geometryArrays[j].getInterleavedVertices();
			if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
			    for (k = 0; k < texCoordSetCount; k++) {
				if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				    tIndex = 0;
				    count =  geometryArrays[j].getNumCoordCount();
				}
				else {
				    tIndex = geometryArrays[j].getInitialVertexIndex();
				    count = geometryArrays[j].validVertexCount;
				}
				offset = (tIndex * stride)+k*tstride;
				int tcount = 0;
				for (i = 0; i < count; i++, offset += stride) {
				    MtexCoord[k][tcount++] += vdata[offset] * w;
				    MtexCoord[k][tcount++] += vdata[offset+1] * w;
				    if ((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
					MtexCoord[k][tcount++] += vdata[offset+2]*w;
				    } else if ((vFormat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
					MtexCoord[k][tcount++] += vdata[offset+2]*w;
					MtexCoord[k][tcount++] += vdata[offset+3]*w;
				    }
				}
			    }

			}
			if ((vFormat & GeometryArray.COLOR) != 0) {
			    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				cIndex = 0;
				count =  geometryArrays[j].getNumCoordCount();
			    }
			    else {
				cIndex = geometryArrays[j].getInitialVertexIndex();
				count = geometryArrays[j].validVertexCount;
			    }
			    offset = (cIndex * stride)+coffset;
			    for (i = 0; i < count; i++, offset += stride) {
				Mcolor[cc++] += vdata[offset]*w;
				Mcolor[cc++] += vdata[offset+1]*w;
				Mcolor[cc++] += vdata[offset+2]*w;
				if ((vFormat & GeometryArray.WITH_ALPHA)!= 0)
				    Mcolor[cc++] += vdata[offset+3]*w;

			    }
			}

			if ((vFormat & GeometryArray.NORMALS) != 0) {
			    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				nIndex = 0;
				count =  geometryArrays[j].getNumCoordCount();
			    }
			    else {
				nIndex = geometryArrays[j].getInitialVertexIndex();
				count = geometryArrays[j].validVertexCount;
			    }
			    offset = (nIndex * stride)+noffset;
			    for (i = 0; i < count; i++, offset += stride) {
				Mnormal[nc++] += vdata[offset]*w;
				Mnormal[nc++] += vdata[offset+1]*w;
				Mnormal[nc++] += vdata[offset+2]*w;
			    }
			}
			if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
			    vIndex = 0;
			    count =  geometryArrays[j].getNumCoordCount();
			}
			else {
			    vIndex = geometryArrays[j].getInitialVertexIndex();
			    count = geometryArrays[j].validVertexCount;
			}
			offset = (vIndex * stride)+voffset;
			for (i = 0; i < count; i++, offset += stride) {
			    Mcoord[vc++] += vdata[offset]*w;
			    Mcoord[vc++] += vdata[offset+1]*w;
			    Mcoord[vc++] += vdata[offset+2]*w;

			}
		    }
		}
	    }
	    else {
		float byteToFloatScale = 1.0f/255.0f;
		for (j=0;j < numGeometryArrays;j++) {
		    double w = weights[j];
		    if (w != 0) {
			if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
			    switch ((geometryArrays[j].vertexType & GeometryArrayRetained.TEXCOORD_DEFINED)) {
			    case GeometryArrayRetained.TF:
				for (k = 0; k < texCoordSetCount; k++) {
				    float[] tf = geometryArrays[j].getTexCoordRefFloat(k);
				    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
					tIndex = 0;
					count =  geometryArrays[j].getNumTexCoordCount(k);
				    }
				    else {
					tIndex = geometryArrays[j].getInitialTexCoordIndex(k);
					count =  geometryArrays[j].validVertexCount;
				    }
				    tIndex *= tstride;
				    int tcount = 0;
				    for (i=0; i< count; i++) {
					MtexCoord[k][tcount++] += tf[tIndex++]*w;
					MtexCoord[k][tcount++] += tf[tIndex++]*w;
					if ((vFormat & GeometryArray.TEXTURE_COORDINATE_3) != 0)
					    MtexCoord[k][tcount++] += tf[tIndex++]*w;
				    }
				}
				break;
			    case GeometryArrayRetained.T2F:
				for (k = 0; k < texCoordSetCount; k++) {
				    int tcount = 0;
				    float[] tf = geometryArrays[j].getTexCoordRefFloat(k);
				    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
					tIndex = 0;
					count =  geometryArrays[j].getNumTexCoordCount(k);
				    }
				    else {
					tIndex = geometryArrays[j].getInitialTexCoordIndex(k);
					count =  geometryArrays[j].validVertexCount;
				    }
				    TexCoord2f[] t2f = geometryArrays[j].getTexCoordRef2f(k);
				    for (i=0; i< count; i++, tIndex++) {
					MtexCoord[k][tcount++] += t2f[tIndex].x*w;
					MtexCoord[k][tcount++] += t2f[tIndex].y*w;
				    }
				}
				break;
			    case GeometryArrayRetained.T3F:
				for (k = 0; k < texCoordSetCount; k++) {
				    int tcount = 0;
				    TexCoord3f[] t3f = geometryArrays[j].getTexCoordRef3f(k);
				    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
					tIndex = 0;
					count =  geometryArrays[j].getNumTexCoordCount(k);
				    }
				    else {
					tIndex = geometryArrays[j].getInitialTexCoordIndex(k);
					count =  geometryArrays[j].validVertexCount;
				    }
				    for (i=0; i< count; i++, tIndex++) {
					MtexCoord[k][tcount++] += t3f[tIndex].x*w;
					MtexCoord[k][tcount++] += t3f[tIndex].y*w;
					MtexCoord[k][tcount++] += t3f[tIndex].z*w;
				    }
				}
				break;

			    }
			}
			if ((vFormat & GeometryArray.COLOR) != 0) {
			    double val = byteToFloatScale * w;
			    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				cIndex = 0;
				count =  geometryArrays[j].getNumColorCount();
			    }
			    else {
				cIndex = geometryArrays[j].getInitialColorIndex();
				count =  geometryArrays[j].validVertexCount;
			    }

			    switch ((geometryArrays[j].vertexType & GeometryArrayRetained.COLOR_DEFINED)) {
			    case GeometryArrayRetained.CF:
				float[] cf = geometryArrays[j].getColorRefFloat();
				cc = 0;
				cIndex *= cstride;
				for (i=0; i< count; i++) {
				    Mcolor[cc++] += cf[cIndex++]*w;
				    Mcolor[cc++] += cf[cIndex++]*w;
				    Mcolor[cc++] += cf[cIndex++]*w;
				    if ((vFormat & GeometryArray.WITH_ALPHA)!= 0)
					Mcolor[cc++] += cf[cIndex++]*w;
				}
				break;
			    case GeometryArrayRetained.CUB:
				byte[] cub = geometryArrays[j].getColorRefByte();
				cc = 0;
				cIndex *= cstride;
				for (i=0; i< count; i++) {
				    Mcolor[cc++] += (cub[cIndex++] & 0xff) * val;
				    Mcolor[cc++] += (cub[cIndex++] & 0xff) *val;
				    Mcolor[cc++] += (cub[cIndex++] & 0xff) *val;
				    if ((vFormat & GeometryArray.WITH_ALPHA)!= 0)
					Mcolor[cc++] += (cub[cIndex++] & 0xff) *val;
				}

				break;
			    case GeometryArrayRetained.C3F:
				Color3f[] c3f = geometryArrays[j].getColorRef3f();
				cc = 0;
				for (i=0; i< count; i++, cIndex++) {
				    Mcolor[cc++] += c3f[cIndex].x * w;
				    Mcolor[cc++] += c3f[cIndex].y * w;
				    Mcolor[cc++] += c3f[cIndex].z * w;
				}
				break;
			    case GeometryArrayRetained.C4F:
				Color4f[] c4f = geometryArrays[j].getColorRef4f();
				cc = 0;
				for (i=0; i< count; i++, cIndex++) {
				    Mcolor[cc++] += c4f[cIndex].x * w;
				    Mcolor[cc++] += c4f[cIndex].y * w;
				    Mcolor[cc++] += c4f[cIndex].z * w;
				    Mcolor[cc++] += c4f[cIndex].w * w;
				}
				break;
			    case GeometryArrayRetained.C3UB:
				Color3b[] c3b = geometryArrays[j].getColorRef3b();
				cc = 0;
				for (i=0; i< count; i++, cIndex++) {
				    Mcolor[cc++] += (c3b[cIndex].x  & 0xff)* val;
				    Mcolor[cc++] += (c3b[cIndex].y  & 0xff) * val;
				    Mcolor[cc++] += (c3b[cIndex].z & 0xff) * val;
				}
				break;
			    case GeometryArrayRetained.C4UB:
				Color4b[] c4b = geometryArrays[j].getColorRef4b();
				cc = 0;
				for (i=0; i< count; i++, cIndex++) {
				    Mcolor[cc++] += (c4b[cIndex].x  & 0xff)* val;
				    Mcolor[cc++] += (c4b[cIndex].y  & 0xff) * val;
				    Mcolor[cc++] += (c4b[cIndex].z & 0xff) * val;
				    Mcolor[cc++] += (c4b[cIndex].w & 0xff) * val;
				}
				break;

			    }
			}
			if ((vFormat & GeometryArray.NORMALS) != 0) {
			    nc = 0;
			    if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
				nIndex = 0;
				count =  geometryArrays[j].getNumNormalCount();
			    }
			    else {
				nIndex = geometryArrays[j].getInitialNormalIndex();
				count =  geometryArrays[j].validVertexCount;
			    }
			    switch ((geometryArrays[j].vertexType & GeometryArrayRetained.NORMAL_DEFINED)) {
			    case GeometryArrayRetained.NF:
				float[] nf = geometryArrays[j].getNormalRefFloat();
				nIndex *= 3;
				for (i=0; i< count; i++) {
				    Mnormal[nc++] += nf[nIndex++]*w;
				    Mnormal[nc++] += nf[nIndex++]*w;
				    Mnormal[nc++] += nf[nIndex++]*w;
				}
				break;
			    case GeometryArrayRetained.N3F:
				Vector3f[] n3f = geometryArrays[j].getNormalRef3f();
				for (i=0; i< count; i++, nIndex++) {
				    Mnormal[nc++] += n3f[nIndex].x*w;
				    Mnormal[nc++] += n3f[nIndex].y*w;
				    Mnormal[nc++] += n3f[nIndex].z*w;
				}
				break;
			    }
			}
			// Handle vertices ..
			vc = 0;
			if (geometryArrays[j] instanceof IndexedGeometryArrayRetained) {
			    vIndex = 0;
			    count =  geometryArrays[j].getNumCoordCount();
			}
			else {
			    vIndex = geometryArrays[j].getInitialCoordIndex();
			    count =  geometryArrays[j].validVertexCount;
			}
			switch ((geometryArrays[j].vertexType & GeometryArrayRetained.VERTEX_DEFINED)) {
			case GeometryArrayRetained.PF:
			    float[] pf = geometryArrays[j].getCoordRefFloat();
			    vIndex *= 3;
			    for (i=0; i< count; i++) {
				Mcoord[vc++] += pf[vIndex++]*w;
				Mcoord[vc++] += pf[vIndex++]*w;
				Mcoord[vc++] += pf[vIndex++]*w;
			    }
			    break;
			case GeometryArrayRetained.PD:
			    double[] pd = geometryArrays[j].getCoordRefDouble();
			    vIndex *= 3;
			    for (i=0; i< count; i++) {
				Mcoord[vc++] += (float)pd[vIndex++]*w;
				Mcoord[vc++] += (float)pd[vIndex++]*w;
				Mcoord[vc++] += (float)pd[vIndex++]*w;
			    }
			    break;
			case GeometryArrayRetained.P3F:
			    Point3f[] p3f = geometryArrays[j].getCoordRef3f();
			    for (i=0; i< count; i++, vIndex++) {
				Mcoord[vc++] += p3f[vIndex].x*w;
				Mcoord[vc++] += p3f[vIndex].y*w;
				Mcoord[vc++] += p3f[vIndex].z*w;
			    }
			    break;
			case GeometryArrayRetained.P3D:
			    Point3d[] p3d = geometryArrays[j].getCoordRef3d();
			    for (i=0; i< count; i++, vIndex++) {
				Mcoord[vc++] += (float)p3d[vIndex].x*w;
				Mcoord[vc++] += (float)p3d[vIndex].y*w;
				Mcoord[vc++] += (float)p3d[vIndex].z*w;
			    }
			    break;

			}

		    }
		}
	    }
	}

	GeometryArrayRetained mgaR =
	    (GeometryArrayRetained)mga.retained;

	mgaR.setCoordRefFloat(Mcoord);

	if ((vFormat & GeometryArray.COLOR) != 0)
	    mgaR.setColorRefFloat(Mcolor);

	// *******Need to normalize normals
	if ((vFormat & GeometryArray.NORMALS) != 0)
	    mgaR.setNormalRefFloat(Mnormal);

	if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
    	    for (k = 0; k < texCoordSetCount; k++) {
		mgaR.setTexCoordRefFloat(k, MtexCoord[k]);
	    }
	}
    }

    void updateImmediateMirrorObject(Object[] objs) {
	int i;

	int component = ((Integer)objs[1]).intValue();
	Shape3DRetained[] msArr = (Shape3DRetained[]) objs[2];
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
	    System.err.println("ChangedFrequent = "+changedFrequent);
	    for ( i = msArr.length-1; i >=0; i--) {
		msArr[i].appearanceOverrideEnable = ((Boolean)arg[0]).booleanValue();
		msArr[i].changedFrequent = val;
	    }
	}
    }

    /**
     * assign a name to this node when it is made live.
     */
    void setLive(SetLiveState s) {
	int i, j;
	Shape3DRetained shape;
	ArrayList msList = new ArrayList();
        GeometryAtom ga;
	int oldrefCount = refCount;

	super.doSetLive(s);
	nodeId = universe.getNodeId();


	for (i = 0; i < numGeometryArrays; i++) {
	    synchronized(geometryArrays[i].liveStateLock) {
		geometryArrays[i].setLive(inBackgroundGroup, s.refCount);
		// Add this morph object as user the first time
		if (oldrefCount <= 0) {
		    geometryArrays[i].addMorphUser(this);
		}
	    }
	}

	if (this.morphedGeometryArray == null){
	    initMorphedGeometry();

	}
	((GeometryArrayRetained)(morphedGeometryArray.retained)).setLive(inBackgroundGroup, s.refCount);

	if (boundsAutoCompute) {
	    GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;
	    // Compute the bounds once
	    mga.incrComputeGeoBounds(); // This compute the bbox if dirty
	    mga.decrComputeGeoBounds();
	    localBounds.setWithLock(mga.geoBounds);
	}


	if (inSharedGroup) {
	    for (i=0; i<s.keys.length; i++) {
		shape = new Shape3DRetained();
		shape.key = s.keys[i];
		shape.localToVworld = new Transform3D[1][];
		shape.localToVworldIndex = new int[1][];


		j = s.keys[i].equals(localToVworldKeys, 0,
				     localToVworldKeys.length);
		if(j < 0) {
		    System.err.println("MorphRetained : Can't find hashKey");
		}

		shape.localToVworld[0] = localToVworld[j];
		shape.localToVworldIndex[0] = localToVworldIndex[j];
		shape.branchGroupPath = (BranchGroupRetained []) branchGroupPaths.get(j);
		shape.isPickable = s.pickable[i];
		shape.isCollidable = s.collidable[i];

		shape.initMirrorShape3D(s, this, j);
		mirrorShape3D.add(j, shape);

		msList.add(shape);
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

		if (s.viewLists != null)
		    shape.viewList = (ArrayList)s.viewLists.get(i);
		else
		    shape.viewList = null;

		//		((GeometryArrayRetained)(morphedGeometryArray.retained)).addUser(shape);


                ga = Shape3DRetained.getGeomAtom(shape);

		// Add the geometry atom for this shape to the Targets
		s.nodeList.add(ga);

                if (s.transformTargets != null &&
                                s.transformTargets[i] != null) {
		    s.transformTargets[i].addNode(ga, Targets.GEO_TARGETS);
		}
                if (s.switchTargets != null &&
                        s.switchTargets[i] != null) {
		    s.switchTargets[i].addNode(shape, Targets.GEO_TARGETS);
                    shape.closestSwitchParent = s.closestSwitchParents[i];
                    shape.closestSwitchIndex = s.closestSwitchIndices[i];
                }
        	shape.switchState = (SwitchState)s.switchStates.get(j);

	    }
	} else {
	    shape = new Shape3DRetained();
	    shape.localToVworld = new Transform3D[1][];
	    shape.localToVworldIndex = new int[1][];
	    shape.localToVworld[0] = this.localToVworld[0];
	    shape.localToVworldIndex[0] = this.localToVworldIndex[0];
	    shape.branchGroupPath = (BranchGroupRetained []) branchGroupPaths.get(0);
	    shape.isPickable = s.pickable[0];
	    shape.isCollidable = s.collidable[0];
	    shape.initMirrorShape3D(s, this, 0);
	    mirrorShape3D.add(shape);

	    msList.add(shape);
	    // Add any scoped lights to the mirror shape
	    if (s.lights != null) {
		ArrayList l = (ArrayList)s.lights.get(0);
		if (l != null) {
		    for (int m = 0; m < l.size(); m++) {
			shape.addLight((LightRetained)l.get(m));
		    }
		}
	    }

	    // Add any scoped fog
	    if (s.fogs != null) {
		ArrayList l = (ArrayList)s.fogs.get(0);
		if (l != null) {
		    for (int m = 0; m < l.size(); m++) {
			shape.addFog((FogRetained)l.get(m));
		    }
		}
	    }

	    // Add any scoped modelClip
	    if (s.modelClips != null) {
		ArrayList l = (ArrayList)s.modelClips.get(0);
		if (l != null) {
		    for (int m = 0; m < l.size(); m++) {
			shape.addModelClip((ModelClipRetained)l.get(m));
		    }
		}
	    }

	    // Add any scoped alt app
	    if (s.altAppearances != null) {
		ArrayList l = (ArrayList)s.altAppearances.get(0);
		if (l != null) {
		    for (int m = 0; m < l.size(); m++) {
			shape.addAltApp((AlternateAppearanceRetained)l.get(m));
		    }
		}
	    }

	    if (s.viewLists != null)
		shape.viewList = (ArrayList)s.viewLists.get(0);
	    else
		shape.viewList = null;

	    //	    ((GeometryArrayRetained)(morphedGeometryArray.retained)).addUser(shape);

            ga = Shape3DRetained.getGeomAtom(shape);

    	    // Add the geometry atom for this shape to the Targets
	    s.nodeList.add(ga);

            if (s.transformTargets != null &&
                                s.transformTargets[0] != null) {
		s.transformTargets[0].addNode(ga, Targets.GEO_TARGETS);
	    }
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
		s.switchTargets[0].addNode(shape, Targets.GEO_TARGETS);
                shape.closestSwitchParent = s.closestSwitchParents[0];
                shape.closestSwitchIndex = s.closestSwitchIndices[0];
            }
            shape.switchState = (SwitchState)s.switchStates.get(0);
	}
	if (appearance != null) {
	    synchronized(appearance.liveStateLock) {
		appearance.setLive(inBackgroundGroup, s.refCount);
		appearance.initMirrorObject();
		if (appearance.renderingAttributes != null)
		    visible = appearance.renderingAttributes.visible;
		for (int k = 0; k < msList.size(); k++) {
		    Shape3DRetained sh = (Shape3DRetained)msList.get(k);
		    sh.appearance = (AppearanceRetained)appearance.mirror;
		    appearance.addAMirrorUser(sh);
		}
	    }

	}
	else {
	    for (int k = 0; k < msList.size(); k++) {
		Shape3DRetained sh = (Shape3DRetained)msList.get(k);
		sh.appearance = null;
	    }
	}

	s.notifyThreads |= (J3dThread.UPDATE_GEOMETRY |
			    J3dThread.UPDATE_TRANSFORM |
			    J3dThread.UPDATE_RENDER |
			    J3dThread.UPDATE_RENDERING_ATTRIBUTES);

	// Need to clone the geometry , if its indexed ...
	if (refCount == 1 && this.geometryArrays[0] instanceof IndexedGeometryArrayRetained) {
	    J3dMessage mChangeMessage = new J3dMessage();
	    mChangeMessage.type = J3dMessage.MORPH_CHANGED;
	    mChangeMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	    mChangeMessage.args[0] = this;
	    mChangeMessage.args[1]= new Integer(GEOMETRY_CHANGED);
	    mChangeMessage.universe = universe;
	    VirtualUniverse.mc.processMessage(mChangeMessage);
	}
	super.markAsLive();

    }


    /**
     * assign a name to this node when it is made live.
     */
    void clearLive(SetLiveState s) {
	int i, j;
	Shape3DRetained shape;
	Object[] shapes;
	ArrayList appList = new ArrayList();
	GeometryAtom ga;

	super.clearLive(s);

	for (i = 0; i < numGeometryArrays; i++) {
	    synchronized(geometryArrays[i].liveStateLock) {
		geometryArrays[i].clearLive(s.refCount);
		// Remove this morph object as user, when the last branch
		// is clearlived
		if (refCount <= 0) {
		    geometryArrays[i].removeMorphUser(this);
		}
	    }
	}
	GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;

	mga.clearLive( s.refCount);

	if (inSharedGroup) {
	    shapes = mirrorShape3D.toArray();
	    for (i=0; i<s.keys.length; i++) {
		for (j=0; j<shapes.length; j++) {
		    shape = (Shape3DRetained)shapes[j];
		    if (shape.key.equals(s.keys[i])) {
			// clearMirrorShape(shape);
			mirrorShape3D.remove(j);
                	if (s.switchTargets != null &&
                        	s.switchTargets[i] != null) {
                            s.switchTargets[i].addNode(shape,
                                                        Targets.GEO_TARGETS);
                        }
			if (appearance != null)
			    appList.add(shape);

			//			((GeometryArrayRetained)(morphedGeometryArray.retained)).removeUser(shape);
                        ga = Shape3DRetained.getGeomAtom(shape);

			// Add the geometry atoms for this shape to the Targets
			s.nodeList.add(ga);
                        if (s.transformTargets != null &&
                                s.transformTargets[i] != null) {
                            s.transformTargets[i].addNode(ga,
							  Targets.GEO_TARGETS);
                        }
		    }
		}
	    }
	} else {
	    // Only entry 0 is valid
	    shape = (Shape3DRetained)mirrorShape3D.get(0);
	    // clearMirrorShape(shape);
	    mirrorShape3D.remove(0);
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(shape, Targets.GEO_TARGETS);
            }
	    if (appearance != null)
		appList.add(shape);

	    //	    ((GeometryArrayRetained)(morphedGeometryArray.retained)).removeUser(shape);
            ga = Shape3DRetained.getGeomAtom(shape);

	    // Add the geometry atom for this shape to the Targets
	    s.nodeList.add(ga);
            if (s.transformTargets != null &&
                                s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ga, Targets.GEO_TARGETS);
            }
	}
	if (appearance != null) {
	    synchronized(appearance.liveStateLock) {
		appearance.clearLive(s.refCount);
		for (int k = 0; k < appList.size(); k++) {
		    appearance.removeAMirrorUser((Shape3DRetained)appList.get(k));
		}
	    }
	}

	s.notifyThreads |= (J3dThread.UPDATE_GEOMETRY |
			    J3dThread.UPDATE_TRANSFORM |
			    // This is used to clear the scope info
			    // of all the mirror shapes
			    J3dThread.UPDATE_RENDERING_ENVIRONMENT |
			    J3dThread.UPDATE_RENDER);

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

    Shape3DRetained getMirrorShape(SceneGraphPath path) {
	if (!inSharedGroup) {
	    return (Shape3DRetained) mirrorShape3D.get(0);
	}
	HashKey key = new HashKey("");
	path.getHashKey(key);
	return getMirrorShape(key);
    }

    Shape3DRetained getMirrorShape(HashKey key) {
	int i = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	if (i>=0) {
	    return (Shape3DRetained) mirrorShape3D.get(i);
	}

	// Not possible
	throw new RuntimeException("Shape3DRetained: MirrorShape Not found!");
    }

    void getMirrorObjects(ArrayList leafList, HashKey key) {
	Shape3DRetained ms;
	if (inSharedGroup) {
	    ms = getMirrorShape(key);
	}
	else {
	    ms = (Shape3DRetained)mirrorShape3D.get(0);
	}
	GeometryAtom ga = Shape3DRetained.getGeomAtom(ms);
	leafList.add(ga);

    }

    void setBoundsAutoCompute(boolean autoCompute) {
        if (autoCompute != boundsAutoCompute) {
            if (autoCompute) {
                // localBounds may not have been set to bbox
                localBounds = new BoundingBox();
		if (source.isLive() && morphedGeometryArray != null) {
		    GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;
		    mga.incrComputeGeoBounds(); // This compute the bbox if dirty
		    mga.decrComputeGeoBounds();
		}
            }


	    localBounds = getBounds();
            super.setBoundsAutoCompute(autoCompute);
            if (source.isLive()) {
                J3dMessage message = new J3dMessage();
                message.type = J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED;
                message.threads = J3dThread.UPDATE_TRANSFORM |
		                  J3dThread.UPDATE_GEOMETRY |
		                  J3dThread.UPDATE_RENDER;
                message.universe = universe;
                message.args[0] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
		message.args[1] = localBounds;
                VirtualUniverse.mc.processMessage(message);
            }
        }
    }

    void updateBounds() {
	localBounds = getEffectiveBounds();
	if (source.isLive()) {
	    J3dMessage message = new J3dMessage();
	    message.type = J3dMessage.BOUNDS_AUTO_COMPUTE_CHANGED;
	    message.threads = J3dThread.UPDATE_TRANSFORM |
		J3dThread.UPDATE_GEOMETRY |
		J3dThread.UPDATE_RENDER;
                message.universe = universe;
                message.args[0] = Shape3DRetained.getGeomAtomsArray(mirrorShape3D);
		message.args[1] = localBounds;
                VirtualUniverse.mc.processMessage(message);
	}
    }

    /**
     * Initialization of morphed geometry
     */
    void initMorphedGeometry() {
      int  vFormat, geoType, stripVCount[];
      int iCount = 0;
      int numStrips = 0;
      int texCoordSetCount = 0;
      int texCoordSetMapLen = 0;
      int [] texCoordSetMap = null;
      int k;
      GeometryArrayRetained geo = geometryArrays[0];
      vFormat = ((geo.getVertexFormat() | (GeometryArray.BY_REFERENCE)) & ~(GeometryArray.INTERLEAVED)) ;
      texCoordSetCount = geo.getTexCoordSetCount();
      texCoordSetMapLen = geo.getTexCoordSetMapLength();
      if (texCoordSetMapLen > 0) {
          texCoordSetMap = new int[texCoordSetMapLen];
	   geo.getTexCoordSetMap(texCoordSetMap);
      }
      geoType = geo.geoType;

      switch (geoType){
	case GeometryRetained.GEO_TYPE_QUAD_SET:
	    this.morphedGeometryArray =
	     	new QuadArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
				texCoordSetMap);
	    break;
	case GeometryRetained.GEO_TYPE_TRI_SET:
	    this.morphedGeometryArray =
		new TriangleArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
				texCoordSetMap);
	    break;
	case GeometryRetained.GEO_TYPE_POINT_SET:
	    this.morphedGeometryArray =
		new PointArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
				texCoordSetMap);
	    break;
	case GeometryRetained.GEO_TYPE_LINE_SET:
	    this.morphedGeometryArray =
		new LineArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
				texCoordSetMap);
	    break;
	case GeometryRetained.GEO_TYPE_TRI_STRIP_SET:
	    numStrips = ((TriangleStripArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((TriangleStripArrayRetained)geo).getStripVertexCounts(stripVCount);
	    this.morphedGeometryArray =
		new TriangleStripArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
			texCoordSetMap, stripVCount);
	    break;
	case GeometryRetained.GEO_TYPE_TRI_FAN_SET:
	    numStrips = ((TriangleFanArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((TriangleFanArrayRetained)geo).getStripVertexCounts(stripVCount);
	    this.morphedGeometryArray =
		new TriangleFanArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
			texCoordSetMap, stripVCount);
		break;
	case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
	    numStrips = ((LineStripArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((LineStripArrayRetained)geo).getStripVertexCounts(stripVCount);
	    this.morphedGeometryArray =
		new LineStripArray(geometryArrays[0].validVertexCount, vFormat, texCoordSetCount,
			texCoordSetMap, stripVCount);
		break;

	case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    this.morphedGeometryArray =
		new IndexedQuadArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount);
		break;
	case GeometryRetained.GEO_TYPE_INDEXED_TRI_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    this.morphedGeometryArray =
		new IndexedTriangleArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount);
		break;
	case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    this.morphedGeometryArray =
		new IndexedPointArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount);
		break;
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    this.morphedGeometryArray =
		new IndexedLineArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount);
	    break;
	case GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    numStrips = ((IndexedTriangleStripArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((IndexedTriangleStripArrayRetained)geo).getStripIndexCounts(stripVCount);
	    this.morphedGeometryArray =
		new IndexedTriangleStripArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount, stripVCount);break;
	case GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    numStrips = ((IndexedTriangleFanArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((IndexedTriangleFanArrayRetained)geo).getStripIndexCounts(stripVCount);
	    this.morphedGeometryArray =
		new IndexedTriangleFanArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount, stripVCount);break;
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
	    iCount = ((IndexedGeometryArrayRetained)geo).getIndexCount();
	    numStrips = ((IndexedLineStripArrayRetained)geo).getNumStrips();
	    stripVCount = new int[numStrips];
	    ((IndexedLineStripArrayRetained)geo).getStripIndexCounts(stripVCount);
	    this.morphedGeometryArray =
		new IndexedLineStripArray(geometryArrays[0].getNumCoordCount(), vFormat, texCoordSetCount,
			texCoordSetMap, iCount, stripVCount);break;
	}
	if (geometryArrays[0] instanceof IndexedGeometryArrayRetained) {
	    IndexedGeometryArrayRetained igeo = (IndexedGeometryArrayRetained)
		geometryArrays[0];
	    IndexedGeometryArray morphedGeo = (IndexedGeometryArray)
		morphedGeometryArray;
	    if ((vFormat & GeometryArray.COORDINATES) != 0) {
		morphedGeo.setCoordinateIndices(0, igeo.indexCoord);
	    }
	    if ((vFormat & GeometryArray.USE_COORD_INDEX_ONLY) == 0) {
	        if ((vFormat & GeometryArray.NORMALS) != 0) {
	            morphedGeo.setNormalIndices(0, igeo.indexNormal);
	        }
	        if ((vFormat & GeometryArray.COLOR) != 0) {
	            morphedGeo.setColorIndices(0, igeo.indexColor);
	        }
	        if ((vFormat & GeometryArray.TEXTURE_COORDINATE) != 0) {
	            for (k = 0; k < texCoordSetCount; k++) {
	                 morphedGeo.setTextureCoordinateIndices(k, 0,
                                 igeo.indexTexCoord[k]);
	            }
	        }
            }
	}
	this.morphedGeometryArray.setCapability(GeometryArray.ALLOW_REF_DATA_WRITE);

	GeometryArrayRetained mga = (GeometryArrayRetained)morphedGeometryArray.retained;
	mga.updateData(this);


    }

    void getMirrorShape3D(ArrayList list, HashKey k) {
	Shape3DRetained ms;
	if (inSharedGroup) {
	    ms = getMirrorShape(k);
	}
	else {
	    ms = (Shape3DRetained)mirrorShape3D.get(0);
	}
	list.add(ms);

    }

    void compile(CompileState compState) {

        super.compile(compState);

        // XXXX: for now keep the static transform in the parent tg
        compState.keepTG = true;

        if (J3dDebug.devPhase && J3dDebug.debug) {
            compState.numMorphs++;
        }
    }

    void doErrorCheck(GeometryArrayRetained prevGeo, GeometryArrayRetained geo) {

	// If indexed Geometry array check the entire list instead of just the vcount
	if ((prevGeo.vertexFormat != geo.vertexFormat) ||
	    (prevGeo.validVertexCount != geo.validVertexCount) ||
	    (prevGeo.geoType != geo.geoType) ||
	    (prevGeo.texCoordSetCount != geo.texCoordSetCount)) {
	    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	}
	if (geo.getTexCoordSetMapLength() != prevGeo.getTexCoordSetMapLength()){
	    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	}
	int texCoordSetMapLen = geo.getTexCoordSetMapLength();
	int[] prevSvc= prevGeo.texCoordSetMap;
	int[] svc= geo.texCoordSetMap;
	for (int k = 0; k < texCoordSetMapLen; k++) {
	    if (prevSvc[k] != svc[k])
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	}

	if (geo instanceof GeometryStripArrayRetained) {
	    prevSvc= ((GeometryStripArrayRetained)prevGeo).stripVertexCounts;
	    svc= ((GeometryStripArrayRetained)geo).stripVertexCounts;
	    if (prevSvc.length != svc.length)
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	    for (int k = 0; k < prevSvc.length; k++) {
		if (prevSvc[k] != svc[k])
		    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	    }
	} else if (geo instanceof IndexedGeometryArrayRetained) {
	    if (((IndexedGeometryArrayRetained)prevGeo).validIndexCount != ((IndexedGeometryArrayRetained)geo).validIndexCount)
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));

	    // If by reference, then all array lengths should be same
	    if (geo.getNumCoordCount() != prevGeo.getNumCoordCount() ||
		geo.getNumColorCount() != prevGeo.getNumColorCount() ||
		geo.getNumNormalCount() != prevGeo.getNumNormalCount()) {
		throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
	    }
	    int texCoordSetCount = geo.getTexCoordSetCount();
	    for (int k = 0; k < texCoordSetCount; k++) {
		if (geo.getNumTexCoordCount(k) != prevGeo.getNumTexCoordCount(k)) {
		    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
		}
	    }

	    if (geo instanceof IndexedGeometryStripArrayRetained) {
		prevSvc= ((IndexedGeometryStripArrayRetained)prevGeo).stripIndexCounts;
		svc= ((IndexedGeometryStripArrayRetained)geo).stripIndexCounts;
		if (prevSvc.length != svc.length)
		    throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
		for (int k = 0; k < prevSvc.length; k++) {
		    if (prevSvc[k] != svc[k])
			throw new IllegalArgumentException(J3dI18N.getString("MorphRetained1"));
		}
	    }
	}
    }

    void handleFrequencyChange(int bit) {
	int mask = 0;
	if (bit == Morph.ALLOW_GEOMETRY_ARRAY_WRITE) {
	    mask = GEOMETRY_CHANGED;
	}
	else if (bit == Morph.ALLOW_APPEARANCE_WRITE) {
	    mask = APPEARANCE_CHANGED;
	}
	else if (bit == Morph.ALLOW_APPEARANCE_OVERRIDE_WRITE) {
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

    void searchGeometryAtoms(UnorderList list) {
	list.add(Shape3DRetained.getGeomAtom(
	     (Shape3DRetained) mirrorShape3D.get(0)));
    }
}





