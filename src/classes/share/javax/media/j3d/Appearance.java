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

import java.util.Hashtable;

/**
 * The Appearance object defines all rendering state that can be set
 * as a component object of a Shape3D node. The rendering state
 * consists of the following:<p>
 * <ul>
 * <li>Coloring attributes - defines attributes used in color selection
 * and shading. These attributes are defined in a ColoringAttributes
 * object.</li><p>
 *
 * <li>Line attributes - defines attributes used to define lines, including
 * the pattern, width, and whether antialiasing is to be used. These
 * attributes are defined in a LineAttributes object.</li><p>
 *
 * <li>Point attributes - defines attributes used to define points,
 * including the size and whether antialiasing is to be used. These
 * attributes are defined in a PointAttributes object.</li><p>
 *
 * <li>Polygon attributes - defines the attributes used to define
 * polygons, including culling, rasterization mode (filled, lines,
 * or points), constant offset, offset factor, and whether back
 * back facing normals are flipped. These attributes are defined
 * in a PolygonAttributes object.</li><p>
 *
 * <li>Rendering attributes - defines rendering operations,
 * including the alpha test function and test value, the raster
 * operation, whether vertex colors are ignored, whether invisible
 * objects are rendered, and whether the depth buffer is enabled.
 * These attributes are defined in a RenderingAttributes
 * object.</li><p>
 *
 * <li>Transparency attributes - defines the attributes that affect
 * transparency of the object, such as the transparency mode
 * (blended, screen-door), blending function (used in transparency
 * and antialiasing operations), and a blend value that defines
 * the amount of transparency to be applied to this Appearance
 * component object.</li><p>
 *
 * <li>Material - defines the appearance of an object under illumination,
 * such as the ambient color, diffuse color, specular color, emissive
 * color, and shininess. These attributes are defined in a Material
 * object.</li><p>
 *
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
 *
 * <li>Texture unit state - array that defines texture state for each
 * of <i>N</i> separate texture units.  This allows multiple textures
 * to be applied to geometry.  Each TextureUnitState object contains a
 * Texture object, TextureAttributes, and TexCoordGeneration object
 * for one texture unit.  If the length of the texture unit state
 * array is greater than 0, then the array is used for all texture
 * state; the individual Texture, TextureAttributes, and
 * TexCoordGeneration objects in this Appearance object are not used
 * and and must not be set by an application. If the length of the
 * texture unit state array is 0, the multi-texture is disabled and
 * the Texture, TextureAttributes, and TexCoordGeneration objects
 * in the Appearance object are used. If the application sets the
 * existing Texture, TextureAttributes, and TexCoordGeneration
 * objects to non-null values, they effectively define the state
 * for texture unit 0. If the TextureUnitState array is set to a
 * non-null, non-empty array, the individual TextureUnitState
 * objects define the state for texture units 0 through <i>n</i>
 * -1. If both the old and new values are set, an exception is thrown.
 *
 * </li>
 * </ul>
 *
 * @see ColoringAttributes
 * @see LineAttributes
 * @see PointAttributes
 * @see PolygonAttributes
 * @see RenderingAttributes
 * @see TransparencyAttributes
 * @see Material
 * @see Texture
 * @see TextureAttributes
 * @see TexCoordGeneration
 * @see TextureUnitState
 */
public class Appearance extends NodeComponent {

