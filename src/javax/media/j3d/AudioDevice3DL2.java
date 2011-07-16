/*
 * $RCSfile$
 *
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;


/**
 * Extends AudioDevice3D to include reverb and environmental audio parameters
 * that are defined in the MIDI Manufactures' Association Interactive Audio
 * Special Interest Group (MMA IASIG) Level 2 Specification.
 *<P>
 * The reverberation methods of AudioDevice3DL2 interface augment the 
 * reverberation methods defined in AudioDevice3D.
 *<P>
 * The intent is for this interface to be implemented by AudioDevice Driver
 * developers using a software or hardware sound engine of their choice.
 *<P>
 * Methods in this interface provide the Java3D Core a generic way to
 * set and query the audio device the application has chosen audio rendering
 * to be performed on.
 *<P>
 * The non-query methods of this interface should only be called by
 * an application if the AudioDevice instance 
 * is not referenced by any PhysicalEnvironment
 * explicitly with .setAudioDevice() or implicitly through Universe
 * utility method  in which case these are called by Core Java 3D 
 * Sound classes and Sound Scheduler thread(s).
 *<P>
 * After the application chooses the AudioDevice3DL2 implementation
 * that Java3D sound is to be rendered on, the Java 3D Sound Scheduler
 * will call these methods for all active sounds to render them on the
 * audio device.
 *<P>
 * The AudioDevice3DL2 methods should not be call by any application if the
 * audio device is associated with a Physical Environment and thus used by
 * Java3D Core.
 *<P>
 * Filtering for this extended AudioDevice interface is defined uniformly as
 * a simple low-pass filter defined by a cutoff frequency. This will allow the
 * same type of high-frequency attenuation that the MMA IASIG Level 2 filtering
 * model with its 'reference frequency' and 'attenuation ratio' parameters
 * affords.  Use of a cutoff frequency is consistent with the filtering type
 * for distance and angular attenuation for ConeSound and AuralAttributes.
 * The filter methods will likely be overloaded in some future extension of this
 * interface.
 *
 * @see Sound
 * @see AuralAttributes
 * @see AudioDevice3D
 * @since Java 3D 1.3
 */

public interface AudioDevice3DL2 extends AudioDevice3D {

    /**
     * Pause audio device engine (thread/server) without closing the device.
     * Causes all cached sounds to be paused and all streaming sounds to be
     * stopped.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * This method will be implicitly called when View (associated with this
     * device) is deactivated.
     */
    public abstract void pause();

    /**  
     * Resumes audio device engine (if previously paused) without reinitializing
     * the device.
     * Causes all paused cached sounds to be resumed and all streaming sounds
     * restarted. 
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * This method will be implicitly called when View (associated with this  
     * device) is actived. 
     */  
    public abstract void resume();

    /**
     * Set overall gain control of all sounds playing on the audio device.
     * Default: 1.0f = no attenuation.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param scaleFactor scale factor applied to calculated amplitudes for
     * all sounds playing on this device
     */
    public abstract void setGain(float scaleFactor);

    /**
     * Set scale factor applied to sample playback rate for a particular sound
     * associated with the audio device.
     * Changing the device sample rate affects both the pitch and speed.
     * This scale factor is applied to ALL sound types.
     * Changes (scales) the playback rate of a sound independent of
     * Doppler rate changes.
     * Default: 1.0f = original sample rate is unchanged
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param sampleId device specific reference number to device driver sample
     * @param scaleFactor non-negative factor applied to calculated 
     * amplitudes for all sounds playing on this device
     */
    public abstract void  setRateScaleFactor(int sampleId, float scaleFactor);


    /**
     * Set late reflection (referred to as 'reverb') attenuation.
     * This scale factor is applied to iterative, indistinguishable
     * late reflections that constitute the tail of reverberated sound in
     * the aural environment.
     * This parameter, along with the early reflection coefficient, defines
     * the reflective/absorptive characteristic of the surfaces in the
     * current listening region.
     * A coefficient value of 0 disables reverberation.
     * Valid values of parameters range from 0.0 to 1.0.
     * Default: 0.0f.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param coefficient late reflection attenuation factor 
     * @see AuralAttributes#setReverbCoefficient
     */
    public abstract void setReverbCoefficient(float coefficient);


    /**  
     * Sets the early reflection delay time.
     * In this form,  the parameter specifies the delay time between each order
     * of reflection (while reverberation is being rendered) explicitly given
     * in milliseconds. 
     * Valid values are non-negative floats.
     * There may be limitations imposed by the device on how small or large this
     * value can be made.
     * A value of 0.0 would result in early reflections being added as soon as
     * possible after the sound begins.
     * Default = 20.0 milliseconds.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param reflectionDelay time between each order of early reflection
     * @see AuralAttributes#setReflectionDelay
     */ 
    public abstract void  setReflectionDelay(float reflectionDelay);

