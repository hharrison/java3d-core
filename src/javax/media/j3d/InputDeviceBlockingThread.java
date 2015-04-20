/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

class InputDeviceBlockingThread extends Thread {

    // action flag for runMonitor
    private static final int WAIT   = 0;
    private static final int NOTIFY = 1;
    private static final int STOP   = 2;

    // blocking device that this thread manages
    private InputDevice device;
    private volatile boolean running = true;
    private volatile boolean stop = false;
    private boolean waiting = false;
    private boolean ready = false;
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


    @Override
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
        runMonitor(STOP);
    }

    synchronized void runMonitor(int action) {

	switch (action) {
	case WAIT:
            // Issue 279 - loop until ready
            while (running && !ready) {
                waiting = true;
                try {
                    wait();
                } catch (InterruptedException e) {}
                waiting = false;
            }
            ready = false;
	    break;
	case NOTIFY:
            ready = true;
            if (waiting) {
                notify();
            }
	    break;
	case STOP:
	    running = false;
            if (waiting) {
                notify();
            }
	    break;
	}
    }
}
