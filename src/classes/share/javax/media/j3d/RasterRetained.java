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
import java.util.ArrayList;

import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;

/**
 * A Retained Raster.
 */

class RasterRetained extends GeometryRetained {

    /**
     * Raster type
     */  
    int type = Raster.RASTER_COLOR;

    int clipMode = Raster.CLIP_POSITION;
    Point3f position = new Point3f();
    int xSrcOffset = 0;
    int ySrcOffset = 0;

    // Used internally in CLIP_IMAGE mode
    private int xOffset = 0;
    private int yOffset = 0;

    int width = 0;
    int height = 0;
    int xDstOffset = 0;
    int yDstOffset = 0;
    ImageComponent2DRetained image = null;
    DepthComponentRetained depthComponent = null;
    float lastAlpha = 1.0f;

    private Point3d adjPos;		// Position of the Raster after adjusting for dstOffset
    private Point2d winCoord;	// Position of Raster in window coordinates
    private Transform3D vwip;	// Vworld to Image plate transform
    // false when computeWinCoord() get null RenderMolecule.
    // In this case rendering is skip.
    private boolean validVwip; 

    
    RasterRetained() {
    	this.geoType = GEO_TYPE_RASTER;
	
	vwip = new Transform3D();
	adjPos = new Point3d();
	winCoord = new Point2d();
    }

    /**
     * Set the Raster position
     * @param position new raster position 
     */  
    final void setPosition(Point3f pos) {
	geomLock.getLock();
	position.x = pos.x;
	position.y = pos.y;
	position.z = pos.z;
	geomLock.unLock();
        sendChangedMessage(J3dThread.UPDATE_GEOMETRY, null, null);
    }

    /**
     * Retrieves the Raster's position
     * @param position the variable to receive the position vector
     */
    final void getPosition(Point3f pos) {
	pos.x = position.x;
	pos.y = position.y;
	pos.z = position.z;
    }

    /**
     * Sets the type of this raster object to one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH.
     * @param type the new type of this raster
     */
    final void setType(int type) {
	geomLock.getLock();
        this.type = type;
	geomLock.unLock();
    }
 
 
    /**
     * Retrieves the current type of this raster object, one of: RASTER_COLOR,
     * RASTER_DEPTH, or RASTER_COLOR_DEPTH.
     * @return type the type of this raster
     */
    final int getType() {
        return type;
    }

    /**
     * Sets the clipping mode of this raster object.
     * @param clipMode the new clipping mode of this raster,
     * one of: CLIP_POSITION or CLIP_IMAGE.  The default mode
     * is CLIP_POSITION.
     */
    final void setClipMode(int clipMode) {

	geomLock.getLock();
	this.clipMode = clipMode;
	geomLock.unLock();
	computeBoundingBox();
	if(source.isLive()) {
	    //update the Shape3Ds that refer to this Raster
	    int un = userLists.size();
	    ArrayList shapeList; 
	    Shape3DRetained ms, shape;
	    int sn; 
	    for(int i = 0; i < un; i++) {
		shapeList = (ArrayList)userLists.get(i);
		sn = shapeList.size();
		for(int j = 0; j < sn; j++) {
		    ms = (Shape3DRetained)shapeList.get(j);
		    shape = (Shape3DRetained)ms.sourceNode;
		    shape.setBoundsAutoCompute(false);
		    shape.setBounds(geoBounds);
		}
	    }
	}

    }
 
 
    /**
     * Retrieves the current clipping mode of this raster object.
     * @return clipMode the clipping mode of this raster,
     * one of: CLIP_POSITION or CLIP_IMAGE.
     */
    final int getClipMode() {
        return clipMode;
    }

    /**
     * Sets the offset within the source array of pixels at which
     * to start copying.
     * @param xSrcOffset the x offset within the source array of pixels
     * at which to start copying
     * @param ySrcOffset the y offset within the source array of pixels
     * at which to start copying
     */
    final void setSrcOffset(int xSrcOffset, int ySrcOffset) {
	geomLock.getLock();
	this.xSrcOffset = xSrcOffset;
	this.ySrcOffset = ySrcOffset;
	geomLock.unLock();
    }

