/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;
import java.util.Enumeration;

/**
 * The ModelClip leaf node defines a set of 6 arbitrary clipping
 * planes in the virtual universe.  The planes are specified in the
 * local coordinate system of this node, and may be individually
 * enabled or disabled.  This node also specifies a region of
 * influence in which this set of planes is active.
 *<p>
 * A ModelClip node also contains a list of Group nodes that specifies the
 * hierarchical scope of this ModelClip.  If the scope list is empty, then
 * the ModelClip node has universe scope: all nodes within the region of
 * influence are affected by this ModelClip node.  If the scope list is
 * non-empty, then only those Leaf nodes under the Group nodes in the
 * scope list are affected by this ModelClip node (subject to the
 * influencing bounds).
 * <p>
 * If the regions of influence of multiple ModelClip nodes overlap, the
 * Java 3D system will choose a single set of model clip planes for those
 * objects that lie in the intersection.  This is done in an
 * implementation-dependent manner, but in general, the ModelClip node that
 * is "closest" to the object is chosen.
 * <p>
 * The individual planes specify a half-space defined by the equation:
 * <ul>
 * Ax + By + Cz + D <= 0
 * </ul>
 * where A, B, C, D are the parameters that specify the plane.  The
 * parameters are passed in the x, y, z, and w fields, respectively,
 * of a Vector4d object.  The intersection of the set of half-spaces
 * corresponding to the enabled planes in this ModelClip node defines
 * a region in which points are accepted.  Points in this acceptance
 * region will be rendered (subject to view clipping and other
 * attributes).  Points that are not in the acceptance region will not
 * be rendered.
 *
 * @since Java 3D 1.2
 */

public class ModelClip extends Leaf {
    /**
     * Specifies that the ModelClip node allows read access to its influencing
     * bounds and bounding leaf at runtime.
     */
    public static final int ALLOW_INFLUENCING_BOUNDS_READ =
    CapabilityBits.MODEL_CLIP_ALLOW_INFLUENCING_BOUNDS_READ;

    /**
     * Specifies that the ModelClip node allows write access to its influencing
     * bounds and bounding leaf at runtime.
     */
    public static final int ALLOW_INFLUENCING_BOUNDS_WRITE =
    CapabilityBits.MODEL_CLIP_ALLOW_INFLUENCING_BOUNDS_WRITE;

    /**
     * Specifies that the ModelClip node allows read access to its planes
     * at runtime.
     */
    public static final int ALLOW_PLANE_READ =
    CapabilityBits.MODEL_CLIP_ALLOW_PLANE_READ;

    /**
     * Specifies that the ModelClip node allows write access to its planes
     * at runtime.
     */
    public static final int ALLOW_PLANE_WRITE =
    CapabilityBits.MODEL_CLIP_ALLOW_PLANE_WRITE;

    /**
     * Specifies that the ModelClip node allows read access to its enable
     * flags at runtime.
     */
    public static final int ALLOW_ENABLE_READ =
    CapabilityBits.MODEL_CLIP_ALLOW_ENABLE_READ;

    /**
     * Specifies that the ModelClip node allows write access to its enable
     * flags at runtime.
     */
    public static final int ALLOW_ENABLE_WRITE =
    CapabilityBits.MODEL_CLIP_ALLOW_ENABLE_WRITE;

    /**
     * Specifies that this ModelClip node allows read access to its scope
     * information at runtime.
     */
    public static final int ALLOW_SCOPE_READ =
    CapabilityBits.MODEL_CLIP_ALLOW_SCOPE_READ;

