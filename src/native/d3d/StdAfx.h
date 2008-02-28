/*
 * $RCSfile$
 *
 * Copyright 2000-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

#if !defined(AFX_STDAFX_H)
#define AFX_STDAFX_H

#ifndef WINVER
#define WINVER 0x0501
#endif

#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0501
#endif

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef _MT
#define _MT
#endif 

// Exclude rarely-used stuff from Windows headers
#define VC_EXTRALEAN
#define WIN32_LEAN_AND_MEAN


//#undef _AFXDLL
//#undef _UNICODE

// Windows Header Files:
#include <afx.h> 
#include <winbase.h>
#include <windows.h>
#include <multimon.h>

// C RunTime Header Files
#include <stdlib.h>
#include <malloc.h>
#include <memory.h>
#include <tchar.h>
#include <string.h>
#include <jni.h>
#include <math.h>
#define D3D_OVERLOADS
#include <d3d9.h>
#include <dxerr9.h>
#include <d3dx9.h>
#include <d3dx9tex.h>
#include <vector>
#include <algorithm>
using namespace std ;

// Local header file
#include "gldefs.h"
#include "D3dDeviceInfo.hpp"
#include "D3dDriverInfo.hpp"
#include "D3dCtx.hpp"
#include "D3dUtil.hpp"
#include "D3dVertexBuffer.hpp"
#include "D3dDisplayList.hpp"
#endif

