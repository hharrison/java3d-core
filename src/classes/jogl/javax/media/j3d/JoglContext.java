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

  // Implementation of vertex attribute methods
  static interface VertexAttributeImpl {
    // FIXME: more strongly type the "object" arguments to these methods
    // Need a JOGL-specific ShaderProgram implementation to map
    // e.g. int indices to CGparameters
    public void vertexAttrPointer(GL gl, Object program,
                                  int index, int size, int type, int stride, Buffer pointer);
    public void enableVertexAttrArray(GL gl, Object program, int index);
    public void disableVertexAttrArray(GL gl, Object program, int index);
    public void vertexAttr1fv(GL gl, Object program, int index, FloatBuffer buf);
    public void vertexAttr2fv(GL gl, Object program, int index, FloatBuffer buf);
    public void vertexAttr3fv(GL gl, Object program, int index, FloatBuffer buf);
    public void vertexAttr4fv(GL gl, Object program, int index, FloatBuffer buf);
  }
  private VertexAttributeImpl vertexAttrImpl;

  static class CgVertexAttributeImpl implements VertexAttributeImpl {
    public void vertexAttrPointer(GL gl, Object program,
                                  int index, int size, int type, int stride, Buffer pointer) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void enableVertexAttrArray(GL gl, Object program, int index) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void disableVertexAttrArray(GL gl, Object program, int index) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    // NOTE: we should never get here. These functions are only called
    // when building display lists for geometry arrays with vertex
    // attributes, and such display lists are disabled in Cg mode.
    public void vertexAttr1fv(GL gl, Object program, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr1fv");
    }

    public void vertexAttr2fv(GL gl, Object program, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr2fv");
    }

    public void vertexAttr3fv(GL gl, Object program, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr3fv");
    }

    public void vertexAttr4fv(GL gl, Object program, int index, FloatBuffer buf) {
      throw new RuntimeException("Java 3D ERROR : Assertion failed: invalid call to cgVertexAttr4fv");
    }
  }

  static class GLSLVertexAttributeImpl implements VertexAttributeImpl {
    public void vertexAttrPointer(GL gl, Object program,
                                  int index, int size, int type, int stride, Buffer pointer) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void enableVertexAttrArray(GL gl, Object program, int index) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void disableVertexAttrArray(GL gl, Object program, int index) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void vertexAttr1fv(GL gl, Object program, int index, FloatBuffer buf) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void vertexAttr2fv(GL gl, Object program, int index, FloatBuffer buf) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void vertexAttr3fv(GL gl, Object program, int index, FloatBuffer buf) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }

    public void vertexAttr4fv(GL gl, Object program, int index, FloatBuffer buf) {
      // FIXME
      throw new RuntimeException("Not yet implemented");
    }
  }

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

  void vertexAttrPointer(GL gl, Object program,
                         int index, int size, int type, int stride, Buffer pointer) {
    vertexAttrImpl.vertexAttrPointer(gl, program, index, size, type, stride, pointer);    
  }

  void enableVertexAttrArray(GL gl, Object program, int index) {
    vertexAttrImpl.enableVertexAttrArray(gl, program, index);
  }

  void disableVertexAttrArray(GL gl, Object program, int index) {
    vertexAttrImpl.disableVertexAttrArray(gl, program, index);
  }

  void vertexAttr1fv(GL gl, Object program, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr1fv(gl, program, index, buf);
  }

  void vertexAttr2fv(GL gl, Object program, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr2fv(gl, program, index, buf);
  }

  void vertexAttr3fv(GL gl, Object program, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr3fv(gl, program, index, buf);
  }

  void vertexAttr4fv(GL gl, Object program, int index, FloatBuffer buf) {
    vertexAttrImpl.vertexAttr4fv(gl, program, index, buf);
  }

  // Only used when Cg shaders are in use
  CGcontext getCgContext()              { return cgContext;           }
  void      setCgContext(CGcontext c)   { cgContext = c;              }
  int       getCgVertexProfile()        { return cgVertexProfile;     }
  void      setCgVertexProfile(int p)   { cgVertexProfile = p;        }
  int       getCgFragmentProfile()      { return cgFragmentProfile;   }
  void      setCgFragmentProfile(int p) { cgFragmentProfile = p;      }
}
