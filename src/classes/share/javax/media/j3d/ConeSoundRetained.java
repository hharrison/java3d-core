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

import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;



/**
 * A ConeSoundRetained node defines a point sound source located at some
 * location 
 * in space whose amplitude is constrained not only by maximum and minimum
 * amplitude
 * spheres but by two concentric cone volumes directed down an vector radiating
 * from the sound's location.
 */ 

class ConeSoundRetained extends PointSoundRetained {
    /**
     * The Cone Sound's direction vector.  This is the cone axis.
     */
    Vector3f	direction = new Vector3f(0.0f, 0.0f, 1.0f);

    // The transformed direction of this sound
    Vector3f xformDirection = new Vector3f(0.0f, 0.0f, 1.0f);

    // Sound's gain is attenuated for listener locations off-angle from
    // the source source direction.
    // This can be set of three numbers:
    //     angular distance in radians
    //     gain scale factor
    //     filtering (currently the only filtering supported is lowpass)

    // For now the only supported filterType will be LOW_PASS frequency cutoff.
    // At some time full FIR filtering will be supported.
    static final int  NO_FILTERING  = -1;
    static final int  LOW_PASS      =  1;

    // Pairs of distances and gain scale factors that define piecewise linear
    // gain BACK attenuation between each pair.
    // These are used for defining elliptical attenuation regions.
    float[]     backAttenuationDistance = null;
    float[]     backAttenuationGain = null;

    float[]	angularDistance = {0.0f, ((float)(Math.PI) * 0.5f)};
    float[]	angularGain     = {1.0f, 0.0f};
    int         filterType      = NO_FILTERING;
    float[]     frequencyCutoff = {Sound.NO_FILTER, Sound.NO_FILTER};

    ConeSoundRetained() {
        this.nodeType = NodeRetained.CONESOUND;
    }

    // *********************
    //
    // Distance Gain methods
    //
    // *********************

