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

#ifndef _Java3D_CgWrapper_h_
#define _Java3D_CgWrapper_h_

#include "gldefs.h"

#ifdef COMPILE_CG_SHADERS
#include <Cg/cgGL.h>
#endif /* COMPILE_CG_SHADERS */


/* Forward references for structure typedefs */
typedef struct CgWrapperInfoRec CgWrapperInfo;
typedef struct CgShaderInfoRec CgShaderInfo;
typedef struct CgShaderProgramInfoRec CgShaderProgramInfo;
typedef struct CgParameterInfoRec CgParameterInfo;


/* Typedef for function pointer to entry point in CgWrapper library */
typedef void (*PFNJ3DLOADCGFUNCTIONPOINTERS)(CgWrapperInfo *);


#ifdef COMPILE_CG_SHADERS
typedef CGcontext (*PFNCGCREATECONTEXT)(void); 
typedef CGbool (*PFNCGGLISPROFILESUPPORTED)(CGprofile);
typedef CGerror (*PFNCGGETERROR)(void);
typedef const char * (*PFNCGGETERRORSTRING)(CGerror);
typedef const char * (*PFNCGGETLASTLISTING)(CGcontext);
typedef CGprogram (*PFNCGCREATEPROGRAM)(CGcontext, CGenum, const char *,
					CGprofile, const char *, const char **);
typedef void (*PFNCGDESTROYPROGRAM)(CGprogram program); 
typedef void (*PFNCGGLLOADPROGRAM)(CGprogram);
typedef void (*PFNCGGLBINDPROGRAM)(CGprogram);
typedef void (*PFNCGGLUNBINDPROGRAM)(CGprofile);
typedef void (*PFNCGGLENABLEPROFILE)(CGprofile);
typedef void (*PFNCGGLDISABLEPROFILE)(CGprofile);
typedef CGparameter (*PFNCGGETNAMEDPARAMETER)(CGprogram, const char *);
typedef CGtype (*PFNCGGETPARAMETERTYPE)(CGparameter);
typedef int (*PFNCGGETARRAYDIMENSION)(CGparameter);
typedef CGtype (*PFNCGGETARRAYTYPE)(CGparameter);
typedef int (*PFNCGGETARRAYSIZE)(CGparameter, int);
typedef CGparameter (*PFNCGGETARRAYPARAMETER)(CGparameter, int);
typedef void (*PFNCGGLSETPARAMETER1F)(CGparameter, float);
typedef void (*PFNCGGLSETPARAMETER2F)(CGparameter, float, float);
typedef void (*PFNCGGLSETPARAMETER3F)(CGparameter, float, float, float);
typedef void (*PFNCGGLSETPARAMETER4F)(CGparameter, float, float, float, float);
typedef void (*PFNCGGLSETPARAMETERARRAY1F)(CGparameter, long, long, const float *);
typedef void (*PFNCGGLSETPARAMETERARRAY2F)(CGparameter, long, long, const float *);
typedef void (*PFNCGGLSETPARAMETERARRAY3F)(CGparameter, long, long, const float *);
typedef void (*PFNCGGLSETPARAMETERARRAY4F)(CGparameter, long, long, const float *);
typedef void (*PFNCGGLSETMATRIXPARAMETERFR)(CGparameter, const float *);
typedef void (*PFNCGGLSETMATRIXPARAMETERARRAYFR)(CGparameter, long, long, const float *);
typedef void (*PFNCGGLSETPARAMETERPOINTER)(CGparameter, GLint, GLenum,
					   GLsizei, const GLvoid *);
typedef void (*PFNCGGLENABLECLIENTSTATE)(CGparameter);
typedef void (*PFNCGGLDISABLECLIENTSTATE)(CGparameter);

#endif /* COMPILE_CG_SHADERS */


/*
 * Global struct that contains the reference to the CG wrapper library
 * and the function pointers to each wrapper function.  This is a
 * singleton (only one instance exists), and is initialized by a
 * static MasterControl method. For performance, we will cache the
 * pointer to this global struct in each context.
 */
struct CgWrapperInfoRec {
    /*
     * Flag indicating whether the library and all function pointers
     * were successfully loaded.
     */
    jboolean loaded;

    /*
     * Pointer to library, returned by dlopen (UNIX) or LoadLibrary (Windows)
     */
    void *cgLibraryHandle;

    /* Function pointer to entry point in CgWrapper library */
    PFNJ3DLOADCGFUNCTIONPOINTERS j3dLoadCgFunctionPointers;

