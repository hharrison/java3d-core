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

#if !defined(D3DUTIL_H)
#define D3DUTIL_H

#include "StdAfx.h"


// Index to D3dErrorMessage
#define DRIVERNOTFOUND          0
#define CANNOTRENDERWINDOW      1
#define D3DNOTFOUND             2
#define CARDNOTSUPPORT          3
#define NEEDSWITCHMODE          4
#define DEVICENOTFOUND          5
#define CREATEDEVICEFAIL        6
#define CREATEREFDEVICEFAIL     7
#define VIEWPORTFAIL            8
#define GETBACKBUFFERFAIL       9
#define COLORMODENOTSUPPORT     10
#define OUTOFMEMORY             11
#define UNKNOWNDEVICE           12
#define HALDEVICENOTFOUND       13
#define TNLHALDEVICENOTFOUND    14
#define NOSTENCILBUFFER         15
#define NOTEXTUREMEMORY         16
#define OFFSCREENCREATEFAIL     17
#define CREATEVERTEXBUFFER      18
#define RESETFAIL               19
#define HALNOTCOMPATIBLE        20
#define DEPTHSTENCILNOTFOUND    21
#define LOCKVBFAIL              22
#define CREATEVBFAIL            23
#define CREATEINDEXVBFAIL       24
#define LOCKINDEXVBFAIL         25

#define PI  3.14159265f

// Same definition as in ImageComponent2DRetained
#define CLEAN   0
#define MODIFY  1
#define NOTLIVE 2

// If we want to synchrinized draw primitive, change the 
// following to D3DDP_WAIT
#define DEFAULTMODE 0

#define D3D_STEREO 1


// Macro
#define SafeRelease(p) if(p) {p->Release(); p = NULL;}
#define SafeDelete(p)  if(p) { delete p; p = NULL;}
#define SafeFree(p)    if(p) { free(p); p = NULL;}

#define GetCtx() \
         if (ctx == 0) return; \
         D3dCtx *d3dCtx = reinterpret_cast<D3dCtx*>(ctx); \

         
#define GetDevice()  \
         GetCtx() \
         LPDIRECT3DDEVICE8 device = d3dCtx->pDevice; \
         if (device == NULL)  return;

#define GetCtx2() \
         if (ctx == 0) return 0;\
         D3dCtx *d3dCtx = reinterpret_cast<D3dCtx*>(ctx); \

         
#define GetDevice2()  \
         GetCtx2(); \
         LPDIRECT3DDEVICE8 device = d3dCtx->pDevice; \
         if (device == NULL)  return 0;

#define CopyColor(c, red, green, blue, alpha)  \
               (c).a = alpha; (c).r = red; (c).g = green; (c).b = blue;


#define CopyTranspose(m, s) { \
      (m)._11 = (s)[0]; (m)._12 = (s)[4]; (m)._13 = (s)[8];  (m)._14 = (s)[12]; \
      (m)._21 = (s)[1]; (m)._22 = (s)[5]; (m)._23 = (s)[9];  (m)._24 = (s)[13]; \
      (m)._31 = (s)[2]; (m)._32 = (s)[6]; (m)._33 = (s)[10]; (m)._34 = (s)[14]; \
      (m)._41 = (s)[3]; (m)._42 = (s)[7]; (m)._43 = (s)[11]; (m)._44 = (s)[15]; }

// Note that v should not be one of the reference in m
#define MultiplyScaler(m, v) { \
     (m)._11 *= v;  (m)._12 *= v; (m)._13 *= v;  (m)._14 *= v; \
     (m)._21 *= v;  (m)._22 *= v; (m)._23 *= v;  (m)._24 *= v; \
     (m)._31 *= v;  (m)._32 *= v; (m)._33 *= v;  (m)._34 *= v; \
     (m)._41 *= v;  (m)._42 *= v; (m)._43 *= v;  (m)._44 *= v; }

#define Clamp(c) \
         if (c > 1.0f) { \
              c = 1.0f; \
         } else if (c < 0.0f) { \
              c = 0.0f; \
         }

#define Magitude(x, y, z, w) sqrt((x)*(x) + (y)*(y) + (z)*(z) + (w)*(w));

#define NORMALIZE(x, y, z, w) { \
    float d; \
    d = Magitude(x, y, z, w); \
    (x) /= d; (y) /= d;  (z) /= d;  (w) /= d; }

extern vector<void *> freePointerList0;
extern vector<void *> freePointerList1;
extern BOOL useFreePointerList0;

extern HANDLE hSema; // handle to semaphore
extern BOOL firstError;
extern BOOL debug;

// use for VertexBuffer
extern OSVERSIONINFO osvi; // OS info

extern D3dCtx* findCtx(HWND hwnd);
extern VOID lock();
extern VOID unlock();
extern VOID lockImage();
extern VOID unlockImage();
extern VOID lockBackground();
extern VOID unlockBackground();
extern VOID lockGeometry();
extern VOID unlockGeometry();
extern VOID lockSurfaceList();
extern VOID unlockSurfaceList();
extern VOID freeSurface(LPDIRECT3DBASETEXTURE8 surf);
extern VOID freePointer(void* surf);
extern VOID freePointerList();
extern VOID setWindowCallback(HWND topHwnd, HWND hwnd);
extern char* getErrorMessage(int idx);
extern HWND getTopWindow(HWND hwnd);

