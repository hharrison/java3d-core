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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader; 
import java.io.StreamTokenizer; 
import java.io.IOException; 
import javax.media.j3d.*; 
import javax.vecmath.Point3d;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import java.io.FileNotFoundException;

/**
 * This class was created to handle "sequence files", which allow 
 * holosketch-type Tloop sequences to be loaded through the lw3d loader.
 * The class reads a sequence file line by line and uses SequenceLine to 
 * load the file specified in each line.<BR>
 * Idea behind the Tloop process:<BR>
 * Artist creates "tloops" (animations with each keyframe's
 * geometry saved out explicitly) where the geometries are spaced
 * one frame apart.  Then I can automatically create a SwitchValueInterpolator
 * based on this spacing.  If the number of frames in the sequence is
 * greater than the number of frames in the tloop, then it will automatically
 * loop until the end of the sequence.<BR>
 * Process:<BR>
 * 1) Artist creates an animation of a null group that has a child with some
 * special name, such as "bucket_sequence_bucketsequence.txt.lwo", which tells
 * the lw3d loader that it should look for a sequence file by the name of
 * bucketsequence.txt.  What happens to this object is irrelevant (as far as
 * the loader is concerned); all animation information is taken from its 
 * parent instead.<BR>
 * 2) Artist saves out the geometry of the bucket at whatever frames she wants
 * to.  If she's saving a tloop (a sequence of frames), she should save them 
 * under the names <filename>xxx.lwo, where xxx is the 3-digit sequence number
 * (000, 001, 002, etc.).<BR>
 * 3) Artist creates the sequence file, which lists all saved geometry files
 * (except sequences - these can be referred to simply by the first file 
 * (...000.lwo)), along with their associated start/end frames.  She also lists 
 * the number of files in the sequence, although this parameter is implied
 * anyway, through the existence of the sequence files and their naming
 * convention.  Maybe we should trash this guy.<BR>
 * 4) In the lw3d loader, when LwsObject encounters an object with the 
 * filename "..._sequence_<filename>.lwo", it searches for filename.  If
 * found, it parses the file (using the SequenceReader class) to retrieve
 * all parameters.<BR>
 * 5)  Each SequenceLine creates a Java3D group containing its objects.  This
 * is either a plain-old-Group (if there is only one object) or a Switch group
 * with a SwitchValueInterpolator.<BR>
 * 6) SequenceReader constructs a Switch group and adds all SequenceLine groups
 * to this new group.  It also creates a SwitchPathInterpolator (child of
 * PathInterolator) that contsructs an Alpha based on the startFrame values of
 * each SequenceLine.  It creates a group and adds the SwitchPathInterpolator
 * plus any SequenceLine SwitchValueInterpolators to this group.<BR>
 * 7) LwsObject adds the SequenceReader Switch group to its objectTransform.
 * It does a getBehaviors() from SequenceReader and adds the result (the
 * SwitchPathInterpolator group) to its objectBehaviors group.<BR>
 * 8) Done.
 */

class SequenceLine {

    int      startFrame;
    int      endFrame;
    String   fileName;

    Group    geometryGroup = null;
    Behavior behaviors;
    int      numFrames;
    float    totalTime;
    int      totalFrames;

    // storedRefList keeps references to already loaded objects
    static Hashtable storedRefList = new Hashtable();
    
    SequenceLine(StreamTokenizer st, float time, int frames)
	throws ParsingErrorException {
	try {
	    totalTime = time;
	    totalFrames = frames;
	    startFrame = (int)st.nval;
	    st.nextToken();
	    endFrame = (int)st.nval;
	    st.nextToken();
	    fileName = st.sval;
	    numFrames = endFrame - startFrame + 1;
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }

    /**
     * Creates a SwitchValueInterpolator which is used to switch between
     * the objects specified for the sequence.  This is done for files
     * that end in "000", meaning that there are a sequence of files
     * with that same base name that should specify every frame of a
     * sequence for an object.  The Switch node is used to hold all of the
     * files and the Switch Behavior node is used to activate switching
     * at the right time and to the right object.
     */
    private void createSwitchBehavior(Switch target) {

	int loopCount = -1;
	float animTime = 1000.0f * totalTime *
	    (float)(target.numChildren())/(float)totalFrames;
	float startTime = 1000f * totalTime *
	    (float)startFrame/(float)totalFrames;
	Alpha theAlpha =
	          new Alpha(-1, (long)startTime, 0, (long)animTime, 0, 0);
		     
	SwitchValueInterpolator b=new SwitchValueInterpolator(theAlpha,target);
	behaviors = b;
	BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000000.0);
	b.setSchedulingBounds(bounds);
        target.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        target.addChild(behaviors);
 
    }
   
