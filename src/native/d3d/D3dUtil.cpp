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

#include "StdAfx.h"

HANDLE hSema = CreateSemaphore(NULL, 1, 1, "Java3d_Ctx");
HANDLE imageSema = CreateSemaphore(NULL, 1, 1,
				   "Java3d_ImageComponent2DLock");
HANDLE backgroundSema = CreateSemaphore(NULL, 1, 1,
					"Java3d_ImageBackgroundLock");
HANDLE geometrySema = CreateSemaphore(NULL, 1, 1,
				      "Java3d_GeometryArrayLock");
HANDLE surfaceListSema = CreateSemaphore(NULL, 1, 1, "Java3d_SurfaceListLock");
			
BOOL firstError = true;
BOOL firstWarning = true;
BOOL debug;


vector<void *> freePointerList0;
vector<void *> freePointerList1;
BOOL useFreePointerList0 = true;

char *D3dErrorMessage[] = {
    "Can't found 3D Driver !",
    "Current display driver did not support renderer inside window. Now switch to full screen mode...",
    "DirectX 8.0 or above is required for this version of Java3D.",
    "Your graphics card did not support >= 16 bit color mode which Java 3D required.",
    "Please switch your display mode to at least 16 bit color depth.",
    "No compatible device found, please switch to other display mode and try again !",
    "Fail to create hardware D3D Device, switch to use reference rasterizer.",
    "Fail to create reference rasterizer 3D Device.",
    "Fail to set Viewport",
    "Fail to get attach back buffer",
    "256 color mode not support !",
    "Out of memory !",
    "Unknown 3D device specified in property j3d.d3ddevice, please use either \"reference\" or \"hardware\".",
    "Graphics card did not support Hardware acceleration",
    "Graphics card did not support Transform and light hardware acceleration",
    "No Stencil buffer found in current display mode. DecalGroup may not work correctly.",
    "Can't found a valid texture format, please try to use reference mode",
    "Fail to create offscreen image for background",
    "Fail to create Vertex Buffer",
    "Fail to Reset() D3D device, try Recreate device again.",
    "No compatible depth buffer found in your system, please switch to other display mode or try reference rasterizer.",
    "Depth buffer with the required bit depth is not support, please try the default.",
    "Fail to lock Vertex Buffer",
    "Fail to create Vertex Buffer",
    "Fail to create Index Buffer",
    "Fail to lock Index Buffer"
};

D3DTRANSFORMSTATETYPE transformState[] = {
                       D3DTS_TEXTURE0,
                       D3DTS_TEXTURE1,
                       D3DTS_TEXTURE2,
                       D3DTS_TEXTURE3,
                       D3DTS_TEXTURE4,
                       D3DTS_TEXTURE5,
                       D3DTS_TEXTURE6,
                       D3DTS_TEXTURE7};

#define D3DFORMATTABLESIZE 40

D3DFORMAT d3dFormatTable[] = {
    D3DFMT_UNKNOWN,
    D3DFMT_R8G8B8,
    D3DFMT_A8R8G8B8,
    D3DFMT_X8R8G8B8,
    D3DFMT_R5G6B5,
    D3DFMT_X1R5G5B5,
    D3DFMT_A1R5G5B5,
    D3DFMT_A4R4G4B4,
    D3DFMT_R3G3B2,
    D3DFMT_A8,
    D3DFMT_A8R3G3B2,
    D3DFMT_X4R4G4B4,
    D3DFMT_A8P8,
    D3DFMT_P8,
    D3DFMT_L8,
    D3DFMT_A8L8,
    D3DFMT_A4L4,
    D3DFMT_V8U8,
    D3DFMT_L6V5U5,
    D3DFMT_X8L8V8U8,
    D3DFMT_Q8W8V8U8,
    D3DFMT_V16U16,
    D3DFMT_W11V11U10,
    D3DFMT_UYVY,
    D3DFMT_YUY2,
    D3DFMT_DXT1,
    D3DFMT_DXT2,
    D3DFMT_DXT3,
    D3DFMT_DXT4,
    D3DFMT_DXT5,
    D3DFMT_D16_LOCKABLE,
    D3DFMT_D32,
    D3DFMT_D15S1,
    D3DFMT_D24S8,
    D3DFMT_D16,
    D3DFMT_D24X8,
    D3DFMT_D24X4S4,
    D3DFMT_VERTEXDATA,
    D3DFMT_INDEX16,
    D3DFMT_INDEX32
};

char *d3dFormatTableChar[] = {
    "D3DFMT_UNKNOWN",
    "D3DFMT_R8G8B8", 
    "D3DFMT_A8R8G8B8",
    "D3DFMT_X8R8G8B8",
    "D3DFMT_R5G6B5",
    "D3DFMT_X1R5G5B5",
    "D3DFMT_A1R5G5B5",
    "D3DFMT_A4R4G4B4",
    "D3DFMT_R3G3B2",
    "D3DFMT_A8",
    "D3DFMT_A8R3G3B2",
    "D3DFMT_X4R4G4B4",
    "D3DFMT_A8P8",
    "D3DFMT_P8",
    "D3DFMT_L8",
    "D3DFMT_A8L8",
    "D3DFMT_A4L4",
    "D3DFMT_V8U8",
    "D3DFMT_L6V5U5",
    "D3DFMT_X8L8V8U8",
    "D3DFMT_Q8W8V8U8",
    "D3DFMT_V16U16",
    "D3DFMT_W11V11U10",
    "D3DFMT_UYVY",
    "D3DFMT_YUY2",
    "D3DFMT_DXT1",
    "D3DFMT_DXT2",
    "D3DFMT_DXT3",
    "D3DFMT_DXT4",
    "D3DFMT_DXT5",
    "D3DFMT_D16_LOCKABLE",
    "D3DFMT_D32",
    "D3DFMT_D15S1",
    "D3DFMT_D24S8",
    "D3DFMT_D16",
    "D3DFMT_D24X8",
    "D3DFMT_D24X4S4",
    "D3DFMT_VERTEXDATA",
    "D3DFMT_INDEX16",
    "D3DFMT_INDEX32"
};

char* multipleSampleTypeTable[] = {
    "D3DMULTISAMPLE_NONE",
    "D3DMULTISAMPLE_UNKNOWN"
    "D3DMULTISAMPLE_2_SAMPLES",
    "D3DMULTISAMPLE_3_SAMPLES",
    "D3DMULTISAMPLE_4_SAMPLES",
    "D3DMULTISAMPLE_5_SAMPLES",
    "D3DMULTISAMPLE_6_SAMPLES",
    "D3DMULTISAMPLE_7_SAMPLES",
    "D3DMULTISAMPLE_8_SAMPLES",
    "D3DMULTISAMPLE_9_SAMPLES",
    "D3DMULTISAMPLE_10_SAMPLES",
    "D3DMULTISAMPLE_11_SAMPLES",
    "D3DMULTISAMPLE_12_SAMPLES",
    "D3DMULTISAMPLE_13_SAMPLES",
    "D3DMULTISAMPLE_14_SAMPLES",
    "D3DMULTISAMPLE_15_SAMPLES",
    "D3DMULTISAMPLE_16_SAMPLES",
};

char* swapEffectTable[] = {
    "D3DSWAPEFFECT_UNKNOWN",
    "D3DSWAPEFFECT_DISCARD",
    "D3DSWAPEFFECT_FLIP",
    "D3DSWAPEFFECT_COPY",
    "D3DSWAPEFFECT_COPY_VSYNC"
};


// mapping from java enum to d3d enum

D3DCUBEMAP_FACES textureCubeMapFace[] = {
    D3DCUBEMAP_FACE_POSITIVE_X,
    D3DCUBEMAP_FACE_NEGATIVE_X,
    D3DCUBEMAP_FACE_POSITIVE_Y,
    D3DCUBEMAP_FACE_NEGATIVE_Y,
    D3DCUBEMAP_FACE_NEGATIVE_Z,
    D3DCUBEMAP_FACE_POSITIVE_Z,
};

typedef struct _PIXELFORMAT {
    DWORD dwRGBBitCount;
    DWORD dwRBitMask;
    DWORD dwGBitMask;    
    DWORD dwBBitMask;    
    DWORD dwRGBAlphaBitMask;    
    BOOL  noAlpha;
} PIXELFORMAT;


typedef struct _DEPTHPIXELFORMAT {
    DWORD dwZBufferBitDepth;
    DWORD dwZBitMask;
} DEPTHPIXELFORMAT;


char *getSwapEffectName(D3DSWAPEFFECT swapEffect) 
{
    int t = (int) swapEffect;
    if ((t < 0) || (t > 4)) {
	return swapEffectTable[0];
    }
    return swapEffectTable[t];
}

char *getMultiSampleName(D3DMULTISAMPLE_TYPE mtype) 
{
    int t = (int) mtype;
    if ((t < 0) || (t > 16)) {
	// UNKNOWN
	return multipleSampleTypeTable[1];
    }
    return multipleSampleTypeTable[t];
}

char* getPixelFormatName(D3DFORMAT f)
{
    for (int i=0; i < D3DFORMATTABLESIZE; i++) {
	if (f == d3dFormatTable[i]) {
	    return d3dFormatTableChar[i];
	}
    } 
    // should not happen
    return d3dFormatTableChar[0];
}

// If there is a new D3DFORMAT, just add it here and
// our copy procedures can handle any format specific
// as bit mask.
VOID computePixelFormat(PIXELFORMAT *ddpf, D3DFORMAT format) 
{
    switch (format) {
        case D3DFMT_R8G8B8:
	    ddpf->dwRGBBitCount = 24;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0x00ff0000;
	    ddpf->dwGBitMask = 0x0000ff00;
	    ddpf->dwBBitMask = 0x000000ff;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_A8R8G8B8: 
	    ddpf->dwRGBBitCount = 32;
	    ddpf->dwRGBAlphaBitMask = 0xff000000;
	    ddpf->dwRBitMask = 0x00ff0000;
	    ddpf->dwGBitMask = 0x0000ff00;
	    ddpf->dwBBitMask = 0x000000ff;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_X8R8G8B8:
	    ddpf->dwRGBBitCount = 32;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0x00ff0000;
	    ddpf->dwGBitMask = 0x0000ff00;
	    ddpf->dwBBitMask = 0x000000ff;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_R5G6B5:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0xf800;
	    ddpf->dwGBitMask = 0x07e0;
	    ddpf->dwBBitMask = 0x001f;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_X1R5G5B5:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0x7c00;
	    ddpf->dwGBitMask = 0x03e0;
	    ddpf->dwBBitMask = 0x001f;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_A1R5G5B5: 
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0x8000;
	    ddpf->dwRBitMask = 0x7c00;
	    ddpf->dwGBitMask = 0x03e0;
	    ddpf->dwBBitMask = 0x001f;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_A4R4G4B4:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0xf000;
	    ddpf->dwRBitMask = 0x0f00;
	    ddpf->dwGBitMask = 0x00f0;
	    ddpf->dwBBitMask = 0x000f;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_X4R4G4B4:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0x0f00;
	    ddpf->dwGBitMask = 0x00f0;
	    ddpf->dwBBitMask = 0x000f;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_R3G3B2:
	    ddpf->dwRGBBitCount = 8;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0xe0;
	    ddpf->dwGBitMask = 0x1c;
	    ddpf->dwBBitMask = 0x03;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_A8R3G3B2:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0xff00;
	    ddpf->dwRBitMask = 0x00e0;
	    ddpf->dwGBitMask = 0x001c;
	    ddpf->dwBBitMask = 0x0003;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_A8:
	    ddpf->dwRGBBitCount = 8;
	    ddpf->dwRGBAlphaBitMask = 0xff;
	    ddpf->dwRBitMask = 0;
	    ddpf->dwGBitMask = 0;
	    ddpf->dwBBitMask = 0;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_L8:
	    ddpf->dwRGBBitCount = 8;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0xff;
	    ddpf->dwGBitMask = 0;
	    ddpf->dwBBitMask = 0;
	    ddpf->noAlpha = true;
	    break;
        case D3DFMT_A8L8:
	    ddpf->dwRGBBitCount = 16;
	    ddpf->dwRGBAlphaBitMask = 0xff00;
	    ddpf->dwRBitMask = 0x00ff;
	    ddpf->dwGBitMask = 0;
	    ddpf->dwBBitMask = 0;
	    ddpf->noAlpha = false;
	    break;
        case D3DFMT_A4L4:
	    ddpf->dwRGBBitCount = 8;
	    ddpf->dwRGBAlphaBitMask = 0xf0;
	    ddpf->dwRBitMask = 0x0f;
	    ddpf->dwGBitMask = 0;
	    ddpf->dwBBitMask = 0;
	    ddpf->noAlpha = false;
	    break;
        default: 
	    printf("Unsupport format %d\n ", format);
	    ddpf->dwRGBBitCount = 8;
	    ddpf->dwRGBAlphaBitMask = 0;
	    ddpf->dwRBitMask = 0;
	    ddpf->dwGBitMask = 0;
	    ddpf->dwBBitMask = 0;
	    ddpf->noAlpha = true;
	    break;
    }

}


/*
 * Right now only format D3DFMT_D16_LOCKABLE 
 * is lockable by application. So can't use
 * with stencil buffer (in DecalGroup) together
 */
VOID computeDepthPixelFormat(DEPTHPIXELFORMAT *ddpf, 
			     D3DFORMAT format) 
{
    switch (format) {
        case D3DFMT_D16_LOCKABLE:
        case D3DFMT_D16:
	    ddpf->dwZBufferBitDepth = 16;
	    ddpf->dwZBitMask = 0xffff;
	    break;
        case D3DFMT_D15S1:
	    ddpf->dwZBufferBitDepth = 16;
	    ddpf->dwZBitMask = 0xfffe;
	    break;	    
        case D3DFMT_D32:
	    ddpf->dwZBufferBitDepth = 32;
	    ddpf->dwZBitMask = 0xffffffff;
	    break;	    
        case D3DFMT_D24S8:
        case D3DFMT_D24X8:
        case D3DFMT_D24X4S4:
	    ddpf->dwZBufferBitDepth = 32;
	    ddpf->dwZBitMask = 0xffffff00;
	    break;	    	    
        default:
	    printf("Unknown depth buffer format %d\n", format);
    }
}


/*
 *  Set the correct D3DTSS_TEXTURETRANSFORMFLAGS
 */