  /**
   * Specifies that this Appearance object
   * allows reading its coloringAttributes component
   * information.
   */
  public static final int
    ALLOW_COLORING_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_COLORING_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its coloringAttributes component
   * information.
   */
  public static final int
    ALLOW_COLORING_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_COLORING_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its transparency component
   * information.
   */
  public static final int
    ALLOW_TRANSPARENCY_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_TRANSPARENCY_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its transparency component
   * information.
   */
  public static final int
    ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its rendering/rasterization component
   * information.
   */
  public static final int
    ALLOW_RENDERING_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_RENDERING_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its rendering/rasterization component
   * information.
   */
  public static final int
    ALLOW_RENDERING_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_RENDERING_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its polygon component
   * information.
   */
  public static final int
    ALLOW_POLYGON_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_POLYGON_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its polygon component
   * information.
   */
  public static final int
    ALLOW_POLYGON_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_POLYGON_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its line component
   * information.
   */
  public static final int
    ALLOW_LINE_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_LINE_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its line component
   * information.
   */
  public static final int
    ALLOW_LINE_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_LINE_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its point component
   * information.
   */
  public static final int
    ALLOW_POINT_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_POINT_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its point component
   * information.
   */
  public static final int
    ALLOW_POINT_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_POINT_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its material component information.
   */
  public static final int
    ALLOW_MATERIAL_READ = CapabilityBits.APPEARANCE_ALLOW_MATERIAL_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its material component information.
   */
  public static final int
    ALLOW_MATERIAL_WRITE = CapabilityBits.APPEARANCE_ALLOW_MATERIAL_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its texture component information.
   */
  public static final int
    ALLOW_TEXTURE_READ = CapabilityBits.APPEARANCE_ALLOW_TEXTURE_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its texture component information.
   */
  public static final int
    ALLOW_TEXTURE_WRITE = CapabilityBits.APPEARANCE_ALLOW_TEXTURE_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its textureAttributes component
   * information.
   */
  public static final int
    ALLOW_TEXTURE_ATTRIBUTES_READ = CapabilityBits.APPEARANCE_ALLOW_TEXTURE_ATTRIBUTES_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its textureAttributes component
   * information.
   */
  public static final int
    ALLOW_TEXTURE_ATTRIBUTES_WRITE = CapabilityBits.APPEARANCE_ALLOW_TEXTURE_ATTRIBUTES_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its texture coordinate generation component
   * information.
   */
  public static final int
    ALLOW_TEXGEN_READ = CapabilityBits.APPEARANCE_ALLOW_TEXGEN_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its texture coordinate generation component
   * information.
   */
  public static final int
    ALLOW_TEXGEN_WRITE = CapabilityBits.APPEARANCE_ALLOW_TEXGEN_WRITE;

  /**
   * Specifies that this Appearance object
   * allows reading its texture unit state component
   * information.
   *
   * @since Java 3D 1.2
   */
  public static final int ALLOW_TEXTURE_UNIT_STATE_READ =
    CapabilityBits.APPEARANCE_ALLOW_TEXTURE_UNIT_STATE_READ;

  /**
   * Specifies that this Appearance object
   * allows writing its texture unit state  component
   * information.
   *
   * @since Java 3D 1.2
   */
  public static final int ALLOW_TEXTURE_UNIT_STATE_WRITE =
    CapabilityBits.APPEARANCE_ALLOW_TEXTURE_UNIT_STATE_WRITE;

   // Array for setting default read capabilities
    private static final int[] readCapabilities = {
        ALLOW_COLORING_ATTRIBUTES_READ,
        ALLOW_LINE_ATTRIBUTES_READ,
        ALLOW_MATERIAL_READ,
        ALLOW_POINT_ATTRIBUTES_READ,
        ALLOW_POLYGON_ATTRIBUTES_READ,
        ALLOW_RENDERING_ATTRIBUTES_READ,
        ALLOW_TEXGEN_READ,
        ALLOW_TEXTURE_ATTRIBUTES_READ,
        ALLOW_TEXTURE_READ,
        ALLOW_TEXTURE_UNIT_STATE_READ,
        ALLOW_TRANSPARENCY_ATTRIBUTES_READ
    };

    /**
     * Constructs an Appearance component object using defaults for all
     * state variables. All component object references are initialized
     * to null.
     */
    public Appearance() {
	// Just use default values
        // set default read capabilities
        setDefaultReadCapabilities(readCapabilities);
    }

    /**
     * Creates the retained mode AppearanceRetained object that this
     * Appearance component object will point to.
     */
    @Override
    void createRetained() {
	this.retained = new AppearanceRetained();
	this.retained.setSource(this);
    }

