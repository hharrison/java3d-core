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

package com.sun.j3d.loaders.objectfile;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.ComponentColorModel;
import java.awt.image.WritableRaster;
import java.awt.color.ColorSpace;
import java.awt.Transparency;

class RgbFile extends BufferedInputStream {

    // Header data
    short dimension;
    short xSize;
    short ySize;
    short zSize;

    String filename;

    private static final int DEBUG = 0;


    short getShort() throws IOException {
	int t1 = (short)read();
	if (t1 == -1) throw new IOException("Unexpected EOF");
	int t2 = (short)read();
	if (t2 == -1) throw new IOException("Unexpected EOF");
	return (short)((t1 << 8) | t2);
    } // End of getShort()


    byte getByte() throws IOException {
	int t = read();
	if (t == -1) throw new IOException("Unexpected EOF");
	return (byte)t;
    } // End of getByte


    int getInt() throws IOException {
	int ret = 0;
	for (int i = 0 ; i < 4 ; i++) {
	    int t = read();
	    if (t == -1) throw new IOException("Unexpected EOF");
	    ret = (ret << 8) | t;
	}
	return ret;
    } // end of getInt


    public BufferedImage getImage() throws IOException {
	short magic = getShort();

	if (magic != 474) throw new IOException("Unrecognized file format.");

	byte storage = getByte();

	if (storage != 0)
	    throw new IOException("RLE Compressed files not supported");
    
	byte bpc = getByte();
	dimension = getShort();
	xSize = getShort();
	ySize = getShort();
	zSize = getShort();
	int pixMin = getInt();
	int pixMax = getInt();
	skip(84l);
	int colorMap = getInt();

	if ((DEBUG & 1) != 0) {
	    System.out.println(filename + ":");
	    System.out.println("  bpc = " + bpc);
	    System.out.println("  dimension = " + dimension);
	    System.out.println("  xSize = " + xSize);
	    System.out.println("  ySize = " + ySize);
	    System.out.println("  zSize = " + zSize);
	    System.out.println("  pixMin = " + pixMin);
	    System.out.println("  pixMax = " + pixMax);
	    System.out.println("  colorMap = " + colorMap);
	}

	if ((pixMin != 0) || (pixMax != 0xff) || (colorMap != 0) || (bpc != 1))
	    throw new IOException("Unsupported options in file");

	skip(404l);

	ComponentColorModel cm = null;
	if (zSize == 1) {
	    // Black and White image
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);

	    int[] nBits = {8};
	    cm = new ComponentColorModel(cs, nBits, false, false,
					 Transparency.OPAQUE, 
					 DataBuffer.TYPE_BYTE);

	} else if (zSize == 2) {
	    // Black and White image with alpha
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);

	    int[] nBits = {8, 8};
	    cm = new ComponentColorModel(cs, nBits, true, false,
					 Transparency.TRANSLUCENT,
					 DataBuffer.TYPE_BYTE);

	} else if (zSize == 3) {
	    // RGB Image
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	    int[] nBits = {8, 8, 8};
	    cm = new ComponentColorModel(cs, nBits, false, false,
					 Transparency.OPAQUE,
					 DataBuffer.TYPE_BYTE);

	} else if (zSize == 4) {
	    // RGBA Image
	    ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

	    int[] nBits = {8, 8, 8, 8};
	    cm = new ComponentColorModel(cs, nBits, true, false,
					 Transparency.TRANSLUCENT,
					 DataBuffer.TYPE_BYTE);
	} else {
	    throw new IOException("Unsupported options in file");
	}

	WritableRaster r = cm.createCompatibleWritableRaster(xSize, ySize);
	BufferedImage bi = new BufferedImage(cm, r, false, null);

	int t;
	byte image[] = ((DataBufferByte)r.getDataBuffer()).getData();
	for (short z = 0 ; z < zSize ; z++) {
	    for (int y = ySize - 1 ; y >= 0 ; y--) {
		for (short x = 0 ; x < xSize ; x++) {
		    t = read();
		    if (t == -1) throw new IOException("Unexpected EOF");
		    image[y * (xSize * zSize) + (x * zSize) + z] = (byte)t;
		}
	    }
	}

	return bi;
    } // End of getImage


    public RgbFile(InputStream s) {
	super(s);
    } // End of RgbFile(URL)

} // End of class RgbFile

// End of file RgbFile.java
