/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
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
    public Object nextElement() {
        if (this.hasMoreElements()) {
            endOfList = true;
            return ((Object) device);
        } else {
            throw new NoSuchElementException(J3dI18N.getString("AudioDeviceEnumerator0"));
        }
    }
}
