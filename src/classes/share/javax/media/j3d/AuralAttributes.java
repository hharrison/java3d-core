/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Point2f;

/**
 * The AuralAttributes object is a component object of a Soundscape node that
 * defines environmental audio parameters that affect sound rendering. These
 * attributes include gain scale factor, atmospheric rolloff, and parameters
 * controlling reverberation, distance frequency filtering, and velocity-based
 * Doppler effect.
 *<P>
 * Attribute Gain
 *   <P><UL>
 *    Scale factor applied to all sound's amplitude active within this region.
 *    This factor attenuates both direct and reflected/reverbered amplitudes.
 *    Valid values are >= 0.0
 *  </UL>
 *<P>
 * Attribute Gain Rolloff
 *   <P><UL>
 *    Rolloff scale factor is used to model atmospheric changes from normal
 *    speed of sound.  The base value, 0.344 meters/millisecond is used
 *    to approximate the speed of sound through air at room temperature, 
 *    is multipled by this scale factor whenever the speed of sound is
 *    applied during spatialization calculations.
 *    Valid values are >= 0.0.  Values > 1.0 increase the speed of sound,
 *    while values < 1.0 decrease its speed. A value of zero makes sound
 *    silent (but it continues to play).
 *   </UL>
 *<P>
 * Auralization <P>
 *<UL>
 *    Auralization is the environmental modeling of sound iteratively
 *    reflecting off the surfaces of the bounded region the listener is in.
 *    Auralization components include 
 *    early, distinct, low-order reflections and later, dense,
 *    higher-order reflections referred to as reverberation.
 *    These reflections are attenuated relative to the direct, unreflected 
 *    sound.  The difference in gain between direct and reflected sound
 *    gives the listener a sense of the surface material and 
 *    the relative distance of the sound.
 *    The delay between the start of the direct sound and start of 
 *    reverberation (as detected by the listener), 
 *    as well as the length of time reverberation is audible as it
 *    exponentially decays, give the listener a sense of the size of the 
 *    listening space.
 *   <P>
 *    In Java3D's model for auralization there are several parameters
 *    that approximate sound reflection and reverberation for a particular
 *    listening space:
 *     <UL>Reflection Coefficient  <UL>Gain attenuation of the initial
 *         reflections across all frequencies.</UL></UL>
 *     <UL>(Early) Reflection Delay  <UL>The time it takes for the first
 *         low-order reflected sound to reach the listener.</UL></UL>
 *     <UL>Reverb Coefficient  <UL>Gain attenuation of the late reflections
 *         (referred to as 'reverb') across all frequencies.</UL></UL>
 *     <UL>Reverb Delay  <UL>The time it takes for reverbered sound
 *         to reach the listener.</UL></UL>
 *     <UL>Decay Time <UL>Describes the reverb decay curve by defining the
 *         length of time reverb takes to decay to effective zero.
 *         </UL></UL>
 *     <UL>Decay Filter  <UL>High-frequencies of the late reverberation
 *         can be attenuated at a different rate. </UL></UL>
 *     <UL>Density  <UL>Modal density (spectral coloration) of
 *         reverberation.</UL></UL>
 *     <UL>Diffusion  <UL>Echo dispersement of reverberation.</UL></UL>
 *     <UL>Reverb Bounds  <UL>Approximates the volume of the listening space.
 *         If specified, it defines the reverberation delay.</UL></UL>
 *     <UL>Reverb Order  <UL>Optionally limits the amount of times during 
 *         reverb calculation that a sound is recursively reflected off the 
 *         bounding region.</UL></UL>
 *   <P>
 *    Reflection Coefficient
 *   <P><UL>
 *       The reflection coefficient is an amplitude scale factor used to
 *       approximate the average reflective or absorptive characteristics
 *       for early reflections
 *       of the composite surfaces in the region the listener is in.
 *       This scale factor is applied to the sound's amplitude regardless of the
 *       sound's position.  
 *       The range of valid values is 0.0 to 1.0.
 *       A value of 1.0 denotes that reflections are unattenuated -
 *       the amplitude of reflected sound waves are not decreased.
 *       A value of 0.0 represents full absorption of reflections
 *       by the surfaces in the listening space (no reflections occur
 *       thus reverberation is disabled).
 *     </UL>
 *   <P>
 *    Reflection Delay
 *   <P><UL>
 *       The early reflection delay time (in milliseconds) can be explicitly
 *       set.  Well-defined values are floats > 0.0.
 *       A value of 0.0 results in reverberation being added as soon as
 *       possible after the sound begins.
 *     </UL>
 *   <P>
 *    Reverberation Coefficient
 *   <P><UL>
 *       The reverb coefficient is an amplitude scale factor used to
 *       approximate the average reflective or absorptive characteristics
 *       of late reflections.
 *       A value of 0.0 represents full absorption of reflections
 *       by the surfaces in the listening space (no reflections occur
 *       thus reverberation is disabled).
 *     </UL>
 *   <P>
 *    Reverberation Delay
 *   <P><UL>
 *       The reverb delay time (in milliseconds) is set either explicitly,
 *       or implicitly by supplying a reverb bounds volume (from which the
 *       delay time can be calculated).  Well-defined values are floats > 0.0.
 *       A value of 0.0 results in reverberation being added as soon as
 *       possible after the sound begins.  Reverb delay, as calculated from non-
 *       null reverb bounds, takes precedence over explicitly set delay time.
 *     </UL>
 *   <P>
 *    Reverberation Bounds
 *   <P><UL>
 *       The reverb bounding region defines the overall size of space
 *       that reverberation is calculated for.
 *       This optional bounds does not have to be the same as the application
 *       region of the Soundscape node referencing this AuralAttributes object.
 *       If this bounding region is specified then reverb decay and delay are
 *       internally calculated from this bounds.
 *     </UL>
 *   <P>
 *    Reverberation Order
 *   <P><UL>
 *       The reverb order is a hint that can be used during reverberation
 *       to limit the number of late reflections required in calculation of
 *       reverb decay.
 *       All positive values can be interpreted during reverb rendering
 *       as the maximum order of reflections to be calculated.
 *       A non-positive value signifies that no limit is placed on the order of
 *       reflections calculated during reverberation rendering.
 *       In the case where reverb order is not limited, reverb decay is defined
 *       strictly by the Reverberation Decay Time parameter.
 *     </UL>
 *   <P>
 *    Decay Time
 *   <P><UL>
 *       The reverberation decay time explicitly defines the length of time in
 *       milliseconds it takes for the amplitude of late reflections to 
 *       exponentally decrease to effective zero.
 *       In the case where reverb delay is set non-positive
 *       the renderer will perform the shortest reverberation decay
 *       possible.
 *       If ReverbOrder is set, this parameter is clamped by the reverb
 *       time calculated as time = reverb Delay * reverb Order.
 *       If ReverbOrder is 0, the decay time parameter is not clamped.
 *     </UL>
 *   <P>
 *    Decay Filter
 *   <P><UL>
 *       The reverberation decay filter defines how frequencies above a given
 *       value are attenuated by the listening space.  This allows for modelling
 *       materials on surfaces that absorb high frequencies at a faster rate 
 *       than low frequencies.
 *     </UL>
 *   <P>
 *    Reverberation Diffusion
 *   <P><UL>
 *       The reverberation diffusion explicitly defines echo dispersement
 *       (sometimes refered to as echo density).  The value for diffusion
 *       is proportional to the number of echos per second heard in late
 *       reverberation, especially noticable at the tail of the reverberation
 *       decay.  The greater the diffusion the more 'natural' the reverberation
 *       decay sounds.  Reducing diffusion makes the decay sound hollow as 
 *       produced in a small highly reflecive space (such as a bathroom).
 *     </UL>
 *   <P>
 *    Reverberation Density
 *   <P><UL>
 *       The reverberation density explicitly defines modal reverb density 
 *       The value for this modal density is proportional to the number of
 *       resonances heard in late reverberation perceived as spectral 
 *       coloration.  The greater the density, the smoother, less grainy the
 *       later reverberation decay.
 *     </UL>
 *</UL>
 *<P>
 * Distance Filter
 *   <P><UL>
 *  This parameter specifies a (distance, filter) attenuation pairs array.
 *  If this is not set, no distance filtering is performed (equivalent to
 *  using a distance filter of Sound.NO_FILTER for all distances). Currently,
 *  this filter is a low-pass cutoff frequency. This array of pairs defines
 *  a piece-wise linear slope for range of values. This attenuation array is
 *  similar to the PointSound node's distanceAttenuation pair array, except
 *  paired with distances in this list are frequency values.  Using these
 *  pairs, distance-based low-pass frequency filtering can be applied during
 *  sound rendering. Distances, specified in the local coordinate system in
 *  meters, must be > 0. Frequencies (in Hz) must be > 0.
 *<P>
 *  If the distance from the listener to the sound source is less than the
 *  first distance in the array, the first filter is applied to the sound
 *  source.  This creates a spherical region around the listener within
 *  which a sound is uniformly attenuated by the first filter in the array.
 *  If the distance from the listener to the sound source is greater than
 *  the last distance in the array, the last filter is applied to the sound
 *  source.
 *   <P>
 *  Distance elements in these array of pairs is a monotonically-increasing
 *  set of floating point numbers measured from the location of the sound
 *  source.  FrequencyCutoff elements in this list of pairs can be any
 *  positive float. While for most applications this list of values will
 *  usually be monotonically-decreasing, they do not have to be.
 *   <P>
 *  The getDistanceFilterLength method returns the length of the distance filter
 *  arrays. Arrays passed into getDistanceFilter methods should all be at
 *  least this size.</UL>
 *   </UL><P>
 *  Doppler Effect Model
 *   <P><UL>
 *  Doppler effect can be used to create a greater sense of movement of
 *  sound sources, and can help reduce front-back localization errors.
 *  The frequency of sound waves emanating from the source are raised or
 *  lowered based on the speed of the source in relation to the listener,
 *  and several AuralAttribute parameters.
 *   <P>
 *  The FrequencyScaleFactor can be used to increase or reduce the change
 *  of frequency associated with normal Doppler calculation, or to shift
 *  the pitch of the sound directly if Doppler effect is disabled.
 *  Values must be > zero for sounds to be heard.  If the value is zero,
 *  sounds affected by this AuralAttribute object are paused.
 *   <P>
 *  To simulate Doppler effect, the relative velocity (change in 
 *  distance in the local coordinate system between the sound source and 
 *  the listener over time, in meters per second) is calculated. This
 *  calculated velocity is multipled by the given VelocityScaleFactor.
 *  Values must be >= zero.  If is a scale factor value of zero is given,
 *  Doppler effect is not calculated or applied to sound.</UL></UL>
 */
