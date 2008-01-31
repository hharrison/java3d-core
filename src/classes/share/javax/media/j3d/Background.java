/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;

/**
 * The Background leaf node defines a solid background color
 * and a background image that are used to fill the window at the
 * beginning of each new frame.  The background image may be null.
 * It optionally allows background
 * geometry---which is pre-tessellated onto a unit sphere and is drawn
 * at infinity---to be referenced.  It also specifies an application
 * region in which this background is active.  A Background node is
 * active when its application region intersects the ViewPlatform's
 * activation volume. If multiple Background nodes are active, the
 * Background node that is "closest" to the eye will be used. If no
 * Background nodes are active, then the window is cleared to black.
 *
 * <p>
 * The set of nodes that can be added to a BranchGroup associated with
 * a Background node is limited. All Group nodes except
 * ViewSpecificGroup are legal in a background geometry branch
 * graph. The only Leaf nodes that are legal are Shape3D (except
 * OrientedShape3D), Morph, Light, and Fog. The presence of any other
 * Leaf node, including OrientedShape3D, or of a ViewSpecificGroup
 * node will cause an IllegalSceneGraphException to be thrown. Note
 * that Link nodes are not allowed; a background geometry branch graph
 * must not reference shared subgraphs.  NodeComponent objects can be
 * shared between background branches and ordinary (non-background)
 * branches or among different background branches, however.
 *
 * <p>
 * Light and Fog nodes in a background geometry branch graph do not
 * affect nodes outside of the background geometry branch graph, and
 * vice versa.  Light and Fog nodes that appear in a background
 * geometry branch graph must not be hierarchically scoped to any
 * group node outside of that background geometry branch graph.
 * Conversely, Light and Fog nodes that appear outside of a particular
 * background geometry branch graph must not be hierarchically scoped
 * to any group node in that background geometry branch graph.  Any
 * attempt to do so will be ignored.
 *
 * <p>
 * The influencing bounds of any Light or Fog node in a background
 * geometry branch graph is effectively infinite (meaning that all
 * lights can affect all geometry objects nodes within the background
 * geometry graph, and that an arbitrary fog is selected).  An
 * application wishing to limit the scope of a Light or Fog node must
 * use hierarchical scoping.
 *
 * <p>
 * Picking and collision is ignored for nodes inside a background
 * geometry branch graph.
 */
public class Background extends Leaf {
    /**
     * Specifies that the Background allows read access to its application
     * bounds and bounding leaf at runtime.
     */
    public static final int
    ALLOW_APPLICATION_BOUNDS_READ = CapabilityBits.BACKGROUND_ALLOW_APPLICATION_BOUNDS_READ;

    /**
     * Specifies that the Background allows write access to its application
     * bounds and bounding leaf at runtime.
     */
    public static final int
    ALLOW_APPLICATION_BOUNDS_WRITE = CapabilityBits.BACKGROUND_ALLOW_APPLICATION_BOUNDS_WRITE;

    /**
      * Specifies that the Background allows read access to its image
      * at runtime.
      */
     public static final int
    ALLOW_IMAGE_READ = CapabilityBits.BACKGROUND_ALLOW_IMAGE_READ;

    /**
      * Specifies that the Background allows write access to its image
      * at runtime.
      */
     public static final int
    ALLOW_IMAGE_WRITE = CapabilityBits.BACKGROUND_ALLOW_IMAGE_WRITE;

    /**
      * Specifies that the Background allows read access to its color
      * at runtime.
      */
     public static final int
    ALLOW_COLOR_READ = CapabilityBits.BACKGROUND_ALLOW_COLOR_READ;

    /**
      * Specifies that the Background allows write access to its color
      * at runtime.
      */
     public static final int
    ALLOW_COLOR_WRITE = CapabilityBits.BACKGROUND_ALLOW_COLOR_WRITE;

    /**
      * Specifies that the Background allows read access to its
      * background geometry at runtime.
      */
     public static final int
    ALLOW_GEOMETRY_READ = CapabilityBits.BACKGROUND_ALLOW_GEOMETRY_READ;

    /**
      * Specifies that the Background allows write access to its
      * background geometry at runtime.
      */
     public static final int
    ALLOW_GEOMETRY_WRITE = CapabilityBits.BACKGROUND_ALLOW_GEOMETRY_WRITE;

