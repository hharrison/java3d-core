/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.Enumeration;
import java.util.Vector;
import java.util.ArrayList;

/**
 * LightRetained is an abstract class that contains instance variable common to
 * all lights.
 */

abstract class LightRetained extends LeafRetained {

    // Statics used when something in the light changes
    static final int ENABLE_CHANGED          = 0x0001;
    static final int SCOPE_CHANGED           = 0x0002;
    static final int BOUNDS_CHANGED          = 0x0004;
    static final int COLOR_CHANGED           = 0x0008;
    static final int BOUNDINGLEAF_CHANGED    = 0x0010;
    static final int INIT_MIRROR             = 0x0020;
    static final int CLEAR_MIRROR            = 0x0040;
    static final int LAST_DEFINED_BIT        = 0x0040;

    // Indicates whether the light is turned on.
    boolean	lightOn = true;

    // The color of the light (white by default).
    Color3f	color = new Color3f(1.0f, 1.0f, 1.0f);

    // This node which specifies the hierarchical scope of the
    // light.  A null reference means that this light has universal
    // scope.
    Vector scopes = new Vector();

    /**
     * The Boundary object defining the lights's region of influence.
     */  
    Bounds regionOfInfluence = null;
 
    /** 
     * The bounding leaf reference
     */
    BoundingLeafRetained boundingLeaf = null;

    /**
     * The transformed value of the applicationRegion.
     */
    Bounds region = null;

    /** 
     * This bitmask is set when something changes in the light
     */
    int lightDirty = 0xffff;

    // This is a copy of the sgLight's dirty bits
    int sgLightDirty = 0xffff;

    // The type of light
    int lightType = -1;

    // This is true when this light is needed in the current light set
    boolean isNeeded = false;

    // This is true when this light is referenced in an immediate mode context
    boolean inImmCtx = false;

    // A back reference to the scene graph light, when this is a mirror light
    LightRetained sgLight = null;

    // A HashKey for lights in a shared group
    HashKey key = null;

    // An array of mirror lights, one for each instance of this light in a
    // shared group.  Entry 0 is the only one valid if we are not in a shared
    // group.
    LightRetained[] mirrorLights = new LightRetained[1];

    // The number of valid lights in mirrorLights
    int numMirrorLights = 0;

    // Indicated whether the light is a scoped light
    boolean isScoped = false;

   // The object that contains the dynamic HashKey - a string type object
    // Used in scoping 
    HashKey tempKey = new HashKey(250);

    /**
     * A list of all the EnvironmentSets that reference this light.
     * Note that multiple RenderBin update thread may access
     * this shared environmentSets simultaneously.
     * So we use UnorderList when sync. all the operations.
     */
    UnorderList environmentSets = new UnorderList(1, EnvironmentSet.class);

    // Is true, if the mirror light is viewScoped
    boolean isViewScoped = false;


    /**
     * Temporary list of newly added mirror lights, during any setlive
     */
    ArrayList newlyAddedMirrorLights = new ArrayList();

    // Target threads to be notified when light changes
    static final int targetThreads = J3dThread.UPDATE_RENDERING_ENVIRONMENT |
	J3dThread.UPDATE_RENDER;

    /**
     * Initialize the light on or off.
     * @param state true or false to enable or disable the light
     */
    void initEnable(boolean state) {
        this.lightOn = state;
    }

    /**
     * Turns the light on or off and send a message
     * @param state true or false to enable or disable the light
     */
    void setEnable(boolean state) {
	initEnable(state);
	sendMessage(ENABLE_CHANGED, 
		    (state ? Boolean.TRUE: Boolean.FALSE));
    }


    /**
     * Returns the state of the light (on/off).
     * @return true if the light is on, false if the light is off.
     */
     boolean getEnable() {
	return this.lightOn;
    }

    /**
     * Initialize the color of this light node.
     * @param color the value of this new light color
     */
    void initColor(Color3f color) {
	this.color.set(color);
    }

