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

import java.awt.image.*;
import java.awt.color.ColorSpace;

/**
 * This class defines a 2D image component.
 * This is used for texture images, background images and raster components
 * of Shape3D nodes.
 */

class ImageComponent2DRetained extends ImageComponentRetained {
    private int rasterRefCnt = 0; 	// number of raster using this object
    private int textureRefCnt = 0;	// number of texture using this object

    private DetailTextureImage detailTexture = null; // will reference a 
					// DetailTexture object if 
					// this image is being
					// referenced as a detail image

    // use in D3D to map object to surface pointer
    int hashId;       
    native void freeD3DSurface(int hashId);

    float[] lastAlpha = new float[1];
    
    static final double EPSILON = 1.0e-6;

    // dirty mask to track if the image has been changed since the last
    // alpha update. The nth bit in the mask represents the dirty bit
    // of the image for screen nth. If nth bit is set, then the image
    // needs to be updated with the current alpha values.
    int imageChanged = 0;

    ImageComponent2DRetained() {
	hashId = hashCode();
    }

    /**
     * Copies the specified BufferedImage to this 2D image component object.
     * @param image BufferedImage object containing the image.
     * The format and size must be the same as the current format in this
     * ImageComponent2D object.
     */
    final void set(BufferedImage image) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

	if (width != this.width)
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained0"));

	if (height != this.height)
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained1"));

	int imageBytes;
	// Note, currently only EXT_ABGR and EXT_BGR are not copied, if
	// its going to be copied, do it at set time (at this time, the
	// renderer is running in parallel), if we delay it until
	// renderBin:updateObject time (when it calls evaluateExtension),
	// we are stalling the renderer
	geomLock.getLock();
	if (!byReference || (byReference && willBeCopied(image))) {
	    imageBytes = height * width * bytesPerPixelIfStored;
	    if (usedByTexture || ! usedByRaster) {
		// ==> (usedByTexture) || (! usedByTexture && ! usedByRaster)
 
		if (imageYup == null || imageYup.length < imageBytes) {
		    imageYup = new byte[imageBytes];
		    imageYupAllocated = true;  
		}
 
		// buffered image -> imageYup
		storedYupFormat = internalFormat;
		bytesPerYupPixelStored = getBytesStored(storedYupFormat);
		copyImage(image, imageYup, true, 0, storedYupFormat,
				bytesPerYupPixelStored);
		imageYupClass = BUFFERED_IMAGE;
	    }
 
	    if (usedByRaster) {
		imageYdownClass = BUFFERED_IMAGE;
		storedYdownFormat = internalFormat;
		bytesPerYdownPixelStored = getBytesStored(storedYdownFormat);

		if (imageYdown[0] == null || imageYdown[0].length < imageBytes) {
		    imageYdown[0] = new byte[imageBytes];
		    imageYdownAllocated = true;
		}
 
		if (imageYup != null){
		    //imageYup -> imageYdown
		    setImageYdown(imageYup, imageYdown[0]);
		} else {
		    // buffered image -> imageYdown
		    copyImage(image, imageYdown[0], false, 0,
				storedYdownFormat, bytesPerYdownPixelStored);
		}
	    }
	    // If its byRef case, but we copied because we know that
	    // the underlying native API cannot support this case!
	    if (byReference) {
		bImage[0] = image;
		if (usedByTexture || !usedByRaster)
		    imageYupCacheDirty = false;
		else
		    imageYupCacheDirty = true;
		    
		if (usedByRaster)
		    imageYdownCacheDirty = false;
		else
		    imageYdownCacheDirty = true;
		    
	    }
	    else {
		imageDirty[0] = true;
	    }
		
	}
	// If its by reference, then make a copy only if necessary
	else {
	    imageYupCacheDirty = true;
	    imageYdownCacheDirty = true;
	    bImage[0] = image;
	}
	imageChanged = 0xffff;
	lastAlpha[0] = 1.0f;
	geomLock.unLock();
	    
