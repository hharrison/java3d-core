/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
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

VOID D3dDeviceInfo::setCaps(D3DCAPS8 *d3dCaps) {

    if (deviceType == D3DDEVTYPE_HAL) {
	isHardware = true;
	isHardwareTnL = (d3dCaps->DevCaps &
			 D3DDEVCAPS_HWTRANSFORMANDLIGHT);
    } else {
	isHardware = false;
	isHardwareTnL = false;
    }
    maxTextureBlendStages = d3dCaps->MaxTextureBlendStages;
    maxSimultaneousTextures = d3dCaps->MaxSimultaneousTextures;
    maxTextureUnitStageSupport = min(maxTextureBlendStages,
				     maxSimultaneousTextures);
    supportMipmap = ((d3dCaps->TextureCaps & 
		      D3DPTEXTURECAPS_MIPMAP) != 0);
    texturePow2Only =  ((d3dCaps->TextureCaps & 
			 D3DPTEXTURECAPS_POW2) != 0);
    textureSquareOnly = ((d3dCaps->TextureCaps & 
			  D3DPTEXTURECAPS_SQUAREONLY) != 0);
    linePatternSupport = ((d3dCaps->PrimitiveMiscCaps & 
			   D3DPMISCCAPS_LINEPATTERNREP) != 0);
    texBorderModeSupport = ((d3dCaps->TextureAddressCaps &
			     D3DPTADDRESSCAPS_BORDER) != 0);
    texLerpSupport = ((d3dCaps->TextureOpCaps & 
		       D3DTEXOPCAPS_LERP) != 0);
    canRenderWindowed = ((d3dCaps->Caps2 & 
			  D3DCAPS2_CANRENDERWINDOWED) != 0);
    maxPrimitiveCount = d3dCaps->MaxPrimitiveCount;
    maxVertexIndex = min(vertexBufferMaxVertexLimit, 
			 d3dCaps->MaxVertexIndex);
    maxTextureHeight = d3dCaps->MaxTextureHeight;
    maxTextureWidth =  d3dCaps->MaxTextureWidth;
    maxTextureDepth =  d3dCaps->MaxVolumeExtent;

    maxActiveLights = d3dCaps->MaxActiveLights;
    maxPointSize = d3dCaps->MaxPointSize;
    maxAnisotropy = d3dCaps->MaxAnisotropy;

    maxVertexCount[GEO_TYPE_QUAD_SET] = min(vertexBufferMaxVertexLimit, 
					    maxPrimitiveCount << 1);

    // Since index is used, we need to make sure than index range
    // is also support.
    maxVertexCount[GEO_TYPE_QUAD_SET] = min(maxVertexCount[GEO_TYPE_QUAD_SET],
					    maxVertexIndex);

    maxVertexCount[GEO_TYPE_TRI_SET] = min(vertexBufferMaxVertexLimit,
					    maxPrimitiveCount*3);
    maxVertexCount[GEO_TYPE_POINT_SET] = min(vertexBufferMaxVertexLimit,
					     maxPrimitiveCount);
    maxVertexCount[GEO_TYPE_LINE_SET] = min(vertexBufferMaxVertexLimit,
					    maxPrimitiveCount << 1);
    maxVertexCount[GEO_TYPE_TRI_STRIP_SET] = min(vertexBufferMaxVertexLimit,
						 maxPrimitiveCount + 2);
    maxVertexCount[GEO_TYPE_TRI_FAN_SET] = min(vertexBufferMaxVertexLimit,
						 maxPrimitiveCount + 2);
    maxVertexCount[GEO_TYPE_LINE_STRIP_SET] = min(vertexBufferMaxVertexLimit,
						 maxPrimitiveCount +1);
    maxVertexCount[GEO_TYPE_INDEXED_QUAD_SET] = maxVertexCount[GEO_TYPE_QUAD_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_SET] = maxVertexCount[GEO_TYPE_TRI_SET];
    maxVertexCount[GEO_TYPE_INDEXED_POINT_SET] = maxVertexCount[GEO_TYPE_POINT_SET];
    maxVertexCount[GEO_TYPE_INDEXED_LINE_SET] = maxVertexCount[GEO_TYPE_LINE_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_STRIP_SET] = maxVertexCount[GEO_TYPE_TRI_STRIP_SET];
    maxVertexCount[GEO_TYPE_INDEXED_TRI_FAN_SET] = maxVertexCount[GEO_TYPE_TRI_FAN_SET];
    maxVertexCount[GEO_TYPE_INDEXED_LINE_STRIP_SET] = maxVertexCount[GEO_TYPE_LINE_STRIP_SET];


    if (((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGTABLE) != 0) &&
	((d3dCaps->RasterCaps & D3DPRASTERCAPS_WFOG) != 0)) {
	// use pixel w-fog
	fogMode = D3DRS_FOGTABLEMODE;
	rangeFogEnable = false;
    } else if (((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGVERTEX) != 0) &&
	       ((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGRANGE) != 0)) {
	// use vertex range based fog
	fogMode = D3DRS_FOGVERTEXMODE;
	rangeFogEnable = true;
    } else if ((d3dCaps->RasterCaps & D3DPRASTERCAPS_FOGTABLE) != 0) {
	// use pixel z-fog
	fogMode = D3DRS_FOGTABLEMODE;
	rangeFogEnable = false;
    } else  if (D3DPRASTERCAPS_FOGVERTEX) {
        // use vertex z-fog
	fogMode = D3DRS_FOGVERTEXMODE;
	rangeFogEnable = false;
    } else {
	if (debug) {
	    printf("[Java 3D] Fog not support in this device !\n");
	}
    }


    texMask = 0;

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


void D3dDeviceInfo::findDepthStencilFormat(int minZDepth)
{
    depthStencilFormat = D3DFMT_UNKNOWN;
    for (int i=0; i < D3DDEPTHFORMATSIZE; i++) {
	if (depthFormatSupport[i]) {
	    // prefer one with stencil buffer, follow by D3DFMT_D16_LOCKABLE, 
	    if (d3dDepthTable[i] >= minZDepth) {
		depthStencilFormat = (D3DFORMAT) d3dDepthFormat[i];
		break;
	    }
	}
    }
}


D3DMULTISAMPLE_TYPE D3dDeviceInfo::getBestMultiSampleType()
{
    DWORD bitmask = 0;
    UINT i;

    // start with 4, if none found, try 3 and 2
    for (i=4; i < 16; i++) {
	bitmask = (1 << i);
	if (multiSampleSupport & bitmask) {
	    return (D3DMULTISAMPLE_TYPE) i;
	}
    }

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
