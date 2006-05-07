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

import javax.media.opengl.*;

/**
 * Graphics context objects for Jogl rendering pipeline.
 */
class JoglContext implements Context {
  private GLContext  context;

  // Properties we need to keep track of for efficiency
  private int maxTextureUnits;
  private int maxTexCoordSets;
  private float alphaClearValue;

  JoglContext(GLContext context) {
    this.context = context;
  }

  GLContext getGLContext() {
    return context;
  }

  int   getMaxTextureUnits()          { return maxTextureUnits; }
  void  setMaxTextureUnits(int val)   { maxTextureUnits = val;  }
  int   getMaxTexCoordSets()          { return maxTexCoordSets; }
  void  setMaxTexCoordSets(int val)   { maxTexCoordSets = val;  }
  float getAlphaClearValue()          { return alphaClearValue; }
  void  setAlphaClearValue(float val) { alphaClearValue = val;  }
}
