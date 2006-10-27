/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

#if !defined(D3DCTX_H)
#define D3DCTX_H


#include "StdAfx.h"
#include "D3dVertexBuffer.hpp"
#include "D3dDisplayList.hpp"

#define TEXTURETABLESIZE     8
#define TEXSTAGESUPPORT      8
#define DISPLAYLIST_INITSIZE 8
#define NOCHANGE             0
#define RESETSURFACE         1
#define RECREATEDDRAW        2
#define RECREATEDFAIL       -1

#undef javax_media_j3d_Canvas3D_STENCIL_BUFFER
#define javax_media_j3d_Canvas3D_STENCIL_BUFFER 4096L


// Use in texCoordPosition[]
// Must be negative number
#define TEX_GEN_NONE             0
#define TEX_EYE_LINEAR          -1
#define TEX_SPHERE_MAP          -2
#define TEX_NORMAL_MAP          -3
#define TEX_REFLECT_MAP         -4
#define TEX_OBJ_LINEAR          -5
#define TEX_GEN_INVALID         -6
#define TEX_GEN_AUTO            -7

typedef struct _D3DVERTEX {
    float x, y, z;
} D3DVERTEX;


typedef struct _D3DTLVERTEX {
    float sx, sy, sz, rhw;
    float tu, tv;
} D3DTLVERTEX;

typedef struct _COORDTEXVERTEX {
    float sx, sy, sz;
    float tu, tv;
} COORDTEXVERTEX;

typedef struct _COORDCLRTEXVERTEX {
    float sx, sy, sz;
    D3DCOLOR color;
    float tu, tv;
} COORDCLRTEXVERTEX;


typedef vector<LPDIRECT3DRESOURCE9> LPDIRECT3DRESOURCE9Vector;
typedef vector<LPDIRECT3DVERTEXBUFFER9> LPDIRECT3DVERTEXBUFFER9Vector;
//issue 135 iterator for vectors
typedef vector<LPDIRECT3DRESOURCE9>::iterator ITER_LPDIRECT3DRESOURCE9;
typedef vector<LPDIRECT3DVERTEXBUFFER9>::iterator ITER_LPDIRECT3DVERTEXBUFFER9;

class D3dCtx {
public:

    HWND   hwnd;                  // window handle
    HWND   topHwnd;               // Top window handle
    D3dDriverInfo  *driverInfo;   // Driver use
    D3dDeviceInfo  *deviceInfo;   // Device use

    LPDIRECT3D9       pD3D;       // Direct3D interface
    LPDIRECT3DDEVICE9 pDevice;    // Instance of D3D Device

    LPDIRECT3DSURFACE9 depthStencilSurface;

    // This is used for readRaster and offscreen rendering
    // Only allocate the memory area if necessary
    LPDIRECT3DSURFACE9 frontSurface;

    LPDIRECT3DSURFACE9 backSurface;

    // Parameters use for CreateDevice()
    D3DPRESENT_PARAMETERS d3dPresent;
    DWORD          dwBehavior;
	BOOL           bForceHwdVertexProcess; // true if j3d.d3dVertexProcess is hardware
	BOOL           bForceMixVertexProcess; // true if j3d.d3dVertexProcess is mixed
	BOOL           bForceSWVertexProcess;  // true if j3d.d3dVertexProcess is software

	BOOL           bUseNvPerfHUD; // true if j3d.useNvPerfHUD is true
	                              // it also makes  bForceHwdVertexProcess true

    BOOL           offScreen; // true if it is offScreen rendering
                              // in this case only backSurface is used
	BOOL           bFastDrawQuads;
    DWORD          offScreenWidth;
    DWORD          offScreenHeight;

    BOOL           bFullScreen;  // true if in full screen mode
    BOOL           bFullScreenRequired; // true if must run in full
                                        // screen mode or die
    BOOL           inToggle;     // in toggle fullscreen/window mode
    RECT           screenRect;   // coordinate of window relative to
                                 // the whole desktop in multiple monitor
    RECT           windowRect;   // coordinate of window relative to
                                 // the current monitor desktop only
    INT            minZDepth;    // min Z depth set in NativeConfigTemplate
    INT            minZDepthStencil; // min Stencil depth set in NativeConfigTemplate
    DEVMODE        devmode;     // current display mode
    DWORD          antialiasing; // PREFERRED, REQUIRED or UNNECESSARY


    // Store current color as in OGL glColor()
    float currentColor_r;
    float currentColor_g;
    float currentColor_b;
    float currentColor_a;