void setTexTransformStageFlag(D3dCtx* d3dCtx,  
			      LPDIRECT3DDEVICE8 device, 
			      int tus, int ts, int genMode) 
{
    /*
     * In case of automatic texture generation, disable
     * texture unit transform stage will cause crash in 
     * reference device mode.
     */
    if ((!d3dCtx->texTransformSet[tus]) &&
	(d3dCtx->texGenMode == TEX_GEN_NONE)) {
	device->SetTextureStageState(tus,
				     D3DTSS_TEXTURETRANSFORMFLAGS,
				     D3DTTFF_DISABLE);
    } else {
	D3DXMATRIX *m;
	
	switch (ts) {
	case 2:
	    // Adjust for 2D texture transform in D3D
	    // 1  0  0 0
	    // 0  1  0 0
	    // du dv 0 0
	    // 0  0  0 0
	    //

	    /*
	     * From DIRECTXDEV@DISCUSS.MICROSOFT.COM:
	     *
	     * The texture transform matrix is funky.  With COUNT=2
	     * and texture coordinates coming from the vertices, you
	     * can't use the stock transformation matrix functions to
	     * generate the matrix without adjusting it before setting
	     * the transform.  Basically in the case of COUNT=2 with
	     * texcoords coming from the vertices, the pipeline uses
	     * the upper 3x3 submatrix of the 4x4 matrix as the
	     * transformation.  So if you were expecting the (u,v)
	     * translation elements to be on the 4th row, these won't
	     * be applied properly in this case.
	     *
	     * What's funky is that if you were using COUNT=2 and
	     * texture coordinates coming in automatically, then it
	     * wants the translation in the 4th row.  I can't decide
	     * yet if this is a poor specification that results in
	     * this matrix weirdness, or if its a bug in the runtime.
	     */
	    if ((genMode != TEX_GEN_AUTO) &&
		!d3dCtx->texTranslateSet[tus]) {
		m = &d3dCtx->texTransform[tus];
		(*m)._31 = (*m)._41;
		(*m)._32 = (*m)._42;
		device->SetTransform((D3DTRANSFORMSTATETYPE) 
				     (D3DTS_TEXTURE0 + tus), m);
		d3dCtx->texTranslateSet[tus] = true;
	    }

	    device->SetTextureStageState(tus,
					 D3DTSS_TEXTURETRANSFORMFLAGS, 
					 D3DTTFF_COUNT2);
	    break;
	case 3:
	    device->SetTextureStageState(tus,
					 D3DTSS_TEXTURETRANSFORMFLAGS, 
					 D3DTTFF_COUNT3);
	    break;
	case 4:
	    if (d3dCtx->texGenMode[tus] == TEX_OBJ_LINEAR) {
		// The texture transform matrix is funky that only the
		// upper 3x3 matrix is used if we are not using
		// automatic texture generation. In case of Object
		// Linear we are need to workaround by doing our 
		// own texture transform when generate texture
		// coordinate.
		device->SetTextureStageState(tus,
					     D3DTSS_TEXTURETRANSFORMFLAGS,
					     D3DTTFF_DISABLE);	 
	    } else {   
		device->SetTextureStageState(tus,
					     D3DTSS_TEXTURETRANSFORMFLAGS, 
					     D3DTTFF_COUNT4|D3DTTFF_PROJECTED);
	    }
	    break;
	default:
	    printf("ERROR texCoordFormat, stage %d, format  %d\n",
		   tus, ts);
	}
    }

}

/*
 * Set the corresponding D3D texture coordinate 
 * mapping mode.
 */
inline int setTextureStage(D3dCtx *d3dCtx,
			   LPDIRECT3DDEVICE8 device, 
			   int mapTexStage,
			   jint texStage)
{
    DWORD mode = 0;
    int genMode = d3dCtx->texGenMode[mapTexStage];

    //    printf("Set TexStage mapTexStage = %d, texStage = %d, genMode = %d\n", 
    //           mapTexStage, texStage, genMode);

    switch (genMode) {
    case TEX_GEN_NONE:
    case TEX_OBJ_LINEAR:
    case TEX_GEN_INVALID:
	// optimize for general case
	device->SetTextureStageState(mapTexStage,
				     D3DTSS_TEXCOORDINDEX, 
				     texStage);
	return genMode;
    case TEX_EYE_LINEAR: 
	mode = D3DTSS_TCI_CAMERASPACEPOSITION;
	break;
    case TEX_SPHERE_MAP:
	mode = D3DTSS_TCI_CAMERASPACEREFLECTIONVECTOR;
	break;
    case TEX_NORMAL_MAP:
	mode = D3DTSS_TCI_CAMERASPACENORMAL;
	break;
    case TEX_REFLECT_MAP:
	mode = D3DTSS_TCI_CAMERASPACEREFLECTIONVECTOR;
	break;
    default:
	// should not happen
	printf("Unknown TexCoordGenMode %d\n", genMode);
	break;
    }
    // Need to OR texStage for Wrapping mode even though
    // there is no texture coordinate defined in that texStage in VB.
    // This also clear the texStage previously set.
    device->SetTextureStageState(mapTexStage,
				 D3DTSS_TEXCOORDINDEX, 
				 mode | texStage);

    return TEX_GEN_AUTO;
}


void getTexWidthHeight(D3dDeviceInfo *deviceInfo, 
		       jint* width, jint *height)
{
    int texWidth, texHeight;
    
    texWidth = *width;
    texHeight = *height;
    

    // Found a texture bigger than width/height
    if (deviceInfo->texturePow2Only) {
	for (texWidth=1; *width > texWidth; texWidth <<= 1);
	for (texHeight=1; *height > texHeight; texHeight <<= 1);
    }


    if (deviceInfo->textureSquareOnly) {
	if (texWidth >= texHeight) {
	    texHeight = texWidth;
	} else {
	    texWidth = texHeight;
	}
    }

    // Check for maximum texture size support by hardware
    if (texWidth > deviceInfo->maxTextureWidth) {
	if (debug) {
	    printf("[Java 3D] Warning: Input texture width %d > maximum texture width %d hardware can support.\n", texWidth, deviceInfo->maxTextureWidth);
	    if (*width != texWidth) {
		printf("Note that width is adjust from %d to %d to reflect texture limitation e.g. POW2, SQAUREONLY in hardware.\n", *width, texWidth);
	    }
	}
	texWidth =  deviceInfo->maxTextureWidth;
    }

    if (texHeight > deviceInfo->maxTextureHeight) {
	if (debug) {
	    printf("[Java 3D] Warning: Input texture height %d > maximum texture height %d hardware can support.\n", texHeight, deviceInfo->maxTextureHeight);
	    if (*height != texHeight) {
		printf("Note that height is adjust from %d to %d to reflect texture limitation e.g. POW2, SQAUREONLY in hardware.\n", *height, texHeight);
	    }
	}
	texHeight = deviceInfo->maxTextureHeight;
    }

    *width = texWidth;
    *height = texHeight;
}

D3DFORMAT getTexFormat(jint internalFormat) {

    switch (internalFormat) {
        case J3D_RGBA:
        case INTENSITY:
	    return D3DFMT_A8R8G8B8;
        case J3D_RGB:
	    return D3DFMT_R8G8B8;
        case LUMINANCE_ALPHA:
	    return D3DFMT_A8L8;
        case ALPHA:
	    return D3DFMT_A8;
        case LUMINANCE:
	    return D3DFMT_L8;
        default:
	    printf("CreateTextureSurface: Unknown internal Format %d \n", internalFormat);
	    return D3DFMT_UNKNOWN;
    } 

}

D3dCtx* findCtx(HWND hwnd)
{
    D3dCtx *ctx = NULL;

    for (D3dCtx **p = d3dCtxList.begin(); p != d3dCtxList.end(); p++) {
	if ((*p)->hwnd == hwnd) {
	    ctx = *p;
	    break;
	}
    }
    return ctx;
}


inline VOID lock() 
{
    if (hSema != NULL) {
	WaitForSingleObject(hSema, INFINITE);
    }
}

inline VOID unlock()
{
    if (hSema != NULL) {
	ReleaseSemaphore(hSema, 1, NULL);
    }
}


inline VOID lockSurfaceList() 
{
    if (surfaceListSema != NULL) {
	WaitForSingleObject(surfaceListSema, INFINITE);
    }
}

inline VOID unlockSurfaceList()
{
    if (surfaceListSema != NULL) {
	ReleaseSemaphore(surfaceListSema, 1, NULL);
    }
}

inline VOID lockBackground()
{
    if (backgroundSema != NULL) {
	WaitForSingleObject(backgroundSema, INFINITE);
    }
}

inline VOID unlockBackground()
{
    if (backgroundSema != NULL) {
	ReleaseSemaphore(backgroundSema, 1, NULL);
    }
}

inline VOID lockImage() 
{
    if (imageSema != NULL) {
	WaitForSingleObject(imageSema, INFINITE);
    }
}

inline VOID unlockImage()
{
    if (imageSema != NULL) {
	ReleaseSemaphore(imageSema, 1, NULL);
    }
}



inline VOID lockGeometry() 
{
    if (geometrySema != NULL) {
	WaitForSingleObject(geometrySema, INFINITE);
    }
}

inline VOID unlockGeometry()
{
    if (geometrySema != NULL) {
	ReleaseSemaphore(geometrySema, 1, NULL);
    }
}

VOID freePointer(void * ptr) 
{
    if (ptr != NULL) {
	lockSurfaceList();
	if (useFreePointerList0) {
	    freePointerList0.push_back(ptr);
	} else {
	    freePointerList1.push_back(ptr);	    
	}
	unlockSurfaceList();
    }
}


char *getErrorMessage(int idx) 
{
    return D3dErrorMessage[idx];
}



HWND getTopWindow(HWND hwnd) 
{
    HWND desktop = GetDesktopWindow();
    HWND parent = GetParent(hwnd);

    while ((parent != NULL) && (parent != desktop)) {
	hwnd = parent;
	parent = GetParent(hwnd);
    }
    return hwnd;
}


DWORD firstBit(DWORD mask) 
{
    int i;

    for (i=0; i < sizeof(DWORD)*8-1; i++) {
	if ((mask & 0x01) > 0) {
	    return i;
	}
	mask >>= 1;
    }

    return i;
}

// create a DirectDraw Texture surface of specific width and height
LPDIRECT3DTEXTURE8 createTextureSurface(D3dCtx *d3dCtx,
					jint numLevels,
					jint internalFormat,
					jint width, jint height) 
{
    LPDIRECT3DTEXTURE8 pTexture;
    D3DFORMAT format;
    HRESULT hr;

    LPDIRECT3DDEVICE8 pDevice = d3dCtx->pDevice;
    D3dDeviceInfo *deviceInfo = d3dCtx->deviceInfo;

    if (!deviceInfo->supportMipmap) {
	numLevels = 1;
    }     

    getTexWidthHeight(deviceInfo, &width, &height); 
    format = getTexFormat(internalFormat);

    // If format not support, the utility function will adjust the
    // calling parameters automatically
    hr = D3DXCreateTexture(d3dCtx->pDevice, width, height,
			   numLevels, 0, format, D3DPOOL_MANAGED,
			   &pTexture);

    if (FAILED(hr)) {
	printf("Fail to create texture surface %dx%d, format %d, level %d : %s\n",
	       width, height, format, numLevels, DXGetErrorString8(hr));
	return NULL;
    }
    
    return pTexture;
}



// create a DirectDraw Texture surface of specific width and height
LPDIRECT3DVOLUMETEXTURE8 createVolumeTexture(D3dCtx *d3dCtx,
					     jint numLevels,
					     jint internalFormat,
					     jint width, 
					     jint height,
					     jint depth) 
{
    LPDIRECT3DVOLUMETEXTURE8 pTexture;
    int texWidth, texHeight, texDepth;
    D3DFORMAT format;
    HRESULT hr;

    LPDIRECT3DDEVICE8 pDevice = d3dCtx->pDevice;
    D3dDeviceInfo *deviceInfo = d3dCtx->deviceInfo;

    texWidth = width;
    texHeight = height;
    texDepth = depth;


    if (!deviceInfo->supportMipmap) {
	numLevels = 1;
    }     


    // Found a texture bigger than width/height
    if (deviceInfo->texturePow2Only) {
	for (texWidth=1; width > texWidth; texWidth <<= 1);
	for (texHeight=1; height > texHeight; texHeight <<= 1);
	for (texDepth=1; depth > texDepth; texDepth <<= 1);
    }


    if (deviceInfo->textureSquareOnly) {
	if (texWidth >= texHeight) {
	    texHeight = texWidth;
	} else {
	    texWidth = texHeight;
	}
	if (texDepth <= texWidth) {
	    texDepth = texWidth;
	} else {
	    texWidth = texHeight = texDepth;
	}
    }

    // Check for maximum texture size support by hardware
    if (texWidth > deviceInfo->maxTextureWidth) {
	if (debug) {
	    printf("[Java 3D] Warning: Input texture width %d > maximum texture width %d hardware can support.\n", texWidth, deviceInfo->maxTextureWidth);
	    if (width != texWidth) {
		printf("Note that width is adjust from %d to %d to reflect texture limitation e.g. POW2, SQAUREONLY in hardware.\n", width, texWidth);
	    }
	}
	texWidth =  deviceInfo->maxTextureWidth;
    }

    if (texHeight > deviceInfo->maxTextureHeight) {
	if (debug) {
	    printf("[Java 3D] Warning: Input texture height %d > maximum texture height %d hardware can support.\n", texHeight, deviceInfo->maxTextureHeight);
	    if (height != texHeight) {
		printf("Note that height is adjust from %d to %d to reflect texture limitation e.g. POW2, SQAUREONLY in hardware.\n", height, texHeight);
	    }
	}
	texHeight = deviceInfo->maxTextureHeight;
    }

    if (texDepth > deviceInfo->maxTextureDepth) {
	if (debug) {
	    printf("[Java 3D] Warning: Input texture depth %d > maximum texture depth %d hardware can support.\n", texDepth, deviceInfo->maxTextureDepth);
	    if (depth != texDepth) {
		printf("Note that depth is adjust from %d to %d to reflect texture limitation e.g. POW2, SQAUREONLY in hardware.\n", depth, texDepth);
	    }
	}
	texDepth = deviceInfo->maxTextureDepth;
    }


    format = getTexFormat(internalFormat);

    // If format not support, the utility function will adjust the
    // calling parameters automatically
    hr = D3DXCreateVolumeTexture(d3dCtx->pDevice, texWidth, texHeight,
				 texDepth, numLevels, 0, format, D3DPOOL_MANAGED,
				 &pTexture);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to create volume texture %dx%dx%d, format %d, level %d : %s\n",
		   texWidth, texHeight, texDepth, format, numLevels,
		   DXGetErrorString8(hr));
	}
	return NULL;
    }
    
    return pTexture;
}


