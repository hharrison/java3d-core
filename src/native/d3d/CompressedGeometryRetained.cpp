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
void JNICALL Java_javax_media_j3d_CompressedGeometryRetained_execute
    (JNIEnv *env, jobject obj, jlong ctx, jint version, jint bufferType,
     jint bufferContents, jint renderFlags, jint offset, jint size,
     jbyteArray geometry)
{
    // Not support by D3D, problem should not call this.
    printf("Error: CompressedGeometryRetained execute should not invoke by D3D");
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressByRef
    (JNIEnv *env, jobject obj, jlong ctx)
{
    // Not support by D3D
    return JNI_FALSE ;
}

extern "C" JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressHW
    (JNIEnv *env, jobject obj, jlong ctx, jint majorVersion, jint minorVersion)
{
    // Not support by D3D
    return JNI_FALSE ;
}
