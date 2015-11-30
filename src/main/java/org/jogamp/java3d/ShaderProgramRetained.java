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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * The ShaderProgramRetained object is a component object of an AppearanceRetained
 * object that defines the shader properties used when programmable shader is
 * enabled. ShaderProgramRetained object is an abstract class. All shader program
 * objects must be created as either a GLSLShaderProgramRetained object or a
 * CgShaderProgramRetained object.
 */
abstract class ShaderProgramRetained extends NodeComponentRetained {

    // Each element in the array corresponds to a unique renderer if shared
    // context or a unique canvas otherwise.
    protected ShaderProgramData shaderProgramData[];

    // Flag indicating whether an UNSUPPORTED_LANGUAGE_ERROR has
    // already been reported for this shader program object.  It is
    // set in verifyShaderProgram and cleared in setLive or clearLive.
    // TODO KCR: Add code to clear this in setLive or clearLive
    private boolean unsupportedErrorReported = false;

    // Flag indicating whether a LINK_ERROR has occurred for this shader program
    // object.  It is set in updateNative to indicate that the linkShaderProgram
    // operation failed. It is cleared in setLive or clearLive.
    // TODO KCR: Add code to clear this in setLive or clearLive
    private boolean linkErrorOccurred = false;

    // an array of shaders used by this shader program
    protected ShaderRetained[] shaders;

    // an array of vertex attribute names
    protected String[] vertexAttrNames;

    // an array of (uniform) shader attribute names
    protected String[] shaderAttrNames;

// Set of ShaderAttribute objects for which we have already reported an error
private HashSet<ShaderAttribute> shaderAttrErrorSet = null;

    // need to synchronize access from multiple rendering threads
    Object resourceLock = new Object();

    // Package-scope default constructor
    ShaderProgramRetained() {
    }

    /**
     * Sets the vertex attribute names array for this ShaderProgram
     * object. Each element in the array specifies the shader
     * attribute name that is bound to the corresponding numbered
     * vertex attribute within a GeometryArray object that uses this
     * shader program. Array element 0 specifies the name of
     * GeometryArray vertex attribute 0, array element 1 specifies the
     * name of GeometryArray vertex attribute 1, and so forth.
     * The array of names may be null or empty (0 length), but the
     * elements of the array must be non-null.
     *
     * @param vertexAttrNames array of vertex attribute names for this
     * shader program. A copy of this array is made.
     */
    void setVertexAttrNames(String[] vertexAttrNames) {
        if (vertexAttrNames == null) {
            this.vertexAttrNames = null;
        }
        else {
		this.vertexAttrNames = vertexAttrNames.clone();
        }
    }


    /**
     * Retrieves the vertex attribute names array from this
     * ShaderProgram object.
     *
     * @return a copy of this ShaderProgram's array of vertex attribute names.
     */
    String[] getVertexAttrNames() {

        if (vertexAttrNames == null) {
	    return null;
	}

	return vertexAttrNames.clone();

    }


    /**
     * Sets the shader attribute names array for this ShaderProgram
     * object. Each element in the array specifies a shader
     * attribute name that may be set via a ShaderAttribute object.
     * Only those attributes whose names that appear in the shader
     * attribute names array can be set for a given shader program.
     * The array of names may be null or empty (0 length), but the
     * elements of the array must be non-null.
     *
     * @param shaderAttrNames array of shader attribute names for this
     * shader program. A copy of this array is made.
     */
    void setShaderAttrNames(String[] shaderAttrNames) {
        if (shaderAttrNames == null) {
            this.shaderAttrNames = null;
        }
        else {
		this.shaderAttrNames = shaderAttrNames.clone();
        }
    }


    /**
     * Retrieves the shader attribute names array from this
     * ShaderProgram object.
     *
     * @return a copy of this ShaderProgram's array of shader attribute names.
     */

    String[] getShaderAttrNames() {

        if (shaderAttrNames == null) {
	    return null;
	}

	return shaderAttrNames.clone();

    }



    /**
     * Copies the specified array of shaders into this shader
     * program. This method makes a shallow copy of the array. The
     * array of shaders may be null or empty (0 length), but the
     * elements of the array must be non-null. The shading
     * language of each shader in the array must match the
     * subclass. Subclasses may impose additional restrictions.
     *
     * @param shaders array of Shader objects to be copied into this
     * ShaderProgram
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalArgumentException if the shading language of
     * any shader in the shaders array doesn't match the type of the
     * subclass.
     */
    void setShaders(Shader[] shaders) {

	if (shaders == null) {
	    this.shaders = null;
	    return;
	}

	this.shaders = new ShaderRetained[shaders.length];

	// Copy vertex and fragment shader
	for (int i = 0; i < shaders.length; i++) {
	    this.shaders[i] = (ShaderRetained)shaders[i].retained;
	}

    }

