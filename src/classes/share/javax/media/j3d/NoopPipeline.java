/*
 * Copyright 2006-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package javax.media.j3d;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 * Concrete implementation of Pipeline class for the noop rendering
 * pipeline.
 */
class NoopPipeline extends Pipeline {
    /**
     * Constructor for singleton NoopPipeline instance
     */
    protected NoopPipeline() {
    }

    /**
     * Initialize the pipeline
     */
    @Override
    void initialize(Pipeline.Type pipelineType) {
        super.initialize(pipelineType);

        assert pipelineType == Pipeline.Type.NOOP;
    }

    // ---------------------------------------------------------------------

    //
    // GeometryArrayRetained methods
    //

    // used for GeometryArrays by Copy or interleaved
    @Override
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
    }

    // used by GeometryArray by Reference with java arrays
    @Override
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
    }

    // used by GeometryArray by Reference with NIO buffer
    @Override
    void executeVABuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int coordIndex,
            Buffer vcoords,
            int colorIndex,
            Buffer cdataBuffer,
            float[] cfdata, byte[] cbdata,
            int normalIndex, FloatBuffer ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndex, FloatBuffer[] vertexAttrData,
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty) {
    }

    // used by GeometryArray by Reference in interleaved format with NIO buffer
    @Override
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
            FloatBuffer varray, float[] cdata, int cdirty) {
    }

    @Override
    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
    }

    // used for GeometryArrays
    @Override
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
    @Override
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
    @Override
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
    }

    // interleaved, by reference, nio buffer
    @Override
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
            FloatBuffer varray, float[] cdata,
            int cdirty,
            int[] indexCoord) {
    }

    // non interleaved, by reference, Java arrays
    @Override
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
    }

    // non interleaved, by reference, nio buffer
    @Override
    void executeIndexedGeometryVABuffer(Context ctx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount,
            int vformat,
            int vdefined,
            Buffer vcoords,
            Buffer cdataBuffer,
            float[] cfdata, byte[] cbdata,
            FloatBuffer normal,
            int vertexAttrCount, int[] vertexAttrSizes,
            FloatBuffer[] vertexAttrData,
            int texcoordmaplength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texstride, Object[] texCoords,
            int cdirty,
            int[] indexCoord) {
    }

    // by-copy geometry
    @Override
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

    @Override
    void readRaster(Context ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas,
            int imageDataType,             int imageFormat,
            Object imageBuffer,
            int depthFormat,
            Object depthBuffer) {

    }

    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    @Override
    ShaderError setGLSLUniform1i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        return null;
    }

    // ShaderAttributeArray methods

    @Override
    ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        return null;
    }

    // interfaces for shader compilation, etc.
    @Override
    ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        return null;
    }
    @Override
    ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId) {
        return null;
    }
    @Override
    ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program) {
        return null;
    }

    @Override
    ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        return null;
    }
    @Override
    ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }
    @Override
    ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
        return null;
    }
    @Override
    ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex) {
        return null;
    }
    @Override
    void lookupGLSLShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
    }

    @Override
    ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        return null;
    }


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    @Override
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

    @Override
    void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z) {
    }


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    @Override
    void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz) {
    }


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    @Override
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

    @Override
    void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density) {
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    @Override
    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
    }


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    @Override
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

    @Override
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

    @Override
    void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D) {
    }


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    @Override
    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
    }


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    @Override
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

    @Override
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
    @Override
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

    @Override
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

    @Override
    void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat) {
    }

    @Override
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

    @Override
    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] colorTable) {
    }

    @Override
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

    @Override
    void updateTextureUnitState(Context ctx, int unitIndex, boolean enableFlag) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    @Override
    void bindTexture2D(Context ctx, int objectId, boolean enable) {
    }

    @Override
    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object data, boolean useAutoMipMap) {
    }

    @Override
    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object data, boolean useAutoMipMap) {
    }

    @Override
    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    @Override
    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    @Override
    void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
    }

    @Override
    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    @Override
    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    @Override
    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    @Override
    void updateTexture2DAnisotropicFilter(Context ctx, float degree) {
    }


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    @Override
    void bindTexture3D(Context ctx, int objectId, boolean enable) {
    }

    @Override
    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int imageDataType, Object imageData, boolean useAutoMipMap) {
    }

    @Override
    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int imageTypeData, Object imageData, boolean useAutoMipMap) {
    }

    @Override
    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    @Override
    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    @Override
    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
    }

    @Override
    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    @Override
    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    @Override
    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    @Override
    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    @Override
    void bindTextureCubeMap(Context ctx, int objectId, boolean enable) {
    }

    @Override
    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int imageDataType, Object imageData, boolean useAutoMipMap) {
    }

    @Override
    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int imageDataType, Object imageData, boolean useAutoMipMap) {
    }

    @Override
    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
    }

    @Override
    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
    }

    @Override
    void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
    }

    @Override
    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
    }

    @Override
    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
    }

    @Override
    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
    }

    @Override
    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
    }

    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Maximum lights supported by the native API
    @Override
    int getMaximumLights() {
        return 8;
    }


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods - native wrappers
    //

    // This is the native method for creating the underlying graphics context.
    @Override
    Context createNewContext(Canvas3D cv, Drawable drawable,
            Context shareCtx, boolean isSharedCtx,
            boolean offScreen) {
        return new NoopContext();
    }

    @Override
    void createQueryContext(Canvas3D cv, Drawable drawable,
            boolean offScreen, int width, int height) {
    }

    // This is the native for creating offscreen buffer
    @Override
    Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, int width, int height) {
        return null;
    }

    @Override
    void destroyOffScreenBuffer(Canvas3D cv, Context ctx, Drawable drawable) {
    }

    // This is the native for reading the image from the offscreen buffer
    @Override
    void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int type, Object data, int width, int height) {
    }

