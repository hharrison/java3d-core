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

/**
 * Abstract pipeline class for rendering pipeline methods. All rendering
 * pipeline methods will eventually go here.
 */
abstract class Pipeline {
    // Singleton pipeline instance
    private static Pipeline pipeline;

    // Supported Rendering APIs
    enum Type {
        // Native rendering pipelines using OGL or D3D library
        NATIVE_OGL,
        NATIVE_D3D,
        
        // Java rendering pipeline using Java Bindings for OpenGL
        JOGL,

        // No-op rendering pipeline
        NOOP,
    }

    // Type of renderer (as defined above)
    private Type rendererType = null;

    protected Pipeline() {
    }

    /**
     * Initialize the Pipeline. Called exactly once by
     * MasterControl.loadLibraries() to create the singleton
     * Pipeline object.
     */
    static void createPipeline(Type rendererType) {
        String className = null;
        switch (rendererType) {
        case NATIVE_OGL:
        case NATIVE_D3D:
            className = "javax.media.j3d.NativePipeline";
            break;
        case JOGL:
            className = "javax.media.j3d.JoglPipeline";
            break;
        case NOOP:
            className = "javax.media.j3d.NoopPipeline";
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
    void initialize(Type rendererType) {
        setRendererType(rendererType);
    }

    /**
     * Sets the renderer type. Only called by initialize.
     */
    private void setRendererType(Type rendererType) {
        this.rendererType = rendererType;
    }

    /**
     * Returns the renderer type
     */
    Type getRendererType() {
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
    abstract void execute(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] cdata, int pass, int cdirty);

    // used by GeometryArray by Reference with java arrays
    abstract void executeVA(Context ctx,
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
    abstract void executeVABuffer(Context ctx,
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
    abstract void executeInterleavedBuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            Object varray, float[] cdata, int pass, int cdirty);

    abstract void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors);

    abstract void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors);

    // used for GeometryArrays
    abstract void buildGA(Context ctx,
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

    // used to Build Dlist GeometryArray by Reference with java arrays
    abstract void buildGAForByRef(Context ctx,
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
    abstract void buildGAForBuffer(Context ctx,
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
    abstract void executeIndexedGeometry(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int vertexAttrCount, int[] vertexAttrSizes,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            float[] varray, float[] cdata,
            int pass, int cdirty,
            int[] indexCoord);

    // interleaved, by reference, nio buffer
    abstract void executeIndexedGeometryBuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int indexCount,
            int vertexCount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            Object varray, float[] cdata,
            int pass, int cdirty,
            int[] indexCoord);

    // non interleaved, by reference, Java arrays
    abstract void executeIndexedGeometryVA(Context ctx,
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
    abstract void executeIndexedGeometryVABuffer(Context ctx,
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
    abstract void buildIndexedGeometry(Context ctx,
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


    // ---------------------------------------------------------------------

    //
    // GraphicsContext3D methods
    //

    // Native method for readRaster
    abstract void readRasterNative(Context ctx,
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

    abstract ShaderError setCgUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value);

    abstract ShaderError setCgUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value);

    abstract ShaderError setCgUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setCgUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setCgUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setCgUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setCgUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setCgUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    abstract ShaderError setCgUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setCgUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setCgUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    // abstract interfaces for shader compilation, etc.
    abstract ShaderError createCgShader(Context ctx, int shaderType, ShaderId[] shaderId);
    abstract ShaderError destroyCgShader(Context ctx, ShaderId shaderId);
    abstract ShaderError compileCgShader(Context ctx, ShaderId shaderId, String program);

    abstract ShaderError createCgShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId);
    abstract ShaderError destroyCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId);
    abstract ShaderError linkCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds);
    abstract void lookupCgVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr);
    abstract void lookupCgShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    abstract ShaderError useCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId);


    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    abstract ShaderError setGLSLUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value);

    abstract ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value);

    abstract ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value);

    abstract ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value);

    // ShaderAttributeArray methods

    abstract ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value);

    abstract ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    abstract ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value);

    // abstract interfaces for shader compilation, etc.
    abstract ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId);
    abstract ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId);
    abstract ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program);

    abstract ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId);
    abstract ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId);
    abstract ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds);
    abstract ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex);
    abstract void lookupGLSLShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr);

    abstract ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId);


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
    // Renderer methods
    //

    abstract void cleanupRenderer();


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    abstract void updateColoringAttributes(Context ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lEnable,
            int shadeModel);


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    abstract void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z);


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    abstract void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz);


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    abstract void updateSpotLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz, float spreadAngle,
            float concentration, float dx, float dy,
            float dz);


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    abstract void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density);


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    abstract void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist);


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    abstract void updateLineAttributes(Context ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing);


    // ---------------------------------------------------------------------

    //
    // MaterialRetained methods
    //

    abstract void updateMaterial(Context ctx,
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

    abstract void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D);


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    abstract void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing);


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    abstract void updatePolygonAttributes(Context ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor);


    // ---------------------------------------------------------------------

    //
    // RenderingAttributesRetained methods
    //

    abstract void updateRenderingAttributes(Context ctx,
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
    abstract void updateTexCoordGeneration(Context ctx,
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

    abstract void updateTransparencyAttributes(Context ctx,
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

    abstract void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat);

    abstract void updateRegisterCombiners(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);

    abstract void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] colorTable);

    abstract void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    abstract void updateTextureUnitState(Context ctx, int unitIndex, boolean enableFlag);


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    abstract void bindTexture2D(Context ctx, int objectId, boolean enable);

    abstract void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object data);

    abstract void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object data);
    
    abstract void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTexture2DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    abstract void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter);

    abstract void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTexture2DAnisotropicFilter(Context ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    abstract void bindTexture3D(Context ctx, int objectId, boolean enable);

    abstract void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int imageDataType, Object imageData);

    abstract void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int imageDataType, Object imageData);

    abstract void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTexture3DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha);

    abstract void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter);

    abstract void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTexture3DAnisotropicFilter(Context ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    abstract void bindTextureCubeMap(Context ctx, int objectId, boolean enable);

    abstract void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object imageData);
    
    abstract void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData);
    
    abstract void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    abstract void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    abstract void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    abstract void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter);

    abstract void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    abstract void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    abstract void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree);

    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Method to return the AWT object
    abstract long getAWT();

    // Method to initialize the native J3D library
    abstract boolean initializeJ3D(boolean disableXinerama);

    // Maximum lights supported by the native API 
    abstract int getMaximumLights();


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods - native wrappers
    //

    // This is the native method for creating the underlying graphics context.
    abstract Context createNewContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, Context shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    abstract void createQueryContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    // This is the native for creating offscreen buffer
    abstract Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, int width, int height);

    abstract void destroyOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, Drawable drawable);

    // This is the native for reading the image from the offscreen buffer
    abstract void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int type, Object data, int width, int height);

    // The native method for swapBuffers
    abstract int swapBuffers(Canvas3D cv, Context ctx, long dpy, Drawable drawable);

    // notify D3D that Canvas is resize
    abstract int resizeD3DCanvas(Canvas3D cv, Context ctx);

    // notify D3D to toggle between FullScreen and window mode
    abstract int toggleFullScreenMode(Canvas3D cv, Context ctx);

    // native method for setting Material when no material is present
    abstract void updateMaterialColor(Context ctx, float r, float g, float b, float a);

    abstract void destroyContext(long display, Drawable drawable, Context ctx);

    // This is the native method for doing accumulation.
    abstract void accum(Context ctx, float value);

    // This is the native method for doing accumulation return.
    abstract void accumReturn(Context ctx);

    // This is the native method for clearing the accumulation buffer.
    abstract void clearAccum(Context ctx);

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    abstract int getNumCtxLights(Context ctx);

    // Native method for decal 1st child setup
    abstract boolean decal1stChildSetup(Context ctx);

    // Native method for decal nth child setup
    abstract void decalNthChildSetup(Context ctx);

    // Native method for decal reset
    abstract void decalReset(Context ctx, boolean depthBufferEnable);

    // Native method for decal reset
    abstract void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable);

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    abstract void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha);

    // native method for setting blend func
    abstract void setBlendFunc(Context ctx, int src, int dst);

    // native method for setting fog enable flag
    abstract void setFogEnableFlag(Context ctx, boolean enableFlag);

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    abstract void setFullSceneAntialiasing(Context ctx, boolean enable);

    abstract void setGlobalAlpha(Context ctx, float alpha);

    // Native method to update separate specular color control
    abstract void updateSeparateSpecularColorEnable(Context ctx, boolean control);

    // Initialization for D3D when scene begin
    abstract void beginScene(Context ctx);
    abstract void endScene(Context ctx);

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    abstract boolean validGraphicsMode();

    // native method for setting light enables
    abstract void setLightEnables(Context ctx, long enableMask, int maxLights);

    // native method for setting scene ambient
    abstract void setSceneAmbient(Context ctx, float red, float green, float blue);

    // native method for disabling fog
    abstract void disableFog(Context ctx);

    // native method for disabling modelClip
    abstract void disableModelClip(Context ctx);

    // native method for setting default RenderingAttributes
    abstract void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride);

    // native method for setting default texture
    abstract void resetTextureNative(Context ctx, int texUnitIndex);

    // native method for activating a particular texture unit
    abstract void activeTextureUnit(Context ctx, int texUnitIndex);

    // native method for setting default TexCoordGeneration
    abstract void resetTexCoordGeneration(Context ctx);

    // native method for setting default TextureAttributes
    abstract void resetTextureAttributes(Context ctx);

    // native method for setting default PolygonAttributes
    abstract void resetPolygonAttributes(Context ctx);

    // native method for setting default LineAttributes
    abstract void resetLineAttributes(Context ctx);

    // native method for setting default PointAttributes
    abstract void resetPointAttributes(Context ctx);

    // native method for setting default TransparencyAttributes
    abstract void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA);

    // native method for setting default ColoringAttributes
    abstract void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight);

    // native method for updating the texture unit state map
    abstract void updateTexUnitStateMap(Context ctx, int numActiveTexUnit,
            int[] texUnitStateMap);

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    abstract void syncRender(Context ctx, boolean wait);

    // The native method that sets this ctx to be the current one
    abstract boolean useCtx(Context ctx, long display, Drawable drawable);

    // Optionally release the context. A pipeline may override this and
    // returns true if the context was released.
    boolean releaseCtx(Context ctx, long dpy) {
        return false;
    }

    abstract void textureclear(Context ctx, int maxX, int maxY,
            float r, float g, float b,
            int winWidth, int winHeight,
            int objectId, int scalemode,
            ImageComponent2DRetained image,
            boolean update);


    // The native method for setting the ModelView matrix.
    abstract void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix);

    // The native method for setting the Projection matrix.
    abstract void setProjectionMatrix(Context ctx, double[] projMatrix);

    // The native method for setting the Viewport.
    abstract void setViewport(Context ctx, int x, int y, int width, int height);

    // used for display Lists
    abstract void newDisplayList(Context ctx, int displayListId);
    abstract void endDisplayList(Context ctx);
    abstract void callDisplayList(Context ctx, int id, boolean isNonUniformScale);

    abstract void freeDisplayList(Context ctx, int id);
    abstract void freeTexture(Context ctx, int id);

    abstract void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight);

    abstract boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId);


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    abstract void setRenderMode(Context ctx, int mode, boolean doubleBuffer);

    // Set glDepthMask.
    abstract void setDepthBufferWriteEnable(Context ctx, boolean mode);


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
    abstract GraphicsConfiguration getGraphicsConfig(GraphicsConfiguration gconfig);

    // Get the native FBconfig pointer
    abstract long getFbConfig(GraphicsConfigInfo gcInfo);

    // Get best graphics config from pipeline
    abstract GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration[] gc);

    // Determine whether specified graphics config is supported by pipeline
    abstract boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration gc);

    // Methods to get actual capabilities from Canvas3D
    abstract boolean hasDoubleBuffer(Canvas3D cv);
    abstract boolean hasStereo(Canvas3D cv);
    abstract int getStencilSize(Canvas3D cv);
    abstract boolean hasSceneAntialiasingMultisample(Canvas3D cv);
    abstract boolean hasSceneAntialiasingAccum(Canvas3D cv);
    
    // Methods to get native WS display and screen
    abstract long getDisplay();
    abstract int getScreen(GraphicsDevice graphicsDevice);


    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    abstract DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv);

    // Method to free the drawing surface object
    abstract void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject);

    // Method to free the native drawing surface object
    abstract void freeDrawingSurfaceNative(Object o);

}
