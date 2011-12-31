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

class LightSet extends Object {
    /**
     * The Lights that make up this set
     */
    LightRetained[] lights = null;

    // The number of lights in this lightset, may be less than lights.length
    int nlights = 0;

    // A reference to the next LightSet
    LightSet next = null;

    // A reference to the previous LightSet
    LightSet prev = null;

    // A flag that indicates that lighting is on
    boolean lightingOn = true;

    // A flag that indicates that this light set has changed.
    boolean isDirty = true;

    /**
     * Constructs a new LightSet
     */
    LightSet(RenderBin rb, RenderAtom ra, LightRetained[] lights,
	     int nlights, boolean lightOn) {
	this.reset(rb, ra, lights, nlights, lightOn);
    }

    void reset(RenderBin rb, RenderAtom ra, LightRetained[] lights,
	       int nlights, boolean lightOn) {
	int i;

	this.isDirty = true;
	this.lightingOn = lightOn;
	if (this.lights == null || this.lights.length < nlights) {
	   this.lights = new LightRetained[nlights];
	}

	for (i=0; i<nlights; i++) {
	    this.lights[i] = lights[i];
	}

	this.nlights = nlights;

        //lists = new RenderList(ro);
        //lists.prims[ro.geometry.geoType-1] = ro;
    }

    boolean equals(RenderBin rb, LightRetained[] lights, int nlights,
		   boolean lightOn) {
	int i, j;
	LightRetained light;

	if (this.nlights != nlights)
	   return(false);

	if (this.lightingOn != lightOn)
	   return(false);

	for (i=0; i<nlights; i++) {
	   for (j=0; j<this.nlights; j++) {
	      if (this.lights[j] == lights[i]) {
		 break;
	      }
	   }
	   if (j==this.nlights) {
	      return(false);
	   }
	}
	return(true);
    }

}
