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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jni.h>


#include "gldefs.h"

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


#ifndef FALSE
#define FALSE 0
#endif

#ifndef TRUE
#define TRUE 1
#endif

#ifndef BOOL
#define BOOL int
#endif

static char *gl_VERSION;
static char *gl_VENDOR;
                         
void initializeCtxInfo(JNIEnv *env, GraphicsContextPropertiesInfo* ctxInfo);
void cleanupCtxInfo(GraphicsContextPropertiesInfo* ctxInfo);

/*
 * Class:     javax_media_j3d_Canvas3D
 * Method:    getTextureColorTableSize
 * Signature: ()I
 */
int getTextureColorTableSize(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    char *extensionStr,
    int minorVersion);


#ifdef WIN32
  extern HDC getMonitorDC(int screen);
#endif

/*
 * extract the version numbers
 * when return , numbers[0] contains major version number
 * numbers[1] contains minor version number
 */
void extractVersionInfo(char *versionStr, int* numbers){
    char *majorNumStr;
    char *minorNumStr;
    majorNumStr = strtok(versionStr, (char *)".");
    minorNumStr = strtok(0, (char *)".");
    if (majorNumStr != NULL)
	numbers[0] = atoi(majorNumStr);
    if (minorNumStr != NULL)
	numbers[1] = atoi(minorNumStr);

    /* fprintf(stderr, "majorNumStr = %d, minNumStr = %d \n", numbers[0], numbers[1]); */
    return; 
}

/*
 * check if the extension is supported
 */
int isExtensionSupported(const char *allExtensions, const char *extension)
{
    const char *start;
    const char *where, *terminator;

    /* Extension names should not have spaces. */
    where = (const char *) strchr(extension, ' ');
    if (where || *extension == '\0')
	return 0;
    
    /*
     * It takes a bit of care to be fool-proof about parsing the
     * OpenGL extensions string. Don't be fooled by sub-strings,
     * etc.
     */
    start = allExtensions;
    for (;;) {
	where = (const char *) strstr((const char *) start, extension);
	if (!where)
	    break;
	terminator = where + strlen(extension);
	if (where == start || *(where - 1) == ' ')
	    if (*terminator == ' ' || *terminator == '\0')
		return 1;
	start = terminator;
    }
    return 0;
}

void checkTextureExtensions(
    JNIEnv *env,
    jobject obj,
    char *tmpExtensionStr,
    int versionNumber,
    GraphicsContextPropertiesInfo* ctxInfo) {

    if(isExtensionSupported(tmpExtensionStr, "GL_ARB_multitexture")) {
	ctxInfo->arb_multitexture = JNI_TRUE ;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_MULTI_TEXTURE;
        glGetIntegerv(GL_MAX_TEXTURE_UNITS_ARB, &ctxInfo->textureUnitCount);

    }
    
    if(isExtensionSupported(tmpExtensionStr,"GL_SGI_texture_color_table" )){
	ctxInfo->textureColorTableAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COLOR_TABLE;

	/* get texture color table size */
	/* need to check later */
	ctxInfo->textureColorTableSize = getTextureColorTableSize(env, obj, (jlong)ctxInfo, tmpExtensionStr, versionNumber);
	if (ctxInfo->textureColorTableSize <= 0) {
	    ctxInfo->textureColorTableAvailable = JNI_FALSE;
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_COLOR_TABLE;
	}
	if (ctxInfo->textureColorTableSize > 256) {
	    ctxInfo->textureColorTableSize = 256;
	}
    }

    if(isExtensionSupported(tmpExtensionStr,"GL_ARB_texture_env_combine" )){
	ctxInfo->textureEnvCombineAvailable = JNI_TRUE;
	ctxInfo->textureCombineSubtractAvailable = JNI_TRUE;

	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE_SUBTRACT;
	ctxInfo->combine_enum = GL_COMBINE_ARB;
	ctxInfo->combine_add_signed_enum = GL_ADD_SIGNED_ARB;
	ctxInfo->combine_interpolate_enum = GL_INTERPOLATE_ARB;
	ctxInfo->combine_subtract_enum = GL_SUBTRACT_ARB;

    } else if(isExtensionSupported(tmpExtensionStr,"GL_EXT_texture_env_combine" )){
	ctxInfo->textureEnvCombineAvailable = JNI_TRUE;
	ctxInfo->textureCombineSubtractAvailable = JNI_FALSE;

	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE;
	ctxInfo->combine_enum = GL_COMBINE_EXT;
	ctxInfo->combine_add_signed_enum = GL_ADD_SIGNED_EXT;
	ctxInfo->combine_interpolate_enum = GL_INTERPOLATE_EXT;

	/* EXT_texture_env_combine does not include subtract */
	ctxInfo->combine_subtract_enum = 0;
    }

    if(isExtensionSupported(tmpExtensionStr,"GL_NV_register_combiners" )) {
	ctxInfo->textureRegisterCombinersAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_REGISTER_COMBINERS;
#if defined(SOLARIS) || defined(__linux__)
       ctxInfo->glCombinerInputNV =
         (MYPFNGLCOMBINERINPUTNV) glCombinerInputNV;
       ctxInfo->glFinalCombinerInputNV =
         (MYPFNGLFINALCOMBINERINPUTNV) glFinalCombinerInputNV;
       ctxInfo->glCombinerOutputNV =
         (MYPFNGLCOMBINEROUTPUTNV) glCombinerOutputNV;
       ctxInfo->glCombinerParameterfvNV =
         (MYPFNGLCOMBINERPARAMETERFVNV) glCombinerParameterfvNV;
       ctxInfo->glCombinerParameterivNV =
          (MYPFNGLCOMBINERPARAMETERIVNV) glCombinerParameterivNV;
       ctxInfo->glCombinerParameterfNV =
         (MYPFNGLCOMBINERPARAMETERFNV) glCombinerParameterfNV;
       ctxInfo->glCombinerParameteriNV =
         (MYPFNGLCOMBINERPARAMETERINV) glCombinerParameteriNV;
       if (ctxInfo->glCombinerInputNV == NULL ||
           ctxInfo->glFinalCombinerInputNV == NULL ||
           ctxInfo->glCombinerOutputNV == NULL ||
           ctxInfo->glCombinerParameterfvNV == NULL ||
           ctxInfo->glCombinerParameterivNV == NULL ||
           ctxInfo->glCombinerParameterfNV == NULL ||
           ctxInfo->glCombinerParameteriNV == NULL) {
            /* lets play safe: */
           ctxInfo->textureExtMask &=
                ~javax_media_j3d_Canvas3D_TEXTURE_REGISTER_COMBINERS;
           ctxInfo->textureRegisterCombinersAvailable = JNI_FALSE;
       }
#endif

#ifdef WIN32
	ctxInfo->glCombinerInputNV = 
	  (MYPFNGLCOMBINERINPUTNV) wglGetProcAddress("glCombinerInputNV");
	ctxInfo->glFinalCombinerInputNV = 
	  (MYPFNGLFINALCOMBINERINPUTNV) wglGetProcAddress("glFinalCombinerInputNV");
	ctxInfo->glCombinerOutputNV = 
	  (MYPFNGLCOMBINEROUTPUTNV) wglGetProcAddress("glCombinerOutputNV");
	ctxInfo->glCombinerParameterfvNV = 
	  (MYPFNGLCOMBINERPARAMETERFVNV) wglGetProcAddress("glCombinerParameterfvNV");
	ctxInfo->glCombinerParameterivNV = 
	  (MYPFNGLCOMBINERPARAMETERIVNV) wglGetProcAddress("glCombinerParameterivNV");
	ctxInfo->glCombinerParameterfNV = 
	  (MYPFNGLCOMBINERPARAMETERFNV) wglGetProcAddress("glCombinerParameterfNV");
	ctxInfo->glCombinerParameteriNV = 
	  (MYPFNGLCOMBINERPARAMETERINV) wglGetProcAddress("glCombinerParameteriNV");

	/*
	if (ctxInfo->glCombinerInputNV == NULL) {
	    printf("glCombinerInputNV == NULL\n");
	}
	if (ctxInfo->glFinalCombinerInputNV == NULL) {
	    printf("glFinalCombinerInputNV == NULL\n");
	}
	if (ctxInfo->glCombinerOutputNV == NULL) {
	    printf("ctxInfo->glCombinerOutputNV == NULL\n");
	}
	if (ctxInfo->glCombinerParameterfvNV == NULL) {
	    printf("ctxInfo->glCombinerParameterfvNV == NULL\n");
	}
	if (ctxInfo->glCombinerParameterivNV == NULL) {
	    printf("ctxInfo->glCombinerParameterivNV == NULL\n");
	}
	if (ctxInfo->glCombinerParameterfNV == NULL) {
	    printf("ctxInfo->glCombinerParameterfNV == NULL\n");
	}
	if (ctxInfo->glCombinerParameteriNV == NULL) {
	    printf("ctxInfo->glCombinerParameteriNV == NULL\n");
	}
	*/
	if ((ctxInfo->glCombinerInputNV == NULL) ||
	    (ctxInfo->glFinalCombinerInputNV == NULL) ||
	    (ctxInfo->glCombinerOutputNV == NULL) ||
	    (ctxInfo->glCombinerParameterfvNV == NULL) ||
	    (ctxInfo->glCombinerParameterivNV == NULL) ||
	    (ctxInfo->glCombinerParameterfNV == NULL) ||
	    (ctxInfo->glCombinerParameteriNV == NULL)) {
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_REGISTER_COMBINERS;	    
	    ctxInfo->textureRegisterCombinersAvailable = JNI_FALSE;
	}
	    
#endif

    }

    if(isExtensionSupported(tmpExtensionStr,"GL_ARB_texture_env_dot3" )) {
	ctxInfo->textureCombineDot3Available = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE_DOT3;
	ctxInfo->combine_dot3_rgb_enum = GL_DOT3_RGB_ARB;
	ctxInfo->combine_dot3_rgba_enum = GL_DOT3_RGBA_ARB;
    } else if(isExtensionSupported(tmpExtensionStr,"GL_EXT_texture_env_dot3" )) {
	ctxInfo->textureCombineDot3Available = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE_DOT3;
	ctxInfo->combine_dot3_rgb_enum = GL_DOT3_RGB_EXT;
	ctxInfo->combine_dot3_rgba_enum = GL_DOT3_RGBA_EXT;
    }

    if (isExtensionSupported(tmpExtensionStr, "GL_ARB_texture_cube_map")) {
	ctxInfo->texture_cube_map_ext_enum = GL_TEXTURE_CUBE_MAP_ARB;
	ctxInfo->textureCubeMapAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_CUBE_MAP;
    } else if (isExtensionSupported(tmpExtensionStr, "GL_EXT_texture_cube_map")) {
	ctxInfo->texture_cube_map_ext_enum = GL_TEXTURE_CUBE_MAP_EXT;
	ctxInfo->textureCubeMapAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_CUBE_MAP;
    }

    if (isExtensionSupported(tmpExtensionStr, "GL_SGIS_sharpen_texture")) {
	ctxInfo->textureSharpenAvailable = JNI_TRUE;
        ctxInfo->linear_sharpen_enum = GL_LINEAR_SHARPEN_SGIS;
        ctxInfo->linear_sharpen_rgb_enum = GL_LINEAR_SHARPEN_COLOR_SGIS;
        ctxInfo->linear_sharpen_alpha_enum = GL_LINEAR_SHARPEN_ALPHA_SGIS;
 	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_SHARPEN;
#if defined(SOLARIS) || defined(__linux__)
	ctxInfo->glSharpenTexFuncSGIS = 
		(MYPFNGLSHARPENTEXFUNCSGI) glSharpenTexFuncSGIS;
#endif
#ifdef WIN32
	ctxInfo->glSharpenTexFuncSGIS = (MYPFNGLSHARPENTEXFUNCSGI) 
			wglGetProcAddress("glSharpenTexFuncSGIS");
	if (ctxInfo->glSharpenTexFuncSGIS == NULL) {
	    /*	    printf("ctxInfo->glSharpenTexFuncSGIS == NULL\n"); */
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_SHARPEN;
	    ctxInfo->textureSharpenAvailable = JNI_FALSE;
	}
#endif
    }

    if (isExtensionSupported(tmpExtensionStr, "GL_SGIS_detail_texture")) {
	ctxInfo->textureDetailAvailable = JNI_TRUE;
	ctxInfo->texture_detail_ext_enum = GL_DETAIL_TEXTURE_2D_SGIS;
        ctxInfo->linear_detail_enum = GL_LINEAR_DETAIL_SGIS;
        ctxInfo->linear_detail_rgb_enum = GL_LINEAR_DETAIL_COLOR_SGIS;
        ctxInfo->linear_detail_alpha_enum = GL_LINEAR_DETAIL_ALPHA_SGIS;
	ctxInfo->texture_detail_mode_enum = GL_DETAIL_TEXTURE_MODE_SGIS;
	ctxInfo->texture_detail_level_enum = GL_DETAIL_TEXTURE_LEVEL_SGIS;
 	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_DETAIL;
#if defined(SOLARIS) || defined(__linux__)
	ctxInfo->glDetailTexFuncSGIS = 
		(MYPFNGLDETAILTEXFUNCSGI) glDetailTexFuncSGIS;
#endif
#ifdef WIN32
	ctxInfo->glDetailTexFuncSGIS = (MYPFNGLDETAILTEXFUNCSGI) 
			wglGetProcAddress("glDetailTexFuncSGIS");
	if (ctxInfo->glDetailTexFuncSGIS == NULL) {
	    /*	    printf("ctxInfo->glDetailTexFuncSGIS == NULL\n"); */
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_DETAIL;	    
	    ctxInfo->textureDetailAvailable = JNI_FALSE;
	}
#endif
    }

    if (isExtensionSupported(tmpExtensionStr, "GL_SGIS_texture_filter4")) {
	ctxInfo->textureFilter4Available = JNI_TRUE;
        ctxInfo->filter4_enum = GL_FILTER4_SGIS;
 	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_FILTER4;
#if defined(SOLARIS) || defined(__linux__)
	ctxInfo->glTexFilterFuncSGIS = 
		(MYPFNGLTEXFILTERFUNCSGI) glTexFilterFuncSGIS;
#endif
#ifdef WIN32
	ctxInfo->glTexFilterFuncSGIS = (MYPFNGLTEXFILTERFUNCSGI) 
			wglGetProcAddress("glTexFilterFuncSGIS");
	if (ctxInfo->glTexFilterFuncSGIS == NULL) {
	    /*	    printf("ctxInfo->glTexFilterFuncSGIS == NULL\n"); */
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_FILTER4;	    
	    ctxInfo->textureFilter4Available = JNI_FALSE;
	}
#endif
    }

    if (isExtensionSupported(tmpExtensionStr, 
				"GL_EXT_texture_filter_anisotropic")) {
	ctxInfo->textureAnisotropicFilterAvailable = JNI_TRUE;
	ctxInfo->texture_filter_anisotropic_ext_enum = 
				GL_TEXTURE_MAX_ANISOTROPY_EXT;
        ctxInfo->max_texture_filter_anisotropy_enum =
				GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT;
	ctxInfo->textureExtMask |= 
			javax_media_j3d_Canvas3D_TEXTURE_ANISOTROPIC_FILTER;
    }

    if (isExtensionSupported(tmpExtensionStr,
				"GL_ARB_texture_border_clamp")) {
	ctxInfo->texture_clamp_to_border_enum = GL_CLAMP_TO_BORDER_ARB;
    } else if (isExtensionSupported(tmpExtensionStr,
				"GL_SGIS_texture_border_clamp")) {
	ctxInfo->texture_clamp_to_border_enum = GL_CLAMP_TO_BORDER_SGIS;
    } else {
	ctxInfo->texture_clamp_to_border_enum = GL_CLAMP;
    }
    
    if (isExtensionSupported(tmpExtensionStr,
				"GL_SGIX_texture_lod_bias")) {
	ctxInfo->textureLodBiasAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |=
			javax_media_j3d_Canvas3D_TEXTURE_LOD_OFFSET;
    }
}

