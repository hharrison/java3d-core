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

/**
 * A Node Component Update interface.  Any object that can be put in the 
 * node component updateCheck list must implement this interface.
 */

interface NodeComponentUpdate {

    /**
     * The actual update function.
     */
    abstract void updateNodeComponentCheck();
}
