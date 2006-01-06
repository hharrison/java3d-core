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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

#ifdef DEBUG
#define DPRINT(args) fprintf args
#else
#define DPRINT(args)
#endif /* DEBUG */

#include <jni.h>
#include <math.h>
#include <string.h>
#include "gldefs.h"

#ifdef WIN32
#include <windows.h>
#include <winbase.h>
#endif /* WIN32 */

#if defined(UNIX)
#include <unistd.h>
#ifdef SOLARIS
#include <thread.h>
#else
#include <pthread.h>
#endif
#include <dlfcn.h>
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>

#if defined(SOLARIS) && defined(__sparc)
#pragma weak glXInitThreadsSUN
#pragma weak glXDisableXineramaSUN
#pragma weak XPanoramiXQueryExtension

extern int glXInitThreadsSUN();
extern int glXDisableXineramaSUN(Display *dpy);


/*
 * The following is currently an unsupported, undocumented function to query
 * whether the X server is running with Xinerama support.  This is an interim
 * solution until it is made part of the X Window System standard or replaced
 * with a fully supported API.  It is currently available in the libXext
 * shipped with Solaris 9 and patched versions of Solaris 7 and 8.  dlsym() is
 * used to check for its existence.
 */
extern Bool XPanoramiXQueryExtension(Display *dpy,
				     int *event_base, int *error_base);
#endif /* SOLARIS && __sparc */

#endif /* UNIX_ */

/* defined in Canvas3D.c */
extern int isExtensionSupported(const char *allExtensions,
				const char *extension); 

JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_MasterControl_initializeJ3D(
    JNIEnv *env, jobject obj, jboolean disableXinerama)
{
    jboolean glIsMTSafe = JNI_TRUE;

    /* Nothing to do for non-sparc-solaris platforms */

#if defined(SOLARIS) && defined(__sparc)
    Display* dpy;
    int event_base, error_base;
    const char *glxExtStr = NULL;
    
    glIsMTSafe = JNI_FALSE;

    dpy = XOpenDisplay(NULL);
    glxExtStr = glXGetClientString((Display*)dpy, GLX_EXTENSIONS);

#ifdef GLX_SUN_init_threads
    if(isExtensionSupported(glxExtStr, "GLX_SUN_init_threads")) {
	if (glXInitThreadsSUN()) {
	    glIsMTSafe = JNI_TRUE;
	}
	else {
	    DPRINT((stderr, "Failed initializing OpenGL for MT rendering.\n"));
	    DPRINT((stderr, "glXInitThreadsSUN returned false.\n"));
	}
    }
    else {
	DPRINT((stderr, "Failed to initialize OpenGL for MT rendering.\n"));
	DPRINT((stderr, "GLX_SUN_init_threads not available.\n"));
    }
#endif /* GLX_SUN_init_threads */

    if (disableXinerama) {
	DPRINT((stderr, "Property j3d.disableXinerama true "));

	if ((! dlsym(RTLD_DEFAULT, "XPanoramiXQueryExtension")) ||
	    (! dlsym(RTLD_DEFAULT, "XDgaGetXineramaInfo"))) {

	    DPRINT((stderr, "but required API not available.\n"));
	    return glIsMTSafe;
	}

	if (XPanoramiXQueryExtension(dpy, &event_base, &error_base)) {
	    DPRINT((stderr, "and Xinerama is in use.\n"));
#ifdef GLX_SUN_disable_xinerama
	    if(isExtensionSupported(glxExtStr, "GLX_SUN_disable_xinerama")) {

		if (glXDisableXineramaSUN((Display *)dpy)) {
		    jclass cls = (*env)->GetObjectClass(env, obj);
		    jfieldID disabledField =
			(*env)->GetFieldID(env, cls, "xineramaDisabled", "Z");

		    (*env)->SetBooleanField(env, obj, disabledField, JNI_TRUE);
		    DPRINT((stderr, "Successfully disabled Xinerama.\n"));
		}
		else {
		    DPRINT((stderr, "Failed to disable Xinerama:  "));
		    DPRINT((stderr, "glXDisableXineramaSUN returns false.\n"));
		}
	    } else {
		DPRINT((stderr, "Failed to disable Xinerama:  "));
		DPRINT((stderr, "GLX_SUN_disable_xinerama not available.\n"));
	    }
#endif /* GLX_SUN_disable_xinerama */
	} else {
	    DPRINT((stderr, "but Xinerama is not in use.\n"));
	}
    }
#endif /* SOLARIS && __sparc */

    return glIsMTSafe;
}


