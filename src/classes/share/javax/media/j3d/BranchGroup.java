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
 * The BranchGroup serves as a pointer to the root of a
 * scene graph branch; BranchGroup objects are the only objects that
 * can be inserted into a Locale's set of objects. A subgraph, rooted
 * by a BranchGroup node can be thought of as a compile unit. The
 * following things may be done with BranchGroup:
 * <P><UL>
 * <LI>A BranchGroup may be compiled by calling its compile method. This causes the
 * entire subgraph to be compiled. If any BranchGroup nodes are contained within the
 * subgraph, they are compiled as well (along with their descendants).</LI>
 * <p>
 * <LI>A BranchGroup may be inserted into a virtual universe by attaching it to a
 * Locale. The entire subgraph is then said to be live.</LI>
 * <p>
 * <LI>A BranchGroup that is contained within another subgraph may be reparented or
 * detached at run time if the appropriate capabilities are set.</LI>
 * </UL>
 * Note that that if a BranchGroup is included in another subgraph, as a child of
 * some other group node, it may not be attached to a Locale.
 */

public class BranchGroup extends Group {

  /**
   * For BranchGroup nodes, specifies that this BranchGroup allows detaching 
   * from its parent.
   */
    public static final int
        ALLOW_DETACH = CapabilityBits.BRANCH_GROUP_ALLOW_DETACH;

    /**
     * Constructs and initializes a new BranchGroup node object.
     */
    public BranchGroup() {
    }

    /**
     * Creates the retained mode BranchGroupRetained object that this
     * BranchGroup component object will point to.
     */
    void createRetained() {
	this.retained = new BranchGroupRetained();
	this.retained.setSource(this);
    }
  

  /**
   * Compiles the source BranchGroup associated with this object and
   * creates and caches a compiled scene graph.
   * @exception SceneGraphCycleException if there is a cycle in the
   * scene graph
   * @exception RestrictedAccessException if the method is called
   * when this object is part of a live scene graph.
   */
    public void compile() {
        if (isLive()) {
	    throw new RestrictedAccessException(
				    J3dI18N.getString("BranchGroup0"));
	}

	if (isCompiled() == false) {
	    // will throw SceneGraphCycleException if there is a cycle
	    // in the scene graph
	    checkForCycle();
	    
	    ((BranchGroupRetained)this.retained).compile();
	}
    }

  /**
   * Detaches this BranchGroup from its parent.
   */
    public void detach() {
       Group parent;

       if (isLiveOrCompiled()) {
           if(!this.getCapability(ALLOW_DETACH))
               throw new CapabilityNotSetException(J3dI18N.getString("BranchGroup1"));

           if (((BranchGroupRetained)this.retained).parent != null) {
	       parent = (Group)((BranchGroupRetained)this.retained).parent.source;
               if(!parent.getCapability(Group.ALLOW_CHILDREN_WRITE))
                   throw new CapabilityNotSetException(J3dI18N.getString("BranchGroup2"));
	   }
       }

      ((BranchGroupRetained)this.retained).detach();
  }

  /**
   * Returns an array referencing all the items that are pickable below this
   * <code>BranchGroup</code> that intersect with PickShape.
   * The resultant array is unordered.
   *
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickAll
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *
   */
    public SceneGraphPath[] pickAll( PickShape pickShape ) {
        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));
    
	return Picking.pickAll( this, pickShape );
    }

  /**
   * Returns a sorted array of references to all the Pickable items that 
   * intersect with the pickShape. Element [0] references the item closest 
   * to <i>origin</i> of PickShape successive array elements are further 
   * from the <i>origin</i>
   *
   * Note: If pickShape is of type PickBounds, the resulting array 
   * is unordered.
   * @param pickShape the PickShape object
   * 
   * @see SceneGraphPath
   * @see Locale#pickAllSorted
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *  
   */
    public SceneGraphPath[] pickAllSorted( PickShape pickShape ) {
        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));
    
	return Picking.pickAllSorted( this, pickShape );
    }

  /**
   * Returns a SceneGraphPath that references the pickable item 
   * closest to the origin of <code>pickShape</code>.
   *
   * Note: If pickShape is of type PickBounds, the return is any pickable node
   * below this BranchGroup.
   * @param pickShape the PickShape object
   *
   * @see SceneGraphPath
   * @see Locale#pickClosest
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *  
   */
    public SceneGraphPath pickClosest( PickShape pickShape ) {
        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));

	return Picking.pickClosest( this, pickShape );
    }

  /**
   * Returns a reference to any item that is Pickable below this BranchGroup that
   * intersects with <code>pickShape</code>.
   *
   * @param pickShape the PickShape object
   * @see SceneGraphPath
   * @see Locale#pickAny
   * @see PickShape
   * @exception IllegalStateException if BranchGroup is not live.
   *  
   */
    public SceneGraphPath pickAny( PickShape pickShape ) {
        if(isLive()==false)
	    throw new IllegalStateException(J3dI18N.getString("BranchGroup3"));
    
	return Picking.pickAny( this, pickShape );
    }


   /**
    * Creates a new instance of the node.  This routine is called
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
        BranchGroup bg = new BranchGroup();
	bg.duplicateNode(this, forceDuplicate);
	return bg;
    }
}
