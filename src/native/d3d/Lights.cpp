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

 #define D3DLIGHT_RANGE_MAX sqrt(FLT_MAX)

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_DirectionalLightRetained_updateLight(
    JNIEnv *env,
    jobject light,
    jlong ctx,
    jint lightSlot,
    jfloat red,
    jfloat green,
    jfloat blue,
    jfloat dirx,
    jfloat diry,
    jfloat dirz)
{
    D3DLIGHT9 d3dLight;

    GetDevice();

    d3dLight.Type = D3DLIGHT_DIRECTIONAL;

    d3dLight.Direction.x = dirx;
    d3dLight.Direction.y = diry;
    d3dLight.Direction.z = dirz;

    // Although spec. said this value is ignore, but
    // if we don't set it the debug version will fail
    // to set directional light
    d3dLight.Range = D3DLIGHT_RANGE_MAX;
    // D3D will not clamp to range [0, 1] automatically like OGL did
    /*
    Clamp(red);
    Clamp(green);
    Clamp(blue);
    */
    CopyColor(d3dLight.Diffuse, red, green, blue, 1.0f);
    CopyColor(d3dLight.Ambient, 0.0f, 0.0f, 0.0f, 1.0f);
    CopyColor(d3dLight.Specular, red, green, blue, 1.0f);

    device->SetLight(lightSlot, &d3dLight);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_PointLightRetained_updateLight(
    JNIEnv *env, 
    jobject light, 
    jlong ctx,
    jint lightSlot,
    jfloat red,
    jfloat green,
    jfloat blue,
    jfloat attenx,
    jfloat atteny,
    jfloat attenz,
    jfloat posx,
    jfloat posy,
    jfloat posz)
{
    D3DLIGHT9 d3dLight;

    GetDevice();

    d3dLight.Type = D3DLIGHT_POINT;

    d3dLight.Position.x = posx;
    d3dLight.Position.y = posy;
    d3dLight.Position.z = posz;
    /*
    Clamp(red);
    Clamp(green);
    Clamp(blue);
    */
    CopyColor(d3dLight.Diffuse, red, green, blue, 1.0f);
    CopyColor(d3dLight.Ambient, 0.0f, 0.0f, 0.0f, 1.0f);
    CopyColor(d3dLight.Specular, red, green, blue, 1.0f);

    d3dLight.Attenuation0 = attenx;
    d3dLight.Attenuation1 = atteny;
    d3dLight.Attenuation2 = attenz;
    d3dLight.Range = D3DLIGHT_RANGE_MAX;

    device->SetLight(lightSlot, &d3dLight);
}


extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_SpotLightRetained_updateLight(
    JNIEnv *env, 
    jobject light, 
    jlong ctx,
    jint lightSlot,
    jfloat red,
    jfloat green,
    jfloat blue,
    jfloat attenx,
    jfloat atteny,
    jfloat attenz,
    jfloat posx,
    jfloat posy,
    jfloat posz,
    jfloat spreadAngle,
    jfloat concentration,
    jfloat dirx,
    jfloat diry,
    jfloat dirz)
{
    D3DLIGHT9 d3dLight;

    GetDevice();

    d3dLight.Type = D3DLIGHT_SPOT;
    d3dLight.Direction.x = dirx;
    d3dLight.Direction.y = diry;
    d3dLight.Direction.z = dirz;
    d3dLight.Position.x = posx;
    d3dLight.Position.y = posy;
    d3dLight.Position.z = posz;
    /*
    Clamp(red);
    Clamp(green);
    Clamp(blue);
    */
    CopyColor(d3dLight.Diffuse, red, green, blue, 1.0f);
    CopyColor(d3dLight.Ambient, 0.0f, 0.0f, 0.0f, 1.0f);
    CopyColor(d3dLight.Specular, red, green, blue, 1.0f);

    d3dLight.Attenuation0 = attenx;
    d3dLight.Attenuation1 = atteny;
    d3dLight.Attenuation2 = attenz;
    d3dLight.Range = D3DLIGHT_RANGE_MAX;
    d3dLight.Theta = 0;
    d3dLight.Phi = spreadAngle*2;
    if (d3dLight.Phi > PI) {
	d3dLight.Phi = PI;
    }
    d3dLight.Falloff = concentration;

    device->SetLight(lightSlot, &d3dLight);
}

