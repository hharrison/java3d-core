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
 * The Clip leaf node defines the back, or far, clip distance in
 * the virtual universe.
 * The distance is specified in the local coordinate system of this node.
 * This node also specifies an application
 * region in which this clip node is active.
 * A Clip node is active when its application region intersects
 * the ViewPlatform's activation volume. If multiple Clip nodes
 * are active, the Clip node that is "closest" to the eye will be
 * used.
 * If no clip node is in scope of the view platform
 * associated with the current view, then the back clip distance is
 * defined by the View object.
 * The front clip distance is always defined by the
 * View object.
 *
 * @see View
 */
public class Clip extends Leaf {

    /**
     * Specifies that the Clip allows read access to its application
     * bounds and bounding leaf at runtime.
     */
    public static final int
    ALLOW_APPLICATION_BOUNDS_READ = CapabilityBits.CLIP_ALLOW_APPLICATION_BOUNDS_READ;

    /**
     * Specifies that the Clip allows write access to its application
     * bounds and bounding leaf at runtime.
     */
    public static final int
    ALLOW_APPLICATION_BOUNDS_WRITE = CapabilityBits.CLIP_ALLOW_APPLICATION_BOUNDS_WRITE;

    /**
      * Specifies that the Clip allows read access to its back distance
      * at runtime.
      */
     public static final int
    ALLOW_BACK_DISTANCE_READ = CapabilityBits.CLIP_ALLOW_BACK_DISTANCE_READ;

    /**
      * Specifies that the Clip allows write access to its back distance
      * at runtime.
      */
     public static final int
    ALLOW_BACK_DISTANCE_WRITE = CapabilityBits.CLIP_ALLOW_BACK_DISTANCE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_APPLICATION_BOUNDS_READ,
        ALLOW_BACK_DISTANCE_READ
    };

    /**
     * Constructs a Clip node with default parameters.  The default
     * values are as follows:
     * <ul>
     * back clip distance : 100 meters<sr>
     * application bounds : null<br>
     * application bounding leaf : null<br>
     * </ul>
     */
    public Clip () {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a Clip node with the specified back clip distance.
     */
    public Clip(double backDistance) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ClipRetained)this.retained).initBackDistance(backDistance);
    }

    /**
     * Sets the back clip distance to the specified value.
     * There are several considerations that need to be taken into
     * account when choosing values for the front and back clip
     * distances. These are enumerated in the description of
     * <a href=View.html#setFrontClipDistance(double)>
     * View.setFrontClipDistance</a>.
     * @param backDistance the new back clip distance in meters
     * @see View#setFrontClipDistance
     * @see View#setBackClipDistance
     */
    public void setBackDistance(double backDistance) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_BACK_DISTANCE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip0"));

	if (isLive())
	    ((ClipRetained)this.retained).setBackDistance(backDistance);
	else
	    ((ClipRetained)this.retained).initBackDistance(backDistance);
    }

    /**
     * Retrieves the back clip distance.
     * @return the current back clip distance, in meters
     */
    public double getBackDistance() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_BACK_DISTANCE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip1"));
        return ((ClipRetained)this.retained).getBackDistance();
    }

    /**
     * Set the Clip's application region to the specified bounds.
     * This is used when the application bounding leaf is set to null.
     * @param region the bounds that contains the Clip's new application
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setApplicationBounds(Bounds region) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip2"));

	if (isLive())
	    ((ClipRetained)this.retained).setApplicationBounds(region);
	else
	    ((ClipRetained)this.retained).initApplicationBounds(region);
    }

    /**
     * Retrieves the Clip node's application bounds.
     * @return this Clip's application bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getApplicationBounds() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip3"));

        return ((ClipRetained)this.retained).getApplicationBounds();
    }

    /**
     * Set the Clip's application region to the specified bounding leaf.
     * When set to a value other than null, this overrides the application
     * bounds object.
     * @param region the bounding leaf node used to specify the Clip
     * node's new application region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setApplicationBoundingLeaf(BoundingLeaf region) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip2"));

	if (isLive())
	    ((ClipRetained)this.retained).setApplicationBoundingLeaf(region);
	else
	    ((ClipRetained)this.retained).initApplicationBoundingLeaf(region);
    }

    /**
     * Retrieves the Clip node's application bounding leaf.
     * @return this Clip's application bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BoundingLeaf getApplicationBoundingLeaf() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Clip3"));

        return ((ClipRetained)this.retained).getApplicationBoundingLeaf();
    }

    /**
     * Creates the retained mode ClipRetained object that this
     * Clip component object will point to.
     */
    void createRetained() {
        this.retained = new ClipRetained();
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
        Clip c = new Clip();
        c.duplicateNode(this, forceDuplicate);
        return c;
    }

    /**
     * Callback used to allow a node to check if any scene graph objects
     * referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any object references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding object in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * object is returned depending on the value of the
     * <code>allowDanglingReferences</code> parameter passed in the
     * <code>cloneTree</code> call.
     * <p>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneTree method.
     *
     * @param referenceTable a NodeReferenceTableObject that contains the
     *  <code>getNewObjectReference</code> method needed to search for
     *  new object instances.
     * @see NodeReferenceTable
     * @see Node#cloneTree
     * @see DanglingReferenceException
     */
    public void updateNodeReferences(NodeReferenceTable referenceTable) {
	ClipRetained rt = (ClipRetained) retained;
        BoundingLeaf bl = rt.getApplicationBoundingLeaf();

        // check for applicationBoundingLeaf
        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.initApplicationBoundingLeaf((BoundingLeaf) o);
        }
    }


   /**
     * Copies all Clip information from
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

	ClipRetained attr = (ClipRetained) originalNode.retained;
	ClipRetained rt = (ClipRetained) retained;

	rt.initBackDistance(attr.getBackDistance());
	rt.initApplicationBounds(attr.getApplicationBounds());

	// correct value will set in updateNodeReferences
	rt.initApplicationBoundingLeaf(attr.getApplicationBoundingLeaf());
    }
}
