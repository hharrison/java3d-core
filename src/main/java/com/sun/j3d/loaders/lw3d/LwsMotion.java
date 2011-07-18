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
import java.util.Enumeration;
import java.util.Vector;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.behaviors.interpolators.*;
import com.sun.j3d.internal.J3dUtilsI18N;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.IncorrectFormatException;

/**
 * This class is responsible for parsing the data in a Scene file related to
 * an object's animation and constructing the appropriate Java3D
 * Behavior objects. For each keyframe defined for the animation in the
 * Lightwave file, this class creates a LwsFrame object to parse that
 * keyframe data and create the appropriate data structures. Then for
 * each of those LwsFrame objects created, LwsMotion creates a knot
 * value for a PathInterpolator and fills in the appropriate field. Finally,
 * the class creates a RotPosScalePathInterpolator with all of the data
 * from the animation. There are also some utility functions in this
 * class for dealing with special cases of animations, such as animations
 * that begin after the first frame of the scene and animations that
 * define frames in a way that Java3D cannot easily interpret.
 */

class LwsMotion extends TextfileParser {

    // data from the file
    String motionName;
    LwsFrame frames[];
    int numFrames;
    int numChannels;
    boolean loop;
    float totalTime;
    int firstFrame;
    int totalFrames;
    Behavior behaviors;

    /**
     * Constructor
     */
    LwsMotion(StreamTokenizer st, int frames, float time) {
        this(st, 0, frames, time, EXCEPTION);

    }

    /**
     * Constructor: takes tokenizer, 1st frame of this animation, total
     * number of frames, total time of animation, and the debug settings
     */
    LwsMotion(StreamTokenizer st, int firstFrame,
		     int frames, float time, int debugVals)
	throws ParsingErrorException, IncorrectFormatException {

        debugPrinter.setValidOutput(debugVals);
	numFrames = 0;
	totalTime = time;
	this.firstFrame = firstFrame;
	totalFrames = frames;
	debugOutputLn(LINE_TRACE, "about to get motion name");
	motionName = getName(st);
	debugOutputLn(LINE_TRACE, "about to get motion");
	getMotion(st);
    }

    /**
     * This method parses the tokenizer and creates the data structures
     * that hold the data from that file.  For each separate keyframe,
     * this method calls LwsFrame to parse and interpret that data.
     */
    void getMotion(StreamTokenizer st)
	throws ParsingErrorException, IncorrectFormatException
    {
	debugOutputLn(TRACE, "getMotion()");
	numChannels = (int)getNumber(st);
	if (numChannels != 9) {
	    throw new IncorrectFormatException(
		J3dUtilsI18N.getString("LwsMotion0"));
	}
	debugOutputLn(LINE_TRACE, "got channels");

	numFrames = (int)getNumber(st);
	frames = new LwsFrame[numFrames];
	debugOutputLn(VALUES, "got frames" + numFrames);

	for (int i = 0; i < numFrames; ++i) {
	    frames[i] = new LwsFrame(st);
	}

	debugOutput(LINE_TRACE, "got all frames");

	getAndCheckString(st, "EndBehavior");
	int repeatVal = (int)getNumber(st);
	if (repeatVal == 1)
	    loop = false;
	else
	    loop = true;

	// need to make sure frame info is in sycn with j3d
  	// fixFrames();
    }

    /**
      * The previous version of this method looked for sucessive frames with
      * the same rotation value (mod 2PI).  If it found such frames, it would
      * divide that interval into 4 separate frames.
      * This fix is not sufficient for various rotation cases, though.  For
      * instance, if the rotation difference between two frames is more than
      * 2PI, the rotation will not case a flag to be fixed and the resulting
      * rotation will only be between the delta of the two rotations, mod 2PI.
      * The real fix should behave as follows:
      * - Iterate through all sucessive frames
      * - For any two frames that have rotation components that differ by more
      * than PI/2 (one quarter rotation - no reason for this, but let's pick a
      * small value to give our resulting path interpolations a better chance
      * of behaving correctly), figure out how many frames we need to create to
      * get increments of <= PI/2 between each frame.  
      * - Create these new frames
      * - Set the odl frames pointer to the new frames structures.
      */