    /* Function pointers for warapper functions */
#ifdef COMPILE_CG_SHADERS

    /* CG function pointers */
    PFNCGCREATECONTEXT cgCreateContext;
    PFNCGGLISPROFILESUPPORTED cgGLIsProfileSupported;
    PFNCGGETERROR cgGetError;
    PFNCGGETERRORSTRING cgGetErrorString;
    PFNCGGETLASTLISTING cgGetLastListing;
    PFNCGCREATEPROGRAM cgCreateProgram;
    PFNCGDESTROYPROGRAM cgDestroyProgram;
    PFNCGGLLOADPROGRAM cgGLLoadProgram;
    PFNCGGLBINDPROGRAM cgGLBindProgram;
    PFNCGGLUNBINDPROGRAM cgGLUnbindProgram;
    PFNCGGLENABLEPROFILE cgGLEnableProfile;
    PFNCGGLDISABLEPROFILE cgGLDisableProfile;
    PFNCGGETNAMEDPARAMETER cgGetNamedParameter;
    PFNCGGETPARAMETERTYPE cgGetParameterType;
    PFNCGGETARRAYDIMENSION cgGetArrayDimension;
    PFNCGGETARRAYTYPE cgGetArrayType;
    PFNCGGETARRAYSIZE cgGetArraySize;
    PFNCGGETARRAYPARAMETER cgGetArrayParameter;
    PFNCGGLSETPARAMETER1F cgGLSetParameter1f;
    PFNCGGLSETPARAMETER2F cgGLSetParameter2f;
    PFNCGGLSETPARAMETER3F cgGLSetParameter3f;
    PFNCGGLSETPARAMETER4F cgGLSetParameter4f;
    PFNCGGLSETPARAMETERARRAY1F cgGLSetParameterArray1f;
    PFNCGGLSETPARAMETERARRAY2F cgGLSetParameterArray2f;
    PFNCGGLSETPARAMETERARRAY3F cgGLSetParameterArray3f;
    PFNCGGLSETPARAMETERARRAY4F cgGLSetParameterArray4f;
    PFNCGGLSETMATRIXPARAMETERFR cgGLSetMatrixParameterfr;
    PFNCGGLSETMATRIXPARAMETERARRAYFR cgGLSetMatrixParameterArrayfr;
    PFNCGGLSETPARAMETERPOINTER cgGLSetParameterPointer;
    PFNCGGLENABLECLIENTSTATE cgGLEnableClientState;
    PFNCGGLDISABLECLIENTSTATE cgGLDisableClientState;

#endif /* COMPILE_CG_SHADERS */
};


/* Structure used to hold CG context information; stored in ctxInfo */
struct CgCtxInfoRec {
    CgWrapperInfo *cgWrapperInfo; /* Pointer to static wrapper info */

#ifdef COMPILE_CG_SHADERS
    CGcontext cgCtx;
    CGprofile vProfile;
    CGprofile fProfile;
#endif /* COMPILE_CG_SHADERS */
};


/* Structure used to hold CG shader information; passed back to Java as cgShaderId */
struct CgShaderInfoRec {
#ifdef COMPILE_CG_SHADERS
    CGprogram cgShader;
    jint shaderType;
    CGprofile shaderProfile;
#else /* COMPILE_CG_SHADERS */
    int dummy;
#endif /* COMPILE_CG_SHADERS */
};

/*
 * Structure used to hold CG shader program information; passed back
 * to Java as cgShaderProgramId
 */
struct CgShaderProgramInfoRec {
#ifdef COMPILE_CG_SHADERS
    /*
     * Vertex and fragment shader -- may be null to indicate that one
     *  or the other is not present
     */
    CgShaderInfo *vShader; /* Vertex shader */
    CgShaderInfo *fShader; /* Fragment shader */

    /* Array of parameters for (varying) vertex attributes */
    int numVtxAttrs;
    CGparameter *vtxAttrs;
#else /* COMPILE_CG_SHADERS */
    int dummy;
#endif /* COMPILE_CG_SHADERS */
};

/*
 * Structure used to hold CG shader parameter information for uniform
 * shader attributes; passed back to Java in the locArr array
 */
struct CgParameterInfoRec {
#ifdef COMPILE_CG_SHADERS
    CGparameter vParam; /* Parameter handle for vertex shader */
    CGparameter fParam; /* Parameter handle for fragment shader */
#else /* COMPILE_CG_SHADERS */
    int dummy;
#endif /* COMPILE_CG_SHADERS */
};


#endif /* _Java3D_CgWrapper_h_ */
