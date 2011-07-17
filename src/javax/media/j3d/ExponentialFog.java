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

import javax.vecmath.Color3f;

/**
 * The ExponentialFog leaf node extends the Fog leaf node by adding a
 * fog density that is used as the exponent of the fog equation. The
 * density is defined in the local coordinate system of the node, but
 * the actual fog equation will ideally take place in eye coordinates.
 * <P>
 * The fog blending factor, f, is computed as follows:
 * <P><UL>
 * f = e<sup>-(density * z)</sup><P>
 * where
 * <ul>z is the distance from the viewpoint.<br>
 * density is the density of the fog.<P></ul></UL>
 * 
 * In addition to specifying the fog density, ExponentialFog lets you
 * specify the fog color, which is represented by R, G, and B
 * color values, where a color of (0,0,0) represents black.
 */
public class ExponentialFog extends Fog {
    /**
     * Specifies that this ExponentialFog node allows read access to its
     * density information.
     */
    public static final int
    ALLOW_DENSITY_READ = CapabilityBits.EXPONENTIAL_FOG_ALLOW_DENSITY_READ;

    /**
     * Specifies that this ExponentialFog node allows write access to its
     * density information.
     */
    public static final int
    ALLOW_DENSITY_WRITE = CapabilityBits.EXPONENTIAL_FOG_ALLOW_DENSITY_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_DENSITY_READ
    };

    /**
     * Constructs an ExponentialFog node with default parameters.
     * The default values are as follows:
     * <ul>
     * density : 1.0<br>
     * </ul>
     */
    public ExponentialFog() {
	// Just use the defaults
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs an ExponentialFog node with the specified fog color.
     * @param color the fog color
     */
    public ExponentialFog(Color3f color) {
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs an ExponentialFog node with the specified fog color
     * and density.
     * @param color the fog color
     * @param density the density of the fog
     */
    public ExponentialFog(Color3f color, float density) {
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((ExponentialFogRetained)this.retained).initDensity(density);
    }

    /**
     * Constructs an ExponentialFog node with the specified fog color.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     */
    public ExponentialFog(float r, float g, float b) {
	super(r, g, b);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs an ExponentialFog node with the specified fog color
     * and density.
     * @param r the red component of the fog color
     * @param g the green component of the fog color
     * @param b the blue component of the fog color
     * @param density the density of the fog
     */
    public ExponentialFog(float r, float g, float b, float density) {
	super(r, g, b);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((ExponentialFogRetained)this.retained).initDensity(density);
    }

    /**
     * Sets fog density.
     * @param density the new density of this fog
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDensity(float density) {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DENSITY_WRITE))
	    	throw new CapabilityNotSetException(J3dI18N.getString("ExponentialFog0"));

	if (isLive())
	    ((ExponentialFogRetained)this.retained).setDensity(density);
	else
	    ((ExponentialFogRetained)this.retained).initDensity(density);
    }

    /**
     * Gets fog density.
     * @return the density of this fog
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getDensity() {
    	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DENSITY_READ))
	    	throw new CapabilityNotSetException(J3dI18N.getString("ExponentialFog1"));

	return ((ExponentialFogRetained)this.retained).getDensity();
    }

    /**
     * Creates the retained mode ExponentialFogRetained object that this
     * ExponentialFog node will point to.
     */
    void createRetained() {
        this.retained = new ExponentialFogRetained();
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
        ExponentialFog ef = new ExponentialFog();
        ef.duplicateNode(this, forceDuplicate);
        return ef;
    }


   /**
     * Copies all ExponentialFog information from
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

	((ExponentialFogRetained) retained).initDensity(
           ((ExponentialFogRetained) originalNode.retained).getDensity());
    }
}
