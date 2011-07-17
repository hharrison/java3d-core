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

import javax.vecmath.*;
import java.awt.*;
import java.util.*;

/**
 * This object contains a specification of the physical environment in
 * which the view will be generated.  It is used to set up input
 * devices (sensors) for head-tracking and other uses, and the audio
 * output device.  Sensors are indexed starting at zero.
 *
 * @see View
 */

public class PhysicalEnvironment extends Object {
    /**
     * The Sensor Index associated with the Head
     */
    int HeadIndex = 0;

    // The Sensor index associated with the Right Hand
    int RightHandIndex = 1;

    // The Sensor index associated with the Left Hand
    int LeftHandIndex = 2;

    // The current Dominant Hand Sensor Index
    int DominantHandIndex = 1;

    // The current Non Dominant Hand Sensor Index
    int NonDominantHandIndex = 2;

    //
    // Coexistence coordinate system to tracker-base coordinate
    // system transform.  If head tracking is enabled, this transform
    // is a calibration constant.  If head tracking is not enabled,
    // this transform is not used.
    // This is used in both SCREEN_VIEW and HMD_VIEW modes.
    //
    Transform3D coexistenceToTrackerBase = new Transform3D();

    //
    // Indicates whether the underlying hardware implementation
    // supports tracking.
    //
    boolean trackingAvailable = false;

    // The view associated with this physical environment
    //    View view;

    //
    // This variable specifies the policy Java 3D will use in placing
    // the user's eye position relative to the user's head position
    // (NOMINAL_SCREEN, NOMINAL_HEAD, or NOMINAL_FEET).
    // It is used in the calibration process.
    //
    // TODO: this needs better explanation in the spec
    int coexistenceCenterInPworldPolicy = View.NOMINAL_SCREEN;

    // Mask that indicates this PhysicalEnv's view dependence info. has changed,
    // and CanvasViewCache may need to recompute the final view matries.
    int peDirtyMask = (View.PE_COE_TO_TRACKER_BASE_DIRTY
		       | View.PE_TRACKING_AVAILABLE_DIRTY
		       | View.PE_COE_CENTER_IN_PWORLD_POLICY_DIRTY);

    
////    /**
////     * The offset in the user's dominant-hand-tracker coordinates
////     * to that hand's hot spot. This value is a calibration constant.
////     */
////    Vector3d dominantHandTrackerHotspotOffset;
////
////    /**
////     * The offset in the user's non-dominant-hand-tracker coordinates
////     * to that hand's hot spot. This value is a calibration constant.
////     */
////    Vector3d nondominantHandTrackerHotspotOffset;

    //
    // The number of sensor stored within the PhysicalEnvironment
    //
    int sensorCount;

    //
    // Array of sensors
    //
    Sensor[] sensors;

    // Audio device associated with this PhysicalEnvironment    
    AudioDevice audioDevice = null;

    boolean sensorListChanged = false;    

    Sensor[] sensorList = null;

    // A list of View Objects that refer to this
    ArrayList users = new ArrayList();

    // Scheduler for input devices
    InputDeviceScheduler inputsched;

    // store all inputDevices
    Vector devices = new Vector(1);

    // Number of active view users
    int activeViewRef = 0;

    // Hashtable that maps a PhysicalEnvironment to its InputDeviceScheduler
    static Hashtable physicalEnvMap = new Hashtable();

    /**
     * Constructs a PhysicalEnvironment object with default parameters.
     * The default values are as follows:
     * <ul>
     * sensor count : 3<br>
     * sensors : null (for all array elements)<br>
     * head index : 0<br>
     * right hand index : 1<br>
     * left hand index : 2<br>
     * dominant hand index : 1<br>
     * nondominant hand index : 2<br>
     * tracking available : false<br>
     * audio device : null<br>
     * input device list : empty<br>
     * coexistence to tracker base transform : identity<br>
     * coexistence center in pworld policy : View.NOMINAL_SCREEN<br>
     * </ul>
     */
    public PhysicalEnvironment() {
	this(3);
    }

    // Add a user to the list of users
    synchronized void removeUser(View view) {
	int idx = users.indexOf(view);
	if (idx >= 0) {
	    users.remove(idx);
	}
    }

    // Add a user to the list of users
    synchronized void addUser(View view) {
	int idx = users.indexOf(view);
	if (idx < 0) {
	    users.add(view);
	}
    }

    // Add a user to the list of users
    synchronized void notifyUsers() {
	for (int i=users.size()-1; i>=0; i--) {
	    View view = (View)users.get(i);
	    view.repaint();
	}
    }

    /**
     * Constructs and initializes a PhysicalEnvironment object with
     * the specified number of sensors.
     * @param sensorCount the number of sensors to create.
     */
    public PhysicalEnvironment(int sensorCount) {
	this.sensorCount = sensorCount;
	sensors = new Sensor[sensorCount];
	for(int i=sensorCount-1; i>=0; i--) {
	    sensors[i] = null;
	}
    }

   

