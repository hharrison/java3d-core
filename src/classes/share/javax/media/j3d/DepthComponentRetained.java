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
 * Abstract base class that defines a 2D array of depth (Z) values.
 */


abstract class DepthComponentRetained extends NodeComponentRetained {
    // depth component types
    static final int DEPTH_COMPONENT_TYPE_INT   = 1;
    static final int DEPTH_COMPONENT_TYPE_FLOAT = 2;
    static final int DEPTH_COMPONENT_TYPE_NATIVE = DEPTH_COMPONENT_TYPE_INT;


    // Width and height of DepthComponent---set by subclasses
    int		width;
    int		height;
    int		type;

    /**
     * Retrieves the width of this depth component object
     * @return the width of the array of depth values
     */
    int getWidth() {
	return width;
    }

    /**
     * Retrieves the height of this depth component object
     * @return the height of the array of depth values
     */
    int getHeight() {
	return height;
    }

}
