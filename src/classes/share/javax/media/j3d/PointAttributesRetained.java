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

import java.util.ArrayList;

/**
 * The PointAttributesRetained object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 */
class PointAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this LineAttributesRetained object changed.
    static final int POINT_SIZE_CHANGED      	= 0x01;
    static final int POINT_AA_CHANGED      	= 0x02;

    // Size, in pixels, of point primitives
    float pointSize = 1.0f;

    // Point antialiasing switch
    boolean pointAntialiasing = false;


    /**
     * Sets the point size for this appearance component object.
     * @param pointSize the size, in pixels, of point primitives
     */
    final void initPointSize(float pointSize) {
	this.pointSize = pointSize;
    }

    /**
     * Sets the point size for this appearance component object and sends a 
     * message notifying the interested structures of the change.
     * @param pointSize the size, in pixels, of point primitives
     */
    final void setPointSize(float pointSize) {
	initPointSize(pointSize);
	sendMessage(POINT_SIZE_CHANGED, new Float(pointSize));
    }

    /**
     * Gets the point size for this appearance component object.
     * @return the size, in pixels, of point primitives
     */
    final float getPointSize() {
	return pointSize;
    }

    /**
     * Enables or disables point antialiasing
     * for this appearance component object.
     * @param state true or false to enable or disable point antialiasing
     */
    final void initPointAntialiasingEnable(boolean state) {
	pointAntialiasing = state;
    }

    /**
     * Enables or disables point antialiasing
     * for this appearance component object and sends a 
     * message notifying the interested structures of the change.
     * @param state true or false to enable or disable point antialiasing
     */
    final void setPointAntialiasingEnable(boolean state) {
	initPointAntialiasingEnable(pointAntialiasing);
	sendMessage(POINT_AA_CHANGED, 
		    (state ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of the point antialiasing flag.
     * @return true if point antialiasing is enabled,
     * false if point antialiasing is disabled
     */
    final boolean getPointAntialiasingEnable() {
	return pointAntialiasing;
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
		PointAttributesRetained mirrorPa
		    = new PointAttributesRetained();
		mirrorPa.set(this);
		mirrorPa.source = source;
		mirror = mirrorPa;
	    }
	} else {
	   ((PointAttributesRetained) mirror).set(this);
	}
    }

    /**
     * Update the native context
     */
    native void updateNative(long ctx, float pointSize, boolean pointAntialiasing);

    /**
     * Update the native context
     */
    void updateNative(long ctx) {
	updateNative(ctx, pointSize, pointAntialiasing);
    }


    /**
     * Initializes a mirror object, point the mirror object to the retained
     * object if the object is not editable
     */
    synchronized void initMirrorObject() {
	((PointAttributesRetained)mirror).set(this);
    }


    /**
     * Update the "component" field of the mirror object with the 
     * given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	PointAttributesRetained mirrorPa = (PointAttributesRetained) mirror;

	if ((component & POINT_SIZE_CHANGED) != 0) {
	    mirrorPa.pointSize = ((Float)value).floatValue();
	}
	else if ((component & POINT_AA_CHANGED) != 0) {
	    mirrorPa.pointAntialiasing = ((Boolean)value).booleanValue();
	}
    }

    boolean equivalent(PointAttributesRetained pr) {
	return ((pr != null) &&
		(pr.pointSize == pointSize) && 
		(pr.pointAntialiasing == pointAntialiasing));
    }


     protected void set(PointAttributesRetained pr) {
	 super.set(pr);
	 pointSize = pr.pointSize;
	 pointAntialiasing = pr.pointAntialiasing;
     }

    
    final void sendMessage(int attrMask, Object attr) {
       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.POINTATTRIBUTES_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);


	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.POINTATTRIBUTES_CHANGED;
		
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
	if (bit == PointAttributes.ALLOW_SIZE_WRITE ||
	    bit == PointAttributes.ALLOW_ANTIALIASING_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }
    
}
