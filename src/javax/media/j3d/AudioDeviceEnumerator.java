/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * The class that enumerates all AudioDevices defined in the environment
 *
 * An AudioDeviceEnumerator generates the audio devices defined with the
 * execution environment of the currently running Java 3D application.
 */

class AudioDeviceEnumerator implements Enumeration {

    boolean endOfList;  // NOTE: list length always equals one or zero
    AudioDevice device;

    AudioDeviceEnumerator(PhysicalEnvironment physicalEnvironment) {
        device = physicalEnvironment.getAudioDevice();
        if(device == null)
            endOfList = true;
        else
            endOfList = false;
    }

    void reset() {
        if(device != null)
             endOfList = false;
    }


    /**
     * Query that tells whether the enumerator has more elements
     * @return true if the enumerator has more elements, false otherwise
     */
    @Override
    public boolean hasMoreElements() {
        if(endOfList == false)
            return true;
        else
            return false;
    }

    /**
     * Return the next element in the enumerators
     * @return the next element in this enumerator
     */
    @Override
    public Object nextElement() {
        if (this.hasMoreElements()) {
            endOfList = true;
            return ((Object) device);
        } else {
            throw new NoSuchElementException(J3dI18N.getString("AudioDeviceEnumerator0"));
        }
    }
}
