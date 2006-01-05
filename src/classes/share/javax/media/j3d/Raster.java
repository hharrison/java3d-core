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

import javax.vecmath.*;
import java.awt.Point;
import java.awt.Dimension;


/**
 * The Raster object extends Geometry to allow drawing a raster image
 * that is attached to a 3D location in the virtual world.
 * It contains a 3D point that is defined in the local object
 * coordinate system of the Shape3D node that references the Raster.
 * It also contains a type specifier, a clipping mode, a reference to
 * a ImageComponent2D object and/or a DepthComponent object, an
 * integer x,y source offset and a size (width, height) to allow
 * reading or writing a portion of the referenced image, and an
 * integer x,y destination offset to position the raster relative to
 * the transformed 3D point.
 * In addition to being used as a type of geometry for drawing,
 * a Raster may be used to readback pixel data (color and/or z-buffer)
 * from the frame buffer in immediate mode.
 * <p>
 * The geometric extent of a Raster object is a single 3D point, specified
 * by the raster position.  This means that geometry-based picking or
 * collision with a Raster object will only intersect the object at
 * this single point; the 2D raster image is neither pickable
 * nor collidable.
 */

public class Raster extends Geometry {
    /**
     * Specifies a Raster object with color data.
     * In this mode, the image reference must point to
     * a valid ImageComponent object.
     *
     * @see #setType
     */
    public static final int RASTER_COLOR = 0x1;

    /**
     * Specifies a Raster object with depth (z-buffer) data.
     * In this mode, the depthImage reference must point to
     * a valid DepthComponent object.
     *
     * @see #setType
     */
    public static final int RASTER_DEPTH = 0x2;

    /**
     * Specifies a Raster object with both color and depth (z-buffer) data.
     * In this mode, the image reference must point to
     * a valid ImageComponent object, and the depthImage reference
     * must point to a valid DepthComponent object.
     *
     * @see #setType
     */
    public static final int RASTER_COLOR_DEPTH = RASTER_COLOR | RASTER_DEPTH;


    /**
     * Specifies that this raster object is not drawn
     * if the raster position is outside the viewing volume.
     * In this mode, the raster is not drawn when the transformed
     * raster position is clipped out, even if part of the raster would
     * have been visible.  This is the default mode.
     *
     * @see #setClipMode
     *
     * @since Java 3D 1.3
     */
    public static final int CLIP_POSITION = 0;

    /**
     * Specifies that the raster object is clipped as an image after
     * the raster position has been transformed.  In this mode, part
     * of the raster may be drawn even when the transformed raster
     * position is clipped out.
     *
     * @see #setClipMode
     *
     * @since Java 3D 1.3
     */
    public static final int CLIP_IMAGE = 1;


    /**
     * Specifies that this Raster allows reading the position.
     */
    public static final int
    ALLOW_POSITION_READ = CapabilityBits.RASTER_ALLOW_POSITION_READ;

    /**
     * Specifies that this Raster allows writing the position.
     */
    public static final int
    ALLOW_POSITION_WRITE = CapabilityBits.RASTER_ALLOW_POSITION_WRITE;

    /** 
     * Specifies that this Raster allows reading the source or
     * destination offset.
     */ 
    public static final int
    ALLOW_OFFSET_READ = CapabilityBits.RASTER_ALLOW_OFFSET_READ;
 
    /** 
     * Specifies that this Raster allows writing the source or
     * destination offset.
     */ 
    public static final int 
    ALLOW_OFFSET_WRITE = CapabilityBits.RASTER_ALLOW_OFFSET_WRITE;

    /**  
     * Specifies that this Raster allows reading the image.
     */  
    public static final int 
    ALLOW_IMAGE_READ = CapabilityBits.RASTER_ALLOW_IMAGE_READ;
  
    /**  
     * Specifies that this Raster allows writing the image. 
     */  
    public static final int  
    ALLOW_IMAGE_WRITE = CapabilityBits.RASTER_ALLOW_IMAGE_WRITE;

    /**  
     * Specifies that this Raster allows reading the depth component.
     */  
    public static final int 
    ALLOW_DEPTH_COMPONENT_READ = CapabilityBits.RASTER_ALLOW_DEPTH_COMPONENT_READ;
  
    /**  
     * Specifies that this Raster allows writing the depth component. 
     */  
    public static final int  
    ALLOW_DEPTH_COMPONENT_WRITE = CapabilityBits.RASTER_ALLOW_DEPTH_COMPONENT_WRITE;

    /**  
     * Specifies that this Raster allows reading the size.
     */
    public static final int
    ALLOW_SIZE_READ = CapabilityBits.RASTER_ALLOW_SIZE_READ;
 
