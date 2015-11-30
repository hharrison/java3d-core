/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * class by adding a third co-ordinate, constructor and a mutator
 * method for setting a 3D texture image.
 */

class Texture3DRetained extends TextureRetained {
    // Boundary mode for R coordinate (wrap, clamp)
    int		boundaryModeR = Texture.WRAP;
    int		depth = 1;	// Depth (num slices) of texture map (2**p)

    final void setDepth(int depth) {
	this.depth = depth;
    }

    final int getDepth() {
	return this.depth;
    }

    /**
     * Sets the boundary mode for the R coordinate in this texture object.
     * @param boundaryModeR the boundary mode for the R coordinate,
     * one of: CLAMP or WRAP.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final void initBoundaryModeR(int boundaryModeR) {
	this.boundaryModeR = boundaryModeR;
    }

    /**
     * Retrieves the boundary mode for the R coordinate.
     * @return the current boundary mode for the R coordinate.
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    final int getBoundaryModeR() {
	return boundaryModeR;
    }

    /**
     * This method updates the native context.
     */
    @Override
    void bindTexture(Context ctx, int objectId, boolean enable) {
        Pipeline.getPipeline().bindTexture3D(ctx, objectId, enable);
    }

    void updateTextureBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {

        Pipeline.getPipeline().updateTexture3DBoundary(ctx,
                boundaryModeS, boundaryModeT, boundaryModeR,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    @Override
    void updateTextureFilterModes(Context ctx,
            int minFilter, int magFilter) {

        Pipeline.getPipeline().updateTexture3DFilterModes(ctx,
                minFilter, magFilter);
    }

    @Override
    void updateTextureSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {

        Pipeline.getPipeline().updateTexture3DSharpenFunc(ctx,
            numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    @Override
    void updateTextureFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {

        Pipeline.getPipeline().updateTexture3DFilter4Func(ctx,
                numFilter4FuncPts, filter4FuncPts);
    }

    @Override
    void updateTextureAnisotropicFilter(Context ctx, float degree) {
        Pipeline.getPipeline().updateTexture3DAnisotropicFilter(ctx, degree);
    }



    // Wrapper around the native call for 3D textures
    void updateTextureImage(Canvas3D cv,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth, int imageDataType,
            Object imageData) {

        Pipeline.getPipeline().updateTexture3DImage(cv.ctx,
                numLevels, level,
                textureFormat, imageFormat,
                width, height, depth,
                boundaryWidth, imageDataType, imageData, useAutoMipMapGeneration(cv));
    }

    // Wrapper around the native call for 3D textures
    void updateTextureSubImage(Canvas3D cv,
            int face, int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset, int imgZOffset,
            int tilew, int tileh, int width, int height, int depth,
            int imageDataType, Object imageData) {

        Pipeline.getPipeline().updateTexture3DSubImage(cv.ctx,
                level, xoffset, yoffset, zoffset,
                textureFormat, imageFormat,
                imgXOffset, imgYOffset, imgZOffset,
                tilew, tileh, width, height, depth,
                imageDataType, imageData, useAutoMipMapGeneration(cv));
    }

    // load level 0 image with null data pointer, just to enable
    // mipmapping when level 0 is not the base level

    @Override
    void updateTextureDimensions(Canvas3D cv) {
        if(images[0][0] != null) {
            updateTextureImage(cv, maxLevels, 0, 0,
                    format, images[0][0].getImageFormatTypeIntValue(false),
                    width, height, depth, boundaryWidth,
                    images[0][0].getImageDataTypeIntValue(), null);
        }
    }


    @Override
    void updateTextureBoundary(Canvas3D cv) {
        updateTextureBoundary(cv.ctx,
                boundaryModeS, boundaryModeT, boundaryModeR,
                boundaryColor.x, boundaryColor.y,
                boundaryColor.z, boundaryColor.w);
    }

    @Override
    void updateTextureLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {

        Pipeline.getPipeline().updateTexture3DLodRange(ctx, baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    @Override
    void updateTextureLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {

        Pipeline.getPipeline().updateTexture3DLodOffset(ctx,
                lodOffsetX, lodOffsetY, lodOffsetZ);
    }

    @Override
    void reloadTextureImage(Canvas3D cv, int face, int level,
			ImageComponentRetained image, int numLevels) {

/*
        System.err.println("Texture3D.reloadTextureImage: level= " + level +
		" image.imageYup= " + image.imageYup + " w= " + image.width +
		" h= " + image.height + " d= " + depth +
		" numLevels= " + numLevels);
*/

        // Texture3D does not need to support Raster
        ImageComponentRetained.ImageData imageData = image.getImageData(false);

        updateTextureImage(cv,
                0, numLevels, level, format,
                image.getImageFormatTypeIntValue(false),
                image.width, image.height, depth,
                boundaryWidth, image.getImageDataTypeIntValue(),
                imageData.get());
    }

    @Override
    void reloadTextureSubImage(Canvas3D cv, int level, int face,
				ImageComponentUpdateInfo info,
				ImageComponentRetained image) {
	int x = info.x,
	    y = info.y,
	    z = info.z,
	    width = info.width,
	    height = info.height;

        int xoffset = x;
        int yoffset = y;
        // Texture3D does not need to support Raster
        ImageComponentRetained.ImageData imageData = image.getImageData(false);

        updateTextureSubImage(cv,
                0, level, xoffset, yoffset, z,
                format, image.getImageFormatTypeIntValue(false),
                xoffset, yoffset, z,
                image.width, image.height,
                width, height, 1, image.getImageDataTypeIntValue(),
                imageData.get());
    }

}
