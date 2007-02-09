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
