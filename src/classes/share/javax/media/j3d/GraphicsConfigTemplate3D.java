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

import java.awt.GraphicsDevice;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsConfigTemplate;

/**
 * This class is used to obtain a valid GraphicsConfiguration that can be used by Java 3D.
 * A user instantiates one of these objects and then sets all
 * non-default attributes as desired.  The getBestConfiguration()
 * method in the GraphicsDevice class is then called with this
 * GraphicsConfigTemplate and the "best" GraphicsConfiguration is returned. The "best"
 * GraphicsConfiguration means that this GraphicsConfiguration is supported and it
 * meets or exceeds what was requested in the GraphicsConfigTemplate.
 * Null is returned if no such "best" GraphicsConfiguration is found.
 * 
 * @see GraphicsConfigTemplate
 * @see GraphicsDevice
 * @see GraphicsConfiguration
 */
public class GraphicsConfigTemplate3D extends GraphicsConfigTemplate {

    int depthSize;
    int doubleBuffer;
    int blueSize;
    int greenSize;
    int redSize;
    int sceneAntialiasing;
    int stereo;
    int stencilSize;

    // Temporary variables use for passing argument to/from Request Renderer
    Object testCfg;

    static Object globalLock = new Object();
    static Object monitorLock = new Object();
    static volatile boolean threadWaiting = false;

    /**
     * Constructs a GraphicsConfigTemplate3D object with default parameters.
     * The default values are as follows:
     * <ul>
     * depthSize : 16<br>
     * doubleBuffer : REQUIRED<br>
     * sceneAntialiasing : UNNECESSARY<br>
     * stereo : UNNECESSARY<br>
     * redSize : 2<br>
     * greenSize : 2<br>
     * blueSize : 2<br>
     * stencilSize : 0<br>
     * </ul>
     */
    public GraphicsConfigTemplate3D() {
        doubleBuffer = REQUIRED;
        stereo = UNNECESSARY;
        depthSize = 16;
        stencilSize = 0;
        redSize = greenSize = blueSize = 2;
        sceneAntialiasing = UNNECESSARY;
    }

    /**
     * Sets the double buffering requirement. It should be
     * GraphicsConfigTemplate.REQUIRED, GraphicsConfigTemplate.PREFERRED,
     * or GraphicsConfigTemplate.UNNECESSARY.
     * If an invalid value is passed in, it is ignored.
     * If the value of double buffering is
     * GraphicsConfigTemplate.REQUIRED, and no GraphicsConfiguration is found
     * that meets this requirement, null will be returned in getBestConfiguration().
     * @param value the value to set this field to 
     */
    public void setDoubleBuffer(int value) {
        if (value < REQUIRED && value > UNNECESSARY)
            return;

        doubleBuffer = value;
    }

    /**
     * Retrieves the double buffering value.
     * @return the current value of the doubleBuffer attribute
     */
    public int getDoubleBuffer() {
        return doubleBuffer;
    }

    /**
     * Sets the stereo requirement.  It should be
     * GraphicsConfigTemplate.REQUIRED, GraphicsConfigTemplate.PREFERRED,
     * or GraphicsConfigTemplate.UNNECESSARY. If an invalid value
     * is passed in, it is ignored. If the value of stereo requirement is
     * GraphicsConfigTemplate.REQUIRED, and no GraphicsConfiguration is found
     * that meets this requirement, null will be returned in getBestConfiguration().
     * @param value the value to set this field to 
     */
    public void setStereo(int value) {
        if (value < REQUIRED && value > UNNECESSARY)
            return;

        stereo = value;
    }

    /**
     * Retrieves the stereo value.
     * @return the current value of the stereo attribute.
     */
    public int getStereo() {
        return stereo;
    }

    /**
     * Sets the scene antialiasing requirement.  It should be
     * GraphicsConfigTemplate.REQUIRED, GraphicsConfigTemplate.PREFERRED,
     * or GraphicsConfigTemplate.UNNECESSARY. If an invalid value
     * is passed in, it is ignored. If the value of scene antialiasing is
     * GraphicsConfigTemplate.REQUIRED, and no GraphicsConfiguration is found
     * that meets this requirement, null will be returned in getBestConfiguration().
     * @param value the value to set this field to 
     */
    public void setSceneAntialiasing(int value) {
        if (value < REQUIRED && value > UNNECESSARY)
            return;

        sceneAntialiasing = value;
    }

