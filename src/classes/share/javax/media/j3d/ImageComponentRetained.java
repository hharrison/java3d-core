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

import java.util.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.Transparency;

/**
 * Abstract class that is used to define 2D or 3D ImageComponent classes
 * used in a Java 3D scene graph.
 * This is used for texture images, background images and raster components
 * of Shape3D nodes.
 */

abstract class ImageComponentRetained extends NodeComponentRetained {
    int		format;		// PixelArray format (RGB, RGBA, ALPHA, etc.)
    int		width;		// Width of PixelArray
    int		height;		// Height of PixelArray
    byte[][]	imageYdown = new byte[1][];	// 2D array of pixel values in
				// one of various formats - Y downwards
    byte[]	imageYup;	// 2D or 3D array of pixel values in
				// one of various formats - Y upwards

    int		bytesPerPixel;  // computed from input format
    boolean 	usedByRaster = false;   // used by a raster object?
    boolean 	usedByTexture = false;  // used by a texture object?

     boolean     byReference = false;   // Is the imageComponent by reference
     boolean     yUp = false;
    
    // array of Buffered image
    // This will store the refImage array, if the imagecomponent
    // is by reference
    RenderedImage[] bImage;     
    boolean[] imageDirty;       // array of image dirty flag

    boolean noAlpha = false;

    // Format of the Yup and Ydown image
    // in the case of "by Copy" it is RGBA
    // In the case of "by ref" it may be one of the original
    // formats supported by OpenGL
    /*
    int storedFormat;
    */
    int bytesPerPixelIfStored; // Number of bytes if a copy is made
    int storedYupFormat;
    int storedYdownFormat;

    int bytesPerYupPixelStored;
    int bytesPerYdownPixelStored;
    
    int internalFormat; // Used when a copy is made, RGBA, LA, L
    boolean imageYupAllocated = false;
    boolean imageYdownAllocated = false;

    // If cache is dirty (clearLive, setLive has occureed), then
    // extension based cache needs to be re-evaluated
    boolean imageYupCacheDirty = false;
    boolean imageYdownCacheDirty = false;
    

    static final int BYTE_RGBA    =  0x1;
    static final int BYTE_ABGR    =  0x2;
    static final int BYTE_GRAY    =  0x4;
    static final int USHORT_GRAY  =  0x8;
    static final int BYTE_LA      =  0x10;
    static final int BYTE_BGR     =  0x20;
    static final int BYTE_RGB     =  0x40;

    int imageYupClass = 0;
    int imageYdownClass = 0;
    static final int BUFFERED_IMAGE   = 0x1;
    static final int RENDERED_IMAGE   = 0x2;
    
    // change flag
    static final int IMAGE_CHANGED       = 0x01;
    static final int SUBIMAGE_CHANGED    = 0x02;
    
    // Lock used in the "by ref case"
    GeometryLock geomLock = new GeometryLock();

    int minTileX = 0;
    int minTileY = 0;
    int minTileZ = 0;    

    int tilew = 0;
    int tileh = 0;
    int tiled = 0;

    int numXTiles = 0;
    int numYTiles = 0;
    int numZTiles = 0;

    int tileGridXOffset = 0;
    int tileGridYOffset = 0;


    int minX = 0;
    int minY = 0;
    
    // lists of Node Components that are referencing this ImageComponent
    // object. This list is used to notify the referencing node components
    // of any changes of this ImageComponent.

    ArrayList userList = new ArrayList();

    /**
     * Retrieves the width of this image component object.
     * @return the width of this image component object
     */  
    final int getWidth() {
        return width;
    }

    /**
     * Retrieves the height of this image component object.
     * @return the height of this image component object
     */  
    final int getHeight() {
        return height;
    }

    /**
     * Retrieves the format of this image component object.
     * @return the format of this image component object
     */  
    final int getFormat() {
        return format;
    }

    /**
     * Check if ImageComponent parameters have valid values..
     */
    void processParams(int format, int width, int height, int depth) {        
	if (width < 1)
          throw new IllegalArgumentException(J3dI18N.getString("ImageComponentRetained0"));

        if (height < 1)
          throw new IllegalArgumentException(J3dI18N.getString("ImageComponentRetained1"));

        if (depth < 1)
          throw new IllegalArgumentException(J3dI18N.getString("ImageComponentRetained2"));

        if (format < 1 || format > ImageComponent.FORMAT_TOTAL)
          throw new IllegalArgumentException(J3dI18N.getString("ImageComponentRetained3"));
        this.format = format;
        this.width = width;
        this.height = height;
	imageDirty = new boolean[depth];
	for (int i=0; i< depth; i++)
	    imageDirty[i] = false;
	bImage = new RenderedImage[depth];

	noAlpha = (format == ImageComponent.FORMAT_RGB ||
		   format == ImageComponent.FORMAT_RGB4 ||
		   format == ImageComponent.FORMAT_R3_G3_B2 ||
		   format == ImageComponent.FORMAT_RGB5);

	// If the format is 8bit per component, we may send it down
	// to OpenGL directly if its by ref case
        switch (format) {
        case ImageComponent.FORMAT_RGB:// same as ImageComponent.FORMAT_RGB8
            bytesPerPixel = 3;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_RGBA:// same as ImageComponent.FORMAT_RGBA8
            bytesPerPixel = 4;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_RGB5:
            bytesPerPixel = 2;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_RGB5_A1:
            bytesPerPixel = 2;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_RGB4:
            bytesPerPixel = 2;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_RGBA4:
            bytesPerPixel = 2;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_R3_G3_B2:
            bytesPerPixel = 1;
            bytesPerPixelIfStored = 4;
	    internalFormat = BYTE_RGBA;
            break;
        case ImageComponent.FORMAT_LUM4_ALPHA4:
            bytesPerPixel = 1;
            bytesPerPixelIfStored = 2;
	    internalFormat = BYTE_LA;
            break;
        case ImageComponent.FORMAT_LUM8_ALPHA8:
            bytesPerPixel = 2;
            bytesPerPixelIfStored = 2;
	    internalFormat = BYTE_LA;
            break;
        case ImageComponent.FORMAT_CHANNEL8:
            bytesPerPixel = 1;
            bytesPerPixelIfStored = 1;
	    internalFormat = BYTE_GRAY;
            break;
        default:
            // ERROR
        }
    }

