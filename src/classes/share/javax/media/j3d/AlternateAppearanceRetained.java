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


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;


class AlternateAppearanceRetained extends LeafRetained {


    // Statics used when something in the alternate app changes
    static final int APPEARANCE_CHANGED      = 0x0001;
    static final int SCOPE_CHANGED           = 0x0002;
    static final int BOUNDS_CHANGED          = 0x0004;
    static final int BOUNDINGLEAF_CHANGED    = 0x0008;
    static final int INIT_MIRROR             = 0x0010; // setLive
    static final int CLEAR_MIRROR            = 0x0020; // clearLive


    /**
     * The Boundary object defining the lights's region of influence.
     */
    Bounds regionOfInfluence = null;

    /**
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * Vector of GroupRetained  nodes that scopes this alternate app .
     */
    Vector scopes = new Vector();

    // This is true when this alternate app  is referenced in an immediate mode context
    boolean inImmCtx = false;

   // Target threads to be notified when light changes
    static final int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
                                     J3dThread.UPDATE_RENDER;

    // Boolean to indicate if this object is scoped (only used for mirror objects
    boolean isScoped = false;

    // The object that contains the dynamic HashKey - a string type object
    // Used in scoping
    HashKey tempKey = new HashKey(250);

    /**
     * The transformed value of the applicationRegion.
     */
    Bounds region = null;

    /**
     * mirror Alternate appearance
     */
    AlternateAppearanceRetained mirrorAltApp = null;

    /**
     * Appearance for this object
     */
    AppearanceRetained appearance;

    /**
     * A reference to the scene graph alternateApp
     */
    AlternateAppearanceRetained  sgAltApp = null;

    /**
     * Is true, if the mirror altapp is viewScoped
     */
    boolean isViewScoped = false;

    AlternateAppearanceRetained() {
	this.nodeType = NodeRetained.ALTERNATEAPPEARANCE;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    /**
     * Initializes the appearance
     */
    void initAppearance(Appearance app) {
	if (app != null)
	    appearance = (AppearanceRetained) app.retained;
	else
	    appearance = null;
    }


    /**
     * sets the appearance and send a message
     */
    void setAppearance(Appearance app) {
	if (appearance != null)
	    synchronized(appearance.liveStateLock) {
		appearance.clearLive(refCount);
	    }
	initAppearance(app);
	if (appearance != null) {
	    synchronized(appearance.liveStateLock) {
		appearance.setLive(inBackgroundGroup, refCount);
	    }
	}
	// There is no need to clone the appearance's mirror
	sendMessage(APPEARANCE_CHANGED,
		    (appearance != null ? appearance.mirror: null));
    }



    Appearance getAppearance() {
        return (appearance == null ? null: (Appearance) appearance.source);
    }


    /**
     * Set the alternate's region of influence.
     */
    void initInfluencingBounds(Bounds region) {
	if (region != null) {
            this.regionOfInfluence = (Bounds) region.clone();
	} else {
	    this.regionOfInfluence = null;
	}
    }

    /**
     * Set the alternate's region of influence and send message
     */
    void setInfluencingBounds(Bounds region) {
	initInfluencingBounds(region);
	sendMessage(BOUNDS_CHANGED,
		    (region != null ? region.clone() : null));
    }

    /**
     * Get the alternate's region of Influence.
     */
    Bounds getInfluencingBounds() {
	return (regionOfInfluence != null ?
		(Bounds) regionOfInfluence.clone() : null);
    }

    /**
     * Set the alternate's region of influence to the specified Leaf node.
     */
    void initInfluencingBoundingLeaf(BoundingLeaf region) {
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}
    }

