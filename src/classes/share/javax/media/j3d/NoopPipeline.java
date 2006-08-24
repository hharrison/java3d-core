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
import java.awt.GraphicsEnvironment;

/**
 * Concrete implementation of Pipeline class for the noop rendering
 * pipeline.
 */
class NoopPipeline extends Pipeline {

    // Flags indicating whether the Cg or GLSL libraries are available.
    private boolean cgLibraryAvailable = false;

    /**
     * Constructor for singleton NoopPipeline instance
     */
    protected NoopPipeline() {
    }

    /**
     * Initialize the pipeline
     */
    void initialize(Pipeline.Type rendererType) {
        super.initialize(rendererType);

        assert rendererType == Pipeline.Type.NOOP;
    }

    /**
     * Load all of the required libraries
     */
    void loadLibraries(int globalShadingLanguage) {
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
        return true;
    }


    // ---------------------------------------------------------------------

    //
    // GeometryArrayRetained methods
    //

    // Used by D3D to free vertex buffer
    void freeD3DArray(GeometryArrayRetained geo, boolean deleteVB) {
    }

    // used for GeometryArrays by Copy or interleaved
    void execute(Context ctx,
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
            float[] varray, float[] cdata, int pass, int cdirty) {
    }

    // used by GeometryArray by Reference with java arrays
    void executeVA(Context ctx,
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
            int cdirty) {
    }

    // used by GeometryArray by Reference with NIO buffer
    void executeVABuffer(Context ctx,
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
            int cdirty) {
    }

