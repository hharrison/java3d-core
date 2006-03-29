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

#if defined(LINUX)
#define _GNU_SOURCE 1
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jni.h>

#include "gldefs.h"
#include "GLSLInfo.h"

#if defined(UNIX)
#include <dlfcn.h>
#endif

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


extern char *strJavaToC(JNIEnv *env, jstring str);
extern void throwAssert(JNIEnv *env, char *str);
extern jobject createShaderError(JNIEnv *env,
				 int errorCode,
				 const char *errorMsg,
				 const char *detailMsg);

extern int isExtensionSupported(const char *allExtensions, const char *extension);


static void glslVertexAttrPointer(GraphicsContextPropertiesInfo *ctxProperties,
				  int index, int size, int type, int stride,
				  const void *pointer);
static void glslEnableVertexAttrArray(GraphicsContextPropertiesInfo *ctxProperties,
				      int index);
static void glslDisableVertexAttrArray(GraphicsContextPropertiesInfo *ctxProperties,
				       int index);
static void glslVertexAttr1fv(GraphicsContextPropertiesInfo *ctxProperties,
			      int index, const float *v);
static void glslVertexAttr2fv(GraphicsContextPropertiesInfo *ctxProperties,
			      int index, const float *v);
static void glslVertexAttr3fv(GraphicsContextPropertiesInfo *ctxProperties,
			      int index, const float *v);
static void glslVertexAttr4fv(GraphicsContextPropertiesInfo *ctxProperties,
			      int index, const float *v);


/*
 * Called by getPropertiesFromCurrentContext to initialize the GLSL
 * shader function pointers and set the flag indicating whether GLSL
 * shaders are available.
 */
