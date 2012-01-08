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

import javax.vecmath.Vector4f;

/**
 * The TexCoordGeneration object contains all parameters needed for texture
 * coordinate generation.  It is included as part of an Appearance
 * component object.
 */
class TexCoordGenerationRetained extends NodeComponentRetained {

    // A list of pre-defined bits to indicate which component
    // in this TexCoordGeneration object changed.
    private static final int ENABLE_CHANGED     = 0x01;
    private static final int PLANE_S_CHANGED    = 0x02;
    private static final int PLANE_T_CHANGED    = 0x04;
    private static final int PLANE_R_CHANGED    = 0x08;
    private static final int PLANE_Q_CHANGED    = 0x10;

    //
    // State variables
    //
    int genMode = TexCoordGeneration.OBJECT_LINEAR;
    int format = TexCoordGeneration.TEXTURE_COORDINATE_2;

    Vector4f planeS = new Vector4f(1.0f, 0.0f, 0.0f, 0.0f);
    Vector4f planeT = new Vector4f(0.0f, 1.0f, 0.0f, 0.0f);
    Vector4f planeR = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);
    Vector4f planeQ = new Vector4f(0.0f, 0.0f, 0.0f, 0.0f);

    /**
     * Flag to enable/disable Texture coordinate generation.
     */
     boolean enable = true;

    // true when mirror texCoord component set
    boolean mirrorCompDirty = false;

    /**
     * Enables or disables texture coordinate generation for this
     * appearance component object.
     * @param state true or false to enable or disable texture coordinate
     * generation
     */
    final void initEnable(boolean state) {
	enable = state;
    }
    /**
     * Enables or disables texture coordinate generation for this
     * appearance component object and sends a message notifying
     * the interested structures of the change.
     * @param state true or false to enable or disable texture coordinate
     * generation
     */
    final void setEnable(boolean state) {
	initEnable(state);
	sendMessage(ENABLE_CHANGED, (state ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of the texCoordGeneration enable flag.
     * @return true if texture coordinate generation is enabled,
     * false if texture coordinate generation is disabled
     */
    final boolean getEnable() {
	return enable;
    }
    /**
     * Sets the TexCoordGeneration format to the specified value.
     * @param format texture format, one of: TEXTURE_COORDINATE_2
     * or TEXTURE_COORDINATE_3
     */
    final void initFormat(int format) {
	this.format = format;
    }

    /**
     * Retrieves the current TexCoordGeneration format.
     * @return the texture format
     */
    final int getFormat() {
	return format;
    }

    /**
     * Sets the TexCoordGeneration generation mode to the specified value.
     * @param genMode texture generation mode, one of: OBJECT_LINEAR,
     * EYE_LINEAR, or SPHERE_MAP
     */
    final void initGenMode(int genMode) {
	this.genMode = genMode;
    }

    /**
     * Retrieves the current TexCoordGeneration generation mode.
     * @return the texture generation mode
     */
    final int getGenMode() {
	return genMode;
    }

    /**
     * Sets the S coordinate plane equation.  This plane equation
     * is used to generate the S coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeS plane equation for the S coordinate
     */
    final void setPlaneS(Vector4f planeS) {
	initPlaneS(planeS);
	sendMessage(PLANE_S_CHANGED, new Vector4f(planeS));
    }

    /**
     * Sets the S coordinate plane equation.  This plane equation
     * is used to generate the S coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeS plane equation for the S coordinate
     */
    final void initPlaneS(Vector4f planeS) {
	this.planeS.set(planeS);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the S coordinate.
     * @param planeS the S coordinate plane equation
     */
    final void getPlaneS(Vector4f planeS) {
	planeS.set(this.planeS);
    }

    /**
     * Sets the T coordinate plane equation.  This plane equation
     * is used to generate the T coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeT plane equation for the T coordinate
     */
    final void setPlaneT(Vector4f planeT) {
	initPlaneT(planeT);
	sendMessage(PLANE_T_CHANGED, new Vector4f(planeT));
    }

    /**
     * Sets the T coordinate plane equation.  This plane equation
     * is used to generate the T coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeT plane equation for the T coordinate
     */
    final void initPlaneT(Vector4f planeT) {
	this.planeT.set(planeT);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the T coordinate.
     * @param planeT the T coordinate plane equation
     */
    final void getPlaneT(Vector4f planeT) {
	planeT.set(this.planeT);
    }

    /**
     * Sets the R coordinate plane equation.  This plane equation
     * is used to generate the R coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeR plane equation for the R coordinate
     */
    final void setPlaneR(Vector4f planeR) {
	initPlaneR(planeR);
	sendMessage(PLANE_R_CHANGED, new Vector4f(planeR));
    }

    /**
     * Sets the R coordinate plane equation.  This plane equation
     * is used to generate the R coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeR plane equation for the R coordinate
     */
    final void initPlaneR(Vector4f planeR) {
	this.planeR.set(planeR);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the R coordinate.
     * @param planeR the R coordinate plane equation
     */
    final void getPlaneR(Vector4f planeR) {
	planeR.set(this.planeR);
    }

    /**
     * Sets the Q coordinate plane equation.  This plane equation
     * is used to generate the Q coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeQ plane equation for the Q coordinate
     */
    final void setPlaneQ(Vector4f planeQ) {
	initPlaneQ(planeQ);
	sendMessage(PLANE_Q_CHANGED, new Vector4f(planeQ));
    }

    /**
     * Sets the Q coordinate plane equation.  This plane equation
     * is used to generate the Q coordinate in OBJECT_LINEAR and EYE_LINEAR
     * texture generation modes.
     * @param planeQ plane equation for the Q coordinate
     */
    final void initPlaneQ(Vector4f planeQ) {
        this.planeQ.set(planeQ);
    }

    /**
     * Retrieves a copy of the plane equation used to
     * generate the Q coordinate.
     * @param planeQ the Q coordinate plane equation
     */
    final void getPlaneQ(Vector4f planeQ) {
        planeQ.set(this.planeQ);
    }



   /**
    * Creates a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void createMirrorObject() {
	if (mirror == null) {
	    // Check the capability bits and let the mirror object
	    // point to itself if is not editable
	    if (isStatic()) {
		mirror= this;
	    } else {
		TexCoordGenerationRetained mirrorTg = new TexCoordGenerationRetained();
		mirrorTg.set(this);
		mirrorTg.source = source;
		mirror = mirrorTg;
	    }
	} else {
	    ((TexCoordGenerationRetained) mirror).set(this);
	}
    }

    void updateNative(Canvas3D cv) {
	int gMode = genMode;
	Transform3D trans = null;
	Transform3D m = cv.vworldToEc;

	if (((cv.textureExtendedFeatures & Canvas3D.TEXTURE_CUBE_MAP) == 0) &&
	    ((genMode == TexCoordGeneration.NORMAL_MAP) ||
	    (genMode == TexCoordGeneration.REFLECTION_MAP))) {
	    gMode = TexCoordGeneration.SPHERE_MAP;
	}

	if (VirtualUniverse.mc.isD3D() &&
	    (gMode == TexCoordGeneration.EYE_LINEAR)) {
	    trans = new Transform3D(cv.vworldToEc);
	    trans.invert();
	    m = trans;
	}

	Pipeline.getPipeline().updateTexCoordGeneration(cv.ctx,
		     enable, gMode, format, planeS.x, planeS.y, planeS.z,
		     planeS.w, planeT.x, planeT.y, planeT.z, planeT.w,
		     planeR.x, planeR.y, planeR.z, planeR.w,
		     planeQ.x, planeQ.y, planeQ.z, planeQ.w,
		     m.mat);
    }

   /**
    * Initializes a mirror object, point the mirror object to the retained
    * object if the object is not editable
    */
    synchronized void initMirrorObject() {
	((TexCoordGenerationRetained)mirror).set(this);
    }

    /** Update the "component" field of the mirror object with the
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {

	TexCoordGenerationRetained mirrorTc = (TexCoordGenerationRetained) mirror;

	mirrorTc.mirrorCompDirty = true;

	if ((component & ENABLE_CHANGED) != 0) {
	    mirrorTc.enable  = ((Boolean)value).booleanValue();
	}
	else if ((component & PLANE_S_CHANGED) != 0) {
	    mirrorTc.planeS = (Vector4f)value;
	}
	else if ((component & PLANE_T_CHANGED) != 0) {
	    mirrorTc.planeT = (Vector4f)value;
	}
	else if ((component & PLANE_R_CHANGED) != 0) {
	    mirrorTc.planeR = (Vector4f)value;
	}
	else if ((component & PLANE_Q_CHANGED) != 0) {
	    mirrorTc.planeQ = (Vector4f)value;
	}
    }


    boolean equivalent(TexCoordGenerationRetained tr) {

	if (tr == null) {
	    return (false);

	} else if ((this.changedFrequent != 0) || (tr.changedFrequent != 0)) {
	    return (this == tr);
	}

	return ((tr.genMode == genMode) &&
		(tr.format == format) &&
		(tr.enable == enable) &&
    		tr.planeS.equals(planeS) &&
    		tr.planeT.equals(planeT) &&
    		tr.planeR.equals(planeR));
    }

    protected Object clone() {
	TexCoordGenerationRetained tr = (TexCoordGenerationRetained)super.clone();
	tr.planeS = new Vector4f(planeS);
	tr.planeT = new Vector4f(planeT);
	tr.planeR = new Vector4f(planeR);
	// other attributes is copied in super.clone()
	return tr;

    }

    protected void set(TexCoordGenerationRetained tr) {
	super.set(tr);
	genMode = tr.genMode;
	format = tr.format;
	enable = tr.enable;
	planeS.set(tr.planeS);
	planeT.set(tr.planeT);
	planeR.set(tr.planeR);
    }

    final void sendMessage(int attrMask, Object attr) {

	ArrayList<VirtualUniverse> univList = new ArrayList<VirtualUniverse>();
	ArrayList<ArrayList<GeometryAtom>> gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.TEXCOORDGENERATION_CHANGED;
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
	    createMessage.type = J3dMessage.TEXCOORDGENERATION_CHANGED;

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
        switch (bit) {
        case TexCoordGeneration.ALLOW_ENABLE_WRITE:
        case TexCoordGeneration.ALLOW_PLANE_WRITE: {
            setFrequencyChangeMask(bit, bit);
        }
        default:
            break;
        }
    }
}
