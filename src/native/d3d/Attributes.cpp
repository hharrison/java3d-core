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


const DWORD blendFunctionTable[] =
{
    D3DBLEND_ZERO, D3DBLEND_ONE,
    D3DBLEND_SRCALPHA, D3DBLEND_INVSRCALPHA,
    D3DBLEND_DESTCOLOR, D3DBLEND_SRCCOLOR,
    D3DBLEND_INVSRCCOLOR, D3DBLEND_SRCCOLOR
};

const DWORD combineFunctionTable[] =
{
 D3DTOP_SELECTARG1,  D3DTOP_SELECTARG1,  D3DTOP_SELECTARG1,  D3DTOP_SELECTARG1,
 D3DTOP_MODULATE,    D3DTOP_MODULATE2X,  D3DTOP_MODULATE2X,  D3DTOP_MODULATE4X,
 D3DTOP_ADD,         D3DTOP_ADD,         D3DTOP_ADD,         D3DTOP_ADD,
 D3DTOP_ADDSIGNED,   D3DTOP_ADDSIGNED2X, D3DTOP_ADDSIGNED2X, D3DTOP_ADDSIGNED2X,
 D3DTOP_SUBTRACT,    D3DTOP_SUBTRACT,    D3DTOP_SUBTRACT,    D3DTOP_SUBTRACT,
 D3DTOP_LERP,        D3DTOP_LERP,        D3DTOP_LERP,        D3DTOP_LERP,
 D3DTOP_DOTPRODUCT3, D3DTOP_DOTPRODUCT3, D3DTOP_DOTPRODUCT3, D3DTOP_DOTPRODUCT3
};

// Assume COMBINE_OBJECT_COLOR	              = 0
//        COMBINE_TEXTURE_COLOR	              = 1
//        COMBINE_CONSTANT_COLOR              = 2
//        COMBINE_PREVIOUS_TEXTURE_UNIT_STATE = 3
//
//        COMBINE_SRC_COLOR		= 0
//        COMBINE_ONE_MINUS_SRC_COLOR	= 1
//        COMBINE_SRC_ALPHA		= 2
//        COMBINE_ONE_MINUS_SRC_ALPHA	= 3
//
const DWORD combineSourceTable[] =
{
   D3DTA_DIFFUSE,
   D3DTA_DIFFUSE | D3DTA_COMPLEMENT,
   D3DTA_DIFFUSE | D3DTA_ALPHAREPLICATE,
   D3DTA_DIFFUSE | D3DTA_COMPLEMENT | D3DTA_ALPHAREPLICATE,
   D3DTA_TEXTURE,
   D3DTA_TEXTURE | D3DTA_COMPLEMENT,
   D3DTA_TEXTURE | D3DTA_ALPHAREPLICATE,
   D3DTA_TEXTURE | D3DTA_COMPLEMENT | D3DTA_ALPHAREPLICATE,
   D3DTA_TFACTOR,
   D3DTA_TFACTOR | D3DTA_COMPLEMENT,
   D3DTA_TFACTOR | D3DTA_ALPHAREPLICATE,
   D3DTA_TFACTOR | D3DTA_COMPLEMENT | D3DTA_ALPHAREPLICATE,
   D3DTA_CURRENT,
   D3DTA_CURRENT | D3DTA_COMPLEMENT,
   D3DTA_CURRENT | D3DTA_ALPHAREPLICATE,
   D3DTA_CURRENT | D3DTA_COMPLEMENT | D3DTA_ALPHAREPLICATE
};

// Assume  TEXTURE_COORDINATE_2 = 0
//         TEXTURE_COORDINATE_3 = 1;
//         TEXTURE_COORDINATE_4 = 2;
const int coordFormatTable[] = {2, 3, 4};

