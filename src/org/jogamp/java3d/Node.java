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

package org.jogamp.java3d;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The Node class provides an abstract class for all Group and Leaf Nodes.
 * It provides a common framework for constructing a Java 3D scene graph,
 * specifically bounding volumes.
 *
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a>.
 *
 * <p>
 * NOTE: Applications should <i>not</i> extend this class directly.
 */
public abstract class Node extends SceneGraphObject {

    /**
     * Specifies that this Node will be reported in the pick
     * SceneGraphPath if a pick occurs.  This capability is only
     * specifiable for Group nodes; it is ignored for leaf nodes.
     * The default for Group nodes is false.  All interior nodes not
     * needed for uniqueness in a SceneGraphPath that don't have
     * ENABLE_PICK_REPORTING set to true will not be reported in the
     * SceneGraphPath.
     * @see SceneGraphPath
     */
    public static final int
    ENABLE_PICK_REPORTING = CapabilityBits.NODE_ENABLE_PICK_REPORTING;

    /**
     * Specifies that this Node will be reported in the collision
     * SceneGraphPath if a collision occurs.  This capability is only
     * specifiable for Group nodes; it is ignored for leaf nodes.
     * The default for Group nodes is false.  All interior nodes not
     * needed for uniqueness in a SceneGraphPath that don't have
     * ENABLE_COLLISION_REPORTING set to true will not be reported
     * in the SceneGraphPath.
     * @see SceneGraphPath
     */
    public static final int
    ENABLE_COLLISION_REPORTING = CapabilityBits.NODE_ENABLE_COLLISION_REPORTING;

    /**
     * Specifies that this Node allows read access to its bounds
     * information.
     */
    public static final int
    ALLOW_BOUNDS_READ = CapabilityBits.NODE_ALLOW_BOUNDS_READ;

    /**
     * Specifies that this Node allows write access to its bounds
     * information.
     */
    public static final int
    ALLOW_BOUNDS_WRITE = CapabilityBits.NODE_ALLOW_BOUNDS_WRITE;

    /**
     * Specifies that this Node allows reading its pickability state.
     */
    public static final int
    ALLOW_PICKABLE_READ = CapabilityBits.NODE_ALLOW_PICKABLE_READ;

    /**
     * Specifies that this Node allows write access its pickability state.
     */
    public static final int
    ALLOW_PICKABLE_WRITE = CapabilityBits.NODE_ALLOW_PICKABLE_WRITE;

    /**
     * Specifies that this Node allows reading its collidability state.
     */
    public static final int
    ALLOW_COLLIDABLE_READ = CapabilityBits.NODE_ALLOW_COLLIDABLE_READ;

    /**
     * Specifies that this Node allows write access its collidability state.
     */
    public static final int
    ALLOW_COLLIDABLE_WRITE = CapabilityBits.NODE_ALLOW_COLLIDABLE_WRITE;

    /**
     * Specifies that this Node allows read access to its bounds
     * auto compute information.
     */
    public static final int
    ALLOW_AUTO_COMPUTE_BOUNDS_READ = CapabilityBits.NODE_ALLOW_AUTO_COMPUTE_BOUNDS_READ;

    /**
     * Specifies that this Node allows write access to its bounds
     * auto compute information.
     */
    public static final int
    ALLOW_AUTO_COMPUTE_BOUNDS_WRITE = CapabilityBits.NODE_ALLOW_AUTO_COMPUTE_BOUNDS_WRITE;

    /**
     * Specifies that this Node allows read access to its local
     * coordinates to virtual world (Vworld) coordinates transform.
     */
    public static final int
    ALLOW_LOCAL_TO_VWORLD_READ = CapabilityBits.NODE_ALLOW_LOCAL_TO_VWORLD_READ;

    /**
     * Specifies that this Node allows read access to its parent Group node.
     *
     * @since Java 3D 1.4
     */
    public static final int
	ALLOW_PARENT_READ = CapabilityBits.NODE_ALLOW_PARENT_READ;

