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


/**
 * The BoundingLeaf node defines a bounding region object that can be
 * referenced by other nodes to define a region of influence
 * (Fog and Light nodes), an application region (Background, Clip,
 * and Soundscape nodes), or a scheduling region (Sound and
 * Behavior nodes). The bounding region is defined in the local
 * coordinate system of the BoundingLeaf node. A reference to a
 * BoundingLeaf node can be used in place
 * of a locally defined bounds object for any of the aforementioned regions.
 * <P>
 * This allows an application to specify a bounding region in one coordinate system
 * (the local coordinate system of the BoundingLeaf node) other than the local
 * coordinate system of the node that references the bounds. For an example of how
 * this might be used, consider a closed room with a number of track lights. Each
 * light can move independent of the other lights and, as such, needs its own local
 * coordinate system. However, the bounding volume is used by all the lights in the
 * boundary of the room, which doesn't move when the lights move. In this example,
 * the BoundingLeaf node allows the bounding region to be defined in the local
 * coordinate system of the room, rather than in the local coordinate system of a
 * particular light. All lights can then share this single bounding volume.
 */
public class BoundingLeaf extends Leaf {
    /**
     * Specifies that this BoundingLeaf node allows read access to its
     * bounding region object.
     */
    public static final int
    ALLOW_REGION_READ = CapabilityBits.BOUNDING_LEAF_ALLOW_REGION_READ;

    /**
     * Specifies that this BoundingLeaf node allows write access to its
     * bounding region object.
     */
    public static final int
    ALLOW_REGION_WRITE = CapabilityBits.BOUNDING_LEAF_ALLOW_REGION_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_REGION_READ
    };

        /**
     * Constructs a BoundingLeaf node with a null (empty) bounding region.
     */
    public BoundingLeaf() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((BoundingLeafRetained)this.retained).createBoundingLeaf();
    }

    /**
     * Constructs a BoundingLeaf node with the specified bounding region.
     * @param region the bounding region of this leaf node
     */
    public BoundingLeaf(Bounds region) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((BoundingLeafRetained)this.retained).createBoundingLeaf();
	((BoundingLeafRetained)this.retained).initRegion(region);
    }

    /**
     * Sets this BoundingLeaf node's bounding region.
     * @param region the bounding region of this leaf node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setRegion(Bounds region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_REGION_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("BoundingLeaf0"));

	if (isLive())
	    ((BoundingLeafRetained)this.retained).setRegion(region);
	else
	    ((BoundingLeafRetained)this.retained).initRegion(region);
    }

    /**
     * Retrieves this BoundingLeaf's bounding region.
     * @return the bounding region of this leaf node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getRegion() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_REGION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("BoundingLeaf1"));

	return ((BoundingLeafRetained)this.retained).getRegion();
    }

    /**
     * Creates the BoundingLeafRetained object that this
     * BoundingLeaf object will point to.
     */
    void createRetained() {
        this.retained = new BoundingLeafRetained();
        this.retained.setSource(this);
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
        BoundingLeaf bl = new BoundingLeaf();
        bl.duplicateNode(this, forceDuplicate);
        return bl;
    }



   /**
     * Copies all BoundingLeaf information from
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

	((BoundingLeafRetained) retained).initRegion(
             ((BoundingLeafRetained) originalNode.retained).getRegion());
    }

}
