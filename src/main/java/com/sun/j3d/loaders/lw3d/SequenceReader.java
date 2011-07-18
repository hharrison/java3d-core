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
 * This class was created to read a special file format devised for
 * JavaOne '98 that allowed Tloop functionality inside of Lightwave. It
 * would be best to find a more standard solution, including using some
 * plug-in for lw3d that I've heard of that allows artists to automatically
 * save out the geometry for a file at every frame.
 */

class SequenceReader {


    Vector         sequenceLines;
    float          totalTime;
    int            totalFrames;
	
    TransformGroup objectTransform;
    Vector         behaviorVector;

    /**
     * Constructor: parses a sequence file and creates a new SequenceLine
     * object to read in every line of the file
     */
    SequenceReader(String filename, float time, int frames)
	throws ParsingErrorException {
	totalTime = time;
	totalFrames = frames;
	sequenceLines = new Vector();
	try {
	    // System.out.println("reading sequence from " + filename);
	    StreamTokenizer st = new StreamTokenizer(new BufferedReader(
		    new FileReader(filename)));
	    st.wordChars('_', '_');
	    st.wordChars('/', '/');
	    int type = st.nextToken();
	    while (st.ttype != StreamTokenizer.TT_EOF) {
		sequenceLines.addElement(new SequenceLine(st,
							  totalTime,
							  totalFrames));
		st.nextToken();
	    }
	}
	catch (IOException e) {
	    throw new ParsingErrorException(e.getMessage());
	}
    }

	/**
	* Creates Java3D objects from the data defined in the sequence
	* file.  Calls each sequenceLine object to create its own 
	* j3d objects, then puts all of those objects in a single Switch
	* node.  Finally, it creates a SwitchPathInterpolator object which
	* handles switching between each object/s defined by each line
	*/
    void createJava3dObjects(int debugVals, int loadBehaviors)
	throws FileNotFoundException {

	objectTransform = new TransformGroup();
        behaviorVector = new Vector();
	Enumeration e = sequenceLines.elements();
	Switch switchNode = new Switch();
	switchNode.setCapability(Switch.ALLOW_SWITCH_READ);
	switchNode.setCapability(Switch.ALLOW_SWITCH_WRITE);
	objectTransform.addChild(switchNode);
	while (e.hasMoreElements()) {
	    SequenceLine line = (SequenceLine)e.nextElement();
	    line.createJava3dObjects(debugVals, loadBehaviors);
	    if (line.getGeometry() != null)
	      switchNode.addChild(line.getGeometry());
	      //objectTransform.addChild(line.getGeometry());
	      if (line.getBehavior() != null) {
                behaviorVector.addElement(line.getBehavior());
	    }
	}
	float knots[] = new float[sequenceLines.size() + 1];
	for (int i = 0; i < knots.length-1; ++i) {
	    SequenceLine sl = (SequenceLine)sequenceLines.elementAt(i);
	    knots[i] = (float)sl.startFrame/(float)totalFrames;
	}
	knots[knots.length-1] = 1.0f;
	Alpha theAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE,
		                    0, 0, (long)(1000f * totalTime), 0,
		                    0, 0, 0, 0);
	
	SwitchPathInterpolator switchPath =
	    new SwitchPathInterpolator(theAlpha,
				       knots,
				       switchNode);
	BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 1000000.0);
	switchPath.setSchedulingBounds(bounds);
        switchNode.addChild(switchPath);
        behaviorVector.addElement(switchPath);
    }

    TransformGroup getObjectNode() {
	return objectTransform;
    }

    Vector getObjectBehaviors() {
	return behaviorVector;
    }
    
    void printLines() {
	Enumeration e = sequenceLines.elements();
	while (e.hasMoreElements()) {
	    SequenceLine line = (SequenceLine)e.nextElement();
	}
    }

}