BOOL getJavaBoolEnv(JNIEnv *env, char* envStr)
{
    JNIEnv table = *env;
    jclass cls;
    jfieldID fieldID;
    jobject obj;
    
    cls = (jclass) (*(table->FindClass))(env, "javax/media/j3d/VirtualUniverse");

    if (cls == NULL) {
	return FALSE;
    }
    
    fieldID = (jfieldID) (*(table->GetStaticFieldID))(env, cls, "mc",
						      "Ljavax/media/j3d/MasterControl;");
    if (fieldID == NULL) {
	return FALSE;	
    }

    obj = (*(table->GetStaticObjectField))(env, cls, fieldID);

    if (obj == NULL) {
	return FALSE;
    }

    cls = (jclass) (*(table->FindClass))(env, "javax/media/j3d/MasterControl");    

    if (cls == NULL) {
	return FALSE;
    }

    fieldID = (jfieldID) (*(table->GetFieldID))(env, cls, envStr, "Z");

    if (fieldID == NULL ) {
	return FALSE;
    }

    return (*(table->GetBooleanField))(env, obj, fieldID);
}


/*
 * get properties from current context
 */
BOOL getPropertiesFromCurrentContext(
    JNIEnv *env,
    jobject obj,
    GraphicsContextPropertiesInfo *ctxInfo,
    jlong hdc,
    int pixelFormat,
    long display,
    jlong vinfo)
{
    JNIEnv table = *env; 

    /* version and extension */
    char *glversion;
    char *extensionStr;
    char *tmpVersionStr;
    char *tmpExtensionStr;
    int   versionNumbers[2];
    char *cgHwStr = 0;
    int stencilSize;
    
#ifdef WIN32
    PFNWGLGETPIXELFORMATATTRIBIVEXTPROC wglGetPixelFormatAttribivEXT = NULL;
    PIXELFORMATDESCRIPTOR pfd;
    int attr[3];
    int piValues[2];
#endif
    
    /* get OpenGL version */
    glversion = (char *)glGetString(GL_VERSION);
    if (glversion == NULL) {
	fprintf(stderr, "glversion == null\n");
	return FALSE;
    }
    gl_VERSION = glversion;
    tmpVersionStr = strdup(glversion);
    gl_VENDOR = (char *)glGetString(GL_VENDOR);
    if (gl_VENDOR == NULL) {
        gl_VENDOR = "<unkown vendor>";
    }
    
    /* Get the extension */
    extensionStr = (char *)glGetString(GL_EXTENSIONS);
    if (extensionStr == NULL) {
        fprintf(stderr, "extensionStr == null\n");
        return FALSE;
    }
    tmpExtensionStr = strdup(extensionStr);
    
    ctxInfo->versionStr = strdup(glversion);
    ctxInfo->extensionStr = strdup(extensionStr);

    
    /* find out the version, major and minor version number */
    extractVersionInfo(tmpVersionStr, versionNumbers);

    /* *********************************************************/
    /* setup the graphics context properties */
    if (versionNumbers[1] >= 2) { /* check 1.2 core and above */
	/* 1.2 core */
        ctxInfo->rescale_normal_ext = JNI_TRUE;
	ctxInfo->rescale_normal_ext_enum = GL_RESCALE_NORMAL;
	ctxInfo->bgr_ext = JNI_TRUE;
	ctxInfo->bgr_ext_enum = GL_BGR;
	ctxInfo->texture3DAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_3D;
#if defined(SOLARIS) || defined(__linux__)
	ctxInfo->glTexImage3DEXT = (MYPFNGLTEXIMAGE3DPROC )glTexImage3D;
	ctxInfo->glTexSubImage3DEXT = (MYPFNGLTEXSUBIMAGE3DPROC )glTexSubImage3D;
#endif
#ifdef WIN32
	ctxInfo->glTexImage3DEXT = (MYPFNGLTEXIMAGE3DPROC )wglGetProcAddress("glTexImage3D");
	ctxInfo->glTexSubImage3DEXT = (MYPFNGLTEXSUBIMAGE3DPROC )wglGetProcAddress("glTexSubImage3D");
	if ((ctxInfo->glTexImage3DEXT == NULL) || (ctxInfo->glTexSubImage3DEXT == NULL)) {
	    ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_3D;
	    ctxInfo->texture3DAvailable = JNI_FALSE;
	}
#endif
	ctxInfo->texture_3D_ext_enum = GL_TEXTURE_3D;
	ctxInfo->texture_wrap_r_ext_enum = GL_TEXTURE_WRAP_R;
	ctxInfo->texture_clamp_to_edge_enum = GL_CLAMP_TO_EDGE;

	if(isExtensionSupported(tmpExtensionStr, "GL_ARB_imaging")){	
	    ctxInfo->blend_color_ext = JNI_TRUE;
	    ctxInfo->blendFunctionTable[7] = GL_CONSTANT_COLOR;
#if defined(SOLARIS) || defined(__linux__)
	    ctxInfo->glBlendColor = (MYPFNGLBLENDCOLORPROC )glBlendColor;
#endif
#ifdef WIN32	    
	    ctxInfo->glBlendColor = (MYPFNGLBLENDCOLORPROC )wglGetProcAddress("glBlendColor");
	    if (ctxInfo->glBlendColor == NULL) {
		ctxInfo->blend_color_ext = JNI_FALSE;
	    }
#endif
	}
	
	ctxInfo->seperate_specular_color = JNI_TRUE;
	ctxInfo->light_model_color_control_enum =  GL_LIGHT_MODEL_COLOR_CONTROL;
	ctxInfo->single_color_enum = GL_SINGLE_COLOR;
	ctxInfo->seperate_specular_color_enum =  GL_SEPARATE_SPECULAR_COLOR; 

	ctxInfo->textureLodAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_LOD_RANGE;
	ctxInfo->texture_min_lod_enum = GL_TEXTURE_MIN_LOD;
	ctxInfo->texture_max_lod_enum = GL_TEXTURE_MAX_LOD;
	ctxInfo->texture_base_level_enum = GL_TEXTURE_BASE_LEVEL;
	ctxInfo->texture_max_level_enum = GL_TEXTURE_MAX_LEVEL;

	/* ...  */
	
    } else { /* check 1.1 extension */
	if(isExtensionSupported(tmpExtensionStr,"GL_EXT_rescale_normal")){
   	    ctxInfo->rescale_normal_ext = JNI_TRUE;
	    ctxInfo->rescale_normal_ext_enum = GL_RESCALE_NORMAL_EXT;
	}
	if(isExtensionSupported(tmpExtensionStr,"GL_BGR_EXT")) {
	    ctxInfo->bgr_ext = 1;
	    ctxInfo->bgr_ext_enum = GL_BGR_EXT;
	}
	
	if(isExtensionSupported(tmpExtensionStr,"GL_EXT_texture3D" )){
	    ctxInfo->texture3DAvailable = JNI_TRUE;
	    ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_3D;
	    ctxInfo->texture_3D_ext_enum = GL_TEXTURE_3D_EXT;
	    ctxInfo->texture_wrap_r_ext_enum = GL_TEXTURE_WRAP_R_EXT;
#if defined(SOLARIS) || defined(__linux__)
	    ctxInfo->glTexImage3DEXT = (MYPFNGLTEXIMAGE3DPROC )glTexImage3DEXT;
	    ctxInfo->glTexSubImage3DEXT = (MYPFNGLTEXSUBIMAGE3DPROC )glTexSubImage3DEXT;
            /* Fallback to non-EXT variants, needed for older
               NVIDIA drivers which announce GL_EXT_texture3D but
               don't have the EXT variants */
           if (ctxInfo->glTexImage3DEXT == NULL ||
                ctxInfo->glTexSubImage3DEXT == NULL) {

                ctxInfo->glTexImage3DEXT =
                    (MYPFNGLTEXIMAGE3DPROC) glTexImage3D;
                ctxInfo->glTexSubImage3DEXT =
                    (MYPFNGLTEXSUBIMAGE3DPROC) glTexSubImage3D;

                if (ctxInfo->glTexImage3DEXT == NULL ||
                    ctxInfo->glTexSubImage3DEXT == NULL) {

                    ctxInfo->textureExtMask &=
                        ~javax_media_j3d_Canvas3D_TEXTURE_3D;
                    ctxInfo->texture3DAvailable = JNI_FALSE;
                }
            }

#endif
#ifdef WIN32
	    ctxInfo->glTexImage3DEXT = (MYPFNGLTEXIMAGE3DPROC )wglGetProcAddress("glTexImage3DEXT");
	    ctxInfo->glTexSubImage3DEXT = (MYPFNGLTEXSUBIMAGE3DPROC )wglGetProcAddress("glTexSubImage3DEXT");
	    if ((ctxInfo->glTexImage3DEXT == NULL) || (ctxInfo->glTexSubImage3DEXT == NULL)) {
		ctxInfo->textureExtMask &= ~javax_media_j3d_Canvas3D_TEXTURE_3D;
		ctxInfo->texture3DAvailable = JNI_FALSE;
	    }   
#endif	    
	}


	if(isExtensionSupported(tmpExtensionStr, "GL_EXT_texture_edge_clamp")) {
	    ctxInfo->texture_clamp_to_edge_enum = GL_CLAMP_TO_EDGE_EXT;
	} else if(isExtensionSupported(tmpExtensionStr, "GL_SGIS_texture_edge_clamp")) {
	    ctxInfo->texture_clamp_to_edge_enum = GL_CLAMP_TO_EDGE_SGIS;
	} else {
	    /* fallback to GL_CLAMP */
	    ctxInfo->texture_clamp_to_edge_enum = GL_CLAMP;
	}
	    

	if(isExtensionSupported(tmpExtensionStr, "GL_EXT_blend_color")){
	    ctxInfo->blend_color_ext = JNI_TRUE;
#if defined(SOLARIS) || defined(__linux__)
	    ctxInfo->glBlendColor = (MYPFNGLBLENDCOLOREXTPROC )glBlendColorEXT;
#endif
#ifdef WIN32	    
	    ctxInfo->glBlendColor = (MYPFNGLBLENDCOLOREXTPROC )wglGetProcAddress("glBlendColorEXT");
	    if (ctxInfo->glBlendColor == NULL) {
		ctxInfo->blend_color_ext = JNI_FALSE;
	    }
#endif
	    ctxInfo->blendFunctionTable[7] = GL_CONSTANT_COLOR_EXT;
	}
	
	if(isExtensionSupported(tmpExtensionStr,"GL_EXT_separate_specular_color" )){
	    ctxInfo->seperate_specular_color = JNI_TRUE;
	    ctxInfo->light_model_color_control_enum =  GL_LIGHT_MODEL_COLOR_CONTROL_EXT;
	    ctxInfo->single_color_enum = GL_SINGLE_COLOR_EXT;
	    ctxInfo->seperate_specular_color_enum =  GL_SEPARATE_SPECULAR_COLOR_EXT ;
	}

	if (isExtensionSupported(tmpExtensionStr,"GL_SGIS_texture_lod")) {
	    ctxInfo->textureLodAvailable = JNI_TRUE;
	    ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_LOD_RANGE;
	    ctxInfo->texture_min_lod_enum = GL_TEXTURE_MIN_LOD_SGIS;
	    ctxInfo->texture_max_lod_enum = GL_TEXTURE_MAX_LOD_SGIS;
	    ctxInfo->texture_base_level_enum = GL_TEXTURE_BASE_LEVEL_SGIS;
	    ctxInfo->texture_max_level_enum = GL_TEXTURE_MAX_LEVEL_SGIS;
	}


  	/* ... */
    }


	
    /* check extensions for remaining of 1.1 and 1.2 */
    if(isExtensionSupported(tmpExtensionStr, "GL_EXT_multi_draw_arrays")){
	ctxInfo->multi_draw_arrays_ext = JNI_TRUE;
    }
    if(isExtensionSupported(tmpExtensionStr, "GL_SUN_multi_draw_arrays")){
	ctxInfo->multi_draw_arrays_sun = JNI_TRUE;
    }


    if (isExtensionSupported(tmpExtensionStr, "GL_EXT_compiled_vertex_array") &&
	getJavaBoolEnv(env, "isCompiledVertexArray")) {
	ctxInfo->compiled_vertex_array_ext = JNI_TRUE;
    }


    if(isExtensionSupported(tmpExtensionStr, "GLX_SUN_video_resize")){
	ctxInfo->videoResizeAvailable = JNI_TRUE;
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_SUN_VIDEO_RESIZE;
    }
    
    if(isExtensionSupported(tmpExtensionStr, "GL_SUN_global_alpha")){
	ctxInfo->global_alpha_sun = JNI_TRUE;
    }

    if(isExtensionSupported(tmpExtensionStr, "GL_SUNX_constant_data")){
	ctxInfo->constant_data_sun = JNI_TRUE;
    }

    if(isExtensionSupported(tmpExtensionStr, "GL_EXT_abgr")) {
	ctxInfo->abgr_ext = JNI_TRUE;
    }
    
    if(isExtensionSupported(tmpExtensionStr, "GL_ARB_transpose_matrix")) {
	ctxInfo->arb_transpose_matrix = JNI_TRUE;
    }

    if(isExtensionSupported(tmpExtensionStr, "GL_SUNX_geometry_compression")) {
	ctxInfo->geometry_compression_sunx = JNI_TRUE ;
    }
    
#if defined(SOLARIS) || defined(__linux__)
    /*
     * setup ARB_multisample, under windows this is setup in
     * NativeConfigTemplate when pixel format is choose
     */
    if (isExtensionSupported(tmpExtensionStr, "GL_ARB_multisample")){
	ctxInfo->arb_multisample = JNI_TRUE;

    }
#endif

#ifdef WIN32
    wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	wglGetProcAddress("wglGetPixelFormatAttribivARB");

    if (wglGetPixelFormatAttribivEXT == NULL) {
	wglGetPixelFormatAttribivEXT = (PFNWGLGETPIXELFORMATATTRIBIVEXTPROC)
	    wglGetProcAddress("wglGetPixelFormatAttribivEXT");
    } 

    ctxInfo->arb_multisample = JNI_FALSE;    
    if (wglGetPixelFormatAttribivEXT != NULL) {
	attr[0] = WGL_SAMPLE_BUFFERS_ARB;
	attr[1] = WGL_SAMPLES_ARB;
	attr[2] = 0;

	if (wglGetPixelFormatAttribivEXT((HDC) hdc, pixelFormat, 0, 2, attr, piValues)) {
	    if ((piValues[0] == TRUE) && (piValues[1] > 1)) {
		ctxInfo->arb_multisample = JNI_TRUE;
	    }
	} 
    }
#endif
    
    /*
     * Disable multisample by default since OpenGL will enable
     * it by default if the surface is multisample capable.
     */
    if (ctxInfo->arb_multisample && !ctxInfo->implicit_multisample) {
	glDisable(MULTISAMPLE_ARB);
    }
    /*
     * checking of the texture extensions is done in checkTextureExtensions(),
     * so that the same function can be used for queryContext as well
     */
    checkTextureExtensions(env, obj, tmpExtensionStr, versionNumbers[1],
				ctxInfo);



    
    /* ... */
    
    /* *********************************************************/
    /* Set up rescale_normal if extension supported */
    if (ctxInfo->rescale_normal_ext ) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_RESCALE_NORMAL;
    }

    /* Setup the multi_draw_array */
    if(ctxInfo->multi_draw_arrays_ext) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_MULTI_DRAW_ARRAYS;
    } else if (ctxInfo->multi_draw_arrays_sun) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_SUN_MULTI_DRAW_ARRAYS;
    }
    if(ctxInfo->compiled_vertex_array_ext) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_COMPILED_VERTEX_ARRAYS;
    }
    

    /* Setup GL_SUN_gloabl_alpha */
    if (ctxInfo->global_alpha_sun) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_SUN_GLOBAL_ALPHA;
    }

    /* Setup GL_SUNX_constant_data */
    if (ctxInfo->constant_data_sun) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_SUN_CONSTANT_DATA;
    }

    /* Setup GL_EXT_abgr */
    if (ctxInfo->abgr_ext) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_ABGR;
    }

    /* Setup GL_BGR_EXT */
    if (ctxInfo->bgr_ext) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_BGR;
    }

    /* Setup  GL_ARB_transpose_matrix */
    if (ctxInfo->arb_transpose_matrix) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_ARB_TRANSPOSE_MATRIX;
    }
    
    /*
     * Check for compressed geometry extensions and see if hardware
     * acceleration is supported in the runtime environment.
     */
    if (ctxInfo->geometry_compression_sunx) {
	cgHwStr = (char *)glGetString(GL_COMPRESSED_GEOM_ACCELERATED_SUNX) ;
    }

    if (cgHwStr == 0 || strstr(cgHwStr, " ")) {
	ctxInfo->geometry_compression_accelerated = 0 ;
       
    } else {
	char *tmp = strdup(cgHwStr) ;

	ctxInfo->geometry_compression_accelerated_major_version =
	    atoi(strtok(tmp, ".")) ;
	ctxInfo->geometry_compression_accelerated_minor_version =
	    atoi(strtok(0, ".")) ;
	ctxInfo->geometry_compression_accelerated_subminor_version =
	    atoi(strtok(0, ".")) ;

	free(tmp) ;
	ctxInfo->geometry_compression_accelerated = 1 ;
    }


    /* Setup GL_EXT_separate_specular_color */
    if(ctxInfo->seperate_specular_color) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_EXT_SEPARATE_SPECULAR_COLOR;
    }
    
    if (ctxInfo->constant_data_sun) {
	/*        glPixelStorei(GL_UNPACK_CONSTANT_DATA_SUNX, GL_TRUE); */
    }
    
    if(ctxInfo->arb_multisample) {
	ctxInfo->extMask |= javax_media_j3d_Canvas3D_ARB_MULTISAMPLE;
    }
    
    /* setup those functions pointers */
