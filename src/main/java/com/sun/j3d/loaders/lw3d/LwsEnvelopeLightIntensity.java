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
import javax.media.j3d.TransformGroup;


/**
 * This class creates a LightIntensityPathInterpolator object from the
 * keyframe-based envelope specified in a Scene file.
 */

class LwsEnvelopeLightIntensity extends LwsEnvelope {


    /**
     * Constructor: Calls superclass, which will parse the stream
     * and store the envelope data
     */    
    LwsEnvelopeLightIntensity(StreamTokenizer st,
			      int frames, float time) {
	super(st, frames, time);
    }

    /**
     * Creates Java3d behaviors given the stored envelope data.  The 
     * Behavior created is a LightIntensityPathInterpolator
     */
    void createJava3dBehaviors(Object target) {
	if (numFrames <= 1)
	    behaviors = null;
	else {
	    long alphaAtOne = 0;
	    int loopCount;
	    if (loop)
		loopCount = -1;
	    else
		loopCount = 1;
		// Note: hardcoded to always loop...
	    loopCount = -1;
	    debugOutputLn(VALUES, "totalTime = " + totalTime);
	    debugOutputLn(VALUES, "loopCount = " + loopCount);
	    float animTime = 1000.0f * totalTime *
		(float)(frames[numFrames-1].getFrameNum()/(float)totalFrames);
	    debugOutputLn(VALUES, " anim time: " + animTime);
	    debugOutputLn(VALUES, " totalFrames = " + totalFrames);
	    debugOutputLn(VALUES, " lastFrame = " +
			  frames[numFrames-1].getFrameNum());
	    if (!loop)
		alphaAtOne = (long)(1000.0*totalTime - animTime);
	    Alpha theAlpha =
		new Alpha(loopCount, Alpha.INCREASING_ENABLE,
			  0, 0, (long)animTime, 0,
			  alphaAtOne, 0, 0, 0);
	    float knots[] = new float[numFrames];
	    float values[] = new float[numFrames];
	    for (int i=0; i < numFrames; ++i) {
		values[i] = (float)frames[i].getValue();
		knots[i] = (float)(frames[i].getFrameNum())/
		    (float)(frames[numFrames-1].getFrameNum());
		debugOutputLn(VALUES, "value, knot = " +
				   values[i] + ", " + knots[i]);
	    }
	    LightIntensityPathInterpolator l = new
		LightIntensityPathInterpolator(theAlpha,
					       knots,
					       values,
					       target);
            if (l != null) {
	      behaviors = l;
	      BoundingSphere bounds =
	  	  new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000000.0);
	      behaviors.setSchedulingBounds(bounds);
              ((TransformGroup)target).setCapability
                                (TransformGroup.ALLOW_TRANSFORM_WRITE);
              ((TransformGroup)target).addChild(behaviors);
            }
	}
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
