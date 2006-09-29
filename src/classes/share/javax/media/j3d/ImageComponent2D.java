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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

/**
 * This class defines a 2D image component.  This is used for texture
 * images, background images and raster components of Shape3D nodes.
 * Prior to Java 3D 1.2, only BufferedImage objects could be used as the
 * input to an ImageComponent2D object.  As of Java 3D 1.2, an
 * ImageComponent2D accepts any RenderedImage object (BufferedImage is
 * an implementation of the RenderedImage interface).  The methods
 * that set/get a BufferedImage object are left in for compatibility.
 * The new methods that set/get a RenderedImage are a superset of the
 * old methods.  In particular, the two set methods in the following
 * example are equivalent:
 *
 * <p>
 * <ul>
 * <code>
 * BufferedImage bi;<br>
 * RenderedImage ri = bi;<br>
 * ImageComponent2D ic;<br>
 * <p>
 * // Set the image to the specified BufferedImage<br>
 * ic.set(bi);<br>
 * <p>
 * // Set the image to the specified RenderedImage<br>
 * ic.set(ri);<br>
 * </code>
 * </ul>
 *
 * <p>
 * As of Java 3D 1.5, an ImageComponent2D accepts an NioImageBuffer object
 * as an alternative to a RenderedImage.
 */

public class ImageComponent2D extends ImageComponent {

    // non-public, no parameter constructor
    ImageComponent2D() {}
    
    /**
     * Constructs a 2D image component object using the specified
     * format, width, and height. Default values are used for
     * all other parameters.  The default values are as follows:
     * <ul>
     * image : null<br>
     * imageClass : ImageClass.BUFFERED_IMAGE<br>
     * </ul>
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param width the number of columns of pixels in this image component
     * object
     * @param height the number of rows of pixels in this image component
     * object
     * @exception IllegalArgumentException if format is invalid, or if
     * width or height are not positive.
     */
    public ImageComponent2D(int		format,
			    int		width,
			    int		height) {

        ((ImageComponent2DRetained)this.retained).processParams(format, width, height, 1);
    }

