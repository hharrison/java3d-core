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


/**
 * The ShaderAttributeArray object encapsulates a uniform shader
 * attribute whose value is specified explicitly. The shader variable
 * <code>attrName</code> is explicitly set to the specified
 * <code>value</code> during rendering. <code>attrName</code> must be
 * the name of a valid uniform attribute in the shader in which it is
 * used. Otherwise, the attribute name will be ignored and a runtime
 * error may be generated. The <code>value</code> must be an array
 * of one of the allowed classes. The allowed classes are:
 * <code>Integer[]</code>, <code>Float[]</code>,
 * <code>Tuple{2,3,4}{i,f}[]</code>, <code>Matrix{3,4}f[]</code>. A
 * ClassCastException will be thrown if a specified <code>value</code>
 * object is not one of the allowed types. Further, the type and length of the
 * value is immutable once a ShaderAttributeArray is constructed.
 * Subsequent setValue operations must be called with an array of the
 * same type and length as the one that was used to construct the
 * ShaderAttributeArray. Finally, the type of the <code>value</code>
 * object must match the type of the corresponding
 * <code>attrName</code> variable in the shader in which it is
 * used. Otherwise, the shader will not be able to use the attribute
 * and a runtime error may be generated.
 *
 * @see ShaderAttributeSet
 * @see ShaderProgram
 *
 * @since Java 3D 1.4
 */

public class ShaderAttributeArray extends ShaderAttributeObject {
    /**
     * Constructs a new ShaderAttributeArray object with the specified
     * <code>(attrName,&nbsp;value)</code> pair. The specified value
     * must be an array of one of the allowed class types.
     * A deep copy of the array is stored.
     *
     * @param attrName the name of the shader attribute
     * @param value the value of the shader attribute
     *
     * @exception NullPointerException if attrName or value is null
     *
     * @exception ClassCastException if value is not an array of
     * one of the allowed classes
     */
    public ShaderAttributeArray(String attrName, Object value) {
	super(attrName, value);
    }

    // Implement abstract getValue method
    public Object getValue() {
        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject0"));

 	return ((ShaderAttributeArrayRetained)this.retained).getValue();
    }

    // Implement abstract setValue method
    public void setValue(Object value) {
	if (value == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject1"));

	if (isLive())
	    ((ShaderAttributeArrayRetained)this.retained).setValue(value);
	else
	    ((ShaderAttributeArrayRetained)this.retained).initValue(value);

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
    public void setValue(int index, Object value) {
	if (value == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject1"));

	if (isLive())
	    ((ShaderAttributeArrayRetained)this.retained).setValue(index, value);
	else {
	    ((ShaderAttributeArrayRetained)this.retained).initValue(index, value);
	}
    }

    /**
     * Returns the number of elements in the value array.
     *
     * @return the number of elements in the value array
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int length() {
        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject0"));

        return ((ShaderAttributeArrayRetained)this.retained).length();
    }

    /**
     * Creates a retained mode ShaderAttributeArrayRetained object that this
     * ShaderAttributeArray component object will point to.
     */
    void createRetained() {
	this.retained = new ShaderAttributeArrayRetained();
	this.retained.setSource(this);
    }
}
