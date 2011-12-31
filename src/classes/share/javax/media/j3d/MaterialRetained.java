/*
 * $RCSfile$
 *
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
 * $Revision$
 * $Date$
 * $State$
 */

package javax.media.j3d;

import java.util.ArrayList;

import javax.vecmath.Color3f;

/**
 * The MaterialRetained object defines the appearance of an object under
 * illumination.
 */
class MaterialRetained extends NodeComponentRetained {
    // Initialize default values for all class variables
    Color3f ambientColor = new Color3f(0.2f, 0.2f, 0.2f);
    Color3f emissiveColor = new Color3f(0.0f, 0.0f, 0.0f);
    Color3f diffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
    Color3f specularColor = new Color3f(1.0f, 1.0f, 1.0f);
    float shininess = 64.0f;
    int colorTarget = Material.DIFFUSE;

    // Lighting enable switch for this material object
    boolean	lightingEnable = true;

    // A list of pre-defined bits to indicate which component
    // in this Material object changed.
    static final int AMBIENT_COLOR_CHANGED      = 0x01;

    static final int EMISSIVE_COLOR_CHANGED     = 0x02;

    static final int DIFFUSE_COLOR_CHANGED      = 0x04;

    static final int SPECULAR_COLOR_CHANGED     = 0x08;

    static final int SHININESS_CHANGED          = 0x10;

    static final int ENABLE_CHANGED             = 0x20;

    static final int COLORTARGET_CHANGED        = 0x40;

    /**
     * Constructs and initializes a new material object using the specified
     * parameters.
     * @param ambientColor the material's ambient color
     * @param emissiveColor the material's emissive color
     * @param diffuseColor the material's diffuse color when illuminated by a
     * light
     * @param specularColor the material's specular color when illuminated
     * to generate a highlight
     * @param shininess the material's shininess in the
     * range [1.0, 128.0] with 1.0 being not shiny and 128.0 being very shiny
     */
    void createMaterial(Color3f aColor,
		    Color3f eColor,
		    Color3f dColor,
		    Color3f sColor,
		    float shine)
    {
	ambientColor.set(aColor);
	emissiveColor.set(eColor);
	diffuseColor.set(dColor);
	specularColor.set(sColor);
	shininess = shine;
    }

    /** Initializes this material's ambient color
     * This specifies how much ambient light is reflected by
     * the surface.  The ambient light color is the product of this
     * color and the material diffuseColor.
     * @param color the material's ambient color
     */
     final void initAmbientColor(Color3f color) {
	this.ambientColor.set(color);
     }

    /**
     * Sets this material's ambient color and sends a message notifying
     * the interested structures of the change.
     * This specifies how much ambient light is reflected by
     * the surface.  The ambient light color is the product of this
     * color and the material diffuseColor.
     * @param color the material's ambient color
     */
     final void setAmbientColor(Color3f color) {
	initAmbientColor(color);
	sendMessage(AMBIENT_COLOR_CHANGED, new Color3f(color));
    }

    /**
     * Sets this material's ambient color
     * @param r the new ambient color's red component
     * @param g the new ambient color's green component
     * @param b the new ambient color's blue component
     */
     final void initAmbientColor(float r, float g, float b) {
	this.ambientColor.set(r, g, b);
     }


