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

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

#include <jni.h>

#include "javax_media_j3d_MasterControl.h"
#include "javax_media_j3d_NativeAPIInfo.h"

JNIEXPORT
jint JNICALL Java_javax_media_j3d_NativeAPIInfo_getRenderingAPI(
    JNIEnv *env, jobject obj)
{
#ifdef WIN32
    return (jint)javax_media_j3d_MasterControl_RENDER_OPENGL_WIN32;
#endif /* WIN32 */

#ifdef SOLARIS
    return (jint)javax_media_j3d_MasterControl_RENDER_OPENGL_SOLARIS;
#endif /* SOLARIS */

#ifdef LINUX
    return (jint)javax_media_j3d_MasterControl_RENDER_OPENGL_LINUX;
#endif /* LINUX */
}
