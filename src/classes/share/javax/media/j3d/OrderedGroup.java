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

import java.util.Arrays;

/**
 * The OrderedGroup node is a Group that ensures its children render
 * in a specified order.  In addition to the list of children
 * inherited from the base Group class, OrderedGroup defines an
 * integer array of child indices that specify the order in which its
 * children are rendered.  This provides a level of indirection in
 * determining the rendering order of its children.  By default, the
 * child index order array is null, and the children are rendered in
 * increasing index order.
 *
 * <p>
 * When the child index order array is non-null, it must be the same
 * length as the number of children.  Every entry in the array must
 * have a unique value between 0 and <code>numChildren-1</code> (i.e.,
 * there must be no duplicate values and no missing indices).  The
 * order that the child indices appear in the child index order array
 * determines the order in which the children are rendered.  The child
 * at <code>childIndexOrder[0]</code> is rendered first, followed by
 * <code>childIndexOrder[1]</code>, and so on, with the child at
 * <code>childIndexOrder[numChildren-1]</code> rendered
 * last.
 *
 * <p>
 * The child index order array only affects rendering.  List
 * operations that refer to a child by index, such as getChild(index),
 * will not be altered by the entries in this array.  They will get,
 * enumerate, add, remove, etc., the children based on the actual
 * index in the group node.  However, some of the list operations,
 * such as addChild, removeChild, etc., will update the child index
 * order array as a result of their operation.  For example,
 * removeChild will remove the entry in the child index order array
 * corresponding to the removed child's index and adjust the remaining
 * entries accordingly.  See the documentation for these methods for
 * more details.
 */

public class OrderedGroup extends Group {

    private boolean checkArr[] = null;

    /**
     * Specifies that this OrderedGroup node
     * allows reading its child index order information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_CHILD_INDEX_ORDER_READ =
	CapabilityBits.ORDERED_GROUP_ALLOW_CHILD_INDEX_ORDER_READ;

    /**
     * Specifies that this OrderedGroup node
     * allows writing its child index order information.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_CHILD_INDEX_ORDER_WRITE =
	CapabilityBits.ORDERED_GROUP_ALLOW_CHILD_INDEX_ORDER_WRITE;


    /**
     * Constructs and initializes a new OrderedGroup node object.
     * The childIndexOrder array is initialized to null, meaning
     * that its children are rendered in increasing index order.
     */
    public OrderedGroup() {
    }


