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

import javax.vecmath.Color3f;

/**
 * The LinearFog leaf node defines fog distance parameters for
 * linear fog.
 * LinearFog extends the Fog node by adding a pair of distance values,
 * in Z, at which the fog should start obscuring the scene and should maximally
 * obscure the scene.
 * <P>
 * The front and back fog distances are defined in the local coordinate system of
 * the node, but the actual fog equation will ideally take place in eye
 * coordinates.
 * <P>
 * The linear fog blending factor, <code>f</code>, is computed as follows:
 * <ul>
 * <code>f = (backDistance - z) / (backDistance - frontDistance)</code>
 * </ul>
 * where:
 * <ul>
 * <code>z</code> is the distance from the viewpoint.<br>
 * <code>frontDistance</code> is the distance at which fog starts obscuring objects.<br>
 * <code>backDistance</code> is the distance at which fog totally obscurs objects.
 * </ul>
 */
public class LinearFog extends Fog {
    /**
     * Specifies that this LinearFog node allows read access to its distance
     * information.
     */
    public static final int
    ALLOW_DISTANCE_READ = CapabilityBits.LINEAR_FOG_ALLOW_DISTANCE_READ;

    /**
     * Specifies that this LinearFog node allows write access to its distance
     * information.
     */
    public static final int
    ALLOW_DISTANCE_WRITE = CapabilityBits.LINEAR_FOG_ALLOW_DISTANCE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_DISTANCE_READ
    };

    /**
     * Constructs a LinearFog node with default parameters.
     * The default values are as follows:
     * <ul>
     * front distance : 0.1<br>
     * back distance : 1.0<br>
     * </ul>
     */
    public LinearFog() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a LinearFog node with the specified fog color.
     * @param color the fog color
     */
    public LinearFog(Color3f color) {
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a LinearFog node with the specified fog color and distances.
     * @param color the fog color
     * @param frontDistance the front   distance for the fog
     * @param backDistance the back   distance for the fog
     */
    public LinearFog(Color3f color, double frontDistance, double backDistance) {
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((LinearFogRetained)this.retained).initFrontDistance(frontDistance);
	((LinearFogRetained)this.retained).initBackDistance(backDistance);
    }

    /**
     * Constructs a LinearFog node with the specified fog color.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     */
    public LinearFog(float r, float g, float b) {
	super(r, g, b);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a LinearFog node with the specified fog color and distances.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     * @param frontDistance the front   distance for the fog
     * @param backDistance the back   distance for the fog
     */
    public LinearFog(float r, float g, float b,
		     double frontDistance, double backDistance) {
	super(r, g, b);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((LinearFogRetained)this.retained).initFrontDistance(frontDistance);
	((LinearFogRetained)this.retained).initBackDistance(backDistance);
    }

    /**
     * Sets front distance for fog.
     * @param frontDistance the distance at which fog starts obscuring objects
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setFrontDistance(double frontDistance) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DISTANCE_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("LinearFog0"));

	if (isLive())
	    ((LinearFogRetained)this.retained).setFrontDistance(frontDistance);
	else
	    ((LinearFogRetained)this.retained).initFrontDistance(frontDistance);

    }

    /**
     * Gets front distance for fog.
     * @return  the distance at which fog starts obscuring objects
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public double getFrontDistance() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DISTANCE_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("LinearFog1"));

	return ((LinearFogRetained)this.retained).getFrontDistance();
    }

    /**
     * Sets back distance for fog.
     * @param backDistance the distance at which fog totally obscurs objects
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setBackDistance(double backDistance) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DISTANCE_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("LinearFog0"));
	if (isLive())
	    ((LinearFogRetained)this.retained).setBackDistance(backDistance);
	else
	    ((LinearFogRetained)this.retained).initBackDistance(backDistance);

    }

    /**
     * Gets back distance for fog.
     * @return the distance at which fog totally obscurs objects
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public double getBackDistance() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DISTANCE_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("LinearFog1"));

	return ((LinearFogRetained)this.retained).getBackDistance();
    }

    /**
     * Creates the retained mode LinearFogRetained object that this
     * LinearFog node will point to.
     */
    void createRetained() {
        this.retained = new LinearFogRetained();
        this.retained.setSource(this);
    }

    /**
     * Used to create a new instance of the node.  This routine is called
     * by <code>cloneTree</code> to duplicate the current node.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @see Node#cloneTree
     * @see Node#cloneNode
     * @see Node#duplicateNode
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    public Node cloneNode(boolean forceDuplicate) {
        LinearFog lf = new LinearFog();
        lf.duplicateNode(this, forceDuplicate);
        return lf;
    }


   /**
     * Copies all LinearFog information from
     * <code>originalNode</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P> 
     *
     * @param originalNode the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#duplicateNode
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    void duplicateAttributes(Node originalNode, boolean forceDuplicate) {
        super.duplicateAttributes(originalNode, forceDuplicate);

	LinearFogRetained attr = (LinearFogRetained) originalNode.retained;
	LinearFogRetained rt = (LinearFogRetained) retained;

	rt.initFrontDistance(attr.getFrontDistance());
	rt.initBackDistance(attr.getBackDistance());
    }
}