#ifdef WIN32
   
    if (ctxInfo->multi_draw_arrays_ext) {
	ctxInfo->glMultiDrawArraysEXT = (MYPFNGLMULTIDRAWARRAYSEXTPROC)wglGetProcAddress("glMultiDrawArraysEXT");
	ctxInfo->glMultiDrawElementsEXT = (MYPFNGLMULTIDRAWELEMENTSEXTPROC)wglGetProcAddress("glMultiDrawElementsEXT");
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;
	}
    }
    else if (ctxInfo->multi_draw_arrays_sun) {
	ctxInfo->glMultiDrawArraysEXT = (MYPFNGLMULTIDRAWARRAYSEXTPROC)wglGetProcAddress("glMultiDrawArraysSUN");
	ctxInfo->glMultiDrawElementsEXT = (MYPFNGLMULTIDRAWELEMENTSEXTPROC)wglGetProcAddress("glMultiDrawElementsSUN");
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_sun = JNI_FALSE;
	}
	    
    }
    if (ctxInfo->compiled_vertex_array_ext) {
	ctxInfo->glLockArraysEXT = (MYPFNGLLOCKARRAYSEXTPROC)wglGetProcAddress("glLockArraysEXT");
	ctxInfo->glUnlockArraysEXT = (MYPFNGLUNLOCKARRAYSEXTPROC)wglGetProcAddress("glUnlockArraysEXT");
	if ((ctxInfo->glLockArraysEXT == NULL) ||
	    (ctxInfo->glUnlockArraysEXT == NULL)) {
	    ctxInfo->compiled_vertex_array_ext = JNI_FALSE;
	}
    }

    if (ctxInfo->arb_multitexture) {
	ctxInfo->glClientActiveTextureARB = (MYPFNGLCLIENTACTIVETEXTUREARBPROC)wglGetProcAddress("glClientActiveTextureARB");
	ctxInfo->glMultiTexCoord2fvARB = (MYPFNGLMULTITEXCOORD2FVARBPROC)wglGetProcAddress("glMultiTexCoord2fvARB");
	ctxInfo->glMultiTexCoord3fvARB = (MYPFNGLMULTITEXCOORD3FVARBPROC)wglGetProcAddress("glMultiTexCoord3fvARB");
	ctxInfo->glMultiTexCoord4fvARB = (MYPFNGLMULTITEXCOORD4FVARBPROC)wglGetProcAddress("glMultiTexCoord4fvARB");
	ctxInfo->glActiveTextureARB = (MYPFNGLACTIVETEXTUREARBPROC) wglGetProcAddress("glActiveTextureARB");
	/*
	if (ctxInfo->glClientActiveTextureARB == NULL) {
	    printf("ctxInfo->glClientActiveTextureARB == NULL\n");
	}
	if (ctxInfo->glMultiTexCoord2fvARB == NULL) {
	    printf("ctxInfo->glMultiTexCoord2fvARB == NULL\n");
	}
	if (ctxInfo->glMultiTexCoord3fvARB == NULL) {
	    printf("ctxInfo->glMultiTexCoord3fvARB == NULL\n");
	}
	if (ctxInfo->glMultiTexCoord4fvARB == NULL) {
	    printf("ctxInfo->glMultiTexCoord4fvARB == NULL\n");
	}
	if (ctxInfo->glActiveTextureARB == NULL) {
	    printf("ctxInfo->glActiveTextureARB == NULL\n");
	}
	*/  
	if ((ctxInfo->glClientActiveTextureARB == NULL) ||
	    (ctxInfo->glMultiTexCoord2fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord3fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord4fvARB == NULL) ||
	    (ctxInfo->glActiveTextureARB == NULL)) {
	    ctxInfo->arb_multitexture = JNI_FALSE;
	}
    }
    
    if(ctxInfo->arb_transpose_matrix) {
	ctxInfo->glLoadTransposeMatrixdARB = (MYPFNGLLOADTRANSPOSEMATRIXDARBPROC)wglGetProcAddress("glLoadTransposeMatrixdARB");
	ctxInfo->glMultTransposeMatrixdARB = (MYPFNGLMULTTRANSPOSEMATRIXDARBPROC)wglGetProcAddress("glMultTransposeMatrixdARB");
	/*
	if (ctxInfo->glLoadTransposeMatrixdARB == NULL) {
	    printf("ctxInfo->glLoadTransposeMatrixdARB == NULL\n");
	}
	if (ctxInfo->glMultTransposeMatrixdARB == NULL) {
	    printf("ctxInfo->glMultTransposeMatrixdARB == NULL\n");
	}
	*/
	if ((ctxInfo->glLoadTransposeMatrixdARB == NULL) ||
	    (ctxInfo->glMultTransposeMatrixdARB == NULL)) {
	    ctxInfo->arb_transpose_matrix = JNI_FALSE;
	}
    }

    if (ctxInfo->global_alpha_sun) {
	ctxInfo->glGlobalAlphaFactorfSUN = (MYPFNGLGLOBALALPHAFACTORFSUNPROC )wglGetProcAddress("glGlobalAlphaFactorfSUN");

	if (ctxInfo->glGlobalAlphaFactorfSUN == NULL) {
	    /* printf("ctxInfo->glGlobalAlphaFactorfSUN == NULL\n");*/
	    ctxInfo->global_alpha_sun = JNI_FALSE;
	}
    }


    DescribePixelFormat((HDC) hdc, pixelFormat, sizeof(pfd), &pfd);

    stencilSize = pfd.cStencilBits;
#endif
    
#if defined(SOLARIS) || defined(__linux__)
    if(ctxInfo->multi_draw_arrays_ext) {
	ctxInfo->glMultiDrawArraysEXT = glMultiDrawArraysEXT;
	ctxInfo->glMultiDrawElementsEXT = glMultiDrawElementsEXT;
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;
	}
    }
    else if (ctxInfo->multi_draw_arrays_sun) {
	ctxInfo->glMultiDrawArraysEXT = glMultiDrawArraysSUN;
	ctxInfo->glMultiDrawElementsEXT = glMultiDrawElementsSUN;
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;
	}
    }
    if(ctxInfo->compiled_vertex_array_ext) {
	ctxInfo->glLockArraysEXT = glLockArraysEXT;
	ctxInfo->glUnlockArraysEXT = glUnlockArraysEXT;
	if ((ctxInfo->glLockArraysEXT == NULL) ||
	    (ctxInfo->glUnlockArraysEXT == NULL)) {
	    ctxInfo->compiled_vertex_array_ext = JNI_FALSE;
	}
    }
    
    if(ctxInfo->arb_multitexture){
	ctxInfo->glClientActiveTextureARB = glClientActiveTextureARB;
	ctxInfo->glMultiTexCoord2fvARB = glMultiTexCoord2fvARB;
	ctxInfo->glMultiTexCoord3fvARB = glMultiTexCoord3fvARB;
	ctxInfo->glMultiTexCoord4fvARB = glMultiTexCoord4fvARB;
	ctxInfo->glActiveTextureARB = glActiveTextureARB;
	if ((ctxInfo->glClientActiveTextureARB == NULL) ||
	    (ctxInfo->glMultiTexCoord2fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord3fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord4fvARB == NULL) ||
	    (ctxInfo->glActiveTextureARB == NULL)) {
	    ctxInfo->arb_multitexture = JNI_FALSE;
	}
    }
    if(ctxInfo->arb_transpose_matrix) {
	ctxInfo->glLoadTransposeMatrixdARB = glLoadTransposeMatrixdARB;
	ctxInfo->glMultTransposeMatrixdARB = glMultTransposeMatrixdARB; 
	if ((ctxInfo->glLoadTransposeMatrixdARB == NULL) ||
	    (ctxInfo->glMultTransposeMatrixdARB == NULL)) {
	    ctxInfo->arb_transpose_matrix = JNI_FALSE;
	}
    }
    if(ctxInfo->global_alpha_sun) {
	ctxInfo->glGlobalAlphaFactorfSUN = glGlobalAlphaFactorfSUN;
	if (ctxInfo->glGlobalAlphaFactorfSUN == NULL) {
	    ctxInfo->global_alpha_sun = JNI_FALSE;
	}
    }

    glXGetConfig((Display *) display, (XVisualInfo *) vinfo, GLX_STENCIL_SIZE, &stencilSize);
    
#endif

    if (stencilSize > 1) {
      ctxInfo->extMask |= javax_media_j3d_Canvas3D_STENCIL_BUFFER;
    }

    
    /* ... */
    
    /* clearing up the memory */
    free(tmpExtensionStr);
    free(tmpVersionStr);
    return TRUE;
}


/*
 * put properties to the java side
 */
void setupCanvasProperties(
    JNIEnv *env, 
    jobject obj,
    GraphicsContextPropertiesInfo *ctxInfo) 
{
    jclass cv_class;
    jfieldID rsc_field;
    JNIEnv table = *env;
    GLint param;
    
    cv_class =  (jclass) (*(table->GetObjectClass))(env, obj);
    
    /* set the canvas.multiTexAccelerated flag */
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "multiTexAccelerated", "Z");
    (*(table->SetBooleanField))(env, obj, rsc_field, ctxInfo->arb_multitexture);

    if (ctxInfo->arb_multitexture) {
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "numTexUnitSupported", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->textureUnitCount);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "numTexCoordSupported", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->textureUnitCount);
    }
    
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "extensionsSupported", "I");
    (*(table->SetIntField))(env, obj, rsc_field, ctxInfo->extMask);

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "textureExtendedFeatures", "I");
    (*(table->SetIntField))(env, obj, rsc_field, ctxInfo->textureExtMask);
    
    /* get texture color table size */
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "textureColorTableSize", "I");
    (*(table->SetIntField))(env, obj, rsc_field, ctxInfo->textureColorTableSize);
    
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "nativeGraphicsVersion", "Ljava/lang/String;");
    (*(table->SetObjectField))(env, obj, rsc_field, (*env)->NewStringUTF(env, ctxInfo->versionStr));

    if (ctxInfo->textureAnisotropicFilterAvailable) {

	float degree;

        glGetFloatv(GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, &degree);
        rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "anisotropicDegreeMax", "F");
        (*(table->SetFloatField))(env, obj, rsc_field, degree);
    }

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "textureBoundaryWidthMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, 1);

    glGetIntegerv(GL_MAX_TEXTURE_SIZE, &param);
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "textureWidthMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, param);

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "textureHeightMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, param);

    param = -1;
    glGetIntegerv(GL_MAX_3D_TEXTURE_SIZE, &param);
    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "texture3DWidthMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, param);

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "texture3DHeightMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, param);

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "texture3DDepthMax", "I");
    (*(table->SetIntField))(env, obj, rsc_field, param);
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_destroyContext(
    JNIEnv *env,
    jclass cl,
    jlong display,
    jint window,
    jlong ctxInfo)
{
    GraphicsContextPropertiesInfo* s =  (GraphicsContextPropertiesInfo* )ctxInfo;
    jlong context = s->context;
    
#ifdef WIN32
    /*
     * It is possible the window is removed by removeNotify()
     * before the following is invoked :
     * wglMakeCurrent((HDC)window, NULL);
     * This will cause WinMe crash on swapBuffers()
     */
    wglDeleteContext((HGLRC)context);
#endif /* WIN32 */
    
#if defined(SOLARIS) || defined(__linux__)
    /*
    glXMakeCurrent((Display *)display, (GLXDrawable)window, NULL);
    */
    glXDestroyContext((Display *)display, (GLXContext)context);
#endif /* SOLARIS */
    /* cleanup CtxInfo and free its memory */
    cleanupCtxInfo(s); 
   
    free(s);
 
    
}

