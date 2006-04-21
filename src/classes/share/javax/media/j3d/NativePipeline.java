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
     * Constructor for singleton NativePipeline instance
     */
    protected NativePipeline() {
    }

    /**
     * Initialize the pipeline
     */
    void initialize(Type rendererType) {
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
            if (getRendererType() == Type.NATIVE_OGL) {
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


    // ---------------------------------------------------------------------

    //
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    native void freeD3DSurface(ImageComponent2DRetained image, int hashId);


    // ---------------------------------------------------------------------

    //
    // RasterRetained methods
    //

    // Native method that does the rendering
    native void executeRaster(long ctx, GeometryRetained geo,
            boolean updateAlpha, float alpha,
            int type, int width, int height,
            int xSrcOffset, int ySrcOffset,
            float x, float y, float z, byte[] image);


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


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    native void updateDirectionalLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float x, float y, float z);


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    native void updatePointLight(long ctx,
            int lightSlot, float red, float green,
            float blue, float ax, float ay, float az,
            float px, float py, float pz);


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


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    native void updateExponentialFog(long ctx,
            float red, float green, float blue,
            float density);


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    native void updateLinearFog(long ctx,
            float red, float green, float blue,
            double fdist, double bdist);


    // ---------------------------------------------------------------------

    //
    // LineAttributesRetained methods
    //

    native void updateLineAttributes(long ctx,
            float lineWidth, int linePattern,
            int linePatternMask,
            int linePatternScaleFactor,
            boolean lineAntialiasing);


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


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    native void updateModelClip(long ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D);


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    native void updatePointAttributes(long ctx, float pointSize, boolean pointAntialiasing);


    // ---------------------------------------------------------------------

    //
    // PolygonAttributesRetained methods
    //

    native void updatePolygonAttributes(long ctx,
            int polygonMode, int cullFace,
            boolean backFaceNormalFlip,
            float polygonOffset,
            float polygonOffsetFactor);


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


    // ---------------------------------------------------------------------

    //
    // TextureAttributesRetained methods
    //

    native void updateTextureAttributes(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat);

    native void updateRegisterCombiners(long ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode, float red,
            float green, float blue, float alpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);

    native void updateTextureColorTable(long ctx, int numComponents,
            int colorTableSize,
            int[] colorTable);

    native void updateCombiner(long ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale);


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    native void updateTextureUnitState(long ctx, int unitIndex, boolean enableFlag);


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    native void bindTexture2D(long ctx, int objectId, boolean enable);

    native void updateTexture2DImage(long ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData);

    native void updateTexture2DSubImage(long ctx,
            int level, int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData);

    native void updateTexture2DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    native void updateTexture2DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    native void updateTexture2DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    native void updateDetailTextureParameters(long ctx,
            int detailTextureMode,
            int detailTextureLevel,
            int nPts, float[] pts);

    native void updateTexture2DFilterModes(long ctx,
            int minFilter, int magFilter);

    native void updateTexture2DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    native void updateTexture2DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    native void updateTexture2DAnisotropicFilter(long ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    native void bindTexture3D(long ctx, int objectId, boolean enable);

    native void updateTexture3DImage(long ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height, int depth,
            int boundaryWidth,
            byte[] imageData);

    native void updateTexture3DSubImage(long ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int internalFormat, int storedFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            byte[] imageData);

    native void updateTexture3DLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    native void updateTexture3DLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    native void updateTexture3DBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha);

    native void updateTexture3DFilterModes(long ctx,
            int minFilter, int magFilter);

    native void updateTexture3DSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    native void updateTexture3DFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    native void updateTexture3DAnisotropicFilter(long ctx, float degree);


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    native void bindTextureCubeMap(long ctx, int objectId, boolean enable);

    native void updateTextureCubeMapImage(long ctx,
            int face, int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData);

    native void updateTextureCubeMapSubImage(long ctx,
            int face, int level, int xoffset, int yoffset,
            int internalFormat,int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData);

    native void updateTextureCubeMapLodRange(long ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod);

    native void updateTextureCubeMapLodOffset(long ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ);

    native void updateTextureCubeMapBoundary(long ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha);

    native void updateTextureCubeMapFilterModes(long ctx,
            int minFilter, int magFilter);

    native void updateTextureCubeMapSharpenFunc(long ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts);

    native void updateTextureCubeMapFilter4Func(long ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts);

    native void updateTextureCubeMapAnisotropicFilter(long ctx, float degree);


    //
    // DetailTextureImage methods
    //

    native void bindDetailTexture(long ctx, int objectId);

    native void updateDetailTextureImage(long ctx,
            int numLevels, int level,
            int format, int storedFormat,
            int width, int height,
            int boundaryWidth, byte[] data);


    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Method to return the AWT object
    native long getAWT();

    // Method to initialize the native J3D library
    native boolean initializeJ3D(boolean disableXinerama);

    // Method to get number of procesor
    native int getNumberOfProcessor();

    // Maximum lights supported by the native API 
    native int getMaximumLights();


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods
    //

    // This is the native method for creating the underlying graphics context.
    native long createNewContext(Canvas3D cv, long display, int window,
            long fbConfig, long shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    native void createQueryContext(Canvas3D cv, long display, int window,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable);

    // This is the native for creating offscreen buffer
    native int createOffScreenBuffer(Canvas3D cv, long ctx, long display, long fbConfig, int width, int height);

    native void destroyOffScreenBuffer(Canvas3D cv, long ctx, long display, long fbConfig, int window);

    // This is the native for reading the image from the offscreen buffer
    native void readOffScreenBuffer(Canvas3D cv, long ctx, int format, int width, int height);

    // The native method for swapBuffers
    native int swapBuffers(Canvas3D cv, long ctx, long dpy, int win);

    // notify D3D that Canvas is resize
    native int resizeD3DCanvas(Canvas3D cv, long ctx);

    // notify D3D to toggle between FullScreen and window mode
    native int toggleFullScreenMode(Canvas3D cv, long ctx);

    // native method for setting Material when no material is present
    native void updateMaterialColor(long ctx, float r, float g, float b, float a);

    native void destroyContext(long display, int window, long context);

    // This is the native method for doing accumulation.
    native void accum(long ctx, float value);

    // This is the native method for doing accumulation return.
    native void accumReturn(long ctx);

    // This is the native method for clearing the accumulation buffer.
    native void clearAccum(long ctx);

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    native int getNumCtxLights(long ctx);

    // Native method for decal 1st child setup
    native boolean decal1stChildSetup(long ctx);

    // Native method for decal nth child setup
    native void decalNthChildSetup(long ctx);

    // Native method for decal reset
    native void decalReset(long ctx, boolean depthBufferEnable);

    // Native method for decal reset
    native void ctxUpdateEyeLightingEnable(long ctx, boolean localEyeLightingEnable);

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    native void setBlendColor(long ctx, float red, float green,
            float blue, float alpha);

    // native method for setting blend func
    native void setBlendFunc(long ctx, int src, int dst);

    // native method for setting fog enable flag
    native void setFogEnableFlag(long ctx, boolean enableFlag);

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    native void setFullSceneAntialiasing(long ctx, boolean enable);

    native void setGlobalAlpha(long ctx, float alpha);

    // Native method to update separate specular color control
    native void updateSeparateSpecularColorEnable(long ctx, boolean control);

    // Initialization for D3D when scene begin
    native void beginScene(long ctx);
    native void endScene(long ctx);

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    native boolean validGraphicsMode();

    // native method for setting light enables
    native void setLightEnables(long ctx, long enableMask, int maxLights);

    // native method for setting scene ambient
    native void setSceneAmbient(long ctx, float red, float green, float blue);

    // native method for disabling fog
    native void disableFog(long ctx);

    // native method for disabling modelClip
    native void disableModelClip(long ctx);

    // native method for setting default RenderingAttributes
    native void resetRenderingAttributes(long ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride);

    // native method for setting default texture
    native void resetTextureNative(long ctx, int texUnitIndex);

    // native method for activating a particular texture unit
    native void activeTextureUnit(long ctx, int texUnitIndex);

    // native method for setting default TexCoordGeneration
    native void resetTexCoordGeneration(long ctx);

    // native method for setting default TextureAttributes
    native void resetTextureAttributes(long ctx);

    // native method for setting default PolygonAttributes
    native void resetPolygonAttributes(long ctx);

    // native method for setting default LineAttributes
    native void resetLineAttributes(long ctx);

    // native method for setting default PointAttributes
    native void resetPointAttributes(long ctx);

    // native method for setting default TransparencyAttributes
    native void resetTransparency(long ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA);

    // native method for setting default ColoringAttributes
    native void resetColoringAttributes(long ctx,
            float r, float g,
            float b, float a,
            boolean enableLight);

    // native method for updating the texture unit state map
    native void updateTexUnitStateMap(long ctx, int numActiveTexUnit,
            int[] texUnitStateMap);

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    native void syncRender(long ctx, boolean wait);

    // The native method that sets this ctx to be the current one
    native boolean useCtx(long ctx, long display, int window);

    native void clear(long ctx, float r, float g, float b, int winWidth, int winHeight,
            ImageComponent2DRetained image, int imageScaleMode, byte[] imageYdown);
    native void textureclear(long ctx, int maxX, int maxY,
            float r, float g, float b,
            int winWidth, int winHeight,
            int objectId, int scalemode,
            ImageComponent2DRetained image,
            boolean update);


    // The native method for setting the ModelView matrix.
    native void setModelViewMatrix(long ctx, double[] viewMatrix, double[] modelMatrix);

    // The native method for setting the Projection matrix.
    native void setProjectionMatrix(long ctx, double[] projMatrix);

    // The native method for setting the Viewport.
    native void setViewport(long ctx, int x, int y, int width, int height);

    // used for display Lists
    native void newDisplayList(long ctx, int displayListId);
    native void endDisplayList(long ctx);
    native void callDisplayList(long ctx, int id, boolean isNonUniformScale);

    native void freeDisplayList(long ctx, int id);
    native void freeTexture(long ctx, int id);

    native void composite(long ctx, int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int rasWidth,  byte[] image,
            int winWidth, int winHeight);

    native void texturemapping(long ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight);

    native boolean initTexturemapping(long ctx, int texWidth,
            int texHeight, int objectId);


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    native void setRenderMode(long ctx, int mode, boolean doubleBuffer);

    // Set glDepthMask.
    native void setDepthBufferWriteEnable(long ctx, boolean mode);


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods - logic dealing with native graphics configuration
    // or drawing surface
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

}
