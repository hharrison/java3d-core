/*
 * $RCSfile$
 *
 * Copyright 2004-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.Hashtable;

/**
 * <p>The ShaderAppearance object defines programmable shading attributes
 * that can be set as a component object of a Shape3D node. The
 * ShaderAppearance rendering state adds the following attributes in
 * addition to those defined by Appearance:</p>
 *
 * <ul>
 * <li>Shader program - specifies the shader program...</li>
 *
 * <p></p>
 * <li>Shader attribute set - specifies the shader parameters, both as
 * explicit attributes and as implicit bindings to Java 3D
 * state...</li>
 * </ul>
 *
 * <p>The ShaderAppearance object modifies the definition of some of the
 * attributes in Appearance:</p>
 *
 * <ul>
 * <li>Coloring attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Line attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Point attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Polygon attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Rendering attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Transparency attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Material - XXXXX</li>
 *
 * <p></p>
 * <li>Texture - XXXXX</li>
 *
 * <p></p>
 * <li>Texture attributes - XXXXX</li>
 *
 * <p></p>
 * <li>Texture coordinate generation - XXXXX</li>
 *
 * <p></p>
 * <li>Texture unit state - XXXXX</li>
 * </ul>
 *
 * @see ShaderProgram
 * @see ShaderAttributeSet
 *
 * @since Java 3D 1.4
 */
public class ShaderAppearance extends Appearance {
    /**
     * Specifies that this ShaderAppearance object allows reading its
     * ShaderProgram component information.
     */
    public static final int
	ALLOW_SHADER_PROGRAM_READ =
	CapabilityBits.SHADER_APPEARANCE_ALLOW_SHADER_PROGRAM_READ;

    /**
     * Specifies that this ShaderAppearance object allows writing its
     * ShaderProgram component information.
     */
    public static final int
	ALLOW_SHADER_PROGRAM_WRITE =
	CapabilityBits.SHADER_APPEARANCE_ALLOW_SHADER_PROGRAM_WRITE;

    /**
     * Specifies that this ShaderAppearance object allows reading its
     * ShaderAttributeSet component information.
     */
    public static final int
	ALLOW_SHADER_ATTRIBUTE_SET_READ =
	CapabilityBits.SHADER_APPEARANCE_ALLOW_SHADER_ATTRIBUTE_SET_READ;

    /**
     * Specifies that this ShaderAppearance object allows writing its
     * ShaderAttributeSet component information.
     */
    public static final int
	ALLOW_SHADER_ATTRIBUTE_SET_WRITE =
	CapabilityBits.SHADER_APPEARANCE_ALLOW_SHADER_ATTRIBUTE_SET_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_SHADER_PROGRAM_READ,
	ALLOW_SHADER_ATTRIBUTE_SET_READ
    };

    /**
     * Constructs a ShaderAppearance component object using defaults for all
     * state variables. All component object references are initialized
     * to null.
     */
    public ShaderAppearance() {
	// Just use default values
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Creates the retained mode ShaderAppearanceRetained object that this
     * ShaderAppearance component object will point to.
     */
    void createRetained() {
	this.retained = new ShaderAppearanceRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the ShaderProgram object to the specified object.  Setting it to
     * null causes a default pass-through shader to be used ???
     *
     * @param shaderProgram object that specifies the desired shader program
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setShaderProgram(ShaderProgram shaderProgram) {

        if (isLiveOrCompiled()) {
	  if(!this.getCapability(ALLOW_SHADER_PROGRAM_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("ShaderAppearance0"));
	}

	((ShaderAppearanceRetained)this.retained).setShaderProgram(shaderProgram);
	
    }


    /**
     * Retrieves the current ShaderProgram object.
     *
     * @return the ShaderProgram object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ShaderProgram getShaderProgram() {

        if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_SHADER_PROGRAM_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAppearance1"));
	}
	return ((ShaderAppearanceRetained)this.retained).getShaderProgram();
    }


    /**
     * Sets the ShaderAttributeSet object to the specified object.  Setting it to
     * null is equivalent to specifying an empty set of attributes.
     *
     * @param shaderAttributeSet object that specifies the desired shader attributes
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setShaderAttributeSet(ShaderAttributeSet shaderAttributeSet) {
        if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_SHADER_ATTRIBUTE_SET_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAppearance2"));
	}

	((ShaderAppearanceRetained)this.retained).setShaderAttributeSet(shaderAttributeSet);
    }


    /**
     * Retrieves the current ShaderAttributeSet object.
     *
     * @return the ShaderAttributeSet object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ShaderAttributeSet getShaderAttributeSet() {
        if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_SHADER_ATTRIBUTE_SET_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ShaderAppearance3"));
	}
	return ((ShaderAppearanceRetained)this.retained).getShaderAttributeSet();
    }


   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        ShaderAppearance a = new ShaderAppearance();
        a.duplicateNodeComponent(this);
        return a;
    }

    /**
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated replaced with duplicateNodeComponent(
     *  NodeComponent originalNodeComponent, boolean forceDuplicate)
     */
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
	checkDuplicateNodeComponent(originalNodeComponent);
    }

   /**
     * Copies all ShaderAppearance information from
     * <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	Hashtable hashtable = originalNodeComponent.nodeHashtable;

	ShaderAppearanceRetained app =
	    (ShaderAppearanceRetained) originalNodeComponent.retained;

	ShaderAppearanceRetained rt = (ShaderAppearanceRetained) retained;

	rt.setShaderProgram((ShaderProgram) getNodeComponent(app.getShaderProgram(),
				forceDuplicate,
				hashtable));
    }

    /**
     *  This function is called from getNodeComponent() to see if any of
     *  the sub-NodeComponents  duplicateOnCloneTree flag is true.
     *  If it is the case, current NodeComponent needs to
     *  duplicate also even though current duplicateOnCloneTree flag is false.
     *  This should be overwrite by NodeComponent which contains sub-NodeComponent.
     */
    boolean duplicateChild() {
	if (super.duplicateChild())
	    return true;

	if (getDuplicateOnCloneTree())
	    return true;

	ShaderAppearanceRetained rt = (ShaderAppearanceRetained) retained;

	NodeComponent nc;

	nc = rt.getShaderProgram();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	return false;
    }

}
