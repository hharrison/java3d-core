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

import javax.vecmath.Color4f;

/**
 * The TextureAttributes object defines attributes that apply to
 * texture mapping.
 * The texture attributes include the following:<P>
 * <UL>
 * <LI>Texture mode - defines how the object and texture colors
 * are blended. The mode may be one of the following:</LI><P>
 * <UL>
 * <LI>MODULATE - modulates the incoming color with the texture
 * color.<P>
 * <UL>
 * C' = C Ct
 * </UL></LI><P>
 * <LI>DECAL - applies the texture color to the incoming color as a decal.<P>
 * <UL>
 * C'<sub>rgb</sub> = C<sub>rgb</sub> (1 - Ct<sub>a</sub>) + Ct<sub>rgb</sub> Ct<sub>a</sub><P>
 * C'<sub>a</sub> = C<sub>a</sub>
 * </UL></LI><P>
 * <LI>BLEND - blends the texture blend color with the incoming color.<P>
 * <UL>
 * C'<sub>rgb</sub> = C<sub>rgb</sub> (1 - Ct<sub>rgb</sub>) + Cb<sub>rgb</sub> Ct<sub>rgb</sub><P>
 * C'<sub>a</sub> = C<sub>a</sub> Ct<sub>a</sub><P>
 * </UL>
 * Note that if the texture format is INTENSITY, alpha is computed identically
 * to red, green, and blue: <P>
 * <UL>
 * C'<sub>a</sub> = C<sub>a</sub> (1 - Ct<sub>a</sub>) + Cb<sub>a</sub> Ct<sub>a</sub>
 * </UL></LI><P>
 * <LI>REPLACE - replaces the incoming color with the texture color.<P>
 * <UL>
 * C' = Ct <P>
 * </UL></LI><P>
 * <LI>COMBINE - combines the object color with the texture color or texture
 * blend color according to the combine operation as specified in the
 * texture combine mode. </LI><P>
 * <p>
 * </UL>
 * C  = Incoming color to the texture unit state. For texture unit state 0, C is the object color
 * Ct = Texture color<br>
 * Cb = Texture blend color<br>
 * <p>
 * <LI>Combine Mode - defines the combine operation when texture mode
 * specifies COMBINE. The combine mode includes the following:<p>
 * <UL>
 * <LI>COMBINE_REPLACE<P> 
 * <UL>
 * C' = C<sub>0</sub> <P>
 * </UL></LI><P>
 * <LI>COMBINE_MODULATE<P>
 * <UL>
 * C' = C<sub>0</sub> C<sub>1</sub>
 * </UL></LI><P>
 * <LI>COMBINE_ADD<P>
 * <UL>
 * C' = C<sub>0</sub> + C<sub>1</sub> <P>
 * </UL></LI><P>
 * <LI>COMBINE_ADD_SIGNED <P>
 * <UL>
 * C' = C<sub>0</sub> + C<sub>1</sub> - 0.5 <P>
 * </UL></LI><P>
 * <LI>COMBINE_SUBTRACT <P>
 * <UL>
 * C' = C<sub>0</sub> - C<sub>1</sub> <P>
 * </UL></LI><P>
 * <LI>COMBINE_INTERPOLATE<P> 
 * <UL>
 * C' = C<sub>0</sub> C<sub>2</sub> + C<sub>1</sub> (1 - C<sub>2</sub>) <P>
 * </UL></LI><P>
 * <LI>COMBINE_DOT3<P>
 * <UL>
 * C' = 4 * (
 * (C<sub>0<sub>r</sub></sub> - 0.5) * (C<sub>1<sub>r</sub></sub> - 0.5) + 
 * (C<sub>0<sub>g</sub></sub> - 0.5) * (C<sub>1<sub>g</sub></sub> - 0.5) + 
 * (C<sub>0<sub>b</sub></sub> - 0.5) * (C<sub>1<sub>b</sub></sub> - 0.5))<P>
 * where C<sub>N<sub>x</sub></sub> is the x component of the Nth color operand
 * in the combine operation.<P>
 * The value C' will be placed to the all three r,g,b components or the 
 * a component of the output.
 * </UL></LI><P>
 * </UL></LI><P>
 * where C<sub>0</sub>, C<sub>1</sub> and C<sub>2</sub> are determined by
 * the color source, and the color operand.
 * </UL></LI><P>
 * <UL>
 * <LI>Combine Color Source - defines the source for a color operand in the
 * combine operation. The color source includes the following:<p>
 * <UL>
 * <LI> COMBINE_OBJECT_COLOR  - object color<P>
 * <LI> COMBINE_TEXTURE_COLOR  - texture color<P>
 * <LI> COMBINE_CONSTANT_COLOR - texture blend color<P>
 * <LI> COMBINE_PREVIOUS_TEXTURE_UNIT_STATE - color from the previous texture
 * unit state. For texture unit state 0, this is equivalent to 
 * COMBINE_OBJECT_COLOR.<P>
 * </UL></LI><P>
 * <LI>Combine Color Function - specifies the function for a color operand
 * in the combine operation. The valid values are:<P>
 * <UL>
 * <LI>COMBINE_SRC_COLOR - the color function is f = C<sub>rgb</sub><P>
 * <LI>COMBINE_ONE_MINUS_SRC_COLOR - the color function is f = (1 - C<sub>rgb</sub>)<P>
 * <LI>COMBINE_SRC_ALPHA - the color function is f = C<sub>a</sub><P>
 * <LI>COMBINE_ONE_MINUS_SRC_ALPHA - the color function is f = (1 - C<sub>a</sub>)<P>
 * </UL></LI><P>
 * <LI>Combine scale factor - specifies the scale factor to be applied to
 * the output color of the combine operation. The valid values include:
 * 1, 2, or 4.</LI><P>
 * <LI>Transform - the texture transform object used to transform
 * texture coordinates. The texture transform can translate, scale,
 * or rotate the texture coordinates before the texture is applied
 * to the object.</LI><P>
 * <LI>Blend color - the constant texture blend color</LI><P>
 * <LI>Perspective correction - the perspective correction mode
 * used for color and texture coordinate interpolation. One of
 * the following:</LI><P>
 * <UL>
 * <LI>NICEST - uses the nicest (highest quality) available
 * method for texture mapping perspective correction.</LI><P>
 * <LI>FASTEST - uses the fastest available method for texture
 * mapping perspective correction.</LI><P>
 * </UL>
 * <LI>Texture color table - defines a table that is used to look up
 * texture colors before applying the texture mode.</LI>
 * </UL>
 *
 * @see Appearance
 * @see Canvas3D#queryProperties
 */