// copy data from DirectDraw surface to memory
// and reverse the Y axis
void copyDataFromSurface(jint internalFormat, 
			 jint xoffset, jint yoffset,
			 jint subWidth, jint subHeight,
			 jbyte *data, 
			 LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;
    HRESULT hr;

    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, D3DLOCK_NOSYSLOCK|
			D3DLOCK_READONLY);

    if (FAILED(hr)) {
	printf("Fail to lock surface: %s\n", DXGetErrorString8(hr));
	return;
    }

    unsigned char *src;
    unsigned char *dst;
    byte b1, b2, b3, b4;
    DWORD mask, t;
    DWORD dstPitch;

    unsigned char *destRow = (unsigned char *) data;
    unsigned char *srcRow = ((unsigned char *) lockedRect.pBits) +
	xoffset*((int) ceil((float) ddpf.dwRGBBitCount/8.0)) + 
	(yoffset*lockedRect.Pitch);
    

    if ((internalFormat == FORMAT_BYTE_RGBA) ||
    	(internalFormat == FORMAT_BYTE_RGB)) {
	dstPitch = subWidth << 2;
	destRow += (subHeight-1)*dstPitch;

	if ((ddpf.dwRGBBitCount == 32) &&
	    (ddpf.dwRBitMask == 0xff0000) &&
	    (ddpf.dwGBitMask == 0xff00) &&
	    (ddpf.dwBBitMask == 0xff)) {
	    // Optimize for the most common case
	    if (ddpf.noAlpha) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			*dst++ = b3;
			*dst++ = b2;
			*dst++ = b1;
			*src++;
			*dst++ = (byte) 0xff;
		    }
		    srcRow += lockedRect.Pitch; 
		    destRow -= dstPitch;
		}
	    } else {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			*dst++ = b3;
			*dst++ = b2;
			*dst++ = b1;
			*dst++ = *src++;
		    }
		    srcRow += lockedRect.Pitch; 
		    destRow -= dstPitch;
		}
	    }
	} else { // handle less common format
	    int rshift = firstBit(ddpf.dwRBitMask) +
		           countBits(ddpf.dwRBitMask) - 8;
	    int gshift = firstBit(ddpf.dwGBitMask) + 
		           countBits(ddpf.dwGBitMask) - 8; 
	    int bshift = firstBit(ddpf.dwBBitMask) +
		           countBits(ddpf.dwBBitMask) - 8; 
	    int ashift = firstBit(ddpf.dwRGBAlphaBitMask) +
		           countBits(ddpf.dwRGBAlphaBitMask) - 8; 

	    if ((ddpf.dwRGBBitCount <= 32) && 
		(ddpf.dwRGBBitCount > 24)) {

		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			b4 = *src++;
			mask = ((b4 << 24) | (b3 << 16)| (b2 << 8) | b1);
			if (rshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwRBitMask) >>
					      rshift);
			} else {
			    t = (mask & ddpf.dwRBitMask) << -rshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}

			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					      gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (bshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwBBitMask) >>
					     bshift); 
			} else {
			    t = (mask & ddpf.dwBBitMask) << -bshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 24) && 
		       (ddpf.dwRGBBitCount > 16)) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			mask = ((b3 << 16) | (b2 << 8) | b1);
			if (rshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwRBitMask) >>
					     rshift);
			} else {
			    t = (mask & ddpf.dwRBitMask) << -rshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) <<-gshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (bshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwBBitMask) >>
					     bshift);
			} else {
			    t = (mask & ddpf.dwBBitMask) << -bshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 16) && 
		       (ddpf.dwRGBBitCount > 8)) {

		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			mask = ((b2 << 8) | b1);
			if (rshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwRBitMask) >>
					     rshift);
			} else {
			    t = (mask & ddpf.dwRBitMask) << -rshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (bshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwBBitMask) >>
					     bshift); 
			} else {
			    t = (mask & ddpf.dwBBitMask) << -bshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}

			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if (ddpf.dwRGBBitCount <= 8) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			mask = *src++;
			if (rshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwRBitMask) >>
					     rshift);
			} else {
			    t = (mask & ddpf.dwRBitMask) << -rshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (bshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwBBitMask) >>
					     bshift); 
			} else {
			    t = (mask & ddpf.dwBBitMask) << -bshift;
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    }
	}  
    } else if (internalFormat == FORMAT_BYTE_LA) {
	int gshift = firstBit(ddpf.dwGBitMask) + 
	    countBits(ddpf.dwGBitMask) - 8; 
	int ashift = firstBit(ddpf.dwRGBAlphaBitMask) +
	    countBits(ddpf.dwRGBAlphaBitMask) - 8; 
	dstPitch = subWidth << 1;
	destRow += (subHeight-1)*dstPitch;

	if ((ddpf.dwRGBBitCount == 32) &&
	    (ddpf.dwRBitMask == 0xff0000) &&
	    (ddpf.dwGBitMask == 0xff00) &&
	    (ddpf.dwBBitMask == 0xff)) {
	    // Optimize for the most common case
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    b1 = *src++;
		    b2 = *src++;
		    b3 = *src++;
		    *dst++ = b2;
		    if (ddpf.noAlpha) {
			*dst++ = (byte) 0xff;
			*src++;
		    } else {
			*dst++ = *src++;
		    }
		}
		srcRow += lockedRect.Pitch; 
		destRow -= dstPitch;
	    }
	} else { // handle less common format
	    int gshift = firstBit(ddpf.dwGBitMask) + 
		           countBits(ddpf.dwGBitMask) - 8; 
	    if ((ddpf.dwRGBBitCount <= 32) && 
		(ddpf.dwRGBBitCount > 24)) {

		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			b4 = *src++;
			mask = ((b4 << 24) | (b3 << 16)| (b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 24) && 
		       (ddpf.dwRGBBitCount > 16)) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			mask = ((b3 << 16) | (b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 16) && 
		       (ddpf.dwRGBBitCount > 8)) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			mask = ((b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if (ddpf.dwRGBBitCount <= 8) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			mask = *src++;
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
			if (ddpf.noAlpha) {
			    *dst++ = (byte) 0xff;
			} else {
			    if (ashift >= 0) {
				*dst++ = (byte) ((mask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
			    } else {
				t = (mask & ddpf.dwRGBAlphaBitMask) <<-ashift;
				*dst++ = (t <= 0xff ? (byte) t : 0xff);
			    }
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    }
	}  

    } else if (internalFormat == FORMAT_BYTE_GRAY) {
	int gshift = firstBit(ddpf.dwGBitMask) + 
	    countBits(ddpf.dwGBitMask) - 8; 
	dstPitch = subWidth;
	destRow += (subHeight-1)*dstPitch;

	if ((ddpf.dwRGBBitCount == 32) &&
	    (ddpf.dwRBitMask == 0xff0000) &&
	    (ddpf.dwGBitMask == 0xff00) &&
	    (ddpf.dwBBitMask == 0xff)) {
	    // Optimize for the most common case
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    b1 = *src++;
		    b2 = *src++;
		    b3 = *src++;
		    *dst++ = b2;
		    *src++;
		}
		srcRow += lockedRect.Pitch; 
		destRow -= dstPitch;
	    }
	} else { // handle less common format
	    int gshift = firstBit(ddpf.dwGBitMask) + 
		           countBits(ddpf.dwGBitMask) - 8; 
	    if ((ddpf.dwRGBBitCount <= 32) && 
		(ddpf.dwRGBBitCount > 24)) {

		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			b4 = *src++;
			mask = ((b4 << 24) | (b3 << 16)| (b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 24) && 
		       (ddpf.dwRGBBitCount > 16)) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			b3 = *src++;
			mask = ((b3 << 16) | (b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if ((ddpf.dwRGBBitCount <= 16) && 
		       (ddpf.dwRGBBitCount > 8)) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			b1 = *src++;
			b2 = *src++;
			mask = ((b2 << 8) | b1);
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    } else if (ddpf.dwRGBBitCount <= 8) {
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			mask = *src++;
			if (gshift >= 0) {
			    *dst++ = (byte) ((mask & ddpf.dwGBitMask) >>
					     gshift);
			} else {
			    t = (mask & ddpf.dwGBitMask) << -gshift;			    
			    *dst++ = (t <= 0xff ? (byte) t : 0xff);
			}
		    }
		    srcRow += lockedRect.Pitch;
		    destRow -= dstPitch;
		}
	    }
	}  

    } else {
	// FORMAT_USHORT_GRAY
	printf("[Java 3D] copyDataFromSurface: Format not support %d\n", internalFormat);
    }


    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock surface: %s\n", DXGetErrorString8(hr));
	return;
    }

}


void copyDataToSurfaceABGR(jint internalFormat, 
			   PIXELFORMAT *ddpf,
			   unsigned char* pRect,
			   DWORD rectPitch,
			   jbyte *data,
			   jint xoffset, jint yoffset,
			   DWORD xlimit, DWORD ylimit, 
			   jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, a, l;
    const DWORD srcPitch = subWidth*4;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Optimize for most common case
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    *dst++ = *src++;
		    *dst++ = *src++;
		    *dst++ = *src++;
		    *dst++ = a;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = (*src++) >> 4; // discard the lower 4 bit
		    b = (*src++) >> 4;
		    g = (*src++) >> 4;
		    r = (*src++) >> 4;
		    *dst++ = (g << 4) | b;
		    *dst++ = (a << 4) | r;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | (a << ashift);
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			*dst++ = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    *dst++ = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}


void copyDataToSurfaceBGR(jint internalFormat, 
			  PIXELFORMAT *ddpf,
			  unsigned char* pRect,
			  DWORD rectPitch,
			  jbyte *data,
			  jint xoffset, jint yoffset,
			  DWORD xlimit, DWORD ylimit, 
			  jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, l;
    const DWORD srcPitch = subWidth*3;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;


    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Optimize for most common case
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *dst++ = *src++;
		    *dst++ = *src++;
		    *dst++ = *src++;
		    *dst++ = 0xff;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }

	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    b = (*src++) >> 4;
		    g = (*src++) >> 4;
		    r = (*src++) >> 4;
		    *dst++ = (g << 4) | b;
		    *dst++ = 0xf0 | r;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			*dst++ = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | ddpf->dwRGBAlphaBitMask);	  
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  |ddpf->dwRGBAlphaBitMask);	   
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	byte m1 = (byte) (ddpf->dwRGBAlphaBitMask & 0xff);
	byte m2 = (byte) ((ddpf->dwRGBAlphaBitMask >> 8) & 0xff);
	byte m3 = (byte) ((ddpf->dwRGBAlphaBitMask >> 16) & 0xff);
	byte m4 = (byte) ((ddpf->dwRGBAlphaBitMask >> 24) & 0xff);

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		    *dst++ = m3;
		    *dst++ = m4;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		    *dst++ = m3;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;  
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;  
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     ddpf->dwRGBAlphaBitMask);  
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}

/*
 * Same as copyDataToSurfaceRGBA()
 * but the pixel is written in the destination buffer
 * from right to left. This is used for CubeMapping.
 */
void copyDataToSurfaceRGBARev(jint internalFormat, 
			      PIXELFORMAT *ddpf,
			      unsigned char* pRect,
			      DWORD rectPitch,
			      jbyte *data,
			      jint xoffset, jint yoffset,
			      DWORD xlimit, DWORD ylimit, 
			      jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, a, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth*4;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = *src++;
		    g = *src++;
		    b = *src++;
		    *dst-- = *src++;
		    *dst-- = r;
		    *dst-- = g;
		    *dst-- = b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = (*src++) >> 4; // discard the lower 4 bit
		    g = (*src++) >> 4;
		    b = (*src++) >> 4;
		    a = (*src++) >> 4;
		    *dst-- = (a << 4) | r;
		    *dst-- = (g << 4) | b;

		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;
	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);

		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3 - 1);

		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit-1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			*dst-- = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) -1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);

		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += ((xlimit*3) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += ((xlimit*3) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst-- = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += ((xlimit*3)-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}



void copyDataToSurfaceABGRRev(jint internalFormat, 
			      PIXELFORMAT *ddpf,
			      unsigned char* pRect,
			      DWORD rectPitch,
			      jbyte *data,
			      jint xoffset, jint yoffset,
			      DWORD xlimit, DWORD ylimit, 
			      jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, a, l;
    const DWORD srcPitch = subWidth*4;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Optimize for most common case
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    b = *src++;
		    g = *src++;
		    r = *src++;
		    *dst-- = a;
		    *dst-- = r;
		    *dst-- = g;
		    *dst-- = b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = (*src++) >> 4; // discard the lower 4 bit
		    b = (*src++) >> 4;
		    g = (*src++) >> 4;
		    r = (*src++) >> 4;
		    *dst-- = (a << 4) | r;
		    *dst-- = (g << 4) | b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3-1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit-1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			*dst-- = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);

		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    mask = (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xlimit;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    src += 3;
		    *dst-- = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3-1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }

}


void copyDataToSurfaceBGRRev(jint internalFormat, 
			     PIXELFORMAT *ddpf,
			     unsigned char* pRect,
			     DWORD rectPitch,
			     jbyte *data,
			     jint xoffset, jint yoffset,
			     DWORD xlimit, DWORD ylimit, 
			     jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, l;
    const DWORD srcPitch = subWidth*3;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;


    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Optimize for most common case
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    b = *src++;
		    g = *src++;
		    r = *src++;
		    *dst-- = 0xff;
		    *dst-- = r;
		    *dst-- = g;
		    *dst-- = b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }

	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    b = (*src++) >> 4;
		    g = (*src++) >> 4;
		    r = (*src++) >> 4;
		    *dst-- = 0xf0 | r;
		    *dst-- = (g << 4) | b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3-1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			*dst-- = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | ddpf->dwRGBAlphaBitMask);	  
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;	  
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  |ddpf->dwRGBAlphaBitMask);	   
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	byte m1 = (byte) (ddpf->dwRGBAlphaBitMask & 0xff);
	byte m2 = (byte) ((ddpf->dwRGBAlphaBitMask >> 8) & 0xff);
	byte m3 = (byte) ((ddpf->dwRGBAlphaBitMask >> 16) & 0xff);
	byte m4 = (byte) ((ddpf->dwRGBAlphaBitMask >> 24) & 0xff);

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m4;
		    *dst-- = m3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);

		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;  
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;  
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src += 3;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     ddpf->dwRGBAlphaBitMask);  
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}


void copyDataToSurfaceRGBRev(jint internalFormat, 
			     PIXELFORMAT *ddpf,
			     unsigned char* pRect,
			     DWORD rectPitch,
			     jbyte *data,
			     jint xoffset, jint yoffset,
			     DWORD xlimit, DWORD ylimit, 
			     jint subWidth) 
{
   unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, l;
    const DWORD srcPitch = subWidth*3;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	destRow += ((xlimit << 2) - 1);
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = *src++;
		    g = *src++;
		    b = *src++;
		    *dst-- = (byte) 0xff;
		    *dst-- = r;
		    *dst-- = g;
		    *dst-- = b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = (*src++) >> 4; // discard the lower 4 bit
		    g = (*src++) >> 4;
		    b = (*src++) >> 4;
		    *dst-- = 0xf0 | r;
		    *dst-- = (g << 4) | b;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	   
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3 - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | ddpf->dwRGBAlphaBitMask;	   
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			*dst-- = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) |  ddpf->dwRGBAlphaBitMask);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | ddpf->dwRGBAlphaBitMask);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	byte m1 = (byte) (ddpf->dwRGBAlphaBitMask & 0xff);
	byte m2 = (byte) ((ddpf->dwRGBAlphaBitMask >> 8) & 0xff);
	byte m3 = (byte) ((ddpf->dwRGBAlphaBitMask >> 16) & 0xff);
	byte m4 = (byte) ((ddpf->dwRGBAlphaBitMask >> 24) & 0xff);

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m4;
		    *dst-- = m3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3- 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m2;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst-- = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
        } else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     ddpf->dwRGBAlphaBitMask);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}

