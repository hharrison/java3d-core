/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Hashtable;

/**
 * SceneGraphObject is the common superclass for all scene graph
 * objects. Scene graph objects are classified into two main types:
 * nodes and node components. The Node object is the common superclass
 * of all nodes, which includes TransformGroup, Shape3D, etc.
 * The NodeComponent object is the common superclass of all node
 * components, which includes Geometry, Appearance, etc.
 *
 * <p>
 * All scene graph objects have a name, a user data object, a set of
 * capability bits, and a set of capabilityIsFrequent bits.
 *
 * <p>
 * Capability bits control whether a particular attribute in a node or
 * node component is readable or writable. For live or compiled scene
 * graphs, only those attributes whose capabilities are set before the
 * scene graph is compiled or made live may be read or written. The
 * default value for all <i>read</i> capability bits is true, meaning
 * that all attributes may be read by default. The default value for
 * all <i>write</i> capability bits is false, meaning that no
 * attributes may be written by default. Read capability bits are
 * defined as those capability bits of the form <code>ALLOW_*_READ</code>,
 * plus the <code>ALLOW_INTERSECT</code> capability bit. Write
 * capability bits are defined as those capability bits of the form
 * <code>ALLOW_*_WRITE</code>, plus the <code>ALLOW_CHILDREN_EXTEND</code>
 * and <code>ALLOW_DETACH</code> capability bits.
 *
 * <p>
 * NOTE that the <code>ENABLE_COLLISION_REPORTING</code> and
 * <code>ENABLE_PICK_REPORTING</code> bits are not really capability bits,
 * although they are set with the setCapability method. The default value
 * for each of the <code>ENABLE_*_REPORTING bits</code> is false.
 *
 * <p>
 * For more information, see the
 * <a href="doc-files/intro.html">Introduction to the Java 3D API</a>.
 */
public abstract class SceneGraphObject extends Object {
   // Any global flags? (e.g., execution cullable, collideable)

    // Reference to the retained-mode scene-graph element.
    SceneGraphObjectRetained retained;

    // This object's capability bits
    private long capabilityBits = 0L;

    // This object's capabilityIsFrequent bits
    private long capabilityIsFrequentBits = ~0L;

    //boolean indicating is Scene Graph is compiled
    private boolean compiled = false;

    //boolean indicating if Scene Graph is live.
    private boolean live = false;

    //boolean indicating if Scene Graph is live or compiled
    private boolean liveOrCompiled = false;

    // A reference to user data
    private Object userData = null;

    // Optional name for object.
    private String objectName = null;

    // use for cloneTree/cloneNode only, set to null after the operation
    Hashtable nodeHashtable = null;



    /**
     * Constructs a SceneGraphObject with default parameters.  The default
     * values are as follows:
     * <ul>
     * all <i>read</i> capability bits : set (true)<br>
     * all <i>write</i> capability bits : clear (false)<br>
     * all capabilityIsFrequent bits : set (true)<br>
     * isLive : false<br>
     * isCompiled : false<br>
     * user data : null<br>
     * name : null<br>
     * </ul>
     */
    public SceneGraphObject() {
	createRetained();
    }

    /**
     * Creates the retained mode object that this scene graph object
     * will point to.  This should be overridden by those classes
     * that have a specific retained mode object.
     */
    void createRetained() {
	this.retained = null;

	// Non-abstract subclasses of SceneGraphObject should override
	// this function with code which is something like the following:
	//
	//	this.retained = new <ClassName>Retained();
	//	this.retained.setSource(this);
    }

    /**
     * Method to set default read capability bits to true
     */
    void setDefaultReadCapabilities(int[] bits) {
        if (true /*VirtualUniverse.mc.defaultReadCapability*/) {
            for (int i=0; i < bits.length; i++) {
                setCapability(bits[i]);
            }
        }
    }

    /**
     * Retrieves the specified capability bit.  Note that only one capability
     * bit may be retrieved per method invocation--capability bits cannot
     * be ORed together.
     * @param bit the bit whose value is returned
     * @return true if the bit is set, false if the bit is clear
     */
    public final boolean getCapability(int bit) {
	return (capabilityBits & (1L << bit)) != 0L;
    }

