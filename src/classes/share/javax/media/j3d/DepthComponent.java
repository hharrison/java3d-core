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
 * Abstract base class that defines a 2D array of depth (Z) values.
 */

public abstract class DepthComponent extends NodeComponent {
    /**
     * Specifies that this DepthComponent object allows reading its
     * size component information (width and height).
     */
    public static final int
    ALLOW_SIZE_READ = CapabilityBits.DEPTH_COMPONENT_ALLOW_SIZE_READ;

    /**
     * Specifies that this DepthComponent object allows reading its
     * depth data component information.
     */
    public static final int
    ALLOW_DATA_READ = CapabilityBits.DEPTH_COMPONENT_ALLOW_DATA_READ;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_SIZE_READ,
        ALLOW_DATA_READ
    };

    /**
     * default constructor
     */
    DepthComponent() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Retrieves the width of this depth component object.
     * @return the width of the array of depth values
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getWidth() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_SIZE_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("DepthComponent0"));
	return ((DepthComponentRetained)this.retained).getWidth();
    }

    /**
     * Retrieves the height of this depth component object.
     * @return the height of the array of depth values
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getHeight() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_SIZE_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("DepthComponent0"));
	return ((DepthComponentRetained)this.retained).getHeight();
    }


}
