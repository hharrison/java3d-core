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

import javax.vecmath.Color3f;

/**
 * The ColoringAttributesRetained object defines attributes that apply to
 * to coloring mapping.
 */
class ColoringAttributesRetained extends NodeComponentRetained {
    // A list of pre-defined bits to indicate which component
    // in this ColoringAttributes object changed.
    static final int COLOR_CHANGED      	= 0x01;
    static final int SHADE_MODEL_CHANGED      	= 0x02;

    // Intrinsic color used when lighting is disabled or when
    // material is null
    Color3f color = new Color3f(1.0f, 1.0f, 1.0f);

    // Shade model (flat, smooth)
    int shadeModel = ColoringAttributes.SHADE_GOURAUD;

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object.
     * @param color the color that is used when lighting is disabled
     * or when material is null
     */
    final  void initColor(Color3f color) {
	this.color.set(color);
    }

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object and sends a message notifying
     * the interested structures of the change.
     * @param color the color that is used when lighting is disabled
     * or when material is null
     */
    final  void setColor(Color3f color) {
	initColor(color);
	sendMessage(COLOR_CHANGED, new Color3f(color));
    }

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object.  This color is used when lighting is disabled
     * or when material is null.
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     */
    final  void initColor(float r, float g, float b) {
	this.color.set(r, g, b);
    }

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object and sends a message notifying
     * the interested structures of the change.
     * This color is used when lighting is disabled
     * or when material is null.
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     */
    final  void setColor(float r, float g, float b) {
	initColor(r, g, b);
	sendMessage(COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Gets the intrinsic color of this ColoringAttributes
     * component object.
     * @param color the vector that will receive color
     */
    final void getColor(Color3f color) {
	color.set(this.color);
    }

    /**
     * Sets the shade mode for this ColoringAttributes component object.
     * @param shadeModel the shade mode to be used; one of FASTEST,
     * NICEST, SHADE_FLAT, or SHADE_GOURAUD
     */
    final void initShadeModel(int shadeModel) {
	this.shadeModel = shadeModel;
    }

    /**
     * Sets the shade mode for this ColoringAttributes component object
     * and sends a message notifying
     * the interested structures of the change.
     * @param shadeModel the shade mode to be used; one of FASTEST,
     * NICEST, SHADE_FLAT, or SHADE_GOURAUD
     */
    final void setShadeModel(int shadeModel) {
	initShadeModel(shadeModel);
	sendMessage(SHADE_MODEL_CHANGED, new Integer(shadeModel));
    }

    /**
     * Gets the shade mode for this ColoringAttributes component object.
     * @return shadeModel the shade mode
     */
    final int getShadeModel() {
	return shadeModel;
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
		ColoringAttributesRetained mirrorCa
		    = new ColoringAttributesRetained();
		mirrorCa.source = source;
		mirrorCa.set(this);
		mirror = mirrorCa;
	    }
	} else {
	    ((ColoringAttributesRetained) mirror).set(this);
	}
    }

    void updateNative(Context ctx,
		      float dRed, float dGreen, float dBlue,
		      float alpha, boolean lEnable) {
        Pipeline.getPipeline().updateColoringAttributes(ctx,
                dRed, dBlue, dGreen, color.x, color.y,
                color.z, alpha,
                lEnable, shadeModel);
    }

   /**
    * Creates a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void initMirrorObject() {
	((ColoringAttributesRetained)mirror).set(this);
    }

    /** Update the "component" field of the mirror object with the
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	ColoringAttributesRetained mirrorCa =
	    (ColoringAttributesRetained) mirror;

	if ((component & COLOR_CHANGED) != 0) {
	    mirrorCa.color.set(((Color3f)value));
	}
	else if ((component & SHADE_MODEL_CHANGED) != 0) {
	    mirrorCa.shadeModel = ((Integer)value).intValue();
	}
    }

    boolean equivalent(ColoringAttributesRetained cr) {
	return ((cr != null) &&
		color.equals(cr.color) &&
		(shadeModel == cr.shadeModel));
    }


    // This functions clones the retained side only and is used
    // internally
     protected Object clone() {
	 ColoringAttributesRetained cr =
	     (ColoringAttributesRetained)super.clone();
	 cr.color = new Color3f(color);
	 // shadeModel is copied in super.clone()
	 return cr;
     }

    // This functions clones the retained side only and is used
    // internally
     protected void set(ColoringAttributesRetained cr) {
	 super.set(cr);
         color.set(cr.color);
         shadeModel = cr.shadeModel;
     }

    final void sendMessage(int attrMask, Object attr) {
	ArrayList<VirtualUniverse> univList = new ArrayList<VirtualUniverse>();
	ArrayList<ArrayList<GeometryAtom>> gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);
	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.COLORINGATTRIBUTES_CHANGED;
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
	    createMessage.type = J3dMessage.COLORINGATTRIBUTES_CHANGED;

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
	if (bit == ColoringAttributes.ALLOW_COLOR_WRITE ||
	    bit == ColoringAttributes.ALLOW_SHADE_MODEL_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }

}