#ifdef WIN32
DWORD countBits(DWORD mask) 
{
    DWORD count = 0;
    int i;
    
    for (i=sizeof(DWORD)*8-1; i >=0 ; i--) {
	if ((mask & 0x01) > 0) {
	    count++;
	}
	mask >>= 1;
    }
    return count;
}

#endif /* WIN32 */

/*
 * Class:     javax_media_j3d_MasterControl
 * Method:    getNumberOfProcessor
 * Signature: ()I
 *
 * This function get the number of active processor in the system
 */
JNIEXPORT jint JNICALL Java_javax_media_j3d_MasterControl_getNumberOfProcessor
  (JNIEnv *env, jobject obj)
{
#if defined(UNIX)
    return sysconf(_SC_NPROCESSORS_ONLN);
#endif /* UNIX_ */

#ifdef WIN32
    SYSTEM_INFO sysInfo;

    GetSystemInfo(&sysInfo);
    return countBits(sysInfo.dwActiveProcessorMask);
#endif /* WIN32 */
}


/*
 * Class:     javax_media_j3d_MasterControl
 * Method:    getThreadConcurrency
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_javax_media_j3d_MasterControl_getThreadConcurrency(JNIEnv *env,
							jobject obj)
{
    /*
     * Return the number of concurrent threads that can be run,
     * -1 if unknown.
     */

#ifdef SOLARIS
    return (jint) thr_getconcurrency();
#endif /* SOLARIS */

#ifdef LINUX
    return -1;
#endif /* LINUX */

#ifdef WIN32
    return -1;
#endif /* WIN32 */
}


/*
 * Class:     javax_media_j3d_MasterControl
 * Method:    setThreadConcurrency
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_javax_media_j3d_MasterControl_setThreadConcurrency(JNIEnv *env,
							jobject obj,
							jint newLevel)
{
#ifdef SOLARIS
    thr_setconcurrency((int)newLevel);
#endif /* SOLARIS */

#ifdef WIN32
    /* No-op on windows */
#endif /* WIN32 */

#ifdef LINUX
    /* No-op on linux */
#endif /* LINUX */
}



JNIEXPORT
jint JNICALL Java_javax_media_j3d_MasterControl_getMaximumLights(
    JNIEnv *env, 
    jobject obj
    ) {

#ifdef SOLARIS
    return 32;
#endif /* SOLARIS */

#ifdef WIN32
    return 8;
#endif /* WIN32 */

#ifdef LINUX
    return 8;
#endif /* LINUX */
}


/* ======================================================================= */

/*
 * The following method implements a high-resolution timer (based on the
 * native code in the J3DTimer class). It will no longer be needed once
 * we drop support for JDK 1.4.2, at which time it will be replaced by
 * a call to System.nanoTime().
 */

#define NSEC_PER_SEC ((jlong)1000000000)

#ifdef __linux__
#include <sys/time.h>
#include <time.h>
#include <unistd.h>
#endif

#ifdef SOLARIS
#include <time.h>
#include <sys/systeminfo.h>
#include <string.h>
#ifndef CLOCK_HIGHRES
#define CLOCK_HIGHRES 4			/* Solaris 7 does not define this */
#endif					/* constant. When run on Solaris 7 */
#endif					/* CLOCK_HIGHRES is not used. */

#ifdef WIN32
#include <Windows.h>
#include <math.h>
static double timerScale = -1.0;
#endif

/*
 * Class:     javax_media_j3d_MasterControl
 * Method:    getNativeTimerValue
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_javax_media_j3d_MasterControl_getNativeTimerValue(JNIEnv *env, jclass clazz)
{
    jlong timerNsec;

#ifdef SOLARIS
    /*
    struct timespec tp;
    clock_gettime( CLOCK_HIGHRES, &tp );

    return (jlong)tp.tv_nsec + (jlong)tp.tv_sec * NSEC_PER_SEC;
    */

    timerNsec = (jlong)gethrtime();
#endif /* SOLARIS */

#ifdef WIN32
    LARGE_INTEGER time;
    LARGE_INTEGER freq;

    if (timerScale < 0.0) {
	QueryPerformanceFrequency( &freq );
	if (freq.QuadPart <= 0) {
	    timerScale = 0.0;
	}
	else {
	    timerScale = (double) NSEC_PER_SEC / (double)freq.QuadPart;
	}
    }

    QueryPerformanceCounter(&time);
    timerNsec = (jlong)((double)time.QuadPart * timerScale);

#endif /* WIN32 */

#ifdef __linux__
    struct timeval t;

    gettimeofday(&t, 0);
    timerNsec = ((jlong)t.tv_sec) * NSEC_PER_SEC + ((jlong)t.tv_usec) * ((jlong)1000);
#endif /* __linux__ */

    return timerNsec;
}
