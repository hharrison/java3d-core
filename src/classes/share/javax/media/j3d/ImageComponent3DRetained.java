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

import java.awt.image.*;

/**
 * This class defines a 3D array of pixels.
 * This is used for texture images.
 */

class ImageComponent3DRetained extends ImageComponentRetained {
    int		depth;		// Depth of 3D image

    void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Retrieves the depth of this 3D image component object.
     * @return the format of this 3D image component object
     */
    final int getDepth() {
        return depth;
    }

    /**
     * Copies the specified BufferedImage to this 3D image component
     * object at the specified index.
     * @param index the image index
     * @param images BufferedImage object containing the image.
     * The format and size must be the same as the current format in this
     * ImageComponent3D object.  The index must not exceed the depth of this
     * ImageComponent3D object.
     */
    final void set(int index, BufferedImage image) {
        if (imageYup == null) 
            imageYup = new byte[height * width * depth * bytesPerPixelIfStored];
        imageDirty[index] = true;
	storedYupFormat = internalFormat;
	bytesPerYupPixelStored = bytesPerPixelIfStored;
        copyImage(image, imageYup, true, index, storedYupFormat,
			bytesPerYupPixelStored);
	if (byReference)
	    bImage[index] = image;
    }

    final void set(int index, RenderedImage image) {
	if (image instanceof BufferedImage) {
	    set(index, ((BufferedImage)image));
	}
	else {
	    // Create a buffered image from renderImage
	    ColorModel cm = image.getColorModel();
	    WritableRaster wRaster = image.copyData(null);
	    BufferedImage bi = new BufferedImage(cm,
						 wRaster,
						 cm.isAlphaPremultiplied()
						 ,null);
	    set(index, bi);
	}
    }

    /**
     * Retrieves a copy of the images in this ImageComponent3D object.
     * @return a new array of new BufferedImage objects created from the
     * images in this ImageComponent3D object
     */
    final RenderedImage[] getRenderedImage() {
	int i;
	RenderedImage bi[] = new RenderedImage[bImage.length];
	if (!byReference) {
	    for (i=0; i<depth; i++) {
		if (imageDirty[i]) {
		    retrieveBufferedImage(i);
		}
	    }
	}
	for (i = 0; i < bImage.length; i++) {
	    bi[i] = bImage[i];
	}
	// If by reference, then the image should not be dirty
        return bi;
    }


    /**
     * Retrieves a copy of the images in this ImageComponent3D object.
     * @return a new array of new BufferedImage objects created from the
     * images in this ImageComponent3D object
     */
    final BufferedImage[] getImage() {
	int i;
	BufferedImage bi[] = new BufferedImage[bImage.length];

	if (!byReference) {
	    for (i=0; i<depth; i++) {
		if (imageDirty[i]) {
		    retrieveBufferedImage(i);
		}
	    }
	} 

	for (i = 0; i < bImage.length; i++) {
	    if (!(bImage[i] instanceof BufferedImage)) {
		throw new IllegalStateException(J3dI18N.getString("ImageComponent3DRetained0"));
	    }
	    bi[i] = (BufferedImage) bImage[i];
	}
	// If by reference, then the image should not be dirty
        return bi;
    }

    /**
     * Retrieves a copy of one of the images in this ImageComponent3D object.
     * @param index the index of the image to retrieve
     * @return a new BufferedImage objects created from the
     * image at the specified index in this ImageComponent3D object
     */
    final RenderedImage getImage(int index) {
	if (!byReference) {
	    if (imageDirty[index]) {
		retrieveBufferedImage(index);
	    }
	}
        return bImage[index];
    }

    /**
     * Update data.
     * x and y specifies the x & y offset of the image data in
     * ImageComponent.  It assumes that the origin is (0, 0).
     */
    void updateData(ImageComponent3D.Updater updater,
                        int index, int x, int y, int width, int height) {

        geomLock.getLock();

        // call the user supplied updateData method to update the data
        updater.updateData((ImageComponent3D)source, index, x, y, width, height);

        // update the internal copy of the image data if a copy has been
        // made
        if (imageYupAllocated) {
            copyImage(bImage[0], (x + bImage[0].getMinX()),
                        (y + bImage[0].getMinY()), imageYup, x, y,
                        true, index, width, height, storedYupFormat,
                                bytesPerYupPixelStored);
        }

	imageDirty[index] = true;

        geomLock.unLock();


        if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info = VirtualUniverse.mc.getFreeImageUpdateInfo();
            info.x = x;
            info.y = y;
	    info.z = index;
            info.width = width;
            info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
        }
    }

    void setSubImage(int index, RenderedImage image, int width, int height,
                        int srcX, int srcY, int dstX, int dstY) {

        geomLock.getLock();

        if (imageYupAllocated) {
            copyImage(image, srcX, srcY, imageYup, dstX, dstY,
                        true, index, width, height, storedYupFormat,
                                bytesPerYupPixelStored);
        }

	imageDirty[index] = true;

        geomLock.unLock();


        if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info = VirtualUniverse.mc.getFreeImageUpdateInfo();
            info.x = dstX;
            info.y = dstY;
	    info.z = index;
            info.width = width;
            info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
        }
    }
}
