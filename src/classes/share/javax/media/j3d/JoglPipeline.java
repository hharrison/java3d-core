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

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsConfigTemplate;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.media.nativewindow.AbstractGraphicsDevice;
import javax.media.nativewindow.AbstractGraphicsScreen;
import javax.media.nativewindow.CapabilitiesChooser;
import javax.media.nativewindow.CapabilitiesImmutable;
import javax.media.nativewindow.GraphicsConfigurationFactory;
import javax.media.nativewindow.NativeSurface;
import javax.media.nativewindow.NativeWindowFactory;
import javax.media.nativewindow.ProxySurface;
import javax.media.nativewindow.UpstreamSurfaceHook;
import javax.media.nativewindow.VisualIDHolder;
import javax.media.opengl.DefaultGLCapabilitiesChooser;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLCapabilitiesImmutable;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLFBODrawable;
import javax.media.opengl.GLProfile;
import javax.media.opengl.Threading;

import com.jogamp.common.nio.Buffers;
import com.jogamp.nativewindow.awt.AWTGraphicsConfiguration;
import com.jogamp.nativewindow.awt.AWTGraphicsDevice;
import com.jogamp.nativewindow.awt.AWTGraphicsScreen;
import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.opengl.FBObject;

/**
 * Concrete implementation of Pipeline class for the JOGL rendering
 * pipeline.
 */
class JoglPipeline extends Pipeline {
    // Currently prints for entry points not yet implemented
    private static final boolean DEBUG = true;
    // Currently prints for entry points already implemented
    private static final boolean VERBOSE = false;
    // Debugging output for graphics configuration selection
    private static final boolean DEBUG_CONFIG = false;
    // Prints extra debugging information
    private static final boolean EXTRA_DEBUGGING = false;
    // Number of milliseconds to wait for windows to pop up on screen
    private static final int WAIT_TIME = 1000;
    // Configurable constant just in case we want to change this later
    private static final int MIN_FRAME_SIZE = 1;

    private GLProfile profile;
    /**
     * Constructor for singleton JoglPipeline instance
     */
    protected JoglPipeline() {
    }

