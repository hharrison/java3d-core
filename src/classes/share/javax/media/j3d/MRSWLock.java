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

/**
 * Use this lock to allow multiple reads/single write synchronization.
 * To prevent deadlock a read/writeLock call must match with a read/writeUnlock call.
 * Write request has precedence over read request.
 */

class MRSWLock {

    static boolean debug = false;

    private int readCount;
    private boolean write;
    private int writeRequested;
    private int lockRequested;

    MRSWLock() {
	readCount = 0;
	write = false;
	writeRequested = 0;
	lockRequested = 0;
    }

    synchronized final void readLock() {
	lockRequested++;
	while((write == true) || (writeRequested > 0)) {
	    try { wait(); } catch(InterruptedException e){}
	}
	lockRequested--;
	readCount++;
    }

    synchronized final void readUnlock() {
	if(readCount>0)
	    readCount--;
	else
	    if(debug) System.err.println("ReadWriteLock.java : Problem! readCount is >= 0.");

	if(lockRequested>0)
	    notifyAll();
    }

    synchronized final void writeLock() {
	lockRequested++;
	writeRequested++;
	while((readCount>0)||(write == true)) {
	    try { wait(); } catch(InterruptedException e){}
	}
	write = true;
	lockRequested--;
	writeRequested--;
    }

    synchronized final void writeUnlock() {
	write = false;

	if(lockRequested>0)
	    notifyAll();
    }

}
