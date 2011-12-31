/*
 * $RCSfile$
 *
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

/**
 * This class defines a 3D image component.  This is used for texture
 * images.
 * Prior to Java 3D 1.2, only BufferedImage objects could be used as
 * the input to an ImageComponent3D object.  As of Java 3D 1.2, an
 * ImageComponent3D accepts an array of arbitrary RenderedImage
 * objects (BufferedImage is an implementation of the RenderedImage
 * interface).  The methods that set/get a BufferedImage object are
 * left in for compatibility.  The new methods that set/get a
 * RenderedImage are a superset of the old methods.  In particular,
 * the two set methods in the following example are equivalent:
 *
 * <p>
 * <ul>
 * <code>
 * BufferedImage bi;<br>
 * RenderedImage ri = bi;<br>
 * ImageComponent3D ic;<br>
 * <p>
 * // Set image 0 to the specified BufferedImage<br>
 * ic.set(0, bi);<br>
 * <p>
 * // Set image 0 to the specified RenderedImage<br>
 * ic.set(0, ri);<br>
 * </code>
 * </ul>
 *
 */
public class ImageComponent3D extends ImageComponent {

    // non-public, no parameter constructor
    ImageComponent3D() {}

    /**
     * Constructs a 3D image component object using the specified
     * format, width, height, and depth.  Default values are used for
     * all other parameters.  The default values are as follows:
     * <ul>
     * array of images : null<br>
     * imageClass : ImageClass.BUFFERED_IMAGE<br>
     * </ul>
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param width the number of columns of pixels in this image component
     * object
     * @param height the number of rows of pixels in this image component
     * object
     * @param depth the number of 2D slices in this image component object
     * @exception IllegalArgumentException if format is invalid, or if
     * any of width, height, or depth are not positive.
     */
    public ImageComponent3D(int		format,
			    int		width,
			    int		height,
			    int		depth) {

        ((ImageComponent3DRetained)this.retained).processParams(format, width, height, depth);
    }

    /**
     * Constructs a 3D image component object using the specified format,
     * and the BufferedImage array.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA etc.
     * @param images an array of BufferedImage objects.  The
     * first image in the array determines the width and height of this
     * ImageComponent3D.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the first image are not positive.
     */
    public ImageComponent3D(int format, BufferedImage[] images) {
        ((ImageComponent3DRetained)this.retained).processParams(format,
		images[0].getWidth(null), images[0].getHeight(null), images.length);
        for (int i=0; i<images.length; i++) {
            ((ImageComponent3DRetained)this.retained).set(i, images[i]);
        }
    }