void
checkGLSLShaderExtensions(
    JNIEnv *env,
    jobject obj,
    char *tmpExtensionStr,
    GraphicsContextPropertiesInfo *ctxInfo,
    jboolean glslLibraryAvailable)
{
    ctxInfo->shadingLanguageGLSL = JNI_FALSE;	
    ctxInfo->glslCtxInfo = NULL;

    if (glslLibraryAvailable &&
	isExtensionSupported(tmpExtensionStr, "GL_ARB_shader_objects") &&
	isExtensionSupported(tmpExtensionStr, "GL_ARB_shading_language_100")) {

	GLSLCtxInfo *glslCtxInfo = (GLSLCtxInfo *)malloc(sizeof(GLSLCtxInfo));
	memset(glslCtxInfo, 0, sizeof(GLSLCtxInfo));

#if defined(UNIX)
	glslCtxInfo->pfnglAttachObjectARB =
	    (PFNGLATTACHOBJECTARBPROC)dlsym(RTLD_DEFAULT, "glAttachObjectARB");
	glslCtxInfo->pfnglCompileShaderARB =
	    (PFNGLCOMPILESHADERARBPROC)dlsym(RTLD_DEFAULT, "glCompileShaderARB");
	glslCtxInfo->pfnglCreateProgramObjectARB =
	    (PFNGLCREATEPROGRAMOBJECTARBPROC)dlsym(RTLD_DEFAULT, "glCreateProgramObjectARB");
	glslCtxInfo->pfnglCreateShaderObjectARB =
	    (PFNGLCREATESHADEROBJECTARBPROC)dlsym(RTLD_DEFAULT, "glCreateShaderObjectARB");
	glslCtxInfo->pfnglglDeleteObjectARB =
	    (PFNGLDELETEOBJECTARBPROC)dlsym(RTLD_DEFAULT, "glDeleteObjectARB");
	glslCtxInfo->pfnglGetInfoLogARB =
	    (PFNGLGETINFOLOGARBPROC)dlsym(RTLD_DEFAULT, "glGetInfoLogARB");
	glslCtxInfo->pfnglGetObjectParameterivARB =
	    (PFNGLGETOBJECTPARAMETERIVARBPROC)dlsym(RTLD_DEFAULT, "glGetObjectParameterivARB");
	glslCtxInfo->pfnglLinkProgramARB =
	    (PFNGLLINKPROGRAMARBPROC)dlsym(RTLD_DEFAULT, "glLinkProgramARB");
	glslCtxInfo->pfnglShaderSourceARB =
	    (PFNGLSHADERSOURCEARBPROC)dlsym(RTLD_DEFAULT, "glShaderSourceARB");
	glslCtxInfo->pfnglUseProgramObjectARB =
	    (PFNGLUSEPROGRAMOBJECTARBPROC)dlsym(RTLD_DEFAULT, "glUseProgramObjectARB");
	glslCtxInfo->pfnglGetUniformLocationARB =
	    (PFNGLGETUNIFORMLOCATIONARBPROC)dlsym(RTLD_DEFAULT, "glGetUniformLocationARB");
	glslCtxInfo->pfnglGetAttribLocationARB =
	    (PFNGLGETATTRIBLOCATIONARBPROC)dlsym(RTLD_DEFAULT, "glGetAttribLocationARB");
	glslCtxInfo->pfnglBindAttribLocationARB =
	    (PFNGLBINDATTRIBLOCATIONARBPROC)dlsym(RTLD_DEFAULT, "glBindAttribLocationARB");
	glslCtxInfo->pfnglVertexAttrib1fvARB =
	    (PFNGLVERTEXATTRIB1FVARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttrib1fvARB");
	glslCtxInfo->pfnglVertexAttrib2fvARB =
	    (PFNGLVERTEXATTRIB2FVARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttrib2fvARB");
	glslCtxInfo->pfnglVertexAttrib3fvARB =
	    (PFNGLVERTEXATTRIB3FVARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttrib3fvARB");
	glslCtxInfo->pfnglVertexAttrib4fvARB =
	    (PFNGLVERTEXATTRIB4FVARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttrib4fvARB");
	glslCtxInfo->pfnglVertexAttribPointerARB =
	    (PFNGLVERTEXATTRIBPOINTERARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttribPointerARB");
	glslCtxInfo->pfnglEnableVertexAttribArrayARB =
	    (PFNGLENABLEVERTEXATTRIBARRAYARBPROC)dlsym(RTLD_DEFAULT, "glEnableVertexAttribArrayARB");
	glslCtxInfo->pfnglDisableVertexAttribArrayARB =
	    (PFNGLDISABLEVERTEXATTRIBARRAYARBPROC)dlsym(RTLD_DEFAULT, "glDisableVertexAttribArrayARB");
	glslCtxInfo->pfnglVertexAttribPointerARB =
	    (PFNGLVERTEXATTRIBPOINTERARBPROC)dlsym(RTLD_DEFAULT, "glVertexAttribPointerARB");
	glslCtxInfo->pfnglGetActiveUniformARB =
	    (PFNGLGETACTIVEUNIFORMARBPROC)dlsym(RTLD_DEFAULT, "glGetActiveUniformARB");
	glslCtxInfo->pfnglUniform1iARB =
	    (PFNGLUNIFORM1IARBPROC)dlsym(RTLD_DEFAULT, "glUniform1iARB");
	glslCtxInfo->pfnglUniform1fARB =
	    (PFNGLUNIFORM1FARBPROC)dlsym(RTLD_DEFAULT, "glUniform1fARB");
	glslCtxInfo->pfnglUniform2iARB =
	    (PFNGLUNIFORM2IARBPROC)dlsym(RTLD_DEFAULT, "glUniform2iARB");
	glslCtxInfo->pfnglUniform2fARB =
	    (PFNGLUNIFORM2FARBPROC)dlsym(RTLD_DEFAULT, "glUniform2fARB");
	glslCtxInfo->pfnglUniform3iARB =
	    (PFNGLUNIFORM3IARBPROC)dlsym(RTLD_DEFAULT, "glUniform3iARB");
	glslCtxInfo->pfnglUniform3fARB =
	    (PFNGLUNIFORM3FARBPROC)dlsym(RTLD_DEFAULT, "glUniform3fARB");
	glslCtxInfo->pfnglUniform4iARB =
	    (PFNGLUNIFORM4IARBPROC)dlsym(RTLD_DEFAULT, "glUniform4iARB");
	glslCtxInfo->pfnglUniform4fARB =
	    (PFNGLUNIFORM4FARBPROC)dlsym(RTLD_DEFAULT, "glUniform4fARB");
	glslCtxInfo->pfnglUniform1ivARB =
	    (PFNGLUNIFORM1IVARBPROC)dlsym(RTLD_DEFAULT, "glUniform1ivARB");
	glslCtxInfo->pfnglUniform1fvARB =
	    (PFNGLUNIFORM1FVARBPROC)dlsym(RTLD_DEFAULT, "glUniform1fvARB");
	glslCtxInfo->pfnglUniform2ivARB =
	    (PFNGLUNIFORM2IVARBPROC)dlsym(RTLD_DEFAULT, "glUniform2ivARB");
	glslCtxInfo->pfnglUniform2fvARB =
	    (PFNGLUNIFORM2FVARBPROC)dlsym(RTLD_DEFAULT, "glUniform2fvARB");
	glslCtxInfo->pfnglUniform3ivARB =
	    (PFNGLUNIFORM3IVARBPROC)dlsym(RTLD_DEFAULT, "glUniform3ivARB");
	glslCtxInfo->pfnglUniform3fvARB =
	    (PFNGLUNIFORM3FVARBPROC)dlsym(RTLD_DEFAULT, "glUniform3fvARB");
	glslCtxInfo->pfnglUniform4ivARB =
	    (PFNGLUNIFORM4IVARBPROC)dlsym(RTLD_DEFAULT, "glUniform4ivARB");
	glslCtxInfo->pfnglUniform4fvARB =
	    (PFNGLUNIFORM4FVARBPROC)dlsym(RTLD_DEFAULT, "glUniform4fvARB");
	glslCtxInfo->pfnglUniformMatrix3fvARB =
	    (PFNGLUNIFORMMATRIX3FVARBPROC)dlsym(RTLD_DEFAULT, "glUniformMatrix3fvARB");
	glslCtxInfo->pfnglUniformMatrix4fvARB =
	    (PFNGLUNIFORMMATRIX4FVARBPROC)dlsym(RTLD_DEFAULT, "glUniformMatrix4fvARB");
#endif
#ifdef WIN32
	glslCtxInfo->pfnglAttachObjectARB =
	    (PFNGLATTACHOBJECTARBPROC)wglGetProcAddress("glAttachObjectARB");
	glslCtxInfo->pfnglCompileShaderARB =
	    (PFNGLCOMPILESHADERARBPROC)wglGetProcAddress("glCompileShaderARB");
	glslCtxInfo->pfnglCreateProgramObjectARB =
	    (PFNGLCREATEPROGRAMOBJECTARBPROC)wglGetProcAddress("glCreateProgramObjectARB");
	glslCtxInfo->pfnglCreateShaderObjectARB =
	    (PFNGLCREATESHADEROBJECTARBPROC)wglGetProcAddress("glCreateShaderObjectARB");
	glslCtxInfo->pfnglglDeleteObjectARB =
	    (PFNGLDELETEOBJECTARBPROC)wglGetProcAddress("glDeleteObjectARB");
	glslCtxInfo->pfnglGetInfoLogARB =
	    (PFNGLGETINFOLOGARBPROC)wglGetProcAddress("glGetInfoLogARB");
	glslCtxInfo->pfnglGetObjectParameterivARB =
	    (PFNGLGETOBJECTPARAMETERIVARBPROC)wglGetProcAddress("glGetObjectParameterivARB");
	glslCtxInfo->pfnglLinkProgramARB =
	    (PFNGLLINKPROGRAMARBPROC)wglGetProcAddress("glLinkProgramARB");
	glslCtxInfo->pfnglShaderSourceARB =
	    (PFNGLSHADERSOURCEARBPROC)wglGetProcAddress("glShaderSourceARB");
	glslCtxInfo->pfnglUseProgramObjectARB =
	    (PFNGLUSEPROGRAMOBJECTARBPROC)wglGetProcAddress("glUseProgramObjectARB");
	glslCtxInfo->pfnglGetUniformLocationARB =
	    (PFNGLGETUNIFORMLOCATIONARBPROC)wglGetProcAddress("glGetUniformLocationARB");
	glslCtxInfo->pfnglGetAttribLocationARB =
	    (PFNGLGETATTRIBLOCATIONARBPROC)wglGetProcAddress("glGetAttribLocationARB");
	glslCtxInfo->pfnglBindAttribLocationARB =
	    (PFNGLBINDATTRIBLOCATIONARBPROC)wglGetProcAddress("glBindAttribLocationARB");
	glslCtxInfo->pfnglVertexAttrib1fvARB =
	    (PFNGLVERTEXATTRIB1FVARBPROC)wglGetProcAddress("glVertexAttrib1fvARB");
	glslCtxInfo->pfnglVertexAttrib2fvARB =
	    (PFNGLVERTEXATTRIB2FVARBPROC)wglGetProcAddress("glVertexAttrib2fvARB");
	glslCtxInfo->pfnglVertexAttrib3fvARB =
	    (PFNGLVERTEXATTRIB3FVARBPROC)wglGetProcAddress("glVertexAttrib3fvARB");
	glslCtxInfo->pfnglVertexAttrib4fvARB =
	    (PFNGLVERTEXATTRIB4FVARBPROC)wglGetProcAddress("glVertexAttrib4fvARB");
	glslCtxInfo->pfnglVertexAttribPointerARB =
	    (PFNGLVERTEXATTRIBPOINTERARBPROC)wglGetProcAddress("glVertexAttribPointerARB");
	glslCtxInfo->pfnglEnableVertexAttribArrayARB =
	    (PFNGLENABLEVERTEXATTRIBARRAYARBPROC)wglGetProcAddress("glEnableVertexAttribArrayARB");
	glslCtxInfo->pfnglDisableVertexAttribArrayARB =
	    (PFNGLDISABLEVERTEXATTRIBARRAYARBPROC)wglGetProcAddress("glDisableVertexAttribArrayARB");
	glslCtxInfo->pfnglVertexAttribPointerARB =
	    (PFNGLVERTEXATTRIBPOINTERARBPROC)wglGetProcAddress("glVertexAttribPointerARB");
	glslCtxInfo->pfnglGetActiveUniformARB =
	    (PFNGLGETACTIVEUNIFORMARBPROC)wglGetProcAddress("glGetActiveUniformARB");
	glslCtxInfo->pfnglUniform1iARB =
	    (PFNGLUNIFORM1IARBPROC)wglGetProcAddress("glUniform1iARB");
	glslCtxInfo->pfnglUniform1fARB =
	    (PFNGLUNIFORM1FARBPROC)wglGetProcAddress("glUniform1fARB");
	glslCtxInfo->pfnglUniform2iARB =
	    (PFNGLUNIFORM2IARBPROC)wglGetProcAddress("glUniform2iARB");
	glslCtxInfo->pfnglUniform2fARB =
	    (PFNGLUNIFORM2FARBPROC)wglGetProcAddress("glUniform2fARB");
	glslCtxInfo->pfnglUniform3iARB =
	    (PFNGLUNIFORM3IARBPROC)wglGetProcAddress("glUniform3iARB");
	glslCtxInfo->pfnglUniform3fARB =
	    (PFNGLUNIFORM3FARBPROC)wglGetProcAddress("glUniform3fARB");
	glslCtxInfo->pfnglUniform4iARB =
	    (PFNGLUNIFORM4IARBPROC)wglGetProcAddress("glUniform4iARB");
	glslCtxInfo->pfnglUniform4fARB =
	    (PFNGLUNIFORM4FARBPROC)wglGetProcAddress("glUniform4fARB");
	glslCtxInfo->pfnglUniform1ivARB =
	    (PFNGLUNIFORM1IVARBPROC)wglGetProcAddress("glUniform1ivARB");
	glslCtxInfo->pfnglUniform1fvARB =
	    (PFNGLUNIFORM1FVARBPROC)wglGetProcAddress("glUniform1fvARB");
	glslCtxInfo->pfnglUniform2ivARB =
	    (PFNGLUNIFORM2IVARBPROC)wglGetProcAddress("glUniform2ivARB");
	glslCtxInfo->pfnglUniform2fvARB =
	    (PFNGLUNIFORM2FVARBPROC)wglGetProcAddress("glUniform2fvARB");
	glslCtxInfo->pfnglUniform3ivARB =
	    (PFNGLUNIFORM3IVARBPROC)wglGetProcAddress("glUniform3ivARB");
	glslCtxInfo->pfnglUniform3fvARB =
	    (PFNGLUNIFORM3FVARBPROC)wglGetProcAddress("glUniform3fvARB");
	glslCtxInfo->pfnglUniform4ivARB =
	    (PFNGLUNIFORM4IVARBPROC)wglGetProcAddress("glUniform4ivARB");
	glslCtxInfo->pfnglUniform4fvARB =
	    (PFNGLUNIFORM4FVARBPROC)wglGetProcAddress("glUniform4fvARB");
	glslCtxInfo->pfnglUniformMatrix3fvARB =
	    (PFNGLUNIFORMMATRIX3FVARBPROC)wglGetProcAddress("glUniformMatrix3fvARB");
	glslCtxInfo->pfnglUniformMatrix4fvARB =
	    (PFNGLUNIFORMMATRIX4FVARBPROC)wglGetProcAddress("glUniformMatrix4fvARB");
#endif

	/* Initialize shader vertex attribute function pointers */
	ctxInfo->vertexAttrPointer = glslVertexAttrPointer;
	ctxInfo->enableVertexAttrArray = glslEnableVertexAttrArray;
	ctxInfo->disableVertexAttrArray = glslDisableVertexAttrArray;
	ctxInfo->vertexAttr1fv = glslVertexAttr1fv;
	ctxInfo->vertexAttr2fv = glslVertexAttr2fv;
	ctxInfo->vertexAttr3fv = glslVertexAttr3fv;
	ctxInfo->vertexAttr4fv = glslVertexAttr4fv;

        ctxInfo->maxTextureImageUnits = 0;
        ctxInfo->maxVertexTextureImageUnits = 0;
        ctxInfo->maxCombinedTextureImageUnits = 0;

        /* Initialize GLSL texture sampler limits */
        glGetIntegerv(GL_MAX_TEXTURE_IMAGE_UNITS_ARB, &ctxInfo->maxTextureImageUnits);
        glGetIntegerv(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB, &ctxInfo->maxVertexTextureImageUnits);
        glGetIntegerv(GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB, &ctxInfo->maxCombinedTextureImageUnits);
	glGetIntegerv(GL_MAX_VERTEX_ATTRIBS_ARB, &ctxInfo->maxVertexAttrs);
        if (ctxInfo->maxVertexAttrs > 0) {
            /* decr count, since vertexAttr[0] is reserved for position */
            ctxInfo->maxVertexAttrs -= 1;
        }

	if (glslCtxInfo->pfnglCreateShaderObjectARB != NULL) {
	    /*fprintf(stderr, "Java 3D : GLSLShader extension is  available\n");*/
	    ctxInfo->shadingLanguageGLSL = JNI_TRUE;	
	    /* TODO: need to free ctxInfo->glslCtxInfo when ctxInfo is freed */
	    ctxInfo->glslCtxInfo = glslCtxInfo;
	}
	else {
	    free(glslCtxInfo);
	}
    }
}


/*
 * Return the info log as a string. This is used as the detail message
 * for a ShaderError.
 */
static const char *
getInfoLog(
    GraphicsContextPropertiesInfo* ctxProperties,
    GLhandleARB obj)
{
    int infoLogLength = 0;
    int len = 0;
    GLcharARB *infoLog = NULL;

    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    static const char *allocMsg =
	"Java 3D ERROR: could not allocate infoLog buffer\n";

    glslCtxInfo->pfnglGetObjectParameterivARB(obj,
					      GL_OBJECT_INFO_LOG_LENGTH_ARB,
					      &infoLogLength);
    if (infoLogLength > 0) {
	infoLog = (GLcharARB *)malloc(infoLogLength);
	if (infoLog == NULL) {
	    return allocMsg;
	}

	glslCtxInfo->pfnglGetInfoLogARB(obj, infoLogLength, &len, infoLog);
    }

    return infoLog;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    createGLSLShader
 * Signature: (JI[J)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_createGLSLShader(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jint  shaderType,
    jlongArray shaderIdArray)
{

    jlong *shaderIdPtr;
    GLhandleARB shaderHandle = 0;
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    jobject shaderError = NULL;

    shaderIdPtr = (*env)->GetLongArrayElements(env, shaderIdArray, NULL);

    /* Process  shader */
    /*
    fprintf(stderr, "    shaderType == %d\n", shaderType);
    */
    if (shaderType == javax_media_j3d_Shader_SHADER_TYPE_VERTEX) { 
	/* create the vertex shader */
	shaderHandle = glslCtxInfo->pfnglCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
    }
    else if (shaderType == javax_media_j3d_Shader_SHADER_TYPE_FRAGMENT) { 
	    /* create the fragment shader */
	shaderHandle = glslCtxInfo->pfnglCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);
    }
    
    if (shaderHandle == 0) {
	shaderError = createShaderError(env,
					javax_media_j3d_ShaderError_COMPILE_ERROR,
					"Unable to create native shader object",
					NULL);
    }

    shaderIdPtr[0] = (jlong) shaderHandle;
    (*env)->ReleaseLongArrayElements(env, shaderIdArray, shaderIdPtr, 0); 

    return shaderError;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    destroyGLSLShader
 * Signature: (JJ)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_destroyGLSLShader(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderId)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglglDeleteObjectARB( (GLhandleARB) shaderId);
    
    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    compileGLSLShader
 * Signature: (JJLjava/lang/String;)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_compileGLSLShader(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderId,
    jstring program)
{    
    GLint status;
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    jobject shaderError = NULL;

    /* Null-terminated "C" strings */
    GLcharARB *shaderString = NULL;
    const GLcharARB *shaderStringArr[1];


    /* Assertion check the shaderId */
    if (shaderId == 0) {
	throwAssert(env, "shaderId == 0");
	return NULL;
    }

    /* Assertion check the program string */
    if (program == NULL) {
	throwAssert(env, "shader program string is NULL");
	return NULL;
    }

    shaderString = (GLcharARB *)strJavaToC(env, program);
    if (shaderString == NULL) {	
	/* Just return, since strJavaToC will throw OOM if it returns NULL */
	return NULL;
    }

    shaderStringArr[0] = shaderString;
    glslCtxInfo->pfnglShaderSourceARB((GLhandleARB)shaderId, 1, shaderStringArr, NULL);
    glslCtxInfo->pfnglCompileShaderARB((GLhandleARB)shaderId);
    glslCtxInfo->pfnglGetObjectParameterivARB((GLhandleARB)shaderId,
						GL_OBJECT_COMPILE_STATUS_ARB,
						&status);
    if (!status) {
	const char *detailMsg = getInfoLog(ctxProperties, (GLhandleARB)shaderId);

	shaderError = createShaderError(env,
					javax_media_j3d_ShaderError_COMPILE_ERROR,
					"GLSL shader compile error",
					detailMsg);
    }

    free(shaderString);    
    return shaderError;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    createGLSLShaderProgram
 * Signature: (J[J)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_createGLSLShaderProgram(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlongArray shaderProgramIdArray)    
{

    jlong *shaderProgramIdPtr;
    GLhandleARB shaderProgramHandle;
    jobject shaderError = NULL;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    shaderProgramIdPtr = (*env)->GetLongArrayElements(env, shaderProgramIdArray, NULL);

    shaderProgramHandle = glslCtxInfo->pfnglCreateProgramObjectARB();

    if (shaderProgramHandle == 0) {
	shaderError = createShaderError(env,
					javax_media_j3d_ShaderError_LINK_ERROR,
					"Unable to create native shader program object",
					NULL);
    }

    shaderProgramIdPtr[0] = (jlong) shaderProgramHandle;
    (*env)->ReleaseLongArrayElements(env, shaderProgramIdArray, shaderProgramIdPtr, 0);
    
    return shaderError;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    destroyGLSLShaderProgram
 * Signature: (JJ)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_destroyGLSLShaderProgram(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglglDeleteObjectARB((GLhandleARB)shaderProgramId);

    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    linkGLSLShaderProgram
 * Signature: (JJ[J)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_linkGLSLShaderProgram(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlongArray shaderIdArray)
{
    GLint status;
    int i;
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    jlong *shaderIdPtr = (*env)->GetLongArrayElements(env, shaderIdArray, NULL);
    jsize shaderIdArrayLength = (*env)->GetArrayLength(env,  shaderIdArray);
    jobject shaderError = NULL;

    /*
    fprintf(stderr, "linkShaderProgram: shaderIdArrayLength %d\n", shaderIdArrayLength);
    */
    
    for(i=0; i<shaderIdArrayLength; i++) {
	glslCtxInfo->pfnglAttachObjectARB((GLhandleARB)shaderProgramId,
					    (GLhandleARB)shaderIdPtr[i]);
    }

    glslCtxInfo->pfnglLinkProgramARB((GLhandleARB)shaderProgramId);
    glslCtxInfo->pfnglGetObjectParameterivARB((GLhandleARB)shaderProgramId,
						GL_OBJECT_LINK_STATUS_ARB,
						&status);

    if (!status) {
	const char *detailMsg = getInfoLog(ctxProperties, (GLhandleARB)shaderProgramId);

	shaderError = createShaderError(env,
					javax_media_j3d_ShaderError_LINK_ERROR,
					"GLSL shader program link error",
					detailMsg);
    }

    (*env)->ReleaseLongArrayElements(env, shaderIdArray, shaderIdPtr, JNI_ABORT); 

    return shaderError;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    bindGLSLVertexAttrName
 * Signature: (JJLjava/lang/String;I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL
Java_javax_media_j3d_NativePipeline_bindGLSLVertexAttrName(
    JNIEnv * env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jstring attrName,
    jint attrIndex)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    jobject shaderError = NULL;
    GLcharARB *attrNameString = (GLcharARB *)strJavaToC(env, attrName);

    /*
    fprintf(stderr,
	    "GLSLShaderProgramRetained.bindGLSLVertexAttrName: %s\n",
	    attrNameString);
    */

    glslCtxInfo->pfnglBindAttribLocationARB((GLhandleARB)shaderProgramId,
					      attrIndex + 1,
					      attrNameString);

    /* No error checking needed, so just return */

    return shaderError;
}


static jint
glslToJ3dType(GLint type)
{
    switch (type) {
    case GL_BOOL_ARB:
    case GL_INT:
    case GL_SAMPLER_2D_ARB:
    case GL_SAMPLER_3D_ARB:
    case GL_SAMPLER_CUBE_ARB:
	return TYPE_INTEGER;

    case GL_FLOAT:
	return TYPE_FLOAT;

    case GL_INT_VEC2_ARB:
    case GL_BOOL_VEC2_ARB:
	return TYPE_TUPLE2I;

    case GL_FLOAT_VEC2_ARB:
	return TYPE_TUPLE2F;

    case GL_INT_VEC3_ARB:
    case GL_BOOL_VEC3_ARB:
	return TYPE_TUPLE3I;

    case GL_FLOAT_VEC3_ARB:
	return TYPE_TUPLE3F;

    case GL_INT_VEC4_ARB:
    case GL_BOOL_VEC4_ARB:
	return TYPE_TUPLE4I;

    case GL_FLOAT_VEC4_ARB:
	return TYPE_TUPLE4F;

    /* case GL_FLOAT_MAT2_ARB: */

    case GL_FLOAT_MAT3_ARB:
	return TYPE_MATRIX3F;

    case GL_FLOAT_MAT4_ARB:
	return TYPE_MATRIX4F;

    /*
     * Java 3D does not support the following sampler types:
     *
     * case GL_SAMPLER_1D_ARB:
     * case GL_SAMPLER_1D_SHADOW_ARB:
     * case GL_SAMPLER_2D_SHADOW_ARB:
     * case GL_SAMPLER_2D_RECT_ARB:
     * case GL_SAMPLER_2D_RECT_SHADOW_ARB:
     */
    }

    return -1;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    lookupGLSLShaderAttrNames
 * Signature: (JJI[Ljava/lang/String;[J[I[I[Z)V
 */
JNIEXPORT void JNICALL
Java_javax_media_j3d_NativePipeline_lookupGLSLShaderAttrNames(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jint numAttrNames,
    jobjectArray attrNames,
    jlongArray locArr,
    jintArray typeArr,
    jintArray sizeArr,
    jbooleanArray isArrayArr)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    GLcharARB **attrNamesString;
    jlong *locPtr;
    jint *typePtr;
    jint *sizePtr;
    jboolean *isArrayPtr;
    GLint loc;
    GLenum type;
    GLint size;
    GLcharARB *name;
    GLint maxStrLen;
    int numActiveUniforms;
    int i, j;
    
    JNIEnv table = *env;

#ifdef VERBOSE
    fprintf(stderr, "GLSLShaderProgramRetained.lookupGLSLShaderAttrNames\n");
#endif

    locPtr = (*env)->GetLongArrayElements(env, locArr, NULL);
    typePtr = (*env)->GetIntArrayElements(env, typeArr, NULL);
    sizePtr = (*env)->GetIntArrayElements(env, sizeArr, NULL);
    isArrayPtr = (*env)->GetBooleanArrayElements(env, isArrayArr, NULL);

    /*
     * Initialize the name array, also set the loc, type, and size
     * arrays to out-of-band values
     */
    attrNamesString = (GLcharARB **)malloc(numAttrNames * sizeof(GLcharARB *));
    for (i = 0; i < numAttrNames; i++) {
	jstring attrName;

        attrName = (*env)->GetObjectArrayElement(env, attrNames, i);
        attrNamesString[i] = (GLcharARB *)strJavaToC(env, attrName);

	locPtr[i] = -1;
	typePtr[i] = -1;
	sizePtr[i] = -1;
    }

    /*
     * Loop through the list of active uniform variables, one at a
     * time, searching for a match in the attrNames array.
     *
     * NOTE: Since attrNames isn't sorted, and we don't have a
     * hashtable of names to index locations, we will do a
     * brute-force, linear search of the array. This leads to an
     * O(n^2) algorithm (actually O(n*m) where n is attrNames.length
     * and m is the number of uniform variables), but since we expect
     * N to be small, we will not optimize this at this time.
     */
    glslCtxInfo->pfnglGetObjectParameterivARB((GLhandleARB) shaderProgramId,
						GL_OBJECT_ACTIVE_UNIFORMS_ARB,
						&numActiveUniforms);
    glslCtxInfo->pfnglGetObjectParameterivARB((GLhandleARB) shaderProgramId,
						GL_OBJECT_ACTIVE_UNIFORM_MAX_LENGTH_ARB,
						&maxStrLen);
    name = malloc(maxStrLen + 1);

#ifdef VERBOSE
    fprintf(stderr,
	    "numActiveUniforms = %d, maxStrLen = %d\n",
	    numActiveUniforms, maxStrLen);
#endif

    for (i = 0; i < numActiveUniforms; i++) {
        int len;

	glslCtxInfo->pfnglGetActiveUniformARB((GLhandleARB) shaderProgramId,
						i,
						maxStrLen,
						NULL,
						&size,
						&type,
						name);

        /*
         * Issue 247 - we need to workaround an ATI bug where they erroneously
         * report individual elements of arrays rather than the array itself
         */
        len = strlen(name);
        if (len >= 3 && name[len-1] == ']') {
            if (strcmp(&name[len-3], "[0]") == 0) {
                /* fprintf(stderr, "**** changing \"%s\" ", name); */
                name[len-3] = '\0';
                /* fprintf(stderr, "to \"%s\"\n", name); */
            } else {
                /* Ignore this name */
                /* fprintf(stderr, "Uniform[%d] : %s ignored\n", i, name); */
                free(name);
                continue;
            }
        }

#ifdef VERBOSE
	fprintf(stderr,
		"Uniform[%d] : name = %s ; type = %d ; size = %d\n",
		i, name, type, size);
#endif

	/* Now try to find the name */
	for (j = 0; j < numAttrNames; j++) {
	    if (strcmp(attrNamesString[j], name) == 0) {
		sizePtr[j] = (jint)size;
                isArrayPtr[j] = (size > 1);
		typePtr[j] = glslToJ3dType(type);
		break;
	    }
	}
    }

    free(name);

    /* Now lookup the location of each name in the attrNames array */
    for (i = 0; i < numAttrNames; i++) {
        /*
         * Get uniform attribute location
         */
        loc = glslCtxInfo->pfnglGetUniformLocationARB((GLhandleARB)shaderProgramId,
                                                        attrNamesString[i]);

#ifdef VERBOSE
        fprintf(stderr,
                "str = %s, loc = %d\n",
                attrNamesString[i], loc);
#endif

        locPtr[i] = (jlong)loc;
    }

    /* Free the array of strings */
    for (i = 0; i < numAttrNames; i++) {
        free(attrNamesString[i]);
    }
    free(attrNamesString);

    /* Release JNI arrays */
    (*env)->ReleaseLongArrayElements(env, locArr, locPtr, 0);
    (*env)->ReleaseIntArrayElements(env, typeArr, typePtr, 0);
    (*env)->ReleaseIntArrayElements(env, sizeArr, sizePtr, 0);
    (*env)->ReleaseBooleanArrayElements(env, isArrayArr, isArrayPtr, 0);
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    useGLSLShaderProgram
 * Signature: (JI)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_useGLSLShaderProgram(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId)
{
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglUseProgramObjectARB((GLhandleARB)shaderProgramId);

    ctxProperties->shaderProgramId = shaderProgramId;

    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform1i
 * Signature: (JJJI)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform1i(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint value)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Load attribute */
    glslCtxInfo->pfnglUniform1iARB((GLint)location, value);

    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform1f
 * Signature: (JJJF)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform1f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloat value)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    
    /* Load attribute */
    glslCtxInfo->pfnglUniform1fARB((GLint)location, value);

    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform2i
 * Signature: (JJJ[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform2i(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jintArray varray)
{    
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    jint *values;
	
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    
    /* Get array values */
    values = (*env)->GetIntArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform2iARB((GLint)location, values[0], values[1]);

    /* Release array values */
    (*env)->ReleaseIntArrayElements(env, varray, values, JNI_ABORT);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform2f
 * Signature: (JJJ[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform2f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloatArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    jfloat *values;
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Why shaderProgramId is not needed ? */
    
    /* Get array values */
    values = (*env)->GetFloatArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform2fARB((GLint)location, values[0], values[1]);

    /* Release array values */
    (*env)->ReleaseFloatArrayElements(env, varray, values, JNI_ABORT);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform3i
 * Signature: (JJJ[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform3i(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jintArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    jint *values;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (*env)->GetIntArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform3iARB((GLint)location, values[0], values[1], values[2]);

    /* Release array values */
    (*env)->ReleaseIntArrayElements(env, varray, values, JNI_ABORT);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform3f
 * Signature: (JJJ[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform3f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloatArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    jfloat *values;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    
    /* Get array values */
    values = (*env)->GetFloatArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform3fARB((GLint)location, values[0], values[1], values[2]);

    /* Release array values */
    (*env)->ReleaseFloatArrayElements(env, varray, values, JNI_ABORT);
    
    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform4i
 * Signature: (JJJ[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform4i(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jintArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    jint *values;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    /* Get array values */
    values = (*env)->GetIntArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform4iARB((GLint)location, values[0], values[1], values[2], values[3]);

    /* Release array values */
    (*env)->ReleaseIntArrayElements(env, varray, values, JNI_ABORT);

    return NULL;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform4f
 * Signature: (JJJ[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform4f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloatArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    jfloat *values;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (*env)->GetFloatArrayElements(env, varray, NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform4fARB((GLint)location, values[0], values[1], values[2], values[3]);

    /* Release array values */
    (*env)->ReleaseFloatArrayElements(env, varray, values, JNI_ABORT);

    return NULL;

}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniformMatrix3f
 * Signature: (JJJ[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniformMatrix3f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloatArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    jfloat *values;

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (*env)->GetFloatArrayElements(env, varray, NULL);

    /* Load attribute */
    /*  transpose is GL_TRUE : each matrix is supplied in row major order */
    glslCtxInfo->pfnglUniformMatrix3fvARB((GLint)location, 1, GL_TRUE, (GLfloat *)values);

    /* Release array values */
    (*env)->ReleaseFloatArrayElements(env, varray, values, JNI_ABORT);

    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniformMatrix4f
 * Signature: (JJJ[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniformMatrix4f(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jfloatArray varray)
{
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    jfloat *values;
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (*env)->GetFloatArrayElements(env, varray, NULL);
    
    /* Load attribute */
    /*  transpose is GL_TRUE : each matrix is supplied in row major order */
    glslCtxInfo->pfnglUniformMatrix4fvARB((GLint)location, 1, GL_TRUE, (GLfloat *)values);

    /* Release array values */
    (*env)->ReleaseFloatArrayElements(env, varray, values, JNI_ABORT);
    
    return NULL;
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform1iArray
 * Signature: (JJJI[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform1iArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jintArray vArray)
{

    JNIEnv table = *env;
    jint *values;
    
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;
    
    /* Get array values */
    values = (jint *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);
    
    /* Load attribute */
    glslCtxInfo->pfnglUniform1ivARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform1fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform1fArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{
    
    JNIEnv table = *env;
    jfloat *values;
    
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */
    
    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform1fvARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;
  
}

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform2iArray
 * Signature: (JJJI[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform2iArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jintArray vArray)
{

    JNIEnv table = *env;
    jint *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jint *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform2ivARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}

 

/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform2fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform2fArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{

    JNIEnv table = *env;
    jfloat *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform2fvARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform3iArray
 * Signature: (JJJI[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform3iArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jintArray vArray)
{

    JNIEnv table = *env;
    jint *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jint *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform3ivARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform3fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform3fArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{

    JNIEnv table = *env;
    jfloat *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform3fvARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform4iArray
 * Signature: (JJJI[I)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform4iArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jintArray vArray)
{

    JNIEnv table = *env;
    jint *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jint *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform4ivARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniform4fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniform4fArray(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{

    JNIEnv table = *env;
    jfloat *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    glslCtxInfo->pfnglUniform4fvARB((GLint)location, length, values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;

}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniformMatrix3fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniformMatrix3fArray
(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{

    JNIEnv table = *env;
    jfloat *values;

    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */    

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);

    /* Load attribute */
    /*  transpose is GL_TRUE : each matrix is supplied in row major order */
    glslCtxInfo->pfnglUniformMatrix3fvARB((GLint)location, length,
					    GL_TRUE, (GLfloat *)values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);

    return NULL;
}


/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    setGLSLUniformMatrix4fArray
 * Signature: (JJJI[F)Ljavax/media/j3d/ShaderError;
 */
JNIEXPORT jobject
JNICALL Java_javax_media_j3d_NativePipeline_setGLSLUniformMatrix4fArray
(
    JNIEnv *env,
    jobject obj,
    jlong ctxInfo,
    jlong shaderProgramId,
    jlong location,
    jint length,
    jfloatArray vArray)
{

    JNIEnv table = *env;
    jfloat *values;
    
    /* We do not need to use shaderProgramId because caller has already called
       useShaderProgram(). */    

    GraphicsContextPropertiesInfo* ctxProperties =  (GraphicsContextPropertiesInfo* )ctxInfo;
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    /* Get array values */
    values = (jfloat *)(*(table->GetPrimitiveArrayCritical))(env, vArray , NULL);
    
    /* Load attribute */
    /*  transpose is GL_TRUE : each matrix is supplied in row major order */
    glslCtxInfo->pfnglUniformMatrix4fvARB((GLint)location, length,
					    GL_TRUE, (GLfloat *)values);

    /* Release array values */
    (*(table->ReleasePrimitiveArrayCritical))(env, vArray, values, 0);
    
    return NULL;
}


/*
 * GLSL vertex attribute functions
 */

static void
glslVertexAttrPointer(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, int size, int type, int stride,
    const void *pointer)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglVertexAttribPointerARB(index+1, size, type,
					     GL_FALSE, stride, pointer);
}

static void
glslEnableVertexAttrArray(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglEnableVertexAttribArrayARB(index+1);
}

static void
glslDisableVertexAttrArray(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

    glslCtxInfo->pfnglDisableVertexAttribArrayARB(index+1);
}

static void
glslVertexAttr1fv(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, const float *v)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

#ifdef VERBOSE
    fprintf(stderr, "glslVertexAttr1fv()\n");
#endif
    glslCtxInfo->pfnglVertexAttrib1fvARB(index+1, v);
}

static void
glslVertexAttr2fv(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, const float *v)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

#ifdef VERBOSE
    fprintf(stderr, "glslVertexAttr2fv()\n");
#endif
    glslCtxInfo->pfnglVertexAttrib2fvARB(index+1, v);
}

static void
glslVertexAttr3fv(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, const float *v)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

#ifdef VERBOSE
    fprintf(stderr, "glslVertexAttr3fv()\n");
#endif
    glslCtxInfo->pfnglVertexAttrib3fvARB(index+1, v);
}

static void
glslVertexAttr4fv(
    GraphicsContextPropertiesInfo *ctxProperties,
    int index, const float *v)
{
    GLSLCtxInfo *glslCtxInfo = ctxProperties->glslCtxInfo;

#ifdef VERBOSE
    fprintf(stderr, "glslVertexAttr4fv()\n");
#endif
    glslCtxInfo->pfnglVertexAttrib4fvARB(index+1, v);
}

