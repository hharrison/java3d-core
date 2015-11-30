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

package org.jogamp.java3d;

/**
 * The TransparencyAttributes object defines all attributes affecting
 * transparency of the object. The transparency attributes are:<p>
 * <ul>
 * <li>Transparency mode - defines how transparency is applied to
 * this Appearance component object:</li><p>
 * <ul>
 * <li>FASTEST - uses the fastest available method for transparency.</li><p>
 * <li>NICEST - uses the nicest available method for transparency.</li><p>
 * <li>SCREEN_DOOR - uses screen-door transparency. This is done using
 * an on/off stipple pattern in which the percentage of transparent pixels
 * is approximately equal to the value specified by the transparency
 * parameter.</li><p>
 * <li>BLENDED - uses alpha blended transparency. The blend equation is
 * specified by the srcBlendFunction and dstBlendFunction attributes.
 * The default equation is:
 * <ul>
 * <code>alpha<sub><font size=-1>src</font></sub>*src +
 * (1-alpha<sub><font size=-1>src</font></sub>)*dst</code>
 * </ul>
 * where <code>alpha<sub><font size=-1>src</font></sub></code> is
 * <code>1-transparency</code>.
 * When this mode is used with a Raster object or with a Geometry
 * that contains per-vertex colors with alpha, the alpha values in
 * the Raster's image or in the Geometry's per-vertex colors are
 * combined with the transparency value in this TransparencyAttributes
 * object to perform blending.  In this case, the alpha value used for
 * blending at each pixel is:
 * <ul>
 * <code>alpha<sub><font size=-1>src</font></sub> =
 * alpha<sub><font size=-1>pix</font></sub> *
 * (1-transparency)</code>.
 * </ul>
 * </li><p>
 * <li>NONE - no transparency; opaque object.</li><p>
 * </ul>
 * <li>Transparency value - the amount of transparency to be applied to this
 * Appearance component object. The transparency values are in the
 * range [0.0,&nbsp;1.0], with 0.0 being fully opaque and 1.0 being
 * fully transparent.</li><p>
 * <li>Blend function - used in blended transparency and antialiasing
 * operations. The source function specifies the factor that is
 * multiplied by the source color. This value is added to the product
 * of the destination factor and the destination color. The default
 * source blend function is BLEND_SRC_ALPHA. The source blend function
 * is one of the following:</li><p>
 * <ul>
 * <li>BLEND_ZERO - the blend function is <code>f = 0</code></li>
 * <li>BLEND_ONE - the blend function is <code>f = 1</code></li>
 * <li>BLEND_SRC_ALPHA - the blend function is <code>f =
 * alpha<sub><font size=-1>src</font></sub></code></li>
 * <li>BLEND_ONE_MINUS_SRC_ALPHA - the blend function is <code>f =
 * 1 - alpha<sub><font size=-1>src</font></sub></code></li>
 * <li>BLEND_DST_COLOR - the blend function is <code>f =
 * color<sub><font size=-1>dst</font></sub></code></li>
 * <li>BLEND_ONE_MINUS_DST_COLOR - the blend function is <code>f =
 * 1 - color<sub><font size=-1>dst</font></sub></code></li>
 * <li>BLEND_SRC_COLOR - the blend function is <code>f =
 * color<sub><font size=-1>src</font></sub></code></li>
 * <li>BLEND_ONE_MINUS_SRC_COLOR - the blend function is <code>f =
 * 1 - color<sub><font size=-1>src</font></sub></code></li>
 * </ul>
 * </ul>
 */
public class TransparencyAttributes extends NodeComponent {
    /**
     * Specifies that this TransparencyAttributes object
     * allows reading its transparency mode component information.
     */
    public static final int
        ALLOW_MODE_READ = CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_MODE_READ;

    /**
     * Specifies that this TransparencyAttributes object
     * allows writing its transparency mode component information.
     */
    public static final int
        ALLOW_MODE_WRITE = CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_MODE_WRITE;

    /**
     * Specifies that this TransparencyAttributes object
     * allows reading its transparency value.
     */
    public static final int
        ALLOW_VALUE_READ = CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_VALUE_READ;

    /**
     * Specifies that this TransparencyAttributes object
     * allows writing its transparency value.
     */
    public static final int
        ALLOW_VALUE_WRITE = CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_VALUE_WRITE;

    /**
     * Specifies that this TransparencyAttributes object
     * allows reading its blend function.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_BLEND_FUNCTION_READ =
        CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_BLEND_FUNCTION_READ;

    /**
     * Specifies that this TransparencyAttributes object
     * allows writing its blend function.
     *
     * @since Java 3D 1.2
     */
    public static final int ALLOW_BLEND_FUNCTION_WRITE =
        CapabilityBits.TRANSPARENCY_ATTRIBUTES_ALLOW_BLEND_FUNCTION_WRITE;

