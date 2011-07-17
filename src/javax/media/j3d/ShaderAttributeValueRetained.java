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

import javax.vecmath.*;

/**
 * The ShaderAttributeValueRetained object encapsulates a uniform shader
 * attribute whose value is specified explicitly.
 */

class ShaderAttributeValueRetained extends ShaderAttributeObjectRetained {

    ShaderAttributeValueRetained() {
    }
    
    synchronized void createMirrorObject() {
	// System.err.println("ShaderAttributeValueRetained : createMirrorObject");
        // This method should only call by setLive().
	if (mirror == null) {
            ShaderAttributeValueRetained mirrorSAV = new ShaderAttributeValueRetained();
            mirrorSAV.createObjectData(getValue());
	    mirror = mirrorSAV;
	    mirror.source = source;

	}
	initMirrorObject();
    }

    /**
     * Computes the base class from the specified object. A
     * ClassCastException is thrown if the object is not an instance
     * of one of the allowed classes.
     */
    int computeClassType(Object value) {
	Class objClass = value.getClass();
	if (objClass.isArray()) {
	    throw new ClassCastException(objClass + " -- array class not allowed");
	}

	for (int i = 0; i < classTable.length; i++) {
	    if (classTable[i].isInstance(value)) {
		return i;
	    }
	}
	throw new ClassCastException(objClass + " -- unrecognized class");
    }

    /**
     * Returns the base class represented by the specified class type.
     */
    Class getBaseClass(int classType) {
	return classTable[classType];
    }

    /**
     * Creates an attribute wrapper object of the specified class
     * type, and stores the specified object.
     */
    AttrWrapper createAttrWrapper(Object value, int classType) {
	ValueWrapper attrWrapper = null;
	switch (classType) {
	case TYPE_INTEGER:
	    attrWrapper = new IntegerWrapper();
	    break;
	case TYPE_FLOAT:
	    attrWrapper = new FloatWrapper();
	    break;
//	case TYPE_DOUBLE:
//	    attrWrapper = new DoubleWrapper();
//	    break;
	case TYPE_TUPLE2I:
	    attrWrapper = new Tuple2iWrapper();
	    break;
	case TYPE_TUPLE2F:
	    attrWrapper = new Tuple2fWrapper();
	    break;
//	case TYPE_TUPLE2D:
//	    attrWrapper = new Tuple2dWrapper();
//	    break;
	case TYPE_TUPLE3I:
	    attrWrapper = new Tuple3iWrapper();
	    break;
	case TYPE_TUPLE3F:
	    attrWrapper = new Tuple3fWrapper();
	    break;
//	case TYPE_TUPLE3D:
//	    attrWrapper = new Tuple3dWrapper();
//	    break;
	case TYPE_TUPLE4I:
	    attrWrapper = new Tuple4iWrapper();
	    break;
	case TYPE_TUPLE4F:
	    attrWrapper = new Tuple4fWrapper();
	    break;
//	case TYPE_TUPLE4D:
//	    attrWrapper = new Tuple4dWrapper();
//	    break;
	case TYPE_MATRIX3F:
	    attrWrapper = new Matrix3fWrapper();
	    break;
//	case TYPE_MATRIX3D:
//	    attrWrapper = new Matrix3dWrapper();
//	    break;
	case TYPE_MATRIX4F:
	    attrWrapper = new Matrix4fWrapper();
	    break;
//	case TYPE_MATRIX4D:
//	    attrWrapper = new Matrix4dWrapper();
//	    break;
	default:
	    // Should never get here
	    assert false;
	    return null;
	}

	attrWrapper.set(value);
	return attrWrapper;
    }

    //
    // The following wrapper classes are used to store a copy of the
    // user-specified shader attribute value. There is a wrapper class
    // for each supported base class.
    //

    // Base wrapper class for non-array attribute types
    static abstract class ValueWrapper extends AttrWrapper {
	// No additional fields or methods are defined in this class
    }

    // Wrapper class for Integer
    static class IntegerWrapper extends ValueWrapper {
	private int[] value = new int[1];

	void set(Object value) {
	    this.value[0] = ((Integer)value).intValue();
	}

	Object get() {
	    return new Integer(this.value[0]);
	}

	Object getRef() {
	    return this.value;
	}
    }

    // Wrapper class for Float
    static class FloatWrapper extends ValueWrapper {
	private float[] value = new float[1];

	void set(Object value) {
	    this.value[0] = ((Float)value).floatValue();
	}

