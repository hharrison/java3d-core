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
    void createRetained() {
	this.retained = new SourceCodeShaderRetained();
	this.retained.setSource(this);
	// System.out.println("SourceCodeShader.createRetained()");
    }
    
    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
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
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) { 
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	String sc = ((SourceCodeShaderRetained) originalNodeComponent.retained).getShaderSource();

	if (sc != null) {
	    ((SourceCodeShaderRetained) retained).setShaderSource(sc);
	}
    }

}