/*
 * A dummy WndProc for dummy window
 */
#ifdef WIN32
LONG WINAPI WndProc( HWND hWnd, UINT msg,
                     WPARAM wParam, LPARAM lParam )
{
            
    /* This function handles any messages that we didn't. */
    /* (Which is most messages) It belongs to the OS. */
    return DefWindowProc( hWnd, msg, wParam, lParam );
}
#endif /*end of WIN32 */




JNIEXPORT
jlong JNICALL Java_javax_media_j3d_Canvas3D_createContext(
    JNIEnv *env, 
    jobject obj, 
    jlong display,
    jint window, 
    jint vid,
    jlong visInfo,
    jlong sharedCtxInfo,
    jboolean isSharedCtx,
    jboolean offScreen)
{
    jlong gctx;
    jlong sharedCtx;

    static GLboolean first_time = GL_TRUE;
    static GLboolean force_normalize = GL_FALSE;
    
    GraphicsContextPropertiesInfo *ctxInfo = NULL;
    GraphicsContextPropertiesInfo *sharedCtxStructure;
    int PixelFormatID=0;
    
#if defined(SOLARIS) || defined(__linux__)
    GLXContext ctx;
    jlong hdc;
    
    if(sharedCtxInfo == 0)
	sharedCtx = 0;
    else {
	sharedCtxStructure = (GraphicsContextPropertiesInfo *)sharedCtxInfo;
	sharedCtx = sharedCtxStructure->context;
    }

    if (display == 0) {
	fprintf(stderr, "Canvas3D_createContext: display is null\n");
	ctx = NULL;
    }
    else if (visInfo == 0) {
	/*
	 * visInfo must be a valid pointer to an XVisualInfo struct returned
	 * by glXChooseVisual() for a physical screen.  The visual id in vid
	 * is not sufficient for handling OpenGL with Xinerama mode disabled:
	 * it doesn't distinguish between the physical screens making up the
	 * virtual screen when the X server is running in Xinerama mode.
	 */
	fprintf(stderr, "Canvas3D_createContext: visual is null\n");
	ctx = NULL;
    }
    else {
	ctx = glXCreateContext((Display *)display, (XVisualInfo *)visInfo,
	 		       (GLXContext)sharedCtx, True);
    }

    
    if (ctx == NULL) {
 	fprintf(stderr, "Canvas3D_createContext: couldn't create context\n");
	return 0;
    } 

    if (!glXMakeCurrent((Display *)display, (GLXDrawable)window, 
			(GLXContext)ctx)) {
        fprintf( stderr, "Canvas3D_createContext: couldn't make current\n");
        return 0;
    }

    gctx = (jlong)ctx;
#endif /* SOLARIS */

#ifdef WIN32
    HGLRC hrc; /* HW Rendering Context */
    HDC hdc;   /* HW Device Context */
    jboolean rescale = JNI_FALSE;
    JNIEnv table = *env;
    DWORD err;
    LPTSTR errString;

       
    static PIXELFORMATDESCRIPTOR pfd = {
	sizeof(PIXELFORMATDESCRIPTOR),
	1,                      /* Version number */
	PFD_DRAW_TO_WINDOW |
	PFD_SUPPORT_OPENGL|
	PFD_DOUBLEBUFFER,
	PFD_TYPE_RGBA,
	24,                     /* 24 bit color depth */
	0, 0, 0,                /* RGB bits and pixel sizes */
	0, 0, 0,                /* Do not care about them */
	0, 0,                   /* no alpha buffer info */
	0, 0, 0, 0, 0,          /* no accumulation buffer */
	32,                     /* 16 bit depth buffer */
	0,                      /* no stencil buffer */
	0,                      /* no auxiliary buffers */
	PFD_MAIN_PLANE,         /* layer type */
	0,                      /* reserved, must be 0 */
	0,                      /* no layer mask */
	0,                      /* no visible mask */
	0                       /* no damage mask */
    };

    jboolean result;

    /* hWnd = (HWND) window; */
    /* or could use: hDC = GetDC(hWnd); */
    
    if(sharedCtxInfo == 0)
	sharedCtx = 0;
    else {
	sharedCtxStructure = (GraphicsContextPropertiesInfo *)sharedCtxInfo;
	sharedCtx = sharedCtxStructure->context;
    }

    
    hdc =  (HDC) window;
    
    if (offScreen)  {
	pfd.dwFlags = PFD_DRAW_TO_BITMAP | PFD_SUPPORT_OPENGL |
	    PFD_SUPPORT_GDI;
	vid = -1;
	sharedCtx = 0;
    }

    /* vid of -1 means no vid was specified - do the old way */
    if (vid == -1) {
	/* choose the "pixel format", terminology is equivalent */
	/* to UNIX "visual" */
	PixelFormatID = ChoosePixelFormat(hdc, &pfd);

	if(PixelFormatID == 0) {
	    fprintf(stderr,"\nERROR: pixel format ID = 0");
	    return 0;
	}
    }
    else {
	PixelFormatID = vid;
    }


    SetPixelFormat(hdc, PixelFormatID, &pfd);

    hrc = wglCreateContext( hdc );

    if (!hrc) {
	err = GetLastError();
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		      FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, err, 0, (LPTSTR)&errString, 0, NULL);

	fprintf(stderr, "wglCreateContext Failed: %s\n", errString);
	return 0;
    }

    if (sharedCtx != 0) {
	wglShareLists( (HGLRC) sharedCtx, hrc );
    } 

    result = wglMakeCurrent(hdc, hrc);

    if (!result) {
	err = GetLastError();
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		      FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, err, 0, (LPTSTR)&errString, 0, NULL);
	fprintf(stderr, "wglMakeCurrent Failed: %s\n", errString);
	return 0;
    }

    gctx = (jlong)hrc;
#endif /* WIN32 */

    /* allocate the structure */
    ctxInfo = (GraphicsContextPropertiesInfo *)malloc(sizeof(GraphicsContextPropertiesInfo));
    
    /* initialize the structure */
    initializeCtxInfo(env, ctxInfo);
    ctxInfo->context = gctx; 

    if (!getPropertiesFromCurrentContext(env, obj, ctxInfo, (jlong) hdc, PixelFormatID,  display, (jlong) visInfo)) {
	return 0;
    }


    /* setup structure */
    
    if(!isSharedCtx){
	/* Setup field in Java side */
	setupCanvasProperties(env, obj, ctxInfo);
    }
    
    /* Set up rescale_normal if extension supported */
    if (first_time && getJavaBoolEnv(env, "isForceNormalized")) {      
      force_normalize = GL_TRUE;
      first_time = GL_FALSE;
    }

    if (force_normalize) {
      /* Disable rescale normal */
      ctxInfo->rescale_normal_ext = GL_FALSE;      
    }
    
    if (ctxInfo->rescale_normal_ext ) {
        glEnable(ctxInfo->rescale_normal_ext_enum);
    }
    else {
        glEnable(GL_NORMALIZE);
    }

    glColorMaterial(GL_FRONT_AND_BACK, GL_DIFFUSE);
    glDepthFunc(GL_LEQUAL);
    glEnable(GL_COLOR_MATERIAL);
    glReadBuffer(GL_FRONT);
    return ((jlong)ctxInfo);
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_useCtx(
    JNIEnv *env, 
    jclass cl, 
    jlong ctxInfo,
    jlong display, 
    jint window)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
#if defined(SOLARIS) || defined(__linux__)
    glXMakeCurrent((Display *)display, (GLXDrawable)window, (GLXContext)ctx);
#endif

#ifdef WIN32
    wglMakeCurrent((HDC) window, (HGLRC) ctx);
#endif
}

JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_getNumCtxLights(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo)
{
    GLint nlights;

    glGetIntegerv(GL_MAX_LIGHTS, &nlights);
    return((jint)nlights);
}



JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_composite(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint px,
    jint py,
    jint minX,
    jint minY,
    jint maxX,
    jint maxY,
    jint rasWidth,
    jbyteArray imageYdown,
    jint winWidth,
    jint winHeight)
{
    GLenum gltype;
    JNIEnv table;
    jbyte *byteData;
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    GLboolean tex3d, texCubeMap;
    
    table = *env;
   
#ifdef VERBOSE
    fprintf(stderr, "Canvas3D.composite()\n");
#endif
    /* temporarily disable fragment operations */
    /* TODO: the GL_TEXTURE_BIT may not be necessary */
    glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_DEPTH_BUFFER_BIT);
    
    if(ctxProperties->texture3DAvailable) 
	tex3d = glIsEnabled(ctxProperties->texture_3D_ext_enum);

    if(ctxProperties->textureCubeMapAvailable) 
	texCubeMap = glIsEnabled(ctxProperties->texture_cube_map_ext_enum);
    
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_FOG);
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    if(ctxProperties->texture3DAvailable) 
	glDisable(ctxProperties->texture_3D_ext_enum);
    if(ctxProperties->textureCubeMapAvailable) 
	glDisable(ctxProperties->texture_cube_map_ext_enum);

    glEnable(GL_BLEND);
    glBlendFunc (GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    /* loaded identity modelview and projection matrix */
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrtho(0.0, (double)winWidth, 0.0, (double)winHeight, -1.0, 1.0);
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    /* start from upper left corner */
    glRasterPos2i(px + minX, winHeight-(py + minY));
    
    glPixelZoom(1.0, -1.0);

    byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
							      imageYdown,
							      NULL);
    /* if abgr_ext is supported then the data will be in that format */
    if (ctxProperties->abgr_ext) {
	gltype = GL_ABGR_EXT;
    } else {
	gltype = GL_RGBA;
    }

    /*
     * set the actual width of data which is the width of the canvas
     * because what needs to be drawn may be smaller than the canvas
     */
    glPixelStorei(GL_UNPACK_ROW_LENGTH, rasWidth);

    /*
     * we only need to skip pixels if width of the area to draw is smaller
     * than the width of the raster
     */
    
    /*
     * skip this many rows in the data because the size of what
     * needs to be drawn may be smaller than the canvas
     */
    glPixelStorei(GL_UNPACK_SKIP_ROWS, minY);
    /*
     * skip this many pixels in the data before drawing because
     * the size of what needs to be drawn may be smaller than the
     * canvas
     */
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, minX);


    glDrawPixels(maxX - minX, maxY - minY,
		 gltype, GL_UNSIGNED_BYTE,  byteData);

    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
    
    glMatrixMode(GL_PROJECTION);

    glLoadIdentity();

    (*(table->ReleasePrimitiveArrayCritical))(env, imageYdown, byteData, 0);

    /* re-enable fragment operation if necessary */
    if(ctxProperties->texture3DAvailable)
	if (tex3d) glEnable(ctxProperties->texture_3D_ext_enum);
    
    if(ctxProperties->textureCubeMapAvailable)
	if (texCubeMap) glEnable(ctxProperties->texture_cube_map_ext_enum);
    
    /* Java 3D always clears the Z-buffer */
    glDepthMask(GL_TRUE);
    glClear(GL_DEPTH_BUFFER_BIT);

    glPopAttrib();
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_initTexturemapping(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint texWidth,
    jint texHeight,
    jint objectId)
{
    GraphicsContextPropertiesInfo *ctxProperties =
	(GraphicsContextPropertiesInfo *)ctxInfo;
    GLint gltype;
    GLint width;

    gltype = (ctxProperties->abgr_ext ? GL_ABGR_EXT : GL_RGBA);

    glBindTexture(GL_TEXTURE_2D, objectId);


    glTexImage2D(GL_PROXY_TEXTURE_2D, 0, GL_RGBA, texWidth,
		 texHeight, 0, gltype, GL_UNSIGNED_BYTE,  NULL);

    glGetTexLevelParameteriv(GL_PROXY_TEXTURE_2D, 0,
			     GL_TEXTURE_WIDTH, &width);

    if (width <= 0) {
	return JNI_FALSE;
    }

    /* init texture size only without filling the pixels */
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth,
		 texHeight, 0, gltype, GL_UNSIGNED_BYTE,  NULL);


    return JNI_TRUE;
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_texturemapping(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint px,
    jint py,
    jint minX,
    jint minY,
    jint maxX,
    jint maxY,
    jint texWidth,
    jint texHeight,
    jint rasWidth,
    jint format,
    jint objectId,
    jbyteArray imageYdown,
    jint winWidth,
    jint winHeight)
{
    JNIEnv table;
    GLint gltype;
    GLfloat texMinU,texMinV,texMaxU,texMaxV;
    GLfloat mapMinX,mapMinY,mapMaxX,mapMaxY;
    GLfloat halfWidth,halfHeight;
    jbyte *byteData;
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    GLboolean tex3d, texCubeMap;
    
    table = *env;
    gltype = GL_RGBA;
    
    /* temporary disable fragment operation */
    glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_DEPTH_BUFFER_BIT);
    
    if (ctxProperties->texture3DAvailable) {
	tex3d = glIsEnabled(ctxProperties->texture_3D_ext_enum);
    }
    
    if (ctxProperties->textureCubeMapAvailable) {
	texCubeMap = glIsEnabled(ctxProperties->texture_cube_map_ext_enum);
    }
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_FOG);
    glDisable(GL_LIGHTING);
    glDisable(GL_TEXTURE_2D);
    if (ctxProperties->texture3DAvailable) {
	glDisable(ctxProperties->texture_3D_ext_enum);
    }
    if (ctxProperties->textureCubeMapAvailable) {
	glDisable(ctxProperties->texture_cube_map_ext_enum);
    }
    /* glGetIntegerv(GL_TEXTURE_BINDING_2D,&binding); */
    glDepthMask(GL_FALSE);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    glBindTexture(GL_TEXTURE_2D, objectId);
    /* set up texture parameter */
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

