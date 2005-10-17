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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

#include <jni.h>
#include <math.h>

#include "gldefs.h"

#if defined(UNIX)
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#endif

#ifdef WIN32
#include <windows.h>
#endif

#ifdef WIN32

/*
 * Workaround for bug 4169320: Resizing a Java 3D canvas
 * on Win95 crashes the application
 */

#ifdef STRICT
static WNDPROC g_lpDefWindowProc;
#else
static FARPROC g_lpDefWindowProc;
#endif

static
LRESULT CALLBACK canvas3dWndProc(
    HWND hWnd,
    UINT message,
    WPARAM wParam,
    LPARAM lParam)
{
    switch (message) {
      case WM_WINDOWPOSCHANGED:
          return 0;
      default:
          return CallWindowProc(g_lpDefWindowProc, hWnd, message,
                                wParam, lParam);
    }
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_NativeWSInfo_subclass(
    JNIEnv * env,
    jobject obj,
    jint hWnd)
{
#ifdef STRICT
    g_lpDefWindowProc = (WNDPROC) SetWindowLong((HWND) hWnd, GWL_WNDPROC,
                                                (LONG) canvas3dWndProc);
#else
    g_lpDefWindowProc = (FARPROC) SetWindowLong((HWND) hWnd, GWL_WNDPROC,
                                                (LONG) canvas3dWndProc);
#endif
}

#endif
