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

import java.util.Vector;
import java.util.BitSet;
import java.util.ArrayList;


/**
 * The Appearance object defines all rendering state that can be set
 * as a component object of a Shape3D node.
 */
class AppearanceRetained extends NodeComponentRetained {

    //
    // State variables: these should all be initialized to approproate
    // Java 3D defaults.
    //

    // Material object used when lighting is enabled
    MaterialRetained	material = null;

    // Texture object used to apply a texture map to an object
    TextureRetained	texture = null;

    // Texture coordinate generation object
    TexCoordGenerationRetained	texCoordGeneration = null;

    // Texture Attributes bundle object
    TextureAttributesRetained	textureAttributes = null;

    TextureUnitStateRetained texUnitState[] = null;

    // Coloring Attributes bundle object
    ColoringAttributesRetained	coloringAttributes = null;

    // Transparency Attributes bundle object
    TransparencyAttributesRetained	transparencyAttributes = null;

    // Rendering Attributes bundle object
    RenderingAttributesRetained	renderingAttributes = null;

    // Polygon Attributes bundle object
    PolygonAttributesRetained	polygonAttributes = null;

    // Line Attributes bundle object
    LineAttributesRetained	lineAttributes = null;

    // Point Attributes bundle object
    PointAttributesRetained	pointAttributes = null;


    // Lock used for synchronization of live state
    Object liveStateLock = new Object();
    
    // NOTE: Consider grouping random state into common objects

    // Cache used during compilation.  If map == compState, then 
    // mapAppearance can be used for this appearance
    CompileState map = null;
    AppearanceRetained mapAppearance = null;

    static final int MATERIAL           = 0x0001;
    static final int TEXTURE            = 0x0002;
    static final int TEXCOORD_GEN       = 0x0004;
    static final int TEXTURE_ATTR       = 0x0008;
    static final int COLOR              = 0x0010;
    static final int TRANSPARENCY       = 0x0020;
    static final int RENDERING          = 0x0040;
    static final int POLYGON            = 0x0080;
    static final int LINE               = 0x0100;
    static final int POINT              = 0x0200;
    static final int TEXTURE_UNIT_STATE = 0x0400;

    static final int ALL_COMPONENTS = (MATERIAL|TEXTURE|TEXCOORD_GEN|TEXTURE_ATTR|COLOR|TRANSPARENCY|
				       RENDERING|POLYGON|LINE|POINT|TEXTURE_UNIT_STATE);

    static final int ALL_SOLE_USERS = 0;

    // A pointer to the scene graph appearance object
    AppearanceRetained sgApp = null;

    // The object level hashcode for this appearance
    //    int objHashCode = super.hashCode();