    /**
     * Sets the specified capability bit.  Note that only one capability bit
     * may be set per method invocation--capability bits cannot be ORed
     * together.
     * @param bit the bit to set
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     */
    public final void setCapability(int bit) {
	if (isLiveOrCompiled()) {
            throw new RestrictedAccessException(J3dI18N.getString("SceneGraphObject0"));
	}

	capabilityBits |= (1L << bit);
	retained.handleFrequencyChange(bit);

    }

    /**
     * Clear the specified capability bit.  Note that only one capability bit
     * may be cleared per method invocation--capability bits cannot be ORed
     * together.
     * @param bit the bit to clear
     * @exception RestrictedAccessException if this object is part of live
     * or compiled scene graph
     */
    public final void clearCapability(int bit) {
	if (isLiveOrCompiled())
            throw new RestrictedAccessException(J3dI18N.getString("SceneGraphObject0"));

	capabilityBits &= ~(1L << bit);
	retained.handleFrequencyChange(bit);
    }


    // Internal method, returns true if no capability bits are set
    final boolean capabilityBitsEmpty() {
	return capabilityBits == 0L;
    }


    /**
     * Retrieves the isFrequent bit associated with the specified capability
     * bit.
     *
     * Note that only one isFrequent bit, for a single capability
     * bit, may be retrieved per method invocation--capability bits cannot
     * be ORed together.
     *
     * @param bit the bit whose value is returned
     *
     * @return true if the isFrequent bit is set, false if the isFrequent
     * bit is clear
     *
     * @since Java 3D 1.3
     */
    public final boolean getCapabilityIsFrequent(int bit) {
	return (capabilityIsFrequentBits & (1L << bit)) != 0L;
    }

    /**
     * Sets the isFrequent bit associated with the specified
     * capability bit.  Setting the isFrequent bit indicates that the
     * application may frequently access or modify those attributes
     * permitted by the associated capability bit.  This can be used
     * by Java 3D as a hint to avoid certain optimizations that could
     * cause those accesses or modifications to be expensive.  By
     * default the isFrequent bit associated with each capability bit
     * is set.
     *
     * <p>
     * Unlike setCapability, this method may be called on a live scene
     * graph object (but not on a compiled object).
     *
     * <p>
     * Note that only one isFrequent bit, for a single capability bit,
     * may be set per method invocation--capability bits cannot be ORed
     * together.
     *
     * @param bit the capability bit for which to set the associated
     * isFrequent bit
     *
     * @exception RestrictedAccessException if this object is part of a
     * compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public final void setCapabilityIsFrequent(int bit) {
	if (isCompiled())
            throw new RestrictedAccessException(J3dI18N.getString("SceneGraphObject1"));

	capabilityIsFrequentBits |= (1L << bit);
	retained.handleFrequencyChange(bit);
    }

    /**
     * Clears the isFrequent bit associated with the specified
     * capability bit.  Clearing the isFrequent bit indicates that the
     * application will infrequently access or modify those attributes
     * permitted by the associated capability bit.  This can be used
     * by Java 3D as a hint to enable certain optimizations that it
     * might otherwise avoid, for example, optimizations that could
     * cause those accesses or modifications to be expensive.
     *
     * <p>
     * Unlike clearCapability, this method may be called on a live scene
     * graph object (but not on a compiled object).
     *
     * <p>
     * Note that only one isFrequent bit, for a single capability bit,
     * may be cleared per method invocation--capability bits cannot be ORed
     * together.
     *
     * @param bit the capability bit for which to clear the associated
     * isFrequent bit
     *
     * @exception RestrictedAccessException if this object is part of a
     * compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public final void clearCapabilityIsFrequent(int bit) {
	if (isCompiled())
            throw new RestrictedAccessException(J3dI18N.getString("SceneGraphObject1"));

	capabilityIsFrequentBits &= ~(1L << bit);
	retained.handleFrequencyChange(bit);
    }


    /**
     * Sets an internal flag which indicates that this scene graph object
     * has been compiled.
     */
    final void setCompiled() {
	this.compiled = true;
	this.liveOrCompiled = this.live || this.compiled;
    }

    /**
     * Returns a flag indicating whether the node is part of a scene graph
     * that has been compiled.  If so, then only those capabilities explicitly
     * allowed by the object's capability bits are allowed.
     * @return true if node is part of a compiled scene graph, else false
     */

    public final boolean isCompiled() {
	return this.compiled;
    }

    /**
     * Sets an internal flag which indicates that this scene graph object
     * is part of a live scene graph.
     */
    final void setLive() {
	this.live = true;
	this.liveOrCompiled = this.live || this.compiled;
    }

