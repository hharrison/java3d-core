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

#include "StdAfx.h"



#ifdef STRICT
typedef WNDPROC PROC_TYPE;
#else
typedef FARPROC PROC_TYPE;
#endif


static PROC_TYPE g_lpDefWindowProcChild;
static long oldWindowHandle = 0;

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativeWSInfo_subclass(
    JNIEnv * env,
    jobject obj,
    jint hwnd)
{
    // For some reason, setting the callback before
    // setCooperative level will cause setCooperative level
    // to hang when start in fullscreen mode
    // So we delay it later.
}

// Handle child move request
static LRESULT CALLBACK canvas3dWndProcChild(
    HWND hwnd,
    UINT message,
    WPARAM wParam,
    LPARAM lParam)
{
    /*
    switch (message) {
        case WM_NCPAINT:
	    // handle switch mode from 256 back to >= 16 bits color mode
	    DWORD status;
	    if (hSema != NULL) {
		status = WaitForSingleObject(hSema, 0);
		if (status == WAIT_OBJECT_0) {
		    // This prevent deadlock, otherwise the window event
		    // thread will hangs if lock already acquired
		    // during window resize/toggle.
		    D3dCtx *ctx = findCtx(hwnd);
		    if (ctx != NULL) {
			ctx->retryRestoreSurface = true;
		    }    
		    unlock();
		} 
	    }
	    break;
    }
    */
    return CallWindowProc(g_lpDefWindowProcChild, hwnd, message,
			  wParam, lParam);
}


VOID setWindowCallback(HWND topHwnd, HWND hwnd)
{
    // For some reasons, setting proc for the same handle 
    // will crash the application. So we work around this
    // by checking the old window handle before setting the
    // new one.
    //    long newWindowHandle;
    /*
    if (oldWindowHandle == 0) {
	oldWindowHandle = GetWindowLong((HWND) hwnd, GWL_WNDPROC); 
    }

    newWindowHandle = GetWindowLong((HWND) hwnd, GWL_WNDPROC);
    if (newWindowHandle == oldWindowHandle) { 
	g_lpDefWindowProcChild = (PROC_TYPE)
	    SetWindowLong((HWND) hwnd, GWL_WNDPROC, 
			  (LONG) canvas3dWndProcChild);
    } 
    */
}

