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

#if !defined(D3DDRIVERINFO_H)
#define D3DDRIVERINFO_H

#include "StdAfx.h"


#define DEVICE_HAL         0
#define DEVICE_REF         1
#define DEVICE_HAL_TnL     2

extern D3DFORMAT d3dDepthFormat[D3DDEPTHFORMATSIZE];
extern int d3dDepthTable[D3DDEPTHFORMATSIZE];
extern int d3dStencilDepthTable[D3DDEPTHFORMATSIZE];
			
class D3dDriverInfo {
public:
    // DDraw Driver info
    D3DADAPTER_IDENTIFIER9 adapterIdentifier;
    // Desktop display mode for this adapter
    D3DDISPLAYMODE desktopMode; 
    // monitor handle for this adapter
    HMONITOR        hMonitor;

    // Index position in the adapter list
    UINT iAdapter;

    // Support devices : HAL or REF
    D3dDeviceInfo* d3dDeviceList[2];

    // Use for NativeConfigTemplate to
    // determine the min. config support
    UINT  redDepth, greenDepth, blueDepth;

    D3dDriverInfo();
    ~D3dDriverInfo();

    static VOID initialize(JNIEnv *env); 
    static VOID release();
};

typedef D3dDriverInfo* LPD3dDriverInfo;
extern int numDriver; // size of above array list
extern D3dDriverInfo **d3dDriverList;
extern const DWORD numDeviceTypes;
extern const D3DDEVTYPE deviceTypes[2];
extern int requiredDeviceID;   // force to use HAL/REF or exit   
extern int requiredDriverID;   // force to use specific adapte
extern D3DLIGHT9 ambientLight; // constant ambient light
extern BOOL implicitMultisample; 
#endif