    /**
     * Set the alternate's region of influence to the specified Leaf node.
     */
    void setInfluencingBoundingLeaf(BoundingLeaf region) {
	if (boundingLeaf != null)
	    boundingLeaf.mirrorBoundingLeaf.removeUser(mirrorAltApp);
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorAltApp);
	} else {
	    boundingLeaf = null;
	}
	sendMessage(BOUNDINGLEAF_CHANGED,
		    (boundingLeaf != null ?
		     boundingLeaf.mirrorBoundingLeaf : null));
    }

    /**
     * Get the alternate's region of influence.
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

	ArrayList removeScopeList = new ArrayList();
	GroupRetained group;
	ArrayList addScopeList = new ArrayList();
	Object[] scopeInfo = new Object[3];

	group = (GroupRetained) scopes.get(index);
	tempKey.reset();
	group.removeAllNodesForScopedAltApp(mirrorAltApp, removeScopeList, tempKey);

	group = (GroupRetained)scope.retained;
	initScope(scope, index);
	tempKey.reset();

	// If its a group, then add the scope to the group, if
	// its a shape, then keep a list to be added during
	// updateMirrorObject
	group.addAllNodesForScopedAltApp(mirrorAltApp,addScopeList, tempKey);
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE:Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

     Group getScope(int index) {
	return (Group)(((GroupRetained)(scopes.elementAt(index))).source);
    }


    /**
     * Inserts the specified scope at specified index.before the
     * alt app is live
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void initInsertScope(Node scope, int index) {
	GroupRetained group = (GroupRetained)scope.retained;
	scopes.insertElementAt((GroupRetained)(scope.retained), index);
	group.setAltAppScope();
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
	GroupRetained group = (GroupRetained)scope.retained;

	initInsertScope(scope, index);
        group = (GroupRetained)scope.retained;
	tempKey.reset();
	group.addAllNodesForScopedAltApp(mirrorAltApp,addScopeList, tempKey);

	scopeInfo[0] = addScopeList;
	scopeInfo[1] = null;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }



    void initRemoveScope(int index) {
	GroupRetained group  = (GroupRetained)scopes.elementAt(index);
	scopes.removeElementAt(index);
	group.removeAltAppScope();

    }

    void removeScope(int index) {

	Object[] scopeInfo = new Object[3];
	ArrayList removeScopeList = new ArrayList();
	GroupRetained group  = (GroupRetained)scopes.elementAt(index);

	tempKey.reset();
	group.removeAllNodesForScopedAltApp(mirrorAltApp, removeScopeList, tempKey);

	initRemoveScope(index);
	scopeInfo[0] = null;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

  /**
   * Removes the specified Group node from this node's list of scopes.
   * Method is a no-op if the
   * specified node is not found
   * @param The Group node to be removed
   */
    void removeScope(Group scope) {
      int ind = indexOfScope(scope);
      if(ind >= 0)
	removeScope(ind);
    }

    void initRemoveScope(Group scope) {
      int ind = indexOfScope(scope);
      if(ind >= 0)
	initRemoveScope(ind);
    }

    void removeAllScopes() {
      GroupRetained group;
      ArrayList removeScopeList = new ArrayList();
      int n = scopes.size();
      for(int index = n-1; index >= 0; index--) {
	group  = (GroupRetained)scopes.elementAt(index);
	tempKey.reset();
	group.removeAllNodesForScopedAltApp(mirrorAltApp, removeScopeList, tempKey);
	initRemoveScope(index);
      }
      Object[] scopeInfo = new Object[3];
      scopeInfo[0] = null;
      scopeInfo[1] = removeScopeList;
      scopeInfo[2] = (Boolean.FALSE);
      sendMessage(SCOPE_CHANGED, scopeInfo);
    }

    void initRemoveAllScopes() {
      int n = scopes.size();
      for(int i = n-1; i >= 0; i--)
	initRemoveScope(i);
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
   * Returns the index of the specified Group node in this node's list of scopes.
   * @param scope the Group node whose index is needed
   */
  int indexOfScope(Group scope) {
    if(scope != null)
      return scopes.indexOf((GroupRetained)scope.retained);
    else
      return scopes.indexOf(null);
  }

    /**
     * Appends the specified scope to this node's list of scopes before
     * the alt app is alive
     * @param scope the scope to add to this node's list of scopes
     */
    void initAddScope(Group scope) {
        GroupRetained group = (GroupRetained)scope.retained;
	scopes.addElement((GroupRetained)(scope.retained));
	group.setAltAppScope();
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
	group.addAllNodesForScopedAltApp(mirrorAltApp,addScopeList, tempKey);
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


    void updateImmediateMirrorObject(Object[] objs) {
	GroupRetained group;
	Vector currentScopes;
	int i, nscopes;
	Transform3D trans;

	int component = ((Integer)objs[1]).intValue();
	if ((component & APPEARANCE_CHANGED) != 0) {
	    mirrorAltApp.appearance = (AppearanceRetained)objs[2];
	}
	if ((component & BOUNDS_CHANGED) != 0) {
	    mirrorAltApp.regionOfInfluence = (Bounds) objs[2];
	    if (mirrorAltApp.boundingLeaf == null) {
		if (objs[2] != null) {
		    mirrorAltApp.region = (Bounds)mirrorAltApp.regionOfInfluence.copy(mirrorAltApp.region);
		    mirrorAltApp.region.transform(
				mirrorAltApp.regionOfInfluence,
				getCurrentLocalToVworld());
		}
		else {
		    mirrorAltApp.region = null;
		}
	    }
	}
	else if  ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    mirrorAltApp.boundingLeaf = (BoundingLeafRetained)objs[2];
	    if (objs[2] != null) {
		mirrorAltApp.region = (Bounds)mirrorAltApp.boundingLeaf.transformedRegion;
	    }
	    else {
		if (mirrorAltApp.regionOfInfluence != null) {
		    mirrorAltApp.region = mirrorAltApp.regionOfInfluence.copy(mirrorAltApp.region);
		    mirrorAltApp.region.transform(
				mirrorAltApp.regionOfInfluence,
				getCurrentLocalToVworld());
		}
		else {
		    mirrorAltApp.region = null;
		}

	    }
	}
	else if ((component & SCOPE_CHANGED) != 0) {
	    Object[] scopeList = (Object[])objs[2];
	    ArrayList addList = (ArrayList)scopeList[0];
	    ArrayList removeList = (ArrayList)scopeList[1];
	    boolean isScoped = ((Boolean)scopeList[2]).booleanValue();

	    if (addList != null) {
		mirrorAltApp.isScoped = isScoped;
		for (i = 0; i < addList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)addList.get(i)).source;
		    obj.addAltApp(mirrorAltApp);
		}
	    }

	    if (removeList != null) {
		mirrorAltApp.isScoped = isScoped;
		for (i = 0; i < removeList.size(); i++) {
		    Shape3DRetained obj = ((GeometryAtom)removeList.get(i)).source;
		    obj.removeAltApp(mirrorAltApp);
		}
	    }
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
	Vector currentScopes;
	int i, nscopes;
	TransformGroupRetained[] tlist;

        if (inImmCtx) {
	    throw new IllegalSharingException(J3dI18N.getString("AlternateAppearanceRetained13"));
        }

	if (inSharedGroup) {
	    throw new
		IllegalSharingException(J3dI18N.getString("AlternateAppearanceRetained15"));
	}

	if (inBackgroundGroup) {
	    throw new
		IllegalSceneGraphException(J3dI18N.getString("AlternateAppearanceRetained16"));
	}

        super.doSetLive(s);

	if (appearance != null) {
	    if (appearance.getInImmCtx()) {
		throw new IllegalSharingException(J3dI18N.getString("AlternateAppearanceRetained14"));
	    }
	    synchronized(appearance.liveStateLock) {
		appearance.setLive(inBackgroundGroup, s.refCount);
	    }
	}

    // Create the mirror object
	// Initialization of the mirror object during the INSERT_NODE
	// message (in updateMirrorObject)
	if (mirrorAltApp == null) {
	    mirrorAltApp = (AlternateAppearanceRetained)this.clone();
	    // Assign the bounding leaf of this mirror object as null
	    // it will later be assigned to be the mirror of the alternate app
	    // bounding leaf object
	    mirrorAltApp.boundingLeaf = null;
	    mirrorAltApp.sgAltApp = this;
	}
	// If bounding leaf is not null, add the mirror object as a user
	// so that any changes to the bounding leaf will be received
	if (boundingLeaf != null) {
	    boundingLeaf.mirrorBoundingLeaf.addUser(mirrorAltApp);
	}

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorAltApp);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorAltApp);
	}

        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorAltApp,
                                                Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}

        // process switch leaf
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorAltApp, Targets.ENV_TARGETS);
	}
        mirrorAltApp.switchState = (SwitchState)s.switchStates.get(0);

	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;

	// At the end make it live
	super.markAsLive();

	// Initialize the mirror object, this needs to be done, when
	// renderBin is not accessing any of the fields
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.ALTERNATEAPPEARANCE_CHANGED;
	createMessage.args[0] = this;
	// a snapshot of all attributes that needs to be initialized
	// in the mirror object
	createMessage.args[1]= new Integer(INIT_MIRROR);
	ArrayList addScopeList = new ArrayList();
	for (i = 0; i < scopes.size(); i++) {
	    group = (GroupRetained)scopes.get(i);
	    tempKey.reset();
	    group.addAllNodesForScopedAltApp(mirrorAltApp, addScopeList, tempKey);
	}
	Object[] scopeInfo = new Object[2];
	scopeInfo[0] = ((scopes.size() > 0) ? Boolean.TRUE:Boolean.FALSE);
	scopeInfo[1] = addScopeList;
	createMessage.args[2] = scopeInfo;
	if (appearance != null) {
	    createMessage.args[3] = appearance.mirror;
	}
	else {
	    createMessage.args[3] = null;
	}
	Object[] obj = new Object[2];
	obj[0] = boundingLeaf;
	obj[1] = (regionOfInfluence != null?regionOfInfluence.clone():null);
	createMessage.args[4] = obj;
	VirtualUniverse.mc.processMessage(createMessage);





    }

    /**
     * This is called on the parent object
     */
    void initMirrorObject(Object[] args) {
	Shape3DRetained shape;
	Object[] scopeInfo = (Object[]) args[2];
	Boolean scoped = (Boolean)scopeInfo[0];
	ArrayList shapeList = (ArrayList)scopeInfo[1];
	AppearanceRetained app = (AppearanceRetained)args[3];
	BoundingLeafRetained bl=(BoundingLeafRetained)((Object[])args[4])[0];
	Bounds bnds = (Bounds)((Object[])args[4])[1];

	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.addAltApp(mirrorAltApp);
	}
	mirrorAltApp.isScoped = scoped.booleanValue();

	if (app != null)
	    mirrorAltApp.appearance = app;

	if (bl != null) {
	    mirrorAltApp.boundingLeaf = bl.mirrorBoundingLeaf;
	    mirrorAltApp.region = boundingLeaf.transformedRegion;
	} else {
	    mirrorAltApp.boundingLeaf = null;
	    mirrorAltApp.region = null;
	}

	if (bnds != null) {
	    mirrorAltApp.regionOfInfluence = bnds;
	    if (mirrorAltApp.region == null) {
		mirrorAltApp.region = (Bounds)regionOfInfluence.clone();
		mirrorAltApp.region.transform(regionOfInfluence, getLastLocalToVworld());
	    }
	}
	else {
	    mirrorAltApp.regionOfInfluence = null;
	}

    }

    void clearMirrorObject(Object[] args) {
	Shape3DRetained shape;
	ArrayList shapeList = (ArrayList)args[2];
	ArrayList removeScopeList = new ArrayList();

	for (int i = 0; i < shapeList.size(); i++) {
	    shape = ((GeometryAtom)shapeList.get(i)).source;
	    shape.removeAltApp(mirrorAltApp);
	}
	mirrorAltApp.isScoped = false;



    }


    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of alt app
     */
    void clearLive(SetLiveState s) {
	int i, j;
        GroupRetained group;

	if (appearance != null) {
	    synchronized(appearance.liveStateLock) {
		appearance.clearLive(s.refCount);
	    }
	}

	super.clearLive(s);
	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;

	// Remove this mirror light as users of the bounding leaf
	if (mirrorAltApp.boundingLeaf != null)
	    mirrorAltApp.boundingLeaf.removeUser(mirrorAltApp);

	if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
	    s.viewScopedNodeList.add(mirrorAltApp);
	    s.scopedNodesViewList.add(s.viewLists.get(0));
	} else {
	    s.nodeList.add(mirrorAltApp);
	}
        if (s.transformTargets != null && s.transformTargets[0] != null) {
            s.transformTargets[0].addNode(mirrorAltApp,
                                                Targets.ENV_TARGETS);
	    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	}
        if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
            s.switchTargets[0].addNode(mirrorAltApp, Targets.ENV_TARGETS);
        }


	if (scopes.size() > 0) {
	    J3dMessage createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	    createMessage.universe = universe;
	    createMessage.type = J3dMessage.ALTERNATEAPPEARANCE_CHANGED;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(CLEAR_MIRROR);
	    ArrayList removeScopeList = new ArrayList();
	    for (i = 0; i < scopes.size(); i++) {
		group = (GroupRetained)scopes.get(i);
		tempKey.reset();
		group.removeAllNodesForScopedAltApp(mirrorAltApp, removeScopeList, tempKey);
	    }
	    createMessage.args[2] = removeScopeList;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }



    void updateTransformChange() {
    }

    /**
     * Called on mirror object
     */
    void updateImmediateTransformChange() {
	// If bounding leaf is null, tranform the bounds object
	if (boundingLeaf == null) {
	    if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
		region.transform(regionOfInfluence,
				 sgAltApp.getCurrentLocalToVworld());
	    }

	}
    }

    final void sendMessage(int attrMask, Object attr) {
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = targetThreads;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.ALTERNATEAPPEARANCE_CHANGED;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	VirtualUniverse.mc.processMessage(createMessage);
    }


    void getMirrorObjects(ArrayList leafList, HashKey key) {
	leafList.add(mirrorAltApp);
    }


   /**
    * Copies all AlternateAppearance information from
    * <code>originalNode</code> into
    * the current node.  This method is called from the
    * <code>cloneNode</code> method which is, in turn, called by the
    * <code>cloneTree</code> method.<P>
    *
    * @param originalNode the original node to duplicate.
    * @param forceDuplicate when set to <code>true</code>, causes the
    *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
    *  <code>false</code>, the value of each node's
    *  <code>duplicateOnCloneTree</code> variable determines whether
    *  NodeComponent data is duplicated or copied.
    *
    * @exception RestrictedAccessException if this object is part of a live
    *  or compiled scenegraph.
    *
    * @see Node#duplicateNode
    * @see Node#cloneTree
    * @see NodeComponent#setDuplicateOnCloneTree
    */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
	throw new RuntimeException("method not implemented");