    /**
     * Retrieves the scene antialiasing value.
     * @return the current value of the scene antialiasing attribute.
     */
    public int getSceneAntialiasing() {
        return sceneAntialiasing;
    }

    /**
     * Sets the depth buffer size requirement.  This is the minimum requirement.
     * If no GraphicsConfiguration is found that meets or
     * exceeds this minimum requirement, null will be returned in
     * getBestConfiguration(). 
     * @param value the value to set this field to
     */
    public void setDepthSize(int value) {
        if (value < 0)
            return;

	depthSize = value;
    }

    /**
     * Retrieves the size of the depth buffer.
     * @return the current value of the depthSize attribute
     */
    public int getDepthSize() {
        return depthSize;
    }

    /**
     * Sets the stencil buffer size requirement.
     * This is the minimum requirement.
     * If no GraphicsConfiguration is found that meets or
     * exceeds this minimum requirement, null will be returned in
     * getBestConfiguration().
     *
     * @param value the value to set this field to
     *
     * @since Java 3D 1.4
     */
    public void setStencilSize(int value) {
        if (value < 0)
            return;
        
	stencilSize = value;
    }

    /**
     * Retrieves the size of the stencil buffer.
     *
     * @return the current value of the stencilSize attribute
     *
     * @since Java 3D 1.4
     */
    public int getStencilSize() {
        return stencilSize;
    }

    /**
     * Sets the number of red bits required. This is the minimum requirement.
     * If no GraphicsConfiguration is found that meets or
     * exceeds this minimum requirement, null will be returned in
     * getBestConfiguration().  
     * @param value the value to set this field to
     */
    public void setRedSize(int value) {
        if (value < 0)
            return;

        redSize = value;
    }

    /**
     * Retrieves the number of red bits requested by this template.
     * @return the current value of the redSize attribute.
     */
    public int getRedSize() {
        return redSize;
    }


    /**
     * Sets the number of green bits required.  This is the minimum requirement.
     * If no GraphicsConfiguration is found that meets or
     * exceeds this minimum requirement, null will be returned in
     * getBestConfiguration().  
     * @param value the value to set this field to
     */
    public void setGreenSize(int value) {
        if (value < 0)
            return;

        greenSize = value;
    }

    /**
     * Retrieves the number of green bits requested by this template.
     * @return the current value of the greenSize attribute.
     */
    public int getGreenSize() {
        return greenSize;
    }

    /**
     * Sets the number of blue bits required. This is the minimum requirement.
     * If no GraphicsConfiguration is found that meets or
     * exceeds this minimum requirement, null will be returned in
     * getBestConfiguration().  
     * @param value the value to set this field to
     */
    public void setBlueSize(int value) {
        if (value < 0)
            return;

        blueSize = value;
    }

    /**
     * Retrieves the number of blue bits requested by this template.
     * @return the current value of the blueSize attribute.
     */
    public int getBlueSize() {
        return blueSize;
    }

    /**
     * Implement the abstract function of getBestConfiguration() in GraphicsConfigTemplate.
     * Usually this function is not directly called by the user. It is
     * implicitly called by getBestConfiguration() in GraphicsDevice.
     * The method getBestConfiguration() in GraphicsDevice will return whatever this function returns.
     * This function will return the "best" GraphicsConfiguration. The "best" GraphicsConfiguration
     * means that this GraphicsConfiguration is supported and it meets or exceeds what was requested in the
     * GraphicsConfigTemplate. If no such "best" GraphicsConfiguration is found, null is returned.
     * @param gc the array of GraphicsConfigurations to choose from
     *
     * @return the best GraphicsConfiguration
     *
     * @see GraphicsDevice
     */
    public GraphicsConfiguration
      getBestConfiguration(GraphicsConfiguration[] gc) {
	if ((gc == null) || (gc.length == 0) || (gc[0] == null)) {
	    return null;
	}

	synchronized (globalLock) {
	    testCfg = gc;

	    // It is possible that the followign postRequest will
	    // cause request renderer run immediately before
	    // runMonitor(WAIT). So we need to set 
	    // threadWaiting to true.
	    threadWaiting = true;

	    // Prevent deadlock if invoke from Behavior callback since
	    // this thread has to wait Renderer thread to finish but
	    // MC can only handle postRequest and put it in Renderer
	    // queue when free.
	    if (Thread.currentThread() instanceof BehaviorScheduler) {
		VirtualUniverse.mc.sendRenderMessage(gc[0], this, 
						     MasterControl.GETBESTCONFIG);
	    } else {
		VirtualUniverse.mc.postRequest(MasterControl.GETBESTCONFIG, this);
	    }
	    runMonitor(J3dThread.WAIT);
	    return (GraphicsConfiguration) testCfg;
	}
    }

