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

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.io.File;
import java.util.ArrayList;

/**
 * Concrete implementation of Pipeline class for native OGL and D3D rendering
 * pipeline.
 */
class NativePipeline extends Pipeline {

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
     * The platform dependent template.  Since there is no
     * template-specific instance data in the NativeConfigTemplate3D
     * class, we can create one statically.
     */
    private static NativeConfigTemplate3D nativeTemplate = new NativeConfigTemplate3D();

    /**
     * Constructor for singleton NativePipeline instance
     */
    protected NativePipeline() {
    }

    /**
     * Initialize the pipeline
     */
    void initialize(Pipeline.Type pipelineType) {
        super.initialize(pipelineType);

        // This works around a native load library bug
        try {
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            toolkit = null;   // just making sure GC collects this
        } catch (java.awt.AWTError e) {
        }

        switch (pipelineType) {
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
            if (getPipelineType() == Pipeline.Type.NATIVE_OGL) {
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


    //
    // Methods to box/unbox various native objects
    //

    private long unbox(Context ctx) {
        if (ctx == null) {
            return 0;
        } else {
            return ((NativeContext)ctx).getNativeCtx();
        }
    }

    private Context boxContext(long nativeCtx) {
        if (nativeCtx == 0) {
            return null;
        } else {
            return new NativeContext(nativeCtx);
        }
    }

    private long unbox(Drawable drawable) {
        if (drawable == null) {
            return 0;
        } else {
            return ((NativeDrawable)drawable).getNativeDrawable();
        }
    }

    private Drawable boxDrawable(long nativeDrawable) {
        if (nativeDrawable == 0) {
            return null;
        } else {
            return new NativeDrawable(nativeDrawable);
        }
    }

    private long unbox(ShaderProgramId shaderProgramId) {
        if (shaderProgramId == null) {
            return 0;
        } else {
            return ((NativeShaderObject)shaderProgramId).getNativeId();
        }
    }

    private ShaderProgramId boxShaderProgramId(long nativeId) {
        if (nativeId == 0) {
            return null;
        } else {
            return new NativeShaderObject(nativeId);
        }
    }

    private long unbox(ShaderId shaderId) {
        if (shaderId == null) {
            return 0;
        } else {
            return ((NativeShaderObject)shaderId).getNativeId();
        }
    }

    private ShaderId boxShaderId(long nativeId) {
        if (nativeId == 0) {
            return null;
        } else {
            return new NativeShaderObject(nativeId);
        }
    }

    private long unbox(ShaderAttrLoc attrLoc) {
        if (attrLoc == null) {
            return -1;
        } else {
            return ((NativeShaderObject)attrLoc).getNativeId();
        }
    }

    private ShaderAttrLoc boxShaderAttrLoc(long nativeId) {
        if (nativeId == -1) {
            return null;
        } else {
            return new NativeShaderObject(nativeId);
        }
    }

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
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] cdata, int cdirty);

    void execute(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] cdata, int cdirty) {
        execute(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                useAlpha,
                ignoreVertexColors,
                startVIndex, vcount, vformat,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen,
                texCoordSetOffset,
                numActiveTexUnitState,
                vertexAttrCount, vertexAttrSizes,
                varray, cdata, cdirty);
    }

