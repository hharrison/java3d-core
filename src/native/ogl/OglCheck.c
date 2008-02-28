/*
 * $RCSfile$
 *
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision$
 * $Date$
 * $State$
 */

#ifdef DEBUG
/* #define VERBOSE */
#endif /* DEBUG */

/* This entire file is Windows-only */
#ifdef WIN32

/* j3dsys.h needs to be included before any other include files to suppres VC warning */
#include "j3dsys.h"

#include <jni.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>

#include <GL/gl.h>
#include "wglext.h"
#include "javax_media_j3d_NativePipeline.h"


static void
printErrorMessage(char *message) 
{
    DWORD err;
    char * errString;

    err = GetLastError();
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		  FORMAT_MESSAGE_FROM_SYSTEM,
		  NULL, err, 0, (LPTSTR)&errString, 0, NULL);    
    fprintf(stderr, "Java 3D ERROR : %s - %s\n", message, errString);
    LocalFree(errString);
}


/*
 * A dummy WndProc for dummy window
 */
static LONG WINAPI
WndProc(HWND hWnd, UINT msg, WPARAM wParam, LPARAM lParam)
{
    /* This function handles any messages that we didn't. */
    /* (Which is most messages) It belongs to the OS. */
    return (LONG) DefWindowProc( hWnd, msg, wParam, lParam );
}


static HWND
createDummyWindow(const char* szAppName)
{
    static const char *szTitle = "Dummy Window";
    WNDCLASS wc;   /* windows class sruct */
   
    HWND hWnd; 

    /* Fill in window class structure with parameters that */
    /*  describe the main window. */

    wc.style         =
	CS_HREDRAW | CS_VREDRAW;/* Class style(s). */
    wc.lpfnWndProc   = 
	(WNDPROC)WndProc;      /* Window Procedure */
    wc.cbClsExtra    = 0;     /* No per-class extra data. */
    wc.cbWndExtra    = 0;     /* No per-window extra data. */
    wc.hInstance     =
	NULL;            /* Owner of this class */
    wc.hIcon         = NULL;  /* Icon name */
    wc.hCursor       =
	NULL;/* Cursor */
    wc.hbrBackground = 
	(HBRUSH)(COLOR_WINDOW+1);/* Default color */
    wc.lpszMenuName  = NULL;  /* Menu from .RC */
    wc.lpszClassName =
	szAppName;            /* Name to register as

				 /* Register the window class */
    
    if(RegisterClass( &wc )==0) {
	printErrorMessage("createDummyWindow: couldn't register class");
	return NULL;
    }
  
    /* Create a main window for this application instance. */

    hWnd = CreateWindow(
			szAppName, /* app name */
			szTitle,   /* Text for window title bar */
			WS_OVERLAPPEDWINDOW/* Window style */
			/* NEED THESE for OpenGL calls to work!*/
			| WS_CLIPCHILDREN | WS_CLIPSIBLINGS,
			CW_USEDEFAULT, 0, CW_USEDEFAULT, 0,
			NULL,     /* no parent window */
			NULL,     /* Use the window class menu.*/
			NULL,     /* This instance owns this window */
			NULL      /* We don't use any extra data */
			);

    /* If window could not be created, return zero */
    if ( !hWnd ){
	printErrorMessage("createDummyWindow: couldn't create window");
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return NULL;
    }
    return hWnd; 
}


static PIXELFORMATDESCRIPTOR
getDummyPFD()
{
    
    /*  Dummy pixel format.  -- Chien */
    static PIXELFORMATDESCRIPTOR dummy_pfd = {
	sizeof(PIXELFORMATDESCRIPTOR),
	1,                      /* Version number */
	PFD_DRAW_TO_WINDOW |
	PFD_SUPPORT_OPENGL,
	PFD_TYPE_RGBA,
	16,                     /* 16 bit color depth */
	0, 0, 0,                /* RGB bits and pixel sizes */
	0, 0, 0,                /* Do not care about them */
	0, 0,                   /* no alpha buffer info */
	0, 0, 0, 0, 0,          /* no accumulation buffer */
	8,                      /* 8 bit depth buffer */
	0,                      /* no stencil buffer */
	0,                      /* no auxiliary buffers */
	PFD_MAIN_PLANE,         /* layer type */
	0,                      /* reserved, must be 0 */
	0,                      /* no layer mask */
	0,                      /* no visible mask */
	0                       /* no damage mask */
    };

    return dummy_pfd;
}


