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

import javax.vecmath.*;

/**
 * TextureCubeMap is a subclass of Texture class. It defines
 * a special kind of texture mapping which is composed of a set of six
 * 2D images representating the six faces of a cube. The texture coordinate
 * (s,t,r) is used as a 3D direction vector emanating from the center
 * of a cube to select a particular face of the cube based on the
 * largest magnitude coordinate (the major axis). A new 2D texture coordinate
 * (s,t) is then determined by dividing the other two coordinates (the minor
 * axes) by the major axis value. The new coordinate is then used for
 * texel lookup from the selected texture image of this cube map.
 *
 * The TextureCubeMap image is defined by specifying the images for each
 * face of the cube. The cube map texture can be thought of as centered at
 * the orgin of and aligned to an XYZ coordinate system. The names 
 * of the cube faces are:
 *
 * <UL>
 * <LI>POSITIVE_X</LI>
 * <LI>NEGATIVE_X</LI>
 * <LI>POSITIVE_Y</LI>
 * <LI>NEGATIVE_Y</LI>
 * <LI>POSITIVE_Z</LI>
 * <LI>NEGATIVE_Z</LI>
 * </UL> 
 *
 * @since Java 3D 1.3
 * @see Canvas3D#queryProperties
 */
public class TextureCubeMap extends Texture {

    /**
     * Specifies the face of the cube that is pierced by the positive x axis 
     */
    public static final int POSITIVE_X = 0;

    /**
     * Specifies the face of the cube that is pierced by the negative x axis 
     */
    public static final int NEGATIVE_X = 1;
 
    /**
     * Specifies the face of the cube that is pierced by the positive y axis 
     */
    public static final int POSITIVE_Y = 2;

    /**
     * Specifies the face of the cube that is pierced by the negative y axis 
     */
    public static final int NEGATIVE_Y = 3;

    /**
     * Specifies the face of the cube that is pierced by the positive z axis 
     */
    public static final int POSITIVE_Z = 4;
 
    /**
     * Specifies the face of the cube that is pierced by the negative z axis 
     */
    public static final int NEGATIVE_Z = 5;


    /**
     * Constructs a texture object using default values.
     * Note that the default constructor creates a texture object with 
     * a width of 0 and is, therefore, not useful.
     */
    public TextureCubeMap() {
    	super();
    }

    /**
     * Constructs an empty TextureCubeMap object with specified mipmapMode
     * format, and width. Image at base level 
     * must be set by 
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through maximum level
     * must be set.
     * Note that cube map is square in dimensions, hence specifying width 
     * is sufficient.
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0. Must be power of 2.
     * @exception IllegalArgumentException if width is NOT
     * power of 2 OR invalid format/mipmapMode is specified.
     */
    public TextureCubeMap(
        int     mipmapMode,
        int     format,
        int     width){

        super(mipmapMode, format, width, width);
    }

    /**
     * Constructs an empty TextureCubeMap object with specified mipmapMode
     * format, width, and boundary width. Image at base level 
     * must be set by 
     * the application using 'setImage' method. If mipmapMode is
     * set to MULTI_LEVEL_MIPMAP, images for base level through maximum level
     * must be set.
     * Note that cube map is square in dimensions, hence specifying width 
     * is sufficient.
     * @param mipmapMode type of mipmap for this Texture: One of
     * BASE_LEVEL, MULTI_LEVEL_MIPMAP.
     * @param format data format of Textures saved in this object.
     * One of INTENSITY, LUMINANCE, ALPHA, LUMINANCE_ALPHA, RGB, RGBA.
     * @param width width of image at level 0. Must be power of 2.
     * @param boundaryWidth width of the boundary.
     *
     * @exception IllegalArgumentException if width is NOT
     * power of 2 OR invalid format/mipmapMode is specified.
     */
    public TextureCubeMap(
        int     mipmapMode,
        int     format,
        int     width,
        int     boundaryWidth){

        super(mipmapMode, format, width, width, boundaryWidth);
    }

