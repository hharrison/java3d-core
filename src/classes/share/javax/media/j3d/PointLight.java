/*
 * $RCSfile$
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Point3f;
import javax.vecmath.Color3f;


/**
 * The PointLight object specifies an attenuated light source at a
 * fixed point in space that radiates light equally in all directions
 * away from the light source. PointLight has the same attributes as
 * a Light node, with the addition of location and attenuation
 * parameters.
 * <p>
 * A point light contributes to diffuse and specular reflections,
 * which in turn depend on the orientation and position of a
 * surface. A point light does not contribute to ambient reflections.
 * <p>
 * A PointLight is attenuated by multiplying the contribution of the
 * light by an attenuation factor. The attenuation factor causes the
 * the PointLight's brightness to decrease as distance from the light
 * source increases.
 * A PointLight's attenuation factor contains three values:
 * <P><UL>
 * <LI>Constant attenuation</LI>
 * <LI>Linear attenuation</LI>
 * <LI>Quadratic attenuation</LI></UL>
 * <p>
 * A PointLight is attenuated by the reciprocal of the sum of:
 * <p>
 * <ul>
 * The constant attenuation factor<br>
 * The Linear attenuation factor times the distance between the light
 * and the vertex being illuminated<br>
 * The quadratic attenuation factor times the square of the distance
 * between the light and the vertex
 * </ul>
 * <p>
 * By default, the constant attenuation value is 1 and the other
 * two values are 0, resulting in no attenuation.
 */

public class PointLight extends Light {
 /**
  * Specifies that this PointLight node allows reading its position
  * information.
  */
  public static final int
    ALLOW_POSITION_READ = CapabilityBits.POINT_LIGHT_ALLOW_POSITION_READ;

  /**
   * Specifies that this PointLight node allows writing its position
   * information.
   */
  public static final int
    ALLOW_POSITION_WRITE = CapabilityBits.POINT_LIGHT_ALLOW_POSITION_WRITE;

 /**
  * Specifies that this PointLight node allows reading its attenuation
  * information.
  */
  public static final int
    ALLOW_ATTENUATION_READ = CapabilityBits.POINT_LIGHT_ALLOW_ATTENUATION_READ;

  /**
   * Specifies that this PointLight node allows writing its attenuation
   * information.
   */
  public static final int
    ALLOW_ATTENUATION_WRITE = CapabilityBits.POINT_LIGHT_ALLOW_ATTENUATION_WRITE;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_POSITION_READ,
	ALLOW_ATTENUATION_READ
    };

    /**
     * Constructs a PointLight node with default parameters.
     * The default values are as follows:
     * <ul>
     * position : (0,0,0)<br>
     * attenuation : (1,0,0)<br>
     * </ul>
     */
    public PointLight() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs and initializes a point light.
     * @param color the color of the light source
     * @param position the position of the light in three-space
     * @param attenuation the attenutation (constant, linear, quadratic) of the light
     */
    public PointLight(Color3f color,
		      Point3f position,
		      Point3f attenuation) { 
	super(color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((PointLightRetained)this.retained).initPosition(position);
	((PointLightRetained)this.retained).initAttenuation(attenuation);
    }

    /**
     * Constructs and initializes a point light.
     * @param lightOn flag indicating whether this light is on or off
     * @param color the color of the light source
     * @param position the position of the light in three-space
     * @param attenuation the attenuation (constant, linear, quadratic) of the light
     */
    public PointLight(boolean lightOn,
		      Color3f color,
		      Point3f position,
		      Point3f attenuation) { 
	super(lightOn, color);

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((PointLightRetained)this.retained).initPosition(position);
	((PointLightRetained)this.retained).initAttenuation(attenuation);
    }

    /**
     * Creates the retained mode PointLightRetained object that this
     * PointLight component object will point to.
     */
    void createRetained() {
	this.retained = new PointLightRetained();
	this.retained.setSource(this);
    }
  

  /**
   * Set light position.
   * @param position the new position
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setPosition(Point3f position) {
      if (isLiveOrCompiled())
	  if(!this.getCapability(ALLOW_POSITION_WRITE))
	      throw new CapabilityNotSetException(J3dI18N.getString("PointLight0"));

      if (isLive())
	  ((PointLightRetained)this.retained).setPosition(position);
      else
	  ((PointLightRetained)this.retained).initPosition(position);
   }

  /**
   * Set light position.
   * @param x  the new X position
   * @param y  the new Y position
   * @param z  the new Z position
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setPosition(float x, float y, float z) {
      if (isLiveOrCompiled())
	  if(!this.getCapability(ALLOW_POSITION_WRITE))
	      throw new CapabilityNotSetException(J3dI18N.getString("PointLight1"));

      if (isLive())
	  ((PointLightRetained)this.retained).setPosition(x,y,z);
      else
	  ((PointLightRetained)this.retained).initPosition(x,y,z);
    }

  /**
   * Gets this Light's current position and places it in the parameter specified.
   * @param position the vector that will receive this node's position
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void getPosition(Point3f position) {
        if (isLiveOrCompiled())
 	    if(!this.getCapability(ALLOW_POSITION_READ))
	        throw new CapabilityNotSetException(J3dI18N.getString("PointLight2"));

	((PointLightRetained)this.retained).getPosition(position);
    }

  /**
   * Sets this Light's current attenuation values and places it in the parameter specified.
   * @param attenuation the vector that will receive the attenuation values
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setAttenuation(Point3f attenuation) {
      if (isLiveOrCompiled())
	  if(!this.getCapability(ALLOW_ATTENUATION_WRITE))
	      throw new CapabilityNotSetException(J3dI18N.getString("PointLight3"));

      if (isLive())
	  ((PointLightRetained)this.retained).setAttenuation(attenuation);
      else
	  ((PointLightRetained)this.retained).initAttenuation(attenuation);
    }

  /**
   * Sets this Light's current attenuation values and places it in the parameter specified.
   * @param constant the light's constant attenuation 
   * @param linear the light's linear attenuation 
   * @param quadratic the light's quadratic attenuation 
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void setAttenuation(float constant, float linear, float quadratic) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_ATTENUATION_WRITE))
	        throw new CapabilityNotSetException(J3dI18N.getString("PointLight3"));

	if (isLive())
	    ((PointLightRetained)this.retained).setAttenuation(constant, linear, quadratic);
	else
	    ((PointLightRetained)this.retained).initAttenuation(constant, linear, quadratic);
  }

  /**
   * Gets this Light's current attenuation values and places it in the parameter specified.
   * @param attenuation the vector that will receive the attenuation values
   * @exception CapabilityNotSetException if appropriate capability is
   * not set and this object is part of live or compiled scene graph
   */
    public void getAttenuation(Point3f attenuation) {
        if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_ATTENUATION_READ))
	        throw new CapabilityNotSetException(J3dI18N.getString("PointLight5"));

	((PointLightRetained)this.retained).getAttenuation(attenuation);
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
        PointLight p = new PointLight();
        p.duplicateNode(this, forceDuplicate);
        return p;
    }


   /**
     * Copies all PointLight information from
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

	PointLightRetained attr = (PointLightRetained) originalNode.retained;
	PointLightRetained rt = (PointLightRetained) retained;

	Point3f p = new Point3f();
	attr.getPosition(p);
	rt.initPosition(p);
	
	attr.getAttenuation(p);
	rt.initAttenuation(p);
    }
}