    /**
     * Specifies that the Background allows read access to its image
     * scale mode at runtime.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_IMAGE_SCALE_MODE_READ =
	CapabilityBits.BACKGROUND_ALLOW_IMAGE_SCALE_MODE_READ;

    /**
     * Specifies that the Background allows write access to its image
     * scale mode at runtime.
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_IMAGE_SCALE_MODE_WRITE =
	CapabilityBits.BACKGROUND_ALLOW_IMAGE_SCALE_MODE_WRITE;


    /**
     * Indicates that no scaling of the background image is done.  The
     * image will be drawn in its actual size.  If the window is
     * smaller than the image, the image will be clipped.  If the
     * window is larger than the image, the portion of the window not
     * filled by the image will be filled with the background color.
     * In all cases, the upper left corner of the image is anchored at
     * the upper-left corner of the window.
     * This is the default mode.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_NONE = 0;

    /**
     * Indicates that the background image is uniformly scaled to fit
     * the window such that the entire image is visible.  The image is
     * scaled by the smaller of <code>window.width/image.width</code>
     * and <code>window.height/image.height</code>.  The image will
     * exactly fill either the width or height of the window, but not
     * necessarily both.  The portion of the window not filled by the
     * image will be filled with the background color.
     * The upper left corner of the image is anchored at the
     * upper-left corner of the window.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_FIT_MIN = 1;

    /**
     * Indicates that the background image is uniformly scaled to fit
     * the window such that the entire window is filled.  The image is
     * scaled by the larger of <code>window.width/image.width</code>
     * and <code>window.height/image.height</code>.  The image will
     * entirely fill the window, but may by clipped either in <i>X</i>
     * or <i>Y</i>.
     * The upper left corner of the image is anchored at the
     * upper-left corner of the window.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_FIT_MAX = 2;


    /**
     * Indicates that the background image is scaled to fit the
     * window.  The image is scaled non-uniformly in <i>x</i> and
     * <i>y</i> by <code>window.width/image.width</code> and and
     * <code>window.height/image.height</code>, respectively.  The
     * image will entirely fill the window.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_FIT_ALL = 3;

    /**
     * Indicates that the background image is tiled to fill the entire
     * window.  The image is not scaled.
     * The upper left corner of the image is anchored at the
     * upper-left corner of the window.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_REPEAT = 4; 

    /**
     * Indicates that the background image is centered in the window
     * and that no scaling of the image is done.  The image will be
     * drawn in its actual size.  If the window is smaller than the
     * image, the image will be clipped.  If the window is larger than
     * the image, the portion of the window not filled by the image
     * will be filled with the background color.
     *
     * @see #setImageScaleMode
     *
     * @since Java 3D 1.3
     */
    public static final int SCALE_NONE_CENTER = 5; 
    
   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_APPLICATION_BOUNDS_READ,
        ALLOW_COLOR_READ,
        ALLOW_GEOMETRY_READ,
        ALLOW_IMAGE_READ,
        ALLOW_IMAGE_SCALE_MODE_READ
    };
    
    
    /**
     * Constructs a Background node with default parameters.  The default
     * values are as follows:
     * <ul>
     * color : black (0,0,0)<br>
     * image : null<br>
     * geometry : null<br>
     * image scale mode : SCALE_NONE<br>
     * application bounds : null<br>
     * application bounding leaf : null<br>
     * </ul>
     */
    public Background () {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a Background node with the specified color.
     * This color is used to fill the window prior to drawing any
     * objects in the scene.
     */
    public Background(Color3f color) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((BackgroundRetained)this.retained).setColor(color);
    }

    /**
     * Constructs a Background node with the specified color.
     * This color is used to fill the window prior to drawing any
     * objects in the scene.
     */
    public Background(float r, float g, float b) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((BackgroundRetained)this.retained).setColor(r, g, b);
    }

    /**
     * Constructs a Background node with the specified image.  If this
     * image is non-null, it is rendered to the window prior to
     * drawing any objects in the scene.  If the image is smaller
     * than the window,
     * then that portion of the window not covered by the image is
     * filled with the background color.
     *
     * @param image pixel array object used as the background image
     * 
     * @exception IllegalArgumentException if the image class of the specified
     * ImageComponent2D is ImageClass.NIO_IMAGE_BUFFER.
     */
    public Background(ImageComponent2D image) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
       
        if((image != null) && 
                (image.getImageClass() == ImageComponent.ImageClass.NIO_IMAGE_BUFFER)) {     
            throw new IllegalArgumentException(J3dI18N.getString("Background14"));
        }

        ((BackgroundRetained)this.retained).setImage(image);
    }

    /**
     * Constructs a Background node with the specified geometry.
     * If non-null, this background geometry is drawn on top of
     * the background color and image using a projection
     * matrix that essentially puts the geometry at infinity.  The geometry
     * should be pre-tessellated onto a unit sphere.
     * @param branch the root of the background geometry
     * @exception IllegalSharingException if the BranchGroup node
     * is a child of any Group node, or is already attached to a Locale,
     * or is already referenced by another Background node.
     * @exception IllegalSceneGraphException if specified branch graph
     * contains an illegal node.
     */
    public Background(BranchGroup branch) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((BackgroundRetained)this.retained).setGeometry(branch);
    }

    /**
     * Sets the background color to the specified color.
     * This color is used to fill the window prior to drawing any
     * objects in the scene.
     * @param color the new background color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColor(Color3f color) {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_COLOR_WRITE))
           throw new CapabilityNotSetException(J3dI18N.getString("Background0"));

	if (isLive()) 
	    ((BackgroundRetained)this.retained).setColor(color);
	else
	    ((BackgroundRetained)this.retained).initColor(color);

    }

    /**
     * Sets the background color to the specified color.
     * This color is used to fill the window prior to drawing any
     * objects in the scene.
     * @param r the red component of the background color
     * @param g the green component of the background color
     * @param b the blue component of the background color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColor(float r, float g, float b) {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_COLOR_WRITE))
           throw new CapabilityNotSetException(J3dI18N.getString("Background0"));

	if (isLive())
	    ((BackgroundRetained)this.retained).setColor(r, g, b);
	else
	    ((BackgroundRetained)this.retained).initColor(r, g, b);
    }

    /**
     * Retrieves the background color.
     * @param color the vector that will receive the current background color
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getColor(Color3f color) {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_COLOR_READ))
           throw new CapabilityNotSetException(J3dI18N.getString("Background2"));

	((BackgroundRetained)this.retained).getColor(color);
    }

    /**
     * Sets the background image to the specified image.  If this
     * image is non-null, it is rendered to the window prior to
     * drawing any objects in the scene.  If the image is smaller
     * than the window,
     * then that portion of the window not covered by the image is
     * filled with the background color.
     *
     * @param image new pixel array object used as the background image
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalSharingException if this Background is live and
     * the specified image is being used by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this Background is
     * being used by an immediate mode context and
     * the specified image is being used by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalArgumentException if the image class of the specified
     * ImageComponent2D is ImageClass.NIO_IMAGE_BUFFER.
     */
    public void setImage(ImageComponent2D image) {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_IMAGE_WRITE))
           throw new CapabilityNotSetException(J3dI18N.getString("Background3"));

        BackgroundRetained bgRetained = (BackgroundRetained)this.retained;

        if((image != null) && 
                (image.getImageClass() == ImageComponent.ImageClass.NIO_IMAGE_BUFFER)) {     
            throw new IllegalArgumentException(J3dI18N.getString("Background14"));
        }

        // Do illegal sharing check
        if(image != null) {
            ImageComponent2DRetained imageRetained = (ImageComponent2DRetained) image.retained;
            if(imageRetained.getUsedByOffScreen()) {
                if(isLive()) {
                    throw new IllegalSharingException(J3dI18N.getString("Background12"));
                }
                if(bgRetained.getInImmCtx()) {
                    throw new IllegalSharingException(J3dI18N.getString("Background13"));
                }
            }
        }
        
	if (isLive())
	    bgRetained.setImage(image);
	else
	    bgRetained.initImage(image);
    }

    /**
     * Retrieves the background image.
     * @return the current background image
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ImageComponent2D getImage() {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_IMAGE_READ))
           throw new CapabilityNotSetException(J3dI18N.getString("Background4"));

	return ((BackgroundRetained)this.retained).getImage();
    }

    /**
     * Sets the image scale mode for this Background node.
     *
     * @param imageScaleMode the new image scale mode, one of:
     * SCALE_NONE, SCALE_FIT_MIN, SCALE_FIT_MAX, SCALE_FIT_ALL,
     * SCALE_REPEAT, or SCALE_NONE_CENTER.
     *
     * @exception IllegalArgumentException if <code>imageScaleMode</code>
     * is a value other than SCALE_NONE, SCALE_FIT_MIN, SCALE_FIT_MAX,
     * SCALE_FIT_ALL, SCALE_REPEAT, or SCALE_NONE_CENTER.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setImageScaleMode(int imageScaleMode) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_IMAGE_SCALE_MODE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Background9"));

	switch (imageScaleMode) {
	case SCALE_NONE:
	case SCALE_FIT_MIN:
	case SCALE_FIT_MAX:
	case SCALE_FIT_ALL:
	case SCALE_REPEAT:
	case SCALE_NONE_CENTER:
	    break;
	default:
	    throw new IllegalArgumentException(J3dI18N.getString("Background11"));
	}
	
	if (isLive())
	    ((BackgroundRetained)this.retained).setImageScaleMode(imageScaleMode);
	else
	    ((BackgroundRetained)this.retained).initImageScaleMode(imageScaleMode);
	
    }

    /**
     * Retrieves the current image scale mode.
     * @return the current image scale mode, one of:
     * SCALE_NONE, SCALE_FIT_MIN, SCALE_FIT_MAX, SCALE_FIT_ALL,
     * SCALE_REPEAT, or SCALE_NONE_CENTER.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getImageScaleMode() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_IMAGE_SCALE_MODE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Background10"));
	return ((BackgroundRetained)this.retained).getImageScaleMode();
    }

    /**
     * Sets the background geometry to the specified BranchGroup node.
     * If non-null, this background geometry is drawn on top of
     * the background color and image using a projection
     * matrix that essentially puts the geometry at infinity.  The geometry
     * should be pre-tessellated onto a unit sphere.
     * @param branch the root of the background geometry
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalSharingException if the BranchGroup node
     * is a child of any Group node, or is already attached to a Locale,
     * or is already referenced by another Background node.
     * @exception IllegalSceneGraphException if specified branch graph
     * contains an illegal node.
     */
    public void setGeometry(BranchGroup branch) {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_GEOMETRY_WRITE))
           throw new CapabilityNotSetException(J3dI18N.getString("Background5"));

	if (isLive())
	    ((BackgroundRetained)this.retained).setGeometry(branch);
	else
	    ((BackgroundRetained)this.retained).initGeometry(branch);
    }

    /**
     * Retrieves the background geometry.
     * @return the BranchGroup node that is the root of the background
     *  geometry
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public BranchGroup getGeometry() {
        if (isLiveOrCompiled())
         if(!this.getCapability(ALLOW_GEOMETRY_READ))
           throw new CapabilityNotSetException(J3dI18N.getString("Background6"));

	return ((BackgroundRetained)this.retained).getGeometry();
    }

    /**
     * Set the Background's application region to the specified bounds.
     * This is used when the application bounding leaf is set to null.
     * @param region the bounds that contains the Background's new application
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setApplicationBounds(Bounds region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Background7"));

	if (isLive())
	    ((BackgroundRetained)this.retained).setApplicationBounds(region);
	else
	    ((BackgroundRetained)this.retained).initApplicationBounds(region);
    }

    /**  
     * Retrieves the Background node's application bounds.
     * @return this Background's application bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public Bounds getApplicationBounds() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Background8"));

	return ((BackgroundRetained)this.retained).getApplicationBounds();
    }

    /**
     * Set the Background's application region to the specified bounding leaf.
     * When set to a value other than null, this overrides the application
     * bounds object.
     * @param region the bounding leaf node used to specify the Background
     * node's new application region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setApplicationBoundingLeaf(BoundingLeaf region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Background7"));

	if (isLive())
	    ((BackgroundRetained)this.retained).setApplicationBoundingLeaf(region);
	else
	    ((BackgroundRetained)this.retained).initApplicationBoundingLeaf(region);
    }

    /**  
     * Retrieves the Background node's application bounding leaf.
     * @return this Background's application bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public BoundingLeaf getApplicationBoundingLeaf() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_APPLICATION_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Background8"));

	return ((BackgroundRetained)this.retained).getApplicationBoundingLeaf();
    }

    /**
     * Creates the retained mode BackgroundRetained object that this
     * Background component object will point to.
     */
    void createRetained() {
        this.retained = new BackgroundRetained();
        this.retained.setSource(this);
    }

 
   /**
     * Creates a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.<br>
     * Background geometry will not clone in this operation. 
     * It is the user's responsibility
     * to call <code>cloneTree</code> on that branchGroup.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        Background b = new Background();
        b.duplicateNode(this, forceDuplicate);
        return b;
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
     * @exception ClassCastException if originalNode is not an instance of 
     *  <code>Background</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean
			      forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }


   /**
     * Copies all Background information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P> 
     *
     * @param originalNode the original node to duplicate
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

	BackgroundRetained attr = (BackgroundRetained) originalNode.retained;
	BackgroundRetained rt = (BackgroundRetained) retained;

	Color3f c = new Color3f();
	attr.getColor(c);
	rt.initColor(c);
	rt.initApplicationBounds(attr.getApplicationBounds());
	rt.initGeometry(attr.getGeometry());
	// issue # 563: add call to cloneTree()
	rt.initGeometry((BranchGroup) (attr.getGeometry() == null ? null : attr.getGeometry().cloneTree(true)));
	rt.initImage((ImageComponent2D) getNodeComponent(
					     attr.getImage(),
					     forceDuplicate, 
					     originalNode.nodeHashtable));

	// this will be updated in updateNodeReferences
	rt.initApplicationBoundingLeaf(attr.getApplicationBoundingLeaf());
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
     *  new object instances
     *
     * @see NodeReferenceTable
     * @see Node#cloneTree
     * @see DanglingReferenceException
     */
    public void updateNodeReferences(NodeReferenceTable referenceTable) {
        super.updateNodeReferences(referenceTable);

	BackgroundRetained rt = (BackgroundRetained) retained;
	BoundingLeaf bl=  rt.getApplicationBoundingLeaf();

        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.initApplicationBoundingLeaf((BoundingLeaf) o);
        }
    }
}
