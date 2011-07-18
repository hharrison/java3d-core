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

package com.sun.j3d.utils.behaviors.interpolators;

import javax.media.j3d.*;
import java.util.*;
import javax.vecmath.*;
import com.sun.j3d.internal.J3dUtilsI18N;

/**
 * KBCubicSplineCurve is a container class that holds a number of 
 * KBCubicSplineSegments 
 * 
 * @since Java3D 1.2
 */

public class KBCubicSplineCurve {
  
    private float                    totalCurveLength; 
    private KBCubicSplineSegment[]   cubicSplineSegment;
    public  int                      numSegments;
 

    // default constructor 
    KBCubicSplineCurve () {
        numSegments = 0;
        totalCurveLength = 0f;
    }

    /**
     * This method takes a list of key frames and creates spline segments
     * from it. It requires at least four key frames to be passed to it.
     * Given n key frames, it creates n-3 KBCubicSplineSegments.
     * @param the list of key frames that specify the motion path 
     */

    KBCubicSplineCurve (KBKeyFrame keys[]) {

        int keyLength = keys.length;
        // Require at least 4 key frames for cubic spline curve 
        if (keyLength < 4)
          throw new 
            IllegalArgumentException(J3dUtilsI18N.getString("KBCubicSplineCurve0"));
        
        numSegments = keyLength - 3;
        this.cubicSplineSegment = new KBCubicSplineSegment[numSegments];

        // intialize and calculate coefficients for each segment 
        int k0 = 0; int k1 = 1; int k2 = 2; int k3 = 3;
        for (; k0 < numSegments; k0++, k1++, k2++, k3++) {
           this.cubicSplineSegment[k0] = new KBCubicSplineSegment
                                (keys[k0], keys[k1], keys[k2], keys[k3]);
        }

        // compute total curve length
        computeTotalCurveLength ();
    }

    /**
     * This method takes a list of spline segments creates the 
     * KBCubicSplineCurve. 
     * @param the list of segments that comprise the complete motion path
     */

    KBCubicSplineCurve (KBCubicSplineSegment s[]) {

        cubicSplineSegment = new KBCubicSplineSegment[s.length];
        numSegments = cubicSplineSegment.length;
        for (int i = 0; i < numSegments; i++) {
           this.cubicSplineSegment[i] = s[i];
        }

        // compute total curve length
        computeTotalCurveLength ();
    }

    /**
     * This method takes a list of spline segments to replace the existing 
     * set of KBCubicSplineSegments that comprise the current 
     * KBCubicSplineCurve motion path. 
     * @param s the list of segments that comprise the complete motion path
     */

    public void setSegments (KBCubicSplineSegment s[]) {

        cubicSplineSegment = new KBCubicSplineSegment[s.length];
        numSegments = cubicSplineSegment.length;
        for (int i = 0; i < numSegments; i++) {
           this.cubicSplineSegment[i] = s[i];
        }

        // compute total curve length
        computeTotalCurveLength ();
    }

    /**
     * This method returns the KBCubicSplineSegments pointed to by index 
     * @param index the index of the KBCubicSplineSegment required 
     * @return the KBCubicSplineSegment pointed to by index
     */
    public KBCubicSplineSegment getSegment (int index) {

        return this.cubicSplineSegment[index];

    } 
    

    // computes the total length of the curve 
    private void computeTotalCurveLength () {

        totalCurveLength = 0f;
        for (int i = 0; i < numSegments; i++) {
           totalCurveLength += cubicSplineSegment[i].length;
        } 

    }

    /**
     * This method returns the total length of the entire KBCubicSplineCurve 
     * motion path.
     *
     * @return the length of the KBCubicSplineCurve motion path 
     */

    public float getTotalCurveLength () {

        return this.totalCurveLength;

    }
  
}