    /**
     * Sets the material object to the specified object.
     * Setting it to null disables lighting.
     * @param material object that specifies the desired material
     * properties
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setMaterial(Material material) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_MATERIAL_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance0"));
	((AppearanceRetained)this.retained).setMaterial(material);
    }

    /**
     * Retrieves the current material object.
     * @return the material object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Material getMaterial() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_MATERIAL_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance1"));
	return ((AppearanceRetained)this.retained).getMaterial();
    }

    /**
     * Sets the coloringAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param coloringAttributes object that specifies the desired
     * coloringAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setColoringAttributes(ColoringAttributes coloringAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_COLORING_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance6"));
	((AppearanceRetained)this.retained).setColoringAttributes(coloringAttributes);
    }

    /**
     * Retrieves the current coloringAttributes object.
     * @return the coloringAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public ColoringAttributes getColoringAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_COLORING_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance7"));
	return ((AppearanceRetained)this.retained).getColoringAttributes();
    }

    /**
     * Sets the transparencyAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param transparencyAttributes object that specifies the desired
     * transparencyAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setTransparencyAttributes(TransparencyAttributes transparencyAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance8"));
	((AppearanceRetained)this.retained).setTransparencyAttributes(transparencyAttributes);
    }

    /**
     * Retrieves the current transparencyAttributes object.
     * @return the transparencyAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public TransparencyAttributes getTransparencyAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TRANSPARENCY_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance9"));
	return ((AppearanceRetained)this.retained).getTransparencyAttributes();
    }

    /**
     * Sets the renderingAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param renderingAttributes object that specifies the desired
     * renderingAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setRenderingAttributes(RenderingAttributes renderingAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_RENDERING_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance10"));
	((AppearanceRetained)this.retained).setRenderingAttributes(renderingAttributes);
    }

    /**
     * Retrieves the current renderingAttributes object.
     * @return the renderingAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public RenderingAttributes getRenderingAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_RENDERING_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance11"));
	return ((AppearanceRetained)this.retained).getRenderingAttributes();
    }

    /**
     * Sets the polygonAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param polygonAttributes object that specifies the desired
     * polygonAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPolygonAttributes(PolygonAttributes polygonAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_POLYGON_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance12"));
	((AppearanceRetained)this.retained).setPolygonAttributes(polygonAttributes);
    }

    /**
     * Retrieves the current polygonAttributes object.
     * @return the polygonAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public PolygonAttributes getPolygonAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_POLYGON_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance13"));
	return ((AppearanceRetained)this.retained).getPolygonAttributes();
    }

    /**
     * Sets the lineAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param lineAttributes object that specifies the desired
     * lineAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setLineAttributes(LineAttributes lineAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_LINE_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance14"));
	((AppearanceRetained)this.retained).setLineAttributes(lineAttributes);
    }

    /**
     * Retrieves the current lineAttributes object.
     * @return the lineAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public LineAttributes getLineAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_LINE_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance15"));
	return ((AppearanceRetained)this.retained).getLineAttributes();
    }

    /**
     * Sets the pointAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     * @param pointAttributes object that specifies the desired
     * pointAttributes parameters
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public void setPointAttributes(PointAttributes pointAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_POINT_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance16"));
	((AppearanceRetained)this.retained).setPointAttributes(pointAttributes);
    }

    /**
     * Retrieves the current pointAttributes object.
     * @return the pointAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public PointAttributes getPointAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_POINT_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance17"));
	return ((AppearanceRetained)this.retained).getPointAttributes();
    }

    /**
     * Sets the texture object to the specified object.
     * Setting it to null disables texture mapping.
     *
     * <p>
     * Applications must not set individual texture component objects
     * (texture, textureAttributes, or texCoordGeneration) and
     * the texture unit state array in the same Appearance object.
     * Doing so will result in an exception being thrown.
     *
     * @param texture object that specifies the desired texture
     * map and texture parameters
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the specified texture
     * object is non-null and the texture unit state array in this
     * appearance object is already non-null.
     *
     * @exception IllegalSharingException if this Appearance is live and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this Appearance is
     * being used by an immediate mode context and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     */
    public void setTexture(Texture texture) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance2"));

        // Do illegal sharing check
        if(texture != null) {
            ImageComponent[] images = ((TextureRetained)(texture.retained)).getImages();
            if(images != null) {
                for(int i=0; i<images.length; i++) {
                    validateImageIllegalSharing(images[i]);
                }
            }
        }

