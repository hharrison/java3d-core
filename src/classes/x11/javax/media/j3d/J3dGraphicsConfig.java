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

import sun.awt.*;
import java.awt.*;

class J3dGraphicsConfig {

    private static native boolean isValidVisualID(long display, int vid);

    J3dGraphicsConfig(GraphicsDevice gd, int pixelFormat) {
	// a dummy class that should never be invoked under X11
        assert false;
    }
    
    static boolean isValidPixelFormat(GraphicsConfiguration gc) {
	return isValidVisualID(NativeScreenInfo.getStaticDisplay(),
			       ((X11GraphicsConfig) gc).getVisual());
    }

    static boolean isValidConfig(GraphicsConfiguration gc) {	
	// Check to see if a valid FBConfig pointer has been cached.
	Object fbConfigObject = Canvas3D.fbConfigTable.get(gc);
	return ((fbConfigObject != null) && 
		(fbConfigObject instanceof GraphicsConfigInfo));
    }


}
