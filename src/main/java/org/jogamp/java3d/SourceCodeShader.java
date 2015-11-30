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

package org.jogamp.java3d;

/**
 * The SourceCodeShader object is a shader that is defined using
 * text-based source code. It is used to define the source code for
 * both vertex and fragment shaders. The currently supported shading
 * languages are Cg and GLSL.
 *
 * @see ShaderProgram
 *
 * @since Java 3D 1.4
 */

public class SourceCodeShader extends Shader {

    /**
     * Not a public constructor, for internal use
     */
    SourceCodeShader() {
    }

    /**
     * Constructs a new shader object of the specified shading
     * language and shader type from the specified source string.
     *
     * @param shadingLanguage the specified shading language, one of:
     * <code>SHADING_LANGUAGE_GLSL</code> or
     * <code>SHADING_LANGUAGE_CG</code>.
     *
     * @param shaderType the shader type, one of:
     * <code>SHADER_TYPE_VERTEX</code> or
     * <code>SHADER_TYPE_FRAGMENT</code>.
     *
     * @param shaderSource the shader source code
     *
     * @exception NullPointerException if shaderSource is null.
     */

    public SourceCodeShader(int shadingLanguage, int shaderType, String shaderSource) {
	super(shadingLanguage, shaderType);
        if (shaderSource == null) {
            throw new NullPointerException();
        }
	((SourceCodeShaderRetained)this.retained).initShaderSource(shaderSource);
    }

    /**
     * Retrieves the shader source string from this shader object.
     *
     * @return the shader source string.
     */
    public String getShaderSource() {
	return ((SourceCodeShaderRetained)this.retained).getShaderSource();
    }


    /**
     * Creates a retained mode SourceCodeShaderRetained object that this
     * SourceCodeShader component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new SourceCodeShaderRetained();
	this.retained.setSource(this);
	// System.err.println("SourceCodeShader.createRetained()");
    }

    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
    public NodeComponent cloneNodeComponent() {
	SourceCodeShaderRetained scsRetained = (SourceCodeShaderRetained) retained;

	SourceCodeShader scs = new SourceCodeShader(scsRetained.getShadingLanguage(),
						    scsRetained.getShaderType(),
						    scsRetained.getShaderSource());
	scs.duplicateNodeComponent(this);
	return scs;
    }


   /**
     * Copies all node information from <code>originalNodeComponent</code>
     * into the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object).
     *
     * @param originalNodeComponent the original node to duplicate
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	String sc = ((SourceCodeShaderRetained) originalNodeComponent.retained).getShaderSource();

	if (sc != null) {
	    ((SourceCodeShaderRetained) retained).setShaderSource(sc);
	}
    }

}
