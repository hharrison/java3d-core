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

#ifndef _Java3D_GLSLInfo_h_
#define _Java3D_GLSLInfo_h_

#include "gldefs.h"

/* Structure used to hold GLSL context information; stored in ctxInfo */
struct GLSLCtxInfoRec {
    int vertexAttrOffset;

    PFNGLATTACHOBJECTARBPROC pfnglAttachObjectARB;
    PFNGLCOMPILESHADERARBPROC pfnglCompileShaderARB;
    PFNGLCREATEPROGRAMOBJECTARBPROC pfnglCreateProgramObjectARB;
    PFNGLCREATESHADEROBJECTARBPROC pfnglCreateShaderObjectARB;
    PFNGLDELETEOBJECTARBPROC pfnglglDeleteObjectARB;
    PFNGLGETINFOLOGARBPROC pfnglGetInfoLogARB;
    PFNGLGETOBJECTPARAMETERIVARBPROC pfnglGetObjectParameterivARB;
    PFNGLLINKPROGRAMARBPROC pfnglLinkProgramARB;
    PFNGLSHADERSOURCEARBPROC pfnglShaderSourceARB;
    PFNGLUSEPROGRAMOBJECTARBPROC pfnglUseProgramObjectARB;
    PFNGLGETUNIFORMLOCATIONARBPROC pfnglGetUniformLocationARB;
    PFNGLGETATTRIBLOCATIONARBPROC pfnglGetAttribLocationARB;
    PFNGLBINDATTRIBLOCATIONARBPROC pfnglBindAttribLocationARB;
    PFNGLVERTEXATTRIB1FVARBPROC pfnglVertexAttrib1fvARB;
    PFNGLVERTEXATTRIB2FVARBPROC pfnglVertexAttrib2fvARB;
    PFNGLVERTEXATTRIB3FVARBPROC pfnglVertexAttrib3fvARB;
    PFNGLVERTEXATTRIB4FVARBPROC pfnglVertexAttrib4fvARB;
    PFNGLVERTEXATTRIBPOINTERARBPROC pfnglVertexAttribPointerARB;
    PFNGLENABLEVERTEXATTRIBARRAYARBPROC pfnglEnableVertexAttribArrayARB;
    PFNGLDISABLEVERTEXATTRIBARRAYARBPROC pfnglDisableVertexAttribArrayARB;
    PFNGLGETACTIVEUNIFORMARBPROC pfnglGetActiveUniformARB;
    PFNGLUNIFORM1IARBPROC pfnglUniform1iARB;
    PFNGLUNIFORM1FARBPROC pfnglUniform1fARB;
    PFNGLUNIFORM2IARBPROC pfnglUniform2iARB;
    PFNGLUNIFORM2FARBPROC pfnglUniform2fARB;
    PFNGLUNIFORM3IARBPROC pfnglUniform3iARB;
    PFNGLUNIFORM3FARBPROC pfnglUniform3fARB;
    PFNGLUNIFORM4IARBPROC pfnglUniform4iARB;
    PFNGLUNIFORM4FARBPROC pfnglUniform4fARB;
    PFNGLUNIFORM1IVARBPROC pfnglUniform1ivARB;
    PFNGLUNIFORM1FVARBPROC pfnglUniform1fvARB;
    PFNGLUNIFORM2IVARBPROC pfnglUniform2ivARB;
    PFNGLUNIFORM2FVARBPROC pfnglUniform2fvARB;
    PFNGLUNIFORM3IVARBPROC pfnglUniform3ivARB;
    PFNGLUNIFORM3FVARBPROC pfnglUniform3fvARB;
    PFNGLUNIFORM4IVARBPROC pfnglUniform4ivARB;
    PFNGLUNIFORM4FVARBPROC pfnglUniform4fvARB;
    PFNGLUNIFORMMATRIX3FVARBPROC pfnglUniformMatrix3fvARB;
    PFNGLUNIFORMMATRIX4FVARBPROC pfnglUniformMatrix4fvARB;
};

#endif /* _Java3D_GLSLInfo_h_ */
