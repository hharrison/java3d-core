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

import java.nio.*;
import javax.media.opengl.*;
import com.sun.opengl.cg.*;

/**
 * Graphics context objects for Jogl rendering pipeline.
 */
class JoglContext implements Context {
  private GLContext  context;

  // Properties we need to keep track of for efficiency
  private int maxTexCoordSets;
  private float alphaClearValue;
  private int currentTextureUnit;
  private int currentCombinerUnit;
  private boolean hasMultisample;

  // Needed for vertex attribute implementation
  private JoglShaderObject shaderProgram;

  // Implementation of vertex attribute methods
  static interface VertexAttributeImpl {
    public void vertexAttrPointer(GL gl,
                                  int index, int size, int type, int stride, Buffer pointer);
    public void enableVertexAttrArray(GL gl, int index);
    public void disableVertexAttrArray(GL gl, int index);
    public void vertexAttr1fv(GL gl, int index, FloatBuffer buf);
    public void vertexAttr2fv(GL gl, int index, FloatBuffer buf);
    public void vertexAttr3fv(GL gl, int index, FloatBuffer buf);
    public void vertexAttr4fv(GL gl, int index, FloatBuffer buf);
  }
  private VertexAttributeImpl vertexAttrImpl;

  class CgVertexAttributeImpl implements VertexAttributeImpl {
    public void vertexAttrPointer(GL gl,
                                  int index, int size, int type, int stride, Buffer pointer) {
      JoglCgShaderProgramInfo shaderProgramInfo = (JoglCgShaderProgramInfo) shaderProgram;
      if (shaderProgramInfo != null && index < shaderProgramInfo.getNumVertexAttributes()) {
        CgGL.cgGLSetParameterPointer(shaderProgramInfo.getVertexAttributes()[index],
                                     size, type, stride, pointer);
      } else {
        if (shaderProgramInfo == null) {
          System.err.println("    shaderProgramInfo is null");
        } else {
          System.err.println("    index (" + index + ") out of range: numVtxAttrs = " +
                             shaderProgramInfo.getNumVertexAttributes());
        }
      }
    }

    public void enableVertexAttrArray(GL gl, int index) {
      JoglCgShaderProgramInfo shaderProgramInfo = (JoglCgShaderProgramInfo) shaderProgram;
      if (shaderProgramInfo != null && index < shaderProgramInfo.getNumVertexAttributes()) {
        CgGL.cgGLEnableClientState(shaderProgramInfo.getVertexAttributes()[index]);
      } else {
        if (shaderProgramInfo == null) {
          System.err.println("    shaderProgramInfo is null");
        } else {
          System.err.println("    index (" + index + ") out of range: numVtxAttrs = " +
                             shaderProgramInfo.getNumVertexAttributes());
        }
      }
    }

    public void disableVertexAttrArray(GL gl, int index) {
      JoglCgShaderProgramInfo shaderProgramInfo = (JoglCgShaderProgramInfo) shaderProgram;
      if (shaderProgramInfo != null && index < shaderProgramInfo.getNumVertexAttributes()) {
        CgGL.cgGLDisableClientState(shaderProgramInfo.getVertexAttributes()[index]);
      } else {
        if (shaderProgramInfo == null) {
          System.err.println("    shaderProgramInfo is null");
        } else {
          System.err.println("    index (" + index + ") out of range: numVtxAttrs = " +
                             shaderProgramInfo.getNumVertexAttributes());
        }
      }
    }

    // NOTE: we should never get here. These functions are only called
    // when building display lists for geometry arrays with vertex
    // attributes, and such display lists are disabled in Cg mode.
    public void vertexAttr1fv(GL gl, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr1fv");
    }

    public void vertexAttr2fv(GL gl, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr2fv");
    }

    public void vertexAttr3fv(GL gl, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr3fv");
    }

    public void vertexAttr4fv(GL gl, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr4fv");
    }
  }

  class GLSLVertexAttributeImpl implements VertexAttributeImpl {
    public void vertexAttrPointer(GL gl,
                                  int index, int size, int type, int stride, Buffer pointer) {
      gl.glVertexAttribPointerARB(index + glslVertexAttrOffset,
                                  size, type, false, stride, pointer);
    }

    public void enableVertexAttrArray(GL gl, int index) {
      gl.glEnableVertexAttribArrayARB(index + glslVertexAttrOffset);
    }

