/*
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import com.sun.opengl.cg.*;

class JoglCgShaderParameter extends JoglShaderObject {
  private CGparameter vParam;
  private CGparameter fParam;

  JoglCgShaderParameter(CGparameter vParam,
                        CGparameter fParam) {
    super(0);
    this.vParam = vParam;
    this.fParam = fParam;
  }

  CGparameter vParam() {
    return vParam;
  }

  CGparameter fParam() {
    return fParam;
  }
}