    /**
     * Initialize the pipeline
     */
    @Override
    void initialize(Pipeline.Type pipelineType) {
        super.initialize(pipelineType);

        assert pipelineType == Pipeline.Type.JOGL;

        // Java3D maintains strict control over which threads perform OpenGL work
        Threading.disableSingleThreading();

        profile = GLProfile.getMaxFixedFunc(true);
        // TODO: finish this with any other needed initialization
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
            int[] texUnitOffset,
            int numActiveTexUnitState,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] carray, int cDirty) {
        if (VERBOSE) System.err.println("JoglPipeline.execute()");

        executeGeometryArray(ctx, geo, geo_type, isNonUniformScale, useAlpha,
                ignoreVertexColors, startVIndex, vcount, vformat,
                texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                texUnitOffset, numActiveTexUnitState,
                vertexAttrCount, vertexAttrSizes,
                varray, null, carray, cDirty);
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
            int initialCoordIndex, float[] vfcoords, double[] vdcoords,
            int initialColorIndex, float[] cfdata, byte[] cbdata,
            int initialNormalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, float[][] vertexAttrData,
            int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty) {
        if (VERBOSE) System.err.println("JoglPipeline.executeVA()");

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        FloatBuffer fverts = null;
        DoubleBuffer dverts = null;
        FloatBuffer fclrs = null;
        ByteBuffer bclrs = null;
        FloatBuffer[] texCoordBufs = null;
        FloatBuffer norms = null;
        FloatBuffer[] vertexAttrBufs = null;

        // Get vertex attribute arrays
        if (vattrDefined) {
            vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
        }

        // get texture arrays
        if (textureDefined) {
            texCoordBufs = getTexCoordSetBuffer(texCoords);
        }

        // get coordinate array
        if (floatCoordDefined) {
            fverts = getVertexArrayBuffer(vfcoords);
        } else if (doubleCoordDefined) {
            dverts = getVertexArrayBuffer(vdcoords);
        }

        // get color array
        if (floatColorsDefined) {
            fclrs = getColorArrayBuffer(cfdata);
        } else if (byteColorsDefined) {
            bclrs = getColorArrayBuffer(cbdata);
        }

        // get normal array
        if (normalsDefined) {
            norms = getNormalArrayBuffer(ndata);
        }

        int[] sarray = null;
        int[] start_array = null;
        int strip_len = 0;
        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            sarray = ((GeometryStripArrayRetained) geo).stripVertexCounts;
            strip_len = sarray.length;
            start_array = ((GeometryStripArrayRetained) geo).stripStartOffsetIndices;
        }

        executeGeometryArrayVA(ctx, geo, geo_type,
                isNonUniformScale, ignoreVertexColors,
                vcount, vformat, vdefined,
                initialCoordIndex, fverts, dverts,
                initialColorIndex, fclrs, bclrs,
                initialNormalIndex, norms,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndices, vertexAttrBufs,
                texCoordMapLength,
                texcoordoffset, numActiveTexUnitState,
                texIndex, texstride, texCoordBufs, cdirty,
                sarray, strip_len, start_array);
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
            int initialCoordIndex,
            Buffer vcoords,
            int initialColorIndex,
            Buffer cdataBuffer,
            float[] cfdata, byte[] cbdata,
            int initialNormalIndex, FloatBuffer ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, FloatBuffer[] vertexAttrData,
            int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int[] texIndex, int texstride, Object[] texCoords,
            int cdirty) {
        if (VERBOSE) System.err.println("JoglPipeline.executeVABuffer()");

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        FloatBuffer fverts = null;
        DoubleBuffer dverts = null;
        FloatBuffer fclrs = null;
        ByteBuffer bclrs = null;
        FloatBuffer[] texCoordBufs = null;
        FloatBuffer norms = null;
        FloatBuffer[] vertexAttrBufs = null;

        // Get vertex attribute arrays
        if (vattrDefined)
            vertexAttrBufs = vertexAttrData;

        // get texture arrays
        if (textureDefined) {
            texCoordBufs = new FloatBuffer[texCoords.length];
            for (int i = 0; i < texCoords.length; i++) {
                texCoordBufs[i] = (FloatBuffer) texCoords[i];
            }
        }

        // get coordinate array
        if (floatCoordDefined) {
            fverts = (FloatBuffer) vcoords;
        } else if (doubleCoordDefined) {
            dverts = (DoubleBuffer) vcoords;
        }

        if (fverts == null && dverts == null) {
            return;
        }

        // get color array
        if (floatColorsDefined) {
            if (cfdata != null)
                fclrs = getColorArrayBuffer(cfdata);
            else
                fclrs = (FloatBuffer) cdataBuffer;
        } else if (byteColorsDefined) {
            if (cbdata != null)
                bclrs = getColorArrayBuffer(cbdata);
            else
                bclrs = (ByteBuffer) cdataBuffer;
        }

        // get normal array
        if (normalsDefined) {
            norms = ndata;
        }

        int[] sarray = null;
        int[] start_array = null;
        int strip_len = 0;
        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            sarray = ((GeometryStripArrayRetained) geo).stripVertexCounts;
            strip_len = sarray.length;
            start_array = ((GeometryStripArrayRetained) geo).stripStartOffsetIndices;
        }

        executeGeometryArrayVA(ctx, geo, geo_type,
                isNonUniformScale, ignoreVertexColors,
                vcount, vformat, vdefined,
                initialCoordIndex, fverts, dverts,
                initialColorIndex, fclrs, bclrs,
                initialNormalIndex, norms,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndices, vertexAttrBufs,
                texCoordMapLength,
                texcoordoffset, numActiveTexUnitState,
                texIndex, texstride, texCoordBufs, cdirty,
                sarray, strip_len, start_array);
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
            int[] texUnitOffset,
            int numActiveTexUnit,
            FloatBuffer varray, float[] cdata, int cdirty) {
        if (VERBOSE) System.err.println("JoglPipeline.executeInterleavedBuffer()");

        executeGeometryArray(ctx, geo, geo_type,
                isNonUniformScale, useAlpha, ignoreVertexColors,
                startVIndex, vcount, vformat,
                texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                texUnitOffset, numActiveTexUnit, 0, null,
                null, varray, cdata, cdirty);
    }

    @Override
    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
        if (VERBOSE) System.err.println("JoglPipeline.setVertexFormat()");

		GL2 gl = context(ctx).getGL().getGL2();

        // Enable and disable the appropriate pointers
        if ((vformat & GeometryArray.NORMALS) != 0) {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        } else {
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        }
        if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
        } else {
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

// FIXME: SUN_global_alpha
//        if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
//            if (useAlpha) {
//                gl.glEnable(GL.GL_GLOBAL_ALPHA_SUN);
//            } else {
//                gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
//            }
//        }

        if ((vformat & GeometryArray.COORDINATES) != 0) {
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        } else {
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        }
    }

    @Override
    void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors) {
        if (VERBOSE) System.err.println("JoglPipeline.disableGlobalAlpha()");
// FIXME: SUN_global_alpha
//        GL gl = context(ctx).getGL();

//        if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
//            if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
//                if (useAlpha) {
//                    gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
//                }
//            }
//        }
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
        if (VERBOSE) System.err.println("JoglPipeline.buildGA()");
        JoglContext jctx = (JoglContext) ctx;
		GL2 gl = context(ctx).getGL().getGL2();
        FloatBuffer verts = null;
        int stride = 0, coordoff = 0, normoff = 0, coloroff = 0, texCoordoff = 0;
        int texStride = 0;
        int vAttrOff = 0;
        if ((vformat & GeometryArray.COORDINATES) != 0) {
            stride += 3;
        }
        if ((vformat & GeometryArray.NORMALS) != 0) {
            stride += 3;
            coordoff += 3;
        }

        if ((vformat & GeometryArray.COLOR) != 0) {
            if ((vformat & GeometryArray.BY_REFERENCE) != 0) {
                if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                    stride += 4;
                    normoff += 4;
                    coordoff += 4;
                } else {
                    stride += 3;
                    normoff += 3;
                    coordoff += 3;
                }
            } else {
                stride += 4;
                normoff += 4;
                coordoff += 4;
            }
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                texStride = 2 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                texStride = 3 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
                texStride = 4 * texCoordSetCount;
            }
            stride += texStride;
            normoff += texStride;
            coloroff += texStride;
            coordoff += texStride;
        }

        int vAttrStride = 0;
        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            for (int i = 0; i < vertexAttrCount; i++) {
                vAttrStride += vertexAttrSizes[i];
            }
            stride += vAttrStride;
            normoff += vAttrStride;
            coloroff += vAttrStride;
            coordoff += vAttrStride;
            texCoordoff += vAttrStride;
        }

		int bstride = stride * Buffers.SIZEOF_FLOAT;
        // Start sending down from the startVIndex
        int initialOffset = startVIndex * stride;
        normoff += initialOffset;
        coloroff += initialOffset;
        coordoff += initialOffset;
        texCoordoff += initialOffset;
        vAttrOff += initialOffset;

        // process alpha for geometryArray without alpha
        boolean useAlpha = false;
        if (updateAlpha && !ignoreVertexColors) {
            useAlpha = true;
        }

        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            int[] sarray = ((GeometryStripArrayRetained) geo).stripVertexCounts;

            int primType = 0;
            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_TRI_STRIP_SET :
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_TRI_FAN_SET :
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_LINE_STRIP_SET :
                    primType = GL.GL_LINE_STRIP;
                    break;
            }

            if (ignoreVertexColors) {
                vformat &= ~GeometryArray.COLOR;
            }

            for (int i = 0; i < sarray.length; i++) {
                gl.glBegin(primType);
                for (int j = 0; j < sarray[i]; j++) {
                    if ((vformat & GeometryArray.NORMALS) != 0) {
                        if (nxform != null) {
                            float nx = (float) (nxform[0] * varray[normoff] +
                                    nxform[1] * varray[normoff+1] +
                                    nxform[2] * varray[normoff+2]);
                            float ny = (float) (nxform[4] * varray[normoff] +
                                    nxform[5] * varray[normoff+1] +
                                    nxform[6] * varray[normoff+2]);
                            float nz = (float) (nxform[8] * varray[normoff] +
                                    nxform[9] * varray[normoff+1] +
                                    nxform[10] * varray[normoff+2]);
                            gl.glNormal3f(nx, ny, nz);
                        } else {
                            gl.glNormal3f(varray[normoff], varray[normoff+1], varray[normoff+2]);
                        }
                    }
                    if ((vformat & GeometryArray.COLOR) != 0) {
                        if (useAlpha) {
                            gl.glColor4f(varray[coloroff],
                                    varray[coloroff+1],
                                    varray[coloroff+2],
                                    varray[coloroff+3] * alpha);
                        } else {
                            if ((vformat & GeometryArray.WITH_ALPHA) != 0) { // alpha is present
                                gl.glColor4f(varray[coloroff],
                                        varray[coloroff+1],
                                        varray[coloroff+2],
                                        varray[coloroff+3]);
                            } else {
                                gl.glColor3f(varray[coloroff],
                                        varray[coloroff+1],
                                        varray[coloroff+2]);
                            }
                        }
                    }

                    if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                        int vaOff = vAttrOff;
                        if (verts == null) {
                            verts = FloatBuffer.wrap(varray);
                        }
                        for (int vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
                            switch (vertexAttrSizes[vaIdx]) {
                                case 1:
                                    verts.position(vaOff);
                                    jctx.vertexAttr1fv(gl, vaIdx, verts);
                                    break;
                                case 2:
                                    verts.position(vaOff);
                                    jctx.vertexAttr2fv(gl, vaIdx, verts);
                                    break;
                                case 3:
                                    verts.position(vaOff);
                                    jctx.vertexAttr3fv(gl, vaIdx, verts);
                                    break;
                                case 4:
                                    verts.position(vaOff);
                                    jctx.vertexAttr4fv(gl, vaIdx, verts);
                                    break;
                            }

                            vaOff += vertexAttrSizes[vaIdx];
                        }
                    }

                    if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                        if (texCoordSetMapLen > 0) {
                            if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
                                if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                                    for (int k = 0; k < texCoordSetMapLen; k++) {
                                        if (texCoordSetMapOffset[k] != -1) {
                                            int off = texCoordoff + texCoordSetMapOffset[k];
                                            gl.glMultiTexCoord2f(GL.GL_TEXTURE0 + k,
                                                    varray[off],
                                                    varray[off + 1]);
                                        }
                                    }
                                } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                                    for (int k = 0; k < texCoordSetMapLen; k++) {
                                        if (texCoordSetMapOffset[k] != -1) {
                                            int off = texCoordoff + texCoordSetMapOffset[k];
                                            gl.glMultiTexCoord3f(GL.GL_TEXTURE0 + k,
                                                    varray[off],
                                                    varray[off + 1],
                                                    varray[off + 2]);
                                        }
                                    }
                                } else {
                                    for (int k = 0; k < texCoordSetMapLen; k++) {
                                        if (texCoordSetMapOffset[k] != -1) {
                                            int off = texCoordoff + texCoordSetMapOffset[k];
                                            gl.glMultiTexCoord4f(GL.GL_TEXTURE0 + k,
                                                    varray[off],
                                                    varray[off + 1],
                                                    varray[off + 2],
                                                    varray[off + 3]);
                                        }
                                    }
                                }
                            } else { // no multitexture
                                if (texCoordSetMapOffset[0] != -1) {
                                    int off = texCoordoff + texCoordSetMapOffset[0];
                                    if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                                        gl.glTexCoord2f(varray[off], varray[off + 1]);
                                    } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                                        gl.glTexCoord3f(varray[off], varray[off + 1], varray[off + 2]);
                                    } else {
                                        gl.glTexCoord4f(varray[off], varray[off + 1], varray[off + 2], varray[off + 3]);
                                    }
                                }
                            } // no multitexture
                        }
                        // texCoordSetMapLen can't be 0 if texture coordinates
                        // is to be specified
                    }

                    if ((vformat & GeometryArray.COORDINATES) != 0) {
                        if (xform != null) {
                            // transform the vertex data with the static transform
                            float w    = (float) (xform[12] * varray[coordoff] +
                                    xform[13] * varray[coordoff+1] +
                                    xform[14] * varray[coordoff+2] +
                                    xform[15]);
                            float winv = 1.0f/w;
                            float vx = (float) (xform[0] * varray[coordoff] +
                                    xform[1] * varray[coordoff+1] +
                                    xform[2] * varray[coordoff+2] +
                                    xform[3]) * winv;
                            float vy = (float) (xform[4] * varray[coordoff] +
                                    xform[5] * varray[coordoff+1] +
                                    xform[6] * varray[coordoff+2] +
                                    xform[7]) * winv;
                            float vz = (float) (xform[8] * varray[coordoff] +
                                    xform[9] * varray[coordoff+1] +
                                    xform[10] * varray[coordoff+2] +
                                    xform[11]) * winv;
                            gl.glVertex3f(vx, vy, vz);
                        } else {
                            gl.glVertex3f(varray[coordoff], varray[coordoff + 1], varray[coordoff + 2]);
                        }
                    }
                    normoff += stride;
                    coloroff += stride;
                    coordoff += stride;
                    texCoordoff += stride;
                    vAttrOff += stride;
                }
                gl.glEnd();
            }
        } else if ((geo_type == GeometryRetained.GEO_TYPE_QUAD_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_TRI_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_POINT_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_LINE_SET)) {
            int primType = 0;
            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_QUAD_SET :
                    primType = GL2.GL_QUADS;
                    break;
                case GeometryRetained.GEO_TYPE_TRI_SET :
                    primType = GL.GL_TRIANGLES;
                    break;
                case GeometryRetained.GEO_TYPE_POINT_SET :
                    primType = GL.GL_POINTS;
                    break;
                case GeometryRetained.GEO_TYPE_LINE_SET :
                    primType = GL.GL_LINES;
                    break;
            }

            if (ignoreVertexColors) {
                vformat &= ~GeometryArray.COLOR;
            }

            gl.glBegin(primType);
            for (int j = 0; j < vcount; j++) {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    if (nxform != null) {
                        float nx = (float) (nxform[0] * varray[normoff] +
                                nxform[1] * varray[normoff+1] +
                                nxform[2] * varray[normoff+2]);
                        float ny = (float) (nxform[4] * varray[normoff] +
                                nxform[5] * varray[normoff+1] +
                                nxform[6] * varray[normoff+2]);
                        float nz = (float) (nxform[8] * varray[normoff] +
                                nxform[9] * varray[normoff+1] +
                                nxform[10] * varray[normoff+2]);
                        gl.glNormal3f(nx, ny, nz);
                    } else {
                        gl.glNormal3f(varray[normoff], varray[normoff + 1], varray[normoff + 2]);
                    }
                }
                if ((vformat & GeometryArray.COLOR) != 0) {
                    if (useAlpha) {
                        float cr, cg, cb, ca;
                        if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                            cr = varray[coloroff];
                            cg = varray[coloroff + 1];
                            cb = varray[coloroff + 2];
                            ca = varray[coloroff + 3] * alpha;
                        } else {
                            cr = varray[coloroff];
                            cg = varray[coloroff + 1];
                            cb = varray[coloroff + 2];
                            ca = alpha;
                        }
                        gl.glColor4f(cr, cg, cb, ca);
                    } else {
                        if ((vformat & GeometryArray.WITH_ALPHA) != 0) { // alpha is present
                            gl.glColor4f(varray[coloroff],
                                    varray[coloroff + 1],
                                    varray[coloroff + 2],
                                    varray[coloroff + 3]);
                        } else {
                            gl.glColor3f(varray[coloroff],
                                    varray[coloroff + 1],
                                    varray[coloroff + 2]);
                        }
                    }
                }

                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vaOff = vAttrOff;
                    if (verts == null) {
                        verts = FloatBuffer.wrap(varray);
                    }
                    for (int vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
                        switch (vertexAttrSizes[vaIdx]) {
                            case 1:
                                verts.position(vaOff);
                                jctx.vertexAttr1fv(gl, vaIdx, verts);
                                break;
                            case 2:
                                verts.position(vaOff);
                                jctx.vertexAttr2fv(gl, vaIdx, verts);
                                break;
                            case 3:
                                verts.position(vaOff);
                                jctx.vertexAttr3fv(gl, vaIdx, verts);
                                break;
                            case 4:
                                verts.position(vaOff);
                                jctx.vertexAttr4fv(gl, vaIdx, verts);
                                break;
                        }

                        vaOff += vertexAttrSizes[vaIdx];
                    }
                }

                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    if (texCoordSetMapLen > 0) {
                        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
                            if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                                for (int k = 0; k < texCoordSetMapLen; k++) {
                                    if (texCoordSetMapOffset[k] != -1) {
                                        int off = texCoordoff + texCoordSetMapOffset[k];
                                        gl.glMultiTexCoord2f(GL.GL_TEXTURE0 + k,
                                                varray[off],
                                                varray[off + 1]);
                                    }
                                }
                            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                                for (int k = 0; k < texCoordSetMapLen; k++) {
                                    if (texCoordSetMapOffset[k] != -1) {
                                        int off = texCoordoff + texCoordSetMapOffset[k];
                                        gl.glMultiTexCoord3f(GL.GL_TEXTURE0 + k,
                                                varray[off],
                                                varray[off + 1],
                                                varray[off + 2]);
                                    }
                                }
                            } else {
                                for (int k = 0; k < texCoordSetMapLen; k++) {
                                    if (texCoordSetMapOffset[k] != -1) {
                                        int off = texCoordoff + texCoordSetMapOffset[k];
                                        gl.glMultiTexCoord4f(GL.GL_TEXTURE0 + k,
                                                varray[off],
                                                varray[off + 1],
                                                varray[off + 2],
                                                varray[off + 3]);
                                    }
                                }
                            }
                        } else { // no multitexture
                            if (texCoordSetMapOffset[0] != -1) {
                                int off = texCoordoff + texCoordSetMapOffset[0];
                                if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                                    gl.glTexCoord2f(varray[off], varray[off + 1]);
                                } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                                    gl.glTexCoord3f(varray[off], varray[off + 1], varray[off + 2]);
                                } else {
                                    gl.glTexCoord4f(varray[off], varray[off + 1], varray[off + 2], varray[off + 3]);
                                }
                            }
                        } // no multitexture
                    }
                    // texCoordSetMapLen can't be 0 if texture coordinates is
                    // to be specified
                }

                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    if (xform != null) {
                        // transform the vertex data with the static transform
                        float w = (float) (xform[12] * varray[coordoff] +
                                xform[13] * varray[coordoff+1] +
                                xform[14] * varray[coordoff+2] +
                                xform[15]);
                        float winv = 1.0f/w;
                        float vx = (float) (xform[0] * varray[coordoff] +
                                xform[1] * varray[coordoff+1] +
                                xform[2] * varray[coordoff+2] +
                                xform[3]) * winv;
                        float vy = (float) (xform[4] * varray[coordoff] +
                                xform[5] * varray[coordoff+1] +
                                xform[6] * varray[coordoff+2] +
                                xform[7]) * winv;
                        float vz = (float) (xform[8] * varray[coordoff] +
                                xform[9] * varray[coordoff+1] +
                                xform[10] * varray[coordoff+2] +
                                xform[11]) * winv;
                        gl.glVertex3f(vx, vy, vz);
                    } else {
                        gl.glVertex3f(varray[coordoff], varray[coordoff + 1], varray[coordoff + 2]);
                    }
                }
                normoff += stride;
                coloroff += stride;
                coordoff += stride;
                texCoordoff += stride;
                vAttrOff += stride;
            }
            gl.glEnd();
        }
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
            int initialCoordIndex, float[] vfcoords, double[] vdcoords,
            int initialColorIndex, float[] cfdata, byte[] cbdata,
            int initialNormalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, float[][] vertexAttrData,
            int texCoordMapLength,
            int[] tcoordsetmap,
            int[] texIndices, int texStride, Object[] texCoords,
            double[] xform, double[] nxform) {
        if (VERBOSE) System.err.println("JoglPipeline.buildGAForByRef()");

		GL2 gl = context(ctx).getGL().getGL2();

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        FloatBuffer fverts = null;
        DoubleBuffer dverts = null;
        FloatBuffer fclrs = null;
        ByteBuffer bclrs = null;
        FloatBuffer[] texCoordBufs = null;
        FloatBuffer norms = null;
        FloatBuffer[] vertexAttrBufs = null;

        // Get vertex attribute arrays
        if (vattrDefined) {
            vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
        }

        // get texture arrays
        if (textureDefined) {
            texCoordBufs = getTexCoordSetBuffer(texCoords);
        }

        // process alpha for geometryArray without alpha
        boolean useAlpha = false;
        if (updateAlpha && !ignoreVertexColors) {
            useAlpha = true;
        }

        int[] sarray = null;
        int[] start_array = null;
        int strip_len = 0;
        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            sarray = ((GeometryStripArrayRetained) geo).stripVertexCounts;
            strip_len = sarray.length;
            start_array = ((GeometryStripArrayRetained) geo).stripStartOffsetIndices;
        }

        if (ignoreVertexColors) {
            vformat &= ~GeometryArray.COLOR;
            floatColorsDefined = false;
            byteColorsDefined = false;
        }

        // get coordinate array
        if (floatCoordDefined) {
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            fverts = getVertexArrayBuffer(vfcoords, (xform == null));
            if (xform != null) {
                // Must copy in and transform data
                for (int i = initialCoordIndex; i < vcount * 3; i += 3) {
                    fverts.put(i  , (float) (xform[0]  * vfcoords[i] +
                            xform[1]  * vfcoords[i+1] +
                            xform[2]  * vfcoords[i+2]));
                    fverts.put(i+1, (float) (xform[4]  * vfcoords[i] +
                            xform[5]  * vfcoords[i+1] +
                            xform[6]  * vfcoords[i+2]));
                    fverts.put(i+2, (float) (xform[8]  * vfcoords[i] +
                            xform[9]  * vfcoords[i+1] +
                            xform[10] * vfcoords[i+2]));
                }
            }
        } else if (doubleCoordDefined) {
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            dverts = getVertexArrayBuffer(vdcoords, (xform == null));
            if (xform != null) {
                // Must copy in and transform data
                for (int i = initialCoordIndex; i < vcount * 3; i += 3) {
                    dverts.put(i  , (xform[0]  * vdcoords[i] +
                            xform[1]  * vdcoords[i+1] +
                            xform[2]  * vdcoords[i+2]));
                    dverts.put(i+1, (xform[4]  * vdcoords[i] +
                            xform[5]  * vdcoords[i+1] +
                            xform[6]  * vdcoords[i+2]));
                    dverts.put(i+2, (xform[8]  * vdcoords[i] +
                            xform[9]  * vdcoords[i+1] +
                            xform[10] * vdcoords[i+2]));
                }
            }
        } else {
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        }

        // get color array
        if (floatColorsDefined) {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            fclrs = getColorArrayBuffer(cfdata, !useAlpha);
            if (useAlpha) {
                // Must copy in and modify color data
                if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                    for (int i = initialColorIndex; i < vcount * 4; i += 4) {
                        fclrs.put(i  , cfdata[i]);
                        fclrs.put(i+1, cfdata[i+1]);
                        fclrs.put(i+2, cfdata[i+2]);
                        fclrs.put(i+3, alpha * cfdata[i+3]);
                    }
                } else {
                    int k = 0;
                    for (int i = initialColorIndex; i < vcount * 4; i += 4) {
                        fclrs.put(i  , cfdata[k++]);
                        fclrs.put(i+1, cfdata[k++]);
                        fclrs.put(i+2, cfdata[k++]);
                        fclrs.put(i+3, alpha);
                    }
                }
                vformat |= GeometryArray.WITH_ALPHA;
            }
        } else if (byteColorsDefined) {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            bclrs = getColorArrayBuffer(cbdata, !useAlpha);
            if (useAlpha) {
                // Must copy in and modify color data
                if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                    for (int i = initialColorIndex; i < vcount * 4; i += 4) {
                        bclrs.put(i  , cbdata[i]);
                        bclrs.put(i+1, cbdata[i+1]);
                        bclrs.put(i+2, cbdata[i+2]);
                        bclrs.put(i+3, (byte) (alpha * (int) (cbdata[i+3] & 0xFF)));
                    }
                } else {
                    int k = 0;
                    for (int i = initialColorIndex; i < vcount * 4; i += 4) {
                        bclrs.put(i  , cbdata[k++]);
                        bclrs.put(i+1, cbdata[k++]);
                        bclrs.put(i+2, cbdata[k++]);
                        bclrs.put(i+3, (byte) (alpha * 255.0f));
                    }
                }
                vformat |= GeometryArray.WITH_ALPHA;
            }
        } else {
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

        // get normal array
        if (normalsDefined) {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            norms = getNormalArrayBuffer(ndata, (nxform == null));
            if (nxform != null) {
                // Must copy in and transform data
                for (int i = initialNormalIndex; i < vcount * 3; i += 3) {
                    norms.put(i  , (float) (nxform[0]  * ndata[i] +
                            nxform[1]  * ndata[i+1] +
                            nxform[2]  * ndata[i+2]));
                    norms.put(i+1, (float) (nxform[4]  * ndata[i] +
                            nxform[5]  * ndata[i+1] +
                            nxform[6]  * ndata[i+2]));
                    norms.put(i+2, (float) (nxform[8]  * ndata[i] +
                            nxform[9]  * ndata[i+1] +
                            nxform[10] * ndata[i+2]));
                }
            }
        } else {
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        }

        executeGeometryArrayVA(ctx, geo, geo_type,
                isNonUniformScale, ignoreVertexColors,
                vcount, vformat, vdefined,
                initialCoordIndex, fverts, dverts,
                initialColorIndex, fclrs, bclrs,
                initialNormalIndex, norms,
                vertexAttrCount, vertexAttrSizes,
                vertexAttrIndices, vertexAttrBufs,
                texCoordMapLength,
                tcoordsetmap, texCoordMapLength,
                texIndices, texStride, texCoordBufs, 0,
                sarray, strip_len, start_array);
    }

    //----------------------------------------------------------------------
    // Private helper methods for GeometryArrayRetained
    //

    private void
            testForInterleavedArrays(int vformat,
            boolean[] useInterleavedArrays,
            int[] iaFormat) {
        if (VERBOSE) System.err.println("JoglPipeline.testForInterleavedArrays()");
        useInterleavedArrays[0] = true;
        switch (vformat) {
            case GeometryArray.COORDINATES :
                iaFormat[0] = GL2.GL_V3F; break;
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS) :
                iaFormat[0] = GL2.GL_N3F_V3F; break;
            case (GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2) :
                iaFormat[0] = GL2.GL_T2F_V3F; break;
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR) :
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.WITH_ALPHA) :
                iaFormat[0] = GL2.GL_C4F_N3F_V3F; break;
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2) :
                iaFormat[0] = GL2.GL_T2F_N3F_V3F; break;
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.TEXTURE_COORDINATE_2):
            case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.WITH_ALPHA | GeometryArray.TEXTURE_COORDINATE_2):
                iaFormat[0] = GL2.GL_T2F_C4F_N3F_V3F; break;
            default:
                useInterleavedArrays[0] = false; break;
        }
    }

    private void
            enableTexCoordPointer(GL2 gl,
            int texUnit,
            int texSize,
            int texDataType,
            int stride,
            Buffer pointer) {
        if (VERBOSE) System.err.println("JoglPipeline.enableTexCoordPointer()");
        clientActiveTextureUnit(gl, texUnit);
        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(texSize, texDataType, stride, pointer);
    }

	private void disableTexCoordPointer(GL2 gl, int texUnit) {
        if (VERBOSE) System.err.println("JoglPipeline.disableTexCoordPointer()");
        clientActiveTextureUnit(gl, texUnit);
        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
    }

	private void clientActiveTextureUnit(GL2 gl, int texUnit) {
        if (VERBOSE) System.err.println("JoglPipeline.clientActiveTextureUnit()");
        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glClientActiveTexture(texUnit + GL.GL_TEXTURE0);
        }
    }


    private void
            executeTexture(int texCoordSetMapLen,
            int texSize, int bstride, int texCoordoff,
            int[] texCoordSetMapOffset,
            int numActiveTexUnit,
            FloatBuffer verts, GL2 gl) {
        if (VERBOSE) System.err.println("JoglPipeline.executeTexture()");
        int tus = 0;  /* texture unit state index */

        for (int i = 0; i < numActiveTexUnit; i++) {

            tus = i;

      /*
       * it's possible thattexture unit state index (tus)
       * is greater than the texCoordSetMapOffsetLen, in this
       * case, just disable TexCoordPointer.
       */
            if ((tus < texCoordSetMapLen) &&
                    (texCoordSetMapOffset[tus] != -1)) {
                if (EXTRA_DEBUGGING) {
                    System.err.println("  texCoord position " + i + ": " + (texCoordoff + texCoordSetMapOffset[tus]));
                }
                verts.position(texCoordoff + texCoordSetMapOffset[tus]);
                enableTexCoordPointer(gl, i,
                        texSize, GL.GL_FLOAT, bstride,
                        verts);
            } else {
                disableTexCoordPointer(gl, i);
            }
        }
    }

	private void resetTexture(GL2 gl, JoglContext ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetTexture()");
        /* Disable texture coordinate arrays for all texture units */
        for (int i = 0; i < ctx.getMaxTexCoordSets(); i++) {
            disableTexCoordPointer(gl, i);
        }
        /* Reset client active texture unit to 0 */
        clientActiveTextureUnit(gl, 0);
    }

    private void
            executeGeometryArray(Context absCtx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean useAlpha,
            boolean ignoreVertexColors,
            int startVIndex, int vcount, int vformat,
            int texCoordSetCount, int[] texCoordSetMap,
            int texCoordSetMapLen,
            int[] texCoordSetMapOffset,
            int numActiveTexUnitState,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, FloatBuffer varrayBuffer,
            float[] carray, int cDirty) {
        if (VERBOSE) System.err.println("JoglPipeline.executeGeometryArray()");
        JoglContext ctx = (JoglContext) absCtx;
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        boolean useInterleavedArrays;
        int iaFormat = 0;
        int primType = 0;
        int stride = 0, coordoff = 0, normoff = 0, coloroff = 0, texCoordoff = 0;
        int texSize = 0, texStride = 0;
        int vAttrOff = 0;
        int vAttrStride = 0;
        int bstride = 0, cbstride = 0;
        FloatBuffer verts = null;
        FloatBuffer clrs  = null;
        int[] sarray = null;
        int[] start_array = null;

        if (EXTRA_DEBUGGING) {
            System.err.println("Vertex format: " + getVertexDescription(vformat));
            System.err.println("Geometry type: " + getGeometryDescription(geo_type));
            if (carray != null) {
                System.err.println("  Separate color array");
            } else {
                System.err.println("  Colors (if any) interleaved");
            }
        }

        if ((vformat & GeometryArray.COORDINATES) != 0) {
            stride += 3;
        }
        if ((vformat & GeometryArray.NORMALS) != 0) {
            stride += 3;
            coordoff += 3;
        }
        if ((vformat & GeometryArray.COLOR) != 0) {
            if ((vformat & GeometryArray.WITH_ALPHA) != 0 ) {
                stride += 4;
                normoff += 4;
                coordoff += 4;
            } else { /* Handle the case of executeInterleaved 3f */
                stride += 3;
                normoff += 3;
                coordoff += 3;
            }
        }
        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            if (EXTRA_DEBUGGING) {
                System.err.println("  Number of tex coord sets: " + texCoordSetCount);
            }
            if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                texSize = 2;
                texStride = 2 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                texSize = 3;
                texStride = 3 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
                texSize = 4;
                texStride = 4 * texCoordSetCount;
            }
            stride += texStride;
            normoff += texStride;
            coloroff += texStride;
            coordoff += texStride;
        }
        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            for (int i = 0; i < vertexAttrCount; i++) {
                vAttrStride += vertexAttrSizes[i];
            }
            stride += vAttrStride;
            normoff += vAttrStride;
            coloroff += vAttrStride;
            coordoff += vAttrStride;
            texCoordoff += vAttrStride;
        }

		bstride = stride * Buffers.SIZEOF_FLOAT;

        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            sarray = ((GeometryStripArrayRetained) geo).stripVertexCounts;
            start_array = ((GeometryStripArrayRetained) geo).stripStartOffsetIndices;
        }

        // We have to copy if the data isn't specified using NIO
        if (varray != null) {
            verts = getVertexArrayBuffer(varray);
        } else if (varrayBuffer != null) {
            verts = varrayBuffer;
        } else {
            // This should never happen
            throw new AssertionError("Unable to get vertex pointer");
        }

        // using byRef interleaved array and has a separate pointer, then ..
        int cstride = stride;
        if (carray != null) {
            clrs = getColorArrayBuffer(carray);
            cstride = 4;
        } else {
            // FIXME: need to "auto-slice" this buffer later
            clrs = verts;
        }

		cbstride = cstride * Buffers.SIZEOF_FLOAT;

        // Enable normalize for non-uniform scale (which rescale can't handle)
        if (isNonUniformScale) {
            gl.glEnable(GL2.GL_NORMALIZE);
        }

        int startVertex = stride  * startVIndex;
        int startClrs   = cstride * startVIndex;
        if (clrs == verts) {
            startClrs += coloroff;
        }

        /*** Handle non-indexed strip GeometryArray first *******/
        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            if (ignoreVertexColors || (carray != null) ||
                    ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0 && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }
            if (useInterleavedArrays) {
                verts.position(startVertex);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(startVertex + normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }
                if (!ignoreVertexColors && (vformat & GeometryArray.COLOR) != 0) {
                    if (EXTRA_DEBUGGING) {
                        System.err.println("  Doing colors");
                    }
                    clrs.position(startClrs);
                    if ((vformat & GeometryArray.WITH_ALPHA) != 0 || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, cbstride, clrs);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, cbstride, clrs);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(startVertex + coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }

                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetMapOffset,
                            numActiveTexUnitState,
                            verts, gl);
                }

                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = startVertex + vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }
            }

            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_TRI_STRIP_SET:
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_TRI_FAN_SET:
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
                    primType = GL.GL_LINE_STRIP;
                    break;
            }

            if (gl.isExtensionAvailable("GL_EXT_multi_draw_arrays")) {
                gl.glMultiDrawArrays(primType, start_array, 0, sarray, 0, sarray.length);
            } else {
                for (int i = 0; i < sarray.length; i++) {
                    gl.glDrawArrays(primType, start_array[i], sarray[i]);
                }
            }
        } else if ((geo_type == GeometryRetained.GEO_TYPE_QUAD_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_TRI_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_POINT_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_LINE_SET)) {
            /******* Handle non-indexed non-striped GeometryArray now *****/
            if (ignoreVertexColors || (carray != null) ||
                    ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0 && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }

            if (useInterleavedArrays) {
                verts.position(startVertex);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if (EXTRA_DEBUGGING) {
                    System.err.println("  startVertex: " + startVertex);
                    System.err.println("  stride: " + stride);
                    System.err.println("  bstride: " + bstride);
                    System.err.println("  normoff: " + normoff);
                    System.err.println("  coloroff: " + coloroff);
                    System.err.println("  coordoff: " + coordoff);
                    System.err.println("  texCoordoff: " + texCoordoff);
                }
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(startVertex + normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }
                if (!ignoreVertexColors && (vformat & GeometryArray.COLOR) != 0) {
                    clrs.position(startClrs);
                    if ((vformat & GeometryArray.WITH_ALPHA) != 0 || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, cbstride, clrs);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, cbstride, clrs);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(startVertex + coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }

                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetMapOffset,
                            numActiveTexUnitState,
                            verts, gl);
                }

                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = startVertex + vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }
            }
            switch (geo_type){
                case GeometryRetained.GEO_TYPE_QUAD_SET : gl.glDrawArrays(GL2.GL_QUADS,     0, vcount); break;
                case GeometryRetained.GEO_TYPE_TRI_SET  : gl.glDrawArrays(GL.GL_TRIANGLES, 0, vcount); break;
                case GeometryRetained.GEO_TYPE_POINT_SET: gl.glDrawArrays(GL.GL_POINTS,    0, vcount); break;
                case GeometryRetained.GEO_TYPE_LINE_SET : gl.glDrawArrays(GL.GL_LINES,     0, vcount); break;
            }
        }

        /* clean up if we turned on normalize */
        if (isNonUniformScale) {
            gl.glDisable(GL2.GL_NORMALIZE);
        }

        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            resetVertexAttrs(gl, ctx, vertexAttrCount);
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            resetTexture(gl, ctx);
        }
    }


    // glLockArrays() is invoked only for indexed geometry, and the
    // vertexCount is guarenteed to be >= 0.
    private void lockArray(GL2 gl, int vertexCount) {
        if (gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")) {
            gl.glLockArraysEXT(0, vertexCount);
        }
    }

    private void unlockArray(GL2 gl) {
        if (gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")) {
            gl.glUnlockArraysEXT();
        }
    }

    private void
            executeGeometryArrayVA(Context absCtx,
            GeometryArrayRetained geo,
            int geo_type,
            boolean isNonUniformScale,
            boolean ignoreVertexColors,
            int vcount,
            int vformat,
            int vdefined,
            int initialCoordIndex, FloatBuffer fverts, DoubleBuffer dverts,
            int initialColorIndex, FloatBuffer fclrs, ByteBuffer bclrs,
            int initialNormalIndex, FloatBuffer norms,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, FloatBuffer[] vertexAttrData,
            int texCoordMapLength,
            int[] texCoordSetMap,
            int numActiveTexUnit,
            int[] texindices, int texStride, FloatBuffer[] texCoords,
            int cdirty,
            int[] sarray,
            int strip_len,
            int[] start_array) {
        JoglContext ctx = (JoglContext) absCtx;
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        // Enable normalize for non-uniform scale (which rescale can't handle)
        if (isNonUniformScale) {
            gl.glEnable(GL2.GL_NORMALIZE);
        }

        int coordoff = 3 * initialCoordIndex;
        // Define the data pointers
        if (floatCoordDefined) {
            fverts.position(coordoff);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, fverts);
        } else if (doubleCoordDefined){
            dverts.position(coordoff);
            gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, dverts);
        }

        if (floatColorsDefined) {
            int coloroff;
            int sz;
            if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                coloroff = 4 * initialColorIndex;
                sz = 4;
            } else {
                coloroff = 3 * initialColorIndex;
                sz = 3;
            }
            fclrs.position(coloroff);
            gl.glColorPointer(sz, GL.GL_FLOAT, 0, fclrs);
        } else if (byteColorsDefined) {
            int coloroff;
            int sz;
            if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                coloroff = 4 * initialColorIndex;
                sz = 4;
            } else {
                coloroff = 3 * initialColorIndex;
                sz = 3;
            }
            bclrs.position(coloroff);
            gl.glColorPointer(sz, GL.GL_UNSIGNED_BYTE, 0, bclrs);
        }
        if (normalsDefined) {
            int normoff = 3 * initialNormalIndex;
            norms.position(normoff);
            gl.glNormalPointer(GL.GL_FLOAT, 0, norms);
        }

        if (vattrDefined) {
            for (int i = 0; i < vertexAttrCount; i++) {
                FloatBuffer vertexAttrs = vertexAttrData[i];
                int sz = vertexAttrSizes[i];
                int initIdx = vertexAttrIndices[i];
                ctx.enableVertexAttrArray(gl, i);
                vertexAttrs.position(initIdx * sz);
                ctx.vertexAttrPointer(gl, i, sz, GL.GL_FLOAT, 0, vertexAttrs);
            }
        }

        if (textureDefined) {
            int texSet = 0;
            for (int i = 0; i < numActiveTexUnit; i++) {
                if (( i < texCoordMapLength) &&
                        ((texSet = texCoordSetMap[i]) != -1)) {
                    FloatBuffer buf = texCoords[texSet];
                    buf.position(texStride * texindices[texSet]);
                    enableTexCoordPointer(gl, i, texStride,
                            GL.GL_FLOAT, 0, buf);
                } else {
                    disableTexCoordPointer(gl, i);
                }
            }

            // Reset client active texture unit to 0
            clientActiveTextureUnit(gl, 0);
        }

        if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
            int primType = 0;
            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_TRI_STRIP_SET:
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_TRI_FAN_SET:
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
                    primType = GL.GL_LINE_STRIP;
                    break;
            }
            if (gl.isExtensionAvailable("GL_EXT_multi_draw_arrays")) {
                gl.glMultiDrawArrays(primType, start_array, 0, sarray, 0, strip_len);
            } else if (gl.isExtensionAvailable("GL_VERSION_1_4")) {
                gl.glMultiDrawArrays(primType, start_array, 0, sarray, 0, strip_len);
            } else {
                for (int i = 0; i < strip_len; i++) {
                    gl.glDrawArrays(primType, start_array[i], sarray[i]);
                }
            }
        } else {
            switch (geo_type){
                case GeometryRetained.GEO_TYPE_QUAD_SET  : gl.glDrawArrays(GL2.GL_QUADS, 0, vcount);     break;
                case GeometryRetained.GEO_TYPE_TRI_SET   : gl.glDrawArrays(GL.GL_TRIANGLES, 0, vcount); break;
                case GeometryRetained.GEO_TYPE_POINT_SET : gl.glDrawArrays(GL.GL_POINTS, 0, vcount);    break;
                case GeometryRetained.GEO_TYPE_LINE_SET  : gl.glDrawArrays(GL.GL_LINES, 0, vcount);     break;
            }
        }

        // clean up if we turned on normalize
        if (isNonUniformScale) {
            gl.glDisable(GL2.GL_NORMALIZE);
        }

        if (vattrDefined) {
            resetVertexAttrs(gl, ctx, vertexAttrCount);
        }

        if (textureDefined) {
            resetTexture(gl, ctx);
        }
    }

    private String getVertexDescription(int vformat) {
        String res = "";
        if ((vformat & GeometryArray.COORDINATES)          != 0) res += "COORDINATES ";
        if ((vformat & GeometryArray.NORMALS)              != 0) res += "NORMALS ";
        if ((vformat & GeometryArray.COLOR)                != 0) res += "COLOR ";
        if ((vformat & GeometryArray.WITH_ALPHA)           != 0) res += "(WITH_ALPHA) ";
        if ((vformat & GeometryArray.TEXTURE_COORDINATE)   != 0) res += "TEXTURE_COORDINATE ";
        if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) res += "(2) ";
        if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) res += "(3) ";
        if ((vformat & GeometryArray.TEXTURE_COORDINATE_4) != 0) res += "(4) ";
        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES)    != 0) res += "VERTEX_ATTRIBUTES ";
        return res;
    }

    private String getGeometryDescription(int geo_type) {
        switch (geo_type) {
            case GeometryRetained.GEO_TYPE_TRI_STRIP_SET : return "GEO_TYPE_TRI_STRIP_SET";
            case GeometryRetained.GEO_TYPE_TRI_FAN_SET   : return "GEO_TYPE_TRI_FAN_SET";
            case GeometryRetained.GEO_TYPE_LINE_STRIP_SET: return "GEO_TYPE_LINE_STRIP_SET";
            case GeometryRetained.GEO_TYPE_QUAD_SET      : return "GEO_TYPE_QUAD_SET";
            case GeometryRetained.GEO_TYPE_TRI_SET       : return "GEO_TYPE_TRI_SET";
            case GeometryRetained.GEO_TYPE_POINT_SET     : return "GEO_TYPE_POINT_SET";
            case GeometryRetained.GEO_TYPE_LINE_SET      : return "GEO_TYPE_LINE_SET";
            default: return "(unknown " + geo_type + ")";
        }
    }

    private void resetVertexAttrs(GL gl, JoglContext ctx, int vertexAttrCount) {
        // Disable specified vertex attr arrays
        for (int i = 0; i < vertexAttrCount; i++) {
            ctx.disableVertexAttrArray(gl, i);
        }
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
            float[] varray, float[] carray,
            int cdirty,
            int[] indexCoord) {
        if (VERBOSE) System.err.println("JoglPipeline.executeIndexedGeometry()");

        executeIndexedGeometryArray(ctx, geo, geo_type,
                isNonUniformScale, useAlpha, ignoreVertexColors,
                initialIndexIndex, indexCount,
                vertexCount, vformat,
                vertexAttrCount, vertexAttrSizes,
                texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                texCoordSetOffset,
                numActiveTexUnitState,
                varray, null, carray,
                cdirty, indexCoord);
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
            FloatBuffer vdata, float[] carray,
            int cDirty,
            int[] indexCoord) {
        if (VERBOSE) System.err.println("JoglPipeline.executeIndexedGeometryBuffer()");

        executeIndexedGeometryArray(ctx, geo, geo_type,
                isNonUniformScale, useAlpha, ignoreVertexColors,
                initialIndexIndex, indexCount, vertexCount, vformat,
                0, null,
                texCoordSetCount, texCoordSetMap, texCoordSetMapLen, texCoordSetOffset,
                numActiveTexUnitState,
                null, vdata, carray,
                cDirty, indexCoord);
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
            int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texStride, Object[] texCoords,
            int cdirty,
            int[] indexCoord) {
        if (VERBOSE) System.err.println("JoglPipeline.executeIndexedGeometryVA()");

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        FloatBuffer fverts = null;
        DoubleBuffer dverts = null;
        FloatBuffer fclrs = null;
        ByteBuffer bclrs = null;
        FloatBuffer[] texCoordBufs = null;
        FloatBuffer norms = null;
        FloatBuffer[] vertexAttrBufs = null;

        // Get vertex attribute arrays
        if (vattrDefined) {
            vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
        }

        // get texture arrays
        if (textureDefined) {
            texCoordBufs = getTexCoordSetBuffer(texCoords);
        }

        int[] sarray = null;
        int strip_len = 0;
        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            sarray = ((IndexedGeometryStripArrayRetained) geo).stripIndexCounts;
            strip_len = sarray.length;
        }

        // get coordinate array
        if (floatCoordDefined) {
            fverts = getVertexArrayBuffer(vfcoords);
        } else if (doubleCoordDefined) {
            dverts = getVertexArrayBuffer(vdcoords);
        }

        // get color array
        if (floatColorsDefined) {
            fclrs = getColorArrayBuffer(cfdata);
        } else if (byteColorsDefined) {
            bclrs = getColorArrayBuffer(cbdata);
        }

        // get normal array
        if (normalsDefined) {
            norms = getNormalArrayBuffer(ndata);
        }

        executeIndexedGeometryArrayVA(ctx, geo, geo_type,
                isNonUniformScale, ignoreVertexColors,
                initialIndexIndex, validIndexCount, vertexCount,
                vformat, vdefined,
                fverts, dverts,
                fclrs, bclrs,
                norms,
                vertexAttrCount, vertexAttrSizes, vertexAttrBufs,
                texCoordMapLength, texcoordoffset,
                numActiveTexUnitState,
                texStride, texCoordBufs,
                cdirty, indexCoord,
                sarray, strip_len);
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
            FloatBuffer ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            FloatBuffer[] vertexAttrData,
            int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState,
            int texStride, Object[] texCoords,
            int cdirty,
            int[] indexCoord) {
        if (VERBOSE) System.err.println("JoglPipeline.executeIndexedGeometryVABuffer()");

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        FloatBuffer fverts = null;
        DoubleBuffer dverts = null;
        FloatBuffer fclrs = null;
        ByteBuffer bclrs = null;
        FloatBuffer[] texCoordBufs = null;
        FloatBuffer norms = null;
        FloatBuffer[] vertexAttrBufs = null;

        // Get vertex attribute arrays
        if (vattrDefined) {
            vertexAttrBufs = vertexAttrData;
        }

        // get texture arrays
        if (textureDefined) {
            texCoordBufs = new FloatBuffer[texCoords.length];
            for (int i = 0; i < texCoords.length; i++) {
                texCoordBufs[i] = (FloatBuffer) texCoords[i];
            }
        }

        // get coordinate array
        if (floatCoordDefined) {
            fverts = (FloatBuffer) vcoords;
        } else if (doubleCoordDefined) {
            dverts = (DoubleBuffer) vcoords;
        }

        if (fverts == null && dverts == null) {
            return;
        }

        int[] sarray = null;
        int strip_len = 0;
        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            sarray = ((IndexedGeometryStripArrayRetained) geo).stripIndexCounts;
            strip_len = sarray.length;
        }

        // get color array
        if (floatColorsDefined) {
            if (cfdata != null)
                fclrs = getColorArrayBuffer(cfdata);
            else
                fclrs = (FloatBuffer) cdataBuffer;
        } else if (byteColorsDefined) {
            if (cbdata != null)
                bclrs = getColorArrayBuffer(cbdata);
            else
                bclrs = (ByteBuffer) cdataBuffer;
        }

        // get normal array
        if (normalsDefined) {
            norms = ndata;
        }

        executeIndexedGeometryArrayVA(ctx, geo, geo_type,
                isNonUniformScale, ignoreVertexColors,
                initialIndexIndex, validIndexCount, vertexCount,
                vformat, vdefined,
                fverts, dverts,
                fclrs, bclrs,
                norms,
                vertexAttrCount, vertexAttrSizes, vertexAttrBufs,
                texCoordMapLength, texcoordoffset,
                numActiveTexUnitState,
                texStride, texCoordBufs,
                cdirty, indexCoord,
                sarray, strip_len);
    }

    // by-copy geometry
    @Override
    void buildIndexedGeometry(Context absCtx,
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
        if (VERBOSE) System.err.println("JoglPipeline.buildIndexedGeometry()");

        JoglContext ctx = (JoglContext) absCtx;
		GL2 gl = context(ctx).getGL().getGL2();

        boolean useInterleavedArrays;
        int iaFormat = 0;
        int primType = 0;
        int stride = 0, coordoff = 0, normoff = 0, coloroff = 0, texCoordoff = 0;
        int texSize = 0, texStride = 0;
        int vAttrOff = 0;
        int vAttrStride = 0;
        int bstride = 0, cbstride = 0;
        FloatBuffer verts = null;
        FloatBuffer clrs  = null;
        int[] sarray = null;
        int strip_len = 0;
        boolean useAlpha = false;

        if ((vformat & GeometryArray.COORDINATES) != 0) {
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            stride += 3;
        } else {
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        }

        if ((vformat & GeometryArray.NORMALS) != 0) {
            gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
            stride += 3;
            coordoff += 3;
        } else {
            gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        }

        if ((vformat & GeometryArray.COLOR) != 0) {
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            stride += 4;
            normoff += 4;
            coordoff += 4;
        } else {
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                texSize = 2;
                texStride = 2 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                texSize = 3;
                texStride = 3 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
                texSize = 4;
                texStride = 4 * texCoordSetCount;
            }
            stride += texStride;
            normoff += texStride;
            coloroff += texStride;
            coordoff += texStride;
        }

        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            for (int i = 0; i < vertexAttrCount; i++) {
                vAttrStride += vertexAttrSizes[i];
            }
            stride += vAttrStride;
            normoff += vAttrStride;
            coloroff += vAttrStride;
            coordoff += vAttrStride;
            texCoordoff += vAttrStride;
        }

		bstride = stride * Buffers.SIZEOF_FLOAT;

        // process alpha for geometryArray without alpha
        if (updateAlpha && !ignoreVertexColors) {
            useAlpha = true;
        }

        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            sarray = ((IndexedGeometryStripArrayRetained) geo).stripIndexCounts;
            strip_len = sarray.length;
        }

        // Copy data into NIO array
        verts = getVertexArrayBuffer(varray);

        // Apply normal transform if necessary
        if ((vformat & GeometryArray.NORMALS) != 0 && nxform != null) {
            int off = normoff;
            for (int i = 0; i < vertexCount * 3; i+=3) {
                verts.put(off  , (float) (nxform[0] * varray[off] +
                        nxform[1] * varray[off+1] +
                        nxform[2] * varray[off+2]));
                verts.put(off+1, (float) (nxform[4] * varray[off] +
                        nxform[5] * varray[off+1] +
                        nxform[6] * varray[off+2]));
                verts.put(off+2, (float) (nxform[8] * varray[off] +
                        nxform[9] * varray[off+1] +
                        nxform[10] * varray[off+2]));
                off += stride;
            }
        }

        // Apply coordinate transform if necessary
        if ((vformat & GeometryArray.COORDINATES) != 0 && xform != null) {
            int off = coordoff;
            for (int i = 0; i < vertexCount * 3; i+=3) {
                verts.put(off  , (float) (xform[0] * varray[off] +
                        xform[1] * varray[off+1] +
                        xform[2] * varray[off+2]));
                verts.put(off+1, (float) (xform[4] * varray[off] +
                        xform[5] * varray[off+1] +
                        xform[6] * varray[off+2]));
                verts.put(off+2, (float) (xform[8] * varray[off] +
                        xform[9] * varray[off+1] +
                        xform[10] * varray[off+2]));
                off += stride;
            }
        }

        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            // Note we can use interleaved arrays even if we have a
            // non-null xform since we use the same data layout unlike the
            // C code
            if (ignoreVertexColors ||
                    (((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }

            if (useInterleavedArrays) {
                verts.position(0);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }
                if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
                    verts.position(coloroff);
                    if (((vformat & GeometryArray.WITH_ALPHA) != 0) || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, bstride, verts);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, bstride, verts);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }
                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetMapOffset,
                            texCoordSetMapLen,
                            verts, gl);
                }
                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }
            }

            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
                    primType = GL.GL_LINE_STRIP;
                    break;
            }

            lockArray(gl, vertexCount);

            // Note: using MultiDrawElements is probably more expensive than
            // not in this case due to the need to allocate more temporary
            // direct buffers and slice up the incoming indices array
            int offset = initialIndexIndex;
            IntBuffer indicesBuffer = IntBuffer.wrap(indexCoord);
            for (int i = 0; i < strip_len; i++) {
                indicesBuffer.position(offset);
                int count = sarray[i];
                gl.glDrawElements(primType, count, GL.GL_UNSIGNED_INT, indicesBuffer);
                offset += count;
            }
        } else if ((geo_type == GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_POINT_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_SET)) {
            // Note we can use interleaved arrays even if we have a
            // non-null xform since we use the same data layout unlike the
            // C code
            if (ignoreVertexColors ||
                    (((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }

            if (useInterleavedArrays) {
                verts.position(0);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }

                if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
                    verts.position(coloroff);
                    if (((vformat & GeometryArray.WITH_ALPHA) != 0) || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, bstride, verts);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, bstride, verts);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }
                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetMapOffset,
                            texCoordSetMapLen,
                            verts, gl);
                }
                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }

                switch (geo_type) {
                    case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET :
                        primType = GL2.GL_QUADS;
                        break;
                    case GeometryRetained.GEO_TYPE_INDEXED_TRI_SET :
                        primType = GL.GL_TRIANGLES;
                        break;
                    case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET :
                        primType = GL.GL_POINTS;
                        break;
                    case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET :
                        primType = GL.GL_LINES;
                        break;
                }

                lockArray(gl, vertexCount);

                IntBuffer indicesBuffer = IntBuffer.wrap(indexCoord);
                indicesBuffer.position(initialIndexIndex);
                gl.glDrawElements(primType, validIndexCount, GL.GL_UNSIGNED_INT, indicesBuffer);
            }
        }

        unlockArray(gl);

        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            resetVertexAttrs(gl, ctx, vertexAttrCount);
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            resetTexture(gl, ctx);
        }
    }


    //----------------------------------------------------------------------
    //
    // Helper routines for IndexedGeometryArrayRetained
    //

    private void executeIndexedGeometryArray(Context absCtx,
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
            float[] varray, FloatBuffer vdata, float[] carray,
            int cDirty,
            int[] indexCoord) {
        JoglContext ctx = (JoglContext) absCtx;
		GL2 gl = context(ctx).getGL().getGL2();

        boolean useInterleavedArrays;
        int iaFormat = 0;
        int primType = 0;
        int stride = 0, coordoff = 0, normoff = 0, coloroff = 0, texCoordoff = 0;
        int texSize = 0, texStride = 0;
        int vAttrOff = 0;
        int vAttrStride = 0;
        int bstride = 0, cbstride = 0;
        FloatBuffer verts = null;
        FloatBuffer clrs  = null;
        int[] sarray = null;
        int strip_len = 0;

        if ((vformat & GeometryArray.COORDINATES) != 0) {
            stride += 3;
        }
        if ((vformat & GeometryArray.NORMALS) != 0) {
            stride += 3;
            coordoff += 3;
        }

        if ((vformat & GeometryArray.COLOR) != 0) {
            if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                stride += 4;
                normoff += 4;
                coordoff += 4;
            } else { // Handle the case of executeInterleaved 3f
                stride += 3;
                normoff += 3;
                coordoff += 3;
            }
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            if ((vformat & GeometryArray.TEXTURE_COORDINATE_2) != 0) {
                texSize = 2;
                texStride = 2 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_3) != 0) {
                texSize = 3;
                texStride = 3 * texCoordSetCount;
            } else if ((vformat & GeometryArray.TEXTURE_COORDINATE_4) != 0) {
                texSize = 4;
                texStride = 4 * texCoordSetCount;
            }
            stride += texStride;
            normoff += texStride;
            coloroff += texStride;
            coordoff += texStride;
        }

        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            for (int i = 0; i < vertexAttrCount; i++) {
                vAttrStride += vertexAttrSizes[i];
            }
            stride += vAttrStride;
            normoff += vAttrStride;
            coloroff += vAttrStride;
            coordoff += vAttrStride;
            texCoordoff += vAttrStride;
        }

		bstride = stride * Buffers.SIZEOF_FLOAT;

        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            sarray = ((IndexedGeometryStripArrayRetained) geo).stripIndexCounts;
            strip_len = sarray.length;
        }

        // We have to copy if the data isn't specified using NIO
        if (varray != null) {
            verts = getVertexArrayBuffer(varray);
        } else if (vdata != null) {
            verts = vdata;
        } else {
            // This should never happen
            throw new AssertionError("Unable to get vertex pointer");
        }

        // using byRef interleaved array and has a separate pointer, then ..
        int cstride = stride;
        if (carray != null) {
            clrs = getColorArrayBuffer(carray);
            cstride = 4;
        } else {
            // FIXME: need to "auto-slice" this buffer later
            clrs = verts;
        }

		cbstride = cstride * Buffers.SIZEOF_FLOAT;

        // Enable normalize for non-uniform scale (which rescale can't handle)
        if (isNonUniformScale) {
            gl.glEnable(GL2.GL_NORMALIZE);
        }

        /*** Handle non-indexed strip GeometryArray first *******/
        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            if (ignoreVertexColors || (carray != null) ||
                    ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0 && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }
            if (useInterleavedArrays) {
                verts.position(0);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }
                if (!ignoreVertexColors && (vformat & GeometryArray.COLOR) != 0) {
                    if (clrs == verts) {
                        clrs.position(coloroff);
                    }
                    if ((vformat & GeometryArray.WITH_ALPHA) != 0 || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, cbstride, clrs);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, cbstride, clrs);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }

                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    /* XXXX: texCoordoff == 0 ???*/
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetOffset,
                            numActiveTexUnitState,
                            verts, gl);
                }

                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }
            }

            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
                    primType = GL.GL_LINE_STRIP;
                    break;
            }

            lockArray(gl, vertexCount);

            // Note: using MultiDrawElements is probably more expensive than
            // not in this case due to the need to allocate more temporary
            // direct buffers and slice up the incoming indices array
            int offset = initialIndexIndex;
            IntBuffer indicesBuffer = IntBuffer.wrap(indexCoord);
            for (int i = 0; i < strip_len; i++) {
                indicesBuffer.position(offset);
                int count = sarray[i];
                gl.glDrawElements(primType, count, GL.GL_UNSIGNED_INT, indicesBuffer);
                offset += count;
            }
        } else if ((geo_type == GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_POINT_SET) ||
                (geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_SET)) {
            /******* Handle non-indexed non-striped GeometryArray now *****/
            if (ignoreVertexColors || (carray != null) ||
                    ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0 && ((texCoordSetMapLen > 1) ||
                    (texCoordSetCount > 1)))) {
                useInterleavedArrays = false;
            } else {
                boolean[] tmp = new boolean[1];
                int[] tmp2 = new int[1];
                testForInterleavedArrays(vformat, tmp, tmp2);
                useInterleavedArrays = tmp[0];
                iaFormat = tmp2[0];
            }

            if (useInterleavedArrays) {
                verts.position(0);
                gl.glInterleavedArrays(iaFormat, bstride, verts);
            } else {
                if ((vformat & GeometryArray.NORMALS) != 0) {
                    verts.position(normoff);
                    gl.glNormalPointer(GL.GL_FLOAT, bstride, verts);
                }

                if (!ignoreVertexColors && (vformat & GeometryArray.COLOR) != 0) {
                    if (clrs == verts) {
                        clrs.position(coloroff);
                    }
                    if ((vformat & GeometryArray.WITH_ALPHA) != 0 || useAlpha) {
                        gl.glColorPointer(4, GL.GL_FLOAT, cbstride, clrs);
                    } else {
                        gl.glColorPointer(3, GL.GL_FLOAT, cbstride, clrs);
                    }
                }
                if ((vformat & GeometryArray.COORDINATES) != 0) {
                    verts.position(coordoff);
                    gl.glVertexPointer(3, GL.GL_FLOAT, bstride, verts);
                }

                if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
                    /* XXXX: texCoordoff == 0 ???*/
                    executeTexture(texCoordSetMapLen,
                            texSize, bstride, texCoordoff,
                            texCoordSetOffset,
                            numActiveTexUnitState,
                            verts, gl);
                }

                if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
                    int vAttrOffset = vAttrOff;
                    for (int i = 0; i < vertexAttrCount; i++) {
                        ctx.enableVertexAttrArray(gl, i);
                        verts.position(vAttrOffset);
                        ctx.vertexAttrPointer(gl, i, vertexAttrSizes[i],
                                GL.GL_FLOAT, bstride, verts);
                        vAttrOffset += vertexAttrSizes[i];
                    }
                }
            }

            lockArray(gl, vertexCount);
            IntBuffer buf = IntBuffer.wrap(indexCoord);
            buf.position(initialIndexIndex);
            switch (geo_type){
                case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET : gl.glDrawElements(GL2.GL_QUADS,     indexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_SET  : gl.glDrawElements(GL.GL_TRIANGLES, indexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET: gl.glDrawElements(GL.GL_POINTS,    indexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET : gl.glDrawElements(GL.GL_LINES,     indexCount, GL.GL_UNSIGNED_INT, buf); break;
            }
        }

        unlockArray(gl);

        if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
            resetVertexAttrs(gl, ctx, vertexAttrCount);
        }

        if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
            resetTexture(gl, ctx);
        }

        // clean up if we turned on normalize
        if (isNonUniformScale) {
            gl.glDisable(GL2.GL_NORMALIZE);
        }
    }


    private void executeIndexedGeometryArrayVA(Context absCtx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount, int vformat, int vdefined,
            FloatBuffer fverts, DoubleBuffer dverts,
            FloatBuffer fclrs, ByteBuffer bclrs,
            FloatBuffer norms,
            int vertexAttrCount, int[] vertexAttrSizes, FloatBuffer[] vertexAttrBufs,
            int texCoordSetCount, int[] texCoordSetMap,
            int numActiveTexUnitState,
            int texStride,
            FloatBuffer[] texCoords,
            int cDirty, int[] indexCoord, int[] sarray, int strip_len) {
        JoglContext ctx = (JoglContext) absCtx;
		GL2 gl = context(ctx).getGL().getGL2();

        boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
        boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
        boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
        boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
        boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
        boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
        boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

        // Enable normalize for non-uniform scale (which rescale can't handle)
        if (isNonUniformScale) {
            gl.glEnable(GL2.GL_NORMALIZE);
        }

        // Define the data pointers
        if (floatCoordDefined) {
            fverts.position(0);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, fverts);
        } else if (doubleCoordDefined){
            dverts.position(0);
            gl.glVertexPointer(3, GL2.GL_DOUBLE, 0, dverts);
        }
        if (floatColorsDefined) {
            fclrs.position(0);
            if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                gl.glColorPointer(4, GL.GL_FLOAT, 0, fclrs);
            } else {
                gl.glColorPointer(3, GL.GL_FLOAT, 0, fclrs);
            }
        } else if (byteColorsDefined) {
            bclrs.position(0);
            if ((vformat & GeometryArray.WITH_ALPHA) != 0) {
                gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, bclrs);
            } else {
                gl.glColorPointer(3, GL.GL_UNSIGNED_BYTE, 0, bclrs);
            }
        }
        if (normalsDefined) {
            norms.position(0);
            gl.glNormalPointer(GL.GL_FLOAT, 0, norms);
        }

        if (vattrDefined) {
            for (int i = 0; i < vertexAttrCount; i++) {
                FloatBuffer vertexAttrs = vertexAttrBufs[i];
                int sz = vertexAttrSizes[i];
                ctx.enableVertexAttrArray(gl, i);
                vertexAttrs.position(0);
                ctx.vertexAttrPointer(gl, i, sz, GL.GL_FLOAT, 0, vertexAttrs);
            }
        }

        if (textureDefined) {
            int texSet = 0;
            for (int i = 0; i < numActiveTexUnitState; i++) {
                if ((i < texCoordSetCount) &&
                        ((texSet = texCoordSetMap[i]) != -1)) {
                    FloatBuffer buf = texCoords[texSet];
                    buf.position(0);
                    enableTexCoordPointer(gl, i, texStride,
                            GL.GL_FLOAT, 0, buf);
                } else {
                    disableTexCoordPointer(gl, i);
                }
            }

            // Reset client active texture unit to 0
            clientActiveTextureUnit(gl, 0);
        }

        lockArray(gl, vertexCount);

        if (geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET   ||
                geo_type == GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET) {
            int primType = 0;
            switch (geo_type) {
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_STRIP_SET:
                    primType = GL.GL_TRIANGLE_STRIP;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_FAN_SET:
                    primType = GL.GL_TRIANGLE_FAN;
                    break;
                case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
                    primType = GL.GL_LINE_STRIP;
                    break;
            }

            // Note: using MultiDrawElements is probably more expensive than
            // not in this case due to the need to allocate more temporary
            // direct buffers and slice up the incoming indices array
            int offset = initialIndexIndex;
            IntBuffer indicesBuffer = IntBuffer.wrap(indexCoord);
            for (int i = 0; i < strip_len; i++) {
                indicesBuffer.position(offset);
                int count = sarray[i];
                gl.glDrawElements(primType, count, GL.GL_UNSIGNED_INT, indicesBuffer);
                offset += count;
            }
        } else {
            IntBuffer buf = IntBuffer.wrap(indexCoord);
            buf.position(initialIndexIndex);
            switch (geo_type){
                case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET : gl.glDrawElements(GL2.GL_QUADS,     validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_TRI_SET  : gl.glDrawElements(GL.GL_TRIANGLES, validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET: gl.glDrawElements(GL.GL_POINTS,    validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
                case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET : gl.glDrawElements(GL.GL_LINES,     validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
            }
        }

        unlockArray(gl);

        // clean up if we turned on normalize
        if (isNonUniformScale) {
            gl.glDisable(GL2.GL_NORMALIZE);
        }

        if (vattrDefined) {
            resetVertexAttrs(gl, ctx, vertexAttrCount);
        }

        if (textureDefined) {
            resetTexture(gl, ctx);
        }
    }


    // ---------------------------------------------------------------------

    //
    // GraphicsContext3D methods
    //

    // Native method for readRaster
    @Override
    void readRaster(Context ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas,
            int imageDataType,
            int imageFormat,
            Object imageBuffer,
            int depthFormat,
            Object depthBuffer) {

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glPixelStorei(GL2.GL_PACK_ROW_LENGTH, width);
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
        int yAdjusted = hCanvas - height - ySrcOffset;

        if ((type & Raster.RASTER_COLOR) != 0) {
            int oglFormat = 0;
            if(imageDataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) {

                switch (imageFormat) {
                    case ImageComponentRetained.TYPE_BYTE_BGR:
                        oglFormat = GL2.GL_BGR;
                        break;
                    case ImageComponentRetained.TYPE_BYTE_RGB:
                        oglFormat = GL.GL_RGB;
                        break;
                    case ImageComponentRetained.TYPE_BYTE_ABGR:
                        if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
                            oglFormat = GL2.GL_ABGR_EXT;
                        } else {
                            assert false;
                            return;
                        }
                        break;
                    case ImageComponentRetained.TYPE_BYTE_RGBA:
                        // all RGB types are stored as RGBA
                        oglFormat = GL.GL_RGBA;
                        break;
                    case ImageComponentRetained.TYPE_BYTE_LA:
                        // all LA types are stored as LA8
                        oglFormat = GL.GL_LUMINANCE_ALPHA;
                        break;
                    case ImageComponentRetained.TYPE_BYTE_GRAY:
                    case ImageComponentRetained.TYPE_USHORT_GRAY:
                    case ImageComponentRetained.TYPE_INT_BGR:
                    case ImageComponentRetained.TYPE_INT_RGB:
                    case ImageComponentRetained.TYPE_INT_ARGB:
                    default:
                        assert false;
                        return;
                }

                gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                        oglFormat, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[]) imageBuffer));


            } else if(imageDataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) {
                int intType = GL2.GL_UNSIGNED_INT_8_8_8_8;
                boolean forceAlphaToOne = false;

                switch (imageFormat) {
                    /* GL_BGR */
                    case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                        oglFormat = GL.GL_RGBA;
                        intType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                        forceAlphaToOne = true;
                        break;
                    case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                        forceAlphaToOne = true;
                        /* Fall through to next case */
                    case ImageComponentRetained.TYPE_INT_ARGB:
                        oglFormat = GL2.GL_BGRA;
                        intType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                        break;
                        /* This method only supports 3 and 4 components formats and INT types. */
                    case ImageComponentRetained.TYPE_BYTE_LA:
                    case ImageComponentRetained.TYPE_BYTE_GRAY:
                    case ImageComponentRetained.TYPE_USHORT_GRAY:
                    case ImageComponentRetained.TYPE_BYTE_BGR:
                    case ImageComponentRetained.TYPE_BYTE_RGB:
                    case ImageComponentRetained.TYPE_BYTE_RGBA:
                    case ImageComponentRetained.TYPE_BYTE_ABGR:
                    default:
                        assert false;
                        return;
                }

                /* Force Alpha to 1.0 if needed */
                if(forceAlphaToOne) {
                    gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                    gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
                }

                gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                        oglFormat, intType, IntBuffer.wrap((int[]) imageBuffer));

                /* Restore Alpha scale and bias */
                if(forceAlphaToOne) {
                    gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
                    gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
                }

            } else {
                assert false;
            }
        }

        if ((type & Raster.RASTER_DEPTH) != 0) {

            if (depthFormat == DepthComponentRetained.DEPTH_COMPONENT_TYPE_INT) {
                // yOffset is adjusted for OpenGL - Y upward
                gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                        GL2.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_INT, IntBuffer.wrap((int[]) depthBuffer));
            } else {
                // DEPTH_COMPONENT_TYPE_FLOAT
                // yOffset is adjusted for OpenGL - Y upward
                gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                        GL2.GL_DEPTH_COMPONENT, GL.GL_FLOAT, FloatBuffer.wrap((float[]) depthBuffer));
            }
        }

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
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1i()");
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform1iARB(unbox(uniformLocation), value);
        return null;
    }

    @Override
    ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1f()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform1fARB(unbox(uniformLocation), value);
        return null;
    }

    @Override
    ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2i()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform2iARB(unbox(uniformLocation), value[0], value[1]);
        return null;
    }

    @Override
    ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2f()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform2fARB(unbox(uniformLocation), value[0], value[1]);
        return null;
    }

    @Override
    ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3i()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform3iARB(unbox(uniformLocation), value[0], value[1], value[2]);
        return null;
    }

    @Override
    ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3f()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform3fARB(unbox(uniformLocation), value[0], value[1], value[2]);
        return null;
    }

    @Override
    ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4i()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform4iARB(unbox(uniformLocation), value[0], value[1], value[2], value[3]);
        return null;
    }

    @Override
    ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4f()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform4fARB(unbox(uniformLocation), value[0], value[1], value[2], value[3]);
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix3f()");

        // Load attribute
        // transpose is true : each matrix is supplied in row major order
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniformMatrix3fvARB(unbox(uniformLocation), 1, true, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix4f()");

        // Load attribute
        // transpose is true : each matrix is supplied in row major order
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniformMatrix4fvARB(unbox(uniformLocation), 1, true, value, 0);
        return null;
    }

    // ShaderAttributeArray methods

    @Override
    ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1iArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform1ivARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1fArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform1fvARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2iArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform2ivARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2fArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform2fvARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3iArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform3ivARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3fArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform3fvARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4iArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform4ivARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4fArray()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniform4fvARB(unbox(uniformLocation), numElements, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix3fArray()");

        // Load attribute
        // transpose is true : each matrix is supplied in row major order
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniformMatrix3fvARB(unbox(uniformLocation), numElements, true, value, 0);
        return null;
    }

    @Override
    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
        if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix4fArray()");

        // Load attribute
        // transpose is true : each matrix is supplied in row major order
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUniformMatrix4fvARB(unbox(uniformLocation), numElements, true, value, 0);
        return null;
    }

    // interfaces for shader compilation, etc.
    @Override
    ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId) {
        if (VERBOSE) System.err.println("JoglPipeline.createGLSLShader()");

		GL2 gl = context(ctx).getGL().getGL2();

        int shaderHandle = 0;
        if (shaderType == Shader.SHADER_TYPE_VERTEX) {
            shaderHandle = gl.glCreateShaderObjectARB(GL2.GL_VERTEX_SHADER);
        } else if (shaderType == Shader.SHADER_TYPE_FRAGMENT) {
            shaderHandle = gl.glCreateShaderObjectARB(GL2.GL_FRAGMENT_SHADER);
        }

        if (shaderHandle == 0) {
            return new ShaderError(ShaderError.COMPILE_ERROR,
                    "Unable to create native shader object");
        }

        shaderId[0] = new JoglShaderObject(shaderHandle);
        return null;
    }
    @Override
    ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId) {
        if (VERBOSE) System.err.println("JoglPipeline.destroyGLSLShader()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glDeleteObjectARB(unbox(shaderId));
        return null;
    }
    @Override
    ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program) {
        if (VERBOSE) System.err.println("JoglPipeline.compileGLSLShader()");

        int id = unbox(shaderId);
        if (id == 0) {
            throw new AssertionError("shaderId == 0");
        }

        if (program == null) {
            throw new AssertionError("shader program string is null");
        }

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glShaderSourceARB(id, 1, new String[] { program }, null, 0);
        gl.glCompileShaderARB(id);
        int[] status = new int[1];
        gl.glGetObjectParameterivARB(id, GL2.GL_OBJECT_COMPILE_STATUS_ARB, status, 0);
        if (status[0] == 0) {
            String detailMsg = getInfoLog(gl, id);
            ShaderError res = new ShaderError(ShaderError.COMPILE_ERROR,
                    "GLSL shader compile error");
            res.setDetailMessage(detailMsg);
            return res;
        }
        return null;
    }

    @Override
    ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
        if (VERBOSE) System.err.println("JoglPipeline.createGLSLShaderProgram()");

		GL2 gl = context(ctx).getGL().getGL2();

        int shaderProgramHandle = gl.glCreateProgramObjectARB();
        if (shaderProgramHandle == 0) {
            return new ShaderError(ShaderError.LINK_ERROR,
                    "Unable to create native shader program object");
        }
        shaderProgramId[0] = new JoglShaderObject(shaderProgramHandle);
        return null;
    }
    @Override
    ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        if (VERBOSE) System.err.println("JoglPipeline.destroyGLSLShaderProgram()");
		GL2 gl = context(ctx).getGL().getGL2();
		gl.glDeleteObjectARB(unbox(shaderProgramId));
        return null;
    }
    @Override
    ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
        if (VERBOSE) System.err.println("JoglPipeline.linkGLSLShaderProgram()");

		GL2 gl = context(ctx).getGL().getGL2();
        int id = unbox(shaderProgramId);
        for (int i = 0; i < shaderIds.length; i++) {
            gl.glAttachObjectARB(id, unbox(shaderIds[i]));
        }
        gl.glLinkProgramARB(id);
        int[] status = new int[1];
        gl.glGetObjectParameterivARB(id, GL2.GL_OBJECT_LINK_STATUS_ARB, status, 0);
        if (status[0] == 0) {
            String detailMsg = getInfoLog(gl, id);
            ShaderError res = new ShaderError(ShaderError.LINK_ERROR,
                    "GLSL shader program link error");
            res.setDetailMessage(detailMsg);
            return res;
        }
        return null;
    }
    @Override
    ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex) {
        if (VERBOSE) System.err.println("JoglPipeline.bindGLSLVertexAttrName()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glBindAttribLocation(unbox(shaderProgramId),
                attrIndex + VirtualUniverse.mc.glslVertexAttrOffset,
                attrName);
        return null;
    }
    @Override
    void lookupGLSLShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
        if (VERBOSE) System.err.println("JoglPipeline.lookupGLSLShaderAttrNames()");

        // set the loc, type, and size arrays to out-of-bound values
        for (int i = 0; i < attrNames.length; i++) {
            locArr[i] = null;
            typeArr[i] = -1;
            sizeArr[i] = -1;
        }

        // Loop through the list of active uniform variables, one at a
        // time, searching for a match in the attrNames array.
        //
        // NOTE: Since attrNames isn't sorted, and we don't have a
        // hashtable of names to index locations, we will do a
        // brute-force, linear search of the array. This leads to an
        // O(n^2) algorithm (actually O(n*m) where n is attrNames.length
        // and m is the number of uniform variables), but since we expect
        // N to be small, we will not optimize this at this time.
        int id = unbox(shaderProgramId);
        int[] tmp = new int[1];
        int[] tmp2 = new int[1];
        int[] tmp3 = new int[1];
		GL2 gl = context(ctx).getGL().getGL2();
        gl.glGetObjectParameterivARB(id,
                GL2.GL_OBJECT_ACTIVE_UNIFORMS_ARB,
                tmp, 0);
        int numActiveUniforms = tmp[0];
        gl.glGetObjectParameterivARB(id,
                GL2.GL_OBJECT_ACTIVE_UNIFORM_MAX_LENGTH_ARB,
                tmp, 0);
        int maxStrLen = tmp[0];
        byte[] nameBuf = new byte[maxStrLen];

        for (int i = 0; i < numActiveUniforms; i++) {
            gl.glGetActiveUniformARB(id, i, maxStrLen, tmp3, 0,
                    tmp, 0,
                    tmp2, 0,
                    nameBuf, 0);
            int size = tmp[0];
            int type = tmp2[0];
            String name = null;
            try {
                // TODO KCR : Shouldn't this use the default locale?
                name = new String(nameBuf, 0, tmp3[0], "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            // Issue 247 - we need to workaround an ATI bug where they erroneously
            // report individual elements of arrays rather than the array itself
            if (name.length() >= 3 && name.endsWith("]")) {
                if (name.endsWith("[0]")) {
                    name = name.substring(0, name.length() - 3);
                } else {
                    // Ignore this name
                    continue;
                }
            }

            // Now try to find the name
            for (int j = 0; j < numAttrNames; j++) {
                if (name.equals(attrNames[j])) {
                    sizeArr[j] = size;
                    isArrayArr[j] = (size > 1);
                    typeArr[j] = glslToJ3dType(type);
                    break;
                }
            }
        }

        // Now lookup the location of each name in the attrNames array
        for (int i = 0; i < numAttrNames; i++) {
            // Get uniform attribute location
            int loc = gl.glGetUniformLocationARB(id, attrNames[i]);
            locArr[i] = new JoglShaderObject(loc);
        }
    }

    @Override
    ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
        if (VERBOSE) System.err.println("JoglPipeline.useGLSLShaderProgram()");

		GL2 gl = context(ctx).getGL().getGL2();
		gl.glUseProgramObjectARB(unbox(shaderProgramId));
        ((JoglContext) ctx).setShaderProgram((JoglShaderObject) shaderProgramId);
        return null;
    }

    //----------------------------------------------------------------------
    // Helper methods for above shader routines
    //
    private int unbox(ShaderAttrLoc loc) {
        if (loc == null)
            return 0;
        return ((JoglShaderObject) loc).getValue();
    }

    private int unbox(ShaderProgramId id) {
        if (id == null)
            return 0;
        return ((JoglShaderObject) id).getValue();
    }

    private int unbox(ShaderId id) {
        if (id == null)
            return 0;
        return ((JoglShaderObject) id).getValue();
    }

    private String getInfoLog(GL2 gl, int id) {
        int[] infoLogLength = new int[1];
        gl.glGetObjectParameterivARB(id, GL2.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0);
        if (infoLogLength[0] > 0) {
            byte[] storage = new byte[infoLogLength[0]];
            int[] len = new int[1];
            gl.glGetInfoLogARB(id, infoLogLength[0], len, 0, storage, 0);
            try {
                // TODO KCR : Shouldn't this use the default locale?
                return new String(storage, 0, len[0], "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private int glslToJ3dType(int type) {
        switch (type) {
            case GL2.GL_BOOL_ARB:
            case GL2.GL_INT:
            case GL2.GL_SAMPLER_2D_ARB:
            case GL2.GL_SAMPLER_3D_ARB:
            case GL2.GL_SAMPLER_CUBE_ARB:
                return ShaderAttributeObjectRetained.TYPE_INTEGER;

            case GL.GL_FLOAT:
                return ShaderAttributeObjectRetained.TYPE_FLOAT;

            case GL2.GL_INT_VEC2_ARB:
            case GL2.GL_BOOL_VEC2_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE2I;

            case GL2.GL_FLOAT_VEC2_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE2F;

            case GL2.GL_INT_VEC3_ARB:
            case GL2.GL_BOOL_VEC3_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE3I;

            case GL2.GL_FLOAT_VEC3_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE3F;

            case GL2.GL_INT_VEC4_ARB:
            case GL2.GL_BOOL_VEC4_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE4I;

            case GL2.GL_FLOAT_VEC4_ARB:
                return ShaderAttributeObjectRetained.TYPE_TUPLE4F;

                // case GL.GL_FLOAT_MAT2_ARB:

            case GL2.GL_FLOAT_MAT3_ARB:
                return ShaderAttributeObjectRetained.TYPE_MATRIX3F;

            case GL2.GL_FLOAT_MAT4_ARB:
                return ShaderAttributeObjectRetained.TYPE_MATRIX4F;

                // Java 3D does not support the following sampler types:
                //
                // case GL.GL_SAMPLER_1D_ARB:
                // case GL.GL_SAMPLER_1D_SHADOW_ARB:
                // case GL.GL_SAMPLER_2D_SHADOW_ARB:
                // case GL.GL_SAMPLER_2D_RECT_ARB:
                // case GL.GL_SAMPLER_2D_RECT_SHADOW_ARB:
        }

        return -1;
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
            boolean lightEnable,
            int shadeModel) {
        if (VERBOSE) System.err.println("JoglPipeline.updateColoringAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        float cr, cg, cb;

        if (lightEnable) {
            cr = dRed; cg = dGreen; cb = dBlue;
        } else {
            cr = red; cg = green; cb = blue;
        }
        gl.glColor4f(cr, cg, cb, alpha);
        if (shadeModel == ColoringAttributes.SHADE_FLAT) {
            gl.glShadeModel(GL2.GL_FLAT);
        } else {
            gl.glShadeModel(GL2.GL_SMOOTH);
        }
    }


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    private static final float[] black = new float[4];
    @Override
    void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float dirx, float diry, float dirz) {
        if (VERBOSE) System.err.println("JoglPipeline.updateDirectionalLight()");

		GL2 gl = context(ctx).getGL().getGL2();

        int lightNum = GL2.GL_LIGHT0 + lightSlot;
        float[] values = new float[4];

        values[0] = red;
        values[1] = green;
        values[2] = blue;
        values[3] = 1.0f;
        gl.glLightfv(lightNum, GL2.GL_DIFFUSE, values, 0);
        gl.glLightfv(lightNum, GL2.GL_SPECULAR, values, 0);
        values[0] = -dirx;
        values[1] = -diry;
        values[2] = -dirz;
        values[3] = 0.0f;
        gl.glLightfv(lightNum, GL2.GL_POSITION, values, 0);
        gl.glLightfv(lightNum, GL2.GL_AMBIENT, black, 0);
        gl.glLightf(lightNum, GL2.GL_CONSTANT_ATTENUATION, 1.0f);
        gl.glLightf(lightNum, GL2.GL_LINEAR_ATTENUATION, 0.0f);
        gl.glLightf(lightNum, GL2.GL_QUADRATIC_ATTENUATION, 0.0f);
        gl.glLightf(lightNum, GL2.GL_SPOT_EXPONENT, 0.0f);
        gl.glLightf(lightNum, GL2.GL_SPOT_CUTOFF, 180.0f);
    }


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    @Override
    void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float attenx, float atteny, float attenz,
            float posx, float posy, float posz) {
        if (VERBOSE) System.err.println("JoglPipeline.updatePointLight()");

		GL2 gl = context(ctx).getGL().getGL2();

        int lightNum = GL2.GL_LIGHT0 + lightSlot;
        float[] values = new float[4];

        values[0] = red;
        values[1] = green;
        values[2] = blue;
        values[3] = 1.0f;
        gl.glLightfv(lightNum, GL2.GL_DIFFUSE, values, 0);
        gl.glLightfv(lightNum, GL2.GL_SPECULAR, values, 0);
        gl.glLightfv(lightNum, GL2.GL_AMBIENT, black, 0);
        values[0] = posx;
        values[1] = posy;
        values[2] = posz;
        gl.glLightfv(lightNum, GL2.GL_POSITION, values, 0);
        gl.glLightf(lightNum, GL2.GL_CONSTANT_ATTENUATION, attenx);
        gl.glLightf(lightNum, GL2.GL_LINEAR_ATTENUATION, atteny);
        gl.glLightf(lightNum, GL2.GL_QUADRATIC_ATTENUATION, attenz);
        gl.glLightf(lightNum, GL2.GL_SPOT_EXPONENT, 0.0f);
        gl.glLightf(lightNum, GL2.GL_SPOT_CUTOFF, 180.0f);
    }


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    @Override
    void updateSpotLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float attenx, float atteny, float attenz,
            float posx, float posy, float posz, float spreadAngle,
            float concentration, float dirx, float diry,
            float dirz) {
        if (VERBOSE) System.err.println("JoglPipeline.updateSpotLight()");

		GL2 gl = context(ctx).getGL().getGL2();

        int lightNum = GL2.GL_LIGHT0 + lightSlot;
        float[] values = new float[4];

        values[0] = red;
        values[1] = green;
        values[2] = blue;
        values[3] = 1.0f;
        gl.glLightfv(lightNum, GL2.GL_DIFFUSE, values, 0);
        gl.glLightfv(lightNum, GL2.GL_SPECULAR, values, 0);
        gl.glLightfv(lightNum, GL2.GL_AMBIENT, black, 0);
        values[0] = posx;
        values[1] = posy;
        values[2] = posz;
        gl.glLightfv(lightNum, GL2.GL_POSITION, values, 0);
        gl.glLightf(lightNum, GL2.GL_CONSTANT_ATTENUATION, attenx);
        gl.glLightf(lightNum, GL2.GL_LINEAR_ATTENUATION, atteny);
        gl.glLightf(lightNum, GL2.GL_QUADRATIC_ATTENUATION, attenz);
        values[0] = dirx;
        values[1] = diry;
        values[2] = dirz;
        gl.glLightfv(lightNum, GL2.GL_SPOT_DIRECTION, values, 0);
        gl.glLightf(lightNum, GL2.GL_SPOT_EXPONENT, concentration);
        gl.glLightf(lightNum, GL2.GL_SPOT_CUTOFF, (float) (spreadAngle * 180.0f / Math.PI));
    }


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    @Override
    void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density) {
        if (VERBOSE) System.err.println("JoglPipeline.updateExponentialFog()");

		GL2 gl = context(ctx).getGL().getGL2();

        float[] color = new float[3];
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_EXP);
        gl.glFogfv(GL2.GL_FOG_COLOR, color, 0);
        gl.glFogf(GL2.GL_FOG_DENSITY, density);
        gl.glEnable(GL2.GL_FOG);
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    @Override
    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
        if (VERBOSE) System.err.println("JoglPipeline.updateLinearFog()");

		GL2 gl = context(ctx).getGL().getGL2();

        float[] color = new float[3];
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        gl.glFogi(GL2.GL_FOG_MODE, GL.GL_LINEAR);
        gl.glFogfv(GL2.GL_FOG_COLOR, color, 0);
        gl.glFogf(GL2.GL_FOG_START, (float) fdist);
        gl.glFogf(GL2.GL_FOG_END, (float) bdist);
        gl.glEnable(GL2.GL_FOG);
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
        if (VERBOSE) System.err.println("JoglPipeline.updateLineAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glLineWidth(lineWidth);

        if (linePattern == LineAttributes.PATTERN_SOLID) {
            gl.glDisable(GL2.GL_LINE_STIPPLE);
        } else {
            if (linePattern == LineAttributes.PATTERN_DASH) { // dashed lines
                gl.glLineStipple(1, (short) 0x00ff);
            } else if (linePattern == LineAttributes.PATTERN_DOT) { // dotted lines
                gl.glLineStipple(1, (short) 0x0101);
            } else if (linePattern == LineAttributes.PATTERN_DASH_DOT) { // dash-dotted lines
                gl.glLineStipple(1, (short) 0x087f);
            } else if (linePattern == LineAttributes.PATTERN_USER_DEFINED) { // user-defined mask
                gl.glLineStipple(linePatternScaleFactor, (short) linePatternMask);
            }
            gl.glEnable(GL2.GL_LINE_STIPPLE);
        }

        /* XXXX: Polygon Mode check, blend enable */
        if (lineAntialiasing) {
            gl.glEnable(GL.GL_LINE_SMOOTH);
        } else {
            gl.glDisable(GL.GL_LINE_SMOOTH);
        }
    }


    // ---------------------------------------------------------------------

    //
    // MaterialRetained methods
    //

    @Override
    void updateMaterial(Context ctx,
            float red, float green, float blue, float alpha,
            float aRed, float aGreen, float aBlue,
            float eRed, float eGreen, float eBlue,
            float dRed, float dGreen, float dBlue,
            float sRed, float sGreen, float sBlue,
            float shininess, int colorTarget, boolean lightEnable) {
        if (VERBOSE) System.err.println("JoglPipeline.updateMaterial()");

        float[] color = new float[4];

		GL2 gl = context(ctx).getGL().getGL2();

        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
        switch (colorTarget) {
            case Material.DIFFUSE:
                gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE);
                break;
            case Material.AMBIENT:
                gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT);
                break;
            case Material.EMISSIVE:
                gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION);
                break;
            case Material.SPECULAR:
                gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR);
                break;
            case Material.AMBIENT_AND_DIFFUSE:
                gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
                break;
        }

        color[0] = eRed; color[1] = eGreen; color[2] = eBlue;
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_EMISSION, color, 0);

        color[0] = aRed; color[1] = aGreen; color[2] = aBlue;
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, color, 0);

        color[0] = sRed; color[1] = sGreen; color[2] = sBlue;
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, color, 0);

        if (lightEnable) {
            color[0] = dRed; color[1] = dGreen; color[2] = dBlue;
        } else {
            color[0] = red; color[1] = green; color[2] = blue;
        }
        color[3] = alpha;
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, color, 0);
        gl.glColor4f(color[0], color[1], color[2], color[3]);

        if (lightEnable) {
            gl.glEnable(GL2.GL_LIGHTING);
        } else {
            gl.glDisable(GL2.GL_LIGHTING);
        }
    }


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    @Override
    void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D) {
        if (VERBOSE) System.err.println("JoglPipeline.updateModelClip()");

		GL2 gl = context(ctx).getGL().getGL2();

        double[] equation = new double[4];
        int pl = GL2.GL_CLIP_PLANE0 + planeNum;

        // OpenGL clip planes are opposite to J3d clip planes
        if (enableFlag) {
            equation[0] = -A;
            equation[1] = -B;
            equation[2] = -C;
            equation[3] = -D;
            gl.glClipPlane(pl, DoubleBuffer.wrap(equation));
            gl.glEnable(pl);
        } else {
            gl.glDisable(pl);
        }
    }


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    @Override
    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
        if (VERBOSE) System.err.println("JoglPipeline.updatePointAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glPointSize(pointSize);

        // XXXX: Polygon Mode check, blend enable
        if (pointAntialiasing) {
            gl.glEnable(GL2.GL_POINT_SMOOTH);
        } else {
            gl.glDisable(GL2.GL_POINT_SMOOTH);
        }
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
        if (VERBOSE) System.err.println("JoglPipeline.updatePolygonAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (cullFace == PolygonAttributes.CULL_NONE) {
            gl.glDisable(GL.GL_CULL_FACE);
        } else {
            if (cullFace == PolygonAttributes.CULL_BACK) {
                gl.glCullFace(GL.GL_BACK);
            } else {
                gl.glCullFace(GL.GL_FRONT);
            }
            gl.glEnable(GL.GL_CULL_FACE);
        }

        if (backFaceNormalFlip && (cullFace != PolygonAttributes.CULL_BACK)) {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
        } else {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);
        }

        if (polygonMode == PolygonAttributes.POLYGON_POINT) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_POINT);
        } else if (polygonMode == PolygonAttributes.POLYGON_LINE) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        } else {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }

        gl.glPolygonOffset(polygonOffsetFactor, polygonOffset);

        if ((polygonOffsetFactor != 0.0) || (polygonOffset != 0.0)) {
            switch (polygonMode) {
                case PolygonAttributes.POLYGON_POINT:
                    gl.glEnable(GL2.GL_POLYGON_OFFSET_POINT);
                    gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
                    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
                    break;
                case PolygonAttributes.POLYGON_LINE:
                    gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
                    gl.glDisable(GL2.GL_POLYGON_OFFSET_POINT);
                    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
                    break;
                case PolygonAttributes.POLYGON_FILL:
                    gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
                    gl.glDisable(GL2.GL_POLYGON_OFFSET_POINT);
                    gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
                    break;
            }
        } else {
            gl.glDisable(GL2.GL_POLYGON_OFFSET_POINT);
            gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
            gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        }
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
        if (VERBOSE) System.err.println("JoglPipeline.updateRenderingAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (!depthBufferEnableOverride) {
            if (depthBufferEnable) {
                gl.glEnable(GL.GL_DEPTH_TEST);
                gl.glDepthFunc(getFunctionValue(depthTestFunction));
            } else {
                gl.glDisable(GL.GL_DEPTH_TEST);
            }
        }

        if (!depthBufferWriteEnableOverride) {
            if (depthBufferWriteEnable) {
                gl.glDepthMask(true);
            } else {
                gl.glDepthMask(false);
            }
        }

        if (alphaTestFunction == RenderingAttributes.ALWAYS) {
            gl.glDisable(GL2.GL_ALPHA_TEST);
        } else {
            gl.glEnable(GL2.GL_ALPHA_TEST);
            gl.glAlphaFunc(getFunctionValue(alphaTestFunction), alphaTestValue);
        }

        if (ignoreVertexColors) {
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
        } else {
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
        }

        if (rasterOpEnable) {
            gl.glEnable(GL.GL_COLOR_LOGIC_OP);
            switch (rasterOp) {
                case RenderingAttributes.ROP_CLEAR:
                    gl.glLogicOp(GL.GL_CLEAR);
                    break;
                case RenderingAttributes.ROP_AND:
                    gl.glLogicOp(GL.GL_AND);
                    break;
                case RenderingAttributes.ROP_AND_REVERSE:
                    gl.glLogicOp(GL.GL_AND_REVERSE);
                    break;
                case RenderingAttributes.ROP_COPY:
                    gl.glLogicOp(GL.GL_COPY);
                    break;
                case RenderingAttributes.ROP_AND_INVERTED:
                    gl.glLogicOp(GL.GL_AND_INVERTED);
                    break;
                case RenderingAttributes.ROP_NOOP:
                    gl.glLogicOp(GL.GL_NOOP);
                    break;
                case RenderingAttributes.ROP_XOR:
                    gl.glLogicOp(GL.GL_XOR);
                    break;
                case RenderingAttributes.ROP_OR:
                    gl.glLogicOp(GL.GL_OR);
                    break;
                case RenderingAttributes.ROP_NOR:
                    gl.glLogicOp(GL.GL_NOR);
                    break;
                case RenderingAttributes.ROP_EQUIV:
                    gl.glLogicOp(GL.GL_EQUIV);
                    break;
                case RenderingAttributes.ROP_INVERT:
                    gl.glLogicOp(GL.GL_INVERT);
                    break;
                case RenderingAttributes.ROP_OR_REVERSE:
                    gl.glLogicOp(GL.GL_OR_REVERSE);
                    break;
                case RenderingAttributes.ROP_COPY_INVERTED:
                    gl.glLogicOp(GL.GL_COPY_INVERTED);
                    break;
                case RenderingAttributes.ROP_OR_INVERTED:
                    gl.glLogicOp(GL.GL_OR_INVERTED);
                    break;
                case RenderingAttributes.ROP_NAND:
                    gl.glLogicOp(GL.GL_NAND);
                    break;
                case RenderingAttributes.ROP_SET:
                    gl.glLogicOp(GL.GL_SET);
                    break;
            }
        } else {
            gl.glDisable(GL.GL_COLOR_LOGIC_OP);
        }

        if (userStencilAvailable) {
            if (stencilEnable) {
                gl.glEnable(GL.GL_STENCIL_TEST);

                gl.glStencilOp(getStencilOpValue(stencilFailOp),
                        getStencilOpValue(stencilZFailOp),
                        getStencilOpValue(stencilZPassOp));

                gl.glStencilFunc(getFunctionValue(stencilFunction),
                        stencilReferenceValue, stencilCompareMask);

                gl.glStencilMask(stencilWriteMask);

            } else {
                gl.glDisable(GL.GL_STENCIL_TEST);
            }
        }
    }

    private int getFunctionValue(int func) {
        switch (func) {
            case RenderingAttributes.ALWAYS:
                func = GL.GL_ALWAYS;
                break;
            case RenderingAttributes.NEVER:
                func = GL.GL_NEVER;
                break;
            case RenderingAttributes.EQUAL:
                func = GL.GL_EQUAL;
                break;
            case RenderingAttributes.NOT_EQUAL:
                func = GL.GL_NOTEQUAL;
                break;
            case RenderingAttributes.LESS:
                func = GL.GL_LESS;
                break;
            case RenderingAttributes.LESS_OR_EQUAL:
                func = GL.GL_LEQUAL;
                break;
            case RenderingAttributes.GREATER:
                func = GL.GL_GREATER;
                break;
            case RenderingAttributes.GREATER_OR_EQUAL:
                func = GL.GL_GEQUAL;
                break;
        }

        return func;
    }

    private int getStencilOpValue(int op) {
        switch (op) {
            case RenderingAttributes.STENCIL_KEEP:
                op = GL.GL_KEEP;
                break;
            case RenderingAttributes.STENCIL_ZERO:
                op = GL.GL_ZERO;
                break;
            case RenderingAttributes.STENCIL_REPLACE:
                op = GL.GL_REPLACE;
                break;
            case RenderingAttributes.STENCIL_INCR:
                op = GL.GL_INCR;
                break;
            case RenderingAttributes.STENCIL_DECR:
                op = GL.GL_DECR;
                break;
            case RenderingAttributes.STENCIL_INVERT:
                op = GL.GL_INVERT;
                break;
        }

        return op;
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
            double[] vworldToEc) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexCoordGeneration()");

		GL2 gl = context(ctx).getGL().getGL2();

        float[] planeS = new float[4];
        float[] planeT = new float[4];
        float[] planeR = new float[4];
        float[] planeQ = new float[4];

        if (enable) {
            gl.glEnable(GL2.GL_TEXTURE_GEN_S);
            gl.glEnable(GL2.GL_TEXTURE_GEN_T);
            if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                gl.glEnable(GL2.GL_TEXTURE_GEN_R);
                gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
            } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                gl.glEnable(GL2.GL_TEXTURE_GEN_R);
                gl.glEnable(GL2.GL_TEXTURE_GEN_Q);
            } else {
                gl.glDisable(GL2.GL_TEXTURE_GEN_R);
                gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
            }

            if (genMode != TexCoordGeneration.SPHERE_MAP) {
                planeS[0] = planeSx; planeS[1] = planeSy;
                planeS[2] = planeSz; planeS[3] = planeSw;
                planeT[0] = planeTx; planeT[1] = planeTy;
                planeT[2] = planeTz; planeT[3] = planeTw;
                if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                    planeR[0] = planeRx; planeR[1] = planeRy;
                    planeR[2] = planeRz; planeR[3] = planeRw;
                } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                    planeR[0] = planeRx; planeR[1] = planeRy;
                    planeR[2] = planeRz; planeR[3] = planeRw;
                    planeQ[0] = planeQx; planeQ[1] = planeQy;
                    planeQ[2] = planeQz; planeQ[3] = planeQw;
                }
            }

            switch (genMode) {
                case TexCoordGeneration.OBJECT_LINEAR:
                    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
                    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
                    gl.glTexGenfv(GL2.GL_S, GL2.GL_OBJECT_PLANE, planeS, 0);
                    gl.glTexGenfv(GL2.GL_T, GL2.GL_OBJECT_PLANE, planeT, 0);

                    if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
                        gl.glTexGenfv(GL2.GL_R, GL2.GL_OBJECT_PLANE, planeR, 0);
                    } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
                        gl.glTexGenfv(GL2.GL_R, GL2.GL_OBJECT_PLANE, planeR, 0);
                        gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_OBJECT_LINEAR);
                        gl.glTexGenfv(GL2.GL_Q, GL2.GL_OBJECT_PLANE, planeQ, 0);
                    }
                    break;
                case TexCoordGeneration.EYE_LINEAR:

                    gl.glMatrixMode(GL2.GL_MODELVIEW);
                    gl.glPushMatrix();

                    if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
                        gl.glLoadTransposeMatrixd(vworldToEc, 0);
                    } else {
                        double[] v = new double[16];
                        copyTranspose(vworldToEc, v);
                        gl.glLoadMatrixd(v, 0);
                    }

                    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
                    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
                    gl.glTexGenfv(GL2.GL_S, GL2.GL_EYE_PLANE, planeS, 0);
                    gl.glTexGenfv(GL2.GL_T, GL2.GL_EYE_PLANE, planeT, 0);

                    if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
                        gl.glTexGenfv(GL2.GL_R, GL2.GL_EYE_PLANE, planeR, 0);
                    } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
                        gl.glTexGenfv(GL2.GL_R, GL2.GL_EYE_PLANE, planeR, 0);
                        gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_EYE_LINEAR);
                        gl.glTexGenfv(GL2.GL_Q, GL2.GL_EYE_PLANE, planeQ, 0);
                    }
                    gl.glPopMatrix();
                    break;
                case TexCoordGeneration.SPHERE_MAP:
                    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
                    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
                    if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
                    } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
                        gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_SPHERE_MAP);
                    }

                    break;
                case TexCoordGeneration.NORMAL_MAP:
                    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
                    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
                    if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
                    } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
                        gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_NORMAL_MAP);
                    }
                    break;
                case TexCoordGeneration.REFLECTION_MAP:
                    gl.glTexGeni(GL2.GL_S, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
                    gl.glTexGeni(GL2.GL_T, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
                    if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
                    } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
                        gl.glTexGeni(GL2.GL_R, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
                        gl.glTexGeni(GL2.GL_Q, GL2.GL_TEXTURE_GEN_MODE, GL2.GL_REFLECTION_MAP);
                    }
                    break;
            }
        } else {
            gl.glDisable(GL2.GL_TEXTURE_GEN_S);
            gl.glDisable(GL2.GL_TEXTURE_GEN_T);
            gl.glDisable(GL2.GL_TEXTURE_GEN_R);
            gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
        }
    }


    // ---------------------------------------------------------------------

    //
    // TransparencyAttributesRetained methods
    //

    private static final int screen_door[][] = {
        /* 0 / 16 */
        {
            0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
                    0x00000000, 0x00000000, 0x00000000, 0x00000000,
        },
        /* 1 / 16 */
        {
            0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
                    0x00000000, 0x22222222, 0x00000000, 0x00000000,
        },
        /* 2 / 16 */
        {
            0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
                    0x00000000, 0x22222222, 0x00000000, 0x88888888,
        },
        /* 3 / 16 */
        {
            0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0x88888888,
        },
        /* 4 / 16 */
        {
            0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x00000000, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
        },
        /* 5 / 16 */
        {
            0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x00000000, 0xaaaaaaaa,
        },
        /* 6 / 16 */
        {
            0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x11111111, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
        },
        /* 7 / 16 */
        {
            0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x44444444, 0xaaaaaaaa,
        },
        /* 8 / 16 */
        {
            0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x55555555, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
        },
        /* 9 / 16 */
        {
            0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0x55555555, 0xaaaaaaaa,
        },
        /* 10 / 16 */
        {
            0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0x77777777, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
        },
        /* 11 / 16 */
        {
            0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xdddddddd, 0xaaaaaaaa,
        },
        /* 12 / 16 */
        {
            0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xaaaaaaaa, 0xffffffff, 0xaaaaaaaa,
        },
        /* 13 / 16 */
        {
            0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xaaaaaaaa,
        },
        /* 14 / 16 */
        {
            0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xbbbbbbbb, 0xffffffff, 0xeeeeeeee,
        },
        /* 15 / 16 */
        {
            0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xeeeeeeee,
        },
        /* 16 / 16 */
        {
            0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
                    0xffffffff, 0xffffffff, 0xffffffff, 0xffffffff,
        },
    };
    private static final ByteBuffer[] screen_door_table = new ByteBuffer[screen_door.length];
    static {
		int eachLen = screen_door[0].length * Buffers.SIZEOF_INT;
		ByteBuffer buf = Buffers.newDirectByteBuffer(screen_door.length * eachLen);
        IntBuffer intBuf = buf.asIntBuffer();
        for (int i = 0; i < screen_door.length; i++) {
            intBuf.put(screen_door[i]);
        }
        buf.rewind();
        for (int i = 0; i < screen_door.length; i++) {
            buf.position(i * eachLen);
            buf.limit((i+1) * eachLen);
            screen_door_table[i] = buf.slice();
        }
    }

    private static final int[] blendFunctionTable = new int[TransparencyAttributes.MAX_BLEND_FUNC_TABLE_SIZE];
    static {
        blendFunctionTable[TransparencyAttributes.BLEND_ZERO] = GL.GL_ZERO;
        blendFunctionTable[TransparencyAttributes.BLEND_ONE] = GL.GL_ONE;
        blendFunctionTable[TransparencyAttributes.BLEND_SRC_ALPHA] = GL.GL_SRC_ALPHA;
        blendFunctionTable[TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA] = GL.GL_ONE_MINUS_SRC_ALPHA;
        blendFunctionTable[TransparencyAttributes.BLEND_DST_COLOR] = GL.GL_DST_COLOR;
        blendFunctionTable[TransparencyAttributes.BLEND_ONE_MINUS_DST_COLOR] = GL.GL_ONE_MINUS_DST_COLOR;
        blendFunctionTable[TransparencyAttributes.BLEND_SRC_COLOR] = GL.GL_SRC_COLOR;
        blendFunctionTable[TransparencyAttributes.BLEND_ONE_MINUS_SRC_COLOR] = GL.GL_ONE_MINUS_SRC_COLOR;
        blendFunctionTable[TransparencyAttributes.BLEND_CONSTANT_COLOR] = GL2.GL_CONSTANT_COLOR;
    }

    @Override
    void updateTransparencyAttributes(Context ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTransparencyAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (transparencyMode != TransparencyAttributes.SCREEN_DOOR) {
            gl.glDisable(GL2.GL_POLYGON_STIPPLE);
        } else  {
            gl.glEnable(GL2.GL_POLYGON_STIPPLE);
            gl.glPolygonStipple(screen_door_table[(int)(alpha * 16)]);
        }

        if ((transparencyMode < TransparencyAttributes.SCREEN_DOOR) ||
                ((((geometryType & RenderMolecule.LINE) != 0) ||
                (polygonMode == PolygonAttributes.POLYGON_LINE))
                && lineAA) ||
                ((((geometryType & RenderMolecule.POINT) != 0) ||
                (polygonMode == PolygonAttributes.POLYGON_POINT))
                && pointAA)) {
            gl.glEnable(GL.GL_BLEND);
            // valid range of blendFunction 0..3 is already verified in shared code.
            gl.glBlendFunc(blendFunctionTable[srcBlendFunction], blendFunctionTable[dstBlendFunction]);
        } else {
            gl.glDisable(GL.GL_BLEND);
        }
    }


    // ---------------------------------------------------------------------

    //
    // TextureAttributesRetained methods
    //

    @Override
    void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode,
            float textureBlendColorRed,
            float textureBlendColorGreen,
            float textureBlendColorBlue,
            float textureBlendColorAlpha,
            int textureFormat) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT,
                (perspCorrectionMode == TextureAttributes.NICEST) ? GL.GL_NICEST : GL.GL_FASTEST);

        // set OGL texture matrix
        gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_TEXTURE);

        if (isIdentity) {
            gl.glLoadIdentity();
        } else if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glLoadTransposeMatrixd(transform, 0);
        } else {
            double[] mx = new double[16];
            copyTranspose(transform, mx);
            gl.glLoadMatrixd(mx, 0);
        }

        gl.glPopAttrib();

        // set texture color
        float[] color = new float[4];
        color[0] = textureBlendColorRed;
        color[1] = textureBlendColorGreen;
        color[2] = textureBlendColorBlue;
        color[3] = textureBlendColorAlpha;
        gl.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, color, 0);

        // set texture environment mode

        switch (textureMode) {
            case TextureAttributes.MODULATE:
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
                break;
            case TextureAttributes.DECAL:
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);
                break;
            case TextureAttributes.BLEND:
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
                break;
            case TextureAttributes.REPLACE:
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
                break;
            case TextureAttributes.COMBINE:
                gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_COMBINE);
                break;
        }
