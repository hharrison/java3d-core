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

/**
 * Texture3D is a subclass of Texture class. It extends Texture
 * class by adding a third coordinate, constructor and a mutator
 * method for setting a 3D texture image.
 * If 3D texture mapping is not supported on a particular Canvas3D,
 * 3D texture mapping is ignored for that canvas.
 *
 * <p>
 * Note that as of Java 3D 1.5, the texture width, height, and depth
 * are no longer
 * required to be an exact power of two. However, not all graphics devices
 * supports non-power-of-two textures. If non-power-of-two texture mapping is
 * unsupported on a particular Canvas3D, textures with a width, height,
 * or depth that are not an exact power of two are ignored for that canvas.
 *
 * @see Canvas3D#queryProperties
 */

public class Texture3D extends Texture {

    // TODO KCR : NPOT

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
     * Note that a texture with a non-power-of-two width, height, or depth will
     * only be rendered on a graphics device that supports non-power-of-two
     * textures.
     *
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0.
     * @param height height of image at level 0.
     * @param depth depth of image at level 0.
     * @exception IllegalArgumentException if width, height, or depth are not
     * greater than 0 OR invalid format/mipmapMode is specified.
     */
    public Texture3D(int	mipmapMode,
		     int	format,
		     int	width,
		     int	height,
		     int	depth) {

	super(mipmapMode, format, width, height);

        int  depthLevels = -1;

        depthLevels = getLevelsNPOT(depth);

        // TODO : Need to verify whether this is a bug. Why depthLevels isn't
        // use to determine maxMipMapLevels ? See also Texture.java

	((Texture3DRetained)this.retained).setDepth(depth);
    }

    /**
     * Constructs an empty Texture3D object with specified mipmapMode
     * format, width, height, depth, and boundaryWidth.
     * Image at base level must be set by
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through
     * maximum level must be set.
     * Note that a texture with a non-power-of-two width, height, or depth will
     * only be rendered on a graphics device that supports non-power-of-two
     * textures.
     *
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0. This
     * does not include the width of the boundary.
     * @param height height of image at level 0. This
     * does not include the width of the boundary.
     * @param depth depth of image at level 0. This
     * does not include the width of the boundary.
     * @param boundaryWidth width of the boundary, which must be 0 or 1.
     * @exception IllegalArgumentException if width, height, or depth are not
     * greater than 0 OR invalid format/mipmapMode is specified, or
     * if the boundaryWidth is &lt; 0 or &gt; 1
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
        int  depthLevels = -1;

        depthLevels = getLevelsNPOT(depth);

        // TODO : Need to verify whether this is a bug. Why depthLevels isn't
        // use to determine maxMipMapLevels ? See also Texture.java

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
    @Override
    void createRetained() {
	this.retained = new Texture3DRetained();
	this.retained.setSource(this);
    }

   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
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
    @Override
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
    @Override
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	((Texture3DRetained) retained).initBoundaryModeR(((Texture3DRetained)
			  originalNodeComponent.retained).getBoundaryModeR());

    }

}
