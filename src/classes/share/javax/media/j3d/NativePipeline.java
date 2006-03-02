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

import java.io.File;
import java.util.ArrayList;

/**
 * Concrete implementation of Pipeline class for native OGL and D3D rendering
 * pipeline.
 */
class NativePipeline extends Pipeline{

    // System properties containing the native library search PATH
    // The order listed is the order in which they will be searched
    private static final String[] systemPathProps = {
        "sun.boot.library.path",
        "java.library.path"
    };

    // Prefix for native libraries
    private static final String libPrefix = "j3dcore";

    // Boolean indicating whether we are using D3D or OGL
    private boolean isD3D;

    // Renderer name, either "ogl" or "d3d"
    private String rendererName;

    // Flags indicating whether the Cg or GLSL libraries are available.
    private boolean cgLibraryAvailable = false;
    private boolean glslLibraryAvailable = false;

    /**
     * Constructor for singleton NativePipeline instance
     */
    protected NativePipeline() {
    }

    /**
     * Initialize the pipeline
     */
    void initialize(int rendererType) {
        super.initialize(rendererType);

        // This works around a native load library bug
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            toolkit = null;   // just making sure GC collects this
        } catch (java.awt.AWTError e) {
        }

        switch (rendererType) {
        case NATIVE_OGL:
            isD3D = false;
            rendererName = "ogl";
            break;
        case NATIVE_D3D:
            isD3D = true;
            rendererName = "d3d";
            break;
        default:
            assert false; // Should never get here
        }
    }

    /**
     * Load all of the required libraries
     */
    void loadLibraries(int globalShadingLanguage) {
        // Load the native JAWT library
        loadLibrary("jawt");

        // Load the native rendering library
        String libraryName = libPrefix + "-" + rendererName;
        loadLibrary(libraryName);

        // Check whether the Cg library is available
        if (globalShadingLanguage == Shader.SHADING_LANGUAGE_CG) {
            String cgLibraryName = libPrefix + "-" + rendererName + "-cg";
            String[] libpath = setupLibPath(cgLibraryName);
            cgLibraryAvailable = loadNativeCgLibrary(libpath);
        }

        // Check whether the GLSL library is available
        if (globalShadingLanguage == Shader.SHADING_LANGUAGE_GLSL) {
            if (getRendererType() == NATIVE_OGL) {
                // No need to verify that GLSL is available, since GLSL is part
                // of OpenGL as an extension (or part of core in 2.0)
                glslLibraryAvailable = true;
            }
        }
    }

    /**
     * Returns true if the Cg library is loaded and available. Note that this
     * does not necessarily mean that Cg is supported by the graphics card.
     */
    boolean isCgLibraryAvailable() {
        return cgLibraryAvailable;
    }

    /**
     * Returns true if the GLSL library is loaded and available. Note that this
     * does not necessarily mean that GLSL is supported by the graphics card.
     */
    boolean isGLSLLibraryAvailable() {
        return glslLibraryAvailable;
    }

    /** 
     * Load the specified native library.
     */
    private void loadLibrary(String libName) {
        final String libraryName = libName;
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    System.loadLibrary(libraryName);
                    return null;
                }
            });
    }

    /**
     * Parse the specified System properties containing a PATH and return an
     * array of Strings, where each element is an absolute filename consisting of
     * the individual component of the path concatenated with the (relative)
     * library file name. Only those absolute filenames that exist are included.
     * If no absolute filename is found, we will try the relative library name.
     */
    private static String[] setupLibPath(String libName) {
        final String libraryName = libName;
        return (String[])
            java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        ArrayList pathList = new ArrayList();

                        String filename = System.mapLibraryName(libraryName);
                        for (int n = 0; n < systemPathProps.length; n++) {
                            String pathString = System.getProperty(systemPathProps[n]);
                            boolean done = false;
                            int posStart = 0;
                            while (!done) {
                                int posEnd = pathString.indexOf(File.pathSeparator, posStart);
                                if (posEnd == -1) {
                                    posEnd = pathString.length();
                                    done = true;
                                }
                                String pathDir = pathString.substring(posStart, posEnd);
                                File pathFile = new File(pathDir, filename);
                                if (pathFile.exists()) {
                                    pathList.add(pathFile.getAbsolutePath());
                                }

                                posStart = posEnd + 1;
                            }
                        }

                        // If no absolute path names exist, add in the relative library name
                        if (pathList.size() == 0) {
                            pathList.add(filename);
                        }

                        return (String[])pathList.toArray(new String[0]);
                    }
                });
    }

    // Method to verify whether the native Cg library is available
    private static native boolean loadNativeCgLibrary(String[] libpath);

}
