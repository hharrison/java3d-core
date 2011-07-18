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
import java.nio.DoubleBuffer;

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

public class DoubleBufferWrapper extends BufferWrapper {

    private DoubleBuffer buffer = null;

    /**
     * Constructor initializes buffer with a
     * java.nio.DoubleBuffer object.
     */
    public DoubleBufferWrapper(DoubleBuffer buffer) {
	this.buffer = buffer;
    }

    /**
     * Constructor initializes buffer with a
     * javax.media.j3d.J3DBuffer object.
     */
    public DoubleBufferWrapper(J3DBuffer b) {
        buffer = (DoubleBuffer)(b.getBuffer());
    }

    /**
     * Returns the java.nio.Buffer contained within this
     * DoubleBufferWrapper.
     */
    public java.nio.Buffer getBuffer() {
	return this.buffer;
    }

    // Wrapper for all relevant DoubleBuffer methods.

    /**
     * @return A boolean indicating whether the java.nio.Buffer
     * object contained within this DoubleBuffer is direct or
     * indirect.
     */
    public boolean isDirect() {
	return buffer.isDirect();
    }

    /**
     * Reads the double at this buffer's current position,
     * and then increments the position.
     */
    public double get() {
	return buffer.get();
    }

    /**
     * Reads the double at the given offset into the buffer.
     */
    public double get(int index) {
	return buffer.get(index);
    }

    /**
     * Bulk <i>get</i> method.  Transfers <code>dst.length</code>
     * doubles from
     * the buffer to the destination array and increments the
     * buffer's position by <code>dst.length</code>.
     */
    public DoubleBufferWrapper get(double[] dst) {
	buffer.get(dst);
	return this;
    }

    /**
     * Bulk <i>get</i> method.  Transfers <i>length</i> doubles
     * from the buffer starting at position <i>offset</i> into
     * the destination array.
     */
    public DoubleBufferWrapper get(double[] dst,  int offset, int length){
        buffer.get(dst, offset, length);
        return this;
    }

    /**
     * Bulk <i>put</i> method.  Transfers <code>src.length</code>
     * doubles into the buffer at the current position.
     */
    public DoubleBufferWrapper put(double[] src) {
        buffer.put(src);
        return this;
    }

    /**
     * Creates and returns a J3DBuffer object containing the
     * buffer in this DoubleBufferWrapper object.
     */
    public J3DBuffer getJ3DBuffer() {
        return new J3DBuffer( buffer );
    }

}
