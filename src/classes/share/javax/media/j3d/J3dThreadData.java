/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The J3dThreadData is the data wrapper for threads in Java 3D.  
 */

class J3dThreadData extends Object {
    /**
     * Thread run options
     */
    static final int WAIT_ALL_THREADS = 0x01;
    static final int CONT_THREAD      = 0x02;
    static final int WAIT_THIS_THREAD = 0x04;
    static final int START_TIMER      = 0x08;
    static final int STOP_TIMER       = 0x10;
    static final int LAST_STOP_TIMER  = 0x20; 
    //static final int LOCK_RENDERBIN   = 0x20;
    //static final int RELEASE_RENDERBIN = 0x40;

    /**
     * The thread for this data
     */
    J3dThread thread = null;

    /**
     * The last time that a message was sent to this thread.
     */
    long lastUpdateTime = -1;

    /**
     * The last time that this thread was run
     */
    long lastRunTime = -1;

    /**
     * The thread type
     */
    int threadType = 0;

    /**
     * The run options for this thread.
     */
    int threadOpts = 0;

    /**
     * The arguments to be passed to this thread
     */
    Object threadArgs = null;

    /**
     * This indicates whether or not this thread needs to run.
     */
    boolean needsRun = false;

    /**
     * The following data is only used by the Render Thread
     */

    /**
     * The type of the thread invocation.  RENDER or SWAP
     */
    int type = 0;

    /**
     * The view that this Render invocation belongs to.
     */
    View view = null;

    /**
     * The Canvas3D that this Render invocation belongs to.
     * It is null for the SWAP invocation.
     */
    Canvas3D canvas = null;

    /**
     * This constructor does nothing
     */
    J3dThreadData() {
    }
}
