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

package javax.media.j3d;

import com.sun.j3d.internal.ByteBufferWrapper;
import com.sun.j3d.internal.BufferWrapper;
import com.sun.j3d.internal.FloatBufferWrapper;
import com.sun.j3d.internal.DoubleBufferWrapper;
import com.sun.j3d.internal.ByteOrderWrapper;

/**
 * Java 3D wrapper class for java.nio.Buffer objects.
 * When used to wrap a non-null NIO buffer object, this class will
 * create a read-only view of the wrapped NIO buffer, and will call
 * <code>rewind</code> on the read-only view, so that elements 0
 * through <code>buffer.limit()-1</code> will be available internally.
 *
 * <p>
 * NOTE: Use of this class requires version 1.4 of the
 * Java<sup><font size="-2">TM</font></sup>&nbsp;2 Platform.
 * Any attempt to access this class on an earlier version of the
 * Java&nbsp;2 Platform will fail with a NoClassDefFoundError.
 * Refer to the documentation for version 1.4 of the Java&nbsp;2 SDK
 * for a description of the
 * <code>
 * <a href="http://java.sun.com/j2se/1.4/docs/api/java/nio/package-summary.html">java.nio</a>
 * </code> package.
 *
 * @see GeometryArray#setCoordRefBuffer(J3DBuffer)
 * @see GeometryArray#setColorRefBuffer(J3DBuffer)
 * @see GeometryArray#setNormalRefBuffer(J3DBuffer)
 * @see GeometryArray#setTexCoordRefBuffer(int,J3DBuffer)
 * @see GeometryArray#setInterleavedVertexBuffer(J3DBuffer)
 * @see CompressedGeometry#CompressedGeometry(CompressedGeometryHeader,J3DBuffer)
 *
 * @since Java 3D 1.3
 */

public class J3DBuffer {
    static final int TYPE_NULL = 0;
    static final int TYPE_UNKNOWN = 1;
    static final int TYPE_BYTE = 2;
    static final int TYPE_CHAR = 3;
    static final int TYPE_SHORT = 4;
    static final int TYPE_INT = 5;
    static final int TYPE_LONG = 6;
    static final int TYPE_FLOAT = 7;
    static final int TYPE_DOUBLE = 8;

    static boolean unsupportedOperation = false;

    private java.nio.Buffer originalBuffer = null;
    private BufferWrapper bufferImpl = null;
    private int bufferType = TYPE_NULL;

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
	if (unsupportedOperation)
	    throw new UnsupportedOperationException(J3dI18N.getString("J3DBuffer0"));

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
	int bType = TYPE_NULL;
	boolean direct = false;
	java.nio.ByteOrder order = java.nio.ByteOrder.BIG_ENDIAN;

	if (buffer == null) {
	    bType = TYPE_NULL;
	}
	else if (buffer instanceof java.nio.ByteBuffer) {
	    bType = TYPE_BYTE;
	    direct = ((java.nio.ByteBuffer)buffer).isDirect();
	    order = ((java.nio.ByteBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.CharBuffer) {
	    bType = TYPE_CHAR;
	    direct = ((java.nio.CharBuffer)buffer).isDirect();
	    order = ((java.nio.CharBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.ShortBuffer) {
	    bType = TYPE_SHORT;
	    direct = ((java.nio.ShortBuffer)buffer).isDirect();
	    order = ((java.nio.ShortBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.IntBuffer) {
	    bType = TYPE_INT;
	    direct = ((java.nio.IntBuffer)buffer).isDirect();
	    order = ((java.nio.IntBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.LongBuffer) {
	    bType = TYPE_LONG;
	    direct = ((java.nio.LongBuffer)buffer).isDirect();
	    order = ((java.nio.LongBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.FloatBuffer) {
	    bType = TYPE_FLOAT;
	    direct = ((java.nio.FloatBuffer)buffer).isDirect();
	    order = ((java.nio.FloatBuffer)buffer).order();
	}
	else if (buffer instanceof java.nio.DoubleBuffer) {
	    bType = TYPE_DOUBLE;
	    direct = ((java.nio.DoubleBuffer)buffer).isDirect();
	    order = ((java.nio.DoubleBuffer)buffer).order();
	}
	else {
	    bType = TYPE_UNKNOWN;
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
	case TYPE_BYTE:
	    java.nio.ByteBuffer byteBuffer =
		((java.nio.ByteBuffer)buffer).asReadOnlyBuffer();
	    byteBuffer.rewind();
	    bufferImpl = new ByteBufferWrapper(byteBuffer);
	    break;
	case TYPE_FLOAT:
	    java.nio.FloatBuffer floatBuffer =
		((java.nio.FloatBuffer)buffer).asReadOnlyBuffer();
	    floatBuffer.rewind();
	    bufferImpl = new FloatBufferWrapper(floatBuffer);
	    break;
	case TYPE_DOUBLE:
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


    int getBufferType() {
	return bufferType;
    }


    BufferWrapper getBufferImpl() {
	return bufferImpl;
    }

    // Native method to verify that we can access a direct NIO buffer
    // from native code
    private native static long getNativeAddress(java.nio.Buffer buffer);

    private static boolean checkNativeBufferAccess(java.nio.Buffer buffer) {
	// TODO: uncomment out the call to getNativeAddress and implement it
	if (buffer == null /*|| getNativeAddress(buffer) == 0L*/) {
	    return false;
	}
	else {
	    return true;
	}
    }

    static {
	// Allocate a direct byte buffer and verify that we can
	// access the data pointer from native code
	java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(8);

	if (!checkNativeBufferAccess(buffer)) {
	    unsupportedOperation = true;
	}
    }
}
