/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.*;

/**
 * The ShaderRetained object is the abstract base class for programmable
 * shader code. Currently, only text-based source code shaders are
 * supported, so the only subclass of Shader is SourceCodeShader. We
 * leave open the possibility for binary (object code) shaders in the
 * future.
 */
abstract class ShaderRetained extends NodeComponentRetained {
    int shadingLanguage;
    int shaderType;

    // Each element in the array corresponds to a unique renderer if shared
    // context or a unique canvas otherwise.
    // shaderId use by native code. One per Canvas.
    ShaderData[] shaderData;

    // Flag indicating whether a COMPILE_ERROR has occurred for this shader
    // object.  It is set in updateNative to indicate that the compileShader
    // operation failed. It is cleared in setLive or clearLive.
    // TODO KCR: Add code to clear this in setLive or clearLive
    boolean compileErrorOccurred = false;

    // need to synchronize access from multiple rendering threads 
    Object resourceLock = new Object();

    void initializeShader(int shadingLanguage, int shaderType) {
	this.shadingLanguage = shadingLanguage;
	this.shaderType = shaderType;
    }

    int getShadingLanguage() {
	return shadingLanguage;
    }

    int getShaderType() {
	return shaderType;
    }
 
    void setLive(boolean inBackgroundGroup, int refCount) {
	// System.err.println("SourceCodeShaderRetained.setLive()");
	super.setLive(inBackgroundGroup, refCount);
    }

    void clearLive(int refCount) {
	// System.err.println("SourceCodeShaderRetained.clearLive()");
	super.clearLive(refCount);
    }

     /**
      * Shader object doesn't really have mirror object.
      * But it's using the updateMirrorObject interface to propagate
      * the changes to the users
      */
     synchronized void updateMirrorObject(int component, Object value) {
	System.err.println("Shader.updateMirrorObject not implemented yet!");
     }

    void handleFrequencyChange(int bit) {
	System.err.println("Shader.handleFrequencyChange not implemented yet!");
    }

    void createShaderData(int cvRdrIndex, long ctxTimeStamp) {
        // Create shaderProgram resources if it has not been done.
        synchronized(resourceLock) {
            if (shaderData == null) {
                shaderData = new ShaderData[cvRdrIndex+1];
            } else if (shaderData.length <= cvRdrIndex) {
                ShaderData[] tempSData = new ShaderData[cvRdrIndex+1];
                
                System.arraycopy(shaderData, 0,
                        tempSData, 0,
                        shaderData.length);
                shaderData = tempSData;
            }

            if (shaderData[cvRdrIndex] == null) {
                shaderData[cvRdrIndex] = new ShaderData();
            } else if (shaderData[cvRdrIndex].getCtxTimeStamp() != ctxTimeStamp) {
                // Issue 378 - reset the shader data for this canvas / renderer
                // if the context has been recreated
                shaderData[cvRdrIndex].reset();
            }
            shaderData[cvRdrIndex].setCtxTimeStamp(ctxTimeStamp);
        }
    }


    // Per-context (canvas) data for this shader
    class ShaderData extends Object {

        // Issue 378 - time stamp of context creation for this canvas
        private long ctxTimeStamp;

        // shaderId use by native code
        private ShaderId shaderId = null;

        // indicated that the shader has been compiled for this canvas
        private boolean compiled = false;

        /** ShaderProgramData Constructor */
        ShaderData() {
        }

        void reset() {
            ctxTimeStamp = 0L;
            shaderId = null;
            compiled = false;
        }

        long getCtxTimeStamp() {
            return ctxTimeStamp;
        }

        void setCtxTimeStamp(long ctxTimeStamp) {
            this.ctxTimeStamp = ctxTimeStamp;
        }

        ShaderId getShaderId() {
            return shaderId;
        }

        void setShaderId(ShaderId shaderId) {
            this.shaderId = shaderId;
        }

        boolean isCompiled() {
            return compiled;
        }

        void setCompiled(boolean compiled) {
            this.compiled = compiled;
        }

    }

}