    /**
     * Specifies that this ModelClip node allows write access to its scope
     * information at runtime.
     */
    public static final int ALLOW_SCOPE_WRITE =
    CapabilityBits.MODEL_CLIP_ALLOW_SCOPE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_SCOPE_READ,
        ALLOW_ENABLE_READ,
        ALLOW_INFLUENCING_BOUNDS_READ,
        ALLOW_PLANE_READ
    };
                
    /**
     * Constructs a ModelClip node with default parameters.  The default
     * values are as follows:
     * <ul>
     * planes[0] : x <= 1 (1,0,0,-1)<br>
     * planes[1] : -x <= 1 (-1,0,0,-1)<br>
     * planes[2] : y <= 1 (0,1,0,-1)<br>
     * planes[3] : -y <= 1 (0,-1,0,-1)<br>
     * planes[4] : z <= 1 (0,0,1,-1)<br>
     * planes[5] : -z <= 1 (0,0,-1,-1)<br>
     * enables : all planes enabled<br>
     * scope : empty (universe scope)<br>
     * influencing bounds : null<br>
     * influencing bounding leaf : null<br>
     * </ul>
     */
    public ModelClip() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
        
    }


    /**
     * Constructs a ModelClip node using the specified planes.  The individual
     * planes are copied into this node.  All planes are enabled.
     * @param planes an array of 6 model clipping planes
     */
    public ModelClip(Vector4d[] planes) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ModelClipRetained)this.retained).initPlanes(planes);
    }


    /**
     * Constructs a ModelClip node using the specified planes and enable
     * flags.  The individual
     * planes and enable flags are copied into this node.
     * @param planes an array of 6 model clipping planes
     * @param enables an array of 6 enable flags
     */
    public ModelClip(Vector4d[] planes, boolean[] enables) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
        
	((ModelClipRetained)this.retained).initPlanes(planes);
	((ModelClipRetained)this.retained).initEnables(enables);
    }


    /**
     * Set the ModelClip node's influencing region to the specified bounds.
     * This is used when the influencing bounding leaf is set to null.
     * @param region the bounds that contains the new influencing
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setInfluencingBounds(Bounds region) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip0"));

	if (isLive())
	    ((ModelClipRetained)this.retained).setInfluencingBounds(region);
	else
	    ((ModelClipRetained)this.retained).initInfluencingBounds(region);
    }


    /**  
     * Retrieves the ModelClip node's influencing bounds.
     * @return this node's influencing bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public Bounds getInfluencingBounds() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip1"));

	return ((ModelClipRetained)this.retained).getInfluencingBounds();
    }


    /**
     * Set the ModelClip node's influencing region to the specified
     * bounding leaf.
     * When set to a value other than null, this overrides the influencing
     * bounds object.
     * @param region the bounding leaf node used to specify the
     * new influencing region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setInfluencingBoundingLeaf(BoundingLeaf region) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_INFLUENCING_BOUNDS_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip13"));

        if (isLive())
            ((ModelClipRetained)this.retained).setInfluencingBoundingLeaf(region);
        else
            ((ModelClipRetained)this.retained).initInfluencingBoundingLeaf(region);
    }


    /**  
     * Retrieves the ModelClip node's influencing bounding leaf.
     * @return this node's influencing bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public BoundingLeaf getInfluencingBoundingLeaf() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_INFLUENCING_BOUNDS_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip14"));

        return ((ModelClipRetained)this.retained).getInfluencingBoundingLeaf();
    }


    /**
     * Replaces the node at the specified index in this ModelClip node's
     * list of scopes with the specified Group node.
     * By default, ModelClip nodes are scoped only by their influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip7"));

	if (isLive())
	    ((ModelClipRetained)this.retained).setScope(scope, index);
	else
	    ((ModelClipRetained)this.retained).initScope(scope, index);
    }


    /**
     * Retrieves the Group node at the specified index from this ModelClip node's
     * list of scopes.
     * @param index the index of the Group node to be returned.
     * @return the Group node at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Group getScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip8"));

	return ((ModelClipRetained)this.retained).getScope(index);
    }


    /**
     * Inserts the specified Group node into this ModelClip node's
     * list of scopes at the specified index.
     * By default, ModelClip nodes are scoped only by their influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip9"));

	if (isLive())
	    ((ModelClipRetained)this.retained).insertScope(scope, index);
	else
	    ((ModelClipRetained)this.retained).initInsertScope(scope, index);
    }


    /**
     * Removes the node at the specified index from this ModelClip node's
     * list of scopes.  If this operation causes the list of scopes to
     * become empty, then this ModelClip will have universe scope: all nodes
     * within the region of influence will be affected by this ModelClip node.
     * @param index the index of the Group node to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception RestrictedAccessException if the group node at the
     * specified index is part of a compiled scene graph
     */
    public void removeScope(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip10"));

	if (isLive())
	    ((ModelClipRetained)this.retained).removeScope(index);
	else
	    ((ModelClipRetained)this.retained).initRemoveScope(index);
    }
  

    /**
     * Returns an enumeration of this ModelClip node's list of scopes.
     * @return an Enumeration object containing all nodes in this ModelClip node's
     * list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Enumeration getAllScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip11"));

	return (Enumeration) ((ModelClipRetained)this.retained).getAllScopes();
    }


    /**
     * Appends the specified Group node to this ModelClip node's list of scopes.
     * By default, ModelClip nodes are scoped only by their influencing
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip12"));

	if (isLive())
	    ((ModelClipRetained)this.retained).addScope(scope);
	else
	    ((ModelClipRetained)this.retained).initAddScope(scope);
    }

  
    /**
     * Returns the number of nodes in this ModelClip node's list of scopes.
     * If this number is 0, then the list of scopes is empty and this
     * ModelClip node has universe scope: all nodes within the region of
     * influence are affected by this ModelClip node.
     * @return the number of nodes in this ModelClip node's list of scopes.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int numScopes() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCOPE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip11"));

	return ((ModelClipRetained)this.retained).numScopes();
    }


    /**
     * Retrieves the index of the specified Group node in this
     * ModelClip node's list of scopes.
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip8"));
	return ((ModelClipRetained)this.retained).indexOfScope(scope);
    }


    /**
     * Removes the specified Group node from this ModelClip
     * node's list of scopes.  If the specified object is not in the
     * list, the list is not modified.  If this operation causes the
     * list of scopes to become empty, then this ModelClip
     * will have universe scope: all nodes within the region of
     * influence will be affected by this ModelClip node.
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip10"));
	if (isLive())
	    ((ModelClipRetained)this.retained).removeScope(scope);
	else
	    ((ModelClipRetained)this.retained).initRemoveScope(scope);
    }


    /**
     * Removes all Group nodes from this ModelClip node's
     * list of scopes.  The ModelClip node will then have
     * universe scope: all nodes within the region of influence will
     * be affected by this ModelClip node.
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
		throw new CapabilityNotSetException(J3dI18N.getString("ModelClip10"));
	if (isLive())
	    ((ModelClipRetained)this.retained).removeAllScopes();
	else
	    ((ModelClipRetained)this.retained).initRemoveAllScopes();
    }


    /**
     * Sets the clipping planes of this ModelClip node to the
     * specified planes.
     * The individual planes are copied into this node.
     * @param planes an array of 6 model clipping planes
     */
    public void setPlanes(Vector4d[] planes) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_PLANE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip2"));

        if (isLive())
            ((ModelClipRetained)this.retained).setPlanes(planes);
        else
            ((ModelClipRetained)this.retained).initPlanes(planes);
    }


    /**
     * Retrieves the clipping planes from this ModelClip node.
     * The individual planes are copied into the specified planes, which
     * must be allocated by the caller. The array must be large
     * enough to hold all of the vectors.
     * @param planes an array of 6 vectors that will receive the model
     * clipping planes from this node
     */
    public void getPlanes(Vector4d[] planes) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_PLANE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip3"));

	((ModelClipRetained)this.retained).getPlanes(planes);
    }


    /**
     * Sets the specified clipping plane of this ModelClip node.
     * The specified plane is copied into this node.
     * @param planeNum specifies which model clipping plane (0-5) is replaced
     * @param plane new model clipping plane
     */
    public void setPlane(int planeNum, Vector4d plane) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_PLANE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip2"));

        if (isLive())
            ((ModelClipRetained)this.retained).setPlane(planeNum, plane);
        else
            ((ModelClipRetained)this.retained).initPlane(planeNum, plane);

    }


    /**
     * Retrieves the specified clipping plane from this ModelClip node.
     * The plane is copied into the specified plane, which
     * must be allocated by the caller.
     * @param planeNum specifies which model clipping plane (0-5) is retrieved
     * @param plane a vector that will receive the specified model
     * clipping plane from this node
     */
    public void getPlane(int planeNum, Vector4d plane) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_PLANE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip3"));

	((ModelClipRetained)this.retained).getPlane(planeNum, plane);
    }


    /**
     * Sets the per-plane enable flags of this ModelClip node to the
     * specified values.
     * @param enables an array of 6 enable flags
     */
    public void setEnables(boolean[] enables) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ENABLE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip4"));

        if (isLive())
            ((ModelClipRetained)this.retained).setEnables(enables);
        else
            ((ModelClipRetained)this.retained).initEnables(enables);
    }


    /**
     * Retrieves the per-plane enable flags from this ModelClip node.
     * The enable flags are copied into the specified array.
     * The array must be large enough to hold all of the enables.
     * @param enables an array of 6 booleans that will receive the
     * enable flags from this node
     */
    public void getEnables(boolean[] enables) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ENABLE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip5"));

	((ModelClipRetained)this.retained).getEnables(enables);
    }


    /**
     * Sets the specified enable flag of this ModelClip node.
     * @param planeNum specifies which enable flag (0-5) is set
     * @param enable new enable flag
     */
    public void setEnable(int planeNum, boolean enable) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ENABLE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip4"));

        if (isLive())
            ((ModelClipRetained)this.retained).setEnable(planeNum, enable);
        else
            ((ModelClipRetained)this.retained).initEnable(planeNum, enable);
    }


    /**
     * Retrieves the specified enable flag from this ModelClip node.
     * @param planeNum specifies which enable flag (0-5) is retrieved
     * @return the specified enable flag
     */
    public boolean getEnable(int planeNum) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ENABLE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ModelClip5"));

	return ((ModelClipRetained)this.retained).getEnable(planeNum);
    }

    /**
     * Creates the retained mode ModelClipRetained object that 
     * this ModelClip node will point to.
     */
    void createRetained() {
	this.retained = new ModelClipRetained();
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
        ModelClip c = new ModelClip();
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
	ModelClipRetained rt = (ModelClipRetained) retained;
        BoundingLeaf bl = rt.getInfluencingBoundingLeaf();

        // check for influencingBoundingLeaf
        if (bl != null) {
            Object o = referenceTable.getNewObjectReference( bl);
            rt.initInfluencingBoundingLeaf((BoundingLeaf) o);
        }

	int num = rt.numScopes();
	for (int i=0; i < num; i++) {
	    rt.initScope((Group) referenceTable.
			 getNewObjectReference(rt.getScope(i)), i);
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

	ModelClipRetained attr = (ModelClipRetained) 
	                              originalNode.retained;
	ModelClipRetained rt = (ModelClipRetained) retained;
	
	Vector4d plane = new Vector4d();

	for (int i=5; i >=0; i--) {
	    attr.getPlane(i, plane);
	    rt.initPlane(i, plane);
	    rt.initEnable(i, attr.getEnable(i));
	}
	rt.initInfluencingBounds(attr.getInfluencingBounds());

	Enumeration elm = attr.getAllScopes();
	while (elm.hasMoreElements()) {
	  // this reference will set correctly in updateNodeReferences() callback
	    rt.initAddScope((Group) elm.nextElement());
	}

	// correct value will set in updateNodeReferences
	rt.initInfluencingBoundingLeaf(attr.getInfluencingBoundingLeaf());
    }
}
