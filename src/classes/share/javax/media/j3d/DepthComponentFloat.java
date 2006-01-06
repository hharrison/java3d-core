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
 * A 2D array of depth (Z) values in floating point format in the range [0,1].
 * A value of 0.0 indicates the closest Z value to the user while a value of
 * 1.0 indicates the farthest Z value.
 */

public class DepthComponentFloat extends DepthComponent {

    /**
     * Package scope defualt constructor used by cloneNodeComponent
     */
    DepthComponentFloat() {
    }

    /**
     * Constructs a new floating-point depth (z-buffer) component object with 
     * the specified width and height.
     * @param width the width of the array of depth values
     * @param height the height of the array of depth values
     */
    public DepthComponentFloat(int width, int height) {
	((DepthComponentFloatRetained)this.retained).initialize(width, height);
    }

    /**
     * Copies the specified depth data to this object.
     * @param depthData array of floats containing the depth data
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void setDepthData(float[] depthData) {
	checkForLiveOrCompiled();
	((DepthComponentFloatRetained)this.retained).setDepthData(depthData);
    }

    /**
     * Copies the depth data from this object to the specified array.
     * The array must be large enough to hold all of the floats. 
     * @param depthData array of floats that will receive a copy of
     * the depth data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDepthData(float[] depthData) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(DepthComponent.ALLOW_DATA_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("DepthComponentFloat0"));
	((DepthComponentFloatRetained)this.retained).getDepthData(depthData);
    }

    /**
     * Creates a retained mode DepthComponentFloatRetained object that this
     * DepthComponentFloat component object will point to.
     */
    void createRetained() {
	this.retained = new DepthComponentFloatRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)  
     */
    public NodeComponent cloneNodeComponent() {
	DepthComponentFloatRetained rt = (DepthComponentFloatRetained) retained;
	DepthComponentFloat d = new DepthComponentFloat(rt.width,
							rt.height);
        d.duplicateNodeComponent(this);
        return d;
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
	 super.duplicateAttributes(originalNodeComponent,
				   forceDuplicate);
	 // width, height is copied in cloneNode before
	 int len = getWidth()*getHeight();
	 float f[] = new float[len];

	 ((DepthComponentFloatRetained) originalNodeComponent.retained).getDepthData(f);
	 ((DepthComponentFloatRetained) retained).setDepthData(f);
    }


}
