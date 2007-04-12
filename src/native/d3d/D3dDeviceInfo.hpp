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

#if !defined(D3DDEVICEINFO_H)
#define D3DDEVICEINFO_H

#include "StdAfx.h"

extern UINT vertexBufferMaxVertexLimit;

// Fix to Issue 226 : D3D - fail on stress test for the creation and destruction of Canvases
#define D3DDEPTHFORMATSIZE 7

class D3dDeviceInfo {
    public:
        // Hardware Rasterizer
        // Transform & Light Hardware Rasterizer
        // Reference Rasterizer
        char deviceName[40];         // One of above name
        D3DDEVTYPE   deviceType;     // D3DDEVTYPE_HAL or D3DDEVTYPE_REF
        BOOL desktopCompatible;      // Can render in desktop mode
        BOOL fullscreenCompatible;   // Can render in fullscreen mode
        // using current desktop mode setting
        //issue 135 - adding device info
        char* deviceVendor;
        char* deviceRenderer;
        char* deviceVersion;
        
        // each bitmask correspond to the support of
        // D3DMULTISAMPLE_i_SAMPLES type, i = 2...16
        DWORD multiSampleSupport;
        
        // TRUE when d3dDepthFormat[i] support
        BOOL  depthFormatSupport[D3DDEPTHFORMATSIZE];
        
        // depth format select
        D3DFORMAT depthStencilFormat;
        
        // max z buffer depth support
        UINT  maxZBufferDepthSize;
        
        // max stencil buffer depth support
        UINT  maxStencilDepthSize; // new on 1.4
        
        // Max vertex count support for each primitive
        DWORD maxVertexCount[GEO_TYPE_INDEXED_LINE_STRIP_SET+1];
        
        BOOL supportStencil; // new on 1.4
        BOOL supportShaders11;
        BOOL isHardware;
        BOOL isHardwareTnL;
        BOOL supportDepthBias;
        BOOL supportRasterPresImmediate;
        BOOL canRenderWindowed;
        BOOL supportMipmap;
        BOOL texturePow2Only;
        BOOL textureSquareOnly;
        BOOL linePatternSupport;
        BOOL texBorderModeSupport;
        BOOL texLerpSupport;
        DWORD maxTextureUnitStageSupport;
        DWORD maxTextureBlendStages;
        DWORD maxSimultaneousTextures;
        DWORD maxTextureWidth;
        DWORD maxTextureHeight;
        DWORD maxTextureDepth;
        DWORD maxPrimitiveCount;
        DWORD maxVertexIndex;
        DWORD maxActiveLights;
        DWORD maxPointSize;
        DWORD rangeFogEnable;
        D3DRENDERSTATETYPE  fogMode;
        int texMask;
        int maxAnisotropy;
        
        BOOL supportStreamOffset;
        
        D3dDeviceInfo();
        ~D3dDeviceInfo();
        
        // set capabilities of this device
        VOID setCaps(D3DCAPS9 *d3dCaps);
        BOOL supportAntialiasing();
        D3DMULTISAMPLE_TYPE getBestMultiSampleType();
        int getTextureFeaturesMask();
        void findDepthStencilFormat(int minZDepth, int minZDepthStencil);
        
        
};

#endif