    /**
     * Retrieves the current source pixel offset.
     * @param srcOffset the object that will receive the source offset
     */
    final void getSrcOffset(Point srcOffset) {
	srcOffset.setLocation(xSrcOffset, ySrcOffset);
    }

    /**
     * Sets the number of pixels to be copied from the pixel array.
     * @param width the number of columns in the array of pixels to copy
     * @param height the number of rows in the array of pixels to copy
     */
    final void setSize(int width, int height) {
	geomLock.getLock();
	this.width = width;
	this.height = height;
	geomLock.unLock();
    }  
 
 
    /**
     * Sets the size of the array of pixels to be copied.
     * @param size the new size
     */
    final void getSize(Dimension size) {
	size.setSize(width, height);
    }  


    /**
     * Sets the destination pixel offset of the upper-left
     * corner of the rendered image relative to the transformed position.
     * @param xDstOffset the x coordinate of the new offset
     * @param yDstOffset the y coordinate of the new offset
     */
    final void setDstOffset(int xDstOffset, int yDstOffset) {
	geomLock.getLock();
	this.xDstOffset = xDstOffset;
	this.yDstOffset = yDstOffset;
	geomLock.unLock();
    }

    /**
     * Retrieves the current destination pixel offset.
     * @param dstOffset the object that will receive the destination offset
     */
    final void getDstOffset(Point dstOffset) {
	dstOffset.setLocation(xDstOffset, yDstOffset);
    }


    /**
     * Sets the pixel array used to copy pixels to/from a Canvas3D.
     * This is used when the type is RASTER_COLOR or RASTER_COLOR_DEPTH.
     * @param image the ImageComponent2D object containing the
     * color data
     */
    final void setImage(ImageComponent2D image) {
	ImageComponent2DRetained oldImage = this.image;

	if (this.source.isLive()) {

	    if (this.image != null) {
		this.image.clearLive(refCount);
	    }
	    if (image != null) {
		((ImageComponent2DRetained)image.retained).setLive(inBackgroundGroup, refCount);
	    }
	}

	geomLock.getLock();
	if (image != null) {
	    ImageComponent2DRetained rimage = 
		(ImageComponent2DRetained)image.retained;
            
            /*  Don't think this is needed.   --- Chien.            
            rimage.setRasterRef();
             */
	    this.image = rimage;
	} else {
	    this.image = null;
	}



	// Set the lastAlpha to 1.0f
	lastAlpha = 1.0f;
	geomLock.unLock();
	sendChangedMessage((J3dThread.UPDATE_RENDER|J3dThread.UPDATE_RENDERING_ATTRIBUTES),
			   oldImage, this.image);
    }  

    /**
     * Retrieves the current pixel array object.
     * @return image the ImageComponent2D object containing the
     * color data
     */
    final ImageComponent2D getImage() {
	return (image == null ? null : (ImageComponent2D)image.source);
    }

    /**
     * Sets the depth image used to copy pixels to/from a Canvas3D.
     * This is used when the type is RASTER_DEPTH or RASTER_COLOR_DEPTH.
     * @param depthImage the DepthComponent object containing the
     * depth (z-buffer) data
     */
    final void setDepthComponent(DepthComponent depthComponent) {
	if (this.source.isLive()) {
	    if (this.depthComponent != null) {
		this.depthComponent.clearLive(refCount);
	    }
	    if (depthComponent != null) {
		((DepthComponentRetained)depthComponent.retained).setLive(inBackgroundGroup, refCount);
	    }
	}
	geomLock.getLock();
	if (depthComponent == null) {
            this.depthComponent = null;
	} else {
            this.depthComponent = 
		(DepthComponentRetained)depthComponent.retained;
	}
	geomLock.unLock();
    }  
 
