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
 * The CgShaderProgram object is a concrete implementation of a
 * ShaderProgram node component for NVIDIA's Cg shader language.
 */

class CgShaderProgramRetained extends ShaderProgramRetained {
    
    /**
     * Constructs a Cg shader program node component.
     */
    CgShaderProgramRetained() {
    }

    synchronized void createMirrorObject() {
	// System.out.println("CgShaderProgramRetained : createMirrorObject");
        // This method should only call by setLive().
	if (mirror == null) {
	    CgShaderProgramRetained  mirrorCgSP = new CgShaderProgramRetained();	    
	    mirror = mirrorCgSP;
	}
	initMirrorObject();
    }

    // ShaderAttributeValue methods

    ShaderError setUniform1i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int value) {

        return Pipeline.getPipeline().setCgUniform1i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform1f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float value) {

        return Pipeline.getPipeline().setCgUniform1f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform2i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform2f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform3i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform4i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform4f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    // ShaderAttributeArray methods

    ShaderError setUniform1iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform1iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform1fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform1fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform2iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform2fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform2fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform3iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform4iArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniform4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform4fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix3fArray(ctx,
                shaderProgramId,
                uniformLocation,
                numElements,
                value);
    }

    ShaderError setUniformMatrix4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix4fArray(ctx,
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
        return cv.shadingLanguageCg;
    }

    /**
     * Method to create the native shader.
     */
    ShaderError createShader(Context ctx, ShaderRetained shader, long[] shaderIdArr) {	
	  return Pipeline.getPipeline().createCgShader(ctx, shader.shaderType, shaderIdArr);
    }
    
    /**
     * Method to destroy the native shader.
     */
    ShaderError destroyShader(Context ctx, long shaderId) {
	return Pipeline.getPipeline().destroyCgShader(ctx, shaderId);
    }
    
    /**
     * Method to compile the native shader.
     */
    ShaderError compileShader(Context ctx, long shaderId, String source) {
        return Pipeline.getPipeline().compileCgShader(ctx, shaderId, source );
    }

    /**
     * Method to create the native shader program.
     */
    ShaderError createShaderProgram(Context ctx, long[] shaderProgramIdArr) {
	    return Pipeline.getPipeline().createCgShaderProgram(ctx, shaderProgramIdArr);  
    }

    /**
     * Method to destroy the native shader program.
     */
    ShaderError destroyShaderProgram(Context ctx, long shaderProgramId) {
        return Pipeline.getPipeline().destroyCgShaderProgram(ctx, shaderProgramId);
    }

    /**
     * Method to link the native shader program.
     */
    ShaderError linkShaderProgram(Context ctx, long shaderProgramId, long[] shaderIds) {
        return Pipeline.getPipeline().linkCgShaderProgram(ctx, shaderProgramId, shaderIds);
    }
 
    ShaderError bindVertexAttrName(Context ctx, long shaderProgramId, String attrName, int attrIndex) {
        // This is a no-op for Cg
        return null;
    }

    void lookupVertexAttrNames(Context ctx, long shaderProgramId, String[] attrNames, boolean[] errArr) {
        Pipeline.getPipeline().lookupCgVertexAttrNames(ctx, shaderProgramId, attrNames.length, attrNames, errArr);
    }

    void lookupShaderAttrNames(Context ctx, long shaderProgramId,
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

        Pipeline.getPipeline().lookupCgShaderAttrNames(ctx, shaderProgramId,
                numAttrNames, attrNames, locArr, typeArr, sizeArr, isArrayArr);

        for (int i = 0; i < numAttrNames; i++) {
            attrNameInfoArr[i] = new AttrNameInfo();
            attrNameInfoArr[i].setLocation(locArr[i]);
            attrNameInfoArr[i].setArray(isArrayArr[i]);
            attrNameInfoArr[i].setType(typeArr[i]);
            /*
            System.err.println(attrNames[i] +
                    " : loc = " + locArr[i] +
                    ", type = " + typeArr[i] +
                    ", isArray = " + isArrayArr[i] +
                    ", size = " + sizeArr[i]);
            */
        }
    }

    /**
     * Method to enable the native shader program.
     */
    ShaderError enableShaderProgram(Context ctx, long shaderProgramId) {
	return Pipeline.getPipeline().useCgShaderProgram(ctx, shaderProgramId);
    }
	
    /**
     * Method to disable the native shader program.
     */
    ShaderError disableShaderProgram(Context ctx) {
	return Pipeline.getPipeline().useCgShaderProgram(ctx, 0);
    }


}