// FIXME: GL_SGI_texture_color_table
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
//            gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
//        }
    }

    @Override
    void updateRegisterCombiners(Context absCtx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode,
            float textureBlendColorRed,
            float textureBlendColorGreen,
            float textureBlendColorBlue,
            float textureBlendColorAlpha,
            int textureFormat,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
// FIXME: GL_NV_register_combiners
//        if (VERBOSE) System.err.println("JoglPipeline.updateRegisterCombiners()");
//
//        JoglContext ctx = (JoglContext) absCtx;
//		GL2 gl = context(ctx).getGL().getGL2();
//
//        if (perspCorrectionMode == TextureAttributes.NICEST) {
//            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
//        } else {
//            gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);
//        }
//
//        // set OGL texture matrix
//        gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
//        gl.glMatrixMode(GL.GL_TEXTURE);
//
//        if (isIdentity) {
//            gl.glLoadIdentity();
//        } else if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
//            gl.glLoadTransposeMatrixd(transform, 0);
//        } else {
//            double[] mx = new double[16];
//            copyTranspose(transform, mx);
//            gl.glLoadMatrixd(mx, 0);
//        }
//
//        gl.glPopAttrib();
//
//        // set texture color
//        float[] color = new float[4];
//        color[0] = textureBlendColorRed;
//        color[1] = textureBlendColorGreen;
//        color[2] = textureBlendColorBlue;
//        color[3] = textureBlendColorAlpha;
//        gl.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, color, 0);
//
//        // set texture environment mode
//        gl.glEnable(GL.GL_REGISTER_COMBINERS_NV);
//        int textureUnit = ctx.getCurrentTextureUnit();
//        int combinerUnit = ctx.getCurrentCombinerUnit();
//        int fragment;
//        if (combinerUnit == GL.GL_COMBINER0_NV) {
//            fragment = GL.GL_PRIMARY_COLOR_NV;
//        } else {
//            fragment = GL.GL_SPARE0_NV;
//        }
//
//        switch (textureMode) {
//            case TextureAttributes.MODULATE:
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_B_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_B_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                break;
//
//            case TextureAttributes.DECAL:
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_B_NV, textureUnit,
//                        GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_C_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_D_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
//                        GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
//
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, GL.GL_SPARE0_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                break;
//
//            case TextureAttributes.BLEND:
//                gl.glCombinerParameterfvNV(GL.GL_CONSTANT_COLOR0_NV, color, 0);
//
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_B_NV, textureUnit,
//                        GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_C_NV, GL.GL_CONSTANT_COLOR0_NV,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_D_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_A_NV, fragment,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_B_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, GL.GL_SPARE0_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                break;
//
//            case TextureAttributes.REPLACE:
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_A_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
//                        GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_A_NV, textureUnit,
//                        GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//                gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
//                        GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
//
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA,
//                        GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                        GL.GL_NONE, GL.GL_NONE, false, false, false);
//                break;
//
//            case TextureAttributes.COMBINE:
//                if (combineRgbMode == TextureAttributes.COMBINE_DOT3) {
//                    int color1 = getCombinerArg(gl, combineRgbSrc[0], textureUnit, combinerUnit);
//                    gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                            GL.GL_VARIABLE_A_NV, color1,
//                            GL.GL_EXPAND_NORMAL_NV, GL.GL_RGB);
//                    int color2 = getCombinerArg(gl, combineRgbSrc[1], textureUnit, combinerUnit);
//                    gl.glCombinerInputNV(combinerUnit, GL.GL_RGB,
//                            GL.GL_VARIABLE_B_NV, color2,
//                            GL.GL_EXPAND_NORMAL_NV, GL.GL_RGB);
//                    gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                            GL.GL_VARIABLE_A_NV, GL.GL_ZERO,
//                            GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
//                    gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA,
//                            GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
//                            GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
//
//                    gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB,
//                            GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                            GL.GL_NONE/*SCALE_BY_FOUR_NV*/, GL.GL_NONE, true,
//                            false, false);
//                    gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA,
//                            GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV,
//                            GL.GL_NONE, GL.GL_NONE, false,
//                            false, false);
//                }
//                break;
//        }
//
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_A_NV,
//                GL.GL_SPARE0_NV, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_B_NV,
//                GL.GL_ZERO, GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_C_NV,
//                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_D_NV,
//                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_E_NV,
//                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_F_NV,
//                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
//        gl.glFinalCombinerInputNV(GL.GL_VARIABLE_G_NV,
//                GL.GL_SPARE0_NV, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
//
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table"))
//            gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
        // GL_SGI_texture_color_table
    }

    @Override
    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] textureColorTable) {
// FIXME: GL_SGI_texture_color_table
//        if (VERBOSE) System.err.println("JoglPipeline.updateTextureColorTable()");
//
//        GL gl = context(ctx).getGL();
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
//            if (numComponents == 3) {
//                gl.glColorTable(GL.GL_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGB,
//                        colorTableSize, GL.GL_RGB, GL2.GL_INT, IntBuffer.wrap(textureColorTable));
//            } else {
//                gl.glColorTable(GL.GL_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGBA,
//                        colorTableSize, GL.GL_RGBA, GL2.GL_INT, IntBuffer.wrap(textureColorTable));
//            }
//            gl.glEnable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
//        }
    }

    @Override
    void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
        if (VERBOSE) System.err.println("JoglPipeline.updateCombiner()");

		GL2 gl = context(ctx).getGL().getGL2();
        int[] GLrgbMode = new int[1];
        int[] GLalphaMode = new int[1];
        getGLCombineMode(gl, combineRgbMode, combineAlphaMode,
                GLrgbMode, GLalphaMode);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_RGB, GLrgbMode[0]);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_COMBINE_ALPHA, GLalphaMode[0]);

        int nargs;
        if (combineRgbMode == TextureAttributes.COMBINE_REPLACE) {
            nargs = 1;
        } else if (combineRgbMode == TextureAttributes.COMBINE_INTERPOLATE) {
            nargs = 3;
        } else {
            nargs = 2;
        }

        for (int i = 0; i < nargs; i++) {
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, _gl_combineRgbSrcIndex[i],
                    _gl_combineSrc[combineRgbSrc[i]]);
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, _gl_combineRgbOpIndex[i],
                    _gl_combineFcn[combineRgbFcn[i]]);
        }

        if (combineAlphaMode == TextureAttributes.COMBINE_REPLACE) {
            nargs = 1;
        } else if (combineAlphaMode == TextureAttributes.COMBINE_INTERPOLATE) {
            nargs = 3;
        } else {
            nargs = 2;
        }

        for (int i = 0; i < nargs; i++) {
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, _gl_combineAlphaSrcIndex[i],
                    _gl_combineSrc[combineAlphaSrc[i]]);
            gl.glTexEnvi(GL2.GL_TEXTURE_ENV, _gl_combineAlphaOpIndex[i],
                    _gl_combineFcn[combineAlphaFcn[i]]);
        }

        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_RGB_SCALE, combineRgbScale);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_ALPHA_SCALE, combineAlphaScale);
    }

    // Helper routines for above

    private void getGLCombineMode(GL gl, int combineRgbMode, int combineAlphaMode,
            int[] GLrgbMode, int[] GLalphaMode) {
        switch (combineRgbMode) {
            case TextureAttributes.COMBINE_REPLACE:
                GLrgbMode[0] = GL.GL_REPLACE;
                break;
            case TextureAttributes.COMBINE_MODULATE:
                GLrgbMode[0] = GL2.GL_MODULATE;
                break;
            case TextureAttributes.COMBINE_ADD:
                GLrgbMode[0] = GL2.GL_ADD;
                break;
            case TextureAttributes.COMBINE_ADD_SIGNED:
                GLrgbMode[0] = GL2.GL_ADD_SIGNED;
                break;
            case TextureAttributes.COMBINE_SUBTRACT:
                GLrgbMode[0] = GL2.GL_SUBTRACT;
                break;
            case TextureAttributes.COMBINE_INTERPOLATE:
                GLrgbMode[0] = GL2.GL_INTERPOLATE;
                break;
            case TextureAttributes.COMBINE_DOT3:
                GLrgbMode[0] = GL2.GL_DOT3_RGB;
                break;
            default:
                break;
        }

        switch (combineAlphaMode) {
            case TextureAttributes.COMBINE_REPLACE:
                GLalphaMode[0] = GL.GL_REPLACE;
                break;
            case TextureAttributes.COMBINE_MODULATE:
                GLalphaMode[0] = GL2.GL_MODULATE;
                break;
            case TextureAttributes.COMBINE_ADD:
                GLalphaMode[0] = GL2.GL_ADD;
                break;
            case TextureAttributes.COMBINE_ADD_SIGNED:
                GLalphaMode[0] = GL2.GL_ADD_SIGNED;
                break;
            case TextureAttributes.COMBINE_SUBTRACT:
                GLalphaMode[0] = GL2.GL_SUBTRACT;
                break;
            case TextureAttributes.COMBINE_INTERPOLATE:
                GLalphaMode[0] = GL2.GL_INTERPOLATE;
                break;
            case TextureAttributes.COMBINE_DOT3:
                // dot3 will only make sense for alpha if rgb is also
                // doing dot3. So if rgb is not doing dot3, fallback to replace
                if (combineRgbMode == TextureAttributes.COMBINE_DOT3) {
                    GLrgbMode[0] = GL2.GL_DOT3_RGBA;
                } else {
                    GLalphaMode[0] = GL.GL_REPLACE;
                }
                break;
            default:
                break;
        }
    }

    // mapping from java enum to gl enum
    private static final int[] _gl_combineRgbSrcIndex = {
        GL2.GL_SOURCE0_RGB,
        GL2.GL_SOURCE1_RGB,
        GL2.GL_SOURCE2_RGB,
    };

    private static final int[] _gl_combineAlphaSrcIndex = {
        GL2.GL_SOURCE0_ALPHA,
        GL2.GL_SOURCE1_ALPHA,
        GL2.GL_SOURCE2_ALPHA,
    };

    private static final int[] _gl_combineRgbOpIndex = {
        GL2.GL_OPERAND0_RGB,
        GL2.GL_OPERAND1_RGB,
        GL2.GL_OPERAND2_RGB,
    };

    private static final int[] _gl_combineAlphaOpIndex = {
        GL2.GL_OPERAND0_ALPHA,
        GL2.GL_OPERAND1_ALPHA,
        GL2.GL_OPERAND2_ALPHA,
    };

    private static final int[] _gl_combineSrc = {
        GL2.GL_PRIMARY_COLOR,      // TextureAttributes.COMBINE_OBJECT_COLOR
        GL.GL_TEXTURE,            // TextureAttributes.COMBINE_TEXTURE
        GL2.GL_CONSTANT,           // TextureAttributes.COMBINE_CONSTANT_COLOR
        GL2.GL_PREVIOUS,           // TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE
    };

    private static final int[] _gl_combineFcn = {
        GL.GL_SRC_COLOR,              // TextureAttributes.COMBINE_SRC_COLOR
        GL.GL_ONE_MINUS_SRC_COLOR,    // TextureAttributes.COMBINE_ONE_MINUS_SRC_COLOR
        GL.GL_SRC_ALPHA,              // TextureAttributes.COMBINE_SRC_ALPHA
        GL.GL_ONE_MINUS_SRC_ALPHA,    // TextureAttributes.COMBINE_ONE_MINUS_SRC_ALPHA
    };

