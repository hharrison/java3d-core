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
 * Class specifying a wakeup on first detection of sensors no 
 * longer intersecting the specified boundary.
 */
public final class WakeupOnSensorExit extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;
    static final int SENSOREXIT_IN_BS_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    Bounds region;
    // Transformed region used by BehaviorStructure
    Bounds transformedRegion;

    Sensor armingSensor;

    /**
     * Constructs a new WakeupOnExit criterion.
     * @param region the region that will trigger a wakeup if a Sensor
     *        intersects.
     */
    public WakeupOnSensorExit(Bounds region) {
	this.region = (Bounds)region.clone();
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }
    
    /**
     * Returns this object's bounds specification
     * @return the bounds used in constructing this WakeupCriterion.
     */
    public Bounds getBounds() {
	return (Bounds)region.clone();
    }

    /**
     * Update the cached Transfrom Region, call from BehaviorStructure
     */
    void updateTransformRegion() {
	if (transformedRegion != null) {
	    transformedRegion.set(region);
	} else {
	    // region is read only once initialize (since there is no
	    // set method for region). So no need to use cloneWithLock()
	    transformedRegion = (Bounds) region.clone();
	}
	transformedRegion.transform(behav.getCurrentLocalToVworld(null));
    }

    /**
     * This is a callback from BehaviorStructure. It is 
     * used to add wakeupCondition to behavior structure.
     */
    void addBehaviorCondition(BehaviorStructure bs) {
	bs.addSensorExitCondition(this);
	if ((behav != null) && behav.enable) {
	    bs.activeWakeupOnSensorCount++;
	}
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.removeSensorExitCondition(this);
	if ((behav != null) && behav.enable) {
	    bs.activeWakeupOnSensorCount--;
	}
    }


    /**
     * Set the sensor that trigger this behavior
     */
    void setTarget(Sensor sensor) {
	this.armingSensor = sensor;
    }

    /**
     * Retrieves the Sensor object that caused the wakeup.
     *
     * @return the triggering Sensor object
     *
     * @exception IllegalStateException if not called from within 
     * a behavior's processStimulus method which was awoken by a sensor
     * exit.
     *
     * @since Java 3D 1.2
     */
    public Sensor getTriggeringSensor() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnSensorExit0"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new
		    IllegalStateException(J3dI18N.getString("WakeupOnSensorExit0"));
	    }
	}
	return armingSensor;
    }


    /**
     * Perform task in addBehaviorCondition() that has to be
     * set every time the condition met.
     */
    void resetBehaviorCondition(BehaviorStructure bs) {}
}