    void setTextureRef() {
	usedByTexture = true;
    }

    void setRasterRef() {
	usedByRaster = true;
    }


    boolean formatMatches(int format, RenderedImage ri) {

	// there is no RenderedImage format that matches BYTE_LA
	if (format == BYTE_LA) {
	    return false;
	}

        int riFormat = getImageType(ri);

	if ((format == BYTE_ABGR && riFormat == BufferedImage.TYPE_4BYTE_ABGR)
	    || (format == BYTE_BGR && riFormat == BufferedImage.TYPE_3BYTE_BGR)
	    || (format == BYTE_GRAY && riFormat == BufferedImage.TYPE_BYTE_GRAY)
	    || (format == USHORT_GRAY && riFormat == 
			BufferedImage.TYPE_USHORT_GRAY)) {
	    return true;
	}

	if (riFormat == BufferedImage.TYPE_CUSTOM) {
	    if (is4ByteRGBAOr3ByteRGB(ri)) {
	        int numBands = ri.getSampleModel().getNumBands();
		if (numBands == 3 && format == BYTE_RGB) {
		    return true;
		} else if (numBands == 4 && format == BYTE_RGBA) {
		    return true;
		}
	    }
	}

	return false;
    }

    /**
     * copy complete region of a RenderedImage
     */
    final void copyImage(RenderedImage ri, byte[] image,
			 boolean usedByTexture, int depth, 
			 int imageFormat, int imageBytesPerPixel) {

	if (ri instanceof BufferedImage) {
	    copyImage((BufferedImage)ri, 0, 0, image, 0, 0, 
			usedByTexture, depth, width, height, 
			imageFormat, imageBytesPerPixel);
	} else { 
	    copyImage(ri, ri.getMinX(), ri.getMinY(), 
			image, 0, 0, usedByTexture, depth, width, height,
			imageFormat, imageBytesPerPixel);
	}
    }


    /**
     * copy subregion of a RenderedImage
     */
    final void copyImage(RenderedImage ri, int srcX, int srcY, 
			byte[] image, int dstX, int dstY,
			boolean usedByTexture, int depth,
			int copywidth, int copyheight, 
			int imageFormat, int imageBytesPerPixel) {

	if (ri instanceof BufferedImage) {
	    copyImage((BufferedImage)ri, srcX, srcY, image, dstX, dstY,
			usedByTexture, depth, copywidth, copyheight, 
			imageFormat, imageBytesPerPixel);
	    return;
  	}


	int w, h, i, j, m, n;
        int dstBegin;
        Object pixel = null;
	java.awt.image.Raster ras;
	int lineBytes = width * imageBytesPerPixel; // nbytes per line in
						    // dst image buffer
	int sign;				// -1 for going down
	int dstLineBytes;			// sign * lineBytes
	int tileStart;				// destination buffer offset
						// at the next left most tile
						 
	int offset;

	ColorModel cm = ri.getColorModel();

	int xoff = ri.getTileGridXOffset();	// tile origin x offset
	int yoff = ri.getTileGridYOffset();	// tile origin y offset
	int minTileX = ri.getMinTileX();	// min tile x index 
	int minTileY = ri.getMinTileY();	// min tile y index
	tilew = ri.getTileWidth();		// tile width in pixels
	tileh = ri.getTileHeight();		// tile height in pixels


	// determine the first tile of the image

	float mt;

	mt = (float)(srcX - xoff) / (float)tilew;
	if (mt < 0) {
	    minTileX = (int)(mt - 1);
	} else {
	    minTileX = (int)mt;
	}

	mt = (float)(srcY - yoff) / (float)tileh;
	if (mt < 0) {
	    minTileY = (int)(mt - 1);
	} else {
	    minTileY = (int)mt;
	}
	

	// determine the pixel offset of the upper-left corner of the
	// first tile

	int startXTile = minTileX * tilew + xoff;
	int startYTile = minTileY * tileh + yoff;


	// image dimension in the first tile

	int curw = (startXTile + tilew - srcX);
	int curh = (startYTile + tileh - srcY);


	// check if the to-be-copied region is less than the tile image
	// if so, update the to-be-copied dimension of this tile

	if (curw > copywidth) {
	    curw = copywidth;
	}
	
	if (curh > copyheight) {
	    curh = copyheight;
	}


	// save the to-be-copied width of the left most tile

	int startw = curw;
	

	// temporary variable for dimension of the to-be-copied region

	int tmpw = copywidth;
	int tmph = copyheight;

	
	// offset of the first pixel of the tile to be copied; offset is
        // relative to the upper left corner of the title

	int x = srcX - startXTile;
	int y = srcY - startYTile;


	// determine the number of tiles in each direction that the
 	// image spans

	numXTiles = (copywidth + x) / tilew;
	numYTiles = (copyheight + y) / tileh;

	if (((float)(copywidth + x ) % (float)tilew) > 0) {
	    numXTiles += 1;
	}
	
	if (((float)(copyheight + y ) % (float)tileh) > 0) {
	    numYTiles += 1;
	}

/*
	System.out.println("-----------------------------------------------");
	System.out.println("minTileX= " + minTileX + " minTileY= " + minTileY);
	System.out.println("numXTiles= " + numXTiles + " numYTiles= " + numYTiles);
	System.out.println("tilew= " + tilew + " tileh= " + tileh);
	System.out.println("xoff= " + xoff + " yoff= " + yoff);
	System.out.println("startXTile= " + startXTile + " startYTile= " + startYTile);
	System.out.println("srcX= " + srcX + " srcY= " + srcY);
	System.out.println("copywidth= " + copywidth + " copyheight= " + copyheight);

	System.out.println("rminTileX= " + ri.getMinTileX() + " rminTileY= " + ri.getMinTileY());
	System.out.println("rnumXTiles= " + ri.getNumXTiles() + " rnumYTiles= " + ri.getNumYTiles());
*/

	if ((!yUp && usedByTexture) ||
	    (yUp && !usedByTexture)) {

	    // destination buffer offset

	    tileStart = ((height - dstY - 1) * width + dstX) 
				* imageBytesPerPixel;

	    sign = -1;
	    dstLineBytes = -lineBytes;
	} else {

	    // destination buffer offset

	    tileStart = (dstY * width + dstX) * imageBytesPerPixel;
	    sign = 1;
	    dstLineBytes = lineBytes;
	}

/*
	System.out.println("tileStart= " + tileStart + " dstLineBytes= " + dstLineBytes);
	System.out.println("startw= " + startw);
*/

	// allocate memory for a pixel 

	ras = ri.getTile(minTileX,minTileY);
	pixel = getDataElementBuffer(ras);

	if (formatMatches(imageFormat, ri)) {
	    byte[] src;
	    int srcOffset, dstOffset;
	    int tileLineBytes= tilew * imageBytesPerPixel;
            int copyBytes;

	    for (n = minTileY; n < minTileY+numYTiles; n++) {

		dstBegin = tileStart;	// destination buffer offset
		tmpw = copywidth;	// reset the width to be copied
		curw = startw;		// reset the width to be copied of
					// the left most tile
		x = srcX - startXTile;	// reset the starting x offset of
					// the left most tile

		for (m = minTileX; m < minTileX+numXTiles; m++) {

		    // retrieve the raster for the next tile
		    ras = ri.getTile(m,n);
		    src = ((DataBufferByte)ras.getDataBuffer()).getData();

		    srcOffset = (y * tilew + x) * imageBytesPerPixel;
		    dstOffset = dstBegin;

		    copyBytes = curw * imageBytesPerPixel;

		    //System.out.println("curh = "+curh+" curw = "+curw);
		    //System.out.println("x = "+x+" y = "+y);

		    for (h = 0; h < curh; h++) {
			System.arraycopy(src, srcOffset, image, dstOffset,
				copyBytes);
			srcOffset += tileLineBytes;
			dstOffset += dstLineBytes;
		    }

		    // advance the destination buffer offset
		    dstBegin += curw * imageBytesPerPixel;

		    // move to the next tile in x direction
		    x = 0;

		    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < tilew) {
			curw = tmpw;
		    } else {
			curw = tilew;
		    }
		}


		// we are done copying an array of tiles in the x direction
		// advance the tileStart offset 

		tileStart += width * imageBytesPerPixel * curh * sign;


		// move to the next set of tiles in y direction
		y = 0;

		// determine the height of copy region for the next set
		// of tiles
		tmph -= curh;
		if (tmph < tileh) {
		    curh = tmph;
		} else {
		    curh = tileh;
		}
	    }
	    return;
	}

