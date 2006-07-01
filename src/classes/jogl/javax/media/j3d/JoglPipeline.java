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

import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.*;
import java.security.*;
import java.util.*;
import java.util.regex.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.cg.*;
import com.sun.opengl.util.*;

/**
 * Concrete implementation of Pipeline class for the JOGL rendering
 * pipeline.
 */
class JoglPipeline extends Pipeline {

    // Flags indicating whether the Cg or GLSL libraries are available.
    private boolean cgLibraryAvailable = false;

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

    /**
     * Constructor for singleton JoglPipeline instance
     */
    protected JoglPipeline() {
    }

    /**
     * Initialize the pipeline
     */
    void initialize(Pipeline.Type rendererType) {
        super.initialize(rendererType);

        assert rendererType == Pipeline.Type.JOGL;
        
        // Java3D maintains strict control over which threads perform OpenGL work
        Threading.disableSingleThreading();

        // TODO: finish this with any other needed initialization
    }

    /**
     * Load all of the required libraries
     */
    void loadLibraries(int globalShadingLanguage) {
      if (globalShadingLanguage == Shader.SHADING_LANGUAGE_CG) {
        // Try to load the jogl_cg library and set the
        // cgLibraryAvailable flag to true if loads successfully; note
        // that successfully performing initialization of this class
        // will cause the Cg native library to be loaded on our behalf
        try {
          Class.forName("com.sun.opengl.cg.CgGL");
          cgLibraryAvailable = true;
        } catch (Exception e) {
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
        return true;
    }


    // ---------------------------------------------------------------------

    //
    // GeometryArrayRetained methods
    //

    // Used by D3D to free vertex buffer
    void freeD3DArray(GeometryArrayRetained geo, boolean deleteVB) {
      // Nothing to do
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
            int[] texUnitOffset,
            int numActiveTexUnitState,
            int[] texUnitStateMapArray,
            int vertexAttrCount, int[] vertexAttrSizes,
            float[] varray, float[] carray, int pass, int cDirty) {
      if (VERBOSE) System.err.println("JoglPipeline.execute()");

      executeGeometryArray(ctx, geo, geo_type, isNonUniformScale, useAlpha,
                           multiScreen, ignoreVertexColors, startVIndex, vcount, vformat,
                           texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                           texUnitOffset, numActiveTexUnitState, texUnitStateMapArray,
                           vertexAttrCount, vertexAttrSizes,
                           varray, null, carray, pass, cDirty);
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
            int initialCoordIndex, float[] vfcoords, double[] vdcoords,
            int initialColorIndex, float[] cfdata, byte[] cbdata,
            int initialNormalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, float[][] vertexAttrData,
            int pass, int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
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
                             isNonUniformScale, multiScreen, ignoreVertexColors,
                             vcount, vformat, vdefined,
                             initialCoordIndex, fverts, dverts,
                             initialColorIndex, fclrs, bclrs,
                             initialNormalIndex, norms,
                             vertexAttrCount, vertexAttrSizes,
                             vertexAttrIndices, vertexAttrBufs,
                             pass, texCoordMapLength,
                             texcoordoffset, numActiveTexUnitState, texunitstatemap,
                             texIndex, texstride, texCoordBufs, cdirty,
                             sarray, strip_len, start_array);
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
            int initialCoordIndex,
            Object vcoords,
            int initialColorIndex,
            Object cdataBuffer,
            float[] cfdata, byte[] cbdata,
            int initialNormalIndex, Object ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndices, Object[] vertexAttrData,
            int pass, int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
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
      if (vattrDefined) {
        vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
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
        norms = (FloatBuffer) ndata;
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
                             isNonUniformScale, multiScreen, ignoreVertexColors,
                             vcount, vformat, vdefined,
                             initialCoordIndex, fverts, dverts,
                             initialColorIndex, fclrs, bclrs,
                             initialNormalIndex, norms,
                             vertexAttrCount, vertexAttrSizes,
                             vertexAttrIndices, vertexAttrBufs,
                             pass, texCoordMapLength,
                             texcoordoffset, numActiveTexUnitState, texunitstatemap,
                             texIndex, texstride, texCoordBufs, cdirty,
                             sarray, strip_len, start_array);
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
            int[] texUnitOffset,
            int numActiveTexUnit,
            int[] texUnitStateMapArray,
            Object varray, float[] cdata, int pass, int cdirty) {
      if (VERBOSE) System.err.println("JoglPipeline.executeInterleavedBuffer()");

      executeGeometryArray(ctx, geo, geo_type,
                           isNonUniformScale, useAlpha, multiScreen, ignoreVertexColors,
                           startVIndex, vcount, vformat,
                           texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                           texUnitOffset, numActiveTexUnit, texUnitStateMapArray,
                           0, null,
                           null, (Buffer) varray, cdata, pass, cdirty);
    }

    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
      if (VERBOSE) System.err.println("JoglPipeline.setVertexFormat()");

      GL gl = context(ctx).getGL();

      // Enable and disable the appropriate pointers
      if ((vformat & GeometryArray.NORMALS) != 0) {
        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
      } else {
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
      }
      if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
        gl.glEnableClientState(GL.GL_COLOR_ARRAY);
      } else {
        gl.glDisableClientState(GL.GL_COLOR_ARRAY);
      }