BOOL isLineWidthMessOutput = false;
BOOL isBackFaceMessOutput = false;
BOOL isLinePatternMessOutput = false;
BOOL isTexBorderMessOutput = false;

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_LinearFogRetained_update(
	JNIEnv *env,
	jobject obj,
	jlong ctx,
	jfloat red,
	jfloat green,
	jfloat blue,
	jdouble fdist,
	jdouble bdist)
{
    GetDevice();

    float fstart = (float) fdist;
    float fend = (float) bdist;

    device->SetRenderState(d3dCtx->deviceInfo->fogMode,
			   D3DFOG_LINEAR);
    device->SetRenderState(D3DRS_FOGCOLOR,
			   D3DCOLOR_COLORVALUE(red, green, blue, 0));

    device->SetRenderState(D3DRS_FOGSTART,
			   *((LPDWORD) (&fstart)));
    device->SetRenderState(D3DRS_FOGEND,
			   *((LPDWORD) (&fend)));
    device->SetRenderState(D3DRS_FOGENABLE, TRUE);

}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_ExponentialFogRetained_update(
	JNIEnv *env,
	jobject obj,
	jlong  ctx,
	jfloat red,
	jfloat green,
	jfloat blue,
	jfloat density)
{
    GetDevice();

    float d = (float) density;

    device->SetRenderState(d3dCtx->deviceInfo->fogMode,
			   D3DFOG_EXP);
    device->SetRenderState(D3DRS_FOGCOLOR,
			   D3DCOLOR_COLORVALUE(red, green, blue, 0));
    device->SetRenderState(D3DRS_FOGDENSITY,
			   *((LPDWORD) (&d)));
    device->SetRenderState(D3DRS_FOGENABLE, TRUE);

}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_ModelClipRetained_update(
	JNIEnv *env,
	jobject obj,
	jlong ctx,
	jint planeNum,
	jboolean enableFlag,
	jdouble A,
	jdouble B,
	jdouble C,
	jdouble D)
{
    DWORD status;
    float clip[4];

    GetDevice();

    clip[0] = -A;
    clip[1] = -B;
    clip[2] = -C;
    clip[3] = -D;

    device->GetRenderState(D3DRS_CLIPPLANEENABLE, &status);

    if (enableFlag) {
	device->SetClipPlane(planeNum, clip);
	device->SetRenderState(D3DRS_CLIPPLANEENABLE,
			       status | (1 << planeNum));
    } else {
	device->SetRenderState(D3DRS_CLIPPLANEENABLE,
			       status & ~(1 << planeNum));
    }
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setModelViewMatrix(
    JNIEnv * env,
    jobject obj,
    jlong ctx,
    jdoubleArray viewMatrix,
    jdoubleArray modelMatrix)
{
    D3DXMATRIX d3dMatrix;

    GetDevice();


    jdouble *matrix = reinterpret_cast<jdouble*>(
	       env->GetPrimitiveArrayCritical(modelMatrix, NULL));

    CopyTranspose(d3dMatrix, matrix);

    env->ReleasePrimitiveArrayCritical(modelMatrix, matrix, 0);


    device->SetTransform(D3DTS_WORLD,&d3dMatrix);

    matrix = reinterpret_cast<jdouble*>(
		        env->GetPrimitiveArrayCritical(viewMatrix, NULL));
    CopyTranspose(d3dMatrix, matrix);

    env->ReleasePrimitiveArrayCritical(viewMatrix, matrix, 0);

    // Because we negate the third row in projection matrix to
    // make ._34 = 1. Here we negate the third column of View
    // matrix to compensate it.
    d3dMatrix._13 = -d3dMatrix._13;
    d3dMatrix._23 = -d3dMatrix._23;
    d3dMatrix._33 = -d3dMatrix._33;
    d3dMatrix._43 = -d3dMatrix._43;

    device->SetTransform(D3DTS_VIEW, &d3dMatrix);
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setProjectionMatrix(
    JNIEnv * env,
    jobject obj,
    jlong ctx,
    jdoubleArray projMatrix)
{

    GetDevice();

    jdouble *s = reinterpret_cast<jdouble*>(
		    env->GetPrimitiveArrayCritical(projMatrix, NULL));

    /*
     * There are five steps we need to do in order that this
     * matrix is useful by D3D
     *
     * (1) We need to transpose the matrix since in Java3D v' = M*v
     *     but in Direct3D v' = v*N where N is the transpose of M
     *
     * (2) Invert the Z value in clipping coordinates because OpenGL
     *     uses left-handed clipping coordinates, while Java3D defines
     *     right-handed coordinates everywhere. i.e. do the following
     *     after the transpose
     *
     *    d3dMatrix._13 *= -1;
     *	  d3dMatrix._23 *= -1;
     *	  d3dMatrix._33 *= -1;
     *	  d3dMatrix._43 *= -1;
     *
     * (3) In Direct3D, the z-depths range is [0,1] instead of
     *     OGL [-1, 1], so we need to multiple it by
     *
     *	        [1 0   0 0]
     *      R = [0 1   0 0]
     *	        [0 0 0.5 0]
     *		[0 0 0.5 1]
     *
     *     after the transpose and negate. i.e. N*R
     *
     * (4) We want w-friendly perspective matrix, i.e., d3dMatrix._34 = 1
     *     We do this first by divide the whole matrix by
     *     1/d3dMatrix._34 Since d3dMatrix._34 is always negative as
     *     input from Java3D.  Now d3dMatrix._34 = -1
     *
     * (5) To make d3dMatrix._34 = 1, we negate the third row of it.
     *     Because of this, we need to negate the third column in
     *     View matrix to compensate this.
     *
     * All of the above operation is combined together in this
     * implementation for optimization.
     */
    D3DXMATRIX m;

    if (s[14] != 0) {
	// Perspective projection
	// s[14] is always < 0
	float ratio = -1/s[14];
	m._12= m._13 = m._14 = m._21 = m._23 =
	    m._24 = m._41 = m._42 = m._44 = 0;
	m._11 = s[0]*ratio;
	m._22 = s[5]*ratio;
	m._31 = -s[2]*ratio;
	m._32 = -s[6]*ratio;
	m._33 = -(s[14]-s[10])*ratio/2;
	m._43 = -s[11]*ratio/2;
	m._34 = 1;
    } else {
	// parallel projection
	m._12 = m._13 = m._14 = m._21 = m._23 =
	    m._24 = m._31 = m._32 = m._34 = 0;
	m._11 = s[0];
	m._22 = s[5];
	m._33 = s[10]/2;
	m._41 = s[3];
	m._42 = s[7];
	m._43 = (s[15]-s[11])/2;
	m._44 = s[15];
    }

    env->ReleasePrimitiveArrayCritical(projMatrix, s, 0);
    device->SetTransform(D3DTS_PROJECTION,  &m);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setViewport(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint x,
    jint y,
    jint width,
    jint height)
{
    GetDevice();

    if (d3dCtx->bFullScreen) {
	width = d3dCtx->devmode.dmPelsWidth;
	height = d3dCtx->devmode.dmPelsHeight;
    }
    D3DVIEWPORT9 vp = {x, y, width, height, 0.0f, 1.0f};

    device->SetViewport(&vp);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setSceneAmbient(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat red,
    jfloat green,
    jfloat blue)
{
    GetDevice();
    /*
    Clamp(red);
    Clamp(green);
    Clamp(blue);
    */
    device->SetRenderState(D3DRS_AMBIENT,
			   D3DCOLOR_COLORVALUE(red, green, blue, 0));
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setLightEnables(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jlong enable_mask,
    jint nlights)
{
    GetDevice();

#pragma warning(disable:4244)   // loss of data from __int64 to int

    for (int i=nlights-1; i>=0; i--) {
	device->LightEnable(i, enable_mask & (long)(1L<<i));
    }
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setLightingEnable(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean lightingOn)
{
    GetDevice();

    d3dCtx->isLightEnable = lightingOn;
    device->SetRenderState(D3DRS_LIGHTING, lightingOn);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_disableFog(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();
    device->SetRenderState(D3DRS_FOGENABLE, false);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_disableModelClip(
        JNIEnv *env,
        jobject obj,
	jlong ctx)
{
    GetDevice();
    device->SetRenderState(D3DRS_CLIPPLANEENABLE, 0);
}

/**
 * one of :
 * STENCIL_KEEP - keeps the current value (no operation performed). This is the default setting.
 * STENCIL_ZERO - Sets the stencil buffer value to 0.
 * STENCIL_REPLACE - Sets the stencil buffer value to refValue, as specified by setStencilFunction.
 * STENCIL_INCR - Increments the current stencil buffer value.
 * STENCIL_DECR - Decrements the current stencil buffer value.
 * STENCIL_INVERT - Bitwise inverts the current stencil buffer value.
*/
DWORD getStencilOP(jint op)
{
	DWORD value = D3DSTENCILOP_KEEP;
	switch(op)
	{
	case javax_media_j3d_RenderingAttributes_STENCIL_KEEP:
		value = D3DSTENCILOP_KEEP; break;
	case javax_media_j3d_RenderingAttributes_STENCIL_ZERO:
		value = D3DSTENCILOP_ZERO; break;
	case javax_media_j3d_RenderingAttributes_STENCIL_REPLACE:
		value = D3DSTENCILOP_REPLACE; break;
	case javax_media_j3d_RenderingAttributes_STENCIL_INCR:
		value = D3DSTENCILOP_INCRSAT; break;
	case javax_media_j3d_RenderingAttributes_STENCIL_DECR:
		value = D3DSTENCILOP_DECRSAT; break;
	case javax_media_j3d_RenderingAttributes_STENCIL_INVERT:
		value = D3DSTENCILOP_INVERT; break;
	default :
		value = D3DSTENCILOP_KEEP; break;
	}

	return value;
}

/**
 * ALWAYS - pixels are always drawn, irrespective of the stencil value. This effectively disables stencil testing. This is the default setting.
 * NEVER - pixels are never drawn, irrespective of the stencil value.
 * EQUAL - pixels are drawn if the stencil reference value is equal to the stored stencil value in the frame buffer.
 * NOT_EQUAL - pixels are drawn if the stencil reference value is not equal to the stored stencil value in the frame buffer.
 * LESS - pixels are drawn if the stencil reference value is less than the stored stencil value in the frame buffer.
 * LESS_OR_EQUAL - pixels are drawn if the stencil reference value is less than or equal to the stored stencil value in the frame buffer.
 * GREATER - pixels are drawn if the stencil reference value is greater than the stored stencil value in the frame buffer.
 * GREATER_OR_EQUAL - pixels are drawn if the stencil reference value is greater than or equal to the stored stencil value in the frame buffer.
*/
DWORD getStencilFunc(jint func)
{
	DWORD value = D3DCMP_ALWAYS;
	switch(func)
	{
	case javax_media_j3d_RenderingAttributes_ALWAYS:
		value = D3DCMP_ALWAYS; break;
	case javax_media_j3d_RenderingAttributes_NEVER:
		value = D3DCMP_NEVER; break;
	case javax_media_j3d_RenderingAttributes_EQUAL:
		value = D3DCMP_EQUAL; break;
	case javax_media_j3d_RenderingAttributes_NOT_EQUAL:
		value = D3DCMP_NOTEQUAL; break;
	case javax_media_j3d_RenderingAttributes_LESS_OR_EQUAL:
		value = D3DCMP_LESSEQUAL; break;
	case javax_media_j3d_RenderingAttributes_GREATER:
		value = D3DCMP_GREATER; break;
	case javax_media_j3d_RenderingAttributes_GREATER_OR_EQUAL:
		value = D3DCMP_GREATEREQUAL; break;
	default :
		value = D3DCMP_ALWAYS; break;
	}

	return value;
}

/**
 * LESS_OR_EQUAL - DEFAULT pixels are drawn if the depth value is less than or equal to the stored depth value in the frame buffer.
 * ALWAYS - pixels are always drawn, irrespective of the depth value. This effectively disables depth testing.
 * NEVER - pixels are never drawn, irrespective of the depth value.
 * EQUAL - pixels are drawn if the depth value is equal to the stored stencil value in the frame buffer.
 * NOT_EQUAL - pixels are drawn if the depth value is not equal to the stored depth value in the frame buffer.
 * LESS - pixels are drawn if the depth value is less than the stored stencil value in the frame buffer.
 * GREATER - pixels are drawn if the depth value is greater than the stored stencil value in the frame buffer.
 * GREATER_OR_EQUAL - pixels are drawn if the depth value is greater than or equal to the stored stencil value in the frame buffer.
*/
DWORD getDepthFunc(jint func)
{
	DWORD value = D3DCMP_LESSEQUAL;
	switch(func)
	{
	case javax_media_j3d_RenderingAttributes_ALWAYS:
		value = D3DCMP_ALWAYS; break;
	case javax_media_j3d_RenderingAttributes_NEVER:
		value = D3DCMP_NEVER; break;
	case javax_media_j3d_RenderingAttributes_EQUAL:
		value = D3DCMP_EQUAL; break;
	case javax_media_j3d_RenderingAttributes_NOT_EQUAL:
		value = D3DCMP_NOTEQUAL; break;
	case javax_media_j3d_RenderingAttributes_LESS_OR_EQUAL:
		value = D3DCMP_LESSEQUAL; break;
	case javax_media_j3d_RenderingAttributes_GREATER:
		value = D3DCMP_GREATER; break;
	case javax_media_j3d_RenderingAttributes_GREATER_OR_EQUAL:
		value = D3DCMP_GREATEREQUAL; break;
	default :
		value = D3DCMP_LESSEQUAL; break;
	}

	return value;
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetRenderingAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean db_write_enable_override,
    jboolean db_enable_override)
{
    GetDevice();

    if (!db_write_enable_override) {
	d3dCtx->zWriteEnable = TRUE;
	device->SetRenderState(D3DRS_ZWRITEENABLE, D3DZB_TRUE);
	device->SetRenderState(D3DRS_ZFUNC, D3DCMP_LESSEQUAL);
    }

    if (!db_enable_override) {
	d3dCtx->zEnable = TRUE;
	device->SetRenderState(D3DRS_ZENABLE, D3DZB_TRUE);
	device->SetRenderState(D3DRS_ZFUNC, D3DCMP_LESSEQUAL);
    }

    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_ALWAYS);
    device->SetRenderState(D3DRS_ALPHAREF, 0);
	if(d3dCtx->stencilWriteEnable)
	{
		device->SetRenderState(D3DRS_STENCILFUNC, D3DCMP_ALWAYS);
		device->SetRenderState( D3DRS_STENCILREF,  0);
		device->SetRenderState( D3DRS_STENCILMASK,      0xFFFFFFFF);
		device->SetRenderState( D3DRS_STENCILWRITEMASK, 0xFFFFFFFF );

         // Always increment the stencil value
		device->SetRenderState(D3DRS_STENCILFAIL,  D3DSTENCILOP_KEEP);
        device->SetRenderState(D3DRS_STENCILZFAIL, D3DSTENCILOP_KEEP);
        device->SetRenderState(D3DRS_STENCILPASS,  D3DSTENCILOP_KEEP);

		device->SetRenderState(D3DRS_STENCILENABLE, FALSE);
		d3dCtx->stencilWriteEnable = false;
	}

    /*    setRasterOp(d3dCtx, R2_COPYPEN); */

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_RenderingAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean db_write_enable_override,
    jboolean db_enable_override,
    jboolean db_enable,
    jboolean db_write_enable,
	jint db_func,
    jfloat at_value,
    jint at_func,
    jboolean ignoreVertexColors,
    jboolean rasterOpEnable,
    jint rasterOp,
	jboolean userStencilAvailable,
    jboolean stencilEnable,
    jint stencilFailOp,
    jint stencilZFailOp,
    jint stencilZPassOp,
    jint stencilFunction,
    jint stencilReferenceValue,
    jint stencilCompareMask,
    jint stencilWriteMask)
{

    GetDevice();

    DWORD alpha = (DWORD) (at_value * 255 + 0.5f);

    if (!db_enable_override)
	{
        if (db_enable)
		{
	      d3dCtx->zEnable = TRUE;
	      device->SetRenderState(D3DRS_ZENABLE, D3DZB_TRUE);
        }
		else
		{
	      d3dCtx->zEnable = FALSE;
	      device->SetRenderState(D3DRS_ZENABLE, D3DZB_FALSE);		  
        }
    }

    if (!db_write_enable_override)
	{
	  d3dCtx->zWriteEnable = db_write_enable;
	  device->SetRenderState(D3DRS_ZWRITEENABLE, db_write_enable);
	  // disable ZFunc if ZBuffer is disabled ? no. 
	  //  ZFunc must work even when there is no z-buffer enable
      device->SetRenderState(D3DRS_ZFUNC, getDepthFunc(db_func));	  
    }

    if (at_func == javax_media_j3d_RenderingAttributes_ALWAYS)
	{
	  device->SetRenderState(D3DRS_ALPHATESTENABLE, FALSE);
    }
	else
	{
	  device->SetRenderState(D3DRS_ALPHATESTENABLE, TRUE);
	  device->SetRenderState(D3DRS_ALPHAREF, alpha);
    }

    switch (at_func)
	{
	case javax_media_j3d_RenderingAttributes_ALWAYS:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_ALWAYS);
	    break;
	case javax_media_j3d_RenderingAttributes_NEVER:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_NEVER);
	    break;
	case javax_media_j3d_RenderingAttributes_EQUAL:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_EQUAL);
	    break;
	case javax_media_j3d_RenderingAttributes_NOT_EQUAL:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_NOTEQUAL);
	    break;
	case javax_media_j3d_RenderingAttributes_LESS:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_LESS);
	    break;
	case javax_media_j3d_RenderingAttributes_LESS_OR_EQUAL:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_LESSEQUAL);
	    break;
	case javax_media_j3d_RenderingAttributes_GREATER:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_GREATER);
	    break;
	case javax_media_j3d_RenderingAttributes_GREATER_OR_EQUAL:
	    device->SetRenderState(D3DRS_ALPHAFUNC, D3DCMP_GREATEREQUAL);
	    break;
    }

/*
	 stencilFailOp,  *
     stencilZFailOp, *
     stencilZPassOp, *
     stencilFunction, *
     stencilReferenceValue, *
     stencilCompareMask, *
     stencilWriteMask *
*/
	if (userStencilAvailable == JNI_TRUE)
	{
        if (stencilEnable == JNI_TRUE)
		{
         // Turn on stenciling
         device->SetRenderState( D3DRS_STENCILENABLE, TRUE );
        // printf("StencilEnable TRUE\n");
         // Set the function to always pass.
         device->SetRenderState( D3DRS_STENCILFUNC, getStencilFunc(stencilFunction) );
         device->SetRenderState( D3DRS_STENCILREF,  stencilReferenceValue );
		 device->SetRenderState( D3DRS_STENCILMASK, stencilCompareMask );
		 device->SetRenderState( D3DRS_STENCILWRITEMASK, stencilWriteMask );

         // Always increment the stencil value
		 device->SetRenderState(D3DRS_STENCILFAIL,  getStencilOP(stencilFailOp) );
         device->SetRenderState(D3DRS_STENCILZFAIL, getStencilOP(stencilZFailOp));
         device->SetRenderState(D3DRS_STENCILPASS,  getStencilOP(stencilZPassOp) );
   	   }
	   else
	   {
		 device->SetRenderState( D3DRS_STENCILENABLE, FALSE );
		 // printf("StencilEnable False\n");
       }
    }
	/*
	else
	{
		 printf("UserStencilEnable False\n");
	}
	*/

}//



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetPolygonAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();
    // D3D vertex order is reverse of OGL
    d3dCtx->cullMode = D3DCULL_CW;
    d3dCtx->fillMode = D3DFILL_SOLID;
    device->SetRenderState(D3DRS_FILLMODE, D3DFILL_SOLID);
    device->SetRenderState(D3DRS_CULLMODE, D3DCULL_CW);
    d3dCtx->twoSideLightingEnable = false;
    device->SetRenderState(D3DRS_DEPTHBIAS, 0);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_PolygonAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint polygonMode,
    jint cullFace,
    jboolean backFaceNormalFlip,
    jfloat polygonOffset,
    jfloat polygonOffsetFactor)
{

    GetDevice();

    jfloat zbias = polygonOffset + polygonOffsetFactor;
    DWORD zbias_w = 0;
    /*
     * DirectX support Z-bias from 0 to 16 only and the
     * direction is opposite to OGL. If we pass negative
     * Z-bias the polygon will not render at all.
     * So we map -ve polygon offset to positive value
     * and +ve offset to 0. (i.e. we don't support positive
     * polygon offset)
     */
    if (zbias <= -1) {
	zbias_w = max(-zbias/50, 1);

	if (zbias_w > 16) {
	    zbias_w = 16;
	}
    }

    device->SetRenderState(D3DRS_DEPTHBIAS, zbias_w);

    if (cullFace == javax_media_j3d_PolygonAttributes_CULL_NONE) {
	d3dCtx->cullMode = D3DCULL_NONE;
	device->SetRenderState(D3DRS_CULLMODE, D3DCULL_NONE);
    } else {
        if (cullFace == javax_media_j3d_PolygonAttributes_CULL_BACK) {
	    d3dCtx->cullMode = D3DCULL_CW;
	    device->SetRenderState(D3DRS_CULLMODE, D3DCULL_CW);
        } else {
	    d3dCtx->cullMode = D3DCULL_CCW;
	    device->SetRenderState(D3DRS_CULLMODE, D3DCULL_CCW);
	}
    }

    if (polygonMode == javax_media_j3d_PolygonAttributes_POLYGON_POINT) {
	d3dCtx->fillMode = D3DFILL_POINT;
	device->SetRenderState(D3DRS_FILLMODE, D3DFILL_POINT);
    } else if (polygonMode == javax_media_j3d_PolygonAttributes_POLYGON_LINE) {
	d3dCtx->fillMode = D3DFILL_WIREFRAME;
	device->SetRenderState(D3DRS_FILLMODE, D3DFILL_WIREFRAME);
    } else {
	d3dCtx->fillMode = D3DFILL_SOLID;
	device->SetRenderState(D3DRS_FILLMODE, D3DFILL_SOLID);
    }

    /*
    if (debug && !isBackFaceMessOutput &&
	(backFaceNormalFlip) && (cullFace != javax_media_j3d_PolygonAttributes_CULL_BACK)) {
	isBackFaceMessOutput = true;
	printf("BackFaceNormalFlip is not support !\n");
    }
    */
}