public class AuralAttributes extends NodeComponent {

     /**
      *
      *  Constants
      *
      * These flags, when enabled using the setCapability method, allow an
      * application to invoke methods that read or write its parameters.
      *
      */
     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's attribute gain scale factor information.
      */
     public static final int
    ALLOW_ATTRIBUTE_GAIN_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_ATTRIBUTE_GAIN_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's attribute gain scale factor information.
      */
     public static final int
    ALLOW_ATTRIBUTE_GAIN_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_ATTRIBUTE_GAIN_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's atmospheric rolloff.
      */
     public static final int
    ALLOW_ROLLOFF_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_ROLLOFF_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's atmospheric rolloff.
      */
     public static final int
    ALLOW_ROLLOFF_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_ROLLOFF_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reflection coefficient.
      */
     public static final int
    ALLOW_REFLECTION_COEFFICIENT_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REFLECTION_COEFFICIENT_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reflection coefficient.
      */
     public static final int
    ALLOW_REFLECTION_COEFFICIENT_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REFLECTION_COEFFICIENT_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reflection delay information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_REFLECTION_DELAY_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REFLECTION_DELAY_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reflection delay information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_REFLECTION_DELAY_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REFLECTION_DELAY_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb coefficient.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_REVERB_COEFFICIENT_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_COEFFICIENT_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb coefficient.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_REVERB_COEFFICIENT_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_COEFFICIENT_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverberation delay information.
      */
     public static final int
    ALLOW_REVERB_DELAY_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_DELAY_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverberation delay information.
      */
     public static final int
    ALLOW_REVERB_DELAY_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_DELAY_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb order (feedback loop) information.
      */
     public static final int
    ALLOW_REVERB_ORDER_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_ORDER_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb order (feedback loop) information.
      */
     public static final int
    ALLOW_REVERB_ORDER_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_REVERB_ORDER_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb decay time information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DECAY_TIME_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DECAY_TIME_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb decay time information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DECAY_TIME_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DECAY_TIME_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb decay filter information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DECAY_FILTER_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DECAY_FILTER_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb decay filter information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DECAY_FILTER_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DECAY_FILTER_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb diffusion information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DIFFUSION_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DIFFUSION_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb diffusion information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DIFFUSION_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DIFFUSION_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's reverb density information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DENSITY_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DENSITY_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's reverb density information.
      *
      * @since Java 3D 1.3
      */
     public static final int
    ALLOW_DENSITY_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DENSITY_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's frequency cutoff information.
      */
     public static final int
    ALLOW_DISTANCE_FILTER_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DISTANCE_FILTER_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's frequency cutoff information.
      */
     public static final int
    ALLOW_DISTANCE_FILTER_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_DISTANCE_FILTER_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's frequency scale factor information.
      */
     public static final int
    ALLOW_FREQUENCY_SCALE_FACTOR_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_FREQUENCY_SCALE_FACTOR_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's frequency scale factor information.
      */
     public static final int
    ALLOW_FREQUENCY_SCALE_FACTOR_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_FREQUENCY_SCALE_FACTOR_WRITE;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the reading of it's velocity scale factor information.
      */
     public static final int
    ALLOW_VELOCITY_SCALE_FACTOR_READ = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_VELOCITY_SCALE_FACTOR_READ;

