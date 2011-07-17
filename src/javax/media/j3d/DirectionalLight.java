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
import javax.vecmath.Vector3f;

/**
 * A DirectionalLight node defines an oriented light with an origin at
 * infinity. It has the same attributes as a Light node, with the
 * addition of a directional vector to specify the direction in which the
 * light shines. A directional light has parallel light rays that travel 
 * in one direction along the specified vector. Directional light contributes
 * to diffuse and specular reflections, which in turn depend on the
 * orientation of an object's surface but not its position. A directional
 * light does not contribute to ambient reflections.
 */

public class DirectionalLight extends Light {
 /**
  * Specifies that the Node allows access to its object's direction
  * information.
  */
  public static final int
    ALLOW_DIRECTION_READ = CapabilityBits.DIRECTIONAL_LIGHT_ALLOW_DIRECTION_READ;

  /**
   * Specifies that the Node allows writing to its object's direction
   * information.
   */
  public static final int
    ALLOW_DIRECTION_WRITE = CapabilityBits.DIRECTIONAL_LIGHT_ALLOW_DIRECTION_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_DIRECTION_READ
    };
    
    /**
     * Constructs a DirectionalLight node with default parameters.
     * The default values are as follows:
     * <ul>
     * direction : (0,0,-1)<br>
     * </ul>
     */
    public DirectionalLight() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a directional light.
     * @param color the color of the light source
     * @param direction the direction vector pointing from the light
     * to the object
     */
    public DirectionalLight(Color3f color, Vector3f direction) {
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((DirectionalLightRetained)this.retained).initDirection(direction);
    }

    /**
     * Constructs and initializes a directional light.
     * @param lightOn flag indicating whether this light is on or off
     * @param color the color of the light source
     * @param direction the direction vector pointing from the light
     * to the object
     */
    public DirectionalLight(boolean lightOn, Color3f color, Vector3f direction) {
	super(lightOn, color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((DirectionalLightRetained)this.retained).initDirection(direction);
    }

    /**
     * Creates the retained mode DirectionalLightRetained object that this
     * DirectionalLight component object will point to.
     */
    void createRetained() {
	this.retained = new DirectionalLightRetained();
	this.retained.setSource(this);
    }
  
  /**
   * Set light direction.
   * @param direction the new direction
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setDirection(Vector3f direction) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_WRITE))
	        throw new CapabilityNotSetException(
				   J3dI18N.getString("DirectionalLight0"));

	if (isLive())        
	    ((DirectionalLightRetained)this.retained).setDirection(direction);
	else
	    ((DirectionalLightRetained)this.retained).initDirection(direction);
    }

  /**
   * Set light direction.
   * @param x  the new X direction
   * @param y  the new Y direction
   * @param z  the new Z direction
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setDirection(float x, float y, float z) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_WRITE))
	        throw new CapabilityNotSetException(
				       J3dI18N.getString("DirectionalLight1"));

	if (isLive())
	    ((DirectionalLightRetained)this.retained).setDirection(x,y,z);
	else
	    ((DirectionalLightRetained)this.retained).initDirection(x,y,z);
  }

  /**
   * Gets this Light's current direction and places it in the parameter specified.
   * @param direction the vector that will receive this node's direction
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void getDirection(Vector3f direction) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_READ))
	        throw new CapabilityNotSetException(
				    J3dI18N.getString("DirectionalLight2"));

	((DirectionalLightRetained)this.retained).getDirection(direction);
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
        DirectionalLight d = new DirectionalLight();
        d.duplicateNode(this, forceDuplicate);
        return d;
    }


   /**
     * Copies all DirectionalLight information from
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
	
	Vector3f v = new Vector3f();
	((DirectionalLightRetained) originalNode.retained).getDirection(v);
	((DirectionalLightRetained) retained).initDirection(v);
    }
}
