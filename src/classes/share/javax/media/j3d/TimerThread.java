/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
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
    // XXXX: need a way to remove/null this condition when all
    //          soundschedulers are halted
    private WakeupOnElapsedTime soundSchedCond = 
        new WakeupOnElapsedTime(120000);  // every 2 minutes

    private volatile boolean running = true;
    private boolean waiting = false;
    private boolean ready = false;

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
	    J3dClock.currentTimeMillis();
	add(inputDeviceSchedCond);
    }

    void addSoundSchedCond(long wakeupTime) {
        // XXXX: there are potentially multiple sound schedulers.
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
	    time = J3dClock.currentTimeMillis();

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
            // Issue 308 - wait unless ready flag already set
            // Note that we can't loop since we need to be able to timeout
            // after "waitTime" msec
            if (running && !ready) {
                waiting = true;
                try {
                    if (waitTime < 0) {
                        wait();
                    } else {
                        wait(waitTime);
                    }
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
	    notify();
	    break;
	}
    }

}
