/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;


/**
 * Sound node is an abstract class that defines the properties common to all
 * sound sources. A scene graph can contain multiple sounds. Associated with each
 * sound source are: a reference to sound data, an amplitude scale factor, a release
 * flag denoting that the sound associated with this node is to play to end when
 * it is disabled, the number of times sound is to be repeated, the sound's state 
 * (on or off), a scheduling region, and a flag denoting if the sound is to
 * continue playing "silently" even while it is inactive. Whenever the listener 
 * is within a sound node's scheduling bounds this sound is potentially audible.
 *<P>
 * Sound Data 
 *
 * <UL>Associated with each Sound node is a MediaContainer
 * which includes audio data and information about this data.
 * This data can be cached (buffered) or non-cached (unbuffered or streaming).
 * If an AudioDevice has been attached to the PhysicalEnvironment, the sound 
 * data is made ready to begin playing.
 * Certain functionality can not be applied to true streaming sound data:<p>
 * 1) querying the sound's duration (Sound.DURATION_UNKNOWN will be returned),<br>
 * 2) looping over a range of the streaming data; and<br>
 * 3) restart a previously played portion of the data.<p>
 * Depending on the implementation of the AudioDevice used, streamed, non-
 * cached data may not be fully spatialized.</UL>
 *<P>
 * Initial Gain
 *
 * <UL>This gain is a scale factor applied to the sound data associated 
 * with this sound source to increase or decrease its overall amplitude.</UL>
 *<P>
 * Loop
 *
 * <UL>Data for non-streaming sound (such as a sound sample) can contain two
 * loop points marking a section of the data that is to be looped a specific
 * number of times. Thus sound data can be divided into three segments: 
 * the attack (before the begin loop point), the sustain (between the begin
 * and end loop points), and the release (after the end loop point). If 
 * there are no loop begin and end points defined as part of the sound data,
 * the begin loop point is set at the beginning of the sound data,
 * and the end loop point at the end of the sound data.
 * If this is the case, looping the sound would mean repeating the whole 
 * sound. However, these allow a portion in the middle of the sound to 
 * be looped.
 *<P>
 * A sound can be looped a specified number of times after it is activated 
 * before it is completed. The loop count value explicitly sets the number
 * of times the sound is looped. Any non-negative number is a valid value.
 * A value of zero denotes that the looped section is not repeated, but is
 * played only once. A value of -1 denotes that the loop is repeated
 * indefinitely.
 *<P>
 * Changing loop count of a sound after the sound has been started will not
 * dynamically affect the loop count currently used by the sound playing.
 * The new loop count will be used the next time the sound is enabled.</UL>
 * <P>
 * Release Flag
 *
 * <UL>When a sound is disabled, its playback would normally stop immediately 
 * no matter what part of the sound data was currently being played. By 
 * setting the Release Flag to true for nodes with non-streaming sound data,
 * the sound is allowed to play from its current position in the sound data 
 * to the end of the data (without repeats), thus playing the release portion
 * of the sound before stopping.</UL>
 *<P>
 * Continuous Flag
 *
 * <UL>For some applications, it's useful to turn a sound source "off" but to 
 * continue "silently" playing the sound so that when it is turned back "on"
 * the sound picks up playing in the same location (over time) as it would 
 * have been if the sound had never been disabled (turned off). Setting the 
 * Continuous flag true causes the sound renderer to keep track of where 
 * (over time) the sound would be playing even when the sound is disabled.</UL>
 *<P>
 *  Enable Sound
 *
 * <UL>When enabled, the sound source is started
 * playing and thus can potentially be heard, depending on its activation
 * state, gain control parameters, continuation state, and spatialization
 * parameters.  If the continuous state is true, even if the sound is not
 * active, enabling the sound starts the sound silently "playing," so that
 * when the sound is activated, the sound is (potentially) heard from 
 * somewhere in the middle of the sound data. Activation state can change
 * from active to inactive any number of times without stopping or starting
 * the sound. To re-start a sound at the beginning of its data, re-enable
 * the sound by calling setEnable with true.
 *<P>
 * Setting the enable flag to true during construction acts as a request
 * to start the sound playing "as soon as it can" be started.  
 * This could be close to immediately in limited cases, but several conditions,
 * detailed below, must be met for a sound to be ready to be played.</UL>
 *<P>
 *  Mute Sound
 *
 * <UL>When the mute state is set true, a playing sound is made to play silently.
 *</UL><P>
 *  Pause Sound
 *
 * <UL>When the pause state is set true, a playing sound is paused.
 *<P>
 * Setting the enable flag to true during construction acts as a request
 * to start the sound playing "as soon as it can" be started.  
 * This could be close to immediately in limited cases, but several conditions,
 * detailed below, must be met for a sound to be ready to be played.</UL>
 *   <P>
 *  Scheduling Bounds
 *
 * <UL>
 * A Sound is scheduled for activation when its scheduling region intersects 
 * the ViewPlatform's activation volume. This is used when the scheduling 
 * bounding leaf is set to null.</UL>
 *<P>
 * Scheduling Bounding Leaf
 * 
 * <UL>When set to a value other than null, the scheduling bounding leaf 
 * region overrides the scheduling bounds
 * object.</UL>
 *<P>
 *  Prioritize Sound
 *
 * <UL>Sound Priority is used
 * to rank concurrently playing sounds in order of importance during playback.
 * When more sounds are started than the AudioDevice
 * can handle, the sound node with the lowest priority ranking is 
 * deactivated (but continues playing silently). If a sound is deactivated
 * (due to a sound with a higher 
 * priority being started), it is automatically re-activated when
 * resources become available (e.g., when a sound with a higher priority 
 * finishes playing), or when the ordering of sound nodes are changed due to
 * a change in a sound node's priority.
 * <P>
 * Sounds with a lower priority than sound that can
 * not be played due to lack of channels will be played.
 * For example, assume we have eight channels available for playing sounds.
 * After ordering four sounds, we begin playing them in order, checking if
 * the number of channels required to play a given sound are actually available
 * before the sound is played.  Furthermore, say the first sound needs three
 * channels
 * to play, the second sound needs four channels, the third sound needs three
 * channels
 * and the fourth sound needs only one channel.  The first and second sounds
 * can be started because they require seven of the eight available channels.  The
 * third sound can not be audibly started because it requires three channels and 
 * only one is still available.  Consequently, the third sound starts playing
 * 'silently.' The fourth sound can and will be started since it only requires
 * one channel.  The third sound will be made audible when three channels become
 * available (i.e., when the first or second sound finishes playing).
 * <P>
 * Sounds given the same priority are ordered randomly. If the application
 * wants a specific ordering, it must assign unique priorities to each sound.
 * <P>
 * Methods to determine what audio output resources are required for playing
 * a Sound node on a particular AudioDevice and to determine the currently
 * available audio output resources are described in the AudioDevice class.</UL>
 *   <P>
 * Duration
 *  
 * <UL>Each sound has a length of time in milliseconds that it 
 * can run (including repeating loop section)
 * if it plays to completion. If the sound
 * media type is streaming, or if the sound is looped indefinitely, then a
 * value of -1 (implying infinite length) is returned.</UL>
 *<P>
 * Number of Channels used on Audio Device to Play Sound
 *
 * <UL>When a sound is started, it could use more than one channel on the
 * selected AudioDevice it is to be played on.  The number of Audio Device
 * channels currently used for a sound can be queried using 
 * getNumberOfChannelsUsed().</UL>
 *<P>
 * Preparing a Sound to be Played
 *
 * <UL>Sound data associated with a Sound node, either during construction
 * (when the MediaContainer is passed into the constructor as a parameter)
 * or by calling setSoundData(), it can be prepared to begin playing
 * only after the following conditions are satisfied:<p>
 * 1) the Sound node has non-null sound data associated with it<br>
 * 2) the Sound node is live<br>
 * 3) there is an active View in the Universe and<br>
 * 4) there is an initialized AudioDevice associated with the
 * PhysicalEnvironment.<p>
 * Depending on the type of MediaContainer the sound data is and on the
 * implementation of the AudioDevice used, sound data preparation could consist
 * of opening, attaching, loading, or copying into memory the associated sound data.
 * The query method, isReady() returns true when the sound is fully preprocessed
 * so that it is playable (audibly if active, silently if not active).</UL>
 *<P>
 * Playing Status
 *
 * <UL>A sound source will not be heard unless it is:<p>
 * 1) enabled/started<br>
 * 2) activated<br>
 * 3) not muted<br>
 * 4) not paused<p>
 * While these conditions are meet, the sound is potentially audible
 * and the method isPlaying() will return a status of true.
 *<P>
 * isPlaying returns false but isPlayingSilently returns true if a sound:<p>
 * 1) is enabled before it is activated; it is begun playing silently.<br>
 * 2) is enabled then deactivated while playing; it continues playing silently<br>
 * 3) is enabled while it mute state is true
 *<P>
 * When the sound finishes playing it's sound data (including all loops), it 
 * is implicitly disabled.</UL>
 *<P>
 * @see AudioDevice
 */

