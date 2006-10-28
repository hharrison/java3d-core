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
void JNICALL Java_javax_media_j3d_NativePipeline_readRaster(
    JNIEnv *env, jobject obj, jlong ctx,
    jint type, jint xOffset, jint yOffset, 
    jint wRaster, jint hRaster, jint hCanvas,
    jint imageDataType,
    jint imageFormat, jobject imageBuffer,
    jint depthFormat, jobject depthBuffer)
{
    void *imageObjPtr;
    void *depthObjPtr;

    GetDevice();

    /* printf("[GraphicsContext3D] readRaster ...\n"); */
    
    if ((type & javax_media_j3d_Raster_RASTER_COLOR) != 0) {

	if ((d3dCtx->d3dPresent.SwapEffect == D3DSWAPEFFECT_DISCARD) 
	    // For offScreen rendering, swapBuffer never invoked
	    // so it is safe to use backBuffer
	    && (!d3dCtx->offScreen)
	    // If fail to createFrontBuffer, fallback to use
	    // backSurface. There is no gaurantee this fallback
	    // will work, but at least in non-debug DirectX library
	    // it works.
	    && ((d3dCtx->frontSurface != NULL) ||
		(d3dCtx->frontSurface == NULL) &&
		d3dCtx->createFrontBuffer())) {
	    
	    
	    HRESULT hr = device->GetFrontBufferData(0,d3dCtx->frontSurface);//iSwapChain as 0
	    if (FAILED(hr)) {
		    printf("GetFrontBuffer fail %s\n", DXGetErrorString9(hr));
		    return;
	    }
	    imageObjPtr = (void *) env->GetPrimitiveArrayCritical((jarray)imageBuffer, NULL);
	    
	    if (!d3dCtx->bFullScreen) {
		// We need to invoke GetWindowRect() everytime
		// since message resize() will not receive
		// when Canvas3D inside browers.
		d3dCtx->getScreenRect(d3dCtx->hwnd, &d3dCtx->windowRect);
		copyDataFromSurface(imageFormat, 
				    xOffset + d3dCtx->windowRect.left, 
				    yOffset + d3dCtx->windowRect.top, 
				    wRaster,
				    hRaster, 
				    (jbyte *) imageObjPtr, 
				    d3dCtx->frontSurface);
	    } 
	    else {
		copyDataFromSurface(imageFormat, xOffset, yOffset,
				    wRaster, hRaster,  (jbyte *) imageObjPtr,
				    d3dCtx->frontSurface);
	    }
 
	    env->ReleasePrimitiveArrayCritical((jarray) imageBuffer, imageObjPtr, 0);

	} 
	else {
	    if (d3dCtx->backSurface == NULL) {
		HRESULT hr = device->GetBackBuffer(0,0, D3DBACKBUFFER_TYPE_MONO, //isSwapChain as 0
						   &d3dCtx->backSurface);
		if (FAILED(hr)) {
		    printf("GetBackBuffer fail %s\n", DXGetErrorString9(hr));
		    return;
		}
	    }
	    imageObjPtr = (void *) env->GetPrimitiveArrayCritical((jarray)imageBuffer, NULL);

	    copyDataFromSurface(imageFormat, xOffset, yOffset, wRaster,
				hRaster, (jbyte *) imageObjPtr, d3dCtx->backSurface);

	    env->ReleasePrimitiveArrayCritical((jarray)imageBuffer, imageObjPtr, 0);

        }
	
    }

    if ((type & javax_media_j3d_Raster_RASTER_DEPTH) != 0) {

	if (d3dCtx->depthStencilSurface == NULL) {
	    HRESULT hr =
		device->GetDepthStencilSurface(&d3dCtx->depthStencilSurface);
	    if (FAILED(hr)) {
		if (debug) {
		    printf("[Java3D] Fail to get depth stencil surface %s\n",
			   DXGetErrorString9(hr));
		}
		return;
	    }
	}

	depthObjPtr = (void *) env->GetPrimitiveArrayCritical((jarray)depthBuffer, NULL);

        if (depthFormat == javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) { 

            // yOffset is adjusted for OpenGL - Y upward
	    copyDepthFromSurface(xOffset, yOffset, wRaster,
				 hRaster, (jint *) depthObjPtr, d3dCtx->depthStencilSurface);

        } else { // javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT
	    
            // yOffset is adjusted for OpenGL - Y upward
	    copyDepthFromSurface(xOffset, yOffset, wRaster,
				 hRaster, (jfloat *) depthObjPtr, d3dCtx->depthStencilSurface);
        }
	env->ReleasePrimitiveArrayCritical((jarray)depthBuffer, depthObjPtr, 0);
    }
}

