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

#if defined(LINUX)
#define _GNU_SOURCE 1
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jni.h>

#include "gldefs.h"

#if defined(UNIX)
#include <dlfcn.h>
#endif

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


extern void throwAssert(JNIEnv *env, char *str);

static void initializeCtxInfo(JNIEnv *env, GraphicsContextPropertiesInfo* ctxInfo);
static void cleanupCtxInfo(GraphicsContextPropertiesInfo* ctxInfo);
static void disableAttribFor2D(GraphicsContextPropertiesInfo *ctxProperties);

/*
 * Class:     javax_media_j3d_Canvas3D
 * Method:    getTextureColorTableSize
 * Signature: ()I
 */
extern int getTextureColorTableSize(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    char *extensionStr,
    int minorVersion);


extern void checkGLSLShaderExtensions(
    JNIEnv *env,
    jobject obj,
    char *tmpExtensionStr,
    GraphicsContextPropertiesInfo *ctxInfo,
    jboolean glslLibraryAvailable);

extern void checkCgShaderExtensions(
    JNIEnv *env,
    jobject obj,
    char *tmpExtensionStr,
    GraphicsContextPropertiesInfo *ctxInfo,
    jboolean cgLibraryAvailable);


#ifdef WIN32
extern void printErrorMessage(char *message);
extern PIXELFORMATDESCRIPTOR getDummyPFD();
extern HDC getMonitorDC(int screen);
HWND createDummyWindow(const char* szAppName);
#endif

/*
 * Extract the version numbers from a copy of the version string.
 * Upon return, numbers[0] contains major version number
 * numbers[1] contains minor version number
 * Note that the passed in version string is modified.
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

    return; 
}

/*
 * check if the extension is supported
 */
int
isExtensionSupported(const char *allExtensions, const char *extension)
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