    /**
     * Sets this sound's distance gain elliptical attenuation - 
     * where gain scale factor is applied to sound based on distance listener
     * is from sound source.
     * @param frontAttenuation defined by pairs of (distance,gain-scale-factor)
     * @param backAttenuation defined by pairs of (distance,gain-scale-factor)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    void setDistanceGain(Point2f[] frontAttenuation,
                                      Point2f[] backAttenuation ) {

        this.setDistanceGain(frontAttenuation);
        this.setBackDistanceGain(backAttenuation);
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
    void setDistanceGain(float[] frontDistance, float[] frontGain,
                                      float[] backDistance, float[] backGain) {
        this.setDistanceGain(frontDistance, frontGain);
        this.setBackDistanceGain(backDistance, backGain);
    }

    /**
     * Sets this sound's back distance gain attenuation - where gain scale 
     * factor is applied to sound based on distance listener along the negative
     * sound direction axis from sound source.
     * @param attenuation defined by pairs of (distance,gain-scale-factor)
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    void setBackDistanceGain(Point2f[] attenuation)
    {
        // if attenuation array null set both attenuation components to null
        if (attenuation == null) {
            this.backAttenuationDistance = null;
            this.backAttenuationGain = null;
        }
        else {
            int attenuationLength = attenuation.length;
            if (attenuationLength == 0) {
                this.backAttenuationDistance = null;
                this.backAttenuationGain = null;
            }
            else {
                this.backAttenuationDistance = new float[attenuationLength];
                this.backAttenuationGain = new float[attenuationLength];
                for (int i = 0; i < attenuationLength; i++) {
                    this.backAttenuationDistance[i] = attenuation[i].x;
                    this.backAttenuationGain[i] = attenuation[i].y;
                }
            }
        }
        dispatchAttribChange(BACK_DISTANCE_GAIN_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Sets this sound's back distance gain attenuation as an array of Point2fs.
     * @param distance array of monotonically-increasing floats
     * @param gain array of non-negative scale factors
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    void setBackDistanceGain(float[] distance, float[] gain)
    {
        int distanceLength = 0;
        // if distance or gain arrays are null then treat both as null
        if (distance == null || gain == null) {
            this.backAttenuationDistance = null;
            this.backAttenuationGain = null;
        }
        else {
            // now process the back attenuation values
            int gainLength = gain.length;
            distanceLength = distance.length;
            if (distanceLength == 0 || gainLength == 0) {
                this.backAttenuationDistance = null;
                this.backAttenuationGain = null;
            }
            else {
                this.backAttenuationDistance = new float[distanceLength];
                this.backAttenuationGain = new float[distanceLength];
                // Copy the distance array into nodes field
                System.arraycopy(distance, 0, this.backAttenuationDistance,
                                           0, distanceLength);      
                // Copy the gain array an array of same length as the distance array
                if (distanceLength <= gainLength) {
                    System.arraycopy(gain, 0, this.backAttenuationGain, 
                                           0, distanceLength);
                }
                else {
                    System.arraycopy(gain, 0, this.backAttenuationGain, 0, gainLength);
		    // Extend gain array to length of distance array
		    // replicate last gain values.
                    for (int i=gainLength; i< distanceLength; i++) {
                        this.backAttenuationGain[i] = gain[gainLength - 1];
                    }
                }
            }
        }
 
        Point2f [] attenuation = new Point2f[distanceLength]; 
        for (int i=0; i<distanceLength; i++) { 
             attenuation[i] = new Point2f(this.backAttenuationDistance[i],
                                          this.backAttenuationGain[i]);
        } 
        dispatchAttribChange(BACK_DISTANCE_GAIN_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Gets this sound's elliptical distance attenuation
     * @param frontAttenuation arrays containing forward distances attenuation pairs
     * @param backAttenuation arrays containing backward distances attenuation pairs
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */ 
    void getDistanceGain(Point2f[] frontAttenuation,
                    Point2f[] backAttenuation) { 
        this.getDistanceGain(frontAttenuation);
        this.getBackDistanceGain(backAttenuation);
    }

    /**
     * Gets this sound's elliptical distance gain attenuation values in separate arrays
     * @param frontDistance array of float distances along the sound axis
     * @param fronGain array of non-negative scale factors associated with front distances
     * @param backDistance array of float negative distances along the sound axis
     * @param backGain array of non-negative scale factors associated with back distances
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */ 
    void getDistanceGain(float[] frontDistance, float[] frontGain,
                                      float[] backDistance, float[] backGain) { 
        this.getDistanceGain( frontDistance, frontGain);
        this.getBackDistanceGain( backDistance, backGain);
    }

    /**  
     * Retieves sound's back distance attenuation
     * Put the contents of the two separate distance and gain arrays into
     * an array of Point2f.
     * @param attenuation containing distance attenuation pairs
     */  
    void getBackDistanceGain(Point2f[] attenuation) {
        // Write into arrays passed in, don't do a new
        if (attenuation == null)
            return;
        if (this.backAttenuationDistance == null ||
            this.backAttenuationGain == null)
            return;
        // These two array length should be the same
        // can assume lengths are non-zero
        int distanceLength = this.backAttenuationDistance.length;
        int attenuationLength = attenuation.length;
        if (distanceLength < attenuationLength)
            distanceLength = attenuationLength;
        for (int i=0; i< distanceLength; i++) {
            attenuation[i].x = this.backAttenuationDistance[i];
            attenuation[i].y = this.backAttenuationGain[i];
        }
    }