    /**
     * Sets this material's ambient color and sends a message notifying
     * the interested structures of the change.
     * @param r the new ambient color's red component
     * @param g the new ambient color's green component
     * @param b the new ambient color's blue component
     */
     final void setAmbientColor(float r, float g, float b) {
	initAmbientColor(r, g, b);
	sendMessage(AMBIENT_COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Retrieves this material's ambient color.
     * @return the material's ambient color
     */
     final void getAmbientColor(Color3f color) {
	color.set(this.ambientColor);
    }

    /**
     * Sets this material's emissive color
     * This is the color of light, if any, that the material emits.
     * @param color the new emissive color
     */
     final void initEmissiveColor(Color3f color) {
	this.emissiveColor.set(color);
    }

    /**
     * Sets this material's emissive color and sends a message notifying
     * the interested structures of the change.
     * This is the color of light, if any, that the material emits.
     * @param color the new emissive color
     */
     final void setEmissiveColor(Color3f color) {
	initEmissiveColor(color);
	sendMessage(EMISSIVE_COLOR_CHANGED, new Color3f(color));
     }

    /**
     * Sets this material's emissive color.
     * This is the color of light, if any, that the material emits.
     * @param r the new emissive color's red component
     * @param g the new emissive color's green component
     * @param b the new emissive color's blue component
     */
     final void initEmissiveColor(float r, float g, float b) {
	this.emissiveColor.set(r, g, b);
    }

    /**
     * Sets this material's emissive color and sends a message notifying
     * the interested structures of the change.
     * This is the color of light, if any, that the material emits.
     * @param r the new emissive color's red component
     * @param g the new emissive color's green component
     * @param b the new emissive color's blue component
     */
     final void setEmissiveColor(float r, float g, float b) {
	initEmissiveColor(r, g, b);
	sendMessage(EMISSIVE_COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Retrieves this material's emissive color and stores it in the
     * argument provided.
     * @param color the vector that will receive this material's emissive color
     */
     final void getEmissiveColor(Color3f color) {
	color.set(this.emissiveColor);
    }

    /**
     * Sets this material's diffuse color.
     * This is the color of the material when illuminated by a light source.
     * @param color the new diffuse color
     */
    final void initDiffuseColor(Color3f color) {
	this.diffuseColor.set(color);
    }

    /**
     * Sets this material's diffuse color and sends a message notifying
     * the interested structures of the change.
     * This is the color of the material when illuminated by a light source.
     * @param color the new diffuse color
     */
    final void setDiffuseColor(Color3f color) {
	initDiffuseColor(color);
	sendMessage(DIFFUSE_COLOR_CHANGED, new Color3f(color));
     }

    /**
     * Sets this material's diffuse color.
     * @param r the new diffuse color's red component
     * @param g the new diffuse color's green component
     * @param b the new diffuse color's blue component
     */
    final void initDiffuseColor(float r, float g, float b) {
	this.diffuseColor.set(r, g, b);
    }

    /**
     * Sets this material's diffuse color and sends a message notifying
     * the interested structures of the change.
     * @param r the new diffuse color's red component
     * @param g the new diffuse color's green component
     * @param b the new diffuse color's blue component
     */
    final void setDiffuseColor(float r, float g, float b) {
	initDiffuseColor(r, g, b);
	sendMessage(DIFFUSE_COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Sets this material's diffuse color plus alpha.
     * This is the color of the material when illuminated by a light source.
     * @param r the new diffuse color's red component
     * @param g the new diffuse color's green component
     * @param b the new diffuse color's blue component
     * @param a the alpha component used to set transparency
     */
    final void initDiffuseColor(float r, float g, float b, float a) {
	this.diffuseColor.set(r, g, b);
    }

    /**
     * Sets this material's diffuse color plus alpha and sends
     * a message notifying the interested structures of the change.
     * This is the color of the material when illuminated by a light source.
     * @param r the new diffuse color's red component
     * @param g the new diffuse color's green component
     * @param b the new diffuse color's blue component
     * @param a the alpha component used to set transparency
     */
    final void setDiffuseColor(float r, float g, float b, float a) {
	initDiffuseColor(r, g, b);
	sendMessage(DIFFUSE_COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Retrieves this material's diffuse color.
     * @param color the vector that will receive this material's diffuse color
     */
     final void getDiffuseColor(Color3f color) {
	color.set(this.diffuseColor);
    }

    /**
     * Sets this material's specular color.
     * This is the specular highlight color of the material.
     * @param color the new specular color
     */
    final void initSpecularColor(Color3f color) {
	this.specularColor.set(color);
    }

    /**
     * Sets this material's specular color and sends a message notifying
     * the interested structures of the change.
     * This is the specular highlight color of the material.
     * @param color the new specular color
     */
    final void setSpecularColor(Color3f color) {
	initSpecularColor(color);
	sendMessage(SPECULAR_COLOR_CHANGED, new Color3f(color));
     }

    /**
     * Sets this material's specular color.
     * This is the specular highlight color of the material.
     * @param r the new specular color's red component
     * @param g the new specular color's green component
     * @param b the new specular color's blue component
     */
    final void initSpecularColor(float r, float g, float b) {
	this.specularColor.set(r, g, b);
     }


    /**
     * Sets this material's specular color and sends a message notifying
     * the interested structures of the change.
     * This is the specular highlight color of the material.
     * @param r the new specular color's red component
     * @param g the new specular color's green component
     * @param b the new specular color's blue component
     */
    final void setSpecularColor(float r, float g, float b) {
	initSpecularColor(r, g, b);
	sendMessage(SPECULAR_COLOR_CHANGED, new Color3f(r, g, b));
    }

    /**
     * Retrieves this material's specular color.
     * @param color the vector that will receive this material's specular color
     */
     final void getSpecularColor(Color3f color) {
	color.set(this.specularColor);
    }

      /**
       * Sets this material's shininess.
       * This specifies a material specular exponent, or shininess.
       * It takes a floating point number in the range [1.0, 128.0]
       * with 1.0 being not shiny and 128.0 being very shiny.
       * @param shininess the material's shininess
       */
      final void initShininess(float shininess) {
	  // Clamp shininess value
	  if (shininess < 1.0f)
	      this.shininess = 1.0f;
	  else if (shininess > 128.0f)
	      this.shininess = 128.0f;
	  else
	      this.shininess = shininess;

      }

      /**
       * Sets this material's shininess and sends a message notifying
       * the interested structures of the change.
       * This specifies a material specular exponent, or shininess.
       * It takes a floating point number in the range [1.0, 128.0]
       * with 1.0 being not shiny and 128.0 being very shiny.
       * @param shininess the material's shininess
       */
      final void setShininess(float shininess) {
	  initShininess(shininess);
	  sendMessage(SHININESS_CHANGED, new Float(this.shininess));
      }

      /**
       * Retrieves this material's shininess.
       * @return the material's shininess
       */
      final float getShininess() {
	  return this.shininess;
      }

    /**
     * Enables or disables lighting for this appearance component object.
     * @param state true or false to enable or disable lighting
     */
    void initLightingEnable(boolean state) {
	lightingEnable = state;
    }

    /**
     * Enables or disables lighting for this appearance component object
     * and sends a message notifying
     * the interested structures of the change.
     * @param state true or false to enable or disable lighting
     */
    void setLightingEnable(boolean state) {
	initLightingEnable(state);
	sendMessage(ENABLE_CHANGED,
		    (state ? Boolean.TRUE: Boolean.FALSE));
    }

    /**
     * Retrieves the state of the lighting enable flag.
     * @return true if lighting is enabled, false if lighting is disabled
     */
    boolean getLightingEnable() {
	return lightingEnable;
    }

    void initColorTarget(int colorTarget) {
	this.colorTarget = colorTarget;
    }

    final void setColorTarget(int colorTarget) {
	initColorTarget(colorTarget);
	sendMessage(COLORTARGET_CHANGED, new Integer(colorTarget));
    }

    final int getColorTarget() {
	return colorTarget;
    }

    synchronized void createMirrorObject() {
	if (mirror == null) {
	    // Check the capability bits and let the mirror object
	    // point to itself if is not editable
	    if (isStatic()) {
		mirror = this;
	    } else {
		MaterialRetained mirrorMat = new MaterialRetained();
		mirrorMat.set(this);
		mirrorMat.source = source;
		mirror = mirrorMat;
	    }
	} else {
	    ((MaterialRetained) mirror).set(this);
	}
    }


    /**
     * Updates the native context.
     */
    void updateNative(Context ctx,
		      float red, float green, float blue, float alpha,
		      boolean enableLighting) {
	Pipeline.getPipeline().updateMaterial(ctx, red, green, blue, alpha,
		     ambientColor.x, ambientColor.y, ambientColor.z,
		     emissiveColor.x, emissiveColor.y, emissiveColor.z,
		     diffuseColor.x, diffuseColor.y, diffuseColor.z,
		     specularColor.x, specularColor.y, specularColor.z,
		     shininess, colorTarget, enableLighting);
    }


    /**
     * Creates a mirror object, point the mirror object to the retained
     * object if the object is not editable
     */
    synchronized void initMirrorObject() {
	MaterialRetained mirrorMaterial = (MaterialRetained)mirror;
	mirrorMaterial.set(this);
    }

    /**
     * Update the "component" field of the mirror object with the
     * given "value"
     */
    synchronized void updateMirrorObject(int component, Object value) {
	MaterialRetained mirrorMaterial = (MaterialRetained)mirror;
	if ((component & AMBIENT_COLOR_CHANGED) != 0) {
	    mirrorMaterial.ambientColor = (Color3f)value;
	}
	else if ((component & EMISSIVE_COLOR_CHANGED) != 0) {
	    mirrorMaterial.emissiveColor = (Color3f)value;
	}
	else if ((component & DIFFUSE_COLOR_CHANGED) != 0) {
	    mirrorMaterial.diffuseColor = (Color3f)value;
	}
	else if ((component & SPECULAR_COLOR_CHANGED) != 0) {
	    mirrorMaterial.specularColor = (Color3f)value;
	}
	else if ((component & SHININESS_CHANGED) != 0) {
	    mirrorMaterial.shininess = ((Float)value).floatValue();
	}
	else if ((component & ENABLE_CHANGED) != 0) {
	    mirrorMaterial.lightingEnable = ((Boolean)value).booleanValue();
	}
	else if ((component & COLORTARGET_CHANGED) != 0) {
	    mirrorMaterial.colorTarget = ((Integer)value).intValue();
	}

    }


    boolean equivalent(MaterialRetained m) {
	return ((m != null) &&
		lightingEnable == m.lightingEnable &&
		diffuseColor.equals(m.diffuseColor) &&
		emissiveColor.equals(m.emissiveColor) &&
		specularColor.equals(m.specularColor) &&
		ambientColor.equals(m.ambientColor) &&
		colorTarget == m.colorTarget &&
		shininess == m.shininess);
    }


    // This functions clones the retained side only and is used
    // internally
     protected Object clone() {
         MaterialRetained mr = (MaterialRetained)super.clone();
	 // color can't share the same reference
	 mr.ambientColor = new Color3f(ambientColor);
	 mr.emissiveColor = new Color3f(emissiveColor);
	 mr.diffuseColor = new Color3f(diffuseColor);
	 mr.specularColor = new Color3f(specularColor);
	 // other attributes are copy by clone() automatically
         return mr;
     }

    protected void set(MaterialRetained mat) {
	 super.set(mat);

         // duplicate any referenced data
         ambientColor.set(mat.ambientColor);
         emissiveColor.set(mat.emissiveColor);
         diffuseColor.set(mat.diffuseColor);
         specularColor.set(mat.specularColor);
	 shininess = mat.shininess;
	 lightingEnable = mat.lightingEnable;
	 colorTarget = mat.colorTarget;
    }


    final void sendMessage(int attrMask, Object attr) {
       	ArrayList univList = new ArrayList();
	ArrayList gaList = Shape3DRetained.getGeomAtomsList(mirror.users, univList);
	// Send to rendering attribute structure, regardless of
	// whether there are users or not (alternate appearance case ..)
	J3dMessage createMessage = new J3dMessage();
	createMessage.threads = J3dThread.UPDATE_RENDERING_ATTRIBUTES;
	createMessage.type = J3dMessage.MATERIAL_CHANGED;
	createMessage.universe = null;
	createMessage.args[0] = this;
	createMessage.args[1]= new Integer(attrMask);
	createMessage.args[2] = attr;
	createMessage.args[3] = new Integer(changedFrequent);
	VirtualUniverse.mc.processMessage(createMessage);

	int size = univList.size();
	for(int i=0; i<size; i++) {
	    createMessage = new J3dMessage();
	    createMessage.threads = J3dThread.UPDATE_RENDER;
	    createMessage.type = J3dMessage.MATERIAL_CHANGED;

	    createMessage.universe = (VirtualUniverse) univList.get(i);
	    createMessage.args[0] = this;
	    createMessage.args[1]= new Integer(attrMask);
	    createMessage.args[2] = attr;

	    ArrayList gL = (ArrayList) gaList.get(i);
	    GeometryAtom[] gaArr = new GeometryAtom[gL.size()];
	    gL.toArray(gaArr);
	    createMessage.args[3] = gaArr;
	    VirtualUniverse.mc.processMessage(createMessage);
	}

    }

    void handleFrequencyChange(int bit) {
	if (bit == Material.ALLOW_COMPONENT_WRITE) {
	    setFrequencyChangeMask(Material.ALLOW_COMPONENT_WRITE, 0x1);
	}
    }
}

