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

 #define D3DLIGHT_RANGE_MAX sqrt(FLT_MAX)

extern "C" JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_updateDirectionalLight(
    JNIEnv *env,
    jobject obj,
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
void JNICALL Java_javax_media_j3d_NativePipeline_updatePointLight(
    JNIEnv *env, 
    jobject obj, 
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
void JNICALL Java_javax_media_j3d_NativePipeline_updateSpotLight(
    JNIEnv *env, 
    jobject obj, 
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

