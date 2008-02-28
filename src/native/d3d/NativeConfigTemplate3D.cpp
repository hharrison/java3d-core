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

#include "StdAfx.h"


extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_isStereoAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    // DirectX 9.0 don't support stereo
    return false;

}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_isDoubleBufferAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{ 
    // D3D always support double buffer
    return true;
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(
    JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen,
    jint screen)
{
    BOOL antialiasingSupport = false;

    lock();
    if (d3dDriverList == NULL) 
	{
        D3dDriverInfo::initialize(env);
    }

    if (d3dDriverList != NULL) 
	{
	  D3dDriverInfo *driverInfo = d3dDriverList[screen];
	  for (int i=0; i < numDeviceTypes; i++) 
	  {
	    D3dDeviceInfo *pDeviceInfo = driverInfo->d3dDeviceList[i];
	    if (pDeviceInfo->desktopCompatible &&
		    pDeviceInfo->supportAntialiasing()) 
		  {
		   antialiasingSupport = true;
		   break;
	      }
	  }
    }
    unlock();
    return antialiasingSupport;
}
extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_isSceneAntialiasingAccumAvailable(JNIEnv *env,
    jobject obj,
    jlong pFormatInfo,
    jboolean offScreen)
{
    return JNI_FALSE;
}



extern "C" JNIEXPORT
jint JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_choosePixelFormat(
    JNIEnv   *env,
    jobject   obj,
    jlong ctx,
    jint screen,
    jintArray attrList,
    jlongArray offScreenPFArray)
{
    int depth, red, green, blue;
	int stencilDepth;
    int retValue = -1;

    jint *mx_ptr = (jint *) env->GetPrimitiveArrayCritical(attrList, NULL);
    red   = mx_ptr[RED_SIZE];
    green = mx_ptr[GREEN_SIZE];
    blue  = mx_ptr[BLUE_SIZE];
    depth = mx_ptr[DEPTH_SIZE];
    stencilDepth = mx_ptr[STENCIL_SIZE];

    env->ReleasePrimitiveArrayCritical(attrList, mx_ptr, 0);

    if (mx_ptr[STEREO] != REQUIRED) 
	{
	  lock();
	   if (d3dDriverList == NULL) 
	   {
	     D3dDriverInfo::initialize(env);
	    }

	if (d3dDriverList != NULL) 
	{
	    BOOL bFullScreen;
	    D3dDriverInfo *pDriver = d3dDriverList[screen];
	    D3dDeviceInfo *deviceInfo = D3dCtx::setDeviceInfo(pDriver, &bFullScreen, depth, stencilDepth);

	    if (deviceInfo != NULL) 
		{
		  if ((depth <= deviceInfo->maxZBufferDepthSize) &&
		      (red <= pDriver->redDepth) &&
		      (green <= pDriver->greenDepth) &&
		      (blue <= pDriver->blueDepth)&&
			  (stencilDepth)<= deviceInfo->maxStencilDepthSize) 
		  {
			 // printf("\n[Java3D] NativeConfigTemplate3D.choosePixelFormat ZBuffer depth %d", deviceInfo->maxZBufferDepthSize);
			//  printf("\n[Java3D] NativeConfigTemplate3D.choosePixelFormat stencil depth %d", deviceInfo->maxStencilDepthSize);
           
			  // first 0-7bits for depth,8-15 Stencil 
		       retValue = deviceInfo->maxZBufferDepthSize |(deviceInfo->maxStencilDepthSize <<8);
                       // set value for Canvas3D GraphicsConfigInfo
                       jlong *pfi_ptr = (jlong *) env->GetLongArrayElements(offScreenPFArray, NULL);
                       pfi_ptr[0] = retValue;
                       env->ReleaseLongArrayElements(offScreenPFArray, pfi_ptr, 0);
		  }
	    }
	}
	unlock();
    }

    if (mx_ptr[ANTIALIASING] == REQUIRED) 
	{
	  if (Java_javax_media_j3d_Win32NativeConfigTemplate3D_isSceneAntialiasingMultisampleAvailable(env, obj, 0, JNI_TRUE, screen) == JNI_TRUE)
	    {
		 retValue |= (1 << 31);
	    } 
	  else 
		 {
		  retValue = -1;
	     }
    }
    return retValue;
}



/*
 * Class:     javax_media_j3d_Win32NativeConfigTemplate3D
 * Method:    getStencilSize
 * Signature: (JZ)I * 
 */
JNIEXPORT jint JNICALL Java_javax_media_j3d_Win32NativeConfigTemplate3D_getStencilSize
  (JNIEnv *env, jobject obj, jlong pFormatInfo, jboolean offScreen)
{
    jlong stencilSize = pFormatInfo;	
    stencilSize &= 0x0000ff00 ; //clean    
	stencilSize = (stencilSize >> 8);
	
    /** // next version pFormatInfo will be a D3DFORMAT value or index for   
	D3DFORMAT fmt = d3dCtx->deviceInfo->depthStencilFormat;
    if (fmt == D3DFMT_D15S1) stencilSize = 1;
	else
		if (fmt == D3DFMT_D24X4S4) stencilSize = 4;
		else
			if(fmt == D3DFMT_D24S8)stencilSize = 8;
    */
    return (int)stencilSize;
}
