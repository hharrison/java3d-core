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
 * The AudioEngine3D Class defines an audio output device that generates
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
 * <p>
 * NOTE: AudioEngine developers should not subclass this class directly.
 * Subclass AudioEngine3DL2 instead.
 */

public abstract class AudioEngine3D extends AudioEngine implements AudioDevice3D
{
    /*
     *  Identifiers of sample associated with sound source
     *  This array grows as the AudioDevice3D implementation requires it larger.
     */  
    protected ArrayList samples = new ArrayList(64);

    /**
     *  Current View sound is being rendered
     */  
    protected View  currentView = (View)null;

    /*
     * current Aural attribute Parameters
     */
    protected AuralParameters attribs = new AuralParameters();

    /**
     * Construct a new AudioEngine with the specified PhysicalEnvironment.
     * @param physicalEnvironment the physical environment object where we
     * want access to this device.
     */  
    public AudioEngine3D(PhysicalEnvironment physicalEnvironment ) {
        super(physicalEnvironment);
    }

    /*
     *
     * Methods that affect AudioEngine3D fields that are NOT associated 
     * with a specific sound sample
     *
     */

    /**
     * Save a reference to the current View object.
     * @param reference to current view object
     */
    public void  setView(View reference) {
        currentView = reference;
        return;
    }
    /**
     * Get reference to the current View object.
     * @return reference to current view object
     */
    public View getView() {
        return (currentView);
    }

    /*
     *
     *  Methods explicitly affect sound rendering and that require
     *  audio device specific methods that override this class.
     *
     */

    /**
     * Prepare Sound in device.
     * Makes sound assessible to device - in this case attempts to load sound
     * Stores sound type and data.
     * @param soundType denotes type of sound: Background, Point or Cone
     * @param soundData descrition of sound source data
     * @return index into sample vector of Sample object for sound
     */  
    public int   prepareSound(int soundType, MediaContainer soundData) {
        // This method must be overridden by device specific implementation
        return Sample.NULL_SAMPLE;
    }

    /**
     * Clear Sound.
     * Removes/clears associated sound data with this sound source node
     * @param index device specific reference number to device driver sample
     */  
    public abstract void clearSound(int index);

    /**
     * Set the transform for local to virtual world coordinate space
     * @param index device specific reference number to device driver sample
     * @param trans is a reference to virtual world composite transform
     */
    public void  setVworldXfrm(int index, Transform3D trans) {
         Sample sample = (Sample)getSample(index);
         if (sample != null)
             sample.vworldXfrm.set(trans);
         return;
    }
    /**
     * Start sample playing on audio device
     * @param index device specific reference number to device driver sample
     * @return status: < 0 denotes an error
     */
    public abstract int startSample(int index);

    /**
     * Stop sample playing on audio device
     * @param index device specific reference number to device driver sample
     * @return status: < 0 denotes an error
     */
    public abstract int stopSample(int index);

    /**
     * Update sample. 
     * Implies that some parameters affecting rendering have been modified.
     * @param index device specific reference number to device driver sample
     */
    // TODO: The update method exists on a TEMPORARY basis.
    public abstract void updateSample(int index);

    /**
     * Mute sample. 
     * @param index device specific reference number to device driver sample
     */
    public abstract void muteSample(int index);

    /**
     * Unmute sample. 
     * @param index device specific reference number to device driver sample
     */
    public abstract void unmuteSample(int index);

    /**
     * Pause sample. 
     * @param index device specific reference number to device driver sample
     */
    public abstract void pauseSample(int index);

    /**
     * Unpause sample. 
     * @param index device specific reference number to device driver sample
     */
    public abstract void unpauseSample(int index);
    
    /*
     *
     *  Methods that affect fields associated with the sound sample
     *  and that may cause implicit rendering.
     *
     */
    /**
     * Set gain scale factor applied to sample. 
     * @param index device specific reference number to device driver sample
     * @param scaleFactor floating point multiplier applied to sample amplitude
     */
    public void  setSampleGain(int index, float scaleFactor) {
        Sample sample = (Sample)getSample(index);
        if (sample != null) 
            sample.setGain(scaleFactor);
        return;
    }

    /** 
     * Set number of times sample is looped.
     * @param index device specific reference number to device driver sample
     * @param count number of times sample is repeated
     */
    public void   setLoop(int index, int count) {
        Sample sample = (Sample)getSample(index);
        if (sample != null) 
            sample.setLoopCount(count);
        return;
    }

