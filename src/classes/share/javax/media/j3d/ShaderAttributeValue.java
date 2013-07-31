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
 * The ShaderAttributeValue object encapsulates a uniform shader
 * attribute whose value is specified explicitly. The shader variable
 * <code>attrName</code> is explicitly set to the specified
 * <code>value</code> during rendering. <code>attrName</code> must be
 * the name of a valid uniform attribute in the shader in which it is
 * used. Otherwise, the attribute name will be ignored and a runtime
 * error may be generated. The <code>value</code> must be an instance
 * of one of the allowed classes. The allowed classes are:
 * <code>Integer</code>, <code>Float</code>,
 * <code>Tuple{2,3,4}{i,f}</code>, <code>Matrix{3,4}f</code>. A
 * ClassCastException will be thrown if a specified <code>value</code>
 * object is not one of the allowed types. Further, the type of the
 * value is immutable once a ShaderAttributeValue is constructed.
 * Subsequent setValue operations must be called with an object of the
 * same type as the one that was used to construct the
 * ShaderAttributeValue. Finally, the type of the <code>value</code>
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

public class ShaderAttributeValue extends ShaderAttributeObject {
    /**
     * Constructs a new ShaderAttributeValue object with the specified
     * <code>(attrName,&nbsp;value)</code> pair.
     * A copy of the object is stored.
     *
     * @param attrName the name of the shader attribute
     * @param value the value of the shader attribute
     *
     * @exception NullPointerException if attrName or value is null
     *
     * @exception ClassCastException if value is not an instance of
     * one of the allowed classes
     */
    public ShaderAttributeValue(String attrName, Object value) {
	super(attrName, value);
    }

    // Implement abstract getValue method
    @Override
    public Object getValue() {

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject0"));

 	return ((ShaderAttributeValueRetained)this.retained).getValue();
    }

    // Implement abstract setValue method
    @Override
    public void setValue(Object value) {

        if (value == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_VALUE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeObject1"));

	if (isLive())
	    ((ShaderAttributeValueRetained)this.retained).setValue(value);
	else
	    ((ShaderAttributeValueRetained)this.retained).initValue(value);

    }

    /**
     * Creates a retained mode ShaderAttributeValueRetained object that this
     * ShaderAttributeValue component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new ShaderAttributeValueRetained();
	this.retained.setSource(this);
    }

}
