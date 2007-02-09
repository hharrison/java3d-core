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

package javax.media.j3d;

/**
 * Shader objects for native rendering pipeline.
 */
class NativeShaderObject implements ShaderProgramId, ShaderId, ShaderAttrLoc {

    // Native shader object;
    private long nativeId;

    NativeShaderObject(long nativeId) {
        this.nativeId = nativeId;
    }

    long getNativeId() {
        return nativeId;
    }

}
