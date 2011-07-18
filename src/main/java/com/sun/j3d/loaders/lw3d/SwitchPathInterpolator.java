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

 
import javax.vecmath.*;
import java.util.BitSet;
import java.util.Enumeration;

import javax.media.j3d.Alpha;
import javax.media.j3d.Node;
import javax.media.j3d.NodeReferenceTable;
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Switch;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * This class was used in conjunction with SequenceReader to create
 * Tloop functionality inside of Lightwave files.  This behavior handles
 * the switching between objects defined in separate lines of a 
 * sequence file.  That is, each line in a sequence file has the name
 * of an object (or an object sequence, if the name ends in "000")
 * and details the start and end frames that that object should be active.
 * This class determines which object/s defined in the file should be active
 * at any given time during the animation.
 */

class SwitchPathInterpolator extends FloatValueInterpolator {

    Switch target;
    int firstSwitchIndex;
    int lastSwitchIndex;
    int currentChild;
    int childCount;
    
    /**
      * Constructs a new SwitchPathInterpolator object.
      * @param alpha the alpha object for this interpolator
      * @param knots an array of knot values that specify a spline
     */
    SwitchPathInterpolator(Alpha alpha, float knots[], Switch target) {

	super(alpha, knots, new float[knots.length]);

	if (knots.length != (target.numChildren() + 1))
	    throw new IllegalArgumentException(J3dUtilsI18N.getString("SwitchPathInterpolator0"));

	this.target = target;
	firstSwitchIndex = 0;
	lastSwitchIndex = target.numChildren() - 1;
	childCount = lastSwitchIndex + 1;
    }

    /**
     * This method sets the correct child for the Switch node according
     * to alpha  
     * @param criteria enumeration of criteria that have triggered this wakeup
     */

    public void processStimulus(Enumeration criteria) {

	int child;

        // Handle stimulus 
        if (this.getAlpha() != null) {

            // Let PathInterpolator calculate the correct
            // interpolated knot point 
            computePathInterpolation();

	    if (currentKnotIndex > 0)
	        child = currentKnotIndex - 1;
	    else
	        child = 0;
	
	    if (target.getWhichChild() != child) {
	        target.setWhichChild(child);
	    }

            if ((this.getAlpha()).finished())
                return;
        }

        wakeupOn(defaultWakeupCriterion);
    }

}
