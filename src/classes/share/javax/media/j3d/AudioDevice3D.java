/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.*;


/**
 * The AudioDevice3D class defines a 3D audio device that is used to set
 * sound and aural attributes.
 *<P>
 * After the application chooses the AudioDevice3D that Java3D sound
 * is to be rendered on, the Java 3D Sound Scheduler will call these
 * methods for all active sounds to render them on the audio device.
 *<P>
 * The intent is for this interface to be implemented by AudioDevice Driver
 * developers using a software or hardware sound engine of their choice.
 *<P>
 * Methods in this interface provide the Java3D Core a generic way to
 * set and query the audio device the application has chosen audio rendering
 * to be performed on.  Methods in this interface include:
 * <UL>
 *    Set up and clear the sound as a sample on the device.
 * <P>
 *    Start, stop, pause, unpause, mute, and unmute of sample on the device.
 * <P>
 *    Set parameters for each sample corresponding to the fields in the
 *    Sound node.
 * <P>
 *    Set the current active aural parameters that affect all positional samples.
 * </UL>
 * <P>
 * Sound Types
 * <P>
 * Sound types match the Sound node classes defined for Java 3D core
 * for BackgroundSound, PointSound, and ConeSound.  The type of sound
 * a sample is loaded as determines which methods affect it.
 *
 * <P>
 * Sound Data Types
 * <P>
 * Samples can be processed as streaming or buffered data.
 * Fully spatializing sound sources may require data to be buffered.
 *
 */

public interface AudioDevice3D extends AudioDevice {

     /**
      *  Specifies the sound type as background sound.
      */
     public static final int BACKGROUND_SOUND = 1;

     /**
      *  Specifies the sound type as point sound.
      */
     public static final int POINT_SOUND      = 2;

     /**
      *  Specifies the sound type as cone sound.
      */

     public static final int CONE_SOUND       = 3;

     /**
      *  Sound data specified as Streaming is not copied by the AudioDevice
      *  driver implementation.  It is up to the application to ensure that
      *  this data is continuously accessible during sound rendering.
      *  Furthermore, full sound spatialization may not be possible, for
      *  all AudioDevice3D implementations on unbuffered sound data.
      */
     public static final int STREAMING_AUDIO_DATA = 1;
     /**
      *  Sound data specified as Buffered is copied by the AudioDevice
      *  driver implementation.
      */
     public static final int BUFFERED_AUDIO_DATA = 2;


    /**
     * Accepts a reference to the current View.
     * Passes reference to current View Object.  The PhysicalEnvironment
     * parameters (with playback type and speaker placement), and the
     * PhysicalBody parameters (position/orientation of ears)
     * can be obtained from this object, and the transformations to/from
     * ViewPlatform coordinate (space the listener's head is in) and
     * Virtual World coordinates (space sounds are in).
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param reference the current View
     */
    public abstract void  setView(View reference);

    /**
     * Accepts a reference to the MediaContainer
     * which contains a reference to sound data and information about the
     * type of data it is.  A "sound type" input parameter denotes if the
     * Java 3D sound associated with this sample is a Background, Point, or
     * Cone Sound node.
     * Depending on the type of MediaContainer the sound data is and on the
     * implementation of the AudioDevice used, sound data preparation could
     * consist of opening, attaching, or loading sound data into the device.
     * Unless the cached flag is true, this sound data should NOT be copied,
     * if possible, into host or device memory.
     *<P>
     * Once this preparation is complete for the sound sample, an AudioDevice
     * specific index, used to reference the sample in future method calls,
     * is returned.  All the rest of the methods described below require
     * this index as a parameter.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param soundType defines the type of Sound Node: Background, Point, and
     * Cone
     * @param soundData reference to MediaContainer sound data and cached flag
     * @return device specific sample index used for referencing this sound
     */
    public abstract int   prepareSound(int soundType, MediaContainer soundData);

    /**
     * Requests that the AudioDevice free all
     * resources associated with sample with index id.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void   clearSound(int index);

    /**
     * Returns the duration in milliseconds of the sound sample,
     * if this information can be determined.
     * For non-cached
     * streams, this method returns Sound.DURATION_UNKNOWN.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @return sound duration in milliseconds if this can be determined,
     * otherwise (for non-cached streams) Sound.DURATION_UNKNOWN is returned
     */
    public abstract long   getSampleDuration(int index);

    /**
     *
     * Retrieves the number of channels (on executing audio device) that
     * this sound is using, if it is playing, or is expected to use
     * if it were begun to be played.  This form of this method takes the
     * sound's current state (including whether it is muted or unmuted)
     * into account.
     *<P>
     * For some AudioDevice3D implementations:
     *<UL>
     *     Muted sound take channels up on the systems mixer (because they're
     *         rendered as samples playing with gain zero.
     *<P>
     *     A single sound could be rendered using multiple samples, each taking
     *         up mixer channels.
     *</UL>
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @return number of channels used by sound if it were playing
     */
    public abstract int    getNumberOfChannelsUsed(int index);

