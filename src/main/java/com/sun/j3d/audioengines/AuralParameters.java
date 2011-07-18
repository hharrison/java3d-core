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

package com.sun.j3d.audioengines;

import javax.media.j3d.*;
import javax.vecmath.*;

/**
 * The AuralParameters Class defines a set of fields that define the
 * Aural listening environment.  Many of the parameters correspond to
 * AuralAttribute fields.
 *
 * <p>
 * Error checking on all parameters passed to these methods is already
 * explicitly being done by the Java 3D core code that calls these methods.
 */

public class AuralParameters
{
    // Speed of Sound in meters/milliseconds
    public static final float  SPEED_OF_SOUND  = 0.344f;
    public static final int    NO_FILTERING    = -1;

    public float rolloff = 1.0f;
    public float reflectionCoefficient = 0.0f;
    public float reverbDelay = 40.0f;
    public int   reverbOrder = 0;
    public float frequencyScaleFactor = 1.0f;
    public float velocityScaleFactor = 0.0f;
    int          filterType = NO_FILTERING;
    double[]     filterDistance = null;
    float[]      filterCutoff   = null;

    /*
     * @since Java 3D 1.3
     */
    public float reverbCoefficient = 1.0f;  
    public float reflectionDelay = 20.0f;  
    public float decayTime       = 1000.0f;
    public float decayFrequencyCutoff = 5000.0f;
    public float diffusion       = 1.0f;  // 100%
    public float density       = 1.0f;  // 100%

    /**
     * Construct a new AuralParameters object
     */  
    public AuralParameters() {
        frequencyScaleFactor = 1.0f;
        velocityScaleFactor = 0.0f;
        rolloff = 1.0f;
        reflectionCoefficient = 0.0f;
        reflectionDelay = 20.0f;
        reverbCoefficient = 1.0f;
        reverbDelay = 40.0f;
        reverbOrder = 0;
        filterType = NO_FILTERING;
        filterDistance = new double[2];  // start out with array of two
        filterCutoff   = new float[2];  // start out with array of two
        decayTime       = 1000.0f;
        decayFrequencyCutoff = 5000.0f;
        diffusion       = 1.0f;  // 100%
        density       = 1.0f;  // 100%
    }

    public void setDistanceFilter(int filterType, double[] distance, 
              float[]  filterCutoff) {
        boolean error = false;
        boolean allocate = false;
        int attenuationLength = 0;
        if (distance == null || filterCutoff == null) {
            error = true;
        } 
        else {
            attenuationLength = distance.length;
            if (attenuationLength == 0 || filterType == NO_FILTERING) {
                error = true;
            }
        }  
        if (error) {
            this.filterType = NO_FILTERING;
            this.filterDistance = null;
            this.filterCutoff   = null;
            if (debugFlag)
                debugPrint("setDistanceFilter NO_FILTERING");
            return;
        }
        this.filterType = filterType;
            if (debugFlag)
                debugPrint("setDistanceFilter type = " + filterType);
        if ((filterDistance == null) || (filterCutoff == null)) {
            allocate = true;
        }
        else if (attenuationLength > filterDistance.length) {
            allocate = true;
        }
        if (allocate) {
            if (debugFlag)
                debugPrint("setDistanceFilter length = " + attenuationLength);
            this.filterDistance = new double[attenuationLength];
            this.filterCutoff   = new float[attenuationLength];
        }
        System.arraycopy(distance, 0, this.filterDistance, 0, 
                         attenuationLength); 
        System.arraycopy(filterCutoff, 0, this.filterCutoff, 0, 
                         attenuationLength); 

        if (debugFlag) {
            debugPrint("setDistanceFilter arrays = ");
            for (int i=0; i<attenuationLength; i++)
                debugPrint(this.filterDistance[i] + "," + this.filterCutoff[i]);
            debugPrint("setDistanceFilter passed in = ");
            for (int i=0; i<attenuationLength; i++)
                debugPrint((float)(filterDistance[i]) + "," + filterCutoff[i]);
        }
        return;
    }
    public int  getDistanceFilterLength() {
        if (filterDistance != null)
            return filterDistance.length;
        return 0;
    }

    public int  getDistanceFilterType() {
        return filterType;
    }

    public void getDistanceFilter(double[] distance, float[]  filterCutoff) {
        if (distance == null || filterCutoff == null)
            return;
        int attenuationLength = distance.length;
        if (attenuationLength == 0 ||
            (filterDistance==null) || (filterCutoff==null))
            return;
        if (attenuationLength > filterDistance.length)
            attenuationLength = filterDistance.length;
        System.arraycopy(this.filterDistance, 0, distance, 0,
                         attenuationLength); 
        System.arraycopy(this.filterCutoff, 0, filterCutoff, 0,
                         attenuationLength); 
        return;
    }

    // Debug print flags
    static final boolean debugFlag = false;
    static final boolean internalErrors = false;

    /**
     * Debug print method for Sound nodes
     */ 
    protected void debugPrint(String message) {
        if (debugFlag)
            System.out.println(message);
    }
 
}
