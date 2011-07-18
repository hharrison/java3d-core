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



import java.io.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.Enumeration;
import com.sun.j3d.loaders.ParsingErrorException;


/**
 * This class creates a Background object (solid color only, no geometry)
 * according to some of the data stored in a Scene file. Note: Lightwave
 * defines much more complex backgrounds that the loader currently
 * handles. It should be possible to se Background Geometry to handle
 * most of these cases, if there's time and will to work on the problem.
 */

class LwsBackground extends TextfileParser {

    // data from the file
    int solidBackdrop;
    Color3f color, zenithColor, skyColor, groundColor, nadirColor;
    Background backgroundObject = null;


    /**
     * Constructor: parses stream and retrieves all Background-related data
     */    
    LwsBackground(StreamTokenizer st, int debugVals)
	throws ParsingErrorException {

	debugPrinter.setValidOutput(debugVals);
	debugOutput(TRACE, "LwsBackground()");
	color = new Color3f(0f, 0f, 0f);
	zenithColor = new Color3f(0f, 0f, 0f);
	skyColor = new Color3f(0f, 0f, 0f);
	groundColor = new Color3f(0f, 0f, 0f);
	nadirColor = new Color3f(0f, 0f, 0f);
	
	solidBackdrop = (int)getNumber(st);
	while (!isCurrentToken(st, "FogType")) {
	    debugOutputLn(LINE_TRACE, "currentToken = " + st.sval);
	    
	    if (isCurrentToken(st, "BackdropColor")) {
		color.x = (float)getNumber(st)/255f;
		color.y = (float)getNumber(st)/255f;
		color.z = (float)getNumber(st)/255f;
	    }
	    else if (isCurrentToken(st, "NadirColor")) {
		nadirColor.x = (float)getNumber(st)/255f;
		nadirColor.y = (float)getNumber(st)/255f;
		nadirColor.z = (float)getNumber(st)/255f;
	    }
	    else if (isCurrentToken(st, "SkyColor")) {
		skyColor.x = (float)getNumber(st)/255f;
		skyColor.y = (float)getNumber(st)/255f;
		skyColor.z = (float)getNumber(st)/255f;
	    }
	    else if (isCurrentToken(st, "GroundColor")) {
		groundColor.x = (float)getNumber(st)/255f;
		groundColor.y = (float)getNumber(st)/255f;
		groundColor.z = (float)getNumber(st)/255f;
	    }
	    else if (isCurrentToken(st, "NadirColor")) {
		nadirColor.x = (float)getNumber(st)/255f;
		nadirColor.y = (float)getNumber(st)/255f;
		nadirColor.z = (float)getNumber(st)/255f;
	    }
	    try {
		st.nextToken();
	    }
	    catch (IOException e) {
		throw new ParsingErrorException(e.getMessage());
	    }
	}
	st.pushBack();   // push token back on stack
    }

    /**
     * Creates Java3d objects from the background data.  Note that there
     * are plenty of lw3d background attributes that the loader currently
     * ignores.  Some of these may best be handled by creating background
     * geometry rather than a solid background color
     */
    void createJava3dObject() {
	// TODO:  there are various attributes of 
	// backdrops that we're not currently handling.  In 
	// particular, if the file calls for a gradient background
	// (solidBackdrop == 0), we ignore the request and just
	// create a solid background from the sky color instead.
	// We should be able to do something with the
	// various colors specified, perhaps by creating
	// background geometry with the appropriate vertex
	// colors?

	if (solidBackdrop != 0) {
	    backgroundObject = new Background(color);
	    debugOutput(VALUES, "Background color = " + color);
	}
	else {
	    backgroundObject = new Background(skyColor);
	    debugOutput(VALUES, "Background color = " + skyColor);
	}
	BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100000.0);
	backgroundObject.setApplicationBounds(bounds);
    }

    Background getObjectNode() {
	return backgroundObject;
    }

    void printVals() {
	debugOutputLn(VALUES, "  BACKGROUND vals: ");
    }

    
}	