    /**
     * Retrieves the current depth image object.
     * @return depthImage DepthComponent containing the
     * depth (z-buffer) data
     */
    final DepthComponent getDepthComponent() {
	return (depthComponent == null ? null :
		(DepthComponent)depthComponent.source);
    }

    void setLive(boolean inBackgroundGroup, int refCount) {
        super.doSetLive(inBackgroundGroup, refCount);
	if (image != null) {
	    image.setLive(inBackgroundGroup, refCount);
	}
	if (depthComponent != null) {
	    depthComponent.setLive(inBackgroundGroup, refCount);
	}
        isEditable = source.getCapability(Raster.ALLOW_OFFSET_WRITE) ||
	    source.getCapability(Raster.ALLOW_POSITION_WRITE) ||
	    ((type & Raster.RASTER_COLOR) != 0 &&
	     source.getCapability(Raster.ALLOW_IMAGE_WRITE)) ||
	    ((type & Raster.RASTER_DEPTH) != 0 &&
	     source.getCapability(
				     Raster.ALLOW_DEPTH_COMPONENT_WRITE)) ||
	    source.getCapability( Raster.ALLOW_SIZE_WRITE);

	super.markAsLive();
    }

    void clearLive(int refCount) {
	super.clearLive(refCount);
	if (image != null)
	    image.clearLive(refCount);
	if (depthComponent != null)
	    depthComponent.clearLive(refCount);
    }
    /*
    // Simply pass along to the NodeComponents
    void compile(CompileState compState) {
	setCompiled();

	if (image != null)
	    image.compile(compState);

	if (depthComponent != null)
	    depthComponent.compile(compState);
    }
    */

    void computeBoundingBox() {
	if(clipMode == Raster.CLIP_IMAGE) {
	    // Disable view frustum culling by setting the raster's bounds to 
	    // infinity. 
    	    Point3d minBounds = new Point3d(Double.NEGATIVE_INFINITY, 
		    Double.NEGATIVE_INFINITY, 
		    Double.NEGATIVE_INFINITY);
	    Point3d maxBounds = new Point3d(Double.POSITIVE_INFINITY, 
		    Double.POSITIVE_INFINITY, 
		    Double.POSITIVE_INFINITY); 
	    geoBounds.setUpper(maxBounds);
	    geoBounds.setLower(minBounds);
	} else {
	    Point3d center = new Point3d();
	    center.x = position.x;
	    center.y = position.y;
	    center.z = position.z;
	    geoBounds.setUpper(center);
	    geoBounds.setLower(center);
	}
    }

    void update() {
	computeBoundingBox();
    }   