static void
checkTextureExtensions(
    JNIEnv *env,
    jobject obj,
    char *tmpExtensionStr,
    int versionNumber,
    GraphicsContextPropertiesInfo* ctxInfo)
{
    if (isExtensionSupported(tmpExtensionStr, "GL_ARB_multitexture")) {
	ctxInfo->arb_multitexture = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_MULTI_TEXTURE;
        glGetIntegerv(GL_MAX_TEXTURE_UNITS_ARB, &ctxInfo->maxTextureUnits);
        ctxInfo->maxTexCoordSets = ctxInfo->maxTextureUnits;
        if (isExtensionSupported(tmpExtensionStr, "GL_ARB_vertex_shader")) {
            glGetIntegerv(GL_MAX_TEXTURE_COORDS_ARB, &ctxInfo->maxTexCoordSets);
        }
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
#if defined(UNIX)
       ctxInfo->glCombinerInputNV =
	   (MYPFNGLCOMBINERINPUTNV) dlsym(RTLD_DEFAULT, "glCombinerInputNV");
       ctxInfo->glFinalCombinerInputNV =
	   (MYPFNGLFINALCOMBINERINPUTNV) dlsym(RTLD_DEFAULT, "glFinalCombinerInputNV");
       ctxInfo->glCombinerOutputNV =
	   (MYPFNGLCOMBINEROUTPUTNV) dlsym(RTLD_DEFAULT, "glCombinerOutputNV");
       ctxInfo->glCombinerParameterfvNV =
	   (MYPFNGLCOMBINERPARAMETERFVNV) dlsym(RTLD_DEFAULT, "glCombinerParameterfvNV");
       ctxInfo->glCombinerParameterivNV =
	   (MYPFNGLCOMBINERPARAMETERIVNV) dlsym(RTLD_DEFAULT, "glCombinerParameterivNV");
       ctxInfo->glCombinerParameterfNV =
	   (MYPFNGLCOMBINERPARAMETERFNV) dlsym(RTLD_DEFAULT, "glCombinerParameterfNV");
       ctxInfo->glCombinerParameteriNV =
	   (MYPFNGLCOMBINERPARAMETERINV) dlsym(RTLD_DEFAULT, "glCombinerParameteriNV");
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
#if defined(UNIX)
	ctxInfo->glSharpenTexFuncSGIS = 
	    (MYPFNGLSHARPENTEXFUNCSGI) dlsym(RTLD_DEFAULT, "glSharpenTexFuncSGIS");
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
#if defined(UNIX)
	ctxInfo->glDetailTexFuncSGIS = 
	    (MYPFNGLDETAILTEXFUNCSGI) dlsym(RTLD_DEFAULT, "glDetailTexFuncSGIS");
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
#if defined(UNIX)
	ctxInfo->glTexFilterFuncSGIS = 
	    (MYPFNGLTEXFILTERFUNCSGI) dlsym(RTLD_DEFAULT, "glTexFilterFuncSGIS");
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

jboolean
getJavaBoolEnv(JNIEnv *env, char* envStr)
{
    JNIEnv table = *env;
    jclass cls;
    jfieldID fieldID;
    jobject obj;
    
    cls = (jclass) (*(table->FindClass))(env, "javax/media/j3d/VirtualUniverse");

    if (cls == NULL) {
	return JNI_FALSE;
    }
    
    fieldID = (jfieldID) (*(table->GetStaticFieldID))(env, cls, "mc",
						      "Ljavax/media/j3d/MasterControl;");
    if (fieldID == NULL) {
	return JNI_FALSE;	
    }

    obj = (*(table->GetStaticObjectField))(env, cls, fieldID);

    if (obj == NULL) {
	return JNI_FALSE;
    }

    cls = (jclass) (*(table->FindClass))(env, "javax/media/j3d/MasterControl");    

    if (cls == NULL) {
	return JNI_FALSE;
    }

    fieldID = (jfieldID) (*(table->GetFieldID))(env, cls, envStr, "Z");

    if (fieldID == NULL ) {
	return JNI_FALSE;
    }

    return (*(table->GetBooleanField))(env, obj, fieldID);
}

/*
 * Dummy functions for language-independent vertex attribute functions
 */
static void
dummyVertexAttrPointer(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, int size, int type, int stride,
    const void *pointer)
{
#ifdef DEBUG
    fprintf(stderr, "dummyVertexAttrPointer()\n");
#endif /* DEBUG */
}

static void
dummyEnDisableVertexAttrArray(
    GraphicsContextPropertiesInfo *ctxProperties, int index)
{
#ifdef DEBUG
    fprintf(stderr, "dummyEnDisableVertexAttrArray()\n");
#endif /* DEBUG */
}

static void
dummyVertexAttr(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, const float *v)
{
#ifdef DEBUG
    fprintf(stderr, "dummyVertexAttr()\n");
#endif /* DEBUG */
}

/*
 * get properties from current context
 */
static jboolean
getPropertiesFromCurrentContext(
    JNIEnv *env,
    jobject obj,
    GraphicsContextPropertiesInfo *ctxInfo,
    jlong hdc,
    int pixelFormat,
    jlong fbConfigListPtr,
    jboolean offScreen,
    jboolean glslLibraryAvailable,
    jboolean cgLibraryAvailable)
{
    JNIEnv table = *env; 

    /* version and extension */
    char *glVersion;
    char *glVendor;
    char *glRenderer;
    char *extensionStr;
    char *tmpVersionStr;
    char *tmpExtensionStr;
    int   versionNumbers[2];
    char *cgHwStr = 0;

#ifdef WIN32
    PixelFormatInfo *PixelFormatInfoPtr = (PixelFormatInfo *)fbConfigListPtr;
#endif
    
    /* Get the list of extension */
    extensionStr = (char *)glGetString(GL_EXTENSIONS);
    if (extensionStr == NULL) {
        fprintf(stderr, "extensionStr == null\n");
        return JNI_FALSE;
    }
    tmpExtensionStr = strdup(extensionStr);

    /* Get the OpenGL version */
    glVersion = (char *)glGetString(GL_VERSION);
    if (glVersion == NULL) {
	fprintf(stderr, "glVersion == null\n");
	return JNI_FALSE;
    }
    tmpVersionStr = strdup(glVersion);

    /* Get the OpenGL vendor and renderer */
    glVendor = (char *)glGetString(GL_VENDOR);
    if (glVendor == NULL) {
        glVendor = "<UNKNOWN>";
    }
    glRenderer = (char *)glGetString(GL_RENDERER);
    if (glRenderer == NULL) {
        glRenderer = "<UNKNOWN>";
    }

    /*
      fprintf(stderr, " pixelFormat : %d\n", pixelFormat);
      fprintf(stderr, " extensionStr : %s\n", tmpExtensionStr);
    */
    
    ctxInfo->versionStr = strdup(glVersion);
    ctxInfo->vendorStr = strdup(glVendor);
    ctxInfo->rendererStr = strdup(glRenderer);
    ctxInfo->extensionStr = strdup(extensionStr);

    /* find out the version, major and minor version number */
    extractVersionInfo(tmpVersionStr, versionNumbers);

    /* *********************************************************/
    /* setup the graphics context properties */

    /* NOTE : At some point we will want to require OpenGL 1.3 */
    /* Check for OpenGL 1.2 core or better */
    if ((versionNumbers[0] > 1) ||
	(versionNumbers[0] == 1 && versionNumbers[1] >= 2)) {

	if (versionNumbers[0] == 1 && versionNumbers[1] == 2) {
	    fprintf(stderr,
		"Java 3D WARNING : OpenGL 1.3 will be required in the near future (GL_VERSION=%d.%d)\n",
		versionNumbers[0], versionNumbers[1]);
	}

        ctxInfo->rescale_normal_ext = JNI_TRUE;
	ctxInfo->rescale_normal_ext_enum = GL_RESCALE_NORMAL;
	ctxInfo->bgr_ext = JNI_TRUE;
	ctxInfo->bgr_ext_enum = GL_BGR;
	ctxInfo->texture3DAvailable = JNI_TRUE;
	ctxInfo->textureExtMask |= javax_media_j3d_Canvas3D_TEXTURE_3D;
#if defined(UNIX)
	ctxInfo->glTexImage3DEXT = (MYPFNGLTEXIMAGE3DPROC )dlsym(RTLD_DEFAULT, "glTexImage3D");
	ctxInfo->glTexSubImage3DEXT = (MYPFNGLTEXSUBIMAGE3DPROC )dlsym(RTLD_DEFAULT, "glTexSubImage3D");
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
	    
	    ctxInfo->blendFunctionTable[BLEND_CONSTANT_COLOR] = GL_CONSTANT_COLOR;
#if defined(UNIX)
	    ctxInfo->glBlendColor = (MYPFNGLBLENDCOLORPROC )dlsym(RTLD_DEFAULT, "glBlendColor");
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
    }
    else {
	jclass rte;

	fprintf(stderr,
		"Java 3D ERROR : OpenGL 1.2 or better is required (GL_VERSION=%d.%d)\n",
		versionNumbers[0], versionNumbers[1]);
	if ((rte = (*(table->FindClass))(env, "java/lang/IllegalStateException")) != NULL) {
	    (*(table->ThrowNew))(env, rte, "GL_VERSION");
	}
	return JNI_FALSE;
    }

    /*
     * TODO: Remove extension checks for those features that are core
     * in OpenGL 1.2 and just use the core feature.
     */

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

#if defined(UNIX)
    /*
     * setup ARB_multisample, under windows this is setup in
     * NativeConfigTemplate when pixel format is choose
     */
    if (isExtensionSupported(tmpExtensionStr, "GL_ARB_multisample")){
	ctxInfo->arb_multisample = JNI_TRUE;

    }
#endif

#ifdef WIN32
    if(offScreen) {
	ctxInfo->arb_multisample = PixelFormatInfoPtr->offScreenHasMultisample;
    }
    else {
	ctxInfo->arb_multisample = PixelFormatInfoPtr->onScreenHasMultisample;
    }

    /*
      fprintf(stderr, "Canvas3D - onScreenHasMultisample = %d, offScreenHasMultisample = %d\n",
      PixelFormatInfoPtr->onScreenHasMultisample,
      PixelFormatInfoPtr->offScreenHasMultisample);
    
      fprintf(stderr, "Canvas3D - ctxInfo->arb_multisample = %d, offScreen = %d\n",
      ctxInfo->arb_multisample, offScreen);
    */
    
#endif
    
    /*
     * Disable multisample by default since OpenGL will enable
     * it by default if the surface is multisample capable.
     */
    if (ctxInfo->arb_multisample && !ctxInfo->implicit_multisample) {
	glDisable(GL_MULTISAMPLE_ARB);
    }

    /* Check texture extensions */
    checkTextureExtensions(env, obj, tmpExtensionStr, versionNumbers[1],
			   ctxInfo);

    /* Check shader extensions */
    checkGLSLShaderExtensions(env, obj, tmpExtensionStr, ctxInfo, glslLibraryAvailable);
    checkCgShaderExtensions(env, obj, tmpExtensionStr, ctxInfo, cgLibraryAvailable);

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

#endif
    
#if defined(UNIX)
    if(ctxInfo->multi_draw_arrays_ext) {
	ctxInfo->glMultiDrawArraysEXT =
	    (MYPFNGLMULTIDRAWARRAYSEXTPROC)dlsym(RTLD_DEFAULT, "glMultiDrawArraysEXT");
	ctxInfo->glMultiDrawElementsEXT =
	    (MYPFNGLMULTIDRAWELEMENTSEXTPROC)dlsym(RTLD_DEFAULT, "glMultiDrawElementsEXT");
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;
	}
    }
    else if (ctxInfo->multi_draw_arrays_sun) {
	ctxInfo->glMultiDrawArraysEXT =
	    (MYPFNGLMULTIDRAWARRAYSEXTPROC)dlsym(RTLD_DEFAULT, "glMultiDrawArraysSUN");
	ctxInfo->glMultiDrawElementsEXT =
	    (MYPFNGLMULTIDRAWELEMENTSEXTPROC)dlsym(RTLD_DEFAULT, "glMultiDrawElementsSUN");
	if ((ctxInfo->glMultiDrawArraysEXT == NULL) ||
	    (ctxInfo->glMultiDrawElementsEXT == NULL)) {
	    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;
	}
    }
    if(ctxInfo->compiled_vertex_array_ext) {
	ctxInfo->glLockArraysEXT =
	    (MYPFNGLLOCKARRAYSEXTPROC)dlsym(RTLD_DEFAULT, "glLockArraysEXT");
	ctxInfo->glUnlockArraysEXT =
	    (MYPFNGLUNLOCKARRAYSEXTPROC)dlsym(RTLD_DEFAULT, "glUnlockArraysEXT");
	if ((ctxInfo->glLockArraysEXT == NULL) ||
	    (ctxInfo->glUnlockArraysEXT == NULL)) {
	    ctxInfo->compiled_vertex_array_ext = JNI_FALSE;
	}
    }    

    if(ctxInfo->arb_multitexture){
	ctxInfo->glClientActiveTextureARB =
	    (MYPFNGLCLIENTACTIVETEXTUREARBPROC)dlsym(RTLD_DEFAULT, "glClientActiveTextureARB");
	ctxInfo->glMultiTexCoord2fvARB =
	    (MYPFNGLMULTITEXCOORD2FVARBPROC)dlsym(RTLD_DEFAULT, "glMultiTexCoord2fvARB");
	ctxInfo->glMultiTexCoord3fvARB =
	    (MYPFNGLMULTITEXCOORD3FVARBPROC)dlsym(RTLD_DEFAULT, "glMultiTexCoord3fvARB");
	ctxInfo->glMultiTexCoord4fvARB =
	    (MYPFNGLMULTITEXCOORD4FVARBPROC)dlsym(RTLD_DEFAULT, "glMultiTexCoord4fvARB");
	ctxInfo->glActiveTextureARB =
	    (MYPFNGLACTIVETEXTUREARBPROC)dlsym(RTLD_DEFAULT, "glActiveTextureARB");
	if ((ctxInfo->glClientActiveTextureARB == NULL) ||
	    (ctxInfo->glMultiTexCoord2fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord3fvARB == NULL) ||
	    (ctxInfo->glMultiTexCoord4fvARB == NULL) ||
	    (ctxInfo->glActiveTextureARB == NULL)) {
	    ctxInfo->arb_multitexture = JNI_FALSE;
	}
    }
    if(ctxInfo->arb_transpose_matrix) {
	ctxInfo->glLoadTransposeMatrixdARB =
	    (MYPFNGLLOADTRANSPOSEMATRIXDARBPROC)dlsym(RTLD_DEFAULT, "glLoadTransposeMatrixdARB");
	ctxInfo->glMultTransposeMatrixdARB =
	    (MYPFNGLMULTTRANSPOSEMATRIXDARBPROC)dlsym(RTLD_DEFAULT, "glMultTransposeMatrixdARB");
	if ((ctxInfo->glLoadTransposeMatrixdARB == NULL) ||
	    (ctxInfo->glMultTransposeMatrixdARB == NULL)) {
	    ctxInfo->arb_transpose_matrix = JNI_FALSE;
	}
    }
    if(ctxInfo->global_alpha_sun) {
	ctxInfo->glGlobalAlphaFactorfSUN =
	    (MYPFNGLGLOBALALPHAFACTORFSUNPROC)dlsym(RTLD_DEFAULT, "glGlobalAlphaFactorfSUN");
	if (ctxInfo->glGlobalAlphaFactorfSUN == NULL) {
	    ctxInfo->global_alpha_sun = JNI_FALSE;
	}
    }
    
    if(ctxInfo->videoResizeAvailable) {
	ctxInfo->glXVideoResizeSUN =
	    (MYPFNGLXVIDEORESIZESUN)dlsym(RTLD_DEFAULT, "glXVideoResizeSUN");
	if (ctxInfo->glXVideoResizeSUN == NULL) {
	    ctxInfo->videoResizeAvailable = JNI_FALSE;
	    ctxInfo->extMask &= ~javax_media_j3d_Canvas3D_SUN_VIDEO_RESIZE;
	}
    }

#endif /* UNIX */
    
    /* clearing up the memory */
    free(tmpExtensionStr);
    free(tmpVersionStr);
    return JNI_TRUE;
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
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxTextureUnits", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxTextureUnits);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxTexCoordSets", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxTexCoordSets);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxTextureImageUnits", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxTextureImageUnits);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxVertexTextureImageUnits", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxVertexTextureImageUnits);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxCombinedTextureImageUnits", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxCombinedTextureImageUnits);
	rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "maxVertexAttrs", "I");
	(*(table->SetIntField))(env, obj, rsc_field, ctxInfo->maxVertexAttrs);
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

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "nativeGraphicsVendor", "Ljava/lang/String;");
    (*(table->SetObjectField))(env, obj, rsc_field, (*env)->NewStringUTF(env, ctxInfo->vendorStr));

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "nativeGraphicsRenderer", "Ljava/lang/String;");
    (*(table->SetObjectField))(env, obj, rsc_field, (*env)->NewStringUTF(env, ctxInfo->rendererStr));

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

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "shadingLanguageGLSL", "Z");
    (*(table->SetBooleanField))(env, obj, rsc_field, ctxInfo->shadingLanguageGLSL);

    rsc_field = (jfieldID) (*(table->GetFieldID))(env, cv_class, "shadingLanguageCg", "Z");
    (*(table->SetBooleanField))(env, obj, rsc_field, ctxInfo->shadingLanguageCg);
    
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
    