    /**
     *
     * Retrieves the number of channels (on executing audio device) that
     * this sound is using, if it is playing, or is projected to use if
     * it were to be started playing.  Rather than using the actual current
     * muted/unmuted state of the sound, the muted parameter is used in
     * making the determination.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param muted flag to use as the current muted state ignoring current
     * mute state
     * @return number of channels used by sound if it were playing
     */
    public abstract int    getNumberOfChannelsUsed(int index, boolean muted);

    /**
     * Begins a sound playing on the AudioDevice.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @return flag denoting if sample was started; 1 if true, 0 if false
     */
    public abstract int    startSample(int index);

    /**
     * Returns the system time of when the sound
     * was last "started".  Note that this start time will be as accurate
     * as the AudioDevice implementation can make it - but that it is not
     * guaranteed to be exact.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @return system time in milliseconds of the last time sound was started
     */
    public abstract long   getStartTime(int index);

    /**
     * Stops the sound on the AudioDevice.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * associated with sound data to be played
     * @return flag denoting if sample was stopped; 1 if true, 0 if false
     */
    public abstract int    stopSample(int index);

    /**
     * Sets the overall gain scale factor applied to data associated with this
     * source to increase or decrease its overall amplitude.
     * The gain scale factor value passed into this method is the combined value
     * of the Sound node's Initial Gain and the current AuralAttribute Gain
     * scale factors.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param scaleFactor amplitude (gain) scale factor
     */
    public abstract void   setSampleGain(int index, float scaleFactor);

    /**
     * Sets a sound's loop count.
     * A full description of this parameter and how it is used is in
     * the documentation for Sound.setLoop.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param count number of times sound is looped during play
     * @see Sound#setLoop
     */
    public abstract void   setLoop(int index, int count);

    /**
     * Passes a reference to the concatenated transformation to be applied to
     * local sound position and direction parameters.
     * <P>
     * This method should only be called by Java3D Core and NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param trans transformation matrix applied to local coordinate parameters
     */
    public abstract void  setVworldXfrm(int index, Transform3D trans);


    /**
     * Sets this sound's location (in Local coordinates) from specified
     * Point. The form of the position parameter matches those of the PointSound
     * method of the same name.
     * A full description of this parameter and how it is used is in
     * the documentation for PointSound class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param position location of Point or Cone Sound in Virtual World
     * coordinates
     * @see PointSound#setPosition(float x, float y, float z)
     * @see PointSound#setPosition(Point3f position)
     */
    public abstract void   setPosition(int index, Point3d position);

    /**
     * Sets this sound's distance gain elliptical attenuation (not
     * including filter cutoff frequency) by defining corresponding
     * arrays containing distances from the sound's origin and gain
     * scale factors applied to all active positional sounds.
     * Gain scale factor is applied to sound based on the distance
     * the listener
     * is from sound source.
     * These attenuation parameters are ignored for BackgroundSound nodes.
     * The back attenuation parameter is ignored for PointSound nodes.
     * <P>
     * The form of the attenuation parameters match that of the ConeSound method
     * of the same name.
     * A full description of this parameter and how it is used is in
     * the documentation for ConeSound class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param frontDistance defines an array of distance along positive axis
     * through which ellipses pass
     * @param frontAttenuationScaleFactor gain scale factors
     * @param backDistance defines an array of distance along the negative axis
     * through which ellipses pass
     * @param backAttenuationScaleFactor gain scale factors
     * @see ConeSound#setDistanceGain(float[] frontDistance, float[] frontGain,
     * float[] backDistance, float[] backGain)
     * @see ConeSound#setDistanceGain(Point2f[] frontAttenuation,
     * Point2f[] backAttenuation)
     */
    public abstract void setDistanceGain(int index,
              double[] frontDistance, float[]  frontAttenuationScaleFactor,
              double[] backDistance, float[]  backAttenuationScaleFactor);
    /**
     * Sets this sound's direction from the local coordinate vector provided.
     * The form of the direction parameter matches that of the ConeSound method
     * of the same name.
     * A full description of this parameter and how it is used is in
     * the documentation for the ConeSound class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param direction the new direction vector in local coordinates
     * @see ConeSound#setDirection(float x, float y, float z)
     * @see ConeSound#setDirection(Vector3f direction)
     */
    public abstract void setDirection(int index, Vector3d direction);

    /**
     * Sets this sound's angular gain attenuation (including filter)
     * by defining corresponding arrays containing angular offsets from
     * the sound's axis, gain scale factors, and frequency cutoff applied
     * to all active directional sounds.
     * Gain scale factor is applied to sound based on the angle between the
     * sound's axis and the ray from the sound source origin to the listener.
     * The form of the attenuation parameter matches that of the ConeSound
     * method of the same name.
     * A full description of this parameter and how it is used is in
     * the documentation for the ConeSound class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     * @param filterType describes type (if any) of filtering defined by attenuation
     * @param angle array containing angular distances from sound axis
     * @param attenuationScaleFactor array containing gain scale factor
     * @param filterCutoff array containing filter cutoff frequencies.
     * The filter values for each tuples can be set to Sound.NO_FILTER.
     * @see ConeSound#setAngularAttenuation(float[] distance, float[] gain,
     * float[] filter)
     * @see ConeSound#setAngularAttenuation(Point3f[] attenuation)
     * @see ConeSound#setAngularAttenuation(Point2f[] attenuation)
     */
    public abstract void setAngularAttenuation(int index, int filterType,
          double[] angle, float[] attenuationScaleFactor, float[] filterCutoff);