public abstract class Sound extends Leaf {
    // Constants for Sound object.
    //
    // These flags, when enabled using the setCapability method, allow an
    // application to invoke methods that respectively read and write the 
    // sound fields. 
    // These capability flags are enforced only when the node is part of 
    // a live or compiled scene graph.

    /**
     * Specifies that this node allows access to its object's sound data
     * information.
     */
    public static final int
    ALLOW_SOUND_DATA_READ = CapabilityBits.SOUND_ALLOW_SOUND_DATA_READ;

  /**
   * Specifies that this node allows writing to its object's sound data
   * information.
   */
    public static final int
    ALLOW_SOUND_DATA_WRITE = CapabilityBits.SOUND_ALLOW_SOUND_DATA_WRITE;

    /**
     * Specifies that this node allows access to its object's initial gain
     * information.
     */
    public static final int
    ALLOW_INITIAL_GAIN_READ = CapabilityBits.SOUND_ALLOW_INITIAL_GAIN_READ;

    /**
     * Specifies that this node allows writing to its object's initial gain
     * information.
     */
    public static final int
    ALLOW_INITIAL_GAIN_WRITE = CapabilityBits.SOUND_ALLOW_INITIAL_GAIN_WRITE;

    /**
     * Specifies that this node allows access to its object's loop
     * information.
     */
    public static final int
    ALLOW_LOOP_READ = CapabilityBits.SOUND_ALLOW_LOOP_READ;

