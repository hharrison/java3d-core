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

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import com.sun.j3d.utils.image.TextureLoader;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import com.sun.j3d.loaders.lw3d.LWOBFileReader;
import java.io.FileNotFoundException;
import com.sun.j3d.loaders.ParsingErrorException;

/**
 * This class is responsible for parsing the binary data in an Object file
 * that describes a texture for a particular surface and turning that data
 * into Java3D texture data. If the texture is coming from a file (which
 * is the only type of texture handled by the loader currently; other
 * types of texture definitions are ignored), then this class instantiates
 * a TargaReader object to read the data in that file. Once all of the
 * data has been read, the class creates a Java3D Texture object by first
 * scaling the image using the ImageScaler class (since all textures must
 * have width/height = power of 2; Note: this functionality is now built
 * into the TextureLoader class, so it could be removed from this loader)
 * and then creating a Texture with that image.
 */

class LwoTexture extends ParserObject {
	
    LWOBFileReader theReader;
    int red = 255, green = 255, blue = 255;
    Color3f color, diffuseColor, specularColor, emissiveColor;
    Image theImage = null;
    String imageFile = null;
    Vector3f textureSize = new Vector3f(1f, 1f, 1f);;
    Vector3f textureCenter = new Vector3f(0f, 0f, 0f);
    int textureAxis;
    int flags = 0;
    String type;
    String mappingType;
    String nextToken = null;
    static Hashtable imageTable = new Hashtable();
    static Hashtable textureTable = new Hashtable();

	/** 
	* Constructor: calls readTexture() to parse the file and retrieve
	* texture parameters
	*/
    LwoTexture(LWOBFileReader reader, int length, String typename, 
	       int debugVals) throws FileNotFoundException {
	super(debugVals);
	debugOutputLn(TRACE, "Constructor");
	theReader = reader;
	type = typename;
	readTexture(length);
    }
		
    String getNextToken() {
        return nextToken;
    }

	/**
	* The loader currently only handles CTEX and DTEX texture types
	* (These either represent the surface color like a decal (CTEX)
	* or modify the diffuse color (DTEX)
	*/
    boolean isHandled() {
        if ((type.equals("CTEX") ||
             type.equals("DTEX")) &&
             theImage != null)
             return true;
        debugOutputLn(LINE_TRACE, "failed isHandled(), type, theImage = " +
		      type + ", " + theImage);
        return false;
    }

	/**
	* Return the actual Texture object associated with the current image.
	* If we've already created a texture for this image, return that; 
	* otherwise create a new Texture
	*/
    Texture getTexture() {
	debugOutputLn(TRACE, "getTexture()");
	if (theImage == null)
	    return null;
	Texture2D t2d = (Texture2D)textureTable.get(theImage);
	if (t2d == null) {
	    ImageScaler scaler = new ImageScaler((BufferedImage)theImage);
	    BufferedImage scaledImage = (BufferedImage)scaler.getScaledImage();
	    TextureLoader tl = new TextureLoader(scaledImage);
	    t2d = (Texture2D)tl.getTexture();
	    textureTable.put(theImage, t2d);
	}

	return t2d;
    }

    String getType() {
	return type;
    }
    
    Color3f getColor() {
	return color;
    }

    Image getImage() {
	return theImage;
    }

    Vector3f getTextureSize() {
	return textureSize;
    }

    int getTextureAxis() {
	return textureAxis;
    }
    
    Vector3f getTextureCenter() {
	return textureCenter;
    }

    String getMappingType() {
	return mappingType;
    }
   
	/**
	* Parse the binary file to retrieve all texture parameters for this
	* surface.  If/when we encounter a TIMG parameter, which contains the
	* filename of an image, then create a new TargaReader object to 
	* read that image file
	*/ 
    void readTexture(int length)
	throws FileNotFoundException, ParsingErrorException {

	debugOutputLn(TRACE, "readTexture()");
	
	int surfStopMarker = theReader.getMarker() + length;
	mappingType = theReader.getString();
	debugOutputLn(VALUES, "mappingType = " + mappingType);
	String tokenString = theReader.getToken();
	while (!(tokenString == null) && theReader.getMarker() < surfStopMarker) {
	    
	    debugOutputLn(VALUES, "  tokenString = " + tokenString);
	    debugOutputLn(VALUES, "  marker, stop = " + theReader.getMarker() + ", " + surfStopMarker);
	    
	    if (tokenString.endsWith("TEX") ||
		(!tokenString.startsWith("T") || tokenString.equals("TRAN"))) {
		nextToken = tokenString;
		return;
	    }

	    int fieldLength = theReader.getShortInt();
	    debugOutputLn(VALUES, "  fl = " + fieldLength);

	    if (tokenString.equals("TFLG")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		flags = theReader.getShortInt();
		textureAxis = flags & 0x07;
		debugOutputLn(WARNING, "val = " + flags);
	    }
	    else if (tokenString.equals("TCLR")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		try {
		    red = theReader.read();
		    green = theReader.read();
		    blue = theReader.read();
		    theReader.read();
		}
		catch (IOException e) {
		    throw new ParsingErrorException(e.getMessage());
		}
		debugOutputLn(WARNING, "val = " + red + ", " + green +
			      ", " + blue);
	    }
	    else if (tokenString.equals("TIMG")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		imageFile = theReader.getString();
		debugOutputLn(VALUES, "imageFile = " + imageFile);
		if (imageFile.indexOf("none") == -1) {
		    if ((theImage =
			 (Image)imageTable.get(imageFile)) == null) {
			try {
			    TargaReader tr =
				new TargaReader(imageFile,
						debugPrinter.getValidOutput());
			    theImage = tr.getImage();
			    imageTable.put(imageFile, theImage);
			}
			catch (FileNotFoundException e) {
			    // Ignore texture if can't find it
			    debugOutputLn(WARNING, "Image File skipped: " +
				imageFile);
			}
		    }
		}
		debugOutputLn(WARNING, "val = __" + imageFile + "__");
	    }
	    else if (tokenString.equals("TWRP")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		int widthWrap = theReader.getShortInt();
		int heightWrap = theReader.getShortInt();
		debugOutputLn(WARNING, "val = " + widthWrap + ", " +
			      heightWrap);
	    }
	    else if (tokenString.equals("TCTR")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		textureCenter.x = theReader.getFloat();
		textureCenter.y = theReader.getFloat();
		textureCenter.z = theReader.getFloat();
		debugOutputLn(WARNING, "val = " + textureCenter);
	    }
	    else if (tokenString.equals("TSIZ")) {
		debugOutputLn(WARNING, "Not yet handling: " + tokenString);
		textureSize.x = theReader.getFloat();
		textureSize.y = theReader.getFloat();
		textureSize.z = theReader.getFloat();
		debugOutputLn(WARNING, "val = " + textureSize);
	    }
	    else {
		debugOutputLn(WARNING,
			      "unrecognized token: " + tokenString);
		theReader.skipLength(fieldLength);
	    }
	    if (theReader.getMarker() < surfStopMarker) {
		tokenString = theReader.getToken();
	    }
	}
    }
}

