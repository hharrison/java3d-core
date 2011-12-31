/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

/**
 * A 2D array of depth (Z) values in integer format.  Values are in the
 * range [0,(2**N)-1], where N is the pixel depth of the Z buffer.
 */

public class DepthComponentInt extends DepthComponent {

    /**
     * Package scope default constructor
     */
    DepthComponentInt() {
    }

    /**
     * Constructs a new integer depth (z-buffer) component object with the
     * specified width and height.
     * @param width the width of the array of depth values
     * @param height the height of the array of depth values
     */
    public DepthComponentInt(int width, int height) {
	((DepthComponentIntRetained)this.retained).initialize(width, height);
    }

    /**
     * Copies the specified depth data to this object.
     * @param depthData array of ints containing the depth data
     * @exception RestrictedAccessException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void setDepthData(int[] depthData) {
	checkForLiveOrCompiled();
	((DepthComponentIntRetained)this.retained).setDepthData(depthData);
    }

    /**
     * Copies the depth data from this object to the specified array.
     * The array must be large enough to hold all of the ints.
     * @param depthData array of ints that will receive a copy of
     * the depth data
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDepthData(int[] depthData) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(DepthComponent.ALLOW_DATA_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("DepthComponentInt0"));
	((DepthComponentIntRetained)this.retained).getDepthData(depthData);
    }

    /**
     * Creates a retained mode DepthComponentIntRetained object that this
     * DepthComponentInt component object will point to.
     */
    void createRetained() {
	this.retained = new DepthComponentIntRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
	DepthComponentIntRetained rt = (DepthComponentIntRetained) retained;
        DepthComponentInt d = new DepthComponentInt(rt.width,
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
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	// width, height is copied in cloneNode before
	int len = getWidth()*getHeight();
	int d[] = new int[len];
	((DepthComponentIntRetained) originalNodeComponent.retained).getDepthData(d);
	((DepthComponentIntRetained) retained).setDepthData(d);
    }


}
