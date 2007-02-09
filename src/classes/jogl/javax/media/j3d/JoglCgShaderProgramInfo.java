/*
 * $RCSfile$
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import com.sun.opengl.cg.*;

class JoglCgShaderProgramInfo extends JoglShaderObject {
  private JoglCgShaderInfo vShader;  // vertex shader
  private JoglCgShaderInfo fShader;  // fragment shader
  // Array of parameters for (varying) vertex attributes
  private CGparameter[] vtxAttrs;

  JoglCgShaderProgramInfo() {
    super(0);
  }

  public JoglCgShaderInfo getVertexShader()                        { return vShader;   }
  public void             setVertexShader(JoglCgShaderInfo info)   { vShader = info;   }
  public JoglCgShaderInfo getFragmentShader()                      { return fShader;   }
  public void             setFragmentShader(JoglCgShaderInfo info) { fShader = info;   }
  public CGparameter[]    getVertexAttributes()                    { return vtxAttrs;  }
  public void             setVertexAttributes(CGparameter[] attrs) { vtxAttrs = attrs; }
  public int getNumVertexAttributes() {
    if (vtxAttrs == null)
      return 0;
    return vtxAttrs.length;
  }
}
