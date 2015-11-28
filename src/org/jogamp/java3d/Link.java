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

package org.jogamp.java3d;

/**
 * A Link leaf node allows an application to reference a shared graph,
 * rooted by a SharedGroup node, from within a branch graph or another
 * shared graph.
 * Any number of Link nodes can refer to the same SharedGroup node.
 */

public class Link extends Leaf {
    /**
     * For Link nodes, specifies that the node allows access to
     * its object's SharedGroup information.
     */
    public static final int
    ALLOW_SHARED_GROUP_READ = CapabilityBits.LINK_ALLOW_SHARED_GROUP_READ;

    /**
     * For Link nodes, specifies that the node allows writing
     * its object's SharedGroup information.
     */
    public static final int
    ALLOW_SHARED_GROUP_WRITE = CapabilityBits.LINK_ALLOW_SHARED_GROUP_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_SHARED_GROUP_READ
    };

    /**
     * Constructs a Link node object that does not yet point to a
     * SharedGroup node.
     */
    public Link() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a Link node object that points to the specified
     * SharedGroup node.
     * @param sharedGroup the SharedGroup node
     */
    public Link(SharedGroup sharedGroup) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((LinkRetained)this.retained).setSharedGroup(sharedGroup);
    }

    /**
     * Creates the retained mode LinkRetained object that this
     * Link object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new LinkRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the node's SharedGroup reference.
     * @param sharedGroup the SharedGroup node to reference
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setSharedGroup(SharedGroup sharedGroup) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_SHARED_GROUP_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Link0"));
	((LinkRetained)this.retained).setSharedGroup(sharedGroup);
    }

    /**
     * Retrieves the node's SharedGroup reference.
     * @return the SharedGroup node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public SharedGroup getSharedGroup() {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_SHARED_GROUP_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Link1"));
	return ((LinkRetained)this.retained).getSharedGroup();
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <br>
     * The cloned Link node will refer to the same
     * SharedGroup as the original node.  The SharedGroup referred to by
     * this Link node will not be cloned.
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
    @Override
    public Node cloneNode(boolean forceDuplicate) {
	Link l = new Link();
	l.duplicateNode(this, forceDuplicate);
	return l;
    }

    /**
     * Copies all Link information from
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
    @Override
    void duplicateAttributes(Node originalNode,  boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);
	((LinkRetained) retained).setSharedGroup(
		 ((LinkRetained) originalNode.retained).getSharedGroup());
    }
}
