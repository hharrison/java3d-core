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

#if defined(SOLARIS) || defined(__linux__)
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

#pragma weak glXInitThreadsSUN
#pragma weak glXDisableXineramaSUN
#pragma weak XPanoramiXQueryExtension

#ifdef SOLARIS
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
#endif /* SOLARIS */
#endif /* SOLARIS || __linux__ */

/* defined in Canvas3D.c */
extern int isExtensionSupported(const char *allExtensions,
				const char *extension); 

JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_MasterControl_initializeJ3D(
    JNIEnv *env, jobject obj, jboolean disableXinerama)
{
    jboolean glIsMTSafe = JNI_FALSE;

#ifdef WIN32
    glIsMTSafe = JNI_TRUE;
    return glIsMTSafe;
#endif /* WIN32 */

#ifdef __linux__
    glIsMTSafe = JNI_TRUE;
    return glIsMTSafe;
#endif /* __linux__ */

#ifdef SOLARIS
    Display* dpy;
    int event_base, error_base;
    const char *glxExtStr = NULL;
    
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
#endif /* SOLARIS */

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
#if defined(SOLARIS) || defined(__linux__)
    return sysconf(_SC_NPROCESSORS_ONLN);
#endif /* SOLARIS || __linux__ */

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

#ifdef __linux__
    /* No-op on linux */
#endif /* __linux__ */
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

#ifdef __linux__
    return 8;
#endif /* __linux__ */
}
