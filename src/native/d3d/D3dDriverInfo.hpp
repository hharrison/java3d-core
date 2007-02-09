/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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
