/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.timer;

/**
 * A High Resolution interval timer. The timer resolution is 
 * operating system dependent and can be queried using 
 * getTimerResolution(). 
 *
 * These methods are not reentrant and should not
 * be called concurrently from multiple threads.
 *
 * @deprecated Use java.lang.System.nanoTime() instead.
 */
public class J3DTimer {

    // Since we can't get the resolution from the JDK, we will hard-code it
    // at 1000 (microsecond resolution).
    private static final long resolution = 1000L;

    /**
     * Private constructor because users should
     * not construct instances of this class
     */
    private J3DTimer() {
    }

    /**
     * Get the timer value, in nanoseconds.
     * The initial value of the timer is OS dependent.
     *
     * @return The current timer value in nanoseconds.
     */
    public static long getValue() {
        return System.nanoTime();
    }

    /**
     * Get the nanosecond resolution of the timer
     *
     * @return The timer resolution in nanoseconds.
     */
    public static long getResolution() {
        return resolution;
    }
}