void copyDataToSurfaceLARev(jint internalFormat, 
			    PIXELFORMAT *ddpf,
			    unsigned char* pRect,
			    DWORD rectPitch,
			    jbyte *data,
			    jint xoffset, jint yoffset,
			    DWORD xlimit, DWORD ylimit, 
			    jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD a, r, g, b, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth*2;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    *dst-- = *src++;
		    *dst-- = l;
		    *dst-- = l;
		    *dst-- = l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = (*src++) >> 4; // discard the lower 4 bit
		    a = (*src++) >> 4;
		    *dst-- = (a << 4) | l;
		    *dst-- = (l << 4) | l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask);
	    int bDiscard = 8-countBits(ddpf->dwBBitMask);
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);
	    int bshift = firstBit(ddpf->dwBBitMask);
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3 - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			*dst-- = (byte) ((l << rshift) | (l << gshift) | 
					 (l << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {

	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);

		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst-- = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}

void copyDataToSurfaceGrayRev(jint internalFormat, 
			      PIXELFORMAT *ddpf,
			      unsigned char* pRect,
			      DWORD rectPitch,
			      jbyte *data,
			      jint xoffset, jint yoffset,
			      DWORD xlimit, DWORD ylimit, 
			      jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD a, r, g, b, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    *dst-- = 0xff;
		    *dst-- = l;
		    *dst-- = l;
		    *dst-- = l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = (*src++) >> 4; // discard the lower 4 bit
		    *dst-- = 0xf0 | l;
		    *dst-- = (l << 4) | l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask);
	    int bDiscard = 8-countBits(ddpf->dwBBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);
	    int bshift = firstBit(ddpf->dwBBitMask);
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += ((xlimit << 2) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst-- = (byte) ((mask >> 24) & 0xff);
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xlimit*3 - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst-- = (byte) ((mask >> 16) & 0xff);
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte)  (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += ((xlimit << 1) - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst-- = (byte) ((mask >> 8) & 0xff);
			*dst-- = (byte) (mask & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += (xlimit - 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			*dst-- = (byte) ((l << rshift) | (l << gshift) | 
					 (l << bshift) | ddpf->dwRGBAlphaBitMask); 
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {

	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst-- = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst-- = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}


void copyDataToSurfaceGrayRev(jint internalFormat, 
			      PIXELFORMAT *ddpf,
			      unsigned char* pRect,
			      DWORD rectPitch,
			      jshort *data,
			      jint xoffset, jint yoffset,
			      DWORD xlimit, DWORD ylimit, 
			      jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD  a, r, g, b, l;
    DWORD srcPitch = subWidth << 1;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;


    if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst-- = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += ((xlimit << 2) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 24) & 0xff);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xlimit*3 - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 16) & 0xff);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += ((xlimit << 1) - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst-- = (byte) ((mask >> 8) & 0xff);
		    *dst-- = (byte) (mask & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += (xlimit - 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst-- =  (byte) ((r << rshift) | 
				      (g << gshift) | 
				      (b << bshift) |
				      (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n", internalFormat);
    } 
}



/*
 *  Copy data to Texture memory surface *pRect
 *  with  pitch = rectPitch 
 *  Note that rectPitch >= surface width since
 *  D3D may allocate extra width in texture memory
 *  for other purpose or for alignment. Addional
 *  offset = (xoffset, yoffset) is added to copy 
 *  data in texture memory. 
 *
 * The source image has width = subWidth and
 * pointer *data.
 *  
 *
 */
void copyDataToSurfaceRGBA(jint internalFormat, 
			   PIXELFORMAT *ddpf,
			   unsigned char* pRect,
			   DWORD rectPitch,
			   jbyte *data,
			   jint xoffset, jint yoffset,
			   DWORD xlimit, DWORD ylimit, 
			   jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, a, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth*4;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = *src++;
		    g = *src++;
		    b = *src++;
		    *dst++ = b;
		    *dst++ = g;
		    *dst++ = r;
		    *dst++ = *src++;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = (*src++) >> 4; // discard the lower 4 bit
		    g = (*src++) >> 4;
		    b = (*src++) >> 4;
		    a = (*src++) >> 4;
		    *dst++ = (g << 4) | b;
		    *dst++ = (a << 4) | r;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | (a << ashift);
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			*dst++ = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {

	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst++ = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}


void copyDataToSurfaceRGB(jint internalFormat, 
			  PIXELFORMAT *ddpf,
			  unsigned char* pRect,
			  DWORD rectPitch,
			  jbyte *data,
			  jint xoffset, jint yoffset,
			  DWORD xlimit, DWORD ylimit, 
			  jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, l;
    const DWORD srcPitch = subWidth*3;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	destRow += (xoffset << 2);
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = *src++;
		    g = *src++;
		    b = *src++;
		    *dst++ = b;
		    *dst++ = g;
		    *dst++ = r;
		    *dst++ = (byte) 0xff;

		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    r = (*src++) >> 4; // discard the lower 4 bit
		    g = (*src++) >> 4;
		    b = (*src++) >> 4;
		    *dst++ = (g << 4) | b;
		    *dst++ = 0xf0 | r;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask); 
	    int bDiscard = 8-countBits(ddpf->dwBBitMask); 
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);	  
	    int bshift = firstBit(ddpf->dwBBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;	   
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			       (b << bshift)  | ddpf->dwRGBAlphaBitMask;	   
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			mask = (r << rshift) | (g << gshift) | 
			    (b << bshift)  | ddpf->dwRGBAlphaBitMask;
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    r = (*src++) >> rDiscard;
			} else {
			    r = (*src++) << -rDiscard;
			}
			if (gDiscard >= 0) {
			    g = (*src++) >> gDiscard;
			} else {
			    g = (*src++) >> -gDiscard;
			}
			if (bDiscard >= 0) {
			    b = (*src++) >> bDiscard;
			} else {
			    b = (*src++) >> -bDiscard;
			}
			*dst++ = (byte) ((r << rshift) | (g << gshift) | 
					 (b << bshift) |  ddpf->dwRGBAlphaBitMask);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | ddpf->dwRGBAlphaBitMask);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	byte m1 = (byte) (ddpf->dwRGBAlphaBitMask & 0xff);
	byte m2 = (byte) ((ddpf->dwRGBAlphaBitMask >> 8) & 0xff);
	byte m3 = (byte) ((ddpf->dwRGBAlphaBitMask >> 16) & 0xff);
	byte m4 = (byte) ((ddpf->dwRGBAlphaBitMask >> 24) & 0xff);

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		    *dst++ = m3;
		    *dst++ = m4;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		    *dst++ = m3;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		    *dst++ = m2;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src += 3;
		    *dst++ = m1;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
        } else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | ddpf->dwRGBAlphaBitMask;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    src++;
		    src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     ddpf->dwRGBAlphaBitMask);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}


void copyDataToSurfaceLA(jint internalFormat, 
			 PIXELFORMAT *ddpf,
			 unsigned char* pRect,
			 DWORD rectPitch,
			 jbyte *data,
			 jint xoffset, jint yoffset,
			 DWORD xlimit, DWORD ylimit, 
			 jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD a, r, g, b, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth*2;

    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    *dst++ = l;
		    *dst++ = l;
		    *dst++ = l;
		    *dst++ = *src++;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = (*src++) >> 4; // discard the lower 4 bit
		    a = (*src++) >> 4;
		    *dst++ = (l << 4) | l;
		    *dst++ = (a << 4) | l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask);
	    int bDiscard = 8-countBits(ddpf->dwBBitMask);
	    int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);
	    int bshift = firstBit(ddpf->dwBBitMask);
	    int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | (a << ashift);
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			if (aDiscard >= 0) {
			    a = (*src++) >> aDiscard;
			} else {
			    a = (*src++) >> -aDiscard;
			}
			*dst++ = (byte) ((l << rshift) | (l << gshift) | 
					 (l << bshift) | (a << ashift));
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {

	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst++ = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    src++;
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}

void copyDataToSurfaceGray(jint internalFormat, 
			   PIXELFORMAT *ddpf,
			   unsigned char* pRect,
			   DWORD rectPitch,
			   jbyte *data,
			   jint xoffset, jint yoffset,
			   DWORD xlimit, DWORD ylimit, 
			   jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD a, r, g, b, l;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;
    const DWORD srcPitch = subWidth;


    if ((internalFormat == J3D_RGBA) ||
	(internalFormat == J3D_RGB)) {
	if ((ddpf->dwRGBBitCount == 32) &&
	    (ddpf->dwRBitMask == 0xff0000) &&
	    (ddpf->dwGBitMask == 0xff00) &&
	    (ddpf->dwBBitMask == 0xff)) {
	    // Most common case
	    // Note that format of destination is ARGB, which
	    // in PC format are BGRA, so we can't directly
	    // copy a row using CopyMemory()
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    *dst++ = l;
		    *dst++ = l;
		    *dst++ = l;
		    *dst++ = 0xff;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount == 16) &&
		   (ddpf->dwRBitMask == 0xf00) &&
		   (ddpf->dwGBitMask == 0xf0) &&
		   (ddpf->dwBBitMask == 0xf)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = (*src++) >> 4; // discard the lower 4 bit
		    *dst++ = (l << 4) | l;
		    *dst++ = 0xf0 | l;
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else { // handle less common (even weird) format
	    int rDiscard = 8-countBits(ddpf->dwRBitMask);
	    int gDiscard = 8-countBits(ddpf->dwGBitMask);
	    int bDiscard = 8-countBits(ddpf->dwBBitMask);
	    int rshift = firstBit(ddpf->dwRBitMask);
	    int gshift = firstBit(ddpf->dwGBitMask);
	    int bshift = firstBit(ddpf->dwBBitMask);
	    DWORD mask;

	    if ((ddpf->dwRGBBitCount <= 32) && 
		(ddpf->dwRGBBitCount > 24)) {
		destRow += (xoffset << 2);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 24) &&
		       (ddpf->dwRGBBitCount > 16)) {
		destRow += (xoffset*3);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst++ = (byte)  (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if ((ddpf->dwRGBBitCount <= 16) &&
		       (ddpf->dwRGBBitCount > 8)) {
		destRow += (xoffset << 1);
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			mask = (l << rshift) | (l << gshift) | 
			       (l << bshift) | ddpf->dwRGBAlphaBitMask;
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else if (ddpf->dwRGBBitCount <= 8) {
		destRow += xoffset;
		for (int i=yoffset; i < ylimit; i++) {
		    src = srcRow;
		    dst = destRow;
		    for (int j=xoffset; j < xlimit; j++) {
			if (rDiscard >= 0) {
			    l = (*src++) >> rDiscard;
			} else {
			    l = (*src++) << -rDiscard;
			}
			*dst++ = (byte) ((l << rshift) | (l << gshift) | 
					 (l << bshift) | ddpf->dwRGBAlphaBitMask); 
		    }
		    srcRow += srcPitch;
		    destRow += rectPitch; 
		}
	    } else { 
		// should not happen, RGBBitCount > 32. Even DirectX 
		// RGB mask can't address it.
		printf("Texture memory with RGBBitCount = %d not support. \n", 
		       ddpf->dwRGBBitCount);
	    }
	}
    } else if (internalFormat == LUMINANCE_ALPHA) {

	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
		  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	   	  	   (b << bshift)  | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;			
		    }
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | (g << gshift) | 
			             (b << bshift)  | (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst++ = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;
	
	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
	 		   (b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst++ = (byte) ((r << rshift) | 
				     (g << gshift) | 
				     (b << bshift) |
				     (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n",  internalFormat);
    }
}



/*
 * Copy data from memory to DirectDraw surface
 *
 * Source image with WIDTH = tilew, the subimage with
 * dimension (subWidth, subHeight) is copy with 
 * offset = (imgXOffset, imgYOffset) from the start
 * pointer *data.
 *
 * Destination frame buffer is copy with 
 * offset = (xoffset, yoffset)
 *
 */
void copyDataToSurface(jint storedFormat,
		       jint internalFormat, 
		       jint xoffset, jint yoffset,
		       jint imgXOffset, jint imgYOffset,
		       jint subWidth, jint subHeight,
		       jint tilew, jbyte *data, 
		       LPDIRECT3DTEXTURE8 surf,
		       jint level) 
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }
    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    // It is possible when texture is not a power of 2 or
    // square only texture is required in hardware. In these
    // case the hardware memory buffer may smaller than the
    // texture pass in.

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(level, &lockedRect, NULL, 0);


    if (FAILED(hr)) {
	printf("Fail to lock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
    int offset = tilew*imgYOffset + imgXOffset;
    switch (storedFormat) {
        case  FORMAT_BYTE_RGBA :
	    // This is the one we use when byReference = false
	    copyDataToSurfaceRGBA(internalFormat, &ddpf, 
				  (unsigned char *) lockedRect.pBits,
				  lockedRect.Pitch,
				  data + (offset << 2), 
				  xoffset, yoffset, 
				  xlimit, ylimit, tilew);
	    break;
        case FORMAT_BYTE_RGB:
	    copyDataToSurfaceRGB(internalFormat, &ddpf, 
				 (unsigned char *) lockedRect.pBits,
				 lockedRect.Pitch,
				 data + 3*offset, 
				 xoffset, yoffset, 
				 xlimit, ylimit, tilew);
	    break;
        case FORMAT_BYTE_ABGR:
	    copyDataToSurfaceABGR(internalFormat, &ddpf, 
				  (unsigned char *) lockedRect.pBits,
				  lockedRect.Pitch,
				  data + (offset << 2), 
				  xoffset, yoffset, 
				  xlimit, ylimit, tilew);
	    break;
        case FORMAT_BYTE_BGR:
	    copyDataToSurfaceBGR(internalFormat, &ddpf, 
				 (unsigned char *) lockedRect.pBits,
				 lockedRect.Pitch,
				 data + 3*offset, 
				 xoffset, yoffset, 
				 xlimit, ylimit, tilew);
	    break;
        case FORMAT_BYTE_LA:
	    copyDataToSurfaceLA(internalFormat, &ddpf, 
				(unsigned char *) lockedRect.pBits,
				lockedRect.Pitch,
				data + (offset << 1), 
				xoffset, yoffset, 
				xlimit, ylimit, tilew);
	    break;
        case FORMAT_BYTE_GRAY:
	    copyDataToSurfaceGray(internalFormat, &ddpf,
				  (unsigned char *) lockedRect.pBits,
				  lockedRect.Pitch,
				  data + offset, 
				  xoffset, yoffset, 
				  xlimit, ylimit, tilew);
	    break;
        default: // should not happen
	    printf("[Java 3D] StoredFormat %d, internalFormat %d not support !\n",
		   storedFormat, internalFormat);
    }

    hr = surf->UnlockRect(level);
    if (FAILED(hr)) {
	printf("Fail to unlock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}



void copyDataToSurfaceGray(jint internalFormat, 
			   PIXELFORMAT *ddpf,
			   unsigned char* pRect,
			   DWORD rectPitch,
			   jshort *data,
			   jint xoffset, jint yoffset,
			   DWORD xlimit, DWORD ylimit, 
			   jint subWidth) 
{
    unsigned char *src;
    unsigned char *dst;
    DWORD  a, r, g, b, l;
    DWORD srcPitch = subWidth << 1;
    unsigned char *srcRow = (unsigned char *) data;
    unsigned char *destRow = pRect + rectPitch*yoffset;


    if (internalFormat == ALPHA) {	
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;

	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = a << ashift;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    mask = (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    if (aDiscard >= 0) {
			a = (*src++) >> aDiscard;
		    } else {
			a = (*src++) << -aDiscard;
		    }
		    *dst++ = (byte) (a << ashift);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else if ((internalFormat == LUMINANCE) ||
	       (internalFormat == INTENSITY)) {
	int rDiscard = 8-countBits(ddpf->dwRBitMask);
	int gDiscard = 8-countBits(ddpf->dwGBitMask);
	int bDiscard = 8-countBits(ddpf->dwBBitMask);
	int aDiscard = 8-countBits(ddpf->dwRGBAlphaBitMask);
	int rshift = firstBit(ddpf->dwRBitMask);
	int gshift = firstBit(ddpf->dwGBitMask);
	int bshift = firstBit(ddpf->dwBBitMask);
	int ashift = firstBit(ddpf->dwRGBAlphaBitMask);	  
	DWORD mask;


	if ((ddpf->dwRGBBitCount <= 32) && 
	    (ddpf->dwRGBBitCount > 24)) {
	    destRow += (xoffset << 2);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 24) &&
		   (ddpf->dwRGBBitCount > 16)) {
	    destRow += (xoffset*3);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if ((ddpf->dwRGBBitCount <= 16) &&
		   (ddpf->dwRGBBitCount > 8)) {
	    destRow += (xoffset << 1);
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    mask = (r << rshift) | (g << gshift) | 
			(b << bshift) | (a << ashift);
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else if (ddpf->dwRGBBitCount <= 8) {
	    destRow += xoffset;
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    *src++;   // discard lower order byte
		    l = *src++;
		    if (rDiscard >= 0) {
			r = l >> rDiscard;
		    } else {
			r = l << -rDiscard;
		    }
		    if (gDiscard >= 0) {
			g = l >> gDiscard;
		    } else {
			g = l << -gDiscard;
		    }
		    if (bDiscard >= 0) {
			b = l >> bDiscard;
		    } else {
			b = l << -bDiscard;
		    }
		    if (aDiscard >= 0) {
			a = l >> aDiscard;
		    } else {
			a = l << -aDiscard;
		    }
		    *dst++ =  (byte) ((r << rshift) | 
				      (g << gshift) | 
				      (b << bshift) |
				      (a << ashift));
		}
		srcRow += srcPitch;
		destRow += rectPitch; 
	    }
	} else {
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf->dwRGBBitCount);
	}
    } else {
	printf("Texture format %d not support.\n", internalFormat);
    } 
}


// copy the texture data to DirectDraw surface
void copyDataToSurface(jint storedFormat,
		       jint internalFormat,
		       jint xoffset, jint yoffset,
		       jint imgXOffset, jint imgYOffset,
		       jint subWidth, jint subHeight,
		       jint tilew, jshort *data,
		       LPDIRECT3DTEXTURE8 surf,
		       jint level)
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }
    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(level, &lockedRect, NULL, 0);

    if (FAILED(hr)) {
	printf("Fail to lock surface: %s\n", DXGetErrorString8(hr));
	return;
    }

    int offset = tilew*imgYOffset + imgXOffset;

    copyDataToSurfaceGray(internalFormat, &ddpf, 
			  (unsigned char *) lockedRect.pBits,
			  lockedRect.Pitch,
			  data + (offset << 1), 
			  xoffset, yoffset, 
			  xlimit, ylimit, tilew);
    
    hr = surf->UnlockRect(level);
    if (FAILED(hr)) {
	printf("Fail to unlock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


// copy data from DirectDraw depth surface to memory
// and reverse the Y axis
void copyDepthFromSurface(jint xoffset, jint yoffset,
			  jint subWidth, jint subHeight,
			  jint *data, 
			  LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }
    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, D3DLOCK_READONLY);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }

    DWORD b1, b2, b3, b4;
    DWORD mask;
    jint *destRow = data;
    jint *dst;
    unsigned char *src;
    unsigned char *srcRow = ((unsigned char *) lockedRect.pBits) +
	xoffset*((int) ceil((float) ddpf.dwZBufferBitDepth/8.0)) + 
	(yoffset*lockedRect.Pitch);

    int zshift = firstBit(ddpf.dwZBitMask);

    destRow += (subHeight-1)*subWidth;

    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		b3 = *src++;
		b4 = *src++;
		mask = (b4 << 24) | (b3 << 16) | 
		       (b2 << 8) | b1;
		*dst++ =  (mask & ddpf.dwZBitMask) >> zshift;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		b3 = *src++;
		mask = (b3 << 16) | (b2 << 8) | b1;
		*dst++ =  (mask & ddpf.dwZBitMask) >> zshift;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		mask = (b2 << 8) | b1;
		*dst++ =  (mask & ddpf.dwZBitMask) >> zshift;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		*dst++ =  (*src++ & ddpf.dwZBitMask) >> zshift;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else {
	// This is not support by D3D 8 either
	printf("[Java 3D] %d bit Z  buffer not support !\n", 
	       ddpf.dwZBufferBitDepth);
    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


// copy data from DirectDraw depth surface to memory
// and reverse the Y axis
void copyDepthFromSurface(jint xoffset, jint yoffset,
			  jint subWidth, jint subHeight,
			  jfloat *data, 
			  LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, D3DLOCK_READONLY);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }

    DWORD b1, b2, b3, b4;
    DWORD mask;
    jfloat *destRow = data;
    jfloat *dst;
    unsigned char *src;
    unsigned char *srcRow = ((unsigned char *) lockedRect.pBits) +
	xoffset*((int) ceil((float) ddpf.dwZBufferBitDepth/8.0)) + 
	(yoffset*lockedRect.Pitch);

    int zshift = firstBit(ddpf.dwZBitMask);
    float maxdepth = 1 << ddpf.dwZBufferBitDepth;

    destRow += (subHeight-1)*subWidth;

    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		b3 = *src++;
		b4 = *src++;
		mask = (b4 << 24) | (b3 << 16) | 
		       (b2 << 8) | b1;
		*dst++ =  (((mask & ddpf.dwZBitMask) >>
				    zshift))/ maxdepth;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		b3 = *src++;
		mask = (b3 << 16) | (b2 << 8) | b1;
		*dst++ =  ((mask & ddpf.dwZBitMask) >>
			   zshift)/ maxdepth;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		b1 = *src++;
		b2 = *src++;
		mask = (b2 << 8) | b1;
		*dst++ =  ((mask & ddpf.dwZBitMask) >>
			   zshift)/ maxdepth;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		*dst++ =  ((*src++ & ddpf.dwZBitMask) >>
			   zshift)/ maxdepth;
	    }
	    srcRow += lockedRect.Pitch;
	    destRow -= subWidth;
	}
    } else {
	// This is not support by D3D 8 either
	printf("[Java 3D] %d bit Z  buffer not support !\n", 
	       ddpf.dwZBufferBitDepth);
    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


// copy data to DirectDraw depth surface from memory
// and reverse the Y axis
void copyDepthToSurfaceAlways(jint dst_xoffset, jint dst_yoffset,
			      jint src_xoffset, jint src_yoffset,
			      jint subWidth, jint subHeight,
			      jint src_width, jint src_height,
			      jint *data, 
			      LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);

    if ((dst_xoffset >= width) || (dst_yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(dst_xoffset + subWidth, width);
    DWORD ylimit = min(dst_yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, 0);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }
    jint *src;
    jint *srcRow = data + src_xoffset + 
	(src_yoffset + subHeight-1)*src_width;
    unsigned char *dst;
    unsigned char *destRow = ((unsigned char *) lockedRect.pBits) +
	                    dst_xoffset + dst_yoffset*lockedRect.Pitch;

    int zshift = firstBit(ddpf.dwZBitMask);
    DWORD mask;
    int maxValue = ddpf.dwZBitMask >> zshift;


    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = *src++;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
		*dst++ = (byte) ((mask >> 16) & 0xff);
		*dst++ = (byte) ((mask >> 24) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = *src++;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
		*dst++ = (byte) ((mask >> 16) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = *src++;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	byte bmask =   (byte) (ddpf.dwZBitMask & 0xff);
	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = *src++;
		if (mask < maxValue) {
		    *dst++ = (byte) ((mask << zshift) & 0xff);
		} else {
		    *dst++ = bmask;
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}

    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }

}


// copy data to DirectDraw depth surface from memory
// and reverse the Y axis
void copyDepthToSurfaceAlways(jint dst_xoffset, jint dst_yoffset,
			      jint src_xoffset, jint src_yoffset,
			      jint subWidth, jint subHeight,
			      jint src_width, jint src_height,
			      jfloat *data, 
			      LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);


    if ((dst_xoffset >= width) || (dst_yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(dst_xoffset + subWidth, width);
    DWORD ylimit = min(dst_yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, 9);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }
    jfloat *src;
    jfloat *srcRow = data + src_xoffset + 
	(src_yoffset + subHeight-1)*src_width;
    unsigned char *dst;
    unsigned char *destRow = ((unsigned char *) lockedRect.pBits) +
	                    dst_xoffset + dst_yoffset*lockedRect.Pitch;

    int zshift = firstBit(ddpf.dwZBitMask);
    DWORD mask;
    int maxValue = ddpf.dwZBitMask >> zshift;
    float maxdepth = 1 << ddpf.dwZBufferBitDepth;

    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
		*dst++ = (byte) ((mask >> 16) & 0xff);
		*dst++ = (byte) ((mask >> 24) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
		*dst++ = (byte) ((mask >> 16) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < maxValue) {
		    mask = mask << zshift; 
		} else {
		    mask = ddpf.dwZBitMask;
		}
		*dst++ = (byte) (mask & 0xff);
		*dst++ = (byte) ((mask >> 8) & 0xff);
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	byte bmask =   (byte) (ddpf.dwZBitMask & 0xff);
	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < maxValue) {
		    *dst++ = (byte) ((mask << zshift) & 0xff);
		} else {
		    *dst++ = bmask;
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}

    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }

}

// copy data to DirectDraw depth surface from memory
// and reverse the Y axis with Z test D3DCMP_LESS
void copyDepthToSurfaceCmp(jint dst_xoffset, jint dst_yoffset,
			   jint src_xoffset, jint src_yoffset,
			   jint subWidth, jint subHeight,
			   jint src_width, jint src_height,
			   jint *data, 
			   LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);


    if ((dst_xoffset >= width) || (dst_yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(dst_xoffset + subWidth, width);
    DWORD ylimit = min(dst_yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, 0);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }

    jint *src;
    jint *srcRow = data + src_xoffset + 
	(src_yoffset + subHeight-1)*src_width;
    unsigned char *dst;
    unsigned char *destRow = ((unsigned char *) lockedRect.pBits) +
	                    dst_xoffset + dst_yoffset*lockedRect.Pitch;


    int zshift = firstBit(ddpf.dwZBitMask);
    DWORD mask;
    DWORD b1, b2, b3, b4;
    DWORD zmask;
    int maxValue = ddpf.dwZBitMask >> zshift;


    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		b3 = *dst++;
		b4 = *dst++;
		zmask = (b4 << 24) | (b3 << 16) | 
		       (b2 << 8) | b1;
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = *src++;
		if (mask < zmask) { 
		    // z depth test pass
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 4;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		} 
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		b3 = *dst++;
		zmask = (b3 << 16) | (b2 << 8) | b1;		
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = *src++;
		if (mask < zmask) { 		
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 3;		    
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		zmask = (b2 << 8) | b1;		
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = *src++;
		if (mask < zmask) { 				
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 2;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	byte bmask =   (byte) (ddpf.dwZBitMask & 0xff);
	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		zmask =  (*dst++ & ddpf.dwZBitMask) >> zshift;
		mask = *src++;
		if (mask < zmask) { 				
		    dst--;
		    if (mask < maxValue) {
			*dst++ = (byte) ((mask << zshift) & 0xff);
		    } else {
			*dst++ = bmask;
		    }
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}

    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }

}


// copy data to DirectDraw depth surface from memory
// and reverse the Y axis with Z test D3DCMP_LESS
void copyDepthToSurfaceCmp(jint dst_xoffset, jint dst_yoffset,
			   jint src_xoffset, jint src_yoffset,
			   jint subWidth, jint subHeight,
			   jint src_width, jint src_height,
			   jfloat *data, 
			   LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    DEPTHPIXELFORMAT ddpf;
    D3DLOCKED_RECT lockedRect; 
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computeDepthPixelFormat(&ddpf, ddsd.Format);


    if ((dst_xoffset >= width) || (dst_yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(dst_xoffset + subWidth, width);
    DWORD ylimit = min(dst_yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, 0);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to lock depth surface: %s\n", DXGetErrorString8(hr));
	}
	return;
    }
    jfloat *src;
    jfloat *srcRow = data + src_xoffset + 
	(src_yoffset + subHeight-1)*src_width;
    unsigned char *dst;
    unsigned char *destRow = ((unsigned char *) lockedRect.pBits) +
	                    dst_xoffset + dst_yoffset*lockedRect.Pitch;

    int zshift = firstBit(ddpf.dwZBitMask);
    DWORD mask;
    DWORD b1, b2, b3, b4;
    DWORD zmask;
    int maxValue = ddpf.dwZBitMask >> zshift;
    float maxdepth = 1 << ddpf.dwZBufferBitDepth;

    if ((ddpf.dwZBufferBitDepth <= 32) &&
	(ddpf.dwZBufferBitDepth > 24)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		b3 = *dst++;
		b4 = *dst++;
		zmask = (b4 << 24) | (b3 << 16) | 
		       (b2 << 8) | b1;
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < zmask) { 
		    // z depth test pass
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 4;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		    *dst++ = (byte) ((mask >> 24) & 0xff);
		} 
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 24) &&
	       (ddpf.dwZBufferBitDepth > 16)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		b3 = *dst++;
		zmask = (b3 << 16) | (b2 << 8) | b1;		
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < zmask) { 		
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 3;		    
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		    *dst++ = (byte) ((mask >> 16) & 0xff);
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if ((ddpf.dwZBufferBitDepth <= 16) &&
	       (ddpf.dwZBufferBitDepth > 8)) {

	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		b1 = *dst++;
		b2 = *dst++;
		zmask = (b2 << 8) | b1;		
		zmask =  (zmask & ddpf.dwZBitMask) >> zshift;
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < zmask) { 				
		    if (mask < maxValue) {
			mask = mask << zshift; 
		    } else {
			mask = ddpf.dwZBitMask;
		    }
		    dst -= 2;
		    *dst++ = (byte) (mask & 0xff);
		    *dst++ = (byte) ((mask >> 8) & 0xff);
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}
    } else if (ddpf.dwZBufferBitDepth <= 8) {
	byte bmask =   (byte) (ddpf.dwZBitMask & 0xff);
	for (int i=dst_yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=dst_xoffset; j < xlimit; j++) {
		zmask =  (*dst++ & ddpf.dwZBitMask) >> zshift;
		mask = (DWORD) (*src++)*maxdepth;
		if (mask < zmask) { 				
		    dst--;
		    if (mask < maxValue) {
			*dst++ = (byte) ((mask << zshift) & 0xff);
		    } else {
			*dst++ = bmask;
		    }
		}
	    }
	    srcRow -= src_width;
	    destRow += lockedRect.Pitch;
	}

    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock depth surface: %s\n", DXGetErrorString8(hr));
	return;
    }

}

// copy data to DirectDraw depth surface from memory
// and reverse the Y axis
void copyDepthToSurface(D3dCtx *d3dCtx,
			LPDIRECT3DDEVICE8 device,
			jint dst_xoffset, jint dst_yoffset,
			jint src_xoffset, jint src_yoffset,
			jint subWidth, jint subHeight,
			jint src_width, jint src_height,
			jint *data, 
			LPDIRECT3DSURFACE8 surf) 
{

    if (!d3dCtx->zWriteEnable) {
	return;
    }

    if (!d3dCtx->zEnable) {
	copyDepthToSurfaceAlways(dst_xoffset, dst_yoffset,
				 src_xoffset, src_yoffset,
				 subWidth, subHeight,
				 src_width, src_height,
				 data, surf);
    } else {
	// Currently ZFUNC must be D3DCMP_LESS 
	copyDepthToSurfaceCmp(dst_xoffset, dst_yoffset,
			      src_xoffset, src_yoffset,
			      subWidth, subHeight,
			      src_width, src_height,
			      data, surf);
    }
}


// copy data to DirectDraw depth surface from memory
// and reverse the Y axis
void copyDepthToSurface(D3dCtx *d3dCtx,
			LPDIRECT3DDEVICE8 device,
			jint dst_xoffset, jint dst_yoffset,
			jint src_xoffset, jint src_yoffset,
			jint subWidth, jint subHeight,
			jint src_width, jint src_height,
			jfloat *data, 
			LPDIRECT3DSURFACE8 surf) 
{
    if (!d3dCtx->zWriteEnable) {
	return;
    }

    if (!d3dCtx->zEnable) {
	copyDepthToSurfaceAlways(dst_xoffset, dst_yoffset,
				 src_xoffset, src_yoffset,
				 subWidth, subHeight,
				 src_width, src_height,
				 data, surf);
    } else {
	// Currently ZFUNC must be D3DCMP_LESS 
	copyDepthToSurfaceCmp(dst_xoffset, dst_yoffset,
			      src_xoffset, src_yoffset,
			      subWidth, subHeight,
			      src_width, src_height,
			      data, surf);
    }
}

// composite data from memory to DirectDraw surface
void compositeDataToSurface(jint px, jint py,
			    jint xoffset, jint yoffset,
			    jint subWidth, jint subHeight,
			    jint dataWidth, 
			    jbyte *data, 
			    LPDIRECT3DSURFACE8 surf) 
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;

    HRESULT hr;

    if (surf == NULL) {
	return;
    }

    surf->GetDesc(&ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(&lockedRect, NULL, 0);

    if (FAILED(hr)) {
	printf("Fail to lock rendering surface: %s\n",  DXGetErrorString8(hr));
	return;
    }

    unsigned char *src;
    unsigned char *dst;
    DWORD r, g, b, a;
    DWORD srcPitch;
    int offset = (xoffset + yoffset*dataWidth) << 2;
    unsigned char *srcRow = (unsigned char *) data + offset;
    unsigned char *destRow = ((unsigned char *) lockedRect.pBits) + 
	((px + xoffset) << 2) + (py + yoffset)*lockedRect.Pitch; 
    unsigned char a2;
    float inv = 1.0f/255.0f;
    ddpf.noAlpha = true;

    if (ddpf.dwRGBAlphaBitMask != 0) {
	ddpf.noAlpha = false;
    }
	
    srcPitch = dataWidth << 2;

    if ((ddpf.dwRGBBitCount == 32) &&
	(ddpf.dwRBitMask == 0xff0000) &&
	(ddpf.dwGBitMask == 0xff00) &&
	(ddpf.dwBBitMask == 0xff)) {
	// Most common case
	for (int i=yoffset; i < ylimit; i++) {
	    src = srcRow;
	    dst = destRow;
	    for (int j=xoffset; j < xlimit; j++) {
		a = *src++;
		b = *src++;
		g = *src++;
		r = *src++;

		if (a != 0) {
		    if (a == 0xff) {
			*dst++ = b;
			*dst++ = g;
			*dst++ = r;
			*dst++ = 0xff;
		    } else {
			a2 = 255-a;
			*dst++ = (*dst * a2 + b * a)*inv;
			*dst++ = (*dst * a2 + g * a)*inv;
			*dst++ = (*dst * a2 + r * a)*inv;
			if (ddpf.noAlpha) {
			    *dst++ = a;
			} else {
			    *dst++ = (*dst * a2 + a * a)*inv;
			}
		    }
		 } else {
		     dst += 4;
		 }
	    }
	    srcRow += srcPitch;
	    destRow += lockedRect.Pitch; 
	}
    } else { // handle less common (even weird) format
	int rshift = firstBit(ddpf.dwRBitMask) +
	    countBits(ddpf.dwRBitMask) - 8;
	int gshift = firstBit(ddpf.dwGBitMask) + 
	    countBits(ddpf.dwGBitMask) - 8; 
	int bshift = firstBit(ddpf.dwBBitMask) +
	    countBits(ddpf.dwBBitMask) - 8; 
	int ashift = firstBit(ddpf.dwRGBAlphaBitMask) +
	    countBits(ddpf.dwRGBAlphaBitMask) - 8; 

	DWORD mask, dmask;
	DWORD dr, dg, db, da;

	if ((ddpf.dwRGBBitCount <= 32) && 
	    (ddpf.dwRGBBitCount > 24)) {
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    b = *src++;
		    g = *src++;
		    r = *src++;

		    if (a != 0) {
			if (a != 0xff) {
			    dmask = (*(dst+3) << 24) |
				(*(dst+2) << 16) |
				(*(dst+1) << 8) | 
				*dst;
			    if (rshift >= 0) {
				dr = (byte) ((dmask & ddpf.dwRBitMask) >>
					     rshift);
			    } else {
				dr = (byte) ((dmask & ddpf.dwRBitMask) <<
					     -rshift);
			    }
			    if (gshift >= 0) {
				dg = (byte) ((dmask & ddpf.dwGBitMask) >>
					     gshift);
			    } else {
				dg = (byte) ((dmask & ddpf.dwGBitMask) <<
					     -gshift);
			    }
			    if (bshift >= 0) {
				db = (byte) ((dmask & ddpf.dwBBitMask) >>
					     bshift);
			    } else {
				db = (byte) ((dmask & ddpf.dwBBitMask) <<
					     -bshift);
			    }
			    a2 = 255 - a;
			    if (!ddpf.noAlpha) {
				if (ashift >= 0) {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
				} else {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) <<
						 -ashift);
				}
				a = (da * a2 + a * a)*inv;
			    }

			    g = (dg * a2 + g * a)*inv;
			    b = (db * a2 + b * a)*inv;
			    r = (dr * a2 + r * a)*inv;
			}
			if (rshift >= 0) {
			    mask = (r << rshift) & ddpf.dwRBitMask;
			} else {
			    mask = (r >> -rshift) & ddpf.dwRBitMask;
			}
			if (gshift >= 0) {
			    mask |= (g << gshift) & ddpf.dwGBitMask;
			} else {
			    mask |= (g >> -gshift) & ddpf.dwGBitMask;
			}
			if (bshift >= 0) {
			    mask |= (b << bshift) & ddpf.dwBBitMask;
			} else {
			    mask |= (b >> -bshift) & ddpf.dwBBitMask;
			}
			if (!ddpf.noAlpha) {
			    if (ashift >= 0) {
				mask |= (a << ashift) & ddpf.dwRGBAlphaBitMask;
			    } else {
				mask |= (a >> -ashift) & ddpf.dwRGBAlphaBitMask;
			    }
			}
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
			*dst++ = (byte) ((mask >> 24) & 0xff);
		    } else {
			dst += 4;
		    }
		}
		srcRow += srcPitch;
		destRow += lockedRect.Pitch; 
	    }
	} else if ((ddpf.dwRGBBitCount <= 24) &&
		   (ddpf.dwRGBBitCount > 16)) {
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    b = *src++;
		    g = *src++;
		    r = *src++;
		    if (a != 0) {
			if (a != 0xff) {
			    dmask = (*(dst+2) << 16) |
				(*(dst+1) << 8) |
				*dst;
			    if (rshift >= 0) {
				dr = (byte) ((dmask & ddpf.dwRBitMask) >>
					     rshift);
			    } else {
				dr = (byte) ((dmask & ddpf.dwRBitMask) <<
					     -rshift);
			    }
			    if (gshift >= 0) {
				dg = (byte) ((dmask & ddpf.dwGBitMask) >>
					     gshift);
			    } else {
				dg = (byte) ((dmask & ddpf.dwGBitMask) <<
					     -gshift);
			    }
			    if (bshift >= 0) {
				db = (byte) ((dmask & ddpf.dwBBitMask) >>
					     bshift);
			    } else {
				db = (byte) ((dmask & ddpf.dwBBitMask) <<
					     -bshift);
			    }
			    a2 = 255 - a;
			    if (!ddpf.noAlpha) {
				if (ashift >= 0) {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
				} else {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) <<
						 -ashift);
				}
				a = (da * a2 + a * a)*inv;
			    }
			    g = (dg * a2 + g * a)*inv;
			    b = (db * a2 + b * a)*inv;
			    r = (dr * a2 + r * a)*inv;
			}
			if (rshift >= 0) {
			    mask = (r << rshift) & ddpf.dwRBitMask;
			} else {
			    mask = (r >> -rshift) & ddpf.dwRBitMask;
			}
			if (gshift >= 0) {
			    mask |= (g << gshift) & ddpf.dwGBitMask;
			} else {
			    mask |= (g >> -gshift) & ddpf.dwGBitMask;
			}
			if (bshift >= 0) {
			    mask |= (b << bshift) & ddpf.dwBBitMask;
			} else {
			    mask |= (b >> -bshift) & ddpf.dwBBitMask;
			}
			if (!ddpf.noAlpha) {
			    if (ashift >= 0) {
				mask |= (a << ashift) & ddpf.dwRGBAlphaBitMask;
			    } else {
				mask |= (a >> -ashift) & ddpf.dwRGBAlphaBitMask;
			    }
			}
			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
			*dst++ = (byte) ((mask >> 16) & 0xff);
		    } else {
			dst += 3;
		    }
		}
		srcRow += srcPitch;
		destRow += lockedRect.Pitch; 
	    }
	} else if ((ddpf.dwRGBBitCount <= 16) &&
		   (ddpf.dwRGBBitCount > 8)) {

	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    b = *src++;
		    g = *src++;
		    r = *src++;

		    if (a != 0) {
			if (a != 0xff) {
			    dmask = (*(dst+1) << 8) | *dst;

			    if (rshift >= 0) {
				dr = (byte) ((dmask & ddpf.dwRBitMask) >>
					     rshift);
			    } else {
				dr = (byte) ((dmask & ddpf.dwRBitMask) <<
					     -rshift);
			    }
			    if (gshift >= 0) {
				dg = (byte) ((dmask & ddpf.dwGBitMask) >>
					     gshift);
			    } else {
				dg = (byte) ((dmask & ddpf.dwGBitMask) <<
					     -gshift);
			    }
			    if (bshift >= 0) {
				db = (byte) ((dmask & ddpf.dwBBitMask) >>
					     bshift);
			    } else {
				db = (byte) ((dmask & ddpf.dwBBitMask) <<
					     -bshift);
			    }
			    a2 = 255 - a;

			    if (!ddpf.noAlpha) {
				if (ashift >= 0) {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
				} else {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) <<
						 -ashift);
				}
				a = (da * a2 + a * a)*inv;
			    } 

			    g = (dg * a2 + g * a)*inv;
			    b = (db * a2 + b * a)*inv;
			    r = (dr * a2 + r * a)*inv;
			}

			if (rshift >= 0) {
			    mask = (r << rshift) & ddpf.dwRBitMask;
			} else {
			    mask = (r >> -rshift) & ddpf.dwRBitMask;
			}
			if (gshift >= 0) {
			    mask |= ((g << gshift) & ddpf.dwGBitMask);
			} else {
			    mask |= ((g >> -gshift) & ddpf.dwGBitMask);
			}
			if (bshift >= 0) {
			    mask |= ((b << bshift) & ddpf.dwBBitMask);
			} else {
			    mask |= ((b >> -bshift) & ddpf.dwBBitMask);
			}
			if (!ddpf.noAlpha) {
			    if (ashift >= 0) {
				mask |= ((a << ashift) & ddpf.dwRGBAlphaBitMask);
			    } else {
				mask |= ((a >> -ashift) & ddpf.dwRGBAlphaBitMask);
			    }
			}

			*dst++ = (byte) (mask & 0xff);
			*dst++ = (byte) ((mask >> 8) & 0xff);
		    } else {
			dst += 2;
		    }
		}
		srcRow += srcPitch;
		destRow += lockedRect.Pitch; 
	    }
	} else if (ddpf.dwRGBBitCount <= 8) {
	    for (int i=yoffset; i < ylimit; i++) {
		src = srcRow;
		dst = destRow;
		for (int j=xoffset; j < xlimit; j++) {
		    a = *src++;
		    b = *src++;
		    g = *src++;
		    r = *src++;
		    if (a != 0) {
			if (a != 0xff) {
			    dmask = *dst;
			    if (rshift >= 0) {
				dr = (byte) ((dmask & ddpf.dwRBitMask) >>
					     rshift);
			    } else {
				dr = (byte) ((dmask & ddpf.dwRBitMask) <<
					     -rshift);
			    }
			    if (gshift >= 0) {
				dg = (byte) ((dmask & ddpf.dwGBitMask) >>
					     gshift);
			    } else {
				dg = (byte) ((dmask & ddpf.dwGBitMask) <<
					     -gshift);
			    }
			    if (bshift >= 0) {
				db = (byte) ((dmask & ddpf.dwBBitMask) >>
					     bshift);
			    } else {
				db = (byte) ((dmask & ddpf.dwBBitMask) <<
					     -bshift);
			    }
			    a2 = 255 - a;
			    if (!ddpf.noAlpha) {
				if (ashift >= 0) {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) >>
						 ashift);
				} else {
				    da = (byte) ((dmask & ddpf.dwRGBAlphaBitMask) <<
						 -ashift);
				}
				a = (da * a2 + a * a)*inv;
			    }

			    g = (dg * a2 + g * a)*inv;
			    b = (db * a2 + b * a)*inv;
			    r = (dr * a2 + r * a)*inv;
			}
			if (rshift >= 0) {
			    mask = (r << rshift) & ddpf.dwRBitMask;
			} else {
			    mask = (r >> -rshift) & ddpf.dwRBitMask;
			}
			if (gshift >= 0) {
			    mask |= (g << gshift) & ddpf.dwGBitMask;
			} else {
			    mask |= (g >> -gshift) & ddpf.dwGBitMask;
			}
			if (bshift >= 0) {
			    mask |= (b << bshift) & ddpf.dwBBitMask;
			} else {
			    mask |= (b >> -bshift) & ddpf.dwBBitMask;
			}
			if (!ddpf.noAlpha) {
			    if (ashift >= 0) {
				mask |= (a << ashift) & ddpf.dwRGBAlphaBitMask;
			    } else {
				mask |= (a >> -ashift) & ddpf.dwRGBAlphaBitMask;
			    }
			}
			*dst++ = (byte) (mask & 0xff);
		    } else {
			dst++;
		    }
		}
		srcRow += srcPitch;
		destRow += lockedRect.Pitch; 
	    }
	} else { 
	    // should not happen, RGBBitCount > 32. Even DirectX 
	    // RGB mask can't address it.
	    printf("Texture memory with RGBBitCount = %d not support. \n", 
		   ddpf.dwRGBBitCount);
	}
    }

    hr = surf->UnlockRect();
    if (FAILED(hr)) {
	printf("Fail to unlock rendering surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


void copyDataToVolume(jint storedFormat,
		      jint internalFormat,
		      jint xoffset, jint yoffset,
		      jint zoffset,
		      jint imgXOffset, jint imgYOffset,
		      jint imgZOffset,
		      jint subWidth, jint subHeight, jint subDepth,
		      jint tilew, jint tileh,
		      jbyte* data, 
		      LPDIRECT3DVOLUMETEXTURE8 surf,
		      jint level)
{

    D3DVOLUME_DESC ddsd;
    D3DLOCKED_BOX lockedBox; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    UINT i;

    if (surf == NULL) {
	return;
    }
    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    DWORD depth = ddsd.Depth;
    computePixelFormat(&ddpf, ddsd.Format);

    // It is possible when texture is not a power of 2 or
    // square only texture is required in hardware. In these
    // case the hardware memory buffer may smaller than the
    // texture pass in.

    if ((xoffset >= width) || 
	(yoffset >= height) ||
	(zoffset >= depth)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);
    DWORD zlimit = min(zoffset + subDepth, depth);

    hr = surf->LockBox(level, &lockedBox, NULL, 0);


    if (FAILED(hr)) {
	printf("Fail to lock volume: %s\n", DXGetErrorString8(hr));
	return;
    }

    int imgOffset = tilew*(tileh*imgZOffset + imgYOffset) + imgXOffset;
    int srcSlicePitch = tilew*tileh;
    unsigned char* p = (unsigned char *) lockedBox.pBits +
	                       zoffset*lockedBox.SlicePitch;



    switch (storedFormat) {
        case  FORMAT_BYTE_RGBA :
	    // This is the one we use when byReference = false
	    data += (imgOffset << 2);
	    srcSlicePitch <<= 2;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceRGBA(internalFormat, &ddpf, 
				      p,
				      lockedBox.RowPitch,
				      data,
				      xoffset, yoffset, 
				      xlimit, ylimit, 
				      tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }

	    break;
        case FORMAT_BYTE_RGB:
	    data += (imgOffset*3);
	    srcSlicePitch *= 3;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceRGB(internalFormat, &ddpf, 
				     p,
				     lockedBox.RowPitch,
				     data,
				     xoffset, yoffset, 
				     xlimit, ylimit, 
				     tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }
	    break;
        case FORMAT_BYTE_ABGR:
	    data += (imgOffset << 2);
	    srcSlicePitch <<= 2;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceABGR(internalFormat, &ddpf, 
				      p,
				      lockedBox.RowPitch,
				      data,
				      xoffset, yoffset, 
				      xlimit, ylimit, 
				      tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }
	    break;
        case FORMAT_BYTE_BGR:
	    data += (imgOffset*3);
	    srcSlicePitch *= 3;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceBGR(internalFormat, &ddpf, 
				     p,
				     lockedBox.RowPitch,
				     data,
				     xoffset, yoffset, 
				     xlimit, ylimit,
				     tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }
	    break;
        case FORMAT_BYTE_LA:
	    data += (imgOffset << 1);
	    srcSlicePitch <<= 1;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceLA(internalFormat, &ddpf, 
				    p,
				    lockedBox.RowPitch,
				    data,
				    xoffset, yoffset, 
				    xlimit, ylimit, 
				    tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }
	    break;
        case FORMAT_BYTE_GRAY:
	    data += imgOffset;

	    for (i = zoffset; i < zlimit; i++) {
		copyDataToSurfaceGray(internalFormat, &ddpf,
				      p,
				      lockedBox.RowPitch,
				      data,
				      xoffset, yoffset, 
				      xlimit, ylimit, 
				      tilew);
		p += lockedBox.SlicePitch;
		data += srcSlicePitch;
	    }
	    break;
        default: // should not happen
	    printf("[Java 3D] StoredFormat %d, internalFormat %d not support !\n",
		   storedFormat, internalFormat);
    }

    hr = surf->UnlockBox(level);
    if (FAILED(hr)) {
	printf("Fail to unlock volume: %s\n", DXGetErrorString8(hr));
	return;
    }
}


void copyDataToVolume(jint storedFormat,
		      jint internalFormat,
		      jint xoffset, jint yoffset,
		      jint zoffset,
		      jint imgXOffset, jint imgYOffset,
		      jint imgZOffset,
		      jint subWidth, jint subHeight, jint subDepth,
		      jint tilew, jint tileh,
		      jshort* data, 
		      LPDIRECT3DVOLUMETEXTURE8 surf,
		      jint level)
{
    D3DVOLUME_DESC ddsd;
    D3DLOCKED_BOX lockedBox; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    UINT i;

    if (surf == NULL) {
	return;
    }

    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    DWORD depth = ddsd.Depth;

    computePixelFormat(&ddpf, ddsd.Format);


    if ((xoffset >= width) || 
	(yoffset >= height) ||
	(zoffset >= depth)) {
	return;
    }


    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);
    DWORD zlimit = min(zoffset + subDepth, depth);

    hr = surf->LockBox(level, &lockedBox, NULL, 0);

    if (FAILED(hr)) {
	printf("Fail to lock volume: %s\n", DXGetErrorString8(hr));
	return;
    }

    int imgOffset = tilew*(tileh*imgZOffset + imgYOffset) + imgXOffset;
    int srcSlicePitch = (tilew*tileh) << 1;
    unsigned char* p = (unsigned char *) lockedBox.pBits +
	                       zoffset*lockedBox.SlicePitch;

    data += (imgOffset << 1);

    for (i = zoffset; i < zlimit; i++) {
	copyDataToSurfaceGray(internalFormat, &ddpf, 
			      p,
			      lockedBox.RowPitch,
			      data,
			      xoffset, yoffset, 
			      xlimit, ylimit, 
			      tilew);
	p += lockedBox.SlicePitch;
	data += srcSlicePitch;
    }

    hr = surf->UnlockBox(level);
    if (FAILED(hr)) {
	printf("Fail to unlock volume: %s\n", DXGetErrorString8(hr));
	return;
    }
}


LPDIRECT3DTEXTURE8 createSurfaceFromImage(JNIEnv *env,
					  jobject pa2d,
					  jlong ctx,
					  int width,
					  int height,
					  jbyteArray imageYdown)
{
    GetDevice2();

    int internalFormat;
    jclass pa2d_class = env->GetObjectClass(pa2d);

    jfieldID id = env->GetFieldID(pa2d_class, "storedYdownFormat", "I");
    int storedFormat = env->GetIntField(pa2d, id);

    id = env->GetFieldID(pa2d_class, "internalFormat", "I");
    internalFormat = env->GetIntField(pa2d, id);


    switch (internalFormat) {
        case FORMAT_BYTE_RGBA:
	    internalFormat = J3D_RGBA;
	    break;
        case FORMAT_BYTE_RGB:
	    internalFormat = J3D_RGB;
	    break;
        case FORMAT_BYTE_LA:
	    internalFormat = LUMINANCE_ALPHA;
	    break;
        case FORMAT_BYTE_GRAY:
        case FORMAT_USHORT_GRAY:	    
	    internalFormat = LUMINANCE;
	    break;
        default:
	    printf("Format %d not support for Image Component\n",  internalFormat);
	    return NULL;
    }
	     

    LPDIRECT3DTEXTURE8 surf;

    surf = createTextureSurface(d3dCtx, 1, internalFormat, 
				width, height);

    if (surf == NULL) {
	return NULL; 
    }

    if (imageYdown != NULL) {
	if (storedFormat != FORMAT_USHORT_GRAY) {
	    jbyte *byteData = (jbyte *) (env->GetPrimitiveArrayCritical(
							imageYdown,   NULL));
	    copyDataToSurface(storedFormat, internalFormat, 0, 0, 0, 0,
			      width, height, width, byteData, surf, 0);
	    env->ReleasePrimitiveArrayCritical(imageYdown, byteData, 0);
	    
	} else {
	    jshort *shortData = (jshort *)(env->GetPrimitiveArrayCritical(
								  imageYdown, NULL));
	    copyDataToSurface(storedFormat, internalFormat, 0, 0, 0, 0,
			      width, height, width, shortData, surf, 0);
	    env->ReleasePrimitiveArrayCritical(imageYdown, shortData, 0);
	}
    }
    return surf;
}

VOID createLineModeIndexBuffer(D3dCtx *d3dCtx) 
{
    HRESULT hr;
    WORD *wptr;

    hr = d3dCtx->pDevice->CreateIndexBuffer(6*sizeof(WORD),
					    D3DUSAGE_WRITEONLY,
					    D3DFMT_INDEX16,   
					    D3DPOOL_DEFAULT,
					    &d3dCtx->lineModeIndexBuffer);
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(CREATEINDEXVBFAIL, hr);
	return;
    }


    hr = d3dCtx->lineModeIndexBuffer->Lock(0, 0, (BYTE **) &wptr,  0);
    if (FAILED(hr)) {
	D3dCtx::d3dWarning(LOCKINDEXVBFAIL, hr);
	return;
    }

    *wptr++ = 0;
    *wptr++ = 1;
    *wptr++ = 2;
    *wptr++ = 3;
    *wptr++ = 0;
    *wptr = 0; // not used
    d3dCtx->lineModeIndexBuffer->Unlock();
}