/*
void printDepthFunc(jint func)
{
	DWORD value = D3DCMP_LESSEQUAL;
	printf("DepthFunc: ");
	switch(func)
	{
	case javax_media_j3d_RenderingAttributes_ALWAYS:
		printf(" D3DCMP_ALWAYS\n"); break;
	case javax_media_j3d_RenderingAttributes_NEVER:
		printf(" D3DCMP_NEVER\n"); break;
	case javax_media_j3d_RenderingAttributes_EQUAL:
		printf(" D3DCMP_EQUAL\n"); break;
	case javax_media_j3d_RenderingAttributes_NOT_EQUAL:
		printf(" D3DCMP_NOTEQUAL\n"); break;
	case javax_media_j3d_RenderingAttributes_LESS_OR_EQUAL:
		printf(" D3DCMP_LESSEQUAL\n"); break;
	case javax_media_j3d_RenderingAttributes_GREATER:
		printf(" D3DCMP_GREATER\n"); break;
	case javax_media_j3d_RenderingAttributes_GREATER_OR_EQUAL:
		printf(" D3DCMP_GREATEREQUAL\n"); break;
	default :
		printf(" D3DCMP_LESSEQUAL\n"); break;
	}
}

*/

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetLineAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();

    // D3D don't support Line width
    // glLineWidth(1);
    //D3D9 doesnot support line Patterns
	// @TODO must update this to use ID3DXLine Interface
/*
    D3DLINEPATTERN pattern;
    pattern.wRepeatFactor = 0;
    pattern.wLinePattern = 0;
    device->SetRenderState(D3DRS_LINEPATTERN,
			   *((LPDWORD) (&pattern)));

*/
}


