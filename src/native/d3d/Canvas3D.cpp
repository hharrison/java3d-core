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


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setDrawActive(
    JNIEnv *env,
    jobject obj,
    jint fd)
{
    // This function is only used for Solaris OpenGL
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_widSync(
    JNIEnv *env,
    jobject obj,
    jint fd,
    jint numWindows)
{
    // This function is only used for Solaris OpenGL
}



extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_useSharedCtx(
    JNIEnv *env, 
    jobject obj)
{
    return JNI_FALSE;
}



extern "C" JNIEXPORT
jlong JNICALL Java_javax_media_j3d_Canvas3D_createNewContext(
    JNIEnv  *env,
    jobject  obj,
    jlong    display,
    jint     window, 
    jint     vid,    
    jlong    fbConfigListPtr,
    jlong    sharedCtx,
    jboolean isSharedCtx,
    jboolean offScreen)
{
    HWND hwnd = WindowFromDC(reinterpret_cast<HDC>(window));

    lock();
    D3dCtx* ctx = new D3dCtx(env, obj, hwnd, offScreen, vid);
    if (ctx == NULL) {
	printf("%s", getErrorMessage(OUTOFMEMORY));
	unlock();
	return 0;
    }

    if (offScreen) {

	jclass cls = (jclass) env->GetObjectClass(obj);
	jfieldID fieldId = env->GetFieldID(cls,
					   "offScreenCanvasSize", 
					   "Ljava/awt/Dimension;");
	jobject dimObj = env->GetObjectField(obj, fieldId);
	if (dimObj == NULL) {
	    // user invoke queryProperties()
	    ctx->offScreenWidth = 1;
	    ctx->offScreenHeight = 1;
	} else {
	    cls = (jclass) env->GetObjectClass(dimObj);
	    fieldId = env->GetFieldID(cls, "width", "I");
	    ctx->offScreenWidth = env->GetIntField(dimObj, fieldId);
	    fieldId = env->GetFieldID(cls, "height", "I");
	    ctx->offScreenHeight = env->GetIntField(dimObj, fieldId);
	}
    }

    if (!ctx->initialize(env, obj)) {
	delete ctx;
	unlock();
	return 0;
    } 
    d3dCtxList.push_back(ctx);

    unlock();
    return reinterpret_cast<jlong>(ctx);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_createQueryContext(
    JNIEnv *env,
    jobject obj,
    jlong display,
    jint window,
    jint vid,
    jlong fbConfigListPtr,
    jboolean offScreen,
    jint width,
    jint height)
{
    HWND hwnd = WindowFromDC(reinterpret_cast<HDC>(window));

    lock();
    // always use offscreen for property since it
    // makes no difference in D3D and this will also
    // instruct initialize() to use offScreenWidth/Height
    // instead of current window width/height to create
    // context.

    D3dCtx* ctx = new D3dCtx(env, obj, hwnd, true, vid);
    if (ctx == NULL) {
	printf("%s", getErrorMessage(OUTOFMEMORY));
	unlock();
	return;
    }

    ctx->offScreenWidth = width;
    ctx->offScreenHeight = height;

    ctx->initialize(env, obj);
    delete ctx;
    unlock();
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_useCtx(
    JNIEnv *env, 
    jclass cl, 
    jlong ctx, 
    jlong display, 
    jint window)
{
    // D3D doesn't have notation of current context
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_getNumCtxLights(
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
void JNICALL Java_javax_media_j3d_Canvas3D_composite(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint px,
    jint py,
    jint minX,
    jint minY,
    jint maxX,
    jint maxY,
    jint rasWidth,
    jbyteArray imageYdown,
    jint winWidth,
    jint winHeight)

{
    GetDevice();

    // However we use the following texturemapping function instead
    // so this will not invoke.
    if (d3dCtx->backSurface == NULL) {
	device->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO,
			      &d3dCtx->backSurface);
    }    
    jbyte *byteData = (jbyte *) (env->GetPrimitiveArrayCritical(
					      imageYdown,   NULL));
    compositeDataToSurface(px, py,
			   minX, minY, maxX-minX, maxY-minY,
			   rasWidth,
			   byteData, d3dCtx->backSurface);
    env->ReleasePrimitiveArrayCritical(imageYdown, byteData, 0);
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_initTexturemapping(
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
	 Java_javax_media_j3d_Canvas3D_freeTexture(env,
						   NULL,
						   ctx,
						   objectId);
    }

    Java_javax_media_j3d_TextureRetained_bindTexture(
	 env, texture, ctx, objectId, TRUE);

     Java_javax_media_j3d_TextureRetained_updateTextureImage(
         env, texture, ctx, 1, 0, J3D_RGBA, 0, texWidth, texHeight, 0, NULL);
     
     return (d3dCtx->textureTable[objectId] != NULL);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_texturemapping(
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
    jbyteArray imageYdown,
    jint winWidth,
    jint winHeight)
{
    GetDevice();

    Java_javax_media_j3d_TextureRetained_bindTexture(
	 env, texture, ctx, objectId, TRUE);


    Java_javax_media_j3d_Texture2DRetained_updateTextureSubImage(
         env, texture, ctx, 0, minX, minY, J3D_RGBA, format,
	 minX, minY, rasWidth, maxX-minX, maxY-minY, imageYdown);

    LPDIRECT3DTEXTURE8 surf = d3dCtx->textureTable[objectId];

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

    Java_javax_media_j3d_TextureRetained_bindTexture(
	 env, texture, ctx, objectId, FALSE);

    device->SetRenderState(D3DRS_ALPHABLENDENABLE, blendEnable);
    device->SetRenderState(D3DRS_SRCBLEND,  srcBlend);
    device->SetRenderState(D3DRS_DESTBLEND, dstBlend);
    device->SetRenderState(D3DRS_ZFUNC, zcmpfunc);
    device->SetRenderState(D3DRS_ZWRITEENABLE, 
			   d3dCtx->zWriteEnable);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_clear(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat r,
    jfloat g,
    jfloat b,
    jint winWidth,
    jint winHeight,
    jobject pa2d,
    jint imageScaleMode, 
    jbyteArray pixels_obj)
{

    GetDevice();

 /* Java 3D always clears the Z-buffer */

    if (!d3dCtx->zWriteEnable) {
	device->SetRenderState(D3DRS_ZWRITEENABLE, TRUE);
    } 

    device->Clear(0, NULL, D3DCLEAR_TARGET | D3DCLEAR_ZBUFFER, 
		  D3DCOLOR_COLORVALUE(r, g, b, 1.0f), 1.0, 0);

    if (pa2d) {
	jclass pa2d_class = env->GetObjectClass(pa2d);
	/*
	jfieldID id = env->GetFieldID(pa2d_class, "surfaceDirty", "I");
	if (env->GetIntField(pa2d, id) == NOTLIVE) {
	    return;
	}
	*/
	// It is possible that (1) Another user thread will free this
	// image. (2) Another Renderer thread in case of multiple screen
	// will invoke clear() at the same time.
	lockBackground();


	jfieldID id = env->GetFieldID(pa2d_class, "hashId", "I");
	int hashCode = env->GetIntField(pa2d, id);	

	D3dImageComponent *d3dImage =
	    D3dImageComponent::find(&BackgroundImageList, d3dCtx, hashCode);

	id = env->GetFieldID(pa2d_class, "width", "I");
	int width = env->GetIntField(pa2d, id);
	id = env->GetFieldID(pa2d_class, "height", "I");
	int height = env->GetIntField(pa2d, id);
	
	LPDIRECT3DTEXTURE8 surf;

	if ((d3dImage == NULL) || (d3dImage->surf == NULL)) {
	    surf = createSurfaceFromImage(env, pa2d, ctx,
					   width, height, pixels_obj);
	     if (surf == NULL) {
		 if (d3dImage != NULL) {
		     D3dImageComponent::remove(&BackgroundImageList, d3dImage);
		 }
		 unlockBackground();
		 return;
	     }

	     if (d3dImage == NULL) {
		 d3dImage = 
		     D3dImageComponent::add(&BackgroundImageList, d3dCtx, hashCode, surf);
	     } else {
		 // need to add this one because the new imageDirtyFlag may
		 // cause d3dImage->surf set to NULL
		 d3dImage->surf = surf;
	     } 
        }


	D3DTLVERTEX screenCoord;	
	DWORD zcmpfunc;
	boolean texModeRepeat;
	int scaleWidth, scaleHeight;
	float sw, sh;

	// sz can be any number since we already disable z buffer
	// However rhw can't be 0, otherwise texture will not shown
	screenCoord.sz = 0.999f;
	screenCoord.rhw = 1;

	// disable z buffer
	device->GetRenderState(D3DRS_ZFUNC, &zcmpfunc);
	device->SetRenderState(D3DRS_ZWRITEENABLE, FALSE);
	device->SetRenderState(D3DRS_ZFUNC, D3DCMP_ALWAYS);

	switch (imageScaleMode){
	case javax_media_j3d_Background_SCALE_NONE:
	    screenCoord.sx = -0.5f;
	    screenCoord.sy = -0.5f;
	    scaleWidth = width;
	    scaleHeight = height;
	    texModeRepeat = FALSE;
	    break;
	case javax_media_j3d_Background_SCALE_FIT_MIN:
	    screenCoord.sx = -0.5f;
	    screenCoord.sy = -0.5f;
	    sw = winWidth/(float) width;
	    sh = winHeight/(float) height;
	    if (sw >= sh) {
		scaleWidth = width*sh;
		scaleHeight = winHeight;
	    } else {
		scaleWidth = winWidth;
		scaleHeight = height*sw;		
	    }
	    texModeRepeat = FALSE;	    
	    break;
	case javax_media_j3d_Background_SCALE_FIT_MAX:
	    screenCoord.sx = -0.5f;
	    screenCoord.sy = -0.5f;
	    sw = winWidth/(float) width;
	    sh = winHeight/(float) height;
	    if (sw >= sh) {
		scaleWidth = winWidth;
		scaleHeight = height*sw;		
	    } else {
		scaleWidth = width*sh;
		scaleHeight = winHeight;		
	    }
	    texModeRepeat = FALSE;	    
	    break;	
	case javax_media_j3d_Background_SCALE_FIT_ALL:
	    screenCoord.sx = -0.5f;
	    screenCoord.sy = -0.5f;
	    scaleWidth = winWidth;
	    scaleHeight = winHeight;
	    texModeRepeat = FALSE;	    
	    break;
	case javax_media_j3d_Background_SCALE_REPEAT:	  
	    screenCoord.sx = -0.5f;
	    screenCoord.sy = -0.5f;
	    scaleWidth = winWidth;
	    scaleHeight = winHeight;
	    texModeRepeat = TRUE;
	    break;	    
	case javax_media_j3d_Background_SCALE_NONE_CENTER:
	    screenCoord.sx = (winWidth - width)/2.0f - 0.5f;
	    screenCoord.sy = (winHeight - height)/2.0f -0.5f;
	    scaleWidth = width;
	    scaleHeight = height;
	    texModeRepeat = FALSE;
	    break;
 	default:
	    printf("Unknown Background scale mode %d\n", imageScaleMode);
	}

	drawTextureRect(d3dCtx, device, d3dImage->surf,
			screenCoord, 0, 0, width, height, 
			scaleWidth, scaleHeight, texModeRepeat);

	device->SetRenderState(D3DRS_ZFUNC, zcmpfunc);
	device->SetRenderState(D3DRS_ZWRITEENABLE, 
			       d3dCtx->zWriteEnable);
	unlockBackground();
    } else {
	if (!d3dCtx->zWriteEnable) {
	    device->SetRenderState(D3DRS_ZWRITEENABLE, FALSE);
	}
    }


}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setRenderMode(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jint mode,
    jboolean dbEnable)
{
    // D3D v8.0 doesn't support stereo
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_clearAccum(
    JNIEnv *env, 
    jobject obj,
    jlong ctx)
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which it didn't support)
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_accum(
    JNIEnv *env, 
    jobject obj, 
    jlong ctx,
    jfloat value)
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which didn't support)
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_accumReturn(
    JNIEnv *env, 
    jobject obj,
    jlong ctx) 
{
    // D3D use full-scene antialiasing capbilities in device
    // instead of accumulation buffer (which it didn't support)
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setDepthBufferWriteEnable(
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
	    for (void **p = freePointerList1.begin(); 
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
	    for (void **p = freePointerList0.begin(); 
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
jint JNICALL Java_javax_media_j3d_Canvas3D_swapBuffers(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jlong display,
    jint win)
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
		       DXGetErrorString8(hr));	    
	    }
	    retCode = d3dCtx->resetSurface(env, obj);
	    GetDevice2();
	    hr = device->Present(NULL, NULL, NULL, NULL);
	    if (FAILED(hr)) {
		if (debug) {
		    printf("Buffer swap error %s \n",
			   DXGetErrorString8(hr));
		}
	    }
	} 

    }

    d3dCtx->freeList();
    freePointerList();
    return retCode;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_syncRender(
      JNIEnv *env, 
      jobject obj, 
      jlong ctx,
      jboolean waitFlag)  
{
    // do nothing since D3D always wait in Blt
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_newDisplayList(
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
void JNICALL Java_javax_media_j3d_Canvas3D_endDisplayList(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();
    d3dCtx->displayListTable[d3dCtx->currDisplayListID]->optimize(d3dCtx);
    d3dCtx->currDisplayListID = 0;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_callDisplayList(
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
void JNICALL Java_javax_media_j3d_Canvas3D_freeDisplayList(
    JNIEnv *env,
    jclass cl,
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
void JNICALL Java_javax_media_j3d_Canvas3D_freeTexture(
    JNIEnv *env,
    jclass cls,
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
jboolean JNICALL Java_javax_media_j3d_Canvas3D_isTexture3DAvailable(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    return JNI_FALSE;
}


extern "C" JNIEXPORT 
jint JNICALL Java_javax_media_j3d_Canvas3D_getTextureColorTableSize(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    // Not support by D3D
    return 0;
}


extern "C" JNIEXPORT 
jint JNICALL Java_javax_media_j3d_Canvas3D_getTextureUnitCount(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetCtx2();
    return d3dCtx->deviceInfo->maxTextureUnitStageSupport;
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_createOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jlong display,
    jint window,
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
	return SUCCEEDED(d3dCtx->resetSurface(env, obj));
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_destroyContext(
    JNIEnv *env,
    jclass cl,
    jlong display,
    jint window,
    jlong ctx)
{
    GetDevice();

    lock();
    d3dCtxList.erase(find(d3dCtxList.begin(), d3dCtxList.end(), d3dCtx));
    delete d3dCtx;
    unlock();

    Java_javax_media_j3d_Renderer_D3DCleanUp(env, cl);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_destroyOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jlong display,
    jlong fbConfigListPtr,
    jint window)
{
    // do nothing, since the old buffer will destory 
    // in createOffScreenBuffer
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_readOffScreenBuffer(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint format,
    jint width,
    jint height)
{
    GetDevice();

    if (format == FORMAT_USHORT_GRAY) {
	printf("[Java 3D] readOffScreenBuffer not support FORMAT_USHORT_GRAY\n");
	return;
    }

    if (d3dCtx->backSurface == NULL) {
	HRESULT hr = device->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO,
					   &d3dCtx->backSurface);
	if (FAILED(hr)) {
	    printf("[Java 3D] GetBackBuffer fail %s\n",
		   DXGetErrorString8(hr));
	    return;
	}
    }

    jclass cv_class =  env->GetObjectClass(obj);

    jfieldID byteData_field = env->GetFieldID(cv_class, "byteBuffer", "[B");
    jbyteArray byteData_array = (jbyteArray) env->GetObjectField(obj, byteData_field);
    jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(
					       byteData_array, NULL);

    copyDataFromSurface(format, 0, 0, width, height, byteData, 
			d3dCtx->backSurface);

    env->ReleasePrimitiveArrayCritical(byteData_array, byteData, 0);
    return;
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_resizeD3DCanvas(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    int status;

    GetCtx2();
    lock();
    status = d3dCtx->resize(env, obj);
    unlock();

    return status;
}


extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_Canvas3D_toggleFullScreenMode(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    int status;

    GetCtx2();
    lock();
    status = d3dCtx->toggleMode(!d3dCtx->bFullScreen, env, obj);
    unlock();  
    if (status == RECREATEDFAIL) {
	return RECREATEDDRAW;
    }
    return status;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setFullSceneAntialiasing(
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
void JNICALL Java_javax_media_j3d_Renderer_D3DCleanUp(
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
void JNICALL Java_javax_media_j3d_ImageComponent2DRetained_freeD3DSurface(
    JNIEnv *env,
    jobject image,
    jint hashCode)

{

    lockImage();
    D3dImageComponent::remove(&RasterList, hashCode);
    unlockImage();
    lockBackground();
    D3dImageComponent::remove(&BackgroundImageList, hashCode);
    unlockBackground();
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_beginScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctx)
{
    GetDevice();
    device->BeginScene();
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_endScene(
       JNIEnv *env,
       jobject obj, 
       jlong ctx)
{
    GetDevice();
    device->EndScene();
}


extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_validGraphicsMode(
       JNIEnv *env,
       jobject obj) 
{
    DEVMODE devMode;
    
    EnumDisplaySettings(NULL, ENUM_CURRENT_SETTINGS, &devMode);
    return (devMode.dmBitsPerPel > 8);
}