    /**                                                        
     * Specifies that this Raster allows writing the size.
     */
    public static final int
    ALLOW_SIZE_WRITE = CapabilityBits.RASTER_ALLOW_SIZE_WRITE;

    /**  
     * Specifies that this Raster allows reading the type. 
     */  
    public static final int 
    ALLOW_TYPE_READ = CapabilityBits.RASTER_ALLOW_TYPE_READ;
  
    /**  
     * Specifies that this Raster allows reading the clip mode.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_CLIP_MODE_READ = CapabilityBits.RASTER_ALLOW_CLIP_MODE_READ;
 
    /**                                                        
     * Specifies that this Raster allows writing the clip mode.
     *
     * @since Java 3D 1.3
     */
    public static final int
    ALLOW_CLIP_MODE_WRITE = CapabilityBits.RASTER_ALLOW_CLIP_MODE_WRITE;


   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_POSITION_READ,
	ALLOW_OFFSET_READ,
	ALLOW_IMAGE_READ,
	ALLOW_DEPTH_COMPONENT_READ,
	ALLOW_SIZE_READ,
	ALLOW_TYPE_READ,
	ALLOW_CLIP_MODE_READ
    };

    /**
     * Constructs a Raster object with default parameters.
     * The default values are as follows:
     * <ul>
     * type : RASTER_COLOR<br>
     * clipMode : CLIP_POSITION<br>
     * position : (0,0,0)<br>
     * srcOffset : (0,0)<br>
     * size : (0,0)<br>
     * dstOffset : (0,0)<br>
     * image : null<br>
     * depth component : null<br>
     * </ul>
     */
    public Raster() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a new Raster object with the specified values.
     * @param pos the position in object coordinates of the upper-left
     * corner of the raster
     * @param type the type of raster object, one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH
     * @param xSrcOffset the x offset within the source array of pixels
     * at which to start copying
     * @param ySrcOffset the y offset within the source array of pixels
     * at which to start copying
     * @param width the number of columns of pixels to copy
     * @param height the number of rows of pixels to copy
     * @param image the ImageComponent2D object containing the
     * color data
     * @param depthComponent the DepthComponent object containing the depth
     * (z-buffer) data
     */
    public Raster(Point3f pos,
                  int type,
                  int xSrcOffset,
                  int ySrcOffset,
                  int width,
                  int height,
                  ImageComponent2D image,
                  DepthComponent depthComponent) {

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((RasterRetained)this.retained).setPosition(pos);
        ((RasterRetained)this.retained).setType(type);
        ((RasterRetained)this.retained).setSrcOffset(xSrcOffset, ySrcOffset);
        ((RasterRetained)this.retained).setSize(width, height);
        ((RasterRetained)this.retained).setImage(image);
        ((RasterRetained)this.retained).setDepthComponent(depthComponent);
    }

    /**
     * Constructs a new Raster object with the specified values.
     * @param pos the position in object coordinates of the upper-left
     * corner of the raster
     * @param type the type of raster object, one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH
     * @param srcOffset the offset within the source array of pixels
     * at which to start copying
     * @param size the width and height of the image to be copied
     * @param image the ImageComponent2D object containing the
     * color data
     * @param depthComponent the DepthComponent object containing the depth
     * (z-buffer) data
     */
    public Raster(Point3f pos, 
                  int type, 
                  Point srcOffset,
                  Dimension size,
                  ImageComponent2D image, 
                  DepthComponent depthComponent) { 

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((RasterRetained)this.retained).setPosition(pos);
        ((RasterRetained)this.retained).setType(type);
        ((RasterRetained)this.retained).setSrcOffset(srcOffset.x, srcOffset.y);
        ((RasterRetained)this.retained).setSize(size.width, size.height);
        ((RasterRetained)this.retained).setImage(image);
        ((RasterRetained)this.retained).setDepthComponent(depthComponent);
    }

    /**
     * Constructs a new Raster object with the specified values.
     * @param pos the position in object coordinates of the upper-left
     * corner of the raster
     * @param type the type of raster object, one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH
     * @param clipMode the clipping mode of the raster object, one of:
     * CLIP_POSITION or CLIP_IMAGE
     * @param srcOffset the offset within the source array of pixels
     * at which to start copying
     * @param size the width and height of the image to be copied
     * @param dstOffset the destination pixel offset of the upper-left
     * corner of the rendered image relative to the transformed position
     * @param image the ImageComponent2D object containing the
     * color data
     * @param depthComponent the DepthComponent object containing the depth
     * (z-buffer) data
     *
     * @since Java 3D 1.3
     */
    public Raster(Point3f pos,
		  int type,
		  int clipMode,
		  Point srcOffset,
		  Dimension size,
		  Point dstOffset,
		  ImageComponent2D image,
		  DepthComponent depthComponent) {

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((RasterRetained)this.retained).setPosition(pos);
        ((RasterRetained)this.retained).setType(type);
        ((RasterRetained)this.retained).setClipMode(clipMode);
        ((RasterRetained)this.retained).setSrcOffset(srcOffset.x, srcOffset.y);
        ((RasterRetained)this.retained).setSize(size.width, size.height);
        ((RasterRetained)this.retained).setDstOffset(dstOffset.x, dstOffset.y);
        ((RasterRetained)this.retained).setImage(image);
        ((RasterRetained)this.retained).setDepthComponent(depthComponent);
    }

    /**
     * Creates the retained mode Raster object that this
     * Raster object will point to.
     */
    void createRetained() {
        retained = new RasterRetained();
        retained.setSource(this);
    }

    /**
     * Sets the position in object coordinates of this raster.  This
     * position is transformed into device coordinates and is used as
     * the upper-left corner of the raster.
     * @param pos the new position of this raster
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setPosition(Point3f pos) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster0"));  
        ((RasterRetained)this.retained).setPosition(pos);
    }

    /**
     * Retrieves the current position in object coordinates of this raster.
     * @param pos the vector that will receive the current position
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getPosition(Point3f pos) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster1"));  

        ((RasterRetained)this.retained).getPosition(pos);
    }

    /**
     * Sets the type of this raster object to one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH.
     * @param type the new type of this raster
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void setType(int type) {
        checkForLiveOrCompiled();
        ((RasterRetained)this.retained).setType(type);
    }
 
 
    /**
     * Retrieves the current type of this raster object, one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH.
     * @return type the type of this raster
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getType() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_TYPE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster2"));  
        return (((RasterRetained)this.retained).getType());
    }


    /**
     * Sets the clipping mode of this raster object.
     * @param clipMode the new clipping mode of this raster,
     * one of: CLIP_POSITION or CLIP_IMAGE.  The default mode
     * is CLIP_POSITION.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setClipMode(int clipMode) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CLIP_MODE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster10"));

        ((RasterRetained)this.retained).setClipMode(clipMode);
    }


    /**
     * Retrieves the current clipping mode of this raster object.
     * @return clipMode the clipping mode of this raster,
     * one of: CLIP_POSITION or CLIP_IMAGE.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getClipMode() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CLIP_MODE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster11"));

        return (((RasterRetained)this.retained).getClipMode());
    }


    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>setSrcOffset(int,int)</code>
     */
    public void setOffset(int xSrcOffset, int ySrcOffset) {
	setSrcOffset(xSrcOffset, ySrcOffset);
    }


    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>setSrcOffset(java.awt.Point)</code>
     */
    public void setOffset(Point srcOffset) {
	setSrcOffset(srcOffset);
    }


    /**
     * @deprecated As of Java 3D version 1.3, replaced by
     * <code>getSrcOffset(java.awt.Point)</code>
     */
    public void getOffset(Point srcOffset) {
	getSrcOffset(srcOffset);
    }


    /**
     * Sets the offset within the source array of pixels
     * at which to start copying.
     * @param xSrcOffset the x offset within the source array of pixels
     * at which to start copying
     * @param ySrcOffset the y offset within the source array of pixels
     * at which to start copying
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setSrcOffset(int xSrcOffset, int ySrcOffset) {

        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster7"));

        ((RasterRetained)this.retained).setSrcOffset(xSrcOffset, ySrcOffset);
    }


    /**
     * Sets the offset within the source array of pixels
     * at which to start copying.
     * @param srcOffset the new source pixel offset
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setSrcOffset(Point srcOffset) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster7"));

        ((RasterRetained)this.retained).setSrcOffset(srcOffset.x, srcOffset.y);
    }


    /**
     * Retrieves the current source pixel offset.
     * @param srcOffset the object that will receive the source offset
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void getSrcOffset(Point srcOffset) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster8"));

        ((RasterRetained)this.retained).getSrcOffset(srcOffset);
    }


    /**
     * Sets the number of pixels to be copied from the pixel array.
     * @param width the number of columns in the array of pixels to copy
     * @param height the number of rows in the array of pixels to copy
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setSize(int width, int height) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster9"));

        ((RasterRetained)this.retained).setSize(width, height);
    }  
 
    /**
     * Sets the size of the array of pixels to be copied.
     * @param size the new size
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setSize(Dimension size) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster9"));

        ((RasterRetained)this.retained).setSize(size.width, size.height);
    }  
 
 
    /**
     * Retrieves the current raster size.
     * @param size the object that will receive the size
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getSize(Dimension size) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster1"));  

        ((RasterRetained)this.retained).getSize(size);
    }  


    /**
     * Sets the destination pixel offset of the upper-left corner of
     * the rendered image relative to the transformed position.  This
     * pixel offset is added to the transformed raster position prior
     * to rendering the image.
     *
     * @param xDstOffset the x coordinate of the new offset
     * @param yDstOffset the y coordinate of the new offset
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setDstOffset(int xDstOffset, int yDstOffset) {

        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster7"));

        ((RasterRetained)this.retained).setDstOffset(xDstOffset, yDstOffset);
    }


    /**
     * Sets the destination pixel offset of the upper-left corner of
     * the rendered image relative to the transformed position.  This
     * pixel offset is added to the transformed raster position prior
     * to rendering the image.
     *
     * @param dstOffset the new destination pixel offset
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void setDstOffset(Point dstOffset) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster7"));

        ((RasterRetained)this.retained).setDstOffset(dstOffset.x, dstOffset.y);
    }


    /**
     * Retrieves the current destination pixel offset.
     * @param dstOffset the object that will receive the destination offset
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public void getDstOffset(Point dstOffset) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_OFFSET_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster8"));

        ((RasterRetained)this.retained).getDstOffset(dstOffset);
    }


    /**
     * Sets the pixel array used to copy pixels to/from a Canvas3D.
     * This is used when the type is RASTER_COLOR or RASTER_COLOR_DEPTH.
     * @param image the ImageComponent2D object containing the
     * color data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setImage(ImageComponent2D image) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_IMAGE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster3"));
        ((RasterRetained)this.retained).setImage(image);
    }

    /**
     * Retrieves the current pixel array object.
     * @return image the ImageComponent2D object containing the
     * color data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public ImageComponent2D getImage() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_IMAGE_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster4"));  
        return (((RasterRetained)this.retained).getImage());
    }

    /**
     * Sets the depth image used to copy pixels to/from a Canvas3D.
     * This is used when the type is RASTER_DEPTH or RASTER_COLOR_DEPTH.
     * @param depthComponent the DepthComponent object containing the
     * depth (z-buffer) data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public void setDepthComponent(DepthComponent depthComponent) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DEPTH_COMPONENT_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster5"));
        ((RasterRetained)this.retained).setDepthComponent(depthComponent);
    }

    /**
     * Retrieves the current depth image object.
     * @return depthImage DepthComponent containing the
     * depth (z-buffer) data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */  
    public DepthComponent getDepthComponent() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_DEPTH_COMPONENT_READ))
                throw new CapabilityNotSetException(J3dI18N.getString("Raster6"));  
        return (((RasterRetained)this.retained).getDepthComponent());
    }

   

   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        Raster r = new Raster();
        r.duplicateNodeComponent(this);           
        return r;
    }


    /**
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated replaced with duplicateNodeComponent(
     *  NodeComponent originalNodeComponent, boolean forceDuplicate)
     */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
	checkDuplicateNodeComponent(originalNodeComponent);
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

	RasterRetained raster = (RasterRetained) originalNodeComponent.retained;
	RasterRetained rt = (RasterRetained) retained;

	Point3f p = new Point3f();
	raster.getPosition(p);
	rt.setPosition(p);
	rt.setType(raster.getType());
	rt.setClipMode(raster.getClipMode());
	Point offset = new Point();
	raster.getSrcOffset(offset);
	rt.setSrcOffset(offset.x, offset.y);
	raster.getDstOffset(offset);
	rt.setDstOffset(offset.x, offset.y);
	Dimension dim = new Dimension();
	raster.getSize(dim);
	rt.setSize(dim.width, dim.height);
	rt.setImage((ImageComponent2D) getNodeComponent(
				     raster.getImage(),
				     forceDuplicate,
				     originalNodeComponent.nodeHashtable));
	rt.setDepthComponent((DepthComponent) getNodeComponent(
					    raster.getDepthComponent(),
					    forceDuplicate,
					    originalNodeComponent.nodeHashtable));
    }


 /** 
   *  This function is called from getNodeComponent() to see if any of
   *  the sub-NodeComponents  duplicateOnCloneTree flag is true. 
   *  If it is the case, current NodeComponent needs to 
   *  duplicate also even though current duplicateOnCloneTree flag is false. 
   *  This should be overwrite by NodeComponent which contains sub-NodeComponent.
   */
   boolean duplicateChild() {
      if (getDuplicateOnCloneTree())
	return true;
      RasterRetained rt = (RasterRetained) retained;

      NodeComponent nc = rt.getImage();
      if ((nc != null) && nc.getDuplicateOnCloneTree())
	return true;

      nc = rt.getDepthComponent();
      if ((nc != null) && nc.getDuplicateOnCloneTree())
	return true;

      return false;
   }

}