    // used by GeometryArray by Reference in interleaved format with NIO buffer
    void executeInterleavedBuffer(Context ctx,
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
            Object varray, float[] cdata, int pass, int cdirty) {
    }

    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
    }

    void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors) {
    }

    // used for GeometryArrays
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
    }

    // used to Build Dlist GeometryArray by Reference with java arrays
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
    }


    // ---------------------------------------------------------------------

    //
    // IndexedGeometryArrayRetained methods
    //

    // by-copy or interleaved, by reference, Java arrays
    void executeIndexedGeometry(Context ctx,
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
            int[] indexCoord) {
    }

    // interleaved, by reference, nio buffer
    void executeIndexedGeometryBuffer(Context ctx,
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
            int[] indexCoord) {
    }

    // non interleaved, by reference, Java arrays
    void executeIndexedGeometryVA(Context ctx,
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
            int[] indexCoord) {
    }

    // non interleaved, by reference, nio buffer
    void executeIndexedGeometryVABuffer(Context ctx,
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
            int[] indexCoord) {
    }

    // by-copy geometry
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
    }


    // ---------------------------------------------------------------------

    //
    // GraphicsContext3D methods
    //

    // Native method for readRaster
    void readRasterNative(Context ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas, int format,
            ImageComponentRetained image,
            DepthComponentRetained depth,
            GraphicsContext3D gc) {
    }


    // ---------------------------------------------------------------------

    //
    // CgShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    ShaderError setCgUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {
        return null;
    }

    ShaderError setCgUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        return null;
    }

    ShaderError setCgUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setCgUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setCgUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setCgUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setCgUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    // ShaderAttributeArray methods

    ShaderError setCgUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setCgUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setCgUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setCgUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setCgUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setCgUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setCgUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createCgShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        return null;
    }
    ShaderError destroyCgShader(Context ctx, ShaderId shaderId) {
        return null;
    }
    ShaderError compileCgShader(Context ctx, ShaderId shaderId, String program) {
        return null;
    }

    ShaderError createCgShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        return null;
    }
    ShaderError destroyCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }
    ShaderError linkCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
        return null;
    }
    void lookupCgVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr) {
    }
    void lookupCgShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
    }

    ShaderError useCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }


    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    ShaderError setGLSLUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {
        return null;
    }

    ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        return null;
    }

    ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    // ShaderAttributeArray methods

    ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        return null;
    }
    ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId) {
        return null;
    }
    ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program) {
        return null;
    }

    ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        return null;
    }
    ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }
    ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
        return null;
    }
    ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex) {
        return null;
    }
    void lookupGLSLShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
    }

    ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }


    // ---------------------------------------------------------------------

    //
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    void freeD3DSurface(ImageComponent2DRetained image, int hashId) {
    }



    // ---------------------------------------------------------------------

    //
    // Renderer methods
    //

    void cleanupRenderer() {
    }


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    void updateColoringAttributes(Context ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lEnable,
            int shadeModel) {
    }


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z) {
    }


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz) {
    }


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    void updateSpotLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz, float spreadAngle,
            float concentration, float dx, float dy,
            float dz) {
    }


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density) {
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
    }


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    void updateLineAttributes(Context ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing) {
    }


    // ---------------------------------------------------------------------

    //
    // MaterialRetained methods
    //

    void updateMaterial(Context ctx,
            float red, float green, float blue, float alpha,
            float ared, float agreen, float ablue,
            float ered, float egreen, float eblue,
            float dred, float dgreen, float dblue,
            float sred, float sgreen, float sblue,
            float shininess, int colorTarget, boolean enable) {
    }


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D) {
    }


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
    }


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    void updatePolygonAttributes(Context ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor) {
    }


    // ---------------------------------------------------------------------

    //
    // RenderingAttributesRetained methods
    //

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
    void updateTexCoordGeneration(Context ctx,
            boolean enable, int genMode, int format,
            float planeSx, float planeSy, float planeSz, float planeSw,
            float planeTx, float planeTy, float planeTz, float planeTw,
            float planeRx, float planeRy, float planeRz, float planeRw,
            float planeQx, float planeQy, float planeQz, float planeQw,
            double[] trans) {
    }


    // ---------------------------------------------------------------------

    //
    // TransparencyAttributesRetained methods
    //

    void updateTransparencyAttributes(Context ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureAttributesRetained methods
    //

    void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat) {
    }

    void updateRegisterCombiners(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
    }

    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] colorTable) {
    }

    void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    void updateTextureUnitState(Context ctx, int unitIndex, boolean enableFlag) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    void bindTexture2D(Context ctx, int objectId, boolean enable) {
    }

    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object data) {
    }

    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object data) {
    }
    
    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
    }

    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    void updateTexture2DAnisotropicFilter(Context ctx, float degree) {
    }


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    void bindTexture3D(Context ctx, int objectId, boolean enable) {
    }

    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int imageDataType, Object imageData) {
    }

    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int imageTypeData, Object imageData) {
    }

    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
    }

    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    void bindTextureCubeMap(Context ctx, int objectId, boolean enable) {
    }

    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object imageData) {
    }

    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData) {
    }

    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
    }

    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
    }

    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Method to return the AWT object
    long getAWT() {
        return 0L;
    }

    // Method to initialize the native J3D library
    boolean initializeJ3D(boolean disableXinerama) {
        return true;
    }

    // Maximum lights supported by the native API
    int getMaximumLights() {
        return 8;
    }


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods - native wrappers
    //

    // This is the native method for creating the underlying graphics context.
    Context createNewContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, Context shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {
        return new NoopContext();
    }

    void createQueryContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {
    }

    // This is the native for creating offscreen buffer
    Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, int width, int height) {
        return null;
    }

    void destroyOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, Drawable drawable) {
    }

    // This is the native for reading the image from the offscreen buffer
    void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int type, Object data, int width, int height) {
    }
    
    // The native method for swapBuffers
    int swapBuffers(Canvas3D cv, Context ctx, long dpy, Drawable drawable) {
        return 0;
    }

    // notify D3D that Canvas is resize
    int resizeD3DCanvas(Canvas3D cv, Context ctx) {
        return 0;
    }

    // notify D3D to toggle between FullScreen and window mode
    int toggleFullScreenMode(Canvas3D cv, Context ctx) {
        return 0;
    }

    // native method for setting Material when no material is present
    void updateMaterialColor(Context ctx, float r, float g, float b, float a) {
    }

    void destroyContext(long display, Drawable drawable, Context ctx) {
    }

    // This is the native method for doing accumulation.
    void accum(Context ctx, float value) {
    }

    // This is the native method for doing accumulation return.
    void accumReturn(Context ctx) {
    }

    // This is the native method for clearing the accumulation buffer.
    void clearAccum(Context ctx) {
    }

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    int getNumCtxLights(Context ctx) {
        return 0;
    }

    // Native method for decal 1st child setup
    boolean decal1stChildSetup(Context ctx) {
        return false;
    }

    // Native method for decal nth child setup
    void decalNthChildSetup(Context ctx) {
    }

    // Native method for decal reset
    void decalReset(Context ctx, boolean depthBufferEnable) {
    }

    // Native method for decal reset
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
    }

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
    }

    // native method for setting blend func
    void setBlendFunc(Context ctx, int src, int dst) {
    }

    // native method for setting fog enable flag
    void setFogEnableFlag(Context ctx, boolean enableFlag) {
    }

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    void setFullSceneAntialiasing(Context ctx, boolean enable) {
    }

    void setGlobalAlpha(Context ctx, float alpha) {
    }

    // Native method to update separate specular color control
    void updateSeparateSpecularColorEnable(Context ctx, boolean control) {
    }

    // Initialization for D3D when scene begin
    void beginScene(Context ctx) {
    }
    void endScene(Context ctx) {
    }

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    boolean validGraphicsMode() {
        return true;
    }

    // native method for setting light enables
    void setLightEnables(Context ctx, long enableMask, int maxLights) {
    }

    // native method for setting scene ambient
    void setSceneAmbient(Context ctx, float red, float green, float blue) {
    }

    // native method for disabling fog
    void disableFog(Context ctx) {
    }

    // native method for disabling modelClip
    void disableModelClip(Context ctx) {
    }

    // native method for setting default RenderingAttributes
    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
    }

    // native method for setting default texture
    void resetTextureNative(Context ctx, int texUnitIndex) {
    }

    // native method for activating a particular texture unit
    void activeTextureUnit(Context ctx, int texUnitIndex) {
    }

    // native method for setting default TexCoordGeneration
    void resetTexCoordGeneration(Context ctx) {
    }

    // native method for setting default TextureAttributes
    void resetTextureAttributes(Context ctx) {
    }

    // native method for setting default PolygonAttributes
    void resetPolygonAttributes(Context ctx) {
    }

    // native method for setting default LineAttributes
    void resetLineAttributes(Context ctx) {
    }

    // native method for setting default PointAttributes
    void resetPointAttributes(Context ctx) {
    }

    // native method for setting default TransparencyAttributes
    void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA) {
    }

    // native method for setting default ColoringAttributes
    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
    }

    // native method for updating the texture unit state map
    void updateTexUnitStateMap(Context ctx, int numActiveTexUnit,
            int[] texUnitStateMap) {
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    void syncRender(Context ctx, boolean wait) {
    }

    // The native method that sets this ctx to be the current one
    boolean useCtx(Context ctx, long display, Drawable drawable) {
        return true;
    }

    void textureclear(Context ctx, int maxX, int maxY,
            float r, float g, float b,
            int winWidth, int winHeight,
            int objectId, int scalemode,
            ImageComponent2DRetained image,
            boolean update) {
    }


    // The native method for setting the ModelView matrix.
    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
    }

    // The native method for setting the Projection matrix.
    void setProjectionMatrix(Context ctx, double[] projMatrix) {
    }

    // The native method for setting the Viewport.
    void setViewport(Context ctx, int x, int y, int width, int height) {
    }

    // used for display Lists
    void newDisplayList(Context ctx, int displayListId) {
    }
    void endDisplayList(Context ctx) {
    }
    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
    }

    void freeDisplayList(Context ctx, int id) {
    }
    void freeTexture(Context ctx, int id) {
    }

    void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight) {
    }

    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
        return true;
    }


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
    }

    // Set glDepthMask.
    void setDepthBufferWriteEnable(Context ctx, boolean mode) {
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
        System.err.println("NoopPipeline.getGraphicsConfig()");
        return gconfig;
    }

    // Get the native FBconfig pointer
    long getFbConfig(GraphicsConfigInfo gcInfo) {
        return 0L;
    }


    // Get best graphics config from pipeline
    GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration[] gc) {

        GraphicsConfiguration gc1 = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();
        // We need to cache the GraphicsTemplate3D
	synchronized (Canvas3D.graphicsConfigTable) {
	    if (Canvas3D.graphicsConfigTable.get(gc1) == null) {
                GraphicsConfigInfo gcInfo = new GraphicsConfigInfo(gct);
//                gcInfo.setPrivateData(privateData);
		Canvas3D.graphicsConfigTable.put(gc1, gcInfo);
            }
	}   
        return gc1;
    }

    // Determine whether specified graphics config is supported by pipeline
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration gc) {
        return true;
    }

    // Methods to get actual capabilities from Canvas3D
    boolean hasDoubleBuffer(Canvas3D cv) {
        return true;
    }

    boolean hasStereo(Canvas3D cv) {
        return false;
    }

    int getStencilSize(Canvas3D cv) {
        return 0;
    }

    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
        return false;
    }

    boolean hasSceneAntialiasingAccum(Canvas3D cv) {
        return false;
    }

    // Methods to get native WS display and screen
    long getDisplay() {
        return 0L;
    }

    int getScreen(GraphicsDevice graphicsDevice) {
        return 0;
    }


    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
        return new NoopDrawingSurfaceObject(cv);
    }

    // Method to free the drawing surface object
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
        // This method is a no-op
    }

    // Method to free the native drawing surface object
    void freeDrawingSurfaceNative(Object o) {
        // This method is a no-op
    }

    /**
     * Dummy context for noop pipeline
     */
    static class NoopContext implements Context {
    }

}
