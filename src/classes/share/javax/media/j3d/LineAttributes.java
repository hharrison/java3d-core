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

/**
 * The LineAttributes object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 * The line attributes that can be defined are:<P>
 * <UL><LI>Pattern - specifies the pattern used to draw the line:<p>
 * <ul>
 * <li>PATTERN_SOLID - draws a solid line with no pattern. This is
 * the default.</li>
 * <p>
 * <li>PATTERN_DASH - draws dashed lines. Ideally, these will be drawn with
 * a repeating pattern of 8 pixels on and 8 pixels off.</li>
 * <p>
 * <li>PATTERN_DOT - draws dotted lines. Ideally, these will be drawn with
 * a repeating pattern of 1 pixel on and 7 pixels off.</li>
 * <p>
 * <li>PATTERN_DASH_DOT - draws dashed-dotted lines. Ideally, these will be
 * drawn with a repeating pattern of 7 pixels on, 4 pixels off, 1 pixel on, 
 * and 4 pixels off.</li>
 * <p>
 * <li>PATTERN_USER_DEFINED - draws lines with a user-defined line pattern.
 * See "User-defined Line Patterns," below.</li><p>
 * </ul>
 * <p>
 * <LI>Antialiasing (enabled or disabled). By default, antialiasing
 * is disabled.</LI>
 * <p>
 * <p>
 * If antialiasing is enabled, the lines are considered transparent
 * for rendering purposes.  They are rendered with all the other transparent 
 * objects and adhere to the other transparency settings such as the
 * View transparency sorting policy and the View depth buffer freeze 
 * transparent enable.
 * </p> 
 * <LI>Width (in pixels). The default is a line width of one pixel.
 * </LI></UL><p>
 *
 * <b>User-defined Line Patterns</b>
 * <p>
 * A user-defined line pattern is specified with a pattern mask and 
 * an optional scale factor.
 * <p>
 * The Pattern Mask<p>
 *
 * The pattern is specified
 * using a 16-bit mask that specifies on and off segments. Bit 0 in 
 * the pattern mask corresponds to the first pixel of the line or line 
 * strip primitive. A value of 1 for a bit in the pattern mask indicates 
 * that the corresponding pixel is drawn, while a value of 0
 * indicates that the corresponding pixel is not drawn. After all 16 bits 
 * in the pattern are used, the pattern is repeated.
 * <p>
 * For example, a mask of 0x00ff defines a dashed line with a repeating
 * pattern of 8 pixels on followed by 8 pixels off. A value of 0x0101 
 * defines a a dotted line with a repeating pattern of 1 pixel on and 7 
 * pixels off.
 * <p>
 * The pattern continues around individual line segments of a line strip
 * primitive. It is restarted at the beginning of each new line strip. 
 * For line array primitives, the pattern is restarted at the beginning 
 * of each line.
 * <p>
 * The Scale Factor
 * <p>
 * The pattern is multiplied by the scale factor such that each bit in
 * the pattern mask corresponds to that many consecutive pixels.
 * For example, a scale factor of 3 applied to a pattern mask of 0x001f 
 * would produce a repeating pattern of 15 pixels on followed by 33
 * pixels off. The valid range for this attribute is [1,15]. Values
 * outside this range are clamped.<p>
 * 
 * @see Appearance
 * @see View
 */
public class LineAttributes extends NodeComponent {

    /**
     * Specifies that this LineAttributes object allows reading its
     * line width information.
     */
    public static final int
    ALLOW_WIDTH_READ = CapabilityBits.LINE_ATTRIBUTES_ALLOW_WIDTH_READ;

    /**
     * Specifies that this LineAttributes object allows writing its
     * line width information.
     */
    public static final int
    ALLOW_WIDTH_WRITE = CapabilityBits.LINE_ATTRIBUTES_ALLOW_WIDTH_WRITE;

    /**
     * Specifies that this LineAttributes object allows reading its
     * line pattern information.
     */
    public static final int
    ALLOW_PATTERN_READ = CapabilityBits.LINE_ATTRIBUTES_ALLOW_PATTERN_READ;

    /**
     * Specifies that this LineAttributes object allows writing its
     * line pattern information.
     */
    public static final int
    ALLOW_PATTERN_WRITE = CapabilityBits.LINE_ATTRIBUTES_ALLOW_PATTERN_WRITE;

    /**
     * Specifies that this LineAttributes object allows reading its
     * line antialiasing flag.
     */
    public static final int
    ALLOW_ANTIALIASING_READ = CapabilityBits.LINE_ATTRIBUTES_ALLOW_ANTIALIASING_READ;

    /**
     * Specifies that this LineAttributes object allows writing its
     * line antialiasing flag.
     */
    public static final int
    ALLOW_ANTIALIASING_WRITE = CapabilityBits.LINE_ATTRIBUTES_ALLOW_ANTIALIASING_WRITE;


