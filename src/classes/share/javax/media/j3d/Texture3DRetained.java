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
import java.util.Enumeration;
import java.util.BitSet;

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
    native void bindTexture(long ctx, int objectId, boolean enable);

    native void updateTextureBoundary(long ctx,
				   int boundaryModeS, int boundaryModeT, 
				   int boundaryModeR, float boundaryRed, 
				   float boundaryGreen, float boundaryBlue, 
				   float boundaryAlpha);
					
    native void updateTextureFilterModes(long ctx,
                                        int minFilter, int magFilter);

    native void updateTextureSharpenFunc(long ctx,
                                   int numSharpenTextureFuncPts,
                                   float[] sharpenTextureFuncPts);

    native void updateTextureFilter4Func(long ctx,
                                   int numFilter4FuncPts,
                                   float[] filter4FuncPts);

    native void updateTextureAnisotropicFilter(long ctx, float degree);


    native void updateTextureImage(long ctx, int numLevels, int level,
				   int format, int internalFormat, int width, 
				   int height, int depth, 
				   int boundaryWidth, byte[] imageYup);

    native void updateTextureSubImage(long ctx, int level,
				   int xoffset, int yoffset, int zoffset,
				   int internalFormat, int format, 
				   int imgXoffset, int imgYoffset, int imgZoffset,
				   int tilew, int tileh,
				   int width, int height, int depth, 
				   byte[] imageYup);


    // get an ID for Texture3D 

    int getTextureId() {
        return (VirtualUniverse.mc.getTexture3DId());
    }


    // get a Texture3D Id

    void freeTextureId(int id) {
	synchronized (resourceLock) {
	    if (objectId == id) {
		objectId = -1;
		VirtualUniverse.mc.freeTexture3DId(id);
	    }
	}
    }


    // load level 0 image with null data pointer, just to enable
    // mipmapping when level 0 is not the base level

    void updateTextureDimensions(Canvas3D cv) {
        updateTextureImage(cv.ctx, maxLevels, 0,
                format, ImageComponentRetained.BYTE_RGBA,
                width, height, depth, boundaryWidth, null);
    }


    void updateTextureBoundary(Canvas3D cv) {
	updateTextureBoundary(cv.ctx, 
			boundaryModeS, boundaryModeT, boundaryModeR,
			boundaryColor.x, boundaryColor.y,
			boundaryColor.z, boundaryColor.w);
		
    }

    void reloadTextureImage(Canvas3D cv, int face, int level,
			ImageComponentRetained image, int numLevels) {

/*
        System.out.println("Texture3D.reloadTextureImage: level= " + level +
		" image.imageYup= " + image.imageYup + " w= " + image.width + 
		" h= " + image.height + " d= " + depth + 
		" numLevels= " + numLevels);
*/


        updateTextureImage(cv.ctx,  numLevels, level, format,
                                image.storedYupFormat,
                                image.width, image.height, depth,
				boundaryWidth, image.imageYup);

    }

    void reloadTextureSubImage(Canvas3D cv, int level, int face,
				ImageComponentUpdateInfo info,
				ImageComponentRetained image) {
	int x = info.x,
	    y = info.y,
	    z = info.z,
	    width = info.width,
	    height = info.height;

        int xoffset = x - image.minX;
        int yoffset = y - image.minY;

        updateTextureSubImage(cv.ctx, level, xoffset, yoffset, z,
				format, image.storedYupFormat, 
				xoffset, yoffset, z, 
				image.width, image.height, 
				width, height, 1, image.imageYup);
    }



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
		    renderMessage.args[2] = "3D";
		    rdr.rendererStructure.addMessage(renderMessage);
		}
		objectId = -1;
	    }
	    VirtualUniverse.mc.setWorkForRequestRenderer();
	}
    }

}
