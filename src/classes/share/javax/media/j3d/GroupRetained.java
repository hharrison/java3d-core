/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Vector;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Group node.
 */

class GroupRetained extends NodeRetained implements BHLeafInterface {
    /**
     * The Group Node's children vector.
     */
    ArrayList children = new ArrayList(1);

    /**
     * The Group node's collision bounds in local coordinates.
     */
    Bounds collisionBound = null;

    // The locale that this node is decended from
    Locale locale = null;

    // The list of lights that are scoped to this node
    // One such arraylist per path. If not in sharedGroup
    // then only index 0 is valid
    ArrayList lights = null;

    // The list of fogs that are scoped to this node
    // One such arraylist per path. If not in sharedGroup
    // then only index 0 is valid
    ArrayList fogs = null;

    // The list of model clips that are scoped to this node
    // One such arraylist per path. If not in sharedGroup
    // then only index 0 is valid
    ArrayList modelClips = null;


    // The list of alternateappearance that are scoped to this node
    // One such arraylist per path. If not in sharedGroup
    // then only index 0 is valid
    ArrayList altAppearances = null;


    // indicates whether this Group node can be the target of a collision
    boolean collisionTarget = false;

    // per child switchLinks
    ArrayList childrenSwitchLinks = null;

    // the immediate childIndex of a parentSwitchLink
    int parentSwitchLinkChildIndex = -1;

    // per shared path ordered path data
    ArrayList orderedPaths = null;
    
    /**
     * If collisionBound is set, this is equal to the
     * transformed collisionBounds, otherwise it is equal
     * to the transformed localBounds.
     * This variable is set to null unless collisionTarget = true.
     * This bound is only used by mirror Group.
     */
    BoundingBox collisionVwcBounds;

    /**
     * Mirror group of this node, it is only used when
     * collisionTarget = true. Otherwise it is set to null.
     * If not in shared group,
     * only entry 0 is used.
     *
     */
    ArrayList mirrorGroup;

    /**
     * key of mirror GroupRetained.
     */
    HashKey key;

    /**
     * sourceNode of this mirror Group 
     */
    GroupRetained sourceNode;

    /**
     * The BHLeafNode for this GeometryAtom.
     */
    BHLeafNode bhLeafNode = null;

    //
    // The following variables are used during compile
    //

    // true if this is the root of the scenegraph tree
    boolean isRoot = false;

    boolean allocatedLights = false;

    boolean allocatedFogs = false;

    boolean allocatedMclips = false;

    boolean allocatedAltApps = false;

    // > 0 if this group is being used in scoping
    int scopingRefCount = 0;

    
    ArrayList compiledChildrenList = null;

    boolean isInClearLive = false;

    // List of viewes scoped to this Group, for all subclasses
    // of group, except ViewSpecificGroup its a pointer to closest
    // ViewSpecificGroup parent
    // viewList for this node, if inSharedGroup is
    // false then only viewList(0) is valid
    // For VSGs, this list is an intersection of
    // higher level VSGs
    ArrayList viewLists = null;

    // True if this Node is descendent of ViewSpecificGroup;
    boolean inViewSpecificGroup = false;

