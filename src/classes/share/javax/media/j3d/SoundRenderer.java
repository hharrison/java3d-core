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

import javax.vecmath.*;
import java.util.Vector;

class SoundRenderer extends Object {

    SoundRenderer() {
    }

    void activate(SoundRetained sound, SoundscapeRetained ss) {
        AuralAttributesRetained aa = ss.attributes.mirrorAa;

	if (sound instanceof BackgroundSoundRetained) {
	        System.out.println("Activating BackgroundSoundRetained");
	} else if (sound instanceof ConeSoundRetained) {
	        System.out.println("Activating ConeSoundRetained");
	} else if (sound instanceof PointSoundRetained) {
	        System.out.println("Activating PointSoundRetained");
	}
        if (ss != null)
	    System.out.println("Soundscape is " + ss);
        else
	    System.out.println("Soundscape is null");

        if (aa != null)
	    System.out.println("AuralAttributes is " + aa);
        else
	    System.out.println("AuralAttributes is null");
    }
 
    void update(SoundRetained sound, SoundscapeRetained ss) {
        AuralAttributesRetained aa = ss.attributes.mirrorAa;

	if (false) {
	    if (sound instanceof BackgroundSoundRetained) {
	        System.out.println("Updating BackgroundSoundRetained");
	    } else if (sound instanceof ConeSoundRetained) {
	        System.out.println("Updating ConeSoundRetained");
	    } else if (sound instanceof PointSoundRetained) {
	        System.out.println("Updating PointSoundRetained");
	    }
	    System.out.println("Soundscape is " + ss);
	}
    }
 
    void deactivate(SoundRetained sound) {
	if (false) {
	    if (sound instanceof BackgroundSoundRetained) {
	        System.out.println("Deactivating BackgroundSoundRetained");
	    } else if (sound instanceof ConeSoundRetained) {
	        System.out.println("Deactivating ConeSoundRetained");
	    } else if (sound instanceof PointSoundRetained) {
	        System.out.println("Deactivating PointSoundRetained");
	    }
	}
    }
 
    public String toString() {
           return "";
    }
 
}