    /**
     * Set reverb decay time.
     * Defines the reverberation decay curve.
     * Default: 1000.0 milliseconds.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param time decay time in milliseconds
     * @see AuralAttributes#setDecayTime
     */
    public abstract void  setDecayTime(float time);

    /**
     * Set reverb decay filter.
     * This provides for frequencies above the given cutoff frequency to be
     * attenuated during reverb decay at a different rate than frequencies
     * below this value.  Thus, defining a different reverb decay curve for 
     * frequencies above the cutoff value.
     * Default: 1.0 decay is uniform for all frequencies.
     * <P>
     * There is no corresponding Core AuralAttributes method at this time.
     * Until high frequency attenuation is supported by new Core API,
     * this will be set by the Core with the value 1.0.
     * It is highly recommended that this method should NOT be
     * called by any application if the audio device is associated with 
     * a Physical Environment used by Java3D Core.
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied.
     * @see AuralAttributes#setDecayFilter
     */
    public abstract void  setDecayFilter(float frequencyCutoff);

    /**
     * Set reverb diffusion.
     * This defines the echo dispersement (also referred to as 'echo density').
     * The value of this reverb parameter is expressed as a percent of the
     * audio device's minimum-to-maximum values.
     * Default: 1.0f - maximum diffusion on device.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param diffusion percentage expressed within the range of 0.0 and 1.0 
     * @see AuralAttributes#setDiffusion
     */
    public abstract void  setDiffusion(float diffusion);

    /**
     * Set reverb density.
     * This defines the modal density (also referred to as 'spectral 
     * coloration').
     * The value of this parameter is expressed as a percent of the audio
     * device's minimum-to-maximum values for this reverb parameter.
     * Default: 1.0f - maximum density on device.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should NOT be called by any application if the audio device
     * is associated with a Physical Environment used by Java3D Core.
     * @param density reverb density expressed as a percentage,
     * within the range of 0.0 and 1.0 
     * @see AuralAttributes#setDensity
     */
    public abstract void setDensity(float density); 


    /**
     * Set the obstruction gain control.  This method allows for attenuating
     * sound waves traveling between the sound source and the listener
     * obstructed by objects.  Direct sound signals/waves for obstructed sound
     * source are attenuated but not indirect (reflected) waves.
     * Default: 1.0 - gain is not attenuated; obstruction is not occurring.
     * <P>
     * There is no corresponding Core AuralAttributes method at this time.
     * Even so, this method should NOT be called by any application if the
     * audio device is associated with a Physical Environment used by Java3D
     * Core.
     * @param sampleId device specific reference number to device driver sample
     * @param scaleFactor non-negative factor applied to direct sound gain
     */
    public abstract void  setObstructionGain(int sampleId, float scaleFactor);

    /**
     * Set the obstruction filter control.
     * This provides for frequencies above the given cutoff frequency
     * to be attenuated, during while the gain of an obstruction signal
     * is being calculated, at a different rate than frequencies
     * below this value.
     * Default: 1.0 - filtering is uniform for all frequencies.
     * <P>
     * There is no corresponding Core AuralAttributes method at this time.
     * Until high frequency attenuation is supported by new Core API
     * this will be set by the Core with the value 1.0.
     * It is highly recommended that this method should NOT be
     * called by any application if the audio device is associated with
     * a Physical Environment used by Java3D Core.
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied. 
     */

    public abstract void  setObstructionFilter(int sampleId, float frequencyCutoff);

    /**
     * Set the occlusion gain control.  This method allows for attenuating
     * sound waves traveling between the sound source and the listener
     * occluded by objects.  Both direct and indirect sound signals/waves
     * for occluded sound sources are attenuated.
     * Default: 1.0 - gain is not attenuated; occlusion is not occurring.
     * <P>
     * There is no corresponding Core AuralAttributes method at this time.
     * Even so, this method should NOT be called by any application if the
     * audio device is associated with a Physical Environment used by Java3D
     * Core.
     * @param sampleId device specific reference number to device driver sample
     * @param  scaleFactor non-negative factor applied to direct sound gain
     */
    public abstract void setOcclusionGain(int sampleId, float scaleFactor);

    /**
     * Set the occlusion filter control.
     * This provides for frequencies above the given cutoff frequency
     * to be attenuated, during while the gain of an occluded signal
     * is being calculated, at a different rate than frequencies below
     * this value.
     * Default: 1.0 - filtering is uniform for all frequencies.
     * <P>
     * There is no corresponding Core AuralAttributes method at this time.
     * Until high frequency attenuation is supported by new Core API
     * this will be set by the Core with the value 1.0.
     * It is highly recommended that this method should NOT be
     * called by any application if the audio device is associated with
     * a Physical Environment used by Java3D Core.
     * @param frequencyCutoff value of frequencies in Hertz above which a
     * low-pass filter is applied. 
     */
    public abstract void  setOcclusionFilter(int sampleId, float frequencyCutoff);
}
