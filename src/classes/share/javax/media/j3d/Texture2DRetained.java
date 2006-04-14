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
import javax.vecmath.*;

/**
 * Texture2D is a subclass of Texture class. It extends Texture
 * class by adding a constructor and a mutator method for
 * setting a 2D texture image.
 */
class Texture2DRetained extends TextureRetained {

    // currently detail image is only applicable to 2D texture

    // detail texture info

    int detailTextureId = -1;
    ImageComponent2DRetained detailImage = null;
    DetailTextureImage detailTexture = null;
    int detailTextureMode = Texture2D.DETAIL_MODULATE;
    int detailTextureLevel = 2;
    int numDetailTextureFuncPts = 0;
    float detailTextureFuncPts[] = null;  // array of pairs of floats
                                           // first value for LOD
                                           // second value for the fcn value

    /**
     * Set detail texture image
     */
    final void initDetailImage(ImageComponent2D image) {
	if (image == null) {
	    detailImage = null;
	} else {
	    detailImage = (ImageComponent2DRetained)image.retained;
	    detailImage.setTextureRef();
	}
    }


    /**
     * Get detail texture image
     */
    final ImageComponent2D getDetailImage() {
	if (detailImage != null) {
	    return (ImageComponent2D)detailImage.source;
	} else {
	    return null;
	}
    }


    /**
     * Set detail texture mode
     */
    final void initDetailTextureMode(int mode) {
	detailTextureMode = mode;
    }


    /**
     * Get detail texture mode
     */
    final int getDetailTextureMode() {
	return detailTextureMode;
    }


    /**
     * Set detail texture level
     */
    final void initDetailTextureLevel(int level) {
	detailTextureLevel = level;
    }


    /**
     * Get detail texture level
     */
    final int getDetailTextureLevel() {
	return detailTextureLevel;
    }