// FIXME: GL_NV_register_combiners
//    private int getCombinerArg(GL gl, int arg, int textureUnit, int combUnit) {
//        int comb = 0;
//
//        switch (arg) {
//            case TextureAttributes.COMBINE_OBJECT_COLOR:
//                if (combUnit == GL.GL_COMBINER0_NV) {
//                    comb = GL.GL_PRIMARY_COLOR_NV;
//                } else {
//                    comb = GL.GL_SPARE0_NV;
//                }
//                break;
//            case TextureAttributes.COMBINE_TEXTURE_COLOR:
//                comb = textureUnit;
//                break;
//            case TextureAttributes.COMBINE_CONSTANT_COLOR:
//                comb = GL.GL_CONSTANT_COLOR0_NV;
//                break;
//            case TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE:
//                comb = textureUnit -1;
//                break;
//        }
//
//        return comb;
//    }


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    @Override
    void updateTextureUnitState(Context ctx, int index, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureUnitState()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (index >= 0 && gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glActiveTexture(index + GL.GL_TEXTURE0);
            gl.glClientActiveTexture(GL.GL_TEXTURE0 + index);
// FIXME: GL_NV_register_combiners
//            if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
//                jctx.setCurrentTextureUnit(index + GL.GL_TEXTURE0);
//                jctx.setCurrentCombinerUnit(index + GL.GL_COMBINER0_NV);
//                gl.glCombinerParameteriNV(GL.GL_NUM_GENERAL_COMBINERS_NV, index + 1);
//            }
        }

        if (!enable) {
            // if not enabled, then don't enable any tex mapping
            gl.glDisable(GL2.GL_TEXTURE_1D);
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glDisable(GL2.GL_TEXTURE_3D);
            gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
        }

        // if it is enabled, the enable flag will be taken care of
        // in the bindTexture call
    }


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    @Override
    void bindTexture2D(Context ctx, int objectId, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.bindTexture2D(objectId=" + objectId + ",enable=" + enable + ")");

        GL gl = context(ctx).getGL();
        gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
        gl.glDisable(GL2.GL_TEXTURE_3D);

        if (!enable) {
            gl.glDisable(GL.GL_TEXTURE_2D);
        } else {
            gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);
            gl.glEnable(GL.GL_TEXTURE_2D);
        }
    }

    @Override
    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int dataType, Object data, boolean useAutoMipMap) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DImage(width=" + width + ",height=" + height + ",level=" + level + ")");

        updateTexture2DImage(ctx, GL.GL_TEXTURE_2D,
                numLevels, level, textureFormat, imageFormat,
                width, height, boundaryWidth, dataType, data, useAutoMipMap);
    }

    @Override
    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int dataType, Object data, boolean useAutoMipMap) {

        /* Note: useAutoMipMap is not use for SubImage in the jogl pipe */

        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DSubImage()");

        updateTexture2DSubImage(ctx, GL.GL_TEXTURE_2D,
                level, xoffset, yoffset,
                textureFormat, imageFormat,
                imgXOffset, imgYOffset, tilew, width, height,
                dataType, data);
    }

    @Override
    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLOD, float maximumLOD) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DLodRange()");

        updateTextureLodRange(ctx, GL.GL_TEXTURE_2D,
                baseLevel, maximumLevel,
                minimumLOD, maximumLOD);
    }

    @Override
    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DLodOffset()");

        updateTextureLodOffset(ctx, GL.GL_TEXTURE_2D,
                lodOffsetS, lodOffsetT, lodOffsetR);
    }

    @Override
    void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DBoundary()");

        updateTextureBoundary(ctx, GL.GL_TEXTURE_2D,
                boundaryModeS, boundaryModeT, -1,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    @Override
    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DFilterModes()");

        updateTextureFilterModes(ctx, GL.GL_TEXTURE_2D, minFilter, magFilter);
    }

    @Override
    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DSharpenFunc()");

        updateTextureSharpenFunc(ctx, GL.GL_TEXTURE_2D,
                numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    @Override
    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DFilter4Func()");

        updateTextureFilter4Func(ctx, GL.GL_TEXTURE_2D,
                numFilter4FuncPts, filter4FuncPts);
    }

    @Override
    void updateTexture2DAnisotropicFilter(Context ctx, float degree) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DAnisotropicFilter()");

        updateTextureAnisotropicFilter(ctx, GL.GL_TEXTURE_2D, degree);
    }

    private void updateTextureLodRange(Context ctx,
            int target,
            int baseLevel, int maximumLevel,
            float minimumLOD, float maximumLOD) {
        GL gl = context(ctx).getGL();
        // checking of the availability of the extension is already done
        // in the shared code
        gl.glTexParameteri(target, GL2.GL_TEXTURE_BASE_LEVEL, baseLevel);
        gl.glTexParameteri(target, GL2.GL_TEXTURE_MAX_LEVEL, maximumLevel);
        gl.glTexParameterf(target, GL2.GL_TEXTURE_MIN_LOD, minimumLOD);
        gl.glTexParameterf(target, GL2.GL_TEXTURE_MAX_LOD, maximumLOD);
    }

    private void updateTextureLodOffset(Context ctx,
            int target,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
// FIXME: GL_SGIX_texture_lod_bias
//        GL gl = context(ctx).getGL();
        // checking of the availability of the extension is already done
        // in the shared code
//        gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_S_SGIX, lodOffsetS);
//        gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_T_SGIX, lodOffsetT);
//        gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_R_SGIX, lodOffsetR);
    }

    private void updateTextureAnisotropicFilter(Context ctx, int target, float degree) {
        GL gl = context(ctx).getGL();
        // checking of the availability of anisotropic filter functionality
        // is already done in the shared code
        gl.glTexParameterf(target,
                GL.GL_TEXTURE_MAX_ANISOTROPY_EXT,
                degree);
    }

    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    @Override
    void bindTexture3D(Context ctx, int objectId, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.bindTexture3D()");

        GL gl = context(ctx).getGL();
        // textureCubeMap will take precedure over 3D Texture
        gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);

        if (!enable) {
            gl.glDisable(GL2.GL_TEXTURE_3D);
        } else {
            gl.glBindTexture(GL2.GL_TEXTURE_3D, objectId);
            gl.glEnable(GL2.GL_TEXTURE_3D);
        }
    }

    @Override
    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height, int depth,
            int boundaryWidth,
            int dataType, Object data, boolean useAutoMipMap) {

        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DImage()");

		GL2 gl = context(ctx).getGL().getGL2();

        int format = 0;
        int internalFormat = 0;
        int type = GL2.GL_UNSIGNED_INT_8_8_8_8;
        boolean forceAlphaToOne = false;

        switch (textureFormat) {
            case Texture.INTENSITY:
                internalFormat = GL2.GL_INTENSITY;
                break;
            case Texture.LUMINANCE:
                internalFormat = GL.GL_LUMINANCE;
                break;
            case Texture.ALPHA:
                internalFormat = GL.GL_ALPHA;
                break;
            case Texture.LUMINANCE_ALPHA:
                internalFormat = GL.GL_LUMINANCE_ALPHA;
                break;
            case Texture.RGB:
                internalFormat = GL.GL_RGB;
                break;
            case Texture.RGBA:
                internalFormat = GL.GL_RGBA;
                break;
            default:
                assert false;
                return;
        }

        if (useAutoMipMap) {
            gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_GENERATE_MIPMAP, GL.GL_TRUE);
        }
        else {
            gl.glTexParameteri(GL2.GL_TEXTURE_3D, GL2.GL_GENERATE_MIPMAP, GL.GL_FALSE);
        }

        if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_BUFFER)) {

            switch (imageFormat) {
                case ImageComponentRetained.TYPE_BYTE_BGR:
                    format = GL2.GL_BGR;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    format = GL.GL_RGB;
                    break;
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                    if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
                        format = GL2.GL_ABGR_EXT;
                    } else {
                        assert false;
                        return;
                    }
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    // all RGB types are stored as RGBA
                    format = GL.GL_RGBA;
                    break;
                case ImageComponentRetained.TYPE_BYTE_LA:
                    // all LA types are stored as LA8
                    format = GL.GL_LUMINANCE_ALPHA;
                    break;
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                    if (internalFormat == GL.GL_ALPHA) {
                        format = GL.GL_ALPHA;
                    } else  {
                        format = GL.GL_LUMINANCE;
                    }
                    break;
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_INT_BGR:
                case ImageComponentRetained.TYPE_INT_RGB:
                case ImageComponentRetained.TYPE_INT_ARGB:
                default:
                    assert false;
                    return;
            }

	    if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) {

            gl.glTexImage3D(GL2.GL_TEXTURE_3D,
                    level, internalFormat,
                    width, height, depth, boundaryWidth,
                    format, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[]) data));
            }
	    else {
                gl.glTexImage3D(GL2.GL_TEXTURE_3D,
                    level, internalFormat,
                    width, height, depth, boundaryWidth,
                    format, GL.GL_UNSIGNED_BYTE, (ByteBuffer) data);
	    }

        } else if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_BUFFER)) {

            switch (imageFormat) {
                /* GL_BGR */
                case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                    format = GL.GL_RGBA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    forceAlphaToOne = true;
                    break;
                case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                    forceAlphaToOne = true;
                    /* Fall through to next case */
                case ImageComponentRetained.TYPE_INT_ARGB:
                    format = GL2.GL_BGRA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    break;
                    /* This method only supports 3 and 4 components formats and INT types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_BYTE_BGR:
                case ImageComponentRetained.TYPE_BYTE_RGB:
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                default:
                    assert false;
                    return;
            }

            /* Force Alpha to 1.0 if needed */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
            }

            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) {
                gl.glTexImage3D(GL2.GL_TEXTURE_3D,
                        level, internalFormat,
                        width, height, depth, boundaryWidth,
                        format, type, IntBuffer.wrap((int[]) data));
            } else {
                gl.glTexImage3D(GL2.GL_TEXTURE_3D,
                        level, internalFormat,
                        width, height, depth, boundaryWidth,
                        format, type, (Buffer) data);
            }

            /* Restore Alpha scale and bias */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
            }
        } else {
            assert false;
        }
    }

    @Override
    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset, int imgZOffset,
            int tilew, int tileh,
            int width, int height, int depth,
            int dataType, Object data, boolean useAutoMipMap) {

        /* Note: useAutoMipMap is not use for SubImage in the jogl pipe */

        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DSubImage()");

		GL2 gl = context(ctx).getGL().getGL2();

        int format = 0;
        int internalFormat = 0;
        int type = GL2.GL_UNSIGNED_INT_8_8_8_8;
        int numBytes = 0;
        boolean forceAlphaToOne = false;
        boolean pixelStore = false;

        if (imgXOffset > 0 || (width < tilew)) {
            pixelStore = true;
            gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, tilew);
        }

        switch (textureFormat) {
            case Texture.INTENSITY:
                internalFormat = GL2.GL_INTENSITY;
                break;
            case Texture.LUMINANCE:
                internalFormat = GL.GL_LUMINANCE;
                break;
            case Texture.ALPHA:
                internalFormat = GL.GL_ALPHA;
                break;
            case Texture.LUMINANCE_ALPHA:
                internalFormat = GL.GL_LUMINANCE_ALPHA;
                break;
            case Texture.RGB:
                internalFormat = GL.GL_RGB;
                break;
            case Texture.RGBA:
                internalFormat = GL.GL_RGBA;
                break;
            default:
                assert false;
        }

        if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_BUFFER)) {

            switch (imageFormat) {
                case ImageComponentRetained.TYPE_BYTE_BGR:
                    format = GL2.GL_BGR;
                    numBytes = 3;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    format = GL.GL_RGB;
                    numBytes = 3;
                    break;
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                    if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
                        format = GL2.GL_ABGR_EXT;
                        numBytes = 4;
                    } else {
                        assert false;
                        return;
                    }
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    // all RGB types are stored as RGBA
                    format = GL.GL_RGBA;
                    numBytes = 4;
                    break;
                case ImageComponentRetained.TYPE_BYTE_LA:
                    // all LA types are stored as LA8
                    format = GL.GL_LUMINANCE_ALPHA;
                    numBytes = 2;
                    break;
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                    if (internalFormat == GL.GL_ALPHA) {
                        format = GL.GL_ALPHA;
                        numBytes = 1;
                    } else  {
                        format = GL.GL_LUMINANCE;
                        numBytes = 1;
                    }
                    break;
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_INT_BGR:
                case ImageComponentRetained.TYPE_INT_RGB:
                case ImageComponentRetained.TYPE_INT_ARGB:
                default:
                    assert false;
                    return;
            }

            ByteBuffer buf = null;
            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) {
                buf = ByteBuffer.wrap((byte[]) data);
            }
            else {
                buf = (ByteBuffer) data;
            }

            int offset = (tilew * tileh * imgZOffset +
                    tilew * imgYOffset + imgXOffset) * numBytes;
            buf.position(offset);
            gl.glTexSubImage3D(GL2.GL_TEXTURE_3D,
                    level, xoffset, yoffset, zoffset,
                    width, height, depth,
                    format, GL.GL_UNSIGNED_BYTE,
                    buf);

        } else if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_BUFFER)) {

            switch (imageFormat) {
                /* GL_BGR */
                case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                    format = GL.GL_RGBA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    forceAlphaToOne = true;
                    break;
                case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                    forceAlphaToOne = true;
                    /* Fall through to next case */
                case ImageComponentRetained.TYPE_INT_ARGB:
                    format = GL2.GL_BGRA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    break;
                    /* This method only supports 3 and 4 components formats and INT types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_BYTE_BGR:
                case ImageComponentRetained.TYPE_BYTE_RGB:
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                default:
                    assert false;
                    return;
            }

            /* Force Alpha to 1.0 if needed */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
            }

            IntBuffer buf = null;
            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) {
                buf = IntBuffer.wrap((int[]) data);
            }
            else {
                buf = (IntBuffer) data;
            }

            int offset = tilew * tileh * imgZOffset +
                    tilew * imgYOffset + imgXOffset;
            buf.position(offset);
            gl.glTexSubImage3D(GL2.GL_TEXTURE_3D,
                    level, xoffset, yoffset, zoffset,
                    width, height, depth,
                    format, type,
                    buf);

            /* Restore Alpha scale and bias */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
            }
        } else {
            assert false;
            return;
        }

        if (pixelStore) {
            gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
        }

    }


    @Override
    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DLodRange()");

        updateTextureLodRange(ctx, GL2.GL_TEXTURE_3D,
                baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    @Override
    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DLodOffset()");

        updateTextureLodOffset(ctx, GL2.GL_TEXTURE_3D,
                lodOffsetS, lodOffsetT, lodOffsetR);
    }

    @Override
    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DBoundary()");

        updateTextureBoundary(ctx, GL2.GL_TEXTURE_3D,
                boundaryModeS, boundaryModeT, boundaryModeR,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    @Override
    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DFilterModes()");

        updateTextureFilterModes(ctx, GL2.GL_TEXTURE_3D,
                minFilter, magFilter);
    }

    @Override
    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DSharpenFunc()");

        updateTextureSharpenFunc(ctx, GL2.GL_TEXTURE_3D,
                numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    @Override
    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DFilter4Func()");

        updateTextureFilter4Func(ctx, GL2.GL_TEXTURE_3D,
                numFilter4FuncPts, filter4FuncPts);
    }

    @Override
    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DAnisotropicFilter()");

        updateTextureAnisotropicFilter(ctx, GL2.GL_TEXTURE_3D, degree);
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    @Override
    void bindTextureCubeMap(Context ctx, int objectId, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.bindTextureCubeMap()");

        GL gl = context(ctx).getGL();
        // TextureCubeMap will take precedure over 3D Texture so
        // there is no need to disable 3D Texture here.
        if (!enable) {
            gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
        } else {
            gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, objectId);
            gl.glEnable(GL.GL_TEXTURE_CUBE_MAP);
        }
    }

    @Override
    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int textureFormat, int imageFormat,
            int width, int height,
            int boundaryWidth,
            int dataType, Object data, boolean useAutoMipMap) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapImage()");

        updateTexture2DImage(ctx, _gl_textureCubeMapFace[face],
                numLevels, level, textureFormat, imageFormat,
                width, height, boundaryWidth, dataType, data, useAutoMipMap);
    }

    @Override
    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int textureFormat,int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int dataType, Object data, boolean useAutoMipMap) {

        /* Note: useAutoMipMap is not use for SubImage in the jogl pipe */

        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapSubImage()");

        updateTexture2DSubImage(ctx, _gl_textureCubeMapFace[face],
                level, xoffset, yoffset, textureFormat,
                imageFormat, imgXOffset, imgYOffset, tilew,
                width, height, dataType, data);
    }

    @Override
    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapLodRange()");

        updateTextureLodRange(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                baseLevel, maximumLevel,
                minimumLod, maximumLod);
    }

    @Override
    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapLodOffset()");

        updateTextureLodOffset(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                lodOffsetS, lodOffsetT, lodOffsetR);
    }

    @Override
    void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapBoundary()");

        updateTextureBoundary(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                boundaryModeS, boundaryModeT, -1,
                boundaryRed, boundaryGreen,
                boundaryBlue, boundaryAlpha);
    }

    @Override
    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapFilterModes()");

        updateTextureFilterModes(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                minFilter, magFilter);
    }

    @Override
    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapSharpenFunc()");

        updateTextureSharpenFunc(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    @Override
    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapFilter4Func()");

        updateTextureFilter4Func(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                numFilter4FuncPts, filter4FuncPts);
    }

    @Override
    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
        if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapAnisotropicFilter()");

        updateTextureAnisotropicFilter(ctx,
                GL.GL_TEXTURE_CUBE_MAP,
                degree);
    }

    //----------------------------------------------------------------------
    //
    // Helper routines for above texture methods
    //

    private void updateTexture2DImage(Context ctx,
            int target,
            int numLevels,
            int level,
            int textureFormat,
            int imageFormat,
            int width,
            int height,
            int boundaryWidth,
            int dataType,
            Object data,
            boolean useAutoMipMap) {
		GL2 gl = context(ctx).getGL().getGL2();

        int format = 0, internalFormat = 0;
        int type = GL2.GL_UNSIGNED_INT_8_8_8_8;
        boolean forceAlphaToOne = false;

        switch (textureFormat) {
            case Texture.INTENSITY:
                internalFormat = GL2.GL_INTENSITY;
                break;
            case Texture.LUMINANCE:
                internalFormat = GL.GL_LUMINANCE;
                break;
            case Texture.ALPHA:
                internalFormat = GL.GL_ALPHA;
                break;
            case Texture.LUMINANCE_ALPHA:
                internalFormat = GL.GL_LUMINANCE_ALPHA;
                break;
            case Texture.RGB:
                internalFormat = GL.GL_RGB;
                break;
            case Texture.RGBA:
                internalFormat = GL.GL_RGBA;
                break;
            default:
                assert false;
        }

        if (useAutoMipMap) {
            gl.glTexParameteri(target, GL2.GL_GENERATE_MIPMAP, GL.GL_TRUE);
        }
        else {
            gl.glTexParameteri(target, GL2.GL_GENERATE_MIPMAP, GL.GL_FALSE);
        }

        if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_BUFFER)) {

            switch (imageFormat) {
                case ImageComponentRetained.TYPE_BYTE_BGR:
                    format = GL2.GL_BGR;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    format = GL.GL_RGB;
                    break;
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                    if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
                        format = GL2.GL_ABGR_EXT;
                    } else {
                        assert false;
                        return;
                    }
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    // all RGB types are stored as RGBA
                    format = GL.GL_RGBA;
                    break;
                case ImageComponentRetained.TYPE_BYTE_LA:
                    // all LA types are stored as LA8
                    format = GL.GL_LUMINANCE_ALPHA;
                    break;
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                    if (internalFormat == GL.GL_ALPHA) {
                        format = GL.GL_ALPHA;
                    } else  {
                        format = GL.GL_LUMINANCE;
                    }
                    break;
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_INT_BGR:
                case ImageComponentRetained.TYPE_INT_RGB:
                case ImageComponentRetained.TYPE_INT_ARGB:
                default:
                    assert false;
                    return;
            }

            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) {

                gl.glTexImage2D(target, level, internalFormat,
                        width, height, boundaryWidth,
                        format, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[])data));
            } else {
                gl.glTexImage2D(target, level, internalFormat,
                        width, height, boundaryWidth,
                        format, GL.GL_UNSIGNED_BYTE, (Buffer) data);
            }

        } else if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_BUFFER)) {

            switch (imageFormat) {
                /* GL_BGR */
                case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                    format = GL.GL_RGBA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    forceAlphaToOne = true;
                    break;
                case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                    forceAlphaToOne = true;
                    /* Fall through to next case */
                case ImageComponentRetained.TYPE_INT_ARGB:
                    format = GL2.GL_BGRA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    break;
                    /* This method only supports 3 and 4 components formats and INT types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_BYTE_BGR:
                case ImageComponentRetained.TYPE_BYTE_RGB:
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                default:
                    assert false;
                    return;
            }

            /* Force Alpha to 1.0 if needed */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
            }

            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) {
                gl.glTexImage2D(target, level, internalFormat,
                        width, height, boundaryWidth,
                        format, type, IntBuffer.wrap((int[])data));
            } else {
                gl.glTexImage2D(target, level, internalFormat,
                        width, height, boundaryWidth,
                        format, type, (Buffer) data);
            }

            /* Restore Alpha scale and bias */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
            }
        } else {
            assert false;
        }
    }

    private void updateTexture2DSubImage(Context ctx,
            int target,
            int level, int xoffset, int yoffset,
            int textureFormat, int imageFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            int dataType, Object data) {
		GL2 gl = context(ctx).getGL().getGL2();

        int format = 0, internalFormat=0;
        int numBytes = 0;
        int type = GL2.GL_UNSIGNED_INT_8_8_8_8;
        boolean forceAlphaToOne = false;
        boolean pixelStore = false;

        if (imgXOffset > 0 || (width < tilew)) {
            pixelStore = true;
            gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, tilew);
        }

        switch (textureFormat) {
            case Texture.INTENSITY:
                internalFormat = GL2.GL_INTENSITY;
                break;
            case Texture.LUMINANCE:
                internalFormat = GL.GL_LUMINANCE;
                break;
            case Texture.ALPHA:
                internalFormat = GL.GL_ALPHA;
                break;
            case Texture.LUMINANCE_ALPHA:
                internalFormat = GL.GL_LUMINANCE_ALPHA;
                break;
            case Texture.RGB:
                internalFormat = GL.GL_RGB;
                break;
            case Texture.RGBA:
                internalFormat = GL.GL_RGBA;
                break;
            default:
                assert false;
        }

        if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_BUFFER)) {

            switch (imageFormat) {
                case ImageComponentRetained.TYPE_BYTE_BGR:
                    format = GL2.GL_BGR;
                    numBytes = 3;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    format = GL.GL_RGB;
                    numBytes = 3;
                    break;
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                    if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
                        format = GL2.GL_ABGR_EXT;
                        numBytes = 4;
                    } else {
                        assert false;
                        return;
                    }
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    // all RGB types are stored as RGBA
                    format = GL.GL_RGBA;
                    numBytes = 4;
                    break;
                case ImageComponentRetained.TYPE_BYTE_LA:
                    // all LA types are stored as LA8
                    format = GL.GL_LUMINANCE_ALPHA;
                    numBytes = 2;
                    break;
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                    if (internalFormat == GL.GL_ALPHA) {
                        format = GL.GL_ALPHA;
                        numBytes = 1;
                    } else  {
                        format = GL.GL_LUMINANCE;
                        numBytes = 1;
                    }
                    break;
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_INT_BGR:
                case ImageComponentRetained.TYPE_INT_RGB:
                case ImageComponentRetained.TYPE_INT_ARGB:
                default:
                    assert false;
                    return;
            }

            ByteBuffer buf = null;
            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) {
                buf = ByteBuffer.wrap((byte[]) data);
            }
            else {
                buf = (ByteBuffer) data;
            }

            // offset by the imageOffset
            buf.position((tilew * imgYOffset + imgXOffset) * numBytes);
            gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height,
                    format, GL.GL_UNSIGNED_BYTE, buf);

        } else if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_BUFFER)) {

            switch (imageFormat) {
                /* GL_BGR */
                case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                    format = GL.GL_RGBA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    forceAlphaToOne = true;
                    break;
                case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                    forceAlphaToOne = true;
                    /* Fall through to next case */
                case ImageComponentRetained.TYPE_INT_ARGB:
                    format = GL2.GL_BGRA;
                    type = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    break;
                    /* This method only supports 3 and 4 components formats and INT types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_BYTE_BGR:
                case ImageComponentRetained.TYPE_BYTE_RGB:
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                default:
                    assert false;
                    return;
            }
            /* Force Alpha to 1.0 if needed */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
            }

            IntBuffer buf = null;
            if(dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) {
                buf = IntBuffer.wrap((int[]) data);
            }
            else {
                buf = (IntBuffer) data;
            }

            // offset by the imageOffset
            buf.position(tilew * imgYOffset + imgXOffset);
            gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height,
                    format, type, buf);

            /* Restore Alpha scale and bias */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
            }
        } else {
            assert false;
            return;
        }

        if (pixelStore) {
            gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
        }

    }

    void updateTextureFilterModes(Context ctx,
            int target,
            int minFilter,
            int magFilter) {
        GL gl = context(ctx).getGL();

        if (EXTRA_DEBUGGING) {
            System.err.println("minFilter: " + getFilterName(minFilter) +
                    " magFilter: " + getFilterName(magFilter));
        }

        // FIXME: unclear whether we really need to set up the enum values
        // in the JoglContext as is done in the native code depending on
        // extension availability; maybe this is the defined fallback
        // behavior of the various Java3D modes

        // set texture min filter
        switch (minFilter) {
            case Texture.FASTEST:
            case Texture.BASE_LEVEL_POINT:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
                break;
            case Texture.BASE_LEVEL_LINEAR:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                break;
            case Texture.MULTI_LEVEL_POINT:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER,
                        GL.GL_NEAREST_MIPMAP_NEAREST);
                break;
            case Texture.NICEST:
            case Texture.MULTI_LEVEL_LINEAR:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER,
                        GL.GL_LINEAR_MIPMAP_LINEAR);
                break;
            case Texture.FILTER4:
