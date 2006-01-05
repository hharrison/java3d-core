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

import java.awt.*;
import java.awt.event.*;
import sun.awt.*;
import java.lang.reflect.Method;

class NativeWSInfo {

    //Win32DrawingSurface wds;
    Object wds;

    void getCanvasWSParameters(Canvas3D canvas) {
        //canvas.window = wds.getHDC();
        try {
            Class win32DSclass = Class.forName("sun.awt.Win32DrawingSurface");
            Method getHDC = win32DSclass.getDeclaredMethod("getHDC", null );
            
            canvas.window = ((Integer)getHDC.invoke( wds, null )).intValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
      }
 
    void getWSDrawingSurface(Object dsi) {
        //wds = (Win32DrawingSurface)dsi.getSurface();
        int hwnd =0;
        
        try {
            Class drawingSurfaceInfoClass = Class.forName("sun.awt.DrawingSurfaceInfo");
            Method getSurface = drawingSurfaceInfoClass.getDeclaredMethod( "getSurface", null);

            wds = getSurface.invoke( dsi, null );
            
            Class win32DSclass = Class.forName("sun.awt.Win32DrawingSurface");
            Method getHWnd = win32DSclass.getDeclaredMethod("getHWnd", null );
            hwnd = ((Integer)getHWnd.invoke( wds, null )).intValue();
        } catch( Exception e ) {
            e.printStackTrace();
        }
        
	// note: dsi lock is called from the caller of this method
	// Workaround for bug 4169320
	//dsi.lock();
	//subclass(wds.getHWnd());
	subclass(hwnd);
	//dsi.unlock();
    }

    // Used in workaround for bug 4169320: Resizing a Java 3D canvas
    // on Win95 crashes the application
    private native void subclass(int hWnd);

    int getCanvasVid(GraphicsConfiguration gcfg) {
        return ((J3dGraphicsConfig) gcfg).getPixelFormat();
    }
}
