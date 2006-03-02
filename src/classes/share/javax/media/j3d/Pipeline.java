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
 * Abstract pipeline class for rendering pipeline methods. All rendering
 * pipeline methods will eventually go here.
 */
abstract class Pipeline {
    // Singleton pipeline instance
    private static Pipeline pipeline;

    // Supported Rendering APIs
    static final int NATIVE_OGL = 1;
    static final int NATIVE_D3D = 2;
    static final int JOGL = 3;

    // Type of renderer (as defined above)
    private int rendererType = -1;

    protected Pipeline() {
    }

    /**
     * Initialize the Pipeline. Called exactly once by
     * MasterControl.loadLibraries() to create the singleton
     * Pipeline object.
     */
    static void createPipeline(int rendererType) {
        String className = null;
        switch (rendererType) {
        case NATIVE_OGL:
        case NATIVE_D3D:
            className = "javax.media.j3d.NativePipeline";
            break;
        case JOGL:
            className = "javax.media.j3d.JoglPipeline";
            break;
        default:
            assert false; // Should not get here
        }

        final String pipelineClassName = className;
        pipeline = (Pipeline)
            java.security.AccessController.doPrivileged(new
                java.security.PrivilegedAction() {
                    public Object run() {
                        try {
                            Class pipelineClass = Class.forName(pipelineClassName);
                            return pipelineClass.newInstance();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

        pipeline.initialize(rendererType);
    }

    /**
     * Returns the singleton Pipeline object.
     */
    static Pipeline getPipeline() {
        return pipeline;
    }

    /**
     * Initializes the pipeline object. Only called by initPipeline.
     * Pipeline subclasses may override this, but must call
     * super.initialize(renderType);
     */
    void initialize(int rendererType) {
        setRendererType(rendererType);
    }

    /**
     * Sets the renderer type. Only called by initialize.
     */
    private void setRendererType(int rendererType) {
        this.rendererType = rendererType;
    }

    /**
     * Returns the renderer type
     */
    int getRendererType() {
        return rendererType;
    }

    // ---------------------------------------------------------------------

    //
    // Methods to initialize load required libraries (from MasterControl)
    //

    /**
     * Load all of the required libraries
     */
    abstract void loadLibraries(int globalShadingLanguage);

    /**
     * Returns true if the Cg library is loaded and available. Note that this
     * does not necessarily mean that Cg is supported by the graphics card.
     */
    abstract boolean isCgLibraryAvailable();

    /**
     * Returns true if the GLSL library is loaded and available. Note that this
     * does not necessarily mean that GLSL is supported by the graphics card.
     */
    abstract boolean isGLSLLibraryAvailable();

}