    void fixFrames() {

	boolean  addedFrames   = false;
	Vector   newFramesList = new Vector();
	double   halfPI        = (float)(Math.PI/2);
	LwsFrame finalFrame    = null;

	for (int i = 1 ; i < numFrames; ++i) {
           LwsFrame prevFrame;
	   LwsFrame lastFrame = frames[i-1];
	   LwsFrame thisFrame = frames[i];
           LwsFrame nextFrame;

	   finalFrame = thisFrame;
	   newFramesList.add(lastFrame);

	   double largestAngleDifference = 0;
	   double thisAngle = thisFrame.getHeading();
	   double lastAngle = lastFrame.getHeading();
	   double angleDifference = Math.abs(thisAngle - lastAngle);
	   if (angleDifference > largestAngleDifference) 
	     largestAngleDifference = angleDifference;

	   thisAngle = thisFrame.getPitch();
	   lastAngle = lastFrame.getPitch();
	   angleDifference = Math.abs(thisAngle - lastAngle);
	   if (angleDifference > largestAngleDifference) 
	     largestAngleDifference = angleDifference;

	   thisAngle = thisFrame.getBank();
	   lastAngle = lastFrame.getBank();
	   angleDifference = Math.abs(thisAngle - lastAngle);
	   if (angleDifference > largestAngleDifference) 
	     largestAngleDifference = angleDifference;

	   if (largestAngleDifference > halfPI) {
		// Angles too big - create new frames 
		addedFrames = true;
		int numNewFrames = (int)(largestAngleDifference/halfPI);
		double increment = 1.0/(double)(numNewFrames+1);
		double currentRatio = increment;

                double totalf = frames[numFrames-1].getFrameNum();
                double tlength = (thisFrame.getFrameNum() - 
                                            lastFrame.getFrameNum())/totalf;
                double adj0; 
                double adj1; 

                // get the previous and next frames 
                if ((i-1) < 1) { 
                   prevFrame = frames[i-1];
                   adj0 = 0.0;
                } else {
                   prevFrame = frames[i-2];
                   adj0 = tlength/((thisFrame.getFrameNum() - 
                                         prevFrame.getFrameNum())/totalf); 
                }

                if ((i+1) < numFrames) {
                   nextFrame = frames[i+1];
                   adj1 = tlength/((nextFrame.getFrameNum()- 
                                         lastFrame.getFrameNum())/totalf); 
                 } else {
                   nextFrame = frames[i];
                   adj1 = 1.0;
                 }

		for (int j = 0; j < numNewFrames; ++j) {

                   LwsFrame newFrame;

                   // if linear interpolation
                   if (thisFrame.linearValue == 1) {
  	              newFrame = new LwsFrame(lastFrame, 
                                              thisFrame, currentRatio);
			         
                    // if spline interpolation
                    } else {
		      newFrame = new LwsFrame(prevFrame, lastFrame, 
                                              thisFrame, nextFrame, 
                                              currentRatio, adj0, adj1);
                    }
 
		    currentRatio += increment;
		    newFramesList.add(newFrame);
		}
	   }
	}

	// Now add in final frame
	if (finalFrame != null)
		newFramesList.add(finalFrame);
	if (addedFrames) {

		// Recreate frames array from newFramesList
		LwsFrame newFrames[] = new LwsFrame[newFramesList.size()];
		Enumeration elements = newFramesList.elements();
		int index = 0;
		while (elements.hasMoreElements()) {
			newFrames[index++] = (LwsFrame)elements.nextElement();
		}
		frames = newFrames;
		numFrames = frames.length;
		for (int i = 0; i < numFrames; ++i) {
		   debugOutputLn(VALUES, "frame " + i + " = " + frames[i]);
		   frames[i].printVals();
		}
	}
    }

    /**
     * Utility for getting integer mod value
     */
    int intMod(int divisee, int divisor) {
	int tmpDiv = divisee;
	int tmpDivisor = divisor;
	if (tmpDiv < 0)
	    tmpDiv = -tmpDiv;
	if (tmpDivisor < 0)
	    tmpDivisor = -tmpDivisor;
	while (tmpDiv > tmpDivisor) {
	    tmpDiv -= tmpDivisor;
	}
	return tmpDiv;
    }