    /**
     * Set the material object to the specified object.
     * @param material object that specifies the desired material
     * @exception IllegalSharingException
     * properties
     */
    void setMaterial(Material material) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.material != null) {
		    this.material.clearLive(refCount);
		    this.material.removeMirrorUsers(this);
		}
		if (material != null) {
		    ((MaterialRetained)material.retained).setLive(inBackgroundGroup, refCount);
		    // If appearance is live, then copy all the users of this
		    // appaearance as users of this material
		    ((MaterialRetained)material.retained).copyMirrorUsers(this);
		}
		sendMessage(MATERIAL,  
			    (material != null ?
			     ((MaterialRetained)material.retained).mirror : null), true);
	    }
	    if (material == null) {
		this.material = null;
	    } else {
		this.material = (MaterialRetained)material.retained;
	    }
	}
    }

    /**
     * Retrieve the current material object.
     * @return the material object
     */
    Material getMaterial() {
        return (material == null ? null : (Material)material.source);
    }

    /**
     * Sets the texture object to the specified object.
     * @param texture object that specifies the desired texture
     * map and texture parameters
     */
    void setTexture(Texture texture) {


	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.texture != null) {
		    this.texture.clearLive(refCount);
		    this.texture.removeMirrorUsers(this);
		}

		if (texture != null) {
		    ((TextureRetained)texture.retained).setLive(inBackgroundGroup, refCount);
		    ((TextureRetained)texture.retained).copyMirrorUsers(this);
	    	}
		sendMessage(TEXTURE,  
			    (texture != null ?
			     ((TextureRetained)texture.retained).mirror : null), true);

	    } 


	    if (texture == null) {
		this.texture = null;
	    } else {
		this.texture = (TextureRetained)texture.retained;
	    }
	}
    }

    /**
     * Retrieves the current texture object. 
     * @return the texture object
     */
    Texture getTexture() {
	return (texture == null ? null : (Texture)texture.source);
    }

    /**
     * Sets the textureAttrbutes object to the specified object.
     * @param textureAttributes object that specifies the desired texture
     * attributes
     */
    void setTextureAttributes(TextureAttributes textureAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.textureAttributes != null) {
		    this.textureAttributes.clearLive(refCount);
		    this.textureAttributes.removeMirrorUsers(this);
		}

		if (textureAttributes != null) {
		    ((TextureAttributesRetained)textureAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((TextureAttributesRetained)textureAttributes.retained).copyMirrorUsers(this);
		}
		sendMessage(TEXTURE_ATTR, 
			    (textureAttributes != null ?
			     ((TextureAttributesRetained)textureAttributes.retained).mirror:
			     null), true);

	    } 


	    if (textureAttributes == null) {
		this.textureAttributes = null;
	    } else {
		this.textureAttributes = (TextureAttributesRetained)textureAttributes.retained;
	    }
	}
    }

    /**
     * Retrieves the current textureAttributes object.
     * @return the textureAttributes object
     */
    TextureAttributes getTextureAttributes() {
	return (textureAttributes == null ? null :
		(TextureAttributes)textureAttributes.source);
    }

    /**
     * Sets the coloringAttrbutes object to the specified object.
     * @param coloringAttributes object that specifies the desired texture
     * attributes
     */
    void setColoringAttributes(ColoringAttributes coloringAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.coloringAttributes != null) {
		    this.coloringAttributes.clearLive(refCount);
		    this.coloringAttributes.removeMirrorUsers(this);
		}

		if (coloringAttributes != null) {
		    ((ColoringAttributesRetained)coloringAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((ColoringAttributesRetained)coloringAttributes.retained).copyMirrorUsers(this);
		}
		sendMessage(COLOR, 
			    (coloringAttributes != null ? 
			     ((ColoringAttributesRetained)coloringAttributes.retained).mirror:
			     null), true);
	    } 


	    if (coloringAttributes == null) {
		this.coloringAttributes = null;
	    } else {
		this.coloringAttributes = (ColoringAttributesRetained)coloringAttributes.retained;
	    }
	}
    }

    /**
     * Retrieves the current coloringAttributes object.
     * @return the coloringAttributes object
     */
    ColoringAttributes getColoringAttributes() {
	return (coloringAttributes == null ? null :
		(ColoringAttributes)coloringAttributes.source);
    }

    /**
     * Sets the transparencyAttrbutes object to the specified object.
     * @param transparencyAttributes object that specifies the desired texture
     * attributes
     */
    void setTransparencyAttributes(TransparencyAttributes transparencyAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.transparencyAttributes != null) {
		    this.transparencyAttributes.clearLive(refCount);
		    this.transparencyAttributes.removeMirrorUsers(this);
		}

		if (transparencyAttributes != null) {
		    ((TransparencyAttributesRetained)transparencyAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((TransparencyAttributesRetained)transparencyAttributes.retained).copyMirrorUsers(this);
		}

		sendMessage(TRANSPARENCY, 
			    (transparencyAttributes != null ? 
			     ((TransparencyAttributesRetained)transparencyAttributes.retained).mirror: null), true);
	    } 


	    if (transparencyAttributes == null) {
		this.transparencyAttributes = null;
	    } else {
		this.transparencyAttributes = (TransparencyAttributesRetained)transparencyAttributes.retained;

	    }
	}
    }

    /**
     * Retrieves the current transparencyAttributes object.
     * @return the transparencyAttributes object
     */
    TransparencyAttributes getTransparencyAttributes() {
	return (transparencyAttributes == null ? null :
		(TransparencyAttributes)transparencyAttributes.source);
    }

    /**
     * Sets the renderingAttrbutes object to the specified object.
     * @param renderingAttributes object that specifies the desired texture
     * attributes
     */
    void setRenderingAttributes(RenderingAttributes renderingAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {
		if (this.renderingAttributes != null) {
		    this.renderingAttributes.clearLive(refCount);
		    this.renderingAttributes.removeMirrorUsers(this);
		}

		if (renderingAttributes != null) {
		    ((RenderingAttributesRetained)renderingAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((RenderingAttributesRetained)renderingAttributes.retained).copyMirrorUsers(this);
		}
		Object m = null;
		boolean v = true;
		if (renderingAttributes != null) {
		    m = ((RenderingAttributesRetained)renderingAttributes.retained).mirror;
		    v = ((RenderingAttributesRetained)renderingAttributes.retained).visible;
		}
		sendMessage(RENDERING,m, v);
		// Also need to send a message to GeometryStructure.
		sendRenderingAttributesChangedMessage( v);
	    }
	    if (renderingAttributes == null) {
		this.renderingAttributes = null;
	    } else {
		this.renderingAttributes = (RenderingAttributesRetained)renderingAttributes.retained;

	    }
	}
    }

    /**
     * Retrieves the current renderingAttributes object.
     * @return the renderingAttributes object
     */
    RenderingAttributes getRenderingAttributes() {
	if (renderingAttributes == null)
	    return null;

	return (RenderingAttributes)renderingAttributes.source;
    }

    /**
     * Sets the polygonAttrbutes object to the specified object.
     * @param polygonAttributes object that specifies the desired texture
     * attributes
     */
    void setPolygonAttributes(PolygonAttributes polygonAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {
		if (this.polygonAttributes != null) {
		    this.polygonAttributes.clearLive(refCount);
		    this.polygonAttributes.removeMirrorUsers(this);
		}

		if (polygonAttributes != null) {
		    ((PolygonAttributesRetained)polygonAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((PolygonAttributesRetained)polygonAttributes.retained).copyMirrorUsers(this);
		}
		sendMessage(POLYGON, 
			    (polygonAttributes != null ?
			     ((PolygonAttributesRetained)polygonAttributes.retained).mirror :
			     null), true);

	    } 

	    if (polygonAttributes == null) {
		this.polygonAttributes = null;
	    } else {
		this.polygonAttributes = (PolygonAttributesRetained)polygonAttributes.retained;
	    }
	}
    }

    /**
     * Retrieves the current polygonAttributes object.
     * @return the polygonAttributes object
     */
    PolygonAttributes getPolygonAttributes() {
	return (polygonAttributes == null ? null:
		(PolygonAttributes)polygonAttributes.source);
    }

    /**
     * Sets the lineAttrbutes object to the specified object.
     * @param lineAttributes object that specifies the desired texture
     * attributes
     */
    void setLineAttributes(LineAttributes lineAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.lineAttributes != null) {
		    this.lineAttributes.clearLive(refCount);
		    this.lineAttributes.removeMirrorUsers(this);
		}

		if (lineAttributes != null) {
		    ((LineAttributesRetained)lineAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((LineAttributesRetained)lineAttributes.retained).copyMirrorUsers(this);
		}
		sendMessage(LINE, 
			    (lineAttributes != null ? 
			     ((LineAttributesRetained)lineAttributes.retained).mirror: null), true);
	    } 


	    if (lineAttributes == null) {
		this.lineAttributes = null;
	    } else {
		this.lineAttributes = (LineAttributesRetained)lineAttributes.retained;
	    }
	}
    }

    /**
     * Retrieves the current lineAttributes object.
     * @return the lineAttributes object
     */
    LineAttributes getLineAttributes() {
	return (lineAttributes == null ? null :
		(LineAttributes)lineAttributes.source);
    }

    /**
     * Sets the pointAttrbutes object to the specified object.
     * @param pointAttributes object that specifies the desired texture
     * attributes
     */
    void setPointAttributes(PointAttributes pointAttributes) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.pointAttributes != null) {
		    this.pointAttributes.clearLive(refCount);
		    this.pointAttributes.removeMirrorUsers(this);
		}
		if (pointAttributes != null) {
		    ((PointAttributesRetained)pointAttributes.retained).setLive(inBackgroundGroup, refCount);
		    ((PointAttributesRetained)pointAttributes.retained).copyMirrorUsers(this);
		}
		sendMessage(POINT, 
			    (pointAttributes != null ?
			     ((PointAttributesRetained)pointAttributes.retained).mirror: 
			     null), true);
	    } 


	    if (pointAttributes == null) {
		this.pointAttributes = null;
	    } else {
		this.pointAttributes = (PointAttributesRetained)pointAttributes.retained;
	    }
	}
    }

    /**
     * Retrieves the current pointAttributes object.
     * @return the pointAttributes object
     */
    PointAttributes getPointAttributes() {
	return (pointAttributes == null? null : (PointAttributes)pointAttributes.source);
    }

    /**
     * Sets the texCoordGeneration object to the specified object.
     * @param texCoordGeneration object that specifies the texture coordinate
     * generation parameters
     */
    void setTexCoordGeneration(TexCoordGeneration texGen) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		if (this.texCoordGeneration != null) {
		    this.texCoordGeneration.clearLive(refCount);
		    this.texCoordGeneration.removeMirrorUsers(this);
		}

		if (texGen != null) {
		    ((TexCoordGenerationRetained)texGen.retained).setLive(inBackgroundGroup, refCount);
		    ((TexCoordGenerationRetained)texGen.retained).copyMirrorUsers(this);
		}
		sendMessage(TEXCOORD_GEN, 
			    (texGen != null ?
			     ((TexCoordGenerationRetained)texGen.retained).mirror : null), true);
	    } 

	    if (texGen == null) {
		this.texCoordGeneration = null;
	    } else {
		this.texCoordGeneration = (TexCoordGenerationRetained)texGen.retained;
	    }
	}
    }

    /**
     * Retrieves the current texCoordGeneration object.
     * @return the texCoordGeneration object
     */
    TexCoordGeneration getTexCoordGeneration() {
	return (texCoordGeneration == null ? null :
		(TexCoordGeneration)texCoordGeneration.source);
    }


    /**
     * Sets the texture unit state array to the specified array.
     * @param textureUnitState array that specifies the texture unit state
     */
    void setTextureUnitState(TextureUnitState[] stateArray) {

	int i;

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		// remove the existing texture unit states from this appearance
		if (this.texUnitState != null) {
		    for (i = 0; i < this.texUnitState.length; i++) {
			if (this.texUnitState[i] != null) {
			    this.texUnitState[i].clearLive(refCount);
			    this.texUnitState[i].removeMirrorUsers(this);
			}
		    }
		}

		// add the specified texture unit states to this appearance
		// also make a copy of the array of references to the units
		if (stateArray != null) {

		    Object [] args = new Object[2];

		    // -1 index means the entire array is to be set
		    args[0] = new Integer(-1);
	   
		    // make a copy of the array for the message,
		    TextureUnitStateRetained mirrorStateArray[] = 
			new TextureUnitStateRetained[stateArray.length];

		    args[1] = (Object) mirrorStateArray;

		    for (i = 0; i < stateArray.length; i++) {
			TextureUnitState tu = stateArray[i];
			if (tu != null) {
			    ((TextureUnitStateRetained)tu.retained).setLive(
									    inBackgroundGroup, refCount);
			    ((TextureUnitStateRetained)tu.retained).copyMirrorUsers(
										    this);
			    mirrorStateArray[i] =  (TextureUnitStateRetained)
				((TextureUnitStateRetained)tu.retained).mirror;
			} else {
			    mirrorStateArray[i] = null;
			}
		    }
		    sendMessage(TEXTURE_UNIT_STATE, args, true);

		} else {
		    sendMessage(TEXTURE_UNIT_STATE, null, true);
		}
	    } 

	    // assign the retained copy of the texture unit state to the
	    // appearance
	    if (stateArray == null) {
		this.texUnitState = null;
	    } else {

		// make another copy of the array for the retained object
		// itself if it doesn't have a copy or the array size is
		// not the same
		if ((this.texUnitState == null) || 
		    (this.texUnitState.length != stateArray.length)) {
		    this.texUnitState = new TextureUnitStateRetained[
								     stateArray.length];
		}
		for (i = 0; i < stateArray.length; i++) {
		    if (stateArray[i] != null) {
			this.texUnitState[i] = 
			    (TextureUnitStateRetained)stateArray[i].retained;
		    } else {
			this.texUnitState[i] = null;
		    }
		}
	    }
	}
    }

    void setTextureUnitState(int index, TextureUnitState state) {

	synchronized(liveStateLock) {
	    if (source.isLive()) {

		// remove the existing texture unit states from this appearance
		// Note: Let Java throw an exception if texUnitState is null
		// or index is >= texUnitState.length.
		if (this.texUnitState[index] != null) {
		    this.texUnitState[index].clearLive(refCount);
		    this.texUnitState[index].removeMirrorUsers(this);
		}

		// add the specified texture unit states to this appearance
		// also make a copy of the array of references to the units
		Object args[] = new Object[2];
		args[0] = new Integer(index);

		if (state != null) {
		    ((TextureUnitStateRetained)state.retained).setLive(
								       inBackgroundGroup, refCount);
		    ((TextureUnitStateRetained)state.retained).copyMirrorUsers(this);
		    args[1] =  ((TextureUnitStateRetained)state.retained).mirror;
		    sendMessage(TEXTURE_UNIT_STATE, args, true);
		} else {
		    args[1] =  null;
		    sendMessage(TEXTURE_UNIT_STATE, args, true);
		}
	    } 

	    // assign the retained copy of the texture unit state to the
	    // appearance
	    if (state != null) {
		this.texUnitState[index] = (TextureUnitStateRetained)state.retained;
	    } else {
		this.texUnitState[index] = null;
	    }
	}
    }



    /**
     * Retrieves the array of texture unit state objects from this
     * Appearance object.  A shallow copy of the array of references to
     * the TextureUnitState objects is returned.
     *
     */
    TextureUnitState[] getTextureUnitState() {
	if (texUnitState == null) {
	    return null;
	} else {
	    TextureUnitState tus[] = 
		new TextureUnitState[texUnitState.length];
	    for (int i = 0; i < texUnitState.length; i++) {
		 if (texUnitState[i] != null) {
		     tus[i] = (TextureUnitState) texUnitState[i].source;
	 	 } else {
		     tus[i] = null;
		 }
	    }
	    return tus;
	}
    }

    /**
     * Retrieves the texture unit state object at the specified
     * index within the texture unit state array.  
     */
    TextureUnitState getTextureUnitState(int index) {

	// let Java throw an exception if texUnitState == null or
	// index is >= length
	if (texUnitState[index] != null) 
	    return (TextureUnitState)texUnitState[index].source;
	else
	    return null;
    }


    /**
     * Retrieves the length of the texture unit state array from
     * this appearance object.  The length of this array specifies the
     * maximum number of texture units that will be used by this
     * appearance object.  If the array is null, a count of 0 is
     * returned.
     */

    int getTextureUnitCount() {
	if (texUnitState == null)
	    return 0;
	else
	    return texUnitState.length;
    }


    synchronized void createMirrorObject() {
	if (mirror == null) {
	    // we can't check isStatic() since it sub-NodeComponent
	    // create a new one, we should create a 
	    // new AppearanceRetained() even though isStatic() = true.
	    // For simplicity, always create a retained side.
	    mirror = new AppearanceRetained();
	}
	initMirrorObject();
    }

    /**
     * This routine updates the mirror appearance for this appearance.
     * It also calls the update method for each node component if it
     * is not null.
     */
    synchronized void initMirrorObject() {

	AppearanceRetained mirrorApp = (AppearanceRetained)mirror;

	mirrorApp.source = source;
	mirrorApp.sgApp = this;

	// Fix for Issue 33: copy the changedFrequent mask to mirror
	mirrorApp.changedFrequent = changedFrequent;

	if (material != null) { 
	    mirrorApp.material = (MaterialRetained)material.mirror;
	} else {
	    mirrorApp.material = null;
	}

	if (texture != null) { 
	    mirrorApp.texture = (TextureRetained)texture.mirror;
	} else {
	    mirrorApp.texture = null;
	}
	if (texCoordGeneration != null) { 
	    mirrorApp.texCoordGeneration = (TexCoordGenerationRetained)texCoordGeneration.mirror;
	} else {
	    mirrorApp.texCoordGeneration = null;
	}

	if (textureAttributes != null) { 
	    mirrorApp.textureAttributes = (TextureAttributesRetained)textureAttributes.mirror;
	} else {
	    mirrorApp.textureAttributes = null;
	}

	// TextureUnitState supercedes the single texture interface
	if (texUnitState != null && texUnitState.length > 0) {
	    mirrorApp.texUnitState = 
		new TextureUnitStateRetained[texUnitState.length];
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null) {
		    mirrorApp.texUnitState[i] = 
			(TextureUnitStateRetained)texUnitState[i].mirror;
		}
	    }
	} else if (mirrorApp.texture != null ||
			mirrorApp.textureAttributes != null ||
			mirrorApp.texCoordGeneration != null) {

            mirrorApp.texUnitState = new TextureUnitStateRetained[1];
            mirrorApp.texUnitState[0] = new TextureUnitStateRetained();
            mirrorApp.texUnitState[0].set(
                                mirrorApp.texture,
                                mirrorApp.textureAttributes,
                                mirrorApp.texCoordGeneration);
	}

	if (coloringAttributes != null) { 
	    mirrorApp.coloringAttributes = (ColoringAttributesRetained)coloringAttributes.mirror;
	} else {
	    mirrorApp.coloringAttributes = null;
	}
	if (transparencyAttributes != null) { 
	    mirrorApp.transparencyAttributes = (TransparencyAttributesRetained)transparencyAttributes.mirror;
	} else {
	    mirrorApp.transparencyAttributes = null;
	}

	if (renderingAttributes != null) { 
	    mirrorApp.renderingAttributes = (RenderingAttributesRetained)renderingAttributes.mirror;
	} else {
	    mirrorApp.renderingAttributes = null;
	}

	if (polygonAttributes != null) { 
	    mirrorApp.polygonAttributes = (PolygonAttributesRetained)polygonAttributes.mirror;
	} else {
	    mirrorApp.polygonAttributes = null;
	}

	if (lineAttributes != null) { 
	    mirrorApp.lineAttributes = (LineAttributesRetained)lineAttributes.mirror;
	} else {
	    mirrorApp.lineAttributes = null;
	}

	if (pointAttributes != null) { 
	    mirrorApp.pointAttributes = (PointAttributesRetained)pointAttributes.mirror;
	} else {
	    mirrorApp.pointAttributes = null;
	}
    }

  /** 
   * Update the "component" field of the mirror object with the 
   *  given "value"
   */
    synchronized void updateMirrorObject(int component, Object value) {
      AppearanceRetained mirrorApp = (AppearanceRetained)mirror;
      if ((component & MATERIAL) != 0) {
	  mirrorApp.material = (MaterialRetained)value;
      }
      else if ((component & TEXTURE) != 0) {
	  if (mirrorApp.texUnitState == null) {
	      mirrorApp.texUnitState = new TextureUnitStateRetained[1];
	      mirrorApp.texUnitState[0] = new TextureUnitStateRetained();
	  }
	  mirrorApp.texUnitState[0].texture = (TextureRetained)value;
      }
      else if ((component & TEXCOORD_GEN) != 0) {
	  if (mirrorApp.texUnitState == null) {
	      mirrorApp.texUnitState = new TextureUnitStateRetained[1];
	      mirrorApp.texUnitState[0] = new TextureUnitStateRetained();
	  }
	  mirrorApp.texUnitState[0].texGen = (TexCoordGenerationRetained)value;
      }
      else if ((component & TEXTURE_ATTR) != 0) {
	  if (mirrorApp.texUnitState == null) {
	      mirrorApp.texUnitState = new TextureUnitStateRetained[1];
	      mirrorApp.texUnitState[0] = new TextureUnitStateRetained();
	  }
	  mirrorApp.texUnitState[0].texAttrs = (TextureAttributesRetained)value;
      }
      else if ((component & TEXTURE_UNIT_STATE) != 0) {
	  Object [] args = (Object [])value;

	  if (args == null) {
	      mirrorApp.texUnitState = null;
	  } else {
	      int index = ((Integer)args[0]).intValue();
	      if (index == -1) {
	          mirrorApp.texUnitState = 
			(TextureUnitStateRetained [])args[1];
	      } else {
	          mirrorApp.texUnitState[index] = 
			(TextureUnitStateRetained)args[1];
	      }
	  }
      }
      else if ((component & COLOR) != 0) {
	  mirrorApp.coloringAttributes = (ColoringAttributesRetained)value;
      }
      else if ((component & TRANSPARENCY) != 0) {
	  mirrorApp.transparencyAttributes = (TransparencyAttributesRetained)value;
      }
      else if ((component & RENDERING) != 0) {
	  mirrorApp.renderingAttributes = (RenderingAttributesRetained)value;
      }
      else if ((component & POLYGON) != 0) {
	  mirrorApp.polygonAttributes = (PolygonAttributesRetained)value;
      }
      else if ((component & LINE) != 0) {
	  mirrorApp.lineAttributes = (LineAttributesRetained)value;
      }
      else if ((component & POINT) != 0) {
	  mirrorApp.pointAttributes = (PointAttributesRetained)value;
      }

    }

    /**
     * This setLive routine first calls the superclass's method, then
     * it adds itself to the list of lights
     */
    void setLive(boolean backgroundGroup, int refCount) {
	
	if (material != null) {	    
	
	    material.setLive(backgroundGroup, refCount);
	}

	if (texture != null) {

	    texture.setLive(backgroundGroup, refCount);
	}

	if (texCoordGeneration != null) {

	    texCoordGeneration.setLive(backgroundGroup, refCount);
	}

	if (textureAttributes != null) {
	    
	    textureAttributes.setLive(backgroundGroup, refCount);
	}

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null)
		    texUnitState[i].setLive(backgroundGroup, refCount);
	    }
	}

		
	if (coloringAttributes != null) {
	    coloringAttributes.setLive(backgroundGroup, refCount);
	}

	if (transparencyAttributes != null) {
	    transparencyAttributes.setLive(backgroundGroup, refCount);
	}

	if (renderingAttributes != null) {
	    renderingAttributes.setLive(backgroundGroup, refCount);
	}

	if (polygonAttributes != null) {
	    polygonAttributes.setLive(backgroundGroup, refCount);
	}

	if (lineAttributes != null) {
	    lineAttributes.setLive(backgroundGroup, refCount);
	}

	if (pointAttributes != null) {
	    pointAttributes.setLive(backgroundGroup, refCount);
	}


	// Increment the reference count and initialize the appearance
	// mirror object
        super.doSetLive(backgroundGroup, refCount);
	super.markAsLive();
    }

    /**
     * This clearLive routine first calls the superclass's method, then
     * it removes itself to the list of lights
     */
    void clearLive(int refCount) {
	super.clearLive(refCount);

	if (texture != null) {
	    texture.clearLive(refCount);
	}

	if (texCoordGeneration != null) {
	    texCoordGeneration.clearLive(refCount);
	}

	if (textureAttributes != null) {
	    textureAttributes.clearLive(refCount);
	}

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null) 
		    texUnitState[i].clearLive(refCount);
	    }
	}

	if (coloringAttributes != null) {
	    coloringAttributes.clearLive(refCount);
	}

	if (transparencyAttributes != null) {
	    transparencyAttributes.clearLive(refCount);
	}

	if (renderingAttributes != null) {
	    renderingAttributes.clearLive(refCount);
	}

	if (polygonAttributes != null) {
	    polygonAttributes.clearLive(refCount);
	}

	if (lineAttributes != null) {
	    lineAttributes.clearLive(refCount);
	}

	if (pointAttributes != null) {
	    pointAttributes.clearLive(refCount);
	}

	if (material != null) {
	    material.clearLive(refCount);
	}
    }


    boolean isStatic() {
	boolean flag;

	flag = (source.capabilityBitsEmpty() && 
		((texture == null) ||
		 texture.source.capabilityBitsEmpty()) &&
		((texCoordGeneration == null) || 
		 texCoordGeneration.source.capabilityBitsEmpty()) && 
		((textureAttributes == null) || 
		 textureAttributes.source.capabilityBitsEmpty()) &&
		((coloringAttributes == null) ||
		 coloringAttributes.source.capabilityBitsEmpty()) &&
		((transparencyAttributes == null) || 
		 transparencyAttributes.source.capabilityBitsEmpty()) &&
		((renderingAttributes == null) || 
		 renderingAttributes.source.capabilityBitsEmpty()) &&
		((polygonAttributes == null) || 
		 polygonAttributes.source.capabilityBitsEmpty()) &&
		((lineAttributes == null) || 
		 lineAttributes.source.capabilityBitsEmpty()) &&
		((pointAttributes == null) || 
		 pointAttributes.source.capabilityBitsEmpty()) &&
		((material == null) || 
		 material.source.capabilityBitsEmpty()));

	if (!flag)
	    return flag;

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length && flag; i++) {
		if (texUnitState[i] != null) {
		    flag = flag && texUnitState[i].isStatic();
		}
	    }
	}

	return flag;
    }
    /*
    // Simply pass along to the NodeComponents
    void compile(CompileState compState) {
	setCompiled();

	if (texture != null) {
	   texture.compile(compState);
	}

	if (texCoordGeneration != null) {
	   texCoordGeneration.compile(compState);
	}

	if (textureAttributes != null) {
	   textureAttributes.compile(compState);
	}

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length; i++) {
		 if (texUnitState[i] != null)
		     texUnitState[i].compile(compState);
	    }
	}
	   
	if (coloringAttributes != null) {
	   coloringAttributes.compile(compState);
	}

	if (transparencyAttributes != null) {
	   transparencyAttributes.compile(compState);
	}

	if (renderingAttributes != null) {
	   renderingAttributes.compile(compState);
	}

	if (polygonAttributes != null) {
	   polygonAttributes.compile(compState);
	}

	if (lineAttributes != null) {
	   lineAttributes.compile(compState);
	}

	if (pointAttributes != null) {
	   pointAttributes.compile(compState);
	}

	if (material != null) {
	   material.compile(compState);
	}
    }
    */

    /**
     * Returns the hashcode for this object.
     * hashcode should be constant for object but same for two objects 
     * if .equals() is true.  For an appearance (where .equals() is going
     * to use the values in the appearance), the only way to have a
     * constant value is for all appearances to have the same hashcode, so
     * we use the hashcode of the class obj.
     *
     * Since hashCode is only used by AppearanceMap (at present) we may be 
     * able to improve efficency by calcing a hashCode from the values.
     */
    public int hashCode() {
	return getClass().hashCode();
    }

    public boolean equals(Object obj) {
	return ((obj instanceof AppearanceRetained) &&
		equals((AppearanceRetained) obj));
    }

    boolean equals(AppearanceRetained app) {
        boolean flag;

	flag = (app == this) ||
	       ((app != null) &&
	       (((material == app.material) ||
	         ((material != null) && material.equivalent(app.material))) &&
	        ((texture == app.texture) ||
	         ((texture != null) && texture.equals(app.texture))) &&
	        ((renderingAttributes == app.renderingAttributes) ||
	         ((renderingAttributes != null) && 
			renderingAttributes.equivalent(
				app.renderingAttributes))) &&
	        ((polygonAttributes == app.polygonAttributes) || 
		 ((polygonAttributes != null) && 
			polygonAttributes.equivalent(app.polygonAttributes))) &&
	        ((texCoordGeneration == app.texCoordGeneration) ||
	         ((texCoordGeneration != null) && 
			texCoordGeneration.equivalent(app.texCoordGeneration))) && 
	        ((textureAttributes == app.textureAttributes) ||
	         ((textureAttributes != null) && 
			textureAttributes.equivalent(app.textureAttributes))) && 
	        ((coloringAttributes == app.coloringAttributes) ||
	         ((coloringAttributes != null) && 
			coloringAttributes.equivalent(app.coloringAttributes))) && 
	        ((transparencyAttributes == app.transparencyAttributes) ||
	         ((transparencyAttributes != null) && 
			transparencyAttributes.equivalent(
				app.transparencyAttributes))) && 
	        ((lineAttributes == app.lineAttributes) ||
	         ((lineAttributes != null) && 
			lineAttributes.equivalent(app.lineAttributes))) && 
	        ((pointAttributes == app.pointAttributes) ||
	         ((pointAttributes != null) && 
			pointAttributes.equivalent(app.pointAttributes)))));

	if (!flag)
	    return (flag);

	if (texUnitState == app.texUnitState)
	    return (flag);

	if (texUnitState == null || app.texUnitState == null ||
		texUnitState.length != app.texUnitState.length)
	    return (false);

	for (int i = 0; i < texUnitState.length; i++) {
	     if (texUnitState[i] == app.texUnitState[i]) 
		 continue;

	     if (texUnitState[i] == null || app.texUnitState[i] == null ||
		    !texUnitState[i].equals(app.texUnitState[i]))
		return (false);
	}
	return (true);
    }




    synchronized void addAMirrorUser(Shape3DRetained shape) {

	super.addAMirrorUser(shape);
	if (material != null) 
	    material.addAMirrorUser(shape);

	if (texture != null) 
	    texture.addAMirrorUser(shape);
	if (texCoordGeneration != null) 
	    texCoordGeneration.addAMirrorUser(shape);
	if (textureAttributes != null) 
	    textureAttributes.addAMirrorUser(shape);

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length; i++) {
		if (texUnitState[i] != null)
		    texUnitState[i].addAMirrorUser(shape);
	    }
        }

	if (coloringAttributes != null) 
	    coloringAttributes.addAMirrorUser(shape);
	if (transparencyAttributes != null) 
	    transparencyAttributes.addAMirrorUser(shape);
	if (renderingAttributes != null) 
	    renderingAttributes.addAMirrorUser(shape);
	if (polygonAttributes != null) 
	    polygonAttributes.addAMirrorUser(shape);
	if (lineAttributes != null) 
	    lineAttributes.addAMirrorUser(shape);
	if (pointAttributes != null) 
	    pointAttributes.addAMirrorUser(shape);
    }

  synchronized void removeAMirrorUser(Shape3DRetained shape) {
	super.removeAMirrorUser(shape);
	if (material != null) 
	    material.removeAMirrorUser(shape);
	if (texture != null) 
	    texture.removeAMirrorUser(shape);
	if (texCoordGeneration != null) 
	    texCoordGeneration.removeAMirrorUser(shape);
	if (textureAttributes != null) 
	    textureAttributes.removeAMirrorUser(shape);

	if (texUnitState != null) {
	    for (int i = 0; i < texUnitState.length; i++) {
	 	 if (texUnitState[i] != null)
		     texUnitState[i].removeAMirrorUser(shape);
	    }
	}

	if (coloringAttributes != null) 
	    coloringAttributes.removeAMirrorUser(shape);
	if (transparencyAttributes != null) 
	    transparencyAttributes.removeAMirrorUser(shape);
	if (renderingAttributes != null) 
	    renderingAttributes.removeAMirrorUser(shape);
	if (polygonAttributes != null) 
	    polygonAttributes.removeAMirrorUser(shape);
	if (lineAttributes != null) 
	    lineAttributes.removeAMirrorUser(shape);
	if (pointAttributes != null) 
	    pointAttributes.removeAMirrorUser(shape);
    }

    // 3rd argument used only when Rendering Attr comp changes
    final void sendMessage(int attrMask, Object attr, boolean visible) {
	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  
	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.APPEARANCE_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);

	VirtualUniverse.mc.processMessage(createMessage);

	    
	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.APPEARANCE_CHANGED;
		
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    // Send the value itself, since Geometry Structure cannot rely on the
	    // mirror (which may be updated lazily)
	    if (attrMask == RENDERING) {
		if (attr != null) { 
		    createMessage.args[4] = visible?Boolean.TRUE:Boolean.FALSE; 
		} 
		else { 
		    createMessage.args[4] = Boolean.TRUE; 
		} 
	    } 
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

  

    final void sendRenderingAttributesChangedMessage(boolean visible) {

	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);  
	
	// System.out.println("univList.size is " + univList.size());
	for(int i=0; i<univList.size(); i++) {
	    J3dMessage createMessage = VirtualUniverse.mc.getMessage();
	    createMessage.threads = J3dThread.UPDATE_GEOMETRY;
	    createMessage.type = J3dMessage.RENDERINGATTRIBUTES_CHANGED;
	    
	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1] = null; // Sync with RenderingAttrRetained sendMessage
	    createMessage.args[2]= visible?Boolean.TRUE:Boolean.FALSE;
	    
	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    VirtualUniverse.mc.processMessage(createMessage);
	}
    }

    boolean isOpaque(int geoType) {
	TransparencyAttributesRetained ta;
	int i;

        ta = transparencyAttributes;
        if (ta != null &&
            ta.transparencyMode != TransparencyAttributes.NONE &&
	    (VirtualUniverse.mc.isD3D() ||
	     (!VirtualUniverse.mc.isD3D() &&
	     (ta.transparencyMode !=
	      TransparencyAttributes.SCREEN_DOOR)))) {
           return(false);
        } 

	switch (geoType) {
	case GeometryRetained.GEO_TYPE_POINT_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_POINT_SET:
            if ((pointAttributes != null) &&
                pointAttributes.pointAntialiasing) {
		return (false);
	    }
	    break;
	case GeometryRetained.GEO_TYPE_LINE_SET:
	case GeometryRetained.GEO_TYPE_LINE_STRIP_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_SET:
	case GeometryRetained.GEO_TYPE_INDEXED_LINE_STRIP_SET:
            if ((lineAttributes != null) &&
		lineAttributes.lineAntialiasing) {
		return (false);
	    }
	    break;
	case GeometryRetained.GEO_TYPE_RASTER:
	case GeometryRetained.GEO_TYPE_COMPRESSED:
	    break;
	default:
	    if (polygonAttributes != null) {
		if((polygonAttributes.polygonMode == 
		    PolygonAttributes.POLYGON_POINT) &&
		   (pointAttributes != null) &&
		   pointAttributes.pointAntialiasing) {
		    return (false);
		} else if ((polygonAttributes.polygonMode == 
			    PolygonAttributes.POLYGON_LINE) &&
			   (lineAttributes != null) &&
			    lineAttributes.lineAntialiasing) {
		    return (false);
		}
	    }
	    break;
	}

        return(true);
    }    

    void handleFrequencyChange(int bit) {
	int mask = 0;
	if (bit == Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE)
	    mask = COLOR;
	else if(bit == Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE)
	    mask = TRANSPARENCY;
	else if(bit == Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE)
	    mask = RENDERING;
	else if (bit == Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE)
	    mask = POLYGON;
	else if (bit == Appearance.ALLOW_LINE_ATTRIBUTES_WRITE)
	    mask = LINE;
	else if (bit == Appearance.ALLOW_POINT_ATTRIBUTES_WRITE)
	    mask = POINT;
	else if (bit == Appearance.ALLOW_MATERIAL_WRITE)
	    mask = MATERIAL;
	else if (bit == Appearance.ALLOW_TEXTURE_WRITE)
	    mask = TEXTURE;
	else if (bit == Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE)
	    mask = TEXTURE_ATTR;
	else if (bit == Appearance.ALLOW_TEXGEN_WRITE)
	    mask = TEXCOORD_GEN;
	else if (bit == Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE)
	    mask = TEXTURE_UNIT_STATE;

	if (mask != 0)
	    setFrequencyChangeMask(bit, mask);
    }
}


