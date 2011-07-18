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
 * The Sample class defines the data and methods associated with a sound
 * sample played through the AudioDevice.
 * This contains all the data fields for non-spatialized and spatialized
 * (positional and directional) sound samples.
 */
public class Sample {

    // Debug print flags and methods
    static final protected boolean debugFlag = false;
    static final protected boolean internalErrors = false;

    protected void debugPrint(String message) {
        if (debugFlag)
            System.out.println(message);
    }

    protected void debugPrintln(String message) {
        if (debugFlag)
            System.out.println(message);
    }

   /**
    * Null Sound identifier denotes sound is not created or initialized
    */
    public static final int NULL_SAMPLE = -1;

    /**
     *  sound data associated with sound source
     */  
    protected MediaContainer  soundData = null;

    /**
     *  sound data associated with sound source
     */  
    protected int soundType = -1;

    /**
     *  Overall Scale Factor applied to sound gain.
     */  
    protected float gain = 1.0f;  // Valid values are >= 0.0.

    /**
     *  Overall Scale Factor applied to sound.
     *  @since Java 3D 1.3
     */  
    protected float rateScaleFactor = 1.0f;  // Valid values are >= 0.0.

    /**
     *  Number of times sound is looped/repeated during play
     */ 
    protected int   loopCount = 0;  //  Range from 0 to POSITIVE_INFINITY(-1)


    /*
     * Duration of sample
     * This should match the Sound node constant of same name
     */
    public static final int  DURATION_UNKNOWN = -1;
    protected long   duration = DURATION_UNKNOWN;

    protected int     numberOfChannels = 0;
    protected boolean mute = false;  // denotes if sample is muted
                          // (playing with zero gain)

    /*
     *
     * Fields associated with positional sound samples
     *
     */
    /*
     * Local to Vworld transform
     */
    protected Transform3D vworldXfrm = new Transform3D();
    protected boolean     vwXfrmFlag = false;

    /*
     * Origin of Sound source in Listener's space.
     */
    protected Point3f     position = new Point3f(0.0f, 0.0f, 0.0f);

    /*
     * Pairs of distances and gain scale factors that define piecewise linear
     * gain attenuation between each pair.
     */  
    protected double[]  attenuationDistance = null; 
    protected float[]   attenuationGain = null;;

    /**
     * dirty flags denoting what has changed since last rendering
     */  
    protected int dirtyFlags = 0xFFFF;

    /*
     *
     * Direction sample fields
     *
     */
    /**
     * The Cone Sound's direction vector.  This is the cone axis.
     */
    protected Vector3f	direction = new Vector3f(0.0f, 0.0f, 1.0f);

    /**
     * Pairs of distances and gain scale factors that define piecewise linear
     * gain BACK attenuation between each pair.
     * These are used for defining elliptical attenuation regions.
     */  
    protected double[]     backAttenuationDistance = null;
    protected float[]     backAttenuationGain = null;

    /**
     * Directional Sound's gain can be attenuated based on the listener's
     * location off-angle from the source source direction.
     * This can be set by three parameters:
     *     angular distance in radians
     *     gain scale factor
     *     filtering (currently the only filtering supported is lowpass)
     */
    protected double[]	angularDistance = {0.0, (Math.PI * 0.5)};
    protected float[]	angularGain     = {1.0f, 0.0f};

    /**
     *  Distance Filter
     *  Each sound source is attenuated by a filter based on it's distance
     *  from the listener.
     *  For now the only supported filterType will be LOW_PASS frequency 
     *  cutoff.
     *  At some time full FIR filtering will be supported.
     */ 
    public static final int  NO_FILTERING  = -1;
    public static final int  LOW_PASS      =  1;

    protected int         angularFilterType      = NO_FILTERING;
    protected float[]     angularFilterCutoff = {Sound.NO_FILTER, Sound.NO_FILTER};

    /*
     * Obstruction and Occlusion parameters
     * For now the only type of filtering supported is a low-pass filter
     * defined by a frequency cutoff value.
     * @since Java 3D 1.3
     */  
    protected float obstructionGain = 1.0f;  // scale factor
    protected int   obstructionFilterType = NO_FILTERING;
    protected float obstructionFilterCutoff = Sound.NO_FILTER;
    protected float occlusionGain = 1.0f;  // scale factor
    protected int   occlusionFilterType = NO_FILTERING;
    protected float occlusionFilterCutoff = Sound.NO_FILTER; 

    /*
     * Construct a new audio device Sample object
     */  
    public Sample() {
        if (debugFlag)
            debugPrintln("Sample constructor");
    }
    
    public long  getDuration() {
        return 0;
    }

    public long  getStartTime() {
        return 0;
    }

    public int    getNumberOfChannelsUsed() {
        return 0;
    }

    public void  setDirtyFlags(int flags) {
        dirtyFlags = flags;
    }

    public int   getDirtyFlags() {
        return dirtyFlags;
    }

    public void  setSoundType(int type) {
        soundType = type;
    }

    public int   getSoundType() {
        return soundType;
    }

    public void  setSoundData(MediaContainer ref) {
        soundData = ref;
    }

    public MediaContainer getSoundData() {
        return soundData;
    }

    public void  setMuteFlag(boolean flag) {
        mute = flag;
    }

    public boolean getMuteFlag() {
        return mute;
    }

    public void  setVWrldXfrmFlag(boolean flag) {
        // this flag is ONLY true if the VirtualWorld Transform is ever set
        vwXfrmFlag = flag;
    }

    public boolean getVWrldXfrmFlag() {
        return vwXfrmFlag;
    }

    public void  setGain(float scaleFactor) {
        gain = scaleFactor;
    }

    public float  getGain() {
        return gain;
    }

    public void   setLoopCount(int count) {
        loopCount = count; 
    }

