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
import com.sun.j3d.internal.J3dUtilsI18N;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.IncorrectFormatException;

/**
 * This class is a superclass for any implementation of envelopes; the
 * only subclass currently is LwsEnvelopeLightIntensity. LwsEnvelope
 * parses the data in a Scene file and extracts the envelope data.
 */

class LwsEnvelope extends TextfileParser {

    // data from the file
    String name;
    LwsEnvelopeFrame frames[];
    int numFrames;
    int numChannels;
    boolean loop;
    float totalTime;
    int totalFrames;
    Behavior behaviors;

    /**
     * Constructor: calls getEnvelope() to parse the stream for the
     * envelope data
     */    
    LwsEnvelope(StreamTokenizer st, int frames, float time) {
	numFrames = 0;
	totalTime = time;
	totalFrames = frames;
	name = getName(st);
	getEnvelope(st);
    }

	/**
	* Parses the stream to retrieve all envelope data.  Creates
	* LwsEnvelopeFrame objects for each keyframe of the envelope 
	* (these frames differ slightly from LwsFrame objects because
	* envelopes contain slightly different data)
	*/
    void getEnvelope(StreamTokenizer st)
	throws IncorrectFormatException, ParsingErrorException
    {
	debugOutputLn(TRACE, "getEnvelope()");
	numChannels = (int)getNumber(st);
	if (numChannels != 1) {
	    throw new IncorrectFormatException(
		J3dUtilsI18N.getString("LwsEnvelope0"));
	}
	debugOutputLn(LINE_TRACE, "got channels");

	numFrames = (int)getNumber(st);
	frames = new LwsEnvelopeFrame[numFrames];
	debugOutputLn(VALUES, "got frames" + numFrames);

	for (int i = 0; i < numFrames; ++i) {
	    frames[i] = new LwsEnvelopeFrame(st);
	}
	debugOutput(LINE_TRACE, "got all frames");

        try {
	    st.nextToken();
	    while (!isCurrentToken(st, "EndBehavior")) {
		// There is an undocumented "FrameOffset" in some files
		st.nextToken();
	    }
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
	int repeatVal = (int)getNumber(st);
	if (repeatVal == 1)
	    loop = false;
	else
	    loop = true;
    }

	/**
	* This superclass does nothing here - if the loader understands
	* how to deal with a particular type of envelope, it will use
	* a subclass of LwsEnvelope that will override this method
	*/
    void createJava3dBehaviors(TransformGroup target) {
	behaviors = null;
    }

    Behavior getBehaviors() {
	return behaviors;
    }
    
    
    LwsEnvelopeFrame getFirstFrame() {
	if (numFrames > 0)
	    return frames[0];
	else
	    return null;
    }

    
    void printVals() {
	debugOutputLn(VALUES, "   name = " + name);
	debugOutputLn(VALUES, "   numChannels = " + numChannels);
	debugOutputLn(VALUES, "   numFrames = " + numFrames);
	debugOutputLn(VALUES, "   loop = " + loop);
	for (int i = 0; i < numFrames; ++i) {
	    debugOutputLn(VALUES, "       FRAME " + i);
	    frames[i].printVals();
	}
    }

}	