     /**
      * For AuralAttributes component objects, specifies that this object
      * allows the writing of it's velocity scale factor information.
      */
     public static final int
    ALLOW_VELOCITY_SCALE_FACTOR_WRITE = CapabilityBits.AURAL_ATTRIBUTES_ALLOW_VELOCITY_SCALE_FACTOR_WRITE;

    /** *****************
     *   
     *  Constructors
     *   
     * ******************/
    /**
     * Constructs and initializes a new AuralAttributes object using default
     * parameters.  The following default values are used:
     * <ul>
     * attribute gain: 1.0<br>
     * rolloff: 1.0<br>
     * reflection coeff: 0.0<br>
     * reflection delay: 20.0<br>
     * reverb coeff: 1.0<br>
     * reverb delay: 40.0<br>
     * decay time: 1000.0<br>
     * decay filter: 5000.0<>
     * diffusion: 1.0<br>
     * density: 1.0<br>
     * reverb bounds: null<br>
     * reverb order: 0<br>
     * distance filtering: null (no filtering performed)<br>
     * frequency scale factor: 1.0<br>
     * velocity scale factor: 0.0<br>
     * </ul>
     */  
    public AuralAttributes() {
         // Just use default values
    }

    /**
     * Constructs and initializes a new AuralAttributes object using specified
     * parameters including an array of Point2f for the distanceFilter.
     * @param gain amplitude scale factor
     * @param rolloff atmospheric (changing speed of sound) scale factor
     * @param reflectionCoefficient reflective/absorptive factor applied to reflections
     * @param reverbDelay delay time before start of reverberation
     * @param reverbOrder limit to number of reflections added to reverb signal
     * @param distanceFilter frequency cutoff
     * @param frequencyScaleFactor applied to change of pitch
     * @param velocityScaleFactor applied to velocity of sound in relation to listener
     */  
    public AuralAttributes(float     	gain,
                      float      	rolloff,
                      float      	reflectionCoefficient,
                      float      	reverbDelay,
                      int	      	reverbOrder,
                      Point2f[]         distanceFilter,
                      float      	frequencyScaleFactor,
                      float      	velocityScaleFactor) {
        ((AuralAttributesRetained)this.retained).setAttributeGain(gain);
        ((AuralAttributesRetained)this.retained).setRolloff(rolloff);
        ((AuralAttributesRetained)this.retained).setReflectionCoefficient(
                      reflectionCoefficient);
        ((AuralAttributesRetained)this.retained).setReverbDelay(reverbDelay);
        ((AuralAttributesRetained)this.retained).setReverbOrder(reverbOrder);
        ((AuralAttributesRetained)this.retained).setDistanceFilter(
                      distanceFilter);
        ((AuralAttributesRetained)this.retained).setFrequencyScaleFactor(
                      frequencyScaleFactor);
        ((AuralAttributesRetained)this.retained).setVelocityScaleFactor(
                      velocityScaleFactor);
    }
    /**
     * Constructs and initializes a new AuralAttributes object using specified
     * parameters with separate float arrays for components of distanceFilter.
     * @param gain amplitude scale factor
     * @param rolloff atmospheric (changing speed of sound) scale factor
     * @param reflectionCoefficient reflection/absorption factor applied to reflections
     * @param reverbDelay delay time before start of reverberation
     * @param reverbOrder limit to number of reflections added to reverb signal
     * @param distance filter frequency cutoff distances
     * @param frequencyCutoff distance filter frequency cutoff
     * @param frequencyScaleFactor applied to velocity/wave-length
     * @param velocityScaleFactor applied to velocity of sound in relation to listener
     */  
    public AuralAttributes(float     	gain,
                      float      	rolloff,
                      float      	reflectionCoefficient,
                      float      	reverbDelay,
                      int	      	reverbOrder,
                      float[]           distance,
                      float[]           frequencyCutoff,
                      float      	frequencyScaleFactor,
                      float      	velocityScaleFactor) {
        ((AuralAttributesRetained)this.retained).setAttributeGain(gain);
        ((AuralAttributesRetained)this.retained).setRolloff(rolloff);
        ((AuralAttributesRetained)this.retained).setReflectionCoefficient(
                      reflectionCoefficient);
        ((AuralAttributesRetained)this.retained).setReverbDelay(reverbDelay);
        ((AuralAttributesRetained)this.retained).setReverbOrder(reverbOrder);
        ((AuralAttributesRetained)this.retained).setDistanceFilter(distance,
                      frequencyCutoff);
        ((AuralAttributesRetained)this.retained).setFrequencyScaleFactor(
                      frequencyScaleFactor);
        ((AuralAttributesRetained)this.retained).setVelocityScaleFactor(
                      velocityScaleFactor);
    }

