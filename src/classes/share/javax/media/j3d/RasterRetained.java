/*
 * $RCSfile$
 *
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import javax.vecmath.Point2d;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point4d;

/**
 * A Retained Raster.
 */

class RasterRetained extends GeometryRetained {

    /**
     * Raster type
     */
    int type = Raster.RASTER_COLOR;

    private int clipMode = Raster.CLIP_POSITION;
    private Point3f position = new Point3f();
    private int xSrcOffset = 0;
    private int ySrcOffset = 0;
    private int width = 0;
    private int height = 0;
    private int xDstOffset = 0;
    private int yDstOffset = 0;
    ImageComponent2DRetained image = null;
    Texture2DRetained texture = null;
    DepthComponentRetained depthComponent = null;

    RasterRetained() {
    	this.geoType = GEO_TYPE_RASTER;
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
	    Shape3DRetained ms, shape;
	    int sn;
	    for(int i = 0; i < un; i++) {
			ArrayList<Shape3DRetained> shapeList = userLists.get(i);
		sn = shapeList.size();
		for(int j = 0; j < sn; j++) {
				ms = shapeList.get(j);
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
     * Gets the size of the array of pixels to be copied.
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
     * Initializes the raster image to the specified image.
     * @param image new ImageCompoent2D object used as the raster image
     */
    final void initImage(ImageComponent2D img) {

        int texFormat;

        if(img == null) {
            image = null;
            texture = null;
            return;
        }

        image = (ImageComponent2DRetained) img.retained;
        image.setEnforceNonPowerOfTwoSupport(true);
        switch(image.getNumberOfComponents()) {
            case 1:
                texFormat = Texture.INTENSITY;
                break;
            case 2:
                texFormat = Texture.LUMINANCE_ALPHA;
                break;
            case 3:
                texFormat = Texture.RGB;
                break;
            case 4:
                texFormat = Texture.RGBA;
                break;
            default:
                assert false;
                return;
        }

        Texture2D tex2D = new Texture2D(Texture.BASE_LEVEL, texFormat,
                img.getWidth(), img.getHeight());
        texture = (Texture2DRetained) tex2D.retained;
        texture.setUseAsRaster(true);
        // Fix to issue 372 : ImageComponent.set(BufferedImage) ignored when used by Raster
        image.addUser(texture);
        texture.initImage(0,img);

    }

    /**
     * Sets the pixel array used to copy pixels to/from a Canvas3D.
     * This is used when the type is RASTER_COLOR or RASTER_COLOR_DEPTH.
     * @param image the ImageComponent2D object containing the
     * color data
     */
    final void setImage(ImageComponent2D img) {

        if((img != null) &&
                (img.getImageClass() == ImageComponent.ImageClass.NIO_IMAGE_BUFFER)) {
            throw new IllegalArgumentException(J3dI18N.getString("Background14"));
        }

        TextureRetained oldTex = this.texture;
        if (source.isLive()) {
            if (this.texture != null) {
                this.texture.clearLive(refCount);
            }
        }

        // Issue 370: only hold the geomLock while calling initImage
        // (cannot hold it while sending a message).
        geomLock.getLock();
        initImage(img);
        geomLock.unLock();

        if (source.isLive()) {
            if (texture != null) {
                texture.setLive(inBackgroundGroup, refCount);
            }

            sendChangedMessage((J3dThread.UPDATE_RENDER|J3dThread.UPDATE_RENDERING_ATTRIBUTES),
                    oldTex, this.texture);
        }
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
	geomLock.getLock();
        if (this.source.isLive()) {
	    if (this.depthComponent != null) {
		this.depthComponent.clearLive(refCount);
	    }
	    if (depthComponent != null) {
		((DepthComponentRetained)depthComponent.retained).setLive(inBackgroundGroup, refCount);
	    }
	}

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
	if (texture != null) {
	    texture.setLive(inBackgroundGroup, refCount);
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
	if (texture != null)
	    texture.clearLive(refCount);
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
			m[i] = new J3dMessage();
			m[i].type = J3dMessage.GEOMETRY_CHANGED;
			m[i].threads = threads;
			m[i].args[0] = Shape3DRetained.getGeomAtomsArray(userLists.get(i));
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

        // Compute the offset position of the raster
        // This has to be done at render time because we need access
        // to the Canvas3D info

        // Check if adjusted position needs to be computed
        Point3d adjPos = new Point3d();    // Position of the Raster after adjusting for dstOffset
        adjPos.set(position);

        Point2d winCoord = new Point2d();  // Position of Raster in window coordinates
        Transform3D localToImagePlate = new Transform3D();  // Local to Image plate transform

        Point3d clipCoord = computeWinCoord(cv, ra, winCoord, adjPos, localToImagePlate);

        // Test raster for out of bounds in Z.
        if (clipCoord == null) {
            return;
        }

        if(clipMode == Raster.CLIP_POSITION) {
            // Do trivial reject test on Raster position.
            if(!isRasterClipPositionInside(clipCoord)) {
                return;
            }
        }

        // Add the destination offset to the Raster position in window coordinates
        winCoord.x += xDstOffset;
        winCoord.y += yDstOffset;

        // System.err.println("Step 2 : adjPos " + adjPos + " winCoord " + winCoord);


        if((type == Raster.RASTER_COLOR) || (type == Raster.RASTER_COLOR_DEPTH)) {
            float devCoordZ = (float) (clipCoord.z * 0.5 - 0.5);
            // Do textfill stuffs
            if (texture != null) {
                // setup Texture pipe.
                cv.updateTextureForRaster(texture);

			cv.textureFill(this, winCoord, devCoordZ, alpha);

                // Restore texture pipe.
                cv.restoreTextureBin();
            }

        }

        if((type == Raster.RASTER_DEPTH) || (type == Raster.RASTER_COLOR_DEPTH)) {

            Point2i srcOffset = new Point2i(xSrcOffset, ySrcOffset);

            if (clipMode == Raster.CLIP_IMAGE) {
                clipImage(cv, ra, winCoord, srcOffset);
            }

            computeObjCoord(cv, winCoord, adjPos, localToImagePlate);

            cv.executeRasterDepth(cv.ctx,
                    (float) adjPos.x,
                    (float) adjPos.y,
                    (float) adjPos.z,
                    srcOffset.x,
                    srcOffset.y,
                    width,
                    height,
                    depthComponent.width,
                    depthComponent.height,
                    depthComponent.type,
                    ((DepthComponentIntRetained) depthComponent).depthData);

        }
    }


    /**
     * Clips the image against the window.  This method simulates
     * clipping the image by determining the subimage that will be
     * drawn and adjusting the xOffset and yOffset accordingly.  Only
     * clipping against the left and top edges needs to be handled,
     * clipping against the right and bottom edges will be handled by
     * the underlying graphics library automatically.
     */
    private void clipImage(Canvas3D canvas, RenderAtom ra, Point2d winCoord, Point2i srcOffset) {

        if ((winCoord.x > 0) && (winCoord.y > 0)) {
            return;
        }

	// Check if the Raster point will be culled
	// Note that w use 1 instead of 0, because when hardware
	// tranform the coordinate back to winCoord it may get
	// a small negative value due to numerically inaccurancy.
	// This clip the Raster away and cause flickering
	// (see bug 4732965)
	if(winCoord.x < 1) {
	    // Negate the window position and use this as the offset
	    srcOffset.x = (int)-winCoord.x+1;
	    winCoord.x = 1;
	}

	if(winCoord.y < 1) {
	    // Negate the window position and use this as the offset
	    srcOffset.y = (int)-winCoord.y+1;
	    winCoord.y = 1;
	}

	//check if user-specified subimage is smaller than the clipped image
	if (srcOffset.x < xSrcOffset)
	    srcOffset.x = xSrcOffset;
	if(srcOffset.y < ySrcOffset)
	    srcOffset.y = ySrcOffset;

    }


    private boolean isRasterClipPositionInside(Point3d clipCoord) {
        return (clipCoord.x >= -1.0) && (clipCoord.x <= 1.0) &&
                (clipCoord.y >= -1.0) && (clipCoord.y <= 1.0);
    }

    private void computeObjCoord(Canvas3D canvas, Point2d winCoord, Point3d objCoord,
                                Transform3D localToImagePlate) {
	// Back transform this pt. from window to object coordinates
	// Assumes this method is ALWAYS called after computeWinCoord has been
	// called. computeWinCoord calculates the Vworld to Image Plate Xform.
	// This method simply uses it without recomputing it.

	canvas.getPixelLocationInImagePlate(winCoord.x, winCoord.y, objCoord.z,
					    objCoord);
	// Get image plate to object coord transform
	// inv(P x M)
	localToImagePlate.invert();
	localToImagePlate.transform(objCoord);
    }

    private Point3d computeWinCoord(Canvas3D canvas, RenderAtom ra,
				Point2d winCoord, Point3d objCoord,
                                Transform3D localToImagePlate) {
	// Get local to Vworld transform
	RenderMolecule rm = ra.renderMolecule;

	if (rm == null) {
	    // removeRenderAtom() may set ra.renderMolecule to null
	    // in RenderBin before this renderer thread run.
	    return null;
        }

        // MT safe issue: We can't reference ra.renderMolecule below since
        // RenderBin thread may set it to null anytime. Use rm instead.

	Transform3D lvw = rm.localToVworld[rm.localToVworldIndex[
				 NodeRetained.LAST_LOCAL_TO_VWORLD]];


        Point3d clipCoord3 = new Point3d();
        clipCoord3.set(objCoord);
        Point4d clipCoord4 = new Point4d();

        // Transform point from local coord. to clipping coord.
        lvw.transform(clipCoord3);
        canvas.vworldToEc.transform(clipCoord3);
        canvas.projTrans.transform(clipCoord3, clipCoord4);

        // clip check in Z
        if((clipCoord4.w <= 0.0) ||
                (clipCoord4.z > clipCoord4.w) || (-clipCoord4.z > clipCoord4.w)) {

            return null;
        }
        double invW = 1.0 / clipCoord4.w;

        clipCoord3.x = clipCoord4.x * invW;
        clipCoord3.y = clipCoord4.y * invW;
        clipCoord3.z = clipCoord4.z * invW;

	// Get Vworld to image plate Xform
	canvas.getLastVworldToImagePlate(localToImagePlate);

	// v' = vwip x lvw x v
	// 		where v' = transformed vertex,
	// 			  lvw = local to Vworld Xform
	//			  vwip = Vworld to Image plate Xform
	//			  v = vertex

	// Compute composite local to image plate Xform
	localToImagePlate.mul(lvw);

	// Transform the Raster's position from object to world coordinates
	localToImagePlate.transform(objCoord);


	// Get the window coordinates of this point
	canvas.getPixelLocationFromImagePlate(objCoord, winCoord);

        return clipCoord3;
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
