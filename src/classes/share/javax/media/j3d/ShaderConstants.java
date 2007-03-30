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
 * The ShaderConstants class contains internal constants used by other
 * Shader classes.
 */
class ShaderConstants extends Object {

    //
    // The following bits are used in the messages for various Shader objects.
    //

    // ShaderAppearance bits
    static final int SHADER_PROGRAM         = 0x0001;
    static final int SHADER_ATTRIBUTE_SET   = 0x0002;    

    // ShaderAttributeSet bits -- indicates which attribute
    // operation in this ShaderAttributeSet object is needed.
    static final int ATTRIBUTE_SET_PUT       = 0x0004;
    static final int ATTRIBUTE_SET_REMOVE    = 0x0008;
    static final int ATTRIBUTE_SET_CLEAR     = 0x0010;

    // ShaderAttribute bits
    static final int ATTRIBUTE_VALUE_UPDATE  = 0x0020;

}
