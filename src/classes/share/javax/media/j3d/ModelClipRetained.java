/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;
import javax.vecmath.*;

/**
 * The ModelClip retained object.
 */
class ModelClipRetained extends LeafRetained {

    // Statics used when something in the fog changes
    static final int PLANE_CHANGED           = 0x0001;
    static final int PLANES_CHANGED          = 0x0002;
    static final int ENABLE_CHANGED          = 0x0004;
    static final int ENABLES_CHANGED         = 0x0008;
    static final int BOUNDS_CHANGED          = 0x0010;
    static final int BOUNDINGLEAF_CHANGED    = 0x0020;
    static final int SCOPE_CHANGED	     = 0x0040;
    static final int INIT_MIRROR	     = 0x0080;
    static final int CLEAR_MIRROR	     = 0x0100;
    static final int LAST_DEFINED_BIT        = 0x0100;

    /**
     * The clip planes and the enable bits
     */
    Vector4d[] planes = new Vector4d[6];
    boolean[]  enables = new boolean[6];

    Vector4d[] xformPlanes = new Vector4d[6];

    // enableFlag is true if one of the enables is true
    // only used by mirror object
    boolean    enableFlag = false;

    /**
     * The Boundary object defining the model clip's region of influencing
     */
    Bounds regionOfInfluence = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * The transformed value of the influencingRegion.
     */
    Bounds region = null;

    /**
     * Vector of GroupRetained  nodes that scopes this model clip.
     */
    Vector scopes = new Vector();

    //Boolean to indicate if this object is scoped (only used for mirror objects
    boolean isScoped = false;

    // The object that contains the dynamic HashKey - a string type object
    // Used in scoping
    HashKey tempKey = new HashKey(250);

    // This is true when this model clip is referenced in an immediate mode context
    boolean inImmCtx = false;

    // The mirror copy of this modelClip
    ModelClipRetained mirrorModelClip = null; 

    // A reference to the scene graph model clip
    ModelClipRetained sgModelClip = null;

    // Target threads to be notified when model clip changes
    final static int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
                                     J3dThread.UPDATE_RENDER;

    /**
     * The EnvironmentSets which reference this model clip.
     * Note that multiple RenderBin update thread may access
     * this shared environmentSets simultaneously.
     * So we use UnorderList which sync. all the operations.
     */
    UnorderList environmentSets = new UnorderList(1, EnvironmentSet.class);

    // Is true, if the mirror clip is viewScoped
    boolean isViewScoped = false;

    /**
     * Constructs and initializes model clip planes
     */  
    ModelClipRetained() {

	// planes contains the negate default values
	planes[0] = new Vector4d( 1.0, 0.0,  0.0,-1.0);
	planes[1] = new Vector4d(-1.0, 0.0,  0.0,-1.0);
	planes[2] = new Vector4d( 0.0, 1.0,  0.0,-1.0);
	planes[3] = new Vector4d( 0.0,-1.0,  0.0,-1.0);
	planes[4] = new Vector4d( 0.0, 0.0,  1.0,-1.0);
	planes[5] = new Vector4d( 0.0, 0.0, -1.0,-1.0);

	for (int i = 0; i < 6; i++)
	    xformPlanes[i] = new Vector4d(planes[i]);

        enables[0] = enables[1] = enables[2] = enables[3] =
	    enables[4] = enables[5] = true;
    }

    /**
     * Initializes planes before the object is live
     */
    void initPlanes(Vector4d[] planes) {

        if (staticTransform != null) {
	    Transform3D xform = staticTransform.getNormalTransform();
	    for (int i = 0; i < 6; i++) {
		 this.planes[i].set(planes[i]);
	         xform.transform(this.planes[i], this.xformPlanes[i]);
	    }
	} else {
	    for (int i = 0; i < 6; i++) {
		 this.planes[i].set(planes[i]);
		 this.xformPlanes[i].set(this.planes[i]);
	    }
	}
    }

