/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 */

package org.jogamp.java3d;


/**
 * The AudioDevice Class defines and encapsulates the
 * audio device's basic information and characteristics.
 * <P>
 * A Java3D application running on a particular machine could have one of
 * several options available to it for playing the audio image created by the
 * sound renderer. Perhaps the machine Java3D is executing on has more than
 * one sound card (e.g., one that is a Wave Table Synthesis card and the other
 * with accelerated sound spatialization hardware). Furthermore, suppose there
 * are Java3D audio device drivers that execute Java3D audio methods on each of
 * these specific cards. In such a case the application would have at least two
 * audio device drivers through which the audio could be produced. For such a
 * case the Java3D application must choose the audio device driver with which
 * sound rendering is to be performed. Once this audio device is chosen, the
 * application can additionally select the type of audio playback type the
 * rendered sound image is to be output on. The playback device (headphones or
 * speaker(s)) is physically connected to the port the selected device driver
 * outputs to.
 *<P>
 * AudioDevice Interface
 *<P>
 *<UL> The selection of this device driver is done through methods in the
 *     PhysicalEnvironment object - see PhysicalEnvironment class.
 *     The application would query how many audio devices are available. For
 *     each device, the user can get the AudioDevice object that describes it
 *     and query its characteristics. Once a decision is made about which of
 *     the available audio devices to use for a PhysicalEnvironment, the
 *     particular device is set into this PhysicalEnvironment's fields. Each
 *     PhysicalEnvironment object may use only a single audio device.
 *<P>
 *     The AudioDevice object interface specifies an abstract input device
 *     that creators of Java3D class libraries would implement for a particular
 *     device. Java3D's uses several methods to interact with specific devices.
 *     Since all audio devices implement this consistent interface, the user
 *     could have a portable means of initialize, set particular audio device
 *     elements and query generic characteristics for any audio device.
 *<P>
 *Initialization
 *<P><UL>
 *  Each audio device driver must be initialized.
 *  The chosen device driver should be initialized before any Java3D
 *  Sound methods are executed because the implementation of the Sound
 *  methods, in general, are potentially device driver dependent.</UL>
 *<P>
 * Audio Playback Type
 *<P><UL>
 *  These methods set and retrieve the audio playback type used to output
 *  the analog audio from rendering Java3D Sound nodes.
 *  The audio playback type specifies that playback will be through:
 *  stereo headphones, a monaural speaker,  or a pair of speakers.
 *  For the stereo speakers, it is assumed that the two output speakers are
 *  equally distant from the listener, both at same angle from the head
 *  axis (thus oriented symmetrically about the listener), and at the same
 *  elevation.
 *  The type of playback chosen affects the sound image generated.
 *  Cross-talk cancellation is applied to the audio image if playback over
 *  stereo speakers is selected.</UL>
 *<P>
 * Distance to Speaker
 *<P><UL>
 * These methods set and retrieve the distance in meters from the center
 * ear (the midpoint between the left and right ears) and one of the
 * speakers in the listener's environment. For monaural speaker playback,
 * a typical distance from the listener to the speaker in a workstation
 * cabinet is 0.76 meters. For stereo speakers placed at the sides of the
 * display, this might be 0.82 meters.</UL>
 *<P>
 *  Angular Offset of Speakers
 *<P><UL>
 *  These methods set and retrieve the angle in radians between the vectors
 *  from the center ear to each of the speaker transducers and the vectors
 *  from the center ear parallel to the head coordinate's Z axis. Speakers
 *  placed at the sides of the computer display typically range between
 *  0.28 to 0.35 radians (between 10 and 20 degrees).</UL>
 *<P>
 *  Device Driver Specific Data
 *<P><UL>
 *  While the sound image created for final output to the playback system
 *  is either only mono or stereo (for this version of Java3D) most device
 *  driver implementations will mix the left and right image signals
 *  generated for each rendered sound source before outputting the final
 *  playback image. Each sound source will use N input channels of this
 *  internal mixer. Each implemented Java3D audio device driver will have
 *  its own limitations and driver-specific characteristics. These include
 *  channel availability and usage (during rendering).  Methods for
 *  querying these device-driver specific characteristics are provided.</UL></UL>
 *<P>
 * Instantiating and Registering a New Device
 *<P>
 *<UL> A browser or applications developer must instantiate whatever system-
 *     specific audio devices that he or she needs and that exist on the system.
 *     This device information typically exists in a site configuration file.
 *     The browser or application will instantiate the physical environment as
 *     requested by the end-user.
 *<P>
 *     The API for instantiating devices is site-specific, but it consists of
 *     a device object with a constructor and at least all of the methods
 *     specified in the AudioDevice interface.
 *<P>
 *     Once instantiated, the browser or application must register the device
 *     with the Java3D sound scheduler by associating this device with a
 *     PhysicalEnvironment.  The setAudioDevice method introduces new devices
 *     to the Java3D environment and the allAudioDevices method produces an
 *     enumeration that allows examining all available devices within a Java3D
 *     environment. See PhysicalEnvironment class for more details.</UL>
 * <P>
 * General Rules for calling AudioDevice methods:
 * It is illegal for an application to call any non-query AudioDevice method
 * if the AudioDevice is created then explicitly assigned to a
 * PhysicalEnvironment using PhysicalEnvironment.setAudioDevice();
 * When either PhysicalEnvironment.setAudioDevice() is called - including
 * when implicitly called by SimpleUniverse.getViewer().createAudioDevice()
 * - the Core creates a SoundScheduler thread which makes calls to
 * the AudioDevice.
 * <P>
 * If an application creates it's own instance of an AudioDevice and
 * initializes it directly, rather than using PhysicalEnvironment.
 * setAudioDevice(), that application may make <i>any</i> AudioDevice3D methods calls
 * without fear of the Java 3D Core also trying to control the AudioDevice.
 * Under this condition it is safe to call AudioDevice non-query methods.
 */

