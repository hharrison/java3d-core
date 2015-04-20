/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple2i;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple3i;
import javax.vecmath.Tuple4f;
import javax.vecmath.Tuple4i;

/**
 * The ShaderAttributeObjectRetained class is an abstract class that
 * encapsulates a uniform shader attribute whose value is specified
 * explicitly.
 */

abstract class ShaderAttributeObjectRetained extends ShaderAttributeRetained {

    private int classType;
    private Class baseClass;
    AttrWrapper attrWrapper;

    /**
     * Package scope constructor
     */
    ShaderAttributeObjectRetained() {
    }

    void createObjectData(Object value) {

  	classType = computeClassType(value);
	baseClass = getBaseClass(classType);
	attrWrapper = createAttrWrapper(value, classType);
 	/*
	System.err.println("    classType = " + classType +
			   ", baseClass = " + baseClass +
			   ", attrWrapper.get() = " + attrWrapper.get());
	*/
    }


    void initValue(Object value) {
	/*
	System.err.println("ShaderAttributeObjectRetained : attrName = " + attrName +
			   ", value = " + value +
			   ", value.class = " + value.getClass());
	*/
	attrWrapper.set(value);

    }

    /**
     * Retrieves the value of this shader attribute.
     * A copy of the object is returned.
     */
    Object getValue() {
	return attrWrapper.get();
    }

    /**
     * Sets the value of this shader attribute to the specified value.
     * A copy of the object is stored.
     *
     * @param value the new value of the shader attribute
     *
     * @exception NullPointerException if value is null
     *
     * @exception ClassCastException if value is not an instance of
     * the same base class as the object used to construct this shader
     * attribute object.
     *
     */
    void setValue(Object value) {
        initValue(value);
	AttrWrapper valueWrapper = createAttrWrapper(value, this.classType);
	sendMessage(ShaderConstants.ATTRIBUTE_VALUE_UPDATE, valueWrapper);
    }

    /**
     * Retrieves the base class of the value of this shader attribute.
     * This class will always be one of the allowable classes, even if
     * a subclass was used to construct this shader attribute object.
     * For example, if this shader attribute object was constructed
     * with an instance of <code>javax.vecmath.Point3f</code>, the
     * returned class would be <code>javax.vecmath.Tuple3f</code>.
     *
     * @return the base class of the value of this shader attribute
     */
    Class getValueClass() {
	return baseClass;
    }

   /**
     * Initializes a mirror object.
     */
    @Override
    synchronized void initMirrorObject() {
	super.initMirrorObject();
	((ShaderAttributeObjectRetained)mirror).initValue(getValue());
    }

     /**
     * Update the "component" field of the mirror object with the  given "value"
     */
    @Override
    synchronized void updateMirrorObject(int component, Object value) {

	//System.err.println("ShaderAttributeObjectRetained : updateMirrorObject");
	ShaderAttributeObjectRetained mirrorSAV = (ShaderAttributeObjectRetained)mirror;
        if ((component & ShaderConstants.ATTRIBUTE_VALUE_UPDATE) != 0) {
	    //System.err.println("     -- SHADER_ATTRIBUTE_VALUE_UPDATE");
	    mirrorSAV.attrWrapper = (AttrWrapper) value;
	}
    }

    final void sendMessage(int attrMask, Object attr) {

	ArrayList<VirtualUniverse> univList = new ArrayList<VirtualUniverse>();
	ArrayList<ArrayList<GeometryAtom>> gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);

	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.SHADER_ATTRIBUTE_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	//	System.err.println("changedFreqent1 = "+changedFrequent);
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	// System.err.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.SHADER_ATTRIBUTE_CHANGED;

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


