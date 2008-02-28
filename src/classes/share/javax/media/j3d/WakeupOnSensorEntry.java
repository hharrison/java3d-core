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

/**
 * Class specifying a wakeup on first sensor intersection with the
 * specified boundary.
 */
public final class WakeupOnSensorEntry extends WakeupCriterion {

    // different types of WakeupIndexedList that use in BehaviorStructure
    static final int COND_IN_BS_LIST = 0;
    static final int SENSORENTRY_IN_BS_LIST = 1;

    // total number of different IndexedUnorderedSet types
    static final int TOTAL_INDEXED_UNORDER_SET_TYPES = 2;

    Bounds region;
    
    // Transformed region used by BehaviorStructure
    Bounds transformedRegion;

    Sensor armingSensor;

    /**
     * Constructs a new WakeupOnEntry criterion.
     * @param region the region that will trigger a wakeup if a Sensor
     *        intersects.
     */
    public WakeupOnSensorEntry(Bounds region) {
	this.region = (Bounds)region.clone();
	WakeupIndexedList.init(this, TOTAL_INDEXED_UNORDER_SET_TYPES);
    }

    /**
     * Returns this object's bounds specification
     * @return the bounds used in constructing this WakeupCriterion.
     */
    public Bounds getBounds() {
	return (Bounds) region.clone();
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
	bs.addSensorEntryCondition(this);
	if ((behav != null) && behav.enable) {
	    bs.activeWakeupOnSensorCount++;
	}
    }


    /**
     * This is a callback from BehaviorStructure. It is 
     * used to remove wakeupCondition from behavior structure.
     */
    void removeBehaviorCondition(BehaviorStructure bs) {
	bs.removeSensorEntryCondition(this);
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
     * entry.
     *
     * @since Java 3D 1.2
     */
    public Sensor getTriggeringSensor() {
	if (behav == null) {
	    throw new IllegalStateException(J3dI18N.getString("WakeupOnSensorEntry0"));
	}

	synchronized (behav) {
	    if (!behav.inCallback) {
		throw new
		    IllegalStateException(J3dI18N.getString("WakeupOnSensorEntry0"));
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
