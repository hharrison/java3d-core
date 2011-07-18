/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
 * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
 * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
 * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
 * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
 * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
 * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 *
 */

package com.sun.j3d.utils.geometry;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import javax.media.j3d.Appearance;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;

/**
 * A Text2D object is a representation of a string as a texture mapped
 * rectangle.  The texture for the rectangle shows the string as rendered in
 * the specified color with a transparent background.  The appearance of the
 * characters is specified using the font indicated by the font name, size
 * and style (see java.awt.Font).  The approximate height of the rendered
 * string will be the font size times the rectangle scale factor, which has a
 * default value of 1/256.  For example, a 12 point font will produce
 * characters that are about 12/256 = 0.047 meters tall. The lower left
 * corner of the rectangle is located at (0,0,0) with the height
 * extending along the positive y-axis and the width extending along the
 * positive x-axis.
 */
public class Text2D extends Shape3D {

    // This table caches FontMetrics objects to avoid the huge cost
    // of re-retrieving metrics for a font we've already seen.
    private static Hashtable metricsTable = new Hashtable(); 
    private float rectangleScaleFactor = 1f/256f;
    
    private boolean enableTextureWrite = false; 

    private Color3f   color = new Color3f();
    private String    fontName;
    private int       fontSize, fontStyle;
    private String text;
    
    // max texture dimension, as some font size can be greater than
    // video card max texture size. 2048 is a conservative value.
    private int MAX_TEXTURE_DIM = 2048; 

    // vWidth is the virtual width texture. Value set by setupImage()
    private int vWidth;
    // vHeight is the virtual height texture. Value set by setupImage()
    private int vHeight;


    /**
     * Creates a Shape3D object which holds a
     * rectangle that is texture-mapped with an image that has
     * the specified text written with the specified font
     * parameters.
     *
     * @param text The string to be written into the texture map.
     * @param color The color of the text string.
     * @param fontName The name of the Java font to be used for
     *  the text string.
     * @param fontSize The size of the Java font to be used.
     * @param fontStyle The style of the Java font to be used.
     */
    public Text2D(String text, Color3f color, String fontName,
		  int fontSize, int fontStyle) {

        this.color.set(color);
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
	this.text = text;
	    setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	    setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	    setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

        updateText2D(text, color, fontName, fontSize, fontStyle);
    }

    // issue 655
    private Text2D() {
    	
	    setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	    setCapability(Shape3D.ALLOW_APPEARANCE_READ);
	    setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
    }
    
    // issue 655
    public Node cloneNode(boolean forceDuplicate) {
        Text2D t2d = new Text2D();

        t2d.color.set(color);
        t2d.fontName = fontName;
        t2d.fontSize = fontSize;
        t2d.fontStyle = fontStyle;
	    t2d.text = text;

        t2d.duplicateNode(this, forceDuplicate);
        return t2d;
    }
    
    /*
     * Changes text of this Text2D to 'text'. All other
     * parameters (color, fontName, fontSize, fontStyle
     * remain the same.
     * @param text The string to be set.
     */
    public void setString(String text){
	this.text = text;
	
	Texture tex = getAppearance().getTexture();
	
	// mcneillk: JAVA3D-657
	if (tex == null) {
		tex = getAppearance().getTextureUnitState(0).getTexture();
	}
	
	int width = tex.getWidth();
	int height = tex.getHeight();	
    int oldVW = vWidth; 
    int oldVH = vHeight;

        ImageComponent imageComponent = setupImage(text, color, fontName,
                                          fontSize, fontStyle);
	if ((imageComponent.getWidth() == width) &&
	    (imageComponent.getHeight() == height)) {
	    tex.setImage(0, imageComponent);
	} else {
	    Texture2D newTex = setupTexture(imageComponent);
	    // Copy texture attributes except those related to
	    // mipmap since Texture only set base imageComponent. 

	    newTex.setBoundaryModeS(tex.getBoundaryModeS());
	    newTex.setBoundaryModeT(tex.getBoundaryModeT());
	    newTex.setMinFilter(tex.getMinFilter());
	    newTex.setMagFilter(tex.getMagFilter());      
	    newTex.setEnable(tex.getEnable());
	    newTex.setAnisotropicFilterMode(tex.getAnisotropicFilterMode());
	    newTex.setAnisotropicFilterDegree(tex.getAnisotropicFilterDegree());
	    int pcount = tex.getFilter4FuncPointsCount();
	    if (pcount > 0) {
		float weights[] = new float[pcount];
		tex.getFilter4Func(weights);
		newTex.setFilter4Func(weights);
	    }
	    Color4f c = new Color4f();
	    tex.getBoundaryColor(c);
	    newTex.setBoundaryColor(c);      
	    newTex.setUserData(tex.getUserData());
	    
	    // mcneillk: JAVA3D-657
	    if (getAppearance().getTexture() != null) {
		    getAppearance().setTexture(newTex);			
		} else {
			getAppearance().getTextureUnitState(0).setTexture(newTex);					
		}
	}
	// Does the new text requires a new geometry ?
	if ( oldVH != vHeight || oldVW != vWidth){
		QuadArray rect = setupGeometry(vWidth, vHeight);
		setGeometry(rect);
	}
    }