//	super.duplicateAttributes(originalNode, forceDuplicate);

//	AlternateAppearance alternate appearance = (AlternateAppearance) originalNode;

//	// XXXX: clone appearance

//	setInfluencingBounds(alternate appearance.getInfluencingBounds());

//	Enumeration elm = alternate appearance.getAllScopes();
//	while (elm.hasMoreElements()) {
//	    // this reference will set correctly in updateNodeReferences() callback
//	    addScope((Group) elm.nextElement());
//	}

//	// this reference will set correctly in updateNodeReferences() callback
//	setInfluencingBoundingLeaf(alternate appearance.getInfluencingBoundingLeaf());
    }

//    /**
//     * Callback used to allow a node to check if any nodes referenced
//     * by that node have been duplicated via a call to <code>cloneTree</code>.
//     * This method is called by <code>cloneTree</code> after all nodes in
//     * the sub-graph have been duplicated. The cloned Leaf node's method
//     * will be called and the Leaf node can then look up any node references
//     * by using the <code>getNewObjectReference</code> method found in the
//     * <code>NodeReferenceTable</code> object.  If a match is found, a
//     * reference to the corresponding Node in the newly cloned sub-graph
//     * is returned.  If no corresponding reference is found, either a
//     * DanglingReferenceException is thrown or a reference to the original
//     * node is returned depending on the value of the
//     * <code>allowDanglingReferences</code> parameter passed in the
//     * <code>cloneTree</code> call.
//     * <p>
//     * NOTE: Applications should <i>not</i> call this method directly.
//     * It should only be called by the cloneTree method.
//     *
//     * @param referenceTable a NodeReferenceTableObject that contains the
//     *  <code>getNewObjectReference</code> method needed to search for
//     *  new object instances.
//     * @see NodeReferenceTable
//     * @see Node#cloneTree
//     * @see DanglingReferenceException
//     */
//    public void updateNodeReferences(NodeReferenceTable referenceTable) {
//	throw new RuntimeException("method not implemented");
//
//	Object o;
//
//	BoundingLeaf bl = getInfluencingBoundingLeaf();
//	if (bl != null) {
//	    o = referenceTable.getNewObjectReference(bl);
//	    setInfluencingBoundingLeaf((BoundingLeaf) o);
//	}
//
//	for (int i=0; i < numScopes(); i++) {
//	    o = referenceTable.getNewObjectReference(getScope(i));
//	    setScope((Group) o, i);
//	}
//   }

}
