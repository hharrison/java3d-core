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

import java.awt.image.BufferedImage;
import java.util.*;
import javax.vecmath.*;
import java.awt.image.DataBufferByte;

/**
 * The Texture object is a component object of an Appearance object
 * that defines the texture properties used when texture mapping is
 * enabled. Texture object is an abstract class and all texture 
 * objects must be created as either a Texture2D object or a
 * Texture3D object.
 */
abstract class TextureRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this Texture object changed.
    static final int ENABLE_CHANGED      = 0x001;
    static final int COLOR_CHANGED       = 0x002;
    static final int IMAGE_CHANGED       = 0x004;
    static final int STATE_CHANGED       = 0x008;
    static final int UPDATE_IMAGE        = 0x010;
    static final int IMAGES_CHANGED      = 0x020;
    static final int BASE_LEVEL_CHANGED  = 0x040;
    static final int MAX_LEVEL_CHANGED   = 0x080;
    static final int MIN_LOD_CHANGED     = 0x100;
    static final int MAX_LOD_CHANGED     = 0x200;
    static final int LOD_OFFSET_CHANGED  = 0x400;

    // constants for min and mag filter
    static final int MIN_FILTER = 0;
    static final int MAG_FILTER = 1;

    // Boundary width
    int		boundaryWidth = 0;

    // Boundary modes (wrap, clamp, clamp_to_edge, clamp_to_boundary)
    int		boundaryModeS = Texture.WRAP;
    int		boundaryModeT = Texture.WRAP;

    // Filter modes
    int		minFilter = Texture.BASE_LEVEL_POINT;
    int		magFilter = Texture.BASE_LEVEL_POINT;

    // Integer flag that contains bitset to indicate 
    // which field changed.
    int isDirty = 0xffff;

    // Texture boundary color
    Color4f	boundaryColor = new Color4f(0.0f, 0.0f, 0.0f, 0.0f);

    // Texture Object Id used by native code.
    int 	objectId = -1;

    int		mipmapMode = Texture.BASE_LEVEL; // Type of mip-mapping 
    int		format = Texture.RGB;		// Texture format
    int		width = 1;			// Width in pixels (2**n)
    int		height = 1;			// Height in pixels (2**m)

    ImageComponentRetained images[][];	// Array of images (one for each mipmap level)
    boolean	imagesLoaded = false;	// TRUE if all mipmap levels are loaded
    int         mipmapLevels;        // Number of MIPMAP levels needed
    int		maxLevels = 0;       // maximum number of levels needed for
				     // the mipmapMode of this texture
    int		maxMipMapLevels = 0; // maximum number of mipmap levels that 
				     // can be defined for this texture

    int 	numFaces = 1;		// For CubeMap, it is 6

    int		baseLevel = 0;
    int		maximumLevel = 0;
    float	minimumLod = -1000.0f;
    float	maximumLod = 1000.0f;
    Point3f	lodOffset = null;


    // Texture mapping enable switch
    // This enable is derived from the user specified enable
    // and whether the buf image in the imagecomp is null
    boolean	enable = true;

    // User specified enable
    boolean userSpecifiedEnable = true;


    // true if alpha channel need update during rendering
    boolean isAlphaNeedUpdate = false;

    // sharpen texture info
    int numSharpenTextureFuncPts = 0;
    float sharpenTextureFuncPts[] = null;  // array of pairs of floats
					   // first value for LOD
					   // second value for the fcn value	
    
    // filter4 info
    float filter4FuncPts[] = null;

    // anisotropic filter info
    int anisotropicFilterMode = Texture.ANISOTROPIC_NONE;
    float anisotropicFilterDegree = 1.0f;


    // Each bit corresponds to a unique renderer if shared context
    // or a unique canvas otherwise.
    // This mask specifies which renderer/canvas has loaded the
    // texture images. 0 means no renderer/canvas has loaded the texture.
    // 1 at the particular bit means that renderer/canvas has loaded the
    // texture. 0 means otherwise.
    int resourceCreationMask = 0x0;

    // Each bit corresponds to a unique renderer if shared context
    // or a unique canvas otherwise
    // This mask specifies if texture images are up-to-date.
    // 0 at a particular bit means texture images are not up-to-date.
    // 1 means otherwise. If it specifies 0, then it needs to go
    // through the imageUpdateInfo to update the images accordingly.
    // 
    int resourceUpdatedMask = 0x0; 

    // Each bit corresponds to a unique renderer if shared context
    // or a unique canvas otherwise
    // This mask specifies if texture lod info is up-to-date.
    // 0 at a particular bit means texture lod info is not up-to-date.
    // 1 means otherwise. 
    // 
    int resourceLodUpdatedMask = 0x0; 

    // Each bit corresponds to a unique renderer if shared context
    // or a unique canvas otherwise
    // This mask specifies if texture is in the resource reload list
    // 0 at a particular bit means texture is not in reload list
    // 1 means otherwise. 
    // 
    int resourceInReloadList = 0x0; 

    // image update info
    ArrayList imageUpdateInfo[][];


    int imageUpdatePruneMask[];


    // textureBin reference counter
    int textureBinRefCount = 0;

    // This is used for D3D only to check whether texture need to
    // resend down
    private int texTimestamp = 0;
 
    // need to synchronize access from multiple rendering threads 
    Object resourceLock = new Object();


    void initialize(int	format, int width, int widLevels, 
			int height, int heiLevels, int mipmapMode,
			int boundaryWidth) {

	this.mipmapMode = mipmapMode;
	this.format = format;
	this.width = width;
	this.height = height;
	this.boundaryWidth = boundaryWidth;

	// determine the maximum number of mipmap levels that can be
	// defined from the specified dimension

        if (widLevels > heiLevels) {
            maxMipMapLevels = widLevels + 1;
        } else {
            maxMipMapLevels = heiLevels + 1;
	}


	// determine the maximum number of mipmap levels that will be
	// needed with the current mipmapMode

        if (mipmapMode != Texture.BASE_LEVEL) {
	    baseLevel = 0;
	    maximumLevel = maxMipMapLevels - 1;
	    maxLevels = maxMipMapLevels;
        } else {
	    baseLevel = 0;
	    maximumLevel = 0;
	    maxLevels = 1;
	}

        images = new ImageComponentRetained[numFaces][maxLevels];

	for (int j = 0; j < numFaces; j++) {
	    for (int i = 0; i < maxLevels; i++) {
	       images[j][i] = null;
	    }
	}
	imagesLoaded = false;

    }

    final int getFormat() {
	return this.format;
    }

    final int getWidth() {
   	return this.width;
    }

    final int getHeight() {
	return this.height;
    }

    final int numMipMapLevels() {
	return (maximumLevel - baseLevel + 1);
    }

    /**
     * Sets the boundary mode for the S coordinate in this texture object.
     * @param boundaryModeS the boundary mode for the S coordinate,
     * one of: CLAMP or WRAP.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final void initBoundaryModeS(int boundaryModeS) {
	this.boundaryModeS = boundaryModeS;
    }

    /**
     * Retrieves the boundary mode for the S coordinate.
     * @return the current boundary mode for the S coordinate.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final int getBoundaryModeS() {
	return  boundaryModeS;
    }

    /**
     * Sets the boundary mode for the T coordinate in this texture object.
     * @param boundaryModeT the boundary mode for the T coordinate,
     * one of: CLAMP or WRAP.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final void initBoundaryModeT(int boundaryModeT) {
	this.boundaryModeT = boundaryModeT;
    }

    /**
     * Retrieves the boundary mode for the T coordinate.
     * @return the current boundary mode for the T coordinate.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final int getBoundaryModeT() {
	return  boundaryModeT;
    }

    /**
     * Retrieves the boundary width.
     * @return the boundary width of this texture.
     */
    final int getBoundaryWidth() {
	return  boundaryWidth;
    }

    /**
     * Sets the minification filter function.  This
     * function is used when the pixel being rendered maps to an area
     * greater than one texel.
     * @param minFilter the minification filter, one of:
     * FASTEST, NICEST, BASE_LEVEL_POINT, BASE_LEVEL_LINEAR, 
     * MULTI_LEVEL_POINT, MULTI_LEVEL_LINEAR.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final void initMinFilter(int minFilter) {
	this.minFilter = minFilter;
    }

    /**
     * Retrieves the minification filter.
     * @return the current minification filter function.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final int getMinFilter() {
	return  minFilter;
    }

    /**
     * Sets the magnification filter function.  This
     * function is used when the pixel being rendered maps to an area
     * less than or equal to one texel.
     * @param magFilter the magnification filter, one of:
     * FASTEST, NICEST, BASE_LEVEL_POINT, or BASE_LEVEL_LINEAR.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final void initMagFilter(int magFilter) {
	this.magFilter = magFilter;
    }

    /**
     * Retrieves the magnification filter.
     * @return the current magnification filter function.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final int getMagFilter() {
	return  magFilter;
    }

    /**
     * Sets a specified mipmap level.
     * @param level mipmap level to set: 0 is the base level
     * @param image pixel array object containing the texture image
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     * @exception IllegalArgumentException if an ImageComponent3D
     * is used in a Texture2D or ImageComponent2D in Texture3D
     * power of 2 OR invalid format/mipmapMode is specified.
     */
    void initImage(int level, ImageComponent image) {

        // Issue 172 : call checkImageSize even for non-live setImage calls
        checkImageSize(level, image);

	if (this.images == null) {
           throw new IllegalArgumentException(J3dI18N.getString("TextureRetained0"));
	} 

        if (this.source instanceof Texture2D) {
            if (image instanceof ImageComponent3D)
               throw new IllegalArgumentException(J3dI18N.getString("Texture8"))
;
        } else {

            if (image instanceof ImageComponent2D)
               throw new IllegalArgumentException(J3dI18N.getString("Texture14")
);
        }


	if (this.source.isLive()) {

	    if (this.images[0][level] != null) {
		this.images[0][level].clearLive(refCount);
	    }

	    
	    if (image != null) {
		((ImageComponentRetained)image.retained).setLive(inBackgroundGroup, refCount);
	    }
	}

        if (this instanceof Texture2DRetained) {
	    ((ImageComponent2DRetained)image.retained).setTextureRef();
        } else {
	    ((ImageComponent3DRetained)image.retained).setTextureRef();
        }

	if (image != null) {
	    this.images[0][level] = (ImageComponentRetained)image.retained;

	} else {
	    this.images[0][level] = null;
	}
    }

    final void checkImageSize(int level, ImageComponent image) {
        if (image != null) {
	    int imgWidth  = ((ImageComponentRetained)image.retained).width;
            int imgHeight = ((ImageComponentRetained)image.retained).height;

            int wdh = width;
	    int hgt = height;
	    for (int i = 0; i < level; i++) {
                wdh >>= 1;
                hgt >>= 1;
            }

	    if (wdh < 1) wdh = 1;
	    if (hgt < 1) hgt = 1;
    
	    if ((wdh != (imgWidth - 2*boundaryWidth)) ||
                    (hgt != (imgHeight - 2*boundaryWidth))) {
	       throw new IllegalArgumentException(
				J3dI18N.getString("TextureRetained1"));
	    }
        }
    }

    final void checkSizes(ImageComponentRetained images[]) {
        // Issue 172 : this method is now redundant

        // Assertion check that the image at each level is the correct size
        // This shouldn't be needed since we already should have checked the
        // size at each level, and verified that all levels are set.
        if (images != null) {
            int hgt = height;
            int wdh = width;
            for (int level = 0; level < images.length; level++) {
                int imgWidth  = images[level].width;
                int imgHeight = images[level].height;
                
                assert (wdh == (imgWidth - 2*boundaryWidth)) &&
                       (hgt == (imgHeight - 2*boundaryWidth));

                wdh /= 2;
                hgt /= 2;
                if (wdh < 1) wdh = 1;
                if (hgt < 1) hgt = 1;
            }
        }
    }

    final void setImage(int level, ImageComponent image) {
        initImage(level, image);

        Object arg[] = new Object[3];
	arg[0] = new Integer(level);
	arg[1] = image;
        arg[2] = new Integer(0);
	sendMessage(IMAGE_CHANGED, arg);

	// If the user has set enable to true, then if the image is null
	// turn off texture enable

	if (userSpecifiedEnable) {
	    enable = userSpecifiedEnable;
            if (image != null && level >= baseLevel && level <= maximumLevel) {
		ImageComponentRetained img= (ImageComponentRetained)image.retained;
		if (img.isByReference()) {
		    if (img.bImage[0] == null) {
			enable = false;
		    }
		}
		else {
		    if (img.imageYup == null) {
			enable = false;
		    }
		}
		if (!enable) 
		    sendMessage(ENABLE_CHANGED, Boolean.FALSE);
	    }
	}
    }

    void initImages(ImageComponent[] images) {

	if (images.length != maxLevels)
            throw new IllegalArgumentException(J3dI18N.getString("Texture20"));

	for (int i = 0; i < images.length; i++) {
	     initImage(i, images[i]);
	}
    }

    final void setImages(ImageComponent[] images) {

        int i;

        initImages(images);

	ImageComponent [] imgs = new ImageComponent[images.length];
	for (i = 0; i < images.length; i++) {
	     imgs[i] = images[i];
	}

        Object arg[] = new Object[2];
	arg[0] = imgs;
	arg[1] = new Integer(0);

	sendMessage(IMAGES_CHANGED, arg);

	// If the user has set enable to true, then if the image is null
	// turn off texture enable

	if (userSpecifiedEnable) {
	    enable = userSpecifiedEnable;
	    for (i = baseLevel; i <= maximumLevel && enable; i++) {
		if (images[i] != null) {
		    ImageComponentRetained img= 
			(ImageComponentRetained)images[i].retained;
		    if (img.isByReference()) {
			if (img.bImage[0] == null) {
			    enable = false;
			}
		    }
		    else {
			if (img.imageYup == null) {
			    enable = false;
			}
		    }
		}
	    }
	    if (!enable) {
		sendMessage(ENABLE_CHANGED, Boolean.FALSE);
	    }
	}
    }

	


    /**
     * Gets a specified mipmap level.
     * @param level mipmap level to get: 0 is the base level
     * @return the pixel array object containing the texture image
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final ImageComponent getImage(int level) {
	return  (((images != null) && (images[0][level] != null)) ? 
		 (ImageComponent)images[0][level].source : null);
    }

    final ImageComponent[] getImages() {
	if (images == null)
	    return null;

	ImageComponent [] rImages = new ImageComponent[images[0].length];
	for (int i = 0; i < images[0].length; i++) {
	    if (images[0][i] != null)
	        rImages[i] = (ImageComponent)images[0][i].source;
	    else
		rImages[i] = null;
	}
	return rImages;
    }

    /**
     * Sets mipmap mode for texture mapping for this texture object.  
     * @param mipMapMode the new mipmap mode for this object.  One of:
     * BASE_LEVEL or MULTI_LEVEL_MIPMAP.
     * @exception RestrictedAccessException if the method is called
     */
    final void initMipMapMode(int mipmapMode) {

	if (this.mipmapMode == mipmapMode) 
	    return;


	int prevMaxLevels = maxLevels;		// previous maxLevels

	this.mipmapMode = mipmapMode;

        if (mipmapMode != Texture.BASE_LEVEL) {
	    maxLevels = maxMipMapLevels;
        } else {
            baseLevel = 0;
            maximumLevel = 0;
	    maxLevels = 1;
        }
	

	ImageComponentRetained[][] newImages = 
			new ImageComponentRetained[numFaces][maxLevels];

	if (prevMaxLevels < maxLevels) {
	    for (int f = 0; f < numFaces; f++) {
	        for (int i = 0; i < prevMaxLevels; i++) {
		     newImages[f][i] = images[f][i];
		}
	    
	        for (int j = prevMaxLevels; j < maxLevels; j++) {
		     newImages[f][j] = null;
		}
	    }
	} else {
	    for (int f = 0; f < numFaces; f++) {
	    	for (int i = 0; i < maxLevels; i++)
		     newImages[f][i] = images[f][i];
	    }
	}
	images = newImages;
    }

    /**
     * Retrieves current mipmap mode.
     * @return current mipmap mode of this texture object.
     * @exception RestrictedAccessException if the method is called
     */
    final int getMipMapMode() {
	return this.mipmapMode;
    }

    /**
     * Enables or disables texture mapping for this
     * appearance component object.
     * @param state true or false to enable or disable texture mapping
     */
    final void initEnable(boolean state) {
	userSpecifiedEnable = state;
    }

    /**
     * Enables or disables texture mapping for this
     * appearance component object and sends a 
     * message notifying the interested structures of the change.
     * @param state true or false to enable or disable texture mapping
     */
    final void setEnable(boolean state) {

	initEnable(state);

	if (state == enable) {
	    // if enable flag is same as user specified one
            // this is only possible when enable is false
	    // because one of the images is null and user specifies false
	    return;
	}
	    
	enable = state;

	for (int j = 0; j < numFaces && enable; j++) {
	     for (int i = baseLevel; i <= maximumLevel && enable; i++) {
	    	  if (images[j][i].isByReference()) {
		      if (images[j][i].bImage[0] == null) {
		          enable = false;
		      }
                  } else {
		      if (images[j][i].imageYup == null) {
		          enable = false;
		      }
	          }
	     }
	}
	sendMessage(ENABLE_CHANGED, (enable ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of the texture enable flag.
     * @return true if texture mapping is enabled,
     * false if texture mapping is disabled
     */
    final boolean getEnable() {
	return userSpecifiedEnable;
    }


    final void initBaseLevel(int level) {
	if ((level < 0) || (level > maximumLevel)) {
	    throw new IllegalArgumentException(
			J3dI18N.getString("Texture36"));
	}
	baseLevel = level;
    }


    final void setBaseLevel(int level) {

	if (level == baseLevel)
	    return;

	initBaseLevel(level);
	sendMessage(BASE_LEVEL_CHANGED, new Integer(level));
    }

    final int getBaseLevel() {
	return baseLevel;
    }


    final void initMaximumLevel(int level) {
	if ((level < baseLevel) || (level >=  maxMipMapLevels)) {
	    throw new IllegalArgumentException(
			J3dI18N.getString("Texture37"));
	}
	maximumLevel = level; 
    }

    final void setMaximumLevel(int level) {

	if (level == maximumLevel)
	    return;

	initMaximumLevel(level);
	sendMessage(MAX_LEVEL_CHANGED, new Integer(level));
    }

    final int getMaximumLevel() {
   	return maximumLevel;
    }

    final void initMinimumLOD(float lod) {
	if (lod > maximumLod) {
	    throw new IllegalArgumentException(
			J3dI18N.getString("Texture42"));
	}
	minimumLod = lod;
    }

    final void setMinimumLOD(float lod) {
	initMinimumLOD(lod);
	sendMessage(MIN_LOD_CHANGED, new Float(lod));
    }

    final float getMinimumLOD() {
  	return minimumLod;
    }


    final void initMaximumLOD(float lod) {
	if (lod < minimumLod) {
	    throw new IllegalArgumentException(
			J3dI18N.getString("Texture42"));
	}
	maximumLod = lod;
    }

    final void setMaximumLOD(float lod) {
	initMaximumLOD(lod);
	sendMessage(MAX_LOD_CHANGED, new Float(lod));
    }

    final float getMaximumLOD() {
  	return maximumLod;
    }


    final void initLodOffset(float s, float t, float r) {
	if (lodOffset == null) {
	    lodOffset = new Point3f(s, t, r);
	} else {
	    lodOffset.set(s, t, r);
	}
    }

    final void setLodOffset(float s, float t, float r) {
	initLodOffset(s, t, r);
	sendMessage(LOD_OFFSET_CHANGED, new Point3f(s, t, r));
    }

    final void getLodOffset(Tuple3f offset) {
	if (lodOffset == null) {
	    offset.set(0.0f, 0.0f, 0.0f);
	} else {
	    offset.set(lodOffset);
	}
    }


    /**
     * Sets the texture boundary color for this texture object.  The
     * texture boundary color is used when boundaryModeS or boundaryModeT
     * is set to CLAMP.
     * @param boundaryColor the new texture boundary color.
     */
    final void initBoundaryColor(Color4f boundaryColor) {
	this.boundaryColor.set(boundaryColor);
    }

    /**
     * Sets the texture boundary color for this texture object.  The
     * texture boundary color is used when boundaryModeS or boundaryModeT
     * is set to CLAMP.
     * @param r the red component of the color.
     * @param g the green component of the color.
     * @param b the blue component of the color.
     * @param a the alpha component of the color.
     */
    final void initBoundaryColor(float r, float g, float b, float a) {
	this.boundaryColor.set(r, g, b, a);
    }

    /**
     * Retrieves the texture boundary color for this texture object.
     * @param boundaryColor the vector that will receive the
     * current texture boundary color.
     */
    final void getBoundaryColor(Color4f boundaryColor) {
	boundaryColor.set(this.boundaryColor);
    }

    
    /**
     * Set Anisotropic Filter
     */
    final void initAnisotropicFilterMode(int mode) {
	anisotropicFilterMode = mode;
    }

    final int getAnisotropicFilterMode() {
	return anisotropicFilterMode;
    }

    final void initAnisotropicFilterDegree(float degree) {
	anisotropicFilterDegree = degree;
    }

    final float getAnisotropicFilterDegree() {
	return anisotropicFilterDegree;
    }

    /**
     * Set Sharpen Texture function
     */
    final void initSharpenTextureFunc(float[] lod, float[] pts) {
	if (lod == null) {  // pts will be null too.
	    sharpenTextureFuncPts = null;
	    numSharpenTextureFuncPts = 0;
	} else {
	    numSharpenTextureFuncPts = lod.length;
	    if ((sharpenTextureFuncPts == null) ||
		    (sharpenTextureFuncPts.length != lod.length * 2)) {
		sharpenTextureFuncPts = new float[lod.length * 2];
	    }
	    for (int i = 0, j = 0; i < lod.length; i++) {
		sharpenTextureFuncPts[j++] = lod[i];
		sharpenTextureFuncPts[j++] = pts[i];
	    }
	}
    }

    final void initSharpenTextureFunc(Point2f[] pts) {
	if (pts == null) {
	    sharpenTextureFuncPts = null;
	    numSharpenTextureFuncPts = 0;
	} else {
	    numSharpenTextureFuncPts = pts.length;
	    if ((sharpenTextureFuncPts == null) ||
		    (sharpenTextureFuncPts.length != pts.length * 2)) {
		sharpenTextureFuncPts = new float[pts.length * 2];
	    }
	    for (int i = 0, j = 0; i < pts.length; i++) {
		sharpenTextureFuncPts[j++] = pts[i].x;
		sharpenTextureFuncPts[j++] = pts[i].y;
	    }
	}
    }

    final void initSharpenTextureFunc(float[] pts) {
	if (pts == null) {
	    sharpenTextureFuncPts = null;
	    numSharpenTextureFuncPts = 0;
	} else {
	    numSharpenTextureFuncPts = pts.length / 2;
	    if ((sharpenTextureFuncPts == null) ||
		    (sharpenTextureFuncPts.length != pts.length)) {
		sharpenTextureFuncPts = new float[pts.length];
	    }
	    for (int i = 0; i < pts.length; i++) {
		sharpenTextureFuncPts[i] = pts[i];
	    }
	}
    }

    /**
     * Get number of points in the sharpen texture LOD function
     */
    final int getSharpenTextureFuncPointsCount() {
	return numSharpenTextureFuncPts;
    }


    /**
     * Copies the array of sharpen texture LOD function points into the
     * specified arrays
     */
    final void getSharpenTextureFunc(float[] lod, float[] pts) {
	if (sharpenTextureFuncPts != null) {
	    for (int i = 0, j = 0; i < numSharpenTextureFuncPts; i++) {
		lod[i] = sharpenTextureFuncPts[j++];
		pts[i] = sharpenTextureFuncPts[j++];
	    }
	}
    }

    final void getSharpenTextureFunc(Point2f[] pts) {
	if (sharpenTextureFuncPts != null) {
	    for (int i = 0, j = 0; i < numSharpenTextureFuncPts; i++) {
		pts[i].x = sharpenTextureFuncPts[j++];
		pts[i].y = sharpenTextureFuncPts[j++]; } }
    }


    final void initFilter4Func(float[] weights) {
        if (weights == null) {
            filter4FuncPts = null;
        } else {
            if ((filter4FuncPts == null) ||
                    (filter4FuncPts.length != weights.length)) {
                filter4FuncPts = new float[weights.length];
            }
            for (int i = 0; i < weights.length; i++) {
                filter4FuncPts[i] = weights[i];
            }
        }
    }


    final int getFilter4FuncPointsCount() {
	if (filter4FuncPts == null) {
	    return 0;
	} else {
	    return filter4FuncPts.length;
	}
    }

    final void getFilter4Func(float[] weights) {
	if (filter4FuncPts != null) {
	    for (int i = 0; i < filter4FuncPts.length; i++) {
		weights[i] = filter4FuncPts[i];
	    }
	}
    }


    /**
     * internal method only -- returns internal function points
     */
    final float[] getSharpenTextureFunc() {
	return sharpenTextureFuncPts;
    }

    final float[] getFilter4Func(){
	return filter4FuncPts;
    }




    void setLive(boolean backgroundGroup, int refCount) {

	// This line should be assigned before calling doSetLive, so that
	// the mirror object's enable is assigned correctly!
	enable = userSpecifiedEnable;

        super.doSetLive(backgroundGroup, refCount);

	// XXXX: for now, do setLive for all the defined images.
	// But in theory, we only need to setLive those within the
	// baseLevel and maximumLevel range. But then we'll need
	// setLive and clearLive image when the range changes.

	if (images != null) {

	    for (int j = 0; j < numFaces; j++) {
	  	 for (int i = 0; i < maxLevels; i++){
	    	      if (images[j][i] == null) {
              		  throw new IllegalArgumentException(
				J3dI18N.getString("TextureRetained3") + i);
		      }
	    	      images[j][i].setLive(backgroundGroup, refCount);
		 }
	    }
	}

        // Issue 172 : assertion check the sizes of the images after we have
        // checked for all mipmap levels being set
	if (images != null) {
	    for (int j = 0; j < numFaces; j++) {
                checkSizes(images[j]);
	    }
	}

        // Send a message to Rendering Attr stucture to update the resourceMask
        J3dMessage createMessage = VirtualUniverse.mc.getMessage();
        createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
        createMessage.type = J3dMessage.TEXTURE_CHANGED;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(UPDATE_IMAGE);
	createMessage.args[2] = null;
	createMessage.args[3] = new Integer(changedFrequent);
        VirtualUniverse.mc.processMessage(createMessage);

	// If the user has set enable to true, then if the image is null
	// turn off texture enable
	if (userSpecifiedEnable) {
	    if (images != null) {
	        for (int j = 0; j < numFaces && enable; j++) {
	  	    for (int i = baseLevel; i <= maximumLevel && enable; i++){
		        if (images[j][i].isByReference()) {
		            if (images[j][i].bImage[0] == null) {
			        enable = false;
 			    }
		        } else {
		            if (images[j][i].imageYup == null) {
			        enable = false;
		            }
		        }
		    }
		}
	    } else {
		enable = false;
	    }
	    if (!enable)
		sendMessage(ENABLE_CHANGED, Boolean.FALSE);
	}
	    
	super.markAsLive();
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);

	if (images != null) {
	    for (int j = 0; j < numFaces; j++) {
	         for (int i = 0; i < maxLevels; i++) {
	              images[j][i].clearLive(refCount);
	              images[j][i].removeUser(mirror);
		 }
	    }
	}
    }

    /*
     * The following methods update the native context.
     * The implementation for Texture2D happens here.
     * Texture3D and TextureCubeMap implement their own versions.
     */

    void bindTexture(Context ctx, int objectId, boolean enable) {
        Pipeline.getPipeline().bindTexture2D(ctx, objectId, enable);
    }

    void updateTextureBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {

        Pipeline.getPipeline().updateTexture2DBoundary(ctx,
                boundaryModeS, boundaryModeT,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    void updateTextureFilterModes(Context ctx,
            int minFilter, int magFilter) {

        Pipeline.getPipeline().updateTexture2DFilterModes(ctx,
                minFilter, magFilter);
    }

    void updateTextureSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {

        Pipeline.getPipeline().updateTexture2DSharpenFunc(ctx,
            numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    void updateTextureFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {

        Pipeline.getPipeline().updateTexture2DFilter4Func(ctx,
                numFilter4FuncPts, filter4FuncPts);
    }

    void updateTextureAnisotropicFilter(Context ctx, float degree) {
        Pipeline.getPipeline().updateTexture2DAnisotropicFilter(ctx, degree);
    }

    void updateTextureLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {

        Pipeline.getPipeline().updateTexture2DLodRange(ctx, baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    void updateTextureLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {

        Pipeline.getPipeline().updateTexture2DLodOffset(ctx,
                lodOffsetX, lodOffsetY, lodOffsetZ);
    }


    // get an ID for Texture 2D 
    int getTextureId() {
	return (VirtualUniverse.mc.getTexture2DId());
    }


    // free a Texture2D id
    void freeTextureId(int id) {
	synchronized (resourceLock) {
	    if (objectId == id) {
		objectId = -1;
		VirtualUniverse.mc.freeTexture2DId(id);
	    }
	}
    }


    // bind a named texture to a texturing target

    void bindTexture(Canvas3D cv) {
        synchronized(resourceLock) {
	    if (objectId == -1) {
		objectId = getTextureId();
	    }
	    cv.addTextureResource(objectId, this);
 	}	
	bindTexture(cv.ctx, objectId, enable);
    }


    /**
     * load level 0 explicitly with null pointer to enable
     * mipmapping when level 0 is not the base level
     */
    void updateTextureDimensions(Canvas3D cv) {
	updateTextureImage(cv, 0, maxLevels, 0, 
		format, ImageComponentRetained.BYTE_RGBA,
		width, height, boundaryWidth, null);
    }
    

    void updateTextureLOD(Canvas3D cv) {

	if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_LOD_RANGE) != 0 ) {
            updateTextureLodRange(cv.ctx, baseLevel, maximumLevel,
                    minimumLod, maximumLod);
	}

        if ((lodOffset != null) &&
                ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_LOD_OFFSET) != 0)) {
            updateTextureLodOffset(cv.ctx,
                    lodOffset.x, lodOffset.y, lodOffset.z);
	}
    }


    void updateTextureBoundary(Canvas3D cv) {
	updateTextureBoundary(cv.ctx, boundaryModeS, boundaryModeT,
				boundaryColor.x, boundaryColor.y,
				boundaryColor.z, boundaryColor.w);
    }


    void updateTextureFields(Canvas3D cv) {

	int magnificationFilter = magFilter;
	int minificationFilter = minFilter;

	// update sharpen texture function if applicable

	if ((magFilter >= Texture.LINEAR_SHARPEN) &&
		(magFilter <= Texture.LINEAR_SHARPEN_ALPHA)) {

	    if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_SHARPEN) != 0 ) {

		// send down sharpen texture LOD function
		//
	        updateTextureSharpenFunc(cv.ctx, 
			numSharpenTextureFuncPts, sharpenTextureFuncPts);

	    } else {

		// sharpen texture is not supported by the underlying
		// library, fallback to BASE_LEVEL_LINEAR

		magnificationFilter = Texture.BASE_LEVEL_LINEAR;
	    }
	} else if ((magFilter >= Texture2D.LINEAR_DETAIL) &&
		(magFilter <= Texture2D.LINEAR_DETAIL_ALPHA)) {
	    if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_DETAIL) == 0) {

		// detail texture is not supported by the underlying
		// library, fallback to BASE_LEVEL_LINEAR

		magnificationFilter = Texture.BASE_LEVEL_LINEAR;
	    }
	} 

	if (minFilter == Texture.FILTER4 || magFilter == Texture.FILTER4) {

	    boolean noFilter4 = false;

	    if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_FILTER4) != 0) {

		if (filter4FuncPts == null) {

		    // filter4 function is not defined, 
		    // fallback to BASE_LEVEL_LINEAR

		    noFilter4 = true;
		} else {
	            updateTextureFilter4Func(cv.ctx, filter4FuncPts.length,
					filter4FuncPts);
		}
	    } else {

		// filter4 is not supported by the underlying
		// library, fallback to BASE_LEVEL_LINEAR

		noFilter4 = true;
	    } 

	    if (noFilter4) {
		if (minFilter == Texture.FILTER4) {
		    minificationFilter = Texture.BASE_LEVEL_LINEAR;
		}
		if (magFilter == Texture.FILTER4) {
		    magnificationFilter = Texture.BASE_LEVEL_LINEAR;
		}
	    }
	}

	// update texture filtering modes

	updateTextureFilterModes(cv.ctx, minificationFilter, 
						magnificationFilter);

	if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_ANISOTROPIC_FILTER) 
			!= 0) {
	    if (anisotropicFilterMode == Texture.ANISOTROPIC_NONE) {
	        updateTextureAnisotropicFilter(cv.ctx, 1.0f);
	    } else {
	        updateTextureAnisotropicFilter(cv.ctx, anisotropicFilterDegree);
	    }
	}

	// update texture boundary modes, boundary color

	updateTextureBoundary(cv);
	
    }


    // Wrapper around the native call for 2D textures; overridden for
    // Texture3D and TextureCureMap
    void updateTextureImage(Canvas3D cv,
            int face, int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth, byte[] imageData) {

        Pipeline.getPipeline().updateTexture2DImage(cv.ctx,
                numLevels, level,
                internalFormat, storedFormat,
                width, height, boundaryWidth, imageData);
    }

    // Wrapper around the native call for 2D textures; overridden for
    // Texture3D and TextureCureMap
    void updateTextureSubImage(Canvas3D cv,
            int face, int level,
            int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData) {

        Pipeline.getPipeline().updateTexture2DSubImage(cv.ctx,
                level, xoffset, yoffset,
                internalFormat, storedFormat,
                imgXOffset, imgYOffset,
                tilew, width, height,
                imageData);
    }



    /**
     * reloadTextureImage is used to load a particular level of image
     * This method needs to take care of RenderedImage as well as
     * BufferedImage
     */
    void reloadTextureImage(Canvas3D cv, int face, int level,
				ImageComponentRetained image, int numLevels) {

        //System.out.println("Texture.reloadTextureImage: face= " + face + " level= " + level);

	//System.out.println("...image = "+image+" image.storedFormat = "+image.storedYupFormat+" image.imageYup = "+image.imageYup+" texture - "+this);

	//System.out.println("....imageYupAllocated= " + image.imageYupAllocated);

        updateTextureImage(cv,
                face, numLevels, level,
                format, image.storedYupFormat,
                image.width, image.height,
                boundaryWidth, image.imageYup);

	// Now take care of the RenderedImage case. Note, if image
	// is a RenderedImage, then imageYup will be null

	if (image.imageYupClass == ImageComponentRetained.RENDERED_IMAGE) {

	    //		    System.out.println("==========. subImage");
	    // Download all the tiles for this texture
	    int xoffset = 0, yoffset = 0;
	    int tmpw = image.width;
	    int tmph = image.height;
	    int endXTile = image.minTileX * image.tilew + image.tileGridXOffset+image.tilew;
	    int endYTile = image.minTileY * image.tileh + image.tileGridYOffset+image.tileh;
	    int curw = (endXTile - image.minX);
	    int curh = (endYTile - image.minY);

	    if (tmpw < curw) {
	        curw = tmpw;
	    }
	
	    if (tmph < curh) {
	        curh = tmph;
	    }

	    int startw = curw;
	    int imageXOffset = image.tilew - curw;
	    int imageYOffset = image.tileh - curh;
	    for (int m = image.minTileY; m < image.minTileY+image.numYTiles; m++) {
	        xoffset = 0;
	        tmpw = width;
	        curw = startw;
	        imageXOffset = image.tilew - curw;
	        for (int n = image.minTileX; 
			n < image.minTileX+image.numXTiles; n++) {
		    java.awt.image.Raster ras;
		    ras = image.bImage[0].getTile(n,m);
		    byte[] tmpImage =  ((DataBufferByte)ras.getDataBuffer()).getData();
                    updateTextureSubImage(cv, face,
                            level, xoffset, yoffset, format,
                            image.storedYupFormat,
                            imageXOffset, imageYOffset,
                            image.tilew,
                            curw, curh,
                            tmpImage);
	  	    xoffset += curw;
	  	    imageXOffset = 0;
		    tmpw -= curw;
		    if (tmpw < image.tilew) 
		        curw = tmpw;
		    else
		        curw = image.tilew;
	        }
	        yoffset += curh;
	        imageYOffset = 0;
	        tmph -= curh;
	        if (tmph < image.tileh)
		    curh = tmph;
	        else
		    curh = image.tileh;
	    }
        }
    }


    /**
     * update a subregion of the texture image
     * This method needs to take care of RenderedImage as well as
     * BufferedImage
     */
    void reloadTextureSubImage(Canvas3D cv, int face, int level, 
				ImageComponentUpdateInfo info,
				ImageComponentRetained image) {

	int x = info.x,
	    y = info.y,
	    width = info.width,
	    height = info.height;

        //The x and y here specifies the subregion of the imageData of
        //the associated RenderedImage.

	//System.out.println("\nupdateTextureSubImage: x= " + x + " y= " + y +
	//			" width= " + width + " height= " + height +
	//			" format= " + format);


	if (image.imageYupClass == ImageComponentRetained.BUFFERED_IMAGE) {

	    int xoffset = x - image.minX;
	    int yoffset = y - image.minY;

	    byte[] imageData;
	    if (image.imageYupAllocated) {
		imageData = image.imageYup;
		yoffset = image.height - yoffset - height;

	    } else {
                // Fix issue 132
		imageData = ((DataBufferByte)
			((BufferedImage)image.bImage[0]).getRaster().getDataBuffer()).getData();

	        // based on the yUp flag in the associated ImageComponent,
	        // adjust the yoffset

	        if (!image.yUp) {
		    yoffset = image.height - yoffset - height;
		}
	    }

            updateTextureSubImage(cv, face, level,
                    xoffset, yoffset,
                    format, image.storedYupFormat,
                    xoffset, yoffset,
                    image.width, width, height, imageData);
	} else {

	    // System.out.println("RenderedImage subImage update");

	    // determine the first tile of the image

            float mt;
	    int xoff = image.tileGridXOffset;
	    int yoff = image.tileGridYOffset;
	    int minTileX, minTileY;

	    int rx = x + image.minX;	// x offset in RenderedImage
	    int ry = y + image.minY;	// y offset in RenderedImage
	
            mt = (float)(rx - xoff) / (float)image.tilew;
            if (mt < 0) {
                minTileX = (int)(mt - 1);
            } else {
                minTileX = (int)mt;
            }

            mt = (float)(ry - yoff) / (float)image.tileh;
            if (mt < 0) {
                minTileY = (int)(mt - 1);
            } else {
                minTileY = (int)mt;
            }

	    // determine the pixel offset of the upper-left corner of the
	    // first tile
	    int startXTile = minTileX * image.tilew + xoff;
	    int startYTile = minTileY * image.tilew + yoff;


            // image dimension in the first tile

            int curw = (startXTile + image.tilew - rx);
            int curh = (startYTile + image.tileh - ry);

            // check if the to-be-copied region is less than the tile image
            // if so, update the to-be-copied dimension of this tile

            if (curw > width) {
                curw = width;
            }

            if (curh > height) {
                curh = height;
            }

            // save the to-be-copied width of the left most tile

            int startw = curw;


            // temporary variable for dimension of the to-be-copied region

            int tmpw = width;
            int tmph = height;


            // offset of the first pixel of the tile to be copied; offset is
            // relative to the upper left corner of the title

            int imgX = rx - startXTile;
            int imgY = ry - startYTile;


            // determine the number of tiles in each direction that the
            // image spans

            int numXTiles = (width + imgX) / image.tilew;
            int numYTiles = (height + imgY) / image.tileh;

            if (((float)(width + imgX ) % (float)image.tilew) > 0) {
                numXTiles += 1;
            }

            if (((float)(height + imgY ) % (float)image.tileh) > 0) {
                numYTiles += 1;
            }

	    java.awt.image.Raster ras;
	    byte[] imageData;

	    int textureX = x; 	// x offset in the texture 
	    int textureY = y; 	// y offset in the texture 

	    for (int yTile = minTileY; yTile < minTileY + numYTiles;
			yTile++) {
		
		tmpw = width;
		curw = startw;
		imgX = rx - startXTile;
		
		for (int xTile = minTileX; xTile < minTileX + numXTiles;
			xTile++) {
		    ras = image.bImage[0].getTile(xTile, yTile);
		    imageData = ((DataBufferByte)ras.getDataBuffer()).getData();

                    updateTextureSubImage(cv, face, level,
                            textureX, textureY,
                            format, image.storedYupFormat,
                            imgX, imgY,
                            image.tilew, curw, curh, imageData);

                    // move to the next tile in x direction

		    textureX += curw;
		    imgX = 0;

                    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < image.tilew) {
			curw = tmpw;
		    } else {
			curw = image.tilew;
		    }	
	   	}

                // move to the next set of tiles in y direction
		textureY += curh;
                imgY = 0;

                // determine the height of copy region for the next set
                // of tiles
                tmph -= curh;
                if (tmph < image.tileh) {
                    curh = tmph;
                } else {
                    curh = image.tileh;
                }
	    }
	}
    }


    // reload texture mipmap

    void reloadTexture(Canvas3D cv) {


	int blevel, mlevel;

	//System.out.println("reloadTexture: baseLevel= " + baseLevel +
	//			" maximumLevel= " + maximumLevel);

	if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_LOD_RANGE) == 0 ) {
	    blevel = 0;
	    mlevel = maxLevels - 1;
        } else {
	    blevel = baseLevel;
	    mlevel = maximumLevel;
	}

	if (blevel != 0) {
	    // level 0 is not the base level, hence, need
            // to load level 0 explicitly with a null pointer in order 
	    // for mipmapping to be active. 

	    updateTextureDimensions(cv);
	}

	for (int j = 0; j < numFaces; j++) {
	    for (int i = blevel; i <= mlevel; i++) {

		// it is possible to have null pointer if only a subset
                // of mipmap levels is defined but the canvas does not
 		// support lod_range extension

		if (images[j][i] != null) {
		    reloadTextureImage(cv, j, i, images[j][i], maxLevels);
		}
	    }
        }
    }


    // update texture mipmap based on the imageUpdateInfo

    void updateTexture(Canvas3D cv, int resourceBit) {

	//System.out.println("updateTexture\n");

	ImageComponentUpdateInfo info;

	for (int k = 0; k < numFaces; k++) { 
	    for (int i = baseLevel; i <= maximumLevel; i++) {
		if (imageUpdateInfo[k][i] != null) {
		    for (int j = 0; j < imageUpdateInfo[k][i].size(); j++) {

			info = (ImageComponentUpdateInfo)
					imageUpdateInfo[k][i].get(j);


	    	        synchronized(resourceLock) {

			    // if this info is updated, move on to the next one

			    if ((info.updateMask & resourceBit) == 0)
			        continue;


			    // check if all canvases have processed this update
			    info.updateMask &= ~resourceBit;

			    // all the current resources have updated this
			    // info, so this info can be removed from the
			    // update list
			    if ((info.updateMask & resourceCreationMask) 
					== 0) {
				info.updateMask = 0; // mark this update as
						     // done

			 	// mark the prune flag so as to prune the
				// update list next time when the update 
				// list is to be modified.
				// Don't want to clean up the list at 
				// rendering time because (1) MT issue,
				// other renderer could be processing
				// the update list now;
				// (2) takes up rendering time.
				if (imageUpdatePruneMask == null) {
				    imageUpdatePruneMask = new int[numFaces];
				}
				imageUpdatePruneMask[k] = 1 << i;
			    }
			}

			if (info.entireImage == true) {
			    reloadTextureImage(cv, k, i, 
						images[k][i], maxLevels);
			} else {
			    reloadTextureSubImage(cv, k, i, info, images[k][i]);
			}

		    }
		}
	    }
	}
    }


    /**
     * reloadTextureSharedContext is called to reload texture
     * on a shared context. It is invoked from the Renderer
     * before traversing the RenderBin. The idea is to reload
     * all necessary textures up front for all shared contexts
     * in order to minimize the context switching overhead.
     */
    void reloadTextureSharedContext(Canvas3D cv) {

	// if texture is not enabled, don't bother downloading the
	// the texture state

	if (enable == false) {
	    return;
	}

	bindTexture(cv);

	// reload all levels of texture image

	// update texture parameters such as boundary modes, filtering

	updateTextureFields(cv);


	// update texture Lod parameters

	updateTextureLOD(cv);


	// update all texture images

	reloadTexture(cv);

        synchronized(resourceLock) {
	    resourceCreationMask |= cv.screen.renderer.rendererBit;
	    resourceUpdatedMask |= cv.screen.renderer.rendererBit;
	    resourceLodUpdatedMask |= cv.screen.renderer.rendererBit;
	    resourceInReloadList &= ~cv.screen.renderer.rendererBit;
	}
    }

	
    /**
     * updateNative is called while traversing the RenderBin to 
     * update the texture state
     */
    void updateNative(Canvas3D cv) {
	boolean reloadTexture = false; // true - reload all levels of texture
	boolean updateTexture = false; // true - update a portion of texture
	boolean updateTextureLod = false; // true - update texture Lod info

        //System.out.println("Texture/updateNative: " + this + "object= " + objectId + " enable= " + enable);

	bindTexture(cv);

	// if texture is not enabled, don't bother downloading the
	// the texture state

	if (enable == false) {
	    return;
	}

        if (cv.useSharedCtx && cv.screen.renderer.sharedCtx != null) {

            if ((resourceCreationMask & cv.screen.renderer.rendererBit) == 0) {
		reloadTexture = true;
	    } else {
	        if (((resourceUpdatedMask & 
		      cv.screen.renderer.rendererBit) == 0) && 
		    (imageUpdateInfo != null)) {
		    updateTexture = true;
		}

		if ((resourceLodUpdatedMask &
				cv.screen.renderer.rendererBit) == 0) {
		    updateTextureLod = true;
		}
	    }
	    if (reloadTexture || updateTexture || updateTextureLod) {
		cv.makeCtxCurrent(cv.screen.renderer.sharedCtx);
	        bindTexture(cv);
	    }
	} else {
            if ((resourceCreationMask & cv.canvasBit) == 0) {
		reloadTexture = true;
	    } else {
	  	if (((resourceUpdatedMask & cv.canvasBit) == 0) && 
			(imageUpdateInfo != null)) {
		    updateTexture = true;
		}

		if ((resourceLodUpdatedMask & cv.canvasBit) == 0) {
		    updateTextureLod = true;
		}
	    }
	}
	

	if (VirtualUniverse.mc.isD3D()) {
	    if (texTimestamp != VirtualUniverse.mc.resendTexTimestamp) {
		texTimestamp = VirtualUniverse.mc.resendTexTimestamp;
		reloadTexture = true;
	    }

	    if (!reloadTexture) {
		// D3D didn't store texture properties during Texture binding
		updateTextureFields(cv);
	    }
	}


//System.out.println("......... reloadTexture= " + reloadTexture +
//		 " updateTexture= " + updateTexture + 
//		 " updateTextureLod= " + updateTextureLod);

//System.out.println("......... resourceCreationMask= " + resourceCreationMask +
//		   " resourceUpdatedMask= " + resourceUpdatedMask);
 
	if (reloadTexture) {

	    // reload all levels of texture image

	    // update texture parameters such as boundary modes, filtering

	    updateTextureFields(cv);


	    // update texture Lod parameters

	    updateTextureLOD(cv);


	    // update all texture images

	    reloadTexture(cv);


	    if (cv.useSharedCtx) {
		cv.makeCtxCurrent(cv.ctx);
                synchronized(resourceLock) {
		    resourceCreationMask |= cv.screen.renderer.rendererBit;
		    resourceUpdatedMask |= cv.screen.renderer.rendererBit;
		    resourceLodUpdatedMask |= cv.screen.renderer.rendererBit;
	 	}
	    }
	    else {
                synchronized(resourceLock) {
                    resourceCreationMask |= cv.canvasBit;
                    resourceUpdatedMask |= cv.canvasBit;
                    resourceLodUpdatedMask |= cv.canvasBit;
	 	}
	    }
	} else if (updateTextureLod || updateTexture) {

	    if (updateTextureLod) {
		updateTextureLOD(cv);
	    }

	    if (updateTexture) {

	        // update texture image

	        int resourceBit = 0;

	        if (cv.useSharedCtx) {
		    resourceBit = cv.screen.renderer.rendererBit;
	        } else {
		    resourceBit = cv.canvasBit;
	        }

	        // update texture based on the imageComponent update info

	        updateTexture(cv, resourceBit);
	    }

	    // set the appropriate bit in the resource update masks showing
            // that the resource is up-to-date

	    if (cv.useSharedCtx) {
		cv.makeCtxCurrent(cv.ctx);
                synchronized(resourceLock) {
		    resourceUpdatedMask |= cv.screen.renderer.rendererBit;
		    resourceLodUpdatedMask |= cv.screen.renderer.rendererBit;
	 	}
	    } else {
                synchronized(resourceLock) {
		    resourceUpdatedMask |= cv.canvasBit;
		    resourceLodUpdatedMask |= cv.canvasBit;
	 	}
	    }
	}
    }

    synchronized void createMirrorObject() {
       if (mirror == null) {
	   if (this instanceof Texture3DRetained) {
		Texture3DRetained t3d = (Texture3DRetained)this;
		Texture3D tex = new Texture3D(t3d.mipmapMode,
						 t3d.format,
						 t3d.width,
						 t3d.height,
						 t3d.depth,
						 t3d.boundaryWidth);
		mirror = (Texture3DRetained)tex.retained;;

	   } else if (this instanceof TextureCubeMapRetained) {
		TextureCubeMap tex = new TextureCubeMap(mipmapMode,
						format, width,
						boundaryWidth);
		mirror = (TextureCubeMapRetained)tex.retained;

	   } else {
		Texture2D tex = new Texture2D(mipmapMode,
						 format,
						 width,
						 height,
						 boundaryWidth);
		mirror = (Texture2DRetained)tex.retained;;
	   }

	   ((TextureRetained)mirror).objectId = -1;
       }
       initMirrorObject();
    }


    /**
     * Initializes a mirror object, point the mirror object to the retained
     * object if the object is not editable
     */
    synchronized void initMirrorObject() {
	mirror.source = source;
	if (this instanceof Texture3DRetained) {
	    Texture3DRetained t3d = (Texture3DRetained)this;
	   
	    ((Texture3DRetained)mirror).boundaryModeR = t3d.boundaryModeR;
	    ((Texture3DRetained)mirror).depth = t3d.depth;
	}
	TextureRetained mirrorTexture = (TextureRetained)mirror;

	mirrorTexture.boundaryModeS = boundaryModeS;
	mirrorTexture.boundaryModeT = boundaryModeT;
	mirrorTexture.minFilter = minFilter;
	mirrorTexture.magFilter = magFilter;
	mirrorTexture.boundaryColor.set(boundaryColor);
	mirrorTexture.enable = enable;
	mirrorTexture.userSpecifiedEnable = enable;
	mirrorTexture.imagesLoaded = imagesLoaded;
	mirrorTexture.enable = enable;
	mirrorTexture.numFaces = numFaces;
	mirrorTexture.resourceCreationMask = 0x0;
	mirrorTexture.resourceUpdatedMask = 0x0;
	mirrorTexture.resourceLodUpdatedMask = 0x0;
	mirrorTexture.resourceInReloadList = 0x0;

	// LOD information
	mirrorTexture.baseLevel = baseLevel;
	mirrorTexture.maximumLevel = maximumLevel;
	mirrorTexture.minimumLod = minimumLod;
	mirrorTexture.maximumLod = maximumLod;
	mirrorTexture.lodOffset = lodOffset;

	// sharpen texture LOD function

	mirrorTexture.numSharpenTextureFuncPts = numSharpenTextureFuncPts;
	if (sharpenTextureFuncPts == null) {
	    mirrorTexture.sharpenTextureFuncPts = null;
	} else {
	    if ((mirrorTexture.sharpenTextureFuncPts == null) ||
		    (mirrorTexture.sharpenTextureFuncPts.length !=
			sharpenTextureFuncPts.length)) {
		mirrorTexture.sharpenTextureFuncPts = 
			new float[sharpenTextureFuncPts.length];
	    }
	    for (int i = 0; i < sharpenTextureFuncPts.length; i++) {
		mirrorTexture.sharpenTextureFuncPts[i] = 
			sharpenTextureFuncPts[i];
	    }
	}

        // filter4 function
        if (filter4FuncPts == null) {
	    mirrorTexture.filter4FuncPts = null;
        } else {
	    if ((mirrorTexture.filter4FuncPts == null) ||
		    (mirrorTexture.filter4FuncPts.length !=
			filter4FuncPts.length)) {
		mirrorTexture.filter4FuncPts =
			new float[filter4FuncPts.length];
	    }
	    for (int i = 0; i < filter4FuncPts.length; i++) {
		mirrorTexture.filter4FuncPts[i] =
		  	filter4FuncPts[i];
	    }
	}

	// Anisotropic Filter
	mirrorTexture.anisotropicFilterMode = anisotropicFilterMode;
	mirrorTexture.anisotropicFilterDegree = anisotropicFilterDegree;

	// implicit mipmap generation
	if (mipmapMode == Texture.BASE_LEVEL &&
	    (minFilter == Texture.NICEST ||
	     minFilter == Texture.MULTI_LEVEL_POINT ||
	     minFilter == Texture.MULTI_LEVEL_LINEAR)) {
	    mirrorTexture.maxLevels = maxMipMapLevels;

	    if ((mirrorTexture.images == null) ||
		  (mirrorTexture.images.length < numFaces) ||
		  (mirrorTexture.images[0].length < mirrorTexture.maxLevels)) {
		mirrorTexture.images = 
		 new ImageComponentRetained[numFaces][mirrorTexture.maxLevels];
	    }

	    for (int j = 0; j < numFaces; j++) {
	        mirrorTexture.images[j][0] = images[j][0];

	        // add texture to the userList of the images
	        if (images[j][0] != null) {
	            images[j][0].addUser(mirrorTexture);
		}

	        for (int i = 1; i < mirrorTexture.maxLevels; i++) {
		    mirrorTexture.images[j][i] = createNextLevelImage(
			       (mirrorTexture.images[j][i-1]));
		}
	    }
	}
	else {
	    mirrorTexture.maxLevels = maxLevels;
	    if (images != null) {

		for (int j = 0; j < numFaces; j++) {
	            for (int i = 0; i < maxLevels; i++) {
			mirrorTexture.images[j][i] = images[j][i];

	                // add texture to the userList of the images
	                if (images[j][i] != null) {
	                    images[j][i].addUser(mirrorTexture);
			}
		    }
		}
	    }
	}
    }


    /**
     * Go through the image update info list
     * and remove those that are already done
     * by all the resources
     */
    void pruneImageUpdateInfo() {
	ImageComponentUpdateInfo info;

	//System.out.println("Texture.pruneImageUpdateInfo");

	for (int k = 0; k < numFaces; k++) {
	    for (int i = baseLevel; i <= maximumLevel; i++) {
	        if ((imageUpdatePruneMask[k] & (1<<i)) != 0) {
		    if (imageUpdateInfo[k][i] != null) {
		        for (int j = 0; j < imageUpdateInfo[k][i].size(); j++) {
			    info = (ImageComponentUpdateInfo)
					imageUpdateInfo[k][i].get(j);
			    if (info.updateMask == 0) {
			        // this update info is done, remove it
			        // from the update list
			        VirtualUniverse.mc.addFreeImageUpdateInfo(info);
			        imageUpdateInfo[k][i].remove(j);
			    }
		        }
		    }
		    imageUpdatePruneMask[k] &= ~(1<<i);
		}
	    }
	}
    }

    /**
     * addImageUpdateInfo(int level) is to update a particular level.
     * In this case, it supercedes all the subImage update for this level,
     * and all those update info can be removed from the update list.
     *
     * Note: this method is called from mirror only
     */
    void addImageUpdateInfo(int level, int face, ImageComponentUpdateInfo arg) {

	ImageComponentUpdateInfo info;

	if (imageUpdateInfo == null) {
	    imageUpdateInfo = new ArrayList[numFaces][maxLevels];
	}

	if (imageUpdateInfo[face][level] == null) {
	    imageUpdateInfo[face][level] = new ArrayList();
	}

	//info = mirrorTa.getFreeImageUpdateInfo();
	info = VirtualUniverse.mc.getFreeImageUpdateInfo();


	if (arg == null) {
	    // no subimage info, so the entire image is to be updated
	    info.entireImage = true;
        // Fix issue 117 using ogl subimage always
//	} else if ((arg.width >= width/2) && (arg.height >= height/2)) {
//
//	    // if the subimage dimension is close to the complete dimension,
//            // use the full update (it's more efficient)
//	    info.entireImage = true;
	} else {
	    info.entireImage = false;
	}

	if (info.entireImage) {
	    // the entire image update supercedes all the subimage update;
            // hence, remove all the existing updates from the list
	    VirtualUniverse.mc.addFreeImageUpdateInfo(
			imageUpdateInfo[face][level]);
	    imageUpdateInfo[face][level].clear();

	    // reset the update prune mask for this level
	    if (imageUpdatePruneMask != null) {
	        imageUpdatePruneMask[face] &= ~(1 << level);
	    }

	} else {
	    // subimage update, needs to save the subimage info
	    info.x = arg.x;
	    info.y = arg.y;
	    info.z = arg.z;
	    info.width = arg.width;
	    info.height = arg.height;
	}

	// save the mask which shows the canvases that have created resources
	// for this image, aka, these are the resources that need to be
	// updated.
	info.updateMask = resourceCreationMask;

	// add the image update to the list
	imageUpdateInfo[face][level].add(info);

	// check if the update list stills need to be pruned
	if (imageUpdatePruneMask != null) {
	    pruneImageUpdateInfo();
	}
    }

    void validate() {
	enable = true;
	for (int j = 0; j < numFaces && enable; j++) {
	    for (int i = baseLevel; i <= maximumLevel && enable; i++) {
		if (images[j][i] == null) {
		    enable = false;
		}
	    }
	}
    }

    /**
     * Update the "component" field of the mirror object with the 
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	TextureRetained mirrorTexture = (TextureRetained)mirror;

	if ((component & ENABLE_CHANGED) != 0) {
	    mirrorTexture.enable = ((Boolean)value).booleanValue();

	} else if ((component & IMAGE_CHANGED) != 0) {

	    Object [] arg = (Object []) value;
            int level = ((Integer)arg[0]).intValue();
	    ImageComponent image = (ImageComponent)arg[1];
            int face = ((Integer)arg[2]).intValue();

	    // first remove texture from the userList of the current
            // referencing image and

	    if (mirrorTexture.images[face][level] != null) {
	        mirrorTexture.images[face][level].removeUser(mirror);
	    }

	    // assign the new image and add texture to the userList
	    if (image == null) {
	        mirrorTexture.images[face][level] = null;

	    } else {
	        mirrorTexture.images[face][level] = 
			(ImageComponentRetained)image.retained;
	        mirrorTexture.images[face][level].addUser(mirror);

            }

	    // NOTE: the old image has to be removed from the
	    // renderBins' NodeComponentList and new image has to be
	    // added to the lists. This will be taken care of
	    // in the RenderBin itself in response to the
	    // IMAGE_CHANGED message

		
	    // mark that texture images need to be updated
	    mirrorTexture.resourceUpdatedMask = 0;

	    // add update info to the update list
	    mirrorTexture.addImageUpdateInfo(level, face, null);

	} else if ((component & IMAGES_CHANGED) != 0) {
	    
	    Object [] arg = (Object []) value;
	    ImageComponent [] images = (ImageComponent[])arg[0];
	    int face = ((Integer)arg[1]).intValue();
	    
	    for (int i = 0; i < images.length; i++) {
		
		// first remove texture from the userList of the current
		// referencing image
		if (mirrorTexture.images[face][i] != null) {
		    mirrorTexture.images[face][i].removeUser(mirror);
		}
		
		// assign the new image and add texture to the userList
		if (images[i] == null) {
		    mirrorTexture.images[face][i] = null;
		} else {
		    mirrorTexture.images[face][i] = 
			(ImageComponentRetained)images[i].retained;
		    mirrorTexture.images[face][i].addUser(mirror);
		}
	    }
	    mirrorTexture.updateResourceCreationMask();
	    
	    // NOTE: the old images have to be removed from the
	    // renderBins' NodeComponentList and new image have to be
	    // added to the lists. This will be taken care of
	    // in the RenderBin itself in response to the
	    // IMAGES_CHANGED message

	} else if ((component & BASE_LEVEL_CHANGED) != 0) {
	    int level = ((Integer)value).intValue();
	    
	    if (level < mirrorTexture.baseLevel) {

		// add texture to the userList of those new levels of 
		// enabling images

		for (int j = 0; j < numFaces; j++) {
		    for (int i = level; i < mirrorTexture.baseLevel; i++) {

			if (mirrorTexture.images[j][i] == null) {
			    mirrorTexture.enable = false;
			} else {
	    		    mirrorTexture.addImageUpdateInfo(i, j, null);
			}
		    }
		}

		mirrorTexture.baseLevel = level;

	        // mark that texture images need to be updated
	        mirrorTexture.resourceUpdatedMask = 0;


	    } else {

		mirrorTexture.baseLevel = level;

		if (userSpecifiedEnable && (mirrorTexture.enable == false)) {

		    // if texture is to be enabled but is currently
		    // disabled, it's probably disabled because
		    // some of the images are missing. Now that
		    // the baseLevel is modified, validate the
		    // texture images again

		    mirrorTexture.validate();
		}
	    }

	    // mark that texture lod info needs to be updated
	    mirrorTexture.resourceLodUpdatedMask = 0;

	} else if ((component & MAX_LEVEL_CHANGED) != 0) {
            int level = ((Integer)value).intValue();

            if (level > mirrorTexture.maximumLevel) {

                // add texture to the userList of those new levels of
                // enabling images

                for (int j = 0; j < numFaces; j++) {
                    for (int i = mirrorTexture.maximumLevel; i < level; i++) {

                        if (mirrorTexture.images[j][i] == null) {
                            mirrorTexture.enable = false;
			} else {
	    		    mirrorTexture.addImageUpdateInfo(i, j, null);
                        }
                    }
                }

                mirrorTexture.maximumLevel = level;

	        // mark that texture images need to be updated
	        mirrorTexture.resourceUpdatedMask = 0;

            } else {

                mirrorTexture.maximumLevel = level;

                if (userSpecifiedEnable && (mirrorTexture.enable == false)) {

                    // if texture is to be enabled but is currently
                    // disabled, it's probably disabled because
                    // some of the images are missing. Now that
                    // the baseLevel is modified, validate the
                    // texture images again

                    mirrorTexture.validate();
                }
            }

	    // mark that texture lod info needs to be updated
	    mirrorTexture.resourceLodUpdatedMask = 0;

	} else if ((component & MIN_LOD_CHANGED) != 0) {
	    mirrorTexture.minimumLod = ((Float)value).floatValue();

	    // mark that texture lod info needs to be updated
	    mirrorTexture.resourceLodUpdatedMask = 0;

	} else if ((component & MAX_LOD_CHANGED) != 0) {
	    mirrorTexture.maximumLod = ((Float)value).floatValue();

	    // mark that texture lod info needs to be updated
	    mirrorTexture.resourceLodUpdatedMask = 0;

	} else if ((component & LOD_OFFSET_CHANGED) != 0) {
	    if ((mirrorTexture.lodOffset) == null) {
		mirrorTexture.lodOffset = 
					new Point3f((Point3f)value);
	    } else {
		mirrorTexture.lodOffset.set((Point3f)value);
	    }

	    // mark that texture lod info needs to be updated
	    mirrorTexture.resourceLodUpdatedMask = 0;

        } else if ((component & UPDATE_IMAGE) != 0) {
            mirrorTexture.updateResourceCreationMask();
        }

    }


    // notifies the Texture mirror object that the image data in a referenced
    // ImageComponent object is changed. Need to update the texture image
    // accordingly.
    // Note: this is called from mirror object only

    void notifyImageComponentImageChanged(ImageComponentRetained image,
					ImageComponentUpdateInfo value) {

        //System.out.println("Texture.notifyImageComponentImageChanged");


	// if this texture is to be reloaded, don't bother to keep
	// the update info

	if (resourceCreationMask == 0) {

	    if (imageUpdateInfo != null) {

                //remove all the existing updates from the list

	        for (int face = 0; face < numFaces; face++) {
		    for (int level = 0; level < maxLevels; level++) {
			if (imageUpdateInfo[face][level] != null) {
			    VirtualUniverse.mc.addFreeImageUpdateInfo(
                        	     imageUpdateInfo[face][level]);
			    imageUpdateInfo[face][level].clear();
			}
		    }

                    // reset the update prune mask for this level
                    if (imageUpdatePruneMask != null) {
                        imageUpdatePruneMask[face] = 0;
		    }
		}
	    }

	    return;
	}


	// first find which texture image is being affected

	boolean done;
	
	for (int j = 0; j < numFaces; j++) {

	    done = false;
	    for (int i = baseLevel; i <= maximumLevel && !done; i++) {
	         if (images[j][i] == image) {

		     // reset the resourceUpdatedMask to tell the
		     // rendering method to update the resource
		     resourceUpdatedMask = 0;

	             // add update info to the update list
	             addImageUpdateInfo(i, j, value);

		     // set done to true for this face because no two levels
		     // can reference the same ImageComponent object
		     done = true;
	         }
	    }
	}
    }

	
    // reset the resourceCreationMask
    // Note: called from the mirror object only

    void updateResourceCreationMask() {
        resourceCreationMask = 0x0; 
    }

    final ImageComponentRetained createNextLevelImage(
		ImageComponentRetained oImage) {
 
	int xScale, yScale, nWidth, nHeight;
	ImageComponentRetained nImage = null;

        if (oImage.width > 1) {
            nWidth = oImage.width >> 1;
            xScale = 2;
        } else {
            nWidth = 1;
            xScale = 1;
        }
        if (oImage.height > 1) {
            nHeight = oImage.height >> 1; 
            yScale = 2; 
        } else { 
            nHeight = 1;
            yScale = 1; 
        }   

        int bytesPerPixel = oImage.bytesPerYupPixelStored;   

	if (oImage instanceof ImageComponent2DRetained) {

            nImage = new ImageComponent2DRetained();
            nImage.processParams(oImage.getFormat(), nWidth, nHeight, 1);
            nImage.imageYup = new byte[nWidth * nHeight * bytesPerPixel];
	    nImage.storedYupFormat = nImage.internalFormat;
	    nImage.bytesPerYupPixelStored = bytesPerPixel;
	    scaleImage(nWidth, nHeight, xScale, yScale, oImage.width, 0, 0,
			bytesPerPixel, nImage.imageYup, oImage.imageYup);

	} else {	//oImage instanceof ImageComponent3DRetained 

	    int depth =  ((ImageComponent3DRetained)oImage).depth;
            nImage = new ImageComponent3DRetained();
            nImage.processParams(oImage.getFormat(), nWidth, nHeight, depth);
            nImage.imageYup = new byte[nWidth * nHeight * bytesPerPixel];
	    nImage.storedYupFormat = nImage.internalFormat;
	    nImage.bytesPerYupPixelStored = bytesPerPixel;

            for (int i = 0; i < depth; i++) {
	        scaleImage(nWidth, nHeight, xScale, yScale, oImage.width, 
			i * nWidth * nHeight * bytesPerPixel,
			i * oImage.width * oImage.height * bytesPerPixel,
			bytesPerPixel, nImage.imageYup, oImage.imageYup);
	    }
	} 
        return nImage;
    }

    final void scaleImage(int nWidth, int nHeight, int xScale, int yScale,
			int oWidth, int nStart, int oStart, int bytesPerPixel,
			byte[] nData, byte[] oData) {

	int nOffset = 0; 
	int oOffset = 0; 
	int oLineIncr = bytesPerPixel * oWidth;
	int oPixelIncr = bytesPerPixel << 1;

	if (yScale == 1) {
                for (int x = 0; x < nWidth; x++) {
                    for (int k = 0; k < bytesPerPixel; k++) {
			nData[nStart + nOffset + k] = (byte)
			    (((int)(oData[oStart + oOffset + k] & 0xff) +
			      (int)(oData[oStart + oOffset + k 
					+ bytesPerPixel] & 0xff) + 1) >> 1); 
                    }      
                    nOffset += bytesPerPixel;
                    oOffset += oPixelIncr;
                }      
	} else if (xScale == 1) {
                for (int y = 0; y < nHeight; y++) {
                    for (int k = 0; k < bytesPerPixel; k++) {
			nData[nStart + nOffset + k] = (byte)
			    (((int)(oData[oStart + oOffset + k] & 0xff) +
			      (int)(oData[oStart + oOffset + k 
					+ oLineIncr] & 0xff) + 1) >> 1); 
                    }      
                    nOffset += bytesPerPixel;
                    oOffset += oLineIncr;
                }      
	} else {
            for (int y = 0; y < nHeight; y++) {
                for (int x = 0; x < nWidth; x++) {
                    for (int k = 0; k < bytesPerPixel; k++) {
			nData[nStart + nOffset + k] = (byte)
			    (((int)(oData[oStart + oOffset + k] & 0xff) +
			      (int)(oData[oStart + oOffset + k  
					+ bytesPerPixel] & 0xff) +
			      (int)(oData[oStart + oOffset + k 
					+ oLineIncr] & 0xff) + 
			      (int)(oData[oStart + oOffset + k + oLineIncr +
					+ bytesPerPixel] & 0xff) + 2) >> 2); 
                    }      
                    nOffset += bytesPerPixel;
                    oOffset += oPixelIncr;
                }      
                oOffset += oLineIncr;
            }  
	}
    }

    void incTextureBinRefCount(TextureBin tb) {

	ImageComponentRetained image;

        textureBinRefCount++;

	// check to see if there is any modifiable images,
	// if yes, add those images to nodeComponentList in RenderBin
	// so that RenderBin can acquire a lock before rendering
        // to prevent updating of image data while rendering

        for (int j = 0; j < numFaces; j++) {
            for (int i = 0; i < maxLevels; i++) {
		image = images[j][i];

		// it is possible that image.source == null because
		// the mipmap could have been created by the library, and
		// hence don't have source and therefore they are
		// guaranteed not modifiable

		if (image != null && 
		     (image.isByReference() ||
		      (image.source != null &&
		       image.source.getCapability(
				ImageComponent.ALLOW_IMAGE_WRITE)))) {
		    tb.renderBin.addNodeComponent(image);
		}
	    }
	}
    }

    void decTextureBinRefCount(TextureBin tb) {

	ImageComponentRetained image;

        textureBinRefCount--;

	// remove any modifiable images from RenderBin nodeComponentList

        for (int j = 0; j < numFaces; j++) {
            for (int i = 0; i < maxLevels; i++) {
		image = images[j][i];
		if (image != null && 
		     (image.isByReference() ||
		      (image.source != null &&
		       image.source.getCapability(
				ImageComponent.ALLOW_IMAGE_WRITE)))) {
		    tb.renderBin.removeNodeComponent(image);
		}
	    }
	}
    }

    
    final void sendMessage(int attrMask, Object attr) {

       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.TEXTURE_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1] = new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.TEXTURE_CHANGED;
		
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    
	    VirtualUniverse.mc.processMessage(createMessage);
	}

    }

    // Issue 121 : Stop using finalize() to clean up state
    // Explore release native resources during clearlive without using finalize.
    protected void finalize() {

	if (objectId > 0) {
	    // memory not yet free
	    // send a message to the request renderer
	    synchronized (VirtualUniverse.mc.contextCreationLock) {
		boolean found = false;

		for (Enumeration e = Screen3D.deviceRendererMap.elements();
		     e.hasMoreElements(); ) {
		    Renderer rdr = (Renderer) e.nextElement();	  
		    J3dMessage renderMessage = VirtualUniverse.mc.getMessage();
		    renderMessage.threads = J3dThread.RENDER_THREAD;
		    renderMessage.type = J3dMessage.RENDER_IMMEDIATE;
		    renderMessage.universe = null;
		    renderMessage.view = null;
		    renderMessage.args[0] = null;
		    renderMessage.args[1] = new Integer(objectId);
		    renderMessage.args[2] = "2D";
		   rdr.rendererStructure.addMessage(renderMessage);
		}
		objectId = -1;
	    }

	    VirtualUniverse.mc.setWorkForRequestRenderer();
	}

    }

    void handleFrequencyChange(int bit) {
        switch (bit) {
        case Texture.ALLOW_ENABLE_WRITE:
        case Texture.ALLOW_IMAGE_WRITE:
        case Texture.ALLOW_LOD_RANGE_WRITE: {
            setFrequencyChangeMask(bit, bit);
        }
        default:
            break;
        }
    }
}

