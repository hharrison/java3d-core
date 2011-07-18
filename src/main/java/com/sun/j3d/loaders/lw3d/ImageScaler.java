/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.loaders.lw3d;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * This class resizes an image to be the nearest power of 2 wide and high.
 * This facility now exists inside of the TextureLoader class, so
 * ImageScaler should be eliminated at some point.
 */

class ImageScaler {

    int origW, origH;
    Image origImage;
    
    ImageScaler(Image image, int w, int h) {
	origImage = image;
	origW = w;
	origH = h;
    }

    ImageScaler(BufferedImage image) {
	origImage = image;
	origW = image.getWidth();
	origH = image.getHeight();
    }

	/**
	* Utility method to return closes poser of 2 to the given integer
	*/    
    int getClosestPowerOf2(int value) {

	if (value < 1)
	    return value;
	
	int powerValue = 1;
	for (int i = 1; i < 20; ++i) {
	    powerValue *= 2;
	    if (value < powerValue) {
		// Found max bound of power, determine which is closest
		int minBound = powerValue/2;
		if ((powerValue - value) >
		    (value - minBound))
		    return minBound;
		else
		    return powerValue;
	    }
	}
	// shouldn't reach here...
	return 1;
    }
	 
	/**
	* Returns an Image that has been scaled from the original image to
	* the closest power of 2
	*/   
    Image getScaledImage() {
	int newWidth = getClosestPowerOf2(origW);
	int newHeight = getClosestPowerOf2(origH);

	// If the image is already a power of 2 wide/tall, no need to scale
	if (newWidth == origW &&
	    newHeight == origH)
	    return origImage;

	Image scaledImage = null;
	
	if (origImage instanceof BufferedImage) {
		// If BufferedImage, then we have some work to do
	    BufferedImage origImageB = (BufferedImage)origImage;
	    scaledImage =
		new BufferedImage(newWidth,
				  newHeight,
				  origImageB.getType());
	    BufferedImage scaledImageB = (BufferedImage)scaledImage;
	    float widthScale = (float)origW/(float)newWidth;
	    float heightScale = (float)origH/(float)newHeight;
	    int origPixels[] = ((DataBufferInt)origImageB.getRaster().getDataBuffer()).getData();
	    int newPixels[] = ((DataBufferInt)scaledImageB.getRaster().getDataBuffer()).getData();
	    for (int row = 0; row < newHeight; ++row) {
		for (int column = 0; column < newWidth; ++column) {
		    int oldRow = Math.min(origH-1,
					  (int)((float)(row)*
						heightScale + .5f));
		    int oldColumn =
			Math.min(origW-1, 
				 (int)((float)column*widthScale + .5f));
		    newPixels[row*newWidth + column] =
			origPixels[oldRow*origW + oldColumn];
		}
	    }
	}
	else {
		// If regular Image, then the work is done for us
	    scaledImage = origImage.getScaledInstance(newWidth,
						      newHeight,
						      Image.SCALE_DEFAULT);
	}
	return scaledImage;
    }
}
