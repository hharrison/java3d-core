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

class JoglShaderObject implements ShaderProgramId, ShaderId, ShaderAttrLoc {
  private int val;

  JoglShaderObject(int val) {
    this.val = val;
  }

  int getValue() {
    return val;
  }
}
