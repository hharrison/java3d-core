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

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.MediaTracker;
import java.awt.Frame;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import java.io.IOException;

/**
 * This class parses a standard Targa file and retrieves the image stored
 * therein, storing the pixel data in a BufferedImage.
 */

class TargaReader extends ParserObject {

    BufferedInputStream  bufferedReader;
    Image theImage = null;

    /**
     * Constructor: creates file reader and calls parseFile() to do the real
     * work
     */
    TargaReader(String fileName, int debugVals) throws FileNotFoundException {
	super(debugVals);
	debugOutputLn(TRACE, "constructor");
	bufferedReader = new BufferedInputStream(
		 new DataInputStream(new FileInputStream(fileName)));
        if (bufferedReader != null)
            parseFile();
    }

    /**
     * Returns the image that was created from parsing the targa file (null
     * if the file reading failed)
     */
    Image getImage() {
	return theImage;
    }

    /**
     * This method parses the file and stores the pixel data in a
     * BufferedImage.  The basic file format is:
     *		Byte		Description
     *
     *		0		Image ID Length
     *		1		Colormap type
     *		2		Image Type
     *		3-4		Colormap spec: 1st entry index
     *		5-6		Colormap spec: length
     *		7		Colormap spec: entry size
     *		8-9		X-origin of lower-left corner
     *		10-11		Y-origin of lower-left corner
     *		12-13		Image width
     *		14-15		Image height
     *		16		Pixel depth
     *		17		00(origin)(alpha)
     *				first 2 bytes 0, next 2 starting corner,
     *				last four number of overlay bits per pixel
     *		18-		Image ID
     *		??		Colormap data
     *		??		Image Data
     *		??		Developer Area
     *		??		Extension Area
     *		??		File Footer
     *
     * We're going to make some assumptions about the format of files we're
     * asked to load.  In particular, we're not going to do any colormpa-based
     * images: the images need to be either 24-bit or 32-bit true color.
     * We're also going to ignore vaiours parameters in the header block, since
     * they complicate life and don't appear to be used in Lightwave image
     * files.  In particular, the following fields will be ignored:
     * Image ID, colormap info, xy origins, and alpha/overlay bits.
     */

    void parseFile()
	throws IncorrectFormatException, ParsingErrorException {
	try {
	    int idLength = bufferedReader.read();
	    int colormapPresent = bufferedReader.read();
	    int imageType = bufferedReader.read();
	    bufferedReader.skip(9); // skipping camp and xy origin data
	    int width = bufferedReader.read() | bufferedReader.read() << 8;
	    int height = bufferedReader.read() | bufferedReader.read() << 8;
	    int depth = bufferedReader.read();
	    int flags = bufferedReader.read();
	    boolean bottomToTop = ((flags & 0x20) == 0);
	    boolean leftToRight = ((flags & 0x10) == 0);
	    bufferedReader.skip(idLength);
	    
	    // Check on the file parameters to see whether we should punt
	    if ((colormapPresent == 1) ||
		imageType != 2 ||
		(depth != 24 &&
		 depth != 32)) {
		    // Punt
		    throw new IncorrectFormatException(
			"This format is not readable by the Lightwave " +
			"loader.  Only 24- or 32-bit true-color " +
			"uncompressed Targa images will work");
	    }

	    // Image format must be okay for us to read
	    BufferedImage bImage =
		new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    int[] imageBits =
		((DataBufferInt)bImage.getRaster().getDataBuffer()).getData();

	    int row;
	    int column;

	    for (int i = 0; i < height; ++i) {
		if (bottomToTop)
		    row = (height - i - 1);
		else
		    row = i;
		for (int j = 0; j < width; ++j) {

		    if (leftToRight)
			column = j;
		    else
			column = (width - j - 1);

		    int blue = bufferedReader.read();
		    int green = bufferedReader.read();
		    int red = bufferedReader.read();
		    int alpha = 0xff;
		    if (depth == 32)
			alpha = bufferedReader.read();
		    imageBits[row*width + column] = alpha << 24 |
			red << 16 |
			green << 8 |
			blue;
		}
	    }
	    theImage = bImage;
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }

}