// Note that some graphics card don't support it.
// In this case use RGB Emulation.
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_LineAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat lineWidth,
    jint linePattern,
    jint linePatternMask,
    jint linePatternScaleFactor,
    jboolean lineAntialiasing)
{
    GetDevice();

	//Alessandro
    //D3DLINEPATTERN pattern;

    /*
    if (lineWidth > 1) {
	if (debug && !isLineWidthMessOutput) {
	    isLineWidthMessOutput = true;
	    printf("Line width > 1 not support !\n");
	}
    }
    */
    // glLineWidth(lineWidth);
  /** Alessandro
    if (linePattern == javax_media_j3d_LineAttributes_PATTERN_SOLID) {
	pattern.wRepeatFactor = 0;
	pattern.wLinePattern = 0;

    } else {
	**/

	/*
	if (!d3dCtx->deviceInfo->linePatternSupport) {
	    if (debug && !isLinePatternMessOutput) {
		printf("Device not support line pattern !\n");
		isLinePatternMessOutput = false;
	    }
	}
	*/
	/** alessandro
        if (linePattern == javax_media_j3d_LineAttributes_PATTERN_DASH) { // dashed lines
	    pattern.wRepeatFactor = 1;
	    pattern.wLinePattern = 0x00ff;
        } else if (linePattern == javax_media_j3d_LineAttributes_PATTERN_DOT) { // dotted lines
	    pattern.wRepeatFactor = 1;
	    pattern.wLinePattern = 0x0101;
        } else if (linePattern == javax_media_j3d_LineAttributes_PATTERN_DASH_DOT) { // dash-dotted lines
	    pattern.wRepeatFactor = 1;
	    pattern.wLinePattern = 0x087f;
	} else if (linePattern == javax_media_j3d_LineAttributes_PATTERN_USER_DEFINED) { // user-defined mask
	    pattern.wRepeatFactor = linePatternScaleFactor;
	    pattern.wLinePattern = (WORD) linePatternMask;
	}
    }

    device->SetRenderState(D3DRS_LINEPATTERN,
			   *((LPDWORD) (&pattern)));
      **/
    /*
      if (lineAntialiasing == JNI_TRUE) {
      glEnable (GL_LINE_SMOOTH);
      } else {
      glDisable (GL_LINE_SMOOTH);
      }
    */
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetPointAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();

    if (d3dCtx->pointSize != 1.0f) {
	d3dCtx->pointSize = 1.0f;
	device->SetRenderState(D3DRS_POINTSIZE, *((LPDWORD) &d3dCtx->pointSize));
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_PointAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat pointSize,
    jboolean pointAntialiasing)
{
    // point Antialiasing not support by D3D
    GetDevice();

    if (pointSize < 1.0f) {
	// We don't want to set pointSize unnecessary and
	// trigger the software vertex processing mode in
	// D3DVertexBuffer if possible. It is an error
	// to set pointSize to zero under OGL.
	pointSize = 1.0f;
    }

    if (d3dCtx->pointSize != pointSize) {
	device->SetRenderState(D3DRS_POINTSIZE, *((LPDWORD)
						  &pointSize));
	d3dCtx->pointSize = pointSize;
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetTexCoordGeneration(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();

    int tus = d3dCtx->texUnitStage;

    if (tus >= d3dCtx->bindTextureIdLen) {
	return;
    }

    d3dCtx->texGenMode[tus] = TEX_GEN_NONE;

    if (d3dCtx->texTransformSet[tus]) {
	device->SetTransform((D3DTRANSFORMSTATETYPE)
			     (D3DTS_TEXTURE0 + tus),
			     &(d3dCtx->texTransform[tus]));
    }

    device->SetTextureStageState(tus,
				 D3DTSS_TEXCOORDINDEX,
				 D3DTSS_TCI_PASSTHRU);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TexCoordGenerationRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean enable,
    jint genMode,
    jint format,
    jfloat planeSx,
    jfloat planeSy,
    jfloat planeSz,
    jfloat planeSw,
    jfloat planeTx,
    jfloat planeTy,
    jfloat planeTz,
    jfloat planeTw,
    jfloat planeRx,
    jfloat planeRy,
    jfloat planeRz,
    jfloat planeRw,
    jfloat planeQx,
    jfloat planeQy,
    jfloat planeQz,
    jfloat planeQw,
    jdoubleArray eyeToVworld)
{

    D3DXMATRIX m;
    jdouble *mv;
    GetDevice();

    int tus = d3dCtx->texUnitStage;


    if (tus >= d3dCtx->bindTextureIdLen) {
	return;
    }

    if (!enable) {
	device->SetTextureStageState(tus,
				     D3DTSS_TEXCOORDINDEX,
				     D3DTSS_TCI_PASSTHRU);
	d3dCtx->texGenMode[tus] = TEX_GEN_NONE;
	return;
    }

    d3dCtx->texCoordFormat[tus] = coordFormatTable[format];

//  printf("TexCoordGenerationRetained_updateNative texStage %d set Mode %d, format %d, texTransformSet %d\n", tus, genMode, format, d3dCtx->texTransformSet[tus]);

    switch (genMode) {
    case javax_media_j3d_TexCoordGeneration_EYE_LINEAR:
	// Generated Coordinate = p1'*x + p2'*y + p3'*z + p4'*w;
	// where (p1', p2', p3', p4') = (p1 p2 p3 p4)*eyeToVworld
	mv = (jdouble * ) env->GetPrimitiveArrayCritical(eyeToVworld, 0);
	m._11 = planeSx*mv[0] + planeSy*mv[4] + planeSz*mv[8]  + planeSw*mv[12];
	m._21 = planeSx*mv[1] + planeSy*mv[5] + planeSz*mv[9]  + planeSw*mv[13];
	m._31 = planeSx*mv[2] + planeSy*mv[6] + planeSz*mv[10] + planeSw*mv[14];
	m._41 = planeSx*mv[3] + planeSy*mv[7] + planeSz*mv[11] + planeSw*mv[15];
	m._12 = planeTx*mv[0] + planeTy*mv[4] + planeTz*mv[8]  + planeTw*mv[12];
	m._22 = planeTx*mv[1] + planeTy*mv[5] + planeTz*mv[9]  + planeTw*mv[13];
	m._32 = planeTx*mv[2] + planeTy*mv[6] + planeTz*mv[10] + planeTw*mv[14];
	m._42 = planeTx*mv[3] + planeTy*mv[7] + planeTz*mv[11] + planeTw*mv[15];


	if (format >= javax_media_j3d_TexCoordGeneration_TEXTURE_COORDINATE_3) {
	    m._13 = planeRx*mv[0] + planeRy*mv[4] + planeRz*mv[8]  + planeRw*mv[12];
	    m._23 = planeRx*mv[1] + planeRy*mv[5] + planeRz*mv[9]  + planeRw*mv[13];
	    m._33 = planeRx*mv[2] + planeRy*mv[6] + planeRz*mv[10] + planeRw*mv[14];
	    m._43 = planeRx*mv[3] + planeRy*mv[7] + planeRz*mv[11] + planeRw*mv[15];

	    if (format >= javax_media_j3d_TexCoordGeneration_TEXTURE_COORDINATE_4) {
		m._14 = planeQx*mv[0] + planeQy*mv[4] + planeQz*mv[8]  + planeQw*mv[12];
		m._24 = planeQx*mv[1] + planeQy*mv[5] + planeQz*mv[9]  + planeQw*mv[13];
		m._34 = planeQx*mv[2] + planeQy*mv[6] + planeQz*mv[10] + planeQw*mv[14];
		m._44 = planeQx*mv[3] + planeQy*mv[7] + planeQz*mv[11] + planeQw*mv[15];
	    } else {
		m._14 = 0;
		m._24 = 0;
		m._34 = 0;
		m._44 = 0;
	    }
	} else {
	    m._13 = 0;
	    m._23 = 0;
	    m._33 = 0;
	    m._43 = 0;
	    m._14 = 0;
	    m._24 = 0;
	    m._34 = 0;
	    m._44 = 0;
	}

	env->ReleasePrimitiveArrayCritical(eyeToVworld, mv, 0);

	if (d3dCtx->texTransformSet[tus]) {
	    device->MultiplyTransform((D3DTRANSFORMSTATETYPE)
				      (D3DTS_TEXTURE0 + tus) , &m);
	} else {
	    device->SetTransform((D3DTRANSFORMSTATETYPE)
				 (D3DTS_TEXTURE0 + tus), &m);
	    d3dCtx->texTransformSet[tus] = true;
	}
	d3dCtx->texGenMode[tus] = TEX_EYE_LINEAR;

	break;

    case javax_media_j3d_TexCoordGeneration_SPHERE_MAP:
	/*
	  The matrix has to scale and translate the texture coordinates
	  Since in sphere map Tx = Nx/2 + 0.5, Ty = Ny/2 + 0.5
	*/
	m._11 = 0.5f; m._12 = 0.0f; m._13 = 0.0f; m._14 = 0.0f;
	m._21 = 0.0f; m._22 = 0.5f; m._23 = 0.0f; m._24 = 0.0f;
	m._31 = 0.0f; m._32 = 0.0f; m._33 = 1.0f; m._34 = 0.0f;
	m._41 = 0.5f; m._42 = 0.5f; m._43 = 0.0f; m._44 = 1.0f;

	if (d3dCtx->texTransformSet[tus]) {
	    // If texture transform already set, multiple by this
	    // matrix.
	    device->MultiplyTransform((D3DTRANSFORMSTATETYPE)
				      (D3DTS_TEXTURE0 + tus) , &m);
	} else {
	    device->SetTransform((D3DTRANSFORMSTATETYPE)
				 (D3DTS_TEXTURE0 + tus), &m);
	    d3dCtx->texTransformSet[tus] = true;
	}

	d3dCtx->texGenMode[tus] = TEX_SPHERE_MAP;
	break;
    case javax_media_j3d_TexCoordGeneration_OBJECT_LINEAR:
	// OBJECT_LINEAR not support by D3D, we'll do it ourselve.
	d3dCtx->planeS[tus][0] = planeSx;
	d3dCtx->planeS[tus][1] = planeSy;
	d3dCtx->planeS[tus][2] = planeSz;
	d3dCtx->planeS[tus][3] = planeSw;
	d3dCtx->planeT[tus][0] = planeTx;
	d3dCtx->planeT[tus][1] = planeTy;
	d3dCtx->planeT[tus][2] = planeTz;
	d3dCtx->planeT[tus][3] = planeTw;
	d3dCtx->planeR[tus][0] = planeRx;
	d3dCtx->planeR[tus][1] = planeRy;
	d3dCtx->planeR[tus][2] = planeRz;
	d3dCtx->planeR[tus][3] = planeRw;
	d3dCtx->planeQ[tus][0] = planeQx;
	d3dCtx->planeQ[tus][1] = planeQy;
	d3dCtx->planeQ[tus][2] = planeQz;
	d3dCtx->planeQ[tus][3] = planeQw;
	d3dCtx->texGenMode[tus] = TEX_OBJ_LINEAR;
	break;
    case javax_media_j3d_TexCoordGeneration_NORMAL_MAP:
	d3dCtx->texGenMode[tus] = TEX_NORMAL_MAP;
	break;
    case javax_media_j3d_TexCoordGeneration_REFLECTION_MAP:
	d3dCtx->texGenMode[tus] = TEX_REFLECT_MAP;
	break;
    default:
	printf("Unknown TexCoordinate Generation mode %d\n", genMode);
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetTextureAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();

    int tus = d3dCtx->texUnitStage;

    if (tus >= d3dCtx->bindTextureIdLen) {
	return;
    }

    if (d3dCtx->texTransformSet[tus]) {
	d3dCtx->texTransformSet[tus] = false;
	device->SetTransform((D3DTRANSFORMSTATETYPE)
			     (D3DTS_TEXTURE0 + tus),
			     &identityMatrix);
    }

    // perspCorrectionMode always turn on in DX8.0 if device support

    device->SetTextureStageState(tus,
				 D3DTSS_COLOROP, D3DTOP_SELECTARG1);
    device->SetTextureStageState(tus,
				 D3DTSS_COLORARG1, D3DTA_TEXTURE);
    device->SetTextureStageState(tus,
				 D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
    device->SetTextureStageState(tus,
				 D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jdoubleArray transform, jboolean isIdentityT,
    jint textureMode,
    jint perspCorrectionMode,
    jfloat textureBlendColorRed,
    jfloat textureBlendColorGreen,
    jfloat textureBlendColorBlue,
    jfloat textureBlendColorAlpha,
    jint format)
{

    D3DCOLOR textureBlendColor;
    BOOL alphaDisable = FALSE;

    GetDevice();

    int tus = d3dCtx->texUnitStage;

    if (tus >= d3dCtx->bindTextureIdLen) {
	return;
    }

    // perspCorrectionMode always turn on in DX8.0 if device support

    if (isIdentityT) {
	d3dCtx->texTransformSet[tus] = false;
	device->SetTransform((D3DTRANSFORMSTATETYPE)
			     (D3DTS_TEXTURE0 + tus),
			     &identityMatrix);
    } else {
	D3DXMATRIX *m = &(d3dCtx->texTransform[tus]);
	jdouble *mx_ptr = reinterpret_cast<jdouble *>(
			     env->GetPrimitiveArrayCritical(transform, NULL));
	CopyTranspose((*m), mx_ptr);

	env->ReleasePrimitiveArrayCritical(transform, mx_ptr, 0);
	/*
	printf("set Tex Transform \n");
	printf("%f, %f, %f, %f\n", (*m)._11, (*m)._12, (*m)._13, (*m)._14);
	printf("%f, %f, %f, %f\n", (*m)._21, (*m)._22, (*m)._23, (*m)._24);
	printf("%f, %f, %f, %f\n", (*m)._31, (*m)._23, (*m)._33, (*m)._34);
	printf("%f, %f, %f, %f\n", (*m)._41, (*m)._42, (*m)._43, (*m)._44);
	*/
	d3dCtx->texTransformSet[tus] = true;
	d3dCtx->texTranslateSet[tus] = false;
	device->SetTransform((D3DTRANSFORMSTATETYPE)
			     (D3DTS_TEXTURE0 + tus), m);
    }

    /* set texture environment mode */

    switch (textureMode) {
        case javax_media_j3d_TextureAttributes_MODULATE:
	    switch (format) {
	    case J3D_RGBA:
	    case INTENSITY:
	    case LUMINANCE_ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_MODULATE);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG2, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_MODULATE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG2, D3DTA_CURRENT);
		break;
	    case J3D_RGB:
	    case LUMINANCE:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_MODULATE);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG2, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_DISABLE);
		break;
	    case ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_MODULATE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG2, D3DTA_CURRENT);
		break;
	    default:
		printf("Format %d not support\n", format);
	    }
	    break;
        case javax_media_j3d_TextureAttributes_DECAL:
	    switch (format) {
            case J3D_RGBA:
	    case INTENSITY:
	    case LUMINANCE_ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_BLENDTEXTUREALPHA);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG2, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_DISABLE);
		break;
	    case J3D_RGB:
	    case LUMINANCE:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_DISABLE);
		break;
	    case ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		break;
	    default:
		printf("Format %d not support\n", format);
	    }
	    break;
        case javax_media_j3d_TextureAttributes_BLEND:
	    // Two pass is needed for this mode, the first pass
	    // will

	    textureBlendColor = D3DCOLOR_COLORVALUE(textureBlendColorRed,
						    textureBlendColorGreen,
						    textureBlendColorBlue,
						    textureBlendColorAlpha);

	    device->SetRenderState(D3DRS_TEXTUREFACTOR,
				   *((LPDWORD) &textureBlendColor));

	    switch (format) {
	    case ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_MODULATE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG2, D3DTA_CURRENT);
		break;
	    case INTENSITY:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_BLENDTEXTUREALPHA);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG2, D3DTA_TFACTOR);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_BLENDTEXTUREALPHA);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG2, D3DTA_TFACTOR);
		break;
	    case J3D_RGB:
	    case LUMINANCE:
		device->SetTextureStageState(0,
					     D3DTSS_ALPHAOP,
					     D3DTOP_DISABLE);
		alphaDisable = TRUE;
		// fallthrough
	    case J3D_RGBA:
	    case LUMINANCE_ALPHA:

		if (!d3dCtx->deviceInfo->texLerpSupport) {
		    // Use two pass, first pass will enable specular and
		    // compute Cf*(1 - Ct), second pass will disable specular and
		    // comptue  Cc*Ct. Note that multi-texturing is disable
		    // in this case, so stage 0 is always use.
		    device->SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_MODULATE);
		    device->SetTextureStageState(0, D3DTSS_COLORARG1,
						 D3DTA_TEXTURE|D3DTA_COMPLEMENT);
		    device->SetTextureStageState(0, D3DTSS_COLORARG2, D3DTA_CURRENT);

		    if (!alphaDisable) {
			device->SetTextureStageState(0, D3DTSS_ALPHAOP, D3DTOP_MODULATE);
			device->SetTextureStageState(0, D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
			device->SetTextureStageState(0, D3DTSS_ALPHAARG2, D3DTA_CURRENT);
		    }
		} else {
		    device->SetTextureStageState(tus, D3DTSS_COLOROP, D3DTOP_LERP);
		    device->SetTextureStageState(tus, D3DTSS_COLORARG0, D3DTA_TEXTURE);
		    device->SetTextureStageState(tus, D3DTSS_COLORARG1, D3DTA_TFACTOR);
		    device->SetTextureStageState(tus, D3DTSS_COLORARG2, D3DTA_CURRENT);

		    if (!alphaDisable) {
			device->SetTextureStageState(tus, D3DTSS_ALPHAOP, D3DTOP_LERP);
			device->SetTextureStageState(tus, D3DTSS_ALPHAARG0, D3DTA_TEXTURE);
			device->SetTextureStageState(tus, D3DTSS_ALPHAARG1, D3DTA_TFACTOR);
			device->SetTextureStageState(tus, D3DTSS_ALPHAARG2, D3DTA_CURRENT);
		    }
		}
		break;
	    default:
		printf("Format %d not support\n", format);
	    }


            break;
        case javax_media_j3d_TextureAttributes_REPLACE:
	    switch (format) {
	    case J3D_RGBA:
	    case INTENSITY:
	    case LUMINANCE_ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		break;
	    case J3D_RGB:
	    case LUMINANCE:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_TEXTURE);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_DISABLE);
		break;
	    case ALPHA:
		device->SetTextureStageState(tus,
					     D3DTSS_COLOROP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_COLORARG1, D3DTA_CURRENT);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
		device->SetTextureStageState(tus,
					     D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
		break;
	    default:
		printf("Format %d not support\n", format);
	    }
	    break;
      default:
	   // TEXTURE COMBINER case
	  break;
    }
}


