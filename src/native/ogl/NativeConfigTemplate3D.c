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
#include <stdlib.h>

#include "gldefs.h"

#if defined(SOLARIS) || defined(__linux__)
#include <X11/X.h>
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#endif

#ifdef WIN32
#include <windows.h>
#endif

/* check if the extension is supported  */
extern int isExtensionSupported(const char *allExtensions, const char *extension); 

#if defined(SOLARIS) || defined(__linux__)

/* Fix for issue 20 */
#define  MAX_GLX_ATTRS_LENGTH 25

GLXFBConfig *find_S_FBConfigs(jlong display,
			      jint screen,
			      int* glxAttrs,
			      int sVal, int sIndex) {

    GLXFBConfig *fbConfigList = NULL;
    int          numFBConfigs, index;
    
    J3D_ASSERT((sIndex+3) < MAX_GLX_ATTRS_LENGTH);
    
    if (sVal == REQUIRED || sVal== PREFERRED) {

	index = sIndex;
	glxAttrs[index++] = GLX_STEREO;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;
	
	fbConfigList = glXChooseFBConfig((Display*)display, screen,
					 glxAttrs, &numFBConfigs);
	    
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    
    if (sVal == UNNECESSARY || sVal== PREFERRED) {
	/* This is a workaround to BugId : 5106472 in Solaris OGL.
	   We can't set glxAttrs with GLX_STEREO follow by a boolean */

	index = sIndex;
	glxAttrs[index] = None;
	
	/* For debug only
	{   
	    int i=0;
	    fprintf(stderr, "find_S_FBConfigs sVal = %d\n", sVal);    

	    while(glxAttrs[i] != None) {
		fprintf(stderr, "glxAttrs[%d] = %x", i, glxAttrs[i]);
		i++;
		fprintf(stderr, " glxAttrs[%d] = %x\n", i, glxAttrs[i]);    
		i++;
	    }
	}
	*/
	fbConfigList = glXChooseFBConfig((Display*)display, screen,
					 glxAttrs, &numFBConfigs);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    if (sVal == UNNECESSARY) {
	index = sIndex;
	glxAttrs[index++] = GLX_STEREO;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;
	
	fbConfigList = glXChooseFBConfig((Display*)display, screen,
					 glxAttrs, &numFBConfigs);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    return NULL;
}

GLXFBConfig *find_AA_S_FBConfigs(jlong display,
				 jint screen,
				 int* glxAttrs,
				 int sVal, 
				 int antialiasVal, int antialiasIndex) {

    const char *glxExtensions = NULL;
    GLXFBConfig *fbConfigList = NULL;
    int index = antialiasIndex;


    J3D_ASSERT((antialiasIndex+7) < MAX_GLX_ATTRS_LENGTH);

    if(antialiasVal == REQUIRED || antialiasVal== PREFERRED) {
	glxExtensions = (const char *) glXGetClientString((Display*)display, GLX_EXTENSIONS);
	
	if(isExtensionSupported(glxExtensions, "GLX_ARB_multisample")){
	    
	    index = antialiasIndex;
	    glxAttrs[index++] = GLX_SAMPLE_BUFFERS_ARB;
	    glxAttrs[index++] = 1;
	    glxAttrs[index++] = GLX_SAMPLES_ARB;
	    glxAttrs[index++] = 1;
	    glxAttrs[index] = None;

	    fbConfigList = find_S_FBConfigs(display, screen, 
					    glxAttrs, sVal, index);
	    
	    if(fbConfigList != NULL) {
		return fbConfigList;
	    }
	}
    }
    
    if ( antialiasVal == REQUIRED ) {
	index = antialiasIndex;
        glxAttrs[index++] = GLX_ACCUM_RED_SIZE;
        glxAttrs[index++] = 8;
        glxAttrs[index++] = GLX_ACCUM_GREEN_SIZE;
        glxAttrs[index++] = 8;
        glxAttrs[index++] = GLX_ACCUM_BLUE_SIZE;
        glxAttrs[index++] = 8;
	glxAttrs[index] = None;
	
	fbConfigList = find_S_FBConfigs(display, screen, 
					glxAttrs, sVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    
    glxAttrs[antialiasIndex] = None;

    if (antialiasVal == UNNECESSARY || antialiasVal == PREFERRED) {
	fbConfigList = find_S_FBConfigs(display, screen, 
					glxAttrs, sVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    /* We will stop trying even if no fbConfigList is found and 
       antialiasVal = UNNECESSARY */

    return NULL;
    
}

GLXFBConfig *find_DB_AA_S_FBConfigs(jlong display,
				    jint screen,
				    int* glxAttrs,
				    int sVal, int dbVal,
				    int antialiasVal, int dbIndex) {

    GLXFBConfig *fbConfigList = NULL;
    int index = dbIndex;

    J3D_ASSERT((dbIndex+3) < MAX_GLX_ATTRS_LENGTH);

    if (dbVal == REQUIRED || dbVal== PREFERRED) {
	    
	    index = dbIndex;
	    glxAttrs[index++] = GLX_DOUBLEBUFFER;
            glxAttrs[index++] = True;
	    glxAttrs[index] = None;

	    fbConfigList = find_AA_S_FBConfigs(display, screen, 
					       glxAttrs, sVal, 
					       antialiasVal, index);
	    
	    if(fbConfigList != NULL) {
		return fbConfigList;
	    }
    }
    
    if (dbVal == UNNECESSARY || dbVal== PREFERRED) {
	index = dbIndex;
	glxAttrs[index++] = GLX_DOUBLEBUFFER;
	glxAttrs[index++] = False;
	glxAttrs[index] = None;

	fbConfigList = find_AA_S_FBConfigs(display, screen, 
					   glxAttrs, sVal, 
					   antialiasVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    if (dbVal == UNNECESSARY) {
	index = dbIndex;
	glxAttrs[index++] = GLX_DOUBLEBUFFER;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;

	fbConfigList = find_AA_S_FBConfigs(display, screen, 
					   glxAttrs, sVal, 
					   antialiasVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    return NULL;
}

/*
 * Uses the passed in array to choose the best OpenGL visual.
 * When the "best" visual cannot be used, the "enums" (three
 * state attributes) are looped through setting/resetting in all
 * combinations in hopes of finding an valid visual.
 */
JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_chooseOglVisual(
    JNIEnv    *env,
    jobject    obj,
    jlong      display,
    jint       screen,
    jintArray  attrList,
    jlongArray fbConfigArray)
{
    jint *mx_ptr;
    int   glxAttrs[MAX_GLX_ATTRS_LENGTH];  /* value, attr pair plus a None */
    int   index;
    GLXFBConfig *fbConfigList = NULL;

    /* use to cycle through when attr is not REQUIRED */
    int sVal;
    int dbVal;    
    int antialiasVal;

    int drawableIndex;
    
    jlong *fbConfigListPtr = NULL;
    int status, major, minor;    

    Display *dpy = (Display *) display;
    
    fbConfigListPtr = (*env)->GetLongArrayElements(env, fbConfigArray, NULL);
    mx_ptr = (jint *)(*env)->GetPrimitiveArrayCritical(env, attrList, NULL);

    /*
     * convert Java 3D values to GLX
     */
    index = 0;

    /* Specify pbuffer as default */    
    /* Fix for Issue 20 */
    glxAttrs[index++] = GLX_DRAWABLE_TYPE;
    drawableIndex = index;
    glxAttrs[index++] = (GLX_PBUFFER_BIT | GLX_WINDOW_BIT);
    
    /* only interested in RGBA type */
    glxAttrs[index++] = GLX_RENDER_TYPE;
    glxAttrs[index++] = GLX_RGBA_BIT; 

    /* only interested in FBConfig with associated X Visual type */
    glxAttrs[index++] = GLX_X_RENDERABLE;
    glxAttrs[index++] = True;
    
    glxAttrs[index++] = GLX_RED_SIZE;
    glxAttrs[index++] = mx_ptr[RED_SIZE];
    glxAttrs[index++] = GLX_GREEN_SIZE;
    glxAttrs[index++] = mx_ptr[GREEN_SIZE];
    glxAttrs[index++] = GLX_BLUE_SIZE;
    glxAttrs[index++] = mx_ptr[BLUE_SIZE];
    glxAttrs[index++] = GLX_DEPTH_SIZE;
    glxAttrs[index++] = mx_ptr[DEPTH_SIZE];
    glxAttrs[index] = None;
    
    dbVal = mx_ptr[DOUBLEBUFFER];
    sVal = mx_ptr[STEREO];
    antialiasVal = mx_ptr[ANTIALIASING]; 


    (*env)->ReleasePrimitiveArrayCritical(env, attrList, mx_ptr, 0);

    fbConfigList = find_DB_AA_S_FBConfigs(display, screen, glxAttrs, sVal,
					  dbVal, antialiasVal, index);
    
    if(fbConfigList == NULL) { //  Try with Pixmap, if Pbuffer fail. */
	
	glxAttrs[drawableIndex] = (GLX_PIXMAP_BIT | GLX_WINDOW_BIT);
	
	fbConfigList = find_DB_AA_S_FBConfigs(display, screen, glxAttrs, sVal,
					      dbVal, antialiasVal, index);
	
    }
    
    if(fbConfigList == NULL) { // Try with Window only, if Pixmap fail.
	glxAttrs[drawableIndex] =  GLX_WINDOW_BIT;
	
	fbConfigList = find_DB_AA_S_FBConfigs(display, screen, glxAttrs, sVal,
					      dbVal, antialiasVal, index);
	
    }
    
    fbConfigListPtr[0] = (jlong)fbConfigList;
    (*env)->ReleaseLongArrayElements(env, fbConfigArray, fbConfigListPtr, 0);

    /* For debug only.
       if(fbConfigList != NULL) {
       int val;
       
       glXGetFBConfigAttrib(dpy, fbConfigList[0], 
       GLX_FBCONFIG_ID, &val);
       
       fprintf(stderr, "display 0x%x, fbConfigList 0x%x, fbConfig 0x%x, fbConfigId %d\n",
       (int) display, (int) fbConfigList, (int) fbConfigList[0], val);
       
       }
       else {
       fprintf(stderr, "display 0x%x, fbConfigList 0x%x\n",
       (int) display, (int) fbConfigList);
       }
    */
    
    if(fbConfigList != NULL) {
	int vis_id;
	
	if(glXGetFBConfigAttrib(dpy, fbConfigList[0], GLX_VISUAL_ID, &vis_id) != Success) {
	    fprintf(stderr, "Java 3D ERROR: unable to get VisualID\n");
	    return 0;
	}

	/* fprintf(stderr, "********* VisualID = %d\n", vis_id ); */
	
	return (jint) vis_id;
	
    } else {
	return 0; 
    }
}    


JNIEXPORT 
void JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_freeFBConfig(
     JNIEnv *env,
     jclass  class,	/* this is a static native method */
     jlong   fbConfigListPtr)
{
    GLXFBConfig *fbConfigList = (GLXFBConfig *) fbConfigListPtr;
    XFree(fbConfigList);
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isStereoAvailable(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint screen,
    jint vid)
{
    Display *dpy = (Display*) display;
    XVisualInfo *vinfo, template;
    int nitems;
    int stereoFlag;
    static GLboolean first_time = GL_TRUE;
    static GLboolean force_no_stereo = GL_FALSE;
 
    if (first_time) {
	if (getenv("J3D_NO_STEREO") != NULL) {
	    fprintf(stderr, "Java 3D: stereo mode disabled\n");
	    force_no_stereo = GL_TRUE;
	}
	first_time = GL_FALSE;
    }

    if (force_no_stereo)
	return JNI_FALSE;

    template.visualid = vid;
    vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_isStereoAvailable got unexpected number of matching visuals %d\n", nitems);
    }

    glXGetConfig(dpy, vinfo, GLX_STEREO, &stereoFlag);

    return (stereoFlag ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isDoubleBufferAvailable(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint screen,
    jint vid)
{
    Display *dpy = (Display*) display;
    XVisualInfo *vinfo, template;
    int nitems;
    int doubleBufferFlag;

    template.visualid = vid;
    vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_isDoubleBufferAvailable got unexpected number of matching visuals %d\n", nitems);
    }

    glXGetConfig(dpy, vinfo, GLX_DOUBLEBUFFER, &doubleBufferFlag);

    return (doubleBufferFlag ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingAccumAvailable(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint screen,
    jint vid)
{
    Display *dpy = (Display*) display;
    XVisualInfo *vinfo, template;
    int nitems;
    int numAccumRedBits;
	
    template.visualid = vid;
    vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_isSceneAntialiasingAvailable got unexpected number of matching visuals %d\n", nitems);
    }

    glXGetConfig(dpy, vinfo, GLX_ACCUM_RED_SIZE, &numAccumRedBits);

    return (numAccumRedBits > 0 ? JNI_TRUE : JNI_FALSE);
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultiSamplesAvailable(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint screen,
    jint vid)
{
    Display *dpy = (Display*) display;
    XVisualInfo *vinfo, template;
    int nitems;
    
    const char *glxExtensions;
    int numSampleBuffers;
    int numSamples;
	
    template.visualid = vid;
    vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_isSceneAntialiasingAvailable got unexpected number of matching visuals %d\n", nitems);
    }
    /* try GLX_ARB_multisample */
    glxExtensions = (const char *)glXGetClientString((Display*)display, GLX_EXTENSIONS);
    
    if(isExtensionSupported(glxExtensions, "GLX_ARB_multisample")){
	glXGetConfig(dpy, vinfo, GLX_SAMPLE_BUFFERS_ARB, &numSampleBuffers);
	glXGetConfig(dpy, vinfo, GLX_SAMPLES_ARB, &numSamples);
	if(numSampleBuffers > 0 && numSamples > 1){
	    return JNI_TRUE;
	}
    }

    return JNI_FALSE;
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_J3dGraphicsConfig_isValidVisualID(
    JNIEnv *env,
    jclass cls,
    jlong display,
    jint vid)
{
   XVisualInfo template;
   int nitems;
   
   template.visualid = vid;
   XGetVisualInfo((Display *)display, VisualIDMask, &template, &nitems);
   return (nitems == 1);

}
#endif /* SOLARIS || __linux__ */


#ifdef WIN32

extern HWND createDummyWindow(const char* szAppName);

/*
void printPixelDescriptor(PIXELFORMATDESCRIPTOR *pfd)
{

    printf("color : r=%d, g=%d, b=%d, a=%d, shift r=%d, g=%d, b=%d, a=%d\n",
	   pfd->cRedBits, pfd->cGreenBits, pfd->cBlueBits, pfd->cAlphaBits,
	   pfd->cRedShift, pfd->cGreenShift, pfd->cBlueShift, pfd->cAlphaShift); 
    printf("Accum r=%d, g=%d, b=%d, a=%d, depth %d, stencil %d, AuxBuffers %d\n",
	   pfd->cAccumRedBits, pfd->cAccumGreenBits, pfd->cAccumBlueBits,
	   pfd->cAccumAlphaBits, pfd->cDepthBits, pfd->cStencilBits, pfd->cAuxBuffers);
    printf("iLayerType %x, bReserved %x, dwLayerMask %x, dwVisibleMask %x, dwDamageMask %x\n",
	   pfd->iLayerType, pfd->bReserved, pfd->dwLayerMask, pfd->dwVisibleMask, pfd->dwDamageMask);
    if (pfd->dwFlags & PFD_SUPPORT_OPENGL) {
	printf("SUPPORT_OPENGL ");
    }
    if (pfd->dwFlags & PFD_DRAW_TO_WINDOW) {
	printf("DRAW_TO_WINDOW ");
    }
    if (pfd->dwFlags & PFD_DRAW_TO_BITMAP) {
	printf("DRAW_TO_BITMAP ");
    }
    if (pfd->dwFlags & PFD_SUPPORT_GDI) {
	printf("SUPPORT_GDI ");
    }
    if (pfd->dwFlags & PFD_SUPPORT_GDI) {
	printf("NEED_PALETTE ");
    }
    if (pfd->dwFlags & PFD_SUPPORT_GDI) {
	printf("NEED_SYSTEM_PALETTE ");
    }
    if (pfd->dwFlags & PFD_STEREO) {
	printf("STEREO ");
    }
    if (pfd->dwFlags & PFD_SUPPORT_GDI) {
	printf("SWAP_LAYER_BUFFERS ");
    }
    if (pfd->dwFlags & PFD_GENERIC_FORMAT) {
	printf("PFD_GENERIC_FORMAT ");		    
    }
    if (pfd->dwFlags & PFD_GENERIC_ACCELERATED) {
	printf("PFD_GENERIC_FORMAT ");		    
    }
    if (pfd->dwFlags & PFD_DOUBLEBUFFER) {
	printf("PFD_DOUBLEBUFFER ");		    	
    }
    printf("\n");

}

*/

BOOL isSupportedWGL(const char * extensions, const char *extension_string) {    
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

HDC getMonitorDC(int screen)
{
    return CreateDC("DISPLAY", NULL, NULL, NULL);     
}

int findPixelFormatSwitchDoubleBufferAndStereo (PIXELFORMATDESCRIPTOR* pfd, HDC hdc, int *mx_ptr)
{

    int pf;
    
    pf = ChoosePixelFormat(hdc, pfd);

    /* Check if pixel format support min. requirement */
    DescribePixelFormat(hdc, pf, sizeof(*pfd), pfd);
    
    if ((pfd->cRedBits < (unsigned char) mx_ptr[RED_SIZE]) || 
	(pfd->cGreenBits < (unsigned char) mx_ptr[GREEN_SIZE]) ||
	(pfd->cBlueBits  < (unsigned char) mx_ptr[BLUE_SIZE]) ||
	(pfd->cDepthBits < (unsigned char) mx_ptr[DEPTH_SIZE]) ||
	((mx_ptr[DOUBLEBUFFER] == REQUIRED) && ((pfd->dwFlags & PFD_DOUBLEBUFFER) == 0)) ||
	((mx_ptr[STEREO] == REQUIRED) && ((pfd->dwFlags & PFD_STEREO) == 0)))
	{
	    return -1;
	}
    
    if ((mx_ptr[ANTIALIASING] == REQUIRED) &&
	((pfd->cAccumRedBits <= 0) ||
	 (pfd->cAccumGreenBits <= 0) ||
	 (pfd->cAccumBlueBits  <= 0)))
	{
	    return -1;
	}
    return pf; 
}


void printErrorMessage(char *message) 
{
    DWORD err;
    char * errString;

    err = GetLastError();
    FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		  FORMAT_MESSAGE_FROM_SYSTEM,
		  NULL, err, 0, (LPTSTR)&errString, 0, NULL);    
    fprintf(stderr, "%s - %s\n", message, errString);
    LocalFree(errString);
}

/* Prefer multiSample in following order
   4, 5, 6, 3, 7, 8, 2, 9, 10, 11, ...
*/
int getMultiSampleScore(int s)
{
    static int multiSampleScore[9] = {9999, 9999, 6, 3, 0, 1, 2, 4, 5};

    if (s < 9) {
	return multiSampleScore[s];
    }
    return s-2;
}


/* Max no of format wglChoosePixelFormatEXT can return  */
#define NFORMAT 100

int getExtPixelFormat(int *nativeConfigAttrs)
{
    static const BOOL debug = FALSE;
    static char szAppName[] = "Choose Pixel Format";

    static PIXELFORMATDESCRIPTOR pfd = {
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

    HWND hwnd;
    HGLRC hrc;
    HDC   hdc;
    int attr[22];
    int piValues[12];
    int i, idx;
    int pNumFormats[NFORMAT], nAvailableFormat;
    const char* supportedExtensions;
    int score;
    int highestScore, highestScorePF;
    int highestScoreAlpha, lowestScoreMultiSample;

    /* declare function pointers for WGL functions */
    PFNWGLGETEXTENSIONSSTRINGEXTPROC  wglGetExtensionsStringEXT = NULL;
    PFNWGLCHOOSEPIXELFORMATEXTPROC wglChoosePixelFormatEXT = NULL;
    PFNWGLGETPIXELFORMATATTRIBIVEXTPROC wglGetPixelFormatAttribivEXT = NULL;    

    /*
     * Select any pixel format and bound current context to
     * it so that we can get the wglChoosePixelFormatARB entry point.
     * Otherwise wglxxx entry point will always return null.
     * That's why we need to create a dummy window also.
     */
    hwnd = createDummyWindow((const char *)szAppName);

    if (!hwnd) {
	return -1;
    }
    hdc = GetDC(hwnd);

    pNumFormats[0] = ChoosePixelFormat(hdc, &pfd);
    if (!pNumFormats[0]) {
	printErrorMessage("Failed in ChoosePixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    SetPixelFormat(hdc, pNumFormats[0], &pfd);
    
    hrc = wglCreateContext(hdc);
    if (!hrc) {
	printErrorMessage("Failed in wglCreateContext");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }
    
    if (!wglMakeCurrent(hdc, hrc)) {
	printErrorMessage("Failed in wglMakeCurrent");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    wglGetExtensionsStringEXT = (PFNWGLGETEXTENSIONSSTRINGEXTPROC)
	wglGetProcAddress("wglGetExtensionsStringARB");

    if (wglGetExtensionsStringEXT == NULL) {
	wglGetExtensionsStringEXT = (PFNWGLGETEXTENSIONSSTRINGEXTPROC)
	    wglGetProcAddress("wglGetExtensionsStringEXT");
	if (wglGetExtensionsStringEXT == NULL) {
	    if (debug) {
		printf("wglGetExtensionsStringEXT/ARB not support !\n");
	    }
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);	    
	    return -1;
	}
	if (debug) {
	    printf("Support wglGetExtensionsStringEXT\n");
	}
    } else {
	if (debug) {	
	    printf("Support wglGetExtensionsStringARB\n");
	}
    }
    
    /* get the list of supported extensions */
    supportedExtensions = (const char *)wglGetExtensionsStringEXT(hdc);    

    if (debug) {
	fprintf(stderr, "WGL Supported extensions: %s.\n", supportedExtensions);    
    }
    
    if (!isSupportedWGL(supportedExtensions, "WGL_ARB_multisample") &&
	!isSupportedWGL(supportedExtensions, "WGL_EXT_multisample") &&
	!isSupportedWGL(supportedExtensions, "WGL_SGIS_multisample")) {	

	/* Under Wildcat III it doesn't use wglGetExtensionString */
	supportedExtensions = (char *) glGetString(GL_EXTENSIONS);

	if (debug) {
	    fprintf(stderr, "GL Supported extensions: %s.\n", supportedExtensions);    
	}
	
	if (!isSupportedWGL(supportedExtensions, "GL_ARB_multisample") &&
	    !isSupportedWGL(supportedExtensions, "GL_EXT_multisample") &&
	    !isSupportedWGL(supportedExtensions, "GL_SGIS_multisample")) {	    
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);	    
	    return -1;
	}
    }
    
    wglChoosePixelFormatEXT = (PFNWGLCHOOSEPIXELFORMATEXTPROC)
	wglGetProcAddress("wglChoosePixelFormatARB");
    
    if (wglChoosePixelFormatEXT == NULL) {
	wglChoosePixelFormatEXT = (PFNWGLCHOOSEPIXELFORMATEXTPROC)
	    wglGetProcAddress("wglChoosePixelFormatEXT");
	if (wglChoosePixelFormatEXT == NULL) {
	    if (debug) {
		printf("wglChoosePixelFormatARB/EXT not support !\n");
	    }
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);	    	    
	    return -1;
	} 
	if (debug) {
	    printf("Support wglChoosePixelFormatEXT\n");
	}
    } else {
	if (debug) {
	    printf("Support wglChoosePixelFormatARB\n");
	}	
    }
    
    idx = 0;
    attr[idx++] = WGL_SUPPORT_OPENGL_EXT;
    attr[idx++] = TRUE;
    attr[idx++] = WGL_DRAW_TO_WINDOW_EXT; 
    attr[idx++] = TRUE;
    attr[idx++] = WGL_RED_BITS_EXT;
    attr[idx++] = nativeConfigAttrs[RED_SIZE];
    attr[idx++] = WGL_GREEN_BITS_EXT;
    attr[idx++] = nativeConfigAttrs[GREEN_SIZE];
    attr[idx++] = WGL_BLUE_BITS_EXT;
    attr[idx++] = nativeConfigAttrs[BLUE_SIZE];
    attr[idx++] = WGL_DEPTH_BITS_EXT;
    attr[idx++] = nativeConfigAttrs[DEPTH_SIZE];

    if (nativeConfigAttrs[DOUBLEBUFFER] == REQUIRED) {
	attr[idx++] = WGL_DOUBLE_BUFFER_EXT;
	attr[idx++] = TRUE;
    }
    if (nativeConfigAttrs[STEREO] == REQUIRED) {    
	attr[idx++] = WGL_STEREO_EXT;
	attr[idx++] = TRUE;
    }

    if (nativeConfigAttrs[ANTIALIASING] == REQUIRED) {        
	attr[idx++] = WGL_SAMPLE_BUFFERS_ARB;
	attr[idx++] = TRUE;
	attr[idx++] = WGL_SAMPLES_ARB;
	attr[idx++] = 2;
    }

    /*
     * Terminate by 2 zeros to avoid driver bugs
     * that assume attributes always come in pairs.
     */
    attr[idx++] = 0;
    attr[idx++] = 0;
	
    if (!wglChoosePixelFormatEXT(hdc, (const int *)attr, NULL, NFORMAT,
				 pNumFormats, &nAvailableFormat)) {
	printErrorMessage("Failed in wglChoosePixelFormatEXT");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);	    
	return -1;
    }
    
    if (debug) {
	printf("No. of available pixel format is: %d\n", nAvailableFormat);
    }

    if (nAvailableFormat <= 0) {
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);	    
	return -1;
    }
    
    wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	wglGetProcAddress("wglGetPixelFormatAttribivARB");

    if (wglGetPixelFormatAttribivEXT == NULL) {
	wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	    wglGetProcAddress("wglGetPixelFormatAttribivEXT");

	if (wglGetPixelFormatAttribivEXT == NULL) {
	    if (debug) {
		printf("wglGetPixelFormatAttribivEXT/ARB not support !\n");
	    }
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);	    	    
	    return -1;
	} 
	if (debug) {
	    printf("Support wglGetPixelFormatAttribivEXT\n");
	}
    } else {
	if (debug) {
	    printf("Support wglGetPixelFormatAttribivARB\n");
	}	
    }

    idx = 0;
    attr[idx++] = WGL_ACCELERATION_EXT;
    attr[idx++] = WGL_RED_BITS_EXT;
    attr[idx++] = WGL_GREEN_BITS_EXT;
    attr[idx++] = WGL_BLUE_BITS_EXT;
    attr[idx++] = WGL_ALPHA_BITS_EXT;    
    attr[idx++] = WGL_DEPTH_BITS_EXT;
    attr[idx++] = WGL_STENCIL_BITS_EXT;
    attr[idx++] = WGL_SAMPLE_BUFFERS_ARB;
    attr[idx++] = WGL_SAMPLES_ARB;
    attr[idx++] = WGL_DOUBLE_BUFFER_EXT;
    attr[idx++] = WGL_STEREO_EXT;
    attr[idx] = 0;

    /* Select the best pixel format based on score */
    highestScore = 0;
    highestScorePF = -1;
    highestScoreAlpha = 0;
    lowestScoreMultiSample = 9999;
    
    for (i=0; i < nAvailableFormat; i++) {
	if (!wglGetPixelFormatAttribivEXT(hdc, pNumFormats[i], 0, idx, attr, piValues)) {
	    printErrorMessage("Failed in wglGetPixelFormatAttribivEXT");
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);
	    return -1;
	}
	if (debug) {
	    printf("Format %d\n", pNumFormats[i]);

	    if (piValues[0] == WGL_FULL_ACCELERATION_EXT) {
		printf("WGL_FULL_ACCELERATION_EXT");
	    } else if (piValues[0] == WGL_GENERIC_ACCELERATION_EXT) {
		printf("WGL_GENERIC_ACCELERATION_EXT");		
	    } else {
		printf("WGL_NO_ACCELERATION_EXT");		
	    }

	    printf(" R %d, G %d, B %d, A %d, Depth %d, Stencil %d",
		   piValues[1], piValues[2], piValues[3], piValues[4],
		   piValues[5], piValues[6]);

	    if (piValues[7] == TRUE) {
		printf(" MultiSample %d", piValues[8]);
	    }

	    if (piValues[9] == TRUE) {
		printf(" DoubleBuffer");
	    }
	    
	    if (piValues[10] == TRUE) {
		printf(" Stereo");
	    }
	    printf("\n");
	}

	/* Red, Green, Blue are fixed under windows so they are not checked */
	score = 0;

	if (piValues[0] == WGL_FULL_ACCELERATION_EXT) {
	    score += 20000;
	} else if (piValues[0] == WGL_GENERIC_ACCELERATION_EXT) {
	    score += 10000;
	}
	if ((nativeConfigAttrs[DOUBLEBUFFER] == PREFERRED) &&
	    (piValues[9] == TRUE)) {
	    score += 5000;
	}
	if (piValues[4] > 0) { /* Alpha */
	    score += 2500;
	}
	if ((nativeConfigAttrs[STEREO] == PREFERRED) &&
	    (piValues[10] == TRUE)) {
	    score += 1250;
	}
	if ((nativeConfigAttrs[ANTIALIASING] == PREFERRED) &&
	    (piValues[7] == TRUE)) {
	    score += 624;
	}
	
	/* Stencil bit * 10 + Depth bit */	
	score += piValues[6]*10 + piValues[5];

	if (score > highestScore) {
	    highestScore = score;
	    highestScorePF = i;	    
	    highestScoreAlpha = piValues[4];
	    lowestScoreMultiSample = getMultiSampleScore(piValues[8]);
	} else if (score == highestScore) {
	    if (piValues[4] > highestScoreAlpha) {
		highestScore = score;
		highestScorePF = i;	    
		highestScoreAlpha = piValues[4];
		lowestScoreMultiSample = getMultiSampleScore(piValues[8]);
	    } else if (piValues[4] == highestScoreAlpha) {
		if (getMultiSampleScore(piValues[8]) < lowestScoreMultiSample) { 
		    highestScore = score;
		    highestScorePF = i;	    
		    highestScoreAlpha = piValues[4];
		    lowestScoreMultiSample = getMultiSampleScore(piValues[8]);
		} 
	    }
	}

    }

    if (debug) {
	printf("Select Pixel Format %d\n", pNumFormats[highestScorePF]);
    }

    ReleaseDC(hwnd, hdc);
    wglDeleteContext(hrc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);
    return pNumFormats[highestScorePF];
}


JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_choosePixelFormat(
    JNIEnv   *env,
    jobject   obj,
    jlong     ctxInfo,
    jint      screen,
    jintArray attrList)
{
    int *mx_ptr;
    int   dbVal;  /* value for double buffering */
    int   sVal;   /* value for stereo */
    HDC   hdc;
    int pf;  /* PixelFormat */
    PIXELFORMATDESCRIPTOR pfd;
     
    mx_ptr = (int *)(*env)->GetIntArrayElements(env, attrList, NULL);

    if (mx_ptr[ANTIALIASING] != UNNECESSARY) {
	pf = getExtPixelFormat(mx_ptr);
	if (pf > 0) {
	    return pf;
	}

	/* fallback to use standard ChoosePixelFormat and accumulation buffer */
    }

    hdc = getMonitorDC(screen);
    
    ZeroMemory(&pfd, sizeof(PIXELFORMATDESCRIPTOR));
    pfd.nSize = sizeof(PIXELFORMATDESCRIPTOR);
    pfd.nVersion = 1;  /*TODO: when would this change? */
    pfd.iPixelType = PFD_TYPE_RGBA;

    /*
     * Convert Java 3D values to PixelFormat
     */

    pfd.cRedBits   = (unsigned char) mx_ptr[RED_SIZE];
    pfd.cGreenBits = (unsigned char) mx_ptr[GREEN_SIZE];
    pfd.cBlueBits  = (unsigned char) mx_ptr[BLUE_SIZE];
    pfd.cDepthBits = (unsigned char) mx_ptr[DEPTH_SIZE];

    if (mx_ptr[DOUBLEBUFFER] == REQUIRED || mx_ptr[DOUBLEBUFFER] == PREFERRED)
	dbVal = PFD_DOUBLEBUFFER;
    else 
	dbVal = PFD_DOUBLEBUFFER_DONTCARE;

    sVal = 0;
    if (mx_ptr[STEREO] == REQUIRED || mx_ptr[STEREO] == PREFERRED) {
	sVal = PFD_STEREO;
    } else {
	sVal = 0;
    }
    
    pfd.dwFlags = dbVal | sVal | PFD_SUPPORT_OPENGL;
    pfd.cStencilBits = 2;
    
    if (mx_ptr[ANTIALIASING] == REQUIRED || mx_ptr[ANTIALIASING] == PREFERRED) {
	pfd.cAccumRedBits   = 8;
	pfd.cAccumGreenBits = 8;
	pfd.cAccumBlueBits  = 8;
    }

    pf = findPixelFormatSwitchDoubleBufferAndStereo(&pfd, hdc, mx_ptr); 

    if (pf == -1) {
	/* try disable stencil buffer */
	pfd.cStencilBits = 0;
	pf =  findPixelFormatSwitchDoubleBufferAndStereo(&pfd, hdc, mx_ptr);

	if (pf == -1) {
	    /* try disable accumulate buffer */
	    if (mx_ptr[ANTIALIASING] == PREFERRED) {
		pfd.cStencilBits = 2;
		pfd.cAccumRedBits   = 0;
		pfd.cAccumGreenBits = 0;
		pfd.cAccumBlueBits  = 0;
		pf =  findPixelFormatSwitchDoubleBufferAndStereo(&pfd, hdc, mx_ptr);

		if (pf == -1) {
		    /* try disable stencil buffer */
		    pfd.cStencilBits = 0;
		    pf =  findPixelFormatSwitchDoubleBufferAndStereo(&pfd, hdc, mx_ptr);
		}
	    }
	}
    }
    
    DeleteDC(hdc);

    (*env)->ReleaseIntArrayElements(env, attrList, mx_ptr, JNI_ABORT);
    return pf;
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isStereoAvailable(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong display,
    jint screen,
    jint pixelFormat)
{
    HDC hdc;          /* HW Device Context */

    PIXELFORMATDESCRIPTOR pfd;

    static GLboolean first_time = GL_TRUE;
    static GLboolean force_no_stereo = GL_FALSE;

    if (first_time) {
        if (getenv("J3D_NO_STEREO") != NULL) {
            fprintf(stderr, "Java 3D: stereo mode disabled\n");
            force_no_stereo = GL_TRUE;
        }
        first_time = GL_FALSE;
    }

    if (force_no_stereo)
        return JNI_FALSE;

    hdc = getMonitorDC(screen);

    /* Check the chosen PixelFormat to see if it is stereo capable */
    DescribePixelFormat(hdc, pixelFormat, sizeof(pfd), &pfd);

    DeleteDC(hdc);
    if (pfd.dwFlags & PFD_STEREO) 
        return JNI_TRUE;
    else
        return JNI_FALSE;

    return JNI_TRUE;
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isDoubleBufferAvailable(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong display,    
    jint screen,
    jint pixelFormat)
{
    HDC hdc;          /* HW Device Context */

    PIXELFORMATDESCRIPTOR pfd;

    hdc = getMonitorDC(screen);
    
    /* Check the chosen PixelFormat to see if it is doubleBuffer capable */
    DescribePixelFormat(hdc, pixelFormat, sizeof(pfd), &pfd);

    DeleteDC(hdc);
    if (pfd.dwFlags & PFD_DOUBLEBUFFER)
        return JNI_TRUE;
    else
        return JNI_FALSE;
}



JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingAccumAvailable(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong display,
    jint screen,
    jint pixelFormat)
{
    HDC hdc;          /* HW Device Context */
    PIXELFORMATDESCRIPTOR pfd;

    hdc = getMonitorDC(screen);
    /* Check the chosen PixelFormat to see if it is sceneAntialiasing capable */
    DescribePixelFormat(hdc, pixelFormat, sizeof(pfd), &pfd);

    DeleteDC(hdc);
    if (pfd.cAccumRedBits > 0)
        return JNI_TRUE;
    else
        return JNI_FALSE;
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultiSamplesAvailable(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong display,
    jint screen,
    jint pixelFormat)
{
    /*
     * Issue 16: this routine is commented out (and JNI_FALSE is
     * returned) until a solution can be found that doesn't affect the
     * current WGL context for the calling thread, or we need to
     * define and implement a mechanism to revalidate the current
     * context.
     */
    return JNI_FALSE;

#if 0
    static char szAppName[] = "Choose Pixel Format";
    HWND hwnd;
    HGLRC hrc;
    HDC   hdc;
    int attr[3];
    int piValues[2];
    int pf;
    BOOL support;
    
    static PIXELFORMATDESCRIPTOR pfd = {
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

    PFNWGLGETPIXELFORMATATTRIBIVEXTPROC wglGetPixelFormatAttribivEXT = NULL;

    hwnd = createDummyWindow((const char *)szAppName);
 
    if (!hwnd) {
	return -1;
    }
    hdc = GetDC(hwnd);

    pf = ChoosePixelFormat(hdc, &pfd);
    if (!pf) {
	printErrorMessage("Failed in ChoosePixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    SetPixelFormat(hdc, pf, &pfd);
    
    hrc = wglCreateContext(hdc);
    if (!hrc) {
	printErrorMessage("Failed in wglCreateContext");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }
    
    if (!wglMakeCurrent(hdc, hrc)) {
	printErrorMessage("Failed in wglMakeCurrent");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	wglGetProcAddress("wglGetPixelFormatAttribivARB");

    if (wglGetPixelFormatAttribivEXT == NULL) {
	wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	    wglGetProcAddress("wglGetPixelFormatAttribivEXT");

	if (wglGetPixelFormatAttribivEXT == NULL) {
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);	    	    
	    return FALSE;
	} 
    }

    attr[0] = WGL_SAMPLE_BUFFERS_ARB;
    attr[1] = WGL_SAMPLES_ARB;
    attr[2] = 0;
    support = FALSE;
    
    if (wglGetPixelFormatAttribivEXT(hdc, pixelFormat, 0, 2, attr, piValues)) {
	if ((piValues[0] == TRUE) && (piValues[1] > 1)) {
	    support = TRUE;
	}
    } 

    ReleaseDC(hwnd, hdc);
    wglDeleteContext(hrc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);
    return support;
#endif /* 0 */
}
#endif /* WIN32 */
