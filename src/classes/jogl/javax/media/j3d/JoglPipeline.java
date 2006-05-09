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
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
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
        // TODO: for globalShadingLanguage == CG,  set the
        // cgLibraryAvailable flag to true if the JOGL CG code
        // is compiled, and if the native CG library is available
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
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.executeVA()");

        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.executeVABuffer()");

        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.executeInterleavedBuffer()");
        // TODO: implement this
    }

    void setVertexFormat(Context ctx, GeometryArrayRetained geo,
            int vformat, boolean useAlpha, boolean ignoreVertexColors) {
      if (DEBUG) System.err.println("JoglPipeline.setVertexFormat()");
        // TODO: implement this
    }

    void disableGlobalAlpha(Context ctx, GeometryArrayRetained geo, int vformat,
            boolean useAlpha, boolean ignoreVertexColors) {
      if (DEBUG) System.err.println("JoglPipeline.disableGlobalAlpha()");
        // TODO: implement this
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
      GL gl = context(ctx).getGL();
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
              // FIXME
              throw new RuntimeException("Vertex attributes not implemented yet");

              /*
		    int vaIdx, vaOff;

		    vaOff = vAttrOff;
		    for (vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
			switch (vAttrSizesPtr[vaIdx]) {
			case 1:
			    ctxProperties->vertexAttr1fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 2:
			    ctxProperties->vertexAttr2fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 3:
			    ctxProperties->vertexAttr3fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			case 4:
			    ctxProperties->vertexAttr4fv(ctxProperties, vaIdx, &verts[vaOff]);
			    break;
			}

			vaOff += vAttrSizesPtr[vaIdx];
		    }
		}
              */
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
            // FIXME
            throw new RuntimeException("Vertex attributes not implemented yet");

            /*

            int vaIdx, vaOff;

            vaOff = vAttrOff;
            for (vaIdx = 0; vaIdx < vertexAttrCount; vaIdx++) {
              switch (vAttrSizesPtr[vaIdx]) {
              case 1:
                ctxProperties->vertexAttr1fv(ctxProperties, vaIdx, &verts[vaOff]);
                break;
              case 2:
                ctxProperties->vertexAttr2fv(ctxProperties, vaIdx, &verts[vaOff]);
                break;
              case 3:
                ctxProperties->vertexAttr3fv(ctxProperties, vaIdx, &verts[vaOff]);
                break;
              case 4:
                ctxProperties->vertexAttr4fv(ctxProperties, vaIdx, &verts[vaOff]);
                break;
              }

              vaOff += vAttrSizesPtr[vaIdx];
              }

            */
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
            int coordIndex, float[] vfcoords, double[] vdcoords,
            int colorIndex, float[] cfdata, byte[] cbdata,
            int normalIndex, float[] ndata,
            int vertexAttrCount, int[] vertexAttrSizes,
            int[] vertexAttrIndex, float[][] vertexAttrData,
            int texcoordmaplength,
            int[] texcoordoffset,
            int[] texIndex, int texstride, Object[] texCoords,
            double[] xform, double[] nxform) {
      if (DEBUG) System.err.println("JoglPipeline.buildGAForByRef()");
        // TODO: implement this
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
    int vAttrOff = 0;
    int vAttrStride = 0;
    int bstride = 0, cbstride = 0;
    int[] sarray = null;
    int[] start_array = null;
    FloatBuffer verts = null;
    FloatBuffer clrs  = null;
    int texSize = 0;
    int texStride = 0;

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
    if (gl.isExtensionAvailable("GL_EXT_rescale_normal") && isNonUniformScale) {
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
          // FIXME
          throw new RuntimeException("Vertex attributes not implemented yet");

          /*
            jfloat *vAttrPtr = &verts[vAttrOff];

            for (i = 0; i < vertexAttrCount; i++) {
            ctxProperties->enableVertexAttrArray(ctxProperties, i);
            ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
            GL_FLOAT, bstride, vAttrPtr);
            vAttrPtr += vAttrSizesPtr[i];
            }
          */
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
          // FIXME
          throw new RuntimeException("Vertex attributes not implemented yet");

          /*
            jfloat *vAttrPtr = &verts[vAttrOff];

            for (i = 0; i < vertexAttrCount; i++) {
            ctxProperties->enableVertexAttrArray(ctxProperties, i);
            ctxProperties->vertexAttrPointer(ctxProperties, i, vAttrSizesPtr[i],
            GL_FLOAT, bstride, vAttrPtr);
            vAttrPtr += vAttrSizesPtr[i];
            }
          */
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
    if (gl.isExtensionAvailable("GL_EXT_rescale_normal") && isNonUniformScale) {
      gl.glDisable(GL.GL_NORMALIZE);
    }
        
    if ((vformat & GeometryArray.VERTEX_ATTRIBUTES) != 0) {
      // FIXME
      throw new RuntimeException("Vertex attributes not implemented yet");
      // resetVertexAttrs(ctxInfo, vertexAttrCount);
    }

    if ((vformat & GeometryArray.TEXTURE_COORDINATE) != 0) {
      resetTexture(gl, ctx);
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
            float[] varray, float[] cdata,
            int pass, int cdirty,
            int[] indexCoord) {
      if (DEBUG) System.err.println("JoglPipeline.executeIndexedGeometry()");
        // TODO: implement this
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
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.executeIndexedGeometryBuffer()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.executeIndexedGeometryVABuffer()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.buildIndexedGeometry()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.readRasterNative()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // CgShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    ShaderError setCgUniform1i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform1i()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform1f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform1f()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform2i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform2i()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform2f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform2f()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform3i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform3i()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform3f()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform4i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform4i()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform4f()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniformMatrix3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniformMatrix3f()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniformMatrix4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniformMatrix4f()");
        // TODO: implement this
        return null;
    }

    // ShaderAttributeArray methods

    ShaderError setCgUniform1iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform1iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform1fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform1fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform2iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform2iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform2fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform2fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform3iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform3iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform3fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform4iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform4iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniform4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniform4fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniformMatrix3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniformMatrix3fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setCgUniformMatrix4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setCgUniformMatrix4fArray()");
        // TODO: implement this
        return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createCgShader(Context ctx, int shaderType, long[] shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.createCgShader()");
        // TODO: implement this
        return null;
    }
    ShaderError destroyCgShader(Context ctx, long shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.destroyCgShader()");
        // TODO: implement this
        return null;
    }
    ShaderError compileCgShader(Context ctx, long shaderId, String program) {
      if (DEBUG) System.err.println("JoglPipeline.compileCgShader()");
        // TODO: implement this
        return null;
    }

    ShaderError createCgShaderProgram(Context ctx, long[] shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.createCgShaderProgram()");
        // TODO: implement this
        return null;
    }
    ShaderError destroyCgShaderProgram(Context ctx, long shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.destroyCgShaderProgram()");
        // TODO: implement this
        return null;
    }
    ShaderError linkCgShaderProgram(Context ctx, long shaderProgramId,
            long[] shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.linkCgShaderProgram()");
        // TODO: implement this
        return null;
    }
    void lookupCgVertexAttrNames(Context ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, boolean[] errArr) {
      if (DEBUG) System.err.println("JoglPipeline.lookupCgVertexAttrNames()");
        // TODO: implement this
    }
    void lookupCgShaderAttrNames(Context ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
      if (DEBUG) System.err.println("JoglPipeline.lookupCgShaderAttrNames()");
        // TODO: implement this
    }

    ShaderError useCgShaderProgram(Context ctx, long shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.useCgShaderProgram()");
        // TODO: implement this
        return null;
    }


    // ---------------------------------------------------------------------

    //
    // GLSLShaderProgramRetained methods
    //

    // ShaderAttributeValue methods

    ShaderError setGLSLUniform1i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform1i()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform1f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform1f()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform2i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform2i()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform2f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform2f()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform3i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform3i()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform3f()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform4i(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform4i()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform4f()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniformMatrix3f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniformMatrix3f()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniformMatrix4f(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniformMatrix4f()");
        // TODO: implement this
        return null;
    }

    // ShaderAttributeArray methods

    ShaderError setGLSLUniform1iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform1iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform1fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform1fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform2iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform2iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform2fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform2fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform3iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform3iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform3fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform4iArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            int[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform4iArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniform4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniform4fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniformMatrix3fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniformMatrix3fArray()");
        // TODO: implement this
        return null;
    }

    ShaderError setGLSLUniformMatrix4fArray(Context ctx,
            long shaderProgramId,
            long uniformLocation,
            int numElements,
            float[] value) {
      if (DEBUG) System.err.println("JoglPipeline.setGLSLUniformMatrix4fArray()");
        // TODO: implement this
        return null;
    }

    // interfaces for shader compilation, etc.
    ShaderError createGLSLShader(Context ctx, int shaderType, long[] shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.createGLSLShader()");
        // TODO: implement this
        return null;
    }
    ShaderError destroyGLSLShader(Context ctx, long shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.destroyGLSLShader()");
        // TODO: implement this
        return null;
    }
    ShaderError compileGLSLShader(Context ctx, long shaderId, String program) {
      if (DEBUG) System.err.println("JoglPipeline.compileGLSLShader()");
        // TODO: implement this
        return null;
    }

    ShaderError createGLSLShaderProgram(Context ctx, long[] shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.createGLSLShaderProgram()");
        // TODO: implement this
        return null;
    }
    ShaderError destroyGLSLShaderProgram(Context ctx, long shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.destroyGLSLShaderProgram()");
        // TODO: implement this
        return null;
    }
    ShaderError linkGLSLShaderProgram(Context ctx, long shaderProgramId,
            long[] shaderId) {
      if (DEBUG) System.err.println("JoglPipeline.linkGLSLShaderProgram()");
        // TODO: implement this
        return null;
    }
    ShaderError bindGLSLVertexAttrName(Context ctx, long shaderProgramId,
            String attrName, int attrIndex) {
      if (DEBUG) System.err.println("JoglPipeline.bindGLSLVertexAttrName()");
        // TODO: implement this
        return null;
    }
    void lookupGLSLShaderAttrNames(Context ctx, long shaderProgramId,
            int numAttrNames, String[] attrNames, long[] locArr,
            int[] typeArr, int[] sizeArr, boolean[] isArrayArr) {
      if (DEBUG) System.err.println("JoglPipeline.lookupGLSLShaderAttrNames()");
        // TODO: implement this
    }

    ShaderError useGLSLShaderProgram(Context ctx, long shaderProgramId) {
      if (DEBUG) System.err.println("JoglPipeline.useGLSLShaderProgram()");
        // TODO: implement this
        return null;
    }


    // ---------------------------------------------------------------------

    //
    // ImageComponent2DRetained methods
    //

    // free d3d surface referred to by id
    void freeD3DSurface(ImageComponent2DRetained image, int hashId) {
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // RasterRetained methods
    //

    // Native method that does the rendering
    void executeRaster(Context ctx, GeometryRetained geo,
            boolean updateAlpha, float alpha,
            int type, int width, int height,
            int xSrcOffset, int ySrcOffset,
            float x, float y, float z, byte[] image) {
      if (DEBUG) System.err.println("JoglPipeline.executeRaster()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // Renderer methods
    //

    void cleanupRenderer() {
      if (DEBUG) System.err.println("JoglPipeline.cleanupRenderer()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateExponentialFog()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // LinearFogRetained methods
    //

    void updateLinearFog(Context ctx,
            float red, float green, float blue,
            double fdist, double bdist) {
      if (DEBUG) System.err.println("JoglPipeline.updateLinearFog()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateLineAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateModelClip()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // PointAttributesRetained methods
    //

    void updatePointAttributes(Context ctx, float pointSize, boolean pointAntialiasing) {
      if (DEBUG) System.err.println("JoglPipeline.updatePointAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updatePolygonAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateRenderingAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateTexCoordGeneration()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateTransparencyAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateTextureAttributes()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.updateRegisterCombiners()");
        // TODO: implement this
    }

    void updateTextureColorTable(Context ctx, int numComponents,
            int colorTableSize,
            int[] colorTable) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureColorTable()");
        // TODO: implement this
    }

    void updateCombiner(Context ctx,
            int combineRgbMode, int combineAlphaMode,
            int[] combineRgbSrc, int[] combineAlphaSrc,
            int[] combineRgbFcn, int[] combineAlphaFcn,
            int combineRgbScale, int combineAlphaScale) {
      if (DEBUG) System.err.println("JoglPipeline.updateCombiner()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // TextureUnitStateRetained methods
    //

    void updateTextureUnitState(Context ctx, int unitIndex, boolean enableFlag) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureUnitState()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // TextureRetained methods
    // Texture2DRetained methods
    //

    void bindTexture2D(Context ctx, int objectId, boolean enable) {
      if (DEBUG) System.err.println("JoglPipeline.bindTexture2D()");
        // TODO: implement this
    }

    void updateTexture2DImage(Context ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DImage()");
        // TODO: implement this
    }

    void updateTexture2DSubImage(Context ctx,
            int level, int xoffset, int yoffset,
            int internalFormat, int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DSubImage()");
        // TODO: implement this
    }

    void updateTexture2DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DLodRange()");
        // TODO: implement this
    }

    void updateTexture2DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DLodOffset()");
        // TODO: implement this
    }

    void updateTexture2DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DBoundary()");
        // TODO: implement this
    }

    void updateDetailTextureParameters(Context ctx,
            int detailTextureMode,
            int detailTextureLevel,
            int nPts, float[] pts) {
      if (DEBUG) System.err.println("JoglPipeline.updateDetailTextureParameters()");
        // TODO: implement this
    }

    void updateTexture2DFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DFilterModes()");
        // TODO: implement this
    }

    void updateTexture2DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DSharpenFunc()");
        // TODO: implement this
    }

    void updateTexture2DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DFilter4Func()");
        // TODO: implement this
    }

    void updateTexture2DAnisotropicFilter(Context ctx, float degree) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture2DAnisotropicFilter()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // Texture3DRetained methods
    //

    void bindTexture3D(Context ctx, int objectId, boolean enable) {
      if (DEBUG) System.err.println("JoglPipeline.bindTexture3D()");
        // TODO: implement this
    }

    void updateTexture3DImage(Context ctx,
            int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height, int depth,
            int boundaryWidth,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DImage()");
        // TODO: implement this
    }

    void updateTexture3DSubImage(Context ctx,
            int level,
            int xoffset, int yoffset, int zoffset,
            int internalFormat, int storedFormat,
            int imgXoffset, int imgYoffset, int imgZoffset,
            int tilew, int tileh,
            int width, int height, int depth,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DSubImage()");
        // TODO: implement this
    }

    void updateTexture3DLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DLodRange()");
        // TODO: implement this
    }

    void updateTexture3DLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DLodOffset()");
        // TODO: implement this
    }

    void updateTexture3DBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            int boundaryModeR, float boundaryRed,
            float boundaryGreen, float boundaryBlue,
            float boundaryAlpha) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DBoundary()");
        // TODO: implement this
    }

    void updateTexture3DFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DFilterModes()");
        // TODO: implement this
    }

    void updateTexture3DSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DSharpenFunc()");
        // TODO: implement this
    }

    void updateTexture3DFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DFilter4Func()");
        // TODO: implement this
    }

    void updateTexture3DAnisotropicFilter(Context ctx, float degree) {
      if (DEBUG) System.err.println("JoglPipeline.updateTexture3DAnisotropicFilter()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // TextureCubeMapRetained methods
    //

    void bindTextureCubeMap(Context ctx, int objectId, boolean enable) {
      if (DEBUG) System.err.println("JoglPipeline.bindTextureCubeMap()");
        // TODO: implement this
    }

    void updateTextureCubeMapImage(Context ctx,
            int face, int numLevels, int level,
            int internalFormat, int storedFormat,
            int width, int height,
            int boundaryWidth,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapImage()");
        // TODO: implement this
    }

    void updateTextureCubeMapSubImage(Context ctx,
            int face, int level, int xoffset, int yoffset,
            int internalFormat,int storedFormat,
            int imgXOffset, int imgYOffset,
            int tilew, int width, int height,
            byte[] imageData) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapSubImage()");
        // TODO: implement this
    }

    void updateTextureCubeMapLodRange(Context ctx,
            int baseLevel, int maximumLevel,
            float minimumLod, float maximumLod) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapLodRange()");
        // TODO: implement this
    }

    void updateTextureCubeMapLodOffset(Context ctx,
            float lodOffsetX, float lodOffsetY,
            float lodOffsetZ) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapLodOffset()");
        // TODO: implement this
    }

    void updateTextureCubeMapBoundary(Context ctx,
            int boundaryModeS, int boundaryModeT,
            float boundaryRed, float boundaryGreen,
            float boundaryBlue, float boundaryAlpha) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapBoundary()");
        // TODO: implement this
    }

    void updateTextureCubeMapFilterModes(Context ctx,
            int minFilter, int magFilter) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapFilterModes()");
        // TODO: implement this
    }

    void updateTextureCubeMapSharpenFunc(Context ctx,
            int numSharpenTextureFuncPts,
            float[] sharpenTextureFuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapSharpenFunc()");
        // TODO: implement this
    }

    void updateTextureCubeMapFilter4Func(Context ctx,
            int numFilter4FuncPts,
            float[] filter4FuncPts) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapFilter4Func()");
        // TODO: implement this
    }

    void updateTextureCubeMapAnisotropicFilter(Context ctx, float degree) {
      if (DEBUG) System.err.println("JoglPipeline.updateTextureCubeMapAnisotropicFilter()");
        // TODO: implement this
    }


    // ---------------------------------------------------------------------

    //
    // DetailTextureImage methods
    //

    void bindDetailTexture(Context ctx, int objectId) {
      if (DEBUG) System.err.println("JoglPipeline.bindDetailTexture()");
        // TODO: implement this
    }

    void updateDetailTextureImage(Context ctx,
            int numLevels, int level,
            int format, int storedFormat,
            int width, int height,
            int boundaryWidth, byte[] data) {
      if (DEBUG) System.err.println("JoglPipeline.updateDetailTextureImage()");
        // TODO: implement this
    }


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
        // TODO: implement this
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
      if (cv.drawable == null) {
        draw =
          GLDrawableFactory.getFactory().getGLDrawable(cv,
                                                       ((JoglGraphicsConfiguration) cv.graphicsConfiguration).getGLCapabilities(),
                                                       null);
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
        setupCanvasProperties(cv, ctx, gl);
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
      if (DEBUG) System.err.println("JoglPipeline.createQueryContext()");
      // FIXME: for now just set some defaults in the Canvas3D

        // TODO: implement this
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
        // TODO: implement this
        return 0;
    }

    // notify D3D to toggle between FullScreen and window mode
    int toggleFullScreenMode(Canvas3D cv, Context ctx) {
        // TODO: implement this
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
      context.destroy();
      Thread.dumpStack();
      // FIXME: assuming this is the right point at which to make this call
      draw.setRealized(false);
    }

    // This is the native method for doing accumulation.
    void accum(Context ctx, float value) {
      if (DEBUG) System.err.println("JoglPipeline.accum()");
        // TODO: implement this
    }

    // This is the native method for doing accumulation return.
    void accumReturn(Context ctx) {
      if (DEBUG) System.err.println("JoglPipeline.accumReturn()");
        // TODO: implement this
    }

    // This is the native method for clearing the accumulation buffer.
    void clearAccum(Context ctx) {
      if (DEBUG) System.err.println("JoglPipeline.clearAccum()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.decal1stChildSetup()");
        // TODO: implement this
        return false;
    }

    // Native method for decal nth child setup
    void decalNthChildSetup(Context ctx) {
      if (DEBUG) System.err.println("JoglPipeline.decalNthChildSetup()");
        // TODO: implement this
    }

    // Native method for decal reset
    void decalReset(Context ctx, boolean depthBufferEnable) {
      if (DEBUG) System.err.println("JoglPipeline.decalReset()");
        // TODO: implement this
    }

    // Native method for eye lighting
    void ctxUpdateEyeLightingEnable(Context ctx, boolean localEyeLightingEnable) {
      if (DEBUG) System.err.println("JoglPipeline.ctxUpdateEyeLightingEnable()");

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
      if (DEBUG) System.err.println("JoglPipeline.setBlendColor()");
        // TODO: implement this
    }

    // native method for setting blend func
    void setBlendFunc(Context ctx, int src, int dst) {
      if (DEBUG) System.err.println("JoglPipeline.setBlendFunc()");
        // TODO: implement this
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
    void setFullSceneAntialiasing(Context ctx, boolean enable) {
      if (DEBUG) System.err.println("JoglPipeline.setFullSceneAntialiasing()");
        // TODO: implement this
    }

    void setGlobalAlpha(Context ctx, float alpha) {
      if (DEBUG) System.err.println("JoglPipeline.setGlobalAlpha()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.validGraphicsMode()");
        // TODO: implement this
        return true;
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
      if (DEBUG) System.err.println("JoglPipeline.updateTexUnitStateMap()");
        // TODO: implement this
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
      if (DEBUG) System.err.println("JoglPipeline.freeTexture()");
        // TODO: implement this
    }

    void composite(Context ctx, int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int rasWidth,  byte[] image,
            int winWidth, int winHeight) {
      if (DEBUG) System.err.println("JoglPipeline.composite()");
        // TODO: implement this
    }

    void texturemapping(Context ctx,
            int px, int py,
            int xmin, int ymin, int xmax, int ymax,
            int texWidth, int texHeight,
            int rasWidth,
            int format, int objectId,
            byte[] image,
            int winWidth, int winHeight) {
      if (DEBUG) System.err.println("JoglPipeline.texturemapping()");
        // TODO: implement this
    }

    boolean initTexturemapping(Context ctx, int texWidth,
            int texHeight, int objectId) {
      if (DEBUG) System.err.println("JoglPipeline.initTexturemapping()");
        // TODO: implement this
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
    ctx.setMaxTextureUnits(tmp[0]);
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

  private void setupCanvasProperties(Canvas3D cv,
                                     JoglContext ctx,
                                     GL gl) {
    // FIXME: this is a heavily abridged version of the code in
    // Canvas3D.c; need to pull much more in
    if (gl.isExtensionAvailable("GL_ARB_multitexture")) {
      cv.maxTextureUnits = ctx.getMaxTextureUnits();
      cv.maxTexCoordSets = ctx.getMaxTexCoordSets();
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
      AbstractGraphicsConfiguration absConfig =
        GLDrawableFactory.getFactory().chooseGraphicsConfiguration(config.getGLCapabilities(),
                                                                   null,
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
      if (DEBUG) System.err.println("JoglPipeline.getFbConfig()");
        return 0L; // Dummy method in JOGL
    }


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

      // Pick the GraphicsDevice from a random configuration
      GraphicsDevice dev = gc[0].getDevice();
      
      JoglGraphicsConfiguration config = new JoglGraphicsConfiguration(caps, dev);

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
      if (DEBUG) System.err.println("JoglPipeline.isGraphicsConfigSupported()");
        // TODO: implement this
        return true;
    }

    // Methods to get actual capabilities from Canvas3D
    boolean hasDoubleBuffer(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.hasDoubleBuffer()");
        // TODO: implement this
        return true;
    }

    boolean hasStereo(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.hasStereo()");
        // TODO: implement this
        return false;
    }

    int getStencilSize(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.getStencilSize()");
        // TODO: implement this
        return 0;
    }

    boolean hasSceneAntialiasingMultisample(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.hasSceneAntialiasingMultisample()");
        // TODO: implement this
        return false;
    }

    boolean hasSceneAntialiasingAccum(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.hasSceneAntialiasingAccum()");
        // TODO: implement this
        return false;
    }

    // Methods to get native WS display and screen
    long getDisplay() {
      if (DEBUG) System.err.println("JoglPipeline.getDisplay()");
        return 0L; // Dummy method in JOGL
    }

    int getScreen(GraphicsDevice graphicsDevice) {
      if (DEBUG) System.err.println("JoglPipeline.getScreen()");
        // TODO: implement this
        return 0;
    }


    // ---------------------------------------------------------------------

    //
    // DrawingSurfaceObject methods
    //

    // Method to construct a new DrawingSurfaceObject
    DrawingSurfaceObject createDrawingSurfaceObject(Canvas3D cv) {
      if (DEBUG) System.err.println("JoglPipeline.createDrawingSurfaceObject()");
        return new JoglDrawingSurfaceObject(cv);
    }

    // Method to free the drawing surface object
    void freeDrawingSurface(Canvas3D cv, DrawingSurfaceObject drawingSurfaceObject) {
      if (DEBUG) System.err.println("JoglPipeline.freeDrawingSurface()");
        // This method is a no-op
    }

    // Method to free the native drawing surface object
    void freeDrawingSurfaceNative(Object o) {
      if (DEBUG) System.err.println("JoglPipeline.freeDrawingSurfaceNative()");
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

  //----------------------------------------------------------------------
  // General helper routines
  //

  private static ThreadLocal nioVertexTemp = new ThreadLocal();
  private static ThreadLocal nioColorTemp = new ThreadLocal();
  private static FloatBuffer getVertexArrayBuffer(float[] vertexArray) {
    return getNIOBuffer(vertexArray, nioVertexTemp);
  }

  private static FloatBuffer getColorArrayBuffer(float[] colorArray) {
    return getNIOBuffer(colorArray, nioColorTemp);
  }

  private static FloatBuffer getNIOBuffer(float[] array, ThreadLocal threadLocal) {
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
    buf.put(array);
    buf.rewind();
    return buf;
  }

}