    /**
     * Constructs and initializes a new AuralAttributes object using specified
     * parameters with separate float arrays for components of distanceFilter
     * and full reverb parameters.
     * @param gain amplitude scale factor
     * @param rolloff atmospheric (changing speed of sound) scale factor
     * @param reflectionCoefficient factor applied to early reflections
     * @param reflectionDelay delay time before start of early reflections
     * @param reverbCoefficient factor applied to late reflections
     * @param reverbDelay delay time before start of late reverberation
     * @param decayTime time (in milliseconds) reverb takes to decay to -60bD
     * @param decayFilter reverb decay filter frequency cutoff
     * @param diffusion percentage of echo dispersement between min and max
     * @param density percentage of modal density between min and max
     * @param distance filter frequency cutoff distances
     * @param frequencyCutoff distance filter frequency cutoff
     * @param frequencyScaleFactor applied to velocity/wave-length
     * @param velocityScaleFactor applied to velocity of sound in relation to listener
     * @since Java 3D 1.3
     */
    public AuralAttributes(float	gain,
			   float	rolloff,
			   float	reflectionCoefficient,
			   float	reflectionDelay,
			   float	reverbCoefficient,
			   float	reverbDelay,
			   float	decayTime,
			   float	decayFilter,
			   float	diffusion,
			   float	density,
			   float[]	distance,
			   float[]	frequencyCutoff,
			   float	frequencyScaleFactor,
			   float	velocityScaleFactor) {
        ((AuralAttributesRetained)this.retained).setAttributeGain(gain);
        ((AuralAttributesRetained)this.retained).setRolloff(rolloff);
        ((AuralAttributesRetained)this.retained).setReflectionCoefficient(
                      reflectionCoefficient);
        ((AuralAttributesRetained)this.retained).setReflectionDelay(
                      reflectionDelay);
        ((AuralAttributesRetained)this.retained).setReverbCoefficient(
                      reverbCoefficient);
        ((AuralAttributesRetained)this.retained).setReverbDelay(
                      reverbDelay);
        ((AuralAttributesRetained)this.retained).setDecayTime(decayTime);
        ((AuralAttributesRetained)this.retained).setDecayFilter(decayFilter);
        ((AuralAttributesRetained)this.retained).setDiffusion(diffusion);
        ((AuralAttributesRetained)this.retained).setDensity(density);
        ((AuralAttributesRetained)this.retained).setDistanceFilter(distance,
                      frequencyCutoff);
        ((AuralAttributesRetained)this.retained).setFrequencyScaleFactor(
                      frequencyScaleFactor);
        ((AuralAttributesRetained)this.retained).setVelocityScaleFactor(
                      velocityScaleFactor);
    }