    /**
     * Use the fastest available method for transparency.
     * @see #setTransparencyMode
     */
    public static final int FASTEST            = 0;

    /**
     * Use the nicest available method for transparency.
     * @see #setTransparencyMode
     */
    public static final int NICEST             = 1;

    /**
     * Use alpha blended transparency.  The blend equation is
     * specified by the srcBlendFunction and dstBlendFunction attributes.
     * The default equation is:
     * <ul>
     * <code>alpha<sub><font size=-1>src</font></sub>*src +
     * (1-alpha<sub><font size=-1>src</font></sub>)*dst</code>
     * </ul>
     * where <code>alpha<sub><font size=-1>src</font></sub></code> is
     * <code>1-transparency</code>.
     * When this mode is used with a Raster object or with a Geometry
     * that contains per-vertex colors with alpha, the alpha values in
     * the Raster's image or in the Geometry's per-vertex colors are
     * combined with the transparency value in this TransparencyAttributes
     * object to perform blending.  In this case, the alpha value used for
     * blending at each pixel is:
     * <ul>
     * <code>alpha<sub><font size=-1>src</font></sub> =
     * alpha<sub><font size=-1>pix</font></sub> *
     * (1-transparency)</code>.
     * </ul>
     *
     * @see #setTransparencyMode
     * @see #setSrcBlendFunction
     * @see #setDstBlendFunction
     */
    public static final int BLENDED     = 2;

    /**
     * Use screen-door transparency.  This is done using an on/off stipple
     * pattern where the percentage of pixels that are transparent is
     * approximately equal to the value specified by the transparency
     * parameter.
     * @see #setTransparencyMode
     */
    public static final int SCREEN_DOOR = 3;

    /**
     * No transparency, opaque object.
     * @see #setTransparencyMode
     */
    public static final int NONE     = 4;

    /**
     * Blend function: <code>f = 0</code>.
     * @see #setSrcBlendFunction
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.2
     */
    public static final int BLEND_ZERO = 0;

    /**
     * Blend function: <code>f = 1</code>.
     * @see #setSrcBlendFunction
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.2
     */
    public static final int BLEND_ONE = 1;

    /**
     * Blend function:
     * <code>f = alpha<sub><font size=-1>src</font></sub></code>.
     * @see #setSrcBlendFunction
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.2
     */
    public static final int BLEND_SRC_ALPHA = 2;

    /**
     * Blend function:
     * <code>f = 1-alpha<sub><font size=-1>src</font></sub></code>.
     * @see #setSrcBlendFunction
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.2
     */
    public static final int BLEND_ONE_MINUS_SRC_ALPHA = 3;

    /**
     * Blend function:
     * <code>f = color<sub><font size=-1>dst</font></sub></code>.
     * <p>Note that this function may <i>only</i> be used as a source
     * blend function.</p>
     * @see #setSrcBlendFunction
     *
     * @since Java 3D 1.4
     */
    public static final int BLEND_DST_COLOR = 4;

    /**
     * Blend function:
     * <code>f = 1-color<sub><font size=-1>dst</font></sub></code>.
     * <p>Note that this function may <i>only</i> be used as a source
     * blend function.</p>
     * @see #setSrcBlendFunction
     *
     * @since Java 3D 1.4
     */
    public static final int BLEND_ONE_MINUS_DST_COLOR = 5;

    /**
     * Blend function:
     * <code>f = color<sub><font size=-1>src</font></sub></code>.
     * <p>Note that this function may <i>only</i> be used as a destination
     * blend function.</p>
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.4
     */
    public static final int BLEND_SRC_COLOR = 6;

    /**
     * Blend function:
     * <code>f = 1-color<sub><font size=-1>src</font></sub></code>.
     * <p>Note that this function may <i>only</i> be used as a destination
     * blend function.</p>
     * @see #setDstBlendFunction
     *
     * @since Java 3D 1.4
     */
    public static final int BLEND_ONE_MINUS_SRC_COLOR = 7;

    static final int BLEND_CONSTANT_COLOR = 8;