    private void sendChangedMessage(int threads, Object arg1, Object arg2) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {
		synchronized (universeList) {
		    int numMessages = universeList.size();
		    J3dMessage[] m = new J3dMessage[numMessages];
		    for (int i=0; i<numMessages; i++) {
			m[i] = VirtualUniverse.mc.getMessage();
			m[i].type = J3dMessage.GEOMETRY_CHANGED;
			m[i].threads = threads;
			m[i].args[0] = Shape3DRetained.
			    getGeomAtomsArray((ArrayList)userLists.get(i));
			m[i].args[1] = this;
			Object[] obj = new Object[2];
			obj[0] = arg1;
			obj[1] = arg2;
			m[i].args[2] = obj;
			m[i].args[3] = new Integer(changedFrequent);
			m[i].universe = (VirtualUniverse)universeList.get(i);
		    }
		    VirtualUniverse.mc.processMessage(m);
		}
	    }
	}
    }


    void execute(Canvas3D cv, RenderAtom ra, boolean isNonUniformScale, 
	    boolean updateAlpha, float alpha,
	    int screen, boolean ignoreVertexColors) {
        
        throw new RuntimeException("Sorry!!! RASTER is temporarily unsupported.");

/*
        // Compute the offset position of the raster 
	// This has to be done at render time because we need access 
	// to the Canvas3D info

	// Check if adjusted position needs to be computed

	validVwip = true;
	adjPos.set((double)position.x, (double)position.y, (double)position.z);

	if(xDstOffset != 0 || yDstOffset != 0) {			       
	    getOffsetPos(cv, ra, adjPos);
	}

	xOffset = xSrcOffset;
	yOffset = ySrcOffset;
	// Check if the image needs to be clipped

	if (clipMode == Raster.CLIP_IMAGE) 
	    clipImage(cv, ra, adjPos);

	if (!validVwip) {
	    return;
	}
	if ((image != null) && !image.imageYdownCacheDirty) {
	    // If its a null image do nothing ..
	    if (image != null && image.imageYdown[0] != null) {
		// Handle alpha, if necessary
		// Note, raster always makes a copy, so we can update alpha
		// in the image
		if (updateAlpha) {
		    // Update Alpha value per screen
		    // If the image is by reference, force a copy, since
		    // we need to copy the alpha values
		    image.updateAlpha(cv, screen, alpha);
		    Pipeline.getPipeline().executeRaster(cv.ctx, this, updateAlpha, alpha,
			    type, width, height, xOffset, yOffset,
			    (float)adjPos.x, (float)adjPos.y , (float)adjPos.z,
			    image.imageYdown[screen]);
		}
		else {
		    Pipeline.getPipeline().executeRaster(cv.ctx, this, updateAlpha, alpha,
			    type, width, height, xOffset, yOffset,
			    (float)adjPos.x, (float)adjPos.y , (float)adjPos.z,
			    image.imageYdown[0]);
		}
	    }
	}
 */
    }
    
    /**
     * Computes the position of the origin of this Raster in object coordinates
     * The origin is the top-left corner offset by the destination offset
     * The offset position is returned in objCoord
     *
     * @param objCoord - Position of the Raster in object coordinates
     * @return nothing. The offset position is returned in objCoord
     */
    private void getOffsetPos(Canvas3D canvas, RenderAtom ra, Point3d objCoord) {
	computeWinCoord(canvas, ra, winCoord, objCoord);
	
	// Add the destination offset to the Raster position in window coordinates
	winCoord.x -= xDstOffset;
	winCoord.y -= yDstOffset;
	
	// Now back transform this offset pt. from window to object coordinates
	computeObjCoord(canvas, winCoord, objCoord);
	// pt. is now in object space again
    }
    
    /**
     * Clips the image against the window.  This method simulates
     * clipping the image by determining the subimage that will be
     * drawn and adjusting the xOffset and yOffset accordingly.  Only
     * clipping against the left and top edges needs to be handled,
     * clipping against the right and bottom edges will be handled by
     * the underlying graphics library automatically.
     */
    private void clipImage(Canvas3D canvas, RenderAtom ra, Point3d objCoord) {
	// check if window coordinates have already been calculated by
	// getOffsetPos(). 

	if(xDstOffset == 0 && yDstOffset == 0) {
	    double x = objCoord.x;
	    double y = objCoord.y;
	    double z = objCoord.z;
	    computeWinCoord(canvas, ra, winCoord, objCoord);

	    if ((winCoord.x > 0) && (winCoord.y > 0)) {
		objCoord.x = x;
		objCoord.y = y;
		objCoord.z = z;
		return; // no need to clip
	    }
	} else {
	    if ((winCoord.x > 0) && (winCoord.y > 0)) {
		return;
	    }	    
	}


	// Check if the Raster point will be culled
	// Note that w use 1 instead of 0, because when hardware
	// tranform the coordinate back to winCoord it may get
	// a small negative value due to numerically inaccurancy.
	// This clip the Raster away and cause flickering 
	// (see bug 4732965)
	if(winCoord.x < 1) {
	    // Negate the window position and use this as the offset
	    xOffset = (int)-winCoord.x+1;
	    winCoord.x = 1;
	}
	
	if(winCoord.y < 1) {
	    // Negate the window position and use this as the offset
	    yOffset = (int)-winCoord.y+1;
	    winCoord.y = 1;
	}
	
	//check if user-specified subimage is smaller than the clipped image
	if (xOffset < xSrcOffset)
	    xOffset = xSrcOffset;
	if(yOffset < ySrcOffset)
	    yOffset = ySrcOffset;
	// back transform to object coords
	if(xDstOffset == 0 && yDstOffset == 0) 
	    // Image plate to local Xform needs to be computed
	    computeObjCoord(canvas, winCoord, objCoord);
	else {
	    // vwip should contain the Imageplate to Local transform 
	    // (it was computed by computeObjCoord). 
	    // We can simply use the previously computed value here
	    canvas.getPixelLocationInImagePlate(winCoord.x, winCoord.y, 
						objCoord.z, objCoord);
	    vwip.transform(objCoord);
	}

    }
    
    private void computeObjCoord(Canvas3D canvas, Point2d winCoord, Point3d objCoord) {
	// Back transform this pt. from window to object coordinates
	// Assumes this method is ALWAYS called after computeWinCoord has been 
	// called. computeWinCoord calculates the Vworld to Image Plate Xform. 
	// This method simply uses it without recomputing it.
	
	canvas.getPixelLocationInImagePlate(winCoord.x, winCoord.y, objCoord.z, 
					    objCoord);
	// Get image plate to object coord transform
	// inv(P x M)
	vwip.invert();
	vwip.transform(objCoord);
    }
    
    private void computeWinCoord(Canvas3D canvas, RenderAtom ra, 
				Point2d winCoord, Point3d objCoord) {
	// Get local to Vworld transform
	RenderMolecule rm = ra.renderMolecule;

	if (rm == null) {
	    // removeRenderAtom() may set ra.renderMolecule to null
	    // in RenderBin before this renderer thread run. 
	    validVwip = false;
	    return;
	}
	// MT safe issue: We can't reference ra.renderMolecule below since
        // RenderBin thread may set it to null anytime. Use rm instead.

	Transform3D lvw = rm.localToVworld[rm.localToVworldIndex[
				 NodeRetained.LAST_LOCAL_TO_VWORLD]];
	
	// Get Vworld to image plate Xform
	canvas.getLastVworldToImagePlate(vwip);
	
	// v' = vwip x lvw x v 		
	// 		where v' = transformed vertex, 
	// 			  lvw = local to Vworld Xform
	//			  vwip = Vworld to Image plate Xform
	//			  v = vertex
	
	// Compute composite local to image plate Xform
	vwip.mul(lvw);
	
	// Transform the Raster's position from object to world coordinates
	vwip.transform(objCoord);
	
	// Get the window coordinates of this point
	canvas.getPixelLocationFromImagePlate(objCoord, winCoord);
    }
    
    int getClassType() {
	return RASTER_TYPE;
    }
    
    // notifies the Raster mirror object that the image data in a referenced
    // ImageComponent object is changed.
    // Currently we are not making use of this information.

    void notifyImageComponentImageChanged(ImageComponentRetained image,
                                        ImageComponentUpdateInfo value) {
    }

    boolean intersect(PickShape pickShape, PickInfo pickInfo, int flags, Point3d iPnt,
                      GeometryRetained geom, int geomIndex) {
         return false;
     }   
    
    boolean intersect(Bounds targetBound) {
	return false;
    }

    boolean intersect(Point3d[] pnts) {
	return false;
    }
    boolean intersect(Transform3D thisToOtherVworld, GeometryRetained
		      geom) {
	return false;
    }
    boolean intersect(Transform3D thisLocalToVworld, 
		       Transform3D otherLocalToVworld,
		       GeometryRetained geom) {
	return false;
    }

    boolean intersect(Transform3D thisLocalToVworld, Bounds targetBound) {
	return false;
    }
    void handleFrequencyChange(int bit) {
	if (bit == Raster.ALLOW_IMAGE_WRITE)
	    setFrequencyChangeMask(bit, 0x1);
	
    }

}
