/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;

/**
 * The PolygonAttributes object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 */
class PolygonAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this LineAttributesRetained object changed.
    static final int POLYGON_MODE_CHANGED      	            = 0x01;
    static final int POLYGON_CULL_CHANGED      	            = 0x02;
    static final int POLYGON_OFFSET_CHANGED                 = 0x04;
    static final int POLYGON_BACKFACENORMALFLIP_CHANGED     = 0x08;
    static final int POLYGON_OFFSETFACTOR_CHANGED           = 0x10;

    // Polygon rasterization mode (point, line, fill)
    int polygonMode = PolygonAttributes.POLYGON_FILL;

    // Face culling mode
    int cullFace = PolygonAttributes.CULL_BACK;

    // back face normal flip flag
    boolean backFaceNormalFlip = false;

    // constant polygon offset
    float polygonOffset;

    // polygon offset factor
    float polygonOffsetFactor;

    /**
     * Sets the face culling for this
     * appearance component object,
     * @param cullFace the face to be culled, one of:
     * CULL_NONE, CULL_FRONT, or CULL_BACK
     */
    final void initCullFace(int cullFace) {
	this.cullFace = cullFace;
    }

    /**
     * Sets the face culling for this
     * appearance component object and sends a message notifying
     * the interested structures of the change.
     * @param cullFace the face to be culled, one of:
     * CULL_NONE, CULL_FRONT, or CULL_BACK
     */
    final void setCullFace(int cullFace) {
	initCullFace(cullFace);
	sendMessage(POLYGON_CULL_CHANGED, new Integer(cullFace));
    }

    /**
     * Gets the face culling for this
     * appearance component object.
     * @return the face to be culled
     */
    final int getCullFace() {
	return cullFace;
    }

    /**
     * Sets the back face normal flip flag to the specified value
     * This flag indicates whether vertex normals of back facing polygons
     * should be flipped (negated) prior to lighting.  When this flag
     * is set to true and back face culling is disabled, polygons are
     * rendered as if the polygon had two sides with opposing normals.
     * This feature is disabled by default
     * @param backFaceNormalFlip the back face normal flip flag
     */
    final void initBackFaceNormalFlip(boolean backFaceNormalFlip) {
	this.backFaceNormalFlip = backFaceNormalFlip;
    }

    /**
     * Sets the back face normal flip flag to the specified value
     * and sends a message notifying
     * the interested structures of the change.
     * This flag indicates whether vertex normals of back facing polygons
     * should be flipped (negated) prior to lighting.  When this flag
     * is set to true and back face culling is disabled, polygons are
     * rendered as if the polygon had two sides with opposing normals.
     * This feature is disabled by default
     * @param backFaceNormalFlip the back face normal flip flag
     */
    final void setBackFaceNormalFlip(boolean backFaceNormalFlip) {
	initBackFaceNormalFlip(backFaceNormalFlip);
	sendMessage(POLYGON_BACKFACENORMALFLIP_CHANGED,
		    (backFaceNormalFlip ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Gets the back face normal flip flag.
     * @return the back face normal flip flag
     */
    final boolean getBackFaceNormalFlip() {
        return backFaceNormalFlip;
    }

    /**
     * Sets the polygon rasterization mode for this
     * appearance component object.
     * @param polygonMode the polygon rasterization mode to be used; one of
     * POLYGON_FILL, POLYGON_LINE, or POLYGON_POINT
     */
    final void initPolygonMode(int polygonMode) {
	this.polygonMode = polygonMode;
    }

    /**
     * Sets the polygon rasterization mode for this
     * appearance component object and sends a message notifying
     * the interested structures of the change.
     * @param polygonMode the polygon rasterization mode to be used; one of
     * POLYGON_FILL, POLYGON_LINE, or POLYGON_POINT
     */
    final void setPolygonMode(int polygonMode) {
	initPolygonMode(polygonMode);
	sendMessage(POLYGON_MODE_CHANGED, new Integer(polygonMode));
    }

    /**
     * Gets the polygon rasterization mode for this
     * appearance component object.
     * @return polygonMode the polygon rasterization mode
     */
    final int getPolygonMode() {
	return polygonMode;
    }

    /**
     * Sets the polygon offset to the specified value and sends a 
     * message notifying the interested structures of the change.
     * This screen space offset is added to the final, device 
     * coordinate Z value of polygon primitives.
     * @param polygonOffset the polygon offset
     */
    final void initPolygonOffset(float polygonOffset) {
	this.polygonOffset = polygonOffset;
    }

    /**
     * Sets the polygon offset to the specified value and sends a 
     * message notifying the interested structures of the change.
     * This screen space offset is added to the final, device 
     * coordinate Z value of polygon primitives.
     * @param polygonOffset the polygon offset
     */
    final void setPolygonOffset(float polygonOffset) {
	initPolygonOffset(polygonOffset);
	sendMessage(POLYGON_OFFSET_CHANGED, new Float(polygonOffset));
    }


    /**
     * Gets the polygon offset.
     * @return polygonOffset the polygon offset
     */
    final float getPolygonOffset() {
	return polygonOffset;
    }


    /**
     * Sets the polygon offset factor to the specified value and sends a 
     * message notifying the interested structures of the change.
     * This factor is multiplied by the slope of the polygon, and
     * then added to the final, device coordinate Z value of polygon
     * primitives.
     * @param polygonOffsetFactor the polygon offset factor
     */
    final void initPolygonOffsetFactor(float polygonOffsetFactor) {
	this.polygonOffsetFactor = polygonOffsetFactor;
    }

    /**
     * Sets the polygon offset factor to the specified value and sends a 
     * message notifying the interested structures of the change.
     * This factor is multiplied by the slope of the polygon, and
     * then added to the final, device coordinate Z value of polygon
     * primitives.
     * @param polygonOffsetFactor the polygon offset
     */
    final void setPolygonOffsetFactor(float polygonOffsetFactor) {
	initPolygonOffsetFactor(polygonOffsetFactor);
	sendMessage(POLYGON_OFFSETFACTOR_CHANGED, 
		    new Float(polygonOffsetFactor));
    }


    /**
     * Gets the polygon offset factor.
     * @return polygonOffset the polygon offset factor
     */
    final float getPolygonOffsetFactor() {
	return polygonOffsetFactor;
    }

   /**
    * Creates and initializes a mirror object, point the mirror object 
    * to the retained object if the object is not editable
    */
    synchronized void createMirrorObject() {
	if (mirror == null) {
	    // Check the capability bits and let the mirror object
	    // point to itself if is not editable
	    if (isStatic()) {
		mirror = this;
	    } else {
                PolygonAttributesRetained mirrorPa = new PolygonAttributesRetained();
		mirrorPa.set(this);
		mirrorPa.source = source;
		mirror = mirrorPa;
	    } 
	} else {
	    ((PolygonAttributesRetained) mirror).set(this);	    
	}
    }


    /**
     * Updates the native context
     */
    void updateNative(Context ctx) {
        Pipeline.getPipeline().updatePolygonAttributes(ctx,
                polygonMode, cullFace, backFaceNormalFlip,
                polygonOffset, polygonOffsetFactor);
    }

   /**
    * Initializes a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void initMirrorObject() {
	((PolygonAttributesRetained) mirror).set(this);
    }

    /**
     * Update the "component" field of the mirror object with the 
     * given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

      PolygonAttributesRetained mirrorPa = (PolygonAttributesRetained) mirror;

      if ((component & POLYGON_MODE_CHANGED) != 0) {
	  mirrorPa.polygonMode = ((Integer)value).intValue();
      }
      else if ((component & POLYGON_CULL_CHANGED) != 0) {
	  mirrorPa.cullFace = ((Integer)value).intValue();
      }
      else if ((component & POLYGON_BACKFACENORMALFLIP_CHANGED) != 0) {
	  mirrorPa.backFaceNormalFlip = ((Boolean)value).booleanValue();
      }
      else if ((component & POLYGON_OFFSET_CHANGED) != 0) {
	  mirrorPa.polygonOffset = ((Float)value).floatValue();
      } 
      else if ((component & POLYGON_OFFSETFACTOR_CHANGED) != 0) {
	  mirrorPa.polygonOffsetFactor = ((Float) value).floatValue();
      }
    }
  

    boolean equivalent(PolygonAttributesRetained pr) {
	return ((pr != null) &&
		(pr.cullFace == cullFace) &&
		(pr.backFaceNormalFlip == backFaceNormalFlip) &&
		(pr.polygonOffset == polygonOffset) &&
		(pr.polygonMode == polygonMode) &&
		(pr.polygonOffsetFactor == polygonOffsetFactor));
    }

    protected void set(PolygonAttributesRetained pr) {
	super.set(pr);
	cullFace = pr.cullFace;
	backFaceNormalFlip = pr.backFaceNormalFlip;
	polygonMode = pr.polygonMode;
	polygonOffset = pr.polygonOffset;
	polygonOffsetFactor = pr.polygonOffsetFactor;
    }
    
    final void sendMessage(int attrMask, Object attr) {
       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.POLYGONATTRIBUTES_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	// System.err.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.POLYGONATTRIBUTES_CHANGED;
		
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    
	    VirtualUniverse.mc.processMessage(createMessage);
	}


    }
    void handleFrequencyChange(int bit) {
	if (bit == PolygonAttributes.ALLOW_CULL_FACE_WRITE ||
	    bit == PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE||
	    bit == PolygonAttributes.ALLOW_MODE_WRITE ||
	    bit == PolygonAttributes.ALLOW_OFFSET_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }    

}
