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

package org.jogamp.java3d;

/**
 * The J3dThread is the super class of all slave threads in Java 3D.  It implements
 * all of the common flow control constructs.
 */

abstract class J3dThread extends Thread {
    /**
     * These are the thread types that a message may affect
     */
    static final int BEHAVIOR_SCHEDULER     		= 0x01;
    static final int SOUND_SCHEDULER        		= 0x02;
    static final int INPUT_DEVICE_SCHEDULER 		= 0x04;
    static final int RENDER_THREAD          		= 0x10;
//  static final int COLLISION_THREAD       		= 0x20;
    static final int UPDATE_GEOMETRY	    		= 0x40;
    static final int UPDATE_RENDER  	    		= 0x80;
    static final int UPDATE_BEHAVIOR	    		= 0x100;
    static final int UPDATE_SOUND   	    		= 0x200;
    static final int UPDATE_RENDERING_ATTRIBUTES    	= 0x400;
    static final int UPDATE_RENDERING_ENVIRONMENT    	= 0x1000;
    static final int UPDATE_TRANSFORM    	        = 0x2000;

    /**
     * The classification types.
     */
    static final int WORK_THREAD   = 0x01;
    static final int UPDATE_THREAD = 0x02;

    /**
     * This runMonitor action puts the thread into an initial wait state
     */
    static final int WAIT = 0;

    /**
     * This runMonitor action notifies MasterControl that this thread
     * has completed and wait.
     */
    static final int NOTIFY_AND_WAIT = 1;

    /**
     * This is used by Canvas3D Renderer to notify user thread
     * that swap is completed.
     */
    static final int NOTIFY = 2;

    /**
     * This runMonitor action tells the thread to run N number of
     * iterations.
     */
    static final int RUN = 2;

    /**
     * This runMonitor action tells the thread to stop running
     */
    static final int STOP = 3;

    /**
     * This indicates that this thread has been activated by MC
     */
    boolean active = false;

    /**
     * This indicates that this thread is alive and running
     */
    private volatile boolean running = true;

    /**
     * This flag is set by the RUN action of runMonitor to indicate that the
     * waiting thread has work to do.
     */
    private volatile boolean ready = false;

    /**
     * The thread data for this thread
     */
    private J3dThreadData[] data = null;

    /**
     * This indicates that this thread is started and able to accept work
     */
    private volatile boolean started = false;

    /**
     * The time values passed into this thread
     */
    long referenceTime;

    /**
     * Use to assign threadOpts WAIT_ALL_THREADS
     */
    long lastWaitTimestamp = 0;

    /**
     * The type of this thread.  It is one of the above constants.
     */
    int type;

    /**
     * The classification of this thread.  It is one of the above constants.
     */
    int classification = WORK_THREAD;

    /**
     * The arguments passed in for this thread
     */
    Object[] args = null;

    /**
     * Flag to indicate that user initiate a thread stop
     */
    volatile boolean userStop = false;

    /**
     * Flag to indicate that this thread is waiting to be notify
     */
    private volatile boolean waiting = false;

    /**
     * Some variables used to name threads correctly
     */
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

    /**
     * This method is defined by all slave threads to implement
     * one iteration of work.
     */
    abstract void doWork(long referenceTime);

    /**
     * This constructor simply assigns the given id.
     */
    J3dThread(ThreadGroup t) {
	super(t, "");
    }

    /**
     * This returns the thread data for this thread.
     */
    synchronized J3dThreadData getThreadData(View v, Canvas3D c) {
	J3dThreadData threadData;
	int i, j;
	J3dThreadData[] newData;

	if (type != RENDER_THREAD) { // Regular Thread
	    if (data == null) {
		data = new J3dThreadData[1];
		data[0] = new J3dThreadData();
		data[0].thread = this;
		data[0].threadType = type;
		data[0].view = null;
		data[0].canvas = null;
	    }
	    threadData = data[0];
	} else {	      // Render thread

	// Note: each renderer has multiple thread data mappings
        //       for its render and swap threads

	    if (data == null) {
		data = new J3dThreadData[1];
		data[0] = new J3dThreadData();
		data[0].thread = this;
		data[0].threadType = type;
		data[0].view = v;
		data[0].canvas = c;
		data[0].threadArgs = new Object[4];
		threadData = data[0];
	    } else {
		for (i=0; i<data.length; i++) {
		    if (data[i].view == v && data[i].canvas == c) {
			break;
		    }
		}
		if (i==data.length) {
		    newData = new J3dThreadData[data.length+1];
		    for (j=0; j<data.length; j++) {
			newData[j] = data[j];
		    }
		    data = newData;
		    data[j] = new J3dThreadData();
		    data[j].thread = this;
		    data[j].threadType = type;
                    data[j].view = v;
                    data[j].canvas = c;
		    data[j].threadArgs = new Object[4];
                    threadData = data[j];
		} else {
		    threadData = data[i];
		    Object args[] = (Object []) threadData.threadArgs;
		    args[0] = null;
		    args[1] = null;
		    args[2] = null;
		    args[3] = null;
		}
	    }

	}

	return (threadData);
    }

    /**
     * This initializes this thread.  Once this method returns, the thread is
     * able to accept work.
     */
    void initialize() {
	this.start();
	while (!started) {
	    MasterControl.threadYield();
	}
    }

    /**
     * This causes the threads run method to exit.
     */
    void finish() {
        // NOTE: This spin loop is probably not necessary.
	while (!waiting) {
	    MasterControl.threadYield();
	}
	runMonitor(STOP, 0,null);
    }

    /**
     * This thread controls the syncing of all the canvases attached to
     * this view.
     */
    @Override
    public void run() {
	runMonitor(WAIT, 0, null);
	while (running) {
	    doWork(referenceTime);
	    runMonitor(NOTIFY_AND_WAIT, 0, null);
	}
	// resource clean up
        shutdown();
    }

    synchronized void runMonitor(int action, long referenceTime,
				 Object[] args) {
        switch (action) {
            case WAIT:
                started = true;
                // Issue 279 - loop until ready
                while (!ready && running) {
                    waiting = true;
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    }
                    waiting = false;
                }
                ready = false;
                break;

            case NOTIFY_AND_WAIT:
                VirtualUniverse.mc.runMonitor(MasterControl.THREAD_DONE, null,
                        null, null, this);
                // Issue 279 - loop until ready
                while (!ready && running) {
                    waiting = true;
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        System.err.println(e);
                    }
                    waiting = false;
                }
                ready = false;
                break;

            case RUN:
                this.referenceTime = referenceTime;
                this.args = args;
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

    void cleanupView() {
	// renderer will reconstruct threadData next time
	// in getThreadData
	data = null;
    }

    // default resource clean up method
    void shutdown() {
    }

    void cleanup() {
	active = false;
	running = true;
        ready = false;
	data = null;
	started = true;
	lastWaitTimestamp = 0;
	classification = WORK_THREAD;
	args = null;
	userStop = false;
	referenceTime = 0;

    }

}
