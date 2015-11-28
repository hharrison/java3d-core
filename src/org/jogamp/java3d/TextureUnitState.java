/*
 * Copyright 1999-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.Hashtable;

/**
 * The TextureUnitState object defines all texture mapping state for a
 * single texture unit.  An appearance object contains an array of
 * texture unit state objects to define the state for multiple texture
 * mapping units.  The texture unit state consists of the
 * following:
 *
 * <p>
 * <ul>
 * <li>Texture - defines the texture image and filtering
 * parameters used when texture mapping is enabled. These attributes
 * are defined in a Texture object.</li><p>
 *
 * <li>Texture attributes - defines the attributes that apply to
 * texture mapping, such as the texture mode, texture transform,
 * blend color, and perspective correction mode. These attributes
 * are defined in a TextureAttributes object.</li><p>
 *
 * <li>Texture coordinate generation - defines the attributes
 * that apply to texture coordinate generation, such as whether
 * texture coordinate generation is enabled, coordinate format
 * (2D or 3D coordinates), coordinate generation mode (object
 * linear, eye linear, or spherical reflection mapping), and the
 * R, S, and T coordinate plane equations. These attributes
 * are defined in a TexCoordGeneration object.</li><p>
 * </ul>
 *
 * @see Appearance
 * @see Texture
 * @see TextureAttributes
 * @see TexCoordGeneration
 *
 * @since Java 3D 1.2
 */
public class TextureUnitState extends NodeComponent {

    /**
     * Specifies that this TextureUnitState object allows reading its
     * texture, texture attribute, or texture coordinate generation
     * component information.
     */
    public static final int ALLOW_STATE_READ =
	CapabilityBits.TEXTURE_UNIT_STATE_ALLOW_STATE_READ;

