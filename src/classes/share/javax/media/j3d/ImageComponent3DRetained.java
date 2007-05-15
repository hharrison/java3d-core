/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
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

    void setDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Retrieves the depth of this 3D image component object.
     * @return the format of this 3D image component object
     */
    int getDepth() {
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
    void set(int index, BufferedImage image) {
        
        geomLock.getLock();        
        
        if(byReference) {
            // Fix to issue 488.
            setRefImage(image, index);    
        }

        if(imageData == null) {
            // Only do this once, on the first image            
            // Reset this flag to true, incase it was set to false due to
            // the previous image type.
            abgrSupported = true;
            imageTypeIsSupported = isImageTypeSupported(image);
            imageData = createRenderedImageDataObject(null);
        }
        else {
             if(getImageType() != evaluateImageType(image)) {
                 // TODO need to throw illegal state exception
             }
        }
        
        if (imageTypeIsSupported) {
            copySupportedImageToImageData(image, index, imageData);
        } else {
            // image type is unsupported, need to create a supported local copy.
            // TODO : borrow code from JAI to convert to right format.
            copyUnsupportedImageToImageData(image, index, imageData);

        }    
        
        geomLock.unLock();
        
        if (source.isLive()) {
            // send a IMAGE_CHANGED message in order to
            // notify all the users of the change
            sendMessage(IMAGE_CHANGED, null);
        }        
    }
    
    /**
     * Copies the specified BufferedImage to this 3D image component
     * object at the specified index.
     * @param index the image index
     * @param images BufferedImage object containing the image.
     * The format and size must be the same as the current format in this
     * ImageComponent3D object.  The index must not exceed the depth of this
     * ImageComponent3D object.
     *
    void set(int index, NioImageBuffer nioImage) {
        
        int width = nioImage.getWidth();
        int height = nioImage.getHeight();
        
        if (!byReference) {
            throw new IllegalArgumentException(J3dI18N.getString("Need_New_Message_XXXXXImageComponent2D7"));    
        }
        if (!yUp) {
            throw new IllegalArgumentException(J3dI18N.getString("Need_New_Message_XXXXXImageComponent2D8"));           
        }
        
        if (width != this.width) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D2"));
        }
        if (height != this.height) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D4"));
        }
       
        geomLock.getLock();
        
        setImageClass(nioImage);
        
        // This is a byRef image.                
         setRefImage(nioImage,0);           

        if(imageData == null) {
            // Only do this once, on the first image            
            // Reset this flag to true, incase it was set to false due to
            // the previous image type.
            abgrSupported = true;
            
            imageTypeIsSupported = isImageTypeSupported(nioImage);

       
            // TODO : Need to handle null ....
            imageData = createNioImageBufferDataObject(null);
        }
        else {
             
             //if(getImageType() != evaluateImageType(image)) {
                 // TODO need to throw illegal state exception
             //}
              
        }
        
        if (imageTypeIsSupported) {
             // TODO : Need to handle this ..... case .... 
            // copySupportedImageToImageData(image, index, imageData);
        } else {
             // System.err.println("Image format is unsupported -- illogical case");
            throw new AssertionError();
        }    
        
        geomLock.unLock();
        
        if (source.isLive()) {
            // send a IMAGE_CHANGED message in order to
            // notify all the users of the change
            sendMessage(IMAGE_CHANGED, null);
        }        
    }
    */
            
    void set(int index, RenderedImage image) {
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width != this.width) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D2"));
        }
        if (height != this.height) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D4"));
        }
       
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
    RenderedImage[] getRenderedImage() {
	int i;
	RenderedImage bi[] = new RenderedImage[depth];
        
	if (!byReference) {
	    for (i=0; i<depth; i++) {
                bi[i] = imageData.createBufferedImage(i);
	    }
	}
        else {
            for (i = 0; i < depth; i++) {
                bi[i] = imageData.createBufferedImage(i);
            }
        }
        
        return bi;
    }


    /**
     * Retrieves a copy of the images in this ImageComponent3D object.
     * @return a new array of new BufferedImage objects created from the
     * images in this ImageComponent3D object
     */
    BufferedImage[] getImage() {
	int i;
	BufferedImage bi[] = new BufferedImage[depth];

	if (!byReference) {
	    for (i=0; i<depth; i++) {
                bi[i] = imageData.createBufferedImage(i);
	    }
	} 
        else {
            for (i = 0; i < depth; i++) {
                bi[i] = imageData.createBufferedImage(i);
                if (!(bi[i] instanceof BufferedImage)) {
                    throw new IllegalStateException(J3dI18N.getString("ImageComponent3DRetained0"));
                }

            }
        }
        return bi;
    }

    /**
     * Retrieves a copy of one of the images in this ImageComponent3D object.
     * @param index the index of the image to retrieve
     * @return a new BufferedImage objects created from the
     * image at the specified index in this ImageComponent3D object
     */
    RenderedImage getImage(int index) {
	if (!byReference) {
            return imageData.createBufferedImage(index);
	}
        
        return (RenderedImage) getRefImage(index);
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

        RenderedImage refImage = (RenderedImage) getRefImage(index);
        assert (refImage != null);
        assert (imageData != null);

        
        // update the internal copy of the image data if a copy has been
        // made
        int srcX = x + refImage.getMinX();
        int srcY = y + refImage.getMinY();
        
        if (imageTypeIsSupported) {
            if (refImage instanceof BufferedImage) {
                copyImageLineByLine((BufferedImage)refImage, srcX, srcY, x, y, index, width, height, imageData);
            } else {
                copySupportedImageToImageData(refImage, srcX, srcY, x, y, index, width, height, imageData);
            }
        } else {
            // image type is unsupported, need to create a supported local copy.
            // TODO : Should look into borrow code from JAI to convert to right format.
            if (refImage instanceof BufferedImage) {
                copyUnsupportedImageToImageData((BufferedImage)refImage, srcX, srcY, x, y, index, width, height, imageData);
            } else {
                copyUnsupportedImageToImageData(refImage, srcX, srcY, x, y, index, width, height, imageData);
            }
        }
        
        geomLock.unLock();


        if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info = new ImageComponentUpdateInfo();
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

         if(!isSubImageTypeEqual(image)) {
            throw new IllegalStateException(
                                J3dI18N.getString("ImageComponent2D6"));           
        }

        // Can't be byReference
        assert (!byReference);
        assert (imageData != null);
        
        geomLock.getLock();

        if (imageTypeIsSupported) {            
            // Either not byRef or not yUp or not both
            // System.err.println("ImageComponen3DRetained.setSubImage() : (imageTypeSupported ) --- (1)");
            if (image instanceof BufferedImage) {
                copyImageLineByLine((BufferedImage)image, srcX, srcY, dstX, dstY, index, width, height, imageData);
            }
            else {
                copySupportedImageToImageData(image, srcX, srcY, dstX, dstY, index, width, height, imageData);
            }
       } else {
            // image type is unsupported, need to create a supported local copy.
            // TODO : Should look into borrow code from JAI to convert to right format.
            // System.err.println("ImageComponent3DRetained.setSubImage() : (imageTypeSupported == false) --- (2)");
             if (image instanceof BufferedImage) {
                copyUnsupportedImageToImageData((BufferedImage)image, srcX, srcY, dstX, dstY, index, width, height, imageData);             
            }
            else {
                copyUnsupportedImageToImageData(image, srcX, srcY, dstX, dstY, index, width, height, imageData);
            }
        }    

        geomLock.unLock();


        if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info = new ImageComponentUpdateInfo();
            info.x = dstX;
            info.y = dstY;
	    info.z = index;
            info.width = width;
            info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
        }
    }
}
