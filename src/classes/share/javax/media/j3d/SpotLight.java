/*
 * $RCSfile$
 *
 * Copyright (c) 2004 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * The SpotLight object specifies an attenuated light source at a
 * fixed point in space that radiates light in a specified direction
 * from the light source.  A SpotLight has the same attributes as a
 * PointLight node, with the addition of the following:<P>
 * <UL>
 * <LI>Direction - The axis of the cone of light. The default
 * direction is (0.0, 0.0, -1.0). The spot light direction is
 * significant only when the spread angle is not PI radians
 * (which it is by default).</LI>
 * <P>
 * <LI>Spread angle - The angle in radians between the direction axis
 * and a ray along the edge of the cone. Note that the angle of the
 * cone at the apex is then twice this value. The range of values
 * is [0.0,PI/2] radians, with a special value of PI radians.  Values
 * lower than 0 are clamped to 0 and values over PI/2 are clamped
 * to PI. The default spread angle is PI radians. </LI>
 * <P>
 * <LI>Concentration - Specifies how quickly the light intensity 
 * attenuates as a function of the angle of radiation as measured from
 * the direction of radiation. The light's intensity is highest at the
 * center of the cone and is attenuated toward the edges of the cone
 * by the cosine of the angle between the direction of the light
 * and the direction from the light to the object being lit, raised
 * to the power of the spot concentration exponent.
 * The higher the concentration value, the more focused the light
 * source. The range of values is [0.0,128.0]. The default
 * concentration is 0.0, which provides uniform light 
 * distribution.</LI><P>
 * </UL>
 * A spot light contributes to diffuse and specular reflections, which
 * depend on the orientation and position of an object's surface.
 * A spot light does not contribute to ambient reflections.
 */

public class SpotLight extends PointLight {
  /**
   * Specifies that the Node allows writing to its spot lights spread angle
   * information.
   */
  public static final int
    ALLOW_SPREAD_ANGLE_WRITE = CapabilityBits.SPOT_LIGHT_ALLOW_SPREAD_ANGLE_WRITE;

  /**
   * Specifies that the Node allows reading its spot lights spread angle
   * information.
   */
  public static final int
    ALLOW_SPREAD_ANGLE_READ = CapabilityBits.SPOT_LIGHT_ALLOW_SPREAD_ANGLE_READ;

  /**
   * Specifies that the Node allows writing to its spot lights concentration
   * information.
   */
  public static final int
    ALLOW_CONCENTRATION_WRITE = CapabilityBits.SPOT_LIGHT_ALLOW_CONCENTRATION_WRITE;

  /**
   * Specifies that the Node allows reading its spot lights concentration
   * information.
   */
  public static final int
    ALLOW_CONCENTRATION_READ = CapabilityBits.SPOT_LIGHT_ALLOW_CONCENTRATION_READ;

  /**
   * Specifies that the Node allows writing to its spot lights direction
   * information.
   */
  public static final int
    ALLOW_DIRECTION_WRITE = CapabilityBits.SPOT_LIGHT_ALLOW_DIRECTION_WRITE;

  /**
   * Specifies that the Node allows reading its spot lights direction
   * information.
   */
  public static final int
    ALLOW_DIRECTION_READ = CapabilityBits.SPOT_LIGHT_ALLOW_DIRECTION_READ;

    /**
     * Constructs a SpotLight node with default parameters.
     * The default values are as follows:
     * <ul>
     * direction : (0,0,-1)<br>
     * spread angle : <i>PI</i> radians<br>
     * concentration : 0.0<br>
     * </ul>
     */
    public SpotLight() {
    }

    /**
     * Constructs and initializes a SpotLight node using the
     * specified parameters.
     * @param color the color of the light source
     * @param position the position of the light in three-space
     * @param attenuation the attenuation (constant, linear, quadratic)
     * of the light
     * @param direction the direction of the light
     * @param spreadAngle the spread angle of the light
     * @param concentration the concentration of the light
     */
    public SpotLight(Color3f color,
		     Point3f position,
		     Point3f attenuation, 
		     Vector3f direction,
		     float spreadAngle,
		     float concentration) {
	super(color, position, attenuation);
	((SpotLightRetained)this.retained).initDirection(direction);
	((SpotLightRetained)this.retained).initSpreadAngle(spreadAngle);
	((SpotLightRetained)this.retained).initConcentration(concentration);
    }

