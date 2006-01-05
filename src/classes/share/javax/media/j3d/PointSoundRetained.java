/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.lang.Math;
import java.net.URL; 
import javax.vecmath.Point3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point2f;

/**
 * The PointSoundRetained node (a sub-class of the SoundRetained node) defines
 * a spatially-located sound source whose waves radiate uniformly in all 
 * directions from a given location in space.
 */

class PointSoundRetained extends SoundRetained {
    /**
     * Origin of Sound source in Listener's space.
     */
    Point3f     position = new Point3f(0.0f, 0.0f, 0.0f);

    /**
     * The transformed position of this sound
     */
    Point3f xformPosition = new Point3f();
    Transform3D trans = new Transform3D();

    // Pairs of distances and gain scale factors that define piecewise linear
    // gain attenuation between each pair.
    float[]  attenuationDistance;
    float[]  attenuationGain;
    
    PointSoundRetained() {
        this.nodeType = NodeRetained.POINTSOUND;
    }

    /**
     * Sets this sound's location from the vector provided.
     * @param position the new location
     */
    void setPosition(Point3f position) {
   	if (staticTransform != null) {
	    staticTransform.transform.transform(position, this.position);
	} else {
	    this.position.set(position);	
	}

        getLastLocalToVworld().transform(position, xformPosition);

        dispatchAttribChange(POSITION_DIRTY_BIT, (new Point3f(this.position)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Sets this sound's position from the three values provided.
     * @param x the new x position
     * @param y the new y position
     * @param z the new z position
     */
    void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
   	if (staticTransform != null) {
	    staticTransform.transform.transform(this.position);
	}

        getLastLocalToVworld().transform(position, xformPosition);

        dispatchAttribChange(POSITION_DIRTY_BIT, (new Point3f(this.position)));
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Retrieves this sound's location and places it in the vector provided.
     * @param position the variable to receive the location vector
     */
    void getPosition(Point3f position) {
        if (staticTransform != null) {
            Transform3D invTransform = staticTransform.getInvTransform();
            invTransform.transform(this.position, position);
        } else {
            position.set(this.position);
        }
    }

    void getXformPosition(Point3f position) {
        position.set(this.xformPosition);
    }

    /**
     * Sets this sound's distance gain attenuation - where gain scale factor
     * is applied to sound based on distance listener is from sound source.
     * @param distance attenuation pairs of (distance,gain-scale-factor)
     */  
    void setDistanceGain(Point2f[] attenuation) { 
        // if attenuation array null set both attenuation components to null
        if (attenuation == null) {
            this.attenuationDistance = null;
            this.attenuationGain = null;
	    // QUESTION: is this needed so that dispatch***() doesn't
	    // fail with null?
            return; 
        }

        int attenuationLength = attenuation.length;
        this.attenuationDistance = new float[attenuationLength];
        this.attenuationGain = new float[attenuationLength];
        for (int i = 0; i < attenuationLength; i++) {
           this.attenuationDistance[i] = attenuation[i].x;
           this.attenuationGain[i] = attenuation[i].y;
        }
        dispatchAttribChange(DISTANCE_GAIN_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**
     * Sets this sound's distance gain given separate arrays.
     * applied to sound based on distance listener is from sound source.
     * @param distance array of monotonically-increasing floats.
     * @param gain array of amplitude scale factors associated with distances.
     */  
    void setDistanceGain(float[] distance, float[] gain) { 
        // if distance or gain arrays are null then treat both as null 
        if (distance == null) {
            this.attenuationDistance = null; 
            this.attenuationGain = null;   
	    // QUESTION: is this needed so that dispatch***() doesn't
	    // fail with null?
            return;
        }
 
        int gainLength = gain.length;
        int distanceLength = distance.length;
        this.attenuationDistance = new float[distanceLength];
        this.attenuationGain = new float[distanceLength];
        // Copy the distance array into nodes field
        System.arraycopy(distance, 0, this.attenuationDistance, 0, distanceLength);
        // Copy the gain array an array of same length as the distance array
        if (distanceLength <= gainLength) {
            System.arraycopy(gain, 0, this.attenuationGain, 0, distanceLength);
        }
        else {
            System.arraycopy(gain, 0, this.attenuationGain, 0, gainLength);
	    // Extend gain array to length of distance array
	    // replicate last gain values.
            for (int i=gainLength; i< distanceLength; i++) {
                this.attenuationGain[i] = gain[gainLength - 1];
            }
        }
        Point2f [] attenuation = new Point2f[distanceLength];
        for (int i=0; i<distanceLength; i++) {
             attenuation[i] = new Point2f(this.attenuationDistance[i],
                                          this.attenuationGain[i]);
        }
        dispatchAttribChange(DISTANCE_GAIN_DIRTY_BIT, attenuation);
	if (source != null && source.isLive()) {
	    notifySceneGraphChanged(false);
	}
    }

    /**     
     * Gets this sound's distance attenuation array length 
     * @return distance gain attenuation array length
     */  
    int getDistanceGainLength() { 
        if (attenuationDistance == null) 
            return 0;
        else
            return this.attenuationDistance.length;
    } 
 
    /**
     * Retieves sound's distance attenuation
     * Put the contents of the two separate distance and gain arrays into
     * an array of Point2f.
     * @param attenuation containing distance attenuation pairs
     */ 
    void getDistanceGain(Point2f[] attenuation) {
        // write into arrays passed in, don't do a new
        if (attenuation == null)
            return;
        if (this.attenuationDistance == null ||
            this.attenuationGain == null)
            return;
        int attenuationLength = attenuation.length;
        // attenuationDistance and Gain array lengths should be the same
        int distanceLength = this.attenuationDistance.length;
        if (distanceLength > attenuationLength)
            distanceLength = attenuationLength;
        for (int i=0; i< distanceLength; i++) {
            attenuation[i].x = attenuationDistance[i];
            attenuation[i].y = attenuationGain[i];
        }
    }

    /**
     * Retieves this sound's attenuation distance and gain arrays, returned in 
     * separate arrays.
     * @param distance array of monotonically-increasing floats.
     * @param gain array of amplitude scale factors associated with distances.
     */  
    void getDistanceGain(float[] distance, float[] gain) { 
        // write into arrays passed in, don't do a new
        if (distance == null || gain == null)
            return;
        if (this.attenuationDistance == null || this.attenuationGain == null)
            return;
        // These two array length should be the same
        int attenuationLength = this.attenuationDistance.length;
        int distanceLength = distance.length;
        if (distanceLength > attenuationLength)
            distanceLength = attenuationLength;  
        System.arraycopy(this.attenuationDistance, 0, distance, 0, distanceLength);
        attenuationLength = this.attenuationDistance.length;
        int gainLength = gain.length;
        if (gainLength > attenuationLength)
            gainLength = attenuationLength;  
        System.arraycopy(this.attenuationGain, 0, gain, 0, gainLength);
    }

    /**  
     * This updates the positional fields of point sound.
     *
     * Distance gain attenuation field not maintained in mirror object.
     */ 
    void updateMirrorObject(Object[] objs) {
        if (debugFlag)
            debugPrint("PointSoundRetained:updateMirrorObj()");
        int component = ((Integer)objs[1]).intValue();
        int numSnds = ((Integer)objs[2]).intValue();
        SoundRetained[] mSnds = (SoundRetained[]) objs[3];
        if (component == -1) {
            // update every field
            initMirrorObject(((PointSoundRetained)objs[2]));
            return;
        }
        if ((component & POSITION_DIRTY_BIT) != 0) {
            for (int i = 0; i < numSnds; i++) {
                PointSoundRetained point = (PointSoundRetained)mSnds[i];
                point.position = (Point3f)objs[4];
                point.getLastLocalToVworld().transform(point.position,
                                                      point.xformPosition);
            }
        }

        // call the parent's mirror object update routine
        super.updateMirrorObject(objs);
    }

    synchronized void initMirrorObject(PointSoundRetained ms) {
        super.initMirrorObject(ms);
        ms.position.set(this.position);
        ms.xformPosition.set(this.xformPosition);
    }

    // Called on the mirror object
    void updateTransformChange() {
        super.updateTransformChange();
        getLastLocalToVworld().transform(position, xformPosition);
        // set flag looked at by Scheduler to denote Transform change 
        // this flag will force resneding transformed position to AudioDevice
        if (debugFlag)
            debugPrint("PointSoundRetained xformPosition is (" + xformPosition.x +
                       ", " + xformPosition.y + ", "+ xformPosition.z + ")");
    }

    void mergeTransform(TransformGroupRetained xform) {
	super.mergeTransform(xform);
	xform.transform.transform(position, position);
    }
}
