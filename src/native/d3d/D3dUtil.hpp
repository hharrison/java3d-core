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
#define NOHARDWAREACCEL         26
#define PLEASEUPDATEDRIVERS     27

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
         LPDIRECT3DDEVICE9 device = d3dCtx->pDevice; \
         if (device == NULL)  return;

#define GetCtx2() \
         if (ctx == 0) return 0;\
         D3dCtx *d3dCtx = reinterpret_cast<D3dCtx*>(ctx); \

         
#define GetDevice2()  \
         GetCtx2(); \
         LPDIRECT3DDEVICE9 device = d3dCtx->pDevice; \
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

//ISSUE 135 a iterator to void *
typedef vector<void *>::iterator ITER_VOID;

extern HANDLE hSema; // handle to semaphore
extern BOOL firstError;
extern BOOL debug;

// use for VertexBuffer
extern OSVERSIONINFO osvi; // OS info

extern D3dCtx* findCtx(HWND hwnd);
extern VOID lock();
extern VOID unlock();
extern VOID lockGeometry();
extern VOID unlockGeometry();
extern VOID lockSurfaceList();
extern VOID unlockSurfaceList();
extern VOID freeSurface(LPDIRECT3DBASETEXTURE9 surf);
extern VOID freePointer(void* surf);
extern VOID freePointerList();
extern char* getErrorMessage(int idx);
extern HWND getTopWindow(HWND hwnd);

extern LPDIRECT3DTEXTURE9 createTextureSurface(D3dCtx *d3dCtx,
					       jint numLevels,
					       jint internalFormat,
					       jint width, 
					       jint height,
					       jboolean useAutoMipMap);


extern LPDIRECT3DVOLUMETEXTURE9 createVolumeTexture(D3dCtx *d3dCtx,
						    jint numLevels,
						    jint internalFormat,
						    jint width, 
						    jint height,
						    jint depth,
						    jboolean useAutoMipMap);


extern LPDIRECT3DCUBETEXTURE9 createCubeMapTexture(D3dCtx *d3dCtx,
						   jint numLevels,
						   jint internalFormat,
						   jint width, 
						   jint height,
						   jboolean useAutoMipMap);


extern void copyDataToSurface(jint format, 
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,  jint tilew,
			      jshort *data, LPDIRECT3DTEXTURE9 surf,
			      jint level);

extern void copyDataToSurface(jint format,
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height, jint tilew,
			      jbyte* data, 
			      LPDIRECT3DTEXTURE9 surf,
			      jint level);

extern void copyDataToVolume(jint format, 
			     jint internalFormat,
			     jint xoffset, jint yoffset,
			     jint zoffset,
			     jint imgXOffset, jint imgYOffset,
			     jint imgZOffset,
			     jint width, jint height,  jint depth,
			     jint tilew, jint tileh,
			     jshort *data, LPDIRECT3DVOLUMETEXTURE9 surf,
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
			     LPDIRECT3DVOLUMETEXTURE9 surf,
			     jint level);

extern void copyDataToCubeMap(jint format, 
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,
			      jint tilew, 
			      jshort *data, LPDIRECT3DCUBETEXTURE9 surf,
			      jint level,
			      jint face);


extern void copyDataToCubeMap(jint format,
			      jint internalFormat,
			      jint xoffset, jint yoffset,
			      jint imgXOffset, jint imgYOffset,
			      jint width, jint height,
			      jint tilew, 
			      jbyte* data, 
			      LPDIRECT3DCUBETEXTURE9 surf,
			      jint level,
			      jint face);


extern void copyDepthFromSurface(jint xoffset, jint yoffset,
				 jint subWidth, jint subHeight,
				 jint *data, 
				 LPDIRECT3DSURFACE9 surf);

extern void copyDepthFromSurface(jint xoffset, jint yoffset,
				 jint subWidth, jint subHeight,
				 jfloat *data, 
				 LPDIRECT3DSURFACE9 surf);

extern void copyDepthToSurface(D3dCtx *d3dCtx,
			       LPDIRECT3DDEVICE9 device,
			       jint dst_xoffset, jint dst_yoffset,
			       jint src_xoffset, jint src_yoffset,
			       jint subWidth, jint subHeight,
			       jint src_width, jint src_height,
			       jint *data, 
			       LPDIRECT3DSURFACE9 surf);

extern void copyDepthToSurface(D3dCtx *d3dCtx,
			       LPDIRECT3DDEVICE9 device,
			       jint dst_xoffset, jint dst_yoffset,
			       jint src_xoffset, jint src_yoffset,
			       jint subWidth, jint subHeight,
			       jint src_width, jint src_height,
			       jfloat *data, 
			       LPDIRECT3DSURFACE9 surf);

extern void copyDataFromSurface(jint internalFormat,
				jint xoffset, jint yoffset,
				jint width, jint height, 
				jbyte *data, LPDIRECT3DSURFACE9 surf);

void compositeDataToSurface(jint px, jint py,
			    jint xoffset, jint yoffset,
			    jint subWidth, jint subHeight,
			    jint dataWidth, 
			    jbyte *data, 
			    LPDIRECT3DSURFACE9 surf);

// extern BOOL isIdentity(jdouble *matrix);

extern void CopyTextureStage(LPDIRECT3DDEVICE9 device, 
			     int fromLevel, int toLevel);

extern "C"
DWORD countBits(DWORD mask); // Define in MasterControl.c

extern void throwAssert(JNIEnv *env, char *str);
extern BOOL createQuadIndices(D3dCtx *d3dCtx, int vcount);
extern VOID createLineModeIndexBuffer(D3dCtx *d3dCtx);
extern char *getPixelFormatName(D3DFORMAT format);
extern char *getMultiSampleName(D3DMULTISAMPLE_TYPE mtype); 
extern char *getSwapEffectName(D3DSWAPEFFECT swapEffect);
extern int getPrimitiveNum(int primitive, int vcount);
extern int getMaxNumVertex(int primitive, int vcount);
extern void drawTextureRect(D3dCtx *d3dCtx,
			    LPDIRECT3DDEVICE9 device,
			    LPDIRECT3DTEXTURE9 tex,
			    D3DTLVERTEX screenCoord,
			    int startx, int starty,
			    int endx, int endy,
			    int scaleWidth, int scaleHeight,
			    boolean texModeRepeat);
extern int  setTextureStage(D3dCtx *d3dCtx,
			    LPDIRECT3DDEVICE9 device, 
			    int mapTexStage,
			    jint texStage);
extern void setTexTransformStageFlag(D3dCtx* d3dCtx,  
				     LPDIRECT3DDEVICE9 device, 
				     int tus, int ts, int genMode); 
DWORD ucountBits(DWORD mask) ;
#endif