    /**
     * Specifies that this node allows writing to its object's loop
     * information.
     */
    public static final int
    ALLOW_LOOP_WRITE = CapabilityBits.SOUND_ALLOW_LOOP_WRITE;

    /**
     * Specifies that this node allows access to its object's release flag
     *  information.
     */
    public static final int
    ALLOW_RELEASE_READ = CapabilityBits.SOUND_ALLOW_RELEASE_READ;

    /**
     * Specifies that this node allows writing to its object's release flag
     * information.
     */
    public static final int
    ALLOW_RELEASE_WRITE = CapabilityBits.SOUND_ALLOW_RELEASE_WRITE;

    /**
     * Specifies that this node allows access to its object's continuous
     *  play information.
     */
    public static final int
    ALLOW_CONT_PLAY_READ = CapabilityBits.SOUND_ALLOW_CONT_PLAY_READ;

    /**
     * Specifies that this node allows writing to its object's continuous
     * play information.
     */
    public static final int
    ALLOW_CONT_PLAY_WRITE = CapabilityBits.SOUND_ALLOW_CONT_PLAY_WRITE;
    
    /**
     * Specifies that this node allows access to its object's sound on
     *  information.
     */
    public static final int
    ALLOW_ENABLE_READ = CapabilityBits.SOUND_ALLOW_ENABLE_READ;

    /**
     * Specifies that this node allows writing to its object's sound on
     * information.
     */
    public static final int
    ALLOW_ENABLE_WRITE = CapabilityBits.SOUND_ALLOW_ENABLE_WRITE;
    
