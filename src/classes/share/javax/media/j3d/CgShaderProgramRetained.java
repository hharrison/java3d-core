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

    native ShaderError setUniform1i(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int value);
    
    native ShaderError setUniform1f(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float value);
    
    native ShaderError setUniform2i(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    native ShaderError setUniform2f(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);
    
    native ShaderError setUniform3i(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    native ShaderError setUniform3f(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);    
    
    native ShaderError setUniform4i(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    native ShaderError setUniform4f(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);    
    
    native ShaderError setUniformMatrix3f(long ctx,
					   long shaderProgramId,
				           long uniformLocation,
					   float[] value);

    native ShaderError setUniformMatrix4f(long ctx,
					   long shaderProgramId,
			         	   long uniformLocation,
					   float[] value);

    // ShaderAttributeArray methods

    native ShaderError setUniform1iArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    native ShaderError setUniform1fArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);
    
    native ShaderError setUniform2iArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    native ShaderError setUniform2fArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);
    
    native ShaderError setUniform3iArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    native ShaderError setUniform3fArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);    
    
    native ShaderError setUniform4iArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    native ShaderError setUniform4fArray(long ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);    
    
    native ShaderError setUniformMatrix3fArray(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int numElements,
					    float[] value);

    native ShaderError setUniformMatrix4fArray(long ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int numElements,
					    float[] value);


    
    /* New native interfaces */
    private native ShaderError createNativeShader(long ctx, int shaderType, long[] shaderId);
    private native ShaderError destroyNativeShader(long ctx, long shaderId);
    private native ShaderError compileNativeShader(long ctx, long shaderId, String program);

    private native ShaderError createNativeShaderProgram(long ctx, long[] shaderProgramId);
    private native ShaderError destroyNativeShaderProgram(long ctx, long shaderProgramId);
    private native ShaderError linkNativeShaderProgram(long ctx, long shaderProgramId,
						       long[] shaderId);
    private native void lookupNativeVertexAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr);
    private native void lookupNativeShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    private native ShaderError useShaderProgram(long ctx, long shaderProgramId);

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
    ShaderError createShader(long ctx, ShaderRetained shader, long[] shaderIdArr) {	
	  return  createNativeShader(ctx, shader.shaderType, shaderIdArr);
    }
    
    /**
     * Method to destroy the native shader.
     */
    ShaderError destroyShader(long ctx, long shaderId) {
	return destroyNativeShader(ctx, shaderId);
    }
    
    /**
     * Method to compile the native shader.
     */
    ShaderError compileShader(long ctx, long shaderId, String source) {
        return compileNativeShader(ctx, shaderId, source );
    }

    /**
     * Method to create the native shader program.
     */
    ShaderError createShaderProgram(long ctx, long[] shaderProgramIdArr) {
	    return createNativeShaderProgram(ctx, shaderProgramIdArr);  
    }

    /**
     * Method to destroy the native shader program.
     */
    ShaderError destroyShaderProgram(long ctx, long shaderProgramId) {
        return destroyNativeShaderProgram(ctx, shaderProgramId);
    }

    /**
     * Method to link the native shader program.
     */
    ShaderError linkShaderProgram(long ctx, long shaderProgramId, long[] shaderIds) {
        return linkNativeShaderProgram(ctx, shaderProgramId, shaderIds);
    }
 
    ShaderError bindVertexAttrName(long ctx, long shaderProgramId, String attrName, int attrIndex) {
        // This is a no-op for Cg
        return null;
    }

    void lookupVertexAttrNames(long ctx, long shaderProgramId, String[] attrNames, boolean[] errArr) {
        lookupNativeVertexAttrNames(ctx, shaderProgramId, attrNames.length, attrNames, errArr);
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

        lookupNativeShaderAttrNames(ctx, shaderProgramId,
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
    ShaderError enableShaderProgram(long ctx, long shaderProgramId) {
	return useShaderProgram(ctx, shaderProgramId);
    }
	
    /**
     * Method to disable the native shader program.
     */
    ShaderError disableShaderProgram(long ctx) {
	return useShaderProgram(ctx, 0);
    }


}
