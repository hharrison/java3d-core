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

import java.util.*;
import javax.vecmath.*;

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
    private HashSet shaderAttrErrorSet = null;

    // need to synchronize access from multiple rendering threads 
    Object resourceLock = new Object();

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
            this.vertexAttrNames = (String[])vertexAttrNames.clone();
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
	
	return (String[])vertexAttrNames.clone();

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
            this.shaderAttrNames = (String[])shaderAttrNames.clone();
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
	
	return (String[])shaderAttrNames.clone();

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
    abstract ShaderError createShader(Context ctx, ShaderRetained shader, long[] shaderIdArr); 

    /**
     * Method to destroy the native shader.
     */
    abstract ShaderError destroyShader(Context ctx, long shaderId);

    /**
     * Method to compile the native shader.
     */
    abstract ShaderError compileShader(Context ctx, long shaderId, String source);

    /**
     * Method to create the native shader program.
     */
    abstract ShaderError createShaderProgram(Context ctx, long[] shaderProgramIdArr);

    /**
     * Method to destroy the native shader program.
     */
    abstract ShaderError destroyShaderProgram(Context ctx, long shaderProgramId);

    /**
     * Method to link the native shader program.
     */
    abstract ShaderError linkShaderProgram(Context ctx, long shaderProgramId, long[] shaderIds);

    /**
     * Method to bind a vertex attribute name to the specified index.
     */
    abstract ShaderError bindVertexAttrName(Context ctx, long shaderProgramId, String attrName, int attrIndex);

    /**
     * Method to lookup a list of (uniform) shader attribute names and return
     * information about the attributes.
     */
    abstract void lookupShaderAttrNames(Context ctx, long shaderProgramId, String[] attrNames, AttrNameInfo[] attrNameInfoArr);
    
    /*
     * Method to lookup a list of vertex attribute names.
     */
    abstract void lookupVertexAttrNames(Context ctx, long shaderProgramId, String[] attrNames, boolean[] errArr);

    /**
     * Method to use the native shader program.
     */
    abstract ShaderError enableShaderProgram(Context ctx, long shaderProgramId);
    
    /**
     * Method to disable the native shader program.
     */
    abstract ShaderError disableShaderProgram(Context ctx);
    
    // ShaderAttributeValue methods

    abstract ShaderError setUniform1i(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int value);
    
    abstract ShaderError setUniform1f(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float value);
    
    abstract ShaderError setUniform2i(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    abstract ShaderError setUniform2f(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);
    
    abstract ShaderError setUniform3i(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    abstract ShaderError setUniform3f(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);    
    
    abstract ShaderError setUniform4i(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int[] value);
    
    abstract ShaderError setUniform4f(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    float[] value);    
    
    abstract ShaderError setUniformMatrix3f(Context ctx,
					   long shaderProgramId,
				           long uniformLocation,
					   float[] value);

    abstract ShaderError setUniformMatrix4f(Context ctx,
					   long shaderProgramId,
			         	   long uniformLocation,
					   float[] value);


    // ShaderAttributeArray methods
    
    abstract ShaderError setUniform1iArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    abstract ShaderError setUniform1fArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);
    
    abstract ShaderError setUniform2iArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    abstract ShaderError setUniform2fArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);
    
    abstract ShaderError setUniform3iArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    abstract ShaderError setUniform3fArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);    
    
    abstract ShaderError setUniform4iArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      int[] value);
    
    abstract ShaderError setUniform4fArray(Context ctx,
				      long shaderProgramId,
				      long uniformLocation,
				      int numElements,
				      float[] value);    
    
    abstract ShaderError setUniformMatrix3fArray(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int numElements,
					    float[] value);

    abstract ShaderError setUniformMatrix4fArray(Context ctx,
					    long shaderProgramId,
					    long uniformLocation,
					    int numElements,
					    float[] value);

    
    /**
     * Method to return a flag indicating whether this
     * ShaderProgram is supported on the specified Canvas.
     */
    abstract boolean isSupported(Canvas3D cv);


    void setLive(boolean backgroundGroup, int refCount) {
	
	// System.out.println("ShaderProgramRetained.setLive()");

	if (shaders != null) {
	    for (int i = 0; i < shaders.length; i++){
		shaders[i].setLive(backgroundGroup, refCount);
	    }
	}
	
	super.doSetLive(backgroundGroup, refCount);
        super.markAsLive();

    }

    void clearLive(int refCount) {

        // System.out.println("ShaderProgramRetained.clearLive()");

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
            ((ShaderProgramRetained)mirror).vertexAttrNames = (String[])this.vertexAttrNames.clone();
        }
        
        // Create mirror copy of shader attribute names
        if (this.shaderAttrNames == null) {
            ((ShaderProgramRetained)mirror).shaderAttrNames = null;
        }
        else {
            ((ShaderProgramRetained)mirror).shaderAttrNames = (String[])this.shaderAttrNames.clone();
        }
        
        // Clear shader attribute error set
        ((ShaderProgramRetained)mirror).shaderAttrErrorSet = null;
    }
    
    /**
     * Update the "component" field of the mirror object with the  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {
	
	// ShaderProgram can't be modified once it is live.
	assert(false);	
	System.out.println("ShaderProgramRetained : updateMirrorObject NOT IMPLEMENTED YET");
    } 

    /**
     * Method to create a ShaderProgramData object for the specified
     * canvas/renderer if it doesn't already exist
     */
    private void createShaderProgramData(int cvRdrIndex) {
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
            }
        }
    }

    /**
     * Method to create the native shader program. We must already have
     * called createShaderProgramData for this cvRdrIndex.
     */
    private ShaderError createShaderProgram(Canvas3D cv, int cvRdrIndex) {
        // Create shaderProgram resources if it has not been done.
        synchronized(resourceLock) {
            assert(shaderProgramData[cvRdrIndex].getShaderProgramId() == 0);

            long[] spIdArr = new long[1];
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
            long[] shaderIds = new long[shaders.length];
	    for(int i=0; i<shaders.length; i++) {
                synchronized(shaders[i]) {
                    shaderIds[i] = shaders[i].shaderIds[cvRdrIndex];
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
            long shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();
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
            long shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();

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
            long shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId();

            AttrNameInfo[] attrNameInfoArr = new AttrNameInfo[attrNames.length];
            lookupShaderAttrNames(cv.ctx, shaderProgramId, attrNames, attrNameInfoArr);
        
            for (int i = 0; i < attrNames.length; i++) {
                shaderProgramData[cvRdrIndex].setAttrNameInfo(attrNames[i], attrNameInfoArr[i]);
                
                // Report non-fatal error if location is invalid (-1)
                if (attrNameInfoArr[i].getLocation() == -1) {
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
     * Method to create the native shader.
     */
    private ShaderError createShader(Canvas3D cv, int cvRdrIndex, ShaderRetained shader) {
        
        // Create shaderProgram resources if it has not been done.
        synchronized(shader.resourceLock) {
            if(shader.shaderIds == null){
                // We rely on Java to initial the array elements to 0 or false;
                shader.shaderIds = new long[cvRdrIndex+1];
                shader.compiled = new boolean[cvRdrIndex+1];
            } else if( shader.shaderIds.length <= cvRdrIndex) {
                // We rely on Java to initial the array elements to 0 or false;
                long[] tempSIds = new long[cvRdrIndex+1];
                boolean[] tempCompiled = new boolean[cvRdrIndex+1];
                
                System.arraycopy(shader.shaderIds, 0,
                        tempSIds, 0,
                        shader.shaderIds.length);
                shader.shaderIds = tempSIds;
                
                System.arraycopy(shader.compiled, 0,
                        tempCompiled, 0,
                        shader.compiled.length);
                shader.compiled = tempCompiled;
            }
            
            if(shader.shaderIds[cvRdrIndex] != 0) {
                // We have already created the shaderId for this Canvas.
                return null;
            }
            
            long[] shaderIdArr = new long[1];
            ShaderError err = createShader(cv.ctx, shader, shaderIdArr);
            if(err != null) {
                return err;
            }
            shader.shaderIds[cvRdrIndex] = shaderIdArr[0];
        }
        return null;
    }

    /**
     * Method to compile the native shader.
     */
    private ShaderError compileShader(Canvas3D cv, int cvRdrIndex, ShaderRetained shader) {
        
        synchronized(shader.resourceLock) {
            
            if(shader.compiled[cvRdrIndex] == true) {
                // We have already compiled the shaderId for this Canvas.
                return null;
            }
            
	    String source = ((SourceCodeShaderRetained)shader).getShaderSource();
            ShaderError err = compileShader(cv.ctx, shader.shaderIds[cvRdrIndex], source);
            if(err != null) {
                return err;
            }
            shader.compiled[cvRdrIndex] = true;
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
            // Check whether an entry in the shaderIds array has been allocated
            if (shader.shaderIds == null || shader.shaderIds.length <= cvRdrIndex) {
                return;
            }
            
            // Nothing to do if the shaderId is 0
            if (shader.shaderIds[cvRdrIndex] == 0) {
                return;
            }

            // Destroy the native resource and set the ID to 0 for this canvas/renderer
            // Ignore any possible shader error, because there is no meaningful way to report it
            destroyShader(cv.ctx, shader.shaderIds[cvRdrIndex]);
            shader.shaderIds[cvRdrIndex] = 0;
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
            
	    long shaderProgramId = shaderProgramData[cvRdrIndex].getShaderProgramId(); 
            // Nothing to do if the shaderProgramId is 0
            if (shaderProgramId == 0) {
                return;
            }

            // Destroy the native resource, set the ID to 0 for this canvas/renderer,
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
	// System.out.println("ShaderProgramRetained.updateNative : ");

        final boolean useSharedCtx = cv.useSharedCtx && cv.screen.renderer.sharedCtx != null;
        final int cvRdrIndex = useSharedCtx ? cv.screen.renderer.rendererId : cv.canvasId;

        // Create ShaderProgramData object for this canvas/renderer if it doesn't already exist
        createShaderProgramData(cvRdrIndex);

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
        if (getShaderProgramData(cvRdrIndex).getShaderProgramId() == 0) {
            loadShaderProgram = true;
        }
        
	//System.out.println(".... loadShaderProgram = " + loadShaderProgram);
	//System.out.println(".... resourceCreationMask= " + resourceCreationMask);
 
        ShaderError err = null;
        boolean errorOccurred = false;
	if (loadShaderProgram) {
            if (useSharedCtx) {
	    // TODO : Need to test useSharedCtx case. ** Untested case **
		cv.makeCtxCurrent(cv.screen.renderer.sharedCtx);
            }
        
            // Create shader resources if not already done
            for(int i=0; i < shaders.length; i++) {
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
    ShaderError setUniformAttrValue(Context ctx, long shaderProgramId, long loc,
				    ShaderAttributeValueRetained sav) {

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
    ShaderError setUniformAttrArray(Context ctx, long shaderProgramId, long loc,
				    ShaderAttributeArrayRetained saa) {   

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
        
        long shaderProgramId = spData.getShaderProgramId();
        
        Iterator attrs = attributeSet.getAttrs().values().iterator();
        while (attrs.hasNext()) {
            ShaderError err = null;
            ShaderAttributeRetained saRetained = (ShaderAttributeRetained)attrs.next();

            // Lookup attribute info for the specified attrName; null means
            // that the name does not appear in the ShaderProgram, so we will
            // report an error.
            AttrNameInfo attrNameInfo = spData.getAttrNameInfo(saRetained.getAttributeName());
            if(attrNameInfo == null) {
//                System.err.println("ShaderProgramRetained : attrLocation (" + saRetained.getAttributeName() + ") is null.");
                String errMsg = "Attribute name not set in ShaderProgram: " + saRetained.getAttributeName(); // TODO: I18N
                err = new ShaderError(ShaderError.SHADER_ATTRIBUTE_NAME_NOT_SET_ERROR, errMsg);
            } else {
                long loc = attrNameInfo.getLocation();
                if (loc != -1) {
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
                    shaderAttrErrorSet = new HashSet();
                }
                if (shaderAttrErrorSet.add(saRetained.source)) {
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
	
	// shaderProgramId use by native code. 
	private long shaderProgramId = 0;
	
	// linked flag for native.
	private boolean linked = false;
	
	// A map of locations for ShaderAttributes.
	private HashMap attrNameInfoMap = new HashMap();

	/** ShaderProgramData Constructor */
	ShaderProgramData() {
	}
        
        void reset() {
            shaderProgramId = 0;
            linked = false;
            attrNameInfoMap.clear();
        }

	void setShaderProgramId(long shaderProgramId) {
	    this.shaderProgramId = shaderProgramId;
	}

	long getShaderProgramId() {
	    return this.shaderProgramId;
	}

	void setLinked(boolean linked) {
	    this.linked = linked;
	}

	boolean isLinked() {
	    return linked;
	}

	void setAttrNameInfo(String shaderAttribute, AttrNameInfo attrNameInfo) {
	    assert(shaderAttribute != null);
	    attrNameInfoMap.put(shaderAttribute, attrNameInfo);
	}

	AttrNameInfo getAttrNameInfo(String shaderAttribute) {
	    return  (AttrNameInfo) attrNameInfoMap.get(shaderAttribute);
	}


    }
    
    // Data associated with an attribute name
    class AttrNameInfo {
        void setLocation(long loc) {
            this.loc = loc;
        }

        long getLocation() {
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
        private long loc;

        // boolean indicating whether the attribute is an array
        private boolean isArray;
        
        // type of shader attribute
        private int type;
    }

}
