/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.*;


/**
 * Texture2D is a subclass of Texture class. It extends Texture
 * class by adding a constructor and a mutator method for
 * setting a 2D texture image.
 * <p>
 * Note that as of Java 3D 1.5, the texture width and height are no longer
 * required to be an exact power of two. However, not all graphics devices
 * supports non-power-of-two textures. If non-power-of-two texture mapping is
 * unsupported on a particular Canvas3D, textures with a width or height that
 * are not an exact power of two are ignored for that canvas.
 *
 * @see Canvas3D#queryProperties
 */
public class Texture2D extends Texture {

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Specifies that this Texture object allows reading its detail
     * texture information (e.g., detail texture image, detail texture mode,
     * detail texture function, detail texture function points count,
     * detail texture level)
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_DETAIL_TEXTURE_READ = CapabilityBits.TEXTURE2D_ALLOW_DETAIL_TEXTURE_READ;

    /** 
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Performs linear sampling in both the base level
     * texture image and the detail texture image, and combines the two
     * texture values according to the detail texture mode.
     *
     * @since Java 3D 1.3
     * @see #setMagFilter
     */
    public static final int LINEAR_DETAIL         = 6;

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Performs linear detail for the rgb
     * components only. The alpha component is computed using 
     * BASE_LEVEL_LINEAR filter.
     *
     * @since Java 3D 1.3
     * @see #setMagFilter
     */
    public static final int LINEAR_DETAIL_RGB     = 7;

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Performs linear detail for the alpha
     * component only. The rgb components are computed using 
     * BASE_LEVEL_LINEAR filter.
     *
     * @since Java 3D 1.3
     * @see #setMagFilter
     */
    public static final int LINEAR_DETAIL_ALPHA   = 8;

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Adds the detail texture image to the level 0 image of this texture
     * object
     *
     * @since Java 3D 1.3
     * @see #setDetailTextureMode
     */
    public static final int DETAIL_ADD = 0;

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     * Modulates the detail texture image with the level 0 image of this
     * texture object
     *
     * @since Java 3D 1.3
     * @see #setDetailTextureMode
     */
    public static final int DETAIL_MODULATE = 1;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_DETAIL_TEXTURE_READ
    };

    /**
     * Constructs a texture object using default values.
     *
     * The default values are as follows:
     * <ul>
     * detail texture image: null<br>
     * detail texture mode: DETAIL_MODULATE<br>
     * detail texture func: null<br>
     * detail texture level: 2<br>
     * </ul>
     * <p>
     * Note that the default constructor creates a texture object with
     * a width and height of 0 and is, therefore, not useful.
     */
    public Texture2D() {
	super();
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

    }

    /**
     * Constructs an empty Texture2D object with specified mipmapMode
     * format, width and height. Image at base level must be set by
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through maximum level
     * must be set.
     * Note that a texture with a non-power-of-two width or height will
     * only be rendered on a graphics device that supports non-power-of-two
     * textures.
     *
     * @param mipMapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0.
     * @param height height of image at level 0.
     * @exception IllegalArgumentException if width or height are NOT
     * greater than 0 OR invalid format/mipmapMode is specified.
     */
    public Texture2D(
            int		mipMapMode,
            int		format,
            int		width,
            int		height) {

	super(mipMapMode, format, width, height);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }


    /**
     * Constructs an empty Texture2D object with specified mipMapMode,
     * format, width, height, and boundaryWidth.
     * Defaults are used for all other
     * parameters.  If <code>mipMapMode</code> is set to
     * <code>BASE_LEVEL</code>, then the image at level 0 must be set
     * by the application (using either the <code>setImage</code> or
     * <code>setImages</code> method). If <code>mipMapMode</code> is
     * set to <code>MULTI_LEVEL_MIPMAP</code>, then images for levels
     * Base Level through Maximum Level must be set.
     * Note that a texture with a non-power-of-two width or height will
     * only be rendered on a graphics device that supports non-power-of-two
     * textures.
     *
     * @param mipMapMode type of mipmap for this Texture: one of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA
     * @param width width of image at level 0. This
     * does not include the width of the boundary.
     * @param height height of image at level 0. This
     * does not include the width of the boundary.
     * @param boundaryWidth width of the boundary, which must be 0 or 1.
     * @exception IllegalArgumentException if width or height are not greater
     * than 0, if an invalid format or mipMapMode is specified, or
     * if the boundaryWidth is &lt; 0 or &gt; 1
     *
     * @since Java 3D 1.3
     */
    public Texture2D(int          mipMapMode,
                   int          format,
                   int          width,
                   int          height,
                   int          boundaryWidth) {

        super(mipMapMode, format, width, height, boundaryWidth);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Sets the magnification filter function.  This
     * function is used when the pixel being rendered maps to an area
     * less than or equal to one texel.
     * @param magFilter the magnification filter, one of:
     * FASTEST, NICEST, BASE_LEVEL_POINT, BASE_LEVEL_LINEAR, 
     * LINEAR_DETAIL, LINEAR_DETAIL_RGB, LINEAR_DETAIL_ALPHA,
     * LINEAR_SHARPEN, LINEAR_SHARPEN_RGB, LINEAR_SHARPEN_ALPHA, or FILTER4.
     *
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     * @exception IllegalArgumentException if <code>minFilter</code>
     * is a value other than <code>FASTEST</code>, <code>NICEST</code>,
     * <code>BASE_LEVEL_POINT</code>, <code>BASE_LEVEL_LINEAR</code>,
     * <code>LINEAR_DETAIL</code>, <code>LINEAR_DETAIL_RGB</code>, 
     * <code>LINEAR_DETAIL_ALPHA</code>, 
     * <code>LINEAR_SHARPEN</code>, <code>LINEAR_SHARPEN_RGB</code>, 
     * <code>LINEAR_SHARPEN_ALPHA</code>,  or
     * <code>FILTER4</code>.
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setMagFilter(int magFilter) {
	checkForLiveOrCompiled();

	switch (magFilter) {
	case FASTEST:
	case NICEST:
	case BASE_LEVEL_POINT:
	case BASE_LEVEL_LINEAR:
	case LINEAR_DETAIL:
	case LINEAR_DETAIL_RGB:
	case LINEAR_DETAIL_ALPHA:
	case LINEAR_SHARPEN:
	case LINEAR_SHARPEN_RGB:
	case LINEAR_SHARPEN_ALPHA:
	case FILTER4:
	    break;
	default:
	    throw new IllegalArgumentException(J3dI18N.getString("Texture29"));
	}

	((Texture2DRetained)this.retained).initMagFilter(magFilter);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param detailTexture ImageComponent2D object containing the
     * detail texture image.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @since Java 3D 1.3
     * @see Canvas3D#queryProperties
     */
    public void setDetailImage(ImageComponent2D detailTexture) {
        checkForLiveOrCompiled();
        ((Texture2DRetained)this.retained).initDetailImage(detailTexture);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @return ImageComponent2D object containing the detail texture image.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public ImageComponent2D getDetailImage() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        return ((Texture2DRetained)this.retained).getDetailImage();
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param mode detail texture mode. One of: DETAIL_ADD or DETAIL_MODULATE
     *
     * @exception IllegalArgumentException if
     * <code>mode</code> is a value other than
     * <code>DETAIL_ADD</code>, or <code>DETAIL_MODULATE</code>
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @since Java 3D 1.3
     * @see Canvas3D#queryProperties
     */
    public void setDetailTextureMode(int mode) {
        checkForLiveOrCompiled();
        if ((mode != DETAIL_ADD) && (mode != DETAIL_MODULATE)) {
            throw new IllegalArgumentException(
                        J3dI18N.getString("Texture2D1"));
        }
        ((Texture2DRetained)this.retained).initDetailTextureMode(mode);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @return the detail texture mode.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getDetailTextureMode() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        return ((Texture2DRetained)this.retained).getDetailTextureMode();
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param level the detail texture level.
     *
     * @exception IllegalArgumentException if <code>level</code> < 0
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @since Java 3D 1.3
     * @see Canvas3D#queryProperties
     */
    public void setDetailTextureLevel(int level) {
        checkForLiveOrCompiled();
        if (level < 0) {
            throw new IllegalArgumentException(
                        J3dI18N.getString("Texture2D2"));
        }
        ((Texture2DRetained)this.retained).initDetailTextureLevel(level);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @return the detail texture level.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getDetailTextureLevel() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        return ((Texture2DRetained)this.retained).getDetailTextureLevel();
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param lod array containing the level-of-detail values.
     * @param pts array containing the function values for the corresponding
     * level-of-detail values.
     *
     * @exception IllegalStateException if the length of <code>lod</code>
     * does not match the length of <code>pts</code>
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @since Java 3D 1.3
     * @see Canvas3D#queryProperties
     */
    public void setDetailTextureFunc(float[] lod, float[] pts) {
        checkForLiveOrCompiled();
        if (((lod != null) && (pts != null) && (lod.length == pts.length)) ||
             ((lod == null) && (pts == null))) {
            ((Texture2DRetained)this.retained).initDetailTextureFunc(lod, pts);
        } else {
            throw new IllegalStateException(J3dI18N.getString("Texture2D3"));
        }
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param pts array of Point2f containing the lod as well as the
     * corresponding function value.
     *
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     *
     * @since Java 3D 1.3
     * @see Canvas3D#queryProperties
     */
    public void setDetailTextureFunc(Point2f[] pts) {
        checkForLiveOrCompiled();
        ((Texture2DRetained)this.retained).initDetailTextureFunc(pts);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @return the number of points in the detail texture LOD function.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getDetailTextureFuncPointsCount() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        return ((Texture2DRetained)this.retained).getDetailTextureFuncPointsCount();
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param lod the array to receive the level-of-detail values.
     * @param pts the array to receive the function values for the
     * corresponding level-of-detail values.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void getDetailTextureFunc(float[] lod, float[] pts) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        ((Texture2DRetained)this.retained).getDetailTextureFunc(lod, pts);
    }

    /**
     * @deprecated As of Java 3D 1.5 the optional detail texture feature is no
     * longer supported.
     *
     * @param pts the array to receive the detail texture LOD function points
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void getDetailTextureFunc(Point2f[] pts) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_DETAIL_TEXTURE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("Texture2D0"));
            }
        }
        ((Texture2DRetained)this.retained).getDetailTextureFunc(pts);
    }


    /**
     * Creates a retained mode Texture2DRetained object that this
     * Texture2D component object will point to.
     */
    void createRetained() {
	this.retained = new Texture2DRetained();
	this.retained.setSource(this);
    }



    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)  
     */
    public NodeComponent cloneNodeComponent() {
	Texture2DRetained t2d = (Texture2DRetained) retained;

	Texture2D t = new Texture2D(t2d.getMipMapMode(), t2d.format, 
				    t2d.width, t2d.height);
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

	Texture2DRetained tex = (Texture2DRetained) 
					originalNodeComponent.retained;
	Texture2DRetained rt = (Texture2DRetained) retained;


	rt.initDetailImage(tex.getDetailImage());
	rt.initDetailTextureMode(tex.getDetailTextureMode());
	rt.initDetailTextureLevel(tex.getDetailTextureLevel());
	rt.initDetailTextureFunc(tex.getDetailTextureFunc());
    }
}