    /**
     * Sets the clip planes and send a message
     */
    void setPlanes(Vector4d[] planes) {
        Vector4d[] pl = new Vector4d[6];
	initPlanes(planes);

	for (int i = 0; i < 6; i++) {
             pl[i] = new Vector4d(this.xformPlanes[i]);
	}

	sendMessage(PLANES_CHANGED, pl, null);
    }

    /**
     * Initializes planes before the object is live
     */
    void initPlane(int planeNum, Vector4d plane) {
        if (planeNum < 0 || planeNum > 5)
	    throw new IllegalArgumentException(J3dI18N.getString("ModelClip6"));

	if (staticTransform != null) {
	    Transform3D xform = staticTransform.getNormalTransform();
            this.planes[planeNum].set(plane);
	    xform.transform(this.planes[planeNum], this.xformPlanes[planeNum]);
	} else {
            this.planes[planeNum].set(plane);
	    this.xformPlanes[planeNum].set(plane);
	}
    }

    /**
     * Sets the clip planes and send a message
     */
    void setPlane(int planeNum, Vector4d plane) {
	initPlane(planeNum, plane);
	sendMessage(PLANE_CHANGED, 
		    new Integer(planeNum), 
		    new Vector4d(this.xformPlanes[planeNum]));
    }

    /**
     * Gets planes
     */
    void getPlanes(Vector4d[] planes){

	for (int i = 0; i < 6; i++) {
	     planes[i].set(this.planes[i]);
  	}
    }

    /**
     * Gets the specified clipping plane
     */
    void getPlane(int planeNum, Vector4d plane) {
        if (planeNum < 0 || planeNum > 5)
	    throw new IllegalArgumentException(J3dI18N.getString("ModelClip6"));
	plane.set(this.planes[planeNum]);
    }

    /**
     * Initializes planes before the object is live
     */
    void initEnables(boolean[] enables) {
        this.enables[0] = enables[0];
        this.enables[1] = enables[1];
        this.enables[2] = enables[2];
        this.enables[3] = enables[3];
        this.enables[4] = enables[4];
        this.enables[5] = enables[5];
    }

    /**
     * Sets the clip planes and send a message
     */
    void setEnables(boolean[] enables) {
        Boolean[] en = new Boolean[6];

	initEnables(enables);        
        en[0] = (enables[0] ? Boolean.TRUE: Boolean.FALSE);
        en[1] = (enables[1] ? Boolean.TRUE: Boolean.FALSE);
        en[2] = (enables[2] ? Boolean.TRUE: Boolean.FALSE);
        en[3] = (enables[3] ? Boolean.TRUE: Boolean.FALSE);
        en[4] = (enables[4] ? Boolean.TRUE: Boolean.FALSE);
        en[5] = (enables[5] ? Boolean.TRUE: Boolean.FALSE);

	sendMessage(ENABLES_CHANGED, en, null);
    }

    /**
     * Initializes planes before the object is live
     */
    void initEnable(int planeNum, boolean enable) {
        if (planeNum < 0 || planeNum > 5)
	    throw new IllegalArgumentException(J3dI18N.getString("ModelClip6"));
        this.enables[planeNum] = enable;
    }