    /**
     * Class that associates a particular frame with its effective frame
     * number (which accounts for animations that start after frame 1)
     */
    class FrameHolder {
	double frameNumber;
	LwsFrame frame;

	FrameHolder(LwsFrame theFrame, double number) {
	    frame = theFrame;
	    frameNumber = number;
	}
    }
    

    /**
     * This method was added to account for animations that start after
     * the first frame (e.g., Juggler.lws starts at frame 30).  We need
     * to alter some of the information for the frames in this "frame subset"
     */
    void playWithFrameTimes(Vector framesVector) {
	debugOutputLn(TRACE, "playWithFrameTimes: firstFrame = " +
		      firstFrame);
	if (firstFrame == 1) {
	    return;
	}
	else if (frames[numFrames-1].getFrameNum() < totalFrames) {
	    // First, create a vector that holds all LwsFrame's in frame
	    // increasing order (where order is started at firstFrame Modulo
	    // this motion's last frame
	    int motionLastFrame =
		(int)(frames[numFrames-1].getFrameNum() + .4999999);
	    int newFirstFrame = intMod(firstFrame, motionLastFrame);
	    int newLastFrame = intMod(totalFrames, motionLastFrame);
	    int index = 0;
	    while (index < numFrames) {
		if (frames[index].getFrameNum() >= newFirstFrame)
		    break;
		++index;
	    }
	    int startIndex = index;
	    if (frames[startIndex].getFrameNum() > firstFrame &&
		startIndex > 0)
		startIndex--;  // Actually, should interpolate
	    index = startIndex;
	    if (newFirstFrame < newLastFrame) {
		while (index < numFrames &&
		       frames[index].getFrameNum() <= newLastFrame) {
		    FrameHolder frameHolder =
			new FrameHolder(frames[index],
					frames[index].getFrameNum() -
					newFirstFrame);
		    framesVector.addElement(frameHolder);
		    ++index;
		}
	    }
	    else {
		double currentNewFrameNumber = -1.0;
		while (index < numFrames) {
		    currentNewFrameNumber = frames[index].getFrameNum() -
			newFirstFrame;
		    FrameHolder frameHolder =
			new FrameHolder(frames[index],
					currentNewFrameNumber);
		    framesVector.addElement(frameHolder);
		    ++index;
		}
		index = 0;
		while (index <= startIndex &&
		       frames[index].getFrameNum() <= newLastFrame) {
		    if (index == 0) {
			LwsFrame newFrame =
			    new LwsFrame(frames[index],
					 frames[index+1],
					 1.0/(frames[index+1].getFrameNum() -
					     frames[index].getFrameNum()));
			FrameHolder frameHolder =
			    new FrameHolder(newFrame,
					    newFrame.getFrameNum() +
					    currentNewFrameNumber);
			framesVector.addElement(frameHolder);
		    }
		    else {
			FrameHolder frameHolder =
			    new FrameHolder(frames[index],
					    frames[index].getFrameNum() +
					    currentNewFrameNumber);
			framesVector.addElement(frameHolder);
		    }
		    ++index;
		}
	    }
	}
	else {
	    int index = 0;
	    while (index < numFrames) {
		if (frames[index].getFrameNum() >= firstFrame)
		    break;
		++index;
	    }
	    int startIndex = index;
	    if (frames[startIndex].getFrameNum() > firstFrame &&
		startIndex > 0) {
		// Interpolate to first frame
		double ratio = (double)firstFrame /
		    (frames[startIndex].getFrameNum() -
		     frames[startIndex-1].getFrameNum());
		LwsFrame newFrame = new LwsFrame(frames[startIndex-1],
						 frames[startIndex],
						 ratio);
		FrameHolder frameHolder =
		    new FrameHolder(newFrame, newFrame.getFrameNum() -
				    firstFrame);
		framesVector.addElement(frameHolder);
	    }
	    index = startIndex;
	    while (index < numFrames &&
		   frames[index].getFrameNum() <= totalFrames) {
		FrameHolder frameHolder =
		    new FrameHolder(frames[index],
				    frames[index].getFrameNum() -
				    firstFrame);
		framesVector.addElement(frameHolder);
		++index;
	    }
	    if (frames[index-1].getFrameNum() < totalFrames) {
		// Interpolate to last frame
		double ratio = (double)(totalFrames -
					frames[index-1].getFrameNum()) /
		    (frames[index].getFrameNum() -
		     frames[index-1].getFrameNum());
		LwsFrame newFrame = new LwsFrame(frames[index-1],
						 frames[index],
						 ratio);
		FrameHolder frameHolder =
		    new FrameHolder(newFrame, totalFrames - firstFrame);
		framesVector.addElement(frameHolder);
	    }
	}
    }

