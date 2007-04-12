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

import javax.vecmath.*;

/**
 * The ShaderAttributeArray object encapsulates a uniform shader
 * attribute whose value is specified explicitly.
 */

class ShaderAttributeArrayRetained extends ShaderAttributeObjectRetained {

    ShaderAttributeArrayRetained() {
    }

    void initValue(int index, Object value) {
	/*
	System.err.println("ShaderAttributeObjectRetained : attrName = " + attrName +
			   ", index = " + index + ", value = " + value +
			   ", value.class = " + value.getClass());
	*/
	((ArrayWrapper)attrWrapper).set(index, value);

    }

    
    /**
     * Sets the specified array element of the value of this shader
     * attribute to the specified value.
     * A copy of the object is stored.
     *
     * @param value the new value of the shader attribute
     *
     * @exception NullPointerException if value is null
     *
     * @exception ClassCastException if value is not an instance of
     * the same base class as the individual elements of the array object
     * used to construct this shader attribute object.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    void setValue(int index, Object value) {
	initValue(index, value);
	// We should only need to update the array instead of replacing it.
	// Until this become a really bottleneck, it will just be a convenience 
	// method for end user. 
	// An efficient approach is to 
	// (1) Create a new ShaderAttributeValue object for the "value" object 
	// and pass it to sendMessage(), (2) Create a new sendMessage that take in
	// a third arguement, ie. index.
	setValue(attrWrapper.getRef());
    }

    /**
     * Returns the number of elements in the value array.
     *
     * @return the number of elements in the value array
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    int length() {
	return ((ArrayWrapper)attrWrapper).length();

    }

    // Helper methods ...

    synchronized void createMirrorObject() {
	// System.err.println("ShaderAttributeArrayRetained : createMirrorObject");
        // This method should only call by setLive().
	if (mirror == null) {
            ShaderAttributeArrayRetained mirrorSAA = new ShaderAttributeArrayRetained();
	    mirrorSAA.createObjectData(getValue());
	    mirror = mirrorSAA;
	    mirror.source = source;
	    
	}
	initMirrorObject();
    }


    /**
     * Computes the base class from the specified object. A
     * ClassCastException is thrown if the object is not an array of
     * one of the allowed classes.
     */
    int computeClassType(Object value) {
	Class objClass = value.getClass();
	if (!objClass.isArray()) {
	    throw new ClassCastException(objClass + " -- must be array class");
	}

	for (int i = 0; i < classTable.length; i++) {
	    if (classTableArr[i].isInstance(value)) {
		return i;
	    }
	}
	throw new ClassCastException(objClass + " -- unrecognized class");
    }

    /**
     * Returns the base class represented by the specified class type.
     */
    Class getBaseClass(int classType) {
	return classTableArr[classType];
    }

