/*
 * Copyright 1997-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

package javax.media.j3d;

/**
 * Geometry is an abstract class that specifies the geometry
 * component information required by a Shape3D node. Geometry objects
 * describe both the geometry and topology of the Shape3D nodes that
 * reference them. Geometry objects consist of four generic geometric
 * types:<P>
 * <UL><LI>Compressed Geometry</LI>
 * <LI>GeometryArray</LI>
 * <LI>Raster</LI>
 * <LI>Text3D</LI>
 * </UL><P>
 * Each of these geometric types defines a visible object or set of
 * objects. A Geometry object is used as a component object of a Shape3D
 * leaf node.
 *
 */

public abstract class Geometry extends NodeComponent {

    /**
     * Specifies that this Geometry allows intersect operation. This
     * capability bit is set (true) by default for all Geometry objects.
     */
    public static final int
    ALLOW_INTERSECT = CapabilityBits.GEOMETRY_ALLOW_INTERSECT;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_INTERSECT
    };

    /**
     * Constructs a new Geometry object.
     */
    public Geometry() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }
}