// This procedure is invoked after Blend2Pass to restore the original value
extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureAttributesRetained_restoreBlend1Pass(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();

    device->SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_MODULATE);
    device->SetTextureStageState(0, D3DTSS_COLORARG1,
				 D3DTA_TEXTURE|D3DTA_COMPLEMENT);
    device->SetTextureStageState(0, D3DTSS_COLORARG2, D3DTA_CURRENT);

    device->SetRenderState(D3DRS_SRCBLEND,
			   d3dCtx->srcBlendFunc);
    device->SetRenderState(D3DRS_DESTBLEND,
			   d3dCtx->dstBlendFunc);
    device->SetRenderState(D3DRS_ALPHABLENDENABLE,
			   d3dCtx->blendEnable);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureAttributesRetained_updateBlend2Pass(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice();
    device->GetRenderState(D3DRS_SRCBLEND,
			   &d3dCtx->srcBlendFunc);
    device->GetRenderState(D3DRS_DESTBLEND,
			   &d3dCtx->dstBlendFunc);
    device->GetRenderState(D3DRS_ALPHABLENDENABLE,
			   &d3dCtx->blendEnable);

    device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
    device->SetRenderState(D3DRS_SRCBLEND, D3DBLEND_ONE);
    device->SetRenderState(D3DRS_DESTBLEND, D3DBLEND_ONE);
    device->SetTextureStageState(0, D3DTSS_COLOROP, D3DTOP_MODULATE);
    device->SetTextureStageState(0, D3DTSS_COLORARG1, D3DTA_TEXTURE);
    device->SetTextureStageState(0, D3DTSS_COLORARG2, D3DTA_TFACTOR);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureAttributesRetained_updateCombinerNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint combineRgbMode,
    jint combineAlphaMode,
    jintArray combineRgbSrc,
    jintArray combineAlphaSrc,
    jintArray combineRgbFcn,
    jintArray combineAlphaFcn,
    jint combineRgbScale,
    jint combineAlphaScale)
{

    GetDevice();

    DWORD ts = d3dCtx->texUnitStage;

    jint *rgbSrc = (jint *) env->GetPrimitiveArrayCritical(combineRgbSrc, NULL);
    jint *alphaSrc = (jint *) env->GetPrimitiveArrayCritical(combineAlphaSrc, NULL);
    jint *rgbFcn = (jint *) env->GetPrimitiveArrayCritical(combineRgbFcn, NULL);
    jint *alphaFcn = (jint *) env->GetPrimitiveArrayCritical(combineAlphaFcn, NULL);


    device->SetTextureStageState(ts, D3DTSS_COLOROP,
				 combineFunctionTable[(combineRgbMode << 2) + combineRgbScale - 1]);

    if (combineRgbMode !=  javax_media_j3d_TextureAttributes_COMBINE_INTERPOLATE) {
	device->SetTextureStageState(ts, D3DTSS_COLORARG1,
				     combineSourceTable[(rgbSrc[0] << 2) + rgbFcn[0]]);
	if (combineRgbMode != javax_media_j3d_TextureAttributes_COMBINE_REPLACE) {
	    device->SetTextureStageState(ts, D3DTSS_COLORARG2,
					 combineSourceTable[(rgbSrc[1] << 2) + rgbFcn[1]]);
	}
    } else {
	device->SetTextureStageState(ts, D3DTSS_COLORARG1,
				     combineSourceTable[(rgbSrc[2] << 2) + rgbFcn[2]]);
	device->SetTextureStageState(ts, D3DTSS_COLORARG2,
				     combineSourceTable[(rgbSrc[0] << 2) + rgbFcn[0]]);
	device->SetTextureStageState(ts, D3DTSS_COLORARG0,
				     combineSourceTable[(rgbSrc[1] << 2) + rgbFcn[1]]);
    }

    device->SetTextureStageState(ts, D3DTSS_ALPHAOP,
				 combineFunctionTable[(combineAlphaMode << 2) + combineAlphaScale - 1]);

    if (combineAlphaMode != javax_media_j3d_TextureAttributes_COMBINE_INTERPOLATE) {
	device->SetTextureStageState(ts, D3DTSS_ALPHAARG1,
				     combineSourceTable[(alphaSrc[0] << 2) + alphaFcn[0]]);
	if (combineAlphaMode != javax_media_j3d_TextureAttributes_COMBINE_REPLACE) {
	    device->SetTextureStageState(ts, D3DTSS_ALPHAARG2,
					 combineSourceTable[(alphaSrc[1] << 2) + alphaFcn[1]]);
	}
    } else {
	device->SetTextureStageState(ts, D3DTSS_ALPHAARG0,
				     combineSourceTable[(alphaSrc[2] << 2) + alphaFcn[2]]);
	device->SetTextureStageState(ts, D3DTSS_ALPHAARG1,
				     combineSourceTable[(alphaSrc[0] << 2) + alphaFcn[0]]);
	device->SetTextureStageState(ts, D3DTSS_ALPHAARG2,
				     combineSourceTable[(alphaSrc[1] << 2) + alphaFcn[1]]);
    }

    env->ReleasePrimitiveArrayCritical(combineRgbSrc, rgbSrc, 0);
    env->ReleasePrimitiveArrayCritical(combineAlphaSrc, alphaSrc, 0);
    env->ReleasePrimitiveArrayCritical(combineRgbFcn, rgbFcn, 0);
    env->ReleasePrimitiveArrayCritical(combineAlphaFcn, alphaFcn, 0);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureAttributesRetained_updateTextureColorTableNative(
	JNIEnv *env,
	jobject obj,
	jlong ctx,
	jint numComponents,
	jint colorTableSize,
	jintArray textureColorTable)
{
    // Not support by D3D
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_updateMaterial(
	JNIEnv *env,
	jobject obj,
	jlong ctx,
	jfloat colorRed,
	jfloat colorGreen,
	jfloat colorBlue,
	jfloat transparency)
{
    GetDevice();

    d3dCtx->currentColor_r = colorRed;
    d3dCtx->currentColor_g = colorGreen;
    d3dCtx->currentColor_b = colorBlue;
    d3dCtx->currentColor_a = transparency;

    d3dCtx->isLightEnable = false;
    device->SetRenderState(D3DRS_LIGHTING, false);
    if (d3dCtx->resetColorTarget) {
	device->SetRenderState(D3DRS_DIFFUSEMATERIALSOURCE,
			       D3DMCS_COLOR1);
	device->SetRenderState(D3DRS_SPECULARMATERIALSOURCE,
			       D3DMCS_MATERIAL);
	device->SetRenderState(D3DRS_AMBIENTMATERIALSOURCE,
			       D3DMCS_MATERIAL);
	device->SetRenderState(D3DRS_EMISSIVEMATERIALSOURCE,
			       D3DMCS_MATERIAL);
	d3dCtx->resetColorTarget = false;
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_MaterialRetained_updateNative(
        JNIEnv *env,
        jobject obj,
	jlong ctx,
        jfloat colorRed,
        jfloat colorGreen,
        jfloat colorBlue,
        jfloat transparency,
        jfloat aRed,
        jfloat aGreen,
        jfloat aBlue,
        jfloat eRed,
        jfloat eGreen,
        jfloat eBlue,
        jfloat dRed,
        jfloat dGreen,
        jfloat dBlue,
        jfloat sRed,
        jfloat sGreen,
        jfloat sBlue,
        jfloat shininess,
	jint colorTarget,
        jboolean lightEnable)
{
    D3DMATERIAL9 material;

    GetDevice();

    switch (colorTarget) {
    case javax_media_j3d_Material_DIFFUSE:
	device->SetRenderState(D3DRS_DIFFUSEMATERIALSOURCE,
			       D3DMCS_COLOR1);
	break;
    case javax_media_j3d_Material_SPECULAR:
	device->SetRenderState(D3DRS_SPECULARMATERIALSOURCE,
			       D3DMCS_COLOR1);
	d3dCtx->resetColorTarget = true;
	break;
    case javax_media_j3d_Material_AMBIENT:
	device->SetRenderState(D3DRS_AMBIENTMATERIALSOURCE,
			       D3DMCS_COLOR1);
	d3dCtx->resetColorTarget = true;
	break;
    case javax_media_j3d_Material_AMBIENT_AND_DIFFUSE:
	device->SetRenderState(D3DRS_AMBIENTMATERIALSOURCE,
			       D3DMCS_COLOR1);
	device->SetRenderState(D3DRS_DIFFUSEMATERIALSOURCE,
			       D3DMCS_COLOR1);
	d3dCtx->resetColorTarget = true;
	break;
    case javax_media_j3d_Material_EMISSIVE:
	device->SetRenderState(D3DRS_EMISSIVEMATERIALSOURCE,
			       D3DMCS_COLOR1);
	d3dCtx->resetColorTarget = true;
	break;
    default:
	printf("Material updateNative: Uknown colorTarget %d\n", colorTarget);
    }

    material.Power = shininess;

    CopyColor(material.Emissive, eRed, eGreen, eBlue, 1.0f);
    CopyColor(material.Ambient,  aRed, aGreen, aBlue, 1.0f);
    CopyColor(material.Specular, sRed, sGreen, sBlue, 1.0f);

    d3dCtx->currentColor_a = transparency;

    if (lightEnable) {
	d3dCtx->currentColor_r = dRed;
	d3dCtx->currentColor_g = dGreen;
	d3dCtx->currentColor_b = dBlue;

	CopyColor(material.Diffuse,  dRed, dGreen, dBlue,
		  transparency);

    } else {
	d3dCtx->currentColor_r = colorRed;
	d3dCtx->currentColor_g = colorGreen;
	d3dCtx->currentColor_b = colorBlue;

	CopyColor(material.Diffuse, colorRed, colorGreen,
		  colorBlue, transparency);
    }


    d3dCtx->isLightEnable = lightEnable;
    device->SetRenderState(D3DRS_LIGHTING, lightEnable);
    device->SetMaterial(&material);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetTransparency(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint geometryType,
    jint polygonMode,
    jboolean lineAA,
    jboolean pointAA)
{
    GetDevice();

    // Line/Point  Antialiasing not support

    /*
    if (((((geometryType & LINE) != 0) || polygonMode == POLYGON_LINE)
	 && lineAA == JNI_TRUE) ||
        ((((geometryType & _POINT) != 0) || polygonMode == POLYGON_POINT)
	 && pointAA == JNI_TRUE)) {
	device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
	device->SetRenderState(D3DRS_SRCBLEND, D3DBLEND_SRCALPHA);
	device->SetRenderState(D3DRS_DESTBLEND, D3DBLEND_INVSRCALPHA);
    } else {
    */
	device->SetRenderState(D3DRS_ALPHABLENDENABLE, FALSE);
	//    }
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TransparencyAttributesRetained_updateNative(
    JNIEnv *env,
    jobject tr,
    jlong ctx,
    jfloat transparency,
    jint geometryType,
    jint polygonMode,
    jboolean lineAA,
    jboolean pointAA,
    jint transparencyMode,
    jint srcBlendFunction,
    jint dstBlendFunction)
{

    GetDevice();

    // No screen door transparency in D3D, use BLENDED
    // Don't know how to use STIPPLEDALPHA either.
    /*
    if (transparencyMode != TRANS_SCREEN_DOOR) {
	device->SetRenderState(D3DRS_STIPPLEDALPHA, FALSE);
    } else  {
	device->SetRenderState(D3DRS_STIPPLEDALPHA, TRUE);
    }
    */

    if (transparencyMode < javax_media_j3d_TransparencyAttributes_NONE) {
	/*
	((((geometryType & LINE) != 0) || polygonMode == POLYGON_LINE)
	 && lineAA == JNI_TRUE) ||
        ((((geometryType & _POINT) != 0) || polygonMode == POLYGON_POINT)
	 && pointAA == JNI_TRUE)) {
	*/
	device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
	device->SetRenderState(D3DRS_SRCBLEND,
			       blendFunctionTable[srcBlendFunction]);
	device->SetRenderState(D3DRS_DESTBLEND,
			       blendFunctionTable[dstBlendFunction]);
    } else {
	device->SetRenderState(D3DRS_ALPHABLENDENABLE, FALSE);
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetColoringAttributes(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat colorRed,
    jfloat colorGreen,
    jfloat colorBlue,
    jfloat transparency,
    jboolean lightEnable)
{

    GetDevice();

    if (!lightEnable) {
	d3dCtx->currentColor_r = colorRed;
	d3dCtx->currentColor_g = colorGreen;
	d3dCtx->currentColor_b = colorBlue;
	d3dCtx->currentColor_a = transparency;
    }
    device->SetRenderState(D3DRS_SHADEMODE, D3DSHADE_GOURAUD);
    // No line smooth in D3D
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_ColoringAttributesRetained_updateNative(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat dRed,
    jfloat dGreen,
    jfloat dBlue,
    jfloat colorRed,
    jfloat colorGreen,
    jfloat colorBlue,
    jfloat transparency,
    jboolean lightEnable,
    jint shadeModel)
{

    GetDevice();

    d3dCtx->currentColor_a = transparency;
    if (lightEnable) {
	d3dCtx->currentColor_r = dRed;
	d3dCtx->currentColor_g = dGreen;
	d3dCtx->currentColor_b = dBlue;
    } else {
	d3dCtx->currentColor_r = colorRed;
	d3dCtx->currentColor_g = colorGreen;
	d3dCtx->currentColor_b = colorBlue;
    }


    if (shadeModel == javax_media_j3d_ColoringAttributes_SHADE_FLAT) {
	device->SetRenderState(D3DRS_SHADEMODE, D3DSHADE_FLAT);
    } else {
	device->SetRenderState(D3DRS_SHADEMODE, D3DSHADE_GOURAUD);
    }
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_resetTextureNative(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint texUnitIndex)
{
    GetDevice();

    if (texUnitIndex < 0) {
	texUnitIndex = 0;
    }

    d3dCtx->texUnitStage = texUnitIndex;

    if (texUnitIndex >= d3dCtx->bindTextureIdLen) {
	return;
    }
    device->SetTexture(texUnitIndex, NULL);
    d3dCtx->bindTextureId[texUnitIndex] = -1;

    if (d3dCtx->texTransformSet[texUnitIndex]) {
	d3dCtx->texTransformSet[texUnitIndex] = false;
	device->SetTransform((D3DTRANSFORMSTATETYPE)
			     (D3DTS_TEXTURE0 + texUnitIndex),
			     &identityMatrix);
    }
    d3dCtx->texGenMode[texUnitIndex] = TEX_GEN_NONE;
    device->SetTextureStageState(texUnitIndex,
				 D3DTSS_TEXCOORDINDEX,
				 D3DTSS_TCI_PASSTHRU);
    device->SetTextureStageState(texUnitIndex,
				 D3DTSS_COLOROP, D3DTOP_SELECTARG1);
    device->SetTextureStageState(texUnitIndex,
				 D3DTSS_COLORARG1, D3DTA_TEXTURE);
    device->SetTextureStageState(texUnitIndex,
				 D3DTSS_ALPHAOP, D3DTOP_SELECTARG1);
    device->SetTextureStageState(texUnitIndex,
				 D3DTSS_ALPHAARG1, D3DTA_TEXTURE);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_bindTexture(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint objectId,
    jboolean enable)
{
    GetDevice();

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    if (!enable) {
	device->SetTexture(d3dCtx->texUnitStage, NULL);
	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = -1;
    } else {
	if (d3dCtx->bindTextureId[d3dCtx->texUnitStage] == objectId) {
	    return;
	}

	if (objectId >= d3dCtx->textureTableLen) {
	    DWORD i;
	    DWORD len = max(objectId+1, d3dCtx->textureTableLen << 1);
	    LPDIRECT3DTEXTURE9 *newTable = (LPDIRECT3DTEXTURE9 *)
		malloc(sizeof(LPDIRECT3DTEXTURE9) * len);

	    if (newTable == NULL) {
		printf("Not enough memory to alloc texture table of size %d.\n", len);
		return;
	    }
	    for (i=0; i < d3dCtx->textureTableLen; i++) {
		newTable[i] = d3dCtx->textureTable[i];
	    }
	    for (i=d3dCtx->textureTableLen; i < len; i++) {
		newTable[i] = NULL;
	    }
	    d3dCtx->textureTableLen = len;
	    SafeFree(d3dCtx->textureTable);
	    d3dCtx->textureTable = newTable;
	}

	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = objectId;
	if (d3dCtx->textureTable[objectId] != NULL) {
	    device->SetTexture(d3dCtx->texUnitStage,
			       d3dCtx->textureTable[objectId]);
	}
	// else we will bind this in updateTextureImage
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureFilterModes(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint minFilter,
    jint magFilter)
{
    GetDevice();


    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    d3dCtx->texLinearMode = false;

    /* set texture min filter */
    switch (minFilter) {
        case javax_media_j3d_Texture_FASTEST:
        case javax_media_j3d_Texture_BASE_LEVEL_POINT:
	    device->SetSamplerState(d3dCtx->texUnitStage,
					 D3DSAMP_MINFILTER, D3DTEXF_POINT);
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MIPFILTER, D3DTEXF_POINT);
	    break;
        case javax_media_j3d_Texture_BASE_LEVEL_LINEAR:
                    d3dCtx->texLinearMode = true;
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MINFILTER, D3DTEXF_LINEAR);
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MIPFILTER, D3DTEXF_POINT);
	    break;
        case javax_media_j3d_Texture_MULTI_LEVEL_POINT:
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MINFILTER, D3DTEXF_POINT);
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MIPFILTER, D3DTEXF_LINEAR);
	    break;
        case javax_media_j3d_Texture_NICEST:
        case javax_media_j3d_Texture_MULTI_LEVEL_LINEAR:
            d3dCtx->texLinearMode = true;
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MINFILTER, D3DTEXF_LINEAR);
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MIPFILTER, D3DTEXF_LINEAR);
	    break;
    }

    /* set texture mag filter */
    switch (magFilter) {
        case javax_media_j3d_Texture_FASTEST:
        case javax_media_j3d_Texture_BASE_LEVEL_POINT:
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MAGFILTER, D3DTEXF_POINT);
	    break;
        case javax_media_j3d_Texture_NICEST:
        case javax_media_j3d_Texture_BASE_LEVEL_LINEAR:
            d3dCtx->texLinearMode = true;
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_MAGFILTER, D3DTEXF_LINEAR);
	    break;
    }

    return;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureLodRange(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint baseLevel,
    jint maximumLevel,
    jfloat minimumLod,
    jfloat maximumLod)
{
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureLodOffset(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat lodOffsetS,
    jfloat lodOffsetT,
    jfloat lodOffsetR)
{
    /* not supported */
}

void updateTextureBoundary(JNIEnv *env,
			   jobject texture,
			   jlong ctx,
			   jint boundaryModeS,
			   jint boundaryModeT,
			   jint boundaryModeR,
			   jfloat boundaryRed,
			   jfloat boundaryGreen,
			   jfloat boundaryBlue,
			   jfloat boundaryAlpha)
{
    GetDevice();


    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    /* set texture wrap parameter */
    BOOL useBorderMode = FALSE;

    // D3D ignored border color in CLAMP mode.
    // Instead D3D use Border color in D3DTADDRESS_BORDER only.
    // So we approximate the effect by using D3DTADDRESS_BORDER
    // mode if linear filtering mode is used.

    switch (boundaryModeS) {
        case javax_media_j3d_Texture_WRAP:
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_ADDRESSU,
					 D3DTADDRESS_WRAP);
	    break;
        case javax_media_j3d_Texture_CLAMP:
	    if (!d3dCtx->texLinearMode || !d3dCtx->deviceInfo->texBorderModeSupport) {
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSU,
					     D3DTADDRESS_CLAMP);
	    } else {
		useBorderMode = TRUE;
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSU,
					     D3DTADDRESS_BORDER);
	    }
	    break;
    }

    switch (boundaryModeT) {
        case javax_media_j3d_Texture_WRAP:
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_ADDRESSV,
					 D3DTADDRESS_WRAP);
	    break;
        case javax_media_j3d_Texture_CLAMP:
	    if (!d3dCtx->texLinearMode || !d3dCtx->deviceInfo->texBorderModeSupport) {
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSV,
						 D3DTADDRESS_CLAMP);
	    } else {
		useBorderMode = TRUE;
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSV,
					     D3DTADDRESS_BORDER);
	    }
	    break;
    }

    if (boundaryModeR >= 0) {
	switch (boundaryModeR) {
        case javax_media_j3d_Texture_WRAP:
	    device->SetSamplerState (d3dCtx->texUnitStage,
					 D3DSAMP_ADDRESSW,
					 D3DTADDRESS_WRAP);
	    break;
        case javax_media_j3d_Texture_CLAMP:
	    if (!d3dCtx->texLinearMode || !d3dCtx->deviceInfo->texBorderModeSupport) {
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSW,
					     D3DTADDRESS_CLAMP);
	    } else {
		useBorderMode = TRUE;
		device->SetSamplerState (d3dCtx->texUnitStage,
					     D3DSAMP_ADDRESSW,
					     D3DTADDRESS_BORDER);
	    }
	    break;
	}
    }

    if (useBorderMode) {
	D3DCOLOR color = D3DCOLOR_COLORVALUE(boundaryRed, boundaryGreen,
					     boundaryBlue, boundaryAlpha);

	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_BORDERCOLOR,
				      *((DWORD *) &color));
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureBoundary(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint boundaryModeS,
    jint boundaryModeT,
    jfloat boundaryRed,
    jfloat boundaryGreen,
    jfloat boundaryBlue,
    jfloat boundaryAlpha)
{
    updateTextureBoundary(env, texture, ctx, boundaryModeS,
			  boundaryModeT, -1,
			  boundaryRed, boundaryGreen,
			  boundaryBlue, boundaryAlpha);

}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureSharpenFunc(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numPts,
    jfloatArray pts)
{
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureFilter4Func(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numPts,
    jfloatArray pts)
{
}


void updateTextureAnisotropicFilter(
    jlong ctx,
    jfloat degree)
{
    GetDevice();

    if (degree > 1) {
	DWORD deg = degree + 0.5f; // round float to int
	// This will overwrite the previous setting in
	// updateTextureFilterModes()
	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_MINFILTER,
				     D3DTEXF_ANISOTROPIC);
	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_MAGFILTER,
				     D3DTEXF_ANISOTROPIC);
	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_MIPFILTER,
				     D3DTEXF_ANISOTROPIC);

	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_MAXANISOTROPY, deg);
    } else {
	// updateTextureFilterModes() will always invoke before
	// updateTextureAnisotropicFilter() to set Filter mode
	// correctly.
	device->SetSamplerState (d3dCtx->texUnitStage,
				     D3DSAMP_MAXANISOTROPY, 1);
    }

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureAnisotropicFilter(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat degree)
{
     updateTextureAnisotropicFilter(ctx, degree);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureAnisotropicFilter(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat degree)
{
    GetCtx();

    if (d3dCtx->deviceInfo->maxTextureDepth > 0) {
	updateTextureAnisotropicFilter(ctx, degree);
    }

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureAnisotropicFilter(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat degree)
{
     updateTextureAnisotropicFilter(ctx, degree);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureSubImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint level,
    jint xoffset,
    jint yoffset,
    jint internalFormat,
    jint storedFormat,
    jint imgXOffset,
    jint imgYOffset,
    jint tilew,
    jint width,
    jint height,
    jbyteArray image)
{
    GetDevice();

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->textureTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateTextureSubImage  bind texture ID %d, textureTableLen %d, texUnitStage = %d \n", currBindTex, d3dCtx->textureTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DTEXTURE9 surf = d3dCtx->textureTable[currBindTex];

    if ((surf == NULL) ||
	((level > 0) && (!d3dCtx->deviceInfo->supportMipmap))) {
	return;
    }

    // update Image data
    if (storedFormat != FORMAT_USHORT_GRAY) {
	jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(image,   NULL);
	copyDataToSurface(storedFormat, internalFormat, xoffset, yoffset,
			  imgXOffset, imgYOffset,
			  width, height, tilew, byteData,
			  surf, level);
	env->ReleasePrimitiveArrayCritical(image, byteData, 0);

    } else {
	jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(image, NULL);
	copyDataToSurface(storedFormat, internalFormat, xoffset, yoffset,
			  imgXOffset, imgYOffset,
			  width, height, tilew, shortData,
			  surf, level);
	env->ReleasePrimitiveArrayCritical(image, shortData, 0);
    }

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureRetained_updateTextureImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numLevels,
    jint level,
    jint internalFormat,
    jint format,
    jint width,
    jint height,
    jint boundaryWidth,
    jbyteArray imageYup)
{
    GetDevice();



    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	if (debug) {
	    printf("Internal Error: texUnitState %d, bindTextureIDLen %d\n",
		   d3dCtx->texUnitStage, d3dCtx->bindTextureIdLen);
	}
	return;
    }


    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->textureTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateTextureImage  bind texture ID %d, textureTableLen %d, texUnitStage = %d \n",  currBindTex, d3dCtx->textureTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DTEXTURE9 surf = d3dCtx->textureTable[currBindTex];

    if (level == 0) {
	if (surf != NULL) {
	    // see if no. of mipmap level change

	    if (surf->GetLevelCount() != numLevels) {
		d3dCtx->freeResource(surf);
		d3dCtx->textureTable[currBindTex] = NULL;
		surf = NULL;
	    }
	}

	if (surf == NULL) {
	// Need to create surface
	    surf = createTextureSurface(d3dCtx, numLevels, internalFormat,
					width, height);

	    if (surf == NULL) {
		return;
	    }

	    d3dCtx->textureTable[currBindTex] = surf;
	}
    } else {
	if (surf == NULL) {
	    return;
	}
    }

    if ((level > 0) && (!d3dCtx->deviceInfo->supportMipmap)) {
	if (debug) {
	    printf("mipmap not support\n");
	}
	return;
    }

    // update Image data
    if (imageYup != NULL) {
	if (format != FORMAT_USHORT_GRAY) {
	    jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(imageYup,   NULL);
	    copyDataToSurface(format, internalFormat, 0, 0, 0, 0,
			      width, height, width, byteData,
			      surf, level);
	    env->ReleasePrimitiveArrayCritical(imageYup, byteData, 0);

	} else {
	    jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(imageYup, NULL);
	    copyDataToSurface(format, internalFormat, 0, 0, 0, 0,
			      width, height, width,  shortData,
			      surf, level);
	    env->ReleasePrimitiveArrayCritical(imageYup, shortData, 0);
	}
    }


    device->SetTexture(d3dCtx->texUnitStage, surf);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture2DRetained_bindTexture(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint objectId,
    jboolean enable)
{
    Java_javax_media_j3d_TextureRetained_bindTexture(env, texture,
			ctx, objectId, enable);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture2DRetained_updateTextureSubImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint level,
    jint xoffset,
    jint yoffset,
    jint internalFormat,
    jint storedFormat,
    jint imgXOffset,
    jint imgYOffset,
    jint tilew,
    jint width,
    jint height,
    jbyteArray image)
{
    Java_javax_media_j3d_TextureRetained_updateTextureSubImage(
	env, texture, ctx, level, xoffset, yoffset, internalFormat,
	storedFormat, imgXOffset, imgYOffset, tilew, width, height, image);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture2DRetained_updateTextureImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numLevels,
    jint level,
    jint internalFormat,
    jint format,
    jint width,
    jint height,
    jint boundaryWidth,
    jbyteArray imageYup)
{
    Java_javax_media_j3d_TextureRetained_updateTextureImage(env, texture,
		ctx, numLevels, level, internalFormat, format,
		width, height, boundaryWidth, imageYup);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture2DRetained_updateDetailTextureParameters(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint detailTextureMode,
    jint detailTextureLevel,
    jint numPts,
    jfloatArray funcPts)
{
    // Not support
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_bindTexture(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint objectId,
    jboolean enable)
{
    GetDevice();

    if ((d3dCtx->deviceInfo->maxTextureDepth <= 0) ||
	(d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen)) {
	return;
    }

    if (!enable) {
	device->SetTexture(d3dCtx->texUnitStage, NULL);
	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = -1;
    } else {
	if (d3dCtx->bindTextureId[d3dCtx->texUnitStage] == objectId) {
	    return;
	}
	if (objectId >= d3dCtx->volumeTableLen) {
	    DWORD i;
	    DWORD len = max(objectId+1, d3dCtx->volumeTableLen << 1);
	    LPDIRECT3DVOLUMETEXTURE9 *newTable = (LPDIRECT3DVOLUMETEXTURE9 *)
		malloc(sizeof(LPDIRECT3DVOLUMETEXTURE9) * len);

	    if (newTable == NULL) {
		printf("Not enough memory to alloc volume texture table of size %d.\n", len);
		return;
	    }
	    for (i=0; i < d3dCtx->volumeTableLen; i++) {
		newTable[i] = d3dCtx->volumeTable[i];
	    }
	    for (i=d3dCtx->volumeTableLen; i < len; i++) {
		newTable[i] = NULL;
	    }
	    d3dCtx->volumeTableLen = len;
	    SafeFree(d3dCtx->volumeTable);
	    d3dCtx->volumeTable = newTable;
	}

	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = objectId;
	if (d3dCtx->volumeTable[objectId] != NULL) {
	    device->SetTexture(d3dCtx->texUnitStage,
			       d3dCtx->volumeTable[objectId]);
	}
	// else we will bind this in updateTextureImage
    }

}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureFilterModes(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint minFilter,
    jint magFilter)
{
    GetCtx();

    if (d3dCtx->deviceInfo->maxTextureDepth > 0) {
	Java_javax_media_j3d_TextureRetained_updateTextureFilterModes(
			      env, texture, ctx, minFilter, magFilter);
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureLodRange(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint baseLevel,
    jint maximumLevel,
    jfloat minimumLod,
    jfloat maximumLod)
{
    // Not support
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureLodOffset(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat lodOffsetS,
    jfloat lodOffsetT,
    jfloat lodOffsetR)
{
    /* not supported */
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureBoundary(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint boundaryModeS,
    jint boundaryModeT,
    jint boundaryModeR,
    jfloat boundaryRed,
    jfloat boundaryGreen,
    jfloat boundaryBlue,
    jfloat boundaryAlpha)
{

    GetCtx();

    if (d3dCtx->deviceInfo->maxTextureDepth > 0) {

	updateTextureBoundary(
			      env, texture, ctx,
			      boundaryModeS,
			      boundaryModeT,
			      boundaryModeR,
			      boundaryRed,
			      boundaryGreen,
			      boundaryBlue,
			      boundaryAlpha);
    }
}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numLevels,
    jint level,
    jint internalFormat,
    jint format,
    jint width,
    jint height,
    jint depth,
    jint boundaryWidth,
    jbyteArray imageYup)
{

    GetDevice();

    if (d3dCtx->deviceInfo->maxTextureDepth <= 0) {
	return;
    }

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	if (debug) {
	    printf("Internal Error: texUnitState %d, bindTextureIDLen %d\n",
		   d3dCtx->texUnitStage, d3dCtx->bindTextureIdLen);
	}
	return;
    }


    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->volumeTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateTexture3DImage  bind texture ID %d, volumeTableLen %d, texUnitStage = %d \n",  currBindTex, d3dCtx->volumeTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DVOLUMETEXTURE9 surf = d3dCtx->volumeTable[currBindTex];

    if (level == 0) {
	if (surf != NULL) {
	    // see if no. of mipmap level change

	    if (surf->GetLevelCount() != numLevels) {
		d3dCtx->freeResource(surf);
		d3dCtx->volumeTable[currBindTex] = NULL;
		surf = NULL;
	    }
	}

	if (surf == NULL) {
	    surf = createVolumeTexture(d3dCtx, numLevels, internalFormat,
				       width, height, depth);
	    if (surf == NULL) {
		return;
	    }

	    d3dCtx->volumeTable[currBindTex] = surf;
	}
    } else {
	if (surf == NULL) {
	    return;
	}
    }

    if ((level > 0) && (!d3dCtx->deviceInfo->supportMipmap)) {
	if (debug) {
	    printf("mipmap not support\n");
	}
	return;
    }

    // update Image data
    if (imageYup != NULL) {
	if (format != FORMAT_USHORT_GRAY) {
	    jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(imageYup,   NULL);
	    copyDataToVolume(format, internalFormat, 0, 0, 0, 0, 0, 0,
			     width, height, depth, width, height, byteData,
			     surf, level);
	    env->ReleasePrimitiveArrayCritical(imageYup, byteData, 0);

	} else {
	    jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(imageYup, NULL);
	    copyDataToVolume(format, internalFormat, 0, 0, 0, 0, 0, 0,
			      width, height, depth, width, height, shortData,
			      surf, level);
	    env->ReleasePrimitiveArrayCritical(imageYup, shortData, 0);
	}
    }
    device->SetTexture(d3dCtx->texUnitStage, surf);

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Texture3DRetained_updateTextureSubImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint level,
    jint xoffset,
    jint yoffset,
    jint zoffset,
    jint internalFormat,
    jint storedFormat,
    jint imgXOffset,
    jint imgYOffset,
    jint imgZOffset,
    jint tilew,
    jint tileh,
    jint width,
    jint height,
    jint depth,
    jbyteArray image)
{
    GetDevice();

    if ((d3dCtx->deviceInfo->maxTextureDepth <= 0) ||
	(d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen)) {
	return;
    }

    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->volumeTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateTexture3DSubImage  bind texture ID %d, volumeableLen %d, texUnitStage = %d \n", currBindTex, d3dCtx->volumeTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DVOLUMETEXTURE9 surf = d3dCtx->volumeTable[currBindTex];

    if ((surf == NULL) ||
	((level > 0) && (!d3dCtx->deviceInfo->supportMipmap))) {
	return;
    }

    // update Image data
    if (storedFormat != FORMAT_USHORT_GRAY) {
	jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(image,   NULL);
	copyDataToVolume(storedFormat, internalFormat, xoffset,
			 yoffset, zoffset, imgXOffset, imgYOffset,
			 imgZOffset, width, height, depth,
			 tilew, tileh, byteData,
			 surf, level);
	env->ReleasePrimitiveArrayCritical(image, byteData, 0);

    } else {
	jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(image, NULL);
	copyDataToVolume(storedFormat, internalFormat, xoffset,
			 yoffset, zoffset,
			 imgXOffset, imgYOffset, imgZOffset,
			 width, height, depth, tilew, tileh, shortData,
			 surf, level);
	env->ReleasePrimitiveArrayCritical(image, shortData, 0);
    }

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_bindTexture(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint objectId,
    jboolean enable)
{
    GetDevice();

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    if (!enable) {
	device->SetTexture(d3dCtx->texUnitStage, NULL);
	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = -1;
    } else {

	if (d3dCtx->bindTextureId[d3dCtx->texUnitStage] == objectId) {
	    return;
	}

	if (objectId >= d3dCtx->cubeMapTableLen) {
	    DWORD i;
	    DWORD len = max(objectId+1, d3dCtx->cubeMapTableLen << 1);
	    LPDIRECT3DCUBETEXTURE9 *newTable = (LPDIRECT3DCUBETEXTURE9 *)
		malloc(sizeof(LPDIRECT3DCUBETEXTURE9) * len);

	    if (newTable == NULL) {
		printf("Not enough memory to alloc cubeMap table of size %d.\n", len);
		return;
	    }
	    for (i=0; i < d3dCtx->cubeMapTableLen; i++) {
		newTable[i] = d3dCtx->cubeMapTable[i];
	    }
	    for (i=d3dCtx->cubeMapTableLen; i < len; i++) {
		newTable[i] = NULL;
	    }
	    d3dCtx->cubeMapTableLen = len;
	    SafeFree(d3dCtx->cubeMapTable);
	    d3dCtx->cubeMapTable = newTable;
	}

	d3dCtx->bindTextureId[d3dCtx->texUnitStage] = objectId;
	if (d3dCtx->cubeMapTable[objectId] != NULL) {
	    device->SetTexture(d3dCtx->texUnitStage,
			       d3dCtx->cubeMapTable[objectId]);
	}
	// else we will bind this in updateTextureImage
    }
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureFilterModes(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint minFilter,
    jint magFilter)
{
     Java_javax_media_j3d_TextureRetained_updateTextureFilterModes(env,
		   texture, ctx, minFilter, magFilter);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureLodRange(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint baseLevel,
    jint maximumLevel,
    jfloat minimumLod,
    jfloat maximumLod)
{
    // not support
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureLodOffset(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jfloat lodOffsetS,
    jfloat lodOffsetT,
    jfloat lodOffsetR)
{
    /* not supported */
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureBoundary(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint boundaryModeS,
    jint boundaryModeT,
    jfloat boundaryRed,
    jfloat boundaryGreen,
    jfloat boundaryBlue,
    jfloat boundaryAlpha)
{
    updateTextureBoundary(env, texture, ctx, boundaryModeS,
			  boundaryModeT, -1, boundaryRed,
			  boundaryGreen, boundaryBlue,
			  boundaryAlpha);

}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureSubImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint face,
    jint level,
    jint xoffset,
    jint yoffset,
    jint internalFormat,
    jint storedFormat,
    jint imgXOffset,
    jint imgYOffset,
    jint tilew,
    jint width,
    jint height,
    jbyteArray image)
{
    GetDevice();

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	return;
    }

    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->cubeMapTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateCubeMapSubImage  bind texture ID %d, cubeMapTableLen %d, texUnitStage = %d \n", currBindTex, d3dCtx->cubeMapTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DCUBETEXTURE9 surf = d3dCtx->cubeMapTable[currBindTex];

    if ((surf == NULL) ||
	((level > 0) && (!d3dCtx->deviceInfo->supportMipmap))) {
	return;
    }

    // update Image data
    if (storedFormat != FORMAT_USHORT_GRAY) {
	jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(image,   NULL);
	copyDataToCubeMap(storedFormat, internalFormat,
			  xoffset, yoffset,
			  imgXOffset, imgYOffset,
			  width, height,
			  tilew, byteData,
			  surf, level, face);
	env->ReleasePrimitiveArrayCritical(image, byteData, 0);

    } else {
	jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(image, NULL);
	copyDataToCubeMap(storedFormat, internalFormat,
			  xoffset, yoffset,
			  imgXOffset, imgYOffset,
			  width, height,
			  tilew, shortData,
			  surf, level, face);
	env->ReleasePrimitiveArrayCritical(image, shortData, 0);
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureCubeMapRetained_updateTextureImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint face,
    jint numLevels,
    jint level,
    jint internalFormat,
    jint format,
    jint width,
    jint height,
    jint boundaryWidth,
    jbyteArray imageYup)
{
    GetDevice();

    if (d3dCtx->texUnitStage >= d3dCtx->bindTextureIdLen) {
	if (debug) {
	    printf("Internal Error: texUnitState %d, bindTextureIDLen %d\n",
		   d3dCtx->texUnitStage, d3dCtx->bindTextureIdLen);
	}
	return;
    }


    INT currBindTex = d3dCtx->bindTextureId[d3dCtx->texUnitStage];

    if ((currBindTex < 1) ||
	(currBindTex >= d3dCtx->cubeMapTableLen)) {
	if (debug) {
	    printf("Internal Error : UpdateCubeMapImage  bind texture ID %d, cubeMapTableLen %d, texUnitStage = %d \n",  currBindTex, d3dCtx->cubeMapTableLen, d3dCtx->texUnitStage);
	}
	return;
    }

    LPDIRECT3DCUBETEXTURE9 surf = d3dCtx->cubeMapTable[currBindTex];

    if (level == 0) {
	if (surf != NULL) {
	    // see if no. of mipmap level change

	    if (surf->GetLevelCount() != numLevels) {
		d3dCtx->freeResource(surf);
		d3dCtx->cubeMapTable[currBindTex] = NULL;
		surf = NULL;
	    }
	}

	if (surf == NULL) {
	// Need to create surface
	    surf = createCubeMapTexture(d3dCtx, numLevels, internalFormat,
					width, height);
	    if (surf == NULL) {
		return;
	    }

	    d3dCtx->cubeMapTable[currBindTex] = surf;
	}
    } else {
	if (surf == NULL) {
	    return;
	}
    }

    if ((level > 0) && (!d3dCtx->deviceInfo->supportMipmap)) {
	if (debug) {
	    printf("mipmap not support\n");
	}
	return;
    }

    // update Image data
    if (imageYup != NULL) {
	if (format != FORMAT_USHORT_GRAY) {
	    jbyte *byteData = (jbyte *) env->GetPrimitiveArrayCritical(imageYup,   NULL);
	    copyDataToCubeMap(format, internalFormat, 0, 0, 0, 0,
			      width, height, width, byteData,
			      surf, level, face);
	    env->ReleasePrimitiveArrayCritical(imageYup, byteData, 0);

	} else {
	    jshort *shortData = (jshort *) env->GetPrimitiveArrayCritical(imageYup, NULL);
	    copyDataToCubeMap(format, internalFormat, 0, 0, 0, 0,
			      width, height, width,  shortData,
			      surf, level, face);
	    env->ReleasePrimitiveArrayCritical(imageYup, shortData, 0);
	}
    }

    device->SetTexture(d3dCtx->texUnitStage, surf);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_DetailTextureImage_bindTexture(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint objectId)
{
    // NOT SUPPORTED
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_DetailTextureImage_updateTextureImage(
    JNIEnv *env,
    jobject texture,
    jlong ctx,
    jint numLevels,
    jint level,
    jint internalFormat,
    jint format,
    jint width,
    jint height,
    jint boundaryWidth,
    jbyteArray imageYup)
{
    // NOT SUPPORTED
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_Canvas3D_decal1stChildSetup(
    JNIEnv *env,
    jobject obj,
    jlong ctx)
{
    GetDevice2();

    device->SetRenderState(D3DRS_STENCILENABLE, TRUE);
    device->Clear(0, NULL, D3DCLEAR_STENCIL, 0, 1.0, 0);
    device->SetRenderState(D3DRS_STENCILFUNC, D3DCMP_ALWAYS);
    device->SetRenderState(D3DRS_STENCILREF, 0x1);
    device->SetRenderState(D3DRS_STENCILMASK, 0x1);
    device->SetRenderState(D3DRS_STENCILFAIL,
			   D3DSTENCILOP_KEEP);
    device->SetRenderState(D3DRS_STENCILZFAIL,
			   D3DSTENCILOP_KEEP);
    device->SetRenderState(D3DRS_STENCILPASS,
			   D3DSTENCILOP_REPLACE);
    return d3dCtx->zEnable;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_decalNthChildSetup(
    JNIEnv *env,
    jobject obj,
    jlong ctx)

{
    GetDevice();

    d3dCtx->zEnable = FALSE;
    device->SetRenderState(D3DRS_ZENABLE, FALSE);
    device->SetRenderState(D3DRS_STENCILFUNC, D3DCMP_EQUAL);
    device->SetRenderState(D3DRS_STENCILREF, 0x1);
    device->SetRenderState(D3DRS_STENCILMASK, 0x1);
    device->SetRenderState(D3DRS_STENCILFAIL,
			   D3DSTENCILOP_KEEP);
    device->SetRenderState(D3DRS_STENCILZFAIL,
			   D3DSTENCILOP_KEEP);
    device->SetRenderState(D3DRS_STENCILPASS,
			   D3DSTENCILOP_KEEP);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_decalReset(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean depthBufferEnable)
{
    GetDevice();

    device->SetRenderState(D3DRS_STENCILENABLE, FALSE);

    if (depthBufferEnable) {
	d3dCtx->zEnable = TRUE;
	device->SetRenderState(D3DRS_ZENABLE, TRUE);
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_ctxUpdateEyeLightingEnable(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean localEyeLightingEnable)
{
    GetDevice();
    device->SetRenderState(D3DRS_LOCALVIEWER, localEyeLightingEnable);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_activeTextureUnit(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint index)
{
    GetDevice();
    // If this index is greater than max support stage,
    // then subsequence texture operation will ignore.
    if (index < 0) {
        index = 0;
    }

    d3dCtx->texUnitStage = index;
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_TextureUnitStateRetained_updateTextureUnitState(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint index,
    jboolean enable)
{
    GetDevice();
    // If this index is greater than max support stage,
    // then subsequence texture operation will ignore.
    if (index <= 0) {
	index = 0;
    }

    d3dCtx->texUnitStage = index;

    if (!enable && (index < d3dCtx->bindTextureIdLen)) {
	device->SetTexture(index, NULL);
	d3dCtx->bindTextureId[index] = -1;
    }
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setDepthFunc(
    JNIEnv * env,
    jobject obj,
    jlong ctx,
    jint func)
{
    GetDevice();

    if (func == javax_media_j3d_RenderingAttributesRetained_LESS) {
	device->SetRenderState(D3DRS_ZFUNC, D3DCMP_LESS);
    } else if (func ==
	       javax_media_j3d_RenderingAttributesRetained_LEQUAL) {
	device->SetRenderState(D3DRS_ZFUNC, D3DCMP_LESSEQUAL);
    }

}



extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setBlendColor(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jfloat colorRed,
    jfloat colorGreen,
    jfloat colorBlue,
    jfloat colorAlpha)
{
    // Not support in D3D
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setBlendFunc(
    JNIEnv * env,
    jobject obj,
    jlong ctx,
    jint srcBlendFunction,
    jint dstBlendFunction)
{
    GetDevice();

    device->SetRenderState(D3DRS_ALPHABLENDENABLE, TRUE);
    device->SetRenderState(D3DRS_SRCBLEND,
			   blendFunctionTable[srcBlendFunction]);
    device->SetRenderState(D3DRS_DESTBLEND,
			   blendFunctionTable[dstBlendFunction]);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_setFogEnableFlag(
    JNIEnv * env,
    jobject obj,
    jlong ctx,
    jboolean enable)
{
    GetDevice();

    device->SetRenderState(D3DRS_FOGENABLE, enable);
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_updateSeparateSpecularColorEnable(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jboolean enable)
{
}

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_Canvas3D_updateTexUnitStateMap(
    JNIEnv *env,
    jobject obj,
    jlong ctx,
    jint numActiveTexUnit,
    jintArray texUnitStateMapArray)
{
    if ((texUnitStateMapArray != NULL) && (numActiveTexUnit > 0)) {
	GetDevice();

        jint* texUnitStateMap = (jint *) env->GetPrimitiveArrayCritical(
                                                texUnitStateMapArray, NULL);
	int genMode;
	int ts;
	for (int i = 0; i < numActiveTexUnit; i++) {
	    genMode = setTextureStage(d3dCtx, device, i, texUnitStateMap[i]);
	    if (genMode != TEX_GEN_AUTO) {
		ts = d3dCtx->texStride[i];
		if (ts == 0) {
		    /*
		      In multiTexture case when no tex defined in non object
		      linear mode.
		    */
		    ts = d3dCtx->texCoordFormat[i];
		}
	    } else {
		ts = d3dCtx->texCoordFormat[i];
	    }
	    setTexTransformStageFlag(d3dCtx, device, i, ts, genMode);
	}

        env->ReleasePrimitiveArrayCritical(texUnitStateMapArray,
                                                texUnitStateMap, 0);
    }
}


// Fix issue 221 : Temporary stub until Cg is implemented
/*
 * Class:     javax_media_j3d_NativePipeline
 * Method:    loadNativeCgLibrary
 * Signature: ([Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL
Java_javax_media_j3d_NativePipeline_loadNativeCgLibrary(
    JNIEnv *env,
    jobject thiz,
    jobjectArray libpath)
{
    return JNI_FALSE;
}