	switch(format) {
	case ImageComponent.FORMAT_RGBA8: 
	case ImageComponent.FORMAT_RGB5_A1: 
	case ImageComponent.FORMAT_RGBA4: {
	    //	    System.out.println("Case 1: byReference = "+byReference);
	    for (n = minTileY; n < minTileY+numYTiles; n++) {

		dstBegin = tileStart;	// destination buffer offset
		tmpw = copywidth;	// reset the width to be copied
		curw = startw;		// reset the width to be copied of
					// the left most tile
		x = srcX - startXTile;	// reset the starting x offset of
					// the left most tile

		for (m = minTileX; m < minTileX+numXTiles; m++) {

		    // retrieve the raster for the next tile
		    ras = ri.getTile(m,n);

		    j = dstBegin;
		    offset = 0;

		    //System.out.println("curh = "+curh+" curw = "+curw);
		    //System.out.println("x = "+x+" y = "+y);

		    for (h = y; h < (y + curh); h++) {
			//			System.out.println("j = "+j);
			for (w = x; w < (x + curw); w++) {
			    ras.getDataElements(w, h, pixel);
			    image[j++]   = (byte)cm.getRed(pixel);
			    image[j++] = (byte)cm.getGreen(pixel);
			    image[j++] = (byte)cm.getBlue(pixel);
			    image[j++] = (byte)cm.getAlpha(pixel);
			}
			offset += dstLineBytes;
			j = dstBegin + offset;
		    }

		    // advance the destination buffer offset
		    dstBegin += curw * imageBytesPerPixel;

		    // move to the next tile in x direction
		    x = 0;

		    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < tilew) {
			curw = tmpw;
		    } else {
			curw = tilew;
		    }
		}


		// we are done copying an array of tiles in the x direction
		// advance the tileStart offset 

		tileStart += width * imageBytesPerPixel * curh * sign;


		// move to the next set of tiles in y direction
		y = 0;

		// determine the height of copy region for the next set
		// of tiles
		tmph -= curh;
		if (tmph < tileh) {
		    curh = tmph;
		} else {
		    curh = tileh;
		}
	    }
	}
	break;
	case ImageComponent.FORMAT_RGB8: 
	case ImageComponent.FORMAT_RGB5:
	case ImageComponent.FORMAT_RGB4: 
	case ImageComponent.FORMAT_R3_G3_B2: {
	    for (n = minTileY; n < minTileY+numYTiles; n++) {

		dstBegin = tileStart;	// destination buffer offset
		tmpw = copywidth;	// reset the width to be copied
		curw = startw;		// reset the width to be copied of
					// the left most tile
		x = srcX - startXTile;	// reset the starting x offset of
					// the left most tile

		for (m = minTileX; m < minTileX+numXTiles; m++) {

		    // retrieve the raster for the next tile
		    ras = ri.getTile(m,n);

		    j = dstBegin;
		    offset = 0;

		    //System.out.println("curh = "+curh+" curw = "+curw);
		    //System.out.println("x = "+x+" y = "+y);

		    for (h = y; h < (y + curh); h++) {
			//			System.out.println("j = "+j);
			for (w = x; w < (x + curw); w++) {
			    ras.getDataElements(w, h, pixel);
			    image[j++]   = (byte)cm.getRed(pixel);
			    image[j++] = (byte)cm.getGreen(pixel);
			    image[j++] = (byte)cm.getBlue(pixel);
			    image[j++] = (byte)255;
			}
			offset += dstLineBytes;
			j = dstBegin + offset;
		    }

		    // advance the destination buffer offset
		    dstBegin += curw * imageBytesPerPixel;

		    // move to the next tile in x direction
		    x = 0;

		    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < tilew) {
			curw = tmpw;
		    } else {
			curw = tilew;
		    }
		}


		// we are done copying an array of tiles in the x direction
		// advance the tileStart offset 

		tileStart += width * imageBytesPerPixel * curh * sign;


		// move to the next set of tiles in y direction
		y = 0;

		// determine the height of copy region for the next set
		// of tiles
		tmph -= curh;
		if (tmph < tileh) {
		    curh = tmph;
		} else {
		    curh = tileh;
		}
	    }
	}
	break;	    
	case ImageComponent.FORMAT_LUM8_ALPHA8: 
	case ImageComponent.FORMAT_LUM4_ALPHA4: {
	    for (n = minTileY; n < minTileY+numYTiles; n++) {

		dstBegin = tileStart;	// destination buffer offset
		tmpw = copywidth;	// reset the width to be copied
		curw = startw;		// reset the width to be copied of
					// the left most tile
		x = srcX - startXTile;	// reset the starting x offset of
					// the left most tile

		for (m = minTileX; m < minTileX+numXTiles; m++) {

		    // retrieve the raster for the next tile
		    ras = ri.getTile(m,n);

		    j = dstBegin;
		    offset = 0;

		    //System.out.println("curh = "+curh+" curw = "+curw);
		    //System.out.println("x = "+x+" y = "+y);

		    for (h = y; h < (y + curh); h++) {
			//			System.out.println("j = "+j);
			for (w = x; w < (x + curw); w++) {
			    ras.getDataElements(w, h, pixel);
			    image[j++]   = (byte)cm.getRed(pixel);
			    image[j++] = (byte)cm.getAlpha(pixel);
			}
			offset += dstLineBytes;
			j = dstBegin + offset;
		    }

		    // advance the destination buffer offset
		    dstBegin += curw * imageBytesPerPixel;

		    // move to the next tile in x direction
		    x = 0;

		    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < tilew) {
			curw = tmpw;
		    } else {
			curw = tilew;
		    }
		}


		// we are done copying an array of tiles in the x direction
		// advance the tileStart offset 

		tileStart += width * imageBytesPerPixel * curh * sign;


		// move to the next set of tiles in y direction
		y = 0;

		// determine the height of copy region for the next set
		// of tiles
		tmph -= curh;
		if (tmph < tileh) {
		    curh = tmph;
		} else {
		    curh = tileh;
		}
	    }
	}
	break;
	case ImageComponent.FORMAT_CHANNEL8: {
	    for (n = minTileY; n < minTileY+numYTiles; n++) {

		dstBegin = tileStart;	// destination buffer offset
		tmpw = copywidth;	// reset the width to be copied
		curw = startw;		// reset the width to be copied of
					// the left most tile
		x = srcX - startXTile;	// reset the starting x offset of
					// the left most tile

		for (m = minTileX; m < minTileX+numXTiles; m++) {

		    // retrieve the raster for the next tile
		    ras = ri.getTile(m,n);

		    j = dstBegin;
		    offset = 0;

		    //System.out.println("curh = "+curh+" curw = "+curw);
		    //System.out.println("x = "+x+" y = "+y);

		    for (h = y; h < (y + curh); h++) {
			//			System.out.println("j = "+j);
			for (w = x; w < (x + curw); w++) {
			    ras.getDataElements(w, h, pixel);
			    image[j++]   = (byte)cm.getRed(pixel);
			}
			offset += dstLineBytes;
			j = dstBegin + offset;
		    }

		    // advance the destination buffer offset
		    dstBegin += curw * imageBytesPerPixel;

		    // move to the next tile in x direction
		    x = 0;

		    // determine the width of copy region of the next tile

		    tmpw -= curw;
		    if (tmpw < tilew) {
			curw = tmpw;
		    } else {
			curw = tilew;
		    }
		}


		// we are done copying an array of tiles in the x direction
		// advance the tileStart offset 

		tileStart += width * imageBytesPerPixel * curh * sign;


		// move to the next set of tiles in y direction
		y = 0;

		// determine the height of copy region for the next set
		// of tiles
		tmph -= curh;
		if (tmph < tileh) {
		    curh = tmph;
		} else {
		    curh = tileh;
		}
	    }
	}
	break;
	default:
	break;
	}
    }


    /**
     * Copy entire image data from Buffered Image to 
     * ImageComponent's internal representation
     */ 
    final void copyImage(BufferedImage bi, byte[] image, 
				boolean usedByTexture, int depth, 
				int imageFormat, int imageBytesPerPixel) {
	    copyImage(bi, 0, 0, image, 0, 0, usedByTexture, depth, 
		width, height, imageFormat, imageBytesPerPixel);
    }

    /**
     * Copy specified region of image data from Buffered Image to 
     * ImageComponent's internal representation
     */ 
    final void copyImage(BufferedImage bi, int srcX, int srcY,
	byte[] image, int dstX, int dstY, boolean usedByTexture, 
	int depth, int copywidth, int copyheight, 
	int imageFormat, int imageBytesPerPixel) {

	int w, h, i, j;
	int rowBegin,		// src begin row index
            srcBegin,		// src begin offset
            dstBegin,		// dst begin offset
            rowInc,		// row increment
				// -1 --- ydown
				//  1 --- yup
	    row;
        Object pixel = null;

	rowBegin = srcY;
        rowInc = 1;

        int dstBytesPerRow = width * imageBytesPerPixel; // bytes per row
							    // in dst image

	if ((!yUp && usedByTexture) || (yUp && !usedByTexture)) {
	    dstBegin = (depth * width * height + 
				(height - dstY - 1) * width + dstX) *
					imageBytesPerPixel;

	    dstBytesPerRow = - 1 * dstBytesPerRow;

	} else {
	    dstBegin = (dstY * width + dstX) * imageBytesPerPixel;
	}

	// if the image format matches the format of the incoming 
	// buffered image, then do a straight copy, else do the
	// format conversion while copying the data

	if (formatMatches(imageFormat, bi)) {
            byte[] byteData =
                ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
	    int copyBytes = copywidth * imageBytesPerPixel;
	    int scanline = width * imageBytesPerPixel;
	    
	    srcBegin = (rowBegin * width + srcX) * imageBytesPerPixel;
	    for (h = 0; h < copyheight; h++) {
		System.arraycopy(byteData, srcBegin, image, dstBegin, copyBytes);
		dstBegin += dstBytesPerRow;
		srcBegin += scanline;
	    }
        } else {

	    int biType = bi.getType();
	    if ((biType == BufferedImage.TYPE_INT_ARGB ||
	         biType == BufferedImage.TYPE_INT_RGB) && 
	        (format == ImageComponent.FORMAT_RGBA8 || 
	         format == ImageComponent.FORMAT_RGB8)) {

	        // optimized cases

                int[] intData =
                    ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
	        int rowOffset = rowInc * width;
	        int intPixel;
	    
	        srcBegin = rowBegin * width + srcX;
	
	        if (biType == BufferedImage.TYPE_INT_ARGB &&
                    format == ImageComponent.FORMAT_RGBA8) {
                    for (h = 0; h < copyheight; h++) {   
	              i = srcBegin;
                      j = dstBegin;
                      for (w = 0; w < copywidth; w++, i++) {   
                        intPixel = intData[i];
                        image[j++]   = (byte)((intPixel >> 16) & 0xff);
                        image[j++] = (byte)((intPixel >>  8) & 0xff);
                        image[j++] = (byte)(intPixel & 0xff);
                        image[j++] = (byte)((intPixel >> 24) & 0xff);
                      }
	              srcBegin += rowOffset;
	              dstBegin += dstBytesPerRow;
                    }
                } else { // format == ImageComponent.FORMAT_RGB8
                    for (h = 0; h < copyheight; h++) {   
	              i = srcBegin;
		      j = dstBegin;
                      for (w = 0; w < copywidth; w++, i++) {   
                        intPixel = intData[i];
                        image[j++]   = (byte)((intPixel >> 16) & 0xff);
                        image[j++] = (byte)((intPixel >>  8) & 0xff);
                        image[j++] = (byte)(intPixel & 0xff);
                        image[j++] = (byte)255;
                      }
	              srcBegin += rowOffset;
	              dstBegin += dstBytesPerRow;
                    }
                }

	    } else if ((biType == BufferedImage.TYPE_BYTE_GRAY) &&
	          (format == ImageComponent.FORMAT_CHANNEL8)) {

                byte[] byteData =
                    ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
	        int rowOffset = rowInc * width;
	    
	        j = dstBegin;
	        srcBegin = rowBegin * width + srcX;

	        for (h = 0; h < copyheight; 
			h++, j += width, srcBegin += rowOffset) {   
	          System.arraycopy(byteData, srcBegin, image, j, copywidth);
	        }
	    } else {
	    // non-optimized cases

                WritableRaster ras = bi.getRaster(); 
                ColorModel cm = bi.getColorModel();
		pixel = getDataElementBuffer(ras);

	        switch(format) {
                case ImageComponent.FORMAT_RGBA8: 
                case ImageComponent.FORMAT_RGB5_A1: 
	        case ImageComponent.FORMAT_RGBA4: {
                    for (row = rowBegin, h = 0;
                            h < copyheight; h++, row += rowInc) {
		      j = dstBegin;
                      for (w = srcX; w < (copywidth + srcX); w++) {
		          ras.getDataElements(w, row, pixel);
		          image[j++]   = (byte)cm.getRed(pixel);
		          image[j++] = (byte)cm.getGreen(pixel);
		          image[j++] = (byte)cm.getBlue(pixel);
		          image[j++] = (byte)cm.getAlpha(pixel);
                      }    
	              dstBegin += dstBytesPerRow;
                    }
                }
                break;

                case ImageComponent.FORMAT_RGB8: 
	        case ImageComponent.FORMAT_RGB5:
	        case ImageComponent.FORMAT_RGB4: 
                case ImageComponent.FORMAT_R3_G3_B2: {
                    for (row = rowBegin, h = 0;
                            h < copyheight; h++, row += rowInc) {
		      j = dstBegin;
                      for (w = srcX; w < (copywidth + srcX); w++) {
		          ras.getDataElements(w, row, pixel);
		          image[j++]   = (byte)cm.getRed(pixel);
		          image[j++] = (byte)cm.getGreen(pixel);
		          image[j++] = (byte)cm.getBlue(pixel);
		          image[j++] = (byte)255;
                      }
		      dstBegin += dstBytesPerRow;
                  }
                }
                break;

	        case ImageComponent.FORMAT_LUM8_ALPHA8: 
	        case ImageComponent.FORMAT_LUM4_ALPHA4: {
                    for (row = rowBegin, h = 0;
                            h < copyheight; h++, row += rowInc) {
		      j = dstBegin;
                      for (w = srcX; w < (copywidth + srcX); w++) {
		          ras.getDataElements(w, row, pixel);
		          image[j++]   = (byte)cm.getRed(pixel);
		          image[j++] = (byte)cm.getAlpha(pixel);
                      }
		      dstBegin += dstBytesPerRow;
                    }    
	        }
	        break;

	        case ImageComponent.FORMAT_CHANNEL8: {
                    for (row = rowBegin, h = 0;
                            h < copyheight; h++, row += rowInc) {
		      j = dstBegin;
                      for (w = srcX; w < (copywidth + srcX); w++) {
		          ras.getDataElements(w, row, pixel);
		          image[j++]   = (byte)cm.getRed(pixel);
                      }
		      dstBegin += dstBytesPerRow;
                  }    
                }
	        break;
                }
            }
	}
   }

    


   final int getBytesStored(int f) {
       int val = 0;
       switch(f) {
       case BYTE_RGBA:
	  val =  4;
	  break;
       case BYTE_ABGR:
	  val =  4;
	  break;
       case BYTE_GRAY:
	   val = 1;
	   break;
       case USHORT_GRAY:
	   val = 2;
	   break;
       case BYTE_LA:
	  val = 2;
	  break;
       case BYTE_BGR:;
	  val = 3;
	  break;
      case BYTE_RGB:;
	  val = 3;
	  break;
       }
       return val;
    }


    boolean is4ByteRGBAOr3ByteRGB(RenderedImage ri) {
        boolean value = false;
        int i;
        int biType = getImageType(ri);
        if (biType != BufferedImage.TYPE_CUSTOM)
            return false;
        ColorModel cm = ri.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        SampleModel sm = ri.getSampleModel();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        int csType = cs.getType();
        if ( csType == ColorSpace.TYPE_RGB) {
            int numBands = sm.getNumBands();
            if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (cm instanceof ComponentColorModel &&
                    sm instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel csm = 
				(PixelInterleavedSampleModel) sm;
                    int[] offs = csm.getBandOffsets();
                    ComponentColorModel ccm = (ComponentColorModel)cm;
                    int[] nBits = ccm.getComponentSize();
                    boolean is8Bit = true;
                    for (i=0; i < numBands; i++) {
                        if (nBits[i] != 8) {
                            is8Bit = false;
                            break;
                        }
                    }
                    if (is8Bit &&
                        offs[0] == 0 &&
                        offs[1] == 1 &&
                        offs[2] == 2) {
                        if (numBands == 3) {
                            if (format == ImageComponent.FORMAT_RGB)
                                value = true;
                        }
                        else if (offs[3] == 3 && !isAlphaPre) {
                            if (format == ImageComponent.FORMAT_RGBA)
                                value = true;
                        }
                    }
                }
            }
        }
        return value;
    }
       
    final int getImageType(RenderedImage ri) {
        int imageType = BufferedImage.TYPE_CUSTOM;
        int i;

        if (ri instanceof BufferedImage) {
            return ((BufferedImage)ri).getType();
        }
        ColorModel cm = ri.getColorModel();
        ColorSpace cs = cm.getColorSpace();
        SampleModel sm = ri.getSampleModel();
        int csType = cs.getType();
        boolean isAlphaPre = cm.isAlphaPremultiplied();
        if ( csType != ColorSpace.TYPE_RGB) {
            if (csType == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                    imageType = BufferedImage.TYPE_BYTE_GRAY;
                } else if (sm.getDataType() == DataBuffer.TYPE_USHORT) {
                    imageType = BufferedImage.TYPE_USHORT_GRAY;
                }
            }
        }
        // RGB , only interested in BYTE ABGR and BGR for now
        // all others will be copied to a buffered image
        else {
            int numBands = sm.getNumBands();
            if (sm.getDataType() == DataBuffer.TYPE_BYTE) {
                if (cm instanceof ComponentColorModel &&
                    sm instanceof PixelInterleavedSampleModel) {
                    PixelInterleavedSampleModel csm = 
				(PixelInterleavedSampleModel) sm;
                    int[] offs = csm.getBandOffsets();
                    ComponentColorModel ccm = (ComponentColorModel)cm;
                    int[] nBits = ccm.getComponentSize();
                    boolean is8Bit = true;
                    for (i=0; i < numBands; i++) {
                        if (nBits[i] != 8) {
                            is8Bit = false;
                            break;
                        }
                    }
                    if (is8Bit &&
                        offs[0] == numBands-1 &&
                        offs[1] == numBands-2 &&
                        offs[2] == numBands-3) {
                        if (numBands == 3) {
                            imageType = BufferedImage.TYPE_3BYTE_BGR;
                        }
                        else if (offs[3] == 0) {
                            imageType = (isAlphaPre
                                         ? BufferedImage.TYPE_4BYTE_ABGR_PRE
                                         : BufferedImage.TYPE_4BYTE_ABGR);
                        }
                    }
                }
            }
        }
        return imageType;
    }


    

    /**
     * Retrieves the bufferedImage at the specified depth level
     */  
    final void retrieveBufferedImage(int depth) {

        // create BufferedImage if one doesn't exist
        if (bImage[depth] == null) {
            if (format == ImageComponent.FORMAT_RGBA ||
                format == ImageComponent.FORMAT_RGBA4 ||
                format == ImageComponent.FORMAT_RGB5_A1 ||
                format == ImageComponent.FORMAT_LUM4_ALPHA4 ||
                format == ImageComponent.FORMAT_LUM8_ALPHA8) {
                bImage[depth] = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_ARGB);
	    }
            else
                bImage[depth] = new BufferedImage(width, height,
                        BufferedImage.TYPE_INT_RGB);
        }

	if (usedByTexture || !usedByRaster) {
            copyToBufferedImage(imageYup, depth, true);
	} else {
            copyToBufferedImage(imageYdown[0], depth, false);
	}
        imageDirty[depth] = false;

    }

    /**
     * Copy Image from RGBA to the user defined bufferedImage
     */
    final void copyBufferedImageWithFormatConversion(boolean usedByTexture, int depth) {
        int w, h, i, j;
        int dstBegin, dstInc, dstIndex, dstIndexInc;
	// Note that if a copy has been made, then its always a bufferedImage
	// and not a renderedImage
	BufferedImage bi = (BufferedImage)bImage[depth];
	int biType = bi.getType();
	byte[] buf;
	
       // convert from Ydown to Yup for texture
	if (!yUp) {
	    if (usedByTexture == true) {
		dstInc = -1 * width;
		dstBegin = (height - 1) * width;
		dstIndex = height -1;
		dstIndexInc = -1;
		buf = imageYup;
	    } else {
		dstInc = width;
		dstBegin = 0;
		dstIndex = 0;
		dstIndexInc = 1;
		buf = imageYdown[0];
	    }
	}
	else {
	    if (usedByTexture == true) {
		dstInc = width;
		dstBegin = 0;
		dstIndex = 0;
		dstIndexInc = 1;
		buf = imageYup;
	    }
	    else {
		dstInc = -1 * width;
		dstBegin = (height - 1) * width;
		dstIndex = height -1;
		dstIndexInc = -1;
		buf = imageYdown[0];
	    }
	}
	
	switch (biType) {
	case BufferedImage.TYPE_INT_ARGB:
	    int[] intData =
		((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
	    // Multiply by 4 to get the byte incr and start point
	    j = 0;
	    for(h = 0; h < height; h++, dstBegin += dstInc) {
		i = dstBegin;
		for (w = 0; w < width; w++, j+=4, i++) {
		    intData[i] = (((buf[j+3] &0xff) << 24) | // a
				  ((buf[j] &0xff) << 16) | // r
				  ((buf[j+1] &0xff) << 8) | // g 
				  (buf[j+2] & 0xff)); // b


		}
	    }
	    break;

	case BufferedImage.TYPE_INT_RGB:
	    intData =
		((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
	    // Multiply by 4 to get the byte incr and start point
	    j = 0;
	    for(h = 0; h < height; h++, dstBegin += dstInc) {
		i = dstBegin;
		for (w = 0; w < width; w++, j+=4, i++) {
		    intData[i] = (0xff000000 | // a
				  ((buf[j] &0xff) << 16) | // r
				  ((buf[j+1] &0xff) << 8) | // g 
				  (buf[j+2] & 0xff)); // b


		}
	    }
	    break;

	case BufferedImage.TYPE_4BYTE_ABGR:
	    byte[] byteData =
		((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
	    // Multiply by 4 to get the byte incr and start point
	    j = 0;
	    for(h = 0; h < height; h++, dstBegin += (dstInc << 2)) {
		i = dstBegin;
		for (w = 0; w < width; w++, j+=4) {

		    byteData[i++] = buf[j+3]; // a
		    byteData[i++] = buf[j+2]; // b
		    byteData[i++] = buf[j+1];// g
		    byteData[i++] = buf[j]; // r
		}
	    }
	    break;
	case BufferedImage.TYPE_INT_BGR:
	    intData =
		((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
	    // Multiply by 4 to get the byte incr and start point
	    j = 0;
	    
	    for(h = 0; h < height; h++, dstBegin += dstInc) {
		i = dstBegin;
		for (w = 0; w < width; w++, j+=4, i++) {
		    intData[i] = (0xff000000 | // a
				  ((buf[j] &0xff) ) | // r
				  ((buf[j+1] &0xff) << 8) | // g 
				  (buf[j+2] & 0xff)<< 16); // b


		}
	    }
	    break;
	case BufferedImage.TYPE_BYTE_GRAY:
	    byteData =
		((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
	    j = 0;
	    for( h = 0; h < height; h++, dstBegin += dstInc) {
		System.arraycopy(byteData, dstBegin, buf, j, width);
		j += width;
	    }
	    break;		
	case BufferedImage.TYPE_USHORT_GRAY:
	    int pixel;
	    j = 0;
	    short[] shortData  =
		((DataBufferShort)bi.getRaster().getDataBuffer()).getData();
	    // Multiply by 4 to get the byte incr and start point
	    for(h = 0; h < height; h++, dstBegin+= dstInc) {
		i = dstBegin;
		for (w = 0; w < width; w++, i++, j++) {
		    shortData[i] = (short)buf[j];
		}
	    }
	    break;
	
	default:
	    j = 0;
	    for( h = 0; h < height; h++, dstIndex += dstIndexInc) {
		i = dstIndex;
		for (w = 0; w < width; w++, j+=4) { 
		    pixel =  (((buf[j+3] &0xff) << 24) | // a
			      ((buf[j] &0xff) << 16) | // r 
			      ((buf[j+1] &0xff) << 8) | // g
			      (buf[j+2] & 0xff)); // b
		    bi.setRGB(w, i, pixel);
		    
		}
	    }
	    break;
	}
	
    }
    
    /**
     * Copy image data from ImageComponent's internal representation
     * to Buffered Image 
     */  
    final void copyToBufferedImage(byte[] buf, int depth, 
		boolean usedByTexture) {
 
        int w, h, i, j;
        int dstBegin, dstInc, srcBegin;


        // convert from Ydown to Yup for texture
	if (!yUp) {
	    if (usedByTexture == true) {
		srcBegin = depth * width * height * bytesPerYupPixelStored;
		dstInc = -1 * width;
		dstBegin = (height - 1) * width;
	    } else {
		srcBegin = 0;
		dstInc = width;
		dstBegin = 0;
	    }
	}
	else {
	    if (usedByTexture == true) {
		srcBegin = 0;
		dstInc = width;
		dstBegin = 0;
	    }
	    else {
		srcBegin = depth * width * height * bytesPerYdownPixelStored;
		dstInc = -1 * width;
		dstBegin = (height - 1) * width;
	    }
	}

	// Note that if a copy has been made, then its always a bufferedImage
	// and not a renderedImage
        int[] intData = ((DataBufferInt)
		((BufferedImage)bImage[depth]).getRaster().getDataBuffer()).getData();

        switch(format) {
        case ImageComponent.FORMAT_RGBA8:
        case ImageComponent.FORMAT_RGB5_A1:
        case ImageComponent.FORMAT_RGBA4: 

            for (j = srcBegin, h = 0; h < height; h++, dstBegin += dstInc) {
		i = dstBegin;
                for (w = 0; w < width; w++, j+=4, i++) {
                    intData[i]  = ((buf[j+3] & 0xff) << 24) |
                                  ((buf[j]   & 0xff) << 16) |
                                  ((buf[j+1] & 0xff) <<  8) |
                                  (buf[j+2]  & 0xff);
                }
            }
	    break;


        case ImageComponent.FORMAT_RGB8:
        case ImageComponent.FORMAT_RGB5:
        case ImageComponent.FORMAT_RGB4:
        case ImageComponent.FORMAT_R3_G3_B2: 
            for (j = srcBegin, h = 0; h < height; h++, dstBegin += dstInc) {
                i = dstBegin;
                for (w = 0; w < width; w++, j+=4, i++) {
                    intData[i]  = ((buf[j]   & 0xff) << 16) |
                                  ((buf[j+1] & 0xff) <<  8) |
                                  (buf[j+2] & 0xff);
                }
            }   
	    break;

        case ImageComponent.FORMAT_LUM8_ALPHA8:
        case ImageComponent.FORMAT_LUM4_ALPHA4:
            for (j = srcBegin, h = 0; h < height; h++, dstBegin += dstInc) {
                i = dstBegin; 
                for (w = 0; w < width; w++, j+=2, i++) { 
                    intData[i]  = ((buf[j+1]   & 0xff) << 24) |
                                  ((buf[j]     & 0xff) << 16);
                }
            }   
            break;

        case ImageComponent.FORMAT_CHANNEL8:
            for (j = srcBegin, h = 0; h < height; h++, dstBegin += dstInc) {
                i = dstBegin; 
                for (w = 0; w < width; w++, j++, i++) { 
                    intData[i]  = ((buf[j] & 0xff) << 16);
                }
            }   
            break;
	}
    }

    Object getData(DataBuffer buffer) {
	Object data = null;
	switch (buffer.getDataType()) {
	case DataBuffer.TYPE_BYTE:
	    data = ((DataBufferByte)buffer).getData();
	    break;
	case DataBuffer.TYPE_INT:
	    data = ((DataBufferInt)buffer).getData();
	    break;
	case DataBuffer.TYPE_SHORT:
	    data = ((DataBufferShort)buffer).getData();
	    break;
	}
	return data;
    }

     final void setByReference(boolean byReference) {
 	this.byReference = byReference;
     }
     
     final boolean isByReference() {
 	return byReference;
     }
 
     final void setYUp( boolean yUp) {
 	this.yUp = yUp;
     }
 
     final boolean isYUp() {
 	return yUp;
     }

     // Add a user to the userList
     synchronized void addUser(NodeComponentRetained node) {
        userList.add(node);
     }

     // Add a user to the  userList
     synchronized void removeUser(NodeComponentRetained node) {
	int i = userList.indexOf(node);
	if (i >= 0) {
	    userList.remove(i);
	}
     }

     /**
      * ImageComponent object doesn't really have mirror object.
      * But it's using the updateMirrorObject interface to propagate
      * the changes to the users
      */
     synchronized void updateMirrorObject(int component, Object value) {

	//System.out.println("ImageComponent.updateMirrorObject");

	Object user;

        if (((component & IMAGE_CHANGED) != 0) || 
		((component & SUBIMAGE_CHANGED) != 0)) {
	    synchronized(userList) {
                for (int i = userList.size()-1; i >=0; i--) {
                     user = userList.get(i);
		     if (user != null) {
			 if (user instanceof TextureRetained) {
			     ((TextureRetained)user).notifyImageComponentImageChanged(this, (ImageComponentUpdateInfo)value);
			 } else if (user instanceof RasterRetained) {
			     ((RasterRetained)user).notifyImageComponentImageChanged(this, (ImageComponentUpdateInfo)value);
			 } else if (user instanceof BackgroundRetained) {
			     ((BackgroundRetained)user).notifyImageComponentImageChanged(this, (ImageComponentUpdateInfo)value);
			 }
		     }
		}
	    }

	    // return the subimage update info to the free list
	    if (value != null) {
		VirtualUniverse.mc.addFreeImageUpdateInfo(
			(ImageComponentUpdateInfo)value);
	    }
	}
     }

     final void sendMessage(int attrMask, Object attr) {

        J3dMessage createMessage = VirtualUniverse.mc.getMessage();
        createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES |
				J3dThread.UPDATE_RENDER;
        createMessage.type = J3dMessage.IMAGE_COMPONENT_CHANGED;
        createMessage.universe = null;
        createMessage.args[0] = this;
        createMessage.args[1]= new Integer(attrMask);
        createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
        VirtualUniverse.mc.processMessage(createMessage);
     }

    void handleFrequencyChange(int bit) {
	if (bit == ImageComponent.ALLOW_IMAGE_WRITE) {
	    setFrequencyChangeMask(ImageComponent.ALLOW_IMAGE_WRITE, 0x1);
	}
    }

    static Object getDataElementBuffer(java.awt.image.Raster ras) {
	int nc = ras.getNumDataElements();		

        switch (ras.getTransferType()) {
   	    case DataBuffer.TYPE_INT:
		return new int[nc];
	    case DataBuffer.TYPE_BYTE:
		return new byte[nc];
 	    case DataBuffer.TYPE_USHORT:
	    case DataBuffer.TYPE_SHORT:
		return new short[nc];
	    case DataBuffer.TYPE_FLOAT:
		return new float[nc];
	    case DataBuffer.TYPE_DOUBLE:
		return new double[nc];
	}
	// Should not happen
	return null;
    }
}
