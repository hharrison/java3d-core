/*
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
 */

package javax.media.j3d;

import java.util.Enumeration;


/**
 * The AlternateAppearance leaf node is used for overriding the
 * Appearance component of selected nodes.  It defines an Appearance
 * component object and a region of influence in which this
 * AlternateAppearance node is active.  An AlternateAppearance node
 * also contains a list of Group nodes that specifies the hierarchical
 * scope of this AlternateAppearance.  If the scope list is empty,
 * then the AlternateAppearance node has universe scope: all nodes
 * within the region of influence are affected by this
 * AlternateAppearance node.  If the scope list is non-empty, then
 * only those Leaf nodes under the Group nodes in the scope list are
 * affected by this AlternateAppearance node (subject to the
 * influencing bounds).
 *
 * <p>
 * An AlternateAppearance node affects Shape3D and Morph nodes by
 * overriding their appearance component with the appearance
 * component in this AlternateAppearance node.  Only those Shape3D and
 * Morph nodes that explicitly allow their appearance to be
 * overridden are affected.  The AlternateAppearance node has no
 * effect on Shape3D and Morph nodes that do not allow their
 * appearance to be overridden.
 *
 * <p>
 * If the regions of influence of multiple AlternateAppearance nodes
 * overlap, the Java 3D system will choose a single alternate
 * appearance for those objects that lie in the intersection.  This is
 * done in an implementation-dependent manner, but in general, the
 * AlternateAppearance node that is "closest" to the object is chosen.
 *
 * @since Java 3D 1.2
 */

public class AlternateAppearance extends Leaf {
    /**
     * Specifies that this AlternateAppearance node allows read access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int ALLOW_INFLUENCING_BOUNDS_READ =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_INFLUENCING_BOUNDS_READ;

    /**
     * Specifies that this AlternateAppearance node allows write access to its
     * influencing bounds and bounds leaf information.
     */
    public static final int ALLOW_INFLUENCING_BOUNDS_WRITE =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_INFLUENCING_BOUNDS_WRITE;

    /**
     * Specifies that this AlternateAppearance node allows read access to
     * its appearance information.
     */
    public static final int ALLOW_APPEARANCE_READ =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_APPEARANCE_READ;

    /**
     * Specifies that this AlternateAppearance node allows write access to
     * its appearance information.
     * information.
     */
    public static final int ALLOW_APPEARANCE_WRITE =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_APPEARANCE_WRITE;

    /**
     * Specifies that this AlternateAppearance node allows read access
     * to its scope information at runtime.
     */
    public static final int ALLOW_SCOPE_READ =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_SCOPE_READ;