    /**
     * Specifies that this node allows read access to its scheduling bounds
     * information.
     */
    public static final int
    ALLOW_SCHEDULING_BOUNDS_READ = CapabilityBits.SOUND_ALLOW_SCHEDULING_BOUNDS_READ;

    /**
     * Specifies that this node allows write access to its scheduling bounds
     * information.
     */
    public static final int
    ALLOW_SCHEDULING_BOUNDS_WRITE = CapabilityBits.SOUND_ALLOW_SCHEDULING_BOUNDS_WRITE;
 
    /**
     * Specifies that this node allows read access to its priority order
     * value.
     */
    public static final int
    ALLOW_PRIORITY_READ = CapabilityBits.SOUND_ALLOW_PRIORITY_READ;
 
    /**
     * Specifies that this node allows write access to its priority order
     * value.
     */
    public static final int
    ALLOW_PRIORITY_WRITE = CapabilityBits.SOUND_ALLOW_PRIORITY_WRITE;
    
    /**
     * Specifies that this node allows access to its object's sound duration
     *  information.
     */
    public static final int
    ALLOW_DURATION_READ = CapabilityBits.SOUND_ALLOW_DURATION_READ;

    /**
     * Specifies that this node allows access to its object's sound status
     *  denoting if it is ready to be played 'immediately'.
     */
    public static final int
    ALLOW_IS_READY_READ = CapabilityBits.SOUND_ALLOW_IS_READY_READ;

    /**
     * Specifies that this node allows access to its object's sound audibly
     *  playing or playing silently status.
     */
    public static final int
    ALLOW_IS_PLAYING_READ = CapabilityBits.SOUND_ALLOW_IS_PLAYING_READ;

    /**
     * Specifies that this node allows access to its number of channels
     * used by this sound.
     */
    public static final int
    ALLOW_CHANNELS_USED_READ = CapabilityBits.SOUND_ALLOW_CHANNELS_USED_READ;
   
    /**
     * Specifies that this node allows access to its object's mute flag
     * information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_MUTE_READ = CapabilityBits.SOUND_ALLOW_MUTE_READ;

    /**
     * Specifies that this node allows writing to its object's mute flag
     * information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_MUTE_WRITE = CapabilityBits.SOUND_ALLOW_MUTE_WRITE;

    /**
     * Specifies that this node allows access to its object's pause flag
     *  information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_PAUSE_READ = CapabilityBits.SOUND_ALLOW_PAUSE_READ;

    /**
     * Specifies that this node allows writing to its object's pause flag
     * information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_PAUSE_WRITE = CapabilityBits.SOUND_ALLOW_PAUSE_WRITE;

    /**
     * Specifies that this node allows access to its object's sample rate scale
     * factor information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_RATE_SCALE_FACTOR_READ = CapabilityBits.SOUND_ALLOW_RATE_SCALE_FACTOR_READ;

    /**
     * Specifies that this node allows writing to its object's sample rate scale
     * factor information.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_RATE_SCALE_FACTOR_WRITE = CapabilityBits.SOUND_ALLOW_RATE_SCALE_FACTOR_WRITE;

    /**
     * Denotes that there is no filter value associated with object's distance
     * or angular attenuation array.
     */
    public static final float NO_FILTER = -1.0f;

    /**
     * Denotes that the sound's duration could not be calculated.
     * A fall back for getDuration of a non-cached sound.
     */
    public static final int DURATION_UNKNOWN = -1;

    /**
     * When used as a loop count sound will loop an infinite number of time
     * until explicitly stopped (setEnabled(false)).
     */
    public static final int INFINITE_LOOPS = -1;


    /**
     * Constructs and initializes a new Sound node using default
     * parameters.  The following defaults values are used:
     * <ul>
     * sound data: null<br>
     * initial gain: 1.0<br>
     * loop: 0<br>
     * release flag: false<br>
     * continuous flag: false<br>
     * enable flag: false<br>
     * scheduling bounds : null<br>
     * scheduling bounding leaf : null<br>
     * priority: 1.0<br>
     * rate scale factor: 1.0<br>
     * mute state: false<br>
     * pause state: false<br>
     * </ul>
     */
    public Sound() {
    } 

