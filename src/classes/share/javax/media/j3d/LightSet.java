/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
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
