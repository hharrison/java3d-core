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

interface BHLeafInterface {
    
    abstract BoundingBox computeBoundingHull();

    abstract boolean isEnable();

    abstract boolean isEnable(int visibilityPolicy);
    
    // Can't use getLocale, it is used by BranchGroupRetained
    abstract Locale getLocale2();
    
}
