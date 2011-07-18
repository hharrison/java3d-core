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
import java.util.Vector;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.Enumeration;
import com.sun.j3d.loaders.ParsingErrorException;

/**
 * This class parses the data in a Scene file related to the camera and
 * creates Java3D TransformGroup that holds the data for positioning
 * and orienting the view according to the camera specifications.
 */
	
class LwsCamera extends TextfileParser implements LwsPrimitive {

    // data from the file
    String         fileName;
    String         objName;
    LwsMotion      motion;
    int            parent;
    TransformGroup objectTransform;
    Vector         objectBehavior;

    /**
     * Constructor: parses camera info and creates LwsMotion object for
     * keyframe data
     */
    LwsCamera(StreamTokenizer st, int firstFrame,
		     int totalFrames, float totalTime,
		     int debugVals) throws ParsingErrorException {
	debugPrinter.setValidOutput(debugVals);
	parent = -1;
	getNumber(st);  // Skip ShowCamera parameters
	getNumber(st);
	getAndCheckString(st, "CameraMotion");
	motion = new LwsMotion(st, firstFrame, totalFrames, totalTime,
			       debugPrinter.getValidOutput());
	
	// TODO: buggy way to stop processing the camera.  Should actually
	// process required/optional fields in order and stop when there's
	// no more.  
	
	while (!isCurrentToken(st, "DepthOfField")) {
	    debugOutputLn(LINE_TRACE, "currentToken = " + st.sval);
	    
	    if (isCurrentToken(st, "ParentObject")) {
		parent = (int)getNumber(st);
	    }
	    try {
		st.nextToken();
	    }
	    catch (IOException e) {
		throw new ParsingErrorException(e.getMessage());
	    }
	}
	getNumber(st); // skip shadow type parameter
    }

    /**
     * Returns parent of the camera object
     */
    int getParent() {
	return parent;
    }

    /**
     * Creates Java3D items from the camera data.  These objects consist
     * of: a TransformGroup to hold the view platform, and the behaviors
     * (if any) that act upon the view's TransformGroup.
     */
    void createJava3dObject(int loadBehaviors)
    {
	Matrix4d mat = new Matrix4d();
	mat.setIdentity();
	// Set the node's transform matrix according to the first frame
	// of the object's motion
	LwsFrame firstFrame = motion.getFirstFrame();
	firstFrame.setMatrix(mat);
	debugOutputLn(VALUES, "  Camera Matrix = \n" + mat);
	Transform3D t1 = new Transform3D();
	    Matrix4d m = new Matrix4d();
	    double scale = .1;
	    m.setColumn(0, scale, 0, 0, 0);  // setScale not yet implemented
	    m.setColumn(1, 0, scale, 0, 0); 
	    m.setColumn(2, 0, 0, scale, 0);
	    m.setColumn(3, 0, 0, 0, 1);
	    Transform3D scaleTrans = new Transform3D(m);
	    TransformGroup scaleGroup = new TransformGroup(scaleTrans);
	    scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	    scaleGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	    //	    mat.mul(m);
	t1.set(mat);
	objectTransform = new TransformGroup(t1);
	objectTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objectBehavior = new Vector();;
        if (loadBehaviors != 0) {
 	  motion.createJava3dBehaviors(objectTransform);
          Behavior b = motion.getBehaviors(); 
          if (b != null)
	    objectBehavior.addElement(b);
        }  
    }

    /**
     * Returns TransformGroup of camera
     */
    public TransformGroup getObjectNode()
    {
	return objectTransform;
    }

    /**
     * Returns animation behaviors for camera
     */
    public Vector getObjectBehaviors()
    {
	debugOutputLn(TRACE, "getObjectBehaviors()");
	return objectBehavior;
    }

    /**
     * This is a debuggin utility, not currently activated.  It prints
     * out the camera values
     */
    void printVals()
    {
	System.out.println("   objName = " + objName);
	motion.printVals();
    }
    
}	
