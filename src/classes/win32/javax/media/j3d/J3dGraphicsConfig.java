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

import sun.awt.Win32GraphicsConfig;
import java.awt.*;

class J3dGraphicsConfig extends Win32GraphicsConfig {

    private int pixelFormat;

    J3dGraphicsConfig(GraphicsDevice gd, int pixelFormat)
    {
	super(gd, pixelFormat);
	this.pixelFormat = pixelFormat;
    }
    
    static boolean isValidPixelFormat(GraphicsConfiguration gc) {
	return (gc instanceof J3dGraphicsConfig);
    }

    int getPixelFormat() {
	return pixelFormat;
    }
    

}
