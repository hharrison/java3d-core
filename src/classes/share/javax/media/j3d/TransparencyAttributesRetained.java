/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
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
 * The TransparencyAttributes object defines all attributes affecting
 * transparency of the object.
 */
class TransparencyAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this TransparencyAttributes object changed.
    static final int MODE_CHANGED                   	= 0x01;
    static final int VALUE_CHANGED     	                = 0x02;
    static final int SRC_BLEND_FUNCTION_CHANGED     	= 0x04;
    static final int DST_BLEND_FUNCTION_CHANGED     	= 0x08;

    // Integer flag that contains bitset to indicate 
    // which field changed.
    int isDirty = 0xffff;

    // Transparency mode (alpha, screen_door)
    int		transparencyMode = TransparencyAttributes.NONE;
    float       transparency = 0.0f;

    // Transparency blend functions
    int srcBlendFunction = TransparencyAttributes.BLEND_SRC_ALPHA;
    int dstBlendFunction = TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA;

    /**
     * Sets the transparency mode for this
     * appearance component object.
     * @param transparencyMode the transparency mode to be used, one of
     * <code>NONE</code>, <code>FASTEST</code>, <code>NICEST</code>, 
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     */
    final void initTransparencyMode(int transparencyMode) {
	this.transparencyMode = transparencyMode;
    }

    /**
     * Sets the transparency mode for this
     * appearance component object and sends a message notifying
     * the interested structures of the change.
     * @param transparencyMode the transparency mode to be used, one of
     * <code>FASTEST</code>, <code>NICEST</code>,
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     */
    final void setTransparencyMode(int transparencyMode) {
	initTransparencyMode(transparencyMode);
	sendMessage(MODE_CHANGED, new Integer(transparencyMode));
    }

    /**
     * Gets the transparency mode for this
     * appearance component object.
     * @return transparencyMode the transparency mode
     */
    final int getTransparencyMode() {
	return transparencyMode;
    }

    /**
     * Sets this appearance's transparency.
     * @param transparency the appearance's transparency
     * in the range [0.0, 1.0] with 0.0 being
     * fully opaque and 1.0 being fully transparent
     */
    final void initTransparency(float transparency) {
	this.transparency = transparency;
    }

    /**
     * Sets this appearance's transparency and sends a message notifying
     * the interested structures of the change.
     * @param transparency the appearance's transparency
     * in the range [0.0, 1.0] with 0.0 being
     * fully opaque and 1.0 being fully transparent
     */
    final void setTransparency(float transparency) {
	initTransparency(transparency);
	sendMessage(VALUE_CHANGED, new Float(transparency));
    }

    /**
     * Retrieves this appearance's transparency.
     * @return the appearance's transparency
     */
    final float getTransparency() {
	return this.transparency;
    }

    /**
     * Sets the source blend function used in blended transparency
     * and antialiasing operations.  The source function specifies the
     * factor that is multiplied by the source color; this value is
     * added to the product of the destination factor and the
     * destination color.  The default source blend function is
     * <code>BLEND_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the source
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, or <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     */
    final void initSrcBlendFunction(int blendFunction) {
	this.srcBlendFunction = blendFunction;
    }


    /**
     * Sets the source blend function used in blended transparency
     * and antialiasing operations and sends a message notifying the
     * interested structures of the change. The source function specifies the
     * factor that is multiplied by the source color; this value is
     * added to the product of the destination factor and the
     * destination color.  The default source blend function is
     * <code>BLEND_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the source
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, or <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     */
    final void setSrcBlendFunction(int blendFunction) {
	initSrcBlendFunction(blendFunction);
	sendMessage(SRC_BLEND_FUNCTION_CHANGED, new Integer(blendFunction));
    }


    /**
     * Retrieves this appearance's source blend function.
     * @return the appearance's source blend function
     */
    final int getSrcBlendFunction() {
	return srcBlendFunction;
    }


    /**
     * Sets the destination blend function used in blended transparency
     * and antialiasing operations.  The destination function specifies the
     * factor that is multiplied by the destination color; this value is
     * added to the product of the source factor and the
     * source color.  The default destination blend function is
     * <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the destination
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, or <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     *
     */
    final void initDstBlendFunction(int blendFunction) {
	this.dstBlendFunction = blendFunction;
    }


    /**
     * Sets the destination blend function used in blended transparency
     * and antialiasing operations and sends a message notifying the 
     * interested structures of the change.  The destination function 
     * specifies the factor that is multiplied by the destination
     * color; this value is added to the product of the source factor
     * and the source color.  The default destination blend function is
     * <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the destination
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, or <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     */
    final void setDstBlendFunction(int blendFunction) {
	initDstBlendFunction(blendFunction);
	sendMessage(DST_BLEND_FUNCTION_CHANGED, new Integer(blendFunction));
    }


    /**
     * Retrieves this appearance's destination blend function.
     * @return the appearance's destination blend function
     */
    final int getDstBlendFunction() {
	return dstBlendFunction;
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
		TransparencyAttributesRetained mirrorTa 
		    = new TransparencyAttributesRetained();
		mirrorTa.source = source;
		mirrorTa.set(this);
		mirror = mirrorTa;

	    }
	} else {
	   ((TransparencyAttributesRetained) mirror).set(this);
	}
    }

    void updateNative(Context ctx,
		      float alpha, int geometryType, int polygonMode, 
		      boolean lineAA, 
		      boolean pointAA) { 
	Pipeline.getPipeline().updateTransparencyAttributes(ctx, alpha, geometryType, polygonMode, 
		     lineAA, pointAA, transparencyMode, 
		     srcBlendFunction, dstBlendFunction);
    }

   /**
    * Initializes a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void initMirrorObject() {
        ((TransparencyAttributesRetained)mirror).set(this);
    }

    /**
     * Update the "component" field of the mirror object with the 
     * given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	TransparencyAttributesRetained mirrorTa =
	    (TransparencyAttributesRetained) mirror;

	if ((component & MODE_CHANGED) != 0) {
	    mirrorTa.transparencyMode = ((Integer)value).intValue();
	}
	else if ((component & VALUE_CHANGED) != 0) {
	    mirrorTa.transparency = ((Float)value).floatValue();
	} 
	else if ((component & SRC_BLEND_FUNCTION_CHANGED) != 0) {
	    mirrorTa.srcBlendFunction = ((Integer) value).intValue();	    
	} 
	else if ((component & DST_BLEND_FUNCTION_CHANGED) != 0) {
	    mirrorTa.dstBlendFunction = ((Integer) value).intValue();	    
	}
    }


    boolean equivalent(TransparencyAttributesRetained tr) {
	return ((tr != null) &&
		(tr.transparencyMode == transparencyMode) &&
		(tr.transparency == transparency) &&
		(tr.srcBlendFunction == srcBlendFunction) &&
		(tr.dstBlendFunction == dstBlendFunction));
    }

    protected void set(TransparencyAttributesRetained transp) {
  	 super.set(transp);
	 transparencyMode = transp.transparencyMode;
	 transparency = transp.transparency;
	 srcBlendFunction = transp.srcBlendFunction;
	 dstBlendFunction = transp.dstBlendFunction;
    }



   final void sendMessage(int attrMask, Object attr) {

       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.TRANSPARENCYATTRIBUTES_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);


	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.TRANSPARENCYATTRIBUTES_CHANGED;
		
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
	if (bit == TransparencyAttributes.ALLOW_MODE_WRITE ||
	    bit == TransparencyAttributes.ALLOW_VALUE_WRITE||
	    bit == TransparencyAttributes.ALLOW_BLEND_FUNCTION_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }
    

    
}
