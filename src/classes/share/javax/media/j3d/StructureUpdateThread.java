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

package javax.media.j3d;

/**
 * The StructureUpdateThread is thread that passes messages to its structure
 */

class StructureUpdateThread extends J3dThread {
    /**
     * The structure that this thread works for
     */
    J3dStructure structure;

    /**
     * Some variables used to name threads correctly
     */
    private static int numInstances[] = new int[7];
    private int instanceNum[] = new int[7];

    private synchronized int newInstanceNum(int idx) {
	return (++numInstances[idx]);
    }

    int getInstanceNum(int idx) {
	if (instanceNum[idx] == 0)
	    instanceNum[idx] = newInstanceNum(idx);
	return instanceNum[idx];
    }

    /**
     * Just saves the structure
     */
    StructureUpdateThread(ThreadGroup t, J3dStructure s, int threadType) {
	super(t);
	structure = s;
	type = threadType;
	classification = J3dThread.UPDATE_THREAD;

	switch (type) {
    	case J3dThread.UPDATE_GEOMETRY:
	    setName("J3D-GeometryStructureUpdateThread-" + getInstanceNum(0));
	    break;
    	case J3dThread.UPDATE_RENDER:
	    setName("J3D-RenderStructureUpdateThread-" + getInstanceNum(1));
	    break;
    	case J3dThread.UPDATE_BEHAVIOR:
	    setName("J3D-BehaviorStructureUpdateThread-" + getInstanceNum(2));
	    break;
    	case J3dThread.UPDATE_SOUND:
	    setName("J3D-SoundStructureUpdateThread-" + getInstanceNum(3));
	    break;
    	case J3dThread.UPDATE_RENDERING_ATTRIBUTES:
	    // Only one exists in Java3D system
	    setName("J3D-RenderingAttributesStructureUpdateThread");
	    break;
    	case J3dThread.UPDATE_RENDERING_ENVIRONMENT:
	    setName("J3D-RenderingEnvironmentStructureUpdateThread-"+
		    getInstanceNum(4));
	    break;
	case J3dThread.UPDATE_TRANSFORM:
	    setName("J3D-TransformStructureUpdateThread-"+ getInstanceNum(5));
	    break;
        case J3dThread.SOUND_SCHEDULER:
            setName("J3D-SoundSchedulerUpdateThread-"+ getInstanceNum(6));
            break;

	}

    }

    void doWork(long referenceTime) {
	structure.processMessages(referenceTime);
    }
}