    /**
     * Specifies that this Node allows read access to its Locale.
     *
     * @since Java 3D 1.4
     */
    public static final int
        ALLOW_LOCALE_READ = CapabilityBits.NODE_ALLOW_LOCALE_READ;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_BOUNDS_READ,
        ALLOW_PICKABLE_READ,
        ALLOW_COLLIDABLE_READ,
        ALLOW_AUTO_COMPUTE_BOUNDS_READ,
        ALLOW_LOCAL_TO_VWORLD_READ,
        ALLOW_PARENT_READ,
        ALLOW_LOCALE_READ
    };

    // for checking for cycles
    private boolean visited = false;


    /**
     * Constructs a Node object with default parameters.  The default
     * values are as follows:
     * <ul>
     * pickable : true<br>
     * collidable : true<br>
     * bounds auto compute : true<br>
     * bounds : N/A (automatically computed)<br>
     * </ul>
     */
    public Node() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**

     * @return the parent of this node, or null if this node has no parent
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Node getParent() {
	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_PARENT_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Node0"));
	    }
	}

	NodeRetained nr = ((NodeRetained)this.retained).getParent();
	return (nr == null ? null :  (Node) nr.getSource());
    }

    /**
     * Sets the geometric bounds of a node.
     * @param bounds the bounding object for a node
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setBounds(Bounds bounds) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Node1"));

	((NodeRetained)this.retained).setBounds(bounds);
    }

    /**
     * Returns the bounding object of a node.
     * @return the node's bounding object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     */
    public Bounds getBounds() {

	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_BOUNDS_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("Node2"));
	    }
	}
	else {
	    // this will throw a SceneGraphCycleException if there is
	    // a cycle
	    checkForCycle();
	}

	return ((NodeRetained)this.retained).getBounds();
    }

   /**
     * Returns the collidable value; this value determines whether this node
     * and it's children, if a group node, can be considered for collision
     * purposes; if it is set to false, then neither this node nor any
     * children nodes will be traversed for collision purposes; the default
     * value is true.   The collidable setting is the way that an
     * application can perform collision culling.
     *  @return the present collidable value for this node
     */
    public boolean getCollidable() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_COLLIDABLE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Node16"));

        return ((NodeRetained)retained).getCollidable();
    }

    /**
     * Sets the collidable value; determines whether this node and any of its
     * children, if a group node, can be considered for collision purposes.
     * @param collidable  the new collidable value for this node
     */
    public void setCollidable( boolean collidable ) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_COLLIDABLE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Node4"));

        ((NodeRetained)retained).setCollidable(collidable);
    }

    /**
     * Turns the automatic calcuation of geometric bounds of a node on/off.
     * @param autoCompute indicates if the node's bounding object is
     * automatically computed.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setBoundsAutoCompute(boolean autoCompute) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_AUTO_COMPUTE_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Node5"));

	((NodeRetained)this.retained).setBoundsAutoCompute(autoCompute);
    }

    /**
     * Gets the value indicating if the automatic calcuation of geometric bounds of a node is on/off.
     * @return the node's auto compute flag for the geometric bounding object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getBoundsAutoCompute() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_AUTO_COMPUTE_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Node6"));

	return ((NodeRetained)this.retained).getBoundsAutoCompute();
    }

    /**
     * Retrieves the local coordinates to virtual world coordinates
     * transform for this node in the scene graph.  This is the composite
     * of all transforms in the scene graph from the root down to
     * <code>this</code> node.  It is only valid
     * for nodes that are part of a live scene graph.
     * If the node is not part of a live scene graph then the coordinates are
     * calculated as if the graph was attached at the origin of a locale.
     * @param t the object that will receive the local coordinates to
     * Vworld coordinates transform.
     * @exception RestrictedAccessException if the node is compiled but not
     * part of a live scene graph
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this node is part of live or compiled scene graph
     * @exception IllegalSharingException if the node is a descendant
     * of a SharedGroup node.
     */
    public void getLocalToVworld(Transform3D t) {
        if (isLiveOrCompiled()) {
            if(!this.getCapability(ALLOW_LOCAL_TO_VWORLD_READ))
                    throw new CapabilityNotSetException(J3dI18N.getString("Node8"));
        }

	if (!isLive()) {
            // TODO Support compiled graphs
            if (isCompiled())
                throw new RestrictedAccessException(J3dI18N.getString("Node7"));

            // In 1.4 we support getLocalToVworld for non live nodes
            ((NodeRetained)this.retained).computeNonLiveLocalToVworld(t, this);
	    //throw new RestrictedAccessException(J3dI18N.getString("Node7"));
        } else {
            ((NodeRetained)this.retained).getLocalToVworld(t);
        }
    }


    /**
     * Retrieves the local coordinates to virtual world coordinates
     * transform for the particular path in the scene graph ending with
     * this node.  This is the composite
     * of all transforms in the scene graph from the root down to
     * <code>this</code> node via the specified Link nodes.  It is
     * only valid for nodes that are part of a live scene graph.
     * @param path the specific path from the node to the Locale
     * @param t the object that will receive the local coordinates to
     * Vworld coordinates transform.
     * @exception RestrictedAccessException if the node is <em>not</em>
     * part of a live scene graph
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this node is part of live scene graph
     * @exception IllegalArgumentException if the specified path does
     * not contain a valid Locale, or if the last node in the path is
     * different from this node
     * @exception IllegalSharingException if the node is not a descendant
     * of a SharedGroup node.
     */
    public void getLocalToVworld(SceneGraphPath path, Transform3D t) {
	if (!isLive()) {
	    throw new RestrictedAccessException(J3dI18N.getString("Node7"));
        }

        if(!this.getCapability(ALLOW_LOCAL_TO_VWORLD_READ))
            throw new CapabilityNotSetException(J3dI18N.getString("Node8"));

        ((NodeRetained)this.retained).getLocalToVworld(path,t);

    }

    /**
     * Retrieves the locale to which this node is attached. If the
     * node is not part of a live scene graph, null is returned.
     *
     * @return the locale to which this node is attached.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this node is part of live scene graph
     * @exception IllegalSharingException if the node is a descendant
     * of a SharedGroup node.
     *
     * @since Java 3D 1.4
     */
    public Locale getLocale() {
	if (!isLive()) {
	    return null;
	}

	if(!this.getCapability(ALLOW_LOCALE_READ)) {
	    throw new CapabilityNotSetException(J3dI18N.getString("Node17"));
	}

	return ((NodeRetained)this.retained).getLocale();
    }

    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code>
     * and then <code>cloneTree</code>
     * is called for each child node.  For Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     * @return a reference to the cloned sub-graph.
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneTree() {
	return cloneTree(new NodeReferenceTable(), false, false);
    }

    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code>
     * and then <code>cloneTree</code> is called for each child node.
     * For Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     * @return a reference to the cloned scene graph.
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneTree(boolean forceDuplicate) {
	return cloneTree(new NodeReferenceTable(), forceDuplicate, false);
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
     *  flag to be ignored.  When <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     *
     * @param allowDanglingReferences when set to <code>true</code> allows
     *  the <code>cloneTree</code>
     *  method to complete even whan a dangling reference is discovered. When
     *  this parameter is <code>false</code> a
     *  <code>DanglingReferenceException</code> is generated as
     *  soon as cloneTree detects this situation.
     *
     * @return a reference to the cloned scene graph.
     *
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation and the
     *  <code>allowDanglingReference</code> parameter is </code>false</code>.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     *
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneTree(boolean forceDuplicate,
			  boolean allowDanglingReferences) {
       return cloneTree(new NodeReferenceTable(),
			forceDuplicate, allowDanglingReferences);
    }


    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code>
     * and then <code>cloneTree</code>
     * is called for each child node.  For Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     * @param referenceTable table that stores the mapping between
     *	original and cloned nodes. All previous values in the
     *	referenceTable will be cleared before the clone is made.
     * @return a reference to the cloned sub-graph.
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @see NodeComponent#setDuplicateOnCloneTree
     * @since Java 3D 1.2
     */
    public Node cloneTree(NodeReferenceTable referenceTable) {
	return cloneTree(referenceTable, false, false);
    }


    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code>
     * and then <code>cloneTree</code> is called for each child node.
     * For Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     * @param referenceTable table that stores the mapping between
     *	original and cloned nodes. All previous values in the
     *	referenceTable will be cleared before the clone is made.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     * @return a reference to the cloned scene graph.
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @see NodeComponent#setDuplicateOnCloneTree
     * @since Java 3D 1.2
     */
    public Node cloneTree(NodeReferenceTable referenceTable,
			  boolean forceDuplicate) {
       return cloneTree(referenceTable, forceDuplicate, false);
    }

    /**
     * Duplicates all the nodes of the specified sub-graph.  For Group Nodes
     * the group node is duplicated via a call to <code>cloneNode</code>
     * and then <code>cloneTree</code> is called for each child node.
     * For Leaf Nodes, component
     * data can either be duplicated or be made a reference to the original
     * data.  Leaf Node cloneTree behavior is determined by the
     * <code>duplicateOnCloneTree</code> flag found in every Leaf Node's
     * component data class and by the <code>forceDuplicate</code> paramter.
     * @param referenceTable table that stores the mapping between
     *	original and cloned nodes. All previous values in the
     *	referenceTable will be cleared before the clone is made.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     *
     * @param allowDanglingReferences when set to <code>true</code> allows
     *  the <code>cloneTree</code>
     *  method to complete even whan a dangling reference is discovered. When
     *  this parameter is <code>false</code> a
     *  <code>DanglingReferenceException</code> is generated as
     *  soon as cloneTree detects this situation.
     *
     * @return a reference to the cloned scene graph.
     * @exception DanglingReferenceException When a dangling reference is
     *  discovered during the cloneTree operation.
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @see NodeComponent#setDuplicateOnCloneTree
     * @since Java 3D 1.2
     */
    public Node cloneTree(NodeReferenceTable referenceTable,
			  boolean forceDuplicate,
			  boolean allowDanglingReferences) {

	if (!isLiveOrCompiled()) {
	    // this will throw a SceneGraphCycleException if there is
	    // a cycle
	    checkForCycle();
	}

        referenceTable.set(allowDanglingReferences, new Hashtable());
        Node n = cloneTree(forceDuplicate, referenceTable.objectHashtable);

        // go through hash table looking for Leaf nodes.
        // call updateNodeReferences for each.
        Enumeration e = referenceTable.objectHashtable.elements();

        while (e.hasMoreElements()) {
            SceneGraphObject o =  (SceneGraphObject) e.nextElement();
	    o.updateNodeReferences(referenceTable);
        }
        return n;
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
     *  flag to be ignored.  When <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> determines whether data is
     *  duplicated or copied.
     *
     * @param nodeHashtable a hashtable used to map orignal node references to
     *  their cloned counterpart.
     *
     * @return a reference to the cloned scene graph.
     *
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    Node cloneTree(boolean forceDuplicate, Hashtable nodeHashtable) {
        Node l;
	this.nodeHashtable = nodeHashtable;
	try {
	    l = cloneNode(forceDuplicate);
	} catch (RuntimeException e) {
	    this.nodeHashtable = null;
	    throw e;
	}
	// must reset to null so that we can tell whether the call is from user
	// or cloneTree
        this.nodeHashtable = null;
	nodeHashtable.put(this, l);
	return l;
    }


    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * <code>cloneNode</code> should be overridden by any user subclassed
     * objects.  All subclasses must have their <code>cloneNode</code>
     * method consist of the following lines:
     * <P><blockquote><pre>
     *     public Node cloneNode(boolean forceDuplicate) {
     *         UserSubClass usc = new UserSubClass();
     *         usc.duplicateNode(this, forceDuplicate);
     *         return usc;
     *     }
     * </pre></blockquote>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneTree method.
     *
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     *
     * @see Node#cloneTree
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        throw new RuntimeException(J3dI18N.getString("Node12"));
    }


    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <code>NodeComponent</code> objects
     * contained by the object being duplicated, each <code>NodeComponent</code>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <code>NodeComponent</code> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * <br>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Group#cloneNode
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode,
			      boolean forceDuplicate) {
        duplicateAttributes(originalNode, forceDuplicate);
    }

   /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from subclass of
     * <code>duplicateNode</code> method which is, in turn, called by the
     * <code>cloneNode</code> method.
     * <P>
     * For any <i>NodeComponent</i> objects
     * contained by the object being duplicated, each <i>NodeComponent</i>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <i>NodeComponent<i> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Group#cloneNode
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    final void checkDuplicateNode(Node originalNode,
				  boolean forceDuplicate) {
        if (originalNode.nodeHashtable != null) {
	    duplicateAttributes(originalNode, forceDuplicate);
	} else {
	    //  user call cloneNode() or duplicateNode() directly
	    // instead of via cloneTree()
	    originalNode.nodeHashtable = new Hashtable();
	    duplicateAttributes(originalNode, forceDuplicate);
	    originalNode.nodeHashtable = null;
	}
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
     * @exception RestrictedAccessException if originalNode object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {

	if (originalNode.isLiveOrCompiled()) {
	    throw new RestrictedAccessException(J3dI18N.getString("Node13"));
	}
        super.duplicateSceneGraphObject(originalNode);
	NodeRetained attr = (NodeRetained) originalNode.retained;
	NodeRetained rt = (NodeRetained) retained;

	rt.setPickable(attr.getPickable());
	rt.setCollidable(attr.getCollidable());
    }


    /**
     * When set to <code>true</code> this <code>Node</code> can be Picked.
     * Setting to false indicates that this node and it's children
     * are ALL unpickable.
     *
     * @param pickable Indicates if this node should be pickable or not
     */
    public void setPickable( boolean pickable ) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_PICKABLE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Node14"));

	((NodeRetained)retained).setPickable(pickable);
    }

    /**
     * Returns true if this <code>Node</code> is pickable,
     * false if it is not pickable.
     */
    public boolean getPickable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_PICKABLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Node3"));

	return ((NodeRetained)retained).getPickable();
    }

    /**
     * checks for cycles in the scene graph
     */
    void checkForCycle() {
	if (visited) {
	    throw new SceneGraphCycleException(J3dI18N.getString("Node15"));
	}
	visited = true;
	Node parent = getParent();
	if (parent != null) {
	    parent.checkForCycle();
	}
	visited = false;
    }

}