// We should never get here as we've disabled the FILTER4 feature
//                gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER,
//                        GL.GL_FILTER4_SGIS);
                break;
        }

        // set texture mag filter
        switch (magFilter) {
            case Texture.FASTEST:
            case Texture.BASE_LEVEL_POINT:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
                break;
            case Texture.NICEST:
            case Texture.BASE_LEVEL_LINEAR:
                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                break;
            case Texture.LINEAR_SHARPEN:
// We should never get here as we've disabled the TEXTURE_SHARPEN feature
//                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_SHARPEN_SGIS);
                break;
            case Texture.LINEAR_SHARPEN_RGB:
// We should never get here as we've disabled the TEXTURE_SHARPEN feature
//                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_SHARPEN_COLOR_SGIS);
                break;
            case Texture.LINEAR_SHARPEN_ALPHA:
// We should never get here as we've disabled the TEXTURE_SHARPEN feature
//                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_SHARPEN_ALPHA_SGIS);
                break;
            case Texture2D.LINEAR_DETAIL:
// We should never get here as we've disabled the TEXTURE_DETAIL feature
//            	gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_DETAIL_SGIS);
                break;
            case Texture2D.LINEAR_DETAIL_RGB:
// We should never get here as we've disabled the TEXTURE_DETAIL feature
//            	gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_DETAIL_COLOR_SGIS);
                break;
            case Texture2D.LINEAR_DETAIL_ALPHA:
// We should never get here as we've disabled the TEXTURE_DETAIL feature
//            	gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_LINEAR_DETAIL_ALPHA_SGIS);
                break;
            case Texture.FILTER4:
// We should never get here as we've disabled the FILTER4 feature
//                gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
//                        GL.GL_FILTER4_SGIS);
                break;
        }
    }

    void updateTextureBoundary(Context ctx,
            int target,
            int boundaryModeS,
            int boundaryModeT,
            int boundaryModeR,
            float boundaryRed,
            float boundaryGreen,
            float boundaryBlue,
            float boundaryAlpha) {
        GL gl = context(ctx).getGL();

        // set texture wrap parameter
        switch (boundaryModeS) {
            case Texture.WRAP:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
                break;
            case Texture.CLAMP:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP);
                break;
            case Texture.CLAMP_TO_EDGE:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S,
                        GL.GL_CLAMP_TO_EDGE);
                break;
            case Texture.CLAMP_TO_BOUNDARY:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S,
                        GL2.GL_CLAMP_TO_BORDER);
                break;
        }

        switch (boundaryModeT) {
            case Texture.WRAP:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
                break;
            case Texture.CLAMP:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP);
                break;
            case Texture.CLAMP_TO_EDGE:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T,
                        GL.GL_CLAMP_TO_EDGE);
                break;
            case Texture.CLAMP_TO_BOUNDARY:
                gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T,
                        GL2.GL_CLAMP_TO_BORDER);
                break;
        }

        // applies to Texture3D only
        if (boundaryModeR != -1) {
            switch (boundaryModeR) {
                case Texture.WRAP:
                    gl.glTexParameteri(target,
                            GL2.GL_TEXTURE_WRAP_R, GL.GL_REPEAT);
                    break;

                case Texture.CLAMP:
                    gl.glTexParameteri(target,
                            GL2.GL_TEXTURE_WRAP_R, GL2.GL_CLAMP);
                    break;
                case Texture.CLAMP_TO_EDGE:
                    gl.glTexParameteri(target,
                            GL2.GL_TEXTURE_WRAP_R,
                            GL.GL_CLAMP_TO_EDGE);
                    break;
                case Texture.CLAMP_TO_BOUNDARY:
                    gl.glTexParameteri(target,
                            GL2.GL_TEXTURE_WRAP_R,
                            GL2.GL_CLAMP_TO_BORDER);
                    break;
            }
        }

        if (boundaryModeS == Texture.CLAMP ||
                boundaryModeT == Texture.CLAMP ||
                boundaryModeR == Texture.CLAMP) {
            // set texture border color
            float[] color = new float[4];
            color[0] = boundaryRed;
            color[1] = boundaryGreen;
            color[2] = boundaryBlue;
            color[3] = boundaryAlpha;
            gl.glTexParameterfv(target, GL2.GL_TEXTURE_BORDER_COLOR, color, 0);
        }
    }

    private static final String getFilterName(int filter) {
        switch (filter) {
            case Texture.FASTEST:
                return "Texture.FASTEST";
            case Texture.NICEST:
                return "Texture.NICEST";
            case Texture.BASE_LEVEL_POINT:
                return "Texture.BASE_LEVEL_POINT";
            case Texture.BASE_LEVEL_LINEAR:
                return "Texture.BASE_LEVEL_LINEAR";
            case Texture.MULTI_LEVEL_POINT:
                return "Texture.MULTI_LEVEL_POINT";
            case Texture.MULTI_LEVEL_LINEAR:
                return "Texture.MULTI_LEVEL_LINEAR";
            case Texture.FILTER4:
                return "Texture.FILTER4";
            case Texture.LINEAR_SHARPEN:
                return "Texture.LINEAR_SHARPEN";
            case Texture.LINEAR_SHARPEN_RGB:
                return "Texture.LINEAR_SHARPEN_RGB";
            case Texture.LINEAR_SHARPEN_ALPHA:
                return "Texture.LINEAR_SHARPEN_ALPHA";
            case Texture2D.LINEAR_DETAIL:
                return "Texture.LINEAR_DETAIL";
            case Texture2D.LINEAR_DETAIL_RGB:
                return "Texture.LINEAR_DETAIL_RGB";
            case Texture2D.LINEAR_DETAIL_ALPHA:
                return "Texture.LINEAR_DETAIL_ALPHA";
            default:
                return "(unknown)";
        }
    }

    private void updateTextureSharpenFunc(Context ctx,
            int target,
            int numPts,
            float[] pts) {
        // checking of the availability of sharpen texture functionality
        // is already done in shared code
// FIXME: GL_SGIS_sharpen_texture
//        GL gl = context(ctx).getGL();
//        gl.glSharpenTexFuncSGIS(target, numPts, pts, 0);
    }

    private void updateTextureFilter4Func(Context ctx,
            int target,
            int numPts,
            float[] pts) {
        // checking of the availability of filter4 functionality
        // is already done in shared code
// FIXME: GL_SGIS_texture_filter4
//        GL gl = context(ctx).getGL();
//        gl.glTexFilterFuncSGIS(target, GL.GL_FILTER4_SGIS,
//                numPts, pts, 0);
    }

    // mapping from java enum to gl enum
    private static final int[] _gl_textureCubeMapFace = {
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_X,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_X,
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Y,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y,
        GL.GL_TEXTURE_CUBE_MAP_POSITIVE_Z,
        GL.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z,
    };

    // ---------------------------------------------------------------------

    //
    // MasterControl methods
    //

    // Maximum lights supported by the native API
    @Override
    int getMaximumLights() {
        if (VERBOSE) System.err.println("JoglPipeline.getMaximumLights()");

        // FIXME: this isn't quite what the NativePipeline returns but
        // is probably close enough
        return 8;
    }


    // ---------------------------------------------------------------------

    //
    // Canvas3D methods - native wrappers
    //

    // Mac/JRE 7; called from Renderer when resizing is dedected
    // Implementation follows the approach in jogamp.opengl.GLDrawableHelper.resizeOffscreenDrawable(..)
    @Override
    void resizeOffscreenLayer(Canvas3D cv, int cvWidth, int cvHeight) {
		if (!isOffscreenLayerSurfaceEnabled(cv))
			return;

		JoglDrawable joglDrawable = (JoglDrawable)cv.drawable;
		if (!hasFBObjectSizeChanged(joglDrawable, cvWidth, cvHeight))
			return;

		int newWidth = Math.max(1, cvWidth);
		int newHeight = Math.max(1, cvHeight);

		GLDrawable glDrawble = joglDrawable.getGLDrawable();
		GLContext glContext = context(cv.ctx);

		// Assuming glContext != null

		final NativeSurface surface = glDrawble.getNativeSurface();
	    final ProxySurface proxySurface = (surface instanceof ProxySurface) ? (ProxySurface)surface : null;

		final int lockRes = surface.lockSurface();

		try {
			// propagate new size - seems not relevant here
			if (proxySurface != null) {
				final UpstreamSurfaceHook ush = proxySurface.getUpstreamSurfaceHook();
				if (ush instanceof UpstreamSurfaceHook.MutableSize) {
					((UpstreamSurfaceHook.MutableSize)ush).setSize(newWidth, newHeight);
				}
			}
			/*else if(DEBUG) { // we have to assume surface contains the new size already, hence size check @ bottom
	              System.err.println("GLDrawableHelper.resizeOffscreenDrawable: Drawable's offscreen surface n.a. ProxySurface, but "+ns.getClass().getName()+": "+ns);
			}*/

			GL2 gl = glContext.getGL().getGL2();

			// FBO : should be the default case on Mac OS X
			if (glDrawble instanceof GLFBODrawable) {

				// Resize GLFBODrawable
				// TODO msaa gets lost
//				((GLFBODrawable)glDrawble).resetSize(gl);

				// Alternative: resize GL_BACK FBObject directly,
				// if multisampled the FBO sink (GL_FRONT) will be resized before the swap is executed
				int numSamples = ((GLFBODrawable)glDrawble).getChosenGLCapabilities().getNumSamples();
				FBObject fboObjectBack = ((GLFBODrawable)glDrawble).getFBObject( GL.GL_BACK );
				fboObjectBack.reset(gl, newWidth, newHeight, numSamples, false); // false = don't reset SamplingSinkFBO immediately
				fboObjectBack.bind(gl);

				// If double buffered without antialiasing the GL_FRONT FBObject
				// will be resized by glDrawble after the next swap-call
			}
			// pbuffer - not tested because Mac OS X 10.7+ supports FBO
			else {
				// Create new GLDrawable (pbuffer) and update the coresponding GLContext

				final GLContext currentContext = GLContext.getCurrent();
				final GLDrawableFactory factory = glDrawble.getFactory();

				// Ensure to sync GL command stream
				if (currentContext != glContext) {
					glContext.makeCurrent();
				}
	         	gl.glFinish();
	         	glContext.release();

	         	if (proxySurface != null) {
	         		proxySurface.enableUpstreamSurfaceHookLifecycle(false);
	         	}

	         	try {
	         		glDrawble.setRealized(false);
	         		// New GLDrawable
	         		glDrawble = factory.createGLDrawable(surface);
	         		glDrawble.setRealized(true);

	         		joglDrawable.setGLDrawable(glDrawble);
	         	}
	         	finally {
	         		if (proxySurface != null) {
	         			proxySurface.enableUpstreamSurfaceHookLifecycle(true);
	         		}
	         	}

	         	glContext.setGLDrawable(glDrawble, true); // re-association

	         	// make current last current context
	         	if (currentContext != null) {
	         		currentContext.makeCurrent();
	         	}
			}
		}
		finally {
			surface.unlockSurface();
		}
    }

    // This is the native method for creating the underlying graphics context.
    @Override
    Context createNewContext(Canvas3D cv, Drawable drawable,
            Context shareCtx, boolean isSharedCtx,
            boolean offScreen) {
        if (VERBOSE) System.err.println("JoglPipeline.createNewContext()");

	    GLDrawable	glDrawable 	= null;
	    GLContext 	glContext	= null;

	    if (offScreen) {
	    	glDrawable = drawable(cv.drawable); // cv.drawable != null, set in 'createOffScreenBuffer'
			glContext = glDrawable.createContext(context(shareCtx));
        }
	    else {
	    	// determined in 'getBestConfiguration'
			GraphicsConfigInfo gcInf0 = Canvas3D.graphicsConfigTable.get(cv.graphicsConfiguration);
			AWTGraphicsConfiguration awtConfig = (AWTGraphicsConfiguration)gcInf0.getPrivateData();

		    // JAWTWindow
			JAWTWindow nativeWindow = (JAWTWindow)NativeWindowFactory.getNativeWindow(cv, awtConfig);
			nativeWindow.lockSurface();
		    try {
	    		glDrawable = GLDrawableFactory.getFactory(profile).createGLDrawable(nativeWindow);
	    		glContext = glDrawable.createContext(context(shareCtx));
		    }
		    finally {
		    	nativeWindow.unlockSurface();
		    }

	    	cv.drawable = new JoglDrawable(glDrawable, nativeWindow);
        }

        // assuming that this only gets called after addNotify has been called
    	glDrawable.setRealized(true);

    	// Apparently we are supposed to make the context current at this point
        // and set up a bunch of properties

        // Work around for some low end graphics driver bug, such as Intel Chipset.
        // Issue 324 : Lockup J3D program and throw exception using JOGL renderer
        boolean failed = false;
        int failCount = 0;
        int MAX_FAIL_COUNT = 5;
        do {
            failed = false;
            int res = glContext.makeCurrent();
            if (res == GLContext.CONTEXT_NOT_CURRENT) {
                // System.err.println("makeCurrent fail : " + failCount);
                failed = true;
                ++failCount;
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                }
            }
        } while (failed && (failCount < MAX_FAIL_COUNT));

        if (failCount == MAX_FAIL_COUNT) {
            throw new IllegalRenderingStateException("Unable to make new context current after " + failCount + "tries");
        }

        GL2 gl = glContext.getGL().getGL2();

        JoglContext ctx = new JoglContext(glContext);

        try {
            if (!getPropertiesFromCurrentContext(ctx, gl)) {
                throw new IllegalRenderingStateException("Unable to fetch properties from current OpenGL context");
            }

            if(!isSharedCtx){
                // Set up fields in Canvas3D
                setupCanvasProperties(cv, ctx, gl);
            }

            // Enable rescale normal
            gl.glEnable(GL2.GL_RESCALE_NORMAL);

            gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE);
            gl.glDepthFunc(GL.GL_LEQUAL);
            gl.glEnable(GL2.GL_COLOR_MATERIAL);

            /*
            OpenGL specs:
               glReadBuffer specifies a color buffer as the source for subsequent glReadPixels.
               This source mode is initially GL_FRONT in single-buffered and GL_BACK in double-buffered configurations.

            We leave this mode unchanged in on-screen rendering and adjust it in off-screen rendering. See below.
            */