    /**
     * Sets the image for a specified mipmap level of a specified face
     * of the cube map
     *
     * @param level mipmap level
     * @param face face of the cube map. One of: 
     * <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>,     
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     * @param image ImageComponent2D object containing the image
     *
     * @exception IllegalArgumentException if 
     * <code>face</code> has a value other
     * than <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>, 
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     */
    public void setImage(int level, int face, ImageComponent2D image) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_WRITE))
            throw new CapabilityNotSetException(
			J3dI18N.getString("TextureCubeMap1"));
        }

        if (isLive())
            ((TextureCubeMapRetained)this.retained).setImage(level, face, image);
        else
            ((TextureCubeMapRetained)this.retained).initImage(level, face, image);
    }

    /**
     * Sets the array of images for mipmap levels from base level through
     * max level for a specified face of the cube map
     *
     * @param face face of the cube map. One of: 
     * <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>,     
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     * @param images array of ImageComponent2D objects containing the images
     *
     * @exception IllegalArgumentException if 
     * <code>face</code> has a value other
     * than <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>, 
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     */
    public void setImages(int face, ImageComponent2D[] images) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_WRITE))
            throw new CapabilityNotSetException(
                        J3dI18N.getString("TextureCubeMap1"));
        }

        if (isLive())
            ((TextureCubeMapRetained)this.retained).setImages(face, images);
        else
            ((TextureCubeMapRetained)this.retained).initImages(face, images);

    }


    /**
     * Retrieves the image for a specified mipmap level of a particular
     * face of the cube map.
     * @param level mipmap level to get.
     * @param face face of the cube map. One of: 
     * <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>,     
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     * @return the ImageComponent object containing the texture image at
     * the specified mipmap level.
     *
     * @exception IllegalArgumentException if 
     * <code>face</code> has a value other
     * than <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>, 
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ImageComponent getImage(int level, int face) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_READ))
            throw new CapabilityNotSetException(
			J3dI18N.getString("TextureCubeMap2"));
        }

        return ((TextureCubeMapRetained)this.retained).getImage(level, face);
    }

    /**
     * Retrieves the array of images for all mipmap level of a particular
     * face of the cube map.
     * @param face face of the cube map. One of:
     * <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>,
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     * @return an array of ImageComponent object for the particular face of
     * of the cube map.
     *
     * @exception IllegalArgumentException if 
     * <code>face</code> has a value other
     * than <code>POSITIVE_X</code>, <code>NEGATIVE_X</code>, 
     * <code>POSITIVE_Y</code>, <code>NEGATIVE_Y</code>,
     * <code>POSITIVE_Z</code> or <code>NEGATIVE_Z</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ImageComponent[] getImages(int face) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_READ))
            throw new CapabilityNotSetException(
                        J3dI18N.getString("TextureCubeMap2"));
        }

        return ((TextureCubeMapRetained)this.retained).getImages(face);
    }


    /**
     * This method is not supported for TextureCubeMap.
     * A face of the cube map has to be specified when setting an image
     * for a particular level of the cube map.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setImage(int level, ImageComponent image) {
	throw new UnsupportedOperationException();
    }


    /**
     * This method is not supported for TextureCubeMap.
     * A face of the cube map has to be specified when setting images
     * for the cube map.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public void setImages(ImageComponent[] images) {
	throw new UnsupportedOperationException();
    }

    /**
     * This method is not supported for TextureCubeMap.
     * A face of the cube map has to be specified when retrieving an image
     * for a particular level of the cube map.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public ImageComponent getImage(int level) {
        throw new UnsupportedOperationException();
    }


    /**
     * This method is not supported for TextureCubeMap.
     * A face of the cube map has to be specified when retrieving images
     * for the cube map.
     *
     * @exception UnsupportedOperationException this method is not supported
     *
     * @since Java 3D 1.3
     */
    public ImageComponent[] getImages() {
        throw new UnsupportedOperationException();
    }


    /**
     * Creates a retained mode TextureCubeMapRetained object that this
     * TextureCubeMap component object will point to.
     */
    void createRetained() {
    	this.retained = new TextureCubeMapRetained();
    	this.retained.setSource(this);
    }

    /**
     * NOTE: Applications should not call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated replaced with duplicateNodeComponent(
     *  NodeComponent originalNodeComponent, boolean forceDuplicate)
     */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
    	checkDuplicateNodeComponent(originalNodeComponent);
    }
}

