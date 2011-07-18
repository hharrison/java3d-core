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
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import com.sun.j3d.loaders.lw3d.LWOBFileReader;
import com.sun.j3d.internal.J3dUtilsI18N;
import java.io.FileNotFoundException;
import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;


/**
 * This class is responsible for retrieving the surface parameters for a
 * particular surface from a binary Object file and turning that data
 * into Java3D data. These surface parameters include
 * diffuse/specular/emissive properties, color, shininess, transparency,
 * and textures. For textures, this class instantiates a LwoTexture object
 * to parse that data and turn it into Java3D texture data.
 */

class LwoSurface extends ParserObject {
	
    LWOBFileReader theReader;
    int red = 255, green = 255, blue = 255;
    float diffuse = 0.0f, specular = 0.0f, transparency = 0.0f, luminosity = 0.0f;
    float creaseAngle = 0.0f;
    int gloss = 128;
    Color3f color, diffuseColor, specularColor, emissiveColor;
    float shininess;
    Image theImage = null;
    Vector3f textureCenter = null, textureSize = null;
    int textureAxis;
    String surfName;
    Vector textureList = new Vector();

    /**
     * Constructor that parses surface data from the binary file
     * and creates the necessary Java3d objects
     */
    LwoSurface(LWOBFileReader reader, int length, int debugVals)
	throws FileNotFoundException {

	super(debugVals);
	debugOutputLn(TRACE, "LwoSurface()");
	theReader = reader;
	getSurf(length);
	setJ3dColors();
    }
		
    /**
     * Creates Java3d color objects from the lw3d surface data
     */
    void setJ3dColors() {
	color = new Color3f((float)red/(float)255,
			    (float)green/(float)255,
			    (float)blue/(float)255);
	diffuseColor = new Color3f(diffuse*color.x,
				   diffuse*color.y,
				   diffuse*color.z);
	specularColor = new Color3f(specular*color.x,
				    specular*color.y,
				    specular*color.z);
	emissiveColor = new Color3f(luminosity*color.x,
				    luminosity*color.y,
				    luminosity*color.z);
	shininess = (float)(128.0 * ((float)gloss/1024.0));
    }

    Color3f getColor() {
	return color;
    }

    Color3f getDiffuseColor() {
	return diffuseColor;
    }

    Color3f getSpecularColor() {
	return specularColor;
    }

    Color3f getEmissiveColor() {
	return emissiveColor;
    }

    float getShininess() {
	return shininess;
    }

    float getCreaseAngle() {
	return creaseAngle;
    }

    /**
     * Returns the LwoTexture for the surface, if any is defined.  Note that
     * lw3d allows users to define multiple textures for any surface, which
     * is not possible through Java3d.  Therefore, we just grab the first
     * texture in any list of textures for a surface
     */
    LwoTexture getTexture() {
        debugOutputLn(TRACE, "getTexture()");
        try {
            if (textureList.isEmpty()) {
                return null;
            }
            else {
                return (LwoTexture)textureList.elementAt(0);
            }
        }
        catch (ArrayIndexOutOfBoundsException e) {
            debugOutputLn(EXCEPTION, 
                "getTexture(), exception returning first element: " + e);
            return null;
        }
    }

    String getSurfName() {
	return surfName;
    }

    float getTransparency() {
	return transparency;
    }