    /**
     * Constructs a 3D image component object using the specified format,
     * and the RenderedImage array.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA etc.
     * @param images an array of RenderedImage objects.  The
     * first image in the array determines the width and height of this
     * ImageComponent3D.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the first image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent3D(int format, RenderedImage[] images) {

        ((ImageComponent3DRetained)this.retained).processParams(format,
	images[0].getWidth(), images[0].getHeight(), images.length);
        for (int i=0; i<images.length; i++) {
            ((ImageComponent3DRetained)this.retained).set(i, images[i]);
        }
    }

    /**
     * Constructs a 3D image component object using the specified
     * format, width, height, depth, byReference flag, and yUp flag.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param width the number of columns of pixels in this image component
     * object
     * @param height the number of rows of pixels in this image component
     * object
     * @param depth the number of 2D slices in this image component object
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * any of width, height, or depth are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent3D(int		format,
			    int		width,
			    int		height,
			    int		depth,
			    boolean	byReference,
			    boolean	yUp) {

 	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent3DRetained)this.retained).processParams(format, width, height, depth);
    }

    /**
     * Constructs a 3D image component object using the specified format,
     * BufferedImage array, byReference flag, and yUp flag.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA etc.
     * @param images an array of BufferedImage objects.  The
     * first image in the array determines the width and height of this
     * ImageComponent3D.
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the first image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent3D(int format,
			    BufferedImage[] images,
			    boolean byReference,
			    boolean yUp) {


 	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent3DRetained)this.retained).processParams(format,
                images[0].getWidth(null), images[0].getHeight(null), images.length);
 	for (int i=0; i<images.length; i++) {
 	    ((ImageComponent3DRetained)this.retained).set(i, images[i]);
 	}
    }

    /**
     * Constructs a 3D image component object using the specified format,
     * RenderedImage array, byReference flag, and yUp flag.
     * The image class is set to ImageClass.RENDERED_IMAGE if the byReference
     * flag is true and any of the specified RenderedImages is <i>not</i> an
     * instance of BufferedImage. In all other cases, the image class is set to
     * ImageClass.BUFFERED_IMAGE.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA etc.
     * @param images an array of RenderedImage objects.  The
     * first image in the array determines the width and height of this
     * ImageComponent3D.
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the first image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent3D(int format,
			    RenderedImage[] images,
			    boolean byReference,
			    boolean yUp) {

 	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent3DRetained)this.retained).processParams(format,
                images[0].getWidth(), images[0].getHeight(), images.length);
 	for (int i=0; i<images.length; i++) {
 	    ((ImageComponent3DRetained)this.retained).set(i, images[i]);
 	}
    }

    /**
     * Constructs a 3D image component object using the specified format,
     * NioImageBuffer array, byReference flag, and yUp flag.
     * The image class is set to ImageClass.NIO_IMAGE_BUFFER.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA etc.
     * @param images an array of NioImageBuffer objects.  The
     * first image in the array determines the width and height of this
     * ImageComponent3D.
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the first image are not positive.
     *
     * @exception IllegalArgumentException if the byReference flag is false.
     *
     * @exception IllegalArgumentException if the yUp flag is false.
     *
     * @exception UnsupportedOperationException this method is not supported
     * for Java 3D 1.5.
     *
     * @since Java 3D 1.5
     */
    public ImageComponent3D(int format,
			    NioImageBuffer[] images,
			    boolean byReference,
			    boolean yUp) {


 	throw new UnsupportedOperationException();
        /*
  	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent3DRetained)this.retained).processParams(format,
                images[0].getWidth(), images[0].getHeight(), images.length);
 	for (int i=0; i<images.length; i++) {
 	    ((ImageComponent3DRetained)this.retained).set(i, images[i]);
 	}
         */
    }

    /**
     * Retrieves the depth of this 3D image component object.
     *
     * @return the depth of this 3D image component object
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getDepth() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_SIZE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent3D0"));
        return ((ImageComponent3DRetained)this.retained).getDepth();
    }

    /**
     * Sets the array of images in this image component to the
     * specified array of BufferedImage objects.  If the data access
     * mode is not by-reference, then the BufferedImage data is copied
     * into this object.  If the data access mode is by-reference,
     * then a shallow copy of the array of references to the
     * BufferedImage objects is made, but the BufferedImage
     * data is not necessarily copied.
     * <p>
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param images array of BufferedImage objects containing the image.
     * The size (width and height) of each image must be the same as the
     * size of the image component, and the length of the images array
     * must equal the depth of the image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the length of the images array is
     * not equal to the depth of this ImageComponent object.
     *
     * @exception IllegalArgumentException if the width and height of each
     * image in the images array is not equal to the width and height of this
     * ImageComponent object.
     */
    public void set(BufferedImage[] images) {
        checkForLiveOrCompiled();
	int depth = ((ImageComponent3DRetained)this.retained).getDepth();

        if (depth != images.length)
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D1"));
        for (int i=0; i<depth; i++) {
            ((ImageComponent3DRetained)this.retained).set(i, images[i]);
        }
    }

    /**
     * Sets the array of images in this image component to the
     * specified array of RenderedImage objects.  If the data access
     * mode is not by-reference, then the RenderedImage data is copied
     * into this object.  If the data access mode is by-reference,
     * then a shallow copy of the array of references to the
     * RenderedImage objects is made, but the RenderedImage
     * data is not necessarily copied.
     * <p>
     * The image class is set to ImageClass.RENDERED_IMAGE if the data access
     * mode is by-reference and any of the specified RenderedImages
     * is <i>not</i> an instance of BufferedImage. In all other cases,
     * the image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param images array of RenderedImage objects containing the image.
     * The size (width and height) of each image must be the same as the
     * size of the image component, and the length of the images array
     * must equal the depth of the image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the length of the images array is
     * not equal to the depth of this ImageComponent object.
     *
     * @exception IllegalArgumentException if the width and height of each
     * image in the images array is not equal to the width and height of this
     * ImageComponent object.
     *
     * @since Java 3D 1.2
     */
    public void set(RenderedImage[] images) {

        checkForLiveOrCompiled();
	int depth = ((ImageComponent3DRetained)this.retained).getDepth();

        if (depth != images.length)
	    throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D1"));
        for (int i=0; i<depth; i++) {
            ((ImageComponent3DRetained)this.retained).set(i, images[i]);
        }
    }

