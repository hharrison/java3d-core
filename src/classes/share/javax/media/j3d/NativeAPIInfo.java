/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

/*
 * Portions of this code were derived from work done by the Blackdown
 * group (www.blackdown.org), who did the initial Linux implementation
 * of the Java 3D API.
 */

package javax.media.j3d;

class NativeAPIInfo {

    /**
     * Returns the rendering API being used.
     * @return the rendering API, one of:
     * <code>MasterControl.RENDER_OPENGL_LINUX</code>,
     * <code>MasterControl.RENDER_OPENGL_SOLARIS</code>,
     * <code>MasterControl.RENDER_OPENGL_WIN32</code>,
     * or <code>MasterControl.RENDER_DIRECT3D</code>
     */
    native int getRenderingAPI();
}