#ifdef VERBOSE
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL);
#endif
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    glEnable(GL_TEXTURE_2D);

    /* glGetIntegerv (GL_VIEWPORT, viewport); */

    /* loaded identity modelview and projection matrix */
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();

    glOrtho(0.0, (double)winWidth, 0.0, (double)winHeight,0.0, 0.0);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
							      imageYdown,
							      NULL);

    if (ctxProperties->abgr_ext) {
	gltype = GL_ABGR_EXT;
    } else { 
	switch (format) {
	case FORMAT_BYTE_RGBA:
	    gltype = GL_RGBA;
	    break;
	case FORMAT_BYTE_RGB:
	    gltype = GL_RGB;
	    break;
	}
    }
    glPixelStorei(GL_UNPACK_ROW_LENGTH, rasWidth);
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, minX);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, minY);
    glTexSubImage2D(GL_TEXTURE_2D, 0, minX, minY,
		    maxX - minX, maxY - minY,
		    gltype, GL_UNSIGNED_BYTE,
		    byteData);
    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
    glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
    glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);

    
    (*(table->ReleasePrimitiveArrayCritical))(env, imageYdown, byteData, 0);

    texMinU = (float) minX/ (float) texWidth; 
    texMinV = (float) minY/ (float) texHeight; 
    texMaxU = (float) maxX/ (float) texWidth;
    texMaxV = (float) maxY/ (float) texHeight; 
    halfWidth = (GLfloat)winWidth/2.0f;
    halfHeight = (GLfloat)winHeight/2.0f;

    mapMinX = (float) (((px + minX)- halfWidth)/halfWidth);
    mapMinY = (float) ((halfHeight - (py + maxY))/halfHeight);
    mapMaxX = (float) ((px + maxX - halfWidth)/halfWidth);
    mapMaxY = (float) ((halfHeight - (py + minY))/halfHeight);

#ifdef VERBOSE
    printf("(texMinU,texMinV,texMaxU,texMaxV) = (%3.2f,%3.2f,%3.2f,%3.2f)\n",
	   texMinU,texMinV,texMaxU,texMaxV);
    printf("(mapMinX,mapMinY,mapMaxX,mapMaxY) = (%3.2f,%3.2f,%3.2f,%3.2f)\n",
	   mapMinX,mapMinY,mapMaxX,mapMaxY);

#endif
    glBegin(GL_QUADS);

    glTexCoord2f(texMinU, texMaxV); glVertex2f(mapMinX,mapMinY);
    glTexCoord2f(texMaxU, texMaxV); glVertex2f(mapMaxX,mapMinY);
    glTexCoord2f(texMaxU, texMinV); glVertex2f(mapMaxX,mapMaxY);
    glTexCoord2f(texMinU, texMinV); glVertex2f(mapMinX,mapMaxY);
    glEnd();

    /* re-enable fragment operation if necessary */
    if (ctxProperties->texture3DAvailable)
	if (tex3d) glEnable(ctxProperties->texture_3D_ext_enum);
    
    if (ctxProperties->textureCubeMapAvailable)
	if (texCubeMap) glEnable(ctxProperties->texture_cube_map_ext_enum);


    /* Java 3D always clears the Z-buffer */
    glDepthMask(GL_TRUE);
    glClear(GL_DEPTH_BUFFER_BIT);
    glPopAttrib();
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_clear(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jfloat r, 
    jfloat g, 
    jfloat b,
    jint winWidth,
    jint winHeight, 
    jobject pa2d,
    jint imageScaleMode, 
    jbyteArray pixels_obj)

{
    jclass pa2d_class;
    jfieldID format_field, width_field, height_field;
    int format, width, height;
    GLubyte * pixels;
    JNIEnv table;
    GLenum gltype;
    float xzoom, yzoom, zoom;
    float rasterX, rasterY;
    int repeatX, repeatY, i, j;
    int row_length, skip_pixels, skip_rows, subwidth, subheight; 
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;
    table = *env;
   
#ifdef VERBOSE
    fprintf(stderr, "Canvas3D.clear()\n");
#endif
    
    if(!pa2d) {
	glClearColor((float)r, (float)g, (float)b, 1.0f);
	glClear(GL_COLOR_BUFFER_BIT); 
    }
    else {
	GLboolean tex3d, texCubeMap;

	/* Do a cool image blit */
	pa2d_class = (jclass) (*(table->GetObjectClass))(env, pa2d);
	format_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class, 
							 "storedYdownFormat", "I");
	width_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class, 
							"width", "I");
	height_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class, 
							 "height", "I");

	format = (int) (*(table->GetIntField))(env, pa2d, format_field);
	width = (int) (*(table->GetIntField))(env, pa2d, width_field);
	height = (int) (*(table->GetIntField))(env, pa2d, height_field);

	pixels = (GLubyte *) (*(table->GetPrimitiveArrayCritical))(env, 
								   pixels_obj, NULL);

	/* temporarily disable fragment operations */
	/* TODO: the GL_TEXTURE_BIT may not be necessary */
	glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT); 
	
	if(ctxProperties->texture3DAvailable)
	    tex3d = glIsEnabled(ctxProperties->texture_3D_ext_enum);

	if(ctxProperties->textureCubeMapAvailable)
	    texCubeMap = glIsEnabled(ctxProperties->texture_cube_map_ext_enum);

	glDisable(GL_ALPHA_TEST);                 
	glDisable(GL_BLEND);
	glDisable(GL_DEPTH_TEST);
	glDisable(GL_FOG);
	glDisable(GL_LIGHTING);
	glDisable(GL_TEXTURE_2D);
	if(ctxProperties->texture3DAvailable)
	    glDisable(ctxProperties->texture_3D_ext_enum);
	
	if(ctxProperties->textureCubeMapAvailable)
	    glDisable(ctxProperties->texture_cube_map_ext_enum);

	/* loaded identity modelview and projection matrix */
	glMatrixMode(GL_PROJECTION); 
	glLoadIdentity();
	glMatrixMode(GL_MODELVIEW); 
	glLoadIdentity();
	
	switch (format) {
        case FORMAT_BYTE_RGBA:
            gltype = GL_RGBA;
            break;
        case FORMAT_BYTE_RGB:
            gltype = GL_RGB;
            break;
	    
        case FORMAT_BYTE_ABGR:         
	    if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
		gltype = GL_ABGR_EXT;
	    }
	    break;
	    
        case FORMAT_BYTE_BGR:         
	    if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */
		gltype = ctxProperties->bgr_ext_enum ;
	    }
	    break;
	    
        case FORMAT_BYTE_LA:
            gltype = GL_LUMINANCE_ALPHA;
            break;
        case FORMAT_BYTE_GRAY:
        case FORMAT_USHORT_GRAY:	    
            /* TODO: throw exception */
            break;
        }
	
	/* start from upper left corner */
	glRasterPos3f(-1.0, 1.0, 0.0);
	
	/* setup the pixel zoom */
	xzoom = (float)winWidth/width;
	yzoom = (float)winHeight/height;
	switch(imageScaleMode){
	case javax_media_j3d_Background_SCALE_NONE:
	     if(xzoom > 1.0f || yzoom > 1.0f)
		{
		/* else don't need to clear the background with background color */
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    glPixelZoom(1.0, -1.0);
	    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);

	    break;
	case javax_media_j3d_Background_SCALE_FIT_MIN:
	    if(xzoom != yzoom ) {
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    zoom = xzoom < yzoom? xzoom:yzoom;
	    glPixelZoom(zoom, -zoom);
	    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);

	    break;
	case javax_media_j3d_Background_SCALE_FIT_MAX:
	    zoom = xzoom > yzoom? xzoom:yzoom;
	    glPixelZoom(zoom, -zoom);
	    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);

	    break;
	case javax_media_j3d_Background_SCALE_FIT_ALL:
	    glPixelZoom(xzoom, -yzoom);
	    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);
	    break;
	case javax_media_j3d_Background_SCALE_REPEAT:	    
	    glPixelZoom(1.0, -1.0);
	    /* get those raster positions */
	    repeatX = winWidth/width;
	    if(repeatX * width < winWidth)
		repeatX++;
	    repeatY = winHeight/height;
	    if(repeatY * height < winHeight)
		repeatY++;
	    for(i = 0; i < repeatX; i++)
		for(j = 0; j < repeatY; j++) {
		    rasterX =  -1.0f + (float)width/winWidth * i * 2;
		    rasterY =  1.0f - (float)height/winHeight * j * 2;
		    glRasterPos3f(rasterX, rasterY, 0.0);
		    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);
		}
	    break;
	        
	case javax_media_j3d_Background_SCALE_NONE_CENTER:
	    if(xzoom > 1.0f || yzoom > 1.0f){
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    if(xzoom >= 1.0f){
		rasterX = -(float)width/winWidth;
		subwidth = width; 
	    }
	    else {
		rasterX = -1.0;
		row_length = width;
		glPixelStorei(GL_UNPACK_ROW_LENGTH, row_length);
		skip_pixels = (width-winWidth)/2;
		glPixelStorei(GL_UNPACK_SKIP_PIXELS, skip_pixels); 
		subwidth = winWidth;
	    }
	    if(yzoom >= 1.0f){
		rasterY = (float)height/winHeight;
		subheight = height; 
	    }
	    else {
		rasterY = 1.0f;
		skip_rows = (height-winHeight)/2;
		glPixelStorei(GL_UNPACK_SKIP_ROWS, skip_rows);
		subheight = winHeight; 
	    } 
	    glRasterPos3f(rasterX, rasterY, 0.0);
	    glPixelZoom(1.0, -1.0);
	    glDrawPixels(subwidth, subheight, gltype, GL_UNSIGNED_BYTE,
			 pixels);
	    glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
	    glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);
	    glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
	    break;
	}
	/* re-enable fragment operation if necessary */
	glPopAttrib();
		
	if(ctxProperties->texture3DAvailable)
	    if (tex3d) glEnable(ctxProperties->texture_3D_ext_enum);

	if(ctxProperties->textureCubeMapAvailable)
	    if (texCubeMap) glEnable(ctxProperties->texture_cube_map_ext_enum);

	(*(table->ReleasePrimitiveArrayCritical))(env, pixels_obj, 
						  (jbyte *)pixels, 0);
    }
    /* Java 3D always clears the Z-buffer */
    glPushAttrib(GL_DEPTH_BUFFER_BIT);
    glDepthMask(GL_TRUE);
    glClear(GL_DEPTH_BUFFER_BIT);
    glPopAttrib();
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_textureclear(JNIEnv *env,
							jobject obj,
							jlong ctxInfo,
							jint maxX, 
							jint maxY,
							jfloat r, 
							jfloat g, 
							jfloat b,
							jint winWidth,
							jint winHeight,
							jint objectId,
							jint imageScaleMode,
							jobject pa2d,
							jboolean update)
{ 
    jclass pa2d_class; 
    jfieldID pixels_field, format_field, width_field, height_field; 
    jbyteArray pixels_obj; 
    int format, width, height;
    GLubyte * pixels;  
    JNIEnv table; 
    GLenum gltype; 
    GLfloat texMinU, texMinV, texMaxU, texMaxV, adjustV; 
    GLfloat mapMinX, mapMinY, mapMaxX, mapMaxY; 
    GLfloat halfWidth, halfHeight; 
    float xzoom, yzoom, zoom;
    int i, j;
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;
    
    table = *env; 

    /* update = 1; */ 
    
#ifdef VERBOSE 
    fprintf(stderr, "Canvas3D.textureclear()\n");  
#endif 
    if(!pa2d){
	glClearColor((float)r, (float)g, (float)b, 1.0f); 
	glClear(GL_COLOR_BUFFER_BIT); 
    }
    /* glPushAttrib(GL_DEPTH_BUFFER_BIT); */
    if (pa2d) { 
	GLboolean tex3d, texCubeMap; 
	  
	
	/* Do a cool image blit */ 
	pa2d_class = (jclass) (*(table->GetObjectClass))(env, pa2d);

	pixels_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class, 
							 "imageYup", "[B"); 
	format_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class,  
							 "storedYupFormat", "I"); 
	pixels_obj = (jbyteArray)(*(table->GetObjectField))(env, pa2d,  
							pixels_field);
	
	width_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class,  
							"width", "I"); 
	height_field = (jfieldID) (*(table->GetFieldID))(env, pa2d_class,  
							 "height", "I");

	format = (int) (*(table->GetIntField))(env, pa2d, format_field); 
	width = (int) (*(table->GetIntField))(env, pa2d, width_field); 
	height = (int) (*(table->GetIntField))(env, pa2d, height_field);	
 	pixels = (GLubyte *) (*(table->GetPrimitiveArrayCritical))(env,   
 								   pixels_obj, NULL);

#ifdef VERBOSE
	fprintf(stderr, "width = %d height = %d \n", width, height);
#endif
	
	/* temporary disable fragment operation */ 
	glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_POLYGON_BIT); 

	if(ctxProperties->texture3DAvailable) 
	    tex3d = glIsEnabled(ctxProperties->texture_3D_ext_enum);
	if(ctxProperties->textureCubeMapAvailable) 
	    texCubeMap = glIsEnabled(ctxProperties->texture_cube_map_ext_enum);
	glDisable(GL_ALPHA_TEST);                  
	glDisable(GL_BLEND); 
	glDisable(GL_DEPTH_TEST); 
	glDisable(GL_FOG); 
	glDisable(GL_LIGHTING);
	
	if(ctxProperties->texture3DAvailable)
	    glDisable(ctxProperties->texture_3D_ext_enum);
	
	if(ctxProperties->textureCubeMapAvailable)
	    glDisable(ctxProperties->texture_cube_map_ext_enum);
	
	Java_javax_media_j3d_Canvas3D_resetTexCoordGeneration(env, obj, ctxInfo); 

	glEnable(GL_TEXTURE_2D);

	/* reset the polygon mode */
	glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

	glDepthMask(GL_FALSE);  
	glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 
	glBindTexture(GL_TEXTURE_2D, objectId);

	/* set up texture parameter */
	if(update){
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); 
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); 
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT); 
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT); 
	}
#ifdef VERBOSE 
	glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_DECAL); 
