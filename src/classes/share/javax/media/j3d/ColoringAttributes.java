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

import javax.vecmath.Color3f;

/**
 * The ColoringAttributes object defines attributes used in
 * color selection and shading model.
 *
 * <p>
 * <b>Color</b>
 * <p>
 * The <code>setColor</code> methods set the current intrinsic red, green, and
 * blue color values of this ColoringAttributes component object.
 * This color is only used for unlit geometry. If lighting is enabled, 
 * the material colors are used in the lighting equation to produce
 * the final color.  When vertex colors are present in unlit
 * geometry, those vertex colors are used in place of this
 * ColoringAttributes color, unless the vertex colors are ignored.
 * <p>
 * There are two variations on the <code>setColor</code> methods, one
 * that takes a Color3f and one that takes three floats. No alpha
 * value is allowed (it's automatically set to 1.0). The float values
 * range between 0.0 and 1.0, with 1.0 being full intensity of the
 * color. A color value of (1.0, 1.0, 1.0) is white.
 * <p>
 * <b>Shading Model</b>
 * <p>
 * The <code>setShadeModel</code> method sets the shade model for this 
 * ColoringAttributes component object. The shade model may be one of 
 * the following:<p>
 * <ul>
 * <li>FASTEST - use the fastest available method for shading. This
 * shading mode maps to whatever shading model the Java 3D implementor
 * defines as the "fastest," which may be hardware-dependent.</li>
 * <p>
 * <li>NICEST - use the nicest (highest quality) available method 
 * for shading. This shading mode maps to whatever shading model
 * the Java 3D implementor defines as the "nicest," shading
 * model, which may be hardware-dependent.</li>
 * <p>
 * <li>SHADE_FLAT -  use the flat shading model. This shading model
 * does not interpolate color across the primitive.
 * The primitive is drawn with a single color
 * and the color of one vertex of the primitive is duplicated 
 * across all the vertices of the primitive.</li>
 * <p>
 * <li>SHADE_GOURAUD - use the Gouraud (smooth) shading model.
 * This shading model smoothly interpolates the color at each vertex 
 * across the primitive.
 * The primitive is drawn with many different colors
 * and the color at each vertex is treated individually. For lines,
 * the colors along the line segment are interpolated between
 * the vertex colors. This is the default shade model if no other
 * is specified.</li>
 * <p></ul>
 *
 * @see Appearance
 */
public class ColoringAttributes extends NodeComponent {
    /**
     * Specifies that this ColoringAttributes object allows
     * reading its color component information.
     */
    public static final int
    ALLOW_COLOR_READ = CapabilityBits.COLORING_ATTRIBUTES_ALLOW_COLOR_READ;

    /**
     * Specifies that this ColoringAttributes object allows
     * writing its color component information.
     */
    public static final int
    ALLOW_COLOR_WRITE = CapabilityBits.COLORING_ATTRIBUTES_ALLOW_COLOR_WRITE;

    /**
     * Specifies that this ColoringAttributes object allows
     * reading its shade model component information.
     */
    public static final int
    ALLOW_SHADE_MODEL_READ = CapabilityBits.COLORING_ATTRIBUTES_ALLOW_SHADE_MODEL_READ;

    /**
     * Specifies that this ColoringAttributes object allows
     * writing its shade model component information.
     */
    public static final int
    ALLOW_SHADE_MODEL_WRITE = CapabilityBits.COLORING_ATTRIBUTES_ALLOW_SHADE_MODEL_WRITE;

    /**
     * Use the fastest available method for shading.
     */
    public static final int FASTEST            = 0;
    /**
     * Use the nicest available method for shading.
     */
    public static final int NICEST             = 1;

    /**
     * Do not interpolate color across the primitive.
     */
    public static final int SHADE_FLAT   = 2;
    /**
     * Smoothly interpolate the color at each vertex across the primitive.
     */
    public static final int SHADE_GOURAUD = 3;

    /**
     * Constructs a ColoringAttributes node with default parameters.
     * The default values are as follows:
     * <ul>
     * color = white (1,1,1)<br>
     * shade model = SHADE_GOURAUD<br>
     * </ul>
     */
    public ColoringAttributes() {
	// Just use default attributes
    }

    /**
     * Construct ColoringAttributes object with specified values.
     * @param color the intrisic color
     * @param shadeModel the shade model used; one of FASTEST, NICEST,
     * SHADE_FLAT, or SHADE_GOURAUD
     */
    public ColoringAttributes(Color3f color, int shadeModel) {
	((ColoringAttributesRetained)this.retained).initColor(color);
	((ColoringAttributesRetained)this.retained).initShadeModel(shadeModel);
  
    }

