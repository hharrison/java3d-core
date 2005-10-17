/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Group node that contains a transform. The TransformGroup node 
 * specifies a single spatial transformation, via a Transform3D
 * object, that can position, orient, and scale all of its children.
 * <P>
 * The specified transformation must be affine. Further, if the 
 * TransformGroup node is used as an ancestor of a ViewPlatform node 
 * in the scene graph, the transformation must be congruent-only 
 * rotations, translations, and uniform scales are allowed in
 * a direct path from a Locale to a ViewPlatform node.
 * <P>
 * Note: Even though arbitrary affine transformations are
 * allowed, better performance will result if all matrices
 * within a branch graph are congruent, containing only rotations
 * translation, and uniform scale.
 * <P>
 * The effects of transformations in the scene graph are cumulative. 
 * The concatenation of the transformations of each TransformGroup in 
 * a direct path from the Locale to a Leaf node defines a composite 
 * model transformation (CMT) that takes points in that Leaf node's 
 * local coordinates and transforms them into Virtual World (Vworld) 
 * coordinates. This composite transformation is used to
 * transform points, normals, and distances into Vworld coordinates. 
 * Points are transformed by the CMT. Normals are transformed by the 
 * inverse-transpose of the CMT. Distances are transformed by the scale 
 * of the CMT. In the case of a transformation containing a nonuniform 
 * scale or shear, the maximum scale value in
 * any direction is used. This ensures, for example, that a transformed 
 * bounding sphere, which is specified as a point and a radius, 
 * continues to enclose all objects that are also transformed using 
 * a nonuniform scale.
 * <P>
 */

public class TransformGroup extends Group {
  /**
   * Specifies that the node allows access to 
   * its object's transform information.
   */
  public static final int
    ALLOW_TRANSFORM_READ = CapabilityBits.TRANSFORM_GROUP_ALLOW_TRANSFORM_READ;

  /**
   * Specifies that the node allows writing 
   * its object's transform information.
   */
  public static final int
    ALLOW_TRANSFORM_WRITE = CapabilityBits.TRANSFORM_GROUP_ALLOW_TRANSFORM_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_TRANSFORM_READ
    };

    /**
     * Constructs and initializes a TransformGroup using an
     * identity transform.
     */
    public TransformGroup() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);        
    }

    /**
     * Constructs and initializes a TransformGroup from
     * the Transform passed.
     * @param t1 the transform3D object
     * @exception BadTransformException if the transform is not affine.
     */
    public TransformGroup(Transform3D t1) {        
	if (!t1.isAffine()) {
	    throw new BadTransformException(J3dI18N.getString("TransformGroup0"));
	}
        
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
        
	((TransformGroupRetained)this.retained).setTransform(t1);
    }

    /**
     * Creates the retained mode TransformGroupRetained object that this
     * TransformGroup object will point to.
     */
    void createRetained() {
	this.retained = new TransformGroupRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the transform component of this TransformGroup to the value of
     * the passed transform.
     * @param t1 the transform to be copied
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception BadTransformException if the transform is not affine.
     */
    public void setTransform(Transform3D t1) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_TRANSFORM_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TransformGroup1"));

	if (!t1.isAffine()) {
	    throw new BadTransformException(J3dI18N.getString("TransformGroup0"));
	}

	((TransformGroupRetained)this.retained).setTransform(t1);
    }

  /**
   * Copies the transform component of this TransformGroup into
   * the passed transform object.
   * @param t1 the transform object to be copied into
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
   */
    public void getTransform(Transform3D t1) {
        if (isLiveOrCompiled())
	  if(!this.getCapability(ALLOW_TRANSFORM_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("TransformGroup2"));
    
	((TransformGroupRetained)this.retained).getTransform(t1);
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
        TransformGroup tg = new TransformGroup();
        tg.duplicateNode(this, forceDuplicate);
        return tg;
    }


    /**
     * Copies all TransformGroup information from
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
      
	Transform3D t = VirtualUniverse.mc.getTransform3D(null);
	((TransformGroupRetained) originalNode.retained).getTransform(t);
	((TransformGroupRetained) retained).setTransform(t);
	FreeListManager.freeObject(FreeListManager.TRANSFORM3D, t);
    }
}