	Object get() {
	    return new Float(this.value[0]);
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Double
    static class DoubleWrapper extends ValueWrapper {
	private double[] value = new double[1];

	void set(Object value) {
	    this.value[0] = ((Double)value).doubleValue();
	}

	Object get() {
	    return new Double(value[0]);
	}

	Object getRef() {
	    return value;
	}
    }
    */

    // Wrapper class for Tuple2i
    static class Tuple2iWrapper extends ValueWrapper {
	private int[] value = new int[2];

	void set(Object value) {
	    ((Tuple2i)value).get(this.value);
	}

	Object get() {
	    return new Point2i(value);
	}

	Object getRef() {
	    return value;
	}
    }

    // Wrapper class for Tuple2f
    static class Tuple2fWrapper extends ValueWrapper {
	private float[] value = new float[2];

	void set(Object value) {
	    ((Tuple2f)value).get(this.value);
	}

	Object get() {
	    return new Point2f(value);
	}

	Object getRef() {
	    return value;
	}
    }

    /*
    // Wrapper class for Tuple2d
    static class Tuple2dWrapper extends ValueWrapper {
	private double[] value = new double[2];

	void set(Object value) {
	    ((Tuple2d)value).get(this.value);
	}

	Object get() {
	    return new Point2d(value);
	}

	Object getRef() {
	    return value;
	}
    }
    */

    // Wrapper class for Tuple3i
    static class Tuple3iWrapper extends ValueWrapper {
	private int[] value = new int[3];

	void set(Object value) {
	    ((Tuple3i)value).get(this.value);
	}

	Object get() {
	    return new Point3i(value);
	}

	Object getRef() {
	    return value;
	}
    }

    // Wrapper class for Tuple3f
    static class Tuple3fWrapper extends ValueWrapper {
	private float[] value = new float[3];

	void set(Object value) {
	    ((Tuple3f)value).get(this.value);
	}

	Object get() {
	    return new Point3f(value);
	}

	Object getRef() {
	    return value;
	}
    }

    /*
    // Wrapper class for Tuple3d
    static class Tuple3dWrapper extends ValueWrapper {
	private double[] value = new double[3];

	void set(Object value) {
	    ((Tuple3d)value).get(this.value);
	}

	Object get() {
	    return new Point3d(value);
	}

	Object getRef() {
	    return value;
	}
    }
    */

    // Wrapper class for Tuple4i
    static class Tuple4iWrapper extends ValueWrapper {
	private int[] value = new int[4];

	void set(Object value) {
	    ((Tuple4i)value).get(this.value);
	}

	Object get() {
	    return new Point4i(value);
	}

	Object getRef() {
	    return value;
	}
    }

    // Wrapper class for Tuple4f
    static class Tuple4fWrapper extends ValueWrapper {
	private float[] value = new float[4];

	void set(Object value) {
	    ((Tuple4f)value).get(this.value);
	}

	Object get() {
	    return new Point4f(value);
	}

	Object getRef() {
	    return value;
	}
    }

    /*
    // Wrapper class for Tuple4d
    static class Tuple4dWrapper extends ValueWrapper {
	private double[] value = new double[4];

	void set(Object value) {
	    ((Tuple4d)value).get(this.value);
	}

	Object get() {
	    return new Point4d(value);
	}

	Object getRef() {
	    return value;
	}
    }
    */

    // Wrapper class for Matrix3f
    static class Matrix3fWrapper extends ValueWrapper {
	private float[] value = new float[9];

	void set(Object value) {
	    Matrix3f m = (Matrix3f)value;
	    this.value[0] = m.m00;
	    this.value[1] = m.m01;
	    this.value[2] = m.m02;
	    this.value[3] = m.m10;
	    this.value[4] = m.m11;
	    this.value[5] = m.m12;
	    this.value[6] = m.m20;
	    this.value[7] = m.m21;
	    this.value[8] = m.m22;
	}

	Object get() {
	    return new Matrix3f(value);
	}

	Object getRef() {
	    return value;
	}
    }

    /*
    // Wrapper class for Matrix3d
    static class Matrix3dWrapper extends ValueWrapper {
	private double[] value = new double[9];

	void set(Object value) {
	    Matrix3d m = (Matrix3d)value;
	    this.value[0] = m.m00;
	    this.value[1] = m.m01;
	    this.value[2] = m.m02;
	    this.value[3] = m.m10;
	    this.value[4] = m.m11;
	    this.value[5] = m.m12;
	    this.value[6] = m.m20;
	    this.value[7] = m.m21;
	    this.value[8] = m.m22;
	}

	Object get() {
	    return new Matrix3d(value);
	}

	Object getRef() {
	    return value;
	}
    }
    */

    // Wrapper class for Matrix4f
    static class Matrix4fWrapper extends ValueWrapper {
	private float[] value = new float[16];

	void set(Object value) {
	    Matrix4f m = (Matrix4f)value;
	    this.value[0]  = m.m00;
	    this.value[1]  = m.m01;
	    this.value[2]  = m.m02;
	    this.value[3]  = m.m03;
	    this.value[4]  = m.m10;
	    this.value[5]  = m.m11;
	    this.value[6]  = m.m12;
	    this.value[7]  = m.m13;
	    this.value[8]  = m.m20;
	    this.value[9]  = m.m21;
	    this.value[10] = m.m22;
	    this.value[11] = m.m23;
	    this.value[12] = m.m30;
	    this.value[13] = m.m31;
	    this.value[14] = m.m32;
	    this.value[15] = m.m33;
	}

	Object get() {
	    return new Matrix4f(value);
	}

	Object getRef() {
	    return value;
	}
    }

    /*
    // Wrapper class for Matrix4d
    static class Matrix4dWrapper extends ValueWrapper {
	private double[] value = new double[16];

	void set(Object value) {
	    Matrix4d m = (Matrix4d)value;
	    this.value[0]  = m.m00;
	    this.value[1]  = m.m01;
	    this.value[2]  = m.m02;
	    this.value[3]  = m.m03;
	    this.value[4]  = m.m10;
	    this.value[5]  = m.m11;
	    this.value[6]  = m.m12;
	    this.value[7]  = m.m13;
	    this.value[8]  = m.m20;
	    this.value[9]  = m.m21;
	    this.value[10] = m.m22;
	    this.value[11] = m.m23;
	    this.value[12] = m.m30;
	    this.value[13] = m.m31;
	    this.value[14] = m.m32;
	    this.value[15] = m.m33;
	}

	Object get() {
	    return new Matrix4d(value);
	}

	Object getRef() {
	    return value;
	}
    }
    */

}
