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

import java.util.ArrayList;

class OrderedPath extends Object {
    ArrayList pathElements = new ArrayList(1);


    void addElementToPath(OrderedGroupRetained og, Integer orderedId) {
        pathElements.add(new OrderedPathElement(og, orderedId));
    }

    OrderedPath clonePath() {
        OrderedPath path = new OrderedPath();
        path.pathElements = (ArrayList)pathElements.clone();
        return path;
    }

    void printPath() {
        System.err.println("orderedPath: [");
        OrderedPathElement ope;
        for (int i=0; i<pathElements.size(); i++) {
            ope = (OrderedPathElement)pathElements.get(i);
            System.err.println("(" + ope.orderedGroup + "," + ope.childId);
        }
        System.err.println("]");
    }
}