// Return TRUE if index is adjust smaller 
BOOL createQuadIndices(D3dCtx *d3dCtx, int vcount) 
{
    DWORD dwIndexCount = (vcount*3) >> 1;    
    WORD *q;
    LPDIRECT3DDEVICE8 device = d3dCtx->pDevice;
    HRESULT hr;
    BOOL adjustIdx = FALSE;

    if (dwIndexCount > d3dCtx->deviceInfo->maxVertexIndex) {
	// We'll render the VB multiple times in this case
	dwIndexCount = min(d3dCtx->deviceInfo->maxVertexIndex,
			   (d3dCtx->deviceInfo->maxPrimitiveCount << 1));
	adjustIdx = TRUE;
    }

    if (dwIndexCount > d3dCtx->quadIndexBufferSize) {
	d3dCtx->freeResource(d3dCtx->quadIndexBuffer);
	hr = device->CreateIndexBuffer(dwIndexCount*sizeof(WORD), 
				       D3DUSAGE_WRITEONLY,
				       D3DFMT_INDEX16,
				       D3DPOOL_MANAGED,
				       &d3dCtx->quadIndexBuffer);
	if (FAILED(hr)) {
	    D3dCtx::d3dWarning(CREATEINDEXVBFAIL, hr);
	    d3dCtx->quadIndexBufferSize = 0;	    
	    d3dCtx->quadIndexBuffer = NULL;
	    if (d3dCtx->quadIndexBufferSize > 0) {	    
		// indices has successfully set before, we prevent
		// setting this when indices did not set before.
		// It is becasue there is a bug in Nvidia driver which
		// will crash in this case.
		device->SetIndices(NULL, 0); 
	    }
	    return adjustIdx;
	} 

	d3dCtx->quadIndexBufferSize = dwIndexCount;
	hr = d3dCtx->quadIndexBuffer->Lock(0, 0, (BYTE **) &q, 0);
	if (FAILED(hr)) {
	    D3dCtx::d3dWarning(LOCKINDEXVBFAIL, hr);
	    if (d3dCtx->quadIndexBufferSize > 0) {	    
		device->SetIndices(NULL, 0); 
	    }
	    return adjustIdx;
	}
	int i = -1;
	int j = 0;

	while (j < dwIndexCount) {
	    q[j++] = ++i;   // q[0] = 0
	    q[j++] = i+1;   // q[1] = 1
	    q[j++] = i+2;   // q[2] = 2	    
	    q[j++] = i++;   // q[3] = 0	    			    
	    q[j++] = ++i;   // q[4] = 2	    			    
	    q[j++] = ++i;   // q[5] = 3	    			    
	}

	d3dCtx->quadIndexBuffer->Unlock();
    }

    return adjustIdx;
}


