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
import java.util.Hashtable;

import java.util.ArrayList;

/**
 * LeafRetained node.
 */
abstract class LeafRetained extends NodeRetained {

    SwitchState switchState = null;

    // temporary variable used during bounds computation, since
    // multiple mirror shapes could be pointing to the same shape3D
    boolean boundsDirty = false;

    // Appicable only to the mirror object
    void updateBoundingLeaf() {

    }
    protected Object clone(boolean forceDuplicate) {
       return super.clone();
    }

    void updateMirrorObject(Object[] args) {
    }

    void updateTransformChange() {
    }

    void updateBounds() {
    }

    void getMirrorObjects(ArrayList l, HashKey k) {
    }
}
