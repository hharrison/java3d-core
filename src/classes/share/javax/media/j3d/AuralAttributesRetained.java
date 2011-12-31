/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Hashtable;
import javax.vecmath.Point2f;

/**
 * The AuralAttributesRetained object defines all rendering state that can
 * be set as a component object of a retained Soundscape node.
 */
class AuralAttributesRetained extends NodeComponentRetained {

     /**
      *  Gain Scale Factor applied to source with this attribute
      */
     float      attributeGain = 1.0f;      // Valid values are >= 0.0.

     /**
      * Atmospheric Rolloff - speed of sound - coeff
      *    Normal gain attenuation based on distance of sound from
      *    listener is scaled by a rolloff factor, which can increase
      *    or decrease the usual inverse-distance-square value.
      */
     float      rolloff = 1.0f;             // Valid values are >= 0.0
     static final float  SPEED_OF_SOUND  = 0.344f;  // in meters/milliseconds

     /*
      * Reverberation
      *
      *   Within Java 3D's model for auralization, the components to
      *   reverberation for a particular space are:
      *     Reflection and Reverb Coefficients -
      *         attenuation of sound (uniform for all frequencies) due to
      *         absorption of reflected sound off materials within the
      *         listening space.
      *     Reflection and Reverb Delay -
      *         approximating time from the start of the direct sound that
      *         initial early and late reflection waves take to reach listener.
      *     Reverb Decay -
      *         approximating time from the start of the direct sound that
      *         reverberation is audible.
      */

     /**
      *   Coefficients for reverberation
      *     The (early) Reflection and Reverberation coefficient scale factors
      *     are used to approximate the reflective/absorptive characteristics
      *     of the surfaces in this bounded Auralizaton environment.
      *     Theses scale factors is applied to sound's amplitude regardless
      *     of sound's position.
      *     Value of 1.0 represents complete (unattenuated) sound reflection.
      *     Value of 0.0 represents full absorption; reverberation is disabled.
      */
     float      reflectionCoefficient = 0.0f; // Range of values 0.0 to 1.0
     float      reverbCoefficient = 1.0f; // Range of values 0.0 to 1.0

     /**
       *  Time Delays in milliseconds
       *    Set with either explicitly with time, or impliciticly by supplying
       *    bounds volume and having the delay time calculated.
       *    Bounds of reverberation space does not have to be the same as
       *    Attribute bounds.
       */
     float      reflectionDelay = 20.0f;    // in milliseconds
     float      reverbDelay = 40.0f;        // in milliseconds
     Bounds     reverbBounds = null;

     /**
      *   Decay parameters
      *     Length and timbre of reverb decay tail
      */
     float      decayTime = 1000.0f;        // in milliseconds
     float      decayFilter = 5000.0f;      // low-pass cutoff frequency

     /**
      *   Reverb Diffusion and Density ratios (0=min, 1=max)
      */
     float      diffusion = 1.0f;
     float      density = 1.0f;

     /**
      *   Reverberation order
      *     This limits the number of Reverberation iterations executed while
      *     sound is being reverberated.  As long as reflection coefficient is
      *     small enough, the reverberated sound decreases (as it would naturally)
      *     each successive iteration.
      *     Value of > zero defines the greatest reflection order to be used by
      *         the reverberator.
      *     All positive values are used as the number of loop iteration.
      *     Value of <= zero signifies that reverberation is to loop until reverb
      *         gain reaches zero (-60dB or 1/1000 of sound amplitude).
      */
     int      reverbOrder = 0;

     /**
      *   Distance Filter
      *   Each sound source is attenuated by a filter based on it's distance
      *   from the listener.
      *   For now the only supported filterType will be LOW_PASS frequency cutoff.
      *   At some time full FIR filtering will be supported.
      */
     static final int  NO_FILTERING  = -1;
     static final int  LOW_PASS      =  1;

     int         filterType = NO_FILTERING;
     float[]     distance = null;
     float[]     frequencyCutoff = null;