    private void updateText2D(String text, Color3f color, String fontName,
                  int fontSize, int fontStyle) {
        ImageComponent imageComponent = setupImage(text, color, fontName,
                                          fontSize, fontStyle);

        Texture2D t2d = setupTexture(imageComponent);

	QuadArray rect = setupGeometry(vWidth, vHeight);
	setGeometry(rect);

    	Appearance appearance = setupAppearance(t2d);
	setAppearance(appearance);
    }


    /**
     * Sets the scale factor used in converting the image width/height
     * to width/height values in 3D.
     *
     * @param newScaleFactor The new scale factor.
     */
    public void setRectangleScaleFactor(float newScaleFactor) {
	rectangleScaleFactor = newScaleFactor;
	updateText2D(text, color, fontName, fontSize, fontStyle);
    }

    /**
     * Gets the current scale factor being used in converting the image
     * width/height to width/height values in 3D.
     *
     * @return The current scale factor.
     */
    public float getRectangleScaleFactor() {
	return rectangleScaleFactor;
    }
    
    /**
     * Create the ImageComponent and Texture object.
     */
    private Texture2D setupTexture(ImageComponent imageComponent) {
	Texture2D t2d = new Texture2D(Texture2D.BASE_LEVEL,
				      Texture.RGBA,
				      imageComponent.getWidth(),
				      imageComponent.getHeight());
	t2d.setMinFilter(Texture2D.BASE_LEVEL_LINEAR);
	t2d.setMagFilter(Texture2D.BASE_LEVEL_LINEAR);
	t2d.setImage(0, imageComponent);
	t2d.setEnable(true);
	t2d.setCapability(Texture.ALLOW_IMAGE_WRITE);
	t2d.setCapability(Texture.ALLOW_SIZE_READ);
	t2d.setCapability(Texture.ALLOW_ENABLE_READ);
	t2d.setCapability(Texture.ALLOW_BOUNDARY_MODE_READ);
	t2d.setCapability(Texture.ALLOW_FILTER_READ); 
	t2d.setCapability(Texture.ALLOW_BOUNDARY_COLOR_READ); 
	t2d.setCapability(Texture.ALLOW_ANISOTROPIC_FILTER_READ); 
	t2d.setCapability(Texture.ALLOW_FILTER4_READ);
	return t2d;
    }

