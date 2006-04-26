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

package javax.media.j3d;

/**
 * Graphics context objects for native rendering pipeline.
 */
class NativeContext implements Context {

    // Native context pointer
    private long nativeCtx;

    NativeContext(long nativeCtx) {
        this.nativeCtx = nativeCtx;
    }

    long getNativeCtx() {
        return nativeCtx;
    }

}
