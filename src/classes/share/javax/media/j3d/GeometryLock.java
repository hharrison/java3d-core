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

import java.util.Vector;

class GeometryLock {

    // Current thread holding the lock
    Thread threadId = null; 

    // Whether the lock is currently owned
    boolean lockOwned = false;

    // Count > 1 , if there is nested lock by the same thread
    int count = 0;

    // Number of outstanding threads waiting for the lock
    int waiting = 0;


    synchronized void getLock() {
	Thread curThread = Thread.currentThread();
	// If the thread already has the lock, incr
	// a count and return
	if (threadId == curThread) {
	    count++;
	    return;
	}
	// Otherwise, wait until the lock is released
	while (lockOwned) {
	    try {
		waiting++;
		wait();
	    } catch (InterruptedException e) {
		System.err.println(e);
	    }
	    waiting--;
	}
	count++;
	// Acquire the lock
	lockOwned = true;
	threadId = curThread;
    }

    synchronized void unLock() {
	Thread curThread = Thread.currentThread();
	if (threadId == curThread) {
	    // If the lock count > 0, then return
	    if (--count > 0) {
		return;
	    }
	    lockOwned = false;
	    threadId = null;
	    if (waiting > 0) {
		notify();
	    }
	}
	    
    }
    
}
