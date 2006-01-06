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
