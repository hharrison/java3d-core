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

package javax.media.j3d;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;


/**
 * The PointSound node (a sub-class of the Sound node) defines a spatially
 * located sound source whose waves radiate uniformly in all directions from
 * a given location in space.  It has the same attributes as a Sound object
 * with the addition of a location and the specification of distance-based
 * gain attenuation for listener positions between an array of distances.
 *<P>
 * A sound's amplitude is attenuated based on the distance between the listener
 * and the sound source position. A piecewise linear curve (defined in terms of
 * pairs of distance and gain scale factor) specifies the gain scale factor slope.
 *
 * The PointSound's location and attenuation distances are defined in the local
 * coordinate system of the node.
 *<P>
 *  Distance Gain Attenuation
 * <UL>
 * Associated with distances from the listener to the sound source via an
 * array of (distance, gain-scale-factor) pairs. The gain scale factor
 * applied to the sound source is the linear interpolated gain value between
 * the distance value range that includes the current distance from
 * the listener to the sound source. If the distance from the listener to
 * the sound source is less than the first distance in the array, the first
 * gain scale factor is applied to the sound source. This creates a
 * spherical region around the listener within which all sound gain is
 * uniformly scaled by the first gain in the array.  If the distance from
 * the listener to the sound source is greater than the last distance in
 * the array, the last gain scale factor is applied to the sound source.
 *<P>
 * Distance elements in this array of Point2f is a monotonically-increasing
 * set of floating point numbers measured from the location of the sound
 * source. Gain scale factors elements in this list of pairs can be any
 * positive floating point numbers. While for most applications this list
 * of gain scale factors will usually be monotonically-decreasing, they
 * do not have to be.
 * If this
 * is not set, no distance gain attenuation is performed (equivalent to
 * using a distance gain of 1.0 for all distances).
 *<P>
 * getDistanceGainLength method returns the length of the distance gain
 * attenuation arrays. Arrays passed into getDistanceGain methods should all
 * be at least this size.
 *<P>
 * There are two methods for getDistanceGain, one returning an array of
 * points, the other returning separate arrays for each attenuation
 * component.</UL>
 */

public class PointSound extends Sound {
    // Constants
    //
    // These flags, when enabled using the setCapability method, allow an
    // application to invoke methods that respectively read and write the position
    // and the distance gain array. These capability flags are enforced only when
    // the node is part of a live or compiled scene graph

  /**
   * Specifies that this node allows access to its object's position
   * information.
   */
  public static final int
    ALLOW_POSITION_READ = CapabilityBits.POINT_SOUND_ALLOW_POSITION_READ;

  /**
   * Specifies that this node allows writing to its object's position
   * information.
   */
  public static final int
    ALLOW_POSITION_WRITE = CapabilityBits.POINT_SOUND_ALLOW_POSITION_WRITE;

  /**
   * Specifies that this node allows access to its object's distance
   * gain attenuation information.
   */
  public static final int
    ALLOW_DISTANCE_GAIN_READ = CapabilityBits.POINT_SOUND_ALLOW_DISTANCE_GAIN_READ;

