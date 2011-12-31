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