    static final int MAX_BLEND_FUNC_TABLE_SIZE = 9;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_BLEND_FUNCTION_READ,
        ALLOW_MODE_READ,
        ALLOW_VALUE_READ
    };

    /**
     * Constructs a TransparencyAttributes object with default parameters.
     * The default values are as follows:
     * <ul>
     * transparency mode : <code>NONE</code><br>
     * transparency value : 0.0<br>
     * source blend function : <code>BLEND_SRC_ALPHA</code><br>
     * destination blend function : <code>BLEND_ONE_MINUS_SRC_ALPHA</code><br>
     * </ul>
     */
    public TransparencyAttributes() {
	// Just use the default for all attributes
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Construct TransparencyAttributes object with specified values.
     * @param tMode the transparency mode
     * @param tVal the transparency value
     * @exception IllegalArgumentException if
     * <code>tMode</code> is a value other than
     * <code>NONE</code>, <code>FASTEST</code>, <code>NICEST</code>,
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     *
     */
    public TransparencyAttributes(int tMode, float tVal){
	this(tMode, tVal, BLEND_SRC_ALPHA, BLEND_ONE_MINUS_SRC_ALPHA);
     }

    /**
     * Construct TransparencyAttributes object with specified values.
     * @param tMode the transparency mode
     * @param tVal the transparency value
     * @param srcBlendFunction the blend function to be used for the source
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, <code>BLEND_ONE_MINUS_SRC_ALPHA</code>,
     * <code>BLEND_DST_COLOR</code>, or <code>BLEND_ONE_MINUS_DST_COLOR</code>.
     * @param dstBlendFunction the blend function to be used for the
     * destination
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, <code>BLEND_ONE_MINUS_SRC_ALPHA</code>,
     * <code>BLEND_SRC_COLOR</code>, or <code>BLEND_ONE_MINUS_SRC_COLOR</code>.
     * @exception IllegalArgumentException if
     * <code>tMode</code> is a value other than
     * <code>NONE</code>, <code>FASTEST</code>, <code>NICEST</code>,
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     * @exception IllegalArgumentException if
     * <code>srcBlendFunction</code> or <code>dstBlendFunction</code>
     * is a value other than one of the supported functions listed above.
     *
     * @since Java 3D 1.2
     */
    public TransparencyAttributes(int tMode,
				  float tVal,
				  int srcBlendFunction,
				  int dstBlendFunction) {
	if ((tMode < FASTEST) ||(tMode > NONE)) {
	    throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes6"));
	}

        switch (srcBlendFunction) {
        case BLEND_ZERO:
        case BLEND_ONE:
        case BLEND_SRC_ALPHA:
        case BLEND_ONE_MINUS_SRC_ALPHA:
        case BLEND_DST_COLOR:
        case BLEND_ONE_MINUS_DST_COLOR:
            break;
            default:
            throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes7"));
        }

        switch (dstBlendFunction) {
        case BLEND_ZERO:
        case BLEND_ONE:
        case BLEND_SRC_ALPHA:
        case BLEND_ONE_MINUS_SRC_ALPHA:
        case BLEND_SRC_COLOR:
        case BLEND_ONE_MINUS_SRC_COLOR:
            break;
            default:
            throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes8"));
        }

        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

        ((TransparencyAttributesRetained)this.retained).initTransparencyMode(tMode);
        ((TransparencyAttributesRetained)this.retained).initTransparency(tVal);
        ((TransparencyAttributesRetained)this.retained).initSrcBlendFunction(srcBlendFunction);
        ((TransparencyAttributesRetained)this.retained).initDstBlendFunction(dstBlendFunction);
    }

    /**
     * Sets the transparency mode for this
     * appearance component object.
     * @param transparencyMode the transparency mode to be used, one of
     * <code>NONE</code>, <code>FASTEST</code>, <code>NICEST</code>,
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalArgumentException if
     * <code>transparencyMode</code> is a value other than
     * <code>NONE</code>, <code>FASTEST</code>, <code>NICEST</code>,
     * <code>SCREEN_DOOR</code>, or <code>BLENDED</code>
     */
    public void setTransparencyMode(int transparencyMode) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_MODE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes0"));

	if ((transparencyMode < FASTEST) || (transparencyMode > NONE)) {
		throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes6"));
	}

	if (isLive())
	    ((TransparencyAttributesRetained)this.retained).setTransparencyMode(transparencyMode);
	else
	    ((TransparencyAttributesRetained)this.retained).initTransparencyMode(transparencyMode);
    }



    /**
     * Gets the transparency mode for this
     * appearance component object.
     * @return transparencyMode the transparency mode
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getTransparencyMode() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_MODE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes1"));

        return ((TransparencyAttributesRetained)this.retained).getTransparencyMode();
    }

    /**
     * Sets this appearance's transparency.
     * @param transparency the appearance's transparency
     * in the range [0.0, 1.0] with 0.0 being
     * fully opaque and 1.0 being fully transparent
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setTransparency(float transparency) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_VALUE_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes2"));


	if (isLive())
	    ((TransparencyAttributesRetained)this.retained).setTransparency(transparency);
	else
	    ((TransparencyAttributesRetained)this.retained).initTransparency(transparency);

    }


    /**
     * Retrieves this appearance's transparency.
     * @return the appearance's transparency
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getTransparency() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_VALUE_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes3"));

        return ((TransparencyAttributesRetained)this.retained).getTransparency();
    }

    /**
     * Sets the source blend function used in blended transparency
     * and antialiasing operations.  The source function specifies the
     * factor that is multiplied by the source color; this value is
     * added to the product of the destination factor and the
     * destination color.  The default source blend function is
     * <code>BLEND_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the source
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, <code>BLEND_ONE_MINUS_SRC_ALPHA</code>,
     * <code>BLEND_DST_COLOR</code>, or <code>BLEND_ONE_MINUS_DST_COLOR</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalArgumentException if <code>blendFunction</code>
     * is a value other than one of the supported functions listed above.
     *
     * @since Java 3D 1.2
     */
    public void setSrcBlendFunction(int blendFunction) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_FUNCTION_WRITE))
		throw new
		    CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes4"));

        switch (blendFunction) {
        case BLEND_ZERO:
        case BLEND_ONE:
        case BLEND_SRC_ALPHA:
        case BLEND_ONE_MINUS_SRC_ALPHA:
        case BLEND_DST_COLOR:
        case BLEND_ONE_MINUS_DST_COLOR:
            break;
            default:
            throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes7"));
        }

	if (isLive())
	    ((TransparencyAttributesRetained)this.retained).setSrcBlendFunction(blendFunction);
	else
	    ((TransparencyAttributesRetained)this.retained).initSrcBlendFunction(blendFunction);
    }



    /**
     * Gets the source blend function for this
     * TransparencyAttributes object.
     * @return the source blend function.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getSrcBlendFunction() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_FUNCTION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes5"));
	return ((TransparencyAttributesRetained)this.retained).getSrcBlendFunction();
    }

    /**
     * Sets the destination blend function used in blended transparency
     * and antialiasing operations.  The destination function specifies the
     * factor that is multiplied by the destination color; this value is
     * added to the product of the source factor and the
     * source color.  The default destination blend function is
     * <code>BLEND_ONE_MINUS_SRC_ALPHA</code>.
     *
     * @param blendFunction the blend function to be used for the destination
     * color, one of <code>BLEND_ZERO</code>, <code>BLEND_ONE</code>,
     * <code>BLEND_SRC_ALPHA</code>, <code>BLEND_ONE_MINUS_SRC_ALPHA</code>,
     * <code>BLEND_SRC_COLOR</code>, or <code>BLEND_ONE_MINUS_SRC_COLOR</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception IllegalArgumentException if <code>blendFunction</code>
     * is a value other than one of the supported functions listed above.
     *
     * @since Java 3D 1.2
     */
    public void setDstBlendFunction(int blendFunction) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_FUNCTION_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes4"));

        switch (blendFunction) {
        case BLEND_ZERO:
        case BLEND_ONE:
        case BLEND_SRC_ALPHA:
        case BLEND_ONE_MINUS_SRC_ALPHA:
        case BLEND_SRC_COLOR:
        case BLEND_ONE_MINUS_SRC_COLOR:
            break;
            default:
            throw new IllegalArgumentException(J3dI18N.getString("TransparencyAttributes8"));
        }

	if (isLive())
	    ((TransparencyAttributesRetained)this.retained).setDstBlendFunction(blendFunction);
	else
	    ((TransparencyAttributesRetained)this.retained).initDstBlendFunction(blendFunction);
    }



    /**
     * Gets the destination blend function for this
     * TransparencyAttributes object.
     * @return the destination blend function.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getDstBlendFunction() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_BLEND_FUNCTION_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TransparencyAttributes5"));

	return ((TransparencyAttributesRetained)this.retained).getDstBlendFunction();
    }

    /**
     * Creates a retained mode TransparencyAttributesRetained object that this
     * TransparencyAttributes component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new TransparencyAttributesRetained();
	this.retained.setSource(this);
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
    public NodeComponent cloneNodeComponent() {
        TransparencyAttributes transa = new TransparencyAttributes();
        transa.duplicateNodeComponent(this);
        return transa;
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
    @Override
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	TransparencyAttributesRetained attr =
	    (TransparencyAttributesRetained) originalNodeComponent.retained;
	TransparencyAttributesRetained rt =
	    (TransparencyAttributesRetained) retained;

	rt.initTransparencyMode(attr.getTransparencyMode());
	rt.initTransparency(attr.getTransparency());
	rt.initSrcBlendFunction(attr.getSrcBlendFunction());
	rt.initDstBlendFunction(attr.getDstBlendFunction());
    }

}
