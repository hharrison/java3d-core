/*
 * Copyright 2005-2008 Sun Microsystems, Inc.  All Rights Reserved.
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

import java.util.ArrayList;
import java.util.Map;


// XXXX : We should have a common Bin object that all other Bins extend from.


//class ShaderBin extends Object implements ObjectUpdate, NodeComponentUpdate {
class ShaderBin implements ObjectUpdate {

    /**
     * Node component dirty mask.
     */
    static final int SHADER_PROGRAM_DIRTY               = 0x1;
    static final int SHADER_ATTRIBUTE_SET_DIRTY         = 0x2;


    /**
     * The RenderBin for this object
     */
    RenderBin renderBin = null;

    /**
     * The AttributeBin that this ShaderBin resides
     */
    AttributeBin attributeBin = null;

    /**
     * The references to the next and previous ShaderBins in the
     * list.
     */
    ShaderBin next = null;
    ShaderBin prev = null;

    /**
     * The list of TextureBins in this ShaderBin
     */
    TextureBin textureBinList = null;

/**
 * The list of TextureBins to be added for the next frame
 */
ArrayList<TextureBin> addTextureBins = new ArrayList<TextureBin>();

    boolean onUpdateList = false;

    int numEditingTextureBins = 0;

    int componentDirty = 0;
    ShaderAppearanceRetained shaderAppearance = null;
    ShaderProgramRetained shaderProgram = null;
    ShaderAttributeSetRetained shaderAttributeSet = new ShaderAttributeSetRetained();

    ShaderBin(ShaderAppearanceRetained sApp,  RenderBin rBin) {
	reset(sApp, rBin);
    }

    void reset(ShaderAppearanceRetained sApp, RenderBin rBin) {
	prev = null;
	next = null;
        renderBin = rBin;
	attributeBin = null;
	textureBinList = null;
	onUpdateList = false;
	numEditingTextureBins = 0;
	addTextureBins.clear();
	if(sApp != null) {
	    shaderProgram = sApp.shaderProgram;
	    shaderAttributeSet = sApp.shaderAttributeSet;
	}
	else {
	    shaderProgram = null;
	    shaderAttributeSet = null;
	}
	shaderAppearance = sApp;
    }

    void clear() {
	reset(null, null);
    }

    /**
     * This tests if the qiven ra.shaderProgram  match this shaderProgram
     */
    boolean equals(ShaderAppearanceRetained sApp) {

	ShaderProgramRetained sp;
	ShaderAttributeSetRetained ss;

	if (sApp == null) {
	    sp = null;
	    ss = null;
	} else {
	    sp = sApp.shaderProgram;
	    ss = sApp.shaderAttributeSet;
	}

	if((shaderProgram != sp) || (shaderAttributeSet != ss)) {
	    return false;
	}

	return true;

    }

    @Override
    public void updateObject() {
	TextureBin t;
	int i;

	if (addTextureBins.size() > 0) {
		t = addTextureBins.get(0);
	    if (textureBinList == null) {
		textureBinList = t;

	    }
	    else {
		// Look for a TextureBin that has the same texture
		insertTextureBin(t);
	    }
	    for (i = 1; i < addTextureBins.size() ; i++) {
			t = addTextureBins.get(i);
		// Look for a TextureBin that has the same texture
		insertTextureBin(t);

	    }
	}
	addTextureBins.clear();
	onUpdateList = false;

    }

    void insertTextureBin(TextureBin t) {
	TextureBin tb;
	TextureRetained texture = null;

	if (t.texUnitState != null && t.texUnitState.length > 0) {
	    if (t.texUnitState[0] != null) {
	        texture = t.texUnitState[0].texture;
	    }
	}

	// use the texture in the first texture unit as the sorting criteria
	if (texture != null) {
	    tb = textureBinList;
	    while (tb != null) {
		if (tb.texUnitState == null || tb.texUnitState[0] == null ||
			tb.texUnitState[0].texture != texture) {
		    tb = tb.next;
		} else {
		    // put it here
		    t.next = tb;
		    t.prev = tb.prev;
		    if (tb.prev == null) {
		        textureBinList = t;
		    }
		    else {
		        tb.prev.next = t;
		    }
		    tb.prev = t;
		    return;
	        }
	    }
	}
	// Just put it up front
	t.prev = null;
	t.next = textureBinList;
	textureBinList.prev = t;
	textureBinList = t;

	t.tbFlag &= ~TextureBin.RESORT;
    }


    /**
     * reInsert textureBin if the first texture is different from
     * the previous bin and different from the next bin
     */
    void reInsertTextureBin(TextureBin tb) {

        TextureRetained texture = null,
                        prevTexture = null,
                        nextTexture = null;

        if (tb.texUnitState != null && tb.texUnitState[0] != null) {
            texture = tb.texUnitState[0].texture;
        }

        if (tb.prev != null && tb.prev.texUnitState != null) {
            prevTexture = tb.prev.texUnitState[0].texture;
        }

        if (texture != prevTexture) {
            if (tb.next != null && tb.next.texUnitState != null) {
                nextTexture = tb.next.texUnitState[0].texture;
            }
            if (texture != nextTexture) {
                if (tb.prev != null && tb.next != null) {
                    tb.prev.next = tb.next;
		    tb.next.prev = tb.prev;
                    insertTextureBin(tb);
                }
            }
        }
    }



    /**
     * Adds the given TextureBin to this AttributeBin.
     */
    void addTextureBin(TextureBin t, RenderBin rb, RenderAtom ra) {

	t.environmentSet = this.attributeBin.environmentSet;
	t.attributeBin = this.attributeBin;
	t.shaderBin = this;

	attributeBin.updateFromShaderBin(ra);
	addTextureBins.add(t);

	if (!onUpdateList) {
	    rb.objUpdateList.add(this);
	    onUpdateList = true;
	}
    }

    /**
     * Removes the given TextureBin from this ShaderBin.
     */
    void removeTextureBin(TextureBin t) {

	// If the TextureBin being remove is contained in addTextureBins,
	// then remove the TextureBin from the addList
	if (addTextureBins.contains(t)) {
	    addTextureBins.remove(addTextureBins.indexOf(t));
	}
	else {
	    if (t.prev == null) { // At the head of the list
		textureBinList = t.next;
		if (t.next != null) {
		    t.next.prev = null;
		}
	    } else { // In the middle or at the end.
		t.prev.next = t.next;
		if (t.next != null) {
		    t.next.prev = t.prev;
		}
	    }
	}

	t.shaderBin = null;
	t.prev = null;
	t.next = null;

	t.clear();

	if (textureBinList == null && addTextureBins.size() == 0 ) {
	    // Note: Removal of this shaderBin as a user of the rendering
	    // atttrs is done during removeRenderAtom() in RenderMolecule.java
	    attributeBin.removeShaderBin(this);
	}
    }

    /**
     * Renders this ShaderBin
     */
    void render(Canvas3D cv) {

	TextureBin tb;

	// System.err.println("ShaderBin.render() shaderProgram = " + shaderProgram);

        // include this ShaderBin to the to-be-updated list in canvas
        cv.setStateToUpdate(Canvas3D.SHADERBIN_BIT, this);

	tb = textureBinList;
	while (tb != null) {
	    tb.render(cv);
	    tb = tb.next;
	}
    }

    void updateAttributes(Canvas3D cv) {

        // System.err.println("ShaderBin.updateAttributes() shaderProgram is " + shaderProgram);
	if (shaderProgram != null) {
            // Compile, link, and enable shader program
	    shaderProgram.updateNative(cv, true);

	    if (shaderAttributeSet != null) {
		shaderAttributeSet.updateNative(cv, shaderProgram);
	    }

	}
	else {
	    if (cv.shaderProgram != null) {
                // Disable shader program
		cv.shaderProgram.updateNative(cv, false);
	    }
	}

        cv.shaderBin = this;
	cv.shaderProgram = shaderProgram;
    }

    void updateNodeComponent() {
	// System.err.println("ShaderBin.updateNodeComponent() ...");

	// We don't need to clone shaderProgram.
	// ShaderProgram object can't be modified once it is live,
	// so each update should be a new reference.
	if ((componentDirty & SHADER_PROGRAM_DIRTY) != 0) {
	    // System.err.println("  - SHADER_PROGRAM_DIRTY");

	    shaderProgram = shaderAppearance.shaderProgram;
	}

	// We need to clone the shaderAttributeSet.
	if ((componentDirty & SHADER_ATTRIBUTE_SET_DIRTY) != 0) {
	    // System.err.println("  - SHADER_ATTRIBUTE_SET_DIRTY");

		Map<String, ShaderAttributeRetained> attrs = shaderAttributeSet.getAttrs();
	    attrs.clear();
            if(shaderAppearance.shaderAttributeSet != null) {
                attrs.putAll(shaderAppearance.shaderAttributeSet.getAttrs());
            }
	}

	componentDirty = 0;
   }

    void incrActiveTextureBin() {
	numEditingTextureBins++;
    }

    void decrActiveTextureBin() {
	numEditingTextureBins--;
    }
}
