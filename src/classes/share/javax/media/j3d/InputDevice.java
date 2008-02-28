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
 * InputDevice is the interface through which Java 3D and Java 3D
 * application programs communicate with a device driver.  All input
 * devices that Java 3D uses must implement the InputDevice interface and
 * be registered with Java 3D via a call to
 * PhysicalEnvironment.addInputDevice(InputDevice).  An input device
 * transfers information to the Java 3D implementation and Java 3D
 * applications by writing transform information to sensors that the
 * device driver has created and manages.  The driver can update its
 * sensor information each time the pollAndProcessInput method is
 * called.
 */

public interface InputDevice {

    /**
     * Signifies that the driver for a device is a blocking driver and that
     * it should be scheduled for regular reads by Java 3D.  A blocking driver
     * is defined as a driver that can cause the thread accessing the driver
     * (the Java 3D implementation thread calling the pollAndProcessInput 
     * method) to block while the data is being accessed from the driver.      
     */ 
    public static final int BLOCKING = 3;


   /**  
    * Signifies that the driver for a device is a non-blocking driver and
    * that it should be scheduled for regular reads by Java 3D.  A
    * non-blocking driver is defined as a driver that does not cause the
    * calling thread to block while data is being retrieved from the
    * driver.  If no data is available from the device, pollAndProcessInput
    * should return without updating the sensor read value.
    */ 
    public static final int NON_BLOCKING = 4;

   /**
    * Signifies that the Java 3D implementation should not schedule
    * regular reads on the sensors of this device; the Java 3D
    * implementation will only call pollAndProcessInput when one of the
    * device's sensors' getRead methods is called.  A DEMAND_DRIVEN driver
    * must always provide the current value of the sensor on demand whenever
    * pollAndProcessInput is called.  This means that DEMAND_DRIVEN drivers
    * are non-blocking by definition.
    */ 
    public static final int DEMAND_DRIVEN = 5;


    /**
     * This method initializes the device.  A device should be initialized 
     * before it is registered with Java 3D via the 
     * PhysicalEnvironment.addInputDevice(InputDevice) method call.
     * @return return true for succesful initialization, false for failure
     */
    public abstract boolean initialize();

    /**
     * This method sets the device's current position and orientation as the
     * devices nominal position and orientation (establish its reference 
     * frame relative to the "Tracker base" reference frame).
     */
    public abstract void setNominalPositionAndOrientation();


    /**
     * This method causes the device's sensor readings to be updated by the
     * device driver.  For BLOCKING and NON_BLOCKING drivers, this method is 
     * called regularly and the Java 3D implementation can cache the sensor 
     * values.  For DEMAND_DRIVEN drivers this method is called each time one 
     * of the Sensor.getRead methods is called, and is not otherwise called.
     */
    public abstract void pollAndProcessInput();

    /**
     * This method will not be called by the Java 3D implementation and 
     * should be implemented as an empty method.
     */
    public abstract void processStreamInput();

    /**
     * Code to process the clean up of the device and relinquish associated 
     * resources.  This method should be called after the device has been 
     * unregistered from Java 3D via the
     * PhysicalEnvironment.removeInputDevice(InputDevice) method call.
     */
    public abstract void close();
    
    /**
     * This method retrieves the device's processing mode: one of BLOCKING, 
     * NON_BLOCKING, or DEMAND_DRIVEN.  The Java 3D implementation calls
     * this method when PhysicalEnvironment.addInputDevice(InputDevice) is
     * called to register the device with Java 3D.  If this method returns 
     * any value other than BLOCKING, NON_BLOCKING, or DEMAND_DRIVEN,
     * addInputDevice will throw an IllegalArgumentException.
     * @return Returns the devices processing mode, one of BLOCKING, 
     * NON_BLOCKING, or DEMAND_DRIVEN
     */
    public abstract int getProcessingMode();


    /**
     * This method sets the device's processing mode to one of: BLOCKING,
     * NON_BLOCKING, or DEMAND_DRIVEN.  Many drivers will be written to run 
     * in only one mode.  Applications using such drivers should not attempt 
     * to set the processing mode.  This method should throw an 
     * IllegalArgumentException if there is an attempt to set the processing 
     * mode to anything other than the aforementioned three values.
     *
     * <p>
     * NOTE: this method should <i>not</i> be called after the input
     * device has been added to a PhysicalEnvironment.  The
     * processingMode must remain constant while a device is attached
     * to a PhysicalEnvironment.
     *
     * @param mode One of BLOCKING, NON_BLOCKING, or DEMAND_DRIVEN
     */
    public abstract void setProcessingMode(int mode);


    /**
     * This method gets the number of sensors associated with the device.
     * @return the device's sensor count.
     */
    public int getSensorCount();

    /**
     * Gets the specified Sensor associated with the device.  Each InputDevice
     * implementation is responsible for creating and managing its own set of
     * sensors.  The sensor indices begin at zero and end at number of 
     * sensors minus one.  Each sensor should have had 
     * Sensor.setDevice(InputDevice) set properly before addInputDevice
     * is called.
     * @param sensorIndex the sensor to retrieve
     * @return Returns the specified sensor.
     */
    public Sensor getSensor(int sensorIndex);   

}