#endif 
	glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
	
 	
	
	 if(update){
	    switch (format) { 
	    case FORMAT_BYTE_RGBA: 
		gltype = GL_RGBA;
#ifdef VERBOSE
		fprintf(stderr, "FORMAT_BYTE_RGBA\n");
#endif
		break; 
	    case FORMAT_BYTE_RGB: 
		gltype = GL_RGB;
#ifdef VERBOSE
		fprintf(stderr, "FORMAT_BYTE_RGB\n");
#endif
		break; 

		/* GL_ABGR_EXT */
	    case FORMAT_BYTE_ABGR:           
		if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */  
		    gltype = GL_ABGR_EXT;  
		}  
		break;

		/* GL_BGR_EXT or GL_BGR */
	    case FORMAT_BYTE_BGR:           
		if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */  
		    gltype = ctxProperties->bgr_ext_enum;  
		}  
		break;  
  
	    case FORMAT_BYTE_LA:  
		gltype = GL_LUMINANCE_ALPHA;  
		break;  
	    case FORMAT_BYTE_GRAY:  
	    case FORMAT_USHORT_GRAY:	       
		/* TODO: throw exception */  
		break;  
	    } 
	
	    /* texture map here! */ 
	    glTexImage2D(GL_TEXTURE_2D, 0, gltype, width,  
			 height, 0, gltype, GL_UNSIGNED_BYTE,  
			 pixels);
	}
	/* loaded identity modelview and projection matrix */ 
	glMatrixMode(GL_PROJECTION);  
	glLoadIdentity();
	glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
	glMatrixMode(GL_MODELVIEW);  
	glLoadIdentity(); 
	glMatrixMode(GL_TEXTURE);
	glPushMatrix();
	glLoadIdentity();

	xzoom = (float)winWidth/maxX;
	yzoom = (float)winHeight/maxY;
	switch(imageScaleMode) {
	case javax_media_j3d_Background_SCALE_NONE:
	    if(xzoom > 1.0f || yzoom > 1.0f){
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    texMinU = 0.0f; 
	    texMinV = 0.0f; 
	    texMaxU = 1.0f; 
	    texMaxV = 1.0f;
	    halfWidth = (GLfloat)winWidth/2.0f;  
	    halfHeight = (GLfloat)winHeight/2.0f;  
	    mapMinX = (float) ((0 - halfWidth)/halfWidth);   
	    mapMinY = (float) ((0 - halfHeight)/halfHeight);
	    mapMaxX = (float) ((maxX - halfWidth)/halfWidth);    
	    mapMaxY = (float) ((maxY - halfHeight)/halfHeight);
	    adjustV = ((float)winHeight - (float)maxY)/halfHeight; 
	    mapMinY += adjustV;
	    mapMaxY += adjustV;
	    break;
	case javax_media_j3d_Background_SCALE_FIT_MIN:
	    if(xzoom != yzoom){
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }

	    zoom = xzoom < yzoom? xzoom: yzoom;
	    texMinU = 0.0f; 
	    texMinV = 0.0f; 
	    texMaxU = 1.0f; 
	    texMaxV = 1.0f;
	    mapMinX = -1.0f;
	    mapMaxY = 1.0f;
	    if(xzoom < yzoom){
		mapMaxX = 1.0f;
		mapMinY = -1.0f + 2.0f * ( 1.0f - zoom * (float)maxY/(float) winHeight );
	    } else {
		mapMaxX = -1.0f + zoom * (float)maxX/winWidth * 2;
		mapMinY = -1.0f;
	    }
	    break;
	case javax_media_j3d_Background_SCALE_FIT_MAX: 
	    zoom = xzoom > yzoom? xzoom: yzoom;
	    /*fprintf(stderr, "zoom: %f, xzoom: %f, yzoom: %f\n", zoom, xzoom, yzoom);*/
	    mapMinX = -1.0f;
	    mapMinY = -1.0f;
	    mapMaxX = 1.0f; 
	    mapMaxY = 1.0f;
	    if(xzoom < yzoom) {
		texMinU = 0.0f;
		texMinV = 0.0f;
		texMaxU = (float)winWidth/maxX/zoom; 
		texMaxV = 1.0f;
	    } else {
		texMinU = 0.0f;
		texMinV = 1.0f - (float)winHeight/maxY/zoom; 
		texMaxU = 1.0f;
		texMaxV = 1.0f;
	    }
	    break;
	case javax_media_j3d_Background_SCALE_FIT_ALL:
	    texMinU = 0.0f; 
	    texMinV = 0.0f; 
	    texMaxU = 1.0f; 
	    texMaxV = 1.0f;
	    mapMinX = -1.0f;
	    mapMinY = -1.0f;
	    mapMaxX = 1.0f; 
	    mapMaxY = 1.0f;   
	    break;
	case javax_media_j3d_Background_SCALE_REPEAT:
	    /* glScalef(1.0f, -1.0f, 1.0f); */
	    i = winWidth/width;
	    j = winHeight/height; 
	    texMinU = 0.0f;
	    texMinV = (float)(j + 1) - yzoom;
	    texMaxU = xzoom;
	    texMaxV = (float)(j + 1);
	    mapMinX = -1.0f;
	    mapMinY = -1.0f;
	    mapMaxX = 1.0f; 
	    mapMaxY = 1.0f;	    
	    break;
	case javax_media_j3d_Background_SCALE_NONE_CENTER:
	    if(xzoom > 1.0f || yzoom > 1.0f){
		glClearColor((float)r, (float)g, (float)b, 1.0f);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    if(xzoom >= 1.0f){
		texMinU = 0.0f;
		texMaxU = 1.0f;
		mapMinX = -(float)maxX/winWidth;
		mapMaxX = (float)maxX/winWidth;
	    } else {
		texMinU = 0.5f - (float)winWidth/maxX/2;
		texMaxU = 0.5f + (float)winWidth/maxX/2;
		mapMinX = -1.0f;
		mapMaxX = 1.0f;
	    }
	    if(yzoom >= 1.0f) {
		texMinV = 0.0f;
		texMaxV = 1.0f;
		mapMinY = -(float)maxY/winHeight;
		mapMaxY = (float)maxY/winHeight;
	    }else {
		texMinV = 0.5f - (float)winHeight/maxY/2; 
		texMaxV = 0.5f + (float)winHeight/maxY/2;
		mapMinY = -1.0f;
		mapMaxY = 1.0f;	
	    }
	    break;
	}
#ifdef VERBOSE 
        printf("adjustV = %3.2f\n",adjustV);  
        printf("(texMinU,texMinV,texMaxU,texMaxV) = (%3.2f,%3.2f,%3.2f,%3.2f)\n", 
	       texMinU,texMinV,texMaxU,texMaxV); 
        printf("(mapMinX,mapMinY,mapMaxX,mapMaxY) = (%3.2f,%3.2f,%3.2f,%3.2f)\n", 
    	       mapMinX,mapMinY,mapMaxX,mapMaxY);
#endif


	glBegin(GL_QUADS); 
#ifdef VERBOSE	
	/* 	glTexCoord2f(0.2, 0.2); glVertex2f(0.0,0.0);  */
	/* 	glTexCoord2f(0.4, 0.2); glVertex2f(0.2,0.0);  */
	/* 	glTexCoord2f(0.4, 0.4); glVertex2f(0.2,0.2);  */
	/* 	glTexCoord2f(0.2, 0.4); glVertex2f(0.0,0.2); */
	glColor3f(1.0, 0.0, 0.0);
#endif
	glTexCoord2f(texMinU, texMinV); glVertex2f(mapMinX,mapMinY); 
	glTexCoord2f(texMaxU, texMinV); glVertex2f(mapMaxX,mapMinY); 
	glTexCoord2f(texMaxU, texMaxV); glVertex2f(mapMaxX,mapMaxY); 
	glTexCoord2f(texMinU, texMaxV); glVertex2f(mapMinX,mapMaxY);	
	glEnd(); 

	/* Restore texture Matrix transform */	
	glPopMatrix();
	
	glMatrixMode(GL_MODELVIEW);      	
	/* re-enable fragment operation if necessary */
	glPopAttrib();	
	if(ctxProperties->texture3DAvailable)
	    if (tex3d) glEnable(ctxProperties->texture_3D_ext_enum); 
	
	if(ctxProperties->textureCubeMapAvailable)
	    if (texCubeMap) glEnable(ctxProperties->texture_cube_map_ext_enum); 
	
	(*(table->ReleasePrimitiveArrayCritical))(env, pixels_obj,  
						  (jbyte *)pixels, 0); 
    }

    /* Java 3D always clears the Z-buffer */
    glPushAttrib(GL_DEPTH_BUFFER_BIT);
    glDepthMask(GL_TRUE);
    glClear(GL_DEPTH_BUFFER_BIT);
    glPopAttrib();
    
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setRenderMode(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jint mode,
    jboolean dbEnable)
{
    GLint drawBuf;
    
    if (dbEnable) {
        drawBuf = GL_BACK;
        switch (mode) {
	case 0:				/* FIELD_LEFT */
	    drawBuf = GL_BACK_LEFT;
	    break;
	case 1:				/* FIELD_RIGHT */
	    drawBuf = GL_BACK_RIGHT;
	    break;
	case 2:				/* FIELD_ALL */
	    drawBuf = GL_BACK;
	    break;
        }
    }
    else {
        drawBuf = GL_FRONT;
        switch (mode) {
	case 0:                             /* FIELD_LEFT */
	    drawBuf = GL_FRONT_LEFT;
	    break;
	case 1:                             /* FIELD_RIGHT */
	    drawBuf = GL_FRONT_RIGHT;
	    break;
	case 2:                             /* FIELD_ALL */
	    drawBuf = GL_FRONT;
	    break;
        }
    }

#ifdef VERBOSE
    switch (drawBuf) {
    case GL_FRONT_LEFT:
        fprintf(stderr, "glDrawBuffer(GL_FRONT_LEFT)\n");
        break;
    case GL_FRONT_RIGHT:
        fprintf(stderr, "glDrawBuffer(GL_FRONT_RIGHT)\n");
        break;
    case GL_FRONT:
        fprintf(stderr, "glDrawBuffer(GL_FRONT)\n");
        break;
    case GL_BACK_LEFT:
	fprintf(stderr, "glDrawBuffer(GL_BACK_LEFT)\n");
	break;
    case GL_BACK_RIGHT:
	fprintf(stderr, "glDrawBuffer(GL_BACK_RIGHT)\n");
	break;
    case GL_BACK:
	fprintf(stderr, "glDrawBuffer(GL_BACK)\n");
	break;
    default:
	fprintf(stderr, "Unknown glDrawBuffer!!!\n");
	break;
    }
#endif

    glDrawBuffer(drawBuf);
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_clearAccum(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo)
{
    
    glClear(GL_ACCUM_BUFFER_BIT);

}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_accum(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jfloat value)
{
    
    glReadBuffer(GL_BACK);
    
    glAccum(GL_ACCUM, (float)value);

    glReadBuffer(GL_FRONT);

}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_accumReturn(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo)
{

    glAccum(GL_RETURN, 1.0);

}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setDepthBufferWriteEnable(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jboolean mode)
{    
    if (mode)  
	glDepthMask(GL_TRUE);
    else
	glDepthMask(GL_FALSE);

}


JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_swapBuffers(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jlong display, 
    jint win)
{
    
#if defined(SOLARIS) || defined(__linux__)
   glXSwapBuffers((Display *)display, (Window)win);
   
#endif

#ifdef WIN32
   HDC hdc;

   hdc = (HDC) win;

   SwapBuffers(hdc);
#endif
  /*
   * It would be nice to do a glFinish here, but we can't do this
   * in the ViewThread Java thread in MT-aware OGL libraries without
   * switching from the ViewThread to the Renderer thread an extra time
   * per frame. Instead, we do glFinish after every rendering but before
   * swap in the Renderer thread.
   */
   /* glFinish(); */
   return 0;
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_syncRender(
      JNIEnv *env,
      jobject obj,
      jlong ctxInfo,
      jboolean waitFlag)  
{
	
    if (waitFlag == JNI_TRUE)
        glFinish();  
    else
	glFlush();
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_newDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint id)
{
    
    glNewList(id, GL_COMPILE);
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_endDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo)
{
    
    glEndList();
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setGlobalAlpha(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jfloat alpha)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
    
    /* GL_GLOBAL_ALPHA_SUN */
    if(ctxProperties->global_alpha_sun){
	glEnable(GL_GLOBAL_ALPHA_SUN);
	ctxProperties->glGlobalAlphaFactorfSUN(alpha);
    }
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_disableGlobalAlpha(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo)
{

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;
	
    /* GL_GLOBAL_ALPHA_SUN */
    if(ctxProperties->global_alpha_sun){
	glDisable(GL_GLOBAL_ALPHA_SUN);
    }
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_callDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint id,
    jboolean isNonUniformScale)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;

    /* resale_normal_ext */
    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glEnable(GL_NORMALIZE);
    } 
    
    glCallList(id);

    if (ctxProperties->rescale_normal_ext && isNonUniformScale) {
	glDisable(GL_NORMALIZE);
    } 
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_freeDisplayList(
    JNIEnv *env,
    jclass cl,
    jlong ctxInfo,
    jint id)
{
    
    glDeleteLists(id, 1);
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_freeTexture(
    JNIEnv *env,
    jclass cl,
    jlong ctxInfo,
    jint id)
{    
    GLuint texObj;

    if(id > 0) {
	texObj = id;
	glDeleteTextures(1, &texObj);
    }
    else
	fprintf(stderr, "try to delete tex with texid <= 0. \n");
	
}



JNIEXPORT jint JNICALL Java_javax_media_j3d_Canvas3D_getTextureUnitCount(
    JNIEnv *env,
    jobject obj,
    jlong  ctxInfo)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    
    return ctxProperties->textureUnitCount;
}

/*
 * Method:    getTextureColorTableSize
 */
int getTextureColorTableSize(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    char *extensionStr,
    int minorVersion)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    int size;
    
    if(minorVersion >= 2 && isExtensionSupported(extensionStr, "GL_ARB_imaging")){
	
#ifdef WIN32
	ctxProperties->glColorTable = (MYPFNGLCOLORTABLEPROC)wglGetProcAddress("glColorTable");
	ctxProperties->glGetColorTableParameteriv =
	    (MYPFNGLGETCOLORTABLEPARAMETERIVPROC)wglGetProcAddress("glGetColorTableParameteriv");
#endif
#if defined(SOLARIS) || defined(__linux__)
	ctxProperties->glColorTable = glColorTable;
	ctxProperties->glGetColorTableParameteriv = glGetColorTableParameteriv;
#endif

    } else if(isExtensionSupported(extensionStr, "GL_SGI_color_table")) {
#ifdef WIN32	
	ctxProperties->glColorTable = (MYPFNGLCOLORTABLEPROC)wglGetProcAddress("glColorTableSGI");
        ctxProperties->glGetColorTableParameteriv =
                (MYPFNGLGETCOLORTABLEPARAMETERIVPROC)wglGetProcAddress("glGetColorTableParameterivSGI");
#endif
#if defined(SOLARIS) || defined(__linux__)
	ctxProperties->glColorTable = glColorTableSGI;
	ctxProperties->glGetColorTableParameteriv = glGetColorTableParameterivSGI; 
#endif
	
    } else {
	return 0;
    }

    if ((ctxProperties->glColorTable == NULL) ||
	(ctxProperties->glGetColorTableParameteriv == NULL)) {
	return 0;
    }
    
    ctxProperties->glColorTable(GL_PROXY_TEXTURE_COLOR_TABLE_SGI, GL_RGBA, 256, GL_RGB,
                                GL_INT,  NULL);
    ctxProperties->glGetColorTableParameteriv(GL_PROXY_TEXTURE_COLOR_TABLE_SGI,
                                GL_COLOR_TABLE_WIDTH_SGI, &size);
    return size;
}

/* we want to use this if available: */
#define GLX_SGIX_pbuffer 1

#ifndef GLX_VERSION_1_3
#ifdef GLX_SGIX_pbuffer
#ifdef __linux__
typedef XID GLXPbuffer;
typedef struct __GLXFBConfigRec *GLXFBConfig;
typedef struct __GLXFBConfigRec *GLXFBConfigSGIX;
extern GLXFBConfig * glXChooseFBConfig (Display *dpy, int screen, const int *attrib_list, int *nelements);
extern GLXPbuffer glXCreatePbuffer (Display *dpy, GLXFBConfig config, const int *attrib_list);
extern void glXDestroyPbuffer (Display *dpy, GLXPbuffer pbuf);
extern GLXFBConfigSGIX *glXChooseFBConfigSGIX(Display *dpy, int screen, const int *attribList, int *nitems);
extern GLXPbuffer glXCreateGLXPbufferSGIX(Display *dpy, GLXFBConfig config, unsigned int width, unsigned int height, const int *attribList);
extern void glXDestroyGLXPbufferSGIX(Display *dpy, GLXPbuffer pbuf);
#define GLX_DRAWABLE_TYPE               0x8010
#define GLX_PBUFFER_BIT                 0x00000004
#define GLX_RENDER_TYPE                 0x8011
#define GLX_RGBA_BIT                    0x00000001
#define GLX_MAX_PBUFFER_WIDTH           0x8016
#define GLX_MAX_PBUFFER_HEIGHT          0x8017
#define GLX_PRESERVED_CONTENTS          0x801B
#define GLX_PBUFFER_HEIGHT              0x8040  /* New for GLX 1.3 */
#define GLX_PBUFFER_WIDTH               0x8041  /* New for GLX 1.3 */
#define GLX_LARGEST_PBUFFER             0x801C
#define GLX_LARGEST_PBUFFER_SGIX        GLX_LARGEST_PBUFFER
#else

#define GLX_DRAWABLE_TYPE GLX_DRAWABLE_TYPE_SGIX
#define GLX_PBUFFER_BIT   GLX_PBUFFER_BIT_SGIX
#define GLX_RENDER_TYPE   GLX_RENDER_TYPE_SGIX
#define GLX_RGBA_BIT      GLX_RGBA_BIT_SGIX
#endif
#endif /* GLX_SGIX_pbuffer */
#else
#ifdef __linux__
typedef struct __GLXFBConfigRec *GLXFBConfigSGIX;
#endif /* __linux__ */
#endif /* GLX_VERSION_1_3 */

#if defined(SOLARIS) || defined(__linux__)
#pragma weak glXChooseFBConfig
#pragma weak glXCreatePbuffer
#pragma weak glXDestroyPbuffer
#pragma weak glXChooseFBConfigSGIX
#pragma weak glXCreateGLXPbufferSGIX
#pragma weak glXDestroyGLXPbufferSGIX
#endif /* SOLARIS */



/* For dvr support */
JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_videoResize(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jlong display, 
    jint win,
    jfloat dvrFactor)
{
    
#if defined(SOLARIS) || defined(__linux__)
    /* Not need to do ext. supported checking. This check is done in java. */

    /* fprintf(stderr, "Canvas3D.c -- glXVideoResize -- %d %f\n", win, dvrFactor); */
    glXVideoResizeSUN((Display *)display, (Window)win, (float) dvrFactor);
#endif

}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_videoResizeCompensation(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jboolean enable)
{
    
#if defined(SOLARIS) || defined(__linux__)
    GraphicsContextPropertiesInfo *ctxProperties = 
	(GraphicsContextPropertiesInfo *)ctxInfo; 

    if (ctxProperties->videoResizeAvailable) {
	if(enable == JNI_TRUE) {
	    /* fprintf(stderr, "videoResizeCompensation - glEnable"); */
	    glEnable(GL_VIDEO_RESIZE_COMPENSATION_SUN);
	}
	else {
	    /* fprintf(stderr, "videoResizeCompensation - glDisable"); */
	    glDisable(GL_VIDEO_RESIZE_COMPENSATION_SUN);
	}
    }

#endif

}

JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_createOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,    
    jlong display,
    jint vid,
    jint width,
    jint height)
{
    
#if defined(SOLARIS) || defined(__linux__)
   XVisualInfo *vinfo, template;
   int nitems, depth, redSize;
   Display *dpy;
   static GLboolean pbufferSupported = GL_FALSE;
   static GLboolean pbufferExtSupported = GL_FALSE;
   int major, minor;
   const char *extStr;
   int status;

   dpy = (Display *)display;

   if (dpy == NULL)
        dpy = XOpenDisplay(NULL);

   template.visualid = vid;
   vinfo = XGetVisualInfo(dpy, VisualIDMask, &template, &nitems);
   if (nitems != 1) {
      fprintf(stderr, "Warning Canvas3D_createContext got unexpected number of matching visuals %d\n", nitems);
   }

   glXGetConfig (dpy, vinfo, GLX_BUFFER_SIZE, &depth);
   glXGetConfig (dpy, vinfo, GLX_RED_SIZE, &redSize);

   if (status = glXQueryVersion(dpy, &major, &minor)) {

#if 0
       /* don't use the 1.3 pbuffer interface for now. */
	if ((major > 1) || (major == 1 && minor >= 3))
	    pbufferSupported = GL_TRUE;
	else 
#endif
	{
	    extStr = glXQueryExtensionsString(dpy, DefaultScreen(dpy));
	    if ((extStr != NULL) && (strstr(extStr, "GLX_SGIX_pbuffer"))) {
		pbufferExtSupported = GL_TRUE;
	    }
	}
   }

#if defined(GLX_VERSION_1_3) || defined(GLX_SGIX_pbuffer)
   if (pbufferExtSupported || pbufferSupported) {


        int attrCount, configAttr[10], numConfig, val;
        GLXPbuffer pbuff;

        /* Initialize the attribute list to be used for choosing FBConfig */
        attrCount = 0;
        configAttr[attrCount++] = GLX_DRAWABLE_TYPE;
        configAttr[attrCount++] = GLX_PBUFFER_BIT;
        configAttr[attrCount++] = GLX_RENDER_TYPE;
        configAttr[attrCount++] = GLX_RGBA_BIT;
        configAttr[attrCount++] = GLX_RED_SIZE;
        configAttr[attrCount++] = redSize;
        configAttr[attrCount++] = None;
/*
        configAttr[attrCount++] = GLX_DEPTH_SIZE;
        configAttr[attrCount++] = depth;
*/


#ifdef GLX_VERSION_1_3
	if (pbufferSupported) {
            GLXFBConfig *fbconfiglst;

            fbconfiglst = glXChooseFBConfig(dpy, DefaultScreen(dpy),
                        configAttr, &numConfig);

            if (numConfig < 1) {
                fprintf(stderr, "# of configs returned is %d\n", numConfig);
                return None;
            }

	    attrCount = 0;
            configAttr[attrCount++] = GLX_PBUFFER_WIDTH;
            configAttr[attrCount++] = width;
            configAttr[attrCount++] = GLX_PBUFFER_HEIGHT;
            configAttr[attrCount++] = height;
            configAttr[attrCount++] = GLX_PRESERVED_CONTENTS;
            configAttr[attrCount++] = GL_TRUE;
            configAttr[attrCount++] = None;
	

            pbuff = glXCreatePbuffer(dpy, fbconfiglst[0], configAttr);
	}
#endif /* GLX_VERSION_1_3 */

#ifdef GLX_SGIX_pbuffer
	if (pbufferExtSupported && !pbufferSupported) {
            GLXFBConfigSGIX *fbconfiglst;


            /* Determine what config to use according to config_attr */
            fbconfiglst = glXChooseFBConfigSGIX(dpy, DefaultScreen(dpy),
                        configAttr, &numConfig);

            if (numConfig < 1) {
                fprintf(stderr, "# of configs returned is %d\n", numConfig);
                return None;
            }

           attrCount = 0;
            configAttr[attrCount++] = GLX_PRESERVED_CONTENTS;
            configAttr[attrCount++] = GL_TRUE;
            configAttr[attrCount++] = None;
            pbuff = glXCreateGLXPbufferSGIX(dpy, fbconfiglst[0], width, height,
                        configAttr );
	}
#endif /* GLX_SGIX_pbuffer */
        if (pbuff == None) {
            fprintf(stderr, "glXCreateGLXPbuffer() returns None\n");
        }

        return pbuff;

   } else 
#endif /* GLX_VERSION_1_3 || GLX_SGIX_pbuffer */

   {
        Pixmap pixmap;
        GLXPixmap glxpixmap;

        /* fall back to pixmap */
        pixmap = XCreatePixmap(dpy, DefaultRootWindow(dpy), width, height, 
				vinfo->depth);

        glxpixmap = glXCreateGLXPixmap(dpy, vinfo, pixmap);
        if (glxpixmap == None) {
           fprintf(stderr, "glXCreateGLXPixmap() returns None\n");
        }

        return glxpixmap;
   }

#endif /* SOLARIS */

#ifdef WIN32
    int PixelFormatID=0;
    int dpy = (int)display;
    HDC hdc;          /* HW Device Context */
    jboolean rescale = JNI_FALSE;
    JNIEnv table = *env;
    
   HBITMAP hbitmap, oldhbitmap;

   BITMAPINFOHEADER bih;
   void *ppvBits;
   int err;
   LPTSTR errString;
   HDC hdcMonitor;

   /* create a DIB */
   memset(&bih, 0, sizeof(BITMAPINFOHEADER));

   bih.biSize = sizeof(BITMAPINFOHEADER);
   bih.biWidth = width;
   bih.biHeight = height;
   bih.biPlanes = 1;
   bih.biBitCount = 24;
   bih.biCompression = BI_RGB;

   /* create a new device context */

   if (dpy == 0) {
       hdc = CreateCompatibleDC(0);
   } else {
       /*
	* Should be getMonitorDC(screen)
        * but display and screen are the same under windows
        * They are return from NativeScreenInfo
	*/
       hdcMonitor = getMonitorDC((int) display);       
       hdc = CreateCompatibleDC(hdcMonitor);
       DeleteDC(hdcMonitor);
       
   }
   hbitmap = CreateDIBSection(hdc, (BITMAPINFO *)&bih,
				DIB_PAL_COLORS, &ppvBits, NULL, 0);

   if (!hbitmap) {
	err = GetLastError();
        FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
			FORMAT_MESSAGE_FROM_SYSTEM,
			NULL, err, 0, (LPTSTR)&errString, 0, NULL);
	fprintf(stderr, "CreateDIBSection failed: %s\n", errString);
   }

   oldhbitmap = SelectObject(hdc, hbitmap);

   /* Choosing and setting of pixel format is done in createContext */

   return ((jint)hdc);
#endif /* WIN32 */
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_destroyOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,    
    jlong display,
    jint window)
{
#if defined(SOLARIS) || defined(__linux__)
   Display *dpy = (Display*)display;
     
   GLboolean pbufferSupported = GL_FALSE;
   GLboolean pbufferExtSupported = GL_TRUE;
   int major, minor;
   char *extStr;

   if (glXQueryVersion(dpy, &major, &minor)) {

#if 0
       /* don't use the 1.3 pbuffer interface for now. */
	if ((major > 1) || (major == 1 && minor >= 3))
	    pbufferSupported = GL_TRUE;
	else 
#endif
	{
	    extStr = (char *)glXQueryExtensionsString(dpy, 
		DefaultScreen(dpy));
	    if ((extStr != NULL) && (strstr(extStr, "GLX_SGIX_pbuffer"))) {
		pbufferExtSupported = GL_TRUE;
	    }
	}
   }

#if defined(GLX_VERSION_1_3) || defined(GLX_SGIX_pbuffer)
   
   if (pbufferSupported) {
	glXDestroyPbuffer(dpy, (GLXPbuffer)window);
   } else if (pbufferExtSupported) {
	glXDestroyGLXPbufferSGIX(dpy, (GLXPbuffer)window);
   } else
#endif
 
   {
	glXDestroyGLXPixmap(dpy, (GLXPixmap)window);
   }
#endif /* SOLARIS */

#ifdef WIN32
   HBITMAP oldhbitmap;
   HDC hdc = (HDC) window;

   oldhbitmap = SelectObject(hdc, NULL);
   DeleteObject(oldhbitmap);
   DeleteDC(hdc);
#endif /* WIN32 */
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_readOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,    
    jint format,
    jint width,
    jint height)
{
    JNIEnv table = *env;
    jclass cv_class;
    jfieldID byteData_field;
    jbyteArray byteData_array;
    jbyte *byteData;
    int type;
    
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;

    glPixelStorei(GL_PACK_ROW_LENGTH, width);
    glPixelStorei(GL_PACK_ALIGNMENT, 1);

    cv_class =  (jclass) (*(table->GetObjectClass))(env, obj);
    byteData_field = (jfieldID) (*(table->GetFieldID))(env, cv_class,
                                        "byteBuffer", "[B");
    byteData_array = (jbyteArray)(*(table->GetObjectField))(env, obj,
                                        byteData_field);
    byteData = (jbyte *)(*(table->GetPrimitiveArrayCritical))(env,
                                        byteData_array, NULL);

       
    switch (format) {
    case FORMAT_BYTE_RGBA:
	type = GL_RGBA;
	break;
    case FORMAT_BYTE_RGB:
	type = GL_RGB;
	break;
	
	/* GL_ABGR_EXT */
    case FORMAT_BYTE_ABGR:         
	if (ctxProperties->abgr_ext) { /* If its zero, should never come here! */
	    type = GL_ABGR_EXT;
	}
	break;

	/* GL_BGR_EXT */
    case FORMAT_BYTE_BGR:         
	if (ctxProperties->bgr_ext) { /* If its zero, should never come here! */
	    type = ctxProperties->bgr_ext_enum;
	}
	break;
	
    case FORMAT_BYTE_LA:
	type = GL_LUMINANCE_ALPHA;
	break;
    case FORMAT_BYTE_GRAY:
    case FORMAT_USHORT_GRAY:	    
	/* TODO: throw exception */
	break;
    }
  

    glReadPixels(0, 0, width, height, type, GL_UNSIGNED_BYTE, byteData);

    (*(table->ReleasePrimitiveArrayCritical))(env, byteData_array,
                byteData, 0);
}

