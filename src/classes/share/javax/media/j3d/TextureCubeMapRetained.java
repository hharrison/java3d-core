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

import java.util.*;
import javax.vecmath.Color4f;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

/**
 * TextureCubeMap is a subclass of Texture class.
 */
class TextureCubeMapRetained extends TextureRetained {


    static final int NUMFACES = 6;


    void initialize(int	format, int width, int widPower, 
			int height, int heiPower, int mipmapMode, 
			int boundaryWidth) {

	this.numFaces = 6;

	super.initialize(format, width, widPower, height, heiPower,
				mipmapMode, boundaryWidth);
    }


    /**
     * Sets a specified mipmap level for a particular face of the cubemap.
     */
    void initImage(int level, int face, ImageComponent image) {

        // Issue 172 : call checkImageSize even for non-live setImage calls
        checkImageSize(level, image);

	if (this.images == null) {
            throw new IllegalArgumentException(
			J3dI18N.getString("TextureRetained0"));
	} 

        if (image instanceof ImageComponent3D) {
            throw new IllegalArgumentException(
			J3dI18N.getString("TextureCubeMap3"));
        }


	if (face < TextureCubeMap.POSITIVE_X ||
			face > TextureCubeMap.NEGATIVE_Z) {
            throw new IllegalArgumentException(
			J3dI18N.getString("TextureCubeMap4"));
	}

	if (this.source.isLive()) {
	    if (this.images[face][level] != null) {
		this.images[face][level].clearLive(refCount);
	    }

	    
	    if (image != null) {
		((ImageComponentRetained)image.retained).setLive(
			inBackgroundGroup, refCount);
	    }
	}

        /*  Don't think this is needed.   --- Chien.
         ((ImageComponent2DRetained)image.retained).setTextureRef();
        */
        
	if (image != null) {
	    this.images[face][level] = (ImageComponentRetained)image.retained;
	} else {
	    this.images[face][level] = null;
	}
    }

    final void setImage(int level, int face, ImageComponent image) {

	initImage(level, face, image);

        Object arg[] = new Object[3];
	arg[0] = new Integer(level);
	arg[1] = image;
	arg[2] = new Integer(face);
	sendMessage(IMAGE_CHANGED, arg);

	// If the user has set enable to true, then if the image is null
	// turn off texture enable
	if (userSpecifiedEnable) {
	    enable = userSpecifiedEnable;
	    if (image != null && level < maxLevels) {
		ImageComponentRetained img= (ImageComponentRetained)image.retained;
		if (img.isByReference()) {
		    if (img.getRefImage(0) == null) {
			enable = false;
		    }
		}
		else {
		    if (img.getImageData(isUseAsRaster()).get() == null) {
			enable = false;
		    }
		}
		if (!enable) 
		    sendMessage(ENABLE_CHANGED, Boolean.FALSE);
	    }
	}
    }

    void initImages(int face, ImageComponent[] images) {

	if (images.length != maxLevels)
            throw new IllegalArgumentException(J3dI18N.getString("Texture20"));

	for (int i = 0; i < images.length; i++) {
	     initImage(i, face, images[i]);
	}
    }

    final void setImages(int face, ImageComponent[] images) {

        int i;

        initImages(face, images);

	ImageComponent [] imgs = new ImageComponent[images.length];
	for (i = 0; i < images.length; i++) {
	     imgs[i] = images[i];
	}

        Object args[] = new Object[2];
        args[0] = imgs;
        args[1] = new Integer(face);

	sendMessage(IMAGES_CHANGED, args);
	// If the user has set enable to true, then if the image is null
	// turn off texture enable
	if (userSpecifiedEnable) {
	    enable = userSpecifiedEnable;
	    i = 0;
	    while (enable && i < maxLevels) {
		if (images[i] != null) {
		    ImageComponentRetained img= (ImageComponentRetained)images[i].retained;
		    if (img.isByReference()) {
			if (img.getRefImage(0) == null) {
			    enable = false;
			}
		    }
		    else {
			if (img.getImageData(isUseAsRaster()).get() == null) {
			    enable = false;
			}
		    }
		}
		i++;
	    }
	    if (!enable) {
		sendMessage(ENABLE_CHANGED, Boolean.FALSE);
	    }
	}
    }

	


