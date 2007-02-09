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

/*
 * A Object Update interface.  Any object that can be put in the ObjectUpdate list
 * must implement this interface.
 */

interface ObjectUpdate {

    /**
     * The actual update function.
     */
    abstract void updateObject();
}