    /**
     * Sets the clip planes and send a message
     */
    void setEnable(int planeNum, boolean enable) {
	initEnable(planeNum, enable);
	sendMessage(ENABLE_CHANGED, 
		    new Integer(planeNum),
		     (enable ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Gets enables
     */
    void getEnables(boolean[] enables) {
	enables[0] = this.enables[0];
	enables[1] = this.enables[1];
	enables[2] = this.enables[2];
	enables[3] = this.enables[3];
	enables[4] = this.enables[4];
	enables[5] = this.enables[5];
    }

    /**
     * Gets the specified enable
     */
    boolean getEnable(int planeNum) {
        if (planeNum < 0 || planeNum > 5)
	    throw new IllegalArgumentException(J3dI18N.getString("ModelClip6"));
	return (this.enables[planeNum]);
    }

    /**
     * Set the Model Clip's region of influencing
     */
    void initInfluencingBounds(Bounds region) {
        if (region != null) {
            this.regionOfInfluence = (Bounds) region.clone();
            if (staticTransform != null) {
                regionOfInfluence.transform(staticTransform.transform);
            }
        } else {
            this.regionOfInfluence = null;
        }
    }

    /**
     * Set the Model Clip's region of influencing and send message
     */ 
    void setInfluencingBounds(Bounds region) {
        initInfluencingBounds(region);
	sendMessage(BOUNDS_CHANGED,
		    (region != null ? (Bounds) region.clone(): null),
		    null);
    }

    /** 
     * Get the Model Clip's region of influencing.
     */ 
    Bounds getInfluencingBounds() {
        Bounds b = null;

        if (regionOfInfluence != null) {
            b = (Bounds) regionOfInfluence.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
        }
        return b;
    }

    /**
     * Set the Model Clip's region of influencing to the specified Leaf node.
     */ 
    void initInfluencingBoundingLeaf(BoundingLeaf region) {
        if (region != null) {
            boundingLeaf = (BoundingLeafRetained)region.retained;
        } else {
            boundingLeaf = null;
        }
    }

    /**
     * Set the Model Clip's region of influencing to the specified Leaf node.
     */ 
    void setInfluencingBoundingLeaf(BoundingLeaf region) {
        if (boundingLeaf != null)
            boundingLeaf.mirrorBoundingLeaf.removeUser(mirrorModelClip);
        if (region != null) {
            boundingLeaf = (BoundingLeafRetained)region.retained;
            boundingLeaf.mirrorBoundingLeaf.addUser(mirrorModelClip);
        } else {
            boundingLeaf = null;
        }

	sendMessage(BOUNDINGLEAF_CHANGED, 
		    (boundingLeaf != null ?
		     boundingLeaf.mirrorBoundingLeaf : null), 
		    null);
    }


    /** 
     * Get the Model Clip's region of influencing.
     */ 
    BoundingLeaf getInfluencingBoundingLeaf() {
        return (boundingLeaf != null ?
		(BoundingLeaf)boundingLeaf.source : null);
    }

    /**
     * Replaces the specified scope with the scope provided.
     * @param scope the new scope
     * @param index which scope to replace
     */
    void initScope(Group scope, int index) {
        scopes.setElementAt((GroupRetained)(scope.retained), index);
    }

    /**
     * Replaces the specified scope with the scope provided.
     * @param scope the new scope
     * @param index which scope to replace
     */
    void setScope(Group scope, int index) {

        ArrayList addScopeList = new ArrayList();
        ArrayList removeScopeList = new ArrayList();
        GroupRetained group;
        Object[] scopeInfo = new Object[3];

        group = (GroupRetained) scopes.get(index);
        tempKey.reset();
	group.removeAllNodesForScopedModelClip(mirrorModelClip, removeScopeList, tempKey);

        group = (GroupRetained)scope.retained;
        initScope(scope, index);
        tempKey.reset();
	// If its a group, then add the scope to the group, if
	// its a shape, then keep a list to be added during
	// updateMirrorObject
	group.addAllNodesForScopedModelClip(mirrorModelClip,addScopeList, tempKey);
        scopeInfo[0] = addScopeList;
        scopeInfo[1] = removeScopeList;
        scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE:Boolean.FALSE);
        sendMessage(SCOPE_CHANGED, scopeInfo, null);
    }

    /**
     * Inserts the specified scope at specified index
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void initInsertScope(Node scope, int index) {
        GroupRetained group = (GroupRetained)scope.retained;
	group.setMclipScope();
        scopes.insertElementAt((GroupRetained)(scope.retained), index);
    }

    /**
     * Inserts the specified scope at specified index and sends
     * a message
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void insertScope(Node scope, int index) {
        Object[] scopeInfo = new Object[3];
        ArrayList addScopeList = new ArrayList();

        initInsertScope(scope, index);
        GroupRetained group = (GroupRetained)scope.retained;
        tempKey.reset();
	group.addAllNodesForScopedModelClip(mirrorModelClip,addScopeList, tempKey);
        scopeInfo[0] = addScopeList;
        scopeInfo[1] = null;
        scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
        sendMessage(SCOPE_CHANGED, scopeInfo, null);
    }


    void initRemoveScope(int index) {
        GroupRetained group  = (GroupRetained)scopes.elementAt(index);
	group.removeMclipScope();
        scopes.removeElementAt(index);

    }

    void removeScope(int index) {

        Object[] scopeInfo = new Object[3];
        ArrayList removeScopeList = new ArrayList();
        GroupRetained group  = (GroupRetained)scopes.elementAt(index);

	initRemoveScope(index);
        tempKey.reset();
	group.removeAllNodesForScopedModelClip(mirrorModelClip, removeScopeList, tempKey);

        scopeInfo[0] = null;
        scopeInfo[1] = removeScopeList;
        scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
        sendMessage(SCOPE_CHANGED, scopeInfo, null);
    }

    /**
     * Removes the specified Group node from this ModelClip's list of
     * scopes if the specified node is not found in the list of scoped
     * nodes, method returns quietly.
     *
     * @param Group node to be removed
     */
    void removeScope(Group node) {
      int ind = indexOfScope(node);
      if(ind >= 0)
	removeScope(ind);
    }

   void initRemoveScope(Group node) {
      int ind = indexOfScope(node);
      if(ind >= 0)
	initRemoveScope(ind);
    }

    /**
     * Removes all the Group nodes from the ModelClip's scope
     * list. The ModelClip reverts to universal scope.
     */
    void removeAllScopes() {
      Object[] scopeInfo = new Object[3];
      ArrayList removeScopeList = new ArrayList();
      int n = scopes.size();
      for(int index = n-1; index >= 0; index--) {
	GroupRetained group  = (GroupRetained)scopes.elementAt(index);
	initRemoveScope(index);
	tempKey.reset();
	group.removeAllNodesForScopedModelClip(mirrorModelClip, removeScopeList, tempKey);
      }

      scopeInfo[0] = null;
      scopeInfo[1] = removeScopeList;
      scopeInfo[2] = (Boolean.FALSE);
      sendMessage(SCOPE_CHANGED, scopeInfo, null);
    }


    void initRemoveAllScopes() {
      int n = scopes.size();
      for(int i = n-1; i >= 0; i--) {
	initRemoveScope(i);
      }
    }

    /**
     * Returns the scope specified by the index.
     * @param index which scope to return
     * @return the scoperen at location index
     */
    Group getScope(int index) {
        return (Group)(((GroupRetained)(scopes.elementAt(index))).source);
    }

    /**
     * Returns an enumeration object of the scoperen.
     * @return an enumeration object of the scoperen
     */
    Enumeration getAllScopes() {
        Enumeration elm = scopes.elements();
        Vector v = new Vector(scopes.size());
        while (elm.hasMoreElements()) {
            v.add( ((GroupRetained) elm.nextElement()).source);
        }
        return v.elements();
    }

    /**
     * Appends the specified scope to this node's list of scopes before
     * the fog is alive
     * @param scope the scope to add to this node's list of scopes
     */
    void initAddScope(Group scope) {
        GroupRetained group = (GroupRetained)scope.retained;
        scopes.addElement((GroupRetained)(scope.retained));
	group.setMclipScope();
    }

    /**
     * Appends the specified scope to this node's list of scopes.
     * @param scope the scope to add to this node's list of scopes
     */
    void addScope(Group scope) {

        Object[] scopeInfo = new Object[3];
        ArrayList addScopeList = new ArrayList();
        GroupRetained group = (GroupRetained)scope.retained;

        initAddScope(scope);
        tempKey.reset();
	group.addAllNodesForScopedModelClip(mirrorModelClip,addScopeList, tempKey);
        scopeInfo[0] = addScopeList;
        scopeInfo[1] = null;
        scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
        sendMessage(SCOPE_CHANGED, scopeInfo, null);
    }

    /**
     * Returns a count of this nodes' scopes.
     * @return the number of scopes descendant from this node
     */
    int numScopes() {
        return scopes.size();
    }

    /**
     * Returns the index of the specified Group node within the ModelClip's list of scoped 
     * Group nodes
     * @param Group node whose index is desired
     * @return index of this node
     */
    int indexOfScope(Group node) {
      if(node != null)
	return scopes.indexOf((GroupRetained)node.retained);
      else
	return scopes.indexOf(null);
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
     * This method and its native counterpart update the native context
     * model clip planes.
     */
    void update(Canvas3D cv, int enableMask) {
	cv.setModelViewMatrix(cv.ctx, 
			      cv.vworldToEc.mat,
			      getLastLocalToVworld());
	update(cv.ctx, enableMask, getLastLocalToVworld());
    }

    void update(Context ctx, int enableMask, Transform3D trans) {
	if (!VirtualUniverse.mc.isD3D()) {
	    for (int i = 0; i < 6; i ++) {
	         Pipeline.getPipeline().updateModelClip(ctx, i, ((enableMask & (1 << i)) != 0), 
			xformPlanes[i].x, xformPlanes[i].y, 
			xformPlanes[i].z, xformPlanes[i].w);
	    }
	    return;
	}

	// For D3D we need to transform the plane equations from local to
	// world coordinate.
	Transform3D invtrans = new Transform3D(trans);

	// can't call getNormalTransform() since it will cache
	// normalTransform and may return previous result next time.
	invtrans.invert();
	invtrans.transpose();

	for (int i=0; i < 6; i++) {
	    if ((enableMask & (1 << i)) != 0) {
		
		Vector4d vec = new Vector4d(xformPlanes[i].x, xformPlanes[i].y, 
					    xformPlanes[i].z, xformPlanes[i].w);
		vec.normalize();
		invtrans.transform(vec);
		Pipeline.getPipeline().updateModelClip(ctx, i, true, vec.x, vec.y, vec.z, vec.w);

 	    } else {
		Pipeline.getPipeline().updateModelClip(ctx, i, false, 0, 0, 0, 0);
	    }
	}
    }

    void initMirrorObject(Object[] args) {
	Shape3DRetained shape;
	Object[] scopeInfo = (Object[]) args[2];
	Boolean scoped = (Boolean)scopeInfo[0];
	ArrayList shapeList = (ArrayList)scopeInfo[1];
	BoundingLeafRetained bl=(BoundingLeafRetained)((Object[])args[4])[0];
	Bounds bnds = (Bounds)((Object[])args[4])[1];

	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.addModelClip(mirrorModelClip);
	}
	mirrorModelClip.isScoped = scoped.booleanValue();

	if (bl != null) {
	    mirrorModelClip.boundingLeaf = bl.mirrorBoundingLeaf;
	    mirrorModelClip.region = boundingLeaf.transformedRegion;
	} else {
	    mirrorModelClip.boundingLeaf = null;
	    mirrorModelClip.region = null;
	}
	
	if (bnds != null) {
	    mirrorModelClip.regionOfInfluence = bnds;
	    if (mirrorModelClip.region == null) {
		mirrorModelClip.region = (Bounds)regionOfInfluence.clone();
		mirrorModelClip.region.transform(regionOfInfluence, getLastLocalToVworld());
	    }
	}
	else {
	    mirrorModelClip.regionOfInfluence = null;
	}
	boolean[] ens = (boolean[])((Object[])args[4])[2];

	for (int i = 0; i < ens.length; i++) {
	    mirrorModelClip.enables[i] = ens[i];
	}
        mirrorModelClip.enableFlag = mirrorModelClip.enables[0] |
					mirrorModelClip.enables[1] |
					mirrorModelClip.enables[2] |
					mirrorModelClip.enables[3] |
					mirrorModelClip.enables[4] |
					mirrorModelClip.enables[5] ;

    }



    void updateMirrorObject(Object[] objs) {
	int component = ((Integer)objs[1]).intValue();
	if ((component & PLANES_CHANGED) != 0) {
	    Vector4d[] pl = ((Vector4d[]) objs[2]);

	    for (int i = 0; i < 6; i++) {
	         mirrorModelClip.xformPlanes[i].set(pl[i]);
	    }
	}
	else if ((component & PLANE_CHANGED) != 0) {
	    int planeNum = ((Integer)objs[2]).intValue();
	    
	    mirrorModelClip.xformPlanes[planeNum].set((Vector4d)objs[3]);
	}
	else if ((component & INIT_MIRROR) != 0) {
	    Vector4d[] pl = (Vector4d[]) objs[3];
	    for (int i = 0; i < 6; i++) {
	         mirrorModelClip.xformPlanes[i].set(pl[i]);
	    }
	}
    }

    // The update Object function.
    void updateImmediateMirrorObject(Object[] objs) {
	int component = ((Integer)objs[1]).intValue();
	Transform3D trans;


	if  ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    mirrorModelClip.boundingLeaf = (BoundingLeafRetained)objs[2];
	    if (objs[2] != null) {
		mirrorModelClip.region = 
		    (Bounds)mirrorModelClip.boundingLeaf.transformedRegion;
	    }
	    else {
		if (mirrorModelClip.regionOfInfluence != null) {
		    mirrorModelClip.region = 
			((Bounds)mirrorModelClip.regionOfInfluence).copy(mirrorModelClip.region);
		    mirrorModelClip.region.transform(mirrorModelClip.regionOfInfluence,
						     getCurrentLocalToVworld());
		}
		else {
		    mirrorModelClip.region = null;
		}
		
	    }
	} 

	if ((component & BOUNDS_CHANGED) != 0) {
	    mirrorModelClip.regionOfInfluence = (Bounds) objs[2];
	    if (mirrorModelClip.boundingLeaf == null) {
		if (objs[2] != null) {
		    mirrorModelClip.region = 
			((Bounds)mirrorModelClip.regionOfInfluence).copy(mirrorModelClip.region);
		    
		    mirrorModelClip.region.transform(mirrorModelClip.regionOfInfluence,
						     getCurrentLocalToVworld());
		}
		else {
		    mirrorModelClip.region = null;
		}
	    }
	} 

	if ((component & SCOPE_CHANGED) != 0) {
            Object[] scopeList = (Object[])objs[2];
            ArrayList addList = (ArrayList)scopeList[0];
            ArrayList removeList = (ArrayList)scopeList[1];
            boolean isScoped = ((Boolean)scopeList[2]).booleanValue();

            if (addList != null) {
                mirrorModelClip.isScoped = isScoped;
                for (int i = 0; i < addList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)addList.get(i)).source;
		    obj.addModelClip(mirrorModelClip);
                }
            }

            if (removeList != null) {
                mirrorModelClip.isScoped = isScoped;
                for (int i = 0; i < removeList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)removeList.get(i)).source;
		    obj.removeModelClip(mirrorModelClip);
                }
            }
        }

	if ((component & ENABLES_CHANGED) != 0) {
	    Boolean[] en = ((Boolean[]) objs[2]);

	    mirrorModelClip.enables[0] = en[0].booleanValue();
	    mirrorModelClip.enables[1] = en[1].booleanValue();
	    mirrorModelClip.enables[2] = en[2].booleanValue();
	    mirrorModelClip.enables[3] = en[3].booleanValue();
	    mirrorModelClip.enables[4] = en[4].booleanValue();
	    mirrorModelClip.enables[5] = en[5].booleanValue();
            mirrorModelClip.enableFlag = mirrorModelClip.enables[0] |
					mirrorModelClip.enables[1] |
					mirrorModelClip.enables[2] |
					mirrorModelClip.enables[3] |
					mirrorModelClip.enables[4] |
					mirrorModelClip.enables[5] ;
	} else if ((component & ENABLE_CHANGED) != 0) {
	    int planeNum = ((Integer)objs[2]).intValue();

	    mirrorModelClip.enables[planeNum] = ((Boolean)objs[3]).booleanValue();
            mirrorModelClip.enableFlag = mirrorModelClip.enables[0] |
					mirrorModelClip.enables[1] |
					mirrorModelClip.enables[2] |
					mirrorModelClip.enables[3] |
					mirrorModelClip.enables[4] |
					mirrorModelClip.enables[5] ;
	} 
    }


    /** Note: This routine will only be called on
     * the mirror object - will update the object's
     * cached region and transformed region
     */
    void updateBoundingLeaf() {
        if (boundingLeaf != null && boundingLeaf.switchState.currentSwitchOn) {
            region = boundingLeaf.transformedRegion;
        } else {
            if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
                region.transform(regionOfInfluence, getCurrentLocalToVworld());
            } else {
                region = null;
            }
        }
    }


    void setLive(SetLiveState s) {
	GroupRetained group;

	super.doSetLive(s);

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("ModelClipRetained1"));
	}

	// Create the mirror object


        if (mirrorModelClip == null) {
	    mirrorModelClip = (ModelClipRetained)this.clone();
	    mirrorModelClip.boundingLeaf = null;
	    mirrorModelClip.sgModelClip = this;
	}
	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorModelClip);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorModelClip);
	}

	// If bounding leaf is not null, add the mirror object as a user
	// so that any changes to the bounding leaf will be received
	if (boundingLeaf != null) {
	    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorModelClip);
	}
        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorModelClip, Targets.ENV_TARGETS);
        }
        mirrorModelClip.switchState = (SwitchState)s.switchStates.get(0);

	// add this model clip to the transform target
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorModelClip, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}

        s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
            J3dThread.UPDATE_RENDER;
	super.markAsLive();


	// Initialize the mirror object, this needs to be done, when
	// renderBin is not accessing any of the fields
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.MODELCLIP_CHANGED;
	createMessage.args[0] = this;
	// a snapshot of all attributes that needs to be initialized
	// in the mirror object
	createMessage.args[1]= new Integer(INIT_MIRROR);
	ArrayList addScopeList = new ArrayList();
	for (int i = 0; i < scopes.size(); i++) {
	    group = (GroupRetained)scopes.get(i);
	    tempKey.reset();
	    group.addAllNodesForScopedModelClip(mirrorModelClip, addScopeList, tempKey);
	}
	Object[] scopeInfo = new Object[2];
	scopeInfo[0] = ((scopes.size() > 0) ? Boolean.TRUE:Boolean.FALSE);
	scopeInfo[1] = addScopeList;	
	createMessage.args[2] = scopeInfo;
	createMessage.args[3] = xformPlanes.clone();

	Object[] obj = new Object[3];
	obj[0] = boundingLeaf;
	obj[1] = (regionOfInfluence != null?regionOfInfluence.clone():null);
	obj[2] = enables.clone();
	createMessage.args[4] = obj;
	VirtualUniverse.mc.processMessage(createMessage);

	
    }


    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of model clip
     */
    void clearLive(SetLiveState s) {

        super.clearLive(s);
        s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
            J3dThread.UPDATE_RENDER;
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorModelClip, Targets.ENV_TARGETS);
        }
        // Remove this mirror light as users of the bounding leaf
        if (mirrorModelClip.boundingLeaf != null)
            mirrorModelClip.boundingLeaf.removeUser(mirrorModelClip);

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorModelClip);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorModelClip);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorModelClip, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }


	if (scopes.size() > 0) {
	    J3dMessage createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	    createMessage.universe = universe;
	    createMessage.type = J3dMessage.MODELCLIP_CHANGED;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(CLEAR_MIRROR);
	    ArrayList removeScopeList = new ArrayList();
	    for (int i = 0; i < scopes.size(); i++) {
		GroupRetained group = (GroupRetained)scopes.get(i);
		tempKey.reset();
		group.removeAllNodesForScopedModelClip(mirrorModelClip, removeScopeList, tempKey);
	    }
	    createMessage.args[2] = removeScopeList;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

   // This is called on the parent object
    void clearMirrorObject(Object[] args) {
	Shape3DRetained shape;
	ArrayList shapeList = (ArrayList)args[2];
	ArrayList removeScopeList = new ArrayList();
	
	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.removeModelClip(mirrorModelClip);
	}

	mirrorModelClip.isScoped = false;	

    }
    

    // Clone the retained side only, internal use only
    protected Object clone() {
        ModelClipRetained mc = (ModelClipRetained)super.clone();

        mc.planes = new Vector4d[6];
	for (int i = 0; i < 6; i++) {
	     mc.planes[i] = new Vector4d(this.planes[i]);
	     mc.xformPlanes[i] = new Vector4d(this.xformPlanes[i]);
	}

        mc.enables = new boolean[6];
        getEnables(mc.enables);

	// Derive the enables flag
        mc.enableFlag = (mc.enables[0] |
			 mc.enables[1] |
			 mc.enables[2] |
			 mc.enables[3] |
			 mc.enables[4] |
			 mc.enables[5] );	

        mc.inImmCtx = false;
        mc.region = null;
        mc.sgModelClip = null;
        mc.mirrorModelClip = null;
	mc.environmentSets = new UnorderList(1, EnvironmentSet.class);

	if (regionOfInfluence != null) {
	    mc.regionOfInfluence = (Bounds) regionOfInfluence.clone();
	}

        return mc;
    }


    // Called on mirror object
    void updateImmediateTransformChange() {
        // If bounding leaf is null, tranform the bounds object
        if (boundingLeaf == null) {
            if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
                region.transform(regionOfInfluence,
				 sgModelClip.getCurrentLocalToVworld());
            }

        }
    }


    void printPlane(int index, String string) 
    {
	System.err.println(string + " : < " + planes[index].toString() 
		+ " > " + enables[index]);
    } 

    void printPlanes(String string, Vector4d[] planes) 
    {
	System.err.println(string);
	printPlane(0, "[0]");
	printPlane(1, "[1]");
	printPlane(2, "[2]");
	printPlane(3, "[3]");
	printPlane(4, "[4]");
	printPlane(5, "[5]");
    }


    void printEnables(String string, boolean[] enables) 
    {
	System.err.println(string);
	System.err.println("[0] : < " + enables[0] + " >");
	System.err.println("[1] : < " + enables[1] + " >");
	System.err.println("[2] : < " + enables[2] + " >");
	System.err.println("[3] : < " + enables[3] + " >");
	System.err.println("[4] : < " + enables[4] + " >");
	System.err.println("[5] : < " + enables[5] + " >");
    }

   final void sendMessage(int attrMask, Object attr1, Object attr2) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.type = J3dMessage.MODELCLIP_CHANGED;
        createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr1;
	createMessage.args[3] = attr2;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void mergeTransform(TransformGroupRetained staticTransform) {
	super.mergeTransform(staticTransform);

	if (regionOfInfluence != null) {
            regionOfInfluence.transform(staticTransform.transform);
	}

	Transform3D xform = staticTransform.getNormalTransform();
	for (int i = 0; i < 6; i++) {
	     xform.transform(planes[i], xformPlanes[i]);
	}
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	leafList.add(mirrorModelClip);
    }
}
