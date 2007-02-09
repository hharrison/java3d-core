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

class OrderedPathElement extends Object {
    OrderedGroupRetained orderedGroup;
    Integer childId;

    OrderedPathElement(OrderedGroupRetained og, Integer orderedId) {
        orderedGroup = og;
        childId = orderedId;
    }
}
