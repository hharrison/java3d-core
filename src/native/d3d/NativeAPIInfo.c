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

#include <jni.h>

#include "javax_media_j3d_MasterControl.h"
#include "javax_media_j3d_NativeAPIInfo.h"

JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeAPIInfo_getRenderingAPI(
    JNIEnv *env, jobject obj)
{
    return (jint)javax_media_j3d_MasterControl_RENDER_DIRECT3D;
}
