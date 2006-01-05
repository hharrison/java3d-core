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
 * Master control thread.  The MasterControlThread object and thread
 * are created dynamically whenever needed.  Once created, the thread
 * runs until all other threads are terminated.  Then the master
 * control thread terminates.  There is never more than one
 * MasterControl object or thread in existence at any one time.
 */
class MasterControlThread extends Thread {

    private static int numInstances = 0;
    private int instanceNum = -1;

    private static synchronized int newInstanceNum() {
	return (++numInstances);
    }

    private int getInstanceNum() {
	if (instanceNum == -1)
	    instanceNum = newInstanceNum();
	return instanceNum;
    }

    MasterControlThread(ThreadGroup threadGroup) {
	super(threadGroup, "");
	setName("J3D-MasterControl-" + getInstanceNum());
	VirtualUniverse.mc.createMCThreads();
	this.start();
    }

    public void run() {

	do {
	    while (VirtualUniverse.mc.running) {
		VirtualUniverse.mc.doWork();

		// NOTE: no need to call Thread.yield(), since we will
		// call wait() if there is no work to do (yield seems
		// to be a no-op on Windows anyway)
	    }
	} while (!VirtualUniverse.mc.mcThreadDone());

	if(J3dDebug.devPhase) {
	    J3dDebug.doDebug(J3dDebug.masterControl, J3dDebug.LEVEL_1,
			     "MC: MasterControl Thread Terminate");
	}
    }
} 
