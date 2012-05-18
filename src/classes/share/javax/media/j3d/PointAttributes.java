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
 * The PointAttributes object defines all attributes that apply to
 * point primitives. The point attributes that can be defined are:<p>
 * <ul>
 * <li>Size - the size of the point, in pixels. The default is a point
 * size of one pixel.</li><p>
 * <li>Antialiasing - for points greater than one-pixel in size,
 * antialiasing smooths the outline of the point when it is rendered.</li>
 * <p></ul>
 * If antialiasing is disabled (the default), fractional point sizes
 * are rounded to integer sizes, and a screen-aligned square region
 * of pixels is drawn.<p>
 * <p>
 * If antialiasing is enabled, the points are considered transparent
 * for rendering purposes.  They are rendered with all the other transparent
 * objects and adhere to the other transparency settings such as the
 * View transparency sorting policy and the View depth buffer freeze
 * transparent enable.
 * </p>
 *
 * @see Appearance
 * @see View
 */
public class PointAttributes extends NodeComponent {

    /**
     * Specifies that this PointAttributes object allows reading its
     * point size information.
     */
    public static final int
    ALLOW_SIZE_READ = CapabilityBits.POINT_ATTRIBUTES_ALLOW_SIZE_READ;

    /**
     * Specifies that this PointAttributes object allows writing its
     * point size information.
     */
    public static final int
    ALLOW_SIZE_WRITE = CapabilityBits.POINT_ATTRIBUTES_ALLOW_SIZE_WRITE;

    /**
     * Specifies that this PointAttributes object allows reading its
     * point antialiasing flag.
     */
    public static final int
    ALLOW_ANTIALIASING_READ = CapabilityBits.POINT_ATTRIBUTES_ALLOW_ANTIALIASING_READ;

    /**
     * Specifies that this PointAttributes object allows writing its
     * point antialiasing flag.
     */
    public static final int
    ALLOW_ANTIALIASING_WRITE = CapabilityBits.POINT_ATTRIBUTES_ALLOW_ANTIALIASING_WRITE;

       // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_SIZE_READ,
        ALLOW_ANTIALIASING_READ
    };

    /**
     * Constructs a PointAttributes object with default parameters.
     * The default values are as follows:
     * <ul>
     * point size : 1<br>
     * point antialiasing : false<br>
     * </ul>
     */
     public PointAttributes(){
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
      }

    /**
     * Constructs a PointAttributes object with specified values.
     * @param pointSize the size of points, in pixels
     * @param pointAntialiasing flag to set point antialising ON or OFF
     */
     public PointAttributes(float pointSize, boolean pointAntialiasing){
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((PointAttributesRetained)this.retained).initPointSize(pointSize);
       ((PointAttributesRetained)this.retained).initPointAntialiasingEnable(pointAntialiasing);
     }

    /**
     * Sets the point size for this appearance component object.
     * @param pointSize the size, in pixels, of point primitives
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPointSize(float pointSize) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("PointAttributes0"));

	if (isLive())
	    ((PointAttributesRetained)this.retained).setPointSize(pointSize);
	else
	    ((PointAttributesRetained)this.retained).initPointSize(pointSize);

    }

    /**
     * Gets the point size for this appearance component object.
     * @return the size, in pixels, of point primitives
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getPointSize() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_SIZE_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("PointAttributes1"));
	return ((PointAttributesRetained)this.retained).getPointSize();
    }

    /**
     * Enables or disables point antialiasing
     * for this appearance component object.
     * <p>
     * If antialiasing is enabled, the points are considered transparent
     * for rendering purposes.  They are rendered with all the other
     * transparent objects and adhere to the other transparency settings
     * such as the View transparency sorting policy and the View depth
     * buffer freeze transparent enable.
     * </p>
     * @param state true or false to enable or disable point antialiasing
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @see View
     */
    public void setPointAntialiasingEnable(boolean state) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANTIALIASING_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("PointAttributes2"));
	if (isLive())
	    ((PointAttributesRetained)this.retained).setPointAntialiasingEnable(state);
	else
	    ((PointAttributesRetained)this.retained).initPointAntialiasingEnable(state);

    }

    /**
     * Retrieves the state of the point antialiasing flag.
     * @return true if point antialiasing is enabled,
     * false if point antialiasing is disabled
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getPointAntialiasingEnable() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANTIALIASING_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("PointAttributes3"));
	return ((PointAttributesRetained)this.retained).getPointAntialiasingEnable();
    }

    /**
     * Creates a retained mode PointAttributesRetained object that this
     * PointAttributes component object will point to.
     */
    void createRetained() {
	this.retained = new PointAttributesRetained();
	this.retained.setSource(this);
    }

   /**
    * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
    */
    public NodeComponent cloneNodeComponent() {
        PointAttributes pa = new PointAttributes();
        pa.duplicateNodeComponent(this);
        return pa;
    }


     /**
     * Copies all node information from <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>duplicateNode</code> method. This routine does
     * the actual duplication of all "local data" (any data defined in
     * this object).
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	PointAttributesRetained attr = (PointAttributesRetained)
	                         originalNodeComponent.retained;
	PointAttributesRetained rt = (PointAttributesRetained) retained;

	rt.initPointSize(attr.getPointSize());
	rt.initPointAntialiasingEnable(attr.getPointAntialiasingEnable());
    }

}