// The native method for swapBuffers
@Override
void swapBuffers(Canvas3D cv, Context ctx, Drawable drawable) {}

    // native method for setting Material when no material is present
    @Override
    void updateMaterialColor(Context ctx, float r, float g, float b, float a) {
    }

    @Override
    void destroyContext(Drawable drawable, Context ctx) {
    }

    // This is the native method for doing accumulation.
    @Override
    void accum(Context ctx, float value) {
    }

    // This is the native method for doing accumulation return.
    @Override
    void accumReturn(Context ctx) {
    }

    // This is the native method for clearing the accumulation buffer.
    @Override
    void clearAccum(Context ctx) {
    }

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    @Override
    int getNumCtxLights(Context ctx) {
        return 0;
    }

    // Native method for decal 1st child setup
    @Override
    boolean decal1stChildSetup(Context ctx) {
        return false;
    }

    // Native method for decal nth child setup
    @Override
    void decalNthChildSetup(Context ctx) {
    }

    // Native method for decal reset
    @Override
    void decalReset(Context ctx, boolean depthBufferEnable) {
    }

    // Native method for decal reset
    @Override
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
    }

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    @Override
    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
    }

    // native method for setting blend func
    @Override
    void setBlendFunc(Context ctx, int src, int dst) {
    }

    // native method for setting fog enable flag
    @Override
    void setFogEnableFlag(Context ctx, boolean enableFlag) {
    }

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    @Override
    void setFullSceneAntialiasing(Context ctx, boolean enable) {
    }

    // Native method to update separate specular color control
    @Override
    void updateSeparateSpecularColorEnable(Context ctx, boolean control) {
    }

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    @Override
    boolean validGraphicsMode() {
        return true;
    }

    // native method for setting light enables
    @Override
    void setLightEnables(Context ctx, long enableMask, int maxLights) {
    }

    // native method for setting scene ambient
    @Override
    void setSceneAmbient(Context ctx, float red, float green, float blue) {
    }

    // native method for disabling fog
    @Override
    void disableFog(Context ctx) {
    }

    // native method for disabling modelClip
    @Override
    void disableModelClip(Context ctx) {
    }

    // native method for setting default RenderingAttributes
    @Override
    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
    }

    // native method for setting default texture
    @Override
    void resetTextureNative(Context ctx, int texUnitIndex) {
    }

    // native method for activating a particular texture unit
    @Override
    void activeTextureUnit(Context ctx, int texUnitIndex) {
    }

    // native method for setting default TexCoordGeneration
    @Override
    void resetTexCoordGeneration(Context ctx) {
    }

    // native method for setting default TextureAttributes
    @Override
    void resetTextureAttributes(Context ctx) {
    }

    // native method for setting default PolygonAttributes
    @Override
    void resetPolygonAttributes(Context ctx) {
    }

    // native method for setting default LineAttributes
    @Override
    void resetLineAttributes(Context ctx) {
    }

    // native method for setting default PointAttributes
    @Override
    void resetPointAttributes(Context ctx) {
    }

    // native method for setting default TransparencyAttributes
    @Override
    void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA) {
    }

    // native method for setting default ColoringAttributes
    @Override
    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    @Override
    void syncRender(Context ctx, boolean wait) {
    }

    // The native method that sets this ctx to be the current one
    @Override
    boolean useCtx(Context ctx, Drawable drawable) {
        return true;
    }

    @Override
    void clear(Context ctx, float r, float g, float b, boolean clearStencil) {

    }

    @Override
    void textureFillBackground(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, boolean useBiliearFilter) {

    }

    @Override
    void textureFillRaster(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, float mapZ, float alpha,
            boolean useBiliearFilter)  {

    }

    @Override
    void executeRasterDepth(Context ctx, float posX, float posY, float posZ,
            int srcOffsetX, int srcOffsetY, int rasterWidth, int rasterHeight,
            int depthWidth, int depthHeight, int depthType, Object depthData) {

    }

    // The native method for setting the ModelView matrix.
    @Override
    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
    }

    // The native method for setting the Projection matrix.
    @Override
    void setProjectionMatrix(Context ctx, double[] projMatrix) {
    }

    @Override
    void resizeOffscreenLayer(Canvas3D cv, int width, int height) {}

    // The native method for setting the Viewport.
    @Override
    void setViewport(Context ctx, int x, int y, int width, int height) {
    }

    // used for display Lists
    @Override
    void newDisplayList(Context ctx, int displayListId) {
    }
    @Override
    void endDisplayList(Context ctx) {
    }
    @Override
    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
    }

    @Override
    void freeDisplayList(Context ctx, int id) {
    }
    @Override
    void freeTexture(Context ctx, int id) {
    }

	@Override
	int generateTexID(Context ctx) {
		return 0;
	}

    @Override
    void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight) {
    }

    @Override
    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
        return true;
    }


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    @Override
    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
    }

    // Set glDepthMask.
    @Override
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
    @Override
    GraphicsConfiguration getGraphicsConfig(GraphicsConfiguration gconfig) {
        System.err.println("NoopPipeline.getGraphicsConfig()");
        return gconfig;
    }

    // Get best graphics config from pipeline
    @Override
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
    @Override
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration gc) {
        return true;
    }

    // Methods to get actual capabilities from Canvas3D
    @Override
    boolean hasDoubleBuffer(Canvas3D cv) {
        return true;
    }

    @Override
    boolean hasStereo(Canvas3D cv) {
        return false;
    }

    @Override
    int getStencilSize(Canvas3D cv) {
        return 0;
    }

    @Override
    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
        return false;
    }

    @Override
    boolean hasSceneAntialiasingAccum(Canvas3D cv) {
        return false;
    }

    @Override
    int getScreen(GraphicsDevice graphicsDevice) {
        return 0;
    }


    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    @Override
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
        return new NoopDrawingSurfaceObject(cv);
    }

    // Method to free the drawing surface object
    @Override
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
        // This method is a no-op
    }

    // Method to free the native drawing surface object
    @Override
    void freeDrawingSurfaceNative(Object o) {
        // This method is a no-op
    }

    /**
     * Dummy context for noop pipeline
     */
    static class NoopContext implements Context {
    }

}
