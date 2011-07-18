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
 * This class represents one keyframe in an envelope sequence.
 */

class LwsEnvelopeFrame extends TextfileParser {

    // data from the file
    double value;
    double frameNumber;
    int linearValue;
    double tension, continuity, bias;


    /**
     * Constructor: parses stream and stores data for one keyframe of
     * an envelope sequence
     */    
    LwsEnvelopeFrame(StreamTokenizer st) {
	value = getNumber(st);
	debugOutputLn(VALUES, "value = " + value);
	frameNumber = (int)getNumber(st);
	linearValue = (int)getNumber(st);
	debugOutputLn(VALUES, "framenum, linear " + frameNumber + " , " + linearValue);
	tension = getNumber(st);
	continuity = getNumber(st);
	bias = getNumber(st);
	debugOutputLn(VALUES, "tension, cont, bias = " + tension + ", " + continuity + ", " + bias);
	//System.out.println("   FRAME VALS");
	//printVals();
    }


    double getValue() {
	return value;
    }
    

    double getFrameNum() {
	return frameNumber;
    }

    
    void printVals() {
	debugOutputLn(VALUES, "         value = " + value);
	debugOutputLn(VALUES, "         frameNum = " + frameNumber);
	debugOutputLn(VALUES, "         lin = " + linearValue);
	debugOutputLn(VALUES, "         tension = " + tension);
	debugOutputLn(VALUES, "         continuity = " + continuity);
	debugOutputLn(VALUES, "         bias = " + bias);
    }

}	
