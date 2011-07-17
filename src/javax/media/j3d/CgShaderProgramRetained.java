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
	// System.err.println("CgShaderProgramRetained : createMirrorObject");
        // This method should only call by setLive().
	if (mirror == null) {
	    CgShaderProgramRetained  mirrorCgSP = new CgShaderProgramRetained();	    
	    mirror = mirrorCgSP;
	}
	initMirrorObject();
    }

    // ShaderAttributeValue methods

    ShaderError setUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {

        return Pipeline.getPipeline().setCgUniform1i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {

        return Pipeline.getPipeline().setCgUniform1f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform2i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform2f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform3i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {

        return Pipeline.getPipeline().setCgUniform4i(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniform4f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix3f(ctx,
                shaderProgramId,
                uniformLocation,
                value);
    }

    ShaderError setUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {

        return Pipeline.getPipeline().setCgUniformMatrix4f(ctx,
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

        return Pipeline.getPipeline().setCgUniform1iArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform1fArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform2iArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform2fArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform3iArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform3fArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform4iArray(ctx,
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

        return Pipeline.getPipeline().setCgUniform4fArray(ctx,
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

        return Pipeline.getPipeline().setCgUniformMatrix3fArray(ctx,
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
    ShaderError createShader(Context ctx, ShaderRetained shader, ShaderId[] shaderIdArr) {	
	  return Pipeline.getPipeline().createCgShader(ctx, shader.shaderType, shaderIdArr);
    }
    
    /**
     * Method to destroy the native shader.
     */
    ShaderError destroyShader(Context ctx, ShaderId shaderId) {
	return Pipeline.getPipeline().destroyCgShader(ctx, shaderId);
    }
    
    /**
     * Method to compile the native shader.
     */
    ShaderError compileShader(Context ctx, ShaderId shaderId, String source) {
        return Pipeline.getPipeline().compileCgShader(ctx, shaderId, source );
    }

    /**
     * Method to create the native shader program.
     */
    ShaderError createShaderProgram(Context ctx, ShaderProgramId[] shaderProgramIdArr) {
	    return Pipeline.getPipeline().createCgShaderProgram(ctx, shaderProgramIdArr);  
    }

    /**
     * Method to destroy the native shader program.
     */
    ShaderError destroyShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return Pipeline.getPipeline().destroyCgShaderProgram(ctx, shaderProgramId);
    }

    /**
     * Method to link the native shader program.
     */
    ShaderError linkShaderProgram(Context ctx, ShaderProgramId shaderProgramId, ShaderId[] shaderIds) {
        return Pipeline.getPipeline().linkCgShaderProgram(ctx, shaderProgramId, shaderIds);
    }
 
    ShaderError bindVertexAttrName(Context ctx, ShaderProgramId shaderProgramId, String attrName, int attrIndex) {
        // This is a no-op for Cg
        return null;
    }

    void lookupVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId, String[] attrNames, boolean[] errArr) {
        Pipeline.getPipeline().lookupCgVertexAttrNames(ctx, shaderProgramId, attrNames.length, attrNames, errArr);
    }

    void lookupShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            String[] attrNames, AttrNameInfo[] attrNameInfoArr) {

        int numAttrNames = attrNames.length;
        
        ShaderAttrLoc[] locArr = new ShaderAttrLoc[numAttrNames];
        int[] typeArr = new int[numAttrNames];
        int[] sizeArr = new int[numAttrNames]; // currently unused
        boolean[] isArrayArr = new boolean[numAttrNames];

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
    ShaderError enableShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
	return Pipeline.getPipeline().useCgShaderProgram(ctx, shaderProgramId);
    }
	
    /**
     * Method to disable the native shader program.
     */
    ShaderError disableShaderProgram(Context ctx) {
	return Pipeline.getPipeline().useCgShaderProgram(ctx, null);
    }


}
