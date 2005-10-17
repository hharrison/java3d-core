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

import java.util.BitSet;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * The Group node object is a general-purpose grouping node. Group
 * nodes have exactly one parent and an arbitrary number of children
 * that are rendered in an unspecified order (or in parallel).  Null
 * children are allowed; no operation is performed on a null child
 * node.  Operations on Group node objects include adding, removing,
 * and enumerating the children of the Group node. The subclasses of
 * Group node add additional semantics.
 */

public class Group extends Node {
    /**
     * Specifies that this Group node allows reading its children.
     */
    public static final int
    ALLOW_CHILDREN_READ = CapabilityBits.GROUP_ALLOW_CHILDREN_READ;

    /**
     * Specifies that this Group node allows writing its children.
     */
    public static final int
    ALLOW_CHILDREN_WRITE = CapabilityBits.GROUP_ALLOW_CHILDREN_WRITE;

    /**
     * Specifies that this Group node allows adding new children.
     */
    public static final int
    ALLOW_CHILDREN_EXTEND = CapabilityBits.GROUP_ALLOW_CHILDREN_EXTEND;

    /**
     * Specifies that this Group node allows reading its collision Bounds
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_READ =
        CapabilityBits.GROUP_ALLOW_COLLISION_BOUNDS_READ;

    /**
     * Specifies that this Group node allows writing its collision Bounds
     */
    public static final int
    ALLOW_COLLISION_BOUNDS_WRITE =
        CapabilityBits.GROUP_ALLOW_COLLISION_BOUNDS_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_CHILDREN_READ,
        ALLOW_COLLISION_BOUNDS_READ
    };
    
    
    /**
     * Creates the retained mode GroupRetained object that this
     * Group component object will point to.
     */
    void createRetained() {
	retained = new GroupRetained();
	retained.setSource(this);
    }
  

    /**
      * Sets the collision bounds of a node.
      * @param bounds the collision bounding object for a node
      * @exception CapabilityNotSetException if appropriate capability is
      * not set and this object is part of live or compiled scene graph
      */
    public void setCollisionBounds(Bounds bounds) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLLISION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Group0"));

	((GroupRetained)this.retained).setCollisionBounds(bounds);
    }

    /**
      * Returns the collision bounding object of this node.
      * @return the node's collision bounding object
      * @exception CapabilityNotSetException if appropriate capability is
      * not set and this object is part of live or compiled scene graph
      */
    public Bounds getCollisionBounds() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLLISION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Group1"));

	return ((GroupRetained)this.retained).getCollisionBounds();
    }

    /**
     * Replaces the child node at the specified index in this
     * group node's list of children with the specified child.
     * @param child the new child
     * @param index which child to replace.  The <code>index</code> must
     * be a value
     * greater than or equal to 0 and less than <code>numChildren()</code>.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live or compiled scene graph and the child node being set is not
     * a BranchGroup node
     * @exception MultipleParentException if <code>child</code> has already
     * been added as a child of another group node
     * @exception IndexOutOfBoundsException if <code>index</code> is invalid
     */
    public void setChild(Node child, int index) {
	if (child instanceof SharedGroup) {
	    throw new IllegalArgumentException(J3dI18N.getString("Group2"));
	}

	if (isLiveOrCompiled()) {
	    Node oldchild =
                (Node) ((GroupRetained)this.retained).getChild(index);
	    if (! (child instanceof BranchGroup))
		throw new RestrictedAccessException(J3dI18N.getString("Group3"));

	    if (!getCapability(ALLOW_CHILDREN_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Group13"));

	    if ((oldchild != null) && 
		(! ((BranchGroup)oldchild).getCapability(BranchGroup.ALLOW_DETACH))) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group4"));
	    }
	}

	((GroupRetained)retained).setChild(child, index);
    }
  
    /**
     * Inserts the specified child node in this group node's list of
     * children at the specified index.
     * @param child the new child
     * @param index at which location to insert. The <code>index</code>
     * must be a value
     * greater than or equal to 0 and less than or equal to
     * <code>numChildren()</code>.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live
     * or compiled scene graph and the child node being inserted is not
     * a BranchGroup node
     * @exception MultipleParentException if <code>child</code> has already
     * been added as a child of another group node.
     * @exception IndexOutOfBoundsException if <code>index</code> is invalid.
     */
    public void insertChild(Node child, int index) {
	if (child instanceof SharedGroup) {
	    throw new IllegalArgumentException(J3dI18N.getString("Group2"));
	}

	if (isLiveOrCompiled()) {
	    if (! (child instanceof BranchGroup))
		throw new RestrictedAccessException(J3dI18N.getString("Group6"));

	    if (!this.getCapability(ALLOW_CHILDREN_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Group14"));
	}

	((GroupRetained)this.retained).insertChild(child, index);
    }
  
    /**
     * Removes the child node at the specified index from this group node's
     * list of children.
     * @param index which child to remove.  The <code>index</code>
     * must be a value
     * greater than or equal to 0 and less than <code>numChildren()</code>.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live or compiled scene graph and the child node being removed is not
     * a BranchGroup node
     * @exception IndexOutOfBoundsException if <code>index</code> is invalid.
     */
    public void removeChild(int index) {
	if (isLiveOrCompiled()) {
	    Node child = ((GroupRetained)this.retained).getChild(index);
	    if (!(child instanceof BranchGroup)) {
		throw new RestrictedAccessException(J3dI18N.getString("Group7"));
	    }

	    if (!this.getCapability(ALLOW_CHILDREN_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group15"));
	    }

	    if (!((BranchGroup)child).getCapability(BranchGroup.ALLOW_DETACH)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group4"));
	    }
	}

	((GroupRetained)this.retained).removeChild(index);
    }
  
    /**
     * Retrieves the child node at the specified index in
     * this group node's list of children.
     * @param index which child to return.
     * @return the children at location index.  The <code>index</code>
     * must be a value
     * greater than or equal to 0 and less than <code>numChildren()</code>.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception IndexOutOfBoundsException if <code>index</code> is invalid.
     */
    public Node getChild(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILDREN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Group9"));

	return (Node) ((GroupRetained)this.retained).getChild(index);
    }
  
    /**
     * Returns an Enumeration object of this group node's list of children.
     * @return an Enumeration object of all the children
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     */
    public Enumeration getAllChildren() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILDREN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Group9"));

	return (Enumeration)((GroupRetained)this.retained).getAllChildren();
    }

    /**
     * Appends the specified child node to this group node's list of children.
     * @param child the child to add to this node's list of children
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part
     * of live
     * or compiled scene graph and the child node being added is not
     * a BranchGroup node
     * @exception MultipleParentException if <code>child</code> has already
     * been added as a child of another group node.
     */
    public void addChild(Node child) {
	if (child instanceof SharedGroup) {
	    throw new IllegalArgumentException(J3dI18N.getString("Group2"));
	}

	if (isLiveOrCompiled()) {
	    if (! (child instanceof BranchGroup))
		throw new RestrictedAccessException(J3dI18N.getString("Group12"));

	    if(!this.getCapability(ALLOW_CHILDREN_EXTEND))
		throw new CapabilityNotSetException(J3dI18N.getString("Group16"));
	}

	((GroupRetained)this.retained).addChild(child);
    }
  
    /**
     * Moves the specified branch group node from its existing location to
     * the end of this group node's list of children.
     * @param branchGroup the branch group node to move to this node's list
     * of children
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     */
    public void moveTo(BranchGroup branchGroup) {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_CHILDREN_EXTEND))
		throw new CapabilityNotSetException(J3dI18N.getString("Group16"));

	    if (! branchGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group4"));
	    }
	}

	((GroupRetained)this.retained).moveTo(branchGroup);
    }
  
    /**
     * Returns a count of this group node's children.
     * @return the number of children descendant from this node.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     */
    public int numChildren() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILDREN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Group9"));

	return ((GroupRetained)this.retained).numChildren();
    }


    /**
     * Retrieves the index of the specified child node in
     * this group node's list of children.
     *
     * @param child the child node to be looked up.
     * @return the index of the specified child node;
     * returns -1 if the object is not in the list.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int indexOfChild(Node child) {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILDREN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Group9"));

	return ((GroupRetained)this.retained).indexOfChild(child);
    }


    /**
     * Removes the specified child node from this group node's
     * list of children.
     * If the specified object is not in the list, the list is not modified.
     *
     * @param child the child node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live or compiled scene graph and the child node being removed is not
     * a BranchGroup node
     *
     * @since Java 3D 1.3
     */
    public void removeChild(Node child) {

	if (isLiveOrCompiled()) {
	    if (!(child instanceof BranchGroup)) {
		throw new RestrictedAccessException(J3dI18N.getString("Group7"));
	    }

	    if (!this.getCapability(ALLOW_CHILDREN_WRITE)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group15"));
	    }

	    if (!((BranchGroup)child).getCapability(BranchGroup.ALLOW_DETACH)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Group4"));
	    }
	}

	((GroupRetained)retained).removeChild(child);
    }


    /**
     * Removes all children from this Group node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live or compiled scene graph and any of the children being removed are
     * not BranchGroup nodes
     *
     * @since Java 3D 1.3
     */
    public void removeAllChildren() {

	if (isLiveOrCompiled()) {
	    GroupRetained groupR = (GroupRetained)this.retained;
	    for (int index = groupR.numChildren() - 1; index >= 0; index--) {
		Node child = groupR.getChild(index);
		if (! (child instanceof BranchGroup))
		    throw new RestrictedAccessException(J3dI18N.getString("Group7"));

		if (!this.getCapability(ALLOW_CHILDREN_WRITE))
		    throw new CapabilityNotSetException(J3dI18N.getString("Group15"));

		if (!((BranchGroup)child).getCapability(BranchGroup.ALLOW_DETACH)) {
		    throw new CapabilityNotSetException(J3dI18N.getString("Group4"));
		}
	    }
	}

	((GroupRetained)retained).removeAllChildren();
    }


    /**
     * Causes this Group node to be reported as the collision target when
     * collision is being used and this node or any of its children is in 
     * a collision. The default value is false.  For collision with
     * USE_GEOMETRY set, the collision traverser will check the geometry
     * of all the Group node's leaf descendants; for collision with 
     * USE_BOUNDS set, the collision traverser will only check the bounds
     * at this Group node.  In both cases, if there is a collision, this 
     * Group node will be reported as the colliding object in the
     * SceneGraphPath.  This reporting is done regardless of whether
     * ENABLE_COLLISION_REPORTING
     * is set for this group node (setting alternate collision target to
     * true implies collision reporting).
     * @param  target  Indicates whether this Group node can be the target
     * of a collision.
     * @see WakeupOnCollisionEntry
     * @see WakeupOnCollisionMovement
     * @see WakeupOnCollisionExit
     */
    public void setAlternateCollisionTarget(boolean target) {
       ((GroupRetained)this.retained).setAlternateCollisionTarget(target);
    }

    /**
      * Returns the collision target state.
      * @return Indicates whether this Group node can be the target of a
      * collision.
      */
    public boolean getAlternateCollisionTarget() {
        return ((GroupRetained)this.retained).getAlternateCollisionTarget();
    }

    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code> and
     * then <code>cloneTree</code> is called for each child node.  For
     * Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     *
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code>
     *  flag to be ignored.  When <code>false</code>, the value of each
     *  node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     *
     *
     * @return a reference to the cloned scene graph.
     *
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    Node cloneTree(boolean forceDuplicate, Hashtable nodeHashtable) {
        Group g = (Group) super.cloneTree(forceDuplicate, nodeHashtable);
	GroupRetained rt = (GroupRetained) retained;

        int nChildren = rt.numChildren();
        // call cloneTree on all child nodes
        for (int i = 0; i < nChildren; i++) { 
            Node n = rt.getChild(i);
            Node clonedN = n.cloneTree(forceDuplicate, nodeHashtable);
            // add the cloned child to the cloned group node
            ((GroupRetained) g.retained).addChild(clonedN);

        }
        return g;
    }



   /**
     * Copies all Node information from
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
        super.duplicateAttributes(originalNode, forceDuplicate);

        GroupRetained attr = (GroupRetained) originalNode.retained;
	GroupRetained rt = (GroupRetained) retained;

	rt.setCollisionBounds(attr.getCollisionBounds());
	rt.setAlternateCollisionTarget(attr.getAlternateCollisionTarget());
	// throw away any child create before, since some node such as
	// Sphere has already created its own branch
	// Without doing this, we may end up with two branches with exactly
	// the same content when cloneTree() is invoked.
	rt.children.clear();
    }

    
    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
     public Node cloneNode(boolean forceDuplicate) {
	 Group g = new Group();
	 g.duplicateNode(this, forceDuplicate);
	 return g;
    }


    /**
     * Constructs a Group node with default parameters.  The default
     * values are as follows:
     * <ul>
     * collision bounds : null<br>
     * alternate collision target : false<br>
     * </ul>
     */
    public Group() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        
    }
}