    /**
     * Specifies that this AlternateAppearance node allows write access
     * to its scope information at runtime.
     */
    public static final int ALLOW_SCOPE_WRITE =
	CapabilityBits.ALTERNATE_APPEARANCE_ALLOW_SCOPE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_INFLUENCING_BOUNDS_READ,
        ALLOW_APPEARANCE_READ,
        ALLOW_SCOPE_READ
    };

    /**
     * Constructs an AlternateAppearance node with default
     * parameters.  The default values are as follows:
     *
     * <ul>
     * appearance : null<br>
     * scope : empty (universe scope)<br>
     * influencing bounds : null<br>
     * influencing bounding leaf : null<br>
     * </ul>
     */
    public AlternateAppearance() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

    }

    /**
     * Constructs an AlternateAppearance node with the specified appearance.
     * @param appearance the appearance that is used for those nodes affected
     * by this AlternateAppearance node.
     */
    public AlternateAppearance(Appearance appearance) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
	((AlternateAppearanceRetained)retained).initAppearance(appearance);
    }

    /**
     * Creates the retained mode AlternateAppearanceRetained object that this
     * Alternate Appearance component object will point to.
     */
    void createRetained() {
	this.retained = new AlternateAppearanceRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the appearance of this AlternateAppearance node.
     * This appearance overrides the appearance in those Shape3D and
     * Morph nodes affected by this AlternateAppearance node.
     * @param appearance the new appearance.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAppearance(Appearance appearance) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPEARANCE_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance0"));

	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).setAppearance(appearance);
	else
	    ((AlternateAppearanceRetained)this.retained).initAppearance(appearance);
    }


    /**
     * Retrieves the appearance from this AlternateAppearance node.
     * @return the current appearance.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Appearance getAppearance() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPEARANCE_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance2"));

	return ((AlternateAppearanceRetained)this.retained).getAppearance();

    }

    /**
     * Sets the AlternateAppearance's influencing region to the specified
     * bounds.
     * This is used when the influencing bounding leaf is set to null.
     * @param region the bounds that contains the AlternateAppearance's
     * new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBounds(Bounds region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance3"));


	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).setInfluencingBounds(region);
	else
	    ((AlternateAppearanceRetained)this.retained).initInfluencingBounds(region);
    }

    /**
     * Retrieves the AlternateAppearance node's influencing bounds.
     * @return this AlternateAppearance's influencing bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Bounds getInfluencingBounds() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance4"));


	return ((AlternateAppearanceRetained)this.retained).getInfluencingBounds();
    }


    /**
     * Sets the AlternateAppearance's influencing region to the specified
     * bounding leaf.
     * When set to a value other than null, this overrides the influencing
     * bounds object.
     * @param region the bounding leaf node used to specify the
     * AlternateAppearance node's new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInfluencingBoundingLeaf(BoundingLeaf region) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance3"));


	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).setInfluencingBoundingLeaf(region);
	else
	    ((AlternateAppearanceRetained)this.retained).initInfluencingBoundingLeaf(region);
    }


    /**
     * Retrieves the AlternateAppearance node's influencing bounding leaf.
     * @return this AlternateAppearance's influencing bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BoundingLeaf getInfluencingBoundingLeaf() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance4"));


	return ((AlternateAppearanceRetained)this.retained).getInfluencingBoundingLeaf();
    }


    /**
     * Replaces the node at the specified index in this
     * AlternateAppearance node's
     * list of scopes with the specified Group node.
     * By default, AlternateAppearance nodes are scoped only by their
     * influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance7"));



	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).setScope(scope, index);
	else
	    ((AlternateAppearanceRetained)this.retained).initScope(scope, index);
    }


    /**
     * Retrieves the Group node at the specified index from
     * this AlternateAppearance node's list of scopes.
     * @param index the index of the Group node to be returned.
     * @return the Group node at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Group getScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance8"));


	return ((AlternateAppearanceRetained)this.retained).getScope(index);
    }


    /**
     * Inserts the specified Group node into this AlternateAppearance node's
     * list of scopes at the specified index.
     * By default, AlternateAppearance nodes are scoped only by their
     * influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance9"));


	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).insertScope(scope, index);
	else
	    ((AlternateAppearanceRetained)this.retained).initInsertScope(scope, index);
    }


    /**
     * Removes the node at the specified index from this AlternateAppearance
     * node's
     * list of scopes.  If this operation causes the list of scopes to
     * become empty, then this AlternateAppearance will have universe scope:
     * all nodes
     * within the region of influence will be affected by this
     * AlternateAppearance node.
     * @param index the index of the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the group node at the
     * specified index is part of a compiled scene graph
     */
    public void removeScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance10"));


	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).removeScope(index);
	else
	    ((AlternateAppearanceRetained)this.retained).initRemoveScope(index);
    }


    /**
     * Returns an enumeration of this AlternateAppearance node's list
     * of scopes.
     * @return an Enumeration object containing all nodes in this
     * AlternateAppearance node's list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Enumeration getAllScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance11"));


	return (Enumeration) ((AlternateAppearanceRetained)this.retained).getAllScopes();
    }


    /**
     * Appends the specified Group node to this AlternateAppearance node's
     * list of scopes.
     * By default, AlternateAppearance nodes are scoped only by their
     * influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance12"));


	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).addScope(scope);
	else
	    ((AlternateAppearanceRetained)this.retained).initAddScope(scope);
    }


    /**
     * Returns the number of nodes in this AlternateAppearance node's list
     * of scopes.
     * If this number is 0, then the list of scopes is empty and this
     * AlternateAppearance node has universe scope: all nodes within the
     * region of
     * influence are affected by this AlternateAppearance node.
     * @return the number of nodes in this AlternateAppearance node's list
     * of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int numScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance11"));


	return ((AlternateAppearanceRetained)this.retained).numScopes();
    }


    /**
     * Retrieves the index of the specified Group node in this
     * AlternateAppearance node's list of scopes.
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance8"));

	return ((AlternateAppearanceRetained)this.retained).indexOfScope(scope);
    }


    /**
     * Removes the specified Group node from this AlternateAppearance
     * node's list of scopes.  If the specified object is not in the
     * list, the list is not modified.  If this operation causes the
     * list of scopes to become empty, then this AlternateAppearance
     * will have universe scope: all nodes within the region of
     * influence will be affected by this AlternateAppearance node.
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance10"));

	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).removeScope(scope);
	else
	    ((AlternateAppearanceRetained)this.retained).initRemoveScope(scope);
    }


    /**
     * Removes all Group nodes from this AlternateAppearance node's
     * list of scopes.  The AlternateAppearance node will then have
     * universe scope: all nodes within the region of influence will
     * be affected by this AlternateAppearance node.
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
		throw new CapabilityNotSetException(J3dI18N.getString("AlternateAppearance10"));
	if (isLive())
	    ((AlternateAppearanceRetained)this.retained).removeAllScopes();
	else
	    ((AlternateAppearanceRetained)this.retained).initRemoveAllScopes();
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
	super.duplicateAttributes(originalNode, forceDuplicate);

	AlternateAppearanceRetained attr = (AlternateAppearanceRetained)
	                                   originalNode.retained;
	AlternateAppearanceRetained rt = (AlternateAppearanceRetained) retained;

	rt.initAppearance((Appearance) getNodeComponent(
						       attr.getAppearance(),
						       forceDuplicate,
						       originalNode.nodeHashtable));

	rt.initInfluencingBounds(attr.getInfluencingBounds());

	Enumeration elm = attr.getAllScopes();
	while (elm.hasMoreElements()) {
	  // this reference will set correctly in updateNodeReferences() callback
	    rt.initAddScope((Group) elm.nextElement());
	}

	// correct value will set in updateNodeReferences
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

	AlternateAppearanceRetained rt = (AlternateAppearanceRetained)
                                   	  retained;

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
        AlternateAppearance app = new AlternateAppearance();
        app.duplicateNode(this, forceDuplicate);
        return app;
    }
}

