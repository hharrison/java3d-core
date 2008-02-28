/*
 * $RCSfile$
 *
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