    /**
     * Sets the childIndexOrder array.  If the specified array is
     * null, this node's childIndexOrder array is set to null.  Its
     * children will then be rendered in increasing index order.  If
     * the specified array is not null, the entire array is copied to
     * this node's childIndexOrder array.  In this case, the length of
     * the array must be equal to the number of children in this
     * group, and every entry in the array must have a unique value
     * between 0 and <code>numChildren-1</code> (i.e., there must be
     * no duplicate values and no missing indices).
     *
     * @param childIndexOrder the array that is copied into this
     * node's child index order array; this can be null
     *
     * @exception IllegalArgumentException if the specified array is
     * non-null and any of the following are true:
     * <ul>
     * <li><code>childIndexOrder.length != numChildren</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] < 0</code>,
     * for <i>i</i> in <code>[0, numChildren-1]</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] >= numChildren</code>,
     * for <i>i</i> in <code>[0, numChildren-1]</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] ==
     * childIndexOrder[</code><i>j</i><code>]</code>,
     * for <i>i</i>,<i>j</i> in <code>[0, numChildren-1]</code>,
     * <i>i</i> <code>!=</code> <i>j</i>;</li>
     * </ul>
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setChildIndexOrder(int[] childIndexOrder) {
	verifyChildIndexOrderArray(childIndexOrder, 0);

	((OrderedGroupRetained)retained).setChildIndexOrder(childIndexOrder);
    }


    /**
     * Retrieves the current childIndexOrder array.
     *
     * @return a copy of this node's childIndexOrder array; this
     * can be null.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int[] getChildIndexOrder() {

	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILD_INDEX_ORDER_READ))
		throw new 
		    CapabilityNotSetException(J3dI18N.getString("OrderedGroup5"));

	return ((OrderedGroupRetained)this.retained).getChildIndexOrder();
    }

    /**
     * Appends the specified child node to this group node's list of
     * children, and sets the child index order array to the specified
     * array.  If the specified array is null, this node's
     * childIndexOrder array is set to null.  Its children will then
     * be rendered in increasing index order.  If the specified array
     * is not null, the entire array is copied to this node's
     * childIndexOrder array.  In this case, the length of the array
     * must be equal to the number of children in this group after the
     * new child has been added, and every entry in the array must
     * have a unique value between 0 and <code>numChildren-1</code>
     * (i.e., there must be no duplicate values and no missing
     * indices).
     *
     * @param child the child to add to this node's list of children
     *
     * @param childIndexOrder the array that is copied into this
     * node's child index order array; this can be null
     *
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     *
     * @exception RestrictedAccessException if this group node is part
     * of live
     * or compiled scene graph and the child node being added is not
     * a BranchGroup node
     *
     * @exception MultipleParentException if <code>child</code> has already
     * been added as a child of another group node.
     *
     * @exception IllegalArgumentException if the specified array is
     * non-null and any of the following are true:
     * <ul>
     * <li><code>childIndexOrder.length != numChildren</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] < 0</code>,
     * for <i>i</i> in <code>[0, numChildren-1]</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] >= numChildren</code>,
     * for <i>i</i> in <code>[0, numChildren-1]</code>;</li>
     * <li><code>childIndexOrder[</code><i>i</i><code>] ==
     * childIndexOrder[</code><i>j</i><code>]</code>,
     * for <i>i</i>,<i>j</i> in <code>[0, numChildren-1]</code>,
     * <i>i</i> <code>!=</code> <i>j</i>;</li>
     * </ul>
     *
     * @since Java 3D 1.3
     */
    public void addChild(Node child, int[] childIndexOrder) {
	
	verifyAddStates(child);
	verifyChildIndexOrderArray(childIndexOrder, 1);

	((OrderedGroupRetained)retained).addChild(child, childIndexOrder);

    }


    // Overridden methods from Group

    /**
     * Appends the specified child node to this group node's list of children.
     *
     * <p>
     * If the current child index order array is non-null, the array
     * is increased in size by one element, and a new element
     * containing the index of the new child is added to the end of
     * the array.  Thus, this new child will be rendered last.
     *
     * @param child the child to add to this node's list of children
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part
     * of live
     * or compiled scene graph and the child node being added is not
     * a BranchGroup node
     * @exception MultipleParentException if <code>child</code> has already
     * been added as a child of another group node.
     *
     * @since Java 3D 1.3
     */
    public void addChild(Node child) {
	// Just call super -- the extra work is done by the retained class
	super.addChild(child);
    }

    /**
     * Inserts the specified child node in this group node's list of
     * children at the specified index.
     * This method is only supported when the child index order array
     * is null.
     *
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
     * @exception IllegalStateException if the childIndexOrder array is
     * not null.
     *
     * @since Java 3D 1.3
     */
    public void insertChild(Node child, int index) {
	if (((OrderedGroupRetained)retained).userChildIndexOrder != null) {
	    throw new IllegalStateException(J3dI18N.getString("OrderedGroup6"));
	}

	// Just call super -- the extra work is done by the retained class
	super.insertChild(child, index);
    }

    /**
     * Removes the child node at the specified index from this group node's
     * list of children.
     *
     * <p>
     * If the current child index order array is non-null, the element
     * containing the removed child's index will be removed from the
     * child index order array, and the array will be reduced in size
     * by one element.  If the child removed was not the last child in
     * the Group, the values of the child index order array will be
     * updated to reflect the indices that were renumbered.  More
     * formally, each child whose index in the Group node was greater
     * than the removed element (before removal) will have its index
     * decremented by one.
     *
     * @param index which child to remove.  The <code>index</code>
     * must be a value
     * greater than or equal to 0 and less than <code>numChildren()</code>.
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     * @exception RestrictedAccessException if this group node is part of
     * live or compiled scene graph and the child node being removed is not
     * a BranchGroup node
     * @exception IndexOutOfBoundsException if <code>index</code> is invalid.
     *
     * @since Java 3D 1.3
     */
    public void removeChild(int index) {
	// Just call super -- the extra work is done by the retained class
	super.removeChild(index);
    }


