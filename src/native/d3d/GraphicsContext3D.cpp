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
void JNICALL Java_javax_media_j3d_GraphicsContext3D_readRasterNative(
    JNIEnv *env, jobject obj, jlong ctx,
    jint type, jint xOffset, jint yOffset, 
    jint wRaster, jint hRaster, jint hCanvas, jint format,
    jobject image, jobject depth, jobject gc)
{

    GetDevice();

    jclass gc_class = env->GetObjectClass(gc);
    jfieldID id;

    if ((type & javax_media_j3d_Raster_RASTER_COLOR) != 0) {
        jclass image_class = env->GetObjectClass(image);
	
	if (image_class == NULL) {
	    return;
	}

        id = env->GetFieldID(gc_class, "byteBuffer","[B");
        jarray byteData_array = (jarray) env->GetObjectField(gc, id);
	jbyte *byteData;

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

	    
	    HRESULT hr = device->GetFrontBuffer(d3dCtx->frontSurface);
	    if (FAILED(hr)) {
		printf("GetFrontBuffer fail %s\n", DXGetErrorString8(hr));
		return;
	    }

	    byteData = (jbyte *)
		env->GetPrimitiveArrayCritical(byteData_array, NULL);

	    if (!d3dCtx->bFullScreen) {
		// We need to invoke GetWindowRect() everytime
		// since message resize() will not receive
		// when Canvas3D inside browers.
		d3dCtx->getScreenRect(d3dCtx->hwnd, &d3dCtx->windowRect);
		copyDataFromSurface(format, 
				    xOffset + d3dCtx->windowRect.left, 
				    yOffset + d3dCtx->windowRect.top, 
				    wRaster,
				    hRaster, 
				    byteData, 
				    d3dCtx->frontSurface);
	    } else {
		copyDataFromSurface(format, xOffset, yOffset,
				    wRaster, hRaster,  byteData,
				    d3dCtx->frontSurface);
	    }
	} else {
	    if (d3dCtx->backSurface == NULL) {
		HRESULT hr = device->GetBackBuffer(0, D3DBACKBUFFER_TYPE_MONO,
						   &d3dCtx->backSurface);
		if (FAILED(hr)) {
		    printf("GetBackBuffer fail %s\n", DXGetErrorString8(hr));
		    return;
		}
	    }
	    byteData = (jbyte *)
		env->GetPrimitiveArrayCritical(byteData_array, NULL);

	    copyDataFromSurface(format, xOffset, yOffset, wRaster,
				hRaster, byteData, d3dCtx->backSurface);
        }



        env->ReleasePrimitiveArrayCritical(byteData_array, byteData, 0);
    }

    if ((type & javax_media_j3d_Raster_RASTER_DEPTH) != 0) {
	jclass depth_class = env->GetObjectClass(depth);

	if (depth_class == NULL) {
	    return;
	}

        id = env->GetFieldID(depth_class, "width", "I");
        int wDepth = env->GetIntField(depth, id);
        id = env->GetFieldID(depth_class, "type", "I"); 
        int depth_type = env->GetIntField(depth, id);

	if (d3dCtx->depthStencilSurface == NULL) {
	    HRESULT hr =
		device->GetDepthStencilSurface(&d3dCtx->depthStencilSurface);
	    if (FAILED(hr)) {
		if (debug) {
		    printf("[Java3D] Fail to get depth stencil surface %s\n",
			   DXGetErrorString8(hr));
		}
		return;
	    }
	}

        if (depth_type == javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_INT) { 
            id  = env->GetFieldID(gc_class, "intBuffer","[I");
            jarray intData_array = (jarray) env->GetObjectField(gc, id);
            jint *intData = (jint *) 
		env->GetPrimitiveArrayCritical(intData_array, NULL);

            // yOffset is adjusted for OpenGL - Y upward

	    copyDepthFromSurface(xOffset, yOffset, wRaster,
				 hRaster, intData, d3dCtx->depthStencilSurface);

            env->ReleasePrimitiveArrayCritical(intData_array, intData, 0);

        } else { // javax_media_j3d_DepthComponentRetained_DEPTH_COMPONENT_TYPE_FLOAT
            id = env->GetFieldID(gc_class, "floatBuffer","[F");
            jarray floatData_array = (jarray) env->GetObjectField(gc, id);
            jfloat *floatData = (jfloat *)
		env->GetPrimitiveArrayCritical(floatData_array, NULL);

            // yOffset is adjusted for OpenGL - Y upward
	    copyDepthFromSurface(xOffset, yOffset, wRaster,
				 hRaster, floatData, d3dCtx->depthStencilSurface);

            env->ReleasePrimitiveArrayCritical(floatData_array,
					       floatData, 0);
        }
    }
}