    public int   getLoopCount() {
        return loopCount; 
    }


    public void   setPosition(Point3d position) {
        this.position.set(position);
        return;
    }

    // TODO: no get method for Position


    public void setDistanceGain(
              double[] frontDistance, float[]  frontAttenuationScaleFactor, 
              double[] backDistance, float[]  backAttenuationScaleFactor) {
        if (frontDistance != null) {
            int size = frontDistance.length;
            attenuationDistance = new double[size];
            attenuationGain = new float[size];
            for (int i=0; i<size; i++) {
                attenuationDistance[i] = frontDistance[i];
                attenuationGain[i] = frontAttenuationScaleFactor[i];
            }
        }
        else {
            attenuationDistance = null;
            attenuationGain = null;
        }
        if (backDistance != null && frontDistance != null) {
            int size = backDistance.length;
            backAttenuationDistance = new double[size];
            backAttenuationGain = new float[size];
            for (int i=0; i<size; i++) {
                backAttenuationDistance[i] = backDistance[i];
                backAttenuationGain[i] = backAttenuationScaleFactor[i];
            }
        }
        else {
            backAttenuationDistance = null;
            backAttenuationGain = null;
        }
        return;
    }

    // TODO: no get method for Back Attenuation


    public void setDirection(Vector3d direction) {
        this.direction.set(direction);
        return;
    }

    // TODO: no get method for Direction


    public void setAngularAttenuation(int filterType, double[] angle, 
                 float[] attenuationScaleFactor, float[] filterCutoff) {
        if (angle != null) {
            int size = angle.length;
            angularDistance = new double[size];
            angularGain = new float[size];
            if (filterType != NO_FILTERING && filterCutoff != null)
                angularFilterCutoff = new float[size];
            else
                angularFilterCutoff = null;
            for (int i=0; i<size; i++) {
                angularDistance[i] = angle[i];
                angularGain[i] = attenuationScaleFactor[i];
                if (filterType != NO_FILTERING)
                    angularFilterCutoff[i] = filterCutoff[i];
            }
            angularFilterType = filterType;
        }
        else {
            angularDistance = null;
            angularGain = null;
            angularFilterCutoff = null;
            angularFilterType = NO_FILTERING;
        }
    }

    // TODO: no get method for Angular Attenuation


    /*
     * Set Rate ScaleFactor
     * @since Java 3D 1.3
     */  
    public void  setRateScaleFactor(float scaleFactor) {
        rateScaleFactor = scaleFactor;
    }

    /*
     * Get Rate ScaleFactor
     * @since Java 3D 1.3
     */  
    public float  getRateScaleFactor() {
        return rateScaleFactor;
    }


    /*
     * Set Obstruction Gain
     * @since Java 3D 1.3
     */  
    public void  setObstructionGain(float scaleFactor) {
        obstructionGain = scaleFactor;
    }

    /*
     * Get Obstruction Gain
     * @since Java 3D 1.3
     */  
    public float  getObstructionGain() {
        return obstructionGain;
    }

    /*
     * Set Obstruction Filter Cutoff Frequency
     * @since Java 3D 1.3
     */  
    public void  setObstructionFilter(float cutoffFrequency) {
        obstructionFilterType = LOW_PASS;
        obstructionFilterCutoff = cutoffFrequency;
    }

    // TODO: no get method for Obstruction Filtering


    /*
     * Set Occlusion Gain
     * @since Java 3D 1.3
     */  
    public void  setOcclusionGain(float scaleFactor) {
        occlusionGain = scaleFactor;
    }

    /*
     * Get Occlusion Gain
     * @since Java 3D 1.3
     */  
    public float  getOcclusionGain() {
        return occlusionGain;
    }

    /*
     * Set Occlusion Filter Cutoff Frequency
     * @since Java 3D 1.3
     */  
    public void  setOcclusionFilter(float cutoffFrequency) {
        occlusionFilterType = LOW_PASS;
        occlusionFilterCutoff = cutoffFrequency;
    }

    // TODO: no get method for Occlusion Filtering


    /**
     * Clears/re-initialize fields associated with sample data
     * for this sound,
     * and frees any device specific data associated with this sample.
     */  
    public void clear() {
        if (debugFlag)
            debugPrintln("Sample.clear() entered");
        soundData = (MediaContainer)null;
        soundType = NULL_SAMPLE;
        gain = 1.0f;
        loopCount = 0; 
        duration = DURATION_UNKNOWN;
        numberOfChannels = 0;
        vworldXfrm.setIdentity();
        vwXfrmFlag = false;
        position.set(0.0f, 0.0f, 0.0f);
        attenuationDistance = null;
        attenuationGain = null;
        direction.set(0.0f, 0.0f, 1.0f);
        backAttenuationDistance = null;
        backAttenuationGain = null;
	if (angularDistance != null) {
	    angularDistance[0] = 0.0f;
	    angularDistance[1] = (float)(Math.PI) * 0.5f;
	}
	if (angularGain != null) {
	    angularGain[0] = 1.0f;
	    angularGain[1] = 0.0f;
	}
        angularFilterType = NO_FILTERING;
	if (angularFilterCutoff != null) {
	    angularFilterCutoff[0] = Sound.NO_FILTER;
	    angularFilterCutoff[1] = Sound.NO_FILTER;
	}
        obstructionGain = 1.0f; 
        obstructionFilterType = NO_FILTERING;
        obstructionFilterCutoff = Sound.NO_FILTER;
        occlusionGain = 1.0f;
        occlusionFilterType = NO_FILTERING;
        occlusionFilterCutoff = Sound.NO_FILTER;
    }

    /*
     * Render
     */
    public void render(int dirtyFlags, View view, AuralParameters attribs) {
        // meant to be overridden
    }
}