    /**  
     * Retieves this sound's back attenuation distance and gain arrays, 
     * returned in separate arrays.
     * @param distance array of monotonically-increasing floats.
     * @param gain array of amplitude scale factors associated with distances.
     */  
    void getBackDistanceGain(float[] distance, float[] gain) {
        // write into arrays passed in, don't do a new
        if (distance == null || gain == null)
            return;
        if (this.backAttenuationDistance == null || 
            this.backAttenuationGain == null)
            return;
        // backAttenuationDistance and backAttenuationGain array length should
        // be the same
        // can assume length is non-zero
        int attenuationLength = this.backAttenuationDistance.length;
        int distanceLength = distance.length;
        if (attenuationLength > distanceLength)
            attenuationLength = distanceLength;
        System.arraycopy(this.backAttenuationDistance, 0, distance, 0, attenuationLength);
        attenuationLength = this.backAttenuationGain.length;
        int gainLength = gain.length;
        if (attenuationLength > gainLength)
            attenuationLength = gainLength;
        System.arraycopy(this.backAttenuationGain, 0, gain, 0, attenuationLength);
    }


    // *********************
    //
    // Direction Methods
    //
    // *********************

    /**
     * Sets this sound's direction from the vector provided.
     * @param direction the new direction
     */
    void setDirection(Vector3f direction) {
	if (staticTransform != null) {
	    staticTransform.transform.transform(direction, this.direction);
	} else {
	    this.direction.set(direction);	
	}
        dispatchAttribChange(DIRECTION_DIRTY_BIT, 
				(new Vector3f(this.direction)));

	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Sets this sound's direction from the three values provided.
     * @param x the new x direction
     * @param y the new y direction
     * @param z the new z direction
     */
    void setDirection(float x, float y, float z) {
	direction.x = x;
	direction.y = y;
	direction.z = z;
	if (staticTransform != null) {
	    staticTransform.transform.transform(direction);
	} 
        dispatchAttribChange(DIRECTION_DIRTY_BIT, (new Vector3f(direction)));

	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }


    /**
     * Retrieves this sound's direction and places it in the
     * vector provided.
     * @return direction vector (axis of cones)
     */
    void getDirection(Vector3f direction)
    {
        if (staticTransform != null) {
            Transform3D invTransform = staticTransform.getInvTransform();
            invTransform.transform(this.direction, direction);
        } else {
            direction.set(this.direction);
        }
    }

    void getXformDirection(Vector3f direction)
    {
        direction.set(this.xformDirection);
    }


    // ***************************
    //
    // Angular Attenuation
    //
    // ***************************

    /**  
     * Sets this sound's angular gain attenuation (not including filter)
     * @param attenuation array containing angular distance and gain
     */  
    void setAngularAttenuation(Point2f[] attenuation) {
        int attenuationLength = 0;
        this.filterType = NO_FILTERING;
        if (attenuation == null) {
            this.angularDistance = null;
            this.angularGain = null;
        }
        else {
            attenuationLength = attenuation.length;
            if (attenuationLength == 0) {
                this.angularDistance = null;
                this.angularGain = null;
            }
            else {
                this.angularDistance = new float[attenuationLength];
                this.angularGain = new float[attenuationLength];
                for (int i = 0; i < attenuationLength; i++) {
                   this.angularDistance[i] = attenuation[i].x;
                   this.angularGain[i] = attenuation[i].y;
                }
            } // lengths not zero
        } // arrays not null
        Point3f [] attenuation3f = new Point3f[attenuationLength]; 
        for (int i=0; i<attenuationLength; i++) { 
             attenuation3f[i] = new Point3f(this.angularDistance[i],
                                            this.angularGain[i], 
                                            Sound.NO_FILTER); 
        }
        dispatchAttribChange(ANGULAR_ATTENUATION_DIRTY_BIT, attenuation3f);

	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
     }

    /**  
     * Sets this sound's angular attenuation including both gain and filter.
     * @param attenuation array containing angular distance, gain and filter
     */  
    void setAngularAttenuation(Point3f[] attenuation) {
        if (attenuation == null) {
            this.angularDistance = null;
            this.angularGain = null;
            this.frequencyCutoff = null;
            this.filterType = NO_FILTERING;
        }
        else {
            int attenuationLength = attenuation.length;
            if (attenuationLength == 0) {
                this.angularDistance = null;
                this.angularGain = null;
                this.frequencyCutoff = null;
                this.filterType = NO_FILTERING;
            }
            else {
                this.angularDistance = new float[attenuationLength];
                this.angularGain = new float[attenuationLength];
                this.frequencyCutoff = new float[attenuationLength];
                this.filterType = LOW_PASS;
                for (int i = 0; i < attenuationLength; i++) {
                   this.angularDistance[i] = attenuation[i].x;
                   this.angularGain[i] = attenuation[i].y;
                   this.frequencyCutoff[i] = attenuation[i].z;
                }
            } // lengths not zero
        } // arrays not null
        dispatchAttribChange(ANGULAR_ATTENUATION_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Sets angular attenuation including gain and filter using separate arrays
     * @param distance array containing angular distance
     * @param filter array containing angular low-pass frequency cutoff values
     */  
    void setAngularAttenuation(float[] distance, float[] gain, float[] filter) {
        int distanceLength = 0;
        if (distance == null || gain == null || filter == null) {
            this.angularDistance = null;
            this.angularGain = null;
            this.frequencyCutoff = null;
            this.filterType = NO_FILTERING;
        }
        else {
            distanceLength = distance.length;
            int gainLength = gain.length;
            if (distanceLength == 0 || gainLength == 0) {
                this.angularDistance = null;
                this.angularGain = null;
                this.frequencyCutoff = null;
                this.filterType = NO_FILTERING;
            }
            else {
                int filterLength = filter.length;
                this.angularDistance = new float[distanceLength];
                this.angularGain = new float[distanceLength];
                this.frequencyCutoff = new float[distanceLength];
                // Copy the distance array into nodes field
                System.arraycopy(distance, 0, this.angularDistance, 0, distanceLength);
                // Copy the gain array an array of same length as the distance array
                if (distanceLength <= gainLength) {
                    System.arraycopy(gain, 0, this.angularGain, 0, distanceLength);
                }
                else {
                    System.arraycopy(gain, 0, this.angularGain, 0, gainLength);
                    /**
                     * Extend gain array to length of distance array by
                     * replicate last gain values.
                     */  
                    for (int i=gainLength; i< distanceLength; i++) {
                        this.angularGain[i] = gain[gainLength - 1];
                    }
                }
                // Copy the filter array an array of same length as the distance array
                if (filterLength == 0)
                    this.filterType = NO_FILTERING;
                else {
                    this.filterType = LOW_PASS;
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
                }
            }  // length not zero
        } // arrays not null
        Point3f [] attenuation = new Point3f[distanceLength];
        for (int i=0; i<distanceLength; i++) {
             if (this.filterType != NO_FILTERING) {
                 attenuation[i] = new Point3f(this.angularDistance[i],
                                          this.angularGain[i],
                                          this.frequencyCutoff[i]);
             }
             else {
                 attenuation[i] = new Point3f(this.angularDistance[i],
                                          this.angularGain[i],
                                          Sound.NO_FILTER);
             }
        }
        dispatchAttribChange(ANGULAR_ATTENUATION_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves angular attenuation array length.
     * All arrays are forced to same size
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    int getAngularAttenuationLength() {

       if (angularDistance == null)
           return 0;
       else 
           return (this.angularDistance.length);
    }
 
    /**
     * Retrieves angular attenuation including gain and filter in a single array
     * @param attenuation applied to gain when listener is between cones
     */  
    void getAngularAttenuation(Point3f[] attenuation) {
        /// use attenuation array allocated by user - don't new it
        // The three angular attenuation arrays length should be the same
        if (this.angularDistance == null || this.angularGain == null)
            return;
        if (attenuation == null)
            return;
        int distanceLength = this.angularDistance.length;
        if (attenuation.length < distanceLength)
            distanceLength = attenuation.length;
        for (int i=0; i< distanceLength; i++) {
            attenuation[i].x = this.angularDistance[i];
            attenuation[i].y = this.angularGain[i];
            if (filterType == NO_FILTERING || this.frequencyCutoff == null)
                attenuation[i].z = Sound.NO_FILTER;
            else if (filterType == LOW_PASS)
                attenuation[i].z = this.frequencyCutoff[i];
        }
    }

    /**
     * Retrieves angular attenuation including gain and filter 
     * returned as separate arrays
     * @param distance array containing angular distance
     * @param gain array containing angular gain attenuation
     * @param filter array containing angular low-pass frequency cutoff values
     */  
    void getAngularAttenuation(float[] distance, float[] gain, float[] filter) {
        // use attenuation array allocated by user - don't new it
        if (distance == null || gain == null || filter == null)
            return;
        if (this.angularDistance == null || this.angularGain == null)
            return;
        int distanceLength = this.angularDistance.length;
        if (distance.length < distanceLength)
            distanceLength = distance.length;
        System.arraycopy(this.angularDistance, 0, distance, 0, distanceLength);

        int gainLength = this.angularGain.length;
        if (gain.length < gainLength)
            gainLength = gain.length;
        System.arraycopy(this.angularGain, 0, gain, 0, gainLength);

        int filterLength = 0;
        if (this.frequencyCutoff == null || filterType == NO_FILTERING)
            filterLength = filter.length;
        else {
            filterLength = this.frequencyCutoff.length;
            if (filter.length < filterLength)
                filterLength = filter.length;
        }
        if (filterType == NO_FILTERING || this.frequencyCutoff == null) {
            for (int i=0; i< filterLength; i++) 
                filter[i] = Sound.NO_FILTER;
        }
        if (filterType == LOW_PASS) {
            System.arraycopy(this.frequencyCutoff, 0, filter,0, filterLength);
        }
    }


    /**  
     * This updates the Direction fields of cone sound.
     *
     * Neither Angular gain Attenuation and Filtering fields, nor
     * back distance gain not maintained in mirror object
     */  
    void updateMirrorObject(Object[] objs) {
        if (debugFlag)
            debugPrint("PointSoundRetained:updateMirrorObj()");
        Transform3D trans = null;
        int component = ((Integer)objs[1]).intValue();
        int numSnds = ((Integer)objs[2]).intValue(); 
        SoundRetained[] mSnds = (SoundRetained[]) objs[3];
        if (component == -1) {
            // update every field
            initMirrorObject(((ConeSoundRetained)objs[2]));
            return;
        }
        if ((component & DIRECTION_DIRTY_BIT) != 0) {
            for (int i = 0; i < numSnds; i++) {    
                ConeSoundRetained cone = (ConeSoundRetained)mSnds[i];
                cone.direction = (Vector3f)objs[4];
                cone.getLastLocalToVworld().transform(cone.direction,
                                                      cone.xformDirection);
                    cone.xformDirection.normalize();
            }
        }
        // call the parent's mirror object update routine
        super.updateMirrorObject(objs);
    }

    synchronized void initMirrorObject(ConeSoundRetained ms) {
        super.initMirrorObject(ms);
        ms.direction.set(this.direction);
        ms.xformDirection.set(this.xformDirection);
    }

    // Called on the mirror object
    void updateTransformChange() {
	Transform3D lastLocalToVworld = getLastLocalToVworld();

        super.updateTransformChange();
        lastLocalToVworld.transform(direction, xformDirection);
        xformDirection.normalize();
        // set flag looked at by Scheduler to denote Transform change  
        // this flag will force resneding transformed direction to AudioDevice 
        if (debugFlag) 
            debugPrint("ConeSound xformDirection is (" + xformDirection.x + ", "
                    + xformDirection.y + ", "+ xformDirection.z + ")"); 
    } 

    void mergeTransform(TransformGroupRetained xform) {
        super.mergeTransform(xform);
	xform.transform.transform(direction);
    }
}
