/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.ArrayList;

/**
 * The LineAttributesRetained object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 */
class LineAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this LineAttributesRetained object changed.
    static final int LINE_WIDTH_CHANGED      	        = 0x01;
    static final int LINE_PATTERN_CHANGED      	        = 0x02;
    static final int LINE_AA_CHANGED      	        = 0x04;
    static final int LINE_PATTERN_MASK_CHANGED 	        = 0x08;
    static final int LINE_PATTERN_SCALEFACTOR_CHANGED 	= 0x10;

    // Width, in pixels, of line primitives
    float lineWidth = 1.0f;

    // The line pattern to be used
    int linePattern = LineAttributes.PATTERN_SOLID;

    // Line antialiasing switch
    boolean lineAntialiasing = false;

    // user-defined line pattern mask
    int linePatternMask = 0xffff;

    // line mask pattern scale factor
    int linePatternScaleFactor = 1;

    /**
     * Sets the line width for this lineAttributes component object.
     * @param lineWidth the width, in pixels, of line primitives
     */
    final void initLineWidth(float lineWidth) {
	this.lineWidth = lineWidth;
    }

    /**
     * Sets the line width for this lineAttributes component object and sends a
     * message notifying the interested structures of the change.
     * @param lineWidth the width, in pixels, of line primitives
     */
    final void setLineWidth(float lineWidth) {
	initLineWidth(lineWidth);
	sendMessage(LINE_WIDTH_CHANGED, new Float(lineWidth));
    }

    /**
     * Gets the line width for this lineAttributes component object.
     * @return the width, in pixels, of line primitives
     */
    final float getLineWidth() {
	return lineWidth;
    }

    /**
     * Sets the line pattern for this lineAttributes component object
     * @param linePattern the line pattern to be used, one of:
     * PATTERN_SOLID, PATTERN_DASH, PATTERN_DOT, or PATTERN_DASH_DOT
     */
    final void initLinePattern(int linePattern) {
	this.linePattern = linePattern;
    }
    /**
     * Sets the line pattern for this lineAttributes component object
     * and sends a message notifying the interested structures of the change.
     * @param linePattern the line pattern to be used, one of:
     * PATTERN_SOLID, PATTERN_DASH, PATTERN_DOT, or PATTERN_DASH_DOT
     */
    final void setLinePattern(int linePattern) {
	initLinePattern(linePattern);
	sendMessage(LINE_PATTERN_CHANGED, new Integer(linePattern));
    }

    /**
     * Gets the line pattern for this lineAttributes component object.
     * @return the line pattern
     */
    final int getLinePattern() {
	return linePattern;
    }

    /**
     * Enables or disables line antialiasing
     * for this lineAttributes component object and sends a
     * message notifying the interested structures of the change.
     * @param state true or false to enable or disable line antialiasing
     */
    final void initLineAntialiasingEnable(boolean state) {
	lineAntialiasing = state;
    }
    /**
     * Enables or disables line antialiasing
     * for this lineAttributes component object and sends a
     * message notifying the interested structures of the change.
     * @param state true or false to enable or disable line antialiasing
     */
    final void setLineAntialiasingEnable(boolean state) {
	initLineAntialiasingEnable(state);
	sendMessage(LINE_AA_CHANGED,
		    (state ?  Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of the line antialiasing flag.
     * @return true if line antialiasing is enabled,
     * false if line antialiasing is disabled
     */
    final boolean getLineAntialiasingEnable() {
	return lineAntialiasing;
    }


   /**
     * Sets the pattern mask for this LineAttributes component object.
     * This is used when the linePattern attribute is set to
     * PATTERN_USER_DEFINED.
     * @param mask the line pattern mask to be used.
     */
    final void initPatternMask(int mask) {
	this.linePatternMask = mask;
    }

    /**
     * Sets the pattern mask for this LineAttributes component object
     * and sends a message notifying the interested structures of change.
     * This is used when the linePattern attribute is set to
     * PATTERN_USER_DEFINED.
     * @param mask the line pattern mask to be used.
     */
    final void setPatternMask(int mask) {
	initPatternMask(mask);
	sendMessage(LINE_PATTERN_MASK_CHANGED, new Integer(mask));
    }

   /**
     * Retrieves the pattern mask for this LineAttributes component object.
     * @return the user-defined pattern mask
     */
    final int getPatternMask() {
	return linePatternMask;
    }

   /**
     * Sets the pattern mask scale factor for this LineAttributes
     * component object. This is used when the linePattern attribute
     * is set to PATTERN_USER_DEFINED.
     * @param scaleFactor the scale factor of mask, clamp to [1, 15]
     */
    final void initPatternScaleFactor(int scaleFactor) {
	if (scaleFactor < 1) {
	    scaleFactor = 1;
	} else if (scaleFactor > 15) {
	    scaleFactor = 15;
	}
	this.linePatternScaleFactor = scaleFactor;
    }

    /**
     * Sets the pattern mask scale factor for this LineAttributes
     * component object and sends a message notifying the interested
     * structures of change. This is used when the linePattern
     * attribute is set to PATTERN_USER_DEFINED.
     * @param scaleFactor  the scale factor of mask, clamp to [1, 15]
     */
    final void setPatternScaleFactor(int scaleFactor) {
	initPatternScaleFactor(scaleFactor);
	sendMessage(LINE_PATTERN_SCALEFACTOR_CHANGED, new Integer(scaleFactor));
    }

   /**
     * Retrieves the pattern scale factor for this LineAttributes
     * component object.
     * @return the pattern mask scale factor
     */
    final int getPatternScaleFactor() {
	return linePatternScaleFactor;
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
	    }  else {
		LineAttributesRetained mirrorLa = new LineAttributesRetained();
		mirrorLa.source = source;
		mirrorLa.set(this);
		mirror = mirrorLa;
	    }
	} else {
	    ((LineAttributesRetained) mirror).set(this);
	}
    }


    /**
     * This method updates the native context.
     */
    void updateNative(Context ctx) {
        Pipeline.getPipeline().updateLineAttributes(ctx,
                lineWidth, linePattern, linePatternMask,
                linePatternScaleFactor, lineAntialiasing);
    }


    /**
     * Initializes a mirror object, point the mirror object to the retained
     * object if the object is not editable
     */
    synchronized void initMirrorObject() {
	((LineAttributesRetained)mirror).set(this);
    }

    /** Update the "component" field of the mirror object with the
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	LineAttributesRetained mirrorLa = (LineAttributesRetained) mirror;

	if ((component & LINE_WIDTH_CHANGED) != 0) {
	    mirrorLa.lineWidth = ((Float)value).floatValue();
	}
	else if ((component & LINE_PATTERN_CHANGED) != 0) {
	    mirrorLa.linePattern = ((Integer)value).intValue();
	}
	else if ((component & LINE_AA_CHANGED) != 0) {
	    mirrorLa.lineAntialiasing = ((Boolean)value).booleanValue();
	}
	else if ((component & LINE_PATTERN_MASK_CHANGED) != 0) {
	    mirrorLa.linePatternMask = ((Integer)value).intValue();
	}
	else if ((component & LINE_PATTERN_SCALEFACTOR_CHANGED) != 0)
	    {
	    mirrorLa.linePatternScaleFactor = ((Integer)value).intValue();
	}
    }


    boolean equivalent(LineAttributesRetained lr) {
        return ((lr != null) &&
		(lineWidth == lr.lineWidth) &&
		(linePattern == lr.linePattern) &&
		(lineAntialiasing == lr.lineAntialiasing) &&
		(linePatternMask == lr.linePatternMask) &&
		(linePatternScaleFactor == lr.linePatternScaleFactor));

    }

    protected void set(LineAttributesRetained lr) {
	super.set(lr);
	lineWidth = lr.lineWidth;
	linePattern = lr.linePattern;
	linePatternScaleFactor = lr.linePatternScaleFactor;
	linePatternMask = lr.linePatternMask;
	lineAntialiasing = lr.lineAntialiasing;
     }

     final void sendMessage(int attrMask, Object attr) {
	ArrayList<VirtualUniverse> univList = new ArrayList<VirtualUniverse>();
	ArrayList<ArrayList<GeometryAtom>> gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.LINEATTRIBUTES_CHANGED;
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
	    createMessage.type = J3dMessage.LINEATTRIBUTES_CHANGED;

		createMessage.universe = univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

		ArrayList<GeometryAtom> gL = gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;

	    VirtualUniverse.mc.processMessage(createMessage);
	}


    }
    void handleFrequencyChange(int bit) {
	if (bit == LineAttributes.ALLOW_WIDTH_WRITE ||
	    bit == LineAttributes.ALLOW_PATTERN_WRITE||
	    bit == LineAttributes.ALLOW_ANTIALIASING_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }

}
