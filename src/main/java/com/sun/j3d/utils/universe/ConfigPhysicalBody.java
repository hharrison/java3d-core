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

package com.sun.j3d.utils.universe;

import java.awt.event.*;
import java.lang.Integer;
import java.io.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;

class ConfigPhysicalBody extends ConfigObject {

    Point3d	leftEyePosition = new Point3d(-0.033, 0.0, 0.0);
    Point3d	rightEyePosition = new Point3d(0.033, 0.0, 0.0);
    double	stereoEyeSeparation = Double.MAX_VALUE;

    Point3d	leftEarPosition = new Point3d(-0.080, -0.030, 0.09);
    Point3d	rightEarPosition = new Point3d(0.080, -0.030, 0.09);

    double	nominalEyeHeightFromGround = 1.68;
    double	nominalEyeOffsetFromNominalScreen = 0.4572;

    Matrix4d	headToHeadTracker = new Matrix4d(
		    1.0, 0.0, 0.0, 0.0,
		    0.0, 1.0, 0.0, 0.0,
		    0.0, 0.0, 1.0, 0.0,
		    0.0, 0.0, 0.0, 1.0);

    PhysicalBody j3dPhysicalBody;

    // Overridden to do nothing.
    protected void initialize(ConfigCommand command) {
    }

    protected void setProperty(ConfigCommand command) {
	int argc = command.argc;
	Object[] argv = command.argv;
	String prop;
	Object val;

	// Check that arg[1] and arg[2] are strings
	if (argc != 4) {
	    syntaxError("Incorrect number of arguments to " +
			command.commandName);
	}

	if (!isName(argv[1])) {
	    syntaxError("The first argument to " + command.commandName +
			" must be a name");
	}

	if (!isName(argv[2])) {
	    syntaxError("The second argument to " + command.commandName +
			" must be an property/attribute name");
	}

	prop = (String) argv[2];
	val = argv[3];

	if (prop.equals("StereoEyeSeparation")) {
	    if (!(val instanceof Double)) {
		syntaxError("StereoEyeSeparation must be a number");
	    }
	    stereoEyeSeparation = ((Double) val).doubleValue();
	}
	else if (prop.equals("LeftEyePosition")) {
	    if (!(val instanceof Point3d)) {
		syntaxError("LeftEyePosition must be a point");
	    }
	    leftEyePosition = (Point3d) val;
	}
	else if (prop.equals("RightEyePosition")) {
	    if (!(val instanceof Point3d)) {
		syntaxError("RightEyePosition must be a point");
	    }
	    rightEyePosition = (Point3d) val;
	}
	else if (prop.equals("LeftEarPosition")) {
	    if (!(val instanceof Point3d)) {
		syntaxError("LeftEarPosition must be a point");
	    }
	    leftEarPosition = (Point3d) val;
	}
	else if (prop.equals("RightEarPosition")) {
	    if (!(val instanceof Point3d)) {
		syntaxError("RightEarPosition must be a point");
	    }
	    leftEarPosition = (Point3d) val;
	}
	else if (prop.equals("NominalEyeHeightFromGround")) {
	    if (!(val instanceof Double)) {
		syntaxError("NominalEyeHeightFromGround must be a number");
	    }
	    nominalEyeHeightFromGround = ((Double) val).doubleValue();
	}
	else if (prop.equals("NominalEyeOffsetFromNominalScreen")) {
	    if (!(val instanceof Double)) {
		syntaxError("NominalEyeOffsetFromNominalScreen " +
			    "must be a number");
	    }
	    nominalEyeOffsetFromNominalScreen = ((Double) val).doubleValue();
	}
	else if (prop.equals("HeadToHeadTracker")) {
	    if (!(val instanceof Matrix4d)) {
		syntaxError("HeadToHeadTracker must be a matrix");
	    }
	    headToHeadTracker = (Matrix4d) val;
	}
	else {
	    syntaxError("Unknown " + command.commandName +
			" \"" + prop + "\"") ;
	}
    }

    PhysicalBody createJ3dPhysicalBody() {
	// Transfer all the information from the config version
	if (stereoEyeSeparation < Double.MAX_VALUE) {
	    leftEyePosition.set(-stereoEyeSeparation / 2.0, 0.0, 0.0);
	    rightEyePosition.set(stereoEyeSeparation / 2.0, 0.0, 0.0);
	}

	j3dPhysicalBody = new PhysicalBody(leftEyePosition, rightEyePosition,
					   leftEarPosition, rightEarPosition);

	j3dPhysicalBody.setHeadToHeadTracker(
				new Transform3D(headToHeadTracker));

	j3dPhysicalBody.setNominalEyeHeightFromGround(
				nominalEyeHeightFromGround);
	j3dPhysicalBody.setNominalEyeOffsetFromNominalScreen(
				nominalEyeOffsetFromNominalScreen);    

	return j3dPhysicalBody ;
    }
}