//          gl.glReadBuffer(GL_FRONT); 		// off window, default for single-buffered non-stereo window

            // Issue 417: JOGL: Mip-mapped NPOT textures rendered incorrectly
            // J3D images are aligned to 1 byte
            gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

            // Workaround for issue 400: Enable separate specular by default
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);


            // Mac OS X / JRE 7 : onscreen rendering = offscreen rendering
            // bind FBO
            if (!offScreen && glDrawable instanceof GLFBODrawable) {
            	GLFBODrawable fboDrawable = (GLFBODrawable)glDrawable;
            	// bind GLFBODrawable's drawing FBObject
            	// GL_BACK returns the correct FBOObject for single/double buffering, incl. multisampling
            	fboDrawable.getFBObject( GL.GL_BACK ).bind(gl);
            }


            // FBO or pbuffer
            if (offScreen) {

            	// Final caps
            	GLCapabilitiesImmutable chosenCaps = glDrawable.getChosenGLCapabilities();

            	// FBO
            	if (glDrawable instanceof GLFBODrawable) {
                	GLFBODrawable fboDrawable = (GLFBODrawable)glDrawable;
                	// bind GLFBODrawable's drawing FBObject
                	// GL_BACK returns the correct FBOObject for single/double buffering, incl. multisampling
                	fboDrawable.getFBObject( GL.GL_BACK ).bind(gl);
            	}
            	// pbuffer
            	else {
	            	// Double buffering: read from back buffer, as we don't swap
					// Even this setting is identical to the initially mode it is set explicitly
					if (chosenCaps.getDoubleBuffered()) {
						gl.glReadBuffer(GL.GL_BACK);
					}
					else {
						gl.glReadBuffer(GL.GL_FRONT);
					}
            	}
            }
        }
        finally {
 			glContext.release();
        }

        return ctx;
    }

    @Override
    void createQueryContext(Canvas3D cv, Drawable drawable,
            boolean offScreen, int width, int height) {
        if (VERBOSE) System.err.println("JoglPipeline.createQueryContext()");

        // Assumes createQueryContext is never called for a drawable != null

        if (offScreen) {

         	Drawable offDrawable = createOffScreenBuffer(cv, null, width, height);

         	GLDrawable glDrawable = drawable(offDrawable);

        	glDrawable.setRealized(true);

    		GLContext glContext = glDrawable.createContext(null);
            glContext.makeCurrent();

            JoglContext ctx = new JoglContext(glContext);

            GL2 gl = glContext.getGL().getGL2();

            // get current context properties
            getPropertiesFromCurrentContext(ctx, gl);
            // Set up fields in Canvas3D
            setupCanvasProperties(cv, ctx, gl);

            // Done !

            glContext.release();
    		glContext.destroy();
    		glDrawable.setRealized(false);
    	}
        else {

        	// TODO can't find an implementation which avoids the use of QueryCanvas
        	// JOGL requires a visible Frame for an onscreen context

        Frame f = new Frame();
        f.setUndecorated(true);
        f.setLayout(new BorderLayout());

        ContextQuerier querier = new ContextQuerier(cv);

		    AWTGraphicsConfiguration awtConfig =
		    		(AWTGraphicsConfiguration)Canvas3D.graphicsConfigTable.get(cv.graphicsConfiguration).getPrivateData();

		    QueryCanvas canvas = new QueryCanvas(awtConfig, querier);

        f.add(canvas, BorderLayout.CENTER);
        f.setSize(MIN_FRAME_SIZE, MIN_FRAME_SIZE);
        f.setVisible(true);
        canvas.doQuery();
        // Attempt to wait for the frame to become visible, but don't block the EDT
        if (!EventQueue.isDispatchThread()) {
            synchronized(querier) {
                if (!querier.done()) {
                    try {
                        querier.wait(WAIT_TIME);
	                    }
	                    catch (InterruptedException e) {
                    }
                }
            }
        }

        disposeOnEDT(f);
    }
    }

    // This is the native for creating an offscreen buffer
    @Override
    Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, int width, int height) {
        if (VERBOSE) System.err.println("JoglPipeline.createOffScreenBuffer()");

        // ctx unused, doesn't exist yet

        // Offscreen Canvas3D's JoglGraphicsConfiguration
        JoglGraphicsConfiguration jgc = (JoglGraphicsConfiguration)cv.graphicsConfiguration;

        // Retrieve the offscreen Canvas3D's GraphicsConfigInfo
		GraphicsConfigInfo gcInf0 = Canvas3D.graphicsConfigTable.get(jgc);

		// Offscreen Canvas3D's graphics configuration, determined in 'getBestConfiguration'
		AWTGraphicsConfiguration awtConfig = (AWTGraphicsConfiguration)gcInf0.getPrivateData();

		// TODO Offscreen Canvas3D's graphics devise, determined in 'getBestConfiguration'
		//AbstractGraphicsDevice device = awtConfig.getScreen().getDevice();			// throws exception
		// Alternative: default graphics device
        AbstractGraphicsDevice device = GLDrawableFactory.getDesktopFactory().getDefaultDevice();

		// Offscreen Canvas3D's capabilites, determined in 'getBestConfiguration'
		GLCapabilities canvasCaps = (GLCapabilities)awtConfig.getChosenCapabilities();

		// For further investigations : the user's GraphicsConfigTemplate3D (not used yet)
		GraphicsConfigTemplate3D gct3D = gcInf0.getGraphicsConfigTemplate3D();


        // Assuming that the offscreen drawable will/can support the chosen GLCapabilities
    	// of the offscreen Canvas3D

        final GLCapabilities offCaps = new GLCapabilities(profile);
        offCaps.copyFrom(canvasCaps);

        // double bufffering only if scene antialiasing is required/preferred and supported
        if (offCaps.getSampleBuffers() == false) {
        	offCaps.setDoubleBuffered(false);
        	offCaps.setNumSamples(0);
        }

        // Never stereo
        offCaps.setStereo(false);

        // Set preferred offscreen drawable : framebuffer object (FBO) or pbuffer
        offCaps.setFBO(true); // switches to pbuffer if FBO is not supported
		// caps.setPBuffer(true);

		// !! a 'null' capability chooser; JOGL doesn't call a chooser for offscreen drawable

		// If FBO : 'offDrawable' is of type javax.media.opengl.GLFBODrawable
        GLDrawable offDrawable = GLDrawableFactory.getFactory(profile).createOffscreenDrawable(device, offCaps, null, width, height);

// !! these chosen caps are not final as long as the corresponding context is made current
//System.out.println("createOffScreenBuffer chosenCaps = " + offDrawable.getChosenGLCapabilities());

        return new JoglDrawable(offDrawable, null);
    }

    // 'destroyContext' is called first if context exists
    @Override
    void destroyOffScreenBuffer(Canvas3D cv, Context ctx, Drawable drawable) {
        if (VERBOSE) System.err.println("JoglPipeline.destroyOffScreenBuffer()");

        // it is done in 'destroyContext'
    }

    // This is the native for reading the image from the offscreen buffer
    @Override
    void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int dataType, Object data, int width, int height) {
        if (VERBOSE) System.err.println("JoglPipeline.readOffScreenBuffer()");

    	GLDrawable 				glDrawable  = ((JoglDrawable)cv.drawable).getGLDrawable();
        GLCapabilitiesImmutable chosenCaps  = glDrawable.getChosenGLCapabilities();
        GLFBODrawable 			fboDrawable = null;

		GL2 gl = context(ctx).getGL().getGL2();

        // If FBO
        if (chosenCaps.isFBO()) {

        	fboDrawable = (GLFBODrawable)glDrawable;

        	if (chosenCaps.getDoubleBuffered()) {
	        	// swap = resolve multisampling or flip back/front FBO
	        	fboDrawable.swapBuffers();
	        	// unbind texture render target, we read from FBO
	        	gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        	}

        	// bind FBO for reading pixel data
        	// GL_FRONT = SamplingSinkFBO if double buffered and multisampled
        	// GL_FRONT if double buffered ( = GL_BAck before swap was called)
        	// GL_FRONT = GL_BACK if single buffered (single FBO)

        	fboDrawable.getFBObject( GL.GL_FRONT ).bind(gl);
        }
        // else pbuffer

        gl.glPixelStorei(GL2.GL_PACK_ROW_LENGTH, width);
        gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

        int type = 0;

        if((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_BYTE_BUFFER)) {

            switch (format) {
                // GL_BGR
                case ImageComponentRetained.TYPE_BYTE_BGR:
                    type = GL2.GL_BGR;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    type = GL.GL_RGB;
                    break;
                    // GL_ABGR_EXT
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                    if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If false, should never come here!
                        type = GL2.GL_ABGR_EXT;
                    } else {
                        assert false;
                        return;
                    }
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    type = GL.GL_RGBA;
                    break;

                    /* This method only supports 3 and 4 components formats and BYTE types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_INT_BGR:
                case ImageComponentRetained.TYPE_INT_RGB:
                case ImageComponentRetained.TYPE_INT_ARGB:
                default:
                    throw new AssertionError("illegal format " + format);
            }

            gl.glReadPixels(0, 0, width, height, type, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap((byte[]) data));

        }
        else if ((dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_ARRAY) ||
                (dataType == ImageComponentRetained.IMAGE_DATA_TYPE_INT_BUFFER)) {

            int intType = GL2.GL_UNSIGNED_INT_8_8_8_8;
            boolean forceAlphaToOne = false;

            switch (format) {
                /* GL_BGR */
                case ImageComponentRetained.TYPE_INT_BGR: /* Assume XBGR format */
                    type = GL.GL_RGBA;
                    intType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    forceAlphaToOne = true;
                    break;
                case ImageComponentRetained.TYPE_INT_RGB: /* Assume XRGB format */
                    forceAlphaToOne = true;
                    /* Fall through to next case */
                case ImageComponentRetained.TYPE_INT_ARGB:
                    type = GL2.GL_BGRA;
                    intType = GL2.GL_UNSIGNED_INT_8_8_8_8_REV;
                    break;
                    /* This method only supports 3 and 4 components formats and BYTE types. */
                case ImageComponentRetained.TYPE_BYTE_LA:
                case ImageComponentRetained.TYPE_BYTE_GRAY:
                case ImageComponentRetained.TYPE_USHORT_GRAY:
                case ImageComponentRetained.TYPE_BYTE_BGR:
                case ImageComponentRetained.TYPE_BYTE_RGB:
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                case ImageComponentRetained.TYPE_BYTE_ABGR:
                default:
                    throw new AssertionError("illegal format " + format);
            }

            /* Force Alpha to 1.0 if needed */
            if(forceAlphaToOne) {
                gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 0.0f);
                gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 1.0f);
            }

            gl.glReadPixels(0, 0, width, height, type, intType, IntBuffer.wrap((int[]) data));

	    /* Restore Alpha scale and bias */
	    if(forceAlphaToOne) {
		gl.glPixelTransferf(GL2.GL_ALPHA_SCALE, 1.0f);
		gl.glPixelTransferf(GL2.GL_ALPHA_BIAS, 0.0f);
	    }
        }
        else {
            throw new AssertionError("illegal image data type " + dataType);
        }

        // If FBO
        if (chosenCaps.isFBO()) {
        	// bind FBO for drawing
        	fboDrawable.getFBObject( GL.GL_BACK ).bind(gl);
        }
    }

	// The native method for swapBuffers - onscreen only
@Override
void swapBuffers(Canvas3D cv, Context ctx, Drawable drawable) {
	if (VERBOSE) System.err.println("JoglPipeline.swapBuffers()");
	GLDrawable draw = drawable(drawable);
	draw.swapBuffers();
}

    // native method for setting Material when no material is present
    @Override
    void updateMaterialColor(Context ctx, float r, float g, float b, float a) {
        if (VERBOSE) System.err.println("JoglPipeline.updateMaterialColor()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glColor4f(r, g, b, a);
        gl.glDisable(GL2.GL_LIGHTING);
    }

    @Override
    void destroyContext(Drawable drawable, Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.destroyContext()");

        JoglDrawable joglDrawable =	(JoglDrawable)drawable;
        GLContext  context  = context(ctx);

        if (GLContext.getCurrent() == context) {
            context.release();
        }
        context.destroy();

        // assuming this is the right point at which to make this call
        joglDrawable.getGLDrawable().setRealized(false);

        joglDrawable.destroyNativeWindow();
    }

    // This is the native method for doing accumulation.
    @Override
    void accum(Context ctx, float value) {
        if (VERBOSE) System.err.println("JoglPipeline.accum()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glReadBuffer(GL.GL_BACK);
        gl.glAccum(GL2.GL_ACCUM, value);
        gl.glReadBuffer(GL.GL_FRONT);
    }

    // This is the native method for doing accumulation return.
    @Override
    void accumReturn(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.accumReturn()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glAccum(GL2.GL_RETURN, 1.0f);
    }

    // This is the native method for clearing the accumulation buffer.
    @Override
    void clearAccum(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.clearAccum()");

        GL gl = context(ctx).getGL();
        gl.glClear(GL2.GL_ACCUM_BUFFER_BIT);
    }

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    @Override
    int getNumCtxLights(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.getNumCtxLights()");

        GL gl = context(ctx).getGL();
        int[] res = new int[1];
        gl.glGetIntegerv(GL2.GL_MAX_LIGHTS, res, 0);
        return res[0];
    }

    // Native method for decal 1st child setup
    @Override
    boolean decal1stChildSetup(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.decal1stChildSetup()");

        GL gl = context(ctx).getGL();
        gl.glEnable(GL.GL_STENCIL_TEST);
        gl.glClearStencil(0x0);
        gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
        gl.glStencilFunc(GL.GL_ALWAYS, 0x1, 0x1);
        gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
        if (gl.glIsEnabled(GL.GL_DEPTH_TEST))
            return true;
        else
            return false;
    }

    // Native method for decal nth child setup
    @Override
    void decalNthChildSetup(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.decalNthChildSetup()");

        GL gl = context(ctx).getGL();
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glStencilFunc(GL.GL_EQUAL, 0x1, 0x1);
        gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
    }

    // Native method for decal reset
    @Override
    void decalReset(Context ctx, boolean depthBufferEnable) {
        if (VERBOSE) System.err.println("JoglPipeline.decalReset()");

        GL gl = context(ctx).getGL();
        gl.glDisable(GL.GL_STENCIL_TEST);
        if (depthBufferEnable)
            gl.glEnable(GL.GL_DEPTH_TEST);
    }

    // Native method for eye lighting
    @Override
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
        if (VERBOSE) System.err.println("JoglPipeline.ctxUpdateEyeLightingEnable()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (localEyeLightingEnable) {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
        } else {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_FALSE);
        }
    }

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    @Override
    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
        if (VERBOSE) System.err.println("JoglPipeline.setBlendColor()");

		GL2 gl = context(ctx).getGL().getGL2();
        if (gl.isExtensionAvailable("GL_ARB_imaging")) {
            gl.glBlendColor(red, green, blue, alpha);
        }
    }

    // native method for setting blend func
    @Override
    void setBlendFunc(Context ctx, int srcBlendFunction, int dstBlendFunction) {
        if (VERBOSE) System.err.println("JoglPipeline.setBlendFunc()");

        GL gl = context(ctx).getGL();
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(blendFunctionTable[srcBlendFunction],
                blendFunctionTable[dstBlendFunction]);
    }

    // native method for setting fog enable flag
    @Override
    void setFogEnableFlag(Context ctx, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.setFogEnableFlag()");

        GL gl = context(ctx).getGL();

        if (enable)
            gl.glEnable(GL2.GL_FOG);
        else
            gl.glDisable(GL2.GL_FOG);
    }

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
    @Override
    void setFullSceneAntialiasing(Context absCtx, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.setFullSceneAntialiasing()");

        JoglContext ctx = (JoglContext) absCtx;
        GL gl = context(ctx).getGL();
        if (ctx.getHasMultisample() && !VirtualUniverse.mc.implicitAntialiasing) {
            if (enable) {
                gl.glEnable(GL.GL_MULTISAMPLE);
            } else {
                gl.glDisable(GL.GL_MULTISAMPLE);
            }
        }
    }

    @Override
    void setGlobalAlpha(Context ctx, float alpha) {
        if (VERBOSE) System.err.println("JoglPipeline.setGlobalAlpha()");
// FIXME: SUN_global_alpha
//        GL gl = context(ctx).getGL();
//        if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
//            gl.glEnable(GL.GL_GLOBAL_ALPHA_SUN);
//            gl.glGlobalAlphaFactorfSUN(alpha);
//        }
    }

    // Native method to update separate specular color control
    @Override
    void updateSeparateSpecularColorEnable(Context ctx, boolean enable) {
        if (VERBOSE) System.err.println("JoglPipeline.updateSeparateSpecularColorEnable()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (enable) {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
        } else {
            gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SINGLE_COLOR);
        }
    }

    // True under Solaris,
    // False under windows when display mode <= 8 bit
    @Override
    boolean validGraphicsMode() {
        if (VERBOSE) System.err.println("JoglPipeline.validGraphicsMode()");

        // FIXME: believe this should do exactly what the native code
        // used to, but not 100% sure (also in theory should only run
        // this code on the Windows platform? What about Mac OS X?)
        DisplayMode currentMode =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
        // Note: on X11 platforms, a bit depth < 0 simply indicates that
        // multiple visuals are supported on the current display mode

        if (VERBOSE) System.err.println("  Returning " + (currentMode.getBitDepth() < 0 ||
                currentMode.getBitDepth() > 8));

        return (currentMode.getBitDepth() < 0 ||
                currentMode.getBitDepth() > 8);
    }

    // native method for setting light enables
    @Override
    void setLightEnables(Context ctx, long enableMask, int maxLights) {
        if (VERBOSE) System.err.println("JoglPipeline.setLightEnables()");

        GL gl = context(ctx).getGL();

        for (int i = 0; i < maxLights; i++) {
            if ((enableMask & (1 << i)) != 0) {
                gl.glEnable(GL2.GL_LIGHT0 + i);
            } else {
                gl.glDisable(GL2.GL_LIGHT0 + i);
            }
        }
    }

    // native method for setting scene ambient
    @Override
    void setSceneAmbient(Context ctx, float red, float green, float blue) {
        if (VERBOSE) System.err.println("JoglPipeline.setSceneAmbient()");

		GL2 gl = context(ctx).getGL().getGL2();

        float[] color = new float[4];
        color[0] = red;
        color[1] = green;
        color[2] = blue;
        color[3] = 1.0f;
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, color, 0);
    }

    // native method for disabling fog
    @Override
    void disableFog(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.disableFog()");

        GL gl = context(ctx).getGL();
        gl.glDisable(GL2.GL_FOG);
    }

    // native method for disabling modelClip
    @Override
    void disableModelClip(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.disableModelClip()");

        GL gl = context(ctx).getGL();

        gl.glDisable(GL2.GL_CLIP_PLANE0);
        gl.glDisable(GL2.GL_CLIP_PLANE1);
        gl.glDisable(GL2.GL_CLIP_PLANE2);
        gl.glDisable(GL2.GL_CLIP_PLANE3);
        gl.glDisable(GL2.GL_CLIP_PLANE4);
        gl.glDisable(GL2.GL_CLIP_PLANE5);
    }

    // native method for setting default RenderingAttributes
    @Override
    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
        if (VERBOSE) System.err.println("JoglPipeline.resetRenderingAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (!depthBufferWriteEnableOverride) {
            gl.glDepthMask(true);
        }
        if (!depthBufferEnableOverride) {
            gl.glEnable(GL.GL_DEPTH_TEST);
        }
        gl.glAlphaFunc(GL.GL_ALWAYS, 0.0f);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glDisable(GL.GL_COLOR_LOGIC_OP);
    }

    // native method for setting default texture
    @Override
    void resetTextureNative(Context ctx, int texUnitIndex) {
        if (VERBOSE) System.err.println("JoglPipeline.resetTextureNative()");

		GL2 gl = context(ctx).getGL().getGL2();
        if (texUnitIndex >= 0 &&
                gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
            gl.glClientActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
        }

        gl.glDisable(GL2.GL_TEXTURE_1D);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_TEXTURE_3D);
        gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
    }

    // native method for activating a particular texture unit
    @Override
    void activeTextureUnit(Context ctx, int texUnitIndex) {
        if (VERBOSE) System.err.println("JoglPipeline.activeTextureUnit()");

		GL2 gl = context(ctx).getGL().getGL2();
        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
            gl.glClientActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
        }
    }

    // native method for setting default TexCoordGeneration
    @Override
    void resetTexCoordGeneration(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetTexCoordGeneration()");

        GL gl = context(ctx).getGL();
        gl.glDisable(GL2.GL_TEXTURE_GEN_S);
        gl.glDisable(GL2.GL_TEXTURE_GEN_T);
        gl.glDisable(GL2.GL_TEXTURE_GEN_R);
        gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
    }

    // native method for setting default TextureAttributes
    @Override
    void resetTextureAttributes(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetTextureAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        float[] color = new float[4];

        gl.glPushAttrib(GL2.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glLoadIdentity();
        gl.glPopAttrib();
        gl.glTexEnvfv(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_COLOR, color, 0);
        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

// FIXME: GL_NV_register_combiners
//        if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
//            gl.glDisable(GL.GL_REGISTER_COMBINERS_NV);
//        }

// FIXME: GL_SGI_texture_color_table
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
//            gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
//        }
    }

    // native method for setting default PolygonAttributes
    @Override
    void resetPolygonAttributes(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetPolygonAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        gl.glCullFace(GL.GL_BACK);
        gl.glEnable(GL.GL_CULL_FACE);

        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);

        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glPolygonOffset(0.0f, 0.0f);
        gl.glDisable(GL2.GL_POLYGON_OFFSET_POINT);
        gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
    }

    // native method for setting default LineAttributes
    @Override
    void resetLineAttributes(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetLineAttributes()");

        GL gl = context(ctx).getGL();
        gl.glLineWidth(1.0f);
        gl.glDisable(GL2.GL_LINE_STIPPLE);

        // XXXX: Polygon Mode check, blend enable
        gl.glDisable(GL.GL_LINE_SMOOTH);
    }

    // native method for setting default PointAttributes
    @Override
    void resetPointAttributes(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.resetPointAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glPointSize(1.0f);

        // XXXX: Polygon Mode check, blend enable
        gl.glDisable(GL2.GL_POINT_SMOOTH);
    }

    // native method for setting default TransparencyAttributes
    @Override
    void resetTransparency(Context ctx, int geometryType,
            int polygonMode, boolean lineAA,
            boolean pointAA) {
        if (VERBOSE) System.err.println("JoglPipeline.resetTransparency()");

        GL gl = context(ctx).getGL();

        if (((((geometryType & RenderMolecule.LINE) != 0) ||
                (polygonMode == PolygonAttributes.POLYGON_LINE))
                && lineAA) ||
                ((((geometryType & RenderMolecule.POINT) != 0) ||
                (polygonMode == PolygonAttributes.POLYGON_POINT))
                && pointAA)) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        } else {
            gl.glDisable(GL.GL_BLEND);
        }
        gl.glDisable(GL2.GL_POLYGON_STIPPLE);
    }

    // native method for setting default ColoringAttributes
    @Override
    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
        if (VERBOSE) System.err.println("JoglPipeline.resetColoringAttributes()");

		GL2 gl = context(ctx).getGL().getGL2();

        if (!enableLight) {
            gl.glColor4f(r, g, b, a);
        }
        gl.glShadeModel(GL2.GL_SMOOTH);
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    @Override
    void syncRender(Context ctx, boolean wait) {
        if (VERBOSE) System.err.println("JoglPipeline.syncRender()");

        GL gl = context(ctx).getGL();

        if (wait)
            gl.glFinish();
        else
            gl.glFlush();
    }

    // The native method that sets this ctx to be the current one
    @Override
    boolean useCtx(Context ctx, Drawable drawable) {
        if (VERBOSE) System.err.println("JoglPipeline.useCtx()");
        GLContext context = context(ctx);
        int res = context.makeCurrent();
        return (res != GLContext.CONTEXT_NOT_CURRENT);
    }

    // Optionally release the context. Returns true if the context was released.
    @Override
    boolean releaseCtx(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.releaseCtx()");
        GLContext context = context(ctx);
        context.release();
        return true;
    }

    @Override
    void clear(Context ctx, float r, float g, float b, boolean clearStencil) {
        if (VERBOSE) System.err.println("JoglPipeline.clear()");

        JoglContext jctx = (JoglContext) ctx;
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        // OBSOLETE CLEAR CODE
        /*
        gl.glClearColor(r, g, b, jctx.getAlphaClearValue());
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // Java 3D always clears the Z-buffer
        gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT);
        gl.glDepthMask(true);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glPopAttrib();

        // Issue 239 - clear stencil if specified
        if (clearStencil) {
            gl.glPushAttrib(GL.GL_STENCIL_BUFFER_BIT);
            gl.glClearStencil(0);
            gl.glStencilMask(~0);
            gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
            gl.glPopAttrib();
        }
        */

        // Mask of which buffers to clear, this always includes color & depth
        int clearMask = GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT;

        // Issue 239 - clear stencil if specified
        if (clearStencil) {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);

            gl.glClearStencil(0);
            gl.glStencilMask(~0);
            clearMask |= GL.GL_STENCIL_BUFFER_BIT;
        } else {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT);
        }

        gl.glDepthMask(true);
        gl.glClearColor(r, g, b, jctx.getAlphaClearValue());
        gl.glClear(clearMask);
        gl.glPopAttrib();

    }

    @Override
    void textureFillBackground(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, boolean useBilinearFilter)  {
        if (VERBOSE) System.err.println("JoglPipeline.textureFillBackground()");

        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        // Temporarily disable fragment and most 3D operations
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_POLYGON_BIT);

        disableAttribFor2D(gl);
        gl.glDepthMask(false);
        gl.glEnable(GL.GL_TEXTURE_2D);

        /* Setup filter mode if needed */
        if(useBilinearFilter) {
            // System.err.println("JoglPipeline - Background  : use bilinear filter\n");
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        }

        // reset the polygon mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

        // load identity modelview and projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(texMinU, texMinV); gl.glVertex2f(mapMinX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMinV); gl.glVertex2f(mapMaxX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMaxV); gl.glVertex2f(mapMaxX,mapMaxY);
        gl.glTexCoord2f(texMinU, texMaxV); gl.glVertex2f(mapMinX,mapMaxY);
        gl.glEnd();

        // Restore texture Matrix transform
        gl.glPopMatrix();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        // Restore attributes
        gl.glPopAttrib();

    }

    @Override
    void textureFillRaster(Context ctx, float texMinU, float texMaxU, float texMinV, float texMaxV,
            float mapMinX, float mapMaxX, float mapMinY, float mapMaxY, float mapZ, float alpha,
            boolean useBilinearFilter)  {

        if (VERBOSE) System.err.println("JoglPipeline.textureFillRaster()");

        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        // Temporarily disable fragment and most 3D operations
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL2.GL_POLYGON_BIT |
                GL2.GL_CURRENT_BIT );

        disableAttribForRaster(gl);

        /* Setup filter mode if needed */
        if(useBilinearFilter) {
            // System.err.println("JoglPipeline - Raster  : use bilinear filter\n");
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        }

        gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glColor4f(1.0f, 1.0f, 1.0f, alpha);

        // reset the polygon mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);

        // load identity modelview and projection matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0.0, 1.0, 0.0, 1.0, 0.0, 1.0);

        gl.glBegin(GL2.GL_QUADS);
        gl.glTexCoord2f(texMinU, texMinV); gl.glVertex3f(mapMinX,mapMinY, mapZ);
        gl.glTexCoord2f(texMaxU, texMinV); gl.glVertex3f(mapMaxX,mapMinY, mapZ);
        gl.glTexCoord2f(texMaxU, texMaxV); gl.glVertex3f(mapMaxX,mapMaxY, mapZ);
        gl.glTexCoord2f(texMinU, texMaxV); gl.glVertex3f(mapMinX,mapMaxY, mapZ);
        gl.glEnd();

        // Restore matrices
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        // Restore attributes
        gl.glPopAttrib();

    }

    @Override
    void executeRasterDepth(Context ctx, float posX, float posY, float posZ,
            int srcOffsetX, int srcOffsetY, int rasterWidth, int rasterHeight,
            int depthWidth, int depthHeight, int depthFormat, Object depthData) {
        if (VERBOSE) System.err.println("JoglPipeline.executeRasterDepth()");
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();


        gl.glRasterPos3f(posX, posY, posZ);

        int[] drawBuf = new int[1];
        gl.glGetIntegerv(GL2.GL_DRAW_BUFFER, drawBuf, 0);
        /* disable draw buffer */
        gl.glDrawBuffer(GL.GL_NONE);

        /*
         * raster position is upper left corner, default for Java3D
         * ImageComponent currently has the data reverse in Y
         */
        gl.glPixelZoom(1.0f, -1.0f);
        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, depthWidth);
        if (srcOffsetX >= 0) {
            gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, srcOffsetX);
            if (srcOffsetX + rasterWidth > depthWidth) {
                rasterWidth = depthWidth - srcOffsetX;
            }
        } else {
            rasterWidth += srcOffsetX;
            if (rasterWidth > depthWidth) {
                rasterWidth  = depthWidth;
            }
        }
        if (srcOffsetY >= 0) {
            gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, srcOffsetY);
            if (srcOffsetY + rasterHeight > depthHeight) {
                rasterHeight = depthHeight - srcOffsetY;
            }
        } else {
			rasterHeight += srcOffsetY;
			if (rasterHeight > depthHeight) {
				rasterHeight = depthHeight;
			}
        }


        if (depthFormat == DepthComponentRetained.DEPTH_COMPONENT_TYPE_INT) {
            gl.glDrawPixels(rasterWidth, rasterHeight, GL2.GL_DEPTH_COMPONENT,
                    GL.GL_UNSIGNED_INT, IntBuffer.wrap((int[]) depthData));
        } else { /* DepthComponentRetained.DEPTH_COMPONENT_TYPE_FLOAT */
            gl.glDrawPixels(rasterWidth, rasterHeight, GL2.GL_DEPTH_COMPONENT,
                    GL.GL_FLOAT, FloatBuffer.wrap((float[]) depthData));
        }

        /* re-enable draw buffer */
        gl.glDrawBuffer(drawBuf[0]);

        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, 0);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, 0);

    }

    // The native method for setting the ModelView matrix.
    @Override
    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
        if (VERBOSE) System.err.println("JoglPipeline.setModelViewMatrix()");
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_MODELVIEW);

        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glLoadTransposeMatrixd(viewMatrix, 0);
            gl.glMultTransposeMatrixd(modelMatrix, 0);
        } else {
            double[] v = new double[16];
            double[] m = new double[16];
            copyTranspose(viewMatrix, v);
            copyTranspose(modelMatrix, m);
            gl.glLoadMatrixd(v, 0);
            gl.glMultMatrixd(m, 0);
        }
    }

    // The native method for setting the Projection matrix.
    @Override
    void setProjectionMatrix(Context ctx, double[] projMatrix) {
        if (VERBOSE) System.err.println("JoglPipeline.setProjectionMatrix()");
        GLContext context = context(ctx);
		GL2 gl = context.getGL().getGL2();

        gl.glMatrixMode(GL2.GL_PROJECTION);

        if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            // Invert the Z value in clipping coordinates because OpenGL uses
            // left-handed clipping coordinates, while Java3D defines right-handed
            // coordinates everywhere.
            projMatrix[8] *= -1.0;
            projMatrix[9] *= -1.0;
            projMatrix[10] *= -1.0;
            projMatrix[11] *= -1.0;
            gl.glLoadTransposeMatrixd(projMatrix, 0);
            projMatrix[8] *= -1.0;
            projMatrix[9] *= -1.0;
            projMatrix[10] *= -1.0;
            projMatrix[11] *= -1.0;
        } else {
            double[] p = new double[16];
            copyTranspose(projMatrix, p);
            // Invert the Z value in clipping coordinates because OpenGL uses
            // left-handed clipping coordinates, while Java3D defines right-handed
            // coordinates everywhere.
            p[2] *= -1.0;
            p[6] *= -1.0;
            p[10] *= -1.0;
            p[14] *= -1.0;
            gl.glLoadMatrixd(p, 0);
        }
    }