public class TextureAttributes extends NodeComponent {
    /**
     * Specifies that this TextureAttributes object allows
     * reading its texture mode component
     * information and perspective correction mode.
     */
    public static final int
    ALLOW_MODE_READ = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_MODE_READ;

    /**
     * Specifies that this TextureAttributes object allows
     * writing its texture mode component
     * information and perspective correction mode.
     */
    public static final int
    ALLOW_MODE_WRITE = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_MODE_WRITE;

    /**
     * Specifies that this TextureAttributes object allows
     * reading its texture blend color component
     * information.
     */
    public static final int
    ALLOW_BLEND_COLOR_READ = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_BLEND_COLOR_READ;

    /**
     * Specifies that this TextureAttributes object allows
     * writing its texture blend color component
     * information.
     */
    public static final int
    ALLOW_BLEND_COLOR_WRITE = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_BLEND_COLOR_WRITE;

    /**
     * Specifies that this TextureAttributes object allows
     * reading its texture transform component
     * information.
     */
    public static final int
    ALLOW_TRANSFORM_READ = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_TRANSFORM_READ;

    /**
     * Specifies that this TextureAttributes object allows
     * writing its texture transform component
     * information.
     */
    public static final int
    ALLOW_TRANSFORM_WRITE = CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_TRANSFORM_WRITE;

    /**
     * Specifies that this TextureAttributes object allows
     * reading its texture color table component
     * information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_COLOR_TABLE_READ =
	CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_COLOR_TABLE_READ;

    /**
     * Specifies that this TextureAttributes object allows
     * writing its texture color table component
     * information.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_COLOR_TABLE_WRITE =
	CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_COLOR_TABLE_WRITE;

    /**
     * Specifies that this TextureAttributes object allows
     * reading its texture combine mode information. (e.g. combine mode,
     * combine color source, combine color function, combine scale factor)
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_COMBINE_READ =
	CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_COMBINE_READ;

    /**
     * Specifies that this TextureAttributes object allows
     * writing its texture combine mode information. (e.g. combine mode,
     * combine color source, combine color function, combine scale factor)
     *
     * @since Java 3D 1.3
     */
    public static final int ALLOW_COMBINE_WRITE =
	CapabilityBits.TEXTURE_ATTRIBUTES_ALLOW_COMBINE_WRITE;


    /**
     * Use the fastest available method for perspective correction.
     * @see #setPerspectiveCorrectionMode
     */
    public static final int FASTEST            = 0;

