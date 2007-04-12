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

import javax.vecmath.*;
import java.util.Vector;

class SoundRenderer extends Object {

    SoundRenderer() {
    }

    void activate(SoundRetained sound, SoundscapeRetained ss) {
        AuralAttributesRetained aa = ss.attributes.mirrorAa;

	if (sound instanceof BackgroundSoundRetained) {
	        System.err.println("Activating BackgroundSoundRetained");
	} else if (sound instanceof ConeSoundRetained) {
	        System.err.println("Activating ConeSoundRetained");
	} else if (sound instanceof PointSoundRetained) {
	        System.err.println("Activating PointSoundRetained");
	}
        if (ss != null)
	    System.err.println("Soundscape is " + ss);
        else
	    System.err.println("Soundscape is null");

        if (aa != null)
	    System.err.println("AuralAttributes is " + aa);
        else
	    System.err.println("AuralAttributes is null");
    }
 
    void update(SoundRetained sound, SoundscapeRetained ss) {
        AuralAttributesRetained aa = ss.attributes.mirrorAa;

	if (false) {
	    if (sound instanceof BackgroundSoundRetained) {
	        System.err.println("Updating BackgroundSoundRetained");
	    } else if (sound instanceof ConeSoundRetained) {
	        System.err.println("Updating ConeSoundRetained");
	    } else if (sound instanceof PointSoundRetained) {
	        System.err.println("Updating PointSoundRetained");
	    }
	    System.err.println("Soundscape is " + ss);
	}
    }
 
    void deactivate(SoundRetained sound) {
	if (false) {
	    if (sound instanceof BackgroundSoundRetained) {
	        System.err.println("Deactivating BackgroundSoundRetained");
	    } else if (sound instanceof ConeSoundRetained) {
	        System.err.println("Deactivating ConeSoundRetained");
	    } else if (sound instanceof PointSoundRetained) {
	        System.err.println("Deactivating PointSoundRetained");
	    }
	}
    }
 
    public String toString() {
           return "";
    }
 
}
