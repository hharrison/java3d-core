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
	    if(debug) System.out.println("ReadWriteLock.java : Problem! readCount is >= 0.");
	
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
