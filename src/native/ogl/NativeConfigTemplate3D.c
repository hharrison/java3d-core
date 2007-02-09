/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
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

/* j3dsys.h needs to be included before any other include files to suppres VC warning */
#include "j3dsys.h"

#include <jni.h>
#include <math.h>
#include <stdlib.h>

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

/* check if the extension is supported  */
extern int isExtensionSupported(const char *allExtensions, const char *extension); 

/* by MIK OF CLASSX */
extern jboolean getJavaBoolEnv(JNIEnv *env, char* envStr);

#if defined(UNIX)

/* Fix for issue 20 */
#define  MAX_GLX_ATTRS_LENGTH 100


GLXFBConfig *find_S_FBConfigs(jlong display,
			      jint screen,
			      int* glxAttrs,
			      int stencilVal,
			      int sIndex) {


    GLXFBConfig *fbConfigList = NULL;
    int          numFBConfigs, index;
    MYPFNGLXCHOOSEFBCONFIG pGLXChooseFbConfig = NULL;
    GLboolean userReq = GL_TRUE;

    pGLXChooseFbConfig =
	(MYPFNGLXCHOOSEFBCONFIG) dlsym(RTLD_DEFAULT, "glXChooseFBConfig");

    J3D_ASSERT((sIndex+3) < MAX_GLX_ATTRS_LENGTH);

    /* if user not use stencil we will request one of internal use. */
    if (stencilVal < 1) {
	userReq = GL_FALSE;
	stencilVal = 1;
    }

    index = sIndex;
    glxAttrs[index++] = GLX_STENCIL_SIZE;
    glxAttrs[index++] = stencilVal;
    glxAttrs[index] = None;
	
    fbConfigList = pGLXChooseFbConfig((Display*)display, screen,
				      glxAttrs, &numFBConfigs);
	
    
    if(fbConfigList != NULL) {
	return fbConfigList;
    }

    /*
      fprintf(stderr, "Stencil : find_S_FBConfigs (TRY 1): FAIL -- stencilVal = %d\n",
      stencilVal);
    */
    
    if (userReq == GL_TRUE) {
	/* fprintf(stderr, "    userReq :  *** FAILED ***\n"); */
	return NULL;
    }
    
    index = sIndex;
    glxAttrs[index] = None;	
	
    fbConfigList = pGLXChooseFbConfig((Display*)display, screen,
				      glxAttrs, &numFBConfigs);
	
    
    if(fbConfigList != NULL) {
	return fbConfigList;
    }
    
    /* 
       fprintf(stderr, "Stencil : find_S_FBConfigs (TRY 2): FAIL -- stencilVal = %d\n",
       stencilVal);
    */
    
    return NULL;

}