    /**
     * Returns a boolean indicating whether or not the given
     * GraphicsConfiguration can be used to create a drawing
     * surface that can be rendered to.
     *
     * @param gc the GraphicsConfiguration object to test
     *
     * @return <code>true</code> if this GraphicsConfiguration object
     *  can be used to create surfaces that can be rendered to,
     *  <code>false</code> if the GraphicsConfiguration can not be used
     *  to create a drawing surface usable by this API.
     */
    public boolean isGraphicsConfigSupported(GraphicsConfiguration gc) {
	if (gc == null) {
	    return false;
	}

        synchronized (globalLock) {
	    testCfg = gc;
	    threadWaiting = true;
	    if (Thread.currentThread() instanceof BehaviorScheduler) {
		VirtualUniverse.mc.sendRenderMessage(gc, this, MasterControl.ISCONFIGSUPPORT);
	    } else {
		VirtualUniverse.mc.postRequest(MasterControl.ISCONFIGSUPPORT, this);
	    }
	    runMonitor(J3dThread.WAIT);
	    return ((Boolean) testCfg).booleanValue();
	}
    }

    /**
     * Set the stereo/doubleBuffer/sceneAntialiasingAccum
     * and hasSceneAntialiasingMultiSamples flags in Canvas3D
     */
    static void getGraphicsConfigFeatures(Canvas3D c) {
	synchronized (globalLock) {
	    threadWaiting = true;
	    if (Thread.currentThread() instanceof BehaviorScheduler) {
		VirtualUniverse.mc.sendRenderMessage(c.graphicsConfiguration, c,
						     MasterControl.SET_GRAPHICSCONFIG_FEATURES);
	    } else {
		VirtualUniverse.mc.postRequest(MasterControl.SET_GRAPHICSCONFIG_FEATURES, c);
	    }
	    runMonitor(J3dThread.WAIT);
	}
    }



    /**
     * Set the queryProperties() map in Canvas3D
     */
    static void setQueryProps(Canvas3D c) {
	synchronized (globalLock) {
	    threadWaiting = true;
	    if (Thread.currentThread() instanceof BehaviorScheduler) {
		VirtualUniverse.mc.sendRenderMessage(c.graphicsConfiguration, c,
						     MasterControl.SET_QUERYPROPERTIES);
	    } else {
		VirtualUniverse.mc.postRequest(MasterControl.SET_QUERYPROPERTIES, c);
	    }
	    runMonitor(J3dThread.WAIT);
	}
    }

    
    static void runMonitor(int action) {
	// user thread will locked the globalLock when Renderer
	// thread invoke this function so we can't use 
	// the same lock.
	synchronized (monitorLock) {
	    switch (action) {
	    case J3dThread.WAIT:
                // Issue 279 - loop until ready
		while (threadWaiting) {
		    try {
			monitorLock.wait();
		    } catch (InterruptedException e) {
			System.err.println(e);
		    }
		}
		break;
	    case J3dThread.NOTIFY:
		monitorLock.notify();
		threadWaiting = false;
		break;
	    }
	}
    }


    // Return a string representing the value, one of:
    // REQUIRED, PREFERRED, or UNNECESSARY
    private static final String enumStr(int val) {
	switch (val) {
	case REQUIRED:
	    return "REQUIRED";
	case PREFERRED:
	    return "PREFERRED";
	case UNNECESSARY:
	    return "UNNECESSARY";
	}

	return "UNDEFINED";
    }

    /**
     * Returns a string representation of this object.
     * @return a string representation of this object.
     */
    public String toString() {
	return
	    "redSize : " + redSize + ", " +
	    "greenSize : " + greenSize + ", " +
	    "blueSize : " + blueSize + ", " +
	    "depthSize : " + depthSize + ", " +
	    "doubleBuffer : " + enumStr(doubleBuffer) + ", " +
	    "sceneAntialiasing : " + enumStr(sceneAntialiasing) + ", " +
	    "stereo : " + enumStr(stereo);
    }
}
