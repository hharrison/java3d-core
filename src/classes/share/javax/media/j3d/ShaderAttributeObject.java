/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;


/**
 * The ShaderAttributeObject class is an abstract class that
 * encapsulates a uniform shader attribute whose value is specified
 * explicitly. This class has concrete subclasses for single-value
 * attributes (ShaderAttributeValue) and array attributes
 * (ShaderAttributeArray). The shader variable <code>attrName</code>
 * is explicitly set to the specified <code>value</code> during
 * rendering. <code>attrName</code> must be the name of a valid
 * uniform attribute in the shader in which it is used. Otherwise, the
 * attribute name will be ignored and a runtime error may be
 * generated. The <code>value</code> must be an instance of one of the
 * allowed classes or an array of one the allowed classes. The allowed
 * classes are: <code>Integer</code>, <code>Float</code>,
 * <code>Tuple{2,3,4}{i,f}</code>,
 * <code>Matrix{3,4}f</code>. A ClassCastException will be thrown
 * if a specified <code>value</code> object is not one of the allowed
 * types. Further, the type of the value is immutable once a
 * ShaderAttributeObject is constructed.  Subsequent setValue
 * operations must be called with an object of the same type as the
 * one that was used to construct the ShaderAttributeObject. Finally,
 * the type of the <code>value</code> object must match the type of
 * the corresponding <code>attrName</code> variable in the shader in
 * which it is used. Otherwise, the shader will not be able to use the
 * attribute and a runtime error may be generated.
 *
 * @see ShaderAttributeSet
 * @see ShaderProgram
 *
 * @since Java 3D 1.4
 */

public abstract class ShaderAttributeObject extends ShaderAttribute {

    /**
     * Specifies that this ShaderAttributeObject allows reading its value.
     */
    public static final int
	ALLOW_VALUE_READ =
	CapabilityBits.SHADER_ATTRIBUTE_OBJECT_ALLOW_VALUE_READ;

    /**
     * Specifies that this ShaderAttributeObject allows writing its value.
     */
    public static final int
	ALLOW_VALUE_WRITE =
	CapabilityBits.SHADER_ATTRIBUTE_OBJECT_ALLOW_VALUE_WRITE;


   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_VALUE_READ
    };


    /**
     * Package scope constructor
     */
    ShaderAttributeObject(String attrName, Object value) {
	super(attrName);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((ShaderAttributeObjectRetained)this.retained).createObjectData(value);
    }


    /**
     * Retrieves the value of this shader attribute.
     * A copy of the object is returned.
     *
     * @return a copy of the value of this shader attribute
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public abstract Object getValue();

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
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public abstract void setValue(Object value);

    /**
     * Retrieves the base class of the value of this shader attribute.
     * This class will always be one of the allowable classes, even if
     * a subclass was used to construct this shader attribute object.
     * For example, if this shader attribute object was constructed
     * with an instance of <code>javax.vecmath.Point3f</code>, the
     * returned class would be <code>javax.vecmath.Tuple3f</code>.
     *
     * @return the base class of the value of this shader attribute
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Class getValueClass() {

 	return ((ShaderAttributeObjectRetained)this.retained).getValueClass();
    }

}
