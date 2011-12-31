/*
 * $RCSfile$
 *
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.Enumeration;

/**
 * The ViewSpecificGroup node is a Group whose descendants are
 * rendered only on a specified set of views.  It
 * contains a list of views on which its descendants are
 * rendered.  Methods are provided to add, remove, and enumerate the
 * list of views.  The list of views is initially empty, meaning that
 * the descendants of this group will not be rendered on any view.  At
 * least one view must be added to the list of views for rendering to
 * occur.
 *
 * <p>
 * All nodes except ViewPlatform may appear as a descendant of
 * ViewSpecificGroup, including another ViewSpecificGroup.  If a
 * ViewSpecificGroup is a descendant of a ViewSpecificGroup, the
 * effect is to intersect the view sets of the ViewSpecificGroup nodes
 * in the hierarchy; each ViewSpecificGroup encountered when
 * traversing the scene graph further restricts the set of views on
 * which its descendants are rendered.  More formally, descendant
 * nodes of ViewSpecificGroups are rendered in (or apply to) only
 * those views that are contained in the set of views of every
 * ViewSpecificGroup in the scene graph path from the Locale to the
 * Node.
 *
 * <p>
 * Behavior
 * nodes may appear under a ViewSpecificGroup, but are not affected by
 * it--the Behavior scheduler is per-universe rather than per-View.
 * BoundingLeaf nodes are similarly unaffected by being under a
 * ViewSpecificGroup.  A BoundingLeaf under a ViewSpecificGroup
 * provides a valid bounds for any node that refers to it,
 * irrespective of the view.
 *
 * <p>
 * The rest of the leaf nodes either: A) are only rendered within the
 * specified view(s), for example, Shape3D, Morph, and Sound; or B)
 * only affect other objects when they are rendered in the specified
 * view(s), for example, AlternateAppearance, Clip, ModelClip, Fog,
 * Light, Soundscape, Background.
 *
 * @since Java 3D 1.3
 */

public class ViewSpecificGroup extends Group {
    /**
     * Specifies that this ViewSpecificGroup node allows reading its
     * view information at runtime.
     */
    public static final int
    ALLOW_VIEW_READ = CapabilityBits.VIEW_SPECIFIC_GROUP_ALLOW_VIEW_READ;

    /**
     * Specifies that this ViewSpecificGroup node allows writing its
     * view information at runtime.
     */
    public static final int
    ALLOW_VIEW_WRITE = CapabilityBits.VIEW_SPECIFIC_GROUP_ALLOW_VIEW_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_VIEW_READ
    };

    /**
     * Constructs and initializes a new ViewSpecificGroup node object.
     */
    public ViewSpecificGroup() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }


    /**
     * Creates the retained mode ViewSpecificGroupRetained object that this
     * ViewSpecificGroup component object will point to.
     */
    void createRetained() {
	this.retained = new ViewSpecificGroupRetained();
	this.retained.setSource(this);
    }


    /**
     * Replaces the view at the specified index in this node's
     * list of views with the specified View object.
     *
     * @param view the View object to be stored at the specified index.
     * @param index the index of the View object to be replaced.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setView(View view, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));

	((ViewSpecificGroupRetained)this.retained).setView(view, index);
    }


    /**
     * Retrieves the View object at the specified index from this node's
     * list of views.
     *
     * @param index the index of the View object to be returned.
     * @return the View object at the specified index.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public View getView(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup2"));

	return ((ViewSpecificGroupRetained)this.retained).getView(index);
    }


    /**
     * Inserts the specified View object into this node's
     * list of views at the specified index.
     *
     * @param view the View object to be inserted at the specified index.
     * @param index the index at which the View object is inserted.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void insertView(View view, int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));

	((ViewSpecificGroupRetained)this.retained).insertView(view, index);
    }


    /**
     * Removes the View object at the specified index from this node's
     * list of views.
     * If this operation causes the list of views to become empty,
     * then the descendants of this ViewSpecificGroup node will not be
     * rendered.
     *
     * @param index the index of the View object to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void removeView(int index) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));
	((ViewSpecificGroupRetained)this.retained).removeView(index);

    }


    /**
     * Returns an enumeration of this ViewSpecificGroup node's list
     * of views.
     *
     * @return an Enumeration object containing all the views.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Enumeration getAllViews() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup2"));

	return ((ViewSpecificGroupRetained)this.retained).getAllViews();
    }


    /**
     * Appends the specified View object to this node's list of views.
     *
     * @param view the View object to be appended.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void addView(View view) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));

	((ViewSpecificGroupRetained)this.retained).addView(view);
    }


    /**
     * Returns the number of View objects in this node's list of views.
     * If this number is 0, then the list of views is empty and
     * the descendants of this ViewSpecificGroup node will not be
     * rendered.
     *
     * @return the number of views in this node's list of views.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int numViews() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup2"));

	return ((ViewSpecificGroupRetained)this.retained).numViews();
    }


    /**
     * Retrieves the index of the specified View object in this
     * node's list of views.
     *
     * @param view the View object to be looked up.
     * @return the index of the specified View object;
     * returns -1 if the object is not in the list.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int indexOfView(View view) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup2"));

	return ((ViewSpecificGroupRetained)this.retained).indexOfView(view);
    }


    /**
     * Removes the specified View object from this
     * node's list of views.  If the specified object is not in the
     * list, the list is not modified.
     * If this operation causes the list of views to become empty,
     * then the descendants of this ViewSpecificGroup node will not be
     * rendered.
     *
     * @param view the View object to be removed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeView(View view) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));

	((ViewSpecificGroupRetained)this.retained).removeView(view);
    }


    /**
     * Removes all View objects from this node's
     * list of views.
     * Since this method clears the list of views, the descendants of
     * this ViewSpecificGroup node will not be rendered.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void removeAllViews() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_VIEW_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ViewSpecificGroup1"));

	((ViewSpecificGroupRetained)this.retained).removeAllViews();
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
	ViewSpecificGroup vsg = new ViewSpecificGroup();
	vsg.duplicateNode(this, forceDuplicate);
	return vsg;
    }


    /**
     * Copies all ViewSpecificGroup information from
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

	// XXXX: implement this?
        super.duplicateAttributes(originalNode, forceDuplicate);

        ViewSpecificGroupRetained attr = (ViewSpecificGroupRetained) originalNode.retained;
	ViewSpecificGroupRetained rt = (ViewSpecificGroupRetained) retained;

	for (Enumeration e = attr.getAllViews(); e.hasMoreElements(); ) {
	    rt.addView((View)e.nextElement());
	}
    }

}