    /**
     * Use the nicest (highest quality) available method for texture
     * mapping perspective correction.
     * @see #setPerspectiveCorrectionMode
     */
    public static final int NICEST             = 1;

    /**
     * Modulate the object color with the texture color.
     * @see #setTextureMode
     */
    public static final int MODULATE = 2;

    /**
     * Apply the texture color to the object as a decal.
     * @see #setTextureMode
     */
    public static final int DECAL    = 3;

    /**
     * Blend the texture blend color with the object color.
     * @see #setTextureMode
     */
    public static final int BLEND    = 4;

    /**
     * Replace the object color with the texture color.
     * @see #setTextureMode
     */
    public static final int REPLACE  = 5;
  
    /**
     * Combine the object color with texture color as specified in 
     * the combine mode.
     *
     * @see #setTextureMode
     * @since Java 3D 1.3
     */
    public static final int COMBINE  = 6;
    

    /**
     * Replace the input color with the specified color.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_REPLACE     = 0;

    /**
     * Modulates one color with another color.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_MODULATE    = 1;

    /**
     * Add two colors.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_ADD         = 2;

    /**
     * Add two colors plus an implicit offset.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_ADD_SIGNED  = 3;

    /**
     * Subtract one color from another color.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_SUBTRACT    = 4;

    /**
     * Interpolate two colors with a factor.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_INTERPOLATE = 5;

    /**
     * Dot product of two colors.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbMode
     * @see #setCombineAlphaMode
     */
    public static final int COMBINE_DOT3        = 6;

   
    /**
     * Object color coming into the texturing state.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbSource
     * @see #setCombineAlphaSource
     */
    public static final int COMBINE_OBJECT_COLOR	= 0;

    /**
     * Texture color of the corresponding texture unit state.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbSource
     * @see #setCombineAlphaSource
     */
    public static final int COMBINE_TEXTURE_COLOR	= 1;

    /**
     * Texture blend color. 
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbSource
     * @see #setCombineAlphaSource
     */
    public static final int COMBINE_CONSTANT_COLOR	= 2;

    /**
     * Color from the previous texture unit state.
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbSource
     * @see #setCombineAlphaSource
     */
    public static final int COMBINE_PREVIOUS_TEXTURE_UNIT_STATE	= 3;

    /** 
     * Color function is f = C<sub>rgb</sub>
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbFunction
     */
    public static final int COMBINE_SRC_COLOR		= 0;

    /** 
     * Color function is f = (1 - C<sub>rgb</sub>)
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbFunction
     */
    public static final int COMBINE_ONE_MINUS_SRC_COLOR	= 1;

    /** 
     * Color function is f = C<sub>a</sub>
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbFunction
     * @see #setCombineAlphaFunction
     */
    public static final int COMBINE_SRC_ALPHA		= 2;

    /** 
     * Color function is f = (1 - C<sub>a</sub>)
     *
     * @since Java 3D 1.3
     * @see #setCombineRgbFunction
     * @see #setCombineAlphaFunction
     */
    public static final int COMBINE_ONE_MINUS_SRC_ALPHA	= 3;

    /**
     * Constructs a TextureAttributes object with default parameters.
     * The default values are as follows:
     * <ul>
     * texture mode : REPLACE<br>
     * blend color : black (0,0,0,0)<br>
     * transform : identity<br>
     * perspective correction mode : NICEST<br>
     * texture color table : null<br>
     * combine rgb mode : COMBINE_MODULATE<br>
     * combine alpha mode : COMBINE_MODULATE<br>
     * combine rgb source : 
     * <ul>
     * 		C<sub>0</sub>=COMBINE_TEXTURE_COLOR<br>
     *          C<sub>1</sub>=COMBINE_PREVIOUS_TEXTURE_UNIT_STATE<br>
     *          C<sub>2</sub>=COMBINE_CONSTANT_COLOR<br>
     * </ul>
     * combine alpha source : 
     * <ul>
     * 		C<sub>0</sub>=COMBINE_TEXTURE_COLOR<br>
     *          C<sub>1</sub>=COMBINE_PREVIOUS_TEXTURE_UNIT_STATE<br>
     *          C<sub>2</sub>=COMBINE_CONSTANT_COLOR<br>
     * </ul>
     * combine rgb function : COMBINE_SRC_COLOR<br>
     * combine alpha function : COMBINE_SRC_ALPHA<br> 
     * combine rgb scale : 1<br>
     * combine alpha scale : 1<br>
     * </ul>
     */
    public TextureAttributes()  {
    }
  
