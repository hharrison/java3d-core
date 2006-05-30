/*
 * $RCSfile$
 *
 * Copyright (c) 2006 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import javax.vecmath.Color4f;
import java.util.ArrayList;

/**
 * The TextureAttributes object defines attributes that apply to
 * to texture mapping.
 */
class TextureAttributesRetained extends NodeComponentRetained {

    // A list of pre-defined bits to indicate which component
    // in this TextureAttributes object changed.
    static final int TRANSFORM_CHANGED      		= 0x0001;
    static final int MODE_CHANGED      	        	= 0x0002;
    static final int COLOR_CHANGED      		= 0x0004;
    static final int CORRECTION_CHANGED      		= 0x0008;
    static final int TEXTURE_COLOR_TABLE_CHANGED	= 0x0010;
    static final int COMBINE_RGB_MODE_CHANGED   	= 0x0020;
    static final int COMBINE_ALPHA_MODE_CHANGED		= 0x0040;
    static final int COMBINE_RGB_SRC_CHANGED		= 0x0080;
    static final int COMBINE_ALPHA_SRC_CHANGED		= 0x0100;
    static final int COMBINE_RGB_FCN_CHANGED		= 0x0200;
    static final int COMBINE_ALPHA_FCN_CHANGED		= 0x0400;
    static final int COMBINE_RGB_SCALE_CHANGED		= 0x0800;
    static final int COMBINE_ALPHA_SCALE_CHANGED	= 0x1000;

    // static class variable for commands used in messages
    static Integer commandInt[] = null;

    // static class variable for enums. Currently only supports 0 - 9.
    static Integer enums[] = null;

    // Texture transform
    Transform3D	transform = new Transform3D();

    // Texture mode
    int	textureMode = TextureAttributes.REPLACE;

    // Texture blend color
    Color4f textureBlendColor = new Color4f(0.0f, 0.0f, 0.0f, 0.0f);

    // Texture color table
    int	textureColorTable[] = null;
    int	numTextureColorTableComponents = 0;
    int	textureColorTableSize = 0;

    // Texture Combine Mode

    int	combineRgbMode = TextureAttributes.COMBINE_MODULATE;
    int	combineAlphaMode = TextureAttributes.COMBINE_MODULATE;

    // the following fields are only applicable if textureMode specifies
    // COMBINE. If COMBINE mode is specified, then each of the following
    // fields will be referencing an array of 3 integers, each representing
    // an operand in the combine equation.
    int	[] combineRgbSrc = null;
    int	[] combineAlphaSrc = null;
    int	[] combineRgbFcn = null;
    int	[] combineAlphaFcn = null;

    int combineRgbScale = 1;
    int combineAlphaScale = 1;

    //Perspective correction mode, used for color/texCoord interpolation
    int perspCorrectionMode = TextureAttributes.NICEST;

    // true when mirror texCoord component set
    boolean mirrorCompDirty = false;

    static final void initTextureEnums() {
	// create some of the enums Integer to be used in the messages
	// this can be eliminated if the message is modified to take 
	// integer itself
	//
	// NOTE: check with the actual enum value before using this
	//       list. This list only supports 0 - 9
	if (enums == null) {
	    enums = new Integer[10];
	    for (int i = 0; i < 10; i++) {
		 enums[i] = new Integer(i);
	    }
	}
    }


    TextureAttributesRetained() {
	initTextureEnums();
    }

    // initCombineMode -- initializes the combine mode related fields
    //			  delay the allocation of memory to minimize
    //			  memory footprint