    /**
     * Sets the color of this light node and send a message
     * @param color the value of this new light color
     */
    void setColor(Color3f color) {
	initColor(color);
	sendMessage(COLOR_CHANGED, new Color3f(color));
     }

    /**
     * Retrieves the color of this light.
     * @param color the vector that will receive the color of this light
     */
     void getColor(Color3f color) {
	color.set(this.color);
    }

    /**
     * Initializes the specified scope with the scope provided.
     * @param scope the new scope
     * @param index which scope to replace
     */
    void initScope(Group scope, int index) {
        GroupRetained group = (GroupRetained)scope.retained;
        scopes.setElementAt(group, index);

    }


    /**
     * Replaces the specified scope with the scope provided and
     * send a message
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
	group.removeAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights, removeScopeList, tempKey);

	
        group = (GroupRetained)scope.retained;
	tempKey.reset();
	// If its a group, then add the scope to the group, if
	// its a shape, then keep a list to be added during
	// updateMirrorObject
	group.addAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights,addScopeList, tempKey);

	
	initScope(scope, index);
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }
    
    /**
     * Inserts the specified scope at specified index.
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void initInsertScope(Group scope, int index) {
        GroupRetained group = (GroupRetained)scope.retained;
        scopes.insertElementAt(group, index);
	group.setLightScope();
    }

    /**
     * Inserts the specified scope at specified index.
     * @param scope the new scope
     * @param index position to insert new scope at
     */
    void insertScope(Group scope, int index) {

	Object[] scopeInfo = new Object[3];
	ArrayList addScopeList = new ArrayList();
        GroupRetained group = (GroupRetained)scope.retained;

	tempKey.reset();
	group.addAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights,addScopeList, tempKey);
	
