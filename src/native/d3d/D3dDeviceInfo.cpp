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

#include "Stdafx.h"

// return true if device is capable of hardware accelerated

D3dDeviceInfo::D3dDeviceInfo()
{
}

D3dDeviceInfo::~D3dDeviceInfo()
{
}

VOID D3dDeviceInfo::setCaps(D3DCAPS9 *d3dCaps) {
    
    BOOL supportNPOT;
    if (deviceType == D3DDEVTYPE_HAL ){
        isHardware = true;
        isHardwareTnL = (d3dCaps->DevCaps &  D3DDEVCAPS_HWTRANSFORMANDLIGHT);
    }
    else // D3DDEVTYPE_REF
    {
        isHardware = false;
        isHardwareTnL = false;
    }
    
    
    // D3DTEXTURECAPS_NONPOW2CONDITIONAL caps-bit indicates "conditional"
    // Non Power of Two (NPOT)
    // textures that only support CLAMP addressing and don't
    // support mipmaps or compressed textures.
    // But some new vcards supports NP2 unconditional (GF6 and above).
    // Correct test for any kind of NP2 support:
    // If both unconditional and conditional support is
    // unavailable then NP2 is not possible anyway.
    //  -------------------------------------------
    //  POW2 |  NP2_CONDITIONAL | result
    //  -------------------------------------------
    //  true |      true        | CONDITIONAL NPOT(*)
    //  true |      false       | POW2 Only
    //  false|      any         | UNConditional NPOT (**)	//
    // ---------------------------------------------
    // (**)OpenGL like,  Java3D preferred.
    // (*) below test:
    /*
     * if (((d3dCaps->TextureCaps & D3DPTEXTURECAPS_POW2) != 0) &&  // POW2 is true
     *     ((d3dCaps->TextureCaps & D3DPTEXTURECAPS_NONPOW2CONDITIONAL) == 0)){ //NPOT_Cond is false
     *      //Pow2 Only
     *      supportNPOT = false;
     *   }
     * else{
     *      // both conditional and unconditional
     *      supportNPOT = true;
     * }
     */
    if(d3dCaps->TextureCaps & D3DPTEXTURECAPS_POW2){
        supportNPOT = false;
        if(d3dCaps->TextureCaps & D3DPTEXTURECAPS_NONPOW2CONDITIONAL){
            // NPOT conditionl But, in certain cases textures can ignore the power of 2 limitation
            // As OpenGL is UNCONDITIONAL, it is not used by Java3D
            //supportNPOT = true;
        }
    } else {
        //UNconditional: Textures do not need to be a power of 2 in size
        supportNPOT = true;
    } 
    
    // check if it supports at least vertex shader 1.1
    if(d3dCaps->VertexShaderVersion < D3DVS_VERSION(1, 1)) {
        supportShaders11 = false;
    }
    else {
        supportShaders11 = true;
    }
    
	DWORD vsVersion = d3dCaps->VertexShaderVersion;
    if (debug) {		
        char* dt;
        if (isHardware)
            dt = "HAL";
        else
            dt ="REL";
        
        printf("Java3D: Supported Shaders = %d.%d in mode %s \n",
        HIBYTE(LOWORD(vsVersion)),
        LOBYTE(LOWORD(vsVersion)),
        dt);        
    }
    
    //supportStreamOffset =
    
    supportDepthBias = (d3dCaps->RasterCaps & D3DPRASTERCAPS_DEPTHBIAS) != 0;
    
    maxTextureBlendStages = d3dCaps->MaxTextureBlendStages;
    maxSimultaneousTextures = d3dCaps->MaxSimultaneousTextures;
    
    maxTextureUnitStageSupport = min(maxTextureBlendStages,  maxSimultaneousTextures);
    supportMipmap = ((d3dCaps->TextureCaps & D3DPTEXTURECAPS_MIPMAP) != 0);
    
    texturePow2Only =  ((d3dCaps->TextureCaps &  D3DPTEXTURECAPS_POW2) != 0);
    
    textureSquareOnly = ((d3dCaps->TextureCaps &   D3DPTEXTURECAPS_SQUAREONLY) != 0);
    
    linePatternSupport = false; //((d3dCaps->PrimitiveMiscCaps &   D3DPMISCCAPS_LINEPATTERNREP) != 0);
    
    texBorderModeSupport = ((d3dCaps->TextureAddressCaps & D3DPTADDRESSCAPS_BORDER) != 0);
    
    texLerpSupport = ((d3dCaps->TextureOpCaps &    D3DTEXOPCAPS_LERP) != 0);

    canRenderWindowed = true;//((d3dCaps->Caps2 &  D3DCAPS2_CANRENDERWINDOWED) != 0);
    

    maxPrimitiveCount = d3dCaps->MaxPrimitiveCount;
    maxVertexIndex = min(vertexBufferMaxVertexLimit, d3dCaps->MaxVertexIndex);
    
    maxTextureHeight = d3dCaps->MaxTextureHeight;
    maxTextureWidth =  d3dCaps->MaxTextureWidth;
    maxTextureDepth =  d3dCaps->MaxVolumeExtent;
    
    maxActiveLights = d3dCaps->MaxActiveLights;
    maxPointSize = DWORD(d3dCaps->MaxPointSize);
    maxAnisotropy = d3dCaps->MaxAnisotropy;
   	
    maxVertexCount[GEO_TYPE_QUAD_SET] = min(vertexBufferMaxVertexLimit,  maxPrimitiveCount << 1);
    
    // Since index is used, we need to make sure than index range
    // is also support.
    maxVertexCount[GEO_TYPE_QUAD_SET] = min(maxVertexCount[GEO_TYPE_QUAD_SET],   maxVertexIndex);
    
    maxVertexCount[GEO_TYPE_TRI_SET] = min(vertexBufferMaxVertexLimit,   maxPrimitiveCount*3);
    
    maxVertexCount[GEO_TYPE_POINT_SET] = min(vertexBufferMaxVertexLimit,   maxPrimitiveCount);
    
    maxVertexCount[GEO_TYPE_LINE_SET] = min(vertexBufferMaxVertexLimit, maxPrimitiveCount << 1);
    
    maxVertexCount[GEO_TYPE_TRI_STRIP_SET] = min(vertexBufferMaxVertexLimit, maxPrimitiveCount + 2);
    
    maxVertexCount[GEO_TYPE_TRI_FAN_SET] = min(vertexBufferMaxVertexLimit,	 maxPrimitiveCount + 2);
    
    maxVertexCount[GEO_TYPE_LINE_STRIP_SET] = min(vertexBufferMaxVertexLimit,
    maxPrimitiveCount +1);
    maxVertexCount[GEO_TYPE_INDEXED_QUAD_SET] = maxVertexCount[GEO_TYPE_QUAD_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_SET] = maxVertexCount[GEO_TYPE_TRI_SET];
    maxVertexCount[GEO_TYPE_INDEXED_POINT_SET] = maxVertexCount[GEO_TYPE_POINT_SET];
    maxVertexCount[GEO_TYPE_INDEXED_LINE_SET] = maxVertexCount[GEO_TYPE_LINE_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_STRIP_SET] = maxVertexCount[GEO_TYPE_TRI_STRIP_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_FAN_SET] = maxVertexCount[GEO_TYPE_TRI_FAN_SET];
    maxVertexCount[GEO_TYPE_INDEXED_LINE_STRIP_SET] = maxVertexCount[GEO_TYPE_LINE_STRIP_SET];
    

    if ( (d3dCaps->PresentationIntervals & D3DPRESENT_INTERVAL_IMMEDIATE) != 0)
        supportRasterPresImmediate = true;
    else
        supportRasterPresImmediate = false;
    
    if (((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGTABLE) != 0) &&
	((d3dCaps->RasterCaps & D3DPRASTERCAPS_WFOG) != 0)) {
        // use pixel w-fog
        fogMode = D3DRS_FOGTABLEMODE;
        rangeFogEnable = false;
    }
    else if (((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGVERTEX) != 0) &&
	     ((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGRANGE) != 0)) {
	// use vertex range based fog
	fogMode = D3DRS_FOGVERTEXMODE;
	rangeFogEnable = true;
    }
    else if ((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGTABLE) != 0) {
	// use pixel z-fog
	fogMode = D3DRS_FOGTABLEMODE;
	rangeFogEnable = false;
    }
    else if (D3DPRASTERCAPS_FOGVERTEX) {
	// use vertex z-fog
	fogMode = D3DRS_FOGVERTEXMODE;
	rangeFogEnable = false;
    }
    else {
	if (debug) {
	    printf("[Java 3D] Fog not support in this device !\n");
	}
    } 
    
    texMask = 0;
    if(supportNPOT){
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_NON_POWER_OF_TWO;
    }

    if((d3dCaps->Caps2 & D3DCAPS2_CANAUTOGENMIPMAP) != 0) {
	texMask |= javax_media_j3d_Canvas3D_TEXTURE_AUTO_MIPMAP_GENERATION;
    }
    
    if ((d3dCaps->TextureCaps & D3DPTEXTURECAPS_VOLUMEMAP) &&
    (maxTextureDepth > 0)) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_3D;
    }
    
    if (d3dCaps->TextureCaps & D3DPTEXTURECAPS_CUBEMAP) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_CUBE_MAP;
    }
    
    if (maxTextureUnitStageSupport > 1) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_MULTI_TEXTURE;
    }
    

    if (d3dCaps->TextureOpCaps & D3DTEXOPCAPS_DOTPRODUCT3) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE_DOT3;
    }
    
    if (d3dCaps->TextureOpCaps & D3DTEXOPCAPS_SUBTRACT) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE_SUBTRACT;
    }
    
    if (d3dCaps->TextureOpCaps & D3DTEXOPCAPS_LERP) {
        texMask |= (javax_media_j3d_Canvas3D_TEXTURE_LERP|
        javax_media_j3d_Canvas3D_TEXTURE_COMBINE);
    } else if (d3dCaps->TextureOpCaps &
    (D3DTEXOPCAPS_DOTPRODUCT3|D3DTEXOPCAPS_SUBTRACT|
    D3DTEXOPCAPS_MODULATE|D3DTEXOPCAPS_ADD|
    D3DTEXOPCAPS_ADDSIGNED)) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_COMBINE;
    }
    
    
    if (maxAnisotropy > 1) {
        texMask |= javax_media_j3d_Canvas3D_TEXTURE_ANISOTROPIC_FILTER;
    }

}

