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

/**
 * Defines a "not necessarily unique ID"
 */

interface NnuId {
    
    abstract int equal(NnuId obj);

    abstract int getId();
    
}