     /**
      *   Doppler Effect parameters
      *     Between two snapshots of the head and sound source positions some
      *     delta time apart, the distance between head and source is compared.
      *     If there has been no change in the distance between head and sound
      *     source over this delta time:
      *         f' = f
      *
      *     If there has been a change in the distance between head and sound:
      *         f' = f * Af * v
      *
      *     When head and sound are moving towards each other then
      *                |  (S * Ar)  +  (deltaV(h,t) * Av) |
      *         v  =   | -------------------------------- |
      *                |  (S * Ar)  -  (deltaV(s,t) * Av)  |
      *
      *     When head and sound are moving away from each other then
      *                |  (S * Ar)  -  (deltaV(h,t) * Av) |
      *         v  =   | -------------------------------- |
      *                |  (S * Ar)  +  (deltaV(s,t) * Av) |
      *
      *
      *     Af = AuralAttribute frequency scalefactor
      *     Ar = AuralAttribute rolloff scalefactor
      *     Av = AuralAttribute velocity scalefactor
      *     deltaV = delta velocity
      *     f = frequency of sound
      *     h = Listeners head position
      *     v = Ratio of delta velocities
      *     Vh = Vector from center ear to sound source
      *     S = Speed of sound
      *     s = Sound source position
      *     t = time
      *
      *     If adjusted velocity of head or adjusted velocity of sound is
      *     greater than adjusted speed of sound, f' is undefined.
      */
     /**
      *   Frequency Scale Factor
      *     used to increase or reduce the change of frequency associated
      *     with normal rate of playback.
      *     Value of zero causes sounds to be paused.
      */
     float      frequencyScaleFactor = 1.0f;
     /**
      *   Velocity Scale Factor
      *     Float value applied to the Change of distance between Sound Source
      *     and Listener over some delta time.  Non-zero if listener moving
      *     even if sound is not.  Value of zero implies no Doppler applied.
      */
     float      velocityScaleFactor = 0.0f;

     /**
      * This boolean is set when something changes in the attributes
      */
     boolean         aaDirty = true;

     /**
      * The mirror copy of this AuralAttributes.
      */
     AuralAttributesRetained mirrorAa = null;

    /**
     ** Debug print mechanism for Sound nodes
     **/
    static final // 'static final' so compiler doesn't include debugPrint calls
    boolean  debugFlag = false;

    static final  // 'static final' so internal error message are not compiled
    boolean internalErrors = false;

    void debugPrint(String message) {
        if (debugFlag) // leave test in in case debugFlag made non-static final
            System.err.println(message);
    }


    // ****************************************
    //
    // Set and Get individual attribute values
    //
    // ****************************************

