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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.vecmath.Color3f;

/**
 * The Fog leaf node defines Fog parameters.
 * It also specifies an region of influence in which this fog node
 * is active.
 */
abstract class FogRetained extends LeafRetained{

    // Statics used when something in the fog changes
    static final int COLOR_CHANGED           = 0x0001;
    static final int SCOPE_CHANGED           = 0x0002;
    static final int BOUNDS_CHANGED          = 0x0004;
    static final int BOUNDINGLEAF_CHANGED    = 0x0008;
    static final int INIT_MIRROR             = 0x0010;
    static final int CLEAR_MIRROR            = 0x0020;
    static final int LAST_DEFINED_BIT        = 0x0020;

    // Fog color.
    Color3f color = new Color3f(0.0f, 0.0f, 0.0f);

    /**
     * The Boundary object defining the lights's region of influence.
     */
    Bounds regionOfInfluence = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

/**
 * Vector of GroupRetained nodes that scopes this fog.
 */
Vector<GroupRetained> scopes = new Vector<GroupRetained>();

    // An int that is set when this fog is changed
    int isDirty = 0xffff;

    // This is true when this fog is referenced in an immediate mode context
    boolean inImmCtx = false;

    /**
     * The transformed value of the applicationRegion.
     */
    Bounds region = null;

    // A reference to the scene graph fog
    FogRetained sgFog = null;

    // The mirror copy of this fog
    FogRetained mirrorFog = null;

    // Target threads to be notified when light changes
    static final int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
                                     J3dThread.UPDATE_RENDER;

    // Boolean to indicate if this object is scoped (only used for mirror objects
    boolean isScoped = false;

    // The object that contains the dynamic HashKey - a string type object
    // Used in scoping
    HashKey tempKey = new HashKey(250);

    /**
     * The EnvironmentSets which reference this fog.
     * Note that multiple RenderBin update thread may access
     * this shared environmentSets simultaneously.
     * So we use UnorderList when sync. all the operations.
     */
    UnorderList environmentSets = new UnorderList(1, EnvironmentSet.class);

    // Is true, if the mirror fog is viewScoped
    boolean isViewScoped = false;

    // Scale value extracted from localToVworld transform
    private double localToVworldScale = 1.0;

    FogRetained() {
	localBounds = new BoundingBox((Bounds)null);
    }

    /**
     * Initialize the fog color to the specified color.
     */
    void initColor(Color3f color) {
	this.color.set(color);
    }
    /**
     * Sets the fog color to the specified color and send message
     */
    void setColor(Color3f color) {
	this.color.set(color);
	sendMessage(COLOR_CHANGED, new Color3f(color));
    }