static BOOL
isSupportedWGL(const char *extensions, const char *extension_string)
{    
    /* get the list of supported extensions */
    const char *p = extensions; 

    /* search for extension_string in the list */
    while(p = strstr(p, extension_string)){
	const char *q = p + strlen(extension_string);

	/* must be terminated by <space> or <nul> */
	if(*q == ' ' || *q == '\0') {
	    return TRUE;
	}

	/* try to find another match */
	p = q;
    }
    return FALSE;
}


/*
static HDC
getMonitorDC(int screen)
{
    return CreateDC("DISPLAY", NULL, NULL, NULL);     
}
*/


/*
 * Extract the version numbers from a copy of the version string.
 * Upon return, numbers[0] contains major version number
 * numbers[1] contains minor version number
 * Note that the passed in version string is modified.
 */
static void
extractVersionInfo(char *versionStr, int* numbers)
{
    char *majorNumStr;
    char *minorNumStr;

    numbers[0] = numbers[1] = -1;
    majorNumStr = strtok(versionStr, (char *)".");
    minorNumStr = strtok(0, (char *)".");
    if (majorNumStr != NULL)
	numbers[0] = atoi(majorNumStr);
    if (minorNumStr != NULL)
	numbers[1] = atoi(minorNumStr);
}


/*
 * get properties from current context
 */
