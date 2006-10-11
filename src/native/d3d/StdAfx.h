/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$_WIN32_WINNT
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

