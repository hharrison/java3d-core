/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.internal;

import javax.media.j3d.J3DBuffer;
import java.nio.Buffer;

/**
 * NIO Buffers are new in Java 1.4 but we need to run on 1.3
 * as well, so this class was created to hide the NIO classes
 * from non-1.4 Java 3D users.
 *
 * <p>
 * NOTE: We no longer need to support JDK 1.3 as of the Java 3D 1.3.2
 * community source release on java.net. We should be able to get rid
 * of this class.
 */

public abstract class BufferWrapper {

    /**
     * Value returned from getBufferType(), this indicates
     * that the BufferWrapper contains a null buffer.
     */
    public static final int TYPE_NULL = 0;

    /**
     * Value returned from getBufferType(), this indicates
     * that the BufferWrapper does not hold data of type
     * byte, float, or double.
     */
    public static final int TYPE_UNKNOWN = 1;

    /**
     * Value returned from getBufferType(), this indicates
     * that the BufferWrapper contains a java.nio.ByteBuffer.
     */
    public static final int TYPE_BYTE = 2;

    /**
     * Value returned from getBufferType(), this indicates
     * that the BufferWrapper contains a java.nio.FloatBuffer.
     */
    public static final int TYPE_FLOAT = 3;

    /**
     * Value returned from getBufferType(), this indicates
     * that the BufferWrapper contains a java.nio.DoubleBuffer.
     */
    public static final int TYPE_DOUBLE = 4;

    /**
     * Never used - this class is abstract.
     */
    public BufferWrapper() {
    }

    /**
     * Must be implemented by sublasses.
     */
    abstract Buffer getBuffer();

    /**
     * @return Buffer as object of type Object.
     */
    public Object getBufferAsObject() {
	return getBuffer();
    }

    // Wrapper for all relevant Buffer methods.

    /**
     * @return This buffer's capacity (set at initialization in
     * allocateDirect() ).
     * @see ByteBufferWrapper#allocateDirect
     */
    public int capacity() {
	return getBuffer().capacity();
    }

    /**
     * @return This buffer's limit.
     */
    public int limit() {
	return getBuffer().limit();
    }

    /**
     * @return This buffer's position.
     */
    public int position() {
	return getBuffer().position();
    }

    /**
     * Sets this buffer's position.
     * @return This buffer.
     */
    public BufferWrapper position(int newPosition){
	getBuffer().position(newPosition);
	return this;
    }

    /**
     * Resets this buffer's position to the previously marked
     * position.
     * @return This buffer.
     */
    public BufferWrapper rewind() {
	getBuffer().rewind();
	return this;
    }

    /**
     * @return An integer indicating the type of data held in
     * this buffer.
     * @see #TYPE_NULL
     * @see #TYPE_BYTE
     * @see #TYPE_FLOAT
     * @see #TYPE_DOUBLE
     * @see #TYPE_UNKNOWN
     */
    public static int getBufferType(J3DBuffer b) {
	int bufferType;
	Buffer buffer = b.getBuffer();

        if (buffer == null) {
            bufferType = TYPE_NULL;
        }
        else if (buffer instanceof java.nio.ByteBuffer) {
            bufferType = TYPE_BYTE;
        }
        else if (buffer instanceof java.nio.FloatBuffer) {
            bufferType = TYPE_FLOAT;
        }
        else if (buffer instanceof java.nio.DoubleBuffer) {
            bufferType = TYPE_DOUBLE;
        }
        else {
            bufferType = TYPE_UNKNOWN;
        }
	return bufferType;
    }
}
