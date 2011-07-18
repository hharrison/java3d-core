/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.audioengines;

/*
 * Audio Engine Thread
 */

import javax.media.j3d.*;

/**
 * The Thread Class extended for Audio Device engines that must process
 * calls dynamically, in 'real-time" to asynchronously change engine 
 * parameters.
 *
 * <p>
 * NOTE: this class is probably not needed for those Audio Device implementations
 * that handle all dynamic parameters in the low-level audio library.
 */
public class AudioEngineThread extends Thread {

    // Debug print flag
    static final protected boolean debugFlag = false;


    protected void debugPrint(String message) {
        if (debugFlag)
            System.out.println(message);
    }
 
    /**
     * The classification types.  
     */
    protected static final int WORK_THREAD   = 0x01;
    protected static final int UPDATE_THREAD = 0x02;

    /**
     * This runMonitor action puts the thread into an initial wait state
     */
    protected static final int WAIT = 0;

    /**
     * This runMonitor action notifies MasterControl that this thread 
     * has completed and wait.
     */
    protected static final int NOTIFY_AND_WAIT = 1;

    /**
     * This runMonitor action tells the thread to run N number of 
     * iterations.
     */
    protected static final int RUN = 2;

    /**
     * This runMonitor action tells the thread to stop running
     */
    protected static final int STOP = 3;

    /**
     * This indicates that this thread has been activated by MC
     */
    protected boolean active = false;

    /**
     * This indicates that this thread is alive and running
     */
    protected boolean running = true;


    /**
     * This indicates that this thread is ready
     */
    protected boolean started = false;

    /**
     * The time values passed into this thread
     */
    protected long referenceTime;

    /**
     * Use to assign threadOpts WAIT_ALL_THREADS
     */
    protected long lastWaitTimestamp = 0;

    /**
     * The type of this thread.  It is one of the above constants.
     */
    protected int type;

    /**
     * The classification of this thread.  It is one of the above constants.
     */
    protected int classification = WORK_THREAD;

    /**
     * The arguments passed in for this thread
     */
    protected Object[] args = null;

    /**
     * Flag to indicate that user initiate a thread stop
     */
    protected boolean userStop = false;

    /**
     * Flag to indicate that this thread is waiting to be notify
     */
    protected boolean waiting = false;

    /**
     * Some variables used to name threads correctly
     */
    protected static int numInstances = 0;
    protected int instanceNum = -1;

    /**
     * This constructor simply assigns the given id.
     */
    public AudioEngineThread(ThreadGroup t, String threadName) {
	super(t, threadName);
        if (debugFlag)
            debugPrint("AudioEngineThread.constructor("+threadName +")");
    }

    synchronized int newInstanceNum() {
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
    synchronized public void doWork() {
        if (debugFlag)
            debugPrint("AudioEngineThread.doWork()");
    }

    /**
     * This initializes this thread.  Once this method returns, the thread is
     * ready to do work.
     */
    public void initialize() {
        if (debugFlag)
            debugPrint("AudioEngineThread.initialize()");
	this.start();
	while (!started) {
	    try {
                Thread.currentThread().sleep(1, 0);
            } catch (InterruptedException e) {
            }
	}
    }

    /** 
     * This causes the threads run method to exit.
     */
    public void finish() {
	while (!waiting) {
	    try {
		Thread.sleep(10);
	    } catch (InterruptedException e) {}
	}
	runMonitor(STOP, 0,null);
    }

    /*
     * This thread controls the syncing of all the canvases attached to
     * this view.
     */
    public void run() {
        if (debugFlag)
            debugPrint("AudioEngineThread.run");
	runMonitor(WAIT, 0, null);
	while (running) {
	    doWork();
	    runMonitor(WAIT, 0, null);
	}
	// resource clean up
        shutdown();
    }

    synchronized public void runMonitor(int action, long referenceTime, Object[] args){
        switch (action) {
            case WAIT:
                if (debugFlag)
                   debugPrint("AudioEngineThread.runMonitor(WAIT)");
                try {
		  started = true;
		  waiting = true;
                  wait();
                } catch (InterruptedException e) {
                   System.err.println(e);
                }
		waiting = false;
                break;
            case RUN:
                if (debugFlag)
                   debugPrint("AudioEngineThread.runMonitor(RUN)");
		this.referenceTime = referenceTime;
		this.args = args;
                notify();
                break;
            case STOP:
                if (debugFlag)
                   debugPrint("AudioEngineThread.runMonitor(STOP)");
	        running = false;
	        notify();
	        break;
        }
    }

    public void shutdown() {
    }
    
    // default resource clean up method
    public void cleanup() {
	active = false;
	running = true;
	started = true;
	lastWaitTimestamp = 0;
	classification = WORK_THREAD;
	args = null;
	userStop = false;
	referenceTime = 0;
	
    }
}
