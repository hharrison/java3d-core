/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * The GLSLShaderProgram object is a concrete implementation of a
 * ShaderProgram node component for the OpenGL GLSL shading language.
 *
 * @see SourceCodeShader
 *
 * @since Java 3D 1.4
 */

public class GLSLShaderProgram extends ShaderProgram {

    /**
     * Constructs a GLSL shader program node component.
     *
     * <br>
     * TODO: ADD MORE DOCUMENTATION HERE.
     */
    public GLSLShaderProgram() {
    }

    // Implement abstract setVertexAttrNames method (inherit javadoc from parent class)
    public void setVertexAttrNames(String[] vertexAttrNames) {
	checkForLiveOrCompiled();

        if (vertexAttrNames != null) {
            for (int i = 0; i < vertexAttrNames.length; i++) {
                if (vertexAttrNames[i] == null) {
                    throw new NullPointerException();
                }
            }
        }

        ((GLSLShaderProgramRetained)this.retained).setVertexAttrNames(vertexAttrNames);
    }

    // Implement abstract getVertexAttrNames method (inherit javadoc from parent class)
    public String[] getVertexAttrNames() {

	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_NAMES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GLSLShaderProgram0"));
	    }
	}

 	return ((GLSLShaderProgramRetained)this.retained).getVertexAttrNames();

    }

    // Implement abstract setShaderAttrNames method (inherit javadoc from parent class)
    public void setShaderAttrNames(String[] shaderAttrNames) {
	checkForLiveOrCompiled();

        if (shaderAttrNames != null) {
            for (int i = 0; i < shaderAttrNames.length; i++) {
                if (shaderAttrNames[i] == null) {
                    throw new NullPointerException();
                }
            }
        }

        ((GLSLShaderProgramRetained)this.retained).setShaderAttrNames(shaderAttrNames);
    }

    // Implement abstract getShaderAttrNames method (inherit javadoc from parent class)
    public String[] getShaderAttrNames() {

	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_NAMES_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GLSLShaderProgram0"));
	    }
	}

 	return ((GLSLShaderProgramRetained)this.retained).getShaderAttrNames();

    }

    /**
     * Copies the specified array of shaders into this shader
     * program. This method makes a shallow copy of the array. The
     * array of shaders may be null or empty (0 length), but the
     * elements of the array must be non-null. The shading language of
     * each shader in the array must be
     * <code>SHADING_LANGUAGE_GLSL</code>. Each shader in the array must
     * be a SourceCodeShader.
     *
     * @param shaders array of Shader objects to be copied into this
     * ShaderProgram
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the shading language of
     * any shader in the shaders array is <em>not</em>
     * <code>SHADING_LANGUAGE_GLSL</code>.
     *
     * @exception ClassCastException if any shader in the shaders
     * array is <em>not</em> a SourceCodeShader.
     *
     * @exception NullPointerException if any element in the
     * shaders array is null.
     */
    public void setShaders(Shader[] shaders) {
	checkForLiveOrCompiled();
        
        if(shaders != null) {
            // Check shaders for valid shading language and class type
            for (int i = 0; i < shaders.length; i++) {
                if (shaders[i].getShadingLanguage() != Shader.SHADING_LANGUAGE_GLSL) {
                    throw new IllegalArgumentException(J3dI18N.getString("GLSLShaderProgram2"));
                }
                
                // Try to cast shader to SourceCodeShader; it will throw
                // ClassCastException if it isn't.
                SourceCodeShader shad = (SourceCodeShader)shaders[i];
            }
            
        }
        
 	((GLSLShaderProgramRetained)this.retained).setShaders(shaders);
    }

    // Implement abstract getShaders method (inherit javadoc from parent class)
    public Shader[] getShaders() {

	if (isLiveOrCompiled()) {
	    if(!this.getCapability(ALLOW_SHADERS_READ)) {
		throw new CapabilityNotSetException(J3dI18N.getString("GLSLShaderProgram1"));
	    }
	}

 	return ((GLSLShaderProgramRetained)this.retained).getShaders();
    }

    /**
     * Creates a retained mode GLSLShaderProgramRetained object that this
     * GLSLShaderProgram component object will point to.
     */
    void createRetained() {
	this.retained = new GLSLShaderProgramRetained();
	this.retained.setSource(this);
    }


}
