/*
 * Copyright 2001-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package javax.media.j3d;

import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;

/**
 * Java 3D wrapper class for java.nio.Buffer objects.
 * When used to wrap a non-null NIO buffer object, this class will
 * create a read-only view of the wrapped NIO buffer, and will call
 * <code>rewind</code> on the read-only view, so that elements 0
 * through <code>buffer.limit()-1</code> will be available internally.
 *
 * @see GeometryArray#setCoordRefBuffer(J3DBuffer)
 * @see GeometryArray#setColorRefBuffer(J3DBuffer)
 * @see GeometryArray#setNormalRefBuffer(J3DBuffer)
 * @see GeometryArray#setTexCoordRefBuffer(int,J3DBuffer)
 * @see GeometryArray#setVertexAttrRefBuffer(int,J3DBuffer)
 * @see GeometryArray#setInterleavedVertexBuffer(J3DBuffer)
 * @see CompressedGeometry#CompressedGeometry(CompressedGeometryHeader,J3DBuffer)
 *
 * @since Java 3D 1.3
 */

public class J3DBuffer {

enum Type {
	NULL,
	UNKNOWN,
	BYTE,
	CHAR,
	SHORT,
	INT,
	LONG,
	FLOAT,
	DOUBLE,
}

    private java.nio.Buffer originalBuffer = null;
    private BufferWrapper bufferImpl = null;
    Type bufferType = Type.NULL;

    /**
     * Constructs a J3DBuffer object and initializes it with
     * a null NIO buffer object.  The NIO buffer object
     * must be set to a non-null value before using this J3DBuffer
     * object in a Java 3D node component.
     *
     * @exception UnsupportedOperationException if the JVM does not
     * support native access to direct NIO buffers
     */
    public J3DBuffer() {
	this(null);
    }


    /**
     * Constructs a J3DBuffer object and initializes it with
     * the specified NIO buffer object.
     *
     * @param buffer the NIO buffer wrapped by this J3DBuffer
     *
     * @exception UnsupportedOperationException if the JVM does not
     * support native access to direct NIO buffers
     *
     * @exception IllegalArgumentException if the specified buffer is
     * not a direct buffer, or if the byte order of the specified
     * buffer does not match the native byte order of the underlying
     * platform.
     */
    public J3DBuffer(java.nio.Buffer buffer) {
	setBuffer(buffer);
    }


    /**
     * Sets the NIO buffer object in this J3DBuffer to
     * the specified object.
     *
     * @param buffer the NIO buffer wrapped by this J3DBuffer
     *
     * @exception IllegalArgumentException if the specified buffer is
     * not a direct buffer, or if the byte order of the specified
     * buffer does not match the native byte order of the underlying
     * platform.
     */
    public void setBuffer(java.nio.Buffer buffer) {
	Type bType = Type.NULL;
	boolean direct = false;
	java.nio.ByteOrder order = java.nio.ByteOrder.BIG_ENDIAN;

	if (buffer == null) {
	    bType = Type.NULL;
	}
	else if (buffer instanceof java.nio.ByteBuffer) {
	    bType = Type.BYTE;
	    direct = ((java.nio.ByteBuffer)buffer).isDirect();
	    order = ((java.nio.ByteBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.CharBuffer) {
	    bType = Type.CHAR;
	    direct = ((java.nio.CharBuffer)buffer).isDirect();
	    order = ((java.nio.CharBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.ShortBuffer) {
	    bType = Type.SHORT;
	    direct = ((java.nio.ShortBuffer)buffer).isDirect();
	    order = ((java.nio.ShortBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.IntBuffer) {
	    bType = Type.INT;
	    direct = ((java.nio.IntBuffer)buffer).isDirect();
	    order = ((java.nio.IntBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.LongBuffer) {
	    bType = Type.LONG;
	    direct = ((java.nio.LongBuffer)buffer).isDirect();
	    order = ((java.nio.LongBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.FloatBuffer) {
	    bType = Type.FLOAT;
	    direct = ((java.nio.FloatBuffer)buffer).isDirect();
	    order = ((java.nio.FloatBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.DoubleBuffer) {
	    bType = Type.DOUBLE;
	    direct = ((java.nio.DoubleBuffer)buffer).isDirect();
	    order = ((java.nio.DoubleBuffer)buffer).order();
	}
	else {
	    bType = Type.UNKNOWN;
	}

	// Verify that the buffer is direct and has the correct byte order
	if (buffer != null) {
	    if (!direct) {
		throw new IllegalArgumentException(J3dI18N.getString("J3DBuffer1"));
	    }

	    if (order != java.nio.ByteOrder.nativeOrder()) {
		throw new IllegalArgumentException(J3dI18N.getString("J3DBuffer2"));
	    }
	}

	bufferType = bType;
	originalBuffer = buffer;

	// Make a read-only view of the buffer if the type is one
	// of the internally supported types: byte, float, or double
	switch (bufferType) {
	case BYTE:
	    java.nio.ByteBuffer byteBuffer =
		((java.nio.ByteBuffer)buffer).asReadOnlyBuffer();
	    byteBuffer.rewind();
	    bufferImpl = new ByteBufferWrapper(byteBuffer);
	    break;
	case FLOAT:
	    java.nio.FloatBuffer floatBuffer =
		((java.nio.FloatBuffer)buffer).asReadOnlyBuffer();
	    floatBuffer.rewind();
	    bufferImpl = new FloatBufferWrapper(floatBuffer);
	    break;
	case DOUBLE:
	    java.nio.DoubleBuffer doubleBuffer =
		((java.nio.DoubleBuffer)buffer).asReadOnlyBuffer();
	    doubleBuffer.rewind();
	    bufferImpl = new DoubleBufferWrapper(doubleBuffer);
	    break;
	default:
	    bufferImpl = null;
	}
    }


    /**
     * Retrieves the NIO buffer object from this J3DBuffer.
     *
     * @return the current NIO buffer wrapped by this J3DBuffer
     */
    public java.nio.Buffer getBuffer() {
	return originalBuffer;
    }

    BufferWrapper getBufferImpl() {
	return bufferImpl;
    }
}
