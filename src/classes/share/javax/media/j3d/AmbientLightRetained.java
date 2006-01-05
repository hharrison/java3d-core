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

/**
 * An ambient light source object.
 */

class AmbientLightRetained extends LightRetained {

    AmbientLightRetained() {
        this.nodeType = NodeRetained.AMBIENTLIGHT;
	lightType = 1;
	localBounds = new BoundingBox();
	((BoundingBox)localBounds).setLower( 1.0, 1.0, 1.0);
	((BoundingBox)localBounds).setUpper(-1.0,-1.0,-1.0);
    }

    void setLive(SetLiveState s) {
	super.setLive(s);
	J3dMessage createMessage = super.initMessage(7);
	VirtualUniverse.mc.processMessage(createMessage);
    }

    void update(long ctx, int lightSlot, double scale) {
    }
}
