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

class InputDeviceBlockingThread extends Thread {

    // action flag for runMonitor
    private static final int WAIT   = 0;
    private static final int NOTIFY = 1;
    private static final int STOP   = 2;

    // blocking device that this thread manages
    private InputDevice device;
    private boolean running = true;
    private boolean waiting = false;
    private volatile boolean stop = false;
    private static int numInstances = 0;
    private int instanceNum = -1;

    InputDeviceBlockingThread(ThreadGroup threadGroup, InputDevice device) {
	super(threadGroup, "");
	setName("J3D-InputDeviceBlockingThread-" + getInstanceNum());
	this.device = device;
    }

    private synchronized int newInstanceNum() {
	return (++numInstances);
    }

    private int getInstanceNum() {
	if (instanceNum == -1)
	    instanceNum = newInstanceNum();
	return instanceNum;
    }


    public void run() {
	// Since this thread is blocking, this thread should not be
	// taking an inordinate amount of CPU time.  Note that the
	// yield() call should not be necessary (and may be ineffective),
	// but we can't call MasterControl.threadYield() because it will
	// sleep for at least a millisecond.
	while (running) {	
	    while (!stop) {
		device.pollAndProcessInput();
		Thread.yield();
	    }
	    runMonitor(WAIT);
	}
    }

    void sleep() {
	stop = true;
    }

    void restart() {
	stop = false;
	runMonitor(NOTIFY);
    }

    void finish() {
	stop = true;
	while (!waiting) {
	    MasterControl.threadYield();
	}

	runMonitor(STOP);
    }

    synchronized void runMonitor(int action) {

	switch (action) {
	case WAIT:
	    try {
		waiting = true;
		wait();
	    } catch (InterruptedException e) {}
	    waiting = false;
	    break;
	case NOTIFY:
	    notify();
	    break;
	case STOP:
	    running = false;
	    notify();
	    break;
	}
    }
}