    /**
     * Creates the retained mode AuralAttributesRetained object that this
     * component object will point to.
     */  
    void createRetained() {
        this.retained = new AuralAttributesRetained();
        this.retained.setSource(this);
    }

    /** ****************************************
     *   
     *  Attribute Gain
     *
     * ****************************************/
    /**
     * Set Attribute Gain (amplitude) scale factor.
     * @param gain scale factor applied to amplitude of direct and reflected sound
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setAttributeGain(float gain) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ATTRIBUTE_GAIN_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes0")); 
        ((AuralAttributesRetained)this.retained).setAttributeGain(gain);
    }

    /**
     * Retrieve Attribute Gain (amplitude).
     * @return gain amplitude scale factor
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getAttributeGain() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ATTRIBUTE_GAIN_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes1")); 
        return ((AuralAttributesRetained)this.retained).getAttributeGain();
    }

    /**
     * Set Attribute Gain Rolloff.
     * @param rolloff atmospheric gain scale factor (changing speed of sound)
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setRolloff(float rolloff) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ROLLOFF_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes2")); 
        ((AuralAttributesRetained)this.retained).setRolloff(rolloff);
    }

    /**
     * Retrieve Attribute Gain Rolloff.
     * @return rolloff atmospheric gain scale factor (changing speed of sound)
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getRolloff() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_ROLLOFF_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes3")); 
        return ((AuralAttributesRetained)this.retained).getRolloff();
    }

    /**
     * Set Reflective Coefficient.  
     * Scales the amplitude of the early reflections of reverberated sounds
     * @param coefficient reflection/absorption factor applied to reflections
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setReflectionCoefficient(float coefficient) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REFLECTION_COEFFICIENT_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes4"));
        ((AuralAttributesRetained)this.retained).setReflectionCoefficient(coefficient);
    }

    /**
     * Retrieve Reflective Coefficient.
     * @return reflection coeff reflection/absorption factor
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getReflectionCoefficient() {
        if (isLiveOrCompiled())
             if (!this.getCapability(ALLOW_REFLECTION_COEFFICIENT_READ)) 
                 throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes21")); 
        return ((AuralAttributesRetained)this.retained).getReflectionCoefficient();
    }

    /*********************
     *
     * Early Reflection Delay 
     *
     ********************/
    /**
     * Set early Refection Delay Time.
     * In this form, the parameter specifies the time between the start of the
     * direct, unreflected sound and the start of first order early reflections.
     * In this method, this time is explicitly given in milliseconds.
     * @param reflectionDelay delay time before start of reverberation
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setReflectionDelay(float reflectionDelay) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REFLECTION_DELAY_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes22"));
        ((AuralAttributesRetained)this.retained).setReflectionDelay(reflectionDelay);
    }

    /**
     * Retrieve Reflection Delay Time.
     * @return reflection delay time 
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getReflectionDelay() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REFLECTION_DELAY_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes23"));
        return ((AuralAttributesRetained)this.retained).getReflectionDelay();
    }

    /** ******************
     *
     *  Reverb Coefficient
     *
     ********************/
    /**
     * Set Reverb Coefficient.  
     * Scale the amplitude of the late reflections including the decaying tail
     * of reverberated sound.
     * @param coefficient reflective/absorptive factor applied to late reflections
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setReverbCoefficient(float coefficient) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_COEFFICIENT_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes24"));
        ((AuralAttributesRetained)this.retained).setReverbCoefficient(coefficient);
    }

    /**
     * Retrieve Reverb Coefficient.
     * @return late reflection coeff. reflection/absorption factor
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getReverbCoefficient() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_COEFFICIENT_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes25"));
        return ((AuralAttributesRetained)this.retained).getReverbCoefficient();
    }

    /*********************
     *
     * Reverberation Delay 
     *
     ********************/
    /**
     * Set Reverberation Delay Time.
     * In this form, the parameter specifies the time between the start of the
     * direct, unreflected sound and the start of reverberation. In this
     * method, this time is explicitly given in milliseconds.
     * @param reverbDelay delay time before start of reverberation
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setReverbDelay(float reverbDelay) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_DELAY_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes5"));
        ((AuralAttributesRetained)this.retained).setReverbDelay(reverbDelay);
    }

    /**
     * Retrieve Reverberation Delay Time.
     * @return reverb delay time 
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getReverbDelay() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_DELAY_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes7"));
        return ((AuralAttributesRetained)this.retained).getReverbDelay();
    }

    /** ******************
     *
     *  Decay Time
     *
     ********************/
    /**
     * Set Decay Time  
     * Length of time from the start of late reflections reverberation volume
     * takes to decay to effective zero (-60 dB of initial signal amplitude).
     * @param decayTime of late reflections (reverb) in milliseconds
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setDecayTime(float decayTime) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DECAY_TIME_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes28"));
        ((AuralAttributesRetained)this.retained).setDecayTime(decayTime);
    }

    /**
     * Retrieve Decay Time.
     * @return reverb decay time
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getDecayTime() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DECAY_TIME_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes29"));
        return ((AuralAttributesRetained)this.retained).getDecayTime();
    }

    /** ******************
     *
     *  Decay Filter
     *
     ********************/
    /**
     * Set Decay Filter 
     * In this form, reverberation decay filtering is defined as a low-pass
     * filter, starting at the given reference frequency.  This allows for
     * higher frequencies to be attenuated at a different (typically faster)
     * rate than lower frequencies.
     * @param frequencyCutoff of reverberation decay low-pass filter
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setDecayFilter(float frequencyCutoff) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DECAY_FILTER_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes30"));
        ((AuralAttributesRetained)this.retained).setDecayFilter(frequencyCutoff);
    }

    /**
     * Retrieve Decay Filter.
     * @return reverb decay filter cutoff frequency
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getDecayFilter() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DECAY_FILTER_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes31"));
        return ((AuralAttributesRetained)this.retained).getDecayFilter();
    }

    /** ******************
     *
     *  Diffusion
     *
     ********************/
    /**
     * Set Diffusion. 
     * Sets the echo dispersement of reverberation to an amount between
     * the minimum (0.0) to the maximum (1.0) available.  Changing this
     * increases/decreases the 'smoothness' of reverb decay.
     * @param ratio reverberation echo dispersement factor
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setDiffusion(float ratio) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DIFFUSION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes32"));
        ((AuralAttributesRetained)this.retained).setDiffusion(ratio);
    }

    /**
     * Retrieve Diffusion.
     * @return reverb diffusion ratio
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getDiffusion() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DIFFUSION_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes33"));
        return ((AuralAttributesRetained)this.retained).getDiffusion();
    }

    /** ******************
     *
     *  Density
     *
     ********************/
    /**
     * Set Density.  
     * Sets the density of reverberation to an amount between
     * the minimum (0.0) to the maximum (1.0) available.  Changing this
     * effects the spectral coloration (timbre) of late reflections.
     * @param ratio reverberation modal density factor
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public void setDensity(float ratio) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DENSITY_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes34"));
        ((AuralAttributesRetained)this.retained).setDensity(ratio);
    }

    /**
     * Retrieve Density.
     * @return reverb density
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.3
     */
    public float getDensity() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DENSITY_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes35"));
        return ((AuralAttributesRetained)this.retained).getDensity();
    }

    /**
     * @deprecated As of Java 3D version 1.2, replaced by 
     * <code>setReverbBounds(Bounds)</code>
     */
    public void setReverbDelay(Bounds reverbVolume) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_DELAY_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes5"));
        ((AuralAttributesRetained)this.retained).setReverbBounds(reverbVolume);
    }

    /**
     * Set Reverberation Bounds volume.
     * In this form, the reverberation bounds volume parameter is used to
     * calculate the reverberation Delay and Decay times.  Specification
     * of a non-null bounding volume causes the explicit values given for 
     * Reverb Delay and Decay to be overridden by the implicit values 
     * calculated from these bounds.
     * ALLOW_REVERB_DELAY_WRITE flag used setting capability of this method.
     * @param reverbVolume the bounding region
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.2
     */
    public void setReverbBounds(Bounds reverbVolume) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_DELAY_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes26"));
        ((AuralAttributesRetained)this.retained).setReverbBounds(reverbVolume);
    }

    /**
     * Retrieve Reverberation Delay Bounds volume.
     * @return reverb bounds volume that defines the Reverberation space and
     * indirectly the delay/decay
     * ALLOW_REVERB_DELAY_READ flag used setting capability of this method.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @since Java 3D 1.2
     */
    public Bounds getReverbBounds() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_DELAY_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes27"));
        return ((AuralAttributesRetained)this.retained).getReverbBounds();
    }

    /** *******************
     *
     *  Reverberation Order
     *
     ********************/
    /**
     * Set Reverberation Order
     * This parameter limits the number of times reflections are added
     * to the reverberation being rendered.
     * A non-positive value specifies an unbounded number of reflections.
     * @param reverbOrder limit to the number of times reflections added to reverb signal
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setReverbOrder(int reverbOrder) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_REVERB_ORDER_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes8"));
        ((AuralAttributesRetained)this.retained).setReverbOrder(reverbOrder);
    }

    /**
     * Retrieve Reverberation Order
     * @return reverb order
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getReverbOrder() {
        if (!this.getCapability(ALLOW_REVERB_ORDER_READ)) 
            if (isLiveOrCompiled())
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes9"));
        return ((AuralAttributesRetained)this.retained).getReverbOrder();
    }

    /**
     * Set Distance Filter using a single array containing distances and 
     * frequency cutoff as pairs of values as a single  array of Point2f.
     * @param attenuation array of pairs of distance and frequency cutoff
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setDistanceFilter(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DISTANCE_FILTER_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes10")); 
        ((AuralAttributesRetained)this.retained).setDistanceFilter(attenuation);
    }

    /**
     * Set Distance Filter using separate arrays for distances and frequency
     * cutoff.  The distance and frequencyCutoff arrays should be of the same
     * length. If the frequencyCutoff array length is greater than the distance
     * array length, the frequencyCutoff array elements beyond the length of 
     * the distance array are ignored. If the frequencyCutoff array is shorter
     * than the distance array, the last frequencyCutoff array value is repeated
     * to fill an array of length equal to distance array.
     * @param distance array of float distance with corresponding cutoff values
     * @param frequencyCutoff array of frequency cutoff values in Hertz
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setDistanceFilter(float[] distance, 
                                        float[] frequencyCutoff) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DISTANCE_FILTER_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes10")); 
        ((AuralAttributesRetained)this.retained).setDistanceFilter(
                                distance, frequencyCutoff );
    }

    /**
     * Retrieve Distance Filter array length.
     * @return attenuation array length
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getDistanceFilterLength() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DISTANCE_FILTER_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes12"));
        return (((AuralAttributesRetained)this.retained).getDistanceFilterLength());
    } 
    /**
     * Retrieve Distance Filter as a single array containing distances
     * and frequency cutoff. The distance filter is copied into
     * the specified array.
     * The array must be large enough to hold all of the points. 
     * The individual array elements must be allocated by the caller.
     * @param attenuation array of pairs of distance and frequency cutoff values
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void getDistanceFilter(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DISTANCE_FILTER_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes12"));
        ((AuralAttributesRetained)this.retained).getDistanceFilter(attenuation);
    }

    /**
     * Retrieve Distance Filter in separate distance and frequency cutoff arrays.
     * The arrays must be large enough to hold all of the distance
     * and frequency cutoff values.
     * @param distance array
     * @param frequencyCutoff cutoff array
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void getDistanceFilter(float[] distance, 
                                        float[] frequencyCutoff) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_DISTANCE_FILTER_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes12")); 
        ((AuralAttributesRetained)this.retained).getDistanceFilter(
                                   distance, frequencyCutoff);
    }

    /**
     * This parameter specifies a scale factor applied to the frequency 
     * of sound during rendering playback.  If the Doppler effect is
     * disabled, this scale factor can be used to increase or 
     * decrease the original pitch of the sound.  During rendering,
     * this scale factor expands or contracts the usual frequency shift 
     * applied to the sound source due to Doppler calculations. 
     * Valid values are >= 0.0.
     * A value of zero causes playing sounds to pause.
     * @param frequencyScaleFactor factor applied to change of frequency
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setFrequencyScaleFactor(float frequencyScaleFactor) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_FREQUENCY_SCALE_FACTOR_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes15"));
        ((AuralAttributesRetained)this.retained).setFrequencyScaleFactor(
                                    frequencyScaleFactor);
    }

    /**
     * Retrieve Frequency Scale Factor.
     * @return scaleFactor factor applied to change of frequency
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getFrequencyScaleFactor() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_FREQUENCY_SCALE_FACTOR_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes17"));
        return ((AuralAttributesRetained)this.retained).getFrequencyScaleFactor();
    }

    /** ******************************
     *
     *  Velocity Scale Factor
     *
     *********************************/
    /**
     * Set Velocity scale factor applied during Doppler Effect calculation.
     * This parameter specifies a scale factor applied to the velocity of 
     * the sound relative to the listener's position and movement in relation 
     * to the sound's position and movement.  This scale factor is multipled
     * by the calculated velocity portion of the Doppler effect equation used 
     * during sound rendering.
     * A value of zero disables Doppler calculations.
     * @param velocityScaleFactor applied to velocity of sound in relation 
     * to listener
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setVelocityScaleFactor(float velocityScaleFactor) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_VELOCITY_SCALE_FACTOR_WRITE)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes19")); 
        ((AuralAttributesRetained)this.retained).setVelocityScaleFactor(
                                     velocityScaleFactor);
    }

    /**
     * Retrieve Velocity Scale Factor used to calculate Doppler Effect.
     * @return scale factor applied to Doppler velocity of sound
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public float getVelocityScaleFactor() {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_VELOCITY_SCALE_FACTOR_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("AuralAttributes20")); 
        return ((AuralAttributesRetained)this.retained).getVelocityScaleFactor();
    }


    /**
     * @deprecated As of Java 3D version 1.2, replaced by
     * <code>cloneNodeComponent(boolean forceDuplicate)</code>
     */
    public NodeComponent cloneNodeComponent() {
        AuralAttributes a = new AuralAttributes();
        a.duplicateNodeComponent(this, this.forceDuplicate);
        return a;
    }


   /**
     * Copies all AuralAttributes information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
     void duplicateAttributes(NodeComponent originalNodeComponent, 
			      boolean forceDuplicate) { 
	 super.duplicateAttributes(originalNodeComponent,
				   forceDuplicate);
	 
	 AuralAttributesRetained aural = (AuralAttributesRetained) originalNodeComponent.retained;
	 AuralAttributesRetained rt = (AuralAttributesRetained) retained;

	 rt.setAttributeGain(aural.getAttributeGain());
	 rt.setRolloff(aural.getRolloff());
	 rt.setReflectionCoefficient(aural.getReflectionCoefficient());
	 rt.setReverbDelay(aural.getReverbDelay());
	 rt.setReverbOrder(aural.getReverbOrder());
	 rt.setReverbBounds(aural.getReverbBounds());
	 rt.setFrequencyScaleFactor(aural.getFrequencyScaleFactor());
	 rt.setVelocityScaleFactor(aural.getVelocityScaleFactor());
	 int len = aural.getDistanceFilterLength();
	 float distance[] = new float[len];
	 float frequencyCutoff[] = new float[len];
	 aural.getDistanceFilter(distance, frequencyCutoff);
	 rt.setDistanceFilter(distance, frequencyCutoff);
    }
}