    final void initCombineMode(TextureAttributesRetained tr) {
	tr.combineRgbSrc   = new int[3];
	tr.combineAlphaSrc = new int[3];
	tr.combineRgbFcn   = new int[3];
	tr.combineAlphaFcn = new int[3];

	//default values

	tr.combineRgbSrc[0] = TextureAttributes.COMBINE_TEXTURE_COLOR;
	tr.combineRgbSrc[1] = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
	tr.combineRgbSrc[2] = TextureAttributes.COMBINE_CONSTANT_COLOR;

	tr.combineAlphaSrc[0] = TextureAttributes.COMBINE_TEXTURE_COLOR;
	tr.combineAlphaSrc[1] = TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE;
	tr.combineAlphaSrc[2] = TextureAttributes.COMBINE_CONSTANT_COLOR;

	tr.combineRgbFcn[0] = TextureAttributes.COMBINE_SRC_COLOR;
	tr.combineRgbFcn[1] = TextureAttributes.COMBINE_SRC_COLOR;
	tr.combineRgbFcn[2] = TextureAttributes.COMBINE_SRC_COLOR;

	tr.combineAlphaFcn[0] = TextureAttributes.COMBINE_SRC_ALPHA;
	tr.combineAlphaFcn[1] = TextureAttributes.COMBINE_SRC_ALPHA;
	tr.combineAlphaFcn[2] = TextureAttributes.COMBINE_SRC_ALPHA;
    }

    final void initTextureMode(int textureMode) {
	this.textureMode = textureMode;

	if (textureMode == TextureAttributes.COMBINE) {
	    if (combineRgbSrc == null) {
		initCombineMode(this);
	    }
	}
    }

    /**
     * Sets the texture  mode parameter for this
     * appearance component object.
     * @param textureMode the texture  mode, one of: MODULATE,
     * DECAL, BLEND, or REPLACE
     */
    final void setTextureMode(int textureMode) {
	initTextureMode(textureMode);
	sendMessage(MODE_CHANGED, enums[textureMode], null);
    }

    /**
     * Gets the texture  mode parameter for this
     * texture attributes object.
     * @return textureMode the texture  mode
     */
    final int getTextureMode() {
	return textureMode;
    }

    final void initTextureBlendColor(Color4f textureBlendColor) {
	this.textureBlendColor.set(textureBlendColor);

    }

    /**
     * Sets the texture blend color for this
     * texture attributes object.
     * @param textureBlendColor the texture blend color used when
     * the  mode is BLEND
     */
    final void setTextureBlendColor(Color4f textureBlendColor) {
	this.textureBlendColor.set(textureBlendColor);
	sendMessage(COLOR_CHANGED, new Color4f(textureBlendColor), null);
    }


    final void initTextureBlendColor(float r, float g, float b, float a) {
	this.textureBlendColor.set(r, g, b, a);
    }


    /**
     * Sets the texture blend color for this
     * appearance component object.  This color is used when
     * the  mode is BLEND.
     * @param r the red component of the color
     * @param g the green component of the color
     * @param b the blue component of the color
     * @param a the alpha component of the color
     */
    final void setTextureBlendColor(float r, float g, float b, float a) {
	this.textureBlendColor.set(r, g, b, a);
	sendMessage(COLOR_CHANGED, new Color4f(r, g, b, a), null);
    }


    /**
     * Gets the texture blend color for this
     * appearance component object.
     * @param textureBlendColor the vector that will receive the texture
     * blend color used when the  mode is BLEND
     */
    final void getTextureBlendColor(Color4f textureBlendColor) {
	textureBlendColor.set(this.textureBlendColor);
    }


    final void initTextureTransform(Transform3D transform) {
	this.transform.set(transform);
    }


    /**
     * Sets the texture transform object used to transform texture
     * coordinates.  A copy of the specified Transform3D object is
     * stored in this TextureAttributes object.
     * @param transform the new transform object
     */
    final void setTextureTransform(Transform3D transform) {
	this.transform.set(transform);
	sendMessage(TRANSFORM_CHANGED, 
		VirtualUniverse.mc.getTransform3D(transform), null);
    }


    /**
     * Retrieves a copy of the texture transform object.
     * @param transform the transform object that will receive the
     * current texture transform.
     */
    final void getTextureTransform(Transform3D transform) {
	transform.set(this.transform);
    }


    final void initPerspectiveCorrectionMode(int mode) {
	this.perspCorrectionMode = mode;
    }

