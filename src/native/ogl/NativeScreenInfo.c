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

#pragma weak glXChooseFBConfig


/*
 * Class:     javax_media_j3d_NativeScreenInfo
 * Method:    openDisplay
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_javax_media_j3d_NativeScreenInfo_openDisplay(
    JNIEnv *env,
    jclass cls)
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
    jclass cls,
    jlong display)
{
    Display* dpy = (Display*)display;
    return (jint)DefaultScreen(dpy);
}

/*
 * Class:     javax_media_j3d_NativeScreenInfo
 * Method:    queryGLX13
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_NativeScreenInfo_queryGLX13(
    JNIEnv *env,
    jclass cls,
    jlong display)
{
    /* Fix for Issue 20 */
    void (*tmpfp)();
    int major, minor;
    int errorBase, eventBase;
    Display* dpy = (Display*)display;
    // It should be cleaner to return both the major and minor to the caller.

    if (!glXQueryExtension(dpy, &errorBase, &eventBase)) {
	fprintf(stderr, "Java 3D ERROR : GLX extension is not supported\n");
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }

#if 0 /* Temporary disable this code segment because the ATI driver incorrectly
	 return 1.2 */
    
    /* Check for glX 1.3 and higher */
    if (glXQueryVersion(dpy, &major, &minor)) {
	/* fprintf(stderr, "Checking glX version : %d.%d\n",major, minor); */
	if (!(major == 1 && minor >= 3)) {
	    fprintf(stderr, "Java 3D ERROR : GLX version = %d.%d\n", major, minor);
	    fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	    return JNI_FALSE;
	}
    }
    else {
	fprintf(stderr, "Java 3D ERROR : Unable to query GLX version\n");
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }
    
#elseif
    
    tmpfp = (void (*)())glXChooseFBConfig;

    if (tmpfp == NULL) {
	glXQueryVersion(dpy, &major, &minor);
	fprintf(stderr, "Java 3D ERROR : glXChooseFBConfig not found\n");
	fprintf(stderr, "    GLX version = %d.%d\n", major, minor);
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }

#endif
    
    return JNI_TRUE;
}

#endif
