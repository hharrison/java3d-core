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
 * A class which implements GeneralizedStripFlags provides the means to access
 * the vertex replace code flags associated with each vertex of a generalized
 * strip.  This allows a flexible representation of generalized strips for
 * various classes and makes it possible to provide a common subset of static
 * methods which operate only on their topology.
 *
 * @see GeneralizedStrip
 * @see GeneralizedVertexList
 */
interface GeneralizedStripFlags {

    /**
     * This flag indicates that a vertex starts a new strip with clockwise
     * winding. 
     */
    static final int RESTART_CW = 0 ;

    /**
     * This flag indicates that a vertex starts a new strip with
     * counter-clockwise winding.
     */
    static final int RESTART_CCW = 1 ;

    /**
     * This flag indicates that the next triangle in the strip is defined by
     * replacing the middle vertex of the previous triangle in the strip.
     */
    static final int REPLACE_MIDDLE = 2 ;

    /**
     * This flag indicates that the next triangle in the strip is defined by
     * replacing the oldest vertex of the previous triangle in the strip.
     */
    static final int REPLACE_OLDEST = 3 ;

    /**
     * This constant is used to indicate that triangles with clockwise vertex
     * winding are front facing.
     */
    static final int FRONTFACE_CW  = 0 ;

    /**
     * This constant is used to indicate that triangles with counter-clockwise
     * vertex winding are front facing.
     */
    static final int FRONTFACE_CCW = 1 ;

    /**
     * Return the number of flags.  This should be the same as the number of
     * vertices in the generalized strip.
     */
    int getFlagCount() ;

    /**
     * Return the flag associated with the vertex at the specified index.
     */
    int getFlag(int index) ;
}
