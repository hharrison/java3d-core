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

#include <stdio.h>
#include <jni.h>
#include "gldefs.h"

#ifdef SOLARIS
#pragma weak glDrawCompressedGeomSUNX

#else
static void glDrawCompressedGeomSUNX(GLint size, GLubyte *data) {
    fprintf(stderr, "Warning: glDrawCompressedGeomSUNX() not supported\n") ;
}
#endif /* SOLARIS */


JNIEXPORT
void JNICALL Java_javax_media_j3d_CompressedGeometryRetained_execute
    (JNIEnv *env, jobject obj, jlong ctxInfo, jint version, jint bufferType,
     jint bufferContents, jint renderFlags, jint offset, jint size,
     jbyteArray geometry)
{
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;

    jbyte *cg = (*env)->GetPrimitiveArrayCritical(env, geometry, 0) ;

    /* ignore offset and pass header at 0 along with cg data */
    if(ctxProperties->geometry_compression_sunx)
	glDrawCompressedGeomSUNX((GLint)(size + 48), (GLubyte *)cg) ;
    
    (*env)->ReleasePrimitiveArrayCritical(env, geometry, cg, 0) ;
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressByRef
    (JNIEnv *env, jobject obj, jlong ctxInfo)
{
    return JNI_FALSE ;
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressHW
    (JNIEnv *env, jobject obj, jlong ctxInfo, jint majorVersion, jint minorVersion)
{
    /*
     * Return true if hardware decompression is supported for the given
     * version number of the compressed geometry.
     */

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 
    jlong ctx = ctxProperties->context;

    return
	(ctxProperties->geometry_compression_accelerated == 1) &&
	((majorVersion < ctxProperties->geometry_compression_accelerated_major_version) ||
	 ((majorVersion == ctxProperties->geometry_compression_accelerated_major_version) &&
	  (minorVersion <= ctxProperties->geometry_compression_accelerated_minor_version))) ;
}
