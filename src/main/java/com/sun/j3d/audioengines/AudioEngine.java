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

/**
 * The AudioEngine Class defines an audio output device that generates
 * sound 'image' from scene graph.
 * An AudioEngine object encapsulates the AudioDevice's basic information.
 *
 * <p>
 * NOTE: AudioEngine developers should not subclass this class directly.
 * Subclass AudioEngine3DL2 instead.
 */
public abstract class AudioEngine implements AudioDevice {

    /*
     * This device's UNIX file descriptor
     */
    int fileDescriptor;

    /*
     * Type of audio output device J3D sound is played over:
     * HEADPHONE, MONO_SPEAKER, STEREO_SPEAKERS
     */
    int audioPlaybackType = HEADPHONES;
 
    /*
     * Distance from center ear (midpoint between ears) to physical speaker.
     * Default reflects distance for headphones.
     * For two speakers it is assumed that the speakers are the same
     * distance from the listener and that
     */
    float distanceToSpeaker = 0.0f;
 
    /*   
     * Angle between the vector from center ear parallel to head coordiate
     * Z axis and the vector from the center ear to the speaker.
     * For two speakers it is assumed that the speakers are placed at the
     * same angular offset from the listener.
     */
    float angleOffsetToSpeaker = 0.0f;

    /*
     *  Channels currently available
     */
    int   channelsAvailable = 8;

    /*
     *  Total number of Channels ever available
     */
    int   totalChannels = 8;

    /**
     * Construct a new AudioEngine with the specified P.E.
     * @param physicalEnvironment the physical environment object where we
     * want access to this device.
     */
    public AudioEngine(PhysicalEnvironment physicalEnvironment ) {
	physicalEnvironment.setAudioDevice(this);
    }

    /**
     * Code to initialize the device
     * @return flag: true is initialized sucessfully, false if error
     */
    public abstract boolean initialize();

    /**
     * Code to close the device
     * @return flag: true is closed sucessfully, false if error
     */
    public abstract boolean close();

    /*  
     * Audio Playback Methods
     */ 
    /**
     * Set Type of Audio Playback physical transducer(s) sound is output to.
     *     Valid types are HEADPHONE, MONO_SPEAKER, STEREO_SPEAKERS
     * @param type of audio output device
     */ 
    public void setAudioPlaybackType(int type) {
        audioPlaybackType = type;
    }

    /**
     * Get Type of Audio Playback Output Device
     * returns audio playback type to which sound is currently output
     */ 
    public int getAudioPlaybackType() {
        return audioPlaybackType;
    }

    /**
     * Set Distance from the Center Ear to a Speaker
     * @param distance from the center ear and to the speaker
     */ 
    public void setCenterEarToSpeaker(float distance) {
        distanceToSpeaker = distance;
    }

    /**
     * Get Distance from Ear to Speaker
     * returns value set as distance from listener's ear to speaker
     */ 
    public float getCenterEarToSpeaker() {
        return distanceToSpeaker;
    }
 
    /**
     * Set Angle Offset To Speaker
     * @param angle in radian between head coordinate Z axis and vector to speaker   */ 
    public void setAngleOffsetToSpeaker(float angle) {
        angleOffsetToSpeaker = angle;
    }   
 
    /**
     * Get Angle Offset To Speaker
     * returns value set as angle between vector to speaker and Z head axis
     */ 
    public float getAngleOffsetToSpeaker() {
        return angleOffsetToSpeaker;
    }   

    /**
     * Query total number of channels available for sound rendering
     * for this audio device.
     * returns number of maximum sound channels you can run with this
     * library/device-driver. 
     */  
    public int getTotalChannels() {
        // this method should be overridden by a device specific implementation
        return (totalChannels);
    }

    /**
     * Query number of channels currently available for use by the
     * returns number of sound channels currently available (number
     * not being used by active sounds.
     */  
    public int getChannelsAvailable() {
        return (channelsAvailable);
    }

    /**
     * Query number of channels that would be used to render a particular
     * sound node.
     * @param sound refenence to sound node that query to be performed on
     * returns number of sound channels used by a specific Sound node
     * @deprecated This method is now part of the Sound class
     */  
    public int getChannelsUsedForSound(Sound sound) {
        if (sound != null)
            return sound.getNumberOfChannelsUsed();
        else
            return -1;
    }
}