    /**
     * Sets the array of images in this image component to the
     * specified array of NioImageBuffer objects.  If the data access
     * mode is not by-reference, then the NioImageBuffer data is copied
     * into this object.  If the data access mode is by-reference,
     * then a shallow copy of the array of references to the
     * NioImageBuffer objects is made, but the NioImageBuffer
     * data is not necessarily copied.
     * <p>
     * The image class is set to ImageClass.NIO_IMAGE_BUFFER.
     *
     * @param images array of NioImageBuffer objects containing the image.
     * The size (width and height) of each image must be the same as the
     * size of the image component, and the length of the images array
     * must equal the depth of the image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if this ImageComponent object
     * is <i>not</i> yUp.
     *
     * @exception IllegalArgumentException if the length of the images array is
     * not equal to the depth of this ImageComponent object.
     *
     * @exception IllegalArgumentException if the width and height of each
     * image in the images array is not equal to the width and height of this
     * ImageComponent object.
     *
     * @exception UnsupportedOperationException this method is not supported
     * for Java 3D 1.5.
     *
     * @since Java 3D 1.5
     */
    public void set(NioImageBuffer[] images) {

 	throw new UnsupportedOperationException();
        /*
        checkForLiveOrCompiled();
	int depth = ((ImageComponent3DRetained)this.retained).getDepth();

        if (depth != images.length)
	    throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D1"));
        for (int i=0; i<depth; i++) {
            ((ImageComponent3DRetained)this.retained).set(i, images[i]);
        }
         */
    }

    /**
     * Sets this image component at the specified index to the
     * specified BufferedImage object.  If the data access mode is not
     * by-reference, then the BufferedImage data is copied into this
     * object.  If the data access mode is by-reference, then a
     * reference to the BufferedImage is saved, but the data is not
     * necessarily copied.
     *
     * @param index the image index.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @param image BufferedImage object containing the image.
     * The size (width and height) must be the same as the current size of this
     * ImageComponent3D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.BUFFERED_IMAGE.
     *
     * @exception IllegalArgumentException if the width and height the image
     * is not equal to the width and height of this ImageComponent object.
     */
    public void set(int index, BufferedImage image) {
        checkForLiveOrCompiled();
        if (image.getWidth(null) != this.getWidth())
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D2"));

	if (image.getHeight(null) != this.getHeight())
            throw new IllegalArgumentException(J3dI18N.getString("ImageComponent3D4"));

	((ImageComponent3DRetained)this.retained).set(index, image);
    }

    /**
     * Sets this image component at the specified index to the
     * specified RenderedImage object.  If the data access mode is not
     * by-reference, then the RenderedImage data is copied into this
     * object.  If the data access mode is by-reference, then a
     * reference to the RenderedImage is saved, but the data is not
     * necessarily copied.
     *
     * @param index the image index.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @param image RenderedImage object containing the image.
     * The size (width and height) must be the same as the current size of this
     * ImageComponent3D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not one of:
     * ImageClass.BUFFERED_IMAGE or ImageClass.RENDERED_IMAGE.
     *
     * @exception IllegalArgumentException if the width and height the image
     * is not equal to the width and height of this ImageComponent object.
     *
     * @since Java 3D 1.2
     */
    public void set(int index, RenderedImage image) {

        checkForLiveOrCompiled();
        // For RenderedImage the width and height checking is done in the retained.
	((ImageComponent3DRetained)this.retained).set(index, image);
    }

