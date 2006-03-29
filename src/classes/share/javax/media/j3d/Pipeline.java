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
            // Should not get here
            throw new AssertionError("missing case statement");
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


    // ---------------------------------------------------------------------

    //
    // GeometryArrayRetained methods
    //

    // Used by D3D to free vertex buffer
    abstract void freeD3DArray(GeometryArrayRetained geo, boolean deleteVB);

    // used for GeometryArrays by Copy or interleaved
    abstract void execute(long ctx,
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
    abstract void executeVA(long ctx,
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
    abstract void executeVABuffer(long ctx,
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
    abstract void executeInterleavedBuffer(long ctx,
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

    abstract void setVertexFormat(long ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors);

    abstract void disableGlobalAlpha(long ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors);

    // used for GeometryArrays
    abstract void buildGA(long ctx,
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
    abstract void buildGAForByRef(long ctx,
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

    // used to Build Dlist GeometryArray by Reference with NIO buffer
    // NOTE: NIO buffers are no longer supported in display lists. We
    // have no plans to add this support.
    /*
    abstract void buildGAForBuffer(long ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,  boolean updateAlpha,
            float alpha,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int coordIndex, Object vcoords,
            int colorIndex, Object cdata,
            int normalIndex, Object ndata,
            int texcoordmaplength,
            int[] texcoordoffset,
            int[] texIndex, int texstride, Object[] texCoords,
            double[] xform, double[] nxform);
    */


    // ---------------------------------------------------------------------

    //
    // IndexedGeometryArrayRetained methods
    //

    // by-copy or interleaved, by reference, Java arrays
    abstract void executeIndexedGeometry(long ctx,
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
    abstract void executeIndexedGeometryBuffer(long ctx,
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
    abstract void executeIndexedGeometryVA(long ctx,
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
    abstract void executeIndexedGeometryVABuffer(long ctx,
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
    abstract void buildIndexedGeometry(long ctx,
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
    abstract void readRasterNative(long ctx,
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

    abstract ShaderError setCgUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    abstract ShaderError setCgUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    abstract ShaderError setCgUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setCgUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setCgUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setCgUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setCgUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    abstract ShaderError setCgUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    // abstract interfaces for shader compilation, etc.
    abstract ShaderError createCgShader(long ctx, int shaderType, long[] shaderId);
    abstract ShaderError destroyCgShader(long ctx, long shaderId);
    abstract ShaderError compileCgShader(long ctx, long shaderId, String program);

    abstract ShaderError createCgShaderProgram(long ctx, long[] shaderProgramId);
    abstract ShaderError destroyCgShaderProgram(long ctx, long shaderProgramId);
    abstract ShaderError linkCgShaderProgram(long ctx, long shaderProgramId,
            long[] shaderId);
    abstract void lookupCgVertexAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr);
    abstract void lookupCgShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    abstract ShaderError useCgShaderProgram(long ctx, long shaderProgramId);


    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    abstract ShaderError setGLSLUniform1i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int value);

    abstract ShaderError setGLSLUniform1f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float value);

    abstract ShaderError setGLSLUniform2i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform2f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniform3i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniform4i(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix3f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix4f(long ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    abstract ShaderError setGLSLUniform1iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform1fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform2iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform2fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform3iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform4iArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix3fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix4fArray(long ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value);

    // abstract interfaces for shader compilation, etc.
    abstract ShaderError createGLSLShader(long ctx, int shaderType, long[] shaderId);
    abstract ShaderError destroyGLSLShader(long ctx, long shaderId);
    abstract ShaderError compileGLSLShader(long ctx, long shaderId, String program);

    abstract ShaderError createGLSLShaderProgram(long ctx, long[] shaderProgramId);
    abstract ShaderError destroyGLSLShaderProgram(long ctx, long shaderProgramId);
    abstract ShaderError linkGLSLShaderProgram(long ctx, long shaderProgramId,
            long[] shaderId);
    abstract ShaderError bindGLSLVertexAttrName(long ctx, long shaderProgramId,
            String attrName, int attrIndex);
    abstract void lookupGLSLShaderAttrNames(long ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    abstract ShaderError useGLSLShaderProgram(long ctx, long shaderProgramId);

}
