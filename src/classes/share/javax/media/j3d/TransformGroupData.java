/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

class TransformGroupData extends NodeData {
    // per path node data
    // TODO: replace per path mirror objects with node data
    // TODO: move other TransfromGroup related data here
    boolean switchDirty = false;

    // use for eliminate multiple updates and generate unique targets
    boolean markedDirty = false;
}