    /**
     * Constructs a 2D image component object using the specified format
     * and BufferedImage.  A copy of the BufferedImage is made.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param image the BufferedImage used to create this 2D image component.
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the image are not positive.
     */
    public ImageComponent2D(int format, BufferedImage image) {

        ((ImageComponent2DRetained)this.retained).processParams(format, image.getWidth(), image.getHeight(), 1);
	((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Constructs a 2D image component object using the specified format
     * and RenderedImage.  A copy of the RenderedImage is made.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param image the RenderedImage used to create this 2D image component
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent2D(int format, RenderedImage image) {


	((ImageComponent2DRetained)this.retained).processParams(format, image.getWidth(), image.getHeight(), 1);
	((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Constructs a 2D image component object using the specified
     * format, width, height, byReference flag, and yUp flag.
     * Default values are used for all other parameters.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param width the number of columns of pixels in this image component
     * object
     * @param height the number of rows of pixels in this image component
     * object
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     * @exception IllegalArgumentException if format is invalid, or if
     * width or height are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent2D(int		format,
			    int		width,
			    int		height,
			    boolean	byReference,
			    boolean	yUp) {

 	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent2DRetained)this.retained).processParams(format, width, height, 1);
    }

    /**
     * Constructs a 2D image component object using the specified format,
     * BufferedImage, byReference flag, and yUp flag.
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param image the BufferedImage used to create this 2D image component
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent2D(int format,
			    BufferedImage image,
			    boolean byReference,
			    boolean yUp) {

 	((ImageComponentRetained)this.retained).setByReference(byReference);
 	((ImageComponentRetained)this.retained).setYUp(yUp);
 	((ImageComponent2DRetained)this.retained).processParams(format, image.getWidth(), image.getHeight(), 1);
 	((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Constructs a 2D image component object using the specified format,
     * RenderedImage, byReference flag, and yUp flag.
     * The image class is set to ImageClass.RENDERED_IMAGE if the byReferece
     * flag is true and the specified RenderedImage is <i>not</i> an instance
     * of BufferedImage. In all other cases, the image class is set to
     * ImageClass.BUFFERED_IMAGE.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param image the RenderedImage used to create this 2D image component
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the image are not positive.
     *
     * @since Java 3D 1.2
     */
    public ImageComponent2D(int format,
                            RenderedImage image,
                            boolean byReference,
                            boolean yUp) {

        ((ImageComponentRetained)this.retained).setByReference(byReference);
        ((ImageComponentRetained)this.retained).setYUp(yUp);
        ((ImageComponent2DRetained)this.retained).processParams(format, image.getWidth(), image.getHeight(), 1);
        ((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Constructs a 2D image component object using the specified format,
     * NioImageBuffer, byReference flag, and yUp flag.
     * The image class is set to ImageClass.NIO_IMAGE_BUFFER.
     *
     * @param format the image component format, one of: FORMAT_RGB,
     * FORMAT_RGBA, etc.
     * @param image the NioImageBuffer used to create this 2D image component
     * @param byReference a flag that indicates whether the data is copied
     * into this image component object or is accessed by reference.
     * @param yUp a flag that indicates the y-orientation of this image
     * component.  If yUp is set to true, the origin of the image is
     * the lower left; otherwise, the origin of the image is the upper
     * left.
     *
     * @exception IllegalArgumentException if format is invalid, or if
     * the width or height of the image are not positive.
     *
     * @exception IllegalArgumentException if the byReference flag is false.
     *
     * @exception IllegalArgumentException if the yUp flag is false.
     *
     * @since Java 3D 1.5
     */
    public ImageComponent2D(int format,
                            NioImageBuffer image,
                            boolean byReference,
                            boolean yUp) {

        ((ImageComponentRetained)this.retained).setByReference(byReference);
        ((ImageComponentRetained)this.retained).setYUp(yUp);
        ((ImageComponent2DRetained)this.retained).processParams(format, image.getWidth(), image.getHeight(), 1);
        ((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Sets this image component to the specified BufferedImage
     * object.
     * If the data access mode is not by-reference, then the
     * BufferedImage data is copied into this object.  If
     * the data access mode is by-reference, then a reference to the
     * BufferedImage is saved, but the data is not necessarily
     * copied.
     * <p>
     * The image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param image BufferedImage object containing the image.
     * Its size must be the same as the current size of this
     * ImageComponent2D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the width and height of the
     * specified image is not equal to the width and height of this
     * ImageComponent object.
     */
    public void set(BufferedImage image) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_WRITE))
            throw new CapabilityNotSetException(
				J3dI18N.getString("ImageComponent2D1"));
        }

	((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Sets this image component to the specified RenderedImage
     * object.  If the data access mode is not by-reference, the
     * RenderedImage data is copied into this object.  If
     * the data access mode is by-reference, a reference to the
     * RenderedImage is saved, but the data is not necessarily
     * copied.
     * <p>
     * The image class is set to ImageClass.RENDERED_IMAGE if the the
     * data access mode is by-reference and the specified
     * RenderedImage is <i>not</i> an instance of BufferedImage. In all
     * other cases, the image class is set to ImageClass.BUFFERED_IMAGE.
     *
     * @param image RenderedImage object containing the image.
     * Its size must be the same as the current size of this
     * ImageComponent2D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the width and height of the
     * specified image is not equal to the width and height of this
     * ImageComponent object.
     *
     * @since Java 3D 1.2
     */
    public void set(RenderedImage image) {
        if (isLiveOrCompiled()) {
          if(!this.getCapability(ALLOW_IMAGE_WRITE))
            throw new CapabilityNotSetException(
				J3dI18N.getString("ImageComponent2D1"));
        }

	((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Sets this image component to the specified NioImageBuffer
     * object.  If the data access mode is not by-reference, the
     * NioImageBuffer data is copied into this object.  If
     * the data access mode is by-reference, a reference to the
     * NioImageBuffer is saved, but the data is not necessarily
     * copied.
     * <p>
     * The image class is set to ImageClass.NIO_IMAGE_BUFFER.
     *
     * @param image NioImageBuffer object containing the image.
     * Its size must be the same as the current size of this
     * ImageComponent2D object.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if this ImageComponent object
     * is <i>not</i> yUp.
     *
     * @exception IllegalArgumentException if the width and height of the
     * specified image is not equal to the width and height of this
     * ImageComponent object.
     *
     * @since Java 3D 1.5
     */
    public void set(NioImageBuffer image) {
        if (isLiveOrCompiled()) {
            if(!this.getCapability(ALLOW_IMAGE_WRITE)) {
                throw new CapabilityNotSetException(
                        J3dI18N.getString("ImageComponent2D1"));
            }
        }

        ((ImageComponent2DRetained)this.retained).set(image);
    }

    /**
     * Retrieves the image from this ImageComponent2D object.  If the
     * data access mode is not by-reference, a copy of the image
     * is made.  If the data access mode is by-reference, the
     * reference is returned.
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
    public BufferedImage getImage() {
        if (isLiveOrCompiled()) {
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent2D0"));
	}

	RenderedImage img = ((ImageComponent2DRetained)this.retained).getImage();

	if ((img != null) && !(img instanceof BufferedImage)) {
	    throw new IllegalStateException(J3dI18N.getString("ImageComponent2D5"));
	}	
	return (BufferedImage) img;

    }

    /**
     * Retrieves the image from this ImageComponent2D object.  If the
     * data access mode is not by-reference, a copy of the image
     * is made.  If the data access mode is by-reference, the
     * reference is returned.
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
    public RenderedImage getRenderedImage() {

        if (isLiveOrCompiled())
            if(!this.getCapability(ImageComponent.ALLOW_IMAGE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent2D0"));
	return ((ImageComponent2DRetained)this.retained).getImage();
    }

    /**
     * Retrieves the image from this ImageComponent2D object.  If the
     * data access mode is not by-reference, a copy of the image
     * is made.  If the data access mode is by-reference, the
     * reference is returned.
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
     * @since Java 3D 1.5
     */
    public NioImageBuffer getNioImage() {

        if (isLiveOrCompiled()) {
            if (!this.getCapability(ImageComponent.ALLOW_IMAGE_READ)) {
                throw new CapabilityNotSetException(J3dI18N.getString("ImageComponent2D0"));
            }
        }
        return ((ImageComponent2DRetained)this.retained).getNioImage();

    }


    /**
     * Modifies a contiguous subregion of the image component.
     * Block of data of dimension (width * height)
     * starting at the offset (srcX, srcY) of the specified 
     * RenderedImage object will be copied into the image component
     * starting at the offset (dstX, dstY) of the ImageComponent2D object.
     * The specified RenderedImage object must be of the same format as
     * the current RenderedImage object in this image component.
     * This method can only be used if the data access mode is
     * by-copy. If it is by-reference, see updateData().
     *
     * @param image RenderedImage object containing the subimage.
     * @param width width of the subregion.
     * @param height height of the subregion.
     * @param srcX starting X offset of the subregion in the 
     * specified image.
     * @param srcY starting Y offset of the subregion in the 
     * specified image.
     * @param dstX starting X offset of the subregion in the image 
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
     * the subregion exceeds the dimension of the image of this object.
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
    public void setSubImage(RenderedImage image, int width, int height,
				int srcX, int srcY, int dstX, int dstY) {
        if (isLiveOrCompiled() &&
                !this.getCapability(ALLOW_IMAGE_WRITE)) {
            throw new CapabilityNotSetException(
                                J3dI18N.getString("ImageComponent2D1"));
        }

        if (((ImageComponent2DRetained)this.retained).isByReference()) {
            throw new IllegalStateException(
                                J3dI18N.getString("ImageComponent2D4"));
        }

        int w = ((ImageComponent2DRetained)this.retained).getWidth();
        int h = ((ImageComponent2DRetained)this.retained).getHeight();

        if ((srcX < 0) || (srcY < 0) || 
		((srcX + width) > w) || ((srcY + height) > h) ||
            (dstX < 0) || (dstY < 0) || 
		((dstX + width) > w) || ((dstY + height) > h)) {
            throw new IllegalArgumentException(
                                J3dI18N.getString("ImageComponent2D3"));
        }

        ((ImageComponent2DRetained)this.retained).setSubImage(
                                image, width, height, srcX, srcY, dstX, dstY);
    }

    /**
     * Updates image data that is accessed by reference.
     * This method calls the updateData method of the specified
     * ImageComponent2D.Updater object to synchronize updates to the
     * image data that is referenced by this ImageComponent2D object.
     * Applications that wish to modify such data must perform all
     * updates via this method.
     * <p>
     * The data to be modified has to be within the boundary of the
     * subregion
     * specified by the offset (x, y) and the dimension (width*height).
     * It is illegal to modify data outside this boundary.
     * If any referenced data is modified outisde the updateData
     * method, or any data outside the specified boundary is modified,
     * the results are undefined.
     * <p>
     * @param updater object whose updateData callback method will be
     * called to update the data referenced by this ImageComponent2D object.
     * @param x starting X offset of the subregion.
     * @param y starting Y offset of the subregion.
     * @param width width of the subregion.
     * @param height height of the subregion.
     *
     * @exception CapabilityNotSetException if the appropriate capability
     * is not set, and this object is part of a live or compiled scene graph
     * @exception IllegalStateException if the data access mode is
     * <code>BY_COPY</code>.
     * @exception IllegalArgumentException if <code>width</code> or
     * <code>height</code> of
     * the subregion exceeds the dimension of the image of this object.
     * @exception IllegalArgumentException if <code>x</code> < 0, or
     * (<code>x</code> + <code>width</code>) > width of this object, or
     * <code>y</code> < 0, or
     * (<code>y</code> + <code>height</code>) > height of this object.
     *
     * @since Java 3D 1.3
     */
    public void updateData(Updater updater, 
			   int x, int y,
			   int width, int height) {

	if (isLiveOrCompiled() &&
		!this.getCapability(ALLOW_IMAGE_WRITE)) {
	    throw new CapabilityNotSetException(
                                J3dI18N.getString("ImageComponent2D1"));
	}

	if (!((ImageComponent2DRetained)this.retained).isByReference()) {
	    throw new IllegalStateException(
				J3dI18N.getString("ImageComponent2D2"));
	}

	int w = ((ImageComponent2DRetained)this.retained).getWidth();
	int h = ((ImageComponent2DRetained)this.retained).getHeight();

	if ((x < 0) || (y < 0) || ((x + width) > w) || ((y + height) > h)) {
	    throw new IllegalArgumentException(
				J3dI18N.getString("ImageComponent2D3"));
	}

	((ImageComponent2DRetained)this.retained).updateData(
				updater, x, y, width, height);
    }

    /**
     * Creates a retained mode ImageComponent2DRetained object that this
     * ImageComponent2D component object will point to.
     */
    void createRetained() {
	this.retained = new ImageComponent2DRetained();
	this.retained.setSource(this);
    }

    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	ImageComponent2DRetained rt = (ImageComponent2DRetained) retained;

	ImageComponent2D img = new ImageComponent2D(rt.getFormat(),
						    rt.width,
						    rt.height,
						    rt.byReference,
						    rt.yUp);
	img.duplicateNodeComponent(this);
	return img;
    }

      
   /**
     * Copies all node information from <code>originalNodeComponent</code> 
     * into the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object). 
     *
     * @param originalNodeComponent the original node to duplicate
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

        ImageComponent.ImageClass imageClass =
                ((ImageComponentRetained)originalNodeComponent.retained).getImageClass();
        if(imageClass == ImageComponent.ImageClass.NIO_IMAGE_BUFFER) {
            NioImageBuffer nioImg = ((ImageComponent2DRetained)
            originalNodeComponent.retained).getNioImage();

            if(nioImg != null) {
                ((ImageComponent2DRetained) retained).set(nioImg);
            }
        } else {
            RenderedImage img = ((ImageComponent2DRetained)
            originalNodeComponent.retained).getImage();

            if (img != null) {
                ((ImageComponent2DRetained) retained).set(img);
            }
        }
    }

    /**
     * The ImageComponent2D.Updater interface is used in updating image data
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
	 * @param x starting X offset of the subregion.
	 * @param y starting Y offset of the subregion.
	 * @param width width of the subregion.
	 * @param height height of the subregion.
	 *
	 * @see ImageComponent2D#updateData
	 */
	public void updateData(ImageComponent2D imageComponent,
			       int x, int y,
			       int width, int height);
    }

}
