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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

package javax.media.j3d;

import java.awt.*;
import java.awt.event.*;
import sun.awt.*;
import java.lang.reflect.Method;

class NativeWSInfo {

    //X11DrawingSurface xds;
    Object xds;

    void getCanvasWSParameters(Canvas3D canvas) {
        try {
            Class x11DSclass = Class.forName("sun.awt.X11DrawingSurface");
            Method getDrawable = x11DSclass.getDeclaredMethod("getDrawable", null );
            Method getVisualID = x11DSclass.getDeclaredMethod("getVisualID", null );

        //canvas.window = xds.getDrawable();
        //canvas.vid = xds.getVisualID();

            canvas.window = ((Integer)getDrawable.invoke( xds, null )).intValue();
            canvas.vid = ((Integer)getVisualID.invoke( xds, null )).intValue();
        } catch( Exception e ) {
            e.printStackTrace();
        }
      }
 
    void getWSDrawingSurface( Object dsi) {
        try {
        //xds = (X11DrawingSurface)dsi.getSurface();
        Class drawingSurfaceInfoClass = Class.forName("sun.awt.DrawingSurfaceInfo");
        Method getSurface = drawingSurfaceInfoClass.getDeclaredMethod( "getSurface", null);

        //xds = dsi.getSurface();
        xds = getSurface.invoke( dsi, null );
        } catch( Exception e ) {
            e.printStackTrace();
        }
      }

    int getCanvasVid(GraphicsConfiguration gcfg) {
        return (((X11GraphicsConfig)gcfg).getVisual());
      }
}