#if defined(UNIX)
    /*
    glXMakeCurrent((Display *)display, None, NULL);
    */
    glXDestroyContext((Display *)display, (GLXContext)context);
#endif /* UNIX */
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
jlong JNICALL Java_javax_media_j3d_Canvas3D_createNewContext(
    JNIEnv *env, 
    jobject obj, 
    jlong display,
    jint window, 
    jint vid,
    jlong fbConfigListPtr,
    jlong sharedCtxInfo,
    jboolean isSharedCtx,
    jboolean offScreen,
    jboolean glslLibraryAvailable,
    jboolean cgLibraryAvailable)
{
    jlong gctx;
    jlong sharedCtx;
    int stencilSize=0;
    
    static GLboolean first_time = GL_TRUE;
    static GLboolean force_normalize = GL_FALSE;
    
    GraphicsContextPropertiesInfo *ctxInfo = NULL;
    GraphicsContextPropertiesInfo *sharedCtxStructure;
    int PixelFormatID=0;
        
#if defined(UNIX)

    /* Fix for issue 20 */

    GLXContext ctx;
    jlong hdc;

    GLXFBConfig *fbConfigList = NULL;
    
    fbConfigList = (GLXFBConfig *)fbConfigListPtr;

    /*
    fprintf(stderr, "Canvas3D_createNewContext: \n");
    fprintf(stderr, "    fbConfigListPtr 0x%x\n", (int) fbConfigListPtr);
    fprintf(stderr, "    fbConfigList 0x%x, fbConfigList[0] 0x%x\n",
	    (int) fbConfigList, (int) fbConfigList[0]);
    fprintf(stderr, "    glslLibraryAvailable = %d\n", glslLibraryAvailable);
    fprintf(stderr, "    cgLibraryAvailable = %d\n", cgLibraryAvailable);
    */
    
    if(sharedCtxInfo == 0)
	sharedCtx = 0;
    else {
	sharedCtxStructure = (GraphicsContextPropertiesInfo *)sharedCtxInfo;
	sharedCtx = sharedCtxStructure->context;
    }

    if (display == 0) {
	fprintf(stderr, "Canvas3D_createNewContext: display is null\n");
	ctx = NULL;
    }
    else if((fbConfigList == NULL) || (fbConfigList[0] == NULL)) {
	/*
	 * fbConfig must be a valid pointer to an GLXFBConfig struct returned
	 * by glXChooseFBConfig() for a physical screen.  The visual id in vid
	 * is not sufficient for handling OpenGL with Xinerama mode disabled:
	 * it doesn't distinguish between the physical screens making up the
	 * virtual screen when the X server is running in Xinerama mode.
	 */
	fprintf(stderr, "Canvas3D_createNewContext: FBConfig is null\n");
	ctx = NULL;
    }
    else {
        ctx = glXCreateNewContext((Display *)display, fbConfigList[0],
				  GLX_RGBA_TYPE, (GLXContext)sharedCtx, True);
    }
    
    if (ctx == NULL) {
        fprintf(stderr, "Canvas3D_createNewContext: couldn't create context\n");
	return 0;
    } 

    /* There is a known interportability issue between Solaris and Linux(Nvidia)
       on the new glxMakeContextCurrent() call. Bug Id  5109045.
       if (!glXMakeContextCurrent((Display *)display, (GLXDrawable)window,
       (GLXDrawable)window,(GLXContext)ctx)) {
    */

    if (!glXMakeCurrent((Display *)display, (GLXDrawable)window,(GLXContext)ctx)) {
	
        fprintf( stderr, "Canvas3D_createNewContext: couldn't make current\n");
        return 0;
    }

    /* Shouldn't this be moved to NativeConfig. ? */
    glXGetFBConfigAttrib((Display *) display, fbConfigList[0], 
			 GLX_STENCIL_SIZE, &stencilSize);

    
    gctx = (jlong)ctx;
#endif /* UNIX */

#ifdef WIN32
    HGLRC hrc; /* HW Rendering Context */
    HDC hdc;   /* HW Device Context */
    jboolean rescale = JNI_FALSE;
    JNIEnv table = *env;
    DWORD err;
    LPTSTR errString;    
    jboolean result;
    PixelFormatInfo *PixelFormatInfoPtr = (PixelFormatInfo *)fbConfigListPtr;
    
    /* Fix for issue 76 */

    
    /*
      fprintf(stderr, "Canvas3D_createNewContext: \n");
      fprintf(stderr, "vid %d window 0x%x\n", vid, window);
    */
    if(sharedCtxInfo == 0)
	sharedCtx = 0;
    else {
	sharedCtxStructure = (GraphicsContextPropertiesInfo *)sharedCtxInfo;
	sharedCtx = sharedCtxStructure->context;
    }
    
    hdc =  (HDC) window;

    /* Need to handle onScreen and offScreen differently */
    /* vid is for onScreen and fbConfigListPtr is for offScreen */ 
    /*
     * vid must be a PixelFormat returned
     * by wglChoosePixelFormat() or wglChoosePixelFormatARB.
     */

    if(!offScreen) {  /* Fix to issue 104 */
	if ((PixelFormatInfoPtr == NULL) || (PixelFormatInfoPtr->onScreenPFormat <= 0)) {
	    printErrorMessage("Canvas3D_createNewContext: onScreen PixelFormat is invalid");
	    return 0;
	}
	else {
	    PixelFormatID = PixelFormatInfoPtr->onScreenPFormat;
	}
    }
    else { /* offScreen case */	    
	if ((PixelFormatInfoPtr == NULL) || (PixelFormatInfoPtr->offScreenPFormat <= 0)) {
	    printErrorMessage("Canvas3D_createNewContext: offScreen PixelFormat is invalid");
	    return 0;
	}
	else {
	    PixelFormatID = PixelFormatInfoPtr->offScreenPFormat;
	}
    }
    
    SetPixelFormat(hdc, PixelFormatID, NULL);

    /* fprintf(stderr, "Before wglCreateContext\n"); */

    hrc = wglCreateContext( hdc );

    /* fprintf(stderr, "After wglCreateContext hrc = 0x%x\n", hrc); */

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

    /* fprintf(stderr, "Before wglMakeCurrent\n"); */
    result = wglMakeCurrent(hdc, hrc);
    /* fprintf(stderr, "After wglMakeCurrent result = %d\n", result); */

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

    if (!getPropertiesFromCurrentContext(env, obj, ctxInfo, (jlong) hdc, PixelFormatID,
					 fbConfigListPtr, offScreen,
					 glslLibraryAvailable, cgLibraryAvailable)) {
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
#if defined(UNIX)
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
    
    table = *env;
   
#ifdef VERBOSE
    fprintf(stderr, "Canvas3D.composite()\n");
#endif
    /* Temporarily disable fragment and most 3D operations */
    /* XXXX: the GL_TEXTURE_BIT may not be necessary here */
    glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_DEPTH_BUFFER_BIT);
    disableAttribFor2D(ctxProperties);

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
    
    table = *env;
    gltype = GL_RGBA;
    
    /* Temporarily disable fragment and most 3D operations */
    glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_DEPTH_BUFFER_BIT|GL_POLYGON_BIT);
    disableAttribFor2D(ctxProperties);

    /* Reset the polygon mode */
    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

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
	glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
	glClear(GL_COLOR_BUFFER_BIT); 
    }
    else {
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

	/* Temporarily disable fragment and most 3D operations */
	/* XXXX: the GL_TEXTURE_BIT may not be necessary */
	glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT); 
	disableAttribFor2D(ctxProperties);

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
	default:
	    throwAssert(env, "illegal format");
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
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
		glClear(GL_COLOR_BUFFER_BIT); 
	    }
	    glPixelZoom(1.0, -1.0);
	    glDrawPixels(width, height, gltype, GL_UNSIGNED_BYTE,
			 pixels);

	    break;
	case javax_media_j3d_Background_SCALE_FIT_MIN:
	    if(xzoom != yzoom ) {
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
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
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
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

	/* Restore attributes */
	glPopAttrib();
		
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
	glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue); 
	glClear(GL_COLOR_BUFFER_BIT); 
    }
    /* glPushAttrib(GL_DEPTH_BUFFER_BIT); */
    if (pa2d) { 
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
	
	/* Temporarily disable fragment and most 3D operations */
	glPushAttrib(GL_ENABLE_BIT|GL_TEXTURE_BIT|GL_POLYGON_BIT); 
	disableAttribFor2D(ctxProperties);

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
	    default:
		throwAssert(env, "illegal format");
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
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
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
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
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
		glClearColor((float)r, (float)g, (float)b, ctxProperties->alphaClearValue);
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
	/* Restore attributes */
	glPopAttrib();	
	
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
    
#if defined(UNIX)
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
    if (id <= 0) {
	fprintf(stderr, "JAVA 3D ERROR : glNewList(%d) -- IGNORED\n", id);
	return;
    }

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
    static int numInvalidLists = 0;

    if (id <= 0) {
	if (numInvalidLists < 3) {
	    fprintf(stderr, "JAVA 3D ERROR : glCallList(%d) -- IGNORED\n", id);
	    ++numInvalidLists;
	}
	else if (numInvalidLists == 3) {
	    fprintf(stderr, "JAVA 3D : further glCallList error messages discarded\n");
	    ++numInvalidLists;
	}
	return;
    }

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
    
    if (id <= 0) {
	fprintf(stderr, "JAVA 3D ERROR : glDeleteLists(%d,1) -- IGNORED\n", id);
	return;
    }

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
#if defined(UNIX)
	ctxProperties->glColorTable =
	    (MYPFNGLCOLORTABLEPROC)dlsym(RTLD_DEFAULT, "glColorTable");
	ctxProperties->glGetColorTableParameteriv =
	    (MYPFNGLGETCOLORTABLEPARAMETERIVPROC)dlsym(RTLD_DEFAULT, "glGetColorTableParameteriv");
#endif

    } else if(isExtensionSupported(extensionStr, "GL_SGI_color_table")) {
#ifdef WIN32	
	ctxProperties->glColorTable = (MYPFNGLCOLORTABLEPROC)wglGetProcAddress("glColorTableSGI");
        ctxProperties->glGetColorTableParameteriv =
	    (MYPFNGLGETCOLORTABLEPARAMETERIVPROC)wglGetProcAddress("glGetColorTableParameterivSGI");
#endif
#if defined(UNIX)
	ctxProperties->glColorTable =
	    (MYPFNGLCOLORTABLEPROC)dlsym(RTLD_DEFAULT, "glColorTableSGI");
	ctxProperties->glGetColorTableParameteriv =
	    (MYPFNGLGETCOLORTABLEPARAMETERIVPROC)dlsym(RTLD_DEFAULT, "glGetColorTableParameterivSGI");
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
#if defined(UNIX)

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;

    /* Not need to do ext. supported checking. This check is done in java. */

    /* fprintf(stderr, "Canvas3D.c -- glXVideoResize -- %d %f\n", win, dvrFactor); */
    ctxProperties->glXVideoResizeSUN((Display *)display, (Window)win, (float) dvrFactor);
#endif

}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_videoResizeCompensation(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jboolean enable)
{
    
#if defined(UNIX)
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
    jlong fbConfigListPtr,
    jint width,
    jint height)
{
    
#if defined(UNIX)

    /* Fix for issue 20 */
    
   const char *extStr;
   int attrCount, configAttr[10];
   GLXPbuffer pbuff = None;
   GLXFBConfig *fbConfigList = (GLXFBConfig *)fbConfigListPtr;
   int val;

   /*
     glXGetFBConfigAttrib((Display *) display, fbConfigList[0], 
     GLX_FBCONFIG_ID, &val);
     fprintf(stderr, "GLX_FBCONFIG_ID returns %d\n", val);
     
     fprintf(stderr, "display 0x%x, fbConfigList[0] 0x%x, width %d, height %d\n",
     (int) display, (int) fbConfigList[0], width, height);
       
   */

   
   /* Query DRAWABLE_TYPE. Will use Pbuffer if fbConfig support it,
      else will try for Pixmap. If neither one exists, flag error message
      and return None */
   
   glXGetFBConfigAttrib((Display *) display, fbConfigList[0], 
			GLX_DRAWABLE_TYPE, &val);
   /* fprintf(stderr, "GLX_DRAWABLE_TYPE returns %d\n", val); */

   if (getJavaBoolEnv(env,"usePbuffer") && (val & GLX_PBUFFER_BIT) != 0) {
       /* fprintf(stderr, "Using pbuffer %d\n", val); */

       /* Initialize the attribute list to be used for choosing FBConfig */
       
       attrCount = 0;
       configAttr[attrCount++] = GLX_PBUFFER_WIDTH;
       configAttr[attrCount++] = width;
       configAttr[attrCount++] = GLX_PBUFFER_HEIGHT;
       configAttr[attrCount++] = height;
       configAttr[attrCount++] = GLX_PRESERVED_CONTENTS;
       configAttr[attrCount++] = GL_TRUE;
       configAttr[attrCount++] = None;
       

       pbuff = glXCreatePbuffer((Display *) display, fbConfigList[0], configAttr);
       
       if (pbuff == None) {
	   fprintf(stderr, "Java 3D ERROR : glXCreateGLXPbuffer() returns None\n");	   
       }

       return pbuff;
   }
   else if((val & GLX_PIXMAP_BIT) != 0) {
       Pixmap pixmap;
       GLXPixmap glxpixmap = None;
       XVisualInfo *vinfo;
       Window root;
       Window glWin; 
       XSetWindowAttributes win_attrs;
       Colormap		cmap;
       unsigned long	win_mask;

       /* fprintf(stderr, "Using pixmap %d\n", val); */

       vinfo = glXGetVisualFromFBConfig((Display*)display, fbConfigList[0]);
       if (vinfo == NULL) {
	   fprintf(stderr, "Java 3D ERROR : glXGetVisualFromFBConfig failed\n");
       }
       else {
	   /* fprintf(stderr, "found a %d-bit visual (visual ID = 0x%x)\n",
	      vinfo->depth, vinfo->visualid); */

	   /* fall back to pixmap */
	    root = RootWindow((Display *)display, vinfo->screen);
    
	    /* Create a colormap */
	    cmap = XCreateColormap((Display *)display, root, vinfo->visual, AllocNone);

	    /* Create a window */
	    win_attrs.colormap = cmap;
	    win_attrs.border_pixel = 0;
	    win_mask = CWColormap | CWBorderPixel;
	    glWin = XCreateWindow((Display *)display, root, 0, 0, 1, 1, 0,
				  vinfo->depth, InputOutput, vinfo->visual,
				  win_mask, &win_attrs);
	   
	   /* fprintf(stderr, "glWin %d\n",(int) glWin); */
	   
	   pixmap = XCreatePixmap((Display*)display, (GLXDrawable)glWin,
				  width, height, vinfo->depth);

	   /* fprintf(stderr, "XCreatePixmap returns %d\n", (int) pixmap); */
	   
	   glxpixmap = glXCreatePixmap((Display*)display, fbConfigList[0], pixmap, NULL); 
	   if (glxpixmap == None) {
	       fprintf(stderr, "Java 3D ERROR : glXCreateGLXPixmap() returns None\n");
	   }    
       }

       /* fprintf(stderr, "glxpixmap %d\n",(int) glxpixmap); */
       return glxpixmap;
   }
   else {
       fprintf(stderr, "Java 3D ERROR : FBConfig doesn't support pbuffer or pixmap returns None\n");
       return None;
   }

   
#endif /* UNIX */

#ifdef WIN32   
    /* Fix for issue 76 */
   int dpy = (int)display;   
    static char szAppName[] = "CreateOffScreen";
    HWND hwnd;
    HGLRC hrc;
    HDC   hdc;
    int pixelFormat;
    PixelFormatInfo *pFormatInfoPtr = (PixelFormatInfo *)fbConfigListPtr;
    int piAttrs[2];
    
    HPBUFFERARB hpbuf = NULL;  /* Handle to the Pbuffer */
    HDC hpbufdc = NULL;        /* Handle to the Pbuffer's device context */

    HDC bitmapHdc;
    HBITMAP hbitmap;
    
    BITMAPINFOHEADER bih;
    void *ppvBits;
    int err;
    LPTSTR errString;
    OffScreenBufferInfo *offScreenBufferInfo = NULL; 
    
    PIXELFORMATDESCRIPTOR dummy_pfd = getDummyPFD();
    jclass cv_class;
    jfieldID offScreenBuffer_field;
    JNIEnv table = *env;

    /*
    fprintf(stderr, "****** CreateOffScreenBuffer ******\n");
    fprintf(stderr, "display 0x%x,  pFormat %d, width %d, height %d\n",
	    (int) display,  pFormatInfoPtr->offScreenPFormat, width, height);
    */

    cv_class =  (jclass) (*(table->GetObjectClass))(env, obj);
    offScreenBuffer_field =
	(jfieldID) (*(table->GetFieldID))(env, cv_class, "offScreenBufferInfo", "J");
    
    /*
     * Select any pixel format and bound current context to
     * it so that we can get the wglChoosePixelFormatARB entry point.
     * Otherwise wglxxx entry point will always return null.
     * That's why we need to create a dummy window also.
     */
    hwnd = createDummyWindow((const char *)szAppName);
    
    if (!hwnd) {
	return 0;
    }
    hdc = GetDC(hwnd);

    pixelFormat = ChoosePixelFormat(hdc, &dummy_pfd);

    if (pixelFormat<1) {
	printErrorMessage("In Canvas3D : Failed in ChoosePixelFormat");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return 0;
    }

    SetPixelFormat(hdc, pixelFormat, NULL);
    
    hrc = wglCreateContext(hdc);
    if (!hrc) {
	printErrorMessage("In Canvas3D : Failed in wglCreateContext");
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return 0;
    }
    
    if (!wglMakeCurrent(hdc, hrc)) {
	printErrorMessage("In Canvas3D : Failed in wglMakeCurrent");
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	return 0;
    }
    
    if (pFormatInfoPtr->drawToPbuffer) {
	
	/* fprintf(stderr, "***** Use PBuffer for offscreen  ******\n"); */
	
	piAttrs[0] = 0;
	piAttrs[1] = 0;
		
	hpbuf = pFormatInfoPtr->wglCreatePbufferARB( hdc, pFormatInfoPtr->offScreenPFormat,
						     width, height, piAttrs);
	    
	if(hpbuf == NULL) {
	    printErrorMessage("In Canvas3D : wglCreatePbufferARB FAIL.");
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);
	    return 0;
	}
	
	hpbufdc = pFormatInfoPtr->wglGetPbufferDCARB(hpbuf);
	
	if(hpbufdc == NULL) {
	    printErrorMessage("In Canvas3D : Can't get pbuffer's device context.");
	    ReleaseDC(hwnd, hdc);
	    wglDeleteContext(hrc);
	    DestroyWindow(hwnd);
	    UnregisterClass(szAppName, (HINSTANCE)NULL);
	    return 0;
	}		
	
	/*
	fprintf(stderr,
		"Successfully created PBuffer = 0x%x, hdc = 0x%x\n",
		(int)hpbuf, (int)hpbufdc);
	*/

	/* Destroy all dummy objects */
	ReleaseDC(hwnd, hdc);
	wglDeleteContext(hrc);
	DestroyWindow(hwnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
	
	offScreenBufferInfo  =
	    (OffScreenBufferInfo *) malloc(sizeof(OffScreenBufferInfo));
	offScreenBufferInfo->isPbuffer = GL_TRUE;
	offScreenBufferInfo->hpbuf = hpbuf;

	(*(table->SetLongField))(env, obj, offScreenBuffer_field, (jlong)offScreenBufferInfo);

	return (jint) hpbufdc;
    }

    /* fprintf(stderr, "***** Use Bitmap for offscreen  ******\n"); */

    /* create a DIB */
    memset(&bih, 0, sizeof(BITMAPINFOHEADER));
    
    bih.biSize = sizeof(BITMAPINFOHEADER);
    bih.biWidth = width;
    bih.biHeight = height;
    bih.biPlanes = 1;
    
    
    /* by MIK OF CLASSX */
    if (getJavaBoolEnv(env, "transparentOffScreen")) {
    	bih.biBitCount = 32;
    }
    else {
    	bih.biBitCount = 24;
    }

    bih.biCompression = BI_RGB;    
    
    bitmapHdc = CreateCompatibleDC(hdc);
    
    hbitmap = CreateDIBSection(bitmapHdc, (BITMAPINFO *)&bih,
			       DIB_PAL_COLORS, &ppvBits, NULL, 0);
    
    
    if (!hbitmap) {
	err = GetLastError();
	FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		      FORMAT_MESSAGE_FROM_SYSTEM,
		      NULL, err, 0, (LPTSTR)&errString, 0, NULL);
	fprintf(stderr, "CreateDIBSection failed: %s\n", errString);
    }
    
    SelectObject(bitmapHdc, hbitmap);
    
    /* Choosing and setting of pixel format is done in createContext */    
    
    /* Destroy all dummy objects and fall BitMap  */    
    ReleaseDC(hwnd, hdc);
    wglDeleteContext(hrc);
    DestroyWindow(hwnd);
    UnregisterClass(szAppName, (HINSTANCE)NULL);

    offScreenBufferInfo  =
	(OffScreenBufferInfo *) malloc(sizeof(OffScreenBufferInfo));
    offScreenBufferInfo->isPbuffer = GL_FALSE;
    offScreenBufferInfo->hpbuf = 0;
    
    (*(table->SetLongField))(env, obj, offScreenBuffer_field, (jlong)offScreenBufferInfo);

    return ((jint)bitmapHdc);
    
