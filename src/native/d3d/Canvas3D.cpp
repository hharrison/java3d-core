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

#include "StdAfx.h"


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_setDrawActive(
    JNIEnv *env,
    jobject obj,
    jint fd)
{
    // This function is only used for Solaris OpenGL
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_widSync(
    JNIEnv *env,
    jobject obj,
    jint fd,
    jint numWindows)
{ 
    // This function is only used for Solaris OpenGL
}



extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativePipeline_useSharedCtx(
    JNIEnv *env, 
    jobject obj)
{
    return JNI_FALSE;
}



extern "C" JNIEXPORT
jlong JNICALL Java_javax_media_j3d_NativePipeline_createNewContext(
    JNIEnv  *env,
    jobject  obj,
    jobject  cv,
    jlong    display,
    jlong    window,
    jlong    fbConfigListPtr,
    jlong    sharedCtx,
    jboolean isSharedCtx,
    jboolean offScreen,
	jboolean glslLibraryAvailable,
    jboolean cgLibraryAvailable)
{
    HWND hwnd = WindowFromDC(reinterpret_cast<HDC>(window));

    lock();
    int vid = 0; // TODO: get needed info from fbConfigListPtr
    D3dCtx* ctx = new D3dCtx(env, cv, hwnd, offScreen, vid);
    if (ctx == NULL) {
	printf("%s", getErrorMessage(OUTOFMEMORY));
	unlock();
	return 0;
    }

    if (offScreen) 
	{
	  jclass cls = (jclass) env->GetObjectClass(cv);
	  jfieldID fieldId = env->GetFieldID(cls,
					   "offScreenCanvasSize", 
					   "Ljava/awt/Dimension;");
	  jobject dimObj = env->GetObjectField(cv, fieldId);
	  if (dimObj == NULL) 
	   {
	     // user invoke queryProperties()
	     ctx->offScreenWidth = 1;
	     ctx->offScreenHeight = 1;
	   } 
	  else 
	   {
	     cls = (jclass) env->GetObjectClass(dimObj);
	     fieldId = env->GetFieldID(cls, "width", "I");
	     ctx->offScreenWidth = env->GetIntField(dimObj, fieldId);
	     fieldId = env->GetFieldID(cls, "height", "I");
	     ctx->offScreenHeight = env->GetIntField(dimObj, fieldId);
	   }
    }

    if (!ctx->initialize(env, cv)) 
	{
	 delete ctx;
	 unlock();
	 return 0;
    } 
    d3dCtxList.push_back(ctx);

    unlock();
    return reinterpret_cast<jlong>(ctx);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_createQueryContext(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong display,
    jlong window,
    jlong fbConfigListPtr,
    jboolean offScreen,
    jint width,
    jint height,
	jboolean glslLibraryAvailable,
    jboolean cgLibraryAvailable)
{
    HWND hwnd = WindowFromDC(reinterpret_cast<HDC>(window));

    lock();
    // always use offscreen for property since it
    // makes no difference in D3D and this will also
    // instruct initialize() to use offScreenWidth/Height
    // instead of current window width/height to create
    // context.

    int vid = 0; // TODO: get needed info from fbConfigListPtr
    D3dCtx* ctx = new D3dCtx(env, cv, hwnd, true, vid);
    if (ctx == NULL) {
	printf("%s", getErrorMessage(OUTOFMEMORY));
	unlock();
	return;
    }

    ctx->offScreenWidth = width;
    ctx->offScreenHeight = height;

    ctx->initialize(env, cv);
    delete ctx;
    unlock();
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativePipeline_useCtx(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx, 
    jlong display, 
    jlong window)
{
	return JNI_TRUE;   
	// D3D doesn't have notation of current context
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativePipeline_getNumCtxLights(
    JNIEnv *env, 
    jobject obj,
    jlong ctx)
{
   GetDevice2();

   int nlight = d3dCtx->deviceInfo->maxActiveLights;
   if (nlight <= 0) {
       // In emulation & referene mode, D3D return -1 
       // work around by setting 8.
       nlight = 8;
   }
   return nlight;
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativePipeline_initTexturemapping(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint texWidth,
    jint texHeight,
    jint objectId)
{
    GetCtx2();


    if ((objectId >= 0) &&
	(objectId < d3dCtx->textureTableLen) &&
	(d3dCtx->textureTable[objectId] != NULL)) {
	// delete the previous texture reference
	// when canvas resize
	 Java_javax_media_j3d_NativePipeline_freeTexture(env,
						   NULL,
						   ctx,
						   objectId);
    }

    Java_javax_media_j3d_NativePipeline_bindTexture2D(env, texture, ctx, objectId, TRUE);
    
    Java_javax_media_j3d_NativePipeline_updateTexture2DImage(env, texture, ctx, 1, 0, 
							     J3D_RGBA, 0, texWidth, 
							     texHeight, 0, 0, NULL);
    return (d3dCtx->textureTable[objectId] != NULL);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_texturemapping(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint px,
    jint py,
    jint minX,
    jint minY,
    jint maxX,
    jint maxY,
    jint texWidth,
    jint texHeight,
    jint rasWidth,
    jint format,
    jint objectId,
    jbyteArray byteData,
    jint winWidth,
    jint winHeight)
{
    GetDevice();

    Java_javax_media_j3d_NativePipeline_bindTexture2D(
	 env, texture, ctx, objectId, TRUE);

    // TODO --- Need to re-write.  Chien
    printf("[TODO NEEDED] Canvas3dD : *** texturemapping() ***\n");
    Java_javax_media_j3d_NativePipeline_updateTexture2DSubImage(
         env, texture, ctx, 0, minX, minY, J3D_RGBA, format,
	 minX, minY, rasWidth, maxX-minX, maxY-minY, IMAGE_DATA_TYPE_BYTE_ARRAY,
	 byteData);

    LPDIRECT3DTEXTURE9 surf = d3dCtx->textureTable[objectId];

    if (surf == NULL) {
	if (debug) {
	    printf("[Java 3D] Fail to apply texture in J3DGraphics2D !\n");
	}
	return;
    }

    D3DTLVERTEX screenCoord;	
    DWORD zcmpfunc;

    screenCoord.sx = (px + minX) - 0.5f;
    screenCoord.sy = (py + minY) - 0.5f;

    // sz can be any number since we will disable z buffer
    // However rhw can't be 0, otherwise texture will not shown
    screenCoord.sz = 0.999f;
    screenCoord.rhw = 1;

    DWORD blendEnable;
    DWORD srcBlend;
    DWORD dstBlend;

    // disable z buffer
    device->GetRenderState(D3DRS_ZFUNC, &zcmpfunc);
    device->SetRenderState(D3DRS_ZWRITEENABLE, FALSE);
    device->SetRenderState(D3DRS_ZFUNC, D3DCMP_ALWAYS);

    device->GetRenderState(D3DRS_ALPHABLENDENABLE, &blendEnable);
    device->GetRenderState(D3DRS_SRCBLEND,  &srcBlend);
    device->GetRenderState(D3DRS_DESTBLEND, &dstBlend);

    device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
    device->SetRenderState(D3DRS_SRCBLEND,  D3DBLEND_SRCALPHA);
    device->SetRenderState(D3DRS_DESTBLEND, D3DBLEND_INVSRCALPHA);

    drawTextureRect(d3dCtx, device, surf, screenCoord,
		    minX, minY, maxX, maxY,
		    maxX - minX, maxY - minY, false); 

    Java_javax_media_j3d_NativePipeline_bindTexture2D(
	 env, texture, ctx, objectId, FALSE);

    device->SetRenderState(D3DRS_ALPHABLENDENABLE, blendEnable);
    device->SetRenderState(D3DRS_SRCBLEND,  srcBlend);
    device->SetRenderState(D3DRS_DESTBLEND, dstBlend);
    device->SetRenderState(D3DRS_ZFUNC, zcmpfunc);
    device->SetRenderState(D3DRS_ZWRITEENABLE, 
			   d3dCtx->zWriteEnable);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_clear(
							JNIEnv *env,
							jobject obj,
							jlong ctx,
							jfloat r, 
							jfloat g, 
							jfloat b) 
{

    GetDevice();
    
    /* Java 3D always clears the Z-buffer */
    /* @TODO check same operation for stencil */
    
    if (!d3dCtx->zWriteEnable) {
	device->SetRenderState(D3DRS_ZWRITEENABLE, TRUE);
    } 
    
    /* clear stencil, if in used */    
    if (d3dCtx->stencilWriteEnable ) {
	// clear stencil and ZBuffer
	HRESULT hr = device->Clear(0, NULL, 
				   D3DCLEAR_STENCIL | D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 
				   D3DCOLOR_COLORVALUE(r, g, b, 1.0f), 1.0, 0);
	if (hr == D3DERR_INVALIDCALL) {
	    printf("[Java3D] Error cleaning Canvas3D stencil & ZBuffer\n");
	}
	//  printf("canvas3D clear stencil & ZBuffer\n");
    }
    else {	
	// clear ZBuffer only
	HRESULT hr =  device->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 
				    D3DCOLOR_COLORVALUE(r, g, b, 1.0f), 1.0, 0);
	if (hr == D3DERR_INVALIDCALL) {
	    printf("[Java3D] Error cleaning Canvas3D ZBuffer\n");
	}
	// printf("canvas3D clear ZBuffer\n");
    }
    
    if (!d3dCtx->zWriteEnable) {
	device->SetRenderState(D3DRS_ZWRITEENABLE, FALSE);
    }	
    // disable stencil 
    if (d3dCtx->stencilEnable && !d3dCtx->stencilWriteEnable) {
	device->SetRenderState(D3DRS_STENCILENABLE, FALSE);		  
    }
    
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_textureFillBackground(
							JNIEnv *env,
							jobject obj,
							jlong ctx,
							jfloat texMinU, 
							jfloat texMaxU, 
							jfloat texMinV, 
							jfloat texMaxV, 
							jfloat mapMinX, 
							jfloat mapMaxX, 
							jfloat mapMinY,
							jfloat mapMaxY)
{
    GetDevice();
    /* printf("Canvas3D.textureFillBackground()\n"); */
    /* TODO : Implement textureFillBackground() */
    printf("[TODO NEEDED] Canvas3D : *** textureFillBackground is not implemented yet.\n");


}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_textureFillRaster(JNIEnv *env,
							jobject obj,
							jlong ctx,
							jfloat texMinU, 
							jfloat texMaxU, 
							jfloat texMinV, 
							jfloat texMaxV, 
							jfloat mapMinX, 
							jfloat mapMaxX, 
							jfloat mapMinY,
							jfloat mapMaxY,
                                                        jfloat mapZ,
                                                        jfloat alpha)
{ 
    GetDevice();
    /* printf("Canvas3D.textureFillRaster()\n"); */
    /* TODO : Implement textureFillRaster() */
    printf("[TODO NEEDED] Canvas3D : *** textureFillRaster is not implemented yet.\n");

}


JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_executeRasterDepth(JNIEnv *env,
							jobject obj,
							jlong ctx,
							jfloat posX, 
							jfloat posY, 
							jfloat posZ, 
							jint srcOffsetX, 
							jint srcOffsetY, 
							jint rasterWidth, 
							jint rasterHeight,
							jint depthWidth,
                                                        jint depthHeight,
                                                        jint depthFormat,
                                                        jobject depthData)
{ 
    GetDevice();
    /* printf("Canvas3D.executeRasterDepth()\n"); */
    /* TODO : Implement executeRasterDepth() */
    printf("[TODO NEEDED] Canvas3D : *** : executeRasterDepth is not implemented yet.\n");

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_setRenderMode(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jint mode,
    jboolean dbEnable)
{
    // D3D v8.0 doesn't support stereo
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_clearAccum(
    JNIEnv *env, 
    jobject obj,
    jlong ctx)
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which it didn't support)
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_accum(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jfloat value)
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which didn't support)
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_accumReturn(
    JNIEnv *env, 
    jobject obj,
    jlong ctx) 
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which it didn't support)
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_setDepthBufferWriteEnable(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jboolean mode)
{
    GetDevice();

    d3dCtx->zWriteEnable = mode;
    device->SetRenderState(D3DRS_ZWRITEENABLE, mode);
}


VOID freePointerList()
{
   if (useFreePointerList0) {
	if (freePointerList1.size() > 0) {
	    lockSurfaceList();
	    for (ITER_VOID p = freePointerList1.begin();  
		 p != freePointerList1.end(); ++p) {
		delete (*p);
	    }

	    freePointerList1.clear();
	    unlockSurfaceList();
	}
	useFreePointerList0 = false;
    } else {
	if (freePointerList0.size() > 0) {
	    lockSurfaceList();
	    for (ITER_VOID p = freePointerList0.begin();  
		 p != freePointerList0.end(); ++p) {
		delete (*p);
	    }

	    freePointerList0.clear();
	    unlockSurfaceList();
	}
	useFreePointerList0 = true;

    }
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativePipeline_swapBuffers(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx,
    jlong display,
    jlong window)
{
    GetDevice2();

    int retCode = NOCHANGE;

    HRESULT hr = device->Present(NULL, NULL, NULL, NULL);

    if (FAILED(hr)) {
	hr = device->TestCooperativeLevel();
	if (D3DERR_DEVICELOST == hr) {
	    return NOCHANGE;
	}
	if (D3DERR_DEVICENOTRESET == hr) {
	    if (debug) {
		printf("Buffer swap error %s, try Reset() the surface... \n",
		       DXGetErrorString9(hr));	    
	    }
	    retCode = d3dCtx->resetSurface(env, cv);
	    GetDevice2();
	    hr = device->Present(NULL, NULL, NULL, NULL);
	    if (FAILED(hr)) {
		if (debug) {
		    printf("Buffer swap error %s \n",
			   DXGetErrorString9(hr));
		}
	    }
	} 

    }

    d3dCtx->freeList();
    freePointerList();
    return retCode;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_syncRender(
      JNIEnv *env, 
      jobject obj, 
      jlong ctx,
      jboolean waitFlag)  
{
    // do nothing since D3D always wait in Blt
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_newDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint id)
{
    GetCtx();

    if (id <= 0) {
	if (debug) {
	    printf("In Canvas3D.newDisplayList id pass in = %d !\n", id);
	}
	return;
    }

    if (id >= d3dCtx->dlTableSize) {
	int newSize = d3dCtx->dlTableSize << 1;
	if (id >= newSize) {
	    newSize = id+1;
	}
	int i=0; 
	LPD3DDISPLAYLIST *newTable =  new LPD3DDISPLAYLIST[newSize];

	if (newTable == NULL) {
	    printf("%s", getErrorMessage(OUTOFMEMORY));
	    exit(1);
	}
	// entry 0 is not used
	newTable[0] = NULL;
	while (++i < d3dCtx->dlTableSize) {
	    newTable[i] = d3dCtx->displayListTable[i];
	}
	while (i < newSize) {
	    newTable[i++] = NULL;
	}
	d3dCtx->dlTableSize = newSize;	
	SafeDelete(d3dCtx->displayListTable);
	d3dCtx->displayListTable = newTable;
    }

    if (d3dCtx->displayListTable[id] != NULL) {
	SafeDelete(d3dCtx->displayListTable[id]);
    }
    d3dCtx->displayListTable[id] = new D3dDisplayList();
    if (d3dCtx->displayListTable[id] == NULL) {
	printf("%s", getErrorMessage(OUTOFMEMORY));
	exit(1);
    }
    d3dCtx->currDisplayListID = id;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_endDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();
    d3dCtx->displayListTable[d3dCtx->currDisplayListID]->optimize(d3dCtx);
    d3dCtx->currDisplayListID = 0;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_callDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint id,
    jboolean isNonUniformScale)
{
    GetDevice();
    
    // TODO: Remove this two safe checks when release
    //  d3dCtx->displayListTable[id]->render(d3dCtx);
     

    if ((id <= 0) || (id >= d3dCtx->dlTableSize)) {
	if (debug) {
	    if (id <= 0) {
		printf("[Java 3D] Invalid Display List ID %d is invoked !\n", id);
	    } else {
		printf("[Java 3D] Display List ID %d not yet initialize !\n", id);
	    }
	}
	return;
    }

    LPD3DDISPLAYLIST dl = d3dCtx->displayListTable[id];

    if (dl == NULL) {
	if (debug) {
	    printf("[Java 3D] Display List ID %d not yet initialize !\n", id);
	}
	return;
    }
    dl->render(d3dCtx);

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_freeDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint id)
{
    GetCtx();

    if ((id < 0) || (id >= d3dCtx->dlTableSize)) {
	if (debug) {
	    printf("[Java 3D] FreeDisplayList, id %d not within table range %d!\n", id,
		   d3dCtx->dlTableSize);
	}
	return;
    }

    SafeDelete(d3dCtx->displayListTable[id]);
}


/* 
   Native function to delete OGL texture object after j3d texture object
   has been deleted by java garbage collector.
 */
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_freeTexture(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint id)
{
    GetDevice();

    for (int i=0; i < d3dCtx->bindTextureIdLen; i++) {    
	if (d3dCtx->bindTextureId[i] == id) {
	    device->SetTexture(i, NULL);
	    d3dCtx->bindTextureId[i] = -1;
	}
    }

    if ((id >= d3dCtx->textureTableLen) || (id < 1)) {
	if (debug) {
	    printf("Internal Error : freeTexture ID %d, textureTableLen %d \n", 
		   id, d3dCtx->textureTableLen);
	}
	return;
    }

    d3dCtx->freeResource(d3dCtx->textureTable[id]);
    d3dCtx->textureTable[id] = NULL;
}


extern "C" JNIEXPORT 
jint JNICALL Java_javax_media_j3d_NativePipeline_getTextureUnitCount(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetCtx2();
    return d3dCtx->deviceInfo->maxTextureUnitStageSupport;
}


extern "C" JNIEXPORT
jlong JNICALL Java_javax_media_j3d_NativePipeline_createOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx,
    jlong display,
    jlong fbConfigListPtr,
    jint width,
    jint height)
{

    
    if (ctx == 0) {
	// createContext() will be invoked later in Renderer
	return 1;
    }  else {
	GetCtx2();
	d3dCtx->d3dPresent.BackBufferWidth = width;
	d3dCtx->d3dPresent.BackBufferHeight = height;
	return SUCCEEDED(d3dCtx->resetSurface(env, cv));
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_destroyContext(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jlong window,
    jlong ctx)
{
    GetDevice();

    lock();
    d3dCtxList.erase(find(d3dCtxList.begin(), d3dCtxList.end(), d3dCtx));
    delete d3dCtx;
    unlock();

    Java_javax_media_j3d_NativePipeline_cleanupRenderer(env, obj);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_destroyOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx,
    jlong display,
    jlong fbConfigListPtr,
    jlong window)
{
    // do nothing, since the old buffer will destory 
    // in createOffScreenBuffer

    // TODO : this means that we will hold onto the last off-screen buffer;
    // we should clean this up at some point
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_readOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx,
    jint format,
    jint dataType,
    jobject data,
    jint width,
    jint height)
{
    void *imageObjPtr;

    GetDevice();

    if (format == IMAGE_FORMAT_USHORT_GRAY) {
	printf("[Java 3D] readOffScreenBuffer not support IMAGE_FORMAT_USHORT_GRAY\n");
	return;
    }

    if (d3dCtx->backSurface == NULL) {
	HRESULT hr = device->GetBackBuffer(0,0, D3DBACKBUFFER_TYPE_MONO, //iSwapChain is 0
					   &d3dCtx->backSurface);
	if (FAILED(hr)) {
	    printf("[Java 3D] GetBackBuffer fail %s\n",
		   DXGetErrorString9(hr));
	    return;
	}
    }

    if((dataType == IMAGE_DATA_TYPE_BYTE_ARRAY) || (dataType == IMAGE_DATA_TYPE_INT_ARRAY)) {
	imageObjPtr = (void *)env->GetPrimitiveArrayCritical((jarray)data, NULL);
    }
    else {
       imageObjPtr = (void *)env->GetDirectBufferAddress(data);
    }
    
    /* TODO : Need to re-write --- Chien. */
    printf("[TODO NEEDED] Canvas3D : *** readOffScreenBuffer() ***\n");
    copyDataFromSurface(format, 0, 0, width, height, (jbyte *)imageObjPtr, 
			d3dCtx->backSurface);

    if((dataType == IMAGE_DATA_TYPE_BYTE_ARRAY) || (dataType == IMAGE_DATA_TYPE_INT_ARRAY)) {
        env->ReleasePrimitiveArrayCritical((jarray)data, imageObjPtr, 0);
    }
    return;
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativePipeline_resizeD3DCanvas(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx)
{
    int status;

    GetCtx2();
    lock();
    status = d3dCtx->resize(env, cv);
    unlock();

    return status;
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativePipeline_toggleFullScreenMode(
    JNIEnv *env,
    jobject obj,
    jobject cv,
    jlong ctx)
{
    int status;

    GetCtx2();
    lock();
    status = d3dCtx->toggleMode(!d3dCtx->bFullScreen, env, cv);
    unlock();  
    if (status == RECREATEDFAIL) {
	return RECREATEDDRAW;
    }
    return status;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_setFullSceneAntialiasing(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jboolean enable)
{
    GetDevice();

    if (!implicitMultisample) {
	device->SetRenderState(D3DRS_MULTISAMPLEANTIALIAS, enable);
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_cleanupRenderer(
    JNIEnv *env,
    jobject obj)
{
    lock();
    if (d3dCtxList.empty()) {
	D3dDriverInfo::release();
    }
    unlock();

    // Need to call it two times to free both list0 and list1
    freePointerList();
    freePointerList();
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_beginScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctx)
{
    GetDevice();
    device->BeginScene();
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_endScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctx)
{
    GetDevice();
    device->EndScene();
}


extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativePipeline_validGraphicsMode(
       JNIEnv *env,
       jobject obj) 
{
    DEVMODE devMode;
    
    EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &devMode);
    return (devMode.dmBitsPerPel > 8);
}
