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

#include <stdio.h>
#include <jni.h>
#include "gldefs.h"

#ifdef OBSOLETE_HW_COMPRESSED_GEOM
/* #if defined(SOLARIS) && defined(__sparc) */
/* Solaris/Sparc only, all others define this as a noop */
#pragma weak glDrawCompressedGeomSUNX
#endif /* OBSOLETE_HW_COMPRESSED_GEOM */


JNIEXPORT
void JNICALL Java_javax_media_j3d_CompressedGeometryRetained_execute
    (JNIEnv *env, jobject obj, jlong ctxInfo, jint version, jint bufferType,
     jint bufferContents, jint renderFlags, jint offset, jint size,
     jbyteArray geometry)
{
    fprintf(stderr,
	    "JAVA 3D ERROR: call to obsolete CompressedGeometryRetained.execute method\n");


#ifdef OBSOLETE_HW_COMPRESSED_GEOM
    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 

    jbyte *cg = (*env)->GetPrimitiveArrayCritical(env, geometry, 0) ;

    /* ignore offset and pass header at 0 along with cg data */
    if(ctxProperties->geometry_compression_sunx)
	glDrawCompressedGeomSUNX((GLint)(size + 48), (GLubyte *)cg) ;
    
    (*env)->ReleasePrimitiveArrayCritical(env, geometry, cg, 0) ;
#endif /* OBSOLETE_HW_COMPRESSED_GEOM */
}


JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressByRef
    (JNIEnv *env, jobject obj, jlong ctxInfo)
{
    return JNI_FALSE;
}

JNIEXPORT
jboolean JNICALL Java_javax_media_j3d_CompressedGeometryRetained_decompressHW
    (JNIEnv *env, jobject obj, jlong ctxInfo, jint majorVersion, jint minorVersion)
{
    return JNI_FALSE;


#ifdef OBSOLETE_HW_COMPRESSED_GEOM
    /*
     * Return true if hardware decompression is supported for the given
     * version number of the compressed geometry.
     */

    GraphicsContextPropertiesInfo *ctxProperties = (GraphicsContextPropertiesInfo *)ctxInfo; 

    return
	(ctxProperties->geometry_compression_accelerated == 1) &&
	((majorVersion < ctxProperties->geometry_compression_accelerated_major_version) ||
	 ((majorVersion == ctxProperties->geometry_compression_accelerated_major_version) &&
	  (minorVersion <= ctxProperties->geometry_compression_accelerated_minor_version))) ;
#endif /* OBSOLETE_HW_COMPRESSED_GEOM */
}
