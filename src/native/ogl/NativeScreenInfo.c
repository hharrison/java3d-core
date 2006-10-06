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

#if defined(LINUX)
#define _GNU_SOURCE 1
#endif

#include <jni.h>
#include <math.h>
#include <stdio.h>

#include "gldefs.h"

#if defined(UNIX)
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <dlfcn.h>
#endif

#ifdef WIN32
#include <windows.h>
#endif

#if defined(UNIX)


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
    MYPFNGLXCHOOSEFBCONFIG tmpfp;
    int major, minor;
    int errorBase, eventBase;
    Display* dpy = (Display*)display;
    /* It should be cleaner to return both the major and minor to the caller. */

    if (!glXQueryExtension(dpy, &errorBase, &eventBase)) {
	fprintf(stderr, "Java 3D ERROR : GLX extension is not supported\n");
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }

    /* Query the GLX version number */
    if (!glXQueryVersion(dpy, &major, &minor)) {
	fprintf(stderr, "Java 3D ERROR : Unable to query GLX version\n");
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }
    /*fprintf(stderr, "Checking GLX version : %d.%d\n", major, minor);*/

    tmpfp = (MYPFNGLXCHOOSEFBCONFIG)dlsym(RTLD_DEFAULT, "glXChooseFBConfig");

    if (tmpfp == NULL) {
	fprintf(stderr, "Java 3D ERROR : glXChooseFBConfig not found\n");
	fprintf(stderr, "    GLX version = %d.%d\n", major, minor);
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");
	return JNI_FALSE;
    }

    /* Check for GLX 1.3 and higher */
    if (!(major == 1 && minor >= 3)) {
	fprintf(stderr, "Java 3D WARNING : reported GLX version = %d.%d\n", major, minor);
	fprintf(stderr, "    GLX version 1.3 or higher is required\n");

	fprintf(stderr,
		"    The reported version number may be incorrect.  There is a known\n");
	fprintf(stderr,
		"    ATI driver bug in glXQueryVersion that incorrectly reports the GLX\n");
	fprintf(stderr,
		"    version as 1.2 when it really is 1.3, so Java 3D will attempt to\n");
	fprintf(stderr,
		"    run anyway.\n");

	/*return JNI_FALSE;*/
    }

    return JNI_TRUE;
}

#endif /* Solaris and Linux */


#ifdef WIN32

extern HWND createDummyWindow(const char* szAppName);
extern void printErrorMessage(char *message);
extern PIXELFORMATDESCRIPTOR getDummyPFD();
extern BOOL isSupportedWGL(const char *extensions, const char *extension_string);    

/*
 * Class:     javax_media_j3d_NativeScreenInfo
 * Method:    queryWglARB
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_NativeScreenInfo_queryWglARB(
    JNIEnv *env,
    jclass cls)
{

    static const BOOL debug = TRUE;
    static char szAppName[] = "Choose Pixel Format";

    PIXELFORMATDESCRIPTOR dummy_pfd = getDummyPFD();    
    HWND hwnd;
    HGLRC hrc;
    HDC   hdc;
    int pixelFormat;
    const char* supportedExtensions;
    
    /* declare function pointers for WGL functions */
    PFNWGLGETEXTENSIONSSTRINGARBPROC  wglGetExtensionsStringARB = NULL;
    
    /*
     * Select any pixel format and bound current context to
     * it so that we can get the wglChoosePixelFormatARB entry point.
     * Otherwise wglxxx entry point will always return null.
     * That's why we need to create a dummy window also.
     */
    hwnd = createDummyWindow((const char *)szAppName);

    if (!hwnd) {
	return JNI_FALSE;
    }
    hdc = GetDC(hwnd);

    pixelFormat = ChoosePixelFormat(hdc, &dummy_pfd);
    
    if (pixelFormat<1) {
	printErrorMessage("Failed in ChoosePixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return JNI_FALSE;
    }

    if (!SetPixelFormat(hdc, pixelFormat, NULL)) {
 	printErrorMessage("Failed in SetPixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;    
    }
    
    hrc = wglCreateContext(hdc);
    if (!hrc) {
	printErrorMessage("Failed in wglCreateContext");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return JNI_FALSE;
    }
    
    if (!wglMakeCurrent(hdc, hrc)) {
	printErrorMessage("Failed in wglMakeCurrent");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return JNI_FALSE;
    }

    wglGetExtensionsStringARB = (PFNWGLGETEXTENSIONSSTRINGARBPROC)
	wglGetProcAddress("wglGetExtensionsStringARB");
    if (wglGetExtensionsStringARB == NULL) {
	/* printErrorMessage("wglGetExtensionsStringARB not support !\n"); */
	return JNI_FALSE;
    }
    else {

	/* get the list of supported extensions */
	supportedExtensions = (const char *)wglGetExtensionsStringARB(hdc);    
	
	if (debug) {
	    fprintf(stderr, "WGL Supported extensions: %s.\n", supportedExtensions);    
	}

	if(!isSupportedWGL(supportedExtensions, "WGL_ARB_pixel_format")) {
	    fprintf(stderr, "WGL_ARB_pixel_format not supported.\n");
	    return JNI_FALSE;
	}
    }
    
    ReleaseDC(hwnd, hdc);
    wglDeleteContext(hrc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);
    return JNI_TRUE;

    
}
#endif /*  WIN32 */
