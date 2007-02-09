/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.lang.Math;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


/**
 * The ConeSound node object defines a PointSound node whose sound source is
 * directed along a specific vector in space. A ConeSound source is attenuated
 * by gain scale factors and filters based on the angle between the vector from
 * the source to the listener, and the ConeSound's direction vector. This
 * attenuation is either a single spherical distance gain attenuation (as for
 * a general PointSound source) or dual front and back distance gain 
 * attenuations defining elliptical attenuation areas. The angular filter and the
 * active AuralAttribute component filter define what filtering is applied to
 * the sound source. (See AuralAtttribute class for more details on filtering.)
 * This node has the same attributes as a PointSound node with the addition of a
 * direction vector and an array of points each containing: angular distance (in
 * radians), gain scale factor, and filter (which for now consists of a lowpass
 * filter cutoff frequency). Similar to the definition of the back distance gain
 * array for PointSounds, a piece-wise linear curve (defined in terms of 
 * radians from the axis) specifies the slope of these additional attenuation
 * values.
 *   <P>
 *  Distance Gain attuation
 *   <P><UL>
 * A cone sound node can have one or two distance attenuation arrays.
 * If none are set, no distance gain attenuation is performed (equivalent
 * to using a distance gain of 1.0 for all distances). If only one distance
 * attenuation array is set, sphere attenuation is assumed. If both a front
 * and back distance attenuation are set, elliptical attenuation regions
 * are defined.
 *<P>
 * Use PointSound setDistanceGain() method to set the front distance
 * attenuation array separate from the back distance attenuation array.
 * A front distance attenuation array defines monotonically-increasing
 * distances from the sound source origin along the position direction
 * vector. A back distance attenuation array (if given) defines 
 * monotonically-increasing distances from the sound source origin along the
 * negative direction vector. The two arrays must be of the same length.
 * The backDistance[i] gain values must be less than or equal to
 * the frontDistance[i] gain values.
 * <P>
 * Gain scale factors are associated with distances from the listener to
 * the sound source via an array of (distance, gain-scale-factor) pairs.
 * The gain scale factor applied to the sound source is the linear
 * interpolated gain value between the distance value range that includes
 * the current distance from the listener to the sound source.
 *<P>
 * The getDistanceGainLength method defined for PointSound returns the length
 * of the all distance gain attenuation arrays, including the back distance
 * gain arrays.  Arrays passed into getDistanceGain methods should all
 * be at least this size.
 *  </UL> <P>
 *  Direction Methods
 *   <P><UL>
 * This value is the sound source's direction vector. It is the axis from
 * which angular distance is measured.
 * </UL><P>
 *  Angular Attenuation
 *   <P><UL>
 * Besides sound (linear) distance attenuation a ConeSound can optionally
 * define angular gain and filter attenuation.
 *   <P>
 * This attenuation is defined
 * as a triple of (angular distance, gain-scale-factor, filter). The
 * distance is measured as the angle in radians between the ConeSound's
 * direction vector and the vector from the sound source position to the
 * listener. Both the gain scale factor and filter applied to the sound
 * source is the linear interpolation of values between the distance value
 * range that includes the angular distance from the sound source axis.
 *<P>
 * If this is not set, no angular gain attenuation or filtering is performed
 * (equivalent to using an angular gain scale factor of 1.0 and an angular
 * filter of Sound.NO_FILTER for all distances). 
 *   <P>
 * If angular distance from the listener-sound-position vector and a sound's
 * direction vector is less than the first distance in the array, only the first
 * gain scale factor and first filter are applied to the sound source.
 * This creates a conical region around the listener within which the sound
 * is uniformly attenuated by first gain and first filter in the array.
 *   <P>
 * If the distance from the listener-sound-position vector and the sound's
 * direction vector is greater than the last distance in the array, the last gain
 * scale factor and last filter are applied to the sound source.
 *   <P>
 * Distance elements in this array of points is a monotonically-increasing
 * set of floating point numbers measured from 0 to p radians. Gain scale
 * factors elements in this list of points can be any positive floating
 * point numbers.  While for most applications this list of gain scale
 * factors will usually be monotonically-decreasing, they do not have to be.
 * The filter (for now) is a single simple frequency cutoff value.
 *   <P>
 * The getAngularAttenuationArrayLength method returns the length of the angular
 * attenuation arrays. Arrays passed into getAngularAttenuation methods
 * should all be at least this size.
 *</UL>
 */ 