void initializeCtxInfo(JNIEnv *env , GraphicsContextPropertiesInfo* ctxInfo){
    ctxInfo->context = 0; 
    
    /* version and extension info */
    ctxInfo->versionStr = NULL;
    ctxInfo->extensionStr = NULL;
    ctxInfo->versionNumbers[0] = 1;
    ctxInfo->versionNumbers[1] = 1; 

    /* both in 1.2 core part and 1.1 extensions */
    ctxInfo->rescale_normal_ext = JNI_FALSE;
    ctxInfo->bgr_ext = JNI_FALSE;
    ctxInfo->texture3DAvailable = JNI_FALSE;
    ctxInfo->seperate_specular_color = JNI_FALSE;
    
    /* 1.2 and GL_ARB_imaging */
    ctxInfo->blend_color_ext = JNI_FALSE;
    ctxInfo->color_table_ext = JNI_FALSE;    
    ctxInfo->blendFunctionTable[0] = GL_ZERO;
    ctxInfo->blendFunctionTable[1] = GL_ONE;
    ctxInfo->blendFunctionTable[2] = GL_SRC_ALPHA;
    ctxInfo->blendFunctionTable[3] = GL_ONE_MINUS_SRC_ALPHA;
    ctxInfo->blendFunctionTable[4] = GL_DST_COLOR;
    ctxInfo->blendFunctionTable[5] = GL_SRC_COLOR;
    ctxInfo->blendFunctionTable[6] = GL_ONE_MINUS_SRC_COLOR;
    ctxInfo->blendFunctionTable[7] = GL_SRC_COLOR;

    /* 1.1 extensions or 1.2 extensions */
    /* sun extensions */
    ctxInfo->multi_draw_arrays_sun = JNI_FALSE;
    ctxInfo->compiled_vertex_array_ext = JNI_FALSE;

    ctxInfo->videoResizeAvailable = JNI_FALSE;
    ctxInfo->global_alpha_sun = JNI_FALSE;
    ctxInfo->constant_data_sun = JNI_FALSE;
    ctxInfo->geometry_compression_sunx = JNI_FALSE;
    
    /* EXT extensions */
    ctxInfo->abgr_ext = JNI_FALSE;

    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;

    ctxInfo->implicit_multisample = getJavaBoolEnv(env, "implicitAntialiasing");

    /* ARB extensions */
    ctxInfo->arb_transpose_matrix = JNI_FALSE;
    ctxInfo->arb_multitexture = JNI_FALSE;

    ctxInfo->arb_multisample = JNI_FALSE;
    ctxInfo->textureUnitCount = 1;
    ctxInfo->textureEnvCombineAvailable = JNI_FALSE;
    ctxInfo->textureCombineDot3Available = JNI_FALSE;
    ctxInfo->textureCombineSubtractAvailable = JNI_FALSE;
    ctxInfo->textureCubeMapAvailable = JNI_FALSE;

    /* NV extensions */
    ctxInfo->textureRegisterCombinersAvailable = JNI_FALSE;

    /* SGI extensions */
    ctxInfo->textureSharpenAvailable = JNI_FALSE;
    ctxInfo->textureDetailAvailable = JNI_FALSE;
    ctxInfo->textureFilter4Available = JNI_FALSE;
    ctxInfo->textureAnisotropicFilterAvailable = JNI_FALSE;
    ctxInfo->textureColorTableAvailable = JNI_FALSE;
    ctxInfo->textureColorTableSize = 0;
    ctxInfo->textureLodAvailable = JNI_FALSE;
    ctxInfo->textureLodBiasAvailable = JNI_FALSE;
    
    ctxInfo->geometry_compression_accelerated = JNI_FALSE;
    ctxInfo->geometry_compression_accelerated_major_version = 0;
    ctxInfo->geometry_compression_accelerated_minor_version = 0;
    ctxInfo->geometry_compression_accelerated_subminor_version = 0;

    /* extension mask */
    ctxInfo->extMask = 0;
    ctxInfo->textureExtMask = 0;

    ctxInfo->glBlendColor = NULL;
    ctxInfo->glBlendColorEXT = NULL;    
    ctxInfo->glColorTable =  NULL;
    ctxInfo->glGetColorTableParameteriv =  NULL;
    ctxInfo->glTexImage3DEXT = NULL;
    ctxInfo->glTexSubImage3DEXT = NULL;
    ctxInfo->glClientActiveTextureARB =   NULL;
    ctxInfo->glMultiDrawArraysEXT =  NULL;
    ctxInfo->glMultiDrawElementsEXT =  NULL;
    ctxInfo->glLockArraysEXT =  NULL;
    ctxInfo->glUnlockArraysEXT =  NULL;
    ctxInfo->glMultiTexCoord2fvARB = NULL;
    ctxInfo->glMultiTexCoord3fvARB = NULL;
    ctxInfo->glMultiTexCoord4fvARB = NULL;
    ctxInfo->glLoadTransposeMatrixdARB = NULL;
    ctxInfo->glMultTransposeMatrixdARB = NULL;
    ctxInfo->glActiveTextureARB = NULL;
    ctxInfo->glGlobalAlphaFactorfSUN = NULL;

    ctxInfo->glCombinerInputNV = NULL;
    ctxInfo->glCombinerOutputNV = NULL;
    ctxInfo->glFinalCombinerInputNV = NULL;
    ctxInfo->glCombinerParameterfvNV = NULL;
    ctxInfo->glCombinerParameterivNV= NULL;
    ctxInfo->glCombinerParameterfNV = NULL;
    ctxInfo->glCombinerParameteriNV = NULL;

    ctxInfo->glSharpenTexFuncSGIS = NULL;
    ctxInfo->glDetailTexFuncSGIS = NULL;
    ctxInfo->glTexFilterFuncSGIS = NULL;
}