    /**
     * Specifies that this TextureUnitState object allows writing its
     * texture, texture attribute, or texture coordinate generation
     * component information.
     */
    public static final int ALLOW_STATE_WRITE =
	CapabilityBits.TEXTURE_UNIT_STATE_ALLOW_STATE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_STATE_READ
    };

    /**
     * Constructs a TextureUnitState component object using defaults for all
     * state variables. All component object references are initialized
     * to null.
     */
    public TextureUnitState() {
	// Just use default values
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Constructs a TextureUnitState component object using the specified
     * component objects.
     *
     * @param texture object that specifies the desired texture
     * map and texture parameters
     * @param textureAttributes object that specifies the desired
     * texture attributes
     * @param texCoordGeneration object that specifies the texture coordinate
     * generation parameters
     */
    public TextureUnitState(Texture texture,
			     TextureAttributes textureAttributes,
			     TexCoordGeneration texCoordGeneration) {
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);

	((TextureUnitStateRetained)this.retained).initTexture(texture);
	((TextureUnitStateRetained)this.retained).initTextureAttributes(
							textureAttributes);
	((TextureUnitStateRetained)this.retained).initTexCoordGeneration(
							texCoordGeneration);
    }

    /**
     * Creates the retained mode TextureUnitStateRetained object that this
     * TextureUnitState component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new TextureUnitStateRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the texture, texture attributes, and texture coordinate
     * generation components in this TextureUnitState object to the
     * specified component objects.
     *
     * @param texture object that specifies the desired texture
     * map and texture parameters
     * @param textureAttributes object that specifies the desired
     * texture attributes
     * @param texCoordGeneration object that specifies the texture coordinate
     * generation parameters
     *
     * @exception IllegalSharingException if this TextureUnitState is live and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this TextureUnitState is
     * being used by an immediate mode context and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     */
    public void set(Texture texture,
		    TextureAttributes textureAttributes,
		    TexCoordGeneration texCoordGeneration) {

	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState0"));

        // Do illegal sharing check
        if(texture != null) {
            TextureRetained texRetained = (TextureRetained)texture.retained;
            ImageComponent[] images = texRetained.getImages();
            if(images != null) {
                for(int i=0; i<images.length; i++) {
                    validateImageIllegalSharing(images[i]);
                }
            }
        }

	((TextureUnitStateRetained)this.retained).setTextureUnitState(
				texture, textureAttributes, texCoordGeneration);
    }

    /**
     * Sets the texture object to the specified object.
     * Setting it to null disables texture mapping for the
     * texture unit corresponding to this TextureUnitState object.
     *
     * @param texture object that specifies the desired texture
     * map and texture parameters
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalSharingException if this TextureUnitState is live and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this TextureUnitState is
     * being used by an immediate mode context and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     */
    public void setTexture(Texture texture) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState0"));

        // Do illegal sharing check
        if(texture != null) {
            TextureRetained texRetained = (TextureRetained)texture.retained;
            ImageComponent[] images = texRetained.getImages();
            if(images != null) {
                for(int i=0; i<images.length; i++) {
                    validateImageIllegalSharing(images[i]);
                }
            }
        }

	((TextureUnitStateRetained)this.retained).setTexture(texture);
    }

    /**
     * Retrieves the current texture object.
     * @return the texture object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Texture getTexture() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState1"));

	return ((TextureUnitStateRetained)this.retained).getTexture();
    }

    /**
     * Sets the textureAttributes object to the specified object.
     * Setting it to null will result in default attribute usage for the.
     * texture unit corresponding to this TextureUnitState object.
     * @param textureAttributes object that specifies the desired
     * texture attributes
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setTextureAttributes(TextureAttributes textureAttributes) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState2"));

	((TextureUnitStateRetained)this.retained).setTextureAttributes(textureAttributes);
    }

    /**
     * Retrieves the current textureAttributes object.
     * @return the textureAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public TextureAttributes getTextureAttributes() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState3"));

	return ((TextureUnitStateRetained)this.retained).getTextureAttributes();
    }

    /**
     * Sets the texCoordGeneration object to the specified object.
     * Setting it to null disables texture coordinate generation for the
     * texture unit corresponding to this TextureUnitState object.
     * @param texCoordGeneration object that specifies the texture coordinate
     * generation parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setTexCoordGeneration(TexCoordGeneration texCoordGeneration) {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState4"));

	((TextureUnitStateRetained)this.retained).setTexCoordGeneration(texCoordGeneration);
    }

    /**
     * Retrieves the current texCoordGeneration object.
     * @return the texCoordGeneration object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public TexCoordGeneration getTexCoordGeneration() {
	if (isLiveOrCompiled())
	    if (!this.getCapability(ALLOW_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("TextureUnitState5"));

	return ((TextureUnitStateRetained)this.retained).getTexCoordGeneration();
    }


    /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
    public NodeComponent cloneNodeComponent() {
        TextureUnitState ts = new TextureUnitState();
        ts.duplicateNodeComponent(this);
        return ts;
    }

    /**
     * NOTE: Applications should <i>not</i> call this method directly.
     * It should only be called by the cloneNode method.
     *
     * @deprecated replaced with duplicateNodeComponent(
     *  NodeComponent originalNodeComponent, boolean forceDuplicate)
     */
    @Override
    public void duplicateNodeComponent(NodeComponent originalNodeComponent) {
	checkDuplicateNodeComponent(originalNodeComponent);
    }

    /**
     * Copies all TextureUnitState information from
     * <code>originalNodeComponent</code> into
     * the current node.  This method is called from the
     * <code>cloneNode</code> method which is, in turn, called by the
     * <code>cloneTree</code> method.<P>
     *
     * @param originalNodeComponent the original node to duplicate.
     * @param forceDuplicate when set to <code>true</code>, causes the
     *  <code>duplicateOnCloneTree</code> flag to be ignored.  When
     *  <code>false</code>, the value of each node's
     *  <code>duplicateOnCloneTree</code> variable determines whether
     *  NodeComponent data is duplicated or copied.
     *
     * @exception RestrictedAccessException if this object is part of a live
     *  or compiled scenegraph.
     *
     * @see Node#cloneTree
     * @see NodeComponent#setDuplicateOnCloneTree
     */
    @Override
    void duplicateAttributes(NodeComponent originalNodeComponent,
			     boolean forceDuplicate) {
	super.duplicateAttributes(originalNodeComponent, forceDuplicate);

	Hashtable hashtable = originalNodeComponent.nodeHashtable;

	TextureUnitStateRetained app = (TextureUnitStateRetained) originalNodeComponent.retained;

	TextureUnitStateRetained rt = (TextureUnitStateRetained) retained;

	rt.setTexture((Texture) getNodeComponent(app.getTexture(),
						 forceDuplicate,
						 hashtable));

	rt.setTextureAttributes((TextureAttributes) getNodeComponent(
						app.getTextureAttributes(),
						forceDuplicate,
						hashtable));

	rt.setTexCoordGeneration((TexCoordGeneration) getNodeComponent(
						app.getTexCoordGeneration(),
						forceDuplicate,
						hashtable));
    }

    /**
     *  This function is called from getNodeComponent() to see if any of
     *  the sub-NodeComponents  duplicateOnCloneTree flag is true.
     *  If it is the case, current NodeComponent needs to
     *  duplicate also even though current duplicateOnCloneTree flag is false.
     *  This should be overwrite by NodeComponent which contains sub-NodeComponent.
     */
    @Override
    boolean duplicateChild() {
	if (getDuplicateOnCloneTree())
	    return true;

	TextureUnitStateRetained rt = (TextureUnitStateRetained) retained;

	NodeComponent nc = rt.getTexture();
	if ((nc != null) && nc.duplicateChild())
	    return true;

	nc = rt.getTextureAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getTexCoordGeneration();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	return false;
    }
}