    /**
     * Returns copy of Sensor references.  Returns null for zero
     * sensors, so user of method must check for null.  Also, any of
     * these sensors could be null.
     */
    Sensor[] getSensorList() {
        synchronized(sensors) {
           if(sensorListChanged) {  // note: this is a rare case
               sensorList = new Sensor[sensors.length];
               for(int i=0 ; i<sensors.length ; i++) {
                    sensorList[i] = sensors[i];
               }
               sensorListChanged = false;

           }
           return sensorList;
        }
    }

 
    /** 
     * Sets the specified AudioDevice object as the device through
     * which audio rendering for this PhysicalEnvironment will be
     * performed.
     * @param device audio device object to be associated with this 
     * PhysicalEnvironment
     */
    public void setAudioDevice(AudioDevice device) {
        audioDevice = device;
    }
       
    /**
     * Gets the audioDevice for this PhysicalEnvironment.
     * @return audio device object associated with this PhysicalEnvironment
     */
    public AudioDevice getAudioDevice(){
        return audioDevice;
    }

    /**
     * Create an enumerator that produces all input devices.
     * @return an enumerator of all available devices
     */  
    public Enumeration getAllInputDevices() {
	return devices.elements();
    }

    /**
     * Add an input device to the list of input devices.  User is 
     * responsible for initializing the device and setting the 
     * processing mode (streaming or polling).
     * @param device  the device to be added to the list of input devices
     * @exception IllegalArgumentException if InputDevice.getProcessingMode()
     * does not return one of BLOCKING, NON_BLOCKING, or DEMAND_DRIVEN.
     */
    public void addInputDevice(InputDevice device) {

        int driver_type = device.getProcessingMode();

        if ((driver_type == InputDevice.BLOCKING) || 
            (driver_type == InputDevice.NON_BLOCKING) ||
            (driver_type == InputDevice.DEMAND_DRIVEN)) {
                synchronized (devices) {
                    devices.add(device);
                    if (inputsched != null) {
                        inputsched.addInputDevice(device);
                    }
                }
        } else {
            throw new IllegalArgumentException(J3dI18N.getString("PhysicalEnvironment0"));
        }
    }

    /**
      * Remove an input device from the list of input devices.
      * User is responsible for closing out the device and releasing
      * the device resources.
      * @param device  the device to be removed
      */
    public void removeInputDevice(InputDevice device) {
         devices.remove(device);
         synchronized (devices) {
             if (inputsched != null) {
                 inputsched.removeInputDevice(device);
             }
         }
    }

    /**
     * Sets the index of the head to the specified sensor index.
     * @param index the new sensor index of the head
     */
    public void setHeadIndex(int index) {
	HeadIndex = index;
	synchronized(this) {
	    computeTrackingAvailable();
	    peDirtyMask |= View.PE_TRACKING_AVAILABLE_DIRTY;
	}
	notifyUsers();
    }

    /**
     * Gets the sensor index of the head.
     * @return the sensor index of the head
     */
    public int getHeadIndex() {
	return HeadIndex;
    }

    /**
     * Sets the index of the right hand to the specified sensor index.
     * @param index the new sensor index of the right hand
     */
    public void setRightHandIndex(int index) {
	RightHandIndex = index;
	notifyUsers();
    }

    /**
     * Gets the sensor index of the right hand.
     * @return the sensor index of the right hand
     */
    public int getRightHandIndex() {
	return RightHandIndex;
    }

    /**
     * Sets the index of the left hand to the specified sensor index.
     * @param index the new sensor index of the left hand
     */
    public void setLeftHandIndex(int index) {
	LeftHandIndex = index;
	notifyUsers();
    }

    /**
     * Gets the sensor index of the left hand.
     * @return the sensor index of the left hand
     */
    public int getLeftHandIndex() {
	return LeftHandIndex;
    }

    /**
     * Sets the index of the dominant hand to the specified sensor index.
     * @param index the new sensor index of the dominant hand
     */
    public void setDominantHandIndex(int index) {
	DominantHandIndex = index;
	notifyUsers();
    }

    /**
     * Gets the sensor index of the dominant hand.
     * @return the sensor index of the dominant hand
     */
    public int getDominantHandIndex() {
	return DominantHandIndex;
    }

    /**
     * Sets the index of the non-dominant hand to the specified sensor index.
     * @param index the new sensor index of the non dominant hand
     */
    public void setNonDominantHandIndex(int index) {
	NonDominantHandIndex = index;
	notifyUsers();
    }

