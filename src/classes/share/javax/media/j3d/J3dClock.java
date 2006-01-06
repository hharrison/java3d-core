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
 * Utility class to provide a more accurate replacement for
 * System.currentTimeMillis().
 */
class J3dClock {

    private static long deltaTime;
    private static final long nsecPerMsec = 1000000;

    /**
     * Private constructor, since no instance should ever be created.
     */
    private J3dClock() {
    }
    
    /**
     * Method to return a high-resolution timer value.
     *
     * NOTE: when we no longer support JDK 1.4.2 we can replace this
     * method with System.nanoTime().
     */
    private static long getHiResTimerValue() {
        return MasterControl.getNativeTimerValue();
    }

    /**
     * Returns the current time in milliseconds. This is a more
     * accurate version of System.currentTimeMillis and should be used in
     * its place.
     *
     * @return the current time in milliseconds.
     */
    static long currentTimeMillis() {
        return (getHiResTimerValue() / nsecPerMsec) + deltaTime;
    }

    static {
        // Ensure that the native libraries are loaded (this can be removed
        // once we switch to using System.nanoTime()
        VirtualUniverse.loadLibraries();

        // Call time methods once without using their values to ensure that
        // the methods are "warmed up". We need to make sure that the actual
        // calls that we use take place as close together as possible in time.
        System.currentTimeMillis();
        getHiResTimerValue();

        // Compute deltaTime between System.currentTimeMillis()
        // and the high-res timer, use a synchronized block to force both calls
        // to be made before the integer divide
        long baseTime, baseTimerValue;
        synchronized (J3dClock.class) {
            baseTime = System.currentTimeMillis();
            baseTimerValue = getHiResTimerValue();
        }
        deltaTime = baseTime - (baseTimerValue / nsecPerMsec);
    }
}