    /**
     * Creates an attribute wrapper object of the specified class
     * type, and stores the specified array of objects.
     */
    AttrWrapper createAttrWrapper(Object value, int classType) {
	ArrayWrapper attrWrapper = null;
	switch (classType) {
	case TYPE_INTEGER:
	    attrWrapper = new IntegerArrayWrapper();
	    break;
	case TYPE_FLOAT:
	    attrWrapper = new FloatArrayWrapper();
	    break;
//	case TYPE_DOUBLE:
//	    attrWrapper = new DoubleArrayWrapper();
//	    break;
	case TYPE_TUPLE2I:
	    attrWrapper = new Tuple2iArrayWrapper();
	    break;
	case TYPE_TUPLE2F:
	    attrWrapper = new Tuple2fArrayWrapper();
	    break;
//	case TYPE_TUPLE2D:
//	    attrWrapper = new Tuple2dArrayWrapper();
//	    break;
	case TYPE_TUPLE3I:
	    attrWrapper = new Tuple3iArrayWrapper();
	    break;
	case TYPE_TUPLE3F:
	    attrWrapper = new Tuple3fArrayWrapper();
	    break;
//	case TYPE_TUPLE3D:
//	    attrWrapper = new Tuple3dArrayWrapper();
//	    break;
	case TYPE_TUPLE4I:
	    attrWrapper = new Tuple4iArrayWrapper();
	    break;
	case TYPE_TUPLE4F:
	    attrWrapper = new Tuple4fArrayWrapper();
	    break;
//	case TYPE_TUPLE4D:
//	    attrWrapper = new Tuple4dArrayWrapper();
//	    break;
	case TYPE_MATRIX3F:
	    attrWrapper = new Matrix3fArrayWrapper();
	    break;
//	case TYPE_MATRIX3D:
//	    attrWrapper = new Matrix3dArrayWrapper();
//	    break;
	case TYPE_MATRIX4F:
	    attrWrapper = new Matrix4fArrayWrapper();
	    break;
//	case TYPE_MATRIX4D:
//	    attrWrapper = new Matrix4dArrayWrapper();
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

    // Base wrapper class for array attribute types
    static abstract class ArrayWrapper extends AttrWrapper {
	int length = 0;

	/**
	 * Returns the length of the array
	 */
	int length() {
	    return length;
	}

	/**
	 * Sets the specified array element of the value of this
	 * shader attribute to the specified value.
	 */
	abstract void set(int index, Object value);
    }

    // Wrapper class for Integer
    static class IntegerArrayWrapper extends ArrayWrapper {
	private int[] value = new int[0];

	void set(Object value) {
	    Integer[] arr = (Integer[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new int[this.length];
	    }
	    for (int i = 0; i < this.length; i++) {
		this.value[i] = arr[i].intValue();
	    }
	}

	void set(int index, Object value) {
	    this.value[index] = ((Integer)value).intValue();
	}

	Object get() {
	    Integer[] arr = new Integer[this.length];
	    for (int i = 0; i < this.length; i++) {
		arr[i] = new Integer(this.value[i]);
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    // Wrapper class for Float
    static class FloatArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Float[] arr = (Float[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length];
	    }
	    for (int i = 0; i < this.length; i++) {
		this.value[i] = arr[i].floatValue();
	    }
	}

	void set(int index, Object value) {
	    this.value[index] = ((Float)value).floatValue();
	}

	Object get() {
	    Float[] arr = new Float[this.length];
	    for (int i = 0; i < this.length; i++) {
		arr[i] = new Float(this.value[i]);
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Double
    static class DoubleArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Double[] arr = (Double[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length];
	    }
	    for (int i = 0; i < this.length; i++) {
		this.value[i] = arr[i].doubleValue();
	    }
	}

	void set(int index, Object value) {
	    this.value[index] = ((Double)value).doubleValue();
	}

	Object get() {
	    Double[] arr = new Double[this.length];
	    for (int i = 0; i < this.length; i++) {
		arr[i] = new Double(this.value[i]);
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */

    // Wrapper class for Tuple2i
    static class Tuple2iArrayWrapper extends ArrayWrapper {
	private int[] value = new int[0];

	void set(Object value) {
	    Tuple2i[] arr = (Tuple2i[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new int[this.length*2];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 2;
	    this.value[j+0] = ((Tuple2i)value).x;
	    this.value[j+1] = ((Tuple2i)value).y;
	}

	Object get() {
	    Tuple2i[] arr = new Tuple2i[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
                arr[i] = new Point2i();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    // Wrapper class for Tuple2f
    static class Tuple2fArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Tuple2f[] arr = (Tuple2f[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length*2];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 2;
	    this.value[j+0] = ((Tuple2f)value).x;
	    this.value[j+1] = ((Tuple2f)value).y;
	}

	Object get() {
	    Tuple2f[] arr = new Tuple2f[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
                arr[i] = new Point2f();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Tuple2d
    static class Tuple2dArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Tuple2d[] arr = (Tuple2d[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length*2];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 2;
	    this.value[j+0] = ((Tuple2d)value).x;
	    this.value[j+1] = ((Tuple2d)value).y;
	}

	Object get() {
	    Tuple2d[] arr = new Tuple2d[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 2;
                arr[i] = new Point2d();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */

    // Wrapper class for Tuple3i
    static class Tuple3iArrayWrapper extends ArrayWrapper {
	private int[] value = new int[0];

	void set(Object value) {
	    Tuple3i[] arr = (Tuple3i[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new int[this.length*3];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 3;
	    this.value[j+0] = ((Tuple3i)value).x;
	    this.value[j+1] = ((Tuple3i)value).y;
	    this.value[j+2] = ((Tuple3i)value).z;
	}

	Object get() {
	    Tuple3i[] arr = new Tuple3i[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
                arr[i] = new Point3i();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    // Wrapper class for Tuple3f
    static class Tuple3fArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Tuple3f[] arr = (Tuple3f[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length*3];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 3;
	    this.value[j+0] = ((Tuple3f)value).x;
	    this.value[j+1] = ((Tuple3f)value).y;
	    this.value[j+2] = ((Tuple3f)value).z;
	}

	Object get() {
	    Tuple3f[] arr = new Tuple3f[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
                arr[i] = new Point3f();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Tuple3d
    static class Tuple3dArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Tuple3d[] arr = (Tuple3d[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length*3];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 3;
	    this.value[j+0] = ((Tuple3d)value).x;
	    this.value[j+1] = ((Tuple3d)value).y;
	    this.value[j+2] = ((Tuple3d)value).z;
	}

	Object get() {
	    Tuple3d[] arr = new Tuple3d[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 3;
                arr[i] = new Point3d();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */

    // Wrapper class for Tuple4i
    static class Tuple4iArrayWrapper extends ArrayWrapper {
	private int[] value = new int[0];

	void set(Object value) {
	    Tuple4i[] arr = (Tuple4i[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new int[this.length*4];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
		this.value[j+3] = arr[i].w;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 4;
	    this.value[j+0] = ((Tuple4i)value).x;
	    this.value[j+1] = ((Tuple4i)value).y;
	    this.value[j+2] = ((Tuple4i)value).z;
	    this.value[j+3] = ((Tuple4i)value).w;
	}

	Object get() {
	    Tuple4i[] arr = new Tuple4i[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
                arr[i] = new Point4i();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
		arr[i].w = this.value[j+3];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    // Wrapper class for Tuple4f
    static class Tuple4fArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Tuple4f[] arr = (Tuple4f[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length*4];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
		this.value[j+3] = arr[i].w;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 4;
	    this.value[j+0] = ((Tuple4f)value).x;
	    this.value[j+1] = ((Tuple4f)value).y;
	    this.value[j+2] = ((Tuple4f)value).z;
	    this.value[j+3] = ((Tuple4f)value).w;
	}

	Object get() {
	    Tuple4f[] arr = new Tuple4f[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
                arr[i] = new Point4f();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
		arr[i].w = this.value[j+3];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Tuple4d
    static class Tuple4dArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Tuple4d[] arr = (Tuple4d[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length*4];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
		this.value[j+0] = arr[i].x;
		this.value[j+1] = arr[i].y;
		this.value[j+2] = arr[i].z;
		this.value[j+3] = arr[i].w;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 4;
	    this.value[j+0] = ((Tuple4d)value).x;
	    this.value[j+1] = ((Tuple4d)value).y;
	    this.value[j+2] = ((Tuple4d)value).z;
	    this.value[j+3] = ((Tuple4d)value).w;
	}

	Object get() {
	    Tuple4d[] arr = new Tuple4d[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 4;
                arr[i] = new Point4d();
		arr[i].x = this.value[j+0];
		arr[i].y = this.value[j+1];
		arr[i].z = this.value[j+2];
		arr[i].w = this.value[j+3];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */

    // Wrapper class for Matrix3f
    static class Matrix3fArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Matrix3f[] arr = (Matrix3f[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length * 9];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 9;
		this.value[j+0] = arr[i].m00;
		this.value[j+1] = arr[i].m01;
		this.value[j+2] = arr[i].m02;
		this.value[j+3] = arr[i].m10;
		this.value[j+4] = arr[i].m11;
		this.value[j+5] = arr[i].m12;
		this.value[j+6] = arr[i].m20;
		this.value[j+7] = arr[i].m21;
		this.value[j+8] = arr[i].m22;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 9;
	    Matrix3f m = (Matrix3f)value;

	    this.value[j+0] = m.m00;
	    this.value[j+1] = m.m01;
	    this.value[j+2] = m.m02;
	    this.value[j+3] = m.m10;
	    this.value[j+4] = m.m11;
	    this.value[j+5] = m.m12;
	    this.value[j+6] = m.m20;
	    this.value[j+7] = m.m21;
	    this.value[j+8] = m.m22;
	}

	Object get() {
	    Matrix3f[] arr = new Matrix3f[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 9;
                arr[i] = new Matrix3f();
		arr[i].m00 = this.value[j+0];
		arr[i].m01 = this.value[j+1];
		arr[i].m02 = this.value[j+2];
		arr[i].m10 = this.value[j+3];
		arr[i].m11 = this.value[j+4];
		arr[i].m12 = this.value[j+5];
		arr[i].m20 = this.value[j+6];
		arr[i].m21 = this.value[j+7];
		arr[i].m22 = this.value[j+8];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Matrix3d
    static class Matrix3dArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Matrix3d[] arr = (Matrix3d[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length * 9];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 9;
		this.value[j+0] = arr[i].m00;
		this.value[j+1] = arr[i].m01;
		this.value[j+2] = arr[i].m02;
		this.value[j+3] = arr[i].m10;
		this.value[j+4] = arr[i].m11;
		this.value[j+5] = arr[i].m12;
		this.value[j+6] = arr[i].m20;
		this.value[j+7] = arr[i].m21;
		this.value[j+8] = arr[i].m22;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 9;
	    Matrix3d m = (Matrix3d)value;

	    this.value[j+0] = m.m00;
	    this.value[j+1] = m.m01;
	    this.value[j+2] = m.m02;
	    this.value[j+3] = m.m10;
	    this.value[j+4] = m.m11;
	    this.value[j+5] = m.m12;
	    this.value[j+6] = m.m20;
	    this.value[j+7] = m.m21;
	    this.value[j+8] = m.m22;
	}

	Object get() {
	    Matrix3d[] arr = new Matrix3d[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 9;
                arr[i] = new Matrix3d();
		arr[i].m00 = this.value[j+0];
		arr[i].m01 = this.value[j+1];
		arr[i].m02 = this.value[j+2];
		arr[i].m10 = this.value[j+3];
		arr[i].m11 = this.value[j+4];
		arr[i].m12 = this.value[j+5];
		arr[i].m20 = this.value[j+6];
		arr[i].m21 = this.value[j+7];
		arr[i].m22 = this.value[j+8];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */

    // Wrapper class for Matrix4f
    static class Matrix4fArrayWrapper extends ArrayWrapper {
	private float[] value = new float[0];

	void set(Object value) {
	    Matrix4f[] arr = (Matrix4f[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new float[this.length * 16];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 16;
		this.value[j+0]  = arr[i].m00;
		this.value[j+1]  = arr[i].m01;
		this.value[j+2]  = arr[i].m02;
		this.value[j+3]  = arr[i].m03;
		this.value[j+4]  = arr[i].m10;
		this.value[j+5]  = arr[i].m11;
		this.value[j+6]  = arr[i].m12;
		this.value[j+7]  = arr[i].m13;
		this.value[j+8]  = arr[i].m20;
		this.value[j+9]  = arr[i].m21;
		this.value[j+10] = arr[i].m22;
		this.value[j+11] = arr[i].m23;
		this.value[j+12] = arr[i].m30;
		this.value[j+13] = arr[i].m31;
		this.value[j+14] = arr[i].m32;
		this.value[j+15] = arr[i].m33;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 16;
	    Matrix4f m = (Matrix4f)value;

	    this.value[j+0]  = m.m00;
	    this.value[j+1]  = m.m01;
	    this.value[j+2]  = m.m02;
	    this.value[j+3]  = m.m03;
	    this.value[j+4]  = m.m10;
	    this.value[j+5]  = m.m11;
	    this.value[j+6]  = m.m12;
	    this.value[j+7]  = m.m13;
	    this.value[j+8]  = m.m20;
	    this.value[j+9]  = m.m21;
	    this.value[j+10] = m.m22;
	    this.value[j+11] = m.m23;
	    this.value[j+12] = m.m30;
	    this.value[j+13] = m.m31;
	    this.value[j+14] = m.m32;
	    this.value[j+15] = m.m33;
	}

	Object get() {
	    Matrix4f[] arr = new Matrix4f[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 16;
                arr[i] = new Matrix4f();
		arr[i].m00 = this.value[j+0];
		arr[i].m01 = this.value[j+1];
		arr[i].m02 = this.value[j+2];
		arr[i].m03 = this.value[j+3];
		arr[i].m10 = this.value[j+4];
		arr[i].m11 = this.value[j+5];
		arr[i].m12 = this.value[j+6];
		arr[i].m13 = this.value[j+7];
		arr[i].m20 = this.value[j+8];
		arr[i].m21 = this.value[j+9];
		arr[i].m22 = this.value[j+10];
		arr[i].m23 = this.value[j+11];
		arr[i].m30 = this.value[j+12];
		arr[i].m31 = this.value[j+13];
		arr[i].m32 = this.value[j+14];
		arr[i].m33 = this.value[j+15];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }

    /*
    // Wrapper class for Matrix4d
    static class Matrix4dArrayWrapper extends ArrayWrapper {
	private double[] value = new double[0];

	void set(Object value) {
	    Matrix4d[] arr = (Matrix4d[])value;
	    if (this.length != arr.length) {
		this.length = arr.length;
		this.value = new double[this.length * 16];
	    }
	    for (int i = 0; i < this.length; i++) {
		int j = i * 16;
		this.value[j+0]  = arr[i].m00;
		this.value[j+1]  = arr[i].m01;
		this.value[j+2]  = arr[i].m02;
		this.value[j+3]  = arr[i].m03;
		this.value[j+4]  = arr[i].m10;
		this.value[j+5]  = arr[i].m11;
		this.value[j+6]  = arr[i].m12;
		this.value[j+7]  = arr[i].m13;
		this.value[j+8]  = arr[i].m20;
		this.value[j+9]  = arr[i].m21;
		this.value[j+10] = arr[i].m22;
		this.value[j+11] = arr[i].m23;
		this.value[j+12] = arr[i].m30;
		this.value[j+13] = arr[i].m31;
		this.value[j+14] = arr[i].m32;
		this.value[j+15] = arr[i].m33;
	    }
	}

	void set(int index, Object value) {
	    int j = index * 16;
	    Matrix4d m = (Matrix4d)value;

	    this.value[j+0]  = m.m00;
	    this.value[j+1]  = m.m01;
	    this.value[j+2]  = m.m02;
	    this.value[j+3]  = m.m03;
	    this.value[j+4]  = m.m10;
	    this.value[j+5]  = m.m11;
	    this.value[j+6]  = m.m12;
	    this.value[j+7]  = m.m13;
	    this.value[j+8]  = m.m20;
	    this.value[j+9]  = m.m21;
	    this.value[j+10] = m.m22;
	    this.value[j+11] = m.m23;
	    this.value[j+12] = m.m30;
	    this.value[j+13] = m.m31;
	    this.value[j+14] = m.m32;
	    this.value[j+15] = m.m33;
	}

	Object get() {
	    Matrix4d[] arr = new Matrix4d[this.length];
	    for (int i = 0; i < this.length; i++) {
		int j = i * 16;
                arr[i] = new Matrix4d();
		arr[i].m00 = this.value[j+0];
		arr[i].m01 = this.value[j+1];
		arr[i].m02 = this.value[j+2];
		arr[i].m03 = this.value[j+3];
		arr[i].m10 = this.value[j+4];
		arr[i].m11 = this.value[j+5];
		arr[i].m12 = this.value[j+6];
		arr[i].m13 = this.value[j+7];
		arr[i].m20 = this.value[j+8];
		arr[i].m21 = this.value[j+9];
		arr[i].m22 = this.value[j+10];
		arr[i].m23 = this.value[j+11];
		arr[i].m30 = this.value[j+12];
		arr[i].m31 = this.value[j+13];
		arr[i].m32 = this.value[j+14];
		arr[i].m33 = this.value[j+15];
	    }
	    return arr;
	}

	Object getRef() {
	    return this.value;
	}
    }
    */
}