    /**
     * Parses the binary file and gets all data for this surface
     */
    void getSurf(int length)
	throws FileNotFoundException, IncorrectFormatException,
	ParsingErrorException {

	debugOutputLn(TRACE, "getSurf()");

	// These "got*" booleans are to help use read the best version of
	// the data - the float values for these parameters should take
	// precedence over the other format
	boolean gotLuminosityFloat = false;
	boolean gotTransparencyFloat = false;
	boolean gotDiffuseFloat = false;
	boolean gotSpecularFloat = false;
	int surfStopMarker = theReader.getMarker() + length;
	surfName = theReader.getString();
	String tokenString = theReader.getToken();
	while (!(tokenString == null) &&
	       theReader.getMarker() < surfStopMarker) {
	    debugOutputLn(VALUES, "  tokenString = " + tokenString);
	    debugOutputLn(VALUES, "  marker, stop = " +
			  theReader.getMarker() + ", " + surfStopMarker);
	    String textureToken = null;
	    int fieldLength = theReader.getShortInt();
	    debugOutputLn(VALUES, "  fl = " + fieldLength);

	    if (tokenString.equals("COLR")) {
		debugOutputLn(LINE_TRACE, "  COLR");
		try {
		    red = theReader.read();
		    green = theReader.read();
		    blue = theReader.read();
		    theReader.read();
		}
		catch (IOException e) {
		    throw new ParsingErrorException(e.getMessage());
		}
		if (fieldLength != 4)
		    throw new IncorrectFormatException(
			J3dUtilsI18N.getString("LwoSurface0"));
	    }
	    else if (tokenString.equals("FLAG")) {
		debugOutputLn(LINE_TRACE, "  FLAG");
		theReader.skipLength(fieldLength);
	    }
	    else if (tokenString.equals("VLUM")) {
		debugOutputLn(LINE_TRACE, "  VLUM");
		luminosity = theReader.getFloat();
		gotLuminosityFloat = true;
	    }
	    else if (tokenString.equals("LUMI")) {
		debugOutputLn(LINE_TRACE, "  LUMI");
		if (gotLuminosityFloat)
		    theReader.skipLength(fieldLength);
		else
		    luminosity = (float)(theReader.getShortInt())/255;
	    }
	    else if (tokenString.equals("VDIF")) {
		debugOutputLn(LINE_TRACE, "  VDIF");
		if (fieldLength != 4)
		    throw new IncorrectFormatException("VDIF problem");
		diffuse = theReader.getFloat();
		gotDiffuseFloat = true;
		debugOutputLn(VALUES, "diff = " + diffuse);
	    }
	    else if (tokenString.equals("DIFF")) {
		debugOutputLn(LINE_TRACE, "  DIFF");
		if (gotDiffuseFloat)
		    theReader.skipLength(fieldLength);
		else
		    diffuse = (float)theReader.getShortInt()/255;
	    }
	    else if (tokenString.equals("VTRN")) {
		debugOutputLn(LINE_TRACE, "  VTRN");
		transparency = theReader.getFloat();
		gotTransparencyFloat = true;
	    }
	    else if (tokenString.equals("TRAN")) {
		debugOutputLn(LINE_TRACE, "  TRAN");
		if (gotTransparencyFloat)
		    theReader.skipLength(fieldLength);
		else
		    transparency = (float)theReader.getShortInt()/255;
	    }
	    else if (tokenString.equals("VSPC")) {
		debugOutputLn(LINE_TRACE, "  VSPC");
		specular = theReader.getFloat();
		gotSpecularFloat = true;
		debugOutputLn(VALUES, "spec = " + specular);
	    }
	    else if (tokenString.equals("SPEC")) {
		debugOutputLn(LINE_TRACE, "  SPEC");
		if (gotSpecularFloat)
		    theReader.skipLength(fieldLength);
		else {
		    if (fieldLength == 4) // Bug in some LW versions
			specular = (float)theReader.getInt()/255;
		    else
			specular = (float)theReader.getShortInt()/255;
		}
	    }
	    else if (tokenString.equals("GLOS")) {
		debugOutputLn(LINE_TRACE, "  GLOS");
		if (fieldLength == 4)
		    gloss = theReader.getInt();
		else
		    gloss = theReader.getShortInt();
	    }
	    else if (tokenString.equals("SMAN")) {
		debugOutputLn(LINE_TRACE, "  SMAN");
		creaseAngle = theReader.getFloat();
	    }
	    else if (tokenString.endsWith("TEX")) {
		// Textures are complex - hand off this bit to the
		// LwoTexture class
		LwoTexture texture = 
		    new LwoTexture(theReader,
				   surfStopMarker - theReader.getMarker(),
				   tokenString,
				   debugPrinter.getValidOutput());
		textureToken = texture.getNextToken();
		if (texture.isHandled())
		    textureList.addElement(texture);
		debugOutputLn(WARNING, "val = " + tokenString);
	    }
	    else {
		debugOutputLn(WARNING,
			      "unrecognized token: " + tokenString);
		theReader.skipLength(fieldLength);
	    }
	    if (theReader.getMarker() < surfStopMarker) {
		if (textureToken == null)
		    tokenString = theReader.getToken();
		else
		    tokenString = textureToken;
	    }
	}
    }

}

 