        ((AppearanceRetained)this.retained).setTexture(texture);
    }

    /**
     * Retrieves the current texture object.
     * @return the texture object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public Texture getTexture() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance3"));
	return ((AppearanceRetained)this.retained).getTexture();
    }

    /**
     * Sets the textureAttributes object to the specified object.
     * Setting it to null will result in default attribute usage.
     *
     * <p>
     * Applications must not set individual texture component objects
     * (texture, textureAttributes, or texCoordGeneration) and
     * the texture unit state array in the same Appearance object.
     * Doing so will result in an exception being thrown.
     *
     * @param textureAttributes object that specifies the desired
     * textureAttributes map and textureAttributes parameters
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the specified textureAttributes
     * object is non-null and the texture unit state array in this
     * appearance object is already non-null.
     */
    public void setTextureAttributes(TextureAttributes textureAttributes) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_ATTRIBUTES_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance4"));
	((AppearanceRetained)this.retained).setTextureAttributes(textureAttributes);
    }

    /**
     * Retrieves the current textureAttributes object.
     * @return the textureAttributes object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public TextureAttributes getTextureAttributes() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_ATTRIBUTES_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance5"));
	return ((AppearanceRetained)this.retained).getTextureAttributes();
    }

    /**
     * Sets the texCoordGeneration object to the specified object.
     * Setting it to null disables texture coordinate generation.
     *
     * <p>
     * Applications must not set individual texture component objects
     * (texture, textureAttributes, or texCoordGeneration) and
     * the texture unit state array in the same Appearance object.
     * Doing so will result in an exception being thrown.
     *
     * @param texCoordGeneration object that specifies the texture coordinate
     * generation parameters
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the specified texCoordGeneration
     * object is non-null and the texture unit state array in this
     * appearance object is already non-null.
     */
    public void setTexCoordGeneration(TexCoordGeneration texCoordGeneration) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXGEN_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance18"));
	((AppearanceRetained)this.retained).setTexCoordGeneration(texCoordGeneration);
    }

    /**
     * Retrieves the current texCoordGeneration object.
     * @return the texCoordGeneration object
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     */
    public TexCoordGeneration getTexCoordGeneration() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXGEN_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance19"));
	return ((AppearanceRetained)this.retained).getTexCoordGeneration();
    }

    /**
     * Sets the texture unit state array for this appearance object to the
     * specified array.  A shallow copy of the array of references to
     * the TextureUnitState objects is made.  If the specified array
     * is null or if the length of the array is 0, multi-texture is
     * disabled.  Within the array, a null TextureUnitState element
     * disables the corresponding texture unit.
     *
     * <p>
     * Applications must not set individual texture component objects
     * (texture, textureAttributes, or texCoordGeneration) and
     * the texture unit state array in the same Appearance object.
     * Doing so will result in an exception being thrown.
     *
     * @param stateArray array of TextureUnitState objects that
     * specify the desired texture state for each unit.  The length of
     * this array specifies the maximum number of texture units that
     * will be used by this appearance object.  The texture units are
     * numbered from <code>0</code> through
     * <code>stateArray.length-1</code>.
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @exception IllegalStateException if the specified array is
     * non-null and any of the texture object, textureAttributes
     * object, or texCoordGeneration object in this appearance object
     * is already non-null.
     *
     * @exception IllegalSharingException if this Appearance is live and
     * any of the specified textures refers to an ImageComponent2D that is
     * being used by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this Appearance is
     * being used by an immediate mode context and
     * any of the specified textures refers to an ImageComponent2D that is
     * being used by a Canvas3D as an off-screen buffer.
     *
     * @since Java 3D 1.2
     */
    public void setTextureUnitState(TextureUnitState[] stateArray) {
        if (isLiveOrCompiled())
            if (!this.getCapability(ALLOW_TEXTURE_UNIT_STATE_WRITE))
                throw new CapabilityNotSetException(J3dI18N.getString("Appearance20"));

        // Do illegal sharing check
        if (stateArray != null) {
            for(int j=0; j<stateArray.length; j++) {
                if(stateArray[j] != null) {
                    TextureRetained texRetained =
                            ((TextureUnitStateRetained)stateArray[j].retained).texture;
                    if(texRetained != null) {
                        ImageComponent[] images = texRetained.getImages();
                        if(images != null) {
                            for(int i=0; i<images.length; i++) {
                                validateImageIllegalSharing(images[i]);
                            }
                        }
                    }
                }
            }
        }

	((AppearanceRetained)this.retained).setTextureUnitState(stateArray);
    }

    /**
     * Sets the texture unit state object at the specified index
     * within the texture unit state array to the specified object.
     * If the specified object is null, the corresponding texture unit
     * is disabled.  The index must be within the range
     * <code>[0,&nbsp;stateArray.length-1]</code>.
     *
     * @param index the array index of the object to be set
     *
     * @param state new texture unit state object
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     * @exception NullPointerException if the texture unit state array is
     * null.
     * @exception ArrayIndexOutOfBoundsException if <code>index >=
     * stateArray.length</code>.
     *
     * @exception IllegalSharingException if this Appearance is live and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     *
     * @exception IllegalSharingException if this Appearance is
     * being used by an immediate mode context and
     * the specified texture refers to an ImageComponent2D that is being used
     * by a Canvas3D as an off-screen buffer.
     *
     * @since Java 3D 1.2
     */
    public void setTextureUnitState(int index, TextureUnitState state) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_UNIT_STATE_WRITE))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance20"));

        // Do illegal sharing check
        if (state != null) {
            TextureRetained texRetained =
                    ((TextureUnitStateRetained)state.retained).texture;
            if(texRetained != null) {
                ImageComponent[] images = texRetained.getImages();
                if(images != null) {
                    for(int i=0; i<images.length; i++) {
                        validateImageIllegalSharing(images[i]);
                    }
                }
            }
        }

	((AppearanceRetained)this.retained).setTextureUnitState(index, state);
    }

    /**
     * Retrieves the array of texture unit state objects from this
     * Appearance object.  A shallow copy of the array of references to
     * the TextureUnitState objects is returned.
     *
     * @return the array of texture unit state objects
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public TextureUnitState[] getTextureUnitState() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_UNIT_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance21"));

	return ((AppearanceRetained)this.retained).getTextureUnitState();
    }

    /**
     * Retrieves the texture unit state object at the specified
     * index within the texture unit state array.  The index must be
     * within the range <code>[0,&nbsp;stateArray.length-1]</code>.
     *
     * @param index the array index of the object to be retrieved
     *
     * @return the texture unit state object at the specified index
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public TextureUnitState getTextureUnitState(int index) {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_UNIT_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance21"));

	return ((AppearanceRetained)this.retained).getTextureUnitState(index);
    }

    /**
     * Retrieves the length of the texture unit state array from
     * this appearance object.  The length of this array specifies the
     * maximum number of texture units that will be used by this
     * appearance object.  If the array is null, a count of 0 is
     * returned.
     *
     * @return the length of the texture unit state array
     *
     * @exception CapabilityNotSetException if appropriate capability is
     * not set and this object is part of live or compiled scene graph
     *
     * @since Java 3D 1.2
     */
    public int getTextureUnitCount() {
	if (isLiveOrCompiled())
	  if (!this.getCapability(ALLOW_TEXTURE_UNIT_STATE_READ))
		throw new CapabilityNotSetException(J3dI18N.getString("Appearance21"));

	return ((AppearanceRetained)this.retained).getTextureUnitCount();
    }


   /**
     * @deprecated replaced with cloneNodeComponent(boolean forceDuplicate)
     */
    @Override
    public NodeComponent cloneNodeComponent() {
        Appearance a = new Appearance();
        a.duplicateNodeComponent(this);
        return a;
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
     * Copies all Appearance information from
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

	AppearanceRetained app = (AppearanceRetained) originalNodeComponent.retained;

	AppearanceRetained rt = (AppearanceRetained) retained;

	rt.setMaterial((Material) getNodeComponent(app.getMaterial(),
						forceDuplicate,
						hashtable));

	rt.setColoringAttributes((ColoringAttributes) getNodeComponent(
					    app.getColoringAttributes(),
					    forceDuplicate,
					    hashtable));


	rt.setTransparencyAttributes((TransparencyAttributes) getNodeComponent(
					    app.getTransparencyAttributes(),
					    forceDuplicate,
					    hashtable));


	rt.setRenderingAttributes((RenderingAttributes) getNodeComponent(
				      app.getRenderingAttributes(),
				      forceDuplicate,
				      hashtable));


	rt.setPolygonAttributes((PolygonAttributes) getNodeComponent(
					  app.getPolygonAttributes(),
					  forceDuplicate,
					  hashtable));


	rt.setLineAttributes((LineAttributes) getNodeComponent(
					    app.getLineAttributes(),
					    forceDuplicate,
					    hashtable));


	rt.setPointAttributes((PointAttributes) getNodeComponent(
					      app.getPointAttributes(),
					      forceDuplicate,
					      hashtable));

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

	TextureUnitState state[] = app.getTextureUnitState();
	if (state != null) {
	    rt.setTextureUnitState(state);
	    for (int i=0; i < state.length; i++) {
		rt.setTextureUnitState(i, (TextureUnitState)
				       getNodeComponent(state[i],
							forceDuplicate,
							hashtable));
	    }
	}

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

	AppearanceRetained rt = (AppearanceRetained) retained;

	NodeComponent nc;

	nc = rt.getMaterial();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getColoringAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getTransparencyAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getPolygonAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getLineAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getPointAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getTexture();
	if ((nc != null) && nc.duplicateChild())
	    return true;

	nc = rt.getTextureAttributes();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	nc = rt.getTexCoordGeneration();
	if ((nc != null) && nc.getDuplicateOnCloneTree())
	    return true;

	// XXXX: TextureUnitState

	return false;
    }

}