GLXFBConfig *find_S_S_FBConfigs(jlong display,
			      jint screen,
			      int* glxAttrs,
			      int stereoVal,
			      int stencilVal,
			      int sIndex) {

    GLXFBConfig *fbConfigList = NULL;
    int          numFBConfigs, index;

    J3D_ASSERT((sIndex+3) < MAX_GLX_ATTRS_LENGTH);
    
    if (stereoVal == REQUIRED || stereoVal== PREFERRED) {

	index = sIndex;
	glxAttrs[index++] = GLX_STEREO;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;

	fbConfigList = find_S_FBConfigs(display, screen, glxAttrs, stencilVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    
    if (stereoVal == UNNECESSARY || stereoVal== PREFERRED) {
	/* This is a workaround to BugId : 5106472 in Solaris OGL.
	   We can't set glxAttrs with GLX_STEREO follow by a boolean */

	index = sIndex;
	glxAttrs[index] = None;
	
	/* For debug only
	{   
	    int i=0;
	    fprintf(stderr, "find_S_S_FBConfigs stereoVal = %d\n", stereoVal);    

	    while(glxAttrs[i] != None) {
		fprintf(stderr, "glxAttrs[%d] = %x", i, glxAttrs[i]);
		i++;
		fprintf(stderr, " glxAttrs[%d] = %x\n", i, glxAttrs[i]);    
		i++;
	    }
	}
	*/

	fbConfigList = find_S_FBConfigs(display, screen, glxAttrs, stencilVal, index);

	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    if (stereoVal == UNNECESSARY) {
	index = sIndex;
	glxAttrs[index++] = GLX_STEREO;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;
	
	/*
	  fbConfigList = pGLXChooseFbConfig((Display*)display, screen,
	  glxAttrs, &numFBConfigs);
	*/
	fbConfigList = find_S_FBConfigs(display, screen, glxAttrs, stencilVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }

    return NULL;
}

GLXFBConfig *find_AA_S_S_FBConfigs(jlong display,
				   jint screen,
				   int* glxAttrs,
				   int stereoVal, 
				   int antialiasVal,
				   int stencilVal,
				   int antialiasIndex) {

    const char *glxExtensions = NULL;
    GLXFBConfig *fbConfigList = NULL;
    int index = antialiasIndex;


    J3D_ASSERT((antialiasIndex+7) < MAX_GLX_ATTRS_LENGTH);

    if(antialiasVal == REQUIRED || antialiasVal== PREFERRED) {
	glxExtensions = (const char *) glXGetClientString((Display*)display, GLX_EXTENSIONS);
	
	if(isExtensionSupported(glxExtensions, "GLX_ARB_multisample")){
	    static const int SAMPLE_LENGTH = 5;
	    static const int samples[] = {8,6,4,3,2};
	    int i, samplesIndex;
	    
	    index = antialiasIndex;
	    glxAttrs[index++] = GLX_SAMPLE_BUFFERS_ARB;
	    glxAttrs[index++] = 1;
	    glxAttrs[index++] = GLX_SAMPLES_ARB;
	    samplesIndex = index++; /* Will assign samples in the sample selection loop */
	    glxAttrs[index] = None;

	    for(i=0; i < SAMPLE_LENGTH; i++) {
		/* fprintf(stderr, "find_AA_S_S_FBConfigs samples = %d\n", samples[i]); */
		glxAttrs[samplesIndex] = samples[i];
		fbConfigList = find_S_S_FBConfigs(display, screen, glxAttrs, stereoVal,
						  stencilVal, index);
	    
		if(fbConfigList != NULL) {
		    return fbConfigList;
		}
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
	
	fbConfigList = find_S_S_FBConfigs(display, screen, glxAttrs,
					  stereoVal, stencilVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    
    glxAttrs[antialiasIndex] = None;

    if (antialiasVal == UNNECESSARY || antialiasVal == PREFERRED) {
	fbConfigList = find_S_S_FBConfigs(display, screen, glxAttrs,
					  stereoVal, stencilVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    /* We will stop trying even if no fbConfigList is found and 
       antialiasVal = UNNECESSARY */

    return NULL;
    
}

GLXFBConfig *find_DB_AA_S_S_FBConfigs(jlong display,
				    jint screen,
				    int* glxAttrs,
				    int stereoVal, int dbVal,
				    int antialiasVal,
				    int stencilVal,
				    int dbIndex) {

    GLXFBConfig *fbConfigList = NULL;
    int index = dbIndex;

    J3D_ASSERT((dbIndex+3) < MAX_GLX_ATTRS_LENGTH);

    if (dbVal == REQUIRED || dbVal== PREFERRED) {
	    
	    index = dbIndex;
	    glxAttrs[index++] = GLX_DOUBLEBUFFER;
            glxAttrs[index++] = True;
	    glxAttrs[index] = None;

	    fbConfigList = find_AA_S_S_FBConfigs(display, screen, glxAttrs,
						 stereoVal, antialiasVal,
						 stencilVal, index);
	    
	    if(fbConfigList != NULL) {
		return fbConfigList;
	    }
    }
    
    if (dbVal == UNNECESSARY || dbVal== PREFERRED) {
	index = dbIndex;
	glxAttrs[index++] = GLX_DOUBLEBUFFER;
	glxAttrs[index++] = False;
	glxAttrs[index] = None;
	
	fbConfigList = find_AA_S_S_FBConfigs(display, screen, glxAttrs,
					     stereoVal, antialiasVal,
					     stencilVal, index);
	
	if(fbConfigList != NULL) {
	    return fbConfigList;
	}
    }
    
    if (dbVal == UNNECESSARY) {
	index = dbIndex;
	glxAttrs[index++] = GLX_DOUBLEBUFFER;
	glxAttrs[index++] = True;
	glxAttrs[index] = None;
	
	fbConfigList = find_AA_S_S_FBConfigs(display, screen, glxAttrs,
					     stereoVal, antialiasVal,
					     stencilVal, index);
	
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
    int dbVal;  /* value for double buffering */
    int stereoVal;   /* value for stereo */
    int antialiasVal; /* value for antialias */
    int stencilVal; /* value for stencil size */

    int drawableIndex;
    
    jlong *fbConfigListPtr = NULL;
    int status, major, minor;    

    Display *dpy = (Display *) display;

    fbConfigListPtr = (*env)->GetLongArrayElements(env, fbConfigArray, NULL);
    mx_ptr = (*env)->GetIntArrayElements(env, attrList, NULL);

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
    
    /* by MIK OF CLASSX */
    if (getJavaBoolEnv(env, "transparentOffScreen")) {
    	glxAttrs[index++] = GLX_ALPHA_SIZE;
    	glxAttrs[index++] = 1;
    }

    glxAttrs[index++] = GLX_DEPTH_SIZE;
    glxAttrs[index++] = mx_ptr[DEPTH_SIZE];
    glxAttrs[index] = None;
    
    dbVal = mx_ptr[DOUBLEBUFFER];
    stereoVal = mx_ptr[STEREO];
    antialiasVal = mx_ptr[ANTIALIASING]; 
    stencilVal = mx_ptr[STENCIL_SIZE];

    (*env)->ReleaseIntArrayElements(env, attrList, mx_ptr, JNI_ABORT);

    /* Get Pbuffer-capable visual unless j3d.usePbuffer property is FALSE */
    if (getJavaBoolEnv(env,"usePbuffer")) {
	fbConfigList = find_DB_AA_S_S_FBConfigs(display, screen, glxAttrs, stereoVal,
						dbVal, antialiasVal, stencilVal, index);
    }

    if(fbConfigList == NULL) { /*  Try with Pixmap, if Pbuffer fail. */
	glxAttrs[drawableIndex] = (GLX_PIXMAP_BIT | GLX_WINDOW_BIT);
	fbConfigList = find_DB_AA_S_S_FBConfigs(display, screen, glxAttrs, stereoVal,
						dbVal, antialiasVal, stencilVal, index);
    }
    
    if(fbConfigList == NULL) { /* Try with Window only, if Pixmap fail. */
	glxAttrs[drawableIndex] =  GLX_WINDOW_BIT;	
	fbConfigList = find_DB_AA_S_S_FBConfigs(display, screen, glxAttrs, stereoVal,
						dbVal, antialiasVal, stencilVal, index);
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

JNIEXPORT jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_getStencilSize(
     JNIEnv *env,
     jobject obj,
     jlong display,
     jint screen,
     jint vid)
{
    Display *dpy = (Display*) display;
    XVisualInfo *vinfo, template;
    int nitems;
    int stencilVal = 0;

    template.visualid = vid;
    vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_getStencilSize got unexpected number of matching visuals %d\n", nitems);
    }

    glXGetConfig(dpy, vinfo, GLX_STENCIL_SIZE, &stencilVal);

    return stencilVal;
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
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(
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
#endif /* UNIX_ */


#ifdef WIN32

extern HWND createDummyWindow(const char* szAppName);


PIXELFORMATDESCRIPTOR getDummyPFD() {
    
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

BOOL isSupportedWGL(const char *extensions, const char *extension_string) {    
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

/* NOTE : Since OpenGL 1.2 or greater is required. This is method will not be
 *        called. Need to remove in Java 3D 1.5
 */
int find_DB_S_STDPixelFormat(PIXELFORMATDESCRIPTOR* pfd, HDC hdc,
			     int *mx_ptr, GLboolean offScreen, int stencilVal)
{    
    int pf;
    PIXELFORMATDESCRIPTOR newpfd;
    
    pf = ChoosePixelFormat(hdc, pfd);
    
    if(!offScreen) {
	/* onScreen : Check if pixel format support min. requirement */
	DescribePixelFormat(hdc, pf, sizeof(newpfd), &newpfd);

	if ((newpfd.cRedBits < (unsigned char) mx_ptr[RED_SIZE]) || 
	    (newpfd.cGreenBits < (unsigned char) mx_ptr[GREEN_SIZE]) ||
	    (newpfd.cBlueBits  < (unsigned char) mx_ptr[BLUE_SIZE]) ||
	    (newpfd.cDepthBits < (unsigned char) mx_ptr[DEPTH_SIZE]) ||
	    (newpfd.cStencilBits < (unsigned char) mx_ptr[STENCIL_SIZE]) ||
	    ((mx_ptr[DOUBLEBUFFER] == REQUIRED) &&
	     ((newpfd.dwFlags & PFD_DOUBLEBUFFER) == 0)) ||
	    ((mx_ptr[STEREO] == REQUIRED) && ((newpfd.dwFlags & PFD_STEREO) == 0)))
	    {
		return -1;
	    }
	
	if ((mx_ptr[ANTIALIASING] == REQUIRED) &&
	    ((newpfd.cAccumRedBits <= 0) ||
	     (newpfd.cAccumGreenBits <= 0) ||
	     (newpfd.cAccumBlueBits  <= 0)))
	    {
		return -1;
	    }
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
    fprintf(stderr, "Java 3D ERROR : %s - %s\n", message, errString);
    LocalFree(errString);
}

/* Fix for issue 76 */
#define  MAX_WGL_ATTRS_LENGTH 100


int find_S_PixelFormat(HDC hdc, PixelFormatInfo * pFormatInfo,
		       int* wglAttrs, int stencilVal, int sIndex) {
    
    int pFormat, availableFormats, index;
    GLboolean userReq = GL_TRUE;
	
    J3D_ASSERT((sIndex+4) < MAX_WGL_ATTRS_LENGTH);
    /* if user not use stencil we will request one of internal use. */
    if (stencilVal < 1) {
	userReq = GL_FALSE;
	stencilVal = 1;
    }
    
    index = sIndex;
    wglAttrs[index++] = WGL_STENCIL_BITS_ARB;
    wglAttrs[index++] = stencilVal;
    /*
     * Terminate by 2 zeros to avoid driver bugs
     * that assume attributes always come in pairs.
     */
    wglAttrs[index] = 0;
    wglAttrs[index+1] = 0;

    pFormat = -1;
    
    if ((pFormatInfo->wglChoosePixelFormatARB(hdc, wglAttrs, NULL, 1,
					      &pFormat, &availableFormats)) && (availableFormats > 0)) {
	
	/*
	  fprintf(stderr, "Stencil : wglChoosePixelFormatARB : pFormat %d availableFormats %d\n",
	  pFormat, availableFormats);
	*/
	return pFormat;
    }

    /*
      fprintf(stderr, "Stencil : wglChoosePixelFormatARB (TRY 1): FAIL -- stencilVal = %d\n",
      stencilVal);
    */
    
    if (userReq == GL_TRUE) {
	/* fprintf(stderr, "    userReq :  *** FAILED ***\n"); */
	return -1;
    }

    index = sIndex;

    /*
     * Terminate by 2 zeros to avoid driver bugs
     * that assume attributes always come in pairs.
     */
    wglAttrs[index] = 0;
    wglAttrs[index+1] = 0;
	
    pFormat = -1;
    
    if ((pFormatInfo->wglChoosePixelFormatARB(hdc, wglAttrs, NULL, 1,
					      &pFormat, &availableFormats)) && (availableFormats > 0)) {
	
	/* fprintf(stderr, "wglChoosePixelFormatARB : pFormat %d availableFormats %d\n",
	   pFormat, availableFormats); */
	    
	    return pFormat;
    }
    
    /* fprintf(stderr, "wglChoosePixelFormatARB (TRY 2): FAIL\n"); */
    return -1;
}

int find_S_S_PixelFormat(HDC hdc, PixelFormatInfo * pFormatInfo, int* wglAttrs,
			 int stereoVal, int stencilVal, int sIndex) {
    
    int pFormat, index;
    
    J3D_ASSERT((sIndex+4) < MAX_WGL_ATTRS_LENGTH);
    
    if (stereoVal == REQUIRED || stereoVal== PREFERRED) {

	index = sIndex;
	wglAttrs[index++] = WGL_STEREO_ARB;
	wglAttrs[index++] = TRUE;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;
	
	pFormat = find_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
				     stencilVal, index);
	/* fprintf(stderr,"STEREO REQUIRED or PREFERRED ***pFormat  %d\n", pFormat); */
	    
	if(pFormat >= 0) {
	    return pFormat;
	}	
    }
    
    if (stereoVal == UNNECESSARY || stereoVal== PREFERRED) {
	
	index = sIndex;
	wglAttrs[index++] = WGL_STEREO_ARB;
	wglAttrs[index++] = FALSE;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;
	
	pFormat = find_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
				     stencilVal, index);
	
	/* fprintf(stderr,"STEREO UNNECC. or PREFERRED ***pFormat  %d\n", pFormat); */
	
	if(pFormat >= 0) {
	    return pFormat;
	}
    }

    if (stereoVal == UNNECESSARY) {
	index = sIndex;
	wglAttrs[index++] = WGL_STEREO_ARB;
	wglAttrs[index++] = TRUE;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;
	
	pFormat = find_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
				     stencilVal, index);

	/* fprintf(stderr,"STEREO UNNECC. ***pFormat  %d\n", pFormat); */

	if(pFormat >= 0) {
	    return pFormat;
	}
    }
    
    return -1;
}


int find_AA_S_S_PixelFormat( HDC hdc, PixelFormatInfo * pFormatInfo,
			     int* wglAttrs, int stereoVal, int antialiasVal,
			     int stencilVal, int antialiasIndex) {
    
    int index;
    int pFormat;
    GLboolean supportMS = GL_FALSE;
    
    J3D_ASSERT((antialiasIndex+8) < MAX_WGL_ATTRS_LENGTH);

    if(antialiasVal == REQUIRED || antialiasVal== PREFERRED) {

	if(isSupportedWGL(pFormatInfo->supportedExtensions, "WGL_ARB_multisample")) {	
	    supportMS = GL_TRUE;
	}
	else {
	    /* Under Wildcat III it doesn't use wglGetExtensionString */
	    char *supportedExtensions = (char *) glGetString(GL_EXTENSIONS);	    

	    /* fprintf(stderr, "GL Supported extensions: %s.\n", supportedExtensions); */
	    
	    if (isSupportedWGL(supportedExtensions, "GL_ARB_multisample")) {	    
		supportMS = GL_TRUE;
	    }
	}
	
	if(supportMS) {
	    static const int SAMPLE_LENGTH = 5;
	    static const int samples[] = {8,6,4,3,2};
	    int i, samplesIndex;
	    
	    index = antialiasIndex;
	    wglAttrs[index++] = WGL_SAMPLE_BUFFERS_ARB;
	    wglAttrs[index++] = 1;
	    wglAttrs[index++] = WGL_SAMPLES_ARB;
	    samplesIndex = index++; /* Will assign samples in the sample selection loop */
	    /*
	     * Terminate by 2 zeros to avoid driver bugs
	     * that assume attributes always come in pairs.
	     */
	    wglAttrs[index] = 0;
	    wglAttrs[index+1] = 0;


	    for(i=0; i < SAMPLE_LENGTH; i++) {
		/* fprintf(stderr, "find_AA_S_S_PixelFormat samples = %d\n", samples[i]); */
		
		wglAttrs[samplesIndex] = samples[i];
		pFormat = find_S_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
					       stereoVal, stencilVal, index);
		if(pFormat >= 0) {
		    return pFormat;
		}
	    }
	}
    }
    
    if ( antialiasVal == REQUIRED ) {

	index = antialiasIndex;
        wglAttrs[index++] = WGL_ACCUM_RED_BITS_ARB;
        wglAttrs[index++] = 8;
        wglAttrs[index++] = WGL_ACCUM_GREEN_BITS_ARB;
        wglAttrs[index++] = 8;
        wglAttrs[index++] = WGL_ACCUM_BLUE_BITS_ARB;
        wglAttrs[index++] = 8;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;
	
	pFormat = find_S_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
				       stereoVal, stencilVal, index);
	
	if(pFormat >= 0) {
	    return pFormat;
	}
    }
    
    /*
     * Terminate by 2 zeros to avoid driver bugs
     * that assume attributes always come in pairs.
     */
    index = antialiasIndex;
    wglAttrs[index] = 0;
    wglAttrs[index+1] = 0;

    if (antialiasVal == UNNECESSARY || antialiasVal == PREFERRED) {

	pFormat = find_S_S_PixelFormat(hdc, pFormatInfo, wglAttrs,
				       stereoVal, stencilVal, index);
	
	if(pFormat >= 0) {
	    return pFormat;
	}
	
    }

    return -1;
    
}


int find_DB_AA_S_S_PixelFormat( HDC hdc, PixelFormatInfo * pFormatInfo,
				int* wglAttrs, int stereoVal, int dbVal,
				int antialiasVal, int stencilVal, int dbIndex) {
    
    int index = dbIndex;
    int pFormat;
    
    J3D_ASSERT((dbIndex+4) < MAX_WGL_ATTRS_LENGTH); 

    if (dbVal == REQUIRED || dbVal== PREFERRED) {
	    
	index = dbIndex;
	wglAttrs[index++] = WGL_DOUBLE_BUFFER_ARB;
	wglAttrs[index++] = TRUE;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;

 	pFormat = find_AA_S_S_PixelFormat(hdc, pFormatInfo,
					  wglAttrs, stereoVal, 
					  antialiasVal, stencilVal, index);
	    
	if(pFormat >= 0) {
	    return pFormat;
	}
    }
    
    if (dbVal == UNNECESSARY || dbVal== PREFERRED) {
	index = dbIndex;
	wglAttrs[index++] = WGL_DOUBLE_BUFFER_ARB;
	wglAttrs[index++] = FALSE;  /* Partial fix to issue 100. */
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;

	pFormat = find_AA_S_S_PixelFormat(hdc, pFormatInfo,
					  wglAttrs, stereoVal, 
					  antialiasVal, stencilVal, index);
	
	if(pFormat >= 0) {
	    return pFormat;
	}
    }

    if (dbVal == UNNECESSARY) {
	index = dbIndex;
	wglAttrs[index++] = WGL_DOUBLE_BUFFER_ARB;
	wglAttrs[index++] = TRUE;
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;

 	pFormat = find_AA_S_S_PixelFormat(hdc, pFormatInfo,
					  wglAttrs, stereoVal, 
					  antialiasVal, stencilVal, index);
	
	if(pFormat >= 0) {
	    return pFormat;
	}
    }

    return -1;
}

/* Fix to issue 77 */ 
/* Check PixelFormat capabilities and cache the info. into pFormatInfo structure */
void checkPixelFormat(HDC hdc,
		      PixelFormatInfo *pFormatInfo,
		      GLboolean offScreen )
{
    int wglAttrs[3];
    int piValues[2];
    int pf = -1;
    GLboolean hasMultisample = GL_FALSE;
    GLboolean hasStereo  = GL_FALSE;
    GLboolean hasDoubleBuffer = GL_FALSE;
    GLboolean hasAccum
	= GL_FALSE;
    int       stencilSize = 0;
    PIXELFORMATDESCRIPTOR pfd;

    /* fprintf(stderr, "*** checkPixelFormat : offScreen = %d\n", offScreen); */
    
    if(!offScreen) {
	pf = pFormatInfo->onScreenPFormat;
    }
    else {
	pf = pFormatInfo->offScreenPFormat;
    }

    /* Assert that pf is valid */
    J3D_ASSERT(pf >= 0); 
    piValues[0] = GL_FALSE;
    piValues[1] = -1;    
    
    if( pFormatInfo->supportARB &&
        isSupportedWGL( pFormatInfo->supportedExtensions, "WGL_ARB_multisample")){	
	wglAttrs[0] = WGL_SAMPLE_BUFFERS_ARB;
	wglAttrs[1] = WGL_SAMPLES_ARB;
	wglAttrs[2] = 0;
	if (pFormatInfo->wglGetPixelFormatAttribivARB(hdc, pf, 0, 2,
						      wglAttrs, piValues)) {
	    if ((piValues[0]) && (piValues[1] > 1)) {
		hasMultisample = GL_TRUE;		
	    }
	}
	/*
	  fprintf(stderr, "*** checkPixelFormat : hasMultisample = %d numSamples %d\n",
	  hasMultisample, piValues[1]);
	*/
    }

    DescribePixelFormat(hdc, pf, sizeof(pfd), &pfd);

    if (pfd.dwFlags & PFD_STEREO) {
	hasStereo = GL_TRUE;
    }
    if (pfd.dwFlags & PFD_DOUBLEBUFFER) {
	hasDoubleBuffer = GL_TRUE;
    }
    if (pfd.cAccumRedBits > 0) {
	hasAccum = GL_TRUE;
    }
    
    stencilSize = pfd.cStencilBits;
    
    /*
      fprintf(stderr, "hasStereo = %d,  hasDoubleBuffer %d, hasAccum %d stencilSize %d\n",
	    hasStereo, hasDoubleBuffer, hasAccum, pfd.cStencilBits);
    */

    if(pFormatInfo->onScreenPFormat == pFormatInfo->offScreenPFormat) {
	pFormatInfo->onScreenHasMultisample = hasMultisample;
	pFormatInfo->onScreenHasStereo = hasStereo;
	pFormatInfo->onScreenHasDoubleBuffer = hasDoubleBuffer;
	pFormatInfo->onScreenHasAccum = hasAccum;
	pFormatInfo->onScreenStencilSize = stencilSize;

	pFormatInfo->offScreenHasMultisample = hasMultisample;
	pFormatInfo->offScreenHasStereo = hasStereo;
	pFormatInfo->offScreenHasDoubleBuffer = hasDoubleBuffer;
	pFormatInfo->offScreenHasAccum = hasAccum;
	pFormatInfo->offScreenStencilSize = stencilSize;
    }
    else if(!offScreen) {
	pFormatInfo->onScreenHasMultisample = hasMultisample;
	pFormatInfo->onScreenHasStereo = hasStereo;
	pFormatInfo->onScreenHasDoubleBuffer = hasDoubleBuffer;
	pFormatInfo->onScreenHasAccum = hasAccum;
	pFormatInfo->onScreenStencilSize = stencilSize;
    }
    else {
	pFormatInfo->offScreenHasMultisample = hasMultisample;
	pFormatInfo->offScreenHasStereo = hasStereo;
	pFormatInfo->offScreenHasDoubleBuffer = hasDoubleBuffer;
	pFormatInfo->offScreenHasAccum = hasAccum;
	pFormatInfo->offScreenStencilSize = stencilSize;
    }
    
}

		      
int chooseSTDPixelFormat(
    JNIEnv   *env,
    jint      screen,
    jintArray attrList,
    HDC       hdc,
    GLboolean offScreen)
{
    int *mx_ptr;
    int   dbVal;  /* value for double buffering */
    int   stereoVal;   /* value for stereo */
    int   pFormat = -1;  /* PixelFormat */
    PIXELFORMATDESCRIPTOR pfd;
    int   stencilVal = 0; /* value for stencil size */
    GLboolean userReq = GL_TRUE;

    /* fprintf(stderr, "chooseSTDPixelFormat : screen 0x%x, offScreen %d hdc 0x%x\n",
       screen, offScreen, hdc);  */
    
    ZeroMemory(&pfd, sizeof(PIXELFORMATDESCRIPTOR));
    pfd.nSize = sizeof(PIXELFORMATDESCRIPTOR);
    pfd.nVersion = 1;  /* When would this change? */
    pfd.iPixelType = PFD_TYPE_RGBA;

    /*
     * Convert Java 3D values to PixelFormat
     */
    mx_ptr = (*env)->GetIntArrayElements(env, attrList, NULL);

    if(!offScreen) {
	
	pfd.cRedBits   = (unsigned char) mx_ptr[RED_SIZE];
	pfd.cGreenBits = (unsigned char) mx_ptr[GREEN_SIZE];
	pfd.cBlueBits  = (unsigned char) mx_ptr[BLUE_SIZE];
	pfd.cDepthBits = (unsigned char) mx_ptr[DEPTH_SIZE];
	
	if (mx_ptr[DOUBLEBUFFER] == REQUIRED || mx_ptr[DOUBLEBUFFER] == PREFERRED)
	    dbVal = PFD_DOUBLEBUFFER;
	else 
	    dbVal = PFD_DOUBLEBUFFER_DONTCARE;
	
	stereoVal = 0;
	if (mx_ptr[STEREO] == REQUIRED || mx_ptr[STEREO] == PREFERRED) {
	    stereoVal = PFD_STEREO;
	} else {
	    stereoVal = PFD_STEREO_DONTCARE;
	}
    
	pfd.dwFlags = dbVal | stereoVal | PFD_SUPPORT_OPENGL | PFD_DRAW_TO_WINDOW;

	/* if user not use stencil we will request one of internal use. */
	stencilVal = mx_ptr[STENCIL_SIZE];
	if (stencilVal < 1) {
	    userReq = GL_FALSE;
	    stencilVal = 1;
	}
	
	pfd.cStencilBits = stencilVal;
	
	if (mx_ptr[ANTIALIASING] == REQUIRED) {
	    pfd.cAccumRedBits   = 8;
	    pfd.cAccumGreenBits = 8;
	    pfd.cAccumBlueBits  = 8;
	}

    }
    else { /* Offscreen setting. */
	/* We are here b/c there is no support for Pbuffer on the HW.
	   This is a fallback path, we will hardcore the value. */  

	/* NOTE : Stencil size isn't handle for Offscreen */
	
	/* by MIK OF CLASSX */
	pfd.iPixelType = PFD_TYPE_RGBA;
	if (getJavaBoolEnv(env, "transparentOffScreen")) {
	    pfd.cRedBits   = 8;
	    pfd.cGreenBits = 8;
	    pfd.cBlueBits  = 8;
	    pfd.cAlphaBits = 8;
	    pfd.cColorBits = 32;
	}
	else {
	    pfd.cRedBits   = 8;
	    pfd.cGreenBits = 8;
	    pfd.cBlueBits  = 8;
	    pfd.cAlphaBits = 0;
	    pfd.cColorBits = 24;
	}
	pfd.cDepthBits = 32;
	pfd.iLayerType = PFD_MAIN_PLANE;
	pfd.dwFlags = PFD_SUPPORT_OPENGL | PFD_DRAW_TO_BITMAP | PFD_SUPPORT_GDI;

    }
    
    pFormat = find_DB_S_STDPixelFormat(&pfd, hdc, mx_ptr, offScreen, stencilVal);

    if ((pFormat == -1) && (userReq == GL_FALSE)) {
	/* try disable stencil buffer */
	pfd.cStencilBits = 0;
	stencilVal = 0;
	pFormat =  find_DB_S_STDPixelFormat(&pfd, hdc, mx_ptr, offScreen, stencilVal);
    }
    
    (*env)->ReleaseIntArrayElements(env, attrList, mx_ptr, JNI_ABORT);
    return pFormat;
}

PixelFormatInfo * newPixelFormatInfo(HDC hdc, jboolean usePbuffer)
{
    PFNWGLGETEXTENSIONSSTRINGARBPROC  wglGetExtensionsStringARB = NULL;

    PixelFormatInfo *pFormatInfo = (PixelFormatInfo *) malloc(sizeof(PixelFormatInfo));

    /* Initialize pFormatInfo */
    pFormatInfo->onScreenPFormat = -1;
    pFormatInfo->onScreenHasMultisample = GL_FALSE;    
    pFormatInfo->onScreenHasStereo = GL_FALSE;    
    pFormatInfo->onScreenHasDoubleBuffer = GL_FALSE;    
    pFormatInfo->onScreenHasAccum = GL_FALSE;    
    pFormatInfo->onScreenStencilSize = 0;    

    pFormatInfo->offScreenPFormat = -1;
    pFormatInfo->offScreenHasMultisample = GL_FALSE;
    pFormatInfo->offScreenHasStereo = GL_FALSE;
    pFormatInfo->offScreenHasDoubleBuffer = GL_FALSE;
    pFormatInfo->offScreenHasAccum = GL_FALSE;
    pFormatInfo->offScreenStencilSize = 0;    
    pFormatInfo->drawToPbuffer = GL_FALSE;

    pFormatInfo->supportARB = GL_FALSE;
    pFormatInfo->supportPbuffer = GL_FALSE;
    pFormatInfo->supportedExtensions = NULL;
    pFormatInfo->wglChoosePixelFormatARB = NULL; 
    pFormatInfo->wglGetPixelFormatAttribivARB = NULL;
    pFormatInfo->wglCreatePbufferARB = NULL;
    pFormatInfo->wglGetPbufferDCARB = NULL;
    pFormatInfo->wglReleasePbufferDCARB = NULL;
    pFormatInfo->wglDestroyPbufferARB = NULL;
    pFormatInfo->wglQueryPbufferARB = NULL;
    
    wglGetExtensionsStringARB = (PFNWGLGETEXTENSIONSSTRINGARBPROC)
	wglGetProcAddress("wglGetExtensionsStringARB");
    if (wglGetExtensionsStringARB == NULL) {
	printErrorMessage("wglGetExtensionsStringARB not support !\n");
	/* Doesn't support extensions, return to use standard choosePixelFormat. */
	return pFormatInfo;  
    }
    
    /* get the list of supported extensions */
    pFormatInfo->supportedExtensions = (char *)wglGetExtensionsStringARB(hdc);    
    
    /* fprintf(stderr, "WGL Supported extensions: %s.\n",
       pFormatInfo->supportedExtensions);    */

    if (isSupportedWGL(pFormatInfo->supportedExtensions, "WGL_ARB_pixel_format")) {
	pFormatInfo->wglChoosePixelFormatARB = (PFNWGLCHOOSEPIXELFORMATARBPROC)
	    wglGetProcAddress("wglChoosePixelFormatARB");
	    
	pFormatInfo->wglGetPixelFormatAttribivARB =
	    (PFNWGLGETPIXELFORMATATTRIBIVARBPROC)
	    wglGetProcAddress("wglGetPixelFormatAttribivARB");

	if ((pFormatInfo->wglChoosePixelFormatARB != NULL) &&
	    (pFormatInfo->wglGetPixelFormatAttribivARB != NULL)){

	    /* fprintf(stderr, "wglChoosePixelFormatARB is supported.\n"); */
	    pFormatInfo->supportARB = GL_TRUE;

	    if (usePbuffer &&
		isSupportedWGL(pFormatInfo->supportedExtensions, "WGL_ARB_pbuffer")) {
		/* Get pbuffer entry points */
		pFormatInfo->wglCreatePbufferARB = (PFNWGLCREATEPBUFFERARBPROC)
		    wglGetProcAddress("wglCreatePbufferARB");
		pFormatInfo->wglGetPbufferDCARB = (PFNWGLGETPBUFFERDCARBPROC)
		    wglGetProcAddress("wglGetPbufferDCARB");
		pFormatInfo->wglReleasePbufferDCARB = (PFNWGLRELEASEPBUFFERDCARBPROC)
		    wglGetProcAddress("wglReleasePbufferDCARB");
		pFormatInfo->wglDestroyPbufferARB = (PFNWGLDESTROYPBUFFERARBPROC)
		    wglGetProcAddress("wglDestroyPbufferARB"); 
		pFormatInfo->wglQueryPbufferARB = (PFNWGLQUERYPBUFFERARBPROC)
		    wglGetProcAddress("wglQueryPbufferARB");		    
		
		if((pFormatInfo->wglCreatePbufferARB != NULL) &&
		   (pFormatInfo->wglGetPbufferDCARB != NULL) &&
		   (pFormatInfo->wglReleasePbufferDCARB != NULL) &&
		   (pFormatInfo->wglDestroyPbufferARB != NULL) &&
		   (pFormatInfo->wglQueryPbufferARB != NULL)) {
		    
		    pFormatInfo->supportPbuffer = GL_TRUE;
		    /* fprintf(stderr, "WGL support Pbuffer\n"); */
		    
		}
		else {
		    printErrorMessage("Problem in getting WGL_ARB_pbuffer functions !\n");
		}
	    }
	}
	else {
	    printErrorMessage("Problem in getting WGL_ARB_pixel_format functions !\n");
	}
    }
    
    return pFormatInfo;
}


JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_choosePixelFormat(
    JNIEnv   *env,
    jobject   obj,
    jlong     ctxInfo,
    jint      screen,
    jintArray attrList,
    jlongArray offScreenPFArray)
{
    static const BOOL debug = TRUE;
    static char szAppName[] = "Choose Pixel Format";

    int *mx_ptr;
    int dbVal;  /* value for double buffering */
    int stereoVal;   /* value for stereo */
    int antialiasVal; /* value for antialias */
    int stencilVal; /* value for stencil size */
    
    HWND  hwnd;
    HGLRC hrc;
    HDC   hdc;
    int pixelFormat;
    int wglAttrs[MAX_WGL_ATTRS_LENGTH];
    int index, lastIndex;
    PixelFormatInfo *pFormatInfo = NULL;
    jlong * offScreenPFListPtr;
    PIXELFORMATDESCRIPTOR dummy_pfd = getDummyPFD();
    
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

    pixelFormat = ChoosePixelFormat(hdc, &dummy_pfd);

    if (pixelFormat<1) {
	printErrorMessage("In NativeConfigTemplate : Failed in ChoosePixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    if (!SetPixelFormat(hdc, pixelFormat, NULL)) {
 	printErrorMessage("In NativeConfigTemplate : Failed in SetPixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;    
    }
    
    hrc = wglCreateContext(hdc);
    if (!hrc) {
	printErrorMessage("In NativeConfigTemplate : Failed in wglCreateContext");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }
    
    if (!wglMakeCurrent(hdc, hrc)) {
	printErrorMessage("In NativeConfigTemplate : Failed in wglMakeCurrent");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return -1;
    }

    pFormatInfo = newPixelFormatInfo(hdc, getJavaBoolEnv(env,"usePbuffer"));
    
    offScreenPFListPtr = (*env)->GetLongArrayElements(env, offScreenPFArray, NULL);

    if (pFormatInfo->supportARB) {
	
	/* fprintf(stderr, "Using non standard ChoosePixelFormat.\n"); */

	mx_ptr = (*env)->GetIntArrayElements(env, attrList, NULL);
	
	/*
	 * convert Java 3D values to WGL
	 */
	index = 0;
	
	wglAttrs[index++] = WGL_SUPPORT_OPENGL_ARB;
	wglAttrs[index++] = TRUE;
	wglAttrs[index++] = WGL_ACCELERATION_ARB; 
	wglAttrs[index++] = WGL_FULL_ACCELERATION_ARB;
	wglAttrs[index++] = WGL_DRAW_TO_WINDOW_ARB; 
	wglAttrs[index++] = TRUE;
	wglAttrs[index++] = WGL_RED_BITS_ARB;
	wglAttrs[index++] = mx_ptr[RED_SIZE];
	wglAttrs[index++] = WGL_GREEN_BITS_ARB;
	wglAttrs[index++] = mx_ptr[GREEN_SIZE];
	wglAttrs[index++] = WGL_BLUE_BITS_ARB;
	wglAttrs[index++] = mx_ptr[BLUE_SIZE];
	
	/* by MIK OF CLASSX */
	if (getJavaBoolEnv(env, "transparentOffScreen")) {
	    wglAttrs[index++] = WGL_ALPHA_BITS_ARB;
	    wglAttrs[index++] = 1;
	}

	wglAttrs[index++] = WGL_DEPTH_BITS_ARB;
	wglAttrs[index++] = mx_ptr[DEPTH_SIZE];
	
	lastIndex = index;
	
	dbVal = mx_ptr[DOUBLEBUFFER];
	stereoVal = mx_ptr[STEREO];
	antialiasVal = mx_ptr[ANTIALIASING];
	stencilVal = mx_ptr[STENCIL_SIZE];
	
	(*env)->ReleaseIntArrayElements(env, attrList, mx_ptr, JNI_ABORT);
	
	if(pFormatInfo->supportPbuffer) {	
	    
	    wglAttrs[index++] = WGL_DRAW_TO_PBUFFER_ARB;
	    wglAttrs[index++] = TRUE;
	    
	    /*
	     * Terminate by 2 zeros to avoid driver bugs
	     * that assume attributes always come in pairs.
	     */
	    wglAttrs[index] = 0;
	    wglAttrs[index+1] = 0;

  	    pFormatInfo->onScreenPFormat = find_DB_AA_S_S_PixelFormat( hdc, pFormatInfo,
								       wglAttrs, stereoVal,
								       dbVal, antialiasVal,
								       stencilVal, index);
	    
	    if(pFormatInfo->onScreenPFormat >= 0) {		
		/* Since the return pixel format support pbuffer,
		   we can use it for onScreen and offScreen. */
		pFormatInfo->drawToPbuffer = GL_TRUE;
		pFormatInfo->offScreenPFormat = pFormatInfo->onScreenPFormat;

		/* Fix to issue 77 */
		checkPixelFormat(hdc, pFormatInfo, GL_FALSE );
		
		offScreenPFListPtr[0] = (jlong) pFormatInfo;
		(*env)->ReleaseLongArrayElements(env, offScreenPFArray, offScreenPFListPtr, 0);

		/* Destroy all dummy objects */
		ReleaseDC(hwnd, hdc);
		wglDeleteContext(hrc);
		DestroyWindow(hwnd);
		UnregisterClass(szAppName, (HINSTANCE)NULL);

		return (jint) pFormatInfo->onScreenPFormat;
	    }
	}

	/* Create a onScreen without Pbuffer */
	pFormatInfo->drawToPbuffer = GL_FALSE;
	index = lastIndex;
	
	/*
	 * Terminate by 2 zeros to avoid driver bugs
	 * that assume attributes always come in pairs.
	 */
	wglAttrs[index] = 0;
	wglAttrs[index+1] = 0;
	
	pFormatInfo->onScreenPFormat = find_DB_AA_S_S_PixelFormat( hdc, pFormatInfo,
								   wglAttrs, stereoVal,
								   dbVal, antialiasVal,
								   stencilVal, index);
    } 
    else {
	 fprintf(stderr, "Fallback to use standard ChoosePixelFormat.\n");

	pFormatInfo->onScreenPFormat =  (jint) chooseSTDPixelFormat( env, screen,
								     attrList, hdc, GL_FALSE);
    }

    if(pFormatInfo->onScreenPFormat < 0) {
	/*
	printErrorMessage("In NativeConfigTemplate : Can't choose a onScreen pixel format");
	*/

	offScreenPFListPtr[0] = (jlong) pFormatInfo; 
	(*env)->ReleaseLongArrayElements(env, offScreenPFArray, offScreenPFListPtr, 0);

	/* We are done with dummy context, so destroy all dummy objects */
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);

	return -1;
    }    

    /* Fix to issue 77 */
    checkPixelFormat(hdc, pFormatInfo, GL_FALSE );
    
    /* Create offScreen with standard ChoosePixelFormat */
    pFormatInfo->offScreenPFormat = chooseSTDPixelFormat( env, screen, attrList, hdc, GL_TRUE);

    /* fprintf(stderr, "********* offScreenPFormat = %d\n", pFormatInfo->offScreenPFormat ); */

    /* Fix to issue 77 */
    if(pFormatInfo->offScreenPFormat >= 0) {
	checkPixelFormat(hdc, pFormatInfo, GL_TRUE );
    }
    
    offScreenPFListPtr[0] = (jlong) pFormatInfo;
    (*env)->ReleaseLongArrayElements(env, offScreenPFArray, offScreenPFListPtr, 0);

    /* We are done with dummy context, so destroy all dummy objects */
    ReleaseDC(hwnd, hdc);
    wglDeleteContext(hrc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);

    return (jint) pFormatInfo->onScreenPFormat;
}


JNIEXPORT 
void JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_freePixelFormatInfo(
     JNIEnv *env,
     jclass  class,	/* this is a static native method */
     jlong   pFormatInfo)
{
    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;
    if(pfInfo->supportedExtensions != NULL) {
	free(pfInfo->supportedExtensions);
	pfInfo->supportedExtensions = NULL;
    }
    pfInfo->wglChoosePixelFormatARB = NULL; 
    pfInfo->wglGetPixelFormatAttribivARB = NULL;
    pfInfo->wglCreatePbufferARB = NULL;
    pfInfo->wglGetPbufferDCARB = NULL;
    pfInfo->wglReleasePbufferDCARB = NULL;
    pfInfo->wglDestroyPbufferARB = NULL;
    pfInfo->wglQueryPbufferARB = NULL;
    free(pfInfo);
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isStereoAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    
    static GLboolean first_time = GL_TRUE;
    static GLboolean force_no_stereo = GL_FALSE;
    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;

    if (first_time) {
        if (getenv("J3D_NO_STEREO") != NULL) {
            fprintf(stderr, "Java 3D: stereo mode disabled\n");
            force_no_stereo = GL_TRUE;
        }
        first_time = GL_FALSE;
    }

    if (force_no_stereo)
        return JNI_FALSE;

    if(offScreen) {
	/* fprintf(stderr, "offScreen isStereoAvailable %d\n",
	   pfInfo->offScreenHasStereo); */
	
	return pfInfo->offScreenHasStereo;
    }
    else {
	/* fprintf(stderr, "onScreen isStereoAvailable %d\n",
	   pfInfo->onScreenHasStereo); */
	
	return pfInfo->onScreenHasStereo;
    }

}

JNIEXPORT jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_getStencilSize(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{

    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;
    if(offScreen) {
	/* fprintf(stderr, "offScreen getStencilSize %d\n",
	   pfInfo->offScreenStencilSize); */
	
	return pfInfo->offScreenStencilSize;
    }
    else {
	/* fprintf(stderr, "onScreen getStencilSize %d\n",
	   pfInfo->onScreenStencilSize); */
	
	return pfInfo->onScreenStencilSize;
    }

}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isDoubleBufferAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{

    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;
    if(offScreen) {
	/* fprintf(stderr, "offScreen isDoubleBufferAvailable %d\n",
	   pfInfo->offScreenHasDoubleBuffer); */
	
	return pfInfo->offScreenHasDoubleBuffer;
    }
    else {
	/* fprintf(stderr, "onScreen isDoubleBufferAvailable %d\n",
	   pfInfo->onScreenHasDoubleBuffer); */
	
	return pfInfo->onScreenHasDoubleBuffer;
    }

}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingAccumAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;
    if(offScreen) {
	/* fprintf(stderr, "offScreen isSceneAntialiasingAccumAvailable %d\n",
	   pfInfo->offScreenHasAccum); */
	
	return pfInfo->offScreenHasAccum;
    }
    else {
	/* fprintf(stderr, "onScreen isSceneAntialiasingAccumAvailable %d\n",
	   pfInfo->onScreenHasAccum); */
	
	return pfInfo->onScreenHasAccum;
    }
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen,
    jint screen)
{
    /* Fix to issue 77 */ 
    PixelFormatInfo *pfInfo = (PixelFormatInfo *) pFormatInfo;
    if(offScreen) {
	/* fprintf(stderr, "offScreen isSceneAntialiasingMultisampleAvailable %d\n", 
	   pfInfo->offScreenHasMultisample); */
	
	return pfInfo->offScreenHasMultisample;
    }
    else {
	/* fprintf(stderr, "onScreen isSceneAntialiasingMultisampleAvailable %d\n",
	   pfInfo->onScreenHasMultisample); */
	
	return pfInfo->onScreenHasMultisample;
    }
}
#endif /* WIN32 */
