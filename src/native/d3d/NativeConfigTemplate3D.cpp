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

#include "StdAfx.h"


extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isStereoAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    // DirectX 8.0 don't support stereo
    return false;

}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isDoubleBufferAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{ 
    // D3D always support double buffer
    return true;
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen,
    jint screen)
{
    BOOL antialiasingSupport = false;

    lock();
    if (d3dDriverList == NULL) {
        D3dDriverInfo::initialize(env);
    }

    if (d3dDriverList != NULL) {
	D3dDriverInfo *driverInfo = d3dDriverList[screen];
	for (int i=0; i < numDeviceTypes; i++) {
	    D3dDeviceInfo *pDeviceInfo = driverInfo->d3dDeviceList[i];
	    if (pDeviceInfo->desktopCompatible &&
		pDeviceInfo->supportAntialiasing()) {
		antialiasingSupport = true;
		break;
	    }
	}
    }
    unlock();
    return antialiasingSupport;
}
extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingAccumAvailable(JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    return JNI_FALSE;
}

extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeConfigTemplate3D_choosePixelFormat(
    JNIEnv   *env,
    jobject   obj,
    jlong ctx,
    jint screen,
    jintArray attrList,
    jlongArray offScreenPFArray)
{
    int depth, red, green, blue;
    int retValue = -1;

    jint *mx_ptr = (jint *) env->GetPrimitiveArrayCritical(attrList, NULL);
    red   = mx_ptr[RED_SIZE];
    green = mx_ptr[GREEN_SIZE];
    blue  = mx_ptr[BLUE_SIZE];
    depth = mx_ptr[DEPTH_SIZE];

    env->ReleasePrimitiveArrayCritical(attrList, mx_ptr, 0);

    if (mx_ptr[STEREO] != REQUIRED) {
	lock();

	if (d3dDriverList == NULL) {
	    D3dDriverInfo::initialize(env);
	}

	if (d3dDriverList != NULL) {
	    BOOL bFullScreen;
	    D3dDriverInfo *pDriver = d3dDriverList[screen];
	    D3dDeviceInfo *deviceInfo =
		D3dCtx::setDeviceInfo(pDriver, &bFullScreen, depth);

	    if (deviceInfo != NULL) {
		if ((depth <= deviceInfo->maxZBufferDepthSize) &&
		    (red <= pDriver->redDepth) &&
		    (green <= pDriver->greenDepth) &&
		    (blue <= pDriver->blueDepth)) {
		    retValue = depth;
		}
	    }
	}
	unlock();
    }

    if (mx_ptr[ANTIALIASING] == REQUIRED) {
	if (Java_javax_media_j3d_NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(env, obj, 0, JNI_TRUE, screen) == JNI_TRUE)
	    {
		retValue |= (1 << 31);
	    } else {
		retValue = -1;
	    }
    }

    return retValue;
}