#endif /* WIN32 */
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_destroyOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,    
    jlong display,
    jlong fbConfigListPtr,
    jint window)
{
    jclass cv_class;
    jfieldID offScreenBuffer_field;
    JNIEnv table = *env;

#if defined(UNIX)
    /*  Fix for Issue 20 */
    GLXFBConfig *fbConfigList = (GLXFBConfig *)fbConfigListPtr;
    int val;
    
    glXGetFBConfigAttrib((Display *) display, (GLXFBConfig) fbConfigList[0], 
			 GLX_DRAWABLE_TYPE, &val);
    /* fprintf(stderr, "GLX_DRAWABLE_TYPE returns %d\n", val); */
    
    if((val & GLX_PBUFFER_BIT) != 0) {
	glXDestroyPbuffer((Display *) display, (GLXPbuffer)window);
    }
    else if((val & GLX_PIXMAP_BIT) != 0) {
	glXDestroyPixmap((Display *) display, (GLXPixmap)window);
    }
    
#endif /* UNIX */

#ifdef WIN32
    /* Fix for issue 76 */
    PixelFormatInfo *pFormatInfoPtr = (PixelFormatInfo *)fbConfigListPtr;
    OffScreenBufferInfo *offScreenBufferInfo = NULL;
    HDC hpbufdc = (HDC) window;
    
    cv_class =  (jclass) (*(table->GetObjectClass))(env, obj);
    offScreenBuffer_field =
	(jfieldID) (*(table->GetFieldID))(env, cv_class, "offScreenBufferInfo", "J");

    offScreenBufferInfo =
	(OffScreenBufferInfo *) (*(table->GetLongField))(env, obj, offScreenBuffer_field);

    /*
    fprintf(stderr,"Canvas3D_destroyOffScreenBuffer : offScreenBufferInfo 0x%x\n",
	    offScreenBufferInfo);
    */

    if(offScreenBufferInfo == NULL) {
	return;
    }

    if(offScreenBufferInfo->isPbuffer) {
	/*
	fprintf(stderr,"Canvas3D_destroyOffScreenBuffer : Pbuffer\n");
	*/

	pFormatInfoPtr->wglReleasePbufferDCARB(offScreenBufferInfo->hpbuf, hpbufdc);
	pFormatInfoPtr->wglDestroyPbufferARB(offScreenBufferInfo->hpbuf);
    }
    else {
	HBITMAP oldhbitmap;
	HDC hdc = (HDC) window;
	
	/* fprintf(stderr,"Canvas3D_destroyOffScreenBuffer : BitMap\n"); */
	oldhbitmap = SelectObject(hdc, NULL);
	DeleteObject(oldhbitmap);
	DeleteDC(hdc);
    }
    
    free(offScreenBufferInfo);
    (*(table->SetLongField))(env, obj, offScreenBuffer_field, (jlong)0);

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
    default:
	throwAssert(env, "illegal format");
	break;
    }
  

    glReadPixels(0, 0, width, height, type, GL_UNSIGNED_BYTE, byteData);

    (*(table->ReleasePrimitiveArrayCritical))(env, byteData_array,
                byteData, 0);
}