    // Enumerated types representing allowed classes for shader
    // attributes.
    //
    // NOTE that the values for these enums are used as an index into
    // the tables of classes, so the values must start at 0 and
    // increment by 1. Also, the order must be the same as the order
    // of the entries in each of the two class tables.
    static final int TYPE_INTEGER  =  0;
    static final int TYPE_FLOAT    =  1;
    static final int TYPE_TUPLE2I  =  2;
    static final int TYPE_TUPLE2F  =  3;
    static final int TYPE_TUPLE3I  =  4;
    static final int TYPE_TUPLE3F  =  5;
    static final int TYPE_TUPLE4I  =  6;
    static final int TYPE_TUPLE4F  =  7;
    static final int TYPE_MATRIX3F =  8;
    static final int TYPE_MATRIX4F =  9;

    // Double-precision is not supported in the current version. Uncomment the
    // following if future support is done.
//    static final int TYPE_DOUBLE   = 10;
//    static final int TYPE_TUPLE2D  = 11;
//    static final int TYPE_TUPLE3D  = 12;
//    static final int TYPE_TUPLE4D  = 13;
//    static final int TYPE_MATRIX3D = 14;
//    static final int TYPE_MATRIX4D = 15;

    static final Class classTable[] = {
	Integer.class,
	Float.class,
	Tuple2i.class,
	Tuple2f.class,
	Tuple3i.class,
	Tuple3f.class,
	Tuple4i.class,
	Tuple4f.class,
	Matrix3f.class,
	Matrix4f.class,

        // Double-precision is not supported in the current version. Uncomment the
        // following if future support is done.
//	Double.class,
//	Tuple2d.class,
//	Tuple3d.class,
//	Tuple4d.class,
//	Matrix3d.class,
//	Matrix4d.class,
    };

    static final Class classTableArr[] = {
	Integer[].class,
	Float[].class,
	Tuple2i[].class,
	Tuple2f[].class,
	Tuple3i[].class,
	Tuple3f[].class,
	Tuple4i[].class,
	Tuple4f[].class,
	Matrix3f[].class,
	Matrix4f[].class,

        // Double-precision is not supported in the current version. Uncomment the
        // following if future support is done.
//	Double[].class,
//	Tuple2d[].class,
//	Tuple3d[].class,
//	Tuple4d[].class,
//	Matrix3d[].class,
//	Matrix4d[].class,
    };


    /**
     * Computes the base class from the specified object. A
     * ClassCastException is thrown if the object is not an instance
     * or array of one of the allowed classes.
     */
    abstract int computeClassType(Object value);

    /**
     * Returns the base class represented by the specified class type.
     */
    abstract Class getBaseClass(int classType);

    /**
     * Creates an attribute wrapper object of the specified class
     * type, and stores the specified object.
     */
    abstract AttrWrapper createAttrWrapper(Object value, int classType);


    /**
     * Base wrapper class for subclasses that are used to store a copy
     * of the user-specified shader attribute value. There is a
     * wrapper class for each supported base class in ShaderAttributeValue
     * and ShaderAttributeArray. The value is stored in a Java primitive array.
     */
    static abstract class AttrWrapper {
	/**
	 * Stores a copy of the specified object in the wrapper object
	 */
	abstract void set(Object value);

	/**
	 * Returns a copy of the wrapped object
	 */
	abstract Object get();

	/**
	 * Returns a reference to the internal primitive array used to
	 * wrap the object; note that the caller of this method must
	 * treat the data as read-only. It is intended only as a means
	 * to pass data down to native methods.
	 */
	abstract Object getRef();
    }

    int getClassType() {
        return classType;
    }

    void setClassType(int classType) {
        this.classType = classType;
    }


    // Issue 320 : Override base class method so we can force changedFrequent
    // to be set whenever the capability is writable, regardless of whether
    // it is frequently writable. We must do this because the ShaderBin doesn't
    // support updating shader attributes when changedFrequent is 0.
    @Override
    void setFrequencyChangeMask(int bit, int mask) {
        if (source.getCapability(bit)) {
            changedFrequent |= mask;
        } else if (!source.isLive()) {
            // Record the freq->infreq change only for non-live node components
            changedFrequent &= ~mask;
        }
    }

    @Override
    void handleFrequencyChange(int bit) {
	if (bit == ShaderAttributeObject.ALLOW_VALUE_WRITE) {
	    setFrequencyChangeMask(bit, 0x1);
	}
    }

}