    /**
     * Sets this image component at the specified index to the
     * specified NioImageBuffer object.  If the data access mode is not
     * by-reference, then the NioImageBuffer data is copied into this
     * object.  If the data access mode is by-reference, then a
     * reference to the NioImageBuffer is saved, but the data is not
     * necessarily copied.
     *
     * @param index the image index.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @param image NioImageBuffer object containing the image.
     * The size (width and height) must be the same as the current size of this
     * ImageComponent3D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.NIO_IMAGE_BUFFER.
     *
     * @exception IllegalArgumentException if the width and height the image
     * is not equal to the width and height of this ImageComponent object.
     *
     * @exception UnsupportedOperationException this method is not supported
     * for Java 3D 1.5.
     *
     * @since Java 3D 1.5
     */
    public void set(int index, NioImageBuffer image) {

 	throw new UnsupportedOperationException();
        /*
         checkForLiveOrCompiled();
        // For NioImageBuffer the width and height checking is done in the retained.
        ((ImageComponent3DRetained)this.retained).set(index, image);
         */
    }

    /**
     * Retrieves the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the images
     * is made.  If the data access mode is by-reference, then the
     * references are returned.
     *
     * @return either a new array of new BufferedImage objects created from
     * the data
     * in this image component, or a new array of
     * references to the BufferedImages that this image component refers to.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.BUFFERED_IMAGE.
     */
    public BufferedImage[] getImage() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent3D3"));
	return ((ImageComponent3DRetained)this.retained).getImage();
    }

    /**
     * Retrieves the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the images
     * is made.  If the data access mode is by-reference, then the
     * references are returned.
     *
     * @return either a new array of new RenderedImage objects created from
     * the data
     * in this image component, or a new array of
     * references to the RenderedImages that this image component refers to.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not one of:
     * ImageClass.BUFFERED_IMAGE or ImageClass.RENDERED_IMAGE.
     *
     * @since Java 3D 1.2
     */
    public RenderedImage[] getRenderedImage() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent3D3"));
	return ((ImageComponent3DRetained)this.retained).getRenderedImage();
    }

    /**
     * Retrieves the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the images
     * is made.  If the data access mode is by-reference, then the
     * references are returned.
     *
     * @return either a new array of new RenderedImage objects created from
     * the data
     * in this image component, or a new array of
     * references to the RenderedImages that this image component refers to.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.NIO_IMAGE_BUFFER.
     *
     * @exception UnsupportedOperationException this method is not supported
     * for Java 3D 1.5.
     *
     * @since Java 3D 1.5
     */
    public NioImageBuffer[] getNioImage() {

 	throw new UnsupportedOperationException();
    }

    /**
     * Retrieves one of the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the image
     * is made.  If the data access mode is by-reference, then the
     * reference is returned.
     *
     * @param index the index of the image to retrieve.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @return either a new BufferedImage object created from the data
     * in this image component, or the BufferedImage object referenced
     * by this image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.BUFFERED_IMAGE.
     */
    public BufferedImage getImage(int index) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent3D3"));

	RenderedImage img = ((ImageComponent3DRetained)this.retained).getImage(index);
	if ((img != null) && !(img instanceof BufferedImage)) {
	    throw new IllegalStateException(J3dI18N.getString("ImageComponent3D9"));
	}
	return (BufferedImage) img;
    }

    /**
     * Retrieves one of the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the image
     * is made.  If the data access mode is by-reference, then the
     * reference is returned.
     *
     * @param index the index of the image to retrieve.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @return either a new RenderedImage object created from the data
     * in this image component, or the RenderedImage object referenced
     * by this image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not one of:
     * ImageClass.BUFFERED_IMAGE or ImageClass.RENDERED_IMAGE.
     *
     * @since Java 3D 1.2
     */
    public RenderedImage getRenderedImage(int index) {

        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
             throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent3D3"));
	return ((ImageComponent3DRetained)this.retained).getImage(index);
    }

    /**
     * Retrieves one of the images from this ImageComponent3D object.  If the
     * data access mode is not by-reference, then a copy of the image
     * is made.  If the data access mode is by-reference, then the
     * reference is returned.
     *
     * @param index the index of the image to retrieve.
     * The index must be less than the depth of this ImageComponent3D object.
     *
     * @return either a new NioImageBuffer object created from the data
     * in this image component, or the NioImageBuffer object referenced
     * by this image component.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the image class is not
     * ImageClass.NIO_IMAGE_BUFFER.
     *
     * @exception UnsupportedOperationException this method is not supported
     * for Java 3D 1.5.
     *
     * @since Java 3D 1.5
     */
    public NioImageBuffer getNioImage(int index) {

 	throw new UnsupportedOperationException();
    }

    /**
     * Modifies a contiguous subregion of a particular slice of
     * image of this ImageComponent3D object.
     * Block of data of dimension (width * height)
     * starting at the offset (srcX, srcY) of the specified
     * RenderedImage object will be copied into the particular slice of
     * image component
     * starting at the offset (dstX, dstY) of this ImageComponent3D object.
     * The specified RenderedImage object must be of the same format as
     * the current format of this object.
     * This method can only be used if the data access mode is
     * by-copy. If it is by-reference, see updateData().
     *
     * @param index index of the image to be modified.
     * The index must be less than the depth of this ImageComponent3D object.
     * @param image RenderedImage object containing the subimage.
     * @param width width of the subregion.
     * @param height height of the subregion.
     * @param srcX starting X offset of the subregion in the specified image.
     * @param srcY starting Y offset of the subregion in the specified image.
     * @param dstX startin X offset of the subregion in the image
     * component of this object.
     * @param dstY starting Y offset of the subregion in the image
     * component of this object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the data access mode is
     * <code>BY_REFERENCE</code>.
     *
     * @exception IllegalArgumentException if <code>width</code> or
     * <code>height</code> of
     * the subregion exceeds the dimension of the image in this object.
     *
     * @exception IllegalArgumentException if <code>dstX</code> < 0, or
     * (<code>dstX</code> + <code>width</code>) > width of this object, or
     * <code>dstY</code> < 0, or
     * (<code>dstY</code> + <code>height</code>) > height of this object.
     *
     * @exception IllegalArgumentException if <code>srcX</code> < 0, or
     * (<code>srcX</code> + <code>width</code>) > width of the RenderedImage
     * object containing the subimage, or
     * <code>srcY</code> < 0, or
     * (<code>srcY</code> + <code>height</code>) > height of the
     * RenderedImage object containing the subimage.
     *
     * @exception IllegalArgumentException if the specified RenderedImage
     * is not compatible with the existing RenderedImage.
     *
     * @exception IllegalStateException if the image class is not one of:
     * ImageClass.BUFFERED_IMAGE or ImageClass.RENDERED_IMAGE.
     *
     * @since Java 3D 1.3
     */
    public void setSubImage(int index, RenderedImage image,
				int width, int height,
                                int srcX, int srcY, int dstX, int dstY) {
        if (isLiveOrCompiled() &&
                !this.getCapability(ALLOW_IMAGE_WRITE)) {
            throw new CapabilityNotSetException(
                                J3dI18N.getString("ImageComponent3D5"));
        }

        if (((ImageComponent3DRetained)this.retained).isByReference()) {
            throw new IllegalStateException(
                                J3dI18N.getString("ImageComponent3D8"));
        }

        int w = ((ImageComponent3DRetained)this.retained).getWidth();
        int h = ((ImageComponent3DRetained)this.retained).getHeight();

        if ((srcX < 0) || (srcY < 0) ||
                ((srcX + width) > w) || ((srcY + height) > h) ||
            (dstX < 0) || (dstY < 0) ||
                ((dstX + width) > w) || ((dstY + height) > h)) {
            throw new IllegalArgumentException(
                                J3dI18N.getString("ImageComponent3D7"));
        }

        ((ImageComponent3DRetained)this.retained).setSubImage(
                        index, image, width, height, srcX, srcY, dstX, dstY);
    }

    /**
     * Updates a particular slice of image data that is accessed by reference.
     * This method calls the updateData method of the specified
     * ImageComponent3D.Updater object to synchronize updates to the
     * image data that is referenced by this ImageComponent3D object.
     * Applications that wish to modify such data must perform all
     * updates via this method.
     * <p>
     * The data to be modified has to be within the boundary of the
     * subregion
     * specified by the offset (x, y) and the dimension (width*height).
     * It is illegal to modify data outside this boundary. If any
     * referenced data is modified outside the updateData method, or
     * any data outside the specified boundary is modified, the
     * results are undefined.
     * <p>
     * @param updater object whose updateData callback method will be
     * called to update the data referenced by this ImageComponent3D object.
     * @param index index of the image to be modified.
     * The index must be less than the depth of this ImageComponent3D object.
     * @param x starting X offset of the subregion.
     * @param y starting Y offset of the subregion.
     * @param width width of the subregion.
     * @param height height of the subregion.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalStateException if the data access mode is
     * <code>BY_COPY</code>.
     * @exception IllegalArgumentException if <code>width</code> or
     * <code>height</code> of
     * the subregion exceeds the dimension of the image in this object.
     * @exception IllegalArgumentException if <code>x</code> < 0, or
     * (<code>x</code> + <code>width</code>) > width of this object, or
     * <code>y</code> < 0, or
     * (<code>y</code> + <code>height</code>) > height of this object.
     * @exception ArrayIndexOutOfBoundsException if <code>index</code> > the
     * depth of this object.
     *
     * @since Java 3D 1.3
     */
    public void updateData(Updater updater, int index,
			   int x, int y,
			   int width, int height) {
        if (isLiveOrCompiled() &&
                !this.getCapability(ALLOW_IMAGE_WRITE)) {
            throw new CapabilityNotSetException(
                                J3dI18N.getString("ImageComponent3D5"));
        }

        if (!((ImageComponent3DRetained)this.retained).isByReference()) {
            throw new IllegalStateException(
                                J3dI18N.getString("ImageComponent3D6"));
        }

        int w = ((ImageComponent3DRetained)this.retained).getWidth();
        int h = ((ImageComponent3DRetained)this.retained).getHeight();

        if ((x < 0) || (y < 0) || ((x + width) > w) || ((y + height) > h)) {
            throw new IllegalArgumentException(
                                J3dI18N.getString("ImageComponent3D7"));
        }

        ((ImageComponent3DRetained)this.retained).updateData(
                                updater, index, x, y, width, height);
    }


    /**
     * Creates a retained mode ImageComponent3DRetained object that this
     * ImageComponent3D component object will point to.
     */
    void createRetained() {
        this.retained = new ImageComponent3DRetained();
        this.retained.setSource(this);
    }



    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	ImageComponent3DRetained rt = (ImageComponent3DRetained) retained;

	ImageComponent3D img = new ImageComponent3D(rt.getFormat(),
						    rt.width,
						    rt.height,
						    rt.depth);

	// XXXX : replace by this to duplicate other attributes
	/*
	ImageComponent3D img = new ImageComponent3D(rt.format,
	                                            rt.width,
						    rt.height,
						    rt.depth,
						    rt.byReference,
						    rt.yUp);
	 */
	img.duplicateNodeComponent(this);
	return img;
    }


   /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object).
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
      super.duplicateAttributes(originalNodeComponent, forceDuplicate);
      // TODO : Handle NioImageBuffer if its supported.
      RenderedImage imgs[] = ((ImageComponent3DRetained)
			      originalNodeComponent.retained).getImage();

      if (imgs != null) {
	  ImageComponent3DRetained rt = (ImageComponent3DRetained) retained;

	  for (int i=rt.depth-1; i>=0; i--) {
	      if (imgs[i] != null) {
		  rt.set(i, imgs[i]);
	      }
	  }
      }
    }

    /**
     * The ImageComponent3D.Updater interface is used in updating image data
     * that is accessed by reference from a live or compiled ImageComponent
     * object.  Applications that wish to modify such data must define a
     * class that implements this interface.  An instance of that class is
     * then passed to the <code>updateData</code> method of the
     * ImageComponent object to be modified.
     *
     * @since Java 3D 1.3
     */
    public static interface Updater {
	/**
	 * Updates image data that is accessed by reference.
	 * This method is called by the updateData method of an
	 * ImageComponent object to effect
	 * safe updates to image data that
	 * is referenced by that object.  Applications that wish to modify
	 * such data must implement this method and perform all updates
	 * within it.
	 * <br>
	 * NOTE: Applications should <i>not</i> call this method directly.
	 *
	 * @param imageComponent the ImageComponent object being updated.
	 * @param index index of the image to be modified.
	 * @param x starting X offset of the subregion.
	 * @param y starting Y offset of the subregion.
	 * @param width width of the subregion.
	 * @param height height of the subregion.
	 *
	 * @see ImageComponent3D#updateData
	 */
	public void updateData(ImageComponent3D imageComponent,
			       int index,
			       int x, int y,
			       int width, int height);
    }

}
