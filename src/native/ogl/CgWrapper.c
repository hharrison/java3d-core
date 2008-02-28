/*
 * $RCSfile$
 *
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

/*
 * Note: since this is just a wrapper around the CG functions, it
 * won't be compiled or linked unless the CG library is
 * available. This means we don't need to use "#ifdef COMPILE_CG".
 */

/* j3dsys.h needs to be included before any other include files to suppres VC warning */
#include "j3dsys.h"

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
    cgWrapperInfo->cgGLSetParameter1f = &cgGLSetParameter1f;
    cgWrapperInfo->cgGLSetParameter2f = &cgGLSetParameter2f;
    cgWrapperInfo->cgGLSetParameter3f = &cgGLSetParameter3f;
    cgWrapperInfo->cgGLSetParameter4f = &cgGLSetParameter4f;
    cgWrapperInfo->cgGLSetParameterArray1f = &cgGLSetParameterArray1f;
    cgWrapperInfo->cgGLSetParameterArray2f = &cgGLSetParameterArray2f;
    cgWrapperInfo->cgGLSetParameterArray3f = &cgGLSetParameterArray3f;
    cgWrapperInfo->cgGLSetParameterArray4f = &cgGLSetParameterArray4f;
    cgWrapperInfo->cgGLSetMatrixParameterfr = &cgGLSetMatrixParameterfr;
    cgWrapperInfo->cgGLSetMatrixParameterArrayfr = &cgGLSetMatrixParameterArrayfr;
    cgWrapperInfo->cgGLSetParameterPointer = &cgGLSetParameterPointer;
    cgWrapperInfo->cgGLEnableClientState = &cgGLEnableClientState;
    cgWrapperInfo->cgGLDisableClientState = &cgGLDisableClientState;

    return;
}