    /**
     * Set Attribute Gain (amplitude)
     * @param gain scale factor applied to amplitude
     */
    void setAttributeGain(float gain) {
        this.attributeGain = gain;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Attribute Gain (amplitude)
     * @return gain amplitude scale factor
     */
    float getAttributeGain() {
        return this.attributeGain;
    }

    /**
     * Set Attribute Gain Rolloff
     * @param rolloff atmospheric gain scale factor (changing speed of sound)
     */
    void setRolloff(float rolloff) {
        this.rolloff = rolloff;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Attribute Gain Rolloff
     * @return rolloff atmospheric gain scale factor (changing speed of sound)
     */
    float getRolloff() {
        return this.rolloff;
    }

    /**
     * Set Reflective Coefficient
     * @param reflectionCoefficient reflection/absorption factor applied to
     * early reflections.
     */
    void setReflectionCoefficient(float reflectionCoefficient) {
        this.reflectionCoefficient = reflectionCoefficient;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Reflective Coefficient
     * @return reflection coeff reflection/absorption factor applied to
     * early reflections.
     */
    float getReflectionCoefficient() {
        return this.reflectionCoefficient;
    }

    /**
     * Set Reflection Delay Time
     * @param reflectionDelay time before the start of early (first order)
     * reflections.
     */
    void setReflectionDelay(float reflectionDelay) {
        this.reflectionDelay = reflectionDelay;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Reflection Delay Time
     * @return reflection delay time
     */
    float getReflectionDelay() {
        return this.reflectionDelay;
    }

    /**
     * Set Reverb Coefficient
     * @param reverbCoefficient reflection/absorption factor applied to
     * late reflections.
     */
    void setReverbCoefficient(float reverbCoefficient) {
        this.reverbCoefficient = reverbCoefficient;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Reverb Coefficient
     * @return reverb coeff reflection/absorption factor applied to late
     * reflections.
     */
    float getReverbCoefficient() {
        return this.reverbCoefficient;
    }

    /**
     * Set Revereration Delay Time
     * @param reverbDelay time between each order of reflection
     */
    void setReverbDelay(float reverbDelay) {
        this.reverbDelay = reverbDelay;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Revereration Delay Time
     * @return reverb delay time between each order of reflection
     */
    float getReverbDelay() {
        return this.reverbDelay;
    }
    /**
     * Set Decay Time
     * @param decayTime length of time reverb takes to decay
     */
    void setDecayTime(float decayTime) {
        this.decayTime = decayTime;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Revereration Decay Time
     * @return reverb delay time
     */
    float getDecayTime() {
        return this.decayTime;
    }

    /**
     * Set Decay Filter
     * @param decayFilter frequency referenced used in low-pass filtering
     */
    void setDecayFilter(float decayFilter) {
        this.decayFilter = decayFilter;
	this.aaDirty = true;
	notifyUsers();
    }

    /**
     * Retrieve Revereration Decay Filter
     * @return reverb delay Filter
     */
    float getDecayFilter() {
        return this.decayFilter;
    }

    /**
     * Set Reverb Diffusion
     * @param diffusion ratio between min and max device diffusion settings
     */
    void setDiffusion(float diffusion) {
        this.diffusion = diffusion;
	this.aaDirty = true;
	notifyUsers();
    }

    /**
     * Retrieve Revereration Decay Diffusion
     * @return reverb diffusion
     */
    float getDiffusion() {
        return this.diffusion;
    }

    /**
     * Set Reverb Density
     * @param density ratio between min and max device density settings
     */
    void setDensity(float density) {
        this.density = density;
	this.aaDirty = true;
	notifyUsers();
    }

    /**
     * Retrieve Revereration Density
     * @return reverb density
     */
    float getDensity() {
        return this.density;
    }


    /**
     * Set Revereration Bounds
     * @param reverbVolume bounds used to approximate reverb time.
     */
    synchronized void setReverbBounds(Bounds reverbVolume) {
        this.reverbBounds = reverbVolume;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Revereration Delay Bounds volume
     * @return reverb bounds volume that defines the Reverberation space and
     * indirectly the delay
     */
    Bounds getReverbBounds() {
        return this.reverbBounds;
    }

    /**
     * Set Reverberation Order of Reflections
     * @param reverbOrder number of times reflections added to reverb signal
     */
    void setReverbOrder(int reverbOrder) {
        this.reverbOrder = reverbOrder;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Reverberation Order of Reflections
     * @return reverb order number of times reflections added to reverb signal
     */
    int getReverbOrder() {
        return this.reverbOrder;
    }

    /**
     * Set Distance Filter (based on distances and frequency cutoff)
     * @param attenuation array of pairs defining distance frequency cutoff
     */
    synchronized void setDistanceFilter(Point2f[] attenuation) {
        if (attenuation == null) {
            this.filterType = NO_FILTERING;
            return;
        }
        int attenuationLength = attenuation.length;
        if (attenuationLength == 0) {
            this.filterType = NO_FILTERING;
            return;
        }
        this.filterType = LOW_PASS;
        // Reallocate every time unless size of new array equal old array
        if ( distance == null ||
            (distance != null && (distance.length != attenuationLength) ) ) {
            this.distance = new float[attenuationLength];
            this.frequencyCutoff = new float[attenuationLength];
        }
        for (int i = 0; i< attenuationLength; i++) {
            this.distance[i] = attenuation[i].x;
            this.frequencyCutoff[i] = attenuation[i].y;
        }
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Set Distance Filter (based on distances and frequency cutoff) using
     * separate arrays
     * @param distance array containing distance values
     * @param filter array containing low-pass frequency cutoff values
     */
    synchronized void setDistanceFilter(float[] distance, float[] filter) {
        if (distance == null || filter == null) {
            this.filterType = NO_FILTERING;
            return;
        }
        int distanceLength = distance.length;
        int filterLength = filter.length;
        if (distanceLength == 0 || filterLength == 0) {
            this.filterType = NO_FILTERING;
            return;
        }
        // Reallocate every time unless size of new array equal old array
        if ( this.distance == null ||
            ( this.distance != null &&
                (this.distance.length != filterLength) ) ) {
            this.distance = new float[distanceLength];
            this.frequencyCutoff = new float[distanceLength];
        }
        this.filterType = LOW_PASS;
        // Copy the distance array into nodes field
        System.arraycopy(distance, 0, this.distance, 0, distanceLength);
        // Copy the filter array an array of same length as the distance array
        if (distanceLength <= filterLength) {
            System.arraycopy(filter, 0, this.frequencyCutoff,0, distanceLength);
        }
        else {
            System.arraycopy(filter, 0, this.frequencyCutoff, 0, filterLength);
	    // Extend filter array to length of distance array by
	    // replicate last filter values.
            for (int i=filterLength; i< distanceLength; i++) {
                this.frequencyCutoff[i] = filter[filterLength - 1];
            }
        }
        if (debugFlag) {
            debugPrint("AAR setDistanceFilter(D,F)");
            for (int jj=0;jj<distanceLength;jj++) {
                debugPrint(" from distance, freq = " + distance[jj] + ", " +
                         filter[jj]);
                debugPrint(" into distance, freq = " + this.distance[jj] + ", " +
                         this.frequencyCutoff[jj]);
            }
        }
	this.aaDirty = true;
	notifyUsers();
    }

    /**
     * Retrieve Distance Filter array length
     * @return attenuation array length
     */
    int getDistanceFilterLength() {
        if (distance == null)
            return 0;
        else
            return this.distance.length;
    }


    /**
     * Retrieve Distance Filter (distances and frequency cutoff)
     * @return attenaution pairs of distance and frequency cutoff filter
     */
    void getDistanceFilter(Point2f[] attenuation) {
        // Write into existing param array already allocated
        if (attenuation == null)
            return;
        if (this.distance == null || this.frequencyCutoff == null)
            return;
        // The two filter attenuation arrays length should be the same
        // We can assume that distance and filter lengths are the same
        // and are non-zero.
        int distanceLength = this.distance.length;
        // check that attenuation array large enough to contain
        // auralAttribute arrays
        if (distanceLength > attenuation.length)
            distanceLength = attenuation.length;
        for (int i=0; i< distanceLength; i++) {
            attenuation[i].x = this.distance[i];
            if (filterType == NO_FILTERING)
                attenuation[i].y = Sound.NO_FILTER;
            else if (filterType == LOW_PASS)
                attenuation[i].y = this.frequencyCutoff[i];
            if (debugFlag)
                debugPrint("AAR: getDistF: " + attenuation[i].x + ", " +
                  attenuation[i].y);
        }
    }
    /**
     * Retrieve Distance Filter as arrays distances and frequency cutoff array
     * @param distance array of float values
     * @param frequencyCutoff array of float cutoff filter values in Hertz
     */
    void getDistanceFilter(float[] distance, float[] filter) {
        // Write into existing param arrays already allocated
        if (distance == null || filter == null)
            return;
        if (this.distance == null || this.frequencyCutoff == null)
            return;
        int distanceLength = this.distance.length;
        // check that distance parameter large enough to contain auralAttribute
        // distance array
        // We can assume that distance and filter lengths are the same
        // and are non-zero.
        if (distance.length < distanceLength)
            // parameter array not large enough to hold all this.distance data
            distanceLength = distance.length;
        System.arraycopy(this.distance, 0, distance, 0, distanceLength);
        if (debugFlag)
            debugPrint("AAR getDistanceFilter(D,F) " + this.distance[0]);
        int filterLength = this.frequencyCutoff.length;
        if (filter.length < filterLength)
            // parameter array not large enough to hold all this.filter data
            filterLength = filter.length;
        if (filterType == NO_FILTERING) {
            for (int i=0; i< filterLength; i++)
                filter[i] = Sound.NO_FILTER;
        }
        if (filterType == LOW_PASS) {
            System.arraycopy(this.frequencyCutoff, 0, filter, 0, filterLength);
        }
        if (debugFlag)
            debugPrint(", " + this.frequencyCutoff[0]);
    }

    /**
     * Set Frequency Scale Factor
     * @param frequencyScaleFactor factor applied to sound's base frequency
     */
    void setFrequencyScaleFactor(float frequencyScaleFactor) {
        this.frequencyScaleFactor = frequencyScaleFactor;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Frequency Scale Factor
     * @return frequency scale factor applied to sound's base frequency
     */
    float getFrequencyScaleFactor() {
        return this.frequencyScaleFactor;
    }

    /**
     * Set Velocity ScaleFactor used in calculating Doppler Effect
     * @param velocityScaleFactor applied to velocity of sound in relation to listener
     */
    void setVelocityScaleFactor(float velocityScaleFactor) {
        this.velocityScaleFactor = velocityScaleFactor;
	this.aaDirty = true;
	notifyUsers();
    }
    /**
     * Retrieve Velocity ScaleFactor used in calculating Doppler Effect
     * @return velocity scale factor
     */
    float getVelocityScaleFactor() {
        return this.velocityScaleFactor;
    }

    synchronized void reset(AuralAttributesRetained aa) {
	int i;

        this.attributeGain = aa.attributeGain;
        this.rolloff = aa.rolloff;
        this.reflectionCoefficient = aa.reflectionCoefficient;
        this.reverbCoefficient = aa.reverbCoefficient;
        this.reflectionDelay = aa.reflectionDelay;
        this.reverbDelay = aa.reverbDelay;
        this.reverbBounds = aa.reverbBounds;
        this.reverbOrder = aa.reverbOrder;
        this.decayTime = aa.decayTime;
        this.decayFilter = aa.decayFilter;
        this.diffusion = aa.diffusion;
        this.density = aa.density;
        this.frequencyScaleFactor = aa.frequencyScaleFactor;
        this.velocityScaleFactor = aa.velocityScaleFactor;

	if (aa.distance != null) {
            this.distance = new float[aa.distance.length];
            if (debugFlag)
                debugPrint("reset aa; aa.distance.length = " + this.distance.length);
            System.arraycopy(aa.distance, 0, this.distance, 0, this.distance.length);
        }
        else
            if (debugFlag)
                debugPrint("reset aa; aa.distance = null");
	if (aa.frequencyCutoff != null)  {
            this.frequencyCutoff = new float[aa.frequencyCutoff.length];
            if (debugFlag)
                debugPrint("reset aa; aa.frequencyCutoff.length = " + this.frequencyCutoff.length);
            System.arraycopy(aa.frequencyCutoff, 0, this.frequencyCutoff, 0,
                     this.frequencyCutoff.length);
        }
        else
            if (debugFlag)
                debugPrint("reset aa; aa.frequencyCutoff = null");
	// XXXX: (Enhancement) Why are these dirtyFlag cleared rather than aa->this
        this.aaDirty = false;
	aa.aaDirty = false;
    }

    void update(AuralAttributesRetained aa) {
	this.reset(aa);
    }

}