int getPrimitiveNum(int primitive, int vcount)
{
    switch (primitive) {
        case D3DPT_TRIANGLESTRIP:
	    return vcount-2;
        case D3DPT_TRIANGLEFAN:
	    return vcount-2;
        case D3DPT_LINESTRIP:
	    return vcount - 1;
        case D3DPT_LINELIST:
	    return vcount >> 1;
        case D3DPT_TRIANGLELIST:
	    return vcount/3;
        case D3DPT_POINTLIST:
	    return vcount;
        default:  // should not happen
	    printf("[Java 3D] Unknown primitive type %d\n", primitive);
    }
    return 0;
}


/*
 * Note that tThe condition width == height always holds 
 * when this function is invoked.
 */
LPDIRECT3DCUBETEXTURE8 createCubeMapTexture(D3dCtx *d3dCtx,
					    jint numLevels,
					    jint internalFormat,
					    jint width, 
					    jint height)
{
    LPDIRECT3DCUBETEXTURE8 pTexture;
    D3DFORMAT format;
    HRESULT hr;

    LPDIRECT3DDEVICE8 pDevice = d3dCtx->pDevice;
    D3dDeviceInfo *deviceInfo = d3dCtx->deviceInfo;

    if (!deviceInfo->supportMipmap) {
	numLevels = 1;
    }     

    getTexWidthHeight(deviceInfo, &width, &height); 
    format = getTexFormat(internalFormat);

    // If format not support, the utility function will adjust the
    // calling parameters automatically
    hr = D3DXCreateCubeTexture(d3dCtx->pDevice, width,
			       numLevels, 0, format, D3DPOOL_MANAGED,
			       &pTexture);

    if (FAILED(hr)) {
	if (debug) {
	    printf("Fail to create cube texture surface %dx%d, format %d, level %d : %s\n",
		   width, height, format, numLevels, DXGetErrorString8(hr));
	}
	return NULL;
    }
    
    return pTexture;
}