	if (source.isLive()) {
	    freeSurface();

	    // send a IMAGE_CHANGED message in order to 
	    // notify all the users of the change
            sendMessage(IMAGE_CHANGED, null);
	}
    }


    boolean willBeCopied(RenderedImage image) {
	return shouldImageBeCopied(getImageType(image),
				   (Canvas3D.EXT_ABGR|Canvas3D.EXT_BGR), image);
    }



    // NOTE, IMPORTANT: any additions to the biType tested , should be added to
    // the willBeCopied() function
    final boolean shouldImageBeCopied(int biType, int ext, RenderedImage ri) {

	if (!byReference)
	    return true;

	if ((((ext & Canvas3D.EXT_ABGR) != 0) && 
	     ((biType == BufferedImage.TYPE_4BYTE_ABGR) &&
	      (format == ImageComponent.FORMAT_RGBA8))) ||
	    (((ext & Canvas3D.EXT_BGR) != 0) &&
	     ((biType == BufferedImage.TYPE_3BYTE_BGR) &&
	      (format == ImageComponent.FORMAT_RGB))) ||	     
	    ((biType == BufferedImage.TYPE_BYTE_GRAY) &&
	     (format == ImageComponent.FORMAT_CHANNEL8)) ||
	    (is4ByteRGBAOr3ByteRGB(ri))) {
	    /* ||XXXX: Don't do short for now!
	       ((biType ==  BufferedImage.TYPE_USHORT_GRAY) &&
	       (format == ImageComponent.FORMAT_CHANNEL8)
	    */
	    
	    return false;
	}
	return true;
    }

    final int  getStoredFormat(int biType, RenderedImage ri) {
	int f = 0;
	switch(biType) {
	case BufferedImage.TYPE_4BYTE_ABGR:
	    f=  BYTE_ABGR;
	    break;
	case BufferedImage.TYPE_BYTE_GRAY:
	    f=  BYTE_GRAY;
	    break;
	case BufferedImage.TYPE_USHORT_GRAY:
	    f =  USHORT_GRAY;
	    break;
	case BufferedImage.TYPE_3BYTE_BGR:
	    f =  BYTE_BGR;
	    break;
	case BufferedImage.TYPE_CUSTOM:
	    if (is4ByteRGBAOr3ByteRGB(ri)) {
		SampleModel sm = ri.getSampleModel();
		if (sm.getNumBands() == 3) {
		    f = BYTE_RGB;
		}
		else {
		    f = BYTE_RGBA;
		}
	    }
	    break;
	default:
	    // Should never come here
	}
	return f;
    }
    
    final void set(RenderedImage image) {

	if (image instanceof BufferedImage) {
	    set(((BufferedImage)image));
	}
	else {
	    /*
	    // Create a buffered image from renderImage
	    ColorModel cm = image.getColorModel();
	    WritableRaster wRaster = image.copyData(null);
	    BufferedImage bi = new BufferedImage(cm,
						 wRaster,
						 cm.isAlphaPremultiplied()
						 ,null);
	    set(bi);
	    }
	    */
	    int width = image.getWidth();
	    int height = image.getHeight();

	    if (width != this.width)
		throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained0"));
	    
	    if (height != this.height)
		throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained1"));
	    
	    int imageBytes;
	    // Note, currently only EXT_ABGR and EXT_BGR are not copied, if
	    // its going to be copied, do it at set time (at this time, the
	    // renderer is running in parallel), if we delay it until
	    // renderBin:updateObject time (when it calls evaluateExtension),
	    // we are stalling the renderer
	    geomLock.getLock();
	    if (!byReference ||(byReference && willBeCopied(image))) {
		imageBytes = height * width * bytesPerPixelIfStored;
		if (usedByTexture || ! usedByRaster) {
		    if (imageYup == null || imageYup.length < imageBytes) {
			imageYup = new byte[imageBytes];
		        imageYupAllocated = true;  
		    }
 
		    // buffered image -> imageYup
		    storedYupFormat = internalFormat;
		    bytesPerYupPixelStored = getBytesStored(storedYupFormat);
		    copyImage(image, imageYup, true, 0, storedYupFormat,
				bytesPerYupPixelStored);
		    imageYupClass = BUFFERED_IMAGE;
		}
 
		if (usedByRaster) {

		    imageYdownClass = BUFFERED_IMAGE;
		    storedYdownFormat = internalFormat;
		    bytesPerYdownPixelStored = getBytesStored(storedYdownFormat);

		    if (imageYdown[0] == null || imageYdown[0].length < imageBytes) {
			imageYdown[0] = new byte[imageBytes];
		        imageYdownAllocated = true;
		    }
 
		    if (imageYup != null)
			//imageYup -> imageYdown
			setImageYdown(imageYup, imageYdown[0]);
		    else
			// buffered image -> imageYdown
			copyImage(image, imageYdown[0], false, 0,
				storedYdownFormat, bytesPerYdownPixelStored);
		}
		if (byReference) {
		    bImage[0] = image;
		    if (usedByTexture || !usedByRaster)
			imageYupCacheDirty = false;
		    else
			imageYupCacheDirty = true;
		    
		    if (usedByRaster)
			imageYdownCacheDirty = false;
		    else
			imageYdownCacheDirty = true;
		}
		else {
		    imageDirty[0] = true;
		}
	    }
	    // If its by reference, then make a copy only if necessary
	    else {
		imageYupCacheDirty = true;
		imageYdownCacheDirty = true;
		bImage[0] = image;
	    }

	}
	imageChanged = 0xffff;
	lastAlpha[0] = 1.0f;
	geomLock.unLock();
	if (source.isLive()) {
	    freeSurface();
            sendMessage(IMAGE_CHANGED, null);
	}
    }

    /**
     * Retrieves a copy of the image in this ImageComponent2D object.
     * @return a new BufferedImage object created from the image in this
     * ImageComponent2D object
     */
    final RenderedImage getImage() {
	if (!byReference && imageDirty[0]) {
	    imageDirty[0] = false;
	    retrieveBufferedImage(0);
	}
	return bImage[0];
    }

    // allocate storage for imageYdown
    // set imageYdown and free imageYup if necessary
    final void setRasterRef() {
	// Ref case will be handled by evaluateExtension();
	if (usedByRaster)
	    return;
	
	usedByRaster = true;

	if (format == ImageComponent.FORMAT_CHANNEL8)
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent2DRetained2"));

	if (!byReference) {
	    if (imageYdown[0] == null && imageYup != null) {
		imageYdown[0] = new byte[height * width * bytesPerYupPixelStored];
		imageYdownAllocated = true;

		// imageYup -> imageYdown
		imageYdownClass = BUFFERED_IMAGE;
		storedYdownFormat = storedYupFormat;
		bytesPerYdownPixelStored = bytesPerYupPixelStored;
		setImageYdown(imageYup, imageYdown[0]);
	    }
	    if (usedByTexture == false) {
		imageYup = null;
		imageYupAllocated = false;
	    }

	}
	else {
	    if (willBeCopied(bImage[0])) {
		geomLock.getLock();
		if (imageYdownCacheDirty) {
		    if (imageYdown[0] == null) {

			if (imageYup != null) {
			    storedYdownFormat = storedYupFormat;
			    bytesPerYdownPixelStored = bytesPerYupPixelStored;
			    imageYdown[0] =new byte[height*width *bytesPerYdownPixelStored];
			    setImageYdown(imageYup, imageYdown[0]);
			}
			else {
			    imageYdown[0] = new byte[height * width * bytesPerPixelIfStored];
            		    bytesPerYdownPixelStored = bytesPerPixelIfStored;
            		    storedYdownFormat = internalFormat;

			    if (bImage[0] instanceof BufferedImage) {
				copyImage(((BufferedImage)bImage[0]), 
					imageYdown[0], false, 0,
					storedYdownFormat,
					bytesPerYdownPixelStored);
			    }
			    else {
				copyImage(bImage[0], imageYdown[0], false, 0,
					storedYdownFormat,
					bytesPerYdownPixelStored);
			    }
			}
			imageYdownClass = BUFFERED_IMAGE;
			imageYdownAllocated = true;
		    }
		    imageYdownCacheDirty = false;
		}
		geomLock.unLock();
	    }
	    else {
		geomLock.getLock();
		imageYdownCacheDirty = true;
		geomLock.unLock();
	    }
	    /*
	    // Can't do this - since I don't know which extension
	    // will be supported, if Ydown is going away then
	    // this code will be useful
	    else if (yUp) {
		geomLock.getLock();
		if (imageYdownCacheDirty) {
		    storeRasterImageWithFlip(bImage[0]);
		    imageYdownCacheDirty = false;
		}
		geomLock.unLock();
	    }
	    */
	}
    }

    // allocate storage for imageYup
    // set imageYup and free imageYdown if necessary
    final void setTextureRef() {
	// Ref case will be handled by evaluateExtension();
	if (usedByTexture)
	    return;

	usedByTexture = true;

	if (!byReference) {

	    if (imageYup == null && imageYdown[0] != null) {
		storedYupFormat = storedYdownFormat;
		bytesPerYupPixelStored = bytesPerYdownPixelStored;
		imageYup = new byte[height * width * bytesPerYupPixelStored];
		// imageYdown -> imageYup
		setImageYup(imageYdown[0], imageYup);
		imageYupClass = BUFFERED_IMAGE;
		imageYupAllocated = true;  
	    }
	    if (usedByRaster == false) {
		imageYdown[0] = null;
		imageYdownAllocated = false;
	    }

	}
	// If the image will not be stored by reference, because
	// the format is not supported by the underlying API
	else {
	    if (willBeCopied(bImage[0])) {
		geomLock.getLock();
		if (imageYupCacheDirty) {
		    if (imageYup == null) {
			if (imageYdown[0] != null) {
			    // imageYdown -> imageYup
			    storedYupFormat = storedYdownFormat;
			    bytesPerYupPixelStored = bytesPerYdownPixelStored;
			    imageYup = new byte[height * width * bytesPerYupPixelStored];
			    setImageYup(imageYdown[0], imageYup);
			}
			else {
			    imageYup = new byte[height * width * bytesPerPixelIfStored];
            		    bytesPerYupPixelStored = bytesPerPixelIfStored;
            		    storedYupFormat = internalFormat;

			    if (bImage[0] instanceof BufferedImage) {
				copyImage(((BufferedImage)bImage[0]), imageYup,
					true, 0, storedYupFormat, 
					bytesPerYupPixelStored);
			    }
			    else {
				copyImage(bImage[0], imageYup, true, 0,
					storedYupFormat, 
					bytesPerYupPixelStored);
			    }
			}
			imageYupClass = BUFFERED_IMAGE;
			imageYupAllocated = true;  
		    }
		    imageYupCacheDirty = false;
		}
		geomLock.unLock();
	    }
	    else {
		geomLock.getLock();
		imageYupCacheDirty = true;
		geomLock.unLock();
	    }
	    /*
	    // Can't do this - since I don't know which extension
	    // will be supported, if Ydown is going away then
	    // this code will be useful

	    // If Image will not be stored by reference because
	    // of wrong orienetation
	    else if (!yUp) {
		geomLock.getLock();
		if (imageYupCacheDirty) {
		    storeTextureImageWithFlip(bImage[0]);
		    imageYupCacheDirty = false;
		}
		geomLock.unLock();
	    }
	    */
	}

    }

 
    // copy imageYup to imageYdown in reverse scanline order
    final void setImageYdown(byte[] src, byte[] dst) {
        int scanLineSize = width * bytesPerYdownPixelStored;
        int srcOffset, dstOffset, i;

        for (srcOffset = (height - 1) * scanLineSize, dstOffset = 0,
            i = 0; i < height; i++,
            srcOffset -= scanLineSize, dstOffset += scanLineSize) {
 
            System.arraycopy(src, srcOffset, dst, dstOffset,
                scanLineSize);
        }
	
    }   

    // Preserve the incoming format of thre renderImage, but maybe
    // flip the image or make a plain copy
    // Used only for raster
    final void copyRImage(RenderedImage image, byte[] dst, boolean flip,
			 int bPerPixel) {

	int numX  = image.getNumXTiles();
	int numY  = image.getNumYTiles();
	int tilew  = image.getTileWidth();
	int tileh  = image.getTileHeight();
	int i, j, h;
	java.awt.image.Raster ras;
        int tileLineBytes = tilew * bPerPixel;
	int x = image.getMinTileX();
	int y = image.getMinTileY();
	int srcOffset, dstOffset;
	int dstLineBytes = 0;
	int tileStart;
	int tileBytes = tileh * tilew * numX * bPerPixel;
	int dstStart;
	int tmph, tmpw, curw, curh;
	int rowOffset, colOffset;
	int sign;

	if (flip) {
	    dstLineBytes =width * bPerPixel;
	    tileStart = (height - 1) * dstLineBytes;
	    dstLineBytes = -(dstLineBytes);
	}
	else {
	    tileStart = 0;
	    dstLineBytes = width * bPerPixel;
	}
	// convert from Ydown to Yup for texture
	int minX = image.getMinX();
	int minY = image.getMinY();
	int xoff = image.getTileGridXOffset();
	int yoff = image.getTileGridYOffset();
	int endXTile = x * tilew + xoff+tilew;
	int endYTile = y * tileh + yoff+tileh;
	tmpw = width;
	tmph = height;
	// Check if the width is less than the tile itself ..
	curw = (endXTile - minX);
	curh = (endYTile - minY);

	if (tmpw < curw) {
	    curw = tmpw;
	}
	
	if (tmph < curh) {
	    curh = tmph;
	}
	int startw = curw ;
	
	rowOffset = (tilew - curw) * bPerPixel;
	colOffset = tilew * (tileh - curh) * bPerPixel;
	int bytesCopied = 0;
	srcOffset = rowOffset + colOffset;
	
	
	for (i = y; i < y+numY; i++) {
	    dstStart = tileStart;
	    curw = startw;
	    tmpw = width;
	    for (j = x; j < x+numX; j++) {
		ras = image.getTile(j,i);
		byte[] src = ((DataBufferByte)ras.getDataBuffer()).getData();
		dstOffset = dstStart;
		bytesCopied = curw * bPerPixel;
		for (h = 0;h < curh; h++) {
		    System.arraycopy(src, srcOffset, dst, dstOffset,
				     bytesCopied);
		    srcOffset += tileLineBytes;
		    dstOffset += dstLineBytes;
		}
		srcOffset = colOffset;
		dstStart += curw * bPerPixel;
		tmpw -= curw;
		if (tmpw < tilew) 
		    curw = tmpw;
		else
		    curw = tilew;
	    }
	    srcOffset = rowOffset;
	    colOffset = 0;
	    tileStart += curh * dstLineBytes;
	    tmph -= curh;
	    if (tmph < tileh)
		curh = tmph;
	    else
		curh = tileh;
	}
    }

    // copy imageYdown to imageYup in reverse scanline order
    final void setImageYup(byte[] src, byte[] dst) {
        int scanLineSize = width * bytesPerYupPixelStored;
        int srcOffset, dstOffset, i;
 
        for (srcOffset = 0, dstOffset = (height - 1) * scanLineSize,
            i = 0; i < height; i++,
            srcOffset += scanLineSize, dstOffset -= scanLineSize) {
 
            System.arraycopy(src, srcOffset, dst, dstOffset,
                scanLineSize);
        }
    }

    // Lock out user thread from modifying usedByRaster and
    // usedByTexture variables by using synchronized routines
    final void evaluateExtensions(int ext) {
	int i;
	int imageBytes;
	RenderedImage image = bImage[0];

	//	System.out.println("!!!!!!!!!!!!!imageYupCacheDirty = "+imageYupCacheDirty);
	//	System.out.println("!!!!!!!!!!!!!imageYdownCacheDirty = "+imageYdownCacheDirty);
	//	System.out.println("!!!!!!!!!!!!!usedByTexture = "+usedByTexture);
	//	System.out.println("!!!!!!!!!!!!!usedByRaster = "+usedByRaster);


	if (!imageYupCacheDirty && !imageYdownCacheDirty) {
	    return;
	}
	
	int riType = getImageType(image);

	//	Thread.dumpStack();
	if (usedByTexture == true || ! usedByRaster) {
	    // If the image is already allocated, then return
	    // nothing to do!
	    // Since this is a new image, the app may have changed the image
	    // when it was not live, so re-compute, until the image is allocated
	    // for this pass!
	    //	    System.out.println("!!!!!!!!!!!!!imageYupCacheDirty = "+imageYupCacheDirty);
	    if (!imageYupCacheDirty) {
		evaluateRaster(riType, ext);
		return;
	    }
	    if (shouldImageBeCopied(riType, ext, image)) {
		
		imageBytes = height * width * bytesPerPixelIfStored;
		if (imageYup == null || !imageYupAllocated) {
		    imageYup = new byte[imageBytes];
		    imageYupAllocated = true;
		}		
		// buffered image -> imageYup
                bytesPerYupPixelStored = bytesPerPixelIfStored;
                storedYupFormat = internalFormat;
		copyImage(image, imageYup, true, 0, 
				storedYupFormat, bytesPerYupPixelStored);
		imageYupClass = BUFFERED_IMAGE;
		imageYupCacheDirty = false;
	    }
	    else {
		// This image is already valid ..
		if (!imageYupCacheDirty) {
		    evaluateRaster(riType, ext);
		    return;
		}
		storedYupFormat = getStoredFormat(riType, image);
		bytesPerYupPixelStored = getBytesStored(storedYupFormat);
		
		// It does not have to be copied, but we
		// have to copy because the incoming image is
		// ydown
		if (!yUp) {
		    storeTextureImageWithFlip(image);
		}
		else {
		    if (image instanceof BufferedImage) {
			byte[] tmpImage =  ((DataBufferByte)((BufferedImage)image).getRaster().getDataBuffer()).getData();
			imageYup = tmpImage;
			imageYupAllocated = false;
			imageYupClass = BUFFERED_IMAGE;
		    }
		    else {
			numXTiles = image.getNumXTiles();
			numYTiles = image.getNumYTiles();
			tilew = image.getTileWidth();
			tileh = image.getTileHeight();
			minTileX = image.getMinTileX();
			minTileY = image.getMinTileY();
			minX = image.getMinX();
			minY = image.getMinY();
			tileGridXOffset =image.getTileGridXOffset();
			tileGridYOffset = image.getTileGridYOffset();
			imageYupAllocated = false;
			imageYupClass = RENDERED_IMAGE;
			imageYup = null;
		    }
		}

	    }
	    if (usedByRaster == false) {
		imageYdown[0] = null;
		imageYdownAllocated = false;
	    }
	}
	evaluateRaster(riType, ext);
    }

    void evaluateRaster(int riType, int ext) {
	int i;
	int imageBytes;
	RenderedImage image = bImage[0];

	if (usedByRaster) {
	    // If the image is already allocated, then return
	    // nothing to do!
	    if (!imageYdownCacheDirty) {
		return;
	    }
	    if (shouldImageBeCopied(riType, ext, image)) {
		//		System.out.println("Raster Image is copied");
		imageBytes = height * width * bytesPerPixelIfStored;
		if (imageYdown[0] == null || !imageYdownAllocated || imageYdown[0].length < imageBytes){
		    imageYdown[0] = new byte[imageBytes];
		    imageYdownAllocated = true;
		}
		if (imageYup != null) {
		    storedYdownFormat = storedYupFormat;
		    bytesPerYdownPixelStored = bytesPerYupPixelStored;
		    setImageYdown(imageYup, imageYdown[0]);
		}
		else {
		    // buffered image -> imageYup
                    storedYdownFormat = internalFormat;
                    bytesPerYdownPixelStored = bytesPerPixelIfStored;
		    copyImage(image, imageYdown[0], false, 0,
				storedYdownFormat, bytesPerYdownPixelStored);
		}
		imageYdownCacheDirty = false;
		imageYdownClass = BUFFERED_IMAGE;
	    }
	    else {
		// This image is already valid ..
		if (!imageYdownCacheDirty) {
		    return;
		}
		storedYdownFormat = getStoredFormat(riType, image);
		bytesPerYdownPixelStored = getBytesStored(storedYdownFormat);
		if (yUp) {
		    storeRasterImageWithFlip(image);
		}
		else {
		    if (image instanceof BufferedImage) {
			byte[] tmpImage =  ((DataBufferByte)((BufferedImage)image).getRaster().getDataBuffer()).getData();
			imageYdown[0] = tmpImage;
			imageYdownAllocated = false;
			imageYdownClass = BUFFERED_IMAGE;
			//			System.out.println("Raster Image is stored by ref");
		    }
		    else {
			// Right now, always copy since opengl rasterpos is
			// too restrictive
			imageBytes = width*height*bytesPerYdownPixelStored;
			if (imageYdown[0] == null || !imageYdownAllocated ||imageYdown[0].length < imageBytes){
			    imageYdown[0] = new byte[imageBytes];
			    imageYdownAllocated = true;
			}
			imageYdownClass = BUFFERED_IMAGE;
			copyRImage(image,imageYdown[0], false, bytesPerYdownPixelStored);
			//			System.out.println("Copying by ref RImage");
			
			/*
			numXTiles = image.getNumXTiles();
			numYTiles = image.getNumYTiles();
			tilew = image.getTileWidth();
			tileh = image.getTileHeight();
			minTileX = image.getMinTileX();
			minTileY = image.getMinTileY();
			imageYdownAllocated = false;
			imageYdownClass = RENDERED_IMAGE;
			imageYdown = null;
			*/
		    }
		    imageYdownCacheDirty = false;
		}

		
	    }
	    if (usedByTexture == false) {
		imageYup = null;
		imageYupAllocated = false;
	    }
	}
    }

    void storeRasterImageWithFlip(RenderedImage image) {
	int imageBytes;
	
	if (image instanceof BufferedImage) {
	    imageBytes = width*height*bytesPerYdownPixelStored;
	    if (imageYdown[0] == null || !imageYdownAllocated ||imageYdown[0].length < imageBytes){
		imageYdown[0] = new byte[imageBytes];
		imageYdownAllocated = true;
	    }
	    imageYdownClass = BUFFERED_IMAGE;
	    imageYdownCacheDirty = false;
	    byte[] tmpImage =  ((DataBufferByte)((BufferedImage)image).getRaster().getDataBuffer()).getData();
	    setImageYdown(tmpImage, imageYdown[0]);
	}
	else {
	    // Right now, always copy since opengl rasterpos is
	    // too restrictive
	    
	    imageBytes = width*height*bytesPerYdownPixelStored;
	    if (imageYdown[0] == null || !imageYdownAllocated ||imageYdown[0].length < imageBytes){
		imageYdown[0] = new byte[imageBytes];
		imageYdownAllocated = true;
	    }
	    imageYdownClass = BUFFERED_IMAGE;
	    imageYdownCacheDirty = false;
	    copyRImage(image, imageYdown[0], true, bytesPerYdownPixelStored);
	}
    }

    void storeTextureImageWithFlip(RenderedImage image) {
	int imageBytes;

	if (image instanceof BufferedImage) {
	    byte[] tmpImage =  ((DataBufferByte)((BufferedImage)image).getRaster().getDataBuffer()).getData();

	    if (imageYup == null || !imageYupAllocated) {
		imageBytes = width*height*bytesPerYupPixelStored;
		imageYup = new byte[imageBytes];
		imageYupAllocated = true;
	    }
	    imageYupClass = BUFFERED_IMAGE;
	    setImageYup(tmpImage, imageYup);
	    imageYupCacheDirty = false;

	}
	else {
	    if (imageYup == null || !imageYupAllocated) {
		imageBytes = width*height*bytesPerYupPixelStored;
		imageYup = new byte[imageBytes];
		imageYupAllocated = true;
	    }
	    imageYupClass = BUFFERED_IMAGE;
	    copyRImage(image, imageYup, true, bytesPerYupPixelStored);
	    imageYupCacheDirty = false;
	}
    }

    void setLive(boolean inBackgroundGroup, int refCount) {
	super.setLive(inBackgroundGroup, refCount);
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);
	if (this.refCount <= 0) {
	    freeSurface();
	}
    }

    void freeSurface() {
	if (VirtualUniverse.mc.isD3D()) {
	    freeD3DSurface(hashId);
	}
    }

    protected void finalize() {
	// For Pure immediate mode, there is no clearLive so
	// surface will free when JVM do GC 
	freeSurface();
    }
    void updateAlpha(Canvas3D cv, int screen, float alpha) {
	// if alpha is smaller than EPSILON, set it to EPSILON, so that
	// even if alpha is equal to 0, we will not completely lose
	int i, j;
	byte byteAlpha;
	float rndoff = 0.0f;
	
	// the original alpha value
	if (alpha <= EPSILON) {
	    alpha = (float)EPSILON;
	}
	//	System.out.println("========> updateAlpha, this = "+this);
	// Lock out the other renderers ..
	synchronized (this) {
	    // If by reference, the image has been copied, but aset has occured
	    // or if the format is not RGBA, then copy
	    //	    Thread.dumpStack();
	    if (isByReference() && ((storedYdownFormat != internalFormat) || ((imageChanged & 1) != 0))) {
		int imageBytes = height * width * bytesPerPixelIfStored;
		if (imageYdown[0] == null  || !imageYdownAllocated|| imageYdown[0].length < imageBytes)
		    imageYdown[0] = new byte[imageBytes];
                bytesPerYdownPixelStored = bytesPerPixelIfStored;
                storedYdownFormat = internalFormat;
		copyImage(bImage[0],imageYdown[0], false, 0,
				storedYdownFormat, bytesPerYdownPixelStored);
		imageYdownCacheDirty = false;
		imageYdownClass = BUFFERED_IMAGE;
		imageYdownAllocated = true;
		imageChanged &= ~1;
		freeSurface();
	    }

	    // allocate an entry for the last alpha of the screen if needed
	    if (lastAlpha == null) {
		lastAlpha = new float[screen+1];
		lastAlpha[screen] = 1.0f;
	    }
	    else  if (lastAlpha.length <= screen) {
		float[] la = new float[screen+1];
		for (i = 0; i < lastAlpha.length; i++) {
		    la[i] = lastAlpha[i];
		}
		lastAlpha = la;
		lastAlpha[screen] = 1.0f;
	    }
	    // allocate a copy of the color data for the screen if needed.
	    // this piece of code is mainly for multi-screens case
	    if (imageYdown.length <= screen) {
		byte[][] bdata = new byte[screen+1][];
		byte[] idata;
		int refScreen = -1;

		int imageBytes = height * width * bytesPerYdownPixelStored;
		idata = bdata[screen] = new byte[imageBytes];
		for (i = 0; i < imageYdown.length; i++) {
		    bdata[i] = imageYdown[i];
		    if (Math.abs(lastAlpha[i] - alpha) < EPSILON) {
			refScreen = i;
		    }
		}

		if (noAlpha) {
		    byteAlpha = (byte) (alpha * 255.0f + 0.5);
		    for (j=3,i=0; i< width * height; i++,j+=4) {
			idata[j] = byteAlpha;
		    }
		}
		else {

		    // copy the data from a reference screen which has the closest
		    // alpha values
		    if (refScreen >= 0) {
			System.arraycopy(imageYdown[refScreen], 0, idata, 0, imageBytes);
			lastAlpha[screen] = lastAlpha[refScreen];
		    }
		    else {
			float m = alpha/lastAlpha[0];
			if (m < 1.0f)
			    rndoff = 0.5f;
			else
			    rndoff = -0.5f;

			byte[] srcData = imageYdown[0];
			for (i = 0, j = 0; i <  width * height; i++, j+= 4) {
			    System.arraycopy(srcData, j, idata, j, 3);
			    idata[j+3] =(byte)( ((int)srcData[j+3] & 0xff) * m + rndoff);
			}
			lastAlpha[screen] = alpha;
		    }
		}
		imageYdown = bdata;

		imageChanged &= ~(1 << screen);
		freeSurface();
		return;
	    }

	    if ((imageChanged & (1<< screen)) == 0) {
		// color data is not modified
		// if alpha is different, update the alpha values
		int val = -1;
		if (Math.abs(lastAlpha[screen] - alpha) > EPSILON) {
		    byte[] idata = imageYdown[screen];
		    if (noAlpha) {
			byteAlpha = (byte) (alpha * 255.0f + 0.5);
			for (j=3,i=0; i< width * height; i++,j+=4) {
			    idata[j] = byteAlpha;
			}
		    }
		    else {
			float m = alpha/lastAlpha[screen];
			if (m < 1.0f)
			    rndoff = 0.5f;
			else
			    rndoff = -0.5f;
			
			for (i = 0, j = 3; i <  width * height; i++, j+= 4) {
			    idata[j] =(byte)( ((int)idata[j] & 0xff) * m + rndoff);
			}
		    }
		    freeSurface();
		}
	    }
	    else {
		// color data is modified
		if (screen == 0) {
		// just update alpha values since screen 0 data is
		// already updated in copyImage()
		    byte[] idata = imageYdown[0];
		    if (noAlpha) {
			byteAlpha = (byte) (alpha * 255.0f + 0.5);
			for (j=3,i=0; i< width * height; i++,j+=4) {
			    idata[j] = byteAlpha;
			}
		    }
		    else {
			for (i = 0, j = 3; i <  width * height; i++, j+= 4) {
			    idata[j] =(byte)( ((int)idata[j] & 0xff) * alpha + 0.5);
			}
		    }
		    
		}
		else {
		    // update color values from screen 0 data
		    float m;
		    byte[] ddata = imageYdown[screen];
		    if (noAlpha) {
			byteAlpha = (byte) (alpha * 255.0f + 0.5);
			for (j=3,i=0; i< width * height; i++,j+=4) {
			    ddata[j] = byteAlpha;
			}
		    }
		    else {
			if ((imageChanged & 1) == 0) {
			    // alpha is up to date in screen 0
			    m = alpha / lastAlpha[0];
			}
			else {
			    m = alpha;
			}

			if (m < 1.0f)
			    rndoff = 0.5f;
			else
			    rndoff = -0.5f;

			byte[] sdata = imageYdown[0];

			for (i = 0, j = 0; i < width * height; i++, j+= 4) {
			    System.arraycopy(sdata, j, ddata, j, 3);
			    ddata[j+3] =(byte)( ((int)sdata[j+3] & 0xff) * m + rndoff);
			}
		    }
		}
		freeSurface();
	    }
	    lastAlpha[screen] = alpha;
	    imageChanged &= ~(1 << screen);
	}
    }


    int getEffectiveBytesPerPixel() {
	if (byReference) {
	    if (usedByTexture || !usedByRaster)
		return bytesPerYupPixelStored;
	    else
		return bytesPerYdownPixelStored;
	}
	else {
	    return bytesPerPixelIfStored;
	}
    }


    int getEffectiveFormat() {
	if (byReference) {
	    if (usedByTexture || !usedByRaster)
		return storedYupFormat;
	    else
		return storedYdownFormat;
	}
	else {
	    return internalFormat;
	}
    }
    
    /**
     * retrieve image data from read buffer to ImageComponent's 
     * internal representation
     */  
    final void retrieveImage(byte[] buf, int wRead, int hRead) {
	retrieveImage(buf, 0, 0, wRead, hRead);
    }


    /**
     * retrieve a subimage data from read buffer to ImageComponent's 
     * internal representation
     */  
    final void retrieveImage(byte[] buf, int xRead, int yRead, int
			     wRead, int hRead) {

        int srcOffset, dstOffset, h,w,i,j;  
	int dstWidth;
	
	byte[] bdata;


	// If byReference, then copy to the reference image immediately after
	// readRaster or offscreenbuffer

	// In the by reference case, they should have set and image, before
	// calling readRaster, so there should be a valid imageYup or imageYdown
	// as used by texture or raster

	// Note the format of the glReadPixels is based on storedFormat
	// so, we can do a direct copy

	int bpp = getEffectiveBytesPerPixel();
	int format = getEffectiveFormat();

	if (!byReference) {

	    dstWidth = width * bytesPerPixelIfStored;
	    if ((usedByTexture || !usedByRaster)&& (imageYup == null)) {
		imageYup = new byte[height * dstWidth];
		bytesPerYupPixelStored = bytesPerPixelIfStored;
		storedYupFormat = internalFormat;
		imageYupAllocated = true;  
	    }
	    if (usedByRaster && imageYdown[0] == null) {
		imageYdown[0] = new byte[height * dstWidth];
		bytesPerYdownPixelStored = bytesPerPixelIfStored;
		storedYdownFormat = internalFormat;
		imageYdownAllocated = true;  
	    }
	}


	int srcWidth = wRead * bpp;
	int srcLineBytes = width * bpp;
	
	/*
	System.out.println("bytesPerYdownPixelStored = "+bytesPerYdownPixelStored+" bpp = "+bpp);
	System.out.println("storedYdownFormat = "+storedYdownFormat+" format = "+format);
	System.out.println("bytesPerPixelIfStored = "+bytesPerPixelIfStored+" internalformat = "+internalFormat);
	System.out.println("imageYup = "+imageYup+" imageYdown = "+imageYdown[0]);
	System.out.println("===> usedByRaster = "+usedByRaster);
	*/

	// array copy by scanline

	//position of a raster specifies the upper-left corner
	// copy yUp -> yDown
	imageDirty [0] = true;

	if (usedByRaster) {
	    dstWidth = width * bytesPerYdownPixelStored;
	    srcOffset = (yRead * srcLineBytes) + (xRead * bpp);
	    
	    dstOffset = ((height - yRead - 1)) * dstWidth +
		(xRead * bytesPerYdownPixelStored);
	    // If by Reference and a copy has not been made ...
	    if (byReference && storedYdownFormat != internalFormat) {
		bdata =((DataBufferByte)  ((BufferedImage)bImage[0]).getRaster().getDataBuffer()).getData();
		imageDirty [0] = false;
	    }
	    else {
		bdata = imageYdown[0];
	    }
	    if (storedYdownFormat == format) {
		for (h = 0; h < hRead; h++,
			 srcOffset += srcLineBytes, dstOffset -= dstWidth) {
		    System.arraycopy(buf, srcOffset, bdata, dstOffset, srcWidth);
		}
	    }
	    else { // Would be one of the stored formats to RGBA
		// Convert from one of the byRef formats to RGBA
		switch(format) {
		case BYTE_ABGR:
		    for (h = 0; h < hRead; h++,
			     srcOffset += srcLineBytes, dstOffset -= dstWidth) {
			int offset = dstOffset;
			for (w = 0; w < srcWidth; w +=bpp) {
			    bdata[offset++] = buf[srcOffset+w+3];
			    bdata[offset++] = buf[srcOffset+w+2];
			    bdata[offset++] = buf[srcOffset+w+1];
			    bdata[offset++] = buf[srcOffset+w];
			    
			}
		    }
		    break;
		case BYTE_BGR:

		    for (h = 0; h < hRead; h++,
			     srcOffset += srcLineBytes, dstOffset -= dstWidth) {
			int offset = dstOffset;
			for (w = 0; w < srcWidth; w +=bpp) {
			    bdata[offset++] = buf[srcOffset+w+2];
			    bdata[offset++] = buf[srcOffset+w+1];
			    bdata[offset++] = buf[srcOffset+w];
			    bdata[offset++] =  (byte)0xff;
			    
			}
		    }
		    break;
	    
		}		
	    }
	}

	if (usedByTexture || !usedByRaster) {
	    imageYupCacheDirty = true;
	    dstWidth = width * bytesPerYupPixelStored;
	    srcOffset = (yRead * srcLineBytes) + (xRead * bpp);

	    // If by Reference and a copy has not been made ...
	    if (byReference && storedYupFormat != internalFormat) {
		bdata =((DataBufferByte)  ((BufferedImage)bImage[0]).getRaster().getDataBuffer()).getData();
		imageDirty [0] = false;
	    }
	    else {
		bdata = imageYup;
	    }
	    // If used by texture, then storedYupFormat is always equal to format
	    if (storedYupFormat == format) {
		for (dstOffset = srcOffset, 
			 h = 0; h < hRead; h++,
			 srcOffset += srcLineBytes, dstOffset += dstWidth) {
		    System.arraycopy(buf, srcOffset, bdata, dstOffset, srcWidth);
		}
	    }
	}
	// If its by reference and a copy has been made, make the user's copy
	// up-to-date
	
	if (byReference && imageDirty[0]) {
	    imageDirty [0] = false;
	    if (usedByTexture || !usedByRaster) {
		copyBufferedImageWithFormatConversion(true, 0);
	    }
	    else {
		copyBufferedImageWithFormatConversion(false, 0);
	    }
	}
	imageChanged = 0xffff;
	lastAlpha[0] = 1.0f;
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

	// update the internal copy of the image data if a copy has been
        // made
	if (imageYupAllocated) {
	    copyImage(bImage[0], (x + bImage[0].getMinX()), 
			(y + bImage[0].getMinY()), imageYup, x, y,
			true, 0, width, height, storedYupFormat,
				bytesPerYupPixelStored);
	}


	if (imageYdownAllocated) {
	    copyImage(bImage[0], (x + bImage[0].getMinX()), 
			(y + bImage[0].getMinY()), imageYdown[0], x, y,
			false, 0, width, height, storedYdownFormat,
				bytesPerYdownPixelStored);
	}

	imageDirty[0] = true;

	geomLock.unLock();

	
	if (source.isLive()) {

	    //XXXX: check whether this is needed
	    freeSurface();

	    // send a SUBIMAGE_CHANGED message in order to 
	    // notify all the users of the change

	    ImageComponentUpdateInfo info;

	    info = VirtualUniverse.mc.getFreeImageUpdateInfo();
	    info.x = x;
	    info.y = y;
	    info.z = 0;
	    info.width = width;
	    info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
	}
    }

    void setSubImage(RenderedImage image, int width, int height, 
			int srcX, int srcY, int dstX, int dstY) {

	geomLock.getLock();

	if (imageYupAllocated) {
	    copyImage(image, srcX, srcY, imageYup, dstX, dstY, 
			true, 0, width, height, storedYupFormat,
				bytesPerYupPixelStored);
	}

	if (imageYdownAllocated) {
	    copyImage(image, srcX, srcY, imageYdown[0], 
			dstX, dstY, false, 0, width, height, 
			storedYdownFormat, bytesPerYdownPixelStored);
	}

	imageDirty[0] = true;

        geomLock.unLock();


        if (source.isLive()) {

            // XXXX: check whether this is needed
            freeSurface();

            // send a SUBIMAGE_CHANGED message in order to
            // notify all the users of the change

            ImageComponentUpdateInfo info;

            info = VirtualUniverse.mc.getFreeImageUpdateInfo();
            info.x = dstX;
            info.y = dstY;
	    info.z = 0;
            info.width = width;
            info.height = height;

            sendMessage(SUBIMAGE_CHANGED, info);
        }
    }

    synchronized void updateMirrorObject(int component, Object value) {

	super.updateMirrorObject(component, value);

	if (detailTexture != null) {
            if (((component & IMAGE_CHANGED) != 0) ||
                ((component & SUBIMAGE_CHANGED) != 0)) {
		
		// notify detailTexture of image change

		detailTexture.notifyImageComponentImageChanged(this, value);
	    }
	}
    }


    synchronized void setDetailTexture(DetailTextureImage tex) {
	detailTexture = tex;
    }

    synchronized DetailTextureImage getDetailTexture() {
	if (detailTexture == null) {
	    detailTexture = new DetailTextureImage(this);
	}
	return detailTexture;
    }
}