    /**
     * Retrieves the array of shaders from this shader program. A
     * shallow copy of the array is returned. The return value may
     * be null.
     *
     * @return a copy of this ShaderProgram's array of Shader objects
     *
     */
    Shader[] getShaders() {

	if (shaders == null) {
	    return null;
	} else {
	    Shader shads[] =
		new Shader[shaders.length];
	    for (int i = 0; i < shaders.length; i++) {
		if (shaders[i] != null) {
		    shads[i] = (Shader) shaders[i].source;
		} else {
		    shads[i] = null;
		}
	    }
	    return shads;
	}
    }

    /**
     * Method to create the native shader.
     */
    abstract ShaderError createShader(Context ctx, ShaderRetained shader, ShaderId[] shaderIdArr);

    /**
     * Method to destroy the native shader.
     */
    abstract ShaderError destroyShader(Context ctx, ShaderId shaderId);

    /**
     * Method to compile the native shader.
     */
    abstract ShaderError compileShader(Context ctx, ShaderId shaderId, String source);

    /**
     * Method to create the native shader program.
     */
    abstract ShaderError createShaderProgram(Context ctx, ShaderProgramId[] shaderProgramIdArr);

    /**
     * Method to destroy the native shader program.
     */
    abstract ShaderError destroyShaderProgram(Context ctx, ShaderProgramId shaderProgramId);

    /**
     * Method to link the native shader program.
     */
    abstract ShaderError linkShaderProgram(Context ctx, ShaderProgramId shaderProgramId, ShaderId[] shaderIds);

    /**
     * Method to bind a vertex attribute name to the specified index.
     */
    abstract ShaderError bindVertexAttrName(Context ctx, ShaderProgramId shaderProgramId, String attrName, int attrIndex);