    /**
     * Constructs and initializes a new Sound node object using the provided 
     * data and gain parameter values, and defaults for all other fields. This 
     * constructor implicitly loads the sound data associated with this node if
     * the implementation uses sound caching.
     * @param soundData description of JMF source data used by this sound source
     * @param initialGain overall amplitude scale factor applied to sound source
     */
    public Sound(MediaContainer soundData, float initialGain) {
        ((SoundRetained)this.retained).setSoundData(soundData);
        ((SoundRetained)this.retained).setInitialGain(initialGain);
    } 


    /**
     * Constructs and initializes a new Sound node using provided parameter
     * values.
     * @param soundData description of JMF source data used by this sound source
     * @param initialGain overall amplitude scale factor applied to sound source
     * @param loopCount number of times sound is looped when played
     * @param release flag specifying whether the sound is to be played
     * to end when stopped
     * @param continuous flag specifying whether the sound silently plays
     * when disabled
     * @param enable flag specifying whether the sound is enabled
     * @param region scheduling bounds
     * @param priority defines playback priority if too many sounds started 
     */
    public Sound(MediaContainer soundData,
                 float initialGain,
                 int loopCount,
                 boolean release,
                 boolean continuous,
                 boolean enable,
                 Bounds  region,
                 float   priority ) {
        ((SoundRetained)this.retained).setSoundData(soundData);
        ((SoundRetained)this.retained).setInitialGain(initialGain);
        ((SoundRetained)this.retained).setLoop(loopCount);
        ((SoundRetained)this.retained).setReleaseEnable(release);
        ((SoundRetained)this.retained).setContinuousEnable(continuous);
        ((SoundRetained)this.retained).setEnable(enable);
        ((SoundRetained)this.retained).setSchedulingBounds(region);
        ((SoundRetained)this.retained).setPriority(priority);
    } 

    /**
     * Constructs and initializes a new Sound node using provided parameter
     * values.
     * @param soundData description of JMF source data used by this sound source
     * @param initialGain overall amplitude scale factor applied to sound source
     * @param loopCount number of times sound is looped when played
     * @param release flag specifying whether the sound is to be played
     * to end when stopped
     * @param continuous flag specifying whether the sound silently plays
     * when disabled
     * @param enable flag specifying whether the sound is enabled
     * @param region scheduling bounds
     * @param priority defines playback priority if too many sounds started 
     * @param rateFactor defines playback sample rate scale factor
     * @since Java 3D 1.3
     */
    public Sound(MediaContainer soundData,
                 float initialGain,
                 int loopCount,
                 boolean release,
                 boolean continuous,
                 boolean enable,
                 Bounds  region,
                 float   priority,
                 float   rateFactor ) {
        ((SoundRetained)this.retained).setSoundData(soundData);
        ((SoundRetained)this.retained).setInitialGain(initialGain);
        ((SoundRetained)this.retained).setLoop(loopCount);
        ((SoundRetained)this.retained).setReleaseEnable(release);
        ((SoundRetained)this.retained).setContinuousEnable(continuous);
        ((SoundRetained)this.retained).setEnable(enable);
        ((SoundRetained)this.retained).setSchedulingBounds(region);
        ((SoundRetained)this.retained).setPriority(priority);
        ((SoundRetained)this.retained).setRateScaleFactor(rateFactor);
    } 