    /**
     * Constructs a TextureAttributes object with the specified values.
     * @param textureMode the texture mode; one of <code>MODULATE</code>, 
     * <code>DECAL</code>, <code>BLEND</code>, <code>REPLACE</code>, or
     * <code>COMBINE</code>
     * @param transform the transform object, used to transform texture
     * coordinates
     * @param textureBlendColor the texture constant color
     * @param perspCorrectionMode the perspective correction mode to 
     * be used for color and/or texture coordinate interpolation;
     * one of <code>NICEST</code> or <code>FASTEST</code>
     * @exception IllegalArgumentException if <code>textureMode</code>
     * is a value other than <code>MODULATE</code>,
     * <code>DECAL</code>, <code>BLEND</code>, <code>REPLACE</code>, or
     * <code>COMBINE</code>
     * @exception IllegalArgumentException if mode value is other
     * than <code>FASTEST</code> or <code>NICEST</code>.
     */
    public TextureAttributes(int textureMode, Transform3D transform,
			     Color4f textureBlendColor,
			     int perspCorrectionMode) {

	if ((textureMode < MODULATE) || (textureMode > COMBINE)) {
	    throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes10"));
	}

	if ((perspCorrectionMode != FASTEST) && 
	    (perspCorrectionMode!= NICEST)) {
	    throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes9"));
	}

	((TextureAttributesRetained)this.retained).initTextureMode(textureMode);
	((TextureAttributesRetained)this.retained).initTextureBlendColor(textureBlendColor);
	((TextureAttributesRetained)this.retained).initTextureTransform(transform);
	((TextureAttributesRetained)this.retained).initPerspectiveCorrectionMode(perspCorrectionMode);
    }
  
    /**
     * Sets the texture  mode parameter for this
     * appearance component object.
     * @param textureMode the texture  mode, one of: <code>MODULATE</code>,
     * <code>DECAL</code>, <code>BLEND</code>, <code>REPLACE</code>, or
     * <code>COMBINE</code>
     * @exception IllegalArgumentException if <code>textureMode</code>
     * is a value other than <code>MODULATE</code>,
     * <code>DECAL</code>, <code>BLEND</code>, <code>REPLACE</code>, or
     * <code>COMBINE</code>
     *
     * @see Canvas3D#queryProperties
     */
    public void setTextureMode(int textureMode) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_MODE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes0"));

	if ((textureMode < MODULATE) || (textureMode > COMBINE)) {
	    throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes10"));
	}

