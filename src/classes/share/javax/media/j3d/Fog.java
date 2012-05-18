/*
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
 */

package javax.media.j3d;

import java.util.Enumeration;

import javax.vecmath.Color3f;

/**
 * The Fog leaf node defines a set of fog parameters common to all
 * types of fog.  These parameters include the fog color and a region
 * of influence in which this Fog node is active.
 * A Fog node also contains a list of Group nodes that specifies the
 * hierarchical scope of this Fog.  If the scope list is empty, then
 * the Fog node has universe scope: all nodes within the region of
 * influence are affected by this Fog node.  If the scope list is
 * non-empty, then only those Leaf nodes under the Group nodes in the
 * scope list are affected by this Fog node (subject to the
 * influencing bounds).
 * <p>
 * If the regions of influence of multiple Fog nodes overlap, the
 * Java 3D system will choose a single set of fog parameters for those
 * objects that lie in the intersection.  This is done in an
 * implementation-dependent manner, but in general, the Fog node that
 * is "closest" to the object is chosen.
 */

public abstract class Fog extends Leaf {
    /**
     * Specifies that this Fog node allows read access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int
    ALLOW_INFLUENCING_BOUNDS_READ = CapabilityBits.FOG_ALLOW_INFLUENCING_BOUNDS_READ;

    /**
     * Specifies that this Fog node allows write access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int
    ALLOW_INFLUENCING_BOUNDS_WRITE = CapabilityBits.FOG_ALLOW_INFLUENCING_BOUNDS_WRITE;

    /**
     * Specifies that this Fog node allows read access to its color
     * information.
     */
    public static final int
    ALLOW_COLOR_READ = CapabilityBits.FOG_ALLOW_COLOR_READ;

    /**
     * Specifies that this Fog node allows write access to its color
     * information.
     */
    public static final int
    ALLOW_COLOR_WRITE = CapabilityBits.FOG_ALLOW_COLOR_WRITE;

    /**
     * Specifies that this Fog node allows read access to its scope
     * information at runtime.
     */
    public static final int
    ALLOW_SCOPE_READ = CapabilityBits.FOG_ALLOW_SCOPE_READ;

