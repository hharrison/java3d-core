/*
 * Copyright 1996-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import javax.vecmath.Point3f;

/**
 * A Text3D object is a text string that has been converted to 3D
 * geometry.  The Font3D object determines the appearance of the
 * Text3D NodeComponent object. Each Text3D object has the following
 * parameters:<P>
 * <UL>
 * <LI>Font3D object - describes the font style of the text string,
 * such as the font family (Helvetica, Courier, etc.), style (Italic,
 * bold, etc.), and point size. The size of the resulting characters will
 * be equal to the point size. For example, a 12 point font will result in
 * a Font3D with characters 12 meters tall.  </LI><P>
 * <LI>Text string - the text string to be written.</LI><P>
 * <LI>Position - determines the initial placement of the Text3D string
 * in three-space.</LI><P>
 * <LI>Alignment - specifies how glyphs in the string are placed in
 * relation to the position parameter. Valid values are:
 * <UL>
 * <LI> ALIGN_CENTER - the center of the string is placed on the
 *  <code>position</code> point.</LI>
 * <LI> ALIGN_FIRST - the first character of the string is placed on
 *   the <code>position</code> point.</LI>
 * <LI> ALIGN_LAST - the last character of the string is placed on the
 *   <code>position</code> point.</LI>
 * </UL><P>
 * <LI>Path - specifies how succeeding glyphs in the string are placed
 * in relation to the previous glyph. Valid values are:</LI><P>
 * <UL>
 * <LI> PATH_LEFT - succeeding glyphs are placed to the left of the
 *  current glyph.</LI>
 * <LI> PATH_RIGHT - succeeding glyphs are placed to the right of the
 *  current glyph.</LI>
 * <LI> PATH_UP - succeeding glyphs are placed above the current glyph.</LI>
 * <LI> PATH_DOWN - succeeding glyphs are placed below the current glyph.</LI>
 * </UL><P>
 * <LI>Character spacing - the space between characters. This spacing is
 * in addition to the regular spacing between glyphs as defined in the
 * Font object.</LI></UL><P>
 *
 * @see Font3D
 */
public class Text3D extends Geometry {

    /**
     * Specifies that this Text3D object allows
     * reading the Font3D component information.
     *
     * @see Font3D
     */
    public static final int
    ALLOW_FONT3D_READ = CapabilityBits.TEXT3D_ALLOW_FONT3D_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the Font3D component information.
     *
     * @see Font3D
     */
    public static final int
    ALLOW_FONT3D_WRITE = CapabilityBits.TEXT3D_ALLOW_FONT3D_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the String object.
     */
    public static final int
    ALLOW_STRING_READ = CapabilityBits.TEXT3D_ALLOW_STRING_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the String object.
     */
    public static final int
    ALLOW_STRING_WRITE = CapabilityBits.TEXT3D_ALLOW_STRING_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the text position value.
     */
    public static final int
    ALLOW_POSITION_READ = CapabilityBits.TEXT3D_ALLOW_POSITION_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the text position value.
     */
    public static final int
    ALLOW_POSITION_WRITE = CapabilityBits.TEXT3D_ALLOW_POSITION_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the text alignment value.
     */
    public static final int
    ALLOW_ALIGNMENT_READ = CapabilityBits.TEXT3D_ALLOW_ALIGNMENT_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the text alignment value.
     */
    public static final int
    ALLOW_ALIGNMENT_WRITE = CapabilityBits.TEXT3D_ALLOW_ALIGNMENT_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the text path value.
     */
    public static final int
    ALLOW_PATH_READ = CapabilityBits.TEXT3D_ALLOW_PATH_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the text path value.
     */
    public static final int
    ALLOW_PATH_WRITE = CapabilityBits.TEXT3D_ALLOW_PATH_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the text character spacing value.
     */
    public static final int
    ALLOW_CHARACTER_SPACING_READ = CapabilityBits.TEXT3D_ALLOW_CHARACTER_SPACING_READ;