    /**
     * Create Java3d objects from the data in the sequence line.  This
     * means that for a tloop file (ends in "000"), we're going to create
     * the appropriate geometry for each file, put them all in a Switch
     * node, then create a SwitchValueInterpolator to swap between the
     * frames of the tloop.  If it's not a tloop, then we're just going to
     * create the geometry for that file.
     */
    void createJava3dObjects(int debugVals, int loadBehaviors)
	throws IncorrectFormatException, FileNotFoundException {
	if (fileName.indexOf("000") != -1) {  // Tloop
	    int index = fileName.indexOf("000");
	    String fileNameBase = fileName.substring(0, index);
	    Switch s = new Switch();
	    s.setCapability(Switch.ALLOW_SWITCH_READ);
	    s.setCapability(Switch.ALLOW_SWITCH_WRITE);
	    String tempFileName = fileName;
	    int fileNum = 0;
	    while ((new File(tempFileName)).exists()) {
		if (storedRefList.get(tempFileName) != null) {
		    // System.out.println("retrieve stored version of " +
		    // 		       tempFileName);
		    SharedGroup storedGroup = 
			(SharedGroup)storedRefList.get(tempFileName);
		    Link newLink = new Link(storedGroup);
		    s.addChild(newLink);
		}
		else {
		    // System.out.println("reading " + tempFileName);
		    J3dLwoParser objParser = new J3dLwoParser(tempFileName,
							      debugVals);
		    objParser.createJava3dGeometry();
		    TransformGroup t = new TransformGroup();
		    SharedGroup newSharedGroup = new SharedGroup();
		    storedRefList.put(tempFileName, newSharedGroup);
		    newSharedGroup.addChild(t);
		    Link newLink = new Link(newSharedGroup);
		    s.addChild(newLink);
		    if (objParser.getJava3dShapeList() != null) {
			for (Enumeration e =
				 objParser.getJava3dShapeList().elements() ;
			     e.hasMoreElements() ;) {
			    t.addChild((Shape3D)e.nextElement());
			}
		    }
		}
		++fileNum;
		String fileNumString = String.valueOf(fileNum);
		if (fileNum < 10)
		    fileNumString = "00" + fileNumString;
		else if (fileNum < 100)
		    fileNumString = "0" + fileNumString;
		tempFileName = fileNameBase + fileNumString + ".lwo";
	    }
            behaviors = null;
            if (loadBehaviors != 0) {
	      createSwitchBehavior(s);
            }
	    geometryGroup = (Group)s;
	}
	else {// Not a tloop, just a file
		geometryGroup = new Group();
		if (storedRefList.get(fileName) != null) {
			// System.out.println("getting old ref to " + fileName);
			SharedGroup storedGroup = 
				(SharedGroup)storedRefList.get(fileName);
			Link newLink = new Link(storedGroup);
			geometryGroup.addChild(newLink);
		}
		else {
		// System.out.println("reading " + fileName);
	    J3dLwoParser objParser = new J3dLwoParser(fileName,
						      debugVals);
	    objParser.createJava3dGeometry();
	    TransformGroup t = new TransformGroup();
	    if (objParser.getJava3dShapeList() != null) {
		for (Enumeration e = objParser.getJava3dShapeList().elements() ;
		     e.hasMoreElements() ;) {
		    t.addChild((Shape3D)e.nextElement());
		}
	    }
		SharedGroup newSharedGroup = new SharedGroup();
		newSharedGroup.addChild(t);
		Link newLink = new Link(newSharedGroup);
		geometryGroup.addChild(newLink);
		storedRefList.put(fileName, newSharedGroup);
		}
	}
    }

    Group getGeometry() {
	return geometryGroup;
    }

    Behavior getBehavior() {
	return behaviors;
    }
    
}
