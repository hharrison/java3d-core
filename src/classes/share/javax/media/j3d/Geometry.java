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
     * Specifies that this Geometry allows intersect operation. 
     */
    public static final int
    ALLOW_INTERSECT = CapabilityBits.GEOMETRY_ALLOW_INTERSECT;

    /**
     * Constructs a new Geometry object.
     */
    public Geometry() {
    }
}