    /**
     * Changes the speed of sound factor.
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param rolloff atmospheric gain scale factor (changing speed of sound)
     * @see AuralAttributes#setRolloff
     */
    public abstract void setRolloff(float rolloff);

    /**
     * Sets the Reflective Coefficient scale factor applied to distinct
     * low-order early reflections of sound off the surfaces in the region
     * defined by the current listening region.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param coefficient reflection/absorption factor applied to reverb
     * @see AuralAttributes#setReflectionCoefficient
     */
    public abstract void setReflectionCoefficient(float coefficient);

    /**
     * Sets the reverberation delay time.
     * In this form,  while reverberation is being rendered, the parameter
     * specifies the delay time between each order of late reflections
     * explicitly given in milliseconds.
     * A value for delay time of 0.0 disables
     * reverberation.
     * <P>
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param reverbDelay time between each order of late reflection
     * @see AuralAttributes#setReverbDelay(float reverbDelay)
     */
    public abstract void setReverbDelay(float reverbDelay);

    /**
     * Sets the reverberation order of reflections.
     * The reverbOrder parameter specifies the number of times reflections are added to
     * reverberation being calculated. A value of -1 specifies an unbounded
     * number of reverberations.
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param reverbOrder number of times reflections added to reverb signal
     * @see AuralAttributes#setReverbOrder
     */
    public abstract void setReverbOrder(int reverbOrder);

    /**
     * Sets Distance Filter corresponding arrays containing distances and
     * frequency cutoff applied to all active positional sounds.
     * Gain scale factor is applied to sound based on the distance the listener
     * is from the sound source.
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param filterType denotes the type of filtering to be applied
     * @param distance array of offset distances from sound origin
     * @param filterCutoff array of frequency cutoff
     * @see AuralAttributes#setDistanceFilter(float[] distance,
     *       float[] frequencyCutoff)
     * @see AuralAttributes#setDistanceFilter(Point2f[] attenuation)
     */
    public abstract void setDistanceFilter(int filterType,
              double[] distance, float[]  filterCutoff);

    /**
     * Specifies a scale factor applied to the frequency (or
     * wavelength).  A value less than 1.0 will result of slowing the playback
     * rate of the sample.  A value greater than 1.0 will increase the playback
     * rate.
     * This parameter is also used to expand or contract the usual
     * frequency shift applied to the sound source due to Doppler effect
     * calculations. Valid values are >= 0.0.
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param frequencyScaleFactor factor applied to change of frequency
     * @see AuralAttributes#setFrequencyScaleFactor
     */
    public abstract void setFrequencyScaleFactor(float frequencyScaleFactor);

    /**
     * Sets the Velocity scale factor applied during Doppler Effect calculation.
     * This parameter specifies a scale factor applied to the velocity of sound
     * relative to the listener's position and movement in relation to the sound's
     * position and movement.  This scale factor is multipled by the calculated
     * velocity portion of the Doppler effect equation used during sound rendering.
     * A full description of this parameter and how it is used is in
     * the documentation for the AuralAttributes class.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param velocityScaleFactor applied to velocity of sound in relation
     * to listener
     * @see AuralAttributes#setVelocityScaleFactor
     */
    public abstract void setVelocityScaleFactor(float velocityScaleFactor);

    /**
     * Makes the sample 'play silently'.
     * This method implements (as efficiently as possible) the muting
     * of a playing sound sample.  Ideally this is implemented by
     * stopping a sample and freeing channel resources (rather than
     * just setting the gain of the sample to zero).
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void muteSample(int index);

    /**
     * Makes a silently playing sample audible.
     * In the ideal, this restarts a muted sample by offset from the
     * beginning by the number of milliseconds since the time the sample
     * began playing (rather than setting gain to current non-zero gain).
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void unmuteSample(int index);

    /**
     * Temporarily stops a cached sample from playing without resetting the
     * sample's current pointer back to the beginning of the sound data so
     * that it can be unpaused at a later time from the same location in the
     * sample when the pause was initiated.  Pausing a streaming, non-cached
     * sound sample will be treated as a mute.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void pauseSample(int index);

    /**
     * Restarts the paused sample from the location in the sample where
     * paused.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void unpauseSample(int index);

    /**
     *
     * Explicitly updates a Sample.
     * This method is called when a Sound is to be explicitly updated.
     * It is only called when all a sounds parameters are known to have
     * been passed to the audio device.  In this way, an implementation
     * can choose to perform lazy-evaluation of a sample, rather than
     * updating the rendering state of the sample after every individual
     * parameter changed.
     * This method can be left as a null method if the implementor so chooses.
     * <P>
     * This method should only be called by Java3D Core and  NOT by any application.
     * @param index device specific reference number to device driver sample
     */
    public abstract void updateSample(int index);

}
