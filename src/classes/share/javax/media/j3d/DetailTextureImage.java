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

import javax.vecmath.*;
import java.util.*;


class DetailTextureImage extends Object {

    final static int NFORMAT = 7;	// number of texture format

    int objectIds[];			// texture object id, one per format
    int refCount[];			// texture bin reference count,
					// to keep track of if the texture
					// object id is still being referenced
					// by an active texture. If it
					// goes to 0 for a particular format,
					// the associated texture object 
					// will be destroyed.

    int resourceCreationMask[];		// one creation mask per format

    ImageComponent2DRetained image = null;	// the image itself

    Object resourceLock = new Object();

    DetailTextureImage(ImageComponent2DRetained img) {
	image = img;
    }

    native void bindTexture(long ctx, int objectId);

    native void updateTextureImage(long ctx,
                                int numLevels, int level,
                                int format, int storedFormat,
                                int width, int height, 
				int boundaryWidth, byte[] data);


    synchronized void incTextureBinRefCount(int format, TextureBin tb) {
	if (refCount == null) {
	    refCount = new int[NFORMAT];
	}
	if (resourceCreationMask == null) {
	    resourceCreationMask = new int[NFORMAT];
	}
	refCount[format]++;

	if (image != null && 
	       (image.isByReference() ||
		image.source.getCapability(ImageComponent.ALLOW_IMAGE_WRITE))) {
	    tb.renderBin.addNodeComponent(image);
	}
    }

    synchronized void decTextureBinRefCount(int format, TextureBin tb) {

	if (refCount != null) {
	    refCount[format]--;
	}
	if (image != null && 
	       (image.isByReference() ||
		image.source.getCapability(ImageComponent.ALLOW_IMAGE_WRITE))) {
	    tb.renderBin.removeNodeComponent(image);
	}
    }


    synchronized void freeDetailTextureId(int id, int bitMask) {
	synchronized(resourceLock) {
	    if (objectIds != null) {
		for (int i=0; i < resourceCreationMask.length; i++) {
		    resourceCreationMask[i] &= ~bitMask;
		    if (resourceCreationMask[i] == 0) {		    
			if (objectIds[i] == id) {
			    objectIds[i] = -1;
			    VirtualUniverse.mc.freeTexture2DId(id);
			    break;
			}
		    }
		}
	    }

	}
    }

    synchronized void freeTextureId(int format, int id) {
	synchronized(resourceLock) {
	    if ((objectIds != null) && (objectIds[format] == id)) {
		objectIds[format] = -1;
		VirtualUniverse.mc.freeTexture2DId(objectIds[format]);
	    }
	}
    }

    protected void finalize() {
	if (objectIds != null) {
	    // memory not yet free
	    // send a message to the request renderer
	    synchronized (VirtualUniverse.mc.contextCreationLock) {
		boolean found = false;
		for (int i=0; i < objectIds.length; i++) {
		    if (objectIds[i] > 0) {
			for (Enumeration e = Screen3D.deviceRendererMap.elements();
			     e.hasMoreElements(); ) {
			    Renderer rdr = (Renderer) e.nextElement();	  
			    J3dMessage renderMessage = VirtualUniverse.mc.getMessage();
			    renderMessage.threads = J3dThread.RENDER_THREAD;
			    renderMessage.type = J3dMessage.RENDER_IMMEDIATE;
			    renderMessage.universe = null;
			    renderMessage.view = null;
			    renderMessage.args[0] = null;
			    renderMessage.args[1] = new Integer(objectIds[i]);
			    renderMessage.args[2] = "2D";
			    rdr.rendererStructure.addMessage(renderMessage);
			}
			objectIds[i] = -1;
			found = true;
		    }
		}
		if (found) {
		    VirtualUniverse.mc.setWorkForRequestRenderer();
		}
	    }
	}
    }

    void notifyImageComponentImageChanged(ImageComponentRetained image,
						Object value) {
	if (resourceCreationMask != null) {
	    synchronized(resourceLock) {
	        for (int i = 0; i < NFORMAT; i++) {
	            resourceCreationMask[i] = 0;
	        }
	    }
	}
    }

    void bindTexture(Canvas3D cv, int format) {
	synchronized(resourceLock) {
	    if (objectIds == null) {
		objectIds = new int[NFORMAT];
		for (int i = 0; i < NFORMAT; i++) {
		    objectIds[i] = -1;
		}
	    }
	    
	    if (objectIds[format] == -1) {
		objectIds[format] = VirtualUniverse.mc.getTexture2DId();
	    }
	    cv.addTextureResource(objectIds[format], this);
	}

 	bindTexture(cv.ctx, objectIds[format]);
    }


    void updateNative(Canvas3D cv, int format) {
        if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_DETAIL) == 0) {
	    return;
	}

        boolean reloadTexture = false;

	// bind the detail texture

	bindTexture(cv, format);

	if (cv.useSharedCtx && cv.screen.renderer.sharedCtx != 0) {
	    if ((resourceCreationMask[format] & cv.screen.renderer.rendererBit)
			== 0) {
		reloadTexture = true;
		cv.makeCtxCurrent(cv.screen.renderer.sharedCtx);
		bindTexture(cv, format);
	    }
	} else {
	    if ((resourceCreationMask[format] & cv.canvasBit) == 0) {
		reloadTexture = true;
	    }
	}

	// No D3D support yet

        if (reloadTexture) {

	    updateTextureImage(cv.ctx, 1, 0, format, image.storedYupFormat,
			image.width, image.height, 0, image.imageYup);


	    // Rendered image

	}

        if (cv.useSharedCtx) {
	    cv.makeCtxCurrent(cv.ctx);
	    synchronized(resourceLock) {
		resourceCreationMask[format] |= cv.screen.renderer.rendererBit;
	    }
	} else {
	    synchronized(resourceLock) {
		resourceCreationMask[format] |= cv.canvasBit;
	    }
 	}
    }
}