    /**
     * Creates a ImageComponent2D of the correct dimensions for the
     * given font attributes.  Draw the given text into the image in
     * the given color.  The background of the image is transparent
     * (alpha = 0).
     */
    private ImageComponent setupImage(String text, Color3f color,
				     String fontName,
				     int fontSize, int fontStyle) {
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Font font = new java.awt.Font(fontName, fontStyle, fontSize);

	FontMetrics metrics;
	if ((metrics = (FontMetrics)metricsTable.get(font)) == null) {
	    metrics = toolkit.getFontMetrics(font);
	    metricsTable.put(font, metrics);
	}
	int width = metrics.stringWidth(text);
	int descent = metrics.getMaxDescent();
	int ascent = metrics.getMaxAscent();
	int leading = metrics.getLeading();
	int height = descent + ascent;

	// Need to make width/height powers of 2 because of Java3d texture
	// size restrictions
	int pow = 1;
	for (int i = 1; i < 32; ++i) {
	    pow *= 2;
	    if (width <= pow)
		break;
	}
	width = Math.max (width, pow);
	pow = 1;
	for (int i = 1; i < 32; ++i) {
	    pow *= 2;
	    if (height <= pow)
		break;
	}
	height = Math.max (height, pow);

	// For now, jdk 1.2 only handles ARGB format, not the RGBA we want
	BufferedImage bImage = new BufferedImage(width, height,
						 BufferedImage.TYPE_INT_ARGB);
	Graphics2D offscreenGraphics = bImage.createGraphics();

	// First, erase the background to the text panel - set alpha to 0
	Color myFill = new Color(0f, 0f, 0f, 0f);
	offscreenGraphics.setColor(myFill);
	offscreenGraphics.fillRect(0, 0, width, height);

	// Next, set desired text properties (font, color) and draw String
	offscreenGraphics.setFont(font);
	Color myTextColor = new Color(color.x, color.y, color.z, 1f);
	offscreenGraphics.setColor(myTextColor);
	offscreenGraphics.drawString(text, 0, height - descent);
	offscreenGraphics.dispose();
	//store virtual size	
	vWidth = width;
	vHeight = height;
	// rescale down big images
    if(width > MAX_TEXTURE_DIM || height > MAX_TEXTURE_DIM){
    	bImage = rescaleImage(bImage);    	
    }

	ImageComponent imageComponent =
	    new ImageComponent2D(ImageComponent.FORMAT_RGBA, 
				 bImage);

	imageComponent.setCapability(ImageComponent.ALLOW_SIZE_READ);

	return imageComponent;
    }
    // rescale image
    private BufferedImage rescaleImage(BufferedImage bImage){
    	int width = bImage.getWidth();
    	int height = bImage.getHeight();
    	
    	float sx = (width > MAX_TEXTURE_DIM) ? (float) MAX_TEXTURE_DIM / (float)width  : 1.0f;
    	float sy = (height > MAX_TEXTURE_DIM)? (float) MAX_TEXTURE_DIM / (float)height : 1.0f;
    	    	    	
    	width = Math.round((float) width * sx);
    	height = Math.round((float)height * sy);
    	
    	Image scaledImage = bImage.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
    	bImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    	Graphics2D g = bImage.createGraphics();
    	g.drawImage(scaledImage, 0,0, null);
    	g.dispose();
    	
    	return  bImage;
    }

    /**
     * Creates a rectangle of the given width and height and sets up
     * texture coordinates to map the text image onto the whole surface
     * of the rectangle (the rectangle is the same size as the text image)
     */
    private QuadArray setupGeometry(int width, int height) {
	float zPosition = 0f;
	float rectWidth = (float)width * rectangleScaleFactor;
	float rectHeight = (float)height * rectangleScaleFactor;
	float[] verts1 = {
	    rectWidth, 0f, zPosition,
	    rectWidth, rectHeight, zPosition,
	    0f, rectHeight, zPosition,
	    0f, 0f, zPosition
	};
	float[] texCoords = {
	    0f, -1f,
	    0f, 0f,
	    (-1f), 0f,
	    (-1f), -1f
	};
	
	QuadArray rect = new QuadArray(4, QuadArray.COORDINATES |
				       QuadArray.TEXTURE_COORDINATE_2);
	rect.setCoordinates(0, verts1);
	rect.setTextureCoordinates(0, 0, texCoords);
	
	return rect;
    }

    /**
     * Creates Appearance for this Shape3D.  This sets transparency
     * for the object (we want the text to be "floating" in space,
     * so only the text itself should be non-transparent.  Also, the
     * appearance disables lighting for the object; the text will
     * simply be colored, not lit.
     */
    private Appearance setupAppearance(Texture2D t2d) {
    Appearance appearance = getAppearance();
    
    if (appearance == null) {
	TransparencyAttributes transp = new TransparencyAttributes();
	transp.setTransparencyMode(TransparencyAttributes.BLENDED);
	transp.setTransparency(0f);
			appearance = new Appearance();
	appearance.setTransparencyAttributes(transp);
	appearance.setTexture(t2d);

	Material m = new Material();
	m.setLightingEnable(false);
	appearance.setMaterial(m);
			appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
			appearance.setCapability(Appearance.ALLOW_TEXTURE_READ);
			appearance.setCapabilityIsFrequent(Appearance.ALLOW_TEXTURE_READ);
		}else{			
			appearance.setTexture(t2d);
		}
	
	return appearance;
    }

    /**
     * Returns the text string
     *
     * @since Java 3D 1.2.1
     */
    public String getString() {
	return text;
    }

    /**
     * Returns the color of the text
     *
     * @since Java 3D 1.2.1
     */
    public Color3f getColor() {
	return color;
    }

    /**
     * Returns the font
     *
     * @since Java 3D 1.2.1
     */
    public String getFontName() {
	return fontName;
    }

    /**
     * Returns the font size
     *
     * @since Java 3D 1.2.1
     */
    public int getFontSize() {
	return fontSize;
    }
    
    /**
     * Returns the font style
     *
     * @since Java 3D 1.2.1
     */
    public int getFontStyle() {
	return fontStyle;
    }
    
}