    /**
     * Constructs and initializes a SpotLight node using the
     * specified parameters.
     * @param lightOn flag indicating whether this light is on or off
     * @param color the color of the light source
     * @param position the position of the light in three-space
     * @param attenuation the attenuation (constant, linear, quadratic) of the light
     * @param direction the direction of the light
     * @param spreadAngle the spread angle of the light
     * @param concentration the concentration of the light
     */
    public SpotLight(boolean lightOn,
		     Color3f color,
		     Point3f position,
		     Point3f attenuation, 
		     Vector3f direction,
		     float spreadAngle,
		     float concentration) {
	super(lightOn, color, position, attenuation);
	((SpotLightRetained)this.retained).initDirection(direction);
	((SpotLightRetained)this.retained).initSpreadAngle(spreadAngle);
	((SpotLightRetained)this.retained).initConcentration(concentration);
    }

    /**
     * Creates the retained mode SpotLightRetained object that this
     * SpotLight component object will point to.
     */
    void createRetained() {
	this.retained = new SpotLightRetained();
	this.retained.setSource(this);
    }
  

    /**
     * Sets spot light spread angle.
     * @param spreadAngle the new spread angle for spot light
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph.
     */
    public void setSpreadAngle(float spreadAngle)	{
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SPREAD_ANGLE_WRITE))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight0"));

	if (isLive())
	    ((SpotLightRetained)this.retained).setSpreadAngle(spreadAngle);
	else
	    ((SpotLightRetained)this.retained).initSpreadAngle(spreadAngle);
    }

    /**
     * Gets spot light spread angle.
     * @return the new spread angle for spot light. The value returned
     * is the clamped value.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getSpreadAngle() {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_SPREAD_ANGLE_READ))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight1"));

	return ((SpotLightRetained)this.retained).getSpreadAngle();
    }

    /**
     * Sets spot light concentration.
     * @param concentration the new concentration for spot light
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setConcentration(float concentration) {
      if (isLiveOrCompiled())
	  if(!this.getCapability(ALLOW_CONCENTRATION_WRITE))
	      throw new CapabilityNotSetException(J3dI18N.getString("SpotLight2"));

      if (isLive())
	  ((SpotLightRetained)this.retained).setConcentration(concentration);
      else
	  ((SpotLightRetained)this.retained).initConcentration(concentration);
    }

    /**
     * Gets spot light concentration.
     * @return the new concentration for spot light
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getConcentration()  {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_CONCENTRATION_READ))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight3"));
	return ((SpotLightRetained)this.retained).getConcentration();
    }

    /**
     * Sets light direction.
     * @param x  the new X direction
     * @param y  the new Y direction
     * @param z  the new Z direction
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDirection(float x, float y, float z) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_WRITE))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight4"));

	if (isLive())
	    ((SpotLightRetained)this.retained).setDirection(x,y,z);
	else
	    ((SpotLightRetained)this.retained).initDirection(x,y,z);
    }

    /**
     * Sets this Light's current direction and places it in the parameter specified.
     * @param direction the vector that will receive this node's direction
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setDirection(Vector3f direction) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_WRITE))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight4"));

	if (isLive())
	    ((SpotLightRetained)this.retained).setDirection(direction);
	else
	    ((SpotLightRetained)this.retained).initDirection(direction);
    }

    /**
     * Gets this Light's current direction and places it in the
     * parameter specified.
     * @param direction the vector that will receive this node's direction
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void getDirection(Vector3f direction) {
	if (isLiveOrCompiled())
	    if(!this.getCapability(ALLOW_DIRECTION_READ))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("SpotLight6"));
	((SpotLightRetained)this.retained).getDirection(direction);
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
        SpotLight s = new SpotLight();
        s.duplicateNode(this, forceDuplicate);
        return s;
    }


   /**
     * Copies all SpotLight information from
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
    void duplicateAttributes(Node originalNode, boolean
			     forceDuplicate) {
	
        super.duplicateAttributes(originalNode, forceDuplicate);

	SpotLightRetained attr = (SpotLightRetained) originalNode.retained;
	SpotLightRetained rt = (SpotLightRetained) retained;

	rt.initSpreadAngle(attr.getSpreadAngle());
	rt.initConcentration(attr.getConcentration());
	Vector3f v = new Vector3f();
	attr.getDirection(v);
	rt.initDirection(v);
	
    }
}