      if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
        if (useAlpha) {
          gl.glEnable(GL.GL_GLOBAL_ALPHA_SUN);
        } else {
          gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
        }
      }

      if ((vformat & GeometryArray.COORDINATES) != 0) {
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
      } else {
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
      }
    }

    void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors) {
      if (VERBOSE) System.err.println("JoglPipeline.disableGlobalAlpha()");

      GL gl = context(ctx).getGL();

      if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
        if (!ignoreVertexColors && ((vformat & GeometryArray.COLOR) != 0)) {
          if (useAlpha) {
            gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
          }
        }
      }
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
      if (VERBOSE) System.err.println("JoglPipeline.buildGA()");
      JoglContext jctx = (JoglContext) ctx;
      GL gl = context(ctx).getGL();
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

      int bstride = stride * BufferUtil.SIZEOF_FLOAT;
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
            primType = GL.GL_QUADS;
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

      GL gl = context(ctx).getGL();

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
      int[] tunitstatemap = null;
      FloatBuffer norms = null;
      FloatBuffer[] vertexAttrBufs = null;

      // Get vertex attribute arrays
      if (vattrDefined) {
        vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
      }

      // get texture arrays
      if (textureDefined) {
        texCoordBufs = getTexCoordSetBuffer(texCoords);
        tunitstatemap = new int[texCoordMapLength];
        for (int i = 0; i < texCoordMapLength; i++) {
          tunitstatemap[i] = i;
        }
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
	gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
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
	gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
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
	gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
      }

      // get color array
      if (floatColorsDefined) {
	gl.glEnableClientState(GL.GL_COLOR_ARRAY);
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
	gl.glEnableClientState(GL.GL_COLOR_ARRAY);
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
	gl.glDisableClientState(GL.GL_COLOR_ARRAY);
      }

      // get normal array
      if (normalsDefined) {
	gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
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
	gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
      }

      executeGeometryArrayVA(ctx, geo, geo_type,
                             isNonUniformScale, false, ignoreVertexColors,
                             vcount, vformat, vdefined,
                             initialCoordIndex, fverts, dverts,
                             initialColorIndex, fclrs, bclrs,
                             initialNormalIndex, norms,
                             vertexAttrCount, vertexAttrSizes,
                             vertexAttrIndices, vertexAttrBufs,
                             -1, texCoordMapLength,
                             tcoordsetmap, texCoordMapLength, tunitstatemap,
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
        iaFormat[0] = GL.GL_V3F; break;
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS) :
        iaFormat[0] = GL.GL_N3F_V3F; break;
      case (GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2) :
        iaFormat[0] = GL.GL_T2F_V3F; break;
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR) :
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.WITH_ALPHA) :
        iaFormat[0] = GL.GL_C4F_N3F_V3F; break;
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.TEXTURE_COORDINATE_2) :
        iaFormat[0] = GL.GL_T2F_N3F_V3F; break;
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.TEXTURE_COORDINATE_2):
      case (GeometryArray.COORDINATES | GeometryArray.NORMALS | GeometryArray.COLOR | GeometryArray.WITH_ALPHA | GeometryArray.TEXTURE_COORDINATE_2):
        iaFormat[0] = GL.GL_T2F_C4F_N3F_V3F; break;
      default:
        useInterleavedArrays[0] = false; break;
    }
  }

  private void
    enableTexCoordPointer(GL gl,
                          int texUnit,
                          int texSize,
                          int texDataType,
                          int stride,
                          Buffer pointer)
  {
    if (VERBOSE) System.err.println("JoglPipeline.enableTexCoordPointer()");
    clientActiveTextureUnit(gl, texUnit);
    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
    gl.glTexCoordPointer(texSize, texDataType, stride, pointer);
  }

  private void
    disableTexCoordPointer(GL gl,
                           int texUnit)
  {
    if (VERBOSE) System.err.println("JoglPipeline.disableTexCoordPointer()");
    clientActiveTextureUnit(gl, texUnit);
    gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
  }

  private void
    clientActiveTextureUnit(GL gl,
                            int texUnit)
  {
    if (VERBOSE) System.err.println("JoglPipeline.clientActiveTextureUnit()");
    if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
      gl.glClientActiveTexture(texUnit + GL.GL_TEXTURE0);
    }
  }


  /* 
   * NOTE: pass is unused and must be < 0 which implies that we will
   * send all texture unit state info in one pass
   */
  private void
    executeTexture(int pass, int texCoordSetMapLen,
                   int texSize, int bstride, int texCoordoff,
                   int[] texCoordSetMapOffset,
                   int numActiveTexUnit, int[] texUnitStateMap,
                   FloatBuffer verts, GL gl)
  {
    if (VERBOSE) System.err.println("JoglPipeline.executeTexture()");
    int tus = 0;  /* texture unit state index */
    
    for (int i = 0; i < numActiveTexUnit; i++) {
      /*
       * Null texUnitStateMap means 
       * one to one mapping from texture unit to
       * texture unit state.  It is null in build display list,
       * when the mapping is according to the texCoordSetMap
       */
      if (texUnitStateMap != null) {
        tus = texUnitStateMap[i];   
      } else {
        tus = i;
      }
      /*
       * it's possible that texture unit state index (tus)
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

  private void
    resetTexture(GL gl, JoglContext ctx)
  {
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
                           boolean multiScreen,
                           boolean ignoreVertexColors,
                           int startVIndex, int vcount, int vformat,
                           int texCoordSetCount, int[] texCoordSetMap,
                           int texCoordSetMapLen,
                           int[] texCoordSetMapOffset,
                           int numActiveTexUnitState,
                           int[] texUnitStateMapArray,
                           int vertexAttrCount, int[] vertexAttrSizes,
                           float[] varray, Buffer varrayBuffer,
                           float[] carray, int pass, int cDirty) {
    if (VERBOSE) System.err.println("JoglPipeline.executeGeometryArray()");
    JoglContext ctx = (JoglContext) absCtx;
    GLContext context = context(ctx);
    GL gl = context.getGL();

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
      }
      else { /* Handle the case of executeInterleaved 3f */
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

    bstride = stride * BufferUtil.SIZEOF_FLOAT;

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
      verts = (FloatBuffer) varrayBuffer;
    } else {
      // This should never happen
      throw new RuntimeException("JAVA 3D ERROR : unable to get vertex pointer");
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

    cbstride = cstride * BufferUtil.SIZEOF_FLOAT;
      
    // Enable normalize for non-uniform scale (which rescale can't handle)
    if (isNonUniformScale) {
      gl.glEnable(GL.GL_NORMALIZE);
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
          executeTexture(pass, texCoordSetMapLen,
                         texSize, bstride, texCoordoff,
                         texCoordSetMapOffset,
                         numActiveTexUnitState, texUnitStateMapArray,
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
        gl.glMultiDrawArraysEXT(primType, start_array, 0, sarray, 0, sarray.length);
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
          executeTexture(pass, texCoordSetMapLen,
                         texSize, bstride, texCoordoff,
                         texCoordSetMapOffset,
                         numActiveTexUnitState, texUnitStateMapArray,
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
        case GeometryRetained.GEO_TYPE_QUAD_SET : gl.glDrawArrays(GL.GL_QUADS,     0, vcount); break;
        case GeometryRetained.GEO_TYPE_TRI_SET  : gl.glDrawArrays(GL.GL_TRIANGLES, 0, vcount); break;
        case GeometryRetained.GEO_TYPE_POINT_SET: gl.glDrawArrays(GL.GL_POINTS,    0, vcount); break;
        case GeometryRetained.GEO_TYPE_LINE_SET : gl.glDrawArrays(GL.GL_LINES,     0, vcount); break;
      }
    }

    /* clean up if we turned on normalize */
    if (isNonUniformScale) {
      gl.glDisable(GL.GL_NORMALIZE);
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
  private void lockArray(GL gl, int vertexCount) {
    if (gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")) {
      gl.glLockArraysEXT(0, vertexCount);
    }
  }

  private void unlockArray(GL gl) {
    if (gl.isExtensionAvailable("GL_EXT_compiled_vertex_array")) {
      gl.glUnlockArraysEXT();
    }
  }

  private void
    executeGeometryArrayVA(Context absCtx,
                           GeometryArrayRetained geo,
                           int geo_type,
                           boolean isNonUniformScale,
                           boolean multiScreen,
                           boolean ignoreVertexColors,
                           int vcount,
                           int vformat,
                           int vdefined,
                           int initialCoordIndex, FloatBuffer fverts, DoubleBuffer dverts,
                           int initialColorIndex, FloatBuffer fclrs, ByteBuffer bclrs,
                           int initialNormalIndex, FloatBuffer norms,
                           int vertexAttrCount, int[] vertexAttrSizes,
                           int[] vertexAttrIndices, FloatBuffer[] vertexAttrData,
                           int pass, int texCoordMapLength,
                           int[] texCoordSetMap,
                           int numActiveTexUnit, int[] texUnitStateMap,
                           int[] texindices, int texStride, FloatBuffer[] texCoords,
                           int cdirty,
                           int[] sarray,
                           int strip_len,
                           int[] start_array) {
    JoglContext ctx = (JoglContext) absCtx;
    GLContext context = context(ctx);
    GL gl = context.getGL();

    boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
    boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
    boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
    boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
    boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
    boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
    boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

    // Enable normalize for non-uniform scale (which rescale can't handle)
    if (isNonUniformScale) {
      gl.glEnable(GL.GL_NORMALIZE);
    }

    int coordoff = 3 * initialCoordIndex;
    // Define the data pointers
    if (floatCoordDefined) {
      fverts.position(coordoff);
      gl.glVertexPointer(3, GL.GL_FLOAT, 0, fverts);
    } else if (doubleCoordDefined){
      dverts.position(coordoff);
      gl.glVertexPointer(3, GL.GL_DOUBLE, 0, dverts);
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
        int tus = texUnitStateMap[i];
        if ((tus < texCoordMapLength) &&
            ((texSet = texCoordSetMap[tus]) != -1)) {
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
        gl.glMultiDrawArraysEXT(primType, start_array, 0, sarray, 0, strip_len);
      } else if (gl.isExtensionAvailable("GL_VERSION_1_4")) {
        gl.glMultiDrawArrays(primType, start_array, 0, sarray, 0, strip_len);
      } else {
        for (int i = 0; i < strip_len; i++) {
          gl.glDrawArrays(primType, start_array[i], sarray[i]);
        }
      }
    } else {
      switch (geo_type){
        case GeometryRetained.GEO_TYPE_QUAD_SET  : gl.glDrawArrays(GL.GL_QUADS, 0, vcount);     break;
        case GeometryRetained.GEO_TYPE_TRI_SET   : gl.glDrawArrays(GL.GL_TRIANGLES, 0, vcount); break;
        case GeometryRetained.GEO_TYPE_POINT_SET : gl.glDrawArrays(GL.GL_POINTS, 0, vcount);    break;
        case GeometryRetained.GEO_TYPE_LINE_SET  : gl.glDrawArrays(GL.GL_LINES, 0, vcount);     break;
      }
    }

    // clean up if we turned on normalize
    if (isNonUniformScale) {
      gl.glDisable(GL.GL_NORMALIZE);
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
            float[] varray, float[] carray,
            int pass, int cdirty,
            int[] indexCoord) {
      if (VERBOSE) System.err.println("JoglPipeline.executeIndexedGeometry()");

      executeIndexedGeometryArray(ctx, geo, geo_type,
                                  isNonUniformScale, useAlpha, multiScreen, ignoreVertexColors,
                                  initialIndexIndex, indexCount,
                                  vertexCount, vformat,
                                  vertexAttrCount, vertexAttrSizes,
                                  texCoordSetCount, texCoordSetMap, texCoordSetMapLen,
                                  texCoordSetOffset,
                                  numActiveTexUnitState, texUnitStateMap,
                                  varray, null, carray,
                                  pass, cdirty, indexCoord);
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
            Object vdata, float[] carray,
            int pass, int cDirty,
            int[] indexCoord) {
      if (DEBUG) System.err.println("JoglPipeline.executeIndexedGeometryBuffer()");

      executeIndexedGeometryArray(ctx, geo, geo_type,
                                  isNonUniformScale, useAlpha, multiScreen, ignoreVertexColors,
                                  initialIndexIndex, indexCount, vertexCount, vformat,
                                  0, null,
                                  texCoordSetCount, texCoordSetMap, texCoordSetMapLen, texCoordSetOffset,
                                  numActiveTexUnitState, texUnitStateMap,
                                  null, (FloatBuffer) vdata, carray,
                                  pass, cDirty, indexCoord);
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
            int pass, int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
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
                                    isNonUniformScale, multiScreen, ignoreVertexColors,
                                    initialIndexIndex, validIndexCount, vertexCount,
                                    vformat, vdefined,
                                    fverts, dverts,
                                    fclrs, bclrs,
                                    norms,
                                    vertexAttrCount, vertexAttrSizes, vertexAttrBufs,
                                    pass, texCoordMapLength, texcoordoffset,
                                    numActiveTexUnitState, texunitstatemap, 
                                    texStride, texCoordBufs,
                                    cdirty, indexCoord,
                                    sarray, strip_len);
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
            Object ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            Object[] vertexAttrData,
            int pass, int texCoordMapLength,
            int[] texcoordoffset,
            int numActiveTexUnitState, int[] texunitstatemap,
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
        vertexAttrBufs = getVertexAttrSetBuffer(vertexAttrData);
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
        norms = (FloatBuffer) ndata;
      }

      executeIndexedGeometryArrayVA(ctx, geo, geo_type, 
                                    isNonUniformScale, multiScreen, ignoreVertexColors,
                                    initialIndexIndex, validIndexCount, vertexCount,
                                    vformat, vdefined,
                                    fverts, dverts,
                                    fclrs, bclrs,
                                    norms,
                                    vertexAttrCount, vertexAttrSizes, vertexAttrBufs,
                                    pass, texCoordMapLength, texcoordoffset,
                                    numActiveTexUnitState, texunitstatemap, 
                                    texStride, texCoordBufs,
                                    cdirty, indexCoord,
                                    sarray, strip_len);
    }

    // by-copy geometry
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
      GL gl = context(ctx).getGL();

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
	gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        stride += 3;
      } else {
	gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
      }

      if ((vformat & GeometryArray.NORMALS) != 0) {
	gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        stride += 3;
        coordoff += 3;
      } else {
	gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
      }

      if ((vformat & GeometryArray.COLOR) != 0) {
	gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        stride += 4;
        normoff += 4;
        coordoff += 4;
      } else {
	gl.glDisableClientState(GL.GL_COLOR_ARRAY);
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

      bstride = stride * BufferUtil.SIZEOF_FLOAT;

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
            executeTexture(-1, texCoordSetMapLen,
                           texSize, bstride, texCoordoff,
                           texCoordSetMapOffset,
                           texCoordSetMapLen, null,
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
            executeTexture(-1, texCoordSetMapLen,
                           texSize, bstride, texCoordoff,
                           texCoordSetMapOffset,
                           texCoordSetMapLen, null,
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
              primType = GL.GL_QUADS;
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
            float[] varray, FloatBuffer vdata, float[] carray,
            int pass, int cDirty,
            int[] indexCoord) {
    JoglContext ctx = (JoglContext) absCtx;
    GL gl = context(ctx).getGL();

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
    
    bstride = stride * BufferUtil.SIZEOF_FLOAT;

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
      throw new RuntimeException("JAVA 3D ERROR : unable to get vertex pointer");
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

    cbstride = cstride * BufferUtil.SIZEOF_FLOAT;

    // Enable normalize for non-uniform scale (which rescale can't handle)
    if (isNonUniformScale) {
      gl.glEnable(GL.GL_NORMALIZE);
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
          executeTexture(pass, texCoordSetMapLen,
                         texSize, bstride, texCoordoff,
                         texCoordSetOffset,
                         numActiveTexUnitState, texUnitStateMap,
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
          executeTexture(pass, texCoordSetMapLen,
                         texSize, bstride, texCoordoff,
                         texCoordSetOffset,
                         numActiveTexUnitState, texUnitStateMap,
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
        case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET : gl.glDrawElements(GL.GL_QUADS,     indexCount, GL.GL_UNSIGNED_INT, buf); break;
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
      gl.glDisable(GL.GL_NORMALIZE);
    }
  }


  private void executeIndexedGeometryArrayVA(Context absCtx,
            GeometryArrayRetained geo, int geo_type,
            boolean isNonUniformScale,
            boolean multiScreen,
            boolean ignoreVertexColors,
            int initialIndexIndex,
            int validIndexCount,
            int vertexCount, int vformat, int vdefined,
            FloatBuffer fverts, DoubleBuffer dverts,
            FloatBuffer fclrs, ByteBuffer bclrs,
            FloatBuffer norms,
            int vertexAttrCount, int[] vertexAttrSizes, FloatBuffer[] vertexAttrBufs,
            int pass, int texCoordSetCount, int[] texCoordSetMap,
            int numActiveTexUnitState,
            int[] texUnitStateMap,
            int texStride,
            FloatBuffer[] texCoords,
            int cDirty, int[] indexCoord, int[] sarray, int strip_len) {
    JoglContext ctx = (JoglContext) absCtx;
    GL gl = context(ctx).getGL();

    boolean floatCoordDefined  = ((vdefined & GeometryArrayRetained.COORD_FLOAT)    != 0);
    boolean doubleCoordDefined = ((vdefined & GeometryArrayRetained.COORD_DOUBLE)   != 0);
    boolean floatColorsDefined = ((vdefined & GeometryArrayRetained.COLOR_FLOAT)    != 0);
    boolean byteColorsDefined  = ((vdefined & GeometryArrayRetained.COLOR_BYTE)     != 0);
    boolean normalsDefined     = ((vdefined & GeometryArrayRetained.NORMAL_FLOAT)   != 0);
    boolean vattrDefined       = ((vdefined & GeometryArrayRetained.VATTR_FLOAT)    != 0);
    boolean textureDefined     = ((vdefined & GeometryArrayRetained.TEXCOORD_FLOAT) != 0);

    // Enable normalize for non-uniform scale (which rescale can't handle)
    if (isNonUniformScale) {
      gl.glEnable(GL.GL_NORMALIZE);
    }

    // Define the data pointers
    if (floatCoordDefined) {
      fverts.position(0);
      gl.glVertexPointer(3, GL.GL_FLOAT, 0, fverts);
    } else if (doubleCoordDefined){
      dverts.position(0);
      gl.glVertexPointer(3, GL.GL_DOUBLE, 0, dverts);
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
        int tus = texUnitStateMap[i];
        if ((tus < texCoordSetCount) &&
            ((texSet = texCoordSetMap[tus]) != -1)) {
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

    if (geo_type == GeometryRetained.GEO_TYPE_TRI_STRIP_SET || 
        geo_type == GeometryRetained.GEO_TYPE_TRI_FAN_SET   || 
        geo_type == GeometryRetained.GEO_TYPE_LINE_STRIP_SET) {
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
        case GeometryRetained.GEO_TYPE_INDEXED_QUAD_SET : gl.glDrawElements(GL.GL_QUADS,     validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
        case GeometryRetained.GEO_TYPE_INDEXED_TRI_SET  : gl.glDrawElements(GL.GL_TRIANGLES, validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
        case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET: gl.glDrawElements(GL.GL_POINTS,    validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
        case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET : gl.glDrawElements(GL.GL_LINES,     validIndexCount, GL.GL_UNSIGNED_INT, buf); break;
      }
    }

    unlockArray(gl);

    // clean up if we turned on normalize
    if (isNonUniformScale) {
      gl.glDisable(GL.GL_NORMALIZE);
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
    void readRasterNative(Context ctx,
            int type, int xSrcOffset, int ySrcOffset,
            int width, int height, int hCanvas, int format,
            ImageComponentRetained image,
            DepthComponentRetained depth,
            GraphicsContext3D gc) {
      if (VERBOSE) System.err.println("JoglPipeline.readRasterNative()");

      GL gl = context(ctx).getGL();
      gl.glPixelStorei(GL.GL_PACK_ROW_LENGTH, width);
      gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
      int yAdjusted = hCanvas - height - ySrcOffset;

      if ((type & Raster.RASTER_COLOR) != 0) {
        byte[] byteData = gc.byteBuffer;
        int oglFormat = 0;

        switch (format) {
          case ImageComponentRetained.BYTE_RGBA:         
            // all RGB types are stored as RGBA
            oglFormat = GL.GL_RGBA;
            break;
          case ImageComponentRetained.BYTE_RGB:         
            oglFormat = GL.GL_RGB;
            break;

          case ImageComponentRetained.BYTE_ABGR:         
            if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
              oglFormat = GL.GL_ABGR_EXT;
            }
            break;

          case ImageComponentRetained.BYTE_BGR:         
            oglFormat = GL.GL_BGR;
            break;

          case ImageComponentRetained.BYTE_LA: 
            // all LA types are stored as LA8
            oglFormat = GL.GL_LUMINANCE_ALPHA;
            break;
          case ImageComponentRetained.BYTE_GRAY:
          case ImageComponentRetained.USHORT_GRAY:
            throw new AssertionError("illegal format");
        }

        gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                        oglFormat, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(byteData));
      }

      if ((type & Raster.RASTER_DEPTH) != 0) {
        int wDepth = depth.width;
        int depthType = depth.type;

        if (depthType == DepthComponentRetained.DEPTH_COMPONENT_TYPE_INT) {
          int[] intData = gc.intBuffer;
          // yOffset is adjusted for OpenGL - Y upward
          gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                          GL.GL_DEPTH_COMPONENT, GL.GL_UNSIGNED_INT, IntBuffer.wrap(intData));
        } else {
          // DEPTH_COMPONENT_TYPE_FLOAT
          float[] floatData = gc.floatBuffer;
          // yOffset is adjusted for OpenGL - Y upward
          gl.glReadPixels(xSrcOffset, yAdjusted, width, height,
                          GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, FloatBuffer.wrap(floatData));
        }
      }
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
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform1i()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter1i(param.vParam(), value);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter1i(param.fParam(), value);
      }

      return null;
    }

    ShaderError setCgUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform1f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter1f(param.vParam(), value);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter1f(param.fParam(), value);
      }

      return null;
    }

    ShaderError setCgUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform2i()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter2i(param.vParam(), value[0], value[1]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter2i(param.fParam(), value[0], value[1]);
      }

      return null;
    }

    ShaderError setCgUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform2f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter2f(param.vParam(), value[0], value[1]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter2f(param.fParam(), value[0], value[1]);
      }

      return null;
    }

    ShaderError setCgUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform3i()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter3i(param.vParam(), value[0], value[1], value[2]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter3i(param.fParam(), value[0], value[1], value[2]);
      }

      return null;
    }

    ShaderError setCgUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform3f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter3f(param.vParam(), value[0], value[1], value[2]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter3f(param.fParam(), value[0], value[1], value[2]);
      }

      return null;
    }

    ShaderError setCgUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform4i()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter4i(param.vParam(), value[0], value[1], value[2], value[3]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter4i(param.fParam(), value[0], value[1], value[2], value[3]);
      }

      return null;
    }

    ShaderError setCgUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform4f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgSetParameter4f(param.vParam(), value[0], value[1], value[2], value[3]);
      }

      if (param.fParam() != null) {
        CgGL.cgSetParameter4f(param.fParam(), value[0], value[1], value[2], value[3]);
      }

      return null;
    }

    ShaderError setCgUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniformMatrix3f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetMatrixParameterfr(param.vParam(), value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetMatrixParameterfr(param.fParam(), value, 0);
      }

      return null;
    }

    ShaderError setCgUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniformMatrix4f()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetMatrixParameterfr(param.vParam(), value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetMatrixParameterfr(param.fParam(), value, 0);
      }

      return null;
    }

    // ShaderAttributeArray methods

    ShaderError setCgUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform1iArray()");

      float[] fval = new float[value.length];
      for (int i = 0; i < value.length; i++) {
        fval[i] = value[i];
      }

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray1f(param.vParam(), 0, numElements, fval, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray1f(param.fParam(), 0, numElements, fval, 0);
      }
      
      return null;
    }

    ShaderError setCgUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform1fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray1f(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray1f(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    ShaderError setCgUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform2iArray()");

      float[] fval = new float[value.length];
      for (int i = 0; i < value.length; i++) {
        fval[i] = value[i];
      }

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.vParam(), 0, numElements, fval, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.fParam(), 0, numElements, fval, 0);
      }

      return null;
    }

    ShaderError setCgUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform2fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    ShaderError setCgUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform3iArray()");

      float[] fval = new float[value.length];
      for (int i = 0; i < value.length; i++) {
        fval[i] = value[i];
      }

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray3f(param.vParam(), 0, numElements, fval, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray3f(param.fParam(), 0, numElements, fval, 0);
      }

      return null;
    }

    ShaderError setCgUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform3fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    ShaderError setCgUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform4iArray()");

      float[] fval = new float[value.length];
      for (int i = 0; i < value.length; i++) {
        fval[i] = value[i];
      }

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray4f(param.vParam(), 0, numElements, fval, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray4f(param.fParam(), 0, numElements, fval, 0);
      }

      return null;
    }

    ShaderError setCgUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniform4fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetParameterArray2f(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    ShaderError setCgUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniformMatrix3fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetMatrixParameterArrayfr(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetMatrixParameterArrayfr(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    ShaderError setCgUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setCgUniformMatrix4fArray()");

      JoglCgShaderParameter param = (JoglCgShaderParameter) uniformLocation;
      if (param.vParam() != null) {
        CgGL.cgGLSetMatrixParameterArrayfr(param.vParam(), 0, numElements, value, 0);
      }

      if (param.fParam() != null) {
        CgGL.cgGLSetMatrixParameterArrayfr(param.fParam(), 0, numElements, value, 0);
      }

      return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createCgShader(Context ctx, int shaderType, ShaderId[] shaderId) {
      if (VERBOSE) System.err.println("JoglPipeline.createCgShader()");

      JoglContext jctx = (JoglContext) ctx;
      JoglCgShaderInfo info = new JoglCgShaderInfo();
      info.setJ3DShaderType(shaderType);
      if (shaderType == Shader.SHADER_TYPE_VERTEX) {
        info.setShaderProfile(jctx.getCgVertexProfile());
      } else if (shaderType == Shader.SHADER_TYPE_FRAGMENT) {
        info.setShaderProfile(jctx.getCgFragmentProfile());
      } else {
        throw new AssertionError("unrecognized shaderType " + shaderType);
      }
      shaderId[0] = info;
      return null;
    }
    ShaderError destroyCgShader(Context ctx, ShaderId shaderId) {
      if (VERBOSE) System.err.println("JoglPipeline.destroyCgShader()");

      JoglCgShaderInfo info = (JoglCgShaderInfo) shaderId;
      CGprogram program = info.getCgShader();
      if (program != null) {
        CgGL.cgDestroyProgram(program);
      }
      return null;
    }
    ShaderError compileCgShader(Context ctx, ShaderId shaderId, String programString) {
      if (VERBOSE) System.err.println("JoglPipeline.compileCgShader()");

      if (programString == null)
        throw new AssertionError("shader program string is null");
      JoglCgShaderInfo info = (JoglCgShaderInfo) shaderId;
      JoglContext jctx = (JoglContext) ctx;
      CGprogram program = CgGL.cgCreateProgram(jctx.getCgContext(),
                                               CgGL.CG_SOURCE,
                                               programString,
                                               info.getShaderProfile(),
                                               null,
                                               null);
      int lastError = 0;
      if ((lastError = CgGL.cgGetError()) != 0) {
        ShaderError err = new ShaderError(ShaderError.COMPILE_ERROR,
                                          "Cg shader compile error");
        err.setDetailMessage(getCgErrorLog(jctx, lastError));
        return err;
      }
      info.setCgShader(program);
      return null;
    }

    ShaderError createCgShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.createCgShaderProgram()");

      JoglCgShaderProgramInfo info = new JoglCgShaderProgramInfo();
      shaderProgramId[0] = info;
      return null;
    }
    ShaderError destroyCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.destroyCgShaderProgram()");
      // Nothing to do in pure Java port
      return null;
    }
    ShaderError linkCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
      if (VERBOSE) System.err.println("JoglPipeline.linkCgShaderProgram()");

      JoglCgShaderProgramInfo shaderProgramInfo = (JoglCgShaderProgramInfo) shaderProgramId;
      // NOTE: we assume that the caller has already verified that there
      // is at most one vertex program and one fragment program
      shaderProgramInfo.setVertexShader(null);
      shaderProgramInfo.setFragmentShader(null);
      for (int i = 0; i < shaderIds.length; i++) {
        JoglCgShaderInfo shader = (JoglCgShaderInfo) shaderIds[i];
        if (shader.getJ3DShaderType() == Shader.SHADER_TYPE_VERTEX) {
          shaderProgramInfo.setVertexShader(shader);
        } else {
          shaderProgramInfo.setFragmentShader(shader);
        }

        CgGL.cgGLLoadProgram(shader.getCgShader());
        int lastError = 0;
        if ((lastError = CgGL.cgGetError()) != 0) {
          ShaderError err = new ShaderError(ShaderError.LINK_ERROR,
                                            "Cg shader link/load error");
          err.setDetailMessage(getCgErrorLog((JoglContext) ctx,
                                             lastError));
          return err;
        }

        CgGL.cgGLBindProgram(shader.getCgShader());
        if ((lastError = CgGL.cgGetError()) != 0) {
          ShaderError err = new ShaderError(ShaderError.LINK_ERROR,
                                            "Cg shader link/bind error");
          err.setDetailMessage(getCgErrorLog((JoglContext) ctx,
                                             lastError));
          return err;
        }
      }

      return null;
    }
    void lookupCgVertexAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr) {
      if (VERBOSE) System.err.println("JoglPipeline.lookupCgVertexAttrNames()");

      JoglCgShaderProgramInfo shaderProgramInfo = (JoglCgShaderProgramInfo) shaderProgramId;
      if (shaderProgramInfo.getVertexShader() == null) {
	// If there if no vertex shader, no attributes can be looked up, so all fail
        for (int i = 0; i < errArr.length; i++) {
          errArr[i] = false;
        }
        return;
      }

      shaderProgramInfo.setVertexAttributes(new CGparameter[numAttrNames]);
      for (int i = 0; i < numAttrNames; i++) {
        String attrName = attrNames[i];
        shaderProgramInfo.getVertexAttributes()[i] =
          CgGL.cgGetNamedParameter(shaderProgramInfo.getVertexShader().getCgShader(),
                                   attrName);
        if (shaderProgramInfo.getVertexAttributes()[i] == null) {
          errArr[i] = true;
        }
      }
    }
    void lookupCgShaderAttrNames(Context ctx, ShaderProgramId shaderProgramId,
            int numAttrNames, String[] attrNames, ShaderAttrLoc[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
      if (VERBOSE) System.err.println("JoglPipeline.lookupCgShaderAttrNames()");

      JoglCgShaderProgramInfo shaderProgramInfo =
        (JoglCgShaderProgramInfo) shaderProgramId;

      // Set the loc, type, and size arrays to out-of-bounds values
      for (int i = 0; i < numAttrNames; i++) {
	locArr[i] = null;
	typeArr[i] = -1;
	sizeArr[i] = -1;
      }

      int[] vType = new int[1];
      int[] vSize = new int[1];
      boolean[] vIsArray = new boolean[1];
      int[] fType = new int[1];
      int[] fSize = new int[1];
      boolean[] fIsArray = new boolean[1];

      boolean err = false;

      // Now lookup the location of each name in the attrNames array
      for (int i = 0; i < numAttrNames; i++) {
        String attrName = attrNames[i];
        // Get uniform attribute location -- note that we need to
        // lookup the name in both the vertex and fragment shader
        // (although we will generalize it to look at the list of "N"
        // shaders). If all parameter locations are NULL, then no
        // struct will be allocated and -1 will be stored for this
        // attribute. If there is more than one non-NULL parameter,
        // then all must be of the same type and dimensionality,
        // otherwise an error will be generated and -1 will be stored
        // for this attribute.  If all non-NULL parameters are of the
        // same type and dimensionality, then a struct is allocated
        // containing the list of parameters.
        //
        // When any of the setCgUniform methods are called, the
        // attribute will be set for each parameter in the list.
        CGparameter vLoc = null;
        if (shaderProgramInfo.getVertexShader() != null) {
          vLoc = lookupCgParams(shaderProgramInfo.getVertexShader(),
                                attrName,
                                vType, vSize, vIsArray);
          if (vLoc != null) {
            sizeArr[i] = vSize[0];
            isArrayArr[i] = vIsArray[0];
            typeArr[i] = cgToJ3dType(vType[0]);
          }
        }

        CGparameter fLoc = null;
        if (shaderProgramInfo.getVertexShader() != null) {
          fLoc = lookupCgParams(shaderProgramInfo.getFragmentShader(),
                                attrName,
                                fType, fSize, fIsArray);
          if (fLoc != null) {
            sizeArr[i] = fSize[0];
            isArrayArr[i] = fIsArray[0];
            typeArr[i] = cgToJ3dType(fType[0]);
          }
        }

        // If the name lookup found an entry in both vertex and
        // fragment program, verify that the type and size are the
        // same.
	if (vLoc != null && fLoc != null) {
          if (vType != fType || vSize != fSize || vIsArray != fIsArray) {
            // TODO: the following needs to be propagated to ShaderError
            System.err.println("JAVA 3D : error shader attribute type mismatch: " + attrName);
            System.err.println("    1 : type = " + vType[0] + ", size = " + vSize[0] + ", isArray = " + vIsArray[0]);
            System.err.println("    0 : type = " + fType[0] + ", size = " + fSize[0] + ", isArray = " + fIsArray[0]);
            err = true;
          }
	}

        // Report an error if we got a mismatch or if the attribute
        // was not found in either the vertex or the fragment program
        if (err || (vLoc == null && fLoc == null)) {
          // TODO: distinguish between (err) and (vParam and fParam both NULL)
          // so we can report a more helpful error message
          // locPtr[i] = (jlong)-1;
        } else {
          // TODO: need to store the cgParamInfo pointers in the
          // shader program so we can free them later.
          //
          // NOTE: WE CURRENTLY HAVE A MEMORY LEAK.
          locArr[i] = new JoglCgShaderParameter(vLoc, fLoc);
	}
      }
    }

    ShaderError useCgShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.useCgShaderProgram()");

      JoglCgShaderProgramInfo shaderProgramInfo =
        (JoglCgShaderProgramInfo) shaderProgramId;
      JoglContext jctx = (JoglContext) ctx;

      // Disable shader profiles
      CgGL.cgGLDisableProfile(jctx.getCgVertexProfile());
      CgGL.cgGLDisableProfile(jctx.getCgFragmentProfile());
      if (shaderProgramInfo != null) {
        if (shaderProgramInfo.getVertexShader() != null) {
          CgGL.cgGLBindProgram(shaderProgramInfo.getVertexShader().getCgShader());
          CgGL.cgGLEnableProfile(shaderProgramInfo.getVertexShader().getShaderProfile());
        } else {
          CgGL.cgGLUnbindProgram(jctx.getCgVertexProfile());
        }

        if (shaderProgramInfo.getFragmentShader() != null) {
          CgGL.cgGLBindProgram(shaderProgramInfo.getFragmentShader().getCgShader());
          CgGL.cgGLEnableProfile(shaderProgramInfo.getFragmentShader().getShaderProfile());
        } else {
          CgGL.cgGLUnbindProgram(jctx.getCgFragmentProfile());
        }
      } else {
        CgGL.cgGLUnbindProgram(jctx.getCgVertexProfile());
        CgGL.cgGLUnbindProgram(jctx.getCgFragmentProfile());
      }

      jctx.setShaderProgram(shaderProgramInfo);
      return null;
    }

    //
    // Helper methods for above
    //
    private String getCgErrorLog(JoglContext ctx, int lastError) {
      if (lastError == 0)
        throw new AssertionError("lastError == 0");
      String errString = CgGL.cgGetErrorString(lastError);
      String listing = CgGL.cgGetLastListing(ctx.getCgContext());
      return (errString + System.getProperty("line.separator") + listing);
    }

    private int cgToJ3dType(int type) {
      switch (type) {
        case CgGL.CG_BOOL:
        case CgGL.CG_BOOL1:
        case CgGL.CG_FIXED:
        case CgGL.CG_FIXED1:
        case CgGL.CG_HALF:
        case CgGL.CG_HALF1:
        case CgGL.CG_INT:
        case CgGL.CG_INT1:
          return ShaderAttributeObjectRetained.TYPE_INTEGER;
  
          // XXXX: add ShaderAttribute support for setting samplers. In the
          // mean time, the binding between sampler and texture unit will
          // need to be specified in the shader itself (which it already is
          // in most example shaders).
          //
          // case CgGL.CG_SAMPLER2D:
          // case CgGL.CG_SAMPLER3D:
          // case CgGL.CG_SAMPLERCUBE:
  
        case CgGL.CG_BOOL2:
        case CgGL.CG_FIXED2:
        case CgGL.CG_HALF2:
        case CgGL.CG_INT2:
          return ShaderAttributeObjectRetained.TYPE_TUPLE2I;
  
        case CgGL.CG_BOOL3:
        case CgGL.CG_FIXED3:
        case CgGL.CG_HALF3:
        case CgGL.CG_INT3:
          return ShaderAttributeObjectRetained.TYPE_TUPLE3I;
  
        case CgGL.CG_BOOL4:
        case CgGL.CG_FIXED4:
        case CgGL.CG_HALF4:
        case CgGL.CG_INT4:
          return ShaderAttributeObjectRetained.TYPE_TUPLE4I;
  
        case CgGL.CG_FLOAT:
        case CgGL.CG_FLOAT1:
          return ShaderAttributeObjectRetained.TYPE_FLOAT;
  
        case CgGL.CG_FLOAT2:
          return ShaderAttributeObjectRetained.TYPE_TUPLE2F;
  
        case CgGL.CG_FLOAT3:
          return ShaderAttributeObjectRetained.TYPE_TUPLE3F;
  
        case CgGL.CG_FLOAT4:
          return ShaderAttributeObjectRetained.TYPE_TUPLE4F;
  
        case CgGL.CG_FLOAT3x3:
          return ShaderAttributeObjectRetained.TYPE_MATRIX3F;
  
        case CgGL.CG_FLOAT4x4:
          return ShaderAttributeObjectRetained.TYPE_MATRIX4F;
  
          // Java 3D does not support the following sampler types:
          //
          // case CgGL.CG_SAMPLER1D:
          // case CgGL.CG_SAMPLERRECT:
      }

      return -1;
    }

    private CGparameter lookupCgParams(JoglCgShaderInfo shader,
                                       String attrNameString,
                                       int[] type,
                                       int[] size,
                                       boolean[] isArray) {
      CGparameter loc = CgGL.cgGetNamedParameter(shader.getCgShader(), attrNameString);
      if (loc != null) {
        type[0] = CgGL.cgGetParameterType(loc);
        if (type[0] == CgGL.CG_ARRAY) {
          isArray[0] = true;
          size[0] = CgGL.cgGetArraySize(loc, 0);
          CGparameter firstElem = CgGL.cgGetArrayParameter(loc, 0);
          type[0] = CgGL.cgGetParameterType(firstElem);
        } else {
          isArray[0] = false;
          size[0] = 1;
        }
      }
      return loc;
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
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1i()");

      context(ctx).getGL().glUniform1iARB(unbox(uniformLocation), value);
      return null;
    }

    ShaderError setGLSLUniform1f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1f()");

      context(ctx).getGL().glUniform1fARB(unbox(uniformLocation), value);
      return null;
    }

    ShaderError setGLSLUniform2i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2i()");

      context(ctx).getGL().glUniform2iARB(unbox(uniformLocation), value[0], value[1]);
      return null;
    }

    ShaderError setGLSLUniform2f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2f()");

      context(ctx).getGL().glUniform2fARB(unbox(uniformLocation), value[0], value[1]);
      return null;
    }

    ShaderError setGLSLUniform3i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3i()");

      context(ctx).getGL().glUniform3iARB(unbox(uniformLocation), value[0], value[1], value[2]);
      return null;
    }

    ShaderError setGLSLUniform3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3f()");

      context(ctx).getGL().glUniform3fARB(unbox(uniformLocation), value[0], value[1], value[2]);
      return null;
    }

    ShaderError setGLSLUniform4i(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4i()");

      context(ctx).getGL().glUniform4iARB(unbox(uniformLocation), value[0], value[1], value[2], value[3]);
      return null;
    }

    ShaderError setGLSLUniform4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4f()");

      context(ctx).getGL().glUniform4fARB(unbox(uniformLocation), value[0], value[1], value[2], value[3]);
      return null;
    }

    ShaderError setGLSLUniformMatrix3f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix3f()");

      // Load attribute
      // transpose is true : each matrix is supplied in row major order
      context(ctx).getGL().glUniformMatrix3fvARB(unbox(uniformLocation), 1, true, value, 0);
      return null;
    }

    ShaderError setGLSLUniformMatrix4f(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix4f()");

      // Load attribute
      // transpose is true : each matrix is supplied in row major order
      context(ctx).getGL().glUniformMatrix4fvARB(unbox(uniformLocation), 1, true, value, 0);
      return null;
    }

    // ShaderAttributeArray methods

    ShaderError setGLSLUniform1iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1iArray()");

      context(ctx).getGL().glUniform1ivARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform1fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform1fArray()");

      context(ctx).getGL().glUniform1fvARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform2iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2iArray()");

      context(ctx).getGL().glUniform2ivARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform2fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform2fArray()");

      context(ctx).getGL().glUniform2fvARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform3iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3iArray()");

      context(ctx).getGL().glUniform3ivARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform3fArray()");

      context(ctx).getGL().glUniform3fvARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform4iArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            int[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4iArray()");

      context(ctx).getGL().glUniform4ivARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniform4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniform4fArray()");

      context(ctx).getGL().glUniform4fvARB(unbox(uniformLocation), numElements, value, 0);
      return null;
    }

    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix3fArray()");

      // Load attribute
      // transpose is true : each matrix is supplied in row major order
      context(ctx).getGL().glUniformMatrix3fvARB(unbox(uniformLocation), numElements, true, value, 0);
      return null;
    }

    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            ShaderProgramId shaderProgramId,
            ShaderAttrLoc uniformLocation,
            int numElements,
            float[] value) {
      if (VERBOSE) System.err.println("JoglPipeline.setGLSLUniformMatrix4fArray()");

      // Load attribute
      // transpose is true : each matrix is supplied in row major order
      context(ctx).getGL().glUniformMatrix4fvARB(unbox(uniformLocation), numElements, true, value, 0);
      return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createGLSLShader(Context ctx, int shaderType, ShaderId[] shaderId) {
      if (VERBOSE) System.err.println("JoglPipeline.createGLSLShader()");

      GL gl = context(ctx).getGL();

      int shaderHandle = 0;
      if (shaderType == Shader.SHADER_TYPE_VERTEX) {
        shaderHandle = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
      } else if (shaderType == Shader.SHADER_TYPE_FRAGMENT) {
        shaderHandle = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
      }

      if (shaderHandle == 0) {
        return new ShaderError(ShaderError.COMPILE_ERROR,
                               "Unable to create native shader object");
      }
      
      shaderId[0] = new JoglShaderObject(shaderHandle);
      return null;
    }
    ShaderError destroyGLSLShader(Context ctx, ShaderId shaderId) {
      if (VERBOSE) System.err.println("JoglPipeline.destroyGLSLShader()");

      GL gl = context(ctx).getGL();
      gl.glDeleteObjectARB(unbox(shaderId));
      return null;
    }
    ShaderError compileGLSLShader(Context ctx, ShaderId shaderId, String program) {
      if (VERBOSE) System.err.println("JoglPipeline.compileGLSLShader()");

      int id = unbox(shaderId);
      if (id == 0) {
        throw new AssertionError("shaderId == 0");
      }

      if (program == null) {
        throw new AssertionError("shader program string is null");
      }

      GL gl = context(ctx).getGL();
      gl.glShaderSourceARB(id, 1, new String[] { program }, null, 0);
      gl.glCompileShaderARB(id);
      int[] status = new int[1];
      gl.glGetObjectParameterivARB(id, GL.GL_OBJECT_COMPILE_STATUS_ARB, status, 0);
      if (status[0] == 0) {
        String detailMsg = getInfoLog(gl, id);
        ShaderError res = new ShaderError(ShaderError.COMPILE_ERROR,
                                          "GLSL shader compile error");
        res.setDetailMessage(detailMsg);
        return res;
      }
      return null;
    }

    ShaderError createGLSLShaderProgram(Context ctx, ShaderProgramId[] shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.createGLSLShaderProgram()");

      GL gl = context(ctx).getGL();

      int shaderProgramHandle = gl.glCreateProgramObjectARB();
      if (shaderProgramHandle == 0) {
        return new ShaderError(ShaderError.LINK_ERROR,
                               "Unable to create native shader program object");
      }
      shaderProgramId[0] = new JoglShaderObject(shaderProgramHandle);
      return null;
    }
    ShaderError destroyGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.destroyGLSLShaderProgram()");
      context(ctx).getGL().glDeleteObjectARB(unbox(shaderProgramId));
      return null;
    }
    ShaderError linkGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId,
            ShaderId[] shaderIds) {
      if (VERBOSE) System.err.println("JoglPipeline.linkGLSLShaderProgram()");

      GL gl = context(ctx).getGL();
      int id = unbox(shaderProgramId);
      for (int i = 0; i < shaderIds.length; i++) {
        gl.glAttachObjectARB(id, unbox(shaderIds[i]));
      }
      gl.glLinkProgramARB(id);
      int[] status = new int[1];
      gl.glGetObjectParameterivARB(id, GL.GL_OBJECT_LINK_STATUS_ARB, status, 0);
      if (status[0] == 0) {
        String detailMsg = getInfoLog(gl, id);
        ShaderError res = new ShaderError(ShaderError.LINK_ERROR,
                                          "GLSL shader program link error");
        res.setDetailMessage(detailMsg);
        return res;
      }
      return null;
    }
    ShaderError bindGLSLVertexAttrName(Context ctx, ShaderProgramId shaderProgramId,
            String attrName, int attrIndex) {
      if (VERBOSE) System.err.println("JoglPipeline.bindGLSLVertexAttrName()");

      JoglContext jctx = (JoglContext) ctx;
      context(ctx).getGL().glBindAttribLocationARB(unbox(shaderProgramId),
                                                   attrIndex + VirtualUniverse.mc.glslVertexAttrOffset,
                                                   attrName);
      return null;
    }
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
      GL gl = context(ctx).getGL();
      gl.glGetObjectParameterivARB(id,
                                   GL.GL_OBJECT_ACTIVE_UNIFORMS_ARB,
                                   tmp, 0);
      int numActiveUniforms = tmp[0];
      gl.glGetObjectParameterivARB(id,
                                   GL.GL_OBJECT_ACTIVE_UNIFORM_MAX_LENGTH_ARB,
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

    ShaderError useGLSLShaderProgram(Context ctx, ShaderProgramId shaderProgramId) {
      if (VERBOSE) System.err.println("JoglPipeline.useGLSLShaderProgram()");

      context(ctx).getGL().glUseProgramObjectARB(unbox(shaderProgramId));
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

  private String getInfoLog(GL gl, int id) {
    int[] infoLogLength = new int[1];
    gl.glGetObjectParameterivARB(id, GL.GL_OBJECT_INFO_LOG_LENGTH_ARB, infoLogLength, 0);
    if (infoLogLength[0] > 0) {
      byte[] storage = new byte[infoLogLength[0]];
      int[] len = new int[1];
      gl.glGetInfoLogARB(id, infoLogLength[0], len, 0, storage, 0);
      try {
        return new String(storage, 0, len[0], "US-ASCII");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    return null;
  }

  private int glslToJ3dType(int type) {
    switch (type) {
      case GL.GL_BOOL_ARB:
      case GL.GL_INT:
      case GL.GL_SAMPLER_2D_ARB:
      case GL.GL_SAMPLER_3D_ARB:
      case GL.GL_SAMPLER_CUBE_ARB:
        return ShaderAttributeObjectRetained.TYPE_INTEGER;
  
      case GL.GL_FLOAT:
        return ShaderAttributeObjectRetained.TYPE_FLOAT;
  
      case GL.GL_INT_VEC2_ARB:
      case GL.GL_BOOL_VEC2_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE2I;
  
      case GL.GL_FLOAT_VEC2_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE2F;
  
      case GL.GL_INT_VEC3_ARB:
      case GL.GL_BOOL_VEC3_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE3I;
  
      case GL.GL_FLOAT_VEC3_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE3F;
  
      case GL.GL_INT_VEC4_ARB:
      case GL.GL_BOOL_VEC4_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE4I;
  
      case GL.GL_FLOAT_VEC4_ARB:
        return ShaderAttributeObjectRetained.TYPE_TUPLE4F;
  
        // case GL.GL_FLOAT_MAT2_ARB:
  
      case GL.GL_FLOAT_MAT3_ARB:
        return ShaderAttributeObjectRetained.TYPE_MATRIX3F;
  
      case GL.GL_FLOAT_MAT4_ARB:
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
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    void freeD3DSurface(ImageComponent2DRetained image, int hashId) {
      // Nothing to do
    }


    // ---------------------------------------------------------------------

    //
    // RasterRetained methods
    //

    // Native method that does the rendering
    void executeRaster(Context ctx, GeometryRetained geo,
            boolean updateAlpha, float alpha,
            int type, int wRaster, int hRaster,
            int xSrcOffset, int ySrcOffset,
            float x, float y, float z, byte[] imageYdown) {
      if (VERBOSE) System.err.println("JoglPipeline.executeRaster()");

      GL gl = context(ctx).getGL();
      RasterRetained raster = (RasterRetained) geo;

      if ((type == Raster.RASTER_COLOR) ||
          (type == Raster.RASTER_COLOR_DEPTH)) {
        ImageComponent2DRetained image = raster.image;
        if (image == null)
          return;
        int format = image.storedYdownFormat;
        int width = image.width;
        int height = image.height;
        // raster position is upper left corner, default for Java3D 
        // ImageComponent currently has the data reversed in Y
        gl.glPixelZoom(1.0f, -1.0f);
        if (xSrcOffset >= 0) {
          gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, xSrcOffset);
          if (xSrcOffset + wRaster > width) {
            wRaster = width - xSrcOffset;
          }
        } else {
          wRaster += xSrcOffset;
          if (wRaster > width) {
            wRaster = width;
          }
        }
        if (ySrcOffset >= 0) {
          gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, ySrcOffset);
          if (ySrcOffset + hRaster > height) {
            hRaster = height - ySrcOffset;
          }
        } else {
          hRaster += ySrcOffset;
          if (hRaster > height) {
            hRaster = height;
          }
        }

        gl.glRasterPos3f(x, y, z);

        int oglFormat = 0;
        switch (format) {
          case ImageComponentRetained.BYTE_RGBA:
            // all RGB types are stored as RGBA
            oglFormat = GL.GL_RGBA;
            break;
          case ImageComponentRetained.BYTE_RGB:
            oglFormat = GL.GL_RGB;
            break;

          case ImageComponentRetained.BYTE_ABGR:
            if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
              oglFormat = GL.GL_ABGR_EXT;
            }
            break;

          case ImageComponentRetained.BYTE_BGR:
            oglFormat = GL.GL_BGR;
            break;

          case ImageComponentRetained.BYTE_LA:
            // all LA types are stored as LA8
            oglFormat = GL.GL_LUMINANCE_ALPHA;
            break;
          case ImageComponentRetained.BYTE_GRAY:
          case ImageComponentRetained.USHORT_GRAY:
            throw new AssertionError("illegal format");
        }

        gl.glDrawPixels(wRaster, hRaster, oglFormat, GL.GL_UNSIGNED_BYTE,
                        ByteBuffer.wrap(imageYdown));
      }

      if ((type == Raster.RASTER_DEPTH) ||
          (type == Raster.RASTER_COLOR_DEPTH)) {
        DepthComponentRetained depth = raster.depthComponent;
        if (depth == null)
          return;
        int depthType = depth.type;
        int width = depth.width;
        int height = depth.height;
        int[] drawBuf = new int[1];

        gl.glGetIntegerv(GL.GL_DRAW_BUFFER, drawBuf, 0);
        // disable draw buffer
	gl.glDrawBuffer(GL.GL_NONE);

        // raster position is upper left corner, default for Java3D 
        // ImageComponent currently has the data reversed in Y
	gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, width);
        if (xSrcOffset >= 0) {
          gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, xSrcOffset);
          if (xSrcOffset + wRaster > width) {
            wRaster = width - xSrcOffset;
          }
        } else {
          wRaster += xSrcOffset;
          if (wRaster > width) {
            wRaster = width;
          }
        }
        if (ySrcOffset >= 0) {
          gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, ySrcOffset);
          if (ySrcOffset + hRaster > height) {
            hRaster = height - ySrcOffset;
          }
        } else {
          hRaster += ySrcOffset;
          if (hRaster > height) {
            hRaster = height;
          }
        }
        
        if (depthType == DepthComponentRetained.DEPTH_COMPONENT_TYPE_INT) {
          int[] intData = ((DepthComponentIntRetained) depth).depthData;
          gl.glDrawPixels(wRaster, hRaster, GL.GL_DEPTH_COMPONENT,
                          GL.GL_UNSIGNED_INT, IntBuffer.wrap(intData));
        } else {
          // DepthComponentRetained.DEPTH_COMPONENT_TYPE_FLOAT
          float[] floatData = ((DepthComponentFloatRetained) depth).depthData;
          gl.glDrawPixels(wRaster, hRaster, GL.GL_DEPTH_COMPONENT,
                          GL.GL_FLOAT, FloatBuffer.wrap(floatData));
        }

        // re-enable draw buffer
        gl.glDrawBuffer(drawBuf[0]);
      }

      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, 0);
    }


    // ---------------------------------------------------------------------

    //
    // Renderer methods
    //

    void cleanupRenderer() {
      // Nothing to do
    }


    // ---------------------------------------------------------------------

    //
    // ColoringAttributesRetained methods
    //

    void updateColoringAttributes(Context ctx,
            float dRed, float dGreen, float dBlue,
            float red, float green, float blue,
            float alpha,
            boolean lightEnable,
            int shadeModel) {
      if (VERBOSE) System.err.println("JoglPipeline.updateColoringAttributes()");

      GL gl = context(ctx).getGL();

      float cr, cg, cb;

      if (lightEnable) {
        cr = dRed; cg = dGreen; cb = dBlue;
      } else {
        cr = red; cg = green; cb = blue;
      }
      gl.glColor4f(cr, cg, cb, alpha);
      if (shadeModel == ColoringAttributes.SHADE_FLAT) {
        gl.glShadeModel(GL.GL_FLAT);
      } else {
        gl.glShadeModel(GL.GL_SMOOTH);
      }
    }


    // ---------------------------------------------------------------------

    //
    // DirectionalLightRetained methods
    //

    private static final float[] black = new float[4];
    void updateDirectionalLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float dirx, float diry, float dirz) {
      if (VERBOSE) System.err.println("JoglPipeline.updateDirectionalLight()");
      
      GL gl = context(ctx).getGL();

      int lightNum = GL.GL_LIGHT0 + lightSlot;
      float[] values = new float[4];

      values[0] = red;
      values[1] = green;
      values[2] = blue;
      values[3] = 1.0f;
      gl.glLightfv(lightNum, GL.GL_DIFFUSE, values, 0);
      gl.glLightfv(lightNum, GL.GL_SPECULAR, values, 0);
      values[0] = -dirx;
      values[1] = -diry;
      values[2] = -dirz;
      values[3] = 0.0f;
      gl.glLightfv(lightNum, GL.GL_POSITION, values, 0);
      gl.glLightfv(lightNum, GL.GL_AMBIENT, black, 0);
      gl.glLightf(lightNum, GL.GL_CONSTANT_ATTENUATION, 1.0f);
      gl.glLightf(lightNum, GL.GL_LINEAR_ATTENUATION, 0.0f);
      gl.glLightf(lightNum, GL.GL_QUADRATIC_ATTENUATION, 0.0f);
      gl.glLightf(lightNum, GL.GL_SPOT_EXPONENT, 0.0f);
      gl.glLightf(lightNum, GL.GL_SPOT_CUTOFF, 180.0f);
    }


    // ---------------------------------------------------------------------

    //
    // PointLightRetained methods
    //

    void updatePointLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float attenx, float atteny, float attenz,
            float posx, float posy, float posz) {
      if (VERBOSE) System.err.println("JoglPipeline.updatePointLight()");

      GL gl = context(ctx).getGL();

      int lightNum = GL.GL_LIGHT0 + lightSlot;
      float[] values = new float[4];

      values[0] = red;
      values[1] = green;
      values[2] = blue;
      values[3] = 1.0f;
      gl.glLightfv(lightNum, GL.GL_DIFFUSE, values, 0);
      gl.glLightfv(lightNum, GL.GL_SPECULAR, values, 0);
      gl.glLightfv(lightNum, GL.GL_AMBIENT, black, 0);
      values[0] = posx;
      values[1] = posy;
      values[2] = posz;
      gl.glLightfv(lightNum, GL.GL_POSITION, values, 0);
      gl.glLightf(lightNum, GL.GL_CONSTANT_ATTENUATION, attenx);
      gl.glLightf(lightNum, GL.GL_LINEAR_ATTENUATION, atteny);
      gl.glLightf(lightNum, GL.GL_QUADRATIC_ATTENUATION, attenz);
      gl.glLightf(lightNum, GL.GL_SPOT_EXPONENT, 0.0f);
      gl.glLightf(lightNum, GL.GL_SPOT_CUTOFF, 180.0f);
    }


    // ---------------------------------------------------------------------

    //
    // SpotLightRetained methods
    //

    void updateSpotLight(Context ctx,
            int lightSlot, float red, float green,
            float blue, float attenx, float atteny, float attenz,
            float posx, float posy, float posz, float spreadAngle,
            float concentration, float dirx, float diry,
            float dirz) {
      if (VERBOSE) System.err.println("JoglPipeline.updateSpotLight()");

      GL gl = context(ctx).getGL();

      int lightNum = GL.GL_LIGHT0 + lightSlot;
      float[] values = new float[4];

      values[0] = red;
      values[1] = green;
      values[2] = blue;
      values[3] = 1.0f;
      gl.glLightfv(lightNum, GL.GL_DIFFUSE, values, 0);
      gl.glLightfv(lightNum, GL.GL_SPECULAR, values, 0);
      gl.glLightfv(lightNum, GL.GL_AMBIENT, black, 0);
      values[0] = posx;
      values[1] = posy;
      values[2] = posz;
      gl.glLightfv(lightNum, GL.GL_POSITION, values, 0);
      gl.glLightf(lightNum, GL.GL_CONSTANT_ATTENUATION, attenx);
      gl.glLightf(lightNum, GL.GL_LINEAR_ATTENUATION, atteny);
      gl.glLightf(lightNum, GL.GL_QUADRATIC_ATTENUATION, attenz);
      values[0] = dirx;
      values[1] = diry;
      values[2] = dirz;
      gl.glLightfv(lightNum, GL.GL_SPOT_DIRECTION, values, 0);
      gl.glLightf(lightNum, GL.GL_SPOT_EXPONENT, concentration);
      gl.glLightf(lightNum, GL.GL_SPOT_CUTOFF, (float) (spreadAngle * 180.0f / Math.PI));
    }


    // ---------------------------------------------------------------------

    //
    // ExponentialFogRetained methods
    //

    void updateExponentialFog(Context ctx,
            float red, float green, float blue,
            float density) {
      if (VERBOSE) System.err.println("JoglPipeline.updateExponentialFog()");

      GL gl = context(ctx).getGL();

      float[] color = new float[3];
      color[0] = red;
      color[1] = green;
      color[2] = blue;
      gl.glFogi(GL.GL_FOG_MODE, GL.GL_EXP);
      gl.glFogfv(GL.GL_FOG_COLOR, color, 0);
      gl.glFogf(GL.GL_FOG_DENSITY, density);
      gl.glEnable(GL.GL_FOG);
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
      if (VERBOSE) System.err.println("JoglPipeline.updateLinearFog()");

      GL gl = context(ctx).getGL();

      float[] color = new float[3];
      color[0] = red;
      color[1] = green;
      color[2] = blue;
      gl.glFogi(GL.GL_FOG_MODE, GL.GL_LINEAR);
      gl.glFogfv(GL.GL_FOG_COLOR, color, 0);
      gl.glFogf(GL.GL_FOG_START, (float) fdist);
      gl.glFogf(GL.GL_FOG_END, (float) bdist);
      gl.glEnable(GL.GL_FOG);
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
      if (VERBOSE) System.err.println("JoglPipeline.updateLineAttributes()");

      GL gl = context(ctx).getGL();
      gl.glLineWidth(lineWidth);

      if (linePattern == LineAttributes.PATTERN_SOLID) {
        gl.glDisable(GL.GL_LINE_STIPPLE);
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
        gl.glEnable(GL.GL_LINE_STIPPLE);
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

    void updateMaterial(Context ctx,
            float red, float green, float blue, float alpha,
            float aRed, float aGreen, float aBlue,
            float eRed, float eGreen, float eBlue,
            float dRed, float dGreen, float dBlue,
            float sRed, float sGreen, float sBlue,
            float shininess, int colorTarget, boolean lightEnable) {
      if (VERBOSE) System.err.println("JoglPipeline.updateMaterial()");

      float[] color = new float[4];

      GL gl = context(ctx).getGL();

      gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
      switch (colorTarget) {
        case Material.DIFFUSE:
          gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
          break;
        case Material.AMBIENT:
          gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT);
          break;
        case Material.EMISSIVE:
          gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION);
          break;
        case Material.SPECULAR:
          gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR);
          break;
        case Material.AMBIENT_AND_DIFFUSE:
          gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
          break;
      }

      color[0] = eRed; color[1] = eGreen; color[2] = eBlue;
      gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_EMISSION, color, 0);
 
      color[0] = aRed; color[1] = aGreen; color[2] = aBlue;
      gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, color, 0);

      color[0] = sRed; color[1] = sGreen; color[2] = sBlue;
      gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, color, 0);
  
      float cr, cg, cb;

      if (lightEnable) {
        color[0] = dRed; color[1] = dGreen; color[2] = dBlue;
      } else {
        color[0] = red; color[1] = green; color[2] = blue;
      }
      color[3] = alpha;
      gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, color, 0);
      gl.glColor4f(color[0], color[1], color[2], color[3]);

      if (lightEnable) {
        gl.glEnable(GL.GL_LIGHTING);
      } else {
        gl.glDisable(GL.GL_LIGHTING);
      }
    }


    // ---------------------------------------------------------------------

    //
    // ModelClipRetained methods
    //

    void updateModelClip(Context ctx, int planeNum, boolean enableFlag,
            double A, double B, double C, double D) {
      if (VERBOSE) System.err.println("JoglPipeline.updateModelClip()");

      GL gl = context(ctx).getGL();

      double[] equation = new double[4];
      int pl = GL.GL_CLIP_PLANE0 + planeNum;

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

    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
      if (VERBOSE) System.err.println("JoglPipeline.updatePointAttributes()");

      GL gl = context(ctx).getGL();
      gl.glPointSize(pointSize);

      // XXXX: Polygon Mode check, blend enable
      if (pointAntialiasing) {
        gl.glEnable(GL.GL_POINT_SMOOTH);
      } else {
        gl.glDisable(GL.GL_POINT_SMOOTH);
      }
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
      if (VERBOSE) System.err.println("JoglPipeline.updatePolygonAttributes()");

      GL gl = context(ctx).getGL();

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
        gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);
      } else {
        gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);
      }

      if (polygonMode == PolygonAttributes.POLYGON_POINT) {
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_POINT);
      } else if (polygonMode == PolygonAttributes.POLYGON_LINE) {
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
      } else {
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
      }
    
      gl.glPolygonOffset(polygonOffsetFactor, polygonOffset);
    
      if ((polygonOffsetFactor != 0.0) || (polygonOffset != 0.0)) {
        switch (polygonMode) {
        case PolygonAttributes.POLYGON_POINT:
          gl.glEnable(GL.GL_POLYGON_OFFSET_POINT);
          gl.glDisable(GL.GL_POLYGON_OFFSET_LINE);
          gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
          break;
        case PolygonAttributes.POLYGON_LINE:
          gl.glEnable(GL.GL_POLYGON_OFFSET_LINE);
          gl.glDisable(GL.GL_POLYGON_OFFSET_POINT);
          gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
          break;
        case PolygonAttributes.POLYGON_FILL:
          gl.glEnable(GL.GL_POLYGON_OFFSET_FILL); 
          gl.glDisable(GL.GL_POLYGON_OFFSET_POINT);
          gl.glDisable(GL.GL_POLYGON_OFFSET_LINE);
          break;
        }
      } else {
        gl.glDisable(GL.GL_POLYGON_OFFSET_POINT);
        gl.glDisable(GL.GL_POLYGON_OFFSET_LINE);
        gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
      }
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
      if (VERBOSE) System.err.println("JoglPipeline.updateRenderingAttributes()");

      GL gl = context(ctx).getGL();

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
        gl.glDisable(GL.GL_ALPHA_TEST);
      } else {
        gl.glEnable(GL.GL_ALPHA_TEST);
        gl.glAlphaFunc(getFunctionValue(alphaTestFunction), alphaTestValue);
      }

      if (ignoreVertexColors) {
        gl.glDisable(GL.GL_COLOR_MATERIAL);
      } else {
        gl.glEnable(GL.GL_COLOR_MATERIAL);
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
    void updateTexCoordGeneration(Context ctx,
            boolean enable, int genMode, int format,
            float planeSx, float planeSy, float planeSz, float planeSw,
            float planeTx, float planeTy, float planeTz, float planeTw,
            float planeRx, float planeRy, float planeRz, float planeRw,
            float planeQx, float planeQy, float planeQz, float planeQw,
            double[] vworldToEc) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexCoordGeneration()");

      GL gl = context(ctx).getGL();

      float[] planeS = new float[4];
      float[] planeT = new float[4];
      float[] planeR = new float[4];
      float[] planeQ = new float[4];

      if (enable) {
        gl.glEnable(GL.GL_TEXTURE_GEN_S);
        gl.glEnable(GL.GL_TEXTURE_GEN_T);
        if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
          gl.glEnable(GL.GL_TEXTURE_GEN_R);
          gl.glDisable(GL.GL_TEXTURE_GEN_Q);
        } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
          gl.glEnable(GL.GL_TEXTURE_GEN_R);
          gl.glEnable(GL.GL_TEXTURE_GEN_Q);
        } else {
          gl.glDisable(GL.GL_TEXTURE_GEN_R);
          gl.glDisable(GL.GL_TEXTURE_GEN_Q);
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
          gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
          gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
          gl.glTexGenfv(GL.GL_S, GL.GL_OBJECT_PLANE, planeS, 0);
          gl.glTexGenfv(GL.GL_T, GL.GL_OBJECT_PLANE, planeT, 0);

          if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
            gl.glTexGenfv(GL.GL_R, GL.GL_OBJECT_PLANE, planeR, 0);
          } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
            gl.glTexGenfv(GL.GL_R, GL.GL_OBJECT_PLANE, planeR, 0);
            gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_OBJECT_LINEAR);
            gl.glTexGenfv(GL.GL_Q, GL.GL_OBJECT_PLANE, planeQ, 0);
          }
          break;
        case TexCoordGeneration.EYE_LINEAR:

          gl.glMatrixMode(GL.GL_MODELVIEW);
          gl.glPushMatrix();

          if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
            gl.glLoadTransposeMatrixd(vworldToEc, 0);
          } else {
            double[] v = new double[16];
            copyTranspose(vworldToEc, v);
            gl.glLoadMatrixd(v, 0);
          }

          gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR);
          gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR);
          gl.glTexGenfv(GL.GL_S, GL.GL_EYE_PLANE, planeS, 0);
          gl.glTexGenfv(GL.GL_T, GL.GL_EYE_PLANE, planeT, 0);

          if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR);
            gl.glTexGenfv(GL.GL_R, GL.GL_EYE_PLANE, planeR, 0);
          } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR);
            gl.glTexGenfv(GL.GL_R, GL.GL_EYE_PLANE, planeR, 0);
            gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_EYE_LINEAR);
            gl.glTexGenfv(GL.GL_Q, GL.GL_EYE_PLANE, planeQ, 0);
          }
          gl.glPopMatrix();
          break;
        case TexCoordGeneration.SPHERE_MAP:
          gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
          gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
          if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
          } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
            gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_SPHERE_MAP);
          }

          break;
        case TexCoordGeneration.NORMAL_MAP:
          gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
          gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
          if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
          } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
            gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_NORMAL_MAP);
          }
          break;
        case TexCoordGeneration.REFLECTION_MAP:
          gl.glTexGeni(GL.GL_S, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
          gl.glTexGeni(GL.GL_T, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
          if (format == TexCoordGeneration.TEXTURE_COORDINATE_3) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
          } else if (format == TexCoordGeneration.TEXTURE_COORDINATE_4) {
            gl.glTexGeni(GL.GL_R, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
            gl.glTexGeni(GL.GL_Q, GL.GL_TEXTURE_GEN_MODE, GL.GL_REFLECTION_MAP);
          }
          break;
        }
      } else {
        gl.glDisable(GL.GL_TEXTURE_GEN_S);
        gl.glDisable(GL.GL_TEXTURE_GEN_T);
        gl.glDisable(GL.GL_TEXTURE_GEN_R);
        gl.glDisable(GL.GL_TEXTURE_GEN_Q);
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
    int eachLen = screen_door[0].length;
    ByteBuffer buf = BufferUtil.newByteBuffer(screen_door.length * eachLen * BufferUtil.SIZEOF_INT);
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
    blendFunctionTable[TransparencyAttributes.BLEND_CONSTANT_COLOR] = GL.GL_CONSTANT_COLOR;
  }

    void updateTransparencyAttributes(Context ctx,
            float alpha, int geometryType,
            int polygonMode,
            boolean lineAA, boolean pointAA,
            int transparencyMode,
            int srcBlendFunction,
            int dstBlendFunction) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTransparencyAttributes()");

      GL gl = context(ctx).getGL();

      if (transparencyMode != TransparencyAttributes.SCREEN_DOOR) {
        gl.glDisable(GL.GL_POLYGON_STIPPLE);
      } else  {
        gl.glEnable(GL.GL_POLYGON_STIPPLE);
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

    void updateTextureAttributes(Context ctx,
            double[] transform, boolean isIdentity, int textureMode,
            int perspCorrectionMode,
            float textureBlendColorRed,
            float textureBlendColorGreen,
            float textureBlendColorBlue,
            float textureBlendColorAlpha,
            int textureFormat) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureAttributes()");

      GL gl = context(ctx).getGL();
      gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT,
                (perspCorrectionMode == TextureAttributes.NICEST) ? GL.GL_NICEST : GL.GL_FASTEST);

      // set OGL texture matrix
      gl.glPushAttrib(GL.GL_MATRIX_MODE);
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
      gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, color, 0);

      // set texture environment mode

      switch (textureMode) {
        case TextureAttributes.MODULATE:
          gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
          break;
        case TextureAttributes.DECAL:
          gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
          break;
        case TextureAttributes.BLEND:
          gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_BLEND);
          break;
        case TextureAttributes.REPLACE:
          gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
          break;
        case TextureAttributes.COMBINE:
          gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
          break;
      }

      if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
        gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
      }
    }

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
      if (VERBOSE) System.err.println("JoglPipeline.updateRegisterCombiners()");

      JoglContext ctx = (JoglContext) absCtx;
      GL gl = context(ctx).getGL();

      if (perspCorrectionMode == TextureAttributes.NICEST) {
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
      } else {
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_FASTEST);
      }

      // set OGL texture matrix
      gl.glPushAttrib(GL.GL_MATRIX_MODE);
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
      gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, color, 0);
      
      // set texture environment mode
      gl.glEnable(GL.GL_REGISTER_COMBINERS_NV);
      int textureUnit = ctx.getCurrentTextureUnit();
      int combinerUnit = ctx.getCurrentCombinerUnit();
      int fragment;
      if (combinerUnit == GL.GL_COMBINER0_NV) {
        fragment = GL.GL_PRIMARY_COLOR_NV;
      } else {
        fragment = GL.GL_SPARE0_NV;
      }
      
      switch (textureMode) {
        case TextureAttributes.MODULATE:
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_B_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_B_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);

          gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          break;

        case TextureAttributes.DECAL:
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_B_NV, textureUnit,
                               GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_C_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_D_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);

          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
                               GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);

          gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB, 
                                GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, GL.GL_SPARE0_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          break;

        case TextureAttributes.BLEND:
          gl.glCombinerParameterfvNV(GL.GL_CONSTANT_COLOR0_NV, color, 0);

          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_B_NV, textureUnit,
                               GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_C_NV, GL.GL_CONSTANT_COLOR0_NV,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_D_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
	    
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_A_NV, fragment,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_B_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
	    
          gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB, 
                                GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, GL.GL_SPARE0_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          break;

        case TextureAttributes.REPLACE:
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_A_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                               GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
                               GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_A_NV, textureUnit,
                               GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);
          gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                               GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
                               GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);

          gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA, 
                                GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                GL.GL_NONE, GL.GL_NONE, false, false, false);
          break;

        case TextureAttributes.COMBINE:
          if (combineRgbMode == TextureAttributes.COMBINE_DOT3) {
            int color1 = getCombinerArg(gl, combineRgbSrc[0], textureUnit, combinerUnit);
            gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                                 GL.GL_VARIABLE_A_NV, color1,
                                 GL.GL_EXPAND_NORMAL_NV, GL.GL_RGB);
            int color2 = getCombinerArg(gl, combineRgbSrc[1], textureUnit, combinerUnit);
            gl.glCombinerInputNV(combinerUnit, GL.GL_RGB, 
                                 GL.GL_VARIABLE_B_NV, color2,
                                 GL.GL_EXPAND_NORMAL_NV, GL.GL_RGB);
            gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                                 GL.GL_VARIABLE_A_NV, GL.GL_ZERO,
                                 GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
            gl.glCombinerInputNV(combinerUnit, GL.GL_ALPHA, 
                                 GL.GL_VARIABLE_B_NV, GL.GL_ZERO,
                                 GL.GL_UNSIGNED_INVERT_NV, GL.GL_ALPHA);
	    
            gl.glCombinerOutputNV(combinerUnit, GL.GL_RGB, 
				  GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
                                  GL.GL_NONE/*SCALE_BY_FOUR_NV*/, GL.GL_NONE, true,
				  false, false);
            gl.glCombinerOutputNV(combinerUnit, GL.GL_ALPHA, 
			          GL.GL_SPARE0_NV, GL.GL_DISCARD_NV, GL.GL_DISCARD_NV, 
			          GL.GL_NONE, GL.GL_NONE, false, 
				  false, false);
          }
          break;
      }

      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_A_NV, 
                                GL.GL_SPARE0_NV, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_B_NV, 
                                GL.GL_ZERO, GL.GL_UNSIGNED_INVERT_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_C_NV, 
                                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_D_NV, 
                                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_E_NV, 
                                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_F_NV, 
                                GL.GL_ZERO, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_RGB);
      gl.glFinalCombinerInputNV(GL.GL_VARIABLE_G_NV, 
                                GL.GL_SPARE0_NV, GL.GL_UNSIGNED_IDENTITY_NV, GL.GL_ALPHA);

      if (gl.isExtensionAvailable("GL_SGI_texture_color_table"))
	gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
      // GL_SGI_texture_color_table
    }

    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] textureColorTable) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureColorTable()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
        if (numComponents == 3) {
          gl.glColorTable(GL.GL_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGB,
                          colorTableSize, GL.GL_RGB, GL.GL_INT, IntBuffer.wrap(textureColorTable));
        } else {
          gl.glColorTable(GL.GL_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGBA,
			    colorTableSize, GL.GL_RGBA, GL.GL_INT, IntBuffer.wrap(textureColorTable));
        }
	gl.glEnable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
      }
    }

    void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
      if (VERBOSE) System.err.println("JoglPipeline.updateCombiner()");

      GL gl = context(ctx).getGL();
      int[] GLrgbMode = new int[1];
      int[] GLalphaMode = new int[1];
      getGLCombineMode(gl, combineRgbMode, combineAlphaMode,
                       GLrgbMode, GLalphaMode);
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GLrgbMode[0]);
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_ALPHA, GLalphaMode[0]);

      int nargs;
      if (combineRgbMode == TextureAttributes.COMBINE_REPLACE) {
        nargs = 1;
      } else if (combineRgbMode == TextureAttributes.COMBINE_INTERPOLATE) {
        nargs = 3;
      } else {
        nargs = 2;
      }
      
      for (int i = 0; i < nargs; i++) {
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, _gl_combineRgbSrcIndex[i],
                     _gl_combineSrc[combineRgbSrc[i]]);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, _gl_combineRgbOpIndex[i],
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
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, _gl_combineAlphaSrcIndex[i], 
                     _gl_combineSrc[combineAlphaSrc[i]]);
        gl.glTexEnvi(GL.GL_TEXTURE_ENV, _gl_combineAlphaOpIndex[i], 
                     _gl_combineFcn[combineAlphaFcn[i]]);
      }

      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_RGB_SCALE, combineRgbScale);
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_ALPHA_SCALE, combineAlphaScale);
    }

  // Helper routines for above

  private void getGLCombineMode(GL gl, int combineRgbMode, int combineAlphaMode,
                                int[] GLrgbMode, int[] GLalphaMode) {
    switch (combineRgbMode) {
      case TextureAttributes.COMBINE_REPLACE:
        GLrgbMode[0] = GL.GL_REPLACE;
        break;
      case TextureAttributes.COMBINE_MODULATE:
        GLrgbMode[0] = GL.GL_MODULATE;
        break;
      case TextureAttributes.COMBINE_ADD:
        GLrgbMode[0] = GL.GL_ADD;
        break;
      case TextureAttributes.COMBINE_ADD_SIGNED:
        GLrgbMode[0] = GL.GL_ADD_SIGNED;
        break;
      case TextureAttributes.COMBINE_SUBTRACT:
        GLrgbMode[0] = GL.GL_SUBTRACT;
        break;
      case TextureAttributes.COMBINE_INTERPOLATE:
        GLrgbMode[0] = GL.GL_INTERPOLATE;
        break;
      case TextureAttributes.COMBINE_DOT3:
        GLrgbMode[0] = GL.GL_DOT3_RGB;
        break;
      default:
        break;
    }

    switch (combineAlphaMode) {
      case TextureAttributes.COMBINE_REPLACE:
        GLalphaMode[0] = GL.GL_REPLACE;
        break;
      case TextureAttributes.COMBINE_MODULATE:
        GLalphaMode[0] = GL.GL_MODULATE;
        break;
      case TextureAttributes.COMBINE_ADD:
        GLalphaMode[0] = GL.GL_ADD;
        break;
      case TextureAttributes.COMBINE_ADD_SIGNED:
        GLalphaMode[0] = GL.GL_ADD_SIGNED;
        break;
      case TextureAttributes.COMBINE_SUBTRACT:
        GLalphaMode[0] = GL.GL_SUBTRACT;
        break;
      case TextureAttributes.COMBINE_INTERPOLATE:
        GLalphaMode[0] = GL.GL_INTERPOLATE;
        break;
      case TextureAttributes.COMBINE_DOT3:
        // dot3 will only make sense for alpha if rgb is also
        // doing dot3. So if rgb is not doing dot3, fallback to replace
        if (combineRgbMode == TextureAttributes.COMBINE_DOT3) {
          GLrgbMode[0] = GL.GL_DOT3_RGBA;
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
    GL.GL_SOURCE0_RGB,
    GL.GL_SOURCE1_RGB,
    GL.GL_SOURCE2_RGB,
  };

  private static final int[] _gl_combineAlphaSrcIndex = {
    GL.GL_SOURCE0_ALPHA,
    GL.GL_SOURCE1_ALPHA,
    GL.GL_SOURCE2_ALPHA,
  };

  private static final int[] _gl_combineRgbOpIndex = {
    GL.GL_OPERAND0_RGB,
    GL.GL_OPERAND1_RGB,
    GL.GL_OPERAND2_RGB,
  };

  private static final int[] _gl_combineAlphaOpIndex = {
    GL.GL_OPERAND0_ALPHA,
    GL.GL_OPERAND1_ALPHA,
    GL.GL_OPERAND2_ALPHA,
  };

  private static final int[] _gl_combineSrc = {
    GL.GL_PRIMARY_COLOR,      // TextureAttributes.COMBINE_OBJECT_COLOR
    GL.GL_TEXTURE,            // TextureAttributes.COMBINE_TEXTURE
    GL.GL_CONSTANT,           // TextureAttributes.COMBINE_CONSTANT_COLOR
    GL.GL_PREVIOUS,           // TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE
  };

  private static final int[] _gl_combineFcn = {
    GL.GL_SRC_COLOR,              // TextureAttributes.COMBINE_SRC_COLOR
    GL.GL_ONE_MINUS_SRC_COLOR,    // TextureAttributes.COMBINE_ONE_MINUS_SRC_COLOR
    GL.GL_SRC_ALPHA,              // TextureAttributes.COMBINE_SRC_ALPHA
    GL.GL_ONE_MINUS_SRC_ALPHA,    // TextureAttributes.COMBINE_ONE_MINUS_SRC_ALPHA
  };

  private int getCombinerArg(GL gl, int arg, int textureUnit, int combUnit) {
    int comb = 0;

    switch (arg) {
      case TextureAttributes.COMBINE_OBJECT_COLOR:
        if (combUnit == GL.GL_COMBINER0_NV) {
          comb = GL.GL_PRIMARY_COLOR_NV;
	} else {
          comb = GL.GL_SPARE0_NV;
	}
	break;
      case TextureAttributes.COMBINE_TEXTURE_COLOR:
        comb = textureUnit;
	break;
      case TextureAttributes.COMBINE_CONSTANT_COLOR:
        comb = GL.GL_CONSTANT_COLOR0_NV;
	break;
      case TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE:
        comb = textureUnit -1;
	break;
    }

    return comb;
  }


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    void updateTextureUnitState(Context ctx, int index, boolean enable) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureUnitState()");

      GL gl = context(ctx).getGL();
      JoglContext jctx = (JoglContext) ctx;

      if (index >= 0 && gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glActiveTexture(index + GL.GL_TEXTURE0);
        gl.glClientActiveTexture(GL.GL_TEXTURE0 + index);
        if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
          jctx.setCurrentTextureUnit(index + GL.GL_TEXTURE0);
          jctx.setCurrentCombinerUnit(index + GL.GL_COMBINER0_NV);
          gl.glCombinerParameteriNV(GL.GL_NUM_GENERAL_COMBINERS_NV, index + 1);
        }
      }

      if (!enable) {
        // if not enabled, then don't enable any tex mapping
        gl.glDisable(GL.GL_TEXTURE_1D);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_TEXTURE_3D);
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

    void bindTexture2D(Context ctx, int objectId, boolean enable) {
      if (VERBOSE) System.err.println("JoglPipeline.bindTexture2D(objectId=" + objectId + ",enable=" + enable + ")");

      GL gl = context(ctx).getGL();
      gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
      gl.glDisable(GL.GL_TEXTURE_3D);

      if (!enable) {
        gl.glDisable(GL.GL_TEXTURE_2D);
      } else {
        gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);
        gl.glEnable(GL.GL_TEXTURE_2D);
      }
    }

    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DImage(width=" + width + ",height=" + height + ",level=" + level + ")");

      updateTexture2DImage(ctx, GL.GL_TEXTURE_2D,
                           numLevels, level, internalFormat, storedFormat,
                           width, height, boundaryWidth, imageData);
    }

    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DSubImage()");

      updateTexture2DSubImage(ctx, GL.GL_TEXTURE_2D,
                              level, xoffset, yoffset,
                              internalFormat, storedFormat,
                              imgXOffset, imgYOffset, tilew, width, height,
                              imageData);
    }

    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLOD, float maximumLOD) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DLodRange()");

      updateTextureLodRange(ctx, GL.GL_TEXTURE_2D,
                            baseLevel, maximumLevel,
                            minimumLOD, maximumLOD);
    }

    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DLodOffset()");

      updateTextureLodOffset(ctx, GL.GL_TEXTURE_2D, 
                             lodOffsetS, lodOffsetT, lodOffsetR);
    }

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

    void updateDetailTextureParameters(Context ctx,
            int detailTextureMode,
            int detailTextureLevel,
            int nPts, float[] pts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateDetailTextureParameters()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_SGIS_detail_texture")) {
        switch (detailTextureMode) {
        case Texture2D.DETAIL_ADD:
          gl.glTexParameterf(GL.GL_TEXTURE_2D, 
                             GL.GL_DETAIL_TEXTURE_MODE_SGIS, GL.GL_ADD);
          break;
        case Texture2D.DETAIL_MODULATE:
          gl.glTexParameterf(GL.GL_TEXTURE_2D, 
                             GL.GL_DETAIL_TEXTURE_MODE_SGIS, GL.GL_MODULATE);
          break;
        }

        gl.glTexParameteri(GL.GL_TEXTURE_2D,
                           GL.GL_DETAIL_TEXTURE_LEVEL_SGIS, -detailTextureLevel);

        gl.glDetailTexFuncSGIS(GL.GL_TEXTURE_2D, nPts, pts, 0);
      }
    }

    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DFilterModes()");

      updateTextureFilterModes(ctx, GL.GL_TEXTURE_2D, minFilter, magFilter);
    }

    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DSharpenFunc()");

      updateTextureSharpenFunc(ctx, GL.GL_TEXTURE_2D,
                               numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture2DFilter4Func()");

      updateTextureFilter4Func(ctx, GL.GL_TEXTURE_2D,
                               numFilter4FuncPts, filter4FuncPts);
    }

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
    gl.glTexParameteri(target, GL.GL_TEXTURE_BASE_LEVEL, baseLevel);
    gl.glTexParameteri(target, GL.GL_TEXTURE_MAX_LEVEL, maximumLevel);
    gl.glTexParameterf(target, GL.GL_TEXTURE_MIN_LOD, minimumLOD);
    gl.glTexParameterf(target, GL.GL_TEXTURE_MAX_LOD, maximumLOD);
  }

  private void updateTextureLodOffset(Context ctx,
                                      int target,
                                      float lodOffsetS, float lodOffsetT,
                                      float lodOffsetR) {
    GL gl = context(ctx).getGL();
    // checking of the availability of the extension is already done
    // in the shared code
    gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_S_SGIX, lodOffsetS);
    gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_T_SGIX, lodOffsetT);
    gl.glTexParameterf(target, GL.GL_TEXTURE_LOD_BIAS_R_SGIX, lodOffsetR);
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

    void bindTexture3D(Context ctx, int objectId, boolean enable) {
      if (VERBOSE) System.err.println("JoglPipeline.bindTexture3D()");

      GL gl = context(ctx).getGL();
      // textureCubeMap will take precedure over 3D Texture
      gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
    
      if (!enable) {
        gl.glDisable(GL.GL_TEXTURE_3D);
      } else {
        gl.glBindTexture(GL.GL_TEXTURE_3D, objectId);
        gl.glEnable(GL.GL_TEXTURE_3D);
      }
    }

    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height, int depth,
            int boundaryWidth,
            byte[] imageYup) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DImage()");

      GL gl = context(ctx).getGL();

      int oglFormat = 0;
      int oglInternalFormat = 0;

      switch (internalFormat) {
        case Texture.INTENSITY:
          oglInternalFormat = GL.GL_INTENSITY;
          break;
        case Texture.LUMINANCE:
          oglInternalFormat = GL.GL_LUMINANCE;
          break;
        case Texture.ALPHA:
          oglInternalFormat = GL.GL_ALPHA;
          break;
        case Texture.LUMINANCE_ALPHA:
          oglInternalFormat = GL.GL_LUMINANCE_ALPHA;
          break;
        case Texture.RGB:
          oglInternalFormat = GL.GL_RGB;
          break;
        case Texture.RGBA:
          oglInternalFormat = GL.GL_RGBA;
          break;
      }
      
      switch (storedFormat) {
        case ImageComponentRetained.BYTE_RGBA:
          // all RGB types are stored as RGBA
          oglFormat = GL.GL_RGBA;
          break;
        case ImageComponentRetained.BYTE_RGB:         
          oglFormat = GL.GL_RGB;
          break;
  
        case ImageComponentRetained.BYTE_ABGR:         
          if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
            oglFormat = GL.GL_ABGR_EXT;
          }
          break;
        case ImageComponentRetained.BYTE_BGR:         
          oglFormat = GL.GL_BGR;
          break;
        case ImageComponentRetained.BYTE_LA: 
          // all LA types are stored as LA8
          oglFormat = GL.GL_LUMINANCE_ALPHA;
          break;
        case ImageComponentRetained.BYTE_GRAY:
        case ImageComponentRetained.USHORT_GRAY:	    
          if (oglInternalFormat == GL.GL_ALPHA) {
            oglFormat = GL.GL_ALPHA;
          } else  {
            oglFormat = GL.GL_LUMINANCE;
          }
          break;
      }

      int type =
        (storedFormat == ImageComponentRetained.USHORT_GRAY) ? GL.GL_UNSIGNED_SHORT : GL.GL_UNSIGNED_BYTE;

      gl.glTexImage3D(GL.GL_TEXTURE_3D,
                      level, oglInternalFormat,
                      width, height, depth, boundaryWidth,
                      oglFormat, type, ByteBuffer.wrap(imageYup));
                        
      // No idea why we need the following call.
      gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
    }

    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset, int imgZOffset,
            int tilew, int tileh,
            int width, int height, int depth,
            byte[] image) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DSubImage()");

      GL gl = context(ctx).getGL();

      int oglFormat = 0;
      int oglInternalFormat = 0;
      int numBytes = 0;
      boolean pixelStore = false;

      switch (internalFormat) {
        case Texture.INTENSITY:
          oglInternalFormat = GL.GL_INTENSITY;
          break;
        case Texture.LUMINANCE:
          oglInternalFormat = GL.GL_LUMINANCE;
          break;
        case Texture.ALPHA:
          oglInternalFormat = GL.GL_ALPHA;
          break;
        case Texture.LUMINANCE_ALPHA:
          oglInternalFormat = GL.GL_LUMINANCE_ALPHA;
          break;
        case Texture.RGB:
          oglInternalFormat = GL.GL_RGB;
          break;
        case Texture.RGBA:
          oglInternalFormat = GL.GL_RGBA;
          break;
      }

      switch (storedFormat) {
        case ImageComponentRetained.BYTE_RGBA:
          // all RGB types are stored as RGBA
          oglFormat = GL.GL_RGBA;
          numBytes = 4;
          break;
        case ImageComponentRetained.BYTE_RGB:         
          oglFormat = GL.GL_RGB;
          numBytes = 3;
          break;
  
        case ImageComponentRetained.BYTE_ABGR:         
          if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
            oglFormat = GL.GL_ABGR_EXT;
            numBytes = 4;
          }
          break;
        case ImageComponentRetained.BYTE_BGR:         
          oglFormat = GL.GL_BGR;
          numBytes = 3;
          break;
        case ImageComponentRetained.BYTE_LA: 
          // all LA types are stored as LA8
          oglFormat = GL.GL_LUMINANCE_ALPHA;
          numBytes = 2;
          break;
        case ImageComponentRetained.BYTE_GRAY:
        case ImageComponentRetained.USHORT_GRAY:	    
          if (oglInternalFormat == GL.GL_ALPHA) {
            oglFormat = GL.GL_ALPHA;
          } else  {
            oglFormat = GL.GL_LUMINANCE;
          }
          numBytes = ((storedFormat == ImageComponentRetained.BYTE_GRAY) ? 1 : 2);
          break;
      }

      if (imgXOffset > 0 || (width < tilew)) {
        pixelStore = true;
        gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, tilew);
      }

      ByteBuffer buf = ByteBuffer.wrap(image);
      int offset = (tilew * tileh * imgZOffset +
                    tilew * imgYOffset + imgXOffset) * numBytes;
      buf.position(offset);
      int type =
        (storedFormat == ImageComponentRetained.USHORT_GRAY) ? GL.GL_UNSIGNED_SHORT : GL.GL_UNSIGNED_BYTE;
      gl.glTexSubImage3D(GL.GL_TEXTURE_3D,
                         level, xoffset, yoffset, zoffset,
                         width, height, depth,
                         oglFormat, type,
                         buf);
      if (pixelStore) {
        gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
      }
    }

    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DLodRange()");

      updateTextureLodRange(ctx, GL.GL_TEXTURE_3D,
                            baseLevel, maximumLevel,
                            minimumLod, maximumLod);
    }

    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DLodOffset()");
      
      updateTextureLodOffset(ctx, GL.GL_TEXTURE_3D,
                             lodOffsetS, lodOffsetT, lodOffsetR);
    }

    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DBoundary()");

      updateTextureBoundary(ctx, GL.GL_TEXTURE_2D,
                            boundaryModeS, boundaryModeT, boundaryModeR,
                            boundaryRed, boundaryGreen,
                            boundaryBlue, boundaryAlpha);
    }

    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DFilterModes()");

      updateTextureFilterModes(ctx, GL.GL_TEXTURE_3D,
                               minFilter, magFilter);
    }

    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DSharpenFunc()");

      updateTextureSharpenFunc(ctx, GL.GL_TEXTURE_3D,
                               numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DFilter4Func()");

      updateTextureFilter4Func(ctx, GL.GL_TEXTURE_3D,
                               numFilter4FuncPts, filter4FuncPts);
    }

    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexture3DAnisotropicFilter()");

      updateTextureAnisotropicFilter(ctx, GL.GL_TEXTURE_3D, degree);
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

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

    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageYup) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapImage()");

      updateTexture2DImage(ctx, _gl_textureCubeMapFace[face],
                           numLevels, level, internalFormat, storedFormat,
                           width, height, boundaryWidth, imageYup);
    }

    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int internalFormat,int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageYup) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapSubImage()");

      updateTexture2DSubImage(ctx, _gl_textureCubeMapFace[face],
                              level, xoffset, yoffset, internalFormat,
                              storedFormat, imgXOffset, imgYOffset, tilew,
                              width, height, imageYup);
    }

    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapLodRange()");

      updateTextureLodRange(ctx,
                            GL.GL_TEXTURE_CUBE_MAP,
                            baseLevel, maximumLevel,
                            minimumLod, maximumLod);
    }

    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetS, float lodOffsetT,
            float lodOffsetR) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapLodOffset()");

      updateTextureLodOffset(ctx,
                             GL.GL_TEXTURE_CUBE_MAP,
                             lodOffsetS, lodOffsetT, lodOffsetR);
    }

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

    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapFilterModes()");

      updateTextureFilterModes(ctx,
                               GL.GL_TEXTURE_CUBE_MAP,
                               minFilter, magFilter);
    }

    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapSharpenFunc()");

      updateTextureSharpenFunc(ctx,
                               GL.GL_TEXTURE_CUBE_MAP, 
                               numSharpenTextureFuncPts, sharpenTextureFuncPts);
    }

    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapFilter4Func()");

      updateTextureFilter4Func(ctx,
                               GL.GL_TEXTURE_CUBE_MAP,
                               numFilter4FuncPts, filter4FuncPts);
    }

    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTextureCubeMapAnisotropicFilter()");

      updateTextureAnisotropicFilter(ctx,
                                     GL.GL_TEXTURE_CUBE_MAP,
                                     degree);
    }


    // ---------------------------------------------------------------------

    //
    // DetailTextureImage methods
    //

    void bindDetailTexture(Context ctx, int objectId) {
      if (VERBOSE) System.err.println("JoglPipeline.bindDetailTexture()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_SGIS_detail_texture")) {
        gl.glBindTexture(GL.GL_DETAIL_TEXTURE_2D_SGIS, objectId);
      }
    }

    void updateDetailTextureImage(Context ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth, byte[] imageYup) {
      if (VERBOSE) System.err.println("JoglPipeline.updateDetailTextureImage()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_SGIS_detail_texture")) {
        updateTexture2DImage(ctx,
                             GL.GL_DETAIL_TEXTURE_2D_SGIS,
                             numLevels, level, internalFormat, storedFormat,
                             width, height, boundaryWidth, imageYup);
      }
    }


  //----------------------------------------------------------------------
  //
  // Helper routines for above texture methods
  //

  private void updateTexture2DImage(Context ctx,
                                    int target,
                                    int numLevels,
                                    int level,
                                    int internalFormat, 
                                    int format, 
                                    int width, 
                                    int height, 
                                    int boundaryWidth,
                                    byte[] imageYup) {
    GL gl = context(ctx).getGL();
    
    int oglFormat = 0, oglInternalFormat = 0;
    
    switch (internalFormat) {
      case Texture.INTENSITY:
        oglInternalFormat = GL.GL_INTENSITY;
        break;
      case Texture.LUMINANCE:
        oglInternalFormat = GL.GL_LUMINANCE;
        break;
      case Texture.ALPHA:
        oglInternalFormat = GL.GL_ALPHA;
        break;
      case Texture.LUMINANCE_ALPHA:
        oglInternalFormat = GL.GL_LUMINANCE_ALPHA;
        break;
      case Texture.RGB: 
        oglInternalFormat = GL.GL_RGB;
        break;
      case Texture.RGBA:
        oglInternalFormat = GL.GL_RGBA;
        break;
    }

    switch (format) {
      case ImageComponentRetained.BYTE_RGBA:         
        // all RGB types are stored as RGBA
        oglFormat = GL.GL_RGBA;
        break;
      case ImageComponentRetained.BYTE_RGB:         
        oglFormat = GL.GL_RGB;
        break;

      case ImageComponentRetained.BYTE_ABGR:         
        if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
          oglFormat = GL.GL_ABGR_EXT;
        }
        break;

      case ImageComponentRetained.BYTE_BGR:         
        oglFormat = GL.GL_BGR;
        break;

      case ImageComponentRetained.BYTE_LA: 
        // all LA types are stored as LA8
        oglFormat = GL.GL_LUMINANCE_ALPHA;
        break;
      case ImageComponentRetained.BYTE_GRAY:
      case ImageComponentRetained.USHORT_GRAY:
        if (oglInternalFormat == GL.GL_ALPHA) {
          oglFormat = GL.GL_ALPHA;
        } else  {
          oglFormat = GL.GL_LUMINANCE;
        }
        break;
    }
    // check if we are trying to draw NPOT on a system that doesn't support it
    if ((!gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two")) &&
        (!isPowerOfTwo(width) || !isPowerOfTwo(height))) {
      // disable texture by setting width and height to 0
      width = height = 0;
    }
    int type = GL.GL_UNSIGNED_BYTE;
    if (format == ImageComponentRetained.USHORT_GRAY) {
      type = GL.GL_UNSIGNED_SHORT;
    }
    Buffer buf = null;

    // FIXME: test against imageYup.length added for GenesisFX demo
    // which seems to want to initialize the texture object and then
    // update its sub-image; unclear whether this should be done
    // elsewhere (test is not in NativePipeline)
    if (imageYup != null && imageYup.length > 0) {
      buf = ByteBuffer.wrap(imageYup);
    }

    gl.glTexImage2D(target, level, oglInternalFormat, 
                    width, height, boundaryWidth,
                    oglFormat, type, buf);

    // No idea why we need following call.
    gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
  }
                                    

  private void updateTexture2DSubImage(Context ctx,
            int target,
            int level, int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData) {
    GL gl = context(ctx).getGL();

    int oglFormat = 0, oglInternalFormat=0;
    int numBytes = 0;
    boolean pixelStore = false;
      
    switch (internalFormat) {
      case Texture.INTENSITY:
        oglInternalFormat = GL.GL_INTENSITY;
        break;
      case Texture.LUMINANCE:
        oglInternalFormat = GL.GL_LUMINANCE;
        break;
      case Texture.ALPHA:
        oglInternalFormat = GL.GL_ALPHA;
        break;
      case Texture.LUMINANCE_ALPHA:
        oglInternalFormat = GL.GL_LUMINANCE_ALPHA;
        break;
      case Texture.RGB: 
        oglInternalFormat = GL.GL_RGB;
        break;
      case Texture.RGBA:
        oglInternalFormat = GL.GL_RGBA;
        break;
    }

    switch (storedFormat) {
      case ImageComponentRetained.BYTE_RGBA:         
        // all RGB types are stored as RGBA
        oglFormat = GL.GL_RGBA;
        numBytes = 4;
        break;
      case ImageComponentRetained.BYTE_RGB:         
        oglFormat = GL.GL_RGB;
        numBytes = 3;
        break;
    
      case ImageComponentRetained.BYTE_ABGR:         
        if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If its zero, should never come here!
          oglFormat = GL.GL_ABGR_EXT;
          numBytes = 4;
        }
        break;
    
      case ImageComponentRetained.BYTE_BGR:         
        oglFormat = GL.GL_BGR;
        numBytes = 3;
        break;
    
      case ImageComponentRetained.BYTE_LA: 
        // all LA types are stored as LA8
        oglFormat = GL.GL_LUMINANCE_ALPHA;
        numBytes = 2;
        break;
      case ImageComponentRetained.BYTE_GRAY:
        if (oglInternalFormat == GL.GL_ALPHA) {
          oglFormat = GL.GL_ALPHA;
        } else  {
          oglFormat = GL.GL_LUMINANCE;
        }
        numBytes = 1;
      case ImageComponentRetained.USHORT_GRAY:
        if (oglInternalFormat == GL.GL_ALPHA) {
          oglFormat = GL.GL_ALPHA;
        } else  {
          oglFormat = GL.GL_LUMINANCE;
        }
        numBytes = 2;
        break;
    }

    if (imgXOffset > 0 || (width < tilew)) {
      pixelStore = true;
      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, tilew);
    }
      
    // if NPOT textures are not supported, check if h=w=0, if so we have been 
    // disabled due to a NPOT texture being sent to a context that doesn't
    // support it: disable the glTexSubImage as well
    if (gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two")) {
      int[] tmp = new int[1];
      int texWidth, texHeight;
      gl.glGetTexLevelParameteriv(GL.GL_TEXTURE_2D, 0, GL.GL_TEXTURE_WIDTH, tmp, 0);
      texWidth = tmp[0];
      gl.glGetTexLevelParameteriv(GL.GL_TEXTURE_2D, 0, GL.GL_TEXTURE_HEIGHT, tmp, 0);
      texHeight = tmp[0];
      if ((texWidth == 0) && (texHeight == 0)) {
        // disable the sub-image by setting it's width and height to 0
        width = height = 0;
      }
    }

    if (storedFormat != ImageComponentRetained.USHORT_GRAY) {
      ByteBuffer buf = ByteBuffer.wrap(imageData);
      // offset by the imageOffset
      buf.position((tilew * imgYOffset + imgXOffset) * numBytes);
      gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, 
                         oglFormat, GL.GL_UNSIGNED_BYTE, buf);
    } else { // unsigned short
      ByteBuffer buf = ByteBuffer.wrap(imageData);
      buf.position((tilew * imgYOffset + imgXOffset) * numBytes);
      gl.glTexSubImage2D(target, level, xoffset, yoffset, width, height, 
                         oglFormat, GL.GL_UNSIGNED_SHORT, buf);
    }
    if (pixelStore) {
      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
    }
  }

  private static boolean isPowerOfTwo(int val) {
    return ((val & (val - 1)) == 0);
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
        gl.glTexParameteri(target, GL.GL_TEXTURE_MIN_FILTER, 
                           GL.GL_FILTER4_SGIS);
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
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_SHARPEN_SGIS);
        break;
      case Texture.LINEAR_SHARPEN_RGB:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_SHARPEN_COLOR_SGIS);
        break;
      case Texture.LINEAR_SHARPEN_ALPHA:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_SHARPEN_ALPHA_SGIS);
        break;
      case Texture2D.LINEAR_DETAIL:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_DETAIL_SGIS);
        break;
      case Texture2D.LINEAR_DETAIL_RGB:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_DETAIL_COLOR_SGIS);
        break;
      case Texture2D.LINEAR_DETAIL_ALPHA:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER,
                           GL.GL_LINEAR_DETAIL_ALPHA_SGIS);
        break;
      case Texture.FILTER4:
        gl.glTexParameteri(target, GL.GL_TEXTURE_MAG_FILTER, 
                           GL.GL_FILTER4_SGIS);
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
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        break;
      case Texture.CLAMP_TO_EDGE:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S, 
                           GL.GL_CLAMP_TO_EDGE);
        break;
      case Texture.CLAMP_TO_BOUNDARY:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_S, 
                           GL.GL_CLAMP_TO_BORDER);
        break;
    }

    switch (boundaryModeT) {
      case Texture.WRAP:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        break;
      case Texture.CLAMP:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
        break;
      case Texture.CLAMP_TO_EDGE:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, 
                           GL.GL_CLAMP_TO_EDGE);
        break;
      case Texture.CLAMP_TO_BOUNDARY:
        gl.glTexParameteri(target, GL.GL_TEXTURE_WRAP_T, 
                           GL.GL_CLAMP_TO_BORDER);
        break;
    }

    // applies to Texture3D only
    if (boundaryModeR != -1) {
      switch (boundaryModeR) {
        case Texture.WRAP:
          gl.glTexParameteri(target,
                             GL.GL_TEXTURE_WRAP_R, GL.GL_REPEAT);
          break;
  
        case Texture.CLAMP:
          gl.glTexParameteri(target,
                             GL.GL_TEXTURE_WRAP_R, GL.GL_CLAMP);
          break;
        case Texture.CLAMP_TO_EDGE:
          gl.glTexParameteri(target, 
                             GL.GL_TEXTURE_WRAP_R, 
                             GL.GL_CLAMP_TO_EDGE);
          break;
        case Texture.CLAMP_TO_BOUNDARY:
          gl.glTexParameteri(target, 
                             GL.GL_TEXTURE_WRAP_R,
                             GL.GL_CLAMP_TO_BORDER);
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
      gl.glTexParameterfv(target, GL.GL_TEXTURE_BORDER_COLOR, color, 0);
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
    GL gl = context(ctx).getGL();
    gl.glSharpenTexFuncSGIS(target, numPts, pts, 0);
  }

  private void updateTextureFilter4Func(Context ctx,
                                        int target,
                                        int numPts,
                                        float[] pts) {
    // checking of the availability of filter4 functionality
    // is already done in shared code
    GL gl = context(ctx).getGL();
    gl.glTexFilterFuncSGIS(target, GL.GL_FILTER4_SGIS,
                           numPts, pts, 0);
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

    // Method to return the AWT object
    long getAWT() {
      if (VERBOSE) System.err.println("JoglPipeline.getAWT()");

      // FIXME: probably completely unneeded in this implementation,
      // but should probably remove this dependence in the shared code
      return 0;
    }

    // Method to initialize the native J3D library
    boolean initializeJ3D(boolean disableXinerama) {
        // Dummy method in JOGL pipeline
        return true;
    }

    // Maximum lights supported by the native API
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

    // This is the native method for creating the underlying graphics context.
    Context createNewContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, Context shareCtx, boolean isSharedCtx,
            boolean offScreen,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {
      if (VERBOSE) System.err.println("JoglPipeline.createNewContext()");
      GLDrawable draw = null;
      GLCapabilitiesChooser indexChooser = null;
      JoglGraphicsConfiguration config = (JoglGraphicsConfiguration) cv.graphicsConfiguration;
      if (config.getChosenIndex() >= 0) {
        indexChooser = new IndexCapabilitiesChooser(config.getChosenIndex());
      }
      if (cv.drawable == null) {
        draw =
          GLDrawableFactory.getFactory().getGLDrawable(cv,
                                                       config.getGLCapabilities(),
                                                       indexChooser);
        cv.drawable = new JoglDrawable(draw);
      } else {
        draw = drawable(cv.drawable);
      }
        
      // FIXME: assuming that this only gets called after addNotify has been called
      draw.setRealized(true);
      GLContext context = draw.createContext(context(shareCtx));
      
      // Apparently we are supposed to make the context current at
      // this point and set up a bunch of properties
      if (context.makeCurrent() == GLContext.CONTEXT_NOT_CURRENT) {
        throw new RuntimeException("Unable to make new context current");
      }

      GL gl = context.getGL();
      JoglContext ctx = new JoglContext(context);

      if (!getPropertiesFromCurrentContext(ctx)) {
        throw new RuntimeException("Unable to fetch properties from current OpenGL context");
      }

      if(!isSharedCtx){
        // Set up fields in Canvas3D
        setupCanvasProperties(cv, ctx, gl, glslLibraryAvailable, cgLibraryAvailable);
      }
      
      // Enable rescale normal
      gl.glEnable(GL.GL_RESCALE_NORMAL);

      gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE);
      gl.glDepthFunc(GL.GL_LEQUAL);
      gl.glEnable(GL.GL_COLOR_MATERIAL);
      gl.glReadBuffer(GL.GL_FRONT);

      return ctx;
    }

    void createQueryContext(Canvas3D cv, long display, Drawable drawable,
            long fbConfig, boolean offScreen, int width, int height,
            boolean glslLibraryAvailable,
            boolean cgLibraryAvailable) {
      if (VERBOSE) System.err.println("JoglPipeline.createQueryContext()");

      // FIXME: for now, ignoring the "offscreen" flag -- unclear how
      // to create an offscreen buffer at this point -- very likely
      // need Canvas3D.offScreenBufferInfo promoted to an Object --
      // this logic will need to be revisited to make sure we capture
      // all of the functionality of the NativePipeline

      Frame f = new Frame();
      f.setUndecorated(true);
      f.setLayout(new BorderLayout());
      GLCapabilities caps = new GLCapabilities();
      ContextQuerier querier = new ContextQuerier(cv,
                                                  glslLibraryAvailable,
                                                  cgLibraryAvailable);
      // FIXME: should know what GraphicsDevice on which to create
      // this Canvas / Frame, and this should probably be known from
      // the incoming "display" parameter
      QueryCanvas canvas = new QueryCanvas(caps, querier, null);
      f.add(canvas, BorderLayout.CENTER);
      f.setSize(1, 1);
      f.setVisible(true);
      
      // Attempt to wait for the frame to become visible, but don't block the EDT
      if (!EventQueue.isDispatchThread()) {
        synchronized(querier) {
          if (!querier.done()) {
            try {
              querier.wait(WAIT_TIME);
            } catch (InterruptedException e) {
            }
          }
        }
      }

      disposeOnEDT(f);
    }

    // This is the native for creating offscreen buffer
    Drawable createOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, int width, int height) {
      if (DEBUG) System.err.println("JoglPipeline.createOffScreenBuffer()");
        // TODO: implement this
        return null;
    }

    void destroyOffScreenBuffer(Canvas3D cv, Context ctx, long display, long fbConfig, Drawable drawable) {
      if (DEBUG) System.err.println("JoglPipeline.destroyOffScreenBuffer()");
        // TODO: implement this
    }

    // This is the native for reading the image from the offscreen buffer
    void readOffScreenBuffer(Canvas3D cv, Context ctx, int format, int width, int height) {
      if (DEBUG) System.err.println("JoglPipeline.readOffScreenBuffer()");
        // TODO: implement this
    }

    // The native method for swapBuffers
    int swapBuffers(Canvas3D cv, Context ctx, long dpy, Drawable drawable) {
      if (VERBOSE) System.err.println("JoglPipeline.swapBuffers()");
      GLDrawable draw = drawable(drawable);
      draw.swapBuffers();
      return 0;
    }

    // notify D3D that Canvas is resize
    int resizeD3DCanvas(Canvas3D cv, Context ctx) {
      // Dummy method in JOGL pipeline
      return 0;
    }

    // notify D3D to toggle between FullScreen and window mode
    int toggleFullScreenMode(Canvas3D cv, Context ctx) {
      // Dummy method in JOGL pipeline
      return 0;
    }

    // native method for setting Material when no material is present
    void updateMaterialColor(Context ctx, float r, float g, float b, float a) {
      if (VERBOSE) System.err.println("JoglPipeline.updateMaterialColor()");

      GL gl = context(ctx).getGL();
      gl.glColor4f(r, g, b, a);
      gl.glDisable(GL.GL_LIGHTING);
    }

    void destroyContext(long display, Drawable drawable, Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.destroyContext()");
      GLDrawable draw     = drawable(drawable);
      GLContext  context  = context(ctx);
      if (GLContext.getCurrent() == context) {
        context.release();
      }
      context.destroy();
      // FIXME: assuming this is the right point at which to make this call
      draw.setRealized(false);
    }

    // This is the native method for doing accumulation.
    void accum(Context ctx, float value) {
      if (VERBOSE) System.err.println("JoglPipeline.accum()");

      GL gl = context(ctx).getGL();
      gl.glReadBuffer(GL.GL_BACK);
      gl.glAccum(GL.GL_ACCUM, value);
      gl.glReadBuffer(GL.GL_FRONT);
    }

    // This is the native method for doing accumulation return.
    void accumReturn(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.accumReturn()");

      GL gl = context(ctx).getGL();
      gl.glAccum(GL.GL_RETURN, 1.0f);
    }

    // This is the native method for clearing the accumulation buffer.
    void clearAccum(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.clearAccum()");

      GL gl = context(ctx).getGL();
      gl.glClear(GL.GL_ACCUM_BUFFER_BIT);
    }

    // This is the native method for getting the number of lights the underlying
    // native library can support.
    int getNumCtxLights(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.getNumCtxLights()");

      GL gl = context(ctx).getGL();
      int[] res = new int[1];
      gl.glGetIntegerv(GL.GL_MAX_LIGHTS, res, 0);
      return res[0];
    }

    // Native method for decal 1st child setup
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
    void decalNthChildSetup(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.decalNthChildSetup()");

      GL gl = context(ctx).getGL();
      gl.glDisable(GL.GL_DEPTH_TEST);
      gl.glStencilFunc(GL.GL_EQUAL, 0x1, 0x1);
      gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
    }

    // Native method for decal reset
    void decalReset(Context ctx, boolean depthBufferEnable) {
      if (VERBOSE) System.err.println("JoglPipeline.decalReset()");

      GL gl = context(ctx).getGL();
      gl.glDisable(GL.GL_STENCIL_TEST);
      if (depthBufferEnable)
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    // Native method for eye lighting
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
      if (VERBOSE) System.err.println("JoglPipeline.ctxUpdateEyeLightingEnable()");

      GL gl = context(ctx).getGL();

      if (localEyeLightingEnable) {
        gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
      } else {
        gl.glLightModeli(GL.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_FALSE);
      }
    }

    // The following three methods are used in multi-pass case

    // native method for setting blend color
    void setBlendColor(Context ctx, float red, float green,
            float blue, float alpha) {
      if (VERBOSE) System.err.println("JoglPipeline.setBlendColor()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_ARB_imaging")) {
        gl.glBlendColor(red, green, blue, alpha);
      }        
    }

    // native method for setting blend func
    void setBlendFunc(Context ctx, int srcBlendFunction, int dstBlendFunction) {
      if (VERBOSE) System.err.println("JoglPipeline.setBlendFunc()");

      GL gl = context(ctx).getGL();
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(blendFunctionTable[srcBlendFunction],
                     blendFunctionTable[dstBlendFunction]);
    }

    // native method for setting fog enable flag
    void setFogEnableFlag(Context ctx, boolean enable) {
      if (VERBOSE) System.err.println("JoglPipeline.setFogEnableFlag()");

      GL gl = context(ctx).getGL();

      if (enable)
        gl.glEnable(GL.GL_FOG);
      else
        gl.glDisable(GL.GL_FOG);
    }

    // Setup the full scene antialising in D3D and ogl when GL_ARB_multisamle supported
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

    void setGlobalAlpha(Context ctx, float alpha) {
      if (VERBOSE) System.err.println("JoglPipeline.setGlobalAlpha()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
	gl.glEnable(GL.GL_GLOBAL_ALPHA_SUN);
        gl.glGlobalAlphaFactorfSUN(alpha);
      }
    }

    // Native method to update separate specular color control
    void updateSeparateSpecularColorEnable(Context ctx, boolean enable) {
      if (VERBOSE) System.err.println("JoglPipeline.updateSeparateSpecularColorEnable()");

      GL gl = context(ctx).getGL();

      if (enable) {
        gl.glLightModeli(GL.GL_LIGHT_MODEL_COLOR_CONTROL, GL.GL_SEPARATE_SPECULAR_COLOR);
      } else {
        gl.glLightModeli(GL.GL_LIGHT_MODEL_COLOR_CONTROL, GL.GL_SINGLE_COLOR);
      }
    }

    // Initialization for D3D when scene begins and ends
    void beginScene(Context ctx) {
    }
    void endScene(Context ctx) {
    }

    // True under Solaris,
    // False under windows when display mode <= 8 bit
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
    void setLightEnables(Context ctx, long enableMask, int maxLights) {
      if (VERBOSE) System.err.println("JoglPipeline.setLightEnables()");

      GL gl = context(ctx).getGL();

      for (int i = 0; i < maxLights; i++) {
        if ((enableMask & (1 << i)) != 0) {
          gl.glEnable(GL.GL_LIGHT0 + i);
        } else {
          gl.glDisable(GL.GL_LIGHT0 + i);
        }
      }
    }

    // native method for setting scene ambient
    void setSceneAmbient(Context ctx, float red, float green, float blue) {
      if (VERBOSE) System.err.println("JoglPipeline.setSceneAmbient()");

      GL gl = context(ctx).getGL();

      float[] color = new float[4];
      color[0] = red;
      color[1] = green;
      color[2] = blue;
      color[3] = 1.0f;
      gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT, color, 0);
    }

    // native method for disabling fog
    void disableFog(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.disableFog()");

      GL gl = context(ctx).getGL();
      gl.glDisable(GL.GL_FOG);
    }

    // native method for disabling modelClip
    void disableModelClip(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.disableModelClip()");

      GL gl = context(ctx).getGL();

      gl.glDisable(GL.GL_CLIP_PLANE0);
      gl.glDisable(GL.GL_CLIP_PLANE1);
      gl.glDisable(GL.GL_CLIP_PLANE2);
      gl.glDisable(GL.GL_CLIP_PLANE3);
      gl.glDisable(GL.GL_CLIP_PLANE4);
      gl.glDisable(GL.GL_CLIP_PLANE5);
    }

    // native method for setting default RenderingAttributes
    void resetRenderingAttributes(Context ctx,
            boolean depthBufferWriteEnableOverride,
            boolean depthBufferEnableOverride) {
      if (VERBOSE) System.err.println("JoglPipeline.resetRenderingAttributes()");

      GL gl = context(ctx).getGL();

      if (!depthBufferWriteEnableOverride) {
        gl.glDepthMask(true);
      }
      if (!depthBufferEnableOverride) {
        gl.glEnable(GL.GL_DEPTH_TEST);
      }
      gl.glAlphaFunc(GL.GL_ALWAYS, 0.0f);
      gl.glDepthFunc(GL.GL_LEQUAL);
      gl.glEnable(GL.GL_COLOR_MATERIAL);
      gl.glDisable(GL.GL_COLOR_LOGIC_OP);
    }

    // native method for setting default texture
    void resetTextureNative(Context ctx, int texUnitIndex) {
      if (VERBOSE) System.err.println("JoglPipeline.resetTextureNative()");

      GL gl = context(ctx).getGL();
      if (texUnitIndex >= 0 &&
          gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
        gl.glClientActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
      }

      gl.glDisable(GL.GL_TEXTURE_1D);
      gl.glDisable(GL.GL_TEXTURE_2D);
      gl.glDisable(GL.GL_TEXTURE_3D);
      gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);
    }

    // native method for activating a particular texture unit
    void activeTextureUnit(Context ctx, int texUnitIndex) {
      if (VERBOSE) System.err.println("JoglPipeline.activeTextureUnit()");

      GL gl = context(ctx).getGL();
      if (gl.isExtensionAvailable("GL_VERSION_1_3")) {
        gl.glActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
        gl.glClientActiveTexture(texUnitIndex + GL.GL_TEXTURE0);
      }
    }

    // native method for setting default TexCoordGeneration
    void resetTexCoordGeneration(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.resetTexCoordGeneration()");

      GL gl = context(ctx).getGL();
      gl.glDisable(GL.GL_TEXTURE_GEN_S);
      gl.glDisable(GL.GL_TEXTURE_GEN_T);
      gl.glDisable(GL.GL_TEXTURE_GEN_R);
      gl.glDisable(GL.GL_TEXTURE_GEN_Q);
    }

    // native method for setting default TextureAttributes
    void resetTextureAttributes(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.resetTextureAttributes()");

      GL gl = context(ctx).getGL();

      float[] color = new float[4];

      gl.glPushAttrib(GL.GL_MATRIX_MODE);
      gl.glMatrixMode(GL.GL_TEXTURE);
      gl.glLoadIdentity();
      gl.glPopAttrib();
      gl.glTexEnvfv(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_COLOR, color, 0);
      gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);

      if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
        gl.glDisable(GL.GL_REGISTER_COMBINERS_NV);
      }

      if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
        gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
      }
    }

    // native method for setting default PolygonAttributes
    void resetPolygonAttributes(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.resetPolygonAttributes()");

      GL gl = context(ctx).getGL();

      gl.glCullFace(GL.GL_BACK);
      gl.glEnable(GL.GL_CULL_FACE);

      gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);

      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

      gl.glPolygonOffset(0.0f, 0.0f);
      gl.glDisable(GL.GL_POLYGON_OFFSET_POINT);
      gl.glDisable(GL.GL_POLYGON_OFFSET_LINE);
      gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
    }

    // native method for setting default LineAttributes
    void resetLineAttributes(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.resetLineAttributes()");

      GL gl = context(ctx).getGL();
      gl.glLineWidth(1.0f);
      gl.glDisable(GL.GL_LINE_STIPPLE);

      // XXXX: Polygon Mode check, blend enable
      gl.glDisable(GL.GL_LINE_SMOOTH);
    }

    // native method for setting default PointAttributes
    void resetPointAttributes(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.resetPointAttributes()");

      GL gl = context(ctx).getGL();
      gl.glPointSize(1.0f);

      // XXXX: Polygon Mode check, blend enable
      gl.glDisable(GL.GL_POINT_SMOOTH);
    }

    // native method for setting default TransparencyAttributes
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
      gl.glDisable(GL.GL_POLYGON_STIPPLE);
    }

    // native method for setting default ColoringAttributes
    void resetColoringAttributes(Context ctx,
            float r, float g,
            float b, float a,
            boolean enableLight) {
      if (VERBOSE) System.err.println("JoglPipeline.resetColoringAttributes()");

      GL gl = context(ctx).getGL();

      if (!enableLight) {
        gl.glColor4f(r, g, b, a);
      }
      gl.glShadeModel(GL.GL_SMOOTH);
    }

    // native method for updating the texture unit state map
    void updateTexUnitStateMap(Context ctx, int numActiveTexUnit,
            int[] texUnitStateMap) {
      if (VERBOSE) System.err.println("JoglPipeline.updateTexUnitStateMap()");

      // texture unit state map is explicitly handled in
      // execute; for display list, texture unit has to match
      // texture unit state.
    }

    /**
     *  This native method makes sure that the rendering for this canvas
     *  gets done now.
     */
    void syncRender(Context ctx, boolean wait) {
      if (VERBOSE) System.err.println("JoglPipeline.syncRender()");

      GL gl = context(ctx).getGL();

      if (wait)
        gl.glFinish();  
      else
        gl.glFlush();
    }

    // The native method that sets this ctx to be the current one
    boolean useCtx(Context ctx, long display, Drawable drawable) {
      if (VERBOSE) System.err.println("JoglPipeline.useCtx()");
      GLContext context = context(ctx);
      int res = context.makeCurrent();
      return (res != GLContext.CONTEXT_NOT_CURRENT);
    }

    // Optionally release the context. Returns true if the context was released.
    boolean releaseCtx(Context ctx, long dpy) {
      if (VERBOSE) System.err.println("JoglPipeline.releaseCtx()");
      GLContext context = context(ctx);
      context.release();
      return true;
    }

    void clear(Context ctx, float r, float g, float b, int winWidth, int winHeight,
            ImageComponent2DRetained image, int imageScaleMode, byte[] imageYdown) {
      if (VERBOSE) System.err.println("JoglPipeline.clear()");
      JoglContext jctx = (JoglContext) ctx;
      GLContext context = context(ctx);
      GL gl = context.getGL();
      if (image == null) {
        gl.glClearColor(r, g, b, jctx.getAlphaClearValue());
        gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
      } else {
        // Do a cool image blit
        
        int width = image.width;
        int height = image.height;

        // Temporarily disable fragment and most 3D operations
        // XXXX: the GL_TEXTURE_BIT may not be necessary
        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT); 
        disableAttribFor2D(gl);

        ByteBuffer pixels = ByteBuffer.wrap(imageYdown);

        // loaded identity modelview and projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION); 
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_MODELVIEW); 
        gl.glLoadIdentity();
        
        int gltype = 0;
        float rasterX = 0, rasterY = 0;
        switch (image.storedYdownFormat) {
          case ImageComponentRetained.BYTE_RGBA:
            gltype = GL.GL_RGBA;
            break;
          case ImageComponentRetained.BYTE_RGB:
            gltype = GL.GL_RGB;
            break;

          case ImageComponentRetained.BYTE_ABGR:
            if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If false, should never come here!
              gltype = GL.GL_ABGR_EXT;
            }
            break;

          case ImageComponentRetained.BYTE_BGR:
            gltype = GL.GL_BGR;
            break;

          case ImageComponentRetained.BYTE_LA:
            gltype = GL.GL_LUMINANCE_ALPHA;
            break;
          case ImageComponentRetained.BYTE_GRAY:
          case ImageComponentRetained.USHORT_GRAY:      
          default:
            throw new AssertionError("illegal format");
        }

        // start from upper left corner
        gl.glRasterPos3f(-1.0f, 1.0f, 0.0f);

        // setup the pixel zoom
        float xzoom = (float)winWidth  / width;
        float yzoom = (float)winHeight / height;
        switch (imageScaleMode) {
          case Background.SCALE_NONE:
            if (xzoom > 1.0f || yzoom > 1.0f) {
              // else don't need to clear the background with background color
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
            }
            gl.glPixelZoom(1.0f, -1.0f);
            gl.glDrawPixels(width, height, gltype, GL.GL_UNSIGNED_BYTE,
                            pixels);
            break;
          case Background.SCALE_FIT_MIN:
            if (xzoom != yzoom) {
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
            }
            float zoom = Math.min(xzoom, yzoom);
            gl.glPixelZoom(zoom, -zoom);
            gl.glDrawPixels(width, height, gltype, GL.GL_UNSIGNED_BYTE,
                            pixels);
            break;
          case Background.SCALE_FIT_MAX:
            zoom = xzoom > yzoom? xzoom:yzoom;
            gl.glPixelZoom(zoom, -zoom);
            gl.glDrawPixels(width, height, gltype, GL.GL_UNSIGNED_BYTE,
                            pixels);
            break;
          case Background.SCALE_FIT_ALL:
            gl.glPixelZoom(xzoom, -yzoom);
            gl.glDrawPixels(width, height, gltype, GL.GL_UNSIGNED_BYTE,
                            pixels);
            break;
          case Background.SCALE_REPEAT:
            gl.glPixelZoom(1.0f, -1.0f);
            // get those raster positions
            int repeatX = winWidth / width;
            if (repeatX * width < winWidth)
              repeatX++;
            int repeatY = winHeight / height;
            if (repeatY * height < winHeight)
              repeatY++;
            for (int i = 0; i < repeatX; i++)
              for (int j = 0; j < repeatY; j++) {
                rasterX = -1.0f + (float)width/winWidth * i * 2;
                rasterY =  1.0f - (float)height/winHeight * j * 2;
                gl.glRasterPos3f(rasterX, rasterY, 0.0f);
                gl.glDrawPixels(width, height, gltype, GL.GL_UNSIGNED_BYTE,
                                pixels);
              }
            break;

          case Background.SCALE_NONE_CENTER:
            if (xzoom > 1.0f || yzoom > 1.0f){
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
            }
            int row_length = 0, skip_pixels = 0, skip_rows = 0;
            int subwidth = 0, subheight = 0;
            if (xzoom >= 1.0f) {
              rasterX = -(float)width/winWidth;
              subwidth = width; 
            } else {
              rasterX = -1.0f;
              row_length = width;
              gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, row_length);
              skip_pixels = (width-winWidth)/2;
              gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, skip_pixels); 
              subwidth = winWidth;
            }
            if (yzoom >= 1.0f){
              rasterY = (float)height/winHeight;
              subheight = height; 
            } else {
              rasterY = 1.0f;
              skip_rows = (height-winHeight)/2;
              gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, skip_rows);
              subheight = winHeight; 
            } 
            gl.glRasterPos3f(rasterX, rasterY, 0.0f);
            gl.glPixelZoom(1.0f, -1.0f);
            gl.glDrawPixels(subwidth, subheight, gltype, GL.GL_UNSIGNED_BYTE,
                            pixels);
            gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
            gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, 0);
            gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, 0);
            break;
        }

        // Restore attributes
        gl.glPopAttrib();
      }

      // Java 3D always clears the Z-buffer
      gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT);
      gl.glDepthMask(true);
      gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
      gl.glPopAttrib();
    }

    void textureclear(Context ctx, int maxX, int maxY,
            float r, float g, float b,
            int winWidth, int winHeight,
            int objectId, int scalemode,
            ImageComponent2DRetained image,
            boolean update) {
      if (VERBOSE) System.err.println("JoglPipeline.textureclear()");

      JoglContext jctx = (JoglContext) ctx;
      GLContext context = context(ctx);
      GL gl = context.getGL();
      if (image == null) {
        gl.glClearColor(r, g, b, jctx.getAlphaClearValue());
        gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
      } else {
        // Do a cool image blit

        int width = image.width;
        int height = image.height;

        // Temporarily disable fragment and most 3D operations
        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT | GL.GL_POLYGON_BIT);
        disableAttribFor2D(gl);

        resetTexCoordGeneration(ctx);

        gl.glEnable(GL.GL_TEXTURE_2D);

        // reset the polygon mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

        gl.glDepthMask(false);
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);

        // set up texture parameter
        if (update) {
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
          gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        }

        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);

        int gltype = 0;
        if (update) {
          switch (image.storedYupFormat) {
            case ImageComponentRetained.BYTE_RGBA:
              gltype = GL.GL_RGBA;
              break; 
            case ImageComponentRetained.BYTE_RGB:
              gltype = GL.GL_RGB;
              break;
            case ImageComponentRetained.BYTE_ABGR:
              if (gl.isExtensionAvailable("GL_EXT_abgr")) { // If false, should never come here!
                gltype = GL.GL_ABGR_EXT;
              }
              break;
            case ImageComponentRetained.BYTE_BGR:
              gltype = GL.GL_BGR;
              break;
            case ImageComponentRetained.BYTE_LA:
              gltype = GL.GL_LUMINANCE_ALPHA;
              break;  

            case ImageComponentRetained.BYTE_GRAY:  
            case ImageComponentRetained.USHORT_GRAY:
            default:
              throw new AssertionError("illegal format");
          } 
  
          // texture map here!
          gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, gltype, width,
                          height, 0, gltype, GL.GL_UNSIGNED_BYTE,
                          ByteBuffer.wrap(image.imageYup));
        }

        // load identity modelview and projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION);  
        gl.glLoadIdentity();
        gl.glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);  
        gl.glLoadIdentity(); 
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        float xzoom = (float)winWidth  / maxX;
        float yzoom = (float)winHeight / maxY;
        float zoom = 0;
        float texMinU = 0, texMinV = 0, texMaxU = 0, texMaxV = 0, adjustV = 0; 
        float mapMinX = 0, mapMinY = 0, mapMaxX = 0, mapMaxY = 0; 
        float halfWidth = 0, halfHeight = 0; 
        int i = 0, j = 0;
        switch (scalemode) {
          case Background.SCALE_NONE:
            if (xzoom > 1.0f || yzoom > 1.0f) {
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
            }
            texMinU = 0.0f; 
            texMinV = 0.0f; 
            texMaxU = 1.0f; 
            texMaxV = 1.0f;
            halfWidth = (float)winWidth/2.0f;
            halfHeight = (float)winHeight/2.0f;
            mapMinX = (float) ((0 - halfWidth)/halfWidth);
            mapMinY = (float) ((0 - halfHeight)/halfHeight);
            mapMaxX = (float) ((maxX - halfWidth)/halfWidth);
            mapMaxY = (float) ((maxY - halfHeight)/halfHeight);
            adjustV = ((float)winHeight - (float)maxY)/halfHeight;
            mapMinY += adjustV;
            mapMaxY += adjustV;
            break;
          case Background.SCALE_FIT_MIN:
            if (xzoom != yzoom) {
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT);
            }

            zoom = Math.min(xzoom, yzoom);
            texMinU = 0.0f;
            texMinV = 0.0f;
            texMaxU = 1.0f;
            texMaxV = 1.0f;
            mapMinX = -1.0f;
            mapMaxY = 1.0f;
            if (xzoom < yzoom) {
              mapMaxX = 1.0f;
              mapMinY = -1.0f + 2.0f * ( 1.0f - zoom * (float)maxY/(float) winHeight );
            } else {
              mapMaxX = -1.0f + zoom * (float)maxX/winWidth * 2;
              mapMinY = -1.0f;
            }
            break;
          case Background.SCALE_FIT_MAX:
            zoom = Math.max(xzoom, yzoom);
            mapMinX = -1.0f;
            mapMinY = -1.0f;
            mapMaxX = 1.0f;
            mapMaxY = 1.0f;
            if (xzoom < yzoom) {
              texMinU = 0.0f;
              texMinV = 0.0f;
              texMaxU = (float)winWidth/maxX/zoom;
              texMaxV = 1.0f;
            } else {
              texMinU = 0.0f;
              texMinV = 1.0f - (float)winHeight/maxY/zoom;
              texMaxU = 1.0f;
              texMaxV = 1.0f;
            }
            break;
          case Background.SCALE_FIT_ALL:
            texMinU = 0.0f;
            texMinV = 0.0f;
            texMaxU = 1.0f;
            texMaxV = 1.0f;
            mapMinX = -1.0f;
            mapMinY = -1.0f;
            mapMaxX = 1.0f;
            mapMaxY = 1.0f;
            break;
          case Background.SCALE_REPEAT:
            i = winWidth/width;
            j = winHeight/height;
            texMinU = 0.0f;
            texMinV = (float)(j + 1) - yzoom;
            texMaxU = xzoom;
            texMaxV = (float)(j + 1);
            mapMinX = -1.0f;
            mapMinY = -1.0f;
            mapMaxX = 1.0f; 
            mapMaxY = 1.0f;      
            break;
          case Background.SCALE_NONE_CENTER:
            if (xzoom > 1.0f || yzoom > 1.0f) {
              gl.glClearColor((float)r, (float)g, (float)b, jctx.getAlphaClearValue());
              gl.glClear(GL.GL_COLOR_BUFFER_BIT); 
            }
            if(xzoom >= 1.0f){
              texMinU = 0.0f;
              texMaxU = 1.0f;
              mapMinX = -(float)maxX/winWidth;
              mapMaxX = (float)maxX/winWidth;
            } else {
              texMinU = 0.5f - (float)winWidth/maxX/2;
              texMaxU = 0.5f + (float)winWidth/maxX/2;
              mapMinX = -1.0f;
              mapMaxX = 1.0f;
            }
            if (yzoom >= 1.0f) {
              texMinV = 0.0f;
              texMaxV = 1.0f;
              mapMinY = -(float)maxY/winHeight;
              mapMaxY = (float)maxY/winHeight;
            } else {
              texMinV = 0.5f - (float)winHeight/maxY/2; 
              texMaxV = 0.5f + (float)winHeight/maxY/2;
              mapMinY = -1.0f;
              mapMaxY = 1.0f;  
            }
            break;
        }

        gl.glBegin(GL.GL_QUADS);
        gl.glTexCoord2f(texMinU, texMinV); gl.glVertex2f(mapMinX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMinV); gl.glVertex2f(mapMaxX,mapMinY);
        gl.glTexCoord2f(texMaxU, texMaxV); gl.glVertex2f(mapMaxX,mapMaxY);
        gl.glTexCoord2f(texMinU, texMaxV); gl.glVertex2f(mapMinX,mapMaxY);
        gl.glEnd();

        // Restore texture Matrix transform
        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_MODELVIEW);
        // Restore attributes
        gl.glPopAttrib();
      }

      // Java 3D always clears the Z-buffer
      gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT);
      gl.glDepthMask(true);
      gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
      gl.glPopAttrib();
    }


    // The native method for setting the ModelView matrix.
    void setModelViewMatrix(Context ctx, double[] viewMatrix, double[] modelMatrix) {
      if (VERBOSE) System.err.println("JoglPipeline.setModelViewMatrix()");
      GLContext context = context(ctx);
      GL gl = context.getGL();
      
      gl.glMatrixMode(GL.GL_MODELVIEW);

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
    void setProjectionMatrix(Context ctx, double[] projMatrix) {
      if (VERBOSE) System.err.println("JoglPipeline.setProjectionMatrix()");
      GLContext context = context(ctx);
      GL gl = context.getGL();
      
      gl.glMatrixMode(GL.GL_PROJECTION);

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

    // The native method for setting the Viewport.
    void setViewport(Context ctx, int x, int y, int width, int height) {
      if (VERBOSE) System.err.println("JoglPipeline.setViewport()");
      GL gl = context(ctx).getGL();
      gl.glViewport(x, y, width, height);
    }

    // used for display Lists
    void newDisplayList(Context ctx, int displayListId) {
      if (VERBOSE) System.err.println("JoglPipeline.newDisplayList()");
      if (displayListId <= 0) {
        throw new RuntimeException("JAVA 3D ERROR : glNewList(" + displayListId + ") -- IGNORED");
      }

      GL gl = context(ctx).getGL();
      gl.glNewList(displayListId, GL.GL_COMPILE);
    }

    void endDisplayList(Context ctx) {
      if (VERBOSE) System.err.println("JoglPipeline.endDisplayList()");
      GL gl = context(ctx).getGL();
      gl.glEndList();
    }

    int numInvalidLists = 0;
    void callDisplayList(Context ctx, int id, boolean isNonUniformScale) {
      if (VERBOSE) System.err.println("JoglPipeline.callDisplayList()");
      if (id <= 0) {
        if (numInvalidLists < 3) {
          ++numInvalidLists;
          throw new RuntimeException("JAVA 3D ERROR : glCallList(" + id + ") -- IGNORED");
        } else if (numInvalidLists == 3) {
          ++numInvalidLists;
          throw new RuntimeException("JAVA 3D : further glCallList error messages discarded");
        }
        return;
      }

      GL gl = context(ctx).getGL();
      // Set normalization if non-uniform scale
      if (isNonUniformScale) {
        gl.glEnable(GL.GL_NORMALIZE);
      } 
    
      gl.glCallList(id);

      // Turn normalization back off
      if (isNonUniformScale) {
        gl.glDisable(GL.GL_NORMALIZE);
      } 
    }

    void freeDisplayList(Context ctx, int id) {
      if (VERBOSE) System.err.println("JoglPipeline.freeDisplayList()");
      if (id <= 0) {
        throw new RuntimeException("JAVA 3D ERROR : glDeleteLists(" + id + ",1) -- IGNORED");
      }

      GL gl = context(ctx).getGL();
      gl.glDeleteLists(id, 1);
    }
    void freeTexture(Context ctx, int id) {
      if (VERBOSE) System.err.println("JoglPipeline.freeTexture()");

      GL gl = context(ctx).getGL();
      
      if (id > 0) {
        int[] tmp = new int[1];
        tmp[0] = id;
        gl.glDeleteTextures(1, tmp, 0);
      } else {
        throw new RuntimeException("tried to delete tex with texid <= 0");
      }
    }

    void composite(Context ctx, int px, int py,
            int minX, int minY, int maxX, int maxY,
            int rasWidth,  byte[] imageYdown,
            int winWidth, int winHeight) {
      if (VERBOSE) System.err.println("JoglPipeline.composite()");

      GL gl = context(ctx).getGL();

      // Temporarily disable fragment and most 3D operations
      // XXXX: the GL_TEXTURE_BIT may not be necessary here
      gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT | GL.GL_DEPTH_BUFFER_BIT);
      disableAttribFor2D(gl);

      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

      // load identity modelview and projection matrix
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();
      gl.glOrtho(0.0, winWidth, 0.0, winHeight, -1.0, 1.0);
      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();

      // start from upper left corner
      gl.glRasterPos2i(px + minX, winHeight-(py + minY));
      
      gl.glPixelZoom(1.0f, -1.0f);
      int glType;
      // if abgr_ext is supported then the data will be in that format
      if (gl.isExtensionAvailable("GL_EXT_abgr")) {
        glType = GL.GL_ABGR_EXT;
      } else {
        glType = GL.GL_RGBA;
      }

      // set the actual width of data which is the width of the canvas
      // because what needs to be drawn may be smaller than the canvas
      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, rasWidth);

      // we only need to skip pixels if width of the area to draw is smaller
      // than the width of the raster

      // skip this many rows in the data because the size of what
      // needs to be drawn may be smaller than the canvas
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, minY);

      // skip this many pixels in the data before drawing because
      // the size of what needs to be drawn may be smaller than the
      // canvas
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, minX);

      gl.glDrawPixels(maxX - minX, maxY - minY,
                      glType, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(imageYdown));

      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, 0);

      gl.glMatrixMode(GL.GL_PROJECTION);

      gl.glLoadIdentity();

      // Java 3D always clears the Z-buffer
      gl.glDepthMask(true);
      gl.glClear(GL.GL_DEPTH_BUFFER_BIT);

      gl.glPopAttrib();
    }

    void texturemapping(Context ctx,
            int px, int py,
            int minX, int minY, int maxX, int maxY,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] imageYdown,
            int winWidth, int winHeight) {
      if (VERBOSE) System.err.println("JoglPipeline.texturemapping()");

      GL gl = context(ctx).getGL();

      int glType = GL.GL_RGBA;

      // Temporarily disable fragment and most 3D operations
      gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_POLYGON_BIT);
      disableAttribFor2D(gl);

      // Reset the polygon mode
      gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

      gl.glDepthMask(false);
      gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
      gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);
      // set up texture parameter
      gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
      gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
      gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
      gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);

      gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
      gl.glEnable(GL.GL_BLEND);
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

      gl.glEnable(GL.GL_TEXTURE_2D);

      // loaded identity modelview and projection matrix
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();

      gl.glOrtho(0.0, winWidth, 0.0, winHeight, 0.0, 0.0);

      gl.glMatrixMode(GL.GL_MODELVIEW);
      gl.glLoadIdentity();

      if (gl.isExtensionAvailable("GL_EXT_abgr")) {
	glType = GL.GL_ABGR_EXT;
      } else { 
        switch (format) {
          case ImageComponentRetained.BYTE_RGBA:
            glType = GL.GL_RGBA;
            break;
          case ImageComponentRetained.BYTE_RGB:
            glType = GL.GL_RGB;
            break;
	}
      }
      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, rasWidth);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, minX);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, minY);
      gl.glTexSubImage2D(GL.GL_TEXTURE_2D, 0, minX, minY,
                         maxX - minX, maxY - minY,
                         glType, GL.GL_UNSIGNED_BYTE,
                         ByteBuffer.wrap(imageYdown));
      gl.glPixelStorei(GL.GL_UNPACK_ROW_LENGTH, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_PIXELS, 0);
      gl.glPixelStorei(GL.GL_UNPACK_SKIP_ROWS, 0);

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

      gl.glBegin(GL.GL_QUADS);

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

    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
      if (VERBOSE) System.err.println("JoglPipeline.initTexturemapping()");

      GL gl = context(ctx).getGL();

      int glType = (gl.isExtensionAvailable("GL_EXT_abgr") ? GL.GL_ABGR_EXT : GL.GL_RGBA);
      
      gl.glBindTexture(GL.GL_TEXTURE_2D, objectId);

      gl.glTexImage2D(GL.GL_PROXY_TEXTURE_2D, 0, GL.GL_RGBA, texWidth,
                      texHeight, 0, glType, GL.GL_UNSIGNED_BYTE, null);

      int[] width = new int[1];
      gl.glGetTexLevelParameteriv(GL.GL_PROXY_TEXTURE_2D, 0,
                                  GL.GL_TEXTURE_WIDTH, width, 0);

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
    void setRenderMode(Context ctx, int mode, boolean doubleBuffer) {
      if (VERBOSE) System.err.println("JoglPipeline.setRenderMode()");

      GL gl = context(ctx).getGL();
      int drawBuf = 0;
      if (doubleBuffer) {
        drawBuf = GL.GL_BACK;
        switch (mode) {
          case Canvas3D.FIELD_LEFT:
            drawBuf = GL.GL_BACK_LEFT;
            break;
          case Canvas3D.FIELD_RIGHT:
            drawBuf = GL.GL_BACK_RIGHT;
            break;
          case Canvas3D.FIELD_ALL:
            drawBuf = GL.GL_BACK;
            break;
        }
      } else {
        drawBuf = GL.GL_FRONT;
        switch (mode) {
          case Canvas3D.FIELD_LEFT:
            drawBuf = GL.GL_FRONT_LEFT;
            break;
          case Canvas3D.FIELD_RIGHT:
            drawBuf = GL.GL_FRONT_RIGHT;
            break;
          case Canvas3D.FIELD_ALL:
            drawBuf = GL.GL_FRONT;
            break;
        }
      }

      gl.glDrawBuffer(drawBuf);
    }

    // Set glDepthMask.
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

  private boolean getPropertiesFromCurrentContext(JoglContext ctx) {
    GL gl = GLU.getCurrentGL();
    // FIXME: this is a heavily abridged set of the stuff in Canvas3D.c;
    // probably need to pull much more in
    int[] tmp = new int[1];
    gl.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tmp, 0);
    ctx.setMaxTexCoordSets(tmp[0]);
    if (VirtualUniverse.mc.transparentOffScreen) {
      ctx.setAlphaClearValue(0.0f);
    } else {
      ctx.setAlphaClearValue(1.0f);
    }
    if (gl.isExtensionAvailable("GL_ARB_vertex_shader")) {
      gl.glGetIntegerv(GL.GL_MAX_TEXTURE_COORDS_ARB, tmp, 0);
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

  private int getTextureColorTableSize(GL gl) {
    if (!gl.isExtensionAvailable("GL_ARB_imaging")) {
      return 0;
    }
    
    gl.glColorTable(GL.GL_PROXY_TEXTURE_COLOR_TABLE_SGI, GL.GL_RGBA, 256, GL.GL_RGB,
                    GL.GL_INT, null);
    int[] tmp = new int[1];
    gl.glGetColorTableParameteriv(GL.GL_PROXY_TEXTURE_COLOR_TABLE_SGI,
                                  GL.GL_COLOR_TABLE_WIDTH, tmp, 0);
    return tmp[0];
  }


  private void checkTextureExtensions(Canvas3D cv,
                                      JoglContext ctx,
                                      GL gl,
                                      boolean gl13) {
    if (gl13) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_MULTI_TEXTURE;
      cv.multiTexAccelerated = true;
      int[] tmp = new int[1];
      gl.glGetIntegerv(GL.GL_MAX_TEXTURE_UNITS, tmp, 0);
      cv.maxTextureUnits = tmp[0];
      cv.maxTexCoordSets = cv.maxTextureUnits;
      if (gl.isExtensionAvailable("GL_ARB_vertex_shader")) {
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_COORDS_ARB, tmp, 0);
        cv.maxTexCoordSets = tmp[0];
      }
    }

    if (gl.isExtensionAvailable("GL_SGI_texture_color_table") ||
        gl.isExtensionAvailable("GL_ARB_imaging")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COLOR_TABLE;

      // get texture color table size
      // need to check later
      cv.textureColorTableSize = getTextureColorTableSize(gl);
      if (cv.textureColorTableSize <= 0) {
        cv.textureExtendedFeatures &= ~Canvas3D.TEXTURE_COLOR_TABLE;
      }
      if (cv.textureColorTableSize > 256) {
        cv.textureColorTableSize = 256;
      }
    }

    if (gl.isExtensionAvailable("GL_ARB_texture_env_combine")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE;
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE_SUBTRACT;
    } else if (gl.isExtensionAvailable("GL_EXT_texture_env_combine")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE;
    }

    if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_REGISTER_COMBINERS;
    }

    if (gl.isExtensionAvailable("GL_ARB_texture_env_dot3") ||
        gl.isExtensionAvailable("GL_EXT_texture_env_dot3")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_COMBINE_DOT3;
    }

    if (gl13) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_CUBE_MAP;
    }

    if (gl.isExtensionAvailable("GL_SGIS_sharpen_texture")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_SHARPEN;
    }

    if (gl.isExtensionAvailable("GL_SGIS_detail_texture")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_DETAIL;
    }

    if (gl.isExtensionAvailable("GL_SGIS_texture_filter4")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_FILTER4;
    }

    if (gl.isExtensionAvailable("GL_EXT_texture_filter_anisotropic")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_ANISOTROPIC_FILTER;
      float[] tmp = new float[1];
      gl.glGetFloatv(GL. GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, tmp, 0);
      cv.anisotropicDegreeMax = tmp[0];
    }

    if (gl.isExtensionAvailable("GL_SGIX_texture_lod_bias")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_LOD_OFFSET;
    }

    if (gl.isExtensionAvailable("GL_ARB_texture_non_power_of_two")) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_NON_POWER_OF_TWO;
    }
  }


  private void checkGLSLShaderExtensions(Canvas3D cv,
                                         JoglContext ctx,
                                         GL gl,
                                         boolean glslLibraryAvailable) {
    if (glslLibraryAvailable &&
        gl.isExtensionAvailable("GL_ARB_shader_objects") &&
        gl.isExtensionAvailable("GL_ARB_shading_language_100")) {
      // Initialize shader vertex attribute function pointers
      ctx.initGLSLVertexAttributeImpl();

      // FIXME: this isn't complete and would need to set up the
      // JoglContext for dispatch of various routines such as those
      // related to vertex attributes
      int[] tmp = new int[1];
      gl.glGetIntegerv(GL. GL_MAX_TEXTURE_IMAGE_UNITS_ARB, tmp, 0);
      cv.maxTextureImageUnits = tmp[0];
      gl.glGetIntegerv(GL. GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS_ARB, tmp, 0);
      cv.maxVertexTextureImageUnits = tmp[0];
      gl.glGetIntegerv(GL. GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS_ARB, tmp, 0);
      cv.maxCombinedTextureImageUnits = tmp[0];
      int vertexAttrOffset = VirtualUniverse.mc.glslVertexAttrOffset;
      ctx.setGLSLVertexAttrOffset(vertexAttrOffset);
      gl.glGetIntegerv(GL. GL_MAX_VERTEX_ATTRIBS_ARB, tmp, 0);
      cv.maxVertexAttrs = tmp[0];
      // decr count to allow for reserved vertex attrs
      cv.maxVertexAttrs -= vertexAttrOffset;
      if (cv.maxVertexAttrs < 0) {
        cv.maxVertexAttrs = 0;
      }
      cv.shadingLanguageGLSL = true;
    }
  }

  private void createCgContext(JoglContext ctx) {
    CGcontext cgContext = CgGL.cgCreateContext();

    int err = CgGL.cgGetError();
    if (err != 0) {
      String detail = CgGL.cgGetErrorString(err);
      throw new RuntimeException("Fatal error in creating Cg context: \"" +
                                 detail + "\"");
    }

    if (cgContext == null) {
      throw new RuntimeException("Invalid null Cg context");
    }

    ctx.setCgContext(cgContext);

    // Use GL_ARB_vertex_program extension if supported by video card
    if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_ARBVP1)) {
      ctx.setCgVertexProfile(CgGL.CG_PROFILE_ARBVP1);
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_VP20)) {
      ctx.setCgVertexProfile(CgGL.CG_PROFILE_VP20);
    } else {
      throw new RuntimeException("JAVA 3D ERROR : No CG vertex program profile is supported");
    }

    // Use GL_ARB_fragment_program extension if supported by video card
    if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_ARBFP1)) {
      ctx.setCgFragmentProfile(CgGL.CG_PROFILE_ARBFP1);
    } else if (CgGL.cgGLIsProfileSupported(CgGL.CG_PROFILE_FP20)) {
      ctx.setCgFragmentProfile(CgGL.CG_PROFILE_FP20);
    } else {
      throw new RuntimeException("JAVA 3D ERROR : No CG fragment program profile is supported");
    }
  }

  private void checkCgShaderExtensions(Canvas3D cv,
                                       JoglContext ctx,
                                       GL gl,
                                       boolean cgLibraryAvailable) {
    if (cgLibraryAvailable) {
      createCgContext(ctx);
      cv.shadingLanguageCg = true;
      // TODO: Query Cg texture sampler limits
      cv.maxTextureImageUnits = cv.maxTextureUnits;
      cv.maxVertexTextureImageUnits = 0;
      cv.maxCombinedTextureImageUnits = cv.maxTextureUnits;
      // TODO: Query max vertex attrs
      cv.maxVertexAttrs = 7;
      // Initialize shader vertex attribute function pointers
      ctx.initCgVertexAttributeImpl();
    }
  }

  private void setupCanvasProperties(Canvas3D cv,
                                     JoglContext ctx,
                                     GL gl,
                                     boolean glslLibraryAvailable,
                                     boolean cgLibraryAvailable) {
    // Note: this includes relevant portions from both the
    // NativePipeline's getPropertiesFromCurrentContext and setupCanvasProperties

    // Reset all fields
    cv.multiTexAccelerated = false;
    cv.maxTextureUnits = 0;
    cv.maxTexCoordSets = 0;
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
    cv.shadingLanguageCg = false;

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

    if (major < 1 ||
        (major == 1 && minor < 2)) {
      throw new IllegalStateException("Java 3D ERROR : OpenGL 1.2 or better is required (GL_VERSION=" +
                                      major + "." + minor + ")");
    }

    boolean gl20 = false;
    boolean gl13 = false;
    if (major > 1) {
      // OpenGL 2.x -- set flags for 1.3 and 2.0 or greater
      gl20 = true;
      gl13 = true;
    } else {
      if (minor == 2) {
        System.err.println("*********************************************************");
        System.err.println("*** JAVA 3D: WARNING OpenGL 1.2 is no longer supported.");
        System.err.println("*** Will attempt to run with reduced functionality.");
        System.err.println("*********************************************************");
      } else {
        gl13 = true;
      }
    }

    // Set up properties for OpenGL 1.3
    cv.textureExtendedFeatures |= Canvas3D.TEXTURE_3D;

    // Note that we don't query for GL_ARB_imaging here

    cv.textureExtendedFeatures |= Canvas3D.TEXTURE_LOD_RANGE;

    // look for OpenGL 2.0 features
    if (gl20) {
      cv.textureExtendedFeatures |= Canvas3D.TEXTURE_NON_POWER_OF_TWO;
    }

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
    }

    if ((cv.extensionsSupported & Canvas3D.MULTISAMPLE) != 0 &&
        !VirtualUniverse.mc.implicitAntialiasing) {
      gl.glDisable(GL.GL_MULTISAMPLE);
    }

    // Check texture extensions
    checkTextureExtensions(cv, ctx, gl, gl13);

    // Check shader extensions
    if (gl13) {
      checkGLSLShaderExtensions(cv, ctx, gl, glslLibraryAvailable);
      checkCgShaderExtensions(cv, ctx, gl, cgLibraryAvailable);
    } else {
      // Force shaders to be disabled, since no multitexture support
      checkGLSLShaderExtensions(cv, ctx, gl, false);
      checkCgShaderExtensions(cv, ctx, gl, false);
    }

    // Setup GL_SUN_gloabl_alpha
    if (gl.isExtensionAvailable("GL_SUN_gloabl_alpha")) {
      cv.extensionsSupported |= Canvas3D.SUN_GLOBAL_ALPHA;
    }      

    cv.textureBoundaryWidthMax = 1;
    {
      int[] tmp = new int[1];
      gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE, tmp, 0);
      cv.textureWidthMax = tmp[0];
      cv.textureHeightMax = tmp[0];

      tmp[0] = -1;
      gl.glGetIntegerv(GL.GL_MAX_3D_TEXTURE_SIZE, tmp, 0);
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
  private void
    disableAttribFor2D(GL gl)
  {
    gl.glDisable(GL.GL_ALPHA_TEST);
    gl.glDisable(GL.GL_BLEND);
    gl.glDisable(GL.GL_COLOR_LOGIC_OP);
    gl.glDisable(GL.GL_COLOR_MATERIAL);
    gl.glDisable(GL.GL_CULL_FACE);
    gl.glDisable(GL.GL_DEPTH_TEST);
    gl.glDisable(GL.GL_FOG);
    gl.glDisable(GL.GL_LIGHTING);
    gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
    gl.glDisable(GL.GL_POLYGON_STIPPLE);
    gl.glDisable(GL.GL_STENCIL_TEST);
    gl.glDisable(GL.GL_TEXTURE_2D);
    gl.glDisable(GL.GL_TEXTURE_GEN_Q);
    gl.glDisable(GL.GL_TEXTURE_GEN_R);
    gl.glDisable(GL.GL_TEXTURE_GEN_S);
    gl.glDisable(GL.GL_TEXTURE_GEN_T);

    for (int i = 0; i < 6; i++) {
      gl.glDisable(GL.GL_CLIP_PLANE0 + i);
    }

    gl.glDisable(GL.GL_TEXTURE_3D);
    gl.glDisable(GL.GL_TEXTURE_CUBE_MAP);

    if (gl.isExtensionAvailable("GL_NV_register_combiners")) {
      gl.glDisable(GL.GL_REGISTER_COMBINERS_NV);
    }

    if (gl.isExtensionAvailable("GL_SGI_texture_color_table")) {
      gl.glDisable(GL.GL_TEXTURE_COLOR_TABLE_SGI);
    }

    if (gl.isExtensionAvailable("GL_SUN_global_alpha")) {
      gl.glDisable(GL.GL_GLOBAL_ALPHA_SUN);
    }
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
    GraphicsConfiguration getGraphicsConfig(GraphicsConfiguration gconfig) {
      if (VERBOSE) System.err.println("JoglPipeline.getGraphicsConfig()");
      JoglGraphicsConfiguration config = (JoglGraphicsConfiguration) gconfig;
      GLCapabilitiesChooser indexChooser = null;
      if (config.getChosenIndex() >= 0) {
        indexChooser = new IndexCapabilitiesChooser(config.getChosenIndex());
      }

      AbstractGraphicsConfiguration absConfig =
        GLDrawableFactory.getFactory().chooseGraphicsConfiguration(config.getGLCapabilities(),
                                                                   indexChooser,
                                                                   new AWTGraphicsDevice(config.getDevice()));
      if (absConfig == null) {
        return null;
      }
      return ((AWTGraphicsConfiguration) absConfig).getGraphicsConfiguration();

      /*

        System.err.println("JoglPipeline.getGraphicsConfig()");
        // Just return the input graphics config for now. eventually, we will
        // use the input graphics config to get the GraphicsConfigTemplate3D
        // parameters, which we will use to create a new graphics config with JOGL.
        return gconfig;
      */
    }

    // Get the native FBconfig pointer
    long getFbConfig(GraphicsConfigInfo gcInfo) {
      if (VERBOSE) System.err.println("JoglPipeline.getFbConfig()");
      return 0L; // Dummy method in JOGL
    }


    private static final int DISABLE_STEREO = 1;
    private static final int DISABLE_AA = 2;
    private static final int DISABLE_DOUBLE_BUFFER = 3;

    // Get best graphics config from pipeline
    GraphicsConfiguration getBestConfiguration(GraphicsConfigTemplate3D gct,
            GraphicsConfiguration[] gc) {
      if (VERBOSE) System.err.println("JoglPipeline.getBestConfiguration()");
      /*
      System.err.println("gct.getDoubleBuffer(): " + gct.getDoubleBuffer());
      System.err.println("gct.getStereo():       " + gct.getStereo());
      System.err.println("gct.getDepthBits():    " + gct.getDepthSize());
      System.err.println("gct.getRedSize():      " + gct.getRedSize());
      System.err.println("gct.getGreenSize():    " + gct.getGreenSize());
      System.err.println("gct.getBlueSize():     " + gct.getBlueSize());
      System.err.println("gct.getSceneAntialiasing(): " + gct.getSceneAntialiasing());
      */

      // Create a GLCapabilities based on the GraphicsConfigTemplate3D
      GLCapabilities caps = new GLCapabilities();
      caps.setDoubleBuffered(gct.getDoubleBuffer() <= GraphicsConfigTemplate.PREFERRED);
      caps.setStereo        (gct.getStereo() <= GraphicsConfigTemplate.PREFERRED);
      caps.setDepthBits     (gct.getDepthSize());
      caps.setStencilBits   (gct.getStencilSize());
      caps.setRedBits       (Math.max(5, gct.getRedSize()));
      caps.setGreenBits     (Math.max(5, gct.getGreenSize()));
      caps.setBlueBits      (Math.max(5, gct.getBlueSize()));
      caps.setSampleBuffers (gct.getSceneAntialiasing() <= GraphicsConfigTemplate.PREFERRED);
      // FIXME: should be smarter about choosing the number of samples
      // (Java3D's native code has a loop trying 8, 6, 4, 3, and 2 samples)
      caps.setNumSamples(4);

      java.util.List<Integer> capsToDisable = new ArrayList<Integer>();
      // Add PREFERRED capabilities in order we will try disabling them
      if (gct.getStereo() == GraphicsConfigTemplate.PREFERRED) {
        capsToDisable.add(new Integer(DISABLE_STEREO));
      }
      if (gct.getSceneAntialiasing() == GraphicsConfigTemplate.PREFERRED) {
        capsToDisable.add(new Integer(DISABLE_AA));
      }
      if (gct.getDoubleBuffer() == GraphicsConfigTemplate.PREFERRED) {
        capsToDisable.add(new Integer(DISABLE_DOUBLE_BUFFER));
      }

      // Pick the GraphicsDevice from a random configuration
      GraphicsDevice dev = gc[0].getDevice();
      
      // Create a Frame and dummy GLCanvas to perform eager pixel format selection

      // Note that we loop in similar fashion to the NativePipeline's
      // native code in the situation where we need to disable certain
      // capabilities which aren't required
      boolean tryAgain = true;
      CapabilitiesCapturer capturer = null;
      while (tryAgain) {
        Frame f = new Frame(dev.getDefaultConfiguration());
        f.setUndecorated(true);
        f.setLayout(new BorderLayout());
        capturer = new CapabilitiesCapturer();
        try {
          QueryCanvas canvas = new QueryCanvas(caps, capturer, dev);
          f.add(canvas, BorderLayout.CENTER);
          f.setSize(1, 1);
          f.setVisible(true);
          if (DEBUG_CONFIG) {
            System.err.println("Waiting for CapabilitiesCapturer");
          }
          // Try to wait for result without blocking EDT
          if (!EventQueue.isDispatchThread()) {
            synchronized(capturer) {
              if (!capturer.done()) {
                try {
                  capturer.wait(WAIT_TIME);
                } catch (InterruptedException e) {
                }
              }
            }
          }
          disposeOnEDT(f);
          tryAgain = false;
        } catch (GLException e) {
          // Failure to select a pixel format; try switching off one
          // of the only-preferred capabilities
          if (capsToDisable.size() == 0) {
            tryAgain = false;
          } else {
            int whichToDisable = capsToDisable.remove(0).intValue();
            switch (whichToDisable) {
              case DISABLE_STEREO:
                caps.setStereo(false);
                break;

              case DISABLE_AA:
                caps.setSampleBuffers(false);
                break;

              case DISABLE_DOUBLE_BUFFER:
                caps.setDoubleBuffered(false);
                break;

              default:
                throw new InternalError();
            }
          }
        }
      }
      int chosenIndex = capturer.getChosenIndex();
      GLCapabilities chosenCaps = null;
      if (chosenIndex < 0) {
        if (DEBUG_CONFIG) {
          System.err.println("CapabilitiesCapturer returned invalid index");
        }
        // It's possible some platforms or implementations might not
        // support the GLCapabilitiesChooser mechanism; feed in the
        // same GLCapabilities later which we gave to the selector
        chosenCaps = caps;
      } else {
        if (DEBUG_CONFIG) {
          System.err.println("CapabilitiesCapturer returned index=" + chosenIndex);
        }
        chosenCaps = capturer.getCapabilities();
      }

      JoglGraphicsConfiguration config = new JoglGraphicsConfiguration(caps, chosenIndex, dev);

      // FIXME: because of the fact that JoglGraphicsConfiguration
      // doesn't override hashCode() or equals(), we will basically be
      // creating a new one each time getBestConfiguration() is
      // called; in theory, we should probably map the same
      // GLCapabilities on the same GraphicsDevice to the same
      // JoglGraphicsConfiguration object

      // Cache the GraphicsTemplate3D
      synchronized (Canvas3D.graphicsConfigTable) {
        GraphicsConfigInfo gcInfo = new GraphicsConfigInfo(gct);
        // We don't need this
        // gcInfo.setPrivateData(privateData);
        Canvas3D.graphicsConfigTable.put(config, gcInfo);
      }

      return config;

      /*

        // TODO: implement this

        // TODO: construct a unique GraphicsConfiguration object that will be
        // used the key in the hashmap so we can lookup the GraphicsTemplate3D
        GraphicsConfiguration gc1 = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration();

        // Cache the GraphicsTemplate3D
        synchronized (Canvas3D.graphicsConfigTable) {
          if (Canvas3D.graphicsConfigTable.get(gc1) == null) {
          GraphicsConfigInfo gcInfo = new GraphicsConfigInfo(gct);
          //                gcInfo.setPrivateData(privateData);
          Canvas3D.graphicsConfigTable.put(gc1, gcInfo);
          }
        }   
        return gc1;

      */
    }

    // Determine whether specified graphics config is supported by pipeline
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
    boolean hasDoubleBuffer(Canvas3D cv) {
      if (VERBOSE) System.err.println("JoglPipeline.hasDoubleBuffer()");
      if (VERBOSE) System.err.println("  Returning " + caps(cv).getDoubleBuffered());
      return caps(cv).getDoubleBuffered();
    }

    boolean hasStereo(Canvas3D cv) {
      if (VERBOSE) System.err.println("JoglPipeline.hasStereo()");
      if (VERBOSE) System.err.println("  Returning " + caps(cv).getStereo());
      return caps(cv).getStereo();
    }

    int getStencilSize(Canvas3D cv) {
      if (VERBOSE) System.err.println("JoglPipeline.getStencilSize()");
      if (VERBOSE) System.err.println("  Returning " + caps(cv).getStencilBits());
      return caps(cv).getStencilBits();
    }

    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
      if (VERBOSE) System.err.println("JoglPipeline.hasSceneAntialiasingMultisample()");
      if (VERBOSE) System.err.println("  Returning " + caps(cv).getSampleBuffers());

      return caps(cv).getSampleBuffers();
    }

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

    // Methods to get native WS display and screen
    long getDisplay() {
      if (VERBOSE) System.err.println("JoglPipeline.getDisplay()");
      return 0L; // Dummy method in JOGL
    }

    private boolean checkedForGetScreenMethod = false;
    private Method getScreenMethod = null;
    int getScreen(final GraphicsDevice graphicsDevice) {
      if (VERBOSE) System.err.println("JoglPipeline.getScreen()");

      if (!checkedForGetScreenMethod) {
        // All of the Sun GraphicsDevice implementations have a method
        //   int getScreen();
        // which we want to call reflectively if it's available.
        AccessController.doPrivileged(new PrivilegedAction() {
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
  class QueryCanvas extends Canvas {
    private GLDrawable drawable;
    private ExtendedCapabilitiesChooser chooser;

    public QueryCanvas(GLCapabilities capabilities,
                       ExtendedCapabilitiesChooser chooser,
                       GraphicsDevice device) {
      // The platform-specific GLDrawableFactory will only provide a
      // non-null GraphicsConfiguration on platforms where this is
      // necessary (currently only X11, as Windows allows the pixel
      // format of the window to be set later and Mac OS X seems to
      // handle this very differently than all other platforms). On
      // other platforms this method returns null; it is the case (at
      // least in the Sun AWT implementation) that this will result in
      // equivalent behavior to calling the no-arg super() constructor
      // for Canvas.
      super(unwrap((AWTGraphicsConfiguration)
                   GLDrawableFactory.getFactory().chooseGraphicsConfiguration(capabilities,
                                                                              chooser,
                                                                              new AWTGraphicsDevice(device))));
      drawable = GLDrawableFactory.getFactory().getGLDrawable(this, capabilities, chooser);
      this.chooser = chooser;
    }

    public void addNotify() {
      super.addNotify();
      drawable.setRealized(true);
      GLContext context = drawable.createContext(null);
      int res = context.makeCurrent();
      if (res != GLContext.CONTEXT_NOT_CURRENT) {
        chooser.init(context);
        context.release();
      }
      context.destroy();
    }
  }

  private static GraphicsConfiguration unwrap(AWTGraphicsConfiguration config) {
    if (config == null) {
      return null;
    }
    return config.getGraphicsConfiguration();
  }

  // Used in conjunction with IndexCapabilitiesChooser in pixel format
  // selection -- see getBestConfiguration
  class CapabilitiesCapturer extends DefaultGLCapabilitiesChooser implements ExtendedCapabilitiesChooser {
    private boolean done;
    private GLCapabilities capabilities;
    private int chosenIndex = -1;

    public boolean done() {
      return done;
    }

    public GLCapabilities getCapabilities() {
      return capabilities;
    }

    public int getChosenIndex() {
      return chosenIndex;
    }

    public int chooseCapabilities(GLCapabilities desired,
                                  GLCapabilities[] available,
                                  int windowSystemRecommendedChoice) {
      int res = super.chooseCapabilities(desired, available, windowSystemRecommendedChoice);
      capabilities = available[res];
      chosenIndex = res;
      markDone();
      return res;
    }

    public void init(GLContext context) {
      // Avoid hanging things up for several seconds
      kick();
    }

    private void markDone() {
      synchronized (this) {
        done = true;
        notifyAll();
      }
    }

    private void kick() {
      synchronized (this) {
        notifyAll();
      }
    }
  }

  // Used to support the query context mechanism -- needs to be more
  // than just a GLCapabilitiesChooser
  class ContextQuerier extends DefaultGLCapabilitiesChooser implements ExtendedCapabilitiesChooser {
    private Canvas3D canvas;
    private boolean glslLibraryAvailable;
    private boolean cgLibraryAvailable;
    private boolean done;

    public ContextQuerier(Canvas3D canvas,
                          boolean glslLibraryAvailable,
                          boolean cgLibraryAvailable) {
      this.canvas = canvas;
      this.glslLibraryAvailable = glslLibraryAvailable;
      this.cgLibraryAvailable = cgLibraryAvailable;
    }

    public boolean done() {
      return done;
    }

    public void init(GLContext context) {
      // This is basically a temporary
      JoglContext jctx = new JoglContext(context);
      // Set up various properties
      if (getPropertiesFromCurrentContext(jctx)) {
        setupCanvasProperties(canvas, jctx, context.getGL(),
                              glslLibraryAvailable,
                              cgLibraryAvailable);
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

  // Used in two phases of pixel format selection: transforming the
  // JoglGraphicsConfiguration to a real AWT GraphicsConfiguration and
  // during context creation to select exactly the same graphics
  // configuration as was done during getBestConfiguration.
  class IndexCapabilitiesChooser implements GLCapabilitiesChooser {
    private int indexToChoose;

    IndexCapabilitiesChooser(int indexToChoose) {
      this.indexToChoose = indexToChoose;
    }

    public int chooseCapabilities(GLCapabilities desired,
                                  GLCapabilities[] available,
                                  int windowSystemRecommendedChoice) {
      if (DEBUG_CONFIG) {
        System.err.println("IndexCapabilitiesChooser returning index=" + indexToChoose);
      }
      return indexToChoose;
    }
  }

  private void disposeOnEDT(final Frame f) {
    Runnable r = new Runnable() {
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
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
      if (VERBOSE) System.err.println("JoglPipeline.createDrawingSurfaceObject()");
      return new JoglDrawingSurfaceObject(cv);
    }

    // Method to free the drawing surface object
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
      if (VERBOSE) System.err.println("JoglPipeline.freeDrawingSurface()");
      // This method is a no-op
    }

    // Method to free the native drawing surface object
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
    return ((JoglGraphicsConfiguration) ctx.graphicsConfiguration).getGLCapabilities();
  }

  //----------------------------------------------------------------------
  // General helper routines
  //

  private static ThreadLocal nioVertexTemp = new ThreadLocal();
  private static ThreadLocal nioVertexDoubleTemp = new ThreadLocal();
  private static ThreadLocal nioColorTemp = new ThreadLocal();
  private static ThreadLocal nioColorByteTemp = new ThreadLocal();
  private static ThreadLocal nioNormalTemp = new ThreadLocal();
  private static ThreadLocal nioTexCoordSetTemp = new ThreadLocal();
  private static ThreadLocal nioVertexAttrSetTemp = new ThreadLocal();

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

  private static FloatBuffer getNIOBuffer(float[] array, ThreadLocal threadLocal, boolean copyData) {
    if (array == null) {
      return null;
    }
    FloatBuffer buf = (FloatBuffer) threadLocal.get();
    if (buf == null) {
      buf = BufferUtil.newFloatBuffer(array.length);
      threadLocal.set(buf);
    } else {
      buf.rewind();
      if (buf.remaining() < array.length) {
        int newSize = Math.max(2 * buf.remaining(), array.length);
        buf = BufferUtil.newFloatBuffer(newSize);
        threadLocal.set(buf);
      }
    }
    if (copyData) {
      buf.put(array);
      buf.rewind();
    }
    return buf;
  }

  private static DoubleBuffer getNIOBuffer(double[] array, ThreadLocal threadLocal, boolean copyData) {
    if (array == null) {
      return null;
    }
    DoubleBuffer buf = (DoubleBuffer) threadLocal.get();
    if (buf == null) {
      buf = BufferUtil.newDoubleBuffer(array.length);
      threadLocal.set(buf);
    } else {
      buf.rewind();
      if (buf.remaining() < array.length) {
        int newSize = Math.max(2 * buf.remaining(), array.length);
        buf = BufferUtil.newDoubleBuffer(newSize);
        threadLocal.set(buf);
      }
    }
    if (copyData) {
      buf.put(array);
      buf.rewind();
    }
    return buf;
  }

  private static ByteBuffer getNIOBuffer(byte[] array, ThreadLocal threadLocal, boolean copyData) {
    if (array == null) {
      return null;
    }
    ByteBuffer buf = (ByteBuffer) threadLocal.get();
    if (buf == null) {
      buf = BufferUtil.newByteBuffer(array.length);
      threadLocal.set(buf);
    } else {
      buf.rewind();
      if (buf.remaining() < array.length) {
        int newSize = Math.max(2 * buf.remaining(), array.length);
        buf = BufferUtil.newByteBuffer(newSize);
        threadLocal.set(buf);
      }
    }
    if (copyData) {
      buf.put(array);
      buf.rewind();
    }
    return buf;
  }

  private static FloatBuffer[] getNIOBuffer(Object[] array, ThreadLocal threadLocal) {
    if (array == null) {
      return null;
    }
    FloatBuffer[] bufs = (FloatBuffer[]) threadLocal.get();

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
        buf = BufferUtil.newFloatBuffer(cur.length);
        bufs[i] = buf;
      } else {
        buf.rewind();
        if (buf.remaining() < cur.length) {
          int newSize = Math.max(2 * buf.remaining(), cur.length);
          buf = BufferUtil.newFloatBuffer(newSize);
          bufs[i] = buf;
        }
      }
      buf.put(cur);
      buf.rewind();
    }

    return bufs;
  }
}
