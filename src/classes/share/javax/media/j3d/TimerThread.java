/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The TimerThread is thread that handle WakeupOnElapsedTime call.
 * There is only one thread for the whole system.
 */

class TimerThread extends Thread {

    // action flag for runMonitor
    private static final int WAIT   = 0;
    private static final int NOTIFY = 1;
    private static final int STOP   = 2;

    private WakeupOnElapsedTimeHeap heap = new WakeupOnElapsedTimeHeap();

    // Wakeup InputDeviceScheduler for every sample time reach
    private WakeupOnElapsedTime inputDeviceSchedCond = 
        new WakeupOnElapsedTime(InputDeviceScheduler.samplingTime);

    // Wakeup {all?} Sound Scheduler{s} for every sample time reach
    // QUESTION: this sampling time is set to a very large value so Sound
    //          Schedulers are not pinged often unless explicitly requested
    // TODO: need a way to remove/null this condition when all
    //          soundschedulers are halted
    private WakeupOnElapsedTime soundSchedCond = 
        new WakeupOnElapsedTime(120000);  // every 2 minutes

    private boolean running = true;
    private volatile boolean waiting = false;

    TimerThread(ThreadGroup t) {
	super(t, "J3D-TimerThread");
    }

    // call from UserThread
    void add(WakeupOnElapsedTime wakeup) {
	synchronized (heap) {
	    heap.insert(wakeup);
	}
	runMonitor(NOTIFY, 0);
    }

    void addInputDeviceSchedCond() {
	inputDeviceSchedCond.triggeredTime = 
	    InputDeviceScheduler.samplingTime +
	    System.currentTimeMillis();
	add(inputDeviceSchedCond);
    }

    void addSoundSchedCond(long wakeupTime) {
        // TODO: there are potentially multiple sound schedulers.
        //     this code will force a wait up on ALL sound schedulers
        //     even though only one needs to process the sound that
        //     this wakeup condition is triggered by.
	soundSchedCond.triggeredTime = wakeupTime;
	add(soundSchedCond);
    }

    // call from MasterThread
    void finish() {
	runMonitor(STOP, 0);
    }

    void remove(WakeupOnElapsedTime w) {
	synchronized (heap) {
	    heap.extract(w);
	}
    }

    public void run() {
	long waitTime = -1;
	long time; 
	WakeupOnElapsedTime cond;

	while (running) {
	    runMonitor(WAIT, waitTime);
	    time = System.currentTimeMillis();

	    while (true) {
		cond = null;
		waitTime = -1;
		synchronized (heap) {
		    if (!heap.isEmpty()) {
			waitTime = heap.getMin().triggeredTime - time;
			if (waitTime <= 0) {
			    cond = heap.extractMin();
			}
		    }
		}
		if (cond == null) {
		    break;
		} else if (cond == inputDeviceSchedCond) {
		    VirtualUniverse.mc.sendRunMessage(
				      J3dThread.INPUT_DEVICE_SCHEDULER);
		} else if (cond == soundSchedCond) {
		    VirtualUniverse.mc.sendRunMessage(
				      J3dThread.SOUND_SCHEDULER);
		} else {
		    cond.setTriggered();
		}
	    }
	}
    }


    synchronized void runMonitor(int action, long waitTime) {
	switch (action) {
	case WAIT:
	    try {
		if (running) {
		    waiting = true;
		    if (waitTime < 0) {
			wait();
		    } else {
			wait(waitTime);
		    }
		}
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