    /**
     * Set location of sample.
     * @param index device specific reference number to device driver sample
     * @param position point location in virtual world coordinate of sample
     */
    public void  setPosition(int index, Point3d position) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            sample.setPosition(position);
        return;
    }

    /* Set elliptical distance attenuation arrays applied to sample amplitude.
     * @param index device specific reference number to device driver sample
     * @param frontDistance defines an array of distance along the position axis
     * thru which ellipses pass
     * @param frontAttenuationScaleFactor gain scale factors
     * @param backDistance defines an array of distance along the negative axis
     * thru which ellipses pass
     * @param backAttenuationScaleFactor gain scale factors
     */
    public void setDistanceGain(int index, 
              double[] frontDistance, float[] frontAttenuationScaleFactor,
              double[] backDistance, float[] backAttenuationScaleFactor) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            sample.setDistanceGain(frontDistance, frontAttenuationScaleFactor, 
                                   backDistance, backAttenuationScaleFactor);
        return;
    }

    /**
     * Set direction vector of sample.
     * @param index device specific reference number to device driver sample
     * @param direction vector in virtual world coordinate.
     */
    public void setDirection(int index, Vector3d direction) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            sample.setDirection(direction);
        return;
    }

    /**
     * Set angular attenuation arrays affecting angular amplitude attenuation
     * and angular distance filtering.
     * @param index device specific reference number to device driver sample
     * @param filterType denotes type of filtering (on no filtering) applied
     * to sample.
     * @param angle array containing angular distances from sound axis
     * @param attenuationScaleFactor array containing gain scale factor
     * @param filterCutoff array containing filter cutoff frequencies.
     * The filter values for each tuples can be set to Sound.NO_FILTER.
     */
    public void setAngularAttenuation(int index, int filterType, 
          double[] angle, float[] attenuationScaleFactor, float[] filterCutoff) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            sample.setAngularAttenuation(filterType, angle, 
                       attenuationScaleFactor, filterCutoff);
        return;
    }

    /**
     * Set rolloff value for current aural attribute applied to all samples.
     * @param rolloff scale factor applied to standard speed of sound.
     */
    public void setRolloff(float rolloff) {
        attribs.rolloff = rolloff;
        return;
    }

    /**
     * Set reverberation surface reflection coefficient value for current aural
     * attribute applied to all samples.
     * @param coefficient applied to amplitude of reverbation added at each
     * iteration of reverb processing.
     */
    public void setReflectionCoefficient(float coefficient) {
        attribs.reflectionCoefficient = coefficient;
        return;
    }

    /**
     * Set reverberation delay time for current aural attribute applied to 
     * all samples.
     * @param reverbDelay amount of time in millisecond between each
     * iteration of reverb processing.
     */
    public void setReverbDelay(float reverbDelay) {
        attribs.reverbDelay = reverbDelay;
        return;
    }

    /**
     * Set reverberation order for current aural attribute applied to all 
     * samples.
     * @param reverbOrder number of times reverb process loop is iterated.
     */
    public void setReverbOrder(int reverbOrder) {
        attribs.reverbOrder = reverbOrder;
        return;
    }

    /**
     * Set distance filter for current aural attribute applied to all samples.
     * @param filterType denotes type of filtering (on no filtering) applied
     * to all sample based on distance between listener and sound.
     * @param dist is an attenuation array of distance and low-pass filter values.
     */
    public void setDistanceFilter(int filterType, 
              double[] dist, float[]  filterCutoff) {
        attribs.setDistanceFilter(filterType, dist, filterCutoff);
        return;
    }

    /**
     * Set frequency scale factor for current aural attribute applied to all 
     * samples.
     * @param scaleFactor frequency scale factor applied to samples normal
     * playback rate.
     */
    public void setFrequencyScaleFactor(float scaleFactor) {
        attribs.frequencyScaleFactor = scaleFactor;
        return;
    }
    /**
     * Set velocity scale factor for current aural attribute applied to all 
     * samples when Doppler is calculated.
     * @param scaleFactor scale factor applied to postional samples'
     * listener-to-soundSource velocity.
     * playback rate.
     */
    public void setVelocityScaleFactor(float scaleFactor) {
        attribs.velocityScaleFactor = scaleFactor;
        return;
    }

    /**
     * Get number of channels used by a particular sample on the audio device.
     * @param index device specific reference number to device driver sample
     * @return number of channels currently being used by this sample.
     */
    public int    getNumberOfChannelsUsed(int index) {
        // This method must be overridden by device specific implementation
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            return (sample.getNumberOfChannelsUsed());
        else
            return 0;
    }

    /**
     * Get number of channels that would be used by a particular sample on
     * the audio device given the mute flag passed in as a parameter.
     * @param index device specific reference number to device driver sample
     * @param muteFlag denotes the mute state to assume while executing this
     * query. This mute value does not have to match the current mute state
     * of the sample.
     * @return number of channels that would be used by this sample if it
     * were playing.
     */
    public int    getNumberOfChannelsUsed(int index, boolean muteFlag) {
        // This method must be overridden by device specific implementation
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            return (sample.getNumberOfChannelsUsed());
        else
            return 0;
    }

    /**
     * Get length of time a sample would play if allowed to play to completion.
     * @param index device specific reference number to device driver sample
     * @return length of sample in milliseconds
     */
    public long  getSampleDuration(int index) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            return (sample.getDuration());
        else
            return 0L;
    }

    /**
     * Get time this sample begun playing on the audio device.
     * @param index device specific reference number to device driver sample
     * @return system clock time sample started
     */
    public long  getStartTime(int index) {
        Sample sample = (Sample)getSample(index);
        if (sample != null)  
            return (sample.getStartTime());
        else
            return 0L;
    }

    /**
     * Get reference to the array list of samples
     * @return reference to samples list
     * @deprecated unsafe to get reference to samples list with this method.
     * It's better to directly reference samples list within a synchronized
     * block which also contains calls to .getSample(index).
     */
    protected ArrayList getSampleList() {
        return (samples);
    }

    public int getSampleListSize() {
        return (samples.size());
    }

    /**
     * Get specific sample from indexed sample list
     * Checks for valid index before attempting to get sample from list.
     * @param index device specific reference number to device driver sample
     * @return reference to sample; returns null if index out of range.
     *
     * @since Java 3D 1.2.1
     */  
    public Sample getSample(int index) {
        synchronized(samples) {
            if ((index >= 0) && (index < samples.size())) {
                Sample sample = (Sample)samples.get(index);
                return (sample);
            }
            else
                return null;
        }
    }

    /*
     * Get reference to current aural attribute parameters associated with
     * this audio device.
     * @return reference to current aural attribute parameters
     */
    public AuralParameters getAuralParameters() {
        return (attribs);
    }
}
