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

package javax.media.j3d;

class GraphicsConfigInfo {
    private int reqStencilSize = 0;
    private long fbConfig = 0L;

    int getRequestedStencilSize() {
	return reqStencilSize;
    }

    void setRequestedStencilSize(int reqSS) {
	reqStencilSize = reqSS;
    }
    
    long getFBConfig() {
	return fbConfig;
    }
    
    void setFBConfig(long fbC) {
	fbConfig = fbC;
    }

}
