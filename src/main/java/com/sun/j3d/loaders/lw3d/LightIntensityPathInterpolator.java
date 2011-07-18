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

import java.util.*;

import javax.media.j3d.Alpha;
import javax.media.j3d.Light;

/**
 * This Interpolator object modifies the intensity of a Light object
 * according to the keyframes in a light intensity envelope
 */
class LightIntensityPathInterpolator extends FloatValueInterpolator {

    LwLightObject theLight;

    LightIntensityPathInterpolator(Alpha alpha,
					  float knots[],
					  float values[],
					  Object target) {

	super(alpha, knots, values);
	theLight = (LwLightObject)target;
    }

    /**
     * This method is invoked by the behavior scheduler every frame.  It maps
     * the alpha value that corresponds to the current time into the 
     * appropriate light intensity for that time as obtained by interpolating
     * between the light intensity values for each knot point that were passed  
     * to this class.
     * @param criteria enumeration of criteria that have triggered this wakeup
     */

    public void processStimulus(Enumeration criteria) {
        // Handle stimulus

        if (this.getAlpha() != null) {

            // Let FloatValueInterpolator calculate the correct 
            // interpolated value
            computePathInterpolation();

            // Set light intensity to the value calculated by 
            // FloatValueInterpolator
            if (theLight != null) 
               theLight.setIntensity(currentValue);

            if ((this.getAlpha()).finished())
                return;
        }

        wakeupOn(defaultWakeupCriterion);

    }

}