    // Two side light is used. Note that D3D don't support two side
    // lighting.
    BOOL twoSideLightingEnable;

    // True if lighting is currently enable
    // Save the current RenderingState to avoid GetRenderState()
    // call during Rendering.
    BOOL isLightEnable;
    DWORD cullMode;
    DWORD fillMode;
    DWORD softwareVertexProcessing;
    DWORD zWriteEnable;
    DWORD zEnable;
	DWORD stencilWriteEnable; // new on 1.4
	DWORD stencilEnable; // new on 1.4

    // Ambient material used when coloring Attributes
    D3DMATERIAL9 ambientMaterial;

    // temporary variables for ambient light setting
    D3DLIGHT9 savedLight;
    D3DMATERIAL9 savedMaterial;
    BOOL savedLightEnable;

    // temporary variables used for building VertexBuffer
    LPD3DVERTEXBUFFER pVB;     // point to the current VB being update
    DWORD  texSetUsed;
    DWORD  texStride[TEXSTAGESUPPORT];

    // true when in toggle mode
    BOOL forceResize;

    // Texture related variables
    INT *bindTextureId;
    DWORD bindTextureIdLen;

    LPDIRECT3DTEXTURE9 *textureTable;
    DWORD textureTableLen;

    // Volume Texture related variables
    // Since 2d & 3d texture ID can't be the same from Java3D.
    // We don't need bindVolumeId
    LPDIRECT3DVOLUMETEXTURE9 *volumeTable;
    DWORD volumeTableLen;

    // Texture Cube Mapping related variables
    LPDIRECT3DCUBETEXTURE9 *cubeMapTable;
    DWORD cubeMapTableLen;

    // true if hardware support MultiTexture
    BOOL multiTextureSupport;

    // handle to monitor that this ctx belongs to. This is equal to
    // NULL if this window is a primary display screen or it covers
    // more than one screen.
    HMONITOR monitor;

    // D3D don't have concept of current texture unit stage,
    // instead, the texture unit stage is pass in as argument
    // for all texture call.
    INT texUnitStage;

    // true if linear filtering is to be used
    BOOL texLinearMode;


    // This is used temporary to store the blend function
    // when two pass texture is used to simulate BLEND mode
    DWORD srcBlendFunc;
    DWORD dstBlendFunc;
    DWORD blendEnable;


    // This is used for to transform vertex
    // from world to screen coordinate
    LPDIRECT3DVERTEXBUFFER9 srcVertexBuffer;
    LPDIRECT3DVERTEXBUFFER9 dstVertexBuffer;

    // For Rect of texture map in Raster write
    D3DTLVERTEX rasterRect[4];

    // Set automatic Texture coordinate generation type
    // TEX_xxx_xxx as defined in GeometryArrayRetained.cpp
    INT texGenMode[TEXSTAGESUPPORT];

    // Whether TEXTURE_COORDINATE_2/3/4 is used in this state
    INT texCoordFormat[TEXSTAGESUPPORT];

    // Whether texture transform matrix is set in this state or not
    BOOL texTransformSet[TEXSTAGESUPPORT];

    // Remember the last Texture Transform pass down, since
    // TexCoordGen may destroy it in some mode so we have to
    // restore it later manually.
    D3DXMATRIX texTransform[TEXSTAGESUPPORT];

    // True if we copy m._41, m._42 elment to m._31, m._32
    // as a workaround that 2D texture translation did not work.
    BOOL texTranslateSet[TEXSTAGESUPPORT];

    float planeS[TEXSTAGESUPPORT][4];
    float planeT[TEXSTAGESUPPORT][4];
    float planeR[TEXSTAGESUPPORT][4];
    float planeQ[TEXSTAGESUPPORT][4];

    // Display List ID (start from 1) => VertexBuffer pointer table
    LPD3DDISPLAYLIST *displayListTable;
    int dlTableSize;

    // For immediate mode rendering, we save the vertexBuffer pointer
    // in variable pVertexBuffer of GeometryArrayRetained to reuse it.
    D3dVertexBuffer vertexBufferTable;

    int currDisplayListID;

    // True if colorTarget need to reset
    BOOL resetColorTarget;

    // Use for QuadArray
    LPDIRECT3DINDEXBUFFER9 quadIndexBuffer;
    DWORD quadIndexBufferSize;

    // Use for Quad Polygon Line mode
    LPDIRECT3DINDEXBUFFER9 lineModeIndexBuffer;

    // Use temporary for reindexing
    DWORD *reIndexifyTable;

