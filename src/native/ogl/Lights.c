/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <jni.h>

#include "gldefs.h"

#ifdef DEBUG
/* Uncomment the following for VERBOSE debug messages */
/* #define VERBOSE */
#endif /* DEBUG */


const float black[4] = { 0.0f, 0.0f, 0.0f, 1.0f };

JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_updateDirectionalLight(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,
    jint lightSlot,
    jfloat red,
    jfloat green,
    jfloat blue,
    jfloat dirx,
    jfloat diry,
    jfloat dirz)
{
    int lightNum;
    float values[4];
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo;
    jlong ctx = ctxProperties->context;

#ifdef VERBOSE
    fprintf(stderr, 
            "Directional Light %d: %f %f %f direction, %f %f %f color\n", 
	    lightSlot, dirx, diry, dirz, red, green, blue);
#endif
    lightNum = GL_LIGHT0 + lightSlot;
    values[0] = red;
    values[1] = green;
    values[2] = blue;
    values[3] = 1.0f;
    glLightfv(lightNum, GL_DIFFUSE, values);
    glLightfv(lightNum, GL_SPECULAR, values);
    values[0] = -dirx;
    values[1] = -diry;
    values[2] = -dirz;
    values[3] = 0.0f;
    glLightfv(lightNum, GL_POSITION, values);
    glLightfv(lightNum, GL_AMBIENT, black);
    glLightf(lightNum, GL_CONSTANT_ATTENUATION, 1.0f);
    glLightf(lightNum, GL_LINEAR_ATTENUATION, 0.0f);
    glLightf(lightNum, GL_QUADRATIC_ATTENUATION, 0.0f);
    glLightf(lightNum, GL_SPOT_EXPONENT, 0.0f);
    glLightf(lightNum, GL_SPOT_CUTOFF, 180.0f);
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_updatePointLight(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
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
    int lightNum;
    float values[4];

#ifdef VERBOSE
    fprintf(stderr, "Positional Light %d: %f %f %f position, %f %f %f color\n\t %f %f %f attenuation\n", 
	    lightSlot, posx, posy, posz, red, green, blue, 
	    attenx, atteny, attenz);
#endif
   
    lightNum = GL_LIGHT0 + lightSlot;
    values[0] = red;
    values[1] = green;
    values[2] = blue;
    values[3] = 1.0f;
    glLightfv(lightNum, GL_DIFFUSE, values);
    glLightfv(lightNum, GL_SPECULAR, values);
    glLightfv(lightNum, GL_AMBIENT, black);
    values[0] = posx;
    values[1] = posy;
    values[2] = posz;
    glLightfv(lightNum, GL_POSITION, values);
    glLightf(lightNum, GL_CONSTANT_ATTENUATION, attenx);
    glLightf(lightNum, GL_LINEAR_ATTENUATION, atteny);
    glLightf(lightNum, GL_QUADRATIC_ATTENUATION, attenz);
    glLightf(lightNum, GL_SPOT_EXPONENT, 0.0f);
    glLightf(lightNum, GL_SPOT_CUTOFF, 180.0f);
}

JNIEXPORT
void JNICALL Java_javax_media_j3d_NativePipeline_updateSpotLight(
    JNIEnv *env, 
    jobject obj,
    jlong ctxInfo,    
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
    int lightNum;
    float values[4];

#ifdef VERBOSE
    fprintf(stderr, "Spot Light %d: %f %f %f position, %f %f %f color\n\t %f %f %f attenuation\n\t %f %f %f direction, %f spreadAngle, %f concentration\n",
            lightSlot, posx, posy, posz, red, green, blue, 
	    attenx, atteny, attenz, dirx, diry, dirz, 
	    spreadAngle*180.0 / M_PI, concentration);
#endif
    lightNum = GL_LIGHT0 + lightSlot;
    values[0] = red;
    values[1] = green;
    values[2] = blue;
    values[3] = 1.0f;
    glLightfv(lightNum, GL_DIFFUSE, values);
    glLightfv(lightNum, GL_SPECULAR, values);
    glLightfv(lightNum, GL_AMBIENT, black);
    values[0] = posx;
    values[1] = posy;
    values[2] = posz;
    glLightfv(lightNum, GL_POSITION, values);
    glLightf(lightNum, GL_CONSTANT_ATTENUATION, attenx);
    glLightf(lightNum, GL_LINEAR_ATTENUATION, atteny);
    glLightf(lightNum, GL_QUADRATIC_ATTENUATION, attenz);
    values[0] = dirx;
    values[1] = diry;
    values[2] = dirz;
    glLightfv(lightNum, GL_SPOT_DIRECTION, values);
    glLightf(lightNum, GL_SPOT_EXPONENT, concentration);
    glLightf(lightNum, GL_SPOT_CUTOFF, (float) (spreadAngle * 180.0f / M_PI));
}