public interface AudioDevice {

    /** *************
     *
     * Constants
     *
     ****************/
    /**
     *  Audio Playback Types
     *
     *  Types of audio output device Java3D sound is played over:
     *     Headphones, MONO_SPEAKER, STEREO_SPEAKERS
     */
    /**
     *     Choosing Headphones as the audio playback type
     *     specifies that the audio playback will be through stereo headphones.
     */
    public static final int HEADPHONES = 0;

    /**
     *     Choosing a
     *     single near-field monoaural speaker
     *     as the audio playback type
     *     specifies that the audio playback will be through a single speaker
     *     some supplied distance away from the listener.
     */
    public static final int MONO_SPEAKER = 1;

    /**
     *     Choosing a
     *     two near-field stereo speakers
     *     as the audio playback type
     *     specifies that the audio playback will be through stereo speakers
     *     some supplied distance away from, and at some given angle to
     *     the listener.
     */
    public static final int STEREO_SPEAKERS = 2;

    /**
     * Initialize the audio device.
     * Exactly what occurs during initialization is implementation dependent.
     * This method provides explicit control by the user over when this
     * initialization occurs.
     * Initialization must be initiated before any other AudioDevice
     * methods are called.
     * @return true if initialization was successful without errors
     */
    public abstract boolean initialize();

    /**
     * Code to close the device and release resources.
     * @return true if close of device was successful without errors
     */
    public abstract boolean close();

    /**
     * Set Type of Audio Playback physical transducer(s) sound is output to.
     *     Valid types are HEADPHONES, MONO_SPEAKER, STEREO_SPEAKERS
     * @param type audio playback type
     */
    public abstract void setAudioPlaybackType(int type);

    /**
     * Get Type of Audio Playback Output Device.
     * @return audio playback type
     */
    public abstract int getAudioPlaybackType();

    /**
     * Set Distance from interaural mid-point between Ears to a Speaker.
     * @param distance from interaural midpoint between the ears to closest speaker
     */
    public abstract void setCenterEarToSpeaker(float distance);

    /**
     * Get Distance from interaural mid-point between Ears to a Speaker.
     * @return distance from interaural midpoint between the ears to closest speaker
     */
    public abstract float getCenterEarToSpeaker();

    /**
     * Set Angle Offset (in radians) To Speaker.
     * @param angle in radians from head Z axis and vector from center ear to speaker
     */
    public abstract void setAngleOffsetToSpeaker(float angle);

    /**
     * Get Angle Offset (in radians) To Speaker.
     * @return angle in radians from head Z axis and vector from center ear to speaker
     */
    public abstract float getAngleOffsetToSpeaker();

    /**
     * Query total number of channels available for sound rendering
     * for this audio device.  This returns the maximum number of channels
     * available for Java3D sound rendering for all sound sources.
     * @return total number of channels that can be used for this audio device
     */
     public abstract int getTotalChannels();

    /**
     * Query number of channels currently available for use.
     * During rendering, when sound nodes are playing, this method returns the
     * number of channels still available to Java3D for rendering additional
     * sound nodes.
     * @return total number of channels current available
     */
     public abstract int getChannelsAvailable();

    /**
     * Query number of channels that are used, or would be used to render
     * a particular sound node.  This method returns the number of channels
     * needed to render a particular Sound node.  The return value is the same
     * no matter if the Sound is currently active and enabled (being played) or
     * is inactive.
     * @return number of channels a particular Sound node is using or would used
     * if enabled and activated (rendered).
     */
     public abstract int getChannelsUsedForSound(Sound node);
}