public class ConeSound extends PointSound {
    // Constants
    //
    // These flags, when enabled using the setCapability method, allow an
    // application to invoke methods that respectively read and write direction
    // and angular attenuation array. These capability flags are enforced only
    // when the node is part of a live or compiled scene graph.

  /**
   * Specifies that this ConeSound allows access to its object's direction
   * information.
   */
  public static final int
    ALLOW_DIRECTION_READ = CapabilityBits.CONE_SOUND_ALLOW_DIRECTION_READ;

  /**
   * Specifies that this ConeSound allows writing to its object's direction
   * information.
   */
  public static final int
    ALLOW_DIRECTION_WRITE = CapabilityBits.CONE_SOUND_ALLOW_DIRECTION_WRITE;

  /**
   * Specifies that this ConeSound allows access to its object's cone params
   * information.
   */
  public static final int
    ALLOW_ANGULAR_ATTENUATION_READ = CapabilityBits.CONE_SOUND_ALLOW_ANGULAR_ATTENUATION_READ;

  /**
   * Specifies that this ConeSound allows writing to its object's cone params
   * information.
   */
  public static final int
    ALLOW_ANGULAR_ATTENUATION_WRITE = CapabilityBits.CONE_SOUND_ALLOW_ANGULAR_ATTENUATION_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_DIRECTION_READ,
	ALLOW_ANGULAR_ATTENUATION_READ
    };

    /**
     * Constructs and initializes a new ConeSound node using default
     * parameters.  The following default values are used:
     * <ul>
     * Direction vector: (0.0, 0.0, 1.0) <br>
     * Angular attenuation:
     *     ((0.0, 1.0, Sound.NO_FILTER),(p/2, 0.0, Sound.NO_FILTER)) <br>
     * </ul>
     */
    public ConeSound() {
        // Uses default values defined in ConeSoundRetained.java
       super();
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a ConeSound node object using only the provided parameter
     * values for sound, overall initial gain, position, and direction.  The
     * remaining fields are set to the default values above. This form uses
     * Point3f as input for its position and Vector3f for direction.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param position 3D location of source
     * @param direction 3D vector defining cone's axis
     */
     public ConeSound(MediaContainer soundData,
                      float initialGain,
                      Point3f position, 
                      Vector3f direction) {

        super(soundData, initialGain, position );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setDirection(direction);
    }

    /**
     * Constructs a ConeSound node object using only the provided parameter
     * values for sound, overall initial gain, position, and direction.  The
     * remaining fields are set to the default values above. This form uses
     * individual float parameters for the elements of the position and 
     * direction vectors.
     * @param soundData sound source data
     * @param initialGain amplitude scale factor applied to sound
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param dirX x coordinate cones' axii vector
     * @param dirY y coordinate cones' axii vector
     * @param dirZ z coordinate cones' axii vector
     */
     public ConeSound(MediaContainer soundData,
                      float initialGain,
                      float posX, float posY, float posZ, 
                      float dirX, float dirY, float dirZ) {

        super(soundData, initialGain, posX, posY, posZ );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setDirection(dirX, dirY, dirZ);
    }

    /**
     * Constructs a ConeSound node object using all the provided PointSound
     * parameter values.  This form uses points or vectors as input for its 
     * position, direction, and front/back distance attenuation arrays.
     *<P>
     * Unlike the single distance gain attenuation array for PointSounds which
     * define spherical areas about the sound source between which gains are
     * linearly interpolated, this directed ConeSound can have two distance gain
     * attenuation arrays that define ellipsoidal attenuation areas. See the 
     * setDistanceGain PointSound method for details on how the separate distance
     * and distanceGain arrays are interpreted.
     *<P>
     * The ConeSound's direction vector and angular measurements are defined in
     * the local coordinate system of the node.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param position 3D location of source
     * @param frontDistanceAttenuation array of (distance,gain) pairs controlling 
     * attenuation values along the positive direction axis
     * @param backDistanceAttenuation array of (distance,gain) pairs controlling 
     * attenuation values along the negative direction axis
     * @param direction vector defining cones' axii
     */
     public ConeSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      Point3f position,
                      Point2f[] frontDistanceAttenuation,
                      Point2f[] backDistanceAttenuation,
                      Vector3f direction) {

        super(soundData, initialGain, loopCount, release, continuous, enable,
                      region, priority, position, frontDistanceAttenuation );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setBackDistanceGain(
                      backDistanceAttenuation);
        ((ConeSoundRetained)this.retained).setDirection(direction);
    }

    /**
     * Constructs a ConeSound node object using the provided parameter values.
     * This form uses individual float parameters for the elements of the 
     * position, direction, and two distance attenuation arrays.
     * Unlike the single distance gain attenuation array for PointSounds, which 
     * define spherical areas about the sound source between which gains are
     * linearly interpolated, this directed ConeSound can have two distance
     * gain attenuation arrays that define ellipsoidal attenuation areas. 
     * See the setDistanceGain PointSound method for details on how the 
     * separate distance and distanceGain arrays are interpreted.
     * The ConeSound's direction vector and angular measurements are defined
     * in the local coordinate system of the node.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param frontDistance array of front distance values used for attenuation
     * @param frontDistanceGain array of front gain scale factors used for attenuation
     * @param backDistance array of back distance values used for attenuation
     * @param backDistanceGain array of back gain scale factors used for attenuation
     * @param dirX x coordinate cones' axii vector
     * @param dirY y coordinate cones' axii vector
     * @param dirZ z coordinate cones' axii vector
     */
    public ConeSound(MediaContainer soundData,
                     float initialGain,
                     int loopCount,
                     boolean release,
                     boolean continuous,
                     boolean enable,
                     Bounds  region,
                     float   priority,
                     float posX, float posY, float posZ,
                     float[] frontDistance,
                     float[] frontDistanceGain,
                     float[] backDistance,
                     float[] backDistanceGain,
                     float dirX, float dirY, float dirZ ) {
        super(soundData, initialGain, loopCount, release, continuous, enable,
                     region, priority, posX, posY, posZ, 
                     frontDistance, frontDistanceGain );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setDirection(dirX, dirY, dirZ);
        ((ConeSoundRetained)this.retained).setBackDistanceGain(
                     backDistance, backDistanceGain );
    }

    /**
     * Constructs a ConeSound node object using all the provided PointSound
     * parameter values, which include a single spherical distance attenuation
     * array, but includes an angular attenuation array. 
     * This form uses points and vectors as input for its position, direction,
     * single spherical distanceAttenuation array, and angularAttenuation array.
     * It also accepts arrays of points for the distance attenuation and angular
     * values. Each Point2f in the distanceAttenuation array contains a distance
     * and a gain scale factor. Each Point3f in the angularAttenuation array
     * contains an angular distance, a gain scale factor, and a filtering value
     * (which is currently defined as a simple cutoff frequency).
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param position 3D location of source
     * @param distanceAttenuation array of (distance,gain) pairs controlling 
     * attenuation values along the positive direction axis
     * @param direction vector defining cones' axii
     * @param angularAttenuation array of tuples defining angular gain/filtering
     */
     public ConeSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      Point3f position,
                      Point2f[] distanceAttenuation,
                      Vector3f direction,
                      Point3f[] angularAttenuation ) {

        super(soundData, initialGain, loopCount, release, continuous, enable,
                      region, priority, position, distanceAttenuation );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setDirection(direction);
        ((ConeSoundRetained)this.retained).setAngularAttenuation(
                      angularAttenuation);
    }

    /**
     * Constructs a ConeSound node object using all the provided PointSound
     * parameter values, which include a single spherical distance attenuation
     * array, but includes an angular attenuation array.
     * This form uses individual float parameters for elements of position,
     * direction, distanceAttenuation array, and angularAttenuation array.
     * It also accepts separate arrays for the distance and gain scale factors
     * components of distance attenuation, and separate arrays for the angular
     * distance, angular gain, and filtering components of angular attenuation.
     * See the setDistanceGain ConeSound method for details on how the separate
     * distance and distanceGain arrays are interpreted. See the 
     * setAngularAttenuation ConeSound method for details on how the separate
     * angularDistance, angularGain, and filter arrays are interpreted.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param distance array of front distance values used for attenuation
     * @param distanceGain array of front gain scale factors used for attenuation
     * @param dirX x coordinate cones' axii vector
     * @param dirY y coordinate cones' axii vector
     * @param dirZ z coordinate cones' axii vector
     * @param angle array of angle radians for angularAttenuation
     * @param angularGain array of gain scale factors for angularAttenuation
     * @param frequencyCutoff array of lowpass filter values in Hertz
     */
    public ConeSound(MediaContainer soundData,
                     float initialGain,
                     int loopCount,
                     boolean release,
                     boolean continuous,
                     boolean enable,
                     Bounds  region,
                     float   priority,
                     float posX, float posY, float posZ,
                     float[] distance,
                     float[] distanceGain,
                     float dirX, float dirY, float dirZ,
                     float[] angle,
                     float[] angularGain,
                     float[] frequencyCutoff) {
        super(soundData, initialGain, loopCount, release, continuous, enable,
                     region, priority, posX, posY, posZ, 
                     distance, distanceGain );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setDirection(dirX, dirY, dirZ);
        ((ConeSoundRetained)this.retained).setAngularAttenuation(angle,
                     angularGain, frequencyCutoff);
    }

    /**
     * Constructs and initializes a new Cone Sound node explicitly setting all
     * PointSound and ConeSound fields as arguments: the PointSound position,
     * front and back distance attenuation Point2f array, and ConeSound
     * direction vector and Point3f angular attenuation.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param position 3D location of source
     * @param frontDistanceAttenuation array of (distance,gain) pairs controlling 
     * attenuation values along the positive direction axis
     * @param backDistanceAttenuation array of (distance,gain) pairs controlling 
     * attenuation values along the negative direction axis
     * @param direction vector defining cones' axii
     * @param angularAttenuation array of tuples defining angular gain/filtering
     */
     public ConeSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      Point3f position,
                      Point2f[] frontDistanceAttenuation,
                      Point2f[] backDistanceAttenuation,
                      Vector3f direction,
                      Point3f[] angularAttenuation ) {

        super(soundData, initialGain, loopCount, release, continuous, enable,
                      region, priority, position, frontDistanceAttenuation );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setBackDistanceGain(
                      backDistanceAttenuation);
        ((ConeSoundRetained)this.retained).setDirection(direction);
        ((ConeSoundRetained)this.retained).setAngularAttenuation(
                      angularAttenuation);
    }

    /**
     * Constructs and initializes a new Cone Sound node explicitly setting all
     * PointSound and ConeSound fields as arguments but all the vector and point
     * arguments are broken into individual float array components.
     * @param soundData sound source data associated with this node
     * @param initialGain amplitude scale factor applied to sound
     * @param loopCount number of times sound is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param frontDistance array of front distance values used for attenuation
     * @param frontDistanceGain array of front gain scale factors used for attenuation
     * @param backDistance array of back distance values used for attenuation
     * @param backDistanceGain array of back gain scale factors used for attenuation
     * @param dirX x coordinate cones' axii vector
     * @param dirY y coordinate cones' axii vector
     * @param dirZ z coordinate cones' axii vector
     * @param angle array of angle radians for angularAttenuation
     * @param angularGain array of gain scale factors for angularAttenuation
     * @param frequencyCutoff array of lowpass filter values in Hertz
     */
    public ConeSound(MediaContainer soundData,
                     float initialGain,
                     int loopCount,
                     boolean release,
                     boolean continuous,
                     boolean enable,
                     Bounds  region,
                     float   priority,
                     float posX, float posY, float posZ,
                     float[] frontDistance,
                     float[] frontDistanceGain,
                     float[] backDistance,
                     float[] backDistanceGain,
                     float dirX, float dirY, float dirZ,
                     float[] angle,
                     float[] angularGain,
                     float[] frequencyCutoff) {
        super(soundData, initialGain, loopCount, release, continuous, enable,
                     region, priority, posX, posY, posZ, 
                     frontDistance, frontDistanceGain );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((ConeSoundRetained)this.retained).setBackDistanceGain(
                     backDistance, backDistanceGain );
        ((ConeSoundRetained)this.retained).setDirection(dirX, dirY, dirZ);
        ((ConeSoundRetained)this.retained).setAngularAttenuation(angle,
                     angularGain, frequencyCutoff);
    }

    /**
     * Creates the retained mode ConeSoundRetained object that this
     * ConeSound object will point to.
     */
    void createRetained() {
	this.retained = new ConeSoundRetained();
	this.retained.setSource(this);
    }

    //
    // OVERLOADED Sound methods
    //
    /**
     * Sets this sound's distance gain elliptical attenuation - 
     * where gain scale factor is applied to sound based on distance listener
     * is from sound source.
     * @param frontAttenuation defined by pairs of (distance,gain-scale-factor)
     * @param backAttenuation defined by pairs of (distance,gain-scale-factor)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setDistanceGain(Point2f[] frontAttenuation,
                                      Point2f[] backAttenuation ) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound0"));
  
        ((ConeSoundRetained)this.retained).setDistanceGain(frontAttenuation,
                             backAttenuation);
    }

    /**
     * Sets this sound's distance gain attenuation as an array of Point2fs.
     * @param frontDistance array of monotonically-increasing floats
     * @param frontGain array of non-negative scale factors
     * @param backDistance array of monotonically-increasing floats
     * @param backGain array of non-negative scale factors
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setDistanceGain(float[] frontDistance, float[] frontGain,
                                      float[] backDistance, float[] backGain) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound0"));
  
        ((ConeSoundRetained)this.retained).setDistanceGain(
                 frontDistance, frontGain, backDistance, backGain);
    }

    /**
     * Sets this sound's back distance gain attenuation - where gain scale 
     * factor is applied to sound based on distance listener along the negative
     * sound direction axis from sound source.
     * @param attenuation defined by pairs of (distance,gain-scale-factor)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setBackDistanceGain(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound0"));
  
        ((ConeSoundRetained)this.retained).setBackDistanceGain(attenuation);
    }

    /**
     * Sets this sound's back distance gain attenuation as separate arrays.
     * @param distance array of monotonically-increasing floats
     * @param gain array of non-negative scale factors
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setBackDistanceGain(float[] distance, float[] gain) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound0"));
  
        ((ConeSoundRetained)this.retained).setBackDistanceGain(distance, gain);
    }

    /**
     * Gets this sound's elliptical distance attenuation. The
     * attenuation values are copied into the specified arrays.
     * The arrays must be large enough to hold all of the
     * forward distances and backward distances attenuation values.
     * The individual array elements must be allocated by the
     * caller. The Point2f x,y values are defined as follows:
     * x is the distance, y is the gain.
     * @param frontAttenuation arrays containing forward distances 
     * attenuation pairs
     * @param backAttenuation arrays containing backward distances 
     * attenuation pairs
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */ 
    public void getDistanceGain(Point2f[] frontAttenuation,
                                      Point2f[] backAttenuation) { 
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound2"));
  
        ((ConeSoundRetained)this.retained).getDistanceGain(
                   frontAttenuation, backAttenuation);
    }

    /**
     * Gets this sound's elliptical distance gain attenuation values in 
     * separate arrays. The arrays must be large enough to hold all
     * of the values.
     * @param frontDistance array of float distances along the sound axis
     * @param frontGain array of non-negative scale factors associated with 
     * front distances
     * @param backDistance array of float negative distances along the sound 
     * axis
     * @param backGain array of non-negative scale factors associated with 
     * back distances
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */ 
    public void getDistanceGain(float[] frontDistance, float[] frontGain,
                                      float[] backDistance, float[] backGain) { 
        if (isLiveOrCompiled())
        if(!this.getCapability(ALLOW_DISTANCE_GAIN_READ))
            throw new CapabilityNotSetException(J3dI18N.getString("ConeSound10"));
      
        ((ConeSoundRetained)this.retained).getDistanceGain(
                 frontDistance, frontGain, backDistance, backGain);
    }

    /**
     * Sets this sound's direction from the vector provided.
     * @param direction the new direction
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDirection(Vector3f direction) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DIRECTION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound3"));
   
        ((ConeSoundRetained)this.retained).setDirection(direction);
    }

    /**
     * Sets this sound's direction from the three values provided.
     * @param x the new x direction
     * @param y the new y direction
     * @param z the new z direction
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDirection(float x, float y, float z) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DIRECTION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound3"));

        ((ConeSoundRetained)this.retained).setDirection(x,y,z);
    }

    /**
     * Retrieves this sound's direction and places it in the
     * vector provided.
     * @param direction axis of cones; 'direction' of sound
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDirection(Vector3f direction) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DIRECTION_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound5"));

       ((ConeSoundRetained)this.retained).getDirection(direction);
    }

    /**
     * Sets this sound's angular gain attenuation (not including filter).
     * In this form of setAngularAttenuation, only the angular distances 
     * and angular gain scale factors pairs are given. The filter values for 
     * these tuples are implicitly set to Sound.NO_FILTER. 
     * @param attenuation array containing angular distance and gain
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAngularAttenuation(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound6"));

        ((ConeSoundRetained)this.retained).setAngularAttenuation(attenuation);
    }

    /**
     * In the second form of setAngularAttenuation, an array of all three values 
     * is supplied.
     * @param attenuation array containing angular distance, gain, and filter
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAngularAttenuation(Point3f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound6"));

        ((ConeSoundRetained)this.retained).setAngularAttenuation(attenuation);
    }

    /**
     * Sets angular attenuation including gain and filter using separate arrays.
     * The third form of setAngularAttenuation accepts three separate arrays for
     * these angular attenuation values. These arrays should be of the same
     * length.  If the angularGain or filtering array length is greater than
     * angularDistance array length, the array elements beyond the length of
     * the angularDistance array are ignored. If the angularGain or filtering
     * array is shorter than the angularDistance array, the last value of the
     * short array is repeated to fill an array of length equal to 
     * angularDistance array.
     * @param distance array containing angular distance
     * @param gain array containing angular gain attenuation
     * @param filter array containing angular low-pass frequency cutoff values
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setAngularAttenuation(float[] distance, float[] gain,
                                         float[] filter) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound6"));

        ((ConeSoundRetained)this.retained).setAngularAttenuation(distance,
                      gain, filter);
    }

    /**
     * Retrieves angular attenuation array length.
     * All arrays are forced to same size.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public int getAngularAttenuationLength() {
        if (isLiveOrCompiled()) 
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_READ)) 
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound9")); 
 
       return (((ConeSoundRetained)this.retained).getAngularAttenuationLength());
    }

    /**
     * Copies the array of attenuation values from this sound, including 
     * gain and filter, into the specified array. The array must be
     * large enough to hold all of the points. The individual array
     * elements must be allocated by the caller. The Point3f x,y,z values
     * are defined as follows: x is the angular distance, y is
     * the angular gain attenuation, and z is the frequency
     * cutoff.
     * @param attenuation the array to receive the attenuation values 
     * applied to gain when listener is between cones
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getAngularAttenuation(Point3f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound9"));

       ((ConeSoundRetained)this.retained).getAngularAttenuation(attenuation);
    }

    /**
     * Copies the array of attenuation values from this sound,
     * including gain and filter, into the separate arrays.
     * The arrays must be large enough to hold all of the values.
     * @param distance array containing angular distance
     * @param gain array containing angular gain attenuation
     * @param filter array containing angular low-pass frequency cutoff values
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getAngularAttenuation(float[] distance, float[] gain,
                                         float[] filter) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANGULAR_ATTENUATION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("ConeSound9"));

        ((ConeSoundRetained)this.retained).getAngularAttenuation(distance,
                      gain, filter);
    }

    /**
     * Creates a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        ConeSound c = new ConeSound();
        c.duplicateNode(this, forceDuplicate);
        return c;
    }

    /**
     * Copies all node information from <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.
     * <P>
     * For any <code>NodeComponent</code> objects
     * contained by the object being duplicated, each <code>NodeComponent</code>
     * object's <code>duplicateOnCloneTree</code> value is used to determine
     * whether the <code>NodeComponent</code> should be duplicated in the new node
     * or if just a reference to the current node should be placed in the
     * new node.  This flag can be overridden by setting the
     * <code>forceDuplicate</code> parameter in the <code>cloneTree</code>
     * method to <code>true</code>.
     *
     * <br>
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     * @exception ClassCastException if originalNode is not an instance of 
     *  <code>ConeSound</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }


   /**
     * Copies all ConeSound information from
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

	ConeSoundRetained orgRetained = (ConeSoundRetained)originalNode.retained;
	ConeSoundRetained thisRetained = (ConeSoundRetained)this.retained;

	// front distance gain & attenuation is set in super      
	// set back distance gain only
	int len = orgRetained.getDistanceGainLength();
	float distance[] = new float[len];
	float gain[] = new float[len];
	orgRetained.getDistanceGain(null, null,distance, gain);
	thisRetained.setBackDistanceGain(distance, gain);
	
	Vector3f v = new Vector3f();
	orgRetained.getDirection(v);
	thisRetained.setDirection(v);
	
	len = orgRetained.getAngularAttenuationLength();
	distance = gain = null;
	float angle[] = new float[len];
	float angularGain[] = new float[len];
	float frequencyCutoff[] = new float[len];

	orgRetained.getAngularAttenuation(angle, angularGain,
				    frequencyCutoff);

	thisRetained.setAngularAttenuation(angle, angularGain, frequencyCutoff);
    }

}