    /**
     * Construct ColoringAttributes object with specified values.
     * @param red red component of the intrisic color
     * @param green green component of the intrisic color
     * @param blue blue component of the intrisic color
     * @param shadeModel the shade model used; one of FASTEST, NICEST,
     * SHADE_FLAT, or SHADE_GOURAUD
     */
    public ColoringAttributes(float red, float green, float blue,
			      int shadeModel) {
	((ColoringAttributesRetained)this.retained).initColor(red, green,blue);
	((ColoringAttributesRetained)this.retained).initShadeModel(shadeModel);
    }

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object.  This color is only used for unlit geometry;
     * if lighting is enabled, then the material colors are used in the
     * lighting equation to produce the final color.
     * When vertex colors are present in unlit geometry, those
     * vertex colors are used in place of this ColoringAttributes color
     * unless the vertex colors are ignored.
     * @param color the color that is used when lighting is disabled
     * or when material is null
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @see Material
     * @see RenderingAttributes#setIgnoreVertexColors
     */
    public void setColor(Color3f color) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ColoringAttributes0"));

	if (isLive()) 
	    ((ColoringAttributesRetained)this.retained).setColor(color);
	else
	    ((ColoringAttributesRetained)this.retained).initColor(color);

    }

    /**
     * Sets the intrinsic color of this ColoringAttributes
     * component object.  This color is only used for unlit geometry;
     * if lighting is enabled, then the material colors are used in the
     * lighting equation to produce the final color.
     * When vertex colors are present in unlit geometry, those
     * vertex colors are used in place of this ColoringAttributes color
     * unless the vertex colors are ignored.
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @see Material
     * @see RenderingAttributes#setIgnoreVertexColors
     */
    public void setColor(float r, float g, float b) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ColoringAttributes0"));

	if (isLive())
	    ((ColoringAttributesRetained)this.retained).setColor(r, g, b);
	else
	    ((ColoringAttributesRetained)this.retained).initColor(r, g, b);
    }

    /**
     * Gets the intrinsic color of this ColoringAttributes
     * component object.
     * @param color the vector that will receive color
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void getColor(Color3f color) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLOR_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ColoringAttributes2"));

	((ColoringAttributesRetained)this.retained).getColor(color);
    }

    /**
     * Sets the shade mode for this ColoringAttributes component object.
     * @param shadeModel the shade mode to be used; one of FASTEST,
     * NICEST, SHADE_FLAT, or SHADE_GOURAUD
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setShadeModel(int shadeModel) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_SHADE_MODEL_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("ColoringAttributes3"));

	if (isLive()) 
	    ((ColoringAttributesRetained)this.retained).setShadeModel(shadeModel);
	else
	    ((ColoringAttributesRetained)this.retained).initShadeModel(shadeModel);
    }

    /**
     * Gets the shade mode for this ColoringAttributes component object.
     * @return shadeModel the shade mode
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getShadeModel() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_SHADE_MODEL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("ColoringAttributes4"));

	return ((ColoringAttributesRetained)this.retained).getShadeModel();
    }

    /**
     * Creates a retained mode ColoringAttributesRetained object that this
     * ColoringAttributes component object will point to.
     */
    void createRetained() {
	this.retained = new ColoringAttributesRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */

    public NodeComponent cloneNodeComponent() {
        ColoringAttributes ca = new ColoringAttributes();
        ca.duplicateNodeComponent(this);
        return ca;
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

	ColoringAttributesRetained attr = 
	    (ColoringAttributesRetained) originalNodeComponent.retained;
	
	ColoringAttributesRetained rt = (ColoringAttributesRetained) retained;
	Color3f c = new Color3f();
	attr.getColor(c);

	rt.initColor(c);
	rt.initShadeModel(attr.getShadeModel());
    }

    /**
     * Returns a String representation of this ColoringAttributes object.
     * If the scene graph is live only those values with their
     * Capability read bit set will be displayed.
     */
    public String toString() {
        StringBuffer str=new StringBuffer("ColoringAttributes:");
        String shadingModes[] = { "FASTEST", "NICEST", "SHADE_FLAT",
                                                     "SHADE_GOURAUD" };

        try {
            Color3f color=new Color3f();
            getColor( color );
            str.append( "Color="+color );
        }
        catch (CapabilityNotSetException e) {str.append("Color=N/A");}

        try {
            str.append( " ShadeModel="+shadingModes[getShadeModel()] );
        }
        catch (CapabilityNotSetException ex) {str.append("ShadeModel=N/A");}

        return new String(str);
    }

}
