/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
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
 * The RenderingAttributes object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 */
class RenderingAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this RenderingAttributes object changed.
    static final int DEPTH_ENABLE      	        = 0x01;

    static final int DEPTH_WRITE_ENABLE      	= 0x02;

    static final int ALPHA_TEST_VALUE      	= 0x04;

    static final int ALPHA_TEST_FUNC      	= 0x08;

    static final int VISIBLE               	= 0x10;

    static final int IGNORE_VCOLOR             	= 0x20;

    static final int RASTER_OP_ENABLE		= 0x40;

    static final int RASTER_OP_VALUE		= 0x80;


    // depth buffer Enable for hidden surface removal
    boolean depthBufferEnable = true;

    boolean depthBufferWriteEnable = true;
    
    float alphaTestValue = 0.0f;
    
    int alphaTestFunction = RenderingAttributes.ALWAYS;

    boolean visible  = true;

    boolean ignoreVertexColors = false;

    // raster operation
    boolean rasterOpEnable = false;
    int rasterOp = RenderingAttributes.ROP_COPY;
    
    // depth buffer comparison function. Used by multi-texturing only
    static final int LESS = 0;
    static final int LEQUAL = 1;

    /**
     * Sets the visibility flag for this RenderingAttributes component object. 
     * @param visible true or false to enable or disable visibility
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see View#setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    final void initVisible(boolean state){
	visible = state;
    }
    
    /**
     * Sets the visibility flag for this RenderingAttributes
     * component object.  Invisible objects are not rendered (subject to
     * the visibility policy for the current view), but they can be picked
     * or collided with.
     * @param visible true or false to enable or disable visibility
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see View#setVisibilityPolicy
     *
     * @since Java 3D 1.2
     */
    final void setVisible(boolean  state){	
	// Optimize : If new state equal to current state, should I simply return ?
	// Is it safe ?
	initVisible(state);

	// Need to call sendMessage twice. Not an efficient approach, but
	// it simplified code logic and speed up the common case; where
	// perUniv is false.


	sendMessage(VISIBLE, (state ? Boolean.TRUE: Boolean.FALSE));

    }
    
    /**
     * Retrieves the visibility flag for this RenderingAttributes object.
     * @return true if the object is visible; false
     * if the object is invisible.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    final boolean getVisible() {
   	return visible;
    }

    final void initIgnoreVertexColors(boolean state) {
	ignoreVertexColors = state;
    }

    final void setIgnoreVertexColors(boolean state) {
	initIgnoreVertexColors(state);
	sendMessage(IGNORE_VCOLOR,
		    (state ? Boolean.TRUE: Boolean.FALSE));
    }

    final boolean getIgnoreVertexColors() {
	return ignoreVertexColors;
    }

    /**
     * Enables or disables depth buffer mode for this RenderAttributes
     * component object.
     * @param state true or false to enable or disable depth buffer mode
     */
    final void initDepthBufferEnable(boolean state){
	depthBufferEnable = state;
    }

    /**
     * Enables or disables depth buffer mode for this RenderAttributes
     * component object and sends a 
     * message notifying the interested structures of the change.
     * @param state true or false to enable or disable depth buffer mode
     */
    final void setDepthBufferEnable(boolean state){
	initDepthBufferEnable(state);
	sendMessage(DEPTH_ENABLE, (state ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of zBuffer Enable flag
     * @return true if depth buffer mode is enabled, false
     * if depth buffer mode is disabled
     */
    final boolean getDepthBufferEnable(){
	return depthBufferEnable;
    }

    /**
     * Enables or disables writing the depth buffer for this object.
     * During the transparent rendering pass,
     * this attribute can be overridden by
     * the depthBufferFreezeTransparent attribute in the View object.
     * @param state true or false to enable or disable depth buffer Write mode
     * @see View#setDepthBufferFreezeTransparent
     */
    final void initDepthBufferWriteEnable(boolean state){
	depthBufferWriteEnable = state;
    }

    /**
     * Enables or disables writing the depth buffer for this object and sends
     * a message notifying the interested structures of the change.
     * During the transparent rendering pass,
     * this attribute can be overridden by
     * the depthBufferFreezeTransparent attribute in the View object.
     * @param state true or false to enable or disable depth buffer Write mode
     * @see View#setDepthBufferFreezeTransparent
     */
    final void setDepthBufferWriteEnable(boolean state){

	initDepthBufferWriteEnable(state);
	sendMessage(DEPTH_WRITE_ENABLE, (state ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of Depth Buffer Write Enable flag
     * @return true if depth buffer is writable, false
     * if depth buffer is read-only
     */
    final boolean getDepthBufferWriteEnable(){
	return depthBufferWriteEnable;
    }

    /**
     * Set alpha test value used by alpha test function.  This value is
     * compared to the alpha value of each rendered pixel.
     * @param value the alpha value
     */
    final void initAlphaTestValue(float value){
	alphaTestValue = value;
    }
    /**
     * Set alpha test value used by alpha test function and sends a 
     * message notifying the interested structures of the change.
     * This value is compared to the alpha value of each rendered pixel.
     * @param value the alpha value
     */
    final void setAlphaTestValue(float value){

	initAlphaTestValue(value);
	sendMessage(ALPHA_TEST_VALUE,  new Float(value));
    }

    /**
     * Retrieves the alpha test value.
     * @return the alpha test value.
     */
    final float getAlphaTestValue(){
	return alphaTestValue;
    }


    /**
     * Set alpha test function.  This function is used to compare the
     * alpha test value with each per-pixel alpha value.  If the test
     * passes, then the pixel is written otherwise the pixel is not
     * written.
     * @param function the new alpha test function.  One of:
     * ALWAYS, NEVER, EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER,
     * GREATER_OR_EQUAL.
     */
    final void initAlphaTestFunction(int function){
	alphaTestFunction = function;
    }


    /**
     * Set alpha test function and sends a 
     * message notifying the interested structures of the change.
     * This function is used to compare the
     * alpha test value with each per-pixel alpha value.  If the test
     * passes, then the pixel is written otherwise the pixel is not
     * written.
     * @param function the new alpha test function.  One of:
     * ALWAYS, NEVER, EQUAL, NOT_EQUAL, LESS, LESS_OR_EQUAL, GREATER,
     * GREATER_OR_EQUAL.
     */
    final void setAlphaTestFunction(int function){
	
	initAlphaTestFunction(function);
	sendMessage(ALPHA_TEST_FUNC, new Integer(function));
    }

    /**
     * Retrieves current alpha test function.
     * @return the current alpha test function
     */
    final int getAlphaTestFunction(){
	return alphaTestFunction;
    }


    /**
     * Initialize the raster op enable flag
     */
    final void initRasterOpEnable(boolean flag) {
	rasterOpEnable = flag;
    }

    /**
     * Set the raster op enable flag
     */
    final void setRasterOpEnable(boolean flag) {
	initRasterOpEnable(flag);
	sendMessage(RASTER_OP_ENABLE, new Boolean(flag));
    }

    /**
     * Retrieves the current raster op enable flag.
     */
    final boolean getRasterOpEnable() {
	return rasterOpEnable;
    }

    /**
     * Initialize the raster op value
     */
    final void initRasterOp(int op) {
	rasterOp = op;
    }

    /**
     * Set the raster op value
     */
    final void setRasterOp(int op) {
	initRasterOp(op);
	sendMessage(RASTER_OP_VALUE, new Integer(op));
    }

    /**
     * Retrieves the current raster op value.
     */
    final int getRasterOp() {
	return rasterOp;
    }


    
    /**
     * Updates the native context.
     */
    native void updateNative(long ctx,
			     boolean depthBufferWriteEnableOverride,
                             boolean depthBufferEnableOverride,
			     boolean depthBufferEnable, 
			     boolean depthBufferWriteEnable,
			     float alphaTestValue, int alphaTestFunction,
			     boolean ignoreVertexColors,
			     boolean rasterOpEnable, int rasterOp);

    /**
     * Updates the native context.
     */
    void updateNative(long ctx,
		      boolean depthBufferWriteEnableOverride,
                      boolean depthBufferEnableOverride) {
	updateNative(ctx, 
		     depthBufferWriteEnableOverride, depthBufferEnableOverride,
		     depthBufferEnable, depthBufferWriteEnable, alphaTestValue, 
		     alphaTestFunction, ignoreVertexColors,
		     rasterOpEnable, rasterOp);
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
	    }
	    else {
		RenderingAttributesRetained mirrorRa  
		    = new RenderingAttributesRetained();
		mirrorRa.set(this);
                mirrorRa.source = source;
		mirror = mirrorRa;
	    }
	} else {
	    ((RenderingAttributesRetained) mirror).set(this);	    
	}
    }

   /**
    * Initializes a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void initMirrorObject() {
	((RenderingAttributesRetained)mirror).set(this);
    }

    /**
     * Update the "component" field of the mirror object with the 
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {
	RenderingAttributesRetained mirrorRa = (RenderingAttributesRetained)mirror;
      
	if ((component & DEPTH_ENABLE) != 0) {
	    mirrorRa.depthBufferEnable = ((Boolean)value).booleanValue();
	}
	else if ((component & DEPTH_WRITE_ENABLE) != 0) {
	    mirrorRa.depthBufferWriteEnable = ((Boolean)value).booleanValue();
	}
	else if ((component & ALPHA_TEST_VALUE) != 0) {
	    mirrorRa.alphaTestValue = ((Float)value).floatValue();
	}
	else if ((component & ALPHA_TEST_FUNC) != 0) {
	    mirrorRa.alphaTestFunction = ((Integer)value).intValue();
	}
	else if ((component & VISIBLE) != 0) {
	    mirrorRa.visible = (((Boolean)value).booleanValue());
	}
	else if ((component & IGNORE_VCOLOR) != 0) {
	    mirrorRa.ignoreVertexColors = (((Boolean)value).booleanValue());
	}	
	else if ((component & RASTER_OP_ENABLE) != 0) {
	    mirrorRa.rasterOpEnable = (((Boolean)value).booleanValue());
	}	
	else if ((component & RASTER_OP_VALUE) != 0) {
	    mirrorRa.rasterOp = (((Integer)value).intValue());
	}	
    }

    boolean equivalent(RenderingAttributesRetained rr) {
	return (this == rr) ||
	       ((rr != null) &&
		(rr.depthBufferEnable  == depthBufferEnable) &&
		(rr.depthBufferWriteEnable == depthBufferWriteEnable) && 
		(rr.alphaTestValue == alphaTestValue) &&
		(rr.alphaTestFunction == alphaTestFunction) &&
		(rr.visible == visible) &&
		(rr.ignoreVertexColors == ignoreVertexColors) &&
		(rr.rasterOpEnable == rasterOpEnable) &&
		(rr.rasterOp == rasterOp));
    }

    protected void set(RenderingAttributesRetained ra) {
	super.set(ra);
	depthBufferEnable  = ra.depthBufferEnable;
	depthBufferWriteEnable = ra.depthBufferWriteEnable;
	alphaTestValue = ra.alphaTestValue;
	alphaTestFunction = ra.alphaTestFunction;
	visible = ra.visible;
	ignoreVertexColors = ra.ignoreVertexColors;
	rasterOpEnable = ra.rasterOpEnable;
	rasterOp = ra.rasterOp;
    }
    
    final void sendMessage(int attrMask, Object attr) {

	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.RENDERINGATTRIBUTES_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	//	System.out.println("changedFreqent1 = "+changedFrequent);
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    if (attrMask == VISIBLE)
	        createMessage.threads |= J3dThread.UPDATE_GEOMETRY;
	    createMessage.type = J3dMessage.RENDERINGATTRIBUTES_CHANGED;
		
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

	    ArrayList gL = (ArrayList)gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;  
	    
	    VirtualUniverse.mc.processMessage(createMessage);
	}

    }


    void handleFrequencyChange(int bit) {
	int mask = 0;
	
	if (bit == RenderingAttributes.ALLOW_ALPHA_TEST_VALUE_WRITE)
	    mask = ALPHA_TEST_VALUE;
	if( bit == RenderingAttributes.ALLOW_ALPHA_TEST_FUNCTION_WRITE)
	    mask = ALPHA_TEST_FUNC;
	if(bit == RenderingAttributes.ALLOW_VISIBLE_WRITE)
	    mask = VISIBLE;
	if (bit == RenderingAttributes.ALLOW_IGNORE_VERTEX_COLORS_WRITE)
	    mask = IGNORE_VCOLOR;
	if(bit == RenderingAttributes.ALLOW_RASTER_OP_WRITE)
	    mask = RASTER_OP_ENABLE;
	if(bit == RenderingAttributes.ALLOW_DEPTH_ENABLE_WRITE)
	    mask = DEPTH_WRITE_ENABLE;
	if (mask != 0)
	    setFrequencyChangeMask(bit, mask);
	//	System.out.println("changedFreqent2 = "+changedFrequent);
    }
    
}