    /**
     * Specifies that this Fog node allows write access to its scope
     * information at runtime.
     */
    public static final int
    ALLOW_SCOPE_WRITE = CapabilityBits.FOG_ALLOW_SCOPE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_INFLUENCING_BOUNDS_READ,
        ALLOW_COLOR_READ,
        ALLOW_SCOPE_READ
    };

    /**
     * Constructs a Fog node with default parameters.  The default
     * values are as follows:
     * <ul>
     * color : black (0,0,0)<br>
     * scope : empty (universe scope)<br>
     * influencing bounds : null<br>
     * influencing bounding leaf : null<br>
     * </ul>
     */
    public Fog() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a Fog node with the specified fog color.
     * @param color the fog color
     */
    public Fog(Color3f color) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((FogRetained)this.retained).initColor(color);
    }

    /**
     * Constructs a Fog node with the specified fog color.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     */
    public Fog(float r, float g, float b) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((FogRetained)this.retained).initColor(r, g, b);
    }

    /**
     * Sets the fog color to the specified color.
     * @param color the new fog color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColor(Color3f color) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLOR_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog0"));

	if (isLive())
	    ((FogRetained)this.retained).setColor(color);
	else
	    ((FogRetained)this.retained).initColor(color);
    }

    /**
     * Sets the fog color to the specified color.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColor(float r, float g, float b) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLOR_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog0"));

	if (isLive())
	    ((FogRetained)this.retained).setColor(r, g, b);
	else
	    ((FogRetained)this.retained).initColor(r, g, b);
    }

    /**
     * Retrieves the fog color.
     * @param color the vector that will receive the current fog color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getColor(Color3f color) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_COLOR_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog2"));

	((FogRetained)this.retained).getColor(color);
    }

    /**
     * Sets the Fog's influencing region to the specified bounds.
     * This is used when the influencing bounding leaf is set to null.
     * @param region the bounds that contains the Fog's new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBounds(Bounds region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog3"));

	if (isLive())
	    ((FogRetained)this.retained).setInfluencingBounds(region);
	else
	    ((FogRetained)this.retained).initInfluencingBounds(region);

    }

    /**
     * Retrieves the Fog node's influencing bounds.
     * @return this Fog's influencing bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getInfluencingBounds() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog4"));

	return ((FogRetained)this.retained).getInfluencingBounds();
    }

    /**
     * Sets the Fog's influencing region to the specified bounding leaf.
     * When set to a value other than null, this overrides the influencing
     * bounds object.
     * @param region the bounding leaf node used to specify the Fog
     * node's new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBoundingLeaf(BoundingLeaf region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog3"));

	if (isLive())
	    ((FogRetained)this.retained).setInfluencingBoundingLeaf(region);
	else
	    ((FogRetained)this.retained).initInfluencingBoundingLeaf(region);
    }

    /**
     * Retrieves the Fog node's influencing bounding leaf.
     * @return this Fog's influencing bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BoundingLeaf getInfluencingBoundingLeaf() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("Fog4"));

	return ((FogRetained)this.retained).getInfluencingBoundingLeaf();
    }


    /**
     * Replaces the node at the specified index in this Fog node's
     * list of scopes with the specified Group node.
     * By default, Fog nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be stored at the specified index.
     * @param index the index of the Group node to be replaced.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void setScope(Group scope, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog7"));


	if (isLive())
	    ((FogRetained)this.retained).setScope(scope, index);
	else
	    ((FogRetained)this.retained).initScope(scope, index);
    }


    /**
     * Retrieves the Group node at the specified index from this Fog node's
     * list of scopes.
     * @param index the index of the Group node to be returned.
     * @return the Group node at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Group getScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog8"));

	return ((FogRetained)this.retained).getScope(index);
    }


    /**
     * Inserts the specified Group node into this Fog node's
     * list of scopes at the specified index.
     * By default, Fog nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be inserted at the specified index.
     * @param index the index at which the Group node is inserted.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void insertScope(Group scope, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog9"));

	if (isLive())
	    ((FogRetained)this.retained).insertScope(scope, index);
	else
	    ((FogRetained)this.retained).initInsertScope(scope, index);
    }


    /**
     * Removes the node at the specified index from this Fog node's
     * list of scopes.  If this operation causes the list of scopes to
     * become empty, then this Fog will have universe scope: all nodes
     * within the region of influence will be affected by this Fog node.
     * @param index the index of the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the group node at the
     * specified index is part of a compiled scene graph
     */
    public void removeScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog10"));

	if (isLive())
	    ((FogRetained)this.retained).removeScope(index);
	else
	    ((FogRetained)this.retained).initRemoveScope(index);
    }


    /**
     * Returns an enumeration of this Fog node's list of scopes.
     * @return an Enumeration object containing all nodes in this Fog node's
     * list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Enumeration getAllScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog11"));

	return (Enumeration) ((FogRetained)this.retained).getAllScopes();
    }


    /**
     * Appends the specified Group node to this Fog node's list of scopes.
     * By default, Fog nodes are scoped only by their influencing
     * bounds.  This allows them to be further scoped by a list of
     * nodes in the hierarchy.
     * @param scope the Group node to be appended.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     */
    public void addScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog12"));

	if (isLive())
	    ((FogRetained)this.retained).addScope(scope);
	else
	    ((FogRetained)this.retained).initAddScope(scope);
    }


    /**
     * Returns the number of nodes in this Fog node's list of scopes.
     * If this number is 0, then the list of scopes is empty and this
     * Fog node has universe scope: all nodes within the region of
     * influence are affected by this Fog node.
     * @return the number of nodes in this Fog node's list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int numScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog11"));

	return ((FogRetained)this.retained).numScopes();
    }


    /**
     * Retrieves the index of the specified Group node in this
     * Fog node's list of scopes.
     *
     * @param scope the Group node to be looked up.
     * @return the index of the specified Group node;
     * returns -1 if the object is not in the list.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int indexOfScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog8"));
	return ((FogRetained)this.retained).indexOfScope(scope);
    }


    /**
     * Removes the specified Group node from this Fog
     * node's list of scopes.  If the specified object is not in the
     * list, the list is not modified.  If this operation causes the
     * list of scopes to become empty, then this Fog
     * will have universe scope: all nodes within the region of
     * influence will be affected by this Fog node.
     *
     * @param scope the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the specified group node
     * is part of a compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeScope(Group scope) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog10"));

	if (isLive())
	  ((FogRetained)this.retained).removeScope(scope);
	else
	  ((FogRetained)this.retained).initRemoveScope(scope);
    }


    /**
     * Removes all Group nodes from this Fog node's
     * list of scopes.  The Fog node will then have
     * universe scope: all nodes within the region of influence will
     * be affected by this Fog node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if any group node in this
     * node's list of scopes is part of a compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeAllScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Fog10"));

	if (isLive())
	  ((FogRetained)this.retained).removeAllScopes();
	else
	  ((FogRetained)this.retained).initRemoveAllScopes();
    }


   /**
     * Copies all Fog information from
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

	FogRetained attr = (FogRetained) originalNode.retained;
	FogRetained rt = (FogRetained) retained;

	Color3f c = new Color3f();
	attr.getColor(c);
	rt.initColor(c);
	rt.initInfluencingBounds(attr.getInfluencingBounds());

	Enumeration elm = attr.getAllScopes();
	while (elm.hasMoreElements()) {
	  // this reference will set correctly in updateNodeReferences() callback
	    rt.initAddScope((Group) elm.nextElement());
	}

	  // this reference will set correctly in updateNodeReferences() callback
	rt.initInfluencingBoundingLeaf(attr.getInfluencingBoundingLeaf());
    }

    /**
     * Callback used to allow a node to check if any nodes referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any node references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding Node in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * node is returned depending on the value of the
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

	FogRetained rt = (FogRetained) retained;
        BoundingLeaf bl = rt.getInfluencingBoundingLeaf();

        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.initInfluencingBoundingLeaf((BoundingLeaf) o);
        }


	int num = rt.numScopes();
	for (int i=0; i < num; i++) {
	    rt.initScope((Group) referenceTable.
			 getNewObjectReference(rt.getScope(i)), i);
	}
    }

}
