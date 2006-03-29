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

    ShaderError setUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value) {

        return Pipeline.getPipeline().setGLSLUniform1i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value) {

        return Pipeline.getPipeline().setGLSLUniform1f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform2i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform2f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform3i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform4i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform4f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    // ShaderAttributeArray methods

    ShaderError setUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform1iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform1fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform2iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform2fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform3iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setGLSLUniform4iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniform4fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setGLSLUniformMatrix3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
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
    ShaderError createShader(long ctx, ShaderRetained shader, long[] shaderIdArr) {	
	  return Pipeline.getPipeline().createGLSLShader(ctx, shader.shaderType, shaderIdArr);
    }
    
    /**
     * Method to destroy the native shader.
     */
    ShaderError destroyShader(long ctx, long shaderId) {
	return Pipeline.getPipeline().destroyGLSLShader(ctx, shaderId);
    }
    
    /**
     * Method to compile the native shader.
     */
    ShaderError compileShader(long ctx, long shaderId, String source) {
        return Pipeline.getPipeline().compileGLSLShader(ctx, shaderId, source );
    }

    /**
     * Method to create the native shader program.
     */
    ShaderError createShaderProgram(long ctx, long[] shaderProgramIdArr) {
	    return Pipeline.getPipeline().createGLSLShaderProgram(ctx, shaderProgramIdArr);  
    }

    /**
     * Method to destroy the native shader program.
     */
    ShaderError destroyShaderProgram(long ctx, long shaderProgramId) {
        return Pipeline.getPipeline().destroyGLSLShaderProgram(ctx, shaderProgramId);
    }

    /**
     * Method to link the native shader program.
     */
    ShaderError linkShaderProgram(long ctx, long shaderProgramId, long[] shaderIds) {
        return Pipeline.getPipeline().linkGLSLShaderProgram(ctx, shaderProgramId, shaderIds);
    }
 
    ShaderError bindVertexAttrName(long ctx, long shaderProgramId, String attrName, int attrIndex) {
        return Pipeline.getPipeline().bindGLSLVertexAttrName(ctx, shaderProgramId, attrName, attrIndex);
    }

    void lookupVertexAttrNames(long ctx, long shaderProgramId, String[] attrNames, boolean[] errArr) {
        // This method is a no-op for GLSL
    }

    void lookupShaderAttrNames(long ctx, long shaderProgramId,
            String[] attrNames, AttrNameInfo[] attrNameInfoArr) {

        int numAttrNames = attrNames.length;
        
        long[] locArr = new long[numAttrNames];
        int[] typeArr = new int[numAttrNames];
        int[] sizeArr = new int[numAttrNames]; // currently unused
        boolean[] isArrayArr = new boolean[numAttrNames];

        // Initialize loc array to -1 (indicating no location)
        for (int i = 0; i < numAttrNames; i++) {
            locArr[i] = -1;
        }

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
    ShaderError enableShaderProgram(long ctx, long shaderProgramId) {
	return Pipeline.getPipeline().useGLSLShaderProgram(ctx, shaderProgramId);
    }
	
    /**
     * Method to disable the native shader program.
     */
    ShaderError disableShaderProgram(long ctx) {
	return Pipeline.getPipeline().useGLSLShaderProgram(ctx, 0);
    }


}
