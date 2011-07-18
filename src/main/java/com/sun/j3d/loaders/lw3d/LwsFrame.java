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
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Point3f;

/**
 * This class is responsible for parsing the data in a Scene file
 * associated with a single keyframe. This data includes the position,
 * orientation, and scaling information, in addition to the frame number
 * of that keyframe and some spline controls which are currently
 * ignored.
 */

class LwsFrame extends TextfileParser {

    // data from the file
    double x, y, z;
    double heading, pitch, bank;
    double xScale, yScale, zScale;
    double frameNumber;
    int linearValue;
    double tension, continuity, bias;

    /**
     * Constructor: parses and stores all data associated with a particular
     * keyframe
     */
    LwsFrame(StreamTokenizer st) {
	x = getNumber(st);
	y = getNumber(st);
	z = -getNumber(st);
	debugOutputLn(VALUES, "x, y, z " + x + ", " + y + ", " + z);
	heading = getNumber(st);
	pitch = getNumber(st);
	bank = getNumber(st);
	debugOutputLn(VALUES, "(degrees) h, p, b = " + heading + ", " + pitch + ", " + bank);
	heading *= (Math.PI / 180.0);  // Java3d works with radians
	pitch *= (Math.PI / 180.0);
	bank *= (Math.PI / 180.0);
	debugOutputLn(VALUES, "(radians) h, p, b = " + heading + ", " + pitch + ", " + bank);
	debugOutputLn(LINE_TRACE, "got pos and ori");
	xScale = getNumber(st);
	yScale = getNumber(st);
	zScale = getNumber(st);
	debugOutputLn(VALUES, "xs, ys, zs " + xScale +", " + yScale + ", " + zScale);
	frameNumber = (int)getNumber(st);
	// Note: The following spline controls are ignored
	linearValue = (int)getNumber(st);
	debugOutputLn(VALUES, "framenum, linear " + frameNumber + " , " + linearValue);
	tension = getNumber(st);
	continuity = getNumber(st);
	bias = getNumber(st);
	debugOutputLn(VALUES, "tension, cont, bias = " + tension + ", " + continuity + ", " + bias);
    }



    /**
     * Construct new frame that's in-between two given frames
     * Ratio gives the interpolation value for how far in-between
     * the new frame should be  (0.5 is half-way, etc)
     */
    LwsFrame(LwsFrame prevFrame, LwsFrame nextFrame, double ratio) {

 	x = prevFrame.x + (nextFrame.x - prevFrame.x) * ratio;
	y = prevFrame.y + (nextFrame.y - prevFrame.y) * ratio;
   	z = prevFrame.z + (nextFrame.z - prevFrame.z) * ratio;

	heading = prevFrame.heading +
	  (nextFrame.heading - prevFrame.heading) * ratio;
	pitch = prevFrame.pitch +
	  (nextFrame.pitch - prevFrame.pitch) * ratio;
	bank = prevFrame.bank +
	  (nextFrame.bank - prevFrame.bank) * ratio;
	xScale = prevFrame.xScale +
	  (nextFrame.xScale - prevFrame.xScale) * ratio;
	yScale = prevFrame.yScale +
	  (nextFrame.yScale - prevFrame.yScale) * ratio;
	zScale = prevFrame.zScale +
	  (nextFrame.zScale - prevFrame.zScale) * ratio;
	frameNumber = prevFrame.frameNumber +
	  (nextFrame.frameNumber - prevFrame.frameNumber) * ratio;

        // The following are not interpolated
	linearValue = prevFrame.linearValue;
	tension = prevFrame.tension;
	continuity = prevFrame.continuity;
	bias = prevFrame.bias;
    }