    /**
     * Method to lookup a list of (uniform) shader attribute names and return
     * information about the attributes.
     */
    abstract void lookupShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId, String[] attrNames, AttrNameInfo[] attrNameInfoArr);

    /*
     * Method to lookup a list of vertex attribute names.
     */
    abstract void lookupVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId, String[] attrNames, boolean[] errArr);

    /**
     * Method to use the native shader program.
     */
    abstract ShaderError enableShaderProgram(Context ctx, ShaderProgramId shaderProgramId);

    /**
     * Method to disable the native shader program.
     */
    abstract ShaderError disableShaderProgram(Context ctx);

    // ShaderAttributeValue methods

    abstract ShaderError setUniform1i(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int value);

    abstract ShaderError setUniform1f(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    float value);

    abstract ShaderError setUniform2i(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int[] value);

    abstract ShaderError setUniform2f(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    float[] value);

    abstract ShaderError setUniform3i(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int[] value);

    abstract ShaderError setUniform3f(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    float[] value);

    abstract ShaderError setUniform4i(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int[] value);

    abstract ShaderError setUniform4f(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    float[] value);

    abstract ShaderError setUniformMatrix3f(Context ctx,
					   ShaderProgramId shaderProgramId,
				           ShaderAttrLoc uniformLocation,
					   float[] value);

    abstract ShaderError setUniformMatrix4f(Context ctx,
					   ShaderProgramId shaderProgramId,
			         	   ShaderAttrLoc uniformLocation,
					   float[] value);


    // ShaderAttributeArray methods

    abstract ShaderError setUniform1iArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      int[] value);

    abstract ShaderError setUniform1fArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      float[] value);

    abstract ShaderError setUniform2iArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      int[] value);

    abstract ShaderError setUniform2fArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      float[] value);

    abstract ShaderError setUniform3iArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      int[] value);

    abstract ShaderError setUniform3fArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      float[] value);

    abstract ShaderError setUniform4iArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      int[] value);

    abstract ShaderError setUniform4fArray(Context ctx,
				      ShaderProgramId shaderProgramId,
				      ShaderAttrLoc uniformLocation,
				      int numElements,
				      float[] value);

    abstract ShaderError setUniformMatrix3fArray(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int numElements,
					    float[] value);

    abstract ShaderError setUniformMatrix4fArray(Context ctx,
					    ShaderProgramId shaderProgramId,
					    ShaderAttrLoc uniformLocation,
					    int numElements,
					    float[] value);


    /**
     * Method to return a flag indicating whether this
     * ShaderProgram is supported on the specified Canvas.
     */
    abstract boolean isSupported(Canvas3D cv);


    @Override
    void setLive(boolean backgroundGroup, int refCount) {

	// System.err.println("ShaderProgramRetained.setLive()");

	if (shaders != null) {
	    for (int i = 0; i < shaders.length; i++){
		shaders[i].setLive(backgroundGroup, refCount);
	    }
	}

	super.doSetLive(backgroundGroup, refCount);
        super.markAsLive();

    }

    @Override
    void clearLive(int refCount) {

        // System.err.println("ShaderProgramRetained.clearLive()");

	super.clearLive(refCount);

	if (shaders != null) {
	    for (int i = 0; i < shaders.length; i++) {
		shaders[i].clearLive(refCount);
	    }
	}
    }

    /**
     * Method to enable the native shader program.
     */
    private ShaderError enableShaderProgram(Canvas3D cv, int cvRdrIndex) {
        assert(cvRdrIndex >= 0);
	synchronized(resourceLock) {
            return enableShaderProgram(cv.ctx,
				       shaderProgramData[cvRdrIndex].getShaderProgramId());
	}

    }

    /**
     * Method to disable the native shader program.
     */
    private ShaderError disableShaderProgram(Canvas3D cv) {
        return disableShaderProgram(cv.ctx);
    }

    /**
     * Initializes a mirror object.
     */
    @Override
    synchronized void initMirrorObject() {

        // Create mirror copy of shaders
        if (this.shaders == null) {
            ((ShaderProgramRetained)mirror).shaders = null;
        }
        else {
            ((ShaderProgramRetained)mirror).shaders = new ShaderRetained[this.shaders.length];
            // Copy vertex and fragment shader
            for (int i = 0; i < this.shaders.length; i++) {
                ((ShaderProgramRetained)mirror).shaders[i] =
                        (ShaderRetained)this.shaders[i].mirror;
            }
        }
        ((ShaderProgramRetained)mirror).shaderProgramData = null;

        // Create mirror copy of vertex attribute names
        if (this.vertexAttrNames == null) {
            ((ShaderProgramRetained)mirror).vertexAttrNames = null;
        }
        else {
		((ShaderProgramRetained)mirror).vertexAttrNames = this.vertexAttrNames.clone();
        }

        // Create mirror copy of shader attribute names
        if (this.shaderAttrNames == null) {
            ((ShaderProgramRetained)mirror).shaderAttrNames = null;
        }
        else {
		((ShaderProgramRetained)mirror).shaderAttrNames = this.shaderAttrNames.clone();
        }

        // Clear shader attribute error set
        ((ShaderProgramRetained)mirror).shaderAttrErrorSet = null;
    }

    /**
     * Update the "component" field of the mirror object with the  given "value"
     */
    @Override
    synchronized void updateMirrorObject(int component, Object value) {

	// ShaderProgram can't be modified once it is live.
	assert(false);
	System.err.println("ShaderProgramRetained : updateMirrorObject NOT IMPLEMENTED YET");
    }

    /**
     * Method to create a ShaderProgramData object for the specified
     * canvas/renderer if it doesn't already exist.
     *
     * Issue 378 : reset the ShaderProgramData object if the context
     * has been recreated for the particular canvas / renderer.
     */
    private void createShaderProgramData(int cvRdrIndex, long ctxTimeStamp) {
        // Create shaderProgram resources if it has not been done.
        synchronized(resourceLock) {
	    if(shaderProgramData == null) {
                // We rely on Java to initial the array elements to null.
                shaderProgramData = new ShaderProgramData[cvRdrIndex+1];
	    }
	    else if(shaderProgramData.length <= cvRdrIndex) {
                // We rely on Java to initial the array elements to null.
		ShaderProgramData[] tempSPData = new ShaderProgramData[cvRdrIndex+1];
                System.arraycopy(shaderProgramData, 0,
                        tempSPData, 0,
                        shaderProgramData.length);
                shaderProgramData = tempSPData;
	    }

            if(shaderProgramData[cvRdrIndex] == null) {
                shaderProgramData[cvRdrIndex] = new ShaderProgramData();
            } else if (shaderProgramData[cvRdrIndex].getCtxTimeStamp() != ctxTimeStamp) {
                // Issue 378 - reset the shader program data for this canvas / renderer
                // if the context has been recreated
                shaderProgramData[cvRdrIndex].reset();
            }
            shaderProgramData[cvRdrIndex].setCtxTimeStamp(ctxTimeStamp);
        }
    }

    /**
     * Method to create the native shader program. We must already have
     * called createShaderProgramData for this cvRdrIndex.
     */
    private ShaderError createShaderProgram(Canvas3D cv, int cvRdrIndex) {
        // Create shaderProgram resources if it has not been done.
        synchronized(resourceLock) {
            assert shaderProgramData[cvRdrIndex].getShaderProgramId() == null;

            ShaderProgramId[] spIdArr = new ShaderProgramId[1];
            ShaderError err = createShaderProgram(cv.ctx, spIdArr);
            if(err != null) {
                return err;
            }
            shaderProgramData[cvRdrIndex].setShaderProgramId(spIdArr[0]);
        }

        return null;
    }

    /**
     * Method to link the native shader program.
     */
    private ShaderError linkShaderProgram(Canvas3D cv, int cvRdrIndex,
					  ShaderRetained[] shaders) {
	synchronized(resourceLock) {
            ShaderId[] shaderIds = new ShaderId[shaders.length];
	    for(int i=0; i<shaders.length; i++) {
                synchronized(shaders[i]) {
                    shaderIds[i] = shaders[i].shaderData[cvRdrIndex].getShaderId();
                }
	    }
	    ShaderError err =
		linkShaderProgram(cv.ctx,
				  shaderProgramData[cvRdrIndex].getShaderProgramId(),
				  shaderIds);
            if(err != null) {
                return err;
            }
	    shaderProgramData[cvRdrIndex].setLinked(true);
	}

	return null;
    }


    private ShaderError bindVertexAttrName(Canvas3D cv, int cvRdrIndex, String attrName, int attrIndex) {
        assert(attrName != null);
        synchronized(resourceLock) {
            ShaderProgramId shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();
//            System.err.println("attrName = " + attrName);
            ShaderError err = bindVertexAttrName(cv.ctx, shaderProgramId, attrName, attrIndex);
            if (err != null) {
                return err;
            }
        }
        return null;
    }

    private void lookupVertexAttrNames(Canvas3D cv, int cvRdrIndex, String[] attrNames) {
        synchronized(resourceLock) {
            ShaderProgramId shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();

            boolean[] errArr = new boolean[attrNames.length];
            lookupVertexAttrNames(cv.ctx, shaderProgramId, attrNames, errArr);

            for (int i = 0; i < attrNames.length; i++) {
                // Report non-fatal error if detected
                if (errArr[i]) {
                    String errMsg = "Vertex Attribute name lookup failed: " + attrNames[i];
                    ShaderError err = new ShaderError(ShaderError.VERTEX_ATTRIBUTE_LOOKUP_ERROR, errMsg);
                    err.setShaderProgram((ShaderProgram)this.source);
                    err.setCanvas3D(cv);
                    notifyErrorListeners(cv, err);
                }
            }
        }
    }


    private void lookupShaderAttrNames(Canvas3D cv, int cvRdrIndex, String[] attrNames) {
        synchronized(resourceLock) {
            ShaderProgramId shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();

            AttrNameInfo[] attrNameInfoArr = new AttrNameInfo[attrNames.length];
            lookupShaderAttrNames(cv.ctx, shaderProgramId, attrNames, attrNameInfoArr);

            for (int i = 0; i < attrNames.length; i++) {
                shaderProgramData[cvRdrIndex].setAttrNameInfo(attrNames[i], attrNameInfoArr[i]);

                // Report non-fatal error if location is invalid
                if (attrNameInfoArr[i].getLocation() == null) {
                    String errMsg = "Attribute name lookup failed: " + attrNames[i];
                    ShaderError err = new ShaderError(ShaderError.SHADER_ATTRIBUTE_LOOKUP_ERROR, errMsg);
                    err.setShaderProgram((ShaderProgram)this.source);
                    err.setCanvas3D(cv);
                    notifyErrorListeners(cv, err);
                }
            }
        }
    }


    /**
     * Method to return the shaderProgram data for the specified canvas or renderer
     */
    private ShaderProgramData getShaderProgramData(int cvRdrIndex) {
        synchronized(resourceLock) {
            return shaderProgramData[cvRdrIndex];
        }
    }

    /**
     * Method to create the native shader. We must already have
     * called createShaderData for this cvRdrIndex.
     */
    private ShaderError createShader(Canvas3D cv, int cvRdrIndex, ShaderRetained shader) {

        // Create shaderProgram resources if it has not been done.
        synchronized(shader.resourceLock) {
            if (shader.shaderData[cvRdrIndex].getShaderId() != null) {
                // We have already created the shaderId for this Canvas.
                return null;
            }

            ShaderId[] shaderIdArr = new ShaderId[1];
            ShaderError err = createShader(cv.ctx, shader, shaderIdArr);
            if(err != null) {
                return err;
            }
            shader.shaderData[cvRdrIndex].setShaderId(shaderIdArr[0]);
        }
        return null;
    }

    /**
     * Method to compile the native shader.
     */
    private ShaderError compileShader(Canvas3D cv, int cvRdrIndex, ShaderRetained shader) {

        synchronized(shader.resourceLock) {

            if (shader.shaderData[cvRdrIndex].isCompiled()) {
                // We have already compiled the shaderId for this Canvas.
                return null;
            }

	    String source = ((SourceCodeShaderRetained)shader).getShaderSource();
            ShaderError err = compileShader(cv.ctx, shader.shaderData[cvRdrIndex].getShaderId(), source);
            if(err != null) {
                return err;
            }
            shader.shaderData[cvRdrIndex].setCompiled(true);
        }

        return null;
    }

    /**
     * Send a message to the notification thread, which will call the
     * shader error listeners.
     */
    void notifyErrorListeners(Canvas3D cv, ShaderError err) {
        J3dNotification notification = new J3dNotification();
        notification.type = J3dNotification.SHADER_ERROR;
        notification.universe = cv.view.universe;
        notification.args[0] = err;
        VirtualUniverse.mc.sendNotification(notification);
    }


    /**
     * This method checks whether this ShaderProgram is supported on
     * the specified Canvas. If it isn't supported, it will report a
     * ShaderError unless an error has already been reported for this
     * shader program.
     */
    private boolean verifyShaderProgramSupported(Canvas3D cv) {
        boolean supported = isSupported(cv);
        if (!supported && !unsupportedErrorReported) {
            String errorMsg = J3dI18N.getString("ShaderProgramRetained0");
            ShaderError err = new ShaderError(ShaderError.UNSUPPORTED_LANGUAGE_ERROR, errorMsg);
            err.setShaderProgram((ShaderProgram)this.source);
            err.setCanvas3D(cv);
            notifyErrorListeners(cv, err);
            unsupportedErrorReported = true;
        }
        return supported;
    }

    /**
     * Method to destroy the native shader.
     */
    void destroyShader(Canvas3D cv, int cvRdrIndex, ShaderRetained shader) {
        if (!verifyShaderProgramSupported(cv)) {
            return;
        }

        // Destroy shader resource if it exists
        synchronized(shader.resourceLock) {
            // Check whether an entry in the shaderData array has been allocated
            if (shader.shaderData == null ||
                    shader.shaderData.length <= cvRdrIndex ||
                    shader.shaderData[cvRdrIndex] == null) {
                return;
            }

            // Nothing to do if the shaderId is null
            if (shader.shaderData[cvRdrIndex].getShaderId() == null) {
                return;
            }

            // Destroy the native resource and set the ID to null for this canvas/renderer
            // Ignore any possible shader error, because there is no meaningful way to report it
            destroyShader(cv.ctx, shader.shaderData[cvRdrIndex].getShaderId());
            shader.shaderData[cvRdrIndex].reset();
        }
    }


    /**
     * Method to destroy the native shader program.
     */
    void destroyShaderProgram(Canvas3D cv, int cvRdrIndex) {
        if (!verifyShaderProgramSupported(cv)) {
            return;
        }

        // Destroy shaderProgram resource if it exists
        synchronized(resourceLock) {
            assert(shaderProgramData != null &&
                   shaderProgramData.length > cvRdrIndex &&
                   shaderProgramData[cvRdrIndex] != null);

//            // Check whether an entry in the shaderProgramData array has been allocated
//            if (shaderProgramData == null ||
//                    shaderProgramData.length <= cvRdrIndex ||
//                    shaderProgramData[cvRdrIndex] == null) {
//                return;
//            }

	    ShaderProgramId shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();
            // Nothing to do if the shaderProgramId is null
            if (shaderProgramId == null) {
                return;
            }

            // Destroy the native resource, set the ID to null for this canvas/renderer,
            // and clear the bit in the resourceCreationMask
            // Ignore any possible shader error, because there is no meaningful way to report it
            destroyShaderProgram(cv.ctx, shaderProgramId);
            // Reset this ShaderProgramData object.
	    shaderProgramData[cvRdrIndex].reset();
        }
    }


    /**
     * updateNative is called while traversing the RenderBin to
     * update the shader program state
     */
    void updateNative(Canvas3D cv, boolean enable) {
	// System.err.println("ShaderProgramRetained.updateNative : ");

        final boolean useSharedCtx = cv.useSharedCtx && cv.screen.renderer.sharedCtx != null;
        int cvRdrIndex;
        long ctxTimeStamp;

        if (useSharedCtx) {
            cvRdrIndex = cv.screen.renderer.rendererId;
            ctxTimeStamp = cv.screen.renderer.sharedCtxTimeStamp;
        } else {
            cvRdrIndex = cv.canvasId;
            ctxTimeStamp = cv.ctxTimeStamp;
        }

        // Create ShaderProgramData object for this canvas/renderer if it doesn't already exist
        createShaderProgramData(cvRdrIndex, ctxTimeStamp);

        // Check whether this shader program type is supported for this canvas
        if (!verifyShaderProgramSupported(cv)) {
            return;
        }

        // Just disable shader program and return if enable parameter is set to false
        if (!enable) {
            // Given the current design, disableShaderProgram cannot return a non-null value,
            // so no need to check it
            disableShaderProgram(cv);
            return;
        }

        // Just disable shader program and return if array of shaders is empty,
        // or if a previous attempt to link resulted in an error
        if (shaders == null || shaders.length == 0 || linkErrorOccurred) {
            disableShaderProgram(cv);
            return;
        }

	boolean loadShaderProgram = false; // flag indicating whether to reload all shaderProgram states
        if (getShaderProgramData(cvRdrIndex).getShaderProgramId() == null) {
            loadShaderProgram = true;
        }

	//System.err.println(".... loadShaderProgram = " + loadShaderProgram);
	//System.err.println(".... resourceCreationMask= " + resourceCreationMask);

        ShaderError err = null;
        boolean errorOccurred = false;
	if (loadShaderProgram) {
            if (useSharedCtx) {
	    // TODO : Need to test useSharedCtx case. ** Untested case **
		cv.makeCtxCurrent(cv.screen.renderer.sharedCtx);
            }

            // Create shader resources if not already done
            for(int i=0; i < shaders.length; i++) {
                // Create ShaderProgramData object for this canvas/renderer if it doesn't already exist
                shaders[i].createShaderData(cvRdrIndex, ctxTimeStamp);

                if (shaders[i].compileErrorOccurred) {
                    errorOccurred = true;
                }
                else {
                    err = createShader(cv, cvRdrIndex, shaders[i]);
                    if (err != null) {
                        err.setShaderProgram((ShaderProgram)this.source);
                        err.setShader((Shader)shaders[i].source);
                        err.setCanvas3D(cv);
                        notifyErrorListeners(cv, err);
                        errorOccurred = true;
                    }
                    else {
                        err = compileShader(cv, cvRdrIndex, shaders[i]);
                        if (err != null) {
                            err.setShaderProgram((ShaderProgram)this.source);
                            err.setShader((Shader)shaders[i].source);
                            err.setCanvas3D(cv);
                            notifyErrorListeners(cv, err);
                            destroyShader(cv, cvRdrIndex, shaders[i]);
                            shaders[i].compileErrorOccurred = true;
                            errorOccurred = true;
                        }
                    }
                }
            }

            // Create shader program
            if (!errorOccurred) {
                err = createShaderProgram(cv, cvRdrIndex);
                if (err != null) {
                    err.setShaderProgram((ShaderProgram)this.source);
                    err.setCanvas3D(cv);
                    notifyErrorListeners(cv, err);
                    errorOccurred = true;
                }
            }

            boolean linked = getShaderProgramData(cvRdrIndex).isLinked();
            if (!linked) {
                // Bind vertex attribute names
                if (!errorOccurred) {
                    if (vertexAttrNames != null) {
//                        System.err.println("vertexAttrNames.length = " + vertexAttrNames.length);
                        for (int i = 0; i < vertexAttrNames.length; i++) {
                            err = bindVertexAttrName(cv, cvRdrIndex, vertexAttrNames[i], i);
                            // Report non-fatal error, if one was detected
                            if (err != null) {
                                err.setShaderProgram((ShaderProgram)this.source);
                                err.setCanvas3D(cv);
                                notifyErrorListeners(cv, err);
                            }
                        }
                    }
                }

                // Link shader program
                if (!errorOccurred) {
                    err = linkShaderProgram(cv, cvRdrIndex, shaders);
                    if (err != null) {
                        err.setShaderProgram((ShaderProgram)this.source);
                        err.setCanvas3D(cv);
                        notifyErrorListeners(cv, err);
                        destroyShaderProgram(cv, cvRdrIndex);
                        linkErrorOccurred = true;
                        errorOccurred = true;
                    }
                }

                // lookup vertex attribute names
                if (!errorOccurred) {
                    if (vertexAttrNames != null) {
                        lookupVertexAttrNames(cv, cvRdrIndex, vertexAttrNames);
                    }
                }

                // Lookup shader attribute names
                if (!errorOccurred) {
                    if (shaderAttrNames != null) {
//                        System.err.println("shaderAttrNames.length = " + shaderAttrNames.length);
                        lookupShaderAttrNames(cv, cvRdrIndex, shaderAttrNames);
                    }
                }
            }

            // Restore current context if we changed it to the shareCtx
            if (useSharedCtx) {
                cv.makeCtxCurrent(cv.ctx);
            }

            // If compilation or link error occured, disable shader program and return
            if (errorOccurred) {
                disableShaderProgram(cv);
                return;
            }
        }

        // Now we can enable the shader program
	enableShaderProgram(cv, cvRdrIndex);
    }

    /**
     * Update native value for ShaderAttributeValue class
     */
    ShaderError setUniformAttrValue(Context ctx, ShaderProgramId shaderProgramId,
            ShaderAttrLoc loc, ShaderAttributeValueRetained sav) {

	switch (sav.getClassType()) {
	case ShaderAttributeObjectRetained.TYPE_INTEGER:
	    return setUniform1i(ctx, shaderProgramId, loc,
				((int[])sav.attrWrapper.getRef())[0]);

	case ShaderAttributeObjectRetained.TYPE_FLOAT:
	    return setUniform1f(ctx, shaderProgramId, loc,
				((float[])sav.attrWrapper.getRef())[0]);

	case ShaderAttributeObjectRetained.TYPE_TUPLE2I:
	    return setUniform2i(ctx, shaderProgramId, loc,
				(int[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_TUPLE2F:
	    return setUniform2f(ctx, shaderProgramId, loc,
				(float[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_TUPLE3I:
	    return setUniform3i(ctx, shaderProgramId, loc,
				(int[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_TUPLE3F:
	    return setUniform3f(ctx, shaderProgramId, loc,
				(float[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_TUPLE4I:
	    return setUniform4i(ctx, shaderProgramId, loc,
				(int[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_TUPLE4F:
	    return setUniform4f(ctx, shaderProgramId, loc,
				(float[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_MATRIX3F:
	    return setUniformMatrix3f(ctx, shaderProgramId, loc,
				      (float[])sav.attrWrapper.getRef());

	case ShaderAttributeObjectRetained.TYPE_MATRIX4F:
	    return setUniformMatrix4f(ctx, shaderProgramId, loc,
				      (float[])sav.attrWrapper.getRef());

	default:
	    // Should never get here
	    assert false : "Unrecognized ShaderAttributeValue classType";
	    return null;
	}
    }

     /**
     * Update native value for ShaderAttributeArray class
     */
    ShaderError setUniformAttrArray(Context ctx, ShaderProgramId shaderProgramId,
            ShaderAttrLoc loc, ShaderAttributeArrayRetained saa) {

        switch (saa.getClassType()) {
            case ShaderAttributeObjectRetained.TYPE_INTEGER:
                return  setUniform1iArray(ctx, shaderProgramId, loc, saa.length(),
                        ((int[])saa.attrWrapper.getRef()));

            case ShaderAttributeObjectRetained.TYPE_FLOAT:
                return setUniform1fArray(ctx, shaderProgramId, loc, saa.length(),
                        ((float[])saa.attrWrapper.getRef()));

            case ShaderAttributeObjectRetained.TYPE_TUPLE2I:
                return setUniform2iArray(ctx, shaderProgramId, loc, saa.length(),
                        (int[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_TUPLE2F:
                return setUniform2fArray(ctx, shaderProgramId, loc, saa.length(),
                        (float[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_TUPLE3I:
                return setUniform3iArray(ctx, shaderProgramId, loc, saa.length(),
                        (int[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_TUPLE3F:
                return setUniform3fArray(ctx, shaderProgramId, loc, saa.length(),
                        (float[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_TUPLE4I:
                return setUniform4iArray(ctx, shaderProgramId, loc, saa.length(),
                        (int[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_TUPLE4F:
                return setUniform4fArray(ctx, shaderProgramId, loc, saa.length(),
                        (float[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_MATRIX3F:
                return setUniformMatrix3fArray(ctx, shaderProgramId, loc, saa.length(),
                        (float[])saa.attrWrapper.getRef());

            case ShaderAttributeObjectRetained.TYPE_MATRIX4F:
                return setUniformMatrix4fArray(ctx, shaderProgramId, loc, saa.length(),
                        (float[])saa.attrWrapper.getRef());

            default:
                // Should never get here
                assert false : "Unrecognized ShaderAttributeArray classType";
                return null;
        }

    }


    void setShaderAttributes(Canvas3D cv, ShaderAttributeSetRetained attributeSet) {
        final boolean useSharedCtx = cv.useSharedCtx && cv.screen.renderer.sharedCtx != null;
        final int cvRdrIndex = useSharedCtx ? cv.screen.renderer.rendererId : cv.canvasId;
        ShaderProgramData spData = getShaderProgramData(cvRdrIndex);

        // Just return if shader program wasn't linked successfully
        if (!spData.isLinked()) {
            return;
        }

        ShaderProgramId shaderProgramId = spData.getShaderProgramId();

	Iterator<ShaderAttributeRetained> attrs = attributeSet.getAttrs().values().iterator();
        while (attrs.hasNext()) {
            ShaderError err = null;
		ShaderAttributeRetained saRetained = attrs.next();

            // Lookup attribute info for the specified attrName; null means
            // that the name does not appear in the ShaderProgram, so we will
            // report an error.
            AttrNameInfo attrNameInfo = spData.getAttrNameInfo(saRetained.getAttributeName());
            if(attrNameInfo == null) {
//                System.err.println("ShaderProgramRetained : attrLocation (" + saRetained.getAttributeName() + ") is null.");
                String errMsg = "Attribute name not set in ShaderProgram: " + saRetained.getAttributeName(); // TODO: I18N
                err = new ShaderError(ShaderError.SHADER_ATTRIBUTE_NAME_NOT_SET_ERROR, errMsg);
            } else {
                ShaderAttrLoc loc = attrNameInfo.getLocation();
                if (loc != null) {
                    if (saRetained instanceof ShaderAttributeValueRetained) {
                        ShaderAttributeValueRetained savRetained = (ShaderAttributeValueRetained)saRetained;
                        if (attrNameInfo.isArray() ||
                                (savRetained.getClassType() != attrNameInfo.getType())) {
                            String errMsg = "Attribute type mismatch: " + savRetained.getAttributeName(); // TODO: I18N
                            err = new ShaderError(ShaderError.SHADER_ATTRIBUTE_TYPE_ERROR, errMsg);
                        }
                        else {
                            err = setUniformAttrValue(cv.ctx, shaderProgramId, loc, savRetained);
                        }
                    } else if (saRetained instanceof ShaderAttributeArrayRetained) {
                        ShaderAttributeArrayRetained saaRetained = (ShaderAttributeArrayRetained)saRetained;
                        if (!attrNameInfo.isArray() ||
                                (saaRetained.getClassType() != attrNameInfo.getType())) {
                            String errMsg = "Attribute type mismatch: " + saaRetained.getAttributeName(); // TODO: I18N
                            err = new ShaderError(ShaderError.SHADER_ATTRIBUTE_TYPE_ERROR, errMsg);
                        }
                        else {
                            err = setUniformAttrArray(cv.ctx, shaderProgramId, loc, saaRetained);
                        }
                    } else if (saRetained instanceof ShaderAttributeBindingRetained) {
                        assert false;
                        throw new RuntimeException("not implemented");
                    } else {
                        assert false;
                    }
                }
            }

            if (err != null) {
                // Before reporting the ShaderAttribute error, check
                // whether it has already been reported for this ShaderProgram
                if (shaderAttrErrorSet == null) {
				shaderAttrErrorSet = new HashSet<ShaderAttribute>();
                }
			if (shaderAttrErrorSet.add((ShaderAttribute) saRetained.source)) {
                    err.setShaderProgram((ShaderProgram)this.source);
                    err.setShaderAttributeSet((ShaderAttributeSet)attributeSet.source);
                    err.setShaderAttribute((ShaderAttribute)saRetained.source);
                    err.setCanvas3D(cv);
                    notifyErrorListeners(cv, err);
                }
            }
        }
    }

    class ShaderProgramData extends Object {

        // issue 378 - time stamp of context creation for this Canvas
        private long ctxTimeStamp;

	// shaderProgramId use by native code.
	private ShaderProgramId shaderProgramId = null;

	// linked flag for native.
	private boolean linked = false;

	// A map of locations for ShaderAttributes.
private HashMap<String, AttrNameInfo> attrNameInfoMap = new HashMap<String, AttrNameInfo>();

	/** ShaderProgramData Constructor */
	ShaderProgramData() {
	}

        void reset() {
            ctxTimeStamp = 0L;
            shaderProgramId = null;
            linked = false;
            attrNameInfoMap.clear();
        }

        long getCtxTimeStamp() {
            return ctxTimeStamp;
        }

        void setCtxTimeStamp(long ctxTimeStamp) {
            this.ctxTimeStamp = ctxTimeStamp;
        }

	void setShaderProgramId(ShaderProgramId shaderProgramId) {
	    this.shaderProgramId = shaderProgramId;
	}

	ShaderProgramId getShaderProgramId() {
	    return this.shaderProgramId;
	}

	void setLinked(boolean linked) {
	    this.linked = linked;
	}

	boolean isLinked() {
	    return linked;
	}

void setAttrNameInfo(String shaderAttribute, AttrNameInfo attrNameInfo) {
	assert (shaderAttribute != null);
	attrNameInfoMap.put(shaderAttribute, attrNameInfo);
}

AttrNameInfo getAttrNameInfo(String shaderAttribute) {
	return attrNameInfoMap.get(shaderAttribute);
}

    }

    // Data associated with an attribute name
    class AttrNameInfo {
        void setLocation(ShaderAttrLoc loc) {
            this.loc = loc;
        }

        ShaderAttrLoc getLocation() {
            return loc;
        }

        void setType(int type) {
            this.type = type;
        }

        int getType() {
            return type;
        }

        boolean isArray() {
            return isArray;
        }

        void setArray(boolean isArray) {
            this.isArray = isArray;
        }

        // Location of attribute name in linked shader program
        private ShaderAttrLoc loc;

        // boolean indicating whether the attribute is an array
        private boolean isArray;

        // type of shader attribute
        private int type;
    }

}