    /**
     * Specifies that this Text3D object allows
     * writing the text character spacing value.
     */
    public static final int
    ALLOW_CHARACTER_SPACING_WRITE = CapabilityBits.TEXT3D_ALLOW_CHARACTER_SPACING_WRITE;

    /**
     * Specifies that this Text3D object allows
     * reading the text string bounding box value
     */
    public static final int
    ALLOW_BOUNDING_BOX_READ = CapabilityBits.TEXT3D_ALLOW_BOUNDING_BOX_READ;

    /**
     * <code>alignment</code>: the center of the string is placed on the
     * <code>position</code> point.
     *
     * @see #getAlignment
     */
    public static final int ALIGN_CENTER = 0;

    /**
     * <code>alignment</code>: the first character of the string is placed
     * on the <code>position</code> point.
     *
     * @see #getAlignment
     */
    public static final int ALIGN_FIRST = 1;

    /**
     * <code>alignment</code>: the last character of the string is placed
     * on the <code>position</code> point.
     *
     * @see #getAlignment
     */
    public static final int ALIGN_LAST = 2;

    /**
     * <code>path</code>: succeeding glyphs are placed to the left of
     * the current glyph.
     *
     * @see #getPath
     */
    public static final int PATH_LEFT = 0;
    /**
     * <code>path</code>: succeeding glyphs are placed to the left of
     * the current glyph.
     *
     * @see #getPath
     */
    public static final int PATH_RIGHT = 1;

    /**
     * <code>path</code>: succeeding glyphs are placed above the
     * current glyph.
     *
     * @see #getPath
     */
    public static final int PATH_UP = 2;

    /**
     * <code>path</code>: succeeding glyphs are placed below the
     * current glyph.
     *
     * @see #getPath
     */
    public static final int PATH_DOWN = 3;

    // Array for setting default read capabilities
    private static final int[] readCapabilities = {
	ALLOW_FONT3D_READ,
	ALLOW_STRING_READ,
	ALLOW_POSITION_READ,
	ALLOW_ALIGNMENT_READ,
	ALLOW_PATH_READ,
	ALLOW_CHARACTER_SPACING_READ,
	ALLOW_BOUNDING_BOX_READ
    };

    /**
     * Constructs a Text3D object with default parameters.
     * The default values are as follows:
     * <ul>
     * font 3D : null<br>
     * string : null<br>
     * position : (0,0,0)<br>
     * alignment : ALIGN_FIRST<br>
     * path : PATH_RIGHT<br>
     * character spacing : 0.0<br>
     * </ul>
     */
    public Text3D() {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Creates a Text3D object with the given Font3D object.
     *
     * @see Font3D
     */
    public Text3D(Font3D font3D) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((Text3DRetained)this.retained).setFont3D(font3D);
    }