void copyDataToCubeMap(jint storedFormat, 
		       jint internalFormat,
		       jint xoffset, jint yoffset,
		       jint imgXOffset, jint imgYOffset,
		       jint subWidth, jint subHeight,
		       jint tilew, 
		       jshort *data, LPDIRECT3DCUBETEXTURE8 surf,
		       jint level,
		       jint face)
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }

    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }
    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(textureCubeMapFace[face], level, 
			&lockedRect, NULL, 0);

    if (FAILED(hr)) {
	printf("Fail to lock surface: %s\n", DXGetErrorString8(hr));
	return;
    }

    int offset = tilew*imgYOffset + imgXOffset;

    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
	(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
	copyDataToSurfaceGray(internalFormat, &ddpf, 
			      (unsigned char *) lockedRect.pBits,
			      lockedRect.Pitch,
			      data + 
			      ((offset+tilew*(ylimit-yoffset)) << 1), 
			      xoffset, yoffset, 
			      xlimit, ylimit, -tilew);
    } else {
	copyDataToSurfaceGrayRev(internalFormat, &ddpf, 
			      (unsigned char *) lockedRect.pBits,
				 lockedRect.Pitch,
				 data + (offset << 1), 
				 xoffset, yoffset, 
				 xlimit, ylimit, tilew);
    }

    hr = surf->UnlockRect(textureCubeMapFace[face], level);
    if (FAILED(hr)) {
	printf("Fail to unlock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


void copyDataToCubeMap(jint storedFormat,
		       jint internalFormat,
		       jint xoffset, jint yoffset,
		       jint imgXOffset, jint imgYOffset,
		       jint subWidth, jint subHeight,
		       jint tilew, 
		       jbyte* data, 
		       LPDIRECT3DCUBETEXTURE8 surf,
		       jint level,
		       jint face)
{
    D3DSURFACE_DESC ddsd;
    D3DLOCKED_RECT lockedRect; 
    PIXELFORMAT ddpf;
    HRESULT hr;
    
    if (surf == NULL) {
	return;
    }
    surf->GetLevelDesc(level, &ddsd);
    DWORD width = ddsd.Width;
    DWORD height = ddsd.Height;
    computePixelFormat(&ddpf, ddsd.Format);

    // It is possible when texture is not a power of 2 or
    // square only texture is required in hardware. In these
    // case the hardware memory buffer may smaller than the
    // texture pass in.

    if ((xoffset >= width) || (yoffset >= height)) {
	return;
    }

    DWORD xlimit = min(xoffset + subWidth, width);
    DWORD ylimit = min(yoffset + subHeight, height);

    hr = surf->LockRect(textureCubeMapFace[face],
			level, &lockedRect, NULL, 0);


    if (FAILED(hr)) {
	printf("Fail to lock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
    int offset = tilew*imgYOffset + imgXOffset;

    switch (storedFormat) {
        case  FORMAT_BYTE_RGBA :
	    // This is the one we use when byReference = false
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
		// Copy the pixel from bottom to up and 
		// left to right in this case to match OGL definition
		copyDataToSurfaceRGBA(internalFormat, &ddpf, 
				      (unsigned char *) lockedRect.pBits,
				      lockedRect.Pitch,
				      data + 
				      ((offset + tilew*(ylimit-yoffset-1)) << 2),
				      xoffset, yoffset, 
				      xlimit, ylimit, -tilew);
	    } else {
		// Copy the pixel from up to bottom and 
		// right to left in this case to match OGL definition
		copyDataToSurfaceRGBARev(internalFormat, &ddpf, 
					 (unsigned char *) lockedRect.pBits,
					 lockedRect.Pitch,
					 data + (offset << 2),
					 xoffset, yoffset, 
					 xlimit, ylimit, tilew);
	    }
	    break;
        case FORMAT_BYTE_RGB:
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
		copyDataToSurfaceRGB(internalFormat, &ddpf, 
				     (unsigned char *) lockedRect.pBits,
				     lockedRect.Pitch,
				     data + 
				     3*(offset + tilew*(ylimit-yoffset-1)),
				     xoffset, yoffset, 
				     xlimit, ylimit, -tilew);
	    } else {
		copyDataToSurfaceRGBRev(internalFormat, &ddpf, 
					(unsigned char *) lockedRect.pBits,
					lockedRect.Pitch,
					data + 3*offset,
					xoffset, yoffset, 
					xlimit, ylimit, tilew);
	    }
	    break;
        case FORMAT_BYTE_ABGR:
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
		copyDataToSurfaceABGR(internalFormat, &ddpf, 
				      (unsigned char *) lockedRect.pBits,
				      lockedRect.Pitch,
				      data + 
				      ((offset+tilew*(ylimit-yoffset-1)) << 2), 
				      xoffset, yoffset, 
				      xlimit, ylimit, -tilew);
	    } else {
		copyDataToSurfaceABGRRev(internalFormat, &ddpf, 
					 (unsigned char *) lockedRect.pBits,
					 lockedRect.Pitch,
					 data + (offset << 2), 
					 xoffset, yoffset, 
					 xlimit, ylimit, tilew);
	    }
	    break;
        case FORMAT_BYTE_BGR:
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
		copyDataToSurfaceBGR(internalFormat, &ddpf, 
				     (unsigned char *) lockedRect.pBits,
				     lockedRect.Pitch,
				     data + 
				     3*(offset + tilew*(ylimit-yoffset-1)),
				     xoffset, yoffset, 
				     xlimit, ylimit, -tilew);
	    } else {
		copyDataToSurfaceBGRRev(internalFormat, &ddpf, 
					(unsigned char *) lockedRect.pBits,
					lockedRect.Pitch,
					data + 3*offset, 
					xoffset, yoffset, 
					xlimit, ylimit, tilew);
	    }
	    break;
        case FORMAT_BYTE_LA:
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {
		copyDataToSurfaceLA(internalFormat, &ddpf, 
				    (unsigned char *) lockedRect.pBits,
				    lockedRect.Pitch,
				    data + 
				    ((offset+tilew*(ylimit-yoffset-1)) << 1), 
				    xoffset, yoffset, 
				    xlimit, ylimit, -tilew);		
	    } else {
		copyDataToSurfaceLARev(internalFormat, &ddpf, 
				       (unsigned char *) lockedRect.pBits,
				       lockedRect.Pitch,
				       data + (offset << 1), 
				       xoffset, yoffset, 
				       xlimit, ylimit, tilew);
	    }
	    break;
        case FORMAT_BYTE_GRAY:
	    if ((face == D3DCUBEMAP_FACE_NEGATIVE_Y) ||
		(face == D3DCUBEMAP_FACE_POSITIVE_Y)) {	    
		copyDataToSurfaceGray(internalFormat, &ddpf,
				      (unsigned char *) lockedRect.pBits,
				      lockedRect.Pitch,
				      data + 
				      offset + tilew*(ylimit-yoffset-1), 
				      xoffset, yoffset, 
				      xlimit, ylimit, -tilew);
	    } else {
		copyDataToSurfaceGrayRev(internalFormat, &ddpf,
					 (unsigned char *) lockedRect.pBits,
					 lockedRect.Pitch,
					 data + offset, 
					 xoffset, yoffset, 
					 xlimit, ylimit, tilew);
	    }
	    break;
        default: // should not happen
	    printf("[Java 3D] StoredFormat %d, internalFormat %d not support !\n",
		   storedFormat, internalFormat);
    }

    hr = surf->UnlockRect(textureCubeMapFace[face], level);
    if (FAILED(hr)) {
	printf("Fail to unlock surface: %s\n", DXGetErrorString8(hr));
	return;
    }
}


