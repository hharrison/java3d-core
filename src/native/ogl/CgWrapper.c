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
 * Note: since this is just a wrapper around the CG functions, it
 * won't be compiled or linked unless the CG library is
 * available. This means we don't need to use "#ifdef COMPILE_CG".
 */

#if defined(LINUX)
#define _GNU_SOURCE 1
#endif

#include <stdio.h>
#include <stdlib.h>

#include "gldefs.h"
#include "CgWrapper.h"

/*
 * Setup the function pointers
 */
JNIEXPORT void
j3dLoadCgFunctionPointers(CgWrapperInfo *cgWrapperInfo)
{
    cgWrapperInfo->cgCreateContext = &cgCreateContext;
    cgWrapperInfo->cgGLIsProfileSupported = &cgGLIsProfileSupported;
    cgWrapperInfo->cgGetError = &cgGetError;
    cgWrapperInfo->cgGetErrorString = &cgGetErrorString;
    cgWrapperInfo->cgGetLastListing = &cgGetLastListing;
    cgWrapperInfo->cgCreateProgram = &cgCreateProgram;
    cgWrapperInfo->cgDestroyProgram = &cgDestroyProgram;
    cgWrapperInfo->cgGLLoadProgram = &cgGLLoadProgram;
    cgWrapperInfo->cgGLBindProgram = &cgGLBindProgram;
    cgWrapperInfo->cgGLUnbindProgram = &cgGLUnbindProgram;
    cgWrapperInfo->cgGLEnableProfile = &cgGLEnableProfile;
    cgWrapperInfo->cgGLDisableProfile = &cgGLDisableProfile;
    cgWrapperInfo->cgGetNamedParameter = &cgGetNamedParameter;
    cgWrapperInfo->cgGetParameterType = &cgGetParameterType;
    cgWrapperInfo->cgGetArrayDimension = &cgGetArrayDimension;
    cgWrapperInfo->cgGetArrayType = &cgGetArrayType;
    cgWrapperInfo->cgGetArraySize = &cgGetArraySize;
    cgWrapperInfo->cgGetArrayParameter = &cgGetArrayParameter;
    cgWrapperInfo->cgSetParameter1f = &cgSetParameter1f;
    cgWrapperInfo->cgGLSetParameterPointer = &cgGLSetParameterPointer;
    cgWrapperInfo->cgGLEnableClientState = &cgGLEnableClientState;
    cgWrapperInfo->cgGLDisableClientState = &cgGLDisableClientState;

    return;
}
