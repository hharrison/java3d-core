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
 * The Soundscape Leaf Node defines the attributes that characterize the
 * listener's environment as it pertains to sound. This node defines an
 * application region and an associated aural attribute component object
 * that controls reverberation and atmospheric properties that affect sound
 * source rendering. Multiple Soundscape nodes can be included in a single
 * scene graph.
 *  <P>
 * The Soundscape application region, different from a Sound node's scheduling
 * region, is used to select which Soundscape (and thus which aural attribute
 * object) is to be applied to the sounds being rendered. This selection is
 * based on the position of the ViewPlatform (i.e., the listener), not the
 * position of the sound.
 *  <P>
 * It will be common that multiple Soundscape regions are contained within a
 * scene graph. For example, two Soundscape regions within a single space the
 * listener can move about: a region with a large open area on the right, and
 * a smaller more constricted, less reverberant area on the left.  The rever-
 * beration attributes for these two regions could be set to approximate their
 * physical differences so that active sounds are rendered differently depending
 * on which region the listener is in.
 */
public class Soundscape extends Leaf {

    // Constants
    //
    // These flags, when enabled using the setCapability method, allow an
    // application to invoke methods that respectively read and write the
    // application region and the aural attributes. These capability flags
    // are enforced only when the node is part of a live or compiled scene
    // graph.

    /**
     * For Soundscape component objects, specifies that this object
     * allows read access to its application bounds
     */
    public static final int
    ALLOW_APPLICATION_BOUNDS_READ = CapabilityBits.SOUNDSCAPE_ALLOW_APPLICATION_BOUNDS_READ;

     /**
      * For Soundscape component objects, specifies that this object
      * allows write access to its application bounds
      */
     public static final int
    ALLOW_APPLICATION_BOUNDS_WRITE = CapabilityBits.SOUNDSCAPE_ALLOW_APPLICATION_BOUNDS_WRITE;

     /**
      * For Soundscape component objects, specifies that this object
      * allows the reading of it's aural attributes information
      */
     public static final int
    ALLOW_ATTRIBUTES_READ = CapabilityBits.SOUNDSCAPE_ALLOW_ATTRIBUTES_READ;

     /**
      * For Soundscape component objects, specifies that this object
      * allows the writing of it's aural attribute information
      */
     public static final int
    ALLOW_ATTRIBUTES_WRITE = CapabilityBits.SOUNDSCAPE_ALLOW_ATTRIBUTES_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_APPLICATION_BOUNDS_READ,
        ALLOW_ATTRIBUTES_READ        
    };
     /**
     * Constructs and initializes a new Sound node using following
     * defaults:
     *<UL>   application region: null (no active region)</UL>
     *<UL>  aural attributes: null (uses default aural attributes)</UL>
     */  
    public Soundscape() {
         // Just use default values
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a new Sound node using specified
     * parameters
     * @param region application region
     * @param attributes array of aural attribute component objects
     */  
    public Soundscape(Bounds region,
                      AuralAttributes attributes) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((SoundscapeRetained)this.retained).setApplicationBounds(region);
        ((SoundscapeRetained)this.retained).setAuralAttributes(attributes);
    }

    /**
     * Creates the retained mode SoundscapeRetained object that this
     * component object will point to.
     */  
    void createRetained() {
        this.retained = new SoundscapeRetained();
        this.retained.setSource(this);
    }

    /**
     * Set the Soundscape's application region to the specified bounds
     * specified in local coordinates of this leaf node.  The aural 
     * attributes associated with this Soundscape are used to render 
     * the active sounds when this application region intersects the 
     * ViewPlatform's activation volume. The getApplicationBounds method 
     * returns a new Bounds object.
     * This region is used when the application bounding leaf is null.
     * @param region the bounds that contains the Soundscape's new application
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setApplicationBounds(Bounds region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Soundscape0"));

	((SoundscapeRetained)this.retained).setApplicationBounds(region);
    }

    /**  
     * Retrieves the Soundscape node's application bounds.
     * @return this Soundscape's application bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public Bounds getApplicationBounds() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Soundscape1"));

	return ((SoundscapeRetained)this.retained).getApplicationBounds();
    }

    /**
     * Set the Soundscape's application region to the specified bounding leaf.
     * When set to a value other than null, this overrides the application
     * bounds object.
     * @param region the bounding leaf node used to specify the Soundscape
     * node's new application region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setApplicationBoundingLeaf(BoundingLeaf region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Soundscape0"));

	((SoundscapeRetained)this.retained).setApplicationBoundingLeaf(region);
    }

    /**  
     * Retrieves the Soundscape node's application bounding leaf.
     * @return this Soundscape's application bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public BoundingLeaf getApplicationBoundingLeaf() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Soundscape1"));

	return ((SoundscapeRetained)this.retained).getApplicationBoundingLeaf();
    }

    /**
     * Set a set of aural attributes for this Soundscape
     * @param attributes aural attributes
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAuralAttributes(AuralAttributes attributes) {
      if (isLiveOrCompiled())
       if(!this.getCapability(ALLOW_ATTRIBUTES_WRITE))
         throw new CapabilityNotSetException(J3dI18N.getString("Soundscape4"));

       ((SoundscapeRetained)this.retained).setAuralAttributes(attributes);
    }

    /**
     * Retrieve reference of Aural Attributes
     * @return reference to aural attributes
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public AuralAttributes getAuralAttributes() {
      if (isLiveOrCompiled())
       if(!this.getCapability(ALLOW_ATTRIBUTES_READ)) 
         throw new CapabilityNotSetException(J3dI18N.getString("Soundscape5")); 
 
       return ((SoundscapeRetained)this.retained).getAuralAttributes();
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
        Soundscape s = new Soundscape();
        s.duplicateNode(this, forceDuplicate);
        return s;
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
     * @exception ClassCastException if originalNode is not an instance of 
     *  <code>Soundscape</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }

   /**
     * Copies all Soundscape information from
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

	SoundscapeRetained attr = (SoundscapeRetained) originalNode.retained;
	SoundscapeRetained rt = (SoundscapeRetained) retained;

	rt.setApplicationBounds(attr.getApplicationBounds());
	
	rt.setAuralAttributes((AuralAttributes) getNodeComponent(
					       attr.getAuralAttributes(),
					       forceDuplicate,
					       originalNode.nodeHashtable));

	// the following reference will set correctly in updateNodeReferences
	rt.setApplicationBoundingLeaf(attr.getApplicationBoundingLeaf());
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

	SoundscapeRetained rt = (SoundscapeRetained) retained;

        BoundingLeaf bl = rt.getApplicationBoundingLeaf();

        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.setApplicationBoundingLeaf((BoundingLeaf) o);
        }
    }

}