    /**
     * Moves the specified branch group node from its existing location to
     * the end of this group node's list of children.
     *
     * <p>
     * If the current child index order array is non-null, the array
     * is increased in size by one element, and a new element
     * containing the index of the new child is added to the end of
     * the array.  Thus, this new child will be rendered last.
     *
     * @param branchGroup the branch group node to move to this node's list
     * of children
     * @exception CapabilityNotSetException if the appropriate capability is
     * not set and this group node is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void moveTo(BranchGroup branchGroup) {
	// Just call super -- the extra work is done by the retained class
	super.moveTo(branchGroup);
    }

    /**
     * Removes the specified child node from this group node's
     * list of children.
     * If the specified object is not in the list, the list is not modified.
     *
     * <p>
     * If the current child index order array is non-null, the element
     * containing the removed child's index will be removed from the
     * child index order array, and the array will be reduced in size
     * by one element.  If the child removed was not the last child in
     * the Group, the values of the child index order array will be
     * updated to reflect the indices that were renumbered.  More
     * formally, each child whose index in the Group node was greater
     * than the removed element (before removal) will have its index
     * decremented by one.
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
	// Just call super -- the extra work is done by the retained class
	super.removeChild(child);
    }

    /**
     * Removes all children from this Group node.
     *
     * <p>
     * If the current child index order array is non-null, then it is set to
     * a zero-length array (the empty set).  Note that a zero-length
     * child index order array is not the same as a null array in that
     * as new elements are added, the child index order array will grow
     * and will be used instead of the Group's natural child order.
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
	// Just call super -- the extra work is done by the retained class
	super.removeAllChildren();
    }


    /**
     * Creates the retained mode OrderedGroupRetained object that this
     * OrderedGroup component object will point to.
     */
    void createRetained() {
	this.retained = new OrderedGroupRetained();
	this.retained.setSource(this);
    }
    
    void verifyAddStates(Node child) {

	if (child instanceof SharedGroup) {
	    throw new IllegalArgumentException(J3dI18N.getString("Group2"));
	}
	
	if (isLiveOrCompiled()) {
	    if (! (child instanceof BranchGroup))
		throw new RestrictedAccessException(J3dI18N.getString("Group12"));
	    
	    if(!this.getCapability(ALLOW_CHILDREN_EXTEND))
		throw new CapabilityNotSetException(J3dI18N.getString("Group16"));
	}
    }

    void verifyChildIndexOrderArray(int[] cIOArr, int plus) {

	if (isLiveOrCompiled()) {
	    
	    if(!this.getCapability(ALLOW_CHILD_INDEX_ORDER_WRITE)) 
		throw new
		    CapabilityNotSetException(J3dI18N.getString("OrderedGroup4"));
	}

	if(cIOArr != null) {	    
	    
	    if(cIOArr.length != (((GroupRetained)retained).children.size() + plus)) {
		throw new 
		    IllegalArgumentException(J3dI18N.getString("OrderedGroup0"));
	    }
	    
	    if((checkArr == null) || (checkArr.length != cIOArr.length)) {
		checkArr = new boolean[cIOArr.length];
	    }
	    
	    Arrays.fill(checkArr, false);
	    
	    for(int i=0; i<cIOArr.length; i++) {
		if(cIOArr[i] < 0) {
		    throw new
			IllegalArgumentException(J3dI18N.getString("OrderedGroup1"));
		}
		else if(cIOArr[i] >= cIOArr.length) {
		    throw new
			IllegalArgumentException(J3dI18N.getString("OrderedGroup2"));
		}
		else if(checkArr[cIOArr[i]]) {
		    throw new
			IllegalArgumentException(J3dI18N.getString("OrderedGroup3"));
		}
		else {
		    checkArr[cIOArr[i]] = true;
		}
	    }
	}
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
	 OrderedGroup og = new OrderedGroup();
	 og.duplicateNode(this, forceDuplicate);
	 return og;
    }
}
