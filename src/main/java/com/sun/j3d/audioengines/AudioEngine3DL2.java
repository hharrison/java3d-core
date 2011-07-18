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
import java.util.ArrayList;


/**
 * The AudioEngine3DL2 Class defines an audio output device that generates
 * sound 'image' from high-level sound parameters passed to it during 
 * scene graph.
 *
 * <P>
 * The methods in this class are meant to be optionally overridden by an 
 * extended class.  This extended class would provice device specific code.
 *
 * <P>
 * Error checking on all parameters passed to these methods is already
 * explicitly being done by the Java 3D core code that calls these methods.
 *
 * <P>
 * These methods should NOT be called by any application if the audio engine
 * is associated with a Physical Environment used by Java3D Core.
 *
 * @since Java 3D 1.3
 */
public abstract class AudioEngine3DL2 extends AudioEngine3D implements AudioDevice3DL2 {
    /**
     * Construct a new AudioEngine3DL2 with the specified PhysicalEnvironment.
     * @param physicalEnvironment the physical environment object where we
     * want access to this device.
     */  
    public AudioEngine3DL2(PhysicalEnvironment physicalEnvironment ) {
        super(physicalEnvironment);
    }

    /*
     *
     * Methods that affect AudioEngine3DLD fields that are NOT associated 
     * with a specific sound sample
     *
     */

    /**
     * Pauses audio device engine without closing the device and associated
     * threads.
     * Causes all cached sounds to be paused and all streaming sounds to be
     * stopped.
     */
    public abstract void  pause();

    /**
     * Resumes audio device engine (if previously paused) without
     * reinitializing the device.
     * Causes all paused cached sounds to be resumed and all streaming
     * sounds restarted.
     */
    public abstract void resume();

    /**
     * Set overall gain control of all sounds playing on the audio device.
     * @param scaleFactor scale factor applied to calculated amplitudes for
     * all sounds playing on this device
     */  
    public abstract void setGain(float scaleFactor);

    /*
     *
     *  Methods explicitly affect a particular sound rendering and that
     *  require audio device specific methods that override this class.
     *
     */

    /**
     * Set scale factor applied to sample playback rate for a particular sound
     * associated with the audio device.
     * Changing the device sample rate affects both the pitch and speed.
     * This scale factor is applied to ALL sound types.
     * Changes (scales) the playback rate of a sound independent of
     * Doppler rate changes.
     * @param index device specific reference to device driver sample
     * @param scaleFactor non-negative factor applied to calculated 
     * amplitudes for all sounds playing on this device
     * @see Sound#setRateScaleFactor
     */
    public void  setRateScaleFactor(int index, float scaleFactor) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)
            sample.setRateScaleFactor(scaleFactor);
        return;
    }


    /*
     *
     *  Methods explicitly affect aural attributes of the listening space
     *  used to calculated reverberation during sound rendering.
     *  These require audio device specific methods that override this class.
     *
     */

    /**
     * Set late reflection (referred to as 'reverb') attenuation.
     * This scale factor is applied to iterative, indistinguishable
     * late reflections that constitute the tail of reverberated sound in
     * the aural environment.
     * This parameter, along with the early reflection coefficient, defines
     * the reflective/absorptive characteristic of the surfaces in the
     * current listening region.
     * @param coefficient late reflection attenuation factor 
     * @see AuralAttributes#setReverbCoefficient
     */
    public void setReverbCoefficient(float coefficient) {
        attribs.reverbCoefficient = coefficient;
        return;
    }


    /**  
     * Sets the early reflection delay time.
     * In this form,  the parameter specifies the delay time between each order
     * of reflection (while reverberation is being rendered) explicitly given
     * in milliseconds. 
     * @param reflectionDelay time between each order of early reflection
     * @see AuralAttributes#setReflectionDelay
     */ 
    public void  setReflectionDelay(float reflectionDelay) {
        attribs.reflectionDelay = reflectionDelay;
        return;
    }

    /**
     * Set reverb decay time.
     * Defines the reverberation decay curve.
     * @param time decay time in milliseconds
     * @see AuralAttributes#setDecayTime
     */
    public void  setDecayTime(float time) {
        attribs.decayTime = time;
        return;
    }

    /**
     * Set reverb decay filter.
     * This provides for frequencies above the given cutoff frequency to be
     * attenuated during reverb decay at a different rate than frequencies
     * below this value.  Thus, defining a different reverb decay curve for 
     * frequencies above the cutoff value.
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied.
     * @see AuralAttributes#setDecayFilter
     */
    public void  setDecayFilter(float frequencyCutoff) {
        attribs.decayFrequencyCutoff = frequencyCutoff;
        return;
    }

    /**
     * Set reverb diffusion.
     * This defines the echo dispersement (also referred to as 'echo density').
     * The value of this reverb parameter is expressed as a percent of the
     * audio device's minimum-to-maximum values.
     * @param diffusion percentage expressed within the range of 0.0 and 1.0 
     * @see AuralAttributes#setDiffusion
     */
    public void setDiffusion(float diffusion) {
        attribs.diffusion = diffusion;
        return;
    }

    /**
     * Set reverb density.
     * This defines the modal density (also referred to as 'spectral 
     * coloration').
     * The value of this parameter is expressed as a percent of the audio
     * device's minimum-to-maximum values for this reverb parameter.
     * @param density reverb density expressed as a percentage,
     * within the range of 0.0 and 1.0 
     * @see AuralAttributes#setDensity
     */
    public void setDensity(float density)  {
        attribs.density = density;
        return;
    }


    /**
     * Set the obstruction gain control.  This method allows for attenuating
     * sound waves traveling between the sound source and the listener
     * obstructed by objects.  Direct sound signals/waves for obstructed sound
     * source are attenuated but not indirect (reflected) waves.
     * There is no corresponding Core AuralAttributes method at this time.
     * @param index device specific reference to device driver sample
     * @param scaleFactor non-negative factor applied to direct sound gain
     */
    public void setObstructionGain(int index, float scaleFactor) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)
            sample.setObstructionGain(scaleFactor);
        return;
    }

    /**
     * Set the obstruction filter control.
     * This provides for frequencies above the given cutoff frequency
     * to be attenuated, during while the gain of an obstruction signal
     * is being calculated, at a different rate than frequencies
     * below this value.
     * There is no corresponding Core AuralAttributes method at this time.
     * @param index device specific reference to device driver sample
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied. 
     */

    public void setObstructionFilter(int index, float frequencyCutoff) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)
            sample.setObstructionFilter(frequencyCutoff);
        return;
    }

    /**
     * Set the occlusion gain control.  This method allows for attenuating
     * sound waves traveling between the sound source and the listener
     * occluded by objects.  Both direct and indirect sound signals/waves
     * for occluded sound sources are attenuated.
     * There is no corresponding Core AuralAttributes method at this time.
     * @param index device specific reference to device driver sample
     * @param  scaleFactor non-negative factor applied to direct sound gain
     */
    public void setOcclusionGain(int index, float scaleFactor) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)
            sample.setObstructionGain(scaleFactor);
        return;
    }

    /**
     * Set the occlusion filter control.
     * This provides for frequencies above the given cutoff frequency
     * to be attenuated, during while the gain of an occluded signal
     * is being calculated, at a different rate than frequencies below
     * this value.
     * There is no corresponding Core AuralAttributes method at this time.
     * @param index device specific reference to device driver sample
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied. 
     */
    public void  setOcclusionFilter(int index, float frequencyCutoff) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)
            sample.setObstructionFilter(frequencyCutoff);
        return;
    }
}