    /**
     * Gets a specified mipmap level of a particular face of the cube map.
     * @param level mipmap level to get
     * @param face face of the cube map
     * @return the pixel array object containing the texture image
     */
    final ImageComponent getImage(int level, int face) {

	if (face < TextureCubeMap.POSITIVE_X ||
			face > TextureCubeMap.NEGATIVE_Z) {
            throw new IllegalArgumentException(
			J3dI18N.getString("TextureCubeMap4"));
	}

	return  (((images != null) && (images[face][level] != null)) ? 
		 (ImageComponent)images[face][level].source : null);
    }


    /**
     * Gets an array of image for a particular face of the cube map.
     * @param face face of the cube map
     * @return the pixel array object containing the texture image
     */
    final ImageComponent[] getImages(int face) {

	if (images == null)
	    return null;

	if (face < TextureCubeMap.POSITIVE_X ||
			face > TextureCubeMap.NEGATIVE_Z) {
            throw new IllegalArgumentException(
			J3dI18N.getString("TextureCubeMap4"));
	}

	ImageComponent [] rImages = new ImageComponent[images[face].length];
	for (int i = 0; i < images[face].length; i++) {
	    if (images[face][i] != null)
	        rImages[i] = (ImageComponent)images[face][i].source;
	    else
		rImages[i] = null;
	}
	return rImages;
    }


    void bindTexture(Context ctx, int objectId, boolean enable) {
        Pipeline.getPipeline().bindTextureCubeMap(ctx, objectId, enable);
    }

    void updateTextureBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {

        Pipeline.getPipeline().updateTextureCubeMapBoundary(ctx,
                boundaryModeS, boundaryModeT,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    void updateTextureFilterModes(Context ctx,
            int minFilter, int magFilter) {

        Pipeline.getPipeline().updateTextureCubeMapFilterModes(ctx,
                minFilter, magFilter);
    }

    void updateTextureSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {

        Pipeline.getPipeline().updateTextureCubeMapSharpenFunc(ctx,
            numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    void updateTextureFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {

        Pipeline.getPipeline().updateTextureCubeMapFilter4Func(ctx,
                numFilter4FuncPts, filter4FuncPts);
    }

    void updateTextureAnisotropicFilter(Context ctx, float degree) {
        Pipeline.getPipeline().updateTextureCubeMapAnisotropicFilter(ctx, degree);
    }


    void updateTextureLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {

        Pipeline.getPipeline().updateTextureCubeMapLodRange(ctx, baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    void updateTextureLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {

        Pipeline.getPipeline().updateTextureCubeMapLodOffset(ctx,
                lodOffsetX, lodOffsetY, lodOffsetZ);
    }


    /**
     * Load level 0 explicitly with null data pointer to allow
     * mipmapping when level 0 is not the base level
     */
    void updateTextureDimensions(Canvas3D cv) {
        if(images[0][0] != null) {
            // All faces should have the same image format and type.
            int imageFormat = images[0][0].getImageFormatTypeIntValue(false);
            int imageType = images[0][0].getImageDataTypeIntValue();
            
            for (int i = 0; i < 6; i++) {
                updateTextureImage(cv, i, maxLevels, 0,
                        format, imageFormat,
                        width, height, boundaryWidth,
                        imageType, null);               
            }
        }
    }



    // This is just a wrapper of the native method.
    void updateTextureImage(Canvas3D cv,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth, int imageDataType,
            Object imageData) {

        Pipeline.getPipeline().updateTextureCubeMapImage(cv.ctx,
                face, numLevels, level,
                textureFormat, imageFormat,
                width, height,
                boundaryWidth, imageDataType, imageData);
    }

    // This is just a wrapper of the native method.
    void updateTextureSubImage(Canvas3D cv,
            int face, int level,
            int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData) {

        Pipeline.getPipeline().updateTextureCubeMapSubImage(cv.ctx,
                face, level, xoffset, yoffset,
                textureFormat, imageFormat,
                imgXOffset, imgYOffset,
                tilew, width, height,
                imageDataType, imageData);

    }
}
