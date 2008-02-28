/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.BitSet;

/**
 * The Switch node controls which of its children will be rendered.
 * It defines a child selection value (a switch value) that can either
 * select a single child, or it can select 0 or more children using a
 * mask to indicate which children are selected for rendering.
 * The Switch node contains an ordered list of children, but the
 * index order of the children in the list is only used for selecting
 * the appropriate child or children and does not specify rendering
 * order.
 */

public class Switch extends Group {
  
    /**
     * Specifies that this node allows reading its child selection
     * and mask values and its current child.
     */
    public static final int
    ALLOW_SWITCH_READ = CapabilityBits.SWITCH_ALLOW_SWITCH_READ;

    /**
     * Specifies that this node allows writing its child selection
     * and mask values.
     */
    public static final int
    ALLOW_SWITCH_WRITE = CapabilityBits.SWITCH_ALLOW_SWITCH_WRITE;

    /**
     * Specifies that no children are rendered.
     * This value may be used in place of a non-negative child
     * selection index.
     */
    public static final int CHILD_NONE = -1;

    /**
     * Specifies that all children are rendered. This setting causes
     * the switch node to function as an ordinary group node.
     * This value may be used in place of a non-negative child
     * selection index.
     */
    public static final int CHILD_ALL  = -2;

    /**
     * Specifies that the childMask BitSet is
     * used to select which children are rendered.
     * This value may be used in place of a non-negative child
     * selection index.
     */
    public static final int CHILD_MASK = -3;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_SWITCH_READ
    };
    
    /**
     * Constructs a Switch node with default parameters.
     * The default values are as follows:
     * <ul>
     * child selection index : CHILD_NONE<br>
     * child selection mask : false (for all children)<br>
     * </ul>
     */
    public Switch() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        
    }

    /**
     * Constructs and initializes a Switch node using the specified
     * child selection index.
     * @param whichChild the initial child selection index
     */
    public Switch(int whichChild) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        

        ((SwitchRetained)this.retained).setWhichChild(whichChild, true);
    }

    /**
     * Constructs and initializes a Switch node using the specified
     * child selection index and mask.
     * @param whichChild the initial child selection index
     * @param childMask the initial child selection mask
     */
    public Switch(int whichChild, BitSet childMask){
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        

        ((SwitchRetained)this.retained).setWhichChild(whichChild, true);
	((SwitchRetained)this.retained).setChildMask(childMask);
    }

    /**
     * Creates the retained mode SwitchRetained object that this
     * Switch object will point to.
     */
    void createRetained() {
	this.retained = new SwitchRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the child selection index that specifies which child is rendered.
     * If the value is out of range, then no children are drawn.
     * @param child a non-negative integer index value, indicating a
     * specific child, or one of the following constants: CHILD_NONE,
     * CHILD_ALL, or CHILD_MASK.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #CHILD_NONE
     * @see #CHILD_ALL
     * @see #CHILD_MASK
     */
    public void setWhichChild(int child) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SWITCH_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Switch0"));

	((SwitchRetained)this.retained).setWhichChild(child, false);
    }

    /**
     * Retrieves the current child selection index that specifies which
     * child is rendered.
     * @return a non-negative integer index value, indicating a
     * specific child, or one of the following constants: CHILD_NONE,
     * CHILD_ALL, or CHILD_MASK
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #CHILD_NONE
     * @see #CHILD_ALL
     * @see #CHILD_MASK
     */
    public int getWhichChild() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SWITCH_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Switch1"));

	return ((SwitchRetained)this.retained).getWhichChild();
    }

    /**
     * Sets the child selection mask.  This mask is used when
     * the child selection index is set to CHILD_MASK.
     * @param childMask a BitSet that specifies which children are rendered
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setChildMask(BitSet childMask) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SWITCH_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Switch2"));

	((SwitchRetained)this.retained).setChildMask(childMask);
    }

    /**
     * Retrieves the current child selection mask.  This mask is used when
     * the child selection index is set to CHILD_MASK.
     * @return the child selection mask
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BitSet getChildMask() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SWITCH_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Switch3"));

	return ((SwitchRetained)this.retained).getChildMask();
    }

    /**
     * Retrieves the currently selected child. If the child selection index
     * is out of range or is set to CHILD_NONE, CHILD_ALL, or CHILD_MASK,
     * then this method returns null.
     * @return a reference to the current child chosen for rendering
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Node currentChild() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CHILDREN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Switch4"));

	return ((SwitchRetained)this.retained).currentChild();
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
        Switch s = new Switch();
        s.duplicateNode(this, forceDuplicate);
        return s;
    }

   /**
     * Copies all Switch information from
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
     * @see Group#cloneNode
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

        SwitchRetained attr = (SwitchRetained) originalNode.retained;
	SwitchRetained rt = (SwitchRetained) retained;

	rt.setChildMask(attr.getChildMask());
	rt.setWhichChild(attr.getWhichChild(), true);
    }
}