    /**
     * Draw solid lines with no pattern.
     * @see #setLinePattern
     */
    public static final int PATTERN_SOLID = 0;

    /**
     * Draw dashed lines.  Ideally, these will be drawn with
     * a repeating pattern of 8 pixels on and 8 pixels off.
     * @see #setLinePattern
     */
    public static final int PATTERN_DASH = 1;

    /**
     * Draw dotted lines.  Ideally, these will be drawn with
     * a repeating pattern of 1 pixel on and 7 pixels off.
     * @see #setLinePattern
     */
    public static final int PATTERN_DOT = 2;

    /**
     * Draw dashed-dotted lines.  Ideally, these will be drawn with
     * a repeating pattern of 7 pixels on, 4 pixels off, 1 pixel on,
     * and 4 pixels off.
     * @see #setLinePattern
     */
    public static final int PATTERN_DASH_DOT = 3;

    /**
     * Draw lines with a user-defined line pattern.  The line pattern
     * is specified with a pattern mask and scale factor.
     * @see #setLinePattern
     * @see #setPatternMask
     * @see #setPatternScaleFactor
     *
     * @since Java 3D 1.2
     */
    public static final int PATTERN_USER_DEFINED = 4;


    /**
     * Constructs a LineAttributes object with default parameters.
     * The default values are as follows:
     * <ul>
     * line width : 1<br>
     * line pattern : PATTERN_SOLID<br>
     * pattern mask : 0xffff<br>
     * pattern scale factor : 1<br>
     * line antialiasing : false<br>
     * </ul>
     */
     public LineAttributes(){
      }

    /**
     * Constructs a LineAttributes object with specified values.
     * @param lineWidth the width of lines in pixels
     * @param linePattern the line pattern, one of PATTERN_SOLID,
     * PATTERN_DASH, PATTERN_DOT, or PATTERN_DASH_DOT
     * @param lineAntialiasing flag to set line antialising ON or OFF
     */
     public LineAttributes(float lineWidth, int linePattern,
			   boolean lineAntialiasing){

       if (linePattern < PATTERN_SOLID || linePattern > PATTERN_DASH_DOT)
	 throw new IllegalArgumentException(J3dI18N.getString("LineAttributes0"));

       ((LineAttributesRetained)this.retained).initLineWidth(lineWidth);
       ((LineAttributesRetained)this.retained).initLinePattern(linePattern);
       ((LineAttributesRetained)this.retained).initLineAntialiasingEnable(lineAntialiasing);
      }

