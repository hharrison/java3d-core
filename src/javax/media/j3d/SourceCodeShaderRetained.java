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
 * The SourceCodeShaderRetained object is a shader that is defined using
 * text-based source code. It is used to define the source code for
 * both vertex and fragment shaders. The currently supported shading
 * languages are Cg and GLSL.
 */

class SourceCodeShaderRetained extends ShaderRetained {

    private String shaderSource = null;

    /**
     * Constructs a new shader retained object of the specified shading
     * language and shader type from the specified source string.
     */

    SourceCodeShaderRetained() {
    }

    // This method is similar to setShaderSource(). 
    // To conform to j3d frame in retained creation, we will stick with method 
    // with init name.
    final void initShaderSource(String shaderSource) {
	this.shaderSource = shaderSource;
    }    

    final void set(int shadingLanguage, int shaderType, String shaderSource) {
	this.shadingLanguage = shadingLanguage;
	this.shaderType = shaderType;
	this.shaderSource = shaderSource;
    }

    /**
     * Retrieves the shader source string from this shader object.
     *
     * @return the shader source string.
     */
    final String getShaderSource() {
	return shaderSource;
    }

    final void setShaderSource(String shaderSource) {
	this.shaderSource = shaderSource;
    }    

    synchronized void createMirrorObject() {
	// System.err.println("SourceCodeShaderRetained : createMirrorObject");

	if (mirror == null) {
	    SourceCodeShaderRetained  mirrorSCS = new SourceCodeShaderRetained();
	    mirror = mirrorSCS;
	}

	initMirrorObject();
    }
    
    /**
     * Initializes a mirror object.
     */
    synchronized void initMirrorObject() {
	mirror.source = source;

	((SourceCodeShaderRetained) mirror).set(shadingLanguage, shaderType, shaderSource);
	((SourceCodeShaderRetained) mirror).shaderData = null;	
    }

    synchronized void updateMirrorObject(int component, Object value) {
	System.err.println("SourceCodeShader.updateMirrorObject not implemented yet!");
    }

}