    /**
     * Sets fields that define the sound source data of this node.
     * @param soundData description of JMF source data used by this sound source
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setSoundData(MediaContainer soundData) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SOUND_DATA_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound0"));
	
	if (this instanceof BackgroundSound) 
	    ((SoundRetained)this.retained).setSoundData(soundData);
	else // instanceof PointSound or ConeSound
	    ((PointSoundRetained)this.retained).setSoundData(soundData);
    }

    /**
     * Retrieves description/data associated with this sound source.
     * @return soundData description of JMF source data used by this sound source
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public MediaContainer getSoundData() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SOUND_DATA_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound1"));
	
	return ((SoundRetained)this.retained).getSoundData();
    }

    /**
     * Set the overall gain scale factor applied to data associated with this 
     * source to increase or decrease its overall amplitude.
     * @param amplitude (gain) scale factor
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setInitialGain(float amplitude) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INITIAL_GAIN_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound2"));
	
	((SoundRetained)this.retained).setInitialGain(amplitude);
    }

    /**
     * Get the overall gain applied to the sound data associated with source.
     * @return overall gain scale factor applied to sound source data.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getInitialGain() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_INITIAL_GAIN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound3"));
	
	return ((SoundRetained)this.retained).getInitialGain();
    }

    /**
     * Sets a sound's loop count.
     * @param loopCount number of times sound is looped during play
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setLoop(int loopCount) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_LOOP_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound4"));
	
	((SoundRetained)this.retained).setLoop(loopCount);
    }

    /**
     * Retrieves loop count for this sound
     * @return loop count
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getLoop() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_LOOP_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound5"));
	
	return ((SoundRetained)this.retained).getLoop();
    }
    
    /**
     * Enables or disables the release flag for the sound associated with
     * this sound.
     * @param state release flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setReleaseEnable(boolean state) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_RELEASE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound6"));
	
	((SoundRetained)this.retained).setReleaseEnable(state);
    }

    /**
     * Retrieves the release flag for sound associated with sound.
     * @return sound's release flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getReleaseEnable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_RELEASE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound7"));
	
	return ((SoundRetained)this.retained).getReleaseEnable();
    }
    
    /**
     * Enables or disables continuous play flag.
     * @param state denotes if deactivated sound silently continues playing
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setContinuousEnable(boolean state) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CONT_PLAY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound8"));
	
	((SoundRetained)this.retained).setContinuousEnable(state);
    }

    /**
     * Retrieves sound's continuous play flag.
     * @return flag denoting if deactivated sound silently continues playing
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getContinuousEnable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CONT_PLAY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound9"));
	
	return ((SoundRetained)this.retained).getContinuousEnable();
    }
    
    /**
     * Enable or disable sound.
     * @param state enable (on/off) flag denotes if active sound is heard
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setEnable(boolean state) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_ENABLE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound10"));
	
	if (this instanceof BackgroundSound) 
	    ((SoundRetained)this.retained).setEnable(state);
	else // instanceof PointSound or ConeSound
	    ((PointSoundRetained)this.retained).setEnable(state);
    }
    
    /**
     * Retrieves sound's enabled flag.
     * @return sound enabled flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getEnable() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_ENABLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound21"));
	
	return ((SoundRetained)this.retained).getEnable();
    }


    /**
     * Set the Sound's scheduling region to the specified bounds.
     * This is used when the scheduling bounding leaf is set to null.
     * @param region the bounds that contains the Sound's new scheduling
     * region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setSchedulingBounds(Bounds region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCHEDULING_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound11"));

	((SoundRetained)this.retained).setSchedulingBounds(region);
    }

    /**  
     * Retrieves the Sound node's scheduling bounds.
     * @return this Sound's scheduling bounds information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public Bounds getSchedulingBounds() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCHEDULING_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound12"));

	return ((SoundRetained)this.retained).getSchedulingBounds();
    }


    /**
     * Set the Sound's scheduling region to the specified bounding leaf.
     * When set to a value other than null, this overrides the scheduling
     * bounds object.
     * @param region the bounding leaf node used to specify the Sound
     * node's new scheduling region.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setSchedulingBoundingLeaf(BoundingLeaf region) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCHEDULING_BOUNDS_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound11"));

	((SoundRetained)this.retained).setSchedulingBoundingLeaf(region);
    }

    /**  
     * Retrieves the Sound node's scheduling bounding leaf.
     * @return this Sound's scheduling bounding leaf information
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public BoundingLeaf getSchedulingBoundingLeaf() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SCHEDULING_BOUNDS_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound12"));

	return ((SoundRetained)this.retained).getSchedulingBoundingLeaf();
    }


    /**
     * Set sound's priority value.
     * @param priority value used to order sound's importance for playback.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setPriority(float priority) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_PRIORITY_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound15"));
	
	((SoundRetained)this.retained).setPriority(priority);
    }
    
    /**
     * Retrieves sound's priority value.
     * @return sound priority value
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public float getPriority() {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_PRIORITY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound16"));
	
        return ((SoundRetained)this.retained).getPriority();
    }


    /**
     * Get the Sound's duration
     * @return this Sound's duration in milliseconds including repeated
     * loops
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public long getDuration() { 
        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_DURATION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound17"));

        return ((SoundRetained)this.retained).getDuration();
    }


    /**
     * Retrieves sound's 'ready' status. If this sound is fully
     * prepared to begin playing (audibly or silently) on all
     * initialized audio devices, this method returns true.
     * @return flag denoting if sound is immediate playable or not
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean isReady()  {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_IS_READY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound22"));
	
        return ((SoundRetained)this.retained).isReady();
    }

    /**
     * Retrieves sound's 'ready' status. If this sound is fully
     * prepared to begin playing (audibly or silently) on the audio
     * device associated with this view, this method returns true.
     * @param view the view on which to query the ready status.
     * @return flag denoting if sound is immediate playable or not
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public boolean isReady(View view) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_IS_READY_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound22"));
	
        return ((SoundRetained)this.retained).isReady(view);
    }


    /**
     * Retrieves sound's play status.  If this sound is audibly playing on any
     * initialized audio device, this method returns true.
     * @return flag denoting if sound is playing (potentially audible) or not
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean isPlaying()  {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_IS_PLAYING_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound18"));
  
        return ((SoundRetained)this.retained).isPlaying();
    }

    /**
     * Retrieves sound's play status.  If this sound is audibly playing on the
     * audio device associated with the given view, this method returns
     * true.
     * @param view the view on which to query the isPlaying status.
     * @return flag denoting if sound is playing (potentially audible) or not
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public boolean isPlaying(View view) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_IS_PLAYING_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound18"));
  
        return ((SoundRetained)this.retained).isPlaying(view);
    }

    /**
     * Retrieves sound's silent status.  If this sound is silently playing on
     * any initialized audio device, this method returns true.
     * @return flag denoting if sound is silently playing (enabled but not active)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean isPlayingSilently()  {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_IS_PLAYING_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound18"));
	
        return ((SoundRetained)this.retained).isPlayingSilently();
    }

    /**
     * Retrieves sound's silent status.  If this sound is silently playing on
     * the audio device associated with the given view, this method returns
     * true.
     * The isPlayingSilently state is affected by enable, mute,  and continuous
     * states as well as active status of sound.
     * @param view the view on which to query the isPlayingSilently status.
     * @return flag denoting if sound is silently playing (enabled but not active)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public boolean isPlayingSilently(View view) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_IS_PLAYING_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound18"));
	
        return ((SoundRetained)this.retained).isPlayingSilently(view);
    }


    /**  
     * Retrieves number of channels that are being used to render this sound
     * on the audio device associated with the Virtual Universe's primary view.
     * @return number of channels used by sound; returns 0 if not playing
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public int getNumberOfChannelsUsed()  {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CHANNELS_USED_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound20"));
	
        return ((SoundRetained)this.retained).getNumberOfChannelsUsed();
    }

    /**
     * Retrieves number of channels that are being used to render this sound
     * on the audio device associated with given view.
     * @param view the view on which to query the number of channels used.
     * @return number of channels used by sound; returns 0 if not playing
     * @exception CapabilityNotSetException if appropriate capability is
     * @since Java 3D 1.3
     * not set and this object is part of live or compiled scene graph
     */
    public int getNumberOfChannelsUsed(View view) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CHANNELS_USED_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound20"));
	
