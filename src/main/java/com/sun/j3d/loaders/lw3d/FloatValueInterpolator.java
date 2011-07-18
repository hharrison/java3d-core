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
import javax.media.j3d.Interpolator;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * This class acts as an interpolator between values specified in a 
 * floating point array, based on knot values (keyframes) specified in a 
 * knots array.
 */
abstract class FloatValueInterpolator extends Interpolator {

    private   float knots[];
    private   int   knotsLength;
    protected int   currentKnotIndex; 
    protected float currentInterpolationRatio;
    protected float values[];
    protected float currentValue;
    
    /**
      * Constructs a new FloatValueInterpolator object.
      * @param alpha the alpha object for this interpolator
      * @param knots an array of knot values that specify a spline
     */
    FloatValueInterpolator(Alpha alpha, float k[], float v[]) {

	super(alpha);

        // Check that first knot = 0.0f
        knotsLength = k.length;
        if (k[0] < -0.0001 || k[0] > 0.0001) {
            throw new IllegalArgumentException(J3dUtilsI18N.getString("FloatValueInterpolator0"));
        }

        // Check that last knot = 1.0f
        if ((k[knotsLength-1] - 1.0f) < -0.0001 || 
            (k[knotsLength-1] - 1.0f) > 0.0001) {
 
            throw new IllegalArgumentException(J3dUtilsI18N.getString("FloatValueInterpolator1"));
        }

        // Check to see that knots are in ascending order and copy them
        this.knots = new float[knotsLength];
        for (int i = 0; i < knotsLength; i++)  {
            if ((i > 0) && (k[i] < k[i-1])) {
                throw new IllegalArgumentException(J3dUtilsI18N.getString("FloatValueInterpolator2"));
            }
            this.knots[i] = k[i];
        }

        // check to see that we have the same number of values as knots
	if (knotsLength != v.length) {
	   throw new IllegalArgumentException(J3dUtilsI18N.getString("FloatValueInterpolator3"));
        }

        // copy the values
	this.values = new float[knotsLength];
	for(int i = 0; i < knotsLength; i++) {
	    this.values[i] = v[i];
	}

    }

    /**
      * This method sets the value at the specified index for 
      * this interpolator.
      * @param index the index to be changed
      * @param position the new value at index
      */
    void setValue(int index, float value) {
	this.values[index] = value;
    }

    /**
      * This method retrieves the value at the specified index.
      * @param index the index of the value requested
      * @return the interpolator's value at the index
      */
    float getValue(int index) {
	return this.values[index];
    }

    /**
     * This method computes the bounding knot indices and interpolation value
     * "currentValue" given the current value of alpha, the knots[] array and
     * the array of values.  
     * If the index is 0 and there will be no interpolation, both the
     * index variable and the interpolation variable are set to 0.
     * Otherwise, currentKnotIndex is set to the lower index of the
     * two bounding knot points and the currentInterpolationRatio
     * variable is set to the ratio of the alpha value between these
     * two bounding knot points.
     */
    protected void computePathInterpolation() {
        float alphaValue = (this.getAlpha()).value();

        for (int i = 0; i < knotsLength; i++) {
            if ((i == 0 && alphaValue <= knots[i]) ||
                (i > 0 && alphaValue >= knots[i-1] && alphaValue <= knots[i])) {

                if (i==0) {
                    currentInterpolationRatio = 0f;
                    currentKnotIndex = 0;
	            currentValue = values[0];
                }
                else {
                    currentInterpolationRatio =
                        (alphaValue - knots[i-1])/(knots[i] - knots[i-1]);
                    currentKnotIndex = i - 1;
	            currentValue = values[i-1] +
	 	          currentInterpolationRatio * (values[i] - values[i-1]);
                }
                break;
            }
        }
    }

}
    