    /**
     * Sets the fog color to the specified color.
     */
    void initColor(float r, float g, float b) {
	this.color.x = r;
	this.color.y = g;
	this.color.z = b;
    }
    /**
     * Sets the fog color to the specified color and send message
     */
    void setColor(float r, float g, float b) {
	initColor(r, g, b);
	sendMessage(COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Retrieves the fog color.
     */
    void getColor(Color3f color) {
	color.set(this.color);
    }

    /**
     * Set the Fog's region of influence.
     */
    void initInfluencingBounds(Bounds region) {
	if (region != null) {
            this.regionOfInfluence = (Bounds) region.clone();
	} else {
	    this.regionOfInfluence = null;
	}
	if (staticTransform != null) {
	    this.regionOfInfluence.transform(staticTransform.transform);
	}
    }

    /**
     * Set the Fog's region of influence and send message
     */
    void setInfluencingBounds(Bounds region) {
	initInfluencingBounds(region);
	sendMessage(BOUNDS_CHANGED,
		    (region != null ? region.clone() : null));
    }

    /**
     * Get the Fog's region of Influence.
     */
    Bounds getInfluencingBounds() {
	Bounds b = null;
	if (regionOfInfluence != null) {
	    b = (Bounds)regionOfInfluence.clone();
            if (staticTransform != null) {
                Transform3D invTransform = staticTransform.getInvTransform();
                b.transform(invTransform);
            }
	}
	return b;
    }

    /**
     * Set the Fog's region of influence to the specified Leaf node.
     */
    void initInfluencingBoundingLeaf(BoundingLeaf region) {
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}
    }

    /**
     * Set the Fog's region of influence to the specified Leaf node.
     */
    void setInfluencingBoundingLeaf(BoundingLeaf region) {
	if (boundingLeaf != null)
	    boundingLeaf.mirrorBoundingLeaf.removeUser(mirrorFog);
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorFog);
	} else {
	    boundingLeaf = null;
	}
	sendMessage(BOUNDINGLEAF_CHANGED,
		    (boundingLeaf != null ?
		     boundingLeaf.mirrorBoundingLeaf : null));
    }


    /**
     * Get the Fog's region of influence.
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
	Object[] scopeInfo = new Object[3];


	GroupRetained group = scopes.get(index);
	tempKey.reset();
	group.removeAllNodesForScopedFog(mirrorFog, removeScopeList, tempKey);

	group = (GroupRetained)scope.retained;
	initScope(scope, index);
	tempKey.reset();
	// If its a group, then add the scope to the group, if
	// its a shape, then keep a list to be added during
	// updateMirrorObject
	group.addAllNodesForScopedFog(mirrorFog,addScopeList, tempKey);

	scopeInfo[0] = addScopeList;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE:Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

    /**
     * Inserts the specified scope at specified index.before the
     * fog is live
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void initInsertScope(Node scope, int index) {
        GroupRetained group = (GroupRetained)scope.retained;
	group.setFogScope();
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
	group.addAllNodesForScopedFog(mirrorFog,addScopeList, tempKey);
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = null;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }


    void initRemoveScope(int index) {
	GroupRetained group = scopes.elementAt(index);
	scopes.removeElementAt(index);
	group.removeFogScope();

    }

    void removeScope(int index) {

	Object[] scopeInfo = new Object[3];
	ArrayList removeScopeList = new ArrayList();
	GroupRetained group = scopes.elementAt(index);

	tempKey.reset();
	group.removeAllNodesForScopedFog(mirrorFog, removeScopeList, tempKey);

	initRemoveScope(index);
	scopeInfo[0] = null;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }


    /**
     * Returns the scope specified by the index.
     * @param index which scope to return
     * @return the scoperen at location index
     */
    Group getScope(int index) {
	return (Group)(scopes.elementAt(index).source);
    }

    /**
     * Returns an enumeration object of the scoperen.
     * @return an enumeration object of the scoperen
     */
    Enumeration getAllScopes() {
	Enumeration<GroupRetained> elm = scopes.elements();
	Vector<Group> v = new Vector<Group>(scopes.size());
	while (elm.hasMoreElements()) {
		v.add((Group)elm.nextElement().source);
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
	scopes.addElement(group);
	group.setFogScope();
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
	group.addAllNodesForScopedFog(mirrorFog,addScopeList, tempKey);
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = null;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

    /**
     * Returns a count of this nodes' scopes.
     * @return the number of scopes descendant from this node
     */
    int numScopes() {
	return scopes.size();
    }

  /**
   * Returns the index of the specified scope within this nodes' list of scopes
   * @param scope whose index is desired
   * @return index of specified scope
   */
  int indexOfScope(Group scope) {
    if(scope != null)
      return scopes.indexOf(scope.retained);
    else
      return scopes.indexOf(null);
  }

  /**
   * Removes the specified scope from this nodes' list of scopes
   * @param scope to be removed. If the scope is not found,
   * the method returns silently
   */
  void removeScope(Group scope) {
    int i = indexOfScope(scope);
    if(i >= 0)
      removeScope(i);
  }

  void initRemoveScope(Group scope) {
   int i = indexOfScope(scope);
    if(i >= 0)
      initRemoveScope(i);
  }

  /**
   * Removes all the scopes from this node's list of scopes.
   * The node should revert to universal
   * scope after this method returns
   */
  void removeAllScopes() {
    Object[] scopeInfo = new Object[3];
    ArrayList removeScopeList = new ArrayList();
    int n = scopes.size();

    tempKey.reset();
	for (int index = n - 1; index >= 0; index--) {
		GroupRetained group = scopes.elementAt(index);
       group.removeAllNodesForScopedFog(mirrorFog, removeScopeList, tempKey);
       initRemoveScope(index);
    }
    scopeInfo[0] = null;
    scopeInfo[1] = removeScopeList;
    scopeInfo[2] = Boolean.FALSE;
    sendMessage(SCOPE_CHANGED, scopeInfo);
  }

    /**
     * Removes all scopes from this node
     */
    void initRemoveAllScopes() {
	int n = scopes.size();
	for(int index = n-1; index >= 0; index--)
	    initRemoveScope(index);
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

    boolean isScoped() {
	return (scopes != null);
    }

    /**
     * This abstract method is used to update the current native
     * context fog values.
     */
    abstract void update(Context ctx, double scale);


    void updateImmediateMirrorObject(Object[] objs) {
	int i;

	int component = ((Integer)objs[1]).intValue();
	if ((component & BOUNDS_CHANGED) != 0) {
	    mirrorFog.regionOfInfluence = (Bounds) objs[2];
	    if (mirrorFog.boundingLeaf == null) {
		if (objs[2] != null) {
		    mirrorFog.region = mirrorFog.regionOfInfluence.copy(mirrorFog.region);
		    mirrorFog.region.transform(
				mirrorFog.regionOfInfluence,
				getCurrentLocalToVworld());
		}
		else {
		    mirrorFog.region = null;
		}
	    }
	}
	else if  ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    mirrorFog.boundingLeaf = (BoundingLeafRetained)objs[2];
	    if (objs[2] != null) {
		mirrorFog.region = mirrorFog.boundingLeaf.transformedRegion;
	    }
	    else {
		if (mirrorFog.regionOfInfluence != null) {
		    mirrorFog.region = mirrorFog.regionOfInfluence.copy(mirrorFog.region);
		    mirrorFog.region.transform(
				mirrorFog.regionOfInfluence,
				getCurrentLocalToVworld());
		}
		else {
		    mirrorFog.region = null;
		}

	    }
	}
	else if ((component & SCOPE_CHANGED) != 0) {
	    Object[] scopeList = (Object[])objs[2];
	    ArrayList addList = (ArrayList)scopeList[0];
	    ArrayList removeList = (ArrayList)scopeList[1];
	    boolean isScoped = ((Boolean)scopeList[2]).booleanValue();

	    if (addList != null) {
		mirrorFog.isScoped = isScoped;
		for (i = 0; i < addList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)addList.get(i)).source;
		    obj.addFog(mirrorFog);
		}
	    }

	    if (removeList != null) {
		mirrorFog.isScoped = isScoped;
		for (i = 0; i < removeList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)removeList.get(i)).source;
		    obj.removeFog(mirrorFog);
		}
	    }
	}


    }

    /**
     * The update Object function.
     */
    void updateMirrorObject(Object[] objs) {

	int component = ((Integer)objs[1]).intValue();
	if ((component & COLOR_CHANGED) != 0) {
	    mirrorFog.color.set((Color3f)objs[2]);
	}
	if ((component & INIT_MIRROR) != 0) {
	    mirrorFog.color.set((Color3f)objs[3]);
	}
    }


    /**
     * Note: This routine will only be called on
     * the mirror object - will update the object's
     * cached region and transformed region
     */
    void updateBoundingLeaf() {
        if (boundingLeaf != null && boundingLeaf.switchState.currentSwitchOn) {
            region = boundingLeaf.transformedRegion;
        } else {
            if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
                region.transform(regionOfInfluence,
				 getCurrentLocalToVworld());
            } else {
                region = null;
            }
        }
    }

    /**
     * This setLive routine just calls the superclass's method (after
     * checking for use by an immediate context).  It is up to the
     * subclasses of fog to add themselves to the list of fogs
     */
    void setLive(SetLiveState s) {
        if (inImmCtx) {
	    throw new IllegalSharingException(J3dI18N.getString("FogRetained0"));
        }
        super.doSetLive(s);

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("FogRetained1"));
	}


	// Create the mirror object
	// Initialization of the mirror object during the INSERT_NODE
	// message (in updateMirrorObject)
	if (mirrorFog == null) {
	    //	    mirrorFog = (FogRetained)this.clone(true);
	    mirrorFog = (FogRetained)this.clone();
	    // Assign the bounding leaf of this mirror object as null
	    // it will later be assigned to be the mirror of the lights
	    // bounding leaf object
	    mirrorFog.boundingLeaf = null;
	    mirrorFog.sgFog = this;
	}
	//	initMirrorObject();
	// If bounding leaf is not null, add the mirror object as a user
	// so that any changes to the bounding leaf will be received
	if (boundingLeaf != null) {
	    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorFog);
	}

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorFog);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorFog);
	}

        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorFog, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
        }



	// process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorFog, Targets.ENV_TARGETS);
        }
	mirrorFog.switchState = s.switchStates.get(0);

	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;



	super.markAsLive();


    }
    // This is called on the parent object
    void initMirrorObject(Object[] args) {
	Shape3DRetained shape;
	Object[] scopeInfo = (Object[]) args[2];
	Boolean scoped = (Boolean)scopeInfo[0];
	ArrayList shapeList = (ArrayList)scopeInfo[1];
	BoundingLeafRetained bl=(BoundingLeafRetained)((Object[])args[4])[0];
	Bounds bnds = (Bounds)((Object[])args[4])[1];

	mirrorFog.inBackgroundGroup = ((Boolean)((Object[])args[4])[2]).booleanValue();
	mirrorFog.geometryBackground = (BackgroundRetained)((Object[])args[4])[3];
	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.addFog(mirrorFog);
	}
	mirrorFog.isScoped = scoped.booleanValue();

	if (bl != null) {
	    mirrorFog.boundingLeaf = bl.mirrorBoundingLeaf;
	    mirrorFog.region = boundingLeaf.transformedRegion;
	} else {
	    mirrorFog.boundingLeaf = null;
	    mirrorFog.region = null;
	}

	if (bnds != null) {
	    mirrorFog.regionOfInfluence = bnds;
	    if (mirrorFog.region == null) {
		mirrorFog.region = (Bounds)regionOfInfluence.clone();
		mirrorFog.region.transform(regionOfInfluence, getLastLocalToVworld());
	    }
	}
	else {
	    mirrorFog.regionOfInfluence = null;
	}

    }

   // This is called on the parent object
    void clearMirrorObject(Object[] args) {
	Shape3DRetained shape;
	ArrayList shapeList = (ArrayList)args[2];

	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.removeFog(mirrorFog);
	}
	mirrorFog.isScoped = false;
    }



    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of fogs
     */
    void clearLive(SetLiveState s) {
	int i;
        GroupRetained group;

        super.clearLive(s);

        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorFog, Targets.ENV_TARGETS);
        }
	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;

	// Remove this mirror light as users of the bounding leaf
	if (mirrorFog.boundingLeaf != null)
	    mirrorFog.boundingLeaf.removeUser(mirrorFog);

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorFog);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorFog);
	}
	if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorFog, Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}


	if (scopes.size() > 0) {
	    J3dMessage createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	    createMessage.universe = universe;
	    createMessage.type = J3dMessage.FOG_CHANGED;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(CLEAR_MIRROR);
	    ArrayList removeScopeList = new ArrayList();
	    for (i = 0; i < scopes.size(); i++) {
		group = scopes.get(i);
		tempKey.reset();
		group.removeAllNodesForScopedFog(mirrorFog, removeScopeList, tempKey);
	    }
	    createMessage.args[2] = removeScopeList;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

    // Clone the retained side only, internal use only
     protected Object clone() {
         FogRetained fr = (FogRetained)super.clone();

         fr.color = new Color3f(color);
         Bounds b = getInfluencingBounds();
         if (b != null) {
             fr.initInfluencingBounds(b);
         }

	fr.scopes = new Vector<GroupRetained>();
         fr.isDirty = 0xffff;
         fr.inImmCtx = false;
         fr.region = null;
         fr.sgFog = null;
         fr.mirrorFog = null;
	 fr.environmentSets = new UnorderList(1, EnvironmentSet.class);
         return fr;
     }

    void updateTransformChange() {
	super.updateTransformChange();
        setLocalToVworldScale(sgFog.getLastLocalToVworld().getDistanceScale());
    }

    // Called on mirror object
    void updateImmediateTransformChange() {
	// If bounding leaf is null, tranform the bounds object
	if (boundingLeaf == null) {
	    if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
		region.transform(regionOfInfluence,
				 sgFog.getCurrentLocalToVworld());
	    }

	}
    }

    final void sendMessage(int attrMask, Object attr) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.FOG_CHANGED;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        if (regionOfInfluence != null) {
            regionOfInfluence.transform(xform.transform);
        }
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	leafList.add(mirrorFog);
    }

    /**
     * Scale distances from local to eye coordinate
     */
    protected void validateDistancesInEc(double vworldToCoexistenceScale) {
        assert false : "subclasses should override this method";
    }

    double getLocalToVworldScale() {
        return localToVworldScale;
    }

    void setLocalToVworldScale(double localToVworldScale) {
        this.localToVworldScale = localToVworldScale;
    }
}