BOOL D3dDeviceInfo::supportAntialiasing() {
    return (multiSampleSupport != 0);
}


void D3dDeviceInfo::findDepthStencilFormat(int minZDepth, int minZDepthStencil)
{ 	
    // Fix to Issue 226 : D3D - fail on stress test for the creation and destruction of Canvases
    //sanity check  of stencil and depth
    minZDepthStencil = min(minZDepthStencil, 8);
    minZDepth = min(minZDepth, 32);
    
    depthStencilFormat = D3DFMT_UNKNOWN;
   
    for (int i=0; i < D3DDEPTHFORMATSIZE; i++) {
        //printf("\ndepthFormatSupport %s, %b",getPixelFormatName(d3dDepthFormat[i]), depthFormatSupport[i]);
        if (depthFormatSupport[i]){
            // prefer one with stencil buffer, follow by D3DFMT_D16_LOCKABLE,
            // printf("\n ZDepth %d, Stencil %d ",d3dDepthTable[i],d3dStencilDepthTable[i]);
            if (d3dDepthTable[i] >= minZDepth && d3dStencilDepthTable[i] >= minZDepthStencil ) {
                depthStencilFormat = (D3DFORMAT) d3dDepthFormat[i];
                break;
            }
        }//if
    }// for
    // if none suitable found    
}


D3DMULTISAMPLE_TYPE D3dDeviceInfo::getBestMultiSampleType()
{
    DWORD bitmask = 0;
    UINT i;

    // Fix to Issue 226 : D3D - fail on stress test for the creation and destruction of Canvases
     // start with 4 and up, if none found, try 3 and 2
    for (i=4; i <= 16; i++) {
	bitmask = (1 << i);
	if (multiSampleSupport & bitmask) {
	    return (D3DMULTISAMPLE_TYPE) i;
	}
    }

    // try 3 and 2  
    for (i=3; i >= 2; i--) {
	bitmask = (1 << i);
	if (multiSampleSupport & bitmask) {
	    return (D3DMULTISAMPLE_TYPE) i;
	}
    }

    // Should not happen
    return D3DMULTISAMPLE_NONE;
}

int D3dDeviceInfo::getTextureFeaturesMask()
{
    return texMask;
}
