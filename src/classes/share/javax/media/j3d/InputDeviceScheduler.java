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

import java.util.*;

/**
 * This thread manages all input device scheduling.  It monitors and caches
 * all device additions and removals.  It spawns new threads for blocking 
 * devices, manages all non-blocking drivers itself, and tags the sensors 
 * of demand_driven devices. This implementation assume that
 * processMode of InputDevice will not change after addInputDevice().
 *
 */

class InputDeviceScheduler extends J3dThread {

    // list of devices that have been added with the phys env interface
    ArrayList nonBlockingDevices = new ArrayList(1);

    // This condition holds blockingDevices.size() == threads.size()
    ArrayList blockingDevices = new ArrayList(1);
    ArrayList threads = new ArrayList(1);

    // This is used by MasterControl to keep track activeViewRef 
    PhysicalEnvironment physicalEnv;

    // store all inputDevices
    Vector devices = new Vector(1);

    J3dThreadData threadData = new J3dThreadData();
    boolean active = false;

    // The time to sleep before next processAndProcess() is invoked
    // for non-blocking input device
    static int samplingTime = 5;

    // Some variables used to name threads correctly
    private static int numInstances = 0;
    private int instanceNum = -1;

    private synchronized int newInstanceNum() {
	return (++numInstances);
    }

    int getInstanceNum() {
	if (instanceNum == -1)
	    instanceNum = newInstanceNum();
	return instanceNum;
    }

    InputDeviceScheduler(ThreadGroup threadGroup, 
			 PhysicalEnvironment physicalEnv) {
	super(threadGroup);
	setName("J3D-InputDeviceScheduler-" + getInstanceNum());
	threadData.threadType = J3dThread.INPUT_DEVICE_SCHEDULER;
	threadData.thread = this;
	this.physicalEnv = physicalEnv;

	synchronized (physicalEnv.devices) {
	    Enumeration elm = physicalEnv.devices.elements();
	    while (elm.hasMoreElements()) {
		addInputDevice((InputDevice) elm.nextElement());
	    }
	    physicalEnv.inputsched = this;
	}

    }


    void addInputDevice(InputDevice device) {

	switch(device.getProcessingMode()) {
	    case InputDevice.BLOCKING:
		InputDeviceBlockingThread thread = 
		    VirtualUniverse.mc.getInputDeviceBlockingThread(device);
		thread.start();
		synchronized (blockingDevices) {
		    threads.add(thread); 
		    blockingDevices.add(device);
		}
		break;
	    case InputDevice.NON_BLOCKING:
		synchronized (nonBlockingDevices) {
		    nonBlockingDevices.add(device);
		    if (active && (nonBlockingDevices.size() == 1)) {
			VirtualUniverse.mc.addInputDeviceScheduler(this);
		    }
		}
		break;
	    default: //  InputDevice.DEMAND_DRIVEN:
		// tag the sensors
		for (int i=device.getSensorCount()-1; i>=0; i--) {
		    device.getSensor(i).demand_driven = true; 
		}
		break;
	}

    }


    void removeInputDevice(InputDevice device) {

	switch(device.getProcessingMode()) {
	    case InputDevice.BLOCKING:
		// tell the thread to clean up and permanently block
		synchronized (blockingDevices) {
		    int idx = blockingDevices.indexOf(device);
		    InputDeviceBlockingThread thread = 
			(InputDeviceBlockingThread) threads.remove(idx);
		    thread.finish();
		    blockingDevices.remove(idx);
		}
		break;
	    case InputDevice.NON_BLOCKING:
	        // remove references that are in this thread
	        synchronized (nonBlockingDevices) {
		    nonBlockingDevices.remove(nonBlockingDevices.indexOf(device));
		    if (active && (nonBlockingDevices.size() == 0)) {
			VirtualUniverse.mc.removeInputDeviceScheduler(this);
		    }
	        }
	        break;
	    default: //  InputDevice.DEMAND_DRIVEN: 
	        // untag the sensors
	        for (int i=device.getSensorCount()-1; i>=0; i--) {
		    device.getSensor(i).demand_driven = false; 
	        }
	}
    }

    // Add this thread to MC (Callback from MC thread)
    void activate() {
	if (!active) {
	    active = true;

	    synchronized (nonBlockingDevices) {
		if (nonBlockingDevices.size() > 0) {
		    VirtualUniverse.mc.addInputDeviceScheduler(this);
		}
	    }
	    // run all spawn threads
	    synchronized (blockingDevices) {
		for (int i=threads.size()-1; i >=0; i--) {
		    ((InputDeviceBlockingThread)threads.get(i)).restart();
		}
	    }
	}
    }

    // Remove this thread from MC (Callback from MC thread)
    void deactivate() {
	if (active) {
	    synchronized (nonBlockingDevices) {
		if (nonBlockingDevices.size() > 0) {
		    VirtualUniverse.mc.removeInputDeviceScheduler(this);
		}
	    }
	    
	    // stop all spawn threads
	    synchronized (blockingDevices) {
		for (int i=threads.size()-1; i >=0; i--) {
		    ((InputDeviceBlockingThread)threads.get(i)).sleep();
		}
	    }
	    active = false;
	}
    }

    J3dThreadData getThreadData() {
	return threadData;
    }

    void doWork(long referenceTime) {
	synchronized (nonBlockingDevices) {
	    for (int i = nonBlockingDevices.size()-1; i >=0; i--) {
              ((InputDevice)nonBlockingDevices.get(i)).pollAndProcessInput();
	    }
	}
    }

    void shutdown() {
	// stop all spawn threads
	for (int i=threads.size()-1; i >=0; i--) {
	    ((InputDeviceBlockingThread)threads.get(i)).finish();
	}
	// for gc
	threads.clear();
	blockingDevices.clear();
	nonBlockingDevices.clear();
	devices.clear();
    }

}