    /**
     * Clears an internal flag which indicates that this scene graph object
     * is no longer part of a live scene graph.
     */
    final void clearLive() {
        this.live = false;
        this.liveOrCompiled = this.live || this.compiled;
    }

    /**
     * Returns a flag indicating whether the node is part of a live
     * scene graph.
     * @return true if node is part of a live scene graph, else false
     */
    public final boolean isLive() {
	return this.live;
    }

    /**
     * Returns a flag indicating whether the node is part of a live
     * scene graph or a compiled scene graph.
     * @return true if either live or compiled
     */
    final boolean isLiveOrCompiled() {
	return liveOrCompiled;
    }

    final void checkForLiveOrCompiled() {
	if (isLiveOrCompiled())
	    throw new RestrictedAccessException(J3dI18N.getString("SceneGraphObject2"));
    }

    /**
     * Sets the userData field associated with this scene graph object.
     * The userData field is a reference to an arbitrary object
     * and may be used to store any user-specific data associated
     * with this scene graph object--it is not used by the Java 3D API.
     * If this object is cloned, the userData field is copied
     * to the newly cloned object.
     * @param userData a reference to the new userData field
     */
    public void setUserData(Object userData) {
	this.userData = userData;
    }

    /**
     * Retrieves the userData field from this scene graph object.
     * @return the current userData field
     */
    public Object getUserData() {
	return this.userData;
    }

   /**
     * Callback used to allow a node to check if any scene graph objects
     * referenced by that node have been duplicated via a call to
     * <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf
     * node and cloned NodeComponent's method
     * will be called and the Leaf node/NodeComponent can then look up
     * any object references
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
    }

    /**
     * Sets the name of this object. Object names are for information
     * only.
     *
     * @param name the new name of this object
     *
     * @since Java 3D 1.4
     */
    public void setName( String name ) {
        objectName = name;
    }

    /**
     * Returns the name of this object.
     *
     * @return the name of this object
     *
     * @since Java 3D 1.4
     */
    public String getName() {
	return objectName;
    }

    /**
     * Copies all SceneGraphObject information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @param originalNode the original node to duplicate.
     *
     * @see Group#cloneNode
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    protected void duplicateSceneGraphObject(SceneGraphObject originalNode) {
        // Duplicate any class specific data here.
	capabilityBits = originalNode.capabilityBits;
        userData = originalNode.userData;
        objectName = originalNode.objectName;
    }


    /**
     * If <code>forceDuplicate</code> is <code>true</code> or
     * <code>duplicateOnCloneTree</code> flag is true. This procedure
     * will return a clone of originalNode or the value in
     * in <code>nodeHashtable</code> if found. Otherwise return
     * <code>originalNode</code>
     *
     * This method is called from the
     * <code>duplicateAttributes</code> method during cloneNodeComponent.
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     * @param nodeHashtable is used to keep track of mapping between old and
     *  new node references.
     */
    NodeComponent getNodeComponent(NodeComponent originalNodeComponent,
				   boolean forceDuplicate,
				   Hashtable hashtable) {
        if ((originalNodeComponent != null) &&
	        (forceDuplicate ||
		 originalNodeComponent.duplicateChild())) {
	    NodeComponent nc = (NodeComponent)
	                           hashtable.get(originalNodeComponent);
	    if (nc == null) {
	        originalNodeComponent.nodeHashtable = hashtable;
		try {
  	            nc = originalNodeComponent.
		             cloneNodeComponent(forceDuplicate);
		} catch (RuntimeException e) {
		  // must reset nodeHashtable in any case
		  originalNodeComponent.nodeHashtable = null;
		  throw e;
		}
		originalNodeComponent.nodeHashtable = null;
		// put link to be shared by other Node
		hashtable.put(originalNodeComponent, nc);
	    } // use the share clone node otherwise
	    return nc;
	} else {
	    return originalNodeComponent;
	}
    }

    // Internal method to make a prefix out of the name of this object
    String getNamePrefix() {
	String name = getName();

	if (name != null) {
	    return "[" + name + "] ";
	}

	return "";
    }

    /**
     * Returns a String representation of this SceneGraphObject.
     * If its name is non-null, then it is concatenated with
     * super.toString().
     */
    public String toString() {
	return getNamePrefix() + super.toString();
    }

}