    /**
     * Set detail texture function
     */
    final void initDetailTextureFunc(float[] lod, float[] pts) {
        if (lod == null) {  // pts will be null too.
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = lod.length;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != lod.length * 2)) {
                detailTextureFuncPts = new float[lod.length * 2];
            }
            for (int i = 0, j = 0; i < lod.length; i++) {
                detailTextureFuncPts[j++] = lod[i];
                detailTextureFuncPts[j++] = pts[i];
            }
        }
    }

    final void initDetailTextureFunc(Point2f[] pts) {
        if (pts == null) {
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = pts.length;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != pts.length * 2)) {
                detailTextureFuncPts = new float[pts.length * 2];
            }
            for (int i = 0, j = 0; i < pts.length; i++) {
                detailTextureFuncPts[j++] = pts[i].x;
                detailTextureFuncPts[j++] = pts[i].y;
            }
        }
    }

    final void initDetailTextureFunc(float[] pts) {
        if (pts == null) {
            detailTextureFuncPts = null;
	    numDetailTextureFuncPts = 0;
        } else {
	    numDetailTextureFuncPts = pts.length / 2;
            if ((detailTextureFuncPts == null) ||
                    (detailTextureFuncPts.length != pts.length)) {
                detailTextureFuncPts = new float[pts.length];
            }
            for (int i = 0; i < pts.length; i++) {
                detailTextureFuncPts[i] = pts[i];
            }
        }
    }

    /**
     * Get number of points in the detail texture LOD function
     */
    final int getDetailTextureFuncPointsCount() {
        return numDetailTextureFuncPts;
    }


    /**
     * Copies the array of detail texture LOD function points into the
     * specified arrays
     */
    final void getDetailTextureFunc(float[] lod, float[] pts) {
        if (detailTextureFuncPts != null) {
            for (int i = 0, j = 0; i < numDetailTextureFuncPts; i++) {
                lod[i] = detailTextureFuncPts[j++];
                pts[i] = detailTextureFuncPts[j++];
            }
        }
    }

    final void getDetailTextureFunc(Point2f[] pts) {
        if (detailTextureFuncPts != null) {
            for (int i = 0, j = 0; i < numDetailTextureFuncPts; i++) {
                pts[i].x = detailTextureFuncPts[j++];
                pts[i].y = detailTextureFuncPts[j++]; 
	    } 
	}
    }


    /**
     * internal method only -- returns the detail texture LOD function
     */
    final float[] getDetailTextureFunc() {
        return detailTextureFuncPts;
    }

    synchronized void initMirrorObject() {

	super.initMirrorObject();

	Texture2DRetained mirrorTexture = (Texture2DRetained)mirror;

	// detail texture info
	mirrorTexture.detailImage = detailImage;
	mirrorTexture.detailTextureMode = detailTextureMode;
        mirrorTexture.detailTextureLevel = detailTextureLevel;
        mirrorTexture.detailTexture = null;
	mirrorTexture.numDetailTextureFuncPts = numDetailTextureFuncPts;

	if (detailTextureFuncPts == null) {
	    mirrorTexture.detailTextureFuncPts = null;
	} else {
	    if ((mirrorTexture.detailTextureFuncPts == null) ||
		    (mirrorTexture.detailTextureFuncPts.length !=
			detailTextureFuncPts.length)) {
		mirrorTexture.detailTextureFuncPts =
			new float[detailTextureFuncPts.length];
	    }
	    for (int i = 0; i < detailTextureFuncPts.length; i++) {
		mirrorTexture.detailTextureFuncPts[i] =
			detailTextureFuncPts[i];
	    }

	    // add detail texture to the user list of the image
	    // only if detail texture is to be used
	    if ((mirrorTexture.detailImage != null) &&
	            (mirrorTexture.magFilter >= Texture2D.LINEAR_DETAIL) &&
		    (mirrorTexture.magFilter <= Texture2D.LINEAR_DETAIL_ALPHA)) {
                mirrorTexture.detailImage.addUser(mirrorTexture);
	    }
	}
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);

	// remove detail texture from the user list of the image
	if ((detailImage != null) &&
	        (magFilter >= Texture2D.LINEAR_DETAIL) &&
	    	(magFilter <= Texture2D.LINEAR_DETAIL_ALPHA)) {
	    detailImage.removeUser(mirror);
	}
    }

    // overload the incTextureBinRefCount method to take care
    // of detail texture ref as well
    // This method is called from RenderBin when a new TextureBin
    // is created. And all the referenced textures in that TextureBin
    // will be notified to increment the TextureBin reference count.

    void incTextureBinRefCount(TextureBin tb) {
	super.incTextureBinRefCount(tb);

	// increment detail texture ref count

	if ((detailImage != null) &&
		(magFilter >= Texture2D.LINEAR_DETAIL) &&
		(magFilter <= Texture2D.LINEAR_DETAIL_ALPHA)) {
	    if (detailTexture == null) {
		detailTexture = detailImage.getDetailTexture();
	    }

	    detailTexture.incTextureBinRefCount(format, tb);
	}
    }

    // This method is called from AttributeBin when a TextureBin
    // is to be removed. And all the referenced textures in that TextureBin
    // will be notified to decrement the TextureBin reference count.
    // And if detail texture exists, we need to decrement the
    // TextureBin reference count of the detail texture as well.

    void decTextureBinRefCount(TextureBin tb) {
	super.decTextureBinRefCount(tb);

	// decrement detail texture ref count

	if (detailTexture != null) {
	    detailTexture.decTextureBinRefCount(format, tb);
	}
    }

    void updateNative(Canvas3D cv) {

	// update mipmap texture

	super.updateNative(cv);


	// update detail texture if exists

	if (detailTexture != null) {
	    detailTexture.updateNative(cv, format);
	}
    }


    // update texture parameters

    void updateTextureFields(Canvas3D cv) {
	
	super.updateTextureFields(cv);

	// update detail texture parameters if applicable

	if (detailTexture != null) {
	    
            Pipeline.getPipeline().updateDetailTextureParameters(cv.ctx,
                    detailTextureMode,
                    detailTextureLevel, numDetailTextureFuncPts,
                    detailTextureFuncPts);
	}
    }

}