    // used by GeometryArray by Reference with java arrays
    native void executeVA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty);

    void executeVA(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty) {
        executeVA(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                ignoreVertexColors,
                vcount,
                vformat,
                vdefined,
                coordIndex, vfcoords, vdcoords,
                colorIndex, cfdata, cbdata,
                normalIndex, ndata,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndex, vertexAttrData,
                texcoordmaplength,
                texcoordoffset,
                numActiveTexUnitState,
                texIndex, texstride, texCoords,
                cdirty);
    }

    // used by GeometryArray by Reference with NIO buffer
    native void executeVABuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty);

    void executeVABuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty) {
        executeVABuffer(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                ignoreVertexColors,
                vcount,
                vformat,
                vdefined,
                coordIndex,
                vcoords,
                colorIndex,
                cdataBuffer,
                cfdata, cbdata,
                normalIndex, ndata,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndex, vertexAttrData,
                texcoordmaplength,
                texcoordoffset,
                numActiveTexUnitState,
                texIndex, texstride, texCoords,
                cdirty);
    }

    // used by GeometryArray by Reference in interleaved format with NIO buffer
    native void executeInterleavedBuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            Object varray, float[] cdata, int cdirty);

    void executeInterleavedBuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            Object varray, float[] cdata, int cdirty) {
        executeInterleavedBuffer(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                useAlpha,
                ignoreVertexColors,
                startVIndex, vcount, vformat,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen,
                texCoordSetOffset,
                numActiveTexUnitState,
                varray, cdata, cdirty);
    }

    native void setVertexFormat(long ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors);

    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
        setVertexFormat(unbox(ctx), geo,
                vformat, useAlpha, ignoreVertexColors);
    }

    native void disableGlobalAlpha(long ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors);

    void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors) {
        disableGlobalAlpha(unbox(ctx), geo, vformat,
                useAlpha, ignoreVertexColors);
    }

    // used for GeometryArrays
    native void buildGA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale, boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int startVIndex,
            int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen, int[] texCoordSetMapOffset,
            int vertexAttrCount, int[] vertexAttrSizes,
            double[] xform, double[] nxform,
            float[] varray);

    void buildGA(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale, boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int startVIndex,
            int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen, int[] texCoordSetMapOffset,
            int vertexAttrCount, int[] vertexAttrSizes,
            double[] xform, double[] nxform,
            float[] varray) {
        buildGA(unbox(ctx),
                geo, geo_type,
                isNonUniformScale, updateAlpha,
                alpha,
                ignoreVertexColors,
                startVIndex,
                vcount, vformat,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen, texCoordSetMapOffset,
                vertexAttrCount, vertexAttrSizes,
                xform, nxform,
                varray);
    }

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

    void buildGAForByRef(Context ctx,
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
            double[] xform, double[] nxform) {
        buildGAForByRef(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,  updateAlpha,
                alpha,
                ignoreVertexColors,
                vcount,
                vformat,
                vdefined,
                coordIndex, vfcoords, vdcoords,
                colorIndex, cfdata, cbdata,
                normalIndex, ndata,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndex, vertexAttrData,
                texcoordmaplength,
                texcoordoffset,
                texIndex, texstride, texCoords,
                xform, nxform);
    }

    // ---------------------------------------------------------------------

    //
    // IndexedGeometryArrayRetained methods
    //

    // by-copy or interleaved, by reference, Java arrays
    native void executeIndexedGeometry(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            float[] varray, float[] cdata,
            int cdirty,
            int[] indexCoord);

    void executeIndexedGeometry(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            float[] varray, float[] cdata,
            int cdirty,
            int[] indexCoord) {
        executeIndexedGeometry(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                useAlpha,
                ignoreVertexColors,
                initialIndexIndex,
                indexCount,
                vertexCount, vformat,
                vertexAttrCount, vertexAttrSizes,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen,
                texCoordSetOffset,
                numActiveTexUnitState,
                varray, cdata,
                cdirty,
                indexCoord);
    }

    // interleaved, by reference, nio buffer
    native void executeIndexedGeometryBuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            Object varray, float[] cdata,
            int cdirty,
            int[] indexCoord);

    void executeIndexedGeometryBuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            Object varray, float[] cdata,
            int cdirty,
            int[] indexCoord) {
        executeIndexedGeometryBuffer(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                useAlpha,
                ignoreVertexColors,
                initialIndexIndex,
                indexCount,
                vertexCount, vformat,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen,
                texCoordSetOffset,
                numActiveTexUnitState,
                varray, cdata,
                cdirty,
                indexCoord);
    }

    // non interleaved, by reference, Java arrays
    native void executeIndexedGeometryVA(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord);

    void executeIndexedGeometryVA(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord) {
        executeIndexedGeometryVA(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                ignoreVertexColors,
                initialIndexIndex,
                validIndexCount,
                vertexCount,
                vformat,
                vdefined,
                vfcoords, vdcoords,
                cfdata, cbdata,
                ndata,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrData,
                texcoordmaplength,
                texcoordoffset,
                numActiveTexUnitState,
                texstride, texCoords,
                cdirty,
                indexCoord);
    }

    // non interleaved, by reference, nio buffer
    native void executeIndexedGeometryVABuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord);

    void executeIndexedGeometryVABuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
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
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord) {
        executeIndexedGeometryVABuffer(unbox(ctx),
                geo, geo_type,
                isNonUniformScale,
                ignoreVertexColors,
                initialIndexIndex,
                validIndexCount,
                vertexCount,
                vformat,
                vdefined,
                vcoords,
                cdataBuffer,
                cfdata, cbdata,
                normal,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrData,
                texcoordmaplength,
                texcoordoffset,
                numActiveTexUnitState,
                texstride, texCoords,
                cdirty,
                indexCoord);
    }

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
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetMapOffset,
            double[] xform, double[] nxform,
            float[] varray, int[] indexCoord);

    void buildIndexedGeometry(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale, boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount,
            int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetMapOffset,
            double[] xform, double[] nxform,
            float[] varray, int[] indexCoord) {
        buildIndexedGeometry(unbox(ctx),
                geo, geo_type,
                isNonUniformScale, updateAlpha,
                alpha,
                ignoreVertexColors,
                initialIndexIndex,
                validIndexCount,
                vertexCount,
                vformat,
                vertexAttrCount, vertexAttrSizes,
                texCoordSetCount, texCoordSetMap,
                texCoordSetMapLen,
                texCoordSetMapOffset,
                xform, nxform,
                varray, indexCoord);
    }


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

    void readRasterNative(Context ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas, int format,
            ImageComponentRetained image,
            DepthComponentRetained depth,
            GraphicsContext3D gc) {
        readRasterNative(unbox(ctx),
                type, xSrcOffset, ySrcOffset,
                width, height, hCanvas, format,
                image,
                depth,
                gc);
    }


    // ---------------------------------------------------------------------

    //
    // CgShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    native ShaderError setCgUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    ShaderError setCgUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {
        return setCgUniform1i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    ShaderError setCgUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        return setCgUniform1f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setCgUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setCgUniform2i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setCgUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setCgUniform2f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setCgUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setCgUniform3i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setCgUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setCgUniform3f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setCgUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setCgUniform4i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setCgUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setCgUniform4f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setCgUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setCgUniformMatrix3f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setCgUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setCgUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setCgUniformMatrix4f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    // ShaderAttributeArray methods

    native ShaderError setCgUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setCgUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setCgUniform1iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniform1fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setCgUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setCgUniform2iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniform2fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setCgUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setCgUniform3iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniform3fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setCgUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setCgUniform4iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniform4fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniformMatrix3fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setCgUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setCgUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setCgUniformMatrix4fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    // Native interfaces for shader compilation, etc.
    native ShaderError createCgShader(long ctx, int shaderType, long[] shaderId);

    ShaderError createCgShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        long[] nativeId = new long[1];
        ShaderError err = createCgShader(unbox(ctx), shaderType, nativeId);
        shaderId[0] = boxShaderId(nativeId[0]);
        return err;
    }

    native ShaderError destroyCgShader(long ctx, long shaderId);

    ShaderError destroyCgShader(Context ctx, ShaderId shaderId) {
        return destroyCgShader(unbox(ctx), unbox(shaderId));
    }

    native ShaderError compileCgShader(long ctx, long shaderId, String program);

    ShaderError compileCgShader(Context ctx, ShaderId shaderId, String program) {
        return compileCgShader(unbox(ctx), unbox(shaderId), program);
    }

    native ShaderError createCgShaderProgram(long ctx, long[] shaderProgramId);

    ShaderError createCgShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        long[] nativeId = new long[1];
        ShaderError err = createCgShaderProgram(unbox(ctx), nativeId);
        shaderProgramId[0] = boxShaderProgramId(nativeId[0]);
        return err;
    }

    native ShaderError destroyCgShaderProgram(long ctx, long shaderProgramId);

    ShaderError destroyCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return destroyCgShaderProgram(unbox(ctx), unbox(shaderProgramId));
    }

    native ShaderError linkCgShaderProgram(long ctx, long shaderProgramId,
            long[] shaderIds);

    ShaderError linkCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {

        assert shaderIds != null;
        long[] nativeIds = new long[shaderIds.length];
        for (int i = 0; i < shaderIds.length; i++) {
            nativeIds[i] = unbox(shaderIds[i]);
        }
        return linkCgShaderProgram(unbox(ctx), unbox(shaderProgramId),
                nativeIds);
    }

    native void lookupCgVertexAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr);

    void lookupCgVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr) {
        lookupCgVertexAttrNames(unbox(ctx), unbox(shaderProgramId),
                numAttrNames, attrNames, errArr);
    }

    native void lookupCgShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    void lookupCgShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {

        assert numAttrNames == locArr.length;
        long[] nativeLocArr = new long[numAttrNames];
        for (int i = 0; i < numAttrNames; i++) {
            // Initialize to invalid native location
            nativeLocArr[i] = -1;
        }
        lookupCgShaderAttrNames(unbox(ctx), unbox(shaderProgramId),
                numAttrNames, attrNames, nativeLocArr,
                typeArr, sizeArr, isArrayArr);
        for (int i = 0; i < numAttrNames; i++) {
            locArr[i] = boxShaderAttrLoc(nativeLocArr[i]);
        }
    }

    native ShaderError useCgShaderProgram(long ctx, long shaderProgramId);

    ShaderError useCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return useCgShaderProgram(unbox(ctx), unbox(shaderProgramId));
    }

    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    native ShaderError setGLSLUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    ShaderError setGLSLUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {
        return setGLSLUniform1i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        return setGLSLUniform1f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setGLSLUniform2i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setGLSLUniform2f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setGLSLUniform3i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setGLSLUniform3f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return setGLSLUniform4i(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setGLSLUniform4f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setGLSLUniformMatrix3f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    native ShaderError setGLSLUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return setGLSLUniformMatrix4f(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                value);
    }

    // ShaderAttributeArray methods

    native ShaderError setGLSLUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setGLSLUniform1iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniform1fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setGLSLUniform2iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniform2fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setGLSLUniform3iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniform3fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return setGLSLUniform4iArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniform4fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniformMatrix3fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    native ShaderError setGLSLUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return setGLSLUniformMatrix4fArray(unbox(ctx),
                unbox(shaderProgramId),
                unbox(uniformLocation),
                numElements,
                value);
    }

    // native interfaces for shader compilation, etc.
    native ShaderError createGLSLShader(long ctx, int shaderType, long[] shaderId);

    ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        long[] nativeId = new long[1];
        ShaderError err = createGLSLShader(unbox(ctx), shaderType, nativeId);
        shaderId[0] = boxShaderId(nativeId[0]);
        return err;
    }
    native ShaderError destroyGLSLShader(long ctx, long shaderId);

    ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId) {
        return destroyGLSLShader(unbox(ctx), unbox(shaderId));
    }
    native ShaderError compileGLSLShader(long ctx, long shaderId, String program);

    ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program) {
        return compileGLSLShader(unbox(ctx), unbox(shaderId), program);
    }

    native ShaderError createGLSLShaderProgram(long ctx, long[] shaderProgramId);

    ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        long[] nativeId = new long[1];
        ShaderError err = createGLSLShaderProgram(unbox(ctx), nativeId);
        shaderProgramId[0] = boxShaderProgramId(nativeId[0]);
        return err;
    }
    native ShaderError destroyGLSLShaderProgram(long ctx, long shaderProgramId);

    ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return destroyGLSLShaderProgram(unbox(ctx), unbox(shaderProgramId));
    }
    native ShaderError linkGLSLShaderProgram(long ctx, long shaderProgramId,
            long[] shaderId);

    ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
        assert shaderIds != null;
        long[] nativeIds = new long[shaderIds.length];
        for (int i = 0; i < shaderIds.length; i++) {
            nativeIds[i] = unbox(shaderIds[i]);
        }
        return linkGLSLShaderProgram(unbox(ctx), unbox(shaderProgramId),
                nativeIds);
    }
    native ShaderError bindGLSLVertexAttrName(long ctx, long shaderProgramId,
            String attrName, int attrIndex);

    ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex) {
        return bindGLSLVertexAttrName(unbox(ctx), unbox(shaderProgramId),
                attrName, attrIndex);
    }
    native void lookupGLSLShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    void lookupGLSLShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {

        assert numAttrNames == locArr.length;
        long[] nativeLocArr = new long[numAttrNames];
        for (int i = 0; i < numAttrNames; i++) {
            // Initialize to invalid native location
            nativeLocArr[i] = -1;
        }
        lookupGLSLShaderAttrNames(unbox(ctx), unbox(shaderProgramId),
                numAttrNames, attrNames, nativeLocArr,
                typeArr, sizeArr, isArrayArr);
        for (int i = 0; i < numAttrNames; i++) {
            locArr[i] = boxShaderAttrLoc(nativeLocArr[i]);
        }
    }

    native ShaderError useGLSLShaderProgram(long ctx, long shaderProgramId);

    ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return useGLSLShaderProgram(unbox(ctx), unbox(shaderProgramId));
    }


    // ---------------------------------------------------------------------

    //
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    native void freeD3DSurface(ImageComponent2DRetained image, int hashId);


    // ---------------------------------------------------------------------

    //
    // Renderer methods
    //

    native void cleanupRenderer();


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    native void updateColoringAttributes(long ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lEnable,
            int shadeModel);

    void updateColoringAttributes(Context ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lEnable,
            int shadeModel) {
        updateColoringAttributes(unbox(ctx),
                dRed, dGreen, dBlue,
                red, green, blue,
                alpha,
                lEnable,
                shadeModel);
    }


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    native void updateDirectionalLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z);

    void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z) {
        updateDirectionalLight(unbox(ctx),
                lightSlot, red, green,
                blue, x, y, z);
    }


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    native void updatePointLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz);

    void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz) {
        updatePointLight(unbox(ctx),
                lightSlot, red, green,
                blue, ax, ay, az,
                px, py, pz);
    }


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    native void updateSpotLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz, float spreadAngle,
            float concentration, float dx, float dy,
            float dz);

    void updateSpotLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz, float spreadAngle,
            float concentration, float dx, float dy,
            float dz) {
        updateSpotLight(unbox(ctx),
                lightSlot, red, green,
                blue, ax, ay, az,
                px, py, pz, spreadAngle,
                concentration, dx, dy,
                dz);
    }


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    native void updateExponentialFog(long ctx,
            float red, float green, float blue,
            float density);

    void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density) {
        updateExponentialFog(unbox(ctx),
                red, green, blue,
                density);
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    native void updateLinearFog(long ctx,
            float red, float green, float blue,
            double fdist, double bdist);

    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
        updateLinearFog(unbox(ctx),
                red, green, blue,
                fdist, bdist);
    }


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    native void updateLineAttributes(long ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing);

    void updateLineAttributes(Context ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing) {
        updateLineAttributes(unbox(ctx),
                lineWidth, linePattern,
                linePatternMask,
                linePatternScaleFactor,
                lineAntialiasing);
    }


    // ---------------------------------------------------------------------

    //
    // MaterialRetained methods
    //

    native void updateMaterial(long ctx,
            float red, float green, float blue, float alpha,
            float ared, float agreen, float ablue,
            float ered, float egreen, float eblue,
            float dred, float dgreen, float dblue,
            float sred, float sgreen, float sblue,
            float shininess, int colorTarget, boolean enable);

    void updateMaterial(Context ctx,
            float red, float green, float blue, float alpha,
            float ared, float agreen, float ablue,
            float ered, float egreen, float eblue,
            float dred, float dgreen, float dblue,
            float sred, float sgreen, float sblue,
            float shininess, int colorTarget, boolean enable) {
        updateMaterial(unbox(ctx),
                red, green, blue, alpha,
                ared, agreen, ablue,
                ered, egreen, eblue,
                dred, dgreen, dblue,
                sred, sgreen, sblue,
                shininess, colorTarget, enable);
    }


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    native void updateModelClip(long ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D);

    void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D) {
        updateModelClip(unbox(ctx), planeNum, enableFlag,
                A, B, C, D);
    }


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    native void updatePointAttributes(long ctx, float pointSize, boolean pointAntialiasing);

    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
        updatePointAttributes(unbox(ctx), pointSize, pointAntialiasing);
    }


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    native void updatePolygonAttributes(long ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor);

    void updatePolygonAttributes(Context ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor) {
        updatePolygonAttributes(unbox(ctx),
                polygonMode, cullFace,
                backFaceNormalFlip,
                polygonOffset,
                polygonOffsetFactor);
    }


    // ---------------------------------------------------------------------

    //
    // RenderingAttributesRetained methods
    //

    // TODO : Need to handle stencil operation on the native side -- Chien
    native void updateRenderingAttributes(long ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride,
            boolean depthBufferEnable,
            boolean depthBufferWriteEnable,
            int depthTestFunction,
            float alphaTestValue, int alphaTestFunction,
            boolean ignoreVertexColors,
            boolean rasterOpEnable, int rasterOp,
            boolean userStencilAvailable, boolean stencilEnable,
            int stencilFailOp, int stencilZFailOp, int stencilZPassOp,
            int stencilFunction, int stencilReferenceValue,
            int stencilCompareMask, int stencilWriteMask );

    void updateRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride,
            boolean depthBufferEnable,
            boolean depthBufferWriteEnable,
            int depthTestFunction,
            float alphaTestValue, int alphaTestFunction,
            boolean ignoreVertexColors,
            boolean rasterOpEnable, int rasterOp,
            boolean userStencilAvailable, boolean stencilEnable,
            int stencilFailOp, int stencilZFailOp, int stencilZPassOp,
            int stencilFunction, int stencilReferenceValue,
            int stencilCompareMask, int stencilWriteMask ) {
        updateRenderingAttributes(unbox(ctx),
                depthBufferWriteEnableOverride,
                depthBufferEnableOverride,
                depthBufferEnable,
                depthBufferWriteEnable,
                depthTestFunction,
                alphaTestValue, alphaTestFunction,
                ignoreVertexColors,
                rasterOpEnable, rasterOp,
                userStencilAvailable, stencilEnable,
                stencilFailOp, stencilZFailOp, stencilZPassOp,
                stencilFunction, stencilReferenceValue,
                stencilCompareMask, stencilWriteMask );
    }


    // ---------------------------------------------------------------------

    //
    // TexCoordGenerationRetained methods
    //

    /**
     * This method updates the native context:
     * trans contains eyeTovworld transform in d3d
     * trans contains vworldToEye transform in ogl
     */
    native void updateTexCoordGeneration(long ctx,
            boolean enable, int genMode, int format,
            float planeSx, float planeSy, float planeSz, float planeSw,
            float planeTx, float planeTy, float planeTz, float planeTw,
            float planeRx, float planeRy, float planeRz, float planeRw,
            float planeQx, float planeQy, float planeQz, float planeQw,
            double[] trans);

    void updateTexCoordGeneration(Context ctx,
            boolean enable, int genMode, int format,
            float planeSx, float planeSy, float planeSz, float planeSw,
            float planeTx, float planeTy, float planeTz, float planeTw,
            float planeRx, float planeRy, float planeRz, float planeRw,
            float planeQx, float planeQy, float planeQz, float planeQw,
            double[] trans) {
        updateTexCoordGeneration(unbox(ctx),
                enable, genMode, format,
                planeSx, planeSy, planeSz, planeSw,
                planeTx, planeTy, planeTz, planeTw,
                planeRx, planeRy, planeRz, planeRw,
                planeQx, planeQy, planeQz, planeQw,
                trans);
    }


    // ---------------------------------------------------------------------

    //
    // TransparencyAttributesRetained methods
    //

    native void updateTransparencyAttributes(long ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction);

    void updateTransparencyAttributes(Context ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction) {
        updateTransparencyAttributes(unbox(ctx),
                alpha, geometryType,
                polygonMode,
                lineAA, pointAA,
                transparencyMode,
                srcBlendFunction,
                dstBlendFunction);
    }


    // ---------------------------------------------------------------------

    //
    // TextureAttributesRetained methods
    //

    native void updateTextureAttributes(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat);

    void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat) {
        updateTextureAttributes(unbox(ctx),
                transform, isIdentity, textureMode,
                perspCorrectionMode, red,
                green, blue, alpha,
                textureFormat);
    }

    native void updateRegisterCombiners(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);

    void updateRegisterCombiners(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
        updateRegisterCombiners(unbox(ctx),
                transform, isIdentity, textureMode,
                perspCorrectionMode, red,
                green, blue, alpha,
                textureFormat,
                combineRgbMode, combineAlphaMode,
                combineRgbSrc, combineAlphaSrc,
                combineRgbFcn, combineAlphaFcn,
                combineRgbScale, combineAlphaScale);
    }

    native void updateTextureColorTable(long ctx, int numComponents,
            int colorTableSize,
            int[] colorTable);

    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] colorTable) {
        updateTextureColorTable(unbox(ctx), numComponents,
                colorTableSize,
                colorTable);
    }

    native void updateCombiner(long ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);

    void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
        updateCombiner(unbox(ctx),
                combineRgbMode, combineAlphaMode,
                combineRgbSrc, combineAlphaSrc,
                combineRgbFcn, combineAlphaFcn,
                combineRgbScale, combineAlphaScale);
    }


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    native void updateTextureUnitState(long ctx, int unitIndex, boolean enableFlag);

    void updateTextureUnitState(Context ctx, int unitIndex, boolean enableFlag) {
        updateTextureUnitState(unbox(ctx), unitIndex, enableFlag);
    }


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    native void bindTexture2D(long ctx, int objectId, boolean enable);

    void bindTexture2D(Context ctx, int objectId, boolean enable) {
        bindTexture2D(unbox(ctx), objectId, enable);
    }

    native void updateTexture2DImage(long ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object data);

    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object data) {
        updateTexture2DImage(unbox(ctx),
                numLevels, level,
                textureFormat, imageFormat,
                width, height,
                boundaryWidth,
                imageDataType, data);
    }

    native void updateTexture2DSubImage(long ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object data);

    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object data) {
        updateTexture2DSubImage(unbox(ctx),
                level, xoffset, yoffset,
                textureFormat, imageFormat,
                imgXOffset, imgYOffset,
                tilew, width, height,
                imageDataType, data);
    }
    
    native void updateTexture2DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
        updateTexture2DLodRange(unbox(ctx),
                baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    native void updateTexture2DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
        updateTexture2DLodOffset(unbox(ctx),
                lodOffsetX, lodOffsetY,
                lodOffsetZ);
    }

    native void updateTexture2DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
        updateTexture2DBoundary(unbox(ctx),
                boundaryModeS, boundaryModeT,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    native void updateTexture2DFilterModes(long ctx,
            int minFilter, int magFilter);

    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
        updateTexture2DFilterModes(unbox(ctx),
                minFilter, magFilter);
    }

    native void updateTexture2DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        updateTexture2DSharpenFunc(unbox(ctx),
                numSharpenTextureFuncPts,
                sharpenTextureFuncPts);
    }

    native void updateTexture2DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        updateTexture2DFilter4Func(unbox(ctx),
                numFilter4FuncPts,
                filter4FuncPts);
    }

    native void updateTexture2DAnisotropicFilter(long ctx, float degree);

    void updateTexture2DAnisotropicFilter(Context ctx, float degree) {
        updateTexture2DAnisotropicFilter(unbox(ctx), degree);
    }


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    native void bindTexture3D(long ctx, int objectId, boolean enable);

    void bindTexture3D(Context ctx, int objectId, boolean enable) {
        bindTexture3D(unbox(ctx), objectId, enable);
    }

    native void updateTexture3DImage(long ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int imageDataType, Object imageData);

    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int imageDataType, Object imageData) {
        updateTexture3DImage(unbox(ctx),
                numLevels, level,
                textureFormat, imageFormat,
                width, height, depth,
                boundaryWidth,
                imageDataType, imageData);
    }

    native void updateTexture3DSubImage(long ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int imageDataType, Object imageData);

    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int imageDataType, Object imageData) {
       updateTexture3DSubImage(unbox(ctx),
                level,
                xoffset, yoffset, zoffset,
                textureFormat, imageFormat,
                imgXoffset, imgYoffset, imgZoffset,
                tilew, tileh,
                width, height, depth,
                imageDataType, imageData);
    }

    native void updateTexture3DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
        updateTexture3DLodRange(unbox(ctx),
                baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    native void updateTexture3DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
        updateTexture3DLodOffset(unbox(ctx),
                lodOffsetX, lodOffsetY,
                lodOffsetZ);
    }

    native void updateTexture3DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha);


    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
        updateTexture3DBoundary(unbox(ctx),
                boundaryModeS, boundaryModeT,
                boundaryModeR, boundaryRed,
                boundaryGreen, boundaryBlue,
                boundaryAlpha);
    }

    native void updateTexture3DFilterModes(long ctx,
            int minFilter, int magFilter);

    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
        updateTexture3DFilterModes(unbox(ctx),
                minFilter, magFilter);
    }

    native void updateTexture3DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        updateTexture3DSharpenFunc(unbox(ctx),
                numSharpenTextureFuncPts,
                sharpenTextureFuncPts);
    }

    native void updateTexture3DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        updateTexture3DFilter4Func(unbox(ctx),
                numFilter4FuncPts,
                filter4FuncPts);
    }

    native void updateTexture3DAnisotropicFilter(long ctx, float degree);

    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
        updateTexture3DAnisotropicFilter(unbox(ctx), degree);
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    native void bindTextureCubeMap(long ctx, int objectId, boolean enable);

    void bindTextureCubeMap(Context ctx, int objectId, boolean enable) {
        bindTextureCubeMap(unbox(ctx), objectId, enable);
    }

    native void updateTextureCubeMapImage(long ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object imageData);

    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object imageData) {
        updateTextureCubeMapImage(unbox(ctx),
                face, numLevels, level,
                textureFormat, imageFormat,
                width, height,
                boundaryWidth,
                imageDataType, imageData);
    }

    native void updateTextureCubeMapSubImage(long ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData);

    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData) {
        updateTextureCubeMapSubImage(unbox(ctx),
                face, level, xoffset, yoffset,
                textureFormat, imageFormat,
                imgXOffset, imgYOffset,
                tilew, width, height,
                imageDataType, imageData);
    }

    native void updateTextureCubeMapLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
        updateTextureCubeMapLodRange(unbox(ctx),
                baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    native void updateTextureCubeMapLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
        updateTextureCubeMapLodOffset(unbox(ctx),
                lodOffsetX, lodOffsetY,
                lodOffsetZ);
    }

    native void updateTextureCubeMapBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
        updateTextureCubeMapBoundary(unbox(ctx),
                boundaryModeS, boundaryModeT,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    native void updateTextureCubeMapFilterModes(long ctx,
            int minFilter, int magFilter);

    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
        updateTextureCubeMapFilterModes(unbox(ctx),
                minFilter, magFilter);
    }

    native void updateTextureCubeMapSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        updateTextureCubeMapSharpenFunc(unbox(ctx),
                numSharpenTextureFuncPts,
                sharpenTextureFuncPts);
    }

    native void updateTextureCubeMapFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        updateTextureCubeMapFilter4Func(unbox(ctx),
                numFilter4FuncPts,
                filter4FuncPts);
    }

    native void updateTextureCubeMapAnisotropicFilter(long ctx, float degree);

    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
        updateTextureCubeMapAnisotropicFilter(unbox(ctx), degree);
    }

    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Method to return the AWT object
    native long getAWT();

    // Method to initialize the native J3D library
    native boolean initializeJ3D(boolean disableXinerama);

    // Maximum lights supported by the native API
    native int getMaximumLights();


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods
    //

    // This is the native method for creating the underlying graphics context.
    native long createNewContext(Canvas3D cv, long display, long drawable,
            long fbConfig, long shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    // This is the native method for creating the underlying graphics context.
    Context createNewContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, Context shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {

        long nativeCtx = createNewContext(cv, display, unbox(drawable),
            fbConfig, unbox(shareCtx), isSharedCtx,
            offScreen,
            glslLibraryAvailable,
            cgLibraryAvailable);

        return boxContext(nativeCtx);
    }

    native void createQueryContext(Canvas3D cv, long display, long drawable,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    void createQueryContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {

        createQueryContext(cv, display, unbox(drawable),
            fbConfig, offScreen, width, height,
            glslLibraryAvailable,
            cgLibraryAvailable);
    }

    // This is the native for creating offscreen buffer
    native long createOffScreenBuffer(Canvas3D cv, long ctx, long display, long fbConfig, int width, int height);

    Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, int width, int height) {
        long nativeDrawable = createOffScreenBuffer(cv, unbox(ctx), display, fbConfig, width, height);
        return boxDrawable(nativeDrawable);
    }

    native void destroyOffScreenBuffer(Canvas3D cv, long ctx, long display, long fbConfig, long drawable);

    void destroyOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, Drawable drawable) {
        destroyOffScreenBuffer(cv, unbox(ctx), display, fbConfig, unbox(drawable));
    }
    
    // This is the native for reading the image from the offscreen buffer
    native void readOffScreenBuffer(Canvas3D cv, long ctx, int format, int type, Object data, int width, int height);
    
    void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int type, Object data, int width, int height) {
        readOffScreenBuffer(cv, unbox(ctx), format, type, data, width, height);
    }
    
    // The native method for swapBuffers
    native int swapBuffers(Canvas3D cv, long ctx, long dpy, long drawable);

    int swapBuffers(Canvas3D cv, Context ctx, long dpy, Drawable drawable) {
        return swapBuffers(cv, unbox(ctx), dpy, unbox(drawable));
    }


    // notify D3D that Canvas is resize
    native int resizeD3DCanvas(Canvas3D cv, long ctx);

    int resizeD3DCanvas(Canvas3D cv, Context ctx) {
        return resizeD3DCanvas(cv, unbox(ctx));
    }


    // notify D3D to toggle between FullScreen and window mode
    native int toggleFullScreenMode(Canvas3D cv, long ctx);

    int toggleFullScreenMode(Canvas3D cv, Context ctx) {
        return toggleFullScreenMode(cv, unbox(ctx));
    }


    // native method for setting Material when no material is present
    native void updateMaterialColor(long ctx, float r, float g, float b, float a);

    void updateMaterialColor(Context ctx, float r, float g, float b, float a) {
        updateMaterialColor(unbox(ctx), r, g, b, a);
    }


    native void destroyContext(long display, long drawable, long ctx);

    void destroyContext(long display, Drawable drawable, Context ctx) {
        assert display != 0 || VirtualUniverse.mc.isWindows();
        assert ctx != null;
        assert drawable != null;
        destroyContext(display, unbox(drawable), unbox(ctx));
    }


    // This is the native method for doing accumulation.
    native void accum(long ctx, float value);

    void accum(Context ctx, float value) {
        accum(unbox(ctx), value);
    }


    // This is the native method for doing accumulation return.
    native void accumReturn(long ctx);

    void accumReturn(Context ctx) {
        accumReturn(unbox(ctx));
    }


    // This is the native method for clearing the accumulation buffer.
    native void clearAccum(long ctx);

    void clearAccum(Context ctx) {
        clearAccum(unbox(ctx));
    }


    // This is the native method for getting the number of lights the underlying
    // native library can support.
    native int getNumCtxLights(long ctx);

    int getNumCtxLights(Context ctx) {
        return getNumCtxLights(unbox(ctx));
    }


    // Native method for decal 1st child setup
    native boolean decal1stChildSetup(long ctx);

    boolean decal1stChildSetup(Context ctx) {
        return decal1stChildSetup(unbox(ctx));
    }


    // Native method for decal nth child setup
    native void decalNthChildSetup(long ctx);

    void decalNthChildSetup(Context ctx) {
        decalNthChildSetup(unbox(ctx));
    }


    // Native method for decal reset
    native void decalReset(long ctx, boolean depthBufferEnable);

    void decalReset(Context ctx, boolean depthBufferEnable) {
        decalReset(unbox(ctx), depthBufferEnable);
    }


    // Native method for decal reset
    native void ctxUpdateEyeLightingEnable(long ctx, boolean localEyeLightingEnable);

    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
        ctxUpdateEyeLightingEnable(unbox(ctx), localEyeLightingEnable);
    }


    // The following three methods are used in multi-pass case

    // native method for setting blend color
    native void setBlendColor(long ctx, float red, float green,
            float blue, float alpha);

    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
        setBlendColor(unbox(ctx), red, green,
                blue, alpha);
    }


    // native method for setting blend func
    native void setBlendFunc(long ctx, int src, int dst);

    void setBlendFunc(Context ctx, int src, int dst) {
        setBlendFunc(unbox(ctx), src, dst);
    }


    // native method for setting fog enable flag
    native void setFogEnableFlag(long ctx, boolean enableFlag);

    void setFogEnableFlag(Context ctx, boolean enableFlag) {
        setFogEnableFlag(unbox(ctx), enableFlag);
    }


    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    native void setFullSceneAntialiasing(long ctx, boolean enable);

    void setFullSceneAntialiasing(Context ctx, boolean enable) {
        setFullSceneAntialiasing(unbox(ctx), enable);
    }


    native void setGlobalAlpha(long ctx, float alpha);

    void setGlobalAlpha(Context ctx, float alpha) {
        setGlobalAlpha(unbox(ctx), alpha);
    }


    // Native method to update separate specular color control
    native void updateSeparateSpecularColorEnable(long ctx, boolean control);

    void updateSeparateSpecularColorEnable(Context ctx, boolean control) {
        updateSeparateSpecularColorEnable(unbox(ctx), control);
    }


    // Initialization for D3D when scene begin
    native void beginScene(long ctx);

    void beginScene(Context ctx) {
        beginScene(unbox(ctx));
    }

    native void endScene(long ctx);

    void endScene(Context ctx) {
        endScene(unbox(ctx));
    }


    // True under Solaris,
    // False under windows when display mode <= 8 bit
    native boolean validGraphicsMode();

    // native method for setting light enables
    native void setLightEnables(long ctx, long enableMask, int maxLights);

    void setLightEnables(Context ctx, long enableMask, int maxLights) {
        setLightEnables(unbox(ctx), enableMask, maxLights);
    }


    // native method for setting scene ambient
    native void setSceneAmbient(long ctx, float red, float green, float blue);

    void setSceneAmbient(Context ctx, float red, float green, float blue) {
        setSceneAmbient(unbox(ctx), red, green, blue);
    }


    // native method for disabling fog
    native void disableFog(long ctx);

    void disableFog(Context ctx) {
        disableFog(unbox(ctx));
    }


    // native method for disabling modelClip
    native void disableModelClip(long ctx);

    void disableModelClip(Context ctx) {
        disableModelClip(unbox(ctx));
    }


    // native method for setting default RenderingAttributes
    native void resetRenderingAttributes(long ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride);

    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
        resetRenderingAttributes(unbox(ctx),
                depthBufferWriteEnableOverride,
                depthBufferEnableOverride);
    }


    // native method for setting default texture
    native void resetTextureNative(long ctx, int texUnitIndex);

    void resetTextureNative(Context ctx, int texUnitIndex) {
        resetTextureNative(unbox(ctx), texUnitIndex);
    }


    // native method for activating a particular texture unit
    native void activeTextureUnit(long ctx, int texUnitIndex);

    void activeTextureUnit(Context ctx, int texUnitIndex) {
        activeTextureUnit(unbox(ctx), texUnitIndex);
    }


    // native method for setting default TexCoordGeneration
    native void resetTexCoordGeneration(long ctx);

    void resetTexCoordGeneration(Context ctx) {
        resetTexCoordGeneration(unbox(ctx));
    }


    // native method for setting default TextureAttributes
    native void resetTextureAttributes(long ctx);

    void resetTextureAttributes(Context ctx) {
        resetTextureAttributes(unbox(ctx));
    }


    // native method for setting default PolygonAttributes
    native void resetPolygonAttributes(long ctx);

    void resetPolygonAttributes(Context ctx) {
        resetPolygonAttributes(unbox(ctx));
    }


    // native method for setting default LineAttributes
    native void resetLineAttributes(long ctx);

    void resetLineAttributes(Context ctx) {
        resetLineAttributes(unbox(ctx));
    }


    // native method for setting default PointAttributes
    native void resetPointAttributes(long ctx);

    void resetPointAttributes(Context ctx) {
        resetPointAttributes(unbox(ctx));
    }


    // native method for setting default TransparencyAttributes
    native void resetTransparency(long ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA);

    void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA) {
        resetTransparency(unbox(ctx), geometryType,
                polygonMode, lineAA,
                pointAA);
    }


    // native method for setting default ColoringAttributes
    native void resetColoringAttributes(long ctx,
            float r, float g,
            float b, float a,
            boolean enableLight);

    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
        resetColoringAttributes(unbox(ctx),
                r, g,
                b, a,
                enableLight);
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    native void syncRender(long ctx, boolean wait);

    void syncRender(Context ctx, boolean wait) {
        syncRender(unbox(ctx), wait);
    }


    // The native method that sets this ctx to be the current one
    native boolean useCtx(long ctx, long display, long drawable);

    boolean useCtx(Context ctx, long display, Drawable drawable) {
        assert display != 0 || VirtualUniverse.mc.isWindows();
        return useCtx(unbox(ctx), display, unbox(drawable));
    }

    native void clear(long ctx, float r, float g, float b);    
    
    void clear(Context ctx, float r, float g, float b) {
        clear(unbox(ctx), r, g, b);
        
    }
    
    native void textureFillBackground(long ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY);

    void textureFillBackground(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY)  {
        textureFillBackground(unbox(ctx), texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY);
    }

    native void textureFillRaster(long ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, float mapZ, float alpha);

    void textureFillRaster(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, float mapZ, float alpha) {
        textureFillRaster(unbox(ctx), texMinU, texMaxU, texMinV, texMaxV,
                mapMinX, mapMaxX, mapMinY, mapMaxY, mapZ, alpha);    
    }
    
    // The native method for setting the ModelView matrix.
    native void setModelViewMatrix(long ctx, double[] viewMatrix, double[] modelMatrix);

    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
        setModelViewMatrix(unbox(ctx), viewMatrix, modelMatrix);
    }


    // The native method for setting the Projection matrix.
    native void setProjectionMatrix(long ctx, double[] projMatrix);

    void setProjectionMatrix(Context ctx, double[] projMatrix) {
        setProjectionMatrix(unbox(ctx), projMatrix);
    }


    // The native method for setting the Viewport.
    native void setViewport(long ctx, int x, int y, int width, int height);

    void setViewport(Context ctx, int x, int y, int width, int height) {
        setViewport(unbox(ctx), x, y, width, height);
    }


    // used for display Lists
    native void newDisplayList(long ctx, int displayListId);

    void newDisplayList(Context ctx, int displayListId) {
        newDisplayList(unbox(ctx), displayListId);
    }

    native void endDisplayList(long ctx);

    void endDisplayList(Context ctx) {
        endDisplayList(unbox(ctx));
    }

    native void callDisplayList(long ctx, int id, boolean isNonUniformScale);

    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
        callDisplayList(unbox(ctx), id, isNonUniformScale);
    }


    native void freeDisplayList(long ctx, int id);

    void freeDisplayList(Context ctx, int id) {
        freeDisplayList(unbox(ctx), id);
    }

    native void freeTexture(long ctx, int id);

    void freeTexture(Context ctx, int id) {
        freeTexture(unbox(ctx), id);
    }

    native void texturemapping(long ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight);

    void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight) {
        texturemapping(unbox(ctx),
                px, py,
                xmin, ymin, xmax, ymax,
                texWidth, texHeight,
                rasWidth,
                format, objectId,
                image,
                winWidth, winHeight);
    }


    native boolean initTexturemapping(long ctx, int texWidth,
            int texHeight, int objectId);

    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
        return initTexturemapping(unbox(ctx), texWidth,
                texHeight, objectId);
    }



    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    native void setRenderMode(long ctx, int mode, boolean doubleBuffer);

    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
        setRenderMode(unbox(ctx), mode, doubleBuffer);
    }


    // Set glDepthMask.
    native void setDepthBufferWriteEnable(long ctx, boolean mode);

    void setDepthBufferWriteEnable(Context ctx, boolean mode) {
        setDepthBufferWriteEnable(unbox(ctx), mode);
    }



    // ---------------------------------------------------------------------

    //
    // Canvas3D / GraphicsConfigTemplate3D methods - logic dealing with
    // native graphics configuration or drawing surface
    //

    // Return a graphics config based on the one passed in. Note that we can
    // assert that the input config is non-null and was created from a
    // GraphicsConfigTemplate3D.
    // This method must return a valid GraphicsConfig, or else it must throw
    // an exception if one cannot be returned.
    GraphicsConfiguration getGraphicsConfig(GraphicsConfiguration gconfig) {
//KCR:        System.err.println("NativePipeline.getGraphicsConfig()");

        // Just return the input graphics config
        return gconfig;
    }

    // Get the native FBconfig pointer
    long getFbConfig(GraphicsConfigInfo gcInfo) {
        long fbConfig = ((Long)gcInfo.getPrivateData()).longValue();
        if (fbConfig == 0L) {
            throw new IllegalArgumentException(J3dI18N.getString("Canvas3D23"));
        }

        return fbConfig;
    }

    // Get best graphics config from pipeline
    GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration[] gc) {
        return nativeTemplate.getBestConfiguration(gct, gc);
    }

    // Determine whether specified graphics config is supported by pipeline
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration gc) {
        return nativeTemplate.isGraphicsConfigSupported(gct, gc);
    }

    // Methods to get actual capabilities from Canvas3D
    boolean hasDoubleBuffer(Canvas3D cv) {
        return nativeTemplate.hasDoubleBuffer(cv);
    }

    boolean hasStereo(Canvas3D cv) {
        return nativeTemplate.hasStereo(cv);
    }

    int getStencilSize(Canvas3D cv) {
        return nativeTemplate.getStencilSize(cv);
    }

    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
        return nativeTemplate.hasSceneAntialiasingMultisample(cv);
    }

    boolean hasSceneAntialiasingAccum(Canvas3D cv) {
        return nativeTemplate.hasSceneAntialiasingAccum(cv);
    }

    // Methods to get native WS display and screen
    long getDisplay() {
        return NativeScreenInfo.getDisplay();
    }
    int getScreen(GraphicsDevice graphicsDevice) {
        return NativeScreenInfo.getScreen(graphicsDevice);
    }

    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
        return new DrawingSurfaceObjectAWT(cv,
                VirtualUniverse.mc.awt, cv.screen.display, cv.screen.screen,
                VirtualUniverse.mc.xineramaDisabled);
    }


    // Method to free the drawing surface object
    // (called from Canvas3D.removeNotify)
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
        synchronized (drawingSurfaceObject) {
            DrawingSurfaceObjectAWT dso =
                    (DrawingSurfaceObjectAWT)drawingSurfaceObject;
            // get nativeDS before it is set to 0 in invalidate()
            long ds = dso.getDS();
            long ds_struct[] = {ds, dso.getDSI()};
            if (ds != 0) {
                VirtualUniverse.mc.postRequest(
                        MasterControl.FREE_DRAWING_SURFACE,
                        ds_struct);
            }

            drawingSurfaceObject.invalidate();
        }
    }

    // Method to free the native drawing surface object
    void freeDrawingSurfaceNative(Object o) {
        DrawingSurfaceObjectAWT.freeDrawingSurface(o);
    }

}