    /**
     * Gets the sensor index of the non-dominant hand.
     * @return the sensor index of the non dominant hand
     */
    public int getNonDominantHandIndex() {
	return NonDominantHandIndex;
    }

    /**
     * Set the sensor specified by the index to sensor provided; sensors are
     * indexed starting at 0.  All sensors must be registered via this 
     * method.
     * @param index the sensor's index
     * @param sensor the new sensor
     */
    public void setSensor(int index, Sensor sensor) {
        synchronized(sensors) {
	  sensors[index] = sensor;
          sensorListChanged = true;
        }
	synchronized(this) {
	    computeTrackingAvailable();
	    peDirtyMask |= View.PE_TRACKING_AVAILABLE_DIRTY;
	}

	notifyUsers();
    }

    /**
     * Gets the sensor specified by the index; sensors are indexed starting
     * at 0.
     * @param index the sensor's index
     */
    public Sensor getSensor(int index){
           // not synchronized, since the only way to write to sensors is 
           // via a public API call, and user shouldn't call Sensor with 
           // two threads
	   return sensors[index];
    }

    /**
     * Sets the coexistence coordinate system to tracker-base coordinate
     * system transform.  If head tracking is enabled, this transform
     * is a calibration constant.  If head tracking is not enabled,
     * this transform is not used.
     * This is used in both SCREEN_VIEW and HMD_VIEW modes.
     * @param t the new transform
     * @exception BadTransformException if the transform is not rigid
     */
    public void setCoexistenceToTrackerBase(Transform3D t) {
	if (!t.isRigid()) {
	    throw new BadTransformException(J3dI18N.getString("PhysicalEnvironment1"));
	}
	synchronized(this) {
	    coexistenceToTrackerBase.setWithLock(t);
	    peDirtyMask |= View.PE_COE_TO_TRACKER_BASE_DIRTY;
	}
	
	notifyUsers();
    }

    /**
     * Retrieves the coexistence coordinate system to tracker-base
     * coordinate system transform and copies it into the specified
     * Transform3D object.
     * @param t the object that will receive the transform
     */
    public void getCoexistenceToTrackerBase(Transform3D t) {
	t.set(coexistenceToTrackerBase);
    }

    /**
     * Returns a status flag indicating whether or not tracking
     * is available.
     * @return a flag telling whether tracking is available
     */
    public boolean getTrackingAvailable() {
      return this.trackingAvailable;
    }

    /**
     * Sets the coexistence center in physical world policy.
     * This setting determines how Java 3D places the
     * user's eye point as a function of head position during the
     * calibration process, one of View.NOMINAL_SCREEN,
     * View.NOMINAL_HEAD, or View.NOMINAL_FEET.
     * The default policy is View.NOMINAL_SCREEN.
     * @param policy the new policy
     */
    public void setCoexistenceCenterInPworldPolicy(int policy) {
	switch (policy) {
	case View.NOMINAL_SCREEN:
	case View.NOMINAL_HEAD:
	case View.NOMINAL_FEET:
	    break;
	    
	default:
	    throw new IllegalArgumentException(J3dI18N.getString("PhysicalEnvironment2"));
	}

	synchronized(this) {
	    this.coexistenceCenterInPworldPolicy = policy;
	    peDirtyMask |= View.PE_COE_CENTER_IN_PWORLD_POLICY_DIRTY;
	}
	notifyUsers();
    }
    
    /**
     * Returns the current coexistence center in physical world policy.
     * @return one of: View.NOMINAL_SCREEN, View.NOMINAL_HEAD, or
     * View.NOMINAL_FEET
     */
    public int getCoexistenceCenterInPworldPolicy() {
	return this.coexistenceCenterInPworldPolicy;
    }

    /**
     * Get the current sensor count.
     * @return the number of sensor objects per PhysicalEnvironment object
     */
    public int getSensorCount() {
	return sensorCount;
    }

    /**
     * Set the number of sensor objects per PhysicalEnvironmnet. This is a
     * calibration parameter that should be set before setting any sensors 
     * in the PhysicalEnvironment object.  This call associates 'count'
     * Sensors with this object, and they are indexed from 0 to count-1.
     * @param count the new sensor  count
     */
    public void setSensorCount(int count) {

        Sensor[] tmp = new Sensor[count];
        int i=0;

        synchronized(sensors) {
	    int min =  Math.min(count, sensorCount);
            while(i < min) {
                tmp[i] = sensors[i++];
            }
            while(i < count) {
                tmp[i++] = null;
            }
            sensorCount = count;
            sensorListChanged = true;
            sensors = tmp;
        } 
	notifyUsers();
    }

    // (re-)compute the tracking available flag
    private void computeTrackingAvailable() {
        synchronized(sensors) {
	   trackingAvailable = ((HeadIndex < sensors.length) &&
			     (sensors[HeadIndex] != null));
        }
    }

}
