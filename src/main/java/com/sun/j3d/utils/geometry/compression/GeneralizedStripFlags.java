/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.geometry.compression;

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