static char*
queryVendorString(HDC hdc, HGLRC hrc)
{
    char *glVersion;
    char *tmpVersionStr;
    int  versionNumbers[2];
    char *glVendor;
    char *supportedExtensions;
    PFNWGLGETEXTENSIONSSTRINGARBPROC wglGetExtensionsStringARB;

    if (!wglMakeCurrent(hdc, hrc)) {
#ifdef DEBUG
	printErrorMessage("getSupportedOglVendorNative : Failed in wglMakeCurrent");
#endif /* DEBUG */
	return NULL;
    }

    wglGetExtensionsStringARB = (PFNWGLGETEXTENSIONSSTRINGARBPROC)
            wglGetProcAddress("wglGetExtensionsStringARB");
    if (wglGetExtensionsStringARB == NULL) {
#ifdef DEBUG
	printErrorMessage("getSupportedOglVendorNative : wglGetExtensionsStringARB not supported\n");
#endif /* DEBUG */
	return NULL;
    }

    /* get the list of supported extensions */
    supportedExtensions = (char *)wglGetExtensionsStringARB(hdc);

#ifdef VERBOSE
    fprintf(stderr, "WGL Supported extensions: %s\n",
       supportedExtensions);
#endif /* VERBOSE */

    if (supportedExtensions == NULL ||
            !isSupportedWGL(supportedExtensions, "WGL_ARB_pixel_format") ||
            wglGetProcAddress("wglChoosePixelFormatARB") == NULL ||
            wglGetProcAddress("wglGetPixelFormatAttribivARB") == NULL) {
#ifdef DEBUG
	printErrorMessage("getSupportedOglVendorNative : wglChoosePixelFormatARB/GetPixelFormatAttribivARB not supported\n");
#endif /* DEBUG */
	return NULL;
    }

    /* Get the OpenGL version */
    glVersion = (char *)glGetString(GL_VERSION);
    if (glVersion == NULL) {
#ifdef DEBUG
	fprintf(stderr, "JAVA 3D ERROR : glVersion == null\n");
#endif /* DEBUG */
	return NULL;
    }

    /* find out the version, major and minor version number */
    tmpVersionStr = strdup(glVersion);
    extractVersionInfo(tmpVersionStr, versionNumbers);
    free(tmpVersionStr);

#ifdef VERBOSE
    fprintf(stderr, "GL_VERSION string = %s\n", glVersion);
    fprintf(stderr, "GL_VERSION (major.minor) = %d.%d\n",
            versionNumbers[0], versionNumbers[1]);
#endif /* VERBOSE */

    /*
     * Check for OpenGL 1.2 or later.
     */
    if (versionNumbers[0] < 1 ||
            (versionNumbers[0] == 1 && versionNumbers[1] < 2)) {
#ifdef DEBUG
	fprintf(stderr,
		"Java 3D ERROR : OpenGL 1.2 or better is required (GL_VERSION=%d.%d)\n",
		versionNumbers[0], versionNumbers[1]);
#endif /* DEBUG */
	return NULL;
    }

    /* Get the OpenGL vendor */
    glVendor = (char *)glGetString(GL_VENDOR);
    if (glVendor == NULL) {
#ifdef DEBUG
	fprintf(stderr, "JAVA 3D ERROR : glVendor == null\n");
#endif /* DEBUG */
	return NULL;
    }

#ifdef VERBOSE
    fprintf(stderr, "GL_VENDOR = %s\n", glVendor);
#endif /* VERBOSE */

    return glVendor;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    getSupportedOglVendorNative
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_javax_media_j3d_NativePipeline_getSupportedOglVendorNative(
    JNIEnv *env,
    jclass clazz)
{
    static char szAppName[] = "OglCheck";

    static int wglAttrs[] = {
        WGL_SUPPORT_OPENGL_ARB,
        TRUE,
        WGL_ACCELERATION_ARB, 
        WGL_FULL_ACCELERATION_ARB,
        WGL_DRAW_TO_WINDOW_ARB, 
        TRUE,
        WGL_RED_BITS_ARB,
        4,
        WGL_GREEN_BITS_ARB,
        4,
        WGL_BLUE_BITS_ARB,
        4,
        WGL_DEPTH_BITS_ARB,
        16,
    };

    HWND  hwnd;
    HGLRC hrc;
    HDC   hdc;
    int pixelFormat;
    PIXELFORMATDESCRIPTOR dummy_pfd = getDummyPFD();
    char *glVendor = NULL;
    jstring glVendorString = NULL;

    JNIEnv table = *env; 

#ifdef VERBOSE
    fprintf(stderr, "NativePipeline.getSupportedOglVendorNative()\n");
#endif /* VERBOSE */

    /*
     * Select any pixel format and bound current context to
     * it so that we can get the wglChoosePixelFormatARB entry point.
     * Otherwise wglxxx entry point will always return null.
     * That's why we need to create a dummy window also.
     */
    hwnd = createDummyWindow((const char *)szAppName);

    if (!hwnd) {
	return NULL;
    }
    hdc = GetDC(hwnd);

    pixelFormat = ChoosePixelFormat(hdc, &dummy_pfd);

    if (pixelFormat<1) {
#ifdef DEBUG
	printErrorMessage("getSupportedOglVendorNative : Failed in ChoosePixelFormat");
#endif /* DEBUG */
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return NULL;
    }

    if (!SetPixelFormat(hdc, pixelFormat, NULL)) {
#ifdef DEBUG
 	printErrorMessage("getSupportedOglVendorNative : Failed in SetPixelFormat");
#endif /* DEBUG */
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return NULL;    
    }

    hrc = wglCreateContext(hdc);
    if (!hrc) {
#ifdef DEBUG
	printErrorMessage("getSupportedOglVendorNative : Failed in wglCreateContext");
#endif /* DEBUG */
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return NULL;
    }

    /* Check OpenGL extensions & version, and return vendor string */
    glVendor = queryVendorString(hdc, hrc);
    if (glVendor != NULL) {
	glVendorString = table->NewStringUTF(env, glVendor);
    }

    /* Destroy all dummy objects */
    wglDeleteContext(hrc);
    ReleaseDC(hwnd, hdc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);

    return glVendorString;
}

#endif /* WIN32 */
