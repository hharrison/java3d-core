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

import javax.media.opengl.*;

/**
 * Drawable class for the Jogl rendering pipeline.
 */
class JoglDrawable implements Drawable {
  private GLDrawable  drawable;

  JoglDrawable(GLDrawable drawable) {
    this.drawable = drawable;
  }

  GLDrawable getGLDrawable() {
    return drawable;
  }
}