static void
initializeCtxInfo(JNIEnv *env , GraphicsContextPropertiesInfo* ctxInfo)
{
    ctxInfo->context = 0; 
    
    /* version and extension info */
    ctxInfo->versionStr = NULL;
    ctxInfo->vendorStr = NULL;
    ctxInfo->rendererStr = NULL;
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
    ctxInfo->blendFunctionTable[BLEND_ZERO] = GL_ZERO;
    ctxInfo->blendFunctionTable[BLEND_ONE] = GL_ONE;
    ctxInfo->blendFunctionTable[BLEND_SRC_ALPHA] = GL_SRC_ALPHA;
    ctxInfo->blendFunctionTable[BLEND_ONE_MINUS_SRC_ALPHA] = GL_ONE_MINUS_SRC_ALPHA;
    ctxInfo->blendFunctionTable[BLEND_DST_COLOR] = GL_DST_COLOR;
    ctxInfo->blendFunctionTable[BLEND_ONE_MINUS_DST_COLOR] = GL_ONE_MINUS_DST_COLOR;
    ctxInfo->blendFunctionTable[BLEND_SRC_COLOR] = GL_SRC_COLOR;
    ctxInfo->blendFunctionTable[BLEND_ONE_MINUS_SRC_COLOR] = GL_ONE_MINUS_SRC_COLOR;
    ctxInfo->blendFunctionTable[BLEND_CONSTANT_COLOR] = GL_CONSTANT_COLOR;

    /* 1.1 extensions or 1.2 extensions */
    /* sun extensions */
    ctxInfo->multi_draw_arrays_sun = JNI_FALSE;
    ctxInfo->compiled_vertex_array_ext = JNI_FALSE;

    ctxInfo->videoResizeAvailable = JNI_FALSE;
    ctxInfo->global_alpha_sun = JNI_FALSE;
    ctxInfo->constant_data_sun = JNI_FALSE;
    
    /* EXT extensions */
    ctxInfo->abgr_ext = JNI_FALSE;

    ctxInfo->multi_draw_arrays_ext = JNI_FALSE;

    ctxInfo->implicit_multisample = getJavaBoolEnv(env, "implicitAntialiasing");
    
    /* by MIK OF CLASSX */
    ctxInfo->alphaClearValue = (getJavaBoolEnv(env, "transparentOffScreen") ? 0.0f : 1.0f);

    /* ARB extensions */
    ctxInfo->arb_transpose_matrix = JNI_FALSE;
    ctxInfo->arb_multitexture = JNI_FALSE;

    ctxInfo->arb_multisample = JNI_FALSE;
    ctxInfo->maxTexCoordSets = 1;
    ctxInfo->maxTextureUnits = 1;
    ctxInfo->maxTextureImageUnits = 0;
    ctxInfo->maxVertexTextureImageUnits = 0;
    ctxInfo->maxCombinedTextureImageUnits = 0;

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
    
    /* extension mask */
    ctxInfo->extMask = 0;
    ctxInfo->textureExtMask = 0;

    ctxInfo->shadingLanguageGLSL = JNI_FALSE;
    ctxInfo->shadingLanguageCg = JNI_FALSE;
    
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

    /* Initialize shader program Id */
    ctxInfo->shaderProgramId = 0;

    /* Initialize maximum number of vertex attrs */
    ctxInfo->maxVertexAttrs = 0;

    /* Initialize shader vertex attribute function pointers */
    ctxInfo->vertexAttrPointer = dummyVertexAttrPointer;
    ctxInfo->enableVertexAttrArray = dummyEnDisableVertexAttrArray;
    ctxInfo->disableVertexAttrArray = dummyEnDisableVertexAttrArray;
    ctxInfo->vertexAttr1fv = dummyVertexAttr;
    ctxInfo->vertexAttr2fv = dummyVertexAttr;
    ctxInfo->vertexAttr3fv = dummyVertexAttr;
    ctxInfo->vertexAttr4fv = dummyVertexAttr;

    /* Initialize shader info pointers */
    ctxInfo->glslCtxInfo = NULL;
    ctxInfo->cgCtxInfo = NULL;

#if defined(UNIX)
    ctxInfo->glXVideoResizeSUN = NULL;
#endif /* UNIX */
}