extern LPDIRECT3DTEXTURE8 createTextureSurface(D3dCtx *d3dCtx,
					       jint numLevels,
					       jint internalFormat,
					       jint width, 
					       jint height);


extern LPDIRECT3DVOLUMETEXTURE8 createVolumeTexture(D3dCtx *d3dCtx,
						    jint numLevels,
						    jint internalFormat,
						    jint width, 
						    jint height,
						    jint depth);


extern LPDIRECT3DCUBETEXTURE8 createCubeMapTexture(D3dCtx *d3dCtx,
						   jint numLevels,
						   jint internalFormat,
						   jint width, 
						   jint height);


extern void copyDataToSurface(jint format, 
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,  jint tilew,
			      jshort *data, LPDIRECT3DTEXTURE8 surf,
			      jint level);

extern void copyDataToSurface(jint format,
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height, jint tilew,
			      jbyte* data, 
			      LPDIRECT3DTEXTURE8 surf,
			      jint level);

extern void copyDataToVolume(jint format, 
			     jint internalFormat,
			     jint xoffset, jint yoffset,
			     jint zoffset,
			     jint imgXOffset, jint imgYOffset,
			     jint imgZOffset,
			     jint width, jint height,  jint depth,
			     jint tilew, jint tileh,
			     jshort *data, LPDIRECT3DVOLUMETEXTURE8 surf,
			     jint level);


extern void copyDataToVolume(jint format,
			     jint internalFormat,
			     jint xoffset, jint yoffset,
			     jint zoffset,
			     jint imgXOffset, jint imgYOffset,
			     jint imgZOffset,
			     jint width, jint height, jint depth,
			     jint tilew, jint tileh,
			     jbyte* data, 
			     LPDIRECT3DVOLUMETEXTURE8 surf,
			     jint level);

extern void copyDataToCubeMap(jint format, 
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,
			      jint tilew, 
			      jshort *data, LPDIRECT3DCUBETEXTURE8 surf,
			      jint level,
			      jint face);


extern void copyDataToCubeMap(jint format,
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,
			      jint tilew, 
			      jbyte* data, 
			      LPDIRECT3DCUBETEXTURE8 surf,
			      jint level,
			      jint face);


extern void copyDepthFromSurface(jint xoffset, jint yoffset,
				 jint subWidth, jint subHeight,
				 jint *data, 
				 LPDIRECT3DSURFACE8 surf);

extern void copyDepthFromSurface(jint xoffset, jint yoffset,
				 jint subWidth, jint subHeight,
				 jfloat *data, 
				 LPDIRECT3DSURFACE8 surf);

extern void copyDepthToSurface(D3dCtx *d3dCtx,
			       LPDIRECT3DDEVICE8 device,
			       jint dst_xoffset, jint dst_yoffset,
			       jint src_xoffset, jint src_yoffset,
			       jint subWidth, jint subHeight,
			       jint src_width, jint src_height,
			       jint *data, 
			       LPDIRECT3DSURFACE8 surf);

extern void copyDepthToSurface(D3dCtx *d3dCtx,
			       LPDIRECT3DDEVICE8 device,
			       jint dst_xoffset, jint dst_yoffset,
			       jint src_xoffset, jint src_yoffset,
			       jint subWidth, jint subHeight,
			       jint src_width, jint src_height,
			       jfloat *data, 
			       LPDIRECT3DSURFACE8 surf);

extern void copyDataFromSurface(jint internalFormat,
				jint xoffset, jint yoffset,
				jint width, jint height, 
				jbyte *data, LPDIRECT3DSURFACE8 surf);

void compositeDataToSurface(jint px, jint py,
			    jint xoffset, jint yoffset,
			    jint subWidth, jint subHeight,
			    jint dataWidth, 
			    jbyte *data, 
			    LPDIRECT3DSURFACE8 surf);

// extern BOOL isIdentity(jdouble *matrix);

extern void CopyTextureStage(LPDIRECT3DDEVICE8 device, 
			     int fromLevel, int toLevel);


extern LPDIRECT3DTEXTURE8 createSurfaceFromImage(JNIEnv *env,
						 jobject pa2d,
						 jlong ctx,
						 int width,
						 int height,
						 jbyteArray pixels);
extern "C"
DWORD countBits(DWORD mask); // Define in MasterControl.c

extern BOOL createQuadIndices(D3dCtx *d3dCtx, int vcount);
extern VOID createLineModeIndexBuffer(D3dCtx *d3dCtx);
extern char *getPixelFormatName(D3DFORMAT format);
extern char *getMultiSampleName(D3DMULTISAMPLE_TYPE mtype); 
extern char *getSwapEffectName(D3DSWAPEFFECT swapEffect);
extern int getPrimitiveNum(int primitive, int vcount);
extern int getMaxNumVertex(int primitive, int vcount);
extern void drawTextureRect(D3dCtx *d3dCtx,
			    LPDIRECT3DDEVICE8 device,
			    LPDIRECT3DTEXTURE8 tex,
			    D3DTLVERTEX screenCoord,
			    int startx, int starty,
			    int endx, int endy,
			    int scaleWidth, int scaleHeight,
			    boolean texModeRepeat);
extern int  setTextureStage(D3dCtx *d3dCtx,
			    LPDIRECT3DDEVICE8 device, 
			    int mapTexStage,
			    jint texStage);
extern void setTexTransformStageFlag(D3dCtx* d3dCtx,  
				     LPDIRECT3DDEVICE8 device, 
				     int tus, int ts, int genMode); 
#endif

