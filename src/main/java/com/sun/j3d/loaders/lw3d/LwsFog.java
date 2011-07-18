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
 * This class creates a Fog object from the data in a Scene file.
 */

class LwsFog extends TextfileParser {

    // data from the file
    float minDist, maxDist, minAmount, maxAmount;
    int backdropFog;
    Color3f color;
    int type;
    Fog fogObject = null;

    /**
     * Constructor: parses stream and stores fog data
     */ 
    LwsFog(StreamTokenizer st, int debugVals) throws ParsingErrorException {
	debugPrinter.setValidOutput(debugVals);
	debugOutput(TRACE, "LwsFog()");
	color = new Color3f(0f, 0f, 0f);
	
	while (!isCurrentToken(st, "DitherIntensity")) {
	    debugOutputLn(LINE_TRACE, "currentToken = " + st.sval);
	    
	    if (isCurrentToken(st, "FogMinDist")) {
		minDist = (float)getNumber(st);
	    }
	    else if (isCurrentToken(st, "FogMaxDist")) {
		maxDist = (float)getNumber(st);
	    }
	    else if (isCurrentToken(st, "FogMinAmount")) {
		minAmount = (float)getNumber(st);
	    }
	    else if (isCurrentToken(st, "FogMaxAmount")) {
		maxAmount = (float)getNumber(st);
	    }
	    else if (isCurrentToken(st, "BackdropFog")) {
		backdropFog = (int)getNumber(st);
	    }
	    else if (isCurrentToken(st, "FogColor")) {
		color.x = (float)getNumber(st)/255f;
		color.y = (float)getNumber(st)/255f;
		color.z = (float)getNumber(st)/255f;
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
     * Creates Java3d Fog object given the fog parameters in the file.
     * Note that various fog parameters in lw3d are not currently handled.
     */
    void createJava3dObject() {
	// TODO:  there are various attributes of lw fog that
	// we're not currently handing, including non-linear fog
	// (need to understand the two different types - these may
	// map onto java3d's expontential fog node), non-solid
	// backdrop colors (how to handle this?), min/max amount
	// (j3d only handles 0 -> 1 case)

	fogObject = new LinearFog(color, minDist, maxDist);
	debugOutputLn(VALUES,
		      "just set linearFog with color, minDist, maxDist = " +
		      color + ", " +
		      minDist + ", " +
		      maxDist);
	BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100000.0);
	fogObject.setInfluencingBounds(bounds);
    }

    Fog getObjectNode()
    {
	return fogObject;
    }

    void printVals()
    {
	debugOutputLn(VALUES, "  FOG vals: ");
    }

    
}	
