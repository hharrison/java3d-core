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

package org.jogamp.java3d;

import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;

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

  // Only used when GLSL shader library is active
  private int        glslVertexAttrOffset;

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

void vertexAttrPointer(GL gl, int index, int size, int type, int stride, Buffer pointer) {
	gl.getGL2().glVertexAttribPointerARB(index + glslVertexAttrOffset, size,
	                                     type, false, stride, pointer);
}

void enableVertexAttrArray(GL gl, int index) {
	gl.getGL2().glEnableVertexAttribArrayARB(index + glslVertexAttrOffset);
}

void disableVertexAttrArray(GL gl, int index) {
	gl.getGL2().glDisableVertexAttribArrayARB(index + glslVertexAttrOffset);
}

void vertexAttr1fv(GL gl, int index, FloatBuffer buf) {
	gl.getGL2().glVertexAttrib1fvARB(index + glslVertexAttrOffset, buf);
}

void vertexAttr2fv(GL gl, int index, FloatBuffer buf) {
	gl.getGL2().glVertexAttrib2fvARB(index + glslVertexAttrOffset, buf);
}

void vertexAttr3fv(GL gl, int index, FloatBuffer buf) {
	gl.getGL2().glVertexAttrib3fvARB(index + glslVertexAttrOffset, buf);
}

void vertexAttr4fv(GL gl, int index, FloatBuffer buf) {
	gl.getGL2().glVertexAttrib4fvARB(index + glslVertexAttrOffset, buf);
}

  // Used in vertex attribute implementation
  JoglShaderObject getShaderProgram()                        { return shaderProgram;   }
  void             setShaderProgram(JoglShaderObject object) { shaderProgram = object; }

  // Only used when GLSL shaders are in use
  int  getGLSLVertexAttrOffset()           { return glslVertexAttrOffset;   }
  void setGLSLVertexAttrOffset(int offset) { glslVertexAttrOffset = offset; }
}