    /**
     * Normally, we just create j3d behaviors from the frames.  But if the
     * animation's first frame is after frame number one, then we have to
     * shuffle things around to account for playing/looping on this subset
     * of the total frames of the animation
     */
    void createJava3dBehaviorsForFramesSubset(TransformGroup target) {

	debugOutputLn(TRACE, "createJava3dBehaviorsForFramesSubset");
	Vector frameHolders = new Vector();
	playWithFrameTimes(frameHolders);
	long alphaAtOne = 0;

        // determine looping 
	int loopCount;
	if (loop)
	    loopCount = -1;
	else
	    loopCount = 1;
	loopCount = -1;

	int numFrames = frameHolders.size();

	debugOutputLn(VALUES, "totalTime = " + totalTime);
	debugOutputLn(VALUES, "loopCount = " + loopCount);

	FrameHolder lastFrameHolder =
	    (FrameHolder)frameHolders.elementAt(frameHolders.size() - 1);
	LwsFrame lastFrame = lastFrameHolder.frame;
	float animTime = 1000.0f * totalTime *
	    (float)(lastFrameHolder.frameNumber/(float)(totalFrames -
							   firstFrame));
	debugOutputLn(VALUES, " anim time: " + animTime);
	debugOutputLn(VALUES, " totalFrames = " + totalFrames);

	if (!loop)
	    alphaAtOne = (long)(1000.0*totalTime - animTime);
	Alpha theAlpha =
	    new Alpha(loopCount, Alpha.INCREASING_ENABLE,
		      0, 0, (long)animTime, 0,
		      alphaAtOne, 0, 0, 0);

	float knots[]           = new float[numFrames];
	Point3f[] positions     = new Point3f[numFrames];
	Quat4f[] quats          = new Quat4f[numFrames];
        Point3f[] scales        = new Point3f[numFrames];
	Transform3D yAxis       = new Transform3D();
	Matrix4d mat            = new Matrix4d(); 
        KBKeyFrame[] keyFrames  = new KBKeyFrame[numFrames];

	for (int i=0; i < numFrames; ++i) {

	    FrameHolder frameHolder = (FrameHolder)frameHolders.elementAt(i);
	    LwsFrame frame = frameHolder.frame;

            // copy position
	    positions[i] = frame.getPosition();
 
            // copy scale
	    // Used to hardcode no-scale:   scales[i] = 1.0f, 1.0f, 1.0f;
            // Note that we can't do non-uniform scaling in the current Path
            // interpolators. The interpolator just uses the x scale.
            // getScale makes sure that we don't have any zero scale component
            scales[i] = frame.getScale();

            // copy rotation information
	    frame.setRotationMatrix(mat);
	    debugOutputLn(VALUES, "LwsMotion::createj3dbeh, mat = " + mat);
	    quats[i] = new Quat4f();
	    quats[i].set(mat);
	    debugOutputLn(VALUES, " and quat = " + quats[i]);

            // calculate knot points from frame numbers 
	    if (i == 0)
	      knots[i] = 0.0f;
	    else	
	      knots[i] = (float)(frameHolder.frameNumber)/
				(float)(lastFrameHolder.frameNumber);

             // Create KB key frames
             keyFrames[i] = new KBKeyFrame(knots[i], frame.linearValue,  
                                                     positions[i], 
                                                     (float)frame.heading,
                                                     (float)frame.pitch,
                                                     (float)frame.bank,
                                                     scales[i],
                                                     (float)frame.tension,
                                                     (float)frame.continuity,
                                                     (float)frame.bias);

	    debugOutputLn(VALUES, "pos, knots, quat = " +
			  positions[i] + knots[i] + quats[i]);
	}

        // Pass the KeyFrames to the interpolator an let it do its thing
        KBRotPosScaleSplinePathInterpolator b = new
                   KBRotPosScaleSplinePathInterpolator(theAlpha,
					               target,
					               yAxis, 
                                                       keyFrames);
        if (b != null) {
	  behaviors = b;
	  BoundingSphere bounds =
	              new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000000.0);
	  b.setSchedulingBounds(bounds);
          target.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	  target.addChild(behaviors);
        }
    }
    
    /**
     * Create j3d behaviors for the data stored in this animation.  This is
     * done by creating a RotPosScalePathInterpolator object that contains
     * all of the position, orientation, scale data for each keyframe.
     */
    void createJava3dBehaviors(TransformGroup target) {

	if (numFrames <= 1)
	    behaviors = null;
	else {
	    if (firstFrame > 1) {
		createJava3dBehaviorsForFramesSubset(target);
		return;
	    }

	    long alphaAtOne = 0;

            // determine looping
	    int loopCount;
	    if (loop)
		loopCount = -1;
	    else
		loopCount = 1;
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

	    float knots[]           = new float[numFrames];
	    Point3f[] positions     = new Point3f[numFrames];
	    Quat4f[] quats          = new Quat4f[numFrames];
            Point3f[] scales        = new Point3f[numFrames];
	    Transform3D yAxis       = new Transform3D();
	    Matrix4d mat            = new Matrix4d();
            KBKeyFrame[] keyFrames  = new KBKeyFrame[numFrames]; 

	    for (int i=0; i < numFrames; ++i) {

                // copy position
		positions[i] = frames[i].getPosition();

                // copy scale
                // Used to hardcode no-scale:   scales[i] = 1.0f, 1.0f, 1.0f;
                // Note that we can't do non-uniform scaling in the current Path
                // interpolators. The interpolator just uses the x scale.
                // getScale makes sure that we don't have any 0 scale component
                scales[i] = frames[i].getScale();

                // copy rotation information
		frames[i].setRotationMatrix(mat);
		debugOutputLn(VALUES, "LwsMotion::createj3dbeh, mat = " + mat);
		quats[i] = new Quat4f();
		quats[i].set(mat);
		debugOutputLn(VALUES, " and quat = " + quats[i]);
		
                // calculate knot points from frame numbers
		if (i == 0)
		   knots[i] = 0.0f;
		else
		   knots[i] = (float)(frames[i].getFrameNum())/
		   	         (float)(frames[numFrames-1].getFrameNum());

                // Create KB key frames
                keyFrames[i] = new KBKeyFrame(knots[i],frames[i].linearValue,
                                              positions[i], 
                                              (float)frames[i].heading,
                                              (float)frames[i].pitch,
                                              (float)frames[i].bank,
                                              scales[i],
                                              (float)frames[i].tension,
                                              (float)frames[i].continuity,
                                              (float)frames[i].bias);
 

		debugOutputLn(VALUES, "pos, knots, quat = " +
				   positions[i] + knots[i] + quats[i]);
	    }

            // Pass the KeyFrames to the interpolator an let it do its thing
            KBRotPosScaleSplinePathInterpolator b = new
                   KBRotPosScaleSplinePathInterpolator(theAlpha,
					               target,
					               yAxis, 
                                                       keyFrames);
            if (b != null) {
	      behaviors = b;
	      BoundingSphere bounds =
	            new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000000.0);
	      b.setSchedulingBounds(bounds);
              target.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	      target.addChild(behaviors);
            }
        }

    }

    /**
     * Returns the Behavior object created for this animation
     */
    Behavior getBehaviors() {
	return behaviors;
    }
    
    /**
     * Returns the first LwsFrame object (which contains the initial
     * setup for a given object)
     */
    LwsFrame getFirstFrame() {
	if (numFrames > 0)
	    return frames[0];
	else
	    return null;
    }

    /**
     * Utility function for printing values
     */
    void printVals() {
	debugOutputLn(VALUES, "   motionName = " + motionName);
	debugOutputLn(VALUES, "   numChannels = " + numChannels);
	debugOutputLn(VALUES, "   numFrames = " + numFrames);
	debugOutputLn(VALUES, "   loop = " + loop);
	for (int i = 0; i < numFrames; ++i) {
	    debugOutputLn(VALUES, "       FRAME " + i);
	    frames[i].printVals();
	}
    }

}	