    /**
     * Creates a Text3D object given a Font3D object and a string.  The
     * string is converted into 3D glyphs.  The first glyph from the
     * string is placed at (0.0, 0.0, 0.0) and succeeding glyphs are
     * placed to the right of the initial glyph.
     *
     * @see Font3D
     */
    public Text3D(Font3D font3D, String string) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((Text3DRetained)this.retained).setFont3D(font3D);
	((Text3DRetained)this.retained).setString(string);
    }

    /**
     * Creates a Text3D object given a Font3D, a string and position. The
     * string is converted into 3D glyphs.  The first glyph from the
     * string is placed at position <code>position</code> and succeeding
     * glyphs are placed to the right of the initial glyph.
     *
     * @see Font3D
     */
    public Text3D(Font3D font3D, String string, Point3f position) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((Text3DRetained)this.retained).setFont3D(font3D);
	((Text3DRetained)this.retained).setString(string);
	((Text3DRetained)this.retained).setPosition(position);
    }

    /**
     * Creates a Text3D object given a Font3D, string, position, alignment
     * and path along which string is to be placed. The
     * string is converted into 3D glyphs.  The placement of the glyphs
     * with respect to the <code>position</code> position depends on
     * the alignment parameter and the path parameter.
     *
     * @see Font3D
     */
    public Text3D(Font3D font3D, String string, Point3f position,
		  int alignment, int path) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((Text3DRetained)this.retained).setFont3D(font3D);
	((Text3DRetained)this.retained).setString(string);
	((Text3DRetained)this.retained).setPosition(position);
	((Text3DRetained)this.retained).setAlignment(alignment);
	((Text3DRetained)this.retained).setPath(path);
    }

    /**
     * Creates the retained mode Text3DRetained object that this
     * Text3D component object will point to.
     */
    void createRetained() {
        this.retained = new Text3DRetained();
        this.retained.setSource(this);
    }


    /**
     * Returns the Font3D objects used by this Text3D NodeComponent object.
     *
     * @return the Font3D object of this Text3D node - null if no Font3D
     *  has been associated with this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Font3D getFont3D() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_FONT3D_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D0"));
	return ((Text3DRetained)this.retained).getFont3D();

    }

    /**
     * Sets the Font3D object used by this Text3D NodeComponent object.
     *
     * @param font3d the Font3D object to associate with this Text3D node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setFont3D(Font3D font3d) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_FONT3D_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D1"));
      ((Text3DRetained)this.retained).setFont3D(font3d);

    }

    /**
     * Copies the character string used in the construction of the
     * Text3D node into the supplied parameter.
     *
     * @return a copy of the String object in this Text3D node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public String getString() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_STRING_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D2"));
	return ((Text3DRetained)this.retained).getString();
    }

    /**
     * Copies the character string from the supplied parameter into the
     * Text3D node.
     *
     * @param string the String object to recieve the Text3D node's string.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setString(String string) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_STRING_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D3"));
	((Text3DRetained)this.retained).setString(string);
    }

    /**
     * Copies the node's <code>position</code> field into the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the Text3D string.  The position, combined with
     * the path and alignment control how the text is displayed.
     *
     * @param position the point to position the text.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getAlignment
     * @see #getPath
     */
    public void getPosition(Point3f position) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D4"));
	((Text3DRetained)this.retained).getPosition(position);
    }

    /**
     * Sets the node's <code>position</code> field to the supplied
     * parameter.  The <code>position</code> is used to determine the
     * initial placement of the Text3D string.  The position, combined with
     * the path and alignment control how the text is displayed.
     *
     * @param position the point to position the text.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getAlignment
     * @see #getPath
     */
    public void setPosition(Point3f position) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_POSITION_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D5"));
	((Text3DRetained)this.retained).setPosition(position);
    }

    /**
     * Retrieves the text alignment policy for this Text3D NodeComponent
     * object. The <code>alignment</code> is used to specify how
     * glyphs in the string are placed in relation to the
     * <code>position</code> field.  Valid values for this field
     * are:
     * <UL>
     * <LI> ALIGN_CENTER - the center of the string is placed on the
     *  <code>position</code> point.
     * <LI> ALIGN_FIRST - the first character of the string is placed on
     *   the <code>position</code> point.
     * <LI> ALIGN_LAST - the last character of the string is placed on the
     *   <code>position</code> point.
     * </UL>
     * The default value of this field is <code>ALIGN_FIRST</code>.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getPosition
     */
    public int getAlignment() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ALIGNMENT_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D6"));
	return ((Text3DRetained)this.retained).getAlignment();
    }

    /**
     * Sets the text alignment policy for this Text3D NodeComponent
     * object. The <code>alignment</code> is used to specify how
     * glyphs in the string are placed in relation to the
     * <code>position</code> field.  Valid values for this field
     * are:
     * <UL>
     * <LI> ALIGN_CENTER - the center of the string is placed on the
     *  <code>position</code> point.
     * <LI> ALIGN_FIRST - the first character of the string is placed on
     *   the <code>position</code> point.
     * <LI> ALIGN_LAST - the last character of the string is placed on the
     *   <code>position</code> point.
     * </UL>
     * The default value of this field is <code>ALIGN_FIRST</code>.
     *
     * @param alignment specifies how glyphs in the string are placed
     * in relation to the position field
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see #getPosition
     */
    public void setAlignment(int alignment) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_ALIGNMENT_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D7"));
        ((Text3DRetained)this.retained).setAlignment(alignment);
    }

    /**
     * Retrieves the node's <code>path</code> field.  This field
     * is used  to specify how succeeding
     * glyphs in the string are placed in relation to the previous glyph.
     * Valid values for this field are:
     * <UL>
     * <LI> PATH_LEFT: - succeeding glyphs are placed to the left of the
     *  current glyph.
     * <LI> PATH_RIGHT: - succeeding glyphs are placed to the right of the
     *  current glyph.
     * <LI> PATH_UP: - succeeding glyphs are placed above the current glyph.
     * <LI> PATH_DOWN: - succeeding glyphs are placed below the current glyph.
     * </UL>
     * The default value of this field is <code>PATH_RIGHT</code>.
     *
     * @return the current alingment policy for this node.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public int getPath() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PATH_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D8"));
        return ((Text3DRetained)this.retained).getPath();
    }

    /**
     * Sets the node's <code>path</code> field.  This field
     * is used  to specify how succeeding
     * glyphs in the string are placed in relation to the previous glyph.
     * Valid values for this field are:
     * <UL>
     * <LI> PATH_LEFT - succeeding glyphs are placed to the left of the
     *  current glyph.
     * <LI> PATH_RIGHT - succeeding glyphs are placed to the right of the
     *  current glyph.
     * <LI> PATH_UP - succeeding glyphs are placed above the current glyph.
     * <LI> PATH_DOWN - succeeding glyphs are placed below the current glyph.
     * </UL>
     * The default value of this field is <code>PATH_RIGHT</code>.
     *
     * @param path the value to set the path to
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPath(int path) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_PATH_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D9"));
	((Text3DRetained)this.retained).setPath(path);
    }

    /**
     * Retrieves the 3D bounding box that encloses this Text3D object.
     *
     * @param bounds the object to copy the bounding information to.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @see BoundingBox
     */
    public void getBoundingBox(BoundingBox bounds) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_BOUNDING_BOX_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D10"));
	((Text3DRetained)this.retained).getBoundingBox(bounds);
    }

    /**
     * Retrieves the character spacing used to construct the Text3D string.
     * This spacing is in addition to the regular spacing between glyphs as
     * defined in the Font object.  1.0 in this space is measured as the
     * width of the largest glyph in the 2D Font.  The default value is
     * 0.0.
     *
     * @return the current character spacing value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public float getCharacterSpacing() {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CHARACTER_SPACING_READ))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D11"));
	return ((Text3DRetained)this.retained).getCharacterSpacing();
    }

    /**
     * Sets the character spacing used when constructing the Text3D string.
     * This spacing is in addition to the regular spacing between glyphs as
     * defined in the Font object.  1.0 in this space is measured as the
     * width of the largest glyph in the 2D Font.  The default value is
     * 0.0.
     *
     * @param characterSpacing the new character spacing value
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setCharacterSpacing(float characterSpacing) {
        if (isLiveOrCompiled())
            if(!this.getCapability(ALLOW_CHARACTER_SPACING_WRITE))
              throw new CapabilityNotSetException(J3dI18N.getString("Text3D12"));
	((Text3DRetained)this.retained).setCharacterSpacing(characterSpacing);
    }



   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    public NodeComponent cloneNodeComponent() {
        Text3D t = new Text3D();
        t.duplicateNodeComponent(this);
        return t;
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

	Text3DRetained text = (Text3DRetained) originalNodeComponent.retained;
	Text3DRetained rt = (Text3DRetained) retained;

	Font3D font3D = text.getFont3D();
	if (font3D != null) {
	    rt.setFont3D(font3D);
	}

	String s = text.getString();
	if (s != null) {
	    rt.setString(s);
	}

	Point3f p = new Point3f();
	text.getPosition(p);
	rt.setPosition(p);
	rt.setAlignment(text.getAlignment());
	rt.setPath(text.getPath());
	rt.setCharacterSpacing(text.getCharacterSpacing());
    }

}
