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
 * The GLSLShaderProgram object is a concrete implementation of a
 * ShaderProgram node component for the OpenGL GLSL shading language.
 */

class GLSLShaderProgramRetained extends ShaderProgramRetained {
    
    /**
     * Constructs a GLSL shader program node component.
     */
    GLSLShaderProgramRetained() {
    }

    synchronized void createMirrorObject() {
	// System.out.println("GLSLShaderProgramRetained : createMirrorObject");
        // This method should only call by setLive().
	if (mirror == null) {
            GLSLShaderProgramRetained  mirrorGLSLSP = new GLSLShaderProgramRetained();
            mirror = mirrorGLSLSP;
	    mirror.source = source;
	}
	initMirrorObject();
    }

    // ShaderAttributeValue methods

    ShaderError setUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {

        return Pipeline.getPipeline().setGLSLUniform1i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {

        return Pipeline.getPipeline().setGLSLUniform1f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform2i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform2f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform3i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform4i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform4f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix4f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    // ShaderAttributeArray methods

    ShaderError setUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform1iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform1fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform2iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform2fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform3iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform4iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform4fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix4fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }
 
    /**
     * Method to return a flag indicating whether this
     * ShaderProgram is supported on the specified Canvas.
     */
    boolean isSupported(Canvas3D cv) {
        return cv.shadingLanguageGLSL;
    }

    /**
     * Method to create the native shader.
     */
    ShaderError createShader(Context ctx, ShaderRetained shader, ShaderId[] shaderIdArr) {	
	  return Pipeline.getPipeline().createGLSLShader(ctx, shader.shaderType, shaderIdArr);
    }
    
    /**
     * Method to destroy the native shader.
     */
    ShaderError destroyShader(Context ctx, ShaderId shaderId) {
	return Pipeline.getPipeline().destroyGLSLShader(ctx, shaderId);
    }
    
    /**
     * Method to compile the native shader.
     */
    ShaderError compileShader(Context ctx, ShaderId shaderId, String source) {
        return Pipeline.getPipeline().compileGLSLShader(ctx, shaderId, source );
    }

    /**
     * Method to create the native shader program.
     */
    ShaderError createShaderProgram(Context ctx, ShaderProgramId[] shaderProgramIdArr) {
	    return Pipeline.getPipeline().createGLSLShaderProgram(ctx, shaderProgramIdArr);  
    }

    /**
     * Method to destroy the native shader program.
     */
    ShaderError destroyShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return Pipeline.getPipeline().destroyGLSLShaderProgram(ctx, shaderProgramId);
    }

    /**
     * Method to link the native shader program.
     */
    ShaderError linkShaderProgram(Context ctx, ShaderProgramId shaderProgramId, ShaderId[] shaderIds) {
        return Pipeline.getPipeline().linkGLSLShaderProgram(ctx, shaderProgramId, shaderIds);
    }
 
    ShaderError bindVertexAttrName(Context ctx, ShaderProgramId shaderProgramId, String attrName, int attrIndex) {
        return Pipeline.getPipeline().bindGLSLVertexAttrName(ctx, shaderProgramId, attrName, attrIndex);
    }

    void lookupVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId, String[] attrNames, boolean[] errArr) {
        // This method is a no-op for GLSL
    }

    void lookupShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            String[] attrNames, AttrNameInfo[] attrNameInfoArr) {

        int numAttrNames = attrNames.length;
        
        ShaderAttrLoc[] locArr = new ShaderAttrLoc[numAttrNames];
        int[] typeArr = new int[numAttrNames];
        int[] sizeArr = new int[numAttrNames]; // currently unused
        boolean[] isArrayArr = new boolean[numAttrNames];

        Pipeline.getPipeline().lookupGLSLShaderAttrNames(ctx, shaderProgramId,
                numAttrNames, attrNames, locArr, typeArr, sizeArr, isArrayArr);

        for (int i = 0; i < numAttrNames; i++) {
            attrNameInfoArr[i] = new AttrNameInfo();
            attrNameInfoArr[i].setLocation(locArr[i]);
            attrNameInfoArr[i].setArray(isArrayArr[i]);
            attrNameInfoArr[i].setType(typeArr[i]);
//            System.err.println(attrNames[i] +
//                    " : loc = " + locArr[i] +
//                    ", type = " + typeArr[i] +
//                    ", isArray = " + isArrayArr[i] +
//                    ", size = " + sizeArr[i]);
        }
    }
    
    /**
     * Method to enable the native shader program.
     */
    ShaderError enableShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
	return Pipeline.getPipeline().useGLSLShaderProgram(ctx, shaderProgramId);
    }
	
    /**
     * Method to disable the native shader program.
     */
    ShaderError disableShaderProgram(Context ctx) {
	return Pipeline.getPipeline().useGLSLShaderProgram(ctx, null);
    }


}