    GroupRetained() {
        this.nodeType = NodeRetained.GROUP;
	localBounds = new BoundingSphere();
	((BoundingSphere)localBounds).setRadius( -1.0 );
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
	    J3dMessage message = VirtualUniverse.mc.getMessage();
	    message.type = J3dMessage.COLLISION_BOUND_CHANGED;
            message.threads = J3dThread.UPDATE_TRANSFORM |
		J3dThread.UPDATE_GEOMETRY;
	    message.universe = universe;
	    message.args[0] = this;
	    VirtualUniverse.mc.processMessage(message);
	}

    } 


    /**
     * Gets the collision bounds of a node.
     * @return the node's bounding object
     */
    Bounds getCollisionBounds() {
	return (collisionBound == null ? null : (Bounds)collisionBound.clone());
    } 
	    
    /**
     * Replaces the specified child with the child provided.
     * @param child the new child
     * @param index which child to replace
     */
    void setChild(Node child, int index) {

	checkValidChild(child, "GroupRetained0");
	if (this.source.isLive()) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
	        doSetChild(child, index);
		universe.setLiveState.clear();	
	    }
	    universe.waitForMC();

	} else {
	    doSetChild(child, index);
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();	
		}		
	    }
	}
    }
    
    // The method that does the work once the lock is acquired.
    void doSetChild(Node child, int index) {
	NodeRetained oldchildr;
	J3dMessage[] messages = null;
	int numMessages = 0;
	int attachStartIndex = 0;
	

	// since we want to make sure the replacement of the child
        // including removal of the oldChild and insertion of the newChild
	// all happen in the same frame, we'll send all the necessary
	// messages to masterControl for processing in one call.
	// So let's first find out how many messages will be sent
	
	oldchildr = (NodeRetained) children.get(index);

	if (this.source.isLive()) {
	    if (oldchildr != null) {
	        numMessages+=3;		// REMOVE_NODES, ORDERED_GROUP_REMOVED
		                        // VIEWSPECIFICGROUP_CLEAR
	        attachStartIndex = 3;
	    }

	    if (child != null) {
	        numMessages+=4;	// INSERT_NODES,BEHAVIOR_ACTIVATE,ORDERED_GROUP_INSERTED,
		                // VIEWSPECIFICGROUP_INIT
	    }

	    messages = new J3dMessage[numMessages];
	    for (int i = 0; i < numMessages; i++) {
	         messages[i] = VirtualUniverse.mc.getMessage();
	    }
	}

	if(oldchildr != null) {
	    oldchildr.setParent(null);
	    checkClearLive(oldchildr, messages, 0, index, null);
	}
	removeChildrenData(index);

	if(child == null) {
	    children.set(index, null);
	    if (messages != null) {
	        VirtualUniverse.mc.processMessage(messages);
	    }
	    return;
	}
	
	NodeRetained childr = (NodeRetained) child.retained;
	childr.setParent(this);
	children.set(index, childr);


	insertChildrenData(index);
	checkSetLive(childr, index, messages, attachStartIndex, null);
	if (this.source.isLive()) {
	    ((BranchGroupRetained)childr).isNew = true;
	}

	if (messages != null) {
	    VirtualUniverse.mc.processMessage(messages);
	}
    }
    
    /**
     * Inserts the specified child at specified index.
     * @param child the new child
     * @param index position to insert new child at
     */
    void insertChild(Node child, int index) {

	checkValidChild(child, "GroupRetained1");
	if (this.source.isLive()) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
	        doInsertChild(child, index);
		universe.setLiveState.clear();	
	    }
	    universe.waitForMC();
	} else {
	    doInsertChild(child, index);
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();	
		}		
	    }
	}
    }
    
    // The method that does the work once the lock is acquired.
    void doInsertChild(Node child, int index) {
	int i;
	NodeRetained childi;

	insertChildrenData(index);
	for (i=index; i<children.size(); i++) {
	    childi = (NodeRetained) children.get(i);
	    if(childi != null)
		childi.childIndex++;
	}	
	if(child==null) {
	    children.add(index, null);
	    return;
	}
	
	NodeRetained childr = (NodeRetained) child.retained;
	childr.setParent(this);
	children.add(index, childr);
	checkSetLive(childr, index, null, 0, null);
	if (this.source.isLive()) {
	    ((BranchGroupRetained)childr).isNew = true;
	}
    }
    
    /**
     * Removes the child at specified index.
     * @param index which child to remove
     */
    void removeChild(int index) {
	
	if (this.source.isLive()) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
	      doRemoveChild(index, null, 0);
	      universe.setLiveState.clear();	
	    }
	    universe.waitForMC();
	} else {
	    doRemoveChild(index, null, 0);
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();	
		}		
	    }
	}
    }

    /** 
     * Returns the index of the specified Node in this Group's list of Nodes
     * @param Node whose index is desired
     * @return index of the Node
     */
    int indexOfChild(Node child) {
	if(child != null) 
	    return children.indexOf((NodeRetained)child.retained);
	else
	    return children.indexOf(null);
    }

    /**
     * Removes the specified child from this Group's list of
     * children. If the specified child is not found, the method returns
     * quietly
     *
     * @param child to be removed
     */
    void removeChild(Node child) {
	int i = indexOfChild(child);
	if(i >= 0)
	    removeChild(i);
    }

    void removeAllChildren() {
	int n = children.size();
	for(int i = n-1; i >= 0; i--) {
	    removeChild(i);
	}
    }


    // The method that does the work once the lock is acquired.
    void doRemoveChild(int index, J3dMessage messages[], int messageIndex) {
	NodeRetained oldchildr, child;
	int i;
  
	oldchildr = (NodeRetained) children.get(index);

	int size = children.size();
	for (i=index; i<size; i++) {
	    child = (NodeRetained) children.get(i);
	    if(child != null)
		child.childIndex--;
	}

	if(oldchildr != null) {
	    oldchildr.setParent(null);
	    checkClearLive(oldchildr, messages, messageIndex, index, null);
	}
	
	children.remove(index);
	removeChildrenData(index);

        if (nodeType == NodeRetained.SWITCH) {
	    // force reEvaluation of switch children
	    SwitchRetained sg = (SwitchRetained)this;
	    sg.setWhichChild(sg.whichChild, true);
        }

    }
    
    /**
     * Returns the child specified by the index.
     * @param index which child to return
     * @return the children at location index
     */
    Node getChild(int index) {

	SceneGraphObjectRetained sgo = (SceneGraphObjectRetained) children.get(index);
	if(sgo == null)
	    return null;
	else 
	    return (Node) sgo.source;
    }
  
    /**
     * Returns an enumeration object of the children.
     * @return an enumeration object of the children
     */  
    Enumeration getAllChildren() {
        Vector userChildren=new Vector(children.size());
	SceneGraphObjectRetained sgo;
	
	for(int i=0; i<children.size(); i++) {
	    sgo = (SceneGraphObjectRetained)children.get(i);
	    if(sgo != null)
		userChildren.add(sgo.source);
	    else
		userChildren.add(null);
	}
	
        return userChildren.elements();
    }

    void checkValidChild(Node child, String s) {

	if ((child != null) &&
            (((child instanceof BranchGroup) && 
	      (((BranchGroupRetained) child.retained).attachedToLocale)) ||
	     (((NodeRetained)child.retained).parent != null))) {
		throw new MultipleParentException(J3dI18N.getString(s));
	}
    }

    /**
     * Appends the specified child to this node's list of children.
     * @param child the child to add to this node's list of children
     */
    void addChild(Node child) {
	checkValidChild(child, "GroupRetained2");
 
	if (this.source.isLive()) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
	        doAddChild(child, null, 0);
		universe.setLiveState.clear();	
	    }
	    universe.waitForMC();
	} else {
	    doAddChild(child, null, 0);
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();	
		}		
	    }
	}
    }
    
    // The method that does the work once the lock is acquired.
    void doAddChild(Node child, J3dMessage messages[], int messageIndex) {

	appendChildrenData();

	if(child == null) { 
	    children.add(null);
	    return;
	}
	    
	NodeRetained childr = (NodeRetained) child.retained;
	childr.setParent(this);
	children.add(childr);
	checkSetLive(childr, children.size()-1, messages, messageIndex, null);
	if (this.source.isLive()) {
	    ((BranchGroupRetained)childr).isNew = true;
	}

    }
    
    void moveTo(BranchGroup bg) {
	if (this.source.isLive()) {
	    universe.resetWaitMCFlag();
	    synchronized (universe.sceneGraphLock) {
	        doMoveTo(bg);
		universe.setLiveState.clear();	
	    }
	    universe.waitForMC();
	} else {
	    doMoveTo(bg);
	    if (universe != null) {
		synchronized (universe.sceneGraphLock) {
		    universe.setLiveState.clear();	
		}		
	    }
	}
    }
    
    // The method that does the work once the lock is acquired.
    void doMoveTo(BranchGroup branchGroup) {
	J3dMessage messages[] = null;
	int numMessages = 0;
	int detachStartIndex = 0;
	int attachStartIndex = 0;	    
	if(branchGroup != null) {
	    BranchGroupRetained bg = (BranchGroupRetained) branchGroup.retained;
	    GroupRetained g = (GroupRetained)bg.parent;

	    // Find out how many messages to be created
	    // Note that g can be NULL if branchGroup parent is
	    // a Locale, in this case the following condition 
	    // will fail.
	    // Figure out the number of messages based on whether the group
	    // from which its moving from is live and group to which its
	    // moving to is live
	    if (g != null) {
		if (g.source.isLive()) {
		    numMessages = 3; // REMOVE_NODES, ORDERED_GROUP_REMOVED,VIEWSPECIFICGROUP_CLEAR
		    attachStartIndex = 3;
		}
		else {
		    numMessages = 0;
		    attachStartIndex = 0;
		}
		    
	    }
	    else { // Attached to locale
		numMessages = 3; // REMOVE_NODES, ORDERED_GROUP_REMOVED, VIEWSPECIFICGROUP_CLEAR
		attachStartIndex = 3;
	    }
	    // Now, do the evaluation for the group that its going to be
	    // attached to ..
	    if (this.source.isLive()) {
		numMessages+=4;		// INSERT_NODES, BEHAVIOR_ACTIVATE
		                        // ORDERED_GROUP_INSERTED, VIEWSPECIFICGROUP_INIT

	    }		
	    messages = new J3dMessage[numMessages];
	    for (int i=0; i<numMessages; i++) {
		messages[i] = VirtualUniverse.mc.getMessage();
		messages[i].type = J3dMessage.INVALID_TYPE;
	    }

	    // Remove it from it's parents state
	    if (g == null) {
		if (bg.locale != null) {
		    bg.locale.doRemoveBranchGraph(branchGroup, 
						  messages, detachStartIndex);
		}
	    } else {
		g.doRemoveChild(g.children.indexOf(bg),
				messages, 
				detachStartIndex);
	    }
	}

	
	// Add it to it's new parent
	doAddChild(branchGroup, messages, attachStartIndex);

	if (numMessages > 0) {
	    int count = 0;
	    for (int i=0; i < numMessages; i++) {
		if (messages[i].type != J3dMessage.INVALID_TYPE) {
		    count++;
		}
	    }
	    if (count == numMessages) {
		// in most cases
		VirtualUniverse.mc.processMessage(messages);
	    } else {
		J3dMessage ms[] = null;

		if (count > 0) {
		    ms = new J3dMessage[count];
		}
		 
		int k=0;
		for (int i=0; i < numMessages; i++) {
		    if (messages[i].type != J3dMessage.INVALID_TYPE) {
			ms[k++] = messages[i];
		    } else {
			VirtualUniverse.mc.addMessageToFreelists(messages[i]);
		    }
		}		
		if (ms != null) {
		    VirtualUniverse.mc.processMessage(ms);
		}
	    }
	}
    }


    /**
     * Returns a count of this nodes' children.
     * @return the number of children descendant from this node
     */
    int numChildren() {
	return children.size();
    }
  
    // Remove a light from the list of lights
    void removeLight(int numLgt, LightRetained[] removelight, HashKey key) {
	ArrayList l;
	int index;
	if (inSharedGroup) {
	    int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	    l = (ArrayList)lights.get(hkIndex);
	    if (l != null) {
		for (int i = 0; i < numLgt; i++) {
		    index = l.indexOf(removelight[i]);
		    l.remove(index);
		}
	    }
	}
	else {
	    l = (ArrayList)lights.get(0);
	    for (int i = 0; i < numLgt; i++) {
		index = l.indexOf(removelight[i]);
		l.remove(index);
	    }
	}

	/*
	// TODO: lights may remove twice or more during clearLive(),
	// one from itself and one call from every LightRetained
	// reference this.  So there is case that this procedure get
	// called when light already removed.
	if (i >= 0) 
	    lights.remove(i);
	*/
    }


    void addAllNodesForScopedLight(int numLgts,
				   LightRetained[] ml,
				   ArrayList list,
				   HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processAllNodesForScopedLight(numLgts,  ml, list,  k);
	    }
	}
	else {
	    processAllNodesForScopedLight(numLgts, ml,  list,  k);
	}
    }

    void processAllNodesForScopedLight(int numLgts, LightRetained[] ml, ArrayList list, HashKey k) {
	if (allocatedLights) {
	    addLight(ml, numLgts, k);
	}
	if (this.source.isLive() || this.isInSetLive()) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained  && (child.source.isLive() || child.isInSetLive()))
			((GroupRetained)child).processAllNodesForScopedLight(numLgts, ml, list, k);
		    else if (child instanceof LinkRetained && (child.source.isLive()|| child.isInSetLive())) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processAllNodesForScopedLight(numLgts, ml, list, k.append("+").
						       append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive()) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list, k);
		    }
		}
	    }
	}
    }

    // If its a group, then add the scope to the group, if
    // its a shape, then keep a list to be added during
    // updateMirrorObject
    void removeAllNodesForScopedLight(int numLgts, LightRetained[] ml, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processRemoveAllNodesForScopedLight(numLgts, ml,  list,  k);
	    }
	}
	else {
	    processRemoveAllNodesForScopedLight(numLgts, ml,  list,  k);
	}
    }

    void processRemoveAllNodesForScopedLight(int numLgts, LightRetained[] ml, ArrayList list, HashKey k) {
	if (allocatedLights) {
	    removeLight(numLgts,ml, k);
	}
	// If the source is live, then notify the children
	if (this.source.isLive() && !isInClearLive) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained &&(child.source.isLive() &&
							    ! ((GroupRetained)child).isInClearLive))
			((GroupRetained)child).processRemoveAllNodesForScopedLight(numLgts, ml,list, k);
		    else if (child instanceof LinkRetained && child.source.isLive()) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processRemoveAllNodesForScopedLight(numLgts, ml, list, k.append("+").
							append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive() ) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list,  k);
		    }
		}
	    }
	}
    }
    

    void addAllNodesForScopedFog(FogRetained mfog, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processAddNodesForScopedFog(mfog,  list,  k);
	    }
	}
	else {
	    processAddNodesForScopedFog(mfog,  list,  k);
	}
    }

    void processAddNodesForScopedFog(FogRetained mfog, ArrayList list, HashKey k) {
	// If this group has it own scoping list then add .. 
	if (allocatedFogs) 
	    addFog(mfog, k);
	// If the source is live, then notify the children
	if (this.source.isLive() || this.isInSetLive()) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained && (child.source.isLive()|| child.isInSetLive()))
			((GroupRetained)child).processAddNodesForScopedFog(mfog, list, k);
		    else if (child instanceof LinkRetained && (child.source.isLive()||child.isInSetLive() )) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processAddNodesForScopedFog(mfog, list,  k.append("+").
						     append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive()) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list, k);
		    }
		}
	    }
	}
    }
    
    // If its a group, then add the scope to the group, if
    // its a shape, then keep a list to be added during
    // updateMirrorObject
    void removeAllNodesForScopedFog(FogRetained mfog, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processRemoveAllNodesForScopedFog(mfog,  list,  k);
	    }
	}
	else {
	    processRemoveAllNodesForScopedFog(mfog,  list,  k);
	}
    }
    void processRemoveAllNodesForScopedFog(FogRetained mfog, ArrayList list, HashKey k) {
	// If the source is live, then notify the children
	if (allocatedFogs) 
	    removeFog(mfog, k);
	if (this.source.isLive() && !isInClearLive) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained &&(child.source.isLive() &&
							    ! ((GroupRetained)child).isInClearLive))
			((GroupRetained)child).processRemoveAllNodesForScopedFog(mfog, list, k);
		    else if (child instanceof LinkRetained && child.source.isLive()) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processRemoveAllNodesForScopedFog(mfog, list, k.append("+").
							append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive() ) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list,  k);
		    }
		}
	    }
	}
    }

    void addAllNodesForScopedModelClip(ModelClipRetained mModelClip, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processAddNodesForScopedModelClip(mModelClip,  list,  k);
	    }
	}
	else {
	    processAddNodesForScopedModelClip(mModelClip,  list,  k);
	}
    }
    
    void processAddNodesForScopedModelClip(ModelClipRetained mModelClip,
				       ArrayList list,
					HashKey k) {
	if (allocatedMclips)
	    addModelClip(mModelClip, k);
	// If the source is live, then notify the children
	if (this.source.isLive() || this.isInSetLive()) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained && (child.source.isLive()||child.isInSetLive() ))
			((GroupRetained)child).processAddNodesForScopedModelClip(
									      mModelClip, list,  k);
		    else if (child instanceof LinkRetained && (child.source.isLive()||child.isInSetLive() )) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processAddNodesForScopedModelClip(mModelClip, list,
							   k.append("+").append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive()) {
			((Shape3DRetained)child).getMirrorObjects(list,  k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list, k);
		    }
		}
	    }
	}
    }
    void removeAllNodesForScopedModelClip(ModelClipRetained mModelClip, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processRemoveAllNodesForScopedModelClip(mModelClip,  list,  k);
	    }
	}
	else {
	    processRemoveAllNodesForScopedModelClip(mModelClip,  list,  k);
	}
	
    }

    // If its a group, then add the scope to the group, if
    // its a shape, then keep a list to be added during
    // updateMirrorObject
    void processRemoveAllNodesForScopedModelClip(ModelClipRetained mModelClip, ArrayList list, HashKey k) {
	// If the source is live, then notify the children
	if (allocatedMclips)
	    removeModelClip(mModelClip, k);
	if (this.source.isLive() && !isInClearLive) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained &&(child.source.isLive() &&
							    ! ((GroupRetained)child).isInClearLive))
			((GroupRetained)child).processRemoveAllNodesForScopedModelClip(mModelClip, list, k);
		    else if (child instanceof LinkRetained && child.source.isLive()) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processRemoveAllNodesForScopedModelClip(mModelClip, list, k.append("+").
							append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive() ) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list,  k);
		    }
		}
	    }
	}
    }

    void addAllNodesForScopedAltApp(AlternateAppearanceRetained mAltApp, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processAddNodesForScopedAltApp(mAltApp,  list,  k);
	    }
	}
	else {
	    processAddNodesForScopedAltApp(mAltApp,  list,  k);
	}
    }    

    // If its a group, then add the scope to the group, if
    // its a shape, then keep a list to be added during
    // updateMirrorObject
    void processAddNodesForScopedAltApp(AlternateAppearanceRetained mAltApp, ArrayList list, HashKey k) {
	// If the source is live, then notify the children
	if (allocatedAltApps)
	    addAltApp(mAltApp, k);
	if (this.source.isLive() || this.isInSetLive()) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained && (child.source.isLive() || child.isInSetLive()))
			((GroupRetained)child).processAddNodesForScopedAltApp(mAltApp, list, k);
		    else if (child instanceof LinkRetained && child.source.isLive()) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processAddNodesForScopedAltApp(mAltApp, list, k.append("+").
							append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive() ) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list,  k);
		    }
		}
	    }
	}
    }

    void removeAllNodesForScopedAltApp(AlternateAppearanceRetained mAltApp, ArrayList list, HashKey k) {
	if (inSharedGroup) {
	    for (int i = 0; i < localToVworldKeys.length; i++) {
		k.set(localToVworldKeys[i]);
		processRemoveNodesForScopedAltApp(mAltApp,  list,  k);
	    }
	}
	else {
	    processAddNodesForScopedAltApp(mAltApp,  list,  k);
	}
    }

    // If its a group, then add the scope to the group, if
    // its a shape, then keep a list to be added during
    // updateMirrorObject
    void processRemoveNodesForScopedAltApp(AlternateAppearanceRetained mAltApp, ArrayList list, HashKey k) {
	// If the source is live, then notify the children
	if (allocatedAltApps)
	    removeAltApp(mAltApp, k);
	if (this.source.isLive() && !isInClearLive) {
	    for (int i = children.size()-1; i >=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i);
		if(child != null) {
		    if (child instanceof GroupRetained &&(child.source.isLive() &&
							    ! ((GroupRetained)child).isInClearLive))
			((GroupRetained)child).processRemoveNodesForScopedAltApp(mAltApp, list, k);
		    else if (child instanceof LinkRetained && child.source.isLive()) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			((GroupRetained)(ln.sharedGroup)).
			    processRemoveNodesForScopedAltApp(mAltApp, list, k.append("+").
							append(ln.nodeId));
			k.count = lastCount;
		    } else if (child instanceof Shape3DRetained && child.source.isLive() ) {
			((Shape3DRetained)child).getMirrorObjects(list, k);
		    } else if (child instanceof MorphRetained && child.source.isLive()) {
			((MorphRetained)child).getMirrorObjects(list,  k);
		    }
		}
	    }
	}
    }

    synchronized void setLightScope() {
	// Make group's own copy
	ArrayList newLights;
	if (!allocatedLights) {
	    allocatedLights = true;
	    if (lights != null) {
		newLights = new ArrayList(lights.size());
		int size = lights.size();
		for (int i = 0; i < size; i++) {
		    ArrayList l = (ArrayList)lights.get(i);
		    if (l != null) {
			newLights.add(l.clone());
		    }
		    else {
			newLights.add(null);
		    }
		}
	    }
	    else {
		if (inSharedGroup) {
		    newLights = new ArrayList();
		    for (int i = 0; i < localToVworldKeys.length; i++) {
			newLights.add(new ArrayList());
		    }
		}
		else {
		    newLights = new ArrayList();
		    newLights.add(new ArrayList());
		}
	    }
	    lights = newLights;
	    
	}
	scopingRefCount++;
    }
    synchronized void removeLightScope() {
	scopingRefCount--;
    }


   synchronized void setFogScope() {
	// Make group's own copy
	ArrayList newFogs;
	if (!allocatedFogs) {
	    allocatedFogs = true;
	    if (fogs != null) {
		newFogs = new ArrayList(fogs.size());
		int size = fogs.size();
		for (int i = 0; i < size; i++) {
		    ArrayList l = (ArrayList)fogs.get(i);
		    if (l != null) {
			newFogs.add(l.clone());
		    }
		    else {
			newFogs.add(null);
		    }
		}
	    }
	    else {
		if (inSharedGroup) {
		    newFogs = new ArrayList();
		    for (int i = 0; i < localToVworldKeys.length; i++) {
			newFogs.add(new ArrayList());
		    };
		}
		else {
		    newFogs = new ArrayList();
		    newFogs.add(new ArrayList());
		}
	    }
	    fogs = newFogs;
	    
	}
	scopingRefCount++;
    }
    synchronized void removeFogScope() {
	scopingRefCount--;
    }


   synchronized void setMclipScope() {
	// Make group's own copy
	ArrayList newMclips;
	if (!allocatedMclips) {
	    allocatedMclips = true;
	    if (modelClips != null) {
		newMclips = new ArrayList(modelClips.size());
		int size = modelClips.size();
		for (int i = 0; i < size; i++) {
		    ArrayList l = (ArrayList)modelClips.get(i);
		    if (l != null) {
			newMclips.add(l.clone());
		    }
		    else {
			newMclips.add(null);
		    }
		}
	    }
	    else {
		if (inSharedGroup) {
		    newMclips =new ArrayList();
		    for (int i = 0; i < localToVworldKeys.length; i++) {
			newMclips.add(new ArrayList());
		    }
		}
		else {
		    newMclips = new ArrayList();
		    newMclips.add(new ArrayList());
		}
	    }
	    modelClips = newMclips;
	    
	}
	scopingRefCount++;
    }
    synchronized void removeMclipScope() {
	scopingRefCount--;
    }


   synchronized void setAltAppScope() {
	// Make group's own copy
	ArrayList newAltApps;
	if (!allocatedAltApps) {
	    allocatedAltApps = true;
	    if (altAppearances != null) {
		newAltApps = new ArrayList(altAppearances.size());
		int size = altAppearances.size();
		for (int i = 0; i < size; i++) {
		    ArrayList l = (ArrayList)altAppearances.get(i);
		    if (l != null) {
			newAltApps.add(l.clone());
		    }
		    else {
			newAltApps.add(null);
		    }
		}
	    }
	    else {
		if (inSharedGroup) {
		    newAltApps = new ArrayList();
		    for (int i = 0; i < localToVworldKeys.length; i++) {
			newAltApps.add(new ArrayList());
		    }
		}
		else {
		    newAltApps = new ArrayList();
		    newAltApps.add(new ArrayList());
		}
	    }
	    altAppearances = newAltApps;
	    
	}
	scopingRefCount++;
    }

    synchronized void removeAltAppScope() {
	scopingRefCount--;
    }    


    synchronized boolean usedInScoping() {
	return (scopingRefCount > 0);
    }
    
     // Add a light to the list of lights
     void addLight(LightRetained[] addlight, int numLgts, HashKey key) {
	 ArrayList l;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)lights.get(hkIndex);
	     if (l != null) {
		 for (int i = 0; i < numLgts; i++) {
		     l.add(addlight[i]);
		 }
	     }
	 }
	 else {
	     l = (ArrayList)lights.get(0);
	     for (int i = 0; i < numLgts; i++) {
		 l.add(addlight[i]);
	     }
	 }

     }
      // Add a fog to the list of fogs
      void addFog(FogRetained fog, HashKey key) {
	 ArrayList l;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)fogs.get(hkIndex);
	     if (l != null) {
		 l.add(fog);
	     }
	 }
	 else {
	     l = (ArrayList)fogs.get(0);
	     l.add(fog);
	 }
 
      }
 
      // Add a ModelClip to the list of ModelClip
      void addModelClip(ModelClipRetained modelClip, HashKey key) {
	 ArrayList l;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)modelClips.get(hkIndex);
	     if (l != null) {
		 l.add(modelClip);
	     }
	 }
	 else {
	     l = (ArrayList)modelClips.get(0);
	     l.add(modelClip);
	 }

      }
      // Add a alt appearance to the list of alt appearance
      void addAltApp(AlternateAppearanceRetained altApp, HashKey key) {
	 ArrayList l;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)altAppearances.get(hkIndex);
	     if (l != null) {
		 l.add(altApp);
	     }
	 }
	 else {
	     l = (ArrayList)altAppearances.get(0);
	     l.add(altApp);
	 }
 
      }
 
   
    // Remove a fog from the list of fogs
    void removeFog(FogRetained fog, HashKey key) {
	 ArrayList l;
	 int index;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)fogs.get(hkIndex);
	     if (l != null) {
		 index = l.indexOf(fog);
		 l.remove(index);
	     }
	 }
	 else {
	     l = (ArrayList)fogs.get(0);
	     index = l.indexOf(fog);
	     l.remove(index);
	 }

     }


    // Remove a ModelClip from the list of ModelClip
    void removeModelClip(ModelClipRetained modelClip, HashKey key) {
	 ArrayList l;
	 int index;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)modelClips.get(hkIndex);
	     if (l != null) {
		 index = l.indexOf(modelClip);
		 l.remove(index);
	     }
	 }
	 else {
	     l = (ArrayList)modelClips.get(0);
	     index = l.indexOf(modelClip);
	     l.remove(index);
	 }
     }



    // Remove a fog from the list of alt appearance
    void removeAltApp(AlternateAppearanceRetained altApp, HashKey key) {
	 ArrayList l;
	 int index;
	 if (inSharedGroup) {
	     int hkIndex = key.equals(localToVworldKeys, 0, localToVworldKeys.length);
	     l = (ArrayList)altAppearances.get(hkIndex);
	     if (l != null) {
		 index = l.indexOf(altApp);
		 l.remove(index);
	     }
	 }
	 else {
	     l = (ArrayList)altAppearances.get(0);
	     index = l.indexOf(altApp);
	     l.remove(index);
	 }

     }
    
    
    void updatePickable(HashKey keys[], boolean pick[]) {
	int nchild = children.size()-1;
	super.updatePickable(keys, pick);
	int i=0;
	NodeRetained child;

	for (i = 0; i < nchild; i++) {
	    child = (NodeRetained)children.get(i);
	    if(child != null)
		child.updatePickable(keys, (boolean []) pick.clone());
	}
	// No need to clone for the last value

	child = (NodeRetained)children.get(i);
	if(child != null) 
	    child.updatePickable(keys, pick);

    }
    

    void updateCollidable(HashKey keys[], boolean collide[]) {
	int nchild = children.size()-1;
	super.updateCollidable(keys, collide);
	int i=0;
	NodeRetained child;
	
	for (i = 0; i < nchild; i++) {
	    child = (NodeRetained)children.get(i);
	    if(child != null) 
		child.updateCollidable(keys, (boolean []) collide.clone()); 
	}
	// No need to clone for the last value
	child = (NodeRetained)children.get(i);
	if(child != null)
	    child.updateCollidable(keys, collide);
    }

    void setAlternateCollisionTarget(boolean target) {
	if (collisionTarget == target)
	    return;

	collisionTarget = target;

	if (source.isLive()) {
	    // Notify parent TransformGroup to add itself
	    // Since we want to update collisionVwcBounds when
	    // transform change in TransformStructure.
	    TransformGroupRetained tg;
	    J3dMessage message = VirtualUniverse.mc.getMessage();
	    message.threads = J3dThread.UPDATE_GEOMETRY;
	    message.universe = universe;
	    // send message to GeometryStructure to add/remove this
	    // group node in BHTree as AlternateCollisionTarget

	    int numPath;
	    CachedTargets newCtArr[] = null;

	    if (target) {
		createMirrorGroup();

                TargetsInterface ti = getClosestTargetsInterface(
                                        TargetsInterface.TRANSFORM_TARGETS);
                if (ti != null) {

                    // update targets
	            CachedTargets ct;
		    Targets targets = new Targets();
		    numPath = mirrorGroup.size();
		    newCtArr = 	new CachedTargets[numPath];
		    for (int i=0; i<numPath; i++) {
                        ct = ti.getCachedTargets(TargetsInterface.TRANSFORM_TARGETS, i, -1);
			if (ct != null) {
		            targets.addNode((NnuId)mirrorGroup.get(i), 
					    Targets.GRP_TARGETS);
                            newCtArr[i] = targets.snapShotAdd(ct);
			} else {
			    newCtArr[i] = null;
			}
                    }


            	    // update target threads and propagate change to above
            	    // nodes in scene graph
            	    ti.updateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
                                                newCtArr);
                    ti.resetCachedTargets(TargetsInterface.TRANSFORM_TARGETS,
                                                newCtArr, -1);
                }

		message.type = J3dMessage.INSERT_NODES;
		message.args[0] = mirrorGroup.toArray();
                message.args[1] = ti;
                message.args[2] = newCtArr;

	    } else {
                TargetsInterface ti = 
		    getClosestTargetsInterface(TargetsInterface.TRANSFORM_TARGETS);
                if (ti != null) {

                    // update targets
                    Targets targets = new Targets();
	            CachedTargets ct;
		    numPath = mirrorGroup.size();
		    newCtArr = 	new CachedTargets[numPath];
		    for (int i=0; i<numPath; i++) {
                        ct = ti.getCachedTargets(TargetsInterface.TRANSFORM_TARGETS, i, -1);
			if (ct != null) {			    
                            targets.addNode((NnuId)mirrorGroup.get(i),
					    Targets.GRP_TARGETS);
			    //Note snapShotRemove calls targets.clearNode()
                            newCtArr[i] = targets.snapShotRemove(ct);
                        } else {
                            newCtArr[i] = null;
                        }
                    }
            	    // update target threads and propagate change to above
            	    // nodes in scene graph
            	    ti.updateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
					   newCtArr);
                    ti.resetCachedTargets(TargetsInterface.TRANSFORM_TARGETS,
					  newCtArr, -1);
                }

		message.type = J3dMessage.REMOVE_NODES;
		message.args[0] = mirrorGroup.toArray();
                message.args[1] = ti;
                message.args[2] = newCtArr;
		mirrorGroup = null;  // for gc
	    }
	    VirtualUniverse.mc.processMessage(message);
	}
    }

    boolean getAlternateCollisionTarget() {
	return collisionTarget;
    }



    /**
     * This checks is setLive needs to be called.  If it does, it gets the
     * needed info and calls it.
     */
    void checkSetLive(NodeRetained child, int childIndex, J3dMessage messages[], 
				int messageIndex, NodeRetained linkNode) {
	checkSetLive(child, childIndex, localToVworldKeys, inSharedGroup,
		     messages, messageIndex, linkNode);
    }

    
    /**
     * This checks is setLive needs to be called.  If it does, it gets the
     * needed info and calls it.
     */
    void checkSetLive(NodeRetained child, int childIndex, HashKey keys[], 
		      boolean isShared, J3dMessage messages[], 
		      int messageIndex, NodeRetained linkNode) {

        SceneGraphObject me = this.source;
	SetLiveState s;
	J3dMessage createMessage;
	boolean sendMessages = false;
	boolean sendOGMessage = true;
	boolean sendVSGMessage = true;

        if (me.isLive()) {

	    s = universe.setLiveState;
	    s.reset(locale);
	    s.refCount = refCount;
	    s.inSharedGroup = isShared;
	    s.inBackgroundGroup = inBackgroundGroup;
	    s.inViewSpecificGroup = inViewSpecificGroup;
            s.geometryBackground = geometryBackground;
	    s.keys = keys;	    
	    s.viewLists = viewLists;
	    s.parentBranchGroupPaths = branchGroupPaths;
	    // Note that there is no need to clone individual
	    // branchGroupArray since they will get replace (not append)
	    // by creating a new reference in child's group.
            s.branchGroupPaths = (ArrayList) branchGroupPaths.clone();
            s.orderedPaths = orderedPaths;

	    // Make the scoped fogs and lights of the child to include, the
	    // the scoped fog of this group
	    s.lights = lights;
	    s.altAppearances = altAppearances;
	    s.fogs = fogs;
	    s.modelClips = modelClips;

	    boolean pick[];
	    boolean collide[];

	    if (!inSharedGroup) {
		pick = new boolean[1];
		collide = new boolean[1];
	    } else {
		pick = new boolean[localToVworldKeys.length];
		collide = new boolean[localToVworldKeys.length];
	    }
	    findPickableFlags(pick);
	    super.updatePickable(null, pick);
	    s.pickable = pick;

	    findCollidableFlags(collide);
	    super.updateCollidable(null, collide); 
	    s.collidable = collide;

            TargetsInterface transformInterface, switchInterface;
            transformInterface = initTransformStates(s, true);
            switchInterface = initSwitchStates(s, this, child, linkNode, true);


	    if (s.inViewSpecificGroup && 
		(s.changedViewGroup == null)) {
		s.changedViewGroup = new ArrayList();
		s.changedViewList = new ArrayList();
		s.keyList = new int[10];
		s.viewScopedNodeList = new ArrayList();
		s.scopedNodesViewList = new ArrayList();
	    }

            childCheckSetLive(child, childIndex, s, linkNode);

	    CachedTargets[] newCtArr = null;
            newCtArr = updateTransformStates(s, transformInterface, true);
            updateSwitchStates(s, switchInterface, true);

	    // We're sending multiple messages in the call, inorder to
	    // have all these messages to be process as an atomic operation.
	    // We need to create an array of messages to MasterControl, this
	    // will ensure that all these messages will get the same time stamp.

	    // If it is called from "moveTo",  messages is not null. 
	    if (messages == null) {
		int numMessages = 2;
		if(s.ogList.size() > 0) {
		    numMessages++;
		}
		else {
		    sendOGMessage = false;
		}
		if(s.changedViewGroup != null) {
		    numMessages++;
		}
		else {
		    sendVSGMessage = false;
		}

		messages = new J3dMessage[numMessages];
		messageIndex = 0;
		for(int mIndex=0; mIndex < numMessages; mIndex++) {
		    messages[mIndex] = VirtualUniverse.mc.getMessage();
		}
		sendMessages = true;
	    }
	    
	    if(sendOGMessage) {
		createMessage = messages[messageIndex++];	    
		createMessage.threads = J3dThread.UPDATE_RENDER | 
		    J3dThread.UPDATE_RENDERING_ENVIRONMENT;
		createMessage.type = J3dMessage.ORDERED_GROUP_INSERTED;
		createMessage.universe = universe;
		createMessage.args[0] = s.ogList.toArray();
		createMessage.args[1] = s.ogChildIdList.toArray();
		createMessage.args[2] = s.ogOrderedIdList.toArray();
		createMessage.args[3] = s.ogCIOList.toArray();
		createMessage.args[4] = s.ogCIOTableList.toArray();
	    }


	    if(sendVSGMessage) {
		createMessage = messages[messageIndex++];
		createMessage.threads = J3dThread.UPDATE_RENDERING_ENVIRONMENT;
		createMessage.type = J3dMessage.VIEWSPECIFICGROUP_INIT;
		createMessage.universe = universe;
		createMessage.args[0] = s.changedViewGroup;
		createMessage.args[1] = s.changedViewList;
		createMessage.args[2] = s.keyList;
	    }

	    createMessage = messages[messageIndex++];
	    createMessage.threads = s.notifyThreads;
	    createMessage.type = J3dMessage.INSERT_NODES;
	    createMessage.universe = universe;
	    createMessage.args[0] = s.nodeList.toArray();
	    if (newCtArr != null) {
                createMessage.args[1] = transformInterface;
                createMessage.args[2] = newCtArr;
	    } else {
                createMessage.args[1] = null;
                createMessage.args[2] = null;
	    } 

	    if (s.viewScopedNodeList != null) {
		createMessage.args[3] = s.viewScopedNodeList;
		createMessage.args[4] = s.scopedNodesViewList;
	    }
	   
            // execute user behavior's initialize methods
	    int sz = s.behaviorNodes.size();

            for (int i=0; i < sz; i++) {
                BehaviorRetained b;
                b = (BehaviorRetained)s.behaviorNodes.get(i);
                b.executeInitialize();
            }

            s.behaviorNodes.clear();

	    createMessage = messages[messageIndex++];
            
	    createMessage.threads = J3dThread.UPDATE_BEHAVIOR;
	    createMessage.type = J3dMessage.BEHAVIOR_ACTIVATE;
	    createMessage.universe = universe;

	    if (sendMessages == true) {
		VirtualUniverse.mc.processMessage(messages);
	    }

            if (nodeType == NodeRetained.SWITCH) {
	        // force reEvaluation of switch children
		SwitchRetained sw = (SwitchRetained)this;
	        sw.setWhichChild(sw.whichChild, true);
            }

	    //Reset SetLiveState to free up memory.
	    s.reset(null);
        }
    }


  
    void checkClearLive(NodeRetained child,
                        J3dMessage messages[], int messageIndex, 
			int childIndex, NodeRetained linkNode) {
	checkClearLive(child, localToVworldKeys, inSharedGroup,
		       messages, messageIndex, childIndex, linkNode);
    }

    

    /**
     * This checks if clearLive needs to be called.  If it does, it gets the
     * needed info and calls it.
     */
    void checkClearLive(NodeRetained child, HashKey keys[],
			boolean isShared,
                        J3dMessage messages[], int messageIndex,
			int childIndex, NodeRetained linkNode) {

        SceneGraphObject me = this.source;
	J3dMessage destroyMessage;
	boolean sendMessages = false;
	boolean sendOGMessage = true;
	boolean sendVSGMessage = true;

  	int i, j;
	TransformGroupRetained tg;

        if (me.isLive()) {
            SetLiveState s = universe.setLiveState;

            s.reset(locale);
	    s.refCount = refCount;
            s.inSharedGroup = isShared;
            s.inBackgroundGroup = inBackgroundGroup;
            s.inViewSpecificGroup = inViewSpecificGroup;
            s.keys = keys;
            s.fogs = fogs;
            s.lights = lights;
	    s.altAppearances = altAppearances;
	    s.modelClips = modelClips;


	    if (this instanceof OrderedGroupRetained && linkNode == null) {
                // set this regardless of refCount
		s.ogList.add(this);
		s.ogChildIdList.add(new Integer(childIndex));
		s.ogCIOList.add(this);
		int[] newArr = null;
		OrderedGroupRetained og = (OrderedGroupRetained)this;
		if(og.userChildIndexOrder != null) {
		    newArr = new int[og.userChildIndexOrder.length];
		    System.arraycopy(og.userChildIndexOrder, 0, newArr,
				     0, og.userChildIndexOrder.length);
		}
		s.ogCIOTableList.add(newArr);
		
	    }

	    if (!(this instanceof ViewSpecificGroupRetained)) {
		s.viewLists = viewLists;
	    }

            TargetsInterface transformInterface, switchInterface;
            transformInterface = initTransformStates(s, false);
            switchInterface = initSwitchStates(s, this, child, linkNode, false);

            child.clearLive(s);

            CachedTargets[] newCtArr = null;
            newCtArr = updateTransformStates(s, transformInterface, false);
            updateSwitchStates(s, switchInterface, false);

	    // We're sending multiple messages in the call, inorder to
	    // have all these messages to be process as an atomic operation.
	    // We need to create an array of messages to MasterControl, this
	    // will ensure that all these messages will get the same time stamp.

	    // If it is called from "moveTo",  messages is not null.
	    if (messages == null) {
		int numMessages = 1;
		if(s.ogList.size() > 0) {
		    numMessages++;
		}
		else {
		    sendOGMessage = false;
		}

		if(s.changedViewGroup != null) {
		    numMessages++;
		}
		else {
		    sendVSGMessage = false;
		}

		messages = new J3dMessage[numMessages];
		messageIndex = 0;
		for(int mIndex=0; mIndex < numMessages; mIndex++) {
		    messages[mIndex] = VirtualUniverse.mc.getMessage();
		}
		sendMessages = true;
	    }

	    if(sendOGMessage) {
		destroyMessage = messages[messageIndex++];
		destroyMessage.threads = J3dThread.UPDATE_RENDER | 
		    J3dThread.UPDATE_RENDERING_ENVIRONMENT;
		destroyMessage.type = J3dMessage.ORDERED_GROUP_REMOVED;
		destroyMessage.universe = universe;
		destroyMessage.args[0] = s.ogList.toArray();
		destroyMessage.args[1] = s.ogChildIdList.toArray();
		destroyMessage.args[3] = s.ogCIOList.toArray();
		destroyMessage.args[4] = s.ogCIOTableList.toArray();
	    }

	    if(sendVSGMessage) {
		destroyMessage = messages[messageIndex++];            
		destroyMessage.threads =  J3dThread.UPDATE_RENDERING_ENVIRONMENT;
		destroyMessage.type = J3dMessage.VIEWSPECIFICGROUP_CLEAR;
		destroyMessage.universe = universe;
		destroyMessage.args[0] = s.changedViewGroup;
		destroyMessage.args[1] = s.keyList;
	    }

	    destroyMessage = messages[messageIndex++];            
            destroyMessage.threads = s.notifyThreads;
            destroyMessage.type = J3dMessage.REMOVE_NODES;
            destroyMessage.universe = universe;
            destroyMessage.args[0] = s.nodeList.toArray();
	    
	    if (newCtArr != null) {
                destroyMessage.args[1] = transformInterface;
                destroyMessage.args[2] = newCtArr;
	    } else {
                destroyMessage.args[1] = null;
                destroyMessage.args[2] = null;
	    } 
	    if (s.viewScopedNodeList != null) {
		destroyMessage.args[3] = s.viewScopedNodeList;
		destroyMessage.args[4] = s.scopedNodesViewList;
	    }
            if (sendMessages == true) {
		VirtualUniverse.mc.processMessage(messages);
	    }
	    
	    s.reset(null); // for GC
        }
    }

    TargetsInterface initTransformStates(SetLiveState s, boolean isSetLive) {

        int numPaths = (inSharedGroup)? s.keys.length : 1;
        TargetsInterface ti = getClosestTargetsInterface(
                                        TargetsInterface.TRANSFORM_TARGETS);


	if (isSetLive) {
            s.currentTransforms = localToVworld;
            s.currentTransformsIndex = localToVworldIndex;
            s.localToVworldKeys = localToVworldKeys;
            s.localToVworld = s.currentTransforms;
            s.localToVworldIndex = s.currentTransformsIndex;

            s.parentTransformLink = parentTransformLink;
            if (parentTransformLink != null) {
                if (parentTransformLink instanceof TransformGroupRetained) {
		    TransformGroupRetained tg;
                    tg = (TransformGroupRetained) parentTransformLink;
                    s.childTransformLinks = tg.childTransformLinks;
                } else {
		    SharedGroupRetained sg;
                    sg = (SharedGroupRetained) parentTransformLink;
                    s.childTransformLinks = sg.childTransformLinks;
                }
            }
	}

	int transformLevels[] = new int[numPaths];
	findTransformLevels(transformLevels);
	s.transformLevels = transformLevels;
	
	if (ti != null) {
	    Targets[] newTargets = new Targets[numPaths];
	    for(int i=0; i<numPaths; i++) {
		if (s.transformLevels[i] >= 0) {		    
		    newTargets[i] = new Targets();
		} else {
		    newTargets[i] = null;
		}
	    }
	    s.transformTargets = newTargets;
	    
	    // TODO - optimization for targetThreads computation, require
	    // cleanup in GroupRetained.doSetLive()
	    //s.transformTargetThreads = 0;
	}
        
	return ti;
    }

    CachedTargets[] updateTransformStates(SetLiveState s,
				TargetsInterface ti, boolean isSetLive) {
        CachedTargets[] newCtArr = null;

        if (ti != null) {
	    if (isSetLive) {
                CachedTargets ct;
                int newTargetThreads = 0;
                int hkIndex;

                newCtArr = new CachedTargets[localToVworld.length];

                // update targets
                if (! inSharedGroup) {
                    if (s.transformTargets[0] != null) {
                        ct = ti.getCachedTargets(
                                    TargetsInterface.TRANSFORM_TARGETS, 0, -1);
                        if (ct != null) {
                            newCtArr[0] = s.transformTargets[0].snapShotAdd(ct);
                        }
                    } else {
                        newCtArr[0] = null;
                    }
                } else {
                    for (int i=0; i<s.keys.length; i++) {

                        if (s.transformTargets[i] != null) {
                            ct = ti.getCachedTargets(
                                TargetsInterface.TRANSFORM_TARGETS, i, -1);
                            if (ct != null) {
                                newCtArr[i] =
                                        s.transformTargets[i].snapShotAdd(ct);
                            }
                        } else {
                            newCtArr[i] = null;
                        }
                    }
                }
            } else {

                CachedTargets ct;
                int hkIndex;

                newCtArr = new CachedTargets[localToVworld.length];

                if (! inSharedGroup) {
                    if (s.transformTargets[0] != null) {
                        ct = ti.getCachedTargets(
                                    TargetsInterface.TRANSFORM_TARGETS, 0, -1);
                        if (ct != null) {
                            newCtArr[0] =
                                s.transformTargets[0].snapShotRemove(ct);
                        }
                    } else {
                        newCtArr[0] = null;
                    }
                } else {
                    for (int i=0; i<s.keys.length; i++) {
                        if (s.transformTargets[i] != null) {
                            ct = ti.getCachedTargets(
                                TargetsInterface.TRANSFORM_TARGETS, i, -1);
                            if (ct != null) {
                                newCtArr[i] =
                                    s.transformTargets[i].snapShotRemove(ct);
                            }
                        } else {
                            newCtArr[i] = null;
                        }
                    }
                }
	    }
            // update target threads and propagate change to above
            // nodes in scene graph

	    ti.updateTargetThreads(TargetsInterface.TRANSFORM_TARGETS,
	    			   newCtArr);
	    ti.resetCachedTargets(TargetsInterface.TRANSFORM_TARGETS,
	    			  newCtArr, -1);
	    
        }
        return newCtArr;
    }

    TargetsInterface initSwitchStates(SetLiveState s, 
				NodeRetained parentNode, NodeRetained childNode,
				NodeRetained linkNode, boolean isSetLive) {
        NodeRetained child;
	NodeRetained parent;
	int i,j;

	findSwitchInfo(s, parentNode, childNode, linkNode);
        TargetsInterface ti = getClosestTargetsInterface(
                                        TargetsInterface.SWITCH_TARGETS);
        if (ti != null) {
            Targets[] newTargets = null;
            int numPaths = (inSharedGroup)? s.keys.length : 1;
            newTargets = new Targets[numPaths];
            for(i=0; i<numPaths; i++) {
                if (s.switchLevels[i] >= 0) {
                    newTargets[i] = new Targets();
                } else {
                    newTargets[i] = null;
                }
            }
            s.switchTargets = newTargets;
        }

	if (isSetLive) {
	    // set switch states
            if (nodeType == NodeRetained.SWITCH) {
                i = parentSwitchLinkChildIndex;
                s.childSwitchLinks = (ArrayList)childrenSwitchLinks.get(i);
                s.parentSwitchLink = this;

            } else {
                if (nodeType == NodeRetained.SHAREDGROUP) {
                    i = parentSwitchLinkChildIndex;
                    s.childSwitchLinks = (ArrayList)childrenSwitchLinks.get(i);
                    s.parentSwitchLink = this;

                } else {
		    s.parentSwitchLink = parentSwitchLink;
                    if (parentSwitchLink != null) {
                        i = parentSwitchLinkChildIndex;
                        s.childSwitchLinks = (ArrayList)
                                parentSwitchLink.childrenSwitchLinks.get(i);
                    }
                }
            }
            if (ti != null) {
                s.switchStates = ti.getTargetsData(
					TargetsInterface.SWITCH_TARGETS,
					parentSwitchLinkChildIndex);
            } else {
                s.switchStates = new ArrayList(1);
                s.switchStates.add(new SwitchState(false));
            }
	} 
        return ti;
    }

    void updateSwitchStates(SetLiveState s, TargetsInterface ti, 
				boolean isSetLive) {

        // update switch leaves's compositeSwitchMask for ancestors
        // and update switch leaves' switchOn flag if at top level switch

        if (ti != null) {
	    if (isSetLive) {
                CachedTargets[] newCtArr = null;
                CachedTargets ct;

                newCtArr = new CachedTargets[localToVworld.length];

                // update targets
                if (! inSharedGroup) {

                    if (s.switchTargets[0] != null) {
                        ct = ti.getCachedTargets(
                                TargetsInterface.SWITCH_TARGETS, 0,
                                        parentSwitchLinkChildIndex);
                        if (ct != null) {
                            newCtArr[0] = s.switchTargets[0].snapShotAdd(ct);
                        } else {
                            newCtArr[0] = s.switchTargets[0].snapShotInit();
                        }
		    } else {
                        newCtArr[0] = null;
		    }
                } else {
                    for (int i=0; i<s.keys.length; i++) {
                        if (s.switchTargets[i] != null) {
                            ct = ti.getCachedTargets(
                                TargetsInterface.SWITCH_TARGETS, i, 
                                        parentSwitchLinkChildIndex);
                            if (ct != null) {
                                newCtArr[i] = 
					s.switchTargets[i].snapShotAdd(ct);
                            } else {
                                newCtArr[i] =
                                        s.switchTargets[i].snapShotInit();
                            }
			} else {
                            newCtArr[i] = null;
                        }
                    }
                }
                ti.resetCachedTargets(TargetsInterface.SWITCH_TARGETS,
                                        newCtArr, parentSwitchLinkChildIndex);
                if (ti instanceof SwitchRetained) {
                    ((SwitchRetained)ti).traverseSwitchParent();
                } else if (ti instanceof SharedGroupRetained) {
                    ((SharedGroupRetained)ti).traverseSwitchParent();
                }
	    } else {
                CachedTargets ct;

                CachedTargets[] newCtArr = 
			new CachedTargets[localToVworld.length];

                if (! inSharedGroup) {
                    if (s.switchTargets[0] != null) {
                        ct = ti.getCachedTargets(
                                    TargetsInterface.SWITCH_TARGETS, 0,
                                    parentSwitchLinkChildIndex);
                        if (ct != null) {
                            newCtArr[0] =
                                s.switchTargets[0].snapShotRemove(ct);
                        }
		    } else {
                        newCtArr[0] = null;
                    }
                } else {
                    for (int i=0; i<s.keys.length; i++) {
                        if (s.switchTargets[i] != null) {
                            ct = ti.getCachedTargets(
                                    TargetsInterface.SWITCH_TARGETS, i,
                                    parentSwitchLinkChildIndex);

                            if (ct != null) {
                                newCtArr[i] =
                                    s.switchTargets[i].snapShotRemove(ct);
                            }
			} else {
                            newCtArr[i] = null;
                        }
                    }
                }
                ti.resetCachedTargets(TargetsInterface.SWITCH_TARGETS,
                                        newCtArr, parentSwitchLinkChildIndex);
            }
        }
    }

    void appendChildrenData() {
    }
    void insertChildrenData(int index) {
    }
    void removeChildrenData(int index) {
    }

    TargetsInterface getClosestTargetsInterface(int type) {
        return (type == TargetsInterface.TRANSFORM_TARGETS)?
                (TargetsInterface)parentTransformLink:
                (TargetsInterface)parentSwitchLink;
    }


    synchronized void updateLocalToVworld() {
	NodeRetained child;
	
	// For each children call .....
	for (int i=children.size()-1; i>=0; i--) {
	    child = (NodeRetained)children.get(i);
	    if(child != null)
		child.updateLocalToVworld();
	}
    }

    void setNodeData(SetLiveState s) {
	super.setNodeData(s);
        orderedPaths = s.orderedPaths;
    }

    void removeNodeData(SetLiveState s) {

        if((!inSharedGroup) || (s.keys.length == localToVworld.length)) {
            orderedPaths = null;
        }
        else {
            // Set it back to its parent localToVworld data. This is b/c the
            // parent has changed it localToVworld data arrays.
            orderedPaths = s.orderedPaths;
        }
        super.removeNodeData(s);
    }



    void setLive(SetLiveState s) {
	doSetLive(s);
	super.markAsLive();
    }

    // Note that SwitchRetained, OrderedGroupRetained and SharedGroupRetained
    // override this method
    void childDoSetLive(NodeRetained child, int childIndex, SetLiveState s) {
	if(child!=null)
            child.setLive(s);
    }

    // Note that BranchRetained, OrderedGroupRetained and SharedGroupRetained
    // TransformGroupRetained override this method
    void childCheckSetLive(NodeRetained child, int childIndex,
                                SetLiveState s, NodeRetained linkNode) {
        child.setLive(s);
    }

    /**
     * This version of setLive calls setLive on all of its chidren.
     */
    void doSetLive(SetLiveState s) {
      	int i, nchildren;

	BoundingSphere boundingSphere = new BoundingSphere();
	NodeRetained child;
        super.doSetLive(s);
	locale = s.locale;

	inViewSpecificGroup = s.inViewSpecificGroup;
	nchildren = children.size();
	ArrayList savedScopedLights = s.lights;
	ArrayList savedScopedFogs = s.fogs;
	ArrayList savedScopedAltApps = s.altAppearances;
	ArrayList savedScopedMclips = s.modelClips;
	
	boolean oldpickableArray[] = (boolean []) s.pickable.clone();
	boolean oldcollidableArray[] = (boolean []) s.collidable.clone();
	boolean workingpickableArray[] = new boolean[oldpickableArray.length];
	boolean workingcollidableArray[] = new boolean[oldcollidableArray.length];
	ArrayList oldBranchGroupPaths = s.branchGroupPaths;
	setScopingInfo(s);


	if (!(this instanceof ViewSpecificGroupRetained)) {
	    viewLists = s.viewLists;
	}

	for (i=0; i<nchildren; i++) {
	    child = (NodeRetained)children.get(i);

	    // Restore old values before child.setLive(s)
	    System.arraycopy(oldpickableArray, 0, workingpickableArray, 0,
			     oldpickableArray.length);
	    System.arraycopy(oldcollidableArray, 0, workingcollidableArray, 0,
			     oldcollidableArray.length);
	    s.pickable = workingpickableArray;
	    s.collidable = workingcollidableArray;
	    // s.branchGroupPaths will be modified by child setLive()
	    // so we have to restore it every time.
	    s.parentBranchGroupPaths = branchGroupPaths;
            s.branchGroupPaths = (ArrayList) oldBranchGroupPaths.clone();
	    s.inViewSpecificGroup = inViewSpecificGroup;
            childDoSetLive(child, i, s);
	}



	if (collisionTarget) {
	    processCollisionTarget(s);
	}

	s.lights = savedScopedLights;
	s.fogs = savedScopedFogs;
	s.altAppearances = savedScopedAltApps;
	s.modelClips = savedScopedMclips;

    }

    void setScopingInfo(SetLiveState s) {

	int i, k, hkIndex;
	// If this is a scoped group , then copy the parent's
	// scoping info
	if (allocatedLights) {
	    if (s.lights != null) {
		// Add the parent's scoping info to this group
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)lights.get(hkIndex);
			ArrayList src = (ArrayList)s.lights.get(i);
			if (src != null) {
			    int size = src.size();
			    for (k = 0; k < size; k++) {
				l.add(src.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)lights.get(0);
		    ArrayList src = (ArrayList)s.lights.get(0);
		    int size = src.size();
		    for (i = 0; i < size; i++) {
			l.add(src.get(i));
		    }
		}
	    }
	    s.lights = lights;
	}
	else {
	    lights = s.lights;
	}

	if (allocatedFogs) {
	    if (s.fogs != null) {
		// Add the parent's scoping info to this group
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)fogs.get(hkIndex);
			ArrayList src = (ArrayList)s.fogs.get(i);
			if (src != null) {
			    int size = src.size();
			    for (k = 0; k < size; k++) {
				l.add(src.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)fogs.get(0);
		    ArrayList src = (ArrayList)s.fogs.get(0);
		    int size = src.size();
		    for (i = 0; i < size; i++) {
			l.add(src.get(i));
		    }
		}
	    }
	    s.fogs = fogs;
	}
	else {
	    fogs = s.fogs;
	}

	if (allocatedMclips) {
	    if (s.modelClips != null) {
		// Add the parent's scoping info to this group
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)modelClips.get(hkIndex);
			ArrayList src = (ArrayList)s.modelClips.get(i);
			if (src != null) {
			    int size = src.size();
			    for (k = 0; k < size; k++) {
				l.add(src.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)modelClips.get(0);
		    ArrayList src = (ArrayList)s.modelClips.get(0);
		    int size = src.size();
		    for (i = 0; i < size; i++) {
			l.add(src.get(i));
		    }
		}
	    }
	    s.modelClips = modelClips;
	}
	else {
	    modelClips = s.modelClips;
	}

	if (allocatedAltApps) {
	    if (s.altAppearances != null) {
		// Add the parent's scoping info to this group
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)altAppearances.get(hkIndex);
			ArrayList src = (ArrayList)s.altAppearances.get(i);
			if (src != null) {
			    int size = src.size();
			    for (k = 0; k < size; k++) {
				l.add(src.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)altAppearances.get(0);
		    ArrayList src = (ArrayList)s.altAppearances.get(0);
		    int size = src.size();
		    for (i = 0; i < size; i++) {
			l.add(src.get(i));
		    }
		}
	    }
	    s.altAppearances = altAppearances;
	}
	else {
	    altAppearances = s.altAppearances;
	}
    }

    void processCollisionTarget(SetLiveState s) {

	    GroupRetained g;
	    if (mirrorGroup == null) {
		mirrorGroup = new ArrayList();
	    }
	    Bounds bound = (collisionBound != null ?
			    collisionBound : getEffectiveBounds());
	    if (inSharedGroup) {
		for (int i=0; i < s.keys.length; i++) {
		    int j;
		    g = new GroupRetained();
		    g.key = s.keys[i];
		    g.localToVworld = new Transform3D[1][];
		    g.localToVworldIndex = new int[1][];

		    j = s.keys[i].equals(localToVworldKeys, 0,
					 localToVworldKeys.length);
		    if(j < 0) {
			System.out.println("GroupRetained : Can't find hashKey"); 
		    }
		    
		    g.localToVworld[0] = localToVworld[j];
		    g.localToVworldIndex[0] = localToVworldIndex[j];
		    g.collisionVwcBounds = new BoundingBox();
		    g.collisionVwcBounds.transform(bound, g.getCurrentLocalToVworld(0));
		    g.sourceNode = this;
		    g.locale = locale; // need by getVisibleGeometryAtom()
		    mirrorGroup.add(g);
		    /*
		      System.out.println("processCollisionTarget mirrorGroup.add() : " +
		      g.getId() + " mirrorGroup.size() "
		      + mirrorGroup.size());
		    */
		    if (s.transformTargets != null &&
				s.transformTargets[i] != null) {
		        s.transformTargets[i].addNode(g, Targets.GRP_TARGETS);
		    }
		    s.nodeList.add(g);
		}
	    } else {
		g = new GroupRetained();
		g.localToVworld = new Transform3D[1][];
		g.localToVworldIndex = new int[1][];
		g.localToVworld[0] = localToVworld[0];
		g.localToVworldIndex[0] = localToVworldIndex[0];
		g.collisionVwcBounds = new BoundingBox();
		g.collisionVwcBounds.transform(bound, g.getCurrentLocalToVworld(0));
		g.sourceNode = this;
		g.locale = locale; // need by getVisibleGeometryAtom()
		mirrorGroup.add(g);
                if (s.transformTargets != null && 
			s.transformTargets[0] != null) {
		    s.transformTargets[0].addNode(g, Targets.GRP_TARGETS);
		}
		s.nodeList.add(g);
	    }
    }

    void computeCombineBounds(Bounds bounds) {
    
	if (boundsAutoCompute) {    
	    for (int i=children.size()-1; i>=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i); 
		if(child != null)
		    child.computeCombineBounds(bounds);
	    }
	} else {
	    // Should this be lock too ? ( MT safe  ? )
	    synchronized(localBounds) {
		bounds.combine(localBounds);
	    }
	}
    }
  
  
    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    Bounds getBounds() {
    
	if ( boundsAutoCompute) {
	    BoundingSphere boundingSphere = new BoundingSphere();
	    boundingSphere.setRadius(-1.0);
	    
	    for (int i=children.size()-1; i>=0; i--) {
		NodeRetained child = (NodeRetained)children.get(i); 
		if(child != null)
		    child.computeCombineBounds((Bounds) boundingSphere);
	    }
	    
	    return (Bounds) boundingSphere;
	} 
	return super.getBounds();
    } 

    /**
     * Gets the bounding object of a node.
     * @return the node's bounding object
     */
    Bounds getEffectiveBounds() {
	if ( boundsAutoCompute) {
	    return getBounds();
	} 
	return super.getEffectiveBounds();
    }
    
    // returns true if children cannot be read/written
    boolean isStaticChildren() {
	if (source.getCapability(Group.ALLOW_CHILDREN_READ) ||
	    source.getCapability(Group.ALLOW_CHILDREN_WRITE)) {
	    return false;
	}
	return true;
	
    }


    boolean isStatic() {
	return (super.isStatic() && isStaticChildren());
    }

    /**
     * This compiles() a group
     */
    void setCompiled() {
	super.setCompiled();
	for (int i=children.size()-1; i>=0; i--) {
	    SceneGraphObjectRetained node = 
		(SceneGraphObjectRetained) children.get(i);
	    if (node != null)
		node.setCompiled();
	}
    }

    void traverse(boolean sameLevel, int level) {
	SceneGraphObjectRetained node;

	if (!sameLevel) {
	    super.traverse(true, level);

	    if (source.getCapability(Group.ALLOW_CHILDREN_READ)) {
		System.out.print(" (r)");
	    } else if (isStatic()) {
		System.out.print(" (s)");
	    } else if (source.getCapability(Group.ALLOW_CHILDREN_WRITE)) {
		System.out.print(" (w)");
	    } 
	}

	level++;
	for (int i = 0; i < children.size(); i++) {
	    node = (SceneGraphObjectRetained) children.get(i);
	    if (node != null) {
		node.traverse(false, level);
	    }
	}
    }

    void compile(CompileState compState) {

	SceneGraphObjectRetained node;

	super.compile(compState);

	mergeFlag = SceneGraphObjectRetained.MERGE;

	if (!isStatic()) {
	    compState.keepTG = true;
	    mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
        }

	if (isRoot || this.usedInScoping() || 
	    (parent instanceof SwitchRetained)) {
	    mergeFlag = SceneGraphObjectRetained.DONT_MERGE;
	}

        compiledChildrenList = new ArrayList(5);

	for (int i = 0; i < children.size(); i++) {
	    node = (SceneGraphObjectRetained) children.get(i);
	    if (node != null) {
		node.compile(compState);
	    }
	}

        if (J3dDebug.devPhase && J3dDebug.debug) {
            compState.numGroups++;
	}
    }

    void merge(CompileState compState) {

	GroupRetained saveParentGroup = null;
	SceneGraphObjectRetained node;

	if (mergeFlag != SceneGraphObjectRetained.MERGE_DONE) { 
            if (mergeFlag == SceneGraphObjectRetained.DONT_MERGE) {

	 	// don't merge/eliminate this node
		super.merge(compState);
		
		saveParentGroup = compState.parentGroup;
		compState.parentGroup = this;
	    }

	    for (int i = 0; i < children.size(); i++) {
	        node = (SceneGraphObjectRetained) children.get(i);
	        if (node != null) {
		    node.merge(compState);
		}
	    }

	    if (compState.parentGroup == this) {
	        this.children = compiledChildrenList;
		compState.doShapeMerge();
	        compiledChildrenList = null;
		compState.parentGroup = saveParentGroup;
	    } else {
		// this group node can be eliminated
		this.children.clear();

                if (J3dDebug.devPhase && J3dDebug.debug) {
                    compState.numMergedGroups++;
	        }
	    }

	    mergeFlag = SceneGraphObjectRetained.MERGE_DONE;

	} else {
            if (compState.parentGroup != null) {
                compState.parentGroup.compiledChildrenList.add(this);
                parent = compState.parentGroup;
            }     
	}
    }

    /**
     * This version of clearLive calls clearLive on all of its chidren.
     */
    void clearLive(SetLiveState s) {
	int i, k, hkIndex, nchildren;
	NodeRetained child;
	int parentScopedLtSize = 0;
	int parentScopedFogSize = 0;
	int parentScopedMcSize = 0;
	int parentScopedAltAppSize = 0;
	int groupScopedLtSize = 0;
	int groupScopedFogSize = 0;
	int groupScopedMcSize = 0;
	int groupScopedAltAppSize = 0;
	int size;
	
	isInClearLive = true;

	// Save this for later use in this method. Temporary. to be removed when OG cleanup. 
	HashKey[] savedLocalToVworldKeys = localToVworldKeys;

	super.clearLive(s);

	    
	nchildren = this.children.size();

	if (!(this instanceof ViewSpecificGroupRetained)) {
	    viewLists = s.viewLists;
	}

	ArrayList savedParentLights = s.lights;
	if (allocatedLights) {
	    s.lights = lights;
	}

	ArrayList savedParentFogs = s.fogs;
	if (allocatedFogs) {
	    s.fogs = fogs;
	}

	ArrayList savedParentMclips = s.modelClips;
	if (allocatedMclips) {
	    s.modelClips = modelClips;
	}


	ArrayList savedParentAltApps = s.altAppearances;
	if (allocatedAltApps) {
	    s.altAppearances = altAppearances;
	}
	
	
	for (i=nchildren-1; i >=0 ; i--) {
	    child = (NodeRetained)children.get(i);
	    if (this instanceof OrderedGroupRetained) { 
	        OrderedGroupRetained og = (OrderedGroupRetained)this;

		// adjust refCount, which has been decremented 
	        //in super.clearLive
		if ((refCount+1) == s.refCount) {
		    //only need to do it once if in shared group. Add
		    //all the children to the list of OG_REMOVED message
		    s.ogList.add(this);
		    s.ogChildIdList.add(new Integer(i));
		}
                s.orderedPaths = (ArrayList)og.childrenOrderedPaths.get(i);
	    }

	    if (child != null) {		    
	        child.clearLive(s);
	    }
	}
	// Has its own copy
	// TODO: Handle the case of
	// was non-zero, gone to zero?
	if (savedParentLights != null) {
	    if (allocatedLights) {
		if (inSharedGroup) {
		    
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)savedParentLights.get(hkIndex);
			ArrayList gl = (ArrayList)lights.get(hkIndex);
			if (l != null) {
			    size = l.size();
			    for (k = 0; k < size; k++) {
				gl.remove(l.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)savedParentLights.get(0);
		    ArrayList gl = (ArrayList)lights.get(0);
		    size = l.size();
		    for (int m = 0; m < size; m++) {
			gl.remove(l.get(m));
		    }
		}
	    }
	}

	if (savedParentFogs != null) {
	    if (allocatedFogs) {
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)savedParentFogs.get(hkIndex);
			ArrayList gl = (ArrayList)fogs.get(hkIndex);
			if (l != null) {
			    size = l.size();
			    for (k = 0; k < size; k++) {
				gl.remove(l.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)savedParentFogs.get(0);
		    size = l.size();
		    for (int m = 0; m < size; m++) {
			fogs.remove(l.get(m));
		    }
		}
	    }
	}

	if (savedParentMclips != null) {
	    if (allocatedMclips) {
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)savedParentMclips.get(hkIndex);
			ArrayList gl = (ArrayList)modelClips.get(hkIndex);
			if (l != null) {
			    size = l.size();
			    for (k = 0; k < size; k++) {
				gl.remove(l.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)savedParentMclips.get(0);
		    size = l.size();
		    for (int m = 0; m < size; m++) {
			modelClips.remove(l.get(m));
		    }
		}
	    }
	}

	if (savedParentAltApps != null) {
	    if (allocatedAltApps) {
		if (inSharedGroup) {
		    for (i=0; i < s.keys.length; i++) {
			hkIndex = s.keys[i].equals(localToVworldKeys, 0,
						   localToVworldKeys.length);
			ArrayList l = (ArrayList)savedParentAltApps.get(hkIndex);
			ArrayList gl = (ArrayList)altAppearances.get(hkIndex);
			if (l != null) {
			    size = l.size();
			    for (k = 0; k < size; k++) {
				gl.remove(l.get(k));
			    }
			}
			    
		    }
		}
		else {
		    ArrayList l = (ArrayList)savedParentAltApps.get(0);
		    size = l.size();
		    for (int m = 0; m < size; m++) {
			altAppearances.remove(l.get(m));
		    }
		}
	    }
	}	

	if (collisionTarget) {
	    GroupRetained g;
	    if (inSharedGroup) {
		for (i=s.keys.length-1; i >=0; i--) {
		    HashKey hkey = s.keys[i];
		    for (int j = mirrorGroup.size()-1; j >=0 ; j--) {
			g = (GroupRetained) mirrorGroup.get(j);
			if (g.key.equals(hkey)) {
			    s.nodeList.add(mirrorGroup.remove(j));
			    if (s.transformTargets != null &&
				s.transformTargets[j] != null) {
				s.transformTargets[j].addNode(g, Targets.GRP_TARGETS);
			    }
			    break;
			}
			
		    }
		}
	    } else {
                g = (GroupRetained)mirrorGroup.get(0);
                if (s.transformTargets != null &&
			s.transformTargets[0] != null) {
                    s.transformTargets[0].addNode(g, Targets.GRP_TARGETS);
		}
                s.nodeList.add(mirrorGroup.remove(0)); 
	    }
	}
	s.lights = savedParentLights;
	s.modelClips = savedParentMclips;
	s.fogs = savedParentFogs;
	s.altAppearances = savedParentAltApps;
	isInClearLive = false;
    }

    // This is only used by alternateCollisionTarget
    public BoundingBox computeBoundingHull() {
	return collisionVwcBounds;
    }

    // If isSwitchOn cached here, we don't need to traverse up the tree
    public boolean isEnable() {
	return isNodeSwitchOn(this.sourceNode, key);
    }

    // If isSwitchOn cached here, we don't need to traverse up the tree
    // This method does nothing with vis.
    public boolean isEnable(int vis) {
	return isNodeSwitchOn(this.sourceNode, key);
    }

    // Can't use getLocale, it is used by BranchGroupRetained
    public Locale getLocale2() {
	return locale;
    }

    /**
     * Return true of nodeR is not under a switch group or
     * nodeR is enable under a switch group.
     */
    static boolean isNodeSwitchOn(NodeRetained node, HashKey key) {
	NodeRetained prevNode = null;
	if (key != null) {
	    key = new HashKey(key);
	}

	synchronized (node.universe.sceneGraphLock) {
	    do {
		if ((node instanceof SwitchRetained) &&
		    (prevNode != null) && 
		    !validSwitchChild((SwitchRetained) node, prevNode)) {
		    return false;
		}
		prevNode = node;
		if (node instanceof SharedGroupRetained) {
		    // retrieve the last node ID
		    String nodeId = key.getLastNodeId();
		    Vector parents = ((SharedGroupRetained) node).parents;
		    // find the matching link
		    for(int i=parents.size()-1; i >=0; i--) {
			NodeRetained link = (NodeRetained) parents.get(i);
			if (link.nodeId.equals(nodeId)) {
			    node = link;
			    break;
			}
		    }
		    if (node == prevNode) {
			// Fail to found a matching link, this is
			// probably cause by BHTree not yet updated 
			// because message not yet arrive
			// when collision so it return current node as target.
			return false;
		    }
		} else {
		    node = node.parent;
		}
	    } while (node != null);
	    // reach locale
	}
	return true;
    }


						       
    /** 
     * Determinte if nodeR is a valid child to render for
     * Switch Node swR.
     */
    static boolean validSwitchChild(SwitchRetained sw,
				    NodeRetained node) {

	int whichChild = sw.whichChild;
	
	if (whichChild == Switch.CHILD_NONE) {
	    return false;
	}

	if (whichChild == Switch.CHILD_ALL) {
	    return true;
	}

	ArrayList children = sw.children;
	
	if (whichChild >= 0) { // most common case
	    return (children.get(whichChild) == node);
	}

	// Switch.CHILD_MASK
	for (int i=children.size()-1; i >=0; i--) {
	    if (sw.childMask.get(i) &&
		(children.get(i) == node)) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Create mirror group when this Group AlternateCollisionTarget
     * is set to true while live.
     */
    void createMirrorGroup() {	
	GroupRetained g;

	mirrorGroup = new ArrayList();
    
	Bounds bound = (collisionBound != null ?
			collisionBound : getEffectiveBounds());

	if (inSharedGroup) {
	    for (int i=0; i < localToVworldKeys.length; i++) {
		g = new GroupRetained();
		g.key = localToVworldKeys[i];
		g.localToVworld = new Transform3D[1][];
		g.localToVworldIndex = new int[1][];
		g.localToVworld[0] = localToVworld[i];
		g.localToVworldIndex[0] = localToVworldIndex[i];
		g.collisionVwcBounds = new BoundingBox();
		g.collisionVwcBounds.transform(bound, g.getCurrentLocalToVworld());
		g.sourceNode = this;
		g.locale = locale; // need by getVisibleGeometryAtom()
		mirrorGroup.add(g);
	    }
	} else {
	    g = new GroupRetained();
	    g.localToVworld = new Transform3D[1][];
	    g.localToVworldIndex = new int[1][];
	    g.localToVworld[0] = localToVworld[0];
	    g.localToVworldIndex[0] = localToVworldIndex[0];
	    g.collisionVwcBounds = new BoundingBox();
	    g.collisionVwcBounds.transform(bound, g.getCurrentLocalToVworld());
	    g.sourceNode = this;
	    g.locale = locale; // need by getVisibleGeometryAtom()
	    mirrorGroup.add(g);
	}	
    }

    void setBoundsAutoCompute(boolean autoCompute) {
        if (autoCompute != boundsAutoCompute) {
            super.setBoundsAutoCompute(autoCompute);
            if (!autoCompute) {
                localBounds = getEffectiveBounds();
            }
            if (source.isLive() && collisionBound == null && autoCompute 
                && mirrorGroup != null) {

                J3dMessage message = VirtualUniverse.mc.getMessage();
                message.type = J3dMessage.COLLISION_BOUND_CHANGED;
                message.threads = J3dThread.UPDATE_TRANSFORM |
		    J3dThread.UPDATE_GEOMETRY;
                message.universe = universe;
                message.args[0] = this;
                VirtualUniverse.mc.processMessage(message);
            }
        }
    }

    void setBounds(Bounds bounds) {
        super.setBounds(bounds);
        if (source.isLive() && !boundsAutoCompute &&
	    collisionBound == null && mirrorGroup != null) {

            J3dMessage message = VirtualUniverse.mc.getMessage();
            message.type = J3dMessage.COLLISION_BOUND_CHANGED;
            message.threads = J3dThread.UPDATE_TRANSFORM |
		J3dThread.UPDATE_GEOMETRY;
            message.universe = universe;
            message.args[0] = this;
            VirtualUniverse.mc.processMessage(message);
        }
    }


    int[] processViewSpecificInfo(int mode, HashKey k, View v, ArrayList vsgList, int[] keyList,
				  ArrayList leafList) {
	int nchildren = children.size();
	if (source.isLive()) {
	    for (int i = 0; i < nchildren; i++) {
		NodeRetained child = (NodeRetained) children.get(i);
		if (child instanceof LeafRetained) {
		    if (child instanceof LinkRetained) {
			int lastCount = k.count;
			LinkRetained ln = (LinkRetained) child;
			if (k.count == 0) {
			    k.append(locale.nodeId);
			}
			keyList =  ((GroupRetained)(ln.sharedGroup)).
			    processViewSpecificInfo(mode, k.append("+").append(ln.nodeId), v, vsgList,
						    keyList, leafList);
			k.count = lastCount;
		    }
		    else {
			((LeafRetained)child).getMirrorObjects(leafList, k);
		    }
		} else {
		    keyList =  child.processViewSpecificInfo(mode, k, v, vsgList, keyList, leafList);
		}
	    }
	}
	return keyList;
    }

    void findSwitchInfo(SetLiveState s, NodeRetained parentNode,
                                NodeRetained childNode, NodeRetained linkNode) {

        NodeRetained child;
        NodeRetained parent;

	parentSwitchLinkChildIndex = -1;

	// traverse up scene graph to find switch parent information
        if (!inSharedGroup) {
            child = (linkNode == null)? childNode: linkNode;
	    parent = parentNode;
            while (parent != null) {
		if (parent instanceof SwitchRetained) {
		    s.switchLevels[0]++;
		    if (s.closestSwitchParents[0] == null) {
		        s.closestSwitchParents[0] = (SwitchRetained)parent;
		        s.closestSwitchIndices[0] = 
				((SwitchRetained)parent).switchIndexCount++;
		    }
		    if (parentSwitchLinkChildIndex == -1) {
		        parentSwitchLinkChildIndex = 
                            	((GroupRetained)parent).children.indexOf(child);
		    }
		} else if (parent instanceof SharedGroupRetained) {
		    if (parentSwitchLinkChildIndex == -1) {
		        parentSwitchLinkChildIndex = 
                            	((GroupRetained)parent).children.indexOf(child);
		    }
		}
		child = parent;
		parent = child.parent;
            }
        } else {
            HashKey key;
	    int i,j;

            s.switchLevels = new int[localToVworldKeys.length];
            s.closestSwitchParents =
                                new SwitchRetained[localToVworldKeys.length];
            s.closestSwitchIndices = new int[localToVworldKeys.length];
            for (i=0; i<localToVworldKeys.length; i++) {
                s.switchLevels[i] = -1;
                s.closestSwitchParents[i] = null;
                s.closestSwitchIndices[i] = -1;
            }

            for (i=0; i < localToVworldKeys.length; i++) {
                child = (linkNode == null)? childNode: linkNode;
                parent = parentNode;
                key = new HashKey(localToVworldKeys[i]);

                while (parent != null) {

		    if (parent instanceof SwitchRetained) {
		        s.switchLevels[i]++;
		        if (s.closestSwitchParents[i] == null) {
		            s.closestSwitchParents[i] = (SwitchRetained)parent;
		            s.closestSwitchIndices[i] = 
                                ((SwitchRetained)parent).switchIndexCount++;

		        }
		        if (parentSwitchLinkChildIndex == -1) {
		            parentSwitchLinkChildIndex = 
                            	((GroupRetained)parent).children.indexOf(child);
		        }
		    } else if (parent instanceof SharedGroupRetained) {
                        String nodeId = key.getLastNodeId();
                        Vector parents = ((SharedGroupRetained) parent).parents;
                        NodeRetained ln;

		        if (parentSwitchLinkChildIndex == -1) {
		            parentSwitchLinkChildIndex = 
                            	((GroupRetained)parent).children.indexOf(child);
		        }

                        for(j=0; j< parents.size(); j++) {
                            ln = (NodeRetained)parents.get(j);
                            if (ln.nodeId.equals(nodeId)) {
                                parent = ln;
                                break;
                            }
                        }
		    } 
		    child = parent;
		    parent = child.parent;
		}
            }
        }
    }

    static void gatherBlUsers(ArrayList blUsers, Object[] blArr) {
        ArrayList users;

        for (int i=0; i<blArr.length; i++) {
            users = ((BoundingLeafRetained)blArr[i]).users;
            synchronized(users) {
                blUsers.addAll(users);
            }
        }
    }

    // recursively found all geometryAtoms under this Group
    void searchGeometryAtoms(UnorderList list) {
	for (int i = children.size()-1; i >=0; i--) {
	    NodeRetained child = (NodeRetained)children.get(i);
	    child.searchGeometryAtoms(list);
	}
    }
}
