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
 * Indicates an illegal Java 3D scene graph.
 * For example, the following is illegal:
 * <ul>
 * <li>A ViewPlatform node under a ViewSpecificGroup</li>
 * </ul>
 *
 * @since Java 3D 1.3
 */

public class IllegalSceneGraphException extends RuntimeException {

    /**
     * Create the exception object with default values.
     */
    public IllegalSceneGraphException() {
    }

    /**
     * Create the exception object that outputs message.
     * @param str the message string to be output.
     */
    public IllegalSceneGraphException(String str) {
	super(str);
    }
}