static boolean isOffscreenLayerSurfaceEnabled(Canvas3D cv) {
	if (cv.drawable == null || cv.offScreen)
		return false;

	JoglDrawable joglDrawble = (JoglDrawable)cv.drawable;
	JAWTWindow jawtwindow = (JAWTWindow)joglDrawble.getNativeWindow();
	if (jawtwindow == null)
		return false;

	return jawtwindow.isOffscreenLayerSurfaceEnabled();
}

static boolean hasFBObjectSizeChanged(JoglDrawable jdraw, int width, int height) {
	if (!(jdraw.getGLDrawable() instanceof GLFBODrawable))
		return false;

	FBObject fboBack = ((GLFBODrawable)jdraw.getGLDrawable()).getFBObject(GL.GL_BACK);
	if (fboBack == null)
		return false;

	return (width != fboBack.getWidth() || height != fboBack.getHeight());
}


    // The native method for setting the Viewport.
    @Override
    void setViewport(Context ctx, int x, int y, int width, int height) {
        if (VERBOSE) System.err.println("JoglPipeline.setViewport()");
        GL gl = context(ctx).getGL();
        gl.glViewport(x, y, width, height);
    }

    // used for display Lists
    @Override
    void newDisplayList(Context ctx, int displayListId) {
        if (VERBOSE) System.err.println("JoglPipeline.newDisplayList()");
        if (displayListId <= 0) {
            System.err.println("JAVA 3D ERROR : glNewList(" + displayListId + ") -- IGNORED");
        }

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glNewList(displayListId, GL2.GL_COMPILE);
    }

    @Override
    void endDisplayList(Context ctx) {
        if (VERBOSE) System.err.println("JoglPipeline.endDisplayList()");
		GL2 gl = context(ctx).getGL().getGL2();
        gl.glEndList();
    }

    int numInvalidLists = 0;
    @Override
    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
        if (VERBOSE) System.err.println("JoglPipeline.callDisplayList()");
        if (id <= 0) {
            if (numInvalidLists < 3) {
                ++numInvalidLists;
                System.err.println("JAVA 3D ERROR : glCallList(" + id + ") -- IGNORED");
            } else if (numInvalidLists == 3) {
                ++numInvalidLists;
                System.err.println("JAVA 3D : further glCallList error messages discarded");
            }
            return;
        }

		GL2 gl = context(ctx).getGL().getGL2();
        // Set normalization if non-uniform scale
        if (isNonUniformScale) {
            gl.glEnable(GL2.GL_NORMALIZE);
        }

        gl.glCallList(id);

        // Turn normalization back off
        if (isNonUniformScale) {
            gl.glDisable(GL2.GL_NORMALIZE);
        }
    }

    @Override
    void freeDisplayList(Context ctx, int id) {
        if (VERBOSE) System.err.println("JoglPipeline.freeDisplayList()");
        if (id <= 0) {
            System.err.println("JAVA 3D ERROR : glDeleteLists(" + id + ",1) -- IGNORED");
        }

		GL2 gl = context(ctx).getGL().getGL2();
        gl.glDeleteLists(id, 1);
    }

    @Override
    void freeTexture(Context ctx, int id) {
        if (VERBOSE) System.err.println("JoglPipeline.freeTexture()");

        GL gl = context(ctx).getGL();

        if (id > 0) {
            int[] tmp = new int[1];
            tmp[0] = id;
            gl.glDeleteTextures(1, tmp, 0);
        } else {
            System.err.println("tried to delete tex with texid <= 0");
        }
    }

	@Override
	int generateTexID(Context ctx) {
		if (VERBOSE) System.err.println("JoglPipeline.generateTexID()");

		GL gl = context(ctx).getGL();
		int[] tmp = new int[] { -1 };
		gl.glGenTextures(1, tmp, 0);

		if (tmp[0] < 1)
			return -1;

		return tmp[0];
	}

    @Override
    void texturemapping(Context ctx,
            int px, int py,
            int minX, int minY, int maxX, int maxY,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] imageYdown,
            int winWidth, int winHeight) {
        if (VERBOSE) System.err.println("JoglPipeline.texturemapping()");

		GL2 gl = context(ctx).getGL().getGL2();

        int glType = GL.GL_RGBA;

        // Temporarily disable fragment and most 3D operations
        gl.glPushAttrib(GL2.GL_ENABLE_BIT | GL2.GL_TEXTURE_BIT | GL.GL_DEPTH_BUFFER_BIT | GL2.GL_POLYGON_BIT);
        disableAttribFor2D(gl);

        // Reset the polygon mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

        gl.glDepthMask(false);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);
        // set up texture parameter
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

        gl.glEnable(GL.GL_TEXTURE_2D);

        // loaded identity modelview and projection matrix
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        gl.glOrtho(0.0, winWidth, 0.0, winHeight, 0.0, 0.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();

        if (gl.isExtensionAvailable("GL_EXT_abgr")) {
            glType = GL2.GL_ABGR_EXT;
        } else {
            switch (format) {
                case ImageComponentRetained.TYPE_BYTE_RGBA:
                    glType = GL.GL_RGBA;
                    break;
                case ImageComponentRetained.TYPE_BYTE_RGB:
                    glType = GL.GL_RGB;
                    break;
            }
        }
        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, rasWidth);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, minX);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, minY);
        gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, minX, minY,
                maxX - minX, maxY - minY,
                glType, GL.GL_UNSIGNED_BYTE,
                ByteBuffer.wrap(imageYdown));
        gl.glPixelStorei(GL2.GL_UNPACK_ROW_LENGTH, 0);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_PIXELS, 0);
        gl.glPixelStorei(GL2.GL_UNPACK_SKIP_ROWS, 0);

        float texMinU = (float) minX/ (float) texWidth;
        float texMinV = (float) minY/ (float) texHeight;
        float texMaxU = (float) maxX/ (float) texWidth;
        float texMaxV = (float) maxY/ (float) texHeight;
        float halfWidth = (float)winWidth/2.0f;
        float halfHeight = (float)winHeight/2.0f;

        float mapMinX = (float) (((px + minX)- halfWidth)/halfWidth);
        float mapMinY = (float) ((halfHeight - (py + maxY))/halfHeight);
        float mapMaxX = (float) ((px + maxX - halfWidth)/halfWidth);
        float mapMaxY = (float) ((halfHeight - (py + minY))/halfHeight);

        gl.glBegin(GL2.GL_QUADS);

        gl.glTexCoord2f(texMinU, texMaxV); gl.glVertex2f(mapMinX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMaxV); gl.glVertex2f(mapMaxX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMinV); gl.glVertex2f(mapMaxX,mapMaxY);
        gl.glTexCoord2f(texMinU, texMinV); gl.glVertex2f(mapMinX,mapMaxY);
        gl.glEnd();

        // Java 3D always clears the Z-buffer
        gl.glDepthMask(true);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
        gl.glPopAttrib();
    }

    @Override
    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
        if (VERBOSE) System.err.println("JoglPipeline.initTexturemapping()");

		GL2 gl = context(ctx).getGL().getGL2();

        int glType = (gl.isExtensionAvailable("GL_EXT_abgr") ? GL2.GL_ABGR_EXT : GL.GL_RGBA);

        gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);

        gl.glTexImage2D(GL2.GL_PROXY_TEXTURE_2D, 0, GL.GL_RGBA, texWidth,
                texHeight, 0, glType, GL.GL_UNSIGNED_BYTE, null);

        int[] width = new int[1];
        gl.glGetTexLevelParameteriv(GL2.GL_PROXY_TEXTURE_2D, 0,
                GL2.GL_TEXTURE_WIDTH, width, 0);

        if (width[0] <= 0) {
            return false;
        }

        // init texture size only without filling the pixels
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texWidth,
                texHeight, 0, glType, GL.GL_UNSIGNED_BYTE, null);

        return true;
    }


    // Set internal render mode to one of FIELD_ALL, FIELD_LEFT or
    // FIELD_RIGHT.  Note that it is up to the caller to ensure that
    // stereo is available before setting the mode to FIELD_LEFT or
    // FIELD_RIGHT.  The boolean isTRUE for double buffered mode, FALSE
    // foe single buffering.
    @Override
    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
        if (VERBOSE) System.err.println("JoglPipeline.setRenderMode()");

		GL2 gl = context(ctx).getGL().getGL2();
        int drawBuf = 0;
        if (doubleBuffer) {
            drawBuf = GL.GL_BACK;
            switch (mode) {
                case Canvas3D.FIELD_LEFT:
                    drawBuf = GL2.GL_BACK_LEFT;
                    break;
                case Canvas3D.FIELD_RIGHT:
                    drawBuf = GL2.GL_BACK_RIGHT;
                    break;
                case Canvas3D.FIELD_ALL:
                    drawBuf = GL.GL_BACK;
                    break;
            }
        } else {
            drawBuf = GL.GL_FRONT;
            switch (mode) {
                case Canvas3D.FIELD_LEFT:
                    drawBuf = GL2.GL_FRONT_LEFT;
                    break;
                case Canvas3D.FIELD_RIGHT:
                    drawBuf = GL2.GL_FRONT_RIGHT;
                    break;
                case Canvas3D.FIELD_ALL:
                    drawBuf = GL.GL_FRONT;
                    break;
            }
        }

        gl.glDrawBuffer(drawBuf);
    }

    // Set glDepthMask.
    @Override
    void setDepthBufferWriteEnable(Context ctx, boolean mode) {
        if (VERBOSE) System.err.println("JoglPipeline.setDepthBufferWriteEnable()");

        GL gl = context(ctx).getGL();
        if (mode) {
            gl.glDepthMask(true);
        } else {
            gl.glDepthMask(false);
        }
    }

    //----------------------------------------------------------------------
    // Helper private functions for Canvas3D
    //

    private boolean getPropertiesFromCurrentContext(JoglContext ctx, GL2 gl) {

        // FIXME: this is a heavily abridged set of the stuff in Canvas3D.c;
        // probably need to pull much more in
        int[] tmp = new int[1];
        gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, tmp, 0);
        ctx.setMaxTexCoordSets(tmp[0]);
        if (VirtualUniverse.mc.transparentOffScreen) {
            ctx.setAlphaClearValue(0.0f);
        } else {
            ctx.setAlphaClearValue(1.0f);
        }
        if (gl.isExtensionAvailable("GL_ARB_vertex_shader")) {
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_COORDS_ARB, tmp, 0);
            ctx.setMaxTexCoordSets(tmp[0]);
        }
        return true;
    }

    private int[] extractVersionInfo(String versionString) {
        StringTokenizer tok = new StringTokenizer(versionString, ". ");
        int major = Integer.valueOf(tok.nextToken()).intValue();
        int minor = Integer.valueOf(tok.nextToken()).intValue();

        // See if there's vendor-specific information which might
        // imply a more recent OpenGL version
        tok = new StringTokenizer(versionString, " ");
        if (tok.hasMoreTokens()) {
            tok.nextToken();
            if (tok.hasMoreTokens()) {
                Pattern p = Pattern.compile("\\D*(\\d+)\\.(\\d+)\\.?(\\d*).*");
                Matcher m = p.matcher(tok.nextToken());
                if (m.matches()) {
                    int altMajor = Integer.valueOf(m.group(1)).intValue();
                    int altMinor = Integer.valueOf(m.group(2)).intValue();
                    // Avoid possibly confusing situations by requiring
                    // major version to match
                    if (altMajor == major &&
                            altMinor >  minor) {
                        minor = altMinor;
                    }
                }
            }
        }
        return new int[] { major, minor };
    }

// FIXME: GL_SGI_texture_color_table
//    private int getTextureColorTableSize(GL gl) {
//        if (!gl.isExtensionAvailable("GL_ARB_imaging")) {
//            return 0;
//        }
//
//        gl.glColorTable(GL.GL_PROXY_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGBA, 256, GL.GL_RGB,
//                GL2.GL_INT, null);
//        int[] tmp = new int[1];
//        gl.glGetColorTableParameteriv(GL.GL_PROXY_TEXTURE_COLOR_TABLE_SGI,
//                GL2.GL_COLOR_TABLE_WIDTH, tmp, 0);
//        return tmp[0];
//    }


    private void checkTextureExtensions(Canvas3D cv,
            JoglContext ctx,
            GL gl,
            boolean gl13) {
        if (gl13) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_MULTI_TEXTURE;
            cv.multiTexAccelerated = true;
            int[] tmp = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_UNITS, tmp, 0);
            cv.maxTextureUnits = tmp[0];
            cv.maxTexCoordSets = cv.maxTextureUnits;
            if (gl.isExtensionAvailable("GL_ARB_vertex_shader")) {
                gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_COORDS_ARB, tmp, 0);
                cv.maxTexCoordSets = tmp[0];
            }
        }
// FIXME: GL_SGI_texture_color_table
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table") ||
//                gl.isExtensionAvailable("GL_ARB_imaging")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COLOR_TABLE;
//
//            // get texture color table size
//            // need to check later
//            cv.textureColorTableSize = getTextureColorTableSize(gl);
//            if (cv.textureColorTableSize <= 0) {
//                cv.textureExtendedFeatures &= ~Canvas3D.TEXTURE_COLOR_TABLE;
//            }
//            if (cv.textureColorTableSize > 256) {
//                cv.textureColorTableSize = 256;
//            }
//        }

        if (gl.isExtensionAvailable("GL_ARB_texture_env_combine")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE;
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE_SUBTRACT;
        } else if (gl.isExtensionAvailable("GL_EXT_texture_env_combine")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE;
        }

// FIXME: GL_NV_register_combiners
//        if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_REGISTER_COMBINERS;
//        }

        if (gl.isExtensionAvailable("GL_ARB_texture_env_dot3") ||
                gl.isExtensionAvailable("GL_EXT_texture_env_dot3")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE_DOT3;
        }

        if (gl13) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_CUBE_MAP;
        }

// FIXME: GL_SGIS_sharpen_texture
//        if (gl.isExtensionAvailable("GL_SGIS_sharpen_texture")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_SHARPEN;
//        }

// FIXME: GL_SGIS_sharpen_texture
//        if (gl.isExtensionAvailable("GL_SGIS_detail_texture")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_DETAIL;
//        }

// FIXME: GL_SGIS_texture_filter4
//        if (gl.isExtensionAvailable("GL_SGIS_texture_filter4")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_FILTER4;
//        }

        if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_ANISOTROPIC_FILTER;
            float[] tmp = new float[1];
            gl.glGetFloatv(GL. GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, tmp, 0);
            cv.anisotropicDegreeMax = tmp[0];
        }

// FIXME: GL_SGIX_texture_lod_bias
//        if (gl.isExtensionAvailable("GL_SGIX_texture_lod_bias")) {
//            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_LOD_OFFSET;
//        }

        if (!VirtualUniverse.mc.enforcePowerOfTwo &&
                gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_NON_POWER_OF_TWO;
        }

        if (gl.isExtensionAvailable("GL_SGIS_generate_mipmap")) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_AUTO_MIPMAP_GENERATION;
        }

    }


    private void checkGLSLShaderExtensions(Canvas3D cv,
            JoglContext ctx,
            GL gl,
            boolean hasgl13) {

		// Force shaders to be disabled, since no multitexture support
		if (!hasgl13)
			return;

        if (gl.isExtensionAvailable("GL_ARB_shader_objects") &&
            gl.isExtensionAvailable("GL_ARB_shading_language_100")) {

            // FIXME: this isn't complete and would need to set up the
            // JoglContext for dispatch of various routines such as those
            // related to vertex attributes
            int[] tmp = new int[1];
            gl.glGetIntegerv(GL2.GL_MAX_TEXTURE_IMAGE_UNITS_ARB, tmp, 0);
            cv.maxTextureImageUnits = tmp[0];
            gl.glGetIntegerv(GL2.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, tmp, 0);
            cv.maxVertexTextureImageUnits = tmp[0];
            gl.glGetIntegerv(GL2.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, tmp, 0);
            cv.maxCombinedTextureImageUnits = tmp[0];
            int vertexAttrOffset = VirtualUniverse.mc.glslVertexAttrOffset;
            ctx.setGLSLVertexAttrOffset(vertexAttrOffset);
            gl.glGetIntegerv(GL2.GL_MAX_VERTEX_ATTRIBS_ARB, tmp, 0);
            cv.maxVertexAttrs = tmp[0];
            // decr count to allow for reserved vertex attrs
            cv.maxVertexAttrs -= vertexAttrOffset;
            if (cv.maxVertexAttrs < 0) {
                cv.maxVertexAttrs = 0;
            }
            cv.shadingLanguageGLSL = true;
        }
    }

    private void setupCanvasProperties(Canvas3D cv, JoglContext ctx, GL gl) {
        // Note: this includes relevant portions from both the
        // NativePipeline's getPropertiesFromCurrentContext and setupCanvasProperties

        // Reset all fields
        cv.multiTexAccelerated = false;
        cv.maxTextureUnits = 1;
        cv.maxTexCoordSets = 1;
        cv.maxTextureImageUnits = 0;
        cv.maxVertexTextureImageUnits = 0;
        cv.maxCombinedTextureImageUnits = 0;
        cv.maxVertexAttrs = 0;
        cv.extensionsSupported = 0;
        cv.textureExtendedFeatures = 0;
        cv.textureColorTableSize = 0;
        cv.anisotropicDegreeMax = 0;
        cv.textureBoundaryWidthMax = 0;
        cv.textureWidthMax = 0;
        cv.textureHeightMax = 0;
        cv.texture3DWidthMax = 0;
        cv.texture3DHeightMax = 0;
        cv.texture3DDepthMax = 0;
        cv.shadingLanguageGLSL = false;

        // Now make queries and set up these fields
        String glVersion  = gl.glGetString(GL.GL_VERSION);
        String glVendor   = gl.glGetString(GL.GL_VENDOR);
        String glRenderer = gl.glGetString(GL.GL_RENDERER);
        cv.nativeGraphicsVersion  = glVersion;
        cv.nativeGraphicsVendor   = glVendor;
        cv.nativeGraphicsRenderer = glRenderer;

        // find out the version, major and minor version number
        int[] versionNumbers = extractVersionInfo(glVersion);
        int major = versionNumbers[0];
        int minor = versionNumbers[1];

        ///////////////////////////////////////////
        // setup the graphics context properties //

        // NOTE: Java 3D now requires OpenGL 1.3 for full functionality.
        // For backwards compatibility with certain older graphics cards and
        // drivers (e.g., the Linux DRI driver for older ATI cards),
        // we will try to run on OpenGL 1.2 in an unsupported manner. However,
        // we will not attempt to use OpenGL extensions for any features that
        // are available in OpenGL 1.3, specifically multitexture, multisample,
        // and cube map textures.

        if (major < 1 || (major == 1 && minor < 2)) {
            throw new IllegalRenderingStateException(
                    "Java 3D ERROR : OpenGL 1.2 or better is required (GL_VERSION=" +
                    major + "." + minor + ")");
        }

        boolean gl20 = false;
        boolean gl14 = false;
        boolean gl13 = false;

        if (major == 1) {
            if (minor == 2) {
                System.err.println("JAVA 3D: OpenGL 1.2 detected; will run with reduced functionality");
            }
            if (minor >= 3) {
                gl13 = true;
            }
            if (minor >= 4) {
                gl14 = true;
            }
        } else /* major >= 2 */ {
            gl13 = true;
            gl14 = true;
            gl20 = true;
        }

        if (gl20) {
            assert gl13;
            assert gl14;
            assert gl.isExtensionAvailable("GL_VERSION_2_0");
        }

        if (gl14) {
            assert gl13;
            assert gl.isExtensionAvailable("GL_VERSION_1_4");
        }

        if (gl13) {
            assert gl.isExtensionAvailable("GL_VERSION_1_3");
        }

        // Set up properties for OpenGL 1.3
        cv.textureExtendedFeatures |= Canvas3D.TEXTURE_3D;

        // Note that we don't query for GL_ARB_imaging here

        cv.textureExtendedFeatures |= Canvas3D.TEXTURE_LOD_RANGE;

        if (gl14) {
            cv.textureExtendedFeatures |= Canvas3D.TEXTURE_AUTO_MIPMAP_GENERATION;
        }

        // look for OpenGL 2.0 features
        // Fix to Issue 455 : Need to disable NPOT textures for older cards that claim to support it.
        // Some older cards (e.g., Nvidia fx500 and ATI 9800) claim to support OpenGL 2.0.
        // This means that these cards have to support non-power-of-two (NPOT) texture,
        // but their lack the necessary HW force the vendors the emulate this feature in software.
        // The result is a ~100x slower down compare to power-of-two textures.
        // Do not check for gl20 but instead check of GL_ARB_texture_non_power_of_two extension string
        // if (gl20) {
        //    if(!VirtualUniverse.mc.enforcePowerOfTwo) {
        //        cv.textureExtendedFeatures |= Canvas3D.TEXTURE_NON_POWER_OF_TWO;
        //    }
        // }


        // Setup GL_EXT_abgr
        if (gl.isExtensionAvailable("GL_EXT_abgr")) {
            cv.extensionsSupported |= Canvas3D.EXT_ABGR;
        }

        // GL_BGR is always supported
        cv.extensionsSupported |= Canvas3D.EXT_BGR;

        // Setup multisample
        // FIXME: this is not correct for the Windows platform yet
        if (gl13) {
            cv.extensionsSupported |= Canvas3D.MULTISAMPLE;
            ctx.setHasMultisample(true);
        }

        if ((cv.extensionsSupported & Canvas3D.MULTISAMPLE) != 0 &&
                !VirtualUniverse.mc.implicitAntialiasing) {
            gl.glDisable(GL.GL_MULTISAMPLE);
        }

        // Check texture extensions
        checkTextureExtensions(cv, ctx, gl, gl13);

        // Check shader extensions
        checkGLSLShaderExtensions(cv, ctx, gl, gl13);

        // Setup GL_SUN_gloabl_alpha
// FIXME: SUN_global_alpha
//        if (gl.isExtensionAvailable("GL_SUN_gloabl_alpha")) {
//            cv.extensionsSupported |= Canvas3D.SUN_GLOBAL_ALPHA;
//        }

        cv.textureBoundaryWidthMax = 1;
        {
            int[] tmp = new int[1];
            gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, tmp, 0);
            cv.textureWidthMax = tmp[0];
            cv.textureHeightMax = tmp[0];

            tmp[0] = -1;
            gl.glGetIntegerv(GL2.GL_MAX_3D_TEXTURE_SIZE, tmp, 0);
            cv.texture3DWidthMax = tmp[0];
            cv.texture3DHeightMax = tmp[0];
            cv.texture3DDepthMax = tmp[0];
        }
    }

  /*
   * Function to disable most rendering attributes when doing a 2D
   * clear, image copy, or image composite operation. Note that the
   * caller must save/restore the attributes with
   * pushAttrib(GL_ENABLE_BIT|...) and popAttrib()
   */
    private void disableAttribFor2D(GL gl) {
        gl.glDisable(GL2.GL_ALPHA_TEST);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_COLOR_LOGIC_OP);
        gl.glDisable(GL2.GL_COLOR_MATERIAL);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL2.GL_FOG);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(GL2.GL_POLYGON_STIPPLE);
        gl.glDisable(GL.GL_STENCIL_TEST);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL2.GL_TEXTURE_GEN_Q);
        gl.glDisable(GL2.GL_TEXTURE_GEN_R);
        gl.glDisable(GL2.GL_TEXTURE_GEN_S);
        gl.glDisable(GL2.GL_TEXTURE_GEN_T);


        for (int i = 0; i < 6; i++) {
            gl.glDisable(GL2.GL_CLIP_PLANE0 + i);
        }

        gl.glDisable(GL2.GL_TEXTURE_3D);
        gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);

