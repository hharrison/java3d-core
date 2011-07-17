/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.awt.image.*;
import java.awt.color.ColorSpace;

/**
 * This class defines a 2D image component.
 * This is used for texture images, background images and raster components
 * of Shape3D nodes.
 */

class ImageComponent2DRetained extends ImageComponentRetained {

    ImageComponent2DRetained() {
    }
       
    /**
     * This method handles NioImageBuffer
     * Refers or copies the specified NioImageBuffer to this 2D image component object.
     * @param image NioImageBuffer object containing the image.
     * The format and size must be the same as the current format in this
     * ImageComponent2D object.
     */
    void set(NioImageBuffer image) {

        int width = image.getWidth();
        int height = image.getHeight();

        if (!byReference) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2D7"));    
        }
        if (!yUp) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2D8"));           
        }
        
        if (width != this.width) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained0"));
        }
        if (height != this.height) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained1"));
        }
        
        geomLock.getLock();

        setImageClass(image);

        // This is a byRef image.
        setRefImage(image,0);

        // Reset this flag to true, incase it was set to false due to
        // the previous image type.
        abgrSupported = true;

        imageTypeIsSupported = isImageTypeSupported(image);

        if (imageTypeIsSupported) {
            
            /* Use reference when ( format is OK, Yup is true, and byRef is true). */
            // Create image data object with the byRef image. */
            imageData = createNioImageBufferDataObject(image);
            
        } else {
            // Handle abgrSupported is false case.
            imageData = createNioImageBufferDataObject(null);
            copyUnsupportedNioImageToImageData(image, 0, 0, 0, 0, width, height, imageData);

        }
        
        geomLock.unLock();
        
        if (source.isLive()) {
            // send a IMAGE_CHANGED message in order to
            // notify all the users of the change
            sendMessage(IMAGE_CHANGED, null);
        }
    }
    
     /**
     * This method handles both BufferedImage and RenderedImage
     * Copies the specified RenderedImage to this 2D image component object.
     * @param image RenderedImage object containing the image.
     * The format and size must be the same as the current format in this
     * ImageComponent2D object.
     */
    void set(RenderedImage image) {
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        if (width != this.width) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained0"));
        }
        if (height != this.height) {
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained1"));
        }
        
        setImageClass(image);
        
        geomLock.getLock();        
        
        if (byReference) {            
            setRefImage(image,0);    
        }
        
        // Reset this flag to true, incase it was set to false due to 
        // the previous image type.
        abgrSupported = true;
        
        imageTypeIsSupported = isImageTypeSupported(image);
        
        if (imageTypeIsSupported) {

            if (byReference && yUp) {
                /* Use reference when ( format is OK, Yup is true, and byRef is true). */
                // System.err.println("ImageComponent2DRetained.set() : (imageTypeSupported && byReference && yUp) --- (1)");
                if (image instanceof BufferedImage) {
                    // Create image data object with the byRef image. */                    
                    imageData = createRenderedImageDataObject(image); 
                }
                else {
                    // System.err.println("byRef and not BufferedImage !!!");
                    imageData = null;
                }

            } else { 
                // Either not byRef or not yUp or not both
                // System.err.println("ImageComponent2DRetained.set() : (imageTypeSupported && ((!byReference && yUp) || (imageTypeSupported && !yUp)) --- (2)");

                // Create image data object with buffer for image. */
                imageData = createRenderedImageDataObject(null);
                copySupportedImageToImageData(image, 0, imageData);
            }

        } else {
            // image type is unsupported, need to create a supported local copy.
            // TODO : borrow code from JAI to convert to right format.
            // System.err.println("ImageComponent2DRetained.set() : (imageTypeSupported == false) --- (4)");
            /* Will use the code segment in copy() method */

            // Create image data object with buffer for image. */
            imageData = createRenderedImageDataObject(null);
            copyUnsupportedImageToImageData(image, 0, imageData);

        }    
        
        geomLock.unLock();
        
        if (source.isLive()) {
            // send a IMAGE_CHANGED message in order to
            // notify all the users of the change
            sendMessage(IMAGE_CHANGED, null);
        }
    }

    void setSubImage(RenderedImage image, int width, int height, 
			   int srcX, int srcY, int dstX, int dstY) {

        if (!isSubImageTypeEqual(image)) {
            throw new IllegalStateException(
                                J3dI18N.getString("ImageComponent2D6"));           
        }

        // Can't be byReference
        assert (!byReference);
        assert (imageData != null);

        geomLock.getLock();

        if (imageTypeIsSupported) {            
            // Either not byRef or not yUp or not both
            // System.err.println("ImageComponent2DRetained.setSubImage() : (imageTypeSupported ) --- (1)");
            if (image instanceof BufferedImage) {
                copyImageLineByLine((BufferedImage)image, srcX, srcY, dstX, dstY, 0, width, height, imageData);
            }
            else {
                copySupportedImageToImageData(image, srcX, srcY, dstX, dstY, 0, width, height, imageData);
            }
       } else {
            // image type is unsupported, need to create a supported local copy.
            // TODO : Should look into borrow code from JAI to convert to right format.
            // System.err.println("ImageComponent2DRetained.setSubImage() : (imageTypeSupported == false) --- (2)");
             if (image instanceof BufferedImage) {
                copyUnsupportedImageToImageData((BufferedImage)image, srcX, srcY, dstX, dstY, 0, width, height, imageData);             
            }
            else {
                copyUnsupportedImageToImageData(image, srcX, srcY, dstX, dstY, 0, width, height, imageData);
            }
        }    
        geomLock.unLock();

        if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info =  new ImageComponentUpdateInfo();
            info.x = dstX;
            info.y = dstY;
	    info.z = 0;
            info.width = width;
            info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
        }
    }    
    
    /**
     * Retrieves a copy of the image in this ImageComponent2D object.
     * @return a new RenderedImage object created from the image in this
     * ImageComponent2D object
     */
    RenderedImage getImage() {
        
        if (isByReference()) {
            return (RenderedImage) getRefImage(0);
        }
        
        if(imageData != null) {
            return imageData.createBufferedImage(0);
        }
        
        return null;
    }

    /**
     * Retrieves the reference of the nio image in this ImageComponent2D object.
     */
    NioImageBuffer getNioImage() {

        if (getImageClass() != ImageComponent.ImageClass.NIO_IMAGE_BUFFER) {
             throw new IllegalStateException(J3dI18N.getString("ImageComponent2D9"));          
        }
        
        assert (byReference == true);
        
        return (NioImageBuffer) getRefImage(0);
    }

    /**
     * Update data.
     * x and y specifies the x & y offset of the image data in
     * ImageComponent.  It assumes that the origin is (0, 0).
     */
    void updateData(ImageComponent2D.Updater updater,
		    int x, int y, int width, int height) {

	geomLock.getLock();
	// call the user supplied updateData method to update the data
	updater.updateData((ImageComponent2D)source, x, y, width, height);

        Object refImage = getRefImage(0);
        assert (refImage != null);
        assert (imageData != null);
        
        // Check is data copied internally.
        if(!imageData.isDataByRef()) {
            // update the internal copy of the image data if a copy has been
            // made            
            if (imageTypeIsSupported) {
                assert !(refImage instanceof NioImageBuffer);

                if (refImage instanceof BufferedImage) {
                    copyImageLineByLine((BufferedImage)refImage, x, y, x, y, 0, width, height, imageData);
                } else {
                    RenderedImage ri = (RenderedImage)refImage;
                    copySupportedImageToImageData(ri, (x + ri.getMinX()), (y + ri.getMinY()), x, y, 0, width, height, imageData);
                }
            } else {                
                // image type is unsupported, need to create a supported local copy.
                // TODO : Should look into borrow code from JAI to convert to right format.
                if (refImage instanceof BufferedImage) {
                    copyUnsupportedImageToImageData((BufferedImage)refImage, x, y, x, y, 0, width, height, imageData);
                } else if (refImage instanceof RenderedImage) {
                    RenderedImage ri = (RenderedImage)refImage;
                    copyUnsupportedImageToImageData(ri, (x + ri.getMinX()), (y + ri.getMinY()), x, y, 0, width, height, imageData);
                } else if (refImage instanceof NioImageBuffer) {
                    copyUnsupportedNioImageToImageData((NioImageBuffer)refImage, x, y, x, y, width, height, imageData);
                } else {
                    assert false;
                }
            }
        }
	geomLock.unLock();

	
	if (source.isLive()) {

            // send a SUBIMAGE_CHANGED message in order to 
	    // notify all the users of the change

	    ImageComponentUpdateInfo info;

	    info =  new ImageComponentUpdateInfo();
	    info.x = x;
	    info.y = y;
	    info.z = 0;
	    info.width = width;
	    info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
	}
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);
    }

}
