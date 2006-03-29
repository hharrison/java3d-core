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
    private String[] setupLibPath(String libName) {
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
    private native boolean loadNativeCgLibrary(String[] libpath);

    // ---------------------------------------------------------------------

    //
    // GeometryArrayRetained methods
    //

    // Used by D3D to free vertex buffer
    native void freeD3DArray(GeometryArrayRetained geo, boolean deleteVB);

    // used for GeometryArrays by Copy or interleaved
    native void execute(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] cdata, int texUnitIndex, int cdirty);

    // used by GeometryArray by Reference with java arrays
    native void executeVA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int coordIndex, float[] vfcoords, double[] vdcoords,
            int colorIndex, float[] cfdata, byte[] cbdata,
            int normalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndex, float[][] vertexAttrData,
            int pass, int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty);

    // used by GeometryArray by Reference with NIO buffer
    native void executeVABuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int coordIndex,
            Object vcoords,
            int colorIndex,
            Object cdataBuffer,
            float[] cfdata, byte[] cbdata,
            int normalIndex, Object ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndex, Object[] vertexAttrData,
            int pass, int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty);

    // used by GeometryArray by Reference in interleaved format with NIO buffer
    native void executeInterleavedBuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            Object varray, float[] cdata, int texUnitIndex, int cdirty);

    native void setVertexFormat(long ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors);

    native void disableGlobalAlpha(long ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors);

    // used for GeometryArrays
    native void buildGA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale, boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int startVIndex,
            int vcount, int vformat,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen, int[] texCoordSetMapOffset,
            int vertexAttrCount, int[] vertexAttrSizes,    
            double[] xform, double[] nxform,
            float[] varray);

    // used to Build Dlist GeometryArray by Reference with java arrays
    native void buildGAForByRef(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,  boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int coordIndex, float[] vfcoords, double[] vdcoords,
            int colorIndex, float[] cfdata, byte[] cbdata,
            int normalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndex, float[][] vertexAttrData,
            int texcoordmaplength,
            int[] texcoordoffset,
            int[] texIndex, int texstride, Object[] texCoords,
            double[] xform, double[] nxform);

    // ---------------------------------------------------------------------

    //
    // IndexedGeometryArrayRetained methods
    //

    // by-copy or interleaved, by reference, Java arrays
    native void executeIndexedGeometry(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            float[] varray, float[] cdata,
            int texUnitIndex, int cdirty,
            int[] indexCoord);

    // interleaved, by reference, nio buffer
    native void executeIndexedGeometryBuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            Object varray, float[] cdata,
            int texUnitIndex, int cdirty,
            int[] indexCoord);

    // non interleaved, by reference, Java arrays
    native void executeIndexedGeometryVA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount,
            int vformat,
            int vdefined,
            float[] vfcoords, double[] vdcoords,
            float[] cfdata, byte[] cbdata,
            float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[][] vertexAttrData,
            int pass, int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord);

    // non interleaved, by reference, nio buffer
    native void executeIndexedGeometryVABuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount,
            int vformat,
            int vdefined,
            Object vcoords,
            Object cdataBuffer,
            float[] cfdata, byte[] cbdata,
            Object normal,
            int vertexAttrCount, int[] vertexAttrSizes,
            Object[] vertexAttrData,
            int pass, int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord);

    // by-copy geometry
    native void buildIndexedGeometry(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale, boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount,
            int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int texCoordSetMap[],
            int texCoordSetMapLen,
            int[] texCoordSetMapOffset,
            double[] xform, double[] nxform,
            float[] varray, int[] indexCoord);


    // ---------------------------------------------------------------------

    //
    // GraphicsContext3D methods
    //

    // Native method for readRaster
    native void readRasterNative(long ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas, int format,
            ImageComponentRetained image,
            DepthComponentRetained depth,
            GraphicsContext3D gc);


    // ---------------------------------------------------------------------

    //
    // CgShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    native ShaderError setCgUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    native ShaderError setCgUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    native ShaderError setCgUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setCgUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setCgUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setCgUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setCgUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setCgUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setCgUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setCgUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    native ShaderError setCgUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setCgUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setCgUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setCgUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setCgUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setCgUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setCgUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setCgUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setCgUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setCgUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    // Native interfaces for shader compilation, etc.
    native ShaderError createCgShader(long ctx, int shaderType, long[] shaderId);
    native ShaderError destroyCgShader(long ctx, long shaderId);
    native ShaderError compileCgShader(long ctx, long shaderId, String program);

    native ShaderError createCgShaderProgram(long ctx, long[] shaderProgramId);
    native ShaderError destroyCgShaderProgram(long ctx, long shaderProgramId);
    native ShaderError linkCgShaderProgram(long ctx, long shaderProgramId,
            long[] shaderId);
    native void lookupCgVertexAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr);
    native void lookupCgShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    native ShaderError useCgShaderProgram(long ctx, long shaderProgramId);


    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    native ShaderError setGLSLUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    native ShaderError setGLSLUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    native ShaderError setGLSLUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setGLSLUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setGLSLUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setGLSLUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setGLSLUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    native ShaderError setGLSLUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setGLSLUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    native ShaderError setGLSLUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    native ShaderError setGLSLUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setGLSLUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setGLSLUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setGLSLUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setGLSLUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setGLSLUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setGLSLUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    native ShaderError setGLSLUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setGLSLUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    native ShaderError setGLSLUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    // native interfaces for shader compilation, etc.
    native ShaderError createGLSLShader(long ctx, int shaderType, long[] shaderId);
    native ShaderError destroyGLSLShader(long ctx, long shaderId);
    native ShaderError compileGLSLShader(long ctx, long shaderId, String program);

    native ShaderError createGLSLShaderProgram(long ctx, long[] shaderProgramId);
    native ShaderError destroyGLSLShaderProgram(long ctx, long shaderProgramId);
    native ShaderError linkGLSLShaderProgram(long ctx, long shaderProgramId,
            long[] shaderId);
    native ShaderError bindGLSLVertexAttrName(long ctx, long shaderProgramId,
            String attrName, int attrIndex);
    native void lookupGLSLShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    native ShaderError useGLSLShaderProgram(long ctx, long shaderProgramId);

}
