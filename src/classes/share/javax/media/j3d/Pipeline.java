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


    // ---------------------------------------------------------------------

    //
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    abstract void freeD3DSurface(ImageComponent2DRetained image, int hashId);


    // ---------------------------------------------------------------------

    //
    // J3DBuffer methods
    //

    // Method to verify that we can access a direct NIO buffer
    // from native code
    boolean checkNativeBufferAccess(java.nio.Buffer buffer) {
        // Return true by default. Pipeline can override and implement, if
        // we decide that it is necessary.
        return true;
    }


    // ---------------------------------------------------------------------

    //
    // RasterRetained methods
    //

    // Native method that does the rendering
    abstract void executeRaster(long ctx, GeometryRetained geo,
            boolean updateAlpha, float alpha,
            int type, int width, int height,
            int xSrcOffset, int ySrcOffset,
            float x, float y, float z, byte[] image);


    // ---------------------------------------------------------------------

    //
    // Renderer methods
    //

    abstract void cleanupRenderer();


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    abstract void updateColoringAttributes(long ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lEnable,
            int shadeModel);


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    abstract void updateDirectionalLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z);


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    abstract void updatePointLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz);


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    abstract void updateSpotLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz, float spreadAngle,
            float concentration, float dx, float dy,
            float dz);


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    abstract void updateExponentialFog(long ctx,
            float red, float green, float blue,
            float density);


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    abstract void updateLinearFog(long ctx,
            float red, float green, float blue,
            double fdist, double bdist);


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    abstract void updateLineAttributes(long ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing);


    // ---------------------------------------------------------------------

    //
    // MaterialRetained methods
    //

    abstract void updateMaterial(long ctx,
            float red, float green, float blue, float alpha,
            float ared, float agreen, float ablue,
            float ered, float egreen, float eblue,
            float dred, float dgreen, float dblue,
            float sred, float sgreen, float sblue,
            float shininess, int colorTarget, boolean enable);


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    abstract void updateModelClip(long ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D);


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    abstract void updatePointAttributes(long ctx, float pointSize, boolean pointAntialiasing);


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    abstract void updatePolygonAttributes(long ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor);


    // ---------------------------------------------------------------------

    //
    // RenderingAttributesRetained methods
    //

    abstract void updateRenderingAttributes(long ctx,
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


    // ---------------------------------------------------------------------

    //
    // TexCoordGenerationRetained methods
    //

   /**
    * This method updates the native context:
    * trans contains eyeTovworld transform in d3d
    * trans contains vworldToEye transform in ogl
    */
    abstract void updateTexCoordGeneration(long ctx,
            boolean enable, int genMode, int format,
            float planeSx, float planeSy, float planeSz, float planeSw,
            float planeTx, float planeTy, float planeTz, float planeTw,
            float planeRx, float planeRy, float planeRz, float planeRw,
            float planeQx, float planeQy, float planeQz, float planeQw,
            double[] trans);


    // ---------------------------------------------------------------------

    //
    // TransparencyAttributesRetained methods
    //

    abstract void updateTransparencyAttributes(long ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction);


    // ---------------------------------------------------------------------

    //
    // TextureAttributesRetained methods
    //

    abstract void updateTextureAttributes(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat);

    abstract void updateRegisterCombiners(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);

    abstract void updateTextureColorTable(long ctx, int numComponents,
            int colorTableSize,
            int[] colorTable);

    abstract void updateCombiner(long ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    abstract void updateTextureUnitState(long ctx, int unitIndex, boolean enableFlag);


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    abstract void bindTexture2D(long ctx, int objectId, boolean enable);

    abstract void updateTexture2DImage(long ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData);

    abstract void updateTexture2DSubImage(long ctx,
            int level, int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData);

    abstract void updateTexture2DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTexture2DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTexture2DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    abstract void updateDetailTextureParameters(long ctx,
            int detailTextureMode,
            int detailTextureLevel,
            int nPts, float[] pts);

    abstract void updateTexture2DFilterModes(long ctx,
            int minFilter, int magFilter);

    abstract void updateTexture2DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTexture2DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTexture2DAnisotropicFilter(long ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    abstract void bindTexture3D(long ctx, int objectId, boolean enable);

    abstract void updateTexture3DImage(long ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height, int depth,
            int boundaryWidth,
            byte[] imageData);

    abstract void updateTexture3DSubImage(long ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int internalFormat, int storedFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            byte[] imageData);

    abstract void updateTexture3DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTexture3DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTexture3DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha);

    abstract void updateTexture3DFilterModes(long ctx,
            int minFilter, int magFilter);

    abstract void updateTexture3DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTexture3DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTexture3DAnisotropicFilter(long ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    abstract void bindTextureCubeMap(long ctx, int objectId, boolean enable);

    abstract void updateTextureCubeMapImage(long ctx,
            int face, int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData);

    abstract void updateTextureCubeMapSubImage(long ctx,
            int face, int level, int xoffset, int yoffset,
            int internalFormat,int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData);

    abstract void updateTextureCubeMapLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTextureCubeMapLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTextureCubeMapBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    abstract void updateTextureCubeMapFilterModes(long ctx,
            int minFilter, int magFilter);

    abstract void updateTextureCubeMapSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTextureCubeMapFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTextureCubeMapAnisotropicFilter(long ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // DetailTextureImage methods
    //

    abstract void bindDetailTexture(long ctx, int objectId);

    abstract void updateDetailTextureImage(long ctx,
            int numLevels, int level,
            int format, int storedFormat,
            int width, int height,
            int boundaryWidth, byte[] data);


}
