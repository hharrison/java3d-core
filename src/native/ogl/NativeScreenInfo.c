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

#include <jni.h>
#include <math.h>

#include "gldefs.h"

#if defined(SOLARIS) || defined(__linux__)
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#endif

#ifdef WIN32
#include <windows.h>
#endif

#if defined(SOLARIS) || defined(__linux__)
/*
 * Class:     javax_media_j3d_NativeScreenInfo
 * Method:    openDisplay
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_javax_media_j3d_NativeScreenInfo_openDisplay(
    JNIEnv *env,
    jobject obj)
{
    Display* dpy;
    dpy = XOpenDisplay(NULL);
    return (jlong)dpy;
}


/*
 * Class:     javax_media_j3d_NativeScreenInfo
 * Method:    getDefaultScreen
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL
Java_javax_media_j3d_NativeScreenInfo_getDefaultScreen(
    JNIEnv *env,
    jobject obj,
    jlong display)
{
    Display* dpy = (Display*)display;
    return (jint)DefaultScreen(dpy);
}
#endif