  /**
   * Specifies that this node allows writing to its object's distance
   * gain attenuation information.
   */
  public static final int
    ALLOW_DISTANCE_GAIN_WRITE = CapabilityBits.POINT_SOUND_ALLOW_DISTANCE_GAIN_WRITE;


    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_POSITION_READ,
        ALLOW_DISTANCE_GAIN_READ
    };

    /**
     * Constructs and initializes a new PointSound node using default
     * parameters.  The following default values are used:
     * <ul>
     * position vector: (0.0, 0.0, 0.0)<br>
     * Back attenuation: null<br>
     * distance gain attenuation: null (no attenuation performed)<br>
     * </ul>
     */
    public PointSound() {
	// Uses default values defined for Sound and PointSound nodes
        super();
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

    }

    /**
     * Constructs a PointSound node object using only the provided parameter
     * values for sound data, sample gain, and position. The remaining fields
     * are set to the above default values. This form uses a point as input for
     * its position.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param position 3D location of source
     */
    public PointSound(MediaContainer soundData,
                      float initialGain,
                      Point3f position) {
        super(soundData, initialGain);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(position);
    }

     /**
      * Constructs a PointSound node object using only the provided parameter
      * values for sound data, sample gain, and position. The remaining fields
      * are set to to the above default values. This form uses individual float
      * parameters for the elements of the position point.
      * @param soundData sound data associated with this sound source node
      * @param initialGain amplitude scale factor applied to sound source data
      * @param posX x coordinate of location of source
      * @param posY y coordinate of location of source
      * @param posZ z coordinate of location of source
      */
     public PointSound(MediaContainer soundData,
                      float initialGain,
                      float posX, float posY, float posZ ) {
        super(soundData, initialGain);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(posX,posY,posZ);
    }

    // The next four constructors fill all this classes fields with the provided
    // arguments values.
    // See the header for the setDistanceGain method for details on how the
    // those  arrays are interpreted.

    /**
     * Construct a PointSound object accepting Point3f as input for the position
     * and accepting an array of Point2f for the distance attenuation values
     * where each pair in the array contains a distance and a gain scale factor.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param loopCount number of times loop is looped
     * @param release flag denoting playing sound data to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param position 3D location of source
     * @param distanceGain array of (distance,gain) pairs controling attenuation
     */
    public PointSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      Point3f position,
                      Point2f[] distanceGain) {

        super(soundData, initialGain, loopCount, release, continuous,
                   enable, region, priority );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(position);
        ((PointSoundRetained)this.retained).setDistanceGain(distanceGain);
    }

    /**
     * Construct a PointSound object accepting individual float parameters for
     * the elements of the position point, and accepting an array of Point2f for
     * the distance attenuation values where each pair in the array contains a
     * distance and a gain scale factor.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param loopCount number of times loop is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param distanceGain array of (distance,gain) pairs controling attenuation
     */
    public PointSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      float posX, float posY, float posZ,
                      Point2f[] distanceGain) {

        super(soundData, initialGain, loopCount, release,
              continuous, enable, region, priority );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(posX,posY,posZ);
        ((PointSoundRetained)this.retained).setDistanceGain(distanceGain);
    }

    /**
     * Construct a PointSound object accepting points as input for the position.
     * and accepting separate arrays for the distance and gain scale factors
     * components of distance attenuation.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param loopCount number of times loop is looped
     * @param release flag denoting playing sound data to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param position 3D location of source
     * @param attenuationDistance array of distance values used for attenuation
     * @param attenuationGain array of gain scale factors used for attenuation
     */
    public PointSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      Point3f position,
                      float[] attenuationDistance,
                      float[] attenuationGain) {

        super(soundData, initialGain, loopCount, release, continuous,
                enable, region, priority );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(position);
        ((PointSoundRetained)this.retained).setDistanceGain(
                        attenuationDistance, attenuationGain);
    }

    /**
     * Construct a PointSound object accepting individual float parameters for
     * the elements of the position points, and accepting separate arrays for
     * the distance and gain scale factors components of distance attenuation.
     * @param soundData sound data associated with this sound source node
     * @param initialGain amplitude scale factor applied to sound source
     * @param loopCount number of times loop is looped
     * @param release flag denoting playing sound to end
     * @param continuous denotes that sound silently plays when disabled
     * @param enable sound switched on/off
     * @param region scheduling bounds
     * @param priority playback ranking value
     * @param posX x coordinate of location of source
     * @param posY y coordinate of location of source
     * @param posZ z coordinate of location of source
     * @param attenuationDistance array of distance values used for attenuation
     * @param attenuationGain array of gain scale factors used for attenuation
     */
    public PointSound(MediaContainer soundData,
                      float initialGain,
                      int loopCount,
                      boolean release,
                      boolean continuous,
                      boolean enable,
                      Bounds  region,
                      float   priority,
                      float posX, float posY, float posZ,
                      float[] attenuationDistance,
                      float[] attenuationGain) {

        super(soundData, initialGain, loopCount, release,
              continuous, enable, region, priority );

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointSoundRetained)this.retained).setPosition(posX,posY,posZ);
        ((PointSoundRetained)this.retained).setDistanceGain(
                        attenuationDistance, attenuationGain);
    }

    /**
     * Creates the retained mode PointSoundRetained object that this
     * PointSound object will point to.
     */
    void createRetained() {
	this.retained = new PointSoundRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets this sound's location from the vector provided.
     * @param position the new location
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPosition(Point3f position) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound0"));

        ((PointSoundRetained)this.retained).setPosition(position);
    }

    /**
     * Sets this sound's position from the three values provided.
     * @param x the new x position
     * @param y the new y position
     * @param z the new z position
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPosition(float x, float y, float z) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound0"));

        ((PointSoundRetained)this.retained).setPosition(x,y,z);
    }

    /**
     * Retrieves this sound's direction and places it in the
     * vector provided.
     * @param position the variable to receive the direction vector
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getPosition(Point3f position) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound2"));

        ((PointSoundRetained)this.retained).getPosition(position);
    }

    /**
     * Sets this sound's distance gain attenuation - where gain scale factor
     * is applied to sound based on distance listener is from sound source.
     * This form of setDistanceGain takes these pairs of values as an array of
     * Point2f.
     * @param attenuation defined by pairs of (distance,gain-scale-factor)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDistanceGain(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound3"));

        ((PointSoundRetained)this.retained).setDistanceGain(attenuation);
    }

    /**
     * Sets this sound's distance gain attenuation as an array of Point2fs.
     * This form of setDistanceGain accepts two separate arrays for these values.
     * The distance and gainScale arrays should be of the same length. If the
     * gainScale array length is greater than the distance array length, the
     * gainScale array elements beyond the length of the distance array are
     * ignored. If the gainScale array is shorter than the distance array, the
     * last gainScale array value is repeated to fill an array of length equal
     * to distance array.
     * @param distance array of monotonically-increasing floats
     * @param gain array of non-negative scale factors
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDistanceGain(float[] distance, float[] gain) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound3"));

        ((PointSoundRetained)this.retained).setDistanceGain(distance, gain);
    }

    /**
     * Get the length of this node's distance gain attenuation arrays.
     * @return distance gain attenuation array length
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getDistanceGainLength() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound4"));

        return (((PointSoundRetained)this.retained).getDistanceGainLength());
    }

    /**
     * Gets this sound's distance attenuation. The distance attenuation
     * pairs are copied into the specified array.
     * The array must be large enough to hold all of the points.
     * The individual array elements must be allocated by the caller.
     * @param attenuation arrays containing distance attenuation pairs
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDistanceGain(Point2f[] attenuation) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DISTANCE_GAIN_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("PointSound4"));

        ((PointSoundRetained)this.retained).getDistanceGain(attenuation);
    }

    /**
     * Gets this sound's distance gain attenuation values in separate arrays.
     * The arrays must be large enough to hold all of the values.
     * @param distance array of float distance from sound source
     * @param gain array of non-negative scale factors associated with
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDistanceGain(float[] distance, float[] gain) {
        if (isLiveOrCompiled())
        if(!this.getCapability(ALLOW_DISTANCE_GAIN_READ))
            throw new CapabilityNotSetException(J3dI18N.getString("PointSound4"));

        ((PointSoundRetained)this.retained).getDistanceGain(distance,gain);
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
        PointSound p = new PointSound();
        p.duplicateNode(this, forceDuplicate);
        return p;
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
     *  <code>PointSound</code>
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public void duplicateNode(Node originalNode, boolean forceDuplicate) {
	checkDuplicateNode(originalNode, forceDuplicate);
    }


   /**
     * Copies all PointSound information from
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

	PointSoundRetained orgRetained = (PointSoundRetained)originalNode.retained;
	PointSoundRetained thisRetained = (PointSoundRetained)this.retained;

	Point3f p = new Point3f();
	orgRetained.getPosition(p);
	thisRetained.setPosition(p);

	int len = orgRetained.getDistanceGainLength();
	float distance[] = new float[len];
	float gain[] = new float[len];
	orgRetained.getDistanceGain(distance, gain);
	thisRetained.setDistanceGain(distance, gain);
    }

}
