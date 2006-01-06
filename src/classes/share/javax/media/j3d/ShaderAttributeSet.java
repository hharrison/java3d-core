/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import javax.vecmath.*;

/**
 * The ShaderAttributeSet object provides uniform attributes to shader
 * programs. Uniform attributes (variables) are those attributes whose
 * values are constant during the rendering of a primitive. Their
 * values may change from primitive to primitive, but are constant for
 * each vertex (for vertex shaders) or fragment (for fragment shaders)
 * of a single primitive. Examples of uniform attributes include a
 * transformation matrix, a texture map, lights, lookup tables, etc.
 * The ShaderAttributeSet object contains a set of ShaderAttribute
 * objects. Each ShaderAttribute object defines the value of a single
 * uniform shader variable. The set of attributes is unique with respect
 * to attribute names: no two attributes in the set will have the same
 * name.
 *
 * <p>
 * There are two ways in which values can be specified for uniform
 * attributes: explicitly, by providing a value; and implicitly, by
 * defining a binding between a Java 3D system attribute and a uniform
 * attribute. This functionality is provided by two subclasses of
 * ShaderAttribute: ShaderAttributeObject, which is used to specify
 * explicitly defined attributes; and ShaderAttributeBinding, which is
 * used to specify implicitly defined, automatically tracked attributes.
 *
 * <p>
 * Depending on the shading language (and profile) being used, several
 * Java 3D state attributes are automatically made available to the
 * shader program as pre-defined uniform attributes. The application
 * doesn't need to do anything to pass these attributes in to the
 * shader program. The implementation of each shader language (e.g.,
 * Cg, GLSL) defines its own bindings from Java 3D attribute to uniform
 * variable name. A list of these attributes for each shader language
 * can be found in the concrete subclass of ShaderProgram for that
 * shader language.
 *
 * @see ShaderAttribute
 * @see ShaderProgram
 * @see ShaderAppearance#setShaderAttributeSet
 *
 * @since Java 3D 1.4
 */

public class ShaderAttributeSet extends NodeComponent {

    /**
     * Specifies that this ShaderAttributeSet object allows reading
     * its attributes.
     */
    public static final int
	ALLOW_ATTRIBUTES_READ =
	CapabilityBits.SHADER_ATTRIBUTE_SET_ALLOW_ATTRIBUTES_READ;

    /**
     * Specifies that this ShaderAttributeSet object allows writing
     * its attributes.
     */
    public static final int
	ALLOW_ATTRIBUTES_WRITE =
	CapabilityBits.SHADER_ATTRIBUTE_SET_ALLOW_ATTRIBUTES_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_ATTRIBUTES_READ
    };
    
    /**
     * Constructs an empty ShaderAttributeSet object. The attributes set
     * is initially empty.
     */
    public ShaderAttributeSet() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    //
    // Methods for dealing with the (name, value) pairs for explicit
    // attributes
    //

    /**
     * Adds the specified shader attribute to the attributes set.
     * The newly specified attribute replaces an attribute with the
     * same name, if one already exists in the attributes set.
     *
     * @param attr the shader attribute to be added to the set
     *
     * @exception NullPointerException if attr is null
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void put(ShaderAttribute attr) {
	if (attr == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet1"));
	
        ((ShaderAttributeSetRetained)this.retained).put(attr);

    }

    /**
     * Retrieves the shader attribute with the specified
     * <code>attrName</code> from the attributes set. If attrName does
     * not exist in the attributes set, null is returned.
     *
     * @param attrName the name of the shader attribute to be retrieved
     *
     * @exception NullPointerException if attrName is null
     *
     * @return a the shader attribute associated with the specified
     * attribute name, or null if the name is not in the attributes
     * set
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public ShaderAttribute get(String attrName) {

 	if (attrName == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet0"));

	return ((ShaderAttributeSetRetained)this.retained).get(attrName);
    }

    /**
     * Removes the shader attribute with the specified
     * <code>attrName</code> from the attributes set. If attrName does
     * not exist in the attributes set then nothing happens.
     *
     * @param attrName the name of the shader attribute to be removed
     *
     * @exception NullPointerException if attrName is null
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void remove(String attrName) {
	if (attrName == null) {
	    throw new NullPointerException();
	}
	
        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet1"));
	
	((ShaderAttributeSetRetained)this.retained).remove(attrName);
    }

    /**
     * Removes the specified shader attribute from the attributes
     * set. If the attribute does not exist in the attributes set then
     * nothing happens. Note that this method will <i>not</i> remove a
     * shader object other than the one specified, even if it has the
     * same name as the specified attribute. Applications that wish to
     * remove an attribute by name should use
     * <code>removeAttribute(String)</code>.
     *
     * @param attr the shader attribute to be removed
     *
     * @exception NullPointerException if attr is null
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void remove(ShaderAttribute attr) {
	if (attr == null) {
	    throw new NullPointerException();
	}

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet1"));

	((ShaderAttributeSetRetained)this.retained).remove(attr);
    }

    /**
     * Removes all shader attributes from the attributes set. The
     * attributes set will be empty following this call.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void clear() {

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet1"));

	((ShaderAttributeSetRetained)this.retained).clear();
    }

    /**
     * Returns a shallow copy of the attributes set.
     *
     * @return a shallow copy of the attributes set
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public ShaderAttribute[] getAll() {

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet0"));

	return ((ShaderAttributeSetRetained)this.retained).getAll();
    }

    /**
     * Returns the number of elements in the attributes set.
     *
     * @return the number of elements in the attributes set
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int size() {

        if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAttributeSet0"));
	
	return ((ShaderAttributeSetRetained)this.retained).size();
    }

    /**
     * Creates a retained mode ShaderAttributeSetRetained object that this
     * ShaderAttributeSet component object will point to.
     */
    void createRetained() {
	// System.out.println("ShaderAttributeSet : createRetained() ...");
	this.retained = new ShaderAttributeSetRetained();
	this.retained.setSource(this);
    }
}