    /**
     * Sets perspective correction mode to be used for color
     * and/or texture coordinate interpolation.
     * A value of NICEST indicates that perspective correction should be
     * performed and that the highest quality method should be used.
     * A value of FASTEST indicates that the most efficient perspective
     * correction method should be used.
     * @param mode one of NICEST or FASTEST.
     * The default value is NICEST.
     */
    final void setPerspectiveCorrectionMode(int mode) {
	this.perspCorrectionMode = mode;
	sendMessage(CORRECTION_CHANGED, enums[mode], null);
    }

    /**
     * Gets perspective correction mode value.
     * @return mode the value of perspective correction mode.
     */
    final int getPerspectiveCorrectionMode() {
	return perspCorrectionMode;
    }

    final void setTextureColorTable(int[][] table) {
	initTextureColorTable(table);

	//clone a copy of the texture for the mirror object
	if (table == null) {
	    sendMessage(TEXTURE_COLOR_TABLE_CHANGED, null, null);
	} else {
	    int ctable[] = new int[textureColorTableSize * 
					numTextureColorTableComponents];
	    System.arraycopy(textureColorTable, 0, ctable, 0,
				textureColorTable.length);
	    Object args[] = new Object[3];

            args[0] = new Integer(numTextureColorTableComponents);
            args[1] = new Integer(textureColorTableSize);
            args[2] = ctable;
	    sendMessage(TEXTURE_COLOR_TABLE_CHANGED, args, null);
	}
    }

    final void initTextureColorTable(int[][] table) {

	numTextureColorTableComponents = 0;
	textureColorTableSize = 0;

	if (table == null) {
	    textureColorTable = null;
	    return;
	}

	if (table.length < 3 || table.length > 4) {
            throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes13"));
	}

	if (Texture.getPowerOf2(table[0].length) == -1) {
            throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes14"));
	}

	for (int i = 1; i < table.length; i++) {
	     if (table[i].length != table[0].length)
                 throw new IllegalArgumentException(J3dI18N.getString("TextureAttributes15"));
	}

	numTextureColorTableComponents = table.length;
	textureColorTableSize = table[0].length;

	if (textureColorTable == null ||
		textureColorTable.length != numTextureColorTableComponents *
						textureColorTableSize) {
	    textureColorTable = new int[numTextureColorTableComponents *
					textureColorTableSize];
        }

	int k = 0;
        for (int i = 0; i < textureColorTableSize; i++) {
	     for (int j = 0; j < numTextureColorTableComponents; j++) {
		  textureColorTable[k++] = table[j][i];
	     }
	}
    }


    final void getTextureColorTable(int[][] table) {

	if (textureColorTable == null)
	    return;

	int k = 0;
        for (int i = 0; i < textureColorTableSize; i++) {
	     for (int j = 0; j < numTextureColorTableComponents; j++) {
		  table[j][i] = textureColorTable[k++];
	     }
	}
    }

    final int getNumTextureColorTableComponents() {
	return numTextureColorTableComponents;
    }

    final int getTextureColorTableSize() {
	return textureColorTableSize;
    }
    

    final void initCombineRgbMode(int mode) {
	combineRgbMode = mode;	
    }

    final void setCombineRgbMode(int mode) {
	initCombineRgbMode(mode);
	sendMessage(COMBINE_RGB_MODE_CHANGED, enums[mode], null);
    }

    final int getCombineRgbMode() {
	return combineRgbMode;
    }

    final void initCombineAlphaMode(int mode) {
        combineAlphaMode = mode;
    }

    final void setCombineAlphaMode(int mode) {
	initCombineAlphaMode(mode);
	sendMessage(COMBINE_ALPHA_MODE_CHANGED, enums[mode], null);
    }

    final int getCombineAlphaMode() {
	return combineAlphaMode;
    }

    final void initCombineRgbSource(int index, int src) {
	if (combineRgbSrc == null) {
	    // it is possible to set the combineRgbSource before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
	    initCombineMode(this);
	}
	combineRgbSrc[index] = src;
    }
 
    final void setCombineRgbSource(int index, int src) {
	initCombineRgbSource(index, src);
        sendMessage(COMBINE_RGB_SRC_CHANGED, enums[index], enums[src]);
    }

    final int getCombineRgbSource(int index) {
	if (combineRgbSrc == null) {
	    // it is possible to do a get before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
	    initCombineMode(this);
	}
  	return combineRgbSrc[index];
    }