	if (isLive())
	    ((TextureAttributesRetained)this.retained).setTextureMode(textureMode);
	else
	    ((TextureAttributesRetained)this.retained).initTextureMode(textureMode);
    }

    /**
     * Gets the texture  mode parameter for this
     * texture attributes object.
     * @return textureMode the texture  mode
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getTextureMode() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_MODE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes1"));

	return ((TextureAttributesRetained)this.retained).getTextureMode();
    }

    /**
     * Sets the texture constant color for this
     * texture attributes object.
     * @param textureBlendColor the texture constant color
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setTextureBlendColor(Color4f textureBlendColor) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_COLOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes2"));

	if (isLive())
	    ((TextureAttributesRetained)this.retained).setTextureBlendColor(textureBlendColor);
	else
	    ((TextureAttributesRetained)this.retained).initTextureBlendColor(textureBlendColor);
    }

    /**
     * Sets the texture blend color for this
     * appearance component object.  
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     * @param a the alpha component of the color
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void setTextureBlendColor(float r, float g, float b, float a) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_COLOR_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes3"));

	if (isLive())
	    ((TextureAttributesRetained)this.retained).setTextureBlendColor(r, g, b, a);
	else
	    ((TextureAttributesRetained)this.retained).initTextureBlendColor(r, g, b, a);
    }

    /**
     * Gets the texture blend color for this
     * appearance component object.
     * @param textureBlendColor the vector that will receive the texture
     * constant color 
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public void getTextureBlendColor(Color4f textureBlendColor) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_COLOR_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes4"));

	((TextureAttributesRetained)this.retained).getTextureBlendColor(textureBlendColor);
    }

    /**
     * Sets the texture transform object used to transform texture
     * coordinates.  A copy of the specified Transform3D object is
     * stored in this TextureAttributes object.
     * @param transform the new transform object
     * @exception CapabilityNotSetException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void setTextureTransform(Transform3D transform) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_TRANSFORM_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes5"));

	if (isLive())
	    ((TextureAttributesRetained)this.retained).setTextureTransform(transform);
	else
	    ((TextureAttributesRetained)this.retained).initTextureTransform(transform);
    }

    /**
     * Retrieves a copy of the texture transform object.
     * @param transform the transform object that will receive the
     * current texture transform
     * @exception CapabilityNotSetException if the method is called
     * when this object is part of live or compiled scene graph.
     */
    public void getTextureTransform(Transform3D transform) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_TRANSFORM_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes6"));

	((TextureAttributesRetained)this.retained).getTextureTransform(transform);
    }

    /**
     * Sets perspective correction mode to be used for color
     * and/or texture coordinate interpolation.
     * A value of <code>NICEST</code> indicates that perspective correction should be
     * performed and that the highest quality method should be used.
     * A value of <code>FASTEST</code> indicates that the most efficient perspective
     * correction method should be used.
     * @param mode one of <code>NICEST</code> or <code>FASTEST</code>
     * The default value is <code>NICEST</code>.
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalArgumentException if mode value is other
     * than <code>FASTEST</code> or <code>NICEST</code>.
     */
    public void setPerspectiveCorrectionMode(int mode) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_MODE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes7"));

	if ((mode != FASTEST) && (mode!= NICEST))
	    throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes9"));

	if (isLive())
	    ((TextureAttributesRetained)this.retained).setPerspectiveCorrectionMode(mode);
	else
	    ((TextureAttributesRetained)this.retained).initPerspectiveCorrectionMode(mode);
    }

    /**
     * Gets perspective correction mode value.
     * @return mode the value of perspective correction mode
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     */
    public int getPerspectiveCorrectionMode() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_MODE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes8"));
	return ((TextureAttributesRetained)this.retained).getPerspectiveCorrectionMode();
    }

    /**
     * Sets the texture color table from the specified table.  The
     * individual integer array elements are copied.  The array is
     * indexed first by color component (<i>r</i>, <i>g</i>, <i>b</i>,
     * and <i>a</i>, respectively) and then by color value;
     * <code>table.length</code> defines the number of color
     * components and <code>table[0].length</code> defines the texture
     * color table size.  If the table is non-null, the number of
     * color components must either be 3, for <i>rgb</i> data, or 4,
     * for <i>rgba</i> data.  The size of each array for each color
     * component must be the same and must be a power of 2.  If table
     * is null or if the texture color table size is 0, the texture
     * color table is disabled.  If the texture color table size is
     * greater than the device-dependent maximum texture color table
     * size for a particular Canvas3D, the texture color table is
     * ignored for that canvas.
     *
     * <p>
     * When enabled, the texture color table is applied after the
     * texture filtering operation and before texture application.
     * Each of the <i>r</i>, <i>g</i>, <i>b</i>, and <i>a</i>
     * components are clamped to the range [0,1], multiplied by
     * <code>textureColorTableSize-1</code>, and rounded to the
     * nearest integer.  The resulting value for each component is
     * then used as an index into the respective table for that
     * component.  If the texture color table contains 3 components,
     * alpha is passed through unmodified.
     *
     * @param table the new texture color table
     *
     * @exception IllegalArgumentException if <code>table.length</code>
     * is not 3 or 4, or if the arrays for each component are not all
     * the same length, or if the texture color table size
     * is not a power of 2
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.2
     */
    public void setTextureColorTable(int[][] table) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_COLOR_TABLE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes11"));

        if (isLive())
            ((TextureAttributesRetained)this.retained).setTextureColorTable(table);
        else
            ((TextureAttributesRetained)this.retained).initTextureColorTable(table);
    }

    /**
     * Retrieves the texture color table and copies it into the
     * specified array.  If the current texture color table is null,
     * no values are copied.
     *
     * @param table the array that will receive a copy of the
     * texture color table from this TextureAttributes object.
     * The array must be allocated by the caller and must be large
     * enough to hold the entire table (that is,
     * <code>int[numTextureColorTableComponents][textureColorTableSize]</code>).
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public void getTextureColorTable(int[][] table) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_COLOR_TABLE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureAttributes12"));
	((TextureAttributesRetained)this.retained).getTextureColorTable(table);
	return;
    }

    /**
     * Retrieves the number of color components in the current texture
     * color table.  A value of 0 is returned if the texture color
     * table is null.
     *
     * @return the number of color components in the texture color
     * table, or 0 if the table is null
     *
     * @since Java 3D 1.2
     */
    public int getNumTextureColorTableComponents() {
	return (((TextureAttributesRetained)this.retained).getNumTextureColorTableComponents());
    }

    /**
     * Retrieves the size of the current texture color table.  A value
     * of 0 is returned if the texture color table is null.
     *
     * @return the size of the texture color table, or 0 if the table
     * is null
     *
     * @since Java 3D 1.2
     */
    public int getTextureColorTableSize() {
	return (((TextureAttributesRetained)this.retained).getTextureColorTableSize());
    }


    /**
     * Sets the combine mode for the rgb components of the output color 
     * for this object.
     *
     * @param combineMode the combine  mode, one of: 
     * <code>COMBINE_REPLACE</code>,
     * <code>COMBINE_MODULATE</code>, <code>COMBINE_ADD</code>, 
     * <code>COMBINE_ADD_SIGNED</code>, <code>COMBINE_SUBTRACT</code>,
     * <code>COMBINE_INTERPOLATE</code>, or <code>COMBINE_DOT3</code>
     *
     * @exception IllegalArgumentException if <code>combineMode</code>
     * is a value other than <code>COMBINE_REPLACE</code>,
     * <code>COMBINE_MODULATE</code>, <code>COMBINE_ADD</code>, 
     * <code>COMBINE_ADD_SIGNED</code>, <code>COMBINE_SUBTRACT</code>,
     * <code>COMBINE_INTERPOLATE</code>, or <code>COMBINE_DOT3</code>
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineRgbMode(int combineMode) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
				J3dI18N.getString("TextureAttributes16"));
	    }
	}

        if ((combineMode < COMBINE_REPLACE) || (combineMode > COMBINE_DOT3)) {
            throw new IllegalArgumentException(
		J3dI18N.getString("TextureAttributes20"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineRgbMode(combineMode);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineRgbMode(combineMode);
	}
    }

    /**
     * Sets the combine mode for the alpha component of the output color 
     * for this object.
     *
     * @param combineMode the combine  mode, one of: 
     * <code>COMBINE_REPLACE</code>,
     * <code>COMBINE_MODULATE</code>, <code>COMBINE_ADD</code>, 
     * <code>COMBINE_ADD_SIGNED</code>, <code>COMBINE_SUBTRACT</code>,
     * <code>COMBINE_INTERPOLATE</code>, or <code>COMBINE_DOT3</code>
     *
     * @exception IllegalArgumentException if <code>combineMode</code>
     * is a value other than <code>COMBINE_REPLACE</code>,
     * <code>COMBINE_MODULATE</code>, <code>COMBINE_ADD</code>, 
     * <code>COMBINE_ADD_SIGNED</code>, <code>COMBINE_SUBTRACT</code>,
     * <code>COMBINE_INTERPOLATE</code>, or <code>COMBINE_DOT3</code>
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineAlphaMode(int combineMode) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes18"));
            }   
        }

        if ((combineMode < COMBINE_REPLACE) || (combineMode > COMBINE_DOT3)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes20"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineAlphaMode(combineMode);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineAlphaMode(combineMode);
        }
    }

    /**
     * Retrieves the combine mode for the rgb components of the output color
     * for this object.
     * @return the combine mode for the rgb components.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineRgbMode() {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_COMBINE_READ)) {
		throw new CapabilityNotSetException(
				J3dI18N.getString("TextureAttributes17"));
	    }
	}

	return ((TextureAttributesRetained)this.retained).getCombineRgbMode();
    }

    /**
     * Retrieves the combine mode for the alpha component of the output color
     * for this object.
     * @return the combine mode for the alpha component.
     *
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineAlphaMode() {
	if (isLiveOrCompiled()) {
	    if (!this.getCapability(ALLOW_COMBINE_READ)) {
		throw new CapabilityNotSetException(
				J3dI18N.getString("TextureAttributes19"));
	    }
	}

	return ((TextureAttributesRetained)this.retained).getCombineAlphaMode();
    }

    /**
     * Sets the source for the rgb components of the specified color operand 
     * for this object.
     *
     * @param index color operand in the combine operation
     * @param src the color source, one of: <code>COMBINE_OBJECT_COLOR</code>,
     * <code>COMBINE_TEXTURE_COLOR</code>, 
     * <code>COMBINE_CONSTANT_COLOR</code>, or
     * <code>COMBINE_PREVIOUS_TEXTURE_UNIT_STATE</code>
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception IllegalArgumentException if <code>src</code>
     * is a value other than <code>COMBINE_OBJECT_COLOR</code>,
     * <code>COMBINE_TEXTURE_COLOR</code>, 
     * <code>COMBINE_CONSTANT_COLOR</code>, or
     * <code>COMBINE_PREVIOUS_TEXTURE_UNIT_STATE</code>
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * 
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineRgbSource(int index, int src) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes21"));
            }   
        }

	if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
	}

        if ((src < COMBINE_OBJECT_COLOR) || 
		(src > COMBINE_PREVIOUS_TEXTURE_UNIT_STATE)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes26"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineRgbSource(
							index, src);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineRgbSource(
							index, src);
        }
    }

    /**
     * Sets the source for the alpha component of the specified color operand 
     * for this object.
     *
     * @param index color operand in the combine operation
     * @param src the color source, one of: <code>COMBINE_OBJECT_COLOR</code>,
     * <code>COMBINE_TEXTURE_COLOR</code>, 
     * <code>COMBINE_CONSTANT_COLOR</code>, or
     * <code>COMBINE_PREVIOUS_TEXTURE_UNIT_STATE</code>
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception IllegalArgumentException if <code>src</code>
     * is a value other than <code>COMBINE_OBJECT_COLOR</code>,
     * <code>COMBINE_TEXTURE_COLOR</code>, 
     * <code>COMBINE_CONSTANT_COLOR</code>, or
     * <code>COMBINE_PREVIOUS_TEXTURE_UNIT_STATE</code>
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * 
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineAlphaSource(int index, int src) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes23"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        if ((src < COMBINE_OBJECT_COLOR) || 
                (src > COMBINE_PREVIOUS_TEXTURE_UNIT_STATE)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes26"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineAlphaSource(
                                                        index, src);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineAlphaSource(
                                                        index, src);
        }
    }

    /**
     * Retrieves the source for the rgb components of the specified
     * color operand for this object.
     *
     * @param index color operand in the combine operation
     *
     * @return the source for the rgb components of the specified color
     * operand for this object
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * 
     * @since Java 3D 1.3
     */
    public int getCombineRgbSource(int index) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes22"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        return ((TextureAttributesRetained)this.retained).getCombineRgbSource(index);
    }

    /**
     * Retrieves the source for the alpha component of the specified
     * color operand for this object.
     *
     * @param index color operand in the combine operation
     *
     * @return the source for the alpha component of the specified color
     * operand for this object
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception CapabilityNotSetException if appropriate capability is 
     * not set and this object is part of live or compiled scene graph
     * 
     * @since Java 3D 1.3
     */
    public int getCombineAlphaSource(int index) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes24"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        return ((TextureAttributesRetained)this.retained).getCombineAlphaSource(index);
    }

    /**
     * Sets the function for the rgb components of the specified color operand
     * for this object.
     *
     * @param index color operand in the combine operation
     * @param function the color function, one of: 
     * <code>COMBINE_SRC_COLOR</code>,
     * <code>COMBINE_ONE_MINUS_SRC_COLOR</code>, 
     * <code>COMBINE_SRC_ALPHA</code>, or
     * <code>COMBINE_ONE_MINUS_SRC_ALPHA</code>
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception IllegalArgumentException if <code>function</code>
     * is a value other than <code>COMBINE_SRC_COLOR</code>,
     * <code>COMBINE_ONE_MINUS_SRC_COLOR</code>, 
     * <code>COMBINE_SRC_ALPHA</code>, or
     * <code>COMBINE_ONE_MINUS_SRC_ALPHA</code>
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineRgbFunction(int index, int function) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes27"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        if ((function < COMBINE_SRC_COLOR) ||
                (function > COMBINE_ONE_MINUS_SRC_ALPHA)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes31"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineRgbFunction(
                                                        index, function);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineRgbFunction(
                                                        index, function);
        }
    }

    /**
     * Sets the function for the alpha component of the specified color operand
     * for this object.
     *
     * @param index color operand in the combine operation
     * @param function the color function, one of: 
     * <code>COMBINE_SRC_ALPHA</code>, or
     * <code>COMBINE_ONE_MINUS_SRC_ALPHA</code>
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception IllegalArgumentException if <code>function</code>
     * is a value other than 
     * <code>COMBINE_SRC_ALPHA</code> or
     * <code>COMBINE_ONE_MINUS_SRC_ALPHA</code>
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineAlphaFunction(int index, int function) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes29"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        if ((function < COMBINE_SRC_ALPHA) ||
                (function > COMBINE_ONE_MINUS_SRC_ALPHA)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes31"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineAlphaFunction(
                                                        index, function);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineAlphaFunction(
                                                        index, function);
        }
    }

    /**
     * Retrieves the function for the rgb components of the specified color
     * operand for this object.
     *
     * @param index color operand in the combine operation
     *
     * @return the function for the rgb components of the specified color
     * operand for this object.
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineRgbFunction(int index) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes28"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        return ((TextureAttributesRetained)this.retained).getCombineRgbFunction(index);
    }

    /**
     * Retrieves the function for the alpha component of the specified color
     * operand for this object.
     *
     * @param index color operand in the combine operation
     *
     * @return the function for the alpha component of the specified color
     * operand for this object.
     *
     * @exception IndexOutOfBoundsException if <code>index</code> < 0 or 
     * <code>index</code> > 2
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineAlphaFunction(int index) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes30"));
            }
        }

        if ((index < 0) || (index > 2)) {
            throw new IndexOutOfBoundsException(
                J3dI18N.getString("TextureAttributes25"));
        }

        return ((TextureAttributesRetained)this.retained).getCombineAlphaFunction(index);
    }

    /**
     * Sets the scale factor for the rgb components of the output color
     * for this object.
     *
     * @param scale the scale factor for the rgb components of the output 
     * color. It must be one of the following: 1, 2, or 4.
     *
     * @exception IllegalArgumentException if <code>scale</code> is a
     * value other than 1, 2, or 4.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineRgbScale(int scale) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes32"));
            }
        }


        if ((scale != 1) && (scale != 2) && (scale != 4)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes36"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineRgbScale(scale);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineRgbScale(scale);
        }
    }

    /**
     * Sets the scale factor for the alpha component of the output color
     * for this object.
     *
     * @param scale the scale factor for the alpha component of the output 
     * color. It must be one of the following: 1, 2, or 4.
     *
     * @exception IllegalArgumentException if <code>scale</code> is a
     * value other than 1, 2, or 4.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see Canvas3D#queryProperties
     *
     * @since Java 3D 1.3
     */
    public void setCombineAlphaScale(int scale) {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_WRITE)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes34"));
            }
        }

        if ((scale != 1) && (scale != 2) && (scale != 4)) {
            throw new IllegalArgumentException(
                J3dI18N.getString("TextureAttributes36"));
        }

        if (isLive()) {
            ((TextureAttributesRetained)this.retained).setCombineAlphaScale(scale);
        } else {
            ((TextureAttributesRetained)this.retained).initCombineAlphaScale(scale);
        }
    }

    /**
     * Retrieves the scale factor for the rgb components of the output color
     * for this object.
     *
     * @return the scale factor for the rgb components of the output color
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineRgbScale() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes33"));
            }
        }

        return ((TextureAttributesRetained)this.retained).getCombineRgbScale();
    }

    /**
     * Retrieves the scale factor for the alpha component of the output color
     * for this object.
     *
     * @return the scale factor for the alpha component of the output color
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.3
     */
    public int getCombineAlphaScale() {
        if (isLiveOrCompiled()) {
            if (!this.getCapability(ALLOW_COMBINE_READ)) {
                throw new CapabilityNotSetException(
                                J3dI18N.getString("TextureAttributes35"));
            }
        }

        return ((TextureAttributesRetained)this.retained).getCombineAlphaScale();
    }


    /**
     * Creates a retained mode TextureAttributesRetained object that this
     * TextureAttributes component object will point to.
     */
    void createRetained() {
	this.retained = new TextureAttributesRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)  
     */
    public NodeComponent cloneNodeComponent() {
        TextureAttributes ta = new TextureAttributes();
        ta.duplicateNodeComponent(this);
        return ta;
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
      
	TextureAttributesRetained attr = 
	    (TextureAttributesRetained) originalNodeComponent.retained;
	TextureAttributesRetained rt =  (TextureAttributesRetained) retained;	

	Color4f c = new Color4f();
	attr.getTextureBlendColor(c);
	Transform3D t = new Transform3D();
	attr.getTextureTransform(t);

	rt.initTextureMode(attr.getTextureMode());
	rt.initPerspectiveCorrectionMode(attr.getPerspectiveCorrectionMode());
	rt.initTextureBlendColor(c);
	rt.initTextureTransform(t);

	if ((attr.getNumTextureColorTableComponents() != 0) &&
	    (attr.getTextureColorTableSize() != 0)) {
	    int table[][] = new
		int[attr.getNumTextureColorTableComponents()][attr.getTextureColorTableSize()];
	    attr.getTextureColorTable(table);
	    rt.initTextureColorTable(table);
	}
    }
}
