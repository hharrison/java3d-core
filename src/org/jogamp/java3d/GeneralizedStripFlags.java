/*
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

package org.jogamp.java3d;

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