    final void initCombineAlphaSource(int index, int src) {
	if (combineRgbSrc == null) {
	    // it is possible to set the combineAlphaSource before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
	    initCombineMode(this);
	}
	combineAlphaSrc[index] = src;
    }
 
    final void setCombineAlphaSource(int index, int src) {
	initCombineAlphaSource(index, src);
        sendMessage(COMBINE_ALPHA_SRC_CHANGED, enums[index], enums[src]);
    }

    final int getCombineAlphaSource(int index) {
        if (combineRgbSrc == null) {
            // it is possible to do a get before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
            initCombineMode(this);
        }
	return combineAlphaSrc[index];
    }

    final void initCombineRgbFunction(int index, int fcn) {
	if (combineRgbSrc == null) {
	    // it is possible to set the combineRgbFcn before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
	    initCombineMode(this);
	}
	combineRgbFcn[index] = fcn;
    }

    final void setCombineRgbFunction(int index, int fcn) {
	initCombineRgbFunction(index, fcn);
        sendMessage(COMBINE_RGB_FCN_CHANGED, enums[index], enums[fcn]);
    }

    final int getCombineRgbFunction(int index) {
        if (combineRgbSrc == null) {
            // it is possible to do a get before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
            initCombineMode(this);
        }
	return combineRgbFcn[index];
    }

    final void initCombineAlphaFunction(int index, int fcn) {
	if (combineRgbSrc == null) {
	    // it is possible to set the combineAlphaFcn before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
	    initCombineMode(this);
	}
	combineAlphaFcn[index] = fcn;
    }

    final void setCombineAlphaFunction(int index, int fcn) {
	initCombineAlphaFunction(index, fcn);
        sendMessage(COMBINE_ALPHA_FCN_CHANGED, enums[index], enums[fcn]);
    }

    final int getCombineAlphaFunction(int index) {
        if (combineRgbSrc == null) {
            // it is possible to do a get before
            // setting the texture mode to COMBINE, so need to initialize
            // the combine mode related fields here
            initCombineMode(this);
        }
        return combineAlphaFcn[index];
    }

    final void initCombineRgbScale(int scale) {
	combineRgbScale = scale;
    }

    final void setCombineRgbScale(int scale) {
	initCombineRgbScale(scale);
        sendMessage(COMBINE_RGB_SCALE_CHANGED, enums[scale], null);
    }

    final int getCombineRgbScale() {
	return combineRgbScale;
    }

    final void initCombineAlphaScale(int scale) {
	combineAlphaScale = scale;
    }

    final void setCombineAlphaScale(int scale) {
	initCombineAlphaScale(scale);
        sendMessage(COMBINE_ALPHA_SCALE_CHANGED, enums[scale], null);
    }

    final int getCombineAlphaScale() {
	return combineAlphaScale;
    }