    /**
     * Sets the line width for this LineAttributes component object.
     * @param lineWidth the width, in pixels, of line primitives
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setLineWidth(float lineWidth) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_WIDTH_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes1"));
	if (isLive()) 
	    ((LineAttributesRetained)this.retained).setLineWidth(lineWidth);
	else
	    ((LineAttributesRetained)this.retained).initLineWidth(lineWidth);

    }

    /**
     * Gets the line width for this LineAttributes component object.
     * @return the width, in pixels, of line primitives
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getLineWidth() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_WIDTH_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes2"));
        return ((LineAttributesRetained)this.retained).getLineWidth();
    }

    /**
     * Sets the line pattern for this LineAttributes component object.
     * @param linePattern the line pattern to be used, one of:
     * PATTERN_SOLID, PATTERN_DASH, PATTERN_DOT, PATTERN_DASH_DOT, or
     * PATTERN_USER_DEFINED.
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setLinePattern(int linePattern) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PATTERN_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes3"));

        if (linePattern < PATTERN_SOLID || linePattern > PATTERN_USER_DEFINED)
	  throw new IllegalArgumentException(J3dI18N.getString("LineAttributes4"));

	if (isLive())
	    ((LineAttributesRetained)this.retained).setLinePattern(linePattern);
	else
	    ((LineAttributesRetained)this.retained).initLinePattern(linePattern);

    
}

    /**
     * Gets the line pattern for this LineAttributes component object.
     * @return the line pattern
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getLinePattern() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PATTERN_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes5"));

        return ((LineAttributesRetained)this.retained).getLinePattern();
    }


    /**
     * Sets the line pattern mask to the specified value.  This is
     * used when the linePattern attribute is set to
     * PATTERN_USER_DEFINED.  In this mode, the pattern is specified
     * using a 16-bit mask that specifies on and off segments.  Bit 0
     * in the pattern mask corresponds to the first pixel of the line
     * or line strip primitive.  A value of 1 for a bit in the pattern
     * mask indicates that the corresponding pixel is drawn, while a
     * value of 0 indicates that the corresponding pixel is not drawn.
     * After all 16 bits in the pattern are used, the pattern is
     * repeated.  For example, a mask of 0x00ff defines a dashed line
     * with a repeating pattern of 8 pixels on followed by 8 pixels
     * off.  A value of 0x0101 defines a a dotted line with a
     * repeating pattern of 1 pixel on and 7 pixels off
     * <p>
     * The pattern continues around individual line segments of a line
     * strip primitive.  It is restarted at the beginning of each new
     * line strip.  For line array primitives, the pattern is
     * restarted at the beginning of each line.
     * @param mask the new line pattern mask
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @see #setPatternScaleFactor
     *
     * @since Java 3D 1.2
     */
    public void setPatternMask(int mask) {
        if (isLiveOrCompiled() &&
	    !this.getCapability(ALLOW_PATTERN_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes8"));

	if (isLive())
	    ((LineAttributesRetained)this.retained).setPatternMask(mask);
	else
	    ((LineAttributesRetained)this.retained).initPatternMask(mask);
    }


    /**
     * Retrieves the line pattern mask.
     * @return the line pattern mask
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getPatternMask() {
        if (isLiveOrCompiled() &&
            !this.getCapability(ALLOW_PATTERN_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes9"));

	return ((LineAttributesRetained)this.retained).getPatternMask();
    }


    /**
     * Sets the line pattern scale factor to the specified value.
     * This is used in conjunction with the patternMask when the
     * linePattern attribute is set to PATTERN_USER_DEFINED.  The
     * pattern is multiplied by the scale factor such that each bit in
     * the pattern mask corresponds to that many consecutive pixels.
     * For example, a scale factor of 3 applied to a pattern mask of
     * 0x001f would produce a repeating pattern of 15 pixels on
     * followed by 33 pixels off. The valid range for this attribute
     * is [1,15].  Values outside this range are clamped.
     * @param scaleFactor the new line pattern scale factor
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @see #setPatternMask
     *
     * @since Java 3D 1.2
     */
    public void setPatternScaleFactor(int scaleFactor) {
	if (isLiveOrCompiled() &&
	    !this.getCapability(ALLOW_PATTERN_WRITE))
	    throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes10"));

	if (isLive())
	    ((LineAttributesRetained)this.retained).setPatternScaleFactor(scaleFactor);
	else
	    ((LineAttributesRetained)this.retained).initPatternScaleFactor(scaleFactor);
    }


    /**
     * Retrieves the line pattern scale factor.
     * @return the line pattern scale factor
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getPatternScaleFactor() {
	if (isLiveOrCompiled() &&
            !this.getCapability(ALLOW_PATTERN_READ))
	    throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes11"));

	return ((LineAttributesRetained)this.retained).getPatternScaleFactor();
    }


    /**
     * Enables or disables line antialiasing
     * for this LineAttributes component object.
     * <p>
     * If antialiasing is enabled, the lines are considered transparent
     * for rendering purposes.  They are rendered with all the other 
     * transparent objects and adhere to the other transparency settings 
     * such as the View transparency sorting policy and the View depth buffer 
     * freeze transparent enable.
     * </p> 
     * @param state true or false to enable or disable line antialiasing
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @see View
     */
    public void setLineAntialiasingEnable(boolean state) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANTIALIASING_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes6"));
	if (isLive())
	    ((LineAttributesRetained)this.retained).setLineAntialiasingEnable(state);
	else
	    ((LineAttributesRetained)this.retained).initLineAntialiasingEnable(state);


    }

    /**
     * Retrieves the state of the line antialiasing flag.
     * @return true if line antialiasing is enabled,
     * false if line antialiasing is disabled
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public boolean getLineAntialiasingEnable() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ANTIALIASING_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("LineAttributes7"));

        return ((LineAttributesRetained)this.retained).getLineAntialiasingEnable();
    }

    /**
     * Creates a retained mode LineAttributesRetained object that this
     * LineAttributes component object will point to.
     */
    void createRetained() {
	this.retained = new LineAttributesRetained();
	this.retained.setSource(this);
    }



    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)   
     */
    public NodeComponent cloneNodeComponent() {
        LineAttributes la = new LineAttributes();
        la.duplicateNodeComponent(this);
        return la;
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
      
	 LineAttributesRetained attr = (LineAttributesRetained) 
	                                originalNodeComponent.retained;
	 LineAttributesRetained rt = (LineAttributesRetained) retained;

	 rt.initLineWidth(attr.getLineWidth());
	 rt.initLinePattern(attr.getLinePattern());
	 rt.initLineAntialiasingEnable(attr.getLineAntialiasingEnable());
	 rt.initPatternMask(attr.getPatternMask());
	 rt.initPatternScaleFactor(attr.getPatternScaleFactor());
    }

}