void cleanupCtxInfo(GraphicsContextPropertiesInfo* ctxInfo){
    if( ctxInfo->versionStr != NULL)
	free(ctxInfo->versionStr);
    if( ctxInfo->extensionStr != NULL)
	free(ctxInfo->extensionStr);
    ctxInfo->versionStr = NULL;
    ctxInfo->extensionStr = NULL;
}

#ifdef WIN32
HWND createDummyWindow(const char* szAppName) {
    static char szTitle[]="A Simple C OpenGL Program";
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
    
    if(RegisterClass( &wc )==0)
	fprintf(stdout, "Couldn't register class\n");
  
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
	fprintf(stdout, "Couldn't Create window\n");
	return NULL;
    }
    return hWnd; 
}
#endif

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_createQueryContext(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint window,
    jint vid,
    jboolean offScreen,
    jint width,
    jint height)
{
    JNIEnv table = *env;
    jlong gctx;
    long newWin;
    int PixelFormatID=0;
    GraphicsContextPropertiesInfo* ctxInfo = (GraphicsContextPropertiesInfo *)malloc(sizeof(GraphicsContextPropertiesInfo)); 
	
#if defined(SOLARIS) || defined(__linux__)
    XVisualInfo *vinfo, template;
    int nitems;
    GLXContext ctx;
    int result;
    Window root;
    Window glWin; 
    XSetWindowAttributes win_attrs;
    Colormap		cmap;
    unsigned long	win_mask;
    jlong hdc;
    
    template.visualid = vid;
    vinfo = XGetVisualInfo((Display *)display, VisualIDMask, &template, &nitems);
    if (nitems != 1) {
	fprintf(stderr, "Warning Canvas3D_createQueryContext got unexpected number of matching visuals %d\n", nitems);
    }

    ctx = glXCreateContext((Display *)display, vinfo, NULL, True);
    if (ctx == NULL) {
	fprintf(stderr, "Error Canvas3D_createQueryContext: couldn't create context.\n");
    }
   
   
    /* create window if window == 0 and offscreen == true */
    if(window == 0 && !offScreen) {
     
	root = RootWindow((Display *)display, vinfo->screen);
    
	/* Create a colormap */
	cmap = XCreateColormap((Display *)display, root, vinfo->visual, AllocNone);

	/* Create a window */
	win_attrs.colormap = cmap;
	win_attrs.border_pixel = 0;
	win_attrs.event_mask = KeyPressMask | ExposureMask | StructureNotifyMask;
	win_mask = CWColormap | CWBorderPixel | CWEventMask;
	glWin = XCreateWindow((Display *)display, root, 0, 0, width, height, 0, vinfo->depth,
			      InputOutput, vinfo->visual, win_mask, &win_attrs);
	newWin = (unsigned long)glWin; 
    }
    else if(window == 0 && offScreen){
	newWin = Java_javax_media_j3d_Canvas3D_createOffScreenBuffer( env, obj, 0, display, vid, width, height);
    }
    else if(window != 0) {
	newWin = window;
    }

    result = glXMakeCurrent((Display *)display, (GLXDrawable)newWin, (GLXContext)ctx);
    if (result == GL_FALSE)
	fprintf(stderr, "glXMakeCurrent fails\n");
    gctx = (jlong)ctx;
#endif

#ifdef WIN32
    HGLRC hrc;        /* HW Rendering Context */
    HDC hdc;          /* HW Device Context */
    DWORD err;
    LPTSTR errString;
    HWND hWnd;
    static char szAppName[] = "OpenGL";
    jlong vinfo = 0;
    
    static PIXELFORMATDESCRIPTOR pfd = {
	sizeof(PIXELFORMATDESCRIPTOR),
	1,                      /* Version number */
	PFD_DRAW_TO_WINDOW |
	PFD_SUPPORT_OPENGL|
	PFD_DOUBLEBUFFER,
	PFD_TYPE_RGBA,
	24,                     /* 24 bit color depth */
	0, 0, 0,                /* RGB bits and pixel sizes */
	0, 0, 0,                /* Donnot care about them  */
	0, 0,                   /* no alpha buffer info */
	0, 0, 0, 0, 0,          /* no accumulation buffer */
	32,                     /* 16 bit depth buffer */
	0,                      /* no stencil buffer */
	0,                      /* no auxiliary buffers */
	PFD_MAIN_PLANE,         /* layer type */
	0,                      /* reserved, must be 0 */
	0,                      /* no layer mask */
	0,                      /* no visible mask */
	0                       /* no damage mask */
    };

    jboolean result;

    /* onscreen rendering and window is 0 now */
    if(window == 0 && !offScreen){
	hWnd = createDummyWindow((const char *)szAppName);
	if (!hWnd)
	    return; 
	hdc =  GetDC(hWnd);
    }
    else if(window == 0 && offScreen){
	hdc = (HDC)Java_javax_media_j3d_Canvas3D_createOffScreenBuffer( env, obj, 0, display, vid, width, height);
	pfd.dwFlags = PFD_DRAW_TO_BITMAP | PFD_SUPPORT_OPENGL |
	    PFD_SUPPORT_GDI;
	vid = -1;
    }
    else if(window != 0 && offScreen){
	pfd.dwFlags = PFD_DRAW_TO_BITMAP | PFD_SUPPORT_OPENGL |
	    PFD_SUPPORT_GDI;
	vid = -1;
	hdc =  (HDC) window;
    }
    else if(window !=0 && !offScreen){
	hdc =  (HDC) window;
    }

    newWin = (int)hdc;
   
    /* vid of -1 means no vid was specified - do the old way */
    if (vid == -1) {
	/*
	 * choose the "pixel format", terminology is equivalent
	 * to UNIX "visual"
	 */
	PixelFormatID = ChoosePixelFormat(hdc, &pfd);

	if(PixelFormatID == 0) {
	    fprintf(stderr,"\nERROR: pixel format ID = 0");
	    return; 
	}
    }
    else
	PixelFormatID = vid;

    SetPixelFormat(hdc, PixelFormatID, &pfd);

    hrc = wglCreateContext( hdc );
    
    if (!hrc) {
	err = GetLastError();
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		      FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, err, 0, (LPTSTR)&errString, 0, NULL);

	fprintf(stderr, "wglCreateContext Failed: %s\n", errString);
    }


    result = wglMakeCurrent(hdc, hrc);

    if (!result) {
	err = GetLastError();
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		      FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, err, 0, (LPTSTR)&errString, 0, NULL);
	fprintf(stderr, "wglMakeCurrent Failed: %s\n", errString);
    }

    gctx = (jlong)hrc;

#endif

    initializeCtxInfo(env, ctxInfo);
    ctxInfo->context = gctx;
    
    /* get current context properties */
    if (getPropertiesFromCurrentContext(env, obj, ctxInfo, (jlong) hdc, PixelFormatID, display, (jlong) vinfo)) {
	/* put the properties to the Java side */
	setupCanvasProperties(env, obj, ctxInfo);
    }


    /* clear up the context , colormap and window if appropriate */
    if(window == 0 && !offScreen){
#if defined(SOLARIS) || defined(__linux__)
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display, newWin, (jlong)ctxInfo); 
	XDestroyWindow((Display *)display, glWin);
	XFreeColormap((Display *)display, cmap);
#endif /* SOLARIS */
#ifdef WIN32
	/* Release DC */
	ReleaseDC(hWnd, hdc);
	/* Destroy context */
	/* This will free ctxInfo also */
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display,newWin, (jlong)ctxInfo);
	DestroyWindow(hWnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
#endif /* WIN32 */
    }
    else if(window == 0 && offScreen) {
	Java_javax_media_j3d_Canvas3D_destroyOffScreenBuffer(env, obj, gctx, display, newWin);
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display, newWin, (jlong)ctxInfo);
    }
    else if(window != 0){
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display, newWin, (jlong)ctxInfo);
    }
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_beginScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctxInfo)
{
 /* Not used by OGL version */
}


JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_endScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctxInfo)
{
    /* This function is a no-op */
    /*
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    */
}

/* Setup the multisampling for full scene antialiasing */
JNIEXPORT void JNICALL Java_javax_media_j3d_Canvas3D_setFullSceneAntialiasing
(JNIEnv *env, jobject obj, jlong ctxInfo, jboolean enable)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

    if (ctxProperties->arb_multisample && !ctxProperties->implicit_multisample) {
	if(enable == JNI_TRUE) {
	    glEnable(MULTISAMPLE_ARB);
	}
	else {
	    glDisable(MULTISAMPLE_ARB);

	}
    }
    
}


/*
 * Return false if <= 8 bit color under windows
 */
JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_validGraphicsMode(
       JNIEnv *env,
       jobject obj) 
{
#ifdef WIN32
    DEVMODE devMode;
    
    EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &devMode);
    return (devMode.dmBitsPerPel > 8);
#endif

#if defined(SOLARIS) || defined(__linux__)
    return TRUE;
#endif
}