        return ((SoundRetained)this.retained).getNumberOfChannelsUsed(view);
    }

    /**
     * Set mute state flag.  If the sound is playing it will be set to
     * play silently
     * @param state flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setMute(boolean state) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_MUTE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound23"));
	
        ((SoundRetained)this.retained).setMute(state);
    }

    /**
     * Retrieves sound Mute state.
     * A return value of true does not imply that the sound has 
     * been started playing or is still playing silently.
     * @return mute state flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public boolean getMute() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_MUTE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound24"));
	
        return ((SoundRetained)this.retained).getMute();
    }

    /**
     * Pauses or resumes (paused) playing sound.
     * @param state pause flag
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setPause(boolean state) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PAUSE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound25"));
	
        ((SoundRetained)this.retained).setPause(state);
    }

    /**
     * Retrieves the value of the Pause state flag.
     * A return value of true does not imply that the sound was 
     * started playing and then paused.
     * @return pause state
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public boolean getPause() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PAUSE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound26"));
	
        return ((SoundRetained)this.retained).getPause();
    }

    /**
     * Sets Sample Rate.
     * Changes (scales) the playback rate of a sound independent of
     * Doppler rate changes - applied to ALL sound types.
     * Affects device sample rate playback and thus affects both pitch and speed
     * @param scaleFactor %%% describe this.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setRateScaleFactor(float scaleFactor) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_RATE_SCALE_FACTOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound27"));
	
        ((SoundRetained)this.retained).setRateScaleFactor(scaleFactor);
    }

    /**
     * Retrieves Sample Rate.
     * @return sample rate scale factor
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getRateScaleFactor() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_RATE_SCALE_FACTOR_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Sound28"));
	
        return ((SoundRetained)this.retained).getRateScaleFactor();
    }

   /**
     * Copies all Sound information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P> 
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
	super.duplicateAttributes(originalNode, forceDuplicate);

        SoundRetained orgRetained = (SoundRetained)originalNode.retained;

        SoundRetained thisRetained = (SoundRetained)this.retained;

	thisRetained.setSoundData((MediaContainer) getNodeComponent(
					   orgRetained.getSoundData(),
					   forceDuplicate,
					   originalNode.nodeHashtable));
	thisRetained.setInitialGain(orgRetained.getInitialGain());
	thisRetained.setLoop(orgRetained.getLoop());
	thisRetained.setReleaseEnable(orgRetained.getReleaseEnable());
	thisRetained.setContinuousEnable(orgRetained.getContinuousEnable());
	thisRetained.setSchedulingBounds(orgRetained.getSchedulingBounds());
	thisRetained.setPriority(orgRetained.getPriority());
	thisRetained.setEnable(orgRetained.getEnable());

	// updateNodeReferences will set the following correctly
	thisRetained.setSchedulingBoundingLeaf(orgRetained.getSchedulingBoundingLeaf());
    }

    /**
     * Callback used to allow a node to check if any scene graph objects
     * referenced
     * by that node have been duplicated via a call to <code>cloneTree</code>.
     * This method is called by <code>cloneTree</code> after all nodes in
     * the sub-graph have been duplicated. The cloned Leaf node's method
     * will be called and the Leaf node can then look up any object references
     * by using the <code>getNewObjectReference</code> method found in the
     * <code>NodeReferenceTable</code> object.  If a match is found, a
     * reference to the corresponding object in the newly cloned sub-graph
     * is returned.  If no corresponding reference is found, either a
     * DanglingReferenceException is thrown or a reference to the original
     * object is returned depending on the value of the
     * <code>allowDanglingReferences</code> parameter passed in the
     * <code>cloneTree</code> call.
     * <p>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneTree method.
     *
     * @param referenceTable a NodeReferenceTableObject that contains the
     *  <code>getNewObjectReference</code> method needed to search for
     *  new object instances.
     * @see NodeReferenceTable
     * @see Node#cloneTree
     * @see DanglingReferenceException
     */
    public void updateNodeReferences(NodeReferenceTable referenceTable) {
	super.updateNodeReferences(referenceTable);

	SoundRetained rt = (SoundRetained) retained;
        BoundingLeaf bl = rt.getSchedulingBoundingLeaf();

        if (bl != null) {
            Object o = referenceTable.getNewObjectReference(bl);
            rt.setSchedulingBoundingLeaf((BoundingLeaf)o);
        }
        MediaContainer sd = rt.getSoundData();
        if (sd != null) {
            rt.setSoundData(sd);
        }
    }
}
