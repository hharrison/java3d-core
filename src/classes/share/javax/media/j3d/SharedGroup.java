/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The SharedGroup provides the ability to manipulate an
 * instanced scene graph.
 * A SharedGroup node allows multiple Link leaf nodes to share its
 * subgraph according to the following semantics:
 * <P><UL>
 * <LI>A SharedGroup may be referenced by one or more Link leaf
 * nodes. Any runtime changes to a node or component object in this
 * shared subgraph affect all graphs that refer to this subgraph.</LI><P>
 *
 * <LI>A SharedGroup may be compiled by calling its compile method
 * prior to being referenced by any Link leaf nodes.</LI><P>
 *
 * <LI>Only Link leaf nodes may refer to SharedGroup nodes. A
 * SharedGroup node cannot have parents or be attached to a Locale.</LI><P>
 * </UL>
 *
 * A shared subgraph may contain any group node, except an embedded
 * SharedGroup node (SharedGroup nodes cannot have parents). However,
 * only the following leaf nodes may appear in a shared subgraph:
 * <P><UL>
 * <LI>Light</LI>
 * <LI>Link</LI>
 * <LI>Morph</LI>
 * <LI>Shape</LI>
 * <LI>Sound</LI></UL><P>
 *
 * An IllegalSharingException is thrown if any of the following leaf nodes
 * appear in a shared subgraph:<P>
 * <UL>
 * <LI>AlternateAppearance</LI>
 * <LI>Background</LI>
 * <LI>Behavior</LI>
 * <LI>BoundingLeaf</LI>
 * <LI>Clip</LI>
 * <LI>Fog</LI>
 * <LI>ModelClip</LI>
 * <LI>Soundscape</LI>
 * <LI>ViewPlatform</LI></UL>
 * <P>
 *
 * @see IllegalSharingException
 */

public class SharedGroup extends Group {

    /**
     * Specifies that this SharedGroup node allows reading the
     * list of links that refer to this node.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_LINK_READ = CapabilityBits.SHARED_GROUP_ALLOW_LINK_READ;


    /**
     * Constructs and initializes a new SharedGroup node object.
     */
    public SharedGroup() {
    }


    /**
     * Returns the list of Link nodes that refer to this SharedGroup node.
     * @return An array of Link nodes that refer to this SharedGroup node. 
     *
     * @since Java 3D 1.3
     */
    public Link[] getLinks() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_LINK_READ))
			throw new CapabilityNotSetException(J3dI18N.getString("SharedGroup1"));
		return ((SharedGroupRetained)retained).getLinks();	
    }


    /**
     * Creates the retained mode SharedGroupRetained object that this
     * SharedGroup component object will point to.
     */
    void createRetained() {
        this.retained = new SharedGroupRetained();
        this.retained.setSource(this);
    }
  

    /**
     * Compiles the source SharedGroup associated with this object and
     * creates and caches a compiled scene graph.
     * @exception SceneGraphCycleException if there is a cycle in the
     * scene graph
     * @exception RestrictedAccessException if the method is called
     * when this object is part of a live scene graph.
     */
    public void compile() {
        if (isLive()) {
	    throw new RestrictedAccessException(J3dI18N.getString("SharedGroup0"));
        }

        if (isCompiled() == false) {
	    // will throw SceneGraphCycleException if there is a cycle
	    // in the scene graph
	    checkForCycle();
	    
            ((SharedGroupRetained)this.retained).compile();
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
        SharedGroup sg = new SharedGroup();
        sg.duplicateNode(this, forceDuplicate);
        return sg;
    }
}
