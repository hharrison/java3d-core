/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * Texture3D is a subclass of Texture class. It extends Texture
 * class by adding a third coordinate, constructor and a mutator 
 * method for setting a 3D texture image.
 * If 3D texture mapping is not supported on a particular Canvas3D,
 * 3D texture mapping is ignored for that canvas.
 *
 * @see Canvas3D#queryProperties
 */

public class Texture3D extends Texture {

    /**
     * Constructs a Texture3D object with default parameters.
     * The default values are as follows:
     * <ul>
     * depth : 0<br>
     * boundary mode R : WRAP<br>
     * </ul>
     * <p>
     * Note that the default constructor creates a texture object with 
     * a width, height, and depth of 0 and is, therefore, not useful.
     */
    public Texture3D() {
	super();
    }

    /**
     * Constructs an empty Texture3D object with specified mipmapMode
     * format, width, height, and depth. Image at base level must be set by 
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through
     * maximum level must be set.
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0. Must be power of 2.
     * @param height height of image at level 0. Must be power of 2.
     * @param depth depth of image at level 0. Must be power of 2.
     * @exception IllegalArgumentException if width or height are NOT
     * power of 2 OR invalid format/mipmapMode is specified.
     */
    public Texture3D(int	mipmapMode,
		     int	format,
		     int	width,
		     int	height,
		     int	depth) {

	super(mipmapMode, format, width, height);
	int  depthPower = getPowerOf2(depth);
	if (depthPower == -1)
	    throw new IllegalArgumentException(J3dI18N.getString("Texture3D1"));

	((Texture3DRetained)this.retained).setDepth(depth);
    }

    /**
     * Constructs an empty Texture3D object with specified mipmapMode
     * format, width, height, depth, and boundaryWidth. 
     * Image at base level must be set by 
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through
     * maximum level must be set.
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0. Must be power of 2.
     * @param height height of image at level 0. Must be power of 2.
     * @param depth depth of image at level 0. Must be power of 2.
     * @param boundaryWidth width of the boundary.
     * @exception IllegalArgumentException if width or height are NOT
     * power of 2 OR invalid format/mipmapMode is specified, or
     * if the boundaryWidth < 0
     *
     * @since Java 3D 1.3
     */
    public Texture3D(int	mipmapMode,
		     int	format,
		     int	width,
		     int	height,
		     int	depth,
		     int	boundaryWidth) {

	super(mipmapMode, format, width, height, boundaryWidth);
	int  depthPower = getPowerOf2(depth);
	if (depthPower == -1)
	    throw new IllegalArgumentException(J3dI18N.getString("Texture3D1"));

	((Texture3DRetained)this.retained).setDepth(depth);
    }

    /**
     * Sets the boundary mode for the R coordinate in this texture object.
     * @param boundaryModeR the boundary mode for the R coordinate,
     * one of: CLAMP, WRAP, CLAMP_TO_EDGE, or CLAMP_TO_BOUNDARY
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     * @exception IllegalArgumentException if <code>boundaryModeR</code>
     * is a value other than <code>CLAMP</code>, <code>WRAP</code>,
     * <code>CLAMP_TO_EDGE</code>, or <code>CLAMP_TO_BOUNDARY</code>.
     */
    public void setBoundaryModeR(int boundaryModeR) {
	checkForLiveOrCompiled();
        switch (boundaryModeR) {
        case Texture.CLAMP:
        case Texture.WRAP:
        case Texture.CLAMP_TO_EDGE:
        case Texture.CLAMP_TO_BOUNDARY:
            break;
        default:
            throw new IllegalArgumentException(J3dI18N.getString("Texture31"));
        }
	((Texture3DRetained)this.retained).initBoundaryModeR(boundaryModeR);
    }

    /**
     * Retrieves the boundary mode for the R coordinate.
     * @return the current boundary mode for the R coordinate.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public int getBoundaryModeR() {
        if (isLiveOrCompiled())
            if(!this.getCapability(Texture.ALLOW_BOUNDARY_MODE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Texture3D0"));
	return ((Texture3DRetained)this.retained).getBoundaryModeR();
    }

    /**
     * Retrieves the depth of this Texture3D object.
     * @return the depth of this Texture3D object.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getDepth() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Texture3D2"));

	return ((Texture3DRetained)this.retained).getDepth();
    }

    /**
     * Creates a retained mode Texture3DRetained object that this
     * Texture3D component object will point to.
     */
    void createRetained() {
	this.retained = new Texture3DRetained();
	this.retained.setSource(this);
    }

   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	Texture3DRetained t3d = (Texture3DRetained) retained;
	Texture3D t = new Texture3D(t3d.getMipMapMode(), t3d.format,
				    t3d.width, t3d.height, t3d.depth);
        t.duplicateNodeComponent(this);
        return t;
    }


    /**
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated replaced with duplicateNodeComponent(
     *  NodeComponent originalNodeComponent, boolean forceDuplicate)
     */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
	checkDuplicateNodeComponent(originalNodeComponent);
    }


   /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent, 
			     boolean forceDuplicate) { 
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	((Texture3DRetained) retained).initBoundaryModeR(((Texture3DRetained)
			  originalNodeComponent.retained).getBoundaryModeR());

    }

}