// FIXME: GL_NV_register_combiners
//        if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
//            gl.glDisable(GL.GL_REGISTER_COMBINERS_NV);
//        }
// FIXME: GL_SGI_texture_color_table
//        if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
//            gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
//        }
// FIXME: SUN_global_alpha
//        if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
//            gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
//        }

    }

    private void disableAttribForRaster(GL gl) {

        gl.glDisable(GL2.GL_COLOR_MATERIAL);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
        gl.glDisable(GL2.GL_POLYGON_STIPPLE);

        // TODO: Disable if Raster.CLIP_POSITION is true
//      for (int i = 0; i < 6; i++) {
//          gl.glDisable(GL2.GL_CLIP_PLANE0 + i);
//      }

// FIXME: SUN_global_alpha
//        if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
//            gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
//        }
    }

    private void copyTranspose(double[] src, double[] dst) {
        dst[0] = src[0];
        dst[1] = src[4];
        dst[2] = src[8];
        dst[3] = src[12];
        dst[4] = src[1];
        dst[5] = src[5];
        dst[6] = src[9];
        dst[7] = src[13];
        dst[8] = src[2];
        dst[9] = src[6];
        dst[10] = src[10];
        dst[11] = src[14];
        dst[12] = src[3];
        dst[13] = src[7];
        dst[14] = src[11];
        dst[15] = src[15];
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
        if (VERBOSE) System.err.println("JoglPipeline.getGraphicsConfig()");

		GraphicsConfigInfo       gcInf0    = Canvas3D.graphicsConfigTable.get(gconfig);
		AWTGraphicsConfiguration awtConfig = (AWTGraphicsConfiguration)gcInf0.getPrivateData();

    	return awtConfig.getAWTGraphicsConfiguration();
    }

    // Get best graphics config from pipeline
    @Override
    GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration[] gc) {
        if (VERBOSE) System.err.println("JoglPipeline.getBestConfiguration()");

        // Device / Screen
	    GraphicsDevice device = gc[0].getDevice();
	    AbstractGraphicsScreen screen = (device != null) ? AWTGraphicsScreen.createScreenDevice(device, AbstractGraphicsDevice.DEFAULT_UNIT) :
					                                       AWTGraphicsScreen.createDefault();

        // Create a GLCapabilities based on the GraphicsConfigTemplate3D

        final GLCapabilities caps = new GLCapabilities(profile);

        // On Linux, Windows

        // Only minimum values and REQUIRED capabilities are set !!
        // PREFERRED capabilities are checked by J3DCapsChooser

        // TODO GraphicsConfigTemplate3D doesn't support number of antialiasing samples
        // TODO 4 or 8 samples
        // 4 samples are taken as default maximum number, a smaller number will be chosen by
        // J3DCapsChooser if the requested number is not supported
        // so we require only 2 samples
        // (all available configs with 2 and more samples can then be checked by J3DCapsChooser)

        // REQUIRED

        caps.setStereo( (gct.getStereo() == GraphicsConfigTemplate.REQUIRED) );

        caps.setDoubleBuffered( (gct.getDoubleBuffer() == GraphicsConfigTemplate.REQUIRED) );

        // Scene antialiasing only if double buffering
        if (gct.getDoubleBuffer() == GraphicsConfigTemplate.REQUIRED &&
        	gct.getSceneAntialiasing() == GraphicsConfigTemplate.REQUIRED) {
        	caps.setSampleBuffers(true);
        	caps.setNumSamples(2);
        }
        else {
        	caps.setSampleBuffers(false);
        	caps.setNumSamples(0);
        }

        // Minimum values

        caps.setDepthBits     (gct.getDepthSize());
        caps.setStencilBits   (gct.getStencilSize());

        caps.setRedBits       (Math.max(5, gct.getRedSize()));
        caps.setGreenBits     (Math.max(5, gct.getGreenSize()));
        caps.setBlueBits      (Math.max(5, gct.getBlueSize()));

        // Issue 399: Request alpha buffer if transparentOffScreen is set
        if (VirtualUniverse.mc.transparentOffScreen) {
            caps.setAlphaBits(1);
        }

	    // Custom chooser instead of DefaultGLCapabilitiesChooser
        J3DCapsChooser chooser = new J3DCapsChooser(gct);

        GraphicsConfigurationFactory gcFactory = GraphicsConfigurationFactory.getFactory(AWTGraphicsDevice.class, GLCapabilitiesImmutable.class);

		// !! deadlock if getBestConfiguration is called on EDT (calling thread is waitung !!)
    	AWTGraphicsConfiguration awtConfig = getAWTGraphicsConfiguration(gcFactory, caps, chooser, screen);

    	// If minimum requirements are fulfilled (awtConfig != null),
		// but J3DCapsChooser wasn't called ( e.g. on Mac OS X (2.0-rc11) ) :
    	// set also PREFERRED caps and max sample number
    	if (awtConfig != null && chooser.isCalled() == false) {

			// 1. Double buffering and scene antialiasing : let Mac OS X decide

			boolean isDoubleBuffering = ( gct.getDoubleBuffer() == GraphicsConfigTemplate.REQUIRED ||
			                              gct.getDoubleBuffer() == GraphicsConfigTemplate.PREFERRED  );

			caps.setDoubleBuffered(isDoubleBuffering);

			if (isDoubleBuffering &&
				(gct.getSceneAntialiasing() == GraphicsConfigTemplate.REQUIRED ||
				 gct.getSceneAntialiasing() == GraphicsConfigTemplate.PREFERRED  ) ) {
				caps.setSampleBuffers(true);
				caps.setNumSamples(4); // TODO
                    }
			else {
				caps.setSampleBuffers(false);
				caps.setNumSamples(0);
                }

			AWTGraphicsConfiguration config = getAWTGraphicsConfiguration(gcFactory, caps, chooser, screen);

			// 2. PREFERRED stereo

			if (gct.getStereo() == GraphicsConfigTemplate.PREFERRED) {
				// config is fine so far, now add stereo requirement
				if (config != null) {
					caps.setStereo(true);
					AWTGraphicsConfiguration configStereo = getAWTGraphicsConfiguration(gcFactory, caps, chooser, screen);
					if (configStereo != null) {
						config = configStereo;
                    }
                }
				// start again with awtConfig
				else {
					GLCapabilities stereoCaps = new GLCapabilities(profile);
					stereoCaps.copyFrom((GLCapabilities)awtConfig.getChosenCapabilities());
					stereoCaps.setStereo(true);
					AWTGraphicsConfiguration configStereo = getAWTGraphicsConfiguration(gcFactory, stereoCaps, chooser, screen);
					if (configStereo != null) {
						config = configStereo;
            }
        }
            }

			// a 'better' config found ?
			if (config != null) {
				awtConfig = config;
            }
        }

    	// GraphicsConfigTemplate3D API :
    	// 	 If no such "best" GraphicsConfiguration is found, null is returned.
	    if (awtConfig == null) {
	    	System.out.println("J3D JoglPipeline.getBestConfiguration : no best GraphicsConfiguration is found !");
	    	return null;
        }

	    // !! these chosen caps are not final as long as the corresponding context is made current
	    GLCapabilities chosenCaps = (GLCapabilities)awtConfig.getChosenCapabilities();
//System.out.println("getBestConfiguration chosenCaps = " + chosenCaps);
	    // Index isn't used anymore
		JoglGraphicsConfiguration bestGC = new JoglGraphicsConfiguration(chosenCaps, -1, device);

		GraphicsConfigInfo gcInf0 = new GraphicsConfigInfo(gct);
    	gcInf0.setPrivateData(awtConfig);

        synchronized (Canvas3D.graphicsConfigTable) {
    		Canvas3D.graphicsConfigTable.put(bestGC, gcInf0);
          }

    	return bestGC;
        }

    private static AWTGraphicsConfiguration getAWTGraphicsConfiguration(GraphicsConfigurationFactory gcFactory,
                                                                        CapabilitiesImmutable capsRequested,
                                                                        CapabilitiesChooser chooser,
                                                                        AbstractGraphicsScreen screen) {
    	return (AWTGraphicsConfiguration)gcFactory.chooseGraphicsConfiguration(
    			capsRequested, capsRequested, chooser, screen, VisualIDHolder.VID_UNDEFINED);
    }

    // Determine whether specified graphics config is supported by pipeline
    @Override
    boolean isGraphicsConfigSupported(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration gc) {
        if (VERBOSE) System.err.println("JoglPipeline.isGraphicsConfigSupported()");

        // FIXME: it looks like this method is implemented incorrectly
        // in the existing NativePipeline in both the Windows and X11
        // ports. According to the semantics of the javadoc, it looks
        // like this method is supposed to figure out the OpenGL
        // capabilities which would be requested by the passed
        // GraphicsConfiguration object were it to be used, and see
        // whether it is possible to create a context with them.
        // Instead, on both platforms, the implementations basically set
        // up a query based on the contents of the
        // GraphicsConfigTemplate3D object, using the
        // GraphicsConfiguration object only to figure out on which
        // GraphicsDevice and screen we're making the request, and see
        // whether it's possible to choose an OpenGL pixel format based
        // on that information. This makes this method less useful and
        // we can probably just safely return true here uniformly
        // without breaking anything.
        return true;
    }

    // Methods to get actual capabilities from Canvas3D
    @Override
    boolean hasDoubleBuffer(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.hasDoubleBuffer()");
        if (VERBOSE) System.err.println("  Returning " + caps(cv).getDoubleBuffered());
        return caps(cv).getDoubleBuffered();
    }

    @Override
    boolean hasStereo(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.hasStereo()");
        if (VERBOSE) System.err.println("  Returning " + caps(cv).getStereo());
        return caps(cv).getStereo();
    }

    @Override
    int getStencilSize(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.getStencilSize()");
        if (VERBOSE) System.err.println("  Returning " + caps(cv).getStencilBits());
        return caps(cv).getStencilBits();
    }

    @Override
    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.hasSceneAntialiasingMultisample()");
        if (VERBOSE) System.err.println("  Returning " + caps(cv).getSampleBuffers());

        return caps(cv).getSampleBuffers();
    }

    @Override
    boolean hasSceneAntialiasingAccum(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.hasSceneAntialiasingAccum()");
        GLCapabilities caps = caps(cv);
        if (VERBOSE) System.err.println("  Returning " + (caps.getAccumRedBits() > 0 &&
                caps.getAccumGreenBits() > 0 &&
                caps.getAccumBlueBits() > 0));
        return (caps.getAccumRedBits() > 0 &&
                caps.getAccumGreenBits() > 0 &&
                caps.getAccumBlueBits() > 0);
    }

    private boolean checkedForGetScreenMethod = false;
    private Method getScreenMethod = null;
    @Override
    int getScreen(final GraphicsDevice graphicsDevice) {
        if (VERBOSE) System.err.println("JoglPipeline.getScreen()");

        if (!checkedForGetScreenMethod) {
            // All of the Sun GraphicsDevice implementations have a method
            //   int getScreen();
            // which we want to call reflectively if it's available.
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    try {
                        getScreenMethod = graphicsDevice.getClass().getDeclaredMethod("getScreen", new Class[] {});
                        getScreenMethod.setAccessible(true);
                    } catch (Exception e) {
                    }
                    checkedForGetScreenMethod = true;
                    return null;
                }
            });
        }

        if (getScreenMethod != null) {
            try {
                return ((Integer) getScreenMethod.invoke(graphicsDevice, (Object[]) null)).intValue();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return 0;
    }

    //----------------------------------------------------------------------
    // Helper classes and methods to support query context functionality
    // and pixel format selection

    interface ExtendedCapabilitiesChooser extends GLCapabilitiesChooser {
        public void init(GLContext context);
    }

    // Canvas subclass to help with various query operations such as the
    // "query context" mechanism and pixel format selection.
    // Must defeat and simplify the single-threading behavior of JOGL's
    // GLCanvas in order to be able to set up a temporary pixel format
    // and OpenGL context. Apparently simply turning off the
    // single-threaded mode isn't enough to do this.
    private final class QueryCanvas extends Canvas {

        private GLDrawable glDrawable;
        private ExtendedCapabilitiesChooser chooser;
        private boolean alreadyRan;

        private AWTGraphicsConfiguration awtConfig = null;
        private JAWTWindow nativeWindow = null;

        private QueryCanvas(AWTGraphicsConfiguration awtConfig,
        		            ExtendedCapabilitiesChooser chooser) {
            // The platform-specific GLDrawableFactory will only provide a
            // non-null GraphicsConfiguration on platforms where this is
            // necessary (currently only X11, as Windows allows the pixel
            // format of the window to be set later and Mac OS X seems to
            // handle this very differently than all other platforms). On
            // other platforms this method returns null; it is the case (at
            // least in the Sun AWT implementation) that this will result in
            // equivalent behavior to calling the no-arg super() constructor
            // for Canvas.
            super(awtConfig.getAWTGraphicsConfiguration());

            this.awtConfig = awtConfig;
            this.chooser = chooser;
        }

        @Override
        public void addNotify() {
            super.addNotify();

    		nativeWindow = (JAWTWindow)NativeWindowFactory.getNativeWindow(this, awtConfig);
			nativeWindow.lockSurface();
		    try {
	    		glDrawable = GLDrawableFactory.getFactory(profile).createGLDrawable(nativeWindow);
		    }
		    finally {
		    	nativeWindow.unlockSurface();
		    }

            glDrawable.setRealized(true);
        }

        // It seems that at least on Mac OS X we need to do the OpenGL
        // context-related work outside of the addNotify call because the
        // Canvas hasn't been resized to a non-zero size by that point
        private void doQuery() {
            if (alreadyRan)
                return;
            GLContext context = glDrawable.createContext(null);
            int res = context.makeCurrent();
            if (res != GLContext.CONTEXT_NOT_CURRENT) {
                try {
                    chooser.init(context);
                } finally {
                    context.release();
                }
            }
            context.destroy();
            alreadyRan = true;

            glDrawable.setRealized(false);
            nativeWindow.destroy();
        }
    }

    // Used to support the query context mechanism -- needs to be more
    // than just a GLCapabilitiesChooser
    private final class ContextQuerier extends DefaultGLCapabilitiesChooser
                                              implements ExtendedCapabilitiesChooser {
        private Canvas3D canvas;
        private boolean done;

        public ContextQuerier(Canvas3D canvas) {
            this.canvas = canvas;
        }

        public boolean done() {
            return done;
        }

        @Override
        public void init(GLContext context) {
            // This is basically a temporary
            JoglContext jctx = new JoglContext(context);
            GL2 gl = context.getGL().getGL2();
            // Set up various properties
            if (getPropertiesFromCurrentContext(jctx, gl)) {
                setupCanvasProperties(canvas, jctx, gl);
            }
            markDone();
        }

        private void markDone() {
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
    }

    private void disposeOnEDT(final Frame f) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                f.setVisible(false);
                f.dispose();
            }
        };
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(r);
        } else {
            r.run();
        }
    }


    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    @Override
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
        if (VERBOSE) System.err.println("JoglPipeline.createDrawingSurfaceObject()");
        return new JoglDrawingSurfaceObject(cv);
    }

    // Method to free the drawing surface object
    @Override
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
        if (VERBOSE) System.err.println("JoglPipeline.freeDrawingSurface()");
        // This method is a no-op
    }

    // Method to free the native drawing surface object
    @Override
    void freeDrawingSurfaceNative(Object o) {
        if (VERBOSE) System.err.println("JoglPipeline.freeDrawingSurfaceNative()");
        // This method is a no-op
    }

    //----------------------------------------------------------------------
    // Context-related routines
    //

    // Helper used everywhere
    GLContext context(Context ctx) {
        if (ctx == null)
            return null;
        return ((JoglContext) ctx).getGLContext();
    }

    // Helper used everywhere
    GLDrawable drawable(Drawable drawable) {
        if (drawable == null)
            return null;
        return ((JoglDrawable) drawable).getGLDrawable();
    }

    GLCapabilities caps(Canvas3D ctx) {
    	if (ctx.drawable != null) {
    		// latest state for on- and offscreen drawables
    		return (GLCapabilities)drawable(ctx.drawable).getChosenGLCapabilities();
    	}
    	else {
    		// state at the time of 'getBestConfiguration'
        return ((JoglGraphicsConfiguration) ctx.graphicsConfiguration).getGLCapabilities();
    }
    }

    //----------------------------------------------------------------------
    // General helper routines
    //

    private static ThreadLocal<FloatBuffer> nioVertexTemp = new ThreadLocal<FloatBuffer>();
    private static ThreadLocal<DoubleBuffer> nioVertexDoubleTemp = new ThreadLocal<DoubleBuffer>();
    private static ThreadLocal<FloatBuffer> nioColorTemp = new ThreadLocal<FloatBuffer>();
    private static ThreadLocal<ByteBuffer> nioColorByteTemp = new ThreadLocal<ByteBuffer>();
    private static ThreadLocal<FloatBuffer> nioNormalTemp = new ThreadLocal<FloatBuffer>();
    private static ThreadLocal<FloatBuffer[]> nioTexCoordSetTemp = new ThreadLocal<FloatBuffer[]>();
    private static ThreadLocal<FloatBuffer[]> nioVertexAttrSetTemp = new ThreadLocal<FloatBuffer[]>();

    private static FloatBuffer getVertexArrayBuffer(float[] vertexArray) {
        return getVertexArrayBuffer(vertexArray, true);
    }

    private static FloatBuffer getVertexArrayBuffer(float[] vertexArray, boolean copyData) {
        return getNIOBuffer(vertexArray, nioVertexTemp, copyData);
    }

    private static DoubleBuffer getVertexArrayBuffer(double[] vertexArray) {
        return getVertexArrayBuffer(vertexArray, true);
    }

    private static DoubleBuffer getVertexArrayBuffer(double[] vertexArray, boolean copyData) {
        return getNIOBuffer(vertexArray, nioVertexDoubleTemp, true);
    }

    private static FloatBuffer getColorArrayBuffer(float[] colorArray) {
        return getColorArrayBuffer(colorArray, true);
    }

    private static FloatBuffer getColorArrayBuffer(float[] colorArray, boolean copyData) {
        return getNIOBuffer(colorArray, nioColorTemp, true);
    }

    private static ByteBuffer getColorArrayBuffer(byte[] colorArray) {
        return getColorArrayBuffer(colorArray, true);
    }

    private static ByteBuffer getColorArrayBuffer(byte[] colorArray, boolean copyData) {
        return getNIOBuffer(colorArray, nioColorByteTemp, true);
    }

    private static FloatBuffer getNormalArrayBuffer(float[] normalArray) {
        return getNormalArrayBuffer(normalArray, true);
    }

    private static FloatBuffer getNormalArrayBuffer(float[] normalArray, boolean copyData) {
        return getNIOBuffer(normalArray, nioNormalTemp, true);
    }

    private static FloatBuffer[] getTexCoordSetBuffer(Object[] texCoordSet) {
        return getNIOBuffer(texCoordSet, nioTexCoordSetTemp);
    }

    private static FloatBuffer[] getVertexAttrSetBuffer(Object[] vertexAttrSet) {
        return getNIOBuffer(vertexAttrSet, nioVertexAttrSetTemp);
    }

    private static FloatBuffer getNIOBuffer(float[] array, ThreadLocal<FloatBuffer> threadLocal, boolean copyData) {
        if (array == null) {
            return null;
        }
        FloatBuffer buf = threadLocal.get();
        if (buf == null) {
			buf = Buffers.newDirectFloatBuffer(array.length);
            threadLocal.set(buf);
        } else {
            buf.rewind();
            if (buf.remaining() < array.length) {
                int newSize = Math.max(2 * buf.remaining(), array.length);
				buf = Buffers.newDirectFloatBuffer(newSize);
                threadLocal.set(buf);
            }
        }
        if (copyData) {
            buf.put(array);
            buf.rewind();
        }
        return buf;
    }

    private static DoubleBuffer getNIOBuffer(double[] array, ThreadLocal<DoubleBuffer> threadLocal, boolean copyData) {
        if (array == null) {
            return null;
        }
        DoubleBuffer buf = threadLocal.get();
        if (buf == null) {
			buf = Buffers.newDirectDoubleBuffer(array.length);
            threadLocal.set(buf);
        } else {
            buf.rewind();
            if (buf.remaining() < array.length) {
                int newSize = Math.max(2 * buf.remaining(), array.length);
				buf = Buffers.newDirectDoubleBuffer(newSize);
                threadLocal.set(buf);
            }
        }
        if (copyData) {
            buf.put(array);
            buf.rewind();
        }
        return buf;
    }

    private static ByteBuffer getNIOBuffer(byte[] array, ThreadLocal<ByteBuffer> threadLocal, boolean copyData) {
        if (array == null) {
            return null;
        }
        ByteBuffer buf = threadLocal.get();
        if (buf == null) {
			buf = Buffers.newDirectByteBuffer(array.length);
            threadLocal.set(buf);
        } else {
            buf.rewind();
            if (buf.remaining() < array.length) {
                int newSize = Math.max(2 * buf.remaining(), array.length);
				buf = Buffers.newDirectByteBuffer(newSize);
                threadLocal.set(buf);
            }
        }
        if (copyData) {
            buf.put(array);
            buf.rewind();
        }
        return buf;
    }

    private static FloatBuffer[] getNIOBuffer(Object[] array, ThreadLocal<FloatBuffer[]> threadLocal) {
        if (array == null) {
            return null;
        }
        FloatBuffer[] bufs = threadLocal.get();

        // First resize array of FloatBuffers
        if (bufs == null) {
            bufs = new FloatBuffer[array.length];
            threadLocal.set(bufs);
        } else if (bufs.length < array.length) {
            FloatBuffer[] newBufs = new FloatBuffer[array.length];
            System.arraycopy(bufs, 0, newBufs, 0, bufs.length);
            bufs = newBufs;
            threadLocal.set(bufs);
        }

        // Now go down array of arrays, converting each into a direct FloatBuffer
        for (int i = 0; i < array.length; i++) {
            float[] cur = (float[]) array[i];
            FloatBuffer buf = bufs[i];
            if (buf == null) {
				buf = Buffers.newDirectFloatBuffer(cur.length);
                bufs[i] = buf;
            } else {
                buf.rewind();
                if (buf.remaining() < cur.length) {
                    int newSize = Math.max(2 * buf.remaining(), cur.length);
					buf = Buffers.newDirectFloatBuffer(newSize);
                    bufs[i] = buf;
                }
            }
            buf.put(cur);
            buf.rewind();
        }

        return bufs;
    }

    private static final class J3DCapsChooser implements GLCapabilitiesChooser {

    	private GraphicsConfigTemplate3D gct3D = null;

    	private boolean called = false;

    	private J3DCapsChooser(GraphicsConfigTemplate3D gct) {
    		gct3D = gct;
    	}

    	private boolean isCalled() {
    		return called;
    	}

    	// Interface GLCapabilitiesChooser

    	// javadoc : "Some of the entries in the available array may be null;"

    	@Override
    	public int chooseCapabilities(final CapabilitiesImmutable desiredCaps,
                                      final List<? extends CapabilitiesImmutable> availableCapsList,
                                      final int windowSystemRecommendedChoice) {
    		called = true;

    	    final GLCapabilitiesImmutable caps = (GLCapabilitiesImmutable)desiredCaps;

    	    List<GLCapabilitiesImmutable> capsList = new ArrayList<GLCapabilitiesImmutable>(availableCapsList.size());
    	    for (CapabilitiesImmutable availableCaps : availableCapsList) {
    	    	if (availableCaps != null)
    	    		capsList.add((GLCapabilitiesImmutable)availableCaps);
    	    }

    	    List<GLCapabilitiesImmutable> potentialCapsList = new ArrayList<GLCapabilitiesImmutable>();

    	    int capsListLength = capsList.size();

    	    int chosenIndex = -1;

    	    // I. minimum requirements

    	    // I.a red
    	    int num = gct3D.getRedSize();
	    	for (int i=capsListLength-1; i >= 0; i--) {
	    		if (capsList.get(i).getRedBits() < num) {
	    			capsList.remove(i);
	    		}
	    	}
	    	capsListLength = capsList.size();
    	    // I.b green
	    	num = gct3D.getGreenSize();
	    	for (int i=capsListLength-1; i >= 0; i--) {
	    		if (capsList.get(i).getGreenBits() < num) {
	    			capsList.remove(i);
	    		}
	    	}
	    	capsListLength = capsList.size();
    	    // I.c blue
	    	num = gct3D.getBlueSize();
	    	for (int i=capsListLength-1; i >= 0; i--) {
	    		if (capsList.get(i).getBlueBits() < num) {
	    			capsList.remove(i);
	    		}
	    	}
	    	capsListLength = capsList.size();

    	    // I.d depth
	    	num = gct3D.getDepthSize();
	    	for (int i=capsListLength-1; i >= 0; i--) {
	    		if (capsList.get(i).getDepthBits() < num) {
	    			capsList.remove(i);
	    		}
	    	}
	    	capsListLength = capsList.size();
    	    // I.e stencil
	    	num = gct3D.getStencilSize();
	    	for (int i=capsListLength-1; i >= 0; i--) {
	    		if (capsList.get(i).getStencilBits() < num) {
	    			capsList.remove(i);
	    		}
	    	}
	    	capsListLength = capsList.size();

	    	if (capsListLength < 1)
	    		return chosenIndex;

	    	// II. REQUIRED

    	    // II.a stereo
    	    if (gct3D.getStereo() == GraphicsConfigTemplate3D.REQUIRED) {
    	    	for (int i=capsListLength-1; i >= 0; i--) {
    	    		if (capsList.get(i).getStereo() == false) {
    	    			capsList.remove(i);
    	    		}
    	    	}
    	    	capsListLength = capsList.size();
    	    }

    	    // II.b double buffering
    	    if (gct3D.getDoubleBuffer() == GraphicsConfigTemplate3D.REQUIRED) {
    	    	for (int i=capsListLength-1; i >= 0; i--) {
    	    		if (capsList.get(i).getDoubleBuffered() == false) {
    	    			capsList.remove(i);
    	    		}
    	    	}
    	    	capsListLength = capsList.size();
    	    }

    	    // II.c scene antialiasing (implicitly double buffered !)
    	    if (gct3D.getSceneAntialiasing() == GraphicsConfigTemplate3D.REQUIRED) {
    	    	for (int i=capsListLength-1; i >= 0; i--) {
    	    		if (capsList.get(i).getSampleBuffers() == false) {
    	    			capsList.remove(i);
    	    		}
    	    	}
    	    	capsListLength = capsList.size();

    	    	// Check num samples
    	    	if (capsListLength > 0) {

    	    		selectNumSamples(capsList);
    	    		// capsList now contains only caps with num samples >= min(required number, max supported number)

    	    		capsListLength = capsList.size();
    	    	}
    	    }

	    	if (capsListLength < 1)
	    		return chosenIndex;

	    	// All remaining member of capsList fulfill the min requirements

	    	// III. PREFERRED   priority : 1. scene antialiasing,  2. db buff,  3. stereo

	    	// fill potentialCapsList with caps from capsList

	    	// III.a scene antialiasing (implicitly double buffered !)
	    	if (gct3D.getSceneAntialiasing() == GraphicsConfigTemplate3D.PREFERRED &&
	    		gct3D.getDoubleBuffer() != GraphicsConfigTemplate3D.UNNECESSARY) {
	    		for (GLCapabilitiesImmutable potCaps : capsList) {
    	    		if (potCaps.getSampleBuffers() && potCaps.getDoubleBuffered()) {
    	    			potentialCapsList.add(potCaps);
    	    		}
	    		}
	    		// Check num samples
	    		if (potentialCapsList.size() > 0) {
    	    		selectNumSamples(potentialCapsList);
    	    		// potentialCapsList now contains only caps with num samples >= min(required number, max supported number)
    	    	}
	    		// Check stereo
		    	if (potentialCapsList.size() > 0 && gct3D.getStereo() == GraphicsConfigTemplate3D.PREFERRED) {
	    			selectStereo(potentialCapsList);
	    			// potentialCapsList now contains only caps with stereo or is unchanged
		    	}

		    	// if potentialCapsList.size() > 0 => done
	    	}

	    	// potentialCapsList.size() > 0 ???

	    	// III.b double buffering
	    	if (potentialCapsList.isEmpty() && gct3D.getDoubleBuffer() == GraphicsConfigTemplate3D.PREFERRED) {
	    		for (GLCapabilitiesImmutable potCaps : capsList) {
    	    		if (potCaps.getDoubleBuffered()) {
    	    			potentialCapsList.add(potCaps);
    	    		}
	    		}
	    		// Check stereo
		    	if (potentialCapsList.size() > 0 && gct3D.getStereo() == GraphicsConfigTemplate3D.PREFERRED) {
	    			selectStereo(potentialCapsList);
	    			// potentialCapsList now contains only caps with stereo or is unchanged
		    	}

		    	// if potentialCapsList.size() > 0 => done
        	}

	    	// potentialCapsList.size() > 0 ???

	    	// III.c stereo
	    	if (potentialCapsList.isEmpty() && gct3D.getStereo() == GraphicsConfigTemplate3D.PREFERRED) {
	    		for (GLCapabilitiesImmutable potCaps : capsList) {
    	    		if (potCaps.getStereo()) {
    	    			potentialCapsList.add(potCaps);
    	    		}
	    		}

		    	// if potentialCapsList.size() > 0 => done
        	}

	    	// If no PREFERRED capability exists or no supporting caps were found
	    	if (potentialCapsList.isEmpty()) {
	    		potentialCapsList.addAll(capsList);
	    	}

	    	capsListLength = potentialCapsList.size();

	    	// IV. UNNECESSARY -  TODO remove those caps ????


	    	// Now choose a caps in potentialCapsList and determine its index in availableCapsList

	    	if (!potentialCapsList.isEmpty()) {
	    		// TODO take the first one
	    		chosenIndex = availableCapsList.indexOf(potentialCapsList.get(0));
	    	}

	    	return chosenIndex;
    	}

    	// If one caps supports stereo, remove all caps with no stereo support
    	private void selectStereo(List<GLCapabilitiesImmutable> capsList) {
    		boolean isStereoSupported = false;
    		for (GLCapabilitiesImmutable caps : capsList) {
    			if (caps.getStereo()) {
    				isStereoSupported = true;
    				break;
    			}
    		}
    		// Kepp all caps with stereo support
    		if (isStereoSupported) {
		    	for (int i=capsList.size()-1; i >= 0; i--) {
		    		if (capsList.get(i).getStereo() == false) {
		    			capsList.remove(i);
		    		}
		    	}
    		}
    	}

    	// Search for best caps if supported number of samples ( >=0 ) is less than the requested number
    	private void selectNumSamples(List<GLCapabilitiesImmutable> msaaCapsList) {
			// required number
	    	int reqNumSamples = 4; // TODO
	    	// Maximum available
	    	int maxNumSamples = 0;

    		for (GLCapabilitiesImmutable msaaCaps : msaaCapsList) {
    			if (msaaCaps.getNumSamples() > maxNumSamples) {
    				maxNumSamples = msaaCaps.getNumSamples();
    			}
    		}

    		// The required number might not be supported
    		reqNumSamples = Math.min(reqNumSamples, maxNumSamples);

    		// Kepp all caps with num samples >= reqNumSamples
	    	for (int i=msaaCapsList.size()-1; i >= 0; i--) {
	    		if (msaaCapsList.get(i).getNumSamples() < reqNumSamples) {
	    			msaaCapsList.remove(i);
	    		}
	    	}
    	}
    }
}
