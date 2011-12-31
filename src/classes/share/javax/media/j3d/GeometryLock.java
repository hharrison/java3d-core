/*
 * $RCSfile$
 *
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
