/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 * The NioImageBuffer class is a container for an image whose DataBuffer
 * is specified via a java.nio.Buffer. An an NioImageBuffer can be wrapped by
 * an ImageComponent and used for texture mapping, or for rendering Raster
 * objects or background images. An NioImageBuffer must not be used as the
 * buffer of an off-screen Canvas3D, or for reading back a raster image.
 *
 * @see ImageComponent2D
 * @see ImageComponent3D
 * 
 * @since Java 3D 1.5
 */
public class NioImageBuffer {

    /**
     * Used to specify the type of the image.
     */
    public enum ImageType {
        /**
         * Represents an image with 8-bit RGB color components,
         * corresponding to a Windows-style BGR color model, with the
         * colors Blue, Green, and Red stored in 3 consecutive
         * bytes for each pixel.
         * The data buffer must be a ByteBuffer when using this imageType.
         */
        TYPE_3BYTE_BGR,

        /**
         * Represents an image with 8-bit RGB color components with
         * Red, Green, and Blue, stored in 3 consecutive
         * bytes for each pixel.
         * The data buffer must be a ByteBuffer when using this imageType.
         */
        TYPE_3BYTE_RGB,

        /**
         * Represents an image with 8-bit RGBA color components with
         * Alpha, Blue, Green, and Red stored in 4 consecutive
         * bytes for each pixel.
         * The data buffer must be a ByteBuffer when using this imageType.
         */
        TYPE_4BYTE_ABGR,

        /**
         * Represents an image with 8-bit RGBA color components with
         * Red, Green, Blue, and Alpha stored in 4 consecutive
         * bytes for each pixel.
         * The data buffer must be a ByteBuffer when using this imageType.
         */
        TYPE_4BYTE_RGBA,

        /**
         * Represents a unsigned byte grayscale image, non-indexed.
         * The data buffer must be a ByteBuffer when using this imageType.
         */
        TYPE_BYTE_GRAY,

        /**
         * Represents an image with 8-bit RGBA color components packed
         * into integer pixels.
         * The data buffer must be an IntBuffer when using this imageType.
         */
        TYPE_INT_ARGB,

        /**
         * Represents an image with 8-bit RGB color components,
         * corresponding to a Windows- or Solaris- style BGR color model,
         * with the colors Blue, Green, and Red packed into integer
         * pixels.
         * The data buffer must be an IntBuffer when using this imageType.
         */
        TYPE_INT_BGR,

        /**
         * Represents an image with 8-bit RGB color components packed into
         * integer pixels.
         * The data buffer must be an IntBuffer when using this imageType.
         */
        TYPE_INT_RGB,

    }


    /**
     * Enum for type of buffer
     */
    enum BufferType {
        BYTE_BUFFER,
        INT_BUFFER,
    }


    // Width and height of image
    int width;
    int height;

    // TYpe of image
    ImageType imageType;

    // Cached buffer
    Buffer buffer;

    // Type of NIO Buffer: byte or int
    BufferType bufferType;

    // Number of bytes allocated per pixel
    int bytesPerPixel;

    // Number of byte or int elements per pixel
    int elementsPerPixel;

    /**
     * Constructs an NIO image buffer of the specified size and type.
     * A direct NIO buffer of the correct type (ByteBuffer or IntBuffer)
     * and size to match the input parameters
     * is allocated.
     *
     * @param width width of the image
     * @param height height of the image
     * @param imageType type of the image.
     *
     * @exception IllegalArgumentException if width < 1 or height < 1
     * @exception NullPointerException if imageType is null
     */
    public NioImageBuffer(int width, int height, ImageType imageType) {

        processParams(width, height, imageType);

        ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(width * height * bytesPerPixel);
        switch (bufferType) {
            case BYTE_BUFFER:
                buffer = tmpBuffer;
                break;

            case INT_BUFFER:
                buffer = tmpBuffer.order(ByteOrder.nativeOrder()).asIntBuffer();
                break;

            default:
                // We should never get here
                throw new AssertionError("missing case statement");
        }
    }

    /**
     * Constructs an NIO image buffer of the specified size and type, using
     * the specified dataBuffer.
     * The the byte order of the specified dataBuffer must match the native
     * byte order of the underlying platform.
     * For best performance, the NIO buffer should be a direct buffer.
     *
     * @param width width of the image
     * @param height height of the image
     * @param imageType type of the image.
     * @param dataBuffer an NIO buffer of the correct type (ByteBuffer or
     * IntBuffer) to match the specified imageType.
     * This constructor will create a new view of
     * the buffer, and will call <code>rewind</code> on that view,
     * such that elements 0 through <code>dataBuffer.limit()-1</code>
     * will be available internally. The number of elements in
     * the buffer must be exactly <code>width*height*numElementsPerPixel</code>,
     * where <code>numElementsPerPixel</code> is
     * 3 for TYPE_3BYTE_BGR and TYPE_3BYTE_RGB,
     * 4 for TYPE_4BYTE_ABGR and TYPE_4BYTE_RGBA,
     * and 1 for all other types.
     *
     * @exception IllegalArgumentException if width < 1 or height < 1
     * @exception NullPointerException if imageType or dataBuffer is null
     * @exception IllegalArgumentException if the type of the dataBuffer does
     * not match the imageType
     * @exception IllegalArgumentException if <code>dataBuffer.limit() !=
     * width*height*numElementsPerPixel</code>
     * @exception IllegalArgumentException if the byte order of the specified
     * dataBuffer does not match the native byte order of the underlying
     * platform.
     */
    public NioImageBuffer(int width, int height, ImageType imageType,
            Buffer dataBuffer) {

        processParams(width, height, imageType);
        setDataBuffer(dataBuffer);
    }