    public void disableVertexAttrArray(GL gl, int index) {
      gl.glDisableVertexAttribArrayARB(index + glslVertexAttrOffset);
    }

    public void vertexAttr1fv(GL gl, int index, FloatBuffer buf) {
      gl.glVertexAttrib1fvARB(index + glslVertexAttrOffset, buf);
    }

    public void vertexAttr2fv(GL gl, int index, FloatBuffer buf) {
      gl.glVertexAttrib2fvARB(index + glslVertexAttrOffset, buf);
    }

    public void vertexAttr3fv(GL gl, int index, FloatBuffer buf) {
      gl.glVertexAttrib3fvARB(index + glslVertexAttrOffset, buf);
    }

    public void vertexAttr4fv(GL gl, int index, FloatBuffer buf) {
      gl.glVertexAttrib4fvARB(index + glslVertexAttrOffset, buf);
    }
  }

  // Only used when GLSL shader library is active
  private int        glslVertexAttrOffset;

  // Only used when Cg shader library is active
  private CGcontext  cgContext; 
  private int        cgVertexProfile;
  private int        cgFragmentProfile;

  JoglContext(GLContext context) {
    this.context = context;
  }

  GLContext getGLContext() {
    return context;
  }

  int   getMaxTexCoordSets()            { return maxTexCoordSets;     }
  void  setMaxTexCoordSets(int val)     { maxTexCoordSets = val;      }
  float getAlphaClearValue()            { return alphaClearValue;     }
  void  setAlphaClearValue(float val)   { alphaClearValue = val;      }
  int   getCurrentTextureUnit()         { return currentTextureUnit;  }
  void  setCurrentTextureUnit(int val)  { currentTextureUnit = val;   }
  int   getCurrentCombinerUnit()        { return currentCombinerUnit; }
  void  setCurrentCombinerUnit(int val) { currentCombinerUnit = val;  }
  boolean getHasMultisample()           { return hasMultisample;      }
  void    setHasMultisample(boolean val){ hasMultisample = val;       }

  // Helpers for vertex attribute methods
  void  initCgVertexAttributeImpl() {
    if (vertexAttrImpl != null) {
      throw new RuntimeException("Should not initialize the vertex attribute implementation twice");
    }
    vertexAttrImpl = new CgVertexAttributeImpl();
  }

  void  initGLSLVertexAttributeImpl() {
    if (vertexAttrImpl != null) {
      throw new RuntimeException("Should not initialize the vertex attribute implementation twice");
    }
    vertexAttrImpl = new GLSLVertexAttributeImpl();
  }

  void vertexAttrPointer(GL gl,
                         int index, int size, int type, int stride, Buffer pointer) {
    vertexAttrImpl.vertexAttrPointer(gl, index, size, type, stride, pointer);
  }

  void enableVertexAttrArray(GL gl, int index) {
    vertexAttrImpl.enableVertexAttrArray(gl, index);
  }

  void disableVertexAttrArray(GL gl, int index) {
    vertexAttrImpl.disableVertexAttrArray(gl, index);
  }

  void vertexAttr1fv(GL gl, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr1fv(gl, index, buf);
  }

  void vertexAttr2fv(GL gl, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr2fv(gl, index, buf);
  }

  void vertexAttr3fv(GL gl, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr3fv(gl, index, buf);
  }

  void vertexAttr4fv(GL gl, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr4fv(gl, index, buf);
  }

  // Used in vertex attribute implementation
  JoglShaderObject getShaderProgram()                        { return shaderProgram;   }
  void             setShaderProgram(JoglShaderObject object) { shaderProgram = object; }

  // Only used when GLSL shaders are in use
  int  getGLSLVertexAttrOffset()           { return glslVertexAttrOffset;   }
  void setGLSLVertexAttrOffset(int offset) { glslVertexAttrOffset = offset; }

  // Only used when Cg shaders are in use
  CGcontext getCgContext()              { return cgContext;           }
  void      setCgContext(CGcontext c)   { cgContext = c;              }
  int       getCgVertexProfile()        { return cgVertexProfile;     }
  void      setCgVertexProfile(int p)   { cgVertexProfile = p;        }
  int       getCgFragmentProfile()      { return cgFragmentProfile;   }
  void      setCgFragmentProfile(int p) { cgFragmentProfile = p;      }
}