    /**
     * Using hermite interpolation construct a new frame that's 
     * in-between two given frames. We also need to be given a
     * frame before the first frame and a frame after the second
     * frame. The calling function will make sure that we get the
     * four appropriate frames.
     *
     * Ratio gives the interpolation value for how far in-between
     * the new frame should be.  (.5 is half-way, etc.)
     */
    LwsFrame(LwsFrame prevFrame, LwsFrame frame1, 
             LwsFrame frame2, LwsFrame nextFrame, double u, 
             double adj0, double adj1) {

        double h1, h2, h3, h4;
        double dd0a, dd0b, ds1a, ds1b;

        // pre-compute spline coefficients
        double u2, u3, z1;
        u2 = u * u; 
        u3 = u2 *u;
        z1 = 3.0f *u2 - u3 - u3;
        h1 = 1.0f - z1; 
        h2 = z1;
        h3 = u3 - u2 - u2 + u;
        h4 = u3 - u2;

        dd0a = (1.0f - frame1.tension) * (1.0f + frame1.continuity) 
                                       * (1.0f + frame1.bias);

        dd0b = (1.0f - frame1.tension) * (1.0f - frame1.continuity) 
                                       * (1.0f - frame1.bias);

        ds1a = (1.0f - frame2.tension) * (1.0f - frame2.continuity) 
                                       * (1.0f + frame2.bias);

        ds1b = (1.0f - frame2.tension) * (1.0f + frame2.continuity) 
                                       * (1.0f - frame2.bias);

        double[] v = new double[4];

        // interpolate x, y, z
        v[0] = prevFrame.x; v[1] = frame1.x; 
        v[2] = frame2.x; v[3] = nextFrame.x;
        x = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                     adj0, adj1, h1, h2, h3, h4); 
        v[0] = prevFrame.y; v[1] = frame1.y; 
        v[2] = frame2.y; v[3] = nextFrame.y;
        y = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                     adj0, adj1, h1, h2, h3, h4); 
        v[0] = prevFrame.z; v[1] = frame1.z; 
        v[2] = frame2.z; v[3] = nextFrame.z;
        z = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                     adj0, adj1, h1, h2, h3, h4); 

        // interpolate heading pitch and bank 
        v[0] = prevFrame.heading; v[1] = frame1.heading; 
        v[2] = frame2.heading ; v[3] = nextFrame.heading;
        heading = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                      adj0, adj1, h1, h2, h3, h4); 

        v[0] = prevFrame.pitch; v[1] = frame1.pitch; 
        v[2] = frame2.pitch; v[3] = nextFrame.pitch;
        pitch = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                      adj0, adj1, h1, h2, h3, h4); 

        v[0] = prevFrame.bank; v[1] = frame1.bank; 
        v[2] = frame2.bank; v[3] = nextFrame.bank;
        bank = computeInterpolation (v, dd0a, dd0b, ds1a, ds1b, 
                                      adj0, adj1, h1, h2, h3, h4); 

        // interpolate scale - scale interpolation is assumed to be linear
	xScale = frame1.xScale + (frame2.xScale - frame1.xScale) * u;
	yScale = frame1.yScale + (frame2.yScale - frame1.yScale) * u;
	zScale = frame1.zScale + (frame2.zScale - frame1.zScale) * u;

        // interpolate frame number
	frameNumber = frame1.frameNumber +
	  (frame2.frameNumber - frame1.frameNumber) * u;

        // The following are not interpolated
	linearValue = frame2.linearValue;

        // We need to keep the spline smooth between knot points
	tension = 0.0; 
	continuity = 0.0; 
	bias = 0.0; 
    }

   
    double computeInterpolation(double[] value, double dd0a, 
                                double dd0b, double ds1a, 
                                double ds1b, double adj0, 
                                double adj1, double h1,
                                double h2, double h3, double h4) {

        double dd0, ds1;
        double delta = value[2] - value[1] ; 
        double result;

        // if adj != 0
        if (adj0 < -0.0001 || adj0 > 0.0001) 
          dd0 = adj0 * (dd0a * (value[1] - value[0]) + dd0b * delta);
        else 
          dd0 = 0.5f * (dd0a + dd0b) * delta; 

        // if adj != 0
        if (adj1 < -0.0001 || adj1 > 0.0001) 
          ds1 = adj1 * (ds1a * delta + ds1b * (value[3] - value[2])); 
        else 
          ds1 = 0.5f * (ds1a + ds1b) * delta; 

        result = value[1] * h1 + value[2] * h2 + dd0 * h3 + ds1 * h4;

        return (result);
    }

    double getHeading() {
	return heading;
    }
    
    double getPitch() {
	return pitch;
    }
    
    double getBank() {
	return bank;
    }

    /**
     * Sets the given matrix to contain the position, orientation, and
     * scale values for the keyframe
     */
    void setMatrix(Matrix4d mat)	{
	setRotationMatrix(mat);
	mat.setTranslation(new Vector3d(x, y, z));
	Matrix4d m = new Matrix4d();
	m.setColumn(0, xScale, 0, 0, 0);  // setScale not yet implemented
	m.setColumn(1, 0, yScale, 0, 0); 
	m.setColumn(2, 0, 0, zScale, 0);
	m.setColumn(3, 0, 0, 0, 1);
	mat.mul(m);
    }

    /**
     * Sets the given matrix to contain the orientation for this keyframe
     */
    void setRotationMatrix(Matrix4d mat)
    {
	debugOutputLn(TRACE, "setRotMat()");
	debugOutputLn(VALUES, " p, h, b = " +
			   pitch + ", " +
			   heading + ", " +
			   bank);
	Matrix4d pitchMat = new Matrix4d();
	pitchMat.rotX(-pitch);
	Matrix4d bankMat = new Matrix4d();
	bankMat.rotZ(bank);
	mat.rotY(-heading);
	mat.mul(pitchMat);
	mat.mul(bankMat);
	debugOutputLn(VALUES, "setRotMat(), mat = " + mat);
    }

    Point3f getPosition() {
	return (new Point3f((float)x, (float)y, (float)z));
    }

    Point3f getScale() {
        // Make sure we don't have zero scale components
        if ((xScale < -0.0001 || xScale > 0.0001) &&
            (yScale < -0.0001 || yScale > 0.0001) &&
            (zScale < -0.0001 || zScale > 0.0001)) {
	   return (new Point3f((float)xScale, (float)yScale, (float)zScale));
        } else {
	   return (new Point3f(1.0f, 1.0f, 1.0f));
        }
    }

    double getFrameNum() {
	return frameNumber;
    }

    void printVals() {
	debugOutputLn(VALUES, "         x = " + x);
	debugOutputLn(VALUES, "         y = " + y);
	debugOutputLn(VALUES, "         z = " + z);
	debugOutputLn(VALUES, "         xScale = " + xScale);
	debugOutputLn(VALUES, "         yScale = " + yScale);
	debugOutputLn(VALUES, "         zScale = " + zScale);
	debugOutputLn(VALUES, "         heading = " + heading);
	debugOutputLn(VALUES, "         pitch = " + pitch);
	debugOutputLn(VALUES, "         bank = " + bank);
	debugOutputLn(VALUES, "         frameNum = " + frameNumber);
	debugOutputLn(VALUES, "         lin = " + linearValue);
	debugOutputLn(VALUES, "         tension = " + tension);
	debugOutputLn(VALUES, "         continuity = " + continuity);
	debugOutputLn(VALUES, "         bias = " + bias);
    }

}	