    void updateNative(Canvas3D cv, boolean simulate, int textureFormat) {

        //System.out.println("TextureAttributes/updateNative:  simulate= " + simulate + " " + this);
	
	//if ((cv.textureExtendedFeatures & Canvas3D.TEXTURE_COLOR_TABLE) 
	//	== 0) && textureColorTable != null) {
	//    System.out.println("TextureColorTable Not supported");
	//}

	//System.out.println("textureMode= " + textureMode);
	boolean isIdentity =
			((transform.getType() & Transform3D.IDENTITY) != 0);
	
        if (simulate == false) {
	    if (VirtualUniverse.mc.useCombiners &&
		(cv.textureExtendedFeatures & 
		 Canvas3D.TEXTURE_REGISTER_COMBINERS) != 0) {
                Pipeline.getPipeline().updateRegisterCombiners(cv.ctx,
                        transform.mat, isIdentity, textureMode, perspCorrectionMode,
                        textureBlendColor.x, textureBlendColor.y,
                        textureBlendColor.z, textureBlendColor.w,
                        textureFormat, combineRgbMode, combineAlphaMode,
                        combineRgbSrc, combineAlphaSrc,
                        combineRgbFcn, combineAlphaFcn,
                        combineRgbScale, combineAlphaScale);
	    } else {
	        if (textureMode == TextureAttributes.COMBINE) {

		if ((cv.textureExtendedFeatures & 
				Canvas3D.TEXTURE_COMBINE) != 0) {

		    // Texture COMBINE is supported by the underlying layer

		    int _combineRgbMode = combineRgbMode;
		    int _combineAlphaMode = combineAlphaMode;

                    Pipeline.getPipeline().updateTextureAttributes(cv.ctx,
                            transform.mat, isIdentity, textureMode,
                            perspCorrectionMode,
                            textureBlendColor.x, textureBlendColor.y,
                            textureBlendColor.z, textureBlendColor.w,
                            textureFormat);


		    if (((combineRgbMode == TextureAttributes.COMBINE_DOT3) &&
			  ((cv.textureExtendedFeatures & 
				Canvas3D.TEXTURE_COMBINE_DOT3) == 0)) || 
		        ((combineRgbMode == TextureAttributes.COMBINE_SUBTRACT) &&
			  ((cv.textureExtendedFeatures & 
				Canvas3D.TEXTURE_COMBINE_SUBTRACT) == 0))) {

			// Combine DOT3/SUBTRACT is not supported by the 
			// underlying layer, fallback to COMBINE_REPLACE

			_combineRgbMode = TextureAttributes.COMBINE_REPLACE;
		    }

		    if (((combineAlphaMode == TextureAttributes.COMBINE_DOT3) &&
			  ((cv.textureExtendedFeatures & 
				Canvas3D.TEXTURE_COMBINE_DOT3) == 0)) ||
		        ((combineAlphaMode == TextureAttributes.COMBINE_SUBTRACT) &&
			  ((cv.textureExtendedFeatures & 
				Canvas3D.TEXTURE_COMBINE_SUBTRACT) == 0))) {

			// Combine DOT3/SUBTRACT is not supported by the 
			// underlying layer, fallback to COMBINE_REPLACE

			_combineAlphaMode = TextureAttributes.COMBINE_REPLACE;
		    }

                    Pipeline.getPipeline().updateCombiner(cv.ctx,
                            _combineRgbMode, _combineAlphaMode,
                            combineRgbSrc, combineAlphaSrc,
                            combineRgbFcn, combineAlphaFcn,
                            combineRgbScale, combineAlphaScale);

		} else {

		    // Texture COMBINE is not supported by the underlying
		    // layer, fallback to REPLACE

                    Pipeline.getPipeline().updateTextureAttributes(cv.ctx,
                            transform.mat, isIdentity,
                            TextureAttributes.REPLACE,
                            perspCorrectionMode,
                            textureBlendColor.x, textureBlendColor.y,
                            textureBlendColor.z, textureBlendColor.w,
                            textureFormat);
		}
	    } else {
                    Pipeline.getPipeline().updateTextureAttributes(cv.ctx,
                            transform.mat, isIdentity, textureMode,
                            perspCorrectionMode,
                            textureBlendColor.x, textureBlendColor.y,
                            textureBlendColor.z, textureBlendColor.w,
                            textureFormat);
	    }
	    }


	    if (((cv.textureExtendedFeatures & Canvas3D.TEXTURE_COLOR_TABLE) 
			!= 0) && textureColorTable != null) {

		Pipeline.getPipeline().updateTextureColorTable(cv.ctx, 
			numTextureColorTableComponents,
			textureColorTableSize, textureColorTable);
	    }
	} else {
	    // we are in the multi-pass mode,
	    // in this case, set the texture Mode to replace and use
	    // blending to simulate the original textureMode
            Pipeline.getPipeline().updateTextureAttributes(cv.ctx,
                    transform.mat, isIdentity, TextureAttributes.REPLACE,
                    perspCorrectionMode,
                    textureBlendColor.x, textureBlendColor.y,
                    textureBlendColor.z, textureBlendColor.w, textureFormat);

	    if (((cv.textureExtendedFeatures & Canvas3D.TEXTURE_COLOR_TABLE) 
			!= 0) && textureColorTable != null) {

		Pipeline.getPipeline().updateTextureColorTable(cv.ctx, numTextureColorTableComponents,
			textureColorTableSize, textureColorTable);
	    }

	    switch (textureMode) {
	    case TextureAttributes.COMBINE:
	    case TextureAttributes.REPLACE:
	         cv.setBlendFunc(cv.ctx,
			TransparencyAttributes.BLEND_ONE,
	                TransparencyAttributes.BLEND_ZERO);
		 break;
	    case TextureAttributes.MODULATE:
	         cv.setBlendFunc(cv.ctx,
			TransparencyAttributes.BLEND_DST_COLOR,
	                TransparencyAttributes.BLEND_ZERO);
		 break;
	    case TextureAttributes.DECAL:
		 if (textureFormat == Texture.RGBA) {
	             cv.setBlendFunc(cv.ctx,
			TransparencyAttributes.BLEND_SRC_ALPHA,
	                TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
		 } else {
	             cv.setBlendFunc(cv.ctx,
			TransparencyAttributes.BLEND_ONE,
	                TransparencyAttributes.BLEND_ZERO);
		 }
		 break;
	    case TextureAttributes.BLEND:
		cv.setBlendColor(cv.ctx, textureBlendColor.x, textureBlendColor.y,
				 textureBlendColor.z, textureBlendColor.w);
		cv.setBlendFunc(cv.ctx,
				TransparencyAttributes.BLEND_CONSTANT_COLOR,
				TransparencyAttributes.BLEND_ONE_MINUS_SRC_COLOR);
		break;
	    }
	}
    }


   /**
    * Creates and initializes a mirror object, point the mirror object 
    * to the retained object if the object is not editable
    */
    synchronized void createMirrorObject() {

	if (mirror == null) {
	    // Check the capability bits and let the mirror object
	    // point to itself if is not editable
	    if (isStatic()) {
		mirror = this;
	    } else {
		TextureAttributesRetained mirrorTa  = new TextureAttributesRetained();
		mirrorTa.source = source;
		mirrorTa.set(this);
		mirror = mirrorTa;
	    }
	}  else {
	    ((TextureAttributesRetained)mirror).set(this);
	}
    }

   /**
    * Initializes a mirror object
    */
    synchronized void initMirrorObject() {
	((TextureAttributesRetained)mirror).set(this);
    }


    /**
     * Update the "component" field of the mirror object with the 
     *  given "value"
     */
    synchronized void updateMirrorObject(int component, Object value, 
						Object value2) {
	TextureAttributesRetained mirrorTa = (TextureAttributesRetained)mirror;
	mirrorTa.mirrorCompDirty = true;

	if ((component & TRANSFORM_CHANGED) != 0) {
	    mirrorTa.transform.set((Transform3D)value);
            VirtualUniverse.mc.addToTransformFreeList((Transform3D)value);
	}
	else if ((component & MODE_CHANGED) != 0) {
	    mirrorTa.textureMode = ((Integer)value).intValue();

	    if ((mirrorTa.textureMode == TextureAttributes.COMBINE) &&
			(mirrorTa.combineRgbSrc == null)) {
		initCombineMode(mirrorTa);
	    }
	}
	else if ((component & COLOR_CHANGED) != 0) {
	    mirrorTa.textureBlendColor.set((Color4f)value);
	}
	else if ((component & CORRECTION_CHANGED) != 0) {
	    mirrorTa.perspCorrectionMode = ((Integer)value).intValue();
	} 
	else if ((component & TEXTURE_COLOR_TABLE_CHANGED) != 0) {
	    if (value == null) {
		mirrorTa.textureColorTable = null;
		mirrorTa.numTextureColorTableComponents = 0;
		mirrorTa.textureColorTableSize = 0;
	    } else {
		Object args[] = (Object[])value;
	        mirrorTa.textureColorTable = (int[])args[2];
		mirrorTa.numTextureColorTableComponents = 
			((Integer)args[0]).intValue();
		mirrorTa.textureColorTableSize = 
			((Integer)args[1]).intValue();
	    }
	}
	else if ((component & COMBINE_RGB_MODE_CHANGED) != 0) {
	    mirrorTa.combineRgbMode = ((Integer)value).intValue();
	}
        else if ((component & COMBINE_ALPHA_MODE_CHANGED) != 0) {
            mirrorTa.combineAlphaMode = ((Integer)value).intValue();
        }
        else if ((component & COMBINE_RGB_SRC_CHANGED) != 0) {
            if (mirrorTa.combineRgbSrc == null) {
                //initialize the memory for combine mode
                initCombineMode(mirrorTa);
            }
	    int index = ((Integer)value).intValue();
            mirrorTa.combineRgbSrc[index] = ((Integer)value2).intValue();
        }
        else if ((component & COMBINE_ALPHA_SRC_CHANGED) != 0) {
            if (mirrorTa.combineRgbSrc == null) {
                //initialize the memory for combine mode
                initCombineMode(mirrorTa);
            }
	    int index = ((Integer)value).intValue();
            mirrorTa.combineAlphaSrc[index] = ((Integer)value2).intValue();
        }
        else if ((component & COMBINE_RGB_FCN_CHANGED) != 0) {
            if (mirrorTa.combineRgbSrc == null) {
                //initialize the memory for combine mode
                initCombineMode(mirrorTa);
            }
	    int index = ((Integer)value).intValue();
            mirrorTa.combineRgbFcn[index] = ((Integer)value2).intValue();
        }
        else if ((component & COMBINE_ALPHA_FCN_CHANGED) != 0) {
            if (mirrorTa.combineRgbSrc == null) {
                //initialize the memory for combine mode
                initCombineMode(mirrorTa);
            }
	    int index = ((Integer)value).intValue();
            mirrorTa.combineAlphaFcn[index] = ((Integer)value2).intValue();
        }
        else if ((component & COMBINE_RGB_SCALE_CHANGED) != 0) {
            mirrorTa.combineRgbScale = ((Integer)value).intValue();
        }
        else if ((component & COMBINE_ALPHA_SCALE_CHANGED) != 0) {
            mirrorTa.combineAlphaScale = ((Integer)value).intValue();
        }
    }


    boolean equivalent(TextureAttributesRetained tr) {

	if (tr == null) {
	    return (false);

	} else if ((this.changedFrequent != 0) || (tr.changedFrequent != 0)) {
	    return (this == tr);
	}

	if (!(tr.transform.equals(transform) &&
	        tr.textureBlendColor.equals(textureBlendColor) &&
	        (tr.textureMode == textureMode) &&
	        (tr.perspCorrectionMode == perspCorrectionMode))) {
	    return false;
	}


	// now check for combine mode attributes if textureMode specifies
        // COMBINE

	if (textureMode == TextureAttributes.COMBINE) {

	    if ((tr.combineRgbMode != combineRgbMode) ||
		(tr.combineAlphaMode != combineAlphaMode) ||
		(tr.combineRgbScale != combineRgbScale) ||
		(tr.combineAlphaScale != combineAlphaScale)) {
		return false;
	    }

	    // now check if the operands for the combine equations are
	    // equivalent

	    int nOpNeeded = 0;

	    if (combineRgbMode == TextureAttributes.COMBINE_REPLACE) {
		nOpNeeded = 1;
	    } else if (combineRgbMode == TextureAttributes.COMBINE_INTERPOLATE) {
		nOpNeeded = 3;
	    } else {
		nOpNeeded = 2;
	    }

	    for (int i = 0; i < nOpNeeded; i++) {
		if ((tr.combineRgbSrc[i] != combineRgbSrc[i]) ||
		    (tr.combineAlphaSrc[i] != combineAlphaSrc[i]) ||
		    (tr.combineRgbFcn[i] != combineRgbFcn[i]) ||
		    (tr.combineAlphaFcn[i] != combineAlphaFcn[i])) {
		    return false;
		}
	    }
	}

	// now check for texture color table

	if (tr.textureColorTable == null) {
	    if (this.textureColorTable == null)
		return true;
	    else
		return false;
	} else if (this.textureColorTable == null) {
	    // tr.textureColorTable != null
            return false;
        } else {
	    if (tr.textureColorTable.length != this.textureColorTable.length) 
		return false;

	    for (int i = 0; i < this.textureColorTable.length; i++) {
		 if (this.textureColorTable[i] != tr.textureColorTable[i])
		     return false;
	    }

	    return true;
	}

    }


    protected Object clone() {
	TextureAttributesRetained tr = (TextureAttributesRetained)super.clone();
	tr.transform = new Transform3D(transform);
	tr.textureBlendColor = new Color4f(textureBlendColor);
	if (textureColorTable != null) {
	    tr.textureColorTable = new int[textureColorTable.length];
	    System.arraycopy(textureColorTable, 0, tr.textureColorTable, 0,
				textureColorTable.length);
	} else {
	    tr.textureColorTable = null;
	}

	// clone the combine mode attributes
	if (combineRgbSrc != null) {
            tr.combineRgbSrc   = new int[3];
            tr.combineAlphaSrc = new int[3];
            tr.combineRgbFcn   = new int[3];
            tr.combineAlphaFcn = new int[3];

	    for (int i = 0; i < 3; i++) {
		 tr.combineRgbSrc[i] = combineRgbSrc[i];
		 tr.combineAlphaSrc[i] = combineAlphaSrc[i];
		 tr.combineRgbFcn[i] = combineRgbFcn[i];
		 tr.combineAlphaFcn[i] = combineAlphaFcn[i];
	    }
	}
	   
	// other attributes are copied in super.clone()
	return tr;
    }

    protected void set(TextureAttributesRetained tr) {
	super.set(tr);
        transform.set(tr.transform);
	textureBlendColor.set(tr.textureBlendColor);
	textureMode = tr.textureMode;
	perspCorrectionMode = tr.perspCorrectionMode;

	// set texture color table

	if (tr.textureColorTable != null) {
	    if (textureColorTable == null ||
		    textureColorTable.length != tr.textureColorTable.length) {
	        textureColorTable = new int[tr.textureColorTable.length];
	    }
	    System.arraycopy(tr.textureColorTable, 0, textureColorTable, 0,
					tr.textureColorTable.length);
	} else {
	    textureColorTable = null;
	}
	numTextureColorTableComponents = tr.numTextureColorTableComponents;
	textureColorTableSize = tr.textureColorTableSize;
 

	// set the combine mode attributes

	combineRgbMode = tr.combineRgbMode;
	combineAlphaMode = tr.combineAlphaMode;
	combineRgbScale = tr.combineRgbScale;
	combineAlphaScale = tr.combineAlphaScale;

	if (tr.combineRgbSrc != null) {
	    if (combineRgbSrc == null) {
                combineRgbSrc   = new int[3];
                combineAlphaSrc = new int[3];
                combineRgbFcn   = new int[3];
                combineAlphaFcn = new int[3];
	    }

	    for (int i = 0; i < 3; i++) {
		 combineRgbSrc[i] = tr.combineRgbSrc[i];
		 combineAlphaSrc[i] = tr.combineAlphaSrc[i];
		 combineRgbFcn[i] = tr.combineRgbFcn[i];
		 combineAlphaFcn[i] = tr.combineAlphaFcn[i];
	    }
	}
    }


    final void sendMessage(int attrMask, Object attr1, Object attr2) {

       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  


	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.TEXTUREATTRIBUTES_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1] = new Integer(attrMask);
	createMessage.args[2] = attr1;
	createMessage.args[3] = attr2;
        createMessage.args[4] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);


	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.TEXTUREATTRIBUTES_CHANGED;
		
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1] = new Integer(attrMask);
	    createMessage.args[2] = attr1;

	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

    void handleFrequencyChange(int bit) {
	switch (bit) {
	case TextureAttributes.ALLOW_MODE_WRITE:
	case TextureAttributes.ALLOW_BLEND_COLOR_WRITE:
	case TextureAttributes.ALLOW_TRANSFORM_WRITE:
	case TextureAttributes.ALLOW_COLOR_TABLE_WRITE:
	case TextureAttributes.ALLOW_COMBINE_WRITE: {
            setFrequencyChangeMask(bit, bit);
        }
	default:
	    break;
	}
    }
}