    // True if Direcct Draw context is being destroy and recreate
    // again during resize/toggle
    BOOL recreateDDraw;

    // Screen coordinate of current monitor in use
    // When hardware accleerated mode is used. For Emulation mode
    // they are always zero;
    INT monitorLeft;
    INT monitorTop;
    float pointSize;

    // Use to free resource surface in swap()
    BOOL useFreeList0;
    LPDIRECT3DRESOURCE9Vector freeResourceList0;
    LPDIRECT3DRESOURCE9Vector freeResourceList1;
    D3dVertexBufferVector freeVBList0;
    D3dVertexBufferVector freeVBList1;

    D3dCtx(JNIEnv *env, jobject obj, HWND hwnd, BOOL offScreen, jint vid);
    ~D3dCtx();

    BOOL initialize(JNIEnv *env, jobject obj);
    INT resize(JNIEnv *env, jobject obj);
    VOID error(char *s, HRESULT hr);
    VOID error(int idx, HRESULT hr);
    VOID error(int idx);
    VOID error(char *s);
    VOID warning(int idx, HRESULT hr);
    VOID warning(int idx);

    static VOID d3dError(int idx);
    static VOID d3dError(char *s);
    static VOID d3dWarning(int idx, HRESULT hr);
    static VOID d3dWarning(int idx);

    INT  toggleMode(BOOL fullScreen, JNIEnv *env, jobject obj);
    DWORD getWidth();
    DWORD getHeight();

    VOID release();
    VOID releaseTexture();
    VOID releaseVB();
    VOID setViewport();
    VOID transform(D3DVERTEX *worldCoord, D3DTLVERTEX *screenCoord);
    VOID getScreenRect(HWND hwnd, RECT *rect);
    HMONITOR findMonitor();
    VOID setDriverInfo();
    static D3dDeviceInfo* setDeviceInfo(D3dDriverInfo *driverInfo,
					BOOL *bFullScreen,
					int minZDepth,
					int minZDepthStencil);
    DWORD findBehavior();
    VOID setPresentParams();
    INT resetSurface(JNIEnv *env, jobject obj);
    VOID setPresentParams(JNIEnv *env, jobject obj);
    VOID setAmbientLightMaterial();
    VOID restoreDefaultLightMaterial();
    VOID freeResource(LPDIRECT3DRESOURCE9 surf);
    VOID freeVB(LPD3DVERTEXBUFFER vb);

    VOID freeList();
    VOID freeResourceList(LPDIRECT3DRESOURCE9Vector *v);
    VOID freeVBList(D3dVertexBufferVector *v);
    BOOL createFrontBuffer();

    static D3dDeviceInfo* selectDevice(int deviceID,
				       D3dDriverInfo *driverInfo,
				       BOOL *bFullScreen,
				       int minZDepth,
					   int minZDepthStencil);
    static D3dDeviceInfo* selectBestDevice(D3dDriverInfo *driverInfo,
					   BOOL *bFullScreen,
					   int minZDepth,
					   int minZDepthStencil);
    static VOID setDeviceFromProperty(JNIEnv *env);
	static BOOL getSystemProperty(JNIEnv *env, char *strName, char *strValue);
    static VOID setDebugProperty(JNIEnv *env);
    static VOID setVBLimitProperty(JNIEnv *env);
    static VOID setImplicitMultisamplingProperty(JNIEnv *env);
	


private:

    RECT savedTopRect;        // for toggle between fullscreen mode
    RECT savedClientRect;
    DWORD winStyle;
	// a private reference for JNIEnv
	JNIEnv* jniEnv;

    VOID createVertexBuffer();

    VOID setCanvasProperty(JNIEnv *env, jobject obj);
    VOID setFullScreenFromProperty(JNIEnv *env);
    VOID enumDisplayMode(DEVMODE *devmode);

    static VOID printWarningMessage(D3dDeviceInfo *deviceInfo);
    static VOID showError(HWND hwnd, char *s, BOOL bFullScreen);
    VOID setDefaultAttributes();
    VOID printInfo(D3DPRESENT_PARAMETERS *d3dPresent);
    VOID setWindowMode();
	jboolean getJavaBoolEnv(JNIEnv *env, char* envStr);
};

typedef vector<D3dCtx *> D3dCtxVector;
//issue 135 added iterator for D3dCtxVector
typedef vector<D3dCtx *>::iterator ITER_D3dCtxVector;
extern D3dCtxVector d3dCtxList;
const extern D3DXMATRIX identityMatrix;
#endif