    /**
     * Gets the width of this data buffer.
     *
     * @return the width of this data buffer.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of this data buffer.
     *
     * @return the width of this data buffer.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the image type of this data buffer.
     *
     * @return the image type of this data buffer.
     */
    public ImageType getImageType() {
        return imageType;
    }

    /**
     * Sets the data buffer to the specified input data buffer.
     * The the byte order of the specified dataBuffer must match the native
     * byte order of the underlying platform.
     * For best performance, the NIO buffer should be a direct buffer.
     * 
     * @param dataBuffer an NIO buffer of the correct type (ByteBuffer or
     * IntBuffer) to match the imageType of this
     * NioImageBuffer. This method will create a new view of
     * the buffer, and will call <code>rewind</code> on that view,
     * such that elements 0 through <code>dataBuffer.limit()-1</code>
     * will be available internally. The number of elements in
     * the buffer must be exactly <code>width*height*numElementsPerPixel</code>,
     * where <code>numElementsPerPixel</code> is
     * 3 for TYPE_3BYTE_BGR and TYPE_3BYTE_RGB,
     * 4 for TYPE_4BYTE_ABGR and TYPE_4BYTE_RGBA,
     * and 1 for all other types.
     *
     * @exception NullPointerException if dataBuffer is null
     * @exception IllegalArgumentException if the type of the dataBuffer does
     * not match the imageType
     * @exception IllegalArgumentException if <code>dataBuffer.limit() !=
     * width*height*numElementsPerPixel</code>
     * @exception IllegalArgumentException if the byte order of the specified
     * dataBuffer does not match the native byte order of the underlying
     * platform.
     */
    public void setDataBuffer(Buffer dataBuffer) {
        if (dataBuffer == null) {
            throw new NullPointerException();
        }

        if (dataBuffer.limit() != width*height*elementsPerPixel) {
            throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer3"));
        }

        switch (bufferType) {
            case BYTE_BUFFER:
                if (!(dataBuffer instanceof ByteBuffer)) {
                    throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer4"));
                }
                buffer = ((ByteBuffer)dataBuffer).duplicate().rewind();
                break;

            case INT_BUFFER:
                if (!(dataBuffer instanceof IntBuffer)) {
                    throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer4"));
                }

                if (((IntBuffer)dataBuffer).order() != ByteOrder.nativeOrder()) {
                    throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer5"));
                }
                buffer = ((IntBuffer)dataBuffer).duplicate().rewind();
                break;

            default:
                // We should never get here
                throw new AssertionError("missing case statement");
        }
    }

    /**
     * Gets the data buffer to the specified input data buffer.
     *
     * @return a view of the current data buffer for this NIO image buffer.
     * This view will be rewound such that elements 0
     * through <code>dataBuffer.limit()-1</code> are available.
     */
    public Buffer getDataBuffer() {
        Buffer tmpBuffer = null;

        switch (bufferType) {
            case BYTE_BUFFER:
                tmpBuffer = ((ByteBuffer)buffer).duplicate();
                break;

            case INT_BUFFER:
                tmpBuffer = ((IntBuffer)buffer).duplicate();
                break;

            default:
                // We should never get here
                throw new AssertionError("missing case statement");
        }

        return tmpBuffer.rewind();
    }


    // Sanity check the input parameters, calculate the buffer type and
    // the number of bytes per pixel
    private void processParams(int width, int height, ImageType imageType) {
        if (width < 1) {
            throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer0"));
        }

        if (height < 1) {
            throw new IllegalArgumentException(J3dI18N.getString("NioImageBuffer1"));
        }

        switch (imageType) {
            case TYPE_3BYTE_BGR:
                bufferType = BufferType.BYTE_BUFFER;
                bytesPerPixel = 3;
                elementsPerPixel = 3;
                break;

            case TYPE_3BYTE_RGB:
                bufferType = BufferType.BYTE_BUFFER;
                bytesPerPixel = 3;
                elementsPerPixel = 3;
                break;

            case TYPE_4BYTE_ABGR:
                bufferType = BufferType.BYTE_BUFFER;
                bytesPerPixel = 4;
                elementsPerPixel = 4;
                break;

            case TYPE_4BYTE_RGBA:
                bufferType = BufferType.BYTE_BUFFER;
                bytesPerPixel = 4;
                elementsPerPixel = 4;
                break;

            case TYPE_BYTE_GRAY:
                bufferType = BufferType.BYTE_BUFFER;
                bytesPerPixel = 1;
                elementsPerPixel = 1;
                break;

            case TYPE_INT_ARGB:
            case TYPE_INT_BGR:
            case TYPE_INT_RGB:
                bufferType = BufferType.INT_BUFFER;
                bytesPerPixel = 4;
                elementsPerPixel = 1;
                break;

            default:
                // We should never get here
                throw new AssertionError("missing case statement");
        }

        this.width = width;
        this.height = height;
        this.imageType = imageType;
    }

}