	initInsertScope(scope, index);
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = null;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

    
    /**
     * Removes the scope at specified index.
     * @param index which scope to remove
     */
    void initRemoveScope(int index) {
        GroupRetained group = (GroupRetained)scopes.elementAt(index);
        scopes.removeElementAt(index);
	group.removeLightScope();
    }

    
    /**
     * Removes the scope at specified index.
     * @param index which scope to remove
     */
    void removeScope(int index) {

	Object[] scopeInfo = new Object[3];
	ArrayList removeScopeList = new ArrayList();
      
        GroupRetained group = (GroupRetained)scopes.elementAt(index);
	tempKey.reset();
	group.removeAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights, removeScopeList, tempKey);
	initRemoveScope(index);	scopeInfo[0] = null;
	scopeInfo[1] = removeScopeList;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED, scopeInfo);
    }

      
    /**
     * Removes the specified scope
     * @param scope to be removed
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

    /**
     *  Removes all the scopes from this Light's list of scopes
     */
    void removeAllScopes() {
      int n = scopes.size();
      Object[] scopeInfo = new Object[3];
      ArrayList removeScopeList = new ArrayList();
      GroupRetained group;

      for(int index = n-1; index >= 0; index--) {
	group = (GroupRetained)scopes.elementAt(index);
	tempKey.reset();
	group.removeAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights, removeScopeList, tempKey);
	initRemoveScope(index);
      }
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
     * Returns the scope specified by the index.
     * @param index of the scope to be returned
     * @return the scope at location index
     */
    Group getScope(int index) {
      return (Group)(((GroupRetained)(scopes.elementAt(index))).source);
    }
  
    /**
     * Returns an enumeration object of the scope
     * @return an enumeration object of the scope
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
     * Appends the specified scope to this node's list of scopes.
     * @param scope the scope to add to this node's list of scopes
     */
    void initAddScope(Group scope) {
        GroupRetained group = (GroupRetained)scope.retained;
        scopes.addElement(group);
	group.setLightScope();
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
	group.addAllNodesForScopedLight((inSharedGroup?numMirrorLights:1), mirrorLights,addScopeList, tempKey);
	scopeInfo[0] = addScopeList;
	scopeInfo[1] = null;
	scopeInfo[2] = (scopes.size() > 0 ? Boolean.TRUE: Boolean.FALSE);
	sendMessage(SCOPE_CHANGED,  scopeInfo);
    }
    
    /**
     * Returns a count of this nodes' scopes.
     * @return the number of scopes descendant from this node
     */
    int numScopes() {
      return scopes.size();
    }

    /**
     *  Returns the index of the specified scope
     *  @return index of the scope in this Light's list of scopes
     */
  int indexOfScope(Group scope) {
    if(scope != null)
      return scopes.indexOf((GroupRetained)scope.retained);
    else
      return scopes.indexOf(null);
  }
  
    /**
     * Initializes the Light's region of influence.
     * @param region a region that contains the Light's new region of influence
     */  
    void initInfluencingBounds(Bounds region) {
	if (region != null) {
	    regionOfInfluence = (Bounds) region.clone();
	    if (staticTransform != null) {
		regionOfInfluence.transform(staticTransform.transform);
	    }
	} else {
	    regionOfInfluence = null;
	}
    }


    /**
     * Set the Light's region of influence and send a message
     * @param region a region that contains the Light's new region of influence
     */  
    void setInfluencingBounds(Bounds region) {
	initInfluencingBounds(region);
	sendMessage(BOUNDS_CHANGED,  
		    (region != null ? region.clone() : null));
    }

    /**
     * Get the Light's region of influence
     * @return this Light's region of influence information
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
     * Initializes the Light's region of influence to the specified Leaf node.
     */  
    void initInfluencingBoundingLeaf(BoundingLeaf region) {
	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	} else {
	    boundingLeaf = null;
	}
    }

    /**
     * Set the Light's region of influence to the specified Leaf node.
     */  
    void setInfluencingBoundingLeaf(BoundingLeaf region) {
	int i, numLgts;

	numLgts = numMirrorLights;
	if (numMirrorLights == 0)  
	    numLgts = 1;

	if (boundingLeaf != null) {
	    // Remove the mirror lights as users of the original bounding leaf
	    for (i = 0; i < numLgts; i++) {
		boundingLeaf.mirrorBoundingLeaf.removeUser(mirrorLights[i]);
	    }
	}

	if (region != null) {
	    boundingLeaf = (BoundingLeafRetained)region.retained;
	    // Add all mirror lights as user of this bounding leaf
	    for (i = 0; i < numLgts; i++) {
		boundingLeaf.mirrorBoundingLeaf.addUser(mirrorLights[i]);
	    }
	} else {
	    boundingLeaf = null;
	}

	sendMessage(BOUNDINGLEAF_CHANGED, 
		     (boundingLeaf != null ?
		      boundingLeaf.mirrorBoundingLeaf : null));
    }

    /**  
     * Get the Light's region of influence.
     */  
    BoundingLeaf getInfluencingBoundingLeaf() {
	return  (boundingLeaf != null ?
		 (BoundingLeaf)boundingLeaf.source : null);
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


    // Called on the parent Light object and loops over the mirror object
    void initMirrorObject(Object[] args) {
	Shape3DRetained shape;
	Object[] scopeInfo =  (Object[])((Object[])args[4])[5];
	ArrayList gAtomList = (ArrayList)scopeInfo[1];
	Boolean scoped = (Boolean)scopeInfo[0];
	BoundingLeafRetained bl=(BoundingLeafRetained)((Object[])args[4])[0];
	Bounds bnds = (Bounds)((Object[])args[4])[1];
	int numLgts = ((Integer)args[2]).intValue();
	LightRetained[] mLgts = (LightRetained[]) args[3];
	int k;
	
	for ( k = 0; k < numLgts; k++) {
	    for (int i = 0; i < gAtomList.size(); i++) {
		shape = ((GeometryAtom)gAtomList.get(i)).source;
		shape.addLight(mLgts[k]);
	    }
	    mLgts[k].isScoped = scoped.booleanValue();
	}
	
	for (k = 0; k < numLgts; k++) {
	    mLgts[k].inBackgroundGroup = ((Boolean)((Object[])args[4])[2]).booleanValue();
	    mLgts[k].geometryBackground = (BackgroundRetained)((Object[])args[4])[3];


	    if (bl != null) {
		mLgts[k].boundingLeaf = bl.mirrorBoundingLeaf;
		mLgts[k].region = mLgts[k].boundingLeaf.transformedRegion;
	    } else {
		mLgts[k].boundingLeaf = null;
		mLgts[k].region = null;
	    }
	    
	    if (bnds != null) {
		mLgts[k].regionOfInfluence = bnds;
		if (mLgts[k].region == null) {
		    mLgts[k].region = (Bounds)regionOfInfluence.clone();
		    mLgts[k].region.transform(regionOfInfluence, getLastLocalToVworld());
		}
	    }
	    else {
		mLgts[k].regionOfInfluence = null;
	    }
	    mLgts[k].lightOn = ((Boolean)((Object[])args[4])[4]).booleanValue();

	}
	// if its a ambient light,then do a immediate update of color
	if (this instanceof AmbientLightRetained) {
	    Color3f clr = (Color3f) ((Object[])args[4])[6];
	    for (int i = 0; i < numLgts; i++) {
		mLgts[i].color.set(clr);
	    }
	}
	    
    }
    
    /** 
     * This method is implemented by each light for rendering
     * context updates.  This default one does nothing.
     */
    abstract void update(long ctx, int lightSlot, double scale);


    // This routine is called when rendering Env structure
    // get a message, this routine updates values in the mirror object
    // that are not used by the renderer
    void updateImmediateMirrorObject(Object[] objs) {
	Transform3D trans = null;
	int component = ((Integer)objs[1]).intValue();
	int numLgts = ((Integer)objs[2]).intValue();
	LightRetained[] mLgts = (LightRetained[]) objs[3];

	// Color changed called immediately only for ambient lights
	if ((component & COLOR_CHANGED) != 0) {
	    for (int i = 0; i < numLgts; i++) {
		mLgts[i].color.set(((Color3f)objs[4]));
	    }
	}
	else if ((component & ENABLE_CHANGED) != 0) {
	    for (int i = 0; i < numLgts; i++) 
		mLgts[i].lightOn = ((Boolean)objs[4]).booleanValue();
	}
	else if ((component & BOUNDS_CHANGED) != 0) {
	    for (int i = 0; i < numLgts; i++) {
		mLgts[i].regionOfInfluence = (Bounds) objs[4];
		if (mLgts[i].boundingLeaf == null) {
		    if (objs[4] != null) {
			mLgts[i].region = 
			    ((Bounds)mLgts[i].regionOfInfluence).copy(mLgts[i].region);
			mLgts[i].region.transform(mLgts[i].regionOfInfluence,
						  mLgts[i].getCurrentLocalToVworld());
		    }
		    else {
			mLgts[i].region = null;
		    }
		}
	    }
	}
	else if ((component & BOUNDINGLEAF_CHANGED) != 0) {
	    for (int i = 0; i < numLgts; i++) {
		mLgts[i].boundingLeaf=((BoundingLeafRetained)objs[4]);
		if (objs[4] != null) {
		    mLgts[i].region = (Bounds)mLgts[i].boundingLeaf.transformedRegion;
		}
		else { // evaluate regionOfInfluence if not null 
		    if (mLgts[i].regionOfInfluence != null) {
			mLgts[i].region = 
			    ((Bounds)mLgts[i].regionOfInfluence).copy(mLgts[i].region);
			mLgts[i].region.transform(mLgts[i].regionOfInfluence, 
						  mLgts[i].getCurrentLocalToVworld());
		    }
		    else {
			mLgts[i].region = null;
		    }
		}
	    }
	}
	else if ((component & SCOPE_CHANGED) != 0) {
	    int nscopes, j, i;
	    GroupRetained group;
	    Vector currentScopes;
	    Object[] scopeList = (Object[])objs[4];
	    ArrayList addList = (ArrayList)scopeList[0];
	    ArrayList removeList = (ArrayList)scopeList[1];
	    boolean isScoped = ((Boolean)scopeList[2]).booleanValue();
	    
	    if (addList != null) {
		for (i = 0; i < numLgts; i++) {
		    mLgts[i].isScoped = isScoped;
		    for (j = 0; j < addList.size(); j++) {
			Shape3DRetained obj = ((GeometryAtom)addList.get(j)).source;
			obj.addLight(mLgts[i]);
		    }
		}
	    }
	    
	    if (removeList != null) {
		for (i = 0; i < numLgts; i++) {
		    mLgts[i].isScoped = isScoped;
		    for (j = 0; j < removeList.size(); j++) {
			Shape3DRetained obj = ((GeometryAtom)removeList.get(j)).source;
			((Shape3DRetained)obj).removeLight(mLgts[i]);
		    }
		}
	    }
	}


    }

    
    
    // The update Object function called during RenderingEnv objUpdate
    // Note : if you add any more fields here , you need to update
    // updateLight() in RenderingEnvironmentStructure
    void updateMirrorObject(Object[] objs) {

	Transform3D trans = null;
	int component = ((Integer)objs[1]).intValue();
	int numLgts = ((Integer)objs[2]).intValue();
	LightRetained[] mLgts = (LightRetained[]) objs[3];

	if ((component & COLOR_CHANGED) != 0) {
	    for (int i = 0; i < numLgts; i++) {
		mLgts[i].color.set(((Color3f)objs[4]));
	    }
	}

	if ((component & INIT_MIRROR) != 0) {
	    for (int i = 0; i < numLgts; i++) {
		Color3f clr = (Color3f) ((Object[])objs[4])[6];
		mLgts[i].color.set(clr);
	    }
	}
    }

    /** Note: This routine will only be called on
     * the mirror object - will update the object's
     * cached region and transformed region 
     */

    void updateBoundingLeaf() {
	// This is necessary, if for example, the region
	// changes from sphere to box.
        if (boundingLeaf != null && boundingLeaf.switchState.currentSwitchOn) {
            region = boundingLeaf.transformedRegion;
        } else { // evaluate regionOfInfluence if not null
            if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
                region.transform(regionOfInfluence, getCurrentLocalToVworld());
            } else {
                region = null;
            }
        }
    }
    void getMirrorObjects(ArrayList leafList, HashKey key) {
	if (!inSharedGroup) {
	    leafList.add(mirrorLights[0]);
	}
	else {
	    for (int i=0; i<numMirrorLights; i++) {
		if (mirrorLights[i].key.equals(key)) {
		    leafList.add(mirrorLights[i]);
		    break;
		}
	    }
	    
	}
    }
    /**
     * This gets the mirror light for this light given the key.
     */
    LightRetained getMirrorLight(HashKey key) {
	int i;
	LightRetained[] newLights;

	if (inSharedGroup) {
	    for (i=0; i<numMirrorLights; i++) {
		if (mirrorLights[i].key.equals(key)) {
		    return(mirrorLights[i]);
		}
	    }
	    if (numMirrorLights == mirrorLights.length) {
		newLights = new LightRetained[numMirrorLights*2];
		for (i=0; i<numMirrorLights; i++) {
		    newLights[i] = mirrorLights[i];
		}
		mirrorLights = newLights;
	    } 
	    //	    mirrorLights[numMirrorLights] = (LightRetained)
	    //	    this.clone(true);
	    mirrorLights[numMirrorLights] = (LightRetained) this.clone();
	    // If the bounding leaf is not null , add this
	    // mirror object as a user
	    if (boundingLeaf != null) {
		mirrorLights[numMirrorLights].boundingLeaf = this.boundingLeaf.mirrorBoundingLeaf;
		if (mirrorLights[numMirrorLights].boundingLeaf != null)
		    mirrorLights[numMirrorLights].boundingLeaf.addUser(mirrorLights[numMirrorLights]);
	    }
	    // mirrorLights[numMirrorLights].key = new HashKey(key);
	    mirrorLights[numMirrorLights].key = key;
	    mirrorLights[numMirrorLights].sgLight = this;
	    return(mirrorLights[numMirrorLights++]);
	} else {
	    if (mirrorLights[0] == null) {
		//mirrorLights[0] = (LightRetained) this.clone(true);
		mirrorLights[0] =  (LightRetained) this.clone();
		// If the bounding leaf is not null , add this
		// mirror object as a user
		if (boundingLeaf != null) {
		    mirrorLights[0].boundingLeaf = this.boundingLeaf.mirrorBoundingLeaf;
		    if (mirrorLights[0].boundingLeaf != null)
			mirrorLights[0].boundingLeaf.addUser(mirrorLights[0]);	        
		}
		mirrorLights[0].sgLight = this;
	    }
	    return(mirrorLights[0]);
	}
    }

    void setLive(SetLiveState s) {
	LightRetained ml;
	int i, j;

	newlyAddedMirrorLights.clear();
        if (inImmCtx) {
	    throw new IllegalSharingException(J3dI18N.getString("LightRetained0"));
	}
        super.doSetLive(s);

	if (s.inSharedGroup) {
	    for (i=0; i<s.keys.length; i++) {
		ml = this.getMirrorLight(s.keys[i]);
 		ml.localToVworld = new Transform3D[1][];
 		ml.localToVworldIndex = new int[1][];

		j = s.keys[i].equals(localToVworldKeys, 0,
				     localToVworldKeys.length);
		if(j < 0) {
		    System.out.println("LightRetained : Can't find hashKey"); 
		}
		
		ml.localToVworld[0] = localToVworld[j];
		ml.localToVworldIndex[0] = localToVworldIndex[j];
		// If its view Scoped, then add this list
		// to be sent to Rendering Env
		if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		    s.viewScopedNodeList.add(ml);
		    s.scopedNodesViewList.add(s.viewLists.get(i));
		} else {
		    s.nodeList.add(ml);
		}
		
		newlyAddedMirrorLights.add(ml);
		if (boundingLeaf != null) {
		    boundingLeaf.mirrorBoundingLeaf.addUser(ml);
		}
                if (s.transformTargets != null &&
		    s.transformTargets[i] != null) {
                    s.transformTargets[i].addNode(ml, Targets.ENV_TARGETS);
		    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
		}
                if (s.switchTargets != null &&
		    s.switchTargets[i] != null) {
                    s.switchTargets[i].addNode(ml, Targets.ENV_TARGETS);
                }
	        ml.switchState = (SwitchState)s.switchStates.get(j);
		
	    }
	} else {
	    ml = this.getMirrorLight(null);
	    ml.localToVworld = new Transform3D[1][];
	    ml.localToVworldIndex = new int[1][];
	    ml.localToVworld[0] = this.localToVworld[0];
	    ml.localToVworldIndex[0] = this.localToVworldIndex[0];
	    // Initialization of the mirror object 
	    // If its view Scoped, then add this list
	    // to be sent to Rendering Env
	    //	    System.out.println("lightSetLive, s.viewList = "+s.viewLists);
	    if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		s.viewScopedNodeList.add(ml);
		s.scopedNodesViewList.add(s.viewLists.get(0));
	    } else {
		s.nodeList.add(ml);
	    }
	    newlyAddedMirrorLights.add(ml);
	    if (boundingLeaf != null) {
		boundingLeaf.mirrorBoundingLeaf.addUser(ml);
	    }
            if (s.transformTargets != null &&
		s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ml, Targets.ENV_TARGETS);
		s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
	    }
            if (s.switchTargets != null &&
                        s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(ml, Targets.ENV_TARGETS);
            }
	    ml.switchState = (SwitchState)s.switchStates.get(0);
	}
	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;
	super.markAsLive();

    }

    J3dMessage initMessage(int  num) {
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	createMessage.universe = universe;
	createMessage.type = J3dMessage.LIGHT_CHANGED;
	createMessage.args[0] = this;
	// a snapshot of all attributes that needs to be initialized
	// in the mirror object
	createMessage.args[1]= new Integer(INIT_MIRROR);

	LightRetained[] mlts = new LightRetained[newlyAddedMirrorLights.size()];
	for (int i = 0; i < mlts.length; i++) {
	    mlts[i] = (LightRetained)newlyAddedMirrorLights.get(i);
	}
	createMessage.args[2] = new Integer(mlts.length);
	createMessage.args[3] = mlts;

	Object[] obj = new Object[num];
	obj[0] = boundingLeaf;
	obj[1] = (regionOfInfluence != null?regionOfInfluence.clone():null);
	obj[2] = (inBackgroundGroup? Boolean.TRUE:Boolean.FALSE);
	obj[3] = geometryBackground;
	obj[4] = (lightOn? Boolean.TRUE:Boolean.FALSE);

	ArrayList addScopeList = new ArrayList();
	for (int i = 0; i < scopes.size(); i++) {
	    GroupRetained group = (GroupRetained)scopes.get(i);
	    tempKey.reset();
	    group.addAllNodesForScopedLight(mlts.length, mlts, addScopeList, tempKey);
	}
	Object[] scopeInfo = new Object[2];
	scopeInfo[0] = ((scopes.size() > 0) ? Boolean.TRUE:Boolean.FALSE);
	scopeInfo[1] = addScopeList;
	obj[5] = scopeInfo;
	Color3f clr = new Color3f(color);
	obj[6] = clr;
	createMessage.args[4] = obj;
	return createMessage;
	
    }

    // The default set of clearLive actions
    void clearLive(SetLiveState s) {
	LightRetained ml;
	newlyAddedMirrorLights.clear();
	super.clearLive(s);

	if (inSharedGroup) {
	    for (int i=0; i<s.keys.length; i++) {
		ml = this.getMirrorLight(s.keys[i]);
                if (s.transformTargets != null &&
		    s.transformTargets[i] != null) {
                    s.transformTargets[i].addNode(ml, Targets.ENV_TARGETS);
		    s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
                }
		newlyAddedMirrorLights.add(ml);
		// Remove this mirror light as users of the bounding leaf 
		if (ml.boundingLeaf != null) {
		    ml.boundingLeaf.removeUser(ml);
		    ml.boundingLeaf = null;
		}
                if (s.switchTargets != null &&
                        s.switchTargets[i] != null) {
                    s.switchTargets[i].addNode(ml, Targets.ENV_TARGETS);
                }
		if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		    s.viewScopedNodeList.add(ml);
		    s.scopedNodesViewList.add(s.viewLists.get(i));
		} else {
		    s.nodeList.add(ml);
		}
	    }
	} else {
	    ml = this.getMirrorLight(null);
	    
	    // Remove this mirror light as users of the bounding leaf 
	    if (ml.boundingLeaf != null) {
	        ml.boundingLeaf.removeUser(ml);
		ml.boundingLeaf = null;
	    }
	    if (s.switchTargets != null &&
		s.switchTargets[0] != null) {
                s.switchTargets[0].addNode(ml, Targets.ENV_TARGETS);
	    }
	    if ((s.viewScopedNodeList != null) && (s.viewLists != null)) {
		s.viewScopedNodeList.add(ml);
		//System.out.println("s.viewList is " + s.viewLists); 
		s.scopedNodesViewList.add(s.viewLists.get(0));
	    } else {
		s.nodeList.add(ml);
	    }
	    if (s.transformTargets != null &&
		s.transformTargets[0] != null) {
                s.transformTargets[0].addNode(ml, Targets.ENV_TARGETS);
		s.notifyThreads |= J3dThread.UPDATE_TRANSFORM;
            }


	    newlyAddedMirrorLights.add(ml);
	}
	s.notifyThreads |= J3dThread.UPDATE_RENDERING_ENVIRONMENT|
	    J3dThread.UPDATE_RENDER;


	
	if (scopes.size() > 0) { 
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    LightRetained[] mlts = new LightRetained[newlyAddedMirrorLights.size()];
	    for (int i = 0; i < mlts.length; i++) {
		mlts[i] = (LightRetained)newlyAddedMirrorLights.get(i);
	    }
	    createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
	    createMessage.universe = universe;
	    createMessage.type = J3dMessage.LIGHT_CHANGED;
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(CLEAR_MIRROR);
	    ArrayList removeScopeList = new ArrayList();
	    for (int i = 0; i < scopes.size(); i++) {
		GroupRetained group = (GroupRetained)scopes.get(i);
		tempKey.reset();
		group.removeAllNodesForScopedLight(mlts.length, mlts, removeScopeList, tempKey);
	    }
	    createMessage.args[2] = removeScopeList;
	    createMessage.args[3] = new Integer(mlts.length);
	    createMessage.args[4] = mlts;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

    void clearMirrorObject(Object[] args) {
	Shape3DRetained shape;
	ArrayList shapeList = (ArrayList)args[2];
	ArrayList removeScopeList = new ArrayList();
	LightRetained[] mLgts = (LightRetained[]) args[4];
	int numLgts = ((Integer)args[3]).intValue();

	for (int k = 0; k < numLgts; k++) {
	    for (int i = 0; i < shapeList.size(); i++) {
		shape = ((GeometryAtom)shapeList.get(i)).source;
		shape.removeLight(mLgts[k]);
	    }
	    mLgts[k].isScoped = false;

	}

    }
    

   
    /**
     * Clones only the retained side, internal use only
     */
    protected Object clone() {
         LightRetained lr = (LightRetained)super.clone();
         lr.color = new Color3f(color);
	 lr.scopes = (Vector) scopes.clone();
	 lr.initInfluencingBoundingLeaf(getInfluencingBoundingLeaf());
 	 lr.region = null;
         lr.lightDirty = 0xffff;
         lr.sgLightDirty = 0xffff;
         lr.universe = null;
         lr.isNeeded = false;
         lr.inImmCtx = false;
         lr.sgLight = null;
         lr.key = null;
         lr.mirrorLights = new LightRetained[1];
         lr.numMirrorLights = 0;
         lr.environmentSets = new UnorderList(1, EnvironmentSet.class);
         return lr;
    }


    // Called during RenderingEnv object update
    void updateTransformChange() {
    }

    // Called on mirror object and updated when message is received
    void updateImmediateTransformChange() {
	// If bounding leaf is null, tranform the bounds object
	if (boundingLeaf == null) {
	    if (regionOfInfluence != null) {
		region = regionOfInfluence.copy(region);
		region.transform(regionOfInfluence,
				 getCurrentLocalToVworld());
	    }
	    
	}
    }

    void sendMessage(int attrMask, Object attr) {
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = targetThreads;
	createMessage.type = J3dMessage.LIGHT_CHANGED;
	createMessage.universe = universe;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	if (inSharedGroup)
	    createMessage.args[2] = new Integer(numMirrorLights);
	else
	    createMessage.args[2] = new Integer(1);

	createMessage.args[3] = mirrorLights.clone();
	createMessage.args[4] = attr;
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
        if (regionOfInfluence != null) {
            regionOfInfluence.transform(xform.transform);
        }
    }
}
 