static void
cleanupCtxInfo(GraphicsContextPropertiesInfo* ctxInfo)
{
    if( ctxInfo->versionStr != NULL)
	free(ctxInfo->versionStr);
    if( ctxInfo->vendorStr != NULL)
	free(ctxInfo->vendorStr);
    if( ctxInfo->rendererStr != NULL)
	free(ctxInfo->rendererStr);
    if( ctxInfo->extensionStr != NULL)
	free(ctxInfo->extensionStr);
    ctxInfo->versionStr = NULL;
    ctxInfo->vendorStr = NULL;
    ctxInfo->rendererStr = NULL;
    ctxInfo->extensionStr = NULL;
}

#ifdef WIN32
HWND createDummyWindow(const char* szAppName) {
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
#endif

JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_createQueryContext(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint window,
    jint vid,
    jlong fbConfigListPtr,
    jboolean offScreen,
    jint width,
    jint height,
    jboolean glslLibraryAvailable,
    jboolean cgLibraryAvailable)
{
    JNIEnv table = *env;
    jlong gctx;
    int stencilSize=0;
    jint newWin;
    int PixelFormatID=0;
    GraphicsContextPropertiesInfo* ctxInfo = (GraphicsContextPropertiesInfo *)malloc(sizeof(GraphicsContextPropertiesInfo)); 
	
#if defined(UNIX)

    /* Fix for issue 20 */

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

    GLXFBConfig *fbConfigList = NULL;
    
    fbConfigList = (GLXFBConfig *)fbConfigListPtr;    

    /*
      fprintf(stderr, "Canvas3D_createQueryContext:\n");
      fprintf(stderr, "fbConfigListPtr 0x%x\n", (int) fbConfigListPtr);
      fprintf(stderr, "fbConfigList 0x%x, fbConfigList[0] 0x%x\n",
      (int) fbConfigList, (int) fbConfigList[0]);
    */

    ctx = glXCreateNewContext((Display *)display, fbConfigList[0],
			      GLX_RGBA_TYPE, NULL, True);
    
    if (ctx == NULL) {
	fprintf(stderr, "Java 3D ERROR : Canvas3D_createQueryContext: couldn't create context.\n");
    }
   
   
    /* onscreen rendering and window is 0 now */
    if(window == 0 && !offScreen) {

	vinfo = glXGetVisualFromFBConfig((Display*)display, fbConfigList[0]);
	if (vinfo == NULL) {
	    fprintf(stderr, "Java 3D ERROR : glXGetVisualFromFBConfig failed\n");
	}
	else {
	    /* fprintf(stderr, "found a %d-bit visual (visual ID = 0x%x)\n",
	       vinfo->depth, vinfo->visualid);
	    */
	    root = RootWindow((Display *)display, vinfo->screen);
    
	    /* Create a colormap */
	    cmap = XCreateColormap((Display *)display, root, vinfo->visual, AllocNone);

	    /* Create a window */
	    win_attrs.colormap = cmap;
	    win_attrs.border_pixel = 0;
	    win_attrs.event_mask = KeyPressMask | ExposureMask | StructureNotifyMask;
	    win_mask = CWColormap | CWBorderPixel | CWEventMask;
	    glWin = XCreateWindow((Display *)display, root, 0, 0, width, height, 0,
				  vinfo->depth, InputOutput, vinfo->visual,
				  win_mask, &win_attrs);
	    newWin = (jint)glWin; 
	}
    }
    else if(window == 0 && offScreen){
	newWin = Java_javax_media_j3d_Canvas3D_createOffScreenBuffer( env, obj, 0,
								      display, window,
								      fbConfigListPtr,
								      width, height);
    }
    else if(window != 0) {
	newWin = window;
    }
    
    result = glXMakeCurrent((Display *)display, (GLXDrawable)newWin, (GLXContext)ctx);
    if (result == GL_FALSE)
	fprintf(stderr, "Java 3D ERROR : glXMakeCurrent fails\n");

    glXGetFBConfigAttrib((Display *) display, fbConfigList[0], 
			 GLX_STENCIL_SIZE, &stencilSize);


    gctx = (jlong)ctx;
#endif

#ifdef WIN32
    HGLRC hrc;        /* HW Rendering Context */
    HDC hdc;          /* HW Device Context */
    DWORD err;
    LPTSTR errString;
    HWND hDummyWnd = 0;
    static char szAppName[] = "OpenGL";
    jlong vinfo = 0;
    jboolean result;
    PixelFormatInfo *PixelFormatInfoPtr = (PixelFormatInfo *)fbConfigListPtr;

    /* Fix for issue 76 */
    
    /*
      fprintf(stderr, "Canvas3D_createQueryContext:\n");
      fprintf(stderr, "window is  0x%x, offScreen %d\n", window, offScreen);
    */
    
    /*
     * vid must be valid PixelFormat returned
     * by wglChoosePixelFormat() or wglChoosePixelFormatARB.
     */    

    /* Fix to issue 104 */
    if(!offScreen) {
	if ((PixelFormatInfoPtr == NULL) || (PixelFormatInfoPtr->onScreenPFormat <= 0)) {
	    printErrorMessage("Canvas3D_createNewContext: onScreen PixelFormat is invalid");
	    return;
	}
	else {
	    PixelFormatID = PixelFormatInfoPtr->onScreenPFormat;
	}
    }
    else {
	if ((PixelFormatInfoPtr == NULL) || (PixelFormatInfoPtr->offScreenPFormat <= 0)) {
	    printErrorMessage("Canvas3D_createNewContext: offScreen PixelFormat is invalid");
	    return;
	}
	else {
	    PixelFormatID = PixelFormatInfoPtr->offScreenPFormat;
	}
    }
    
    /* onscreen rendering and window is 0 now */
    if(window == 0 && !offScreen){
	/* fprintf(stderr, "CreateQueryContext : window == 0 && !offScreen\n"); */
	hDummyWnd = createDummyWindow(szAppName);
	if (!hDummyWnd) {
	    return;
	}
	hdc =  GetDC(hDummyWnd);
    }
    else if(window == 0 && offScreen){
	/* fprintf(stderr, "CreateQueryContext : window == 0 && offScreen\n"); */
	hdc = (HDC)Java_javax_media_j3d_Canvas3D_createOffScreenBuffer( env, obj, 0, display,
									vid, fbConfigListPtr,
									width, height);
    }
    else if(window != 0){
	/* fprintf(stderr, "CreateQueryContext : window != 0 0x%x\n", window); */
	hdc =  (HDC) window;
    }

    newWin = (jint)hdc;
   
    SetPixelFormat(hdc, PixelFormatID, NULL);

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
    if (getPropertiesFromCurrentContext(env, obj, ctxInfo, (jlong) hdc, PixelFormatID,
					fbConfigListPtr, offScreen,
					glslLibraryAvailable, cgLibraryAvailable)) {
	/* put the properties to the Java side */
	setupCanvasProperties(env, obj, ctxInfo);
    }


    /* clear up the context , colormap and window if appropriate */
    if(window == 0 && !offScreen){
#if defined(UNIX)
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display, newWin, (jlong)ctxInfo); 
	XDestroyWindow((Display *)display, glWin);
	XFreeColormap((Display *)display, cmap);
#endif /* UNIX */
#ifdef WIN32
	/* Release DC */
	ReleaseDC(hDummyWnd, hdc);
	/* Destroy context */
	/* This will free ctxInfo also */
	Java_javax_media_j3d_Canvas3D_destroyContext(env, obj, display,newWin, (jlong)ctxInfo);
	DestroyWindow(hDummyWnd);
	UnregisterClass(szAppName, (HINSTANCE)NULL);
#endif /* WIN32 */
    }
    else if(window == 0 && offScreen) {
	Java_javax_media_j3d_Canvas3D_destroyOffScreenBuffer(env, obj, gctx, display, fbConfigListPtr,  newWin);
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
	    glEnable(GL_MULTISAMPLE_ARB);
	}
	else {
	    glDisable(GL_MULTISAMPLE_ARB);

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

#if defined(UNIX)
    return JNI_TRUE;
#endif
}


/*
 * Function to disable most rendering attributes when doing a 2D
 * clear, image copy, or image composite operation. Note that the
 * caller must save/restore the attributes with
 * pushAttrib(GL_ENABLE_BIT|...) and popAttrib()
 */
static void
disableAttribFor2D(GraphicsContextPropertiesInfo *ctxProperties)
{
    int i;

    glDisable(GL_ALPHA_TEST);
    glDisable(GL_BLEND);
    glDisable(GL_COLOR_LOGIC_OP);
    glDisable(GL_COLOR_MATERIAL);
    glDisable(GL_CULL_FACE);
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_FOG);
    glDisable(GL_LIGHTING);
    glDisable(GL_POLYGON_OFFSET_FILL);
    glDisable(GL_POLYGON_STIPPLE);
    glDisable(GL_STENCIL_TEST);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_TEXTURE_GEN_Q);
    glDisable(GL_TEXTURE_GEN_R);
    glDisable(GL_TEXTURE_GEN_S);
    glDisable(GL_TEXTURE_GEN_T);

    for (i = 0; i < 6; i++) {
	glDisable(GL_CLIP_PLANE0 + i);
    }

    if (ctxProperties->texture3DAvailable) {
	glDisable(ctxProperties->texture_3D_ext_enum);
    }

    if (ctxProperties->textureCubeMapAvailable) {
	glDisable(ctxProperties->texture_cube_map_ext_enum);
    }

    if (ctxProperties->textureRegisterCombinersAvailable) {
        glDisable(GL_REGISTER_COMBINERS_NV);
    }

    if (ctxProperties->textureColorTableAvailable) {
	glDisable(GL_TEXTURE_COLOR_TABLE_SGI);
    }

    if (ctxProperties->global_alpha_sun) {
	glDisable(GL_GLOBAL_ALPHA_SUN);
    }
}