void drawTextureRect(D3dCtx *d3dCtx,
		     LPDIRECT3DDEVICE8 device,
		     LPDIRECT3DTEXTURE8 surf,
		     D3DTLVERTEX screenCoord,
		     int startx, int starty, 
		     int endx, int endy,
		     int scaleWidth, int scaleHeight,
		     boolean texModeRepeat) 
{
	LPDIRECT3DTEXTURE8 texture = NULL;
	DWORD transflag, minstate, magstate, texcoordstate;
	DWORD wrapU, wrapV;
	DWORD colorOp, colorArg, alphaOp, alphaArg;
	D3DMATRIX m;

	int h = endy - starty;
	int w = endx - startx;

	device->SetRenderState(D3DRS_SPECULARENABLE, FALSE);

	device->GetTexture(0, (LPDIRECT3DBASETEXTURE8 *) &texture);    

	device->GetTextureStageState(0, D3DTSS_TEXTURETRANSFORMFLAGS,
				     &transflag);
	if (transflag != D3DTTFF_DISABLE) {
	    device->GetTransform(D3DTS_TEXTURE0, &m);
	}

	device->GetTextureStageState(0, D3DTSS_MINFILTER, &minstate);
	device->GetTextureStageState(0, D3DTSS_MAGFILTER, &magstate);
	device->GetTextureStageState(0, D3DTSS_TEXCOORDINDEX, &texcoordstate);
	device->SetTexture(0, surf);
	device->SetTextureStageState(0, D3DTSS_TEXTURETRANSFORMFLAGS,
				     D3DTTFF_DISABLE);

	if ((w == scaleWidth) && (h == scaleHeight)) {
	    device->SetTextureStageState(0, D3DTSS_MINFILTER, D3DTEXF_POINT);
	    device->SetTextureStageState(0, D3DTSS_MAGFILTER,
					 D3DTEXF_POINT);
	} else {
	    device->SetTextureStageState(0, D3DTSS_MINFILTER, D3DTEXF_LINEAR);
	    device->SetTextureStageState(0, D3DTSS_MAGFILTER, D3DTEXF_LINEAR);
	}
	device->SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, 0);
	device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);

	device->GetTextureStageState(0, D3DTSS_COLOROP, &colorOp);
	device->GetTextureStageState(0, D3DTSS_COLORARG1, &colorArg);
        device->GetTextureStageState(0, D3DTSS_ALPHAOP, &alphaOp);
	device->GetTextureStageState(0, D3DTSS_ALPHAARG1, &alphaArg);

	device->SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_SELECTARG1);
	device->SetTextureStageState(0, D3DTSS_COLORARG1, D3DTA_TEXTURE);
        device->SetTextureStageState(0, D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
	device->SetTextureStageState(0, D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
	if (d3dCtx->fillMode != D3DFILL_SOLID) {
	    device->SetRenderState(D3DRS_FILLMODE, D3DFILL_SOLID);
	}

	D3DSURFACE_DESC ddsd;
	surf->GetLevelDesc(0, &ddsd);


	float tumax;
	float tvmax;
	float tumin = startx/(float) ddsd.Width;
	float tvmin = starty/(float) ddsd.Height;
	boolean multipleDraw;

	if (texModeRepeat) {
	    if ((w == ddsd.Width) && (h == ddsd.Height)) {
		// width & height match texture, so there is
		// no need to draw multiple time
		tumax = scaleWidth/(float) ddsd.Width;
		tvmax = scaleHeight/(float) ddsd.Height;
		device->GetTextureStageState(0, D3DTSS_ADDRESSU, &wrapU);
		device->GetTextureStageState(0, D3DTSS_ADDRESSV, &wrapV);
		device->SetTextureStageState(0, D3DTSS_ADDRESSU, D3DTADDRESS_WRAP);
		device->SetTextureStageState(0, D3DTSS_ADDRESSV, D3DTADDRESS_WRAP);
		multipleDraw = false;
	    } else {
		tumax = endx/(float) ddsd.Width;
		tvmax = endy/(float) ddsd.Height;
		multipleDraw = true;
	    }
	} else {
	    tumax = endx/(float) ddsd.Width;
	    tvmax = endy/(float) ddsd.Height;	    
	    multipleDraw = false;
	}


	d3dCtx->rasterRect[0].tu = tumin;
	d3dCtx->rasterRect[0].tv = tvmax;
	d3dCtx->rasterRect[1].tu = tumin;
	d3dCtx->rasterRect[1].tv = tvmin;
	d3dCtx->rasterRect[2].tu = tumax;	
	d3dCtx->rasterRect[2].tv = tvmax;	
	d3dCtx->rasterRect[3].tu = tumax;	
	d3dCtx->rasterRect[3].tv = tvmin;
	
	d3dCtx->rasterRect[0].sx = screenCoord.sx;
	d3dCtx->rasterRect[0].sz = screenCoord.sz;
	d3dCtx->rasterRect[0].rhw = screenCoord.rhw;

	d3dCtx->rasterRect[1].sx = screenCoord.sx;
	d3dCtx->rasterRect[1].sy = screenCoord.sy;
	d3dCtx->rasterRect[1].sz = screenCoord.sz;
	d3dCtx->rasterRect[1].rhw = screenCoord.rhw;

	d3dCtx->rasterRect[2].sz = screenCoord.sz;
	d3dCtx->rasterRect[2].rhw = screenCoord.rhw;

	d3dCtx->rasterRect[3].sy = screenCoord.sy;
	d3dCtx->rasterRect[3].sz = screenCoord.sz;
	d3dCtx->rasterRect[3].rhw = screenCoord.rhw;

	if ((h > 0) && (w > 0)) {
	    device->SetVertexShader(D3DFVF_XYZRHW|D3DFVF_TEX1);
	    if (!multipleDraw) {
		d3dCtx->rasterRect[0].sy = screenCoord.sy + scaleHeight;
		d3dCtx->rasterRect[2].sx = screenCoord.sx + scaleWidth;
		d3dCtx->rasterRect[2].sy = screenCoord.sy + scaleHeight;
		d3dCtx->rasterRect[3].sx = screenCoord.sx + scaleWidth;
		device->DrawPrimitiveUP(D3DPT_TRIANGLESTRIP,
					2,
					d3dCtx->rasterRect, 
					sizeof(D3DTLVERTEX));
	    } else {
		d3dCtx->rasterRect[0].sy = screenCoord.sy + h;
		d3dCtx->rasterRect[2].sx = screenCoord.sx + w;
		d3dCtx->rasterRect[2].sy = screenCoord.sy + h;
		d3dCtx->rasterRect[3].sx = screenCoord.sx + w;
		for (int i=0; i < ceil(scaleHeight/((double) h)); i++) {
		    for (int j=0; j < ceil(scaleWidth/((double) w)); j++) {
			device->DrawPrimitiveUP(D3DPT_TRIANGLESTRIP,
						2,
						d3dCtx->rasterRect, 
						sizeof(D3DTLVERTEX));
			d3dCtx->rasterRect[0].sx += w;
			d3dCtx->rasterRect[1].sx += w;
			d3dCtx->rasterRect[2].sx += w;
			d3dCtx->rasterRect[3].sx += w;
		    }
		    d3dCtx->rasterRect[0].sx = screenCoord.sx;
		    d3dCtx->rasterRect[1].sx = screenCoord.sx;
		    d3dCtx->rasterRect[2].sx = screenCoord.sx + w;
		    d3dCtx->rasterRect[3].sx = screenCoord.sx + w;
		    d3dCtx->rasterRect[0].sy += h;
		    d3dCtx->rasterRect[1].sy += h;
		    d3dCtx->rasterRect[2].sy += h;
		    d3dCtx->rasterRect[3].sy += h;
		}

	    }
	}

	// restore original texture stage values
	if (texture != NULL) {
	    device->SetTexture(0, texture);
	    texture->Release();
	} else {
	    device->SetTexture(0, NULL);
	}

	device->SetTextureStageState(0, D3DTSS_TEXTURETRANSFORMFLAGS,
				     transflag);
	if (transflag != D3DTTFF_DISABLE) {
	    device->SetTransform(D3DTS_TEXTURE0, &m);
	}
	if (d3dCtx->fillMode != D3DFILL_SOLID) {
	    device->SetRenderState(D3DRS_FILLMODE, d3dCtx->fillMode);
	}
	device->SetTextureStageState(0, D3DTSS_MINFILTER, minstate);
	device->SetTextureStageState(0, D3DTSS_MAGFILTER, magstate);
	device->SetTextureStageState(0, D3DTSS_TEXCOORDINDEX, texcoordstate);

	device->SetTextureStageState(0, D3DTSS_COLOROP, colorOp);
	device->SetTextureStageState(0, D3DTSS_COLORARG1, colorArg);
        device->SetTextureStageState(0, D3DTSS_ALPHAOP, alphaOp);
	device->SetTextureStageState(0, D3DTSS_ALPHAARG1, alphaArg);

	if (texModeRepeat && !multipleDraw) {
	    device->SetTextureStageState(0, D3DTSS_ADDRESSU, wrapU);
	    device->SetTextureStageState(0, D3DTSS_ADDRESSV, wrapV);
	}
	device->SetRenderState(D3DRS_CULLMODE, d3dCtx->cullMode);
	device->SetRenderState(D3DRS_SPECULARENABLE, TRUE);
}
